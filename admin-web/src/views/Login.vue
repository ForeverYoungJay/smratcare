<template>
  <div class="login-page">
    <header class="login-top">
      <div class="login-top__brand">
        <img :src="brandLogoUrl" alt="弋阳龟峰颐养中心" class="login-top__logo" />
        <div>
          <div class="login-top__org">弋阳龟峰颐养中心</div>
          <div class="login-top__en">Yiyang Guifeng Elderly Care Center</div>
        </div>
        <span class="login-top__divider" aria-hidden="true" />
        <span class="login-top__motto">安心颐养 · 贴心照护 · 健康相伴</span>
      </div>
      <button type="button" class="login-top__home" @click="goEnterpriseHome"><HomeOutlined /> 返回官网</button>
    </header>

    <main class="login-main">
      <section class="login-hero">
        <h1 class="login-hero__title">员工管理服务平台</h1>
        <p class="login-hero__values">协同高效<i>|</i>规范管理<i>|</i>服务长者<i>|</i>共同成长</p>
      </section>

      <section class="login-card" aria-labelledby="login-card-title">
        <div class="login-card__head">
          <TeamOutlined class="login-card__icon" />
          <div>
            <h2 id="login-card-title">员工登录</h2>
            <p>颐养中心管理平台 · 内部专用</p>
          </div>
        </div>

        <a-form v-if="pwdChange.active" :model="pwdChange" layout="vertical" @finish="onSetInitialPassword">
          <a-alert
            type="warning"
            show-icon
            style="margin-bottom: 16px"
            message="首次登录请修改密码"
            description="你正在使用默认密码，设置新密码后才能进入系统。新密码至少 8 位，同时包含字母和数字。"
          />
          <a-form-item
            label="新密码"
            name="newPassword"
            :rules="[{ required: true, message: '请输入新密码' }, { pattern: /^(?=.*[A-Za-z])(?=.*\d).{8,}$/, message: '至少 8 位，同时包含字母和数字' }]"
          >
            <a-input-password v-model:value="pwdChange.newPassword" size="large" autocomplete="new-password" />
          </a-form-item>
          <a-form-item
            label="确认新密码"
            name="confirm"
            :rules="[{ required: true, message: '请再次输入新密码' }, { validator: checkConfirm }]"
          >
            <a-input-password v-model:value="pwdChange.confirm" size="large" autocomplete="new-password" />
          </a-form-item>
          <a-button type="primary" html-type="submit" block size="large" class="login-card__submit" :loading="pwdChange.submitting">设置新密码并登录</a-button>
          <div class="twofa-actions">
            <a-button type="link" size="small" @click="backToPasswordStep">返回重新登录</a-button>
          </div>
        </a-form>
        <a-form v-else-if="!twoFactor.active" ref="loginFormRef" :model="form" @finish="onSubmit">
          <a-form-item name="username" :rules="[{ required: true, message: '请输入员工账号、工号或手机号' }]">
            <a-input v-model:value="form.username" size="large" placeholder="员工账号 / 工号 / 手机号" name="username" autocomplete="username">
              <template #prefix><UserOutlined /></template>
            </a-input>
          </a-form-item>
          <a-form-item name="password" :rules="[{ required: true, message: '请输入密码' }]">
            <a-input-password v-model:value="form.password" size="large" placeholder="请输入密码" name="password" autocomplete="current-password">
              <template #prefix><LockOutlined /></template>
            </a-input-password>
          </a-form-item>
          <div class="login-card__row">
            <span>
              <a-checkbox v-model:checked="rememberAccount">记住账号</a-checkbox>
              <a-checkbox v-model:checked="rememberPassword">记住密码</a-checkbox>
            </span>
            <a class="login-card__link" @click="onForgotPassword">忘记密码？</a>
          </div>
          <a-button type="primary" html-type="submit" block size="large" class="login-card__submit" :loading="loading" @click.capture="syncAutofilledFields">登 录</a-button>
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
              size="large"
              placeholder="请输入短信验证码"
              :maxlength="8"
              autocomplete="one-time-code"
            />
          </a-form-item>
          <a-button type="primary" html-type="submit" block size="large" class="login-card__submit" :loading="twoFactor.submitting">验证并登录</a-button>
          <div class="twofa-actions">
            <a-button type="link" size="small" :disabled="twoFactor.countdown > 0" @click="onResendTwoFactor">
              {{ twoFactor.countdown > 0 ? `重新发送(${twoFactor.countdown}s)` : '重新发送验证码' }}
            </a-button>
            <a-button type="link" size="small" @click="backToPasswordStep">返回重新登录</a-button>
          </div>
        </a-form>

        <div class="login-card__other"><span>其他登录方式</span></div>
        <div class="login-card__alt">
          <!-- 扫码、工卡登录尚未接入后端，按钮如实标注，不模拟登录 -->
          <button type="button" @click="onUnavailable('扫码登录')"><QrcodeOutlined /> 扫码登录 <small>暂未开通</small></button>
          <button type="button" @click="onUnavailable('工卡登录')"><IdcardOutlined /> 工卡登录 <small>暂未开通</small></button>
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
      </section>
    </main>

    <footer class="login-foot">
      <ul class="login-features">
        <li><AppstoreOutlined /><div><b>一体化管理</b><span>业务协同 · 数据共享</span></div></li>
        <li><SafetyCertificateOutlined /><div><b>安全可靠</b><span>权限分级 · 数据加密</span></div></li>
        <li><TeamOutlined /><div><b>高效便捷</b><span>任务清晰 · 移动可用</span></div></li>
        <li><HeartOutlined /><div><b>以人为本</b><span>服务长者 · 关爱员工</span></div></li>
      </ul>
      <div class="login-copyright">© 弋阳龟峰颐养中心 ｜ 管理平台 v1.0</div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { message } from 'ant-design-vue'
