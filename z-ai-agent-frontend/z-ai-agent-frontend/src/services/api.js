import axios from 'axios'

const API_URL = 'http://localhost:8123/api'

// 创建一个随机的聊天ID
export const generateChatId = () => {
  return Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15)
}

// 用户认证相关接口
const authApi = {
  // 用户登录
  login: async (username, password) => {
    try {
      const response = await axios.post(`${API_URL}/user/login`, {
        username,
        password
      })
      return response.data
    } catch (error) {
      console.error('登录请求失败:', error)
      throw error.response?.data || error
    }
  },

  // 用户注册
  register: async (username, password) => {
    try {
      const response = await axios.post(`${API_URL}/user/register`, {
        username,
        password
      })
      return response.data
    } catch (error) {
      console.error('注册请求失败:', error)
      throw error.response?.data || error
    }
  },

  // 获取用户信息
  getUserInfo: async (token) => {
    try {
      const response = await axios.get(`${API_URL}/user/info`, {
        headers: {
          Authorization: token
        }
      })
      return response.data
    } catch (error) {
      console.error('获取用户信息失败:', error)
      throw error.response?.data || error
    }
  },

  // 用户登出
  logout: async (token) => {
    try {
      const response = await axios.post(`${API_URL}/user/logout`, null, {
        headers: {
          Authorization: token
        }
      })
      return response.data
    } catch (error) {
      console.error('登出请求失败:', error)
      throw error.response?.data || error
    }
  }
}

// 会话管理相关接口
const conversationApi = {
  // 创建新会话
  createConversation: async (aiType, firstMessage = null, groupId = null) => {
    try {
      let url = `${API_URL}/conversation/create?aiType=${encodeURIComponent(aiType)}`

      if (firstMessage) {
        url += `&firstMessage=${encodeURIComponent(firstMessage)}`
      }

      if (groupId) {
        url += `&groupId=${groupId}`
      }

      // 获取token并添加到请求头
      const token = localStorage.getItem('Authorization')
      const headers = token ? { Authorization: token } : {}

      const response = await axios.post(url, null, { headers })
      return response.data
    } catch (error) {
      console.error('创建会话失败:', error)
      throw error.response?.data || error
    }
  },

  // 获取用户的会话列表
  getConversations: async (page = 1, size = 10, groupId = null, aiType = null) => {
    try {
      let url = `${API_URL}/conversation/list?page=${page}&size=${size}`

      if (groupId !== null) {
        url += `&groupId=${groupId}`
      }

      if (aiType !== null) {
        url += `&aiType=${encodeURIComponent(aiType)}`
      }

      // 获取token并添加到请求头
      const token = localStorage.getItem('Authorization')
      const headers = token ? { Authorization: token } : {}

      const response = await axios.get(url, { headers })
      return response.data
    } catch (error) {
      console.error('获取会话列表失败:', error)
      throw error.response?.data || error
    }
  },

  // 获取会话消息历史
  getConversationMessages: async (conversationId) => {
    try {
      // 获取token并添加到请求头
      const token = localStorage.getItem('Authorization')
      const headers = token ? { Authorization: token } : {}

      const response = await axios.get(`${API_URL}/conversation/${conversationId}/messages`, { headers })
      return response.data
    } catch (error) {
      console.error('获取会话消息历史失败:', error)
      throw error.response?.data || error
    }
  },

  // 更新会话信息（标题和分组）
  updateConversation: async (conversationId, title, groupId = null) => {
    try {
      // 获取token并添加到请求头
      const token = localStorage.getItem('Authorization')
      const headers = token ? { Authorization: token } : {}

      const response = await axios.post(`${API_URL}/conversation/update`, {
        id: conversationId,
        title,
        groupId
      }, { headers })
      return response.data
    } catch (error) {
      console.error('更新会话信息失败:', error)
      throw error.response?.data || error
    }
  },

  // 删除会话
  deleteConversation: async (conversationId) => {
    try {
      // 获取token并添加到请求头
      const token = localStorage.getItem('Authorization')
      const headers = token ? { Authorization: token } : {}

      const response = await axios.delete(`${API_URL}/conversation/${conversationId}`, { headers })
      return response.data
    } catch (error) {
      console.error('删除会话失败:', error)
      throw error.response?.data || error
    }
  },

  // 保存会话消息
  saveMessage: async (conversationId, userMessage, aiResponse, aiType) => {
    try {
      // 获取token并添加到请求头
      const token = localStorage.getItem('Authorization')
      const headers = token ? { Authorization: token } : {}

      const response = await axios.post(`${API_URL}/message/save`, {
        conversationId,
        userMessage,
        aiResponse,
        aiType
      }, { headers })
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
      // 获取token并添加到请求头
      const token = localStorage.getItem('Authorization')
      const headers = token ? { Authorization: token } : {}

      const response = await axios.post(`${API_URL}/conversation/group/create`, {
        name,
        aiType
      }, { headers })
      return response.data
    } catch (error) {
      console.error('创建会话分组失败:', error)
      throw error.response?.data || error
    }
  },

  // 获取用户的所有会话分组
  getGroups: async (aiType = null) => {
    try {
      // 获取token并添加到请求头
      const token = localStorage.getItem('Authorization')
      const headers = token ? { Authorization: token } : {}

      let url = `${API_URL}/conversation/group/list`;

      // 如果指定了aiType，添加到请求参数
      if (aiType !== null) {
        url += `?aiType=${encodeURIComponent(aiType)}`;
      }

      const response = await axios.get(url, { headers })
      return response.data
    } catch (error) {
      console.error('获取会话分组列表失败:', error)
      throw error.response?.data || error
    }
  },

  // 更新会话分组
  updateGroup: async (groupId, name) => {
    try {
      // 获取token并添加到请求头
      const token = localStorage.getItem('Authorization')
      const headers = token ? { Authorization: token } : {}

      const response = await axios.post(`${API_URL}/conversation/group/update`, {
        id: groupId,
        name
      }, { headers })
      return response.data
    } catch (error) {
      console.error('更新会话分组失败:', error)
      throw error.response?.data || error
    }
  },

  // 删除会话分组
  deleteGroup: async (groupId) => {
    try {
      // 获取token并添加到请求头
      const token = localStorage.getItem('Authorization')
      const headers = token ? { Authorization: token } : {}

      const response = await axios.delete(`${API_URL}/conversation/group/${groupId}`, { headers })
      return response.data
    } catch (error) {
      console.error('删除会话分组失败:', error)
      throw error.response?.data || error
    }
  }
}

