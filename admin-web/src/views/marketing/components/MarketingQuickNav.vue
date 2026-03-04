<template>
  <a-card class="card-elevated quick-nav" :bordered="false">
    <a-space wrap>
      <a-button size="small" @click="router.push(workbenchPath)">返回工作台</a-button>
      <a-button size="small" @click="goParent">返回上级目录</a-button>
      <slot />
    </a-space>
  </a-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const props = withDefaults(defineProps<{
  parentPath?: string
  workbenchPath?: string
}>(), {
  parentPath: '',
  workbenchPath: '/marketing/workbench'
})

const route = useRoute()
const router = useRouter()
const workbenchPath = computed(() => props.workbenchPath || '/marketing/workbench')

function goParent() {
  if (props.parentPath) {
    router.push(props.parentPath)
    return
  }
  const normalized = route.path.replace(/\/+$/, '')
  const segments = normalized.split('/').filter(Boolean)
  if (segments.length <= 2) {
    router.push(workbenchPath.value)
    return
  }
  const parentPath = `/${segments.slice(0, -1).join('/')}`
  router.push(parentPath)
}
</script>

<style scoped>
.quick-nav {
  margin-bottom: 12px;
}
</style>
