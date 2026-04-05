<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-header">
        <i class="fas fa-user-circle auth-icon"></i>
        <h1 class="auth-title">用户登录</h1>
        <p class="auth-desc">欢迎回来，登录后即可发帖、收藏与互动</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" class="auth-form" label-position="top">
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            size="large"
            clearable
          >
            <template #prefix><i class="fas fa-user input-icon"></i></template>
          </el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            show-password
            @keyup.enter="submit"
          >
            <template #prefix><i class="fas fa-lock input-icon"></i></template>
          </el-input>
        </el-form-item>
        <el-form-item class="form-actions">
          <el-button class="btn-submit" :loading="loading" @click="submit">
            <i class="fas fa-sign-in-alt"></i> 登录
          </el-button>
          <span class="auth-link-wrap">
            还没有账号？<router-link to="/register" class="auth-link">去注册</router-link>
          </span>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/store/auth'

const authStore = useAuthStore()
const router = useRouter()
const loading = ref(false)
const formRef = ref()
const form = reactive({ username: '', password: '' })

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const submit = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    await authStore.login(form)
    ElMessage.success('登录成功')
    router.push(authStore.isAdmin ? '/dashboard' : '/posts')
  } catch (error) {
    ElMessage.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: calc(100vh - 40px);
  background: #f4f6f9;
  padding: 32px 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.auth-card {
  width: 100%;
  max-width: 420px;
  background: #fff;
  border-radius: 28px;
  border: 1px solid #ffedd5;
  box-shadow: 0 8px 32px rgba(255, 160, 5, 0.08);
  overflow: hidden;
  padding: 36px 32px;
}

.auth-header {
  text-align: center;
  margin-bottom: 28px;
}

.auth-icon {
  font-size: 48px;
  color: #ffa005;
  margin-bottom: 12px;
  display: block;
}

.auth-title {
  margin: 0 0 8px;
  font-size: 22px;
  font-weight: 700;
  color: #1f1f1f;
}

.auth-desc {
  margin: 0;
  font-size: 14px;
  color: #888;
}

.auth-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #4e4e57;
  margin-bottom: 8px;
}

.auth-form :deep(.el-input__wrapper) {
  border-radius: 50px;
  border: 1px solid #eee;
  box-shadow: none;
  padding: 4px 16px 4px 40px;
}

.auth-form :deep(.el-input__wrapper:hover),
.auth-form :deep(.el-input__wrapper.is-focus) {
  border-color: #ffd7a0;
}

.input-icon {
  color: #ffa005;
  font-size: 14px;
}

.form-actions {
  margin-bottom: 0;
  margin-top: 28px;
}

.form-actions :deep(.el-form-item__content) {
  flex-direction: column;
  align-items: stretch;
  gap: 16px;
}

.btn-submit {
  width: 100%;
  height: 48px;
  border-radius: 50px;
  font-size: 16px;
  font-weight: 600;
  background: #ffa005;
  border-color: #ffa005;
}

.btn-submit:hover {
  background: #e69404;
  border-color: #e69404;
}

.btn-submit i {
  margin-right: 8px;
}

.auth-link-wrap {
  text-align: center;
  font-size: 14px;
  color: #888;
}

.auth-link {
  color: #ffa005;
  font-weight: 500;
  text-decoration: none;
}

.auth-link:hover {
  text-decoration: underline;
}
</style>
