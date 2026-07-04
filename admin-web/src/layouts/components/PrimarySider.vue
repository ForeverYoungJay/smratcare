<template>
  <a-layout-sider
    :collapsed="collapsed"
    collapsible
    :trigger="null"
    :width="264"
    :collapsed-width="mobile ? 0 : 84"
    class="primary-sider"
    :class="{ 'is-peek-open': isPeekOpen, 'is-manual-collapsed': manualCollapsed }"
    @mouseleave="$emit('mouseleave')"
  >
    <div class="primary-sider__brand">
      <div class="primary-sider__logo">
        <img :src="brandLogoUrl" alt="龟峰颐养中心logo" class="primary-sider__logo-image" />
      </div>
      <div v-if="!collapsed" class="primary-sider__brand-copy">
        <strong>龟峰颐养中心</strong>
        <span>运营管理平台</span>
      </div>
    </div>

    <a-menu
      theme="light"
      mode="inline"
      :selectedKeys="selectedKeys"
      :openKeys="openKeys"
      :items="menuItems"
      class="primary-sider__menu"
      @openChange="$emit('open-change', $event)"
      @click="$emit('menu-click', $event)"
    />

    <div v-if="canToggleScope && !collapsed" class="primary-sider__scope">
      <a-button size="small" block @click="$emit('toggle-menu-scope')">
        {{ showAllMenu ? '仅看本岗位常用' : '显示全部功能' }}
      </a-button>
    </div>
  </a-layout-sider>
</template>

<script setup lang="ts">
import type { ItemType } from 'ant-design-vue'
import brandLogoUrl from '../../assets/guifeng-logo.png'

defineProps<{
  collapsed: boolean
  isPeekOpen?: boolean
  manualCollapsed?: boolean
  menuItems: ItemType[]
  mobile?: boolean
  openKeys: string[]
  selectedKeys: string[]
  canToggleScope?: boolean
  showAllMenu?: boolean
}>()

defineEmits<{
  (e: 'open-change', keys: string[]): void
  (e: 'menu-click', info: any): void
  (e: 'mouseleave'): void
  (e: 'toggle-menu-scope'): void
}>()
</script>

<style scoped>
.primary-sider__scope {
  padding: 8px 16px 14px;
}

.primary-sider {
  position: sticky;
  top: 0;
  height: 100vh;
  overflow: hidden;
  border-inline-end: 1px solid var(--border-soft);
  background: #fcfcfa;
  box-shadow: 1px 0 2px rgba(34, 51, 46, 0.04);
}

.primary-sider :deep(.ant-layout-sider-children) {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 18px 12px 16px;
}

.primary-sider__brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px 12px;
  border-bottom: 1px solid var(--border-soft);
}

.primary-sider__logo {
  display: grid;
  place-items: center;
  width: 46px;
  height: 46px;
  border-radius: 12px;
  background: var(--primary-soft);
  border: 1px solid rgba(var(--primary-rgb), 0.16);
  box-shadow: var(--shadow-xs);
}

.primary-sider__logo-image {
  width: 28px;
  height: 28px;
  object-fit: contain;
}

.primary-sider__brand-copy {
  min-width: 0;
  display: grid;
  gap: 2px;
}

.primary-sider__brand-copy strong {
  color: var(--ink);
  font-size: 17px;
  letter-spacing: 0.01em;
}

.primary-sider__brand-copy span {
  color: var(--muted);
  font-size: 12px;
}

.primary-sider__menu {
  flex: 1;
  min-height: 0;
  background: transparent;
  border-inline-end: none;
  overflow-y: auto;
  padding: 4px 0 8px;
}

