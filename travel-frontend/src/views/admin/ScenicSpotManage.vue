<template>
  <div class="scenic-spot-manage">
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="景点名称/地址" clearable />
        </el-form-item>
        <el-form-item label="城市">
          <el-input v-model="searchForm.city" placeholder="所在城市" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
          <el-button type="success" @click="handleAdd">新增景点</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="景点名称" min-width="150" />
        <el-table-column prop="province" label="省份" width="100" />
        <el-table-column prop="city" label="城市" width="100" />
        <el-table-column prop="district" label="区/县" width="100" />
        <el-table-column prop="type" label="类型" width="120" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="handleDetail(scope.row)">详情</el-button>
            <el-button size="small" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 编辑/新增对话框 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form :model="form" label-width="100px" :disabled="isDetail">
        <el-form-item label="景点名称" required>
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="省份">
          <el-input v-model="form.province" />
        </el-form-item>
        <el-form-item label="城市">
          <el-input v-model="form.city" />
        </el-form-item>
        <el-form-item label="区/县">
          <el-input v-model="form.district" />
        </el-form-item>
        <el-form-item label="详细地址">
          <el-input v-model="form.address" />
        </el-form-item>
        <el-row>
          <el-col :span="12">
            <el-form-item label="经度">
              <el-input v-model="form.longitude" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="纬度">
              <el-input v-model="form.latitude" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="类型">
          <el-input v-model="form.type" />
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="form.tag" />
        </el-form-item>
        <el-form-item label="搜索城市">
          <el-input v-model="form.searchCity" />
        </el-form-item>
        
        <!-- 详情模式下显示照片 -->
        <el-form-item label="景点照片" v-if="form.name">
           <div class="image-preview">
             <el-image 
               style="width: 200px; height: 150px"
               :src="getImageUrl(form.name)"
               :preview-src-list="[getImageUrl(form.name)]"
               fit="cover"
             >
               <template #error>
                 <div class="image-slot">
                   <el-icon><Picture /></el-icon>
                   <span>暂无照片</span>
                 </div>
               </template>
             </el-image>
             <div class="image-tip" v-if="!isDetail">照片根据景点名称自动匹配</div>
           </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="handleSubmit" v-if="!isDetail">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import { getAdminScenicSpotPage, createScenicSpot, updateScenicSpot, deleteScenicSpot } from '@/api/admin'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const searchForm = reactive({
  keyword: '',
  city: ''
})

const dialogVisible = ref(false)
const dialogTitle = ref('')
const isDetail = ref(false)
const form = reactive({
  id: null,
  name: '',
  province: '',
  city: '',
  district: '',
  address: '',
  latitude: '',
  longitude: '',
  tag: '',
  type: '',
  searchCity: ''
})

onMounted(() => {
  fetchData()
})

async function fetchData() {
  loading.value = true
  try {
    const res = await getAdminScenicSpotPage({
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchForm.keyword,
      city: searchForm.city
    })
    tableData.value = res.records
    total.value = res.total
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  currentPage.value = 1
  fetchData()
}

function resetSearch() {
  searchForm.keyword = ''
  searchForm.city = ''
  handleSearch()
}

function handleSizeChange(val) {
  pageSize.value = val
  fetchData()
}

function handleCurrentChange(val) {
  currentPage.value = val
  fetchData()
}

function getImageUrl(name) {
  if (!name) return ''
  return `/api/scenic-spot/photo?name=${encodeURIComponent(name)}`
}

function handleAdd() {
  dialogTitle.value = '新增景点'
  isDetail.value = false
  Object.keys(form).forEach(key => form[key] = '')
  delete form.id
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑景点'
  isDetail.value = false
  Object.assign(form, row)
  dialogVisible.value = true
}

function handleDetail(row) {
  dialogTitle.value = '景点详情'
  isDetail.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!form.name) {
    ElMessage.warning('请输入景点名称')
    return
  }
  
  try {
    if (form.id) {
      await updateScenicSpot(form)
      ElMessage.success('更新成功')
    } else {
      await createScenicSpot(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (error) {
    console.error(error)
  }
}

function handleDelete(row) {
  ElMessageBox.confirm('确定要删除该景点吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await deleteScenicSpot(row.id)
      ElMessage.success('删除成功')
      fetchData()
    } catch (error) {
      console.error(error)
    }
  })
}

function handleDialogClose() {
  dialogVisible.value = false
}
</script>

<style scoped>
.scenic-spot-manage {
  padding: 20px;
}
.search-card {
  margin-bottom: 20px;
}
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
.image-slot {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  color: #909399;
}
.image-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}
</style>
