import axios from 'axios'

// 使用相对路径，避免跨域问题
const API_URL = '/api'

// 创建一个带有拦截器的 axios 实例
const apiClient = axios.create({
  baseURL: API_URL,
  withCredentials: true, // 确保跨域请求发送cookies
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
apiClient.interceptors.request.use(
  config => {
    const token = localStorage.getItem('Authorization')
    if (token) {
      // 直接使用token，不添加Bearer前缀
      config.headers['Authorization'] = token
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
apiClient.interceptors.response.use(
  response => {
    return response
  },
  error => {
    // 处理401未授权错误
    if (error.response && error.response.status === 401) {
      // 清除token并重定向到登录页
      localStorage.removeItem('Authorization')
      // 可以在这里添加重定向逻辑
    }
    return Promise.reject(error)
  }
)

// 创建一个随机的聊天ID
export const generateChatId = () => {
  return Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15)
}

// 用户认证相关接口
const authApi = {
  // 用户登录
  login: async (username, password) => {
    try {
      const response = await apiClient.post('/user/login', {
        username,
        password
      })

      // 检查响应结构
      if (response.data && response.data.code === 0) {
        const authToken = response.data.data?.Authorization

        if (authToken) {
          // 保存token到localStorage
          localStorage.setItem('Authorization', authToken)

          // 设置axios默认headers，用于后续请求
          axios.defaults.headers.common['Authorization'] = authToken
        }
      }

      return response.data
    } catch (error) {
      console.error('登录请求失败:', error)
      throw error
    }
  },

  // 用户注册 - 使用apiClient
  register: async (username, password) => {
    try {
      const response = await apiClient.post('/user/register', {
        username,
        password
      })
      return response.data
    } catch (error) {
      console.error('注册请求失败:', error)
      throw error.response?.data || error
    }
  },

  // 检查登录状态
  isLogin: async () => {
    try {
      const response = await apiClient.get('/user/isLogin')
      return response.data
    } catch (error) {
      console.error('检查登录状态失败:', error)
      throw error
    }
  },

  // 获取用户信息
  getUserInfo: async () => {
    try {
      const response = await apiClient.get('/user/info')
      return response.data
    } catch (error) {
      console.error('获取用户信息失败:', error)
      throw error
    }
  },

  // 登出
  logout: async () => {
    try {
      const response = await apiClient.post('/user/logout')
      return response.data
    } catch (error) {
      console.error('登出失败:', error)
      throw error
    }
  }
}

// 会话管理相关接口
const conversationApi = {
  // 创建新会话
  createConversation: async (aiType, firstMessage = null, groupId = null) => {
    try {
      let url = `/conversation/create?aiType=${encodeURIComponent(aiType)}`

      if (firstMessage) {
        url += `&firstMessage=${encodeURIComponent(firstMessage)}`
      }

      if (groupId) {
        url += `&groupId=${groupId}`
      }

      // 使用apiClient而不是axios，确保带上token
      const response = await apiClient.post(url)
      return response.data
    } catch (error) {
      console.error('创建会话失败:', error)
      throw error.response?.data || error
    }
  },

  // 获取用户的会话列表
  getConversations: async (page = 1, size = 10, groupId = null, aiType = null) => {
    try {
      let url = `/conversation/list?page=${page}&size=${size}`

      if (groupId !== null) {
        url += `&groupId=${groupId}`
      }

      if (aiType !== null) {
        url += `&aiType=${encodeURIComponent(aiType)}`
      }

      // 使用apiClient而不是axios，确保带上token
      const response = await apiClient.get(url)
      return response.data
    } catch (error) {
      console.error('获取会话列表失败:', error)
      throw error.response?.data || error
    }
  },

  // 获取会话消息历史
  getConversationMessages: async (conversationId) => {
    try {
      if (!conversationId) {
        throw new Error('无效的会话ID')
      }

      const response = await apiClient.get(`/conversation/${conversationId}/messages`)
      return response.data
    } catch (error) {
      console.error('获取会话消息历史失败:', error)
      throw error.response?.data || error
    }
  },

  // 更新会话信息（标题和分组）
  updateConversation: async (conversationId, title, groupId = null) => {
    try {
      const response = await apiClient.post(`/conversation/update`, {
        id: conversationId,
        title,
        groupId
      })
      return response.data
    } catch (error) {
      console.error('更新会话信息失败:', error)
      throw error.response?.data || error
    }
  },

  // 删除会话
  deleteConversation: async (conversationId) => {
    try {
      const response = await apiClient.delete(`/conversation/${conversationId}`)
      return response.data
    } catch (error) {
      console.error('删除会话失败:', error)
      throw error.response?.data || error
    }
  },

  // 保存会话消息
  saveMessage: async (conversationId, userMessage, aiResponse, aiType) => {
    try {
      const response = await apiClient.post(`/message/save`, {
        conversationId,
        userMessage,
        aiResponse,
        aiType
      })
      return response.data
    } catch (error) {
      console.error('保存会话消息失败:', error)
      throw error.response?.data || error
    }
  }
}

// 会话分组相关接口
const conversationGroupApi = {
  // 创建会话分组
  createGroup: async (name, aiType = null) => {
    try {
      const response = await apiClient.post(`/conversation/group/create`, {
        name,
        aiType
      })
      return response.data
    } catch (error) {
      console.error('创建会话分组失败:', error)
      throw error.response?.data || error
    }
  },

  // 获取用户的所有会话分组
  getGroups: async (aiType = null) => {
    try {
      let url = `/conversation/group/list`;

      // 如果指定了aiType，添加到请求参数
      if (aiType !== null) {
        url += `?aiType=${encodeURIComponent(aiType)}`;
      }

      const response = await apiClient.get(url)
      return response.data
    } catch (error) {
      console.error('获取会话分组列表失败:', error)
      throw error.response?.data || error
    }
  },

  // 更新会话分组
  updateGroup: async (groupId, name) => {
    try {
      const response = await apiClient.post(`/conversation/group/update`, {
        id: groupId,
        name
      })
      return response.data
    } catch (error) {
      console.error('更新会话分组失败:', error)
      throw error.response?.data || error
    }
  },

  // 删除会话分组
  deleteGroup: async (groupId) => {
    try {
      const response = await apiClient.delete(`/conversation/group/${groupId}`)
      return response.data
    } catch (error) {
      console.error('删除会话分组失败:', error)
      throw error.response?.data || error
    }
  }
}

// 爱情应用聊天接口 - SSE方式使用Fetch API实现
export const chatWithLoveApp = (message, conversationId, onMessage, onError, onComplete) => {
  // 检查conversationId是否有效
  if (!conversationId) {
    console.error('无效的会话ID:', conversationId)
    if (onError) onError(new Error('无效的会话ID'))
    if (onComplete) onComplete()
    return { close: () => {} }
  }

  // 构建URL
  let url = `${API_URL}/ai/love_app/chat/sse?message=${encodeURIComponent(message)}&conversationId=${encodeURIComponent(conversationId)}`

  console.log('连接SSE URL:', url)

  // 获取token
  const token = localStorage.getItem('Authorization')

  // 控制变量
  let isCompleted = false
  let messageBuffer = ''
  let partialLine = '' // 用于处理不完整的行
  let lastReceiveTime = Date.now()
  let abortController = new AbortController()
  let timeoutId = null

  const completeConnection = () => {
    if (!isCompleted) {
      isCompleted = true

      // 清除所有超时计时器
      if (timeoutId) {
        clearTimeout(timeoutId)
        timeoutId = null
      }

      // 立即中止fetch请求，确保连接关闭
      try {
        abortController.abort()
      } catch (e) {
        console.error('中止连接时出错:', e)
      }

      // 延迟调用onComplete，确保资源已清理
      setTimeout(() => {
        if (onComplete) onComplete()
      }, 50)
    }
  }

  // 使用Fetch API替代EventSource
  fetch(url, {
    method: 'GET',
    headers: {
      'Accept': 'text/event-stream',
      'Cache-Control': 'no-cache',
      'Authorization': token
    },
    credentials: 'include',
    signal: abortController.signal
  })
  .then(response => {
    if (!response.ok) {
      throw new Error(`HTTP error! Status: ${response.status}`)
    }

    // 获取response的reader
    const reader = response.body.getReader()
    const decoder = new TextDecoder()

    // 递归读取流数据
    const readStream = () => {
      reader.read().then(({ done, value }) => {
        if (done) {
          console.log('SSE流已结束')
          // 处理最后可能的不完整行
          if (partialLine) {
            const dataMatch = partialLine.match(/data:(.*)/i)
            if (dataMatch && dataMatch[1]) {
              const data = dataMatch[1].trim()
              messageBuffer += data
              onMessage(data)
            }
          }
          completeConnection()
          return
        }

        lastReceiveTime = Date.now()
        const chunk = decoder.decode(value, { stream: true })

        // 将新块与之前的不完整行合并
        const fullText = partialLine + chunk

        // 按SSE格式分割消息（空行分隔）
        const events = fullText.split('\n\n')

        // 保存最后一个可能不完整的部分
        partialLine = events.pop() || ''

        // 处理完整的事件
        for (const event of events) {
          // 检查是否是注释（心跳消息）
          if (event.startsWith(':')) {
            console.log('收到心跳消息:', event)
            continue
          }

          // 提取data字段值
          const dataMatch = event.match(/data:(.*)/i)
          if (dataMatch && dataMatch[1]) {
            const data = dataMatch[1].trim()
            messageBuffer += data
            onMessage(data)
          }
        }

        // 如果未完成，继续读取
        if (!isCompleted) {
          readStream()
        }
      }).catch(error => {
        // 只处理非中止错误
        if (error.name !== 'AbortError' && !isCompleted) {
          console.error('SSE读取错误:', error)
          onError(error)
          completeConnection()
        }
      })
    }

    // 开始读取
    readStream()
  })
  .catch(error => {
    if (!isCompleted && error.name !== 'AbortError') {
      console.error('SSE连接错误:', error)
      onError(error)
      completeConnection()
    }
  })

  // 添加超时检测，如果长时间没有数据，自动关闭连接
  timeoutId = setTimeout(() => {
    if (!isCompleted) {
      console.log('SSE连接超时，自动关闭')
      completeConnection()
    }
  }, 30000) // 30秒超时

  // 返回一个可以关闭连接的对象
  return {
    close: () => {
      console.log('手动关闭SSE连接')
      completeConnection()
    }
  }
}

// 使用服务器发送事件(SSE)流式获取Manus AI回复
export const chatWithManus = (message, conversationId, onMessage, onError, onComplete) => {
  // 检查conversationId是否有效
  if (!conversationId) {
    console.error('无效的会话ID:', conversationId)
    if (onError) onError(new Error('无效的会话ID'))
    if (onComplete) onComplete()
    return { close: () => {} }
  }

  // 构建URL
  let url = `${API_URL}/ai/manus/chat/sse?message=${encodeURIComponent(message)}&conversationId=${encodeURIComponent(conversationId)}`

  console.log('连接Manus SSE URL:', url)

  // 获取token
  const token = localStorage.getItem('Authorization')

  // 控制变量
  let isCompleted = false
  let messageBuffer = ''
  let partialLine = '' // 用于处理不完整的行
  let lastReceiveTime = Date.now()
  let abortController = new AbortController()
  let timeoutId = null
  let reconnectAttempt = 0 // 添加重连计数器
  let maxReconnects = 0 // 设置为0，禁止自动重连

  const completeConnection = () => {
    if (!isCompleted) {
      isCompleted = true

      // 清除所有超时计时器
      if (timeoutId) {
        clearTimeout(timeoutId)
        timeoutId = null
      }

      // 立即中止fetch请求，确保连接关闭
      try {
        abortController.abort()
      } catch (e) {
        console.error('中止连接时出错:', e)
      }

      // 延迟调用onComplete，确保资源已清理
      setTimeout(() => {
        if (onComplete) onComplete()
      }, 50)

      console.log('SSE连接已完全关闭')
    }
  }

  // 使用Fetch API替代EventSource
  fetch(url, {
    method: 'GET',
    headers: {
      'Accept': 'text/event-stream',
      'Cache-Control': 'no-cache',
      'Authorization': token
    },
    credentials: 'include',
    signal: abortController.signal
  })
  .then(response => {
    if (!response.ok) {
      throw new Error(`HTTP error! Status: ${response.status}`)
    }

    // 获取response的reader
    const reader = response.body.getReader()
    const decoder = new TextDecoder()

    // 递归读取流数据
    const readStream = () => {
      reader.read().then(({ done, value }) => {
        if (done) {
          console.log('SSE流已结束')
          // 处理最后可能的不完整行
          if (partialLine) {
            const dataMatch = partialLine.match(/data:(.*)/i)
            if (dataMatch && dataMatch[1]) {
              const data = dataMatch[1].trim()
              messageBuffer += data
              onMessage(data)
            }
          }
          completeConnection()
          return
        }

        lastReceiveTime = Date.now()
        const chunk = decoder.decode(value, { stream: true })

        // 检查是否包含特殊的结束标记
        if (chunk.includes('[DONE]') || chunk.includes('data: [DONE]')) {
          console.log('检测到[DONE]标记，结束连接')
          completeConnection()
          return
        }

        // 将新块与之前的不完整行合并
        const fullText = partialLine + chunk

        // 按SSE格式分割消息（空行分隔）
        const events = fullText.split('\n\n')

        // 保存最后一个可能不完整的部分
        partialLine = events.pop() || ''

        // 处理完整的事件
        for (const event of events) {
          // 检查是否是注释（心跳消息）
          if (event.startsWith(':')) {
            console.log('收到心跳消息:', event)
            continue
          }

          // 提取data字段值
          const dataMatch = event.match(/data:(.*)/i)
          if (dataMatch && dataMatch[1]) {
            const data = dataMatch[1].trim()
            // 检查是否是结束标记
            if (data === '[DONE]') {
              console.log('检测到[DONE]标记，结束连接')
              completeConnection()
              return
            }
            messageBuffer += data
            onMessage(data)
          }
        }

        // 如果未完成，继续读取
        if (!isCompleted) {
          readStream()
        }
      }).catch(error => {
        // 只处理非中止错误
        if (error.name !== 'AbortError' && !isCompleted) {
          console.error('SSE读取错误:', error)
          onError(error)
          completeConnection()
        }
      })
    }

    // 开始读取
    readStream()
  })
  .catch(error => {
    if (!isCompleted && error.name !== 'AbortError') {
      console.error('SSE连接错误:', error)
      onError(error)
      completeConnection()
    }
  })

  // 添加超时检测，如果长时间没有数据，自动关闭连接
  timeoutId = setTimeout(() => {
    if (!isCompleted) {
      console.log('SSE连接超时，自动关闭')
      completeConnection()
    }
  }, 30000) // 30秒超时

  // 返回一个可以关闭连接的对象
  return {
    close: () => {
      console.log('手动关闭SSE连接')
      completeConnection()
    }
  }
}

// 将所有导出移到文件末尾
// 默认导出
export default {
  generateChatId,
  authApi,
  conversationApi,
  conversationGroupApi
}

// 命名导出
export { conversationApi, conversationGroupApi, authApi, apiClient }

// 导出API_URL常量，以便其他组件使用
export { API_URL }
