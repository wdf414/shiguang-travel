<template>
  <div class="student-auth-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <h3>🎓 学生认证审核</h3>
          <div class="header-stats">
            <el-tag type="warning">待审核: {{ stats.pending }}</el-tag>
            <el-tag type="success">已通过: {{ stats.approved }}</el-tag>
            <el-tag type="danger">已拒绝: {{ stats.rejected }}</el-tag>
          </div>
        </div>
      </template>

      <!-- 筛选栏 -->
      <div class="filter-bar">
        <el-radio-group v-model="filterStatus" @change="handleFilterChange">
          <el-radio-button label="">全部</el-radio-button>
          <el-radio-button label="0">待审核</el-radio-button>
          <el-radio-button label="1">已通过</el-radio-button>
          <el-radio-button label="2">已拒绝</el-radio-button>
        </el-radio-group>
      </div>

      <!-- 申请列表 -->
      <el-table :data="applicationList" v-loading="loading" stripe>
        <el-table-column type="index" width="50" />
        <el-table-column label="用户信息" min-width="150">
          <template #default="{ row }">
            <div class="user-info">
              <p><strong>{{ row.userName }}</strong></p>
              <p class="text-muted">{{ row.userPhone }}</p>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="学生信息" min-width="200">
          <template #default="{ row }">
            <div class="student-info">
              <p><strong>{{ row.realName }}</strong></p>
              <p>{{ row.schoolName }}</p>
              <p class="text-muted">{{ row.major }} · {{ row.grade }}</p>
              <p class="text-muted">学号: {{ row.studentId }}</p>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="认证材料" width="120">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewMaterials(row)">
              查看材料
            </el-button>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="申请时间" width="150">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="审核信息" min-width="150">
          <template #default="{ row }">
            <div v-if="row.reviewerName">
              <p>{{ row.reviewerName }}</p>
              <p class="text-muted">{{ formatTime(row.reviewTime) }}</p>
            </div>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button type="success" size="small" @click="handleApprove(row)">
                通过
              </el-button>
              <el-button type="danger" size="small" @click="handleReject(row)">
                拒绝
              </el-button>
            </template>
            <span v-else class="text-muted">已处理</span>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 查看材料对话框 -->
    <el-dialog v-model="materialDialogVisible" title="认证材料" width="800px">
      <div v-if="currentApplication" class="materials-content">
        <el-row :gutter="20">
          <el-col :span="12">
            <h4>学生证照片</h4>
            <el-image
              :src="currentApplication.studentCard"
              :preview-src-list="[currentApplication.studentCard]"
              fit="contain"
              class="material-img"
            />
          </el-col>
          <el-col :span="12">
            <h4>身份证正面</h4>
            <el-image
              :src="currentApplication.idCardFront"
              :preview-src-list="[currentApplication.idCardFront]"
              fit="contain"
              class="material-img"
            />
          </el-col>
        </el-row>
        <el-row :gutter="20" style="margin-top: 20px;">
          <el-col :span="12">
            <h4>身份证反面</h4>
            <el-image
              :src="currentApplication.idCardBack"
              :preview-src-list="[currentApplication.idCardBack]"
              fit="contain"
              class="material-img"
            />
          </el-col>
          <el-col :span="12" v-if="currentApplication.chsiScreenshot">
            <h4>学信网截图</h4>
            <el-image
              :src="currentApplication.chsiScreenshot"
              :preview-src-list="[currentApplication.chsiScreenshot]"
              fit="contain"
              class="material-img"
            />
          </el-col>
        </el-row>
      </div>
    </el-dialog>

    <!-- 拒绝原因对话框 -->
    <el-dialog v-model="rejectDialogVisible" title="拒绝原因" width="500px">
      <el-form>
        <el-form-item label="拒绝原因">
          <el-input
            v-model="rejectReason"
            type="textarea"
            :rows="4"
            placeholder="请输入拒绝原因，将告知用户"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject" :loading="submitting">
          确认拒绝
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPendingApplications, approveApplication, rejectApplication } from '@/api/adminStudent'

const loading = ref(false)
const applicationList = ref([])
const filterStatus = ref('')
const materialDialogVisible = ref(false)
const rejectDialogVisible = ref(false)
const currentApplication = ref(null)
const rejectReason = ref('')
const submitting = ref(false)

const stats = reactive({
  pending: 0,
  approved: 0,
  rejected: 0
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

onMounted(() => {
  fetchApplications()
})

async function fetchApplications() {
  loading.value = true
  try {
    const res = await getPendingApplications(pagination.page, pagination.size)
    applicationList.value = res.records || []
    pagination.total = res.total || 0
    
    // 更新统计
    updateStats()
  } catch (error) {
    ElMessage.error('获取申请列表失败')
  } finally {
    loading.value = false
  }
}

function updateStats() {
  // 这里可以通过API获取统计数据，暂时使用列表数据
  stats.pending = applicationList.value.filter(item => item.status === 0).length
  stats.approved = applicationList.value.filter(item => item.status === 1).length
  stats.rejected = applicationList.value.filter(item => item.status === 2).length
}

function handleFilterChange() {
  pagination.page = 1
  fetchApplications()
}

function handleSizeChange(size) {
  pagination.size = size
  fetchApplications()
}

function handlePageChange(page) {
  pagination.page = page
  fetchApplications()
}

function viewMaterials(row) {
  currentApplication.value = row
  materialDialogVisible.value = true
}

async function handleApprove(row) {
  try {
    await ElMessageBox.confirm(
      `确认通过 ${row.realName} 的学生认证申请？`,
      '确认通过',
      { confirmButtonText: '确认', cancelButtonText: '取消', type: 'success' }
    )
    
    await approveApplication(row.id)
    ElMessage.success('审核通过')
    fetchApplications()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

function handleReject(row) {
  currentApplication.value = row
  rejectReason.value = ''
  rejectDialogVisible.value = true
}

async function confirmReject() {
  if (!rejectReason.value.trim()) {
    ElMessage.warning('请输入拒绝原因')
    return
  }
  
  submitting.value = true
  try {
    await rejectApplication(currentApplication.value.id, rejectReason.value)
    ElMessage.success('已拒绝')
    rejectDialogVisible.value = false
    fetchApplications()
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

function getStatusType(status) {
  const types = { 0: 'warning', 1: 'success', 2: 'danger' }
  return types[status] || 'info'
}

function getStatusText(status) {
  const texts = { 0: '待审核', 1: '已通过', 2: '已拒绝' }
  return texts[status] || '未知'
}

function formatTime(time) {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}
</script>

<style scoped lang="scss">
.student-auth-manage {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  h3 {
    margin: 0;
  }
  
  .header-stats {
    display: flex;
    gap: 10px;
  }
}

.filter-bar {
  margin-bottom: 20px;
}

.user-info, .student-info {
  p {
    margin: 4px 0;
  }
}

.text-muted {
  color: #909399;
  font-size: 13px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.materials-content {
  h4 {
    margin-bottom: 10px;
    color: #303133;
  }
}

.material-img {
  width: 100%;
  height: 200px;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
}
</style>
