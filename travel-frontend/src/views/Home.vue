<template>
  <div class="home-page">
    <!-- 轮播图 -->
    <section class="banner-section">
      <el-carousel height="450px" :interval="5000" :autoplay="true" :pause-on-hover="false" arrow="always">
        <el-carousel-item v-for="item in banners" :key="item.id">
          <div class="banner-item" :style="{ backgroundImage: `url(${item.image})` }">
            <div class="banner-content">
              <h2>{{ item.title }}</h2>
              <p>{{ item.subtitle }}</p>
              <el-button type="primary" size="large" @click="searchByKeyword(item.keyword)">立即探索</el-button>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </section>

    <div class="container">
      <!-- 快速分类导航 -->
      <section class="category-nav">
        <div 
          v-for="cat in categories" 
          :key="cat.name" 
          class="category-card"
          @click="searchByCategory(cat.name)"
          :style="{ background: cat.color }"
        >
          <el-icon :size="32" color="#fff"><component :is="cat.icon" /></el-icon>
          <span>{{ cat.name }}</span>
        </div>
      </section>

      <!-- AI 行程规划入口 -->
      <section class="ai-promo-section">
        <div class="ai-promo-card">
          <div class="ai-promo-copy">
            <div class="ai-promo-badge">
              <el-icon><MagicStick /></el-icon>
              NEW · DeepSeek 驱动
            </div>
            <h2>让 AI 帮你规划下一趟旅行</h2>
            <p>
              输入目的地、天数和预算，AI 会结合平台真实线路、酒店与景点，
              生成一份可以逐项查看、直接预订的行程。
            </p>
            <div class="ai-promo-actions">
              <el-button type="primary" size="large" @click="router.push('/ai/planner')">
                <el-icon><MagicStick /></el-icon>
                开始规划
              </el-button>
              <el-button size="large" plain @click="openAssistant">
                <el-icon><ChatDotRound /></el-icon>
                问问 AI 管家
              </el-button>
            </div>
            <div class="ai-promo-points">
              <span>平台数据增强</span>
              <span>真实产品引用</span>
              <span>学生预算优先</span>
            </div>
          </div>
          <div class="ai-promo-visual">
            <div class="ai-chat-bubble bubble-one">帮我规划桂林 3 天行程</div>
            <div class="ai-chat-bubble bubble-two">已匹配 6 条线路、4 家酒店</div>
            <div class="ai-chat-bubble bubble-three">生成逐日行程 ✓</div>
          </div>
        </div>
      </section>

      <!-- 限时活动专区 -->
      <section class="activity-section" v-if="activityList.length > 0">
        <div class="section-header">
          <h2 class="section-title">🎉 限时活动</h2>
          <span class="tip">精彩活动，不容错过</span>
        </div>
        <div class="activity-grid">
          <div 
            v-for="activity in activityList" 
            :key="activity.id" 
            class="activity-card"
            :class="{ 'student-only': activity.studentOnly === 1 }"
            @click="goToActivity(activity)"
          >
            <div class="activity-cover">
              <img :src="resolveImage(activity.coverImage, 'activity-' + activity.id)" @error="onImgError" />
              <div class="activity-badge">进行中</div>
              <div v-if="activity.studentOnly === 1" class="student-badge">🎓 学生专属</div>
            </div>
            <div class="activity-info">
              <h3 class="activity-title">{{ activity.title }}</h3>
              <p class="activity-desc">{{ activity.description }}</p>
              <div class="activity-time">
                <el-icon><Clock /></el-icon>
                <span>{{ formatTime(activity.startTime) }} - {{ formatTime(activity.endTime) }}</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 学生特惠专区 -->
      <section class="special-section">
        <div class="section-header">
          <h2 class="section-title">🎓 学生特惠</h2>
          <span class="more" @click="searchByCategory('学生特惠')">查看更多 <el-icon><ArrowRight /></el-icon></span>
        </div>
        <div class="line-grid" v-loading="loading">
          <LineCard v-for="line in studentLines" :key="line.id" :line="line" />
        </div>
      </section>

      <!-- 热门目的地 -->
      <section class="destination-section">
        <h2 class="section-title">🔥 热门目的地</h2>
        <div class="destination-grid">
          <div 
            v-for="dest in destinations" 
            :key="dest.name" 
            class="destination-item"
            @click="searchByDestination(dest.name)"
          >
            <img :src="dest.image" :alt="dest.name" @error="onImgError" />
            <span>{{ dest.name }}</span>
          </div>
        </div>
      </section>

      <!-- 为你推荐 (个性化) -->
      <section class="recommend-section">
        <div class="section-header">
          <h2 class="section-title">✨ 为你推荐</h2>
          <span class="tip">基于你的偏好</span>
        </div>
        <div class="line-grid">
          <LineCard v-for="line in recommendedLines" :key="line.id" :line="line" />
        </div>
      </section>
      <!-- 热门游记 (Phase 1 新增) -->
      <section class="travel-note-section" v-if="homeNoteList.length > 0">
        <div class="section-header">
          <h2 class="section-title">📒 热门游记</h2>
          <span class="more" @click="goGuide">查看更多 <el-icon><ArrowRight /></el-icon></span>
        </div>
        <div class="note-grid">
          <div 
            v-for="note in homeNoteList" 
            :key="note.id" 
            class="note-card"
            @click="router.push(`/travel-notes/${note.id}`)"
          >
            <div class="cover">
              <img :src="resolveImage(note.coverImage, 'note-' + note.id)" @error="onImgError" />
              <div class="views"><el-icon><View /></el-icon> {{ note.views }}</div>
            </div>
            <div class="info">
              <h3 class="title">{{ note.title }}</h3>
              <div class="author">
                <el-avatar :size="20" :src="note.author?.avatar">{{ note.author?.nickname?.charAt(0) }}</el-avatar>
                <span>{{ note.author?.nickname }}</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 结伴游 & 攻略预留区 -->
    <section class="student-interaction-section">
      <div class="interaction-grid">
        <div class="interaction-card companion-card" :style="{ backgroundImage: `url(${companionBg})` }">
          <div class="overlay">
            <h3>结伴游</h3>
            <p>找同校伙伴，一起探索世界</p>
            <el-button type="primary" plain @click="goCompanion">立即加入</el-button>
          </div>
        </div>
        <div class="interaction-card guide-card" :style="{ backgroundImage: `url(${guideBg})` }">
          <div class="overlay">
            <h3>旅游攻略</h3>
            <p>看学长学姐分享，避雷更省心</p>
            <el-button type="success" plain @click="goGuide">查看攻略</el-button>
          </div>
        </div>
      </div>
    </section>

    <!-- 筛选栏 -->
    <section class="filter-section">
      <h2 class="section-title">精选线路</h2>
      <div class="filter-bar">
        <el-select v-model="filters.destination" placeholder="目的地" clearable>
          <el-option v-for="d in destinationList" :key="d" :label="d" :value="d" />
        </el-select>
        <el-select v-model="filters.priceRange" placeholder="价格区间" clearable>
          <el-option label="3000以下" value="0-3000" />
          <el-option label="3000-5000" value="3000-5000" />
          <el-option label="5000-8000" value="5000-8000" />
          <el-option label="8000以上" value="8000-99999" />
        </el-select>
        <el-select v-model="filters.sortBy" placeholder="排序">
          <el-option label="默认排序" value="" />
          <el-option label="价格从低到高" value="price_asc" />
          <el-option label="价格从高到低" value="price_desc" />
          <el-option label="评分最高" value="rating" />
          <el-option label="销量最高" value="sales" />
        </el-select>
      </div>
    </section>

    <!-- 线路列表 -->
    <section class="line-section">
      <div class="line-grid" v-loading="loading">
        <LineCard v-for="line in lineList" :key="line.id" :line="line" />
      </div>
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          layout="prev, pager, next"
          @current-change="fetchLines"
        />
      </div>
    </section>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { Calendar, School, Sunny, Ship, ArrowRight, View, Clock } from '@element-plus/icons-vue'
