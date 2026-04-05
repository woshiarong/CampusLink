import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/store/auth'

const routes = [
  {
    path: '/',
    component: () => import('@/views/PostListView.vue')
  },
  {
    path: '/login',
    component: () => import('@/views/LoginView.vue'),
    meta: { guestOnly: true }
  },
  {
    path: '/register',
    component: () => import('@/views/RegisterView.vue'),
    meta: { guestOnly: true }
  },
  {
    path: '/posts',
    component: () => import('@/views/PostListView.vue')
  },
  {
    path: '/post/:id',
    component: () => import('@/views/PostDetailView.vue')
  },
  {
    path: '/profile',
    component: () => import('@/views/ProfileView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/center',
    component: () => import('@/views/CenterView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/messages',
    component: () => import('@/views/MessagesView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/editor',
    component: () => import('@/views/PostEditorView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/moderation',
    component: () => import('@/views/ModerationView.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/settings',
    component: () => import('@/views/SettingsView.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/dashboard',
    component: () => import('@/views/admin/DashboardView.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/users',
    component: () => import('@/views/admin/UserManageView.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/boards',
    component: () => import('@/views/admin/BoardManageView.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/posts',
    component: () => import('@/views/admin/PostManageView.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/logs',
    component: () => import('@/views/admin/AdminLogView.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to) => {
  const authStore = useAuthStore()

  if (authStore.token && !authStore.user) {
    await authStore.fetchProfile()
  }

  if (to.meta.guestOnly && authStore.isLogin) {
    return authStore.isAdmin ? '/dashboard' : '/posts'
  }

  if (to.meta.requiresAuth && !authStore.isLogin) {
    return '/login'
  }

  if (to.meta.requiresAdmin && !authStore.isAdmin) {
    return '/posts'
  }

  if (to.path === '/') {
    return authStore.isAdmin ? '/dashboard' : '/posts'
  }

  return true
})

export default router
