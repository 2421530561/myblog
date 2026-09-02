package com.littlecase.myblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.littlecase.myblog.entity.Tag;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TagMapper extends BaseMapper<Tag> {
}