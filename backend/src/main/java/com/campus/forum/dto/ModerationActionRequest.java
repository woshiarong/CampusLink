package com.campus.forum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ModerationActionRequest {
    @NotNull(message = "举报ID不能为空")
    private Long reportId;

    @NotBlank(message = "审核动作不能为空")
    private String action;

    private String reviewRemark;
}
