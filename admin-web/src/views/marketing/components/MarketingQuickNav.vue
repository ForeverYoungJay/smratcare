<template>
  <a-card class="card-elevated quick-nav" :bordered="false">
    <a-space wrap class="quick-nav-main">
      <a-tag color="blue">{{ currentSectionLabel }}</a-tag>
      <a-button size="small" @click="router.push(workbenchPath)">返回工作台</a-button>
      <a-button size="small" @click="goParent">返回上级目录</a-button>
      <a-dropdown>
        <a-button size="small" type="primary">快捷新建</a-button>
        <template #overlay>
          <a-menu @click="onQuickCreate">
            <a-menu-item key="lead">新增线索</a-menu-item>
            <a-menu-item key="followup">新增跟进</a-menu-item>
            <a-menu-item key="contract">新增合同</a-menu-item>
            <a-menu-item key="reservation">新增预定</a-menu-item>
            <a-menu-item key="plan">新增营销方案</a-menu-item>
          </a-menu>
        </template>
      </a-dropdown>
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
const currentSectionLabel = computed(() => {
  const hit = sectionLinks.value.find((item) => isActiveSection(item.path))
  return hit?.label || '营销管理'
})
const quickCreateRoutes: Record<string, { path: string; query?: Record<string, string> }> = {
  lead: { path: '/marketing/leads/all', query: { quick: '1' } },
  followup: { path: '/marketing/followup/records', query: { quick: '1' } },
  contract: { path: '/marketing/contracts/pending', query: { quick: '1' } },
  reservation: { path: '/marketing/reservation/records', query: { quick: '1' } },
  plan: { path: '/marketing/plan', query: { quick: '1' } }
}

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

function onQuickCreate(event: { key: string | number }) {
  const target = quickCreateRoutes[String(event?.key || '')]
  if (!target) return
  router.push({ path: target.path, query: target.query || {} })
}
</script>

<style scoped>
.quick-nav {
  margin-bottom: 12px;
  border: 1px solid rgba(24, 144, 255, 0.16);
  background: linear-gradient(180deg, rgba(24, 144, 255, 0.06), rgba(24, 144, 255, 0.015));
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
