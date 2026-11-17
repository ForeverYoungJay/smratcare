<template>
  <section class="department-home" :class="[`department-home--${department.toLowerCase()}`, { 'department-home--embedded': !showHeader, 'department-home--compact': !showCatalog }]">
    <header v-if="showHeader" class="department-home__header">
      <img :src="visualAssets.scenicMountain.url" alt="" aria-hidden="true" class="department-home__scenery" />
      <span class="department-home__scenery-veil" aria-hidden="true"></span>
      <div>
        <span class="department-home__label">{{ config.label }}工作台</span>
        <h2>今天的重点一屏掌握</h2>
        <p>{{ config.summary }}</p>
      </div>
      <div class="department-home__tools">
        <a-input v-model:value="keyword" allow-clear placeholder="搜索本部门功能" aria-label="搜索本部门功能" />
        <a-button v-if="showRefresh" :loading="refreshing" @click="emit('refresh')">刷新数据</a-button>
      </div>
    </header>

    <div v-if="dataState === 'loading'" class="department-home__state"><a-skeleton active :paragraph="{ rows: 2 }" /></div>
    <a-alert v-else-if="dataState === 'error'" type="warning" show-icon :message="dataMessage || '首页数据加载失败，功能入口仍可使用'" />
    <a-alert v-else-if="dataState === 'forbidden'" type="info" show-icon message="当前账号无指标数据权限，功能入口仍按页面权限展示" />
    <a-empty v-else-if="dataState === 'empty' && !visibleMetrics.length" description="当前统计范围暂无数据，可继续使用下方业务功能" />

    <div v-if="visibleMetrics.length" class="department-home__metrics" aria-label="部门重点指标">
      <button v-for="metric in visibleMetrics" :key="metric.key" type="button" :class="`is-${metric.tone || 'normal'}`" @click="go(metric.path)">
        <span>{{ metric.label }}</span>
        <strong>{{ formatMetric(metric) }}</strong>
        <small v-if="metric.helper || showCatalog">{{ metric.helper || '查看明细' }}</small>
        <span class="department-home__metric-arrow" aria-hidden="true">›</span>
      </button>
    </div>

    <div class="department-home__meta">
      <span>{{ scopeLabel || '当前账号可用范围' }}</span>
      <span v-if="periodLabel">{{ periodLabel }}</span>
      <span v-if="updatedAt">更新于 {{ updatedAt }}</span>
      <span v-if="showCatalog">{{ featureCount }} 项可用功能</span>
    </div>

    <slot name="after-metrics" />

    <section v-if="showCatalog" class="department-home__flow" aria-labelledby="department-flow-title">
      <div class="department-home__section-title">
        <div><span>核心流程</span><h3 id="department-flow-title">按顺序处理更清晰</h3></div>
      </div>
      <ol>
        <li v-for="(step, index) in visibleSteps" :key="step.path">
          <button type="button" @click="go(step.path)">
            <span>{{ String(index + 1).padStart(2, '0') }}</span>
            <strong>{{ step.title }}</strong>
            <em aria-hidden="true">→</em>
          </button>
        </li>
      </ol>
    </section>

    <section v-if="showCatalog && recentFeatures.length" class="department-home__recent" aria-label="最近使用">
      <strong>最近使用</strong>
      <button v-for="item in recentFeatures" :key="item.path" type="button" @click="go(item.path)">{{ item.title }}</button>
    </section>

    <section v-if="showCatalog" class="department-home__functions" aria-labelledby="department-functions-title">
      <div class="department-home__section-title">
        <div><span>全部功能</span><h3 id="department-functions-title">按业务分类直达</h3></div>
        <small>仅展示当前账号可进入的页面</small>
      </div>
      <a-empty v-if="!filteredGroups.length" description="没有匹配的可用功能" />
      <div v-else class="department-home__groups">
        <article v-for="group in filteredGroups" :key="group.key">
          <div class="department-home__group-head">
            <strong>{{ group.label }}</strong><span>{{ group.pages.length }}</span>
          </div>
          <div class="department-home__tiles">
            <button v-for="page in group.pages" :key="page.path" type="button" @click="go(page.path)">
              <span><strong>{{ page.label }}</strong><small v-if="page.description">{{ page.description }}</small></span>
              <em aria-hidden="true">→</em>
            </button>
          </div>
        </article>
      </div>
    </section>
  </section>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import type { DepartmentCode } from '../../access/policy'
