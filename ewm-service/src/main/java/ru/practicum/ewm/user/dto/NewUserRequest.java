package ru.practicum.ewm.user.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.*;

@Builder
@Getter
@Setter
public class NewUserRequest {

    @NotBlank(message = "Field: name. Error: must not be blank.")
    @Size(min = 2, max = 250, message = "Field: name. Error: the length of the username must be at least 2 and no more than 250 characters.")
    @NotNull(message = "Field: name. Error: must not be blank. Value: null")
    private String name;

    @NotBlank(message = "Field: email. Error: must not be blank.")
    @Size(min = 6, max = 254, message = "Field: email. Error: the length of the email must be at least 2 and no more than 254 characters.")
    @Email(message = "Email not valid")
    private String email;

}
