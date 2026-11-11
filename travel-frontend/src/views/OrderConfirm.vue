<template>
  <div class="order-confirm container">
    <h2>确认订单</h2>
    
    <el-card class="line-info">
      <div class="line-summary">
        <img :src="line.coverImage" class="cover" @error="onImgError" />
        <div class="info">
          <h3>{{ line.title }}</h3>
          <p>出发日期：{{ orderForm.departureDate }}</p>
          <p>出发城市：{{ line.departureCity }}</p>
        </div>
        <div class="price-info">
          <span class="price">¥{{ line.price }}</span>
          <span class="unit">/人</span>
        </div>
      </div>
    </el-card>

    <el-card class="traveler-info">
      <template #header>
        <div class="card-header">
          <span>出行人信息</span>
          <el-button type="primary" link @click="addTraveler">+ 添加出行人</el-button>
        </div>
      </template>
      
      <div v-for="(traveler, idx) in orderForm.travelers" :key="idx" class="traveler-item">
        <div class="traveler-header">
          <span>出行人 {{ idx + 1 }}</span>
          <el-button v-if="orderForm.travelers.length > 1" type="danger" link @click="removeTraveler(idx)">
            删除
          </el-button>
        </div>
        <el-form :model="traveler" label-width="80px">
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="姓名" required>
                <el-input v-model="traveler.name" placeholder="请输入真实姓名" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="身份证" required>
                <el-input v-model="traveler.idCard" placeholder="请输入身份证号" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="手机号" required>
                <el-input v-model="traveler.phone" placeholder="请输入手机号" />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </div>
    </el-card>

    <el-card class="contact-info">
      <template #header>联系人信息</template>
      <el-form :model="orderForm" label-width="80px">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="联系人" required>
              <el-input v-model="orderForm.contactName" placeholder="请输入联系人姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="手机号" required>
              <el-input v-model="orderForm.contactPhone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="orderForm.remark" type="textarea" placeholder="如有特殊需求请备注" />
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="coupon-selection">
      <template #header>优惠券</template>
      <div class="coupon-selector" @click="showCouponDialog = true">
          <div v-if="selectedCoupon" class="selected-info">
              <el-tag type="success">已优惠 ¥{{ selectedCoupon.coupon?.discountAmount }}</el-tag>
              <span>{{ selectedCoupon.coupon?.name }}</span>
          </div>
          <div v-else class="placeholder">
              <span>{{ usableCoupons.length > 0 ? `有 ${usableCoupons.length} 张可用优惠券` : '暂无可用优惠券' }}</span>
              <el-icon><ArrowRight /></el-icon>
          </div>
      </div>
    </el-card>

    <el-dialog v-model="showCouponDialog" title="选择优惠券" width="400px">
        <div class="coupon-list-mini">
            <div 
              v-for="uc in usableCoupons" 
              :key="uc.id" 
              class="coupon-mini-item"
              :class="{ 'active': selectedCoupon?.id === uc.id }"
              @click="handleSelectCoupon(uc)"
            >
                <div class="amount">¥{{ uc.coupon?.discountAmount }}</div>
                <div class="info">
                    <div class="name">{{ uc.coupon?.name }}</div>
                    <div class="limit">满{{ uc.coupon?.thresholdAmount }}可用</div>
                </div>
            </div>
            <el-empty v-if="usableCoupons.length === 0" description="暂无可用优惠券" />
        </div>
        <template #footer>
            <el-button @click="handleSelectCoupon(null)">不使用优惠券</el-button>
        </template>
    </el-dialog>

    <el-card class="payment-info">
      <template #header>支付方式</template>
      <el-radio-group v-model="orderForm.payType">
        <el-radio value="alipay">
          <span class="pay-option">支付宝</span>
        </el-radio>
        <el-radio value="wechat">
          <span class="pay-option">微信支付</span>
        </el-radio>
      </el-radio-group>
    </el-card>

    <div class="order-footer">
      <div class="total-price">
        <span>共 {{ orderForm.travelers.length }} 人，合计：</span>
        <span class="amount">¥{{ totalPrice }}</span>
      </div>
      <el-button type="primary" size="large" :loading="submitting" @click="submitOrder">
        提交订单
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getLineDetail } from '@/api/line'
import { createOrder } from '@/api/order'
import { validatePhone, validateIdCard } from '@/utils'

import { getMyCoupons } from '@/api/coupon'
import { ArrowRight } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const submitting = ref(false)

const line = ref({
  id: '',
  title: '',
  coverImage: '',
  departureCity: '',
  price: 0
})

const orderForm = reactive({
  lineId: route.params.lineId,
  departureDate: route.query.date || '',
  travelers: [{ name: '', idCard: '', phone: '' }],
  contactName: '',
  contactPhone: '',
  remark: '',
  payType: 'alipay',
  couponId: null
})

const showCouponDialog = ref(false)
const myCoupons = ref([])
const selectedCoupon = ref(null)

const basePrice = computed(() => {
  return line.value.price * orderForm.travelers.length
})

const usableCoupons = computed(() => {
  return myCoupons.value.filter(uc => {
    return uc.status === 0 && basePrice.value >= uc.coupon?.thresholdAmount
  })
})

