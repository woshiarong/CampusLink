package com.campus.forum.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.forum.dto.SettingUpdateRequest;
import com.campus.forum.entity.SystemSetting;
import com.campus.forum.mapper.SystemSettingMapper;
import com.campus.forum.utils.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class SystemSettingService {
    private static final Set<String> PUBLIC_KEYS = Set.of(
            "forum.allowRegister",
            "forum.emailVerify"
    );

    private final SystemSettingMapper systemSettingMapper;

    public SystemSettingService(SystemSettingMapper systemSettingMapper) {
        this.systemSettingMapper = systemSettingMapper;
    }

    public List<SystemSetting> listSettings() {
        return systemSettingMapper.selectList(new LambdaQueryWrapper<SystemSetting>().orderByAsc(SystemSetting::getId));
    }

    public Map<String, String> getPublicSettings() {
        List<SystemSetting> rows = systemSettingMapper.selectList(new LambdaQueryWrapper<SystemSetting>()
                .in(SystemSetting::getSettingKey, PUBLIC_KEYS));
        Map<String, String> result = new HashMap<>();
        for (SystemSetting row : rows) {
            result.put(row.getSettingKey(), row.getSettingValue());
        }
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public SystemSetting updateSetting(SettingUpdateRequest request) {
        SystemSetting setting = systemSettingMapper.selectOne(new LambdaQueryWrapper<SystemSetting>()
                .eq(SystemSetting::getSettingKey, request.getSettingKey())
                .last("limit 1"));
        if (setting == null) {
            throw new BusinessException("配置项不存在: " + request.getSettingKey());
        }
        setting.setSettingValue(request.getSettingValue());
        systemSettingMapper.updateById(setting);
        return setting;
    }
}
