package com.littlecase.myblog.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.littlecase.myblog.common.Result;
import com.littlecase.myblog.dto.CommentDTO;
import com.littlecase.myblog.service.CommentService;
import com.littlecase.myblog.vo.CommentVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 评论接口（M3，PRD 5.2）
 * <p>
 * GET    /api/comment/page?articleId=&page=&size=  文章评论列表（公开，按顶层评论分页，楼中楼展示）
 * POST   /api/comment                              发表评论（登录，parentId 可空=顶层）
 * DELETE /api/comment/{id}                         删除评论（作者本人或 ADMIN，连带全部回复）
 */
@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /** 前台：文章评论列表（楼中楼，顶层评论分页） */
    @GetMapping("/page")
    public Result<IPage<CommentVO>> page(@RequestParam Long articleId,
                                         @RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "10") int size) {
        return Result.ok(commentService.listByArticle(articleId, page, size));
    }

    /** 登录用户：发表评论 / 回复评论 */
    @PostMapping
    @SaCheckLogin
    public Result<Long> add(@Valid @RequestBody CommentDTO dto) {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(commentService.add(dto.getArticleId(), userId, dto.getParentId(), dto.getContent()));
    }

    /** 删除评论（作者本人或 ADMIN，连带删除其回复） */
    @DeleteMapping("/{id}")
    @SaCheckLogin
    public Result<Void> delete(@PathVariable Long id) {
        long userId = StpUtil.getLoginIdAsLong();
        commentService.delete(id, userId);
        return Result.ok();
    }
}