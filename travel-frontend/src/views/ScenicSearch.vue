<template>
  <div class="scenic-search container">
    <div class="search-header">
      <h2>景点搜索</h2>
    </div>

    <div class="result-list" v-loading="loading">
      <el-empty v-if="!loading && !scenicList.length" description="暂未找到相关景点" />
      <div
        v-for="item in scenicList"
        :key="item.id"
        class="scenic-item"
      >
        <img :src="item.coverImage" class="cover" @error="onImgError" />
        <div class="info">
          <div class="title-row">
            <h3>{{ item.name }}</h3>
            <el-tag v-if="item.tag" size="small">{{ item.tag }}</el-tag>
          </div>
          <p class="location">
            <el-icon><Location /></el-icon>
            <span>{{ item.province }} {{ item.city }} {{ item.district }}</span>
          </p>
          <p class="address" v-if="item.address">{{ item.address }}</p>
        </div>
        <div class="actions">
          <el-button
            type="primary"
            :loading="addingId === item.id"
            @click="handleAddRoute(item)"
          >
            加入我的路线
          </el-button>
        </div>
      </div>

      <div class="pagination" v-if="pagination.total > pagination.size">
        <el-pagination
          v-model:current-page="pagination.page"
          :page-size="pagination.size"
          :total="pagination.total"
          layout="prev, pager, next"
          @current-change="fetchScenicSpots"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Location } from '@element-plus/icons-vue'
import { searchScenicSpots, addRouteSpot } from '@/api/user'

const route = useRoute()
const keyword = ref(route.query.keyword || '')
const loading = ref(false)
const scenicList = ref([])
const addingId = ref(null)

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const fallbackImg = 'https://picsum.photos/180/120?random=666'
function onImgError(e) {
  e.target.src = fallbackImg
  e.target.onerror = null
}

const fetchScenicSpots = async () => {
  loading.value = true
  try {
    const res = await searchScenicSpots({
      keyword: keyword.value,
      page: pagination.page,
      size: pagination.size
    })
    scenicList.value = res.records || []
    pagination.total = res.total || 0
  } catch (e) {
    console.error(e)
    ElMessage.error('景点查询失败')
  } finally {
    loading.value = false
  }
}

const handleAddRoute = async (item) => {
  if (!item.id) return
  addingId.value = item.id
  try {
    await addRouteSpot(item.id)
    ElMessage.success('已加入我的路线')
  } catch (e) {
    console.error(e)
  } finally {
    addingId.value = null
  }
}

onMounted(() => {
  if (keyword.value) {
    fetchScenicSpots()
  }
})

watch(
  () => route.query.keyword,
  (newKeyword) => {
    keyword.value = newKeyword || ''
    pagination.page = 1
    if (keyword.value) {
      fetchScenicSpots()
    } else {
      scenicList.value = []
      pagination.total = 0
    }
  }
)
</script>

<style scoped>
.scenic-search {
  padding-top: 20px;
}

.search-header {
  margin-bottom: 20px;
}

.result-list {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
}

.scenic-item {
  display: flex;
  gap: 20px;
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
}

.scenic-item:last-child {
  border-bottom: none;
}

.cover {
  width: 180px;
  height: 120px;
  border-radius: 8px;
  object-fit: cover;
}

.info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}

.title-row h3 {
  margin: 0;
}

.location {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #666;
  margin: 4px 0;
}

.address {
  color: #999;
  font-size: 13px;
}

.actions {
  display: flex;
  align-items: center;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
