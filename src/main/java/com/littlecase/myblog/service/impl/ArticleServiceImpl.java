package com.littlecase.myblog.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.littlecase.myblog.common.ArticleStatus;
import com.littlecase.myblog.common.BusinessException;
import com.littlecase.myblog.common.HtmlSanitizer;
import com.littlecase.myblog.common.MarkdownUtil;
import com.littlecase.myblog.dto.ArticleDTO;
import com.littlecase.myblog.entity.Article;
import com.littlecase.myblog.entity.ArticleTag;
import com.littlecase.myblog.entity.Category;
import com.littlecase.myblog.entity.Tag;
import com.littlecase.myblog.entity.User;
import com.littlecase.myblog.mapper.ArticleMapper;
import com.littlecase.myblog.mapper.ArticleTagMapper;
import com.littlecase.myblog.mapper.CategoryMapper;
import com.littlecase.myblog.mapper.TagMapper;
import com.littlecase.myblog.mapper.UserMapper;
import com.littlecase.myblog.service.ArticleLikeService;
import com.littlecase.myblog.service.ArticleService;
import com.littlecase.myblog.vo.ArticleDetailVO;
import com.littlecase.myblog.vo.ArticleEditVO;
import com.littlecase.myblog.vo.ArticlePageItemVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 文章服务实现（M2 文章链路）
 * <p>
 * 覆盖：前台列表/详情、分类标签关联、草稿→提交审核→审核发布/驳回状态机、图片配图（Controller 负责落盘）。
 * 状态机见 {@link ArticleStatus}。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleMapper articleMapper;
    private final ArticleTagMapper articleTagMapper;
    private final TagMapper tagMapper;
    private final CategoryMapper categoryMapper;
    private final UserMapper userMapper;
    /** M3：详情页展示点赞数 / 当前用户点赞状态 */
    private final ArticleLikeService articleLikeService;

    // M3 阅读量：Redis 存增量，DB 存存量（PRD 5.3）
    private static final String VIEW_KEY = "blog:view:";
    private final StringRedisTemplate redisTemplate;

    /**
     * 阅读量自增：只写 Redis，由 {@code ViewCountSyncTask} 每 5 分钟回写 MySQL。
     * Redis 不可用时降级为直接 +1 写库，保证主链路不因缓存故障而失败（PRD 6 健壮性）。
     */
    private void incViewCount(Long articleId) {
        try {
            redisTemplate.opsForValue().increment(VIEW_KEY + articleId, 1L);
        } catch (Exception e) {
            log.warn("Redis 阅读量自增失败，降级为直接写库 articleId={}", articleId, e);
            articleMapper.incViewAndReset(articleId, 1L);
        }
    }

    /** 计算阅读量展示值 = DB 存量 + Redis 增量 */
    private long viewCount(Long articleId, int dbBase) {
        try {
            String delta = redisTemplate.opsForValue().get(VIEW_KEY + articleId);
            long inc = delta == null ? 0 : Long.parseLong(delta);
            return Math.max(0, dbBase + inc);
        } catch (Exception e) {
            log.warn("Redis 阅读量读取失败，仅返回 DB 存量 articleId={}", articleId, e);
            return dbBase;
        }
    }

    // ==================================================================
    // 前台
    // ==================================================================

    @Override
    public IPage<ArticlePageItemVO> getPublishedPage(Long categoryId, Long tagId, String keyword, int page, int size) {
        Page<Article> pg = new Page<>(page, size);
        IPage<Article> result = articleMapper.selectPublishedPage(pg, categoryId, tagId, blankToNull(keyword));
        return toVoPage(result, page, size);
    }

    @Override
    public ArticleDetailVO getDetail(Long id, Long currentUserId) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException(404, "文章不存在或已被删除");
        }
        if (!ArticleStatus.PUBLISHED.is(article.getStatus())) {
            // 未发布文章不允许从前台直达详情（作者请走 /api/article/{id}/edit）
            throw new BusinessException(404, "文章不存在或未发布");
        }

        // 阅读量：Redis 增量（M3，替代直接写 DB，由定时任务 ViewCountSyncTask 回写）
        incViewCount(id);

        ArticleDetailVO vo = new ArticleDetailVO();
        vo.setId(article.getId());
        vo.setTitle(article.getTitle());
        vo.setContentHtml(article.getContentHtml());
        vo.setCover(article.getCover());
        vo.setViewCount((int) viewCount(id, article.getViewCount() == null ? 0 : article.getViewCount()));
        vo.setLikeCount((int) articleLikeService.count(id));
        vo.setLiked(articleLikeService.isLiked(id, currentUserId));
        vo.setPublishTime(article.getPublishTime());
        vo.setCreateTime(article.getCreateTime());
        vo.setUpdateTime(article.getUpdateTime());

        User author = userMapper.selectById(article.getUserId());
        if (author != null) {
            vo.setAuthorName(displayName(author));
            vo.setAuthorAvatar(author.getAvatar());
        }
        vo.setCategoryName(categoryNameOf(article.getCategoryId()));
        vo.setTags(tagNamesOf(id));
        return vo;
    }

    // ==================================================================
    // 写作
    // ==================================================================

    @Override
    public ArticleEditVO getEditData(Long id, Long userId) {
        Article article = requireArticle(id);
        checkOwnerOrAdmin(article, userId);

        ArticleEditVO vo = new ArticleEditVO();
        vo.setId(article.getId());
        vo.setTitle(article.getTitle());
        vo.setSummary(article.getSummary());
        vo.setContent(article.getContent());
        vo.setCover(article.getCover());
        vo.setCategoryId(article.getCategoryId());
        vo.setTagIds(articleTagMapper.selectList(
                        new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getArticleId, id))
                .stream().map(ArticleTag::getTagId).collect(Collectors.toList()));
        vo.setStatus(article.getStatus());
        vo.setStatusDesc(ArticleStatus.descOf(article.getStatus()));
        vo.setRejectReason(article.getRejectReason());
        vo.setPublishTime(article.getPublishTime());
        return vo;
    }

    @Override
    public IPage<ArticlePageItemVO> getMyPage(Long userId, Integer status, int page, int size) {
        Page<Article> pg = new Page<>(page, size);
        IPage<Article> result = articleMapper.selectPage(pg, new LambdaQueryWrapper<Article>()
                .eq(Article::getUserId, userId)
                .eq(status != null, Article::getStatus, status)
                .orderByDesc(Article::getUpdateTime));

        IPage<ArticlePageItemVO> voPage = toVoPage(result, page, size);
        User self = userMapper.selectById(userId);
        if (self != null) {
            voPage.getRecords().forEach(vo -> {
                vo.setAuthorName(displayName(self));
                vo.setAuthorAvatar(self.getAvatar());
            });
        }
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(ArticleDTO dto, Long userId) {
        validateRelations(dto);
        int action = dto.getAction() == null ? 0 : dto.getAction();
        if (action == 2 && !StpUtil.hasRole("ADMIN")) {
            throw new BusinessException(403, "只有管理员可以直接发布文章");
        }

        Article article = new Article();
        article.setUserId(userId);
        applyDto(article, dto);
        article.setViewCount(0);
        article.setLikeCount(0);
        article.setIsTop(0);

        if (action == 1) {
            article.setStatus(ArticleStatus.PENDING.getCode());
        } else if (action == 2) {
            publishNow(article);
        } else {
            article.setStatus(ArticleStatus.DRAFT.getCode());
        }

        articleMapper.insert(article);
        saveTagLinks(article.getId(), dto.getTagIds());
        return article.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, ArticleDTO dto, Long userId) {
        Article article = requireArticle(id);
        checkOwnerOrAdmin(article, userId);
        validateRelations(dto);

        boolean admin = StpUtil.hasRole("ADMIN");
        boolean published = ArticleStatus.PUBLISHED.is(article.getStatus());
        if (published && !admin) {
            throw new BusinessException("已发布的文章需要先由管理员下架后才能编辑");
        }

        applyDto(article, dto);
        int action = dto.getAction() == null ? 0 : dto.getAction();

        if (published) {
            // 管理员编辑已发布文章：保持发布状态，同步刷新渲染结果
            article.setContentHtml(buildContentHtml(article.getContent()));
        } else if (action == 2) {
            if (!admin) {
                throw new BusinessException(403, "只有管理员可以直接发布文章");
            }
            publishNow(article);
        } else if (action == 1) {
            // 保存并提交审核
            article.setStatus(ArticleStatus.PENDING.getCode());
            article.setRejectReason(null);
        } else {
            // 普通编辑：回到草稿（无论此前是草稿/待审核/已驳回）
            article.setStatus(ArticleStatus.DRAFT.getCode());
            article.setRejectReason(null);
            article.setPublishTime(null);
        }

        articleMapper.updateById(article);

        // 覆盖式重建标签关联
        articleTagMapper.delete(new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getArticleId, id));
        saveTagLinks(id, dto.getTagIds());
    }

