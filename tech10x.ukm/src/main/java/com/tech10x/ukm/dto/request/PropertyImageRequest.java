package com.tech10x.ukm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PropertyImageRequest {

    /** https URL of the uploaded blob; validated against the storage allow-list by SafeUrlPolicy. */
    @NotBlank(message = "url is required")
    @Size(max = 255, message = "url must be at most 255 characters")
    private String url;

    private Boolean cover;
}
