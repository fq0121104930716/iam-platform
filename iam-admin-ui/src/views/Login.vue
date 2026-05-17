<template>
  <div class="login-container">
    <div class="login-form">
      <h1 class="login-title">IAM Platform</h1>
      <h2 class="login-subtitle">统一身份认证管理平台</h2>
      
      <el-form :model="loginForm" :rules="rules" ref="formRef">
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="用户名"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            prefix-icon="Lock"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            style="width: 100%"
            :loading="loading"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-links">
        <router-link to="/register">注册账号</router-link>
        <a href="#">忘记密码?</a>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref()
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      // TODO: Replace with actual login API call
      // For now, simulate successful login
      authStore.setToken('mock-token')
      authStore.setUser({ username: loginForm.username })
      
      ElMessage.success('登录成功')
      router.push('/dashboard')
    } catch (error) {
      ElMessage.error('登录失败')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-form {
  width: 420px;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.login-title {
  text-align: center;
  color: #303133;
  margin-bottom: 10px;
}

.login-subtitle {
  text-align: center;
  color: #909399;
  font-size: 14px;
  font-weight: normal;
  margin-bottom: 30px;
}

.login-links {
  display: flex;
  justify-content: space-between;
  margin-top: 15px;
  color: #409eff;
}

.login-links a {
  color: #409eff;
  text-decoration: none;
}

.login-links a:hover {
  text-decoration: underline;
}
</style>
