<script setup>
// 提问 / 编辑问题页：创建新问题或编辑已有问题
import { reactive, ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import request from '../../api'

const router = useRouter()
const route = useRoute()
// 是否为编辑模式（通过 URL 参数 id 判断）
const isEdit = !!route.query.id
// 问题表单数据（标题、内容、标签）
const form = reactive({ title: '', content: '', tags: '' })
// 提交加载状态
const loading = ref(false)

// 提交问题（创建新问题或更新已有问题）
async function handleSubmit() {
  loading.value = true
  try {
    if (isEdit) {
      await request.put(`/questions/${route.query.id}`, form)
    } else {
      await request.post('/questions', form)
    }
    router.push('/questions')
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  if (isEdit) {
    const res = await request.get(`/questions/${route.query.id}`)
    form.title = res.data.title
    form.content = res.data.content
    form.tags = res.data.tags || ''
  }
})
</script>

<template>
  <div style="max-width: 800px; margin: 0 auto;">
    <div style="margin-bottom: 24px;">
      <h2 style="margin: 0 0 4px; font-size: 22px;">{{ isEdit ? '编辑问题' : '提问' }}</h2>
      <p style="margin: 0; color: #909399; font-size: 13px;">描述要清晰，方便他人帮助你</p>
    </div>
    <div style="background: #fff; border-radius: 12px; border: 1px solid #f0f0f0; padding: 28px;">
      <el-form :model="form" label-position="top" @submit.prevent="handleSubmit">
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="请简要描述你的问题" size="large" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="8" placeholder="请详细描述你的问题..." />
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="form.tags" placeholder="多个标签用逗号分隔，如：Java,学习" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" native-type="submit" style="border-radius: 8px;">{{ isEdit ? '保存' : '发布问题' }}</el-button>
          <el-button size="large" @click="router.back()" style="border-radius: 8px;">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>
