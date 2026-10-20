package com.littlecase.myblog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 审核驳回请求体：{ "reason": "..." }（PRD 5.1 驳回需填写原因） */
@Data
public class ArticleRejectDTO {

    @NotBlank(message = "驳回原因不能为空")
    @Size(max = 500, message = "驳回原因最多500个字符")
    private String reason;
}
