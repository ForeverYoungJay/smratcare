<template>
  <!-- 数字孪生运营大屏：全部数字来自业务接口；没有数据源的块显示「未接入」，加载失败显示「加载失败」，从不补 0 -->
  <div ref="root" class="scr" :class="{ full: isFull }">
    <header class="scr-head">
      <div class="scr-brand">
        <img :src="logoUrl" alt="" />
        <h1>弋阳龟峰颐养中心<span>｜</span>数字孪生运营大屏</h1>
      </div>
      <nav class="scr-modes" aria-label="三维着色">
        <button v-for="m in MODES" :key="m.key" type="button" :class="{ active: overlayMode === m.key }" :aria-pressed="overlayMode === m.key"
          @click="setOverlay(m.key)">{{ m.label }}</button>
      </nav>
      <div class="scr-meta">
        <span class="scr-conn" :class="connState">{{ connState === 'ok' ? '数据已连接' : connState === 'fail' ? '数据刷新失败' : '连接中' }}</span>
        <span class="scr-clock">{{ clock }}</span>
        <button type="button" class="scr-icon" :aria-label="isFull ? '退出全屏' : '全屏'" @click="toggleFull">{{ isFull ? '退出全屏' : '全屏' }}</button>
      </div>
    </header>

    <main class="scr-grid">
      <!-- 左列 -->
      <section class="scr-col">
        <article class="scr-panel">
          <h2>园区运营总览</h2>
          <div class="scr-kpis">
            <div><small>在住长者</small><strong>{{ num(cockpit?.residentCount) }}<em>人</em></strong></div>
            <div><small>员工人数</small><strong>{{ num(cockpit?.staffCount) }}<em>人</em></strong></div>
            <div><small>总床位</small><strong>{{ num(cockpit?.totalBeds) }}<em>张</em></strong></div>
            <div><small>床位使用率</small><strong>{{ pct(cockpit?.occupancyRate) }}</strong></div>
          </div>
          <p class="scr-src">{{ cockpitError || `经营驾驶舱 · 统计日 ${cockpit?.statDate || '—'}` }}</p>
        </article>

        <article class="scr-panel">
          <h2>长者状态分布 <small>护理等级</small></h2>
          <div v-if="ov" class="scr-donut-row">
            <div class="scr-donut" :style="{ background: donutBg(levelSlices) }"><span>{{ ov.careLevels.total }}<small>在住</small></span></div>
            <ul class="scr-legend">
              <li v-for="(s, i) in levelSlices" :key="s.name"><i :style="{ background: PALETTE[i % PALETTE.length] }" />{{ s.name }}<b>{{ s.count }}人</b><em>{{ share(s.count, ov.careLevels.total) }}</em></li>
              <li v-if="!levelSlices.length" class="scr-empty">暂无在住长者</li>
            </ul>
          </div>
          <p v-else class="scr-empty">{{ ovState }}</p>
        </article>

        <article class="scr-panel scr-grow">
          <h2>实时预警与告警 <small v-if="ov">未处理 {{ ov.alerts.openCount }}</small></h2>
          <ul v-if="ov" class="scr-alerts">
            <li v-for="(a, i) in ov.alerts.items" :key="i" :class="a.type">
              <b>{{ a.typeLabel }}</b><span>{{ a.title }}<template v-if="a.location"> · {{ a.location }}</template></span>
              <time>{{ a.time ? dayjs(a.time).format('MM-DD HH:mm') : '—' }}</time>
            </li>
            <li v-if="!ov.alerts.items.length" class="scr-empty">当前没有未处理的告警</li>
          </ul>
          <p v-else class="scr-empty">{{ ovState }}</p>
        </article>

        <article class="scr-panel">
          <h2>今日重点任务</h2>
          <ul v-if="ov" class="scr-tasks">
            <li v-for="t in ov.tasks.items" :key="t.key">
              <span>{{ t.label }}</span>
              <div class="scr-bar"><i :style="{ width: t.total ? `${Math.min(100, (t.done * 100) / t.total)}%` : '0' }" /></div>
              <b>{{ t.total == null ? `${t.done} 次` : `${t.done}/${t.total}` }}</b>
            </li>
          </ul>
          <p v-else class="scr-empty">{{ ovState }}</p>
        </article>
      </section>

      <!-- 中：三维园区 + 楼栋内部下钻 -->
      <section class="scr-stage">
        <div ref="canvasHost" class="scr-canvas" />
        <div v-if="sceneState !== 'ready'" class="scr-stage-msg">{{ sceneState === 'loading' ? '正在加载三维场景…' : sceneError }}</div>
        <div v-if="overlayMode !== 'campus' && sceneState === 'ready' && !drill" class="scr-stage-legend">
          <b>{{ overlayMode === 'occupancy' ? '楼栋入住率' : '未完成报修' }}</b>
          <span v-for="l in OVERLAY_LEGEND[overlayMode]" :key="l.label"><i :style="{ background: hex(l.color) }" />{{ l.label }}</span>
        </div>
        <p v-if="sceneState === 'ready' && !drill" class="scr-stage-note">楼栋来自主数据，位置为示意布局；点击楼栋查看楼层、房间与床位</p>
        <BuildingDrill v-if="drill" :key="drill.assetId" :asset-id="drill.assetId" :building-name="drill.buildingName!"
          :can-open-elder="canOpenElder" @close="closeDrill" />
      </section>

      <!-- 右列 -->
      <section class="scr-col">
        <article class="scr-panel scr-grow">
          <h2>楼栋与房间 <small>点击进入</small></h2>
          <ul class="scr-buildings">
            <li v-for="a in realBuildings" :key="a.assetId">
              <button type="button" :class="{ active: drill?.assetId === a.assetId }" @click="openDrill(a)">
                <strong>{{ a.buildingName }}</strong>
                <span>{{ a.floorCount ?? '—' }} 层 · {{ a.roomCount ?? '—' }} 间</span>
                <em>{{ occupancyText(a) }}</em>
              </button>
            </li>
            <li v-if="sceneState === 'ready' && !realBuildings.length" class="scr-empty">楼栋主数据里还没有楼栋</li>
          </ul>
        </article>

        <article class="scr-panel">
          <h2>安全消防</h2>
          <div v-if="fire?.metrics.length" class="scr-metrics">
            <div v-for="m in fire.metrics.slice(0, 4)" :key="m.key" :class="m.tone"><small>{{ m.label }}</small><strong>{{ num(m.value) }}<em>{{ m.unit }}</em></strong></div>
          </div>
          <p v-else class="scr-empty">{{ fireState }}</p>
        </article>

        <article class="scr-panel">
          <h2>视频点位 <small>视频流未接入</small></h2>
          <ul v-if="ov" class="scr-cams">
            <li v-for="(c, i) in ov.cameras.items" :key="i" :class="{ off: !c.enabled }"><b>{{ c.name }}</b><span>{{ c.location || '位置未登记' }}</span><em>{{ c.enabled ? '启用' : '停用' }}</em></li>
            <li v-if="!ov.cameras.items.length" class="scr-empty">摄像头台账为空</li>
          </ul>
          <p v-else class="scr-empty">{{ ovState }}</p>
          <p v-if="ov" class="scr-src">摄像头 {{ ov.cameras.total }} 路 · 启用 {{ ov.cameras.enabled }} 路 · 大屏不播放画面</p>
        </article>
      </section>

      <!-- 底部指标 -->
      <section class="scr-bottom">
        <article class="scr-panel">
          <h2>运营概览 <small>楼栋入住率</small></h2>
          <ul class="scr-rank">
            <li v-for="a in realBuildings" :key="a.assetId">
              <span>{{ a.buildingName }}</span>
              <div class="scr-bar"><i :style="{ width: `${occOf(a) ?? 0}%` }" /></div>
              <b>{{ occOf(a) == null ? '无床位' : `${occOf(a)}%` }}</b>
            </li>
            <li v-if="!realBuildings.length" class="scr-empty">{{ sceneState === 'ready' ? '暂无楼栋' : '—' }}</li>
          </ul>
        </article>

        <article class="scr-panel">
          <h2>服务工单统计 <small>本月报修</small></h2>
          <div v-if="ov" class="scr-donut-row">
            <div class="scr-donut" :style="{ background: donutBg(orderSlices) }"><span>{{ ov.workOrders.total }}<small>工单</small></span></div>
            <ul class="scr-legend">
              <li v-for="(s, i) in orderSlices" :key="s.name"><i :style="{ background: ORDER_COLORS[i] }" />{{ s.name }}<b>{{ s.count }}</b><em>{{ share(s.count, ov.workOrders.total) }}</em></li>
            </ul>
          </div>
          <p v-else class="scr-empty">{{ ovState }}</p>
        </article>

        <article class="scr-panel">
          <h2>健康监测 <small>今日</small></h2>
          <template v-if="ov">
            <div class="scr-metrics scr-metrics--3">
              <div><small>测量次数</small><strong>{{ ov.health.measured }}</strong></div>
              <div :class="{ danger: ov.health.abnormal > 0 }"><small>异常次数</small><strong>{{ ov.health.abnormal }}</strong></div>
              <div :class="{ warn: ov.health.abnormalElders > 0 }"><small>异常长者</small><strong>{{ ov.health.abnormalElders }}<em>人</em></strong></div>
            </div>
            <svg class="scr-spark" viewBox="0 0 240 48" preserveAspectRatio="none" role="img" aria-label="今日每小时测量与异常次数">
              <polyline :points="spark(ov.health.hourlyMeasured)" class="m" />
              <polyline :points="spark(ov.health.hourlyAbnormal, ov.health.hourlyMeasured)" class="a" />
            </svg>
            <p class="scr-src"><i class="m" />测量 <i class="a" />异常 · 0–23 时</p>
          </template>
          <p v-else class="scr-empty">{{ ovState }}</p>
        </article>

        <article class="scr-panel">
          <h2>设备运维</h2>
          <div v-if="facility?.metrics.length" class="scr-metrics">
            <div v-for="m in facility.metrics.slice(0, 6)" :key="m.key" :class="m.tone"><small>{{ m.label }}</small><strong>{{ num(m.value) }}<em>{{ m.unit }}</em></strong></div>
          </div>
          <p v-else class="scr-empty">{{ facilityState }}</p>
        </article>

        <article class="scr-panel">
          <h2>能耗 <small>{{ ov?.energy.connected ? `抄表 ${ov.energy.month}` : '' }}</small></h2>
          <template v-if="ov?.energy.connected">
            <div class="scr-metrics">
              <div><small>用电量</small><strong>{{ num(ov.energy.usageKwh) }}<em>kWh</em></strong></div>
              <div><small>电费</small><strong>{{ num(ov.energy.feeAmount) }}<em>元</em></strong></div>
              <div class="muted"><small>用水</small><strong>未接入</strong></div>
              <div class="muted"><small>室内温湿度</small><strong>未接入</strong></div>
            </div>
          </template>
          <div v-else-if="ov" class="scr-metrics">
            <div class="muted"><small>用电</small><strong>未接入</strong></div>
            <div class="muted"><small>用水</small><strong>未接入</strong></div>
          </div>
          <p v-else class="scr-empty">{{ ovState }}</p>
        </article>
      </section>
    </main>
    <footer class="scr-foot">数据更新 {{ updatedText }} · 每 60 秒刷新 · 业务办理请回到原业务模块 · 三维位置为示意，非测绘</footer>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, shallowRef } from 'vue'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import logoUrl from '../../assets/guifeng-logo.png'
