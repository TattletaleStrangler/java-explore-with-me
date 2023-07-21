package ru.practicum.ewm.user.storage;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.user.model.User;

import java.util.List;

public interface UserStorage extends JpaRepository<User, Long> {

    List<User> findAllByIdIn(List<Long> ids, PageRequest page);

}
