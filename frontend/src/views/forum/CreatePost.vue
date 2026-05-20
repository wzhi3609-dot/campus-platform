<script setup>
// 发帖 / 编辑帖子页：创建新帖子或编辑已有帖子（使用 Quill 富文本编辑器）
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { QuillEditor } from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css'
import request from '../../api'

const router = useRouter()
const route = useRoute()
// 是否为编辑模式
const isEdit = !!route.query.id
// 帖子标题
const title = ref('')
// 帖子内容（HTML 格式）
const content = ref('')
// 提交加载状态
const loading = ref(false)

// 提交帖子（创建新帖子或更新已有帖子，含标题与内容校验）
async function handleSubmit() {
  if (!title.value.trim()) { ElMessage.error('请输入标题'); return }
  if (!content.value.trim()) { ElMessage.error('请输入内容'); return }
  loading.value = true
  try {
    if (isEdit) {
      await request.put(`/posts/${route.query.id}`, { title: title.value, content: content.value })
      ElMessage.success('编辑成功，等待管理员重新审核')
    } else {
      await request.post('/posts', { title: title.value, content: content.value })
      ElMessage.success('发帖成功，等待管理员审核')
    }
    router.push('/forum')
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  if (isEdit) {
    const res = await request.get(`/posts/${route.query.id}`)
    title.value = res.data.title
    content.value = res.data.content
  }
})
</script>

<template>
  <div style="max-width: 900px; margin: 0 auto;">
    <div style="margin-bottom: 24px;">
      <h2 style="margin: 0 0 4px; font-size: 22px;">{{ isEdit ? '编辑帖子' : '发帖' }}</h2>
      <p style="margin: 0; color: #909399; font-size: 13px;">发帖后需要等待管理员审核通过后公开可见</p>
    </div>
    <div style="background: #fff; border-radius: 12px; border: 1px solid #f0f0f0; padding: 24px;">
      <el-input v-model="title" placeholder="标题" size="large" style="margin-bottom: 16px;" />
      <div style="border: 1px solid #dcdfe6; border-radius: 8px; margin-bottom: 16px; overflow: hidden;">
        <QuillEditor v-model:content="content" content-type="html" theme="snow" placeholder="写下你的内容..." style="min-height: 300px;" />
      </div>
      <div style="display: flex; gap: 12px;">
        <el-button type="primary" size="large" :loading="loading" @click="handleSubmit" style="border-radius: 8px;">{{ isEdit ? '保存' : '发布' }}</el-button>
        <el-button size="large" @click="router.back()" style="border-radius: 8px;">取消</el-button>
      </div>
    </div>
  </div>
</template>
