<template>
  <div class="my-orders">
    <h3>我的订单</h3>
    
    <el-tabs v-model="activeStatus" @tab-change="fetchOrders">
      <el-tab-pane label="全部" name="" />
      <el-tab-pane label="待支付" name="pending" />
      <el-tab-pane label="已支付" name="paid" />
      <el-tab-pane label="已完成" name="completed" />
      <el-tab-pane label="已取消" name="cancelled" />
    </el-tabs>

    <div class="order-list" v-loading="loading">
      <div v-for="order in orderList" :key="order.orderNo" class="order-item">
        <div class="order-header">
          <span class="order-no">订单号：{{ order.orderNo }}</span>
          <span class="order-time">{{ order.createTime }}</span>
          <el-tag :type="statusMap[order.status].type">{{ statusMap[order.status].text }}</el-tag>
        </div>
        <div class="order-body">
          <img :src="resolveImage(order.lineCover, 'order-' + order.id)" class="line-cover" @error="onImgError" />
          <div class="order-info">
            <h4>{{ order.lineName }}</h4>
            <p>出发日期：{{ order.departureDate }}</p>
            <p>出行人数：{{ order.travelerCount }}人</p>
          </div>
          <div class="order-price">
            <span class="label">订单金额</span>
            <span class="amount">¥{{ order.totalPrice }}</span>
          </div>
        </div>
        <div class="order-footer">
          <el-button v-if="order.status === 'pending'" type="primary" @click="goPay(order.orderNo)">
            去支付
          </el-button>
          <el-button v-if="order.status === 'pending'" @click="handleCancel(order.orderNo)">
            取消订单
          </el-button>
          <el-button v-if="order.status === 'completed'" @click="goReview(order)">
            去评价
          </el-button>
          <el-button @click="goDetail(order.orderNo)">查看详情</el-button>
        </div>
      </div>

      <el-empty v-if="!loading && !orderList.length" description="暂无订单" />
    </div>

    <div class="pagination">
      <el-pagination
        v-model:current-page="pagination.page"
        :total="pagination.total"
        layout="prev, pager, next"
        @current-change="fetchOrders"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderList, cancelOrder } from '@/api/order'
import { resolveImage, localPhoto, FALLBACK_IMAGE } from '@/utils/image'

const router = useRouter()
const loading = ref(false)
const activeStatus = ref('')
const orderList = ref([])

const statusMap = {
  pending: { text: '待支付', type: 'warning' },
  paid: { text: '已支付', type: 'success' },
  completed: { text: '已完成', type: 'info' },
  cancelled: { text: '已取消', type: 'info' }
}

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

async function fetchOrders() {
  loading.value = true
  try {
    const data = await getOrderList({
      page: pagination.page,
      size: pagination.size,
      status: activeStatus.value || undefined
    })
    orderList.value = data.records
    pagination.total = data.total
  } catch {
    // 模拟数据
    orderList.value = [
      {
        orderNo: 'T1705123456789',
        lineName: '云南大理丽江双飞6日游',
        lineCover: localPhoto('order-200'),
        departureDate: '2026-02-15',
        travelerCount: 2,
        totalPrice: 5998,
        status: 'pending',
        createTime: '2026-01-15 10:30:00'
      },
      {
        orderNo: 'T1705123456788',
        lineName: '海南三亚5日游',
        lineCover: localPhoto('order-201'),
        departureDate: '2026-01-20',
        travelerCount: 1,
        totalPrice: 3599,
        status: 'completed',
        createTime: '2026-01-10 14:20:00'
      }
    ]
    pagination.total = 2
  } finally {
    loading.value = false
  }
}

function goPay(orderNo) {
  router.push(`/order/result/${orderNo}`)
}

function goDetail(orderNo) {
  router.push(`/order/result/${orderNo}`)
}

function goReview(order) {
  ElMessage.info('评价功能开发中')
}

async function handleCancel(orderNo) {
  try {
    await ElMessageBox.confirm('确定要取消订单吗？', '提示', { type: 'warning' })
    await cancelOrder(orderNo)
    ElMessage.success('订单已取消')
    fetchOrders()
  } catch (e) {
    if (e !== 'cancel') {
      const order = orderList.value.find(o => o.orderNo === orderNo)
      if (order) order.status = 'cancelled'
    }
  }
}

onMounted(fetchOrders)

const fallbackImg = FALLBACK_IMAGE
function onImgError(e) {
  e.target.src = fallbackImg
  e.target.onerror = null
}
</script>

<style lang="scss" scoped>
h3 { margin-bottom: 20px; }
.order-item {
  border: 1px solid #eee;
  border-radius: 8px;
  margin-bottom: 15px;
  overflow: hidden;
}
.order-header {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 12px 20px;
  background: #f9f9f9;
  .order-no { font-weight: bold; }
  .order-time { color: #999; flex: 1; }
}
.order-body {
  display: flex;
  align-items: center;
  padding: 20px;
  gap: 20px;
  .line-cover { width: 150px; height: 100px; object-fit: cover; border-radius: 4px; }
  .order-info { flex: 1; h4 { margin-bottom: 10px; } p { color: #666; margin: 5px 0; } }
  .order-price { text-align: right; .label { display: block; color: #999; margin-bottom: 5px; } .amount { font-size: 20px; color: #ff6600; font-weight: bold; } }
}
.order-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 15px 20px;
  border-top: 1px solid #eee;
}
.pagination { display: flex; justify-content: center; margin-top: 20px; }
</style>
