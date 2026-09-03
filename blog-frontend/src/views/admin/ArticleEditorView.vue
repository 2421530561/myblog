<template>
  <div class="article-editor">
    <n-page-header>
      <template #title>
        <span>{{ isEdit ? '编辑文章' : '写文章' }}</span>
      </template>
      <template #extra>
        <n-space>
          <n-button @click="goBack">取消</n-button>
          <n-button :loading="submitting" @click="handleSaveDraft">保存草稿</n-button>
          <n-button type="primary" :loading="submitting" @click="handleSubmitReview">提交审核</n-button>
        </n-space>
      </template>
    </n-page-header>

    <n-divider />

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
          :rows="16"
          class="content-input"
        />
      </n-form-item>
    </n-form>

    <n-divider />

    <n-space justify="space-between">
      <n-text depth="3">提示：正文支持 Markdown 语法，保存为草稿后可继续编辑</n-text>
      <n-space>
        <n-button @click="goBack">取消</n-button>
        <n-button :loading="submitting" @click="handleSaveDraft">保存草稿</n-button>
        <n-button type="primary" :loading="submitting" @click="handleSubmitReview">提交审核</n-button>
      </n-space>
    </n-space>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessage, type FormInst, type FormRules } from 'naive-ui'
import * as articleApi from '@/api/article'
import * as categoryApi from '@/api/category'
import * as tagApi from '@/api/tag'
import type { Category } from '@/api/category'
import type { Tag } from '@/api/tag'

const route = useRoute()
const router = useRouter()
const message = useMessage()

const isEdit = ref(false)
const editId = ref<number | null>(null)
const submitting = ref(false)
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

function goBack() {
  router.push('/admin/articles')
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
    goBack()
  } catch (e: any) {
    message.error(e.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  await Promise.all([fetchCategories(), fetchTags()])

  // 如果是编辑模式，根据路由参数加载文章
  const id = route.params.id as string
  if (id) {
    isEdit.value = true
    editId.value = Number(id)
    try {
      const res: any = await articleApi.getDetail(Number(id))
      form.value = {
        title: res.title || '',
        summary: res.summary || '',
        content: res.contentHtml || '',
        cover: res.cover || '',
        categoryId: res.categoryId || null,
        tagIds: res.tagIds || [],
      }
    } catch (e: any) {
      message.error('加载文章失败')
      goBack()
    }
  }
})

async function fetchCategories() {
  categories.value = (await categoryApi.getCategoryList()) as any
}

async function fetchTags() {
  tags.value = (await tagApi.getTagList()) as any
}
</script>

<style scoped>
.article-editor {
  max-width: 900px;
  margin: 0 auto;
}
.flex-1 {
  flex: 1;
}
.editor-form {
  padding: 0 4px;
}
.content-input :deep(textarea) {
  font-family: 'JetBrains Mono', 'Fira Code', 'Consolas', monospace;
  font-size: 14px;
  line-height: 1.7;
}
</style>