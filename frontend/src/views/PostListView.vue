<template>
  <div class="forum-home">
    <!-- 搜索条（清爽版） -->
    <div class="search-section">
      <i class="fas fa-search search-icon"></i>
      <input
        v-model="searchInput"
        type="text"
        placeholder="搜索帖子... 面经 组队 二手书"
        @keyup.enter="onSearch"
      />
    </div>

    <div class="home-grid">
      <!-- 左侧：帖子广场 -->
      <div class="left-col">
        <div class="section-header">
          <h2><i class="fas fa-fire"></i> 帖子广场 · {{ sortBy === 'hot' ? '最热' : '最新' }}</h2>
          <div class="header-actions">
            <el-select v-model="sortBy" placeholder="排序" style="width: 100px" size="small">
              <el-option label="最新" value="latest" />
              <el-option label="最热" value="hot" />
            </el-select>
            <el-button v-if="authStore.isLogin" type="primary" size="small" @click="$router.push('/editor')">
              <i class="fas fa-pen-fancy" style="margin-right: 4px;"></i> 发帖
            </el-button>
          </div>
        </div>

        <el-empty v-if="!loading && filteredList.length === 0" description="暂无帖子" class="empty-tip" />

        <div class="card-list">
          <article v-for="item in filteredList" :key="item.id" class="post-card" @click="$router.push(`/post/${item.id}`)">
            <div class="post-meta">
              <span class="board-tag"><i class="far fa-file-alt"></i> {{ item.boardName }}</span>
              <span v-if="item.isFeatured" class="essence-tag"><i class="fas fa-crown"></i> 精华</span>
              <span v-if="item.isPinned" class="pin-tag"><i class="fas fa-thumbtack"></i> 置顶</span>
              <span class="post-time">{{ formatTime(item.createdAt) }}</span>
            </div>
            <div class="post-author">
              <el-avatar :size="28" :src="item.authorAvatar" class="author-avatar">
                {{ (item.authorName || '')?.slice(0, 1) }}
              </el-avatar>
              <span class="author-name">{{ item.authorName || '匿名' }}</span>
            </div>
            <h3 class="post-title">{{ item.title }}</h3>
            <div class="post-excerpt">{{ contentSummary(item.content) }}</div>
            <div class="post-tags" v-if="(item.tags || []).length">
              <span v-for="tag in (item.tags || []).slice(0, 4)" :key="tag" class="tag-pill">{{ tag }}</span>
            </div>
            <div class="post-footer" @click.stop>
              <span class="action" @click="toggleLike(item)">
                <i class="far fa-thumbs-up" :class="{ liked: item.likedByMe }"></i>
                {{ item.likeCount || 0 }} 点赞
              </span>
              <span class="action"><i class="far fa-comment"></i> {{ item.commentCount || 0 }} 评论</span>
              <span class="action" @click="toggleFavorite(item)">
                <i :class="[item.favoritedByMe ? 'fas fa-heart' : 'far fa-heart', { favorited: item.favoritedByMe }]"></i>
                {{ item.favoritedByMe ? '已收藏' : '收藏' }}
              </span>
              <span class="action" @click="sharePost(item)"><i class="fas fa-share-alt"></i> 分享</span>
              <el-button v-if="authStore.isLogin" type="danger" link size="small" class="report-btn" @click="report(item)">
                <i class="fas fa-flag"></i> 举报
              </el-button>
            </div>
          </article>
        </div>

        <el-pagination
          v-if="total > 0"
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 30]"
          layout="total, sizes, prev, pager, next"
          class="pagination"
          @current-change="loadData"
          @size-change="onSizeChange"
        />
      </div>

      <!-- 右侧栏 -->
      <div class="right-col">
        <div class="side-card">
          <div class="side-card-header">
            <i class="fas fa-bullhorn"></i>
            <span>校园公告</span>
          </div>
          <ul class="announce-list">
            <li><i class="fas fa-circle"></i> 请文明发帖，共建和谐社区</li>
            <li><i class="fas fa-circle"></i> 二手交易请当面核实</li>
            <li><i class="fas fa-circle"></i> 失物招领信息请注明时间地点</li>
            <li><i class="fas fa-circle"></i> 实习与面经欢迎真实分享</li>
          </ul>
        </div>
      </div>
    </div>

    <div class="footer-note">
      <i class="far fa-heart"></i> 年轻化校园论坛 · 清爽版
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, inject } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listPostsApi, likePostApi, favoritePostApi, reportPostApi } from '@/api/post'
import { useAuthStore } from '@/store/auth'

const authStore = useAuthStore()
const route = useRoute()
const router = useRouter()
const boardFilter = inject('boardFilter', ref(null))
const searchKeyword = inject('searchKeyword', ref(''))
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const sortBy = ref('latest')
const loading = ref(false)
const searchInput = ref('')

