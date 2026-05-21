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
  { path: '/questions', name: '问答互助', desc: '提问 · 解答 · 交流', icon: '💬', color: '#4a6a8a', bg: '#e6edf3' },
  { path: '/trades', name: '二手交易', desc: '转让 · 求购 · 交换', icon: '🏪', color: '#5a7a4a', bg: '#e6ede4' },
  { path: '/forum', name: '校园论坛', desc: '分享 · 讨论 · 记录', icon: '📝', color: '#9a6a30', bg: '#f5ede0' },
  { path: '/lost-found', name: '失物招领', desc: '遗失 · 寻找 · 归还', icon: '🔍', color: '#8a4a4a', bg: '#f3e6e6' },
]

// 统计数据卡片配置（标签、对应字段名、颜色）
const statCards = [
  { label: '累计问答', key: 'questions', color: '#4a6a8a' },
  { label: '交易物品', key: 'trades', color: '#5a7a4a' },
  { label: '论坛帖子', key: 'posts', color: '#9a6a30' },
  { label: '失物招领', key: 'lostFound', color: '#8a4a4a' },
]

onMounted(() => { fetchStats(); fetchAnnouncements() })
</script>

<template>
  <div>
    <div class="hero-section">
      <div class="hero-content">
        <h1 class="hero-title">校园互助，温暖相伴</h1>
        <p class="hero-subtitle">知识共享 · 资源流转 · 社区互助 — 你的校园生活助手</p>
        <div class="hero-search">
          <el-input v-model="keyword" placeholder="全局搜索问题、商品、帖子..." size="large" clearable @keyup.enter="doSearch"
            class="hero-search-input" />
          <el-button size="large" :loading="searching" @click="doSearch" class="hero-search-btn">搜索</el-button>
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
          <div style="font-size: 12px; color: #4a6a8a; font-weight: 600; margin: 8px 0 4px;">💬 问答</div>
          <div v-for="q in searchResults.questions" :key="q.id" style="cursor: pointer; padding: 6px 0; border-bottom: 1px solid #e8e0d0; font-size: 14px;" @click="router.push(`/questions/${q.id}`)">• {{ q.title }}</div>
        </div>
        <div v-if="searchResults.trades?.length">
          <div style="font-size: 12px; color: #5a7a4a; font-weight: 600; margin: 8px 0 4px;">🏪 二手</div>
          <div v-for="t in searchResults.trades" :key="t.id" style="cursor: pointer; padding: 6px 0; border-bottom: 1px solid #e8e0d0; font-size: 14px;" @click="router.push(`/trades/${t.id}`)">• {{ t.title }}</div>
        </div>
        <div v-if="searchResults.posts?.length">
          <div style="font-size: 12px; color: #9a6a30; font-weight: 600; margin: 8px 0 4px;">📝 论坛</div>
          <div v-for="p in searchResults.posts" :key="p.id" style="cursor: pointer; padding: 6px 0; border-bottom: 1px solid #e8e0d0; font-size: 14px;" @click="router.push(`/forum/${p.id}`)">• {{ p.title }}</div>
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
          <el-tag size="small" style="background-color: #8a4a4a; color: #fff; border: none; border-radius: 4px;">置顶</el-tag>
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
  color: #f5f0e0;
  background: linear-gradient(135deg, #5c6e3d 0%, #7a8a5a 40%, #6a7a4a 100%);
}
.hero-section::before {
  content: '';
  position: absolute;
  top: -80px; right: -60px;
  width: 220px; height: 220px;
  border: 1px solid rgba(255,255,255,0.06);
  border-radius: 50%;
  pointer-events: none;
}
.hero-section::after {
  content: '';
  position: absolute;
  bottom: -40px; left: -40px;
  width: 160px; height: 160px;
  border: 1px solid rgba(255,255,255,0.05);
  border-radius: 50%;
  pointer-events: none;
}
.hero-content {
  position: relative;
  z-index: 1;
}
.hero-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 30px;
  font-weight: 700;
  margin: 0 0 8px;
  letter-spacing: 3px;
}
.hero-subtitle {
  font-size: 14px;
  opacity: 0.7;
  margin: 0 0 28px;
}
.hero-search {
  max-width: 520px;
  margin: 0 auto;
  display: flex;
  gap: 8px;
  background: rgba(255,255,255,0.10);
  border-radius: 14px;
  padding: 5px;
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255,255,255,0.12);
}
.hero-search-input {
  flex: 1;
}
.hero-search-input :deep(.el-input__wrapper) {
  box-shadow: none !important;
  border-radius: 10px;
  background: rgba(255,255,255,0.88);
  border: 1px solid transparent;
}
.hero-search-input :deep(.el-input__wrapper:hover) {
  border-color: transparent;
}
.hero-search-input :deep(.el-input__inner) { background: transparent; }
.hero-search-btn {
  border-radius: 14px;
  min-width: 80px;
  border: none;
  font-weight: 500;
  background: rgba(255,255,255,0.15);
  color: #f5f0e0;
  border: 1px solid rgba(255,255,255,0.2);
}
.hero-search-btn:hover {
  background: rgba(255,255,255,0.25);
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 28px;
}
.stat-card {
  background: #fff;
  border-radius: 14px;
  padding: 18px 14px;
  text-align: center;
  border: 1px solid #e0d5c0;
  transition: all 0.2s;
  cursor: default;
}
.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(60,40,20,0.08);
  border-color: var(--stat-color);
}
.stat-value {
  font-family: 'Noto Serif SC', serif;
  font-size: 24px;
  font-weight: 700;
  color: var(--stat-color);
  line-height: 1.2;
}
.stat-label {
  font-size: 12px;
  color: #8a7a6a;
  margin-top: 4px;
}

