<template>
  <!-- 楼栋内部三维：盖在园区场景上；楼栋 → 楼层 → 房间都在这个画面里切换 -->
  <div class="bd" role="region" :aria-label="`${buildingName} 楼栋内部`">
    <div ref="host" class="bd-canvas" />

    <div class="bd-top">
      <nav class="bd-crumb" aria-label="位置">
        <button type="button" @click="emit('close')">园区</button>
        <span>/</span>
        <button type="button" :disabled="!floor" @click="goBuilding">{{ buildingName }}</button>
        <template v-if="floor"><span>/</span><button type="button" :disabled="!room" @click="goFloor">{{ floor.floorNo }}</button></template>
        <template v-if="room"><span>/</span><strong>{{ room.roomNo }}</strong></template>
      </nav>
      <div class="bd-actions">
        <a-button size="small" @click="goUp">{{ room || floor ? '返回上一级' : '返回园区' }}</a-button>
        <a-button size="small" :disabled="!sceneHandle" @click="sceneHandle?.refocus()">定位镜头</a-button>
      </div>
    </div>

    <p class="bd-note">楼层、房间、床位来自主数据；房间体块按房号顺序排布，为示意位置，非实测</p>

    <section class="bd-card" :class="{ wide: Boolean(room) }">
      <a-skeleton v-if="loading" active :paragraph="{ rows: 3 }" />
      <a-result v-else-if="error" :status="error.status" :sub-title="error.text" class="bd-result">
        <template #extra><a-button size="small" type="primary" @click="load">重试</a-button></template>
      </a-result>

      <template v-else-if="interior && !floor">
        <div class="bd-card-title">{{ buildingName }} · {{ interior.floors.filter((f) => f.key !== 'unassigned').length }} 层</div>
        <p class="bd-muted">点三维里的楼层，或在下面选择。</p>
        <ul class="bd-list">
          <li v-for="f in floorsTopDown" :key="f.key">
            <button type="button" @click="openFloor(f.key)">
              <strong>{{ f.floorNo }}</strong><span>{{ f.roomCount }} 间 · {{ f.occupiedBeds }}/{{ f.bedCount }} 床</span>
            </button>
          </li>
          <li v-if="!interior.floors.length" class="bd-muted">楼栋主数据里还没有楼层和房间记录。</li>
        </ul>
      </template>

      <template v-else-if="floor && !room">
        <div class="bd-card-title">{{ floor.floorNo }} · {{ floor.roomCount }} 间 · 入住 {{ floor.occupiedBeds }}/{{ floor.bedCount }} 床</div>
        <div class="bd-legend">
          <span><i class="full" />住满</span><span><i class="partial" />部分入住</span><span><i class="vacant" />空房</span><span><i class="none" />无床位</span>
        </div>
        <ul class="bd-list bd-rooms">
          <li v-for="r in floor.rooms" :key="r.roomId">
            <button type="button" @click="openRoom(r.roomId)">
              <strong>{{ r.roomNo }}</strong><span>{{ r.occupiedBeds }}/{{ r.bedCount }} 床</span>
            </button>
          </li>
          <li v-if="!floor.rooms.length" class="bd-muted">该楼层暂无房间记录。</li>
        </ul>
      </template>

      <template v-else-if="room">
        <div class="bd-card-title">
          {{ room.roomNo }} 房间<small v-if="room.roomType"> · {{ room.roomType }}</small>
          <span class="bd-muted"> · 入住 {{ room.occupiedBeds }}/{{ room.bedCount }} 床</span>
        </div>
        <p v-if="!interior?.canViewElders" class="bd-muted">当前岗位只显示床位状态，不显示长者身份。</p>
        <ul class="bd-beds">
          <li v-for="b in room.beds" :key="b.bedId">
            <div class="bd-bed-head">
              <strong>{{ b.bedNo }} 床</strong>
              <a-tag :color="STATUS_COLOR[b.status] || 'default'">{{ STATUS_LABEL[b.status] || '未知状态' }}</a-tag>
            </div>
            <template v-if="b.elder">
              <div class="bd-elder">{{ b.elder.name }}</div>
              <div class="bd-muted">
                {{ genderText(b.elder.gender) }}<template v-if="b.elder.age != null"> · {{ b.elder.age }} 岁</template>
                <template v-if="b.elder.careLevel"> · {{ b.elder.careLevel }}</template>
                <template v-if="b.elder.admissionDate"> · {{ b.elder.admissionDate }} 入住</template>
              </div>
              <a-button v-if="canOpenElder" size="small" type="link" class="bd-link" @click="router.push(`/elder/detail/${b.elder.elderId}`)">打开长者档案</a-button>
            </template>
          </li>
          <li v-if="!room.beds.length" class="bd-muted">这个房间还没有配置床位。</li>
        </ul>
      </template>
      <p v-if="interior && !loading && !error" class="bd-source">来源：楼栋 / 楼层 / 房间 / 床位主数据 · 查询于 {{ dayjs(interior.updatedAt).format('HH:mm:ss') }}</p>
    </section>

    <div v-if="noWebgl" class="bd-nowebgl">当前浏览器不支持三维显示，可用左下角列表浏览楼层与房间。</div>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, shallowRef, watch } from 'vue'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { getTwinInterior, type TwinInterior } from '../../api/digitalTwin'
