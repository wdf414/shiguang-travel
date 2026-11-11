import request from '@/utils/request'

// 获取待审核列表
export function getPendingApplications(page = 1, size = 10) {
  return request.get('/admin/student-auth/pending', {
    params: { page, size }
  })
}

// 获取所有申请列表
export function getAllApplications(params) {
  return request.get('/admin/student-auth/list', { params })
}

// 审核通过
export function approveApplication(id) {
  return request.post(`/admin/student-auth/approve/${id}`)
}

// 审核拒绝
export function rejectApplication(id, reason) {
  return request.post(`/admin/student-auth/reject/${id}`, null, {
    params: { reason }
  })
}

// 获取申请详情
export function getApplicationDetail(id) {
  return request.get(`/admin/student-auth/detail/${id}`)
}
