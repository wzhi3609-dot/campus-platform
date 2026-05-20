<script setup>
// 问答列表页：展示所有问题，支持分页和关键词搜索
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../api'

const router = useRouter()
// 问题列表数据
const questions = ref([])
// 列表加载状态
const loading = ref(false)
// 搜索关键词
const keyword = ref('')
// 问题总条数
const total = ref(0)
// 当前页码
const page = ref(1)

// 获取问题列表（分页 + 关键词搜索）
async function fetchQuestions() {
  loading.value = true
  try {
    const params = { page: page.value - 1, size: 20 }
    if (keyword.value) params.keyword = keyword.value
    const res = await request.get('/questions', { params })
    questions.value = res.data.content
    total.value = res.data.totalElements
  } finally {
    loading.value = false
  }
}

// 格式化时间戳为中文本地时间字符串
function formatTime(t) {
  return new Date(t).toLocaleString('zh-CN')
}

onMounted(fetchQuestions)
</script>

<template>
  <div>
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; flex-wrap: wrap; gap: 12px;">
      <div>
        <h2 style="margin: 0 0 4px; font-size: 20px;">问答</h2>
        <p style="margin: 0; color: #909399; font-size: 13px;">提出问题，分享知识</p>
      </div>
      <el-button type="primary" @click="router.push('/questions/create')" style="border-radius: 8px;">+ 提问</el-button>
    </div>
    <div style="display: flex; gap: 8px; margin-bottom: 20px;">
      <el-input v-model="keyword" placeholder="搜索问题..." style="max-width: 320px;" clearable @keyup.enter="fetchQuestions">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-button @click="fetchQuestions" style="border-radius: 6px;">搜索</el-button>
    </div>
    <div v-loading="loading">
      <div v-for="q in questions" :key="q.id" style="background: #fff; border-radius: 10px; margin-bottom: 12px; cursor: pointer; border: 1px solid #f0f0f0; transition: all 0.2s; padding: 20px;" @click="router.push(`/questions/${q.id}`)" @mouseenter="$event.currentTarget.style.borderColor='#1a73e8'; $event.currentTarget.style.boxShadow='0 2px 8px rgba(26,115,232,0.08)'" @mouseleave="$event.currentTarget.style.borderColor='#f0f0f0'; $event.currentTarget.style.boxShadow='none'">
        <div style="display: flex; gap: 16px; align-items: flex-start;">
          <div style="text-align: center; min-width: 56px;">
            <div style="font-size: 20px; font-weight: 700; color: #1a73e8;">{{ q.answerCount || 0 }}</div>
            <div style="font-size: 11px; color: #909399;">回答</div>
          </div>
          <div style="flex: 1; min-width: 0;">
            <div style="display: flex; align-items: center; gap: 8px; flex-wrap: wrap;">
              <el-tag v-if="q.resolved" type="success" size="small" style="border-radius: 4px;">已解决</el-tag>
              <span style="font-size: 16px; font-weight: 500; color: #303133;">{{ q.title }}</span>
            </div>
            <div style="margin-top: 8px; display: flex; align-items: center; gap: 12px; font-size: 12px; color: #909399;">
              <span>{{ q.userName || '匿名' }}</span>
              <span>·</span>
              <span>{{ q.viewCount }} 次浏览</span>
              <span>·</span>
              <span>{{ formatTime(q.createdAt) }}</span>
            </div>
            <div v-if="q.tags" style="margin-top: 8px; display: flex; gap: 4px; flex-wrap: wrap;">
              <el-tag v-for="tag in (q.tags || '').split(',')" :key="tag" size="small" style="border-radius: 4px; background: #e8f0fe; color: #1a73e8; border: none;">{{ tag }}</el-tag>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div v-if="!loading && questions.length === 0" style="text-align: center; padding: 60px 0; color: #909399;">
      <div style="font-size: 40px; margin-bottom: 12px;">💬</div>
      <p style="font-size: 15px;">还没有任何问题</p>
      <el-button type="primary" plain @click="router.push('/questions/create')">第一个提问</el-button>
    </div>
    <div style="margin-top: 20px; text-align: center;">
      <el-pagination background layout="prev, pager, next" :total="total" :page-size="20" v-model:current-page="page" @current-change="fetchQuestions" />
    </div>
  </div>
</template>
