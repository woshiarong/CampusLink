package com.campus.forum.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PostCreateRequest {
    @NotNull(message = "版块不能为空")
    private Long boardId;

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "正文不能为空")
    private String content;

    private List<String> tags = new ArrayList<>();

    @Valid
    private List<AttachmentDto> attachments = new ArrayList<>();
}
