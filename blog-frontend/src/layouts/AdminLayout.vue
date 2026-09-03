<template>
  <n-layout has-sider class="admin-layout">
    <!-- 侧边菜单 -->
    <n-layout-sider bordered collapse-mode="width" :width="200" :collapsed-width="64" show-trigger>
      <div class="admin-logo">后台管理</div>
      <n-menu :value="activeKey" :options="menuOptions" @update:value="handleMenuSelect" />
    </n-layout-sider>

    <n-layout>
      <!-- 顶栏 -->
      <n-layout-header bordered class="admin-header">
        <span>个人博客 · 后台管理</span>
        <div class="admin-header-right">
          <router-link to="/">返回前台</router-link>
          <a href="javascript:;" @click="handleLogout">退出登录</a>
        </div>
      </n-layout-header>

      <!-- 内容区 -->
      <n-layout-content class="admin-content">
        <router-view />
      </n-layout-content>
    </n-layout>
  </n-layout>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { MenuOption } from 'naive-ui'
import { useUserStore } from '@/stores/user'
import * as authApi from '@/api/auth'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 后台菜单（对应 PRD 4.3）
const menuOptions: MenuOption[] = [
  { label: '仪表盘', key: '/admin' },
  { label: '文章管理', key: '/admin/articles' },
  { label: '分类管理', key: '/admin/categories' },
  { label: '标签管理', key: '/admin/tags' },
  { label: '评论管理', key: '/admin/comments' },
  { label: '用户管理', key: '/admin/users' },
  { label: '友链管理', key: '/admin/friends' },
  { label: '站点设置', key: '/admin/config' },
]

const activeKey = computed(() => route.path)

function handleMenuSelect(key: string) {
  router.push(key)
}

async function handleLogout() {
  try {
    await authApi.logout()
  } catch {
    // 忽略登出接口错误
  }
  userStore.clear()
  router.push('/login')
}
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
}
.admin-logo {
  height: 56px;
  line-height: 56px;
  text-align: center;
  font-weight: 700;
  border-bottom: 1px solid rgba(128, 128, 128, 0.2);
}
.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 56px;
  padding: 0 24px;
}
.admin-header-right {
  display: flex;
  gap: 16px;
}
.admin-header-right a {
  color: inherit;
  text-decoration: none;
}
.admin-content {
  padding: 24px;
  min-height: calc(100vh - 56px);
}
</style>