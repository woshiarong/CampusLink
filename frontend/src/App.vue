<template>
  <el-config-provider :locale="zhCn">
    <el-container class="app-container" :class="{ 'dark-mode': darkMode }">
      <el-container>
        <el-aside :width="asideWidth" class="app-aside">
          <div class="aside-scroll">
            <el-menu :default-active="activeMenu" active-text-color="#ffa005" router class="nav-menu">
              <el-menu-item v-if="!authStore.isAdmin" index="/posts">帖子广场</el-menu-item>
              <el-menu-item v-if="authStore.isLogin && !authStore.isAdmin" index="/editor">发布帖子</el-menu-item>
              <el-menu-item v-if="authStore.isLogin && !authStore.isAdmin" index="/center">个人中心</el-menu-item>
              <el-menu-item v-if="authStore.isLogin && !authStore.isAdmin" index="/profile">个人资料</el-menu-item>
              <template v-if="authStore.isAdmin">
                <el-menu-item index="/dashboard">仪表盘</el-menu-item>
                <el-menu-item index="/admin/users">用户管理</el-menu-item>
                <el-menu-item index="/admin/posts">帖子管理</el-menu-item>
                <el-menu-item index="/admin/boards">版块管理</el-menu-item>
                <el-menu-item index="/moderation">内容审核</el-menu-item>
                <el-menu-item index="/settings">系统设置</el-menu-item>
                <el-menu-item index="/admin/logs">操作日志</el-menu-item>
              </template>
            </el-menu>
            <div v-if="!authStore.isAdmin && boards.length" class="board-list">
              <div class="board-title">版块</div>
              <div
                v-for="b in boards"
                :key="b.id"
                class="board-item"
                :class="{ active: boardFilter === b.id }"
                @click="setBoardFilter(b.id)"
              >
                {{ b.name }}
              </div>
            </div>
          </div>
          <div class="aside-footer">
            <template v-if="authStore.isLogin">
              <div class="aside-user-block">
                <el-avatar :size="40" :src="authStore.user?.avatar" class="aside-avatar">
                  {{ (authStore.user?.nickname || authStore.user?.username || '')?.slice(0, 1) }}
                </el-avatar>
                <span class="aside-user">{{ authStore.user?.nickname || authStore.user?.username }}</span>
              </div>
              <div class="aside-actions">
                <a href="javascript:;" class="aside-link" @click="logout"><i class="fas fa-sign-out-alt"></i> 退出</a>
              </div>
            </template>
            <template v-else>
              <div class="aside-guest">
                <router-link to="/login" class="aside-link">登录</router-link>
                <span class="aside-sep">|</span>
                <router-link to="/register" class="aside-link aside-register">注册</router-link>
              </div>
            </template>
          </div>
        </el-aside>

        <el-main class="app-main">
          <router-view :key="route.fullPath" />
        </el-main>
      </el-container>
    </el-container>
  </el-config-provider>
</template>

<script setup>
import { ref, computed, onMounted, provide } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { ElMessage } from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import { listBoardsApi } from '@/api/board'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const globalKeyword = ref('')
const userSearchKeyword = ref('')
const darkMode = ref(localStorage.getItem('campus-forum-dark') === '1')
const boards = ref([])
const boardFilter = ref(null)

const asideWidth = computed(() => (authStore.isAdmin ? '220px' : '200px'))
const activeMenu = computed(() => (route.path.startsWith('/post/') ? '/posts' : route.path))

provide('boardFilter', boardFilter)
provide('searchKeyword', userSearchKeyword)

onMounted(async () => {
  if (!authStore.isAdmin) {
    try { boards.value = await listBoardsApi() } catch (_) {}
  }
})

function setBoardFilter(id) {
  boardFilter.value = boardFilter.value === id ? null : id
}

function onUserSearch() {
  const kw = userSearchKeyword.value?.trim()
  router.push({ path: '/posts', query: kw ? { keyword: kw } : {} })
}

function onGlobalSearch() {
  const kw = globalKeyword.value?.trim()
  if (!kw) return
  if (route.path.startsWith('/admin/posts')) router.push({ path: '/admin/posts', query: { keyword: kw } })
  else if (route.path.startsWith('/admin/users')) router.push({ path: '/admin/users', query: { keyword: kw } })
  else router.push({ path: '/admin/posts', query: { keyword: kw } })
}

function handleUserCommand(cmd) {
  if (cmd === 'logout') logout()
  else if (cmd === 'center') router.push('/center')
  else if (cmd === 'profile') router.push('/profile')
  else if (cmd === 'dark') {
    darkMode.value = !darkMode.value
    localStorage.setItem('campus-forum-dark', darkMode.value ? '1' : '0')
    document.documentElement.classList.toggle('dark-mode', darkMode.value)
  }
}

const logout = async () => {
  await authStore.logout()
  ElMessage.success('已退出登录')
  router.push('/login')
}

if (darkMode.value) document.documentElement.classList.add('dark-mode')
</script>

<style scoped>
.app-container {
  height: 100vh;
  overflow: hidden;
  background: #f4f6f9;
}

.app-aside {
  display: flex;
  flex-direction: column;
  height: 100vh;
  max-height: 100vh;
  overflow: hidden;
  background: #fff;
  border-right: 1px solid #f0f2f5;
  box-shadow: 0 2px 8px rgba(0,0,0,0.02);
}
.aside-scroll {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
}

.app-aside .el-menu {
  border-right: none;
  padding: 12px 8px;
}
.app-aside :deep(.el-menu-item.is-active) {
  background: #fff7e6 !important;
  color: #ffa005;
  border-radius: 16px;
}
.app-aside :deep(.el-menu-item:hover) {
  background: #f8f9fc;
  border-radius: 16px;
  color: #ffa005;
}

.app-main {
  padding: 20px;
  background: #f4f6f9;
  height: 100vh;
  overflow-y: auto;
}

.board-list { padding: 14px 12px; border-top: 1px solid #f0f2f5; }
.board-title { font-size: 12px; color: #8e9399; margin-bottom: 10px; font-weight: 600; }
.board-item { font-size: 13px; padding: 8px 12px; cursor: pointer; color: #4e4e57; border-radius: 20px; transition: 0.15s; }
.board-item:hover { color: #ffa005; background: #fff7e6; }
.board-item.active { color: #ffa005; font-weight: 600; background: #fff7e6; }

.aside-footer {
  flex-shrink: 0;
  padding: 14px 12px;
  border-top: 1px solid #f0f2f5;
  font-size: 13px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.aside-user-block {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}
.aside-avatar {
  flex-shrink: 0;
  background: linear-gradient(145deg, #ffb96a, #ff8800);
  color: #fff;
  font-weight: 600;
}
.aside-user {
  color: #4e4e57;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.aside-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.aside-link {
  color: #ffa005;
  text-decoration: none;
}
.aside-link:hover { text-decoration: underline; }
.aside-link i { margin-right: 4px; }
.aside-guest { display: flex; align-items: center; gap: 8px; }
.aside-sep { color: #ccc; }
.aside-register { font-weight: 600; }

.dark-mode .app-container { background: #1a1a1a; }
.dark-mode .app-aside { background: #262626; border-color: #434343; }
.dark-mode .app-main { background: #1a1a1a; }
.dark-mode .aside-footer { border-color: #434343; }
.dark-mode .aside-user { color: #e0e0e0; }
.dark-mode .aside-link { color: #ffb96a; }
</style>
