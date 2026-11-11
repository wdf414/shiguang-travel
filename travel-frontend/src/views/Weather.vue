<template>
  <div class="weather-container">
    <div class="weather-card">
      <h2 class="title">实时天气查询</h2>
      
      <el-form :model="form" class="weather-form" label-width="90px">
        <el-form-item label="热门城市">
          <el-select
            v-model="selectedHotSpotName"
            placeholder="请选择热门城市"
            @change="handleHotSpotChange"
            style="width: 100%"
          >
            <el-option
              v-for="city in hotSpots"
              :key="city.name"
              :label="city.name"
              :value="city.name"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="纬度">
          <el-input v-model="form.latitude" placeholder="例如: 31.2304" />
        </el-form-item>
        <el-form-item label="经度">
          <el-input v-model="form.longitude" placeholder="例如: 121.4737" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="getWeather" :loading="loading">查询天气</el-button>
        </el-form-item>
      </el-form>

      <div v-if="weather" class="weather-info">
        <el-divider>查询结果</el-divider>
        <div class="info-item" v-if="locationName">
          <span class="label">位置:</span>
          <span class="value">{{ locationName }}</span>
        </div>
        <div class="info-item">
          <span class="label">当前温度:</span>
          <span class="value">{{ weather.temperature }}°C</span>
        </div>
        <div class="info-item" v-if="weatherText">
          <span class="label">天气现象:</span>
          <span class="value">{{ weatherText }}</span>
        </div>
        <div class="info-item" v-if="humidity !== null">
          <span class="label">相对湿度:</span>
          <span class="value">{{ humidity }}%</span>
        </div>
        <div class="info-item">
          <span class="label">风速:</span>
          <span class="value">{{ weather.windspeed }} km/h</span>
        </div>
        <div class="info-item">
          <span class="label">风向:</span>
          <span class="value">{{ weather.winddirection }}°</span>
        </div>
        <div class="info-item">
          <span class="label">更新时间:</span>
          <span class="value">{{ formatTime(weather.time) }}</span>
        </div>

        <div class="chart-panel" v-if="chartHours > 0">
          <el-divider>未来7天（逐小时）温度 / 湿度趋势</el-divider>
          <div ref="trendChartRef" class="trend-chart"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick, onBeforeUnmount, watch } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import * as echarts from 'echarts'

const loading = ref(false)
const weather = ref(null)
const humidity = ref(null)
const locationName = ref('')
const weatherText = ref('')
const chartHours = ref(0)
const trendChartRef = ref()

const hotSpots = [
  { name: '北京', lat: 39.9042, lon: 116.4074 },
  { name: '西安', lat: 34.3416, lon: 108.9398 },
  { name: '杭州', lat: 30.2741, lon: 120.1551 },
  { name: '成都', lat: 30.5728, lon: 104.0668 },
  { name: '桂林', lat: 25.2736, lon: 110.2900 },
  { name: '苏州', lat: 31.2989, lon: 120.5853 },
  { name: '三亚', lat: 18.2528, lon: 109.5119 },
  { name: '拉萨', lat: 29.6520, lon: 91.1721 },
  { name: '上海', lat: 31.2304, lon: 121.4737 },
  { name: '重庆', lat: 29.5630, lon: 106.5516 },
  { name: '南京', lat: 32.0603, lon: 118.7969 },
  { name: '丽江', lat: 26.8721, lon: 100.2382 },
  { name: '青岛', lat: 36.0671, lon: 120.3826 },
  { name: '厦门', lat: 24.4798, lon: 118.0894 }
]

const selectedHotSpotName = ref('')

let trendChart = null

const form = reactive({
  latitude: '31.2304',
  longitude: '121.4737'
})

const findMatchedHotSpot = (lat, lon) => {
  return hotSpots.find(
    (c) => Math.abs(c.lat - lat) < 0.0001 && Math.abs(c.lon - lon) < 0.0001
  ) || null
}

watch(
  () => [form.latitude, form.longitude],
  ([latStr, lonStr]) => {
    const lat = parseFloat(latStr)
    const lon = parseFloat(lonStr)
    if (Number.isNaN(lat) || Number.isNaN(lon)) {
      selectedHotSpotName.value = ''
      return
    }
    const matched = findMatchedHotSpot(lat, lon)
    selectedHotSpotName.value = matched ? matched.name : ''
  }
)

