package com.littlecase.myblog.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 评论接口（M3 里程碑实现）
 * <p>
 * 计划接口（公开）：
 * GET /api/comment/page   某文章评论列表（分页，楼中楼）
 * <p>
 * 计划接口（登录用户）：
 * POST /api/comment       发表评论（必须登录，可回复 parent_id）
 * DELETE /api/comment/{id} 删除评论（作者本人或 ADMIN）
 */
@RestController
@RequestMapping("/api/comment")
public class CommentController {
    // TODO M3: 评论列表 / 发表 / 删除
}
