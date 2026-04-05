package com.campus.forum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 32, message = "用户名长度需在3-32之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 64, message = "密码长度需在6-64之间")
    private String password;

    @NotBlank(message = "昵称不能为空")
    @Size(min = 2, max = 32, message = "昵称长度需在2-32之间")
    private String nickname;

    @Size(max = 128, message = "邮箱长度不能超过128")
    private String email;
}
