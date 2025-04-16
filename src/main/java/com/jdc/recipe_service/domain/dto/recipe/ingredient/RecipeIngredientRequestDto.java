package com.jdc.recipe_service.domain.dto.recipe.ingredient;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


/**
 *
 *  재료 요청용
 */

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecipeIngredientRequestDto {
    private String name;
    private String quantity;
    //private String unit;
}
