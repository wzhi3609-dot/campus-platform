<script setup>
// 二手商品详情页：展示商品信息，支持编辑、标记已售/重新上架、删除
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../api'

const route = useRoute()
const router = useRouter()
// 当前商品详情数据
const item = ref(null)
// 当前登录用户 ID
const currentUserId = ref(null)

// 格式化时间戳为中文本地时间字符串
function formatTime(t) {
  return new Date(t).toLocaleString('zh-CN')
}

// 获取商品详情
async function fetchItem() {
  const res = await request.get(`/trades/${route.params.id}`)
  item.value = res.data
}

// 更新商品状态（SOLD：标记已售出 / AVAILABLE：重新上架）
async function updateStatus(status) {
  await request.put(`/trades/${route.params.id}/status`, null, { params: { status } })
  ElMessage.success(status === 'SOLD' ? '已标记为已售出' : '已重新上架')
  fetchItem()
}

// 删除该商品（确认后执行）
async function deleteItem() {
  try {
    await ElMessageBox.confirm('确定删除该商品？', '确认')
    await request.delete(`/trades/${route.params.id}`)
    ElMessage.success('已删除')
    router.push('/trades')
  } catch (e) { if (e !== 'cancel') ElMessage.error('删除失败') }
}

// --- 评论区 ---
const comments = ref([])
const commentTotal = ref(0)
const commentPage = ref(1)
const newComment = ref('')
const submittingComment = ref(false)

async function fetchComments() {
  const res = await request.get('/comments', { params: { targetType: 'trade', targetId: route.params.id, page: commentPage.value - 1, size: 20 } })
  comments.value = res.data.content
  commentTotal.value = res.data.totalElements
}

async function submitComment() {
  if (!newComment.value.trim()) return
  submittingComment.value = true
  try {
    await request.post('/comments', { targetType: 'trade', targetId: route.params.id, content: newComment.value })
    newComment.value = ''
    commentPage.value = 1
    fetchComments()
  } finally {
    submittingComment.value = false
  }
}

// --- 举报 ---
const reportDialogVisible = ref(false)
const reportReason = ref('')

function openReport() {
  reportReason.value = ''
  reportDialogVisible.value = true
}

async function submitReport() {
  if (!reportReason.value.trim()) return
  await request.post('/reports', { targetType: 'trade', targetId: route.params.id, reason: reportReason.value })
  ElMessage.success('举报已提交')
  reportDialogVisible.value = false
}

onMounted(() => {
  const user = JSON.parse(localStorage.getItem('user') || 'null')
  if (user) currentUserId.value = user.id
  fetchItem()
  fetchComments()
})
</script>

