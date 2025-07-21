<template>
  <div class="not-found">
    <div class="error-container">
      <div class="error-code">404</div>
      <div class="error-message">页面未找到</div>
      <div class="error-description">
        抱歉，您访问的页面不存在或已被删除
      </div>
      <div class="error-actions">
        <el-button type="primary" @click="goHome">
          <el-icon><HomeFilled /></el-icon>
          返回首页
        </el-button>
        <el-button @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回上页
        </el-button>
      </div>
    </div>
    <div class="error-illustration">
      <svg viewBox="0 0 404 300" xmlns="http://www.w3.org/2000/svg">
        <defs>
          <linearGradient id="gradient" x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%" style="stop-color:#409eff;stop-opacity:1" />
            <stop offset="100%" style="stop-color:#67c23a;stop-opacity:1" />
          </linearGradient>
        </defs>
        <circle cx="202" cy="150" r="120" fill="url(#gradient)" opacity="0.1"/>
        <text x="202" y="170" text-anchor="middle" font-size="80" font-weight="bold" fill="url(#gradient)">404</text>
        <text x="202" y="200" text-anchor="middle" font-size="16" fill="#909399">页面走丢了</text>
      </svg>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { HomeFilled, ArrowLeft } from '@element-plus/icons-vue'

const router = useRouter()

/**
 * 返回首页
 */
const goHome = () => {
  router.push('/')
}

/**
 * 返回上一页
 */
const goBack = () => {
  router.back()
}
</script>

<style scoped>
.not-found {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.error-container {
  text-align: center;
  margin-bottom: 40px;
}

.error-code {
  font-size: 120px;
  font-weight: bold;
  color: #409eff;
  line-height: 1;
  margin-bottom: 20px;
  text-shadow: 0 4px 8px rgba(64, 158, 255, 0.3);
}

.error-message {
  font-size: 32px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
}

.error-description {
  font-size: 16px;
  color: #606266;
  margin-bottom: 40px;
  line-height: 1.6;
}

.error-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  flex-wrap: wrap;
}

.error-illustration {
  max-width: 400px;
  width: 100%;
  opacity: 0.8;
}

.error-illustration svg {
  width: 100%;
  height: auto;
}

@media (max-width: 768px) {
  .error-code {
    font-size: 80px;
  }
  
  .error-message {
    font-size: 24px;
  }
  
  .error-description {
    font-size: 14px;
  }
  
  .error-actions {
    flex-direction: column;
    align-items: center;
  }
  
  .error-actions .el-button {
    width: 200px;
  }
}

@media (max-width: 480px) {
  .not-found {
    padding: 10px;
  }
  
  .error-code {
    font-size: 60px;
  }
  
  .error-message {
    font-size: 20px;
  }
}
</style>