<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #409eff;">
            <el-icon><Document /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalOrders || 0 }}</div>
            <div class="stat-label">总订单数</div>
            <div class="stat-sub">今日: {{ stats.todayOrders || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #67c23a;">
            <el-icon><User /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalUsers || 0 }}</div>
            <div class="stat-label">总用户数</div>
            <div class="stat-sub">今日: {{ stats.todayUsers || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #e6a23c;">
            <el-icon><Guide /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalLines || 0 }}</div>
            <div class="stat-label">总线路数</div>
            <div class="stat-sub">待处理: {{ stats.pendingOrders || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #f56c6c;">
            <el-icon><Money /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">¥{{ stats.totalRevenue || 0 }}</div>
            <div class="stat-label">总交易额</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card>
          <template #header>
            <span>销售趋势</span>
          </template>
          <div ref="salesChartRef" style="height: 300px;"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <el-tabs @tab-click="handleResize">
            <el-tab-pane label="登录时段占比">
              <div ref="loginPieChartRef" style="height: 300px;"></div>
            </el-tab-pane>
            <el-tab-pane label="热门线路">
              <div class="hot-lines">
                <div v-for="(line, idx) in stats.hotLines" :key="line.id" class="hot-line-item">
                  <span class="rank">{{ idx + 1 }}</span>
                  <span class="title">{{ line.title }}</span>
                  <span class="sales">{{ line.sales }}人</span>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card>
          <template #header>
            <span>近期用户数量变化</span>
          </template>
          <div ref="userChartRef" style="height: 320px;"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, onBeforeUnmount } from 'vue'
import { getStatsOverview, getSalesTrend, getUserTrend, getLoginHourStats } from '@/api/admin'
import * as echarts from 'echarts'

const stats = ref({})
const salesChartRef = ref()
const userChartRef = ref()
const loginPieChartRef = ref()

let salesChart = null
let userChart = null
let loginPieChart = null

async function fetchStats() {
  try {
    stats.value = await getStatsOverview()
  } catch (e) {
    console.error(e)
  }
}

async function fetchTrend() {
  try {
    const data = await getSalesTrend(7)
    await nextTick()
    initSalesChart(data)
  } catch (e) {
    console.error(e)
  }
}

async function fetchUserStats() {
  try {
    const data = await getUserTrend(14)
    await nextTick()
    initUserChart(data)
  } catch (e) {
    console.error(e)
  }
}

async function fetchLoginHourStats() {
  try {
    const data = await getLoginHourStats(7)
    await nextTick()
    initLoginPieChart(data)
  } catch (e) {
    console.error(e)
  }
}

function initSalesChart(data) {
  if (!salesChartRef.value) return
  if (salesChart) salesChart.dispose()
  salesChart = echarts.init(salesChartRef.value)
  salesChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['订单数', '交易额'] },
    xAxis: { type: 'category', data: data?.dates || [] },
    yAxis: [
      { type: 'value', name: '订单数' },
      { type: 'value', name: '交易额' }
    ],
    series: [
      { name: '订单数', type: 'line', smooth: true, data: data?.orderCounts || [] },
      { name: '交易额', type: 'line', smooth: true, yAxisIndex: 1, data: data?.revenues || [] }
    ]
  })
}

function initUserChart(data) {
  if (!userChartRef.value) return
  if (userChart) userChart.dispose()
  userChart = echarts.init(userChartRef.value)
  userChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['总用户', '新增用户'] },
    xAxis: { type: 'category', data: data?.dates || [] },
    yAxis: [
      { type: 'value', name: '总用户' },
      { type: 'value', name: '新增用户' }
    ],
    series: [
      {
        name: '总用户',
        type: 'line',
        smooth: true,
        data: data?.totalUsers || []
      },
      {
        name: '新增用户',
        type: 'bar',
        yAxisIndex: 1,
        data: data?.newUsers || []
      }
    ]
  })
}

function initLoginPieChart(data) {
  if (!loginPieChartRef.value) return
  if (loginPieChart) loginPieChart.dispose()
  loginPieChart = echarts.init(loginPieChartRef.value)

  const hours = data?.hours || []
  const counts = data?.counts || []

  const hourBuckets = new Array(24).fill(0)
  hours.forEach((h, idx) => {
    const hour = Number(h)
    if (!Number.isFinite(hour) || hour < 0 || hour > 23) return
    hourBuckets[hour] = Number(counts[idx] || 0)
  })

  const ranges = [
    { label: '凌晨 00-06', start: 0, end: 6 },
    { label: '上午 06-10', start: 6, end: 10 },
    { label: '中午 10-14', start: 10, end: 14 },
    { label: '下午 14-18', start: 14, end: 18 },
    { label: '晚上 18-22', start: 18, end: 22 },
    { label: '深夜 22-24', start: 22, end: 24 }
  ]

  const seriesData = ranges.map(r => {
    let sum = 0
    for (let h = r.start; h < r.end; h++) sum += hourBuckets[h] || 0
    return { name: r.label, value: sum }
  }).filter(x => x.value > 0)

  loginPieChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { type: 'scroll', bottom: 0 },
    series: [
      {
        type: 'pie',
        radius: ['35%', '70%'],
        avoidLabelOverlap: true,
        label: { show: false },
        emphasis: { label: { show: true, fontSize: 12 } },
        labelLine: { show: false },
        data: seriesData
      }
    ]
  })
}

function handleResize() {
  try {
    salesChart?.resize()
    userChart?.resize()
    loginPieChart?.resize()
  } catch (e) {
  }
}

onMounted(() => {
  fetchStats()
  fetchTrend()
  fetchUserStats()
  fetchLoginHourStats()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  try {
    salesChart?.dispose()
    userChart?.dispose()
    loginPieChart?.dispose()
  } catch (e) {
  }
})
</script>

<style lang="scss" scoped>
.stat-card {
  :deep(.el-card__body) {
    display: flex;
    align-items: center;
    gap: 20px;
  }
}
.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #fff;
}
.stat-content {
  flex: 1;
}
.stat-value {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 5px;
}
.stat-label {
  color: #999;
  font-size: 14px;
}
.stat-sub {
  color: #666;
  font-size: 12px;
  margin-top: 5px;
}

.hot-line-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid #eee;
  &:last-child { border-bottom: none; }
  .rank {
    width: 24px;
    height: 24px;
    background: #409eff;
    color: #fff;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 12px;
  }
  .title { flex: 1; font-size: 14px; }
  .sales { color: #999; font-size: 12px; }
}
</style>
