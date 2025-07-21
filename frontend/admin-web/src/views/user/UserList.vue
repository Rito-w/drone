<template>
  <div class="user-list">
    <div class="page-header">
      <h2>用户列表</h2>
      <div class="header-actions">
        <el-button type="primary" @click="refreshData">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
        <el-button type="success" @click="showAddDialog">
          <el-icon><Plus /></el-icon>
          新增用户
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
        <el-form-item label="用户名">
          <el-input 
            v-model="searchForm.username" 
            placeholder="请输入用户名"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input 
            v-model="searchForm.nickname" 
            placeholder="请输入昵称"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="用户类型">
          <el-select 
            v-model="searchForm.userType" 
            placeholder="请选择类型"
            clearable
            style="width: 150px"
          >
            <el-option label="全部" value="" />
            <el-option label="普通用户" value="USER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select 
            v-model="searchForm.status" 
            placeholder="请选择状态"
            clearable
            style="width: 120px"
          >
            <el-option label="全部" value="" />
            <el-option label="正常" value="ACTIVE" />
            <el-option label="禁用" value="DISABLED" />
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

    <!-- 用户表格 -->
    <el-card class="table-card" shadow="never">
      <el-table 
        :data="userList" 
        v-loading="loading"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="nickname" label="昵称" width="120" />
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
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="userType" label="用户类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getUserTypeColor(row.userType)">
              {{ getUserTypeText(row.userType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'">
              {{ row.status === 'ACTIVE' ? '正常' : '禁用' }}
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
              type="warning" 
              size="small" 
              @click="editUser(row)"
            >
              编辑
            </el-button>
            <el-button 
              :type="row.status === 'ACTIVE' ? 'danger' : 'success'"
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

    <!-- 用户详情弹窗 -->
    <el-dialog 
      v-model="detailVisible" 
      title="用户详情" 
      width="600px"
      destroy-on-close
    >
      <div v-if="currentUser" class="user-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户ID">{{ currentUser.id }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ currentUser.username }}</el-descriptions-item>
          <el-descriptions-item label="昵称">{{ currentUser.nickname }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ currentUser.phone }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ currentUser.email }}</el-descriptions-item>
          <el-descriptions-item label="用户类型">
            <el-tag :type="getUserTypeColor(currentUser.userType)">
              {{ getUserTypeText(currentUser.userType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentUser.status === 'ACTIVE' ? 'success' : 'danger'">
              {{ currentUser.status === 'ACTIVE' ? '正常' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentUser.createTime }}</el-descriptions-item>
          <el-descriptions-item label="最后登录">{{ currentUser.lastLoginTime || '从未登录' }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 新增/编辑用户弹窗 -->
    <el-dialog 
      v-model="formVisible" 
      :title="isEdit ? '编辑用户' : '新增用户'" 
      width="600px"
      destroy-on-close
    >
      <el-form 
        :model="userForm" 
        :rules="formRules" 
        ref="formRef"
        label-width="100px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input 
            v-model="userForm.username" 
            placeholder="请输入用户名"
            :disabled="isEdit"
          />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input 
            v-model="userForm.nickname" 
            placeholder="请输入昵称"
          />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input 
            v-model="userForm.phone" 
            placeholder="请输入手机号"
          />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input 
            v-model="userForm.email" 
            placeholder="请输入邮箱"
          />
        </el-form-item>
        <el-form-item label="用户类型" prop="userType">
          <el-select v-model="userForm.userType" placeholder="请选择用户类型">
            <el-option label="普通用户" value="USER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码" prop="password">
          <el-input 
            v-model="userForm.password" 
            type="password"
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">
          {{ isEdit ? '更新' : '创建' }}
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
const userList = ref([])
const detailVisible = ref(false)
const formVisible = ref(false)
const currentUser = ref(null)
const isEdit = ref(false)
const formRef = ref()

// 搜索表单
const searchForm = reactive({
  username: '',
  nickname: '',
  userType: '',
  status: ''
})

// 用户表单
const userForm = reactive({
  id: null,
  username: '',
  nickname: '',
  phone: '',
  email: '',
  userType: 'USER',
  password: ''
})

// 表单验证规则
const formRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  userType: [
    { required: true, message: '请选择用户类型', trigger: 'change' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ]
}

// 分页
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

/**
 * 获取用户列表
 */
const getUserList = async () => {
  loading.value = true
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 模拟数据
    const mockData = Array.from({ length: 20 }, (_, index) => ({
      id: index + 1,
      username: `user${index + 1}`,
      nickname: `用户${index + 1}`,
      avatar: `https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png`,
      phone: `138${String(index + 1).padStart(8, '0')}`,
      email: `user${index + 1}@example.com`,
      userType: index % 5 === 0 ? 'ADMIN' : 'USER',
      status: index % 10 === 0 ? 'DISABLED' : 'ACTIVE',
      createTime: new Date(Date.now() - Math.random() * 30 * 24 * 60 * 60 * 1000).toLocaleString(),
      lastLoginTime: index % 3 === 0 ? null : new Date(Date.now() - Math.random() * 7 * 24 * 60 * 60 * 1000).toLocaleString()
    }))
    
    userList.value = mockData
    pagination.total = 100
  } catch (error) {
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 搜索用户
 */
const handleSearch = () => {
  pagination.page = 1
  getUserList()
}

/**
 * 重置搜索
 */
const resetSearch = () => {
  Object.assign(searchForm, {
    username: '',
    nickname: '',
    userType: '',
    status: ''
  })
  handleSearch()
}

/**
 * 刷新数据
 */
const refreshData = () => {
  getUserList()
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
  getUserList()
}

const handleCurrentChange = (page: number) => {
  pagination.page = page
  getUserList()
}

/**
 * 查看用户详情
 */
const viewDetail = (user: any) => {
  currentUser.value = user
  detailVisible.value = true
}

/**
 * 显示新增用户弹窗
 */
const showAddDialog = () => {
  isEdit.value = false
  Object.assign(userForm, {
    id: null,
    username: '',
    nickname: '',
    phone: '',
    email: '',
    userType: 'USER',
    password: ''
  })
  formVisible.value = true
}

/**
 * 编辑用户
 */
const editUser = (user: any) => {
  isEdit.value = true
  Object.assign(userForm, {
    id: user.id,
    username: user.username,
    nickname: user.nickname,
    phone: user.phone,
    email: user.email,
    userType: user.userType,
    password: ''
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
    
    ElMessage.success(isEdit.value ? '用户更新成功' : '用户创建成功')
    formVisible.value = false
    getUserList()
  } catch (error) {
    console.error('表单验证失败:', error)
  } finally {
    submitting.value = false
  }
}

/**
 * 切换用户状态
 */
const toggleStatus = async (user: any) => {
  const action = user.status === 'ACTIVE' ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确定要${action}用户 "${user.nickname}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500))
    user.status = user.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE'
    ElMessage.success(`用户${action}成功`)
  } catch (error) {
    // 用户取消操作
  }
}

/**
 * 获取用户类型颜色
 */
const getUserTypeColor = (type: string) => {
  return type === 'ADMIN' ? 'danger' : 'primary'
}

/**
 * 获取用户类型文本
 */
const getUserTypeText = (type: string) => {
  return type === 'ADMIN' ? '管理员' : '普通用户'
}

// 组件挂载时获取数据
onMounted(() => {
  getUserList()
})
</script>

<style scoped>
.user-list {
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

.user-detail {
  padding: 20px 0;
}

@media (max-width: 768px) {
  .user-list {
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