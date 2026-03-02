<template>
  <PageContainer title="床位全景" subTitle="楼号横向 + 楼层纵向二维图">
    <a-card class="card-elevated" :bordered="false">
      <a-form :model="query" layout="inline" class="search-bar">
        <a-form-item label="床位号">
          <a-input v-model:value="query.bedNo" placeholder="请输入床位号" allow-clear />
        </a-form-item>
        <a-form-item label="房间号">
          <a-input v-model:value="query.roomNo" placeholder="请输入房间号" allow-clear />
        </a-form-item>
        <a-form-item label="老人">
          <a-input v-model:value="query.elderName" placeholder="请输入老人姓名" allow-clear />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="query.status" allow-clear style="width: 120px">
            <a-select-option :value="1">空床</a-select-option>
            <a-select-option :value="2">入住</a-select-option>
            <a-select-option :value="3">维修</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="applySearch">搜索</a-button>
            <a-button @click="reset">清空</a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <a-row :gutter="[16, 16]" class="overview-cards">
        <a-col :xs="24" :sm="12" :md="8" :lg="4">
          <a-card size="small">
            <a-statistic title="楼栋" :value="buildingCount" />
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8" :lg="4">
          <a-card size="small">
            <a-statistic title="楼层" :value="floorCount" />
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8" :lg="4">
          <a-card size="small">
            <a-statistic title="房间" :value="roomCount" />
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8" :lg="4">
          <a-card size="small">
            <a-statistic title="床位" :value="filteredBeds.length" />
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8" :lg="4">
          <a-card size="small">
            <a-statistic title="入住老人" :value="occupiedBedsCount" />
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8" :lg="4">
          <a-card size="small">
            <a-statistic title="空闲床位" :value="idleBedsCount" />
          </a-card>
        </a-col>
      </a-row>

      <div class="view-switch">
        <a-radio-group v-model:value="viewMode" button-style="solid">
          <a-radio-button value="grid">二维楼层图</a-radio-button>
          <a-radio-button value="list">卡片列表</a-radio-button>
        </a-radio-group>
        <a-radio-group
          v-if="viewMode === 'grid'"
          v-model:value="matrixQuickFilter"
          size="small"
          class="matrix-filter-switch"
        >
          <a-radio-button value="all">全部</a-radio-button>
          <a-radio-button value="idle">仅空床</a-radio-button>
          <a-radio-button value="occupied">仅入住</a-radio-button>
        </a-radio-group>
      </div>

      <div v-if="viewMode === 'grid'" class="matrix-viewport">
        <div class="matrix-grid" :style="{ gridTemplateColumns: `130px repeat(${matrixColumnCount}, minmax(280px, 1fr))` }">
          <div class="matrix-corner">楼层 \\ 楼栋</div>
          <div v-for="building in matrixBuildings" :key="building.key" class="matrix-building-head">
            <div class="building-name">{{ building.name }}</div>
            <div class="building-kpi">{{ building.floors.length }} 层 · {{ building.roomCount }} 间 · {{ building.bedCount }} 床</div>
          </div>
          <template v-for="floor in matrixFloors" :key="floor">
            <div class="matrix-floor-axis">{{ floor }}</div>
            <div v-for="building in matrixBuildings" :key="`${building.key}-${floor}`" class="matrix-cell">
              <template v-if="roomsAt(building.name, floor).length">
                <div v-for="room in roomsAt(building.name, floor)" :key="room.key" class="room-cube">
                  <div class="room-title">{{ room.roomNo }}</div>
                  <div class="room-meta">{{ room.occupiedBeds }}/{{ room.totalBeds }} 床 · {{ room.elderCount }} 人</div>
                  <div class="bed-grid">
                    <button
                      v-for="bed in room.beds"
                      :key="bed.id"
                      type="button"
                      class="bed-pill"
                      :class="statusClass(bed.status, bed.elderId)"
                      @click="openBed(bed)"
                    >
                      {{ bed.bedNo }}
                    </button>
                  </div>
                </div>
              </template>
              <div v-else class="matrix-empty">-</div>
            </div>
          </template>
        </div>
      </div>

      <template v-else>
        <a-row :gutter="16">
          <a-col v-for="bed in pagedBeds" :key="bed.id" :xs="24" :sm="12" :md="8" :lg="6">
            <a-card class="bed-card" :hoverable="true" @click="openBed(bed)">
              <div class="bed-title">
                <span>{{ bed.bedNo }}</span>
                <a-tag :color="statusTag(bed.status, bed.elderId)">{{ statusText(bed.status, bed.elderId) }}</a-tag>
              </div>
              <div class="bed-meta">楼栋/楼层：{{ bed.building || '-' }} / {{ bed.floorNo || '-' }}</div>
              <div class="bed-meta">房间：{{ bed.roomNo || '-' }}</div>
              <div class="bed-meta">老人：{{ bed.elderName || '-' }}</div>
            </a-card>
          </a-col>
        </a-row>

        <a-pagination
          style="margin-top: 16px; text-align: right;"
          :current="query.pageNo"
          :page-size="query.pageSize"
          :total="filteredBeds.length"
          show-size-changer
          @change="onPageChange"
          @showSizeChange="onPageSizeChange"
        />
      </template>
    </a-card>

    <a-modal
      v-model:open="detailOpen"
      title="床位与老人详情"
      width="560px"
      @ok="printBedQr"
      ok-text="打印床位二维码"
      @cancel="() => (detailOpen = false)"
    >
      <a-descriptions bordered :column="2" size="small">
        <a-descriptions-item label="楼栋">{{ current?.building || '-' }}</a-descriptions-item>
        <a-descriptions-item label="楼层">{{ current?.floorNo || '-' }}</a-descriptions-item>
        <a-descriptions-item label="房间">{{ current?.roomNo || current?.roomId || '-' }}</a-descriptions-item>
        <a-descriptions-item label="床位">{{ current?.bedNo || '-' }}</a-descriptions-item>
        <a-descriptions-item label="状态">{{ statusText(current?.status, current?.elderId) }}</a-descriptions-item>
        <a-descriptions-item label="护理等级">{{ current?.careLevel || elderDetail?.careLevel || '-' }}</a-descriptions-item>
      </a-descriptions>

      <a-divider>老人信息</a-divider>
      <div v-if="elderLoading" class="modal-tip">加载中...</div>
      <div v-else-if="!current?.elderId" class="modal-tip">当前床位未入住</div>
      <a-descriptions v-else bordered :column="2" size="small">
        <a-descriptions-item label="姓名">{{ elderDetail?.fullName || current?.elderName || '-' }}</a-descriptions-item>
        <a-descriptions-item label="老人编号">{{ elderDetail?.elderCode || '-' }}</a-descriptions-item>
        <a-descriptions-item label="性别">{{ genderText(elderDetail?.gender) }}</a-descriptions-item>
        <a-descriptions-item label="电话">{{ elderDetail?.phone || '-' }}</a-descriptions-item>
        <a-descriptions-item label="身份证">{{ elderDetail?.idCardNo || elderDetail?.idCard || '-' }}</a-descriptions-item>
        <a-descriptions-item label="入住日期">{{ elderDetail?.admissionDate || elderDetail?.checkInDate || '-' }}</a-descriptions-item>
        <a-descriptions-item label="护理等级">{{ elderDetail?.careLevel || '-' }}</a-descriptions-item>
        <a-descriptions-item label="备注">{{ elderDetail?.remark || '-' }}</a-descriptions-item>
      </a-descriptions>

      <div class="qr-preview" v-if="qrDataUrl">
        <img :src="qrDataUrl" alt="qr" />
      </div>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import QRCode from 'qrcode'
