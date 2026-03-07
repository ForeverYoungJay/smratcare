<template>
  <div class="login-page" :style="{ '--login-mask-opacity': String(maskOpacity) }">
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
      </a-form>
      <div class="tips">
        推荐使用现代浏览器访问以获得最佳体验
      </div>
      <div class="bg-control">
        <span>背景遮罩</span>
        <a-slider v-model:value="maskOpacity" :min="0.2" :max="0.75" :step="0.01" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '../api/auth'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

const form = reactive({
  username: '',
  password: ''
})

const loading = ref(false)
const MASK_OPACITY_KEY = 'zhiyangyun_login_mask_opacity'
const maskOpacity = ref(0.5)

onMounted(() => {
  const raw = localStorage.getItem(MASK_OPACITY_KEY)
  const val = Number(raw)
  if (Number.isFinite(val) && val >= 0.2 && val <= 0.75) {
    maskOpacity.value = val
  }
})

watch(maskOpacity, (value) => {
  localStorage.setItem(MASK_OPACITY_KEY, String(value))
})

async function onSubmit() {
  loading.value = true
  try {
    const res = await login(form)
    userStore.setAuth(res)
    router.push('/portal')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  background-image:
    linear-gradient(135deg, rgba(13, 47, 110, var(--login-mask-opacity)), rgba(20, 84, 184, var(--login-mask-opacity))),
    url('../assets/home-login.jpg');
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

.bg-control {
  margin-top: 14px;
  font-size: 12px;
  color: var(--muted);
}
</style>
