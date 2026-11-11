<template>
  <div class="user-center container">
    <div class="user-sidebar">
      <div class="user-info">
        <el-avatar :size="80" :src="userStore.userInfo?.avatar">
          {{ userStore.userInfo?.nickname?.charAt(0) || 'U' }}
        </el-avatar>
        <h3>{{ userStore.userInfo?.nickname }}</h3>
        <p>{{ userStore.userInfo?.phone }}</p>
      </div>
      <el-menu :default-active="activeMenu" router>
        <el-menu-item index="/user/orders">
          <el-icon><Document /></el-icon>
          <span>我的订单</span>
        </el-menu-item>
        <el-menu-item index="/user/favorites">
          <el-icon><Star /></el-icon>
          <span>我的收藏</span>
        </el-menu-item>
        <el-menu-item index="/user/route">
          <el-icon><Location /></el-icon>
          <span>我的路线</span>
        </el-menu-item>
        <el-menu-item index="/user/profile">
          <el-icon><User /></el-icon>
          <span>个人信息</span>
        </el-menu-item>
        <el-menu-item index="/user/complaints">
          <el-icon><Message /></el-icon>
          <span>意见反馈</span>
        </el-menu-item>
        <el-menu-item index="/user/coupons">
          <el-icon><Ticket /></el-icon>
          <span>我的优惠券</span>
        </el-menu-item>
        <el-menu-item index="/user/student-auth">
          <el-icon><School /></el-icon>
          <span>学生认证</span>
          <el-tag v-if="isStudent" type="success" size="small" class="student-tag">已认证</el-tag>
          <el-tag v-else-if="authStatus === 1" type="warning" size="small" class="student-tag">审核中</el-tag>
        </el-menu-item>
      </el-menu>
    </div>
    <div class="user-content">
      <router-view />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { Document, Star, Location, User, Message, Ticket, School } from '@element-plus/icons-vue'
import { ref, onMounted } from 'vue'
import { getStudentAuthStatus } from '@/api/student'

const route = useRoute()
const userStore = useUserStore()
const isStudent = ref(false)
const authStatus = ref(0)

const activeMenu = computed(() => route.path)

onMounted(() => {
  fetchStudentStatus()
})

async function fetchStudentStatus() {
  try {
    const res = await getStudentAuthStatus()
    isStudent.value = res.isStudent === 1
    authStatus.value = res.authStatus
  } catch (error) {
    console.error('获取学生状态失败:', error)
  }
}
</script>

<style lang="scss" scoped>
.user-center {
  display: grid;
  grid-template-columns: 240px 1fr;
  gap: 30px;
  padding-top: 20px;
}
.user-sidebar {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}
.user-info {
  padding: 30px;
  text-align: center;
  background: linear-gradient(135deg, #409eff 0%, #36d1dc 100%);
  color: #fff;
  h3 { margin: 15px 0 5px; }
  p { opacity: 0.9; font-size: 14px; }
}
.user-content {
  background: #fff;
  border-radius: 8px;
  padding: 25px;
  min-height: 500px;
}

.student-tag {
  margin-left: 8px;
}
@media (max-width: 768px) {
  .user-center { grid-template-columns: 1fr; }
  .user-sidebar { display: none; }
}
</style>
