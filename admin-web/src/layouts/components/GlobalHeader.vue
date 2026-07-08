<template>
  <a-layout-header class="global-header">
    <div class="global-header__left">
      <a-button size="small" class="global-header__toggle" @click="$emit('toggle-sidebar')">
        <component :is="collapsed ? MenuUnfoldOutlined : MenuFoldOutlined" />
      </a-button>
      <div class="global-header__title-block">
        <div class="global-header__page-title">{{ pageTitle }}</div>
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
  border-bottom: 1px solid var(--border-soft);
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(12px);
  box-shadow: var(--shadow-xs);
  /* Reset the tall line-height inherited from ant-layout-header (64~74px),
     which otherwise stretches inline children like the search shortcut into a tall bar. */
  line-height: 1.4;
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
  width: 36px;
  min-width: 36px;
  height: 36px;
  padding: 0;
  border-radius: 10px;
  border-color: var(--border);
  background: #ffffff;
  box-shadow: var(--shadow-xs);
}

.global-header__title-block {
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.global-header__title-block .global-header__page-title {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
  line-height: 1;
  color: var(--ink);
  letter-spacing: 0.01em;
  white-space: nowrap;
}

.global-header__divider {
  width: 1px;
  height: 14px;
  background: var(--border);
}

.global-header__breadcrumb {
  color: var(--muted);
  font-size: 12px;
  line-height: 1;
  min-width: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.global-header__search {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  height: 34px;
  padding: 0 10px 0 11px;
  border: 1px solid var(--border);
  border-radius: 999px;
  background: var(--surface-3);
  box-shadow: none;
  color: var(--ink);
  cursor: pointer;
  min-width: 208px;
  justify-content: space-between;
  transition: border-color 0.2s ease, background 0.2s ease;
}

.global-header__search:hover {
  border-color: rgba(var(--primary-rgb), 0.36);
  background: #ffffff;
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
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: none;
  min-width: 18px;
  height: 18px;
  padding: 0 6px;
  border-radius: 999px;
  background: var(--primary-soft);
  color: var(--primary-strong);
  font-size: 11px;
  font-weight: 700;
  line-height: 1;
}

.global-header__icon-btn {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  border-color: var(--border);
  background: #ffffff;
  box-shadow: var(--shadow-xs);
}

.global-header__icon-btn .anticon {
  font-size: 16px;
}

.global-header__user {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 3px 8px 3px 3px;
  border-radius: 999px;
  color: var(--ink);
  font-size: 13px;
  font-weight: 600;
  transition: background 0.2s ease;
}

.global-header__user:hover {
  background: var(--primary-soft);
  color: var(--primary-strong);
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
