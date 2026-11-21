<template>
  <PageContainer title="数字孪生中心" sub-title="智慧园区 · 一个三维场景，六个只读视图；数字来自源业务表，办理跳转原模块">
    <template #badge>
      <a-tag v-if="scene" color="green">楼栋来自主数据 · 布局示意</a-tag>
    </template>
    <template #extra>
      <a-button :loading="loadState === 'loading'" @click="reload">刷新</a-button>
    </template>

    <div class="twin">
      <!-- 左：模式、楼栋列表、图层、视角 -->
      <aside class="twin-side">
        <div class="twin-block">
          <div class="twin-block-title">场景模式</div>
          <div class="twin-modes" role="tablist">
            <button v-for="m in TWIN_MODES" :key="m.key" type="button" role="tab" :aria-selected="mode === m.key"
              class="twin-mode" :class="{ active: mode === m.key, locked: !canSee(m.key) }" @click="setMode(m.key)">
              <span>{{ m.label }}</span>
              <small>{{ canSee(m.key) ? m.hint : '本岗位不可见' }}</small>
            </button>
          </div>
        </div>

        <div class="twin-block twin-block--grow">
          <div class="twin-block-title">
            楼栋（{{ listed.length }}）
            <a-popconfirm v-if="scene?.canManageBindings" title="为尚未接入的楼栋主数据生成三维楼栋并生效绑定（写审计）" @confirm="syncBuildings">
              <a-button size="small" type="link" :loading="syncing" class="twin-sync">从楼栋主数据同步</a-button>
            </a-popconfirm>
          </div>
          <a-input-search v-model:value="keyword" placeholder="搜索楼栋名称或编码" allow-clear />
          <ul class="twin-list">
            <li v-for="a in filtered" :key="a.assetId">
              <button type="button" :ref="(el) => setItemRef(a.assetId, el)" class="twin-item" :class="{ active: selectedId === a.assetId, concept: !a.buildingName }"
                @click="selectFromList(a)">
                <span class="twin-item-dot" :style="{ background: dotColor(a) }" />
                <span class="twin-item-body">
                  <span class="twin-item-name">{{ assetName(a) }}</span>
                  <span class="twin-item-meta">{{ itemMeta(a) }}</span>
                </span>
              </button>
            </li>
            <li v-if="!filtered.length" class="twin-empty">没有匹配的楼栋</li>
          </ul>
          <p v-if="!hasReal" class="twin-tip">楼栋主数据里还没有楼栋，请先在楼栋管理中添加。</p>
        </div>

        <div class="twin-block">
          <div class="twin-block-title">图层与视角</div>
          <div v-for="l in LAYERS" :key="l.key" class="twin-layer">
            <span>{{ l.label }}</span>
            <a-switch size="small" :checked="layers[l.key]" :disabled="!handle" @change="(v: unknown) => toggleLayer(l.key, Boolean(v))" />
          </div>
          <a-space wrap class="twin-views">
            <a-button size="small" :disabled="!handle" @click="handle?.setView('top')">俯视</a-button>
            <a-button size="small" :disabled="!handle" @click="handle?.setView('reset')">复位镜头</a-button>
            <a-tooltip title="正门方向未核实，不预设"><a-button size="small" disabled>正门</a-button></a-tooltip>
          </a-space>
        </div>
      </aside>

      <!-- 中：三维视图 -->
      <section class="twin-stage">
        <div ref="canvasHost" class="twin-canvas" :class="{ hidden: loadState !== 'ready' }" />
        <template v-if="loadState === 'ready'">
          <div class="twin-hud">
            <div class="twin-hud-title">
              <strong>{{ currentMode.label }}</strong>
              <span>楼栋、楼层取自主数据；位置与外形为示意，不具测绘精度</span>
            </div>
            <div v-if="campusSummary?.metrics.length" class="twin-kpis" aria-label="全园指标">
              <div v-for="m in campusSummary.metrics.slice(0, 5)" :key="m.key" class="twin-kpi" :class="m.tone">
                <strong>{{ fmt(m.value) }}<small>{{ m.unit }}</small></strong>
                <span>{{ m.label }}</span>
              </div>
            </div>
          </div>
          <div v-if="legend" class="twin-legend">
            <div class="twin-legend-title">{{ mode === 'occupancy' ? '楼栋入住率' : '未完成报修' }}</div>
            <div v-for="l in legend" :key="l.label" class="twin-legend-row"><i :style="{ background: hex(l.color) }" />{{ l.label }}</div>
            <div class="twin-legend-row"><i :style="{ background: hex(OVERLAY_EMPTY) }" />未绑定 / 无数据</div>
          </div>
          <div class="twin-hint">拖动旋转 · 右键平移 · 滚轮缩放 · 单击选中 · 双击聚焦</div>
        </template>

        <div v-if="loadState === 'loading'" class="twin-center"><a-spin tip="正在加载三维场景…" /></div>
        <div v-else-if="loadState === 'error' || loadState === 'nowebgl'" class="twin-fallback">
          <a-alert type="warning" show-icon
            :message="loadState === 'nowebgl' ? '当前浏览器不支持 WebGL，已切换为静态平面示意' : '三维场景加载失败，已切换为静态平面示意'"
            :description="errorText">
            <template #action><a-button size="small" type="primary" @click="reload">重试</a-button></template>
          </a-alert>
          <svg v-if="placed.length" class="twin-plan" :viewBox="`${bounds.x} ${bounds.y} ${bounds.width} ${bounds.height}`" role="img" aria-label="楼栋示意平面图">
            <g v-for="p in placed" :key="p.key">
              <rect class="twin-plan-bld" :class="{ active: selected?.modelNodeKey === p.key, concept: !p.real }" v-bind="planRect(p)" tabindex="0"
                @click="selectByKey(p.key)" @keydown.enter="selectByKey(p.key)" />
              <text class="twin-plan-label" :x="p.x" :y="p.z + 2">{{ labelOf(p.key) }}</text>
            </g>
          </svg>
        </div>
      </section>

      <!-- 右：详情 -->
      <aside class="twin-detail">
        <div class="twin-hero" :class="{ concept: selected && !selected.buildingName }">
          <small>{{ selected ? (selected.buildingName ? '楼栋 · 主数据' : '楼栋 · 未接入主数据') : '园区' }}</small>
          <h3>{{ selected ? assetName(selected) : '全园' }}</h3>
          <div class="twin-hero-code">{{ selected?.assetId || CAMPUS_ID }} · 场景 {{ scene?.version || '—' }}</div>
          <a-button v-if="selected" type="text" size="small" class="twin-hero-close" @click="select(null)">返回园区</a-button>
        </div>

        <div v-if="selected" class="twin-card">
          <a-descriptions :column="1" size="small">
            <template v-if="selected.buildingName">
              <a-descriptions-item label="楼层">{{ selected.floorCount ?? 0 }} 层（主数据）</a-descriptions-item>
              <a-descriptions-item label="房间">{{ selected.roomCount ?? 0 }} 间（主数据）</a-descriptions-item>
              <a-descriptions-item label="来源">{{ selected.sourceReference || '楼栋主数据' }}</a-descriptions-item>
              <a-descriptions-item label="三维位置"><a-tag color="orange">示意布局，待测绘</a-tag></a-descriptions-item>
            </template>
            <template v-else>
              <a-descriptions-item label="核实状态"><a-tag color="orange">{{ VERIFICATION_LABEL[selected.verificationStatus] || selected.verificationStatus }}</a-tag></a-descriptions-item>
              <a-descriptions-item label="名称/用途/层数">未核实，不显示</a-descriptions-item>
              <a-descriptions-item label="业务绑定">{{ selected.bound ? '已绑定楼栋主数据' : '未绑定' }}</a-descriptions-item>
            </template>
          </a-descriptions>
        </div>

        <div class="twin-card">
          <div class="twin-card-title">
            {{ currentMode.label }}
            <a-tag v-if="summary" :color="DATA_MODE_TAG[summary.dataMode].color">{{ DATA_MODE_TAG[summary.dataMode].label }}</a-tag>
          </div>
          <p v-if="!canSee(mode)" class="twin-muted">当前岗位无权查看该模式的汇总，可切换其他模式。</p>
          <a-spin v-else :spinning="summaryLoading">
            <template v-if="summary">
              <p v-if="summary.message" class="twin-muted">{{ summary.message }}</p>
              <div v-if="summary.metrics.length" class="twin-metrics">
                <div v-for="m in summary.metrics" :key="m.key" class="twin-metric" :class="m.tone">
                  <span>{{ m.label }}</span>
                  <strong>{{ fmt(m.value) }}<small>{{ m.unit }}</small></strong>
                </div>
              </div>
              <p v-if="summary.scope || summary.source" class="twin-source">
                {{ summary.scope }}<template v-if="summary.source"> · 来源：{{ summary.source }}</template>
                <template v-if="summary.updatedAt"><br>查询于 {{ dayjs(summary.updatedAt).format('YYYY-MM-DD HH:mm:ss') }}</template>
              </p>
              <a-button v-if="summary.link && canOpen(summary.link)" size="small" type="primary" ghost @click="router.push(summary.link)">打开原业务模块</a-button>
            </template>
            <a-alert v-else-if="summaryError" type="error" :message="summaryError" show-icon />
          </a-spin>
        </div>

        <!-- 管理员：概念占位楼栋的手工绑定（建立 → 发布，服务端写审计） -->
        <div v-if="scene?.canManageBindings && selected?.assetType === 'BUILDING' && !selected.buildingName" class="twin-card">
          <div class="twin-card-title">业务绑定（管理员）</div>
          <p class="twin-muted">通常直接用「从楼栋主数据同步」。只有院方确认某个占位楼就是某栋真实楼时才手工绑定；发布后才显示数字。</p>
          <ul class="twin-bindings">
            <li v-for="b in bindings" :key="b.id">
              <span>楼栋 {{ buildingName(b.externalEntityId) }}</span>
              <a-tag :color="b.bindingStatus === 'ACTIVE' ? 'green' : b.bindingStatus === 'PENDING' ? 'blue' : 'default'">{{ BINDING_LABEL[b.bindingStatus] }}</a-tag>
              <a-popconfirm v-if="b.bindingStatus === 'PENDING'" title="发布后立即生效，并停用冲突的旧绑定" @confirm="runBinding(publishTwinBinding, b.id)">
                <a-button size="small" type="link">发布</a-button>
              </a-popconfirm>
              <a-popconfirm v-if="b.bindingStatus !== 'RETIRED'" title="停用该绑定？" @confirm="runBinding(retireTwinBinding, b.id)">
                <a-button size="small" type="link" danger>停用</a-button>
              </a-popconfirm>
            </li>
            <li v-if="!bindings.length" class="twin-muted">暂无绑定</li>
          </ul>
          <a-space>
            <a-select v-model:value="newBuildingId" placeholder="选择楼栋主数据" style="width: 180px"
              :options="buildingOptions.map((b) => ({ value: String(b.id), label: b.name }))" />
            <a-button type="primary" :disabled="!newBuildingId" :loading="bindingBusy" @click="createBinding">建立绑定</a-button>
          </a-space>
        </div>
      </aside>
    </div>

    <footer class="twin-status">
      <span><i class="twin-dot" :class="loadState" />{{ loadState === 'ready' ? '场景运行中' : loadState === 'loading' ? '加载中' : '静态兜底' }}</span>
      <span>模型 {{ scene?.version || '—' }}（{{ scene?.modelUrl ? 'GLB' : '程序化楼栋' }}）</span>
      <span>主数据楼栋 {{ realCount }} 栋</span>
      <span>场景加载 {{ loadedAt || '—' }}</span>
      <span>实时推送：尚未接入（数字在打开、切换时查询）</span>
    </footer>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, nextTick, onActivated, onBeforeUnmount, onDeactivated, onMounted, reactive, ref, shallowRef, watch, type ComponentPublicInstance } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import PageContainer from '../../components/PageContainer.vue'
