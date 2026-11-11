import request from '@/utils/request'

/**
 * 获取导游攻略列表
 */
export function getGuideList(params) {
  return request({
    url: '/guide/list',
    method: 'get',
    params
  })
}

/**
 * 获取导游攻略详情
 */
export function getGuideDetail(id) {
  return request({
    url: `/guide/${id}`,
    method: 'get'
  })
}

/**
 * 创建导游攻略
 */
export function createGuide(data) {
  return request({
    url: '/guide',
    method: 'post',
    data
  })
}

/**
 * 更新导游攻略
 */
export function updateGuide(id, data) {
  return request({
    url: `/guide/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除导游攻略
 */
export function deleteGuide(id) {
  return request({
    url: `/guide/${id}`,
    method: 'delete'
  })
}

/**
 * 发布导游攻略
 */
export function publishGuide(id) {
  return request({
    url: `/guide/${id}/publish`,
    method: 'post'
  })
}

/**
 * 点赞导游攻略
 */
export function likeGuide(id) {
  return request({
    url: `/guide/${id}/like`,
    method: 'post'
  })
}

/**
 * 获取我的导游攻略列表
 */
export function getMyGuides(params) {
  return request({
    url: '/guide/my',
    method: 'get',
    params
  })
}