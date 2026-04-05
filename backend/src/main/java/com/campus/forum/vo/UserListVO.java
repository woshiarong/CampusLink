package com.campus.forum.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserListVO {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    private List<String> roleCodes;
}
