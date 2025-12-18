<template>
  <div class="home-container">
    <div class="cyber-grid"></div>
    <div class="cyber-particles"></div>
    <div class="cyber-lines"></div>
    <div class="cyber-scanner"></div>
    <div class="floating-shapes"></div>
    <div class="glowing-stars"></div>
    <div class="tech-circles"></div>
    
    <HeaderNav />
    
    <div class="home-header">
      <div class="logo-container">
        <div class="logo-icon">AI</div>
        <div class="logo-beam"></div>
      </div>
      <h1 class="home-title glitch-text" data-text="AI 智能体 平台">AI <span class="text-gradient">智能体</span> 平台</h1>
      <p class="home-subtitle">探索未来人工智能应用，体验智能科技的无限可能</p>
    </div>
    
    <div class="app-cards">
      <div @click="handleCardClick('/love-app')" class="app-card love-card">
        <div class="app-card-icon">
          <div class="icon-container love-icon">
            <AiAvatar type="love" />
          </div>
        </div>
        <div class="app-card-content">
          <h2 class="app-card-title">AI恋爱大师</h2>
          <p class="app-card-description">提供专业情感咨询和恋爱建议，帮助您解决感情困惑，获得健康幸福的人际关系。</p>
          <div class="app-card-features">
            <div class="card-feature"><span class="feature-dot"></span>情感分析</div>
            <div class="card-feature"><span class="feature-dot"></span>关系咨询</div>
            <div class="card-feature"><span class="feature-dot"></span>个性化建议</div>
          </div>
          <span class="app-card-button">
            开始聊天
            <span class="button-glow"></span>
          </span>
        </div>
        <div class="card-decoration">
          <div class="decoration-circle"></div>
          <div class="decoration-line"></div>
        </div>
      </div>
      
      <div @click="handleCardClick('/manus-app')" class="app-card manus-card">
        <div class="app-card-icon">
          <div class="icon-container manus-icon">
            <AiAvatar type="manus" />
          </div>
        </div>
        <div class="app-card-content">
          <h2 class="app-card-title">AI超级智能体</h2>
          <p class="app-card-description">强大的AI助手，可以回答各类问题、提供专业建议、完成复杂任务。</p>
          <div class="app-card-features">
            <div class="card-feature"><span class="feature-dot"></span>知识问答</div>
            <div class="card-feature"><span class="feature-dot"></span>工作助手</div>
            <div class="card-feature"><span class="feature-dot"></span>创意伙伴</div>
          </div>
          <span class="app-card-button">
            开始聊天
            <span class="button-glow"></span>
          </span>
        </div>
        <div class="card-decoration">
          <div class="decoration-circle"></div>
          <div class="decoration-line"></div>
        </div>
      </div>
    </div>
    
    <div class="home-features">
      <h2 class="features-title">强大<span class="text-gradient">功能</span>，无限可能</h2>
      <div class="features-grid">
        <div class="feature">
          <div class="feature-icon">
            <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
              <polyline points="22 4 12 14.01 9 11.01"></polyline>
            </svg>
            <div class="icon-glow"></div>
          </div>
          <div class="feature-content">
            <h3>实时对话</h3>
            <p>通过流式响应技术，实现打字机效果的自然对话体验，让交流更加流畅</p>
          </div>
        </div>
        
        <div class="feature">
          <div class="feature-icon">
            <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10"></circle>
              <path d="M12 16v-4M12 8h.01"></path>
            </svg>
            <div class="icon-glow"></div>
          </div>
          <div class="feature-content">
            <h3>专业AI模型</h3>
            <p>每个应用都由专业训练的AI模型提供支持，精准解决特定领域问题，提供最佳解决方案</p>
          </div>
        </div>
        
        <div class="feature">
          <div class="feature-icon">
            <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path>
            </svg>
            <div class="icon-glow"></div>
          </div>
          <div class="feature-content">
            <h3>安全可靠</h3>
            <p>所有对话数据严格保密，采用先进加密技术，确保您的隐私和数据安全</p>
          </div>
        </div>
        
        <div class="feature">
          <div class="feature-icon">
            <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M12 3v19M5 8l14 10M5 18L19 8"></path>
            </svg>
            <div class="icon-glow"></div>
          </div>
          <div class="feature-content">
            <h3>持续学习</h3>
            <p>AI模型不断学习和进化，提供越来越精准的回答和服务，让体验持续提升</p>
          </div>
        </div>
      </div>
    </div>
    
    <div class="cta-section">
      <div class="cta-content">
        <h2 class="cta-title">立即体验 <span class="text-gradient">AI智能体</span></h2>
        <p class="cta-description">开启智能对话新时代，让AI成为您的得力助手</p>
        <div class="cta-buttons">
          <button @click="handleCardClick('/love-app')" class="cta-button love-button">
            试用AI恋爱大师
            <span class="button-glow"></span>
          </button>
          <button @click="handleCardClick('/manus-app')" class="cta-button manus-button">
            试用AI超级智能体
            <span class="button-glow"></span>
          </button>
        </div>
      </div>
      <div class="cta-decoration">
        <div class="circuit-line"></div>
        <div class="circuit-node"></div>
        <div class="circuit-line"></div>
      </div>
    </div>
    
    <TheFooter />


  </div>
