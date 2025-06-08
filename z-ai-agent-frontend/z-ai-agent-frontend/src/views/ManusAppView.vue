<template>
  <div :class="containerClasses">
    <div class="cyber-grid"></div>
    
    <!-- 会话侧边栏 -->
    <ConversationSidebar 
      v-if="isLoggedIn"
      aiType="manus"
      :initialConversationId="null"
      @update:collapsed="updateSidebarState"
      @conversation-selected="() => {}"
      @conversation-created="() => {}"
      disabled
    />
    
    <!-- 登录提示 -->
    <div v-if="!isLoggedIn && showLoginNotice" class="login-notice">
      <p>登录后可保存对话记录</p>
      <div class="login-notice-actions">
        <button @click="showLoginForm">登录</button>
        <button @click="showRegisterForm">注册</button>
        <button @click="dismissLoginNotice" class="dismiss-btn">X</button>
      </div>
    </div>
    
    <!-- 添加页面右上角的用户登录状态和头像 -->
    <div class="header-nav">
      <!-- 右侧用户信息/登录按钮 -->
      <div class="user-section">
        <!-- 已登录状态 -->
        <div v-if="isLoggedIn" class="user-info">
          <div class="user-avatar">{{ usernameInitial }}</div>
          <span class="username">{{ username }}</span>
          <button @click="handleLogout" class="logout-btn">退出</button>
        </div>
        
        <!-- 未登录状态 -->
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
    </div>
    
    <!-- 登录/注册弹窗 -->
    <teleport to="body">
      <div v-if="showAuthModal" class="modal-backdrop" @click="closeModal">
        <div class="modal-content" @click.stop>
          <button class="close-button" @click="closeModal">×</button>
          <AuthComponent 
            :activeTab="activeAuthTab" 
            @close="closeModal" 
            @login-success="handleLoginSuccess" 
          />
        </div>
      </div>
    </teleport>
    
    <div class="chat-header">
      <h1>AI超级智能体</h1>
    </div>
    
    <div class="chat-messages" ref="messagesContainer">
      <div 
        v-for="(message, index) in messages" 
        :key="index" 
        :class="['message', message.isUser ? 'message-user' : 'message-ai']"
      >
        <div v-if="!message.isUser" class="message-avatar">
          <AiAvatar type="manus" />
        </div>
        <div class="message-content">
          {{ message.content }}
          <span v-if="message.isTyping" class="typing-indicator">
            <span class="dot"></span>
            <span class="dot"></span>
            <span class="dot"></span>
          </span>
        </div>
        <div v-if="message.isUser" class="message-avatar">
          <AiAvatar type="user" />
        </div>
      </div>
    </div>
    
    <div class="chat-input">
      <input 
        v-model="inputMessage" 
        placeholder="请输入您的问题..." 
        @keyup.enter="sendMessage"
        :disabled="isWaitingForResponse"
      />
      <button @click="sendMessage" :disabled="isWaitingForResponse">
        {{ isWaitingForResponse ? '等待回复...' : '发送' }}
      </button>
    </div>
    
    <TheFooter />
  </div>
</template>

<script>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { chatWithManus, authApi } from '@/services/api'
import AiAvatar from '../components/AiAvatar.vue'
import TheFooter from '../components/TheFooter.vue'
import ConversationSidebar from '../components/ConversationSidebar.vue'
import AuthComponent from '../components/AuthComponent.vue'

