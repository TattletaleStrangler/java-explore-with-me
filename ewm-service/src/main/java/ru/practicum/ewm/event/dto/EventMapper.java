package ru.practicum.ewm.event.dto;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.Location;
import ru.practicum.ewm.user.model.User;

import java.time.LocalDateTime;

@UtilityClass
public class EventMapper {

    public Event dtoToEvent(NewEventDto dto, User user, Category category, Location location) {
        return Event.builder()
                .title(dto.getTitle())
                .annotation(dto.getAnnotation())
                .description(dto.getDescription())
                .category(category)
                .initiator(user)
                .createdOn(LocalDateTime.now())
                .eventDate(dto.getEventDate())
                .paid(dto.getPaid() != null && dto.getPaid())
                .participantLimit(dto.getParticipantLimit() == null ? 0 : dto.getParticipantLimit())
                .requestModeration(dto.getRequestModeration() == null || dto.getRequestModeration())
                .location(location)
                .build();
    }

    public EventFullDto eventToFullDto(EventDtoParams params) {
        return EventFullDto.builder()
                .id(params.getEvent().getId())
                .title(params.getEvent().getTitle())
                .description(params.getEvent().getDescription())
                .annotation(params.getEvent().getAnnotation())
                .createdOn(params.getEvent().getCreatedOn())
                .eventDate(params.getEvent().getEventDate())
                .publishedOn(params.getEvent().getPublishedOn())
                .paid(params.getEvent().getPaid())
                .requestModeration(params.getEvent().getRequestModeration())
                .participantLimit(params.getEvent().getParticipantLimit())
                .views(params.getViews())
                .confirmedRequests(params.getConfirmedRequests())
                .initiator(params.getInitiator())
                .category(params.getCategory())
                .location(params.getLocation())
                .state(params.getEvent().getState())
                .build();
    }

    public EventShortDto eventToShortDto(EventDtoParams params) {
        return EventShortDto.builder()
                .id(params.getEvent().getId())
                .title(params.getEvent().getTitle())
                .annotation(params.getEvent().getAnnotation())
                .paid(params.getEvent().getPaid())
                .confirmedRequests(params.getConfirmedRequests())
                .eventDate(params.getEvent().getEventDate())
                .initiator(params.getInitiator())
                .category(params.getCategory())
                .views(params.getViews())
                .build();
    }

}
