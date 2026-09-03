<template>
  <div class="home">
    <!-- Hero 区域 -->
    <div class="hero">
      <div class="hero-content">
        <h1 class="hero-title">{{ appStore.siteConfig.siteName }}</h1>
        <p class="hero-slogan">{{ appStore.siteConfig.slogan }}</p>
        <div class="hero-actions">
          <n-button type="primary" size="large" @click="router.push('/archive')">浏览归档</n-button>
          <n-button v-if="userStore.token && userStore.userInfo" size="large" @click="router.push('/editor')">写文章</n-button>
        </div>
      </div>
      <div class="hero-decoration">
        <div class="shape shape-1"></div>
        <div class="shape shape-2"></div>
        <div class="shape shape-3"></div>
      </div>
    </div>

    <!-- 内容区：左侧文章列表 + 右侧边栏 -->
    <div class="content-wrapper">
      <div class="main-area">
        <!-- 分类快速筛选 -->
        <div class="filter-bar" v-if="categories.length > 0">
          <n-space size="small" align="center">
            <span class="filter-label">分类：</span>
            <n-button
              :type="!activeCategoryId ? 'primary' : 'default'"
              size="tiny"
              @click="filterByCategory(undefined)"
            >全部</n-button>
            <n-button
              v-for="cat in categories"
              :key="cat.id"
              :type="activeCategoryId === cat.id ? 'primary' : 'default'"
              size="tiny"
              @click="filterByCategory(cat.id)"
            >{{ cat.name }}</n-button>
          </n-space>
        </div>

        <!-- 文章列表骨架 -->
        <n-skeleton v-if="loading" :height="140" :repeat="3" class="skeleton-item" />
        <!-- 空状态 -->
        <n-empty v-else-if="list.length === 0" description="暂无文章" class="empty-state">
          <template #extra>
            <n-button v-if="userStore.token && userStore.userInfo" type="primary" @click="router.push('/editor')">写第一篇</n-button>
          </template>
        </n-empty>
        <!-- 文章列表 -->
        <template v-else>
          <article
            v-for="item in list"
            :key="item.id"
            class="article-card"
            @click="router.push(`/article/${item.id}`)"
          >
            <div class="card-cover" v-if="item.cover">
              <img :src="item.cover" :alt="item.title" />
            </div>
            <div class="card-body" :class="{ 'no-cover': !item.cover }">
              <div class="card-header">
                <n-tag v-if="item.isTop" type="warning" size="tiny" class="top-tag">置顶</n-tag>
                <h2 class="card-title">{{ item.title }}</h2>
              </div>
              <p class="card-summary">{{ item.summary || '暂无摘要' }}</p>
              <div class="card-meta">
                <n-space size="small" align="center">
                  <n-avatar :src="item.authorAvatar" round size="small" />
                  <span class="meta-item">{{ item.authorName }}</span>
                  <span class="meta-divider">·</span>
                  <span class="meta-item">{{ item.publishTime }}</span>
                  <n-tag v-if="item.categoryName" size="tiny" bordered>{{ item.categoryName }}</n-tag>
                  <span class="meta-divider">·</span>
                  <span class="meta-item">👁 {{ item.viewCount }}</span>
                  <span class="meta-item">👍 {{ item.likeCount }}</span>
                </n-space>
              </div>
            </div>
          </article>
        </template>

        <!-- 分页 -->
        <n-pagination
          v-if="total > size"
          class="pagination"
          :page="page"
          :page-size="size"
          :item-count="total"
          @update:page="onPageChange"
        />
      </div>

      <!-- 右侧边栏 -->
      <aside class="sidebar">
        <!-- 用户卡片 -->
        <n-card v-if="userStore.token && userStore.userInfo" class="sidebar-card" :bordered="true" size="small">
          <template #header>
            <n-space align="center" :size="12">
              <n-avatar :src="userStore.userInfo?.avatar || ''" round :size="40">
                <template v-if="!userStore.userInfo?.avatar">
                  {{ (userStore.userInfo?.nickname || userStore.userInfo?.username || 'U').charAt(0) }}
                </template>
              </n-avatar>
              <div>
                <div class="sidebar-user-name">{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</div>
                <n-text depth="3" style="font-size: 12px;">{{ userStore.userInfo?.role === 'ADMIN' ? '管理员' : '用户' }}</n-text>
              </div>
            </n-space>
          </template>
          <n-space vertical>
            <n-button block size="small" @click="router.push('/editor')">写文章</n-button>
            <n-button block size="small" quaternary @click="router.push('/profile')">个人中心</n-button>
          </n-space>
        </n-card>

        <!-- 分类列表 -->
        <n-card title="分类" class="sidebar-card" :bordered="true" size="small">
          <n-space vertical size="small">
            <n-button
              v-for="cat in categories"
              :key="cat.id"
              :type="activeCategoryId === cat.id ? 'primary' : 'default'"
              size="small"
              block
              quaternary
              @click="filterByCategory(cat.id)"
            >
              <template #default>
                <span class="sidebar-cat-name">{{ cat.name }}</span>
              </template>
            </n-button>
            <n-empty v-if="categories.length === 0" description="暂无分类" />
          </n-space>
        </n-card>

        <!-- 标签云 -->
        <n-card title="标签" class="sidebar-card" :bordered="true" size="small">
          <n-space>
            <n-tag
              v-for="tag in tags"
              :key="tag.id"
              size="small"
              :type="activeTagId === tag.id ? 'primary' : 'default'"
              style="cursor: pointer;"
              @click="filterByTag(tag.id)"
            >{{ tag.name }}</n-tag>
            <n-empty v-if="tags.length === 0" description="暂无标签" />
          </n-space>
        </n-card>

        <!-- 关于 -->
        <n-card class="sidebar-card" :bordered="true" size="small">
          <template #header>
            <n-text depth="3">关于本站</n-text>
          </template>
          <n-text depth="3" style="font-size: 13px;">
            {{ appStore.siteConfig.siteName }} — {{ appStore.siteConfig.slogan }}
          </n-text>
          <template #footer>
            <n-button text size="small" @click="router.push('/about')">了解更多 →</n-button>
          </template>
        </n-card>
      </aside>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import * as articleApi from '@/api/article'
import * as categoryApi from '@/api/category'
import * as tagApi from '@/api/tag'
import type { ArticlePageItem } from '@/api/article'
import type { Category } from '@/api/category'
import type { Tag } from '@/api/tag'

const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()

const list = ref<ArticlePageItem[]>([])
const categories = ref<Category[]>([])
const tags = ref<Tag[]>([])
const loading = ref(true)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const activeCategoryId = ref<number | undefined>()
const activeTagId = ref<number | undefined>()

async function fetchData() {
  loading.value = true
  try {
    const res = await articleApi.getPublishedPage({
      categoryId: activeCategoryId.value,
      tagId: activeTagId.value,
      page: page.value,
      size: size.value,
    })
    const data = res as any
    list.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

async function fetchCategories() {
  try {
    categories.value = (await categoryApi.getCategoryList()) as any
  } catch {
    // 忽略
  }
}

async function fetchTags() {
  try {
    tags.value = (await tagApi.getTagList()) as any
  } catch {
    // 忽略
  }
}

function filterByCategory(id?: number) {
  activeCategoryId.value = id
  activeTagId.value = undefined
  page.value = 1
  fetchData()
}

function filterByTag(id?: number) {
  activeTagId.value = id
  activeCategoryId.value = undefined
  page.value = 1
  fetchData()
}

function onPageChange(p: number) {
  page.value = p
  fetchData()
}

onMounted(async () => {
  await Promise.all([fetchCategories(), fetchTags()])
  await fetchData()
})
</script>

<style scoped>
/* ===== Hero 区域 ===== */
.hero {
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 60px 40px;
  margin-bottom: 32px;
  color: #fff;
}
.hero-content {
  position: relative;
  z-index: 1;
}
.hero-title {
  font-size: 36px;
  font-weight: 800;
  margin: 0 0 8px 0;
  letter-spacing: -0.5px;
}
.hero-slogan {
  font-size: 18px;
  opacity: 0.85;
  margin: 0 0 24px 0;
}
.hero-actions {
  display: flex;
  gap: 12px;
}
.hero-decoration {
  position: absolute;
  top: 0;
  right: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}
.shape {
  position: absolute;
  border-radius: 50%;
  opacity: 0.1;
}
.shape-1 {
  width: 200px;
  height: 200px;
  background: #fff;
  top: -50px;
  right: -30px;
}
.shape-2 {
  width: 120px;
  height: 120px;
  background: #fff;
  bottom: -20px;
  right: 80px;
}
.shape-3 {
  width: 80px;
  height: 80px;
  background: #fff;
  top: 40px;
  right: 160px;
}

/* ===== 双栏布局 ===== */
.content-wrapper {
  display: flex;
  gap: 24px;
  max-width: 1080px;
  margin: 0 auto;
}
.main-area {
  flex: 1;
  min-width: 0;
}
.sidebar {
  width: 260px;
  flex-shrink: 0;
}

/* ===== 筛选栏 ===== */
.filter-bar {
  margin-bottom: 20px;
  padding: 12px 16px;
  background: #f8f9fa;
  border-radius: 8px;
}
.filter-label {
  font-size: 13px;
  color: #666;
  white-space: nowrap;
}

/* ===== 文章卡片 ===== */
.article-card {
  display: flex;
  background: #fff;
  border-radius: 10px;
  overflow: hidden;
  margin-bottom: 16px;
  cursor: pointer;
  border: 1px solid #eee;
  transition: all 0.25s ease;
}
.article-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  border-color: #d0d0d0;
}
.card-cover {
  width: 200px;
  min-height: 140px;
  flex-shrink: 0;
  overflow: hidden;
}
.card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}
.article-card:hover .card-cover img {
  transform: scale(1.05);
}
.card-body {
  flex: 1;
  padding: 18px 20px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.card-body.no-cover {
  padding: 20px 24px;
}
.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
.top-tag {
  flex-shrink: 0;
}
.card-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
  color: #1a1a1a;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.card-summary {
  font-size: 14px;
  color: #666;
  margin: 0 0 12px 0;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.card-meta {
  margin-top: auto;
}
.meta-item {
  font-size: 13px;
  color: #999;
}
.meta-divider {
  color: #ddd;
}

/* ===== 侧边栏 ===== */
.sidebar-card {
  margin-bottom: 16px;
}
.sidebar-user-name {
  font-weight: 600;
  font-size: 14px;
}
.sidebar-cat-name {
  font-size: 13px;
}

/* ===== 通用 ===== */
.skeleton-item {
  margin-bottom: 16px;
  border-radius: 10px;
}
.empty-state {
  margin-top: 60px;
}
.pagination {
  display: flex;
  justify-content: center;
  margin-top: 32px;
  margin-bottom: 24px;
}

/* ===== 响应式 ===== */
@media (max-width: 768px) {
  .sidebar {
    display: none;
  }
  .hero {
    padding: 40px 24px;
  }
  .hero-title {
    font-size: 26px;
  }
  .card-cover {
    width: 120px;
  }
}
</style>