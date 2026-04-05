package com.campus.forum.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.forum.dto.UserResetPasswordRequest;
import com.campus.forum.dto.UserUpdateRequest;
import com.campus.forum.entity.Role;
import com.campus.forum.entity.User;
import com.campus.forum.entity.UserRole;
import com.campus.forum.mapper.RoleMapper;
import com.campus.forum.mapper.UserMapper;
import com.campus.forum.mapper.UserRoleMapper;
import com.campus.forum.utils.BusinessException;
import com.campus.forum.utils.PasswordUtil;
import com.campus.forum.vo.UserListVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserManageService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final AdminLogService adminLogService;

    public UserManageService(UserMapper userMapper, RoleMapper roleMapper, UserRoleMapper userRoleMapper,
                             AdminLogService adminLogService) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.adminLogService = adminLogService;
    }

    public IPage<UserListVO> list(int pageNum, int pageSize, String keyword, Integer status) {
        LambdaQueryWrapper<User> q = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            q.and(w -> w.like(User::getUsername, keyword).or().like(User::getNickname, keyword).or().like(User::getEmail, keyword));
        }
        if (status != null) {
            q.eq(User::getStatus, status);
        }
        q.orderByDesc(User::getCreatedAt);
        Page<User> page = new Page<>(pageNum, pageSize);
        IPage<User> result = userMapper.selectPage(page, q);
        List<User> list = result.getRecords();
        if (list.isEmpty()) {
            return result.convert(r -> null);
        }
        List<Long> userIds = list.stream().map(User::getId).toList();
        Map<Long, List<String>> roleMap = userRoleMapper.selectList(
                        new LambdaQueryWrapper<UserRole>().in(UserRole::getUserId, userIds))
                .stream().collect(Collectors.groupingBy(UserRole::getUserId))
                .entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> {
                    List<Long> rids = e.getValue().stream().map(UserRole::getRoleId).toList();
                    return roleMapper.selectBatchIds(rids).stream().map(Role::getCode).toList();
                }));
        return result.convert(u -> {
            UserListVO vo = new UserListVO();
            vo.setId(u.getId());
            vo.setUsername(u.getUsername());
            vo.setNickname(u.getNickname());
            vo.setEmail(u.getEmail());
            vo.setStatus(u.getStatus());
            vo.setCreatedAt(u.getCreatedAt());
            vo.setLastLoginAt(u.getLastLoginAt());
            vo.setRoleCodes(roleMap.getOrDefault(u.getId(), List.of()));
            return vo;
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateUser(UserUpdateRequest request) {
        User user = userMapper.selectById(request.getUserId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        Long operatorId = StpUtil.getLoginIdAsLong();
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
            userMapper.updateById(user);
            adminLogService.log(operatorId, "USER_STATUS", "user", user.getId(), "status=" + request.getStatus());
        }
        if (request.getRoleIds() != null) {
            userRoleMapper.delete(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, user.getId()));
            for (Long roleId : request.getRoleIds()) {
                UserRole ur = new UserRole();
                ur.setUserId(user.getId());
                ur.setRoleId(roleId);
                userRoleMapper.insert(ur);
            }
            adminLogService.log(operatorId, "USER_ASSIGN_ROLE", "user", user.getId(), "roleIds=" + request.getRoleIds());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(UserResetPasswordRequest request) {
        User user = userMapper.selectById(request.getUserId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(PasswordUtil.encode(request.getNewPassword()));
        userMapper.updateById(user);
        adminLogService.log(StpUtil.getLoginIdAsLong(), "USER_RESET_PASSWORD", "user", user.getId(), null);
    }
}
