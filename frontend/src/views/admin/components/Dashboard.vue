<script setup>
import { ref, onMounted, computed } from 'vue'
import request from '../../../api'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { PieChart, BarChart, LineChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent, GridComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

use([PieChart, BarChart, LineChart, TitleComponent, TooltipComponent, LegendComponent, GridComponent, CanvasRenderer])

const stats = ref({
  totalUsers: 0, totalQuestions: 0, totalTrades: 0, totalPosts: 0, totalLostFound: 0,
  pendingUsers: 0, pendingPosts: 0, userTypeDistribution: {}, tradeCategories: {}
})
const timeline = ref({ dates: [], newUsers: [], newPosts: [] })
const loading = ref(false)

async function fetchDashboard() {
  loading.value = true
  try {
    const res = await request.get('/admin/dashboard')
    stats.value = res.data
    const tRes = await request.get('/admin/dashboard/timeline')
    timeline.value = tRes.data
  } finally {
    loading.value = false
  }
}

const userTypeOption = computed(() => ({
  tooltip: { trigger: 'item' },
  legend: { bottom: '0%' },
  series: [{
    name: '用户分布',
    type: 'pie',
    radius: ['40%', '70%'],
    avoidLabelOverlap: false,
    itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
    label: { show: true, formatter: '{b}: {c} 人' },
    data: [
      { value: stats.value.userTypeDistribution?.student || 0, name: '学生' },
      { value: stats.value.userTypeDistribution?.teacher || 0, name: '教师' }
    ]
  }]
}))

const tradeCategoryOption = computed(() => ({
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: { type: 'category', data: Object.keys(stats.value.tradeCategories || {}), axisLabel: { rotate: 30 } },
  yAxis: { type: 'value', minInterval: 1 },
  series: [{
    name: '商品数', type: 'bar',
    data: Object.values(stats.value.tradeCategories || {}),
    itemStyle: { color: '#1a73e8', borderRadius: [6, 6, 0, 0] }
  }]
}))

const timelineOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: { bottom: '0%' },
  grid: { left: '3%', right: '4%', bottom: '8%', containLabel: true },
  xAxis: { type: 'category', data: timeline.value.dates || [] },
  yAxis: { type: 'value', minInterval: 1 },
  series: [
    {
      name: '新注册用户', type: 'line',
      data: timeline.value.newUsers || [],
      smooth: true,
      itemStyle: { color: '#1a73e8' }
    },
    {
      name: '新发帖', type: 'line',
      data: timeline.value.newPosts || [],
      smooth: true,
      itemStyle: { color: '#34a853' }
    }
  ]
}))

onMounted(fetchDashboard)
</script>

<template>
  <div v-loading="loading">
    <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); gap: 16px; margin-bottom: 24px;">
      <div v-for="item in [
        { label: '用户总数', value: stats.totalUsers, color: '#1a73e8', icon: '👥' },
        { label: '问题总数', value: stats.totalQuestions, color: '#34a853', icon: '❓' },
        { label: '二手商品', value: stats.totalTrades, color: '#fbbc04', icon: '🛒' },
        { label: '论坛帖子', value: stats.totalPosts, color: '#ea4335', icon: '📝' },
        { label: '失物招领', value: stats.totalLostFound, color: '#8e44ad', icon: '🔍' },
        { label: '待审用户', value: stats.pendingUsers, color: '#e67e22', icon: '⏳' },
        { label: '待审帖子', value: stats.pendingPosts, color: '#e74c3c', icon: '📋' }
      ]" :key="item.label"
        style="background: #fff; border-radius: 12px; border: 1px solid #f0f0f0; padding: 20px; text-align: center;">
        <div style="font-size: 28px;">{{ item.icon }}</div>
        <div style="font-size: 28px; font-weight: 700; color: v-bind('item.color'); margin: 8px 0;">{{ item.value }}</div>
        <div style="font-size: 13px; color: #909399;">{{ item.label }}</div>
      </div>
    </div>

    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px;">
      <div style="background: #fff; border-radius: 12px; border: 1px solid #f0f0f0; padding: 24px;">
        <h4 style="margin: 0 0 16px; font-size: 16px; font-weight: 600;">用户身份分布</h4>
        <v-chart :option="userTypeOption" style="height: 300px;" autoresize />
      </div>
      <div style="background: #fff; border-radius: 12px; border: 1px solid #f0f0f0; padding: 24px;">
        <h4 style="margin: 0 0 16px; font-size: 16px; font-weight: 600;">二手商品分类统计</h4>
        <v-chart :option="tradeCategoryOption" style="height: 300px;" autoresize />
      </div>
    </div>

    <div style="margin-top: 16px;">
      <div style="background: #fff; border-radius: 12px; border: 1px solid #f0f0f0; padding: 24px;">
        <h4 style="margin: 0 0 16px; font-size: 16px; font-weight: 600;">近7天数据趋势</h4>
        <v-chart :option="timelineOption" style="height: 300px;" autoresize />
      </div>
    </div>
  </div>
</template>
