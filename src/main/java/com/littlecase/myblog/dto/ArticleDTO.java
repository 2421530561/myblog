package com.littlecase.myblog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class ArticleDTO {

    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题最多200个字符")
    private String title;

    @Size(max = 500, message = "摘要最多500个字符")
    private String summary;

    @NotBlank(message = "正文内容不能为空")
    private String content;

    private String cover;

    private Long categoryId;

    private List<Long> tagIds;

    /**
     * 本次保存的动作（对应 PRD 5.1 状态机入口）：
     * 0 = 保存草稿（DRAFT）；1 = 提交审核（PENDING）；2 = 直接发布（仅 ADMIN，跳过审核）
     */
    private Integer action;
}
