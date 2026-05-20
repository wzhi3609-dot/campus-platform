<script setup>
// 首页组件：展示平台统计数据、全局搜索、置顶/近期公告及四大功能模块入口卡片
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../api'

const router = useRouter()
// 平台统计数据（问答数/二手数/帖子数/失物招领数）
const stats = ref({})
// 全局搜索关键词
const keyword = ref('')
// 搜索结果数据
const searchResults = ref(null)
// 搜索加载状态
const searching = ref(false)
// 近期公告列表
const announcements = ref([])
// 置顶公告列表
const pinnedAnnouncements = ref([])
// 是否展开显示全部公告
const showAllAnnouncements = ref(false)

// 获取平台统计数据
async function fetchStats() {
  try {
    const res = await request.get('/home/stats')
    stats.value = res.data
  } catch {
    stats.value = {}
  }
}

async function fetchAnnouncements() {
  try {
    const res = await request.get('/announcements')
    pinnedAnnouncements.value = res.data.pinned || []
    announcements.value = res.data.recent || []
  } catch {
    pinnedAnnouncements.value = []
    announcements.value = []
  }
}

// 执行全局搜索（同时搜索问答、二手和论坛）
async function doSearch() {
  if (!keyword.value.trim()) return
  searching.value = true
  try {
    const res = await request.get('/home/search', { params: { keyword: keyword.value } })
    searchResults.value = res.data
  } finally {
    searching.value = false
  }
}

// 格式化时间戳为中文本地时间字符串
function formatTime(t) {
  return new Date(t).toLocaleString('zh-CN')
}

// 四大功能模块配置（路径、名称、描述、图标、颜色）
const modules = [
  { path: '/questions', name: '问答互助', desc: '提出你的问题，帮助他人解答', icon: '💬', color: '#1a73e8', bg: '#e8f0fe' },
  { path: '/trades', name: '二手交易', desc: '买卖二手物品，让资源循环利用', icon: '🏪', color: '#34a853', bg: '#e6f4ea' },
  { path: '/forum', name: '校园论坛', desc: '自由交流，分享校园生活', icon: '📝', color: '#f9ab00', bg: '#fef7e0' },
  { path: '/lost-found', name: '失物招领', desc: '发布丢失/捡到的物品信息', icon: '🔍', color: '#ea4335', bg: '#fce8e6' },
]

// 统计数据卡片配置（标签、对应字段名、颜色）
const statCards = [
  { label: '问答', key: 'questions', color: '#1a73e8' },
  { label: '二手', key: 'trades', color: '#34a853' },
  { label: '论坛帖子', key: 'posts', color: '#f9ab00' },
  { label: '失物招领', key: 'lostFound', color: '#ea4335' },
]

onMounted(() => { fetchStats(); fetchAnnouncements() })
</script>

