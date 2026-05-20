<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../../api'

const reports = ref([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)

async function fetchReports() {
  loading.value = true
  try {
    const res = await request.get('/admin/reports/pending', { params: { page: page.value - 1, size: 20 } })
    reports.value = res.data.content
    total.value = res.data.totalElements
  } finally {
    loading.value = false
  }
}

async function handleResolve(id) {
  try {
    await ElMessageBox.confirm('标记为已处理？', '确认')
    await request.put(`/admin/reports/${id}/resolve`)
    ElMessage.success('已处理')
    fetchReports()
  } catch {}
}

function formatTime(t) {
  return new Date(t).toLocaleString('zh-CN')
}

onMounted(fetchReports)
</script>

<template>
  <div v-loading="loading">
    <el-table :data="reports" style="width: 100%;" empty-text="暂无待处理举报">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column label="目标类型" width="80">
        <template #default="{ row }">
          <el-tag size="small" style="border-radius: 4px;">{{ row.targetType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="targetId" label="目标ID" width="80" />
      <el-table-column prop="reason" label="举报原因" min-width="200" show-overflow-tooltip />
      <el-table-column label="时间" width="160">
        <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="handleResolve(row.id)" style="border-radius: 6px;">已处理</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div v-if="total > 20" style="margin-top: 16px; text-align: center;">
      <el-pagination background layout="prev, pager, next" :total="total" :page-size="20" v-model:current-page="page" @current-change="fetchReports" />
    </div>
  </div>
</template>
