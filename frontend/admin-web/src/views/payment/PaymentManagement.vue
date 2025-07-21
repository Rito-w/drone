<template>
  <div class="payment-management">
    <div class="page-header">
      <h2>支付管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="refreshData">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
        <el-button @click="exportData">
          <el-icon><Download /></el-icon>
          导出
        </el-button>
        <el-button @click="showSettingsDialog">
          <el-icon><Setting /></el-icon>
          支付配置
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon success">
              <el-icon><Money /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">¥{{ formatMoney(stats.totalAmount) }}</div>
              <div class="stat-label">总交易金额</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon primary">
              <el-icon><CreditCard /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalTransactions }}</div>
              <div class="stat-label">总交易笔数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon warning">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.pendingRefunds }}</div>
              <div class="stat-label">待处理退款</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon danger">
              <el-icon><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.failedTransactions }}</div>
              <div class="stat-label">失败交易</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

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
        <el-form-item label="交易号">
          <el-input 
            v-model="searchForm.transactionNo" 
            placeholder="请输入交易号"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="支付方式">
          <el-select 
            v-model="searchForm.paymentMethod" 
            placeholder="请选择支付方式"
            clearable
            style="width: 150px"
          >
            <el-option label="全部" value="" />
            <el-option label="微信支付" value="WECHAT" />
            <el-option label="支付宝" value="ALIPAY" />
            <el-option label="银行卡" value="BANK_CARD" />
            <el-option label="余额支付" value="BALANCE" />
          </el-select>
        </el-form-item>
        <el-form-item label="交易状态">
          <el-select 
            v-model="searchForm.status" 
            placeholder="请选择状态"
            clearable
            style="width: 120px"
          >
            <el-option label="全部" value="" />
            <el-option label="成功" value="SUCCESS" />
            <el-option label="失败" value="FAILED" />
            <el-option label="处理中" value="PROCESSING" />
            <el-option label="已退款" value="REFUNDED" />
          </el-select>
        </el-form-item>
        <el-form-item label="交易时间">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            style="width: 300px"
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

    <!-- 交易记录表格 -->
    <el-card class="table-card" shadow="never">
      <el-table 
        :data="transactionList" 
        v-loading="loading"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="orderNo" label="订单号" width="150" />
        <el-table-column prop="transactionNo" label="交易号" width="180" />
        <el-table-column prop="amount" label="交易金额" width="120">
          <template #default="{ row }">
            <span class="amount">¥{{ formatMoney(row.amount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="paymentMethod" label="支付方式" width="120">
          <template #default="{ row }">
            <el-tag :type="getPaymentMethodColor(row.paymentMethod)">
              {{ getPaymentMethodText(row.paymentMethod) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="交易状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusColor(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="userName" label="用户" width="120" />
        <el-table-column prop="createTime" label="交易时间" width="160" />
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
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
              v-if="row.status === 'SUCCESS'"
              type="warning" 
              size="small" 
              @click="showRefundDialog(row)"
            >
              退款
            </el-button>
            <el-button 
              v-if="row.status === 'FAILED'"
              type="success" 
              size="small" 
              @click="retryPayment(row)"
            >
              重试
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

    <!-- 交易详情弹窗 -->
    <el-dialog 
      v-model="detailVisible" 
      title="交易详情" 
      width="600px"
      destroy-on-close
    >
      <div v-if="currentTransaction" class="transaction-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="交易ID">{{ currentTransaction.id }}</el-descriptions-item>
          <el-descriptions-item label="订单号">{{ currentTransaction.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="交易号">{{ currentTransaction.transactionNo }}</el-descriptions-item>
          <el-descriptions-item label="第三方交易号">{{ currentTransaction.thirdPartyNo || '无' }}</el-descriptions-item>
          <el-descriptions-item label="交易金额">¥{{ formatMoney(currentTransaction.amount) }}</el-descriptions-item>
          <el-descriptions-item label="支付方式">
            <el-tag :type="getPaymentMethodColor(currentTransaction.paymentMethod)">
              {{ getPaymentMethodText(currentTransaction.paymentMethod) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="交易状态">
            <el-tag :type="getStatusColor(currentTransaction.status)">
              {{ getStatusText(currentTransaction.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="用户">{{ currentTransaction.userName }}</el-descriptions-item>
          <el-descriptions-item label="交易时间">{{ currentTransaction.createTime }}</el-descriptions-item>
          <el-descriptions-item label="完成时间">{{ currentTransaction.finishTime || '未完成' }}</el-descriptions-item>
          <el-descriptions-item label="手续费">¥{{ formatMoney(currentTransaction.fee || 0) }}</el-descriptions-item>
          <el-descriptions-item label="备注" span="2">{{ currentTransaction.remark || '无' }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 退款弹窗 -->
    <el-dialog 
      v-model="refundVisible" 
      title="申请退款" 
      width="500px"
      destroy-on-close
    >
      <el-form 
        :model="refundForm" 
        :rules="refundRules" 
        ref="refundFormRef"
        label-width="100px"
      >
        <el-form-item label="退款金额" prop="amount">
          <el-input-number 
            v-model="refundForm.amount" 
            :precision="2"
            :min="0.01"
            :max="refundForm.maxAmount"
            style="width: 100%"
          />
          <div class="form-tip">最大可退款金额：¥{{ formatMoney(refundForm.maxAmount) }}</div>
        </el-form-item>
        <el-form-item label="退款原因" prop="reason">
          <el-select v-model="refundForm.reason" placeholder="请选择退款原因">
            <el-option label="用户申请退款" value="USER_REQUEST" />
            <el-option label="订单取消" value="ORDER_CANCELLED" />
            <el-option label="服务异常" value="SERVICE_ERROR" />
            <el-option label="系统错误" value="SYSTEM_ERROR" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="退款说明" prop="description">
          <el-input 
            v-model="refundForm.description" 
            type="textarea"
            :rows="4"
            placeholder="请输入退款说明"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="refundVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRefund" :loading="submitting">
          确认退款
        </el-button>
      </template>
    </el-dialog>

    <!-- 支付配置弹窗 -->
    <el-dialog 
      v-model="settingsVisible" 
      title="支付配置" 
      width="700px"
      destroy-on-close
    >
      <el-tabs v-model="activeTab">
        <el-tab-pane label="微信支付" name="wechat">
          <el-form label-width="120px">
            <el-form-item label="应用ID">
              <el-input v-model="paymentConfig.wechat.appId" placeholder="请输入微信应用ID" />
            </el-form-item>
            <el-form-item label="商户号">
              <el-input v-model="paymentConfig.wechat.mchId" placeholder="请输入微信商户号" />
            </el-form-item>
            <el-form-item label="API密钥">
              <el-input v-model="paymentConfig.wechat.apiKey" type="password" placeholder="请输入API密钥" />
            </el-form-item>
            <el-form-item label="启用状态">
              <el-switch v-model="paymentConfig.wechat.enabled" />
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="支付宝" name="alipay">
          <el-form label-width="120px">
            <el-form-item label="应用ID">
              <el-input v-model="paymentConfig.alipay.appId" placeholder="请输入支付宝应用ID" />
            </el-form-item>
            <el-form-item label="私钥">
              <el-input v-model="paymentConfig.alipay.privateKey" type="textarea" :rows="4" placeholder="请输入应用私钥" />
            </el-form-item>
            <el-form-item label="公钥">
              <el-input v-model="paymentConfig.alipay.publicKey" type="textarea" :rows="4" placeholder="请输入支付宝公钥" />
            </el-form-item>
            <el-form-item label="启用状态">
              <el-switch v-model="paymentConfig.alipay.enabled" />
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button @click="settingsVisible = false">取消</el-button>
        <el-button type="primary" @click="saveSettings" :loading="submitting">
          保存配置
        </el-button>
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
  Setting,
  Search,
  RefreshLeft,
  Money,
  CreditCard,
  Clock,
  Warning
} from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const transactionList = ref([])
const detailVisible = ref(false)
const refundVisible = ref(false)
const settingsVisible = ref(false)
const currentTransaction = ref(null)
const refundFormRef = ref()
const activeTab = ref('wechat')

// 统计数据
const stats = reactive({
  totalAmount: 0,
  totalTransactions: 0,
  pendingRefunds: 0,
  failedTransactions: 0
})

// 搜索表单
const searchForm = reactive({
  orderNo: '',
  transactionNo: '',
  paymentMethod: '',
  status: '',
  dateRange: []
})

// 退款表单
const refundForm = reactive({
  transactionId: null,
  amount: 0,
  maxAmount: 0,
  reason: '',
  description: ''
})

// 支付配置
const paymentConfig = reactive({
  wechat: {
    appId: '',
    mchId: '',
    apiKey: '',
    enabled: true
  },
  alipay: {
    appId: '',
    privateKey: '',
    publicKey: '',
    enabled: true
  }
})

// 退款表单验证规则
const refundRules = {
  amount: [
    { required: true, message: '请输入退款金额', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '退款金额必须大于0.01', trigger: 'blur' }
  ],
  reason: [
    { required: true, message: '请选择退款原因', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入退款说明', trigger: 'blur' }
  ]
}

// 分页
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

/**
 * 获取交易列表
 */
const getTransactionList = async () => {
  loading.value = true
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 模拟数据
    const mockData = Array.from({ length: 20 }, (_, index) => ({
      id: index + 1,
      orderNo: `ORD${String(index + 1).padStart(6, '0')}`,
      transactionNo: `TXN${Date.now()}${index}`,
      thirdPartyNo: `3RD${Date.now()}${index}`,
      amount: Math.floor(Math.random() * 10000) / 100 + 10,
      fee: Math.floor(Math.random() * 100) / 100,
      paymentMethod: ['WECHAT', 'ALIPAY', 'BANK_CARD', 'BALANCE'][index % 4],
      status: ['SUCCESS', 'FAILED', 'PROCESSING', 'REFUNDED'][index % 4],
      userName: `用户${index + 1}`,
      createTime: new Date(Date.now() - Math.random() * 30 * 24 * 60 * 60 * 1000).toLocaleString(),
      finishTime: index % 4 === 0 ? null : new Date(Date.now() - Math.random() * 29 * 24 * 60 * 60 * 1000).toLocaleString(),
      remark: index % 3 === 0 ? `备注信息${index + 1}` : ''
    }))
    
    transactionList.value = mockData
    pagination.total = 100
    
    // 更新统计数据
    stats.totalAmount = 1234567.89
    stats.totalTransactions = 5678
    stats.pendingRefunds = 12
    stats.failedTransactions = 34
  } catch (error) {
    ElMessage.error('获取交易列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 搜索交易
 */
const handleSearch = () => {
  pagination.page = 1
  getTransactionList()
}

/**
 * 重置搜索
 */
const resetSearch = () => {
  Object.assign(searchForm, {
    orderNo: '',
    transactionNo: '',
    paymentMethod: '',
    status: '',
    dateRange: []
  })
  handleSearch()
}

/**
 * 刷新数据
 */
const refreshData = () => {
  getTransactionList()
}

/**
 * 导出数据
 */
const exportData = () => {
  ElMessage.success('导出功能开发中...')
}

/**
 * 分页处理
 */
const handleSizeChange = (size: number) => {
  pagination.size = size
  getTransactionList()
}

const handleCurrentChange = (page: number) => {
  pagination.page = page
  getTransactionList()
}

/**
 * 查看交易详情
 */
const viewDetail = (transaction: any) => {
  currentTransaction.value = transaction
  detailVisible.value = true
}

/**
 * 显示退款弹窗
 */
const showRefundDialog = (transaction: any) => {
  refundForm.transactionId = transaction.id
  refundForm.amount = transaction.amount
  refundForm.maxAmount = transaction.amount
  refundForm.reason = ''
  refundForm.description = ''
  refundVisible.value = true
}

/**
 * 提交退款
 */
const submitRefund = async () => {
  try {
    await refundFormRef.value.validate()
    submitting.value = true
    
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    ElMessage.success('退款申请提交成功')
    refundVisible.value = false
    getTransactionList()
  } catch (error) {
    console.error('表单验证失败:', error)
  } finally {
    submitting.value = false
  }
}

/**
 * 重试支付
 */
const retryPayment = async (transaction: any) => {
  try {
    await ElMessageBox.confirm(`确定要重试交易 "${transaction.transactionNo}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    ElMessage.success('重试支付成功')
    getTransactionList()
  } catch (error) {
    // 用户取消操作
  }
}

/**
 * 显示支付配置弹窗
 */
const showSettingsDialog = () => {
  settingsVisible.value = true
}

/**
 * 保存支付配置
 */
const saveSettings = async () => {
  submitting.value = true
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    ElMessage.success('支付配置保存成功')
    settingsVisible.value = false
  } catch (error) {
    ElMessage.error('支付配置保存失败')
  } finally {
    submitting.value = false
  }
}

/**
 * 格式化金额
 */
const formatMoney = (amount: number) => {
  return amount.toFixed(2)
}

/**
 * 获取支付方式颜色
 */
const getPaymentMethodColor = (method: string) => {
  const colors = {
    WECHAT: 'success',
    ALIPAY: 'primary',
    BANK_CARD: 'warning',
    BALANCE: 'info'
  }
  return colors[method] || 'info'
}

/**
 * 获取支付方式文本
 */
const getPaymentMethodText = (method: string) => {
  const texts = {
    WECHAT: '微信支付',
    ALIPAY: '支付宝',
    BANK_CARD: '银行卡',
    BALANCE: '余额支付'
  }
  return texts[method] || '未知'
}

/**
 * 获取状态颜色
 */
const getStatusColor = (status: string) => {
  const colors = {
    SUCCESS: 'success',
    FAILED: 'danger',
    PROCESSING: 'warning',
    REFUNDED: 'info'
  }
  return colors[status] || 'info'
}

/**
 * 获取状态文本
 */
const getStatusText = (status: string) => {
  const texts = {
    SUCCESS: '成功',
    FAILED: '失败',
    PROCESSING: '处理中',
    REFUNDED: '已退款'
  }
  return texts[status] || '未知'
}

// 组件挂载时获取数据
onMounted(() => {
  getTransactionList()
})
</script>

<style scoped>
.payment-management {
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

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  border: none;
}

.stat-content {
  display: flex;
  align-items: center;
  padding: 10px 0;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  font-size: 24px;
  color: white;
}

.stat-icon.success {
  background: linear-gradient(135deg, #67c23a, #85ce61);
}

.stat-icon.primary {
  background: linear-gradient(135deg, #409eff, #66b1ff);
}

.stat-icon.warning {
  background: linear-gradient(135deg, #e6a23c, #ebb563);
}

.stat-icon.danger {
  background: linear-gradient(135deg, #f56c6c, #f78989);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.search-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.amount {
  font-weight: bold;
  color: #67c23a;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.transaction-detail {
  padding: 20px 0;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

@media (max-width: 1200px) {
  .stats-row .el-col {
    margin-bottom: 16px;
  }
}

@media (max-width: 768px) {
  .payment-management {
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
  
  .stat-content {
    flex-direction: column;
    text-align: center;
  }
  
  .stat-icon {
    margin-right: 0;
    margin-bottom: 12px;
  }
}
</style>