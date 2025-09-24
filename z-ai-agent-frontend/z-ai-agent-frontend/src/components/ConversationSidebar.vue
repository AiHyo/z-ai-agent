<template>
  <div class="conversation-sidebar" :class="{ 'sidebar-collapsed': collapsed }">
    <!-- AI回复时的禁用覆盖层 -->
    <div v-if="!collapsed && isWaitingForResponse" class="sidebar-blocker">
      <div class="blocker-message">
        <span>AI正在回复中...</span>
        <span>请等待回复完成后再操作</span>
      </div>
    </div>
    
    <!-- 侧边栏折叠切换按钮 -->
    <div class="sidebar-toggle" @click="toggleSidebar">
      <div class="toggle-icon">{{ collapsed ? '▶' : '◀' }}</div>
    </div>
    
    <!-- 内容包装器 - 新增元素 -->
    <div class="sidebar-content-wrapper" :class="{ 'content-hidden': collapsed }">
      <!-- 侧边栏标题 -->
      <div class="sidebar-header">
        <div class="cybertext">会话管理</div>
        <button class="cyber-btn" @click="createNewConversation">
          <span class="btn-text">新会话</span>
        </button>
      </div>
      
      <!-- 创建会话分组按钮 -->
      <div class="group-actions">
        <button class="cyber-btn small" @click="createNewGroup">
          <span class="btn-text">新建分组</span>
        </button>
      </div>
      
      <!-- 会话分组和会话列表 -->
      <div class="sidebar-content">
        <!-- 未分组会话 -->
        <div class="conversation-section">
          <div class="section-title">未分组</div>
          <div class="conversation-list">
            <div
              v-for="conversation in ungroupedConversations"
              :key="conversation.id"
              :class="['conversation-item', { active: activeConversationId === conversation.id }]"
              @click="selectConversation(conversation)"
            >
              <div class="conversation-title">{{ conversation.title }}</div>
              <div class="conversation-actions">
                <button class="action-btn edit" @click.stop="editConversation(conversation)">
                  <span class="action-icon">✏️</span>
                </button>
                <button class="action-btn delete" @click.stop="deleteConversation(conversation)">
                  <span class="action-icon">🗑️</span>
                </button>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 分组会话 -->
        <div 
          v-for="group in groups" 
          :key="group.id" 
          class="conversation-section group"
        >
          <div class="section-title" @click="toggleGroup(group.id)">
            <span class="group-collapse-icon">{{ expandedGroups.has(group.id) ? '▼' : '►' }}</span>
            <span>{{ group.name }}</span>
            <div class="group-actions">
              <button class="action-btn edit" @click.stop="editGroup(group)">
                <span class="action-icon small">✏️</span>
              </button>
              <button class="action-btn delete" @click.stop="deleteGroup(group)">
                <span class="action-icon small">🗑️</span>
              </button>
            </div>
          </div>
          
          <div class="conversation-list" v-if="expandedGroups.has(group.id)">
            <div
              v-for="conversation in getConversationsByGroup(group.id)"
              :key="conversation.id"
              :class="['conversation-item', { active: activeConversationId === conversation.id }]"
              @click="selectConversation(conversation)"
            >
              <div class="conversation-title">{{ conversation.title }}</div>
              <div class="conversation-actions">
                <button class="action-btn edit" @click.stop="editConversation(conversation)">
                  <span class="action-icon">✏️</span>
                </button>
                <button class="action-btn delete" @click.stop="deleteConversation(conversation)">
                  <span class="action-icon">🗑️</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 编辑会话对话框 -->
    <teleport to="body">
      <div v-if="showEditConversationModal" class="modal">
        <div class="modal-content">
          <h2>编辑会话</h2>
          <div class="form-group">
            <label for="conversation-title">会话名称:</label>
            <input 
              id="conversation-title"
              v-model="editingConversation.title" 
              placeholder="输入会话名称" 
              class="cyber-input" 
            />
          </div>
          <div class="form-group">
            <label for="conversation-group">所属分组:</label>
            <select id="conversation-group" v-model="editingConversation.groupId" class="cyber-select">
              <option :value="null">未分组</option>
              <option v-for="group in groups" :key="group.id" :value="group.id">
                {{ group.name }}
              </option>
            </select>
          </div>
          <div class="form-actions">
            <button @click="cancelEditConversation" class="cyber-btn">取消</button>
            <button @click="saveEditConversation" class="cyber-btn save">保存</button>
          </div>
        </div>
      </div>
    </teleport>
    
    <!-- 编辑分组对话框 -->
    <teleport to="body">
      <div v-if="showEditGroupModal" class="modal">
        <div class="modal-content">
          <h2>{{ editingGroup.id ? '编辑分组' : '创建分组' }}</h2>
          <div class="form-group">
            <label for="group-name">分组名称:</label>
            <input 
              id="group-name"
              v-model="editingGroup.name" 
              placeholder="输入分组名称" 
              class="cyber-input" 
            />
          </div>
          <div class="form-actions">
            <button @click="cancelEditGroup" class="cyber-btn">取消</button>
            <button @click="saveEditGroup" class="cyber-btn save">保存</button>
          </div>
        </div>
      </div>
    </teleport>
    
    <!-- 删除确认对话框 -->
    <teleport to="body">
      <div v-if="showDeleteConfirmModal" class="modal">
        <div class="modal-content">
          <h2>确认删除</h2>
          <p>{{ deleteConfirmMessage }}</p>
          <div class="form-actions">
            <button @click="cancelDelete" class="cyber-btn">取消</button>
            <button @click="confirmDelete" class="cyber-btn delete">删除</button>
          </div>
        </div>
      </div>
    </teleport>
  </div>
