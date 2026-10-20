package com.littlecase.myblog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 分类新增 / 修改请求体 */
@Data
public class CategoryDTO {

    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称最多50个字符")
    private String name;

    /** 排序（越小越靠前），为空按 0 处理 */
    private Integer sort;
}
