<template>
  <div class="tag-page">
    <n-h2>{{ tagId ? `标签：${tagName}` : '全部分类' }}</n-h2>
    <n-space>
      <n-button
        :type="!tagId ? 'primary' : 'default'"
        size="small"
        @click="router.push('/tag')"
      >全部</n-button>
      <n-button
        v-for="t in tags"
        :key="t.id"
        :type="tagId === t.id ? 'primary' : 'default'"
        size="small"
        @click="router.push(`/tag/${t.id}`)"
      >{{ t.name }}</n-button>
    </n-space>
    <n-divider />
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
import * as tagApi from '@/api/tag'
import type { ArticlePageItem } from '@/api/article'
import type { Tag } from '@/api/tag'

const route = useRoute()
const router = useRouter()
const tagId = ref<number | undefined>()
const tagName = ref('')
const tags = ref<Tag[]>([])
const list = ref<ArticlePageItem[]>([])

async function fetchTags() {
  tags.value = (await tagApi.getTagList()) as any
}

async function fetchArticles() {
  const res = (await articleApi.getPublishedPage({ tagId: tagId.value })) as any
  list.value = res.records
  if (tagId.value) {
    const t = tags.value.find(t => t.id === tagId.value)
    tagName.value = t?.name || ''
  }
}

watch(() => route.params.id, (val) => {
  tagId.value = val ? Number(val) : undefined
  fetchArticles()
})

onMounted(async () => {
  tagId.value = route.params.id ? Number(route.params.id) : undefined
  await fetchTags()
  await fetchArticles()
})
</script>

<style scoped>
.tag-page {
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