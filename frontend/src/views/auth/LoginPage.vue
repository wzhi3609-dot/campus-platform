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
        <div style="width: 56px; height: 56px; background: linear-gradient(135deg, #1a73e8, #0d47a1); border-radius: 16px; display: inline-flex; align-items: center; justify-content: center; color: #fff; font-size: 24px; font-weight: 700; margin-bottom: 12px;">C</div>
        <h2 style="margin: 0 0 4px; font-size: 22px;">欢迎回来</h2>
        <p style="margin: 0; color: #909399; font-size: 14px;">登录校园互助平台</p>
      </div>
      <el-card shadow="never" style="border: 1px solid #e4e7ed; border-radius: 12px; padding: 8px;">
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
  gap: 8px;
  margin-bottom: 24px;
  padding: 5px;
  background: #f5f6f8;
  border-radius: 10px;
}
.type-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 10px 0;
  border: none;
  border-radius: 7px;
  background: transparent;
  color: #909399;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.25s ease;
  outline: none;
  font-family: inherit;
}
.type-btn:hover:not(.active) {
  color: #606266;
  background: rgba(255, 255, 255, 0.6);
}
.type-btn.active {
  background: #fff;
  color: #1a73e8;
  font-weight: 600;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08), 0 1px 2px rgba(0, 0, 0, 0.06);
}
.type-icon {
  font-size: 16px;
}
.bottom-link {
  text-align: center;
  color: #909399;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 2px;
}
</style>
