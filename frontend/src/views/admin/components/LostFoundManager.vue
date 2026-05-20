<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../../api'

const items = ref([])
const loading = ref(false)
const total = ref(0)
const page = ref(1)

async function fetchAll() {
  loading.value = true
  try {
    const res = await request.get('/admin/lost-found', { params: { page: page.value - 1, size: 20 } })
    items.value = res.data.content
    total.value = res.data.totalElements
  } finally {
    loading.value = false
  }
}

async function togglePin(id, current) {
  await request.put(`/admin/lost-found/${id}/pin`)
  ElMessage.success(current ? '已取消置顶' : '已置顶')
  fetchAll()
}

function formatTime(t) {
  return new Date(t).toLocaleString('zh-CN')
}

onMounted(fetchAll)
</script>

<template>
  <div>
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
      <span style="font-weight: 600; font-size: 15px;">失物招领管理 ({{ total }})</span>
    </div>
    <el-table :data="items" v-loading="loading" style="width: 100%; border-radius: 8px;" stripe>
      <el-table-column label="类型" width="80">
        <template #default="{ row }">
          <el-tag :type="row.type === 'LOST' ? 'danger' : 'success'" size="small" style="border-radius: 4px;">
            {{ row.type === 'LOST' ? '寻物' : '招领' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" min-width="200" />
      <el-table-column prop="userName" label="发布者" width="100" />
      <el-table-column prop="createdAt" label="发布时间" width="150">
        <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="置顶" width="80" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.pinned" type="danger" size="small" style="border-radius: 4px;">已置顶</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button size="small" :type="row.pinned ? 'warning' : 'primary'" @click="togglePin(row.id, row.pinned)" style="border-radius: 6px;">
            {{ row.pinned ? '取消置顶' : '置顶' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <div v-if="!loading && items.length === 0" style="text-align: center; padding: 48px; color: #909399; font-size: 15px;">
      暂无失物招领记录
    </div>
    <div style="margin-top: 16px; text-align: center;">
      <el-pagination background layout="prev, pager, next" :total="total" :page-size="20" v-model:current-page="page" @current-change="fetchAll" />
    </div>
  </div>
</template>
