<template>
  <el-card class="line-card" shadow="hover" @click="goDetail">
    <div class="card-image">
      <img :src="resolveImage(line.coverImage, 'line-' + line.id)" :alt="line.title" @error="onImgError" />
      <span class="days-tag">{{ line.days }}天{{ line.days - 1 }}晚</span>
    </div>
    <div class="card-content">
      <h3 class="title">{{ line.title }}</h3>
      <div class="info">
        <span class="destination"><el-icon><Location /></el-icon>{{ line.destination }}</span>
        <span class="departure">出发：{{ line.departureCity }}</span>
      </div>
      <div class="bottom">
        <div class="price">
          <span class="current">¥{{ line.price }}</span>
          <span class="unit">起/人</span>
        </div>
        <div class="stats">
          <span><el-icon><Star /></el-icon>{{ line.rating }}</span>
          <span>已售{{ line.sales }}</span>
        </div>
      </div>
      <div class="detail-hint">
        查看详情
        <el-icon><ArrowRight /></el-icon>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { resolveImage, onImageError as onImgError } from '@/utils/image'

const props = defineProps({
  line: { type: Object, required: true }
})

const router = useRouter()

function goDetail() {
  router.push(`/line/${props.line.id}`)
}
</script>

<style lang="scss" scoped>
.line-card {
  cursor: pointer;
  transition: transform 0.3s;
  &:hover { transform: translateY(-5px); }
  :deep(.el-card__body) { padding: 0; }
}
.card-image {
  position: relative;
  height: 180px;
  overflow: hidden;
  img { width: 100%; height: 100%; object-fit: cover; }
  .days-tag {
    position: absolute;
    top: 10px;
    right: 10px;
    background: rgba(0,0,0,0.6);
    color: #fff;
    padding: 4px 10px;
    border-radius: 4px;
    font-size: 12px;
  }
}
.card-content { padding: 15px; }
.title {
  font-size: 16px;
  margin-bottom: 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.info {
  display: flex;
  gap: 15px;
  font-size: 13px;
  color: #666;
  margin-bottom: 10px;
  .destination { display: flex; align-items: center; gap: 4px; }
}
.bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.price {
  .current { font-size: 20px; color: #ff6600; font-weight: bold; }
  .unit { font-size: 12px; color: #999; }
}
.stats {
  display: flex;
  gap: 10px;
  font-size: 12px;
  color: #999;
  span { display: flex; align-items: center; gap: 2px; }
}
.detail-hint {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 2px;
  margin-top: 10px;
  padding-top: 8px;
  border-top: 1px dashed #eee;
  color: #0086f6;
  font-size: 13px;
  font-weight: 500;
}
</style>