const getWeather = async () => {
  if (!form.latitude || !form.longitude) {
    ElMessage.warning('请输入经纬度')
    return
  }

  const lat = parseFloat(form.latitude)
  const lon = parseFloat(form.longitude)

  if (Number.isNaN(lat) || Number.isNaN(lon)) {
    ElMessage.warning('经纬度必须是数字')
    return
  }

  if (lat < -90 || lat > 90) {
    ElMessage.warning('纬度必须在 -90 到 90 之间，请检查是否填反')
    return
  }

  if (lon < -180 || lon > 180) {
    ElMessage.warning('经度必须在 -180 到 180 之间，请检查是否填反')
    return
  }

  const matched = findMatchedHotSpot(lat, lon)
  selectedHotSpotName.value = matched ? matched.name : ''

  loading.value = true
  weather.value = null
  humidity.value = null
  locationName.value = ''
  weatherText.value = ''
  chartHours.value = 0
  
  try {
    const [weatherRes, locationRes] = await Promise.allSettled([
      axios.get('https://api.open-meteo.com/v1/forecast', {
        params: {
          latitude: lat,
          longitude: lon,
          current_weather: true,
          hourly: 'temperature_2m,relativehumidity_2m'
        }
      }),
      axios.get('https://api.bigdatacloud.net/data/reverse-geocode-client', {
        params: {
          latitude: lat,
          longitude: lon,
          localityLanguage: 'zh'
        }
      })
    ])
    
    // 处理天气数据
    if (weatherRes.status === 'fulfilled' && weatherRes.value.data && weatherRes.value.data.current_weather) {
      const weatherData = weatherRes.value.data
      weather.value = weatherData.current_weather
      weatherText.value = getWeatherText(weatherData.current_weather.weathercode)

      const hourly = weatherData.hourly

      if (hourly && Array.isArray(hourly.time) && Array.isArray(hourly.relativehumidity_2m)) {
        const currentTime = weatherData.current_weather.time
        const currentHourPrefix = currentTime.slice(0, 13)

        let index = hourly.time.findIndex(t => t === currentTime)

        if (index === -1) {
          index = hourly.time.findIndex(t => t.slice(0, 13) === currentHourPrefix)
        }

        if (index !== -1) {
          humidity.value = hourly.relativehumidity_2m[index]
        }
      }

      if (hourly && Array.isArray(hourly.time) && Array.isArray(hourly.temperature_2m) && Array.isArray(hourly.relativehumidity_2m)) {
        const maxLen = Math.min(
          hourly.time.length,
          hourly.temperature_2m.length,
          hourly.relativehumidity_2m.length
        )

        if (maxLen > 0) {
          const times = hourly.time.slice(0, maxLen)
          const temps = hourly.temperature_2m.slice(0, maxLen)
          const hums = hourly.relativehumidity_2m.slice(0, maxLen)

          chartHours.value = maxLen
          await nextTick()
          initTrendChart(times, temps, hums)
        }
      }
    } else {
      throw new Error('天气获取失败')
    }

    // 处理位置数据
    if (locationRes.status === 'fulfilled' && locationRes.value.data) {
      const data = locationRes.value.data
      const adminList = (data.localityInfo && data.localityInfo.administrative) || []

      const provinceObj = adminList.find(item => item.adminLevel === 4)
      const cityObj = adminList.find(item => item.adminLevel === 5)
      const districtObj = adminList.find(item => item.adminLevel === 6)
      const townObj = adminList.find(item => item.adminLevel === 8 || item.adminLevel === 9)

      const parts = []

      if (provinceObj?.name || data.principalSubdivision) {
        parts.push(provinceObj?.name || data.principalSubdivision)
      }

      if (cityObj?.name || data.city) {
        parts.push(cityObj?.name || data.city)
      }

      if (districtObj?.name || data.locality) {
        parts.push(districtObj?.name || data.locality)
      }

      if (townObj?.name) {
        parts.push(townObj.name)
      }

      const fullLocation = parts.join('')
      const finalLocation = fullLocation || data.city || data.locality || data.principalSubdivision || '未知位置'

      locationName.value = toSimplifiedChinese(finalLocation)
    }

    ElMessage.success('查询成功')
  } catch (error) {
    console.error(error)
    ElMessage.error('查询失败，请检查网络或坐标')
  } finally {
    loading.value = false
  }
}


