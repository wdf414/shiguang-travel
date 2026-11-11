<template>
  <div class="my-coupons">
    <el-tabs v-model="activeTab" @tab-change="fetchCoupons">
      <el-tab-pane label="未使用" name="0">
        <div v-loading="loading" class="coupon-list">
          <el-row :gutter="20">
            <el-col :span="12" v-for="coupon in coupons" :key="coupon.id">
              <div class="coupon-item" :class="{ 'expired': isExpired(coupon.coupon) }">
                <div class="coupon-left">
                  <div class="amount">¥<span>{{ coupon.coupon?.discountAmount }}</span></div>
                  <div class="threshold">满{{ coupon.coupon?.thresholdAmount }}可用</div>
                </div>
                <div class="coupon-right">
                  <div class="name">{{ coupon.coupon?.name }}</div>
                  <div class="time">有效期至：{{ formatDate(coupon.coupon?.endTime) }}</div>
                  <div class="status-tag" v-if="isExpired(coupon.coupon)">已过期</div>
                </div>
              </div>
            </el-col>
          </el-row>
          <el-empty v-if="!loading && coupons.length === 0" description="暂无优惠券" />
        </div>
      </el-tab-pane>
      <el-tab-pane label="已使用" name="1">
        <div v-loading="loading" class="coupon-list">
           <el-row :gutter="20">
            <el-col :span="12" v-for="coupon in coupons" :key="coupon.id">
              <div class="coupon-item used">
                <div class="coupon-left">
                  <div class="amount">¥<span>{{ coupon.coupon?.discountAmount }}</span></div>
                  <div class="threshold">满{{ coupon.coupon?.thresholdAmount }}可用</div>
                </div>
                <div class="coupon-right">
                  <div class="name">{{ coupon.coupon?.name }}</div>
                  <div class="time">使用于订单：{{ coupon.orderId }}</div>
                  <div class="status-tag">已使用</div>
                </div>
              </div>
            </el-col>
          </el-row>
          <el-empty v-if="!loading && coupons.length === 0" description="暂无记录" />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 领券中心入口或推荐（可选） -->
    <el-card class="coupon-center-ad" style="margin-top: 30px;">
       <div class="ad-content">
          <span>更多优惠？去领券中心看看吧</span>
          <el-button type="primary" size="small" @click="showClaimDialog = true">立即前往</el-button>
       </div>
    </el-card>

    <el-dialog v-model="showClaimDialog" title="领券中心" width="500px">
        <div v-loading="loadingAvailable">
            <div v-for="c in availableCoupons" :key="c.id" class="claim-item">
                <div class="info">
                    <div class="name">{{ c.name }}</div>
                    <div class="desc">满{{ c.thresholdAmount }}减{{ c.discountAmount }}</div>
                </div>
                <el-button type="primary" size="small" @click="handleClaim(c.id)">立即领取</el-button>
            </div>
            <el-empty v-if="availableCoupons.length === 0" description="暂无可领优惠券" />
        </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyCoupons, getAvailableCoupons, claimCoupon } from '@/api/coupon'

const activeTab = ref('0')
const loading = ref(false)
const loadingAvailable = ref(false)
const coupons = ref([])
const availableCoupons = ref([])
const showClaimDialog = ref(false)

async function fetchCoupons() {
  loading.value = true
  try {
    const data = await getMyCoupons(activeTab.value)
    coupons.value = data
  } catch (e) {
    ElMessage.error('加载优惠券失败')
  } finally {
    loading.value = false
  }
}

async function fetchAvailable() {
    loadingAvailable.value = true
    try {
        availableCoupons.value = await getAvailableCoupons()
    } catch (e) {
        ElMessage.error('加载可领券失败')
    } finally {
        loadingAvailable.value = false
    }
}

async function handleClaim(id) {
    try {
        await claimCoupon(id)
        ElMessage.success('领取成功')
        fetchCoupons()
        fetchAvailable()
    } catch (e) {
        ElMessage.error(e.message || '领取失败')
    }
}

function formatDate(date) {
  if (!date) return ''
  return date.substring(0, 10)
}

function isExpired(coupon) {
  if (!coupon) return false
  return new Date(coupon.endTime) < new Date()
}

onMounted(() => {
    fetchCoupons()
    fetchAvailable()
})
</script>

<style lang="scss" scoped>
.coupon-list {
  padding: 10px 0;
}
.coupon-item {
  display: flex;
  height: 100px;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
  border: 1px solid #f0f0f0;
  
  .coupon-left {
    width: 120px;
    background: linear-gradient(135deg, #ff6b6b, #ff8e8e);
    color: #fff;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    
    .amount {
      font-size: 16px;
      span { font-size: 32px; font-weight: bold; }
    }
    .threshold {
      font-size: 12px;
      opacity: 0.9;
    }
  }
  
  .coupon-right {
    flex: 1;
    padding: 15px;
    display: flex;
    flex-direction: column;
    justify-content: center;
    position: relative;
    
    .name {
      font-size: 16px;
      font-weight: bold;
      color: #333;
      margin-bottom: 8px;
    }
    .time {
      font-size: 12px;
      color: #999;
    }
    .status-tag {
      position: absolute;
      right: 15px;
      top: 15px;
      font-size: 12px;
      color: #ff6b6b;
      border: 1px solid #ff6b6b;
      padding: 2px 6px;
      border-radius: 4px;
      transform: rotate(15deg);
    }
  }

  &.used {
     .coupon-left { background: #dcdfe6; }
     .status-tag { color: #909399; border-color: #909399; }
  }
  &.expired {
     .coupon-left { background: #dcdfe6; }
     .status-tag { color: #f56c6c; border-color: #f56c6c; }
  }
}

.claim-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 15px;
    border-bottom: 1px solid #f5f5f5;
    &:last-child { border-bottom: none; }
    
    .name { font-weight: bold; }
    .desc { font-size: 12px; color: #666; margin-top: 4px; }
}

.coupon-center-ad {
    .ad-content {
        display: flex;
        justify-content: space-between;
        align-items: center;
        color: #666;
    }
}
</style>
