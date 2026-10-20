<template>
  <div class="article-detail" v-if="article">
    <n-h1 class="title">{{ article.title }}</n-h1>
    <n-space class="meta-bar" size="small" align="center">
      <n-avatar :src="article.authorAvatar" round size="small" />
      <span>{{ article.authorName }}</span>
      <span class="divider">|</span>
      <span>{{ formatTime(article.publishTime) }}</span>
      <n-tag v-if="article.categoryName" size="small">{{ article.categoryName }}</n-tag>
      <n-tag v-for="tag in article.tags" :key="tag" size="small" type="info">{{ tag }}</n-tag>
      <span class="divider">|</span>
      <span>👁 {{ article.viewCount }}</span>
    </n-space>

    <img v-if="article.cover" :src="article.cover" class="cover" :alt="article.title" />

    <n-divider />

    <!-- Markdown 渲染结果：后端已做白名单清洗，前端再用 DOMPurify 二次清洗（PRD 6 安全） -->
    <div ref="contentRef" class="article-content" v-html="safeHtml"></div>

    <div class="like-bar">
      <n-button
        round
        size="large"
        :type="article.liked ? 'error' : 'default'"
        :loading="liking"
        @click="onLike"
      >
        {{ article.liked ? '❤️ 已点赞' : '🤍 点赞' }} {{ article.likeCount }}
      </n-button>
    </div>

    <n-divider />

    <!-- ===== 评论区（M3：登录可评、楼中楼、作者/管理员可删） ===== -->
    <section class="comments">
      <h3 class="comments-title">评论 <span class="count">{{ commentTotal }}</span></h3>

      <div v-if="userStore.token" class="comment-editor">
        <n-input
          v-model:value="content"
          type="textarea"
          :autosize="{ minRows: 3, maxRows: 8 }"
          maxlength="1000"
          show-count
          :placeholder="replyTarget ? `回复 @${replyTarget.nickname}：` : '友善发言，理性讨论…'"
        />
        <div class="editor-actions">
          <n-tag v-if="replyTarget" size="small" closable @close="replyTarget = null">
            回复 @{{ replyTarget.nickname }}
          </n-tag>
          <span v-else />
          <n-button type="primary" :loading="submitting" :disabled="!content.trim()" @click="submitComment">
            发表评论
          </n-button>
        </div>
      </div>
      <n-alert v-else type="info" :bordered="false" class="login-tip">
        登录后才能发表评论
        <n-button text type="primary" @click="goLogin">去登录</n-button>
      </n-alert>

      <n-spin :show="commentLoading">
        <div v-if="comments.length" class="comment-list">
          <div v-for="c in comments" :key="c.id" class="comment-item">
            <n-avatar round size="small" :src="c.userAvatar" />
            <div class="comment-body">
              <div class="comment-head">
                <span class="nickname">{{ c.userNickname }}</span>
                <span class="time">{{ formatTime(c.createTime) }}</span>
              </div>
              <div class="comment-text">{{ c.content }}</div>
              <div class="comment-ops">
                <n-button text size="tiny" @click="startReply(c)">回复</n-button>
                <n-button v-if="canDelete(c)" text size="tiny" type="error" @click="removeComment(c)">删除</n-button>
              </div>

              <div v-if="c.children && c.children.length" class="replies">
                <div v-for="r in c.children" :key="r.id" class="reply-item">
                  <n-avatar round size="tiny" :src="r.userAvatar" />
                  <div class="comment-body">
                    <div class="comment-head">
                      <span class="nickname">{{ r.userNickname }}</span>
                      <span class="time">{{ formatTime(r.createTime) }}</span>
                    </div>
                    <div class="comment-text">
                      <span v-if="r.replyNickname" class="reply-to">回复 @{{ r.replyNickname }}：</span>{{ r.content }}
                    </div>
                    <div class="comment-ops">
                      <n-button text size="tiny" @click="startReply(r)">回复</n-button>
                      <n-button v-if="canDelete(r)" text size="tiny" type="error" @click="removeComment(r)">删除</n-button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <n-empty v-else-if="!commentLoading" description="还没有评论，来抢沙发～" size="small" class="empty" />
      </n-spin>

      <n-pagination
        v-if="commentTotal > pageSize"
        class="pager"
        :page="page"
        :page-size="pageSize"
        :item-count="commentTotal"
        @update:page="onPageChange"
      />
    </section>

    <n-divider />
    <n-space>
      <n-button @click="router.push('/')">返回首页</n-button>
      <n-button v-if="canEdit" type="primary" @click="router.push(`/editor/${article.id}`)">编辑文章</n-button>
    </n-space>
  </div>
  <n-skeleton v-else-if="loading" text :repeat="8" class="skeleton" />
  <n-empty v-else description="文章不存在或未发布" />
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useDialog, useMessage } from 'naive-ui'
import DOMPurify from 'dompurify'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'
import * as articleApi from '@/api/article'
import * as commentApi from '@/api/comment'
import type { CommentVO } from '@/api/comment'
import * as likeApi from '@/api/like'
import type { ArticleDetail } from '@/api/article'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const message = useMessage()
const dialog = useDialog()