@Override
public void submit(Long id, Long userId) {
    Article article = articleMapper.selectById(id);
    if (article == null || article.getDeleted() == 1) {
        throw new BusinessException("文章不存在或已被删除");
    }
    if (!article.getUserId().equals(userId)) {
        throw new BusinessException("只能提交自己的文章");
    }
    // 只有草稿(0)才能提交审核；待审核/已发布/已驳回均不允许
    if (!ArticleStatus.DRAFT.is(article.getStatus())) {
        throw new BusinessException("仅草稿状态的文章可以提交审核");
    }
    article.setStatus(ArticleStatus.PENDING.getCode()); // 待审核
    articleMapper.updateById(article);
}

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id, Long userId) {
        Article article = requireArticle(id);
        checkOwnerOrAdmin(article, userId);
        // 标签关联直接物理清理，避免残留脏数据（评论/点赞在 M3 一并逻辑删除）
        articleTagMapper.delete(new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getArticleId, id));
        articleMapper.deleteById(id);
    }

    // ==================================================================
    // 后台审核
    // ==================================================================

    @Override
    public IPage<ArticlePageItemVO> getAdminPage(Integer status, Long userId, String keyword, int page, int size) {
        Page<Article> pg = new Page<>(page, size);
        IPage<Article> result = articleMapper.selectAdminPage(pg, status, userId, blankToNull(keyword));
        return toVoPage(result, page, size);
    }

    @Override
    public void adminPublish(Long id) {
        Article article = requireArticle(id);
        if (ArticleStatus.PUBLISHED.is(article.getStatus())) {
            throw new BusinessException("文章已是发布状态");
        }
        // 管理员拥有全部权限：待审核可直接审核通过，草稿/已驳回也可直接发布（PRD 假设 5）
        publishNow(article);
        articleMapper.updateById(article);
    }

    @Override
    public void adminReject(Long id, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new BusinessException("驳回原因不能为空");
        }
        Article article = requireArticle(id);
        if (!ArticleStatus.PENDING.is(article.getStatus())) {
            throw new BusinessException("只有待审核的文章才能驳回，当前状态：" + ArticleStatus.descOf(article.getStatus()));
        }
        article.setStatus(ArticleStatus.REJECTED.getCode());
        article.setRejectReason(reason.trim());
        articleMapper.updateById(article);
    }

    @Override
    public void adminOffline(Long id) {
        Article article = requireArticle(id);
        if (!ArticleStatus.PUBLISHED.is(article.getStatus())) {
            throw new BusinessException("只有已发布的文章才能下架，当前状态：" + ArticleStatus.descOf(article.getStatus()));
        }
        // 下架：回到草稿，清空发布时间（PRD 5.1）
        article.setStatus(ArticleStatus.DRAFT.getCode());
        article.setPublishTime(null);
        articleMapper.updateById(article);
    }

    // ==================================================================
    // 内部工具
    // ==================================================================

    /** 发布：状态置为已发布，生成 content_html（HTML 渲染 + XSS 清洗），记录发布时间 */
    private void publishNow(Article article) {
        article.setStatus(ArticleStatus.PUBLISHED.getCode());
        article.setContentHtml(buildContentHtml(article.getContent()));
        article.setRejectReason(null);
        if (article.getPublishTime() == null) {
            article.setPublishTime(LocalDateTime.now());
        }
    }

    /** Markdown → HTML → 白名单清洗（PRD 假设 1 + 6 安全） */
    private String buildContentHtml(String markdown) {
        return HtmlSanitizer.clean(MarkdownUtil.render(markdown));
    }

    private void applyDto(Article article, ArticleDTO dto) {
        article.setTitle(dto.getTitle().trim());
        article.setSummary(MarkdownUtil.resolveSummary(dto.getSummary(), dto.getContent()));
        article.setContent(dto.getContent());
        article.setCover(blankToNull(dto.getCover()));
        article.setCategoryId(dto.getCategoryId());
    }

    private Article requireArticle(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException(404, "文章不存在或已被删除");
        }
        return article;
    }

    /** 仅作者本人或管理员可操作 */
    private void checkOwnerOrAdmin(Article article, Long userId) {
        if (!article.getUserId().equals(userId) && !StpUtil.hasRole("ADMIN")) {
            throw new BusinessException(403, "只能操作自己的文章");
        }
    }

    private void validateRelations(ArticleDTO dto) {
        if (dto.getCategoryId() != null && categoryMapper.selectById(dto.getCategoryId()) == null) {
            throw new BusinessException("所选分类不存在");
        }
        List<Long> tagIds = dto.getTagIds();
        if (tagIds != null && !tagIds.isEmpty()) {
            List<Long> distinct = tagIds.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
            if (!distinct.isEmpty() && tagMapper.selectBatchIds(distinct).size() != distinct.size()) {
                throw new BusinessException("存在无效的标签，请重新选择");
            }
        }
    }

    private void saveTagLinks(Long articleId, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }
        for (Long tagId : tagIds.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList())) {
            ArticleTag link = new ArticleTag();
            link.setArticleId(articleId);
            link.setTagId(tagId);
            articleTagMapper.insert(link);
        }
    }

    private IPage<ArticlePageItemVO> toVoPage(IPage<Article> result, long page, long size) {
        List<ArticlePageItemVO> records = result.getRecords().stream()
                .map(this::toItem)
                .collect(Collectors.toList());
        fillTags(records);
        fillCategoryNames(records);

        Page<ArticlePageItemVO> voPage = new Page<>(page, size);
        voPage.setTotal(result.getTotal());
        voPage.setRecords(records);
        return voPage;
    }

    private ArticlePageItemVO toItem(Article article) {
        ArticlePageItemVO vo = new ArticlePageItemVO();
        vo.setId(article.getId());
        vo.setTitle(article.getTitle());
        vo.setSummary(article.getSummary());
        vo.setCover(article.getCover());
        vo.setAuthorName(article.getAuthorName());
        vo.setAuthorAvatar(article.getAuthorAvatar());
        vo.setCategoryId(article.getCategoryId());
        vo.setCategoryName(article.getCategoryName());
        vo.setViewCount(article.getViewCount());
        vo.setLikeCount(article.getLikeCount());
        vo.setIsTop(article.getIsTop());
        vo.setPublishTime(article.getPublishTime());
        vo.setCreateTime(article.getCreateTime());
        vo.setStatus(article.getStatus());
        vo.setStatusDesc(ArticleStatus.descOf(article.getStatus()));
        vo.setRejectReason(article.getRejectReason());
        return vo;
    }

    /** 批量回填标签名，避免列表 N+1 查询 */
    private void fillTags(List<ArticlePageItemVO> records) {
        if (records.isEmpty()) {
            return;
        }
        List<Long> articleIds = records.stream().map(ArticlePageItemVO::getId)
                .filter(Objects::nonNull).collect(Collectors.toList());
        if (articleIds.isEmpty()) {
            records.forEach(vo -> vo.setTags(new ArrayList<>()));
            return;
        }
        List<ArticleTag> links = articleTagMapper.selectList(
                new LambdaQueryWrapper<ArticleTag>().in(ArticleTag::getArticleId, articleIds));
        if (links.isEmpty()) {
            records.forEach(vo -> vo.setTags(new ArrayList<>()));
            return;
        }
        List<Long> tagIds = links.stream().map(ArticleTag::getTagId).distinct().collect(Collectors.toList());
        Map<Long, String> tagNameMap = tagMapper.selectBatchIds(tagIds).stream()
                .collect(Collectors.toMap(Tag::getId, Tag::getName, (a, b) -> a));

        Map<Long, List<String>> articleTagMap = new HashMap<>();
        for (ArticleTag link : links) {
            String name = tagNameMap.get(link.getTagId());
            if (name != null) {
                articleTagMap.computeIfAbsent(link.getArticleId(), k -> new ArrayList<>()).add(name);
            }
        }
        records.forEach(vo -> vo.setTags(articleTagMap.getOrDefault(vo.getId(), new ArrayList<>())));
    }

    /** 自定义 SQL（selectPage）没有 join 分类表，这里按需补名称 */
    private void fillCategoryNames(List<ArticlePageItemVO> records) {
        List<Long> ids = records.stream()
                .filter(vo -> vo.getCategoryName() == null && vo.getCategoryId() != null)
                .map(ArticlePageItemVO::getCategoryId)
                .distinct()
                .collect(Collectors.toList());
        if (ids.isEmpty()) {
            return;
        }
        Map<Long, String> nameMap = categoryMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(Category::getId, Category::getName, (a, b) -> a));
        records.forEach(vo -> {
            if (vo.getCategoryName() == null && vo.getCategoryId() != null) {
                vo.setCategoryName(nameMap.get(vo.getCategoryId()));
            }
        });
    }

    private List<String> tagNamesOf(Long articleId) {
        List<Long> tagIds = articleTagMapper.selectList(
                        new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getArticleId, articleId))
                .stream().map(ArticleTag::getTagId).collect(Collectors.toList());
        if (tagIds.isEmpty()) {
            return new ArrayList<>();
        }
        return tagMapper.selectBatchIds(tagIds).stream().map(Tag::getName).collect(Collectors.toList());
    }

    private String categoryNameOf(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        Category category = categoryMapper.selectById(categoryId);
        return category == null ? null : category.getName();
    }

    private String displayName(User user) {
        return user.getNickname() == null || user.getNickname().isBlank() ? user.getUsername() : user.getNickname();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}