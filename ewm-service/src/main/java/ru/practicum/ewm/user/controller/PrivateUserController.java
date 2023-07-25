package ru.practicum.ewm.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.service.UserService;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(path = "/users")
public class PrivateUserController {

    private final UserService userService;

    /**
     * метод для подписки на пользователя
     */
    @PostMapping("/{subscriberId}/{eventMakerId}")
    @ResponseStatus(code = HttpStatus.CREATED)
    public UserDto subscribeToUser(@PathVariable Long subscriberId,
                                   @PathVariable Long eventMakerId) {
        log.info("POST /users/{}/{}", subscriberId, eventMakerId);
        return userService.subscribeToUser(subscriberId, eventMakerId);
    }

    /**
     * метод для отписки от пользователя
     */
    @DeleteMapping("/{subscriberId}/{eventMakerId}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void unfollowUser(@PathVariable Long subscriberId,
                                @PathVariable Long eventMakerId) {
        log.info("DELETE /users/{}/{}", subscriberId, eventMakerId);
        userService.unfollowUser(subscriberId, eventMakerId);
    }

}
