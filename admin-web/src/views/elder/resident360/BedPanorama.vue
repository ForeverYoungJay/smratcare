<template>
  <PageContainer title="床态全景" subTitle="楼号/楼层平面图联动，空床优先与风险角标可视化">
    <a-card title="楼层平面图" class="card-elevated" :bordered="false">
      <a-space wrap style="margin-bottom: 12px">
        <a-tag color="default">空闲 {{ stats.idle }}</a-tag>
        <a-tag color="processing">预定 {{ stats.reserved }}</a-tag>
        <a-tag color="green">在住 {{ stats.occupied }}</a-tag>
        <a-tag color="orange">维修 {{ stats.maintenance }}</a-tag>
        <a-tag color="cyan">清洁中 {{ stats.cleaning }}</a-tag>
        <a-tag color="red">锁定 {{ stats.locked }}</a-tag>
      </a-space>

      <div class="selector-strip">
        <div class="selector-group">
          <div class="selector-label">搜索</div>
          <a-input-search v-model:value="keyword" placeholder="搜索房间号/长者名" allow-clear style="width: 260px" />
        </div>
        <div class="selector-group">
          <div class="selector-label">楼号</div>
          <a-segmented v-model:value="selectedBuilding" :options="buildingOptions" @change="onBuildingChange" />
        </div>
        <div class="selector-group">
          <div class="selector-label">层数</div>
          <a-segmented v-model:value="selectedFloor" :options="floorOptions" />
        </div>
        <div class="selector-group">
          <div class="selector-label">风险筛选</div>
          <a-switch v-model:checked="riskFilterEnabled" />
          <a-segmented
            v-if="riskFilterEnabled"
            v-model:value="riskFilterLevel"
            :options="riskLevelOptions"
          />
        </div>
      </div>

      <div class="plan-stage">
        <a-empty v-if="!selectedBuilding || !selectedFloor || !planRooms.length" description="当前楼层暂无床位数据" />
        <div v-else class="plan-grid">
          <div
            v-for="room in planRooms"
            :key="room.key"
            class="room-block"
            :style="{ gridColumn: `span ${room.autoSpan}` }"
          >
            <div class="room-head">
              <div class="room-title">{{ room.roomNo }}</div>
              <div class="room-type">{{ room.roomType }} · {{ room.capacity }}床</div>
            </div>
            <div class="room-meta">
              {{ room.occupiedBeds }}/{{ room.totalBeds }} 床 · {{ room.elderCount }} 人 · 空床 {{ room.emptyBeds }}
            </div>
            <div class="bed-matrix">
              <button
                v-for="bed in room.beds"
                :key="bed.id"
                type="button"
                class="bed-tile"
                :class="[statusClass(resolveStatus(bed)), { 'empty-priority': isEmptyBed(bed) }]"
                @click="selectBed(bed)"
              >
                <span v-if="bedRiskLabel(bed)" class="risk-corner" :class="`risk-${bedRiskLevel(bed)}`">{{ bedRiskLabel(bed) }}</span>
                <span class="bed-no">{{ bed.bedNo }}</span>
                <span class="bed-name">{{ bed.elderName || '空床' }}</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </a-card>

    <a-modal v-model:open="detailOpen" title="床位详情" width="560px" :footer="null" destroy-on-close>
      <a-descriptions v-if="selectedBed" :column="1" size="small" bordered>
        <a-descriptions-item label="床位">{{ selectedBed.bedNo || '-' }}</a-descriptions-item>
        <a-descriptions-item label="楼栋/楼层/房间">{{ selectedBed.building || '-' }} / {{ selectedBed.floorNo || '-' }} / {{ selectedBed.roomNo || '-' }}</a-descriptions-item>
        <a-descriptions-item label="在住长者">{{ selectedBed.elderName || '-' }}</a-descriptions-item>
        <a-descriptions-item label="护理等级">{{ selectedBed.careLevel || '-' }}</a-descriptions-item>
        <a-descriptions-item label="风险级别">{{ bedRiskLabel(selectedBed) || '无' }}</a-descriptions-item>
        <a-descriptions-item label="风险来源">{{ selectedBed.riskSource || '-' }}</a-descriptions-item>
        <a-descriptions-item label="最近评估">{{ formatLatestAssessment(selectedBed) }}</a-descriptions-item>
        <a-descriptions-item label="24h异常体征">{{ selectedBed.abnormalVital24hCount || 0 }} 次</a-descriptions-item>
        <a-descriptions-item label="今日任务">巡视2次、翻身3次、测量生命体征1次</a-descriptions-item>
        <a-descriptions-item label="余额/欠费">余额 1500，欠费 0</a-descriptions-item>
      </a-descriptions>

      <a-space direction="vertical" style="margin-top: 12px; width: 100%">
        <a-button block type="primary" @click="openProfile">打开长者档案</a-button>
        <a-button block @click="allocateBed">一键分配床位</a-button>
        <a-button block danger @click="createAlert">生成提醒并进入任务中心</a-button>
        <a-button block @click="goScan">扫码执行（定位今日任务）</a-button>
      </a-space>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import PageContainer from '../../../components/PageContainer.vue'
