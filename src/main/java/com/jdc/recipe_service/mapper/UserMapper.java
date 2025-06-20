package com.jdc.recipe_service.mapper;

import com.jdc.recipe_service.domain.dto.user.*;
import com.jdc.recipe_service.domain.entity.User;

public class UserMapper {

    public static UserDto toSimpleDto(User user) {
        if (user == null) return null;
        return UserDto.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .profileImage(user.getProfileImage())
                .introduction(user.getIntroduction())
                .build();
    }

    public static CommentUserDto toCommentUserDto(User user) {
        if (user == null) return null;
        return CommentUserDto.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .profileImage(user.getProfileImage())
                .build();
    }

    // 프로필 수정용: nickname/profileImage/introduction만
    public static User toEntity(UserRequestDTO dto) {
        if (dto == null) return null;
        return User.builder()
                .nickname(dto.getNickname())
                .profileImage(dto.getProfileImage())
                .introduction(dto.getIntroduction())
                .build();
    }


    // 엔티티 → 응답용 전체 DTO
    public static UserResponseDTO toDto(User user) {
        if (user == null) return null;
        return UserResponseDTO.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .profileImage(user.getProfileImage())
                .introduction(user.getIntroduction())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .provider(user.getProvider())
                .surveyCompleted(user.isSurveyCompleted())
                .build();
    }

}
