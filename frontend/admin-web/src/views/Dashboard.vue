<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon orders">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-number">{{ stats.totalOrders }}</div>
              <div class="stats-label">总订单数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon users">
              <el-icon><User /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-number">{{ stats.totalUsers }}</div>
              <div class="stats-label">注册用户</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon pilots">
              <el-icon><Avatar /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-number">{{ stats.onlinePilots }}</div>
              <div class="stats-label">在线飞手</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon revenue">
              <el-icon><Money /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-number">¥{{ stats.todayRevenue }}</div>
              <div class="stats-label">今日收入</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-row">
      <el-col :span="12">
        <el-card title="订单趋势" class="chart-card">
          <div class="chart-container">
            <div class="chart-placeholder">
              <el-icon><TrendCharts /></el-icon>
              <p>订单趋势图表</p>
              <p class="chart-desc">显示最近7天的订单数量变化</p>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card title="收入统计" class="chart-card">
          <div class="chart-container">
            <div class="chart-placeholder">
              <el-icon><PieChart /></el-icon>
              <p>收入统计图表</p>
              <p class="chart-desc">显示各类订单的收入占比</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 最近订单 -->
    <el-row class="recent-orders-row">
      <el-col :span="24">
        <el-card title="最近订单" class="recent-orders-card">
          <el-table :data="recentOrders" style="width: 100%">
            <el-table-column prop="orderNo" label="订单号" width="180" />
            <el-table-column prop="userName" label="用户" width="120" />
            <el-table-column prop="pilotName" label="飞手" width="120" />
            <el-table-column prop="amount" label="金额" width="100">
              <template #default="{ row }">
                ¥{{ row.amount }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" />
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button type="text" size="small" @click="viewOrder(row)">
                  查看
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

// 统计数据
const stats = ref({
  totalOrders: 1234,
  totalUsers: 5678,
  onlinePilots: 23,
  todayRevenue: 12580
})

// 最近订单数据
const recentOrders = ref([
  {
    id: 1,
    orderNo: 'DO202312010001',
    userName: '张三',
    pilotName: '李飞手',
    amount: 25.80,
    status: 6,
    createTime: '2023-12-01 14:30:00'
  },
  {
    id: 2,
    orderNo: 'DO202312010002',
    userName: '李四',
    pilotName: '王飞手',
    amount: 32.50,
    status: 5,
    createTime: '2023-12-01 14:25:00'
  },
  {
    id: 3,
    orderNo: 'DO202312010003',
    userName: '王五',
    pilotName: '赵飞手',
    amount: 18.90,
    status: 3,
    createTime: '2023-12-01 14:20:00'
  },
  {
    id: 4,
    orderNo: 'DO202312010004',
    userName: '赵六',
    pilotName: '钱飞手',
    amount: 45.60,
    status: 2,
    createTime: '2023-12-01 14:15:00'
  },
  {
    id: 5,
    orderNo: 'DO202312010005',
    userName: '孙七',
    pilotName: '孙飞手',
    amount: 28.30,
    status: 1,
    createTime: '2023-12-01 14:10:00'
  }
])

/**
 * 获取订单状态类型
 */
const getStatusType = (status: number): string => {
  const statusMap: Record<number, string> = {
    1: 'warning',  // 待支付
    2: 'info',     // 已支付
    3: 'primary',  // 已接单
    4: 'primary',  // 取货中
    5: 'primary',  // 配送中
    6: 'success',  // 已送达
    7: 'danger',   // 已取消
    8: 'danger',   // 配送失败
    9: 'info'      // 已退款
  }
  return statusMap[status] || 'info'
}

/**
 * 获取订单状态文本
 */
const getStatusText = (status: number): string => {
  const statusMap: Record<number, string> = {
    1: '待支付',
    2: '已支付',
    3: '已接单',
    4: '取货中',
    5: '配送中',
    6: '已送达',
    7: '已取消',
    8: '配送失败',
    9: '已退款'
  }
  return statusMap[status] || '未知'
}

/**
 * 查看订单详情
 */
const viewOrder = (order: any) => {
  ElMessage.info(`查看订单：${order.orderNo}`)
}

/**
 * 加载数据
 */
const loadData = async () => {
  try {
    // 这里可以调用API获取实际数据
    console.log('加载仪表盘数据')
  } catch (error) {
    console.error('加载数据失败:', error)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.dashboard {
  padding: 0;
}

.stats-row {
  margin-bottom: 20px;
}

.stats-card {
  height: 120px;
}

.stats-content {
  display: flex;
  align-items: center;
  height: 100%;
}

.stats-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
  font-size: 24px;
  color: white;
}

.stats-icon.orders {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stats-icon.users {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stats-icon.pilots {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stats-icon.revenue {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stats-info {
  flex: 1;
}

.stats-number {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.stats-label {
  font-size: 14px;
  color: #909399;
}

.charts-row {
  margin-bottom: 20px;
}

.chart-card {
  height: 400px;
}

.chart-container {
  height: 320px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart-placeholder {
  text-align: center;
  color: #909399;
}

.chart-placeholder .el-icon {
  font-size: 48px;
  margin-bottom: 10px;
}

.chart-placeholder p {
  margin: 5px 0;
  font-size: 16px;
}

.chart-desc {
  font-size: 12px !important;
  color: #c0c4cc !important;
}

.recent-orders-row {
  margin-bottom: 20px;
}

.recent-orders-card :deep(.el-card__header) {
  padding: 18px 20px;
  border-bottom: 1px solid #ebeef5;
}

.recent-orders-card :deep(.el-card__body) {
  padding: 0;
}
</style>