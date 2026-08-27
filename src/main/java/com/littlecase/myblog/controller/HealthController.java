package com.littlecase.myblog.controller;

import com.littlecase.myblog.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检查：验证前后端联通
 * 后端启动后访问 http://localhost:8080/api/health
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    @GetMapping
    public Result<String> health() {
        return Result.ok("blog backend is running");
    }
}
