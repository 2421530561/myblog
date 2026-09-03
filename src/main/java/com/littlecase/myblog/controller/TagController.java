package com.littlecase.myblog.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.littlecase.myblog.common.Result;
import com.littlecase.myblog.entity.Tag;
import com.littlecase.myblog.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
@RequiredArgsConstructor
public class TagController {
    // TODO M2: 标签列表 / 标签 CRUD
    private final TagService tagService;
    /** 公开：全部标签列表 */
    @GetMapping("/list")
    public Result<List<Tag>> list(){
        return Result.ok(tagService.listAll());
    }
    /** 管理员：新增标签 */
    @PostMapping
    @SaCheckRole("ADMIN")
    public Result<Tag> create(@RequestParam String name){
        return Result.ok(tagService.create(name));
    }
        /** 管理员：删除标签 */
    @DeleteMapping("/{id}")
    @SaCheckRole("ADMIN")
    public Result<Void> delete(@PathVariable Long id) {
        tagService.delete(id);
        return Result.ok();
    }

    
}
