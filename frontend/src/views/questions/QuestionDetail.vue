<script setup>
// 问答详情页：展示问题内容与回答列表，支持回答问题、采纳答案、收藏、点赞、删除/编辑
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../api'

const route = useRoute()
const router = useRouter()
// 当前问题详情数据
const question = ref(null)
// 回答列表
const answers = ref([])
// 新回答输入内容
const newAnswer = ref('')
// 提交回答加载状态
const loading = ref(false)
// 当前登录用户 ID
const currentUserId = ref(null)
// 当前用户是否已收藏该问题
const favorited = ref(false)
// 收藏数量
const favoriteCount = ref(0)

// 格式化时间戳为中文本地时间字符串
function formatTime(t) {
  return new Date(t).toLocaleString('zh-CN')
}

// 获取问题详情
async function fetchDetail() {
  const res = await request.get(`/questions/${route.params.id}`)
  question.value = res.data
}

// 获取回答列表
async function fetchAnswers() {
  const res = await request.get(`/questions/${route.params.id}/answers`)
  answers.value = res.data
}

// 提交新回答
async function submitAnswer() {
  if (!newAnswer.value.trim()) return
  loading.value = true
  try {
    await request.post(`/questions/${route.params.id}/answers`, { content: newAnswer.value })
    newAnswer.value = ''
    fetchAnswers()
    fetchDetail()
  } finally {
    loading.value = false
  }
}

// 采纳指定回答（仅提问者可操作）
async function acceptAnswer(answerId) {
  await request.post(`/questions/${route.params.id}/answers/${answerId}/accept`)
  fetchDetail()
  fetchAnswers()
}

// 删除该问题（确认后执行）
async function handleDelete() {
  try {
    await ElMessageBox.confirm('确定删除该问题？', '确认')
    await request.delete(`/questions/${route.params.id}`)
    ElMessage.success('已删除')
    router.push('/questions')
  } catch (e) { if (e !== 'cancel') ElMessage.error('删除失败') }
}

// 点赞该问题
async function handleLike() {
  await request.post(`/questions/${route.params.id}/like`)
  question.value.likeCount++
}

// 检查当前用户是否已收藏该问题
async function checkFavorite() {
  if (!currentUserId.value) return
  try {
    const res = await request.get('/favorites/check', { params: { targetType: 'question', targetId: route.params.id } })
    favorited.value = res.data.favorited
  } catch {}
}

// 获取该问题的收藏数量
async function fetchFavoriteCount() {
  const res = await request.get('/favorites/count', { params: { targetType: 'question', targetId: route.params.id } })
  favoriteCount.value = res.data.count
}

// 切换收藏状态（收藏 / 取消收藏）
async function toggleFavorite() {
  const res = await request.post('/favorites/toggle', { targetType: 'question', targetId: route.params.id })
  favorited.value = res.data.favorited
  fetchFavoriteCount()
}

// --- 评论区 ---
const comments = ref([])
const commentTotal = ref(0)
const commentPage = ref(1)
const newComment = ref('')
const submittingComment = ref(false)

async function fetchComments() {
  const res = await request.get('/comments', { params: { targetType: 'question', targetId: route.params.id, page: commentPage.value - 1, size: 20 } })
  comments.value = res.data.content
  commentTotal.value = res.data.totalElements
}

