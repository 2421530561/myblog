<template>
  <div class="article-editor">
    <n-page-header @back="goBack">
      <template #title>{{ isEdit ? '编辑文章' : '写文章' }}</template>
      <template #subtitle>
        <n-space align="center" size="small">
          <n-tag size="small" :type="statusTagType">{{ statusDesc }}</n-tag>
          <n-text v-if="isEdit" depth="3" style="font-size: 12px">保存后按所选动作流转状态</n-text>
        </n-space>
      </template>
      <template #extra>
        <n-space>
          <n-button @click="goBack">取消</n-button>
          <n-button :loading="saving" @click="handleSave(0)">保存草稿</n-button>
          <n-button type="primary" :loading="saving" @click="handleSave(1)">提交审核</n-button>
          <n-button v-if="isAdmin" type="success" :loading="saving" @click="handleSave(2)">
            {{ status === 2 ? '保存并更新' : '直接发布' }}
          </n-button>
        </n-space>
      </template>
    </n-page-header>

    <n-alert v-if="rejectReason" type="error" class="reject-alert" closable @close="rejectReason = ''">
      驳回原因：{{ rejectReason }}
    </n-alert>

    <n-divider />

    <n-form ref="formRef" :model="form" :rules="rules" label-placement="top" class="editor-form">
      <n-form-item label="标题" path="title">
        <n-input v-model:value="form.title" placeholder="请输入文章标题" maxlength="200" show-count />
      </n-form-item>

      <n-grid :cols="2" :x-gap="16">
        <n-form-item-gi label="分类" path="categoryId">
          <n-select
            v-model:value="form.categoryId"
            placeholder="选择分类"
            :options="categoryOptions"
            clearable
          />
        </n-form-item-gi>
        <n-form-item-gi label="标签" path="tagIds">
          <n-select
            v-model:value="form.tagIds"
            placeholder="选择标签（可多选）"
            :options="tagOptions"
            multiple
            clearable
          />
        </n-form-item-gi>
      </n-grid>

      <n-form-item label="封面图" path="cover">
        <n-space align="center" :size="12">
          <n-upload
            :show-file-list="false"
            accept="image/jpeg,image/png,image/webp,image/gif"
            :custom-request="handleCoverUpload"
          >
            <n-button :loading="coverUploading" size="small">上传封面</n-button>
          </n-upload>
          <n-input
            v-model:value="form.cover"
            placeholder="也可直接填写图片 URL"
            style="width: 380px"
          />
          <img v-if="form.cover" :src="form.cover" class="cover-preview" alt="封面预览" />
        </n-space>
      </n-form-item>

      <n-form-item label="摘要" path="summary">
        <n-input
          v-model:value="form.summary"
          type="textarea"
          placeholder="文章摘要（留空时自动截取正文前 150 字）"
          :rows="2"
          maxlength="500"
          show-count
        />
      </n-form-item>
    </n-form>

    <div class="content-label">
      <span>正文内容</span>
      <n-text depth="3" style="font-size: 12px">
        支持 Markdown（分屏预览 / 所见即所得），可直接粘贴或拖拽图片上传
      </n-text>
    </div>
    <div ref="editorHost" class="vditor-host"></div>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessage, type FormInst, type FormRules, type UploadCustomRequestOptions } from 'naive-ui'
import Vditor from 'vditor'
import 'vditor/dist/index.css'
import * as articleApi from '@/api/article'
import * as categoryApi from '@/api/category'
import * as tagApi from '@/api/tag'
import * as uploadApi from '@/api/upload'
import { useUserStore } from '@/stores/user'
import type { Category } from '@/api/category'
import type { Tag } from '@/api/tag'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const userStore = useUserStore()

const isAdmin = computed(() => userStore.userInfo?.role === 'ADMIN')
const isEdit = computed(() => !!route.params.id)

const saving = ref(false)
const coverUploading = ref(false)
const formRef = ref<FormInst | null>(null)
const editorHost = ref<HTMLElement | null>(null)
let vditor: Vditor | null = null

const status = ref(0)
const rejectReason = ref('')
const statusDesc = computed(() => articleApi.ArticleStatusMap[status.value]?.label ?? '新建')
const statusTagType = computed(() => articleApi.ArticleStatusMap[status.value]?.type ?? 'default')

const categories = ref<Category[]>([])
const tags = ref<Tag[]>([])
const categoryOptions = computed(() => categories.value.map((c) => ({ label: c.name, value: c.id })))
const tagOptions = computed(() => tags.value.map((t) => ({ label: t.name, value: t.id })))

const form = ref({
  title: '',
  summary: '',
  cover: '',
  categoryId: null as number | null,
  tagIds: [] as number[],
})

const rules: FormRules = {
  title: [{ required: true, message: '请输入文章标题' }],
}

function goBack() {
  router.push(isAdmin.value ? '/admin/articles' : '/profile')
}