import LineCard from '@/components/LineCard.vue'
import { getLineList, getDestinations } from '@/api/line'
import { getActivityList } from '@/api/activity'
import { localPhoto, resolveImage, onImageError as onImgError } from '@/utils/image'

const router = useRouter()
const loading = ref(false)
const lineList = ref([])
const studentLines = ref([])
const recommendedLines = ref([])
const destinationList = ref([])
const homeNoteList = ref([]) // 首页游记列表
const activityList = ref([]) // 活动列表

// 轮播图配置
const banners = ref([
  { id: 1, image: localPhoto('banner-1', '桂林'), title: '探索未知的世界', subtitle: '专为学生打造的旅游平台', keyword: '毕业季' },
  { id: 2, image: localPhoto('banner-2', '黄山'), title: '趁年轻，去旅行', subtitle: '定制化行程，低价高质', keyword: '周边游' }
])

// 分类导航
const categories = [
  { name: '学生特惠', icon: 'School', color: '#ff7f50' },
  { name: '周末游', icon: 'Calendar', color: '#6495ed' },
  { name: '海岛度假', icon: 'Sunny', color: '#20b2aa' },
  { name: '游轮旅行', icon: 'Ship', color: '#9370db' }
]

// 热门目的地数据 (静态+动态)
const destinations = ref([
  { name: '三亚', image: localPhoto('dest-sanya', '三亚') },
  { name: '大理', image: localPhoto('dest-dali', '大理') },
  { name: '成都', image: localPhoto('dest-chengdu', '成都') },
  { name: '西藏', image: localPhoto('dest-tibet', '布达拉宫') },
  { name: '新疆', image: localPhoto('dest-xinjiang', '胡杨') },
  { name: '北京', image: localPhoto('dest-beijing', '北京') }
])

