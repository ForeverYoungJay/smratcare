<template>
  <a-card class="card-elevated quick-nav" :bordered="false">
    <a-space wrap class="quick-nav-main">
      <a-button size="small" @click="router.push(workbenchPath)">返回工作台</a-button>
      <a-button size="small" @click="goParent">返回上级目录</a-button>
      <slot />
    </a-space>
    <a-space wrap class="quick-nav-sections">
      <a-tag
        v-for="item in sectionLinks"
        :key="item.path"
        class="section-tag"
        :color="isActiveSection(item.path) ? 'blue' : 'default'"
        @click="router.push(item.path)"
      >
        {{ item.label }}
      </a-tag>
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
const sectionLinks = computed(() => [
  { label: '线索', path: '/marketing/leads/all' },
  { label: '跟进', path: '/marketing/followup/records' },
  { label: '漏斗', path: '/marketing/funnel/consultation' },
  { label: '预定与床态', path: '/marketing/reservation/records' },
  { label: '合同', path: '/marketing/contracts/pending' },
  { label: '回访', path: '/marketing/callback/checkin' },
  { label: '报表', path: '/marketing/reports/conversion' }
])

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

function isActiveSection(path: string) {
  const current = String(route.path || '')
  if (!path) return false
  const normalized = path.replace(/\/+$/, '')
  return current.startsWith(normalized)
}
</script>

<style scoped>
.quick-nav {
  margin-bottom: 12px;
}

.quick-nav-main {
  margin-bottom: 6px;
}

.quick-nav-sections {
  margin-top: 2px;
}

.section-tag {
  cursor: pointer;
  user-select: none;
}
</style>
