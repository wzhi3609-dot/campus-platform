// Vue Router 路由配置模块：定义所有前端页面路由及懒加载组件，含全局路由守卫（需登录鉴权）
import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', name: 'Home', component: () => import('../views/home/HomePage.vue') },
  { path: '/login', name: 'Login', component: () => import('../views/auth/LoginPage.vue') },
  { path: '/register', name: 'Register', component: () => import('../views/auth/RegisterPage.vue') },
  { path: '/questions', name: 'Questions', component: () => import('../views/questions/QuestionList.vue') },
  { path: '/questions/create', name: 'CreateQuestion', component: () => import('../views/questions/CreateQuestion.vue'), meta: { requiresAuth: true } },
  { path: '/questions/:id', name: 'QuestionDetail', component: () => import('../views/questions/QuestionDetail.vue') },
  { path: '/trades', name: 'Trades', component: () => import('../views/trades/TradeList.vue') },
  { path: '/trades/create', name: 'CreateTrade', component: () => import('../views/trades/CreateTrade.vue'), meta: { requiresAuth: true } },
  { path: '/trades/:id', name: 'TradeDetail', component: () => import('../views/trades/TradeDetail.vue') },
  { path: '/lost-found', name: 'LostFound', component: () => import('../views/lostfound/LostFoundList.vue') },
  { path: '/lost-found/create', name: 'CreateLostFound', component: () => import('../views/lostfound/CreateLostFound.vue'), meta: { requiresAuth: true } },
  { path: '/lost-found/:id', name: 'LostFoundDetail', component: () => import('../views/lostfound/LostFoundDetail.vue') },
  { path: '/profile', name: 'Profile', component: () => import('../views/profile/ProfilePage.vue'), meta: { requiresAuth: true } },
  { path: '/forum', name: 'Forum', component: () => import('../views/forum/PostList.vue') },
  { path: '/forum/create', name: 'CreatePost', component: () => import('../views/forum/CreatePost.vue'), meta: { requiresAuth: true } },
  { path: '/forum/:id', name: 'PostDetail', component: () => import('../views/forum/PostDetail.vue') },
  { path: '/admin', name: 'Admin', component: () => import('../views/admin/AdminPage.vue'), meta: { requiresAuth: true, requiresRole: 'ADMIN' } },
  { path: '/admin/login', name: 'AdminLogin', component: () => import('../views/admin/AdminLogin.vue') },
  { path: '/notifications', name: 'Notifications', component: () => import('../views/notifications/NotificationList.vue'), meta: { requiresAuth: true } },
  { path: '/forgot-password', name: 'ForgotPassword', component: () => import('../views/auth/ForgotPassword.vue') },
  { path: '/:pathMatch(.*)*', name: 'NotFound', component: () => import('../views/NotFound.vue') },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if (to.meta.requiresRole) {
    const user = JSON.parse(localStorage.getItem('user') || 'null')
    if (!user || user.role !== to.meta.requiresRole) {
      next('/')
    } else {
      next()
    }
  } else {
    next()
  }
})

export default router
