package com.campus.forum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_setting")
public class SystemSetting {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String settingKey;
    private String settingValue;
    private String description;
}
