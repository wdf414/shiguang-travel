<template>
  <div class="my-favorites">
    <h3>我的收藏</h3>
    
    <div class="favorite-list" v-loading="loading">
      <div v-for="item in favoriteList" :key="item.id" class="favorite-item">
        <img :src="resolveImage(item.coverImage, 'favorite-' + item.lineId)" class="cover" @click="goDetail(item.lineId)" @error="onImgError" />
        <div class="info">
          <h4 @click="goDetail(item.lineId)">{{ item.title }}</h4>
          <p><el-icon><Location /></el-icon>{{ item.destination }} | {{ item.days }}天</p>
          <div class="bottom">
            <span class="price">¥{{ item.price }}<em>起</em></span>
            <el-button type="danger" link @click="handleUnfavorite(item.lineId)">取消收藏</el-button>
          </div>
        </div>
      </div>

      <el-empty v-if="!loading && !favoriteList.length" description="暂无收藏" />
    </div>

    <div class="pagination">
      <el-pagination
        v-model:current-page="pagination.page"
        :total="pagination.total"
        layout="prev, pager, next"
        @current-change="fetchFavorites"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getFavoriteList, unfavoriteLine } from '@/api/line'
import { resolveImage, localPhoto, FALLBACK_IMAGE } from '@/utils/image'

const router = useRouter()
const loading = ref(false)
const favoriteList = ref([])

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

async function fetchFavorites() {
  loading.value = true
  try {
    const data = await getFavoriteList({ page: pagination.page, size: pagination.size })
    favoriteList.value = data.records
    pagination.total = data.total
  } catch {
    favoriteList.value = [
      { id: 1, lineId: 1, title: '云南大理丽江双飞6日游', coverImage: localPhoto('favorite-1'), destination: '云南', days: 6, price: 2999 },
      { id: 2, lineId: 2, title: '海南三亚5日游', coverImage: localPhoto('favorite-2'), destination: '海南', days: 5, price: 3599 }
    ]
    pagination.total = 2
  } finally {
    loading.value = false
  }
}

function goDetail(lineId) {
  router.push(`/line/${lineId}`)
}

async function handleUnfavorite(lineId) {
  try {
    await unfavoriteLine(lineId)
    ElMessage.success('已取消收藏')
    favoriteList.value = favoriteList.value.filter(f => f.lineId !== lineId)
  } catch {
    favoriteList.value = favoriteList.value.filter(f => f.lineId !== lineId)
  }
}

onMounted(fetchFavorites)

const fallbackImg = FALLBACK_IMAGE
function onImgError(e) {
  e.target.src = fallbackImg
  e.target.onerror = null
}
</script>

<style lang="scss" scoped>
h3 { margin-bottom: 20px; }
.favorite-item {
  display: flex;
  gap: 20px;
  padding: 20px;
  border-bottom: 1px solid #eee;
  &:last-child { border-bottom: none; }
  .cover { width: 200px; height: 130px; object-fit: cover; border-radius: 8px; cursor: pointer; }
  .info { flex: 1; display: flex; flex-direction: column; h4 { cursor: pointer; &:hover { color: #409eff; } } p { color: #666; margin: 10px 0; display: flex; align-items: center; gap: 5px; } }
  .bottom { margin-top: auto; display: flex; justify-content: space-between; align-items: center; }
  .price { font-size: 22px; color: #ff6600; font-weight: bold; em { font-size: 12px; font-style: normal; color: #999; } }
}
.pagination { display: flex; justify-content: center; margin-top: 20px; }
</style>
