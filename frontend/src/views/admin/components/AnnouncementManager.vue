<script setup>
// 公告管理组件：展示已发布公告列表，支持新增公告和删除公告
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../../api'

// 公告列表（置顶公告 + 近期公告）
const announcements = ref([])
// 列表加载状态
const loading = ref(false)
// 发布公告弹窗的显示状态
const dialogVisible = ref(false)
// 公告表单数据（标题、内容、是否置顶）
const form = ref({ title: '', content: '', pinned: false })
// 发布提交加载状态
const submitting = ref(false)

// 获取所有公告（置顶 + 近期）
async function fetchAll() {
  loading.value = true
  try {
    const res = await request.get('/announcements')
    announcements.value = [...(res.data.pinned || []), ...(res.data.recent || [])]
  } finally {
    loading.value = false
  }
}

// 发布新公告（标题和内容不能为空）
async function submit() {
  if (!form.value.title.trim() || !form.value.content.trim()) {
    return ElMessage.error('标题和内容不能为空')
  }
  submitting.value = true
  try {
    await request.post('/announcements', form.value)
    ElMessage.success('发布成功')
    dialogVisible.value = false
    form.value = { title: '', content: '', pinned: false }
    fetchAll()
  } finally {
    submitting.value = false
  }
}

// 删除指定公告（含确认弹窗）
async function remove(id) {
  try {
    await ElMessageBox.confirm('确定删除该公告？', '确认')
    await request.delete(`/announcements/${id}`)
    ElMessage.success('已删除')
    fetchAll()
  } catch (e) { if (e !== 'cancel') ElMessage.error('删除失败') }
}

// 格式化时间戳为中文本地时间字符串
function formatTime(t) {
  return new Date(t).toLocaleString('zh-CN')
}

onMounted(fetchAll)
</script>

<template>
  <div>
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
      <span style="font-weight: 600; font-size: 15px;">已发布公告 ({{ announcements.length }})</span>
      <el-button type="primary" size="default" @click="dialogVisible = true" style="border-radius: 8px;">+ 发布公告</el-button>
    </div>
    <el-table :data="announcements" v-loading="loading" style="width: 100%; border-radius: 8px;" stripe>
      <el-table-column prop="title" label="标题" min-width="200" />
      <el-table-column label="内容预览" min-width="200">
        <template #default="{ row }">{{ (row.content || '').substring(0, 80) }}{{ row.content?.length > 80 ? '...' : '' }}</template>
      </el-table-column>
      <el-table-column prop="adminName" label="发布人" width="100" />
      <el-table-column prop="createdAt" label="时间" width="150">
        <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="置顶" width="60">
        <template #default="{ row }">
          <el-tag v-if="row.pinned" type="danger" size="small" style="border-radius: 4px;">是</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="80">
        <template #default="{ row }">
          <el-button size="small" type="danger" text @click="remove(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="发布公告" width="560px" :close-on-click-modal="false" style="border-radius: 12px;">
      <el-form :model="form" label-position="top">
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="公告标题" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="6" placeholder="公告内容" />
        </el-form-item>
        <el-form-item label="置顶">
          <el-switch v-model="form.pinned" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false" style="border-radius: 6px;">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit" style="border-radius: 6px;">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>