const article = ref<ArticleDetail | null>(null)
const loading = ref(true)
const contentRef = ref<HTMLElement | null>(null)

// 点赞（M3）
const liking = ref(false)

// 评论区（M3）
const comments = ref<CommentVO[]>([])
const commentTotal = ref(0)
const page = ref(1)
const pageSize = 10
const commentLoading = ref(false)
const submitting = ref(false)
const content = ref('')
const replyTarget = ref<{ id: number; nickname: string } | null>(null)

const safeHtml = computed(() =>
  DOMPurify.sanitize(article.value?.contentHtml || '', { ADD_ATTR: ['target', 'class'] }),
)

const canEdit = computed(() => {
  const user = userStore.userInfo
  if (!user || !article.value) return false
  return user.role === 'ADMIN'
})

function formatTime(value?: string) {
  if (!value) return ''
  return value.replace('T', ' ').slice(0, 16)
}

/** 代码块语法高亮（PRD 7.2 highlight.js） */
function highlightCode() {
  nextTick(() => {
    contentRef.value?.querySelectorAll('pre code').forEach((block) => {
      hljs.highlightElement(block as HTMLElement)
    })
  })
}

watch(safeHtml, highlightCode)

/** 点赞 / 取消点赞（未登录引导登录） */
async function onLike() {
  if (!article.value) return
  if (!userStore.token) {
    message.info('登录后才能点赞')
    goLogin()
    return
  }
  liking.value = true
  try {
    const res = await likeApi.toggleLike(article.value.id)
    article.value.liked = res.liked
    article.value.likeCount = res.count
  } catch (e: any) {
    message.error(e?.message || '操作失败')
  } finally {
    liking.value = false
  }
}

function goLogin() {
  router.push({ path: '/login', query: { redirect: route.fullPath } })
}

/** 加载评论（按顶层评论分页） */
async function loadComments(targetPage = page.value) {
  if (!article.value) return
  commentLoading.value = true
  try {
    const res = await commentApi.getCommentPage(article.value.id, targetPage, pageSize)
    comments.value = res.records || []
    commentTotal.value = res.total || 0
    page.value = res.current || targetPage
  } catch (e: any) {
    message.error(e?.message || '评论加载失败')
  } finally {
    commentLoading.value = false
  }
}

function onPageChange(p: number) {
  page.value = p
  loadComments(p)
}

function startReply(c: CommentVO) {
  if (!userStore.token) {
    message.info('登录后才能回复')
    goLogin()
    return
  }
  replyTarget.value = { id: c.id, nickname: c.userNickname }
}

function canDelete(c: CommentVO) {
  const user = userStore.userInfo
  if (!user) return false
  return user.role === 'ADMIN' || user.id === c.userId
}

async function submitComment() {
  if (!article.value) return
  const text = content.value.trim()
  if (!text) return
  submitting.value = true
  try {
    await commentApi.addComment({
      articleId: article.value.id,
      parentId: replyTarget.value?.id ?? null,
      content: text,
    })
    content.value = ''
    replyTarget.value = null
    message.success('评论成功')
    await loadComments(1) // 新评论在最前，回到第一页
  } catch (e: any) {
    message.error(e?.message || '评论失败')
  } finally {
    submitting.value = false
  }
}

