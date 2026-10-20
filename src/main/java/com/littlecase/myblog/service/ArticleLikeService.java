package com.littlecase.myblog.service;

public interface ArticleLikeService {
    boolean toggle(Long articleId, Long userId);//返回点赞后状态：true-点赞成功，false-取消点赞成功
    
    boolean isLiked(Long articleId, Long userId);//判断是否点赞

    long count(Long articleId);//统计点赞数
}
