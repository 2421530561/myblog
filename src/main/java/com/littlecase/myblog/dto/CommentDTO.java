package com.littlecase.myblog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 发表评论请求体（M3）
 */
@Data
public class CommentDTO {

    @NotNull(message = "文章ID不能为空")
    private Long articleId;

    /** 被回复评论 ID，可空：NULL 表示顶层评论 */
    private Long parentId;

    @NotBlank(message = "评论内容不能为空")
    private String content;
}