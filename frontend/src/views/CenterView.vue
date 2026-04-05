<template>
  <div class="center-page">
    <el-card class="center-card" shadow="hover">
      <template #header>个人中心</template>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="我的发布" name="posts">
          <!-- 我的发布：闲鱼风格 -->
          <div class="my-posts-section">
            <div class="filter-tabs">
              <span
                v-for="opt in statusOptions"
                :key="opt.value"
                class="filter-tab"
                :class="{ active: currentStatus === opt.value }"
                @click="changeStatus(opt.value)"
              >
                <i :class="opt.icon"></i> {{ opt.label }}
              </span>
            </div>

            <div v-loading="loadingPosts" class="post-list">
              <div v-for="row in myPosts" :key="row.id" class="post-item">
                <div class="post-info">
                  <div class="id">
                    <span>#{{ row.id }}</span>
                    <span class="board">{{ row.boardName || '版块' }}</span>
                  </div>
                  <div class="title">{{ row.title }}</div>
                  <div class="status-time">
                    <span class="status-badge" :class="statusClass(row.status)">{{ statusText(row.status) }}</span>
                    <span><i class="far fa-calendar-alt"></i> {{ formatDateTime(row.createdAt) }}</span>
                  </div>
                </div>
                <div class="post-actions">
                  <template v-if="row.status === 'DRAFTING' || row.status === 'UPDATED'">
                    <span class="action-btn primary" @click="submitPost(row)"><i class="fas fa-paper-plane"></i> 提交</span>
                  </template>
                  <template v-if="row.status === 'VIEWABLE' || row.status === 'INTERACTED' || row.status === 'REPUBLISHED'">
                    <span class="action-btn primary" @click="openUpdate(row)"><i class="far fa-edit"></i> 编辑</span>
                  </template>
                  <span class="action-btn link" @click="$router.push(`/post/${row.id}`)"><i class="fas fa-external-link-alt"></i> 查看</span>
                  <span class="action-btn danger" @click="deletePost(row)"><i class="far fa-trash-alt"></i> 删除</span>
                </div>
              </div>
            </div>

            <div v-if="!loadingPosts && !myPosts.length" class="empty-tip">
              <i class="fas fa-inbox"></i>
              <span>暂无帖子，去 <router-link to="/editor">发布帖子</router-link> 写一篇吧</span>
            </div>

            <div class="tip-box">
              <i class="fas fa-info-circle"></i>
              <span>草稿不会公开；提交后进入待审核，管理员审核通过后自动转为已发布。</span>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="我的收藏" name="favorites">
          <div class="favorites-section">
            <div class="section-title">
              <i class="fas fa-heart"></i>
              <span>我的收藏 · 共 {{ favorites.length }} 篇</span>
            </div>
            <div v-loading="loadingFav" class="favorite-list">
              <article
                v-for="row in favorites"
                :key="row.id"
                class="favorite-card"
                @click="$router.push(`/post/${row.id}`)"
              >
                <div class="fav-meta">
                  <span class="board-tag"><i class="far fa-file-alt"></i> {{ row.boardName || '版块' }}</span>
                  <span class="author-tag"><i class="far fa-user"></i> {{ row.authorName || '-' }}</span>
                </div>
                <h3 class="fav-title">{{ row.title }}</h3>
                <div class="fav-actions" @click.stop>
                  <span class="action-btn link" @click="$router.push(`/post/${row.id}`)">
                    <i class="fas fa-external-link-alt"></i> 查看
                  </span>
                </div>
              </article>
            </div>
            <div v-if="!loadingFav && !favorites.length" class="empty-tip">
              <i class="fas fa-heart-broken"></i>
              <span>暂无收藏，去 <router-link to="/posts">帖子广场</router-link> 发现好帖吧</span>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 更新帖子弹窗 -->
    <el-dialog v-model="dialogVisible" title="更新帖子" width="640px">
      <el-form :model="updateForm" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="updateForm.title" placeholder="标题" />
        </el-form-item>
        <el-form-item label="正文">
          <el-input v-model="updateForm.content" type="textarea" :rows="6" placeholder="正文" />
        </el-form-item>
        <el-form-item label="标签">
          <el-select v-model="updateForm.tags" multiple allow-create filterable default-first-option style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="doUpdate">确认更新</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listMyPostsApi, submitPostApi, updatePostApi, deleteMyPostApi } from '@/api/post'
