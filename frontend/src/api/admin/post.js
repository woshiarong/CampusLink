import http from '../http'

export const listPostsAdminApi = (params) => http.get('/admin/posts', { params })
export const pinPostApi = (id, pinned) => http.put(`/admin/posts/${id}/pin`, { pinned })
export const featurePostApi = (id, featured) => http.put(`/admin/posts/${id}/feature`, { featured })
export const deletePostAdminApi = (id) => http.delete(`/admin/posts/${id}`)
export const movePostApi = (id, boardId) => http.put(`/admin/posts/${id}/move`, { boardId })
export const approvePostApi = (id) => http.put(`/admin/posts/${id}/approve`)