import PageContainer from '../../components/PageContainer.vue'
import { getBedMap } from '../../api/bed'
import { getElderDetail } from '../../api/elder'
import type { BedItem, ElderItem } from '../../types/api'

type RoomScene = {
  key: string
  roomNo: string
  beds: BedItem[]
  totalBeds: number
  occupiedBeds: number
  elderCount: number
}

type FloorScene = {
  key: string
  label: string
  sort: number
  rooms: RoomScene[]
}

type BuildingScene = {
  key: string
  name: string
  floors: FloorScene[]
  roomCount: number
  bedCount: number
}

const beds = ref<BedItem[]>([])
const detailOpen = ref(false)
const current = ref<BedItem | null>(null)
const qrDataUrl = ref('')
const elderLoading = ref(false)
const elderDetail = ref<ElderItem | null>(null)
const viewMode = ref<'grid' | 'list'>('grid')
const matrixQuickFilter = ref<'all' | 'idle' | 'occupied'>('all')

const query = reactive({
  bedNo: '',
  roomNo: '',
  elderName: '',
  status: undefined as number | undefined,
  pageNo: 1,
  pageSize: 12
})

const filteredBeds = computed(() => {
  return beds.value.filter((bed) => {
    if (query.bedNo && !String(bed.bedNo || '').includes(query.bedNo)) return false
    if (query.roomNo && !String(bed.roomNo || '').includes(query.roomNo)) return false
    if (query.elderName && !String(bed.elderName || '').includes(query.elderName)) return false
    if (query.status && bed.status !== query.status) return false
    return true
  })
})

