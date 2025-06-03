<template>
  <div class="chat-container">
    <div class="cyber-grid"></div>
    
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
import { ref, onMounted, watch, nextTick } from 'vue'
import { chatWithManus } from '../services/api'
import AiAvatar from '../components/AiAvatar.vue'
import TheFooter from '../components/TheFooter.vue'

export default {
  name: 'ManusAppView',
  components: {
    AiAvatar,
    TheFooter
  },
  setup() {
    const inputMessage = ref('')
    const messages = ref([])
    const messagesContainer = ref(null)
    const isWaitingForResponse = ref(false)
    let chatConnection = null

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
      sendMessage
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
  background: linear-gradient(to bottom, transparent, rgba(59, 130, 246, 0.5), transparent);
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
  background-color: rgba(59, 130, 246, 0.3);
  border: 1px solid rgba(59, 130, 246, 0.5);
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
</style> 