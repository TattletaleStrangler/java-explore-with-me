package ru.practicum.ewm.compilation.service;

import lombok.RequiredArgsConstructor;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.CategoryMapper;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.compilation.storage.CompilationStorage;
import ru.practicum.ewm.event.dto.ConfirmedRequests;
import ru.practicum.ewm.event.dto.EventMapper;
import ru.practicum.ewm.event.dto.EventShortDto;
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
public class CompilationService {

    private static final String BASE_URI = "/events";
    private final CompilationStorage compilationStorage;
    private final RequestStorage requestStorage;
    private final EventStorage eventStorage;
    private final StatsClient statsClient;

    public CompilationDto createCompilation(NewCompilationDto compilationDto) {
        if (compilationDto.getPinned() == null) {
            compilationDto.setPinned(false);
        }
        Compilation compilation = newDtoToCompilation(compilationDto);
        Compilation savedCompilation = compilationStorage.save(compilation);
        return compilationToDto(savedCompilation);
    }

    public CompilationDto updateCompilation(NewCompilationDto compilationDto, Long compId) {
        Compilation oldCompilation = compilationStorage.findById(compId)
                .orElseThrow(() -> new CompilationNotFoundException("Compilation with id=" + compId + " was not found"));

        updateCompilation(oldCompilation, compilationDto);
        Compilation updatedCompilation = compilationStorage.save(oldCompilation);
        return compilationToDto(updatedCompilation);
    }

    public void deleteCompilation(Long compId) {
        compilationStorage.deleteById(compId);
    }

    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size);
        List<Compilation> compilations = compilationStorage.findAllByPinned(pinned, page);
        //todo слить events в один map<event.id, event> и разом их перевести в eventDto, потом по id доставать
        List<CompilationDto> compilationDtoList = compilations.stream()
                .map(this::compilationToDto)
                .collect(Collectors.toList());
        return compilationDtoList;
    }

    public CompilationDto getCompilation(Long compId) {
        Compilation compilation = compilationStorage.findById(compId)
                .orElseThrow(() -> new CompilationNotFoundException("Compilation with id=" + compId + " was not found"));

        CompilationDto compilationDto = compilationToDto(compilation);
        return compilationDto;
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

    private CompilationDto compilationToDto(Compilation compilation) {
        List<EventShortDto> eventShortDtoList = new ArrayList<>();

        Set<Event> events = compilation.getEvents();
        List<Event> eventList = new ArrayList<>(events);
        List<ConfirmedRequests> confirmedRequests = requestStorage.getConfirmedRequestsByEventAndStatus(eventList, ParticipationRequest.Status.CONFIRMED);
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
        for (Event event : events) {
            CategoryDto categoryDto = CategoryMapper.categoryToDto(event.getCategory());
            UserShortDto initiatorDto = UserMapper.userToShortDto(event.getInitiator());
            ViewStatsDto viewStatsDto = sortViews.getOrDefault(uris.get(event.getId()), new ViewStatsDto(null, null, 0L));
            ConfirmedRequests confirmedRequestsForEvent = groupedConfirmedRequests.get(event.getId());
            long numConfirmedRequests = confirmedRequestsForEvent == null ? 0 : confirmedRequestsForEvent.getConfirmedRequests();
            EventShortDto dto = EventMapper.eventToShortDto(event, initiatorDto, categoryDto, viewStatsDto.getHits(),
                    numConfirmedRequests);
            eventShortDtoList.add(dto);
        }

        return CompilationDto.builder()
                .id(compilation.getId())
                .title(compilation.getTitle())
                .pinned(compilation.getPinned())
                .events(eventShortDtoList)
                .build();
    }

}
