<template>
  <div class="comment-manage">
    <n-page-header>
      <template #title>评论管理</template>
    </n-page-header>

    <n-empty description="评论管理功能将在 M3 里程碑实现" class="mt-8">
      <template #extra>
        <n-tag type="info" size="small">M3 计划中</n-tag>
      </template>
    </n-empty>

    <n-card title="计划功能" class="mt-4">
      <n-ul>
        <n-li>查看所有评论</n-li>
        <n-li>删除违规评论</n-li>
        <n-li>评论审核</n-li>
      </n-ul>
    </n-card>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped>
.comment-manage {
  max-width: 960px;
}
.mt-8 {
  margin-top: 60px;
}
.mt-4 {
  margin-top: 16px;
}
</style>