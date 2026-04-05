<template>
  <el-card class="profile-card" shadow="hover">
    <template #header>个人资料</template>
    <el-form :model="form" label-width="80px" style="max-width: 480px">
      <el-form-item label="头像">
        <el-avatar :size="64" :src="form.avatar">{{ (form.nickname || '')?.slice(0, 1) }}</el-avatar>
        <el-upload
          class="avatar-upload"
          :show-file-list="false"
          :http-request="onUploadAvatar"
          :auto-upload="true"
        >
          <el-button type="primary" plain>上传头像</el-button>
        </el-upload>
      </el-form-item>
      <el-form-item label="昵称">
        <el-input v-model="form.nickname" placeholder="昵称" />
      </el-form-item>
      <el-form-item label="简介">
        <el-input v-model="form.bio" type="textarea" :rows="3" placeholder="个人简介" />
      </el-form-item>
      <el-form-item label="积分">
        <span>{{ form.credit ?? 0 }}</span>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="save">保存</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { profileApi, updateProfileApi } from '@/api/auth'
import { uploadFileApi } from '@/api/file'
import { useAuthStore } from '@/store/auth'

const authStore = useAuthStore()
const form = reactive({ nickname: '', avatar: '', bio: '', credit: 0 })

async function load() {
  try {
    const data = await profileApi()
    form.nickname = data.nickname ?? ''
    form.avatar = data.avatar ?? ''
    form.bio = data.bio ?? ''
    form.credit = data.credit ?? 0
  } catch (_) {}
}

async function onUploadAvatar(option) {
  try {
    const res = await uploadFileApi(option.file)
    form.avatar = res.url
    option.onSuccess(res)
  } catch (e) {
    ElMessage.error(e.message || '头像上传失败')
    option.onError(e)
  }
}

async function save() {
  try {
    const updated = await updateProfileApi({ nickname: form.nickname, avatar: form.avatar, bio: form.bio })
    // 覆盖整个 user，避免合并时遗漏字段导致侧边栏/个人信息未刷新
    authStore.user = updated
    form.nickname = updated.nickname ?? form.nickname
    form.avatar = updated.avatar ?? form.avatar
    form.bio = updated.bio ?? form.bio
    form.credit = updated.credit ?? form.credit
    authStore.persist()
    ElMessage.success('保存成功')
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  }
}

onMounted(load)
</script>

<style scoped>
.profile-card { max-width: 560px; margin: 0 auto; }
.avatar-upload { margin-left: 12px; }
</style>
