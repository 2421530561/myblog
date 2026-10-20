package com.littlecase.myblog.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.littlecase.myblog.common.Result;
import com.littlecase.myblog.dto.ArticleRejectDTO;
import com.littlecase.myblog.service.ArticleService;
import com.littlecase.myblog.vo.ArticlePageItemVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 后台管理接口（仅 ADMIN 角色）
 * <p>
 * 文章审核（M2）：
 * GET    /api/admin/article/page           全量文章管理列表（按状态/作者/标题筛选）
 * POST   /api/admin/article/{id}/publish   审核通过并发布（待审核 → 已发布）
 * POST   /api/admin/article/{id}/reject    驳回（待审核 → 已驳回，携带原因）
 * POST   /api/admin/article/{id}/offline   下架（已发布 → 草稿）
 * <p>
 * 评论 / 用户管理（M3/M4）、站点配置 / 友链 / 仪表盘（M4）后续实现。
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@SaCheckRole("ADMIN")
public class AdminController {

    private final ArticleService articleService;

    /** 全量文章管理列表（含草稿/待审核/已驳回） */
    @GetMapping("/article/page")
    public Result<IPage<ArticlePageItemVO>> articlePage(@RequestParam(required = false) Integer status,
                                                        @RequestParam(required = false) Long userId,
                                                        @RequestParam(required = false) String keyword,
                                                        @RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "10") int size) {
        return Result.ok(articleService.getAdminPage(status, userId, keyword, page, size));
    }

    /** 审核通过并发布 */
    @PostMapping("/article/{id}/publish")
    public Result<Void> publish(@PathVariable Long id) {
        articleService.adminPublish(id);
        return Result.ok();
    }

    /** 驳回（填写原因） */
    @PostMapping("/article/{id}/reject")
    public Result<Void> reject(@PathVariable Long id, @Valid @RequestBody ArticleRejectDTO dto) {
        articleService.adminReject(id, dto.getReason());
        return Result.ok();
    }

    /** 下架 */
    @PostMapping("/article/{id}/offline")
    public Result<Void> offline(@PathVariable Long id) {
        articleService.adminOffline(id);
        return Result.ok();
    }
}
