<template>
  <div class="tag-manage">
    <n-page-header>
      <template #title>标签管理</template>
      <template #extra>
        <n-button type="primary" @click="openCreateModal">新增标签</n-button>
      </template>
    </n-page-header>

    <n-data-table
      :columns="columns"
      :data="list"
      :loading="loading"
      :bordered="true"
      :pagination="false"
      class="mt-4"
    />

    <!-- 新增标签弹窗 -->
    <n-modal v-model:show="showModal" title="新增标签" preset="card" style="width: 420px">
      <n-form ref="formRef" :model="form" :rules="rules" label-placement="top">
        <n-form-item label="标签名称" path="name">
          <n-input v-model:value="form.name" placeholder="请输入标签名称" maxlength="30" @keyup.enter="handleSubmit" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showModal = false">取消</n-button>
          <n-button type="primary" :loading="submitting" @click="handleSubmit">保存</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { useMessage, type DataTableColumn, type FormInst, type FormRules } from 'naive-ui'
import * as tagApi from '@/api/tag'
import type { Tag } from '@/api/tag'

const message = useMessage()
const list = ref<Tag[]>([])
const loading = ref(false)
const submitting = ref(false)
const showModal = ref(false)
const formRef = ref<FormInst | null>(null)
const form = ref({ name: '' })

const rules: FormRules = {
  name: [{ required: true, message: '请输入标签名称' }],
}

const columns: DataTableColumn<Tag>[] = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '名称', key: 'name' },
  { title: '创建时间', key: 'createTime', width: 180 },
  {
    title: '操作',
    key: 'actions',
    width: 120,
    render(row) {
      return h(
        'a',
        {
          style: 'cursor: pointer; color: #d03050;',
          onClick: () => handleDelete(row.id),
        },
        '删除'
      )
    },
  },
]

async function fetchData() {
  loading.value = true
  try {
    list.value = (await tagApi.getTagList()) as any
  } finally {
    loading.value = false
  }
}

function openCreateModal() {
  form.value = { name: '' }
  showModal.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await tagApi.createTag(form.value.name)
    message.success('新增成功')
    showModal.value = false
    await fetchData()
  } catch (e: any) {
    message.error(e.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

async function handleDelete(id: number) {
  const confirm = window.confirm('确定要删除该标签吗？')
  if (!confirm) return
  try {
    await tagApi.deleteTag(id)
    message.success('删除成功')
    await fetchData()
  } catch (e: any) {
    message.error(e.message || '删除失败')
  }
}

onMounted(fetchData)
</script>

<style scoped>
.tag-manage {
  max-width: 800px;
}
.mt-4 {
  margin-top: 16px;
}
</style>