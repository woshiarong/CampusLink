package com.campus.forum.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.forum.service.AdminLogService;
import com.campus.forum.utils.ApiResponse;
import com.campus.forum.vo.AdminLogVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/logs")
public class AdminLogController {

    private final AdminLogService adminLogService;

    public AdminLogController(AdminLogService adminLogService) {
        this.adminLogService = adminLogService;
    }

    @SaCheckPermission("adminLog:view")
    @GetMapping
    public ApiResponse<Object> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        try {
            IPage<AdminLogVO> page = adminLogService.list(pageNum, pageSize);
            return ApiResponse.success(page);
        } catch (Exception e) {
            return ApiResponse.fail(e.getMessage());
        }
    }
}
