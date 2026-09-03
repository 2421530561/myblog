import request from './request'

export interface Tag {
  id: number
  name: string
  createTime: string
}

export function getTagList() {
  return request.get('/tag/list')
}

export function createTag(name: string) {
  return request.post('/tag', null, { params: { name } })
}

export function deleteTag(id: number) {
  return request.delete(`/tag/${id}`)
}