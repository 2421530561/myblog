import request from './request'

export interface ArticlePageParams {
  categoryId?: number
  tagId?: number
  keyword?: string
  page?: number
  size?: number
}

/** 文章状态码（与后端 ArticleStatus 一致，PRD 5.1） */
export const ArticleStatusMap: Record<number, { label: string; type: 'default' | 'info' | 'warning' | 'success' | 'error' }> = {
  0: { label: '草稿', type: 'default' },
  1: { label: '待审核', type: 'warning' },
  2: { label: '已发布', type: 'success' },
  3: { label: '已驳回', type: 'error' },
}

export interface ArticlePageItem {
  id: number
  title: string
  summary: string
  cover: string
  authorName: string
  authorAvatar: string
  categoryId: number | null
  categoryName: string | null
  tags: string[]
  viewCount: number
  likeCount: number
  isTop: number
  publishTime?: string
  createTime?: string
  status: number
  statusDesc: string
  rejectReason?: string
}

export interface ArticleDetail {
  id: number
  title: string
  contentHtml: string
  cover: string
  authorName: string
  authorAvatar: string
  categoryName: string
  tags: string[]
  viewCount: number
  likeCount: number
  liked: boolean
  publishTime: string
  createTime: string
  updateTime: string
}

/** 编辑回显：Markdown 原文 + 标签 ID（与详情接口区分） */
export interface ArticleEditData {
  id: number
  title: string
  summary: string
  content: string
  cover: string
  categoryId: number | null
  tagIds: number[]
  status: number
  statusDesc: string
  rejectReason?: string
  publishTime?: string
}

export interface ArticleDTO {
  title: string
  summary?: string
  content: string
  cover?: string
  categoryId?: number | null
  tagIds?: number[]
  /** 0=存草稿 1=提交审核 2=直接发布（仅管理员） */
  action?: number
}

export interface PageResult<T> {
  records: T[]
  total: number
  current: number
  size: number
}

/** 前台：已发布文章分页 */
export function getPublishedPage(params: ArticlePageParams) {
  return request.get<never, PageResult<ArticlePageItem>>('/article/page', { params })
}

/** 前台：文章详情（阅读量 +1） */
export function getDetail(id: number) {
  return request.get<never, ArticleDetail>(`/article/${id}`)
}

/** 我的文章（登录用户，status 为空查全部） */
export function getMyArticlePage(params: { status?: number; page?: number; size?: number }) {
  return request.get<never, PageResult<ArticlePageItem>>('/article/mine', { params })
}

/** 编辑回显（作者本人或管理员） */
export function getArticleForEdit(id: number) {
  return request.get<never, ArticleEditData>(`/article/${id}/edit`)
}

export function createArticle(data: ArticleDTO) {
  return request.post<never, number>('/article', data)
}

export function updateArticle(id: number, data: ArticleDTO) {
  return request.put<never, void>(`/article/${id}`, data)
}

export function submitArticle(id: number) {
  return request.post<never, void>(`/article/${id}/submit`)
}

export function deleteArticle(id: number) {
  return request.delete<never, void>(`/article/${id}`)
}

// ==================== 后台审核（仅 ADMIN） ====================

/** 后台：全量文章分页 */
export function getAdminArticlePage(params: {
  status?: number
  userId?: number
  keyword?: string
  page?: number
  size?: number
}) {
  return request.get<never, PageResult<ArticlePageItem>>('/admin/article/page', { params })
}

/** 审核通过并发布（待审核 → 已发布） */
export function publishArticle(id: number) {
  return request.post<never, void>(`/admin/article/${id}/publish`)
}

/** 驳回（待审核 → 已驳回） */
export function rejectArticle(id: number, reason: string) {
  return request.post<never, void>(`/admin/article/${id}/reject`, { reason })
}

/** 下架（已发布 → 草稿） */
export function offlineArticle(id: number) {
  return request.post<never, void>(`/admin/article/${id}/offline`)
}
