package com.campus.forum.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaIgnore;
import com.campus.forum.dto.SettingUpdateRequest;
import com.campus.forum.service.SystemSettingService;
import com.campus.forum.utils.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/settings")
public class SystemSettingController {

    private final SystemSettingService systemSettingService;

    public SystemSettingController(SystemSettingService systemSettingService) {
        this.systemSettingService = systemSettingService;
    }

    @SaCheckPermission("settings:view")
    @GetMapping
    public ApiResponse<Object> listSettings() {
        try {
            return ApiResponse.success(systemSettingService.listSettings());
        } catch (Exception exception) {
            return ApiResponse.fail(exception.getMessage());
        }
    }

    @SaIgnore
    @GetMapping("/public")
    public ApiResponse<Object> publicSettings() {
        try {
            return ApiResponse.success(systemSettingService.getPublicSettings());
        } catch (Exception exception) {
            return ApiResponse.fail(exception.getMessage());
        }
    }

    @SaCheckPermission("settings:update")
    @PutMapping
    public ApiResponse<Object> update(@Valid @RequestBody SettingUpdateRequest request) {
        try {
            return ApiResponse.success(systemSettingService.updateSetting(request));
        } catch (Exception exception) {
            return ApiResponse.fail(exception.getMessage());
        }
    }
}
