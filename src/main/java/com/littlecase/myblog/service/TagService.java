package com.littlecase.myblog.service;

import com.littlecase.myblog.entity.Tag;

import java.util.List;
public interface TagService {
    List<Tag>listAll();
    Tag create(String name);
    
    void delete(Long id);
}