// 筛选逻辑
const filters = reactive({
  destination: '',
  priceRange: '',
  sortBy: ''
})

const pagination = reactive({
  page: 1,
  size: 8,
  total: 0
})

// 随机背景图逻辑
const companionBg = ref('')
const guideBg = ref('')
let bgTimer = null

const bgImages = [
  localPhoto('companion-1', '古城'),
  localPhoto('companion-2', '洱海'),
  localPhoto('companion-3', '草原'),
  localPhoto('companion-4', '古镇'),
  localPhoto('companion-5', '山水')
]

function rotateCardsBackground() {
  const seeds = [
    Math.floor(Math.random() * bgImages.length),
    Math.floor(Math.random() * bgImages.length)
  ]
  companionBg.value = bgImages[seeds[0]]
  guideBg.value = bgImages[seeds[1]]
}

// AI 管家入口：由 MainLayout 中全局挂载的 AiAssistant 监听
function openAssistant() {
  window.dispatchEvent(new CustomEvent('open-ai-assistant'))
}

// 业务方法
async function fetchStudentLines() {
  try {
    const res = await getLineList({ page: 1, size: 4, category: '学生特惠' })
    studentLines.value = res.records
  } catch (e) {
    console.error('Fetch student lines error', e)
  }
}

async function fetchRecommendedLines() {
  try {
    const res = await getLineList({ page: 1, size: 4, sortBy: 'rating' })
    recommendedLines.value = res.records
  } catch (e) {
    console.error('Fetch recommended lines error', e)
  }
}

async function fetchLines() {
  loading.value = true
  try {
    const [minPrice, maxPrice] = filters.priceRange ? filters.priceRange.split('-').map(Number) : [undefined, undefined]
    const params = {
      page: pagination.page,
      size: pagination.size,
      destination: filters.destination || undefined,
      minPrice,
      maxPrice,
      sortBy: filters.sortBy || undefined
    }
    const res = await getLineList(params)
    lineList.value = res.records
    pagination.total = res.total
  } catch (e) {
    console.error('Fetch lines error', e)
  } finally {
    loading.value = false
  }
}

