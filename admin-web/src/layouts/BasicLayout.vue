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
          <a-badge status="processing" text="系统运行中" class="system-status" />
          <span class="system-name">智养云·管理后台</span>
          <a-dropdown>
            <a class="user-link">{{ userStore.staffInfo?.realName || '管理员' }}</a>
            <template #overlay>
              <a-menu>
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
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { getMenuTree } from './menu'

const collapsed = ref(false)
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const openKeys = ref<string[]>([])

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
  openKeys.value = keys
}

function onMenuClick(info: any) {
  if (typeof info.key === 'string') {
    router.push(info.key)
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

function logout() {
  userStore.clear()
  router.push('/login')
}
</script>

<style scoped>
.app-layout {
  min-height: 100vh;
  background: var(--bg);
}

.app-sider {
  background: linear-gradient(180deg, #0b2f6b 0%, #0a4aa2 100%);
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
  background: var(--card);
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

.system-name {
  color: var(--muted);
  font-size: 12px;
}

.user-link {
  color: var(--ink);
}

.system-status {
  font-size: 12px;
}

.app-content {
  padding: 24px;
  min-height: calc(100vh - 64px);
}
</style>
