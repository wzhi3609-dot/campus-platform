<script setup>
// 失物招领详情页：展示详细信息，支持标记已解决、编辑、删除
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../api'

const route = useRoute()
const router = useRouter()
const item = ref(null)
const loading = ref(true)
const currentUserId = ref(null)
const reportVisible = ref(false)
const reportReason = ref('')
const submittingReport = ref(false)

// 格式化时间戳为中文本地时间字符串
function formatTime(t) {
  return new Date(t).toLocaleString('zh-CN')
}

// 获取失物招领详情
async function fetchItem() {
  loading.value = true
  try {
    const res = await request.get(`/lost-found/${route.params.id}`)
    item.value = res.data
  } finally {
    loading.value = false
  }
}

// 标记该失物招领为已解决
async function resolveItem() {
  await request.put(`/lost-found/${route.params.id}/resolve`)
  ElMessage.success('已标记为已解决')
  fetchItem()
}

// 删除该记录（确认后执行）
async function deleteItem() {
  try {
    await ElMessageBox.confirm('确定删除该记录？', '确认')
    await request.delete(`/lost-found/${route.params.id}`)
    ElMessage.success('已删除')
    router.push('/lost-found')
  } catch (e) { if (e !== 'cancel') ElMessage.error('删除失败') }
}

async function submitReport() {
  if (!reportReason.value.trim()) return
  submittingReport.value = true
  try {
    await request.post('/reports', { targetType: 'lostfound', targetId: route.params.id, reason: reportReason.value })
    ElMessage.success('举报已提交')
    reportVisible.value = false
    reportReason.value = ''
  } finally {
    submittingReport.value = false
  }
}

onMounted(() => {
  const user = JSON.parse(localStorage.getItem('user') || 'null')
  if (user) currentUserId.value = user.id
  fetchItem()
})
</script>

<template>
  <div style="max-width: 800px; margin: 0 auto;" v-loading="loading">
    <div v-if="loading && !item" style="min-height: 200px;"></div>
    <div v-if="item">
    <el-button text @click="router.back()" style="margin-bottom: 16px; font-size: 14px;">&lt; 返回列表</el-button>
    <div style="background: #fff; border-radius: 12px; border: 1px solid #e0d5c0; padding: 28px;">
      <div style="display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 12px;">
        <div style="flex: 1;">
          <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 8px;">
            <el-tag :type="item.type === 'LOST' ? 'danger' : 'success'" size="large" style="border-radius: 6px; padding: 4px 12px;">
              {{ item.type === 'LOST' ? '寻物' : '招领' }}
            </el-tag>
            <el-tag v-if="item.status === 'RESOLVED'" type="info" size="large" style="border-radius: 6px; padding: 4px 12px;">已解决</el-tag>
          </div>
          <h1 style="margin: 0; font-size: 24px;">{{ item.title }}</h1>
        </div>
      </div>
      <el-divider style="margin: 20px 0;" />
      <div style="white-space: pre-wrap; line-height: 1.8; font-size: 15px; color: #3a4030;">{{ item.description || '暂无描述' }}</div>
      <el-divider style="margin: 20px 0;" />
      <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px; color: #4a4030; font-size: 14px;">
        <div style="background: #f5f0e8; padding: 12px; border-radius: 8px;"><strong style="color: #3a4030;">发布者：</strong>{{ item.userName || '匿名' }}</div>
        <div style="background: #f5f0e8; padding: 12px; border-radius: 8px;"><strong style="color: #3a4030;">地点：</strong>{{ item.location || '未知' }}</div>
        <div style="background: #f5f0e8; padding: 12px; border-radius: 8px;"><strong style="color: #3a4030;">联系人：</strong>{{ item.contactPerson || '匿名' }}</div>
        <div style="background: #f5f0e8; padding: 12px; border-radius: 8px;"><strong style="color: #3a4030;">联系电话：</strong>{{ item.contactPhone || '未提供' }}</div>
        <div style="background: #f5f0e8; padding: 12px; border-radius: 8px;"><strong style="color: #3a4030;">发布时间：</strong>{{ formatTime(item.createdAt) }}</div>
      </div>
      <div style="margin-top: 20px; display: flex; gap: 10px; flex-wrap: wrap; padding-top: 16px; border-top: 1px solid #e0d5c0;">
        <template v-if="currentUserId === item.userId">
          <el-button v-if="item.status !== 'RESOLVED'" type="success" plain @click="resolveItem" style="border-radius: 8px;">标记已解决</el-button>
          <el-button type="primary" plain @click="router.push(`/lost-found/create?id=${item.id}`)" style="border-radius: 8px;">编辑</el-button>
          <el-button type="danger" plain @click="deleteItem" style="border-radius: 8px;">删除</el-button>
        </template>
        <el-button v-if="currentUserId && currentUserId !== item.userId" type="warning" plain @click="reportVisible = true" style="border-radius: 8px;">举报</el-button>
      </div>
    </div>
    </div>
    <el-dialog v-model="reportVisible" title="举报信息" width="400px">
      <el-input v-model="reportReason" type="textarea" :rows="4" placeholder="请描述举报原因" maxlength="500" show-word-limit />
      <template #footer>
        <el-button @click="reportVisible = false">取消</el-button>
        <el-button type="primary" :loading="submittingReport" @click="submitReport">提交举报</el-button>
      </template>
    </el-dialog>
  </div>
</template>
