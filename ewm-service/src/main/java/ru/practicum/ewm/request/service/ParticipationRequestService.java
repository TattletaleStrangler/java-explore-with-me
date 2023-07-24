package ru.practicum.ewm.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.storage.EventStorage;
import ru.practicum.ewm.exception.EventNotFountException;
import ru.practicum.ewm.exception.NotMetConditionsException;
import ru.practicum.ewm.exception.UserNotFoundException;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.model.ParticipationRequest;
import ru.practicum.ewm.request.storage.RequestStorage;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.storage.UserStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipationRequestService {

    private final RequestStorage requestStorage;

    private final UserStorage userStorage;

    private final EventStorage eventStorage;

    /**
    * нельзя добавить повторный запрос (Ожидается код ошибки 409)
    * инициатор события не может добавить запрос на участие в своём событии (Ожидается код ошибки 409)
    * нельзя участвовать в неопубликованном событии (Ожидается код ошибки 409)
    * если у события достигнут лимит запросов на участие - необходимо вернуть ошибку (Ожидается код ошибки 409)
    * если для события отключена пре-модерация запросов на участие, то запрос должен автоматически перейти в состояние подтвержденного
    */
    @Transactional
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {

        User requester = checkUserAndGet(userId);
        Event event = checkEventAndGet(eventId);

        if (userId.equals(event.getInitiator().getId())) {
            throw new NotMetConditionsException("The initiator of the event cannot add a request to participation in his event.");
        }

        if (!event.getState().equals(Event.State.PUBLISHED)) {
            throw new NotMetConditionsException("You can't participate in an unpublished event");
        }

        long countConfirmedRequests = requestStorage.countAllByEventIdAndStatus(event.getId(), ParticipationRequest.Status.CONFIRMED);
        long participantLimit = event.getParticipantLimit();

        if (participantLimit != 0 && countConfirmedRequests >= participantLimit) {
            throw new NotMetConditionsException("The participant limit has been reached");
        }

        ParticipationRequest participationRequest = ParticipationRequestDto.RequestMapper.dtoToRequest(requester, event);

        if (!event.getRequestModeration() || participantLimit == 0) {
            participationRequest.setStatus(ParticipationRequest.Status.CONFIRMED);
        }

        ParticipationRequest savedParticipationRequest = requestStorage.save(participationRequest);

        return ParticipationRequestDto.RequestMapper.requestToDto(savedParticipationRequest);
    }

    public List<ParticipationRequestDto> getRequests(Long userId) {
        List<ParticipationRequest> participationRequests = requestStorage.findAllByRequesterIdOrderByCreatedDesc(userId);
        return ParticipationRequestDto.RequestMapper.requestListToDtoList(participationRequests);
    }

    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        checkUserAndGet(userId);
        ParticipationRequest request = checkRequestAndGet(requestId);

        if (!request.getRequester().getId().equals(userId)) {
            throw new NotMetConditionsException("You can't cancel another user's participant request.");
        }

        request.setStatus(ParticipationRequest.Status.CANCELED);
        requestStorage.save(request);
        return ParticipationRequestDto.RequestMapper.requestToDto(request);
    }

    private User checkUserAndGet(long userId) {
        return userStorage.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Category with id=" + userId + " was not found"));
    }

    private Event checkEventAndGet(long eventId) {
        return eventStorage.findById(eventId)
                .orElseThrow(() -> new EventNotFountException("Category with id=" + eventId + " was not found"));
    }

    private ParticipationRequest checkRequestAndGet(long requestId) {
        return requestStorage.findById(requestId)
                .orElseThrow(() -> new EventNotFountException("Request with id=" + requestId + " was not found"));
    }

}
