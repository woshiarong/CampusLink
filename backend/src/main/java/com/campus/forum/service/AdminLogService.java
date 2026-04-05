package com.campus.forum.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.forum.entity.AdminLog;
import com.campus.forum.mapper.AdminLogMapper;
import com.campus.forum.mapper.UserMapper;
import com.campus.forum.vo.AdminLogVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminLogService {

    private final AdminLogMapper adminLogMapper;
    private final UserMapper userMapper;

    public AdminLogService(AdminLogMapper adminLogMapper, UserMapper userMapper) {
        this.adminLogMapper = adminLogMapper;
        this.userMapper = userMapper;
    }

    public void log(Long operatorId, String action, String targetType, Long targetId, String detail) {
        AdminLog log = new AdminLog();
        log.setOperatorId(operatorId);
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setDetail(detail);
        adminLogMapper.insert(log);
    }

    public IPage<AdminLogVO> list(int pageNum, int pageSize) {
        Page<AdminLog> page = new Page<>(pageNum, pageSize);
        IPage<AdminLog> result = adminLogMapper.selectPage(page, null);
        List<AdminLog> list = result.getRecords();
        if (list.isEmpty()) {
            return result.convert(r -> null);
        }
        List<Long> operatorIds = list.stream().map(AdminLog::getOperatorId).distinct().toList();
        Map<Long, String> nameMap = userMapper.selectBatchIds(operatorIds).stream()
                .collect(Collectors.toMap(u -> u.getId(), u -> u.getNickname() != null ? u.getNickname() : u.getUsername()));
        return result.convert(log -> {
            AdminLogVO vo = new AdminLogVO();
            vo.setId(log.getId());
            vo.setOperatorId(log.getOperatorId());
            vo.setOperatorName(nameMap.get(log.getOperatorId()));
            vo.setAction(log.getAction());
            vo.setTargetType(log.getTargetType());
            vo.setTargetId(log.getTargetId());
            vo.setDetail(log.getDetail());
            vo.setCreatedAt(log.getCreatedAt());
            return vo;
        });
    }
}
