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
        class="side-menu"
      >
        <a-menu-item v-for="item in filteredMenu" :key="item.path" @click="go(item.path)">
          <span>{{ item.label }}</span>
        </a-menu-item>
      </a-menu>
    </a-layout-sider>

    <a-layout class="app-main">
      <a-layout-header class="app-header">
        <div class="header-left">
          <div class="page-title">{{ currentTitle || '工作台' }}</div>
          <a-breadcrumb class="breadcrumb">
            <a-breadcrumb-item>首页</a-breadcrumb-item>
            <a-breadcrumb-item>{{ currentTitle || 'Dashboard' }}</a-breadcrumb-item>
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
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { menuItems } from '../router/menu'

const collapsed = ref(false)
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const filteredMenu = computed(() => {
  if (!userStore.roles || userStore.roles.length === 0) return menuItems
  return menuItems.filter((m) => !m.roles || m.roles.some((r) => userStore.roles.includes(r)))
})

const selectedKeys = computed(() => [route.path])

const currentTitle = computed(() => {
  const item = menuItems.find((m) => m.path === route.path)
  return item?.label || ''
})

function go(path: string) {
  router.push(path)
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
  background: linear-gradient(180deg, #0f172a 0%, #0b3b54 100%);
  border-right: 1px solid rgba(148, 163, 184, 0.2);
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
  background: linear-gradient(135deg, #38bdf8, #22d3ee);
  color: #0f172a;
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

.side-menu .ant-menu-item {
  border-radius: 10px;
  margin: 6px 12px;
}

.side-menu .ant-menu-item-selected {
  background: rgba(56, 189, 248, 0.2) !important;
}

.app-main {
  background: var(--bg);
}

.app-header {
  position: sticky;
  top: 0;
  z-index: 10;
  background: var(--glass);
  border-bottom: 1px solid var(--border);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  height: 64px;
  backdrop-filter: blur(14px);
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
