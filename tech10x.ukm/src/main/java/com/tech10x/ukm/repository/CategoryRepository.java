package com.tech10x.ukm.repository;

import com.tech10x.ukm.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Integer id);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Integer id);
}
