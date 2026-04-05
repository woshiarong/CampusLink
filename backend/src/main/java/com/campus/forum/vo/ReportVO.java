package com.campus.forum.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReportVO {
    private Long id;
    private Long postId;
    private String postTitle;
    private String postContentSummary;
    private Long reporterId;
    private String reporterName;
    private String reason;
    private String status;
    private String reviewRemark;
    private Long reviewerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
