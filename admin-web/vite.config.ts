import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

const apiProxyTarget = process.env.VITE_API_PROXY_TARGET || 'http://localhost:8080'

export default defineConfig({
  plugins: [vue()],
  test: {
    environment: 'jsdom',
    globals: true,
    include: ['src/**/*.test.ts']
  },
  server: {
    host: '127.0.0.1',
    port: 5174,
    strictPort: false,
    allowedHosts: ['considerable-modified-census-sussex.trycloudflare.com'],
    proxy: {
      '/api': {
        target: apiProxyTarget,
        changeOrigin: true
      }
    }
  }
})
