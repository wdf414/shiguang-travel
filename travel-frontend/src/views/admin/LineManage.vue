<template>
  <div class="line-manage">
    <el-card>
      <div class="toolbar">
        <el-input v-model="searchForm.keyword" placeholder="搜索线路" style="width: 200px;" clearable />
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button type="success" @click="handleAdd">新增线路</el-button>
      </div>

      <el-table :data="lineList" v-loading="loading" style="margin-top: 20px;">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="线路名称" min-width="200" />
        <el-table-column prop="destination" label="目的地" width="100" />
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column prop="days" label="天数" width="80" />
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column prop="sales" label="销量" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button 
              :type="row.status === 1 ? 'warning' : 'success'" 
              link 
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        layout="total, prev, pager, next"
        @current-change="fetchLines"
        style="margin-top: 20px; justify-content: center;"
      />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="dialogTitle" 
      width="800px"
      @close="resetForm"
    >
      <el-form :model="form" label-width="100px">
        <el-form-item label="线路名称" required>
          <el-input v-model="form.title" placeholder="请输入线路名称" />
        </el-form-item>
        <el-form-item label="目的地" required>
          <el-input v-model="form.destination" placeholder="如：云南" />
        </el-form-item>
        <el-form-item label="出发城市" required>
          <el-input v-model="form.departureCity" placeholder="如：上海" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="价格" required>
              <el-input-number v-model="form.price" :min="0" :precision="2" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="天数" required>
              <el-input-number v-model="form.days" :min="1" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="库存">
              <el-input-number v-model="form.stock" :min="0" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-radio-group v-model="form.status">
                <el-radio :value="1">上架</el-radio>
                <el-radio :value="0">下架</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="线路描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
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
import { getAdminLineList, createLine, updateLine, updateLineStatus, deleteLine } from '@/api/admin'

const loading = ref(false)
const lineList = ref([])
const searchForm = reactive({ keyword: '' })
const pagination = reactive({ page: 1, size: 10, total: 0 })

const dialogVisible = ref(false)
const dialogTitle = ref('新增线路')
const submitting = ref(false)
const form = reactive({
  id: null,
  title: '',
  destination: '',
  departureCity: '',
  price: 0,
  days: 1,
  stock: 100,
  status: 1,
  description: ''
})

async function fetchLines() {
  loading.value = true
  try {
    const data = await getAdminLineList({
      page: pagination.page,
      size: pagination.size,
      keyword: searchForm.keyword
    })
    lineList.value = data.records
    pagination.total = data.total
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  fetchLines()
}

function handleAdd() {
  dialogTitle.value = '新增线路'
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑线路'
  Object.assign(form, row)
  dialogVisible.value = true
}

async function handleSubmit() {
  submitting.value = true
  try {
    if (form.id) {
      await updateLine(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createLine(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchLines()
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

async function handleToggleStatus(row) {
  try {
    const newStatus = row.status === 1 ? 0 : 1
    await updateLineStatus(row.id, newStatus)
    ElMessage.success('状态更新成功')
    fetchLines()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定删除该线路吗？', '提示', { type: 'warning' })
    await deleteLine(row.id)
    ElMessage.success('删除成功')
    fetchLines()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

function resetForm() {
  form.id = null
  form.title = ''
  form.destination = ''
  form.departureCity = ''
  form.price = 0
  form.days = 1
  form.stock = 100
  form.status = 1
  form.description = ''
}

onMounted(fetchLines)
</script>

<style lang="scss" scoped>
.toolbar {
  display: flex;
  gap: 10px;
}
</style>
