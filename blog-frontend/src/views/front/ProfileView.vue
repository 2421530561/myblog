<template>
  <div class="profile-page">
    <n-h2>个人中心</n-h2>

    <n-card class="profile-card">
      <template #header>
        <n-space align="center" :size="16">
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

    <n-card class="mt-4">
      <template #header>
        <n-space align="center" justify="space-between" style="width: 100%">
          <span>我的文章</span>
          <n-button size="small" type="primary" @click="router.push('/editor')">写文章</n-button>
        </n-space>
      </template>

      <n-tabs v-model:value="statusFilter" type="segment" size="small" @update:value="onFilterChange">
        <n-tab name="all">全部</n-tab>
        <n-tab name="0">草稿</n-tab>
        <n-tab name="1">待审核</n-tab>
        <n-tab name="2">已发布</n-tab>
        <n-tab name="3">已驳回</n-tab>
      </n-tabs>

      <n-skeleton v-if="loading" :height="40" repeat="3" class="mt-4" />
      <n-empty v-else-if="myArticles.length === 0" description="还没有文章，去写一篇吧" class="mt-4" />
      <n-list v-else class="mt-4">
        <n-list-item v-for="item in myArticles" :key="item.id">
          <n-thing :title="item.title">
            <template #description>
              <n-space size="small" align="center" class="mt-2">
                <n-tag size="tiny" :type="statusMeta(item.status).type">
                  {{ statusMeta(item.status).label }}
                </n-tag>
                <n-text depth="3">{{ item.publishTime || item.createTime || '未发布' }}</n-text>
                <n-tag v-if="item.categoryName" size="tiny">{{ item.categoryName }}</n-tag>
                <span class="counts">👁 {{ item.viewCount }} · 👍 {{ item.likeCount }}</span>
              </n-space>
              <n-alert
                v-if="item.status === 3 && item.rejectReason"
                type="error"
                size="small"
                class="mt-2"
              >
                驳回原因：{{ item.rejectReason }}
              </n-alert>
            </template>
            <template #header-extra>
              <n-space size="small">
                <n-button size="tiny" @click="router.push(`/article/${item.id}`)">查看</n-button>
                <n-button size="tiny" type="primary" @click="router.push(`/editor/${item.id}`)">编辑</n-button>
                <n-button
                  v-if="item.status === 0 || item.status === 3"
                  size="tiny"
                  type="info"
                  @click="handleSubmit(item.id)"
                >
                  {{ item.status === 3 ? '重新提交' : '提交审核' }}
                </n-button>
                <n-button size="tiny" type="error" @click="handleDelete(item)">删除</n-button>
              </n-space>
            </template>
          </n-thing>
        </n-list-item>
      </n-list>

      <n-pagination
        v-if="total > pageSize"
        class="pagination"
        :page="page"
        :page-size="pageSize"
        :item-count="total"
        @update:page="onPageChange"
      />
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useDialog, useMessage } from 'naive-ui'
import { useUserStore } from '@/stores/user'
import * as articleApi from '@/api/article'
import { ArticleStatusMap, type ArticlePageItem } from '@/api/article'

const router = useRouter()
const message = useMessage()
const dialog = useDialog()
const userStore = useUserStore()
const userInfo = userStore.userInfo

const loading = ref(true)
const myArticles = ref<ArticlePageItem[]>([])
const statusFilter = ref<string>('all')
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

function statusMeta(status: number) {
  return ArticleStatusMap[status] ?? { label: '未知', type: 'default' as const }
}

async function fetchData() {
  loading.value = true
  try {
    const res = await articleApi.getMyArticlePage({
      status: statusFilter.value === 'all' ? undefined : Number(statusFilter.value),
      page: page.value,
      size: pageSize.value,
    })
    myArticles.value = res.records || []
    total.value = res.total || 0
  } catch (e: any) {
    message.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function onFilterChange() {
  page.value = 1
  fetchData()
}

function onPageChange(p: number) {
  page.value = p
  fetchData()
}

async function handleSubmit(id: number) {
  try {
    await articleApi.submitArticle(id)
    message.success('已提交审核，等待管理员处理')
    await fetchData()
  } catch (e: any) {
    message.error(e.message || '提交失败')
  }
}

function handleDelete(item: ArticlePageItem) {
  dialog.warning({
    title: '删除文章',
    content: `确定删除《${item.title}》吗？`,
    positiveText: '删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await articleApi.deleteArticle(item.id)
        message.success('已删除')
        await fetchData()
      } catch (e: any) {
        message.error(e.message || '删除失败')
      }
    },
  })
}

onMounted(fetchData)
</script>

<style scoped>
.profile-page {
  max-width: 860px;
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
.mt-2 {
  margin-top: 8px;
}
.mt-4 {
  margin-top: 16px;
}
.counts {
  font-size: 12px;
  color: #999;
}
.pagination {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}
</style>
