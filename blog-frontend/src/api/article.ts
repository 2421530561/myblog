import request from './request'

export interface ArticlePageParams {
  categoryId?: number
  tagId?: number
  keyword?: string
  page?: number
  size?: number
}

export interface ArticlePageItem {
  id: number
  title: string
  summary: string
  cover: string
  authorName: string
  authorAvatar: string
  categoryName: string
  tags: string[]
  viewCount: number
  likeCount: number
  isTop: number
  publishTime: string
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

export interface ArticleDTO {
  title: string
  summary?: string
  content: string
  cover?: string
  categoryId?: number
  tagIds?: number[]
  action?: number // 0=草稿 1=提交审核
}

export function getPublishedPage(params: ArticlePageParams) {
  return request.get('/article/page', { params })
}

export function getDetail(id: number) {
  return request.get(`/article/${id}`)
}

export function createArticle(data: ArticleDTO) {
  return request.post('/article', data)
}

export function updateArticle(id: number, data: ArticleDTO) {
  return request.put(`/article/${id}`, data)
}

export function submitArticle(id: number) {
  return request.post(`/article/${id}/submit`)
}

export function deleteArticle(id: number) {
  return request.delete(`/article/${id}`)
}