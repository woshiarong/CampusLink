package com.campus.forum.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminLogVO {
    private Long id;
    private Long operatorId;
    private String operatorName;
    private String action;
    private String targetType;
    private Long targetId;
    private String detail;
    private LocalDateTime createdAt;
}
