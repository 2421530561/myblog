package com.littlecase.myblog.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.littlecase.myblog.common.ArticleStatus;
import com.littlecase.myblog.common.BusinessException;
import com.littlecase.myblog.entity.Article;
import com.littlecase.myblog.entity.Comment;
import com.littlecase.myblog.entity.User;
import com.littlecase.myblog.mapper.ArticleMapper;
import com.littlecase.myblog.mapper.CommentMapper;
import com.littlecase.myblog.mapper.UserMapper;
import com.littlecase.myblog.service.CommentService;
import com.littlecase.myblog.vo.CommentVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 评论服务（M3，PRD 5.2）
 * <p>
 * · 游客可读，登录可发；不做评论审核，默认直接可见。
 * · 楼中楼：按顶层评论分页，回复挂在所属顶层评论下展示（支持“回复 @某人”）。
 * · 作者本人或 ADMIN 可删除，删除时其所有后代回复一并逻辑删除。
 * · 频率限制：同一用户每分钟最多 5 条（Redis 计数，Redis 故障时放行不阻塞主链路）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private static final String COMMENT_RATE_KEY = "blog:comment:rate:";
    private static final int MAX_PER_MINUTE = 5;
    private static final int MAX_LENGTH = 1000;

    private final CommentMapper commentMapper;
    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;
    private final StringRedisTemplate redisTemplate;

    @Override
    public IPage<CommentVO> listByArticle(Long articleId, int page, int size) {
        // 1. 顶层评论分页（新的在前，发表后立刻能在第一页看到）
        Page<Comment> rootPage = new Page<>(Math.max(page, 1), Math.max(size, 1));
        IPage<Comment> roots = commentMapper.selectPage(rootPage,
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getArticleId, articleId)
                        .isNull(Comment::getParentId)
                        .eq(Comment::getStatus, 1)
                        .orderByDesc(Comment::getCreateTime)
                        .orderByDesc(Comment::getId));

        if (roots.getRecords().isEmpty()) {
            return new Page<CommentVO>(roots.getCurrent(), roots.getSize(), roots.getTotal());
        }

        // 2. 一次性取出该文章全部正常评论，用于挂载回复与解析“根评论”
        List<Comment> all = commentMapper.selectList(
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getArticleId, articleId)
                        .eq(Comment::getStatus, 1)
                        .orderByAsc(Comment::getCreateTime)
                        .orderByAsc(Comment::getId));
        Map<Long, Comment> byId = new HashMap<>();
        for (Comment c : all) {
            byId.put(c.getId(), c);
        }
        Map<Long, User> users = userMapOf(all);

        Map<Long, CommentVO> rootVoMap = new LinkedHashMap<>();
        List<CommentVO> records = new ArrayList<>();
        for (Comment c : roots.getRecords()) {
            CommentVO vo = toVO(c, users.get(c.getUserId()));
            vo.setChildren(new ArrayList<>());
            rootVoMap.put(c.getId(), vo);
            records.add(vo);
        }

        // 3. 把回复挂到所属顶层评论下（parent 指向回复时也能正确归位）
        for (Comment c : all) {
            if (c.getParentId() == null) {
                continue;
            }
            Comment root = resolveRoot(c, byId);
            CommentVO rootVo = root == null ? null : rootVoMap.get(root.getId());
            if (rootVo == null) {
                continue; // 根评论不在当前页 / 已删除
            }
            CommentVO child = toVO(c, users.get(c.getUserId()));
            Comment replied = byId.get(c.getParentId());
            if (replied != null && users.get(replied.getUserId()) != null) {
                child.setReplyNickname(nicknameOf(users.get(replied.getUserId())));
            }
            rootVo.getChildren().add(child);
        }

        Page<CommentVO> result = new Page<>(roots.getCurrent(), roots.getSize(), roots.getTotal());
        result.setRecords(records);
        return result;
    }

    @Override
    public Long add(Long articleId, Long userId, Long parentId, String content) {
        String text = content == null ? "" : content.trim();
        if (text.isEmpty()) {
            throw new BusinessException(400, "评论内容不能为空");
        }
        if (text.length() > MAX_LENGTH) {
            throw new BusinessException(400, "评论内容不能超过" + MAX_LENGTH + "字");
        }

        Article article = articleMapper.selectById(articleId);
        if (article == null || !ArticleStatus.PUBLISHED.is(article.getStatus())) {
            throw new BusinessException(404, "文章不存在或未发布");
        }
        // PRD 假设 6：被禁用用户无法评论（登录态可能残留，需在业务层再校验）
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(401, "用户不存在");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(403, "账号已被禁用，无法评论");
        }
        if (parentId != null) {
            Comment parent = commentMapper.selectById(parentId);
            if (parent == null || !parent.getArticleId().equals(articleId)) {
                throw new BusinessException(400, "被回复的评论不存在");
            }
        }

        checkRateLimit(userId);

        Comment c = new Comment();
        c.setArticleId(articleId);
        c.setUserId(userId);
        c.setParentId(parentId);
        c.setContent(text);
        c.setStatus(1);
        commentMapper.insert(c);
        return c.getId();
    }

    @Override
    public void delete(Long id, Long userId) {
        Comment c = commentMapper.selectById(id);
        if (c == null) {
            throw new BusinessException(404, "评论不存在");
        }
        boolean isOwner = c.getUserId().equals(userId);
        boolean isAdmin = StpUtil.hasRole("ADMIN");
        if (!isOwner && !isAdmin) {
            throw new BusinessException(403, "只能删除自己的评论");
        }

        // 收集该评论及其所有后代（楼中楼），统一逻辑删除
        List<Comment> all = commentMapper.selectList(
                new LambdaQueryWrapper<Comment>().eq(Comment::getArticleId, c.getArticleId()));
        Map<Long, List<Long>> childrenOf = new HashMap<>();
        for (Comment item : all) {
            if (item.getParentId() != null) {
                childrenOf.computeIfAbsent(item.getParentId(), k -> new ArrayList<>()).add(item.getId());
            }
        }
        List<Long> toDelete = new ArrayList<>();
        Set<Long> visited = new HashSet<>();
        Deque<Long> queue = new ArrayDeque<>();
        queue.add(id);
        while (!queue.isEmpty()) {
            Long cur = queue.poll();
            if (!visited.add(cur)) {
                continue;
            }
            toDelete.add(cur);
            List<Long> children = childrenOf.get(cur);
            if (children != null) {
                queue.addAll(children);
            }
        }
        commentMapper.delete(new LambdaQueryWrapper<Comment>().in(Comment::getId, toDelete));
    }

    // ------------------------------------------------------------------

    /** 频率限制：INCR + 首次设置 60s 过期；Redis 异常时放行 */
    private void checkRateLimit(Long userId) {
        String key = COMMENT_RATE_KEY + userId;
        try {
            Long count = redisTemplate.opsForValue().increment(key, 1L);
            if (count != null && count == 1L) {
                redisTemplate.expire(key, Duration.ofMinutes(1));
            }
            if (count != null && count > MAX_PER_MINUTE) {
                throw new BusinessException(400, "评论太频繁，请 1 分钟后再试");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("评论限流不可用（Redis），本次放行 userId={}", userId, e);
        }
    }

    /** 沿 parentId 向上找到顶层评论；遇到环形脏数据返回 null */
    private Comment resolveRoot(Comment c, Map<Long, Comment> byId) {
        Comment cur = c;
        Set<Long> seen = new HashSet<>();
        while (cur != null && cur.getParentId() != null) {
            if (!seen.add(cur.getId())) {
                return null;
            }
            cur = byId.get(cur.getParentId());
        }
        return cur;
    }

    private Map<Long, User> userMapOf(List<Comment> list) {
        Set<Long> ids = new HashSet<>();
        for (Comment c : list) {
            ids.add(c.getUserId());
        }
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, User> map = new HashMap<>();
        for (User u : userMapper.selectBatchIds(ids)) {
            map.put(u.getId(), u);
        }
        return map;
    }

    private CommentVO toVO(Comment c, User u) {
        CommentVO vo = new CommentVO();
        vo.setId(c.getId());
        vo.setArticleId(c.getArticleId());
        vo.setParentId(c.getParentId());
        vo.setContent(c.getContent());
        vo.setCreateTime(c.getCreateTime());
        if (u != null) {
            vo.setUserId(u.getId());
            vo.setUserNickname(nicknameOf(u));
            vo.setUserAvatar(u.getAvatar());
        }
        return vo;
    }

    private String nicknameOf(User u) {
        return u.getNickname() == null || u.getNickname().isBlank() ? u.getUsername() : u.getNickname();
    }
}
