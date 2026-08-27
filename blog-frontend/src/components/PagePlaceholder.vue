<template>
  <div class="placeholder">
    <n-result status="info" :title="title" :description="`「${title}」页面框架已就绪，业务逻辑待实现`">
      <template #footer>
        <n-text depth="3">实现计划见根目录 README.md「学习路线」</n-text>
      </template>
    </n-result>
  </div>
</template>

<script setup lang="ts">
defineProps<{ title: string }>()
</script>

<style scoped>
.placeholder {
  max-width: 960px;
  margin: 0 auto;
  padding: 60px 24px;
}
</style>
