<template>
  <div class="statistics-page">
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <!-- 销售分析 -->
      <el-tab-pane label="销售分析" name="sales">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>按时间统计销售额与订单量</span>
              <el-select v-model="salesDays" @change="fetchSalesData" style="width: 120px">
                <el-option label="近7天" :value="7" />
                <el-option label="近30天" :value="30" />
                <el-option label="近90天" :value="90" />
              </el-select>
            </div>
          </template>
          <div ref="salesChartRef" style="height: 400px;"></div>
        </el-card>

        <el-card style="margin-top: 20px;">
          <template #header>
            <span>热门线路排行榜</span>
          </template>
          <el-table :data="hotLinesRanking" stripe>
            <el-table-column type="index" label="排名" width="80" />
            <el-table-column prop="title" label="线路名称" />
            <el-table-column prop="sales" label="销量" width="100" />
            <el-table-column prop="orderCount" label="订单数" width="100" />
            <el-table-column prop="revenue" label="销售额" width="120">
              <template #default="{ row }">
                ¥{{ row.revenue || 0 }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <el-card style="margin-top: 20px;">
          <template #header>
            <span>用户购买行为分析</span>
          </template>
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="stat-box">
                <div class="stat-value">{{ purchaseBehavior.singlePurchase || 0 }}</div>
                <div class="stat-label">单次购买用户</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-box">
                <div class="stat-value">{{ purchaseBehavior.multiPurchase || 0 }}</div>
                <div class="stat-label">多次购买用户(2-3次)</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-box">
                <div class="stat-value">{{ purchaseBehavior.frequentPurchase || 0 }}</div>
                <div class="stat-label">频繁购买用户(>3次)</div>
              </div>
            </el-col>
          </el-row>
          <el-row :gutter="20" style="margin-top: 20px;">
            <el-col :span="12">
              <div ref="payTypeChartRef" style="height: 300px;"></div>
            </el-col>
            <el-col :span="12">
              <div ref="travelerCountChartRef" style="height: 300px;"></div>
            </el-col>
          </el-row>
        </el-card>
      </el-tab-pane>

      <!-- 用户分析 -->
      <el-tab-pane label="用户分析" name="users">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>用户增长统计</span>
              <el-select v-model="userDays" @change="fetchUserData" style="width: 120px">
                <el-option label="近7天" :value="7" />
                <el-option label="近30天" :value="30" />
                <el-option label="近90天" :value="90" />
              </el-select>
            </div>
          </template>
          <div ref="userGrowthChartRef" style="height: 400px;"></div>
        </el-card>

        <el-card style="margin-top: 20px;">
          <template #header>
            <div class="card-header">
              <span>用户活跃度分析</span>
              <el-select v-model="activityDays" @change="fetchActivityData" style="width: 120px">
                <el-option label="近7天" :value="7" />
                <el-option label="近30天" :value="30" />
                <el-option label="近90天" :value="90" />
              </el-select>
            </div>
          </template>
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="stat-box">
                <div class="stat-value">{{ userActivity.activeUsers || 0 }}</div>
                <div class="stat-label">活跃用户数</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-box">
                <div class="stat-value">{{ userActivity.newUsersInPeriod || 0 }}</div>
                <div class="stat-label">新增用户数</div>
              </div>
            </el-col>
          </el-row>
          <div ref="activityChartRef" style="height: 300px; margin-top: 20px;"></div>
        </el-card>

        <el-card style="margin-top: 20px;">
          <template #header>
            <span>用户画像分析</span>
          </template>
          <el-row :gutter="20">
            <el-col :span="12">
              <div ref="userStatusChartRef" style="height: 300px;"></div>
            </el-col>
            <el-col :span="12">
              <div ref="userOrderChartRef" style="height: 300px;"></div>
            </el-col>
          </el-row>
          <div style="margin-top: 20px; text-align: center;">
            <el-statistic title="高价值用户数" :value="userProfile.highValueUsers || 0" />
          </div>
        </el-card>
      </el-tab-pane>

      <!-- 学生分析 -->
      <el-tab-pane label="学生用户分析" name="student">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>学生分布统计</span>
            </div>
          </template>
          <el-row :gutter="20">
            <el-col :span="12">
              <div ref="schoolDistributionChartRef" style="height: 400px;"></div>
            </el-col>
            <el-col :span="12">
              <div ref="gradeDistributionChartRef" style="height: 400px;"></div>
            </el-col>
          </el-row>
        </el-card>

        <el-card style="margin-top: 20px;">
          <template #header>
            <span>消费能力与目的地</span>
          </template>
          <el-row :gutter="20">
             <el-col :span="8">
              <div ref="consumptionChartRef" style="height: 400px;"></div>
            </el-col>
            <el-col :span="16">
              <div ref="hotDestinationsChartRef" style="height: 400px;"></div>
            </el-col>
          </el-row>
        </el-card>

        <el-card style="margin-top: 20px;">
          <template #header>
            <div class="card-header">
              <span>拼团热度趋势</span>
              <el-select v-model="groupTrendDays" @change="fetchGroupTravelTrendData" style="width: 120px">
                <el-option label="近7天" :value="7" />
                <el-option label="近30天" :value="30" />
                <el-option label="近90天" :value="90" />
              </el-select>
            </div>
          </template>
          <div ref="groupTrendChartRef" style="height: 300px;"></div>
        </el-card>
      </el-tab-pane>

      <!-- 流量分析 -->
      <el-tab-pane label="流量分析" name="traffic">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>页面访问统计</span>
              <el-select v-model="visitDays" @change="fetchVisitData" style="width: 120px">
                <el-option label="近7天" :value="7" />
                <el-option label="近30天" :value="30" />
                <el-option label="近90天" :value="90" />
              </el-select>
            </div>
          </template>
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="stat-box">
                <div class="stat-value">{{ pageVisits.homePageVisits || 0 }}</div>
                <div class="stat-label">首页访问</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-box">
                <div class="stat-value">{{ pageVisits.lineDetailVisits || 0 }}</div>
                <div class="stat-label">线路详情页</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-box">
                <div class="stat-value">{{ pageVisits.searchPageVisits || 0 }}</div>
                <div class="stat-label">搜索页</div>
              </div>
            </el-col>
          </el-row>
          <div ref="visitTrendChartRef" style="height: 300px; margin-top: 20px;"></div>
        </el-card>

        <el-card style="margin-top: 20px;">
          <template #header>
            <span>用户来源分析</span>
          </template>
          <div ref="sourceChartRef" style="height: 400px;"></div>
        </el-card>

        <el-card style="margin-top: 20px;">
          <template #header>
            <div class="card-header">
              <span>转化率分析</span>
              <el-select v-model="conversionDays" @change="fetchConversionData" style="width: 120px">
                <el-option label="近7天" :value="7" />
                <el-option label="近30天" :value="30" />
                <el-option label="近90天" :value="90" />
              </el-select>
            </div>
          </template>
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="stat-box">
                <div class="stat-value">{{ conversionRate.visitToOrderRate || 0 }}%</div>
                <div class="stat-label">访问→下单转化率</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-box">
                <div class="stat-value">{{ conversionRate.orderToPaidRate || 0 }}%</div>
                <div class="stat-label">下单→支付转化率</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-box">
                <div class="stat-value">{{ conversionRate.visitToPaidRate || 0 }}%</div>
                <div class="stat-label">访问→支付转化率</div>
              </div>
            </el-col>
          </el-row>
          <div ref="conversionChartRef" style="height: 300px; margin-top: 20px;"></div>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import {
  getSalesTrend,
  getHotLinesRanking,
  getUserPurchaseBehavior,
  getUserActivity,
  getUserProfile,
  getPageVisitStats,
  getUserSource,
  getConversionRate,
  getStudentAnalysis,
  getStudentHotDestinations,
  getGroupTravelTrend
} from '@/api/admin'

const activeTab = ref('sales')
const salesDays = ref(7)
const userDays = ref(30)
const activityDays = ref(30)
const visitDays = ref(30)
const conversionDays = ref(30)
const groupTrendDays = ref(30)

const hotLinesRanking = ref([])
const purchaseBehavior = ref({})
const userActivity = ref({})
const userProfile = ref({})
const pageVisits = ref({})
const conversionRate = ref({})
const studentAnalysis = ref({})
const studentDestinations = ref({})
const groupTrend = ref({})

const salesChartRef = ref()
const payTypeChartRef = ref()
const travelerCountChartRef = ref()
const userGrowthChartRef = ref()
const activityChartRef = ref()
const userStatusChartRef = ref()
const userOrderChartRef = ref()
const visitTrendChartRef = ref()
const sourceChartRef = ref()
const conversionChartRef = ref()
const schoolDistributionChartRef = ref()
const gradeDistributionChartRef = ref()
const consumptionChartRef = ref()
const hotDestinationsChartRef = ref()
const groupTrendChartRef = ref()

let charts = {}

function initChart(ref, option) {
  if (!ref.value) return null
  const chart = echarts.init(ref.value)
  chart.setOption(option)
  return chart
}

async function fetchSalesData() {
  try {
    const data = await getSalesTrend(salesDays.value)
    const option = {
      title: { text: '销售额与订单量趋势', left: 'center' },
      tooltip: { trigger: 'axis' },
      legend: { data: ['订单量', '销售额'], top: 30 },
      xAxis: { type: 'category', data: data.dates },
      yAxis: [
        { type: 'value', name: '订单量', position: 'left' },
        { type: 'value', name: '销售额(元)', position: 'right' }
      ],
      series: [
        {
          name: '订单量',
          type: 'bar',
          data: data.orderCounts,
          itemStyle: { color: '#409eff' }
        },
        {
          name: '销售额',
          type: 'line',
          yAxisIndex: 1,
          data: data.revenues.map(r => r.toString()),
          itemStyle: { color: '#67c23a' }
        }
      ]
    }
    if (charts.sales) charts.sales.dispose()
    charts.sales = initChart(salesChartRef, option)
  } catch (e) {
    console.error('获取销售数据失败', e)
  }
}

async function fetchHotLines() {
  try {
    const data = await getHotLinesRanking(10)
    hotLinesRanking.value = data.rankings || []
  } catch (e) {
    console.error('获取热门线路失败', e)
  }
}

async function fetchPurchaseBehavior() {
  try {
    const data = await getUserPurchaseBehavior()
    purchaseBehavior.value = data

    const payTypeData = Object.entries(data.payTypeDistribution || {}).map(([name, value]) => ({
      name: name === 'alipay' ? '支付宝' : name === 'wechat' ? '微信' : name === 'bank' ? '银行卡' : name,
      value
    }))
    const payTypeOption = {
      title: { text: '支付方式分布', left: 'center' },
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie',
        radius: '60%',
        data: payTypeData
      }]
    }
    if (charts.payType) charts.payType.dispose()
    charts.payType = initChart(payTypeChartRef, payTypeOption)

    const travelerData = Object.entries(data.travelerCountDistribution || {}).map(([name, value]) => ({
      name: `${name}人`,
      value
    }))
    const travelerOption = {
      title: { text: '出行人数分布', left: 'center' },
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie',
        radius: '60%',
        data: travelerData
      }]
    }
    if (charts.travelerCount) charts.travelerCount.dispose()
    charts.travelerCount = initChart(travelerCountChartRef, travelerOption)
  } catch (e) {
    console.error('获取购买行为数据失败', e)
  }
}

