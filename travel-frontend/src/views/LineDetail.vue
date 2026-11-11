<template>
  <div class="line-detail container" v-loading="loading">
    <template v-if="line">
      <!-- 基本信息 -->
      <div class="detail-header">
        <div class="image-gallery">
          <el-carousel height="400px">
            <el-carousel-item v-for="(img, idx) in line.images" :key="idx">
              <img :src="img" class="gallery-img" @error="onImgError" />
            </el-carousel-item>
          </el-carousel>
        </div>
        <div class="info-panel">
          <h1 class="title">{{ line.title }}</h1>
          <div class="tags">
            <el-tag>{{ line.days }}天{{ line.days - 1 }}晚</el-tag>
            <el-tag type="success">{{ line.destination }}</el-tag>
          </div>
          <div class="price-box">
            <span class="label">价格</span>
            <template v-if="studentPriceInfo?.isStudent">
              <span class="original-price">¥{{ selectedPrice || line.price }}</span>
              <span class="price student-price">¥{{ studentPriceInfo.studentPrice }}</span>
              <span class="unit">起/人</span>
              <el-tag type="success" size="small" class="student-tag">学生价</el-tag>
              <span class="discount-text">省¥{{ studentPriceInfo.discountAmount }}</span>
            </template>
            <template v-else>
              <span class="price">¥{{ selectedPrice || line.price }}</span>
              <span class="unit">起/人</span>
            </template>
          </div>
          <div class="stats">
            <span><el-icon><Star /></el-icon>{{ line.rating }}分</span>
            <span>已售{{ line.sales }}人</span>
            <span>{{ line.reviewCount }}条评价</span>
          </div>
          
          <!-- 日期选择 -->
          <div class="date-select">
            <span class="label">出发日期</span>
            <el-date-picker
              v-model="selectedDate"
              type="date"
              placeholder="选择出发日期"
              :disabled-date="disabledDate"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
            />
            <span v-if="selectedStock !== null" class="stock">
              余位：<em :class="{ warning: selectedStock < 10 }">{{ selectedStock }}</em>
            </span>
          </div>

          <!-- 人数选择 -->
          <div class="people-select">
            <span class="label">出行人数</span>
            <el-input-number v-model="peopleCount" :min="1" :max="selectedStock || 10" />
          </div>

          <div class="actions">
            <el-button type="primary" size="large" @click="handleBook" :disabled="!selectedDate">
              立即预订
            </el-button>
            <el-button size="large" @click="handleFavorite">
              <el-icon><Star /></el-icon>
              {{ line.isFavorite ? '已收藏' : '收藏' }}
            </el-button>
          </div>
        </div>
      </div>

      <!-- 行程详情 -->
      <el-tabs v-model="activeTab" class="detail-tabs">
        <el-tab-pane label="行程介绍" name="intro">
          <div class="schedule-list">
            <div v-for="(day, idx) in line.schedule" :key="idx" class="schedule-item">
              <div class="day-tag">Day {{ idx + 1 }}</div>
              <div class="day-content">
                <h4>{{ day.title }}</h4>
                <p>{{ day.content }}</p>
              </div>
            </div>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="费用说明" name="cost">
          <div class="cost-section">
            <div class="cost-group">
              <h4><el-icon class="icon-include"><CircleCheckFilled /></el-icon> 费用包含</h4>
              <div class="cost-items">
                <span v-for="item in line.includes" :key="item" class="cost-tag">{{ item }}</span>
              </div>
            </div>
            <div class="cost-group">
              <h4><el-icon class="icon-exclude"><CircleCloseFilled /></el-icon> 费用不含</h4>
              <div class="cost-items">
                <span v-for="item in line.excludes" :key="item" class="cost-tag exclude">{{ item }}</span>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="用户评价" name="reviews">
          <div class="reviews-section">
            <div class="review-filter">
              <el-checkbox v-model="onlyStudent" @change="fetchReviews">只看大学生评价</el-checkbox>
              <el-button type="primary" size="small" @click="reviewDialogVisible = true" style="margin-left: 15px;">
                写评价
              </el-button>
            </div>
            <div v-for="review in reviews" :key="review.id" class="review-item">
              <div class="review-header">
                <el-avatar :size="40" :src="review.avatar">{{ review.nickname?.charAt(0) }}</el-avatar>
                <div class="review-info">
                  <div class="name-row">
                    <span class="nickname">{{ review.nickname }}</span>
                    <el-tag v-if="review.isStudent === 1" size="small" type="success" effect="dark">大学生认证</el-tag>
                  </div>
                  <el-rate v-model="review.rating" disabled text-color="#ff9900" />
                </div>
                <span class="date">{{ review.createTime }}</span>
              </div>
              
              <div v-if="review.tags" class="review-tags">
                <el-tag 
                  v-for="(tag, index) in (typeof review.tags === 'string' ? review.tags.split(',') : review.tags)" 
                  :key="index" 
                  size="small" 
                  effect="plain"
                  class="review-tag"
                >
                  {{ tag }}
                </el-tag>
              </div>

              <p class="review-content">{{ review.content }}</p>
              
              <div v-if="review.images" class="review-images">
                <el-image 
                  v-for="(img, idx) in parseImages(review.images)" 
                  :key="idx" 
                  :src="img" 
                  :preview-src-list="parseImages(review.images)"
                  fit="cover"
                  class="review-img"
                />
              </div>
            </div>
            <el-empty v-if="!reviews.length" description="暂无评价" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </template>
    
    <ReviewDialog
      v-if="line"
      v-model="reviewDialogVisible"
      type="line"
      :id="line.id"
      @success="handleReviewSuccess"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Star, CircleCheckFilled, CircleCloseFilled } from '@element-plus/icons-vue'
