<template>
  <div class="price-manage">
    <el-card>
      <template #header>
        <span>价格管理</span>
      </template>

      <!-- 批量调价 -->
      <el-card style="margin-bottom: 20px;">
        <template #header>
          <span>批量调价</span>
        </template>
        <el-form :model="batchForm" label-width="120px">
          <el-form-item label="选择线路">
            <el-select v-model="batchForm.lineIds" multiple placeholder="请选择要调价的线路" style="width: 100%">
              <el-option
                v-for="line in lineList"
                :key="line.id"
                :label="line.title"
                :value="line.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="调价方式">
            <el-radio-group v-model="batchForm.adjustType">
            <el-radio value="fixed">固定价格</el-radio>
            <el-radio value="add">增加金额</el-radio>
            <el-radio value="subtract">减少金额</el-radio>
            <el-radio value="percent">按百分比调整</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="调整值">
            <el-input-number
              v-model="batchForm.adjustValue"
              :precision="2"
              :min="0"
              :step="10"
              style="width: 200px"
            />
            <span style="margin-left: 10px; color: #999;">
              {{ batchForm.adjustType === 'percent' ? '%' : '元' }}
            </span>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleBatchAdjust" :loading="batchLoading">
              执行批量调价
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 旺季/淡季价格设置 -->
      <el-card style="margin-bottom: 20px;">
        <template #header>
          <span>旺季/淡季价格设置</span>
        </template>
        <el-form :model="seasonForm" label-width="120px">
          <el-form-item label="选择线路">
            <el-select v-model="seasonForm.lineId" placeholder="请选择线路" style="width: 300px">
              <el-option
                v-for="line in lineList"
                :key="line.id"
                :label="line.title"
                :value="line.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="季节类型">
            <el-radio-group v-model="seasonForm.season">
            <el-radio value="peak">旺季</el-radio>
            <el-radio value="off">淡季</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="价格">
            <el-input-number
              v-model="seasonForm.price"
              :precision="2"
              :min="0"
              :step="10"
              style="width: 200px"
            />
            <span style="margin-left: 10px;">元</span>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSetSeasonPrice" :loading="seasonLoading">
              设置价格
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 动态价格调整 -->
      <el-card>
        <template #header>
          <span>动态价格调整</span>
        </template>
        <el-form :model="dynamicForm" label-width="120px">
          <el-form-item label="选择线路">
            <el-select v-model="dynamicForm.lineId" placeholder="请选择线路" style="width: 300px">
              <el-option
                v-for="line in lineList"
                :key="line.id"
                :label="line.title"
                :value="line.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="调整因素">
            <el-checkbox-group v-model="dynamicForm.factors">
              <el-checkbox value="stockFactor" label="根据库存调整">根据库存调整</el-checkbox>
              <el-checkbox value="salesFactor" label="根据销量调整">根据销量调整</el-checkbox>
              <el-checkbox value="seasonFactor" label="根据季节调整">根据季节调整</el-checkbox>
            </el-checkbox-group>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleDynamicAdjust" :loading="dynamicLoading">
              执行动态调价
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 线路列表 -->
      <el-card style="margin-top: 20px;">
        <template #header>
          <span>线路价格列表</span>
        </template>
        <el-table :data="lineList" v-loading="loading">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="title" label="线路名称" min-width="200" />
          <el-table-column prop="price" label="当前价格" width="120">
            <template #default="{ row }">¥{{ row.price }}</template>
          </el-table-column>
          <el-table-column prop="stock" label="库存" width="80" />
          <el-table-column prop="sales" label="销量" width="80" />
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button type="primary" link @click="viewPriceHistory(row)">查看历史</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 价格历史对话框 -->
      <el-dialog v-model="historyVisible" title="价格历史" width="500px">
        <el-descriptions :column="1" border v-if="priceHistory">
          <el-descriptions-item label="当前价格">
            ¥{{ priceHistory.currentPrice || 0 }}
          </el-descriptions-item>
          <el-descriptions-item label="最后更新时间">
            {{ priceHistory.lastUpdate || '-' }}
          </el-descriptions-item>
        </el-descriptions>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAdminLineList } from '@/api/admin'
import { batchAdjustPrice, setSeasonPrice, dynamicPriceAdjust, getPriceHistory } from '@/api/admin'

const loading = ref(false)
const batchLoading = ref(false)
const seasonLoading = ref(false)
const dynamicLoading = ref(false)
const lineList = ref([])

const batchForm = reactive({
  lineIds: [],
  adjustType: 'percent',
  adjustValue: 10
})

const seasonForm = reactive({
  lineId: null,
  season: 'peak',
  price: 0
})

const dynamicForm = reactive({
  lineId: null,
  factors: ['stockFactor', 'salesFactor', 'seasonFactor']
})

const historyVisible = ref(false)
const priceHistory = ref(null)

async function fetchLines() {
  loading.value = true
  try {
    const data = await getAdminLineList({ page: 1, size: 1000 })
    lineList.value = data.records || []
  } catch (e) {
    ElMessage.error('加载线路列表失败')
  } finally {
    loading.value = false
  }
}

async function handleBatchAdjust() {
  if (batchForm.lineIds.length === 0) {
    ElMessage.warning('请选择要调价的线路')
    return
  }
  if (!batchForm.adjustValue || batchForm.adjustValue <= 0) {
    ElMessage.warning('请输入有效的调整值')
    return
  }

  batchLoading.value = true
  try {
    await batchAdjustPrice({
      lineIds: batchForm.lineIds,
      adjustType: batchForm.adjustType,
      adjustValue: batchForm.adjustValue
    })
    ElMessage.success('批量调价成功')
    fetchLines()
  } catch (e) {
    ElMessage.error('批量调价失败')
  } finally {
    batchLoading.value = false
  }
}

async function handleSetSeasonPrice() {
  if (!seasonForm.lineId) {
    ElMessage.warning('请选择线路')
    return
  }
  if (!seasonForm.price || seasonForm.price <= 0) {
    ElMessage.warning('请输入有效的价格')
    return
  }

  seasonLoading.value = true
  try {
    await setSeasonPrice({
      lineId: seasonForm.lineId,
      season: seasonForm.season,
      price: seasonForm.price
    })
    ElMessage.success('设置成功')
    fetchLines()
  } catch (e) {
    ElMessage.error('设置失败')
  } finally {
    seasonLoading.value = false
  }
}

async function handleDynamicAdjust() {
  if (!dynamicForm.lineId) {
    ElMessage.warning('请选择线路')
    return
  }
  if (dynamicForm.factors.length === 0) {
    ElMessage.warning('请至少选择一个调整因素')
    return
  }

  dynamicLoading.value = true
  try {
    const factors = {}
    dynamicForm.factors.forEach(factor => {
      factors[factor] = true
    })
    await dynamicPriceAdjust(dynamicForm.lineId, factors)
    ElMessage.success('动态调价成功')
    fetchLines()
  } catch (e) {
    ElMessage.error('动态调价失败')
  } finally {
    dynamicLoading.value = false
  }
}

async function viewPriceHistory(row) {
  try {
    const data = await getPriceHistory(row.id)
    priceHistory.value = data
    historyVisible.value = true
  } catch (e) {
    ElMessage.error('获取价格历史失败')
  }
}

onMounted(fetchLines)
</script>

<style lang="scss" scoped>
.price-manage {
  padding: 20px;
}
</style>
