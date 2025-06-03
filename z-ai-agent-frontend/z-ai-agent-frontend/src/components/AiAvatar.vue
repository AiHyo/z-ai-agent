<template>
  <div :class="['ai-avatar', `ai-avatar-${type}`]">
    <div class="avatar-glow"></div>
    <div class="avatar-inner">
      <!-- AI恋爱大师图标 - 更独特的心形+脑波设计 -->
      <svg v-if="type === 'love'" viewBox="0 0 24 24" class="avatar-svg">
        <g>
          <path d="M12 21c0.5-0.7 1-1.35 1.48-1.95 2.97-3.71 5.75-7.2 5.75-10.8C19.23 5.13 16.87 3 14 3c-1.95 0-3.45 0.85-4 2.05C9.45 3.85 7.95 3 6 3c-2.87 0-5.23 2.13-5.23 5.25 0 3.6 2.78 7.09 5.75 10.8C7 19.65 7.5 20.3 8 21" fill="currentColor"/>
          <path d="M20 11c1.39 0.49 2.58 1.4 3.45 2.51C23.8 14 24 14.58 24 15.19c0 0.95-0.55 1.8-1.41 2.2l-5.97 2.81v2.1c0 0.93-0.76 1.69-1.69 1.69h-0.56c-0.56 0-1.1-0.29-1.4-0.76l-1.13-1.67h-3.68L6.97 23.23c-0.3 0.47-0.84 0.76-1.4 0.76H5.1c-0.93 0-1.69-0.76-1.69-1.69v-2.1L0.02 17.39C-0.85 16.99-1.4 16.14-1.4 15.19c0-0.61 0.2-1.19 0.55-1.68C0.02 12.4 1.21 11.49 2.6 11" stroke="currentColor" fill="none" stroke-width="0.6"/>
          <circle cx="9" cy="10" r="1" fill="currentColor"/>
          <circle cx="15" cy="10" r="1" fill="currentColor"/>
          <path d="M12 14c1.5 0 2.5 1 2.5 1s-1 1-2.5 1-2.5-1-2.5-1 1-1 2.5-1z" fill="currentColor"/>
          <!-- 脑波线条 -->
          <path d="M3 6C5 5 8 7 12 7 16 7 19 5 21 6" stroke="currentColor" fill="none" stroke-width="0.5" class="brain-wave"/>
          <path d="M3 8C5 7 8 9 12 9 16 9 19 7 21 8" stroke="currentColor" fill="none" stroke-width="0.5" class="brain-wave" style="animation-delay: 0.2s"/>
        </g>
      </svg>
      
      <!-- AI超级智能体图标 - 高科技机器人/电路脑 -->
      <svg v-if="type === 'manus'" viewBox="0 0 24 24" class="avatar-svg">
        <g>
          <!-- 基础头型 -->
          <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2z" stroke="currentColor" fill="none" stroke-width="0.8"/>
          <!-- 电路线 -->
          <path d="M2 12h4m12 0h4M12 2v4m0 12v4" stroke="currentColor" fill="none" stroke-width="0.5" class="circuit-line"/>
          <path d="M4 8h16M4 16h16" stroke="currentColor" fill="none" stroke-width="0.5" class="circuit-line" style="animation-delay: 0.5s"/>
          <path d="M8 4v16M16 4v16" stroke="currentColor" fill="none" stroke-width="0.5" class="circuit-line" style="animation-delay: 1s"/>
          <!-- 中央处理器 -->
          <rect x="9" y="9" width="6" height="6" fill="currentColor" class="processor"/>
          <!-- 数字状态 -->
          <text x="10.5" y="13.5" font-family="monospace" font-size="3" fill="black" class="digital-text">01</text>
          <!-- 光点 -->
          <circle cx="5" cy="5" r="0.7" fill="currentColor" class="glow-point"/>
          <circle cx="19" cy="5" r="0.7" fill="currentColor" class="glow-point" style="animation-delay: 0.5s"/>
          <circle cx="19" cy="19" r="0.7" fill="currentColor" class="glow-point" style="animation-delay: 1s"/>
          <circle cx="5" cy="19" r="0.7" fill="currentColor" class="glow-point" style="animation-delay: 1.5s"/>
        </g>
      </svg>
      
      <!-- 用户头像图标 - 更加未来科技感 -->
      <svg v-if="type === 'user'" viewBox="0 0 24 24" class="avatar-svg">
        <g>
          <!-- 基础头像轮廓 -->
          <circle cx="12" cy="12" r="10" stroke="currentColor" fill="none" stroke-width="0.8"/>
          <!-- 头部 -->
          <circle cx="12" cy="9" r="3.5" fill="currentColor"/>
          <!-- 身体 -->
          <path d="M12 13c-3.87 0-7 1.79-7 4v2h14v-2c0-2.21-3.13-4-7-4z" fill="currentColor"/>
          <!-- 高科技装饰 -->
          <path d="M4 12c0-4.42 3.58-8 8-8" stroke="currentColor" fill="none" stroke-width="0.5" class="tech-line"/>
          <path d="M20 12c0 4.42-3.58 8-8 8" stroke="currentColor" fill="none" stroke-width="0.5" class="tech-line" style="animation-delay: 0.5s"/>
          <path d="M3 14l3-2m15 2l-3-2" stroke="currentColor" fill="none" stroke-width="0.5" class="tech-line" style="animation-delay: 1s"/>
          <!-- 数据点 -->
          <circle cx="7" cy="6" r="0.5" fill="currentColor" class="data-point"/>
          <circle cx="17" cy="6" r="0.5" fill="currentColor" class="data-point" style="animation-delay: 0.2s"/>
          <circle cx="7" cy="18" r="0.5" fill="currentColor" class="data-point" style="animation-delay: 0.4s"/>
          <circle cx="17" cy="18" r="0.5" fill="currentColor" class="data-point" style="animation-delay: 0.6s"/>
        </g>
      </svg>
    </div>
    <div class="avatar-pulse"></div>
  </div>
