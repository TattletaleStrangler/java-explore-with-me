package ru.practicum.ewm.event.service;

import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPAExpressions;
import lombok.RequiredArgsConstructor;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.CategoryMapper;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.storage.CategoryStorage;
import ru.practicum.ewm.event.controller.EventParams;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.Location;
import ru.practicum.ewm.event.model.QEvent;
import ru.practicum.ewm.event.storage.EventStorage;
import ru.practicum.ewm.event.storage.LocationStorage;
import ru.practicum.ewm.exception.CategoryNotFoundException;
import ru.practicum.ewm.exception.EventNotFountException;
import ru.practicum.ewm.exception.NotMetConditionsException;
import ru.practicum.ewm.exception.UserNotFoundException;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.model.ParticipationRequest;
import ru.practicum.ewm.request.model.QParticipationRequest;
import ru.practicum.ewm.request.model.RequestMapper;
import ru.practicum.ewm.request.storage.RequestStorage;
import ru.practicum.ewm.user.dto.UserShortDto;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.model.UserMapper;
import ru.practicum.ewm.user.storage.UserStorage;
import ru.practicum.ewm.util.PredicateBuilder;

import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventStorage eventStorage;
    private final UserStorage userStorage;
    private final CategoryStorage categoryStorage;
    private final LocationStorage locationStorage;
    private final RequestStorage requestStorage;
    private final StatsClient statsClient;
    private static final String BASE_URI = "/events";

    //**
    //* ADMIN. Получение администратором полных событий
    //**
    public List<EventFullDto> getEvents(EventParams params) {
        checkParams(params);
        Predicate predicate = createPredicate(params);
        int from = params.getFrom();
        int size = params.getSize();
        Sort sort = Sort.by(Sort.Direction.ASC, "eventDate");

        PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size, sort);
        Iterable<Event> events = eventStorage.findAll(predicate, page);
        List<Event> eventList = new ArrayList<>();
        events.forEach(eventList::add);

        List<ConfirmedRequests> confirmedRequests = requestStorage.getConfirmedRequestsByEventAndStatus(eventList, ParticipationRequest.Status.CONFIRMED);
        Map<Long, ConfirmedRequests> groupedConfirmedRequests = confirmedRequests.stream()
                .collect(Collectors.toMap(ConfirmedRequests::getEventId, identity(), (existing, replacement) -> existing));

        Map<Long, String> uris = new HashMap<>();

        for (Event event : eventList) {
            URIBuilder uriBuilder = new URIBuilder()
                    .setPath(BASE_URI)
                    .setPath(String.valueOf(event.getId()));
            uris.put(event.getId(), uriBuilder.toString());
        }

        List<ViewStatsDto> viewStats = statsClient.getStatistics(LocalDateTime.now().minusMonths(1), LocalDateTime.now(),
                false, new ArrayList<>(uris.values()));

        Map<String, ViewStatsDto> groupedViews = viewStats.stream()
                .collect(Collectors.toMap(ViewStatsDto::getUri, identity(), (existing, replacement) -> existing));

        List<EventFullDto> eventFullDtos = new ArrayList<>();
        for (Event event : eventList) {
            CategoryDto categoryDto = CategoryMapper.categoryToDto(event.getCategory()); //todo хранить категории чтобы много раз одни и те же не маппить
            UserShortDto initiatorDto = UserMapper.userToShortDto(event.getInitiator());
            LocationDto locationDto = LocationMapper.locationToDto(event.getLocation());
            ViewStatsDto viewStatsDto = groupedViews.getOrDefault(uris.get(event.getId()), new ViewStatsDto(null, null, 0L));
            ConfirmedRequests confirmedRequestsForEvent = groupedConfirmedRequests.get(event.getId());
            long numConfirmedRequests = confirmedRequestsForEvent == null ? 0 : confirmedRequestsForEvent.getConfirmedRequests();
            EventFullDto dto = EventMapper.eventToFullDto(event, initiatorDto, categoryDto, locationDto,
                    viewStatsDto.getHits(), numConfirmedRequests);
            eventFullDtos.add(dto);
        }
        if (EventParams.Sort.VIEWS.equals(params.getSort())) {
            eventFullDtos.sort(Comparator.comparing(EventFullDto::getViews));
        }
        return eventFullDtos;
    }

    //**
    //* ADMIN. Обновление администратором события (отклонение/публикация)
    //* дата начала изменяемого события должна быть не ранее чем за час от даты публикации. (Ожидается код ошибки 409)
    //* событие можно публиковать, только если оно в состоянии ожидания публикации (Ожидается код ошибки 409)
    //* событие можно отклонить, только если оно еще не опубликовано (Ожидается код ошибки 409)
    //**
    public EventFullDto updateEvent(UpdateEventAdminRequest eventDto, Long eventId) {
        Event oldEvent = checkEventAndGet(eventId);

        if (!oldEvent.getState().equals(Event.State.PENDING)) {
            throw new NotMetConditionsException("Сan only publish pending events");
        }

        if (LocalDateTime.now().plusHours(1).isAfter(oldEvent.getEventDate())) {
            throw new NotMetConditionsException("It is possible to publish an event no earlier than an hour before the event");
        }

        updateEvent(oldEvent, eventDto);

        if (eventDto.getStateAction() != null) {
            if (eventDto.getStateAction().equals(UpdateEventAdminRequest.StateAction.REJECT_EVENT)) {
                oldEvent.setState(Event.State.CANCELED);
            }

            if (eventDto.getStateAction().equals(UpdateEventAdminRequest.StateAction.PUBLISH_EVENT)) {
                oldEvent.setState(Event.State.PUBLISHED);
                oldEvent.setPublishedOn(LocalDateTime.now());
            }
        }

        Event newEvent = eventStorage.save(oldEvent);

        Long confirmedRequests = requestStorage.countAllByEventIdAndStatus(eventId, ParticipationRequest.Status.CONFIRMED);

        List<ViewStatsDto> view = statsClient.getStatistics(LocalDateTime.now().minusMonths(1), LocalDateTime.now(),
                false, List.of(BASE_URI + "/" + newEvent.getId()));
        long hits = 0L;
        if (view != null && view.size() > 0) {
            hits = view.get(0).getHits();
        }
        CategoryDto categoryDto = CategoryMapper.categoryToDto(newEvent.getCategory());
        LocationDto locationDto = LocationMapper.locationToDto(newEvent.getLocation());
        UserShortDto initiatorDto = UserMapper.userToShortDto(newEvent.getInitiator());
        EventFullDto eventFullDto = EventMapper.eventToFullDto(newEvent, initiatorDto, categoryDto, locationDto, hits, confirmedRequests);
        return eventFullDto;
    }
