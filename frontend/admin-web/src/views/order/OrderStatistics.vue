<template>
  <div class="order-statistics">
    <div class="page-header">
      <h2>订单统计</h2>
      <div class="header-actions">
        <el-button type="primary" @click="refreshData">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
        <el-button @click="exportReport">
          <el-icon><Download /></el-icon>
          导出报表
        </el-button>
      </div>
    </div>

    <!-- 时间筛选 -->
    <el-card class="filter-card" shadow="never">
      <el-form :model="filterForm" inline>
        <el-form-item label="统计维度">
          <el-select v-model="filterForm.dimension" @change="handleDimensionChange">
            <el-option label="按日统计" value="day" />
            <el-option label="按周统计" value="week" />
            <el-option label="按月统计" value="month" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="filterForm.dateRange"
            :type="datePickerType"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            @change="handleDateChange"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadStatistics">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 统计概览 -->
    <div class="overview-cards">
      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-icon order-icon">
            <el-icon><Document /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ overview.totalOrders }}</div>
            <div class="stat-label">总订单数</div>
          </div>
        </div>
      </el-card>
      
      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-icon revenue-icon">
            <el-icon><Money /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">¥{{ overview.totalRevenue }}</div>
            <div class="stat-label">总收入</div>
          </div>
        </div>
      </el-card>
      
      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-icon completion-icon">
            <el-icon><CircleCheck /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ overview.completionRate }}%</div>
            <div class="stat-label">完成率</div>
          </div>
        </div>
      </el-card>
      
      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-icon avg-icon">
            <el-icon><TrendCharts /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">¥{{ overview.avgOrderValue }}</div>
            <div class="stat-label">平均订单价值</div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 图表区域 -->
    <div class="charts-container">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card class="chart-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span>订单趋势</span>
              </div>
            </template>
            <div ref="orderTrendChart" class="chart" style="height: 300px;"></div>
          </el-card>
        </el-col>
        
        <el-col :span="12">
          <el-card class="chart-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span>收入趋势</span>
              </div>
            </template>
            <div ref="revenueTrendChart" class="chart" style="height: 300px;"></div>
          </el-card>
        </el-col>
      </el-row>
      
      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="12">
          <el-card class="chart-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span>订单状态分布</span>
              </div>
            </template>
            <div ref="statusPieChart" class="chart" style="height: 300px;"></div>
          </el-card>
        </el-col>
        
        <el-col :span="12">
          <el-card class="chart-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span>热门配送区域</span>
              </div>
            </template>
            <div ref="regionBarChart" class="chart" style="height: 300px;"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 详细数据表格 -->
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>详细统计数据</span>
        </div>
      </template>
      
      <el-table :data="statisticsData" v-loading="loading" stripe>
        <el-table-column prop="date" label="日期" width="120" />
        <el-table-column prop="orderCount" label="订单数量" width="100" />
        <el-table-column prop="completedCount" label="完成数量" width="100" />
        <el-table-column prop="cancelledCount" label="取消数量" width="100" />
        <el-table-column prop="revenue" label="收入金额" width="120">
          <template #default="{ row }">
            <span class="revenue-text">¥{{ row.revenue }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="completionRate" label="完成率" width="100">
          <template #default="{ row }">
            <el-progress 
              :percentage="row.completionRate" 
              :stroke-width="8"
              :show-text="false"
            />
            <span style="margin-left: 8px;">{{ row.completionRate }}%</span>
          </template>
        </el-table-column>
        <el-table-column prop="avgOrderValue" label="平均订单价值" width="140">
          <template #default="{ row }">
            <span>¥{{ row.avgOrderValue }}</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Refresh, 
  Download, 
  Search,
  Document,
  Money,
  CircleCheck,
  TrendCharts
} from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const statisticsData = ref([])

// 筛选表单
const filterForm = reactive({
  dimension: 'day',
  dateRange: []
})

// 统计概览数据
const overview = reactive({
  totalOrders: 0,
  totalRevenue: '0.00',
  completionRate: 0,
  avgOrderValue: '0.00'
})

// 图表引用
const orderTrendChart = ref()
const revenueTrendChart = ref()
const statusPieChart = ref()
const regionBarChart = ref()

// 计算日期选择器类型
const datePickerType = computed(() => {
  const typeMap = {
    day: 'daterange',
    week: 'week',
    month: 'monthrange'
  }
  return typeMap[filterForm.dimension] || 'daterange'
})

/**
 * 加载统计数据
 */
