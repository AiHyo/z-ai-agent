<template>
  <div class="header-nav">
    <!-- 右侧用户信息/登录按钮 -->
    <div class="user-section">
      <!-- 已登录状态 -->
      <div v-if="isLoggedIn" class="user-info">
        <div class="user-avatar">
          <div class="avatar-placeholder">{{ usernameInitial }}</div>
        </div>
        <div class="dropdown-menu">
          <div class="user-name">{{ username }}</div>
          <div class="menu-divider"></div>
          <button @click="handleLogout" class="logout-button">
            登出
            <div class="button-ripple"></div>
          </button>
        </div>
      </div>

      <!-- 未登录状态 - 分离登录和注册按钮 -->
      <div v-else class="auth-buttons">
        <button @click="showLoginForm" class="auth-button login-button">
          登录
          <div class="button-glow"></div>
        </button>
        <button @click="showRegisterForm" class="auth-button register-button">
          注册
          <div class="button-glow"></div>
        </button>
      </div>
    </div>

    <!-- 登录/注册弹窗 -->
    <teleport to="body">
      <div v-if="showAuthModal" class="modal-backdrop" @click="closeModal">
        <div class="modal-content" @click.stop>
          <button class="close-button" @click="closeModal">×</button>
          <AuthComponent
            :initial-tab="activeAuthTab"
            @login-success="handleLoginSuccess"
          />
        </div>
      </div>
    </teleport>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import AuthComponent from './AuthComponent.vue'
import { authApi } from '@/services/api'

export default {
  name: 'HeaderNav',
  components: {
    AuthComponent
  },
  setup() {
    const showAuthModal = ref(false)
    const activeAuthTab = ref('login')
    const isLoggedIn = ref(false)
    const username = ref('')

    // 获取用户名首字母作为头像
    const usernameInitial = computed(() => {
      return username.value ? username.value.charAt(0).toUpperCase() : '?'
    })

    // 显示登录表单
    const showLoginForm = () => {
      activeAuthTab.value = 'login'
      showAuthModal.value = true
    }

    // 显示注册表单
    const showRegisterForm = () => {
      activeAuthTab.value = 'register'
      showAuthModal.value = true
    }

    // 检查登录状态
    const checkLoginStatus = async () => {
      const token = localStorage.getItem('Authorization')
      if (token) {
        try {
          // 使用 isLogin 接口检查登录状态
          const response = await authApi.isLogin()
          if (response.code === 0 && response.data === true) {
            // 获取用户信息
            const userInfoResponse = await authApi.getUserInfo()
            if (userInfoResponse.code === 0 && userInfoResponse.data) {
              isLoggedIn.value = true
              username.value = userInfoResponse.data.username
            } else {
              // token无效
              localStorage.removeItem('Authorization')
              isLoggedIn.value = false
            }
          } else {
            // token无效
            localStorage.removeItem('Authorization')
            isLoggedIn.value = false
          }
        } catch (error) {
          console.error('获取用户信息失败', error)
          localStorage.removeItem('Authorization')
          isLoggedIn.value = false
        }
      }
    }

    // 登录成功处理
    const handleLoginSuccess = (userData) => {
      isLoggedIn.value = true
      username.value = userData.username
      closeModal()
    }

    // 登出处理
    const handleLogout = async () => {
      const token = localStorage.getItem('Authorization')
      if (token) {
        try {
          await authApi.logout(token)
        } catch (error) {
          console.error('登出失败', error)
        } finally {
          localStorage.removeItem('Authorization')
          isLoggedIn.value = false
          username.value = ''
        }
      } else {
        localStorage.removeItem('Authorization')
        isLoggedIn.value = false
        username.value = ''
      }
    }

    // 关闭登录弹窗
    const closeModal = () => {
      showAuthModal.value = false
    }

    // 组件挂载时检查登录状态
    onMounted(() => {
      checkLoginStatus()
    })

    return {
      isLoggedIn,
      username,
      usernameInitial,
      showAuthModal,
      activeAuthTab,
      showLoginForm,
      showRegisterForm,
      closeModal,
      handleLogout,
      handleLoginSuccess
    }
  }
}
</script>

