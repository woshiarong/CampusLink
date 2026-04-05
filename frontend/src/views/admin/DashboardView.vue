<template>
  <div class="admin-page dashboard">
    <h2 class="page-title"><i class="fas fa-chart-line"></i> 数据概览</h2>
    <el-row :gutter="20" class="stat-cards">
      <el-col :xs="24" :sm="12" :md="8" :lg="6">
        <div class="stat-card">
          <div class="stat-label"><i class="fas fa-users"></i> 总用户数</div>
          <div class="stat-value">{{ stats.totalUsers }}</div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8" :lg="6">
        <div class="stat-card">
          <div class="stat-label"><i class="fas fa-file-alt"></i> 总帖子数</div>
          <div class="stat-value">{{ stats.totalPosts }}</div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8" :lg="6">
        <div class="stat-card highlight">
          <div class="stat-label"><i class="fas fa-user-plus"></i> 今日新增用户</div>
          <div class="stat-value">{{ stats.todayNewUsers }}</div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8" :lg="6">
        <div class="stat-card highlight">
          <div class="stat-label"><i class="fas fa-pen-fancy"></i> 今日新增帖子</div>
          <div class="stat-value">{{ stats.todayNewPosts }}</div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="12">
        <div class="stat-card warning">
          <div class="stat-label"><i class="fas fa-clock"></i> 待审核帖子</div>
          <div class="stat-value">{{ stats.pendingReviewCount }}</div>
          <el-button class="btn-go" @click="$router.push('/moderation')">去处理</el-button>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="12">
        <div class="stat-card danger">
          <div class="stat-label"><i class="fas fa-flag"></i> 待处理举报</div>
          <div class="stat-value">{{ stats.reportCount }}</div>
          <el-button class="btn-go" @click="$router.push('/moderation')">去处理</el-button>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :xs="24" :lg="14">
        <div class="chart-card">
          <div class="chart-header"><i class="fas fa-pie-chart"></i> 各版块帖子数</div>
          <div v-show="stats.postCountByBoard?.length" ref="chartBoardRef" class="chart"></div>
          <div v-if="!stats.postCountByBoard?.length" class="chart-empty">暂无版块数据</div>
        </div>
      </el-col>
      <el-col :xs="24" :lg="10">
        <div class="chart-card">
          <div class="chart-header"><i class="fas fa-chart-area"></i> 近七日活跃趋势</div>
          <div ref="chartRef" class="chart"></div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { dashboardApi } from '@/api/admin/statistics'
import * as echarts from 'echarts'

const stats = ref({
  totalUsers: 0,
  totalPosts: 0,
  todayNewUsers: 0,
  todayNewPosts: 0,
  pendingReviewCount: 0,
  reportCount: 0,
  recentActivity: [],
  postCountByBoard: []
})
const chartRef = ref(null)
const chartBoardRef = ref(null)

const load = async () => {
  try {
    const data = await dashboardApi()
    stats.value = data
    await nextTick()
    renderBoardPieChart()
    renderTrendChart()
  } catch (e) {
    console.error(e)
  }
}

const renderBoardPieChart = () => {
  if (!chartBoardRef.value) return
  const chart = echarts.init(chartBoardRef.value)
  const list = stats.value.postCountByBoard || []
  const data = list.map((d) => ({ name: d.boardName, value: d.postCount }))
  chart.setOption({
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} 篇 ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: 16,
      top: 'center',
      data: list.map((d) => d.boardName)
    },
    color: [
      '#ffa005', '#5470c6', '#91cc75', '#fac858', '#ee6666',
      '#73c0de', '#3ba272', '#fc8452', '#9a60b4', '#ea7ccc'
    ],
    series: [
      {
        name: '帖子数',
        type: 'pie',
        radius: ['40%', '65%'],
        center: ['40%', '50%'],
        avoidLabelOverlap: true,
        itemStyle: { borderRadius: 8 },
        label: { show: true, formatter: '{b}\n{c} 篇' },
        data
      }
    ]
  })
}

const renderTrendChart = () => {
  if (!chartRef.value) return
  const chart = echarts.init(chartRef.value)
  const list = stats.value.recentActivity || []
  const dates = list.length ? list.map((d) => d.date.slice(5)) : []
  const userData = list.length ? list.map((d) => d.userCount) : []
  const postData = list.length ? list.map((d) => d.postCount) : []
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['新增用户', '新增帖子'] },
    color: ['#5470c6', '#ffa005'],
    grid: { left: '12%', right: '8%', bottom: '15%', top: '15%', containLabel: true },
    xAxis: { type: 'category', data: dates },
    yAxis: { type: 'value' },
    series: [
      { name: '新增用户', type: 'line', data: userData, smooth: true },
      { name: '新增帖子', type: 'line', data: postData, smooth: true }
    ]
  })
}

onMounted(() => {
  load()
})
</script>

<style scoped>
.admin-page.dashboard {
  max-width: 1200px;
  padding: 0 0 24px;
}
.page-title {
  margin: 0 0 24px 0;
  font-size: 22px;
  font-weight: 700;
  color: #1f1f1f;
  display: flex;
  align-items: center;
  gap: 10px;
}
.page-title i {
  color: #ffa005;
  background: #fff7e6;
  padding: 10px;
  border-radius: 16px;
}
.stat-cards {
  margin-bottom: 24px;
}
.stat-card {
  background: #fff;
  border-radius: 28px;
  border: 1px solid #ffedd5;
  box-shadow: 0 8px 24px rgba(255, 160, 5, 0.06);
  padding: 22px 24px;
  margin-bottom: 20px;
  position: relative;
}
.stat-label {
  color: #888;
  font-size: 14px;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.stat-label i {
  color: #ffa005;
}
.stat-value {
  font-size: 26px;
  font-weight: 700;
  color: #1f1f1f;
}
.stat-card.highlight .stat-value {
  color: #ffa005;
}
.stat-card.warning .stat-value {
  color: #d46b08;
}
.stat-card.danger .stat-value {
  color: #e34d4d;
}
.btn-go {
  margin-top: 12px;
  border-radius: 50px;
  color: #ffa005;
  border-color: #ffa005;
}
.btn-go:hover {
  background: #fff7e6;
  color: #e69404;
  border-color: #e69404;
}
.chart-card {
  background: #fff;
  border-radius: 28px;
  border: 1px solid #ffedd5;
  box-shadow: 0 8px 24px rgba(255, 160, 5, 0.06);
  padding: 24px;
  margin-bottom: 20px;
}
.chart-header {
  font-size: 16px;
  font-weight: 600;
  color: #1f1f1f;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.chart-header i {
  color: #ffa005;
}
.chart {
  height: 280px;
}
.chart-empty {
  padding: 32px;
  text-align: center;
  color: #999;
  font-size: 14px;
}
</style>
