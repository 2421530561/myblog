package com.littlecase.myblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.littlecase.myblog.entity.Category;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}