import { getExecutiveOverview, type CockpitOverviewResponse } from '../../api/cockpit'
import { getPagePermissions, getPermissions, getRoles } from '../../utils/auth'
import { resolveRouteAccess } from '../../utils/routeAccess'
import {
  getTwinAssets, getTwinOverlay, getTwinScene, getTwinScreenOverview, getTwinSummary,
  type TwinAsset, type TwinOverlayItem, type TwinScreenOverview, type TwinSummary
} from '../../api/digitalTwin'
import { OVERLAY_LEGEND, overlayColor, placeBuildings } from './twinModel'
import { createTwinScene, type TwinSceneHandle } from './twinScene'
import BuildingDrill from './BuildingDrill.vue'

type OverlayKey = 'campus' | 'occupancy' | 'facility'
const MODES: Array<{ key: OverlayKey; label: string }> = [
  { key: 'campus', label: '园区三维' }, { key: 'occupancy', label: '入住着色' }, { key: 'facility', label: '报修着色' }
]
const PALETTE = ['#36c5d0', '#f2b84b', '#7aa2ff', '#ef6b73', '#8bd17c', '#c792ea', '#9aa5b1']
const ORDER_COLORS = ['#36c5d0', '#f2b84b', '#ef6b73']
const CAMPUS_ID = 'GF-CAMPUS-001'

