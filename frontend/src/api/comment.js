import http from './http'

export const listCommentsApi = (postId) => http.get(`/comments/post/${postId}`)
export const createCommentApi = (payload) => http.post('/comments', payload)
