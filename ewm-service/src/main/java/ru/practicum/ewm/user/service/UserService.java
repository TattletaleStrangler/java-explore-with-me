package ru.practicum.ewm.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.exception.UserNotFoundException;
import ru.practicum.ewm.user.dto.NewUserRequest;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.model.UserMapper;
import ru.practicum.ewm.user.storage.UserStorage;

import javax.validation.ValidationException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserStorage userStorage;

    @Transactional
    public UserDto createUser(NewUserRequest userDto) {
        User user = UserMapper.dtoToUser(userDto);
        User savedUser = userStorage.save(user);
        return UserMapper.userToDto(savedUser);
    }

    public List<UserDto> getUsers(List<Long> ids, Integer from, Integer size) {
        PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size, Sort.by("id"));
        List<User> users;

        if (ids != null && ids.size() > 0) {
            users = userStorage.findAllByIdIn(ids, page);
        } else {
            users = userStorage.findAll(page).toList();
        }

        return UserMapper.userListToDto(users);
    }

    @Transactional
    public void deleteUser(Long userId) {
        userStorage.deleteById(userId);
    }

    @Transactional
    public UserDto subscribeToUser(Long subscriberId, Long eventMakerId) {
        User subscriber = checkUserAndGet(subscriberId);
        User eventMaker = checkUserAndGet(eventMakerId);
        if (subscriber.getSubscriptions().add(eventMaker)) {
            User savedUser = userStorage.save(subscriber);
            return UserMapper.userToDto(savedUser);
        } else {
            throw new ValidationException("The user with id=" + eventMakerId + " is already subscribed");
        }
    }

    @Transactional
    public void unfollowUser(Long subscriberId, Long eventMakerId) {
        User subscriber = checkUserAndGet(subscriberId);
        User eventMaker = checkUserAndGet(eventMakerId);
        if (subscriber.getSubscriptions().remove(eventMaker)) {
            userStorage.save(subscriber);
        } else {
            throw new ValidationException("User with id=" + eventMakerId + " is not among subscriptions");
        }
    }

    private User checkUserAndGet(long userId) {
        return userStorage.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Category with id=" + userId + " was not found"));
    }

}
