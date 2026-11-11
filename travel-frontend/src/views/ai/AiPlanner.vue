<template>
  <div class="ai-planner-page">
    <section class="ai-hero">
      <div class="hero-copy">
        <div class="hero-badge">
          <el-icon><MagicStick /></el-icon>
          DeepSeek 驱动 · 平台数据增强
        </div>
        <h1>AI 行程规划师</h1>
        <p class="hero-subtitle">
          输入目的地、天数和预算，AI 会检索平台在售线路、酒店与景点，
          生成一份可执行、可跳转预订的逐日行程。
        </p>
        <div class="hero-points">
          <span><el-icon><Connection /></el-icon> RAG 真实数据</span>
          <span><el-icon><Wallet /></el-icon> 学生预算优先</span>
          <span><el-icon><Timer /></el-icon> 30 秒生成</span>
        </div>
        <div class="health-line" v-if="health">
          <span class="status-dot" :class="{ ok: health.configured }"></span>
          {{ health.configured ? `${health.model} 已连接` : 'AI 未配置，当前只能使用本地兜底' }}
        </div>
      </div>
      <div class="hero-visual" aria-hidden="true">
        <div class="orbit orbit-one"></div>
        <div class="orbit orbit-two"></div>
        <div class="hero-card card-a">
          <el-icon><Location /></el-icon>
          <strong>桂林 · 3 天</strong>
          <span>山水与米粉</span>
        </div>
        <div class="hero-card card-b">
          <el-icon><House /></el-icon>
          <strong>¥168 起</strong>
          <span>学生友好酒店</span>
        </div>
        <div class="hero-card card-c">
          <el-icon><Sunny /></el-icon>
          <strong>AI 已规划</strong>
          <span>8 个安排</span>
        </div>
      </div>
    </section>

    <div class="planner-layout">
      <el-card class="form-card" shadow="never">
        <template #header>
          <div class="card-heading">
            <div>
              <h2>告诉我你的旅行计划</h2>
              <p>信息越具体，行程越贴合</p>
            </div>
            <el-tag effect="plain" type="success">学生友好</el-tag>
          </div>
        </template>

        <el-form :model="form" label-position="top" @submit.prevent="generate">
          <el-form-item label="目的地">
            <el-input
              v-model="form.destination"
              size="large"
              clearable
              placeholder="例如：桂林、大理、三亚"
              @keyup.enter="generate"
            >
              <template #prefix><el-icon><Location /></el-icon></template>
            </el-input>
            <div class="quick-tags">
              <el-tag
                v-for="city in popularDestinations"
                :key="city"
                effect="plain"
                round
                @click="form.destination = city"
              >
                {{ city }}
              </el-tag>
            </div>
          </el-form-item>

          <div class="form-row">
            <el-form-item label="行程天数">
              <el-input-number v-model="form.days" :min="1" :max="10" controls-position="right" />
            </el-form-item>
            <el-form-item label="出行人数">
              <el-input-number v-model="form.companions" :min="1" :max="20" controls-position="right" />
            </el-form-item>
          </div>

          <el-form-item label="人均预算（元）">
            <el-input-number
              v-model="form.budget"
              :min="0"
              :step="100"
              :precision="0"
              controls-position="right"
              class="full-width"
            />
          </el-form-item>

          <el-form-item label="出发城市">
            <el-input v-model="form.departureCity" placeholder="例如：南宁（可选）">
              <template #prefix><el-icon><Van /></el-icon></template>
            </el-input>
          </el-form-item>

          <el-form-item label="旅行偏好（可多选）">
            <el-checkbox-group v-model="form.preferences" class="preference-group">
              <el-checkbox-button v-for="item in preferenceOptions" :key="item" :label="item">
                {{ item }}
              </el-checkbox-button>
            </el-checkbox-group>
          </el-form-item>

          <el-form-item label="出行风格">
            <el-radio-group v-model="form.travelStyle" class="style-group">
              <el-radio-button label="学生轻旅行">学生轻旅行</el-radio-button>
              <el-radio-button label="舒适深度游">舒适深度游</el-radio-button>
              <el-radio-button label="摄影打卡">摄影打卡</el-radio-button>
            </el-radio-group>
          </el-form-item>

          <el-button type="primary" size="large" class="generate-btn" :loading="loading" @click="generate">
            <el-icon v-if="!loading"><MagicStick /></el-icon>
            {{ loading ? 'AI 正在规划...' : '生成我的行程' }}
          </el-button>
          <p class="disclaimer">
            AI 仅使用平台真实产品数据，生成结果为参考方案，价格与库存以下单页为准。
          </p>
        </el-form>
      </el-card>

      <div class="result-column">
        <el-card v-if="loading" class="loading-card" shadow="never">
          <div class="loading-orbit">
            <div class="loading-core"><el-icon><MagicStick /></el-icon></div>
            <div class="loading-ring"></div>
          </div>
          <h3>正在组合你的专属行程</h3>
          <p>{{ loadingStep }}</p>
          <el-progress :percentage="progress" :show-text="false" :stroke-width="6" color="#667eea" />
          <div class="loading-chips">
            <span>检索线路</span>
            <span>匹配酒店</span>
            <span>校验价格</span>
            <span>生成行程</span>
          </div>
        </el-card>

        <el-card v-else-if="!result" class="empty-card" shadow="never">
          <div class="empty-illustration">
            <el-icon><Compass /></el-icon>
          </div>
          <h3>下一趟旅行，从一句话开始</h3>
          <p>填写左侧信息，AI 会给你一份带真实产品引用的行程。</p>
          <div class="example-list">
            <span>试试：</span>
            <el-button
              v-for="example in examples"
              :key="example.label"
              size="small"
              round
              @click="applyExample(example)"
            >
              {{ example.label }}
            </el-button>
          </div>
        </el-card>

        <template v-else>
          <el-alert
            v-if="result.meta?.fallback"
            type="warning"
            :closable="false"
            show-icon
            title="当前为本地兜底结果"
            description="AI 服务暂时不可用，已使用平台数据生成本地参考行程，未调用大模型。"
            class="fallback-alert"
          />

          <el-card class="summary-card" shadow="never">
            <div class="summary-top">
              <div>
                <div class="summary-eyebrow">AI ITINERARY</div>
                <h2>{{ form.destination }} · {{ result.days }} 天行程</h2>
              </div>
              <div class="summary-tags">
                <el-tag v-if="result.meta?.cached" type="info" effect="plain">缓存命中</el-tag>
                <el-tag type="success" effect="plain">
                  {{ result.meta?.fallback ? '本地兜底' : (health?.model || 'AI 生成') }}
                </el-tag>
              </div>
            </div>
            <p class="summary-text">{{ result.summary }}</p>
            <div class="summary-meta">
              <span><el-icon><Stopwatch /></el-icon> {{ result.meta?.latencyMs || 0 }} ms</span>
              <span><el-icon><Collection /></el-icon> 参考 {{ result.meta?.groundedProducts || 0 }} 个平台产品</span>
              <span><el-icon><User /></el-icon> {{ form.companions }} 人出行</span>
            </div>
          </el-card>

          <el-card class="itinerary-card" shadow="never">
            <template #header>
              <div class="section-heading">
                <h3>逐日行程</h3>
                <el-button text @click="generate">
                  <el-icon><Refresh /></el-icon> 重新生成
                </el-button>
              </div>
            </template>
            <div class="day-list">
              <div v-for="day in result.itinerary" :key="day.day" class="day-block">
                <div class="day-heading">
                  <div class="day-number">D{{ day.day }}</div>
                  <div>
                    <h4>{{ day.title }}</h4>
                    <span>{{ day.theme }}</span>
                  </div>
                </div>
                <el-timeline class="day-timeline">
                  <el-timeline-item
                    v-for="(item, index) in day.items"
                    :key="index"
                    :timestamp="item.time"
                    placement="top"
                    :type="timelineType(item.type)"
                  >
                    <div class="plan-item">
                      <div class="plan-item-head">
                        <el-tag size="small" :type="tagType(item.type)" effect="light">
                          {{ typeLabel(item.type) }}
                        </el-tag>
                        <strong>{{ item.title }}</strong>
                      </div>
                      <p v-if="item.description">{{ item.description }}</p>
                      <div v-if="item.tip" class="item-tip">
                        <el-icon><InfoFilled /></el-icon> {{ item.tip }}
                      </div>
                    </div>
                  </el-timeline-item>
                </el-timeline>
              </div>
            </div>
          </el-card>

          <div class="two-column">
            <el-card class="budget-card" shadow="never">
              <template #header><h3>预算估算</h3></template>
              <div class="budget-total">
                <span>人均</span>
                <strong>¥{{ money(result.budget?.perPerson) }}</strong>
              </div>
              <div class="budget-list">
                <div v-for="row in budgetRows" :key="row.label" class="budget-row">
                  <span>{{ row.label }}</span>
                  <div class="budget-bar">
                    <i :style="{ width: row.percent + '%' }"></i>
                  </div>
                  <b>¥{{ money(row.value) }}</b>
                </div>
              </div>
              <p class="budget-note">总预算约 ¥{{ money(result.budget?.total) }}，按 {{ form.companions }} 人估算。</p>
            </el-card>

            <el-card class="tips-card" shadow="never">
              <template #header><h3>出行提示</h3></template>
              <ul class="tips-list">
                <li v-for="(tip, index) in result.tips" :key="index">
                  <el-icon><CircleCheck /></el-icon>
                  <span>{{ tip }}</span>
                </li>
              </ul>
            </el-card>
          </div>

          <el-card v-if="result.recommendedLines?.length" class="products-card" shadow="never">
            <template #header>
              <div class="section-heading">
                <h3>平台线路推荐</h3>
                <span class="section-hint">AI 从平台在售线路中挑选</span>
              </div>
            </template>
            <div class="product-grid">
              <div
                v-for="line in result.recommendedLines"
                :key="line.id"
                class="product-card"
                @click="goProduct(line)"
              >
                <img :src="line.image || fallbackImg" :alt="line.name" @error="onImgError" />
                <div class="product-info">
                  <h4>{{ line.name }}</h4>
                  <p>{{ line.subtitle }}</p>
                  <div class="product-bottom">
                    <span class="price">¥{{ money(line.price) }}</span>
                    <span class="link">查看详情 <el-icon><ArrowRight /></el-icon></span>
                  </div>
                </div>
              </div>
            </div>
          </el-card>

          <el-card v-if="result.recommendedHotels?.length" class="products-card" shadow="never">
            <template #header>
              <div class="section-heading">
                <h3>平台酒店推荐</h3>
                <span class="section-hint">按评分与价格匹配</span>
              </div>
            </template>
            <div class="product-grid">
              <div
                v-for="hotel in result.recommendedHotels"
                :key="hotel.id"
                class="product-card"
                @click="goProduct(hotel)"
              >
                <img :src="hotel.image || fallbackImg" :alt="hotel.name" @error="onImgError" />
                <div class="product-info">
                  <h4>{{ hotel.name }}</h4>
                  <p>{{ hotel.subtitle }}</p>
                  <div class="product-bottom">
                    <span class="price">¥{{ money(hotel.price) }} 起</span>
                    <span class="link">查看详情 <el-icon><ArrowRight /></el-icon></span>
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onBeforeUnmount, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getAiHealth, planTrip } from '@/api/ai'