</template>

<script>
import TheFooter from '@/components/TheFooter.vue'
import AiAvatar from '@/components/AiAvatar.vue'
import HeaderNav from '@/components/HeaderNav.vue'
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '@/services/api'

export default {
  name: 'HomeView',
  components: {
    TheFooter,
    AiAvatar,
    HeaderNav
  },
  setup() {
    const router = useRouter()
    const isLoggedIn = ref(false)
    const username = ref('')

    // 检查登录状态
    const checkLoginStatus = async () => {
      const token = localStorage.getItem('Authorization')
      if (token) {
        try {
          const response = await authApi.isLogin()
          if (response.code === 0 && response.data === true) {
            const userInfoResponse = await authApi.getUserInfo()
            if (userInfoResponse.code === 0 && userInfoResponse.data) {
              isLoggedIn.value = true
              username.value = userInfoResponse.data.username
            } else {
              localStorage.removeItem('Authorization')
              isLoggedIn.value = false
            }
          } else {
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

    // 处理卡片点击
    const handleCardClick = (route) => {
      if (isLoggedIn.value) {
        // 已登录，直接跳转
        router.push(route)
      } else {
        // 未登录，保存目标路由并触发HeaderNav显示登录弹窗
        localStorage.setItem('redirectAfterLogin', route)
        // 触发全局事件，让HeaderNav显示登录弹窗
        window.dispatchEvent(new CustomEvent('show-login-modal'))
      }
    }

    // 监听登录状态变化
    const handleAuthStatusChange = (event) => {
      isLoggedIn.value = event.detail.isLoggedIn
      username.value = event.detail.username
      
      // 如果登录成功且有待跳转的路由，执行跳转
      if (event.detail.isLoggedIn) {
        const redirectRoute = localStorage.getItem('redirectAfterLogin')
        if (redirectRoute) {
          localStorage.removeItem('redirectAfterLogin')
          router.push(redirectRoute)
        }
      }
    }

    // 组件挂载时检查登录状态并添加事件监听
    onMounted(() => {
      checkLoginStatus()
      window.addEventListener('auth-status-changed', handleAuthStatusChange)
    })

    // 组件卸载时移除事件监听
    onUnmounted(() => {
      window.removeEventListener('auth-status-changed', handleAuthStatusChange)
    })

    return {
      isLoggedIn,
      username,
      handleCardClick
    }
  },
  mounted() {
    this.initCyberParticles();
  },
  methods: {
    initCyberParticles() {
      if (typeof document !== 'undefined') {
        const particlesContainer = document.querySelector('.cyber-particles');
        if (particlesContainer) {
          for (let i = 0; i < 50; i++) {
            const particle = document.createElement('div');
            particle.className = 'particle';
            
            // 随机位置
            particle.style.left = `${Math.random() * 100}%`;
            particle.style.top = `${Math.random() * 100}%`;
            
            // 随机大小
            const size = Math.random() * 5 + 1;
            particle.style.width = `${size}px`;
            particle.style.height = `${size}px`;
            
            // 随机动画延迟
            particle.style.animationDelay = `${Math.random() * 5}s`;
            particle.style.animationDuration = `${Math.random() * 10 + 10}s`;
            
            particlesContainer.appendChild(particle);
          }
        }
      }
    }
  },
  metaInfo: {
    title: 'AI智能体平台 - 探索未来人工智能应用',
    meta: [
      { name: 'description', content: '与先进AI智能体互动，体验智能科技的无限可能。包括AI恋爱大师和AI超级智能体等应用。' },
      { name: 'keywords', content: 'AI, 人工智能, 智能体, 聊天机器人, 恋爱顾问, 智能助手' }
    ]
  }
}
</script>

<style scoped>
.home-container {
  position: relative;
  min-height: 100vh;
  padding: 1rem 1rem;
  overflow: hidden;
  background-color: #0a0c1b;
  color: #f0f0f0;
  font-family: 'Arial', sans-serif;
}

/* 赛博网格背景 */
.cyber-grid {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: 
    linear-gradient(to right, rgba(50, 50, 150, 0.1) 1px, transparent 1px),
    linear-gradient(to bottom, rgba(50, 50, 150, 0.1) 1px, transparent 1px);
  background-size: 30px 30px;
  z-index: -2;
  animation: gridPulse 15s infinite linear;
}

@keyframes gridPulse {
  0% { opacity: 0.3; }
  50% { opacity: 0.7; }
  100% { opacity: 0.3; }
}

/* 浮动几何形状 */
.floating-shapes {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: -1;
  overflow: hidden;
}

.floating-shapes::before {
  content: '';
  position: absolute;
  width: 100%;
  height: 100%;
  background-image: 
    radial-gradient(circle at 10% 10%, rgba(255, 0, 106, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 90% 20%, rgba(58, 134, 255, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 20% 80%, rgba(0, 255, 217, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 90%, rgba(128, 0, 255, 0.1) 0%, transparent 50%);
  filter: blur(20px);
  animation: floatShapes 30s infinite alternate ease-in-out;
}

@keyframes floatShapes {
  0% { transform: scale(1) rotate(0deg); }
  50% { transform: scale(1.1) rotate(3deg); }
  100% { transform: scale(1) rotate(0deg); }
}

/* 发光星星 */
.glowing-stars {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: -1;
}

.glowing-stars::after {
  content: '';
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  background-image: 
    radial-gradient(circle at 10% 10%, white 1px, transparent 1px),
    radial-gradient(circle at 20% 20%, white 1px, transparent 1px),
    radial-gradient(circle at 30% 30%, white 1px, transparent 1px),
    radial-gradient(circle at 40% 40%, white 1px, transparent 1px),
    radial-gradient(circle at 50% 50%, white 1px, transparent 1px),
    radial-gradient(circle at 60% 60%, white 1px, transparent 1px),
    radial-gradient(circle at 70% 70%, white 1px, transparent 1px),
    radial-gradient(circle at 80% 80%, white 1px, transparent 1px),
    radial-gradient(circle at 90% 90%, white 1px, transparent 1px);
  background-size: 300px 300px;
  background-repeat: repeat;
  animation: twinkle 8s infinite alternate;
}

@keyframes twinkle {
  0%, 100% { opacity: 0.3; }
  50% { opacity: 0.7; }
}

/* 动态线条 */
.cyber-lines {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: -1;
  overflow: hidden;
}

.cyber-lines::before,
.cyber-lines::after {
  content: '';
  position: absolute;
  background: linear-gradient(90deg, transparent, #4d18ff, transparent);
  width: 200%;
  height: 1px;
  animation: lineScan 20s infinite linear;
  opacity: 0.3;
}

.cyber-lines::before {
  top: 25%;
  left: -50%;
  animation-delay: -5s;
}

.cyber-lines::after {
  top: 75%;
  left: -50%;
  animation-delay: -12s;
}

/* 技术圆圈 */
.tech-circles {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: -1;
  overflow: hidden;
}

.tech-circles::before,
.tech-circles::after {
  content: '';
  position: absolute;
  width: 300px;
  height: 300px;
  border-radius: 50%;
  border: 1px dashed rgba(58, 134, 255, 0.2);
  animation: rotateSlow 20s linear infinite;
}

.tech-circles::before {
  top: -150px;
  right: -150px;
}

.tech-circles::after {
  bottom: -150px;
  left: -150px;
  animation-direction: reverse;
}

@keyframes rotateSlow {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

@keyframes lineScan {
  0% { transform: translateX(0); }
  100% { transform: translateX(50%); }
}

/* 扫描效果 */
.cyber-scanner {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(transparent, rgba(61, 90, 254, 0.05), transparent);
  opacity: 0;
  z-index: -1;
  animation: scanEffect 8s infinite;
}

@keyframes scanEffect {
  0% { transform: translateY(-100%); opacity: 0; }
  20% { opacity: 0.5; }
  80% { opacity: 0.5; }
  100% { transform: translateY(100%); opacity: 0; }
}

/* 粒子效果 */
.cyber-particles {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: -1;
  overflow: hidden;
}

.cyber-particles::before {
  content: '';
  position: absolute;
  width: 100%;
  height: 100%;
  background-image: radial-gradient(rgba(100, 100, 255, 0.3) 1px, transparent 1px);
  background-size: 50px 50px;
  animation: particleFloat 60s infinite linear;
}

@keyframes particleFloat {
  0% { background-position: 0 0; }
  100% { background-position: 50px 50px; }
}

/* 标题区域 */
.home-header {
  position: relative;
  text-align: center;
  padding: 2rem 1rem 1rem;
  margin-bottom: 1.5rem;
}

.logo-container {
  position: relative;
  width: 60px;
  height: 60px;
  margin: 0 auto 1rem;
}

.logo-icon {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  font-weight: bold;
  background: linear-gradient(135deg, #ff006a, #3a86ff);
  border-radius: 50%;
  box-shadow: 0 0 20px rgba(58, 134, 255, 0.5);
  z-index: 1;
}

.logo-beam {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(58, 134, 255, 0.3) 0%, transparent 70%);
  animation: beamPulse 3s infinite;
}

@keyframes beamPulse {
  0% { transform: translate(-50%, -50%) scale(1); opacity: 0.7; }
  50% { transform: translate(-50%, -50%) scale(1.2); opacity: 0.3; }
  100% { transform: translate(-50%, -50%) scale(1); opacity: 0.7; }
}

.home-title {
  font-size: 2.5rem;
  font-weight: bold;
  margin-bottom: 0.5rem;
  position: relative;
}

.glitch-text {
  position: relative;
  animation: glitchEffect 10s infinite;
}

.glitch-text::before,
.glitch-text::after {
  content: attr(data-text);
  position: absolute;
  top: 0;
  width: 100%;
  height: 100%;
  left: 0;
  opacity: 0.8;
}

.glitch-text::before {
  animation: glitch-anim-1 5s infinite linear alternate-reverse;
  color: #ff00de;
  clip: rect(44px, 450px, 56px, 0);
  left: 1px;
}

.glitch-text::after {
  animation: glitch-anim-2 2s infinite linear alternate-reverse;
  color: #3a86ff;
  clip: rect(24px, 450px, 36px, 0);
  left: -1px;
}

@keyframes glitch-anim-1 {
  0% { clip: rect(4px, 450px, 24px, 0); }
  20% { clip: rect(52px, 450px, 74px, 0); }
  40% { clip: rect(32px, 450px, 44px, 0); }
  60% { clip: rect(20px, 450px, 30px, 0); }
  80% { clip: rect(48px, 450px, 62px, 0); }
  100% { clip: rect(10px, 450px, 28px, 0); }
}

@keyframes glitch-anim-2 {
  0% { clip: rect(12px, 450px, 34px, 0); }
  20% { clip: rect(48px, 450px, 66px, 0); }
  40% { clip: rect(28px, 450px, 42px, 0); }
  60% { clip: rect(14px, 450px, 38px, 0); }
  80% { clip: rect(42px, 450px, 58px, 0); }
  100% { clip: rect(6px, 450px, 18px, 0); }
}

.text-gradient {
  background: linear-gradient(to right, #ff006a, #3a86ff);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  display: inline-block;
}

.home-subtitle {
  font-size: 1rem;
  max-width: 700px;
  margin: 0 auto;
  color: #b5b5c3;
  line-height: 1.4;
}

/* 应用卡片 */
.app-cards {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 2rem;
  padding: 1rem 1rem 3rem;
  max-width: 1200px;
  margin: 0 auto;
}

.app-card {
  position: relative;
  width: 100%;
  max-width: 500px;
  background: linear-gradient(135deg, rgba(20, 21, 46, 0.8), rgba(16, 17, 38, 0.8));
  border-radius: 16px;
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  text-decoration: none;
  color: inherit;
  overflow: hidden;
  transition: all 0.3s ease;
  border: 1px solid rgba(100, 100, 255, 0.1);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.4);
  transform: perspective(1000px) rotateX(0deg);
  cursor: pointer;
}

.app-card:hover {
  transform: perspective(1000px) rotateX(5deg) translateY(-10px);
  border-color: rgba(100, 100, 255, 0.3);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.6);
}

.love-card {
  background: linear-gradient(135deg, rgba(30, 20, 40, 0.8), rgba(16, 17, 38, 0.8));
  border-color: rgba(255, 0, 106, 0.2);
}

.love-card:hover {
  border-color: rgba(255, 0, 106, 0.4);
  box-shadow: 0 20px 40px rgba(255, 0, 106, 0.2);
}

.manus-card {
  background: linear-gradient(135deg, rgba(20, 30, 60, 0.8), rgba(16, 17, 38, 0.8));
  border-color: rgba(58, 134, 255, 0.2);
}

.manus-card:hover {
  border-color: rgba(58, 134, 255, 0.4);
  box-shadow: 0 20px 40px rgba(58, 134, 255, 0.2);
}

.app-card-icon {
  display: flex;
  justify-content: center;
  margin-bottom: 1rem;
}

.icon-container {
  width: 70px;
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  overflow: hidden;
}

.app-card-content {
  flex: 1;
}

.app-card-title {
  font-size: 1.6rem;
  margin-bottom: 0.8rem;
  text-align: center;
}

.app-card-description {
  color: #b5b5c3;
  line-height: 1.5;
  margin-bottom: 1rem;
  font-size: 0.95rem;
}

.app-card-features {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  margin-bottom: 1.2rem;
}

.card-feature {
  font-size: 0.9rem;
  color: #d0d0d8;
  display: flex;
  align-items: center;
}

.feature-dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  margin-right: 6px;
  background: linear-gradient(to right, #ff006a, #3a86ff);
}

.app-card-button {
  display: inline-block;
  background: linear-gradient(90deg, #ff006a, #3a86ff);
  color: white;
  padding: 0.8rem 2rem;
  border-radius: 50px;
  font-weight: bold;
  text-align: center;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
  box-shadow: 0 5px 15px rgba(58, 134, 255, 0.3);
}

.app-card-button:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(58, 134, 255, 0.5);
}

.button-glow {
  position: absolute;
  top: 0;
  left: -50%;
  width: 50%;
  height: 100%;
  background: rgba(255, 255, 255, 0.3);
  transform: skewX(-25deg);
  animation: buttonGlow 3s infinite;
  pointer-events: none;
}

@keyframes buttonGlow {
  0% { left: -50%; }
  30%, 100% { left: 150%; }
}

.card-decoration {
  position: absolute;
  top: 0;
  right: 0;
  width: 100px;
  height: 100px;
  overflow: hidden;
}

.decoration-circle {
  position: absolute;
  top: -40px;
  right: -40px;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  border: 1px solid rgba(100, 100, 255, 0.2);
}

.decoration-line {
  position: absolute;
  top: 20px;
  right: 20px;
  width: 100px;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(100, 100, 255, 0.5));
  transform: rotate(45deg);
}

/* 功能特性 */
.home-features {
  padding: 4rem 1rem;
  max-width: 1200px;
  margin: 0 auto;
  background: rgba(14, 16, 36, 0.6);
  border-radius: 16px;
  border: 1px solid rgba(100, 100, 255, 0.1);
}

.features-title {
  text-align: center;
  font-size: 2.2rem;
  margin-bottom: 3rem;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 2rem;
}

.feature {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  padding: 1.5rem;
  border-radius: 12px;
  background: rgba(20, 22, 48, 0.5);
  border: 1px solid rgba(100, 100, 255, 0.1);
  transition: all 0.3s ease;
}

.feature:hover {
  transform: translateY(-5px);
  border-color: rgba(100, 100, 255, 0.3);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.3);
}

.feature-icon {
  position: relative;
  margin-bottom: 1.2rem;
}

.feature-icon svg {
  width: 40px;
  height: 40px;
  stroke: #3a86ff;
  position: relative;
  z-index: 1;
}

.icon-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(58, 134, 255, 0.2) 0%, transparent 70%);
  z-index: 0;
}

.feature-content h3 {
  font-size: 1.3rem;
  margin-bottom: 0.8rem;
}

.feature-content p {
  color: #b5b5c3;
  line-height: 1.6;
}

/* CTA区域 */
.cta-section {
  position: relative;
  padding: 5rem 1rem;
  margin: 4rem auto;
  max-width: 1000px;
  text-align: center;
  overflow: hidden;
}

.cta-content {
  position: relative;
  z-index: 1;
}

.cta-title {
  font-size: 2.5rem;
  margin-bottom: 1rem;
}

.cta-description {
  font-size: 1.1rem;
  max-width: 600px;
  margin: 0 auto 2.5rem;
  color: #b5b5c3;
}

.cta-buttons {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 1.5rem;
}

.cta-button {
  display: inline-block;
  padding: 1rem 2rem;
  border-radius: 50px;
  font-weight: bold;
  text-decoration: none;
  color: white;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}

.love-button {
  background: linear-gradient(90deg, #ff006a, #ff4b8b);
  box-shadow: 0 5px 15px rgba(255, 0, 106, 0.3);
}

.love-button:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(255, 0, 106, 0.5);
}

.manus-button {
  background: linear-gradient(90deg, #1a56ff, #00c6ff);
  box-shadow: 0 5px 15px rgba(58, 134, 255, 0.3);
}

.manus-button:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(58, 134, 255, 0.5);
}

.cta-decoration {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
}

.circuit-line {
  position: absolute;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(58, 134, 255, 0.5), transparent);
}

.circuit-line:first-child {
  top: 30%;
  left: 0;
  width: 100%;
}

.circuit-line:last-child {
  bottom: 30%;
  left: 0;
  width: 100%;
}

.circuit-node {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(58, 134, 255, 0.5) 0%, transparent 70%);
  box-shadow: 0 0 15px rgba(58, 134, 255, 0.7);
  animation: nodePulse 3s infinite;
}

@keyframes nodePulse {
  0% { transform: translate(-50%, -50%) scale(1); opacity: 1; }
  50% { transform: translate(-50%, -50%) scale(1.5); opacity: 0.5; }
  100% { transform: translate(-50%, -50%) scale(1); opacity: 1; }
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
  .home-title {
    font-size: 2.2rem;
  }
  
  .section-title, 
  .features-title,
  .cta-title {
    font-size: 1.8rem;
  }
  
  .tech-stats {
    gap: 2rem;
  }
  
  .stat-number {
    font-size: 2rem;
  }
  
  .app-card {
    padding: 1.5rem;
  }
  
  .app-card-title {
    font-size: 1.5rem;
  }
  
  .features-grid {
    grid-template-columns: 1fr;
  }
}
</style> 