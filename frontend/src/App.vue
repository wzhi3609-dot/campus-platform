<script setup>
// 根组件：全局布局（顶部导航栏 + 路由视图），管理未读消息数与用户登录状态
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useAuthStore } from './stores/auth'
import { useRouter, useRoute } from 'vue-router'
import request from './api'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()
// 未读通知数量
const unreadCount = ref(0)
let unreadTimer = null

// 根据当前路由路径计算导航栏高亮菜单项索引
const activeMenuIndex = computed(() => {
  const p = route.path
  if (p.startsWith('/questions')) return '1'
  if (p.startsWith('/trades')) return '2'
  if (p.startsWith('/forum')) return '3'
  if (p.startsWith('/lost-found')) return '4'
  return ''
})

// 获取未读通知数量
async function fetchUnread() {
  if (!auth.isLoggedIn) return
  try {
    const res = await request.get('/notifications/unread')
    unreadCount.value = res.data.count
  } catch {
    unreadCount.value = 0
  }
}

// 退出登录：清除认证状态并跳转至首页
function handleLogout() {
  auth.logout()
  router.push('/')
}

onMounted(() => {
  fetchUnread()
  unreadTimer = setInterval(fetchUnread, 30000)
})

onUnmounted(() => {
  if (unreadTimer) clearInterval(unreadTimer)
})
</script>

<template>
  <div class="app-wrapper">
    <el-header class="app-header">
      <div class="header-inner">
        <router-link to="/" class="header-logo">
          <span class="logo-icon">互</span>
          <span class="logo-text">校园互助平台</span>
        </router-link>
        <el-menu mode="horizontal" :ellipsis="false" class="header-nav" :default-active="activeMenuIndex">
          <el-menu-item index="1" @click="router.push('/questions')">问答</el-menu-item>
          <el-menu-item index="2" @click="router.push('/trades')">二手</el-menu-item>
          <el-menu-item index="3" @click="router.push('/forum')">论坛</el-menu-item>
          <el-menu-item index="4" @click="router.push('/lost-found')">失物招领</el-menu-item>
        </el-menu>
        <div class="header-actions">
          <template v-if="auth.isLoggedIn">
            <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notif-badge">
              <el-button text size="large" @click="router.push('/notifications')" class="notif-btn">🔔</el-button>
            </el-badge>
            <el-dropdown trigger="click">
              <el-button text class="user-btn">
                <el-avatar :size="28" :src="auth.user?.avatar" class="user-avatar">{{ auth.user?.name?.[0] }}</el-avatar>
                <span class="user-name">{{ auth.user?.name }}</span>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="router.push('/profile')">个人中心</el-dropdown-item>
                  <el-dropdown-item v-if="auth.user?.role === 'ADMIN'" @click="router.push('/admin')">管理后台</el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button size="small" @click="router.push('/login')" class="action-btn">登录</el-button>
            <el-button size="small" type="primary" @click="router.push('/register')" class="action-btn">注册</el-button>
            <el-link type="danger" :underline="false" class="admin-link" @click="router.push('/admin/login')">管理登录</el-link>
          </template>
        </div>
      </div>
    </el-header>
    <el-main class="app-main">
      <router-view />
    </el-main>
  </div>
</template>

<style>
body {
  margin: 0;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, "Noto Sans SC", sans-serif;
  -webkit-font-smoothing: antialiased;
}
.app-wrapper {
  min-height: 100vh;
  background: #f5f0e8;
  position: relative;
}
.app-header {
  padding: 0 !important;
  background: rgba(255,255,255,0.88);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  box-shadow: 0 1px 0 rgba(60,40,20,0.06), 0 1px 4px rgba(60,40,20,0.04);
  position: sticky;
  top: 0;
  z-index: 100;
  border-bottom: 1px solid #e0d5c0;
}
.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  height: 60px;
  padding: 0 24px;
  position: relative;
}
.header-logo {
  text-decoration: none;
  display: flex;
  align-items: center;
  gap: 10px;
  margin-right: 32px;
}
.logo-icon {
  width: 36px;
  height: 36px;
  background: #5c6e3d;
  color: #f5f0e0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  font-family: 'Noto Serif SC', serif;
  font-size: 16px;
  font-weight: 700;
}
.logo-text {
  color: #3a402a;
  font-family: 'Noto Serif SC', serif;
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 1px;
}
.header-nav {
  flex: 1;
  border-bottom: none !important;
  background: transparent !important;
}
.header-nav .el-menu-item {
  height: 60px;
  line-height: 60px;
  font-size: 14px;
  font-weight: 500;
  color: #6a604a;
  transition: color 0.2s;
}
.header-nav .el-menu-item:hover {
  color: #5c6e3d;
  background: transparent !important;
}
.header-nav .el-menu-item.is-active {
  color: #5c6e3d;
  border-bottom-color: #5c6e3d;
  font-weight: 600;
}
.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}
.notif-badge { margin-top: 4px; }
.notif-btn {
  font-size: 18px;
  padding: 4px;
  transition: transform 0.2s;
}
.notif-btn:hover { transform: scale(1.1); }
.user-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px !important;
  border-radius: 20px !important;
  transition: background 0.2s;
}
.user-btn:hover { background: #f0ebe0 !important; }
.user-avatar { background: #5c6e3d; flex-shrink: 0; }
.user-name {
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 14px;
  color: #3a4030;
}
.action-btn { border-radius: 20px !important; }
.admin-link { font-size: 13px; margin-left: 4px; }
.app-main {
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
  padding: 24px;
  position: relative;
  z-index: 1;
  min-height: calc(100vh - 60px);
}
.el-card { border-radius: 14px; border: 1px solid #e0d5c0; box-shadow: 0 1px 2px rgba(60,40,20,0.06); transition: box-shadow 0.2s; }
.el-card:hover { box-shadow: 0 2px 8px rgba(60,40,20,0.08); }
.el-button { border-radius: 14px; }
.el-button--primary { --el-button-bg-color: #5c6e3d; --el-button-border-color: #5c6e3d; --el-button-hover-bg-color: #4a5c2e; --el-button-hover-border-color: #4a5c2e; --el-button-active-bg-color: #3a4a20; --el-button-active-border-color: #3a4a20; }
.el-table { border-radius: 14px; overflow: hidden; }
.el-pagination { --el-pagination-button-radius: 8px; }
.el-pagination .el-pager li.is-active { background-color: #5c6e3d; }
.el-dialog { border-radius: 16px; }
.el-tabs__item { font-size: 14px; font-weight: 500; }
.el-tag { border-radius: 4px; }
.el-input__wrapper { border-radius: 10px; box-shadow: 0 0 0 1px #e0d5c0 inset; }
.el-input__wrapper:hover { box-shadow: 0 0 0 1px #c4b8a0 inset; }
.el-input__wrapper.is-focus { box-shadow: 0 0 0 1px #5c6e3d inset; }
.el-divider { border-color: #e8e0d0; }
.el-menu { --el-menu-text-color: #6a604a; --el-menu-hover-text-color: #5c6e3d; --el-menu-active-color: #5c6e3d; }
.el-dropdown-menu__item:hover { background-color: #f0ebe0; color: #5c6e3d; }
</style>
