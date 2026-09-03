package com.littlecase.myblog.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.littlecase.myblog.common.Result;
import com.littlecase.myblog.entity.Category;
import com.littlecase.myblog.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类接口（M2 里程碑实现）
 * <p>
 * 计划接口（公开）：
 * GET /api/category/list   分类列表（含文章数）
 * GET /api/category/{id}/articles  某分类下的文章列表
 * <p>
 * 计划接口（仅 ADMIN）：
 * POST /api/category       新增分类
 * PUT  /api/category/{id}  修改分类
 * DELETE /api/category/{id} 删除分类
 */
@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryController {
    // TODO M2: 分类列表 / 分类 CRUD
    private final CategoryService categoryService;
    /** 公开：全部分类列表 */
    @GetMapping("/list")
    public Result<List<Category>> list()
    {
        return Result.ok(categoryService.listAll());
    }
    /** 管理员：新增分类 */
    @PostMapping
    @SaCheckRole("ADMIN")
    public Result<Category> create(@RequestParam String name, @RequestParam(required = false) Integer sort)
    {
        return Result.ok(categoryService.create(name, sort));
    }
     /** 管理员：修改分类 */
    @PutMapping("/{id}")
    @SaCheckRole("ADMIN")
    public Result<Void> update(@PathVariable Long id, @RequestParam(required = false) String name, @RequestParam(required = false) Integer sort)
    {
        categoryService.update(id, name, sort);
        return Result.ok();
    }

    /** 管理员：删除分类 */
    @DeleteMapping("/{id}")
    @SaCheckRole("ADMIN")
    public Result<Void> delete(@PathVariable Long id)
    {
        categoryService.delete(id);
        return Result.ok();
    }
}
