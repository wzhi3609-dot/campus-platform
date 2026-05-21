<script setup>
// 注册页组件：学生/教师注册，含手机号格式校验
import { reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'

const router = useRouter()
const auth = useAuthStore()
// 当前选中的用户身份类型
const userType = ref('STUDENT')
// 注册表单数据（身份、学号/工号、姓名、密码、邮箱、手机号）
const form = reactive({ userType: 'STUDENT', studentId: '', teacherId: '', name: '', password: '', email: '', phone: '' })
// 注册按钮加载状态
const loading = ref(false)

// 切换身份类型时清空对应的账号字段
function onTypeChange(val) {
  userType.value = val
  form.userType = val
  if (val === 'STUDENT') form.teacherId = ''
  else form.studentId = ''
}

// 执行注册操作，提交前校验手机号格式
async function handleRegister() {
  if (!/^1[3-9]\d{9}$/.test(form.phone)) {
    ElMessage.error('请输入正确的11位手机号')
    return
  }
  loading.value = true
  try {
    await auth.register(form)
    ElMessage.success('注册成功，等待管理员审核')
    router.push('/login')
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div style="min-height: calc(100vh - 200px); display: flex; align-items: center; justify-content: center;">
    <div style="width: 420px;">
      <div style="text-align: center; margin-bottom: 28px;">
        <div style="width: 52px; height: 52px; background: #5c6e3d; border-radius: 50%; display: inline-flex; align-items: center; justify-content: center; color: #f5f0e0; font-family: 'Noto Serif SC', serif; font-size: 20px; font-weight: 700; margin-bottom: 12px;">互</div>
        <h2 style="margin: 0 0 4px; font-family: 'Noto Serif SC', serif; font-size: 20px; color: #3a402a;">创建账号</h2>
        <p style="margin: 0; color: #8a7a6a; font-size: 14px;">注册后等待管理员审核即可使用</p>
      </div>
      <el-card shadow="never" style="border: 1px solid #e0d5c0; border-radius: 16px; padding: 8px;">
        <div class="register-type-switch">
          <button :class="['type-btn', { active: userType === 'STUDENT' }]" @click="onTypeChange('STUDENT')">
            <span class="type-icon">🎓</span>
            <span>学生注册</span>
          </button>
          <button :class="['type-btn', { active: userType === 'TEACHER' }]" @click="onTypeChange('TEACHER')">
            <span class="type-icon">🏫</span>
            <span>教师注册</span>
          </button>
        </div>
        <el-form :model="form" label-width="0" @submit.prevent="handleRegister">
          <el-form-item v-if="userType === 'STUDENT'">
            <el-input v-model="form.studentId" placeholder="学号" size="large" />
          </el-form-item>
          <el-form-item v-else>
            <el-input v-model="form.teacherId" placeholder="工号" size="large" />
          </el-form-item>
          <el-form-item><el-input v-model="form.name" placeholder="姓名" size="large" /></el-form-item>
          <el-form-item><el-input v-model="form.password" type="password" placeholder="密码" size="large" show-password /></el-form-item>
          <el-form-item><el-input v-model="form.email" placeholder="邮箱（选填）" size="large" /></el-form-item>
          <el-form-item><el-input v-model="form.phone" placeholder="手机号" size="large" maxlength="11" /></el-form-item>
          <el-form-item>
            <el-button type="primary" size="large" style="width: 100%; border-radius: 8px;" :loading="loading" native-type="submit">注册</el-button>
          </el-form-item>
        </el-form>
        <div class="bottom-link">
          已有账号？<el-link type="primary" @click="router.push('/login')">立即登录</el-link>
        </div>
      </el-card>
    </div>
  </div>
  </template>

<style scoped>
.register-type-switch {
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
