<template>
  <div class="content-review">
    <el-card class="review-stats">
      <div class="stats-grid">
        <div class="stat-item">
          <div class="stat-value">{{ stats.pendingNotes }}</div>
          <div class="stat-label">待审核游记</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ stats.pendingCompanions }}</div>
          <div class="stat-label">待审核结伴</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ stats.pendingComments }}</div>
          <div class="stat-label">待审核评论</div>
        </div>
        <div class="stat-item highlight">
          <div class="stat-value">{{ stats.totalPending }}</div>
          <div class="stat-label">总计待审核</div>
        </div>
      </div>
    </el-card>

    <el-card class="review-list">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-radio-group v-model="activeType" @change="handleTypeChange">
              <el-radio-button label="travel_note">游记攻略</el-radio-button>
              <el-radio-button label="companion">结伴信息</el-radio-button>
              <el-radio-button label="comment">评论</el-radio-button>
            </el-radio-group>
          </div>
          <div class="header-right">
            <el-button type="primary" @click="showSensitiveWordDialog">
              <el-icon><Setting /></el-icon>
              敏感词管理
            </el-button>
          </div>
        </div>
      </template>

      <el-table
        :data="contentList"
        v-loading="loading"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        
        <el-table-column label="内容标题/内容" min-width="300">
          <template #default="{ row }">
            <div class="content-preview">
              <div class="content-title">{{ row.title || row.content?.substring(0, 50) + '...' }}</div>
              <div class="content-meta">
                <span>作者ID: {{ row.userId }}</span>
                <span>发布时间: {{ formatTime(row.createTime) }}</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="目的地" width="120" v-if="activeType !== 'comment'">
          <template #default="{ row }">
            {{ row.destination || '-' }}
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag type="warning" v-if="row.status === 0">待审核</el-tag>
            <el-tag type="success" v-else-if="row.status === 1">已通过</el-tag>
            <el-tag type="danger" v-else>已拒绝</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row)">查看</el-button>
            <el-button type="warning" size="small" @click="handleAiReview(row)">
              <el-icon><MagicStick /></el-icon>
              AI 复核
            </el-button>
            <el-button type="success" size="small" @click="handleApprove(row)">通过</el-button>
            <el-button type="danger" size="small" @click="handleReject(row)">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="batch-actions" v-if="selectedRows.length > 0">
        <el-alert type="info" :closable="false">
          已选择 {{ selectedRows.length }} 项
          <el-button type="success" size="small" @click="handleBatchApprove">批量通过</el-button>
          <el-button type="danger" size="small" @click="handleBatchReject">批量拒绝</el-button>
        </el-alert>
      </div>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </el-card>

    <!-- 审核详情对话框 -->
    <el-dialog
      v-model="detailDialog.visible"
      title="内容详情"
      width="800px"
    >
      <div class="content-detail" v-if="detailDialog.data">
        <h3>{{ detailDialog.data.title }}</h3>
        <div class="detail-meta">
          <span>作者: {{ detailDialog.data.userId }}</span>
          <span>发布时间: {{ formatTime(detailDialog.data.createTime) }}</span>
        </div>
        <div class="detail-content">{{ detailDialog.data.content }}</div>

        <el-divider />

        <div class="sensitive-check">
          <h4>敏感词检测</h4>
          <el-button type="primary" @click="checkSensitiveWords" :loading="checking">
            检测敏感词
          </el-button>
          <div class="sensitive-result" v-if="sensitiveWords.length > 0">
            <el-tag type="danger" v-for="word in sensitiveWords" :key="word" class="sensitive-word">
              {{ word }}
            </el-tag>
          </div>
          <el-alert v-else-if="checked" type="success" :closable="false" style="margin-top: 10px;">
            未检测到敏感词
          </el-alert>
        </div>

        <el-divider />

        <div class="ai-check">
          <div class="ai-check-header">
            <h4>
              AI 智能初审
              <el-tag size="small" type="success" effect="plain">DeepSeek</el-tag>
            </h4>
            <el-button type="primary" plain @click="runAiReview" :loading="aiChecking">
              <el-icon><MagicStick /></el-icon>
              AI 复核
            </el-button>
          </div>
          <p class="ai-hint">结合敏感词与语义判断广告引流、隐私泄露、人身攻击等风险，结果仅供人工决策参考。</p>

          <div v-if="aiResult" class="ai-result">
            <div class="ai-result-head">
              <el-tag :type="riskTagType(aiResult.riskLevel)" effect="dark">
                风险 {{ riskText(aiResult.riskLevel) }} · {{ aiResult.score }} 分
              </el-tag>
              <el-tag :type="suggestionTagType(aiResult.suggestion)" effect="plain">
                建议 {{ suggestionText(aiResult.suggestion) }}
              </el-tag>
              <el-tag v-if="aiResult.meta?.cached" type="info" effect="plain">缓存</el-tag>
            </div>
            <div class="ai-result-body">
              <p><strong>风险分类：</strong>{{ (aiResult.categories || []).join('、') || '无' }}</p>
              <ul v-if="aiResult.reasons?.length">
                <li v-for="(reason, index) in aiResult.reasons" :key="index">{{ reason }}</li>
              </ul>
              <div v-if="aiResult.sensitiveWords?.length" class="ai-words">
                <span>命中敏感词：</span>
                <el-tag v-for="word in aiResult.sensitiveWords" :key="word" type="danger" size="small">
                  {{ word }}
                </el-tag>
              </div>
            </div>
            <el-alert
              v-if="aiResult.meta?.fallback"
              type="warning"
              :closable="false"
              show-icon
              title="AI 服务不可用，当前为本地敏感词规则兜底结果"
            />
            <div class="ai-result-actions">
              <el-button type="success" size="small" @click="applyAiSuggestion('approve')">按建议通过</el-button>
              <el-button type="danger" size="small" @click="applyAiSuggestion('reject')">按建议拒绝</el-button>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="detailDialog.visible = false">关闭</el-button>
        <el-button type="success" @click="handleApprove(detailDialog.data)">通过</el-button>
        <el-button type="danger" @click="showRejectDialog(detailDialog.data)">拒绝</el-button>
      </template>
    </el-dialog>

    <!-- 拒绝原因对话框 -->
    <el-dialog
      v-model="rejectDialog.visible"
      title="填写拒绝原因"
      width="500px"
    >
      <el-form>
        <el-form-item label="拒绝原因">
          <el-input
            v-model="rejectDialog.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入拒绝原因，将通知给用户"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialog.visible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject">确认拒绝</el-button>
      </template>
    </el-dialog>

    <!-- 敏感词管理对话框 -->
    <el-dialog
      v-model="sensitiveDialog.visible"
      title="敏感词管理"
      width="700px"
    >
      <div class="sensitive-header">
        <el-input
          v-model="sensitiveDialog.newWord"
          placeholder="输入敏感词"
          style="width: 200px; margin-right: 10px;"
        />
        <el-select v-model="sensitiveDialog.wordType" style="width: 120px; margin-right: 10px;">
          <el-option label="违禁词" :value="1" />
          <el-option label="敏感词" :value="2" />
          <el-option label="广告词" :value="3" />
        </el-select>
        <el-button type="primary" @click="addSensitiveWord">添加</el-button>
      </div>
      
      <el-table :data="sensitiveWordsList" style="margin-top: 20px;">
        <el-table-column prop="word" label="敏感词" />
        <el-table-column prop="wordType" label="类型">
          <template #default="{ row }">
            <el-tag type="danger" v-if="row.wordType === 1">违禁词</el-tag>
            <el-tag type="warning" v-else-if="row.wordType === 2">敏感词</el-tag>
            <el-tag v-else>广告词</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="分类" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="danger" size="small" @click="deleteSensitiveWord(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Setting } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { aiModerate } from '@/api/ai'

