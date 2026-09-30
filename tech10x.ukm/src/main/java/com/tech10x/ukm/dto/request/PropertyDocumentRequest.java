package com.tech10x.ukm.dto.request;

import com.tech10x.ukm.entity.DocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PropertyDocumentRequest {

    @NotNull(message = "documentType is required")
    private DocumentType documentType;

    @NotBlank(message = "fileUrl is required")
    @Size(max = 255, message = "fileUrl must be at most 255 characters")
    private String fileUrl;
}
