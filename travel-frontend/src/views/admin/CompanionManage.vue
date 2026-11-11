<template>
  <div class="companion-manage">
    <h2>结伴管理</h2>
    
    <div class="filter-bar">
      <el-input v-model="searchKeyword" placeholder="搜索标题/内容" style="width: 250px" clearable />
      <el-select v-model="filterStatus" placeholder="状态筛选" clearable style="width: 150px">
        <el-option label="招募中" :value="0" />
        <el-option label="已成行" :value="1" />
        <el-option label="已取消" :value="2" />
      </el-select>
      <el-button type="primary" @click="fetchList">搜索</el-button>
    </div>

    <el-table :data="tableData" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
      <el-table-column prop="creatorName" label="发起人" width="100" />
      <el-table-column prop="creatorSchool" label="学校" width="140" show-overflow-tooltip />
      <el-table-column label="人数" width="100">
        <template #default="{ row }">
          {{ row.currentCount }}/{{ row.memberCount }}
        </template>
      </el-table-column>
      <el-table-column prop="departureDate" label="出发日期" width="120" />
      <el-table-column label="领队" width="80" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.isLeader === 1" type="danger" size="small">是</el-tag>
          <span v-else>否</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'warning' : row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 0 ? '招募中' : row.status === 1 ? '已成行' : '已取消' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" plain @click="handleView(row)">查看</el-button>
          <el-button v-if="row.status === 0" size="small" @click="changeStatus(row.id, 2)">取消</el-button>
          <el-button v-if="row.status === 2" size="small" type="success" @click="changeStatus(row.id, 0)">恢复</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="pagination.page"
        :total="pagination.total"
        :page-size="pagination.size"
        layout="total, prev, pager, next"
        @current-change="fetchList"
      />
    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="viewVisible" title="结伴详情" width="600px">
      <div v-if="currentRow" class="detail-content">
        <div class="detail-section">
          <h3>基础信息</h3>
          <div class="info-item">
            <span class="label">标题：</span>
            <span>{{ currentRow.title }}</span>
          </div>
          <div class="info-item">
            <span class="label">状态：</span>
            <el-tag :type="currentRow.status === 0 ? 'warning' : currentRow.status === 1 ? 'success' : 'info'" size="small">
              {{ currentRow.status === 0 ? '招募中' : currentRow.status === 1 ? '已成行' : '已取消' }}
            </el-tag>
          </div>
          <div class="info-item">
            <span class="label">出发日期：</span>
            <span>{{ currentRow.departureDate }}</span>
          </div>
          <div class="info-item">
            <span class="label">招募人数：</span>
            <span>{{ currentRow.currentCount }}/{{ currentRow.memberCount }}</span>
          </div>
          <div class="info-item">
            <span class="label">限定学校：</span>
            <span>{{ currentRow.school || '不限' }}</span>
          </div>
        </div>

        <div class="detail-section">
          <h3>发起人信息</h3>
          <div class="info-item">
            <span class="label">昵称：</span>
            <span>{{ currentRow.creatorName }}</span>
            <el-tag v-if="currentRow.isLeader === 1" type="danger" size="small" style="margin-left: 8px">校园领队</el-tag>
          </div>
          <div class="info-item">
            <span class="label">学校：</span>
            <span>{{ currentRow.creatorSchool || '未填写' }}</span>
          </div>
          <div class="info-item">
            <span class="label">专业/年级：</span>
            <span>{{ currentRow.creatorMajor }} {{ currentRow.creatorGrade }}</span>
          </div>
        </div>

        <div class="detail-section">
          <h3>详细说明</h3>
          <div class="content-text">{{ currentRow.content }}</div>
        </div>
      </div>
      <template #footer>
        <el-button @click="viewVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminCompanionList, updateCompanionStatus, deleteCompanion } from '@/api/admin'

const loading = ref(false)
const tableData = ref([])
const searchKeyword = ref('')
const filterStatus = ref(null)

const viewVisible = ref(false)
const currentRow = ref(null)

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

onMounted(() => {
  fetchList()
})

async function fetchList() {
  try {
    loading.value = true
    const res = await getAdminCompanionList({
      page: pagination.page,
      size: pagination.size,
      keyword: searchKeyword.value || undefined,
      status: filterStatus.value !== null ? filterStatus.value : undefined
    })
    tableData.value = res.records
    pagination.total = res.total
  } catch (error) {
    ElMessage.error('获取列表失败')
  } finally {
    loading.value = false
  }
}

async function changeStatus(id, status) {
  try {
    const statusText = status === 0 ? '恢复' : '取消'
    await ElMessageBox.confirm(`确定要${statusText}该结伴游吗？`, '提示', { type: 'warning' })
    await updateCompanionStatus(id, status)
    ElMessage.success('操作成功')
    fetchList()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定要删除该结伴游吗？此操作不可恢复。', '警告', { type: 'error' })
    await deleteCompanion(id)
    ElMessage.success('删除成功')
    fetchList()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

function handleView(row) {
  currentRow.value = row
  viewVisible.value = true
}
</script>

<style scoped>
.companion-manage {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
}

h2 {
  margin: 0 0 20px 0;
  font-size: 20px;
  color: #303133;
}

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.detail-section {
  margin-bottom: 24px;
}

.detail-section h3 {
  font-size: 16px;
  margin: 0 0 12px 0;
  padding-left: 8px;
  border-left: 4px solid #409eff;
  color: #303133;
}

.info-item {
  display: flex;
  margin-bottom: 8px;
  font-size: 14px;
}

.info-item .label {
  color: #909399;
  width: 80px;
  flex-shrink: 0;
}

.content-text {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  white-space: pre-wrap;
  background: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
}
</style>
