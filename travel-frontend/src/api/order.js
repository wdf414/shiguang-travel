import request from '@/utils/request'

// 创建订单
export function createOrder(data) {
  return request.post('/order/create', data)
}

// 获取订单列表
export function getOrderList(params) {
  return request.get('/order/list', { params })
}

// 获取订单详情
export function getOrderDetail(orderNo) {
  return request.get(`/order/${orderNo}`)
}

// 取消订单
export function cancelOrder(orderNo) {
  return request.put(`/order/${orderNo}/cancel`)
}

// 支付订单
export function payOrder(orderNo, payType) {
  return request.post(`/order/${orderNo}/pay`, { payType })
}

// 查询支付状态
export function getPayStatus(orderNo) {
  return request.get(`/order/${orderNo}/pay-status`)
}