//----------------------------------------------------------------------------------------------------------------------

    //**
    //* PUBLIC. Получение любым пользователем коротких событий
    //* это публичный эндпоинт, соответственно в выдаче должны быть только опубликованные события
    //* текстовый поиск (по аннотации и подробному описанию) должен быть без учета регистра букв
    //* если в запросе не указан диапазон дат [rangeStart-rangeEnd], то нужно выгружать события, которые произойдут позже текущей даты и времени
    //* информация о каждом событии должна включать в себя количество просмотров и количество уже одобренных заявок на участие
    //* информацию о том, что по этому эндпоинту был осуществлен и обработан запрос, нужно сохранить в сервисе статистики
    //**
    public List<EventShortDto> getShortEvents(EventParams params) {
//        params.setStates(List.of(Event.State.PUBLISHED));
        checkParams(params);
        Predicate predicate = createPredicate(params);
        int from = params.getFrom();
        int size = params.getSize();
        Sort sort = Sort.by(Sort.Direction.DESC, "eventDate");
        PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size, sort);
        Iterable<Event> events = eventStorage.findAll(predicate, page);
        List <Event> eventList1 = eventStorage.findAll();

        List<Event> eventList = new ArrayList<>();
        events.forEach(eventList::add);

        List<ConfirmedRequests> confirmedRequests = requestStorage.getConfirmedRequestsByEventAndStatus(eventList, ParticipationRequest.Status.CONFIRMED);
        Map<Long, ConfirmedRequests> groupedConfirmedRequests = confirmedRequests.stream()
                .collect(Collectors.toMap(ConfirmedRequests::getEventId, identity(), (existing, replacement) -> existing));

        Map<Long, String> uris = new HashMap<>();

        for (Event event : eventList) {
            URIBuilder uriBuilder = new URIBuilder()
                    .setPath(BASE_URI)
                    .setPath(String.valueOf(event.getId()));
            uris.put(event.getId(), uriBuilder.toString());
        }

        List<ViewStatsDto> viewStats = statsClient.getStatistics(LocalDateTime.now().minusMonths(1), LocalDateTime.now(),
                false, new ArrayList<>(uris.values()));

        Map<String, ViewStatsDto> sortViews = viewStats.stream()
                .collect(Collectors.toMap(ViewStatsDto::getUri, identity(), (existing, replacement) -> existing));

        List<EventShortDto> eventShortDtoList = new ArrayList<>();
        for (Event event : eventList) {
            CategoryDto categoryDto = CategoryMapper.categoryToDto(event.getCategory());
            UserShortDto initiatorDto = UserMapper.userToShortDto(event.getInitiator());
            ViewStatsDto viewStatsDto = sortViews.getOrDefault(uris.get(event.getId()), new ViewStatsDto(null, null, 0L));
            ConfirmedRequests confirmedRequestsForEvent = groupedConfirmedRequests.get(event.getId());
            long numConfirmedRequests = confirmedRequestsForEvent == null ? 0 : confirmedRequestsForEvent.getConfirmedRequests();
            EventShortDto dto = EventMapper.eventToShortDto(event, initiatorDto, categoryDto, viewStatsDto.getHits(),
                    numConfirmedRequests);
            eventShortDtoList.add(dto);
        }
        if (EventParams.Sort.VIEWS.equals(params.getSort())) {
            eventShortDtoList.sort(Comparator.comparing(EventShortDto::getViews));
        }
        return eventShortDtoList;
    }

    //**
    //* PUBLIC. Получение любым пользователем полного события
    //* событие должно быть опубликовано
    //* информация о событии должна включать в себя количество просмотров и количество подтвержденных запросов
    //* информацию о том, что по этому эндпоинту был осуществлен и обработан запрос, нужно сохранить в сервисе статистики
    //**
    public EventFullDto getEvent(Long eventId) {
        Event event = checkEventAndGet(eventId);
        if (!event.getState().equals(Event.State.PUBLISHED)) {
            throw new EventNotFountException("An unregistered user can view only published events.");
        }

        List<ViewStatsDto> view = statsClient.getStatistics(LocalDateTime.now().minusMonths(1), LocalDateTime.now(),
                true, List.of(BASE_URI + "/" + event.getId()));
        long hit = 0L;
        if (view != null && view.size() > 0) {
            hit = view.get(0).getHits();
        }

        long confirmedRequests = requestStorage.countAllByEventIdAndStatus(eventId, ParticipationRequest.Status.CONFIRMED);
        UserShortDto initiatorDto = UserMapper.userToShortDto(event.getInitiator());
        CategoryDto categoryDto = CategoryMapper.categoryToDto(event.getCategory());
        LocationDto locationDto = LocationMapper.locationToDto(event.getLocation());
        return EventMapper.eventToFullDto(event, initiatorDto, categoryDto, locationDto, hit, confirmedRequests);
    }
