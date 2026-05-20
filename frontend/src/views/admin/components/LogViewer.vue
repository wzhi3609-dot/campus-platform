<script setup>
// 系统日志查看组件：展示用户操作日志，含操作类型到标签颜色的映射
import { ref, onMounted } from 'vue'
import request from '../../../api'

// 日志列表数据
const logs = ref([])
// 加载状态
const loading = ref(false)
// 日志总条数
const total = ref(0)
// 当前页码
const page = ref(1)

// 获取系统日志列表（分页）
async function fetchLogs() {
  loading.value = true
  try {
    const res = await request.get('/admin/logs', { params: { page: page.value - 1, size: 50 } })
    logs.value = res.data.content
    total.value = res.data.totalElements
  } finally {
    loading.value = false
  }
}

// 格式化时间戳为中文本地时间字符串
function formatTime(t) {
  return new Date(t).toLocaleString('zh-CN')
}

// 操作类型到 Element Plus 标签类型的颜色映射表
const actionColors = {
  '注册': 'success',
  '登录': '',
  '提问': 'primary',
  '回答': 'primary',
  '采纳答案': 'warning',
  '删除问题': 'danger',
  '发布二手': 'primary',
  '更新二手状态': 'warning',
  '删除二手': 'danger',
  '发帖': 'primary',
  '删除帖子': 'danger',
  '编辑问题': 'primary',
  '编辑二手': 'primary',
  '编辑帖子': 'primary',
  '编辑失物招领': 'primary',
  '发布失物招领': 'primary',
  '解决失物招领': 'success',
  '审批通过用户': 'success',
  '驳回用户': 'danger',
  '审批通过帖子': 'success',
  '驳回帖子': 'danger',
}

// 根据操作名称获取对应的标签颜色类型
function tagType(action) {
  return actionColors[action] || ''
}

onMounted(fetchLogs)
</script>

<template>
  <div>
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
      <span style="font-weight: 600; font-size: 15px;">系统日志 ({{ total }})</span>
    </div>
    <el-table :data="logs" v-loading="loading" style="width: 100%; border-radius: 8px;" size="small" stripe>
      <el-table-column label="时间" width="150">
        <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column prop="userName" label="用户" width="80" />
      <el-table-column prop="userType" label="身份" width="60">
        <template #default="{ row }">
          <el-tag :type="row.userType === 'TEACHER' ? 'warning' : 'primary'" size="small" style="border-radius: 4px;">
            {{ row.userType === 'TEACHER' ? '教' : '学' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="110">
        <template #default="{ row }">
          <el-tag :type="tagType(row.action)" size="small" style="border-radius: 4px;">{{ row.action }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="target" label="对象" width="80" />
      <el-table-column prop="detail" label="详情" min-width="200">
        <template #default="{ row }">{{ row.detail?.substring(0, 100) }}</template>
      </el-table-column>
      <el-table-column prop="ip" label="IP" width="120" />
    </el-table>
    <div style="margin-top: 16px; text-align: center;">
      <el-pagination background layout="prev, pager, next" :total="total" :page-size="50" v-model:current-page="page" @current-change="fetchLogs" />
    </div>
  </div>
</template>
