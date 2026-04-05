<template>
  <div class="admin-page moderation-page">
    <div class="admin-card section-card">
      <div class="admin-card-header">
        <h2 class="admin-title"><i class="fas fa-clock"></i> 待审核帖子</h2>
        <span class="count-tip">共 {{ pendingPosts.length }} 条，与仪表盘数量一致</span>
      </div>
      <el-table :data="pendingPosts" border stripe v-loading="loadingPosts" class="admin-table">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <el-link type="primary" @click="openPostDetail(row)" :underline="false">{{ row.title }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="authorName" label="作者" width="100" />
        <el-table-column prop="boardName" label="版块" width="100" />
        <el-table-column prop="createdAt" label="提交时间" width="160">
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="success" link @click="approvePost(row)">审核通过</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="!loadingPosts && pendingPosts.length === 0" class="table-empty">暂无待审核帖子</div>
    </div>

    <div class="admin-card section-card">
      <div class="admin-card-header">
        <h2 class="admin-title"><i class="fas fa-flag"></i> 举报管理</h2>
        <el-radio-group v-model="statusFilter" @change="load" class="filter-tabs">
          <el-radio-button label="">全部</el-radio-button>
          <el-radio-button value="IN_REVIEW">待处理</el-radio-button>
          <el-radio-button value="LOG_RECORDED">已处理</el-radio-button>
        </el-radio-group>
      </div>
      <el-table :data="reports" border stripe v-loading="loading" class="admin-table">
      <el-table-column prop="id" label="举报ID" width="80" />
      <el-table-column prop="postId" label="帖子ID" width="80" />
      <el-table-column prop="postTitle" label="帖子标题" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">
          <el-link type="primary" @click="openDetail(row)" :underline="false">{{ row.postTitle || '-' }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="postContentSummary" label="内容摘要" min-width="200" show-overflow-tooltip />
      <el-table-column prop="reporterName" label="举报人" width="90" />
      <el-table-column prop="reason" label="举报理由" min-width="160" show-overflow-tooltip />
      <el-table-column prop="status" label="处理状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'IN_REVIEW' ? 'warning' : 'success'">
            {{ row.status === 'IN_REVIEW' ? '待处理' : '已处理' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="举报时间" width="160">
        <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column v-if="statusFilter !== 'LOG_RECORDED'" label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button type="danger" link @click="review(row, 'HIDE_POST')">隐藏帖子</el-button>
          <el-button type="success" link @click="review(row, 'REJECT_REPORT')">驳回举报</el-button>
          <el-button type="info" link @click="review(row, 'IGNORE_REPORT')">忽略举报</el-button>
        </template>
      </el-table-column>
    </el-table>
    </div>

    <el-dialog v-model="detailVisible" title="举报详情" width="640px">
      <div v-if="currentReport" class="report-detail">
        <p><strong>帖子标题：</strong>{{ currentReport.postTitle }}</p>
        <p><strong>举报理由：</strong>{{ currentReport.reason }}</p>
        <p><strong>举报人：</strong>{{ currentReport.reporterName }} &nbsp; <strong>举报时间：</strong>{{ formatDateTime(currentReport.createdAt) }}</p>
        <div class="content-preview">{{ currentReport.postContentSummary || '-' }}</div>
        <p v-if="currentReport.reviewRemark" class="remark"><strong>审核备注：</strong>{{ currentReport.reviewRemark }}</p>
      </div>
    </el-dialog>

    <el-dialog v-model="postDetailVisible" title="帖子详情" width="640px">
      <div v-if="currentPost" class="report-detail">
        <p><strong>标题：</strong>{{ currentPost.title }}</p>
        <p><strong>作者：</strong>{{ currentPost.authorName }} &nbsp; <strong>版块：</strong>{{ currentPost.boardName }}</p>
        <div class="content-preview" v-html="currentPost.content"></div>
        <div v-if="(currentPost.attachments || []).length" class="attachments-preview">
          <h4 class="attachments-title"><i class="fas fa-paperclip"></i> 附件</h4>
          <div class="attachments-list">
            <template v-for="(att, idx) in (currentPost.attachments || [])" :key="att.fileUrl || idx">
              <img
                v-if="isImageAttachment(att)"
                :src="att.fileUrl"
                :alt="att.fileName || '图片附件'"
                class="attachment-image"
              />
              <a v-else :href="att.fileUrl" target="_blank" rel="noopener" class="attachment-file">
                <i class="far fa-file"></i> {{ att.fileName || att.fileUrl }}
              </a>
            </template>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listReportsApi, reviewReportApi } from '@/api/moderation'
import { listPostsAdminApi, approvePostApi } from '@/api/admin/post'

const formatDateTime = (val) => (val ? String(val).replace('T', ' ') : '')

const reports = ref([])
const loading = ref(false)
const statusFilter = ref('IN_REVIEW')
const detailVisible = ref(false)
const currentReport = ref(null)

const pendingPosts = ref([])
const loadingPosts = ref(false)
const postDetailVisible = ref(false)
const currentPost = ref(null)

const loadPendingPosts = async () => {
  loadingPosts.value = true
  try {
    const res = await listPostsAdminApi({ status: 'UNDER_REVIEW', pageNum: 1, pageSize: 500 })
    pendingPosts.value = res.records || []
  } catch (e) {
    ElMessage.error(e.message || '加载待审核帖子失败')
  } finally {
    loadingPosts.value = false
  }
}

const approvePost = async (row) => {
  try {
    await approvePostApi(row.id)
    ElMessage.success('审核通过，帖子已发布')
    await loadPendingPosts()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

const openPostDetail = (row) => {
  currentPost.value = row
  postDetailVisible.value = true
}

const isImageAttachment = (att) => {
  const source = String(att?.fileName || att?.fileUrl || '').toLowerCase()
  return /\.(png|jpe?g|gif|webp|bmp|svg)$/.test(source)
}

const load = async () => {
  loading.value = true
  try {
    reports.value = await listReportsApi(statusFilter.value ? { status: statusFilter.value } : {})
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const openDetail = (row) => {
  currentReport.value = row
  detailVisible.value = true
}

const review = async (row, action) => {
  try {
    const res = await ElMessageBox.prompt('请输入审核备注（可选）', '审核确认', {
      inputPlaceholder: '例如：证据充分 / 证据不足',
      confirmButtonText: '确定'
    })
    await reviewReportApi({
      reportId: row.id,
      action,
      reviewRemark: res.value || ''
    })
    ElMessage.success('审核完成')
    await load()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '审核失败')
    }
  }
}

onMounted(() => {
  loadPendingPosts()
  load()
})
</script>

<style scoped>
.admin-page.moderation-page { max-width: 1200px; padding: 0 0 24px; }
.admin-card.section-card {
  background: #fff;
  border-radius: 28px;
  border: 1px solid #ffedd5;
  box-shadow: 0 8px 24px rgba(255, 160, 5, 0.06);
  padding: 24px;
  margin-bottom: 24px;
}
.admin-card-header { display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 16px; margin-bottom: 20px; }
.admin-title { margin: 0; font-size: 20px; font-weight: 700; color: #1f1f1f; display: flex; align-items: center; gap: 10px; }
.admin-title i { color: #ffa005; }
.filter-tabs :deep(.el-radio-button__inner) { border-radius: 50px; }
.filter-tabs :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) { background: #ffa005; border-color: #ffa005; }
.count-tip { font-size: 13px; color: #888; }
.admin-table :deep(.el-table__header th) { background: #f8f9fc; }
.admin-table :deep(.el-link--primary) { color: #ffa005; }
.table-empty { padding: 32px; text-align: center; color: #999; font-size: 14px; }
.report-detail .content-preview {
  max-height: 200px;
  overflow-y: auto;
  border: 1px solid #f0f2f5;
  padding: 16px;
  border-radius: 16px;
  margin: 8px 0;
  white-space: pre-wrap;
  background: #f8f9fc;
}
.report-detail .remark { color: #666; font-size: 13px; }
.attachments-preview {
  margin-top: 14px;
}
.attachments-title {
  margin: 0 0 10px 0;
  font-size: 14px;
  color: #666;
  display: flex;
  align-items: center;
  gap: 6px;
}
.attachments-title i {
  color: #ffa005;
}
.attachments-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.attachment-image {
  width: 100%;
  max-width: 560px;
  border-radius: 12px;
  border: 1px solid #eee;
  object-fit: contain;
  background: #fff;
}
.attachment-file {
  color: #409eff;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.attachment-file:hover {
  text-decoration: underline;
}
</style>
