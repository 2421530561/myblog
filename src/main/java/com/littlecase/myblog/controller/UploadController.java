package com.littlecase.myblog.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文件上传接口（M2 里程碑实现）
 * <p>
 * 计划接口（登录用户）：
 * POST /api/upload/image   上传图片（jpg/png/webp/gif，≤5MB，UUID 重命名）
 * <p>
 * 存储：本地磁盘（application.yml 中 blog.upload-dir），
 * 通过 WebMvcConfig 的虚拟路径映射以 /upload/** 访问
 */
@RestController
@RequestMapping("/api/upload")
public class UploadController {
    // TODO M2: 图片上传
}
