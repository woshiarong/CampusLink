package com.campus.forum.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.campus.forum.entity.Role;
import com.campus.forum.mapper.RoleMapper;
import com.campus.forum.utils.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/roles")
public class AdminRoleController {

    private final RoleMapper roleMapper;

    public AdminRoleController(RoleMapper roleMapper) {
        this.roleMapper = roleMapper;
    }

    @SaCheckPermission("user:list")
    @GetMapping
    public ApiResponse<Object> list() {
        try {
            List<Role> roles = roleMapper.selectList(null);
            return ApiResponse.success(roles);
        } catch (Exception e) {
            return ApiResponse.fail(e.getMessage());
        }
    }
}
