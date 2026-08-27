package com.littlecase.myblog.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 标签接口（M2 里程碑实现）
 * <p>
 * 计划接口（公开）：
 * GET /api/tag/list   标签列表（含文章数）
 * GET /api/tag/{id}/articles  某标签下的文章列表
 * <p>
 * 计划接口（仅 ADMIN）：
 * POST /api/tag       新增标签
 * PUT  /api/tag/{id}  修改标签
 * DELETE /api/tag/{id} 删除标签
 */
@RestController
@RequestMapping("/api/tag")
public class TagController {
    // TODO M2: 标签列表 / 标签 CRUD
}