async function fetchUserData() {
  try {
    const data = await getUserTrend(userDays.value)
    const option = {
      title: { text: '用户增长趋势', left: 'center' },
      tooltip: { trigger: 'axis' },
      legend: { data: ['新增用户', '累计用户'], top: 30 },
      xAxis: { type: 'category', data: data.dates },
      yAxis: { type: 'value' },
      series: [
        {
          name: '新增用户',
          type: 'bar',
          data: data.newUsers,
          itemStyle: { color: '#409eff' }
        },
        {
          name: '累计用户',
          type: 'line',
          data: data.totalUsers,
          itemStyle: { color: '#67c23a' }
        }
      ]
    }
    if (charts.userGrowth) charts.userGrowth.dispose()
    charts.userGrowth = initChart(userGrowthChartRef, option)
  } catch (e) {
    console.error('获取用户数据失败', e)
  }
}

async function fetchActivityData() {
  try {
    const data = await getUserActivity(activityDays.value)
    userActivity.value = data
    const option = {
      title: { text: '每日活跃用户趋势', left: 'center' },
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: data.dates },
      yAxis: { type: 'value' },
      series: [{
        name: '活跃用户数',
        type: 'line',
        data: data.dailyActiveUsers,
        smooth: true,
        itemStyle: { color: '#e6a23c' },
        areaStyle: { color: 'rgba(230, 162, 60, 0.2)' }
      }]
    }
    if (charts.activity) charts.activity.dispose()
    charts.activity = initChart(activityChartRef, option)
  } catch (e) {
    console.error('获取活跃度数据失败', e)
  }
}

