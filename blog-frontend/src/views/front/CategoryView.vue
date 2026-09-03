<template>
  <div class="category-page">
    <n-h2>{{ categoryId ? `分类：${categoryName}` : '全部分类' }}</n-h2>
    <n-space>
      <n-button
        :type="!categoryId ? 'primary' : 'default'"
        size="small"
        @click="router.push('/category')"
      >全部</n-button>
      <n-button
        v-for="cat in categories"
        :key="cat.id"
        :type="categoryId === cat.id ? 'primary' : 'default'"
        size="small"
        @click="router.push(`/category/${cat.id}`)"
      >{{ cat.name }}</n-button>
    </n-space>
    <n-divider />
    <!-- 文章列表复用 HomeView 的卡片列表 -->
    <div class="article-list">
      <n-card
        v-for="item in list"
        :key="item.id"
        class="article-card"
        hoverable
        @click="router.push(`/article/${item.id}`)"
      >
        <n-thing>
          <template #header>{{ item.title }}</template>
          <template #description>
            <n-ellipsis :line-clamp="2">{{ item.summary || '暂无摘要' }}</n-ellipsis>
          </template>
          <template #footer>
            <n-space size="small" align="center">
              <span class="meta">{{ item.publishTime }}</span>
              <span class="meta">👁 {{ item.viewCount }}</span>
            </n-space>
          </template>
        </n-thing>
      </n-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import * as articleApi from '@/api/article'
import * as categoryApi from '@/api/category'
import type { ArticlePageItem } from '@/api/article'
import type { Category } from '@/api/category'

const route = useRoute()
const router = useRouter()
const categoryId = ref<number | undefined>()
const categoryName = ref('')
const categories = ref<Category[]>([])
const list = ref<ArticlePageItem[]>([])

async function fetchCategories() {
  categories.value = (await categoryApi.getCategoryList()) as any
}

async function fetchArticles() {
  const res = (await articleApi.getPublishedPage({ categoryId: categoryId.value })) as any
  list.value = res.records
  if (categoryId.value) {
    const cat = categories.value.find(c => c.id === categoryId.value)
    categoryName.value = cat?.name || ''
  }
}

watch(() => route.params.id, (val) => {
  categoryId.value = val ? Number(val) : undefined
  fetchArticles()
})

onMounted(async () => {
  categoryId.value = route.params.id ? Number(route.params.id) : undefined
  await fetchCategories()
  await fetchArticles()
})
</script>

<style scoped>
.category-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}
.article-card {
  margin-bottom: 12px;
  cursor: pointer;
}
.meta {
  color: #999;
  font-size: 13px;
}
</style>