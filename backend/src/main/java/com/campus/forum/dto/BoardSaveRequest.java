package com.campus.forum.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BoardSaveRequest {
    private Long id;
    @NotBlank(message = "版块名称不能为空")
    private String name;
    private String description;
    private Integer sortOrder;
    private Long moderatorId;
    private Integer needApproval;
    private Integer allowAnonymous;
}
