package com.littlecase.myblog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.littlecase.myblog.dto.ArticleDTO;
import com.littlecase.myblog.vo.ArticleDetailVO;
import com.littlecase.myblog.vo.ArticlePageItemVO;

public interface ArticleService {

    IPage<ArticlePageItemVO> getPublishedPage(Long categoryId, Long tagId, String keyword, int page, int pagesize);

    ArticleDetailVO getDetail(Long id, Long currentUserId);

    Long create(ArticleDTO dto, Long userId);

    void update(Long id, ArticleDTO dto, Long userId);

    void submit(Long id, Long userId);

    void delete(Long id, Long userId);
}