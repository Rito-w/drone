<template>
  <div class="system-config">
    <div class="page-header">
      <h2>系统配置</h2>
      <div class="header-actions">
        <el-button type="primary" @click="saveAllConfig" :loading="saving">
          <el-icon><Check /></el-icon>
          保存所有配置
        </el-button>
        <el-button @click="resetConfig">
          <el-icon><RefreshLeft /></el-icon>
          重置配置
        </el-button>
      </div>
    </div>

    <el-row :gutter="20">
      <!-- 左侧配置菜单 -->
      <el-col :span="6">
        <el-card class="config-menu" shadow="never">
          <el-menu 
            v-model:default-active="activeConfig"
            @select="handleConfigSelect"
          >
            <el-menu-item index="basic">
              <el-icon><Setting /></el-icon>
              <span>基础配置</span>
            </el-menu-item>
            <el-menu-item index="delivery">
              <el-icon><Truck /></el-icon>
              <span>配送配置</span>
            </el-menu-item>
            <el-menu-item index="payment">
              <el-icon><CreditCard /></el-icon>
              <span>支付配置</span>
            </el-menu-item>
            <el-menu-item index="notification">
              <el-icon><Bell /></el-icon>
              <span>通知配置</span>
            </el-menu-item>
            <el-menu-item index="security">
              <el-icon><Lock /></el-icon>
              <span>安全配置</span>
            </el-menu-item>
            <el-menu-item index="map">
              <el-icon><Location /></el-icon>
              <span>地图配置</span>
            </el-menu-item>
          </el-menu>
        </el-card>
      </el-col>

      <!-- 右侧配置内容 -->
      <el-col :span="18">
        <el-card class="config-content" shadow="never">
          <!-- 基础配置 -->
          <div v-show="activeConfig === 'basic'" class="config-section">
            <h3>基础配置</h3>
            <el-form :model="configs.basic" label-width="150px">
              <el-form-item label="系统名称">
                <el-input v-model="configs.basic.systemName" placeholder="请输入系统名称" />
              </el-form-item>
              <el-form-item label="系统版本">
                <el-input v-model="configs.basic.systemVersion" placeholder="请输入系统版本" />
              </el-form-item>
              <el-form-item label="公司名称">
                <el-input v-model="configs.basic.companyName" placeholder="请输入公司名称" />
              </el-form-item>
              <el-form-item label="联系电话">
                <el-input v-model="configs.basic.contactPhone" placeholder="请输入联系电话" />
              </el-form-item>
              <el-form-item label="联系邮箱">
                <el-input v-model="configs.basic.contactEmail" placeholder="请输入联系邮箱" />
              </el-form-item>
              <el-form-item label="系统Logo">
                <el-upload
                  class="logo-uploader"
                  action="#"
                  :show-file-list="false"
                  :before-upload="beforeLogoUpload"
                >
                  <img v-if="configs.basic.logoUrl" :src="configs.basic.logoUrl" class="logo" />
                  <el-icon v-else class="logo-uploader-icon"><Plus /></el-icon>
                </el-upload>
              </el-form-item>
              <el-form-item label="维护模式">
                <el-switch 
                  v-model="configs.basic.maintenanceMode"
                  active-text="开启"
                  inactive-text="关闭"
                />
              </el-form-item>
              <el-form-item label="维护提示">
                <el-input 
                  v-model="configs.basic.maintenanceMessage" 
                  type="textarea"
                  :rows="3"
                  placeholder="请输入维护提示信息"
                />
              </el-form-item>
            </el-form>
          </div>

          <!-- 配送配置 -->
          <div v-show="activeConfig === 'delivery'" class="config-section">
            <h3>配送配置</h3>
            <el-form :model="configs.delivery" label-width="150px">
              <el-form-item label="配送范围(km)">
                <el-input-number 
                  v-model="configs.delivery.maxDistance" 
                  :min="1"
                  :max="100"
                  :precision="1"
                />
              </el-form-item>
              <el-form-item label="起送金额(元)">
                <el-input-number 
                  v-model="configs.delivery.minOrderAmount" 
                  :min="0"
                  :precision="2"
                />
              </el-form-item>
              <el-form-item label="配送费(元)">
                <el-input-number 
                  v-model="configs.delivery.deliveryFee" 
                  :min="0"
                  :precision="2"
                />
              </el-form-item>
              <el-form-item label="免配送费金额(元)">
                <el-input-number 
                  v-model="configs.delivery.freeDeliveryAmount" 
                  :min="0"
                  :precision="2"
                />
              </el-form-item>
              <el-form-item label="配送时间段">
                <el-time-picker
                  v-model="configs.delivery.deliveryTimeRange"
                  is-range
                  range-separator="至"
                  start-placeholder="开始时间"
                  end-placeholder="结束时间"
                  format="HH:mm"
                  value-format="HH:mm"
                />
              </el-form-item>
              <el-form-item label="预计配送时间(分钟)">
                <el-input-number 
                  v-model="configs.delivery.estimatedTime" 
                  :min="10"
                  :max="120"
                />
              </el-form-item>
              <el-form-item label="自动接单">
                <el-switch 
                  v-model="configs.delivery.autoAccept"
                  active-text="开启"
                  inactive-text="关闭"
                />
              </el-form-item>
              <el-form-item label="订单超时时间(分钟)">
                <el-input-number 
                  v-model="configs.delivery.orderTimeout" 
                  :min="5"
                  :max="60"
                />
              </el-form-item>
            </el-form>
          </div>

          <!-- 支付配置 -->
          <div v-show="activeConfig === 'payment'" class="config-section">
            <h3>支付配置</h3>
            <el-form :model="configs.payment" label-width="150px">
              <el-form-item label="支付超时时间(分钟)">
                <el-input-number 
                  v-model="configs.payment.paymentTimeout" 
                  :min="5"
                  :max="60"
                />
              </el-form-item>
              <el-form-item label="支持的支付方式">
                <el-checkbox-group v-model="configs.payment.supportedMethods">
                  <el-checkbox label="WECHAT">微信支付</el-checkbox>
                  <el-checkbox label="ALIPAY">支付宝</el-checkbox>
                  <el-checkbox label="BANK_CARD">银行卡</el-checkbox>
                  <el-checkbox label="BALANCE">余额支付</el-checkbox>
                </el-checkbox-group>
              </el-form-item>
              <el-form-item label="自动退款">
                <el-switch 
                  v-model="configs.payment.autoRefund"
                  active-text="开启"
                  inactive-text="关闭"
                />
              </el-form-item>
              <el-form-item label="退款处理时间(小时)">
                <el-input-number 
                  v-model="configs.payment.refundProcessTime" 
                  :min="1"
                  :max="72"
                />
              </el-form-item>
            </el-form>
          </div>

          <!-- 通知配置 -->
          <div v-show="activeConfig === 'notification'" class="config-section">
            <h3>通知配置</h3>
            <el-form :model="configs.notification" label-width="150px">
              <el-form-item label="短信通知">
                <el-switch 
                  v-model="configs.notification.smsEnabled"
                  active-text="开启"
                  inactive-text="关闭"
                />
              </el-form-item>
              <el-form-item label="邮件通知">
                <el-switch 
                  v-model="configs.notification.emailEnabled"
                  active-text="开启"
                  inactive-text="关闭"
                />
              </el-form-item>
              <el-form-item label="推送通知">
                <el-switch 
                  v-model="configs.notification.pushEnabled"
                  active-text="开启"
                  inactive-text="关闭"
                />
              </el-form-item>
              <el-form-item label="通知模板">
                <el-select v-model="configs.notification.template" placeholder="请选择通知模板">
                  <el-option label="默认模板" value="default" />
                  <el-option label="简洁模板" value="simple" />
                  <el-option label="详细模板" value="detailed" />
                </el-select>
              </el-form-item>
              <el-form-item label="SMTP服务器">
                <el-input v-model="configs.notification.smtpServer" placeholder="请输入SMTP服务器地址" />
              </el-form-item>
              <el-form-item label="SMTP端口">
                <el-input-number v-model="configs.notification.smtpPort" :min="1" :max="65535" />
              </el-form-item>
              <el-form-item label="发送邮箱">
                <el-input v-model="configs.notification.senderEmail" placeholder="请输入发送邮箱" />
              </el-form-item>
              <el-form-item label="邮箱密码">
                <el-input 
                  v-model="configs.notification.emailPassword" 
                  type="password"
                  placeholder="请输入邮箱密码"
                  show-password
                />
              </el-form-item>
            </el-form>
          </div>

          <!-- 安全配置 -->
          <div v-show="activeConfig === 'security'" class="config-section">
            <h3>安全配置</h3>
            <el-form :model="configs.security" label-width="150px">
              <el-form-item label="密码最小长度">
                <el-input-number 
                  v-model="configs.security.minPasswordLength" 
                  :min="6"
                  :max="20"
                />
              </el-form-item>
              <el-form-item label="密码复杂度">
                <el-checkbox-group v-model="configs.security.passwordComplexity">
                  <el-checkbox label="uppercase">包含大写字母</el-checkbox>
                  <el-checkbox label="lowercase">包含小写字母</el-checkbox>
                  <el-checkbox label="number">包含数字</el-checkbox>
                  <el-checkbox label="special">包含特殊字符</el-checkbox>
                </el-checkbox-group>
              </el-form-item>
              <el-form-item label="登录失败锁定">
                <el-switch 
                  v-model="configs.security.loginLockEnabled"
                  active-text="开启"
                  inactive-text="关闭"
                />
              </el-form-item>
              <el-form-item label="最大失败次数">
                <el-input-number 
                  v-model="configs.security.maxLoginAttempts" 
                  :min="3"
                  :max="10"
                />
              </el-form-item>
              <el-form-item label="锁定时间(分钟)">
                <el-input-number 
                  v-model="configs.security.lockDuration" 
                  :min="5"
                  :max="60"
                />
              </el-form-item>
              <el-form-item label="会话超时(分钟)">
                <el-input-number 
                  v-model="configs.security.sessionTimeout" 
                  :min="30"
                  :max="480"
                />
              </el-form-item>
              <el-form-item label="双因子认证">
                <el-switch 
                  v-model="configs.security.twoFactorEnabled"
                  active-text="开启"
                  inactive-text="关闭"
                />
              </el-form-item>
            </el-form>
          </div>

          <!-- 地图配置 -->
          <div v-show="activeConfig === 'map'" class="config-section">
            <h3>地图配置</h3>
            <el-form :model="configs.map" label-width="150px">
              <el-form-item label="地图服务商">
                <el-select v-model="configs.map.provider" placeholder="请选择地图服务商">
                  <el-option label="高德地图" value="amap" />
                  <el-option label="百度地图" value="baidu" />
                  <el-option label="腾讯地图" value="tencent" />
                  <el-option label="谷歌地图" value="google" />
                </el-select>
              </el-form-item>
              <el-form-item label="API密钥">
                <el-input 
                  v-model="configs.map.apiKey" 
                  type="password"
                  placeholder="请输入地图API密钥"
                  show-password
                />
              </el-form-item>
              <el-form-item label="默认中心点">
                <el-row :gutter="10">
                  <el-col :span="12">
                    <el-input 
                      v-model="configs.map.defaultCenter.lat" 
                      placeholder="纬度"
                    />
                  </el-col>
                  <el-col :span="12">
                    <el-input 
                      v-model="configs.map.defaultCenter.lng" 
                      placeholder="经度"
                    />
                  </el-col>
                </el-row>
              </el-form-item>
              <el-form-item label="默认缩放级别">
                <el-input-number 
                  v-model="configs.map.defaultZoom" 
                  :min="1"
                  :max="20"
                />
              </el-form-item>
              <el-form-item label="实时更新间隔(秒)">
                <el-input-number 
                  v-model="configs.map.updateInterval" 
                  :min="5"
                  :max="60"
                />
              </el-form-item>
              <el-form-item label="显示交通状况">
                <el-switch 
                  v-model="configs.map.showTraffic"
                  active-text="开启"
                  inactive-text="关闭"
                />
              </el-form-item>
              <el-form-item label="显示禁飞区域">
                <el-switch 
                  v-model="configs.map.showRestrictedAreas"
                  active-text="开启"
                  inactive-text="关闭"
                />
              </el-form-item>
            </el-form>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Check,
  RefreshLeft,
  Setting,
  Truck,
  CreditCard,
  Bell,
  Lock,
  Location,
  Plus
} from '@element-plus/icons-vue'

