import http from './http'

export const loginApi = (payload) => http.post('/auth/login', payload)
export const registerApi = (payload) => http.post('/auth/register', payload)
export const verifyEmailApi = (token) => http.get('/auth/verify-email', { params: { token } })
export const profileApi = () => http.get('/auth/profile')
export const updateProfileApi = (payload) => http.patch('/auth/profile', payload)
export const logoutApi = () => http.post('/auth/logout')