const router = useRouter()
const root = ref<HTMLElement | null>(null)
const canOpenElder = resolveRouteAccess(router, getRoles(), '/elder/detail/0', getPagePermissions(), getPermissions()).canAccess
const canvasHost = ref<HTMLElement | null>(null)
const handle = shallowRef<TwinSceneHandle | null>(null)
const sceneState = ref<'loading' | 'ready' | 'error'>('loading')
const sceneError = ref('')
const assets = ref<TwinAsset[]>([])
const occupancy = ref<TwinOverlayItem[]>([])
const facilityOverlay = ref<TwinOverlayItem[]>([])
const overlayMode = ref<OverlayKey>('campus')
const drill = ref<TwinAsset | null>(null)

const ov = ref<TwinScreenOverview | null>(null)
const ovState = ref('加载中…')
const cockpit = ref<CockpitOverviewResponse | null>(null)
const cockpitError = ref('')
const fire = ref<TwinSummary | null>(null)
const fireState = ref('加载中…')
const facility = ref<TwinSummary | null>(null)
const facilityState = ref('加载中…')
const connState = ref<'pending' | 'ok' | 'fail'>('pending')
const updatedText = ref('—')
const clock = ref('')
const isFull = ref(false)
let timer: number | undefined
let clockTimer: number | undefined
let disposed = false

