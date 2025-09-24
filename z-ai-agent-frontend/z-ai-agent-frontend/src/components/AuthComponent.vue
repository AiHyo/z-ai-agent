<template>
  <div class="auth-container">
    <!-- 登录/注册切换按钮 -->
    <div class="auth-tabs">
      <button
        class="auth-tab"
        :class="{ 'active': activeTab === 'login' }"
        @click="activeTab = 'login'"
      >登录</button>
      <button
        class="auth-tab"
        :class="{ 'active': activeTab === 'register' }"
        @click="activeTab = 'register'"
      >注册</button>
      <div class="tab-indicator" :class="activeTab"></div>
    </div>

    <!-- 错误提示信息 -->
    <div v-if="errorMsg" class="error-message">
      {{ errorMsg }}
    </div>

    <!-- 成功提示信息 -->
    <div v-if="successMsg" class="success-message">
      {{ successMsg }}
    </div>

    <!-- 登录表单 -->
    <div v-if="activeTab === 'login'" class="auth-form">
      <div class="form-group">
        <div class="input-wrapper">
          <input
            type="text"
            v-model="loginForm.username"
            placeholder="用户名"
            class="cyber-input"
          />
          <div class="input-glow"></div>
        </div>
      </div>

      <div class="form-group">
        <div class="input-wrapper">
          <input
            type="password"
            v-model="loginForm.password"
            placeholder="密码"
            class="cyber-input"
          />
          <div class="input-glow"></div>
        </div>
      </div>

      <button @click="handleLogin" class="auth-button login-button">
        <span v-if="!isLoading">登录</span>
        <span v-else class="loading-spinner"></span>
        <div class="button-glow"></div>
      </button>
    </div>

    <!-- 注册表单 -->
    <div v-if="activeTab === 'register'" class="auth-form">
      <div class="form-group">
        <div class="input-wrapper">
          <input
            type="text"
            v-model="registerForm.username"
            placeholder="用户名"
            class="cyber-input"
          />
          <div class="input-glow"></div>
        </div>
      </div>

      <div class="form-group">
        <div class="input-wrapper">
          <input
            type="password"
            v-model="registerForm.password"
            placeholder="密码"
            class="cyber-input"
          />
          <div class="input-glow"></div>
        </div>
      </div>

      <div class="form-group">
        <div class="input-wrapper">
          <input
            type="password"
            v-model="registerForm.confirmPassword"
            placeholder="确认密码"
            class="cyber-input"
          />
          <div class="input-glow"></div>
        </div>
      </div>

      <button @click="handleRegister" class="auth-button register-button">
        <span v-if="!isLoading">注册</span>
        <span v-else class="loading-spinner"></span>
        <div class="button-glow"></div>
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import axios from 'axios'
import { API_URL, authApi, apiClient } from '../services/api.js'

// Props
const props = defineProps({
  initialTab: {
    type: String,
    default: 'login'
  }
})

// Emits
const emit = defineEmits(['login-success', 'close'])

// 状态变量
const activeTab = ref(props.initialTab)
const isLoading = ref(false)
const successMsg = ref('')
const errorMsg = ref('')

// 表单数据
const loginForm = reactive({
  username: '',
  password: ''
})

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: ''
})

// 清除消息
const clearMessages = () => {
  successMsg.value = ''
  errorMsg.value = ''
}

// 设置定时消息
const setTimedMessage = (msgRef, message, duration = 3000) => {
  msgRef.value = message
  setTimeout(() => {
    msgRef.value = ''
  }, duration)
}

// 切换标签
const switchTab = (tab) => {
  activeTab.value = tab
  clearMessages()
}

// 登录处理
const handleLogin = async () => {
  clearMessages()

  if (!loginForm.username || !loginForm.password) {
    setTimedMessage(errorMsg, '请输入用户名和密码')
    return
  }

  try {
    isLoading.value = true
    console.log('开始登录请求...')

    const response = await apiClient.post('/user/login', {
      username: loginForm.username,
      password: loginForm.password
    })

    console.log('登录响应:', response)

    // 检查响应结构
    if (response.data && response.data.code === 0) {
      const authToken = response.data.data?.Authorization

      if (authToken) {
        // 直接保存token，不添加Bearer前缀
        localStorage.setItem('Authorization', authToken)

        // 设置axios默认headers
        axios.defaults.headers.common['Authorization'] = authToken

        // 显示成功消息
        setTimedMessage(successMsg, '登录成功')

        // 触发登录成功事件
        setTimeout(() => {
          emit('login-success', {
            username: loginForm.username,
            token: authToken
          })
        }, 1000)
      } else {
        throw new Error('登录成功但未获取到授权令牌')
      }
    } else {
      throw new Error(response.data?.message || '登录失败，请稍后再试')
    }
  } catch (error) {
    console.error('登录失败', error)
    let errorMessage = '登录失败，请稍后再试'

    if (error.response) {
      errorMessage = error.response.data?.message || `服务器错误: ${error.response.status}`
    } else if (error.request) {
      errorMessage = '无法连接到服务器，请检查网络连接'
    } else if (error.message) {
      errorMessage = error.message
    }

    setTimedMessage(errorMsg, errorMessage)
  } finally {
    isLoading.value = false
  }
}