function removeComment(c: CommentVO) {
  dialog.warning({
    title: '删除评论',
    content: '删除后该评论及其全部回复都将不可见，确定删除吗？',
    positiveText: '删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await commentApi.deleteComment(c.id)
        message.success('已删除')
        // 当前页被删空时回退一页
        await loadComments(comments.value.length === 1 && page.value > 1 ? page.value - 1 : page.value)
      } catch (e: any) {
        message.error(e?.message || '删除失败')
      }
    },
  })
}

onMounted(async () => {
  try {
    article.value = (await articleApi.getDetail(Number(route.params.id))) as any
    highlightCode()
    await loadComments(1)
  } catch (e: any) {
    article.value = null
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.article-detail {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}
.title {
  margin-bottom: 8px;
}
.meta-bar {
  color: #999;
  font-size: 14px;
  margin-bottom: 16px;
}
.divider {
  color: #ddd;
}
.cover {
  width: 100%;
  max-height: 360px;
  object-fit: cover;
  border-radius: 8px;
  margin-top: 8px;
}
.skeleton {
  max-width: 800px;
  margin: 40px auto;
}
/* Markdown 正文样式 */
.article-content {
  line-height: 1.8;
  font-size: 16px;
  color: #1f1f1f;
  word-break: break-word;
}
.article-content :deep(h1),
.article-content :deep(h2),
.article-content :deep(h3),
.article-content :deep(h4) {
  margin: 24px 0 12px;
  font-weight: 600;
  line-height: 1.4;
}
.article-content :deep(h2) {
  font-size: 22px;
  border-bottom: 1px solid #eee;
  padding-bottom: 6px;
}
.article-content :deep(p) {
  margin: 12px 0;
}
.article-content :deep(a) {
  color: #2080f0;
  text-decoration: none;
}
.article-content :deep(a:hover) {
  text-decoration: underline;
}
.article-content :deep(img) {
  max-width: 100%;
  border-radius: 6px;
}
.article-content :deep(pre) {
  background: #f6f8fa;
  padding: 12px 16px;
  border-radius: 6px;
  overflow-x: auto;
}
.article-content :deep(code) {
  font-family: 'JetBrains Mono', 'Fira Code', Consolas, monospace;
  font-size: 14px;
}
.article-content :deep(p code),
.article-content :deep(li code) {
  background: #f2f3f5;
  padding: 2px 5px;
  border-radius: 4px;
}
.article-content :deep(blockquote) {
  margin: 12px 0;
  padding: 4px 16px;
  border-left: 4px solid #ddd;
  color: #666;
  background: #fafafa;
}
.article-content :deep(table) {
  border-collapse: collapse;
  margin: 12px 0;
  width: 100%;
}
.article-content :deep(th),
.article-content :deep(td) {
  border: 1px solid #e5e5e5;
  padding: 6px 10px;
}
.article-content :deep(th) {
  background: #fafafa;
}
.article-content :deep(ul),
.article-content :deep(ol) {
  padding-left: 24px;
}

/* ===== 点赞 ===== */
.like-bar {
  display: flex;
  justify-content: center;
  margin: 24px 0 8px;
}

/* ===== 评论区 ===== */
.comments-title {
  font-size: 18px;
  margin: 0 0 16px;
}
.comments-title .count {
  color: #999;
  font-size: 14px;
  font-weight: normal;
}
.comment-editor {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 20px;
}
.editor-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.login-tip {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 20px;
}
.comment-list {
  display: flex;
  flex-direction: column;
  gap: 18px;
}
.comment-item {
  display: flex;
  gap: 10px;
}
.comment-body {
  flex: 1;
  min-width: 0;
}
.comment-head {
  display: flex;
  align-items: baseline;
  gap: 10px;
}
.nickname {
  font-weight: 600;
  font-size: 14px;
  color: #333;
}
.time {
  font-size: 12px;
  color: #aaa;
}
.comment-text {
  margin: 4px 0;
  font-size: 14px;
  line-height: 1.7;
  color: #333;
  white-space: pre-wrap;
  word-break: break-word;
}
.comment-ops {
  display: flex;
  gap: 12px;
}
.reply-to {
  color: #2080f0;
}
.replies {
  margin-top: 10px;
  padding: 10px 12px;
  background: #fafafa;
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.reply-item {
  display: flex;
  gap: 8px;
}
.empty {
  margin: 24px 0;
}
.pager {
  margin-top: 20px;
  justify-content: center;
}
</style>
