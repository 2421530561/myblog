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
     * 分页查询已发布文章（前台首页/分类/标签列表用）
     * 关联查询作者昵称、分类名称
     */
    IPage<Article> selectPublishedPage(Page<Article> page,@Param("categoryId") Long categoryId,
    @Param("tagId") Long tagId,@Param("keyword") String keyword
);
    


}