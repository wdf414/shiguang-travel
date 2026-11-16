/**
 * 图片地址解析：优先使用仓库内已有的本地景点照片，避免依赖境外图床。
 *
 * 背景：首页与部分演示数据里的图片地址写死为 picsum.photos / unsplash.com /
 * via.placeholder.com 等境外服务。这些域名在国内网络下经常无法访问，
 * 且加载失败兜底图与失败图来自同一个域名，导致整页图片空白。
 *
 * 本地照片直接复用仓库根目录的 scenic_photos/，由后端 /api/photos/** 提供，
 * 不会在仓库中产生任何图片副本。
 */

const PLACEHOLDER_SVG = `<svg xmlns="http://www.w3.org/2000/svg" width="400" height="300" viewBox="0 0 400 300">
  <rect width="400" height="300" fill="#e8f0f9"/>
  <circle cx="320" cy="70" r="26" fill="#c7ddf5"/>
  <path d="M0 220 L90 140 L160 210 L235 120 L320 220 L400 165 L400 300 L0 300 Z" fill="#c7ddf5"/>
  <path d="M0 250 L70 205 L150 255 L240 190 L330 255 L400 225 L400 300 L0 300 Z" fill="#aecbe9"/>
  <text x="200" y="282" text-anchor="middle" font-family="Microsoft YaHei, sans-serif"
        font-size="14" fill="#6b8fb5">图片加载失败</text>
</svg>`

/** 本地兜底占位图（内联 SVG，无需额外图片文件，离线可用） */
export const FALLBACK_IMAGE = 'data:image/svg+xml;charset=UTF-8,' + encodeURIComponent(PLACEHOLDER_SVG)

// 国内网络下大概率不可用的图床域名
const UNREACHABLE_HOSTS = [
  'picsum.photos',
  'via.placeholder.com',
  'placeholder.com',
  'images.unsplash.com',
  'unsplash.com',
  // 携程图床对站外引用做了防盗链/下线处理，直接访问会返回 404，
  // 演示数据里前几家酒店的封面正是携程地址，统一回退到本地景点照片。
  'c-ctrip.com',
  'ctrip.com'
]

/**
 * 后端本地景点照片地址。
 * @param {string} seed 种子，同一 seed 稳定返回同一张照片
 * @param {string} [keyword] 可选，优先匹配文件名包含该关键字的照片
 */
export function localPhoto(seed, keyword) {
  let url = `/api/photos/pick?seed=${encodeURIComponent(seed || 'default')}`
  if (keyword) {
    url += `&keyword=${encodeURIComponent(keyword)}`
  }
  return url
}

/** 判断图片地址是否指向已知不可用的外部图床 */
export function isUnreachableImage(url) {
  if (typeof url !== 'string' || !url) return false
  try {
    const host = new URL(url, window.location.origin).hostname.toLowerCase()
    return UNREACHABLE_HOSTS.some(h => host === h || host.endsWith('.' + h))
  } catch (e) {
    return false
  }
}

/**
 * 解析图片地址：
 * - 空值 / 外部失效图床 → 本地景点照片
 * - data: / blob: / 相对路径 / 其他可访问地址 → 原样返回
 * @param {string} url 原始图片地址
 * @param {string} seed 本地图片种子（建议包含业务 id，保证稳定）
 */
export function resolveImage(url, seed) {
  if (!url || typeof url !== 'string') return localPhoto(seed)
  if (url.startsWith('data:') || url.startsWith('blob:')) return url
  if (isUnreachableImage(url)) return localPhoto(seed || url)
  return url
}

/** img 的 @error 兜底：切换到本地占位图并避免死循环 */
export function onImageError(event) {
  const img = event && event.target
  if (!img || img.dataset.fallbackApplied === '1') return
  img.dataset.fallbackApplied = '1'
  img.src = FALLBACK_IMAGE
  img.onerror = null
}
