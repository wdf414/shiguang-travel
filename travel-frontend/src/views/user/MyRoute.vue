<template>
  <div class="my-route">
    <h3>我的路线</h3>

    <div class="route-list" v-loading="loading">
      <el-empty v-if="!loading && !routeList.length" description="还没有添加任何景点" />
      <div
        v-for="item in routeList"
        :key="item.id"
        class="route-item"
      >
        <img :src="item.coverImage" class="cover" @error="onImgError" />
        <div class="info">
          <h4>{{ item.name }}</h4>
          <p class="location">
            <el-icon><Location /></el-icon>
            <span>{{ item.province }} {{ item.city }} {{ item.district }}</span>
          </p>
          <p class="address" v-if="item.address">{{ item.address }}</p>
        </div>
        <div class="actions">
          <el-button type="danger" link @click="handleRemove(item.id)">移出路线</el-button>
        </div>
      </div>

      <div class="pagination" v-if="pagination.total > pagination.size">
        <el-pagination
          v-model:current-page="pagination.page"
          :page-size="pagination.size"
          :total="pagination.total"
          layout="prev, pager, next"
          @current-change="fetchRoute"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Location } from '@element-plus/icons-vue'
import { getRouteSpots, deleteRouteSpot } from '@/api/user'

const loading = ref(false)
const routeList = ref([])

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const fetchRoute = async () => {
  loading.value = true
  try {
    const res = await getRouteSpots({
      page: pagination.page,
      size: pagination.size
    })
    routeList.value = res.records || []
    pagination.total = res.total || 0
  } catch (e) {
    console.error(e)
    ElMessage.error('加载路线失败')
  } finally {
    loading.value = false
  }
}

const handleRemove = async (id) => {
  try {
    await deleteRouteSpot(id)
    ElMessage.success('已移出路线')
    routeList.value = routeList.value.filter(item => item.id !== id)
    if (!routeList.value.length && pagination.page > 1) {
      pagination.page -= 1
      fetchRoute()
    }
  } catch (e) {
    console.error(e)
  }
}

onMounted(fetchRoute)

const fallbackImg = 'https://picsum.photos/160/110?random=222'
function onImgError(e) {
  e.target.src = fallbackImg
  e.target.onerror = null
}
</script>

<style scoped>
.my-route h3 {
  margin-bottom: 20px;
}

.route-list {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
}

.route-item {
  display: flex;
  gap: 20px;
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
}

.route-item:last-child {
  border-bottom: none;
}

.cover {
  width: 160px;
  height: 110px;
  border-radius: 8px;
  object-fit: cover;
}

.info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.location {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #666;
  margin: 6px 0;
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