// 注册处理
const handleRegister = async () => {
  clearMessages()

  if (!registerForm.username || !registerForm.password) {
    setTimedMessage(errorMsg, '请输入用户名和密码')
    return
  }

  if (registerForm.password !== registerForm.confirmPassword) {
    setTimedMessage(errorMsg, '两次输入的密码不一致')
    return
  }

  try {
    isLoading.value = true
    const response = await axios.post(`/api/user/register`, {
      username: registerForm.username,
      password: registerForm.password
    })

    if (response.data && response.data.code === 0) {
      setTimedMessage(successMsg, '注册成功，请登录')
      // 切换到登录标签
      setTimeout(() => {
        switchTab('login')
        // 预填充登录表单
        loginForm.username = registerForm.username
        loginForm.password = ''
      }, 1500)
    } else {
      throw new Error(response.data?.message || '注册失败，请稍后再试')
    }
  } catch (error) {
    console.error('注册失败', error)
    let errorMessage = '注册失败，请稍后再试'

    if (error.response) {
      errorMessage = error.response.data?.message || `服务器错误: ${error.response.status}`
    } else if (error.request) {
      errorMessage = '无法连接到服务器，请检查网络连接'
    } else if (error.message) {
      errorMessage = error.message
    }

    setTimedMessage(errorMsg, errorMessage)
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped>
.auth-container {
  background: linear-gradient(135deg, rgba(20, 21, 46, 0.95), rgba(16, 17, 38, 0.95));
  border-radius: 20px;
  padding: 2.5rem;
  width: 450px;
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.7), 0 0 20px rgba(100, 100, 255, 0.2);
  border: 1px solid rgba(100, 100, 255, 0.2);
  overflow: hidden;
  position: relative;
  z-index: 1000;
  animation: popIn 0.3s forwards ease-out;
  backdrop-filter: blur(12px);
}

.auth-container::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(
    45deg,
    transparent,
    rgba(58, 134, 255, 0.1),
    transparent
  );
  animation: rotate 15s linear infinite;
  pointer-events: none;
  z-index: -1;
}

/* 消息提示样式 */
.error-message,
.success-message {
  margin: 0.8rem 0;
  padding: 0.8rem;
  border-radius: 10px;
  font-size: 1rem;
  text-align: center;
  animation: fadeIn 0.3s forwards;
}

.error-message {
  background-color: rgba(239, 68, 68, 0.2);
  border: 1px solid rgba(239, 68, 68, 0.4);
  color: #fecaca;
}

.success-message {
  background-color: rgba(34, 197, 94, 0.2);
  border: 1px solid rgba(34, 197, 94, 0.4);
  color: #bbf7d0;
}

@keyframes rotate {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.auth-tabs {
  display: flex;
  position: relative;
  margin-bottom: 2rem;
  border-bottom: 2px solid rgba(100, 100, 255, 0.2);
}

.auth-tab {
  flex: 1;
  background: transparent;
  border: none;
  color: #b5b5c3;
  padding: 1rem 0;
  cursor: pointer;
  font-weight: bold;
  font-size: 1.2rem;
  position: relative;
  transition: all 0.3s ease;
  z-index: 1;
}

.auth-tab.active {
  color: #ffffff;
  text-shadow: 0 0 10px rgba(255, 255, 255, 0.5);
}

.auth-tab:focus {
  outline: none;
}

.tab-indicator {
  position: absolute;
  bottom: -2px;
  height: 4px;
  width: 50%;
  background: linear-gradient(to right, #ff006a, #3a86ff);
  transition: transform 0.3s ease;
  border-radius: 4px 4px 0 0;
  box-shadow: 0 0 10px rgba(58, 134, 255, 0.5);
}

.tab-indicator.login {
  transform: translateX(0);
}

.tab-indicator.register {
  transform: translateX(100%);
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  animation: fadeIn 0.3s forwards;
}

.form-group {
  position: relative;
}

.input-wrapper {
  position: relative;
}

.cyber-input {
  width: 100%;
  padding: 1.2rem 1.5rem;
  background-color: rgba(30, 31, 70, 0.6);
  border: 1px solid rgba(100, 100, 255, 0.2);
  border-radius: 10px;
  color: #ffffff;
  font-family: var(--font-family, 'JetBrains Mono');
  font-size: 1.1rem;
  transition: all 0.3s ease;
}

.cyber-input:focus {
  outline: none;
  border-color: #3a86ff;
  box-shadow: 0 0 15px rgba(58, 134, 255, 0.3);
}

.input-glow {
  position: absolute;
  top: 0;
  left: 0;
  width: 0;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(58, 134, 255, 0.3), transparent);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.cyber-input:focus + .input-glow {
  opacity: 1;
  width: 100%;
  animation: inputGlow 2s infinite;
}

@keyframes inputGlow {
  0% { left: -100%; }
  100% { left: 100%; }
}

.auth-button {
  padding: 1.2rem 0;
  border: none;
  border-radius: 10px;
  color: white;
  font-weight: bold;
  font-size: 1.2rem;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
  margin-top: 1rem;
  letter-spacing: 1px;
}

.login-button {
  background: linear-gradient(90deg, #1a56ff, #00c6ff);
  box-shadow: 0 5px 15px rgba(58, 134, 255, 0.4);
}

.register-button {
  background: linear-gradient(90deg, #ff006a, #ff4b8b);
  box-shadow: 0 5px 15px rgba(255, 0, 106, 0.4);
}

.auth-button:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(58, 134, 255, 0.6);
}

.button-glow {
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  animation: buttonGlow 2s infinite;
}

@keyframes buttonGlow {
  0% { left: -100%; }
  100% { left: 100%; }
}

.loading-spinner {
  display: inline-block;
  width: 24px;
  height: 24px;
  border: 3px solid rgba(255,255,255,0.3);
  border-radius: 50%;
  border-top-color: #fff;
  animation: spin 1s ease-in-out infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

@keyframes popIn {
  0% { transform: scale(0.9); opacity: 0; }
  100% { transform: scale(1); opacity: 1; }
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
