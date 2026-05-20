<script setup>
import { ref } from 'vue'
import Dashboard from './components/Dashboard.vue'
import UserReview from './components/UserReview.vue'
import PostReview from './components/PostReview.vue'
import PostManager from './components/PostManager.vue'
import LostFoundManager from './components/LostFoundManager.vue'
import ReportReview from './components/ReportReview.vue'
import LogViewer from './components/LogViewer.vue'
import AnnouncementManager from './components/AnnouncementManager.vue'

const activeTab = ref('dashboard')
</script>

<template>
  <div>
    <div style="margin-bottom: 24px;">
      <h2 style="margin: 0 0 4px; font-size: 22px;">管理后台</h2>
      <p style="margin: 0; color: #909399; font-size: 13px;">数据概览、审核用户/帖子/举报，管理帖子/失物/公告置顶</p>
    </div>
    <el-card shadow="never" style="border-radius: 12px; border: 1px solid #e4e7ed;">
      <el-tabs v-model="activeTab" style="padding: 0 8px;">
        <el-tab-pane label="数据概览" name="dashboard">
          <Dashboard />
        </el-tab-pane>
        <el-tab-pane label="用户审核" name="users">
          <UserReview />
        </el-tab-pane>
        <el-tab-pane label="帖子审核" name="posts">
          <PostReview />
        </el-tab-pane>
        <el-tab-pane label="帖子置顶" name="postPin">
          <PostManager />
        </el-tab-pane>
        <el-tab-pane label="失物置顶" name="lostfoundPin">
          <LostFoundManager />
        </el-tab-pane>
        <el-tab-pane label="举报审核" name="reports">
          <ReportReview />
        </el-tab-pane>
        <el-tab-pane label="系统日志" name="logs">
          <LogViewer />
        </el-tab-pane>
        <el-tab-pane label="公告管理" name="announcements">
          <AnnouncementManager />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>
