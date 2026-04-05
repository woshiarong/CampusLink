package com.campus.forum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("forum_report")
public class Report {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long postId;
    private Long reporterId;
    private String reason;
    private String status;
    private String reviewRemark;
    private Long reviewerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
