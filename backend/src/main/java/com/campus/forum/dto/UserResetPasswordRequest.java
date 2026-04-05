package com.campus.forum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserResetPasswordRequest {
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