import { listFavoritesApi } from '@/api/favorite'

const activeTab = ref('posts')
const myPosts = ref([])
const favorites = ref([])
const loadingPosts = ref(false)
const loadingFav = ref(false)
const dialogVisible = ref(false)
const editingPostId = ref(null)

const statusOptions = [
  { label: '全部', value: '', icon: 'fas fa-layer-group' },
  { label: '草稿', value: 'DRAFTING', icon: 'fas fa-pencil-alt' },
  { label: '待审核', value: 'UNDER_REVIEW', icon: 'fas fa-clock' },
  { label: '已发布', value: 'PUBLISHED', icon: 'fas fa-check-circle' }
]
const currentStatus = ref('')

const updateForm = reactive({
  title: '',
  content: '',
  tags: [],
  attachments: []
})

function formatDateTime(val) {
  if (!val) return ''
  return String(val).replace('T', ' ').slice(0, 16)
}

function statusText(status) {
  if (status === 'DRAFTING') return '草稿'
  if (status === 'UNDER_REVIEW') return '待审核'
  return '已发布'
}

function statusClass(status) {
  if (status === 'DRAFTING') return 'draft'
  if (status === 'UNDER_REVIEW') return 'pending'
  return ''
}

function changeStatus(value) {
  currentStatus.value = value
  loadPosts()
}

async function loadPosts() {
  loadingPosts.value = true
  try {
    const params = currentStatus.value ? { status: currentStatus.value } : undefined
    myPosts.value = await listMyPostsApi(params)
  } catch (e) {
    console.error('加载我的发布失败', e)
  } finally {
    loadingPosts.value = false
  }
}

async function submitPost(row) {
  try {
    await submitPostApi(row.id)
    ElMessage.success('提交成功，等待管理员审核')
    loadPosts()
  } catch (e) {
    ElMessage.error(e.message || '提交失败')
  }
}

function openUpdate(row) {
  editingPostId.value = row.id
  updateForm.title = row.title
  updateForm.content = row.content || ''
  updateForm.tags = row.tags || []
  updateForm.attachments = row.attachments || []
  dialogVisible.value = true
}

async function doUpdate() {
  try {
    await updatePostApi(editingPostId.value, updateForm)
    ElMessage.success('更新成功')
    dialogVisible.value = false
    loadPosts()
  } catch (e) {
    ElMessage.error(e.message || '更新失败')
  }
}

async function deletePost(row) {
  try {
    await ElMessageBox.confirm('确定删除该帖子？删除后不可恢复。', '提示', { type: 'warning' })
    await deleteMyPostApi(row.id)
    ElMessage.success('删除成功')
    loadPosts()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '删除失败')
  }
}

async function loadFavorites() {
  loadingFav.value = true
  try {
    favorites.value = await listFavoritesApi()
  } catch (_) {}
  finally { loadingFav.value = false }
}

watch(activeTab, (name) => {
  if (name === 'favorites') loadFavorites()
})

onMounted(() => {
  loadPosts()
})
</script>

<style scoped>
.center-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 0 16px 24px;
  background: #f4f6f9;
  min-height: calc(100vh - 100px);
}

.center-card {
  border-radius: 24px;
  border: 1px solid #ffedd5;
  overflow: hidden;
}

.my-posts-section {
  padding: 8px 0;
}

.filter-tabs {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.filter-tab {
  background: #f2f4f8;
  padding: 10px 22px;
  border-radius: 50px;
  font-weight: 500;
  color: #444;
  font-size: 14px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  transition: 0.15s;
}

.filter-tab:hover {
  background: #ffe8cc;
  color: #b65600;
}

.filter-tab.active {
  background: #ffa005;
  color: #fff;
  box-shadow: 0 6px 14px rgba(255, 193, 7, 0.45);
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
  min-height: 80px;
}

.post-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fafbfe;
  padding: 20px 24px;
  border-radius: 40px;
  border: 1px solid #f0f2f5;
  transition: 0.15s;
}

