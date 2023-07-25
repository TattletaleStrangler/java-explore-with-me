package ru.practicum.ewm.user.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder
@Getter
@Setter
public class UserDto {
    private Long id;

    private String name;

    private String email;

    private List<Long> subscriptions;
}