export default {
  name: 'ManusAppView',
  components: {
    AiAvatar,
    TheFooter,
    ConversationSidebar,
    AuthComponent
  },
  setup() {
    const inputMessage = ref('')
    const messages = ref([])
    const messagesContainer = ref(null)
    const isWaitingForResponse = ref(false)
    let chatConnection = null
    
    // 侧边栏折叠状态
    const sidebarCollapsed = ref(false)
    
    // 监听侧边栏折叠状态变化
    const updateSidebarState = (collapsed) => {
      sidebarCollapsed.value = collapsed
    }
    
    // 用户登录相关状态
    const showAuthModal = ref(false)
    const activeAuthTab = ref('login')
    const isLoggedIn = ref(false)
    const username = ref('')
    const showLoginNotice = ref(false)
    
    // 获取用户名首字母作为头像
    const usernameInitial = computed(() => {
      return username.value ? username.value.charAt(0).toUpperCase() : '?'
    })
    
    // 容器类计算属性，根据侧边栏状态更新
    const containerClasses = computed(() => {
      return {
        'chat-container': true,
        'sidebar-collapsed': sidebarCollapsed.value
      }
    })
    
    // 显示登录表单
    const showLoginForm = () => {
      activeAuthTab.value = 'login'
      showAuthModal.value = true
      showLoginNotice.value = false
    }
    
    // 显示注册表单
    const showRegisterForm = () => {
      activeAuthTab.value = 'register'
      showAuthModal.value = true
      showLoginNotice.value = false
    }
    
    // 关闭登录提示
    const dismissLoginNotice = () => {
      showLoginNotice.value = false
    }
    
    // 检查登录状态
    const checkLoginStatus = async () => {
      try {
        const response = await authApi.checkLoginStatus()
        isLoggedIn.value = response.data
        
        if (isLoggedIn.value) {
          const userInfo = await authApi.getUserInfo()
          username.value = userInfo.data.nickname || userInfo.data.username
        } else {
          // 如果未登录，则显示登录提示
          showLoginNotice.value = true
        }
      } catch (error) {
        console.error('检查登录状态失败:', error)
        isLoggedIn.value = false
      }
    }
    
    // 处理登录成功
    const handleLoginSuccess = async (userData) => {
      isLoggedIn.value = true
      username.value = userData.nickname || userData.username
      showAuthModal.value = false
      showLoginNotice.value = false
    }
    
    // 处理登出
    const handleLogout = async () => {
      try {
        await authApi.logout()
        isLoggedIn.value = false
        username.value = ''
        // 清空消息记录
        messages.value = [{
          content: '你好，我是AI超级智能体，我可以协助你完成各种任务。有什么我能帮到你的吗？',
          isUser: false,
          isTyping: false
        }]
      } catch (error) {
        console.error('登出失败:', error)
      }
    }
    
    // 关闭登录弹窗
    const closeModal = () => {
      showAuthModal.value = false
    }

    // 自动滚动到底部
    const scrollToBottom = async () => {
      await nextTick()
      if (messagesContainer.value) {
        messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
      }
    }

    // 监听消息变化并滚动
    watch(messages, () => {
      scrollToBottom()
    }, { deep: true })

    // 初始化聊天
    onMounted(() => {
      messages.value.push({
        content: '你好，我是AI超级智能体，我可以协助你完成各种任务。有什么我能帮到你的吗？',
        isUser: false,
        isTyping: false
      })
      
      // 创建网格背景效果
      initCyberGrid();
      
      // 设置页面标题和元数据
      document.title = 'AI超级智能体 - 您的多功能智能助手'
      
      // 创建meta描述标签
      if (!document.querySelector('meta[name="description"]')) {
        const metaDesc = document.createElement('meta')
        metaDesc.name = 'description'
        metaDesc.content = 'AI超级智能体是您的多功能助手，可以回答问题、提供建议、完成任务，让生活更智能。'
        document.head.appendChild(metaDesc)
      }
      
      // 检查用户登录状态
      checkLoginStatus()
    })
    
    // 创建网格背景
    const initCyberGrid = () => {
      if (typeof document !== 'undefined') {
        const grid = document.querySelector('.cyber-grid');
        if (grid) {
          for (let i = 0; i < 50; i++) {
            const line = document.createElement('div');
            line.className = 'grid-line';
            line.style.left = `${Math.random() * 100}%`;
            line.style.animationDelay = `${Math.random() * 5}s`;
            line.style.height = `${Math.random() * 30 + 70}%`;
            
            grid.appendChild(line);
          }
        }
      }
    }

    // 发送消息
    const sendMessage = () => {
      if (!inputMessage.value.trim() || isWaitingForResponse.value) return

      // 添加用户消息
      const userMessage = inputMessage.value
      messages.value.push({
        content: userMessage,
        isUser: true,
        isTyping: false
      })
      
      // 清空输入框并设置等待状态
      inputMessage.value = ''
      isWaitingForResponse.value = true
      
      // 关闭上一个连接（如果存在）
      if (chatConnection) {
        chatConnection.close()
      }
      
      // 添加AI消息占位符
      messages.value.push({
        content: '',
        isUser: false,
        isTyping: true
      })
      
      let aiResponseIndex = messages.value.length - 1
      
      // 建立新连接并发送消息
      chatConnection = chatWithManus(
        userMessage,
        (data) => {
          // 更新AI消息内容
          messages.value[aiResponseIndex].content += data
          messages.value[aiResponseIndex].isTyping = true
          // 重置任何可能存在的超时检测
          if (timeoutCheck) {
            clearTimeout(timeoutCheck)
            clearInterval(timeoutCheck)
          }
        },
        (error) => {
          console.error('聊天出错:', error)
          // 只在真正错误的情况下才显示错误信息
          messages.value[aiResponseIndex].content = '抱歉，连接出现问题，请稍后再试。'
          messages.value[aiResponseIndex].isTyping = false
          isWaitingForResponse.value = false
        },
        () => {
          // 消息接收完成回调
          console.log('消息接收完成')
          messages.value[aiResponseIndex].isTyping = false
          isWaitingForResponse.value = false
          if (timeoutCheck) {
            clearTimeout(timeoutCheck)
            clearInterval(timeoutCheck)
          }
          chatConnection = null
        }
      )
      
      // 改进超时检测机制 - 作为备用方案
      let timeoutCheck = null;
      const checkMessageComplete = () => {
        if (chatConnection) {
          let lastContent = messages.value[aiResponseIndex].content;
          let noChangeCounter = 0;
          
          // 使用间隔检查，而不是嵌套setTimeout
          timeoutCheck = setInterval(() => {
            // 检查内容是否有变化
            if (lastContent === messages.value[aiResponseIndex].content) {
              noChangeCounter++;
              
              // 如果连续5次检查内容没变化，则认为流已结束
              if (noChangeCounter >= 5) {
                clearInterval(timeoutCheck);
                messages.value[aiResponseIndex].isTyping = false;
                isWaitingForResponse.value = false;
                chatConnection.close();
                chatConnection = null;
              }
            } else {
              // 内容有变化，重置计数器
              lastContent = messages.value[aiResponseIndex].content;
              noChangeCounter = 0;
            }
          }, 1000);
        }
      }
      
      checkMessageComplete()
    }

    return {
      inputMessage,
      messages,
      messagesContainer,
      isWaitingForResponse,
      sendMessage,
      // 新增返回值
      sidebarCollapsed,
      containerClasses,
      isLoggedIn,
      username,
      usernameInitial,
      showAuthModal,
      activeAuthTab,
      showLoginNotice,
      showLoginForm,
      showRegisterForm,
      dismissLoginNotice,
      handleLoginSuccess,
      handleLogout,
      closeModal,
      updateSidebarState
    }
  }
}
</script>

