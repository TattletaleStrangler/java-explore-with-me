package ru.practicum.ewm.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.CategoryMapper;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.storage.CategoryStorage;
import ru.practicum.ewm.event.storage.EventStorage;
import ru.practicum.ewm.exception.CategoryIsNotEmptyException;
import ru.practicum.ewm.exception.CategoryNotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryStorage categoryStorage;
    private final EventStorage eventStorage;

    @Transactional
    public CategoryDto createCategory(NewCategoryDto categoryDto) {
        Category category = CategoryMapper.dtoToCategory(categoryDto);
        Category savedCategory = categoryStorage.save(category);
        return CategoryMapper.categoryToDto(savedCategory);
    }

    @Transactional
    public CategoryDto updateCategory(NewCategoryDto categoryDto, Long categoryId) {
        Category oldCategory = checkCategoryAndGet(categoryId);
        updateCategory(oldCategory, categoryDto);
        Category newCategory = categoryStorage.save(oldCategory);
        return CategoryMapper.categoryToDto(newCategory);
    }

    @Transactional
    public void deleteCategory(Long categoryId) {
        checkCategoryAndGet(categoryId);
        checkEmptyCategory(categoryId);
        categoryStorage.deleteById(categoryId);
    }

    public List<CategoryDto> getCategories(int from, int size) {
        PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size, Sort.by("id"));
        List<Category> categories = categoryStorage.findAll(page).toList();
        return CategoryMapper.categoryListToDto(categories);
    }

    public CategoryDto getCategory(long categoryId) {
        Category category = checkCategoryAndGet(categoryId);
        return CategoryMapper.categoryToDto(category);
    }

    private Category checkCategoryAndGet(long categoryId) {
        return categoryStorage.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Category with id=" + categoryId + " was not found"));
    }

    private void updateCategory(Category oldCategory, NewCategoryDto dto) {
        String name = dto.getName();
        if (name != null && !oldCategory.getName().equals(name)) {
            oldCategory.setName(name);
        }
    }

    private void checkEmptyCategory(Long categoryId) {
        if (eventStorage.existsEventByCategoryId(categoryId)) {
            throw new CategoryIsNotEmptyException("The category is not empty");
        }
    }
}
