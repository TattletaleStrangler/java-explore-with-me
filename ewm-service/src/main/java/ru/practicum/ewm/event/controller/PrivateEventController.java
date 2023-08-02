package ru.practicum.ewm.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEventUserRequest;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(path = "/users/{userId}/events")
public class PrivateEventController {

    private final EventService eventService;

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public EventFullDto createEvent(@Valid @RequestBody NewEventDto eventDto,
                                    @PathVariable Long userId) {
        log.info("POST /users/{}/events", userId);
        return eventService.createEvent(eventDto, userId);
    }

    @GetMapping
    public List<EventShortDto> getEvents(@PathVariable Long userId,
                                         @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                         @RequestParam(defaultValue = "10") @Positive int size) {
        log.info("GET /users/{}/events?from={}&size={}", userId, from, size);
        return eventService.getEvents(userId, from, size);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEvent(@PathVariable Long userId,
                                 @PathVariable Long eventId) {
        log.info("GET /users/{}/events/{}", userId, eventId);
        return eventService.getEvent(userId, eventId);
    }

    @PatchMapping("{eventId}")
    public EventFullDto updateEvent(@RequestBody @Valid UpdateEventUserRequest eventDto,
                                    @PathVariable Long eventId,
                                    @PathVariable Long userId) {
        log.info("PATCH /users/{}/events/{}", userId, eventId);
        return eventService.updateEvent(eventDto, userId, eventId);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getRequestsForEvent(@PathVariable Long userId,
                                                             @PathVariable Long eventId) {
        log.info("GET /users/{}/events/{}/requests", userId, eventId);
        return eventService.getRequestsForEvent(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult confirmRequest(@RequestBody EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest,
                                                         @PathVariable Long eventId,
                                                         @PathVariable Long userId) {
        log.info("PATCH /users/{}/events/{}", userId, eventId);
        return eventService.confirmRequest(eventRequestStatusUpdateRequest, userId, eventId);
    }

    /**
     * метод для получения событий пользователей, на которых подписан
     */
    @GetMapping("/subscriptions")
    public List<EventShortDto> getSubscriptions(@PathVariable Long userId,
                                                @RequestParam(required = false) List<Long> users,
                                                @RequestParam(required = false) List<Event.State> states,
                                                @RequestParam(required = false) String text,
                                                @RequestParam(required = false) Boolean paid,
                                                @RequestParam(required = false) Boolean onlyAvailable,
                                                @RequestParam(required = false) List<Long> categories,
                                                @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                                @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                                @RequestParam(defaultValue = "EVENT_DATE") String sort,
                                                @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                                @RequestParam(defaultValue = "10") @Positive int size) {
        log.info("GET /users/{}/events/subscriptions?users={},states={},text={},paid={},onlyAvailable={},categories={}" +
                ",rangeStart={},rangeEnd={},sort={},from={},size={}", userId, users, states, text, paid, onlyAvailable,
                categories, rangeStart, rangeEnd, sort, from, size);
        EventParams params = EventParams.builder()
                .users(users)
                .states(states)
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

        return eventService.getSubscriptions(userId, params);
    }


}
