import request from '@/utils/request'

// 优惠券管理
export function getAdminCouponList(params) {
    return request.get('/admin/coupon/list', { params })
}

export function createCoupon(data) {
    return request.post('/admin/coupon/add', data)
}

export function updateCoupon(data) {
    return request.put('/admin/coupon/update', data)
}

export function deleteCoupon(id) {
    return request.delete(`/admin/coupon/${id}`)
}

// 活动管理
export function getAdminActivityList(params) {
    return request.get('/admin/activity/list', { params })
}

export function createActivity(data) {
    return request.post('/admin/activity/add', data)
}

export function updateActivity(data) {
    return request.put('/admin/activity/update', data)
}

export function deleteActivity(id) {
    return request.delete(`/admin/activity/${id}`)
}
