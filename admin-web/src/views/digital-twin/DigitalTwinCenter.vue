<template>
  <PageContainer title="数字孪生中心" sub-title="智慧园区 · 一个三维场景，六个只读视图；专业办理跳转原业务模块">
    <template #badge>
      <a-tag v-if="scene" :color="DATA_MODE_TAG[scene.dataMode].color">{{ DATA_MODE_TAG[scene.dataMode].label }}</a-tag>
    </template>
    <template #extra>
      <a-button :loading="loadState === 'loading'" @click="reload">刷新</a-button>
    </template>

    <div class="twin">
      <!-- 左：模式、楼栋列表、图层、快捷视角 -->
      <aside class="twin-side">
        <div class="twin-block">
          <div class="twin-block-title">场景模式</div>
          <div class="twin-modes" role="tablist">
            <button v-for="m in TWIN_MODES" :key="m.key" type="button" role="tab" :aria-selected="mode === m.key"
              class="twin-mode" :class="{ active: mode === m.key }" @click="setMode(m.key)">
              <span>{{ m.label }}</span>
              <small v-if="!m.ready">尚未接入</small>
            </button>
          </div>
        </div>

        <div class="twin-block twin-block--grow">
          <div class="twin-block-title">楼栋（{{ buildings.length }}）</div>
          <a-input-search v-model:value="keyword" placeholder="搜索编码或名称" allow-clear />
          <ul class="twin-list">
            <li v-for="a in filtered" :key="a.assetId">
              <button type="button" :ref="(el) => setItemRef(a.assetId, el)" class="twin-item" :class="{ active: selectedId === a.assetId }"
                @click="selectFromList(a)">
                <span class="twin-item-name">{{ a.displayName }}</span>
                <span class="twin-item-meta">{{ a.assetId }} · {{ VERIFICATION_LABEL[a.verificationStatus] || a.verificationStatus }}</span>
              </button>
            </li>
            <li v-if="!filtered.length" class="twin-empty">没有匹配的楼栋</li>
          </ul>
        </div>

        <div class="twin-block">
          <div class="twin-block-title">图层</div>
          <div v-for="l in LAYERS" :key="l.key" class="twin-layer">
            <span>{{ l.label }}</span>
            <a-switch size="small" :checked="layers[l.key]" :disabled="!handle" @change="(v: unknown) => toggleLayer(l.key, Boolean(v))" />
          </div>
          <div v-for="l in ['报警', '设备', '摄像头']" :key="l" class="twin-layer twin-layer--off">
            <span>{{ l }}</span><small>尚未接入</small>
          </div>
        </div>

        <div class="twin-block">
          <div class="twin-block-title">视角</div>
          <a-space wrap>
            <a-button size="small" :disabled="!handle" @click="handle?.setView('top')">俯视</a-button>
            <a-button size="small" :disabled="!handle" @click="handle?.setView('reset')">复位镜头</a-button>
            <a-tooltip title="正门方向未核实，不预设"><a-button size="small" disabled>正门</a-button></a-tooltip>
          </a-space>
        </div>
      </aside>

      <!-- 中：三维视图 -->
      <section class="twin-stage">
        <div ref="canvasHost" class="twin-canvas" :class="{ hidden: loadState !== 'ready' }" />
        <div v-if="loadState === 'ready'" class="twin-watermark" aria-hidden="true">概念示意 · 非实测</div>
        <div v-if="loadState === 'ready'" class="twin-badge">
          概念示意 · dataMode=DEMO · {{ scene?.coordinateNote || '示意坐标，不具测绘精度' }}
        </div>
        <div v-if="loadState === 'ready'" class="twin-hint">拖动旋转 · 右键平移 · 滚轮缩放 · 单击选中 · 双击聚焦</div>

        <div v-if="loadState === 'loading'" class="twin-center"><a-spin tip="正在加载三维场景…" /></div>
        <div v-else-if="loadState === 'error' || loadState === 'nowebgl'" class="twin-fallback">
          <a-alert type="warning" show-icon
            :message="loadState === 'nowebgl' ? '当前浏览器不支持 WebGL，已切换为静态平面示意' : '三维场景加载失败，已切换为静态平面示意'"
            :description="errorText">
            <template #action><a-button size="small" type="primary" @click="reload">重试</a-button></template>
          </a-alert>
          <svg v-if="assets.length" class="twin-plan" :viewBox="`${bounds.x} ${bounds.y} ${bounds.width} ${bounds.height}`" role="img" aria-label="概念示意平面图">
            <g v-for="a in buildings" :key="a.assetId">
              <rect v-if="a.modelNodeKey && CONCEPT_LAYOUT[a.modelNodeKey]" class="twin-plan-bld" :class="{ active: selectedId === a.assetId }"
                v-bind="planRect(CONCEPT_LAYOUT[a.modelNodeKey])" tabindex="0" @click="selectFromList(a)" @keydown.enter="selectFromList(a)" />
              <text v-if="a.modelNodeKey && CONCEPT_LAYOUT[a.modelNodeKey]" class="twin-plan-label"
                :x="CONCEPT_LAYOUT[a.modelNodeKey].x" :y="CONCEPT_LAYOUT[a.modelNodeKey].z + 2">{{ shortLabel(a.assetId) }}</text>
            </g>
          </svg>
        </div>
      </section>

      <!-- 右：详情 -->
      <aside class="twin-detail">
        <template v-if="selected">
          <div class="twin-detail-head">
            <div>
              <h3>{{ selected.displayName }}</h3>
              <div class="twin-detail-code">{{ selected.assetId }}</div>
            </div>
            <a-button type="text" size="small" @click="select(null)">关闭</a-button>
          </div>
          <a-descriptions :column="1" size="small" bordered>
            <a-descriptions-item label="类别">{{ selected.assetType === 'BUILDING' ? '楼栋' : '园区' }}</a-descriptions-item>
            <a-descriptions-item label="核实状态">
              <a-tag color="orange">{{ VERIFICATION_LABEL[selected.verificationStatus] || selected.verificationStatus }}</a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="名称/用途/层数/面积">未核实，不显示</a-descriptions-item>
            <a-descriptions-item label="来源资料">{{ selected.sourceReference || '—' }}</a-descriptions-item>
            <a-descriptions-item label="模型节点">{{ selected.modelNodeKey || '—' }}</a-descriptions-item>
            <a-descriptions-item label="业务绑定">{{ selected.bound ? '已绑定楼栋主数据' : '未绑定' }}</a-descriptions-item>
          </a-descriptions>
        </template>
        <div v-else class="twin-detail-head">
          <div>
            <h3>园区（概念示意）</h3>
            <div class="twin-detail-code">GF-CAMPUS-001 · 场景版本 {{ scene?.version || '—' }}</div>
          </div>
        </div>

        <div class="twin-card">
          <div class="twin-card-title">
            {{ currentMode.label }}
            <a-tag v-if="mode !== 'campus' && summary" :color="DATA_MODE_TAG[summary.dataMode].color">{{ DATA_MODE_TAG[summary.dataMode].label }}</a-tag>
          </div>
          <p v-if="mode === 'campus'" class="twin-muted">
            园区外观依据规划效果图与航拍照片做的概念示意，楼栋数量、位置与体量都待院方核实；楼栋编号为临时编码，不代表真实名称或用途。
          </p>
          <p v-else-if="mode === 'occupancy' && !scene?.canViewOccupancy" class="twin-muted">当前岗位无权查看床位汇总。</p>
          <a-spin v-else :spinning="summaryLoading">
            <template v-if="summary">
              <p v-if="summary.message" class="twin-muted">{{ summary.message }}</p>
              <div v-if="summary.metrics.length" class="twin-metrics">
                <div v-for="m in summary.metrics" :key="m.key" class="twin-metric">
                  <span>{{ m.label }}</span><strong>{{ m.value ?? '—' }}</strong><small>{{ m.unit }}</small>
                </div>
              </div>
              <p v-if="summary.scope || summary.source" class="twin-source">
                {{ summary.scope }}<template v-if="summary.source"> · 来源：{{ summary.source }}</template>
                <template v-if="summary.updatedAt"> · 更新于 {{ dayjs(summary.updatedAt).format('YYYY-MM-DD HH:mm:ss') }}</template>
              </p>
              <a-button v-if="summary.link && canOpen(summary.link)" size="small" @click="router.push(summary.link)">打开原业务模块</a-button>
            </template>
            <a-alert v-else-if="summaryError" type="error" :message="summaryError" show-icon />
          </a-spin>
        </div>

        <!-- 管理员：楼栋绑定（建立 → 发布，服务端写审计） -->
        <div v-if="scene?.canManageBindings && selected?.assetType === 'BUILDING'" class="twin-card">
          <div class="twin-card-title">业务绑定（管理员）</div>
          <p class="twin-muted">仅在院方确认该示例楼栋对应哪栋真实楼后再绑定；发布后床位汇总才会显示数字。</p>
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
      <span>模型版本 {{ scene?.version || '—' }}（{{ scene?.modelUrl ? 'GLB' : '程序化概念楼栋' }}）</span>
      <span>场景加载 {{ loadedAt || '—' }}</span>
      <span>当前选中 {{ selectedId || '无' }}</span>
      <span>实时事件：尚未接入</span>
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
  createTwinBinding, getTwinAssets, getTwinBindings, getTwinScene, getTwinSummary, publishTwinBinding, retireTwinBinding,
  type TwinAsset, type TwinBinding, type TwinScene, type TwinSummary
} from '../../api/digitalTwin'
import { getBuildingList } from '../../api/bed'
import type { BuildingItem } from '../../types/bed'
import { CONCEPT_LAYOUT, DATA_MODE_TAG, TWIN_MODES, VERIFICATION_LABEL, filterAssets, layoutBounds, normalizeMode, shortLabel, type Footprint, type TwinMode } from './twinModel'
import { createTwinScene, type TwinLayer, type TwinSceneHandle } from './twinScene'
import { getPagePermissions, getPermissions, getRoles } from '../../utils/auth'
import { resolveRouteAccess } from '../../utils/routeAccess'

