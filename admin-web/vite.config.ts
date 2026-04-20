import { resolve } from 'node:path'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

const apiProxyTarget = process.env.VITE_API_PROXY_TARGET || 'http://localhost:8080'

function manualChunks(id: string) {
  if (!id.includes('node_modules')) {
    if (
      id.includes('/src/views/EnterpriseHome.vue') ||
      id.includes('/src/constants/enterpriseProfile') ||
      id.includes('/src/assets/home.')
    ) {
      return 'enterprise-home'
    }

    return undefined
  }

  if (
    id.includes('/echarts/') ||
    id.includes('/vue-echarts/') ||
    id.includes('/@fullcalendar/') ||
    id.includes('/@logicflow/') ||
    id.includes('/@wangeditor/') ||
    id.includes('/vxe-table/') ||
    id.includes('/xe-utils/') ||
    id.includes('/ant-design-x-vue/') ||
    id.includes('/three/') ||
    id.includes('/@tresjs/') ||
    id.includes('/jspdf') ||
    id.includes('/jspdf-autotable')
  ) {
    return 'admin-heavy'
  }

  if (
    id.includes('/ant-design-vue/') ||
    id.includes('/@ant-design/')
  ) {
    return 'ui-vendor'
  }

  if (
    id.includes('/vue/') ||
    id.includes('/vue-router/') ||
    id.includes('/pinia/') ||
    id.includes('/dayjs/') ||
    id.includes('/axios/')
  ) {
    return 'framework-vendor'
  }

  return 'vendor'
}

export default defineConfig({
  plugins: [vue()],
  resolve: {
    extensions: ['.ts', '.tsx', '.mts', '.js', '.jsx', '.mjs', '.json']
  },
  build: {
    rollupOptions: {
      input: {
        admin: resolve(__dirname, 'index.html'),
        home: resolve(__dirname, 'home.html')
      },
      output: {
        manualChunks
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
