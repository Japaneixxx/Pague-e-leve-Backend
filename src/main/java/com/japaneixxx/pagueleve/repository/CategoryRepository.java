package com.japaneixxx.pagueleve.repository;

import com.japaneixxx.pagueleve.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByNameAndLojistaId(String name, String lojistaId);

    List<Category> findByLojistaId(String lojistaId);

    @Query("SELECT DISTINCT c.name FROM Category c ORDER BY c.name")
    List<String> findDistinctNames();
}
