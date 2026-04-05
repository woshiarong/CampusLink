import axios from 'axios'

const http = axios.create({
  baseURL: '/api',
  timeout: 10000
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('campus-forum-token')
  if (token) {
    config.headers.Authorization = token
  }
  return config
})

http.interceptors.response.use(
  (response) => {
    const body = response.data
    if (body.code !== 0) {
      // token 无效时自动清理本地状态并跳转登录
      if (body.message && body.message.includes('token 无效')) {
        localStorage.removeItem('campus-forum-token')
        localStorage.removeItem('campus-forum-user')
        // 简单提示，实际项目中可以用全局消息组件替代
        alert('登录已失效，请重新登录。')
        window.location.href = '/login'
      }
      return Promise.reject(new Error(body.message || '请求失败'))
    }
    return body.data
  },
  (error) => Promise.reject(error)
)

export default http
