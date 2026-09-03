<template>
  <div class="friends-page">
    <n-h2>友情链接</n-h2>
    <n-text depth="3">欢迎交换友链 ~</n-text>

    <n-grid :cols="3" :x-gap="16" :y-gap="16" class="mt-4">
      <n-gi v-for="link in friendLinks" :key="link.name">
        <n-card hoverable class="friend-card" @click="openLink(link.url)">
          <template #header>
            <n-space align="center" size="12">
              <n-avatar :src="link.avatar" round :size="40" />
              <div>
                <div class="friend-name">{{ link.name }}</div>
                <n-text depth="3" style="font-size: 12px;">{{ link.desc }}</n-text>
              </div>
            </n-space>
          </template>
        </n-card>
      </n-gi>
    </n-grid>

    <n-empty v-if="friendLinks.length === 0" description="暂无友链" class="mt-4" />
  </div>
</template>

<script setup lang="ts">
interface FriendLink {
  name: string
  url: string
  desc: string
  avatar?: string
}

// TODO: 后端友链接口实现后，改为从 API 获取
const friendLinks: FriendLink[] = [
  {
    name: '示例博客',
    url: 'https://example.com',
    desc: '一个示例博客',
    avatar: '',
  },
]

function openLink(url: string) {
  window.open(url, '_blank')
}
</script>

<style scoped>
.friends-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}
.mt-4 {
  margin-top: 16px;
}
.friend-card {
  cursor: pointer;
}
.friend-name {
  font-weight: 600;
  font-size: 15px;
}
</style>