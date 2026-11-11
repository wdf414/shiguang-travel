<template>
  <div class="companion-detail container" v-loading="loading">
    <div v-if="companion" class="detail-content">
      <div class="header">
        <div class="title-area">
          <h1>{{ companion.title }}</h1>
          <el-tag v-if="companion.isLeader === 1" type="danger" size="large" class="leader-tag">
            🎓 校园领队发起
          </el-tag>
        </div>
        <div class="status-tags">
          <el-tag 
            v-if="companion.status === 0" 
            type="warning" 
            size="large"
          >
            招募中
          </el-tag>
          <el-tag 
            v-else-if="companion.status === 1" 
            type="success" 
            size="large"
          >
            已成行
          </el-tag>
          <el-tag 
            v-else 
            type="info" 
            size="large"
          >
            已取消
          </el-tag>
        </div>
      </div>

      <div class="basic-info">
        <div class="info-item">
          <el-icon><Location /></el-icon>
          <span>目的地：{{ companion.destination || '未指定' }}</span>
        </div>
        <div class="info-item">
          <el-icon><Calendar /></el-icon>
          <span>出发日期：{{ companion.departureDate || '未指定' }}</span>
        </div>
        <div class="info-item">
          <el-icon><User /></el-icon>
          <span>招募人数：{{ companion.currentCount }}/{{ companion.memberCount }}人</span>
        </div>
        <div class="info-item">
          <el-icon><Clock /></el-icon>
          <span>发布时间：{{ companion.createTime }}</span>
        </div>
      </div>

      <!-- 发起人校园信息 -->
      <div class="campus-info" v-if="companion.creatorSchool">
        <h3>🎓 发起人校园信息</h3>
        <div class="campus-detail">
          <el-descriptions :column="3" border size="small">
            <el-descriptions-item label="学校">{{ companion.creatorSchool }}</el-descriptions-item>
            <el-descriptions-item label="专业">{{ companion.creatorMajor || '未填写' }}</el-descriptions-item>
            <el-descriptions-item label="年级">{{ companion.creatorGrade || '未填写' }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </div>

      <!-- 限定学校 -->
      <div class="school-restrict" v-if="companion.school">
        <el-alert 
          :title="'此结伴仅限 ' + companion.school + ' 的同学参与'" 
          type="info" 
          :closable="false"
          show-icon
        />
      </div>

      <div class="content-section">
        <h3>详细说明</h3>
        <div class="content">{{ companion.content }}</div>
      </div>

      <div class="members-section">
        <h3>已加入成员</h3>
        <div class="member-list">
          <div v-for="member in members" :key="member.id" class="member-item">
            <div class="member-avatar">
              <el-avatar :size="40" :src="member.avatar" />
            </div>
            <div class="member-info">
              <div class="member-name">{{ member.nickname }}</div>
              <div class="member-status">
                <el-tag size="small" :type="member.status === 1 ? 'success' : 'warning'">
                  {{ member.status === 1 ? '已加入' : '待确认' }}
                </el-tag>
              </div>
            </div>
          </div>
          <el-empty v-if="!members.length" description="暂无成员" />
        </div>
      </div>

      <!-- 群聊入口 -->
      <div class="group-chat-section" v-if="companion.status === 1">
        <el-button type="success" size="large" @click="goGroupChat" icon="ChatDotRound">
          🎉 拼团成功！点击进入群聊
        </el-button>
      </div>

      <div class="actions">
        <el-button 
          v-if="isOwner && companion.status === 0" 
          type="primary" 
          @click="editCompanion"
        >
          编辑
        </el-button>
        <el-button 
          v-if="!isOwner && !isJoined && companion.status === 0" 
          type="primary" 
          @click="joinCompanion"
        >
          加入结伴
        </el-button>
        <el-button 
          v-if="!isOwner && isJoined && companion.status === 0" 
          type="warning" 
          @click="quitCompanion"
        >
          退出结伴
        </el-button>
        <el-button @click="goBack">返回</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Location, Calendar, User, Clock } from '@element-plus/icons-vue'
import { getCompanionDetail, joinCompanion as apiJoinCompanion, quitCompanion as apiQuitCompanion, getCompanionMembers } from '@/api/companion'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const companion = ref(null)
const members = ref([])

onMounted(() => {
  fetchDetail()
})

async function fetchDetail() {
  try {
    loading.value = true
    const id = route.params.id
    const [resDetail, resMembers] = await Promise.all([
      getCompanionDetail(id),
      getCompanionMembers(id)
    ])
    companion.value = resDetail
    members.value = resMembers
  } catch (error) {
    console.error('获取结伴游详情失败:', error)
    ElMessage.error('获取详情失败')
  } finally {
    loading.value = false
  }
}

const isOwner = computed(() => {
  return userStore.userInfo?.id === companion.value?.userId
})

const isJoined = computed(() => {
  return members.value.some(member => member.userId === userStore.userInfo?.id)
})

async function joinCompanion() {
  try {
    await apiJoinCompanion(route.params.id)
    ElMessage.success('加入成功！')
    fetchDetail()
  } catch (error) {
    ElMessage.error(error.message || '加入失败')
  }
}

async function quitCompanion() {
  try {
    await ElMessageBox.confirm('确定要退出这个结伴游吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await apiQuitCompanion(route.params.id)
    ElMessage.success('退出成功！')
    fetchDetail()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '退出失败')
    }
  }
}

function editCompanion() {
  router.push(`/companion/edit/${route.params.id}`)
}

function goGroupChat() {
  router.push(`/companion/${route.params.id}/group-chat`)
}

function goBack() {
  router.go(-1)
}
</script>

<style scoped>
.companion-detail {
  padding: 20px 0;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.title-area {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header h1 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.leader-tag {
  flex-shrink: 0;
}

.basic-info {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  font-size: 16px;
  color: #606266;
}

.info-item:last-child {
  margin-bottom: 0;
}

.campus-info {
  margin-bottom: 20px;
}

.campus-info h3 {
  margin-bottom: 12px;
  font-size: 18px;
  color: #303133;
}

.school-restrict {
  margin-bottom: 20px;
}

.content-section {
  margin-bottom: 30px;
}

.content-section h3 {
  margin-bottom: 15px;
  font-size: 18px;
  color: #303133;
}

.content {
  padding: 15px;
  background-color: #fafafa;
  border-radius: 8px;
  line-height: 1.6;
  white-space: pre-wrap;
}

.members-section {
  margin-bottom: 30px;
}

.members-section h3 {
  margin-bottom: 15px;
  font-size: 18px;
  color: #303133;
}

.member-list {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
}

.member-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  width: 200px;
}

.member-info {
  flex: 1;
}

.member-name {
  font-weight: bold;
  margin-bottom: 5px;
}

.group-chat-section {
  margin-bottom: 20px;
  padding: 20px;
  background: linear-gradient(135deg, #e8fce8, #d0f0d0);
  border-radius: 12px;
  text-align: center;
}

.actions {
  display: flex;
  gap: 15px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}
</style>