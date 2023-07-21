package ru.practicum.ewm.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEventUserRequest;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;

import javax.validation.Valid;
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
                                         @RequestParam(required = false, defaultValue = "0") int from,
                                         @RequestParam(required = false, defaultValue = "10") int size) {
        log.info("GET /users/{}/events", userId);
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

    @GetMapping("{eventId}/requests")
    public List<ParticipationRequestDto> getRequestsForEvent(@PathVariable Long userId,
                                                             @PathVariable Long eventId) {
        log.info("GET /users/{}/events/{}/requests", userId, eventId);
        return eventService.getRequestsForEvent(userId, eventId);
    }

    @PatchMapping("{eventId}/requests")
    public EventRequestStatusUpdateResult confirmRequest(@RequestBody EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest,
                                                         @PathVariable Long eventId,
                                                         @PathVariable Long userId) {
        log.info("PATCH /users/{}/events/{}", userId, eventId);
        return eventService.confirmRequest(eventRequestStatusUpdateRequest, userId, eventId);
    }
}
