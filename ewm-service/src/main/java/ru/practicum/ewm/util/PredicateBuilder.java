package ru.practicum.ewm.util;

import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PredicateBuilder {
    private List<Predicate> predicates = new ArrayList<>();

    public static PredicateBuilder builder() {
        return new PredicateBuilder();
    }

    public <T> PredicateBuilder add(T body, Function<T, Predicate> function) {
        if (body != null) {
            predicates.add(function.apply(body));
        }
        return this;
    }

    public <T> PredicateBuilder add(T body, List<Function<T, Predicate>> functions) {
        if (body != null) {
            List<Predicate> orPredicates = functions.stream()
                    .map(function -> function.apply(body))
                    .collect(Collectors.toList());
            Predicate orPredicate = ExpressionUtils.anyOf(orPredicates);
            predicates.add(orPredicate);
        }
        return this;
    }

    public <T> PredicateBuilder add(T body, Predicate predicate) {
        if (body != null) {
            predicates.add(predicate);
        }
        return this;
    }

    public <T> PredicateBuilder addPredicates(T body, List<Predicate> predicateList) {
        if (body != null) {
            Predicate orPredicate = ExpressionUtils.anyOf(predicateList);
            predicates.add(orPredicate);
        }
        return this;
    }

    public Predicate buildAnd() {
        return ExpressionUtils.allOf(predicates);
    }

    public Predicate buildOr() {
        return ExpressionUtils.anyOf(predicates);
    }


}