</template>

<script>
import { ref, computed, watch, onMounted } from 'vue'
// 使用命名导入方式
import { conversationApi, conversationGroupApi } from '@/services/api'

export default {
  name: 'ConversationSidebar',
  props: {
    aiType: {
      type: String,
      required: true
    },
    initialConversationId: {
      type: String,
      default: null
    },
    isWaitingForResponse: {
      type: Boolean,
      default: false
    }
  },
  emits: ['conversation-selected', 'conversation-created', 'sidebar-toggle'],
  
  setup(props, { emit }) {
    // 侧边栏状态
    const collapsed = ref(false)
    const toggleSidebar = () => {
      collapsed.value = !collapsed.value
      // 向父组件发送状态更新
      emit('sidebar-toggle', collapsed.value)
    }
    
    // 会话和分组数据
    const conversations = ref([])
    const groups = ref([])
    const activeConversationId = ref(props.initialConversationId)
    
    // 展开的分组
    const expandedGroups = ref(new Set())
    
    // 会话编辑相关
    const showEditConversationModal = ref(false)
    const editingConversation = ref({})
    
    // 分组编辑相关
    const showEditGroupModal = ref(false)
    const editingGroup = ref({})
    
    // 删除确认相关
    const showDeleteConfirmModal = ref(false)
    const deleteConfirmMessage = ref('')
    const deleteType = ref('')
    const deleteItemId = ref(null)
    
    // 获取未分组的会话
    const ungroupedConversations = computed(() => {
      return conversations.value.filter(conv => !conv.groupId)
    })
    
    // 根据分组ID获取会话列表
    const getConversationsByGroup = (groupId) => {
      return conversations.value.filter(conv => conv.groupId === groupId)
    }
    
    // 加载会话和分组数据
    const loadData = async () => {
      try {
        // 加载分组，添加aiType参数过滤
        const groupsResponse = await conversationGroupApi.getGroups(props.aiType)
        groups.value = groupsResponse.data
        
        // 为每个分组设置展开状态
        groups.value.forEach(group => {
          expandedGroups.value.add(group.id)
        })
        
        // 加载会话列表，添加aiType参数过滤
        const conversationsResponse = await conversationApi.getConversations(1, 50, null, props.aiType)
        conversations.value = conversationsResponse.data.records
        
        console.log('会话列表加载成功:', conversations.value)
        console.log('分组列表加载成功:', groups.value)
      } catch (error) {
        console.error('加载数据失败:', error)
      }
    }
    
    // 创建新会话
    const createNewConversation = async () => {
      try {
        const response = await conversationApi.createConversation(props.aiType)
        const newConversation = response.data
        
        conversations.value.unshift(newConversation)
        activeConversationId.value = newConversation.id
        
        // 通知父组件会话已创建
        emit('conversation-created', newConversation)
        emit('conversation-selected', newConversation)
      } catch (error) {
        console.error('创建会话失败:', error)
      }
    }
    
    // 选择会话
    const selectConversation = (conversation) => {
      // 如果正在等待AI回复，则禁止切换会话
      if (props.isWaitingForResponse) {
        // 可以添加一个提示信息
        alert('AI正在回复中，请等待回复完成后再切换会话')
        return
      }
      
      activeConversationId.value = conversation.id
      emit('conversation-selected', conversation)
    }
    
    // 编辑会话
    const editConversation = (conversation) => {
      editingConversation.value = { ...conversation }
      showEditConversationModal.value = true
    }
    
    // 取消编辑会话
    const cancelEditConversation = () => {
      showEditConversationModal.value = false
      editingConversation.value = {}
    }
    
    // 保存会话编辑
    const saveEditConversation = async () => {
      try {
        const response = await conversationApi.updateConversation(
          editingConversation.value.id,
          editingConversation.value.title,
          editingConversation.value.groupId
        )
        
        // 更新本地会话数据
        const index = conversations.value.findIndex(c => c.id === editingConversation.value.id)
        if (index !== -1) {
          conversations.value[index] = response.data
        }
        
        showEditConversationModal.value = false
      } catch (error) {
        console.error('更新会话失败:', error)
      }
    }
    
    // 删除会话
    const deleteConversation = (conversation) => {
      deleteConfirmMessage.value = `确定要删除会话"${conversation.title}"吗？`
      deleteType.value = 'conversation'
      deleteItemId.value = conversation.id
      showDeleteConfirmModal.value = true
    }
    
    // 创建新分组
    const createNewGroup = () => {
      editingGroup.value = { name: '新的分组', aiType: props.aiType }
      showEditGroupModal.value = true
    }
    
    // 编辑分组
    const editGroup = (group) => {
      editingGroup.value = { ...group }
      showEditGroupModal.value = true
    }
    
    // 取消编辑分组
    const cancelEditGroup = () => {
      showEditGroupModal.value = false
      editingGroup.value = {}
    }
    
    // 保存分组编辑
    const saveEditGroup = async () => {
      try {
        let response
        
        if (editingGroup.value.id) {
          // 更新已有分组
          response = await conversationGroupApi.updateGroup(
            editingGroup.value.id,
            editingGroup.value.name
          )
          
          // 更新本地分组数据
          const index = groups.value.findIndex(g => g.id === editingGroup.value.id)
          if (index !== -1) {
            groups.value[index] = response.data
          }
        } else {
          // 创建新分组, 传递aiType参数
          response = await conversationGroupApi.createGroup(editingGroup.value.name, props.aiType)
          
          // 添加到本地分组数据
          groups.value.push(response.data)
          expandedGroups.value.add(response.data.id)
        }
        
        showEditGroupModal.value = false
      } catch (error) {
        console.error('保存分组失败:', error)
      }
    }
    
    // 删除分组
    const deleteGroup = (group) => {
      deleteConfirmMessage.value = `确定要删除分组"${group.name}"吗？该分组中的会话将变为未分组状态。`
      deleteType.value = 'group'
      deleteItemId.value = group.id
      showDeleteConfirmModal.value = true
    }
    
    // 取消删除
    const cancelDelete = () => {
      showDeleteConfirmModal.value = false
      deleteType.value = ''
      deleteItemId.value = null
    }
    
    // 确认删除
    const confirmDelete = async () => {
      try {
        if (deleteType.value === 'conversation') {
          await conversationApi.deleteConversation(deleteItemId.value)
          conversations.value = conversations.value.filter(c => c.id !== deleteItemId.value)
          
          // 如果删除的是当前会话，选择第一个会话
          if (activeConversationId.value === deleteItemId.value) {
            const firstConversation = conversations.value[0]
            if (firstConversation) {
              activeConversationId.value = firstConversation.id
              emit('conversation-selected', firstConversation)
            } else {
              activeConversationId.value = null
            }
          }
        } else if (deleteType.value === 'group') {
          await conversationGroupApi.deleteGroup(deleteItemId.value)
          groups.value = groups.value.filter(g => g.id !== deleteItemId.value)
          
          // 更新属于该分组的会话
          conversations.value.forEach(conv => {
            if (conv.groupId === deleteItemId.value) {
              conv.groupId = null
            }
          })
          
          // 从expandedGroups中移除
          expandedGroups.value.delete(deleteItemId.value)
        }
        
        showDeleteConfirmModal.value = false
      } catch (error) {
        console.error('删除失败:', error)
      }
    }
    
    // 切换分组展开状态
    const toggleGroup = (groupId) => {
      if (expandedGroups.value.has(groupId)) {
        expandedGroups.value.delete(groupId)
      } else {
        expandedGroups.value.add(groupId)
      }
    }
    
    // 组件挂载时加载数据
    onMounted(() => {
      loadData()
      
      // 如果有初始会话ID，则设置为活动会话
      if (props.initialConversationId) {
        activeConversationId.value = props.initialConversationId
      }
    })
    
    // 监听activeConversationId变化
    watch(activeConversationId, (newId) => {
      const conversation = conversations.value.find(c => c.id === newId)
      if (conversation) {
        emit('conversation-selected', conversation)
      }
    })
    
    return {
      collapsed,
      toggleSidebar,
      conversations,
      groups,
      activeConversationId,
      ungroupedConversations,
      expandedGroups,
      showEditConversationModal,
      editingConversation,
      showEditGroupModal,
      editingGroup,
      showDeleteConfirmModal,
      deleteConfirmMessage,
      getConversationsByGroup,
      createNewConversation,
      selectConversation,
      editConversation,
      cancelEditConversation,
      saveEditConversation,
      deleteConversation,
      createNewGroup,
      editGroup,
      cancelEditGroup,
      saveEditGroup,
      deleteGroup,
      cancelDelete,
      confirmDelete,
      toggleGroup
    }
  }
}
</script>

