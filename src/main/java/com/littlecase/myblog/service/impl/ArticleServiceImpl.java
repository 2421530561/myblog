package com.littlecase.myblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.littlecase.myblog.common.BusinessException;
import com.littlecase.myblog.dto.ArticleDTO;
import com.littlecase.myblog.entity.Article;
import com.littlecase.myblog.entity.ArticleTag;
import com.littlecase.myblog.entity.Tag;
import com.littlecase.myblog.mapper.ArticleMapper;
import com.littlecase.myblog.mapper.ArticleTagMapper;
import com.littlecase.myblog.mapper.TagMapper;
import com.littlecase.myblog.service.ArticleService;
import com.littlecase.myblog.vo.ArticleDetailVO;
import com.littlecase.myblog.vo.ArticlePageItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleMapper articleMapper;
    private final ArticleTagMapper articleTagMapper;
    private final TagMapper tagMapper;

   
}