，而<template>
  <div :class="containerClasses">
    <div class="cyber-grid"></div>

    <!-- 会话侧边栏 -->
    <ConversationSidebar
      v-if="isLoggedIn"
      aiType="love-app"
      :initialConversationId="currentConversationId"
      :isWaitingForResponse="isWaitingForResponse"
      @conversation-selected="handleConversationSelected"
      @conversation-created="handleConversationCreated"
      @sidebar-toggle="updateSidebarState"
    />

    <!-- 未登录提示 -->
    <div v-if="!isLoggedIn && showLoginNotice" class="login-notice">
      <div class="notice-content">
        <div class="notice-title">需要登录</div>
        <div class="notice-text">请先登录以使用会话管理功能</div>
        <div class="notice-actions">
          <button @click="showLoginForm" class="login-btn">登录</button>
          <button @click="showRegisterForm" class="register-btn">注册</button>
        </div>
        <button @click="dismissLoginNotice" class="dismiss-btn">稍后再说</button>
      </div>
    </div>

    <!-- 添加页面右上角的用户登录状态和头像 -->
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
            :initial-tab="activeAuthTab"
            @login-success="handleLoginSuccess"
          />
        </div>
      </div>
    </teleport>

    <div class="chat-header">
      <h1>AI恋爱大师</h1>
      <p v-if="chatId">聊天ID: {{ chatId }}</p>
    </div>

    <div class="chat-messages" ref="messagesContainer">
      <div
        v-for="(message, index) in messages"
        :key="index"
        :class="['message', message.isUser ? 'message-user' : 'message-ai']"
      >
        <div v-if="!message.isUser" class="message-avatar">
          <AiAvatar type="love" />
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
// 使用命名导入方式
import { chatWithLoveApp, generateChatId, conversationApi, authApi } from '@/services/api'
import AiAvatar from '../components/AiAvatar.vue'
import TheFooter from '../components/TheFooter.vue'
import ConversationSidebar from '../components/ConversationSidebar.vue'
import AuthComponent from '../components/AuthComponent.vue'

