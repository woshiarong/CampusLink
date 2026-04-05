package com.campus.forum.vo;

import lombok.Data;

import java.util.List;

@Data
public class UserProfileVO {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String bio;
    private Integer credit;
    private List<String> roles;
    private List<String> permissions;
}