const activeType = ref('travel_note')
const loading = ref(false)
const contentList = ref([])
const selectedRows = ref([])
const checking = ref(false)
const checked = ref(false)
const sensitiveWords = ref([])
const sensitiveWordsList = ref([])
const aiChecking = ref(false)
const aiResult = ref(null)

const stats = reactive({
  pendingNotes: 0,
  pendingCompanions: 0,
  pendingComments: 0,
  totalPending: 0
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const detailDialog = reactive({
  visible: false,
  data: null
})

const rejectDialog = reactive({
  visible: false,
  data: null,
  reason: ''
})

const sensitiveDialog = reactive({
  visible: false,
  newWord: '',
  wordType: 1
})

// 获取统计数据
async function fetchStatistics() {
  try {
    const data = await request.get('/admin/review/statistics')
    if (data) {
      Object.assign(stats, data)
    }
  } catch (e) {
    console.error('获取统计失败', e)
  }
}

// 获取内容列表
async function fetchContentList() {
  loading.value = true
  try {
    const data = await request.get('/admin/review/pending', {
      params: {
        type: activeType.value,
        page: pagination.page,
        size: pagination.size
      }
    })
    contentList.value = data?.records || []
    pagination.total = data?.total || 0
  } catch (e) {
    ElMessage.error('获取列表失败')
  } finally {
    loading.value = false
  }
}

// 处理类型切换
function handleTypeChange() {
  pagination.page = 1
  fetchContentList()
}

// 查看详情
function handleView(row) {
  detailDialog.data = row
  detailDialog.visible = true
  checked.value = false
  sensitiveWords.value = []
  aiResult.value = null
}

// 列表页直接触发 AI 复核：打开详情弹窗并立即执行
async function handleAiReview(row) {
  handleView(row)
  await nextTick()
  await runAiReview()
}

// AI 语义初审
async function runAiReview() {
  if (!detailDialog.data) return
  aiChecking.value = true
  aiResult.value = null
  try {
    const data = await aiModerate({
      contentType: activeType.value,
      title: detailDialog.data.title || '',
      content: detailDialog.data.content || ''
    })
    aiResult.value = data
    ElMessage.success(data?.meta?.fallback ? 'AI 不可用，已返回本地兜底结果' : 'AI 初审完成')
  } catch (e) {
    ElMessage.error('AI 初审失败，请稍后重试')
  } finally {
    aiChecking.value = false
  }
}

function riskText(level) {
  return { LOW: '低', MEDIUM: '中', HIGH: '高' }[level] || level
}

function riskTagType(level) {
  return { LOW: 'success', MEDIUM: 'warning', HIGH: 'danger' }[level] || 'info'
}

function suggestionText(suggestion) {
  return { APPROVE: '通过', REVIEW: '人工复核', REJECT: '拒绝' }[suggestion] || suggestion
}

function suggestionTagType(suggestion) {
  return { APPROVE: 'success', REVIEW: 'warning', REJECT: 'danger' }[suggestion] || 'info'
}

function applyAiSuggestion(action) {
  if (!detailDialog.data) return
  if (action === 'approve') {
    handleApprove(detailDialog.data)
    return
  }
  handleReject(detailDialog.data)
  const reasons = aiResult.value?.reasons || []
  rejectDialog.reason = reasons.length ? reasons.join('；') : 'AI 初审建议拒绝，请补充具体原因'
}

// 检测敏感词
async function checkSensitiveWords() {
  if (!detailDialog.data) return
  
  checking.value = true
  try {
    const content = detailDialog.data.title + ' ' + detailDialog.data.content
    const data = await request.post('/admin/review/check-sensitive', { content })
    sensitiveWords.value = data?.words || []
    checked.value = true
    if (data?.hasSensitive) {
      ElMessage.warning(`检测到 ${sensitiveWords.value.length} 个敏感词`)
    } else {
      ElMessage.success('未检测到敏感词')
    }
  } catch (e) {
    ElMessage.error('检测失败')
  } finally {
    checking.value = false
  }
}

// 通过
async function handleApprove(row) {
  try {
    await request.post('/admin/review/audit', {
      contentType: activeType.value,
      contentId: row.id,
      newStatus: 1
    })
    ElMessage.success('已通过')
    fetchContentList()
    fetchStatistics()
    detailDialog.visible = false
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

// 显示拒绝对话框
function handleReject(row) {
  rejectDialog.data = row
  rejectDialog.reason = ''
  rejectDialog.visible = true
}

function showRejectDialog(row) {
  handleReject(row)
}

// 确认拒绝
async function confirmReject() {
  if (!rejectDialog.reason.trim()) {
    ElMessage.warning('请填写拒绝原因')
    return
  }
  
  try {
    await request.post('/admin/review/audit', {
      contentType: activeType.value,
      contentId: rejectDialog.data.id,
      newStatus: 2,
      rejectReason: rejectDialog.reason
    })
    ElMessage.success('已拒绝')
    rejectDialog.visible = false
    fetchContentList()
    fetchStatistics()
    detailDialog.visible = false
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

// 批量通过
async function handleBatchApprove() {
  try {
    await request.post('/admin/review/batch-audit', {
      contentType: activeType.value,
      contentIds: selectedRows.value.map(row => row.id),
      newStatus: 1
    })
    ElMessage.success('批量通过成功')
    fetchContentList()
    fetchStatistics()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

// 批量拒绝
async function handleBatchReject() {
  try {
    const { value } = await ElMessageBox.prompt('请输入批量拒绝原因', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputType: 'textarea'
    })
    
    await request.post('/admin/review/batch-audit', {
      contentType: activeType.value,
      contentIds: selectedRows.value.map(row => row.id),
      newStatus: 2,
      rejectReason: value
    })
    ElMessage.success('批量拒绝成功')
    fetchContentList()
    fetchStatistics()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

// 选择变化
function handleSelectionChange(selection) {
  selectedRows.value = selection
}

// 分页
function handleSizeChange(val) {
  pagination.size = val
  fetchContentList()
}

function handlePageChange(val) {
  pagination.page = val
  fetchContentList()
}

// 敏感词管理
async function showSensitiveWordDialog() {
  sensitiveDialog.visible = true
  await fetchSensitiveWords()
}

async function fetchSensitiveWords() {
  try {
    const data = await request.get('/admin/review/sensitive-words')
    sensitiveWordsList.value = data || []
  } catch (e) {
    console.error('获取敏感词失败', e)
  }
}

async function addSensitiveWord() {
  if (!sensitiveDialog.newWord.trim()) {
    ElMessage.warning('请输入敏感词')
    return
  }
  
  try {
    await request.post('/admin/review/sensitive-word', {
      word: sensitiveDialog.newWord,
      wordType: sensitiveDialog.wordType
    })
    ElMessage.success('添加成功')
    sensitiveDialog.newWord = ''
    fetchSensitiveWords()
  } catch (e) {
    ElMessage.error('添加失败')
  }
}

async function deleteSensitiveWord(row) {
  try {
    await ElMessageBox.confirm('确定删除该敏感词？', '提示', {
      type: 'warning'
    })
    await request.delete(`/admin/review/sensitive-word/${row.id}`)
    ElMessage.success('删除成功')
    fetchSensitiveWords()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 格式化时间
function formatTime(time) {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 16)
}

onMounted(() => {
  fetchStatistics()
  fetchContentList()
})
</script>

<style lang="scss" scoped>
.content-review {
  padding: 20px;
}

.review-stats {
  margin-bottom: 20px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.stat-item {
  text-align: center;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
  
  &.highlight {
    background: #fff3e0;
    .stat-value {
      color: #ff6b6b;
    }
  }
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.content-preview {
  .content-title {
    font-weight: 500;
    margin-bottom: 8px;
    line-height: 1.4;
  }
  
  .content-meta {
    font-size: 12px;
    color: #999;
    
    span {
      margin-right: 15px;
    }
  }
}

.batch-actions {
  margin: 20px 0;
}

.content-detail {
  h3 {
    margin-bottom: 15px;
  }
  
  .detail-meta {
    color: #999;
    font-size: 14px;
    margin-bottom: 20px;
    
    span {
      margin-right: 20px;
    }
  }
  
  .detail-content {
    line-height: 1.8;
    color: #333;
    white-space: pre-wrap;
    word-break: break-word;
    max-height: 420px;
    overflow-y: auto;
  }
}

.sensitive-check {
  margin-top: 20px;
  
  h4 {
    margin-bottom: 15px;
  }
}

.sensitive-result {
  margin-top: 15px;
  
  .sensitive-word {
    margin-right: 10px;
    margin-bottom: 10px;
  }
}

.sensitive-header {
  display: flex;
  align-items: center;
}

.ai-check {
  h4 {
    display: flex;
    align-items: center;
    gap: 8px;
    margin: 0;
  }
}

.ai-check-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.ai-hint {
  margin: 8px 0 12px;
  color: #909399;
  font-size: 12px;
  line-height: 1.6;
}

.ai-result {
  margin-top: 12px;
  padding: 14px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background: #fafbfc;
}

.ai-result-head {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 10px;
}

.ai-result-body {
  color: #303133;
  font-size: 13px;
  line-height: 1.7;

  p { margin: 0 0 6px; }
  ul {
    margin: 0;
    padding-left: 18px;
    color: #606266;
  }
}

.ai-words {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
  margin-top: 8px;
  color: #606266;
}

.ai-result-actions {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}
</style>
