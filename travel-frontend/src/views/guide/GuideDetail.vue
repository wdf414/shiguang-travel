<template>
  <div class="guide-detail container" v-loading="loading">
    <div v-if="guide" class="detail-content">
      <div class="header">
        <h1>{{ guide.title }}</h1>
        <div class="status-tags">
          <el-tag 
            v-if="guide.status === 0" 
            type="info" 
            size="large"
          >
            草稿
          </el-tag>
          <el-tag 
            v-else 
            type="success" 
            size="large"
          >
            已发布
          </el-tag>
        </div>
      </div>

      <div class="author-info">
        <div class="author-avatar">
          <el-avatar :size="60" :src="guide.avatar" />
        </div>
        <div class="author-details">
          <div class="author-name">{{ guide.authorName || '匿名' }}</div>
          <div class="author-stats">
            <span>浏览: {{ guide.viewCount || 0 }}次</span>
            <span>点赞: {{ guide.likeCount || 0 }}次</span>
          </div>
        </div>
      </div>

      <div class="content-section">
        <h3>攻略详情</h3>
        <div class="content">{{ guide.content }}</div>
      </div>

      <div class="reviews-section">
        <div class="section-header">
          <h3>评价</h3>
          <div class="filters">
            <el-checkbox v-model="onlyStudent" @change="fetchReviews">
              只看大学生评价
            </el-checkbox>
            <el-button type="primary" size="small" @click="reviewDialogVisible = true">
              写评价
            </el-button>
          </div>
        </div>
        
        <div class="review-list">
          <el-empty v-if="!reviews.length" description="暂无评价" />
          <div v-else v-for="review in reviews" :key="review.id" class="review-item">
            <div class="review-user">
              <el-avatar :size="40" :src="review.userAvatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
              <div class="user-info">
                <div class="name">
                  {{ review.userName }}
                  <el-tag v-if="review.isStudent" size="small" type="success" effect="plain">大学生认证</el-tag>
                </div>
                <div class="date">{{ review.createTime }}</div>
              </div>
              <el-rate v-model="review.rating" disabled show-score text-color="#ff9900" />
            </div>
            <div class="review-content">{{ review.content }}</div>
            
            <div v-if="review.tags" class="review-tags">
              <el-tag 
                v-for="tag in (typeof review.tags === 'string' ? review.tags.split(',') : [])" 
                :key="tag"
                size="small"
                effect="plain"
                class="review-tag"
              >
                {{ tag }}
              </el-tag>
            </div>

            <div v-if="parseJSON(review.images).length" class="review-images">
              <el-image 
                v-for="(img, index) in parseJSON(review.images)" 
                :key="index"
                :src="img"
                :preview-src-list="parseJSON(review.images)"
                fit="cover"
                class="review-img"
                @error="onImgError"
              />
            </div>
          </div>
        </div>
      </div>

      <div class="actions">
        <el-button 
          v-if="isOwner" 
          type="primary" 
          @click="editGuide"
        >
          编辑
        </el-button>
        <el-button 
          v-else 
          type="primary" 
          @click="likeGuide"
          :loading="liking"
        >
          点赞
        </el-button>
        <el-button @click="goBack">返回</el-button>
      </div>
    </div>

    <ReviewDialog
      v-if="guide"
      v-model="reviewDialogVisible"
      type="guide"
      :id="guide.id"
      @success="handleReviewSuccess"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getGuideDetail as apiGetGuideDetail, likeGuide as apiLikeGuide } from '@/api/guide'
import { getReviewList } from '@/api/review'
import { useUserStore } from '@/stores/user'
import ReviewDialog from '@/components/ReviewDialog.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const liking = ref(false)
const guide = ref(null)
const reviews = ref([])
const onlyStudent = ref(false)
const reviewDialogVisible = ref(false)
const fallbackImg = 'https://picsum.photos/800/400?random=88'

onMounted(() => {
  fetchDetail()
  fetchReviews()
})

function onImgError(e) {
  e.target.src = fallbackImg
  e.target.onerror = null
}

function parseJSON(str) {
  if (!str) return []
  if (typeof str === 'object') return str
  try { return JSON.parse(str) } catch { return [] }
}

function handleReviewSuccess() {
  fetchReviews()
}

async function fetchReviews() {
  try {
    const params = {
      page: 1,
      size: 10,
      guideId: route.params.id,
      onlyStudent: onlyStudent.value
    }
    const data = await getReviewList(null, params)
    reviews.value = data?.records || []
  } catch (e) {
    console.error('Fetch reviews error', e)
    reviews.value = []
  }
}

async function fetchDetail() {
  try {
    loading.value = true
    const id = route.params.id
    const res = await apiGetGuideDetail(id)
    guide.value = res
  } catch (error) {
    console.error('获取导游攻略详情失败:', error)
    ElMessage.error('获取详情失败')
  } finally {
    loading.value = false
  }
}

const isOwner = computed(() => {
  return userStore.userInfo?.id === guide.value?.userId
})

async function likeGuide() {
  try {
    liking.value = true
    await apiLikeGuide(route.params.id)
    ElMessage.success('点赞成功！')
    // 更新本地数据
    guide.value.likeCount = (guide.value.likeCount || 0) + 1
  } catch (error) {
    ElMessage.error(error.message || '点赞失败')
  } finally {
    liking.value = false
  }
}

function editGuide() {
  router.push(`/guide/edit/${route.params.id}`)
}

function goBack() {
  router.go(-1)
}
</script>

<style scoped>
.guide-detail {
  padding: 20px 0;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.header h1 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.author-info {
  display: flex;
  gap: 15px;
  margin-bottom: 30px;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.author-details {
  flex: 1;
}

.author-name {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 5px;
}

.author-stats {
  display: flex;
  gap: 15px;
  font-size: 14px;
  color: #909399;
}

.content-section {
  margin-bottom: 30px;
}

.content-section h3 {
  margin-bottom: 15px;
  font-size: 18px;
  color: #303133;
}

.content {
  padding: 15px;
  background-color: #fafafa;
  border-radius: 8px;
  line-height: 1.6;
  white-space: pre-wrap;
}

.actions {
  display: flex;
  gap: 15px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}
</style>