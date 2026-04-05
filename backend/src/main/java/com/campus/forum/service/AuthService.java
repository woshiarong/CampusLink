package com.campus.forum.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.forum.dto.LoginRequest;
import com.campus.forum.dto.ProfileUpdateRequest;
import com.campus.forum.dto.RegisterRequest;
import com.campus.forum.entity.Role;
import com.campus.forum.entity.User;
import com.campus.forum.entity.UserRole;
import com.campus.forum.mapper.RoleMapper;
import com.campus.forum.mapper.SystemSettingMapper;
import com.campus.forum.mapper.UserMapper;
import com.campus.forum.mapper.UserRoleMapper;
import com.campus.forum.utils.BusinessException;
import com.campus.forum.utils.JwtUtil;
import com.campus.forum.utils.PasswordUtil;
import com.campus.forum.vo.UserProfileVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {
    private static final ConcurrentHashMap<String, EmailVerifyTokenInfo> EMAIL_VERIFY_TOKENS = new ConcurrentHashMap<>();

    private static class EmailVerifyTokenInfo {
        private final Long userId;
        private final LocalDateTime expireAt;

        private EmailVerifyTokenInfo(Long userId, LocalDateTime expireAt) {
            this.userId = userId;
            this.expireAt = expireAt;
        }
    }

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final SystemSettingMapper systemSettingMapper;

    public AuthService(UserMapper userMapper, RoleMapper roleMapper, UserRoleMapper userRoleMapper,
                       SystemSettingMapper systemSettingMapper) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.systemSettingMapper = systemSettingMapper;
    }

    public Map<String, Object> login(LoginRequest request) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername())
                .last("limit 1"));
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException("用户已禁用");
        }
        if (!PasswordUtil.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        user.setLastLoginAt(java.time.LocalDateTime.now());
        userMapper.updateById(user);

        StpUtil.login(user.getId());
        String token = JwtUtil.createToken(user.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", buildProfile(user.getId()));
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> register(RegisterRequest request) {
        if (!getBoolSetting("forum.allowRegister", true)) {
            throw new BusinessException("当前系统已关闭注册");
        }
        boolean emailVerify = getBoolSetting("forum.emailVerify", false);

        String email = request.getEmail() == null ? null : request.getEmail().trim();
        if (emailVerify) {
            if (email == null || email.isBlank()) {
                throw new BusinessException("当前系统已开启邮箱验证，请填写有效邮箱");
            }
            boolean ok = email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
            if (!ok) {
                throw new BusinessException("当前系统已开启邮箱验证，请填写有效邮箱");
            }
        } else {
            // 关闭邮箱验证时，空邮箱不参与任何校验
            email = (email == null || email.isBlank()) ? null : email;
        }

        User exists = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername())
                .last("limit 1"));
        if (exists != null) {
            throw new BusinessException("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(PasswordUtil.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setEmail(email);
        // 邮箱“注册”模式：只需要填写邮箱，不需要验证激活
        user.setStatus(1);
        userMapper.insert(user);

        Role studentRole = roleMapper.selectOne(new LambdaQueryWrapper<Role>()
                .eq(Role::getCode, "student")
                .last("limit 1"));
        if (studentRole == null) {
            throw new BusinessException("默认角色不存在");
        }

        UserRole userRole = new UserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(studentRole.getId());
        userRoleMapper.insert(userRole);

        Map<String, Object> result = new HashMap<>();
        StpUtil.login(user.getId());
        String token = JwtUtil.createToken(user.getId());
        result.put("verifyRequired", false);
        result.put("token", token);
        result.put("user", buildProfile(user.getId()));
        return result;
    }

    public Map<String, Object> verifyEmail(String verifyToken) {
        if (verifyToken == null || verifyToken.isBlank()) {
            throw new BusinessException("邮箱验证链接无效");
        }

        EmailVerifyTokenInfo info = EMAIL_VERIFY_TOKENS.get(verifyToken);
        if (info == null) {
            throw new BusinessException("邮箱验证链接无效");
        }
        if (info.expireAt.isBefore(LocalDateTime.now())) {
            EMAIL_VERIFY_TOKENS.remove(verifyToken);
            throw new BusinessException("邮箱验证链接已过期");
        }

        User user = userMapper.selectById(info.userId);
        if (user == null) {
            EMAIL_VERIFY_TOKENS.remove(verifyToken);
            throw new BusinessException("用户不存在");
        }

        user.setStatus(1);
        userMapper.updateById(user);
        EMAIL_VERIFY_TOKENS.remove(verifyToken);

        StpUtil.login(user.getId());
        String token = JwtUtil.createToken(user.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("verifyRequired", false);
        result.put("token", token);
        result.put("user", buildProfile(user.getId()));
        return result;
    }

    private boolean getBoolSetting(String key, boolean defaultValue) {
        var row = systemSettingMapper.selectOne(new LambdaQueryWrapper<com.campus.forum.entity.SystemSetting>()
                .eq(com.campus.forum.entity.SystemSetting::getSettingKey, key)
                .last("limit 1"));
        if (row == null || row.getSettingValue() == null) {
            return defaultValue;
        }
        String v = row.getSettingValue().trim();
        if (v.isEmpty()) {
            return defaultValue;
        }
        return "true".equalsIgnoreCase(v)
                || "1".equals(v)
                || "yes".equalsIgnoreCase(v)
                || "on".equalsIgnoreCase(v)
                || "开启".equals(v);
    }

    public UserProfileVO profile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return buildProfile(userId);
    }

    public void logout() {
        StpUtil.logout();
    }

    @Transactional(rollbackFor = Exception.class)
    public UserProfileVO updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        if (request.getNickname() != null && !request.getNickname().isBlank()) user.setNickname(request.getNickname().trim());
        if (request.getAvatar() != null) user.setAvatar(request.getAvatar());
        if (request.getBio() != null) user.setBio(request.getBio());
        userMapper.updateById(user);
        return buildProfile(userId);
    }

    private UserProfileVO buildProfile(Long userId) {
        User user = userMapper.selectById(userId);
        UserProfileVO vo = new UserProfileVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setBio(user.getBio());
        vo.setCredit(user.getCredit() != null ? user.getCredit() : 0);
        List<String> roleList = StpUtil.getRoleList(user.getId());
        List<String> permissionList = StpUtil.getPermissionList(user.getId());
        vo.setRoles(roleList);
        vo.setPermissions(permissionList);
        return vo;
    }
}