import { onUnmounted, reactive, ref, watch } from 'vue'
import { AppstoreOutlined, HeartOutlined, HomeOutlined, IdcardOutlined, LockOutlined, QrcodeOutlined, SafetyCertificateOutlined, TeamOutlined, UserOutlined } from '@ant-design/icons-vue'
import { useRoute, useRouter } from 'vue-router'
import brandLogoUrl from '../assets/guifeng-logo.png'
import { login, setInitialPassword } from '../api/auth'
import { verifyTwoFactor, resendTwoFactorCode } from '../api/complianceSecurity'
import { useUserStore } from '../stores/user'
import { resolveAuthorizedLoginTarget } from '../router/permission'
import { resetBedMapDatasetState } from '../composables/useBedMapDataset'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 记住账号只保存账号，不保存密码；浏览器禁用存储时静默忽略
const REMEMBER_KEY = 'login.rememberedUsername'
function readRemembered() {
  try { return localStorage.getItem(REMEMBER_KEY) || '' } catch { return '' }
}
function writeRemembered(username: string) {
  try {
    if (username) localStorage.setItem(REMEMBER_KEY, username)
    else localStorage.removeItem(REMEMBER_KEY)
  } catch { /* 存储不可用时不影响登录 */ }
}

const form = reactive({
  username: readRemembered(),
  password: ''
})
const rememberAccount = ref(Boolean(form.username))
watch(rememberAccount, (on) => { if (!on) writeRemembered('') })

// 记住密码由本页自己存：浏览器密码管理器在 IP 访问（证书不匹配）和 Safari 下都不保存，用户勾了也没用。
// shortcut: 密码明文存在本机 localStorage，公用电脑上不要勾；要更安全时改成后端签发的长期登录令牌
const REMEMBER_PASSWORD_KEY = 'login.rememberPassword'
const SAVED_PASSWORD_KEY = 'login.savedPassword'
function readStore(key: string) {
  try { return localStorage.getItem(key) || '' } catch { return '' }
}
const rememberPassword = ref(readStore(REMEMBER_PASSWORD_KEY) === '1')
if (rememberPassword.value && form.username) form.password = readStore(SAVED_PASSWORD_KEY)
watch(rememberPassword, (on) => {
  try {
    if (on) localStorage.setItem(REMEMBER_PASSWORD_KEY, '1')
    else {
      localStorage.removeItem(REMEMBER_PASSWORD_KEY)
      localStorage.removeItem(SAVED_PASSWORD_KEY)
    }
  } catch { /* 存储不可用时不影响登录 */ }
})
// 记住密码要连账号一起记，否则下次只有密码没有账号
watch(rememberPassword, (on) => { if (on) rememberAccount.value = true })
watch(rememberAccount, (on) => { if (!on) rememberPassword.value = false })

