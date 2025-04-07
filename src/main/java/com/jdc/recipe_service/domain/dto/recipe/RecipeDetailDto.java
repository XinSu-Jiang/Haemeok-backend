package com.jdc.recipe_service.domain.dto.recipe;

import com.jdc.recipe_service.domain.dto.comment.CommentDto;
import com.jdc.recipe_service.domain.dto.recipe.ingredient.RecipeIngredientDto;
import com.jdc.recipe_service.domain.dto.recipe.step.RecipeStepDto;
import com.jdc.recipe_service.domain.dto.user.UserDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 *
 *  레시피 상세 조회용
 */

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecipeDetailDto {
    private Long id;
    private String title;
    private String dishType;
    private String description;
    private int cookingTime;
    private String imageUrl;
    private String youtubeUrl;
    private List<String> cookingTools;
    private boolean isAiGenerated;

    private Integer servings;

    private UserDto author;
    private BigDecimal avgRating;

    private List<String> tags;
    private List<RecipeIngredientDto> ingredients;
    private List<RecipeStepDto> steps;

    private int likeCount;
    private boolean likedByCurrentUser;
    private boolean favoriteByCurrentUser;

    private List<CommentDto> comments;
    private long commentCount;

    //추가
//    private Boolean isPrivate;
    private Integer totalIngredientCost;
    private Integer marketPrice;
    private Integer savings; // averagePrice - measuredPrice 계산값

}
