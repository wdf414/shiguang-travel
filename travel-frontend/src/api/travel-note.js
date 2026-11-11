import request from '@/utils/request'

// 获取游记列表
export function getNoteList(params) {
    return request({
        url: '/travel-notes',
        method: 'get',
        params
    })
}

// 获取游记详情
export function getNoteDetail(id) {
    return request({
        url: `/travel-notes/${id}`,
        method: 'get'
    })
}

// 发布游记
export function createNote(data) {
    return request({
        url: '/travel-notes',
        method: 'post',
        data
    })
}

// 获取评论列表
export function getNoteComments(noteId) {
    return request({
        url: '/travel-note-comments',
        method: 'get',
        params: { noteId }
    })
}

// 发表评论
export function addComment(data) {
    return request({
        url: '/travel-note-comments',
        method: 'post',
        data
    })
}