// 响应式数据
const saving = ref(false)
const activeConfig = ref('basic')

// 配置数据
const configs = reactive({
  basic: {
    systemName: '无人机配送平台',
    systemVersion: '1.0.0',
    companyName: '低空经济科技有限公司',
    contactPhone: '400-123-4567',
    contactEmail: 'contact@drone-delivery.com',
    logoUrl: '',
    maintenanceMode: false,
    maintenanceMessage: '系统正在维护中，请稍后再试...'
  },
  delivery: {
    maxDistance: 10.0,
    minOrderAmount: 20.00,
    deliveryFee: 5.00,
    freeDeliveryAmount: 50.00,
    deliveryTimeRange: ['08:00', '22:00'],
    estimatedTime: 30,
    autoAccept: false,
    orderTimeout: 15
  },
  payment: {
    paymentTimeout: 15,
    supportedMethods: ['WECHAT', 'ALIPAY'],
    autoRefund: true,
    refundProcessTime: 24
  },
  notification: {
    smsEnabled: true,
    emailEnabled: true,
    pushEnabled: true,
    template: 'default',
    smtpServer: 'smtp.qq.com',
    smtpPort: 587,
    senderEmail: '',
    emailPassword: ''
  },
  security: {
    minPasswordLength: 8,
    passwordComplexity: ['lowercase', 'number'],
    loginLockEnabled: true,
    maxLoginAttempts: 5,
    lockDuration: 30,
    sessionTimeout: 120,
    twoFactorEnabled: false
  },
  map: {
    provider: 'amap',
    apiKey: '',
    defaultCenter: {
      lat: '39.908823',
      lng: '116.397470'
    },
    defaultZoom: 12,
    updateInterval: 30,
    showTraffic: true,
    showRestrictedAreas: true
  }
})

