<template>
  <a-layout-header class="global-header">
    <div class="global-header__left">
      <a-button size="small" class="global-header__toggle" @click="$emit('toggle-sidebar')">
        <component :is="collapsed ? MenuUnfoldOutlined : MenuFoldOutlined" />
      </a-button>
      <div class="global-header__title-block">
        <h1>{{ pageTitle }}</h1>
        <div class="global-header__divider"></div>
        <a-breadcrumb class="global-header__breadcrumb">
          <a-breadcrumb-item v-for="(item, index) in breadcrumbs" :key="`${item}-${index}`">
            {{ item }}
          </a-breadcrumb-item>
        </a-breadcrumb>
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
          <strong>{{ userName }}</strong>
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
  gap: 12px;
  min-height: 56px;
  padding: 0 16px;
  border-bottom: 1px solid rgba(208, 222, 231, 0.72);
  background:
    linear-gradient(180deg, rgba(249, 252, 254, 0.96), rgba(245, 250, 253, 0.92));
  backdrop-filter: blur(16px);
}

.global-header__left,
.global-header__right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.global-header__left {
  min-width: 0;
  flex: 1;
}

.global-header__toggle {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  min-width: 34px;
  height: 34px;
  padding: 0;
  border-radius: 10px;
  border-color: rgba(207, 220, 230, 0.9);
  background: rgba(255, 255, 255, 0.9);
  box-shadow: var(--shadow-xs);
}

.global-header__title-block {
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.global-header__title-block h1 {
  margin: 0;
  font-size: 16px;
  line-height: 1;
  color: var(--ink);
  letter-spacing: 0.01em;
  white-space: nowrap;
}

.global-header__divider {
  width: 1px;
  height: 12px;
  background: rgba(208, 221, 230, 0.86);
}

.global-header__breadcrumb {
  color: var(--muted);
  font-size: 10px;
  line-height: 1;
  min-width: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.global-header__search {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 32px;
  padding: 0 10px 0 9px;
  border: 1px solid rgba(206, 219, 229, 0.9);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: var(--shadow-xs);
  color: var(--ink);
  cursor: pointer;
  min-width: 180px;
  justify-content: space-between;
}

.global-header__search-label {
  color: var(--muted);
  font-size: 10px;
  text-align: left;
  flex: 1;
}

.global-header__search-icon {
  color: var(--primary-strong);
  font-size: 12px;
}

.global-header__search-shortcut {
  padding: 2px 6px;
  border-radius: 999px;
  background: rgba(19, 108, 181, 0.1);
  color: var(--primary-strong);
  font-size: 10px;
  font-weight: 700;
}

.global-header__icon-btn {
  width: 32px;
  height: 32px;
  border-radius: 10px;
  border-color: rgba(207, 220, 230, 0.9);
  background: rgba(255, 255, 255, 0.92);
  box-shadow: var(--shadow-xs);
}

.global-header__user {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 0 0 0 2px;
  color: var(--ink);
  font-size: 12px;
  font-weight: 600;
}

.global-header__user strong {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

@media (max-width: 1280px) {
  .global-header {
    padding: 0 14px;
  }

  .global-header__right {
    min-width: 0;
    flex: 0 1 auto;
  }

  .global-header__search {
    min-width: 144px;
  }
}

@media (max-width: 768px) {
  .global-header {
    padding: 8px 12px;
    align-items: stretch;
    flex-direction: column;
    min-height: auto;
  }

  .global-header__left,
  .global-header__right {
    justify-content: space-between;
  }

  .global-header__divider {
    display: none;
  }

  .global-header__breadcrumb {
    width: 100%;
    order: 4;
    display: none;
  }

  .global-header__search {
    min-width: 0;
    flex: 1;
  }

  .global-header__right {
    gap: 8px;
    flex-wrap: wrap;
  }
}
</style>