async function fetchDestinations() {
  try {
    const res = await getDestinations()
    destinationList.value = res
  } catch (e) {
    console.error('Fetch destinations error', e)
    destinationList.value = ['四川', '云南', '西藏', '新疆', '海南', '北京']
  }
}

// 获取活动列表
async function fetchActivities() {
  try {
    const res = await getActivityList()
    activityList.value = res || []
  } catch (e) {
    console.error('Fetch activities error', e)
    activityList.value = []
  }
}

// 跳转到活动详情
function goToActivity(activity) {
  // 可以跳转到活动详情页或搜索页面
  router.push({ path: '/search', query: { activity: activity.title } })
}

// 格式化时间
function formatTime(time) {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 10)
}

function searchByKeyword(keyword) {
  router.push({ path: '/search', query: { keyword } })
}

function searchByCategory(category) {
  router.push({ path: '/search', query: { category } })
}

function searchByDestination(destination) {
  router.push({ path: '/search', query: { destination } })
}

function goCompanion() {
  router.push('/companion')
}

function goGuide() {
  router.push('/travel-notes')
}

watch(filters, () => {
  pagination.page = 1
  fetchLines()
}, { deep: true })

onMounted(async () => {
  fetchLines()
  fetchStudentLines()
  fetchRecommendedLines()
  fetchDestinations()
  fetchActivities() // 获取活动列表
  
  try {
    const res = await import('@/api/travel-note').then(m => m.getNoteList({ page: 1, size: 4, sort: 'hot' }))
    homeNoteList.value = res.records
  } catch (e) {
    console.error('Failed to fetch home notes', e)
  }
  
  rotateCardsBackground()
  bgTimer = window.setInterval(rotateCardsBackground, 5000)
})

onBeforeUnmount(() => {
  if (bgTimer) window.clearInterval(bgTimer)
})

</script>

<style lang="scss" scoped>
// 穷游网风格
$primary: #0086f6;
$primary-hover: #0070d6;
$text-primary: #333;
$text-secondary: #666;
$bg-color: #f5f5f5;

.home-page {
  padding-bottom: 60px;
  background: $bg-color;
  min-height: 100vh;
}

// ==========================================
// 轮播图样式
// ==========================================
.banner-section {
  position: relative;
  
  :deep(.el-carousel__arrow) {
    background: rgba(0,0,0,0.3);
    color: #fff;
    border-radius: 50%;
    
    &:hover {
      background: rgba(0,0,0,0.6);
    }
  }
  
  :deep(.el-carousel__indicators) {
    bottom: 20px;
    
    .el-carousel__indicator {
      .el-carousel__button {
        width: 10px;
        height: 10px;
        border-radius: 50%;
        background: rgba(255,255,255,0.6);
        opacity: 1;
      }
      
      &.is-active .el-carousel__button {
        background: $primary;
        width: 10px;
      }
    }
  }
  
  .banner-item {
    height: 100%;
    background-color: #dbe4ef;
    background-size: cover;
    background-position: center;
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
    
    &::before {
      content: '';
      position: absolute;
      top: 0; left: 0; right: 0; bottom: 0;
      background: rgba(0,0,0,0.2); // 简单的遮罩
    }
  }
  
  .banner-content {
    position: relative;
    z-index: 1;
    text-align: center;
    color: #fff;
    
    h2 {
      font-size: 56px;
      font-weight: 700;
      margin-bottom: 20px;
      text-shadow: 0 2px 10px rgba(0,0,0,0.3);
    }
    
    p {
      font-size: 24px;
      margin-bottom: 40px;
      text-shadow: 0 1px 5px rgba(0,0,0,0.3);
    }
    
    .el-button {
      background: $primary;
      border: none;
      padding: 12px 36px;
      font-size: 16px;
      border-radius: 4px;
      transition: background 0.3s;
      
      &:hover {
        background: $primary-hover;
      }
    }
  }
}