const router = useRouter()
const route = useRoute()
const fallbackImg = 'https://picsum.photos/400/300?random=ai'

const form = reactive({
  destination: '',
  days: 3,
  companions: 2,
  budget: 1500,
  departureCity: '',
  preferences: [],
  travelStyle: '学生轻旅行'
})

const popularDestinations = ['桂林', '北海', '大理', '三亚', '成都', '西安']
const preferenceOptions = ['自然风光', '人文历史', '美食', '摄影', '海岛', '夜生活', '小众', '亲子']
const examples = [
  {
    label: '桂林 3 天 · 学生党',
    form: { destination: '桂林', days: 3, companions: 2, budget: 1200, departureCity: '南宁', preferences: ['自然风光', '美食'], travelStyle: '学生轻旅行' }
  },
  {
    label: '大理 4 天 · 摄影',
    form: { destination: '大理', days: 4, companions: 2, budget: 2000, departureCity: '昆明', preferences: ['摄影', '小众'], travelStyle: '摄影打卡' }
  },
  {
    label: '三亚 3 天 · 海岛',
    form: { destination: '三亚', days: 3, companions: 2, budget: 2500, departureCity: '', preferences: ['海岛', '美食'], travelStyle: '舒适深度游' }
  }
]

const loading = ref(false)
const result = ref(null)
const health = ref(null)
const progress = ref(8)
const loadingStep = ref('正在检索平台线路与酒店...')
let progressTimer = null

