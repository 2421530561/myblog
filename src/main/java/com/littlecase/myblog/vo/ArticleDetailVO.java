package com.littlecase.myblog.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ArticleDetailVO {
    private Long id;
    private String title;
    private String contentHtml;
    private String cover;
    private String authorName;
    private String authorAvatar;
    private String categoryName;
    private List<String> tags;
    private Integer viewCount;
    private Integer likeCount;
    private Boolean liked;
    private LocalDateTime publishTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
}
