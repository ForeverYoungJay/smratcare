<template>
  <div class="login-page">
    <div class="login-shell">
      <aside class="login-hero">
        <div class="login-hero__brand">
          <img :src="brandLogoUrl" alt="龟峰颐养中心" class="login-hero__logo" />
          <div>
            <div class="login-hero__org">龟峰颐养中心</div>
            <div class="login-hero__platform">智养云 · 运营管理平台</div>
          </div>
        </div>
        <h1 class="login-hero__slogan">照护、健康、安全、经营，一体化管理</h1>
        <p class="login-hero__desc">围绕长者全生命周期，把入住、护理、医疗、安全、家属服务与经营数据收拢在同一平台。</p>
        <ul class="login-hero__features">
          <li><span>护</span>长者全生命周期与护理执行闭环</li>
          <li><span>安</span>生命体征与安全事件实时预警</li>
          <li><span>亲</span>家属服务、探访与关怀协同</li>
          <li><span>营</span>床位、收费与经营一屏掌握</li>
        </ul>
      </aside>
      <div class="login-card">
      <div class="brand">
        <div class="logo">
          <img :src="brandLogoUrl" alt="龟峰颐养中心logo" class="logo-image" />
        </div>
        <div>
          <div class="title">智养云运营中台</div>
          <div class="subtitle">智慧养老管理平台</div>
        </div>
      </div>
      <a-form v-if="!twoFactor.active" :model="form" @finish="onSubmit" layout="vertical">
        <a-form-item label="用户名" name="username" :rules="[{ required: true, message: '请输入用户名' }]">
          <a-input v-model:value="form.username" placeholder="admin" />
        </a-form-item>
        <a-form-item label="密码" name="password" :rules="[{ required: true, message: '请输入密码' }]">
          <a-input-password v-model:value="form.password" placeholder="******" />
        </a-form-item>
        <a-button type="primary" html-type="submit" block :loading="loading">登录</a-button>
        <a-button block style="margin-top: 8px" @click="goEnterpriseHome">返回企业首页</a-button>
      </a-form>
      <a-form v-else layout="vertical" @finish="onVerifyTwoFactor">
        <a-alert
          type="info"
          show-icon
          style="margin-bottom: 16px"
          message="安全验证（双因子登录）"
          :description="`本机构已开启双因子登录，验证码已发送至 ${twoFactor.phone || '预留手机号'}`"
        />
        <a-form-item label="短信验证码">
          <a-input
            v-model:value="twoFactor.code"
            placeholder="请输入短信验证码"
            :maxlength="8"
            autocomplete="one-time-code"
          />
        </a-form-item>
        <a-button type="primary" html-type="submit" block :loading="twoFactor.submitting">验证并登录</a-button>
        <div class="twofa-actions">
          <a-button type="link" size="small" :disabled="twoFactor.countdown > 0" @click="onResendTwoFactor">
            {{ twoFactor.countdown > 0 ? `重新发送(${twoFactor.countdown}s)` : '重新发送验证码' }}
          </a-button>
          <a-button type="link" size="small" @click="backToPasswordStep">返回重新登录</a-button>
        </div>
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
  </div>
</template>

