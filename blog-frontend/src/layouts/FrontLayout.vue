<template>
  <n-layout position="absolute">
    <!-- 顶部导航 -->
    <n-layout-header bordered class="header">
      <div class="header-inner">
        <router-link to="/" class="logo">{{ appStore.siteConfig.siteName }}</router-link>
        <nav class="nav">
          <router-link to="/">首页</router-link>
          <router-link to="/archive">归档</router-link>
          <router-link to="/about">关于</router-link>
          <router-link to="/friends">友链</router-link>
        </nav>
        <div class="user-area">
          <template v-if="userStore.token">
            <router-link to="/profile">个人中心</router-link>
            <a href="javascript:;" @click="handleLogout">退出</a>
          </template>
          <template v-else>
            <router-link to="/login">登录</router-link>
            <router-link to="/register">注册</router-link>
          </template>
        </div>
      </div>
    </n-layout-header>

    <!-- 主内容区 -->
    <n-layout-content class="content" content-style="padding: 24px 16px;">
      <router-view />
    </n-layout-content>

    <!-- 页脚 -->
    <n-layout-footer bordered class="footer">
      © 2025 {{ appStore.siteConfig.siteName }} · {{ appStore.siteConfig.slogan }}
    </n-layout-footer>
  </n-layout>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()

function handleLogout() {
  // TODO M1: 调用后端登出接口后再清除本地态
  userStore.clear()
  router.push('/')
}
</script>

<style scoped>
.header-inner {
  max-width: 1080px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  gap: 32px;
  height: 56px;
  padding: 0 16px;
}
.logo {
  font-size: 18px;
  font-weight: 700;
  color: inherit;
  text-decoration: none;
}
.nav {
  display: flex;
  gap: 16px;
  flex: 1;
}
.nav a,
.user-area a {
  color: inherit;
  text-decoration: none;
  opacity: 0.75;
}
.nav a:hover,
.user-area a:hover {
  opacity: 1;
}
.user-area {
  display: flex;
  gap: 12px;
}
.content {
  min-height: calc(100vh - 112px);
}
.footer {
  text-align: center;
  padding: 16px 0;
  color: #999;
  font-size: 13px;
}
</style>