import { createInteriorScene, type InteriorPick, type InteriorSceneHandle } from './interiorScene'

const props = defineProps<{ assetId: string; buildingName: string; canOpenElder: boolean }>()
const emit = defineEmits<{ (e: 'close'): void }>()
const router = useRouter()

const STATUS_LABEL: Record<number, string> = { 0: '停用', 1: '空闲', 2: '已入住', 3: '维修' }
const STATUS_COLOR: Record<number, string> = { 0: 'default', 1: 'green', 2: 'blue', 3: 'orange' }

const host = ref<HTMLElement | null>(null)
const sceneHandle = shallowRef<InteriorSceneHandle | null>(null)
const noWebgl = ref(false)
const interior = ref<TwinInterior | null>(null)
const loading = ref(false)
const error = ref<{ status: '403' | '404' | 'error'; text: string } | null>(null)
const floorKey = ref<string | null>(null)
const roomId = ref<string | null>(null)
let requestToken = 0

const floor = computed(() => interior.value?.floors.find((f) => f.key === floorKey.value) || null)
const room = computed(() => floor.value?.rooms.find((r) => r.roomId === roomId.value) || null)
/** 列表里楼层从高到低，和三维叠放一致；「待归属」放最后 */
const floorsTopDown = computed(() => {
  const floors = interior.value?.floors || []
  return [...floors.filter((f) => f.key !== 'unassigned').reverse(), ...floors.filter((f) => f.key === 'unassigned')]
})

async function load() {
  const token = ++requestToken
  loading.value = true
  error.value = null
  interior.value = null
  floorKey.value = null
  roomId.value = null
  try {
    const data = await getTwinInterior(props.assetId)
    if (token !== requestToken) return // 已切到别的楼栋，丢弃旧响应
    interior.value = data
    render()
  } catch (e: any) {
    if (token !== requestToken) return
    const status = Number(e?.response?.status || 0)
    error.value = status === 403 ? { status: '403', text: '当前岗位无权查看楼栋内部。' }
      : status === 404 ? { status: '404', text: '该楼栋数据已不存在，请返回园区刷新。' }
      : { status: 'error', text: '楼层数据加载失败，请检查网络后重试。' }
  } finally {
    if (token === requestToken) loading.value = false
  }
}

/** 按当前层级重画三维 */
function render() {
  const h = sceneHandle.value
  if (!h || !interior.value) return
  if (!floor.value) h.showBuilding(interior.value)
  else {
    h.showFloor(floor.value, roomId.value)
    h.showRoom(room.value)
  }
}
function openFloor(key: string) {
  floorKey.value = key
  roomId.value = null
  render()
}
function openRoom(id: string) {
  roomId.value = id
  render()
}
function goBuilding() {
  floorKey.value = null
  roomId.value = null
  render()
}
function goFloor() {
  roomId.value = null
  render()
}
function goUp() {
  if (room.value) goFloor()
  else if (floor.value) goBuilding()
  else emit('close')
}
function onPick(p: InteriorPick) {
  if (p.kind === 'floor') openFloor(p.key)
  else openRoom(p.key)
}
function genderText(g?: number | null) {
  return g === 1 ? '男' : g === 2 ? '女' : '性别未填'
}

onMounted(() => {
  try {
    if (host.value) sceneHandle.value = createInteriorScene(host.value, onPick)
  } catch {
    noWebgl.value = true
  }
  load()
})
watch(() => props.assetId, load)
onBeforeUnmount(() => {
  requestToken++
  sceneHandle.value?.dispose()
})
</script>

