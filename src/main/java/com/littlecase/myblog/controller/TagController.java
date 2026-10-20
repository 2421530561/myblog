package com.littlecase.myblog.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.littlecase.myblog.common.Result;
import com.littlecase.myblog.dto.TagDTO;
import com.littlecase.myblog.entity.Tag;
import com.littlecase.myblog.service.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 标签接口（M2 里程碑）
 * <p>
 * 公开：
 * GET /api/tag/list           标签列表
 * <p>
 * 仅 ADMIN（PRD 4.3 标签管理 CRUD）：
 * POST   /api/tag             新增标签
 * PUT    /api/tag/{id}        重命名标签
 * DELETE /api/tag/{id}        删除标签
 */
@RestController
@RequestMapping("/api/tag")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    /** 公开：全部标签列表 */
    @GetMapping("/list")
    public Result<List<Tag>> list() {
        return Result.ok(tagService.listAll());
    }

    /** 管理员：新增标签 */
    @PostMapping
    @SaCheckRole("ADMIN")
    public Result<Tag> create(@Valid @RequestBody TagDTO dto) {
        return Result.ok(tagService.create(dto.getName()));
    }

    /** 管理员：重命名标签 */
    @PutMapping("/{id}")
    @SaCheckRole("ADMIN")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody TagDTO dto) {
        tagService.update(id, dto.getName());
        return Result.ok();
    }

    /** 管理员：删除标签 */
    @DeleteMapping("/{id}")
    @SaCheckRole("ADMIN")
    public Result<Void> delete(@PathVariable Long id) {
        tagService.delete(id);
        return Result.ok();
    }
}
