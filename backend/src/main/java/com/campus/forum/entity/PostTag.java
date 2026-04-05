package com.campus.forum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("forum_post_tag")
public class PostTag {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long postId;
    private String tagName;
}
