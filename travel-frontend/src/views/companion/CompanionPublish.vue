<template>
  <div class="publish-container container">
    <el-breadcrumb separator="/">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item :to="{ path: '/companion' }">结伴游</el-breadcrumb-item>
      <el-breadcrumb-item>发起结伴</el-breadcrumb-item>
    </el-breadcrumb>

    <div class="publish-card">
      <h1 class="title">发起结伴邀请</h1>
      <p class="subtitle">寻找志同道合的校友，开启一段难忘的旅程</p>

      <!-- 领队标识 -->
      <div v-if="isLeader" class="leader-banner">
        🎓 您是认证校园领队，发布的结伴将显示领队标识
      </div>
      <div v-else-if="userStore.userInfo?.isStudent === 1" class="apply-leader-tip">
        <span>🌟 您是学生用户，</span>
        <el-button type="primary" link @click="showApplyDialog = true">点此申请成为校园领队</el-button>
        <span>，享受佣金分成！</span>
      </div>

      <el-form :model="form" :rules="rules" ref="formRef" label-position="top" class="publish-form">
        <el-form-item label="活动标题" prop="title">
          <el-input v-model="form.title" placeholder="如：3月云南大理丽江结伴，求2位有趣的校友" />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="目的地" prop="destination">
              <el-input v-model="form.destination" placeholder="想要去哪里？" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="关联线路" prop="lineId">
              <el-select v-model="form.lineId" placeholder="选择平台线路（可选）" style="width: 100%" clearable>
                <el-option v-for="line in lines" :key="line.id" :label="line.title" :value="line.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="出发日期" prop="startDate">
              <el-date-picker v-model="form.startDate" type="date" placeholder="预计出发时间" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预计天数" prop="days">
              <el-input-number v-model="form.days" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="招募人数" prop="maxMembers">
              <el-input-number v-model="form.maxMembers" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="预估人均费用" prop="budget">
              <el-input v-model="form.budget" placeholder="如：3000-5000">
                <template #append>元</template>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="限定学校（可选）">
              <el-select 
                v-model="form.school" 
                placeholder="选择或输入学校名称" 
                style="width: 100%" 
                clearable
                filterable
                allow-create
                default-first-option
              >
                <el-option v-for="s in schoolOptions" :key="s" :label="s" :value="s" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="详细说明" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="6"
            placeholder="描述一下你的行程计划、对队友的要求、你的性格特点等..."
          />
        </el-form-item>

        <el-form-item label="封面图片" prop="coverImage">
          <el-upload
            class="cover-uploader"
            action="#"
            :show-file-list="false"
            :auto-upload="false"
            @change="handleImageChange"
          >
            <img v-if="imageUrl" :src="imageUrl" class="cover-image" />
            <el-icon v-else class="uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>

        <el-form-item>
          <div class="form-actions">
            <el-button type="primary" size="large" @click="submitForm" :loading="submitting">立即发布</el-button>
            <el-button size="large" @click="router.back()">取消</el-button>
          </div>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getLineList } from '@/api/line'
import { createCompanion, getLeaderInfo } from '@/api/companion'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const submitting = ref(false)
const lines = ref([])
const imageUrl = ref('')
const isLeader = ref(false)
const showApplyDialog = ref(false)

const schoolOptions = ref([
  '北京大学', '清华大学', '复旦大学', '浙江大学', '中山大学',
  '武汉大学', '南京大学', '四川大学', '华中科技大学', '厦门大学',
  '西安交通大学', '同济大学', '华南理工大学', '中南大学', '天津大学',
  '哈尔滨工业大学', '山东大学', '东南大学', '吉林大学', '北京航空航天大学'
])

const form = reactive({
  title: '',
  destination: '',
  lineId: '',
  startDate: '',
  days: 1,
  maxMembers: 2,
  budget: '',
  content: '',
  coverImage: '',
  school: ''
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  destination: [{ required: true, message: '请输入目的地', trigger: 'blur' }],
  startDate: [{ required: true, message: '请选择出发日期', trigger: 'change' }],
  content: [{ required: true, message: '请输入详细说明', trigger: 'blur' }]
}

onMounted(async () => {
  try {
    const res = await getLineList({ size: 20 })
    lines.value = res.records
  } catch (error) {
    console.error('Fetch lines failed', error)
  }
  // 检查是否是认证领队
  try {
    const leaderInfo = await getLeaderInfo()
    if (leaderInfo && leaderInfo.status === 1) {
      isLeader.value = true
    }
  } catch (e) {
    // 未登录或未申请，忽略
  }
})

function handleImageChange(file) {
  imageUrl.value = URL.createObjectURL(file.raw)
  form.coverImage = imageUrl.value // 模拟上传，直接使用本地URL
}

async function submitForm() {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitting.value = true
    try {
      await createCompanion(form)
      submitting.value = false
      ElMessage.success('发布成功！')
      router.push('/companion')
    } catch (error) {
      submitting.value = false
      ElMessage.error(error.message || '发布失败')
    }
  } catch (e) {
    // 验证失败
    return
  }
}
</script>

<style scoped>
.publish-container {
  padding: 40px 20px;
}

.publish-card {
  background: #fff;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.05);
  margin-top: 20px;
  max-width: 800px;
  margin-left: auto;
  margin-right: auto;
}

.title {
  font-size: 28px;
  color: #303133;
  margin-bottom: 8px;
  text-align: center;
}

.subtitle {
  color: #909399;
  text-align: center;
  margin-bottom: 40px;
}

.publish-form {
  max-width: 600px;
  margin: 0 auto;
}

.cover-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 200px;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: border-color 0.3s;
}

.cover-uploader:hover {
  border-color: #409EFF;
}

.uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 300px;
  height: 180px;
  text-align: center;
  line-height: 180px;
}

.leader-banner {
  padding: 12px 16px;
  background: linear-gradient(135deg, #fff3e0, #ffe0b2);
  border-radius: 8px;
  color: #e65100;
  font-weight: bold;
  margin-bottom: 20px;
}

.apply-leader-tip {
  padding: 12px 16px;
  background: #f0f7ff;
  border-radius: 8px;
  color: #409eff;
  margin-bottom: 20px;
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.form-actions {
  display: flex;
  gap: 20px;
  margin-top: 20px;
}

.form-actions .el-button {
  flex: 1;
}
</style>