async function submitComment() {
  if (!newComment.value.trim()) return
  submittingComment.value = true
  try {
    await request.post('/comments', { targetType: 'question', targetId: route.params.id, content: newComment.value })
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
  await request.post('/reports', { targetType: 'question', targetId: route.params.id, reason: reportReason.value })
  ElMessage.success('举报已提交')
  reportDialogVisible.value = false
}

onMounted(() => {
  const user = JSON.parse(localStorage.getItem('user') || 'null')
  if (user) currentUserId.value = user.id
  fetchDetail()
  fetchAnswers()
  fetchFavoriteCount()
  checkFavorite()
  fetchComments()
})
</script>

<template>
  <div v-if="question" style="max-width: 800px; margin: 0 auto;">
    <el-button text @click="router.back()" style="margin-bottom: 16px; font-size: 14px;">&lt; 返回列表</el-button>
    <div style="background: #fff; border-radius: 12px; border: 1px solid #f0f0f0; padding: 28px; margin-bottom: 24px;">
      <div style="display: flex; align-items: flex-start; gap: 16px; margin-bottom: 16px;">
        <div style="text-align: center; min-width: 72px; background: #f5f7fa; border-radius: 10px; padding: 12px 8px;">
          <div style="font-size: 28px; font-weight: 700; color: #1a73e8;">{{ question.answerCount || 0 }}</div>
          <div style="font-size: 12px; color: #909399;">回答</div>
        </div>
        <div style="flex: 1;">
          <h1 style="margin: 0 0 8px; font-size: 22px; line-height: 1.4;">{{ question.title }}</h1>
          <div style="display: flex; align-items: center; gap: 12px; color: #909399; font-size: 13px; flex-wrap: wrap;">
            <span>{{ question.userName || '匿名' }}</span>
            <span>·</span>
            <span>{{ formatTime(question.createdAt) }}</span>
            <span>·</span>
            <span>{{ question.viewCount }} 次浏览</span>
            <el-tag v-if="question.resolved" type="success" size="small" style="border-radius: 4px;">已解决</el-tag>
          </div>
        </div>
      </div>
      <el-divider style="margin: 16px 0;" />
      <div style="white-space: pre-wrap; line-height: 1.8; font-size: 15px; color: #303133;">{{ question.content }}</div>
      <div v-if="question.tags" style="margin-top: 16px; display: flex; gap: 6px; flex-wrap: wrap;">
        <el-tag v-for="tag in question.tags.split(',')" :key="tag" size="small" style="border-radius: 4px; background: #e8f0fe; color: #1a73e8; border: none;">{{ tag }}</el-tag>
      </div>
      <div style="margin-top: 20px; display: flex; gap: 12px; align-items: center; padding-top: 16px; border-top: 1px solid #f0f0f0;">
        <el-button size="small" :type="favorited ? 'warning' : 'default'" @click="toggleFavorite" style="border-radius: 6px;">
          {{ favorited ? '★' : '☆' }} 收藏 {{ favoriteCount }}
        </el-button>
        <el-button size="small" text type="primary" @click="handleLike" style="border-radius: 6px;">👍 {{ question.likeCount || 0 }}</el-button>
        <el-button v-if="currentUserId === question.userId" size="small" text type="primary" @click="router.push(`/questions/create?id=${question.id}`)" style="border-radius: 6px;">编辑</el-button>
        <el-button v-if="currentUserId === question.userId" size="small" text type="danger" @click="handleDelete" style="border-radius: 6px;">删除</el-button>
      </div>
    </div>

    <h3 style="font-size: 18px; margin: 0 0 16px; display: flex; align-items: center; gap: 8px;">
      {{ answers.length }} 个回答
    </h3>
    <div v-for="answer in answers" :key="answer.id" style="background: #fff; border-radius: 10px; border: 1px solid #f0f0f0; padding: 20px; margin-bottom: 12px;">
      <div style="white-space: pre-wrap; line-height: 1.8; font-size: 15px;">{{ answer.content }}</div>
      <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 12px; color: #909399; font-size: 13px;">
        <span>{{ formatTime(answer.createdAt) }}</span>
        <div style="display: flex; align-items: center; gap: 8px;">
          <el-tag v-if="answer.accepted" type="success" size="small" style="border-radius: 4px;">✓ 已采纳</el-tag>
          <el-button v-if="!answer.accepted && question.userId === currentUserId" size="small" type="primary" plain @click="acceptAnswer(answer.id)" style="border-radius: 6px;">采纳</el-button>
        </div>
      </div>
    </div>

    <div v-if="currentUserId" style="background: #fff; border-radius: 12px; border: 1px solid #f0f0f0; padding: 24px; margin-top: 20px;">
      <div style="font-weight: 600; font-size: 16px; margin-bottom: 12px;">写回答</div>
      <el-input v-model="newAnswer" type="textarea" :rows="4" placeholder="写下你的回答..." />
      <el-button type="primary" style="margin-top: 12px; border-radius: 8px;" :loading="loading" @click="submitAnswer">提交回答</el-button>
    </div>
    <div v-else style="background: #f5f7fa; border-radius: 12px; padding: 24px; text-align: center; margin-top: 20px; color: #909399;">
      <el-link type="primary" @click="router.push('/login')">登录后可以回答问题</el-link>
    </div>

    <!-- 评论区 -->
    <div style="background: #fff; border-radius: 12px; border: 1px solid #f0f0f0; padding: 24px; margin-top: 20px;">
      <div style="font-size: 18px; font-weight: 600; margin-bottom: 20px;">评论 ({{ commentTotal }})</div>
      <div v-if="currentUserId" style="display: flex; gap: 8px; margin-bottom: 20px;">
        <el-input v-model="newComment" placeholder="写下你的评论..." @keyup.enter="submitComment" />
        <el-button type="primary" :loading="submittingComment" @click="submitComment" style="border-radius: 6px;">发表</el-button>
      </div>
      <div v-for="c in comments" :key="c.id" style="padding: 12px 0; border-bottom: 1px solid #f0f0f0;">
        <div style="display: flex; justify-content: space-between; font-size: 13px;">
          <span style="color: #1a73e8; font-weight: 500;">{{ c.userName || '匿名' }}</span>
          <span style="color: #909399;">{{ formatTime(c.createdAt) }}</span>
        </div>
        <div style="margin-top: 6px; line-height: 1.6; font-size: 14px;">{{ c.content }}</div>
      </div>
      <div v-if="comments.length === 0" style="color: #909399; text-align: center; padding: 24px; font-size: 14px;">暂无评论</div>
    </div>

    <!-- 举报弹窗 -->
    <el-dialog v-model="reportDialogVisible" title="举报问题" width="400px">
      <el-input v-model="reportReason" type="textarea" :rows="3" placeholder="请填写举报原因" />
      <template #footer>
        <el-button @click="reportDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="submitReport">提交举报</el-button>
      </template>
    </el-dialog>
    <el-button v-if="currentUserId && currentUserId !== question.userId" size="small" text type="danger" @click="openReport" style="margin-left: 8px;">举报</el-button>
  </div>
</template>
