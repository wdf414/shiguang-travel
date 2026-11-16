<template>
  <div class="hotel-detail-page container" v-loading="loading">
    <template v-if="hotel">
      <!-- 头部信息 -->
      <div class="hotel-header-card">
        <div class="left-col">
          <div class="title-row">
            <h1>{{ hotel.name }}</h1>
            <div class="stars">
              <el-icon v-for="n in hotel.star" :key="n" color="#FF9900"><StarFilled /></el-icon>
            </div>
          </div>
          <div class="address"><el-icon><Location /></el-icon> {{ hotel.address }}</div>
          <div class="tags" v-if="hotel.tags">
            <el-tag v-for="tag in parseJSON(hotel.tags)" :key="tag" type="info" effect="plain">{{ tag }}</el-tag>
          </div>
        </div>
        <div class="right-col">
          <div class="price-box">
            <span class="currency">¥</span>
            <span class="num">{{ hotel.priceStart }}</span>
            <span class="unit">起</span>
          </div>
          <el-button type="primary" size="large" @click="scrollToRooms">立即预订</el-button>
        </div>
      </div>

      <!-- 图片与介绍 -->
      <div class="detail-content">
        <div class="gallery">
          <div class="main-img">
            <img :src="resolveImage(hotel.coverImage, 'hotel-' + hotel.id)" :alt="hotel.name" @error="onImgError">
          </div>
          <div class="sub-imgs">
            <template v-if="hotel.images && parseJSON(hotel.images).length">
              <div v-for="(img, idx) in parseJSON(hotel.images).slice(0, 3)" :key="idx" class="sub-item">
                <img :src="resolveImage(img, 'hotel-' + hotel.id + '-sub-' + idx)" @error="onImgError">
              </div>
            </template>
            <template v-else>
              <div v-for="(img, idx) in [hotel.coverImage, hotel.coverImage, hotel.coverImage]" :key="idx" class="sub-item">
                <img :src="resolveImage(img, 'hotel-' + hotel.id + '-sub-' + idx)" @error="onImgError">
              </div>
            </template>
          </div>
        </div>
        
        <div class="intro-box">
          <h3>酒店介绍</h3>
          <p class="desc">{{ hotel.description || '暂无介绍' }}</p>
          
          <h3>设施服务</h3>
          <div class="facilities">
            <span class="fac-item"><el-icon><Bicycle /></el-icon> 免费停车</span>
            <span class="fac-item"><el-icon><Coffee /></el-icon> 餐厅</span>
            <span class="fac-item"><el-icon><Connection /></el-icon> WiFi</span>
          </div>
        </div>
      </div>

      <!-- 房型列表 -->
      <div class="room-section" id="room-section">
        <h3>选择房型</h3>
        
        <div class="date-picker-bar">
          <span>入住时段：</span>
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="入住日期"
            end-placeholder="离店日期"
            :disabled-date="disabledDate"
          />
        </div>

        <div class="room-list">
          <div v-for="room in rooms" :key="room.id" class="room-item">
            <div class="room-img">
              <img :src="resolveImage(room.image, 'room-' + room.id)" :alt="room.name" @error="onImgError">
            </div>
            <div class="room-info">
              <h4>{{ room.name }}</h4>
              <div class="attrs">
                <span>{{ room.bedType }}</span>
                <span class="divider">|</span>
                <span>{{ room.breakfast }}</span>
                <span class="divider">|</span>
                <span>{{ room.people }}人入住</span>
              </div>
            </div>
            <div class="room-price">
              <div class="price">
                <span class="currency">¥</span>
                <span class="num">{{ room.price }}</span>
              </div>
              <el-button 
                type="primary" 
                :disabled="room.stock <= 0" 
                @click="openBookDialog(room)"
              >
                {{ room.stock > 0 ? '预订' : '满房' }}
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </template>

    <!-- 评价列表 -->
    <div class="reviews-section card">
      <div class="section-header">
        <h3>住客评价</h3>
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
                {{ review.userName || review.nickname }}
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

    <ReviewDialog
      v-if="hotel"
      v-model="reviewDialogVisible"
      type="hotel"
      :id="hotel.id"
      @success="handleReviewSuccess"
    />

    <!-- 预订弹窗 -->
    <el-dialog v-model="bookVisible" title="填写订单" width="600px">
      <el-form :model="orderForm" label-width="90px">
        <el-form-item label="房型信息">
          <div>{{ currentRoom?.name }}</div>
          <div class="small-text">{{ formatDate(dateRange?.[0]) }} 至 {{ formatDate(dateRange?.[1]) }} (共{{ nightCount }}晚)</div>
        </el-form-item>
        <el-form-item label="房间数量">
          <el-input-number v-model="orderForm.roomCount" :min="1" :max="3" />
        </el-form-item>
        <el-form-item label="入住人数">
          <el-input-number v-model="orderForm.guestCount" :min="1" :max="orderForm.roomCount * 3" />
        </el-form-item>

        <div class="guests-container">
          <div v-for="(guest, index) in orderForm.guests" :key="index" class="guest-item">
            <div class="guest-header">入住人 {{ index + 1 }}</div>
            <el-row :gutter="10">
              <el-col :span="12">
                <el-form-item label="姓名" label-width="50px" style="margin-bottom: 10px;">
                  <el-input v-model="guest.name" placeholder="姓名" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="手机" label-width="50px" style="margin-bottom: 10px;">
                  <el-input v-model="guest.phone" placeholder="手机号" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="身份证" label-width="64px" style="margin-bottom: 0;">
              <el-input v-model="guest.idCard" placeholder="身份证号" />
            </el-form-item>
          </div>
        </div>

        <div class="total-bar">
          总价：<span class="price">¥ {{ totalPrice }}</span>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="bookVisible = false">取消</el-button>
        <el-button type="primary" @click="submitOrder" :loading="submitting">提交订单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Location, StarFilled, Bicycle, Coffee, Connection } from '@element-plus/icons-vue'
