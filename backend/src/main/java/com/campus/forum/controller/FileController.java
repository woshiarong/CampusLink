package com.campus.forum.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.forum.entity.SystemSetting;
import com.campus.forum.mapper.SystemSettingMapper;
import com.campus.forum.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/files")
public class FileController {

    private final SystemSettingMapper systemSettingMapper;

    public FileController(SystemSettingMapper systemSettingMapper) {
        this.systemSettingMapper = systemSettingMapper;
    }

    @Value("${forum.upload-dir:uploads}")
    private String uploadDir;

    @PostMapping("/upload")
    public ApiResponse<Object> upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ApiResponse.fail("文件不能为空");
        }
        try {
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            // 图片上传大小限制（KB）
            if (isImageFile(originalFilename)) {
                int maxKb = getIntSetting("forum.imageMaxSizeKb", 2048);
                long maxBytes = (long) maxKb * 1024L;
                if (file.getSize() > maxBytes) {
                    return ApiResponse.fail("图片大小超出限制，当前最多允许 " + maxKb + " KB");
                }
            }
            String ext = "";
            int dot = originalFilename.lastIndexOf('.');
            if (dot >= 0) {
                ext = originalFilename.substring(dot);
            }
            String filename = UUID.randomUUID().toString().replace("-", "") + ext;
            // 统一将上传目录放在应用工作目录下，避免 Tomcat 临时目录导致找不到文件
            String baseDir = System.getProperty("user.dir");
            Path dir = Paths.get(baseDir, uploadDir);
            Files.createDirectories(dir);
            Path target = dir.resolve(filename);
            file.transferTo(target.toFile());

            // 使用 /api/uploads 路径，方便前端通过 Vite 代理访问后端静态资源
            String url = "/api/uploads/" + filename;
            Map<String, Object> data = new HashMap<>();
            data.put("fileName", originalFilename);
            data.put("url", url);
            return ApiResponse.success(data);
        } catch (IOException e) {
            return ApiResponse.fail("文件上传失败: " + e.getMessage());
        }
    }

    private boolean isImageFile(String fileName) {
        if (fileName == null) return false;
        String lower = fileName.toLowerCase();
        return lower.endsWith(".png")
                || lower.endsWith(".jpg")
                || lower.endsWith(".jpeg")
                || lower.endsWith(".gif")
                || lower.endsWith(".webp")
                || lower.endsWith(".bmp")
                || lower.endsWith(".svg");
    }

    private int getIntSetting(String key, int defaultValue) {
        SystemSetting setting = systemSettingMapper.selectOne(new LambdaQueryWrapper<SystemSetting>()
                .eq(SystemSetting::getSettingKey, key)
                .last("limit 1"));
        if (setting == null || setting.getSettingValue() == null) return defaultValue;
        try {
            return Integer.parseInt(setting.getSettingValue().trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }
}

