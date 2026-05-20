<script setup>
// 失物招领列表页：展示所有失物 / 招领信息，支持按类型筛选（全部 / 寻物 / 招领）
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../api'

const router = useRouter()
// 失物招领列表数据
const items = ref([])
const loading = ref(false)
const filterType = ref('')
const total = ref(0)
const page = ref(1)
const keyword = ref('')

// 获取失物招领列表（分页 + 类型筛选）
async function fetchItems() {
  loading.value = true
  try {
    const params = { page: page.value - 1, size: 20 }
    if (filterType.value) params.type = filterType.value
    if (keyword.value.trim()) params.keyword = keyword.value
    const res = await request.get('/lost-found', { params })
    items.value = res.data.content
    total.value = res.data.totalElements
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  fetchItems()
}

// 格式化时间戳为中文本地时间字符串
function formatTime(t) {
  return new Date(t).toLocaleString('zh-CN')
}

onMounted(fetchItems)
</script>

<template>
  <div>
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; flex-wrap: wrap; gap: 12px;">
      <div>
        <h2 style="margin: 0 0 4px; font-size: 20px;">失物招领</h2>
        <p style="margin: 0; color: #909399; font-size: 13px;">发布丢失或捡到的物品信息</p>
      </div>
      <el-button type="primary" @click="router.push('/lost-found/create')" style="border-radius: 8px;">+ 发布信息</el-button>
    </div>
    <div style="margin-bottom: 20px;">
      <el-radio-group v-model="filterType" @change="fetchItems">
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button label="LOST">寻物</el-radio-button>
        <el-radio-button label="FOUND">招领</el-radio-button>
      </el-radio-group>
      <div style="display: inline-flex; gap: 8px; margin-left: 16px;">
        <el-input v-model="keyword" placeholder="搜索标题..." clearable @keyup.enter="handleSearch" style="width: 200px;" />
        <el-button @click="handleSearch" style="border-radius: 8px;">搜索</el-button>
      </div>
    </div>
    <div v-loading="loading">
      <div v-if="items.length === 0 && !loading" style="text-align: center; padding: 60px 0; color: #909399;">
        <div style="font-size: 40px; margin-bottom: 12px;">🔍</div>
        <p style="font-size: 15px;">暂无记录</p>
        <el-button type="primary" plain @click="router.push('/lost-found/create')">发布第一条信息</el-button>
      </div>
      <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 16px;">
        <div v-for="item in items" :key="item.id" style="background: #fff; border-radius: 12px; border: 1px solid #f0f0f0; padding: 20px; cursor: pointer; transition: all 0.2s;" @click="router.push(`/lost-found/${item.id}`)" @mouseenter="$event.currentTarget.style.borderColor='#1a73e8'; $event.currentTarget.style.boxShadow='0 4px 12px rgba(26,115,232,0.1)'" @mouseleave="$event.currentTarget.style.borderColor='#f0f0f0'; $event.currentTarget.style.boxShadow='none'">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px;">
            <div style="display: flex; gap: 6px; align-items: center;">
              <el-tag :type="item.type === 'LOST' ? 'danger' : 'success'" size="small" style="border-radius: 4px;">
                {{ item.type === 'LOST' ? '寻物' : '招领' }}
              </el-tag>
              <el-tag v-if="item.pinned" type="danger" size="small" style="border-radius: 4px;">置顶</el-tag>
            </div>
            <el-tag v-if="item.status === 'RESOLVED'" type="info" size="small" style="border-radius: 4px;">已解决</el-tag>
          </div>
          <h3 style="font-size: 16px; margin: 0 0 8px;">{{ item.title }}</h3>
          <p style="color: #909399; font-size: 13px; margin: 0 0 8px;">
            <span style="display: inline-flex; align-items: center; gap: 4px;">📍 {{ item.location || '未知地点' }}</span>
          </p>
          <div style="color: #c0c4cc; font-size: 12px;">{{ formatTime(item.createdAt) }}</div>
        </div>
      </div>
    </div>
    <div style="margin-top: 20px; text-align: center;">
      <el-pagination background layout="prev, pager, next" :total="total" :page-size="20" v-model:current-page="page" @current-change="fetchItems" />
    </div>
  </div>
</template>
