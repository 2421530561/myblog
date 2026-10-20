package com.littlecase.myblog.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.littlecase.myblog.common.Result;
import com.littlecase.myblog.dto.ArticleDTO;
import com.littlecase.myblog.service.ArticleService;
import com.littlecase.myblog.vo.ArticleDetailVO;
import com.littlecase.myblog.vo.ArticleEditVO;
import com.littlecase.myblog.vo.ArticlePageItemVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 文章接口（M2 里程碑）
 * <p>
 * 公开：
 * GET    /api/article/page        文章列表（分页，仅已发布，置顶优先）
 * GET    /api/article/{id}        文章详情（阅读量 +1）
 * <p>
 * 登录用户（管理自己的文章，见 PRD 4.2）：
 * GET    /api/article/mine        我的文章（草稿/待审核/已发布/已驳回，可选 status 筛选）
 * GET    /api/article/{id}/edit   编辑回显（Markdown 原文 + 标签 ID，作者本人或管理员）
 * POST   /api/article             新建（action：0=草稿 / 1=提交审核 / 2=管理员直接发布）
 * PUT    /api/article/{id}        编辑
 * POST   /api/article/{id}/submit 提交审核
 * DELETE /api/article/{id}        删除（逻辑删除）
 * <p>
 * 后台审核见 {@link AdminController}；点赞在 M3；全文检索在 M4。
 */
@RestController
@RequestMapping("/api/article")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    /** 前台：已发布文章分页列表 */
    @GetMapping("/page")
    public Result<IPage<ArticlePageItemVO>> page(@RequestParam(required = false) Long categoryId,
                                                 @RequestParam(required = false) Long tagId,
                                                 @RequestParam(required = false) String keyword,
                                                 @RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(defaultValue = "10") int size) {
        return Result.ok(articleService.getPublishedPage(categoryId, tagId, keyword, page, size));
    }

    /** 登录用户：我的文章 */
    @GetMapping("/mine")
    @SaCheckLogin
    public Result<IPage<ArticlePageItemVO>> mine(@RequestParam(required = false) Integer status,
                                                 @RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(defaultValue = "10") int size) {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(articleService.getMyPage(userId, status, page, size));
    }

    /** 登录用户：编辑回显（Markdown 原文） */
    @GetMapping("/{id}/edit")
    @SaCheckLogin
    public Result<ArticleEditVO> editData(@PathVariable Long id) {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(articleService.getEditData(id, userId));
    }

    /** 前台：已发布文章详情 */
    @GetMapping("/{id}")
    public Result<ArticleDetailVO> detail(@PathVariable Long id) {
        long userId = 0;
        try {
            userId = StpUtil.getLoginIdAsLong();
        } catch (Exception ignored) {
            // 游客访问：无登录态，点赞状态等个性化字段留空
        }
        return Result.ok(articleService.getDetail(id, userId));
    }

    /** 登录用户：新建文章（草稿 / 提交审核 / 管理员直接发布） */
    @PostMapping
    @SaCheckLogin
    public Result<Long> create(@Valid @RequestBody ArticleDTO dto) {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(articleService.create(dto, userId));
    }

    /** 登录用户：编辑文章 */
    @PutMapping("/{id}")
    @SaCheckLogin
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ArticleDTO dto) {
        long userId = StpUtil.getLoginIdAsLong();
        articleService.update(id, dto, userId);
        return Result.ok();
    }

    /** 登录用户：提交审核 */
    @PostMapping("/{id}/submit")
    @SaCheckLogin
    public Result<Void> submit(@PathVariable Long id) {
        long userId = StpUtil.getLoginIdAsLong();
        articleService.submit(id, userId);
        return Result.ok();
    }

    /** 登录用户：删除文章 */
    @DeleteMapping("/{id}")
    @SaCheckLogin
    public Result<Void> delete(@PathVariable Long id) {
        long userId = StpUtil.getLoginIdAsLong();
        articleService.delete(id, userId);
        return Result.ok();
    }
}