import { getLineDetail, favoriteLine, unfavoriteLine } from '@/api/line'
import { getReviewList } from '@/api/review'
import { calculateStudentPrice } from '@/api/student'
import { useUserStore } from '@/stores/user'
import ReviewDialog from '@/components/ReviewDialog.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const line = ref(null)
const reviews = ref([])
const onlyStudent = ref(false)
const reviewDialogVisible = ref(false)
const activeTab = ref('intro')
const selectedDate = ref('')
const peopleCount = ref(1)
const selectedPrice = ref(null)
const selectedStock = ref(null)
const studentPriceInfo = ref(null)
const fallbackImg = 'https://picsum.photos/800/400?random=77'

function onImgError(e) {
  e.target.src = fallbackImg
  e.target.onerror = null
}

function handleReviewSuccess() {
  fetchReviews()
}

const disabledDate = (date) => {
  return date < new Date()
}

async function fetchDetail() {
  loading.value = true
  try {
    const data = await getLineDetail(route.params.id)
    
    // 获取学生价格
    if (userStore.isLoggedIn && data.price) {
      try {
        const priceRes = await calculateStudentPrice(route.params.id, data.price)
        studentPriceInfo.value = priceRes
      } catch (e) {
        console.error('获取学生价格失败:', e)
      }
    }
    // 解析 JSON 字符串字段
    if (data.images && typeof data.images === 'string') {
      try { data.images = JSON.parse(data.images) } catch (e) { console.error('Parse images error', e) }
    }
    if (data.schedule && typeof data.schedule === 'string') {
      try { data.schedule = JSON.parse(data.schedule) } catch (e) { console.error('Parse schedule error', e) }
    }
    if (data.includes && typeof data.includes === 'string') {
      try { data.includes = JSON.parse(data.includes) } catch (e) { console.error('Parse includes error', e) }
    }
    if (data.excludes && typeof data.excludes === 'string') {
      try { data.excludes = JSON.parse(data.excludes) } catch (e) { console.error('Parse excludes error', e) }
    }
    line.value = data
  } catch (e) {
    console.error('Fetch detail error', e)
    // 模拟数据
    line.value = {
      id: route.params.id,
      title: '云南大理丽江双飞6日游',
      images: [
        'https://picsum.photos/800/400?random=30',
        'https://picsum.photos/800/400?random=31',
        'https://picsum.photos/800/400?random=32'
      ],
      destination: '云南',
      departureCity: '上海',
      days: 6,
      price: 2999,
      rating: 4.8,
      sales: 356,
      reviewCount: 128,
      isFavorite: false,
      schedule: [
        { title: '上海-昆明', content: '乘机前往春城昆明，接机后入住酒店休息。' },
        { title: '昆明-大理', content: '乘车前往大理，游览洱海、崇圣寺三塔。' },
        { title: '大理古城', content: '游览大理古城，体验白族文化。' },
        { title: '大理-丽江', content: '前往丽江，游览束河古镇。' },
        { title: '玉龙雪山', content: '游览玉龙雪山，蓝月谷。' },
        { title: '丽江-上海', content: '自由活动后乘机返回上海。' }
      ],
      includes: ['往返机票', '5晚酒店住宿', '行程内用餐', '景点首道门票', '旅游大巴', '导游服务'],
      excludes: ['个人消费', '单房差', '自费项目', '旅游意外险']
    }
  } finally {
    loading.value = false
  }
}

function parseImages(images) {
  if (!images) return []
  if (typeof images === 'string') {
    try {
      return JSON.parse(images)
    } catch {
      return []
    }
  }
  return Array.isArray(images) ? images : []
}

