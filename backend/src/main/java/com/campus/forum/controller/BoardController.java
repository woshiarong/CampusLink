package com.campus.forum.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.campus.forum.service.BoardService;
import com.campus.forum.utils.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/boards")
public class BoardController {

    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @SaIgnore
    @GetMapping
    public ApiResponse<Object> listBoards() {
        try {
            return ApiResponse.success(boardService.listBoards());
        } catch (Exception exception) {
            return ApiResponse.fail(exception.getMessage());
        }
    }
}