const buildingScenes = computed<BuildingScene[]>(() => {
  const buildingMap = new Map<string, Map<string, Map<string, BedItem[]>>>()
  filteredBeds.value.forEach((bed) => {
    const building = (bed.building || '未分配楼栋').trim() || '未分配楼栋'
    const floor = (bed.floorNo || '未知楼层').trim() || '未知楼层'
    const roomNo = (bed.roomNo || `房间-${bed.roomId || '-'}`).trim() || `房间-${bed.roomId || '-'}`

    if (!buildingMap.has(building)) buildingMap.set(building, new Map())
    const floorMap = buildingMap.get(building)!
    if (!floorMap.has(floor)) floorMap.set(floor, new Map())
    const roomMap = floorMap.get(floor)!
    if (!roomMap.has(roomNo)) roomMap.set(roomNo, [])
    roomMap.get(roomNo)!.push(bed)
  })

  return Array.from(buildingMap.entries()).map(([buildingName, floorMap]) => {
    const floors: FloorScene[] = Array.from(floorMap.entries())
      .map(([floorNo, roomMap]) => {
        const rooms: RoomScene[] = Array.from(roomMap.entries())
          .map(([roomNo, roomBeds]) => {
            const occupiedBeds = roomBeds.filter((bed) => isOccupiedBed(bed)).length
            return {
              key: `${buildingName}-${floorNo}-${roomNo}`,
              roomNo,
              beds: roomBeds.sort((a, b) => String(a.bedNo || '').localeCompare(String(b.bedNo || ''), 'zh-CN')),
              totalBeds: roomBeds.length,
              occupiedBeds,
              elderCount: roomBeds.filter((bed) => !!bed.elderId).length
            }
          })
          .sort((a, b) => a.roomNo.localeCompare(b.roomNo, 'zh-CN'))

        return {
          key: `${buildingName}-${floorNo}`,
          label: floorNo,
          sort: floorSortValue(floorNo),
          rooms
        }
      })
      .sort((a, b) => b.sort - a.sort)

    return {
      key: buildingName,
      name: buildingName,
      floors,
      roomCount: floors.reduce((sum, floor) => sum + floor.rooms.length, 0),
      bedCount: floors.reduce((sum, floor) => sum + floor.rooms.reduce((acc, room) => acc + room.totalBeds, 0), 0)
    }
  }).sort((a, b) => a.name.localeCompare(b.name, 'zh-CN'))
})

const buildingCount = computed(() => buildingScenes.value.length)
const floorCount = computed(() => buildingScenes.value.reduce((sum, building) => sum + building.floors.length, 0))
const roomCount = computed(() => buildingScenes.value.reduce((sum, building) => sum + building.roomCount, 0))
const occupiedBedsCount = computed(() => filteredBeds.value.filter((bed) => isOccupiedBed(bed)).length)
const idleBedsCount = computed(() => filteredBeds.value.filter((bed) => isIdleBed(bed)).length)
const matrixBuildings = computed(() => buildingScenes.value)
const matrixColumnCount = computed(() => Math.max(matrixBuildings.value.length, 1))
const matrixFloors = computed(() => {
  const floorSet = new Set<string>()
  buildingScenes.value.forEach((building) => {
    building.floors.forEach((floor) => floorSet.add(floor.label))
  })
  return Array.from(floorSet).sort((a, b) => {
    const diff = floorSortValue(b) - floorSortValue(a)
    if (diff !== 0) return diff
    return a.localeCompare(b, 'zh-CN')
  })
})
const roomLookup = computed(() => {
  const map = new Map<string, Map<string, RoomScene[]>>()
  buildingScenes.value.forEach((building) => {
    const floorMap = new Map<string, RoomScene[]>()
    building.floors.forEach((floor) => {
      floorMap.set(floor.label, floor.rooms)
    })
    map.set(building.name, floorMap)
  })
  return map
})
const matrixRoomLookup = computed(() => {
  if (matrixQuickFilter.value === 'all') return roomLookup.value
  const map = new Map<string, Map<string, RoomScene[]>>()
  roomLookup.value.forEach((floorMap, buildingName) => {
    const nextFloorMap = new Map<string, RoomScene[]>()
    floorMap.forEach((rooms, floorLabel) => {
      const nextRooms = rooms
        .map((room) => {
          const nextBeds = room.beds.filter((bed) => matchMatrixFilter(bed))
          if (!nextBeds.length) return null
          const occupiedBeds = nextBeds.filter((bed) => isOccupiedBed(bed)).length
          return {
            ...room,
            beds: nextBeds,
            totalBeds: nextBeds.length,
            occupiedBeds,
            elderCount: nextBeds.filter((bed) => !!bed.elderId).length
          }
        })
        .filter((room): room is RoomScene => room !== null)
      nextFloorMap.set(floorLabel, nextRooms)
    })
    map.set(buildingName, nextFloorMap)
  })
  return map
})

