<template>
  <a-tooltip :title="tooltipText" :disabled="canAccess">
    <a-card
      :size="size"
      :bordered="bordered"
      :class="[cardClass, 'permission-card', { 'permission-card-disabled': !canAccess }]"
      @click="handleClick"
    >
      <slot />
    </a-card>
  </a-tooltip>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { PropType } from 'vue'

const props = defineProps({
  canAccess: {
    type: Boolean,
    default: true
  },
  requiredRoles: {
    type: Array as PropType<string[]>,
    default: () => []
  },
  size: {
    type: String as PropType<'small' | 'default'>,
    default: 'default'
  },
  bordered: {
    type: Boolean,
    default: false
  },
  cardClass: {
    type: [String, Array, Object] as PropType<string | string[] | Record<string, boolean>>,
    default: ''
  }
})

const emit = defineEmits<{
  (e: 'click'): void
}>()

const tooltipText = computed(() => {
  if (props.canAccess) {
    return ''
  }
  if (props.requiredRoles.length > 0) {
    return `当前账号暂无权限，需角色：${props.requiredRoles.join(' / ')}`
  }
  return '当前账号暂无权限'
})

function handleClick() {
  if (!props.canAccess) {
    return
  }
  emit('click')
}
</script>

<style scoped>
.permission-card {
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.permission-card:not(.permission-card-disabled) {
  cursor: pointer;
}

.permission-card:not(.permission-card-disabled):hover {
  transform: translateY(-1px);
}

.permission-card-disabled {
  cursor: not-allowed;
  opacity: 0.86;
}
</style>
