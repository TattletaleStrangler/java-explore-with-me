package ru.practicum.ewm.request.dto;

import lombok.Getter;
import lombok.Setter;
import ru.practicum.ewm.request.model.ParticipationRequest;

import java.util.List;

@Getter
@Setter
public class EventRequestStatusUpdateRequest {

    private List<Long> requestIds;

    private Status status;

    public enum Status {
        REJECTED,
        CONFIRMED
    }
}
