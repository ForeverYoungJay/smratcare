<template>
  <PageContainer title="房态全景" sub-title="营销视角：可售房源、在住占用、楼栋楼层一图查看">
    <a-row :gutter="16">
      <a-col :xs="24" :sm="8">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="总床位" :value="scopedBeds.length" />
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="8">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="入住床位" :value="occupiedCount" />
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="8">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="空床率" :value="freeRate" suffix="%" />
        </a-card>
      </a-col>
    </a-row>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px">
      <a-form :model="query" layout="inline" class="search-bar">
        <a-form-item label="房间号">
          <a-input v-model:value="query.roomNo" placeholder="房间号" allow-clear />
        </a-form-item>
        <a-form-item label="长者">
          <a-input v-model:value="query.elderName" placeholder="姓名" allow-clear />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="query.status" allow-clear style="width: 140px">
            <a-select-option :value="1">空床</a-select-option>
            <a-select-option :value="2">入住</a-select-option>
            <a-select-option :value="3">维修</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="房型">
          <a-select v-model:value="query.roomType" allow-clear style="width: 160px" placeholder="全部房型">
            <a-select-option v-for="item in roomTypeOptions" :key="item.value" :value="item.value">{{ item.label }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="applySearch">筛选</a-button>
            <a-button @click="resetSearch">重置</a-button>
            <a-button @click="openAssetMap">运营房态图</a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <div class="matrix-selection-bar">
        <a-space wrap>
          <a-tag color="blue" v-if="selectedBuilding">楼栋：{{ selectedBuilding }}</a-tag>
          <a-tag color="cyan" v-if="selectedFloor">楼层：{{ selectedFloor }}</a-tag>
          <span v-if="!selectedBuilding && !selectedFloor" class="matrix-tip">点击楼栋/楼层可聚焦可售区块</span>
          <a-button size="small" v-if="selectedBuilding || selectedFloor" @click="clearSelection">清除筛选</a-button>
        </a-space>
      </div>

      <div class="matrix-viewport" v-if="matrixBuildings.length && matrixFloors.length">
        <div class="matrix-grid" :style="{ gridTemplateColumns: `120px repeat(${Math.max(matrixBuildings.length, 1)}, minmax(260px, 1fr))` }">
          <div class="matrix-corner">楼层 \\ 楼栋</div>
          <button
            v-for="building in matrixBuildings"
            :key="building"
            type="button"
            class="matrix-building-head"
            :class="{ active: selectedBuilding === building }"
            @click="toggleBuilding(building)"
          >
            {{ building }}
          </button>

          <template v-for="floor in matrixFloors" :key="floor">
            <button
              type="button"
              class="matrix-floor-axis"
              :class="{ active: selectedFloor === floor }"
              @click="toggleFloor(floor)"
            >
              {{ floor }}
            </button>
            <div v-for="building in matrixBuildings" :key="`${building}-${floor}`" class="matrix-cell">
              <template v-if="roomsAt(building, floor).length">
                <div v-for="room in roomsAt(building, floor)" :key="room.key" class="room-cube">
                  <div class="room-title">{{ room.roomNo }}</div>
                  <div class="room-meta">{{ room.roomTypeLabel }} · {{ room.capacity }} 床</div>
                  <div class="room-meta">{{ room.occupied }}/{{ room.total }} 床 · 空床 {{ room.total - room.occupied }}</div>
                  <div class="bed-grid">
                    <span
                      v-for="bed in room.beds"
                      :key="bed.id"
                      class="bed-pill"
                      :class="statusClass(bed)"
                    >
                      {{ bed.bedNo }}
                    </span>
                  </div>
                </div>
              </template>
              <div v-else class="matrix-empty">-</div>
            </div>
          </template>
        </div>
      </div>
      <a-empty v-else description="暂无房态数据" />

      <a-divider />
      <a-table
        :columns="columns"
        :data-source="listRows"
        :loading="loading"
        row-key="id"
        :pagination="{ pageSize: 12 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="statusColor(record)">{{ statusText(record) }}</a-tag>
          </template>
        </template>
      </a-table>
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import { getBedMap, getRoomList } from '../../api/bed'
import { getBaseConfigItemList } from '../../api/baseConfig'
import { useLiveSyncRefresh } from '../../composables/useLiveSyncRefresh'
import type { BaseConfigItem, BedItem, RoomItem } from '../../types'

type RoomScene = {
  key: string
  roomNo: string
  roomTypeLabel: string
  capacity: number
  beds: BedItem[]
  total: number
  occupied: number
}

const router = useRouter()
const loading = ref(false)
const rows = ref<BedItem[]>([])
const rooms = ref<RoomItem[]>([])
const roomTypeItems = ref<BaseConfigItem[]>([])
const selectedBuilding = ref('')
const selectedFloor = ref('')
const query = reactive({
  roomNo: '',
  elderName: '',
  status: undefined as number | undefined,
  roomType: undefined as string | undefined
})

const columns = [
  { title: '楼栋', dataIndex: 'building', key: 'building', width: 120 },
  { title: '楼层', dataIndex: 'floorNo', key: 'floorNo', width: 100 },
  { title: '房间号', dataIndex: 'roomNo', key: 'roomNo', width: 120 },
  { title: '房型', dataIndex: 'roomTypeLabel', key: 'roomTypeLabel', width: 120 },
  { title: '容量', dataIndex: 'capacity', key: 'capacity', width: 90 },
  { title: '床位号', dataIndex: 'bedNo', key: 'bedNo', width: 120 },
  { title: '长者', dataIndex: 'elderName', key: 'elderName', width: 140 },
  { title: '护理等级', dataIndex: 'careLevel', key: 'careLevel', width: 120 },
  { title: '状态', key: 'status', width: 100 }
]

const roomTypeOptions = computed(() => {
  const map = new Map<string, string>()
  roomTypeItems.value.forEach((item) => {
    map.set(item.itemName, item.itemName)
  })
  ;['单人间', '双人间', '三人间', '标准间'].forEach((label) => map.set(label, label))
  return Array.from(map.values()).map((label) => ({ label, value: label }))
})

const filteredRows = computed(() => rows.value.filter((item) => {
  if (query.roomNo && !(item.roomNo || '').includes(query.roomNo)) return false
  if (query.elderName && !(item.elderName || '').includes(query.elderName)) return false
  if (query.status && item.status !== query.status) return false
  if (query.roomType) {
    const room = roomMetaMap.value.get(String(item.roomId || ''))
    const capacity = Number(room?.capacity || 0)
    if (resolveRoomTypeLabel(item.roomType, capacity) !== query.roomType) return false
  }
  return true
}))

const scopedBeds = computed(() => filteredRows.value.filter((item) => {
  if (selectedBuilding.value && String(item.building || '') !== selectedBuilding.value) return false
  if (selectedFloor.value && String(item.floorNo || '') !== selectedFloor.value) return false
  return true
}))

const roomMetaMap = computed(() => {
  const map = new Map<string, RoomItem>()
  rooms.value.forEach((room) => {
    map.set(String(room.id), room)
  })
  return map
})

const roomTypeLabelMap = computed(() =>
  roomTypeItems.value.reduce((acc, item) => {
    acc[item.itemCode] = item.itemName
    return acc
  }, {} as Record<string, string>)
)

const listRows = computed(() =>
  scopedBeds.value.map((item) => {
    const room = roomMetaMap.value.get(String(item.roomId || ''))
    const capacity = Number(room?.capacity || 0)
    return {
      ...item,
      roomTypeLabel: resolveRoomTypeLabel(item.roomType, capacity),
      capacity: capacity || '-'
    }
  })
)

const occupiedCount = computed(() => scopedBeds.value.filter((item) => item.status === 2 || !!item.elderId).length)
const freeRate = computed(() => {
  const total = scopedBeds.value.length
  if (!total) return 0
  return Number((((total - occupiedCount.value) / total) * 100).toFixed(1))
})

const matrixBuildings = computed(() => {
  const set = new Set<string>()
  scopedBeds.value.forEach((item) => set.add((item.building || '未分配楼栋').trim() || '未分配楼栋'))
  return Array.from(set).sort((a, b) => a.localeCompare(b, 'zh-CN'))
})

const matrixFloors = computed(() => {
  const set = new Set<string>()
  scopedBeds.value.forEach((item) => set.add((item.floorNo || '未知楼层').trim() || '未知楼层'))
  return Array.from(set).sort((a, b) => floorSortValue(b) - floorSortValue(a) || a.localeCompare(b, 'zh-CN'))
})

const roomLookup = computed(() => {
  const lookup = new Map<string, RoomScene[]>()
  const bucket = new Map<string, BedItem[]>()

  scopedBeds.value.forEach((bed) => {
    const building = (bed.building || '未分配楼栋').trim() || '未分配楼栋'
    const floor = (bed.floorNo || '未知楼层').trim() || '未知楼层'
    const roomNo = (bed.roomNo || `房间-${bed.roomId || '-'}`).trim() || `房间-${bed.roomId || '-'}`
    const key = `${building}@@${floor}@@${roomNo}`
    if (!bucket.has(key)) bucket.set(key, [])
    bucket.get(key)!.push(bed)
  })

  bucket.forEach((beds, key) => {
    const [building, floor, roomNo] = key.split('@@')
    const cellKey = `${building}@@${floor}`
    if (!lookup.has(cellKey)) lookup.set(cellKey, [])
    const sortedBeds = [...beds].sort((a, b) => String(a.bedNo || '').localeCompare(String(b.bedNo || ''), 'zh-CN'))
    const room = roomMetaMap.value.get(String(sortedBeds[0]?.roomId || ''))
    const capacity = Number(room?.capacity || sortedBeds.length || 1)
    lookup.get(cellKey)!.push({
      key,
      roomNo,
      roomTypeLabel: resolveRoomTypeLabel(sortedBeds[0]?.roomType, capacity),
      capacity,
      beds: sortedBeds,
      total: sortedBeds.length,
      occupied: sortedBeds.filter((bed) => bed.status === 2 || !!bed.elderId).length
    })
  })

  lookup.forEach((rooms) => {
    rooms.sort((a, b) => a.roomNo.localeCompare(b.roomNo, 'zh-CN'))
  })

  return lookup
})

function roomsAt(building: string, floor: string) {
  return roomLookup.value.get(`${building}@@${floor}`) || []
}

function floorSortValue(text: string) {
  const raw = String(text || '').trim()
  const match = raw.match(/\d+/)
  return match ? Number(match[0]) : -999
}

function statusText(item: BedItem) {
  if (item.status === 3) return '维修'
  if (item.status === 2 || item.elderId) return '入住'
  return '空床'
}

function statusColor(item: BedItem) {
  if (item.status === 3) return 'red'
  if (item.status === 2 || item.elderId) return 'blue'
  return 'green'
}

function statusClass(item: BedItem) {
  if (item.status === 3) return 'is-maintain'
  if (item.status === 2 || item.elderId) return 'is-occupied'
  return 'is-idle'
}

function applySearch() {
  // no-op, computed filtering updates immediately
}

function resetSearch() {
  query.roomNo = ''
  query.elderName = ''
  query.status = undefined
  query.roomType = undefined
  clearSelection()
}

function toggleBuilding(building: string) {
  selectedBuilding.value = selectedBuilding.value === building ? '' : building
}

function toggleFloor(floor: string) {
  selectedFloor.value = selectedFloor.value === floor ? '' : floor
}

function clearSelection() {
  selectedBuilding.value = ''
  selectedFloor.value = ''
}

function resolveRoomTypeLabel(roomType?: string, capacity?: number) {
  if (roomType && roomTypeLabelMap.value[roomType]) return roomTypeLabelMap.value[roomType]
  if (capacity === 1) return '单人间'
  if (capacity === 2) return '双人间'
  if (capacity === 3) return '三人间'
  return roomType || '标准间'
}

function openAssetMap() {
  router.push('/bed/map')
}

async function fetchData() {
  loading.value = true
  try {
    const [bedRows, roomRows, roomTypes] = await Promise.all([
      getBedMap(),
      getRoomList(),
      getBaseConfigItemList({ configGroup: 'ADMISSION_ROOM_TYPE', status: 1 })
    ])
    rows.value = bedRows
    rooms.value = roomRows
    roomTypeItems.value = roomTypes
  } catch {
    rows.value = []
    rooms.value = []
    roomTypeItems.value = []
  } finally {
    loading.value = false
  }
}

watch(filteredRows, () => {
  if (selectedBuilding.value && !filteredRows.value.some((item) => String(item.building || '') === selectedBuilding.value)) {
    selectedBuilding.value = ''
  }
  if (selectedFloor.value && !filteredRows.value.some((item) => String(item.floorNo || '') === selectedFloor.value)) {
    selectedFloor.value = ''
  }
})

useLiveSyncRefresh({
  topics: ['bed', 'elder'],
  refresh: fetchData
})

onMounted(fetchData)
</script>

<style scoped>
.search-bar {
  margin-bottom: 12px;
}

.matrix-selection-bar {
  margin-bottom: 12px;
}

.matrix-tip {
  color: #6b7280;
}

.matrix-viewport {
  overflow-x: auto;
  margin-bottom: 12px;
}

.matrix-grid {
  display: grid;
  min-width: max-content;
  border: 1px solid #dbe5f5;
  border-radius: 10px;
  overflow: hidden;
  background: #fff;
}

.matrix-corner {
  position: sticky;
  left: 0;
  z-index: 3;
  min-height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  color: #334155;
  background: #f1f5f9;
  border-right: 1px solid #dbe5f5;
  border-bottom: 1px solid #dbe5f5;
}

.matrix-building-head {
  appearance: none;
  border: 0;
  width: 100%;
  min-height: 64px;
  text-align: left;
  padding: 10px 12px;
  font-weight: 700;
  color: #0f172a;
  background: #f8fafc;
  border-right: 1px solid #dbe5f5;
  border-bottom: 1px solid #dbe5f5;
  cursor: pointer;
}

.matrix-building-head.active {
  background: #dbeafe;
}

.matrix-floor-axis {
  appearance: none;
  border: 0;
  width: 100%;
  position: sticky;
  left: 0;
  z-index: 2;
  min-height: 220px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  color: #334155;
  background: #f8fafc;
  border-right: 1px solid #dbe5f5;
  border-bottom: 1px solid #dbe5f5;
  cursor: pointer;
}

.matrix-floor-axis.active {
  background: #dbeafe;
  color: #1d4ed8;
}

.matrix-cell {
  min-height: 220px;
  max-height: 220px;
  overflow: auto;
  padding: 8px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(130px, 1fr));
  align-content: start;
  gap: 8px;
  border-right: 1px solid #dbe5f5;
  border-bottom: 1px solid #dbe5f5;
  background: #fbfdff;
}

.matrix-empty {
  min-height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
}

.room-cube {
  border: 1px solid #cad8f5;
  border-radius: 10px;
  background: #fff;
  padding: 6px;
}

.room-title {
  font-weight: 700;
  color: #1e293b;
}

.room-meta {
  margin-top: 2px;
  margin-bottom: 6px;
  font-size: 11px;
  color: #64748b;
}

.bed-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.bed-pill {
  height: 22px;
  padding: 0 8px;
  border-radius: 12px;
  display: inline-flex;
  align-items: center;
  font-size: 11px;
}

.bed-pill.is-occupied {
  color: #fff;
  background: linear-gradient(135deg, #2270f8, #2ea0ff);
}

.bed-pill.is-idle {
  color: #166534;
  background: #dcfce7;
}

.bed-pill.is-maintain {
  color: #fff;
  background: linear-gradient(135deg, #e95f3b, #f28c52);
}
</style>
