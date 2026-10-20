package com.littlecase.myblog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 标签新增 / 重命名请求体 */
@Data
public class TagDTO {

    @NotBlank(message = "标签名称不能为空")
    @Size(max = 50, message = "标签名称最多50个字符")
    private String name;
}