// 爱情应用聊天接口 - SSE方式
export const chatWithLoveApp = (message, chatId, onMessage, onError, onComplete, conversationId = null) => {
  let url = `${API_URL}/ai/love_app/chat/sse?message=${encodeURIComponent(message)}&chatId=${encodeURIComponent(chatId)}`

  // 如果有会话ID，添加到请求参数
  if (conversationId) {
    url += `&conversationId=${encodeURIComponent(conversationId)}`
  }

  // 获取token并添加到请求参数，因为SSE不能在header中传递token
  const token = localStorage.getItem('Authorization')
  if (token) {
    url += `&token=${encodeURIComponent(token)}`
  }

  const eventSource = new EventSource(url)
  let isCompleted = false
  let messageBuffer = ''
  let lastReceiveTime = Date.now()

  const completeConnection = () => {
    if (!isCompleted) {
      isCompleted = true
      if (onComplete) onComplete()
      try {
        eventSource.close()
      } catch(e) {
        console.log('关闭EventSource时出错', e)
      }
    }
  }

  eventSource.onmessage = (event) => {
    lastReceiveTime = Date.now()
    messageBuffer += event.data
    onMessage(event.data)

    // 如果消息停顿超过2秒，可能是完成了
    setTimeout(() => {
      const currentTime = Date.now()
      if (currentTime - lastReceiveTime > 2000 && !isCompleted) {
        completeConnection()
      }
    }, 2200)
  }

  eventSource.onerror = (error) => {
    // 如果已经标记为完成，则忽略错误
    if (isCompleted) {
      return
    }

    // 如果消息缓冲区不为空，可能是正常完成
    if (messageBuffer.trim().length > 0) {
      // 只有在消息结尾不是标点符号时才可能是真错误
      const lastChar = messageBuffer.trim().slice(-1)
      if (/[。！？!?.,;:，；：]/.test(lastChar)) {
        // 很可能是正常结束
        completeConnection()
        return
      }

      // 如果最后接收时间在1秒内，可能是正常完成
      if (Date.now() - lastReceiveTime < 1000) {
        completeConnection()
        return
      }
    }

    // 真正的错误情况
    try {
      eventSource.close()
    } catch(e) {}

    onError(error)
  }

  return {
    close: () => {
      completeConnection()
    }
  }
}

