package com.littlecase.myblog.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文章接口（M2 里程碑实现）
 * <p>
 * 计划接口（公开）：
 * GET    /api/article/page       文章列表（分页，仅已发布）
 * GET    /api/article/{id}       文章详情（阅读量 +1）
 * GET    /api/article/archives   归档（按年月）
 * GET    /api/article/search     全文检索（M4，返回高亮字段）
 * <p>
 * 计划接口（登录用户，管理自己的文章）：
 * POST   /api/article            创建文章（草稿）
 * PUT    /api/article/{id}       编辑文章
 * DELETE /api/article/{id}       删除文章（自己的）
 * POST   /api/article/{id}/submit  提交审核
 * <p>
 * 计划接口（点赞，M3）：
 * POST   /api/article/{id}/like       点赞
 * DELETE /api/article/{id}/like       取消点赞
 * GET    /api/article/{id}/like/status 当前用户点赞状态
 */
@RestController
@RequestMapping("/api/article")
public class ArticleController {
    // TODO M2: 列表 / 详情 / 写作与审核流；M3: 点赞；M4: 全文检索
}
