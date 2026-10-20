package com.littlecase.myblog.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Web MVC 配置
 * <p>
 * 上传文件虚拟路径映射：/upload/** → 本地磁盘目录（PRD 决策：图片存本地磁盘）。
 * 目录需转成绝对路径，file: 用相对路径在不同启动目录下会解析失败。
 * <p>
 * 认证拦截与注解鉴权在 {@link SaTokenConfig} 中注册。
 */
@Slf4j
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${blog.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String absolute = Paths.get(uploadDir).toAbsolutePath().normalize().toString()
                .replace("\\", "/");
        log.info("上传文件访问映射：/upload/** -> file:{}/", absolute);
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:" + absolute + "/");
    }
}
