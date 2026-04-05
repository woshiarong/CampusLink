import http from '../http'

export const dashboardApi = () => http.get('/admin/statistics/dashboard')
