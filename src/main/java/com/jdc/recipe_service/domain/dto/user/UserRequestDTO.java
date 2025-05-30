package com.jdc.recipe_service.domain.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRequestDTO {

    @Size(max = 50)
    @NotBlank(message = "닉네임은 빈칸일 수 없습니다.")
    private String nickname;

    @Size(max = 255)
    private String profileImage;

    @Size(max = 255)
    private String introduction;

}
