<template>
  <div class="group-chat container">
    <div class="chat-card">
      <div class="success-icon">🎉</div>
      <h1>拼团成功！</h1>
      <p class="subtitle" v-if="companion">{{ companion.title }}</p>
      
      <div class="qrcode-area">
        <div class="qrcode-placeholder">
          <div class="qr-icon">📱</div>
          <p class="qr-text">微信群聊二维码</p>
          <div class="qr-mock">
            <div class="qr-grid">
              <div v-for="i in 64" :key="i" class="qr-cell" :class="{ filled: Math.random() > 0.4 }"></div>
            </div>
          </div>
        </div>
      </div>
      
      <div class="tips">
        <el-alert 
          title="扫描上方二维码，加入拼团微信群" 
          type="success" 
          :closable="false"
          center
          show-icon
        />
        <div class="tip-list">
          <p>📌 添加群聊后，请在群内确认您的身份</p>
          <p>📌 群主会在出发前统一安排行程</p>
          <p>📌 如有问题，请联系发起人或平台客服</p>
        </div>
      </div>

      <div class="member-info" v-if="companion">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="目的地">{{ companion.destination || '未指定' }}</el-descriptions-item>
          <el-descriptions-item label="出发日期">{{ companion.departureDate || '未指定' }}</el-descriptions-item>
          <el-descriptions-item label="当前人数">{{ companion.currentCount }}/{{ companion.memberCount }}人</el-descriptions-item>
          <el-descriptions-item label="发起人">{{ companion.creatorName || '匿名' }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <div class="actions">
        <el-button type="primary" @click="goBack">返回结伴详情</el-button>
        <el-button @click="goList">返回列表</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getGroupChat } from '@/api/companion'

const route = useRoute()
const router = useRouter()
const companion = ref(null)

onMounted(async () => {
  try {
    const id = route.params.id
    const res = await getGroupChat(id)
    companion.value = res
  } catch (error) {
    ElMessage.error(error.message || '获取群聊信息失败')
  }
})

function goBack() {
  router.push(`/companion/${route.params.id}`)
}

function goList() {
  router.push('/companion')
}
</script>

<style scoped>
.group-chat {
  padding: 40px 0;
  display: flex;
  justify-content: center;
}

.chat-card {
  background: #fff;
  border-radius: 16px;
  padding: 50px 40px;
  box-shadow: 0 8px 30px rgba(0,0,0,0.08);
  max-width: 600px;
  width: 100%;
  text-align: center;
}

.success-icon {
  font-size: 60px;
  margin-bottom: 10px;
}

h1 {
  margin: 0 0 8px 0;
  font-size: 28px;
  color: #67c23a;
}

.subtitle {
  color: #606266;
  font-size: 16px;
  margin-bottom: 30px;
}

.qrcode-area {
  margin-bottom: 30px;
}

.qrcode-placeholder {
  display: inline-block;
  padding: 20px;
  border: 2px dashed #dcdfe6;
  border-radius: 12px;
  background: #fafafa;
}

.qr-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.qr-text {
  font-size: 14px;
  color: #909399;
  margin-bottom: 12px;
}

.qr-mock {
  width: 160px;
  height: 160px;
  margin: 0 auto;
  background: white;
  padding: 8px;
  border-radius: 4px;
}

.qr-grid {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 2px;
  width: 100%;
  height: 100%;
}

.qr-cell {
  background: #f0f0f0;
  border-radius: 1px;
}

.qr-cell.filled {
  background: #303133;
}

.tips {
  margin-bottom: 30px;
}

.tip-list {
  margin-top: 15px;
  text-align: left;
  padding: 0 20px;
}

.tip-list p {
  color: #606266;
  font-size: 14px;
  line-height: 2;
  margin: 0;
}

.member-info {
  margin-bottom: 30px;
}

.actions {
  display: flex;
  gap: 15px;
  justify-content: center;
}
</style>
