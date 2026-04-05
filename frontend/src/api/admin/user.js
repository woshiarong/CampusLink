import http from '../http'

export const listUsersApi = (params) => http.get('/admin/users', { params })
export const updateUserApi = (data) => http.put('/admin/users', data)
export const resetPasswordApi = (data) => http.post('/admin/users/reset-password', data)
