package com.littlecase.myblog.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.littlecase.myblog.common.Result;
import com.littlecase.myblog.dto.ArticleDTO;
import com.littlecase.myblog.service.ArticleService;
import com.littlecase.myblog.vo.ArticleDetailVO;
import com.littlecase.myblog.vo.ArticlePageItemVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/article")
@RequiredArgsConstructor
public class ArticleController {
    // TODO M2: 列表 / 详情 / 写作与审核流；M3: 点赞；M4: 全文检索

    private final ArticleService articleService;
    /** 前台：已发布文章分页列表 */
    @GetMapping("/page")
    public Result<IPage<ArticlePageItemVO>>page( @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long tagId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size)
            {
                return Result.ok(articleService.getPublishedPage(categoryId, tagId, keyword, page, size));
            }
    /** 前台：已发布文章详情 */
    @GetMapping("/{id}")
    public Result<ArticleDetailVO> detail(@PathVariable Long id)
    {
        long userId=0;
        try{
            userId=StpUtil.getLoginIdAsLong();
        }
        catch(Exception ignored){}
        return Result.ok(articleService.getDetail(id, userId));
    }
    /** 用户：创建文章（草稿或提交审核） */
    @PostMapping
    @SaCheckLogin
    public Result<Long>create(@Valid @RequestBody ArticleDTO dto)
    {
        long userId =StpUtil.getLoginIdAsLong();
        return Result.ok(articleService.create(dto,userId));
    }
        /** 用户：编辑文章 */
    @PutMapping("/{id}")
    @SaCheckLogin
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ArticleDTO dto) {
        long userId = StpUtil.getLoginIdAsLong();
        articleService.update(id, dto, userId);
        return Result.ok();
    }

    /** 用户：提交审核 */
    @PostMapping("/{id}/submit")
    @SaCheckLogin
    public Result<Void> submit(@PathVariable Long id) {
        long userId = StpUtil.getLoginIdAsLong();
        articleService.submit(id, userId);
        return Result.ok();
    }

    /** 用户：删除文章 */
    @DeleteMapping("/{id}")
    @SaCheckLogin
    public Result<Void> delete(@PathVariable Long id) {
        long userId = StpUtil.getLoginIdAsLong();
        articleService.delete(id, userId);
        return Result.ok();
    }
    
}