<style scoped>
.chat-container {
  position: relative;
  min-height: 100vh;
  background-color: #0a0c1b;
  color: #f0f0f0;
  display: flex;
  flex-direction: column;
  padding-left: 0;
  transition: padding-left 0.3s ease;
}

.chat-container:not(.sidebar-collapsed) {
  padding-left: 280px;
}

.cyber-grid {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 0;
  overflow: hidden;
}

/* 登录提示样式 */
.login-notice {
  position: fixed;
  top: 70px;
  right: 20px;
  background-color: rgba(20, 30, 60, 0.9);
  border: 1px solid #4a55a0;
  border-radius: 5px;
  padding: 15px;
  z-index: 100;
  box-shadow: 0 0 20px rgba(83, 100, 255, 0.3);
  max-width: 300px;
}

.notice-content {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.notice-title {
  font-size: 18px;
  font-weight: bold;
  color: #7a85ff;
}

.notice-text {
  font-size: 14px;
  color: #f0f0f0;
}

.notice-actions {
  display: flex;
  gap: 10px;
}

.login-btn, .register-btn {
  padding: 8px 15px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
  transition: all 0.3s ease;
}

.login-btn {
  background-color: #4a55a0;
  color: white;
}

.register-btn {
  background-color: transparent;
  border: 1px solid #4a55a0;
  color: #7a85ff;
}

.dismiss-btn {
  background-color: transparent;
  border: none;
  color: #7a85ff;
  text-decoration: underline;
  cursor: pointer;
  margin-top: 5px;
  align-self: flex-end;
  font-size: 12px;
}

/* 用户信息/登录按钮样式 */
.header-nav {
  position: absolute;
  top: 20px;
  right: 30px;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

.user-section {
  position: relative;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  position: relative;
}

.user-info:hover .dropdown-menu {
  display: block;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #4a55a0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: bold;
  border: 2px solid #7a85ff;
  box-shadow: 0 0 10px rgba(122, 133, 255, 0.5);
}

.avatar-placeholder {
  font-size: 18px;
}

.dropdown-menu {
  display: none;
  position: absolute;
  top: 100%;
  right: 0;
  background-color: rgba(20, 30, 60, 0.95);
  border: 1px solid #4a55a0;
  border-radius: 5px;
  padding: 10px;
  min-width: 150px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.3);
  z-index: 101;
  margin-top: 5px;
}

.user-name {
  padding: 5px 0;
  color: #f0f0f0;
  font-weight: bold;
}

.menu-divider {
  height: 1px;
  background-color: #4a55a0;
  margin: 5px 0;
}

.logout-button {
  width: 100%;
  padding: 8px;
  background-color: transparent;
  border: 1px solid #ff5555;
  color: #ff5555;
  border-radius: 4px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}

.logout-button:hover {
  background-color: rgba(255, 85, 85, 0.1);
}

.button-ripple {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle, rgba(255, 85, 85, 0.2) 0%, transparent 70%);
  transform: scale(0);
  opacity: 0;
  transition: transform 0.5s, opacity 0.5s;
}

