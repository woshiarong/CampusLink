package com.campus.forum.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AttachmentDto {
    @NotBlank(message = "附件名称不能为空")
    private String fileName;

    @NotBlank(message = "附件地址不能为空")
    private String fileUrl;
}
