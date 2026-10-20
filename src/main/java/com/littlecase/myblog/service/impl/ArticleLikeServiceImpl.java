package com.littlecase.myblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.littlecase.myblog.common.ArticleStatus;
import com.littlecase.myblog.common.BusinessException;
import com.littlecase.myblog.entity.Article;
import com.littlecase.myblog.entity.ArticleLike;
import com.littlecase.myblog.mapper.ArticleLikeMapper;
import com.littlecase.myblog.mapper.ArticleMapper;
import com.littlecase.myblog.service.ArticleLikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 点赞服务（M3，PRD 5.3）
 * <p>
 * 以 MySQL article_like 表为唯一事实来源（uk_article_user 防重复点赞）；
 * 点赞数读取顺序：Redis 缓存 blog:like:count:{articleId} → 未命中查库并回填（TTL 10 分钟）；
 * 点赞/取消后同步更新缓存与 article.like_count。Redis 故障时全部降级为查库，不影响点赞本身。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleLikeServiceImpl implements ArticleLikeService {

    private static final String LIKE_COUNT_KEY = "blog:like:count:";
    private static final Duration LIKE_COUNT_TTL = Duration.ofMinutes(10);

    private final ArticleLikeMapper articleLikeMapper;
    private final ArticleMapper articleMapper;
    private final StringRedisTemplate redisTemplate;

    @Override
    public boolean toggle(Long articleId, Long userId) {
        Article article = articleMapper.selectById(articleId);
        if (article == null || !ArticleStatus.PUBLISHED.is(article.getStatus())) {
            throw new BusinessException(404, "文章不存在或未发布");
        }

        boolean liked = isLiked(articleId, userId);
        if (liked) {
            articleLikeMapper.delete(new LambdaQueryWrapper<ArticleLike>()
                    .eq(ArticleLike::getArticleId, articleId)
                    .eq(ArticleLike::getUserId, userId));
        } else {
            ArticleLike like = new ArticleLike();
            like.setArticleId(articleId);
            like.setUserId(userId);
            try {
                articleLikeMapper.insert(like);
            } catch (DuplicateKeyException e) {
                // 并发下的重复点赞：唯一索引兜底，视为已点赞（幂等）
                return true;
            }
        }

        // 以表内真实条数为准刷新 DB 冗余字段与 Redis 缓存
        long count = countFromDb(articleId);
        articleMapper.update(null, new LambdaUpdateWrapper<Article>()
                .eq(Article::getId, articleId)
                .set(Article::getLikeCount, (int) count));
        cacheCount(articleId, count);
        return !liked;
    }

    @Override
    public boolean isLiked(Long articleId, Long userId) {
        if (userId == null) {
            return false;
        }
        return articleLikeMapper.selectCount(new LambdaQueryWrapper<ArticleLike>()
                .eq(ArticleLike::getArticleId, articleId)
                .eq(ArticleLike::getUserId, userId)) > 0;
    }

    @Override
    public long count(Long articleId) {
        try {
            String cached = redisTemplate.opsForValue().get(LIKE_COUNT_KEY + articleId);
            if (cached != null) {
                return Long.parseLong(cached);
            }
        } catch (Exception e) {
            log.warn("Redis 点赞数读取失败，降级查库 articleId={}", articleId, e);
            return countFromDb(articleId);
        }
        long count = countFromDb(articleId);
        cacheCount(articleId, count); // 未命中则回填，TTL 10 分钟
        return count;
    }

    private long countFromDb(Long articleId) {
        return articleLikeMapper.selectCount(new LambdaQueryWrapper<ArticleLike>()
                .eq(ArticleLike::getArticleId, articleId));
    }

    private void cacheCount(Long articleId, long count) {
        try {
            redisTemplate.opsForValue().set(LIKE_COUNT_KEY + articleId,
                    String.valueOf(Math.max(0, count)), LIKE_COUNT_TTL);
        } catch (Exception e) {
            log.warn("Redis 点赞数回填失败 articleId={}", articleId, e);
        }
    }
}
