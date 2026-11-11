import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 登录和注册接口不需要token
    const isLoginOrRegister = config.url === '/user/login' || config.url === '/user/register'
    
    if (!isLoginOrRegister) {
      const userStore = useUserStore()
      if (userStore.token) {
        config.headers.Authorization = `Bearer ${userStore.token}`
      }
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const { code, msg, data } = response.data
    if (code === 200) {
      return data
    }
    ElMessage.error(msg || '请求失败')
    return Promise.reject(new Error(msg))
  },
  error => {
    console.error('请求错误:', error)
    
    if (error.response) {
      const status = error.response.status
      const data = error.response.data
      
      if (status === 401) {
        const userStore = useUserStore()
        userStore.logout()
        router.push('/login')
        ElMessage.error('登录已过期，请重新登录')
      } else if (status === 403) {
        ElMessage.error(data?.msg || '没有权限访问')
      } else if (status === 404) {
        ElMessage.error('请求的资源不存在')
      } else if (status === 500) {
        ElMessage.error(data?.msg || '服务器错误')
      } else {
        ElMessage.error(data?.msg || error.message || '请求失败')
      }
    } else if (error.request) {
      ElMessage.error('网络连接失败，请检查后端是否启动')
    } else {
      ElMessage.error(error.message || '请求配置错误')
    }
    
    return Promise.reject(error)
  }
)

export default request