import { useUserStore } from '../../stores/user'
import { getMenuTree, type MenuItem } from '../../layouts/menu'
import { getPageDescription } from '../../router/pageDescriptions'
import { resolveRouteAccess } from '../../utils/routeAccess'
import { loadRecentVisits } from '../../utils/recentVisits'
import { visualAssets } from '../../config/visualAssets'
import {
  getDepartmentHomeConfig,
  formatDepartmentHomeMetric,
  type DepartmentHomeDataState,
  type DepartmentHomeMetric
} from '../../workbench/departmentHome'

type FeaturePage = { label: string; description?: string; path: string }
type FeatureGroup = { key: string; label: string; pages: FeaturePage[] }

const props = withDefaults(defineProps<{
  department: DepartmentCode
  metrics?: DepartmentHomeMetric[]
  dataState?: DepartmentHomeDataState
  dataMessage?: string
  scopeLabel?: string
  periodLabel?: string
  updatedAt?: string
  refreshing?: boolean
  showRefresh?: boolean
  showHeader?: boolean
  showCatalog?: boolean
}>(), {
  metrics: () => [],
  dataState: 'ready',
  dataMessage: '',
  scopeLabel: '',
  periodLabel: '',
  updatedAt: '',
  refreshing: false,
  showRefresh: false,
  showHeader: true,
  showCatalog: true
})

const emit = defineEmits<{ refresh: [] }>()
const router = useRouter()
const userStore = useUserStore()
const keyword = ref('')
const config = computed(() => getDepartmentHomeConfig(props.department))

const fullMenu = computed(() => getMenuTree(
  userStore.roles || [],
  userStore.pagePermissions || [],
  { focused: false, permissions: userStore.permissions || [] }
))

function pathWithoutQuery(path: string) {
  return path.split('?')[0]
}

function canAccess(path: string) {
  return resolveRouteAccess(
    router,
    userStore.roles || [],
    pathWithoutQuery(path),
    userStore.pagePermissions || [],
    userStore.permissions || []
  ).canAccess
}

function collectLeaves(items: MenuItem[] = []): FeaturePage[] {
  return items.flatMap((item) => {
    const self = item.path && !item.children?.length
      ? [{ label: item.label, description: item.desc, path: item.path }]
      : []
    return [...self, ...collectLeaves(item.children)]
  })
}

function findModule(items: MenuItem[], root: string): MenuItem | undefined {
  for (const item of items) {
    if (item.path === root) return item
    if (item.children?.length && collectLeaves(item.children).some((page) => page.path === root || page.path.startsWith(`${root}/`))) {
      return item
    }
    const nested = item.children?.length ? findModule(item.children, root) : undefined
    if (nested) return nested
  }
}

function uniquePages(pages: FeaturePage[]) {
  const seen = new Set<string>()
  return pages.filter((page) => {
    const path = pathWithoutQuery(page.path)
    if (!path || path === pathWithoutQuery(config.value.homePath) || seen.has(path) || !canAccess(page.path)) return false
    seen.add(path)
    return true
  })
}

const groups = computed<FeatureGroup[]>(() => {
  const result: FeatureGroup[] = []
  const usedPaths = new Set<string>()
  config.value.moduleRoots.forEach((root) => {
    const module = findModule(fullMenu.value, root)
    if (!module) return
    const loose: FeaturePage[] = []
    ;(module.children || []).forEach((child) => {
      if (child.children?.length) {
        const pages = uniquePages(collectLeaves(child.children)).filter((page) => !usedPaths.has(pathWithoutQuery(page.path)))
        pages.forEach((page) => usedPaths.add(pathWithoutQuery(page.path)))
        if (pages.length) result.push({ key: `${root}-${child.key}`, label: child.label, pages })
      } else if (child.path) {
        loose.push({ label: child.label, description: child.desc, path: child.path })
      }
    })
    const pages = uniquePages(loose).filter((page) => !usedPaths.has(pathWithoutQuery(page.path)))
    pages.forEach((page) => usedPaths.add(pathWithoutQuery(page.path)))
    if (pages.length) result.unshift({ key: `${root}-common`, label: '常用业务', pages })
  })

  ;(config.value.extraGroups || []).forEach((group) => {
    const pages = uniquePages(group.paths.map((path) => {
      const resolved = router.resolve(path)
      const routeName = String(resolved.name || '')
      return {
        label: String(resolved.meta?.title || routeName || path),
        description: getPageDescription(routeName),
        path
      }
    })).filter((page) => !usedPaths.has(pathWithoutQuery(page.path)))
    pages.forEach((page) => usedPaths.add(pathWithoutQuery(page.path)))
    if (pages.length) result.push({ key: group.key, label: group.label, pages })
  })
  return result
})