const route = useRoute()
const router = useRouter()

const LAYERS: Array<{ key: TwinLayer; label: string }> = [
  { key: 'buildings', label: '建筑' }, { key: 'roads', label: '道路' }, { key: 'vegetation', label: '绿化与水体' }, { key: 'labels', label: '楼栋标签' }
]
const BINDING_LABEL = { PENDING: '待发布', ACTIVE: '生效中', RETIRED: '已停用' } as const

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
let disposed = false

const mode = computed(() => normalizeMode(route.query.mode))
const currentMode = computed(() => TWIN_MODES.find((m) => m.key === mode.value)!)
const buildings = computed(() => assets.value.filter((a) => a.assetType === 'BUILDING'))
const filtered = computed(() => filterAssets(buildings.value, keyword.value))
const selected = computed(() => assets.value.find((a) => a.assetId === selectedId.value) || null)
const bounds = layoutBounds(CONCEPT_LAYOUT)

/** 跳转原业务模块前先看本岗位能否打开，避免把人送到 403 */
function canOpen(path: string) {
  return resolveRouteAccess(router, getRoles(), path, getPagePermissions(), getPermissions()).canAccess
}

function setMode(key: TwinMode) {
  router.replace({ query: { ...route.query, mode: key } })
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
}
function selectFromList(asset: TwinAsset) {
  select(asset.assetId)
  if (asset.modelNodeKey) handle.value?.focus(asset.modelNodeKey)
}
function onSceneSelect(nodeKey: string | null) {
  const asset = nodeKey ? assets.value.find((a) => a.modelNodeKey === nodeKey) : null
  select(asset?.assetId ?? null)
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
  const labels = Object.fromEntries(buildings.value.filter((a) => a.modelNodeKey).map((a) => [a.modelNodeKey!, shortLabel(a.assetId)]))
  try {
    const created = await createTwinScene(canvasHost.value, { modelUrl: scene.value!.modelUrl, labels, onSelect: onSceneSelect })
    if (disposed) { created.dispose(); return }
    handle.value = created
    ;(Object.keys(layers) as TwinLayer[]).forEach((k) => created.setLayer(k, layers[k]))
    if (selected.value?.modelNodeKey) created.highlight(selected.value.modelNodeKey)
    loadState.value = 'ready'
    loadedAt.value = dayjs().format('HH:mm:ss')
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

// ---------- 模式汇总 ----------
const summary = ref<TwinSummary | null>(null)
const summaryLoading = ref(false)
const summaryError = ref('')
let summaryTicket = 0
async function loadSummary() {
  summary.value = null
  summaryError.value = ''
  if (mode.value === 'campus' || !scene.value || (mode.value === 'occupancy' && !scene.value.canViewOccupancy)) return
  const ticket = ++summaryTicket
  summaryLoading.value = true
  try {
    const result = await getTwinSummary(selectedId.value || 'GF-CAMPUS-001', mode.value)
    if (ticket === summaryTicket) summary.value = result
  } catch (e) {
    if (ticket === summaryTicket) summaryError.value = (e as Error)?.message || '汇总接口不可用'
  } finally {
    if (ticket === summaryTicket) summaryLoading.value = false
  }
}
watch([mode, selectedId, scene], loadSummary)

// ---------- 管理员绑定 ----------
const bindings = ref<TwinBinding[]>([])
const buildingOptions = ref<BuildingItem[]>([])
const newBuildingId = ref<string>()
const bindingBusy = ref(false)
function buildingName(id: string) {
  return buildingOptions.value.find((b) => String(b.id) === id)?.name || `#${id}`
}
async function loadBindings() {
  bindings.value = []
  if (!scene.value?.canManageBindings || selected.value?.assetType !== 'BUILDING') return
  const assetId = selected.value.assetId
  const [rows] = await Promise.all([
    getTwinBindings(assetId),
    buildingOptions.value.length ? null : getBuildingList().then((list) => { buildingOptions.value = list })
  ])
  if (selectedId.value === assetId) bindings.value = rows
}
watch([selectedId, scene], () => { loadBindings().catch(() => message.error('绑定列表加载失败')) })

async function refreshAfterBinding() {
  await loadBindings()
  assets.value = await getTwinAssets()
  await loadSummary()
}
async function createBinding() {
  if (!selected.value || !newBuildingId.value) return
  bindingBusy.value = true
  try {
    await createTwinBinding({ assetId: selected.value.assetId, entityType: 'BUILDING', externalEntityId: newBuildingId.value })
    newBuildingId.value = undefined
    message.success('已建立绑定，发布后生效')
    await refreshAfterBinding()
  } finally {
    bindingBusy.value = false
  }
}
async function runBinding(action: (id: string) => Promise<TwinBinding>, id: string) {
  await action(id)
  message.success('已更新')
  await refreshAfterBinding()
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
  grid-template-columns: 280px minmax(0, 1fr) 340px;
  gap: 12px;
  height: calc(100vh - 230px);
  min-height: 560px;
}
.twin-side, .twin-detail {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-height: 0;
  overflow: auto;
}
.twin-block, .twin-card, .twin-detail-head {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 10px;
  padding: 12px;
}
.twin-block--grow { flex: 1; display: flex; flex-direction: column; min-height: 180px; }
.twin-block-title, .twin-card-title { font-weight: 600; color: var(--ink); margin-bottom: 8px; display: flex; gap: 8px; align-items: center; }
.twin-modes { display: grid; grid-template-columns: 1fr 1fr; gap: 6px; }
.twin-mode {
  border: 1px solid var(--border); background: var(--surface); border-radius: 8px; padding: 6px 8px;
  text-align: left; cursor: pointer; color: var(--ink); display: flex; flex-direction: column;
}
.twin-mode small { color: var(--muted); font-size: 11px; }
.twin-mode.active { border-color: var(--primary); background: var(--primary-soft); color: var(--primary-strong); }
.twin-list { list-style: none; margin: 8px 0 0; padding: 0; overflow: auto; flex: 1; }
.twin-item {
  width: 100%; border: 0; background: transparent; text-align: left; padding: 6px 8px; border-radius: 6px; cursor: pointer;
  display: flex; flex-direction: column;
}
.twin-item:hover { background: var(--primary-soft); }
.twin-item.active { background: var(--primary); color: #fff; }
.twin-item.active .twin-item-meta { color: rgba(255, 255, 255, 0.85); }
.twin-item-name { font-weight: 500; }
.twin-item-meta { font-size: 12px; color: var(--muted); }
.twin-empty, .twin-muted { color: var(--muted); font-size: 13px; }
.twin-layer { display: flex; justify-content: space-between; align-items: center; padding: 3px 0; }
.twin-layer--off { color: var(--muted); }
.twin-layer--off small { font-size: 11px; }
.twin-stage {
  position: relative; border-radius: 12px; overflow: hidden; border: 1px solid var(--border); background: #eef3ef; min-height: 0;
}
.twin-canvas { position: absolute; inset: 0; }
.twin-canvas.hidden { visibility: hidden; }
.twin-canvas :deep(.twin-label-layer) { position: absolute; inset: 0; pointer-events: none; }
.twin-canvas :deep(.twin-label) {
  background: rgba(24, 92, 78, 0.88); color: #fff; font-size: 11px; padding: 1px 6px; border-radius: 4px; white-space: nowrap;
}
.twin-watermark {
  position: absolute; inset: 0; display: flex; align-items: center; justify-content: center; pointer-events: none;
  font-size: 44px; font-weight: 700; color: rgba(33, 112, 95, 0.08); transform: rotate(-18deg); letter-spacing: 6px;
}
.twin-badge, .twin-hint {
  position: absolute; left: 12px; padding: 4px 10px; border-radius: 6px; font-size: 12px; pointer-events: none;
}
.twin-badge { top: 12px; background: rgba(222, 155, 61, 0.92); color: #fff; }
.twin-hint { bottom: 12px; background: rgba(255, 255, 255, 0.85); color: var(--muted); }
.twin-center { position: absolute; inset: 0; display: flex; align-items: center; justify-content: center; }
.twin-fallback { position: absolute; inset: 0; padding: 12px; display: flex; flex-direction: column; gap: 12px; }
.twin-plan { flex: 1; width: 100%; min-height: 0; background: #e3ecdc; border-radius: 8px; }
.twin-plan-bld { fill: #f1e9dc; stroke: #c0583f; stroke-width: 1.2; cursor: pointer; }
.twin-plan-bld.active, .twin-plan-bld:focus { fill: var(--primary-soft); stroke: var(--primary); stroke-width: 2; outline: none; }
.twin-plan-label { font-size: 6px; text-anchor: middle; fill: var(--ink); pointer-events: none; }
.twin-detail-head { display: flex; justify-content: space-between; align-items: flex-start; }
.twin-detail-head h3 { margin: 0; font-size: 16px; color: var(--ink); }
.twin-detail-code { font-size: 12px; color: var(--muted); }
.twin-metrics { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; margin-bottom: 8px; }
.twin-metric { background: var(--primary-soft); border-radius: 8px; padding: 8px; display: flex; flex-direction: column; }
.twin-metric span { font-size: 12px; color: var(--muted); }
.twin-metric strong { font-size: 20px; color: var(--primary-strong); }
.twin-metric small { font-size: 11px; color: var(--muted); }
.twin-source { font-size: 12px; color: var(--muted); }
.twin-bindings { list-style: none; padding: 0; margin: 0 0 10px; }
.twin-bindings li { display: flex; align-items: center; gap: 6px; padding: 3px 0; }
.twin-status {
  display: flex; flex-wrap: wrap; gap: 18px; margin-top: 10px; padding: 8px 12px; font-size: 12px; color: var(--muted);
  background: var(--surface); border: 1px solid var(--border); border-radius: 8px;
}
@media (max-width: 1200px) {
  .twin { grid-template-columns: 240px minmax(0, 1fr); height: auto; }
  .twin-stage { height: 520px; }
  .twin-detail { grid-column: 1 / -1; }
}
@media (max-width: 768px) {
  .twin { grid-template-columns: 1fr; }
}
</style>
