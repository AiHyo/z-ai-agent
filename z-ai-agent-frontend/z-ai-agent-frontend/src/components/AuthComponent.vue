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

<script>
import { ref, reactive, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '@/services/api'

export default {
  name: 'AuthComponent',
  emits: ['login-success', 'close'],
  props: {
    show: {
      type: Boolean,
      default: false
    },
    initialTab: {
      type: String,
      default: 'login'
    }
  },
  setup(props, { emit }) {
    const router = useRouter()
    const activeTab = ref(props.initialTab)
    const isLoading = ref(false)
    const errorMsg = ref('')
    const successMsg = ref('')
    
    const loginForm = reactive({
      username: '',
      password: ''
    })
    
    const registerForm = reactive({
      username: '',
      password: '',
      confirmPassword: ''
    })
    
    // 监听初始标签变化
    watch(() => props.initialTab, (newVal) => {
      activeTab.value = newVal
    })
    
    // 清除消息
    const clearMessages = () => {
      errorMsg.value = ''
      successMsg.value = ''
    }
    
    // 设置消息自动消失
    const setTimedMessage = (msgRef, content, duration = 3000) => {
      msgRef.value = content
      setTimeout(() => {
        msgRef.value = ''
      }, duration)
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
        const response = await authApi.login(loginForm.username, loginForm.password)
        
        console.log('登录响应:', response)
        
        // 保存token到localStorage
        if (response.code === 0 && response.data) {
          localStorage.setItem('Authorization', response.data.Authorization)
          
          // 显示成功消息
          setTimedMessage(successMsg, '登录成功')
          
          // 触发登录成功事件
          setTimeout(() => {
            emit('login-success', { 
              username: loginForm.username,
              ...response.data
            })
          }, 1000)
        } else {
          throw new Error(response.message || '登录失败，请稍后再试')
        }
      } catch (error) {
        console.error('登录失败', error)
        setTimedMessage(errorMsg, error?.message || '登录失败，请稍后再试')
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
        console.log('开始注册请求...')
        const response = await authApi.register(registerForm.username, registerForm.password)
        
        console.log('注册响应:', response)
        
        if (response.code === 0) {
          console.log('注册成功')
          setTimedMessage(successMsg, '注册成功，请登录')
          
          setTimeout(() => {
            activeTab.value = 'login'
            loginForm.username = registerForm.username
            loginForm.password = ''
          }, 1000)
        } else {
          throw new Error(response.message || '注册失败，请稍后再试')
        }
      } catch (error) {
        console.error('注册失败', error)
        setTimedMessage(errorMsg, error?.message || '注册失败，请稍后再试')
      } finally {
        isLoading.value = false
      }
    }
    
    // 组件挂载时设置初始标签
    onMounted(() => {
      activeTab.value = props.initialTab
    })
    
    return {
      activeTab,
      loginForm,
      registerForm,
      isLoading,
      errorMsg,
      successMsg,
      handleLogin,
      handleRegister
    }
  }
}
</script>

<style scoped>
.auth-container {
  background: linear-gradient(135deg, rgba(20, 21, 46, 0.95), rgba(16, 17, 38, 0.95));
  border-radius: 16px;
  padding: 1.5rem;
  width: 300px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.6);
  border: 1px solid rgba(100, 100, 255, 0.2);
  overflow: hidden;
  position: relative;
  z-index: 1000;
  animation: popIn 0.3s forwards ease-out;
  backdrop-filter: blur(10px);
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
    rgba(58, 134, 255, 0.05), 
    transparent
  );
  animation: rotate 10s linear infinite;
  pointer-events: none;
  z-index: -1;
}

/* 消息提示样式 */
.error-message,
.success-message {
  margin: 0.5rem 0;
  padding: 0.6rem;
  border-radius: 8px;
  font-size: 0.9rem;
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
  margin-bottom: 1.5rem;
  border-bottom: 1px solid rgba(100, 100, 255, 0.2);
}

.auth-tab {
  flex: 1;
  background: transparent;
  border: none;
  color: #b5b5c3;
  padding: 0.8rem 0;
  cursor: pointer;
  font-weight: bold;
  position: relative;
  transition: all 0.3s ease;
  z-index: 1;
}

.auth-tab.active {
  color: #ffffff;
}

.auth-tab:focus {
  outline: none;
}

.tab-indicator {
  position: absolute;
  bottom: 0;
  height: 3px;
  width: 50%;
  background: linear-gradient(to right, #ff006a, #3a86ff);
  transition: transform 0.3s ease;
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
  gap: 1.2rem;
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
  padding: 0.8rem 1rem;
  background-color: rgba(30, 31, 70, 0.6);
  border: 1px solid rgba(100, 100, 255, 0.2);
  border-radius: 8px;
  color: #ffffff;
  font-family: var(--font-family, 'JetBrains Mono');
  transition: all 0.3s ease;
}

.cyber-input:focus {
  outline: none;
  border-color: #3a86ff;
  box-shadow: 0 0 0 3px rgba(58, 134, 255, 0.2);
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
  padding: 0.8rem 0;
  border: none;
  border-radius: 8px;
  color: white;
  font-weight: bold;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
  margin-top: 0.5rem;
}

.login-button {
  background: linear-gradient(90deg, #1a56ff, #00c6ff);
  box-shadow: 0 4px 10px rgba(58, 134, 255, 0.3);
}

.register-button {
  background: linear-gradient(90deg, #ff006a, #ff4b8b);
  box-shadow: 0 4px 10px rgba(255, 0, 106, 0.3);
}

.auth-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 15px rgba(58, 134, 255, 0.5);
}

.button-glow {
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  animation: buttonGlow 2s infinite;
}

@keyframes buttonGlow {
  0% { left: -100%; }
  100% { left: 100%; }
}

.loading-spinner {
  display: inline-block;
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255,255,255,0.3);
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
  0% { opacity: 0; transform: translateY(10px); }
  100% { opacity: 1; transform: translateY(0); }
}
</style> 