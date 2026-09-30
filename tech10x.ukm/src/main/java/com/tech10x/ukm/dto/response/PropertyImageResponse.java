package com.tech10x.ukm.dto.response;

import com.tech10x.ukm.entity.PropertyImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyImageResponse {
    private String imageId;
    private String url;
    private boolean cover;
    private LocalDateTime uploadedAt;

    public static PropertyImageResponse from(PropertyImage i) {
        return PropertyImageResponse.builder().imageId(i.getImageId()).url(i.getUrl())
                .cover(i.isCover()).uploadedAt(i.getUploadedAt()).build();
    }
}
