import http from '../http'

export const listBoardsAdminApi = () => http.get('/admin/boards')
export const saveBoardApi = (data) => http.post('/admin/boards', data)
export const deleteBoardApi = (id) => http.delete(`/admin/boards/${id}`)
export const orderBoardsApi = (boardIds) => http.put('/admin/boards/order', boardIds)
