import http from './http'

export const listFavoritesApi = () => http.get('/favorites')
