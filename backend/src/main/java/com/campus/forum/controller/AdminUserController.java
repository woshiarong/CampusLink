package com.campus.forum.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.campus.forum.dto.UserResetPasswordRequest;
import com.campus.forum.dto.UserUpdateRequest;
import com.campus.forum.service.UserManageService;
import com.campus.forum.utils.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/admin/users")
public class AdminUserController {

    private final UserManageService userManageService;

    public AdminUserController(UserManageService userManageService) {
        this.userManageService = userManageService;
    }

    @SaCheckPermission("user:list")
    @GetMapping
    public ApiResponse<Object> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        try {
            return ApiResponse.success(userManageService.list(pageNum, pageSize, keyword, status));
        } catch (Exception e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    @SaCheckPermission("user:update")
    @PutMapping
    public ApiResponse<Object> update(@Valid @RequestBody UserUpdateRequest request) {
        try {
            userManageService.updateUser(request);
            return ApiResponse.success("更新成功", null);
        } catch (Exception e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    @SaCheckPermission("user:resetPassword")
    @PostMapping("/reset-password")
    public ApiResponse<Object> resetPassword(@Valid @RequestBody UserResetPasswordRequest request) {
        try {
            userManageService.resetPassword(request);
            return ApiResponse.success("密码已重置", null);
        } catch (Exception e) {
            return ApiResponse.fail(e.getMessage());
        }
    }
}
