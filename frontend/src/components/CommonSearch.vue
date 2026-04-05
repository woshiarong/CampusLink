<template>
  <div class="search-wrap">
    <el-input
      v-model="keyword"
      placeholder="请输入关键词"
      clearable
      @keyup.enter="emitSearch"
      @clear="emitSearch"
    />
    <el-button type="primary" @click="emitSearch">搜索</el-button>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const emit = defineEmits(['search'])
const keyword = ref('')

const emitSearch = () => {
  emit('search', keyword.value)
}
</script>

<style scoped>
.search-wrap {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
}
</style>
