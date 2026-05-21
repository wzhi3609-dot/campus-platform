<script setup>
// 论坛帖子详情页：展示帖子内容与评论列表，支持点赞、收藏、评论、删除/编辑
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../api'

const route = useRoute()
const router = useRouter()
// 当前帖子详情数据
const post = ref(null)
const loading = ref(true)
// 评论列表
const comments = ref([])
// 评论总条数
const commentTotal = ref(0)
// 评论列表当前页码
const commentPage = ref(1)
// 新评论输入内容
const newComment = ref('')
// 当前登录用户 ID
const currentUserId = ref(null)
// 当前用户是否已收藏该帖子
const favorited = ref(false)
// 收藏数量
const favoriteCount = ref(0)
// 提交评论加载状态
const submittingComment = ref(false)
const reportVisible = ref(false)
const reportReason = ref('')
const submittingReport = ref(false)

// 格式化时间戳为中文本地时间字符串
function formatTime(t) {
  return new Date(t).toLocaleString('zh-CN')
}

// 获取帖子详情
async function fetchPost() {
  loading.value = true
  try {
    const res = await request.get(`/posts/${route.params.id}`)
    post.value = res.data
  } finally {
    loading.value = false
  }
}

// 获取评论列表（分页）
async function fetchComments() {
  const res = await request.get('/comments', { params: { targetType: 'post', targetId: route.params.id, page: commentPage.value - 1, size: 20 } })
  comments.value = res.data.content
  commentTotal.value = res.data.totalElements
}

// 检查当前用户是否已收藏该帖子
async function checkFavorite() {
  if (!currentUserId.value) return
  try {
    const res = await request.get('/favorites/check', { params: { targetType: 'post', targetId: route.params.id } })
    favorited.value = res.data.favorited
  } catch {}
}

// 获取该帖子的收藏数量
async function fetchFavoriteCount() {
  const res = await request.get('/favorites/count', { params: { targetType: 'post', targetId: route.params.id } })
  favoriteCount.value = res.data.count
}

// 删除该帖子（确认后执行）
async function handleDelete() {
  try {
    await ElMessageBox.confirm('确定删除该帖子？', '确认')
    await request.delete(`/posts/${route.params.id}`)
    ElMessage.success('已删除')
    router.push('/forum')
  } catch (e) { if (e !== 'cancel') ElMessage.error('删除失败') }
}

// 点赞该帖子
async function handleLike() {
  await request.post(`/posts/${route.params.id}/like`)
  post.value.likeCount++
}

// 切换收藏状态（收藏 / 取消收藏）
async function toggleFavorite() {
  const res = await request.post('/favorites/toggle', { targetType: 'post', targetId: route.params.id })
  favorited.value = res.data.favorited
  fetchFavoriteCount()
}

// 提交新评论
async function submitComment() {
  if (!newComment.value.trim()) return
  submittingComment.value = true
  try {
    await request.post('/comments', { targetType: 'post', targetId: route.params.id, content: newComment.value })
    newComment.value = ''
    commentPage.value = 1
    fetchComments()
  } finally {
    submittingComment.value = false
  }
}

async function submitReport() {
  if (!reportReason.value.trim()) return
  submittingReport.value = true
  try {
    await request.post('/reports', { targetType: 'post', targetId: route.params.id, reason: reportReason.value })
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
  fetchPost()
  fetchComments()
  fetchFavoriteCount()
  checkFavorite()
})
</script>

<template>
  <div style="max-width: 900px; margin: 0 auto;" v-loading="loading">
    <div v-if="loading && !post" style="min-height: 200px;"></div>
    <div v-if="post">
    <el-button text @click="router.back()" style="margin-bottom: 16px; font-size: 14px;">&lt; 返回列表</el-button>
    <div style="background: #fff; border-radius: 12px; border: 1px solid #e0d5c0; padding: 28px; margin-bottom: 24px;">
      <h1 style="margin: 0 0 12px; font-size: 24px;">{{ post.title }}</h1>
      <div style="display: flex; align-items: center; gap: 12px; color: #8a7a6a; font-size: 13px; flex-wrap: wrap; margin-bottom: 20px;">
        <span>{{ post.userName || '匿名' }}</span>
        <span>·</span>
        <span>{{ formatTime(post.createdAt) }}</span>
        <span>·</span>
        <span>{{ post.viewCount }} 次浏览</span>
        <el-tag v-if="post.status === 'PENDING'" type="warning" size="small" style="border-radius: 4px;">待审核</el-tag>
      </div>
      <div style="line-height: 1.8; font-size: 15px;" v-html="post.content"></div>
      <div style="margin-top: 20px; display: flex; gap: 12px; flex-wrap: wrap; padding-top: 16px; border-top: 1px solid #e0d5c0;">
        <el-button size="small" text type="primary" @click="handleLike" style="border-radius: 6px;">👍 {{ post.likeCount || 0 }}</el-button>
        <el-button size="small" :type="favorited ? 'warning' : 'default'" @click="toggleFavorite" style="border-radius: 6px;">
          {{ favorited ? '★' : '☆' }} 收藏 {{ favoriteCount }}
        </el-button>
        <el-button v-if="currentUserId === post.userId" size="small" text type="primary" @click="router.push(`/forum/create?id=${post.id}`)" style="border-radius: 6px;">编辑</el-button>
        <el-button v-if="currentUserId === post.userId" size="small" text type="danger" @click="handleDelete" style="border-radius: 6px;">删除</el-button>
        <el-button v-if="currentUserId && currentUserId !== post.userId" size="small" text type="warning" @click="reportVisible = true" style="border-radius: 6px;">举报</el-button>
      </div>
    </div>

    <div style="background: #fff; border-radius: 12px; border: 1px solid #e0d5c0; padding: 24px;">
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
      <div v-if="comments.length === 0" style="color: #8a7a6a; text-align: center; padding: 24px; font-size: 14px;">暂无评论，来发表第一条评论吧</div>
      <div v-if="commentTotal > 20" style="margin-top: 16px; text-align: center;">
        <el-pagination background layout="prev, pager, next" :total="commentTotal" :page-size="20"
          v-model:current-page="commentPage" @current-change="fetchComments" />
      </div>
    </div>
    </div>
    <el-dialog v-model="reportVisible" title="举报帖子" width="400px">
      <el-input v-model="reportReason" type="textarea" :rows="4" placeholder="请描述举报原因" maxlength="500" show-word-limit />
      <template #footer>
        <el-button @click="reportVisible = false">取消</el-button>
        <el-button type="primary" :loading="submittingReport" @click="submitReport">提交举报</el-button>
      </template>
    </el-dialog>
  </div>
</template>
