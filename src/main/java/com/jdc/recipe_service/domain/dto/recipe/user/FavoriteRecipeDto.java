package com.jdc.recipe_service.domain.dto.recipe.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FavoriteRecipeDto {
    private Long id;
    private String title;
    private String imageUrl;
    private String authorName;
    private LocalDateTime createdAt;

    private long likeCount;
    private boolean likedByCurrentUser;

    public void setLikedByCurrentUser(boolean b) {
        likedByCurrentUser = b;
    }

}