// Manus应用聊天接口 - SSE方式
export const chatWithManus = (message, onMessage, onError, onComplete) => {
  let url = `${API_URL}/ai/manus/chat/sse?message=${encodeURIComponent(message)}`

  // 获取token并添加到请求参数
  const token = localStorage.getItem('Authorization')
  if (token) {
    url += `&token=${encodeURIComponent(token)}`
  }

  console.log("连接Manus SSE URL:", url);

  const eventSource = new EventSource(url);
  let isCompleted = false;
  let messageBuffer = '';
  let lastReceiveTime = Date.now();

  const completeConnection = () => {
    if (!isCompleted) {
      isCompleted = true;
      if (onComplete) onComplete();
      try {
        eventSource.close();
      } catch(e) {
        console.log('关闭EventSource时出错', e);
      }
    }
  };

  // 处理心跳消息
  eventSource.addEventListener('comment', (event) => {
    console.log('收到心跳消息:', event);
    lastReceiveTime = Date.now(); // 更新最后接收时间
  });

  eventSource.onmessage = (event) => {
    lastReceiveTime = Date.now();
    messageBuffer += event.data;
    onMessage(event.data);

    // 强制浏览器重绘
    requestAnimationFrame(() => {
      // 这个空函数会强制浏览器在下一帧重绘
    });

    // 如果消息停顿超过3分钟，可能是完成了（延长等待时间，适应AI思考时间）
    setTimeout(() => {
      const currentTime = Date.now();
      if (currentTime - lastReceiveTime > 180000 && !isCompleted) {
        console.log('长时间未收到消息，关闭连接');
        completeConnection();
      }
    }, 181000); // 3分钟+1秒
  };

  eventSource.onerror = (error) => {
    console.log('SSE连接错误:', error);
    // 如果已经标记为完成，则忽略错误
    if (isCompleted) {
      return;
    }

    // 如果消息缓冲区不为空，可能是正常完成
    if (messageBuffer.trim().length > 0) {
      // 只有在消息结尾不是标点符号时才可能是真错误
      const lastChar = messageBuffer.trim().slice(-1);
      if (/[。！？!?.,;:，；：]/.test(lastChar)) {
        // 很可能是正常结束
        completeConnection();
        return;
      }

      // 如果最后接收时间在5秒内，可能是正常完成
      if (Date.now() - lastReceiveTime < 5000) {
        completeConnection();
        return;
      }
    }

    // 如果是首次尝试（无数据）或者确实是错误，尝试自动重连
    // 注意：EventSource本身会尝试自动重连，这里只处理真正的错误情况
    if (messageBuffer.trim().length === 0 && Date.now() - lastReceiveTime > 10000) {
      // 真正的错误情况
      try {
        eventSource.close();
      } catch(e) {}

      onError(error);
    }
  };

  // 监听打开事件
  eventSource.onopen = (event) => {
    console.log('SSE连接已打开:', event);
    lastReceiveTime = Date.now(); // 更新最后接收时间
  };

  return {
    close: () => {
      completeConnection();
    }
  };
};

// 将所有导出移到文件末尾
// 默认导出
export default {
  chatWithLoveApp,
  chatWithManus,
  generateChatId,
  authApi,
  conversationApi,
  conversationGroupApi
}

// 命名导出
export { conversationApi, conversationGroupApi, authApi }