const filteredGroups = computed(() => {
  const query = keyword.value.trim().toLowerCase()
  if (!query) return groups.value
  return groups.value
    .map((group) => ({
      ...group,
      pages: group.pages.filter((page) => `${page.label} ${page.description || ''}`.toLowerCase().includes(query))
    }))
    .filter((group) => group.pages.length)
})

const allPaths = computed(() => new Set(groups.value.flatMap((group) => group.pages.map((page) => pathWithoutQuery(page.path)))))
const featureCount = computed(() => allPaths.value.size)
const visibleMetrics = computed(() => props.metrics.filter((metric) => canAccess(metric.path)))
const visibleSteps = computed(() => config.value.steps.filter((step) => canAccess(step.path)))
const recentScope = computed(() => {
  if (userStore.staffInfo?.id) return `id_${String(userStore.staffInfo.id)}`
  if (userStore.staffInfo?.username) return `u_${String(userStore.staffInfo.username)}`
  return 'default'
})
const recentFeatures = computed(() => loadRecentVisits(recentScope.value)
  .filter((item) => allPaths.value.has(pathWithoutQuery(item.path)))
  .slice(0, 4))

function formatMetric(metric: DepartmentHomeMetric) {
  return formatDepartmentHomeMetric(metric)
}

function go(path: string) {
  if (!canAccess(path)) return
  router.push(path)
}
</script>

