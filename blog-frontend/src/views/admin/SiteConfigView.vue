<template>
  <div class="site-config">
    <n-page-header>
      <template #title>站点设置</template>
    </n-page-header>

    <n-empty description="站点配置功能将在 M4 里程碑实现" class="mt-8">
      <template #extra>
        <n-tag type="info" size="small">M4 计划中</n-tag>
      </template>
    </n-empty>

    <n-card title="计划功能" class="mt-4">
      <n-ul>
        <n-li>站点名称 / 副标题</n-li>
        <n-li>SEO 关键词与描述</n-li>
        <n-li>关于页面内容管理</n-li>
        <n-li>站点公告</n-li>
      </n-ul>
    </n-card>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped>
.site-config {
  max-width: 960px;
}
.mt-8 {
  margin-top: 60px;
}
.mt-4 {
  margin-top: 16px;
}
</style>