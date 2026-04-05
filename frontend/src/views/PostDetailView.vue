<template>
  <div class="post-detail-page" v-loading="loading">
    <article v-if="post" class="detail-card">
      <div class="card-head">
        <el-avatar :size="40" class="avatar" :src="post.authorAvatar">{{ (post.authorName || '')?.slice(0, 1) }}</el-avatar>
        <div class="author-info">
          <span class="author-name">{{ post.authorName }}</span>
          <span class="meta">{{ post.boardName }} · {{ formatTime(post.createdAt) }}</span>
        </div>
        <el-tag v-if="post.isPinned" type="warning" size="small">置顶</el-tag>
        <el-tag v-if="post.isFeatured" type="success" size="small">精华</el-tag>
      </div>
      <h1 class="detail-title">{{ post.title }}</h1>
      <div class="detail-content" v-html="post.content"></div>
      <div v-if="(post.attachments || []).length" class="detail-attachments">
        <h4 class="attach-title"><i class="fas fa-paperclip"></i> 附件</h4>
        <div class="attach-list">
          <template v-for="(att, idx) in (post.attachments || [])" :key="att.fileUrl || idx">
            <img
              v-if="isImageAttachment(att)"
              :src="att.fileUrl"
              :alt="att.fileName || '图片附件'"
              class="attach-image"
            />
            <a v-else :href="att.fileUrl" target="_blank" rel="noopener" class="attach-file">
              <i class="far fa-file"></i> {{ att.fileName || att.fileUrl }}
            </a>
          </template>
        </div>
      </div>
      <div class="detail-tags">
        <el-tag v-for="tag in (post.tags || [])" :key="tag" size="small" type="info">{{ tag }}</el-tag>
      </div>
      <div class="detail-actions">
        <span class="action" :class="{ liked: post.likedByMe }" @click="toggleLike">
          <el-icon><StarFilled v-if="post.likedByMe" /><Star v-else /></el-icon>
          {{ post.likeCount || 0 }} 点赞
        </span>
        <span class="action" @click="toggleFavorite">
          <el-icon><Collection v-if="post.favoritedByMe" /><CollectionTag v-else /></el-icon>
          {{ post.favoritedByMe ? '已收藏' : '收藏' }}
        </span>
        <span class="action" @click="sharePost"><el-icon><Share /></el-icon> 分享</span>
        <el-button v-if="authStore.isLogin" type="danger" link @click="report">举报</el-button>
      </div>
    </article>

    <section v-if="post" class="comment-section">
      <h3>评论 {{ comments.length }}</h3>
      <div v-if="authStore.isLogin" class="comment-form">
        <div v-if="replyParentId" class="replying-tip">
          <span>正在回复：{{ replyingToName || '该评论' }}</span>
          <el-button type="primary" link size="small" @click="cancelReply">取消回复</el-button>
        </div>
        <el-input
          ref="commentInputRef"
          v-model="commentContent"
          type="textarea"
          :rows="3"
          :placeholder="replyParentId ? `回复 ${replyingToName || '该评论'}...` : '写下你的评论...'"
        />
        <el-button type="primary" @click="submitComment">发表评论</el-button>
      </div>
      <div v-else class="login-tip">登录后参与评论</div>
      <div class="comment-list">
        <div v-for="c in comments" :key="c.id" class="comment-item">
          <el-avatar :size="32">{{ (c.userName || '')?.slice(0, 1) }}</el-avatar>
          <div class="comment-body">
            <span class="comment-user">{{ c.userName }}</span>
            <span class="comment-time">{{ formatTime(c.createdAt) }}</span>
            <p class="comment-text">{{ c.content }}</p>
            <el-button v-if="authStore.isLogin" type="primary" link size="small" @click="replyTo(c)">回复</el-button>
            <div v-for="r in (c.replies || [])" :key="r.id" class="comment-reply">
              <span class="comment-user">{{ r.userName }}</span>
              <span class="comment-time">{{ formatTime(r.createdAt) }}</span>
              <p class="comment-text">{{ r.content }}</p>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Star, StarFilled, Collection, CollectionTag, Share } from '@element-plus/icons-vue'
import { getPostDetailApi, likePostApi, favoritePostApi, reportPostApi } from '@/api/post'
import { listCommentsApi, createCommentApi } from '@/api/comment'
import { useAuthStore } from '@/store/auth'

const authStore = useAuthStore()
const route = useRoute()
const post = ref(null)
const comments = ref([])
const commentContent = ref('')
const replyParentId = ref(null)
const replyingToName = ref('')
const commentInputRef = ref(null)
const loading = ref(true)

const postId = computed(() => Number(route.params.id))

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + ' 分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + ' 小时前'
  return d.toLocaleDateString()
}

function isImageAttachment(att) {
  const source = String(att?.fileName || att?.fileUrl || '').toLowerCase()
  return /\.(png|jpe?g|gif|webp|bmp|svg)$/.test(source)
}

