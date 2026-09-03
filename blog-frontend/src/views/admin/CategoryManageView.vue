<template>
  <div class="category-manage">
    <n-page-header>
      <template #title>分类管理</template>
      <template #extra>
        <n-button type="primary" @click="openCreateModal">新增分类</n-button>
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

    <!-- 新增/编辑弹窗 -->
    <n-modal v-model:show="showModal" :title="isEdit ? '编辑分类' : '新增分类'" preset="card" style="width: 480px">
      <n-form ref="formRef" :model="form" :rules="rules" label-placement="top">
        <n-form-item label="分类名称" path="name">
          <n-input v-model:value="form.name" placeholder="请输入分类名称" maxlength="50" />
        </n-form-item>
        <n-form-item label="排序值" path="sort">
          <n-input-number v-model:value="form.sort" placeholder="排序值（越小越靠前）" :min="0" :max="9999" class="w-full" />
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
import * as categoryApi from '@/api/category'
import type { Category } from '@/api/category'

const message = useMessage()
const list = ref<Category[]>([])
const loading = ref(false)
const submitting = ref(false)
const showModal = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)
const formRef = ref<FormInst | null>(null)

const form = ref({ name: '', sort: 0 })

const rules: FormRules = {
  name: [{ required: true, message: '请输入分类名称' }],
}

const columns: DataTableColumn<Category>[] = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '名称', key: 'name' },
  { title: '排序', key: 'sort', width: 80 },
  { title: '创建时间', key: 'createTime', width: 180 },
  {
    title: '操作',
    key: 'actions',
    width: 180,
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
            style: 'cursor: pointer; color: #d03050;',
            onClick: () => handleDelete(row.id),
          },
          '删除'
        ),
      ])
    },
  },
]

async function fetchData() {
  loading.value = true
  try {
    list.value = (await categoryApi.getCategoryList()) as any
  } finally {
    loading.value = false
  }
}

function openCreateModal() {
  isEdit.value = false
  editId.value = null
  form.value = { name: '', sort: 0 }
  showModal.value = true
}

function openEditModal(row: Category) {
  isEdit.value = true
  editId.value = row.id
  form.value = { name: row.name, sort: row.sort }
  showModal.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (isEdit.value && editId.value) {
      await categoryApi.updateCategory(editId.value, form.value.name, form.value.sort)
      message.success('修改成功')
    } else {
      await categoryApi.createCategory(form.value.name, form.value.sort)
      message.success('新增成功')
    }
    showModal.value = false
    await fetchData()
  } catch (e: any) {
    message.error(e.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

async function handleDelete(id: number) {
  const confirm = window.confirm('确定要删除该分类吗？')
  if (!confirm) return
  try {
    await categoryApi.deleteCategory(id)
    message.success('删除成功')
    await fetchData()
  } catch (e: any) {
    message.error(e.message || '删除失败')
  }
}

onMounted(fetchData)
</script>

<style scoped>
.category-manage {
  max-width: 960px;
}
.mt-4 {
  margin-top: 16px;
}
.w-full {
  width: 100%;
}
</style>