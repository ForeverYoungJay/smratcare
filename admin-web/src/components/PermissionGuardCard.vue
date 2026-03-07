<template>
  <PermissionDeniedTooltip :can-access="canAccess" :required-roles="requiredRoles">
    <a-card
      :size="size"
      :bordered="bordered"
      :class="[cardClass, 'permission-card', { 'permission-card-disabled': !canAccess }]"
      @click="handleClick"
    >
      <slot />
    </a-card>
  </PermissionDeniedTooltip>
</template>

<script setup lang="ts">
import type { PropType } from 'vue'
import PermissionDeniedTooltip from './PermissionDeniedTooltip.vue'

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
