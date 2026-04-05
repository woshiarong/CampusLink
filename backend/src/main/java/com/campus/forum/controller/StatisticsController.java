package com.campus.forum.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.campus.forum.service.StatisticsService;
import com.campus.forum.utils.ApiResponse;
import com.campus.forum.vo.StatisticsVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @SaCheckPermission("statistics:view")
    @GetMapping("/dashboard")
    public ApiResponse<Object> dashboard() {
        try {
            StatisticsVO vo = statisticsService.dashboard();
            return ApiResponse.success(vo);
        } catch (Exception e) {
            return ApiResponse.fail(e.getMessage());
        }
    }
}
