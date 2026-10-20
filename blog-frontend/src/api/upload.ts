import request from './request'

export interface UploadResult {
  url: string
}

/** 上传图片（jpg/png/webp/gif，≤5MB），返回 /upload/yyyyMMdd/uuid.ext */
export function uploadImage(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<never, UploadResult>('/upload/image', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}