<style scoped>
/* 自带文字配色：放在深色大屏里也不继承大屏的浅色文字 */
.bd {
  position: absolute; inset: 0; z-index: 4; background: #eef2f0;
  color: #1f2f29; --muted: #66766f; --border: #d9e1dd; --ink-soft: #33463e;
}
.bd-canvas { position: absolute; inset: 0; }
.bd-canvas :deep(.twin-label-layer) { position: absolute; inset: 0; pointer-events: none; }
.bd-canvas :deep(.twin-label) {
  display: flex; flex-direction: column; align-items: center; gap: 1px;
  background: rgba(255, 255, 255, 0.92); color: var(--muted); font-size: 11px; padding: 3px 8px; border-radius: 8px;
  white-space: nowrap; box-shadow: 0 2px 8px rgba(34, 51, 46, 0.14); border: 1px dashed var(--border-strong);
}
.bd-canvas :deep(.twin-label b) { font-weight: 600; font-size: 12px; color: var(--ink-soft); }
.bd-canvas :deep(.twin-label span:empty) { display: none; }
.bd-canvas :deep(.twin-label--real) { border: 0; border-left: 3px solid var(--primary); }
.bd-canvas :deep(.twin-label--real b) { color: var(--primary-strong); }
.bd-top {
  position: absolute; top: 12px; left: 12px; right: 12px; display: flex; flex-wrap: wrap; gap: 8px;
  justify-content: space-between; align-items: center; pointer-events: none;
}
.bd-crumb, .bd-actions, .bd-card, .bd-note {
  pointer-events: auto; background: rgba(255, 255, 255, 0.88); backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.7); border-radius: 12px; box-shadow: 0 6px 18px rgba(30, 50, 40, 0.1);
}
.bd-crumb { display: flex; align-items: center; gap: 6px; padding: 6px 12px; font-size: 14px; }
.bd-crumb button { border: 0; background: none; padding: 0; color: var(--primary); cursor: pointer; font: inherit; }
.bd-crumb button:disabled { color: inherit; font-weight: 600; cursor: default; }
.bd-crumb span { color: var(--muted); }
.bd-actions { display: flex; gap: 6px; padding: 6px; }
.bd-note { position: absolute; right: 12px; bottom: 12px; margin: 0; padding: 4px 10px; font-size: 12px; color: var(--muted); max-width: 60%; }
.bd-card {
  position: absolute; left: 12px; bottom: 12px; width: min(300px, calc(100% - 24px)); max-height: calc(100% - 80px);
  overflow-y: auto; padding: 12px 14px;
}
.bd-card.wide { width: min(340px, calc(100% - 24px)); }
.bd-card-title { font-weight: 600; margin-bottom: 6px; }
.bd-muted { color: var(--muted); font-size: 12px; margin: 0 0 6px; }
.bd-list { list-style: none; margin: 0; padding: 0; display: flex; flex-direction: column; gap: 4px; }
.bd-rooms { display: grid; grid-template-columns: repeat(auto-fill, minmax(118px, 1fr)); gap: 4px; }
.bd-list button {
  width: 100%; display: flex; justify-content: space-between; align-items: baseline; gap: 8px; padding: 6px 8px;
  border: 1px solid var(--border); border-radius: 8px; background: #fff; cursor: pointer; color: inherit; font: inherit;
}
.bd-list button:hover { border-color: var(--primary); }
.bd-list button:focus-visible, .bd-crumb button:focus-visible { outline: 2px solid var(--primary); outline-offset: 2px; }
.bd-list button span { color: var(--muted); font-size: 12px; }
.bd-legend { display: flex; flex-wrap: wrap; gap: 4px 10px; font-size: 12px; color: var(--muted); margin-bottom: 8px; }
.bd-legend i { display: inline-block; width: 10px; height: 10px; border-radius: 3px; margin-right: 4px; vertical-align: -1px; }
.bd-legend .full { background: #a9c5e8; }
.bd-legend .partial { background: #b5dcc0; }
.bd-legend .vacant { background: #dde3e0; }
.bd-legend .none { background: #eeeeee; border: 1px solid #ddd; }
.bd-beds { list-style: none; margin: 0; padding: 0; display: flex; flex-direction: column; gap: 8px; }
.bd-beds > li:not(.bd-muted) { border: 1px solid var(--border); border-radius: 10px; padding: 8px 10px; background: #fff; }
.bd-bed-head { display: flex; justify-content: space-between; align-items: center; }
.bd-elder { font-size: 15px; font-weight: 600; margin-top: 4px; }
.bd-link { padding-left: 0; }
.bd-source { margin: 8px 0 0; font-size: 11px; color: var(--muted); }
.bd-result { padding: 8px 0; }
.bd-nowebgl { position: absolute; top: 60px; left: 12px; right: 12px; text-align: center; color: var(--muted); }
@media (max-width: 768px) {
  .bd-note { display: none; }
  .bd-card { max-height: 55%; }
}
</style>