const loadingSteps = [
  '正在检索平台线路与酒店...',
  '正在匹配目的地景点...',
  '正在校验价格与库存...',
  '正在生成逐日行程...'
]

const budgetRows = computed(() => {
  const budget = result.value?.budget || {}
  const rows = [
    { label: '交通', value: budget.transport },
    { label: '住宿', value: budget.hotel },
    { label: '门票', value: budget.tickets },
    { label: '餐饮', value: budget.food },
    { label: '其他', value: budget.other }
  ]
  const max = Math.max(...rows.map(row => Number(row.value || 0)), 1)
  return rows.map(row => ({
    ...row,
    percent: Math.max(4, Math.round((Number(row.value || 0) / max) * 100))
  }))
})

function money(value) {
  const number = Number(value || 0)
  return Number.isInteger(number) ? number.toLocaleString('zh-CN') : number.toFixed(2)
}

function applyExample(example) {
  Object.assign(form, example.form)
  generate()
}

function startProgress() {
  progress.value = 8
  loadingStep.value = loadingSteps[0]
  let index = 0
  progressTimer = window.setInterval(() => {
    index = Math.min(index + 1, loadingSteps.length - 1)
    loadingStep.value = loadingSteps[index]
    progress.value = Math.min(92, progress.value + 18 + Math.random() * 10)
  }, 1400)
}

