package com.campus.forum.dto;

import lombok.Data;

@Data
public class ProfileUpdateRequest {
    private String nickname;
    private String avatar;
    private String bio;
}
