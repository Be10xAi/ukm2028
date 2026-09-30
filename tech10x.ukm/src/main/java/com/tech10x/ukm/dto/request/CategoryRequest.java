package com.tech10x.ukm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryRequest {

    @NotBlank(message = "name is required")
    @Size(max = 60, message = "name must be at most 60 characters")
    private String name;

    @NotBlank(message = "slug is required")
    @Size(max = 60, message = "slug must be at most 60 characters")
    @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*$", message = "slug must be lowercase letters, digits and hyphens")
    private String slug;

    @Size(max = 2000, message = "description must be at most 2000 characters")
    private String description;
}
