import http from './http'

export const listBoardsApi = () => http.get('/boards')
