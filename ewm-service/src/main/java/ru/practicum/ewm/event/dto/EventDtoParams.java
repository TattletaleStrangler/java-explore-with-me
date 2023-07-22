package ru.practicum.ewm.event.dto;

import lombok.Builder;
import lombok.Getter;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.user.dto.UserShortDto;

@Builder
@Getter
public class EventDtoParams {
    private Event event;
    private UserShortDto initiator;
    private CategoryDto category;
    private LocationDto location;
    private Long views;
    private Long confirmedRequests;
}
