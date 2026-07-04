<template>
  <div class="route-tabs-bar">
    <a-tabs
      :activeKey="activeTabKey"
      type="editable-card"
      hide-add
      size="small"
      class="route-tabs-bar__tabs"
      @change="$emit('change', $event)"
      @edit="onEdit"
    >
      <a-tab-pane
        v-for="tab in routeTabs"
        :key="tab.key"
        :closable="tab.closable"
      >
        <template #tab>
          <span
            class="route-tabs-bar__title"
            :class="{
              'is-dragging': dragKey === tab.key,
              'is-over': overKey === tab.key && dragKey !== tab.key
            }"
            draggable="true"
            @dragstart="$emit('drag-start', tab.key)"
            @dragover.prevent="$emit('drag-over', $event, tab.key)"
            @drop.prevent="$emit('drop', tab.key)"
            @dragend="$emit('drag-end')"
            @contextmenu.prevent="$emit('context-menu', $event, tab.key)"
          >
            {{ tab.title }}
          </span>
        </template>
      </a-tab-pane>
    </a-tabs>

    <a-dropdown trigger="click">
      <a-button size="small" class="route-tabs-bar__tools">标签操作</a-button>
      <template #overlay>
        <slot name="tab-tools-overlay" />
      </template>
    </a-dropdown>

    <div v-if="$slots.extra" class="route-tabs-bar__extra">
      <slot name="extra" />
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  activeTabKey: string
  dragKey?: string
  overKey?: string
  routeTabs: Array<{ key: string; path: string; title: string; closable: boolean }>
}>()

const emit = defineEmits<{
  (e: 'change', key: string): void
  (e: 'context-menu', event: MouseEvent, key: string): void
  (e: 'drag-end'): void
  (e: 'drag-over', event: DragEvent, key: string): void
  (e: 'drag-start', key: string): void
  (e: 'drop', key: string): void
  (e: 'edit', targetKey: string | MouseEvent, action: 'add' | 'remove'): void
}>()

function onEdit(targetKey: string | MouseEvent, action: 'add' | 'remove') {
  emit('edit', targetKey, action)
}
</script>

<style scoped>
.route-tabs-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 20px 4px;
}

.route-tabs-bar__tabs {
  min-width: 0;
  flex: 1;
}

.route-tabs-bar__extra {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: nowrap;
  white-space: nowrap;
}

.route-tabs-bar__tabs :deep(.ant-tabs-nav) {
  margin-bottom: 0;
}

.route-tabs-bar__tabs :deep(.ant-tabs-tab) {
  min-height: 32px;
  margin-top: 2px;
  padding: 0 13px !important;
  border: 1px solid var(--border-soft) !important;
  border-radius: 999px !important;
  background: var(--surface-3) !important;
  transition: all 0.2s ease;
}

.route-tabs-bar__tabs :deep(.ant-tabs-tab + .ant-tabs-tab) {
  margin-left: 6px;
}

.route-tabs-bar__tabs :deep(.ant-tabs-tab:hover) {
  border-color: rgba(var(--primary-rgb), 0.24) !important;
}

.route-tabs-bar__tabs :deep(.ant-tabs-tab-active) {
  border-color: rgba(var(--primary-rgb), 0.28) !important;
  background: var(--primary-soft) !important;
  box-shadow: none;
}

.route-tabs-bar__tabs :deep(.ant-tabs-tab-active .route-tabs-bar__title) {
  color: var(--primary-strong);
}

.route-tabs-bar__tabs :deep(.ant-tabs-ink-bar) {
  display: none;
}

.route-tabs-bar__title {
  display: inline-flex;
  align-items: center;
  min-width: 0;
  font-size: 12.5px;
  font-weight: 600;
}

.route-tabs-bar__tabs :deep(.ant-tabs-tab .ant-tabs-tab-remove) {
  margin-left: 2px;
  padding: 2px;
  font-size: 11px;
}

.route-tabs-bar__title.is-over {
  color: var(--primary-strong);
}

.route-tabs-bar__tools {
  height: 30px;
  border-radius: 999px !important;
  border-color: var(--border);
  background: #ffffff;
  font-size: 12px;
}

@media (max-width: 992px) {
  .route-tabs-bar {
    display: none;
  }
}
</style>