import {
  createTwinBinding, getTwinAssets, getTwinBindings, getTwinOverlay, getTwinScene, getTwinSummary, publishTwinBinding, retireTwinBinding, syncTwinBuildings,
  type TwinAsset, type TwinBinding, type TwinOverlayItem, type TwinScene, type TwinSummary
} from '../../api/digitalTwin'
import { getBuildingList } from '../../api/bed'
import type { BuildingItem } from '../../types/bed'
import {
  DATA_MODE_TAG, OVERLAY_EMPTY, OVERLAY_LEGEND, TWIN_MODES, VERIFICATION_LABEL, assetName, filterAssets, layoutBounds, normalizeMode,
  overlayColor, placeBuildings, shortLabel, type Footprint, type TwinMode
} from './twinModel'
import { createTwinScene, type TwinLayer, type TwinSceneHandle } from './twinScene'
import { getPagePermissions, getPermissions, getRoles } from '../../utils/auth'
import { resolveRouteAccess } from '../../utils/routeAccess'

type OverlayMode = keyof typeof OVERLAY_LEGEND

const route = useRoute()
const router = useRouter()

const LAYERS: Array<{ key: TwinLayer; label: string }> = [
  { key: 'buildings', label: '建筑' }, { key: 'roads', label: '道路' }, { key: 'vegetation', label: '绿化与水体' }, { key: 'labels', label: '楼栋标签' }
]
const BINDING_LABEL = { PENDING: '待发布', ACTIVE: '生效中', RETIRED: '已停用' } as const
const CAMPUS_ID = 'GF-CAMPUS-001'

