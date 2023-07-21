package ru.practicum.ewm.event.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;

public class CheckEventDateValidator implements ConstraintValidator<EventDateValid, LocalDateTime> {
    @Override
    public void initialize(EventDateValid constraintAnnotation) {
    }

    @Override
    public boolean isValid(LocalDateTime eventDate, ConstraintValidatorContext constraintValidatorContext) {
        if (eventDate == null) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        return eventDate.isAfter(now.plusHours(2));
    }
}