package com.littlecase.myblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.littlecase.myblog.common.BusinessException;
import com.littlecase.myblog.entity.Tag;
import com.littlecase.myblog.mapper.TagMapper;
import com.littlecase.myblog.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {
    private final TagMapper tagMapper;

    @Override
    public List<Tag> listAll()
    {
        return tagMapper.selectList(null);
    }

    @Override
    public Tag create(String name)
    {
        if (tagMapper.exists(new LambdaQueryWrapper<Tag>().eq(Tag::getName, name))) {
            throw new BusinessException("标签名称已存在");
        }
        Tag tag = new Tag();
        tag.setName(name);
        tagMapper.insert(tag);
        return tag;
    }

    @Override
    public void delete(Long id)
    {
        tagMapper.deleteById(id);
    }
    
}
