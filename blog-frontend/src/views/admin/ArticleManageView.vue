<template>
  <div class="article-manage">
    <n-page-header>
      <template #title>文章管理</template>
      <template #extra>
        <n-button type="primary" @click="openCreateModal">写文章</n-button>
      </template>
    </n-page-header>

    <!-- 文章列表 -->
    <n-data-table
      :columns="columns"
      :data="list"
      :loading="loading"
      :bordered="true"
      :pagination="pagination"
      class="mt-4"
      @update:page="onPageChange"
    />

    <!-- 创建/编辑文章弹窗 -->
    <n-modal
      v-model:show="showEditor"
      :title="isEdit ? '编辑文章' : '写文章'"
      preset="card"
      style="width: 800px; max-height: 90vh;"
      :mask-closable="false"
      class="article-editor-modal"
    >
      <n-form ref="formRef" :model="form" :rules="rules" label-placement="top" class="editor-form">
        <n-form-item label="标题" path="title">
          <n-input v-model:value="form.title" placeholder="请输入文章标题" maxlength="50" />
        </n-form-item>

        <n-space :size="16">
          <n-form-item label="分类" path="categoryId" class="flex-1">
            <n-select
              v-model:value="form.categoryId"
              placeholder="选择分类"
              :options="categoryOptions"
              clearable
            />
          </n-form-item>
          <n-form-item label="标签" path="tagIds" class="flex-1">
            <n-select
              v-model:value="form.tagIds"
              placeholder="选择标签（可多选）"
              :options="tagOptions"
              multiple
              clearable
            />
          </n-form-item>
        </n-space>

        <n-form-item label="封面图" path="cover">
          <n-input v-model:value="form.cover" placeholder="输入封面图片URL，或点击上传" />
        </n-form-item>

        <n-form-item label="摘要" path="summary">
          <n-input
            v-model:value="form.summary"
            type="textarea"
            placeholder="文章摘要（可选）"
            :rows="2"
            maxlength="200"
            show-count
          />
        </n-form-item>

        <n-form-item label="正文内容" path="content">
          <n-input
            v-model:value="form.content"
            type="textarea"
            placeholder="支持 Markdown 语法&#10;使用 # 标题、**加粗**、- 列表等"
            :rows="12"
            class="content-input"
          />
        </n-form-item>
      </n-form>

      <template #footer>
        <n-space justify="space-between">
          <n-text depth="3">提示：正文支持 Markdown 语法，保存为草稿后可继续编辑</n-text>
          <n-space>
            <n-button @click="showEditor = false">取消</n-button>
            <n-button :loading="submitting" @click="handleSaveDraft">保存草稿</n-button>
            <n-button type="primary" :loading="submitting" @click="handleSubmitReview">提交审核</n-button>
          </n-space>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, h } from 'vue'
import { useMessage, type DataTableColumn, type FormInst, type FormRules } from 'naive-ui'
import * as articleApi from '@/api/article'
import * as categoryApi from '@/api/category'
import * as tagApi from '@/api/tag'
import type { ArticlePageItem } from '@/api/article'
import type { Category } from '@/api/category'
import type { Tag } from '@/api/tag'

const message = useMessage()
const list = ref<ArticlePageItem[]>([])
const loading = ref(false)
const submitting = ref(false)
const showEditor = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)
const formRef = ref<FormInst | null>(null)

const categories = ref<Category[]>([])
const tags = ref<Tag[]>([])

const categoryOptions = computed(() =>
  categories.value.map((c) => ({ label: c.name, value: c.id }))
)
const tagOptions = computed(() =>
  tags.value.map((t) => ({ label: t.name, value: t.id }))
)

const form = ref({
  title: '',
  summary: '',
  content: '',
  cover: '',
  categoryId: null as number | null,
  tagIds: [] as number[],
})

const rules: FormRules = {
  title: [{ required: true, message: '请输入文章标题' }],
  content: [{ required: true, message: '请输入正文内容' }],
}

const statusMap: Record<number, string> = {
  0: '草稿',
  1: '待审核',
  2: '已发布',
}

const statusTypeMap: Record<number, string> = {
  0: 'warning',
  1: 'info',
  2: 'success',
}