const scene = ref<TwinScene | null>(null)
const assets = ref<TwinAsset[]>([])
const loadState = ref<'loading' | 'ready' | 'error' | 'nowebgl'>('loading')
const errorText = ref('')
const loadedAt = ref('')
const canvasHost = ref<HTMLElement | null>(null)
const handle = shallowRef<TwinSceneHandle | null>(null)
const layers = reactive<Record<TwinLayer, boolean>>({ buildings: true, roads: true, vegetation: true, labels: true })
const keyword = ref('')
const selectedId = ref<string | null>(null)
const overlay = ref<TwinOverlayItem[]>([])
let disposed = false

const mode = computed(() => normalizeMode(route.query.mode))
const currentMode = computed(() => TWIN_MODES.find((m) => m.key === mode.value)!)
const placed = computed(() => placeBuildings(assets.value))
const realCount = computed(() => placed.value.filter((p) => p.real).length)
const hasReal = computed(() => realCount.value > 0)
/** 列表只列场景里实际摆出来的楼栋（被主数据楼栋替换掉的占位不再列出） */
const listed = computed(() => {
  const keys = new Set(placed.value.map((p) => p.key))
  return assets.value.filter((a) => a.assetType === 'BUILDING' && a.modelNodeKey && keys.has(a.modelNodeKey))
})
const filtered = computed(() => filterAssets(listed.value, keyword.value))
const selected = computed(() => assets.value.find((a) => a.assetId === selectedId.value) || null)
const bounds = computed(() => layoutBounds(placed.value))
const overlayMode = computed<OverlayMode | null>(() => (mode.value === 'occupancy' || mode.value === 'facility') && canSee(mode.value) ? mode.value : null)
const legend = computed(() => overlayMode.value ? OVERLAY_LEGEND[overlayMode.value] : null)

