package ru.practicum.ewm.category.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NewCategoryDto {

    @NotBlank(message = "Field: name. Error: must not be blank.")
    @Size(min = 1, max = 50, message = "Field: name. Error: the length of the category name must be at least 2 and no more than 250 characters.")
    private String name;

}
