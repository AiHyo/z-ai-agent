import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'

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
      component: () => import('../views/LoveAppView.vue')
    },
    {
      path: '/manus-app',
      name: 'manusApp',
      component: () => import('../views/ManusAppView.vue')
    }
  ]
})

export default router 