async function fetchUserProfile() {
  try {
    const data = await getUserProfile()
    userProfile.value = data

    const statusOption = {
      title: { text: '用户状态分布', left: 'center' },
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie',
        radius: '60%',
        data: [
          { name: '正常用户', value: data.activeUsers || 0 },
          { name: '禁用用户', value: data.inactiveUsers || 0 }
        ]
      }]
    }
    if (charts.userStatus) charts.userStatus.dispose()
    charts.userStatus = initChart(userStatusChartRef, statusOption)

    const orderOption = {
      title: { text: '用户订单分布', left: 'center' },
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie',
        radius: '60%',
        data: [
          { name: '有订单用户', value: data.usersWithOrders || 0 },
          { name: '无订单用户', value: data.usersWithoutOrders || 0 }
        ]
      }]
    }
    if (charts.userOrder) charts.userOrder.dispose()
    charts.userOrder = initChart(userOrderChartRef, orderOption)
  } catch (e) {
    console.error('获取用户画像失败', e)
  }
}

async function fetchVisitData() {
  try {
    const data = await getPageVisitStats(visitDays.value)
    pageVisits.value = data
    const option = {
      title: { text: '页面访问趋势', left: 'center' },
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: data.dates },
      yAxis: { type: 'value' },
      series: [{
        name: '访问量',
        type: 'line',
        data: data.dailyVisits,
        smooth: true,
        itemStyle: { color: '#f56c6c' },
        areaStyle: { color: 'rgba(245, 108, 108, 0.2)' }
      }]
    }
    if (charts.visitTrend) charts.visitTrend.dispose()
    charts.visitTrend = initChart(visitTrendChartRef, option)
  } catch (e) {
    console.error('获取访问数据失败', e)
  }
}

