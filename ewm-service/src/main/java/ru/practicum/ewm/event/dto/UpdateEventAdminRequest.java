package ru.practicum.ewm.event.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class UpdateEventAdminRequest extends UpdateEventRequest {

    private StateAction stateAction;

    public enum StateAction {
        PUBLISH_EVENT, REJECT_EVENT
    }
}
