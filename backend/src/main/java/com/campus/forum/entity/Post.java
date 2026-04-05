package com.campus.forum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("forum_post")
public class Post {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long boardId;
    private String title;
    private String content;
    private String status;
    private Integer isPinned;
    private Integer isFeatured;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