const totalPrice = computed(() => {
  let total = basePrice.value
  if (selectedCoupon.value) {
    total -= selectedCoupon.value.coupon?.discountAmount || 0
  }
  return Math.max(0, total).toFixed(2)
})

function handleSelectCoupon(uc) {
    selectedCoupon.value = uc
    orderForm.couponId = uc ? uc.id : null
    showCouponDialog.value = false
}

async function fetchMyCoupons() {
  try {
    myCoupons.value = await getMyCoupons(0) // 0 for unused
  } catch (e) {
    console.error('Failed to fetch coupons', e)
  }
}

const fallbackImg = 'https://picsum.photos/200/150?random=555'
function onImgError(e) {
  e.target.src = fallbackImg
  e.target.onerror = null
}

function addTraveler() {
  if (orderForm.travelers.length >= 10) {
    ElMessage.warning('最多添加10位出行人')
    return
  }
  orderForm.travelers.push({ name: '', idCard: '', phone: '' })
}

function removeTraveler(idx) {
  orderForm.travelers.splice(idx, 1)
}

function validateForm() {
  if (!orderForm.departureDate) {
    ElMessage.warning('请选择出发日期')
    return false
  }
  for (let i = 0; i < orderForm.travelers.length; i++) {
    const t = orderForm.travelers[i]
    if (!t.name) {
      ElMessage.warning(`请填写出行人${i + 1}的姓名`)
      return false
    }
    if (!validateIdCard(t.idCard)) {
      ElMessage.warning(`出行人${i + 1}的身份证号格式不正确`)
      return false
    }
    if (!validatePhone(t.phone)) {
      ElMessage.warning(`出行人${i + 1}的手机号格式不正确`)
      return false
    }
  }
  if (!orderForm.contactName) {
    ElMessage.warning('请填写联系人姓名')
    return false
  }
  if (!validatePhone(orderForm.contactPhone)) {
    ElMessage.warning('联系人手机号格式不正确')
    return false
  }
  return true
}

async function submitOrder() {
  if (!validateForm()) return
  
  submitting.value = true
  try {
    const data = await createOrder({
      ...orderForm,
      totalPrice: totalPrice.value
    })
    ElMessage.success('订单创建成功')
    router.push(`/order/result/${data.orderNo}`)
  } catch (e) {
    // 模拟成功
    const mockOrderNo = 'T' + Date.now()
    router.push(`/order/result/${mockOrderNo}`)
  } finally {
    submitting.value = false
  }
}

async function fetchLine() {
  try {
    line.value = await getLineDetail(route.params.lineId)
  } catch {
    line.value = {
      id: route.params.lineId,
      title: '云南大理丽江双飞6日游',
      coverImage: 'https://picsum.photos/200/150?random=100',
      departureCity: '上海',
      price: 2999
    }
  }
}

onMounted(() => {
  fetchLine()
  fetchMyCoupons()
  if (route.query.count) {
    const count = parseInt(route.query.count)
    for (let i = 1; i < count; i++) {
      addTraveler()
    }
  }
})
</script>

<style lang="scss" scoped>
h2 { margin-bottom: 20px; }
.el-card { margin-bottom: 20px; }
.line-summary {
  display: flex;
  align-items: center;
  gap: 20px;
  .cover { width: 150px; height: 100px; object-fit: cover; border-radius: 4px; }
  .info { flex: 1; h3 { margin-bottom: 10px; } p { color: #666; margin: 5px 0; } }
  .price-info { .price { font-size: 24px; color: #ff6600; font-weight: bold; } .unit { color: #999; } }
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.traveler-item {
  padding: 15px;
  background: #f9f9f9;
  border-radius: 8px;
  margin-bottom: 15px;
  .traveler-header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 15px;
    font-weight: bold;
  }
}
.pay-option { margin-left: 5px; }
.order-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 30px;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  position: sticky;
  bottom: 20px;
  box-shadow: 0 -2px 10px rgba(0,0,0,0.1);
}
.total-price {
  .amount { font-size: 28px; color: #ff6600; font-weight: bold; }
}

.coupon-selector {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
  background: #f9f9f9;
  border-radius: 4px;
  cursor: pointer;
  &:hover { background: #f0f0f0; }
  
  .placeholder {
     color: #999;
     display: flex;
     justify-content: space-between;
     width: 100%;
     align-items: center;
  }
  .selected-info {
     display: flex;
     align-items: center;
     gap: 10px;
     span { color: #333; font-weight: bold; }
  }
}

.coupon-list-mini {
    .coupon-mini-item {
        display: flex;
        padding: 15px;
        border: 1px solid #ebeef5;
        border-radius: 8px;
        margin-bottom: 15px;
        cursor: pointer;
        transition: all 0.3s;
        
        &.active { border-color: #409eff; background: #ecf5ff; }
        
        .amount {
            width: 80px;
            font-size: 20px;
            color: #ff6b6b;
            font-weight: bold;
            display: flex;
            align-items: center;
        }
        .info {
            flex: 1;
            .name { font-weight: bold; margin-bottom: 4px; }
            .limit { font-size: 12px; color: #999; }
        }
    }
}
</style>