</template>

<script>
export default {
  name: 'AiAvatar',
  props: {
    type: {
      type: String,
      default: 'user',
      validator: value => ['love', 'manus', 'user'].includes(value)
    }
  }
}
</script>

<style scoped>
.ai-avatar {
  width: 100%;
  height: 100%;
  position: relative;
  border-radius: var(--border-radius-full);
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-inner {
  position: relative;
  width: 80%;
  height: 80%;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2;
  border-radius: 50%;
  animation: pulse 2s infinite;
}

.avatar-glow {
  position: absolute;
  top: -10%;
  left: -10%;
  right: -10%;
  bottom: -10%;
  border-radius: 50%;
  z-index: 1;
  opacity: 0.5;
  filter: blur(10px);
  animation: spin 4s linear infinite;
}

.avatar-pulse {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: 50%;
  z-index: 3;
  box-shadow: 0 0 0 0 rgba(255, 255, 255, 0.7);
  animation: pulse-ring 2s cubic-bezier(0.455, 0.03, 0.515, 0.955) infinite;
}

.ai-avatar-love {
  background: linear-gradient(135deg, #f87171, #f43f5e);
}

.ai-avatar-love .avatar-glow {
  background: radial-gradient(circle at 30% 30%, #fda4af, #f43f5e);
}

.ai-avatar-manus {
  background: linear-gradient(135deg, #60a5fa, #3b82f6);
}

.ai-avatar-manus .avatar-glow {
  background: radial-gradient(circle at 30% 30%, #93c5fd, #3b82f6);
}

.ai-avatar-user {
  background: linear-gradient(135deg, #818cf8, #4f46e5);
}

.ai-avatar-user .avatar-glow {
  background: radial-gradient(circle at 30% 30%, #a5b4fc, #4f46e5);
}

.avatar-svg {
  width: 70%;
  height: 70%;
  color: white;
  filter: drop-shadow(0 0 2px rgba(255, 255, 255, 0.5));
  z-index: 2;
}

.brain-wave {
  animation: wave 2s ease-in-out infinite;
}

.circuit-line {
  stroke-dasharray: 12;
  stroke-dashoffset: 24;
  animation: circuit 3s linear infinite;
}

.processor {
  animation: blink 1.5s ease-in-out infinite alternate;
}

.digital-text {
  animation: flicker 0.5s ease-in-out infinite alternate;
}

.glow-point {
  animation: pulse-point 2s infinite alternate;
}

.tech-line {
  stroke-dasharray: 12;
  stroke-dashoffset: 24;
  animation: circuit 4s linear infinite reverse;
}

.data-point {
  animation: pulse-point 1.5s infinite alternate;
}

@keyframes pulse {
  0% {
    transform: scale(0.95);
  }
  50% {
    transform: scale(1);
  }
  100% {
    transform: scale(0.95);
  }
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

@keyframes pulse-ring {
  0% {
    box-shadow: 0 0 0 0 rgba(255, 255, 255, 0.7);
  }
  50% {
    box-shadow: 0 0 0 10px rgba(255, 255, 255, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(255, 255, 255, 0);
  }
}

@keyframes wave {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-1px);
  }
}

@keyframes circuit {
  0% {
    stroke-dashoffset: 24;
  }
  100% {
    stroke-dashoffset: 0;
  }
}

@keyframes blink {
  0%, 80% {
    opacity: 1;
  }
  90% {
    opacity: 0.7;
  }
  100% {
    opacity: 1;
  }
}

@keyframes flicker {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.7;
  }
}

@keyframes pulse-point {
  0% {
    opacity: 0.3;
    transform: scale(0.8);
  }
  100% {
    opacity: 1;
    transform: scale(1.2);
  }
}
</style> 