import http from './http'

export const listPostsApi = (params) => http.get('/posts', { params })
export const getPostDetailApi = (postId) => http.get(`/posts/${postId}`)
export const listMyPostsApi = (params) => http.get('/posts/mine', { params })
export const createPostApi = (payload) => http.post('/posts', payload)
export const submitPostApi = (postId) => http.post(`/posts/${postId}/submit`)
export const interactPostApi = (postId) => http.post(`/posts/${postId}/interact`)
export const likePostApi = (postId) => http.post(`/posts/${postId}/like`)
export const favoritePostApi = (postId) => http.post(`/posts/${postId}/favorite`)
export const updatePostApi = (postId, payload) => http.put(`/posts/${postId}`, payload)
export const deleteMyPostApi = (postId) => http.delete(`/posts/${postId}`)
export const reportPostApi = (postId, payload) => http.post(`/posts/${postId}/report`, payload)
