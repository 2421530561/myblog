<template>
  <div class="article-manage">
    <n-page-header title="文章管理">
      <template #subtitle>
        <n-text depth="3" style="font-size: 12px">
          全量文章（含草稿 / 待审核 / 已驳回），可审核发布、驳回或下架
        </n-text>
      </template>
      <template #extra>
        <n-button type="primary" @click="router.push('/editor')">写文章</n-button>
      </template>
    </n-page-header>

    <n-space class="toolbar" align="center">
      <n-tabs v-model:value="statusFilter" type="segment" size="small" @update:value="onFilterChange">
        <n-tab name="all">全部</n-tab>
        <n-tab name="1">待审核</n-tab>
        <n-tab name="0">草稿</n-tab>
        <n-tab name="2">已发布</n-tab>
        <n-tab name="3">已驳回</n-tab>
      </n-tabs>
      <n-input
        v-model:value="keyword"
        placeholder="按标题搜索"
        clearable
        size="small"
        style="width: 200px"
        @keyup.enter="onFilterChange"
      />
      <n-button size="small" @click="onFilterChange">查询</n-button>
    </n-space>

    <n-data-table
      :columns="columns"
      :data="list"
      :loading="loading"
      :bordered="true"
      :row-key="(row: ArticlePageItem) => row.id"
      :pagination="pagination"
      remote
      class="mt-4"
      @update:page="onPageChange"
    />

    <!-- 驳回原因 -->
    <n-modal
      v-model:show="showReject"
      preset="card"
      title="驳回文章"
      style="width: 480px"
      :mask-closable="false"
    >
      <n-form-item label="驳回原因（作者可见）" required>
        <n-input
          v-model:value="rejectReason"
          type="textarea"
          :rows="3"
          maxlength="500"
          show-count
          placeholder="例如：正文缺少关键步骤说明，请补充后重新提交"
        />
      </n-form-item>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showReject = false">取消</n-button>
          <n-button type="error" :loading="submitting" @click="confirmReject">确认驳回</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { h, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { NButton, NSpace, NTag, useDialog, useMessage, type DataTableColumn } from 'naive-ui'
import * as articleApi from '@/api/article'
import { ArticleStatusMap, type ArticlePageItem } from '@/api/article'

const router = useRouter()
const message = useMessage()
const dialog = useDialog()

const list = ref<ArticlePageItem[]>([])
const loading = ref(false)
const submitting = ref(false)
const keyword = ref('')
const statusFilter = ref<string>('all')
const showReject = ref(false)
const rejectReason = ref('')
const rejectId = ref<number | null>(null)

const pagination = ref({ page: 1, pageSize: 10, itemCount: 0 })

function actionButton(text: string, type: 'primary' | 'error' | 'warning' | 'default' | 'info', handler: () => void) {
  return h(
    NButton,
    { text: true, size: 'small', type, onClick: handler },
    { default: () => text },
  )
}

const columns: DataTableColumn<ArticlePageItem>[] = [
  {
    title: '标题',
    key: 'title',
    ellipsis: { tooltip: true },
    render(row) {
      const nodes = []
      if (row.isTop === 1) {
        nodes.push(h(NTag, { size: 'tiny', type: 'warning', style: 'margin-right:6px' }, { default: () => '置顶' }))
      }
      nodes.push(h('span', { style: 'cursor:pointer', onClick: () => router.push(`/article/${row.id}`) }, row.title))
      return h('div', nodes)
    },
  },
  { title: '作者', key: 'authorName', width: 100 },
  { title: '分类', key: 'categoryName', width: 90 },
  {
    title: '标签',
    key: 'tags',
    width: 160,
    render(row) {
      const tags = row.tags || []
      if (!tags.length) return h('span', { style: 'color:#bbb' }, '—')
      return h(NSpace, { size: 4 }, {
        default: () => tags.map((t) => h(NTag, { size: 'tiny', type: 'info', bordered: false }, { default: () => t })),
      })
    },
  },
  {
    title: '状态',
    key: 'status',
    width: 90,
    render(row) {
      const meta = ArticleStatusMap[row.status]
      return h(NTag, { size: 'small', type: meta?.type ?? 'default' }, { default: () => meta?.label ?? '未知' })
    },
  },
  { title: '阅读', key: 'viewCount', width: 70 },
  { title: '更新时间', key: 'createTime', width: 165, render: (row) => row.publishTime || row.createTime || '—' },
  {
    title: '操作',
    key: 'actions',
    width: 250,
    render(row) {
      const buttons = [
        actionButton('编辑', 'primary', () => router.push(`/editor/${row.id}`)),
      ]
      if (row.status === 1) {
        buttons.push(actionButton('通过', 'primary', () => handlePublish(row)))
        buttons.push(actionButton('驳回', 'error', () => openReject(row)))
      }
      if (row.status === 2) {
        buttons.push(actionButton('下架', 'warning', () => handleOffline(row)))
      }
      buttons.push(actionButton('删除', 'error', () => handleDelete(row)))
      return h(NSpace, { size: 8 }, { default: () => buttons })
    },
  },
]

async function fetchData() {
  loading.value = true
  try {
    const res = await articleApi.getAdminArticlePage({
      status: statusFilter.value === 'all' ? undefined : Number(statusFilter.value),
      keyword: keyword.value || undefined,
      page: pagination.value.page,
      size: pagination.value.pageSize,
    })
    list.value = res.records || []
    pagination.value.itemCount = res.total || 0
  } catch (e: any) {
    message.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function onFilterChange() {
  pagination.value.page = 1
  fetchData()
}

function onPageChange(page: number) {
  pagination.value.page = page
  fetchData()
}

async function handlePublish(row: ArticlePageItem) {
  try {
    await articleApi.publishArticle(row.id)
    message.success('已发布，前台可见')
    await fetchData()
  } catch (e: any) {
    message.error(e.message || '发布失败')
  }
}

function openReject(row: ArticlePageItem) {
  rejectId.value = row.id
  rejectReason.value = ''
  showReject.value = true
}

async function confirmReject() {
  if (!rejectId.value) return
  if (!rejectReason.value.trim()) {
    message.warning('请填写驳回原因')
    return
  }
  submitting.value = true
  try {
    await articleApi.rejectArticle(rejectId.value, rejectReason.value.trim())
    message.success('已驳回')
    showReject.value = false
    await fetchData()
  } catch (e: any) {
    message.error(e.message || '驳回失败')
  } finally {
    submitting.value = false
  }
}

async function handleOffline(row: ArticlePageItem) {
  try {
    await articleApi.offlineArticle(row.id)
    message.success('已下架（回到草稿）')
    await fetchData()
  } catch (e: any) {
    message.error(e.message || '下架失败')
  }
}

function handleDelete(row: ArticlePageItem) {
  dialog.warning({
    title: '删除文章',
    content: `确定删除《${row.title}》吗？删除后前台不再展示。`,
    positiveText: '删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await articleApi.deleteArticle(row.id)
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
.article-manage {
  max-width: 1280px;
}
.toolbar {
  margin-top: 16px;
}
.mt-4 {
  margin-top: 16px;
}
</style>
