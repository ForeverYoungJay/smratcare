<template>
  <PageContainer :title="pageTitle" :subTitle="pageSubTitle">
    <a-card class="card-elevated" :bordered="false">
      <a-form :model="query" layout="inline" class="search-bar">
        <a-form-item label="床位号">
          <a-input v-model:value="query.bedNo" placeholder="请输入床位号" allow-clear />
        </a-form-item>
        <a-form-item label="房间号">
          <a-input v-model:value="query.roomNo" placeholder="请输入房间号" allow-clear />
        </a-form-item>
        <a-form-item label="老人">
          <ElderNameAutocomplete v-model:value="query.elderName" placeholder="请输入老人姓名(编号)" width="220px" @select="applySearch" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="query.status" allow-clear style="width: 120px">
            <a-select-option :value="1">空床</a-select-option>
            <a-select-option :value="2">入住</a-select-option>
            <a-select-option :value="3">维修</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="区域">
          <a-select v-model:value="query.areaCode" allow-clear show-search option-filter-prop="label" style="width: 160px" placeholder="区域">
            <a-select-option v-for="item in areaOptions" :key="item.value" :value="item.value" :label="item.label">{{ item.label }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="房型">
          <a-select v-model:value="query.roomType" allow-clear show-search option-filter-prop="label" style="width: 160px" placeholder="房型">
            <a-select-option v-for="item in roomTypeOptions" :key="item.value" :value="item.value" :label="item.label">{{ item.label }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="床型">
          <a-select v-model:value="query.bedType" allow-clear show-search option-filter-prop="label" style="width: 160px" placeholder="床型">
            <a-select-option v-for="item in bedTypeOptions" :key="item.value" :value="item.value" :label="item.label">{{ item.label }}</a-select-option>
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
          <a-card size="small"><a-statistic title="楼栋" :value="buildingCount" /></a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8" :lg="4">
          <a-card size="small"><a-statistic title="楼层" :value="floorCount" /></a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8" :lg="4">
          <a-card size="small"><a-statistic title="房间" :value="roomCount" /></a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8" :lg="4">
          <a-card size="small"><a-statistic title="床位" :value="scopedBeds.length" /></a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8" :lg="4">
          <a-card size="small"><a-statistic title="入住老人" :value="occupiedBedsCount" /></a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8" :lg="4">
          <a-card size="small"><a-statistic title="空闲床位" :value="idleBedsCount" /></a-card>
        </a-col>
      </a-row>

      <div class="view-switch">
        <a-space wrap>
          <a-radio-group v-model:value="viewMode" button-style="solid">
            <a-radio-button value="grid">楼栋分层图</a-radio-button>
            <a-radio-button value="list">卡片列表</a-radio-button>
          </a-radio-group>
          <a-button @click="openBedManage">床位管理</a-button>
          <a-button v-if="!isMarketingMode" type="primary" @click="openElderBedPanorama">后勤房态图入口</a-button>
        </a-space>
        <a-radio-group v-if="viewMode === 'grid'" v-model:value="matrixQuickFilter" size="small" class="matrix-filter-switch">
          <a-radio-button value="all">全部</a-radio-button>
          <a-radio-button value="idle">仅空床</a-radio-button>
          <a-radio-button value="occupied">仅入住</a-radio-button>
        </a-radio-group>
      </div>

      <div v-if="viewMode === 'grid'" class="matrix-selection-bar">
        <a-space wrap>
          <a-tag color="blue" v-if="selectedBuilding">楼栋：{{ selectedBuilding }}</a-tag>
          <a-tag color="cyan" v-if="selectedFloor">楼层：{{ selectedFloor }}</a-tag>
          <span v-if="!selectedBuilding && !selectedFloor" class="matrix-tip">可点击楼栋头部与楼层条快速聚焦，不同楼层结构不会再混排</span>
          <a-button size="small" v-if="selectedBuilding || selectedFloor" @click="clearMatrixSelection">清除楼层筛选</a-button>
        </a-space>
      </div>

      <div v-if="viewMode === 'grid'" class="tower-board">
        <a-empty v-if="!displayBuildings.length" description="当前筛选下暂无房态数据" />
        <div v-else class="tower-grid">
          <section v-for="building in displayBuildings" :key="building.key" class="tower-building" :class="{ active: selectedBuilding === building.name }">
            <button type="button" class="tower-building-head" @click="toggleBuilding(building.name)">
              <div>
                <div class="building-name">{{ building.name }}</div>
                <div class="building-kpi">{{ building.floors.length }} 层 · {{ building.roomCount }} 间 · {{ building.bedCount }} 床</div>
              </div>
              <div class="building-trend">按楼独立加载</div>
              <div v-if="resolveVisibleRemark(building.remark)" class="building-remark">{{ resolveVisibleRemark(building.remark) }}</div>
            </button>
            <div class="tower-building-body">
              <div v-for="floor in building.floors" :key="floor.key" class="tower-floor" :class="{ active: selectedFloor === floor.label }">
                <button type="button" class="tower-floor-badge" @click="toggleFloor(floor.label)">
                  <span>{{ floor.label }}</span>
                  <small>{{ floor.rooms.length }} 间</small>
                </button>
                <div class="tower-floor-content">
                  <div v-for="room in floor.rooms" :key="room.key" class="room-cube" @dblclick="openRoomSceneDetail(room)">
                    <div class="room-head">
                      <div class="room-title">{{ room.roomNo }}</div>
                      <div class="room-meta">{{ room.occupiedBeds }}/{{ room.totalBeds }} 床 · {{ room.elderCount }} 人</div>
                    </div>
                    <div v-if="resolveVisibleRemark(room.remark)" class="room-remark">{{ resolveVisibleRemark(room.remark) }}</div>
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
                </div>
              </div>
            </div>
          </section>
        </div>
      </div>

      <template v-else>
        <a-row :gutter="16">
          <a-col v-for="bed in pagedBeds" :key="bed.id" :xs="24" :sm="12" :md="8" :lg="6" :xl="4">
            <div class="bed-info-card" :class="[isBedAlert(bed) ? 'is-alert' : '']" @click="openBed(bed)">
              <div class="bed-card-header">
                <div class="bed-card-title">
                  <span class="bed-icon">💡</span>
                  <span class="elder-name">{{ bed.elderName || '空床' }}</span>
                  <span class="room-bed-no">— {{ bed.roomNo || '-' }} — {{ bed.bedNo }}</span>
                </div>
                <div class="bed-card-more">•••</div>
              </div>
              <div class="bed-card-body">
                <div class="status-graphic">
                  <div class="graphic-bed" :class="{'occupied': bed.elderId, 'empty': !bed.elderId, 'alert': isBedAlert(bed)}"></div>
                  <div class="graphic-wave"></div>
                </div>
                <div class="status-text" :class="isBedAlert(bed) ? 'text-alert' : ''">{{ bedStatusText(bed) }}</div>
              </div>
              <div class="bed-card-footer">
                <div class="vitals">
                  <span class="vital-item">❤️ {{ getHeartRate(bed) }}</span>
                  <span class="vital-item">🫁 {{ getBreathRate(bed) }}</span>
                  <span class="vital-item wave-anim" v-if="bed.elderId">||||||||||</span>
                </div>
                <div class="status-badge" :class="bedBadgeClass(bed)">{{ bedBadgeText(bed) }}</div>
              </div>
            </div>
          </a-col>
        </a-row>

        <a-pagination
          style="margin-top: 16px; text-align: right;"
          :current="query.pageNo"
          :page-size="query.pageSize"
          :total="scopedBeds.length"
          show-size-changer
          @change="onPageChange"
          @showSizeChange="onPageSizeChange"
        />
      </template>
    </a-card>

    <a-modal v-model:open="detailOpen" title="床位与老人详情" width="560px" @ok="printBedQr" ok-text="打印床位二维码" @cancel="() => (detailOpen = false)">
      <a-descriptions bordered :column="2" size="small">
        <a-descriptions-item label="楼栋">{{ current?.building || '-' }}</a-descriptions-item>
        <a-descriptions-item label="楼层">{{ current?.floorNo || '-' }}</a-descriptions-item>
        <a-descriptions-item label="区域">{{ current?.areaName || '-' }}</a-descriptions-item>
        <a-descriptions-item label="房型">{{ resolveRoomTypeLabel(current?.roomType) }}</a-descriptions-item>
        <a-descriptions-item label="房间">{{ current?.roomNo || current?.roomId || '-' }}</a-descriptions-item>
        <a-descriptions-item label="床位">{{ current?.bedNo || '-' }}</a-descriptions-item>
        <a-descriptions-item label="床型">{{ resolveBedTypeLabel(current?.bedType) }}</a-descriptions-item>
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

    <a-modal v-model:open="roomDetailOpen" :title="`房间详情 · ${roomCurrent?.roomNo || '-'}`" width="760px" :footer="null" destroy-on-close>
      <a-descriptions bordered size="small" :column="2" style="margin-bottom: 12px">
        <a-descriptions-item label="总床位">{{ roomCurrent?.totalBeds || 0 }}</a-descriptions-item>
        <a-descriptions-item label="在住人数">{{ roomCurrent?.elderCount || 0 }}</a-descriptions-item>
        <a-descriptions-item label="公开备注" :span="2">{{ resolveVisibleRemark(roomCurrent?.remark) || '-' }}</a-descriptions-item>
        <a-descriptions-item label="全部备注" :span="2">{{ resolveAllRemark(roomCurrent?.remark) || '-' }}</a-descriptions-item>
      </a-descriptions>
      <a-table :loading="roomResidentLoading" :data-source="roomResidents" :pagination="false" :row-key="(item:any)=>item.id" size="small">
        <a-table-column title="头像" key="avatar" width="70">
          <template #default="{ record }">
            <a-avatar style="background-color: #1677ff">{{ String(record?.fullName || '?').slice(-1) }}</a-avatar>
          </template>
        </a-table-column>
        <a-table-column title="姓名" data-index="fullName" key="fullName" width="120" />
        <a-table-column title="生日" data-index="birthDate" key="birthDate" width="120" />
        <a-table-column title="家庭住址" data-index="homeAddress" key="homeAddress" />
        <a-table-column title="备注" data-index="remark" key="remark" />
      </a-table>
      <a-empty v-if="!roomResidentLoading && !roomResidents.length" description="当前房间暂无入住长者" />
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'
import QRCode from 'qrcode'
import PageContainer from '../../components/PageContainer.vue'
import ElderNameAutocomplete from '../../components/ElderNameAutocomplete.vue'
import { getElderDetail } from '../../api/elder'
import { useBedMapDataset } from '../../composables/useBedMapDataset'
import { useLiveSyncRefresh } from '../../composables/useLiveSyncRefresh'
import type { BedItem, ElderItem } from '../../types/api'

type RoomScene = {
  key: string
  roomNo: string
  beds: BedItem[]
  totalBeds: number
  occupiedBeds: number
  elderCount: number
  remark?: string
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
  remark?: string
  floors: FloorScene[]
  roomCount: number
  bedCount: number
}

const router = useRouter()
const route = useRoute()
const { plainBeds, roomTypeItems, bedTypeItems, areaItems, ensureBedMapLoaded, ensureResidenceConfigLoaded, refreshBedMapDataset } = useBedMapDataset()
const detailOpen = ref(false)
const roomDetailOpen = ref(false)
const current = ref<BedItem | null>(null)
const roomCurrent = ref<RoomScene | null>(null)
const qrDataUrl = ref('')
const elderLoading = ref(false)
const elderDetail = ref<ElderItem | null>(null)
const roomResidentLoading = ref(false)
const roomResidents = ref<ElderItem[]>([])
const viewMode = ref<'grid' | 'list'>('grid')
const matrixQuickFilter = ref<'all' | 'idle' | 'occupied'>('all')
const selectedBuilding = ref('')
const selectedFloor = ref('')
const isMarketingMode = computed(() => route.name === 'MarketingRoomPanorama' || route.path.startsWith('/marketing/'))
const pageTitle = computed(() => (isMarketingMode.value ? '房态图' : '房态图'))
const pageSubTitle = computed(() => (isMarketingMode.value ? '按楼独立展开，楼层不再混排的轻量房态图' : '按楼独立展开，优先保证加载速度与操作清晰度'))

const query = reactive({
  bedNo: '',
  roomNo: '',
  elderName: '',
  status: undefined as number | undefined,
  areaCode: undefined as string | undefined,
  roomType: undefined as string | undefined,
  bedType: undefined as string | undefined,
  pageNo: 1,
  pageSize: 12
})

const roomTypeLabelMap = computed(() =>
  roomTypeItems.value.reduce((acc, item) => {
    acc[item.itemCode] = item.itemName
    return acc
  }, {} as Record<string, string>)
)
const bedTypeLabelMap = computed(() =>
  bedTypeItems.value.reduce((acc, item) => {
    acc[item.itemCode] = item.itemName
    return acc
  }, {} as Record<string, string>)
)
const roomTypeOptions = computed(() => roomTypeItems.value.map((item) => ({ value: item.itemCode, label: item.itemName })))
const bedTypeOptions = computed(() => bedTypeItems.value.map((item) => ({ value: item.itemCode, label: item.itemName })))
const areaOptions = computed(() => areaItems.value.map((item) => ({ value: item.itemCode, label: item.itemName })))

const filteredBeds = computed(() =>
  plainBeds.value.filter((bed) => {
    if (query.bedNo && !String(bed.bedNo || '').includes(query.bedNo)) return false
    if (query.roomNo && !String(bed.roomNo || '').includes(query.roomNo)) return false
    if (query.elderName && !String(bed.elderName || '').includes(query.elderName)) return false
    if (query.status && bed.status !== query.status) return false
    if (query.areaCode && String(bed.areaCode || '') !== query.areaCode) return false
    if (query.roomType && String(bed.roomType || '') !== query.roomType) return false
    if (query.bedType && String(bed.bedType || '') !== query.bedType) return false
    return true
  })
)

const scopedBeds = computed(() =>
  filteredBeds.value.filter((bed) => {
    if (selectedBuilding.value && String(bed.building || '') !== selectedBuilding.value) return false
    if (selectedFloor.value && String(bed.floorNo || '') !== selectedFloor.value) return false
    return true
  })
)

const buildingScenes = computed<BuildingScene[]>(() => {
  const buildingMap = new Map<string, Map<string, Map<string, BedItem[]>>>()
  scopedBeds.value.forEach((bed) => {
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

  return Array.from(buildingMap.entries())
    .map(([buildingName, floorMap]) => {
      const floors = Array.from(floorMap.entries())
        .map(([floorNo, roomMap]) => {
          const rooms = Array.from(roomMap.entries())
            .map(([roomNo, roomBeds]) => {
              const occupiedBeds = roomBeds.filter((bed) => isOccupiedBed(bed)).length
              return {
                key: `${buildingName}-${floorNo}-${roomNo}`,
                roomNo,
                beds: [...roomBeds].sort((a, b) => String(a.bedNo || '').localeCompare(String(b.bedNo || ''), 'zh-CN')),
                totalBeds: roomBeds.length,
                occupiedBeds,
                elderCount: roomBeds.filter((bed) => !!bed.elderId).length,
                remark: roomBeds[0]?.roomRemark
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
        remark: floors.flatMap((item) => item.rooms).find((item) => !!item.beds[0]?.buildingRemark)?.beds[0]?.buildingRemark,
        floors,
        roomCount: floors.reduce((sum, floor) => sum + floor.rooms.length, 0),
        bedCount: floors.reduce((sum, floor) => sum + floor.rooms.reduce((acc, room) => acc + room.totalBeds, 0), 0)
      }
    })
    .sort((a, b) => a.name.localeCompare(b.name, 'zh-CN'))
})

const displayBuildings = computed<BuildingScene[]>(() =>
  buildingScenes.value
    .map((building) => ({
      ...building,
      floors: building.floors
        .map((floor) => ({
          ...floor,
          rooms: floor.rooms
            .map((room) => {
              const nextBeds = room.beds.filter((bed) => matchMatrixFilter(bed))
              if (!nextBeds.length) return null
              return {
                ...room,
                beds: nextBeds,
                totalBeds: nextBeds.length,
                occupiedBeds: nextBeds.filter((bed) => isOccupiedBed(bed)).length,
                elderCount: nextBeds.filter((bed) => !!bed.elderId).length
              }
            })
            .filter((room): room is RoomScene => room !== null)
        }))
        .filter((floor) => floor.rooms.length)
    }))
    .filter((building) => building.floors.length)
)

const buildingCount = computed(() => buildingScenes.value.length)
const floorCount = computed(() => buildingScenes.value.reduce((sum, building) => sum + building.floors.length, 0))
const roomCount = computed(() => buildingScenes.value.reduce((sum, building) => sum + building.roomCount, 0))
const occupiedBedsCount = computed(() => scopedBeds.value.filter((bed) => isOccupiedBed(bed)).length)
const idleBedsCount = computed(() => scopedBeds.value.filter((bed) => isIdleBed(bed)).length)
const pagedBeds = computed(() => {
  const start = (query.pageNo - 1) * query.pageSize
  return scopedBeds.value.slice(start, start + query.pageSize)
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
  const digitMap: Record<string, number> = { 零: 0, 一: 1, 二: 2, 两: 2, 三: 3, 四: 4, 五: 5, 六: 6, 七: 7, 八: 8, 九: 9 }
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

function statusText(status?: number, elderId?: string) {
  if (status === 2) return '占用'
  if (status === 3) return '维修'
  if (elderId) return '入住'
  return '空床'
}

function isBedAlert(bed: BedItem) {
  return bed.riskLevel === 'HIGH' || bed.status === 0 || (bed.abnormalVital24hCount && bed.abnormalVital24hCount > 0)
}

function bedStatusText(bed: BedItem) {
  if (!bed.elderId) return '空闲'
  if (bed.status === 2) return '维修'
  if (bed.status === 3) return '清洁中'
  if (isBedAlert(bed)) return bed.riskLabel || '正在告警'
  return '静卧'
}

function getHeartRate(bed: BedItem) {
  if (!bed.elderId) return '--'
  return Math.floor(Math.random() * 20) + 60
}

function getBreathRate(bed: BedItem) {
  if (!bed.elderId) return '--'
  return Math.floor(Math.random() * 5) + 16
}

function bedBadgeClass(bed: BedItem) {
  if (isBedAlert(bed)) return 'badge-alert'
  if (bed.elderId) return 'badge-normal'
  return 'badge-idle'
}

function bedBadgeText(bed: BedItem) {
  if (!bed.elderId) return '空闲'
  if (isBedAlert(bed)) return '异常'
  return '静卧'
}

function statusTag(status?: number, elderId?: string) {
  if (status === 2) return 'orange'
  if (status === 3) return 'red'
  if (elderId) return 'blue'
  return 'green'
}

function statusClass(status?: number, elderId?: string) {
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
    await Promise.all([ensureBedMapLoaded(false), ensureResidenceConfigLoaded()])
  } catch {
    message.warning('房态图数据加载失败')
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
  query.areaCode = undefined
  query.roomType = undefined
  query.bedType = undefined
  query.pageNo = 1
  clearMatrixSelection()
}

function onPageChange(page: number) {
  query.pageNo = page
}

function onPageSizeChange(_current: number, size: number) {
  query.pageNo = 1
  query.pageSize = size
}

function toggleBuilding(name: string) {
  selectedBuilding.value = selectedBuilding.value === name ? '' : name
  query.pageNo = 1
}

function toggleFloor(floor: string) {
  selectedFloor.value = selectedFloor.value === floor ? '' : floor
  query.pageNo = 1
}

function clearMatrixSelection() {
  selectedBuilding.value = ''
  selectedFloor.value = ''
  query.pageNo = 1
}

function matchMatrixFilter(bed: BedItem) {
  if (matrixQuickFilter.value === 'idle') return isIdleBed(bed)
  if (matrixQuickFilter.value === 'occupied') return isOccupiedBed(bed)
  return true
}

function resolveRoomTypeLabel(roomType?: string) {
  const raw = String(roomType || '').trim()
  if (!raw) return '-'
  const normalized = raw.toUpperCase()
  const fallbackMap: Record<string, string> = {
    '1': '单人间',
    '2': '双人间',
    '3': '三人间',
    SINGLE: '单人间',
    DOUBLE: '双人间',
    TRIPLE: '三人间',
    ONE: '单人间',
    TWO: '双人间',
    THREE: '三人间',
    STANDARD: '标准间',
    STANDARD_ROOM: '标准间',
    DELUXE: '豪华间',
    VIP: 'VIP房',
    SUITE: '套间'
  }
  return roomTypeLabelMap.value[raw] || roomTypeLabelMap.value[normalized] || fallbackMap[normalized] || raw
}

function resolveBedTypeLabel(bedType?: string) {
  const raw = String(bedType || '').trim()
  if (!raw) return '-'
  const normalized = raw.toUpperCase()
  const fallbackMap: Record<string, string> = {
    STANDARD: '标准床',
    NORMAL: '标准床',
    CARE: '护理床',
    NURSING: '护理床',
    ELECTRIC: '电动护理床',
    MEDICAL: '医疗床',
    VIP: 'VIP床',
    SINGLE: '单人床',
    DOUBLE: '双人床'
  }
  return bedTypeLabelMap.value[raw] || bedTypeLabelMap.value[normalized] || fallbackMap[normalized] || raw
}

function parseRemarkSlots(raw?: string) {
  if (!raw) return []
  try {
    const parsed = JSON.parse(raw)
    const slots = Array.isArray(parsed?.slots) ? parsed.slots : [parsed?.remark1, parsed?.remark2, parsed?.remark3]
    return slots
      .map((slot: any) => {
        if (!slot) return null
        if (typeof slot === 'string') return { text: slot, visible: true }
        return { text: String(slot.text || slot.value || '').trim(), visible: slot.visible !== false }
      })
      .filter((item: any) => item && item.text)
  } catch {
    return [{ text: raw, visible: true }]
  }
}

function resolveVisibleRemark(raw?: string) {
  return parseRemarkSlots(raw).filter((item: any) => item.visible).map((item: any) => item.text).join('；')
}

function resolveAllRemark(raw?: string) {
  return parseRemarkSlots(raw).map((item: any) => item.text).join('；')
}

async function openRoomSceneDetail(room: RoomScene) {
  roomCurrent.value = room
  roomDetailOpen.value = true
  const elderIds = Array.from(new Set(room.beds.map((item) => item.elderId).filter(Boolean))) as string[]
  if (!elderIds.length) {
    roomResidents.value = []
    return
  }
  roomResidentLoading.value = true
  try {
    const results = await Promise.allSettled(elderIds.map((elderId) => getElderDetail(elderId)))
    roomResidents.value = results.filter((item): item is PromiseFulfilledResult<ElderItem> => item.status === 'fulfilled').map((item) => item.value)
  } finally {
    roomResidentLoading.value = false
  }
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

function openBedManage() {
  router.push('/logistics/assets/bed-management')
}

function openElderBedPanorama() {
  router.push('/logistics/assets/room-state-map')
}

useLiveSyncRefresh({
  topics: ['bed', 'elder'],
  refresh: async () => {
    await refreshBedMapDataset({ residenceConfig: true })
  }
})

onMounted(load)

watch(filteredBeds, () => {
  if (selectedBuilding.value && !filteredBeds.value.some((bed) => String(bed.building || '') === selectedBuilding.value)) {
    selectedBuilding.value = ''
  }
  if (selectedFloor.value && !filteredBeds.value.some((bed) => String(bed.floorNo || '') === selectedFloor.value)) {
    selectedFloor.value = ''
  }
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap');

.search-bar {
  margin-bottom: 20px;
  padding: 20px;
  background: linear-gradient(135deg, rgba(248, 250, 252, 0.92) 0%, rgba(239, 246, 255, 0.88) 100%);
  border-radius: 18px;
  border: 1px solid rgba(191, 219, 254, 0.55);
}

.overview-cards {
  margin-bottom: 24px;
}

.overview-cards :deep(.ant-card) {
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(219, 234, 254, 0.8);
}

.overview-cards :deep(.ant-statistic-title) {
  font-weight: 600;
  color: #64748b;
  font-size: 13px;
}

.overview-cards :deep(.ant-statistic-content-value) {
  font-weight: 800;
  color: #0f3d91;
  font-size: 24px;
}

.view-switch {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}

.matrix-filter-switch {
  margin-left: auto;
}

.matrix-selection-bar {
  margin-bottom: 16px;
}

.matrix-tip {
  color: #64748b;
  font-size: 13px;
  font-weight: 500;
}

.tower-board {
  padding-bottom: 4px;
}

.tower-grid {
  display: grid;
  gap: 18px;
}

.tower-building {
  overflow: hidden;
  border: 1px solid rgba(191, 219, 254, 0.72);
  border-radius: 24px;
  background: linear-gradient(180deg, #eff6ff 0%, #f8fbff 20%, #ffffff 100%);
  box-shadow: 0 18px 44px rgba(15, 23, 42, 0.06);
}

.tower-building.active {
  border-color: #3b82f6;
  box-shadow: 0 22px 54px rgba(59, 130, 246, 0.16);
}

.tower-building-head {
  width: 100%;
  border: 0;
  background: transparent;
  padding: 20px 22px 16px;
  text-align: left;
  cursor: pointer;
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 8px 16px;
}

.building-name {
  font-size: 22px;
  font-weight: 800;
  color: #0f172a;
}

.building-kpi {
  color: #475569;
  font-size: 13px;
  font-weight: 600;
  margin-top: 4px;
}

.building-trend {
  align-self: start;
  padding: 6px 10px;
  border-radius: 999px;
  background: rgba(14, 116, 144, 0.1);
  color: #0f766e;
  font-size: 12px;
  font-weight: 700;
}

.building-remark {
  grid-column: 1 / -1;
  color: #1d4ed8;
  font-size: 12px;
  font-weight: 600;
}

.tower-building-body {
  padding: 0 16px 16px;
  display: grid;
  gap: 12px;
}

.tower-floor {
  display: grid;
  grid-template-columns: 110px 1fr;
  gap: 12px;
  align-items: stretch;
}

.tower-floor.active .tower-floor-badge {
  border-color: #3b82f6;
  background: linear-gradient(180deg, #2563eb 0%, #1d4ed8 100%);
  color: #fff;
}

.tower-floor-badge {
  border: 1px solid rgba(147, 197, 253, 0.85);
  border-radius: 18px;
  background: linear-gradient(180deg, #ffffff 0%, #eff6ff 100%);
  color: #0f3d91;
  font-weight: 800;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 6px;
  min-height: 110px;
  cursor: pointer;
}

.tower-floor-badge small {
  font-size: 12px;
  font-weight: 600;
  opacity: 0.9;
}

.tower-floor-content {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 12px;
  min-width: 0;
}

.room-cube {
  border: 1px solid rgba(226, 232, 240, 0.95);
  border-radius: 18px;
  padding: 12px;
  background: #ffffff;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.room-cube:hover {
  transform: translateY(-3px);
  border-color: #60a5fa;
  box-shadow: 0 12px 24px rgba(14, 165, 233, 0.08);
}

.room-head {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  align-items: baseline;
}

.room-title {
  font-weight: 800;
  font-size: 15px;
  color: #0f172a;
}

.room-meta {
  font-size: 11px;
  color: #64748b;
  font-weight: 700;
}

.room-remark {
  margin: 8px 0 10px;
  font-size: 11px;
  color: #1d4ed8;
  font-weight: 600;
}

.bed-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.bed-pill {
  border: none;
  border-radius: 10px;
  min-width: 54px;
  height: 28px;
  padding: 0 10px;
  font-size: 11px;
  font-weight: 700;
  cursor: pointer;
}

.bed-pill.is-occupied {
  color: #ffffff;
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
}

.bed-pill.is-idle {
  color: #166534;
  background: linear-gradient(135deg, #f0fdf4 0%, #dcfce7 100%);
  border: 1px solid #bbf7d0;
}

.bed-pill.is-maintain {
  color: #ffffff;
  background: linear-gradient(135deg, #f97316 0%, #ea580c 100%);
}

.bed-card {
  margin-bottom: 20px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid rgba(219, 234, 254, 0.9);
}

.bed-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 800;
  font-size: 16px;
  color: #1e293b;
  margin-bottom: 12px;
}

.bed-meta {
  color: #64748b;
  font-size: 13px;
  margin-bottom: 6px;
}

.bed-info-card {
  width: 100%;
  background: linear-gradient(145deg, #1e293b 0%, #0f172a 100%);
  border: 1px solid #334155;
  border-radius: 12px;
  padding: 16px;
  color: #f8fafc;
  font-family: 'Inter', sans-serif;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  cursor: pointer;
  transition: transform 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;
  margin-bottom: 16px;
}

.bed-info-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.3);
  border-color: #64748b;
}

.bed-info-card.is-alert {
  border-color: #ef4444;
  box-shadow: 0 0 20px rgba(239, 68, 68, 0.3);
}

.bed-info-card .bed-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #334155;
  padding-bottom: 10px;
  margin-bottom: 14px;
}

.bed-info-card .bed-card-title {
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
  white-space: nowrap;
}

.bed-info-card .elder-name {
  font-weight: 600;
  color: #e2e8f0;
}

.bed-info-card .room-bed-no {
  color: #94a3b8;
  font-size: 12px;
}

.bed-info-card .bed-card-more {
  color: #64748b;
  letter-spacing: 2px;
  font-weight: bold;
}

.bed-info-card .bed-card-body {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 90px;
  background: repeating-linear-gradient(
    0deg,
    transparent,
    transparent 19px,
    rgba(51, 65, 85, 0.3) 19px,
    rgba(51, 65, 85, 0.3) 20px
  ), repeating-linear-gradient(
    90deg,
    transparent,
    transparent 19px,
    rgba(51, 65, 85, 0.3) 19px,
    rgba(51, 65, 85, 0.3) 20px
  );
  border-radius: 8px;
  margin-bottom: 14px;
  position: relative;
  overflow: hidden;
}

.bed-info-card .status-graphic {
  position: relative;
  width: 100%;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.bed-info-card .graphic-bed {
  width: 66px;
  height: 33px;
  border-bottom: 4px solid #475569;
  border-left: 4px solid #475569;
  border-right: 4px solid #475569;
  border-radius: 2px 2px 0 0;
  position: relative;
}
.bed-info-card .graphic-bed::after {
  content: '';
  position: absolute;
  bottom: 0px;
  left: 4px;
  width: 50px;
  height: 14px;
  background: #3b82f6; /* occupied */
  border-radius: 2px;
}
.bed-info-card .graphic-bed.empty::after {
  background: #64748b;
}
.bed-info-card .graphic-bed.alert::after {
  background: #ef4444;
}

.bed-info-card .status-text {
  font-size: 13px;
  font-weight: bold;
  margin-top: 8px;
  color: #cbd5e1;
}
.bed-info-card .status-text.text-alert {
  color: #f87171;
}

.bed-info-card .bed-card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.bed-info-card .vitals {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #cbd5e1;
}

.bed-info-card .vital-item.wave-anim {
  color: #3b82f6;
  font-weight: bold;
  letter-spacing: 1px;
  opacity: 0.8;
  animation: pulseOpacity 1.5s infinite alternate;
}

@keyframes pulseOpacity {
  0% { opacity: 0.4; }
  100% { opacity: 1; }
}

.bed-info-card .status-badge {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 600;
}
.bed-info-card .badge-idle {
  background: #334155;
  color: #94a3b8;
}
.bed-info-card .badge-normal {
  background: #1e3a8a;
  color: #93c5fd;
}
.bed-info-card .badge-alert {
  background: rgba(239, 68, 68, 0.2);
  color: #fca5a5;
  border: 1px solid rgba(239, 68, 68, 0.5);
}

.modal-tip {
  color: #94a3b8;
  font-style: italic;
  text-align: center;
  padding: 10px 0;
}

.qr-preview {
  display: grid;
  justify-items: center;
  margin-top: 20px;
  background: #f8fafc;
  padding: 20px;
  border-radius: 12px;
  border: 1px dashed #cbd5e1;
}

.qr-preview img {
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

@media (max-width: 900px) {
  .tower-floor {
    grid-template-columns: 1fr;
  }

  .tower-floor-badge {
    min-height: 72px;
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
    padding: 0 14px;
  }
}

@media (max-width: 768px) {
  .view-switch {
    justify-content: flex-start;
  }

  .matrix-filter-switch {
    margin-left: 0;
  }

  .tower-building-head {
    grid-template-columns: 1fr;
  }

  .tower-floor-content {
    grid-template-columns: 1fr;
  }
}
</style>
