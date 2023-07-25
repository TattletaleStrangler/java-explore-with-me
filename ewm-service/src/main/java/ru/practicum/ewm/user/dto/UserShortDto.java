package ru.practicum.ewm.user.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Builder
@Getter
@Setter
public class UserShortDto {
    @NotNull
    private Long id;
    @NotNull
    private String name;
}
