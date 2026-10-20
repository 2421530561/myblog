import request from './request'

export interface LikeResult {
  liked: boolean
  count: number
}

/** 切换点赞状态（需登录），返回最新状态与点赞数 */
export function toggleLike(articleId: number) {
  return request.post<never, LikeResult>(`/like/article/${articleId}`)
}

/** 当前登录用户是否已点赞 */
export function getLikeStatus(articleId: number) {
  return request.get<never, { liked: boolean }>(`/like/article/${articleId}/status`)
}
