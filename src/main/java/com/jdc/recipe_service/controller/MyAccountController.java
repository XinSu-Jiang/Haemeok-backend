package com.jdc.recipe_service.controller;

import com.jdc.recipe_service.domain.dto.calendar.CookingStreakDto;
import com.jdc.recipe_service.domain.dto.recipe.MyRecipeSummaryDto;
import com.jdc.recipe_service.domain.dto.recipe.RecipeSimpleDto;
import com.jdc.recipe_service.domain.dto.user.UserPatchDTO;
import com.jdc.recipe_service.domain.dto.user.UserResponseDTO;
import com.jdc.recipe_service.exception.CustomException;
import com.jdc.recipe_service.exception.ErrorCode;
import com.jdc.recipe_service.security.CustomUserDetails;
import com.jdc.recipe_service.service.CookingRecordService;
import com.jdc.recipe_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


import static org.springframework.data.domain.Sort.Direction.DESC;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class MyAccountController {

    private final UserService userService;
    private final CookingRecordService cookingService;


    //  내 정보 조회
    @GetMapping
    public ResponseEntity<UserResponseDTO> getMyInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new CustomException(ErrorCode.AUTH_UNAUTHORIZED);
        }
        Long userId = Long.valueOf(userDetails.getUsername());
        return ResponseEntity.ok(userService.getUser(userId));
    }

    //  내 프로필 수정
    @PatchMapping
    public ResponseEntity<UserResponseDTO> patchMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UserPatchDTO dto) throws CustomException {
        if (userDetails == null) {
            throw new CustomException(ErrorCode.AUTH_UNAUTHORIZED);
        }
        Long userId = Long.valueOf(userDetails.getUsername());
        UserResponseDTO updated = userService.updateUser(userId, dto);
        return ResponseEntity.ok(updated);
    }

    //  내 계정 삭제 (하드 삭제)
    @DeleteMapping
    public ResponseEntity<String> deleteMyAccount(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new CustomException(ErrorCode.AUTH_UNAUTHORIZED);
        }
        Long userId = Long.valueOf(userDetails.getUsername());
        userService.deleteUser(userId);
        return ResponseEntity.ok("계정이 삭제되었습니다.");
    }

    @GetMapping("/favorites")
    public ResponseEntity<Page<RecipeSimpleDto>> getMyFavorites(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = DESC) Pageable pageable) {
        if (userDetails == null) {
            throw new CustomException(ErrorCode.AUTH_UNAUTHORIZED);
        }
        Long userId = userDetails.getUser().getId();
        Page<RecipeSimpleDto> page = userService.getFavoriteRecipesByUser(userId, userId, pageable);
        return ResponseEntity.ok(page);
    }


    @GetMapping("/recipes")
    public ResponseEntity<Page<MyRecipeSummaryDto>> getMyRecipes(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        if (userDetails == null) {
            throw new CustomException(ErrorCode.AUTH_UNAUTHORIZED);
        }
        Long myId = userDetails.getUser().getId();
        Page<MyRecipeSummaryDto> page =
                userService.getUserRecipes(myId, myId, pageable);

        return ResponseEntity.ok(page);
    }

    @GetMapping("/streak")
    public ResponseEntity<CookingStreakDto> getMyCookingStreak(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            throw new CustomException(ErrorCode.AUTH_UNAUTHORIZED);
        }
        Long userId = userDetails.getUser().getId();
        CookingStreakDto stats = cookingService.getCookingStreakInfo(userId);
        return ResponseEntity.ok(stats);
    }
}