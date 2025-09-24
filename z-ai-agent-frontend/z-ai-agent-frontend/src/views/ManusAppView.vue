<template>
  <div :class="containerClasses">
    <div class="cyber-grid"></div>

    <!-- 会话侧边栏 -->
    <ConversationSidebar
      v-if="isLoggedIn"
      aiType="manus"
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
        <div class="auth-modal" @click.stop>
          <button class="close-button" @click="closeModal">×</button>
          <AuthComponent
            :initial-tab="activeAuthTab"
            @login-success="handleLoginSuccess"
          />
        </div>
      </div>
    </teleport>

    <div class="chat-header">
      <div class="header-left">
        <button class="back-button" @click="goBack">
          <span class="back-icon">←</span>
          <span class="back-text">返回</span>
        </button>
      </div>
      <div class="header-center">
        <h1>AI超级智能体</h1>
        <p v-if="chatId">聊天ID: {{ chatId }}</p>
      </div>
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
          <!-- 修改这里，删除pre标签，直接使用div -->
          <div class="message-text" v-html="formatMessage(message.content)"></div>

          <span v-if="message.isTyping" class="typing-indicator">
            <span class="dot"></span>
            <span class="dot"></span>
            <span class="dot"></span>
          </span>
          
          <!-- 添加时间和删除按钮 -->
          <div class="message-footer">
            <span class="message-time" v-if="message.createdAt">
              {{ formatTime(message.createdAt) }}
            </span>
            <button 
              v-if="!isWaitingForResponse" 
              class="delete-btn" 
              @click="showDeleteConfirm(message.id, index)"
              title="删除消息"
            >
              <span>🗑️</span>
            </button>
          </div>
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

    <!-- 添加删除确认对话框 -->
    <teleport to="body">
      <div v-if="showDeleteModal" class="modal-backdrop" @click="closeDeleteModal">
        <div class="delete-confirm-modal" @click.stop>
          <div class="delete-confirm-header">确认删除</div>
          <div class="delete-confirm-content">确定要删除这条消息吗？此操作无法撤销。</div>
          <div class="delete-confirm-actions">
            <button class="cancel-btn" @click="closeDeleteModal">取消</button>
            <button class="confirm-btn" @click="confirmDelete">删除</button>
          </div>
        </div>
      </div>
    </teleport>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { chatWithManus, conversationApi, authApi } from '../services/api'
import AiAvatar from '../components/AiAvatar.vue'
import TheFooter from '../components/TheFooter.vue'
import ConversationSidebar from '../components/ConversationSidebar.vue'
import AuthComponent from '../components/AuthComponent.vue'
import { marked } from 'marked'
import hljs from 'highlight.js'
import 'highlight.js/styles/atom-one-dark.css'
import DOMPurify from 'dompurify'