const columns: DataTableColumn<ArticlePageItem>[] = [
  { title: '标题', key: 'title', ellipsis: true },
  { title: '分类', key: 'categoryName', width: 100 },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render(row) {
      const status = (row as any).status as number
      return h(
        'n-tag',
        { size: 'small', type: (statusTypeMap[status] || 'default') as any },
        { default: () => statusMap[status] || '未知' }
      )
    },
  },
  { title: '阅读', key: 'viewCount', width: 70 },
  { title: '点赞', key: 'likeCount', width: 70 },
  { title: '发布时间', key: 'publishTime', width: 170 },
  {
    title: '操作',
    key: 'actions',
    width: 200,
    render(row) {
      return h('div', { style: 'display: flex; gap: 8px;' }, [
        h(
          'a',
          {
            style: 'cursor: pointer; color: #2080f0;',
            onClick: () => openEditModal(row),
          },
          '编辑'
        ),
        h(
          'a',
          {
            style: 'cursor: pointer; color: #2080f0;',
            onClick: () => handleSubmitArticle(row.id),
          },
          '提交审核'
        ),
        h(
          'a',
          {
            style: 'cursor: pointer; color: #d03050;',
            onClick: () => handleDelete(row.id),
          },
          '删除'
        ),
      ])
    },
  },
]

const pagination = ref({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: false,
})

async function fetchData() {
  loading.value = true
  try {
    const res = (await articleApi.getPublishedPage({
      page: pagination.value.page,
      size: pagination.value.pageSize,
    })) as any
    list.value = res.records || []
    pagination.value.itemCount = res.total || 0
  } finally {
    loading.value = false
  }
}

async function fetchCategories() {
  categories.value = (await categoryApi.getCategoryList()) as any
}

async function fetchTags() {
  tags.value = (await tagApi.getTagList()) as any
}

function onPageChange(page: number) {
  pagination.value.page = page
  fetchData()
}

function openCreateModal() {
  isEdit.value = false
  editId.value = null
  form.value = {
    title: '',
    summary: '',
    content: '',
    cover: '',
    categoryId: null,
    tagIds: [],
  }
  showEditor.value = true
}

function openEditModal(row: ArticlePageItem) {
  isEdit.value = true
  editId.value = row.id
  form.value = {
    title: row.title,
    summary: row.summary || '',
    content: '',
    cover: row.cover || '',
    categoryId: null,
    tagIds: [],
  }
  showEditor.value = true
  // 获取文章详情填充编辑表单
  articleApi.getDetail(row.id).then((res: any) => {
    form.value.content = res.contentHtml || ''
  }).catch(() => {
    // 忽略错误
  })
}

async function handleSaveDraft() {
  await doSubmit(0)
}

async function handleSubmitReview() {
  await doSubmit(1)
}

async function doSubmit(action: number) {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    const dto = {
      ...form.value,
      action,
    }
    if (isEdit.value && editId.value) {
      await articleApi.updateArticle(editId.value, dto)
      message.success('保存成功')
    } else {
      await articleApi.createArticle(dto)
      message.success('创建成功')
    }
    showEditor.value = false
    await fetchData()
  } catch (e: any) {
    message.error(e.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

async function handleSubmitArticle(id: number) {
  try {
    await articleApi.submitArticle(id)
    message.success('已提交审核')
    await fetchData()
  } catch (e: any) {
    message.error(e.message || '提交失败')
  }
}

async function handleDelete(id: number) {
  const confirm = window.confirm('确定要删除该文章吗？')
  if (!confirm) return
  try {
    await articleApi.deleteArticle(id)
    message.success('删除成功')
    await fetchData()
  } catch (e: any) {
    message.error(e.message || '删除失败')
  }
}

onMounted(async () => {
  await Promise.all([fetchCategories(), fetchTags()])
  await fetchData()
})
</script>

<style scoped>
.article-manage {
  max-width: 1200px;
}
.mt-4 {
  margin-top: 16px;
}
.flex-1 {
  flex: 1;
}
.editor-form {
  overflow-y: auto;
  max-height: calc(90vh - 140px);
  padding-right: 4px;
}
.content-input :deep(textarea) {
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 14px;
  line-height: 1.6;
}
.article-editor-modal :deep(.n-card__content) {
  padding-top: 12px;
}
</style>