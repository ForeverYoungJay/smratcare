<template>
  <div
    v-if="showPeekSensor"
    class="app-shell__peek-sensor"
    @mouseenter="$emit('peek-open')"
  ></div>
  <a-layout class="app-shell">
    <slot name="sider" />
    <a-layout class="app-shell__main">
      <slot name="header" />
      <slot name="utility" />
      <slot name="tabs" />
      <a-layout-content class="app-shell__content">
        <slot />
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup lang="ts">
defineProps<{
  showPeekSensor?: boolean
}>()

defineEmits<{
  (e: 'peek-open'): void
}>()
</script>

<style scoped>
.app-shell {
  min-height: 100vh;
}

.app-shell__peek-sensor {
  position: fixed;
  left: 0;
  top: 0;
  z-index: 30;
  width: 18px;
  height: 100vh;
}

.app-shell__main {
  position: relative;
  min-width: 0;
}

.app-shell__content {
  padding: 20px 24px 28px;
  background: transparent;
}

@media (max-width: 992px) {
  .app-shell__content {
    padding: 14px;
  }
}
</style>

