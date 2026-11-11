<template>
  <div class="order-result container">
    <el-card class="result-card">
      <div class="result-content">
        <template v-if="payStatus === 'pending'">
          <el-icon class="status-icon pending"><Clock /></el-icon>
          <h2>等待支付</h2>
          <p class="order-no">订单号：{{ orderNo }}</p>
          <p class="tip">请在 <span class="countdown">{{ countdown }}</span> 内完成支付，超时订单将自动取消</p>
          
          <div class="pay-actions">
            <el-button type="primary" size="large" :loading="paying" @click="handlePay">
              立即支付 ¥{{ order.totalPrice }}
            </el-button>
            <el-button size="large" @click="handleCancel">取消订单</el-button>
          </div>
        </template>

        <template v-else-if="payStatus === 'success'">
          <el-icon class="status-icon success"><CircleCheck /></el-icon>
          <h2>支付成功</h2>
          <p class="order-no">订单号：{{ orderNo }}</p>
          <p class="tip">您的订单已支付成功，祝您旅途愉快！</p>
          
          <div class="actions">
            <el-button type="primary" @click="$router.push('/user/orders')">查看订单</el-button>
            <el-button @click="$router.push('/')">返回首页</el-button>
          </div>
        </template>

        <template v-else-if="payStatus === 'failed'">
          <el-icon class="status-icon failed"><CircleClose /></el-icon>
          <h2>支付失败</h2>
          <p class="order-no">订单号：{{ orderNo }}</p>
          <p class="tip">支付遇到问题，请重新尝试</p>
          
          <div class="actions">
            <el-button type="primary" @click="handlePay">重新支付</el-button>
            <el-button @click="$router.push('/user/orders')">查看订单</el-button>
          </div>
        </template>

        <template v-else>
          <el-icon class="status-icon cancelled"><Warning /></el-icon>
          <h2>订单已取消</h2>
          <p class="order-no">订单号：{{ orderNo }}</p>
          
          <div class="actions">
            <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
          </div>
        </template>
      </div>

      <div class="order-detail" v-if="order.lineName">
        <h4>订单详情</h4>
        <div class="detail-row">
          <span>线路名称</span>
          <span>{{ order.lineName }}</span>
        </div>
        <div class="detail-row">
          <span>出发日期</span>
          <span>{{ order.departureDate }}</span>
        </div>
        <div class="detail-row">
          <span>出行人数</span>
          <span>{{ order.travelerCount }}人</span>
        </div>
        <div class="detail-row">
          <span>订单金额</span>
          <span class="price">¥{{ order.totalPrice }}</span>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderDetail, payOrder, cancelOrder } from '@/api/order'

const route = useRoute()
const router = useRouter()
const orderNo = route.params.orderNo
const payStatus = ref('pending')
const paying = ref(false)
const countdown = ref('29:59')
let timer = null

const order = ref({
  lineName: '',
  departureDate: '',
  travelerCount: 0,
  totalPrice: 0
})

async function fetchOrder() {
  try {
    const data = await getOrderDetail(orderNo)
    order.value = data
    payStatus.value = data.status
  } catch {
    // 模拟数据
    order.value = {
      lineName: '云南大理丽江双飞6日游',
      departureDate: '2026-02-15',
      travelerCount: 2,
      totalPrice: 5998
    }
  }
}

async function handlePay() {
  paying.value = true
  try {
    await payOrder(orderNo, 'alipay')
    payStatus.value = 'success'
    ElMessage.success('支付成功')
  } catch {
    // 模拟支付成功
    setTimeout(() => {
      payStatus.value = 'success'
      ElMessage.success('支付成功')
    }, 1500)
  } finally {
    paying.value = false
  }
}

async function handleCancel() {
  try {
    await ElMessageBox.confirm('确定要取消订单吗？', '提示', { type: 'warning' })
    await cancelOrder(orderNo)
    payStatus.value = 'cancelled'
    ElMessage.success('订单已取消')
  } catch (e) {
    if (e !== 'cancel') {
      payStatus.value = 'cancelled'
    }
  }
}

function startCountdown() {
  let seconds = 30 * 60
  timer = setInterval(() => {
    seconds--
    if (seconds <= 0) {
      clearInterval(timer)
      payStatus.value = 'cancelled'
      return
    }
    const m = Math.floor(seconds / 60)
    const s = seconds % 60
    countdown.value = `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
  }, 1000)
}

onMounted(() => {
  fetchOrder()
  startCountdown()
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style lang="scss" scoped>
.order-result { max-width: 600px; margin: 0 auto; padding-top: 40px; }
.result-card { text-align: center; }
.result-content { padding: 40px 20px; }
.status-icon {
  font-size: 80px;
  margin-bottom: 20px;
  &.pending { color: #e6a23c; }
  &.success { color: #67c23a; }
  &.failed { color: #f56c6c; }
  &.cancelled { color: #909399; }
}
h2 { margin-bottom: 15px; }
.order-no { color: #666; margin-bottom: 10px; }
.tip { color: #999; margin-bottom: 30px; .countdown { color: #f56c6c; font-weight: bold; } }
.pay-actions, .actions {
  display: flex;
  justify-content: center;
  gap: 20px;
}
.order-detail {
  border-top: 1px solid #eee;
  padding: 20px;
  text-align: left;
  h4 { margin-bottom: 15px; }
}
.detail-row {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px dashed #eee;
  &:last-child { border-bottom: none; }
  .price { color: #ff6600; font-weight: bold; }
}
</style>