<template>
  <div v-if="item" style="max-width: 800px; margin: 0 auto;">
    <el-button text @click="router.back()" style="margin-bottom: 16px; font-size: 14px;">&lt; 返回列表</el-button>
    <div style="background: #fff; border-radius: 12px; border: 1px solid #e0d5c0; padding: 28px;">
      <div style="display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 12px;">
        <div style="flex: 1;">
          <h1 style="margin: 0 0 4px; font-size: 24px;">{{ item.title }}</h1>
          <p style="color: #ea4335; font-size: 28px; font-weight: 700; margin: 12px 0;">¥{{ item.price }}</p>
        </div>
        <div style="display: flex; gap: 8px;">
          <el-tag v-if="item.status === 'AVAILABLE'" type="success" size="large" style="border-radius: 6px; padding: 4px 12px;">在售</el-tag>
          <el-tag v-else-if="item.status === 'SOLD'" type="warning" size="large" style="border-radius: 6px; padding: 4px 12px;">已售出</el-tag>
          <el-tag v-else type="info" size="large" style="border-radius: 6px; padding: 4px 12px;">已下架</el-tag>
        </div>
      </div>
      <el-divider style="margin: 20px 0;" />
      <div style="white-space: pre-wrap; line-height: 1.8; font-size: 15px; color: #3a4030;">{{ item.description || '暂无描述' }}</div>
      <el-divider style="margin: 20px 0;" />
      <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px; color: #4a4030; font-size: 14px;">
        <div style="background: #f5f0e8; padding: 12px; border-radius: 8px;"><strong style="color: #3a4030;">发布者：</strong>{{ item.userName || '匿名' }}</div>
        <div style="background: #f5f0e8; padding: 12px; border-radius: 8px;"><strong style="color: #3a4030;">分类：</strong>{{ item.category || '未分类' }}</div>
        <div style="background: #f5f0e8; padding: 12px; border-radius: 8px;"><strong style="color: #3a4030;">联系人：</strong>{{ item.contactPerson || '匿名' }}</div>
        <div style="background: #f5f0e8; padding: 12px; border-radius: 8px;"><strong style="color: #3a4030;">联系电话：</strong>{{ item.contactPhone || '未提供' }}</div>
        <div style="background: #f5f0e8; padding: 12px; border-radius: 8px;"><strong style="color: #3a4030;">位置：</strong>{{ item.location || '未提供' }}</div>
        <div style="background: #f5f0e8; padding: 12px; border-radius: 8px;"><strong style="color: #3a4030;">发布时间：</strong>{{ formatTime(item.createdAt) }}</div>
        <div style="background: #f5f0e8; padding: 12px; border-radius: 8px;"><strong style="color: #3a4030;">浏览：</strong>{{ item.viewCount }} 次</div>
      </div>
      <div v-if="currentUserId === item.userId" style="margin-top: 20px; display: flex; gap: 10px; flex-wrap: wrap; padding-top: 16px; border-top: 1px solid #e0d5c0;">
        <el-button type="primary" plain @click="router.push(`/trades/create?id=${item.id}`)" style="border-radius: 8px;">编辑</el-button>
        <el-button v-if="item.status === 'AVAILABLE'" type="warning" plain @click="updateStatus('SOLD')" style="border-radius: 8px;">标记已售出</el-button>
        <el-button v-else type="success" plain @click="updateStatus('AVAILABLE')" style="border-radius: 8px;">重新上架</el-button>
        <el-button type="danger" plain @click="deleteItem" style="border-radius: 8px;">删除</el-button>
      </div>
      <el-button v-if="currentUserId && currentUserId !== item.userId" size="small" text type="danger" @click="openReport" style="margin-top: 12px;">举报</el-button>
    </div>

    <!-- 评论区 -->
    <div style="background: #fff; border-radius: 12px; border: 1px solid #e0d5c0; padding: 24px; margin-top: 20px;">
      <div style="font-size: 18px; font-weight: 600; margin-bottom: 20px;">评论 ({{ commentTotal }})</div>
      <div v-if="currentUserId" style="display: flex; gap: 8px; margin-bottom: 20px;">
        <el-input v-model="newComment" placeholder="写下你的评论..." @keyup.enter="submitComment" />
        <el-button type="primary" :loading="submittingComment" @click="submitComment" style="border-radius: 6px;">发表</el-button>
      </div>
      <div v-for="c in comments" :key="c.id" style="padding: 12px 0; border-bottom: 1px solid #e0d5c0;">
        <div style="display: flex; justify-content: space-between; font-size: 13px;">
          <span style="color: #1a73e8; font-weight: 500;">{{ c.userName || '匿名' }}</span>
          <span style="color: #8a7a6a;">{{ formatTime(c.createdAt) }}</span>
        </div>
        <div style="margin-top: 6px; line-height: 1.6; font-size: 14px;">{{ c.content }}</div>
      </div>
      <div v-if="comments.length === 0" style="color: #8a7a6a; text-align: center; padding: 24px; font-size: 14px;">暂无评论</div>
    </div>

    <!-- 举报弹窗 -->
    <el-dialog v-model="reportDialogVisible" title="举报商品" width="400px">
      <el-input v-model="reportReason" type="textarea" :rows="3" placeholder="请填写举报原因" />
      <template #footer>
        <el-button @click="reportDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="submitReport">提交举报</el-button>
      </template>
    </el-dialog>
  </div>
</template>
