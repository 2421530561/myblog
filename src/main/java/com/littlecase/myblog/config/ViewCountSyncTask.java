package com.littlecase.myblog.config;

import com.littlecase.myblog.mapper.ArticleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 阅读量定时落库任务（M3，PRD 5.3）
 * <p>
 * 前台访问详情时只自增 Redis（key=blog:view:{articleId}），
 * 每 5 分钟把 Redis 增量累加进 article.view_count 并清零增量，保证计数最终一致。
 * 任务内部异常全部吞掉并打日志，绝不影响主链路（PRD 6 健壮性）。
 */
@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class ViewCountSyncTask {

    private static final String VIEW_KEY = "blog:view:";

    private final StringRedisTemplate redisTemplate;
    private final ArticleMapper articleMapper;

    /** PRD：每 5 分钟执行一次（cron 秒 分 时 日 月 周） */
    @Scheduled(cron = "0 0/5 * * * ?")
    public void syncViewCount() {
        Set<String> keys;
        try {
            keys = redisTemplate.keys(VIEW_KEY + "*");
        } catch (Exception e) {
            log.warn("阅读量落库跳过：Redis 不可用", e);
            return;
        }
        if (keys == null || keys.isEmpty()) {
            return;
        }
        int synced = 0;
        for (String key : keys) {
            String idStr = key.substring(VIEW_KEY.length());
            try {
                String val = redisTemplate.opsForValue().get(key);
                long delta = val == null ? 0 : Long.parseLong(val);
                if (delta <= 0) {
                    redisTemplate.delete(key);
                    continue;
                }
                articleMapper.incViewAndReset(Long.parseLong(idStr), delta);
                redisTemplate.delete(key); // 落库成功后再清零增量
                synced++;
            } catch (NumberFormatException e) {
                log.warn("非法阅读量 key：{}", key);
                redisTemplate.delete(key);
            } catch (Exception e) {
                // 单条失败不影响其他文章，下次任务会重试（key 未删除）
                log.warn("阅读量落库失败 key={}", key, e);
            }
        }
        log.info("阅读量定时落库完成，本次同步 {} 篇文章", synced);
    }
}
