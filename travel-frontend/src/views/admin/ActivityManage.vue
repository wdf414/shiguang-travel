<template>
  <div class="activity-manage">
    <el-card>
      <div class="toolbar">
        <el-input v-model="searchForm.title" placeholder="搜索活动名称" style="width: 200px;" clearable />
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button type="success" @click="handleAdd">新增活动</el-button>
      </div>

      <el-table :data="activityList" v-loading="loading" style="margin-top: 20px;">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="活动标题" min-width="150" />
        <el-table-column label="有效期" width="320">
          <template #default="{ row }">
            {{ formatTime(row.startTime) }} 至 {{ formatTime(row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '进行中' : '已关闭' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="可见范围" width="120">
          <template #default="{ row }">
            <el-tag :type="row.studentOnly === 1 ? 'success' : ''">
              {{ row.studentOnly === 1 ? '🎓 学生专属' : '所有人' }}
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
        @current-change="fetchActivities"
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
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入活动标题" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入活动描述" />
        </el-form-item>
        <el-form-item label="封面图" prop="coverImage">
          <el-input v-model="form.coverImage" placeholder="请输入图片URL" />
        </el-form-item>
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
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="可见范围">
          <el-radio-group v-model="form.studentOnly">
            <el-radio :label="0">所有人可见</el-radio>
            <el-radio :label="1">仅学生可见</el-radio>
          </el-radio-group>
        </el-form-item>
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
import { getAdminActivityList, createActivity, updateActivity, deleteActivity } from '@/api/admin-marketing'

const loading = ref(false)
const activityList = ref([])
const searchForm = reactive({ title: '' })
const pagination = reactive({ current: 1, size: 10, total: 0 })

const dialogVisible = ref(false)
const dialogTitle = ref('新增活动')
const submitting = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  title: '',
  description: '',
  coverImage: '',
  timeRange: [],
  status: 1,
  studentOnly: 0
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  description: [{ required: true, message: '请输入描述', trigger: 'blur' }],
  timeRange: [{ required: true, message: '请选择有效期', trigger: 'change' }]
}

async function fetchActivities() {
  loading.value = true
  try {
    const data = await getAdminActivityList({
      current: pagination.current,
      size: pagination.size,
      title: searchForm.title
    })
    activityList.value = data.records
    pagination.total = data.total
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.current = 1
  fetchActivities()
}

function handleAdd() {
  dialogTitle.value = '新增活动'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑活动'
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
          await updateActivity(payload)
          ElMessage.success('更新成功')
        } else {
          await createActivity(payload)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        fetchActivities()
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
    await ElMessageBox.confirm('确定删除该活动吗？', '提示', { type: 'warning' })
    await deleteActivity(row.id)
    ElMessage.success('删除成功')
    fetchActivities()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

function resetForm() {
  form.id = null
  form.title = ''
  form.description = ''
  form.coverImage = ''
  form.timeRange = []
  form.status = 1
  form.studentOnly = 0
  if (formRef.value) formRef.value.resetFields()
}

function formatTime(time) {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 19)
}

onMounted(fetchActivities)
</script>

<style lang="scss" scoped>
.toolbar {
  display: flex;
  gap: 10px;
}
</style>
