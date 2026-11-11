import request from '@/utils/request'

/**
 * 获取评价列表
 * @param lineId 可选，兼容旧代码
 * @param params { page, size, lineId, hotelId, guideId, onlyStudent }
 */
export function getReviewList(lineId, params) {
  // 如果传入了 lineId，合并到 params 中
  const queryParams = { ...params }
  if (lineId) {
    queryParams.lineId = lineId
  }
  
  return request({
    url: '/review/list',
    method: 'get',
    params: queryParams
  })
}

/**
 * 提交评价
 */
export function submitReview(data) {
  return request({
    url: '/review/submit',
    method: 'post',
    data
  })
}