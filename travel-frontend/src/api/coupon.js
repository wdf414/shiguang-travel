import request from '@/utils/request'

export function getAvailableCoupons() {
    return request.get('/coupon/available')
}

export function claimCoupon(id) {
    return request.post(`/coupon/claim/${id}`)
}

export function getMyCoupons(status) {
    return request.get('/coupon/my', { params: { status } })
}
