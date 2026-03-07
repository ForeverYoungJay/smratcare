<template>
  <a-tooltip :title="tooltipText" :disabled="canAccess">
    <slot />
  </a-tooltip>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  canAccess: boolean
  requiredRoles?: string[]
}>()

const tooltipText = computed(() => {
  if (props.canAccess) {
    return ''
  }
  const roles = (props.requiredRoles || []).filter(Boolean)
  if (roles.length) {
    return `当前账号暂无权限，需角色：${roles.join(' / ')}`
  }
  return '当前账号暂无权限'
})
</script>
