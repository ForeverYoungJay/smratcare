<template>
  <PageContainer class="rm-page" :title="title" :sub-title="subTitle">
    <section class="rm-card">
      <a-tabs :active-key="active?.key" @change="(key: string | number) => go(String(key))">
        <a-tab-pane v-for="tab in visibleTabs" :key="tab.key" :tab="tab.label" />
      </a-tabs>
      <a-alert v-if="active?.note" class="tc-note" :type="active.noteType || 'info'" show-icon :message="active.note" />
      <a-empty v-if="!active" description="当前账号没有可查看的内容" />
      <EmbeddedPage v-else :key="active.key">
        <component :is="activeComponent" v-bind="activeProps" />
      </EmbeddedPage>
    </section>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, defineAsyncComponent, type Component } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import PageContainer from '../../../components/PageContainer.vue'
import EmbeddedPage from '../../marketing/center/EmbeddedPage.vue'
import { useUserStore } from '../../../stores/user'
import { resolveRouteAccess } from '../../../utils/routeAccess'
import { pickTab, type CenterTab } from './centerTabs'
import '../../elder/residentUi.css'

/**
 * 医护的主页面大多是「标签 + 原有业务页」：每个标签指向一个原有（菜单里隐藏的）路由，
 * 组件、props 和权限都取自那个路由，权限只在路由 meta.roles 维护一处。
 */
const props = defineProps<{ title: string; subTitle: string; tabs: CenterTab[] }>()
const route = useRoute()
const router = useRouter()
const user = useUserStore()

const visibleTabs = computed(() => props.tabs.filter((tab) =>
  resolveRouteAccess(router, user.roles, tab.path, user.pagePermissions, user.permissions).canAccess))
const active = computed(() => pickTab(visibleTabs.value, route.query.tab))

const resolved = computed(() => {
  if (!active.value) return null
  const matched = router.resolve(active.value.path).matched
  const record = matched[matched.length - 1]
  return record ? { component: record.components?.default, props: record.props?.default } : null
})
const activeComponent = computed(() => {
  const comp = resolved.value?.component as Component | (() => Promise<Component>) | undefined
  // 路由里是 () => import(...) 的懒加载函数，需要包一层才能当组件用
  return typeof comp === 'function' ? defineAsyncComponent(comp as () => Promise<Component>) : comp
})
const activeProps = computed(() => (typeof resolved.value?.props === 'object' ? resolved.value.props : {}))

// 切标签只保留 tab 和长者筛选，别把上一个页面的筛选带过去
function go(key: string) {
  const keep = route.query.residentId ? { residentId: route.query.residentId } : {}
  router.replace({ path: route.path, query: { ...keep, tab: key } })
}
</script>

<style scoped>
.tc-note { margin-bottom: 12px; }
</style>
