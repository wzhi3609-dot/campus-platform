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
        <div style="width: 56px; height: 56px; background: linear-gradient(135deg, #34a853, #1e8e3e); border-radius: 16px; display: inline-flex; align-items: center; justify-content: center; color: #fff; font-size: 24px; font-weight: 700; margin-bottom: 12px;">R</div>
        <h2 style="margin: 0 0 4px; font-size: 22px;">创建账号</h2>
        <p style="margin: 0; color: #909399; font-size: 14px;">注册后等待管理员审核即可使用</p>
      </div>
      <el-card shadow="never" style="border: 1px solid #e4e7ed; border-radius: 12px; padding: 8px;">
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
  color: #34a853;
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
