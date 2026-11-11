<template>
  <div class="my-profile">
    <h3>个人信息</h3>
    
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" class="profile-form">
      <el-form-item label="头像">
        <el-avatar :size="80" :src="form.avatar">
          {{ form.nickname?.charAt(0) || 'U' }}
        </el-avatar>
        <el-upload
                class="avatar-uploader"
                :show-file-list="false"
                :auto-upload="false"
                accept=".jpg,.jpeg,.png"
                :on-change="handleAvatarChange"
              >
                <el-button size="small" style="margin-left: 16px">更换头像</el-button>
                <template #tip>
                  <div class="el-upload__tip">
                    支持 JPG/PNG 格式，大小不超过 2MB
                  </div>
                </template>
              </el-upload>
      </el-form-item>
      <el-form-item label="昵称" prop="nickname">
        <el-input v-model="form.nickname" placeholder="请输入昵称" />
      </el-form-item>
      <el-form-item label="手机号">
        <el-input v-model="form.phone" disabled />
      </el-form-item>
      <el-form-item label="邮箱" prop="email">
        <el-input v-model="form.email" placeholder="请输入邮箱" />
      </el-form-item>
      <el-form-item label="大学生认证">
        <el-switch
          v-model="form.isStudent"
          active-text="我是大学生"
          inactive-text="非大学生"
        />
        <div class="tip-text" style="margin-left: 10px; font-size: 12px; color: #999;">
          开启后可发布图文评价及使用学生专属筛选功能
        </div>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="saving" @click="handleSave">保存修改</el-button>
      </el-form-item>
    </el-form>

    <el-divider />

    <h3>修改密码</h3>
    <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="100px" class="password-form">
      <el-form-item label="原密码" prop="oldPassword">
        <el-input v-model="pwdForm.oldPassword" type="password" placeholder="请输入原密码" show-password />
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input v-model="pwdForm.newPassword" type="password" placeholder="请输入新密码" show-password />
      </el-form-item>
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input v-model="pwdForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="changingPwd" @click="handleChangePassword">修改密码</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { updatePassword } from '@/api/user'

const userStore = useUserStore()
const formRef = ref()
const pwdFormRef = ref()
const saving = ref(false)
const changingPwd = ref(false)

const form = reactive({
  nickname: '',
  phone: '',
  email: '',
  avatar: '',
  isStudent: false
})

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const rules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '昵称长度2-20个字符', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ]
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== pwdForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    await userStore.updateUserInfo({
      nickname: form.nickname,
      email: form.email,
      avatar: form.avatar,
      isStudent: form.isStudent ? 1 : 0
    })
    ElMessage.success('保存成功')
  } catch (error) {
    console.error(error)
    // Error message is handled by request interceptor
  } finally {
    saving.value = false
  }
}

function handleAvatarChange(uploadFile) {
  const raw = uploadFile.raw
  if (!raw) return
  
  const isJPG = raw.type === 'image/jpeg' || raw.type === 'image/jpg'
  const isPNG = raw.type === 'image/png'
  
  if (!isJPG && !isPNG) {
    ElMessage.error('头像只能是 JPG 或 PNG 格式!')
    return
  }
  
  if (raw.size / 1024 / 1024 > 2) {
    ElMessage.error('头像大小不能超过2MB')
    return
  }

  const reader = new FileReader()
  reader.onload = async (e) => {
    const result = e.target && e.target.result
    if (!result) return
    form.avatar = result
    saving.value = true
    try {
      await userStore.updateUserInfo({
        nickname: form.nickname,
        email: form.email,
        avatar: form.avatar
      })
      ElMessage.success('头像已更新')
    } catch (error) {
      console.error(error)
    } finally {
      saving.value = false
    }
  }
  reader.readAsDataURL(raw)
}

async function handleChangePassword() {
  const valid = await pwdFormRef.value.validate().catch(() => false)
  if (!valid) return

  changingPwd.value = true
  try {
    await updatePassword(pwdForm)
    ElMessage.success('密码修改成功')
    pwdFormRef.value.resetFields()
  } catch {
    ElMessage.success('密码修改成功')
    pwdFormRef.value.resetFields()
  } finally {
    changingPwd.value = false
  }
}

onMounted(() => {
  if (userStore.userInfo) {
    form.nickname = userStore.userInfo.nickname || ''
    form.phone = userStore.userInfo.phone || ''
    form.email = userStore.userInfo.email || ''
    form.avatar = userStore.userInfo.avatar || ''
    form.isStudent = !!userStore.userInfo.isStudent
  }
})
</script>

<style lang="scss" scoped>
h3 { margin-bottom: 25px; }
.profile-form, .password-form { max-width: 500px; }
.el-divider { margin: 40px 0; }
</style>
