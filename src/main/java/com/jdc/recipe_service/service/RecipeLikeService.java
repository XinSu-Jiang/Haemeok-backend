package com.jdc.recipe_service.service;

import com.jdc.recipe_service.domain.entity.Recipe;
import com.jdc.recipe_service.domain.entity.RecipeLike;
import com.jdc.recipe_service.domain.entity.User;
import com.jdc.recipe_service.domain.repository.RecipeLikeRepository;
import com.jdc.recipe_service.domain.repository.RecipeRepository;
import com.jdc.recipe_service.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RecipeLikeService {


    private final RecipeLikeRepository likeRepository;
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;

    @Transactional
    public boolean toggleLike(Long userId, Long recipeId) {
        Optional<RecipeLike> like = likeRepository.findByUserIdAndRecipeId(userId, recipeId);

        if (like.isPresent()) {
            likeRepository.delete(like.get());
            return false; // 좋아요 취소
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저가 존재하지 않습니다: " + userId));
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("레시피가 존재하지 않습니다: " + recipeId));

        likeRepository.save(RecipeLike.builder().user(user).recipe(recipe).build());
        return true; // 좋아요 등록
    }

}
