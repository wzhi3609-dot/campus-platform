<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'

const router = useRouter()
const auth = useAuthStore()
const form = reactive({ username: 'ADMIN001', password: '' })
const loading = ref(false)

async function handleLogin() {
  loading.value = true
  try {
    await auth.adminLogin(form)
    router.push('/admin')
  } catch (e) {
    loading.value = false
  }
}
</script>

<template>
  <div style="min-height: calc(100vh - 200px); display: flex; align-items: center; justify-content: center;">
    <div style="width: 380px;">
      <div style="text-align: center; margin-bottom: 28px;">
        <div style="width: 56px; height: 56px; background: linear-gradient(135deg, #ea4335, #c62828); border-radius: 16px; display: inline-flex; align-items: center; justify-content: center; color: #fff; font-size: 24px; font-weight: 700; margin-bottom: 12px;">A</div>
        <h2 style="margin: 0 0 4px; font-size: 22px;">管理后台</h2>
        <p style="margin: 0; color: #909399; font-size: 14px;">管理员专用登录</p>
      </div>
      <el-card shadow="never" style="border: 1px solid #e4e7ed; border-radius: 12px; padding: 16px;">
        <el-form :model="form" @submit.prevent="handleLogin">
          <el-form-item>
            <el-input v-model="form.username" placeholder="管理员账号" size="large" />
          </el-form-item>
          <el-form-item>
            <el-input v-model="form.password" type="password" placeholder="密码" size="large" show-password />
          </el-form-item>
          <el-form-item>
            <el-button type="danger" size="large" style="width: 100%; border-radius: 8px;" :loading="loading" native-type="submit">管理员登录</el-button>
          </el-form-item>
        </el-form>
      </el-card>
      <div style="text-align: center; margin-top: 16px;">
        <el-link type="primary" @click="router.push('/login')">← 返回普通登录</el-link>
      </div>
    </div>
  </div>
</template>
