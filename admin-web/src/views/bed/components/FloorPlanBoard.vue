<template>
  <div class="floor-plan-board">
    <a-card class="card-elevated" :bordered="false">
      <div class="plan-toolbar">
        <a-space wrap>
          <a-date-picker v-model:value="month" picker="month" :allow-clear="false" style="width: 150px" />
          <a-select v-model:value="selectedBuilding" style="width: 160px" :options="buildingOptions" />
          <a-radio-group v-model:value="selectedFloor" button-style="solid" size="small">
            <a-radio-button v-for="floor in floorOptions" :key="floor" :value="floor">{{ floor }}</a-radio-button>
          </a-radio-group>
          <a-select
            v-model:value="roomTypeFilter"
            allow-clear
            size="small"
            placeholder="房型"
            style="width: 130px"
            :options="roomTypeOptions"
          />
          <a-select
            v-model:value="bedStatusFilter"
            allow-clear
            size="small"
            placeholder="床位状态"
            style="width: 130px"
            :options="bedStatusOptions"
          />
          <a-button size="small" :loading="loading" @click="load">刷新</a-button>
        </a-space>
        <a-space>
          <span class="zoom-label">缩放 {{ zoom }}%</span>
          <a-button size="small" :disabled="zoom <= 60" @click="stepZoom(-10)">－</a-button>
          <a-slider
            v-model:value="zoom"
            :min="60"
            :max="150"
            :step="10"
            style="width: 160px"
          />
          <a-button size="small" :disabled="zoom >= 150" @click="stepZoom(10)">＋</a-button>
          <a-button size="small" @click="zoom = 100">还原</a-button>
        </a-space>
      </div>
    </a-card>

    <a-row :gutter="[16, 16]" style="margin-top: 16px;">
      <a-col :xs="12" :md="6">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="总房间" :value="plan?.totalRooms || 0" suffix="间" />
        </a-card>
      </a-col>
      <a-col :xs="12" :md="6">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="已住" :value="plan?.occupiedRooms || 0" suffix="间" />
        </a-card>
      </a-col>
      <a-col :xs="12" :md="6">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic
            title="空房"
            :value="plan?.emptyRooms || 0"
            suffix="间"
            :value-style="{ color: '#cf1322' }"
          />
        </a-card>
      </a-col>
      <a-col :xs="12" :md="6">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="床位使用率" :value="Number(plan?.bedUsageRate || 0)" :precision="1" suffix="%" />
          <div class="stat-hint">在住 {{ plan?.occupiedBeds || 0 }} / {{ plan?.totalBeds || 0 }} 床</div>
        </a-card>
      </a-col>
    </a-row>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <div class="legend">
        <span class="legend__item"><i class="dot is-normal"></i>正常</span>
        <span class="legend__item"><i class="dot is-overdue"></i>欠费</span>
        <span class="legend__item"><i class="dot is-empty"></i>空房</span>
        <span class="legend__item"><i class="dot is-disabled"></i>未启用</span>
        <span class="legend__hint">未启用 &gt; 空房 &gt; 欠费 &gt; 正常，按优先级取色；欠费含代养费欠费与本月电费未缴</span>
      </div>

      <a-spin :spinning="loading">
        <div class="plan-viewport">
          <div class="plan-canvas" :style="{ transform: `scale(${zoom / 100})` }">
            <template v-if="activeFloor">
              <template v-for="section in floorSections" :key="section.key">
                <div class="plan-side-label">{{ section.label }}（{{ section.rooms.length }} 间）</div>
                <div class="plan-row">
                  <div
                    v-for="room in section.rooms"
                    :key="String(room.roomId)"
                    class="room-cell"
                    :class="[`is-${String(room.planStatus).toLowerCase()}`, { 'is-drag-over': dragOverRoomId === String(room.roomId) }]"
                    :title="`${room.roomNo} · ${room.planStatusText}`"
                    draggable="true"
                    @click="openRoom(room)"
                    @dragstart="onRoomDragStart(room)"
                    @dragover.prevent="dragOverRoomId = String(room.roomId)"
                    @dragleave="dragOverRoomId = ''"
                    @drop.prevent="onRoomDrop(section, room)"
                  >
                    <span class="room-cell__no">{{ room.roomNo }}</span>
                    <span class="room-cell__beds">{{ room.occupiedBeds }}/{{ room.totalBeds }} 床</span>
                    <span v-if="Number(room.overdueAmount || 0) > 0" class="room-cell__tag">
                      欠 {{ Number(room.overdueAmount).toFixed(0) }}
                    </span>
                    <span v-else-if="room.electricityUnpaid" class="room-cell__tag">电费未缴</span>
                    <!-- 床位格子承接原矩阵布局的床位点击与二维码打印 -->
                    <span v-if="visibleBeds(room).length" class="room-cell__bedchips">
                      <button
                        v-for="bed in visibleBeds(room)"
                        :key="String(bed.bedId)"
                        type="button"
                        class="bed-chip"
                        :class="`is-bed-${bed.status || 0}`"
                        :title="`${bed.bedNo || ''} · ${bed.statusText || ''}${bed.elderName ? ' · ' + bed.elderName : ''}`"
                        @click.stop="openBed(bed, room)"
                      >{{ bed.bedNo || '-' }}</button>
                    </span>
                  </div>
                  <a-empty v-if="!section.rooms.length" :description="section.emptyText" />
                </div>
                <div v-if="section.key === 'south'" class="plan-corridor"><span>走　廊</span></div>
                <div v-if="section.key === 'other' && section.rooms.length" class="plan-tip">
                  在「床位管理 → 房间」里补上朝向，这些房间就会自动归到走廊两侧。
                </div>
              </template>
            </template>
            <a-empty v-else description="请选择楼栋与楼层" />
          </div>
        </div>
      </a-spin>
    </a-card>

    <a-modal v-model:open="roomOpen" :title="`房间概览 · ${activeRoom?.roomNo || ''}`" :footer="null" width="600">
      <a-descriptions bordered :column="2" size="small">
        <a-descriptions-item label="房间号">{{ activeRoom?.roomNo }}</a-descriptions-item>
        <a-descriptions-item label="朝向">{{ activeRoom?.orientationText || '未标注' }}</a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="statusColor(activeRoom?.planStatus)">{{ activeRoom?.planStatusText }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="床位">
          {{ activeRoom?.occupiedBeds }} / {{ activeRoom?.totalBeds }}
        </a-descriptions-item>
        <a-descriptions-item label="代养费欠费">{{ amount(activeRoom?.overdueAmount) }} 元</a-descriptions-item>
        <a-descriptions-item label="本月电费">
          {{ amount(activeRoom?.electricityFee) }} 元
          <a-tag v-if="activeRoom?.electricityUnpaid" color="red" style="margin-left: 6px">未缴</a-tag>
        </a-descriptions-item>
      </a-descriptions>

      <div class="resident-title">在住长者</div>
      <div v-if="activeRoom?.residents?.length" class="resident-list">
        <div v-for="item in activeRoom.residents" :key="String(item.elderId)" class="resident-row">
          <div>
            <strong>{{ item.elderName }}</strong>
            <small>{{ item.bedNo || '未分配床位' }}{{ item.careLevel ? ' · ' + item.careLevel : '' }}</small>
          </div>
          <div class="resident-row__right">
            <span :class="Number(item.outstandingAmount || 0) > 0 ? 'is-danger-text' : ''">
              欠费 {{ amount(item.outstandingAmount) }} 元
            </span>
            <a-space size="small">
              <a-button type="link" size="small" @click="goElderProfile(item.elderId)">档案</a-button>
              <a-button type="link" size="small" @click="goAssessment(item.elderId)">评估</a-button>
              <a-button type="link" size="small" @click="goContracts(item.elderId)">合同票据</a-button>
              <a-button type="link" size="small" @click="goStatusChange(item.elderId)">状态变更</a-button>
            </a-space>
          </div>
        </div>
      </div>
      <a-empty v-else description="该房间当前没有在住长者" />

      <div class="modal-actions">
        <a-space wrap>
          <a-button @click="goRoomDetail">进入房间详情</a-button>
          <a-button v-if="hasEmptyBed" @click="goAdmission">安排入住</a-button>
          <a-button type="primary" @click="goElectricity">去登记电费</a-button>
        </a-space>
      </div>
    </a-modal>

    <a-modal v-model:open="bedOpen" :title="`床位 · ${activeBed?.bedNo || ''}`" :footer="null" width="460">
      <a-descriptions bordered :column="1" size="small">
        <a-descriptions-item label="所在房间">
          {{ activeBedRoom?.roomNo }}（{{ activeBedRoom?.orientationText || '朝向未标注' }}）
        </a-descriptions-item>
        <a-descriptions-item label="床位号">{{ activeBed?.bedNo || '-' }}</a-descriptions-item>
        <a-descriptions-item label="床型">{{ activeBed?.bedType || '-' }}</a-descriptions-item>
        <a-descriptions-item label="状态">{{ activeBed?.statusText || '-' }}</a-descriptions-item>
        <a-descriptions-item label="在住长者">{{ activeBed?.elderName || '空床' }}</a-descriptions-item>
      </a-descriptions>

      <div v-if="qrDataUrl" class="bed-qr">
        <img :src="qrDataUrl" alt="床位二维码" />
        <div class="bed-qr__code">{{ activeBed?.bedQrCode || activeBed?.bedId }}</div>
      </div>

      <div class="modal-actions">
        <a-space wrap>
          <a-button v-if="activeBed?.elderId" @click="goElderProfile(activeBed.elderId)">长者档案</a-button>
          <a-button v-else @click="goAdmission">安排入住</a-button>
          <a-button type="primary" :disabled="!qrDataUrl" @click="printBedQr">打印二维码</a-button>
        </a-space>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import QRCode from 'qrcode'
import { message } from 'ant-design-vue'
import { updateRoomSort } from '../../../api/bed'
import { getFloorPlan, type FloorPlanBed, type FloorPlanResponse, type FloorPlanRoom } from '../../../api/floorPlan'

const router = useRouter()
const loading = ref(false)
const plan = ref<FloorPlanResponse | null>(null)
const month = ref<any>(dayjs().startOf('month'))
const zoom = ref(100)
const selectedBuilding = ref<string | undefined>(undefined)
const selectedFloor = ref<string | undefined>(undefined)
const roomOpen = ref(false)
const activeRoom = ref<FloorPlanRoom | null>(null)


const buildingOptions = computed(() =>
  (plan.value?.buildings || []).map((item) => ({ label: item.building, value: item.building }))
)
const activeBuilding = computed(() =>
  (plan.value?.buildings || []).find((item) => item.building === selectedBuilding.value) || null
)
const floorOptions = computed(() => (activeBuilding.value?.floors || []).map((item) => item.floorNo))
const activeFloor = computed(() =>
  (activeBuilding.value?.floors || []).find((item) => item.floorNo === selectedFloor.value) || null
)

function stepZoom(delta: number) {
  zoom.value = Math.min(150, Math.max(60, zoom.value + delta))
}

function amount(value?: number | null) {
  return Number(value || 0).toFixed(2)
}

function statusColor(status?: string) {
  switch (status) {
    case 'NORMAL':
      return 'green'
    case 'OVERDUE':
      return 'gold'
    case 'EMPTY':
      return 'red'
    case 'DISABLED':
      return 'default'
    default:
      return 'default'
  }
}

async function load() {
  loading.value = true
  try {
    plan.value = await getFloorPlan(dayjs(month.value).format('YYYY-MM'))
    const buildings = plan.value?.buildings || []
    if (!buildings.some((item) => item.building === selectedBuilding.value)) {
      selectedBuilding.value = buildings[0]?.building
    }
    const floors = activeBuilding.value?.floors || []
    if (!floors.some((item) => item.floorNo === selectedFloor.value)) {
      selectedFloor.value = floors[0]?.floorNo
    }
  } finally {
    loading.value = false
  }
}

function openRoom(room: FloorPlanRoom) {
  activeRoom.value = room
  roomOpen.value = true
}

function goRoomDetail() {
  if (!activeRoom.value) return
  roomOpen.value = false
  router.push({ path: '/logistics/assets/room-detail', query: { roomId: String(activeRoom.value.roomId) } })
}

const roomTypeFilter = ref<string | undefined>(undefined)
const bedStatusFilter = ref<number | undefined>(undefined)
const dragRoomId = ref('')
const dragOverRoomId = ref('')
const bedOpen = ref(false)
const activeBed = ref<FloorPlanBed | null>(null)
const activeBedRoom = ref<FloorPlanRoom | null>(null)
const qrDataUrl = ref('')

const bedStatusOptions = [
  { label: '空床', value: 1 },
  { label: '入住', value: 2 },
  { label: '维修', value: 3 }
]

const roomTypeOptions = computed(() => {
  const set = new Set<string>()
  ;(plan.value?.buildings || []).forEach((building) =>
    building.floors.forEach((floor) =>
      [...floor.southRooms, ...floor.northRooms, ...floor.otherRooms].forEach((room) => {
        const type = String(room.roomType || '').trim()
        if (type) set.add(type)
      })
    )
  )
  return Array.from(set).sort().map((item) => ({ label: item, value: item }))
})

function matchRoom(room: FloorPlanRoom) {
  if (roomTypeFilter.value && String(room.roomType || '') !== roomTypeFilter.value) return false
  if (bedStatusFilter.value != null) {
    return (room.beds || []).some((bed) => Number(bed.status) === Number(bedStatusFilter.value))
  }
  return true
}

/** 南侧 / 北侧 / 未标注三段统一成一个结构，模板只写一遍格子 */
const floorSections = computed(() => {
  const floor = activeFloor.value
  if (!floor) return []
  return [
    { key: 'south', label: '南侧', rooms: (floor.southRooms || []).filter(matchRoom), emptyText: '该层没有标注南向的房间' },
    { key: 'north', label: '北侧', rooms: (floor.northRooms || []).filter(matchRoom), emptyText: '该层没有标注北向的房间' },
    { key: 'other', label: '未标注朝向 / 东西向', rooms: (floor.otherRooms || []).filter(matchRoom), emptyText: '' }
  ].filter((section) => section.key !== 'other' || section.rooms.length)
})

function visibleBeds(room: FloorPlanRoom) {
  const beds = room.beds || []
  if (bedStatusFilter.value == null) return beds
  return beds.filter((bed) => Number(bed.status) === Number(bedStatusFilter.value))
}

function onRoomDragStart(room: FloorPlanRoom) {
  dragRoomId.value = String(room.roomId)
}

/** 房间拖拽排序：承接原矩阵布局的能力，按同一分区内的新顺序提交 */
async function onRoomDrop(section: { rooms: FloorPlanRoom[] }, target: FloorPlanRoom) {
  const sourceId = dragRoomId.value
  dragOverRoomId.value = ''
  dragRoomId.value = ''
  if (!sourceId || sourceId === String(target.roomId)) return
  const floorId = target.floorId
  if (!floorId) {
    message.warning('该房间缺少楼层信息，无法保存顺序，请先在床位管理里补齐楼层')
    return
  }
  const ids = section.rooms.map((item) => String(item.roomId))
  const from = ids.indexOf(sourceId)
  const to = ids.indexOf(String(target.roomId))
  if (from < 0 || to < 0) return
  ids.splice(to, 0, ids.splice(from, 1)[0])
  try {
    await updateRoomSort({ floorId, roomIds: ids })
    message.success('房间顺序已更新')
    await load()
  } catch (error: any) {
    message.error(error?.message || '保存顺序失败')
  }
}

async function openBed(bed: FloorPlanBed, room: FloorPlanRoom) {
  activeBed.value = bed
  activeBedRoom.value = room
  qrDataUrl.value = ''
  bedOpen.value = true
  const code = bed.bedQrCode || String(bed.bedId || '')
  if (!code) return
  try {
    qrDataUrl.value = await QRCode.toDataURL(code, { width: 220, margin: 1 })
  } catch {
    qrDataUrl.value = ''
  }
}

/** 床位二维码打印：承接原矩阵布局的能力 */
function printBedQr() {
  if (!qrDataUrl.value) {
    message.warning('未生成二维码')
    return
  }
  const win = window.open('', '_blank')
  if (!win) return
  win.document.write(`<img src="${qrDataUrl.value}" style="width:220px;height:220px"/>`)
  win.print()
  win.close()
}

const hasEmptyBed = computed(() =>
  Number(activeRoom.value?.totalBeds || 0) > Number(activeRoom.value?.occupiedBeds || 0)
)

// 下面几个跳转承接原 3D 床态全景页的导航能力（该页只读、动作全是跳转，故随 3D 一并下线）。
// 统一用路由 name，避免手写路径与真实路由不一致。
function goElderProfile(elderId: string | number) {
  router.push({ name: 'ElderDetail', params: { id: String(elderId) } })
}

function goAssessment(elderId: string | number) {
  router.push({ name: 'ElderAssessmentAdmission', query: { elderId: String(elderId) } })
}

function goContracts(elderId: string | number) {
  router.push({ name: 'ElderContractsInvoices', query: { residentId: String(elderId) } })
}

function goStatusChange(elderId: string | number) {
  router.push({ name: 'ElderStatusChangeOverview', query: { residentId: String(elderId) } })
}

function goAdmission() {
  router.push({ name: 'ElderAdmissionProcessing' })
}

function goElectricity() {
  roomOpen.value = false
  router.push('/finance/electricity-fee')
}

watch(selectedBuilding, () => {
  const floors = activeBuilding.value?.floors || []
  if (!floors.some((item) => item.floorNo === selectedFloor.value)) {
    selectedFloor.value = floors[0]?.floorNo
  }
})

watch(month, () => load())

onMounted(load)
</script>

<style scoped>
.plan-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
}