.logout-button:hover .button-ripple {
  transform: scale(2);
  opacity: 1;
}

.auth-buttons {
  display: flex;
  gap: 10px;
}

.auth-button {
  padding: 8px 15px;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
  position: relative;
  overflow: hidden;
}

.login-button {
  background-color: #4a55a0;
  color: white;
  border: none;
}

.register-button {
  background-color: transparent;
  border: 1px solid #4a55a0;
  color: #7a85ff;
}

.button-glow {
  position: absolute;
  top: -10px;
  left: -10px;
  width: calc(100% + 20px);
  height: calc(100% + 20px);
  background: radial-gradient(circle, rgba(122, 133, 255, 0.4) 0%, transparent 70%);
  opacity: 0;
  transition: opacity 0.5s;
}

.auth-button:hover .button-glow {
  opacity: 1;
}

/* 模态框样式 */
.modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background-color: #0a0c1b;
  border: 1px solid #4a55a0;
  border-radius: 8px;
  padding: 20px;
  position: relative;
  width: 90%;
  max-width: 500px;
  box-shadow: 0 0 30px rgba(83, 100, 255, 0.4);
}

.close-button {
  position: absolute;
  top: 10px;
  right: 10px;
  background: none;
  border: none;
  color: #7a85ff;
  font-size: 24px;
  cursor: pointer;
}

.close-button:hover {
  color: #f0f0f0;
}

/* 其他现有样式保持不变 */
</style> 