const realBuildings = computed(() => assets.value.filter((a) => a.assetType === 'BUILDING' && a.buildingName))
const levelSlices = computed(() => ov.value?.careLevels.items ?? [])
const orderSlices = computed(() => ov.value
  ? [{ name: '已完成', count: ov.value.workOrders.completed }, { name: '处理中', count: ov.value.workOrders.processing }, { name: '待处理', count: ov.value.workOrders.open }]
  : [])

function num(v: unknown) {
  return v === null || v === undefined || v === '' ? '—' : String(v)
}
function pct(v: number | null | undefined) {
  return v === null || v === undefined ? '—' : `${Number(v).toFixed(1)}%`
}
function share(n: number, total: number) {
  return total ? `${((n * 100) / total).toFixed(1)}%` : '—'
}
function hex(c: number) {
  return '#' + c.toString(16).padStart(6, '0')
}
/** 环形图：按份额拼 conic-gradient；总数为 0 时画灰环，不伪造比例 */
function donutBg(slices: Array<{ count: number }>) {
  const total = slices.reduce((s, x) => s + x.count, 0)
  if (!total) return 'conic-gradient(#2a3a4a 0 100%)'
  let acc = 0
  const colors = slices === orderSlices.value ? ORDER_COLORS : PALETTE
  const stops = slices.map((s, i) => {
    const from = (acc / total) * 100
    acc += s.count
    return `${colors[i % colors.length]} ${from}% ${(acc / total) * 100}%`
  })
  return `conic-gradient(${stops.join(', ')})`
}
/** 折线：24 个小时点；异常线与测量线用同一纵轴比例 */
function spark(values: number[], scaleBy?: number[]) {
  const max = Math.max(1, ...(scaleBy ?? values))
  return values.map((v, i) => `${(i / 23) * 240},${46 - (v / max) * 42}`).join(' ')
}
function occOf(a: TwinAsset) {
  return occupancy.value.find((o) => o.assetId === a.assetId)?.value ?? null
}
function occupancyText(a: TwinAsset) {
  return occupancy.value.find((o) => o.assetId === a.assetId)?.text ?? '—'
}

