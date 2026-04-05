<template>
  <div class="admin-page user-manage">
    <div class="admin-card">
      <div class="admin-card-header">
        <h2 class="admin-title"><i class="fas fa-users-cog"></i> 用户管理</h2>
        <div class="header-actions">
          <el-input v-model="keyword" placeholder="用户名/昵称/邮箱" clearable class="search-input" @keyup.enter="load" />
          <el-select v-model="statusFilter" placeholder="状态" clearable style="width: 100px">
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
          <el-button class="btn-primary" @click="load"><i class="fas fa-search"></i> 查询</el-button>
        </div>
      </div>
      <el-table :data="tableData" border stripe v-loading="loading" class="admin-table">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="用户名" min-width="100" />
        <el-table-column prop="nickname" label="昵称" min-width="100" />
        <el-table-column prop="email" label="邮箱" min-width="140" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '正常' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="roleCodes" label="角色" width="120">
          <template #default="{ row }">
            <el-tag v-for="r in (row.roleCodes || [])" :key="r" size="small" style="margin-right: 4px">{{ roleNameMap[r] || r }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" min-width="165">
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column prop="lastLoginAt" label="最后登录" min-width="165">
          <template #default="{ row }">{{ formatDateTime(row.lastLoginAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right" align="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEdit(row)">编辑</el-button>
            <el-button type="warning" link @click="openResetPwd(row)">重置密码</el-button>
            <el-button v-if="row.status === 1" type="danger" link @click="setStatus(row, 0)">禁言</el-button>
            <el-button v-else type="success" link @click="setStatus(row, 1)">解禁</el-button>
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

    <el-dialog v-model="editVisible" title="编辑用户" width="480px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="状态">
          <el-radio-group v-model="editForm.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="角色">
          <el-radio-group v-model="editForm.roleId">
            <el-radio v-for="r in editableRoles" :key="r.id" :label="r.id">{{ roleNameMap[r.code] || r.name }}</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="pwdVisible" title="重置密码" width="400px">
      <el-form :model="pwdForm" label-width="80px">
        <el-form-item label="新密码">
          <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdVisible = false">取消</el-button>
        <el-button type="primary" @click="submitResetPwd">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listUsersApi, updateUserApi, resetPasswordApi } from '@/api/admin/user'
import { listRolesApi } from '@/api/admin/role'

const route = useRoute()
const roleNameMap = { student: '学生', administrator: '管理员', admin: '管理员' }
const keyword = ref(route.query.keyword || '')
const statusFilter = ref(route.query.status !== undefined ? Number(route.query.status) : undefined)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])
const loading = ref(false)
const editVisible = ref(false)
const pwdVisible = ref(false)
const roles = ref([])
const editForm = reactive({ userId: null, status: 1, roleId: null })
const pwdForm = reactive({ userId: null, newPassword: '' })
const editableRoles = ref([])

const load = async () => {
  loading.value = true
  try {
    const res = await listUsersApi({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: keyword.value || undefined,
      status: statusFilter.value
    })
    tableData.value = res.records || []
    total.value = res.total || 0
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const loadRoles = async () => {
  try {
    roles.value = await listRolesApi()
    editableRoles.value = (roles.value || []).filter((r) => ['administrator', 'admin', 'student'].includes(r.code))
  } catch (e) {
    console.error(e)
  }
}

const openEdit = (row) => {
  editForm.userId = row.id
  editForm.status = row.status
  const codes = row.roleCodes || []
  const matched = (editableRoles.value || []).find((r) => codes.includes(r.code))
  editForm.roleId = matched ? matched.id : null
  editVisible.value = true
}

const submitEdit = async () => {
  if (!editForm.roleId) {
    ElMessage.warning('请选择角色（管理员或学生）')
    return
  }
  try {
    await updateUserApi({
      userId: editForm.userId,
      status: editForm.status,
      roleIds: [editForm.roleId]
    })
    ElMessage.success('保存成功')
    editVisible.value = false
    load()
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  }
}

const openResetPwd = (row) => {
  pwdForm.userId = row.id
  pwdForm.newPassword = ''
  pwdVisible.value = true
}

const submitResetPwd = async () => {
  if (!pwdForm.newPassword?.trim()) {
    ElMessage.warning('请输入新密码')
    return
  }
  try {
    await resetPasswordApi({ userId: pwdForm.userId, newPassword: pwdForm.newPassword })
    ElMessage.success('密码已重置')
    pwdVisible.value = false
  } catch (e) {
    ElMessage.error(e.message || '重置失败')
  }
}

const setStatus = async (row, status) => {
  try {
    await ElMessageBox.confirm(status === 0 ? '确定禁言该用户？' : '确定解除禁言？', '提示')
    await updateUserApi({ userId: row.id, status })
    ElMessage.success('操作成功')
    load()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '操作失败')
  }
}

/** 系统时间展示：年月日 与 具体时间 用空格隔开，不用 T */
const formatDateTime = (val) => (val ? String(val).replace('T', ' ') : '')

watch(() => route.query.keyword, (v) => { keyword.value = v || '' })
onMounted(() => {
  loadRoles()
  load()
})
</script>

<style scoped>
.admin-page.user-manage { max-width: 1200px; padding: 0 0 24px; }
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
.search-input { width: 200px; }
.search-input :deep(.el-input__wrapper) { border-radius: 50px; }
.btn-primary { border-radius: 50px; background: #ffa005; border-color: #ffa005; }
.btn-primary:hover { background: #e69404; border-color: #e69404; }
.admin-table :deep(.el-table__header th) { background: #f8f9fc; }
.admin-table :deep(.el-link--primary) { color: #ffa005; }
.pagination { margin-top: 16px; justify-content: flex-end; }
</style>
