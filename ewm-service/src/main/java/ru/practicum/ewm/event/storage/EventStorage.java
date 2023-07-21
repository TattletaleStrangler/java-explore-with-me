package ru.practicum.ewm.event.storage;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.ewm.event.model.Event;

import java.util.List;

public interface EventStorage extends JpaRepository<Event, Long>, QuerydslPredicateExecutor<Event> {

    List<Event> findAllByInitiatorId(Long id, PageRequest page);

    List<Event> findAllByIdIn(List<Long> ids);

    Boolean existsEventByCategoryId(Long categoryId);
}
