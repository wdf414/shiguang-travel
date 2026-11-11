import request from '@/utils/request'

// 提交学生认证申请
export function submitStudentAuth(data) {
  return request.post('/student/auth/apply', data)
}

// 获取学生认证状态
export function getStudentAuthStatus() {
  return request.get('/student/auth/status')
}

// 计算学生价格
export function calculateStudentPrice(lineId, originalPrice) {
  return request.get('/student/price/calculate', {
    params: { lineId, originalPrice }
  })
}

// 检查是否为学生
export function checkIsStudent() {
  return request.get('/student/check')
}

// 更新学生信息
export function updateStudentInfo(data) {
  return request.put('/student/info', data)
}
