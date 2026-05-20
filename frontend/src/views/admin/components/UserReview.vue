<script setup>
// 用户审核组件：展示待审核用户列表，支持通过 / 驳回操作
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../../api'

// 待审核用户列表
const pendingUsers = ref([])
// 列表加载状态
const loading = ref(false)
// 待审核用户总条数
const total = ref(0)
// 当前页码
const page = ref(1)

// 获取待审核用户列表（分页）
async function fetchPending() {
  loading.value = true
  try {
    const res = await request.get('/admin/users/pending', { params: { page: page.value - 1, size: 20 } })
    pendingUsers.value = res.data.content
    total.value = res.data.totalElements
  } finally {
    loading.value = false
  }
}

// 通过指定用户的注册审核
async function approve(id, name) {
  try {
    await request.put(`/admin/users/${id}/approve`)
    ElMessage.success(`已通过: ${name}`)
    fetchPending()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

// 驳回指定用户的注册审核（含确认弹窗）
async function reject(id, name) {
  try {
    await ElMessageBox.confirm(`确定驳回 ${name} 的注册申请？`, '确认')
    await request.put(`/admin/users/${id}/reject`)
    ElMessage.success(`已驳回: ${name}`)
    fetchPending()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

// 格式化时间戳为中文本地时间字符串
function formatTime(t) {
  return new Date(t).toLocaleString('zh-CN')
}

onMounted(fetchPending)
</script>

<template>
  <div>
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
      <span style="font-weight: 600; font-size: 15px;">待审核用户 ({{ total }})</span>
    </div>
    <el-table :data="pendingUsers" v-loading="loading" style="width: 100%; border-radius: 8px;" stripe>
      <el-table-column prop="name" label="姓名" width="100" />
      <el-table-column prop="userType" label="身份" width="80">
        <template #default="{ row }">
          <el-tag :type="row.userType === 'TEACHER' ? 'warning' : 'primary'" size="small" style="border-radius: 4px;">
            {{ row.userType === 'TEACHER' ? '教师' : '学生' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="学号/工号" width="120">
        <template #default="{ row }">{{ row.studentId || row.teacherId }}</template>
      </el-table-column>
      <el-table-column prop="email" label="邮箱" width="180" />
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column prop="createdAt" label="注册时间" width="160">
        <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button size="small" type="success" @click="approve(row.id, row.name)" style="border-radius: 6px;">通过</el-button>
          <el-button size="small" type="danger" @click="reject(row.id, row.name)" style="border-radius: 6px;">驳回</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div v-if="!loading && pendingUsers.length === 0" style="text-align: center; padding: 48px; color: #909399; font-size: 15px;">
      ✅ 暂无待审核用户
    </div>
    <div style="margin-top: 16px; text-align: center;">
      <el-pagination background layout="prev, pager, next" :total="total" :page-size="20"
        v-model:current-page="page" @current-change="fetchPending" />
    </div>
  </div>
</template>
