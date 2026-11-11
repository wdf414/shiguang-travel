import request from '@/utils/request'

export function login(data) {
  return request.post('/user/login', data)
}

export function searchScenicSpots(params) {
  return request.get('/scenic-spot/search', { params })
}

export function addRouteSpot(scenicSpotId) {
  return request.post('/user/route-spot/add', { scenicSpotId })
}

export function getRouteSpots(params) {
  return request.get('/user/route-spot/list', { params })
}

export function deleteRouteSpot(id) {
  return request.delete(`/user/route-spot/${id}`)
}

export function register(data) {
  return request.post('/user/register', data)
}

export function getUserInfo() {
  return request.get('/user/info')
}

export function updateUserInfo(data) {
  return request.put('/user/info', data)
}

export function updatePassword(data) {
  return request.put('/user/password', data)
}