// ==========================================
// 分类导航
// ==========================================
.category-nav {
  display: flex;
  background: #fff;
  padding: 20px 0;
  margin-top: -4px; // 紧贴 banner
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
  position: relative;
  z-index: 10;
  margin-bottom: 30px;
  justify-content: center;
  gap: 10px;
  
  .category-card {
    width: 200px;
    height: 100px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 10px;
    color: $text-primary;
    cursor: pointer;
    transition: all 0.2s;
    border-radius: 4px;
    border: 1px solid transparent; // 预留边框位置
    background: transparent !important; // 覆盖原来的渐变
    
    // 图标颜色
    .el-icon {
      color: $primary !important;
      font-size: 32px;
      margin-bottom: 5px;
    }
    
    span {
      font-size: 16px;
      font-weight: 500;
    }
    
    &:hover {
      background: #f0f9ff !important;
      border-color: #dbeafe;
      
      .el-icon {
        transform: scale(1.1);
      }
    }
  }
}

// ==========================================
// 通用区块
// ==========================================
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
  
  .section-title {
    font-size: 24px;
    color: $text-primary;
    font-weight: 600;
    position: relative;
    padding-left: 15px;
    margin: 0;
    
    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 4px;
      bottom: 4px;
      width: 4px;
      background: $primary; // 蓝色竖条
      border-radius: 0;
    }
  }

  .more {
    cursor: pointer;
    color: $text-secondary;
    font-size: 14px;
    display: flex;
    align-items: center;
    gap: 4px;
    
    &:hover {
      color: $primary;
    }
  }
}

.special-section, .recommend-section, .destination-section, .activity-section {
  background: #fff;
  padding: 30px;
  margin-bottom: 30px;
  border: 1px solid #e5e5e5;
  border-radius: 0; // 穷游网大多是直角或微圆角
  box-shadow: none;
}

// ==========================================
// AI 行程入口
// ==========================================
.ai-promo-section {
  margin-bottom: 30px;
}

.ai-promo-card {
  position: relative;
  overflow: hidden;
  display: grid;
  grid-template-columns: 1.35fr 0.65fr;
  gap: 24px;
  padding: 32px 36px;
  border-radius: 18px;
  background: linear-gradient(125deg, #1f2455 0%, #3d3a8f 56%, #6d4aa8 100%);
  color: #fff;
  box-shadow: 0 24px 50px -30px rgba(31, 36, 85, 0.65);

  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background-image: linear-gradient(rgba(255,255,255,0.035) 1px, transparent 1px),
      linear-gradient(90deg, rgba(255,255,255,0.035) 1px, transparent 1px);
    background-size: 34px 34px;
    pointer-events: none;
  }
}

.ai-promo-copy {
  position: relative;
  z-index: 2;

  h2 {
    margin: 14px 0 10px;
    font-size: 28px;
    line-height: 1.25;
    color: #fff;
  }

  p {
    max-width: 620px;
    margin: 0;
    color: rgba(255, 255, 255, 0.76);
    font-size: 14px;
    line-height: 1.8;
  }
}

.ai-promo-badge {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.2);
  font-size: 12px;
}

