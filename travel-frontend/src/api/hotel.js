import request from '@/utils/request'

// 获取酒店列表
export function getHotelList(params) {
    return request({
        url: '/hotels',
        method: 'get',
        params
    })
}

// 获取酒店评分列表
export function getHotelGradeList(params) {
    return request({
        url: '/hotel-grades',
        method: 'get',
        params
    })
}

// 获取酒店详情
export function getHotelDetail(id) {
    return request({
        url: `/hotels/${id}`,
        method: 'get'
    })
}

// 获取酒店房型
export function getHotelRooms(id) {
    return request({
        url: `/hotels/${id}/rooms`,
        method: 'get'
    })
}

// 创建酒店订单
export function createHotelOrder(data) {
    return request({
        url: '/hotel-orders',
        method: 'post',
        data
    })
}

// 获取酒店订单详情
export function getHotelOrderDetail(orderNo) {
    return request({
        url: `/hotel-orders/${orderNo}`,
        method: 'get'
    })
}
