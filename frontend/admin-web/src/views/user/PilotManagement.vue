<template>
  <div class="pilot-management">
    <div class="page-header">
      <h2>飞手管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="refreshData">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
        <el-button type="success" @click="showAddDialog">
          <el-icon><Plus /></el-icon>
          新增飞手
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
        <el-form-item label="飞手姓名">
          <el-input 
            v-model="searchForm.name" 
            placeholder="请输入飞手姓名"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input 
            v-model="searchForm.phone" 
            placeholder="请输入手机号"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="认证状态">
          <el-select 
            v-model="searchForm.certStatus" 
            placeholder="请选择认证状态"
            clearable
            style="width: 150px"
          >
            <el-option label="全部" value="" />
            <el-option label="已认证" value="CERTIFIED" />
            <el-option label="待认证" value="PENDING" />
            <el-option label="认证失败" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item label="在线状态">
          <el-select 
            v-model="searchForm.onlineStatus" 
            placeholder="请选择在线状态"
            clearable
            style="width: 120px"
          >
            <el-option label="全部" value="" />
            <el-option label="在线" value="ONLINE" />
            <el-option label="离线" value="OFFLINE" />
            <el-option label="忙碌" value="BUSY" />
          </el-select>
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

    <!-- 飞手表格 -->
    <el-card class="table-card" shadow="never">
      <el-table 
        :data="pilotList" 
        v-loading="loading"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="飞手姓名" width="120" />
        <el-table-column prop="avatar" label="头像" width="80">
          <template #default="{ row }">
            <el-avatar 
              :src="row.avatar" 
              :size="40"
              :icon="UserFilled"
            />
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="idCard" label="身份证号" width="180" />
        <el-table-column prop="certStatus" label="认证状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getCertStatusColor(row.certStatus)">
              {{ getCertStatusText(row.certStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="onlineStatus" label="在线状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getOnlineStatusColor(row.onlineStatus)">
              {{ getOnlineStatusText(row.onlineStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="rating" label="评分" width="100">
          <template #default="{ row }">
            <el-rate 
              v-model="row.rating" 
              disabled 
              show-score 
              text-color="#ff9900"
              score-template="{value}"
            />
          </template>
        </el-table-column>
        <el-table-column prop="totalOrders" label="总订单数" width="100" />
        <el-table-column prop="completedOrders" label="完成订单" width="100" />
        <el-table-column prop="createTime" label="注册时间" width="160" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              size="small" 
              @click="viewDetail(row)"
            >
              查看
            </el-button>
            <el-button 
              type="warning" 
              size="small" 
              @click="editPilot(row)"
            >
              编辑
            </el-button>
            <el-button 
              v-if="row.certStatus === 'PENDING'"
              type="success" 
              size="small" 
              @click="approveCert(row)"
            >
              认证
            </el-button>
            <el-button 
              type="danger" 
              size="small" 
              @click="toggleStatus(row)"
            >
              {{ row.status === 'ACTIVE' ? '禁用' : '启用' }}
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

    <!-- 飞手详情弹窗 -->
    <el-dialog 
      v-model="detailVisible" 
      title="飞手详情" 
      width="800px"
      destroy-on-close
    >
      <div v-if="currentPilot" class="pilot-detail">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="基本信息" name="basic">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="飞手ID">{{ currentPilot.id }}</el-descriptions-item>
              <el-descriptions-item label="姓名">{{ currentPilot.name }}</el-descriptions-item>
              <el-descriptions-item label="手机号">{{ currentPilot.phone }}</el-descriptions-item>
              <el-descriptions-item label="身份证号">{{ currentPilot.idCard }}</el-descriptions-item>
              <el-descriptions-item label="认证状态">
                <el-tag :type="getCertStatusColor(currentPilot.certStatus)">
                  {{ getCertStatusText(currentPilot.certStatus) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="在线状态">
                <el-tag :type="getOnlineStatusColor(currentPilot.onlineStatus)">
                  {{ getOnlineStatusText(currentPilot.onlineStatus) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="评分">
                <el-rate 
                  v-model="currentPilot.rating" 
                  disabled 
                  show-score 
                  text-color="#ff9900"
                />
              </el-descriptions-item>
              <el-descriptions-item label="注册时间">{{ currentPilot.createTime }}</el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>
          <el-tab-pane label="认证信息" name="cert">
            <div class="cert-info">
              <el-row :gutter="20">
                <el-col :span="12">
                  <div class="cert-item">
                    <h4>身份证正面</h4>
                    <el-image 
                      :src="currentPilot.idCardFront" 
                      style="width: 200px; height: 120px"
                      fit="cover"
                      :preview-src-list="[currentPilot.idCardFront]"
                    />
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="cert-item">
                    <h4>身份证反面</h4>
                    <el-image 
                      :src="currentPilot.idCardBack" 
                      style="width: 200px; height: 120px"
                      fit="cover"
                      :preview-src-list="[currentPilot.idCardBack]"
                    />
                  </div>
                </el-col>
              </el-row>
              <el-row :gutter="20" style="margin-top: 20px">
                <el-col :span="12">
                  <div class="cert-item">
                    <h4>飞行执照</h4>
                    <el-image 
                      :src="currentPilot.pilotLicense" 
                      style="width: 200px; height: 120px"
                      fit="cover"
                      :preview-src-list="[currentPilot.pilotLicense]"
                    />
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="cert-item">
                    <h4>健康证明</h4>
                    <el-image 
                      :src="currentPilot.healthCert" 
                      style="width: 200px; height: 120px"
                      fit="cover"
                      :preview-src-list="[currentPilot.healthCert]"
                    />
                  </div>
                </el-col>
              </el-row>
            </div>
          </el-tab-pane>
          <el-tab-pane label="订单统计" name="orders">
            <el-row :gutter="20">
              <el-col :span="6">
                <el-statistic title="总订单数" :value="currentPilot.totalOrders" />
              </el-col>
              <el-col :span="6">
                <el-statistic title="完成订单" :value="currentPilot.completedOrders" />
              </el-col>
              <el-col :span="6">
                <el-statistic title="取消订单" :value="currentPilot.cancelledOrders" />
              </el-col>
              <el-col :span="6">
                <el-statistic title="完成率" :value="currentPilot.completionRate" suffix="%" />
              </el-col>
            </el-row>
          </el-tab-pane>
        </el-tabs>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 新增/编辑飞手弹窗 -->
    <el-dialog 
      v-model="formVisible" 
      :title="isEdit ? '编辑飞手' : '新增飞手'" 
      width="600px"
      destroy-on-close
    >
      <el-form 
        :model="pilotForm" 
        :rules="formRules" 
        ref="formRef"
        label-width="100px"
      >
        <el-form-item label="姓名" prop="name">
          <el-input 
            v-model="pilotForm.name" 
            placeholder="请输入飞手姓名"
          />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input 
            v-model="pilotForm.phone" 
            placeholder="请输入手机号"
          />
        </el-form-item>
        <el-form-item label="身份证号" prop="idCard">
          <el-input 
            v-model="pilotForm.idCard" 
            placeholder="请输入身份证号"
          />
        </el-form-item>
        <el-form-item label="认证状态" prop="certStatus">
          <el-select v-model="pilotForm.certStatus" placeholder="请选择认证状态">
            <el-option label="已认证" value="CERTIFIED" />
            <el-option label="待认证" value="PENDING" />
            <el-option label="认证失败" value="REJECTED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">
          {{ isEdit ? '更新' : '创建' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 认证审核弹窗 -->
    <el-dialog 
      v-model="certVisible" 
      title="认证审核" 
      width="600px"
      destroy-on-close
    >
      <el-form 
        :model="certForm" 
        ref="certFormRef"
        label-width="100px"
      >
        <el-form-item label="审核结果" prop="result">
          <el-radio-group v-model="certForm.result">
            <el-radio label="CERTIFIED">通过认证</el-radio>
            <el-radio label="REJECTED">拒绝认证</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核备注" prop="remark">
          <el-input 
            v-model="certForm.remark" 
            type="textarea"
            :rows="4"
            placeholder="请输入审核备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="certVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCert" :loading="submitting">
          提交审核
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
  Plus,
  Download, 
  Search, 
  RefreshLeft,
  UserFilled
} from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const pilotList = ref([])
const detailVisible = ref(false)
const formVisible = ref(false)
const certVisible = ref(false)
const currentPilot = ref(null)
const isEdit = ref(false)
const formRef = ref()
const certFormRef = ref()
const activeTab = ref('basic')

// 搜索表单
const searchForm = reactive({
  name: '',
  phone: '',
  certStatus: '',
  onlineStatus: ''
})

// 飞手表单
const pilotForm = reactive({
  id: null,
  name: '',
  phone: '',
  idCard: '',
  certStatus: 'PENDING'
})

// 认证表单
const certForm = reactive({
  pilotId: null,
  result: 'CERTIFIED',
  remark: ''
})

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入飞手姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /^[1-9]\d{5}(18|19|20)\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\d{3}[0-9Xx]$/, message: '请输入正确的身份证号', trigger: 'blur' }
  ],
  certStatus: [
    { required: true, message: '请选择认证状态', trigger: 'change' }
  ]
}

// 分页
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

/**
 * 获取飞手列表
 */
const getPilotList = async () => {
  loading.value = true
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 模拟数据
    const mockData = Array.from({ length: 20 }, (_, index) => ({
      id: index + 1,
      name: `飞手${index + 1}`,
      avatar: `https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png`,
      phone: `138${String(index + 1).padStart(8, '0')}`,
      idCard: `110101199001010${String(index + 1).padStart(3, '0')}`,
      certStatus: ['CERTIFIED', 'PENDING', 'REJECTED'][index % 3],
      onlineStatus: ['ONLINE', 'OFFLINE', 'BUSY'][index % 3],
      rating: 3 + Math.random() * 2,
      totalOrders: Math.floor(Math.random() * 100) + 10,
      completedOrders: Math.floor(Math.random() * 80) + 5,
      cancelledOrders: Math.floor(Math.random() * 10),
      completionRate: Math.floor(Math.random() * 30) + 70,
      status: index % 10 === 0 ? 'DISABLED' : 'ACTIVE',
      createTime: new Date(Date.now() - Math.random() * 30 * 24 * 60 * 60 * 1000).toLocaleString(),
      idCardFront: 'https://cube.elemecdn.com/6/94/4d3ea53c084bad6931a56d5158a48jpeg.jpeg',
      idCardBack: 'https://cube.elemecdn.com/6/94/4d3ea53c084bad6931a56d5158a48jpeg.jpeg',
      pilotLicense: 'https://cube.elemecdn.com/6/94/4d3ea53c084bad6931a56d5158a48jpeg.jpeg',
      healthCert: 'https://cube.elemecdn.com/6/94/4d3ea53c084bad6931a56d5158a48jpeg.jpeg'
    }))
    
    pilotList.value = mockData
    pagination.total = 100
  } catch (error) {
    ElMessage.error('获取飞手列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 搜索飞手
 */
const handleSearch = () => {
  pagination.page = 1
  getPilotList()
}

/**
 * 重置搜索
 */
const resetSearch = () => {
  Object.assign(searchForm, {
    name: '',
    phone: '',
    certStatus: '',
    onlineStatus: ''
  })
  handleSearch()
}

/**
 * 刷新数据
 */
const refreshData = () => {
  getPilotList()
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
  getPilotList()
}

const handleCurrentChange = (page: number) => {
  pagination.page = page
  getPilotList()
}

/**
 * 查看飞手详情
 */
const viewDetail = (pilot: any) => {
  currentPilot.value = pilot
  activeTab.value = 'basic'
  detailVisible.value = true
}

/**
 * 显示新增飞手弹窗
 */
const showAddDialog = () => {
  isEdit.value = false
  Object.assign(pilotForm, {
    id: null,
    name: '',
    phone: '',
    idCard: '',
    certStatus: 'PENDING'
  })
  formVisible.value = true
}

/**
 * 编辑飞手
 */
const editPilot = (pilot: any) => {
  isEdit.value = true
  Object.assign(pilotForm, {
    id: pilot.id,
    name: pilot.name,
    phone: pilot.phone,
    idCard: pilot.idCard,
    certStatus: pilot.certStatus
  })
  formVisible.value = true
}

/**
 * 提交表单
 */
const submitForm = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true
    
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    ElMessage.success(isEdit.value ? '飞手更新成功' : '飞手创建成功')
    formVisible.value = false
    getPilotList()
  } catch (error) {
    console.error('表单验证失败:', error)
  } finally {
    submitting.value = false
  }
}

/**
 * 认证审核
 */
const approveCert = (pilot: any) => {
  certForm.pilotId = pilot.id
  certForm.result = 'CERTIFIED'
  certForm.remark = ''
  certVisible.value = true
}

/**
 * 提交认证审核
 */
const submitCert = async () => {
  submitting.value = true
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    ElMessage.success('认证审核提交成功')
    certVisible.value = false
    getPilotList()
  } catch (error) {
    ElMessage.error('认证审核失败')
  } finally {
    submitting.value = false
  }
}

/**
 * 切换飞手状态
 */
const toggleStatus = async (pilot: any) => {
  const action = pilot.status === 'ACTIVE' ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确定要${action}飞手 "${pilot.name}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500))
    pilot.status = pilot.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE'
    ElMessage.success(`飞手${action}成功`)
  } catch (error) {
    // 用户取消操作
  }
}

/**
 * 获取认证状态颜色
 */
const getCertStatusColor = (status: string) => {
  const colors = {
    CERTIFIED: 'success',
    PENDING: 'warning',
    REJECTED: 'danger'
  }
  return colors[status] || 'info'
}

/**
 * 获取认证状态文本
 */
const getCertStatusText = (status: string) => {
  const texts = {
    CERTIFIED: '已认证',
    PENDING: '待认证',
    REJECTED: '认证失败'
  }
  return texts[status] || '未知'
}

/**
 * 获取在线状态颜色
 */
const getOnlineStatusColor = (status: string) => {
  const colors = {
    ONLINE: 'success',
    OFFLINE: 'info',
    BUSY: 'warning'
  }
  return colors[status] || 'info'
}

/**
 * 获取在线状态文本
 */
const getOnlineStatusText = (status: string) => {
  const texts = {
    ONLINE: '在线',
    OFFLINE: '离线',
    BUSY: '忙碌'
  }
  return texts[status] || '未知'
}

// 组件挂载时获取数据
onMounted(() => {
  getPilotList()
})
</script>

<style scoped>
.pilot-management {
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

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.pilot-detail {
  padding: 20px 0;
}

.cert-info {
  padding: 20px 0;
}

.cert-item h4 {
  margin: 0 0 10px 0;
  color: #303133;
}

@media (max-width: 768px) {
  .pilot-management {
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