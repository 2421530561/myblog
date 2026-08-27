package com.littlecase.myblog.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口（M1 里程碑实现）
 * <p>
 * 计划接口：
 * POST /api/auth/register  注册（username/email/password，密码 BCrypt 加密）
 * POST /api/auth/login     登录（校验密码，签发 JWT，返回 token + 用户信息）
 * POST /api/auth/logout    登出
 * GET  /api/auth/me        当前登录用户信息
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    // TODO M1: 注册 / 登录 / 登出 / 当前用户
}
