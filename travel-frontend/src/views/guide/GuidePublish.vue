<template>
  <div class="publish-container container">
    <el-breadcrumb separator="/">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item :to="{ path: '/guide' }">校园导游</el-breadcrumb-item>
      <el-breadcrumb-item>{{ isEdit ? '编辑攻略' : '发布攻略' }}</el-breadcrumb-item>
    </el-breadcrumb>

    <div class="publish-card">
      <h1 class="title">{{ isEdit ? '编辑导游攻略' : '发布导游攻略' }}</h1>
      <p class="subtitle">分享你的校园导游经验，帮助更多同学了解学校</p>

      <el-form :model="form" :rules="rules" ref="formRef" label-position="top" class="publish-form">
        <el-form-item label="攻略标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入攻略标题" />
        </el-form-item>

        <el-form-item label="攻略内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="10"
            placeholder="详细描述你的导游经验、推荐路线、注意事项等..."
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
            <el-button type="primary" size="large" @click="submitForm" :loading="submitting">发布</el-button>
            <el-button size="large" @click="router.back()">取消</el-button>
          </div>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { createGuide, updateGuide, getGuideDetail } from '@/api/guide'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
const submitting = ref(false)
const imageUrl = ref('')

const form = reactive({
  title: '',
  content: '',
  coverImage: ''
})

const rules = {
  title: [{ required: true, message: '请输入攻略标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入攻略内容', trigger: 'blur' }]
}

const isEdit = computed(() => !!route.params.id)

onMounted(async () => {
  if (isEdit.value) {
    await loadGuideDetail()
  }
})

async function loadGuideDetail() {
  try {
    const res = await getGuideDetail(route.params.id)
    form.title = res.title
    form.content = res.content
    imageUrl.value = res.coverImage
  } catch (error) {
    console.error('加载攻略详情失败:', error)
    ElMessage.error('加载攻略详情失败')
  }
}

function handleImageChange(file) {
  imageUrl.value = URL.createObjectURL(file.raw)
  form.coverImage = imageUrl.value // 模拟上传，直接使用本地URL
}

async function submitForm() {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        if (isEdit.value) {
          await updateGuide(route.params.id, form)
          ElMessage.success('更新成功！')
        } else {
          await createGuide(form)
          ElMessage.success('发布成功！')
        }
        router.push('/guide')
      } catch (error) {
        ElMessage.error(error.message || (isEdit.value ? '更新失败' : '发布失败'))
      } finally {
        submitting.value = false
      }
    }
  })
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