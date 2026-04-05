package com.campus.forum.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReportCreateRequest {
    @NotBlank(message = "举报原因不能为空")
    private String reason;
}
