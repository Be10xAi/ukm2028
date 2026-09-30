package com.tech10x.ukm.dto.response;

import com.tech10x.ukm.entity.DocumentType;
import com.tech10x.ukm.entity.PropertyDocument;
import com.tech10x.ukm.entity.VerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Only ever returned to the owning supplier and admins - never on a public endpoint. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyDocumentResponse {
    private String documentId;
    private DocumentType documentType;
    private String fileUrl;
    private VerificationStatus verificationStatus;
    private LocalDateTime uploadedAt;

    public static PropertyDocumentResponse from(PropertyDocument d) {
        return PropertyDocumentResponse.builder().documentId(d.getDocumentId())
                .documentType(d.getDocumentType()).fileUrl(d.getFileUrl())
                .verificationStatus(d.getVerificationStatus()).uploadedAt(d.getUploadedAt()).build();
    }
}
