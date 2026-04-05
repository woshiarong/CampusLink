package com.campus.forum.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SettingUpdateRequest {
    @NotBlank(message = "配置键不能为空")
    private String settingKey;

    @NotBlank(message = "配置值不能为空")
    private String settingValue;
}
