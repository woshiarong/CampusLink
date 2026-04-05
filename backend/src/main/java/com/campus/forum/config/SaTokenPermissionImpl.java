package com.campus.forum.config;

import cn.dev33.satoken.stp.StpInterface;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.forum.entity.Role;
import com.campus.forum.entity.RolePermission;
import com.campus.forum.entity.UserRole;
import com.campus.forum.mapper.RoleMapper;
import com.campus.forum.mapper.RolePermissionMapper;
import com.campus.forum.mapper.UserRoleMapper;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class SaTokenPermissionImpl implements StpInterface {

    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;

    public SaTokenPermissionImpl(UserRoleMapper userRoleMapper,
                                 RoleMapper roleMapper,
                                 RolePermissionMapper rolePermissionMapper) {
        this.userRoleMapper = userRoleMapper;
        this.roleMapper = roleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
    }

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        Long userId = Long.valueOf(String.valueOf(loginId));
        List<UserRole> userRoles = userRoleMapper.selectList(new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, userId));
        if (userRoles.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> roleIds = userRoles.stream().map(UserRole::getRoleId).toList();
        List<RolePermission> rolePermissions = rolePermissionMapper.selectList(new LambdaQueryWrapper<RolePermission>()
                .in(RolePermission::getRoleId, roleIds));

        return rolePermissions.stream()
                .map(RolePermission::getPermissionCode)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Long userId = Long.valueOf(String.valueOf(loginId));
        List<UserRole> userRoles = userRoleMapper.selectList(new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, userId));
        if (userRoles.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> roleIds = userRoles.stream().map(UserRole::getRoleId).toList();
        List<Role> roles = roleMapper.selectBatchIds(roleIds);
        return roles.stream()
                .map(Role::getCode)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }
}
