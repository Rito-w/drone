<template>
  <div class="not-found">
    <div class="not-found-content">
      <div class="not-found-icon">
        <el-icon :size="120" color="#409EFF">
          <Warning />
        </el-icon>
      </div>
      <h1 class="not-found-title">404</h1>
      <p class="not-found-description">抱歉，您访问的页面不存在</p>
      <div class="not-found-actions">
        <el-button type="primary" @click="goHome">
          <el-icon><House /></el-icon>
          返回首页
        </el-button>
        <el-button @click="goBack">
          <el-icon><Back /></el-icon>
          返回上页
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { Warning, House, Back } from '@element-plus/icons-vue'

const router = useRouter()

const goHome = () => {
  router.push('/dashboard')
}

const goBack = () => {
  router.go(-1)
}
</script>

<style scoped>
.not-found {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.not-found-content {
  text-align: center;
  padding: 40px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  max-width: 500px;
  width: 90%;
}

.not-found-icon {
  margin-bottom: 20px;
}

.not-found-title {
  font-size: 72px;
  font-weight: bold;
  color: #409EFF;
  margin: 20px 0;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
}

.not-found-description {
  font-size: 18px;
  color: #666;
  margin-bottom: 30px;
  line-height: 1.6;
}

.not-found-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  flex-wrap: wrap;
}

.not-found-actions .el-button {
  padding: 12px 24px;
  font-size: 16px;
}

@media (max-width: 768px) {
  .not-found-title {
    font-size: 48px;
  }
  
  .not-found-description {
    font-size: 16px;
  }
  
  .not-found-actions {
    flex-direction: column;
    align-items: center;
  }
  
  .not-found-actions .el-button {
    width: 200px;
  }
}
</style>