function saveRememberedPassword(password: string) {
  try {
    if (rememberPassword.value && password) localStorage.setItem(SAVED_PASSWORD_KEY, password)
    else localStorage.removeItem(SAVED_PASSWORD_KEY)
  } catch { /* 存储不可用时不影响登录 */ }
}

// Chrome 自动填充的值在用户点击前不触发 input 事件，v-model 里还是空的，会被校验拦成“请输入密码”；
// 点击登录（此时浏览器已放开填充值）先把输入框里的值同步回表单再校验
const loginFormRef = ref<{ $el?: HTMLElement } | null>(null)
function syncAutofilledFields() {
  const root = loginFormRef.value?.$el
  const username = root?.querySelector<HTMLInputElement>('input[name="username"]')?.value
  const password = root?.querySelector<HTMLInputElement>('input[name="password"]')?.value
  if (username && !form.username) form.username = username
  if (password && !form.password) form.password = password
}

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

// 默认密码首次登录：第一步只返回改密令牌，设置新密码后才签发正式 token
const pwdChange = reactive({
  active: false,
  challengeToken: '',
  newPassword: '',
  confirm: '',
  submitting: false
})

function checkConfirm() {
  return pwdChange.confirm && pwdChange.confirm !== pwdChange.newPassword
    ? Promise.reject(new Error('两次输入的密码不一致'))
    : Promise.resolve()
}

// 登录类接口的返回：可能要求改密、可能要求 2FA，否则直接进入系统
async function handleLoginResult(res: any) {
  if (res?.mustChangePassword) {
    pwdChange.active = true
    pwdChange.challengeToken = res.challengeToken || ''
    pwdChange.newPassword = ''
    pwdChange.confirm = ''
    return
  }
  if (res?.requireTwoFactor) {
    // 机构安全策略开启 2FA：进入短信验证码第二步
    twoFactor.active = true
    twoFactor.challengeToken = res.challengeToken || ''
    twoFactor.phone = res.twoFactorPhone || ''
    twoFactor.code = ''
    startTwoFactorCountdown()
    return
  }
  await finishLogin(res)
}

async function onSetInitialPassword() {
  pwdChange.submitting = true
  try {
    const res = await setInitialPassword({ challengeToken: pwdChange.challengeToken, newPassword: pwdChange.newPassword })
    pwdChange.active = false
    pwdChange.challengeToken = ''
    form.password = pwdChange.newPassword // 记住密码时保存的是新密码，不是默认密码
    message.success('新密码已设置')
    await handleLoginResult(res)
  } catch (error: any) {
    // 错误提示由请求拦截器统一弹出；令牌失效时回到第一步
    const msg = String(error?.message || error?.response?.data?.message || '')
    if (msg.includes('重新登录')) backToPasswordStep()
  } finally {
    pwdChange.submitting = false
  }
}