<style scoped>
.department-home { --dept-accent: #176d67; --dept-dark: #173b38; --dept-soft: #edf7f5; display: grid; gap: 18px; padding: 22px; border: 1px solid #d6e2df; border-radius: 10px; background: #fbfdfc; box-shadow: 0 10px 30px rgba(24, 55, 51, .06); }
.department-home--embedded { padding: 0; border: 0; background: transparent; box-shadow: none; }
.department-home--medical { --dept-accent: #27618a; --dept-dark: #193b55; --dept-soft: #eef5fa; }
.department-home--finance { --dept-accent: #8a5b18; --dept-dark: #533a17; --dept-soft: #faf5e9; }
.department-home--logistics { --dept-accent: #7a562c; --dept-dark: #4d3821; --dept-soft: #f8f3eb; }
.department-home--hr { --dept-accent: #526b38; --dept-dark: #354526; --dept-soft: #f1f6ec; }
.department-home--marketing { --dept-accent: #9a493e; --dept-dark: #5e302b; --dept-soft: #fbf0ee; }
.department-home__header { position: relative; isolation: isolate; overflow: hidden; display: flex; justify-content: space-between; gap: 24px; align-items: end; min-height: 148px; padding: 24px; border: 1px solid rgba(172, 154, 113, .2); border-radius: 8px; }
.department-home__header > *:not(.department-home__scenery, .department-home__scenery-veil) { position: relative; z-index: 1; }
.department-home__scenery, .department-home__scenery-veil { position: absolute; inset: 0; width: 100%; height: 100%; }
.department-home__scenery { z-index: -2; object-fit: cover; object-position: center 48%; filter: saturate(.72) contrast(.9); }
.department-home__scenery-veil { z-index: -1; background: linear-gradient(90deg, rgba(255, 253, 247, .97), rgba(255, 253, 247, .78) 60%, rgba(255, 253, 247, .38)); }
.department-home__label, .department-home__section-title span { color: var(--dept-accent); font-size: 12px; font-weight: 700; letter-spacing: .08em; }
.department-home h2 { margin: 5px 0 4px; color: var(--dept-dark); font-family: "Noto Serif SC", "Songti SC", serif; font-size: 27px; }
.department-home__header p { margin: 0; color: #66716e; }
.department-home__tools { display: flex; gap: 8px; min-width: 310px; }
.department-home__meta { display: flex; flex-wrap: wrap; gap: 7px; }
.department-home__meta span { padding: 4px 9px; border: 1px solid #d9e3e0; border-radius: 3px; color: #64706c; background: #fff; font-size: 12px; }
.department-home__state { padding: 8px 0; }
.department-home__metrics { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 10px; }
.department-home__metrics button { position: relative; display: grid; gap: 6px; min-height: 124px; padding: 18px 56px 18px 20px; text-align: left; border: 1px solid #e5eae6; border-radius: 12px; background: linear-gradient(135deg, #fff, #fbfaf7); box-shadow: 0 8px 22px rgba(35, 52, 43, .055); cursor: pointer; transition: border-color .15s ease, box-shadow .15s ease; }
.department-home__metrics .department-home__metric-arrow { position: absolute; right: 18px; top: 50%; display: grid; place-items: center; width: 28px; height: 28px; border-radius: 50%; background: #edf6f2; color: #168066; font-size: 25px; line-height: 1; transform: translateY(-50%); }
.department-home__metrics button.is-warning .department-home__metric-arrow { background: #fff0dd; color: #b47712; }
.department-home__metrics button.is-danger .department-home__metric-arrow { background: #fff0ed; color: #cf3d38; }
.department-home__metrics button:hover { border-color: #a7cbbd; box-shadow: 0 12px 28px rgba(35, 52, 43, .1); }
.department-home__metrics button.is-warning strong { color: #b47712; } .department-home__metrics button.is-danger strong { color: #cf3d38; } .department-home__metrics button.is-success strong { color: #1b8061; }
.department-home__metrics span, .department-home__metrics small { color: #69736f; } .department-home__metrics strong { color: var(--dept-dark); font-size: 25px; } .department-home__metrics small { line-height: 1.4; }
.department-home__section-title { display: flex; justify-content: space-between; gap: 16px; align-items: end; margin-bottom: 11px; }
.department-home__section-title h3 { margin: 2px 0 0; color: var(--dept-dark); font-size: 17px; } .department-home__section-title > small { color: #78817e; }
.department-home__flow ol { display: grid; grid-template-columns: repeat(5, 1fr); gap: 8px; margin: 0; padding: 0; list-style: none; counter-reset: flow; }
.department-home__flow button { display: grid; grid-template-columns: auto 1fr auto; gap: 8px; align-items: center; width: 100%; min-height: 62px; padding: 11px; border: 1px solid #d7e2df; border-radius: 5px; background: var(--dept-soft); color: var(--dept-dark); cursor: pointer; }
.department-home__flow button > span { color: var(--dept-accent); font-size: 12px; font-weight: 800; } .department-home__flow em, .department-home__tiles em { color: var(--dept-accent); font-style: normal; }
.department-home__recent { display: flex; flex-wrap: wrap; align-items: center; gap: 8px; padding: 10px 12px; border-left: 3px solid var(--dept-accent); background: var(--dept-soft); }
.department-home__recent > strong { margin-right: 4px; color: var(--dept-dark); }
.department-home__recent button { padding: 5px 9px; border: 1px solid #d5dfdc; border-radius: 3px; background: #fff; color: #52605c; cursor: pointer; }
.department-home__groups { display: grid; gap: 16px; }
.department-home__group-head { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; color: var(--dept-dark); }
.department-home__group-head span { display: inline-grid; place-items: center; min-width: 22px; height: 22px; border-radius: 50%; background: var(--dept-soft); color: var(--dept-accent); font-size: 12px; }
.department-home__tiles { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 8px; }
.department-home__tiles button { display: flex; justify-content: space-between; align-items: center; gap: 10px; min-height: 68px; padding: 11px 13px; text-align: left; border: 1px solid #dce4e2; border-radius: 5px; background: #fff; cursor: pointer; transition: border-color .15s ease, transform .15s ease, box-shadow .15s ease; }
.department-home__tiles button:hover { transform: translateY(-2px); border-color: var(--dept-accent); box-shadow: 0 7px 18px rgba(30, 65, 60, .08); }
.department-home__tiles button strong, .department-home__tiles button small { display: block; } .department-home__tiles button strong { color: #33413d; } .department-home__tiles button small { margin-top: 3px; overflow: hidden; color: #79827f; font-size: 12px; text-overflow: ellipsis; white-space: nowrap; }
.department-home button:focus-visible { outline: 3px solid color-mix(in srgb, var(--dept-accent) 30%, transparent); outline-offset: 2px; }
@media (max-width: 1100px) { .department-home__metrics, .department-home__tiles { grid-template-columns: repeat(2, 1fr); } .department-home__flow ol { grid-template-columns: 1fr; } .department-home__flow button { grid-template-columns: 40px 1fr auto; } }
@media (max-width: 720px) { .department-home { padding: 15px; } .department-home__header { align-items: stretch; flex-direction: column; } .department-home__tools { min-width: 0; } .department-home__metrics, .department-home__tiles { grid-template-columns: 1fr; } .department-home h2 { font-size: 23px; } }

@media(max-width:700px) {
  .department-home--compact .department-home__metrics { grid-template-columns:repeat(2, minmax(0, 1fr)); gap:8px; }
  .department-home--compact .department-home__metrics button { min-height:88px; padding:12px; }
  .department-home--compact .department-home__metrics strong { font-size:22px; }
}
</style>
