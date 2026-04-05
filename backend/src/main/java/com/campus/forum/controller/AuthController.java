package com.campus.forum.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import com.campus.forum.dto.LoginRequest;
import com.campus.forum.dto.ProfileUpdateRequest;
import com.campus.forum.dto.RegisterRequest;
import com.campus.forum.vo.UserProfileVO;
import com.campus.forum.service.AuthService;
import com.campus.forum.utils.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Validated
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @SaIgnore
    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        try {
            return ApiResponse.success(authService.login(request));
        } catch (Exception exception) {
            return ApiResponse.fail(exception.getMessage());
        }
    }

    @SaIgnore
    @PostMapping("/register")
    public ApiResponse<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            return ApiResponse.success(authService.register(request));
        } catch (Exception exception) {
            return ApiResponse.fail(exception.getMessage());
        }
    }

    @SaIgnore
    @GetMapping("/verify-email")
    public ApiResponse<Map<String, Object>> verifyEmail(@RequestParam("token") String token) {
        try {
            return ApiResponse.success(authService.verifyEmail(token));
        } catch (Exception exception) {
            return ApiResponse.fail(exception.getMessage());
        }
    }

    @SaCheckPermission("auth:profile")
    @GetMapping("/profile")
    public ApiResponse<Object> profile() {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            return ApiResponse.success(authService.profile(userId));
        } catch (Exception exception) {
            return ApiResponse.fail(exception.getMessage());
        }
    }

    @SaCheckPermission("auth:logout")
    @PostMapping("/logout")
    public ApiResponse<Object> logout() {
        try {
            authService.logout();
            return ApiResponse.success("退出成功", null);
        } catch (Exception exception) {
            return ApiResponse.fail(exception.getMessage());
        }
    }

    @SaCheckPermission("auth:profile")
    @PatchMapping("/profile")
    public ApiResponse<Object> updateProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            UserProfileVO vo = authService.updateProfile(userId, request);
            return ApiResponse.success("更新成功", vo);
        } catch (Exception exception) {
            return ApiResponse.fail(exception.getMessage());
        }
    }
}
