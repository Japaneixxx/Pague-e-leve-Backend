package com.japaneixxx.pagueleve.service;

import com.japaneixxx.pagueleve.model.Category;
import com.japaneixxx.pagueleve.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public Category findOrCreateCategory(String name, String lojistaId) {
        return categoryRepository.findByNameAndLojistaId(name, lojistaId)
                .orElseGet(() -> {
                    Category newCategory = new Category();
                    newCategory.setName(name);
                    newCategory.setLojistaId(lojistaId);
                    return categoryRepository.save(newCategory);
                });
    }

    public List<Category> findCategoriesByLojista(String lojistaId) {
        return categoryRepository.findByLojistaId(lojistaId);
    }

    public List<String> findAllDistinctCategoryNames() {
        return categoryRepository.findDistinctNames();
    }
}
