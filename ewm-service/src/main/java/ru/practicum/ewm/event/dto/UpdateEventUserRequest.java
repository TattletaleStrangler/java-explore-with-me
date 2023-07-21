package ru.practicum.ewm.event.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateEventUserRequest extends UpdateEventRequest {

    private StateAction stateAction;

    public enum StateAction {
        SEND_TO_REVIEW, CANCEL_REVIEW
    }
}
