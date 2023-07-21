package ru.practicum.ewm.request.model;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class RequestMapper {
    public ParticipationRequest dtoToRequest(User requester, Event event) {
        return ParticipationRequest.builder()
                .created(LocalDateTime.now())
                .event(event)
                .requester(requester)
                .status(ParticipationRequest.Status.PENDING)
                .build();
    }

    public List<ParticipationRequestDto> requestListToDtoList(List<ParticipationRequest> requests) {
        return requests.stream()
                .map(RequestMapper::requestToDto)
                .collect(Collectors.toList());
    }

    public ParticipationRequestDto requestToDto(ParticipationRequest request) {
        return ParticipationRequestDto.builder()
                .id(request.getId())
                .created(request.getCreated())
                .event(request.getEvent().getId())
                .requester(request.getRequester().getId())
                .status(request.getStatus())
                .build();
    }
}
