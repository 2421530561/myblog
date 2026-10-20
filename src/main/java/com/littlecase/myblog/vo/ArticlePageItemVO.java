package com.littlecase.myblog.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章列表项（前台首页 / 分类 / 标签 / 我的文章 / 后台管理共用）
 */
@Data
public class ArticlePageItemVO {
    private Long id;
    private String title;
    private String summary;
    private String cover;
    private String authorName;
    private String authorAvatar;
    private Long categoryId;
    private String categoryName;
    private List<String> tags;
    private Integer viewCount;
    private Integer likeCount;
    private Integer isTop;
    private LocalDateTime publishTime;
    private LocalDateTime createTime;
    /** 文章状态码：0=草稿 1=待审核 2=已发布 3=已驳回 */
    private Integer status;
    /** 状态中文描述 */
    private String statusDesc;
    /** 驳回原因（状态为已驳回时返回，作者可见） */
    private String rejectReason;
}
