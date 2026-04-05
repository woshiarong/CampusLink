package com.campus.forum.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handler -> {
                    HttpServletRequest req = getRequest();
                    if (req != null && "GET".equalsIgnoreCase(req.getMethod())) {
                        String path = req.getRequestURI();
                        if (path != null && (path.startsWith("/posts/") || path.startsWith("/comments/post/"))) {
                            return;
                        }
                    }
                    SaRouter.match("/**")
                            .notMatch("/auth/login", "/auth/register", "/posts", "/boards")
                            .check(r -> StpUtil.checkLogin());
                }))
                .addPathPatterns("/**");
    }

    private static HttpServletRequest getRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }
}
