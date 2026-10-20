package com.littlecase.myblog.service;

import com.littlecase.myblog.entity.Tag;

import java.util.List;
public interface TagService {
    List<Tag>listAll();
    Tag create(String name);

    /** 重命名标签（PRD 4.3 标签管理 CRUD） */
    void update(Long id, String name);

    void delete(Long id);
}