<style scoped>
.conversation-sidebar {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 280px;
  background-color: rgba(10, 15, 30, 0.9);
  backdrop-filter: blur(10px);
  color: #f0f0f0;
  border-right: 1px solid #4a55a0;
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
  z-index: 200;
  overflow: hidden;
  box-shadow: 0 0 20px rgba(83, 100, 255, 0.3);
}

.sidebar-collapsed {
  width: 30px;
}

/* 新增内容包装器样式 */
.sidebar-content-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: opacity 0.3s ease;
}

.content-hidden {
  opacity: 0;
  pointer-events: none;
}

.sidebar-toggle {
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 30px;
  height: 60px;
  background-color: #181c3a;
  border-left: 1px solid #4a55a0;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 201;
  border-radius: 0 4px 4px 0;
  color: #7a85ff;
  font-size: 12px;
  box-shadow: 2px 0 10px rgba(83, 100, 255, 0.4);
}

.sidebar-toggle:hover {
  background-color: #262b54;
  color: #b5beff;
}

.toggle-icon {
  animation: pulse 2s infinite;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}

@keyframes pulse {
  0% { opacity: 0.7; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.2); }
  100% { opacity: 0.7; transform: scale(1); }
}

.sidebar-header {
  padding: 15px;
  border-bottom: 1px solid rgba(83, 100, 255, 0.3);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.cybertext {
  font-family: 'JetBrains Mono', monospace;
  font-size: 1.2rem;
  color: #70f6ff;
  text-shadow: 0 0 5px rgba(112, 246, 255, 0.5);
  letter-spacing: 1px;
}

.cyber-btn {
  background-color: transparent;
  border: 1px solid #4a55a0;
  border-radius: 2px;
  color: #70f6ff;
  padding: 8px 15px;
  cursor: pointer;
  position: relative;
  font-family: 'JetBrains Mono', monospace;
  font-size: 0.8rem;
  letter-spacing: 1px;
  overflow: hidden;
  transition: all 0.2s ease;
}

.cyber-btn::before {
  content: '';
  position: absolute;
  top: -2px;
  left: -2px;
  width: calc(100% + 4px);
  height: calc(100% + 4px);
  background: linear-gradient(45deg, #7a85ff, #70f6ff, #ff006a, #7a85ff);
  background-size: 400%;
  z-index: -1;
  animation: glowing 20s linear infinite;
  opacity: 0;
  transition: opacity 0.3s ease-in-out;
}

.cyber-btn:hover::before {
  opacity: 1;
}

.cyber-btn:hover {
  color: #ffffff;
  text-shadow: 0 0 5px rgba(255, 255, 255, 0.5);
}

.cyber-btn.small {
  padding: 5px 10px;
  font-size: 0.7rem;
}

.group-actions {
  padding: 10px 15px;
  display: flex;
  justify-content: flex-end;
}

.sidebar-content {
  flex: 1;
  overflow-y: auto;
  padding: 10px 0;
}

.conversation-section {
  margin-bottom: 20px;
}

.section-title {
  padding: 8px 15px;
  font-size: 0.9rem;
  color: #b5beff;
  font-weight: bold;
  border-bottom: 1px solid rgba(83, 100, 255, 0.2);
  cursor: pointer;
  display: flex;
  align-items: center;
}

.group-collapse-icon {
  margin-right: 5px;
  font-size: 10px;
  color: #70f6ff;
}

.group .section-title {
  display: flex;
  justify-content: space-between;
}

.group-actions {
  display: flex;
  align-items: center;
  padding: 0;
}

.conversation-list {
  padding: 5px 0;
}

.conversation-item {
  padding: 10px 15px;
  margin: 2px 0;
  cursor: pointer;
  border-left: 3px solid transparent;
  transition: all 0.2s ease;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.conversation-item:hover {
  background-color: rgba(83, 100, 255, 0.1);
  border-left-color: #70f6ff;
}

.conversation-item.active {
  background-color: rgba(83, 100, 255, 0.2);
  border-left-color: #ff006a;
}

.conversation-title {
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 0.9rem;
}

.conversation-actions {
  display: flex;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.conversation-item:hover .conversation-actions {
  opacity: 1;
}

.action-btn {
  background: transparent;
  border: none;
  cursor: pointer;
  padding: 0;
  margin-left: 8px;
  font-size: 1rem;
  color: #b5beff;
  transition: all 0.2s ease;
}

.action-btn:hover {
  transform: scale(1.2);
}

.action-btn.edit:hover {
  color: #70f6ff;
}

.action-btn.delete:hover {
  color: #ff006a;
}

.action-icon {
  font-size: 14px;
}

.action-icon.small {
  font-size: 12px;
}

.modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}

.modal-content {
  background-color: #181c3a;
  border: 1px solid #4a55a0;
  border-radius: 4px;
  padding: 20px;
  width: 350px;
  box-shadow: 0 0 20px rgba(83, 100, 255, 0.5);
}

.modal-content h2 {
  margin-top: 0;
  color: #70f6ff;
  font-family: 'JetBrains Mono', monospace;
  font-size: 1.2rem;
  margin-bottom: 20px;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  color: #b5beff;
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 8px;
  background-color: rgba(30, 30, 50, 0.6);
  border: 1px solid #4a55a0;
  color: white;
  border-radius: 3px;
}

/* 新增样式 */
.cyber-input,
.cyber-select {
  width: 100%;
  padding: 8px;
  background-color: rgba(30, 30, 50, 0.6);
  border: 1px solid #4a55a0;
  color: white;
  border-radius: 3px;
  font-family: 'JetBrains Mono', monospace;
}

.cyber-input:focus,
.cyber-select:focus {
  border-color: #70f6ff;
  box-shadow: 0 0 8px rgba(112, 246, 255, 0.5);
  outline: none;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}

.cyber-btn.save {
  background-color: rgba(83, 100, 255, 0.2);
  border-color: #70f6ff;
}

.cyber-btn.save:hover {
  background-color: rgba(83, 100, 255, 0.4);
}

.cyber-btn.delete {
  background-color: rgba(255, 0, 106, 0.2);
  color: #ff6696;
  border-color: #ff006a;
}

.cyber-btn.delete:hover {
  background-color: rgba(255, 0, 106, 0.4);
  color: #ffffff;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

@keyframes glowing {
  0% { background-position: 0 0; }
  50% { background-position: 400% 0; }
  100% { background-position: 0 0; }
}

/* 滚动条样式 */
.sidebar-content::-webkit-scrollbar {
  width: 5px;
}

.sidebar-content::-webkit-scrollbar-track {
  background: rgba(30, 30, 50, 0.3);
}

.sidebar-content::-webkit-scrollbar-thumb {
  background: #4a55a0;
  border-radius: 2px;
}

.sidebar-content::-webkit-scrollbar-thumb:hover {
  background: #7a85ff;
}

.sidebar-blocker {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 998;
}

.blocker-message {
  background-color: #181c3a;
  border: 1px solid #4a55a0;
  border-radius: 4px;
  padding: 15px;
  width: 80%;
  box-shadow: 0 0 20px rgba(83, 100, 255, 0.5);
  text-align: center;
}

.blocker-message span {
  display: block;
  margin-bottom: 10px;
  color: #70f6ff;
  font-family: 'JetBrains Mono', monospace;
  font-size: 1.2rem;
}
</style> 