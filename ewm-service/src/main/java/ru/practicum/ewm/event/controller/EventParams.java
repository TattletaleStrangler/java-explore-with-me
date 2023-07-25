package ru.practicum.ewm.event.controller;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.ewm.event.model.Event;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@Setter
public class EventParams {

    private String text;
    private Boolean onlyAvailable;
    private Boolean paid;
    private List<Long> users;
    private List<Event.State> states;
    private List<Long> categories;
    private LocalDateTime rangeStart;
    private LocalDateTime rangeEnd;
    private Sort sort;
    private int from;
    private int size;

    public enum Sort {
        EVENT_DATE, VIEWS
    }

}
