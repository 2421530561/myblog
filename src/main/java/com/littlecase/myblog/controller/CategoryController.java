package com.littlecase.myblog.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.littlecase.myblog.common.Result;
import com.littlecase.myblog.dto.CategoryDTO;
import com.littlecase.myblog.entity.Category;
import com.littlecase.myblog.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类接口（M2 里程碑）
 * <p>
 * 公开：
 * GET /api/category/list           分类列表（按 sort 升序）
 * <p>
 * 仅 ADMIN（PRD 4.3 分类管理 CRUD）：
 * POST   /api/category             新增分类
 * PUT    /api/category/{id}        修改分类
 * DELETE /api/category/{id}        删除分类
 */
@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /** 公开：全部分类列表 */
    @GetMapping("/list")
    public Result<List<Category>> list() {
        return Result.ok(categoryService.listAll());
    }

    /** 管理员：新增分类 */
    @PostMapping
    @SaCheckRole("ADMIN")
    public Result<Category> create(@Valid @RequestBody CategoryDTO dto) {
        return Result.ok(categoryService.create(dto.getName(), dto.getSort()));
    }

    /** 管理员：修改分类 */
    @PutMapping("/{id}")
    @SaCheckRole("ADMIN")
    public Result<Void> update(@PathVariable Long id, @RequestBody CategoryDTO dto) {
        categoryService.update(id, dto.getName(), dto.getSort());
        return Result.ok();
    }

    /** 管理员：删除分类 */
    @DeleteMapping("/{id}")
    @SaCheckRole("ADMIN")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.ok();
    }
}
