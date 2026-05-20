<script setup>
// 发布 / 编辑二手商品页：创建新商品或编辑已有商品信息
import { reactive, ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../../api'

const router = useRouter()
const route = useRoute()
const isEdit = !!route.query.id
const form = reactive({
  title: '', description: '', price: '', category: '', contactPerson: '',
  contactPhone: '', location: '', images: ''
})
const loading = ref(false)
const uploading = ref(false)

const categories = ['教材', '电子产品', '生活用品', '服饰', '体育', '其他']

async function handleSubmit() {
  loading.value = true
  try {
    if (isEdit) {
      await request.put(`/trades/${route.query.id}`, form)
    } else {
      await request.post('/trades', form)
    }
    router.push('/trades')
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
      const existing = form.images ? form.images.split(',') : []
      existing.push(urls[0])
      form.images = existing.join(',')
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
    const res = await request.get(`/trades/${route.query.id}`)
    Object.assign(form, res.data)
  }
})
</script>

<template>
  <div style="max-width: 640px; margin: 0 auto;">
    <div style="margin-bottom: 24px;">
      <h2 style="margin: 0 0 4px; font-size: 22px;">{{ isEdit ? '编辑商品' : '发布商品' }}</h2>
      <p style="margin: 0; color: #909399; font-size: 13px;">填写商品信息，方便买家联系你</p>
    </div>
    <div style="background: #fff; border-radius: 12px; border: 1px solid #f0f0f0; padding: 28px;">
      <el-form :model="form" label-position="top" @submit.prevent="handleSubmit">
        <el-form-item label="标题"><el-input v-model="form.title" placeholder="请填写商品名称" size="large" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="4" placeholder="描述商品成色、规格等..." /></el-form-item>
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px;">
          <el-form-item label="价格"><el-input v-model="form.price" placeholder="如：99.00" /></el-form-item>
          <el-form-item label="分类">
            <el-select v-model="form.category" placeholder="选择分类" style="width: 100%;">
              <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
            </el-select>
          </el-form-item>
        </div>
        <el-form-item label="商品图片">
            <el-upload :http-request="handleUpload" :show-file-list="false" accept="image/*" :disabled="uploading">
              <el-button :loading="uploading" style="border-radius: 8px;">点击上传</el-button>
              <template #tip><span style="margin-left: 8px; font-size: 12px; color: #909399;">支持 jpg/png/gif/webp，单文件不超过 5MB</span></template>
            </el-upload>
            <div v-if="form.images" style="margin-top: 8px; display: flex; gap: 8px; flex-wrap: wrap;">
              <div v-for="(url, i) in form.images.split(',')" :key="i" style="position: relative; width: 80px; height: 80px; border-radius: 6px; overflow: hidden; border: 1px solid #e4e7ed;">
                <img :src="url" style="width: 100%; height: 100%; object-fit: cover;" />
                <span style="position: absolute; top: 2px; right: 4px; font-size: 16px; color: #f56c6c; cursor: pointer;" @click="form.images = form.images.split(',').filter((_,j) => j !== i).join(',')">×</span>
              </div>
            </div>
          </el-form-item>
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px;">
          <el-form-item label="联系人"><el-input v-model="form.contactPerson" /></el-form-item>
          <el-form-item label="联系电话"><el-input v-model="form.contactPhone" /></el-form-item>
        </div>
        <el-form-item label="位置"><el-input v-model="form.location" placeholder="如：图书馆三楼" /></el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" native-type="submit" style="border-radius: 8px;">{{ isEdit ? '保存' : '发布' }}</el-button>
          <el-button size="large" @click="router.back()" style="border-radius: 8px;">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>
