import { resolve } from 'node:path'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

const apiProxyTarget = process.env.VITE_API_PROXY_TARGET || 'http://localhost:8080'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    extensions: ['.ts', '.tsx', '.mts', '.js', '.jsx', '.mjs', '.json']
  },
  build: {
    rollupOptions: {
      input: {
        admin: resolve(__dirname, 'index.html'),
        home: resolve(__dirname, 'home.html'),
        about: resolve(__dirname, 'about.html'),
        services: resolve(__dirname, 'services.html'),
        pricing: resolve(__dirname, 'pricing.html'),
        faq: resolve(__dirname, 'faq.html'),
        contact: resolve(__dirname, 'contact.html'),
        disabilityCare: resolve(__dirname, 'disability-care.html'),
        memoryCare: resolve(__dirname, 'memory-care.html'),
        rehabilitation: resolve(__dirname, 'rehabilitation.html')
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