/** 初始化 Vditor（cdn 指向本地 /vditor/dist，离线可用） */
function initEditor(value: string) {
  if (!editorHost.value) return
  vditor = new Vditor(editorHost.value, {
    height: 520,
    mode: 'ir',
    value,
    cdn: '/vditor',
    placeholder: '开始写作吧……支持 Markdown 语法与图片上传',
    // 关闭本地缓存：编辑回显以服务端内容为准，避免缓存串稿
    cache: { enable: false },
    theme: 'classic',
    toolbar: [
      'headings', 'bold', 'italic', 'strike', '|',
      'list', 'ordered-list', 'check', 'outdent', 'indent', '|',
      'quote', 'line', 'code', 'inline-code', '|',
      'link', 'table', 'upload', '|',
      'undo', 'redo', '|',
      'edit-mode', 'preview', 'fullscreen',
    ],
    upload: {
      url: '/api/upload/image',
      fieldName: 'file',
      accept: 'image/jpeg,image/png,image/webp,image/gif',
      headers: userStore.token ? { Authorization: userStore.token } : {},
      format(files, responseText) {
        const succMap: Record<string, string> = {}
        const errFiles: string[] = []
        try {
          const res = JSON.parse(responseText) as { code: number; message?: string; data?: { url: string } }
          if (res.code === 0 && res.data?.url) {
            files.forEach((file) => {
              succMap[file.name] = res.data!.url
            })
          } else {
            files.forEach((file) => errFiles.push(file.name))
            message.error(res.message || '图片上传失败')
          }
        } catch {
          files.forEach((file) => errFiles.push(file.name))
          message.error('图片上传失败')
        }
        return JSON.stringify({ msg: '', code: 0, data: { errFiles, succMap } })
      },
    },
    preview: {
      hljs: { style: 'github', lineNumber: true },
      markdown: { toc: true, mark: true },
    },
  })
}

async function handleCoverUpload(options: UploadCustomRequestOptions) {
  const file = options.file.file
  if (!file) return
  coverUploading.value = true
  try {
    const res = await uploadApi.uploadImage(file)
    form.value.cover = res.url
    options.onFinish()
    message.success('封面上传成功')
  } catch (e: any) {
    options.onError()
    message.error(e.message || '封面上传失败')
  } finally {
    coverUploading.value = false
  }
}

async function handleSave(action: number) {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  const content = vditor?.getValue() ?? ''
  if (!content.trim()) {
    message.warning('正文内容不能为空')
    return
  }
  saving.value = true
  try {
    const dto: articleApi.ArticleDTO = {
      title: form.value.title,
      summary: form.value.summary,
      content,
      cover: form.value.cover,
      categoryId: form.value.categoryId,
      tagIds: form.value.tagIds,
      action,
    }
    if (isEdit.value) {
      const id = Number(route.params.id)
      await articleApi.updateArticle(id, dto)
      message.success(action === 1 ? '已提交审核' : action === 2 ? '已发布' : '已保存')
      if (action === 0) {
        // 保存草稿后停留在编辑页，刷新状态提示（待审核/已发布被编辑会回到草稿）
        await loadArticle(id)
      }
    } else {
      await articleApi.createArticle(dto)
      message.success(action === 1 ? '已创建并提交审核' : action === 2 ? '已发布' : '草稿已保存')
      goBack()
    }
  } catch (e: any) {
    message.error(e.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function loadArticle(id: number) {
  const data = await articleApi.getArticleForEdit(id)
  form.value = {
    title: data.title,
    summary: data.summary || '',
    cover: data.cover || '',
    categoryId: data.categoryId ?? null,
    tagIds: data.tagIds || [],
  }
  status.value = data.status
  rejectReason.value = data.rejectReason || ''
  if (vditor) {
    vditor.setValue(data.content || '')
  } else {
    initEditor(data.content || '')
  }
}

onMounted(async () => {
  const [cats, tagList] = await Promise.all([
    categoryApi.getCategoryList().catch(() => [] as Category[]),
    tagApi.getTagList().catch(() => [] as Tag[]),
  ])
  categories.value = cats as Category[]
  tags.value = tagList as Tag[]

  if (isEdit.value) {
    const id = Number(route.params.id)
    try {
      await loadArticle(id)
    } catch (e: any) {
      message.error(e.message || '加载文章失败')
      goBack()
    }
  } else {
    initEditor('')
  }
})

onBeforeUnmount(() => {
  vditor?.destroy()
  vditor = null
})
</script>

<style scoped>
.article-editor {
  max-width: 960px;
  margin: 0 auto;
}
.reject-alert {
  margin-top: 12px;
}
.content-label {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin: 4px 0 8px;
  font-size: 14px;
  font-weight: 500;
}
.vditor-host {
  border-radius: 6px;
  overflow: hidden;
}
.cover-preview {
  width: 72px;
  height: 48px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #eee;
}
</style>
