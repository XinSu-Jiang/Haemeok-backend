package com.jdc.recipe_service.domain.dto.url;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileInfoRequest {
    private String contentType;
    private String type;
    private Integer stepIndex;
}