<template>
  <div class="login-page">
    <div class="bg-layer" :class="{ active: showBgA }" :style="{ backgroundImage: `url(${bgA})` }"></div>
    <div class="bg-layer" :class="{ active: !showBgA }" :style="{ backgroundImage: `url(${bgB})` }"></div>
    <div class="bg-overlay"></div>
    <div class="login-container">
      <div class="login-banner">
        <h1>拾光旅行</h1>
        <p>发现世界的美好</p>
      </div>
      <div class="login-form-wrapper">
        <!-- 登录类型切换 -->
        <div class="login-type-tabs">
          <div 
            class="tab-item" 
            :class="{ active: loginType === 'user' }"
            @click="switchLoginType('user')"
          >
            <el-icon><User /></el-icon>
            <span>用户登录</span>
          </div>
          <div 
            class="tab-item" 
            :class="{ active: loginType === 'admin' }"
            @click="switchLoginType('admin')"
          >
            <el-icon><Setting /></el-icon>
            <span>管理员登录</span>
          </div>
        </div>

        <h2>{{ loginType === 'user' ? '用户登录' : '管理员登录' }}</h2>
        
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
          <el-form-item label="账号 (手机号/邮箱)" prop="account">
            <el-input v-model="form.account" placeholder="请输入手机号或邮箱" prefix-icon="User" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input 
              v-model="form.password" 
              type="password" 
              placeholder="请输入密码" 
              prefix-icon="Lock"
              show-password
            />
          </el-form-item>
          <el-form-item>
            <el-checkbox v-model="form.remember">记住我</el-checkbox>
          </el-form-item>
          <el-form-item>
            <el-button 
              :type="loginType === 'admin' ? 'danger' : 'primary'" 
              :loading="loading" 
              @click="handleLogin" 
              class="login-btn"
            >
              {{ loginType === 'admin' ? '管理员登录' : '用户登录' }}
            </el-button>
          </el-form-item>
        </el-form>
        
        <div class="register-link">
          还没有账号？<router-link :to="loginType === 'admin' ? '/register?type=admin' : '/register'">立即注册</router-link>
        </div>
        
        <!-- 测试账号提示 -->
        <div class="test-account-tip">
          <el-divider>测试账号</el-divider>
          <div v-if="loginType === 'admin'" class="tip-content">
            <p><strong>管理员：</strong>13800138000 / 123456</p>
          </div>
          <div v-else class="tip-content">
            <p><strong>普通用户：</strong>13800138001 / 123456</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Setting } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)
const loginType = ref('user') // 'user' 或 'admin'

const bgA = ref('')
const bgB = ref('')
const showBgA = ref(true)
let bgTimer = null
let bgSeed = 0

const form = reactive({
  account: '',
  password: '',
  remember: false
})

const rules = {
  account: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { pattern: /^(1[3-9]\d{9}|[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,})$/, message: '请输入正确的手机号或邮箱', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ]
}

function switchLoginType(type) {
  loginType.value = type
  // 清空表单
  form.account = ''
  form.password = ''
  formRef.value?.clearValidate()
}

function nextBackgroundUrl() {
  bgSeed += 1
  const seed = `${Date.now()}-${bgSeed}`
  return `https://picsum.photos/seed/${encodeURIComponent(seed)}/1920/1080`
}

function loadImage(url) {
  return new Promise((resolve, reject) => {
    const img = new Image()
    img.onload = () => resolve(url)
    img.onerror = reject
    img.src = url
  })
}

async function rotateBackground() {
  const nextUrl = nextBackgroundUrl()
  try {
    await loadImage(nextUrl)
  } catch {
    return
  }
  if (showBgA.value) {
    bgB.value = nextUrl
  } else {
    bgA.value = nextUrl
  }
  showBgA.value = !showBgA.value
}

onMounted(async () => {
  const first = nextBackgroundUrl()
  try {
    await loadImage(first)
    bgA.value = first
    bgB.value = first
  } catch {
    bgA.value = ''
    bgB.value = ''
  }
  bgTimer = window.setInterval(rotateBackground, 3000)
})