import { getBedMap, getRoomList } from '../../../api/bed'
import { useLiveSyncRefresh } from '../../../composables/useLiveSyncRefresh'
import type { BedItem, RoomItem } from '../../../types'

type RoomScene = {
  key: string
  roomNo: string
  roomType: string
  capacity: number
  autoSpan: 1 | 2
  beds: BedItem[]
  totalBeds: number
  occupiedBeds: number
  elderCount: number
  emptyBeds: number
}

const router = useRouter()
const beds = ref<BedItem[]>([])
const roomTypeMap = ref<Record<number, string>>({})
const roomCapacityMap = ref<Record<number, number>>({})
const keyword = ref('')
const selectedBed = ref<BedItem | null>(null)
const detailOpen = ref(false)
const selectedBuilding = ref('')
const selectedFloor = ref('')
const riskFilterEnabled = ref(false)
const riskFilterLevel = ref<'ALL' | 'HIGH' | 'MEDIUM' | 'LOW'>('ALL')
const riskLevelOptions = [
  { label: '全部风险', value: 'ALL' },
  { label: '高风险', value: 'HIGH' },
  { label: '中风险', value: 'MEDIUM' },
  { label: '低风险', value: 'LOW' }
]

const filteredBeds = computed(() => beds.value.filter((b) => {
  if (keyword.value) {
    const text = `${b.roomNo || ''} ${b.elderName || ''}`
    if (!text.includes(keyword.value)) return false
  }
  return true
}))

const buildingList = computed(() => {
  const set = new Set<string>()
  beds.value.forEach((item) => set.add((item.building || '未分配楼栋').trim() || '未分配楼栋'))
  return Array.from(set)
})

const buildingOptions = computed(() => buildingList.value.map((item) => ({ label: item, value: item })))

const floorList = computed(() => {
  if (!selectedBuilding.value) return []
  const set = new Set<string>()
  beds.value
    .filter((item) => ((item.building || '未分配楼栋').trim() || '未分配楼栋') === selectedBuilding.value)
    .forEach((item) => set.add((item.floorNo || '未知楼层').trim() || '未知楼层'))
  return Array.from(set).sort((a, b) => {
    const n1 = Number(String(a).replace(/[^0-9.-]/g, ''))
    const n2 = Number(String(b).replace(/[^0-9.-]/g, ''))
    if (Number.isNaN(n1) || Number.isNaN(n2)) return String(a).localeCompare(String(b))
    return n2 - n1
  })
})

const floorOptions = computed(() => floorList.value.map((item) => ({ label: item, value: item })))

const stats = computed(() => {
  const s = { idle: 0, reserved: 0, occupied: 0, maintenance: 0, cleaning: 0, locked: 0 }
  beds.value.forEach((b) => {
    const st = resolveStatus(b)
    if (st === '空闲') s.idle += 1
    if (st === '预定') s.reserved += 1
    if (st === '在住') s.occupied += 1
    if (st === '维修') s.maintenance += 1
    if (st === '清洁中') s.cleaning += 1
    if (st === '锁定') s.locked += 1
  })
  return s
})

