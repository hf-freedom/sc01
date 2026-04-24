<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
              <el-icon><UserFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.userCount }}</div>
              <div class="stat-label">用户总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
              <el-icon><OfficeBuilding /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.orgCount }}</div>
              <div class="stat-label">组织机构</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
              <el-icon><UserFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.roleCount }}</div>
              <div class="stat-label">角色数量</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);">
              <el-icon><Grid /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.appCount }}</div>
              <div class="stat-label">应用数量</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>欢迎使用</span>
          </template>
          <div class="welcome-content">
            <h2>权限管理系统</h2>
            <p>当前用户：{{ userStore.userInfo?.realName || userStore.userInfo?.username }}</p>
            <p>所属机构：{{ userStore.userInfo?.organization?.name || '-' }}</p>
            <p>拥有角色：{{ userStore.userInfo?.roles?.map(r => r.name).join('、') || '-' }}</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>系统说明</span>
          </template>
          <div class="help-content">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="用户管理">管理系统用户，包括增删改查、绑定角色</el-descriptions-item>
              <el-descriptions-item label="角色管理">管理角色，绑定资源和用户</el-descriptions-item>
              <el-descriptions-item label="组织机构">管理组织机构树</el-descriptions-item>
              <el-descriptions-item label="资源管理">管理菜单、页面、按钮等资源</el-descriptions-item>
              <el-descriptions-item label="应用管理">管理应用，与组织机构绑定</el-descriptions-item>
            </el-descriptions>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '../stores/user'
import request from '../utils/request'
import { UserFilled, OfficeBuilding, Grid } from '@element-plus/icons-vue'

const userStore = useUserStore()

const stats = ref({
  userCount: 0,
  orgCount: 0,
  roleCount: 0,
  appCount: 0
})

const loadStats = async () => {
  try {
    const [userRes, orgRes, roleRes, appRes] = await Promise.all([
      request.get('/user/list'),
      request.get('/organization/tree'),
      request.get('/role/list'),
      request.get('/application/list')
    ])
    
    stats.value.userCount = userRes.data?.length || 0
    stats.value.orgCount = orgRes.data?.length || 0
    stats.value.roleCount = roleRes.data?.length || 0
    stats.value.appCount = appRes.data?.length || 0
  } catch (e) {
    console.error('加载统计数据失败', e)
  }
}

onMounted(() => {
  loadStats()
})
</script>

<style scoped>
.dashboard {
  padding: 0;
}

.stat-card {
  cursor: pointer;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #fff;
}

.stat-info {
  margin-left: 20px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #999;
  margin-top: 5px;
}

.welcome-content h2 {
  color: #409EFF;
  margin-bottom: 15px;
}

.welcome-content p {
  color: #666;
  margin: 8px 0;
}

.help-content {
  padding: 10px 0;
}
</style>
