<template>
  <div class="student-auth-page">
    <el-card class="auth-card">
      <template #header>
        <div class="card-header">
          <h2>🎓 学生认证</h2>
          <p class="subtitle">认证通过后享受专属学生权益</p>
        </div>
      </template>

      <!-- 认证状态展示 -->
      <div v-if="authStatus === 2" class="auth-success">
        <el-result
          icon="success"
          title="认证通过"
          sub-title="您已通过学生认证，享受专属权益"
        >
          <template #extra>
            <div class="student-info">
              <p><strong>学校：</strong>{{ studentInfo.school }}</p>
              <p><strong>专业：</strong>{{ studentInfo.major }}</p>
              <p><strong>年级：</strong>{{ studentInfo.grade }}</p>
              <p><strong>累计节省：</strong>¥{{ studentInfo.discountTotalSaved || 0 }}</p>
            </div>
            <el-button type="primary" @click="showEditDialog = true">
              修改信息
            </el-button>
          </template>
        </el-result>
      </div>

      <!-- 待审核状态 -->
      <div v-else-if="authStatus === 1" class="auth-pending">
        <el-result
          icon="info"
          title="审核中"
          sub-title="您的申请已提交，请等待管理员审核"
        >
          <template #extra>
            <p>申请时间：{{ applicationInfo?.createTime }}</p>
            <el-button @click="refreshStatus">刷新状态</el-button>
          </template>
        </el-result>
      </div>

      <!-- 审核拒绝 -->
      <div v-else-if="authStatus === 3" class="auth-rejected">
        <el-result
          icon="error"
          title="审核未通过"
          :sub-title="applicationInfo?.rejectReason || '请检查提交的信息后重新申请'"
        >
          <template #extra>
            <el-button type="primary" @click="resetForm">重新申请</el-button>
          </template>
        </el-result>
      </div>

      <!-- 认证表单 -->
      <div v-else class="auth-form">
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="120px"
          class="auth-form-content"
        >
          <el-divider>基本信息</el-divider>
          
          <el-form-item label="真实姓名" prop="realName">
            <el-input v-model="form.realName" placeholder="请输入真实姓名" />
          </el-form-item>

          <el-form-item label="学校名称" prop="schoolName">
            <el-input v-model="form.schoolName" placeholder="请输入学校全称" />
          </el-form-item>

          <el-form-item label="专业" prop="major">
            <el-input v-model="form.major" placeholder="请输入专业名称" />
          </el-form-item>

          <el-form-item label="年级" prop="grade">
            <el-select v-model="form.grade" placeholder="请选择年级" style="width: 100%">
              <el-option label="2024级" value="2024级" />
              <el-option label="2023级" value="2023级" />
              <el-option label="2022级" value="2022级" />
              <el-option label="2021级" value="2021级" />
              <el-option label="2020级" value="2020级" />
            </el-select>
          </el-form-item>

          <el-form-item label="学号" prop="studentId">
            <el-input v-model="form.studentId" placeholder="请输入学号" />
          </el-form-item>

          <el-divider>认证材料</el-divider>

          <el-form-item label="学生证照片" prop="studentCard">
            <el-upload
              class="avatar-uploader"
              action="/api/upload"
              :show-file-list="false"
              :on-success="handleStudentCardSuccess"
              :before-upload="beforeUpload"
            >
              <img v-if="form.studentCard" :src="form.studentCard" class="avatar" />
              <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
            </el-upload>
            <p class="upload-tip">请上传学生证照片（含学校名称、姓名、学号）</p>
          </el-form-item>

          <el-form-item label="身份证正面" prop="idCardFront">
            <el-upload
              class="avatar-uploader"
              action="/api/upload"
              :show-file-list="false"
              :on-success="handleIdCardFrontSuccess"
              :before-upload="beforeUpload"
            >
              <img v-if="form.idCardFront" :src="form.idCardFront" class="avatar" />
              <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
            </el-upload>
          </el-form-item>

          <el-form-item label="身份证反面" prop="idCardBack">
            <el-upload
              class="avatar-uploader"
              action="/api/upload"
              :show-file-list="false"
              :on-success="handleIdCardBackSuccess"
              :before-upload="beforeUpload"
            >
              <img v-if="form.idCardBack" :src="form.idCardBack" class="avatar" />
              <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
            </el-upload>
          </el-form-item>

          <el-form-item label="学信网截图" prop="chsiScreenshot">
            <el-upload
              class="avatar-uploader"
              action="/api/upload"
              :show-file-list="false"
              :on-success="handleChsiSuccess"
              :before-upload="beforeUpload"
            >
              <img v-if="form.chsiScreenshot" :src="form.chsiScreenshot" class="avatar" />
              <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
            </el-upload>
            <p class="upload-tip">可选：上传学信网学籍验证截图可加快审核</p>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="submitForm" :loading="submitting">
              提交认证申请
            </el-button>
            <el-button @click="resetForm">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <!-- 学生权益说明 -->
    <el-card class="benefits-card">
      <template #header>
        <h3>🎁 学生专属权益</h3>
      </template>
      <div class="benefits-list">
        <div class="benefit-item">
          <el-icon><Discount /></el-icon>
          <div>
            <h4>专属折扣</h4>
            <p>线路/酒店享受95折优惠，最高可省500元</p>
          </div>
        </div>
        <div class="benefit-item">
          <el-icon><Ticket /></el-icon>
          <div>
            <h4>学生友好标签</h4>
            <p>筛选支持学生免押金、学生票优惠的产品</p>
          </div>
        </div>
        <div class="benefit-item">
          <el-icon><Calendar /></el-icon>
          <div>
            <h4>校园专属活动</h4>
            <p>毕业旅行、寒暑假特惠等专属活动</p>
          </div>
        </div>
        <div class="benefit-item">
          <el-icon><Medal /></el-icon>
          <div>
            <h4>身份标识</h4>
            <p>个人中心展示学生认证标识，增加信任度</p>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 修改信息对话框 -->
    <el-dialog v-model="showEditDialog" title="修改学生信息" width="500px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="学校">
          <el-input v-model="editForm.school" />
        </el-form-item>
        <el-form-item label="专业">
          <el-input v-model="editForm.major" />
        </el-form-item>
        <el-form-item label="年级">
          <el-select v-model="editForm.grade" style="width: 100%">
            <el-option label="2024级" value="2024级" />
            <el-option label="2023级" value="2023级" />
            <el-option label="2022级" value="2022级" />
            <el-option label="2021级" value="2021级" />
            <el-option label="2020级" value="2020级" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="updateInfo" :loading="updating">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Discount, Ticket, Calendar, Medal } from '@element-plus/icons-vue'
