<script setup>
// 个人中心页：查看和编辑个人信息、修改密码、上传头像
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../api'
import { useAuthStore } from '../../stores/auth'

const auth = useAuthStore()

// 个人信息表单（姓名、手机号、邮箱、头像 URL）
const form = ref({
  name: '',
  phone: '',
  email: '',
  avatar: '',
})

// 修改密码表单（原密码、新密码、确认密码）
const passwordForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })
// 保存个人信息加载状态
const saving = ref(false)
// 修改密码加载状态
const changingPwd = ref(false)
// 上传头像加载状态
const uploading = ref(false)

// 获取当前登录用户的个人信息
async function fetchProfile() {
  try {
    const res = await request.get('/users/me')
    form.value.name = res.data.name
    form.value.phone = res.data.phone || ''
    form.value.email = res.data.email || ''
    form.value.avatar = res.data.avatar || ''
  } catch {}
}

// 处理头像上传（校验文件格式与大小，上传至服务器）
async function handleAvatarUpload(rawFile) {
  const f = rawFile?.raw || rawFile
  if (!f) return
  const isImg = f.type.startsWith('image/')
  const isLt5M = f.size / 1024 / 1024 < 5
  if (!isImg) return ElMessage.error('只能上传图片文件')
  if (!isLt5M) return ElMessage.error('头像图片不能超过 5MB')
  uploading.value = true
  try {
    const fd = new FormData()
    fd.append('file', f)
    const res = await request.post('/upload/avatar', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
    form.value.avatar = res.data
  } finally {
    uploading.value = false
  }
}

// 保存个人基本信息
async function save() {
  saving.value = true
  try {
    const res = await request.put('/users/profile', form.value)
    auth.user = res.data
    localStorage.setItem('user', JSON.stringify(res.data))
    ElMessage.success('保存成功')
  } finally {
    saving.value = false
  }
}

// 修改密码（校验原密码、新密码长度与两次输入一致性）
async function changePassword() {
  const { oldPassword, newPassword, confirmPassword } = passwordForm.value
  if (!oldPassword || !newPassword) return ElMessage.error('请填写完整')
  if (newPassword.length < 6) return ElMessage.error('新密码至少6位')
  if (newPassword !== confirmPassword) return ElMessage.error('两次密码不一致')
  changingPwd.value = true
  try {
    await request.put('/users/password', { oldPassword, newPassword })
    ElMessage.success('密码修改成功')
    passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
  } finally {
    changingPwd.value = false
  }
}

onMounted(fetchProfile)
</script>

<template>
  <div style="max-width: 640px; margin: 0 auto;">
    <div style="margin-bottom: 24px;">
      <h2 style="margin: 0 0 4px; font-size: 22px;">个人中心</h2>
      <p style="margin: 0; color: #8a7a6a; font-size: 13px;">管理你的个人信息和账号</p>
    </div>

    <div style="background: #fff; border-radius: 12px; border: 1px solid #e0d5c0; padding: 28px; margin-bottom: 20px;">
      <div style="display: flex; align-items: center; gap: 20px; margin-bottom: 24px; padding-bottom: 20px; border-bottom: 1px solid #e0d5c0;">
        <el-upload class="avatar-uploader" :show-file-list="false"
          :before-upload="(f) => { handleAvatarUpload({ raw: f }); return false }">
          <el-avatar :size="72" :src="form.avatar" style="cursor: pointer; border: 3px solid #e8f0fe;">
            {{ auth.user?.name?.[0] }}
          </el-avatar>
          <div style="font-size: 12px; color: #8a7a6a; text-align: center; margin-top: 4px;">点击更换</div>
        </el-upload>
        <div>
          <div style="font-size: 18px; font-weight: 600;">{{ auth.user?.name }}</div>
          <div style="display: flex; gap: 8px; margin-top: 4px;">
            <el-tag :type="auth.user?.userType === 'TEACHER' ? 'warning' : 'primary'" size="small" style="border-radius: 4px;">
              {{ auth.user?.userType === 'TEACHER' ? '教师' : '学生' }}
            </el-tag>
          </div>
        </div>
      </div>

      <el-form label-position="top">
        <el-form-item label="账号">
          <el-input :model-value="auth.user?.username" disabled />
        </el-form-item>
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px;">
          <el-form-item label="学号/工号">
            <el-input :model-value="auth.user?.studentId || auth.user?.teacherId || ''" disabled />
          </el-form-item>
          <el-form-item label="昵称">
            <el-input v-model="form.name" placeholder="请输入昵称" maxlength="50" />
          </el-form-item>
        </div>
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px;">
          <el-form-item label="手机号">
            <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="form.email" placeholder="请输入邮箱" maxlength="100" />
          </el-form-item>
        </div>
        <el-form-item>
          <el-button type="primary" @click="save" :loading="saving" style="border-radius: 8px;">保存</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div style="background: #fff; border-radius: 12px; border: 1px solid #e0d5c0; padding: 28px;">
      <div style="font-size: 18px; font-weight: 600; margin-bottom: 20px;">修改密码</div>
      <el-form label-position="top">
        <el-form-item label="原密码">
          <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入原密码" />
        </el-form-item>
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px;">
          <el-form-item label="新密码">
            <el-input v-model="passwordForm.newPassword" type="password" placeholder="至少6位" />
          </el-form-item>
          <el-form-item label="确认密码">
            <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="再次输入新密码" />
          </el-form-item>
        </div>
        <el-form-item>
          <el-button type="primary" @click="changePassword" :loading="changingPwd" style="border-radius: 8px;">修改密码</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.avatar-uploader { display: flex; flex-direction: column; align-items: center; }
</style>