const planRooms = computed<RoomScene[]>(() => {
  if (!selectedBuilding.value || !selectedFloor.value) return []
  const sourceBeds = riskFilterEnabled.value
    ? filteredBeds.value.filter((item) => {
      if (!item.riskLevel) return false
      if (riskFilterLevel.value === 'ALL') return true
      return item.riskLevel === riskFilterLevel.value
    })
    : filteredBeds.value
  const roomMap = new Map<string, BedItem[]>()
  sourceBeds.forEach((bed) => {
    const building = (bed.building || '未分配楼栋').trim() || '未分配楼栋'
    const floor = (bed.floorNo || '未知楼层').trim() || '未知楼层'
    if (building !== selectedBuilding.value || floor !== selectedFloor.value) return
    const roomNo = (bed.roomNo || `房间-${bed.roomId || '-'}`).trim() || `房间-${bed.roomId || '-'}`
    if (!roomMap.has(roomNo)) roomMap.set(roomNo, [])
    roomMap.get(roomNo)!.push(bed)
  })

  return Array.from(roomMap.entries())
    .map(([roomNo, roomBeds]) => {
      const roomId = Number(roomBeds[0]?.roomId || 0)
      const sortedBeds = [...roomBeds].sort((a, b) => {
        const aEmpty = isEmptyBed(a) ? 0 : 1
        const bEmpty = isEmptyBed(b) ? 0 : 1
        if (aEmpty !== bEmpty) return aEmpty - bEmpty
        return String(a.bedNo || '').localeCompare(String(b.bedNo || ''))
      })
      const totalBeds = roomBeds.length
      const occupiedBeds = roomBeds.filter((b) => !!b.elderId).length
      const elderCount = roomBeds.filter((b) => !!b.elderName).length
      const emptyBeds = totalBeds - occupiedBeds
      const capacity = roomCapacityMap.value[roomId] || totalBeds || 1
      const autoSpan: 1 | 2 = capacity >= 3 ? 2 : 1
      return {
        key: `${selectedBuilding.value}-${selectedFloor.value}-${roomNo}`,
        roomNo,
        roomType: roomTypeMap.value[roomId] || '标准间',
        capacity,
        autoSpan,
        beds: sortedBeds,
        totalBeds,
        occupiedBeds,
        elderCount,
        emptyBeds
      }
    })
    .sort((a, b) => {
      if (a.capacity !== b.capacity) return b.capacity - a.capacity
      if (a.emptyBeds !== b.emptyBeds) return b.emptyBeds - a.emptyBeds
      return String(a.roomNo).localeCompare(String(b.roomNo))
    })
})

function onBuildingChange() {
  selectedFloor.value = floorList.value[0] || ''
}

function resolveStatus(bed: BedItem): '空闲' | '预定' | '在住' | '维修' | '清洁中' | '锁定' {
  if (bed.elderId) return '在住'
  if (bed.status === 0) return '锁定'
  if (bed.status === 2) return '维修'
  if (bed.status === 3) return '清洁中'
  if (String(bed.bedNo || '').endsWith('R')) return '预定'
  return '空闲'
}

function statusClass(status: string) {
  if (status === '在住') return 'is-occupied'
  if (status === '预定') return 'is-reserved'
  if (status === '维修') return 'is-maintenance'
  if (status === '清洁中') return 'is-cleaning'
  if (status === '锁定') return 'is-locked'
  return 'is-idle'
}

function isEmptyBed(bed: BedItem) {
  return !bed.elderId && resolveStatus(bed) === '空闲'
}

function bedRiskLevel(bed: BedItem): 'high' | 'medium' | 'low' | '' {
  if (bed.riskLevel === 'HIGH') return 'high'
  if (bed.riskLevel === 'MEDIUM') return 'medium'
  if (bed.riskLevel === 'LOW') return 'low'
  return ''
}

function bedRiskLabel(bed: BedItem) {
  return bed.riskLabel || ''
}

function formatLatestAssessment(bed: BedItem) {
  const level = bed.latestAssessmentLevel || '-'
  const date = bed.latestAssessmentDate || '-'
  if (level === '-' && date === '-') return '-'
  return `${level} / ${date}`
}

function selectBed(bed: BedItem) {
  selectedBed.value = bed
  detailOpen.value = true
}

