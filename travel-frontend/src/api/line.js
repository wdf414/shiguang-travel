import request from '@/utils/request'

// 获取线路列表
export function getLineList(params) {
  return request.get('/line/list', { params })
}

// 获取线路详情
export function getLineDetail(id) {
  return request.get(`/line/${id}`)
}

// 获取热门线路
export function getHotLines() {
  return request.get('/line/hot')
}

// 获取线路日期价格
export function getLineDatePrices(lineId, month) {
  return request.get(`/line/${lineId}/prices`, { params: { month } })
}

// 收藏线路
export function favoriteLine(lineId) {
  return request.post(`/line/${lineId}/favorite`)
}

// 取消收藏
export function unfavoriteLine(lineId) {
  return request.delete(`/line/${lineId}/favorite`)
}

// 获取收藏列表
export function getFavoriteList(params) {
  return request.get('/line/favorites', { params })
}

// 获取目的地列表
export function getDestinations() {
  return request.get('/line/destinations')
}