function stopProgress() {
  if (progressTimer) {
    window.clearInterval(progressTimer)
    progressTimer = null
  }
}

async function generate() {
  if (!form.destination.trim()) {
    ElMessage.warning('请先填写目的地')
    return
  }
  loading.value = true
  result.value = null
  startProgress()
  try {
    const data = await planTrip({
      destination: form.destination.trim(),
      days: form.days,
      budget: form.budget || null,
      departureCity: form.departureCity.trim() || null,
      travelStyle: form.travelStyle,
      preferences: form.preferences,
      companions: form.companions
    })
    result.value = data
    progress.value = 100
    ElMessage.success(data.meta?.fallback ? '已生成本地兜底行程' : 'AI 行程已生成')
  } catch (error) {
    ElMessage.error(error?.message || '生成失败，请稍后重试')
  } finally {
    stopProgress()
    loading.value = false
  }
}

function goProduct(product) {
  if (product?.url) {
    router.push(product.url)
  }
}

function onImgError(event) {
  event.target.src = fallbackImg
  event.target.onerror = null
}

function typeLabel(type) {
  return {
    transport: '交通',
    hotel: '住宿',
    scenic: '景点',
    food: '餐饮',
    free: '自由活动'
  }[type] || '安排'
}

function tagType(type) {
  return {
    transport: 'info',
    hotel: 'success',
    scenic: 'primary',
    food: 'warning',
    free: 'info'
  }[type] || 'info'
}

function timelineType(type) {
  return {
    transport: 'info',
    hotel: 'success',
    scenic: 'primary',
    food: 'warning',
    free: 'info'
  }[type] || 'primary'
}