export default {
  name: 'LoveAppView',
  components: {
    AiAvatar,
    TheFooter,
    ConversationSidebar,
    AuthComponent
  },
  setup() {
    const inputMessage = ref('')
    const messages = ref([])
    const chatId = ref('')
    const messagesContainer = ref(null)
    const isWaitingForResponse = ref(false)
    let chatConnection = null

    // 侧边栏折叠状态
    const sidebarCollapsed = ref(false)

    // 监听侧边栏折叠状态变化
    const updateSidebarState = (collapsed) => {
      sidebarCollapsed.value = collapsed
    }

    // 会话管理相关状态
    const currentConversationId = ref(null)

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
              return true
            } else {
              // token无效
              localStorage.removeItem('Authorization')
              isLoggedIn.value = false
              showLoginNotice.value = true
              return false
            }
          } else {
            // token无效
            localStorage.removeItem('Authorization')
            isLoggedIn.value = false
            showLoginNotice.value = true
            return false
          }
        } catch (error) {
          console.error('检查登录状态失败', error)
          localStorage.removeItem('Authorization')
          isLoggedIn.value = false
          showLoginNotice.value = true
          return false
        }
      } else {
        showLoginNotice.value = true
        return false
      }
    }

    // 登录成功处理
    const handleLoginSuccess = (userData) => {
      isLoggedIn.value = true
      username.value = userData.username
      closeModal()

      // 登录成功后创建初始会话或恢复上次会话
      createInitialConversation()
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
          // 清除保存的会话ID
          localStorage.removeItem('loveapp_current_conversation_id')
          isLoggedIn.value = false
          username.value = ''
          // 清空当前会话
          currentConversationId.value = null
          chatId.value = ''
          showLoginNotice.value = true
        }
      } else {
        localStorage.removeItem('Authorization')
        // 清除保存的会话ID
        localStorage.removeItem('loveapp_current_conversation_id')
        isLoggedIn.value = false
        username.value = ''
        // 清空当前会话
        currentConversationId.value = null
        chatId.value = ''
        showLoginNotice.value = true
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
        content: '你好，我是AI恋爱大师，很高兴为你提供情感咨询和恋爱建议。请告诉我你想了解的问题？',
        isUser: false,
        isTyping: false
      })

      // 创建网格背景效果
      initCyberGrid();

      // 设置页面标题和元数据
      document.title = 'AI恋爱大师 - 您的专属情感顾问'

      // 创建meta描述标签
      if (!document.querySelector('meta[name="description"]')) {
        const metaDesc = document.createElement('meta')
        metaDesc.name = 'description'
        metaDesc.content = 'AI恋爱大师为您提供专业的情感咨询和恋爱建议，解决您的感情困惑。'
        document.head.appendChild(metaDesc)
      }

      // 检查用户登录状态
      checkLoginStatus().then(() => {
        // 只有登录后才创建会话
        if (isLoggedIn.value) {
          // 自动创建一个新的会话或恢复上次会话
          createInitialConversation()
        } else {
          // 未登录提示
          console.log('用户未登录，请先登录')
          showLoginForm()
        }
      })
    })

    // 创建初始会话
    const createInitialConversation = async () => {
      // 先尝试加载上次的会话
      const conversationLoaded = await loadCurrentConversation()

      // 如果没有加载到有效会话，则创建新会话
      if (!conversationLoaded) {
        try {
          // 创建新会话
          const response = await conversationApi.createConversation('love-app')
          currentConversationId.value = response.data.id

          // 保存到本地存储
          saveCurrentConversation(currentConversationId.value)

          // 设置聊天ID用于显示
          chatId.value = currentConversationId.value

          console.log('已创建新会话:', currentConversationId.value)
        } catch (error) {
          console.error('创建初始会话失败:', error)
        }
      }
    }

    // 处理选择会话
    const handleConversationSelected = async (conversation) => {
      try {
        if (currentConversationId.value === conversation.id) {
          return // 已经是当前会话，无需切换
        }

        // 更新当前会话ID
        currentConversationId.value = conversation.id

        // 保存到本地存储
        saveCurrentConversation(currentConversationId.value)

        // 设置聊天ID用于显示
        chatId.value = currentConversationId.value

        // 加载会话消息历史
        const response = await conversationApi.getConversationMessages(conversation.id)

        // 清空当前消息列表
        messages.value = []

        // 将历史消息按正确格式添加到消息列表
        const historyMessages = response.data.messages
        historyMessages.forEach(msg => {
          messages.value.push({
            content: msg.content,
            isUser: msg.isUser,
            isTyping: false
          })
        })

        // 如果没有历史消息，添加一条默认欢迎消息
        if (messages.value.length === 0) {
          messages.value.push({
            content: '你好，我是AI恋爱大师，很高兴为你提供情感咨询和恋爱建议。请告诉我你想了解的问题？',
            isUser: false,
            isTyping: false
          })
        }
      } catch (error) {
        console.error('加载会话消息失败:', error)
      }
    }

    // 处理创建会话
    const handleConversationCreated = (conversation) => {
      currentConversationId.value = conversation.id

      // 保存到本地存储
      saveCurrentConversation(currentConversationId.value)

      // 设置聊天ID用于显示
      chatId.value = currentConversationId.value

      // 清空当前消息列表，添加默认欢迎消息
      messages.value = [{
        content: '你好，我是AI恋爱大师，很高兴为你提供情感咨询和恋爱建议。请告诉我你想了解的问题？',
        isUser: false,
        isTyping: false
      }]
    }

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

      // 检查是否有有效的会话ID
      if (!currentConversationId.value) {
        console.error('没有有效的会话ID，无法发送消息')
        // 尝试创建新会话
        createInitialConversation().then(() => {
          // 递归调用自身，此时应该有会话ID了
          setTimeout(() => sendMessage(), 500)
        })
        return
      }

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

      console.log('使用会话ID发送消息:', currentConversationId.value)

      // 建立新连接并发送消息 - 使用currentConversationId
      chatConnection = chatWithLoveApp(
        userMessage,
        currentConversationId.value,
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

    // 在 setup 函数中添加本地存储相关逻辑
    const loadCurrentConversation = async () => {
      // 尝试从本地存储获取上次使用的会话ID
      const savedConversationId = localStorage.getItem('loveapp_current_conversation_id')

      if (savedConversationId) {
        try {
          // 验证会话是否存在
          const response = await conversationApi.getConversationMessages(savedConversationId)

          // 如果会话存在，设置为当前会话
          currentConversationId.value = savedConversationId

          // 加载历史消息
          messages.value = []
          const historyMessages = response.data.messages
          historyMessages.forEach(msg => {
            messages.value.push({
              content: msg.content,
              isUser: msg.isUser,
              isTyping: false
            })
          })

          // 设置聊天ID用于显示
          chatId.value = savedConversationId

          console.log('已恢复上次会话:', savedConversationId)
          return true
        } catch (error) {
          console.error('恢复会话失败，将创建新会话:', error)
          localStorage.removeItem('loveapp_current_conversation_id')
          return false
        }
      }
      return false
    }

    // 保存当前会话ID到本地存储
    const saveCurrentConversation = (conversationId) => {
      if (conversationId) {
        localStorage.setItem('loveapp_current_conversation_id', conversationId)
        console.log('已保存当前会话ID:', conversationId)
      }
    }

    return {
      inputMessage,
      messages,
      chatId,
      messagesContainer,
      isWaitingForResponse,
      sendMessage,
      currentConversationId,
      handleConversationSelected,
      handleConversationCreated,
      // 侧边栏状态
      sidebarCollapsed,
      updateSidebarState,
      containerClasses,
      // 用户登录相关
      isLoggedIn,
      username,
      usernameInitial,
      showAuthModal,
      activeAuthTab,
      showLoginForm,
      showRegisterForm,
      closeModal,
      handleLogout,
      handleLoginSuccess,
      showLoginNotice,
      dismissLoginNotice
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
  padding-left: 280px; /* 修改为默认展开状态的侧边栏宽度 */
}

.chat-container.sidebar-collapsed {
  padding-left: 30px; /* 折叠状态下的宽度 */
}

/* 未登录提示框样式 */
.login-notice {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 300;
  width: 350px;
  background-color: rgba(20, 21, 46, 0.95);
  border: 1px solid #4a55a0;
  border-radius: 12px;
  box-shadow: 0 0 30px rgba(83, 100, 255, 0.5);
  padding: 20px;
  animation: notice-appear 0.3s ease-out;
  backdrop-filter: blur(10px);
}

@keyframes notice-appear {
  from {
    opacity: 0;
    transform: translate(-50%, -60%);
  }
  to {
    opacity: 1;
    transform: translate(-50%, -50%);
  }
}

.notice-content {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.notice-title {
  color: #70f6ff;
  font-size: 1.4rem;
  font-weight: bold;
  margin-bottom: 10px;
  text-shadow: 0 0 5px rgba(112, 246, 255, 0.5);
}

.notice-text {
  color: #fff;
  font-size: 1rem;
  margin-bottom: 20px;
  text-align: center;
}

.notice-actions {
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-bottom: 15px;
  width: 100%;
}

.login-btn, .register-btn {
  padding: 8px 20px;
  border: none;
  border-radius: 50px;
  color: white;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s ease;
}

.login-btn {
  background: linear-gradient(90deg, #1a56ff 30%, #00c6ff 100%);
}

.register-btn {
  background: linear-gradient(90deg, #ff006a 30%, #ff4b8b 100%);
}

.login-btn:hover, .register-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.3);
}

.dismiss-btn {
  background: none;
  border: none;
  color: #b5beff;
  margin-top: 10px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: color 0.2s;
  text-decoration: underline;
}

.dismiss-btn:hover {
  color: #fff;
}

/* 右上角导航栏样式 */
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

.auth-buttons {
  display: flex;
  gap: 1rem;
}

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

.login-button {
  background: linear-gradient(90deg, #1a56ff 30%, #00c6ff 100%);
}

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
  z-index: 101;
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

@keyframes buttonGlow {
  0% { left: -100%; }
  100% { left: 200%; }
}

@keyframes ripple {
  0% { left: -100%; }
  100% { left: 100%; }
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

.grid-line {
  position: absolute;
  top: 0;
  width: 1px;
  background: linear-gradient(to bottom, transparent, rgba(243, 82, 88, 0.5), transparent);
  animation: grid-line-animation 15s infinite;
}

.chat-header {
  text-align: center;
  padding: 1rem;
  position: relative;
  z-index: 1;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 1rem;
  position: relative;
  z-index: 1;
}

.chat-input {
  display: flex;
  padding: 1rem;
  background-color: rgba(20, 20, 40, 0.5);
  position: relative;
  z-index: 1;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.chat-input input {
  flex: 1;
  padding: 0.75rem;
  border-radius: 4px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  background-color: rgba(30, 30, 50, 0.6);
  color: white;
  margin-right: 0.5rem;
}

.chat-input button {
  padding: 0.75rem 1.5rem;
  border-radius: 4px;
  border: none;
  background: linear-gradient(to right, #ff006a, #ff4b8b);
  color: white;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.2s ease;
}

.chat-input button:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(255, 0, 106, 0.4);
}

@keyframes grid-line-animation {
  0% {
    transform: translateY(-100%);
    opacity: 0;
  }
  10% {
    opacity: 1;
  }
  90% {
    opacity: 1;
  }
  100% {
    transform: translateY(100%);
    opacity: 0;
  }
}

.message {
  display: flex;
  margin: 1.5rem 0;
  align-items: flex-start;
  padding: 0;
  background: transparent;
}

.message-avatar {
  width: 40px;
  height: 40px;
  flex-shrink: 0;
  margin: 0 0.7rem;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  top: 0.5rem;
}

.message-content {
  padding: 1rem;
  border-radius: 12px;
  max-width: 70%;
  line-height: 1.5;
  position: relative;
  white-space: pre-wrap;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.message-user {
  flex-direction: row-reverse;
  justify-content: flex-start;
  background: transparent;
}

.message-ai {
  justify-content: flex-start;
  background: transparent;
}

.message-user .message-content {
  background-color: rgba(83, 100, 255, 0.3);
  border: 1px solid rgba(83, 100, 255, 0.5);
  margin-right: 0.2rem;
}

.message-ai .message-content {
  background-color: rgba(255, 100, 100, 0.3);
  border: 1px solid rgba(255, 100, 100, 0.5);
  margin-left: 0.2rem;
}

.typing-indicator {
  display: inline-flex;
  align-items: center;
  height: 20px;
  margin-left: 5px;
}

.typing-indicator .dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: #fff;
  opacity: 0.7;
  margin: 0 2px;
  animation: typingAnimation 1.4s infinite ease-in-out;
}

.typing-indicator .dot:nth-child(1) {
  animation-delay: 0s;
}

.typing-indicator .dot:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator .dot:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typingAnimation {
  0%, 100% {
    transform: scale(0.7);
    opacity: 0.5;
  }
  50% {
    transform: scale(1.2);
    opacity: 1;
  }
}

/* 禁用状态的按钮样式 */
button:disabled {
  background-color: #a8a8a8;
  cursor: not-allowed;
}

input:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
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

  .chat-container {
    padding-left: 0;
  }
}
</style>