function openProfile() {
  if (!selectedBed.value?.elderId) {
    message.warning('当前是空床位，请先分配床位')
    return
  }
  detailOpen.value = false
  router.push(`/elder/detail/${selectedBed.value.elderId}`)
}

function allocateBed() {
  if (!selectedBed.value) return
  detailOpen.value = false
  router.push(`/elder/admission-processing?bedId=${selectedBed.value.id}`)
}

function createAlert() {
  if (!selectedBed.value) return
  detailOpen.value = false
  message.success('已生成提醒并推送到提醒中心/任务中心')
  router.push('/oa/work-execution/task?category=alert')
}

function goScan() {
  detailOpen.value = false
  router.push(`/care/workbench/qr?bedId=${selectedBed.value?.id || ''}`)
}

async function loadBeds() {
  beds.value = await getBedMap()
  if (!selectedBuilding.value) selectedBuilding.value = buildingList.value[0] || ''
  if (!selectedFloor.value) selectedFloor.value = floorList.value[0] || ''
}

async function loadRooms() {
  const rooms: RoomItem[] = await getRoomList()
  const typeMap: Record<number, string> = {}
  const capMap: Record<number, number> = {}
  rooms.forEach((item) => {
    typeMap[item.id] = item.roomType || '标准间'
    capMap[item.id] = Number(item.capacity || 1)
  })
  roomTypeMap.value = typeMap
  roomCapacityMap.value = capMap
}

useLiveSyncRefresh({
  topics: ['elder', 'bed', 'lifecycle', 'finance', 'care', 'dining'],
  refresh: () => {
    loadBeds()
    loadRooms()
  }
})

onMounted(async () => {
  await Promise.all([loadBeds(), loadRooms()])
})
</script>

<style scoped>
.selector-strip {
  display: grid;
  gap: 10px;
  margin-bottom: 12px;
}

.selector-group {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.selector-label {
  min-width: 40px;
  color: #475569;
  font-weight: 600;
}

.plan-stage {
  min-height: 420px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  background: linear-gradient(145deg, #f8fbff 0%, #ffffff 75%);
  padding: 12px;
}

.plan-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.room-block {
  border: 1px solid #dbeafe;
  border-radius: 10px;
  background: #ffffff;
  padding: 10px;
  box-shadow: 0 6px 14px rgba(15, 23, 42, 0.06);
}

.room-head {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
}

.room-title {
  font-weight: 700;
  color: #0f172a;
}

.room-type {
  font-size: 12px;
  color: #2563eb;
}

.room-meta {
  margin: 6px 0 8px;
  color: #64748b;
  font-size: 12px;
}

.bed-matrix {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
}

.bed-tile {
  border: 0;
  border-radius: 8px;
  padding: 8px 6px;
  text-align: left;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  position: relative;
}

.bed-no {
  font-weight: 700;
  font-size: 12px;
}

.bed-name {
  font-size: 12px;
  margin-top: 2px;
}

.empty-priority {
  box-shadow: inset 0 0 0 2px #22c55e;
}

.risk-corner {
  position: absolute;
  right: 4px;
  top: 4px;
  border-radius: 10px;
  padding: 1px 5px;
  font-size: 10px;
  line-height: 1.2;
}

.risk-high {
  background: #ef4444;
  color: #fff;
}

.risk-medium {
  background: #f59e0b;
  color: #fff;
}

.risk-low {
  background: #38bdf8;
  color: #fff;
}

.is-idle {
  background: #f3f4f6;
  color: #111827;
}

.is-reserved {
  background: #dbeafe;
  color: #1d4ed8;
}

.is-occupied {
  background: #dcfce7;
  color: #166534;
}

.is-maintenance {
  background: #ffedd5;
  color: #9a3412;
}

.is-cleaning {
  background: #cffafe;
  color: #0e7490;
}

.is-locked {
  background: #fee2e2;
  color: #991b1b;
}

@media (max-width: 1400px) {
  .plan-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 980px) {
  .plan-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .plan-grid {
    grid-template-columns: 1fr;
  }

  .room-block {
    grid-column: span 1 !important;
  }
}
</style>
