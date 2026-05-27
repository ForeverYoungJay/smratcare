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
  padding: 2px 20px 0;
}

.route-tabs-bar__tabs {
  min-width: 0;
  flex: 1;
}

.route-tabs-bar__tabs :deep(.ant-tabs-nav) {
  margin-bottom: 0;
}

.route-tabs-bar__tabs :deep(.ant-tabs-tab) {
  min-height: 30px;
  padding: 0 10px !important;
  border: 1px solid rgba(214, 225, 232, 0.9) !important;
  border-radius: 10px 10px 0 0 !important;
  background: rgba(247, 251, 253, 0.72) !important;
  transition: all 0.2s ease;
}

.route-tabs-bar__tabs :deep(.ant-tabs-tab-active) {
  background: rgba(255, 255, 255, 0.96) !important;
  box-shadow: 0 8px 16px rgba(21, 63, 97, 0.06);
}

.route-tabs-bar__title {
  display: inline-flex;
  align-items: center;
  min-width: 0;
  font-size: 11px;
  font-weight: 600;
}

.route-tabs-bar__title.is-over {
  color: var(--primary-strong);
}

.route-tabs-bar__tools {
  height: 30px;
  border-radius: 10px;
  border-color: rgba(207, 220, 230, 0.9);
  background: rgba(255, 255, 255, 0.9);
}

@media (max-width: 992px) {
  .route-tabs-bar {
    display: none;
  }
}
</style>
