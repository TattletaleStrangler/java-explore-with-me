package ru.practicum.ewm.event.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(ElementType.FIELD)
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = CheckEventDateValidator.class)
public @interface EventDateValid {
    String message() default "The start date of the event must be no earlier than 2 hours from the current moment.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
