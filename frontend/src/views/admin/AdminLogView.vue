<template>
  <div class="admin-page admin-log">
    <div class="admin-card">
      <h2 class="admin-title"><i class="fas fa-history"></i> 管理员操作日志</h2>

      <el-table :data="tableData" border stripe v-loading="loading" class="admin-table log-table">
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="operatorName" label="操作人" width="170">
          <template #default="{ row }">
            <span class="operator-cell no-wrap">
              <span class="operator-avatar">{{ (row.operatorName || '')?.slice(0, 1) }}</span>
              {{ row.operatorName || '-' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="action" label="操作" width="170">
          <template #default="{ row }">
            <span :class="['action-tag', actionTagClass(row.action), 'no-wrap']">
              <i :class="actionIcon(row.action)"></i>
              {{ actionText(row.action) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="targetType" label="对象类型" width="130">
          <template #default="{ row }">
            <span class="target-type-tag no-wrap">
              <i :class="targetTypeIcon(row.targetType)"></i>
              {{ targetTypeText(row.targetType) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="targetId" label="对象ID" width="85" align="center" />
        <el-table-column label="详情" min-width="360" show-overflow-tooltip>
          <template #default="{ row }">{{ detailText(row) }}</template>
        </el-table-column>
        <el-table-column prop="createdAt" label="时间" width="172">
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[20, 50]"
        layout="total, sizes, prev, pager, next"
        class="pagination"
        @current-change="load"
        @size-change="load"
      />
    </div>

    <div class="log-tip">
      <i class="fas fa-info-circle"></i>
      <span>所有操作码已转换为中文描述，方便阅读。点击操作列可筛选。</span>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listAdminLogsApi } from '@/api/admin/log'

const formatDateTime = (val) => (val ? String(val).replace('T', ' ') : '')

const actionMap = {
  POST_APPROVE: { text: '审核通过', icon: 'fas fa-check-circle', type: 'success' },
  POST_PIN: { text: '置顶', icon: 'fas fa-thumbtack', type: 'warning' },
  POST_FEATURE: { text: '加精', icon: 'fas fa-crown', type: 'warning' },
  POST_DELETE: { text: '删除帖子', icon: 'fas fa-trash-alt', type: 'danger' },
  POST_MOVE: { text: '移动帖子', icon: 'fas fa-arrows-alt', type: 'info' },
  BOARD_CREATE: { text: '新增版块', icon: 'fas fa-plus', type: 'info' },
  BOARD_UPDATE: { text: '编辑版块', icon: 'fas fa-edit', type: 'info' },
  BOARD_DELETE: { text: '删除版块', icon: 'fas fa-trash-alt', type: 'info' },
  BOARD_ORDER: { text: '调整排序', icon: 'fas fa-sort', type: 'info' },
  USER_STATUS: { text: '修改状态', icon: 'fas fa-user-cog', type: 'info' },
  USER_ASSIGN_ROLE: { text: '分配角色', icon: 'fas fa-user-tag', type: 'info' },
  USER_RESET_PASSWORD: { text: '重置密码', icon: 'fas fa-key', type: 'info' }
}

const targetTypeMap = {
  post: { text: '帖子', icon: 'fas fa-file-alt' },
  board: { text: '版块', icon: 'fas fa-th-large' },
  user: { text: '用户', icon: 'fas fa-user' },
  system: { text: '系统', icon: 'fas fa-cog' }
}

function actionText(code) {
  return actionMap[code]?.text || code || '-'
}

function actionIcon(code) {
  return actionMap[code]?.icon || 'fas fa-circle'
}

function actionTagClass(code) {
  const type = actionMap[code]?.type || 'info'
  return `action-tag--${type}`
}

function targetTypeText(code) {
  return code ? (targetTypeMap[code]?.text || code) : '-'
}

function targetTypeIcon(code) {
  return code ? (targetTypeMap[code]?.icon || 'fas fa-question') : ''
}

function detailText(row) {
  const raw = row?.detail || ''
  if (!raw) return '—'
  if (row?.action === 'POST_PIN') return raw === '置顶' ? '将帖子设为置顶' : '取消帖子置顶'
  if (row?.action === 'POST_FEATURE') return raw === '加精' ? '将帖子设为精华' : '取消帖子精华'
  if (row?.action === 'POST_DELETE') return `删除帖子：${raw}`
  if (row?.action === 'POST_MOVE') return `将帖子移动到版块（${raw.replace('boardId=', 'ID: ')})`
  if (row?.action === 'POST_APPROVE') return '审核通过帖子并发布'
  if (row?.action === 'BOARD_CREATE') return `新增版块：${raw}`
  if (row?.action === 'BOARD_UPDATE') return `编辑版块：${raw}`
  if (row?.action === 'BOARD_DELETE') return `删除版块：${raw}`
  if (row?.action === 'BOARD_ORDER') return '调整版块排序'
  if (row?.action === 'USER_RESET_PASSWORD') return '重置用户密码'
  if (row?.action === 'USER_STATUS') {
    if (raw.includes('status=1')) return '将用户状态改为：正常'
    if (raw.includes('status=0')) return '将用户状态改为：禁用'
  }
  if (row?.action === 'USER_ASSIGN_ROLE') {
    return '调整用户角色权限'
  }
  return raw
}

const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)
const tableData = ref([])
const loading = ref(false)

const load = async () => {
  loading.value = true
  try {
    const res = await listAdminLogsApi({ pageNum: pageNum.value, pageSize: pageSize.value })
    tableData.value = res.records || []
    total.value = res.total || 0
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.admin-page.admin-log { max-width: 1200px; padding: 0 0 24px; }
.admin-card {
  background: #fff;
  border-radius: 28px;
  border: 1px solid #ffedd5;
  box-shadow: 0 8px 24px rgba(255, 160, 5, 0.06);
  padding: 24px;
}
.admin-title {
  margin: 0 0 20px 0;
  font-size: 20px;
  font-weight: 700;
  color: #1f1f1f;
  display: flex;
  align-items: center;
  gap: 10px;
}
.admin-title i {
  color: #ffa005;
  background: #fff7e6;
  padding: 10px;
  border-radius: 50%;
  font-size: 18px;
}

.log-table :deep(.el-table__header th) {
  background: #f8f9fc;
  font-weight: 600;
  color: #4e4e57;
}
.log-table :deep(.el-table__cell) {
  padding: 14px 12px;
}

.operator-cell {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}
.no-wrap {
  white-space: nowrap;
}
.operator-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(145deg, #ffb96a, #ff8800);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.action-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}
.action-tag i { font-size: 11px; }
.action-tag--success { background: #e8f5e9; color: #2e7d32; }
.action-tag--warning { background: #fff3e0; color: #d46b08; }
.action-tag--danger { background: #ffebee; color: #c62828; }
.action-tag--info { background: #f0f2f5; color: #4e4e57; }

.target-type-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  background: #f0f2f5;
  color: #4e4e57;
}
.target-type-tag i {
  color: #ffa005;
  font-size: 11px;
}

.pagination {
  margin-top: 24px;
  margin-bottom: 0;
  justify-content: center;
}
.pagination :deep(.el-pager li.is-active) {
  background: #ffa005;
  border-radius: 40px;
}
.pagination :deep(.btn-prev),
.pagination :deep(.btn-next),
.pagination :deep(.el-pager li) {
  border-radius: 40px;
}

.log-tip {
  margin-top: 20px;
  padding: 14px 20px;
  background: #fffbf0;
  border: 1px solid #ffedd5;
  border-radius: 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  color: #8a6d3b;
}
.log-tip i {
  color: #ffa005;
  font-size: 18px;
  flex-shrink: 0;
}
</style>
