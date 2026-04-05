<template>
  <el-table :data="data" border style="width: 100%">
    <el-table-column
      v-for="col in columns"
      :key="col.prop"
      :prop="col.prop"
      :label="col.label"
      :min-width="col.minWidth || 120"
    />
    <el-table-column v-if="$slots.action" label="操作" :min-width="actionWidth">
      <template #default="scope">
        <slot name="action" :row="scope.row"></slot>
      </template>
    </el-table-column>
  </el-table>
</template>

<script setup>
defineProps({
  data: {
    type: Array,
    default: () => []
  },
  columns: {
    type: Array,
    default: () => []
  },
  actionWidth: {
    type: Number,
    default: 220
  }
})
</script>
