<template>
  <a-layout class="app-layout">
    <a-layout-sider v-model:collapsed="collapsed" collapsible class="app-sider">
      <div class="brand">
        <div class="logo">智</div>
        <div class="brand-text" v-if="!collapsed">
          <div class="title">智养云</div>
          <div class="subtitle">智慧养老 OA</div>
        </div>
      </div>
      <a-menu
        theme="dark"
        mode="inline"
        :selectedKeys="selectedKeys"
        :openKeys="openKeys"
        @openChange="onOpenChange"
        :items="menuItems"
        @click="onMenuClick"
        class="side-menu"
      />
    </a-layout-sider>

    <a-layout class="app-main">
      <a-layout-header class="app-header">
        <div class="header-left">
          <div class="page-title">{{ currentTitle || '工作台' }}</div>
          <a-breadcrumb class="breadcrumb">
            <a-breadcrumb-item v-for="(bc, idx) in breadcrumbs" :key="idx">
              {{ bc }}
            </a-breadcrumb-item>
          </a-breadcrumb>
        </div>
        <div class="header-right">
          <span class="today-label">{{ todayLabel }}</span>
          <a-badge status="processing" text="系统运行中" class="system-status" />
          <span class="system-name">龟峰颐养中心智慧养老管理平台</span>
          <a-dropdown trigger="click">
            <a-badge :count="quickNotifyItems.length" size="small">
              <a-button size="small">快捷通知</a-button>
            </a-badge>
            <template #overlay>
              <a-menu>
                <a-menu-item v-for="item in quickNotifyItems" :key="item.title" @click="router.push(item.route)">
                  {{ item.title }}
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
          <a-dropdown>
            <a class="user-link user-entry">
              <a-avatar :size="28" :src="headerSettings.avatarUrl || undefined">
                {{ (userStore.staffInfo?.realName || '管').slice(0, 1) }}
              </a-avatar>
              <span>{{ userStore.staffInfo?.realName || '管理员' }}</span>
            </a>
            <template #overlay>
              <a-menu>
                <a-menu-item @click="openHeaderSettings">个人设置</a-menu-item>
                <a-menu-item @click="router.push('/oa/staff')">账号与赋权</a-menu-item>
                <a-menu-item @click="logout">退出</a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
      </a-layout-header>

      <a-layout-content class="app-content">
        <router-view />
      </a-layout-content>
    </a-layout>
  </a-layout>

  <a-drawer
    v-model:open="headerSettingsOpen"
    title="个人与系统设置"
    width="420"
    placement="right"
  >
    <a-form layout="vertical">
      <a-form-item label="头像">
        <a-space>
          <a-avatar :size="56" :src="headerSettings.avatarUrl || undefined">
            {{ (headerSettings.realName || '管').slice(0, 1) }}
          </a-avatar>
          <a-upload :show-upload-list="false" :before-upload="beforeAvatarUpload">
            <a-button>上传头像</a-button>
          </a-upload>
        </a-space>
      </a-form-item>
      <a-form-item label="真实姓名">
        <a-input v-model:value="headerSettings.realName" placeholder="请输入真实姓名" />
      </a-form-item>
      <a-form-item label="绑定手机号">
        <a-input v-model:value="headerSettings.phone" placeholder="请输入手机号" />
      </a-form-item>
      <a-form-item label="系统主色">
        <a-select v-model:value="headerSettings.themeColor" :options="themeColorOptions" @change="applyAppearance" />
      </a-form-item>
      <a-form-item label="字体大小">
        <a-slider v-model:value="headerSettings.fontScale" :min="90" :max="115" :step="5" @change="applyAppearance" />
      </a-form-item>
      <a-form-item label="快捷通知栏">
        <a-switch v-model:checked="headerSettings.quickNotifyEnabled" checked-children="开启" un-checked-children="关闭" />
      </a-form-item>
      <a-divider orientation="left">账户安全</a-divider>
      <a-form-item label="新密码">
        <a-input-password v-model:value="passwordForm.password" placeholder="请输入新密码（至少6位）" />
      </a-form-item>
      <a-form-item label="确认密码">
        <a-input-password v-model:value="passwordForm.confirmPassword" placeholder="请再次输入新密码" />
      </a-form-item>
      <a-space>
        <a-button type="primary" :loading="savingHeaderSettings" @click="saveHeaderSettings">保存设置</a-button>
        <a-button @click="resetHeaderSettings">恢复默认</a-button>
      </a-space>
      <div class="settings-tip">
        院长赋权注册账号：请前往“账号与赋权”页面进行员工账号开通、角色分配与权限管理。
      </div>
    </a-form>
  </a-drawer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import type { UploadProps } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { getMenuTree } from './menu'
import { updateStaff } from '../api/staff'

