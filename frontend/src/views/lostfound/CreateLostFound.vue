<script setup>
// 发布 / 编辑失物招领信息页：创建或编辑失物 / 招领信息
import { reactive, ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../../api'

const router = useRouter()
const route = useRoute()
const isEdit = !!route.query.id
const form = reactive({ type: 'LOST', title: '', description: '', location: '', contactPerson: '', contactPhone: '', image: '' })
const loading = ref(false)
const uploading = ref(false)

async function handleSubmit() {
  loading.value = true
  try {
    if (isEdit) {
      await request.put(`/lost-found/${route.query.id}`, form)
    } else {
      await request.post('/lost-found', form)
    }
    router.push('/lost-found')
  } finally {
    loading.value = false
  }
}

async function handleUpload(options) {
  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('files', options.file)
    const res = await request.post('/upload/images', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    const urls = res.data
    if (urls && urls.length > 0) {
      form.image = urls[0]
    }
    ElMessage.success('上传成功')
  } catch (e) {
    ElMessage.error('上传失败')
  } finally {
    uploading.value = false
  }
}

onMounted(async () => {
  if (isEdit) {
    const res = await request.get(`/lost-found/${route.query.id}`)
    Object.assign(form, res.data)
  }
})
</script>

<template>
  <div style="max-width: 640px; margin: 0 auto;">
    <div style="margin-bottom: 24px;">
      <h2 style="margin: 0 0 4px; font-size: 22px;">{{ isEdit ? '编辑信息' : '发布失物招领信息' }}</h2>
      <p style="margin: 0; color: #909399; font-size: 13px;">填写的联系方式将会显示给查看的用户</p>
    </div>
    <div style="background: #fff; border-radius: 12px; border: 1px solid #f0f0f0; padding: 28px;">
      <el-form :model="form" label-position="top" @submit.prevent="handleSubmit">
        <el-form-item label="类型">
          <el-radio-group v-model="form.type">
            <el-radio value="LOST" style="margin-right: 16px;">寻物</el-radio>
            <el-radio value="FOUND">招领</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="标题"><el-input v-model="form.title" placeholder="如：丢失蓝色水杯" size="large" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="4" placeholder="详细描述物品特征..." /></el-form-item>
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px;">
          <el-form-item label="地点"><el-input v-model="form.location" placeholder="丢失/捡到的地点" /></el-form-item>
          <el-form-item label="物品图片">
            <el-upload :http-request="handleUpload" :show-file-list="false" accept="image/*" :disabled="uploading">
              <el-button :loading="uploading" style="border-radius: 8px;">点击上传</el-button>
              <template #tip><span style="margin-left: 8px; font-size: 12px; color: #909399;">支持 jpg/png/gif/webp</span></template>
            </el-upload>
            <div v-if="form.image" style="margin-top: 8px;">
              <div style="position: relative; width: 120px; height: 120px; border-radius: 6px; overflow: hidden; border: 1px solid #e4e7ed;">
                <img :src="form.image" style="width: 100%; height: 100%; object-fit: cover;" />
                <span style="position: absolute; top: 2px; right: 6px; font-size: 18px; color: #f56c6c; cursor: pointer;" @click="form.image = ''">×</span>
              </div>
            </div>
          </el-form-item>
        </div>
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px;">
          <el-form-item label="联系人"><el-input v-model="form.contactPerson" /></el-form-item>
          <el-form-item label="手机"><el-input v-model="form.contactPhone" /></el-form-item>
        </div>
        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" native-type="submit" style="border-radius: 8px;">{{ isEdit ? '保存' : '发布' }}</el-button>
          <el-button size="large" @click="router.back()" style="border-radius: 8px;">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>
