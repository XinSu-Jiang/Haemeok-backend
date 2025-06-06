package com.jdc.recipe_service.domain.dto.recipe;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeRatingRequestDto {
    private double rating;

    private String comment;
}