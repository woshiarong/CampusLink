package com.campus.forum.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class PostVO {
    private Long id;
    private Long userId;
    private String authorName;
    private String authorAvatar;
    private Long boardId;
    private String boardName;
    private String title;
    private String content;
    private String status;
    private Integer isPinned;
    private Integer isFeatured;
    private Long likeCount;
    private Long commentCount;
    private Boolean likedByMe;
    private Boolean favoritedByMe;
    private List<String> tags = new ArrayList<>();
    private List<AttachmentVO> attachments = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    public static class AttachmentVO {
        private String fileName;
        private String fileUrl;
    }
}