const handleHotSpotChange = (cityName) => {
  const city = hotSpots.find(c => c.name === cityName)
  if (!city) return
  form.latitude = String(city.lat)
  form.longitude = String(city.lon)
  getWeather()
}

const formatTime = (isoString) => {
  return new Date(isoString).toLocaleString()
}

const getWeatherText = (code) => {
  const map = {
    0: '晴',
    1: '大致晴朗',
    2: '多云',
    3: '阴',
    45: '有雾',
    48: '雾凇雾',
    51: '毛毛雨：轻度',
    53: '毛毛雨：中度',
    55: '毛毛雨：高强度',
    56: '冻毛毛雨：轻度',
    57: '冻毛毛雨：高强度',
    61: '小雨',
    63: '中雨',
    65: '大雨',
    66: '冻雨：轻度',
    67: '冻雨：高强度',
    71: '小雪',
    73: '中雪',
    75: '大雪',
    77: '雪粒',
    80: '阵雨：轻度',
    81: '阵雨：中度',
    82: '阵雨：高强度',
    85: '阵雪：轻度',
    86: '阵雪：高强度',
    95: '雷阵雨：轻度或中度',
    96: '雷阵雨伴有小冰雹',
    99: '雷阵雨伴有大冰雹'
  }
  return map[code] || '未知天气'
}

const toSimplifiedChinese = (text) => {
  if (!text) return ''
  const map = {
    '亞': '亚',
    '歐': '欧',
    '美': '美',
    '非': '非',
    '澳': '澳',
    '紐': '纽',
    '約': '约',
    '舊': '旧',
    '金': '金',
    '門': '门',
    '灣': '湾',
    '倫': '伦',
    '敦': '敦',
    '羅': '罗',
    '馬': '马',
    '德': '德',
    '漢': '汉',
    '華': '华',
    '費': '费',
    '蘭': '兰',
    '蘇': '苏',
    '達': '达',
    '諾': '诺',
    '庫': '库',
    '縣': '县',
    '區': '区',
    '郡': '郡',
    '廣': '广',
    '東': '东',
    '西': '西',
    '南': '南',
    '北': '北',
    '臺': '台',
    '灣': '湾',
    '爾': '尔',
    '納': '纳'
  }
  return text.split('').map(ch => map[ch] || ch).join('')
}

const initTrendChart = (times, temps, hums) => {
  if (!trendChartRef.value) return

  if (trendChart) {
    trendChart.dispose()
  }

  trendChart = echarts.init(trendChartRef.value)
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['温度(°C)', '湿度(%)'] },
    xAxis: {
      type: 'category',
      name: '时间（未来7天，逐小时）',
      data: times.map(t => t.slice(5, 16))
    },
    yAxis: [
      { type: 'value', name: '温度(°C)' },
      { type: 'value', name: '湿度(%)', min: 0, max: 100 }
    ],
    series: [
      {
        name: '温度(°C)',
        type: 'line',
        smooth: true,
        data: temps
      },
      {
        name: '湿度(%)',
        type: 'line',
        smooth: true,
        yAxisIndex: 1,
        data: hums
      }
    ]
  })
}

onBeforeUnmount(() => {
  if (trendChart) {
    try {
      trendChart.dispose()
    } catch (e) {
    }
  }
})
</script>

<style scoped>
.weather-container {
  padding: 20px;
  display: flex;
  justify-content: center;
  margin-top: 40px;
}

.weather-card {
  width: 100%;
  max-width: 500px;
  padding: 30px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.title {
  text-align: center;
  margin-bottom: 30px;
  color: #303133;
}

.weather-info {
  margin-top: 30px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.label {
  color: #606266;
  font-weight: 500;
}

.value {
  color: #409EFF;
  font-weight: bold;
}

.chart-panel {
  margin-top: 30px;
}

.trend-chart {
  width: 100%;
  height: 300px;
}
</style>