function canSee(key: string) {
  return key === 'campus' || Boolean(scene.value?.visibleModes?.includes(key))
}
/** 跳转原业务模块前先看本岗位能否打开，避免把人送到 403 */
function canOpen(path: string) {
  return resolveRouteAccess(router, getRoles(), path, getPagePermissions(), getPermissions()).canAccess
}
function setMode(key: TwinMode) {
  router.replace({ query: { ...route.query, mode: key } })
}
function fmt(v: unknown) {
  return v === null || v === undefined ? '—' : String(v)
}
function hex(color: number) {
  return '#' + color.toString(16).padStart(6, '0')
}
function overlayOf(a: TwinAsset) {
  return overlay.value.find((x) => x.assetId === a.assetId)
}
function labelOf(nodeKey: string) {
  const a = assets.value.find((x) => x.modelNodeKey === nodeKey)
  return a ? (a.buildingName || shortLabel(a.assetId)) : nodeKey
}
function defaultSub(a: TwinAsset) {
  return a.buildingName ? `${a.floorCount ?? 0} 层 · ${a.roomCount ?? 0} 间` : '未接入主数据'
}
function itemMeta(a: TwinAsset) {
  const o = overlayOf(a)
  if (overlayMode.value && o) return o.text
  return a.buildingName ? defaultSub(a) : `${a.assetId} · 未接入主数据`
}
function dotColor(a: TwinAsset) {
  if (overlayMode.value) return hex(overlayColor(overlayMode.value, overlayOf(a)?.value ?? null))
  return a.buildingName ? 'var(--primary)' : 'var(--muted-2)'
}
function planRect(f: Footprint) {
  return { x: f.x - f.w / 2, y: f.z - f.d / 2, width: f.w, height: f.d, transform: `rotate(${(-(f.rot ?? 0) * 180) / Math.PI} ${f.x} ${f.z})` }
}

