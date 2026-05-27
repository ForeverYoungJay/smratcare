<template>
  <a-layout-header class="global-header">
    <div class="global-header__left">
      <a-button size="small" class="global-header__toggle" @click="$emit('toggle-sidebar')">
        <component :is="collapsed ? MenuUnfoldOutlined : MenuFoldOutlined" />
        <span>{{ collapsed ? '导航' : '收起' }}</span>
      </a-button>
      <div class="global-header__title-block">
        <div class="global-header__platform">
          <span class="global-header__platform-dot"></span>
          <span>{{ platformName }}</span>
        </div>
        <div class="global-header__page-row">
          <h1>{{ pageTitle }}</h1>
          <div class="global-header__divider"></div>
          <a-breadcrumb class="global-header__breadcrumb">
            <a-breadcrumb-item v-for="(item, index) in breadcrumbs" :key="`${item}-${index}`">
              {{ item }}
            </a-breadcrumb-item>
          </a-breadcrumb>
        </div>
      </div>
    </div>

    <div class="global-header__right">
      <button type="button" class="global-header__search" @click="$emit('open-search')">
        <SearchOutlined class="global-header__search-icon" />
        <span class="global-header__search-label">{{ searchPlaceholder }}</span>
        <span class="global-header__search-shortcut">/</span>
      </button>

      <a-dropdown trigger="click">
        <a-badge :count="notificationCount" size="small">
          <a-button size="small" class="global-header__icon-btn" title="消息通知">
            <BellOutlined />
          </a-button>
        </a-badge>
        <template #overlay>
          <slot name="notification-overlay" />
        </template>
      </a-dropdown>
      <a-dropdown trigger="click">
        <a-badge :count="todoCount" size="small">
          <a-button size="small" class="global-header__icon-btn" title="待办事项">
            <CheckSquareOutlined />
          </a-button>
        </a-badge>
        <template #overlay>
          <slot name="todo-overlay" />
        </template>
      </a-dropdown>

      <a-dropdown>
        <a class="global-header__user">
          <a-avatar :size="32" :src="avatarUrl || undefined">{{ userInitial }}</a-avatar>
          <div class="global-header__user-copy">
            <strong>{{ userName }}</strong>
            <span>{{ todayLabel }}</span>
          </div>
        </a>
        <template #overlay>
          <slot name="user-overlay" />
        </template>
      </a-dropdown>
    </div>
  </a-layout-header>
</template>

<script setup lang="ts">
import { BellOutlined, CheckSquareOutlined, MenuFoldOutlined, MenuUnfoldOutlined, SearchOutlined } from '@ant-design/icons-vue'

defineProps<{
  avatarUrl?: string
  breadcrumbs: string[]
  collapsed: boolean
  notificationCount: number
  pageTitle: string
  platformName: string
  searchPlaceholder: string
  todoCount: number
  todayLabel: string
  userInitial: string
  userName: string
}>()

defineEmits<{
  (e: 'open-search'): void
  (e: 'toggle-sidebar'): void
}>()
</script>

<style scoped>
.global-header {
  position: sticky;
  top: 0;
  z-index: 20;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  height: auto;
  padding: 14px 24px 12px;
  border-bottom: 1px solid rgba(208, 222, 231, 0.72);
  background:
    linear-gradient(180deg, rgba(249, 252, 254, 0.96), rgba(245, 250, 253, 0.92));
  backdrop-filter: blur(16px);
}

.global-header__left,
.global-header__right {
  display: flex;
  align-items: center;
  gap: 14px;
}

.global-header__left {
  min-width: 0;
  flex: 1;
}

.global-header__toggle {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 38px;
  padding: 0 12px;
  border-radius: 14px;
  border-color: rgba(207, 220, 230, 0.9);
  background: rgba(255, 255, 255, 0.9);
  box-shadow: var(--shadow-xs);
}

.global-header__title-block {
  min-width: 0;
  display: grid;
  gap: 7px;
}

.global-header__platform {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: var(--muted-2);
  font-size: 12px;
  font-weight: 700;
}

.global-header__platform-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: linear-gradient(135deg, #1d8cb4, #45b59f);
  box-shadow: 0 0 0 5px rgba(29, 140, 180, 0.08);
}

.global-header__page-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.global-header__page-row h1 {
  margin: 0;
  font-size: 20px;
  line-height: 1.08;
  color: var(--ink);
  letter-spacing: 0.01em;
}

.global-header__divider {
  width: 1px;
  height: 16px;
  background: rgba(208, 221, 230, 0.86);
}

.global-header__breadcrumb {
  color: var(--muted);
  font-size: 12px;
}

.global-header__search {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  height: 40px;
  padding: 0 14px 0 12px;
  border: 1px solid rgba(206, 219, 229, 0.9);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: var(--shadow-xs);
  color: var(--ink);
  cursor: pointer;
  min-width: 280px;
  justify-content: space-between;
}

.global-header__search-label {
  color: var(--muted);
  font-size: 12px;
  text-align: left;
  flex: 1;
}

.global-header__search-icon {
  color: var(--primary-strong);
  font-size: 14px;
}

.global-header__search-shortcut {
  padding: 4px 8px;
  border-radius: 999px;
  background: rgba(19, 108, 181, 0.1);
  color: var(--primary-strong);
  font-size: 12px;
  font-weight: 700;
}

.global-header__icon-btn {
  width: 40px;
  height: 40px;
  border-radius: 14px;
  border-color: rgba(207, 220, 230, 0.9);
  background: rgba(255, 255, 255, 0.92);
  box-shadow: var(--shadow-xs);
}

.global-header__user {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 4px 0 4px 2px;
}

.global-header__user-copy {
  display: grid;
  gap: 1px;
}

.global-header__user-copy strong {
  color: var(--ink);
  font-size: 13px;
  max-width: 180px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.global-header__user-copy span {
  color: var(--muted);
  font-size: 11px;
}

@media (max-width: 1280px) {
  .global-header {
    flex-direction: column;
    align-items: stretch;
  }

  .global-header__right {
    flex-wrap: wrap;
    justify-content: space-between;
  }
}

@media (max-width: 768px) {
  .global-header {
    padding: 14px;
  }

  .global-header__page-row {
    align-items: flex-start;
  }

  .global-header__divider {
    display: none;
  }

  .global-header__search {
    min-width: 0;
    width: 100%;
  }

  .global-header__right {
    gap: 10px;
  }
}
</style>
