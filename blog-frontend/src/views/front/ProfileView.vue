<template>
  <div class="profile-page">
    <n-h2>个人中心</n-h2>

    <n-card class="profile-card">
      <template #header>
        <n-space align="center" size="16">
          <n-avatar :src="userInfo?.avatar" round :size="64" />
          <div>
            <div class="nickname">{{ userInfo?.nickname || userInfo?.username }}</div>
            <n-text depth="3">@{{ userInfo?.username }}</n-text>
          </div>
        </n-space>
      </template>

      <n-descriptions label-placement="left" :column="1" class="profile-info">
        <n-descriptions-item label="昵称">{{ userInfo?.nickname || '未设置' }}</n-descriptions-item>
        <n-descriptions-item label="邮箱">{{ userInfo?.email }}</n-descriptions-item>
        <n-descriptions-item label="角色">
          <n-tag :type="userInfo?.role === 'ADMIN' ? 'primary' : 'default'" size="small">
            {{ userInfo?.role === 'ADMIN' ? '管理员' : '用户' }}
          </n-tag>
        </n-descriptions-item>
        <n-descriptions-item label="注册时间">{{ userInfo?.createTime }}</n-descriptions-item>
      </n-descriptions>
    </n-card>

    <n-card title="我的文章" class="mt-4">
      <n-skeleton v-if="loading" :height="40" repeat="3" />
      <n-empty v-else-if="myArticles.length === 0" description="还没有发表过文章" />
      <n-list v-else>
        <n-list-item v-for="item in myArticles" :key="item.id">
          <router-link :to="`/article/${item.id}`" class="article-link">
            <n-thing :title="item.title">
              <template #description>
                <n-space size="small">
                  <n-text depth="3">{{ item.publishTime }}</n-text>
                  <n-tag v-if="item.categoryName" size="tiny">{{ item.categoryName }}</n-tag>
                </n-space>
              </template>
            </n-thing>
          </router-link>
        </n-list-item>
      </n-list>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import * as articleApi from '@/api/article'
import type { ArticlePageItem } from '@/api/article'

const userStore = useUserStore()
const userInfo = userStore.userInfo
const loading = ref(true)
const myArticles = ref<ArticlePageItem[]>([])

onMounted(async () => {
  try {
    const res = (await articleApi.getPublishedPage({ page: 1, size: 20 })) as any
    myArticles.value = res.records || []
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.profile-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}
.profile-card {
  margin-bottom: 16px;
}
.nickname {
  font-size: 20px;
  font-weight: 600;
}
.profile-info {
  margin-top: 16px;
}
.mt-4 {
  margin-top: 16px;
}
.article-link {
  text-decoration: none;
  color: inherit;
}
.article-link:hover {
  color: #2080f0;
}
</style>