const loadStatistics = async () => {
  loading.value = true
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 模拟统计数据
    const mockData = Array.from({ length: 30 }, (_, index) => {
      const orderCount = Math.floor(Math.random() * 50) + 10
      const completedCount = Math.floor(orderCount * (0.7 + Math.random() * 0.2))
      const cancelledCount = Math.floor(orderCount * Math.random() * 0.1)
      const revenue = (completedCount * (20 + Math.random() * 80)).toFixed(2)
      const completionRate = Math.floor((completedCount / orderCount) * 100)
      const avgOrderValue = (parseFloat(revenue) / completedCount || 0).toFixed(2)
      
      return {
        date: new Date(Date.now() - (29 - index) * 24 * 60 * 60 * 1000).toLocaleDateString(),
        orderCount,
        completedCount,
        cancelledCount,
        revenue,
        completionRate,
        avgOrderValue
      }
    })
    
    statisticsData.value = mockData
    
    // 计算概览数据
    const totalOrders = mockData.reduce((sum, item) => sum + item.orderCount, 0)
    const totalRevenue = mockData.reduce((sum, item) => sum + parseFloat(item.revenue), 0)
    const totalCompleted = mockData.reduce((sum, item) => sum + item.completedCount, 0)
    
    overview.totalOrders = totalOrders
    overview.totalRevenue = totalRevenue.toFixed(2)
    overview.completionRate = Math.floor((totalCompleted / totalOrders) * 100)
    overview.avgOrderValue = (totalRevenue / totalCompleted).toFixed(2)
    
    // 渲染图表
    await nextTick()
    renderCharts()
    
  } catch (error) {
    ElMessage.error('加载统计数据失败')
  } finally {
    loading.value = false
  }
}

/**
 * 渲染图表
 */
const renderCharts = () => {
  // 这里应该使用 ECharts 或其他图表库来渲染图表
  // 由于没有安装图表库，这里只是模拟
  console.log('渲染图表...')
  
  // 模拟图表渲染
  if (orderTrendChart.value) {
    orderTrendChart.value.innerHTML = '<div style="display: flex; align-items: center; justify-content: center; height: 100%; color: #999;">订单趋势图表 (需要安装 ECharts)</div>'
  }
  
  if (revenueTrendChart.value) {
    revenueTrendChart.value.innerHTML = '<div style="display: flex; align-items: center; justify-content: center; height: 100%; color: #999;">收入趋势图表 (需要安装 ECharts)</div>'
  }
  
  if (statusPieChart.value) {
    statusPieChart.value.innerHTML = '<div style="display: flex; align-items: center; justify-content: center; height: 100%; color: #999;">状态分布饼图 (需要安装 ECharts)</div>'
  }
  
  if (regionBarChart.value) {
    regionBarChart.value.innerHTML = '<div style="display: flex; align-items: center; justify-content: center; height: 100%; color: #999;">区域柱状图 (需要安装 ECharts)</div>'
  }
}

/**
 * 处理维度变化
 */
const handleDimensionChange = () => {
  filterForm.dateRange = []
}

/**
 * 处理日期变化
 */
const handleDateChange = () => {
  loadStatistics()
}

/**
 * 刷新数据
 */
const refreshData = () => {
  loadStatistics()
}

/**
 * 导出报表
 */
const exportReport = () => {
  ElMessage.success('导出功能开发中...')
}

// 组件挂载时加载数据
onMounted(() => {
  // 设置默认日期范围（最近30天）
  const endDate = new Date()
  const startDate = new Date()
  startDate.setDate(startDate.getDate() - 29)
  
  filterForm.dateRange = [
    startDate.toISOString().split('T')[0],
    endDate.toISOString().split('T')[0]
  ]
  
  loadStatistics()
})
</script>

<style scoped>
.order-statistics {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.filter-card {
  margin-bottom: 20px;
}

.overview-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
}

.order-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.revenue-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.completion-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.avg-icon {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.charts-container {
  margin-bottom: 20px;
}

.chart-card {
  margin-bottom: 20px;
}

.card-header {
  font-weight: 600;
  color: #303133;
}

.chart {
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px dashed #ddd;
}

.table-card {
  margin-bottom: 20px;
}

.revenue-text {
  font-weight: bold;
  color: #E6A23C;
}

@media (max-width: 768px) {
  .order-statistics {
    padding: 10px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .header-actions {
    justify-content: center;
  }
  
  .overview-cards {
    grid-template-columns: 1fr;
  }
  
  .stat-content {
    justify-content: center;
  }
}
</style>