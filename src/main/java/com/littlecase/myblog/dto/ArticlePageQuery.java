package com.littlecase.myblog.dto;
import lombok.Data;

@Data
public class ArticlePageQuery {
    private Long categoryId;
    private Long tagId;
    private String keyword;
    private int page = 1;
    private int pagesize = 10;
    
}
