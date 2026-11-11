<template>
  <div class="register-page">
    <!-- 背景图层：和登录页一样，使用随机图片并轮播 -->
    <div class="bg-layer" :class="{ active: showBgA }" :style="{ backgroundImage: `url(${bgA})` }"></div>
    <div class="bg-layer" :class="{ active: !showBgA }" :style="{ backgroundImage: `url(${bgB})` }"></div>
    <div class="bg-overlay"></div>

    <div class="register-container">
      <h2>{{ isAdminRegister ? '管理员注册' : '用户注册' }}</h2>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="请再次输入密码" show-password />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="form.agree">
            我已阅读并同意<a href="#">《用户协议》</a>和<a href="#">《隐私政策》</a>
          </el-checkbox>
        </el-form-item>
        
        <el-form-item v-if="isAdminRegister" label="管理员密钥" prop="adminKey" :rules="[{ required: true, message: '请输入管理员密钥', trigger: 'blur' }]">
          <el-input v-model="form.adminKey" type="password" placeholder="请输入管理员注册密钥" show-password />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleRegister" class="register-btn">
            注册
          </el-button>
        </el-form-item>
      </el-form>
      <div class="login-link">
        已有账号？<router-link to="/login">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

// 背景相关：和登录页保持一致
const bgA = ref('')
const bgB = ref('')
const showBgA = ref(true)
let bgTimer = null
let bgSeed = 0

const form = reactive({
  phone: '',
  nickname: '',
  password: '',
  confirmPassword: '',
  agree: false,
  adminKey: ''
})

const isAdminRegister = ref(false)

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

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '昵称长度2-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

async function handleRegister() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  if (!form.agree) {
    ElMessage.warning('请阅读并同意用户协议')
    return
  }

  // 如果不是管理员注册，清空密钥，避免误传
  if (!isAdminRegister.value) {
    form.adminKey = ''
  }

  loading.value = true
  try {
    await userStore.register(form)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (error) {
    ElMessage.error(error?.message || '注册失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  if (route.query.type === 'admin') {
    isAdminRegister.value = true
  }
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
</script>

<style lang="scss" scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

/* 背景图层样式，与登录页面统一 */
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
  background: linear-gradient(135deg, rgba(255,255,255,0.28), rgba(0,0,0,0.35));
  z-index: 1;
}
.register-container {
  width: 420px;
  padding: 50px 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.3);
  position: relative;
  z-index: 2; // 置于背景与遮罩之上，形成独立卡片
  h2 { margin-bottom: 30px; text-align: center; }
}
.register-btn { width: 100%; }
.login-link {
  text-align: center;
  margin-top: 20px;
  color: #666;
  a { color: #409eff; }
}
</style>
