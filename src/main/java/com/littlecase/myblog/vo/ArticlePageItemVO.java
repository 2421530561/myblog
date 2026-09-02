package com.littlecase.myblog.vo;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ArticlePageItemVO {
    private Long id;
    private String title;
    private String summary;
    private String cover;
    private String authorName;
    private String authorAvatar;
    private String categoryName;
    private List<String> tags;
    private Integer viewCount;
    private Integer likeCount;
    private Integer isTop;
    private LocalDateTime publishTime;
    private String status;
    private String rejectReason;
}
