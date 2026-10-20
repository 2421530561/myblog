import request from './request'

export interface Tag {
  id: number
  name: string
  createTime: string
}

export function getTagList() {
  return request.get<never, Tag[]>('/tag/list')
}

export function createTag(name: string) {
  return request.post<never, Tag>('/tag', { name })
}

/** 重命名标签（PRD 4.3 标签管理 CRUD） */
export function updateTag(id: number, name: string) {
  return request.put<never, void>(`/tag/${id}`, { name })
}

export function deleteTag(id: number) {
  return request.delete<never, void>(`/tag/${id}`)
}
