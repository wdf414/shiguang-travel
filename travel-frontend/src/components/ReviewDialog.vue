<template>
  <el-dialog
    v-model="visible"
    :title="title"
    width="500px"
    @close="handleClose"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
      <el-form-item label="评分" prop="rating">
        <el-rate v-model="form.rating" show-text />
      </el-form-item>
      
      <el-form-item label="评价内容" prop="content">
        <el-input
          v-model="form.content"
          type="textarea"
          :rows="4"
          placeholder="分享您的体验..."
        />
      </el-form-item>

      <el-form-item label="标签" prop="tags">
        <el-select
          v-model="form.tags"
          multiple
          filterable
          allow-create
          default-first-option
          :reserve-keyword="false"
          placeholder="选择或输入标签（如：性价比高）"
        >
          <el-option
            v-for="item in predefinedTags"
            :key="item"
            :label="item"
            :value="item"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="图片">
        <el-upload
          action="#"
          list-type="picture-card"
          :auto-upload="false"
          :on-change="handleFileChange"
          :on-remove="handleFileRemove"
          :file-list="fileList"
        >
          <el-icon><Plus /></el-icon>
        </el-upload>
      </el-form-item>
    </el-form>
    
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          提交
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { submitReview } from '@/api/review'

const props = defineProps({
  modelValue: Boolean,
  type: {
    type: String,
    required: true, // 'line', 'hotel', 'guide'
    validator: (value) => ['line', 'hotel', 'guide'].includes(value)
  },
  id: {
    type: [String, Number],
    required: true
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const title = computed(() => {
  const map = { line: '线路评价', hotel: '酒店评价', guide: '导游/攻略评价' }
  return map[props.type] || '评价'
})

const predefinedTags = computed(() => {
  const map = {
    line: ['行程合理', '导游专业', '住宿舒适', '性价比高', '风景优美'],
    hotel: ['房间整洁', '服务周到', '交通便利', '设施齐全', '早餐丰富'],
    guide: ['讲解生动', '服务热情', '经验丰富', '行程规划好', '避坑指南']
  }
  return map[props.type] || []
})

const formRef = ref(null)
const submitting = ref(false)
const fileList = ref([])

const form = reactive({
  rating: 5,
  content: '',
  tags: []
})

const rules = {
  rating: [{ required: true, message: '请打分', trigger: 'change' }],
  content: [{ required: true, message: '请输入评价内容', trigger: 'blur' }]
}

function handleClose() {
  formRef.value?.resetFields()
  form.rating = 5
  form.content = ''
  form.tags = []
  fileList.value = []
}

function handleFileChange(uploadFile, uploadFiles) {
  fileList.value = uploadFiles
}

function handleFileRemove(uploadFile, uploadFiles) {
  fileList.value = uploadFiles
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        // 模拟图片上传，实际应上传到服务器获取URL
        // 这里直接使用 blob URL 或 base64，或者假装上传成功
        const images = fileList.value.map(f => f.url || URL.createObjectURL(f.raw))
        
        const data = {
          rating: form.rating,
          content: form.content,
          tags: form.tags.join(','), // 转换为字符串
          images: JSON.stringify(images) // 转换为JSON字符串
        }
        
        // 根据类型设置ID
        if (props.type === 'line') data.lineId = props.id
        else if (props.type === 'hotel') data.hotelId = props.id
        else if (props.type === 'guide') data.guideId = props.id

        await submitReview(data)
        ElMessage.success('评价提交成功')
        emit('success')
        visible.value = false
      } catch (error) {
        console.error(error)
      } finally {
        submitting.value = false
      }
    }
  })
}
</script>

<style scoped>
.el-rate {
  margin-top: 6px;
}
</style>