const itemRefs = new Map<string, HTMLElement>()
function setItemRef(id: string, el: Element | ComponentPublicInstance | null) {
  if (el instanceof HTMLElement) itemRefs.set(id, el)
  else itemRefs.delete(id)
}

function select(assetId: string | null) {
  selectedId.value = assetId
  const node = assets.value.find((a) => a.assetId === assetId)?.modelNodeKey ?? null
  handle.value?.highlight(node)
  if (!assetId) handle.value?.setView('reset')
}
function selectFromList(asset: TwinAsset) {
  select(asset.assetId)
  if (asset.modelNodeKey) handle.value?.focus(asset.modelNodeKey)
}
function selectByKey(nodeKey: string) {
  const asset = assets.value.find((a) => a.modelNodeKey === nodeKey)
  if (asset) selectFromList(asset)
}
function onSceneSelect(nodeKey: string | null) {
  const asset = nodeKey ? assets.value.find((a) => a.modelNodeKey === nodeKey) : null
  selectedId.value = asset?.assetId ?? null
  handle.value?.highlight(asset?.modelNodeKey ?? null)
  if (asset) {
    keyword.value = ''
    // 只滚动列表本身，不带动整页
    nextTick(() => {
      const el = itemRefs.get(asset.assetId)
      const list = el?.closest('.twin-list') as HTMLElement | null
      if (el && list) list.scrollTop = el.offsetTop - list.offsetTop - list.clientHeight / 2
    })
  }
}

function toggleLayer(layer: TwinLayer, visible: boolean) {
  layers[layer] = visible
  handle.value?.setLayer(layer, visible)
}

async function load() {
  loadState.value = 'loading'
  errorText.value = ''
  try {
    const [s, a] = await Promise.all([getTwinScene(), getTwinAssets()])
    scene.value = s
    assets.value = a
  } catch (e) {
    loadState.value = 'error'
    errorText.value = '场景清单接口不可用：' + ((e as Error)?.message || '未知错误')
    return
  }
  await nextTick()
  if (!canvasHost.value || disposed) return
  const byKey = new Map(listed.value.map((a) => [a.modelNodeKey!, a]))
  const buildings = placed.value.map((p) => {
    const a = byKey.get(p.key)!
    return { ...p, title: a.buildingName || shortLabel(a.assetId), sub: defaultSub(a) }
  })
  try {
    const created = await createTwinScene(canvasHost.value, { modelUrl: scene.value!.modelUrl, buildings, onSelect: onSceneSelect })
    if (disposed) { created.dispose(); return }
    handle.value = created
    ;(Object.keys(layers) as TwinLayer[]).forEach((k) => created.setLayer(k, layers[k]))
    if (selected.value?.modelNodeKey) created.highlight(selected.value.modelNodeKey)
    loadState.value = 'ready'
    loadedAt.value = dayjs().format('HH:mm:ss')
    applyOverlay()
  } catch (e) {
    const unsupported = (e as Error)?.message === 'WEBGL_UNSUPPORTED'
    loadState.value = unsupported ? 'nowebgl' : 'error'
    errorText.value = unsupported ? '可在支持 WebGL 的 Chrome/Edge 中查看三维场景。' : '模型文件无法加载：' + ((e as Error)?.message || '未知错误')
  }
}

function reload() {
  handle.value?.dispose()
  handle.value = null
  load()
}

// ---------- 楼栋着色（入住率 / 报修） ----------
let overlayTicket = 0
async function loadOverlay() {
  const ticket = ++overlayTicket
  overlay.value = []
  if (overlayMode.value) {
    try {
      const rows = await getTwinOverlay(overlayMode.value)
      if (ticket === overlayTicket) overlay.value = rows
    } catch {
      // 着色失败只影响颜色，详情面板仍按单栋查询
    }
  }
  if (ticket === overlayTicket) applyOverlay()
}
function applyOverlay() {
  const h = handle.value
  if (!h) return
  if (!overlayMode.value) {
    h.setOverlay(null)
    h.setLabelSub({})
    return
  }
  const colors: Record<string, number> = {}
  const subs: Record<string, string> = {}
  for (const a of listed.value) {
    const o = overlayOf(a)
    colors[a.modelNodeKey!] = overlayColor(overlayMode.value, o?.value ?? null)
    subs[a.modelNodeKey!] = o ? o.text : '未绑定'
  }
  h.setOverlay(colors)
  h.setLabelSub(subs)
}
watch([mode, scene], loadOverlay)

