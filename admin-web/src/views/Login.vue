<template>
  <div class="login-page">
    <div class="login-card">
      <div class="brand">
        <div class="logo">智</div>
        <div>
          <div class="title">智养云管理后台</div>
          <div class="subtitle">SaaS 智慧养老 OA</div>
        </div>
      </div>
      <a-form :model="form" @finish="onSubmit" layout="vertical">
        <a-form-item label="用户名" name="username" :rules="[{ required: true, message: '请输入用户名' }]">
          <a-input v-model:value="form.username" placeholder="admin" />
        </a-form-item>
        <a-form-item label="密码" name="password" :rules="[{ required: true, message: '请输入密码' }]">
          <a-input-password v-model:value="form.password" placeholder="******" />
        </a-form-item>
        <a-button type="primary" html-type="submit" block :loading="loading">登录</a-button>
        <a-button block style="margin-top: 8px" @click="goEnterpriseHome">返回企业首页</a-button>
      </a-form>
      <div class="tips">
        推荐使用现代浏览器访问以获得最佳体验
      </div>
      <details v-if="showDemoPanel" class="demo-panel">
        <summary>演示账号快捷填充（开发调试）</summary>
        <p class="demo-help">点击账号会自动填入用户名与默认密码（123456）</p>
        <div class="demo-groups">
          <div v-for="group in demoAccounts" :key="group.name" class="demo-group">
            <div class="demo-group-title">{{ group.name }}</div>
            <div class="demo-users">
              <button
                v-for="item in group.items"
                :key="item.username"
                type="button"
                class="demo-user"
                @click="fillDemo(item.username)"
              >
                {{ item.label }}
              </button>
            </div>
          </div>
        </div>
      </details>
    </div>
  </div>
</template>

<script setup lang="ts">
import { message } from 'ant-design-vue'
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { login } from '../api/auth'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const form = reactive({
  username: '',
  password: ''
})

const loading = ref(false)
const showDemoPanel = import.meta.env.DEV || import.meta.env.VITE_ENABLE_DEMO_LOGIN === 'true'
const demoAccounts = [
  {
    name: '系统管理',
    items: [{ label: 'admin（系统超管）', username: 'admin' }]
  },
  {
    name: '护理部',
    items: [
      { label: '员工 nursing_emp', username: 'nursing_emp' },
      { label: '部长 nursing_minister', username: 'nursing_minister' },
      { label: '管理层 nursing_director', username: 'nursing_director' }
    ]
  },
  {
    name: '医务部',
    items: [
      { label: '员工 medical_emp', username: 'medical_emp' },
      { label: '部长 medical_minister', username: 'medical_minister' },
      { label: '管理层 medical_director', username: 'medical_director' }
    ]
  },
  {
    name: '财务部',
    items: [
      { label: '员工 finance_emp', username: 'finance_emp' },
      { label: '部长 finance_minister', username: 'finance_minister' },
      { label: '管理层 finance_director', username: 'finance_director' }
    ]
  },
  {
    name: '后勤部',
    items: [
      { label: '员工 logistics_emp', username: 'logistics_emp' },
      { label: '部长 logistics_minister', username: 'logistics_minister' },
      { label: '管理层 logistics_director', username: 'logistics_director' }
    ]
  },
  {
    name: '行政人事部',
    items: [
      { label: '员工 hr_emp', username: 'hr_emp' },
      { label: '部长 hr_minister', username: 'hr_minister' },
      { label: '管理层 hr_director', username: 'hr_director' }
    ]
  },
  {
    name: '市场部',
    items: [
      { label: '员工 marketing_emp', username: 'marketing_emp' },
      { label: '部长 marketing_minister', username: 'marketing_minister' },
      { label: '管理层 marketing_director', username: 'marketing_director' }
    ]
  }
] as const

function fillDemo(username: string) {
  form.username = username
  form.password = '123456'
}

async function onSubmit() {
  loading.value = true
  try {
    const res = await login(form)
    userStore.setAuth(res)
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : ''
    router.push(redirect || '/portal')
  } catch (error: any) {
    const status = Number(error?.response?.status || 0)
    const backendMsg = String(error?.response?.data?.message || error?.response?.data?.msg || error?.message || '')
    const normalized = backendMsg.toLowerCase()
    if (normalized.includes('disable') || normalized.includes('禁用') || normalized.includes('停用')) {
      message.error('账号已被禁用，请联系管理员处理。')
      return
    }
    if (normalized.includes('password') || normalized.includes('密码') || normalized.includes('凭证') || normalized.includes('401')) {
      message.error('账号或密码错误，请重新输入。')
      return
    }
    if (status === 401) {
      message.error('账号或密码错误，请重新输入。')
      return
    }
    if (status === 403) {
      message.error('账号已被禁用或无登录权限，请联系管理员。')
      return
    }
    if (status >= 500) {
      message.error('服务暂时不可用（服务器异常），请稍后重试。')
      return
    }
    if (!error?.response) {
      message.error('网络异常，无法连接服务器，请检查网络或稍后重试。')
      return
    }
    message.error('登录失败，请稍后重试。')
  } finally {
    loading.value = false
  }
}

function goEnterpriseHome() {
  router.push('/home')
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  background-image: url('../assets/home-login.jpg');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

.login-card {
  width: 360px;
  background: rgba(255, 255, 255, 0.88);
  border: 1px solid rgba(148, 163, 184, 0.4);
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 24px 48px rgba(15, 23, 42, 0.12);
  backdrop-filter: blur(12px);
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}

.logo {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: linear-gradient(135deg, #1677ff, #22d3ee);
  color: #0f172a;
  font-weight: 700;
  display: grid;
  place-items: center;
}

.title {
  font-size: 16px;
  font-weight: 600;
}

.subtitle {
  font-size: 12px;
  color: var(--muted);
}

.tips {
  margin-top: 16px;
  font-size: 12px;
  color: var(--muted);
  text-align: center;
}

.demo-panel {
  margin-top: 14px;
  padding: 10px 12px;
  border-radius: 10px;
  background: rgba(241, 245, 249, 0.85);
  border: 1px solid rgba(148, 163, 184, 0.35);
}

.demo-panel summary {
  cursor: pointer;
  font-size: 12px;
  color: #334155;
  font-weight: 600;
}

.demo-help {
  margin: 8px 0 10px;
  font-size: 12px;
  color: #64748b;
}

.demo-groups {
  display: grid;
  gap: 8px;
  max-height: 240px;
  overflow: auto;
  padding-right: 2px;
}

.demo-group-title {
  font-size: 12px;
  color: #0f172a;
  margin-bottom: 6px;
}

.demo-users {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.demo-user {
  border: 1px solid rgba(59, 130, 246, 0.35);
  background: rgba(219, 234, 254, 0.65);
  color: #1e3a8a;
  border-radius: 999px;
  padding: 3px 9px;
  font-size: 12px;
  cursor: pointer;
}

.demo-user:hover {
  background: rgba(191, 219, 254, 0.9);
}
</style>
