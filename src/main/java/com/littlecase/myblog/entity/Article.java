package com.littlecase.myblog.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("article")
public class Article {
    @TableId(type=IdType.AUTO)
    private Long id;
    private Long userId;
    private Long categoryId;
    private String title;
    private String summary;
    private String content;
    private String contentHtml;
    private String cover;
    private Integer status;
    private String rejectReason;
    private Integer viewCount;
    private Integer likeCount;
    private Integer isTop;
    private LocalDateTime publishTime;

    @TableField(fill=FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill=FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;

    //非数据库字段(关联查询用)
    @TableField(exist = false)
    private String authorName;
    @TableField(exist = false)
    private String categoryName;
    @TableField(exist = false)
    private String authorAvatar;

}
