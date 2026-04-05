package com.campus.forum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("forum_post_attachment")
public class PostAttachment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long postId;
    private String fileName;
    private String fileUrl;
}