function backToPasswordStep() {
  pwdChange.active = false
  pwdChange.challengeToken = ''
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
      { label: '管理层 logistics_director', username: 'logistics_director' },
      { label: '保卫 guard_demo', username: 'guard_demo' }
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

async function onSubmit() {
  loading.value = true
  try {
    const res = await login(form)
    writeRemembered(rememberAccount.value ? form.username.trim() : '')
    await handleLoginResult(res)
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
  resetBedMapDatasetState()
  userStore.setAuth(res)
  saveRememberedPassword(form.password)
  const redirect = normalizeRedirectPath(typeof route.query.redirect === 'string' ? route.query.redirect : '')
  const nextPath = resolveAuthorizedLoginTarget(router, redirect, userStore.roles || [], userStore.pagePermissions || [], userStore.permissions || [])
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

// 官网是独立页面 home.html：生产 nginx 会把它 301 到 /；开发服务器上 / 是后台，直接去 / 会被送回登录页
function goEnterpriseHome() {
  window.location.assign('/home.html')
}

function onForgotPassword() {
  message.info('员工账号的密码由行政人事部重置，请联系行政人事部或部门负责人。')
}

function onUnavailable(name: string) {
  message.info(`${name}暂未开通，请使用账号密码登录。`)
}
</script>

<style scoped>
.login-page {
  --login-green: #17734f;
  --login-green-dark: #0f5c3f;
  --login-ink: #1f3a2e;
  position: relative;
  display: grid;
  grid-template-rows: auto 1fr auto;
  grid-template-columns: minmax(0, 1fr);
  min-height: 100vh;
  padding: 28px 56px 24px;
  overflow: hidden;
  color: var(--login-ink);
  background-color: #eef5ef;
  background-image:
    linear-gradient(90deg, rgba(246, 251, 247, 0.94) 0%, rgba(246, 251, 247, 0.78) 38%, rgba(246, 251, 247, 0.28) 70%, rgba(246, 251, 247, 0.12) 100%),
    linear-gradient(180deg, rgba(232, 244, 236, 0.35) 0%, rgba(232, 244, 236, 0) 40%, rgba(214, 236, 222, 0.75) 100%),
    url('../assets/home-login.jpg');
  background-size: cover;
  background-position: center;
}

.login-top { display: flex; align-items: center; justify-content: space-between; gap: 16px; }
.login-top__brand { display: flex; align-items: center; gap: 14px; min-width: 0; }
.login-top__logo { width: 58px; height: 58px; object-fit: contain; }
.login-top__org { font-size: 26px; font-weight: 800; letter-spacing: 0.04em; color: var(--login-green-dark); line-height: 1.2; }
.login-top__en { font-size: 12px; font-weight: 600; color: #3d5a4b; }
.login-top__divider { width: 1px; height: 40px; margin: 0 10px; background: rgba(31, 58, 46, 0.35); }
.login-top__motto { font-size: 18px; letter-spacing: 0.12em; color: var(--login-ink); white-space: nowrap; }
.login-top__home {
  flex: none; display: inline-flex; align-items: center; gap: 6px; padding: 8px 18px;
  border: 1px solid rgba(23, 115, 79, 0.45); border-radius: 999px;
  background: rgba(255, 255, 255, 0.72); color: var(--login-green-dark); font-weight: 600; cursor: pointer;
}
.login-top__home:hover { background: #fff; }

.login-main { display: flex; align-items: center; justify-content: space-between; gap: 48px; padding: 24px 0; }
.login-hero { padding-left: clamp(0px, 6vw, 110px); }
.login-hero__title {
  position: relative; display: inline-block; margin: 0 0 22px; padding-bottom: 14px;
  font-family: 'STKaiti', 'KaiTi', 'Kaiti SC', 'BiauKai', serif;
  font-size: clamp(40px, 4.6vw, 68px); font-weight: 700; letter-spacing: 0.06em; color: #12402d;
}
.login-hero__title::after {
  content: ''; position: absolute; left: 22%; right: 12%; bottom: 0; height: 4px; border-radius: 4px;
  background: linear-gradient(90deg, rgba(222, 170, 70, 0), #dfa84a 30%, #dfa84a 70%, rgba(222, 170, 70, 0));
}
.login-hero__values { margin: 0; font-size: 20px; letter-spacing: 0.22em; color: var(--login-ink); }
.login-hero__values i { margin: 0 14px; font-style: normal; opacity: 0.55; }

.login-card {
  position: relative; flex: none; width: 460px; max-width: 100%; padding: 40px 44px 30px;
  border-radius: 28px; background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 24px 60px rgba(18, 64, 45, 0.18); overflow: hidden;
}
.login-card::before {
  content: ''; position: absolute; top: -60px; right: -60px; width: 200px; height: 160px; border-radius: 50%;
  background: radial-gradient(circle at 30% 70%, #cfe9d6, #e9f5ec 60%, transparent 70%); pointer-events: none;
}
.login-card__head { position: relative; display: flex; align-items: center; gap: 16px; margin-bottom: 28px; }
.login-card__icon { font-size: 48px; color: var(--login-green); }
.login-card__head h2 { margin: 0; font-size: 30px; font-weight: 800; color: #12402d; }
.login-card__head p { margin: 2px 0 0; color: #4d6458; font-size: 15px; }
.login-card :deep(.ant-input-affix-wrapper) { height: 52px; border-radius: 12px; }
.login-card :deep(.ant-input-prefix) { margin-right: 10px; color: #4d6458; font-size: 18px; }
.login-card__row { display: flex; justify-content: space-between; align-items: center; margin: -4px 0 18px; }
.login-card__link { color: var(--login-green); }
.login-card__submit.ant-btn { height: 52px; border-radius: 12px; font-size: 18px; letter-spacing: 0.4em; }
.login-card__other { display: flex; align-items: center; gap: 14px; margin: 26px 0 16px; color: #4d6458; }
.login-card__other::before, .login-card__other::after { content: ''; flex: 1; height: 1px; background: #e2ebe5; }
.login-card__alt { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
.login-card__alt button {
  display: inline-flex; align-items: center; justify-content: center; gap: 8px; height: 50px;
  border: 0; border-radius: 12px; background: #f0f7f2; color: var(--login-ink); font-size: 15px; cursor: pointer;
}
.login-card__alt button small { color: #8a9a91; font-size: 11px; }

.twofa-actions { margin-top: 8px; display: flex; justify-content: space-between; }

.login-foot { display: flex; align-items: flex-end; justify-content: space-between; gap: 24px; }
.login-features {
  display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 0; margin: 0; padding: 18px 10px;
  list-style: none; border-radius: 22px; background: rgba(255, 255, 255, 0.86); max-width: 960px; flex: 1;
}
.login-features li { display: flex; align-items: center; gap: 12px; padding: 0 18px; border-left: 1px solid #e2ebe5; }
.login-features li:first-child { border-left: 0; }
.login-features li > :first-child { font-size: 30px; color: var(--login-green); }
.login-features b { display: block; font-size: 16px; color: var(--login-ink); }
.login-features span { font-size: 12px; color: #5c6f65; }
.login-copyright { flex: none; font-size: 13px; color: #3d5a4b; }

.demo-panel { margin-top: 16px; padding: 10px 12px; border-radius: 10px; background: #f7f8f4; border: 1px solid #e4e7e0; }
.demo-panel summary { cursor: pointer; font-size: 12px; color: #3c4f48; font-weight: 600; }
.demo-help { margin: 8px 0 10px; font-size: 12px; color: #5c6f69; }
.demo-groups { display: grid; gap: 8px; max-height: 240px; overflow: auto; padding-right: 2px; }
.demo-group-title { font-size: 12px; color: #22332e; margin-bottom: 6px; }
.demo-users { display: flex; flex-wrap: wrap; gap: 6px; }
.demo-user { border: 1px solid rgba(33, 112, 95, 0.28); border-radius: 999px; background: #e8f3ef; color: #185c4e; padding: 3px 9px; font-size: 12px; cursor: pointer; }
.demo-user:hover { background: #dcede6; }

@media (max-width: 1200px) {
  .login-top__divider, .login-top__motto { display: none; }
  .login-features { grid-template-columns: repeat(2, minmax(0, 1fr)); row-gap: 12px; }
  .login-features li:nth-child(3) { border-left: 0; }
}
@media (max-width: 900px) {
  .login-page { padding: 16px; }
  .login-main { justify-content: center; padding: 20px 0; }
  .login-hero, .login-features { display: none; }
  .login-foot { justify-content: center; }
  .login-top__org { font-size: 18px; }
  .login-top__logo { width: 40px; height: 40px; }
  .login-top__en { display: none; }
  .login-card { padding: 28px 22px 22px; border-radius: 20px; }
  .login-card__head h2 { font-size: 24px; }
  .login-card__alt button { flex-direction: column; gap: 0; height: 58px; }
}
</style>
