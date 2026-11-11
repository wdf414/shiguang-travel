<template>
  <div class="companion-list container">
    <div class="header">
      <h2>结伴同游</h2>
      <div class="actions">
        <el-button v-if="userStore.isLoggedIn" :type="isLeader ? 'success' : 'warning'" plain @click="handleLeaderClick">
          {{ isLeader ? '我的领队中心' : (leaderStatus === 0 ? '领队审核中' : '申请成为校园领队') }}
        </el-button>
        <el-button type="primary" @click="router.push('/companion/publish')">发起结伴</el-button>
      </div>
    </div>

    <div class="filters">
      <el-input 
        v-model="searchKeyword" 
        placeholder="搜索标题或内容" 
        style="width: 220px; margin-right: 12px;"
        @keyup.enter="fetchCompanions"
        clearable
      />
      <el-select 
        v-model="filterSchool" 
        placeholder="筛选/输入学校" 
        style="width: 180px; margin-right: 12px;" 
        clearable 
        filterable
        allow-create
        default-first-option
        @change="fetchCompanions"
      >
        <el-option v-for="s in schoolOptions" :key="s" :label="s" :value="s" />
      </el-select>
      <el-select v-model="filterMajor" placeholder="按专业筛选" style="width: 160px; margin-right: 12px;" clearable @change="fetchCompanions">
        <el-option v-for="m in majorOptions" :key="m" :label="m" :value="m" />
      </el-select>
      <el-select v-model="filterGrade" placeholder="按年级筛选" style="width: 140px; margin-right: 12px;" clearable @change="fetchCompanions">
        <el-option v-for="g in gradeOptions" :key="g" :label="g" :value="g" />
      </el-select>
      <el-button type="primary" @click="fetchCompanions">搜索</el-button>
      <el-button v-if="userStore.userInfo?.school" :type="sameSchoolOnly ? 'warning' : 'default'" @click="toggleSameSchool">
        {{ sameSchoolOnly ? '✓ 只看同校' : '只看同校' }}
      </el-button>
    </div>

    <div class="companion-grid" v-loading="loading">
      <div 
        v-for="item in companionList" 
        :key="item.id" 
        class="companion-card"
        @click="goDetail(item.id)"
      >
        <div class="card-header">
          <div class="title-row">
            <h3 class="title">{{ item.title }}</h3>
            <el-tag v-if="item.isLeader === 1" type="danger" size="small" class="leader-badge">
              🎓 校园领队
            </el-tag>
          </div>
          <el-tag 
            v-if="item.status === 0" 
            type="warning" 
            size="small"
          >
            招募中
          </el-tag>
          <el-tag 
            v-else-if="item.status === 1" 
            type="success" 
            size="small"
          >
            已成行
          </el-tag>
          <el-tag 
            v-else 
            type="info" 
            size="small"
          >
            已取消
          </el-tag>
        </div>
        
        <div class="info-row">
          <div class="info-item">
            <el-icon><Location /></el-icon>
            <span>{{ item.destination || '未指定' }}</span>
          </div>
          <div class="info-item">
            <el-icon><Calendar /></el-icon>
            <span>{{ item.departureDate || '未指定' }}</span>
          </div>
          <div class="info-item">
            <el-icon><User /></el-icon>
            <span>{{ item.currentCount }}/{{ item.memberCount }}人</span>
          </div>
        </div>

        <div class="school-info" v-if="item.creatorSchool">
          <el-icon><School /></el-icon>
          <span>{{ item.creatorSchool }}</span>
          <span v-if="item.creatorMajor"> · {{ item.creatorMajor }}</span>
          <span v-if="item.creatorGrade"> · {{ item.creatorGrade }}</span>
        </div>
        
        <div class="content">
          {{ item.content }}
        </div>
        
        <div class="footer">
          <span class="creator">发起人：{{ item.creatorName || '匿名' }}</span>
          <div class="buttons">
            <el-button v-if="item.status === 1" size="small" type="success" @click.stop="goGroupChat(item.id)">
              进入群聊
            </el-button>
            <el-button size="small" @click.stop="handleJoin(item.id)" :disabled="item.status !== 0">
              {{ isJoined(item.id) ? '已加入' : '加入' }}
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <div class="pagination">
      <el-pagination
        v-model:current-page="pagination.page"
        :total="pagination.total"
        :page-size="pagination.size"
        layout="prev, pager, next"
        @current-change="fetchCompanions"
      />
    </div>

    <!-- 领队申请/详情弹窗 -->
    <el-dialog v-model="showLeaderDialog" :title="isLeader ? '我的领队中心' : '申请成为校园领队'" width="500px">
      <div v-if="isLeader" class="leader-stats">
        <div class="stat-item">
          <div class="num">{{ leaderInfo?.commissionRate }}%</div>
          <div class="label">当前佣金比例</div>
        </div>
        <div class="stat-item">
          <div class="num">¥{{ leaderInfo?.totalCommission }}</div>
          <div class="label">累计赚取佣金</div>
        </div>
      </div>

      <el-form label-position="top">
        <el-form-item label="学校">
          <el-select 
            v-model="leaderForm.school" 
            placeholder="选择或输入学校" 
            style="width: 100%" 
            filterable
            allow-create
            default-first-option
            :disabled="isLeader" 
          >
            <el-option v-for="s in schoolOptions" :key="s" :label="s" :value="s" />
          </el-select>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="专业">
              <el-input v-model="leaderForm.major" placeholder="如：计算机科学" :disabled="isLeader" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="年级">
              <el-select v-model="leaderForm.grade" placeholder="选择年级" style="width: 100%" :disabled="isLeader">
                <el-option v-for="g in gradeOptions" :key="g" :label="g" :value="g" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="个人简介/优势 (展示给用户)">
          <el-input 
            v-model="leaderForm.description" 
            type="textarea" 
            :rows="4" 
            placeholder="介绍你的校园经历、带队经验或性格特点..." 
          />
        </el-form-item>
      </el-form>
      <div v-if="!isLeader" class="apply-notice">
        <p>💡 成为校园领队后，您可以：</p>
        <ul>
          <li>获得专属"校园领队"标识</li>
          <li>发布限定本校的结伴活动</li>
          <li>根据带队人数和效果获得平台佣金分成</li>
        </ul>
      </div>
      <template #footer>
        <el-button @click="showLeaderDialog = false">取消</el-button>
        <el-button type="primary" @click="submitLeaderApply" :loading="leaderSubmitting">
          {{ isLeader ? '更新简介' : '提交申请' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElIcon } from 'element-plus'
import { Location, Calendar, User, School } from '@element-plus/icons-vue'
import { getCompanionList, joinCompanion as apiJoinCompanion, getLeaderInfo, applyLeader, getLeaderList } from '@/api/companion'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const companionList = ref([])
const searchKeyword = ref('')
const filterSchool = ref('')
const filterMajor = ref('')
const filterGrade = ref('')
const sameSchoolOnly = ref(false)

// 领队相关
const isLeader = ref(false)
const leaderStatus = ref(-1) // -1:未申请, 0:待审核, 1:已通过, 2:已拒绝
const showLeaderDialog = ref(false)
const leaderForm = reactive({
  school: userStore.userInfo?.school || '',
  major: userStore.userInfo?.major || '',
  grade: userStore.userInfo?.grade || '',
  description: ''
})
const leaderSubmitting = ref(false)
const leaderInfo = ref(null)
const joinedCompanions = ref([])

// 预设选项
const schoolOptions = ref([
  '北京大学', '清华大学', '复旦大学', '浙江大学', '中山大学',
  '武汉大学', '南京大学', '四川大学', '华中科技大学', '厦门大学',
  '西安交通大学', '同济大学', '华南理工大学', '中南大学', '天津大学',
  '哈尔滨工业大学', '山东大学', '东南大学', '吉林大学', '北京航空航天大学'
])
const majorOptions = ref([
  '计算机科学', '软件工程', '电子信息', '机械工程', '经济学',
  '管理学', '文学', '法学', '医学', '艺术学', '教育学', '旅游管理'
])
const gradeOptions = ref([
  '2021级', '2022级', '2023级', '2024级', '2025级'
])

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

onMounted(async () => {
  fetchCompanions()
  checkLeaderStatus()
})

async function checkLeaderStatus() {
  if (userStore.userInfo?.isStudent !== 1) return
  try {
    const res = await getLeaderInfo()
    if (res) {
      leaderInfo.value = res
      leaderStatus.value = res.status
      if (res.status === 1) {
        isLeader.value = true
      }
      // 回显申请信息
      leaderForm.school = res.school || userStore.userInfo.school
      leaderForm.major = res.major || userStore.userInfo.major
      leaderForm.grade = res.grade || userStore.userInfo.grade
      leaderForm.description = res.description
    }
  } catch (e) {
    // 未申请
    leaderStatus.value = -1
  }
}

function handleLeaderClick() {
  if (!userStore.userInfo?.isStudent) {
    ElMessage.info('该功能仅面向在校大学生开放，请先进行学生认证')
    return
  }
  if (leaderStatus.value === 0) {
    ElMessage.info('您的领队申请正在审核中，请耐心等待')
    return
  }
  showLeaderDialog.value = true
}

async function submitLeaderApply() {
  if (!leaderForm.school || !leaderForm.major || !leaderForm.grade || !leaderForm.description) {
    ElMessage.warning('请填写完整信息')
    return
  }
  
  leaderSubmitting.value = true
  try {
    await applyLeader(leaderForm)
    ElMessage.success(isLeader.value ? '信息更新成功' : '申请已提交，请等待审核')
    showLeaderDialog.value = false
    checkLeaderStatus()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    leaderSubmitting.value = false
  }
}

function toggleSameSchool() {
  sameSchoolOnly.value = !sameSchoolOnly.value
  if (sameSchoolOnly.value && userStore.userInfo?.school) {
    filterSchool.value = userStore.userInfo.school
  } else {
    filterSchool.value = ''
  }
  fetchCompanions()
}

async function fetchCompanions() {
  try {
    loading.value = true
    const params = {
      page: pagination.page,
      size: pagination.size,
      keyword: searchKeyword.value || undefined,
      school: filterSchool.value || undefined,
      major: filterMajor.value || undefined,
      grade: filterGrade.value || undefined
    }
    const res = await getCompanionList(params)
    companionList.value = res.records
    pagination.total = res.total
  } catch (error) {
    console.error('获取结伴游列表失败:', error)
    ElMessage.error('获取列表失败')
  } finally {
    loading.value = false
  }
}

function isJoined(companionId) {
  return joinedCompanions.value.includes(companionId)
}

async function handleJoin(id) {
  try {
    await apiJoinCompanion(id)
    ElMessage.success('加入成功！')
    fetchCompanions()
  } catch (error) {
    ElMessage.error(error.message || '加入失败')
  }
}

function goDetail(id) {
  router.push(`/companion/${id}`)
}

function goGroupChat(id) {
  router.push(`/companion/${id}/group-chat`)
}
</script>

<style scoped>
.companion-list {
  padding: 20px 0;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.filters {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.companion-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 20px;
}

.companion-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.companion-card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  transform: translateY(-2px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.title {
  margin: 0;
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.leader-badge {
  flex-shrink: 0;
}

.info-row {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 8px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #606266;
}

.school-info {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #409eff;
  margin-bottom: 10px;
  padding: 6px 10px;
  background: #f0f7ff;
  border-radius: 4px;
}

.content {
  font-size: 14px;
  color: #909399;
  line-height: 1.5;
  margin-bottom: 16px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid #f0f0f0;
  padding-top: 12px;
}

.creator {
  font-size: 12px;
  color: #909399;
}

.buttons {
  display: flex;
  gap: 8px;
}

.leader-stats {
  display: flex;
  justify-content: space-around;
  margin-bottom: 24px;
  background: #fdf6ec;
  padding: 20px;
  border-radius: 8px;
}

.stat-item {
  text-align: center;
}

.stat-item .num {
  font-size: 24px;
  font-weight: bold;
  color: #e65100;
  margin-bottom: 4px;
}

.stat-item .label {
  font-size: 13px;
  color: #909399;
}

.apply-notice {
  margin-top: 20px;
  background: #f0f9eb;
  padding: 12px;
  border-radius: 4px;
  font-size: 13px;
  color: #67c23a;
}

.apply-notice p {
  margin: 0 0 8px 0;
  font-weight: bold;
}

.apply-notice ul {
  margin: 0;
  padding-left: 20px;
}
</style>