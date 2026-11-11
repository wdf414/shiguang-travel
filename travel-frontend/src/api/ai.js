import request from '@/utils/request'

const AI_TIMEOUT = 120000

export function planTrip(data) {
  return request.post('/ai/plan', data, { timeout: AI_TIMEOUT })
}

export function chatWithAi(data) {
  return request.post('/ai/chat', data, { timeout: AI_TIMEOUT })
}

export function getAiHealth() {
  return request.get('/ai/health', { timeout: 10000 })
}

export function aiModerate(data) {
  return request.post('/ai/moderate', data, { timeout: AI_TIMEOUT })
}
