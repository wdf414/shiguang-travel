import request from '@/utils/request'

// 获取当前用户的投诉/建议列表
export function getComplaintList(params) {
  return request({
    url: '/complaint/my',
    method: 'get',
    params
  })
}

// 提交投诉/建议
export function submitComplaint(data) {
  return request({
    url: '/complaint/add',
    method: 'post',
    data
  })
}