const pagedBeds = computed(() => {
  const start = (query.pageNo - 1) * query.pageSize
  return filteredBeds.value.slice(start, start + query.pageSize)
})

function floorSortValue(text: string) {
  const raw = String(text || '').trim()
  if (!raw) return -999
  const normalized = raw.replace(/\s+/g, '').toUpperCase()
  if (/^(ROOF|RF|屋顶|天台)$/.test(normalized)) return 999

  const basement = normalized.match(/(?:地下|负|B)([0-9]+|[一二三四五六七八九十百两]+)/)
  if (basement) return -parseFloorToken(basement[1])

  const match = normalized.match(/([0-9]+|[一二三四五六七八九十百两]+)(?:F|楼|层)?/)
  if (match) return parseFloorToken(match[1])

  if (/夹层|M/.test(normalized)) return 0.5
  return -999
}

function parseFloorToken(token: string) {
  if (/^\d+$/.test(token)) return Number(token)
  const value = parseChineseNumber(token)
  return value > 0 ? value : 0
}

function parseChineseNumber(text: string) {
  const chars = text.split('')
  const digitMap: Record<string, number> = {
    零: 0,
    一: 1,
    二: 2,
    两: 2,
    三: 3,
    四: 4,
    五: 5,
    六: 6,
    七: 7,
    八: 8,
    九: 9
  }
  if (!chars.length) return 0
  if (chars.length === 1) return digitMap[chars[0]] ?? 0

  let result = 0
  let section = 0
  let number = 0
  chars.forEach((char) => {
    if (digitMap[char] !== undefined) {
      number = digitMap[char]
      return
    }
    if (char === '十') {
      section += (number || 1) * 10
      number = 0
      return
    }
    if (char === '百') {
      section += (number || 1) * 100
      number = 0
    }
  })
  result += section + number
  return result
}

function isOccupiedBed(bed: BedItem) {
  return Boolean(bed.elderId) || bed.status === 2
}

function isIdleBed(bed: BedItem) {
  return !isOccupiedBed(bed) && (bed.status === 1 || bed.status === undefined)
}

function statusText(status?: number, elderId?: number) {
  if (status === 2) return '占用'
  if (status === 3) return '维修'
  if (elderId) return '入住'
  return '空床'
}

function statusTag(status?: number, elderId?: number) {
  if (status === 2) return 'orange'
  if (status === 3) return 'red'
  if (elderId) return 'blue'
  return 'green'
}

function statusClass(status?: number, elderId?: number) {
  if (status === 3) return 'is-maintain'
  if (status === 2 || elderId) return 'is-occupied'
  return 'is-idle'
}

function genderText(gender?: number) {
  if (gender === 1) return '男'
  if (gender === 2) return '女'
  return '-'
}

async function load() {
  try {
    beds.value = await getBedMap()
  } catch {
    beds.value = []
  }
}

function applySearch() {
  query.pageNo = 1
}

function reset() {
  query.bedNo = ''
  query.roomNo = ''
  query.elderName = ''
  query.status = undefined
  query.pageNo = 1
}

function onPageChange(page: number) {
  query.pageNo = page
}

