<script setup>
// 消息通知列表页：展示所有通知，支持标记已读和点击跳转至目标页面
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../api'

const router = useRouter()
// 通知列表数据
const notifications = ref([])
// 列表加载状态
const loading = ref(false)
// 通知总条数
const total = ref(0)
// 当前页码
const page = ref(1)

// 格式化时间戳为中文本地时间字符串
function formatTime(t) {
  return new Date(t).toLocaleString('zh-CN')
}

// 获取通知列表（分页）
async function fetchNotifications() {
  loading.value = true
  try {
    const res = await request.get('/notifications', { params: { page: page.value - 1, size: 20 } })
    notifications.value = res.data.content
    total.value = res.data.totalElements
  } finally {
    loading.value = false
  }
}

// 标记单条通知为已读
async function markRead(n) {
  if (n.read) return
  await request.put(`/notifications/${n.id}/read`)
  n.read = true
}

// 点击通知时标记已读并跳转到对应的目标页面（问答或论坛）
function goToTarget(n) {
  markRead(n)
  if (n.type === 'answer') router.push(`/questions/${n.relatedId}`)
  else if (n.type === 'post_approved' || n.type === 'post_rejected') router.push(`/forum/${n.relatedId}`)
}

onMounted(fetchNotifications)
</script>

<template>
  <div style="max-width: 700px; margin: 0 auto;">
    <div style="margin-bottom: 24px;">
      <h2 style="margin: 0 0 4px; font-size: 22px;">消息通知</h2>
      <p style="margin: 0; color: #8a7a6a; font-size: 13px;">查看与你相关的消息</p>
    </div>
    <div v-loading="loading">
      <div v-if="notifications.length === 0 && !loading" style="text-align: center; padding: 60px 0; color: #8a7a6a;">
        <div style="font-size: 40px; margin-bottom: 12px;">🔔</div>
        <p style="font-size: 15px;">暂无消息</p>
      </div>
      <div v-for="n in notifications" :key="n.id" style="background: #fff; border-radius: 10px; margin-bottom: 10px; cursor: pointer; transition: all 0.2s; padding: 16px 20px;"
        :style="{ borderLeft: n.read ? '3px solid transparent' : '3px solid #5c6e3d', border: '1px solid #e0d5c0', background: n.read ? '#fff' : '#f8faff' }"
        @click="goToTarget(n)"
        @mouseenter="$event.currentTarget.style.boxShadow='0 2px 8px rgba(0,0,0,0.06)'"
        @mouseleave="$event.currentTarget.style.boxShadow='none'">
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <div style="display: flex; align-items: center; gap: 8px;">
            <el-tag v-if="!n.read" size="small" type="danger" style="border-radius: 4px;">新</el-tag>
            <strong :style="{ color: n.read ? '#4a4030' : '#3a4030' }">{{ n.title }}</strong>
          </div>
          <span style="color: #8a7a6a; font-size: 12px;">{{ formatTime(n.createdAt) }}</span>
        </div>
        <p style="color: #4a4030; margin-top: 6px; font-size: 14px;">{{ n.content }}</p>
      </div>
    </div>
    <div style="margin-top: 20px; text-align: center;">
      <el-pagination background layout="prev, pager, next" :total="total" :page-size="20"
        v-model:current-page="page" @current-change="fetchNotifications" />
    </div>
  </div>
</template>
