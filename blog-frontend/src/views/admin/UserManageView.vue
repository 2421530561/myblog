<template>
  <div class="user-manage">
    <n-page-header>
      <template #title>用户管理</template>
    </n-page-header>

    <n-empty description="用户管理功能将在 M3-M4 里程碑实现" class="mt-8">
      <template #extra>
        <n-tag type="info" size="small">M3-M4 计划中</n-tag>
      </template>
    </n-empty>

    <n-card title="计划功能" class="mt-4">
      <n-ul>
        <n-li>查看所有注册用户</n-li>
        <n-li>禁用/启用用户账号</n-li>
        <n-li>用户角色管理</n-li>
      </n-ul>
    </n-card>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped>
.user-manage {
  max-width: 960px;
}
.mt-8 {
  margin-top: 60px;
}
.mt-4 {
  margin-top: 16px;
}
</style>