const filteredList = computed(() => {
  const arr = [...list.value]
  arr.sort((a, b) => {
    const pinA = a.isPinned ? 1 : 0
    const pinB = b.isPinned ? 1 : 0
    if (pinB !== pinA) return pinB - pinA
    const featA = a.isFeatured ? 1 : 0
    const featB = b.isFeatured ? 1 : 0
    if (featB !== featA) return featB - featA
    if (sortBy.value === 'hot') return (b.likeCount || 0) - (a.likeCount || 0)
    return new Date(b.updatedAt || 0) - new Date(a.updatedAt || 0)
  })
  return arr
})

function stripHtml(html) {
  return (html || '').replace(/<[^>]+>/g, '').trim()
}

function contentSummary(html) {
  const text = stripHtml(html)
  return text.length > 120 ? text.slice(0, 120) + '...' : text
}

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + ' 分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + ' 小时前'
  if (diff < 86400000 * 2) return '昨天'
  return d.toLocaleDateString()
}

function onSearch() {
  const kw = searchInput.value?.trim()
  router.push({ path: '/posts', query: kw ? { keyword: kw } : {} })
  pageNum.value = 1
  loadData()
}

function getQueryKeyword() {
  return (route.query.keyword || searchKeyword?.value || searchInput.value || '').trim() || undefined
}

async function loadData() {
  loading.value = true
  try {
    const pNum = Math.max(1, Number(pageNum.value) || 1)
    const pSize = Math.max(1, Math.min(50, Number(pageSize.value) || 10))
    const bId = boardFilter?.value ?? undefined
    const kw = getQueryKeyword()
    const params = { pageNum: pNum, pageSize: pSize }
    if (bId != null && bId !== '') params.boardId = bId
    if (kw != null && kw !== '') params.keyword = kw

    const res = await listPostsApi(params)
    // 兼容多种响应：分页 { records, total }、部分接口 { list }、或直接数组
    let nextList = []
    let nextTotal = 0
    if (Array.isArray(res)) {
      nextList = res
      nextTotal = res.length
    } else if (res && typeof res === 'object') {
      nextList = res.records ?? res.list ?? res.data ?? []
      nextTotal = Number(res.total) ?? (Array.isArray(nextList) ? nextList.length : 0)
    }
    list.value = Array.isArray(nextList) ? nextList : []
    total.value = nextTotal >= 0 ? nextTotal : list.value.length
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
    // 请求失败时不清空已有列表，避免闪一下消失
    if (list.value.length === 0) list.value = []
  } finally {
    loading.value = false
  }
}

function onSizeChange() {
  pageNum.value = 1
  loadData()
}

async function toggleLike(item) {
  if (!authStore.isLogin) {
    ElMessage.warning('请先登录')
    return
  }
  try {
    const res = await likePostApi(item.id)
    item.likedByMe = res?.liked ?? !item.likedByMe
    item.likeCount = (item.likeCount || 0) + (item.likedByMe ? 1 : -1)
    ElMessage.success(item.likedByMe ? '已点赞' : '已取消')
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

async function toggleFavorite(item) {
  if (!authStore.isLogin) {
    ElMessage.warning('请先登录')
    return
  }
  try {
    const res = await favoritePostApi(item.id)
    item.favoritedByMe = res?.favorited ?? !item.favoritedByMe
    ElMessage.success(item.favoritedByMe ? '已收藏' : '已取消收藏')
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

function sharePost(item) {
  const url = window.location.origin + '/#/post/' + item.id
  if (navigator.clipboard) {
    navigator.clipboard.writeText(url).then(() => ElMessage.success('链接已复制到剪贴板'))
  } else {
    ElMessage.info('请手动复制链接：' + url)
  }
}

async function report(item) {
  try {
    const res = await ElMessageBox.prompt('请输入举报理由', '举报帖子', {
      inputPlaceholder: '如：广告、违规内容',
      confirmButtonText: '提交'
    })
    await reportPostApi(item.id, { reason: res.value || '举报' })
    ElMessage.success('举报成功，我们会尽快处理')
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '举报失败')
  }
}

watch(() => [route.query.keyword, searchKeyword?.value], () => {
  searchInput.value = route.query.keyword || searchKeyword?.value || ''
}, { immediate: true })

watch(() => boardFilter?.value, () => {
  pageNum.value = 1
  loadData()
})

onMounted(loadData)
</script>

<style scoped>
.forum-home {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 0 30px;
  background: #f4f6f9;
  min-height: calc(100vh - 100px);
}

.search-section {
  background: #fff;
  border-radius: 50px;
  padding: 14px 24px;
  margin-bottom: 28px;
  display: flex;
  align-items: center;
  gap: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.02);
  border: 1px solid #eee;
}
.search-icon {
  color: #ffa005;
  font-size: 18px;
}
.search-section input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 15px;
  background: transparent;
}
.search-section input::placeholder {
  color: #b0b5bc;
}

.home-grid {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 28px;
  align-items: start;
}

.left-col {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 6px;
}
.section-header h2 {
  font-size: 22px;
  font-weight: 700;
  color: #1c1c1e;
  display: flex;
  align-items: center;
  gap: 8px;
}
.section-header h2 i {
  color: #ffa005;
  background: #ffeee0;
  padding: 8px;
  border-radius: 16px;
  font-size: 18px;
}
.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.card-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.post-card {
  background: #fff;
  border-radius: 28px;
  padding: 22px 24px;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.02);
  border: 1px solid #f0f2f5;
  transition: all 0.2s;
  cursor: pointer;
}
.post-card:hover {
  border-color: #ffd7a0;
  box-shadow: 0 12px 24px rgba(255, 160, 5, 0.06);
}

