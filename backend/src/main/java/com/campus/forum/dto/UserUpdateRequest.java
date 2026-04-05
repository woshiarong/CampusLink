package com.campus.forum.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class UserUpdateRequest {
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    private Integer status;
    private List<Long> roleIds;
}
