package ru.practicum.ewm.request.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.event.dto.ConfirmedRequests;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.request.model.ParticipationRequest;

import java.util.List;

public interface RequestStorage extends JpaRepository<ParticipationRequest, Long> {
    long countAllByEventIdAndStatus(long eventId, ParticipationRequest.Status status);

    List<ParticipationRequest> findAllByRequesterIdOrderByCreatedDesc(long requesterId);

    List<ParticipationRequest> findAllByEventId(long eventId);

    List<ParticipationRequest> findAllByEventIdAndIdIn(Long eventId, List<Long> requestIds);

    @Query("SELECT new ru.practicum.ewm.event.dto.ConfirmedRequests(request.event.id, COUNT(request.event.id)) FROM ParticipationRequest AS request " +
            "WHERE request.status = :status AND request.event IN :events " +
            "GROUP BY request.event.id ")
    List<ConfirmedRequests> getRequestsByEventAndStatus(@Param("events") List<Event> events, @Param("status") ParticipationRequest.Status status);

}
