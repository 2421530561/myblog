import request from './request'
import type { PageResult } from './article'

/** 评论（楼中楼），与后端 CommentVO 对应 */
export interface CommentVO {
  id: number
  articleId: number
  parentId: number | null
  userId: number
  userNickname: string
  userAvatar?: string
  /** 被回复人昵称（仅回复有值） */
  replyNickname?: string
  content: string
  createTime: string
  children?: CommentVO[]
}

/** 文章评论分页（按顶层评论分页，回复挂在 children 下） */
export function getCommentPage(articleId: number, page = 1, size = 10) {
  return request.get<never, PageResult<CommentVO>>('/comment/page', {
    params: { articleId, page, size },
  })
}

/** 发表评论 / 回复（parentId 为空表示顶层评论） */
export function addComment(data: { articleId: number; parentId?: number | null; content: string }) {
  return request.post<never, number>('/comment', data)
}

/** 删除评论（作者本人或管理员，后端会连带删除其回复） */
export function deleteComment(id: number) {
  return request.delete<never, void>(`/comment/${id}`)
}
