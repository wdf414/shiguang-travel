<template>
  <div class="login-log">
    <el-card>
      <div class="toolbar">
        <el-input
          v-model="searchForm.keyword"
          placeholder="搜索手机号/昵称"
          style="width: 250px;"
          clearable
        />
        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </div>

      <el-table :data="logList" v-loading="loading" style="margin-top: 20px;">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="nickname" label="昵称" width="150" />
        <el-table-column prop="loginTime" label="登录时间" width="180" />
        <el-table-column prop="ip" label="IP地址" width="130" />
        <el-table-column prop="device" label="设备" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === '成功' ? 'success' : 'danger'">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        layout="total, prev, pager, next"
        @current-change="fetchLogs"
        style="margin-top: 20px; justify-content: center;"
      />
    </el-card>

    <el-card style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>登录统计</span>
          <el-select v-model="statsDays" @change="fetchStats" style="width: 120px">
            <el-option label="近7天" :value="7" />
            <el-option label="近30天" :value="30" />
            <el-option label="近90天" :value="90" />
          </el-select>
        </div>
      </template>
      <div ref="statsChartRef" style="height: 300px;"></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { getLoginLogs, getLoginStats } from '@/api/admin'
import * as echarts from 'echarts'

const loading = ref(false)
const logList = ref([])
const searchForm = reactive({ keyword: '' })
const pagination = reactive({ page: 1, size: 10, total: 0 })
const statsDays = ref(7)

const statsChartRef = ref()
let statsChart = null

async function fetchLogs() {
  loading.value = true
  try {
    const data = await getLoginLogs({
      page: pagination.page,
      size: pagination.size,
      keyword: searchForm.keyword
    })
    logList.value = data.records || []
    pagination.total = data.total || 0
  } catch (e) {
    console.error('加载登录日志失败', e)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  fetchLogs()
}

async function fetchStats() {
  try {
    const data = await getLoginStats(statsDays.value)
    const option = {
      title: { text: '登录统计趋势', left: 'center' },
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: data.dates },
      yAxis: { type: 'value', name: '登录次数' },
      series: [{
        name: '登录次数',
        type: 'line',
        data: data.loginCounts,
        smooth: true,
        itemStyle: { color: '#409eff' },
        areaStyle: { color: 'rgba(64, 158, 255, 0.2)' }
      }]
    }
    if (statsChart) statsChart.dispose()
    statsChart = echarts.init(statsChartRef.value)
    statsChart.setOption(option)
  } catch (e) {
    console.error('获取登录统计失败', e)
  }
}

onMounted(() => {
  fetchLogs()
  fetchStats()
})

onBeforeUnmount(() => {
  if (statsChart) statsChart.dispose()
})
</script>

<style lang="scss" scoped>
.login-log {
  padding: 20px;
}

.toolbar {
  display: flex;
  gap: 10px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