onMounted(async () => {
  try {
    health.value = await getAiHealth()
  } catch {
    health.value = null
  }

  // 支持 /ai/planner?destination=三亚&auto=1 直接生成，便于演示和截图。
  if (route.query.destination) {
    form.destination = String(route.query.destination)
    if (route.query.days) form.days = Number(route.query.days)
    if (route.query.budget) form.budget = Number(route.query.budget)
    if (route.query.departureCity) form.departureCity = String(route.query.departureCity)
    if (route.query.travelStyle) form.travelStyle = String(route.query.travelStyle)
    if (route.query.preferences) {
      form.preferences = String(route.query.preferences).split(',').filter(Boolean)
    }
    if (route.query.companions) form.companions = Number(route.query.companions)
    if (route.query.auto === '1') {
      generate()
    }
  }
})

onBeforeUnmount(stopProgress)
</script>

<style lang="scss" scoped>
$primary: #667eea;
$primary-dark: #5a67d8;
$ink: #1a1a2e;
$muted: #718096;
$line: #e8ecf4;

.ai-planner-page {
  min-height: 100vh;
  padding: 24px 0 80px;
  background:
    radial-gradient(circle at 8% 8%, rgba(102, 126, 234, 0.12), transparent 30%),
    radial-gradient(circle at 92% 18%, rgba(240, 147, 251, 0.1), transparent 28%),
    #f7fafc;
}

.ai-hero {
  max-width: 1200px;
  margin: 0 auto 28px;
  padding: 44px 48px;
  border-radius: 24px;
  background: linear-gradient(125deg, #1f2455 0%, #3d3a8f 52%, #6d4aa8 100%);
  color: #fff;
  display: grid;
  grid-template-columns: 1.25fr 0.75fr;
  gap: 30px;
  overflow: hidden;
  position: relative;
  box-shadow: 0 24px 60px -30px rgba(31, 36, 85, 0.55);

  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background-image: linear-gradient(rgba(255,255,255,0.035) 1px, transparent 1px),
      linear-gradient(90deg, rgba(255,255,255,0.035) 1px, transparent 1px);
    background-size: 36px 36px;
    pointer-events: none;
  }
}

.hero-copy {
  position: relative;
  z-index: 2;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 7px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.2);
  font-size: 13px;
  letter-spacing: 0.2px;
}

.hero-copy h1 {
  margin: 20px 0 12px;
  font-size: 44px;
  line-height: 1.15;
  letter-spacing: -1px;
}

.hero-subtitle {
  max-width: 640px;
  margin: 0;
  color: rgba(255, 255, 255, 0.78);
  font-size: 16px;
  line-height: 1.8;
}

.hero-points {
  display: flex;
  flex-wrap: wrap;
  gap: 18px;
  margin-top: 24px;

  span {
    display: inline-flex;
    align-items: center;
    gap: 7px;
    color: rgba(255, 255, 255, 0.82);
    font-size: 13px;
  }
}

.health-line {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 20px;
  color: rgba(255, 255, 255, 0.66);
  font-size: 12px;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #f6ad55;
  box-shadow: 0 0 0 4px rgba(246, 173, 85, 0.14);

  &.ok {
    background: #48bb78;
    box-shadow: 0 0 0 4px rgba(72, 187, 120, 0.16);
  }
}

.hero-visual {
  position: relative;
  min-height: 240px;
}

.orbit {
  position: absolute;
  inset: 12% 4%;
  border: 1px solid rgba(255, 255, 255, 0.14);
  border-radius: 50%;
  transform: rotate(-16deg);

  &.orbit-two {
    inset: 24% 16%;
    transform: rotate(24deg);
    border-style: dashed;
  }
}

