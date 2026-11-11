<template>
  <el-container class="admin-layout">
    <el-aside width="200px">
      <div class="logo">
        <el-icon><Location /></el-icon>
        <span>管理后台</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
      >
        <el-menu-item index="/admin">
          <el-icon><DataAnalysis /></el-icon>
          <span>数据看板</span>
        </el-menu-item>
        <el-menu-item index="/admin/scenic-spot">
          <el-icon><Picture /></el-icon>
          <span>景点管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/lines">
          <el-icon><Guide /></el-icon>
          <span>线路管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/orders">
          <el-icon><Document /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/statistics">
          <el-icon><DataAnalysis /></el-icon>
          <span>数据统计</span>
        </el-menu-item>
        <el-menu-item index="/admin/price">
          <el-icon><Money /></el-icon>
          <span>价格管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/complaints">
          <el-icon><ChatDotRound /></el-icon>
          <span>投诉建议</span>
        </el-menu-item>
        <el-menu-item index="/admin/login-log">
          <el-icon><Document /></el-icon>
          <span>登录日志</span>
        </el-menu-item>
        <el-menu-item index="/admin/companion">
          <el-icon><Connection /></el-icon>
          <span>结伴管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/campus-leader">
          <el-icon><UserFilled /></el-icon>
          <span>校园领队</span>
        </el-menu-item>
        <el-menu-item index="/admin/coupon">
          <el-icon><Ticket /></el-icon>
          <span>优惠券管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/activity">
          <el-icon><Calendar /></el-icon>
          <span>活动管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/student-auth">
          <el-icon><School /></el-icon>
          <span>学生认证</span>
        </el-menu-item>
        <el-menu-item index="/admin/content-review">
          <el-icon><Check /></el-icon>
          <span>内容审核</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/admin' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item v-if="breadcrumb">{{ breadcrumb }}</el-breadcrumb-item>
        </el-breadcrumb>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="admin-profile">
              <el-avatar :size="32" :src="userStore.userInfo?.avatar">
                {{ userStore.userInfo?.nickname?.charAt(0) || 'A' }}
              </el-avatar>
              <span class="name">{{ userStore.userInfo?.nickname || '管理员' }}</span>
              <el-icon><CaretBottom /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="info">管理员信息</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main>
        <router-view />
      </el-main>
    </el-container>

    <!-- 管理员信息弹窗 -->
    <el-dialog v-model="infoVisible" title="管理员信息" width="400px" center>
      <div class="admin-info-card">
        <div class="avatar-wrapper">
          <el-avatar :size="80" :src="userStore.userInfo?.avatar">
            {{ userStore.userInfo?.nickname?.charAt(0) || 'A' }}
          </el-avatar>
        </div>
        <div class="info-list">
          <div class="info-item">
            <span class="label">昵称：</span>
            <span class="value">{{ userStore.userInfo?.nickname }}</span>
          </div>
          <div class="info-item">
            <span class="label">账号：</span>
            <span class="value">{{ userStore.userInfo?.account || userStore.userInfo?.phone || 'admin' }}</span>
          </div>
          <div class="info-item">
            <span class="label">角色：</span>
            <el-tag size="small" type="success">超级管理员</el-tag>
          </div>
          <div class="info-item">
            <span class="label">登录时间：</span>
            <span class="value">{{ new Date().toLocaleString() }}</span>
          </div>
        </div>
      </div>
    </el-dialog>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { CaretBottom, Picture, ChatDotRound, Connection, UserFilled, Ticket, Calendar, School, Check } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const infoVisible = ref(false)

const activeMenu = computed(() => route.path)

const breadcrumb = computed(() => {
  const map = {
    '/admin': '',
    '/admin/scenic-spot': '景点管理',
    '/admin/lines': '线路管理',
    '/admin/orders': '订单管理',
    '/admin/users': '用户管理',
    '/admin/statistics': '数据统计',
    '/admin/price': '价格管理',
    '/admin/complaints': '投诉建议',
    '/admin/login-log': '登录日志',
    '/admin/companion': '结伴管理',
    '/admin/campus-leader': '校园领队',
    '/admin/coupon': '优惠券管理',
    '/admin/activity': '活动管理'
  }
  return map[route.path] || ''
})

function handleCommand(command) {
  if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  } else if (command === 'info') {
    infoVisible.value = true
  }
}
</script>

<style lang="scss" scoped>
.admin-layout {
  height: 100vh;
}
.el-aside {
  background: #304156;
  .logo {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
    height: 60px;
    color: #fff;
    font-size: 18px;
    font-weight: bold;
  }
}
.el-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  padding: 0 20px;
  
  .header-right {
    display: flex;
    align-items: center;
    
    .admin-profile {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;
      padding: 5px 10px;
      border-radius: 4px;
      transition: background 0.2s;
      
      &:hover {
        background: #f5f5f5;
      }
      
      .el-avatar {
        background: #409eff;
      }
      
      .name {
        font-size: 14px;
        color: #333;
        font-weight: 500;
      }
      
      .el-icon {
        font-size: 12px;
        color: #999;
      }
    }
  }
}
.el-main {
  background: #f0f2f5;
  padding: 20px;
}

.admin-info-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 10px;
  
  .avatar-wrapper {
    margin-bottom: 25px;
    
    .el-avatar {
      background: #409eff;
      box-shadow: 0 4px 15px rgba(64, 158, 255, 0.3);
      font-size: 32px;
    }
  }
  
  .info-list {
    width: 100%;
    padding: 0 20px;
    
    .info-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 0;
      border-bottom: 1px solid #f0f0f0;
      
      &:last-child {
        border-bottom: none;
      }
      
      .label {
        color: #666;
        font-size: 14px;
      }
      
      .value {
        color: #333;
        font-weight: 500;
        font-size: 14px;
      }
    }
  }
}
</style>