async function fetchReviews() {
  try {
    const params = {
      page: 1,
      size: 10,
      onlyStudent: onlyStudent.value
    }
    const data = await getReviewList(route.params.id, params)
    reviews.value = data.records
  } catch (e) {
    console.error('Fetch reviews error', e)
    reviews.value = []
  }
}

function handleBook() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  router.push({
    path: `/order/confirm/${line.value.id}`,
    query: { date: selectedDate.value, count: peopleCount.value }
  })
}

async function handleFavorite() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  try {
    if (line.value.isFavorite) {
      await unfavoriteLine(line.value.id)
      line.value.isFavorite = false
      ElMessage.success('已取消收藏')
    } else {
      await favoriteLine(line.value.id)
      line.value.isFavorite = true
      ElMessage.success('收藏成功')
    }
  } catch {}
}

onMounted(() => {
  fetchDetail()
  fetchReviews()
})
</script>

<style lang="scss" scoped>
.detail-header {
  display: grid;
  grid-template-columns: 1fr 400px;
  gap: 30px;
  margin-bottom: 30px;
}
.gallery-img { width: 100%; height: 100%; object-fit: cover; }
.info-panel {
  background: #fff;
  padding: 25px;
  border-radius: 8px;
}
.title { font-size: 22px; margin-bottom: 15px; }
.tags { display: flex; gap: 10px; margin-bottom: 20px; }
.price-box {
  background: #fff8f0;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 15px;
  .label { color: #666; margin-right: 10px; }
  .price { font-size: 32px; color: #ff6600; font-weight: bold; }
  .original-price { 
    font-size: 20px; 
    color: #999; 
    text-decoration: line-through; 
    margin-right: 10px;
  }
  .student-price { color: #67c23a; }
  .unit { color: #999; }
  .student-tag { margin-left: 10px; }
  .discount-text { 
    color: #67c23a; 
    font-size: 14px; 
    margin-left: 10px;
    background: #f0f9eb;
    padding: 2px 8px;
    border-radius: 4px;
  }
}
.stats {
  display: flex;
  gap: 20px;
  color: #666;
  margin-bottom: 20px;
  span { display: flex; align-items: center; gap: 4px; }
}
.date-select, .people-select {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 15px;
  .label { color: #333; min-width: 70px; }
  .stock { color: #999; em { color: #67c23a; font-style: normal; &.warning { color: #e6a23c; } } }
}
.actions {
  display: flex;
  gap: 15px;
  margin-top: 25px;
  .el-button { flex: 1; }
}
.detail-tabs { background: #fff; padding: 20px; border-radius: 8px; }
.schedule-list { padding: 20px 0; }
.schedule-item {
  display: flex;
  gap: 20px;
  padding: 20px 0;
  border-bottom: 1px dashed #eee;
  .day-tag {
    background: #409eff;
    color: #fff;
    padding: 5px 15px;
    border-radius: 4px;
    height: fit-content;
    flex-shrink: 0;
  }
  .day-content {
    h4 { margin: 0 0 10px; }
    p { margin: 0; line-height: 1.6; color: #666; }
  }
}
.cost-section {
  display: flex;
  gap: 40px;
  .cost-group { flex: 1; }
  h4 { display: flex; align-items: center; gap: 8px; margin-bottom: 15px; }
  .icon-include { color: #67c23a; }
  .icon-exclude { color: #f56c6c; }
  .cost-items { display: flex; flex-wrap: wrap; gap: 10px; }
  .cost-tag {
    background: #f0f9eb;
    color: #67c23a;
    padding: 4px 12px;
    border-radius: 4px;
    font-size: 13px;
    &.exclude { background: #fef0f0; color: #f56c6c; }
  }
}
.reviews-section {
  padding: 10px 0;
  .review-filter { margin-bottom: 20px; padding-bottom: 15px; border-bottom: 1px solid #eee; }
  .review-item {
    border-bottom: 1px solid #f0f0f0;
    padding: 20px 0;
    &:last-child { border-bottom: none; }
  }
  .review-header {
    display: flex;
    align-items: center;
    gap: 15px;
    margin-bottom: 15px;
    .review-info {
      flex: 1;
      .name-row {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 4px;
      }
      .nickname { font-weight: bold; color: #333; }
    }
    .date { color: #999; font-size: 13px; }
  }
  .review-tags {
    margin-bottom: 10px;
    .review-tag { margin-right: 8px; margin-bottom: 4px; }
  }
  .review-content { line-height: 1.6; color: #333; margin-bottom: 10px; }
  .review-images {
    display: flex;
    gap: 10px;
    flex-wrap: wrap;
    .review-img {
      width: 100px;
      height: 100px;
      border-radius: 4px;
      cursor: pointer;
    }
  }
}
@media (max-width: 768px) {
  .detail-header { grid-template-columns: 1fr; }
}
</style>
