package com.campus.forum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("forum_moderation_log")
public class ModerationLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long reportId;
    private String action;
    private Long operatorId;
    private String detail;
    private LocalDateTime createdAt;
}