// ---------- 模式汇总：园区 KPI 条 + 选中楼栋详情 ----------
const campusSummary = ref<TwinSummary | null>(null)
const summary = ref<TwinSummary | null>(null)
const summaryLoading = ref(false)
const summaryError = ref('')
let summaryTicket = 0
async function loadSummary() {
  summaryError.value = ''
  const ticket = ++summaryTicket
  if (!scene.value || !canSee(mode.value)) {
    summary.value = campusSummary.value = null
    return
  }
  summaryLoading.value = true
  try {
    const target = selectedId.value || CAMPUS_ID
    const [campus, own] = await Promise.all([
      getTwinSummary(CAMPUS_ID, mode.value),
      target === CAMPUS_ID ? null : getTwinSummary(target, mode.value)
    ])
    if (ticket !== summaryTicket) return
    campusSummary.value = campus
    summary.value = own ?? campus
  } catch (e) {
    if (ticket === summaryTicket) {
      summary.value = null
      summaryError.value = (e as Error)?.message || '汇总接口不可用'
    }
  } finally {
    if (ticket === summaryTicket) summaryLoading.value = false
  }
}
watch([mode, selectedId, scene], loadSummary)

// ---------- 管理员：同步与手工绑定 ----------
const syncing = ref(false)
async function syncBuildings() {
  syncing.value = true
  try {
    const r = await syncTwinBuildings()
    message.success(r.created ? `已接入 ${r.created} 栋楼（主数据共 ${r.buildings} 栋）` : `主数据 ${r.buildings} 栋楼都已接入`)
    if (r.created) reload()
  } finally {
    syncing.value = false
  }
}

const bindings = ref<TwinBinding[]>([])
const buildingOptions = ref<BuildingItem[]>([])
const newBuildingId = ref<string>()
const bindingBusy = ref(false)
function buildingName(id: string) {
  return buildingOptions.value.find((b) => String(b.id) === id)?.name || `#${id}`
}
async function loadBindings() {
  bindings.value = []
  if (!scene.value?.canManageBindings || selected.value?.assetType !== 'BUILDING' || selected.value.buildingName) return
  const assetId = selected.value.assetId
  const [rows] = await Promise.all([
    getTwinBindings(assetId),
    buildingOptions.value.length ? null : getBuildingList().then((list) => { buildingOptions.value = list })
  ])
  if (selectedId.value === assetId) bindings.value = rows
}
watch([selectedId, scene], () => { loadBindings().catch(() => message.error('绑定列表加载失败')) })

async function createBinding() {
  if (!selected.value || !newBuildingId.value) return
  bindingBusy.value = true
  try {
    await createTwinBinding({ assetId: selected.value.assetId, entityType: 'BUILDING', externalEntityId: newBuildingId.value })
    newBuildingId.value = undefined
    message.success('已建立绑定，发布后生效')
    await loadBindings()
  } finally {
    bindingBusy.value = false
  }
}
async function runBinding(action: (id: string) => Promise<TwinBinding>, id: string) {
  await action(id)
  message.success('已更新')
  await loadBindings()
  reload()
}

onMounted(load)
// 布局用 keep-alive 缓存标签页：切走时释放 WebGL，切回再建，避免后台继续占用 GPU
onDeactivated(() => {
  handle.value?.dispose()
  handle.value = null
})
onActivated(() => {
  if (!handle.value && loadState.value === 'ready') load()
})
onBeforeUnmount(() => {
  disposed = true
  handle.value?.dispose()
  handle.value = null
})
</script>

