<template>
  <header class="app-header">
    <div class="container header-content">
      <router-link to="/" class="logo">
        <el-icon><Location /></el-icon>
        <span>拾光旅行</span>
      </router-link>
      
      <nav class="nav-menu">
        <router-link to="/" exact-active-class="router-link-active">首页</router-link>
        <router-link to="/search">线路搜索</router-link>
        <router-link to="/hotels">酒店预订</router-link>
        <router-link to="/companion">结伴游</router-link>
        <router-link to="/travel-notes">游记攻略</router-link>
        <router-link to="/weather">天气查询</router-link>
        <router-link to="/ai/planner" class="ai-nav-link">
          <el-icon><MagicStick /></el-icon>
          AI 行程
        </router-link>
      </nav>

      <div class="header-right">
        <div class="search-box">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索城市/景点"
            class="search-input"
            @keyup.enter="handleSearch"
          />
          <el-button class="search-btn" circle @click="handleSearch">
            <el-icon><Search /></el-icon>
          </el-button>
        </div>

        <template v-if="userStore.isLoggedIn">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" :src="userStore.userInfo?.avatar">
                {{ userStore.userInfo?.nickname?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="nickname">{{ userStore.userInfo?.nickname }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="orders">我的订单</el-dropdown-item>
                <el-dropdown-item command="favorites">我的收藏</el-dropdown-item>
                <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <router-link to="/login"><el-button type="primary">登录</el-button></router-link>
          <router-link to="/register"><el-button>注册</el-button></router-link>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const searchKeyword = ref('')

function handleSearch() {
  if (searchKeyword.value.trim()) {
    router.push({ path: '/scenic-search', query: { keyword: searchKeyword.value } })
  }
}

function handleCommand(command) {
  if (command === 'logout') {
    userStore.logout()
    router.push('/')
  } else {
    router.push(`/user/${command}`)
  }
}
</script>

<style lang="scss" scoped>
// 穷游网风格 Header
$primary: #0086f6;
$primary-hover: #0070d6;
$text-primary: #333;
$text-secondary: #666;
$border-color: #e5e5e5;

.app-header {
  background: #fff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border-bottom: 1px solid $border-color;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  display: flex;
  align-items: center;
  height: 60px;
  gap: 30px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: 700;
  color: $primary;
  transition: opacity 0.2s;
  
  .el-icon {
    font-size: 24px;
    color: $primary;
  }
  
  &:hover {
    opacity: 0.85;
  }
}

.nav-menu {
  display: flex;
  gap: 2px;

  a {
    color: $text-primary;
    padding: 8px 12px;
    border-radius: 4px;
    font-size: 14px;
    font-weight: 500;
    transition: all 0.2s;
    display: inline-flex;
    align-items: center;
    gap: 4px;

    &:hover {
      color: $primary;
      background: rgba($primary, 0.06);
    }

    &.router-link-active {
      color: $primary;
      background: rgba($primary, 0.08);
    }

    &.ai-nav-link {
      margin-left: 4px;
      color: #6b46c1;
      background: linear-gradient(135deg, rgba(102, 126, 234, 0.12), rgba(118, 75, 162, 0.12));

      &:hover,
      &.router-link-active {
        color: #fff;
        background: linear-gradient(135deg, #667eea, #764ba2);
      }
    }
  }
}

.header-right {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 12px;
}

.search-box {
  display: flex;
  align-items: center;
  width: 220px;
  border: 1px solid $border-color;
  border-radius: 20px;
  background: #fff;
  transition: all 0.2s;
  overflow: hidden;

  &:hover, &:focus-within {
    border-color: $primary;
  }

  .search-input {
    flex: 1;

    :deep(.el-input__wrapper) {
      border-radius: 0;
      padding: 2px 8px 2px 14px;
      box-shadow: none;
      border: none;
      background: transparent;
    }
  }

  .search-btn {
    width: 40px;
    height: 36px;
    padding: 0;
    background: $primary;
    border: none;
    border-radius: 0;
    color: #fff;
    transition: all 0.2s;
    margin: 0;
    display: flex;
    align-items: center;
    justify-content: center;

    &:hover {
      background: $primary-hover;
    }

    .el-icon {
      font-size: 14px;
    }
  }
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 10px;
  border-radius: 20px;
  transition: background 0.2s;
  
  &:hover {
    background: #f5f5f5;
  }
  
  .el-avatar {
    background: $primary;
  }
}

.nickname {
  color: $text-primary;
  font-size: 14px;
}

// 登录/注册按钮
.header-right {
  :deep(.el-button--primary) {
    background: $primary;
    border: none;
    border-radius: 4px;
    padding: 8px 20px;
    font-size: 14px;
    
    &:hover {
      background: $primary-hover;
    }
  }
  
  :deep(.el-button:not(.el-button--primary)) {
    border-radius: 4px;
    padding: 8px 20px;
    border: 1px solid $border-color;
    color: $text-secondary;
    font-size: 14px;
    
    &:hover {
      border-color: $primary;
      color: $primary;
    }
  }
}

@media (max-width: 768px) {
  .nav-menu { display: none; }
  .header-content { height: 50px; gap: 12px; }
  .search-box { width: 40px; border-radius: 18px; }
  .search-btn { width: 36px; height: 32px; }
}
</style>
