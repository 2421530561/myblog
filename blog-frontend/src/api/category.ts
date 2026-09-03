import request from './request'

export interface Category {
  id: number
  name: string
  sort: number
  createTime: string
}

export function getCategoryList() {
  return request.get('/category/list')
}

export function createCategory(name: string, sort?: number) {
  return request.post('/category', null, { params: { name, sort } })
}

export function updateCategory(id: number, name?: string, sort?: number) {
  return request.put(`/category/${id}`, null, { params: { name, sort } })
}

export function deleteCategory(id: number) {
  return request.delete(`/category/${id}`)
}