<template>
  <div>
    <div class="hero-section">
      <div class="hero-shapes">
        <div class="shape shape-1"></div>
        <div class="shape shape-2"></div>
        <div class="shape shape-3"></div>
        <div class="shape shape-4"></div>
      </div>
      <div class="hero-content">
        <h1 class="hero-title">欢迎来到校园互助平台</h1>
        <p class="hero-subtitle">有问题？想买卖二手？丢了东西？来这里就对了</p>
        <div class="hero-search">
          <el-input v-model="keyword" placeholder="全局搜索问题、商品、帖子..." size="large" clearable @keyup.enter="doSearch"
            class="hero-search-input"
            :style="{ '--el-input-bg-color': 'rgba(255,255,255,0.92)', '--el-input-border-color': 'rgba(255,255,255,0.3)', '--el-input-hover-border-color': 'rgba(255,255,255,0.5)', '--el-input-focus-border-color': '#fff', '--el-input-text-color': '#303133' }" />
          <el-button type="warning" size="large" :loading="searching" @click="doSearch" class="hero-search-btn">搜索</el-button>
        </div>
      </div>
    </div>

    <div v-if="searchResults" style="max-width: 600px; margin: -8px auto 32px; position: relative; z-index: 2;">
      <el-card>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span style="font-weight: 600;">搜索结果</span>
          <el-button text size="small" @click="searchResults = null">关闭 ✕</el-button>
        </div>
        <el-divider style="margin: 12px 0;" />
        <div v-if="searchResults.questions?.length">
          <div style="font-size: 12px; color: #1a73e8; font-weight: 600; margin: 8px 0 4px;">💬 问答</div>
          <div v-for="q in searchResults.questions" :key="q.id" style="cursor: pointer; padding: 6px 0; border-bottom: 1px solid #f0f0f0; font-size: 14px;" @click="router.push(`/questions/${q.id}`)">• {{ q.title }}</div>
        </div>
        <div v-if="searchResults.trades?.length">
          <div style="font-size: 12px; color: #34a853; font-weight: 600; margin: 8px 0 4px;">🏪 二手</div>
          <div v-for="t in searchResults.trades" :key="t.id" style="cursor: pointer; padding: 6px 0; border-bottom: 1px solid #f0f0f0; font-size: 14px;" @click="router.push(`/trades/${t.id}`)">• {{ t.title }}</div>
        </div>
        <div v-if="searchResults.posts?.length">
          <div style="font-size: 12px; color: #f9ab00; font-weight: 600; margin: 8px 0 4px;">📝 论坛</div>
          <div v-for="p in searchResults.posts" :key="p.id" style="cursor: pointer; padding: 6px 0; border-bottom: 1px solid #f0f0f0; font-size: 14px;" @click="router.push(`/forum/${p.id}`)">• {{ p.title }}</div>
        </div>
        <div v-if="!searchResults.questions?.length && !searchResults.trades?.length && !searchResults.posts?.length" style="color: #909399; padding: 8px 0; text-align: center;">未找到相关内容</div>
      </el-card>
    </div>

    <div class="stats-row">
      <div v-for="sc in statCards" :key="sc.key" class="stat-card" :style="{ '--stat-color': sc.color }">
        <div class="stat-value">{{ stats[sc.key] ?? 0 }}</div>
        <div class="stat-label">{{ sc.label }}</div>
      </div>
    </div>

    <div v-if="pinnedAnnouncements.length > 0" style="margin-bottom: 24px;">
      <div v-for="a in pinnedAnnouncements" :key="a.id" class="pinned-card">
        <div class="pinned-header">
          <el-tag size="small" type="danger" style="border-radius: 4px;">置顶</el-tag>
          <strong>{{ a.title }}</strong>
          <span class="pinned-meta">{{ a.adminName }} · {{ formatTime(a.createdAt) }}</span>
        </div>
        <div class="pinned-content">{{ a.content }}</div>
      </div>
    </div>

    <div class="modules-grid">
      <div v-for="m in modules" :key="m.path" class="module-card" :style="{ '--module-bg': m.bg, '--module-color': m.color }" @click="router.push(m.path)">
        <div class="module-icon-wrap" :style="{ backgroundColor: m.bg, color: m.color }">{{ m.icon }}</div>
        <div>
          <h3 class="module-name">{{ m.name }}</h3>
          <p class="module-desc">{{ m.desc }}</p>
        </div>
      </div>
    </div>

    <div v-if="announcements.length > 0" class="announcements-section">
      <div class="section-header">
        <h3>公告</h3>
        <el-button v-if="announcements.length > 3" text size="small" @click="showAllAnnouncements = !showAllAnnouncements">
          {{ showAllAnnouncements ? '收起' : '查看全部' }}
        </el-button>
      </div>
      <div v-for="a in (showAllAnnouncements ? announcements : announcements.slice(0, 3))" :key="a.id" class="announcement-item">
        <div class="announcement-title-row">
          <strong>{{ a.title }}</strong>
          <span class="announcement-meta">{{ a.adminName }} · {{ formatTime(a.createdAt) }}</span>
        </div>
        <div class="announcement-content">{{ a.content }}</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.hero-section {
  position: relative;
  overflow: hidden;
  margin: -24px -24px 28px;
  padding: 56px 24px 60px;
  text-align: center;
  color: #fff;
  background: linear-gradient(135deg, #1a73e8 0%, #1557b0 50%, #0d47a1 100%);
}
.hero-section::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image: url("data:image/svg+xml,%3Csvg width='100' height='100' viewBox='0 0 100 100' xmlns='http://www.w3.org/2000/svg'%3E%3Ccircle cx='50' cy='50' r='40' fill='none' stroke='rgba(255,255,255,0.06)' stroke-width='1'/%3E%3C/svg%3E");
  background-size: 120px 120px;
}
.hero-shapes {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}
.shape {
  position: absolute;
  border-radius: 50%;
  opacity: 0.1;
}
.shape-1 {
  width: 300px; height: 300px;
  background: radial-gradient(circle, #fff, transparent);
  top: -80px; right: -60px;
  animation: float 8s ease-in-out infinite;
}
.shape-2 {
  width: 200px; height: 200px;
  background: radial-gradient(circle, #fff, transparent);
  bottom: -40px; left: -40px;
  animation: float 10s ease-in-out infinite reverse;
}
.shape-3 {
  width: 120px; height: 120px;
  border: 2px solid rgba(255,255,255,0.15);
  top: 20%; left: 15%;
  animation: float 6s ease-in-out infinite 1s;
}
.shape-4 {
  width: 80px; height: 80px;
  border: 2px solid rgba(255,255,255,0.12);
  bottom: 25%; right: 20%;
  animation: float 7s ease-in-out infinite 0.5s;
}
@keyframes float {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-20px) rotate(5deg); }
}
.hero-content {
  position: relative;
  z-index: 1;
}
.hero-title {
  font-size: 34px;
  font-weight: 700;
  margin: 0 0 10px;
  text-shadow: 0 2px 10px rgba(0,0,0,0.1);
}
.hero-subtitle {
  font-size: 16px;
  opacity: 0.85;
  margin: 0 0 30px;
}
.hero-search {
  max-width: 520px;
  margin: 0 auto;
  display: flex;
  gap: 8px;
  background: rgba(255,255,255,0.12);
  border-radius: 12px;
  padding: 5px;
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255,255,255,0.1);
}
.hero-search-input {
  flex: 1;
  :deep(.el-input__wrapper) {
    box-shadow: none !important;
    border-radius: 8px;
    background: rgba(255,255,255,0.9);
  }
  :deep(.el-input__inner) { background: transparent; }
}
.hero-search-btn {
  border-radius: 8px;
  min-width: 80px;
  border: none;
  font-weight: 500;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 28px;
}
.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px 16px;
  text-align: center;
  border: 1px solid #f0f0f0;
  transition: all 0.2s;
  cursor: default;
}
.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0,0,0,0.06);
  border-color: var(--stat-color);
}
.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--stat-color);
  line-height: 1.2;
}
.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.pinned-card {
  background: #fff;
  border-radius: 12px;
  margin-bottom: 10px;
  border: 1px solid #fce8e6;
  border-left: 4px solid #ea4335;
  padding: 16px 20px;
  transition: box-shadow 0.2s;
}
.pinned-card:hover {
  box-shadow: 0 2px 8px rgba(234,67,53,0.08);
}
.pinned-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
.pinned-meta {
  margin-left: auto;
  color: #909399;
  font-size: 12px;
}
.pinned-content {
  font-size: 14px;
  color: #606266;
  white-space: pre-wrap;
  line-height: 1.6;
}

