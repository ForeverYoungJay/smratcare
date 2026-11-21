<template>
  <section class="mk-embedded">
    <div ref="actionHost" class="mk-embedded__actions" />
    <slot />
  </section>
</template>

<script setup lang="ts">
// 把现有的完整办理页（线索表、合同签约、营销方案等）嵌进主页面的标签里：
// 与 FunctionWorkspace 相同的约定——隐藏子页面自己的页头，页头按钮挪到本容器顶部。
import { provide, ref } from 'vue'

const actionHost = ref<HTMLElement | null>(null)
provide('function-workspace-embedded', true)
provide('business-page-layout', true)
provide('workspace-actions', actionHost)
</script>

<style scoped>
.mk-embedded { min-width: 0; }
.mk-embedded__actions { display: flex; justify-content: flex-end; }
.mk-embedded__actions:empty { display: none; }
.mk-embedded :deep(.page-container) { gap: 12px; }
.mk-embedded :deep(.followup-view-nav) { display: none; }
</style>
