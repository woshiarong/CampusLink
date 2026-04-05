package com.campus.forum.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.campus.forum.service.MessageService;
import com.campus.forum.utils.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping
    public ApiResponse<Object> list() {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            return ApiResponse.success(messageService.listByUser(userId));
        } catch (Exception e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    @GetMapping("/unread-count")
    public ApiResponse<Object> unreadCount() {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            return ApiResponse.success(Map.of("count", messageService.countUnread(userId)));
        } catch (Exception e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    @PutMapping("/{id}/read")
    public ApiResponse<Object> read(@PathVariable Long id) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            messageService.markRead(userId, id);
            return ApiResponse.success("ok", null);
        } catch (Exception e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    @PutMapping("/read-all")
    public ApiResponse<Object> readAll() {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            messageService.markAllRead(userId);
            return ApiResponse.success("ok", null);
        } catch (Exception e) {
            return ApiResponse.fail(e.getMessage());
        }
    }
}

