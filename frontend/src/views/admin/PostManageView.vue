<template>
  <div class="admin-page post-manage">
    <div class="admin-card">
      <div class="admin-card-header">
        <h2 class="admin-title"><i class="fas fa-file-alt"></i> 帖子管理</h2>
        <div class="header-actions">
          <el-select v-model="boardId" placeholder="版块" clearable style="width: 140px">
            <el-option v-for="b in boards" :key="b.id" :label="b.name" :value="b.id" />
          </el-select>
          <el-select v-model="statusFilter" placeholder="状态" clearable style="width: 120px">
            <el-option label="可查看" value="VIEWABLE" />
            <el-option label="待审核" value="UNDER_REVIEW" />
            <el-option label="草稿" value="DRAFTING" />
            <el-option label="已隐藏" value="MODERATED" />
          </el-select>
          <el-select v-model="sortBy" placeholder="排序" style="width: 100px">
            <el-option label="最新" value="latest" />
            <el-option label="最早" value="oldest" />
          </el-select>
          <el-button class="btn-primary" @click="load"><i class="fas fa-search"></i> 查询</el-button>
        </div>
      </div>
      <el-table :data="tableData" border stripe v-loading="loading" class="admin-table">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <el-link type="primary" @click="openDetail(row)" :underline="false">{{ row.title }}</el-link>
            <el-tag v-if="row.isPinned" type="warning" size="small" style="margin-left: 4px">置顶</el-tag>
            <el-tag v-if="row.isFeatured" type="success" size="small" style="margin-left: 4px">精</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="authorName" label="作者" width="100" />
        <el-table-column prop="boardName" label="版块" width="100" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="发布时间" width="160">
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="380" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'UNDER_REVIEW'" type="success" link @click="approvePost(row)">审核通过</el-button>
            <el-button type="primary" link @click="togglePin(row)">{{ row.isPinned ? '取消置顶' : '置顶' }}</el-button>
            <el-button type="success" link @click="toggleFeature(row)">{{ row.isFeatured ? '取消加精' : '加精' }}</el-button>
            <el-button type="warning" link @click="openMove(row)">移动</el-button>
            <el-button type="danger" link @click="doDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        class="pagination"
        @current-change="load"
        @size-change="load"
      />
    </div>

    <el-dialog v-model="detailVisible" title="帖子详情" width="700px">
      <div v-if="currentPost" class="post-detail">
        <p><strong>标题：</strong>{{ currentPost.title }}</p>
        <p><strong>作者：</strong>{{ currentPost.authorName }} &nbsp; <strong>版块：</strong>{{ currentPost.boardName }}</p>
        <p><strong>状态：</strong>{{ statusText(currentPost.status) }}</p>
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

    <el-dialog v-model="moveVisible" title="移动版块" width="400px">
      <el-form label-width="80px">
        <el-form-item label="目标版块">
          <el-select v-model="moveBoardId" placeholder="选择版块" style="width: 100%">
            <el-option v-for="b in boards" :key="b.id" :label="b.name" :value="b.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="moveVisible = false">取消</el-button>
        <el-button type="primary" @click="submitMove">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listPostsAdminApi, pinPostApi, featurePostApi, deletePostAdminApi, movePostApi, approvePostApi } from '@/api/admin/post'
import { listBoardsApi } from '@/api/board'

const route = useRoute()
const boards = ref([])
const boardId = ref(null)
const statusFilter = ref('')
const sortBy = ref('latest')
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])
const loading = ref(false)
const detailVisible = ref(false)
const moveVisible = ref(false)
const currentPost = ref(null)
const movePostId = ref(null)
const moveBoardId = ref(null)

const load = async () => {
  loading.value = true
  try {
    const res = await listPostsAdminApi({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      boardId: boardId.value || undefined,
      status: statusFilter.value || undefined,
      sortBy: sortBy.value
    })
    tableData.value = res.records || []
    total.value = res.total || 0
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const loadBoards = async () => {
  try {
    boards.value = await listBoardsApi()
  } catch (e) {
    console.error(e)
  }
}

const formatDateTime = (val) => (val ? String(val).replace('T', ' ') : '')

const statusText = (status) => {
  const map = {
    DRAFTING: '草稿',
    SUBMITTED: '已提交',
    UNDER_REVIEW: '待审核',
    PUBLISHED: '已发布',
    VIEWABLE: '可查看',
    INTERACTED: '已互动',
    UPDATED: '已更新',
    REPUBLISHED: '再发布',
    REPORTED: '已举报',
    MODERATED: '已隐藏',
    ACTION_EXECUTED: '已处理',
    LOG_RECORDED: '已记录'
  }
  return map[status] || status || '-'
}

const isImageAttachment = (att) => {
  const source = String(att?.fileName || att?.fileUrl || '').toLowerCase()
  return /\.(png|jpe?g|gif|webp|bmp|svg)$/.test(source)
}

const openDetail = (row) => {
  currentPost.value = row
  detailVisible.value = true
}

const togglePin = async (row) => {
  try {
    await pinPostApi(row.id, row.isPinned ? 0 : 1)
    ElMessage.success('操作成功')
    load()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

const toggleFeature = async (row) => {
  try {
    await featurePostApi(row.id, row.isFeatured ? 0 : 1)
    ElMessage.success('操作成功')
    load()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

const openMove = (row) => {
  movePostId.value = row.id
  moveBoardId.value = row.boardId
  moveVisible.value = true
}

const submitMove = async () => {
  if (!moveBoardId.value) {
    ElMessage.warning('请选择目标版块')
    return
  }
  try {
    await movePostApi(movePostId.value, moveBoardId.value)
    ElMessage.success('移动成功')
    moveVisible.value = false
    load()
  } catch (e) {
    ElMessage.error(e.message || '移动失败')
  }
}

const approvePost = async (row) => {
  try {
    await approvePostApi(row.id)
    ElMessage.success('审核通过，帖子已发布')
    load()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

const doDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该帖子？删除后不可恢复。', '提示', { type: 'warning' })
    await deletePostAdminApi(row.id)
    ElMessage.success('删除成功')
    load()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '删除失败')
  }
}

watch(() => route.query.keyword, () => load(), { immediate: false })
onMounted(() => {
  loadBoards()
  load()
})
</script>

<style scoped>
.admin-page.post-manage { max-width: 1200px; padding: 0 0 24px; }
.admin-card {
  background: #fff;
  border-radius: 28px;
  border: 1px solid #ffedd5;
  box-shadow: 0 8px 24px rgba(255, 160, 5, 0.06);
  padding: 24px;
}
.admin-card-header { display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 16px; margin-bottom: 20px; }
.admin-title { margin: 0; font-size: 20px; font-weight: 700; color: #1f1f1f; display: flex; align-items: center; gap: 10px; }
.admin-title i { color: #ffa005; }
.header-actions { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.btn-primary { border-radius: 50px; background: #ffa005; border-color: #ffa005; }
.btn-primary:hover { background: #e69404; border-color: #e69404; }
.admin-table :deep(.el-table__header th) { background: #f8f9fc; }
.admin-table :deep(.el-link--primary) { color: #ffa005; }
.pagination { margin-top: 16px; justify-content: flex-end; }
.post-detail .content-preview {
  max-height: 400px;
  overflow-y: auto;
  border: 1px solid #f0f2f5;
  padding: 16px;
  border-radius: 16px;
  margin-top: 12px;
  background: #f8f9fc;
}
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
