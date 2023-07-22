package ru.practicum.ewm.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.service.EventService;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(path = "/events")
public class PublicEventController {

    private static final String APP = "explore-with-me";
    private final EventService eventService;
    private final StatsClient statsClient;

    @GetMapping
    public List<EventShortDto> getEvents(@RequestParam(required = false) String text,
                                         @RequestParam(required = false) Boolean paid,
                                         @RequestParam(required = false) Boolean onlyAvailable,
                                         @RequestParam(required = false) List<Long> categories,
                                         @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                         @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                         @RequestParam(required = false, defaultValue = "EVENT_DATE") String sort,
                                         @RequestParam(required = false, defaultValue = "0") int from,
                                         @RequestParam(required = false, defaultValue = "10") int size,
                                         HttpServletRequest request) {
        log.info("GET /events text={},paid={},onlyAvailable={},categories={},rangeStart={},rangeEnd={},sort={},from={},size={}",
                text, paid, onlyAvailable, categories, rangeStart, rangeEnd, sort, from, size);

        EventParams params = EventParams.builder()
                .text(text)
                .paid(paid)
                .onlyAvailable(onlyAvailable)
                .categories(categories)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .sort(EventParams.Sort.valueOf(sort.toUpperCase()))
                .from(from)
                .size(size)
                .build();

        List<EventShortDto> result = eventService.getShortEvents(params);
        sendStats(request);
        return result;
    }

    @GetMapping("/{id}")
    public EventFullDto getEvent(@PathVariable(name = "id") Long eventId,
                                 HttpServletRequest request) {
        log.info("GET /{}", eventId);

        EventFullDto result = eventService.getEvent(eventId);
        sendStats(request);
        return result;
    }

    private void sendStats(HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        log.info("client ip: {}", ip);
        String uri = request.getRequestURI();
        log.info("endpoint path: {}", uri);

        EndpointHitDto endpointHitDto = EndpointHitDto.builder()
                .app(APP)
                .ip(ip)
                .uri(uri)
                .timestamp(LocalDateTime.now())
                .build();

        statsClient.createEndpointHit(endpointHitDto);
    }

}
