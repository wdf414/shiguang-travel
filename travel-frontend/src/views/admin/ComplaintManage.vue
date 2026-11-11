<template>
  <div class="complaint-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>投诉建议管理</span>
          <div class="header-ops">
            <el-radio-group v-model="queryForm.status" @change="handleQuery">
              <el-radio-button :label="null">全部</el-radio-button>
              <el-radio-button :label="0">待处理</el-radio-button>
              <el-radio-button :label="1">已回复</el-radio-button>
            </el-radio-group>
          </div>
        </div>
      </template>

      <el-table :data="complaintList" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="nickname" label="用户" width="120" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(row.type)">{{ getTypeText(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" width="200" show-overflow-tooltip />
        <el-table-column prop="content" label="内容" min-width="300" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'warning'">
              {{ row.status === 1 ? '已回复' : '待处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              link 
              @click="handleView(row)"
            >
              {{ row.status === 1 ? '查看' : '回复' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="queryForm.page"
          v-model:page-size="queryForm.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 回复详情弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="currentRow?.status === 1 ? '投诉详情' : '回复投诉'"
      width="600px"
    >
      <div v-if="currentRow" class="complaint-detail">
        <div class="detail-item">
          <span class="label">用户信息：</span>
          <span class="value">{{ currentRow.nickname }}</span>
        </div>
        <div class="detail-item">
          <span class="label">投诉标题：</span>
          <span class="value">{{ currentRow.title }}</span>
        </div>
        <div class="detail-item">
          <span class="label">投诉内容：</span>
          <div class="content-box">{{ currentRow.content }}</div>
        </div>
        
        <el-divider />
        
        <div v-if="currentRow.status === 1" class="reply-section">
          <div class="detail-item">
            <span class="label">回复内容：</span>
            <div class="reply-box">{{ currentRow.reply }}</div>
          </div>
          <div class="detail-item">
            <span class="label">回复时间：</span>
            <span class="value">{{ currentRow.replyTime }}</span>
          </div>
        </div>
        <div v-else class="reply-form">
          <el-form label-position="top">
            <el-form-item label="回复内容" required>
              <el-input
                v-model="replyContent"
                type="textarea"
                :rows="5"
                placeholder="请输入回复内容..."
              />
            </el-form-item>
          </el-form>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">关闭</el-button>
          <el-button 
            v-if="currentRow?.status === 0" 
            type="primary" 
            :loading="submitting"
            @click="submitReply"
          >
            提交回复
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAdminComplaintList, replyComplaint } from '@/api/admin'

const loading = ref(false)
const submitting = ref(false)
const complaintList = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const currentRow = ref(null)
const replyContent = ref('')

const queryForm = reactive({
  page: 1,
  size: 10,
  status: null,
  type: null
})

async function fetchComplaints() {
  loading.value = true
  try {
    const res = await getAdminComplaintList(queryForm)
    complaintList.value = res.records
    total.value = res.total
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  queryForm.page = 1
  fetchComplaints()
}

function handleSizeChange(val) {
  queryForm.size = val
  fetchComplaints()
}

function handleCurrentChange(val) {
  queryForm.page = val
  fetchComplaints()
}

function handleView(row) {
  currentRow.value = row
  replyContent.value = ''
  dialogVisible.value = true
}

async function submitReply() {
  if (!replyContent.value.trim()) {
    return ElMessage.warning('请输入回复内容')
  }
  
  submitting.value = true
  try {
    await replyComplaint(currentRow.value.id, replyContent.value)
    ElMessage.success('回复成功')
    dialogVisible.value = false
    fetchComplaints()
  } catch (error) {
    console.error(error)
  } finally {
    submitting.value = false
  }
}

function getTypeText(type) {
  const map = { 0: '投诉', 1: '建议', 2: '咨询' }
  return map[type] || '其他'
}

function getTypeTag(type) {
  const map = { 0: 'danger', 1: 'success', 2: 'info' }
  return map[type] || ''
}

onMounted(() => {
  fetchComplaints()
})
</script>

<style lang="scss" scoped>
.complaint-manage {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}

.complaint-detail {
  .detail-item {
    margin-bottom: 15px;
    display: flex;
    flex-direction: column;
    gap: 8px;
    
    .label {
      font-weight: bold;
      color: #606266;
    }
    
    .value {
      color: #333;
    }
  }
  
  .content-box, .reply-box {
    background: #f5f7fa;
    padding: 12px;
    border-radius: 4px;
    line-height: 1.6;
    color: #333;
    white-space: pre-wrap;
  }
  
  .reply-box {
    background: #f0f9eb;
    border-left: 4px solid #67c23a;
  }
}
</style>