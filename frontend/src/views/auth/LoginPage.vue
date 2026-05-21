<script setup>
// 登录页组件：支持学生/教师身份切换登录
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'

const router = useRouter()
const auth = useAuthStore()
// 当前选中的用户身份类型（STUDENT / TEACHER）
const userType = ref('STUDENT')
// 登录表单数据（账号、密码、身份类型）
const form = reactive({ account: '', password: '', userType: 'STUDENT' })
// 登录按钮加载状态
const loading = ref(false)

// 切换用户身份类型时同步更新表单数据
function onTypeChange(val) {
  userType.value = val
  form.userType = val
}

// 执行登录操作，成功后跳转首页
async function handleLogin() {
  loading.value = true
  try {
    await auth.login(form)
    router.push('/')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div style="min-height: calc(100vh - 200px); display: flex; align-items: center; justify-content: center;">
    <div style="width: 400px;">
      <div style="text-align: center; margin-bottom: 32px;">
        <div style="width: 52px; height: 52px; background: #5c6e3d; border-radius: 50%; display: inline-flex; align-items: center; justify-content: center; color: #f5f0e0; font-family: 'Noto Serif SC', serif; font-size: 20px; font-weight: 700; margin-bottom: 12px;">互</div>
        <h2 style="margin: 0 0 4px; font-family: 'Noto Serif SC', serif; font-size: 20px; color: #3a402a;">欢迎回来</h2>
        <p style="margin: 0; color: #8a7a6a; font-size: 14px;">登录校园互助平台</p>
      </div>
      <el-card shadow="never" style="border: 1px solid #e0d5c0; border-radius: 16px; padding: 8px;">
        <div class="login-type-switch">
          <button :class="['type-btn', { active: userType === 'STUDENT' }]" @click="onTypeChange('STUDENT')">
            <span class="type-icon">🎓</span>
            <span>学生登录</span>
          </button>
          <button :class="['type-btn', { active: userType === 'TEACHER' }]" @click="onTypeChange('TEACHER')">
            <span class="type-icon">🏫</span>
            <span>教师登录</span>
          </button>
        </div>
        <el-form :model="form" label-width="0" @submit.prevent="handleLogin">
          <el-form-item>
            <el-input v-model="form.account" :placeholder="userType === 'STUDENT' ? '学号' : '工号'" size="large" />
          </el-form-item>
          <el-form-item>
            <el-input v-model="form.password" type="password" placeholder="密码" size="large" show-password />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" size="large" style="width: 100%; border-radius: 8px;" :loading="loading" native-type="submit">登录</el-button>
          </el-form-item>
        </el-form>
        <div class="bottom-link">
          还没有账号？<el-link type="primary" @click="router.push('/register')">立即注册</el-link>
        </div>
        <div style="text-align: center; margin-top: 12px;">
          <el-link type="info" :underline="false" @click="router.push('/forgot-password')">忘记密码？</el-link>
        </div>
      </el-card>
    </div>
  </div>
  </template>

<style scoped>
.login-type-switch {
  display: flex;
  background: #f0ebe0;
  border-radius: 12px;
  padding: 3px;
  margin-bottom: 20px;
}
.type-btn {
  flex: 1;
  padding: 8px;
  border: none;
  background: transparent;
  border-radius: 10px;
  cursor: pointer;
  font-size: 14px;
  color: #8a7a6a;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}
.type-btn.active {
  background: #fff;
  color: #3a402a;
  font-weight: 600;
  box-shadow: 0 1px 3px rgba(60,40,20,0.08);
}
.type-icon { font-size: 16px; }
.bottom-link {
  text-align: center;
  color: #8a7a6a;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 2px;
}
</style>
