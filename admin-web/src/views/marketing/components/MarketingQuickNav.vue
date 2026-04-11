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
    <a-space v-if="collaborationLinks.length" wrap class="quick-nav-collab">
      <span class="collab-label">协同入口</span>
      <a-button
        v-for="item in collaborationLinks"
        :key="item.path"
        size="small"
        @click="router.push(item.path)"
      >
        {{ item.label }}
      </a-button>
    </a-space>
  </a-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../../../stores/user'
import { MARKETING_ROUTE_PATHS, MARKETING_SECTION_LINKS } from '../../../utils/marketingRoutes'
import { resolveRouteAccess } from '../../../utils/routeAccess'

const props = withDefaults(defineProps<{
  parentPath?: string
  workbenchPath?: string
}>(), {
  parentPath: '',
  workbenchPath: MARKETING_ROUTE_PATHS.workbench
})

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const workbenchPath = computed(() => props.workbenchPath || MARKETING_ROUTE_PATHS.workbench)
const sectionLinks = computed(() => MARKETING_SECTION_LINKS)
const currentSectionLabel = computed(() => {
  const hit = sectionLinks.value.find((item) => isActiveSection(item.path))
  return hit?.label || '营销管理'
})
const quickCreateRoutes: Record<string, { path: string; query?: Record<string, string> }> = {
  lead: { path: MARKETING_ROUTE_PATHS.leads, query: { quick: '1' } },
  followup: { path: MARKETING_ROUTE_PATHS.interactions, query: { quick: '1' } },
  contract: { path: MARKETING_ROUTE_PATHS.contracts, query: { quick: '1' } },
  reservation: { path: MARKETING_ROUTE_PATHS.reservation, query: { quick: '1' } },
  plan: { path: MARKETING_ROUTE_PATHS.plan, query: { quick: '1' } }
}
const collaborationLinks = computed(() => ([
  { label: '入住评估', path: '/elder/assessment/ability/admission' },
  { label: '入住办理', path: '/elder/admission-processing' },
  { label: 'OA审批', path: '/oa/approval' },
  { label: '我的待办', path: '/workbench/todo' },
  { label: '人力资源中心', path: '/hr/overview' }
]).filter((item) => canAccess(item.path)))

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
  const currentSection = sectionLinks.value.find((item) => item.path === path)
  const candidates = Array.isArray((currentSection as any)?.activePaths)
    ? (currentSection as any).activePaths
    : [path]
  return candidates.some((item: string) => current.startsWith(String(item || '').replace(/\/+$/, '')))
}

function canAccess(path: string) {
  return resolveRouteAccess(router, userStore.roles || [], path, userStore.pagePermissions || []).canAccess
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

.quick-nav-collab {
  margin-top: 8px;
}

.collab-label {
  color: #64748b;
  font-size: 12px;
}

.section-tag {
  cursor: pointer;
  user-select: none;
}
</style>
