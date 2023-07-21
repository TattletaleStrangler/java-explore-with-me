package ru.practicum.ewm.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.user.dto.NewUserRequest;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.model.UserMapper;
import ru.practicum.ewm.user.storage.UserStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl {

    private final UserStorage userStorage;

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

    public void deleteUser(Long userId) {
        userStorage.deleteById(userId);
    }

}
