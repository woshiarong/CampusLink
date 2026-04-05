import http from './http'

export const listSettingsApi = () => http.get('/settings')
export const updateSettingApi = (payload) => http.put('/settings', payload)
export const publicSettingsApi = () => http.get('/settings/public')
