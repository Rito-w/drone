<template>
  <div class="order-list">
    <div class="page-header">
      <h2>订单列表</h2>
      <div class="header-actions">
        <el-button type="primary" @click="refreshData">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
        <el-button @click="exportData">
          <el-icon><Download /></el-icon>
          导出
        </el-button>
      </div>
    </div>

    <!-- 搜索筛选 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="订单号">
          <el-input 
            v-model="searchForm.orderNo" 
            placeholder="请输入订单号"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select 
            v-model="searchForm.status" 
            placeholder="请选择状态"
            clearable
            style="width: 150px"
          >
            <el-option label="全部" value="" />
            <el-option label="待支付" value="PENDING" />
            <el-option label="已支付" value="PAID" />
            <el-option label="配送中" value="DELIVERING" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item label="创建时间">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 240px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="resetSearch">
            <el-icon><RefreshLeft /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 订单表格 -->
    <el-card class="table-card" shadow="never">
      <el-table 
        :data="orderList" 
        v-loading="loading"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="userName" label="用户" width="120" />
        <el-table-column prop="pilotName" label="飞手" width="120" />
        <el-table-column label="配送信息" width="300">
          <template #default="{ row }">
            <div class="delivery-info">
              <div class="address-item">
                <el-icon><LocationFilled /></el-icon>
                <span>{{ row.pickupAddress }}</span>
              </div>
              <div class="address-item">
                <el-icon><Position /></el-icon>
                <span>{{ row.deliveryAddress }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="100">
          <template #default="{ row }">
            <span class="amount">¥{{ row.amount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              size="small" 
              @click="viewDetail(row)"
            >
              查看
            </el-button>
            <el-button 
              v-if="row.status === 'PENDING'"
              type="warning" 
              size="small" 
              @click="cancelOrder(row)"
            >
              取消
            </el-button>
            <el-button 
              v-if="row.status === 'DELIVERING'"
              type="success" 
              size="small" 
              @click="completeOrder(row)"
            >
              完成
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 订单详情弹窗 -->
    <el-dialog 
      v-model="detailVisible" 
      title="订单详情" 
      width="800px"
      destroy-on-close
    >
      <div v-if="currentOrder" class="order-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="getStatusType(currentOrder.status)">
              {{ getStatusText(currentOrder.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="用户">{{ currentOrder.userName }}</el-descriptions-item>
          <el-descriptions-item label="飞手">{{ currentOrder.pilotName || '未分配' }}</el-descriptions-item>
          <el-descriptions-item label="取货地址">{{ currentOrder.pickupAddress }}</el-descriptions-item>
          <el-descriptions-item label="配送地址">{{ currentOrder.deliveryAddress }}</el-descriptions-item>
          <el-descriptions-item label="订单金额">¥{{ currentOrder.amount }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentOrder.createTime }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">
            {{ currentOrder.remark || '无' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Refresh, 
  Download, 
  Search, 
  RefreshLeft,
  LocationFilled,
  Position
} from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const orderList = ref([])
const detailVisible = ref(false)
const currentOrder = ref(null)

// 搜索表单
const searchForm = reactive({
  orderNo: '',
  status: '',
  dateRange: []
})

// 分页
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 获取订单列表
const getOrderList = async () => {
  loading.value = true
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 模拟数据
    const mockData = Array.from({ length: 20 }, (_, index) => ({
      id: index + 1,
      orderNo: `DO${Date.now()}${index.toString().padStart(3, '0')}`,
      userName: `用户${index + 1}`,
      pilotName: index % 3 === 0 ? null : `飞手${index + 1}`,
      pickupAddress: `取货地址${index + 1}`,
      deliveryAddress: `配送地址${index + 1}`,
      amount: (Math.random() * 100 + 20).toFixed(2),
      status: ['PENDING', 'PAID', 'DELIVERING', 'COMPLETED', 'CANCELLED'][index % 5],
      createTime: new Date(Date.now() - Math.random() * 7 * 24 * 60 * 60 * 1000).toLocaleString(),
      remark: index % 3 === 0 ? `备注信息${index + 1}` : null
    }))
    
    orderList.value = mockData
    pagination.total = 100
  } catch (error) {
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  getOrderList()
}

// 重置搜索
const resetSearch = () => {
  Object.assign(searchForm, {
    orderNo: '',
    status: '',
    dateRange: []
  })
  handleSearch()
}

// 刷新数据
const refreshData = () => {
  getOrderList()
}

// 导出数据
const exportData = () => {
  ElMessage.success('导出功能开发中...')
}

// 分页处理
const handleSizeChange = (size: number) => {
  pagination.size = size
  getOrderList()
}

const handleCurrentChange = (page: number) => {
  pagination.page = page
  getOrderList()
}

// 查看详情
const viewDetail = (order: any) => {
  currentOrder.value = order
  detailVisible.value = true
}

// 取消订单
const cancelOrder = async (order: any) => {
  try {
    await ElMessageBox.confirm('确定要取消这个订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500))
    order.status = 'CANCELLED'
    ElMessage.success('订单已取消')
  } catch (error) {
    // 用户取消操作
  }
}

// 完成订单
const completeOrder = async (order: any) => {
  try {
    await ElMessageBox.confirm('确定要完成这个订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'success'
    })
    
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500))
    order.status = 'COMPLETED'
    ElMessage.success('订单已完成')
  } catch (error) {
    // 用户取消操作
  }
}

// 获取状态类型
const getStatusType = (status: string) => {
  const statusMap = {
    'PENDING': 'warning',
    'PAID': 'primary',
    'DELIVERING': 'info',
    'COMPLETED': 'success',
    'CANCELLED': 'danger'
  }
  return statusMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status: string) => {
  const statusMap = {
    'PENDING': '待支付',
    'PAID': '已支付',
    'DELIVERING': '配送中',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return statusMap[status] || '未知'
}

// 组件挂载时获取数据
onMounted(() => {
  getOrderList()
})
</script>

<style scoped>
.order-list {
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

.search-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.delivery-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.address-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
}

.address-item .el-icon {
  color: #909399;
}

.amount {
  font-weight: bold;
  color: #E6A23C;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.order-detail {
  padding: 20px 0;
}

@media (max-width: 768px) {
  .order-list {
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
</style>