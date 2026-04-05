import http from './http'

export const uploadFileApi = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  // 不显式设置 Content-Type，交由浏览器附带 boundary
  return http.post('/files/upload', formData)
}