function onPageSizeChange(_current: number, size: number) {
  query.pageNo = 1
  query.pageSize = size
}

function roomsAt(building: string, floor: string) {
  return matrixRoomLookup.value.get(building)?.get(floor) || []
}

function matchMatrixFilter(bed: BedItem) {
  if (matrixQuickFilter.value === 'idle') return isIdleBed(bed)
  if (matrixQuickFilter.value === 'occupied') return isOccupiedBed(bed)
  return true
}

async function openBed(bed: BedItem) {
  current.value = bed
  detailOpen.value = true
  elderDetail.value = null
  elderLoading.value = false

  qrDataUrl.value = await QRCode.toDataURL(bed.bedQrCode || `BED:${bed.id}`)

  if (!bed.elderId) return
  elderLoading.value = true
  try {
    elderDetail.value = await getElderDetail(bed.elderId)
  } catch {
    elderDetail.value = null
    message.warning('加载老人详情失败')
  } finally {
    elderLoading.value = false
  }
}

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

onMounted(load)
</script>

<style scoped>
.search-bar {
  margin-bottom: 12px;
}

.overview-cards {
  margin-bottom: 16px;
}

.view-switch {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.matrix-filter-switch {
  margin-left: auto;
}

.matrix-viewport {
  overflow-x: auto;
  padding: 4px 0 20px;
}

.building-name {
  font-weight: 700;
  font-size: 16px;
  color: #1f2a44;
}

.building-kpi {
  color: #5f6f8f;
  font-size: 12px;
}

.matrix-grid {
  display: grid;
  min-width: max-content;
  border: 1px solid #d5dff2;
  border-radius: 12px;
  overflow: hidden;
  background: #fff;
}

.matrix-corner {
  position: sticky;
  left: 0;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  color: #31456d;
  background: #f2f6ff;
  border-right: 1px solid #d5dff2;
  border-bottom: 1px solid #d5dff2;
  min-height: 68px;
}

.matrix-building-head {
  padding: 10px 12px;
  border-bottom: 1px solid #d5dff2;
  border-right: 1px solid #e3eaf8;
  background: #f7faff;
}

.matrix-floor-axis {
  position: sticky;
  left: 0;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  color: #31456d;
  background: #f7faff;
  border-right: 1px solid #d5dff2;
  border-bottom: 1px solid #e3eaf8;
  min-height: 120px;
}

.matrix-cell {
  padding: 10px;
  border-right: 1px solid #e3eaf8;
  border-bottom: 1px solid #e3eaf8;
  min-height: 120px;
  background: #fbfdff;
  display: grid;
  gap: 8px;
}

.matrix-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #9aa8c8;
  font-size: 12px;
  min-height: 48px;
}

.room-cube {
  border: 1px solid #cad8f5;
  border-radius: 10px;
  padding: 8px;
  background: #ffffff;
  box-shadow: inset 0 1px 0 #ffffff, 0 6px 10px rgba(42, 87, 166, 0.08);
}

.room-title {
  font-weight: 600;
  font-size: 13px;
  color: #223457;
}

.room-meta {
  font-size: 11px;
  color: #6b7ea5;
  margin-top: 2px;
  margin-bottom: 6px;
}

.bed-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.bed-pill {
  border: none;
  border-radius: 14px;
  height: 24px;
  padding: 0 8px;
  font-size: 11px;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.bed-pill:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 10px rgba(24, 44, 82, 0.2);
}

.bed-pill.is-occupied {
  color: #fff;
  background: linear-gradient(135deg, #2270f8, #2ea0ff);
}

.bed-pill.is-idle {
  color: #1a7d2c;
  background: #e8f8ec;
}

.bed-pill.is-maintain {
  color: #fff;
  background: linear-gradient(135deg, #e95f3b, #f28c52);
}

.bed-card {
  margin-bottom: 16px;
  cursor: pointer;
}

.bed-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.bed-meta {
  color: var(--muted);
  font-size: 12px;
  margin-top: 6px;
}

.modal-tip {
  color: var(--muted);
}

.qr-preview {
  display: grid;
  justify-items: center;
  margin-top: 12px;
}

@media (max-width: 768px) {
  .view-switch {
    justify-content: flex-start;
  }

  .matrix-filter-switch {
    margin-left: 0;
  }

  .matrix-grid {
    grid-auto-rows: minmax(96px, auto);
  }
}
</style>