.modules-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
  margin-bottom: 32px;
}
.module-card {
  background: #fff;
  border-radius: 14px;
  padding: 24px 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 16px;
  border: 1px solid #f0f0f0;
  transition: all 0.25s;
}
.module-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.07);
  border-color: var(--module-color);
}
.module-icon-wrap {
  width: 54px;
  height: 54px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  font-size: 24px;
  flex-shrink: 0;
}
.module-name {
  margin: 0 0 4px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}
.module-desc {
  margin: 0;
  font-size: 13px;
  color: #909399;
}

.announcements-section {
  margin-bottom: 32px;
}
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.section-header h3 { margin: 0; font-size: 16px; font-weight: 600; }
.announcement-item {
  background: #fff;
  border-radius: 10px;
  padding: 14px 18px;
  margin-bottom: 8px;
  border: 1px solid #f0f0f0;
  transition: box-shadow 0.2s;
}
.announcement-item:hover { box-shadow: 0 2px 8px rgba(0,0,0,0.04); }
.announcement-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}
.announcement-title-row strong { font-size: 14px; }
.announcement-meta { color: #909399; font-size: 12px; white-space: nowrap; margin-left: 12px; }
.announcement-content {
  font-size: 14px;
  color: #606266;
  white-space: pre-wrap;
  line-height: 1.6;
}

@media (max-width: 640px) {
  .stats-row { grid-template-columns: repeat(2, 1fr); }
  .hero-title { font-size: 24px; }
  .modules-grid { grid-template-columns: 1fr; }
}
</style>
