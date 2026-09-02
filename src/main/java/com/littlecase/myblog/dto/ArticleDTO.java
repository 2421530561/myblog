package com.littlecase.myblog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class ArticleDTO{
    @NotBlank(message = "标题不能为空")
    @Size(max = 50, message = "标题最多50个字符")
    private String title;
    private String summary;
    @NotBlank(message = "正文内容不能为空")
    private String content;
    private String cover;
    private Long categoryId;
    private List<Long> tagIds;
    /* 操作类型 0：草稿 1：提交审核 */
    private Integer action;

}