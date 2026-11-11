<template>
  <div class="coupon-manage">
    <el-card>
      <div class="toolbar">
        <el-input v-model="searchForm.name" placeholder="搜索优惠券名称" style="width: 200px;" clearable />
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button type="success" @click="handleAdd">新增优惠券</el-button>
      </div>

      <el-table :data="couponList" v-loading="loading" style="margin-top: 20px;">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="优惠券名称" min-width="150" />
        <el-table-column label="优惠内容" width="150">
          <template #default="{ row }">
            满 ¥{{ row.thresholdAmount }} 减 ¥{{ row.discountAmount }}
          </template>
        </el-table-column>
        <el-table-column label="有效期" width="320">
          <template #default="{ row }">
            {{ formatTime(row.startTime) }} 至 {{ formatTime(row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="总量" width="100" />
        <el-table-column prop="issuedCount" label="已领" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '已上线' : '已下线' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        layout="total, prev, pager, next"
        @current-change="fetchCoupons"
        style="margin-top: 20px; justify-content: center;"
      />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="dialogTitle" 
      width="600px"
      top="5vh"
      @close="resetForm"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入优惠券名称" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="门槛金额" prop="thresholdAmount">
              <el-input-number v-model="form.thresholdAmount" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="减免金额" prop="discountAmount">
              <el-input-number v-model="form.discountAmount" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="有效期" prop="timeRange">
          <el-date-picker
            v-model="form.timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            popper-class="date-picker-popper"
            style="width: 100%"
          />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="总量" prop="quantity">
              <el-input-number v-model="form.quantity" :min="1" :step="10" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="form.status" style="width: 100%">
                <el-option label="上架" :value="1" />
                <el-option label="下架" :value="0" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminCouponList, createCoupon, updateCoupon, deleteCoupon } from '@/api/admin-marketing'

const loading = ref(false)
const couponList = ref([])
const searchForm = reactive({ name: '' })
const pagination = reactive({ current: 1, size: 10, total: 0 })

const dialogVisible = ref(false)
const dialogTitle = ref('新增优惠券')
const submitting = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  name: '',
  type: 1,
  thresholdAmount: 0,
  discountAmount: 0,
  timeRange: [],
  quantity: 100,
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  thresholdAmount: [{ required: true, message: '请输入门槛金额', trigger: 'blur' }],
  discountAmount: [{ required: true, message: '请输入减免金额', trigger: 'blur' }],
  timeRange: [{ required: true, message: '请选择有效期', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入总量', trigger: 'blur' }]
}

async function fetchCoupons() {
  loading.value = true
  try {
    const data = await getAdminCouponList({
      current: pagination.current,
      size: pagination.size,
      name: searchForm.name
    })
    couponList.value = data.records
    pagination.total = data.total
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.current = 1
  fetchCoupons()
}

function handleAdd() {
  dialogTitle.value = '新增优惠券'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑优惠券'
  Object.assign(form, row)
  form.timeRange = [row.startTime, row.endTime]
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const payload = { ...form }
        payload.startTime = form.timeRange[0]
        payload.endTime = form.timeRange[1]
        delete payload.timeRange

        if (form.id) {
          await updateCoupon(payload)
          ElMessage.success('更新成功')
        } else {
          await createCoupon(payload)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        fetchCoupons()
      } catch (e) {
        ElMessage.error('操作失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定删除该优惠券吗？', '提示', { type: 'warning' })
    await deleteCoupon(row.id)
    ElMessage.success('删除成功')
    fetchCoupons()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

function resetForm() {
  form.id = null
  form.name = ''
  form.type = 1
  form.thresholdAmount = 0
  form.discountAmount = 0
  form.timeRange = []
  form.quantity = 100
  form.status = 1
  if (formRef.value) formRef.value.resetFields()
}

function formatTime(time) {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 19)
}

onMounted(fetchCoupons)
</script>

<style lang="scss" scoped>
.toolbar {
  display: flex;
  gap: 10px;
}
</style>
