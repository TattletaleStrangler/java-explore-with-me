package ru.practicum.ewm.request.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import lombok.extern.jackson.Jacksonized;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.request.model.ParticipationRequest;
import ru.practicum.ewm.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Builder
@Getter
@Setter
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class ParticipationRequestDto {

    private Long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created;

    private Long event;

    private Long requester;

    private ParticipationRequest.Status status;

    @UtilityClass
    public static class RequestMapper {
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
            return builder()
                    .id(request.getId())
                    .created(request.getCreated())
                    .event(request.getEvent().getId())
                    .requester(request.getRequester().getId())
                    .status(request.getStatus())
                    .build();
        }
    }
}
