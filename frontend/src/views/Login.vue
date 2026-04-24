<template>
  <div class="login-container">
    <div class="login-box">
      <h2 class="login-title">权限管理系统</h2>
      <div class="login-form">
        <div class="form-item">
          <input
            v-model="username"
            type="text"
            placeholder="请输入用户名"
            class="form-input"
          />
        </div>
        <div class="form-item">
          <input
            v-model="password"
            type="password"
            placeholder="请输入密码"
            class="form-input"
            @keyup.enter="handleLogin"
          />
        </div>
        <div class="form-item">
          <button
            type="button"
            class="login-btn"
            @click="handleLogin"
            :disabled="loading"
          >
            <span v-if="!loading">登 录</span>
            <span v-else>加载中...</span>
          </button>
        </div>
      </div>
      <div class="login-tips">
        <p>管理员账号：admin / admin123</p>
        <p>普通用户：user / user123</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const username = ref('')
const password = ref('')

const handleLogin = async () => {
  if (!username.value || !password.value) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  
  try {
    loading.value = true
    const res = await userStore.login(username.value, password.value)
    if (res.code === 200) {
      ElMessage.success('登录成功')
      router.push('/dashboard')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
  width: 400px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 10px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.2);
}

.login-title {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
  font-size: 24px;
}

.login-form {
  margin-bottom: 20px;
}

.form-item {
  margin-bottom: 18px;
}

.form-input {
  width: 100%;
  height: 40px;
  padding: 0 15px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
  box-sizing: border-box;
}

.form-input:focus {
  border-color: #409eff;
}

.form-input::placeholder {
  color: #c0c4cc;
}

.login-btn {
  width: 100%;
  height: 40px;
  background: #409eff;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: background 0.2s;
}

.login-btn:hover:not(:disabled) {
  background: #66b1ff;
}

.login-btn:disabled {
  background: #a0cfff;
  cursor: not-allowed;
}

.login-tips {
  text-align: center;
  color: #999;
  font-size: 12px;
}

.login-tips p {
  margin: 5px 0;
}
</style>
