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
}>()

defineEmits<{
  (e: 'open-change', keys: string[]): void
  (e: 'menu-click', info: any): void
  (e: 'mouseleave'): void
}>()
</script>

<style scoped>
.primary-sider {
  position: sticky;
  top: 0;
  height: 100vh;
  overflow: hidden;
  border-inline-end: 1px solid rgba(201, 216, 227, 0.9);
  background:
    linear-gradient(180deg, rgba(250, 253, 255, 0.98) 0%, rgba(243, 249, 252, 0.98) 100%);
  box-shadow: 10px 0 28px rgba(18, 49, 77, 0.05);
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
  border-bottom: 1px solid rgba(220, 231, 238, 0.86);
}

.primary-sider__logo {
  display: grid;
  place-items: center;
  width: 46px;
  height: 46px;
  border-radius: 16px;
  background: linear-gradient(135deg, rgba(19, 108, 181, 0.14), rgba(30, 138, 143, 0.14));
  border: 1px solid rgba(190, 214, 228, 0.9);
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
  color: #7f96aa;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.12em;
}

.primary-sider__menu :deep(.ant-menu-item-group-title)::before {
  content: '';
  position: absolute;
  left: 0;
  top: 22px;
  width: 8px;
  height: 2px;
  border-radius: 999px;
  background: rgba(29, 140, 180, 0.28);
}

.primary-sider__menu :deep(.ant-menu-item),
.primary-sider__menu :deep(.ant-menu-submenu-title) {
  position: relative;
  height: 44px;
  line-height: 44px;
  margin: 4px 6px;
  border-radius: 14px;
  color: #5c768c;
  font-weight: 600;
}

.primary-sider__menu :deep(.ant-menu-item:hover),
.primary-sider__menu :deep(.ant-menu-submenu-title:hover) {
  color: var(--ink);
  background: rgba(19, 108, 181, 0.08);
}

.primary-sider__menu :deep(.ant-menu-item-selected)::before,
.primary-sider__menu :deep(.ant-menu-submenu-selected > .ant-menu-submenu-title)::before,
.primary-sider__menu :deep(.ant-menu-submenu-open > .ant-menu-submenu-title)::before {
  content: '';
  position: absolute;
  left: 10px;
  top: 10px;
  bottom: 10px;
  width: 3px;
  border-radius: 999px;
  background: linear-gradient(180deg, #1d8cb4, #45b59f);
}

.primary-sider__menu :deep(.ant-menu-item-selected),
.primary-sider__menu :deep(.ant-menu-submenu-selected > .ant-menu-submenu-title),
.primary-sider__menu :deep(.ant-menu-submenu-open > .ant-menu-submenu-title) {
  color: var(--primary-strong);
  background: linear-gradient(90deg, rgba(19, 108, 181, 0.14), rgba(30, 138, 143, 0.08));
  box-shadow: inset 0 0 0 1px rgba(19, 108, 181, 0.08);
}

.primary-sider__menu :deep(.ant-menu-sub.ant-menu-inline) {
  background: rgba(246, 250, 252, 0.82);
  margin: 2px 6px 8px;
  padding: 6px 4px 8px;
  border-radius: 18px;
  box-shadow: inset 0 0 0 1px rgba(220, 231, 238, 0.72);
}

.primary-sider__menu :deep(.ant-menu-sub.ant-menu-inline .ant-menu-item) {
  height: 40px;
  line-height: 40px;
  margin: 3px 4px;
  padding-inline-start: 18px !important;
  border-radius: 12px;
  color: #70889c;
  font-size: 13px;
  font-weight: 500;
}

.primary-sider__menu :deep(.ant-menu-sub.ant-menu-inline .ant-menu-item-selected) {
  color: var(--ink);
  background: rgba(255, 255, 255, 0.98);
  box-shadow:
    inset 0 0 0 1px rgba(19, 108, 181, 0.1),
    0 6px 14px rgba(21, 63, 97, 0.06);
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

@media (max-width: 992px) {
  .primary-sider {
    position: fixed;
    z-index: 40;
  }
}
</style>
