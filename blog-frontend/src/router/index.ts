import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

/**
 * 路由规划（对应 PRD 4.1/4.3 页面清单）：
 * - 前台页面挂在前台布局下，后台页面挂在后台布局下（/admin）
 * - meta.requiresAuth：需要登录；meta.requiresAdmin：需要 ADMIN 角色
 */
const router = createRouter({
  history: createWebHistory(),
  routes: [
    // ===== 前台布局 =====
    {
      path: '/',
      component: () => import('@/layouts/FrontLayout.vue'),
      children: [
        { path: '', name: 'home', component: () => import('@/views/front/HomeView.vue') },
        { path: 'article/:id', name: 'article-detail', component: () => import('@/views/front/ArticleDetailView.vue') },
        { path: 'category/:id?', name: 'category', component: () => import('@/views/front/CategoryView.vue') },
        { path: 'tag/:id?', name: 'tag', component: () => import('@/views/front/TagView.vue') },
        { path: 'archive', name: 'archive', component: () => import('@/views/front/ArchiveView.vue') },
        { path: 'search', name: 'search', component: () => import('@/views/front/SearchView.vue') },
        { path: 'about', name: 'about', component: () => import('@/views/front/AboutView.vue') },
        { path: 'friends', name: 'friends', component: () => import('@/views/front/FriendLinkView.vue') },
        { path: 'login', name: 'login', component: () => import('@/views/front/LoginView.vue') },
        { path: 'register', name: 'register', component: () => import('@/views/front/RegisterView.vue') },
        {
          path: 'profile',
          name: 'profile',
          component: () => import('@/views/front/ProfileView.vue'),
          meta: { requiresAuth: true },
        },
      ],
    },
    // ===== 后台布局（仅 ADMIN）=====
    {
      path: '/admin',
      component: () => import('@/layouts/AdminLayout.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
      children: [
        { path: '', name: 'admin-dashboard', component: () => import('@/views/admin/DashboardView.vue') },
        { path: 'articles', name: 'admin-articles', component: () => import('@/views/admin/ArticleManageView.vue') },
        { path: 'categories', name: 'admin-categories', component: () => import('@/views/admin/CategoryManageView.vue') },
        { path: 'tags', name: 'admin-tags', component: () => import('@/views/admin/TagManageView.vue') },
        { path: 'comments', name: 'admin-comments', component: () => import('@/views/admin/CommentManageView.vue') },
        { path: 'users', name: 'admin-users', component: () => import('@/views/admin/UserManageView.vue') },
        { path: 'friends', name: 'admin-friends', component: () => import('@/views/admin/FriendLinkManageView.vue') },
        { path: 'config', name: 'admin-config', component: () => import('@/views/admin/SiteConfigView.vue') },
      ],
    },
    // 404 兜底
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('@/views/front/NotFoundView.vue'),
    },
  ],
})

// 全局前置守卫：登录校验 + 管理员校验
router.beforeEach((to) => {
  const userStore = useUserStore()

  if (to.meta.requiresAuth && !userStore.token) {
    // 未登录：跳转登录页并记录来源，登录后回跳
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (to.meta.requiresAdmin && userStore.userInfo?.role !== 'ADMIN') {
    // 非管理员：拦截回首页
    return { path: '/' }
  }
  return true
})

export default router
