import http from '../http'

export const listRolesApi = () => http.get('/admin/roles')
