import http from '../http'

export const listAdminLogsApi = (params) => http.get('/admin/logs', { params })