.ai-promo-actions {
  display: flex;
  gap: 10px;
  margin-top: 20px;

  :deep(.el-button--primary) {
    border: none;
    background: #fff;
    color: #4c3b9b;
    font-weight: 600;

    &:hover { background: #f1eeff; }
  }

  :deep(.el-button.is-plain) {
    border-color: rgba(255, 255, 255, 0.4);
    background: transparent;
    color: #fff;

    &:hover {
      border-color: #fff;
      background: rgba(255, 255, 255, 0.1);
      color: #fff;
    }
  }
}

.ai-promo-points {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-top: 18px;
  color: rgba(255, 255, 255, 0.55);
  font-size: 11px;

  span::before {
    content: '·';
    margin-right: 6px;
    color: #a78bfa;
  }
}

.ai-promo-visual {
  position: relative;
  min-height: 170px;
}

.ai-chat-bubble {
  position: absolute;
  max-width: 220px;
  padding: 10px 13px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.95);
  color: #2d3748;
  font-size: 12px;
  line-height: 1.5;
  box-shadow: 0 14px 30px -18px rgba(0, 0, 0, 0.6);

  &.bubble-one {
    top: 8px;
    left: 0;
    background: rgba(255, 255, 255, 0.16);
    color: #fff;
    border: 1px solid rgba(255, 255, 255, 0.2);
  }

  &.bubble-two {
    top: 62px;
    right: 0;
  }

  &.bubble-three {
    bottom: 6px;
    left: 12%;
    background: #eef1ff;
    color: #4c3b9b;
    font-weight: 600;
  }
}

// ==========================================
// 活动专区样式
// ==========================================
.activity-section {
  .activity-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 20px;
  }
  
  .activity-card {
    border: 1px solid #e5e5e5;
    border-radius: 4px;
    overflow: hidden;
    cursor: pointer;
    transition: all 0.3s;
    
    &:hover {
      box-shadow: 0 4px 12px rgba(0,0,0,0.1);
      transform: translateY(-2px);
    }
    
    .activity-cover {
      position: relative;
      height: 160px;
      overflow: hidden;
      
      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
      
      .activity-badge {
        position: absolute;
        top: 10px;
        left: 10px;
        background: #ff6b6b;
        color: #fff;
        padding: 4px 12px;
        border-radius: 4px;
        font-size: 12px;
        font-weight: 500;
      }
      
      .student-badge {
        position: absolute;
        top: 10px;
        right: 10px;
        background: #67c23a;
        color: #fff;
        padding: 4px 12px;
        border-radius: 4px;
        font-size: 12px;
        font-weight: 500;
      }
    }
    
    .activity-info {
      padding: 15px;
      
      .activity-title {
        font-size: 16px;
        font-weight: 600;
        color: $text-primary;
        margin: 0 0 8px 0;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      
      .activity-desc {
        font-size: 13px;
        color: $text-secondary;
        margin: 0 0 12px 0;
        overflow: hidden;
        text-overflow: ellipsis;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        line-height: 1.5;
      }
      
      .activity-time {
        display: flex;
        align-items: center;
        gap: 6px;
        font-size: 12px;
        color: #999;
        
        .el-icon {
          font-size: 14px;
        }
      }
    }
  }
}

// ==========================================
// 热门目的地
// ==========================================
.destination-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 15px;
}

.destination-item {
  position: relative;
  height: 160px;
  cursor: pointer;
  overflow: hidden;
  border-radius: 4px;
  
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.3s;
  }
  
  span {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    background: linear-gradient(transparent, rgba(0,0,0,0.8));
    color: #fff;
    text-align: center;
    padding: 10px;
    font-size: 16px;
    font-weight: 600;
  }
  
  &:hover img {
    transform: scale(1.1);
  }
}

// ==========================================
// 热门游记 (首页)
// ==========================================
.travel-note-section {
  background: #fff;
  padding: 30px;
  margin-bottom: 30px;
  border: 1px solid #e5e5e5;
  
  .note-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;
    
    .note-card {
      cursor: pointer;
      &:hover {
        .title { color: $primary; }
        img { transform: scale(1.05); }
      }
      
      .cover {
        height: 140px;
        overflow: hidden;
        border-radius: 4px;
        position: relative;
        margin-bottom: 10px;
        
        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
          transition: transform 0.3s;
        }
        
        .views {
          position: absolute;
          bottom: 5px;
          left: 5px;
          color: #fff;
          font-size: 12px;
          background: rgba(0,0,0,0.5);
          padding: 2px 6px;
          border-radius: 4px;
          display: flex;
          align-items: center;
          gap: 2px;
        }
      }
      
      .info {
        .title {
          font-size: 15px;
          font-weight: 500;
          margin-bottom: 8px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          transition: color 0.2s;
        }
        
        .author {
          display: flex;
          align-items: center;
          gap: 6px;
          font-size: 12px;
          color: #999;
        }
      }
    }
  }
}

