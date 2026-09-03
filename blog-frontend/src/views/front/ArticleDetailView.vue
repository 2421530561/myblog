<template>
  <div class="article-detail" v-if="article">
    <n-h1>{{ article.title }}</n-h1>
    <n-space class="meta-bar" size="small">
      <n-avatar :src="article.authorAvatar" round size="small" />
      <span>{{ article.authorName }}</span>
      <span class="divider">|</span>
      <span>{{ article.publishTime }}</span>
      <n-tag v-if="article.categoryName" size="small">{{ article.categoryName }}</n-tag>
      <n-tag v-for="tag in article.tags" :key="tag" size="small" type="info">{{ tag }}</n-tag>
      <span class="divider">|</span>
      <span>👁 {{ article.viewCount }}</span>
      <span>👍 {{ article.likeCount }}</span>
    </n-space>

    <n-divider />

    <!-- Markdown 渲染结果 -->
    <div class="article-content" v-html="article.contentHtml"></div>
  </div>
  <n-empty v-else-if="!loading" description="文章不存在" />
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import * as articleApi from '@/api/article'
import type { ArticleDetail } from '@/api/article'

const route = useRoute()
const article = ref<ArticleDetail | null>(null)
const loading = ref(true)

onMounted(async () => {
  try {
    article.value = (await articleApi.getDetail(Number(route.params.id))) as any
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.article-detail {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}
.meta-bar {
  color: #999;
  font-size: 14px;
  margin-bottom: 16px;
}
.divider {
  color: #ddd;
}
.article-content {
  line-height: 1.8;
  font-size: 16px;
}
</style>