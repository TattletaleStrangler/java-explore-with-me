package ru.practicum.ewm.compilation.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.ewm.event.dto.EventShortDto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Builder
@Getter
@Setter
public class CompilationDto {

    @NotNull
    private Long id;

    @NotNull
    @Size(min = 1, max = 50)
    private String title;

    private List<EventShortDto> events;

    @NotNull
    private Boolean pinned;

}
