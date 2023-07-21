package ru.practicum.ewm.category.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.service.CategoryService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(path = "/categories")
public class PublicCategoryController {

    private final CategoryService categoryService;

    @GetMapping
    List<CategoryDto> getCategories(@RequestParam(required = false, defaultValue = "0") @PositiveOrZero int from,
                                    @RequestParam(required = false, defaultValue = "10") @Positive int size) {
        log.info("GET /public/categories?from={}&size={}", from, size);
        return categoryService.getCategories(from, size);
    }

    @GetMapping("/{catId}")
    CategoryDto getCategory(@PathVariable long catId) {
        log.info("GET /public/categories/{}", catId);
        return categoryService.getCategory(catId);
    }
}
