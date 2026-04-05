package com.campus.forum.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.campus.forum.service.FavoriteService;
import com.campus.forum.service.PostService;
import com.campus.forum.utils.ApiResponse;
import com.campus.forum.vo.PostVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final PostService postService;

    public FavoriteController(FavoriteService favoriteService, PostService postService) {
        this.favoriteService = favoriteService;
        this.postService = postService;
    }

    @SaCheckPermission("post:view")
    @GetMapping
    public ApiResponse<Object> list() {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            List<Long> postIds = favoriteService.listPostIdsByUser(userId);
            List<PostVO> list = postService.getPostsByIds(postIds, userId);
            return ApiResponse.success(list);
        } catch (Exception e) {
            return ApiResponse.fail(e.getMessage());
        }
    }
}
