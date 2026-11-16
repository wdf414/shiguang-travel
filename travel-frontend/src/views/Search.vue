<template>
  <div class="search-page container">
    <div class="search-header">
      <el-input
        v-model="keyword"
        placeholder="搜索目的地、线路名称"
        size="large"
        clearable
        @keyup.enter="handleSearch"
      >
        <template #append>
          <el-button @click="handleSearch"><el-icon><Search /></el-icon>搜索</el-button>
        </template>
      </el-input>
    </div>

    <div class="search-content">
      <!-- 筛选侧边栏 -->
      <aside class="filter-sidebar">
        <div class="filter-group">
          <h4>玩法标签</h4>
          <el-checkbox-group v-model="filters.tags">
            <el-checkbox v-for="t in tagOptions" :key="t" :value="t" :label="t">{{ t }}</el-checkbox>
          </el-checkbox-group>
        </div>
        <div class="filter-group">
          <h4>目的地</h4>
          <el-checkbox-group v-model="filters.destinations">
            <el-checkbox v-for="d in destinationOptions" :key="d" :value="d" :label="d">{{ d }}</el-checkbox>
          </el-checkbox-group>
        </div>
        <div class="filter-group">
          <h4>出发城市</h4>
          <el-select v-model="filters.departureCity" placeholder="选择出发城市" clearable>
            <el-option v-for="c in cityOptions" :key="c" :label="c" :value="c" />
          </el-select>
        </div>
        <div class="filter-group">
          <h4>价格区间</h4>
          <el-slider v-model="filters.priceRange" range :min="0" :max="20000" :step="500" />
          <div class="price-range-text">¥{{ filters.priceRange[0] }} - ¥{{ filters.priceRange[1] }}</div>
        </div>
        <div class="filter-group">
          <h4>行程天数</h4>
          <el-checkbox-group v-model="filters.days">
            <el-checkbox :value="3">3天以内</el-checkbox>
            <el-checkbox :value="5">4-5天</el-checkbox>
            <el-checkbox :value="7">6-7天</el-checkbox>
            <el-checkbox :value="8">8天以上</el-checkbox>
          </el-checkbox-group>
        </div>
        <el-button type="primary" @click="handleSearch">筛选</el-button>
        <el-button @click="resetFilters">重置</el-button>
      </aside>

      <!-- 结果列表 -->
      <main class="result-main">
        <div class="result-header">
          <span class="total">共找到 {{ pagination.total }} 条线路</span>
          <div class="sort-options">
            <span 
              v-for="opt in sortOptions" 
              :key="opt.value"
              :class="{ active: sortBy === opt.value }"
              @click="sortBy = opt.value; handleSearch()"
            >
              {{ opt.label }}
            </span>
          </div>
        </div>

        <div class="result-list" v-loading="loading">
          <LineCard v-for="line in lineList" :key="line.id" :line="line" />
        </div>

        <el-empty v-if="!loading && !lineList.length" description="暂无符合条件的线路" />

        <div class="pagination">
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.size"
            :total="pagination.total"
            layout="prev, pager, next, jumper"
            @current-change="handleSearch"
          />
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import LineCard from '@/components/LineCard.vue'
import { getLineList } from '@/api/line'
import { localPhoto } from '@/utils/image'

const route = useRoute()
const loading = ref(false)
const keyword = ref('')
const sortBy = ref('')
const lineList = ref([])

const destinationOptions = ['云南', '海南', '四川', '西藏', '新疆', '北京', '广东', '浙江']
const cityOptions = ['上海', '北京', '广州', '深圳', '成都', '杭州']
const sortOptions = [
  { label: '默认', value: '' },
  { label: '价格↑', value: 'price_asc' },
  { label: '价格↓', value: 'price_desc' },
  { label: '评分', value: 'rating' },
  { label: '销量', value: 'sales' }
]

const filters = reactive({
  destinations: [],
  departureCity: '',
  priceRange: [0, 20000],
  days: [],
  tags: []
})

const tagOptions = ['古城', '摄影', '美食', '海岛', '自然', '徒步', '历史', '研学']

const pagination = reactive({
  page: 1,
  size: 12,
  total: 0
})

