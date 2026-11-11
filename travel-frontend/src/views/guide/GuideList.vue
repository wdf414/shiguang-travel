<template>
  <div class="guide-list container">
    <div class="header">
      <h2>校园导游</h2>
      <div class="actions">
        <el-button type="primary" @click="router.push('/guide/publish')">发布攻略</el-button>
      </div>
    </div>

    <div class="filters">
      <el-input 
        v-model="searchKeyword" 
        placeholder="搜索标题或内容" 
        style="width: 300px; margin-right: 20px;"
        @keyup.enter="fetchGuides"
      />
      <el-button type="primary" @click="fetchGuides">搜索</el-button>
    </div>

    <div class="guide-grid" v-loading="loading">
      <div 
        v-for="item in guideList" 
        :key="item.id" 
        class="guide-card"
        @click="goDetail(item.id)"
      >
        <div class="card-header">
          <h3 class="title">{{ item.title }}</h3>
          <el-tag 
            v-if="item.status === 0" 
            type="info" 
            size="small"
          >
            草稿
          </el-tag>
          <el-tag 
            v-else 
            type="success" 
            size="small"
          >
            已发布
          </el-tag>
        </div>
        
        <div class="info-row">
          <div class="info-item">
            <el-icon><User /></el-icon>
            <span>{{ item.authorName || '匿名' }}</span>
          </div>
          <div class="info-item">
            <el-icon><View /></el-icon>
            <span>{{ item.viewCount || 0 }}次浏览</span>
          </div>
          <div class="info-item">
            <el-icon><Star /></el-icon>
            <span>{{ item.likeCount || 0 }}个赞</span>
          </div>
        </div>
        
        <div class="content">
          {{ item.content }}
        </div>
        
        <div class="footer">
          <span class="date">{{ item.createTime }}</span>
          <el-button size="small" @click.stop="likeGuide(item.id)" :disabled="item.status !== 1">
            点赞
          </el-button>
        </div>
      </div>
    </div>

    <div class="pagination">
      <el-pagination
        v-model:current-page="pagination.page"
        :total="pagination.total"
        :page-size="pagination.size"
        layout="prev, pager, next"
        @current-change="fetchGuides"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElIcon } from 'element-plus'
import { User, View, Star } from '@element-plus/icons-vue'
import { getGuideList as apiGetGuideList, likeGuide as apiLikeGuide } from '@/api/guide'

const router = useRouter()

const loading = ref(false)
const guideList = ref([])
const searchKeyword = ref('')

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

onMounted(() => {
  fetchGuides()
})

async function fetchGuides() {
  try {
    loading.value = true
    const params = {
      page: pagination.page,
      size: pagination.size,
      keyword: searchKeyword.value
    }
    const res = await apiGetGuideList(params)
    guideList.value = res.records
    pagination.total = res.total
  } catch (error) {
    console.error('获取导游攻略列表失败:', error)
    ElMessage.error('获取列表失败')
  } finally {
    loading.value = false
  }
}

async function likeGuide(id) {
  try {
    await apiLikeGuide(id)
    ElMessage.success('点赞成功！')
    // 更新本地列表
    const guide = guideList.value.find(g => g.id === id)
    if (guide) {
      guide.likeCount = (guide.likeCount || 0) + 1
    }
  } catch (error) {
    ElMessage.error(error.message || '点赞失败')
  }
}

function goDetail(id) {
  router.push(`/guide/${id}`)
}
</script>

<style scoped>
.guide-list {
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
}

.guide-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 20px;
}

.guide-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.guide-card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  transform: translateY(-2px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.title {
  margin: 0;
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.info-row {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 12px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #606266;
}

.content {
  font-size: 14px;
  color: #909399;
  line-height: 1.5;
  margin-bottom: 16px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
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

.date {
  font-size: 12px;
  color: #909399;
}

.pagination {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}
</style>