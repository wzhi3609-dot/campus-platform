<script setup>
// 帖子审核组件：展示待审核帖子列表，支持通过 / 驳回操作
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../../api'

// 待审核帖子列表
const posts = ref([])
// 列表加载状态
const loading = ref(false)
// 待审核帖子总条数
const total = ref(0)
// 当前页码
const page = ref(1)

// 获取待审核帖子列表（分页）
async function fetchPending() {
  loading.value = true
  try {
    const res = await request.get('/admin/posts/pending', { params: { page: page.value - 1, size: 20 } })
    posts.value = res.data.content
    total.value = res.data.totalElements
  } finally {
    loading.value = false
  }
}

// 通过指定帖子的审核
async function approve(id) {
  await request.put(`/admin/posts/${id}/approve`)
  ElMessage.success('已通过')
  fetchPending()
}

// 驳回指定帖子的审核（含确认弹窗）
async function reject(id) {
  try {
    await ElMessageBox.confirm('确定驳回该帖子？', '确认')
    await request.put(`/admin/posts/${id}/reject`)
    ElMessage.success('已驳回')
    fetchPending()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

// 格式化时间戳为中文本地时间字符串
function formatTime(t) {
  return new Date(t).toLocaleString('zh-CN')
}

// 去除 HTML 标签，提取纯文本内容（用于列表预览）
function stripHtml(html) {
  const d = document.createElement('div')
  d.innerHTML = html
  return d.textContent || d.innerText || ''
}

// 加载时获取待审核列表
onMounted(fetchPending)
</script>

<template>
  <div>
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
      <span style="font-weight: 600; font-size: 15px;">待审核帖子 ({{ total }})</span>
    </div>
    <el-table :data="posts" v-loading="loading" style="width: 100%; border-radius: 8px;" stripe>
      <el-table-column prop="title" label="标题" min-width="200" />
      <el-table-column label="内容预览" min-width="200">
        <template #default="{ row }">{{ stripHtml(row.content).substring(0, 60) }}...</template>
      </el-table-column>
      <el-table-column prop="userName" label="作者" width="100" />
      <el-table-column prop="createdAt" label="发布时间" width="160">
        <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button size="small" type="success" @click="approve(row.id)" style="border-radius: 6px;">通过</el-button>
          <el-button size="small" type="danger" @click="reject(row.id)" style="border-radius: 6px;">驳回</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div v-if="!loading && posts.length === 0" style="text-align: center; padding: 48px; color: #909399; font-size: 15px;">
      ✅ 暂无待审核帖子
    </div>
    <div style="margin-top: 16px; text-align: center;">
      <el-pagination background layout="prev, pager, next" :total="total" :page-size="20" v-model:current-page="page" @current-change="fetchPending" />
    </div>
  </div>
</template>
