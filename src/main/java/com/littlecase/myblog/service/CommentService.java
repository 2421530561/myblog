package com.littlecase.myblog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.littlecase.myblog.vo.CommentVO;

public interface CommentService {

    /** 文章评论列表（楼中楼，按顶层评论分页） */
    IPage<CommentVO> listByArticle(Long articleId, int page, int size);

    /** 发表评论 / 回复评论（PRD 5.2：仅登录用户，支持楼中楼） */
    Long add(Long articleId, Long userId, Long parentId, String content);

    /** 删除评论：作者本人或 ADMIN，连带其所有回复一并逻辑删除 */
    void delete(Long id, Long userId);
}