onBeforeUnmount(() => {
  if (bgTimer) window.clearInterval(bgTimer)
  bgTimer = null
})

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    // 将account字段转换为phone字段，因为后端期望的是phone
    const loginData = {
      phone: form.account,
      password: form.password,
      remember: form.remember
    }
    const result = await userStore.login(loginData)
    const userRole = result.user.role
    
    // 验证登录类型是否匹配
    if (loginType.value === 'admin' && userRole !== 'ADMIN') {
      ElMessage.error('该账号不是管理员，请使用用户登录')
      loading.value = false
      return
    }
    
    if (loginType.value === 'user' && userRole === 'ADMIN') {
      ElMessage.warning('检测到管理员账号，正在跳转到管理后台...')
    }
    
    ElMessage.success('登录成功')
    
    // 根据角色跳转到不同页面
    if (userRole === 'ADMIN') {
      router.push('/admin')
    } else {
      const redirect = route.query.redirect || '/'
      router.push(redirect)
    }
  } catch (e) {
    ElMessage.error(e.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
// 穷游网风格 Login
$primary: #0086f6;
$text-primary: #333;
$text-secondary: #666;

.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}
.login-container {
  display: flex;
  background: rgba(255, 255, 255, 0.32); // 更白的玻璃态卡片
  border-radius: 18px;
  overflow: hidden;
  box-shadow:
    0 18px 45px rgba(0, 0, 0, 0.18),
    inset 0 1px 0 rgba(255, 255, 255, 0.55),
    inset 0 0 0 1px rgba(255, 255, 255, 0.32);
  position: relative;
  z-index: 2;
  backdrop-filter: blur(18px) saturate(140%);
  -webkit-backdrop-filter: blur(18px) saturate(140%);
  border: 1px solid rgba(255,255,255,0.4);
  animation: slideDown 0.8s cubic-bezier(0.2, 0.8, 0.2, 1) forwards; // 添加进场动画
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-50px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.bg-layer {
  position: fixed;
  inset: 0;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  opacity: 0;
  transition: opacity 900ms ease;
  transform: scale(1.03);
  z-index: 0;
}
.bg-layer.active {
  opacity: 1;
}
.bg-overlay {
  position: fixed;
  inset: 0;
  background: linear-gradient(135deg, rgba(255,255,255,0.28), rgba(0,0,0,0.35)); // 更偏白的整体氛围
  z-index: 1;
}
.login-banner {
  width: 300px;
  padding: 60px 40px;
  background: #0d6efd; // 纯蓝色，不透明
  color: #fff;
  display: flex;
  flex-direction: column;
  justify-content: center;
  h1 { font-size: 32px; margin-bottom: 15px; font-weight: 700; }
  p { opacity: 0.9; font-size: 16px; }
}
.login-form-wrapper {
  width: 420px;
  padding: 40px;
  h2 { 
    margin-bottom: 30px; 
    text-align: center;
    font-size: 24px;
    font-weight: 500;
    color: $text-primary;
  }
}

.login-form-wrapper {
  background: rgba(255,255,255,0.60); // 表单区域更白更通透
}

.login-banner {
  background: #0d6efd; // 纯蓝色，不透明
  color: #fff;
}

.login-type-tabs {
  display: flex;
  gap: 15px; // 增加间距
  margin-bottom: 30px;
  
  .tab-item {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    padding: 10px;
    border: 1px solid #dcdfe6;
    border-radius: 4px;
    cursor: pointer;
    transition: all 0.2s;
    font-size: 15px;
    color: $text-secondary;
    background: #f5f7fa;
    
    &:hover {
      color: $primary;
      border-color: #b3d8ff;
      background: #ecf5ff;
    }
    
    &.active {
      border-color: $primary;
      background: #fff;
      color: $primary;
      font-weight: 500;
      box-shadow: 0 0 0 1px $primary inset;
    }
    
    .el-icon {
      font-size: 18px;
    }
  }
}

.login-btn { 
  width: 100%;
  height: 44px;
  font-size: 16px;
  background: $primary;
  border-color: $primary;
  border-radius: 4px;
  
  &:hover {
    background: #0070d6;
    border-color: #0070d6;
  }
}

.register-link {
  text-align: center;
  margin-top: 20px;
  color: $text-secondary;
  a { 
    color: $primary;
    text-decoration: none;
    font-weight: 500;
    &:hover {
      text-decoration: underline;
    }
  }
}

.test-account-tip {
  margin-top: 30px;
  padding: 15px;
  background: #f9f9f9;
  border-radius: 4px;
  border: 1px solid #eee;
  
  .el-divider {
    margin: 0 0 15px 0;
    background-color: #eee;
    
    :deep(.el-divider__text) {
      background-color: #f9f9f9;
      color: #999;
    }
  }
  
  .tip-content {
    font-size: 13px;
    color: $text-secondary;
    
    p {
      margin: 5px 0;
      line-height: 1.6;
    }
    
    strong {
      color: $text-primary;
    }
  }
}

@media (max-width: 768px) {
  .login-banner { display: none; }
  .login-form-wrapper { 
    width: 100%;
    max-width: 420px;
  }
}
</style>