async function fetchSourceData() {
  try {
    const data = await getUserSource()
    const sourceData = Object.entries(data.sourceDistribution || {}).map(([name, value]) => ({
      name,
      value
    }))
    const option = {
      title: { text: '用户来源分布', left: 'center' },
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        data: sourceData
      }]
    }
    if (charts.source) charts.source.dispose()
    charts.source = initChart(sourceChartRef, option)
  } catch (e) {
    console.error('获取来源数据失败', e)
  }
}

async function fetchConversionData() {
  try {
    const data = await getConversionRate(conversionDays.value)
    conversionRate.value = data
    const option = {
      title: { text: '转化率趋势', left: 'center' },
      tooltip: { trigger: 'axis', formatter: '{b}<br/>{a}: {c}%' },
      xAxis: { type: 'category', data: data.dates },
      yAxis: { type: 'value', name: '转化率(%)', max: 100 },
      series: [{
        name: '转化率',
        type: 'line',
        data: data.dailyConversionRates,
        smooth: true,
        itemStyle: { color: '#67c23a' },
        areaStyle: { color: 'rgba(103, 194, 58, 0.2)' }
      }]
    }
    if (charts.conversion) charts.conversion.dispose()
    charts.conversion = initChart(conversionChartRef, option)
  } catch (e) {
    console.error('获取转化率数据失败', e)
  }
}

