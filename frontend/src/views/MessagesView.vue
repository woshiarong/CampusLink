<template>
  <div class="messages-page">
    <el-card shadow="hover">
      <template #header>
        <div class="header">
          <span>消息中心</span>
          <el-button type="primary" link @click="markAllRead">全部已读</el-button>
        </div>
      </template>
      <el-empty v-if="!loading && !list.length" description="暂无消息" />
      <div v-else class="list">
        <div
          v-for="item in list"
          :key="item.id"
          class="item"
          :class="{ unread: item.isRead === 0 }"
          @click="open(item)"
        >
          <div class="title">
            <span class="dot" v-if="item.isRead === 0"></span>
            {{ item.title }}
          </div>
          <div class="content">{{ item.content }}</div>
          <div class="time">{{ formatDateTime(item.createdAt) }}</div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { listMessagesApi, markAllMessagesReadApi, markMessageReadApi } from '@/api/message'

const router = useRouter()
const list = ref([])
const loading = ref(false)

const formatDateTime = (val) => (val ? String(val).replace('T', ' ') : '')

async function load() {
  loading.value = true
  try {
    list.value = await listMessagesApi()
  } catch (e) {
    ElMessage.error(e.message || '加载消息失败')
  } finally {
    loading.value = false
  }
}

async function open(item) {
  if (item.isRead === 0) {
    try {
      await markMessageReadApi(item.id)
      item.isRead = 1
    } catch (_) {}
  }
  if (item.refId) {
    router.push(`/post/${item.refId}`)
  }
}

async function markAllRead() {
  try {
    await markAllMessagesReadApi()
    list.value = (list.value || []).map((it) => ({ ...it, isRead: 1 }))
    ElMessage.success('已全部标记为已读')
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

onMounted(load)
</script>

<style scoped>
.messages-page { max-width: 900px; margin: 0 auto; }
.header { display: flex; align-items: center; justify-content: space-between; }
.list { display: flex; flex-direction: column; gap: 10px; }
.item {
  border: 1px solid #eee;
  border-radius: 12px;
  padding: 12px 14px;
  cursor: pointer;
  background: #fff;
}
.item.unread { border-color: #ffd7a0; background: #fffaf2; }
.title { font-weight: 600; color: #333; display: flex; align-items: center; gap: 8px; }
.dot { width: 8px; height: 8px; border-radius: 50%; background: #ffa005; display: inline-block; }
.content { margin-top: 6px; color: #666; font-size: 14px; }
.time { margin-top: 6px; color: #999; font-size: 12px; }
</style>