import { submitStudentAuth, getStudentAuthStatus, updateStudentInfo } from '@/api/student'

const formRef = ref(null)
const submitting = ref(false)
const authStatus = ref(0) // 0-未认证 1-待审核 2-已认证 3-已拒绝
const studentInfo = ref({})
const applicationInfo = ref(null)
const showEditDialog = ref(false)
const updating = ref(false)

const form = reactive({
  realName: '',
  schoolName: '',
  major: '',
  grade: '',
  studentId: '',
  studentCard: '',
  idCardFront: '',
  idCardBack: '',
  chsiScreenshot: ''
})

const editForm = reactive({
  school: '',
  major: '',
  grade: ''
})

const rules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  schoolName: [{ required: true, message: '请输入学校名称', trigger: 'blur' }],
  major: [{ required: true, message: '请输入专业', trigger: 'blur' }],
  grade: [{ required: true, message: '请选择年级', trigger: 'change' }],
  studentId: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  studentCard: [{ required: true, message: '请上传学生证照片', trigger: 'change' }],
  idCardFront: [{ required: true, message: '请上传身份证正面', trigger: 'change' }],
  idCardBack: [{ required: true, message: '请上传身份证反面', trigger: 'change' }]
}

onMounted(() => {
  fetchAuthStatus()
})

async function fetchAuthStatus() {
  try {
    const res = await getStudentAuthStatus()
    authStatus.value = res.authStatus || 0
    studentInfo.value = res
    applicationInfo.value = res.application
    
    if (res.school) {
      editForm.school = res.school
      editForm.major = res.major
      editForm.grade = res.grade
    }
  } catch (error) {
    console.error('获取认证状态失败:', error)
  }
}

function submitForm() {
  formRef.value?.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      await submitStudentAuth(form)
      ElMessage.success('申请提交成功，请等待审核')
      authStatus.value = 1
      fetchAuthStatus()
    } catch (error) {
      ElMessage.error(error.message || '提交失败')
    } finally {
      submitting.value = false
    }
  })
}

function resetForm() {
  formRef.value?.resetFields()
  Object.keys(form).forEach(key => {
    form[key] = ''
  })
}

async function updateInfo() {
  updating.value = true
  try {
    await updateStudentInfo(editForm)
    ElMessage.success('信息更新成功')
    showEditDialog.value = false
    fetchAuthStatus()
  } catch (error) {
    ElMessage.error('更新失败')
  } finally {
    updating.value = false
  }
}

function refreshStatus() {
  fetchAuthStatus()
  ElMessage.success('状态已刷新')
}

// 上传相关
function beforeUpload(file) {
  const isJPG = file.type === 'image/jpeg'
  const isPNG = file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG && !isPNG) {
    ElMessage.error('只支持 JPG/PNG 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

function handleStudentCardSuccess(res) {
  form.studentCard = res.data
  ElMessage.success('上传成功')
}

function handleIdCardFrontSuccess(res) {
  form.idCardFront = res.data
  ElMessage.success('上传成功')
}

function handleIdCardBackSuccess(res) {
  form.idCardBack = res.data
  ElMessage.success('上传成功')
}

function handleChsiSuccess(res) {
  form.chsiScreenshot = res.data
  ElMessage.success('上传成功')
}
</script>

<style scoped lang="scss">
.student-auth-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.auth-card {
  margin-bottom: 20px;
}

.card-header {
  text-align: center;
  
  h2 {
    margin: 0 0 10px;
    color: #303133;
  }
  
  .subtitle {
    margin: 0;
    color: #909399;
    font-size: 14px;
  }
}

.auth-form-content {
  max-width: 500px;
  margin: 0 auto;
}

.avatar-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 178px;
  height: 178px;
}

.avatar-uploader:hover {
  border-color: #409eff;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  text-align: center;
  line-height: 178px;
}

.avatar {
  width: 178px;
  height: 178px;
  display: block;
  object-fit: cover;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.student-info {
  text-align: left;
  background: #f5f7fa;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
  
  p {
    margin: 8px 0;
    color: #606266;
  }
}

.benefits-card {
  h3 {
    margin: 0;
    color: #303133;
  }
}

.benefits-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.benefit-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 8px;
  
  .el-icon {
    font-size: 24px;
    color: #409eff;
    margin-top: 2px;
  }
  
  h4 {
    margin: 0 0 5px;
    color: #303133;
  }
  
  p {
    margin: 0;
    font-size: 13px;
    color: #606266;
  }
}

@media (max-width: 768px) {
  .benefits-list {
    grid-template-columns: 1fr;
  }
}
</style>
