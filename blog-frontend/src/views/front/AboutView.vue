<template>
  <div class="about-page">
    <n-h2>关于本站</n-h2>

    <n-card class="about-card">
      <n-h3>个人博客</n-h3>
      <n-p>
        这是一个基于 Spring Boot + Vue 3 构建的个人博客系统。
        前端使用 Naive UI 组件库，后端使用 MyBatis-Plus 和 Sa-Token 实现权限管理。
      </n-p>

      <n-h3>技术栈</n-h3>
      <n-space>
        <n-tag v-for="tech in techStack" :key="tech" :type="tech.type" size="medium" round>
          {{ tech.name }}
        </n-tag>
      </n-space>

      <n-h3>功能特性</n-h3>
      <n-ul>
        <n-li>文章发布与管理（Markdown 支持）</n-li>
        <n-li>分类与标签管理</n-li>
        <n-li>用户注册与登录</n-li>
        <n-li>文章搜索与归档</n-li>
        <n-li>后台管理面板</n-li>
      </n-ul>
    </n-card>
  </div>
</template>

<script setup lang="ts">
const techStack = [
  { name: 'Spring Boot 3', type: 'success' },
  { name: 'MyBatis-Plus', type: 'info' },
  { name: 'Sa-Token', type: 'warning' },
  { name: 'MySQL', type: 'primary' },
  { name: 'Vue 3', type: 'success' },
  { name: 'Naive UI', type: 'info' },
  { name: 'TypeScript', type: 'primary' },
  { name: 'Vite', type: 'warning' },
]
</script>

<style scoped>
.about-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}
.about-card {
  line-height: 1.8;
}
</style>