package ru.practicum.ewm.compilation.service;

import lombok.RequiredArgsConstructor;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.CategoryMapper;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.compilation.storage.CompilationStorage;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.storage.EventStorage;
import ru.practicum.ewm.exception.CompilationNotFoundException;
import ru.practicum.ewm.request.model.ParticipationRequest;
import ru.practicum.ewm.request.storage.RequestStorage;
import ru.practicum.ewm.user.dto.UserShortDto;
import ru.practicum.ewm.user.model.UserMapper;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationService {

    private static final String BASE_URI = "/events";
    private final CompilationStorage compilationStorage;
    private final RequestStorage requestStorage;
    private final EventStorage eventStorage;
    private final StatsClient statsClient;

    @Transactional
    public CompilationDto createCompilation(NewCompilationDto compilationDto) {
        if (compilationDto.getPinned() == null) {
            compilationDto.setPinned(false);
        }
        Compilation compilation = newDtoToCompilation(compilationDto);
        Compilation savedCompilation = compilationStorage.save(compilation);
        return compilationToDto(savedCompilation);
    }

    @Transactional
    public CompilationDto updateCompilation(NewCompilationDto compilationDto, Long compId) {
        Compilation oldCompilation = checkCompilationAndGet(compId);

        updateCompilation(oldCompilation, compilationDto);
        Compilation updatedCompilation = compilationStorage.save(oldCompilation);
        return compilationToDto(updatedCompilation);
    }

    @Transactional
    public void deleteCompilation(Long compId) {
        compilationStorage.deleteById(compId);
    }

    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size);
        List<Compilation> compilations = compilationStorage.findAllByPinned(pinned, page);
        return compilationListToDto(compilations);
    }

    public CompilationDto getCompilation(Long compId) {
        Compilation compilation = checkCompilationAndGet(compId);
        return compilationToDto(compilation);
    }

    private Compilation newDtoToCompilation(NewCompilationDto dto) {
        Set<Event> eventSet = new HashSet<>();
        if (dto.getEvents() != null) {
            List<Event> events = eventStorage.findAllByIdIn(dto.getEvents());
            eventSet.addAll(events);
        }

        return Compilation.builder()
                .title(dto.getTitle())
                .pinned(dto.getPinned())
                .events(eventSet)
                .build();
    }

    private Compilation checkCompilationAndGet(Long compId) {
        return compilationStorage.findById(compId)
                .orElseThrow(() -> new CompilationNotFoundException("Compilation with id=" + compId + " was not found"));
    }

    private void updateCompilation(Compilation oldCompilation, NewCompilationDto dto) {
        String title = dto.getTitle();
        if (title != null) {
            oldCompilation.setTitle(title);
        }

        if (dto.getPinned() != null) {
            oldCompilation.setPinned(dto.getPinned());
        }

        if (dto.getEvents() != null && dto.getEvents().size() > 0) {
            List<Event> events = eventStorage.findAllByIdIn(dto.getEvents());
            Set<Event> eventSet = new HashSet<>(events);
            oldCompilation.setEvents(eventSet);
        }

    }

    private List<CompilationDto> compilationListToDto(List<Compilation> compilations) {
        List<CompilationDto> compilationDtoList = new ArrayList<>();

        Set<Event> events = new HashSet<>();
        compilations.forEach((c) -> events.addAll(c.getEvents()));
        List<Event> eventList = new ArrayList<>(events);

        Map<Long, ConfirmedRequests> groupedConfirmedRequests = getConfirmedRequests(eventList);
        Map<Long, String> uris = new HashMap<>();
        Map<String, ViewStatsDto> sortViews = getStatistics(eventList, uris, false);

        for (Compilation compilation : compilations) {
            List<EventShortDto> eventShortDtoList = eventListToShortDtoList(compilation.getEvents(), uris, sortViews, groupedConfirmedRequests);

            compilationDtoList.add(CompilationDto.builder()
                    .id(compilation.getId())
                    .title(compilation.getTitle())
                    .pinned(compilation.getPinned())
                    .events(eventShortDtoList)
                    .build());
        }
        return compilationDtoList;
    }

    private CompilationDto compilationToDto(Compilation compilation) {
        Set<Event> events = compilation.getEvents();
        List<Event> eventList = new ArrayList<>(events);

        Map<Long, ConfirmedRequests> groupedConfirmedRequests = getConfirmedRequests(eventList);
        Map<Long, String> uris = new HashMap<>();
        Map<String, ViewStatsDto> sortViews = getStatistics(eventList, uris, false);

        List<EventShortDto> eventShortDtoList = eventListToShortDtoList(events, uris, sortViews, groupedConfirmedRequests);

        return CompilationDto.builder()
                .id(compilation.getId())
                .title(compilation.getTitle())
                .pinned(compilation.getPinned())
                .events(eventShortDtoList)
                .build();
    }

    private List<EventShortDto> eventListToShortDtoList(Set<Event> events, Map<Long, String> uris, Map<String,
            ViewStatsDto> sortViews, Map<Long, ConfirmedRequests> groupedConfirmedRequests) {
        List<EventShortDto> eventShortDtoList = new ArrayList<>();

        for (Event event : events) {
            ViewStatsDto viewStatsDto = sortViews.getOrDefault(uris.get(event.getId()), new ViewStatsDto(null, null, 0L));
            ConfirmedRequests confirmedRequestsForEvent = groupedConfirmedRequests.get(event.getId());
            long numConfirmedRequests = confirmedRequestsForEvent == null ? 0 : confirmedRequestsForEvent.getConfirmedRequests();
            EventDtoParams eventDtoParams = createEventDtoParams(event, viewStatsDto.getHits(), numConfirmedRequests);
            EventShortDto dto = EventMapper.eventToShortDto(eventDtoParams);
            eventShortDtoList.add(dto);
        }

        return eventShortDtoList;
    }

    private Map<Long, ConfirmedRequests> getConfirmedRequests(List<Event> events) {
        List<ConfirmedRequests> confirmedRequests = requestStorage.getRequestsByEventAndStatus(events, ParticipationRequest.Status.CONFIRMED);
        return confirmedRequests.stream()
                .collect(Collectors.toMap(ConfirmedRequests::getEventId, identity(), (existing, replacement) -> existing));
    }

    private Map<String, ViewStatsDto> getStatistics(List<Event> events, Map<Long, String> uris, Boolean unique) {
        for (Event event : events) {
            URIBuilder uriBuilder = new URIBuilder()
                    .setPath(BASE_URI)
                    .setPath(String.valueOf(event.getId()));
            uris.put(event.getId(), uriBuilder.toString());
        }

        List<ViewStatsDto> viewStats = statsClient.getStatistics(LocalDateTime.now().minusMonths(1), LocalDateTime.now(),
                unique, new ArrayList<>(uris.values()));

        return viewStats.stream()
                .collect(Collectors.toMap(ViewStatsDto::getUri, identity(), (existing, replacement) -> existing));
    }

    private EventDtoParams createEventDtoParams(Event event, long hits, long confirmedRequests) {
        UserShortDto initiatorDto = UserMapper.userToShortDto(event.getInitiator());
        CategoryDto categoryDto = CategoryMapper.categoryToDto(event.getCategory());
        LocationDto locationDto = LocationMapper.locationToDto(event.getLocation());
        return EventDtoParams.builder()
                .event(event)
                .initiator(initiatorDto)
                .category(categoryDto)
                .location(locationDto)
                .views(hits)
                .confirmedRequests(confirmedRequests)
                .build();
    }

}
