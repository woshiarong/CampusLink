import http from './http'

export const listReportsApi = (params) => http.get('/moderation/reports', { params })
export const reviewReportApi = (payload) => http.post('/moderation/reports/review', payload)
