import request from '@/utils/request'

export function getActivityList() {
    return request.get('/activity/list')
}
