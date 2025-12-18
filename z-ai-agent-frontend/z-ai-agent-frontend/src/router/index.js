import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import { authApi } from '@/services/api'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView
    },
    {
      path: '/love-app',
      name: 'loveApp',
      component: () => import('../views/LoveAppView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/manus-app',
      name: 'manusApp',
      component: () => import('../views/ManusAppView.vue'),
      meta: { requiresAuth: true }
    }
  ]
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  // 检查路由是否需要认证
  if (to.matched.some(record => record.meta.requiresAuth)) {
    const token = localStorage.getItem('Authorization')
    
    if (!token) {
      // 没有token，保存目标路由并跳转到首页，触发登录弹窗
      localStorage.setItem('redirectAfterLogin', to.fullPath)
      // 触发显示登录弹窗事件
      setTimeout(() => {
        window.dispatchEvent(new CustomEvent('show-login-modal'))
      }, 100)
      next('/')
      return
    }

    try {
      // 验证token是否有效
      const response = await authApi.isLogin()
      if (response.code === 0 && response.data === true) {
        // token有效，允许访问
        next()
      } else {
        // token无效，清除并跳转到首页
        localStorage.removeItem('Authorization')
        localStorage.setItem('redirectAfterLogin', to.fullPath)
        setTimeout(() => {
          window.dispatchEvent(new CustomEvent('show-login-modal'))
        }, 100)
        next('/')
      }
    } catch (error) {
      // 验证失败，清除token并跳转到首页
      console.error('Token验证失败:', error)
      localStorage.removeItem('Authorization')
      localStorage.setItem('redirectAfterLogin', to.fullPath)
      setTimeout(() => {
        window.dispatchEvent(new CustomEvent('show-login-modal'))
      }, 100)
      next('/')
    }
  } else {
    // 不需要认证的路由，直接允许访问
    next()
  }
})

export default router 