<script setup lang="ts">
import { message } from 'ant-design-vue'
import { onUnmounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import brandLogoUrl from '../assets/guifeng-logo.png'
import { login } from '../api/auth'
import { verifyTwoFactor, resendTwoFactorCode } from '../api/complianceSecurity'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const form = reactive({
  username: '',
  password: ''
})

const loading = ref(false)

// 双因子登录第二步（机构安全策略开启 2FA 时，第一步只返回 challengeToken）
const twoFactor = reactive({
  active: false,
  challengeToken: '',
  phone: '',
  code: '',
  submitting: false,
  countdown: 0
})
let countdownTimer: ReturnType<typeof setInterval> | null = null

function startTwoFactorCountdown(seconds = 60) {
  twoFactor.countdown = seconds
  if (countdownTimer) clearInterval(countdownTimer)
  countdownTimer = setInterval(() => {
    twoFactor.countdown -= 1
    if (twoFactor.countdown <= 0 && countdownTimer) {
      clearInterval(countdownTimer)
      countdownTimer = null
    }
  }, 1000)
}

function backToPasswordStep() {
  twoFactor.active = false
  twoFactor.challengeToken = ''
  twoFactor.phone = ''
  twoFactor.code = ''
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
  twoFactor.countdown = 0
}

onUnmounted(() => {
  if (countdownTimer) clearInterval(countdownTimer)
})

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

function normalizeRedirectPath(path?: string | null) {
  const raw = String(path || '').trim()
  if (!raw.startsWith('/')) return ''
  if (raw === '/login') return ''
  if (raw.startsWith('/login?')) {
    const query = raw.split('?')[1] || ''
    const params = new URLSearchParams(query)
    return normalizeRedirectPath(params.get('redirect'))
  }
  return raw
}

function fillDemo(username: string) {
  form.username = username
  form.password = '123456'
}

function normalizeRoleList(roles: string[]) {
  return roles.map((role) => String(role || '').trim().toUpperCase()).filter(Boolean)
}

function resolvePostLoginPath(roles: string[]) {
  const normalized = normalizeRoleList(roles)
  const hasManageRole = normalized.some((role) => ['ADMIN', 'SYS_ADMIN', 'DIRECTOR'].includes(role))
  const hasEmployeeRole = normalized.some((role) => role.endsWith('_EMPLOYEE') || role.endsWith('_MINISTER'))
  return hasEmployeeRole && !hasManageRole ? '/workbench/overview' : '/portal'
}

async function onSubmit() {
  loading.value = true
  try {
    const res = await login(form)
    if ((res as any)?.requireTwoFactor) {
      // 机构安全策略开启 2FA：进入短信验证码第二步
      twoFactor.active = true
      twoFactor.challengeToken = (res as any).challengeToken || ''
      twoFactor.phone = (res as any).twoFactorPhone || ''
      twoFactor.code = ''
      startTwoFactorCountdown()
      return
    }
    await finishLogin(res)
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

async function finishLogin(res: any) {
  userStore.setAuth(res)
  const redirect = normalizeRedirectPath(typeof route.query.redirect === 'string' ? route.query.redirect : '')
  const fallbackPath = resolvePostLoginPath(res.roles || [])
  const nextPath = redirect || fallbackPath
  await router.replace(nextPath)
  message.success(nextPath.startsWith('/workbench') ? '登录成功，已进入个人工作台' : '登录成功')
}

async function onVerifyTwoFactor() {
  const code = twoFactor.code.trim()
  if (!code) {
    message.warning('请输入短信验证码')
    return
  }
  twoFactor.submitting = true
  try {
    const res = await verifyTwoFactor({ challengeToken: twoFactor.challengeToken, verifyCode: code })
    backToPasswordStep()
    await finishLogin(res)
  } catch (error: any) {
    // 验证码错误等提示由请求拦截器统一弹出；会话失效时回到第一步
    const msg = String(error?.message || error?.response?.data?.message || '')
    if (msg.includes('重新登录')) {
      backToPasswordStep()
    }
  } finally {
    twoFactor.submitting = false
  }
}

async function onResendTwoFactor() {
  try {
    const res = await resendTwoFactorCode({ challengeToken: twoFactor.challengeToken })
    startTwoFactorCountdown(res?.retryAfterSeconds || 60)
    message.success('验证码已重新发送')
  } catch (error: any) {
    const msg = String(error?.message || error?.response?.data?.message || '')
    if (msg.includes('重新登录')) {
      backToPasswordStep()
    }
  }
}

function goEnterpriseHome() {
  window.location.assign('/')
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 24px;
  background-image:
    linear-gradient(135deg, rgba(24, 92, 78, 0.82) 0%, rgba(33, 112, 95, 0.66) 46%, rgba(46, 138, 114, 0.5) 100%),
    url('../assets/home-login.jpg');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

.login-shell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 56px;
  width: 100%;
  max-width: 960px;
}

.login-hero {
  flex: 1 1 0;
  max-width: 460px;
  color: #ffffff;
}

.login-hero__brand {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 30px;
}

.login-hero__logo {
  width: 54px;
  height: 54px;
  padding: 10px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.18);
  object-fit: contain;
}

.login-hero__org {
  font-size: 22px;
  font-weight: 800;
  letter-spacing: 0.02em;
}

.login-hero__platform {
  margin-top: 2px;
  font-size: 13px;
  opacity: 0.85;
}

.login-hero__slogan {
  margin: 0 0 14px;
  font-size: 30px;
  line-height: 1.3;
  font-weight: 800;
  color: #ffffff;
}

.login-hero__desc {
  margin: 0 0 28px;
  max-width: 420px;
  font-size: 14px;
  line-height: 1.85;
  opacity: 0.9;
}

.login-hero__features {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  gap: 14px;
}

.login-hero__features li {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
}

.login-hero__features li span {
  flex: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  border-radius: 9px;
  background: rgba(255, 255, 255, 0.2);
  font-size: 13px;
  font-weight: 700;
}

.login-card {
  flex: none;
  width: 380px;
  max-width: 100%;
  background: #ffffff;
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 10px 32px rgba(16, 46, 39, 0.28);
}

@media (max-width: 880px) {
  .login-shell {
    gap: 0;
  }

  .login-hero {
    display: none;
  }
}

.login-card :deep(.ant-btn) {
  height: 40px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 6px;
  padding-bottom: 18px;
  border-bottom: 1px solid var(--border-soft, #edefe9);
}

.brand + .ant-form,
.brand + form {
  margin-top: 18px;
}

.logo {
  width: 46px;
  height: 46px;
  border-radius: 12px;
  display: grid;
  place-items: center;
  background: var(--primary-soft, #e8f3ef);
  border: 1px solid rgba(33, 112, 95, 0.18);
}

.logo-image {
  width: 28px;
  height: 28px;
  object-fit: contain;
}

.title {
  font-size: 17px;
  font-weight: 700;
  color: var(--ink, #22332e);
}

.subtitle {
  font-size: 12px;
  color: var(--muted, #5c6f69);
}

.twofa-actions {
  margin-top: 8px;
  display: flex;
  justify-content: space-between;
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
  background: #f7f8f4;
  border: 1px solid var(--border, #e4e7e0);
}

.demo-panel summary {
  cursor: pointer;
  font-size: 12px;
  color: var(--ink-soft, #3c4f48);
  font-weight: 600;
}

.demo-help {
  margin: 8px 0 10px;
  font-size: 12px;
  color: var(--muted, #5c6f69);
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
  color: var(--ink, #22332e);
  margin-bottom: 6px;
}

.demo-users {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.demo-user {
  border: 1px solid rgba(33, 112, 95, 0.28);
  border-radius: 999px;
  background: #e8f3ef;
  color: #185c4e;
  padding: 3px 9px;
  font-size: 12px;
  cursor: pointer;
}

.demo-user:hover {
  background: #dcede6;
}
</style>