.hero-card {
  position: absolute;
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 13px 16px;
  min-width: 138px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.94);
  color: $ink;
  box-shadow: 0 18px 40px -20px rgba(0, 0, 0, 0.55);
  backdrop-filter: blur(8px);
  animation: floatCard 5s ease-in-out infinite;

  .el-icon {
    color: $primary;
    font-size: 18px;
    margin-bottom: 2px;
  }

  strong { font-size: 14px; }
  span { color: $muted; font-size: 11px; }

  &.card-a { top: 12%; left: 2%; }
  &.card-b { top: 44%; right: 0; animation-delay: 1.2s; }
  &.card-c { bottom: 4%; left: 16%; animation-delay: 2.4s; }
}

@keyframes floatCard {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-7px); }
}

.planner-layout {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 390px minmax(0, 1fr);
  gap: 24px;
  align-items: start;
}

.form-card {
  position: sticky;
  top: 84px;
  border-radius: 18px;
  border: 1px solid $line;
}

:deep(.el-card__header) {
  border-bottom: 1px solid $line;
}

.card-heading {
  display: flex;
  justify-content: space-between;
  gap: 14px;
  align-items: flex-start;

  h2 { margin: 0 0 4px; font-size: 18px; color: $ink; }
  p { margin: 0; color: $muted; font-size: 12px; }
}

.quick-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;

  .el-tag { cursor: pointer; }
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}

.full-width,
.form-row :deep(.el-input-number) {
  width: 100%;
}