.post-item:hover {
  border-color: #ffd7a0;
  background: #fff;
}

.post-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
  flex: 1;
}

.post-info .id {
  font-size: 13px;
  color: #999;
  display: flex;
  align-items: center;
  gap: 10px;
}

.post-info .id .board {
  background: #eaeef2;
  padding: 4px 12px;
  border-radius: 40px;
  color: #4e4e57;
  font-weight: 500;
}

.post-info .title {
  font-weight: 700;
  font-size: 17px;
  color: #1f1f1f;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.status-time {
  display: flex;
  align-items: center;
  gap: 14px;
  font-size: 13px;
  color: #888;
}

.status-time i {
  margin-right: 4px;
}

.status-badge {
  background: #e6f7e6;
  color: #2e7d32;
  padding: 6px 16px;
  border-radius: 40px;
  font-weight: 500;
}

.status-badge.draft {
  background: #f1f3f4;
  color: #5f6368;
}

.status-badge.pending {
  background: #fff2d9;
  color: #b45b0a;
}

.post-actions {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
}

.action-btn {
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 40px;
  padding: 10px 20px;
  font-size: 13px;
  font-weight: 500;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  transition: 0.15s;
  color: #ffa005;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.action-btn.primary:hover,
.action-btn.link:hover {
  border-color: #ffa005;
  background: #fff7e6;
}

.action-btn.link {
  color: #666;
}
.action-btn.danger {
  color: #d9534f;
}
.action-btn.danger:hover {
  border-color: #f5b7b5;
  background: #fff1f0;
}

.empty-tip {
  text-align: center;
  padding: 32px 16px;
  color: #999;
  font-size: 15px;
}

.empty-tip i {
  display: block;
  font-size: 36px;
  margin-bottom: 12px;
  color: #ddd;
}

.empty-tip a {
  color: #ffa005;
  text-decoration: none;
}

.empty-tip a:hover {
  text-decoration: underline;
}

.tip-box {
  margin-top: 24px;
  background: #fff7e5;
  border-radius: 60px;
  padding: 16px 24px;
  color: #b65600;
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
}

.tip-box i {
  font-size: 20px;
  flex-shrink: 0;
}

/* 我的收藏 - 年轻化卡片 */
.favorites-section {
  padding: 8px 0;
}

.favorites-section .section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
  font-size: 16px;
  font-weight: 600;
  color: #1f1f1f;
}

.favorites-section .section-title i {
  color: #ffa005;
  font-size: 18px;
}

.favorite-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
  min-height: 80px;
}

.favorite-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
  background: #fafbfe;
  padding: 20px 24px;
  border-radius: 28px;
  border: 1px solid #f0f2f5;
  cursor: pointer;
  transition: 0.15s;
}

.favorite-card:hover {
  border-color: #ffd7a0;
  background: #fff;
  box-shadow: 0 4px 16px rgba(255, 160, 5, 0.08);
}

.fav-meta {
  display: flex;
  align-items: center;
  gap: 14px;
  font-size: 13px;
  color: #888;
}

.board-tag,
.author-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.board-tag {
  background: #eaeef2;
  padding: 4px 12px;
  border-radius: 40px;
  color: #4e4e57;
  font-weight: 500;
}

.fav-title {
  flex: 1;
  min-width: 0;
  margin: 0;
  font-size: 17px;
  font-weight: 700;
  color: #1f1f1f;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.fav-actions {
  flex-shrink: 0;
}

@media (max-width: 700px) {
  .post-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 14px;
  }
  .post-actions {
    align-self: flex-end;
  }
  .favorite-card {
    flex-direction: column;
    align-items: flex-start;
  }
  .fav-actions {
    align-self: flex-end;
  }
}
</style>
