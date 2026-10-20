package com.littlecase.myblog.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章编辑回显（作者本人或管理员）
 * <p>
 * 与 {@link ArticleDetailVO} 的区别：返回 Markdown **原文** content 与 tagIds，
 * 供 Vditor 编辑器回填；详情接口返回的是渲染后的 contentHtml。
 */
@Data
public class ArticleEditVO {
    private Long id;
    private String title;
    private String summary;
    private String content;
    private String cover;
    private Long categoryId;
    private List<Long> tagIds;
    private Integer status;
    private String statusDesc;
    private String rejectReason;
    private LocalDateTime publishTime;
}
