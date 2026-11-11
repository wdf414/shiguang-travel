<template>
  <div class="order-manage">
    <el-card>
      <div class="toolbar">
        <el-input v-model="searchForm.keyword" placeholder="订单号/线路名/联系人" style="width: 250px;" clearable />
        <el-select v-model="searchForm.status" placeholder="订单状态" clearable style="width: 150px;">
          <el-option label="待支付" value="pending" />
          <el-option label="已支付" value="paid" />
          <el-option label="已完成" value="completed" />
          <el-option label="已取消" value="cancelled" />
          <el-option label="已退款" value="refunded" />
        </el-select>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </div>

      <el-table :data="orderList" v-loading="loading" style="margin-top: 20px;">
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="lineName" label="线路名称" min-width="200" />
        <el-table-column prop="contactName" label="联系人" width="100" />
        <el-table-column prop="travelerCount" label="人数" width="80" />
        <el-table-column prop="totalPrice" label="金额" width="100">
          <template #default="{ row }">¥{{ row.totalPrice }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status]?.type">
              {{ statusMap[row.status]?.text || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button 
              v-if="row.status === 'pending'" 
              type="success" 
              link 
              @click="handleConfirmPay(row)"
            >
              确认支付
            </el-button>
            <el-button 
              v-if="row.status === 'paid'" 
              type="warning" 
              link 
              @click="handleRefund(row)"
            >
              退款
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        layout="total, prev, pager, next"
        @current-change="fetchOrders"
        style="margin-top: 20px; justify-content: center;"
      />
    </el-card>

    <!-- 订单详情对话框 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="700px">
      <el-descriptions :column="2" border v-if="currentOrder">
        <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusMap[currentOrder.status]?.type">
            {{ statusMap[currentOrder.status]?.text }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="线路名称" :span="2">{{ currentOrder.lineName }}</el-descriptions-item>
        <el-descriptions-item label="出发日期">{{ currentOrder.departureDate }}</el-descriptions-item>
        <el-descriptions-item label="出行人数">{{ currentOrder.travelerCount }}人</el-descriptions-item>
        <el-descriptions-item label="订单金额">¥{{ currentOrder.totalPrice }}</el-descriptions-item>
        <el-descriptions-item label="支付方式">{{ currentOrder.payType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ currentOrder.contactName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentOrder.contactPhone }}</el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ currentOrder.createTime }}</el-descriptions-item>
        <el-descriptions-item label="支付时间" :span="2">{{ currentOrder.payTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentOrder.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 退款对话框 -->
    <el-dialog v-model="refundVisible" title="订单退款" width="500px">
      <el-form label-width="100px">
        <el-form-item label="退款原因">
          <el-input v-model="refundReason" type="textarea" :rows="4" placeholder="请输入退款原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="refundVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmRefund" :loading="refunding">确认退款</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminOrderList, getAdminOrderDetail, updateOrderStatus, refundOrder } from '@/api/admin'

const loading = ref(false)
const orderList = ref([])
const searchForm = reactive({ keyword: '', status: '' })
const pagination = reactive({ page: 1, size: 10, total: 0 })

const statusMap = {
  pending: { text: '待支付', type: 'warning' },
  paid: { text: '已支付', type: 'success' },
  completed: { text: '已完成', type: 'info' },
  cancelled: { text: '已取消', type: 'info' },
  refunded: { text: '已退款', type: 'danger' }
}

const detailVisible = ref(false)
const currentOrder = ref(null)

const refundVisible = ref(false)
const refundReason = ref('')
const refunding = ref(false)
const refundOrderId = ref(null)

async function fetchOrders() {
  loading.value = true
  try {
    const data = await getAdminOrderList({
      page: pagination.page,
      size: pagination.size,
      keyword: searchForm.keyword,
      status: searchForm.status
    })
    orderList.value = data.records
    pagination.total = data.total
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  fetchOrders()
}

async function handleView(row) {
  try {
    currentOrder.value = await getAdminOrderDetail(row.id)
    detailVisible.value = true
  } catch (e) {
    ElMessage.error('加载详情失败')
  }
}

async function handleConfirmPay(row) {
  try {
    await ElMessageBox.confirm('确认该订单已支付？', '提示', { type: 'warning' })
    await updateOrderStatus(row.id, 'paid')
    ElMessage.success('操作成功')
    fetchOrders()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

function handleRefund(row) {
  refundOrderId.value = row.id
  refundReason.value = ''
  refundVisible.value = true
}

async function confirmRefund() {
  if (!refundReason.value.trim()) {
    ElMessage.warning('请输入退款原因')
    return
  }
  refunding.value = true
  try {
    await refundOrder(refundOrderId.value, refundReason.value)
    ElMessage.success('退款成功')
    refundVisible.value = false
    fetchOrders()
  } catch (e) {
    ElMessage.error('退款失败')
  } finally {
    refunding.value = false
  }
}

onMounted(fetchOrders)
</script>

<style lang="scss" scoped>
.toolbar {
  display: flex;
  gap: 10px;
}
</style>
