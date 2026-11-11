import request from '@/utils/request'

/**
 * 获取结伴游列表（支持校园筛选）
 */
export function getCompanionList(params) {
  return request({
    url: '/companion/list',
    method: 'get',
    params
  })
}

/**
 * 获取结伴游详情
 */
export function getCompanionDetail(id) {
  return request({
    url: `/companion/${id}`,
    method: 'get'
  })
}

/**
 * 创建结伴游
 */
export function createCompanion(data) {
  return request({
    url: '/companion',
    method: 'post',
    data
  })
}

/**
 * 更新结伴游
 */
export function updateCompanion(id, data) {
  return request({
    url: `/companion/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除结伴游
 */
export function deleteCompanion(id) {
  return request({
    url: `/companion/${id}`,
    method: 'delete'
  })
}

/**
 * 加入结伴游
 */
export function joinCompanion(id) {
  return request({
    url: `/companion/${id}/join`,
    method: 'post'
  })
}

/**
 * 退出结伴游
 */
export function quitCompanion(id) {
  return request({
    url: `/companion/${id}/quit`,
    method: 'post'
  })
}

/**
 * 获取用户加入的结伴游列表
 */
export function getUserJoinedCompanions(params) {
  return request({
    url: '/companion/joined',
    method: 'get',
    params
  })
}

/**
 * 获取用户发布的结伴游列表
 */
export function getUserPublishedCompanions(params) {
  return request({
    url: '/companion/published',
    method: 'get',
    params
  })
}

/**
 * 获取群聊入口
 */
export function getGroupChat(id) {
  return request({
    url: `/companion/${id}/group-chat`,
    method: 'get'
  })
}

/**
 * 获取结伴游成员列表（包含详情）
 */
export function getCompanionMembers(id) {
  return request({
    url: `/companion/${id}/members`,
    method: 'get'
  })
}

// ===== 校园领队相关 =====

/**
 * 申请成为校园领队
 */
export function applyLeader(data) {
  return request({
    url: '/campus-leader/apply',
    method: 'post',
    data
  })
}

/**
 * 获取当前用户的领队信息
 */
export function getLeaderInfo() {
  return request({
    url: '/campus-leader/info',
    method: 'get'
  })
}

/**
 * 获取认证领队列表
 */
export function getLeaderList(params) {
  return request({
    url: '/campus-leader/list',
    method: 'get',
    params
  })
}