/**
 * 处理配置选择
 */
const handleConfigSelect = (key: string) => {
  activeConfig.value = key
}

/**
 * 保存所有配置
 */
const saveAllConfig = async () => {
  saving.value = true
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    ElMessage.success('配置保存成功')
  } catch (error) {
    ElMessage.error('配置保存失败')
  } finally {
    saving.value = false
  }
}

/**
 * 重置配置
 */
const resetConfig = async () => {
  try {
    await ElMessageBox.confirm('确定要重置所有配置吗？此操作不可恢复！', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 重置为默认值
    loadDefaultConfig()
    ElMessage.success('配置已重置')
  } catch (error) {
    // 用户取消操作
  }
}

/**
 * 加载默认配置
 */
const loadDefaultConfig = () => {
  // 这里可以从API加载默认配置
  ElMessage.info('已加载默认配置')
}

/**
 * Logo上传前验证
 */
const beforeLogoUpload = (file: File) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG) {
    ElMessage.error('Logo只能是 JPG/PNG 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('Logo大小不能超过 2MB!')
    return false
  }
  
  // 模拟上传成功
  configs.basic.logoUrl = URL.createObjectURL(file)
  return false // 阻止自动上传
}

/**
 * 获取系统配置
 */
const getSystemConfig = async () => {
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500))
    
    // 这里可以从API加载配置数据
    ElMessage.success('配置加载成功')
  } catch (error) {
    ElMessage.error('配置加载失败')
  }
}

// 组件挂载时获取配置
onMounted(() => {
  getSystemConfig()
})
</script>

<style scoped>
.system-config {
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

.config-menu {
  height: calc(100vh - 200px);
  overflow-y: auto;
}

.config-content {
  height: calc(100vh - 200px);
  overflow-y: auto;
}

.config-section {
  padding: 20px;
}

.config-section h3 {
  margin: 0 0 24px 0;
  color: #303133;
  font-size: 18px;
  font-weight: 600;
  border-bottom: 2px solid #409eff;
  padding-bottom: 8px;
}

.logo-uploader .logo {
  width: 100px;
  height: 100px;
  display: block;
  object-fit: cover;
  border-radius: 4px;
}

.logo-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: 0.2s;
}

.logo-uploader .el-upload:hover {
  border-color: #409eff;
}

.logo-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  text-align: center;
  line-height: 100px;
}

@media (max-width: 1200px) {
  .config-menu,
  .config-content {
    height: auto;
    min-height: 400px;
  }
}

@media (max-width: 768px) {
  .system-config {
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
  
  .config-section {
    padding: 10px;
  }
}
</style>