.preference-group,
.style-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.generate-btn {
  width: 100%;
  height: 46px;
  margin-top: 6px;
  border: none;
  border-radius: 10px;
  background: linear-gradient(135deg, $primary 0%, #764ba2 100%);
  font-size: 15px;
  box-shadow: 0 10px 24px -12px rgba(102, 126, 234, 0.8);
}

.disclaimer {
  margin: 12px 0 0;
  color: #a0aec0;
  font-size: 11px;
  line-height: 1.6;
  text-align: center;
}

.result-column {
  display: flex;
  flex-direction: column;
  gap: 18px;
  min-width: 0;
}

.loading-card,
.empty-card {
  min-height: 420px;
  border-radius: 18px;
  border: 1px solid $line;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 40px;

  h3 { margin: 18px 0 8px; color: $ink; }
  p { margin: 0; color: $muted; }
}

.loading-orbit {
  position: relative;
  width: 92px;
  height: 92px;
  display: grid;
  place-items: center;
}

.loading-core {
  width: 58px;
  height: 58px;
  border-radius: 18px;
  background: linear-gradient(135deg, $primary, #764ba2);
  color: #fff;
  display: grid;
  place-items: center;
  font-size: 26px;
  box-shadow: 0 12px 30px -12px rgba(102, 126, 234, 0.9);
}

.loading-ring {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  border: 2px solid rgba(102, 126, 234, 0.16);
  border-top-color: $primary;
  animation: spin 1.1s linear infinite;
}

@keyframes spin { to { transform: rotate(360deg); } }

.loading-chips {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
  margin-top: 18px;

  span {
    padding: 5px 10px;
    border-radius: 999px;
    background: #f1f4ff;
    color: $primary-dark;
    font-size: 11px;
  }
}

.empty-illustration {
  width: 82px;
  height: 82px;
  border-radius: 26px;
  display: grid;
  place-items: center;
  font-size: 38px;
  color: $primary;
  background: linear-gradient(135deg, #eef1ff, #f7ecff);
}

.example-list {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
  margin-top: 22px;
  color: $muted;
  font-size: 12px;
}

.fallback-alert {
  border-radius: 12px;
}

.summary-card,
.itinerary-card,
.budget-card,
.tips-card,
.products-card {
  border-radius: 18px;
  border: 1px solid $line;
}

.summary-top {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  align-items: flex-start;
}

.summary-eyebrow {
  color: $primary;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 2px;
}

.summary-top h2 {
  margin: 4px 0 0;
  font-size: 25px;
  color: $ink;
}

.summary-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.summary-text {
  margin: 16px 0 0;
  color: #4a5568;
  line-height: 1.8;
}

.summary-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 18px;
  margin-top: 16px;
  padding-top: 14px;
  border-top: 1px dashed $line;
  color: $muted;
  font-size: 12px;

  span { display: inline-flex; align-items: center; gap: 5px; }
}

.section-heading {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;

  h3 { margin: 0; font-size: 17px; color: $ink; }
}

.section-hint {
  color: $muted;
  font-size: 12px;
}

.day-block + .day-block {
  margin-top: 12px;
  padding-top: 22px;
  border-top: 1px solid $line;
}

.day-heading {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 6px;

  h4 { margin: 0 0 2px; color: $ink; font-size: 16px; }
  span { color: $muted; font-size: 12px; }
}

.day-number {
  width: 42px;
  height: 42px;
  border-radius: 13px;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, #eef1ff, #f6edff);
  color: $primary-dark;
  font-weight: 700;
  font-size: 13px;
}

.day-timeline {
  padding-left: 4px;
  margin-top: 10px;
}

.plan-item {
  padding: 2px 0 4px;

  p { margin: 6px 0 0; color: #4a5568; font-size: 13px; line-height: 1.65; }
}

.plan-item-head {
  display: flex;
  align-items: center;
  gap: 8px;

  strong { color: $ink; font-size: 14px; }
}

.item-tip {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  margin-top: 8px;
  padding: 5px 9px;
  border-radius: 7px;
  background: #fffaf0;
  color: #b7791f;
  font-size: 11px;
}

.two-column {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18px;
}

.budget-total {
  display: flex;
  align-items: baseline;
  gap: 10px;
  margin-bottom: 18px;

  span { color: $muted; font-size: 12px; }
  strong { color: $ink; font-size: 30px; letter-spacing: -1px; }
}

.budget-list {
  display: flex;
  flex-direction: column;
  gap: 11px;
}

.budget-row {
  display: grid;
  grid-template-columns: 42px 1fr 64px;
  gap: 10px;
  align-items: center;

  span { color: $muted; font-size: 12px; }
  b { color: #4a5568; font-size: 12px; text-align: right; }
}

.budget-bar {
  height: 7px;
  border-radius: 999px;
  background: #edf1f7;
  overflow: hidden;

  i {
    display: block;
    height: 100%;
    border-radius: inherit;
    background: linear-gradient(90deg, $primary, #a78bfa);
  }
}

.budget-note {
  margin: 18px 0 0;
  color: #a0aec0;
  font-size: 11px;
}

.tips-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;

  li {
    display: flex;
    gap: 8px;
    color: #4a5568;
    font-size: 13px;
    line-height: 1.65;
  }

  .el-icon {
    color: #38a169;
    flex-shrink: 0;
    margin-top: 3px;
  }
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 14px;
}

.product-card {
  border: 1px solid $line;
  border-radius: 13px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  background: #fff;

  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 16px 30px -22px rgba(26, 26, 46, 0.5);
  }

  img {
    width: 100%;
    height: 120px;
    object-fit: cover;
    display: block;
    background: #edf1f7;
  }
}

.product-info {
  padding: 12px;

  h4 {
    margin: 0 0 4px;
    color: $ink;
    font-size: 14px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  p {
    margin: 0 0 10px;
    color: $muted;
    font-size: 11px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}

.product-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .price { color: #e53e3e; font-size: 15px; font-weight: 700; }
  .link { display: inline-flex; align-items: center; gap: 3px; color: $primary; font-size: 11px; }
}

@media (max-width: 1024px) {
  .ai-hero { grid-template-columns: 1fr; }
  .hero-visual { display: none; }
  .planner-layout { grid-template-columns: 1fr; }
  .form-card { position: static; }
}

@media (max-width: 640px) {
  .ai-planner-page { padding-top: 12px; }
  .ai-hero { padding: 28px 22px; border-radius: 18px; }
  .hero-copy h1 { font-size: 32px; }
  .two-column { grid-template-columns: 1fr; }
  .summary-top { flex-direction: column; }
  .summary-tags { justify-content: flex-start; }
  .form-row { grid-template-columns: 1fr; }
}
</style>
