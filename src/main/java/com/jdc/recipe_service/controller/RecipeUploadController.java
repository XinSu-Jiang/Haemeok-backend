package com.jdc.recipe_service.controller;

import com.jdc.recipe_service.domain.dto.url.PresignedUrlRequest;
import com.jdc.recipe_service.domain.dto.url.PresignedUrlResponse;
import com.jdc.recipe_service.security.CustomUserDetails;
import com.jdc.recipe_service.service.RecipeUploadService;
import com.jdc.recipe_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class RecipeUploadController {

    private final RecipeUploadService recipeUploadService;
    private final UserService userService;

    @PostMapping("/presigned-urls")
    public ResponseEntity<PresignedUrlResponse> getPresignedUrls(
            @RequestBody PresignedUrlRequest request,
            Authentication authentication
    ) {
//        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getUser().getId();
        Long userId = (authentication != null && authentication.isAuthenticated())
                ? ((CustomUserDetails) authentication.getPrincipal()).getUser().getId()
                : userService.getGuestUser().getId(); // ✅ 비회원 fallback
        return ResponseEntity.ok(recipeUploadService.generatePresignedUrls(request, userId));
    }
}
