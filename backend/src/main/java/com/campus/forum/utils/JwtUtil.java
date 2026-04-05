package com.campus.forum.utils;

import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import cn.dev33.satoken.stp.StpUtil;

import java.util.HashMap;
import java.util.Map;

public class JwtUtil {

    private JwtUtil() {
    }

    public static String createToken(Object loginId) {
        return StpUtil.getTokenValueByLoginId(loginId);
    }

    public static Map<String, Object> parseToken(String token) {
        Map<String, Object> result = new HashMap<>();
        if (token == null || token.isBlank()) {
            return result;
        }
        StpLogicJwtForSimple logic = new StpLogicJwtForSimple();
        Object loginId = logic.getLoginIdByToken(token);
        result.put("loginId", loginId);
        return result;
    }
}
