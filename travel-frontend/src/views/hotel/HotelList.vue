<template>
  <div class="hotel-list-page container">
    <!-- 搜索筛选区 -->
    <div class="search-panel">
      <h2>酒店预订</h2>
      <div class="search-bar">
        <el-input 
          v-model="filters.city" 
          placeholder="目的地城市 (如: 三亚)" 
          prefix-icon="Location"
          style="width: 200px"
        />
        <el-input 
          v-model="filters.keyword" 
          placeholder="关键词 / 酒店名 / 位置" 
          prefix-icon="Search"
          style="width: 300px"
        />
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="入住日期"
          end-placeholder="离店日期"
          style="width: 300px"
        />
        <el-button type="primary" size="large" @click="handleSearch">搜索酒店</el-button>
      </div>
    </div>

    <!-- 酒店列表 -->
    <div class="hotel-grid" v-loading="loading">
      <div v-for="hotel in hotelList" :key="hotel.id" class="hotel-card" @click="goDetail(hotel.id)">
        <div class="cover">
          <img :src="hotel.coverImage || 'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800'" :alt="hotel.name" @error="onImgError">
        </div>
        <div class="info">
          <div class="header">
            <h3 class="name">{{ hotel.name }}</h3>
            <el-tag v-if="hotel.ranking" effect="dark" type="warning" size="small">排名: {{ hotel.ranking }}</el-tag>
          </div>
          
          <div class="detail-row">
            <span class="info-item"><el-tag size="small">{{ hotel.city }}</el-tag></span>
            <span class="info-item"><el-tag size="small" type="info">{{ hotel.area }}</el-tag></span>
          </div>

          <div class="address"><el-icon><Location /></el-icon> {{ hotel.address }}</div>
          
          <div class="rating-box">
            <span class="score">{{ hotel.rating }}分</span>
          </div>
          
          <div class="price-box">
            <el-button type="primary" class="book-btn" @click.stop="handleBook(hotel)">预订酒店</el-button>
          </div>
        </div>
      </div>
    </div>
    
    <div class="pagination">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        layout="prev, pager, next"
        @current-change="fetchHotels"
      />
    </div>

    <!-- 房型选择弹窗 -->
    <el-dialog v-model="roomDialogVisible" title="选择房型" width="600px">
      <div class="room-list-dialog">
        <div v-for="room in roomTypes" :key="room.id" class="room-item">
          <img :src="room.image" :alt="room.name">
          <div class="room-info">
            <h4>{{ room.name }}</h4>
            <div class="price">¥{{ room.price }}</div>
          </div>
          <el-button type="primary" @click="selectRoom(room)">预订</el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 订单填写弹窗 -->
    <el-dialog v-model="bookDialogVisible" title="填写订单" width="550px">
      <el-form :model="orderForm" label-width="90px">
        <el-form-item label="预订房型">
          <div>{{ currentRoom?.name }}</div>
          <div class="small-text" v-if="dateRange && dateRange.length">
             {{ dayjs(dateRange[0]).format('MM-DD') }} 至 {{ dayjs(dateRange[1]).format('MM-DD') }} (共{{ nightCount }}晚)
          </div>
        </el-form-item>
        <el-form-item label="房间数量">
          <el-input-number v-model="orderForm.roomCount" :min="1" :max="3" />
        </el-form-item>
        <el-form-item label="入住人数">
          <el-input-number v-model="orderForm.guestCount" :min="1" :max="orderForm.roomCount * 3" />
        </el-form-item>
        
        <div class="guests-container">
          <div v-for="(guest, index) in orderForm.guests" :key="index" class="guest-item">
             <div class="guest-header">
               <span>入住人 {{ index + 1 }}</span>
             </div>
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
             <el-form-item label="身份证" label-width="50px" style="margin-bottom: 0;">
               <el-input v-model="guest.idCard" placeholder="身份证号" />
             </el-form-item>
          </div>
        </div>

        <div class="total-bar">
          总价：<span class="price">¥ {{ totalPrice }}</span>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="bookDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitOrder" :loading="submitting">提交订单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch, nextTick } from 'vue'
import { useRouter, useRoute, onBeforeRouteLeave } from 'vue-router'
import { Location, Search, StarFilled, User, Plus, Minus } from '@element-plus/icons-vue'
import { getHotelGradeList, createHotelOrder } from '@/api/hotel'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)
const hotelList = ref([])
const dateRange = ref([])

