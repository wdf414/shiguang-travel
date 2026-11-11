import request from '@/utils/request'

export function getAdminLineList(params) {
  return request.get('/admin/line/list', { params })
}

export function getAdminLineDetail(id) {
  return request.get(`/admin/line/${id}`)
}

export function createLine(data) {
  return request.post('/admin/line', data)
}

export function updateLine(id, data) {
  return request.put(`/admin/line/${id}`, data)
}

export function updateLineStatus(id, status) {
  return request.put(`/admin/line/${id}/status`, null, { params: { status } })
}

export function deleteLine(id) {
  return request.delete(`/admin/line/${id}`)
}

export function getAdminOrderList(params) {
  return request.get('/admin/order/list', { params })
}

export function getAdminOrderDetail(id) {
  return request.get(`/admin/order/${id}`)
}

export function updateOrderStatus(id, status) {
  return request.put(`/admin/order/${id}/status`, { status })
}

export function refundOrder(id, reason) {
  return request.put(`/admin/order/${id}/refund`, { reason })
}

export function getAdminUserList(params) {
  return request.get('/admin/user/list', { params })
}

export function getAdminUserDetail(id) {
  return request.get(`/admin/user/${id}`)
}

export function updateUserStatus(id, status) {
  return request.put(`/admin/user/${id}/status`, null, { params: { status } })
}

export function updateUserRole(id, role) {
  return request.put(`/admin/user/${id}/role`, null, { params: { role } })
}

export function updateUserPassword(id, password) {
  return request.put(`/admin/user/${id}/password`, { password })
}

export function getStatsOverview() {
  return request.get('/admin/stats/overview')
}

export function getSalesTrend(days = 7) {
  return request.get('/admin/stats/sales', { params: { days } })
}

export function getUserTrend(days = 7) {
  return request.get('/admin/stats/users', { params: { days } })
}

export function getLoginHourStats(days = 7) {
  return request.get('/admin/stats/login-hours', { params: { days } })
}

export function getHotLinesRanking(limit = 10) {
  return request.get('/admin/stats/hot-lines', { params: { limit } })
}

export function getUserPurchaseBehavior() {
  return request.get('/admin/stats/purchase-behavior')
}

export function getUserActivity(days = 30) {
  return request.get('/admin/stats/user-activity', { params: { days } })
}

export function getUserProfile() {
  return request.get('/admin/stats/user-profile')
}

export function getPageVisitStats(days = 30) {
  return request.get('/admin/stats/page-visits', { params: { days } })
}

export function getStudentAnalysis() {
  return request.get('/admin/stats/student-analysis')
}

export function getStudentHotDestinations(limit = 10) {
  return request.get('/admin/stats/student-destinations', { params: { limit } })
}

export function getGroupTravelTrend(days = 30) {
  return request.get('/admin/stats/group-travel-trend', { params: { days } })
}

// 投诉管理
export function getAdminComplaintList(params) {
  return request.get('/admin/complaint/list', { params })
}

export function replyComplaint(id, reply) {
  return request.post(`/admin/complaint/${id}/reply`, { reply })
}

// 景点管理
export function getAdminScenicSpotPage(params) {
  return request.get('/admin/scenic-spot/page', { params })
}

export function getAdminScenicSpotDetail(id) {
  return request.get(`/admin/scenic-spot/${id}`)
}

export function createScenicSpot(data) {
  return request.post('/admin/scenic-spot', data)
}

export function updateScenicSpot(data) {
  return request.put('/admin/scenic-spot', data)
}

export function deleteScenicSpot(id) {
  return request.delete(`/admin/scenic-spot/${id}`)
}

export function getUserSource() {
  return request.get('/admin/stats/user-source')
}

export function getConversionRate(days = 30) {
  return request.get('/admin/stats/conversion-rate', { params: { days } })
}

// 价格管理
export function batchAdjustPrice(data) {
  return request.post('/admin/price/batch-adjust', data)
}

export function setSeasonPrice(data) {
  return request.post('/admin/price/season-price', data)
}

export function dynamicPriceAdjust(lineId, factors) {
  return request.post(`/admin/price/dynamic-adjust/${lineId}`, factors)
}

export function getPriceHistory(lineId) {
  return request.get(`/admin/price/history/${lineId}`)
}

// 登录日志
export function getLoginLogs(params) {
  return request.get('/admin/login-log/list', { params })
}

export function getLoginStats(days = 7) {
  return request.get('/admin/login-log/stats', { params: { days } })
}

// 结伴管理
export function getAdminCompanionList(params) {
  return request.get('/admin/companion/list', { params })
}

export function updateCompanionStatus(id, status) {
  return request.put(`/admin/companion/${id}/status`, null, { params: { status } })
}

export function deleteCompanion(id) {
  return request.delete(`/admin/companion/${id}`)
}

// 校园领队管理
export function getAdminLeaderList(params) {
  return request.get('/admin/campus-leader/list', { params })
}

export function auditLeader(id, status) {
  return request.put(`/admin/campus-leader/${id}/audit`, null, { params: { status } })
}

export function setLeaderCommissionRate(id, rate) {
  return request.put(`/admin/campus-leader/${id}/commission-rate`, null, { params: { rate } })
}

export function getLeaderCommissions(id, params) {
  return request.get(`/admin/campus-leader/${id}/commissions`, { params })
}

export function deleteLeader(id) {
  return request.delete(`/admin/campus-leader/${id}`)
}