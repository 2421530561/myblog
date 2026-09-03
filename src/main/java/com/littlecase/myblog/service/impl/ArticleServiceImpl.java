package com.littlecase.myblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.littlecase.myblog.common.BusinessException;
import com.littlecase.myblog.dto.ArticleDTO;
import com.littlecase.myblog.entity.Article;
import com.littlecase.myblog.entity.ArticleTag;
import com.littlecase.myblog.entity.Tag;
import com.littlecase.myblog.mapper.ArticleMapper;
import com.littlecase.myblog.mapper.ArticleTagMapper;
import com.littlecase.myblog.mapper.TagMapper;
import com.littlecase.myblog.service.ArticleService;
import com.littlecase.myblog.vo.ArticleDetailVO;
import com.littlecase.myblog.vo.ArticlePageItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleMapper articleMapper;
    private final ArticleTagMapper articleTagMapper;
    private final TagMapper tagMapper;

    @Override
    public IPage<ArticlePageItemVO> getPublishedPage(Long categoryId, Long tagId, String keyword, int page, int size) {
        Page<Article> pg = new Page<>(page, size);
        IPage<Article> result = articleMapper.selectPublishedPage(pg, categoryId, tagId, keyword);

        IPage<ArticlePageItemVO> voPage = new Page<>(page, size);
        voPage.setTotal(result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::toPageItem).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public ArticleDetailVO getDetail(Long id, Long currentUserId)
    {
        Article article = articleMapper.selectById(id);
        if(article ==null ||article.getDeleted()==1 || article.getStatus()!=2)
        {
            throw new BusinessException("文章不存在或已被删除");
        }

        //阅读量 +1
        article.setViewCount(article.getViewCount()+1);
        articleMapper.updateById(article);

        ArticleDetailVO vo = new ArticleDetailVO();
        vo.setId(article.getId());
        vo.setTitle(article.getTitle());
        vo.setContentHtml(article.getContentHtml());
        vo.setCover(article.getCover());
        vo.setViewCount(article.getViewCount());
        vo.setLikeCount(article.getLikeCount());
        vo.setPublishTime(article.getPublishTime());
        vo.setCreateTime(article.getCreateTime());
        vo.setUpdateTime(article.getUpdateTime());

        // 查询标签
        List<ArticleTag> atList = articleTagMapper.selectList(
                new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getArticleId, id));
        List<Long> tagIds = atList.stream().map(ArticleTag::getTagId).collect(Collectors.toList());
        if (!tagIds.isEmpty()) {
            List<Tag> tags = tagMapper.selectBatchIds(tagIds);
            vo.setTags(tags.stream().map(Tag::getName).collect(Collectors.toList()));
        } else {
            vo.setTags(new ArrayList<>());
        }

        return vo;
    }

    @Override
    @Transactional
    public Long create(ArticleDTO dto, Long userId)
    {
        Article article =new Article();
        article.setUserId(userId);
        article.setTitle(dto.getTitle());
        article.setSummary(dto.getSummary());
        article.setContent(dto.getContent());
        article.setCover(dto.getCover());
        article.setCategoryId(dto.getCategoryId());

        if(dto.getAction()!=null &&dto.getAction()==1)
        {
            article.setStatus(1);//Pending 待审核
        }
        else{
            article.setStatus(0);//Draft 草稿
        }

        articleMapper.insert(article);

        //保存标签关联
        if(dto.getTagIds()!=null && !dto.getTagIds().isEmpty())
        {
            for(Long tagId : dto.getTagIds())
            {
                ArticleTag at =new ArticleTag();
                at.setArticleId(article.getId());
                at.setTagId(tagId);
                articleTagMapper.insert(at);
            }
        }
        return article.getId();
    }

    @Override
    @Transactional
    public void update(Long id, ArticleDTO dto, Long userId) {
        Article article = articleMapper.selectById(id);
        if (article == null || article.getDeleted() == 1) {
            throw new BusinessException("文章不存在");
        }
        if (!article.getUserId().equals(userId)) {
            throw new BusinessException("只能编辑自己的文章");
        }

        article.setTitle(dto.getTitle());
        article.setSummary(dto.getSummary());
        article.setContent(dto.getContent());
        article.setCover(dto.getCover());
        article.setCategoryId(dto.getCategoryId());

        // 编辑后重置为草稿
        article.setStatus(0);
        article.setPublishTime(null);

        articleMapper.updateById(article);

        // 重新保存标签关联
        articleTagMapper.delete(new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getArticleId, id));
        if (dto.getTagIds() != null && !dto.getTagIds().isEmpty()) {
            for (Long tagId : dto.getTagIds()) {
                ArticleTag at = new ArticleTag();
                at.setArticleId(id);
                at.setTagId(tagId);
                articleTagMapper.insert(at);
            }
        }
    }

    @Override
    public void submit(Long id, Long userId)
    {
        Article article=articleMapper.selectById(id);
        if(article==null || article.getDeleted()==1 )
        {
            throw new BusinessException("文章不存在或已被删除");
        }
        if(!article.getUserId().equals(userId))
        {
            throw new BusinessException("只能提交自己的文章");
        }
        if(article.getStatus()!=1)
        {
            throw new BusinessException("文章正审核");
        }
        article.setStatus(1);//文章状态设置为审核
        articleMapper.updateById(article);
    }

    @Override
    public void delete(Long id, Long userId)
    {
                Article article = articleMapper.selectById(id);
        if (article == null || article.getDeleted() == 1) {
            throw new BusinessException("文章不存在");
        }
        if (!article.getUserId().equals(userId)) {
            throw new BusinessException("只能删除自己的文章");
        }
        articleMapper.deleteById(id);
    }

    private ArticlePageItemVO toPageItem(Article article) {
    ArticlePageItemVO vo = new ArticlePageItemVO();
        vo.setId(article.getId());
        vo.setTitle(article.getTitle());
        vo.setSummary(article.getSummary());
        vo.setCover(article.getCover());
        vo.setAuthorName(article.getAuthorName());
        vo.setAuthorAvatar(article.getAuthorAvatar());
        vo.setCategoryName(article.getCategoryName());
        vo.setViewCount(article.getViewCount());
        vo.setLikeCount(article.getLikeCount());
        vo.setIsTop(article.getIsTop());
        vo.setPublishTime(article.getPublishTime());
        return vo;
    }
   
}