.zoom-label {
  font-size: 12px;
  color: var(--text-tertiary, #999);
}

.stat-hint {
  margin-top: 4px;
  font-size: 12px;
  color: var(--text-tertiary, #999);
}

.legend {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 14px;
  font-size: 12px;
}

.legend__item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.legend__hint {
  color: var(--text-tertiary, #999);
}

.dot {
  display: inline-block;
  width: 12px;
  height: 12px;
  border-radius: 3px;
}

.dot.is-normal {
  background: #52c41a;
}

.dot.is-overdue {
  background: #faad14;
}

.dot.is-empty {
  background: #ff4d4f;
}

.dot.is-disabled {
  background: #bfbfbf;
}

/* 缩放用 transform，配合外层 overflow 保证放大后可横向滚动查看 */
.plan-viewport {
  overflow: auto;
  padding: 8px;
  border: 1px solid var(--border-soft, #eee);
  border-radius: 8px;
  background: var(--fill-subtle, rgba(0, 0, 0, 0.02));
}

.plan-canvas {
  transform-origin: top left;
  min-width: 640px;
  transition: transform 0.15s ease;
}

.plan-side-label {
  margin: 8px 0 6px;
  font-size: 12px;
  color: var(--text-secondary, #666);
}

.plan-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  min-height: 56px;
}

.plan-corridor {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 34px;
  margin: 12px 0;
  border-top: 2px dashed #bfbfbf;
  border-bottom: 2px dashed #bfbfbf;
  background: repeating-linear-gradient(
    45deg,
    rgba(0, 0, 0, 0.03),
    rgba(0, 0, 0, 0.03) 8px,
    transparent 8px,
    transparent 16px
  );
  color: var(--text-tertiary, #999);
  font-size: 12px;
  letter-spacing: 4px;
}

.plan-tip {
  margin-top: 8px;
  font-size: 12px;
  color: var(--text-tertiary, #999);
}

:deep(.room-cell) {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: center;
  gap: 2px;
  width: 108px;
  min-height: 66px;
  padding: 8px 10px;
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
  text-align: left;
}

:deep(.room-cell__no) {
  font-weight: 600;
  font-size: 14px;
}

:deep(.room-cell__beds) {
  font-size: 12px;
  color: rgba(0, 0, 0, 0.55);
}

:deep(.room-cell__tag) {
  font-size: 11px;
  color: #ad4e00;
}

:deep(.room-cell.is-normal) {
  background: rgba(82, 196, 26, 0.16);
  border-color: rgba(82, 196, 26, 0.45);
}

:deep(.room-cell.is-overdue) {
  background: rgba(250, 173, 20, 0.2);
  border-color: rgba(250, 173, 20, 0.5);
}

:deep(.room-cell.is-empty) {
  background: rgba(255, 77, 79, 0.16);
  border-color: rgba(255, 77, 79, 0.45);
}

:deep(.room-cell.is-disabled) {
  background: rgba(0, 0, 0, 0.06);
  border-color: rgba(0, 0, 0, 0.15);
  color: rgba(0, 0, 0, 0.4);
}

.resident-title {
  margin: 14px 0 8px;
  font-size: 14px;
  font-weight: 600;
}

.resident-list {
  display: flex;
  flex-direction: column;
}

.resident-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 0;
  border-bottom: 1px dashed var(--border-soft, #eee);
  font-size: 13px;
}

.resident-row small {
  margin-left: 8px;
  color: var(--text-tertiary, #999);
}

.is-danger-text {
  color: #cf1322;
  font-weight: 600;
}

.modal-actions {
  margin-top: 16px;
  text-align: right;
}

.resident-row__right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
}

.room-cell__bedchips {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-top: 6px;
}

.bed-chip {
  padding: 1px 6px;
  border: 1px solid var(--border-soft, #e5e5e5);
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.75);
  font-size: 11px;
  line-height: 1.5;
  cursor: pointer;
}

.bed-chip.is-bed-2 {
  border-color: #52c41a;
  color: #237804;
}

.bed-chip.is-bed-3 {
  border-color: #faad14;
  color: #ad6800;
}

.room-cell.is-drag-over {
  outline: 2px dashed #1677ff;
  outline-offset: 2px;
}

.bed-qr {
  margin-top: 12px;
  text-align: center;
}

.bed-qr img {
  width: 180px;
  height: 180px;
}

.bed-qr__code {
  margin-top: 4px;
  font-size: 12px;
  color: var(--text-tertiary, #999);
  word-break: break-all;
}
</style>