// ==========================================
// 结伴游 & 攻略预留区
// ==========================================
.student-interaction-section {
  margin-bottom: 30px;
  
  .interaction-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 20px;
  }
  
  .interaction-card {
    height: 180px;
    border-radius: 4px;
    overflow: hidden;
    position: relative;
    cursor: pointer;
    background-color: #e0e0e0;
    background-size: cover;
    background-position: center;
    transition: background-image 1s ease-in-out;
    
    .overlay {
      position: absolute;
      top: 0; left: 0; right: 0; bottom: 0;
      background: rgba(0,0,0,0.4);
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;
      color: #fff;
      transition: background 0.3s;
      
      h3 {
        font-size: 28px;
        margin-bottom: 10px;
        font-weight: 700;
      }
      
      p {
        font-size: 14px;
        margin-bottom: 20px;
        opacity: 0.9;
      }
      
      .el-button {
        border-radius: 2px;
        padding: 8px 25px;
        border: 1px solid #fff;
        background: transparent;
        color: #fff;
        
        &:hover {
          background: #fff;
          color: $primary;
        }
      }
    }
    
    &:hover .overlay {
      background: rgba(0,0,0,0.2);
    }
  }
}

// ==========================================
// 筛选栏 & 线路列表
// ==========================================
.filter-section {
  background: #fff;
  padding: 20px 30px;
  margin-bottom: 20px;
  border: 1px solid #e5e5e5;
  border-radius: 4px;
  
  .section-title {
    font-size: 20px;
    color: $text-primary;
    margin-bottom: 20px;
    font-weight: 700;
  }
}

.filter-bar {
  display: flex;
  gap: 15px;
  
  .el-select {
    width: 140px;
    
    :deep(.el-input__wrapper) {
      box-shadow: none !important;
      border: 1px solid #dcdfe6;
      border-radius: 4px;
      
      &:hover, &.is-focus {
        border-color: $primary;
      }
    }
  }
}

.line-section {
  margin-bottom: 40px;
}

.line-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  min-height: 200px;
}

.pagination {
  margin-top: 40px;
  display: flex;
  justify-content: center;
  
  :deep(.el-pagination) {
    .el-pager li.is-active {
      background-color: $primary !important;
      color: #fff;
    }
  }
}

// ==========================================
// 动画
// ==========================================
// 移除多余动画，保持简洁

// ==========================================
// 响应式
// ==========================================
@media (max-width: 1200px) {
  .line-grid { grid-template-columns: repeat(3, 1fr); }
  .category-nav { gap: 15px; }
}

@media (max-width: 768px) {
  .ai-promo-card {
    grid-template-columns: 1fr;
    padding: 24px;
  }
  .ai-promo-copy h2 { font-size: 22px; }
  .ai-promo-visual { display: none; }
  .destination-grid { grid-template-columns: repeat(3, 1fr); }
  .line-grid { grid-template-columns: repeat(2, 1fr); }
  .category-nav {
    flex-wrap: wrap;
    margin-top: -10px;
    
    .category-card {
      width: 45%;
      height: 90px;
    }
  }
  .student-interaction-section .interaction-grid {
    grid-template-columns: 1fr;
  }
  .banner-section .banner-content {
    h2 { font-size: 32px; }
    p { font-size: 16px; }
  }
}

@media (max-width: 480px) {
  .line-grid { grid-template-columns: 1fr; }
  .destination-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>
