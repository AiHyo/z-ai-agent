import axios from 'axios'

const API_URL = 'http://localhost:8123/api'

// 创建一个随机的聊天ID
export const generateChatId = () => {
  return Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15)
}

// 爱情应用聊天接口 - SSE方式
export const chatWithLoveApp = (message, chatId, onMessage, onError, onComplete) => {
  const url = `${API_URL}/ai/love_app/chat/sse?message=${encodeURIComponent(message)}&chatId=${encodeURIComponent(chatId)}`
  
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
  const url = `${API_URL}/ai/manus/chat/sse?message=${encodeURIComponent(message)}`
  
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