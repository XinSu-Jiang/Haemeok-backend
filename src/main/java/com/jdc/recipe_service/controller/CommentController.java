package com.jdc.recipe_service.controller;

import com.jdc.recipe_service.domain.dto.comment.CommentDto;
import com.jdc.recipe_service.domain.dto.comment.CommentRequestDto;
import com.jdc.recipe_service.domain.entity.User;
import com.jdc.recipe_service.security.CustomUserDetails;
import com.jdc.recipe_service.service.CommentService;
import org.springframework.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/api/recipes/{recipeId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // 댓글 생성 (상위 댓글)
    @PostMapping
    public ResponseEntity<CommentDto> createComment(@PathVariable Long recipeId,
                                                    @RequestBody CommentRequestDto requestDto,
                                                    Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = ((CustomUserDetails) authentication.getPrincipal()).getUser();
        CommentDto created = commentService.createComment(recipeId, requestDto, user);
        return ResponseEntity.ok(created);
    }

    // 대댓글 생성
    @PostMapping("/{parentId}/replies")
    public ResponseEntity<CommentDto> createReply(@PathVariable Long recipeId,
                                                  @PathVariable Long parentId,
                                                  @RequestBody CommentRequestDto requestDto,
                                                  Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getUser().getId();
        CommentDto created = commentService.createReply(recipeId, parentId, requestDto, userId);
        return ResponseEntity.ok(created);
    }

    // 댓글 조회
    @GetMapping
    public ResponseEntity<List<CommentDto>> getAllCommentsWithLikes(@PathVariable Long recipeId,
                                                                    Authentication authentication) {
        Long userId = (authentication != null && authentication.isAuthenticated())
                ? ((CustomUserDetails) authentication.getPrincipal()).getUser().getId()
                : null;
        return ResponseEntity.ok(commentService.getAllCommentsWithLikes(recipeId, userId));
    }

    //대댓글 조회
    @GetMapping("/{parentId}/replies")
    public List<CommentDto> getReplies(@PathVariable Long parentId,
                                       Authentication authentication) {
        Long userId = (authentication != null && authentication.isAuthenticated())
                ? ((CustomUserDetails) authentication.getPrincipal()).getUser().getId()
                : null;
        return commentService.getRepliesWithLikes(parentId, userId);
    }

    // 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<String> deleteComment(@PathVariable Long commentId,
                                                Authentication authentication) throws AccessDeniedException {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getUser().getId();
        commentService.deleteComment(commentId, userId);
        return ResponseEntity.ok("댓글이 삭제되었습니다.");
    }
}
