package com.campus.forum.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.campus.forum.dto.BoardSaveRequest;
import com.campus.forum.entity.Board;
import com.campus.forum.service.BoardManageService;
import com.campus.forum.utils.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/admin/boards")
public class AdminBoardController {

    private final BoardManageService boardManageService;

    public AdminBoardController(BoardManageService boardManageService) {
        this.boardManageService = boardManageService;
    }

    @SaCheckPermission("board:list")
    @GetMapping
    public ApiResponse<Object> list() {
        try {
            return ApiResponse.success(boardManageService.list());
        } catch (Exception e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    @SaCheckPermission("board:save")
    @PostMapping
    public ApiResponse<Object> save(@Valid @RequestBody BoardSaveRequest request) {
        try {
            Board board = boardManageService.save(request);
            return ApiResponse.success("保存成功", board);
        } catch (Exception e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    @SaCheckPermission("board:delete")
    @DeleteMapping("/{id}")
    public ApiResponse<Object> delete(@PathVariable Long id) {
        try {
            boardManageService.delete(id);
            return ApiResponse.success("删除成功", null);
        } catch (Exception e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    @SaCheckPermission("board:order")
    @PutMapping("/order")
    public ApiResponse<Object> updateOrder(@RequestBody List<Long> boardIds) {
        try {
            boardManageService.updateOrder(boardIds);
            return ApiResponse.success("排序已更新", null);
        } catch (Exception e) {
            return ApiResponse.fail(e.getMessage());
        }
    }
}
