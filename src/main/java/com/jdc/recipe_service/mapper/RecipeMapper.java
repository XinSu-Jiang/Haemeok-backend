package com.jdc.recipe_service.mapper;

import com.jdc.recipe_service.domain.dto.recipe.RecipeCreateRequestDto;
import com.jdc.recipe_service.domain.entity.Recipe;
import com.jdc.recipe_service.domain.entity.User;

public class RecipeMapper {

    public static Recipe toEntity(RecipeCreateRequestDto dto, User user) {
        return Recipe.builder()
                .user(user)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .dishType(dto.getDishType())
                .cookingTime(dto.getCookingTime())
                .imageUrl(dto.getImageUrl())
                .youtubeUrl(dto.getYoutubeUrl())
                .cookingTools(String.join(", ", dto.getCookingTools())) // List → String 변환
                .servings(dto.getServings())
                .marketPrice(dto.getMarketPrice())
                .totalIngredientCost(dto.getTotalIngredientCost())
                .build();
    }

}