async function handleSearch() {
  loading.value = true
  try {
    // 处理天数筛选 - 将选中的天数范围转换为 minDays 和 maxDays
    let minDays = undefined
    let maxDays = undefined
    if (filters.days.length > 0) {
      const selectedDays = filters.days.map(d => Number(d))
      // 找出最小和最大的天数范围
      const minSelected = Math.min(...selectedDays)
      const maxSelected = Math.max(...selectedDays)
      
      // 根据选中的值确定范围
      if (minSelected === 3) minDays = 1  // 3天以内 = 1-3天
      else if (minSelected === 5) minDays = 4  // 4-5天
      else if (minSelected === 7) minDays = 6  // 6-7天
      else if (minSelected === 8) minDays = 8  // 8天以上
      
      if (maxSelected === 3) maxDays = 3
      else if (maxSelected === 5) maxDays = 5
      else if (maxSelected === 7) maxDays = 7
      else if (maxSelected === 8) maxDays = 30  // 8天以上，设置一个较大的值
    }
    
    const params = {
      page: pagination.page,
      size: pagination.size,
      keyword: keyword.value || undefined,
      destinations: filters.destinations.length ? filters.destinations.join(',') : undefined,
      departureCity: filters.departureCity || undefined,
      minPrice: filters.priceRange[0] || undefined,
      maxPrice: filters.priceRange[1] < 20000 ? filters.priceRange[1] : undefined,
      minDays: minDays,
      maxDays: maxDays,
      tags: filters.tags.length ? filters.tags.join(',') : undefined,
      sortBy: sortBy.value || undefined
    }
    const data = await getLineList(params)
    lineList.value = data.records
    pagination.total = data.total
  } catch {
    // 模拟数据
    lineList.value = Array.from({ length: 12 }, (_, i) => ({
      id: i + 1,
      title: `${destinationOptions[i % 8]}精品${[3,4,5,6,7][i % 5]}日游`,
      coverImage: localPhoto(`search-${i}`),
      destination: destinationOptions[i % 8],
      departureCity: cityOptions[i % 6],
      days: [3,4,5,6,7][i % 5],
      price: 1999 + i * 500,
      rating: (4 + Math.random()).toFixed(1),
      sales: Math.floor(Math.random() * 500) + 50
    }))
    pagination.total = 50
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.destinations = []
  filters.departureCity = ''
  filters.priceRange = [0, 20000]
  filters.days = []
  filters.tags = []
  keyword.value = ''
  sortBy.value = ''
  pagination.page = 1
  handleSearch()
}

onMounted(() => {
  if (route.query.keyword) keyword.value = route.query.keyword
  if (route.query.destination) filters.destinations = [route.query.destination]
  handleSearch()
})
</script>

<style lang="scss" scoped>
// 穷游网风格 Search
$primary: #0086f6;
$text-primary: #333;
$text-secondary: #666;
$bg-color: #f5f5f5;

.search-page {
  padding-top: 30px;
  padding-bottom: 60px;
  min-height: calc(100vh - 140px);
  background: $bg-color;
}

.search-header {
  margin-bottom: 30px;
  
  .el-input {
    max-width: 700px;
    
    :deep(.el-input__wrapper) {
      border-radius: 4px;
      padding: 6px 10px 6px 15px;
      box-shadow: none !important;
      border: 2px solid $primary; // 穷游通常有明显的搜索框边框
      transition: all 0.2s;
      background: #fff;
    }
    
    :deep(.el-input-group__append) {
      background: $primary;
      border: 2px solid $primary;
      border-left: none;
      border-radius: 0 4px 4px 0;
      padding: 0 25px;
      
      .el-button {
        background: transparent;
        border: none;
        color: #fff;
        font-weight: 500;
        
        &:hover {
          background: transparent;
        }
      }
    }
  }
}

.search-content {
  display: grid;
  grid-template-columns: 260px 1fr; // 侧边栏稍微窄一点
  gap: 30px;
}

.filter-sidebar {
  background: #fff;
  padding: 20px;
  border-radius: 4px;
  height: fit-content;
  position: sticky;
  top: 80px;
  border: 1px solid #eee;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  
  .el-button {
    width: 100%;
    margin-top: 10px;
    border-radius: 2px;
    font-weight: 500;
    margin-left: 0 !important;
    
    &.el-button--primary {
      background: $primary;
      border: none;
      
      &:hover {
        background: #0070d6;
        transform: none;
        box-shadow: none;
      }
    }
  }
}

.filter-group {
  margin-bottom: 25px;
  
  h4 {
    margin-bottom: 12px;
    color: $text-primary;
    font-weight: 600;
    font-size: 14px;
    display: flex;
    align-items: center;
    gap: 8px;
    
    &::before {
      content: '';
      width: 3px;
      height: 14px;
      background: $primary;
      border-radius: 0;
    }
  }
  
  :deep(.el-checkbox-group), :deep(.el-radio-group) {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }

  :deep(.el-radio) {
    margin-right: 0;
    padding: 0;
    display: flex;
    align-items: center;
    height: auto;

    .el-radio__input {
      display: flex;
      align-items: center;
      justify-content: center;
      height: 14px;
      width: 14px;

      .el-radio__inner {
        width: 14px;
        height: 14px;

        &::after {
          width: 4px;
          height: 4px;
        }
      }
    }

    .el-radio__label {
      color: $text-secondary;
      padding-left: 8px;
      font-size: 14px;
      line-height: 1;
      vertical-align: middle;
    }

    &.is-checked .el-radio__label {
      color: $primary;
    }

    .el-radio__input.is-checked .el-radio__inner {
      background-color: $primary;
      border-color: $primary;
    }
  }

  :deep(.el-checkbox) {
    margin-right: 0;
    padding: 0;
    display: flex;
    align-items: center;
    height: auto;

    .el-checkbox__input {
      display: flex;
      align-items: center;
      justify-content: center;
      height: 14px;
      width: 14px;

      .el-checkbox__inner {
        width: 14px;
        height: 14px;

        &::after {
          box-sizing: content-box;
          content: "";
          border: 1px solid #fff;
          border-left: 0;
          border-top: 0;
          height: 7px;
          left: 4px;
          position: absolute;
          top: 1px;
          transform: rotate(45deg) scaleY(0);
          width: 3px;
          transition: transform .15s ease-in .05s;
          transform-origin: center;
        }
      }

      &.is-checked .el-checkbox__inner::after {
        transform: rotate(45deg) scaleY(1);
      }
    }

    .el-checkbox__label {
      color: $text-secondary;
      padding-left: 8px;
      font-size: 14px;
      line-height: 1;
      vertical-align: middle;
    }

    &.is-checked .el-checkbox__label {
      color: $primary;
    }

    .el-checkbox__input.is-checked .el-checkbox__inner {
      background-color: $primary;
      border-color: $primary;
    }

    &:hover {
      background: transparent;
    }
  }
  
  :deep(.el-slider) {
    .el-slider__bar {
      background: $primary;
    }
    .el-slider__button {
      border-color: $primary;
    }
  }
}

.price-range-text {
  text-align: center;
  color: $primary;
  font-size: 14px;
  font-weight: 600;
  margin-top: 10px;
  background: transparent;
}

.result-main {
  min-width: 0;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 15px 20px;
  background: #fff;
  border-radius: 4px;
  border: 1px solid #eee;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
  
  .total {
    color: $text-secondary;
    font-size: 14px;
    
    strong {
      color: $primary;
      font-size: 16px;
    }
  }
}

.sort-options {
  display: flex;
  gap: 15px; // 增加间距
  
  span {
    cursor: pointer;
    color: $text-secondary;
    padding: 4px 12px;
    border-radius: 2px;
    font-size: 14px;
    transition: all 0.2s;
    
    &:hover {
      color: $primary;
      background: rgba(0, 134, 246, 0.05);
    }
    
    &.active {
      color: #fff;
      background: $primary;
      box-shadow: none;
    }
  }
}

.result-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  min-height: 400px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 40px;
  
  :deep(.el-pagination) {
    .el-pager li.is-active {
        background-color: $primary !important;
        color: #fff;
    }
  }
}

:deep(.el-empty) {
  padding: 60px 0;
}

@media (max-width: 1024px) {
  .result-list { grid-template-columns: repeat(2, 1fr); }
  .search-content { grid-template-columns: 240px 1fr; gap: 20px; }
}

@media (max-width: 768px) {
  .search-content { grid-template-columns: 1fr; }
  .filter-sidebar { 
    position: static;
    margin-bottom: 20px;
  }
  .result-list { grid-template-columns: 1fr; }
  .result-header {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
  }
}
</style>
