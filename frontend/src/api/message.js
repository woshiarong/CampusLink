import http from './http'

export const listMessagesApi = () => http.get('/messages')
export const unreadCountApi = () => http.get('/messages/unread-count')
export const markMessageReadApi = (id) => http.put(`/messages/${id}/read`)
export const markAllMessagesReadApi = () => http.put('/messages/read-all')

