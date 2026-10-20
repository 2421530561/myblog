package com.littlecase.myblog.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.littlecase.myblog.common.BusinessException;
import com.littlecase.myblog.common.Result;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 文件上传接口（M2 里程碑，PRD 6 安全 - 上传约束）
 * <p>
 * POST /api/upload/image  上传图片：jpg/png/webp/gif，单文件 ≤5MB，UUID 重命名
 * <p>
 * 存储：本地磁盘（application.yml 中 blog.upload-dir，默认 ./upload），
 * 落盘路径 ./upload/{yyyyMMdd}/{uuid}.{ext}，通过 WebMvcConfig 的虚拟路径映射以 /upload/** 访问。
 * <p>
 * 注意：上传目录必须转成**绝对路径**再落盘——相对路径会被解析到 Tomcat 临时目录，
 * MultipartFile#transferTo 会直接抛 IOException。
 */
@Slf4j
@RestController
@RequestMapping("/api/upload")
@SaCheckLogin
public class UploadController {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png", "webp", "gif");
    private static final DateTimeFormatter DATE_DIR = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Value("${blog.upload-dir}")
    private String uploadDir;

    /** 启动时解析成绝对路径并创建根目录 */
    private Path baseDir;

    @PostConstruct
    public void init() throws IOException {
        baseDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(baseDir);
        log.info("图片上传目录：{}", baseDir);
    }

    @PostMapping("/image")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("图片大小不能超过5MB");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new BusinessException("无效的文件格式");
        }
        String extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException("仅支持 jpg/png/webp/gif 格式的图片");
        }

        String dateDir = LocalDate.now().format(DATE_DIR);
        String newFileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        String relativePath = dateDir + "/" + newFileName;

        try {
            Path targetDir = baseDir.resolve(dateDir);
            Files.createDirectories(targetDir);
            Path target = baseDir.resolve(relativePath).normalize();
            if (!target.startsWith(baseDir)) {
                // 防御路径穿越（UUID 重命名后理论不会触发，保留兜底）
                throw new BusinessException("非法的文件路径");
            }
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return Result.ok(Map.of("url", "/upload/" + relativePath));
        } catch (IOException e) {
            log.error("图片上传失败：{}", e.getMessage(), e);
            throw new BusinessException("图片上传失败，请稍后重试");
        }
    }
}