async function fetchStudentAnalysisData() {
  try {
    const data = await getStudentAnalysis()
    studentAnalysis.value = data
    
    // 学校分布
    const schoolData = Object.entries(data.schoolDistribution || {}).map(([name, value]) => ({ name, value }))
    const schoolOption = {
      title: { text: '学生学校分布', left: 'center' },
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: schoolData.map(i => i.name), axisLabel: { rotate: 45 } },
      yAxis: { type: 'value' },
      series: [{ data: schoolData.map(i => i.value), type: 'bar', itemStyle: { color: '#409eff' } }],
      toolbox: { feature: { saveAsImage: {} } }
    }
    if (charts.school) charts.school.dispose()
    charts.school = initChart(schoolDistributionChartRef, schoolOption)
    
    // 年级分布
    const gradeData = Object.entries(data.gradeDistribution || {}).map(([name, value]) => ({ name, value }))
    const gradeOption = {
      title: { text: '学生年级分布', left: 'center' },
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: gradeData.map(i => i.name) },
      yAxis: { type: 'value' },
      series: [{ data: gradeData.map(i => i.value), type: 'bar', itemStyle: { color: '#67c23a' } }],
      toolbox: { feature: { saveAsImage: {} } }
    }
    if (charts.grade) charts.grade.dispose()
    charts.grade = initChart(gradeDistributionChartRef, gradeOption)
    
    // 消费能力
    const consumptionData = Object.entries(data.consumptionDistribution || {}).map(([name, value]) => ({ name, value }))
    const consumptionOption = {
      title: { text: '学生消费能力分布', left: 'center' },
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie',
        radius: '60%',
        data: consumptionData
      }],
      toolbox: { feature: { saveAsImage: {} } }
    }
    if (charts.consumption) charts.consumption.dispose()
    charts.consumption = initChart(consumptionChartRef, consumptionOption)
  } catch (e) {
    console.error('获取学生分析数据失败', e)
  }
}

async function fetchStudentHotDestinationsData() {
  try {
    const data = await getStudentHotDestinations()
    const option = {
      title: { text: '学生热门目的地TOP10', left: 'center' },
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: data.destinations, axisLabel: { rotate: 45 } },
      yAxis: { type: 'value' },
      series: [{ data: data.counts, type: 'bar', itemStyle: { color: '#e6a23c' } }],
      toolbox: { feature: { saveAsImage: {} } }
    }
    if (charts.destinations) charts.destinations.dispose()
    charts.destinations = initChart(hotDestinationsChartRef, option)
  } catch (e) {
    console.error('获取热门目的地失败', e)
  }
}

async function fetchGroupTravelTrendData() {
  try {
    const data = await getGroupTravelTrend(groupTrendDays.value)
    const option = {
      title: { text: '拼团热度趋势', left: 'center' },
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: data.dates },
      yAxis: { type: 'value' },
      series: [{
        data: data.newGroups,
        type: 'line',
        smooth: true,
        itemStyle: { color: '#f56c6c' },
        areaStyle: { color: 'rgba(245, 108, 108, 0.2)' }
      }],
      toolbox: { feature: { saveAsImage: {} } }
    }
    if (charts.groupTrend) charts.groupTrend.dispose()
    charts.groupTrend = initChart(groupTrendChartRef, option)
  } catch (e) {
    console.error('获取拼团趋势失败', e)
  }
}

function handleTabChange(tab) {
  nextTick(() => {
    if (tab === 'sales') {
      fetchSalesData()
      fetchHotLines()
      fetchPurchaseBehavior()
    } else if (tab === 'users') {
      fetchUserData()
      fetchActivityData()
      fetchUserProfile()
    } else if (tab === 'traffic') {
      fetchVisitData()
      fetchSourceData()
      fetchConversionData()
    } else if (tab === 'student') {
      fetchStudentAnalysisData()
      fetchStudentHotDestinationsData()
      fetchGroupTravelTrendData()
    }
  })
}

onMounted(() => {
  fetchSalesData()
  fetchHotLines()
  fetchPurchaseBehavior()
})

onBeforeUnmount(() => {
  Object.values(charts).forEach(chart => {
    if (chart) chart.dispose()
  })
})
</script>

<style lang="scss" scoped>
.statistics-page {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-box {
  text-align: center;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;

  .stat-value {
    font-size: 32px;
    font-weight: bold;
    color: #409eff;
    margin-bottom: 10px;
  }

  .stat-label {
    font-size: 14px;
    color: #666;
  }
}
</style>