const collapsed = ref(false)
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const openKeys = ref<string[]>([])
const headerSettingsOpen = ref(false)
const savingHeaderSettings = ref(false)
const headerSettings = reactive({
  avatarUrl: '',
  realName: '',
  phone: '',
  themeColor: '#1b66d6',
  fontScale: 100,
  quickNotifyEnabled: true
})
const passwordForm = reactive({
  password: '',
  confirmPassword: ''
})
const themeColorOptions = [
  { label: '海蓝', value: '#1b66d6' },
  { label: '翠绿', value: '#22a06b' },
  { label: '暖橙', value: '#f59e0b' },
  { label: '典雅紫', value: '#7c3aed' },
  { label: '深青', value: '#0f766e' }
]
const quickNotifyItems = computed(() => {
  if (!headerSettings.quickNotifyEnabled) return []
  return [
    { title: '我的待办', route: '/oa/todo' },
    { title: '待我审批', route: '/oa/approval?scope=PENDING_REVIEW' },
    { title: '协同日历', route: '/oa/work-execution/calendar' },
    { title: '会员生日', route: '/oa/life/birthday' }
  ]
})

const filteredMenu = computed(() => {
  const roles = userStore.roles || []
  return getMenuTree(roles)
})

const menuItems = computed(() => {
  const map = (items: any[]): any[] =>
    items.map((item) => ({
      key: item.path || item.key,
      label: item.label,
      children: item.children ? map(item.children) : undefined
    }))
  return map(filteredMenu.value)
})

const selectedKeys = computed(() => [route.path])

const currentTitle = computed(() => {
  const title = route.meta?.title as string | undefined
  return title || ''
})

const breadcrumbs = computed(() => {
  const titles = route.matched
    .map((r) => r.meta?.title as string | undefined)
    .filter((t) => t && t.length > 0) as string[]
  return ['首页', ...titles]
})

const todayLabel = computed(() => {
  const now = new Date()
  return now.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit', weekday: 'short' })
})

watch(
  () => route.path,
  (path) => {
    const trail = findMenuTrail(path)
    if (trail.length > 1) {
      openKeys.value = trail.slice(0, trail.length - 1).map((t) => t.key)
    } else {
      openKeys.value = []
    }
  },
  { immediate: true }
)

function onOpenChange(keys: string[]) {
  const previousKeys = [...openKeys.value]
  openKeys.value = keys
  const addedKey = keys.find((key) => !previousKeys.includes(key))
  if (!addedKey) {
    return
  }
  const isTopLevel = filteredMenu.value.some((item) => item.key === addedKey)
  if (!isTopLevel) {
    return
  }
  const targetPath = resolveMenuPathByKey(addedKey, filteredMenu.value)
  if (targetPath && route.path !== targetPath) {
    router.push(targetPath)
  }
}

function onMenuClick(info: any) {
  if (typeof info.key === 'string' && info.key.length > 0) {
    const targetPath = resolveMenuPathByKey(info.key, filteredMenu.value)
    router.push(targetPath || info.key)
  }
}

function findMenuTrail(path: string) {
  const walk = (items: any[], trail: any[] = []): any[] => {
    for (const item of items) {
      if (item.path === path) return [...trail, item]
      if (item.children) {
        const found = walk(item.children, [...trail, item])
        if (found.length) return found
      }
    }
    return []
  }
  return walk(filteredMenu.value)
}

function resolveMenuPathByKey(key: string, items: any[]): string | null {
  for (const item of items || []) {
    if (item.key === key) {
      return item.path || firstLeafPath(item.children || [])
    }
    if (item.children && item.children.length > 0) {
      const found = resolveMenuPathByKey(key, item.children)
      if (found) return found
    }
  }
  return null
}

function firstLeafPath(items: any[]): string | null {
  for (const item of items || []) {
    if (item.path) return item.path
    if (item.children && item.children.length > 0) {
      const found = firstLeafPath(item.children)
      if (found) return found
    }
  }
  return null
}

function logout() {
  userStore.clear()
  router.push('/login')
}

function headerSettingsStorageKey() {
  return `portal_header_settings_v1_${String(userStore.staffInfo?.id || 'default')}`
}

function applyAppearance() {
  document.documentElement.style.setProperty('--primary', headerSettings.themeColor || '#1b66d6')
  document.documentElement.style.fontSize = `${Number(headerSettings.fontScale || 100)}%`
}

function loadHeaderSettings() {
  headerSettings.realName = userStore.staffInfo?.realName || ''
  headerSettings.phone = userStore.staffInfo?.phone || ''
  try {
    const raw = localStorage.getItem(headerSettingsStorageKey())
    if (!raw) {
      applyAppearance()
      return
    }
    const parsed = JSON.parse(raw)
    headerSettings.avatarUrl = String(parsed?.avatarUrl || '')
    headerSettings.themeColor = String(parsed?.themeColor || '#1b66d6')
    headerSettings.fontScale = Number(parsed?.fontScale || 100)
    headerSettings.quickNotifyEnabled = parsed?.quickNotifyEnabled !== false
    if (parsed?.realName) headerSettings.realName = String(parsed.realName)
    if (parsed?.phone) headerSettings.phone = String(parsed.phone)
    applyAppearance()
  } catch {
    applyAppearance()
  }
}

