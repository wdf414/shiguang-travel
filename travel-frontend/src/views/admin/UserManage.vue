<template>
  <div class="user-manage">
    <el-card>
      <div class="toolbar">
        <el-input v-model="searchForm.keyword" placeholder="手机号/昵称" style="width: 250px;" clearable />
        <el-select v-model="searchForm.status" placeholder="账号状态" clearable style="width: 150px;">
          <el-option label="全部" :value="''" />
          <el-option label="正常" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </div>

      <el-table :data="userList" v-loading="loading" style="margin-top: 20px;">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="nickname" label="昵称" width="150" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'danger' : ''">
              {{ row.role === 'ADMIN' ? '管理员' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-dropdown trigger="click">
              <el-button type="primary" link>
                更多操作<el-icon class="el-icon--right"><arrow-down /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="handleView(row)">
                    <el-icon><View /></el-icon>查看详情
                  </el-dropdown-item>
                  <el-dropdown-item @click="handleChangeRole(row)">
                    <el-icon><Switch /></el-icon>切换角色
                  </el-dropdown-item>
                  <el-dropdown-item @click="handleChangePassword(row)">
                    <el-icon><Lock /></el-icon>修改密码
                  </el-dropdown-item>
                  <el-dropdown-item 
                    v-if="row.status === 1 && row.role !== 'ADMIN'" 
                    @click="handleDisable(row)"
                    divided
                  >
                    <el-icon><CircleClose /></el-icon>禁用账号
                  </el-dropdown-item>
                  <el-dropdown-item 
                    v-if="row.status === 0" 
                    @click="handleEnable(row)"
                    divided
                  >
                    <el-icon><CircleCheck /></el-icon>启用账号
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        layout="total, prev, pager, next"
        @current-change="fetchUsers"
        style="margin-top: 20px; justify-content: center;"
      />
    </el-card>

    <!-- 用户详情对话框 -->
    <el-dialog v-model="detailVisible" title="用户详情" width="900px">
      <el-descriptions :column="2" border v-if="currentUser">
        <el-descriptions-item label="用户ID">{{ currentUser.id }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ currentUser.phone }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ currentUser.nickname }}</el-descriptions-item>
        <el-descriptions-item label="角色">
          <el-tag :type="currentUser.role === 'ADMIN' ? 'danger' : ''">
            {{ currentUser.role === 'ADMIN' ? '管理员' : '普通用户' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentUser.status === 1 ? 'success' : 'danger'">
            {{ currentUser.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ currentUser.createTime }}</el-descriptions-item>
      </el-descriptions>

      <el-divider>订单记录</el-divider>
      <el-table :data="userOrders" v-loading="orderLoading" max-height="300">
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="lineName" label="线路名称" min-width="200" />
        <el-table-column prop="totalPrice" label="金额" width="100">
          <template #default="{ row }">¥{{ row.totalPrice }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
      </el-table>
    </el-dialog>

    <!-- 切换角色对话框 -->
    <el-dialog v-model="roleVisible" title="切换用户角色" width="400px">
      <div v-if="currentUser">
        <p><strong>当前用户：</strong>{{ currentUser.nickname }} ({{ currentUser.phone }})</p>
        <p style="margin-top: 15px;"><strong>当前角色：</strong>
          <el-tag :type="currentUser.role === 'ADMIN' ? 'danger' : ''">
            {{ currentUser.role === 'ADMIN' ? '管理员' : '普通用户' }}
          </el-tag>
        </p>
        <div style="margin-top: 20px;">
          <strong>切换为：</strong>
          <el-radio-group v-model="newRole" style="margin-left: 10px;">
            <el-radio value="USER">普通用户</el-radio>
            <el-radio value="ADMIN">管理员</el-radio>
          </el-radio-group>
        </div>
      </div>
      <template #footer>
        <el-button @click="roleVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmChangeRole">确认切换</el-button>
      </template>
    </el-dialog>

    <!-- 修改密码对话框 -->
    <el-dialog v-model="changePasswordVisible" title="修改用户密码" width="400px">
      <div v-if="currentUser">
        <p><strong>用户：</strong>{{ currentUser.nickname }} ({{ currentUser.phone }})</p>
        <el-form style="margin-top: 20px;">
          <el-form-item label="新密码">
            <el-input 
              v-model="newPassword" 
              type="password" 
              placeholder="请输入新密码（至少6位）"
              show-password
            />
          </el-form-item>
          <el-form-item label="确认密码">
            <el-input 
              v-model="confirmPassword" 
              type="password" 
              placeholder="请再次输入新密码"
              show-password
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="changePasswordVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmChangePassword">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { View, ArrowDown, Switch, Lock, CircleClose, CircleCheck } from '@element-plus/icons-vue'
import {
  getAdminUserList,
  getAdminUserDetail,
  updateUserStatus,
  updateUserRole,
  updateUserPassword
} from '@/api/admin'

const loading = ref(false)
const userList = ref([])
const searchForm = reactive({ keyword: '', status: '' })
const pagination = reactive({ page: 1, size: 10, total: 0 })

const detailVisible = ref(false)
const roleVisible = ref(false)
const changePasswordVisible = ref(false)
const currentUser = ref(null)
const userOrders = ref([])
const orderLoading = ref(false)
const newRole = ref('')
const newPassword = ref('')
const confirmPassword = ref('')

async function fetchUsers() {
  loading.value = true
  try {
    const data = await getAdminUserList({
      page: pagination.page,
      size: pagination.size,
      keyword: searchForm.keyword,
      status: searchForm.status
    })
    userList.value = data.records
    pagination.total = data.total
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  fetchUsers()
}

async function handleView(row) {
  try {
    orderLoading.value = true
    const data = await getAdminUserDetail(row.id)
    currentUser.value = data.user
    userOrders.value = data.orders || []
    detailVisible.value = true
  } catch (e) {
    ElMessage.error('加载详情失败')
  } finally {
    orderLoading.value = false
  }
}

function handleChangeRole(row) {
  currentUser.value = row
  newRole.value = row.role === 'ADMIN' ? 'USER' : 'ADMIN'
  roleVisible.value = true
}

async function confirmChangeRole() {
  if (!currentUser.value) return
  try {
    await updateUserRole(currentUser.value.id, newRole.value)
    ElMessage.success('角色切换成功')
    roleVisible.value = false
    fetchUsers()
  } catch (e) {
    ElMessage.error('角色切换失败')
  }
}

function handleChangePassword(row) {
  currentUser.value = row
  newPassword.value = ''
  confirmPassword.value = ''
  changePasswordVisible.value = true
}

async function confirmChangePassword() {
  if (!currentUser.value) return
  if (newPassword.value.length < 6) {
    ElMessage.error('密码长度至少6位')
    return
  }
  if (newPassword.value !== confirmPassword.value) {
    ElMessage.error('两次输入的密码不一致')
    return
  }
  try {
    await updateUserPassword(currentUser.value.id, newPassword.value)
    ElMessage.success('密码修改成功')
    changePasswordVisible.value = false
  } catch (e) {
    ElMessage.error('密码修改失败')
  }
}

async function handleDisable(row) {
  try {
    await ElMessageBox.confirm(`确认禁用用户 ${row.nickname}？`, '提示', { type: 'warning' })
    await updateUserStatus(row.id, 0)
    ElMessage.success('禁用成功')
    fetchUsers()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

async function handleEnable(row) {
  try {
    await ElMessageBox.confirm(`确认启用用户 ${row.nickname}？`, '提示', { type: 'warning' })
    await updateUserStatus(row.id, 1)
    ElMessage.success('启用成功')
    fetchUsers()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

function getStatusType(status) {
  const map = {
    pending: 'warning',
    paid: 'success',
    completed: 'info',
    cancelled: 'info',
    refunded: 'danger'
  }
  return map[status] || ''
}

function getStatusText(status) {
  const map = {
    pending: '待支付',
    paid: '已支付',
    completed: '已完成',
    cancelled: '已取消',
    refunded: '已退款'
  }
  return map[status] || status
}

onMounted(fetchUsers)
</script>

<style lang="scss" scoped>
.toolbar {
  display: flex;
  gap: 10px;
}
</style>