function openDrill(a: TwinAsset) {
  drill.value = a
  if (a.modelNodeKey) handle.value?.highlight(a.modelNodeKey)
}
function closeDrill() {
  drill.value = null
  handle.value?.highlight(null)
  handle.value?.setView('reset')
}
function setOverlay(key: OverlayKey) {
  overlayMode.value = key
  applyOverlay()
}
function applyOverlay() {
  const h = handle.value
  if (!h) return
  if (overlayMode.value === 'campus') {
    h.setOverlay(null)
    h.setLabelSub(Object.fromEntries(realBuildings.value.map((a) => [a.modelNodeKey!, occupancyText(a)])))
    return
  }
  const mode = overlayMode.value
  const items = mode === 'occupancy' ? occupancy.value : facilityOverlay.value
  const colors: Record<string, number> = {}
  const subs: Record<string, string> = {}
  for (const a of realBuildings.value) {
    const o = items.find((x) => x.assetId === a.assetId)
    colors[a.modelNodeKey!] = overlayColor(mode, o?.value ?? null)
    subs[a.modelNodeKey!] = o?.text ?? '无数据'
  }
  h.setOverlay(colors)
  h.setLabelSub(subs)
}

async function loadScene() {
  try {
    const [scene, list] = await Promise.all([getTwinScene(), getTwinAssets()])
    assets.value = list
    await nextTick()
    if (!canvasHost.value || disposed) return
    const byKey = new Map(list.filter((a) => a.modelNodeKey).map((a) => [a.modelNodeKey!, a]))
    const buildings = placeBuildings(list).map((p) => {
      const a = byKey.get(p.key)!
      return { ...p, title: a.buildingName || a.displayName, sub: a.buildingName ? `${a.floorCount ?? 0} 层 · ${a.roomCount ?? 0} 间` : '' }
    })
    const created = await createTwinScene(canvasHost.value, {
      modelUrl: scene.modelUrl,
      buildings,
      onSelect: (key) => {
        const a = key ? list.find((x) => x.modelNodeKey === key && x.buildingName) : null
        if (a) openDrill(a)
      }
    })
    if (disposed) { created.dispose(); return }
    handle.value = created
    sceneState.value = 'ready'
    applyOverlay()
  } catch (e) {
    sceneState.value = 'error'
    sceneError.value = (e as Error)?.message === 'WEBGL_UNSUPPORTED' ? '当前浏览器不支持三维显示，请用 Chrome / Edge 打开' : '三维场景加载失败，请刷新重试'
  }
}

/** 每块独立请求：一块失败只影响自己，显示「加载失败」而不是 0 */
async function loadData() {
  const results = await Promise.allSettled([
    getTwinScreenOverview(),
    getExecutiveOverview({ trendDays: 7 }, { silent403: true }),
    getTwinSummary(CAMPUS_ID, 'fire'),
    getTwinSummary(CAMPUS_ID, 'facility'),
    getTwinOverlay('occupancy'),
    getTwinOverlay('facility')
  ])
  const [o, c, f, fa, occ, fov] = results
  if (o.status === 'fulfilled') { ov.value = o.value } else if (!ov.value) ovState.value = failText(o.reason)
  if (c.status === 'fulfilled') { cockpit.value = c.value; cockpitError.value = '' } else if (!cockpit.value) cockpitError.value = `经营驾驶舱：${failText(c.reason)}`
  if (f.status === 'fulfilled') fire.value = f.value
  else if (!fire.value) fireState.value = failText(f.reason)
  if (fa.status === 'fulfilled') facility.value = fa.value
  else if (!facility.value) facilityState.value = failText(fa.reason)
  if (occ.status === 'fulfilled') occupancy.value = occ.value
  if (fov.status === 'fulfilled') facilityOverlay.value = fov.value
  if (fire.value && !fire.value.metrics.length) fireState.value = fire.value.message || '暂无数据'
  if (facility.value && !facility.value.metrics.length) facilityState.value = facility.value.message || '暂无数据'
  // 无权查看（403）是岗位口径，不算连接失败
  connState.value = results.every((r) => r.status === 'fulfilled' || statusOf(r.reason) === 403) ? 'ok' : 'fail'
  updatedText.value = dayjs().format('HH:mm:ss')
  applyOverlay()
}

