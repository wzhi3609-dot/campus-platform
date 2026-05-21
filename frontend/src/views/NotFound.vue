<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()
</script>

<template>
  <div style="text-align: center; padding: 80px 20px;">
    <div style="font-size: 80px; margin-bottom: 16px;">404</div>
    <h2 style="margin: 0 0 8px; color: #3a4030;">页面不存在</h2>
    <p style="color: #8a7a6a; margin: 0 0 24px;">你访问的页面可能已被移除或地址输入有误</p>
    <el-button type="primary" @click="router.push('/')">返回首页</el-button>
  </div>
</template>