import { getHotelDetail, getHotelRooms, createHotelOrder } from '@/api/hotel'
import { getReviewList } from '@/api/review'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import ReviewDialog from '@/components/ReviewDialog.vue'
import dayjs from 'dayjs'
import { resolveImage, FALLBACK_IMAGE } from '@/utils/image'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const hotel = ref(null)
const rooms = ref([])
const reviews = ref([])
const onlyStudent = ref(false)
const reviewDialogVisible = ref(false)
const bookVisible = ref(false)
const submitting = ref(false)
const currentRoom = ref(null)

// 默认日期：今天入住，明天离店
const today = new Date()
const tomorrow = new Date(today)
tomorrow.setDate(tomorrow.getDate() + 1)
const dateRange = ref([today, tomorrow])

const orderForm = reactive({
  roomCount: 1,
  guestCount: 1,
  guests: []
})

// 入住人数变化时动态增减入住人表单
watch(() => orderForm.guestCount, (val) => {
  const currentLen = orderForm.guests.length
  if (val > currentLen) {
    for (let i = 0; i < val - currentLen; i++) {
      orderForm.guests.push({ name: '', phone: '', idCard: '' })
    }
  } else if (val < currentLen) {
    orderForm.guests.splice(val)
  }
})
function onImgError(e) {
  e.target.src = FALLBACK_IMAGE
  e.target.onerror = null
}

function parseJSON(str) {
  if (!str) return []
  if (typeof str === 'object') return str
  try { return JSON.parse(str) } catch { return [] }
}

function formatDate(date) {
  return date ? dayjs(date).format('YYYY-MM-DD') : ''
}

async function fetchReviews() {
  try {
    const params = {
      page: 1,
      size: 10,
      hotelId: route.params.id,
      onlyStudent: onlyStudent.value
    }
    const data = await getReviewList(null, params)
    reviews.value = data.records
  } catch (e) {
    console.error('Fetch reviews error', e)
    reviews.value = []
  }
}

function disabledDate(time) {
  return time.getTime() < Date.now() - 8.64e7
}