//----------------------------------------------------------------------------------------------------------------------

    //**
    //* PRIVATE. Создание зарегистрированным пользователем события
    //**
    public EventFullDto createEvent(NewEventDto eventDto, Long userId) {
        User initiator = checkUserAndGet(userId);
        Category category = checkCategoryAndGet(eventDto.getCategory());
        Location location = locationStorage.findByLatAndLon(eventDto.getLocation().getLat(), eventDto.getLocation().getLon())
                .orElse(locationStorage.save(LocationMapper.dtoToLocation(eventDto.getLocation())));

        Event event = EventMapper.dtoToEvent(eventDto, initiator, category, location);
        event.setState(Event.State.PENDING);
        Event savedEvent = eventStorage.save(event);

        UserShortDto userShortDto = UserMapper.userToShortDto(initiator);
        CategoryDto categoryDto = CategoryMapper.categoryToDto(category);
        LocationDto locationDto = LocationMapper.locationToDto(location);
        long initConfirmedRequests = 0L;
        long initViews = 0L;
        return EventMapper.eventToFullDto(savedEvent, userShortDto, categoryDto, locationDto, initViews, initConfirmedRequests);
    }

    //**
    //* PRIVATE. Получение зарегистрированным пользователем своих коротких событий
    //**
    public List<EventShortDto> getEvents(Long userId, int from, int size) {
        User initiator = checkUserAndGet(userId);
        UserShortDto initiatorDto = UserMapper.userToShortDto(initiator);

        PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size, Sort.by("id"));
        List<Event> events = eventStorage.findAllByInitiatorId(userId, page);

        List<ConfirmedRequests> confirmedRequests = requestStorage.getConfirmedRequestsByEventAndStatus(events, ParticipationRequest.Status.CONFIRMED);
        Map<Long, ConfirmedRequests> groupedConfirmedRequests = confirmedRequests.stream()
                .collect(Collectors.toMap(ConfirmedRequests::getEventId, identity(), (existing, replacement) -> existing));

        Map<Long, String> uris = new HashMap<>();

        for (Event event : events) {
            URIBuilder uriBuilder = new URIBuilder()
                    .setPath(BASE_URI)
                    .setPath(String.valueOf(event.getId()));
            uris.put(event.getId(), uriBuilder.toString());
        }

        List<ViewStatsDto> viewStats = statsClient.getStatistics(LocalDateTime.now().minusMonths(1), LocalDateTime.now(),
                false, new ArrayList<>(uris.values()));

        Map<String, ViewStatsDto> sortViews = viewStats.stream()
                .collect(Collectors.toMap(ViewStatsDto::getUri, identity(), (existing, replacement) -> existing));

        List<EventShortDto> eventShortDtoList = new ArrayList<>();
        for (Event event : events) {
            CategoryDto categoryDto = CategoryMapper.categoryToDto(event.getCategory());
            ViewStatsDto viewStatsDto = sortViews.getOrDefault(uris.get(event.getId()), new ViewStatsDto(null, null, 0L));
            ConfirmedRequests confirmedRequestsForEvent = groupedConfirmedRequests.get(event.getId());
            long numConfirmedRequests = confirmedRequestsForEvent == null ? 0 : confirmedRequestsForEvent.getConfirmedRequests();
            EventShortDto dto = EventMapper.eventToShortDto(event, initiatorDto, categoryDto, viewStatsDto.getHits(),
                    numConfirmedRequests);
            eventShortDtoList.add(dto);
        }

        return eventShortDtoList;
    }

    //**
    //* PRIVATE. Получение зарегистрированным пользователем своего полного события
    //**
    public EventFullDto getEvent(Long userId, Long eventId) {
        User initiator = checkUserAndGet(userId);
        UserShortDto initiatorDto = UserMapper.userToShortDto(initiator);

        Event event = checkEventAndGet(eventId);
        List<ViewStatsDto> view = statsClient.getStatistics(LocalDateTime.now().minusMonths(1), LocalDateTime.now(),
                false, List.of(BASE_URI + "/" + event.getId()));
        long hit = 0L;
        if (view != null && view.size() > 0) {
            hit = view.get(0).getHits();
        }

        long confirmedRequests = requestStorage.countAllByEventIdAndStatus(eventId, ParticipationRequest.Status.CONFIRMED);
        CategoryDto categoryDto = CategoryMapper.categoryToDto(event.getCategory());
        LocationDto locationDto = LocationMapper.locationToDto(event.getLocation());
        return EventMapper.eventToFullDto(event, initiatorDto, categoryDto, locationDto, hit, confirmedRequests);
    }

    //**
    //* PRIVATE. Обновление зарегистрированным пользователем своего события
    //* изменить можно только отмененные события или события в состоянии ожидания модерации (Ожидается код ошибки 409)
    //* дата и время на которые намечено событие не может быть раньше, чем через два часа от текущего момента (Ожидается код ошибки 409)
    //**
    public EventFullDto updateEvent(UpdateEventUserRequest eventDto, Long userId, Long eventId) {
        User initiator = checkUserAndGet(userId);
        UserShortDto initiatorDto = UserMapper.userToShortDto(initiator);
        Event oldEvent = checkEventAndGet(eventId);

        if (oldEvent.getState().equals(Event.State.PUBLISHED)) {
            throw new NotMetConditionsException("Only pending or canceled events can be changed.");
        }
        if (LocalDateTime.now().plusHours(2).isAfter(oldEvent.getEventDate())) {
            throw new NotMetConditionsException("Only edit events that are more than two hours away.");
        }

        updateEvent(oldEvent, eventDto);
        if (eventDto.getStateAction() != null) {
            if (eventDto.getStateAction().equals(UpdateEventUserRequest.StateAction.CANCEL_REVIEW)) {
                oldEvent.setState(Event.State.CANCELED);
            } else if (eventDto.getStateAction().equals(UpdateEventUserRequest.StateAction.SEND_TO_REVIEW)) {
                oldEvent.setState(Event.State.PENDING);
            }
        }

        Event newEvent = eventStorage.save(oldEvent);

        List<ViewStatsDto> view = statsClient.getStatistics(LocalDateTime.now().minusMonths(1), LocalDateTime.now(),
                false, List.of(BASE_URI + "/" + newEvent.getId()));
        long hits = 0L;
        if (view != null && view.size() > 0) {
            hits = view.get(0).getHits();
        }

        long confirmedRequests = requestStorage.countAllByEventIdAndStatus(eventId, ParticipationRequest.Status.CONFIRMED);
        CategoryDto categoryDto = CategoryMapper.categoryToDto(newEvent.getCategory());
        LocationDto locationDto = LocationMapper.locationToDto(newEvent.getLocation());
        EventFullDto eventFullDto = EventMapper.eventToFullDto(newEvent, initiatorDto, categoryDto, locationDto, hits, confirmedRequests);
        return eventFullDto;
    }

    //**
    //* PRIVATE. Получение информации о запросах на участие в событии текущего пользователя
    //**
    public List<ParticipationRequestDto> getRequestsForEvent(Long userId, Long eventId) {
        User user = checkUserAndGet(userId);
        Event event = checkEventAndGet(eventId);

        if (!user.getId().equals(event.getInitiator().getId())) {
            throw new NotMetConditionsException("You can only get information about requests for your own event.");
        }

        List<ParticipationRequest> requests = requestStorage.findAllByEventId(event.getId());
        return RequestMapper.requestListToDtoList(requests);
    }

    //**
    //* PRIVATE. Подтверждение зарегистрированным пользователем заявок на своё событие
    //* если для события лимит заявок равен 0 или отключена пре-модерация заявок, то подтверждение заявок не требуется
    //* нельзя подтвердить заявку, если уже достигнут лимит по заявкам на данное событие (Ожидается код ошибки 409)
    //* статус можно изменить только у заявок, находящихся в состоянии ожидания (Ожидается код ошибки 409)
    //* если при подтверждении данной заявки, лимит заявок для события исчерпан, то все неподтверждённые заявки необходимо отклонить
    //**
    public EventRequestStatusUpdateResult confirmRequest(EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest,
                                                         Long userId, Long eventId) {
        User user = checkUserAndGet(userId);
        Event event = checkEventAndGet(eventId);

        if (!user.getId().equals(event.getInitiator().getId())) {
            throw new NotMetConditionsException("You can only confirm applications for your own event.");
        }

        final long countConfirmedRequests = requestStorage.countAllByEventIdAndStatus(event.getId(), ParticipationRequest.Status.CONFIRMED);
        final long participantLimit = event.getParticipantLimit();
        final long expectedCountConfirmedRequests = countConfirmedRequests + eventRequestStatusUpdateRequest.getRequestIds().size();
        if (participantLimit != 0 && expectedCountConfirmedRequests > participantLimit) {
            throw new NotMetConditionsException("The participant limit has been reached.");
        }

        List<ParticipationRequest> requests = requestStorage.findAllByEventIdAndIdIn(eventId, eventRequestStatusUpdateRequest.getRequestIds());

        List<ParticipationRequest> confirmedRequests = new ArrayList<>();
        List<ParticipationRequest> rejectedRequests = new ArrayList<>();

        final EventRequestStatusUpdateRequest.Status status = eventRequestStatusUpdateRequest.getStatus();

        requests
                .forEach((r) -> {
                    if (!r.getStatus().equals(ParticipationRequest.Status.PENDING)) {
                        throw new NotMetConditionsException("Сan only change the status of an application in the pending stage.");
                    }
                    if (status == EventRequestStatusUpdateRequest.Status.REJECTED) {
                        r.setStatus(ParticipationRequest.Status.REJECTED);
                        rejectedRequests.add(r);
                    } else {
                        r.setStatus(ParticipationRequest.Status.CONFIRMED);
                        confirmedRequests.add(r);
                    }
                });

        requestStorage.saveAll(requests);

        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(RequestMapper.requestListToDtoList(confirmedRequests))
                .rejectedRequests(RequestMapper.requestListToDtoList(rejectedRequests))
                .build();
    }

    private Predicate createPredicate(EventParams params) {
        QEvent event = QEvent.event;
        QParticipationRequest request = QParticipationRequest.participationRequest;

        if (params.getOnlyAvailable() != null && !params.getOnlyAvailable()) {
            params.setOnlyAvailable(null);
        }

        Predicate result =
        PredicateBuilder.builder()
                .add(params.getText(), List.of(event.annotation::containsIgnoreCase,
                        event.description::containsIgnoreCase))
                .addPredicates(params.getOnlyAvailable(),
                        List.of(event.participantLimit.eq(0),
                                event.participantLimit.gt(
                                        JPAExpressions
                                                .select(request.count())
                                                .from(request)
                                                .where(request.event.id.eq(event.id))
                                )))
                .add(params.getPaid(), event.paid.isTrue())
                .add(params.getUsers(), event.initiator.id::in)
                .add(params.getStates(), event.state::in)
                .add(params.getCategories(), event.category.id::in)
                .add(params.getRangeStart(), event.eventDate::goe)
                .add(params.getRangeEnd(), event.eventDate::loe)
                .buildAnd();

        return result;
    }

    private User checkUserAndGet(long userId) {
        return userStorage.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Category with id=" + userId + " was not found"));
    }

    private Category checkCategoryAndGet(long categoryId) {
        return categoryStorage.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Category with id=" + categoryId + " was not found"));
    }

    private Event checkEventAndGet(long eventId) {
        return eventStorage.findById(eventId)
                .orElseThrow(() -> new EventNotFountException("Category with id=" + eventId + " was not found"));
    }

    private void updateEvent(Event event, UpdateEventRequest newDto) {
        if (newDto.getTitle() != null) {
            event.setTitle(newDto.getTitle());
        }
        if (newDto.getDescription() != null) {
            event.setDescription(newDto.getDescription());
        }
        if (newDto.getAnnotation() != null) {
            event.setAnnotation(newDto.getAnnotation());
        }

        LocalDateTime newEventDate = newDto.getEventDate();
        if (newEventDate != null) {
            if (LocalDateTime.now().isAfter(newDto.getEventDate())) {
                throw new ValidationException("Event date must be in the future");
            }
            if (LocalDateTime.now().isAfter(newEventDate) || LocalDateTime.now().isEqual(newEventDate)) {
                throw new ValidationException("Event date must be in the future");
            }
            event.setEventDate(newDto.getEventDate());
        }
        if (newDto.getPaid() != null) {
            event.setPaid(newDto.getPaid());
        }
        if (newDto.getParticipantLimit() != null) {
            event.setParticipantLimit(newDto.getParticipantLimit());
        }
        if (newDto.getRequestModeration() != null) {
            event.setRequestModeration(newDto.getRequestModeration());
        }
        if (newDto.getCategoryId() != null && newDto.getCategoryId() != event.getCategory().getId()) {
            Category newCategory = checkCategoryAndGet(newDto.getCategoryId());
            event.setCategory(newCategory);
        }
        if (newDto.getLocation() != null &&
                (!Objects.equals(newDto.getLocation().getLat(), event.getLocation().getLat()) ||
                         !Objects.equals(newDto.getLocation().getLon(), event.getLocation().getLon()))) {
            Location newLocation = locationStorage.findByLatAndLon(newDto.getLocation().getLat(), newDto.getLocation().getLon())
                    .orElse(locationStorage.save(LocationMapper.dtoToLocation(newDto.getLocation())));
            event.setLocation(newLocation);
        }
    }

    private void checkParams(EventParams params) {
        if (params.getUsers() != null) {
            for (Long id : params.getUsers()) {
                if (id <= 0L) {
                    throw new ValidationException("id must be positive");
                }
            }
        }

        if (params.getCategories() != null) {
            for (Long id : params.getCategories()) {
                if (id <= 0L) {
                    throw new ValidationException("id must be positive");
                }
            }
        }

        if (params.getRangeStart() == null && params.getRangeEnd() == null) {
            params.setRangeStart(LocalDateTime.now());
        }
    }

}
