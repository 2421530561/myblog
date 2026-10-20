package com.littlecase.myblog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.littlecase.myblog.dto.ArticleDTO;
import com.littlecase.myblog.vo.ArticleDetailVO;
import com.littlecase.myblog.vo.ArticleEditVO;
import com.littlecase.myblog.vo.ArticlePageItemVO;

public interface ArticleService {

    // ==================== 前台 ====================

    /** 前台：已发布文章分页（可按分类 / 标签 / 关键词筛选） */
    IPage<ArticlePageItemVO> getPublishedPage(Long categoryId, Long tagId, String keyword, int page, int size);

    /** 前台：文章详情（仅已发布可见，阅读量 +1） */
    ArticleDetailVO getDetail(Long id, Long currentUserId);

    // ==================== 写作（登录用户） ====================

    /** 编辑回显：返回 Markdown 原文与标签 ID 列表（作者本人或管理员） */
    ArticleEditVO getEditData(Long id, Long userId);

    /** 我的文章：按状态筛选（null=全部） */
    IPage<ArticlePageItemVO> getMyPage(Long userId, Integer status, int page, int size);

    /** 新建文章：action 0=存草稿 / 1=提交审核 / 2=直接发布（仅管理员） */
    Long create(ArticleDTO dto, Long userId);

    /** 编辑文章（作者本人或管理员） */
    void update(Long id, ArticleDTO dto, Long userId);

    /** 提交审核：草稿 / 已驳回 → 待审核 */
    void submit(Long id, Long userId);

    /** 删除文章（逻辑删除，作者本人或管理员） */
    void delete(Long id, Long userId);

    // ==================== 后台审核（仅 ADMIN） ====================

    /** 后台：全量文章分页（含草稿/待审核/已驳回） */
    IPage<ArticlePageItemVO> getAdminPage(Integer status, Long userId, String keyword, int page, int size);

    /** 审核通过并发布：待审核 → 已发布（生成 content_html、记录发布时间） */
    void adminPublish(Long id);

    /** 驳回：待审核 → 已驳回（记录驳回原因，作者可见） */
    void adminReject(Long id, String reason);

    /** 下架：已发布 → 草稿 */
    void adminOffline(Long id);
}