const nightCount = computed(() => {
  if (!dateRange.value || dateRange.value.length < 2) return 0
  const start = dayjs(dateRange.value[0])
  const end = dayjs(dateRange.value[1])
  return end.diff(start, 'day') || 1
})

const totalPrice = computed(() => {
  if (!currentRoom.value) return 0
  return currentRoom.value.price * orderForm.roomCount * nightCount.value
})

async function fetchData() {
  loading.value = true
  try {
    const id = route.params.id
    hotel.value = await getHotelDetail(id)
    rooms.value = await getHotelRooms(id)
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function scrollToRooms() {
  document.getElementById('room-section').scrollIntoView({ behavior: 'smooth' })
}

function openBookDialog(room) {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  currentRoom.value = room
  orderForm.roomCount = 1
  orderForm.guestCount = 1
  // 第一位入住人默认带入当前登录用户信息
  orderForm.guests = [{
    name: userStore.userInfo?.nickname || '',
    phone: userStore.userInfo?.phone || '',
    idCard: ''
  }]
  bookVisible.value = true
}

async function submitOrder() {
  // 校验每位入住人的姓名、手机号和身份证
  for (let i = 0; i < orderForm.guests.length; i++) {
    const guest = orderForm.guests[i]
    if (!guest.name || !guest.phone || !guest.idCard) {
      return ElMessage.warning(`请完善第 ${i + 1} 位入住人的信息（姓名、手机、身份证）`)
    }
  }
  if (!orderForm.guests.length) {
    return ElMessage.warning('请至少填写一位入住人')
  }

  submitting.value = true
  try {
    const mainContact = orderForm.guests[0]
    const orderData = {
      hotelId: hotel.value.id,
      hotelName: hotel.value.name,
      roomId: currentRoom.value.id,
      roomName: currentRoom.value.name,
      checkIn: dayjs(dateRange.value[0]).format('YYYY-MM-DD'),
      checkOut: dayjs(dateRange.value[1]).format('YYYY-MM-DD'),
      roomCount: orderForm.roomCount,
      contactName: mainContact.name,
      contactPhone: mainContact.phone,
      travelersInfo: JSON.stringify(orderForm.guests),
      totalPrice: totalPrice.value
    }
    
    const res = await createHotelOrder(orderData)
    bookVisible.value = false
    ElMessageBox.alert(`预订成功！订单号：${res.orderNo}`, '成功', {
      confirmButtonText: '查看订单',
      callback: () => router.push('/user/orders')
    })
  } catch (e) {
    ElMessage.error(e.message || '预订失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchData()
  fetchReviews()
})

function handleReviewSuccess() {
  fetchReviews()
}
</script>

<style lang="scss" scoped>
.hotel-detail-page {
  padding-top: 30px;
  padding-bottom: 60px;
}

.hotel-header-card {
  background: #fff;
  padding: 30px;
  border-radius: 8px;
  display: flex;
  justify-content: space-between;
  margin-bottom: 30px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  
  .left-col {
    .title-row {
      display: flex;
      align-items: center;
      gap: 15px;
      margin-bottom: 10px;
      h1 { margin: 0; font-size: 24px; color: #333; }
    }
    .address { color: #666; margin-bottom: 10px; display: flex; align-items: center; gap: 5px; }
    .tags { display: flex; gap: 8px; }
  }
  
  .right-col {
    text-align: right;
    .price-box {
      margin-bottom: 10px;
      color: #ff7700;
      .currency { font-size: 16px; font-weight: 700; }
      .num { font-size: 32px; font-weight: 700; }
      .unit { font-size: 12px; color: #999; margin-left: 2px; }
    }
  }
}

.detail-content {
  display: grid;
  grid-template-columns: 7fr 3fr;
  gap: 30px;
  margin-bottom: 40px;
  
  .gallery {
    height: 400px;
    display: flex;
    gap: 10px;
    
    .main-img {
      flex: 3;
      border-radius: 8px;
      overflow: hidden;
      img { width: 100%; height: 100%; object-fit: cover; }
    }
    .sub-imgs {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 10px;
      .sub-item {
        flex: 1;
        border-radius: 8px;
        overflow: hidden;
        img { width: 100%; height: 100%; object-fit: cover; }
      }
    }
  }
  
  .intro-box {
    background: #fff;
    padding: 25px;
    border-radius: 8px;
    height: fit-content;
    
    h3 { font-size: 18px; margin-bottom: 15px; border-left: 4px solid #0086f6; padding-left: 10px; }
    .desc { font-size: 14px; color: #666; line-height: 1.6; margin-bottom: 20px; }
    .facilities {
      display: flex;
      flex-wrap: wrap;
      gap: 15px;
      .fac-item {
        display: flex;
        align-items: center;
        gap: 5px;
        color: #666;
        font-size: 13px;
      }
    }
  }
}

.room-section {
  background: #fff;
  padding: 30px;
  border-radius: 8px;
  
  h3 { font-size: 20px; margin-bottom: 20px; }
  
  .date-picker-bar {
    background: #f5f7fa;
    padding: 15px;
    border-radius: 4px;
    margin-bottom: 20px;
    display: flex;
    align-items: center;
    gap: 10px;
  }
  
  .room-list {
    .room-item {
      display: flex;
      padding: 20px 0;
      border-bottom: 1px solid #eee;
      
      &:last-child { border-bottom: none; }
      
      .room-img {
        width: 120px;
        height: 90px;
        border-radius: 4px;
        overflow: hidden;
        margin-right: 20px;
        img { width: 100%; height: 100%; object-fit: cover; }
      }
      
      .room-info {
        flex: 1;
        h4 { font-size: 16px; margin: 0 0 10px; font-weight: 600; }
        .attrs {
          color: #666;
          font-size: 13px;
          .divider { margin: 0 8px; color: #ddd; }
        }
      }
      
      .room-price {
        text-align: right;
        .price {
          color: #ff7700;
          margin-bottom: 10px;
          font-weight: 700;
          .currency { font-size: 14px; }
          .num { font-size: 24px; }
        }
      }
    }
  }
}

.reviews-section {
  background: #fff;
  padding: 30px;
  border-radius: 8px;
  margin-top: 20px;
  
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    h3 { margin: 0; font-size: 20px; }
    .filters { display: flex; align-items: center; gap: 15px; }
  }

  .review-item {
    padding: 20px 0;
    border-bottom: 1px solid #eee;
    &:last-child { border-bottom: none; }
    
    .review-user {
      display: flex;
      align-items: center;
      gap: 15px;
      margin-bottom: 10px;
      
      .user-info {
        flex: 1;
        .name { font-weight: bold; display: flex; align-items: center; gap: 8px; }
        .date { font-size: 12px; color: #999; margin-top: 4px; }
      }
    }
    
    .review-content { line-height: 1.6; margin-bottom: 10px; }
    
    .review-tags {
      margin-bottom: 10px;
      .review-tag { margin-right: 8px; margin-bottom: 8px; }
    }
    
    .review-images {
      display: flex;
      flex-wrap: wrap;
      gap: 10px;
      .review-img {
        width: 100px;
        height: 100px;
        border-radius: 4px;
        cursor: pointer;
      }
    }
  }
}

.guests-container {
  max-height: 300px;
  overflow-y: auto;
  border: 1px solid #eee;
  padding: 10px;
  border-radius: 4px;
}

.guest-item {
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px dashed #eee;

  &:last-child {
    margin-bottom: 0;
    padding-bottom: 0;
    border-bottom: none;
  }
}

.guest-header {
  font-weight: bold;
  margin-bottom: 10px;
  color: #666;
  font-size: 14px;
  background: #f5f7fa;
  padding: 5px 10px;
  border-radius: 4px;
}

.total-bar {
  margin-top: 20px;
  text-align: right;
  font-size: 16px;
  .price { color: #ff6600; font-size: 24px; font-weight: bold; }
}

.small-text { color: #999; font-size: 12px; margin-top: 4px; }
</style>