.pinned-card {
  background: #fff;
  border-radius: 14px;
  margin-bottom: 10px;
  border: 1px solid #e8d8d8;
  border-left: 4px solid #8a4a4a;
  padding: 16px 20px;
  transition: box-shadow 0.2s;
}
.pinned-card:hover {
  box-shadow: 0 2px 8px rgba(138,74,74,0.08);
}
.pinned-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
.pinned-meta {
  margin-left: auto;
  color: #8a7a6a;
  font-size: 12px;
}
.pinned-content {
  font-size: 14px;
  color: #4a4030;
  white-space: pre-wrap;
  line-height: 1.6;
}

.modules-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 12px;
  margin-bottom: 32px;
}
.module-card {
  background: #fff;
  border-radius: 14px;
  padding: 22px 18px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 14px;
  border: 1px solid #e0d5c0;
  transition: all 0.25s;
}
.module-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(60,40,20,0.08);
  border-color: var(--module-color);
}
.module-icon-wrap {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  font-size: 22px;
  flex-shrink: 0;
}
.module-name {
  margin: 0 0 2px;
  font-family: 'Noto Serif SC', serif;
  font-size: 16px;
  font-weight: 700;
  color: #3a4030;
}
.module-desc {
  margin: 0;
  font-size: 12px;
  color: #8a7a6a;
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
.section-header h3 { margin: 0; font-family: 'Noto Serif SC', serif; font-size: 16px; font-weight: 600; }
.announcement-item {
  background: #fff;
  border-radius: 14px;
  padding: 14px 18px;
  margin-bottom: 8px;
  border: 1px solid #e0d5c0;
  transition: box-shadow 0.2s;
}
.announcement-item:hover { box-shadow: 0 2px 6px rgba(60,40,20,0.06); }
.announcement-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}
.announcement-title-row strong { font-size: 14px; color: #3a4030; }
.announcement-meta { color: #8a7a6a; font-size: 12px; white-space: nowrap; margin-left: 12px; }
.announcement-content {
  font-size: 14px;
  color: #4a4030;
  white-space: pre-wrap;
  line-height: 1.6;
}

@media (max-width: 640px) {
  .stats-row { grid-template-columns: repeat(2, 1fr); }
  .hero-title { font-size: 22px; }
  .modules-grid { grid-template-columns: 1fr; }
}
</style>