function statusOf(reason: unknown) {
  return Number((reason as any)?.response?.status || 0)
}
/** 403 是岗位没有这块数据的权限，和接口故障分开提示 */
function failText(reason: unknown) {
  return statusOf(reason) === 403 ? '无权查看' : '加载失败'
}
function tick() {
  clock.value = dayjs().format('YYYY-MM-DD HH:mm:ss')
}
function toggleFull() {
  if (document.fullscreenElement) document.exitFullscreen?.()
  else root.value?.requestFullscreen?.()
}
function onFullChange() {
  isFull.value = Boolean(document.fullscreenElement)
}

onMounted(() => {
  tick()
  clockTimer = window.setInterval(tick, 1000)
  document.addEventListener('fullscreenchange', onFullChange)
  loadScene()
  loadData()
  timer = window.setInterval(loadData, 60_000)
})
onBeforeUnmount(() => {
  disposed = true
  window.clearInterval(timer)
  window.clearInterval(clockTimer)
  document.removeEventListener('fullscreenchange', onFullChange)
  handle.value?.dispose()
})
</script>

<style scoped>
.scr {
  --bg: #06131f; --panel: rgba(10, 34, 52, 0.82); --line: rgba(64, 200, 220, 0.28); --text: #e6f4f7; --muted: #8fb2bd;
  --accent: #36c5d0; --warn: #f2b84b; --danger: #ef6b73;
  min-height: calc(100vh - 120px); border-radius: 12px; display: flex; flex-direction: column; background: radial-gradient(circle at 50% 0%, #0d2b40, var(--bg) 70%);
  color: var(--text); font-size: 13px;
}
.scr.full { min-height: 100vh; border-radius: 0; overflow: auto; }
.scr-head { display: flex; align-items: center; gap: 16px; padding: 10px 18px; border-bottom: 1px solid var(--line); flex-wrap: wrap; }
.scr-brand { display: flex; align-items: center; gap: 10px; min-width: 0; }
.scr-brand img { width: 34px; height: 34px; }
.scr-brand h1 { margin: 0; font-size: 22px; font-weight: 700; letter-spacing: 1px; color: #fff; white-space: nowrap; }
.scr-brand h1 span { color: var(--accent); margin: 0 4px; }
.scr-modes { display: flex; gap: 6px; margin: 0 auto; }
.scr-modes button, .scr-icon {
  background: rgba(54, 197, 208, 0.08); border: 1px solid var(--line); color: var(--text); padding: 6px 14px; border-radius: 6px; cursor: pointer; font: inherit;
}
.scr-modes button.active { background: var(--accent); color: #04202a; font-weight: 700; }
.scr-modes button:focus-visible, .scr-icon:focus-visible, .scr-buildings button:focus-visible { outline: 2px solid #fff; outline-offset: 2px; }
.scr-meta { display: flex; align-items: center; gap: 10px; }
.scr-clock { font-variant-numeric: tabular-nums; color: #fff; }
.scr-conn::before { content: ''; display: inline-block; width: 8px; height: 8px; border-radius: 50%; margin-right: 6px; background: var(--muted); }
.scr-conn.ok::before { background: #4cd08a; }
.scr-conn.fail { color: var(--danger); }
.scr-conn.fail::before { background: var(--danger); }
.scr-grid {
  flex: 1; display: grid; gap: 12px; padding: 12px 16px; min-height: 0;
  grid-template-columns: minmax(280px, 22%) 1fr minmax(280px, 22%); grid-template-rows: 1fr auto;
}
.scr-col { display: flex; flex-direction: column; gap: 12px; min-height: 0; }
.scr-panel { background: var(--panel); border: 1px solid var(--line); border-radius: 10px; padding: 10px 14px; min-width: 0; }
.scr-grow { flex: 1; min-height: 0; overflow: auto; }
.scr-panel h2 { margin: 0 0 8px; font-size: 15px; color: #fff; border-left: 3px solid var(--accent); padding-left: 8px; }
.scr-panel h2 small { font-weight: 400; color: var(--muted); margin-left: 6px; font-size: 12px; }
.scr-kpis, .scr-metrics { display: grid; grid-template-columns: repeat(2, 1fr); gap: 8px; }
.scr-metrics--3 { grid-template-columns: repeat(3, 1fr); }
.scr-kpis div, .scr-metrics div { background: rgba(54, 197, 208, 0.06); border-radius: 8px; padding: 6px 10px; }
.scr-kpis small, .scr-metrics small { display: block; color: var(--muted); font-size: 12px; }
.scr-kpis strong { font-size: 26px; color: #fff; font-variant-numeric: tabular-nums; }
.scr-metrics strong { font-size: 20px; color: #fff; font-variant-numeric: tabular-nums; }
.scr-kpis em, .scr-metrics em { font-style: normal; font-size: 12px; color: var(--muted); margin-left: 3px; }
.scr-metrics .warn strong { color: var(--warn); }
.scr-metrics .danger strong { color: var(--danger); }
.scr-metrics .muted strong { color: var(--muted); font-size: 15px; }
.scr-src { margin: 6px 0 0; color: var(--muted); font-size: 11px; }
.scr-src i { display: inline-block; width: 10px; height: 2px; vertical-align: middle; margin: 0 3px 0 6px; }
.scr-src i.m { background: var(--accent); }
.scr-src i.a { background: var(--danger); }
.scr-empty { color: var(--muted); margin: 4px 0; list-style: none; }
.scr-donut-row { display: flex; align-items: center; gap: 14px; }
.scr-donut { flex: 0 0 104px; height: 104px; border-radius: 50%; display: grid; place-items: center; position: relative; }
.scr-donut::after { content: ''; position: absolute; inset: 16px; border-radius: 50%; background: #0a2234; }
.scr-donut span { position: relative; z-index: 1; font-size: 22px; font-weight: 700; color: #fff; text-align: center; line-height: 1.1; }
.scr-donut small { display: block; font-size: 11px; font-weight: 400; color: var(--muted); }
.scr-legend { list-style: none; margin: 0; padding: 0; flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 4px; }
.scr-legend li { display: grid; grid-template-columns: 12px 1fr auto auto; gap: 6px; align-items: center; }
.scr-legend i { width: 10px; height: 10px; border-radius: 2px; }
.scr-legend b { font-weight: 600; color: #fff; }
.scr-legend em { font-style: normal; color: var(--muted); min-width: 44px; text-align: right; }
.scr-alerts, .scr-tasks, .scr-cams, .scr-buildings, .scr-rank { list-style: none; margin: 0; padding: 0; display: flex; flex-direction: column; gap: 6px; }
.scr-alerts li { display: grid; grid-template-columns: auto 1fr auto; gap: 8px; align-items: baseline; }
.scr-alerts b { font-size: 12px; padding: 0 6px; border-radius: 4px; background: rgba(242, 184, 75, 0.18); color: var(--warn); }
.scr-alerts li.FIRE b, .scr-alerts li.HEALTH b { background: rgba(239, 107, 115, 0.18); color: var(--danger); }
.scr-alerts span { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.scr-alerts time { color: var(--muted); font-size: 12px; font-variant-numeric: tabular-nums; }
.scr-tasks li, .scr-rank li { display: grid; grid-template-columns: 92px 1fr 64px; gap: 8px; align-items: center; }
.scr-tasks b, .scr-rank b { text-align: right; font-variant-numeric: tabular-nums; }
.scr-rank span { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.scr-bar { height: 8px; border-radius: 4px; background: rgba(255, 255, 255, 0.08); overflow: hidden; }
.scr-bar i { display: block; height: 100%; background: linear-gradient(90deg, #1b8fa0, var(--accent)); }
.scr-buildings button {
  width: 100%; display: grid; grid-template-columns: 1fr auto; gap: 2px 8px; text-align: left; padding: 8px 10px;
  background: rgba(54, 197, 208, 0.06); border: 1px solid transparent; border-radius: 8px; color: inherit; cursor: pointer; font: inherit;
}
.scr-buildings button:hover, .scr-buildings button.active { border-color: var(--accent); }
.scr-buildings strong { color: #fff; }
.scr-buildings span { color: var(--muted); font-size: 12px; grid-column: 1; }
.scr-buildings em { font-style: normal; color: var(--accent); grid-column: 2; grid-row: 1 / 3; align-self: center; }
.scr-cams li { display: grid; grid-template-columns: 1fr auto; gap: 0 8px; }
.scr-cams span { color: var(--muted); font-size: 12px; }
.scr-cams em { font-style: normal; color: #4cd08a; grid-column: 2; grid-row: 1 / 3; align-self: center; }
.scr-cams li.off em { color: var(--muted); }
.scr-spark { width: 100%; height: 48px; margin-top: 8px; }
.scr-spark polyline { fill: none; stroke-width: 1.6; vector-effect: non-scaling-stroke; }
.scr-spark .m { stroke: var(--accent); }
.scr-spark .a { stroke: var(--danger); }
.scr-stage { position: relative; border: 1px solid var(--line); border-radius: 12px; overflow: hidden; min-height: 420px; }
.scr-canvas { position: absolute; inset: 0; }
.scr-canvas :deep(.twin-label-layer) { position: absolute; inset: 0; pointer-events: none; }
.scr-canvas :deep(.twin-label) {
  display: flex; flex-direction: column; align-items: center; background: rgba(6, 30, 46, 0.86); color: #cfe9ee;
  font-size: 11px; padding: 3px 8px; border-radius: 6px; border: 1px solid var(--accent); white-space: nowrap;
}
.scr-canvas :deep(.twin-label b) { color: #fff; font-size: 13px; }
.scr-canvas :deep(.twin-label span:empty) { display: none; }
.scr-stage-msg { position: absolute; inset: 0; display: grid; place-items: center; color: var(--muted); }
.scr-stage-legend {
  position: absolute; left: 12px; top: 12px; background: rgba(6, 30, 46, 0.86); border: 1px solid var(--line); border-radius: 8px;
  padding: 6px 10px; display: flex; flex-direction: column; gap: 3px; font-size: 12px;
}
.scr-stage-legend i { display: inline-block; width: 10px; height: 10px; border-radius: 2px; margin-right: 6px; }
.scr-stage-note {
  position: absolute; left: 50%; bottom: 10px; transform: translateX(-50%); margin: 0; background: rgba(6, 30, 46, 0.8);
  padding: 4px 12px; border-radius: 6px; color: #cfe9ee; font-size: 12px; white-space: nowrap;
}
.scr-bottom { grid-column: 1 / -1; display: grid; grid-template-columns: repeat(5, minmax(0, 1fr)); gap: 12px; }
.scr-foot { padding: 4px 16px 10px; color: var(--muted); font-size: 11px; text-align: center; }
@media (max-width: 1280px) {
  .scr-grid { grid-template-columns: 1fr 1fr; }
  .scr-stage { grid-column: 1 / -1; grid-row: 1; min-height: 460px; }
  .scr-bottom { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}
@media (max-width: 768px) {
  .scr-grid { grid-template-columns: 1fr; padding: 10px; }
  .scr-bottom { grid-template-columns: 1fr; }
  .scr-brand h1 { font-size: 17px; white-space: normal; }
  .scr-stage-note { white-space: normal; width: 90%; text-align: center; }
}
@media (min-width: 3000px) {
  .scr { font-size: 22px; }
  .scr-panel h2 { font-size: 26px; }
  .scr-kpis strong { font-size: 48px; }
  .scr-metrics strong { font-size: 36px; }
}
</style>