async function loadPost() {
  try {
    post.value = await getPostDetailApi(postId.value)
  } catch (e) {
    ElMessage.error(e.message || '帖子不存在')
  }
}

async function loadComments() {
  try {
    comments.value = await listCommentsApi(postId.value)
  } catch (_) {}
}

async function toggleLike() {
  if (!authStore.isLogin) { ElMessage.warning('请先登录'); return }
  try {
    const res = await likePostApi(postId.value)
    post.value.likedByMe = res?.liked ?? !post.value.likedByMe
    post.value.likeCount = (post.value.likeCount || 0) + (post.value.likedByMe ? 1 : -1)
    ElMessage.success(post.value.likedByMe ? '已点赞' : '已取消')
  } catch (e) { ElMessage.error(e.message || '操作失败') }
}

async function toggleFavorite() {
  if (!authStore.isLogin) { ElMessage.warning('请先登录'); return }
  try {
    const res = await favoritePostApi(postId.value)
    post.value.favoritedByMe = res?.favorited ?? !post.value.favoritedByMe
    ElMessage.success(post.value.favoritedByMe ? '已收藏' : '已取消收藏')
  } catch (e) { ElMessage.error(e.message || '操作失败') }
}

function sharePost() {
  const url = window.location.origin + '/#/post/' + postId.value
  if (navigator.clipboard) navigator.clipboard.writeText(url).then(() => ElMessage.success('链接已复制'))
  else ElMessage.info('请手动复制：' + url)
}

async function report() {
  try {
    const res = await ElMessageBox.prompt('请输入举报理由', '举报')
    await reportPostApi(postId.value, { reason: res.value || '举报' })
    ElMessage.success('举报成功')
  } catch (e) { if (e !== 'cancel') ElMessage.error(e.message || '举报失败') }
}

function replyTo(c) {
  replyParentId.value = c.id
  commentContent.value = ''
  replyingToName.value = c.userName || ''
  nextTick(() => {
    try {
      commentInputRef.value?.focus?.()
    } catch (_) {}
  })
}

function cancelReply() {
  replyParentId.value = null
  replyingToName.value = ''
}

async function submitComment() {
  if (!commentContent.value?.trim()) { ElMessage.warning('请输入评论'); return }
  try {
    await createCommentApi({
      postId: postId.value,
      parentId: replyParentId.value || null,
      content: commentContent.value.trim()
    })
    ElMessage.success('评论成功')
    commentContent.value = ''
    cancelReply()
    loadComments()
    if (post.value) post.value.commentCount = (post.value.commentCount || 0) + 1
  } catch (e) { ElMessage.error(e.message || '评论失败') }
}

onMounted(async () => {
  await loadPost()
  await loadComments()
  loading.value = false
})
</script>

<style scoped>
.post-detail-page { max-width: 800px; margin: 0 auto; }
.detail-card {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
  margin-bottom: 20px;
}
.card-head { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; }
.author-info { flex: 1; }
.author-name { font-weight: 600; color: #333; display: block; }
.meta { font-size: 13px; color: #999; }
.detail-title { margin: 0 0 16px 0; font-size: 22px; color: #333; }
.detail-content { line-height: 1.7; color: #333; margin-bottom: 16px; }
.detail-attachments {
  margin-bottom: 16px;
}
.attach-title {
  margin: 0 0 10px 0;
  font-size: 14px;
  color: #666;
  display: flex;
  align-items: center;
  gap: 6px;
}
.attach-title i {
  color: #ffa005;
}
.attach-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.attach-image {
  width: 100%;
  max-width: 520px;
  border-radius: 12px;
  border: 1px solid #eee;
  object-fit: contain;
  background: #fff;
}
.attach-file {
  color: #1890ff;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.attach-file:hover {
  text-decoration: underline;
}
.detail-tags { margin-bottom: 16px; }
.detail-actions { display: flex; align-items: center; gap: 20px; padding-top: 16px; border-top: 1px solid #eee; }
.detail-actions .action { display: inline-flex; align-items: center; gap: 6px; cursor: pointer; color: #666; }
.detail-actions .action:hover { color: #1890ff; }
.detail-actions .liked { color: #1890ff; }
.comment-section { background: #fff; border-radius: 8px; padding: 20px; box-shadow: 0 1px 4px rgba(0,0,0,0.08); }
.comment-section h3 { margin: 0 0 16px 0; font-size: 16px; }
.comment-form { margin-bottom: 20px; }
.replying-tip {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 2px;
  color: #666;
  font-size: 13px;
}
.comment-form .el-button { margin-top: 8px; }
.comment-list { display: flex; flex-direction: column; gap: 16px; }
.comment-item { display: flex; gap: 12px; }
.comment-body { flex: 1; }
.comment-user { font-weight: 500; margin-right: 8px; }
.comment-time { font-size: 12px; color: #999; }
.comment-text { margin: 4px 0 0 0; color: #333; }
.comment-reply { margin-top: 12px; padding-left: 12px; border-left: 2px solid #eee; }
.login-tip { color: #999; margin-bottom: 16px; }
</style>
