package com.littlecase.myblog.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台管理接口（仅 ADMIN 角色，M1 起逐步实现）
 * <p>
 * 计划接口：
 * 文章审核（M2）：
 * POST   /api/admin/article/{id}/publish   审核通过并发布
 * POST   /api/admin/article/{id}/reject    驳回（携带原因）
 * POST   /api/admin/article/{id}/offline   下架
 * GET    /api/admin/article/page           全量文章管理列表（按状态/作者筛选）
 * <p>
 * 评论 / 用户管理（M3/M4）：
 * GET    /api/admin/comment/page           全站评论列表
 * DELETE /api/admin/comment/{id}           删除评论
 * GET    /api/admin/user/page              用户列表
 * PUT    /api/admin/user/{id}/status       禁用 / 启用用户
 * <p>
 * 站点配置 / 友链 / 仪表盘（M4）：
 * GET    /api/admin/dashboard/stats        统计
 * GET    /api/config/public                站点配置（公开）
 * PUT    /api/admin/config                 更新站点配置
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    // TODO M2-M4: 审核 / 评论管理 / 用户管理 / 站点配置 / 仪表盘
}