.primary-sider__menu :deep(.ant-menu-item-group-title) {
  position: relative;
  padding: 18px 12px 8px 14px;
  color: var(--muted);
  font-size: 11.5px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.primary-sider__menu :deep(.ant-menu-item-group-title)::before {
  content: '';
  position: absolute;
  left: 0;
  top: 22px;
  width: 8px;
  height: 2px;
  border-radius: 999px;
  background: rgba(var(--primary-rgb), 0.35);
}

.primary-sider__menu :deep(.ant-menu-item),
.primary-sider__menu :deep(.ant-menu-submenu-title) {
  position: relative;
  height: 44px;
  line-height: 44px;
  margin: 4px 6px;
  border-radius: 10px;
  color: var(--muted);
  font-weight: 600;
}

.primary-sider__menu :deep(.ant-menu-item:hover),
.primary-sider__menu :deep(.ant-menu-submenu-title:hover) {
  color: var(--ink);
  background: rgba(var(--primary-rgb), 0.07);
}

.primary-sider__menu :deep(.ant-menu-item-selected)::before,
.primary-sider__menu :deep(.ant-menu-submenu-selected > .ant-menu-submenu-title)::before,
.primary-sider__menu :deep(.ant-menu-submenu-open > .ant-menu-submenu-title)::before {
  content: '';
  position: absolute;
  left: 6px;
  top: 10px;
  bottom: 10px;
  width: 3px;
  border-radius: 999px;
  background: var(--primary);
}

.primary-sider__menu :deep(.ant-menu-item-selected),
.primary-sider__menu :deep(.ant-menu-submenu-selected > .ant-menu-submenu-title),
.primary-sider__menu :deep(.ant-menu-submenu-open > .ant-menu-submenu-title) {
  color: var(--primary-strong);
  background: var(--primary-soft);
}

.primary-sider__menu :deep(.ant-menu-sub.ant-menu-inline) {
  background: var(--surface-3);
  margin: 2px 6px 8px;
  padding: 6px 4px 8px;
  border-radius: 12px;
  box-shadow: inset 0 0 0 1px var(--border-soft);
}

.primary-sider__menu :deep(.ant-menu-sub.ant-menu-inline .ant-menu-item) {
  height: 40px;
  line-height: 40px;
  margin: 3px 4px;
  padding-inline-start: 18px !important;
  border-radius: 10px;
  color: var(--muted);
  font-size: 13px;
  font-weight: 500;
}

.primary-sider__menu :deep(.ant-menu-sub.ant-menu-inline .ant-menu-item-selected) {
  color: var(--primary-strong);
  background: #ffffff;
  box-shadow:
    inset 0 0 0 1px rgba(var(--primary-rgb), 0.14),
    var(--shadow-xs);
}

.primary-sider__menu :deep(.ant-menu-item .ant-menu-title-content),
.primary-sider__menu :deep(.ant-menu-submenu-title .ant-menu-title-content) {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.primary-sider__menu :deep(.ant-menu-item .anticon),
.primary-sider__menu :deep(.ant-menu-submenu-title .anticon) {
  font-size: 17px;
}

/* 收起态：仅保留图标与分组分隔线，去掉残留文字 */
.primary-sider.ant-layout-sider-collapsed :deep(.ant-layout-sider-children) {
  padding-inline: 8px;
}

.primary-sider.ant-layout-sider-collapsed .primary-sider__brand {
  justify-content: center;
  padding-inline: 0;
}

.primary-sider.ant-layout-sider-collapsed :deep(.ant-menu-item-group-title) {
  padding: 14px 0 6px;
  font-size: 0;
  line-height: 0;
}

.primary-sider.ant-layout-sider-collapsed :deep(.ant-menu-item-group-title)::before {
  left: 50%;
  top: auto;
  bottom: 4px;
  width: 16px;
  transform: translateX(-50%);
}

.primary-sider.ant-layout-sider-collapsed :deep(.ant-menu-item),
.primary-sider.ant-layout-sider-collapsed :deep(.ant-menu-submenu-title) {
  margin: 4px auto;
}

@media (max-width: 992px) {
  .primary-sider {
    position: fixed;
    z-index: 40;
  }
}
</style>
