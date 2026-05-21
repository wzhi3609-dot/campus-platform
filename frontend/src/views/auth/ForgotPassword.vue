<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../../api'

const router = useRouter()
const userType = ref('STUDENT')
const phone = ref('')
const studentId = ref('')
const teacherId = ref('')
const name = ref('')
const loading = ref(false)
const result = ref(null)

async function handleReset() {
  if (!name.value.trim()) return ElMessage.error('请输入姓名')
  if (!phone.value.trim()) return ElMessage.error('请输入注册手机号')
  if (userType.value === 'STUDENT' && !studentId.value.trim()) return ElMessage.error('请输入学号')
  if (userType.value === 'TEACHER' && !teacherId.value.trim()) return ElMessage.error('请输入工号')
  loading.value = true
  try {
    const body = { phone: phone.value, name: name.value }
    if (userType.value === 'STUDENT') {
      body.studentId = studentId.value
    } else {
      body.teacherId = teacherId.value
    }
    const res = await request.post('/auth/reset-password', body)
    result.value = res.data.newPassword
  } catch {} finally {
    loading.value = false
  }
}
</script>

<template>
  <div style="max-width: 440px; margin: 0 auto; padding-top: 24px;">
    <div style="text-align: center; margin-bottom: 16px;">
      <div style="width: 52px; height: 52px; background: #5c6e3d; border-radius: 50%; display: inline-flex; align-items: center; justify-content: center; color: #f5f0e0; font-family: 'Noto Serif SC', serif; font-size: 20px; font-weight: 700; margin-bottom: 12px;">互</div>
    </div>
    <h2 style="margin: 0 0 4px; font-size: 22px; text-align: center; font-family: 'Noto Serif SC', serif; color: #3a402a;">找回密码</h2>
    <p style="margin: 0 0 24px; color: #8a7a6a; font-size: 13px; text-align: center;">输入注册手机号和学号/工号，获取新密码</p>
    <div style="background: #fff; border-radius: 16px; border: 1px solid #e0d5c0; padding: 28px;">
      <div v-if="!result">
        <el-tabs v-model="userType" style="margin-bottom: 16px;">
          <el-tab-pane label="我是学生" name="STUDENT" />
          <el-tab-pane label="我是教师" name="TEACHER" />
        </el-tabs>
        <el-form label-position="top">
          <el-form-item v-if="userType === 'STUDENT'" label="学号">
            <el-input v-model="studentId" placeholder="请输入学号" maxlength="30" />
          </el-form-item>
          <el-form-item v-else label="工号">
            <el-input v-model="teacherId" placeholder="请输入工号" maxlength="30" />
          </el-form-item>
          <el-form-item label="姓名">
            <el-input v-model="name" placeholder="请输入注册时使用的姓名" maxlength="50" />
          </el-form-item>
          <el-form-item label="注册手机号">
            <el-input v-model="phone" placeholder="请输入注册时使用的手机号" maxlength="11" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="loading" @click="handleReset" style="width: 100%; border-radius: 8px;">找回密码</el-button>
          </el-form-item>
        </el-form>
      </div>
      <div v-else style="text-align: center;">
        <div style="font-size: 48px; margin-bottom: 12px;">&#x2705;</div>
        <p style="font-size: 15px; color: #303133;">密码重置成功</p>
        <p style="font-size: 14px; color: #8a7a6a;">你的新密码为：</p>
        <div style="background: #f0ebe0; border-radius: 8px; padding: 16px; margin: 16px 0; font-size: 20px; font-weight: 700; letter-spacing: 2px; color: #5c6e3d;">{{ result }}</div>
        <p style="font-size: 12px; color: #e6a23c;">请牢记新密码，登录后可自行修改</p>
        <el-button type="primary" @click="router.push('/login')" style="border-radius: 8px; margin-top: 8px;">去登录</el-button>
      </div>
    </div>
    <div style="text-align: center; margin-top: 16px;">
      <el-link type="primary" :underline="false" @click="router.push('/login')">返回登录</el-link>
    </div>
  </div>
</template>
