<template>
  <div class="my-complaints">
    <div class="header">
      <h2>意见反馈</h2>
      <el-button type="primary" @click="showDialog = true">提交反馈</el-button>
    </div>

    <div class="filter-bar">
      <el-radio-group v-model="filterType" @change="fetchData">
        <el-radio-button :label="null">全部</el-radio-button>
        <el-radio-button :label="1">建议</el-radio-button>
        <el-radio-button :label="2">投诉</el-radio-button>
      </el-radio-group>
    </div>

    <el-table v-loading="loading" :data="complaints" style="width: 100%; margin-top: 20px">
      <el-table-column prop="type" label="类型" width="100">
        <template #default="{ row }">
          <el-tag :type="row.type === 1 ? 'success' : 'danger'">
            {{ row.type === 1 ? '建议' : '投诉' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="content" label="内容" show-overflow-tooltip />
      <el-table-column prop="createTime" label="提交时间" width="180" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '已回复' : '待处理' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button link type="primary" @click="viewDetail(row)">查看详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        layout="prev, pager, next"
        @current-change="fetchData"
      />
    </div>

    <!-- 提交反馈对话框 -->
    <el-dialog v-model="showDialog" title="提交反馈" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio :label="1">建议</el-radio>
            <el-radio :label="2">投诉</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="4"
            placeholder="请详细描述您的问题或建议"
          />
        </el-form-item>
        <el-form-item label="联系方式" prop="contact">
          <el-input v-model="form.contact" placeholder="手机号或邮箱（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showDialog = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">提交</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="反馈详情" width="600px">
      <div v-if="currentComplaint" class="complaint-detail">
        <div class="detail-item">
          <span class="label">类型：</span>
          <el-tag :type="currentComplaint.type === 1 ? 'success' : 'danger'">
            {{ currentComplaint.type === 1 ? '建议' : '投诉' }}
          </el-tag>
        </div>
        <div class="detail-item">
          <span class="label">提交时间：</span>
          <span>{{ currentComplaint.createTime }}</span>
        </div>
        <div class="detail-item content-box">
          <span class="label">反馈内容：</span>
          <p>{{ currentComplaint.content }}</p>
        </div>
        <div v-if="currentComplaint.reply" class="reply-box">
          <div class="reply-header">
            <span class="label">平台回复：</span>
            <span class="time">{{ currentComplaint.replyTime }}</span>
          </div>
          <p class="reply-content">{{ currentComplaint.reply }}</p>
        </div>
        <div v-else class="no-reply">
          暂无回复，请耐心等待...
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getComplaintList, submitComplaint } from '@/api/complaint'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const complaints = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const filterType = ref(null)

const showDialog = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const form = reactive({
  type: 1,
  content: '',
  contact: ''
})

const rules = {
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}

const detailDialogVisible = ref(false)
const currentComplaint = ref(null)

async function fetchData() {
  loading.value = true
  try {
    const res = await getComplaintList({
      page: page.value,
      size: size.value,
      type: filterType.value
    })
    complaints.value = res.records || []
    total.value = res.total || 0
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        await submitComplaint(form)
        ElMessage.success('提交成功')
        showDialog.value = false
        form.content = ''
        form.contact = ''
        fetchData()
      } catch (error) {
        console.error(error)
      } finally {
        submitting.value = false
      }
    }
  })
}

function viewDetail(row) {
  currentComplaint.value = row
  detailDialogVisible.value = true
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.my-complaints {
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  min-height: 500px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.filter-bar {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.complaint-detail {
  padding: 10px;
}

.detail-item {
  margin-bottom: 15px;
  display: flex;
  align-items: flex-start;
}

.detail-item .label {
  font-weight: bold;
  width: 80px;
  flex-shrink: 0;
  color: #606266;
}

.content-box p {
  margin: 0;
  line-height: 1.6;
  background: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
  flex: 1;
}

.reply-box {
  margin-top: 20px;
  background: #f0f9eb;
  padding: 15px;
  border-radius: 4px;
  border-left: 4px solid #67c23a;
}

.reply-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  color: #67c23a;
  font-weight: bold;
}

.reply-content {
  margin: 0;
  color: #333;
}

.no-reply {
  margin-top: 20px;
  text-align: center;
  color: #909399;
  font-style: italic;
}
</style>
