package com.littlecase.myblog.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 分类接口（M2 里程碑实现）
 * <p>
 * 计划接口（公开）：
 * GET /api/category/list   分类列表（含文章数）
 * GET /api/category/{id}/articles  某分类下的文章列表
 * <p>
 * 计划接口（仅 ADMIN）：
 * POST /api/category       新增分类
 * PUT  /api/category/{id}  修改分类
 * DELETE /api/category/{id} 删除分类
 */
@RestController
@RequestMapping("/api/category")
public class CategoryController {
    // TODO M2: 分类列表 / 分类 CRUD
}
