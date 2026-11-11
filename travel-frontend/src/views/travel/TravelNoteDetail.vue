<template>
  <div class="travel-note-detail container" v-loading="loading">
    <template v-if="note">
      <!-- 头部封面区域 -->
      <div class="note-header">
        <div class="cover-image" :style="{ backgroundImage: `url(${note.coverImage})` }">
          <div class="title-wrapper">
            <h1 class="title">{{ note.title }}</h1>
            <div class="meta-info">
              <span class="location" v-if="note.destination">
                <el-icon><Location /></el-icon> {{ note.destination }}
              </span>
              <span class="date">{{ note.createTime }}</span>
              <span class="views"><el-icon><View /></el-icon> {{ note.views }}浏览</span>
            </div>
          </div>
        </div>
      </div>

      <div class="main-content">
        <!-- 左侧正文 -->
        <article class="note-body">
          <div class="author-bar">
            <el-avatar :size="50" :src="note.author?.avatar">{{ note.author?.nickname?.charAt(0) }}</el-avatar>
            <div class="author-info">
              <span class="nickname">{{ note.author?.nickname }}</span>
              <el-tag size="small" effect="plain">Lv.5 旅行家</el-tag>
            </div>
            <div class="actions">
              <el-button type="primary" plain round icon="Plus">关注</el-button>
            </div>
          </div>

          <!-- 用户正文按纯文本展示，避免 UGC 中的 HTML 造成存储型 XSS -->
          <div class="rich-content">{{ note.content }}</div>

          <!-- 底部互动 -->
          <div class="interaction-bar">
            <el-button type="warning" circle size="large" @click="handleLike" :plain="!isLiked">
              <el-icon><Pointer /></el-icon>
            </el-button>
            <span class="count">点赞 {{ note.likes }}</span>
            
            <el-button type="danger" circle size="large" :plain="!isFavorited">
              <el-icon><Star /></el-icon>
            </el-button>
            <span class="count">收藏 {{ note.favorites }}</span>
          </div>
        </article>

        <!-- 右侧相关推荐 -->
        <aside class="sidebar">
          <div class="sidebar-card">
            <h3>相关目的地</h3>
            <div class="dest-tags">
              <el-tag v-if="note.destination">{{ note.destination }}</el-tag>
              <el-tag type="info">自由行</el-tag>
              <el-tag type="success">深度游</el-tag>
            </div>
          </div>
        </aside>
      </div>

      <!-- 评论区 -->
      <div class="comment-section">
        <h3>全部评论 ({{ comments.length }})</h3>
        
        <div class="comment-input">
          <el-avatar :size="40" :src="userStore.userInfo?.avatar">{{ userStore.userInfo?.nickname?.charAt(0) }}</el-avatar>
          <div class="input-wrapper">
            <el-input 
              v-model="commentContent" 
              type="textarea" 
              :rows="3" 
              placeholder="说点什么吧..." 
            />
            <div class="actions">
              <el-button type="primary" @click="submitComment" :loading="submitting">发表评论</el-button>
            </div>
          </div>
        </div>

        <div class="comment-list">
          <div v-for="comment in comments" :key="comment.id" class="comment-item">
            <el-avatar :size="40" :src="comment.user?.avatar">{{ comment.user?.nickname?.charAt(0) }}</el-avatar>
            <div class="comment-content">
              <div class="user-info">
                <span class="name">{{ comment.user?.nickname }}</span>
                <span class="time">{{ comment.createTime }}</span>
              </div>
              <p class="text">{{ comment.content }}</p>
              <div class="comment-actions">
                <span class="reply-btn"><el-icon><ChatDotRound /></el-icon> 回复</span>
                <span class="like-btn"><el-icon><Pointer /></el-icon> {{ comment.likes }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Location, View, Star, Pointer, ChatDotRound, Plus } from '@element-plus/icons-vue'
import { getNoteDetail, getNoteComments, addComment } from '@/api/travel-note'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const note = ref(null)
const comments = ref([])
const isLiked = ref(false)
const isFavorited = ref(false) // 模拟状态
const commentContent = ref('')
const submitting = ref(false)

