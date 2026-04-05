package com.campus.forum.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.forum.entity.Board;
import com.campus.forum.mapper.BoardMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BoardService {

    private final BoardMapper boardMapper;

    public BoardService(BoardMapper boardMapper) {
        this.boardMapper = boardMapper;
    }

    public List<Board> listBoards() {
        return boardMapper.selectList(new LambdaQueryWrapper<Board>().orderByAsc(Board::getId));
    }
}
