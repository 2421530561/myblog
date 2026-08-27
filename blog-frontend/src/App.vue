<template>
  <n-config-provider :theme-overrides="themeOverrides">
    <n-message-provider>
      <router-view />
    </n-message-provider>
  </n-config-provider>
</template>

<script setup lang="ts">
// 全局主题配置（后续可在这里定制 Naive UI 主题色，如暗色模式）
const themeOverrides = {}
</script>
