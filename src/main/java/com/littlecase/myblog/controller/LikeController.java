package com.littlecase.myblog.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.littlecase.myblog.common.Result;
import com.littlecase.myblog.service.ArticleLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 点赞接口（M3 里程碑实现，PRD 4.4 点赞）
 * <p>
 * POST /api/like/article/{id}        切换点赞（返回当前点赞状态与点赞数）
 * GET  /api/like/article/{id}/count  点赞数（公开）
 * GET  /api/like/article/{id}/status 当前登录用户是否已赞
 */
@RestController
@RequestMapping("/api/like")
@RequiredArgsConstructor
public class LikeController {

    private final ArticleLikeService likeService;

    /** 切换点赞 */
    @PostMapping("/article/{id}")
    @SaCheckLogin
    public Result<Map<String, Object>> toggle(@PathVariable Long id) {
        long userId = StpUtil.getLoginIdAsLong();
        boolean liked = likeService.toggle(id, userId);
        return Result.ok(Map.of("liked", liked, "count", likeService.count(id)));
    }

    /** 点赞数 */
    @GetMapping("/article/{id}/count")
    public Result<Long> count(@PathVariable Long id) {
        return Result.ok(likeService.count(id));
    }

    /** 当前登录用户是否已赞 */
    @GetMapping("/article/{id}/status")
    @SaCheckLogin
    public Result<Map<String, Boolean>> status(@PathVariable Long id) {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(Map.of("liked", likeService.isLiked(id, userId)));
    }
}