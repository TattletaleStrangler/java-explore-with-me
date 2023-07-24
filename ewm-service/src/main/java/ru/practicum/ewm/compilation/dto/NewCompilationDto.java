package ru.practicum.ewm.compilation.dto;

import lombok.Getter;
import lombok.Setter;
import ru.practicum.ewm.util.ValidateMarker;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

@Getter
@Setter
public class NewCompilationDto {

    private List<Long> events;

    private Boolean pinned;

    @NotBlank (groups = ValidateMarker.Create.class, message = "Compilation title cannot be blank.")
    @Size(min = 1, max = 50, groups = {ValidateMarker.Create.class, ValidateMarker.Update.class},
            message = "The length of the compilation title cannot be less than 1 and greater than 50.")
    private String title;

}
