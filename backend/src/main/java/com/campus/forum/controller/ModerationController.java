package com.campus.forum.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.campus.forum.dto.ModerationActionRequest;
import com.campus.forum.service.ModerationService;
import com.campus.forum.utils.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/moderation")
public class ModerationController {

    private final ModerationService moderationService;

    public ModerationController(ModerationService moderationService) {
        this.moderationService = moderationService;
    }

    @SaCheckPermission("moderation:review")
    @GetMapping("/reports")
    public ApiResponse<Object> listReports(@RequestParam(required = false) String status) {
        try {
            if (status != null && !status.isBlank()) {
                return ApiResponse.success(moderationService.listReportVOs(status));
            }
            return ApiResponse.success(moderationService.listReportVOs(null));
        } catch (Exception exception) {
            return ApiResponse.fail(exception.getMessage());
        }
    }

    @SaCheckPermission("moderation:review")
    @PostMapping("/reports/review")
    public ApiResponse<Object> review(@Valid @RequestBody ModerationActionRequest request) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            return ApiResponse.success(moderationService.review(userId, request));
        } catch (Exception exception) {
            return ApiResponse.fail(exception.getMessage());
        }
    }
}