function persistHeaderSettings() {
  const payload = {
    avatarUrl: headerSettings.avatarUrl || '',
    realName: headerSettings.realName || '',
    phone: headerSettings.phone || '',
    themeColor: headerSettings.themeColor || '#1b66d6',
    fontScale: Number(headerSettings.fontScale || 100),
    quickNotifyEnabled: headerSettings.quickNotifyEnabled !== false
  }
  localStorage.setItem(headerSettingsStorageKey(), JSON.stringify(payload))
}

function openHeaderSettings() {
  headerSettings.realName = userStore.staffInfo?.realName || headerSettings.realName
  headerSettings.phone = userStore.staffInfo?.phone || headerSettings.phone
  headerSettingsOpen.value = true
}

const beforeAvatarUpload: UploadProps['beforeUpload'] = async (file) => {
  const reader = new FileReader()
  reader.onload = () => {
    headerSettings.avatarUrl = String(reader.result || '')
    persistHeaderSettings()
  }
  reader.readAsDataURL(file as File)
  return false
}

async function saveHeaderSettings() {
  const realName = String(headerSettings.realName || '').trim()
  const phone = String(headerSettings.phone || '').trim()
  if (!realName) {
    message.warning('请填写真实姓名')
    return
  }
  if (phone && !/^1\d{10}$/.test(phone)) {
    message.warning('手机号格式不正确')
    return
  }
  if (passwordForm.password || passwordForm.confirmPassword) {
    if (String(passwordForm.password || '').length < 6) {
      message.warning('新密码至少 6 位')
      return
    }
    if (passwordForm.password !== passwordForm.confirmPassword) {
      message.warning('两次输入密码不一致')
      return
    }
  }
  savingHeaderSettings.value = true
  try {
    if (userStore.staffInfo?.id) {
      const updatePayload: Record<string, any> = { realName, phone: phone || undefined }
      if (passwordForm.password) updatePayload.password = passwordForm.password
      await updateStaff(Number(userStore.staffInfo.id), updatePayload)
      userStore.staffInfo = {
        ...userStore.staffInfo,
        realName,
        phone
      }
    } else {
      userStore.staffInfo = {
        ...(userStore.staffInfo as any || {}),
        realName,
        phone
      }
    }
    applyAppearance()
    persistHeaderSettings()
    passwordForm.password = ''
    passwordForm.confirmPassword = ''
    message.success('设置已保存')
    headerSettingsOpen.value = false
  } catch (error: any) {
    message.error(error?.message || '保存失败，请检查权限')
  } finally {
    savingHeaderSettings.value = false
  }
}

function resetHeaderSettings() {
  headerSettings.avatarUrl = ''
  headerSettings.realName = userStore.staffInfo?.realName || ''
  headerSettings.phone = userStore.staffInfo?.phone || ''
  headerSettings.themeColor = '#1b66d6'
  headerSettings.fontScale = 100
  headerSettings.quickNotifyEnabled = true
  passwordForm.password = ''
  passwordForm.confirmPassword = ''
  applyAppearance()
  persistHeaderSettings()
  message.success('已恢复默认外观')
}

onMounted(() => {
  loadHeaderSettings()
})
</script>

<style scoped>
.app-layout {
  min-height: 100vh;
  background: var(--bg);
}

.app-sider {
  background: linear-gradient(180deg, #08316d 0%, #0e4a99 100%);
  border-right: 1px solid rgba(255, 255, 255, 0.08);
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 16px;
  color: #f8fafc;
}

.logo {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: #ffffff;
  color: #0b2f6b;
  font-weight: 700;
  display: grid;
  place-items: center;
}

.title {
  font-weight: 700;
  font-size: 16px;
}

.subtitle {
  font-size: 12px;
  color: rgba(248, 250, 252, 0.65);
}

.side-menu {
  background: transparent !important;
  border-inline-end: none !important;
}

.side-menu .ant-menu-item,
.side-menu .ant-menu-submenu-title {
  border-radius: 10px;
  margin: 6px 12px;
}

.side-menu .ant-menu-item-selected {
  background: rgba(255, 255, 255, 0.18) !important;
}

.app-main {
  background: var(--bg);
}

.app-header {
  position: sticky;
  top: 0;
  z-index: 10;
  background: linear-gradient(120deg, rgba(255, 255, 255, 0.92) 0%, rgba(244, 249, 255, 0.92) 100%);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--border);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  height: 64px;
  box-shadow: var(--shadow-sm);
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.page-title {
  font-weight: 600;
  font-size: 16px;
  color: var(--ink);
}

.breadcrumb {
  font-size: 12px;
  color: var(--muted);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.today-label {
  padding: 4px 10px;
  font-size: 12px;
  border-radius: 999px;
  color: #2455a8;
  background: rgba(39, 111, 229, 0.12);
}

.system-name {
  color: var(--muted);
  font-size: 12px;
}

.user-link {
  color: var(--ink);
}

.user-entry {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.system-status {
  font-size: 12px;
}

.settings-tip {
  margin-top: 12px;
  color: var(--muted);
  font-size: 12px;
}

.app-content {
  padding: 24px;
  min-height: calc(100vh - 64px);
}

@media (max-width: 992px) {
  .app-header {
    padding: 0 14px;
  }

  .today-label,
  .system-name {
    display: none;
  }

  .app-content {
    padding: 14px;
  }
}
</style>
