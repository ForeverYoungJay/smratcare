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
              <div class="plan-side-label">南侧（{{ activeFloor.southRooms.length }} 间）</div>
              <div class="plan-row">
                <RoomCell
                  v-for="room in activeFloor.southRooms"
                  :key="String(room.roomId)"
                  :room="room"
                  @click="openRoom(room)"
                />
                <a-empty v-if="!activeFloor.southRooms.length" description="该层没有标注南向的房间" />
              </div>

              <div class="plan-corridor"><span>走　廊</span></div>

              <div class="plan-side-label">北侧（{{ activeFloor.northRooms.length }} 间）</div>
              <div class="plan-row">
                <RoomCell
                  v-for="room in activeFloor.northRooms"
                  :key="String(room.roomId)"
                  :room="room"
                  @click="openRoom(room)"
                />
                <a-empty v-if="!activeFloor.northRooms.length" description="该层没有标注北向的房间" />
              </div>

              <template v-if="activeFloor.otherRooms.length">
                <div class="plan-side-label">未标注朝向 / 东西向（{{ activeFloor.otherRooms.length }} 间）</div>
                <div class="plan-row">
                  <RoomCell
                    v-for="room in activeFloor.otherRooms"
                    :key="String(room.roomId)"
                    :room="room"
                    @click="openRoom(room)"
                  />
                </div>
                <div class="plan-tip">在「床位管理 → 房间」里补上朝向，这些房间就会自动归到走廊两侧。</div>
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
          <span :class="Number(item.outstandingAmount || 0) > 0 ? 'is-danger-text' : ''">
            欠费 {{ amount(item.outstandingAmount) }} 元
          </span>
        </div>
      </div>
      <a-empty v-else description="该房间当前没有在住长者" />

      <div class="modal-actions">
        <a-space>
          <a-button @click="goRoomDetail">进入房间详情</a-button>
          <a-button type="primary" @click="goElectricity">去登记电费</a-button>
        </a-space>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, h, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { getFloorPlan, type FloorPlanResponse, type FloorPlanRoom } from '../../../api/floorPlan'

const router = useRouter()
const loading = ref(false)
const plan = ref<FloorPlanResponse | null>(null)
const month = ref<any>(dayjs().startOf('month'))
const zoom = ref(100)
const selectedBuilding = ref<string | undefined>(undefined)
const selectedFloor = ref<string | undefined>(undefined)
const roomOpen = ref(false)
const activeRoom = ref<FloorPlanRoom | null>(null)

/** 房间格子做成内联组件，避免为一个纯展示单元再拆文件。 */
const RoomCell = (props: { room: FloorPlanRoom }) => {
  const room = props.room
  return h(
    'button',
    {
      type: 'button',
      class: ['room-cell', `is-${String(room.planStatus).toLowerCase()}`],
      title: `${room.roomNo} · ${room.planStatusText}`
    },
    [
      h('span', { class: 'room-cell__no' }, room.roomNo),
      h('span', { class: 'room-cell__beds' }, `${room.occupiedBeds}/${room.totalBeds} 床`),
      Number(room.overdueAmount || 0) > 0
        ? h('span', { class: 'room-cell__tag' }, `欠 ${Number(room.overdueAmount).toFixed(0)}`)
        : room.electricityUnpaid
          ? h('span', { class: 'room-cell__tag' }, '电费未缴')
          : null
    ]
  )
}

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
</style>
