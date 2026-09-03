package com.littlecase.myblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.littlecase.myblog.common.BusinessException;
import com.littlecase.myblog.entity.Category;
import com.littlecase.myblog.mapper.CategoryMapper;
import com.littlecase.myblog.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
     private final CategoryMapper categoryMapper;

     @Override
     public List<Category> listAll()
     {
        return categoryMapper.selectList(
        new LambdaQueryWrapper<Category>().orderByAsc(Category::getSort));
     }

     @Override
     public Category create(String name, Integer sort)
     {
        if (categoryMapper.exists(new LambdaQueryWrapper<Category>().eq(Category::getName, name))) {
            throw new BusinessException("分类名称已存在");
        }
        Category category =new Category();
        category.setName(name);
        category.setSort(sort !=null ? sort : 0);
        categoryMapper.insert(category);
        return category;
     }
     @Override
     public void update (Long id,String name,Integer sort)
     {
        Category category =categoryMapper.selectById(id);
        if(category==null)throw new BusinessException("分类不存在");
        if(name!=null)category.setName(name);
        if(sort!=null)category.setSort(sort);
        categoryMapper.updateById(category);
     }

     @Override
     public void delete(Long id)
     {
        categoryMapper.deleteById(id);
     }

}
