package com.campus.forum.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.forum.dto.BoardSaveRequest;
import com.campus.forum.entity.Board;
import com.campus.forum.mapper.BoardMapper;
import com.campus.forum.utils.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BoardManageService {

    private final BoardMapper boardMapper;
    private final AdminLogService adminLogService;

    public BoardManageService(BoardMapper boardMapper, AdminLogService adminLogService) {
        this.boardMapper = boardMapper;
        this.adminLogService = adminLogService;
    }

    public List<Board> list() {
        return boardMapper.selectList(new LambdaQueryWrapper<Board>().orderByAsc(Board::getSortOrder).orderByAsc(Board::getId));
    }

    @Transactional(rollbackFor = Exception.class)
    public Board save(BoardSaveRequest request) {
        Long operatorId = StpUtil.getLoginIdAsLong();
        Board board;
        if (request.getId() != null && request.getId() > 0) {
            board = boardMapper.selectById(request.getId());
            if (board == null) {
                throw new BusinessException("版块不存在");
            }
            board.setName(request.getName());
            board.setDescription(request.getDescription());
            if (request.getSortOrder() != null) board.setSortOrder(request.getSortOrder());
            if (request.getModeratorId() != null) board.setModeratorId(request.getModeratorId());
            if (request.getNeedApproval() != null) board.setNeedApproval(request.getNeedApproval());
            if (request.getAllowAnonymous() != null) board.setAllowAnonymous(request.getAllowAnonymous());
            boardMapper.updateById(board);
            adminLogService.log(operatorId, "BOARD_UPDATE", "board", board.getId(), request.getName());
        } else {
            board = new Board();
            board.setName(request.getName());
            board.setDescription(request.getDescription());
            board.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
            board.setModeratorId(request.getModeratorId());
            board.setNeedApproval(request.getNeedApproval() != null ? request.getNeedApproval() : 0);
            board.setAllowAnonymous(request.getAllowAnonymous() != null ? request.getAllowAnonymous() : 0);
            boardMapper.insert(board);
            adminLogService.log(operatorId, "BOARD_CREATE", "board", board.getId(), request.getName());
        }
        return board;
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long boardId) {
        Board board = boardMapper.selectById(boardId);
        if (board == null) {
            throw new BusinessException("版块不存在");
        }
        boardMapper.deleteById(boardId);
        adminLogService.log(StpUtil.getLoginIdAsLong(), "BOARD_DELETE", "board", boardId, board.getName());
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateOrder(List<Long> boardIdsInOrder) {
        for (int i = 0; i < boardIdsInOrder.size(); i++) {
            Board b = boardMapper.selectById(boardIdsInOrder.get(i));
            if (b != null) {
                b.setSortOrder(i);
                boardMapper.updateById(b);
            }
        }
        adminLogService.log(StpUtil.getLoginIdAsLong(), "BOARD_ORDER", null, null, String.valueOf(boardIdsInOrder));
    }
}
