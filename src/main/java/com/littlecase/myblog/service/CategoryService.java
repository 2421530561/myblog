package com.littlecase.myblog.service;

import com.littlecase.myblog.entity.Category;

import java.util.List;

public interface CategoryService {

    List<Category> listAll();

    Category create(String name, Integer sort);

    void update(Long id, String name, Integer sort);

    void delete(Long id);
}