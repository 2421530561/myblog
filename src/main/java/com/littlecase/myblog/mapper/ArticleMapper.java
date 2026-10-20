package com.littlecase.myblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.littlecase.myblog.entity.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {

    /**
     * 分页查询已发布文章（前台首页/分类/标签/关键词用）
     * 关联查询作者昵称、头像、分类名称
     */
    IPage<Article> selectPublishedPage(Page<Article> page,
                                      @Param("categoryId") Long categoryId,
                                      @Param("tagId") Long tagId,
                                      @Param("keyword") String keyword);

    /**
     * 分页查询全量文章（后台管理，含草稿/待审核/已驳回）
     * 关联查询作者昵称、头像、分类名称
     */
    IPage<Article> selectAdminPage(Page<Article> page,
                                   @Param("status") Integer status,
                                   @Param("userId") Long userId,
                                   @Param("keyword") String keyword);

    /**
     * 阅读量定时落库：累加 Redis 增量到 DB（M3，配合 ViewCountSyncTask）
     */
    int incViewAndReset(@Param("id") Long id, @Param("delta") long delta);
}