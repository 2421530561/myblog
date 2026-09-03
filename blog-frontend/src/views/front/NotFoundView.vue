<template>
  <div class="not-found">
    <n-result status="404" title="页面未找到" description="你访问的页面不存在或已被移除">
      <template #footer>
        <n-space justify="center">
          <n-button type="primary" @click="router.push('/')">返回首页</n-button>
          <n-button @click="router.back()">返回上一页</n-button>
        </n-space>
      </template>
    </n-result>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<style scoped>
.not-found {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 60vh;
}
</style>