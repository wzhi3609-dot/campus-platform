<script setup>
// 二手交易列表页：展示在售商品，支持分类筛选和关键词搜索
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../api'

const router = useRouter()
// 商品列表数据
const items = ref([])
// 列表加载状态
const loading = ref(false)
// 搜索关键词
const keyword = ref('')
// 分类筛选条件
const category = ref('')
// 商品总条数
const total = ref(0)
// 当前页码
const page = ref(1)

// 商品分类选项
const categories = ['教材', '电子产品', '生活用品', '服饰', '体育', '其他']

// 获取商品列表（分页 + 关键词搜索 + 分类筛选）
async function fetchItems() {
  loading.value = true
  try {
    const params = { page: page.value - 1, size: 20 }
    if (keyword.value) params.keyword = keyword.value
    if (category.value) params.category = category.value
    const res = await request.get('/trades', { params })
    items.value = res.data.content
    total.value = res.data.totalElements
  } finally {
    loading.value = false
  }
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
        <h2 style="margin: 0 0 4px; font-size: 20px;">二手交易</h2>
        <p style="margin: 0; color: #8a7a6a; font-size: 13px;">买卖二手物品，让资源循环利用</p>
      </div>
      <el-button type="primary" @click="router.push('/trades/create')" style="border-radius: 8px;">+ 发布商品</el-button>
    </div>
    <div style="display: flex; gap: 8px; margin-bottom: 20px; flex-wrap: wrap;">
      <el-input v-model="keyword" placeholder="搜索商品..." style="max-width: 260px;" clearable @keyup.enter="fetchItems">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-select v-model="category" placeholder="分类" clearable style="width: 140px;" @change="fetchItems">
        <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
      </el-select>
      <el-button @click="fetchItems" style="border-radius: 6px;">搜索</el-button>
    </div>
    <div v-loading="loading">
      <div v-if="items.length === 0 && !loading" style="text-align: center; padding: 60px 0; color: #8a7a6a;">
        <div style="font-size: 40px; margin-bottom: 12px;">🏪</div>
        <p style="font-size: 15px;">暂无商品</p>
        <el-button type="primary" plain @click="router.push('/trades/create')">发布第一个商品</el-button>
      </div>
      <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 16px;">
        <div v-for="item in items" :key="item.id" style="background: #fff; border-radius: 12px; border: 1px solid #e0d5c0; padding: 20px; cursor: pointer; transition: all 0.2s;" @click="router.push(`/trades/${item.id}`)" @mouseenter="$event.currentTarget.style.borderColor='#1a73e8'; $event.currentTarget.style.boxShadow='0 4px 12px rgba(26,115,232,0.1)'" @mouseleave="$event.currentTarget.style.borderColor='#e0d5c0'; $event.currentTarget.style.boxShadow='none'">
          <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 10px;">
            <el-tag size="small" style="border-radius: 4px; background: #e6ede4; color: #5a7a4a; border: none;">{{ item.category || '未分类' }}</el-tag>
            <el-tag v-if="item.status === 'AVAILABLE'" size="small" type="success" style="border-radius: 4px;">在售</el-tag>
            <el-tag v-else-if="item.status === 'SOLD'" size="small" type="warning" style="border-radius: 4px;">已售</el-tag>
            <el-tag v-else size="small" type="info" style="border-radius: 4px;">下架</el-tag>
          </div>
          <h3 style="margin: 0 0 8px; font-size: 16px; font-weight: 500;">{{ item.title }}</h3>
          <p style="color: #ea4335; font-size: 22px; font-weight: 700; margin: 0 0 12px;">¥{{ item.price }}</p>
          <div style="display: flex; justify-content: space-between; align-items: center; font-size: 12px; color: #8a7a6a;">
            <span>{{ item.userName || '匿名' }}</span>
            <span>{{ formatTime(item.createdAt) }}</span>
          </div>
        </div>
      </div>
    </div>
    <div style="margin-top: 20px; text-align: center;">
      <el-pagination background layout="prev, pager, next" :total="total" :page-size="20" v-model:current-page="page" @current-change="fetchItems" />
    </div>
  </div>
</template>
