package com.campus.forum.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentVO {
    private Long id;
    private Long postId;
    private Long userId;
    private String userName;
    private String userAvatar;
    private Long parentId;
    private String content;
    private LocalDateTime createdAt;
    private List<CommentVO> replies;
}