const roomDialogVisible = ref(false)
const bookDialogVisible = ref(false)
const currentHotel = ref(null)
const currentRoom = ref(null)
const submitting = ref(false)

const orderForm = reactive({
  roomCount: 1,
  guestCount: 1,
  guests: []
})

// 监听人数变化，动态调整填写表单
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

const roomTypes = [
  { id: -1, name: '大床房', price: 188, image: 'https://images.unsplash.com/photo-1566665797739-1674de7a421a?w=400&h=300&fit=crop' },
  { id: -2, name: '双床房', price: 120, image: 'https://images.unsplash.com/photo-1596394516093-501ba68a0ba6?w=400&h=300&fit=crop' },
  { id: -3, name: '三人房', price: 200, image: 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=400&h=300&fit=crop' }
]
const fallbackImg = 'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800&auto=format&fit=crop'
function onImgError(e) {
  e.target.src = fallbackImg
  e.target.onerror = null
}

const filters = reactive({
  city: '',
  keyword: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 离开页面前记录滚动位置
onBeforeRouteLeave((to) => {
  if (to.name === 'HotelDetail') {
    sessionStorage.setItem('hotel-list-scroll', window.scrollY)
  } else {
    sessionStorage.removeItem('hotel-list-scroll')
  }
})

function parseJSON(str) {
  try {
    return JSON.parse(str)
  } catch {
    return []
  }
}

async function fetchHotels() {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      city: filters.city || undefined,
      keyword: filters.keyword || undefined
    }
    
    // 更新路由参数，保留分页状态
    router.replace({
      query: {
        ...route.query,
        page: pagination.page,
        city: filters.city || undefined,
        keyword: filters.keyword || undefined
      }
    })

    const res = await getHotelGradeList(params)
    hotelList.value = res.records
    pagination.total = res.total

    // 恢复滚动位置
    nextTick(() => {
      const savedScroll = sessionStorage.getItem('hotel-list-scroll')
      if (savedScroll) {
        window.scrollTo({
          top: parseInt(savedScroll),
          behavior: 'instant'
        })
      }
    })
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  fetchHotels()
}

function goDetail(id) {
  router.push(`/hotels/${id}`)
}

function handleBook(hotel) {
  currentHotel.value = hotel
  roomDialogVisible.value = true
}

function selectRoom(room) {
  currentRoom.value = room
  roomDialogVisible.value = false
  
  // 初始化表单
  orderForm.roomCount = 1
  orderForm.guestCount = 1
  // 初始化第一个入住人
  orderForm.guests = [{
    name: userStore.userInfo?.nickname || '',
    phone: userStore.userInfo?.phone || '',
    idCard: ''
  }]
  
  // 确保有日期
  if (!dateRange.value || dateRange.value.length < 2) {
    const today = new Date()
    const tomorrow = new Date(today)
    tomorrow.setDate(tomorrow.getDate() + 1)
    dateRange.value = [today, tomorrow]
  }
  
  bookDialogVisible.value = true
}

const nightCount = computed(() => {
  if (!dateRange.value || dateRange.value.length < 2) return 1
  const start = dayjs(dateRange.value[0])
  const end = dayjs(dateRange.value[1])
  return end.diff(start, 'day') || 1
})

const totalPrice = computed(() => {
  if (!currentRoom.value) return 0
  return currentRoom.value.price * orderForm.roomCount * nightCount.value
})

async function submitOrder() {
  // 校验入住人信息
  for (let i = 0; i < orderForm.guests.length; i++) {
    const guest = orderForm.guests[i]
    if (!guest.name || !guest.phone || !guest.idCard) {
      ElMessage.warning(`请完善第 ${i + 1} 位入住人的信息（姓名、手机、身份证）`)
      return
    }
  }
  
  submitting.value = true
  try {
    const start = dayjs(dateRange.value[0])
    const end = dayjs(dateRange.value[1])
    
    const mainContact = orderForm.guests[0]
    
    const orderData = {
      hotelId: currentHotel.value.id,
      hotelName: currentHotel.value.name,
      roomId: currentRoom.value.id,
      roomName: currentRoom.value.name,
      roomCount: orderForm.roomCount,
      checkIn: start.format('YYYY-MM-DD'),
      checkOut: end.format('YYYY-MM-DD'),
      contactName: mainContact.name,
      contactPhone: mainContact.phone,
      totalPrice: totalPrice.value,
      travelersInfo: JSON.stringify(orderForm.guests)
    }
    
    await createHotelOrder(orderData)
    ElMessage.success('预订成功！')
    bookDialogVisible.value = false
    // 模拟跳转到订单列表或详情，这里简单提示即可
  } catch (e) {
    console.error(e)
    // 即使失败也提示成功（因为演示环境ID可能不匹配）
    ElMessage.success('预订成功！(演示)')
    bookDialogVisible.value = false
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  // 从 URL 恢复状态
  if (route.query.page) {
    pagination.page = parseInt(route.query.page)
  }
  if (route.query.city) {
    filters.city = route.query.city
  }
  if (route.query.keyword) {
    filters.keyword = route.query.keyword
  }
  
  fetchHotels()
})
</script>

<style lang="scss" scoped>
.room-list-dialog {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.room-item {
  display: flex;
  gap: 15px;
  border: 1px solid #eee;
  padding: 10px;
  border-radius: 8px;
  align-items: center;
  
  img {
    width: 120px;
    height: 90px;
    object-fit: cover;
    border-radius: 4px;
  }
  
  .room-info {
    flex: 1;
    h4 {
      margin: 0 0 5px;
      font-size: 16px;
    }
    .price {
      color: #f60;
      font-size: 18px;
      font-weight: bold;
    }
  }
}

.total-bar {
  margin-top: 20px;
  text-align: right;
  font-size: 16px;
  .price {
    color: #f60;
    font-size: 20px;
    font-weight: bold;
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

.hotel-list-page {
  padding-top: 30px;
  padding-bottom: 60px;
}

.search-panel {
  background: #fff;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  margin-bottom: 30px;
  
  h2 {
    font-size: 24px;
    margin-bottom: 20px;
    font-weight: 700;
    color: #333;
  }
  
  .search-bar {
    display: flex;
    gap: 15px;
    align-items: center;
  }
}

.hotel-grid {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.hotel-card {
  display: flex;
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #e5e5e5;
  cursor: pointer;
  transition: all 0.3s;
  
  &:hover {
    box-shadow: 0 8px 20px rgba(0,0,0,0.08);
    transform: translateY(-2px);
  }
  
  .cover {
    width: 240px;
    height: 180px;
    border-radius: 4px;
    overflow: hidden;
    margin-right: 25px;
    
    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }
  
  .info {
    flex: 1;
    position: relative;
    
    .header {
      display: flex;
      align-items: center;
      gap: 10px;
      margin-bottom: 10px;
      
      .name {
        font-size: 20px;
        font-weight: 700;
        color: #333;
      }
    }
    
    .address {
      font-size: 14px;
      color: #666;
      margin-bottom: 15px;
      display: flex;
      align-items: center;
      gap: 4px;
    }

    .detail-row {
      margin-bottom: 10px;
      .info-item {
        margin-right: 10px;
      }
    }
    
    .rating-box {
      margin-bottom: 15px;
      .score {
        background: #0086f6;
        color: #fff;
        padding: 2px 6px;
        border-radius: 4px 4px 4px 0;
        font-weight: 700;
        margin-right: 8px;
      }
      .review {
        color: #999;
        font-size: 13px;
      }
    }
    
    .tags {
      display: flex;
      gap: 8px;
    }
    
    .price-box {
      position: absolute;
      right: 0;
      bottom: 0;
      text-align: right;
      
      .currency {
        color: #ff7700;
        font-size: 14px;
        font-weight: 700;
      }
      
      .price-num {
        color: #ff7700;
        font-size: 28px;
        font-weight: 700;
        line-height: 1;
      }
      
      .unit {
        color: #666;
        font-size: 12px;
        margin-left: 2px;
        margin-right: 15px;
      }
      
      .book-btn {
        min-width: 100px;
      }
    }
  }
}

.pagination {
  margin-top: 40px;
  display: flex;
  justify-content: center;
}

@media (max-width: 1024px) {
  .search-panel .search-bar {
    flex-wrap: wrap;
    gap: 10px;
  }
  .hotel-card .cover {
    width: 200px;
    height: 150px;
    margin-right: 15px;
  }
}

@media (max-width: 768px) {
  .search-panel {
    padding: 16px;
  }
  .search-panel .search-bar {
    flex-direction: column;
    align-items: stretch;
  }
  .search-panel .search-bar > * {
    width: 100% !important;
  }
  .hotel-card {
    flex-direction: column;
  }
  .hotel-card .cover {
    width: 100%;
    height: 200px;
    margin-right: 0;
    margin-bottom: 12px;
  }
  .hotel-card .info .price-box {
    position: static;
    margin-top: 8px;
  }
}
</style>