<style scoped>
.header-nav {
  position: absolute;
  top: 1rem;
  right: 2rem;
  display: flex;
  align-items: center;
  z-index: 100;
}

.user-section {
  position: relative;
}

/* 登录注册按钮容器 */
.auth-buttons {
  display: flex;
  gap: 1rem;
}

/* 按钮通用样式 */
.auth-button {
  color: white;
  border: none;
  padding: 0.6rem 1.2rem;
  border-radius: 50px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
}

/* 登录按钮样式 */
.login-button {
  background: linear-gradient(90deg, #1a56ff 30%, #00c6ff 100%);
}

/* 注册按钮样式 */
.register-button {
  background: linear-gradient(90deg, #ff006a 30%, #ff4b8b 100%);
}

.auth-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 15px rgba(58, 134, 255, 0.5);
}

.button-glow {
  position: absolute;
  top: 0;
  left: -100%;
  width: 60%;
  height: 100%;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.3),
    transparent
  );
  transform: skewX(-25deg);
  animation: buttonGlow 2s infinite;
}

@keyframes buttonGlow {
  0% { left: -100%; }
  100% { left: 200%; }
}

/* 用户信息样式 */
.user-info {
  position: relative;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #1a56ff, #00c6ff);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border: 2px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.user-avatar::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at center, transparent 50%, rgba(0, 0, 0, 0.2));
  pointer-events: none;
}

.user-avatar:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.4);
}

.avatar-placeholder {
  color: white;
  font-size: 1.2rem;
  font-weight: bold;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

/* 下拉菜单 */
.user-info:hover .dropdown-menu {
  visibility: visible;
  opacity: 1;
  transform: translateY(0);
}

.dropdown-menu {
  position: absolute;
  top: 50px;
  right: 0;
  width: 150px;
  background: linear-gradient(135deg, rgba(20, 21, 46, 0.95), rgba(16, 17, 38, 0.95));
  border-radius: 12px;
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.8rem;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.6);
  border: 1px solid rgba(100, 100, 255, 0.2);
  visibility: hidden;
  opacity: 0;
  transform: translateY(-10px);
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
}

.dropdown-menu::before {
  content: '';
  position: absolute;
  top: -10px;
  right: 15px;
  width: 0;
  height: 0;
  border-left: 10px solid transparent;
  border-right: 10px solid transparent;
  border-bottom: 10px solid rgba(20, 21, 46, 0.95);
}

.user-name {
  font-size: 1rem;
  font-weight: 600;
  color: #fff;
  text-align: center;
  padding: 0.3rem 0;
}

.menu-divider {
  height: 1px;
  background: linear-gradient(to right, transparent, rgba(100, 100, 255, 0.3), transparent);
  margin: 0.2rem 0;
}

.logout-button {
  background: linear-gradient(90deg, #ff006a 30%, #ff4b8b 100%);
  color: white;
  border: none;
  padding: 0.6rem 0;
  border-radius: 8px;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}

.logout-button:hover {
  transform: translateY(-2px);
}

.button-ripple {
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  animation: ripple 2s infinite;
}

@keyframes ripple {
  0% { left: -100%; }
  100% { left: 100%; }
}

/* 弹窗样式 */
.modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.3s forwards;
  backdrop-filter: blur(5px);
}

.modal-content {
  position: relative;
  animation: scaleIn 0.3s forwards;
}

.close-button {
  position: absolute;
  top: -15px;
  right: -15px;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff006a, #3a86ff);
  color: white;
  border: none;
  font-size: 1.2rem;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 10;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.4);
  transition: all 0.3s ease;
}

.close-button:hover {
  transform: rotate(90deg);
}

@keyframes fadeIn {
  0% { opacity: 0; }
  100% { opacity: 1; }
}

@keyframes scaleIn {
  0% { transform: scale(0.8); }
  100% { transform: scale(1); }
}

/* 响应式调整 */
@media (max-width: 768px) {
  .header-nav {
    top: 0.5rem;
    right: 1rem;
  }

  .auth-buttons {
    flex-direction: column;
    gap: 0.5rem;
  }

  .auth-button {
    padding: 0.5rem 1rem;
    font-size: 0.9rem;
  }
}
</style>
