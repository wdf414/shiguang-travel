<template>
  <div class="leader-manage">
    <h2>校园领队管理</h2>
    
    <div class="filter-bar">
      <el-input v-model="searchSchool" placeholder="搜索学校" style="width: 200px" clearable />
      <el-select v-model="filterStatus" placeholder="状态筛选" clearable style="width: 150px">
        <el-option label="待审核" :value="0" />
        <el-option label="已认证" :value="1" />
        <el-option label="已拒绝" :value="2" />
      </el-select>
      <el-button type="primary" @click="fetchList">搜索</el-button>
    </div>

    <el-table :data="tableData" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="nickname" label="用户昵称" width="120" />
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column prop="school" label="学校" width="150" show-overflow-tooltip />
      <el-table-column prop="major" label="专业" width="130" />
      <el-table-column prop="grade" label="年级" width="90" />
      <el-table-column prop="description" label="领队简介" min-width="180" show-overflow-tooltip />
      <el-table-column label="佣金比例" width="100" align="center">
        <template #default="{ row }">
          {{ row.commissionRate }}%
        </template>
      </el-table-column>
      <el-table-column label="累计佣金" width="100" align="center">
        <template #default="{ row }">
          ¥{{ row.totalCommission || '0.00' }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'warning' : row.status === 1 ? 'success' : 'danger'" size="small">
            {{ row.status === 0 ? '待审核' : row.status === 1 ? '已认证' : '已拒绝' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="申请时间" width="170" />
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 0" size="small" type="success" @click="handleAudit(row.id, 1)">通过</el-button>
          <el-button v-if="row.status === 0" size="small" type="danger" @click="handleAudit(row.id, 2)">拒绝</el-button>
          <el-button size="small" @click="showCommissionDialog(row)">设置佣金</el-button>
          <el-button size="small" type="info" @click="viewCommissions(row)">佣金明细</el-button>
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

    <!-- 设置佣金比例弹窗 -->
    <el-dialog v-model="commissionDialogVisible" title="设置佣金比例" width="400px">
      <el-form>
        <el-form-item label="领队">{{ currentLeader?.nickname }}</el-form-item>
        <el-form-item label="学校">{{ currentLeader?.school }}</el-form-item>
        <el-form-item label="佣金比例(%)">
          <el-input-number v-model="newCommissionRate" :min="0" :max="100" :precision="2" :step="0.5" style="width: 200px" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="commissionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveCommissionRate">保存</el-button>
      </template>
    </el-dialog>

    <!-- 佣金明细弹窗 -->
    <el-dialog v-model="commissionDetailVisible" title="佣金明细" width="700px">
      <el-table :data="commissionList" border size="small">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="companionTitle" label="结伴游" min-width="150" />
        <el-table-column prop="orderAmount" label="订单金额" width="100">
          <template #default="{ row }">¥{{ row.orderAmount }}</template>
        </el-table-column>
        <el-table-column prop="commissionRate" label="比例" width="80">
          <template #default="{ row }">{{ row.commissionRate }}%</template>
        </el-table-column>
        <el-table-column prop="commissionAmount" label="佣金" width="100">
          <template #default="{ row }">¥{{ row.commissionAmount }}</template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'warning' : 'success'" size="small">
              {{ row.status === 0 ? '待结算' : '已结算' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" width="170" />
      </el-table>
      <el-empty v-if="!commissionList.length" description="暂无佣金记录" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminLeaderList, auditLeader, setLeaderCommissionRate, getLeaderCommissions, deleteLeader } from '@/api/admin'

const loading = ref(false)
const tableData = ref([])
const searchSchool = ref('')
const filterStatus = ref(null)

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 佣金设置
const commissionDialogVisible = ref(false)
const currentLeader = ref(null)
const newCommissionRate = ref(5)

// 佣金明细
const commissionDetailVisible = ref(false)
const commissionList = ref([])

onMounted(() => {
  fetchList()
})

async function fetchList() {
  try {
    loading.value = true
    const res = await getAdminLeaderList({
      page: pagination.page,
      size: pagination.size,
      school: searchSchool.value || undefined,
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

async function handleAudit(id, status) {
  const text = status === 1 ? '通过' : '拒绝'
  try {
    await ElMessageBox.confirm(`确定要${text}该领队申请吗？`, '审核确认', { type: 'warning' })
    await auditLeader(id, status)
    ElMessage.success(`${text}成功`)
    fetchList()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

function showCommissionDialog(leader) {
  currentLeader.value = leader
  newCommissionRate.value = leader.commissionRate || 5
  commissionDialogVisible.value = true
}

async function saveCommissionRate() {
  try {
    await setLeaderCommissionRate(currentLeader.value.id, newCommissionRate.value)
    ElMessage.success('佣金比例设置成功')
    commissionDialogVisible.value = false
    fetchList()
  } catch (e) {
    ElMessage.error('设置失败')
  }
}

async function viewCommissions(leader) {
  try {
    const res = await getLeaderCommissions(leader.id, { page: 1, size: 50 })
    commissionList.value = res.records || []
    commissionDetailVisible.value = true
  } catch (e) {
    ElMessage.error('获取佣金明细失败')
  }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定要删除该领队吗？', '警告', { type: 'error' })
    await deleteLeader(id)
    ElMessage.success('删除成功')
    fetchList()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}
</script>

<style scoped>
.leader-manage {
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
</style>
