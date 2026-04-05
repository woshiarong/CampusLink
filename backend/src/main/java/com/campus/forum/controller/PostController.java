package com.campus.forum.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import com.campus.forum.dto.PostCreateRequest;
import com.campus.forum.dto.PostUpdateRequest;
import com.campus.forum.dto.ReportCreateRequest;
import com.campus.forum.service.FavoriteService;
import com.campus.forum.service.LikeService;
import com.campus.forum.service.PostService;
import com.campus.forum.utils.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Validated
@RestController
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;
    private final LikeService likeService;
    private final FavoriteService favoriteService;

    public PostController(PostService postService, LikeService likeService, FavoriteService favoriteService) {
        this.postService = postService;
        this.likeService = likeService;
        this.favoriteService = favoriteService;
    }

    @SaIgnore
    @GetMapping
    public ApiResponse<Object> listViewable(
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) Long boardId,
            @RequestParam(required = false) String keyword) {
        try {
            Long uid = StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null;
            if (pageNum != null && pageSize != null && pageNum > 0 && pageSize > 0) {
                return ApiResponse.success(postService.listViewablePostsPage(pageNum, pageSize, uid, boardId, keyword));
            }
            return ApiResponse.success(postService.listViewablePosts(uid));
        } catch (Exception exception) {
            return ApiResponse.fail(exception.getMessage());
        }
    }

    @SaIgnore
    @GetMapping("/{postId}")
    public ApiResponse<Object> getDetail(@PathVariable Long postId) {
        try {
            Long uid = StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null;
            boolean isAdmin = StpUtil.isLogin() && StpUtil.hasRole("administrator");
            return ApiResponse.success(postService.getPostDetail(postId, uid, isAdmin));
        } catch (Exception exception) {
            return ApiResponse.fail(exception.getMessage());
        }
    }

    @SaCheckPermission("post:mine")
    @GetMapping("/mine")
    public ApiResponse<Object> mine(@RequestParam(required = false) String status) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            return ApiResponse.success(postService.listMyPosts(userId, status));
        } catch (Exception exception) {
            return ApiResponse.fail(exception.getMessage());
        }
    }

    @SaCheckPermission("post:create")
    @PostMapping
    public ApiResponse<Object> create(@Valid @RequestBody PostCreateRequest request) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            return ApiResponse.success(postService.createDraft(userId, request));
        } catch (Exception exception) {
            return ApiResponse.fail(exception.getMessage());
        }
    }

    @SaCheckPermission("post:submit")
    @PostMapping("/{postId}/submit")
    public ApiResponse<Object> submit(@PathVariable Long postId) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            return ApiResponse.success(postService.submit(userId, postId));
        } catch (Exception exception) {
            return ApiResponse.fail(exception.getMessage());
        }
    }

    @SaCheckPermission("post:interact")
    @PostMapping("/{postId}/interact")
    public ApiResponse<Object> interact(@PathVariable Long postId) {
        try {
            return ApiResponse.success(postService.interact(postId));
        } catch (Exception exception) {
            return ApiResponse.fail(exception.getMessage());
        }
    }

    @SaCheckPermission("post:interact")
    @PostMapping("/{postId}/like")
    public ApiResponse<Object> toggleLike(@PathVariable Long postId) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            boolean liked = likeService.toggle(postId, userId);
            return ApiResponse.success(liked ? "已点赞" : "已取消点赞", Map.of("liked", liked));
        } catch (Exception exception) {
            return ApiResponse.fail(exception.getMessage());
        }
    }

    @SaCheckPermission("post:view")
    @PostMapping("/{postId}/favorite")
    public ApiResponse<Object> toggleFavorite(@PathVariable Long postId) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            boolean favorited = favoriteService.toggle(postId, userId);
            return ApiResponse.success(favorited ? "已收藏" : "已取消收藏", Map.of("favorited", favorited));
        } catch (Exception exception) {
            return ApiResponse.fail(exception.getMessage());
        }
    }

    @SaCheckPermission("post:update")
    @PutMapping("/{postId}")
    public ApiResponse<Object> update(@PathVariable Long postId, @Valid @RequestBody PostUpdateRequest request) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            return ApiResponse.success(postService.update(userId, postId, request));
        } catch (Exception exception) {
            return ApiResponse.fail(exception.getMessage());
        }
    }

    @SaCheckPermission("post:mine")
    @DeleteMapping("/{postId}")
    public ApiResponse<Object> deleteMine(@PathVariable Long postId) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            postService.deleteByOwner(userId, postId);
            return ApiResponse.success("删除成功", null);
        } catch (Exception exception) {
            return ApiResponse.fail(exception.getMessage());
        }
    }

    @SaCheckPermission("report:create")
    @PostMapping("/{postId}/report")
    public ApiResponse<Object> report(@PathVariable Long postId, @Valid @RequestBody ReportCreateRequest request) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            postService.report(userId, postId, request.getReason());
            return ApiResponse.success("举报成功", null);
        } catch (Exception exception) {
            return ApiResponse.fail(exception.getMessage());
        }
    }
}
