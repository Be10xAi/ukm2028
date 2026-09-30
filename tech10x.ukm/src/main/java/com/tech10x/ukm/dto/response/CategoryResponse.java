package com.tech10x.ukm.dto.response;

import com.tech10x.ukm.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private Integer id;
    private String name;
    private String slug;
    private String description;

    public static CategoryResponse from(Category c) {
        return CategoryResponse.builder().id(c.getId()).name(c.getName())
                .slug(c.getSlug()).description(c.getDescription()).build();
    }
}
