import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

const apiProxyTarget = process.env.VITE_API_PROXY_TARGET || 'http://localhost:8080'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    extensions: ['.ts', '.tsx', '.mts', '.js', '.jsx', '.mjs', '.json']
  },
  build: {
    sourcemap: false,
    chunkSizeWarningLimit: 900,
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (!id.includes('node_modules')) {
            return
          }
          if (id.includes('ant-design-vue') || id.includes('@ant-design')) {
            return 'vendor-antd'
          }
          if (id.includes('vue') || id.includes('pinia') || id.includes('vue-router')) {
            return 'vendor-vue'
          }
          if (id.includes('echarts') || id.includes('vue-echarts')) {
            return 'vendor-echarts'
          }
          if (id.includes('vxe-table') || id.includes('xe-utils')) {
            return 'vendor-vxe'
          }
          if (id.includes('@fullcalendar')) {
            return 'vendor-calendar'
          }
          if (id.includes('@wangeditor')) {
            return 'vendor-editor'
          }
          if (id.includes('@logicflow')) {
            return 'vendor-flow'
          }
          if (id.includes('ant-design-x-vue')) {
            return 'vendor-antdx'
          }
        }
      }
    }
  },
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
      },
      '/ws': {
        target: apiProxyTarget,
        changeOrigin: true,
        ws: true
      }
    }
  }
})