async function fetchDat() {
  loading.value = true
  try {
    const id = route.params.id
    note.value = await getNoteDetail(id)
    comments.value = await getNoteComments(id)
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function handleLike() {
  if (!userStore.isLoggedIn) return ElMessage.warning('请先登录')
  isLiked.value = !isLiked.value
  if (isLiked.value) note.value.likes++
  else note.value.likes--
}

async function submitComment() {
  if (!userStore.isLoggedIn) return ElMessage.warning('请先登录')
  if (!commentContent.value.trim()) return ElMessage.warning('请输入内容')
  
  submitting.value = true
  try {
    await addComment({
      noteId: note.value.id,
      content: commentContent.value
    })
    ElMessage.success('评论成功')
    commentContent.value = ''
    comments.value = await getNoteComments(note.value.id) // 刷新评论
  } catch (e) {
    ElMessage.error('评论失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchDat()
})
</script>

<style lang="scss" scoped>
.travel-note-detail {
  padding-bottom: 60px;
}

.note-header {
  height: 400px;
  position: relative;
  margin-bottom: 40px;
  
  .cover-image {
    width: 100%;
    height: 100%;
    background-size: cover;
    background-position: center;
    position: relative;
    border-radius: 0 0 20px 20px;
    
    &::before {
      content: '';
      position: absolute;
      inset: 0;
      background: linear-gradient(to top, rgba(0,0,0,0.7), transparent);
      border-radius: 0 0 20px 20px;
    }
  }
  
  .title-wrapper {
    position: absolute;
    bottom: 40px;
    left: 0;
    right: 0;
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
    color: #fff;
    
    .title {
      font-size: 36px;
      font-weight: 700;
      margin-bottom: 15px;
      text-shadow: 0 2px 4px rgba(0,0,0,0.3);
    }
    
    .meta-info {
      display: flex;
      gap: 20px;
      font-size: 14px;
      opacity: 0.9;
      
      span {
        display: flex;
        align-items: center;
        gap: 5px;
      }
    }
  }
}

.main-content {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 40px;
  margin-bottom: 40px;
}

.note-body {
  background: #fff;
  padding: 40px;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  
  .author-bar {
    display: flex;
    align-items: center;
    gap: 15px;
    margin-bottom: 40px;
    padding-bottom: 20px;
    border-bottom: 1px solid #eee;
    
    .author-info {
      flex: 1;
      .nickname {
        display: block;
        font-weight: 600;
        font-size: 16px;
        color: #333;
        margin-bottom: 4px;
      }
    }
  }
  
  .rich-content {
    font-size: 16px;
    line-height: 1.8;
    color: #333;
    min-height: 300px;
    white-space: pre-wrap;
    word-break: break-word;
  }
  
  .interaction-bar {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 20px;
    margin-top: 60px;
    
    .count {
      color: #666;
      font-size: 14px;
      margin-right: 20px;
    }
  }
}

.sidebar-card {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #eee;
  
  h3 {
    font-size: 16px;
    margin-bottom: 15px;
    border-left: 4px solid #0086f6;
    padding-left: 10px;
  }
  
  .dest-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
  }
}

.comment-section {
  background: #fff;
  padding: 40px;
  border-radius: 8px;
  max-width: 860px; // 与文章宽度对齐
  
  h3 {
    font-size: 20px;
    margin-bottom: 30px;
  }
  
  .comment-input {
    display: flex;
    gap: 20px;
    margin-bottom: 40px;
    
    .input-wrapper {
      flex: 1;
      
      .actions {
        display: flex;
        justify-content: flex-end;
        margin-top: 10px;
      }
    }
  }
  
  .comment-item {
    display: flex;
    gap: 20px;
    padding: 20px 0;
    border-bottom: 1px solid #eee;
    
    .comment-content {
      flex: 1;
      
      .user-info {
        display: flex;
        justify-content: space-between;
        margin-bottom: 10px;
        
        .name {
          font-weight: 600;
          color: #333;
        }
        
        .time {
          color: #999;
          font-size: 12px;
        }
      }
      
      .text {
        font-size: 14px;
        line-height: 1.6;
        color: #555;
        margin-bottom: 10px;
      }
      
      .comment-actions {
        display: flex;
        gap: 20px;
        font-size: 12px;
        color: #999;
        
        span {
          cursor: pointer;
          display: flex;
          align-items: center;
          gap: 4px;
          &:hover { text-decoration: underline; }
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .main-content {
    grid-template-columns: 1fr;
  }
  .sidebar {
    display: none;
  }
}
</style>