export default {
  name: 'ManusAppView',
  components: {
    AiAvatar,
    TheFooter,
    ConversationSidebar,
    AuthComponent
  },
  setup() {
    const router = useRouter()
    const inputMessage = ref('')
    const messages = ref([])
    const messagesContainer = ref(null)
    const isWaitingForResponse = ref(false)
    let chatConnection = null
    let connectionRetries = 0
    const MAX_RETRIES = 3
    const chatId = ref('')

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
          localStorage.removeItem('manus_current_conversation_id')
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
        localStorage.removeItem('manus_current_conversation_id')
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

    // 创建初始会话
    const createInitialConversation = async () => {
      // 先尝试加载上次的会话
      const conversationLoaded = await loadCurrentConversation()

      // 如果没有加载到有效会话，则创建新会话
      if (!conversationLoaded) {
        try {
          // 创建新会话，注意type为manus
          const response = await conversationApi.createConversation('manus')
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
            id: msg.id,
            content: msg.content,
            isUser: msg.isUser,
            isTyping: false,
            createdAt: msg.createdAt
          })
        })

        // 如果没有历史消息，添加一条默认欢迎消息
        if (messages.value.length === 0) {
          messages.value.push({
            content: '你好，我是AI超级智能体，我可以协助你完成各种任务。有什么我能帮到你的吗？',
            isUser: false,
            isTyping: false,
            createdAt: new Date().toISOString()
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
        content: '你好，我是AI超级智能体，我可以协助你完成各种任务。有什么我能帮到你的吗？',
        isUser: false,
        isTyping: false,
        createdAt: new Date().toISOString()
      }]
    }

    // 本地存储相关逻辑
    const loadCurrentConversation = async () => {
      // 尝试从本地存储获取上次使用的会话ID
      const savedConversationId = localStorage.getItem('manus_current_conversation_id')

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
              id: msg.id,
              content: msg.content,
              isUser: msg.isUser,
              isTyping: false,
              createdAt: msg.createdAt
            })
          })

          // 设置聊天ID用于显示
          chatId.value = savedConversationId

          console.log('已恢复上次会话:', savedConversationId)
          return true
        } catch (error) {
          console.error('恢复会话失败，将创建新会话:', error)
          localStorage.removeItem('manus_current_conversation_id')
          return false
        }
      }
      return false
    }

    // 保存当前会话ID到本地存储
    const saveCurrentConversation = (conversationId) => {
      if (conversationId) {
        localStorage.setItem('manus_current_conversation_id', conversationId)
        console.log('已保存当前会话ID:', conversationId)
      }
    }

    // 改进的格式化消息函数，使用Markdown解析
    const formatMessage = (content) => {
      if (!content) return '';

      try {
        // 首先处理特殊字符串和多余的换行符
        content = content.replace(/\\n/g, '\n');
        content = content.replace(/\n{3,}/g, '\n\n'); // 将3个以上的连续换行符替换为2个

        // 确保代码块前后有空行，使其能正确识别
        content = content.replace(/```(\w+)?\s*\n([\s\S]*?)```/g, (match, language, code) => {
          // 如果没有指定语言，默认为plaintext
          const lang = language || 'plaintext';
          return `\n\`\`\`${lang}\n${code}\`\`\`\n`;
        });
        
        // 使用marked将Markdown转换为HTML
        const renderer = new marked.Renderer();
        
        // 定制代码块渲染
        renderer.code = function(code, language) {
          // 尝试使用指定的语言，如果不支持，fallback到自动检测
          const validLanguage = hljs.getLanguage(language) ? language : null;
          const highlighted = validLanguage 
            ? hljs.highlight(code, { language: validLanguage }).value 
            : hljs.highlightAuto(code).value;
          
          return `<pre><code class="hljs language-${language || 'plaintext'}">${highlighted}</code></pre>`;
        };

        marked.setOptions({
          renderer,
          highlight: function(code, lang) {
            if (lang && hljs.getLanguage(lang)) {
              return hljs.highlight(code, { language: lang }).value;
            } else {
              return hljs.highlightAuto(code).value;
            }
          },
          langPrefix: 'hljs language-',
          gfm: true,         // 启用GitHub风格的Markdown
          breaks: true,      // 启用回车换行
          pedantic: false,   // 不使用pedantic模式
          smartLists: true,  // 使用更智能的列表行为
          smartypants: false // 不使用"智能"排版标点
        });

        const htmlContent = marked(content);
        
        // 配置DOMPurify允许的标签和属性
        const purifyOptions = {
          ALLOWED_TAGS: ['h1', 'h2', 'h3', 'h4', 'h5', 'h6', 'blockquote', 'p', 'a', 'ul', 'ol', 
            'nl', 'li', 'b', 'i', 'strong', 'em', 'strike', 'code', 'hr', 'br', 'div', 
            'table', 'thead', 'caption', 'tbody', 'tr', 'th', 'td', 'pre', 'span', 'img'],
          ALLOWED_ATTR: ['href', 'name', 'target', 'class', 'id', 'style', 'src', 'alt'],
          ADD_ATTR: ['target'], // 允许添加target属性到链接
        };
        
        // 清理HTML并返回
        let sanitizedContent = DOMPurify.sanitize(htmlContent, purifyOptions);
        
        // 给所有链接添加target="_blank"属性，确保在新标签页中打开
        sanitizedContent = sanitizedContent.replace(/<a /g, '<a target="_blank" rel="noopener noreferrer" ');
        
        return sanitizedContent;
      } catch (error) {
        console.error('Markdown解析错误:', error);
        
        // 如果解析失败，应用基本的HTML转义和格式处理
        const escapedContent = content
          .replace(/&/g, '&amp;')
          .replace(/</g, '&lt;')
          .replace(/>/g, '&gt;')
          .replace(/"/g, '&quot;')
          .replace(/'/g, '&#039;')
          .replace(/\n/g, '<br>');
          
        return escapedContent;
      }
    };
    
    // 格式化时间
    const formatTime = (timestamp) => {
      if (!timestamp) return ''
      const date = new Date(timestamp)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      })
    }
    
    // 删除确认相关状态
    const showDeleteModal = ref(false)
    const pendingDeleteId = ref(null)
    const pendingDeleteIndex = ref(null)

    // 显示删除确认对话框
    const showDeleteConfirm = (messageId, index) => {
      pendingDeleteId.value = messageId
      pendingDeleteIndex.value = index
      showDeleteModal.value = true
    }

    // 关闭删除确认对话框
    const closeDeleteModal = () => {
      showDeleteModal.value = false
      pendingDeleteId.value = null
      pendingDeleteIndex.value = null
    }

    // 确认删除
    const confirmDelete = async () => {
      if (pendingDeleteIndex.value !== null) {
        try {
          // 如果有id才调用API删除
          if (pendingDeleteId.value) {
            await conversationApi.deleteMessage(pendingDeleteId.value)
          }
          // 无论是否有id，都从本地列表中删除
          messages.value.splice(pendingDeleteIndex.value, 1)
        } catch (error) {
          console.error('删除消息失败:', error)
        } finally {
          closeDeleteModal()
        }
      }
    }

    // 删除消息方法修改为显示确认框
    const deleteMessage = async (messageId, index) => {
      showDeleteConfirm(messageId, index)
    }

    // 解析工具使用信息
    const parseToolUsage = (content) => {
      // 检测是否包含工具调用信息
      if (content.includes('Step') && content.includes('工具：')) {
        const toolUsage = [];
        let currentToolIndex = -1;

        // 解析工具使用信息
        const lines = content.split('\n');
        for (let i = 0; i < lines.length; i++) {
          const line = lines[i];

          // 检测工具调用行
          if (line.includes('工具：')) {
            const toolMatch = line.match(/工具：(\w+)/);
            if (toolMatch) {
              const toolName = toolMatch[1];

              // 查找工具结果（通常在后面几行）
              let resultLines = [];
              let j = i + 1;
              while (j < lines.length && !lines[j].includes('Step') && !lines[j].includes('工具：')) {
                resultLines.push(lines[j]);
                j++;
              }

              toolUsage.push({
                toolName: toolName,
                result: resultLines.join('\n')
              });

              currentToolIndex = toolUsage.length - 1;
            }
          }
        }

        return {
          toolUsage,
          currentToolIndex
        };
      }

      return null;
    }

    // 改进sendMessage方法，添加缓冲区处理逻辑
    const sendMessage = () => {
      // 声明timeoutCheck变量在函数开头
      let timeoutCheck = null;
      let messageBuffer = ''; // 添加消息缓冲区

      if (!inputMessage.value.trim() || isWaitingForResponse.value) return;

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

      // 清除之前可能存在的超时检测
      if (timeoutCheck) {
        clearInterval(timeoutCheck);
        timeoutCheck = null;
      }

      // 添加用户消息
      const userMessage = inputMessage.value;
      messages.value.push({
        content: userMessage,
        isUser: true,
        isTyping: false,
        createdAt: new Date().toISOString() // 添加当前时间
      });

      // 清空输入框并设置等待状态
      inputMessage.value = '';
      isWaitingForResponse.value = true;

      // 关闭上一个连接（如果存在）
      if (chatConnection) {
        try {
          chatConnection.close();
        } catch (e) {
          console.error('关闭上一个连接时出错:', e);
        }
        chatConnection = null;
      }

      // 添加AI消息占位符
      messages.value.push({
        content: '',
        rawContent: '', // 添加原始内容存储
        isUser: false,
        isTyping: true,
        toolUsage: [],
        currentToolIndex: -1,
        createdAt: new Date().toISOString() // 添加当前时间
      });

      let aiResponseIndex = messages.value.length - 1;

      console.log('使用会话ID发送消息:', currentConversationId.value)

      // 建立新连接并发送消息 - 使用currentConversationId
      chatConnection = chatWithManus(
        userMessage,
        currentConversationId.value,
        (data) => {
          // 将数据添加到缓冲区
          messageBuffer += data;
          
          // 存储原始内容，用于最终处理
          messages.value[aiResponseIndex].rawContent = messageBuffer;
          
          // 对缓冲区内容进行预处理，减少过多的换行
          const processedContent = messageBuffer.replace(/\n{3,}/g, '\n\n');
          
          // 更新AI消息内容
          messages.value[aiResponseIndex].content = processedContent;
          messages.value[aiResponseIndex].isTyping = true;

          // 解析工具使用信息并更新
          const toolInfo = parseToolUsage(processedContent);
          if (toolInfo) {
            messages.value[aiResponseIndex].toolUsage = toolInfo.toolUsage;
            messages.value[aiResponseIndex].currentToolIndex = toolInfo.currentToolIndex;
          }

          // 重置任何可能存在的超时检测
          if (timeoutCheck) {
            clearInterval(timeoutCheck);
            timeoutCheck = null;
          }
        },
        (error) => {
          console.error('聊天出错:', error);
          // 只在真正错误的情况下才显示错误信息
          messages.value[aiResponseIndex].content = '抱歉，连接出现问题，请稍后再试。';
          messages.value[aiResponseIndex].isTyping = false;
          isWaitingForResponse.value = false;
          // 清理超时检测
          if (timeoutCheck) {
            clearInterval(timeoutCheck);
            timeoutCheck = null;
          }
        },
        () => {
          // 消息接收完成回调
          console.log('消息接收完成');
          messages.value[aiResponseIndex].isTyping = false;
          isWaitingForResponse.value = false;
          
          // 消息完全接收后，进行最终的格式优化处理
          if (messages.value[aiResponseIndex].rawContent) {
            const finalContent = messages.value[aiResponseIndex].rawContent.replace(/\n{3,}/g, '\n\n');
            messages.value[aiResponseIndex].content = finalContent;
          }
          
          // 清理超时检测
          if (timeoutCheck) {
            clearInterval(timeoutCheck);
            timeoutCheck = null;
          }
          chatConnection = null;
        }
      );

      // 改进超时检测机制 - 作为备用方案
      const checkMessageComplete = () => {
        if (chatConnection) {
          let lastContent = messages.value[aiResponseIndex].content;
          let noChangeCounter = 0;
          // 固定超时时间为3分钟(180秒)
          const TIMEOUT_SECONDS = 180;

          // 使用间隔检查
          timeoutCheck = setInterval(() => {
            // 检查连接是否已关闭
            if (!chatConnection) {
              clearInterval(timeoutCheck);
              timeoutCheck = null;
              return;
            }

            // 检查内容是否有变化
            if (lastContent === messages.value[aiResponseIndex].content) {
              noChangeCounter++;

              // 如果内容包含正在思考的提示，记录思考状态但不影响超时时间
              if (messages.value[aiResponseIndex].content.includes('Step') &&
                !messages.value[aiResponseIndex].content.includes('思考完成')) {
                // 显示思考状态处理
                if (noChangeCounter % 5 === 0) {
                  console.log('AI正在思考中...');
                }
              }

              // 如果达到固定的超时时间(3分钟)，则认为流已结束
              if (noChangeCounter >= TIMEOUT_SECONDS) {
                clearInterval(timeoutCheck);
                timeoutCheck = null;
                messages.value[aiResponseIndex].isTyping = false;
                isWaitingForResponse.value = false;
                if (chatConnection) {
                  chatConnection.close();
                  chatConnection = null;
                }
                console.log('SSE连接已超时(3分钟)，自动关闭');
              }
            } else {
              // 内容有变化，重置计数器
              lastContent = messages.value[aiResponseIndex].content;
              noChangeCounter = 0;
            }
          }, 1000);
        }
      };

      checkMessageComplete();
    };

    // 返回上一页或首页
    const goBack = () => {
      router.push('/')
    }

    // 初始化聊天
    onMounted(() => {
      // 配置marked以使用highlight.js进行代码高亮
      marked.setOptions({
        highlight: function(code, lang) {
          // 检查语言是否有效，确保代码块正确高亮
          try {
            if (lang && hljs.getLanguage(lang)) {
              return hljs.highlight(code, { language: lang }).value;
            } else {
              // 尝试自动检测语言
              return hljs.highlightAuto(code).value;
            }
          } catch (e) {
            console.error('代码高亮错误:', e);
            // 如果高亮失败，返回原代码，至少保证代码显示
            return code;
          }
        },
        langPrefix: 'hljs language-',
        gfm: true,            // 启用GitHub风格的Markdown
        breaks: true,         // 启用回车换行
        pedantic: false,      // 不使用pedantic模式
        smartLists: true,     // 使用更智能的列表行为
        smartypants: false,   // 不使用"智能"排版标点
        headerIds: false,     // 避免生成标题ID
        xhtml: false          // 不使用XHTML标签闭合格式
      });

      messages.value.push({
        content: '你好，我是AI超级智能体，我可以协助你完成各种任务。有什么我能帮到你的吗？',
        isUser: false,
        isTyping: false,
        createdAt: new Date().toISOString()
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

    return {
      inputMessage,
      messages,
      chatId,
      messagesContainer,
      isWaitingForResponse,
      sendMessage,
      formatMessage,
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
      dismissLoginNotice,
      formatTime,
      deleteMessage,
      goBack,
      // 添加新的返回属性
      showDeleteModal,
      showDeleteConfirm,
      closeDeleteModal,
      confirmDelete
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

/* 模态框背景 */
.modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.75);
  backdrop-filter: blur(5px);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  animation: fadeIn 0.3s ease-out;
}

/* 模态框内容容器 */
.auth-modal {
  position: relative;
  animation: modalSlideIn 0.4s ease-out;
}

/* 关闭按钮 */
.close-button {
  position: absolute;
  top: 15px;
  right: 15px;
  width: 36px;
  height: 36px;
  background-color: rgba(30, 31, 70, 0.7);
  border: 1px solid rgba(100, 100, 255, 0.3);
  border-radius: 50%;
  color: #ffffff;
  font-size: 24px;
  line-height: 32px;
  text-align: center;
  cursor: pointer;
  z-index: 1001;
  transition: all 0.3s ease;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.3);
}

.close-button:hover {
  background-color: rgba(255, 70, 70, 0.7);
  transform: rotate(90deg);
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes modalSlideIn {
  from { 
    opacity: 0;
    transform: translateY(-30px);
  }
  to { 
    opacity: 1;
    transform: translateY(0);
  }
}

/* 保留原有的样式 */
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
  background: linear-gradient(to bottom, transparent, rgba(59, 130, 246, 0.5), transparent);
  animation: grid-line-animation 15s infinite;
}

.chat-header {
  padding: 1rem 2rem;
  background-color: rgba(10, 15, 30, 0.8);
  border-bottom: 1px solid #4a55a0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  position: relative;
  z-index: 10;
}

.header-left {
  flex: 0 0 auto;
  margin-right: 20px;
}

.header-center {
  flex: 1;
  text-align: center;
}

.back-button {
  background: linear-gradient(135deg, #1e2a4a 0%, #2d3a65 100%);
  border: 1px solid #4a55a0;
  border-radius: 8px;
  color: #70f6ff;
  padding: 8px 16px;
  font-size: 0.9rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

.back-button:hover {
  background: linear-gradient(135deg, #2d3a65 0%, #3d4a75 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.5);
}

.back-icon {
  font-size: 1.2rem;
  margin-right: 6px;
}

.back-text {
  font-weight: bold;
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
  background: linear-gradient(to right, #1a56ff, #00c6ff);
  color: white;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.2s ease;
}

.chat-input button:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 198, 255, 0.4);
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
  padding: 12px;
  border-radius: 8px;
  max-width: 80%;
  line-height: 1.5;
  word-break: break-word;
  position: relative;
  overflow: hidden;
}

.message-content pre {
  white-space: pre-wrap;
  margin: 0;
  font-family: inherit;
  overflow-x: auto;
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
  background-color: rgba(59, 130, 246, 0.3);
  border: 1px solid rgba(59, 130, 246, 0.5);
  margin-left: 0.2rem;
}

/* 消息底部时间和删除按钮样式 */
.message-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
  padding-top: 4px;
  border-top: 1px dotted rgba(255, 255, 255, 0.1);
  font-size: 0.75rem;
  min-height: 20px; /* 固定最小高度，防止布局跳动 */
}

.message-time {
  color: rgba(255, 255, 255, 0.5);
  font-size: 0.7rem;
}

.delete-btn {
  background: none;
  border: none;
  color: rgba(255, 255, 255, 0.4);
  cursor: pointer;
  padding: 2px 5px;
  border-radius: 3px;
  transition: all 0.2s ease;
  font-size: 0.8rem;
  opacity: 0; /* 默认隐藏 */
  margin-left: auto; /* 保持右对齐 */
}

.message-content:hover .delete-btn {
  opacity: 1; /* 鼠标悬停时显示 */
}

.delete-btn:hover {
  color: #ff4d4f;
  background-color: rgba(255, 77, 79, 0.1);
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

/* 原有的链接和代码块样式 */
:deep(.highlight-link) {
  color: #4ade80;
  text-decoration: underline;
  font-weight: 500;
  transition: color 0.3s ease;
}

:deep(.highlight-link:hover) {
  color: #22c55e;
  text-decoration: none;
}

:deep(.result-label) {
  color: #fbbf24;
  font-weight: 600;
}

:deep(.code-block) {
  background-color: rgba(30, 41, 59, 0.7);
  border: 1px solid rgba(100, 116, 139, 0.5);
  border-radius: 6px;
  padding: 0.75rem;
  margin: 0.5rem 0;
  overflow-x: auto;
  font-family: 'Courier New', monospace;
  white-space: pre-wrap;
}

:deep(strong) {
  color: #60a5fa;
}

/* 使用pre标签样式，但保持良好的外观 */
.message-text {
  white-space: pre-wrap !important;
  word-break: break-word !important;
  font-family: inherit !important;
  margin: 0 !important;
  width: 100%;
  overflow: visible !important;
  background: transparent !important;
}

/* 确保链接样式正确 */
:deep(a) {
  color: #70f6ff !important;
  text-decoration: underline !important;
  word-break: break-all !important;
}

:deep(a:hover) {
  color: #a0f8ff !important;
}

/* 添加链接样式 */
.message-text a {
  color: #70f6ff;
  text-decoration: underline;
  word-break: break-all;
}

.message-text a:hover {
  color: #a0f8ff;
  text-decoration: underline;
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

  .chat-container.sidebar-collapsed {
    padding-left: 0;
  }
}

/* 删除确认对话框样式 */
.delete-confirm-modal {
  background: linear-gradient(135deg, rgba(20, 21, 46, 0.95), rgba(16, 17, 38, 0.95));
  border: 1px solid rgba(100, 100, 255, 0.3);
  border-radius: 12px;
  width: 300px;
  padding: 20px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.6);
  animation: modalSlideIn 0.4s ease-out;
}

.delete-confirm-header {
  font-size: 1.2rem;
  color: #ff4d4f;
  font-weight: bold;
  margin-bottom: 15px;
  text-align: center;
}

.delete-confirm-content {
  margin-bottom: 20px;
  text-align: center;
  color: #f0f0f0;
  line-height: 1.5;
}

.delete-confirm-actions {
  display: flex;
  justify-content: center;
  gap: 15px;
}

.cancel-btn, .confirm-btn {
  padding: 8px 20px;
  border: none;
  border-radius: 50px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s ease;
}

.cancel-btn {
  background: rgba(255, 255, 255, 0.2);
  color: white;
}

.confirm-btn {
  background: linear-gradient(90deg, #ff006a 30%, #ff4b8b 100%);
  color: white;
}

.cancel-btn:hover, .confirm-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.3);
}

/* 修改Markdown样式 */
:deep(.message-text) {
  white-space: pre-wrap !important;
  word-break: break-word !important;
  font-family: inherit !important;
  margin: 0 !important;
  width: 100%;
  overflow: visible !important;
  background: transparent !important;
  line-height: 1.4;  /* 稍微增加行高以提高可读性 */
}

/* 修正pre和code标签 */
:deep(pre) {
  margin: 0.5em 0;
  padding: 0;
  background: transparent;
  overflow-x: auto;
  white-space: pre;
}

:deep(code) {
  background-color: rgba(30, 41, 59, 0.7);
  color: #f59e0b;
  padding: 0.2em 0.4em;
  border-radius: 3px;
  font-family: 'Courier New', Courier, monospace;
  font-size: 0.9em;
  white-space: pre;
}

:deep(pre > code) {
  display: block;
  padding: 1em;
  background-color: rgba(30, 41, 59, 0.9);
  border: 1px solid rgba(100, 116, 139, 0.7);
  border-radius: 6px;
  color: #e5e7eb;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  line-height: 1.4;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.3);
  margin: 0.4em 0;
  white-space: pre;
  overflow-x: auto;
}

/* 调整段落间距 */
:deep(p) {
  margin-top: 0.2em;
  margin-bottom: 0.2em;
}

/* 减少换行的垂直空间 */
:deep(br) {
  content: "";
  display: block;
  margin: 0;
  padding: 0;
  height: 0;  /* 彻底移除高度 */
  line-height: 0;
}

/* 标题样式 */
:deep(h1) {
  font-size: 1.8em;
  margin-top: 0.6em;
  margin-bottom: 0.3em;
  color: #70f6ff;
  line-height: 1.2;
}

:deep(h2) {
  font-size: 1.5em;
  margin-top: 0.6em;
  margin-bottom: 0.3em;
  color: #60a5fa;
  line-height: 1.2;
}

:deep(h3) {
  font-size: 1.3em;
  margin-top: 0.6em;
  margin-bottom: 0.3em;
  color: #34d399;
  line-height: 1.2;
}

:deep(h4), :deep(h5), :deep(h6) {
  margin-top: 0.6em;
  margin-bottom: 0.3em;
  line-height: 1.2;
}

/* 链接样式 */
:deep(a) {
  color: #70f6ff !important;
  text-decoration: underline !important;
  word-break: break-all !important;
}

:deep(a:hover) {
  color: #a0f8ff !important;
}

/* 列表样式 */
:deep(ul), :deep(ol) {
  margin-top: 0.3em;
  margin-bottom: 0.3em;
  padding-left: 1.5em;
}

:deep(li) {
  margin-bottom: 0.1em;
  line-height: 1.2;
}

/* 表格样式 */
:deep(table) {
  border-collapse: collapse;
  width: 100%;
  margin: 0.3em 0;
  background-color: rgba(30, 41, 59, 0.4);
}

:deep(th), :deep(td) {
  border: 1px solid rgba(100, 116, 139, 0.5);
  padding: 6px;
  text-align: left;
}

:deep(th) {
  background-color: rgba(30, 41, 59, 0.7);
  color: #60a5fa;
}

/* 引用样式 */
:deep(blockquote) {
  border-left: 3px solid #60a5fa;
  padding-left: 1em;
  margin: 0.3em 0;
  color: #94a3b8;
}

:deep(blockquote p) {
  margin-top: 0.1em;
  margin-bottom: 0.1em;
}

/* 水平线样式 */
:deep(hr) {
  border: none;
  height: 1px;
  background: linear-gradient(to right, transparent, rgba(100, 100, 255, 0.3), transparent);
  margin: 0.4em 0;
}

/* 强调样式 */
:deep(strong) {
  color: #60a5fa;
  font-weight: bold;
}

:deep(em) {
  color: #34d399;
  font-style: italic;
}

/* 语法高亮颜色增强 */
:deep(.hljs-keyword) {
  color: #c792ea; /* 关键字 */
}

:deep(.hljs-string) {
  color: #c3e88d; /* 字符串 */
}

:deep(.hljs-number) {
  color: #f78c6c; /* 数值 */
}

:deep(.hljs-function) {
  color: #82aaff; /* 函数 */
}

:deep(.hljs-comment) {
  color: #676e95; /* 注释 */
}

:deep(.hljs-attr) {
  color: #ffcb6b; /* 属性 */
}

:deep(.hljs-variable) {
  color: #f07178; /* 变量 */
}

:deep(.hljs-title) {
  color: #82aaff; /* 标题 */
}

:deep(.hljs-class) {
  color: #ffcb6b; /* 类 */
}

:deep(.hljs-tag) {
  color: #f07178; /* 标签 */
}
</style>
