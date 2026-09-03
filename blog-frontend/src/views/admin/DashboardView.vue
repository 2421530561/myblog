<template>
  <div class="dashboard">
    <n-h2>仪表盘</n-h2>
    <n-text depth="3">欢迎回来，{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</n-text>

    <n-grid :cols="4" :x-gap="16" class="mt-4">
      <n-gi>
        <n-card title="文章总数" hoverable>
          <div class="stat-value">-</div>
          <n-text depth="3">共发布 {{ totalPublished }} 篇</n-text>
        </n-card>
      </n-gi>
      <n-gi>
        <n-card title="分类" hoverable>
          <div class="stat-value">{{ categoryCount }}</div>
          <n-text depth="3">文章分类</n-text>
        </n-card>
      </n-gi>
      <n-gi>
        <n-card title="标签" hoverable>
          <div class="stat-value">{{ tagCount }}</div>
          <n-text depth="3">文章标签</n-text>
        </n-card>
      </n-gi>
      <n-gi>
        <n-card title="阅读量" hoverable>
          <div class="stat-value">-</div>
          <n-text depth="3">累计阅读</n-text>
        </n-card>
      </n-gi>
    </n-grid>

    <n-card title="快速入口" class="mt-4">
      <n-space>
        <n-button type="primary" @click="router.push('/editor')">写文章</n-button>
        <n-button @click="router.push('/admin/categories')">管理分类</n-button>
        <n-button @click="router.push('/admin/tags')">管理标签</n-button>
      </n-space>
    </n-card>

    <n-card title="最近文章" class="mt-4">
      <n-skeleton v-if="loading" :height="40" repeat="5" />
      <n-list v-else v-for="item in recentArticles" :key="item.id" class="recent-list">
        <n-list-item>
          <template #prefix>
            <n-ellipsis style="max-width: 400px">{{ item.title }}</n-ellipsis>
          </template>
          <template #suffix>
            <n-space align="center" size="small">
              <n-tag v-if="item.categoryName" size="tiny">{{ item.categoryName }}</n-tag>
              <n-text depth="3" style="font-size: 13px;">{{ item.publishTime }}</n-text>
            </n-space>
          </template>
        </n-list-item>
      </n-list>
      <n-empty v-if="!loading && recentArticles.length === 0" description="暂无文章" />
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import * as articleApi from '@/api/article'
import * as categoryApi from '@/api/category'
import * as tagApi from '@/api/tag'
import type { ArticlePageItem } from '@/api/article'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(true)
const totalPublished = ref(0)
const categoryCount = ref(0)
const tagCount = ref(0)
const recentArticles = ref<ArticlePageItem[]>([])

onMounted(async () => {
  try {
    const [articleRes, categories, tags] = await Promise.all([
      articleApi.getPublishedPage({ page: 1, size: 5 }),
      categoryApi.getCategoryList(),
      tagApi.getTagList(),
    ])
    const data = articleRes as any
    recentArticles.value = data.records || []
    totalPublished.value = data.total || 0
    categoryCount.value = (categories as any[]).length
    tagCount.value = (tags as any[]).length
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.dashboard {
  max-width: 1200px;
}
.mt-4 {
  margin-top: 16px;
}
.stat-value {
  font-size: 36px;
  font-weight: 700;
  color: #2080f0;
  margin-bottom: 4px;
}
.recent-list {
  margin-top: 0;
}
</style>