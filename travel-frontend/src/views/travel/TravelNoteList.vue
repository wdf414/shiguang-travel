<template>
  <div class="travel-note-list container">
    <!-- 头部Banner -->
    <div class="note-banner">
      <div class="content">
        <h1>游记攻略</h1>
        <p>记录旅行故事，分享旅途精彩</p>
        <el-button type="primary" size="large" icon="Edit" @click="goCreate">写游记</el-button>
      </div>
    </div>

    <!-- 筛选和排序 -->
    <div class="filter-bar">
      <el-radio-group v-model="activeTab" size="large">
        <el-radio-button value="hot">热门游记</el-radio-button>
        <el-radio-button value="new">最新发布</el-radio-button>
      </el-radio-group>
      
      <el-input
        v-model="searchKeyword"
        placeholder="搜索目的地/游记"
        prefix-icon="Search"
        clearable
        @keyup.enter="handleSearch"
        style="width: 300px"
      />
    </div>

    <!-- 瀑布流/网格列表 -->
    <div class="note-grid" v-loading="loading">
      <div 
        v-for="note in noteList" 
        :key="note.id" 
        class="note-card"
        @click="goDetail(note.id)"
      >
        <div class="cover-wrapper">
          <img :src="resolveImage(note.coverImage, 'note-' + note.id)" :alt="note.title" class="cover-img" @error="onImgError">
          <div class="location" v-if="note.destination">
            <el-icon><Location /></el-icon> {{ note.destination }}
          </div>
        </div>
        <div class="card-content">
          <h3 class="title">{{ note.title }}</h3>
          <div class="meta">
            <div class="author" v-if="note.author">
              <el-avatar :size="24" :src="note.author.avatar">{{ note.author.nickname?.charAt(0) }}</el-avatar>
              <span class="name">{{ note.author.nickname }}</span>
            </div>
            <div class="stats">
              <span><el-icon><View /></el-icon> {{ note.views }}</span>
              <span><el-icon><Star /></el-icon> {{ note.likes }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <div class="pagination">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        layout="prev, pager, next"
        @current-change="fetchNotes"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Edit, Search, Location, View, Star } from '@element-plus/icons-vue'
import { getNoteList } from '@/api/travel-note'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { resolveImage, onImageError as onImgError } from '@/utils/image'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const activeTab = ref('hot')
const searchKeyword = ref('')
const noteList = ref([])

const pagination = reactive({
  page: 1,
  size: 12,
  total: 0
})

async function fetchNotes() {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      keyword: searchKeyword.value || undefined,
      sort: activeTab.value,
      status: 1 // 只显示已通过审核的游记
    }
    const res = await getNoteList(params)
    noteList.value = res.records
    pagination.total = res.total
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  fetchNotes()
}

function goDetail(id) {
  router.push(`/travel-notes/${id}`)
}

function goCreate() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push({ path: '/login', query: { redirect: '/travel-notes/create' } })
    return
  }
  router.push('/travel-notes/create')
}

// 模拟数据用于初始展示 (若后端暂无数据)
function loadMockData() {
  // 仅当后端返回为空时调用，或开发阶段使用
}

onMounted(() => {
  fetchNotes()
})

</script>

<style lang="scss" scoped>
.travel-note-list {
  padding-top: 20px;
  padding-bottom: 60px;
}

.note-banner {
  height: 200px;
  background: linear-gradient(135deg, #0086f6 0%, #00cec9 100%);
  border-radius: 8px;
  margin-bottom: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  color: #fff;
  
  h1 {
    font-size: 32px;
    margin-bottom: 10px;
    font-weight: 700;
  }
  
  p {
    font-size: 16px;
    margin-bottom: 20px;
    opacity: 0.9;
  }
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.note-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  min-height: 400px;
}

.note-card {
  background: #fff;
  border-radius: 4px;
  overflow: hidden;
  border: 1px solid #e5e5e5;
  cursor: pointer;
  transition: all 0.3s;
  
  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 10px 20px rgba(0,0,0,0.1);
    
    .cover-img {
      transform: scale(1.05);
    }
  }
  
  .cover-wrapper {
    height: 180px;
    overflow: hidden;
    position: relative;
    
    .cover-img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.5s;
    }
    
    .location {
      position: absolute;
      bottom: 10px;
      left: 10px;
      background: rgba(0,0,0,0.6);
      color: #fff;
      padding: 4px 8px;
      border-radius: 4px;
      font-size: 12px;
      display: flex;
      align-items: center;
      gap: 4px;
    }
  }
  
  .card-content {
    padding: 15px;
    
    .title {
      font-size: 16px;
      font-weight: 600;
      margin-bottom: 15px;
      line-height: 1.4;
      height: 44px; // 限制两行高度
      overflow: hidden;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      color: #333;
    }
    
    .meta {
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-size: 12px;
      color: #999;
      
      .author {
        display: flex;
        align-items: center;
        gap: 6px;
        
        .name {
          max-width: 80px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }
      
      .stats {
        display: flex;
        gap: 10px;
        
        span {
          display: flex;
          align-items: center;
          gap: 2px;
        }
      }
    }
  }
}

.pagination {
  margin-top: 40px;
  display: flex;
  justify-content: center;
  
  :deep(.el-pagination) {
    .el-pager li.is-active {
      background-color: #0086f6 !important; 
      color: #fff;
    }
  }
}

@media (max-width: 1200px) {
  .note-grid { grid-template-columns: repeat(3, 1fr); }
}
@media (max-width: 768px) {
  .note-grid { grid-template-columns: 1fr; }
  .filter-bar {
    flex-direction: column;
    gap: 15px;
    align-items: stretch;
    
    .el-input {
      width: 100% !important;
    }
  }
}
</style>
