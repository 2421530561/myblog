package com.littlecase.myblog.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 * <p>
 * 1. 上传文件虚拟路径映射：/upload/** → 本地磁盘目录（PRD 决策：图片存本地磁盘）
 * 2. M1 认证阶段：在此注册 Sa-Token 拦截器（仅拦截 /api/**，放行登录注册与 /upload/**）
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${blog.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }
}
