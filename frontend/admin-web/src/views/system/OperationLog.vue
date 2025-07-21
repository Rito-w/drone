<template>
  <div class="operation-log">
    <div class="page-header">
      <h2>操作日志</h2>
      <div class="header-actions">
        <el-button @click="exportLogs" :loading="exporting">
          <el-icon><Download /></el-icon>
          导出日志
        </el-button>
        <el-button @click="clearLogs" type="danger">
          <el-icon><Delete /></el-icon>
          清空日志
        </el-button>
      </div>
    </div>

    <!-- 搜索筛选 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" :inline="true">
        <el-form-item label="操作人员">
          <el-input 
            v-model="searchForm.operator" 
            placeholder="请输入操作人员"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="操作模块">
          <el-select 
            v-model="searchForm.module" 
            placeholder="请选择操作模块"
            clearable
            style="width: 150px"
          >
            <el-option label="用户管理" value="user" />
            <el-option label="订单管理" value="order" />
            <el-option label="飞手管理" value="pilot" />
            <el-option label="支付管理" value="payment" />
            <el-option label="系统管理" value="system" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作类型">
          <el-select 
            v-model="searchForm.action" 
            placeholder="请选择操作类型"
            clearable
            style="width: 150px"
          >
            <el-option label="新增" value="CREATE" />
            <el-option label="修改" value="UPDATE" />
            <el-option label="删除" value="DELETE" />
            <el-option label="查询" value="SELECT" />
            <el-option label="登录" value="LOGIN" />
            <el-option label="登出" value="LOGOUT" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作结果">
          <el-select 
            v-model="searchForm.status" 
            placeholder="请选择操作结果"
            clearable
            style="width: 120px"
          >
            <el-option label="成功" value="SUCCESS" />
            <el-option label="失败" value="FAILED" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作时间">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 350px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchLogs">
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

    <!-- 统计信息 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalLogs }}</div>
            <div class="stat-label">总日志数</div>
          </div>
          <el-icon class="stat-icon"><Document /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-value">{{ stats.todayLogs }}</div>
            <div class="stat-label">今日日志</div>
          </div>
          <el-icon class="stat-icon"><Calendar /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-value">{{ stats.successRate }}%</div>
            <div class="stat-label">成功率</div>
          </div>
          <el-icon class="stat-icon"><CircleCheck /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-value">{{ stats.activeUsers }}</div>
            <div class="stat-label">活跃用户</div>
          </div>
          <el-icon class="stat-icon"><User /></el-icon>
        </el-card>
      </el-col>
    </el-row>

    <!-- 日志表格 -->
    <el-card class="table-card" shadow="never">
      <el-table 
        :data="logList" 
        v-loading="loading"
        stripe
        style="width: 100%"
        @sort-change="handleSortChange"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="operator" label="操作人员" width="120" />
        <el-table-column prop="module" label="操作模块" width="100">
          <template #default="{ row }">
            <el-tag :type="getModuleTagType(row.module)">
              {{ getModuleName(row.module) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="action" label="操作类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getActionTagType(row.action)">
              {{ getActionName(row.action) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="操作描述" min-width="200" />
        <el-table-column prop="status" label="操作结果" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'SUCCESS' ? 'success' : 'danger'">
              {{ row.status === 'SUCCESS' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ip" label="IP地址" width="140" />
        <el-table-column prop="userAgent" label="用户代理" width="200" show-overflow-tooltip />
        <el-table-column prop="operateTime" label="操作时间" width="180" sortable="custom" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              size="small" 
              @click="viewLogDetail(row)"
              link
            >
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 日志详情弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="日志详情"
      width="800px"
      :before-close="handleDetailClose"
    >
      <div v-if="currentLog" class="log-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="日志ID">{{ currentLog.id }}</el-descriptions-item>
          <el-descriptions-item label="操作人员">{{ currentLog.operator }}</el-descriptions-item>
          <el-descriptions-item label="操作模块">
            <el-tag :type="getModuleTagType(currentLog.module)">
              {{ getModuleName(currentLog.module) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="操作类型">
            <el-tag :type="getActionTagType(currentLog.action)">
              {{ getActionName(currentLog.action) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="操作结果" :span="2">
            <el-tag :type="currentLog.status === 'SUCCESS' ? 'success' : 'danger'">
              {{ currentLog.status === 'SUCCESS' ? '成功' : '失败' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="操作描述" :span="2">{{ currentLog.description }}</el-descriptions-item>
          <el-descriptions-item label="IP地址">{{ currentLog.ip }}</el-descriptions-item>
          <el-descriptions-item label="操作时间">{{ currentLog.operateTime }}</el-descriptions-item>
          <el-descriptions-item label="用户代理" :span="2">{{ currentLog.userAgent }}</el-descriptions-item>
          <el-descriptions-item label="请求参数" :span="2">
            <pre class="json-content">{{ formatJson(currentLog.requestParams) }}</pre>
          </el-descriptions-item>
          <el-descriptions-item label="响应结果" :span="2">
            <pre class="json-content">{{ formatJson(currentLog.responseData) }}</pre>
          </el-descriptions-item>
          <el-descriptions-item label="错误信息" :span="2" v-if="currentLog.errorMessage">
            <div class="error-message">{{ currentLog.errorMessage }}</div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Download,
  Delete,
  Search,
  RefreshLeft,
  Document,
  Calendar,
  CircleCheck,
  User
} from '@element-plus/icons-vue'

// 接口定义
interface OperationLog {
  id: number
  operator: string
  module: string
  action: string
  description: string
  status: 'SUCCESS' | 'FAILED'
  ip: string
  userAgent: string
  operateTime: string
  requestParams?: any
  responseData?: any
  errorMessage?: string
}

interface SearchForm {
  operator: string
  module: string
  action: string
  status: string
  dateRange: string[]
}

// 响应式数据
const loading = ref(false)
const exporting = ref(false)
const detailDialogVisible = ref(false)
const currentLog = ref<OperationLog | null>(null)

// 搜索表单
const searchForm = reactive<SearchForm>({
  operator: '',
  module: '',
  action: '',
  status: '',
  dateRange: []
})

// 分页信息
const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

// 统计信息
const stats = reactive({
  totalLogs: 15420,
  todayLogs: 234,
  successRate: 98.5,
  activeUsers: 45
})

// 日志列表
const logList = ref<OperationLog[]>([])

/**
 * 获取模块标签类型
 */
const getModuleTagType = (module: string) => {
  const typeMap: Record<string, string> = {
    user: 'primary',
    order: 'success',
    pilot: 'warning',
    payment: 'danger',
    system: 'info'
  }
  return typeMap[module] || 'info'
}

/**
 * 获取模块名称
 */
const getModuleName = (module: string) => {
  const nameMap: Record<string, string> = {
    user: '用户管理',
    order: '订单管理',
    pilot: '飞手管理',
    payment: '支付管理',
    system: '系统管理'
  }
  return nameMap[module] || module
}

/**
 * 获取操作类型标签类型
 */
const getActionTagType = (action: string) => {
  const typeMap: Record<string, string> = {
    CREATE: 'success',
    UPDATE: 'warning',
    DELETE: 'danger',
    SELECT: 'info',
    LOGIN: 'primary',
    LOGOUT: 'info'
  }
  return typeMap[action] || 'info'
}

/**
 * 获取操作类型名称
 */
const getActionName = (action: string) => {
  const nameMap: Record<string, string> = {
    CREATE: '新增',
    UPDATE: '修改',
    DELETE: '删除',
    SELECT: '查询',
    LOGIN: '登录',
    LOGOUT: '登出'
  }
  return nameMap[action] || action
}

/**
 * 搜索日志
 */
const searchLogs = () => {
  pagination.currentPage = 1
  getLogList()
}

/**
 * 重置搜索
 */
const resetSearch = () => {
  Object.assign(searchForm, {
    operator: '',
    module: '',
    action: '',
    status: '',
    dateRange: []
  })
  searchLogs()
}

/**
 * 获取日志列表
 */
const getLogList = async () => {
  loading.value = true
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500))
    
    // 模拟数据
    const mockData: OperationLog[] = [
      {
        id: 1,
        operator: '管理员',
        module: 'user',
        action: 'CREATE',
        description: '创建新用户：张三',
        status: 'SUCCESS',
        ip: '192.168.1.100',
        userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
        operateTime: '2024-01-15 10:30:25',
        requestParams: { name: '张三', phone: '13800138000' },
        responseData: { id: 123, status: 'success' }
      },
      {
        id: 2,
        operator: '张三',
        module: 'order',
        action: 'UPDATE',
        description: '修改订单状态：已完成',
        status: 'SUCCESS',
        ip: '192.168.1.101',
        userAgent: 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36',
        operateTime: '2024-01-15 10:25:18'
      },
      {
        id: 3,
        operator: '李四',
        module: 'payment',
        action: 'DELETE',
        description: '删除支付记录',
        status: 'FAILED',
        ip: '192.168.1.102',
        userAgent: 'Mozilla/5.0 (iPhone; CPU iPhone OS 14_7_1 like Mac OS X)',
        operateTime: '2024-01-15 10:20:45',
        errorMessage: '权限不足，无法删除支付记录'
      }
    ]
    
    logList.value = mockData
    pagination.total = 150
    
  } catch (error) {
    ElMessage.error('获取日志列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 查看日志详情
 */
const viewLogDetail = (log: OperationLog) => {
  currentLog.value = log
  detailDialogVisible.value = true
}

/**
 * 关闭详情弹窗
 */
const handleDetailClose = () => {
  detailDialogVisible.value = false
  currentLog.value = null
}

/**
 * 格式化JSON
 */
const formatJson = (data: any) => {
  if (!data) return ''
  return JSON.stringify(data, null, 2)
}

/**
 * 导出日志
 */
const exportLogs = async () => {
  exporting.value = true
  try {
    // 模拟导出
    await new Promise(resolve => setTimeout(resolve, 2000))
    ElMessage.success('日志导出成功')
  } catch (error) {
    ElMessage.error('日志导出失败')
  } finally {
    exporting.value = false
  }
}

/**
 * 清空日志
 */
const clearLogs = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有日志吗？此操作不可恢复！', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 模拟清空操作
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    logList.value = []
    pagination.total = 0
    ElMessage.success('日志清空成功')
  } catch (error) {
    // 用户取消操作
  }
}

/**
 * 处理排序变化
 */
const handleSortChange = ({ prop, order }: any) => {
  // 处理排序逻辑
  getLogList()
}

/**
 * 处理页面大小变化
 */
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  getLogList()
}

/**
 * 处理当前页变化
 */
const handleCurrentChange = (page: number) => {
  pagination.currentPage = page
  getLogList()
}

// 组件挂载时获取数据
onMounted(() => {
  getLogList()
})
</script>

<style scoped>
.operation-log {
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

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  position: relative;
  overflow: hidden;
}

.stat-content {
  padding: 20px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.stat-icon {
  position: absolute;
  right: 20px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 40px;
  color: #409eff;
  opacity: 0.3;
}

.table-card {
  margin-bottom: 20px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.log-detail {
  max-height: 600px;
  overflow-y: auto;
}

.json-content {
  background-color: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  font-size: 12px;
  line-height: 1.4;
  max-height: 200px;
  overflow-y: auto;
  white-space: pre-wrap;
  word-break: break-all;
}

.error-message {
  color: #f56c6c;
  background-color: #fef0f0;
  padding: 12px;
  border-radius: 4px;
  border-left: 4px solid #f56c6c;
}

@media (max-width: 1200px) {
  .stats-row .el-col {
    margin-bottom: 16px;
  }
}

@media (max-width: 768px) {
  .operation-log {
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
  
  .search-card .el-form {
    flex-direction: column;
  }
  
  .search-card .el-form-item {
    margin-right: 0;
    margin-bottom: 16px;
  }
}
</style>