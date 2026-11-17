<template>
  <main class="function-workspace" :aria-label="workspace?.title">
    <header class="function-workspace__header">
      <div><span class="function-workspace__eyebrow">{{ departmentLabel }}</span><h1>{{ workspace?.title }}</h1></div>
      <label v-if="availableTabs.length > 7" class="function-workspace__selector">
        <span>办理内容</span>
        <a-select show-search option-filter-prop="label" :value="activeTab?.key" :options="availableTabs.map(tab => ({ value: tab.key, label: tab.label }))" @change="selectTab" />
      </label>
      <div ref="actionHost" class="function-workspace__actions" />
    </header>
    <a-tabs v-if="availableTabs.length > 1 && availableTabs.length <= 7" :active-key="activeTab?.key" @change="selectTab">
      <a-tab-pane v-for="tab in availableTabs" :key="tab.key" :tab="tab.label" />
    </a-tabs>
    <a-alert v-if="!activeTab" type="warning" show-icon message="当前账号无权查看此项内容" description="请从已授权的办理内容中选择。" />
    <section v-else class="function-workspace__body" :aria-label="activeTab.label">
      <component :is="pageComponents[activeTab.key]" :key="activeTab.key" v-bind="workspacePageProps(activeTab, embeddedRoute)" />
    </section>
  </main>
</template>

<script setup lang="ts">
import { computed, defineAsyncComponent, provide, reactive, ref, h } from 'vue'
import { routeLocationKey, useRoute, useRouter, type RouteLocationNormalizedLoaded } from 'vue-router'
import { useUserStore } from '../stores/user'
import { canUseWorkspaceTab, getFunctionWorkspace, workspacePageProps } from '../workspaces/catalog'

const props = defineProps<{ workspaceId: string }>()
const route = useRoute()
const router = useRouter()
const user = useUserStore()
const workspace = computed(() => getFunctionWorkspace(props.workspaceId))
const availableTabs = computed(() => workspace.value?.tabs.filter((tab) =>
  canUseWorkspaceTab(tab, user.roles || [], user.pagePermissions || [], user.permissions || [])
) || [])
const activeTab = computed(() => {
  const requested = typeof route.query.view === 'string' ? route.query.view : ''
  return requested ? availableTabs.value.find((tab) => tab.key === requested) : availableTabs.value[0]
})
const departmentLabel = computed(() => props.workspaceId.startsWith('finance-') ? '财务部' : props.workspaceId.startsWith('marketing-') ? '市场部' : props.workspaceId.startsWith('hr-') ? '人事部' : '机构管理')
const loadingPage = { render: () => h('div', { role: 'status', style: 'padding:32px' }, '正在加载办理内容…') }
const failedPage = { render: () => h('div', { role: 'alert', style: 'padding:24px' }, [
  h('p', '页面加载失败，请重试。'), h('button', { onClick: () => window.location.reload() }, '重新加载')
]) }
const pageComponents = computed(() => Object.fromEntries((workspace.value?.tabs || []).map((tab) => [tab.key,
  defineAsyncComponent({ loader: tab.component, loadingComponent: loadingPage, errorComponent: failedPage, delay: 120, timeout: 30000, onError(_error, retry, fail, attempts) { if (attempts < 2) retry(); else fail() } })
])))

// Existing reports and scene pages still receive their original route identity.
const embeddedRoute = reactive({
  path: computed(() => activeTab.value?.path || route.path), name: computed(() => activeTab.value?.key || route.name),
  query: computed(() => { const { view, ...query } = route.query; return query }),
  params: computed(() => route.params), hash: computed(() => route.hash), fullPath: computed(() => route.fullPath),
  matched: computed(() => route.matched), meta: computed(() => route.meta), redirectedFrom: computed(() => route.redirectedFrom)
}) as unknown as RouteLocationNormalizedLoaded
provide(routeLocationKey, embeddedRoute)
provide('function-workspace-embedded', true)
provide('business-page-layout', true)
const actionHost = ref<HTMLElement | null>(null)
provide('workspace-actions', actionHost)

function selectTab(key: string | number) {
  if (!availableTabs.value.some((tab) => tab.key === key)) return
  router.replace({ path: workspace.value!.path, query: { ...route.query, view: String(key) }, hash: route.hash })
}
</script>

<style scoped>
.function-workspace { min-width: 0; padding: 12px 16px; border: 1px solid var(--border); border-radius: 12px; background: var(--surface, #fff); }
.function-workspace__actions { margin-left:auto;min-width:0; }
.function-workspace__header { flex-wrap:wrap; display: flex; align-items: center; justify-content: space-between; gap: 16px; margin-bottom: 8px; }
.function-workspace__eyebrow { color: var(--text-secondary, #61756b); font-size: 12px; }
.function-workspace h1 { margin: 2px 0 0; font-size: 23px; line-height: 1.4; color: var(--text-primary, #20382c); }
.function-workspace__selector { display: flex; align-items: center; gap: 10px; font-size: 14px; }
.function-workspace__selector :deep(.ant-select) { width: 220px; }
.function-workspace__body { min-width: 0; }
.function-workspace__body :deep(.page-container) { gap: 12px; }
.function-workspace__body :deep(.report-nav__tabs), .function-workspace__body :deep(.followup-view-nav) { display: none; }
@media (max-width: 700px) {
  .function-workspace { padding: 14px 12px; }
  .function-workspace__actions { margin-left:auto;min-width:0; }
.function-workspace__header { flex-wrap:wrap; align-items: flex-start; flex-direction: column; }
  .function-workspace__selector { width: 100%; }
  .function-workspace__selector :deep(.ant-select) { flex: 1; min-width: 0; }
}
</style>
