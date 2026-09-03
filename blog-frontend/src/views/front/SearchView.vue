<template>
  <div class="search-page">
    <n-h2>搜索</n-h2>

    <n-input
      v-model:value="keyword"
      placeholder="输入关键词搜索文章..."
      size="large"
      :loading="searching"
      clearable
      @keyup.enter="doSearch"
    >
      <template #prefix>
        <n-icon><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="16" height="16"><path fill="currentColor" d="M15.5 14h-.79l-.28-.27A6.471 6.471 0 0 0 16 9.5 6.5 6.5 0 1 0 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/></svg></n-icon>
      </template>
      <template #suffix>
        <n-button text type="primary" :loading="searching" @click="doSearch">搜索</n-button>
      </template>
    </n-input>

    <div class="search-hint" v-if="!searched">
      <n-empty description="输入关键词搜索文章" />
    </div>

    <template v-else>
      <n-text depth="3" class="result-count">共找到 {{ total }} 条结果</n-text>

      <n-skeleton v-if="searching" :height="80" repeat="5" class="mt-4" />
      <template v-else>
        <div class="search-results">
          <n-card
            v-for="item in list"
            :key="item.id"
            class="result-card"
            hoverable
            @click="router.push(`/article/${item.id}`)"
          >
            <n-thing>
              <template #header>
                <span v-html="highlightKeyword(item.title)"></span>
              </template>
              <template #description>
                <n-ellipsis :line-clamp="2" v-html="highlightKeyword(item.summary || '')" />
              </template>
              <template #footer>
                <n-space size="small">
                  <n-text depth="3">{{ item.publishTime }}</n-text>
                  <n-tag v-if="item.categoryName" size="tiny">{{ item.categoryName }}</n-tag>
                  <n-text depth="3">👁 {{ item.viewCount }}</n-text>
                </n-space>
              </template>
            </n-thing>
          </n-card>
        </div>

        <n-empty v-if="list.length === 0" description="未找到相关文章" />

        <n-pagination
          v-if="total > size"
          class="pagination"
          :page="page"
          :page-size="size"
          :item-count="total"
          @update:page="onPageChange"
        />
      </template>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import * as articleApi from '@/api/article'
import type { ArticlePageItem } from '@/api/article'

const router = useRouter()
const keyword = ref('')
const searching = ref(false)
const searched = ref(false)
const list = ref<ArticlePageItem[]>([])
const page = ref(1)
const size = ref(10)
const total = ref(0)

function highlightKeyword(text: string) {
  if (!keyword.value || !text) return text
  const regex = new RegExp(`(${keyword.value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')})`, 'gi')
  return text.replace(regex, '<span style="color: #d03050; font-weight: 600;">$1</span>')
}

async function doSearch() {
  if (!keyword.value.trim()) return
  searching.value = true
  searched.value = true
  page.value = 1
  try {
    const res = (await articleApi.getPublishedPage({
      keyword: keyword.value.trim(),
      page: page.value,
      size: size.value,
    })) as any
    list.value = res.records || []
    total.value = res.total || 0
  } finally {
    searching.value = false
  }
}

function onPageChange(p: number) {
  page.value = p
  doSearch()
}
</script>

<style scoped>
.search-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}
.search-hint {
  margin-top: 60px;
}
.result-count {
  display: block;
  margin-top: 16px;
  margin-bottom: 8px;
}
.mt-4 {
  margin-top: 16px;
}
.search-results {
  margin-top: 12px;
}
.result-card {
  margin-bottom: 12px;
  cursor: pointer;
}
.pagination {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>