<style scoped>
.twin {
  display: grid;
  grid-template-columns: 272px minmax(0, 1fr) 330px;
  gap: 14px;
  height: calc(100vh - 230px);
  min-height: 600px;
}
.twin-side, .twin-detail { display: flex; flex-direction: column; gap: 12px; min-height: 0; overflow: auto; }
.twin-block, .twin-card {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 14px;
  padding: 14px;
  box-shadow: 0 1px 2px rgba(34, 51, 46, 0.04);
}
.twin-block--grow { flex: 1; display: flex; flex-direction: column; min-height: 200px; }
.twin-block-title, .twin-card-title {
  font-weight: 600; color: var(--ink); margin-bottom: 10px; display: flex; gap: 8px; align-items: center;
}
.twin-sync { margin-left: auto; padding: 0; }
.twin-modes { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; }
.twin-mode {
  border: 1px solid var(--border); background: var(--surface-2); border-radius: 10px; padding: 8px 10px;
  text-align: left; cursor: pointer; color: var(--ink); display: flex; flex-direction: column; gap: 2px;
  transition: border-color 0.15s, background 0.15s, transform 0.15s;
}
.twin-mode:hover { border-color: var(--primary); transform: translateY(-1px); }
.twin-mode span { font-weight: 500; }
.twin-mode small { color: var(--muted-2); font-size: 11px; }
.twin-mode.active { border-color: transparent; background: var(--primary-gradient); color: #fff; box-shadow: 0 4px 12px rgba(var(--primary-rgb), 0.25); }
.twin-mode.active small { color: rgba(255, 255, 255, 0.8); }
.twin-mode.locked:not(.active) { opacity: 0.55; }
.twin-list { list-style: none; margin: 10px 0 0; padding: 0; overflow: auto; flex: 1; }
.twin-item {
  width: 100%; border: 0; background: transparent; text-align: left; padding: 8px; border-radius: 10px; cursor: pointer;
  display: flex; gap: 10px; align-items: center;
}
.twin-item:hover { background: var(--primary-soft); }
.twin-item.active { background: var(--primary-soft-2); box-shadow: inset 3px 0 0 var(--primary); }
.twin-item.concept .twin-item-name { color: var(--muted); }
.twin-item-dot { width: 10px; height: 10px; border-radius: 3px; flex: none; }
.twin-item-body { display: flex; flex-direction: column; min-width: 0; }
.twin-item-name { font-weight: 500; color: var(--ink); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.twin-item-meta { font-size: 12px; color: var(--muted-2); }
.twin-tip { margin: 8px 0 0; font-size: 12px; color: var(--muted); background: var(--surface-3); border-radius: 8px; padding: 8px; }
.twin-empty, .twin-muted { color: var(--muted); font-size: 13px; }
.twin-layer { display: flex; justify-content: space-between; align-items: center; padding: 4px 0; color: var(--ink-soft); }
.twin-views { margin-top: 10px; }

.twin-stage {
  position: relative; border-radius: 16px; overflow: hidden; border: 1px solid var(--border); min-height: 0;
  background: linear-gradient(180deg, #d4e7ea, #f3f1ea);
}
.twin-canvas { position: absolute; inset: 0; }
.twin-canvas.hidden { visibility: hidden; }
.twin-canvas :deep(.twin-label-layer) { position: absolute; inset: 0; pointer-events: none; }
.twin-canvas :deep(.twin-label) {
  display: flex; flex-direction: column; align-items: center; gap: 1px;
  background: rgba(255, 255, 255, 0.92); color: var(--muted); font-size: 11px; padding: 3px 8px; border-radius: 8px;
  white-space: nowrap; box-shadow: 0 2px 8px rgba(34, 51, 46, 0.14); border: 1px dashed var(--border-strong);
}
.twin-canvas :deep(.twin-label b) { font-weight: 600; font-size: 12px; color: var(--ink-soft); }
.twin-canvas :deep(.twin-label span:empty) { display: none; }
.twin-canvas :deep(.twin-label--real) { border: 0; border-left: 3px solid var(--primary); }
.twin-canvas :deep(.twin-label--real b) { color: var(--primary-strong); }
.twin-hud {
  position: absolute; top: 12px; left: 12px; right: 12px; display: flex; flex-wrap: wrap; gap: 10px; align-items: flex-start;
  justify-content: space-between; pointer-events: none;
}
.twin-hud-title, .twin-kpis, .twin-legend, .twin-hint {
  background: rgba(255, 255, 255, 0.82); backdrop-filter: blur(8px); border: 1px solid rgba(255, 255, 255, 0.7);
  border-radius: 12px; box-shadow: 0 4px 16px rgba(34, 51, 46, 0.1);
}
.twin-hud-title { padding: 8px 12px; display: flex; flex-direction: column; max-width: 360px; }
.twin-hud-title strong { color: var(--ink); font-size: 14px; }
.twin-hud-title span { color: var(--muted); font-size: 11px; }
.twin-kpis { display: flex; padding: 6px 4px; }
.twin-kpi { display: flex; flex-direction: column; align-items: center; padding: 2px 12px; border-left: 1px solid var(--border-soft); }
.twin-kpi:first-child { border-left: 0; }
.twin-kpi strong { font-size: 20px; line-height: 1.2; color: var(--primary-strong); font-variant-numeric: tabular-nums; }
.twin-kpi strong small { font-size: 11px; margin-left: 2px; color: var(--muted); font-weight: 400; }
.twin-kpi span { font-size: 11px; color: var(--muted); }
.twin-kpi.warn strong { color: var(--warning); }
.twin-kpi.danger strong { color: var(--danger); }
.twin-legend { position: absolute; right: 12px; bottom: 12px; padding: 8px 12px; font-size: 12px; color: var(--ink-soft); pointer-events: none; }
.twin-legend-title { font-weight: 600; margin-bottom: 4px; }
.twin-legend-row { display: flex; align-items: center; gap: 6px; }
.twin-legend-row i { width: 12px; height: 12px; border-radius: 3px; display: inline-block; }
.twin-hint { position: absolute; left: 12px; bottom: 12px; padding: 4px 10px; font-size: 12px; color: var(--muted); pointer-events: none; }
.twin-center { position: absolute; inset: 0; display: flex; align-items: center; justify-content: center; }
.twin-fallback { position: absolute; inset: 0; padding: 12px; display: flex; flex-direction: column; gap: 12px; }
.twin-plan { flex: 1; width: 100%; min-height: 0; background: #e3ecdc; border-radius: 10px; }
.twin-plan-bld { fill: #f4ede2; stroke: #b9553d; stroke-width: 1.2; cursor: pointer; }
.twin-plan-bld.concept { fill-opacity: 0.5; stroke-dasharray: 3 2; }
.twin-plan-bld.active, .twin-plan-bld:focus { fill: var(--primary-soft); stroke: var(--primary); stroke-width: 2; outline: none; }
.twin-plan-label { font-size: 6px; text-anchor: middle; fill: var(--ink); pointer-events: none; }

.twin-hero {
  position: relative; border-radius: 14px; padding: 16px; color: #fff; background: var(--primary-gradient);
  box-shadow: 0 6px 18px rgba(var(--primary-rgb), 0.25);
}
.twin-hero.concept { background: linear-gradient(135deg, #7d8c87, #9eaba6); box-shadow: none; }
.twin-hero small { opacity: 0.85; font-size: 12px; }
.twin-hero h3 { margin: 2px 0 4px; font-size: 18px; color: #fff; }
.twin-hero-code { font-size: 12px; opacity: 0.85; }
.twin-hero-close { position: absolute; top: 10px; right: 8px; color: #fff !important; }
.twin-metrics { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; margin-bottom: 10px; }
.twin-metric {
  background: var(--surface-3); border-radius: 10px; padding: 10px; display: flex; flex-direction: column;
  border-left: 3px solid var(--primary);
}
.twin-metric span { font-size: 12px; color: var(--muted); }
.twin-metric strong { font-size: 22px; color: var(--ink); font-variant-numeric: tabular-nums; }
.twin-metric strong small { font-size: 11px; color: var(--muted); font-weight: 400; margin-left: 2px; }
.twin-metric.warn { border-left-color: var(--warning); background: rgba(var(--warning-rgb), 0.08); }
.twin-metric.danger { border-left-color: var(--danger); background: rgba(var(--danger-rgb), 0.08); }
.twin-metric.danger strong { color: var(--danger); }
.twin-source { font-size: 12px; color: var(--muted-2); }
.twin-bindings { list-style: none; padding: 0; margin: 0 0 10px; }
.twin-bindings li { display: flex; align-items: center; gap: 6px; padding: 3px 0; }
.twin-status {
  display: flex; flex-wrap: wrap; gap: 20px; margin-top: 12px; padding: 8px 14px; font-size: 12px; color: var(--muted);
  background: var(--surface); border: 1px solid var(--border); border-radius: 10px; align-items: center;
}
.twin-dot { display: inline-block; width: 8px; height: 8px; border-radius: 50%; margin-right: 6px; background: var(--warning); }
.twin-dot.ready { background: #3aa76d; box-shadow: 0 0 0 3px rgba(58, 167, 109, 0.18); }
@media (max-width: 1200px) {
  .twin { grid-template-columns: 240px minmax(0, 1fr); height: auto; }
  .twin-stage { height: 560px; }
  .twin-detail { grid-column: 1 / -1; }
}
@media (max-width: 768px) {
  .twin { grid-template-columns: 1fr; }
  .twin-kpis { display: none; }
}
</style>
