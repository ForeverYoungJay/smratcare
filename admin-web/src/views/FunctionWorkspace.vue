<template>
  <main class="function-workspace" :aria-label="workspace?.title">
    <header class="function-workspace__header">
      <div><span class="function-workspace__eyebrow">{{ departmentLabel }}</span><h1>{{ workspace?.title }}</h1></div>
      <label v-if="!groups.length && availableTabs.length > 7" class="function-workspace__selector">
        <span>办理内容</span>
        <a-select show-search option-filter-prop="label" :value="activeTab?.key" :options="availableTabs.map(tab => ({ value: tab.key, label: tab.label }))" @change="selectTab" />
      </label>
      <div ref="actionHost" class="function-workspace__actions" />
    </header>
    <template v-if="groups.length">
      <a-tabs v-if="groups.length > 1" :active-key="activeGroup?.label" @change="(label: string | number) => selectTab(groups.find((g) => g.label === label)!.tabs[0].key)">
        <a-tab-pane v-for="group in groups" :key="group.label" :tab="group.label" />
      </a-tabs>
      <div v-if="activeGroup && activeGroup.tabs.length > 1" class="function-workspace__subtabs" role="tablist" :aria-label="activeGroup.label">
        <button v-for="tab in activeGroup.tabs" :key="tab.key" type="button" role="tab" :aria-selected="tab.key === activeTab?.key"
          :class="{ 'is-active': tab.key === activeTab?.key }" @click="selectTab(tab.key)">{{ tab.label }}</button>
      </div>
    </template>
    <a-tabs v-else-if="availableTabs.length > 1 && availableTabs.length <= 7" :active-key="activeTab?.key" @change="selectTab">
      <a-tab-pane v-for="tab in availableTabs" :key="tab.key" :tab="tab.label" />
    </a-tabs>
    <a-alert v-if="activeTab && workspaceNotes[activeTab.path]" class="function-workspace__note" show-icon
      :type="workspaceNotes[activeTab.path].type" :message="workspaceNotes[activeTab.path].message" />
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
import { canUseWorkspaceTab, getFunctionWorkspace, workspaceNotes, workspacePageProps, workspaceTabGroups } from '../workspaces/catalog'

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
const groups = computed(() => workspaceTabGroups(workspace.value, availableTabs.value))
const activeGroup = computed(() => groups.value.find((group) => group.tabs.some((tab) => tab.key === activeTab.value?.key)))
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
  // 留在当前地址切换：工作区被嵌在其他主页面（如行政「生活服务」的标签）里时不跳走
  router.replace({ path: route.path, query: { ...route.query, view: String(key) }, hash: route.hash })
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
.function-workspace__note { margin-bottom: 12px; }
.function-workspace__subtabs { display: flex; flex-wrap: wrap; gap: 8px; margin: -4px 0 12px; }
.function-workspace__subtabs button { padding: 4px 14px; border: 1px solid var(--border, #dfe8e3); border-radius: 999px; background: #fff; color: var(--text-secondary, #4f6258); font-size: 13px; cursor: pointer; }
.function-workspace__subtabs button:hover { border-color: #1f9d74; color: #1f7a5c; }
.function-workspace__subtabs button.is-active { border-color: #1f9d74; background: #e8f6ef; color: #1b6e52; font-weight: 600; }
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
