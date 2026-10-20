import request from './request'

export interface Category {
  id: number
  name: string
  sort: number
  createTime: string
}

export function getCategoryList() {
  return request.get<never, Category[]>('/category/list')
}

export function createCategory(name: string, sort?: number) {
  return request.post<never, Category>('/category', { name, sort })
}

export function updateCategory(id: number, name?: string, sort?: number) {
  return request.put<never, void>(`/category/${id}`, { name, sort })
}

export function deleteCategory(id: number) {
  return request.delete<never, void>(`/category/${id}`)
}