.post-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 13px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}
.board-tag {
  background: #f0f2f5;
  padding: 5px 14px;
  border-radius: 40px;
  font-weight: 500;
  color: #4e4e57;
}
.board-tag i {
  margin-right: 4px;
  color: #ffa005;
}
.essence-tag {
  background: #fef1e0;
  color: #b65600;
  padding: 5px 14px;
  border-radius: 40px;
  font-weight: 600;
}
.essence-tag i {
  margin-right: 4px;
}
.pin-tag {
  background: #fff7e6;
  color: #d46b08;
  padding: 5px 14px;
  border-radius: 40px;
  font-weight: 600;
}
.pin-tag i {
  margin-right: 4px;
}
.post-time {
  margin-left: auto;
  color: #999;
}

.post-author {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}
.author-avatar {
  flex-shrink: 0;
  background: linear-gradient(145deg, #ffb96a, #ff8800);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
}
.author-name {
  font-size: 14px;
  font-weight: 500;
  color: #4e4e57;
}

.post-title {
  font-size: 18px;
  font-weight: 700;
  margin: 0 0 8px 0;
  color: #121212;
  line-height: 1.4;
}
.post-title:hover {
  color: #ffa005;
}

.post-excerpt {
  color: #5f6368;
  font-size: 14px;
  line-height: 1.5;
  margin-bottom: 14px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-tags {
  margin-bottom: 14px;
}
.tag-pill {
  display: inline-block;
  background: #f0f2f5;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  color: #4e4e57;
  margin-right: 8px;
  margin-bottom: 4px;
}

.post-footer {
  display: flex;
  align-items: center;
  gap: 28px;
  color: #8e9399;
  font-size: 14px;
  border-top: 1px solid #f0f2f5;
  padding-top: 16px;
}
.post-footer .action {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  transition: color 0.15s;
}
.post-footer .action i {
  color: #ffa005;
}
.post-footer .action:hover {
  color: #ffa005;
}
.post-footer .action.liked i,
.post-footer .action.favorited i {
  color: #ffa005;
}
.report-btn {
  margin-left: auto;
  color: #999 !important;
}
.report-btn:hover {
  color: #ff4d4f !important;
}

.pagination {
  margin-top: 32px;
  margin-bottom: 20px;
  justify-content: center;
}
.pagination :deep(.el-pager li.is-active) {
  background: #ffa005;
  border-color: #ffedd5;
  border-radius: 40px;
}
.pagination :deep(.btn-prev),
.pagination :deep(.btn-next),
.pagination :deep(.el-pager li) {
  border-radius: 40px;
}

/* 右侧栏 */
.right-col {
  display: flex;
  flex-direction: column;
  gap: 24px;
}
.side-card {
  background: #fff;
  border-radius: 28px;
  padding: 24px 22px;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.02);
  border: 1px solid #f0f2f5;
}
.side-card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 18px;
  font-weight: 700;
  font-size: 18px;
  color: #1c1c1e;
}
.side-card-header i {
  color: #ffa005;
  background: #ffeee0;
  padding: 8px;
  border-radius: 14px;
  font-size: 16px;
}

.announce-list {
  list-style: none;
  padding: 0;
  margin: 0;
}
.announce-list li {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 0;
  border-bottom: 1px solid #f0f2f5;
  font-size: 14px;
  color: #3c4043;
}
.announce-list li:last-child {
  border-bottom: none;
}
.announce-list li i {
  color: #ffa005;
  font-size: 6px;
}

.footer-note {
  text-align: center;
  font-size: 14px;
  color: #b0b5bc;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #eaeef2;
}
.footer-note i {
  color: #ffa005;
  margin-right: 4px;
}

.empty-tip {
  padding: 48px 0;
}

@media (max-width: 900px) {
  .home-grid {
    grid-template-columns: 1fr;
  }
  .right-col {
    order: -1;
    flex-direction: row;
    flex-wrap: wrap;
    gap: 16px;
  }
  .side-card {
    min-width: 280px;
    flex: 1;
  }
}
</style>
