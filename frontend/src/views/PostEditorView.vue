<template>
  <div class="editor-page">
    <!-- ========= 发布新帖 ========= -->
    <div class="interface-card">
      <div class="interface-header">
        <div class="icon"><i class="fas fa-feather-alt"></i></div>
        <h2>发布新帖 <span>· 分享你的校园故事</span></h2>
      </div>

      <div class="publish-form-wrap">
        <post-form ref="postFormRef" :boards="boards" hide-actions @save="createDraft" />
        <div class="form-actions">
          <button type="button" class="btn-primary" @click="submitPublish">
            <i class="fas fa-paper-plane"></i> 立即发布
          </button>
          <button type="button" class="btn-secondary" @click="clearForm">
            <i class="fas fa-undo-alt"></i> 清空
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PostForm from '@/components/PostForm.vue'
import { listBoardsApi } from '@/api/board'
import { createPostApi } from '@/api/post'

const postFormRef = ref(null)
const boards = ref([])

const loadBoards = async () => {
  boards.value = await listBoardsApi()
}

const submitPublish = () => {
  postFormRef.value?.onSave?.()
}

const clearForm = async () => {
  try {
    await ElMessageBox.confirm('确定清空已填写内容？', '提示')
    postFormRef.value?.onClear?.()
  } catch (_) {}
}

const createDraft = async (payload) => {
  try {
    const post = await createPostApi(payload)
    if (post?.status === 'VIEWABLE') {
      ElMessage.success('发布成功')
    } else {
      ElMessage.success('草稿已保存，可到个人中心 · 我的发布 查看或提交')
    }
  } catch (error) {
    ElMessage.error(error.message || '保存失败')
  }
}

onMounted(loadBoards)
</script>

<style scoped>
.editor-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 16px 40px;
  background: #f4f6f9;
  min-height: calc(100vh - 100px);
}

.interface-card {
  background: #fff;
  border-radius: 40px;
  padding: 32px 36px;
  box-shadow: 0 16px 32px rgba(0, 0, 0, 0.04);
  border: 1px solid #ffedd5;
  margin-bottom: 24px;
}

.interface-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 28px;
}

.interface-header .icon {
  background: #ffa005;
  color: #fff;
  width: 52px;
  height: 52px;
  border-radius: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  box-shadow: 0 8px 16px rgba(255, 193, 7, 0.45);
}

.interface-header h2 {
  font-size: 26px;
  font-weight: 700;
  color: #1c1c1e;
}

.interface-header h2 span {
  color: #ffa005;
  font-size: 15px;
  font-weight: 400;
  margin-left: 10px;
}

.publish-form-wrap {
  margin-top: 8px;
}

.publish-form-wrap :deep(.el-form-item) {
  margin-bottom: 24px;
}

.publish-form-wrap :deep(.el-form-item__label) {
  font-weight: 600;
  color: #2c2c2e;
}

.publish-form-wrap :deep(.el-input__wrapper),
.publish-form-wrap :deep(.el-textarea__inner) {
  border-radius: 30px !important;
  padding: 12px 20px;
  border: 1px solid #eaecef;
  background: #f8f9fc;
  box-shadow: none;
}

.publish-form-wrap :deep(.el-input__wrapper:hover),
.publish-form-wrap :deep(.el-textarea__inner:hover) {
  border-color: #ffd7a0;
}

.publish-form-wrap :deep(.el-input__wrapper.is-focus),
.publish-form-wrap :deep(.el-textarea__inner:focus) {
  border-color: #ffa005 !important;
  background: #fff !important;
  box-shadow: 0 0 0 4px rgba(255, 160, 5, 0.15) !important;
}

.publish-form-wrap :deep(.el-textarea__inner) {
  min-height: 130px;
  border-radius: 30px !important;
}

.form-actions {
  display: flex;
  gap: 18px;
  margin-top: 28px;
}

.btn-primary,
.btn-secondary {
  border: none;
  padding: 16px 28px;
  border-radius: 60px;
  font-size: 17px;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  cursor: pointer;
  transition: 0.2s;
}

.btn-primary {
  flex: 2;
  background: #ffa005;
  color: #fff;
  box-shadow: 0 12px 22px -8px rgba(255, 160, 5, 0.8);
}

.btn-primary:hover {
  background: #e69404;
  transform: translateY(-1px);
}

.btn-secondary {
  flex: 1;
  background: #fff;
  border: 2px solid #e0e4e9;
  color: #5f5f6b;
}

.btn-secondary:hover {
  border-color: #ffa005;
  color: #ffa005;
}

@media (max-width: 700px) {
  .interface-card {
    padding: 22px 20px;
  }
  .form-actions {
    flex-direction: column;
  }
  .btn-primary, .btn-secondary {
    flex: none;
    width: 100%;
  }
}
</style>
