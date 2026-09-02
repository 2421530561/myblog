<template>
  <div class="auth-page">
    <n-card title="注册" class="auth-card">
      <n-form ref="formRef" :model="form" :rules="rules">
        <n-form-item path="username" label="用户名">
          <n-input v-model:value="form.username" placeholder="请输入用户名" />
        </n-form-item>
        <n-form-item path="email" label="邮箱">
          <n-input v-model:value="form.email" placeholder="请输入邮箱" />
        </n-form-item>
        <n-form-item path="password" label="密码">
          <n-input v-model:value="form.password" type="password" placeholder="请输入密码（至少6位）" />
        </n-form-item>
        <n-button type="primary" block :loading="loading" @click="handleRegister">注册</n-button>
      </n-form>
      <p class="auth-tip">
        已有账号？<router-link to="/login">立即登录</router-link>
      </p>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import * as authApi from '@/api/auth'
import type { FormInst, FormRules } from 'naive-ui'

const router = useRouter()
const formRef = ref<FormInst | null>(null)
const loading = ref(false)
const form = ref({ username: '', email: '', password: '' })

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名' },
    { min: 2, max: 50, message: '用户名长度需在 2-50 之间' },
  ],
  email: [
    { required: true, message: '请输入邮箱' },
    { type: 'email', message: '邮箱格式不正确' },
  ],
  password: [
    { required: true, message: '请输入密码' },
    { min: 6, max: 50, message: '密码长度需在 6-50 之间' },
  ],
}

async function handleRegister() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await authApi.register(form.value)
    router.push('/login')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  display: flex;
  justify-content: center;
  padding-top: 80px;
}
.auth-card {
  width: 400px;
}
.auth-tip {
  text-align: center;
  margin-top: 16px;
  color: #999;
}
</style>