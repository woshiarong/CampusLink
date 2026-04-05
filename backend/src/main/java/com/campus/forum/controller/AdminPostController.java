package com.campus.forum.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.campus.forum.service.PostService;
import com.campus.forum.utils.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/posts")
public class AdminPostController {

    private final PostService postService;

    public AdminPostController(PostService postService) {
        this.postService = postService;
    }

    @SaCheckPermission("post:adminList")
    @GetMapping
    public ApiResponse<Object> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long boardId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sortBy) {
        try {
            return ApiResponse.success(postService.listAdminPosts(pageNum, pageSize, boardId, status, sortBy));
        } catch (Exception e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    @SaCheckPermission("post:pin")
    @PutMapping("/{id}/pin")
    public ApiResponse<Object> setPinned(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        try {
            int pinned = body != null && body.get("pinned") != null ? body.get("pinned") : 1;
            postService.setPinned(id, pinned, StpUtil.getLoginIdAsLong());
            return ApiResponse.success("操作成功", null);
        } catch (Exception e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    @SaCheckPermission("post:feature")
    @PutMapping("/{id}/feature")
    public ApiResponse<Object> setFeatured(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        try {
            int featured = body != null && body.get("featured") != null ? body.get("featured") : 1;
            postService.setFeatured(id, featured, StpUtil.getLoginIdAsLong());
            return ApiResponse.success("操作成功", null);
        } catch (Exception e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    @SaCheckPermission("post:adminDelete")
    @DeleteMapping("/{id}")
    public ApiResponse<Object> delete(@PathVariable Long id) {
        try {
            postService.adminDelete(id, StpUtil.getLoginIdAsLong());
            return ApiResponse.success("删除成功", null);
        } catch (Exception e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    @SaCheckPermission("post:move")
    @PutMapping("/{id}/move")
    public ApiResponse<Object> move(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        try {
            Long boardId = body != null ? body.get("boardId") : null;
            if (boardId == null) {
                return ApiResponse.fail("版块ID不能为空");
            }
            postService.moveBoard(id, boardId, StpUtil.getLoginIdAsLong());
            return ApiResponse.success("移动成功", null);
        } catch (Exception e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    @SaCheckPermission("post:adminList")
    @PutMapping("/{id}/approve")
    public ApiResponse<Object> approve(@PathVariable Long id) {
        try {
            postService.approvePost(id, StpUtil.getLoginIdAsLong());
            return ApiResponse.success("审核通过", null);
        } catch (Exception e) {
            return ApiResponse.fail(e.getMessage());
        }
    }
}
