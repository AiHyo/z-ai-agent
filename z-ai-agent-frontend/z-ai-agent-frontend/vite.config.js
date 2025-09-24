import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

// https://vitejs.dev/config/

const DEV_BASE_URL = "http://localhost:8123";
const PROD_BASE_URL = "http://8.138.251.219:8124";

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src'),
    },
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: PROD_BASE_URL,
        changeOrigin: true,
        secure: false,
        // 如果后端API已经包含/api前缀，则不需要重写路径
        // rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  }
})
