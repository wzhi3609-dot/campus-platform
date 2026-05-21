<script setup>
// 论坛帖子列表页：展示所有帖子，支持分页
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../api'

const router = useRouter()
const posts = ref([])
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const keyword = ref('')

// 获取帖子列表（分页）
async function fetchPosts() {
  loading.value = true
  try {
    const params = { page: page.value - 1, size: 20 }
    if (keyword.value.trim()) params.keyword = keyword.value
    const res = await request.get('/posts', { params })
    posts.value = res.data.content
    total.value = res.data.totalElements
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  fetchPosts()
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

onMounted(fetchPosts)
</script>

<template>
  <div>
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; flex-wrap: wrap; gap: 12px;">
      <div>
        <h2 style="margin: 0 0 4px; font-size: 20px;">校园论坛</h2>
        <p style="margin: 0; color: #8a7a6a; font-size: 13px;">自由交流，分享校园生活（发帖需审核）</p>
      </div>
      <el-button type="primary" @click="router.push('/forum/create')" style="border-radius: 8px;">+ 发帖</el-button>
    </div>
    <div style="margin-bottom: 16px; display: flex; gap: 8px;">
      <el-input v-model="keyword" placeholder="搜索帖子标题..." clearable @keyup.enter="handleSearch" style="max-width: 400px;" />
      <el-button type="primary" @click="handleSearch" style="border-radius: 8px;">搜索</el-button>
    </div>
    <div v-loading="loading">
      <div v-if="posts.length === 0 && !loading" style="text-align: center; padding: 60px 0; color: #8a7a6a;">
        <div style="font-size: 40px; margin-bottom: 12px;">📝</div>
        <p style="font-size: 15px;">暂无帖子</p>
        <el-button type="primary" plain @click="router.push('/forum/create')">发布第一个帖子</el-button>
      </div>
      <div v-for="post in posts" :key="post.id" style="background: #fff; border-radius: 10px; margin-bottom: 12px; cursor: pointer; border: 1px solid #e0d5c0; transition: all 0.2s; padding: 20px;" @click="router.push(`/forum/${post.id}`)" @mouseenter="$event.currentTarget.style.borderColor='#9a6a30'; $event.currentTarget.style.boxShadow='0 2px 8px rgba(249,171,0,0.08)'" @mouseleave="$event.currentTarget.style.borderColor='#e0d5c0'; $event.currentTarget.style.boxShadow='none'">
        <div style="display: flex; justify-content: space-between; align-items: flex-start; gap: 12px;">
          <div style="flex: 1; min-width: 0;">
            <div style="display: flex; align-items: center; gap: 8px; flex-wrap: wrap;">
              <el-tag v-if="post.pinned" type="danger" size="small" style="border-radius: 4px;">置顶</el-tag>
              <span style="font-size: 16px; font-weight: 500; color: #3a4030;">{{ post.title }}</span>
            </div>
            <p style="color: #8a7a6a; font-size: 13px; margin: 8px 0 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">
              {{ stripHtml(post.content).substring(0, 120) }}
            </p>
            <div style="display: flex; align-items: center; gap: 12px; font-size: 12px; color: #c0c4cc; margin-top: 8px;">
              <span>{{ post.userName || '匿名' }}</span>
              <span>·</span>
              <span>{{ formatTime(post.createdAt) }}</span>
              <span>·</span>
              <span>👁 {{ post.viewCount }}</span>
            </div>
          </div>
          <div style="text-align: center; min-width: 48px;">
            <div style="font-size: 16px; font-weight: 600; color: #9a6a30;">{{ post.likeCount || 0 }}</div>
            <div style="font-size: 11px; color: #8a7a6a;">赞</div>
          </div>
        </div>
      </div>
    </div>
    <div style="margin-top: 20px; text-align: center;">
      <el-pagination background layout="prev, pager, next" :total="total" :page-size="20" v-model:current-page="page" @current-change="fetchPosts" />
    </div>
  </div>
</template>
