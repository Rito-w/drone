<template>
  <div class="map-monitor">
    <div class="page-header">
      <h2>地图监控</h2>
      <div class="header-actions">
        <el-button type="primary" @click="refreshData">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
        <el-button @click="toggleFullscreen">
          <el-icon><FullScreen /></el-icon>
          全屏
        </el-button>
      </div>
    </div>

    <el-row :gutter="20">
      <!-- 左侧控制面板 -->
      <el-col :span="6">
        <el-card class="control-panel" shadow="never">
          <template #header>
            <div class="card-header">
              <span>控制面板</span>
            </div>
          </template>

          <!-- 实时统计 -->
          <div class="stats-section">
            <h4>实时统计</h4>
            <el-row :gutter="10">
              <el-col :span="12">
                <div class="stat-item">
                  <div class="stat-value">{{ stats.onlinePilots }}</div>
                  <div class="stat-label">在线飞手</div>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="stat-item">
                  <div class="stat-value">{{ stats.activeDrones }}</div>
                  <div class="stat-label">活跃无人机</div>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="stat-item">
                  <div class="stat-value">{{ stats.deliveryingOrders }}</div>
                  <div class="stat-label">配送中订单</div>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="stat-item">
                  <div class="stat-value">{{ stats.completedToday }}</div>
                  <div class="stat-label">今日完成</div>
                </div>
              </el-col>
            </el-row>
          </div>

          <!-- 图层控制 -->
          <div class="layer-section">
            <h4>图层控制</h4>
            <el-checkbox-group v-model="mapLayers">
              <el-checkbox label="pilots">飞手位置</el-checkbox>
              <el-checkbox label="drones">无人机位置</el-checkbox>
              <el-checkbox label="orders">订单路径</el-checkbox>
              <el-checkbox label="weather">天气信息</el-checkbox>
              <el-checkbox label="traffic">交通状况</el-checkbox>
              <el-checkbox label="restricted">禁飞区域</el-checkbox>
            </el-checkbox-group>
          </div>

          <!-- 筛选条件 -->
          <div class="filter-section">
            <h4>筛选条件</h4>
            <el-form :model="filterForm" size="small">
              <el-form-item label="飞手状态">
                <el-select v-model="filterForm.pilotStatus" placeholder="选择状态">
                  <el-option label="全部" value="" />
                  <el-option label="在线" value="ONLINE" />
                  <el-option label="忙碌" value="BUSY" />
                  <el-option label="离线" value="OFFLINE" />
                </el-select>
              </el-form-item>
              <el-form-item label="订单状态">
                <el-select v-model="filterForm.orderStatus" placeholder="选择状态">
                  <el-option label="全部" value="" />
                  <el-option label="待接单" value="PENDING" />
                  <el-option label="配送中" value="DELIVERING" />
                  <el-option label="已完成" value="COMPLETED" />
                </el-select>
              </el-form-item>
              <el-form-item label="时间范围">
                <el-select v-model="filterForm.timeRange" placeholder="选择时间">
                  <el-option label="最近1小时" value="1h" />
                  <el-option label="最近6小时" value="6h" />
                  <el-option label="今天" value="today" />
                  <el-option label="最近3天" value="3d" />
                </el-select>
              </el-form-item>
            </el-form>
          </div>

          <!-- 告警信息 -->
          <div class="alert-section">
            <h4>告警信息</h4>
            <div class="alert-list">
              <div 
                v-for="alert in alerts" 
                :key="alert.id"
                class="alert-item"
                :class="alert.level"
              >
                <div class="alert-time">{{ alert.time }}</div>
                <div class="alert-message">{{ alert.message }}</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧地图区域 -->
      <el-col :span="18">
        <el-card class="map-card" shadow="never">
          <div id="map-container" class="map-container">
            <!-- 地图容器 -->
            <div class="map-placeholder">
              <el-icon size="64"><Location /></el-icon>
              <p>地图加载中...</p>
              <p class="map-info">这里将显示实时地图，包含飞手位置、无人机轨迹、订单路径等信息</p>
            </div>
          </div>

          <!-- 地图工具栏 -->
          <div class="map-toolbar">
            <el-button-group>
              <el-button @click="zoomIn">
                <el-icon><ZoomIn /></el-icon>
              </el-button>
              <el-button @click="zoomOut">
                <el-icon><ZoomOut /></el-icon>
              </el-button>
              <el-button @click="resetView">
                <el-icon><Aim /></el-icon>
              </el-button>
            </el-button-group>
            
            <el-button-group style="margin-left: 10px">
              <el-button @click="toggleHeatmap">热力图</el-button>
              <el-button @click="toggle3D">3D视图</el-button>
              <el-button @click="toggleSatellite">卫星图</el-button>
            </el-button-group>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 详情弹窗 -->
    <el-dialog 
      v-model="detailVisible" 
      :title="detailTitle" 
      width="600px"
      destroy-on-close
    >
      <div v-if="selectedItem" class="detail-content">
        <!-- 飞手详情 -->
        <div v-if="selectedItem.type === 'pilot'" class="pilot-detail">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="飞手ID">{{ selectedItem.id }}</el-descriptions-item>
            <el-descriptions-item label="姓名">{{ selectedItem.name }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getPilotStatusColor(selectedItem.status)">
                {{ getPilotStatusText(selectedItem.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="当前位置">{{ selectedItem.location }}</el-descriptions-item>
            <el-descriptions-item label="今日订单">{{ selectedItem.todayOrders }}</el-descriptions-item>
            <el-descriptions-item label="在线时长">{{ selectedItem.onlineTime }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 无人机详情 -->
        <div v-if="selectedItem.type === 'drone'" class="drone-detail">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="无人机ID">{{ selectedItem.id }}</el-descriptions-item>
            <el-descriptions-item label="型号">{{ selectedItem.model }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getDroneStatusColor(selectedItem.status)">
                {{ getDroneStatusText(selectedItem.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="电池电量">{{ selectedItem.battery }}%</el-descriptions-item>
            <el-descriptions-item label="飞行高度">{{ selectedItem.altitude }}m</el-descriptions-item>
            <el-descriptions-item label="飞行速度">{{ selectedItem.speed }}km/h</el-descriptions-item>
            <el-descriptions-item label="当前订单">{{ selectedItem.currentOrder || '无' }}</el-descriptions-item>
            <el-descriptions-item label="剩余航程">{{ selectedItem.remainingRange }}km</el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 订单详情 -->
        <div v-if="selectedItem.type === 'order'" class="order-detail">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="订单号">{{ selectedItem.orderNo }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getOrderStatusColor(selectedItem.status)">
                {{ getOrderStatusText(selectedItem.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="起点">{{ selectedItem.startAddress }}</el-descriptions-item>
            <el-descriptions-item label="终点">{{ selectedItem.endAddress }}</el-descriptions-item>
            <el-descriptions-item label="配送员">{{ selectedItem.pilotName }}</el-descriptions-item>
            <el-descriptions-item label="预计送达">{{ selectedItem.estimatedTime }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Refresh,
  FullScreen,
  Location,
  ZoomIn,
  ZoomOut,
  Aim
} from '@element-plus/icons-vue'

// 响应式数据
const stats = reactive({
  onlinePilots: 0,
  activeDrones: 0,
  deliveryingOrders: 0,
  completedToday: 0
})

const mapLayers = ref(['pilots', 'drones', 'orders'])

const filterForm = reactive({
  pilotStatus: '',
  orderStatus: '',
  timeRange: '6h'
})

const alerts = ref([])
const detailVisible = ref(false)
const detailTitle = ref('')
const selectedItem = ref(null)

let refreshTimer = null

/**
 * 获取实时数据
 */
const getRealTimeData = async () => {
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500))
    
    // 更新统计数据
    stats.onlinePilots = Math.floor(Math.random() * 50) + 20
    stats.activeDrones = Math.floor(Math.random() * 30) + 15
    stats.deliveryingOrders = Math.floor(Math.random() * 40) + 10
    stats.completedToday = Math.floor(Math.random() * 200) + 100
    
    // 更新告警信息
    const newAlerts = [
      {
        id: Date.now(),
        time: new Date().toLocaleTimeString(),
        message: '无人机DRN001电量低于20%',
        level: 'warning'
      },
      {
        id: Date.now() + 1,
        time: new Date().toLocaleTimeString(),
        message: '飞手PLT005进入禁飞区域',
        level: 'danger'
      },
      {
        id: Date.now() + 2,
        time: new Date().toLocaleTimeString(),
        message: '订单ORD123配送超时',
        level: 'warning'
      }
    ]
    
    alerts.value = newAlerts.slice(0, 5)
  } catch (error) {
    console.error('获取实时数据失败:', error)
  }
}

/**
 * 刷新数据
 */
const refreshData = () => {
  getRealTimeData()
  ElMessage.success('数据已刷新')
}

/**
 * 全屏切换
 */
const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
  } else {
    document.exitFullscreen()
  }
}

/**
 * 地图操作
 */
const zoomIn = () => {
  ElMessage.info('放大地图')
}

const zoomOut = () => {
  ElMessage.info('缩小地图')
}

const resetView = () => {
  ElMessage.info('重置视图')
}

const toggleHeatmap = () => {
  ElMessage.info('切换热力图')
}

const toggle3D = () => {
  ElMessage.info('切换3D视图')
}

const toggleSatellite = () => {
  ElMessage.info('切换卫星图')
}

/**
 * 显示详情
 */
const showDetail = (item: any) => {
  selectedItem.value = item
  detailTitle.value = getDetailTitle(item.type)
  detailVisible.value = true
}

const getDetailTitle = (type: string) => {
  const titles = {
    pilot: '飞手详情',
    drone: '无人机详情',
    order: '订单详情'
  }
  return titles[type] || '详情'
}

/**
 * 状态颜色和文本
 */
const getPilotStatusColor = (status: string) => {
  const colors = {
    ONLINE: 'success',
    BUSY: 'warning',
    OFFLINE: 'info'
  }
  return colors[status] || 'info'
}

const getPilotStatusText = (status: string) => {
  const texts = {
    ONLINE: '在线',
    BUSY: '忙碌',
    OFFLINE: '离线'
  }
  return texts[status] || '未知'
}

const getDroneStatusColor = (status: string) => {
  const colors = {
    FLYING: 'success',
    CHARGING: 'warning',
    MAINTENANCE: 'danger',
    IDLE: 'info'
  }
  return colors[status] || 'info'
}

const getDroneStatusText = (status: string) => {
  const texts = {
    FLYING: '飞行中',
    CHARGING: '充电中',
    MAINTENANCE: '维护中',
    IDLE: '空闲'
  }
  return texts[status] || '未知'
}

const getOrderStatusColor = (status: string) => {
  const colors = {
    PENDING: 'warning',
    DELIVERING: 'primary',
    COMPLETED: 'success',
    CANCELLED: 'danger'
  }
  return colors[status] || 'info'
}

const getOrderStatusText = (status: string) => {
  const texts = {
    PENDING: '待配送',
    DELIVERING: '配送中',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  return texts[status] || '未知'
}

// 组件挂载时初始化
onMounted(() => {
  getRealTimeData()
  // 设置定时刷新
  refreshTimer = setInterval(getRealTimeData, 30000) // 30秒刷新一次
})

// 组件卸载时清理定时器
onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})
</script>

<style scoped>
.map-monitor {
  padding: 20px;
  height: calc(100vh - 120px);
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

.control-panel {
  height: calc(100vh - 200px);
  overflow-y: auto;
}

.card-header {
  font-weight: bold;
  color: #303133;
}

.stats-section,
.layer-section,
.filter-section,
.alert-section {
  margin-bottom: 24px;
}

.stats-section h4,
.layer-section h4,
.filter-section h4,
.alert-section h4 {
  margin: 0 0 12px 0;
  color: #606266;
  font-size: 14px;
  font-weight: 600;
}

.stat-item {
  text-align: center;
  padding: 12px 8px;
  background: #f5f7fa;
  border-radius: 4px;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 20px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  color: #909399;
}

.layer-section .el-checkbox {
  display: block;
  margin-bottom: 8px;
}

.alert-list {
  max-height: 200px;
  overflow-y: auto;
}

.alert-item {
  padding: 8px;
  margin-bottom: 8px;
  border-radius: 4px;
  border-left: 3px solid;
}

.alert-item.warning {
  background: #fdf6ec;
  border-left-color: #e6a23c;
}

.alert-item.danger {
  background: #fef0f0;
  border-left-color: #f56c6c;
}

.alert-time {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.alert-message {
  font-size: 13px;
  color: #303133;
}

.map-card {
  height: calc(100vh - 200px);
  position: relative;
}

.map-container {
  height: calc(100% - 60px);
  position: relative;
  background: #f5f7fa;
  border-radius: 4px;
  overflow: hidden;
}

.map-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #909399;
}

.map-placeholder p {
  margin: 8px 0;
}

.map-info {
  font-size: 12px;
  text-align: center;
  max-width: 300px;
  line-height: 1.5;
}

.map-toolbar {
  position: absolute;
  bottom: 20px;
  left: 20px;
  z-index: 1000;
}

.detail-content {
  padding: 20px 0;
}

@media (max-width: 1200px) {
  .map-monitor {
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
}

@media (max-width: 768px) {
  .control-panel,
  .map-card {
    height: auto;
    min-height: 400px;
  }
}
</style>