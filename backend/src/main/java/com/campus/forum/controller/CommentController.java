package com.campus.forum.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import com.campus.forum.dto.CommentCreateRequest;
import com.campus.forum.service.CommentService;
import com.campus.forum.utils.ApiResponse;
import com.campus.forum.vo.CommentVO;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @SaIgnore
    @GetMapping("/post/{postId}")
    public ApiResponse<Object> listByPost(@PathVariable Long postId) {
        try {
            List<CommentVO> list = commentService.listByPostId(postId);
            return ApiResponse.success(list);
        } catch (Exception e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    @SaCheckPermission("post:interact")
    @PostMapping
    public ApiResponse<Object> create(@Valid @RequestBody CommentCreateRequest request) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            CommentVO vo = commentService.create(userId, request);
            return ApiResponse.success("评论成功", vo);
        } catch (Exception e) {
            return ApiResponse.fail(e.getMessage());
        }
    }
}
