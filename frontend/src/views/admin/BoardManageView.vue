<template>
  <div class="admin-page board-manage">
    <div class="admin-card">
      <div class="admin-card-header">
        <h2 class="admin-title"><i class="fas fa-th-large"></i> 版块管理</h2>
        <el-button class="btn-primary" @click="openEdit()"><i class="fas fa-plus"></i> 新增版块</el-button>
      </div>
      <el-table :data="list" border stripe v-loading="loading" class="admin-table">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="名称" min-width="120" />
        <el-table-column prop="description" label="描述" min-width="200" />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="需审核" width="80">
          <template #default="{ row }">{{ row.needApproval ? '是' : '否' }}</template>
        </el-table-column>
        <el-table-column label="允许匿名" width="90">
          <template #default="{ row }">{{ row.allowAnonymous ? '是' : '否' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="doDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑版块' : '新增版块'" width="520px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="名称" required>
          <el-input v-model="form.name" placeholder="版块名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" rows="2" placeholder="版块描述" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="需审核">
          <el-switch v-model="form.needApproval" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="允许匿名">
          <el-switch v-model="form.allowAnonymous" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listBoardsAdminApi, saveBoardApi, deleteBoardApi } from '@/api/admin/board'

const list = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const form = reactive({
  id: null,
  name: '',
  description: '',
  sortOrder: 0,
  needApproval: 0,
  allowAnonymous: 0
})

const load = async () => {
  loading.value = true
  try {
    list.value = await listBoardsAdminApi()
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const openEdit = (row) => {
  if (row) {
    form.id = row.id
    form.name = row.name
    form.description = row.description ?? ''
    form.sortOrder = row.sortOrder ?? 0
    form.needApproval = row.needApproval ?? 0
    form.allowAnonymous = row.allowAnonymous ?? 0
  } else {
    form.id = null
    form.name = ''
    form.description = ''
    form.sortOrder = list.value.length
    form.needApproval = 0
    form.allowAnonymous = 0
  }
  dialogVisible.value = true
}

const submit = async () => {
  if (!form.name?.trim()) {
    ElMessage.warning('请输入版块名称')
    return
  }
  try {
    await saveBoardApi(form)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  }
}

const doDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除版块「${row.name}」？`, '提示')
    await deleteBoardApi(row.id)
    ElMessage.success('删除成功')
    load()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '删除失败')
  }
}

onMounted(load)
</script>

<style scoped>
.admin-page.board-manage { max-width: 1200px; padding: 0 0 24px; }
.admin-card {
  background: #fff;
  border-radius: 28px;
  border: 1px solid #ffedd5;
  box-shadow: 0 8px 24px rgba(255, 160, 5, 0.06);
  padding: 24px;
}
.admin-card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.admin-title { margin: 0; font-size: 20px; font-weight: 700; color: #1f1f1f; display: flex; align-items: center; gap: 10px; }
.admin-title i { color: #ffa005; }
.btn-primary { border-radius: 50px; background: #ffa005; border-color: #ffa005; }
.btn-primary:hover { background: #e69404; border-color: #e69404; }
.admin-table :deep(.el-table__header th) { background: #f8f9fc; }
</style>
