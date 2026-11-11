<template>
  <div class="travel-note-create container">
    <div class="page-header">
      <h2>写游记</h2>
      <div class="actions">
        <el-button @click="goBack">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitNote">发布游记</el-button>
      </div>
    </div>

    <div class="create-form">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" size="large">
        
        <!-- 封面图上传 -->
        <el-form-item prop="coverImage" label="封面图">
          <div class="cover-uploader">
            <img v-if="form.coverImage" :src="form.coverImage" class="cover-preview" />
            <el-upload
              v-else
              class="uploader"
              action="#"
              :auto-upload="false"
              :show-file-list="false"
              :on-change="handleCoverChange"
            >
              <el-icon class="uploader-icon"><Plus /></el-icon>
              <div class="upload-text">点击上传封面</div>
            </el-upload>
            <div v-if="form.coverImage" class="re-upload">
              <el-upload
                action="#"
                :auto-upload="false"
                :show-file-list="false"
                :on-change="handleCoverChange"
              >
                <el-button text type="primary">更换封面</el-button>
              </el-upload>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="标题" prop="title">
          <el-input 
            v-model="form.title" 
            placeholder="填写游记标题，吸引更多人看..." 
            maxlength="100" 
            show-word-limit 
          />
        </el-form-item>

        <el-form-item label="目的地" prop="destination">
          <el-input v-model="form.destination" placeholder="请输入目的地，如：云南" />
        </el-form-item>

        <div class="row">
          <el-form-item label="出发日期" prop="travelDate">
            <el-date-picker 
              v-model="form.travelDate" 
              type="date" 
              placeholder="选择出发日期" 
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="游玩天数" prop="days">
            <el-input-number v-model="form.days" :min="1" />
          </el-form-item>
          <el-form-item label="人均花费" prop="cost">
            <el-input-number v-model="form.cost" :min="0" :step="100" />
          </el-form-item>
        </div>

        <el-form-item label="正文内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="15"
            placeholder="在这里分享你的精彩旅程..."
          />
          <div class="tips">目前仅支持纯文本和封面图，完整富文本功能开发中...</div>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'
import { createNote } from '@/api/travel-note'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const router = useRouter()
const formRef = ref()
const submitting = ref(false)

const form = reactive({
  title: '',
  coverImage: '',
  destination: '',
  travelDate: '',
  days: 1,
  cost: 0,
  content: ''
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }],
  destination: [{ required: true, message: '请输入目的地', trigger: 'blur' }]
}

// 模拟上传
function handleCoverChange(file) {
  const reader = new FileReader()
  reader.onload = (e) => {
    form.coverImage = e.target.result // 这里直接使用base64模拟
  }
  reader.readAsDataURL(file.raw)
}

async function submitNote() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  // 敏感词检测
  const contentToCheck = form.title + ' ' + form.content
  const checkRes = await checkSensitiveWords(contentToCheck)
  if (checkRes.hasSensitive) {
    ElMessage.warning(`检测到敏感词：${checkRes.words.join('、')}，请修改后提交`)
    return
  }

  submitting.value = true
  try {
    // 简单的换行转HTML
    const htmlContent = form.content.replace(/\n/g, '<br/>')
    
    await createNote({
      ...form,
      content: htmlContent
    })
    
    ElMessage.success('发布成功，请等待审核通过')
    router.push('/travel-notes')
  } catch (e) {
    console.error('发布游记失败:', e)
    // request.js 已经处理了错误提示，这里不需要重复提示
  } finally {
    submitting.value = false
  }
}

// 检测敏感词
async function checkSensitiveWords(content) {
  try {
    const res = await request.post('/admin/review/check-sensitive', { content })
    if (res) {
      return res
    }
  } catch (e) {
    console.error('敏感词检测失败', e)
  }
  return { hasSensitive: false, words: [] }
}

function goBack() {
  router.back()
}
</script>

<style lang="scss" scoped>
.travel-note-create {
  padding-top: 30px;
  padding-bottom: 60px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  
  h2 {
    font-size: 24px;
    font-weight: 700;
  }
}

.create-form {
  background: #fff;
  padding: 40px;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  max-width: 900px;
  margin: 0 auto;
}

.cover-uploader {
  width: 100%;
  height: 300px;
  background: #f5f7fa;
  border: 1px dashed #dcdfe6;
  border-radius: 8px;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  overflow: hidden;
  margin-bottom: 20px;
  
  .uploader {
    text-align: center;
    cursor: pointer;
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    
    &:hover {
      border-color: #409eff;
      color: #409eff;
    }
  }
  
  .uploader-icon {
    font-size: 40px;
    color: #8c939d;
    margin-bottom: 10px;
  }
  
  .cover-preview {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
  
  .re-upload {
    position: absolute;
    bottom: 10px;
    right: 10px;
    background: rgba(255,255,255,0.9);
    padding: 5px 10px;
    border-radius: 4px;
  }
}

.row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.tips {
  margin-top: 10px;
  font-size: 12px;
  color: #999;
}
</style>
