package ru.practicum.ewm.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;
import ru.practicum.ewm.event.validator.EventDateValid;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class NewEventDto {

    @NotBlank(message = "Field: title. Error: must not be blank.")
    @Size(min = 3, max = 120, message = "Field: title. Error: the length of the title must be at least 3 and no more than 120 characters.")
    private String title;

    @NotBlank(message = "Field: description. Error: must not be blank.")
    @Size(min = 20, max = 7000, message = "Field: description. Error: the length of the description must be at least 20 and no more than 7000 characters.")
    private String description;

    @NotBlank(message = "Field: annotation. Error: must not be blank.")
    @Size(min = 20, max = 2000, message = "Field: annotation. Error: the length of the description must be at least 20 and no more than 2000 characters.")
    private String annotation;

    @NotNull
    private Long category;

    //todo в сообщении вывод текущего значения
    @NotNull
    @EventDateValid(message = "Field: eventDate. Error: должно содержать дату, которая еще не наступила.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    @NotNull
    private LocationDto location;

    private Boolean paid;

    private Integer participantLimit;

    private Boolean requestModeration;
}
