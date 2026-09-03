<template>
  <div class="archive-page">
    <n-h2>归档</n-h2>
    <n-text depth="3">共 {{ total }} 篇文章</n-text>

    <n-skeleton v-if="loading" :height="60" repeat="8" class="mt-4" />
    <template v-else>
      <div v-for="(group, yearMonth) in archives" :key="yearMonth" class="archive-group">
        <n-h3 class="archive-year">{{ yearMonth }}</n-h3>
        <n-list>
          <n-list-item v-for="item in group" :key="item.id">
            <router-link :to="`/article/${item.id}`" class="archive-link">
              <n-thing :title="item.title">
                <template #description>
                  <n-space size="small">
                    <n-text depth="3">{{ formatDate(item.publishTime) }}</n-text>
                    <n-tag v-if="item.categoryName" size="tiny">{{ item.categoryName }}</n-tag>
                  </n-space>
                </template>
              </n-thing>
            </router-link>
          </n-list-item>
        </n-list>
      </div>
      <n-empty v-if="Object.keys(archives).length === 0" description="暂无文章" />
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import * as articleApi from '@/api/article'
import type { ArticlePageItem } from '@/api/article'

const loading = ref(true)
const total = ref(0)
const archives = ref<Record<string, ArticlePageItem[]>>({})

function formatDate(dateStr: string) {
  if (!dateStr) return ''
  return dateStr.substring(0, 10)
}

onMounted(async () => {
  try {
    const res = (await articleApi.getPublishedPage({ page: 1, size: 999 })) as any
    const list: ArticlePageItem[] = res.records || []
    total.value = res.total || 0

    // 按年月分组
    const groups: Record<string, ArticlePageItem[]> = {}
    for (const item of list) {
      if (item.publishTime) {
        const key = item.publishTime.substring(0, 7) // YYYY-MM
        if (!groups[key]) groups[key] = []
        groups[key].push(item)
      }
    }
    // 按年月降序排序
    const sorted: Record<string, ArticlePageItem[]> = {}
    Object.keys(groups)
      .sort((a, b) => b.localeCompare(a))
      .forEach((key) => {
        sorted[key] = groups[key]
      })
    archives.value = sorted
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.archive-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}
.mt-4 {
  margin-top: 16px;
}
.archive-group {
  margin-bottom: 24px;
}
.archive-year {
  border-bottom: 2px solid #2080f0;
  padding-bottom: 8px;
  margin-bottom: 12px;
}
.archive-link {
  text-decoration: none;
  color: inherit;
}
.archive-link:hover {
  color: #2080f0;
}
</style>