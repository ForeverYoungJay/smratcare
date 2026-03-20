<template>
  <PageContainer title="床态全景" subTitle="楼号横向 + 楼层纵向二维图（长者管理）">
    <a-card title="楼层二维矩阵" class="card-elevated" :bordered="false">
      <a-space wrap style="margin-bottom: 12px">
        <a-tag color="default">空闲 {{ stats.idle }}</a-tag>
        <a-tag color="processing">预定 {{ stats.reserved }}</a-tag>
        <a-tag color="green">在住 {{ stats.occupied }}</a-tag>
        <a-tag color="orange">维修 {{ stats.maintenance }}</a-tag>
        <a-tag color="cyan">清洁中 {{ stats.cleaning }}</a-tag>
        <a-tag color="red">锁定 {{ stats.locked }}</a-tag>
        <a-tag color="#ff85c0">女性长者</a-tag>
        <a-tag color="#69b1ff">男性长者</a-tag>
      </a-space>
      <a-alert
        v-if="lifecycleContext.active"
        type="info"
        show-icon
        style="margin-bottom: 12px"
        :message="lifecycleContext.message"
      />

      <div class="selector-strip">
        <div class="selector-group">
          <div class="selector-label">搜索</div>
          <a-input-search v-model:value="keyword" placeholder="搜索房间号/长者名" allow-clear style="width: 260px" />
        </div>
        <div class="selector-group">
          <div class="selector-label">床位筛选</div>
          <a-segmented v-model:value="quickFilter" :options="quickFilterOptions" />
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
        <div class="selector-group">
          <a-button @click="openBedMap">查看床位全景</a-button>
          <a-button type="primary" @click="openBedManage">床位管理</a-button>
          <a-button @click="resetFilters">重置筛选</a-button>
        </div>
      </div>

      <div class="matrix-selection-bar">
        <a-space wrap>
          <a-tag color="blue" v-if="selectedBuilding">楼栋：{{ selectedBuilding }}</a-tag>
          <a-tag color="cyan" v-if="selectedFloor">楼层：{{ selectedFloor }}</a-tag>
          <span v-if="!selectedBuilding && !selectedFloor" class="matrix-tip">可点击楼栋与楼层快速聚焦</span>
          <a-button size="small" v-if="selectedBuilding || selectedFloor" @click="clearMatrixSelection">清除楼层筛选</a-button>
        </a-space>
      </div>
      <FlowGuardBar
        title="在院床态守卫"
        :subject="bedGuardSubject"
        :stage-text="bedGuardStageText"
        :stage-color="bedGuardStageColor"
        :steps="bedGuardSteps"
        :current-index="bedGuardCurrentIndex"
        :blockers="bedGuardBlockers"
        :hint="bedGuardHint"
        @action="handleBedGuardAction"
        style="margin-bottom: 12px"
      />

      <div class="plan-stage">
        <a-empty v-if="!matrixBuildings.length || !matrixFloors.length" description="暂无床位数据" />
        <Panorama3D
          v-else
          :buildings="matrixBuildings"
          :floors="matrixFloors"
          :room-lookup="roomSceneLookup"
          @click-room="openRoomDetail"
        />
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
        <a-button block @click="openAssessmentArchive">查看评估档案</a-button>
        <a-button block @click="openContractsInvoices">合同与票据</a-button>
        <a-button block @click="openStatusChangeCenter">状态变更中心</a-button>
        <a-button block danger @click="createAlert">生成提醒并进入任务中心</a-button>
        <a-button block @click="goScan">扫码执行（定位今日任务）</a-button>
      </a-space>
    </a-modal>

    <a-modal v-model:open="roomDetailOpen" :title="`房间详情 · ${selectedRoom?.roomNo || '-'}`" width="760px" :footer="null" destroy-on-close>
      <a-descriptions bordered size="small" :column="2" style="margin-bottom: 12px">
        <a-descriptions-item label="房型">{{ resolveRoomTypeLabel(selectedRoom?.roomType) }}</a-descriptions-item>
        <a-descriptions-item label="容量">{{ selectedRoom?.capacity || 0 }} 床</a-descriptions-item>
        <a-descriptions-item label="在住人数">{{ selectedRoom?.elderCount || 0 }} 人</a-descriptions-item>
        <a-descriptions-item label="空床">{{ selectedRoom?.emptyBeds || 0 }} 床</a-descriptions-item>
        <a-descriptions-item label="公开备注" :span="2">{{ resolveVisibleRemark(selectedRoom?.remark) || '-' }}</a-descriptions-item>
        <a-descriptions-item label="全部备注" :span="2">{{ resolveAllRemark(selectedRoom?.remark) || '-' }}</a-descriptions-item>
      </a-descriptions>

      <a-table
        :loading="roomResidentLoading"
        :data-source="roomResidents"
        :pagination="false"
        :row-key="(item:any) => item.id"
        size="small"
      >
        <a-table-column title="头像" key="avatar" width="70">
          <template #default="{ record }">
            <a-avatar style="background-color: #1677ff">{{ String(record?.fullName || '?').slice(-1) }}</a-avatar>
          </template>
        </a-table-column>
        <a-table-column title="姓名" data-index="fullName" key="fullName" width="110" />
        <a-table-column title="生日" data-index="birthDate" key="birthDate" width="120" />
        <a-table-column title="家庭住址" data-index="homeAddress" key="homeAddress" />
        <a-table-column title="备注" data-index="remark" key="remark" />
        <a-table-column title="操作" key="action" width="180">
          <template #default="{ record }">
            <a-space>
              <a-button type="link" size="small" @click="openElderProfile(record.id)">详情</a-button>
              <a-button type="link" size="small" @click="openResidentBills(record.id)">账单</a-button>
            </a-space>
          </template>
        </a-table-column>
      </a-table>
      <a-empty v-if="!roomResidentLoading && !roomResidents.length" description="当前房间暂无入住长者" />
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'
import PageContainer from '../../../components/PageContainer.vue'
import FlowGuardBar from '../../../components/FlowGuardBar.vue'
import Panorama3D from './Panorama3D.vue'
import { getBedMap, getRoomList } from '../../../api/bed'
import { getElderDetail } from '../../../api/elder'
import { useLiveSyncRefresh } from '../../../composables/useLiveSyncRefresh'
import type { BedItem, ElderItem, RoomItem } from '../../../types'

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
  remark?: string
}

const router = useRouter()
const route = useRoute()
const beds = ref<BedItem[]>([])
const roomTypeMap = ref<Record<string, string>>({})
const roomCapacityMap = ref<Record<string, number>>({})
const keyword = ref('')
const selectedBed = ref<BedItem | null>(null)
const detailOpen = ref(false)
const roomDetailOpen = ref(false)
const selectedRoom = ref<RoomScene | null>(null)
const roomResidents = ref<ElderItem[]>([])
const roomResidentLoading = ref(false)
const quickFilter = ref<'ALL' | 'IDLE' | 'OCCUPIED'>('ALL')
const riskFilterEnabled = ref(false)
const riskFilterLevel = ref<'ALL' | 'HIGH' | 'MEDIUM' | 'LOW'>('ALL')
const riskDataLoading = ref(false)
const riskDataReady = ref(false)
const selectedBuilding = ref('')
const selectedFloor = ref('')
const quickFilterOptions = [
  { label: '全部', value: 'ALL' },
  { label: '仅空床', value: 'IDLE' },
  { label: '仅入住', value: 'OCCUPIED' }
]
const riskLevelOptions = [
  { label: '全部风险', value: 'ALL' },
  { label: '高风险', value: 'HIGH' },
  { label: '中风险', value: 'MEDIUM' },
  { label: '低风险', value: 'LOW' }
]
const bedGuardSteps = ['合同签署入院', '入住选床完成', '在住照护执行', '风险干预闭环']
const BED_ROUTE_KEYS = ['bedBuilding', 'bedFloor', 'bedKeyword', 'bedQuick', 'bedRiskEnabled', 'bedRiskLevel'] as const
const bedRouteSignature = ref('')
const skipNextBedRouteWatch = ref(false)
const lifecycleContext = computed(() => {
  const source = String(route.query.source || '').trim().toLowerCase()
  const scene = String(route.query.scene || '').trim().toLowerCase()
  const active = source === 'lifecycle' || scene === 'status-change'
  return {
    active,
    message: active ? '当前来自入住状态变更联动，可在床态视图快速确认清洁/维修与空床调配。' : ''
  }
})

const filteredBeds = computed(() => beds.value.filter((b) => {
  if (keyword.value) {
    const text = `${b.roomNo || ''} ${b.elderName || ''}`.toLowerCase()
    const searchText = keyword.value.toLowerCase()
    if (!text.includes(searchText)) return false
  }
  return true
}))

const scopedBeds = computed(() => sourceBeds.value.filter((item) => {
  if (selectedBuilding.value && String(item.building || '') !== selectedBuilding.value) return false
  if (selectedFloor.value && String(item.floorNo || '') !== selectedFloor.value) return false
  return true
}))

const buildingList = computed(() => {
  const set = new Set<string>()
  scopedBeds.value.forEach((item) => set.add((item.building || '未分配楼栋').trim() || '未分配楼栋'))
  return Array.from(set).sort((a, b) => a.localeCompare(b, 'zh-CN'))
})

const matrixBuildings = computed(() => buildingList.value)

const floorList = computed(() => {
  const set = new Set<string>()
  scopedBeds.value.forEach((item) => set.add((item.floorNo || '未知楼层').trim() || '未知楼层'))
  return Array.from(set).sort((a, b) => {
    const diff = floorSortValue(b) - floorSortValue(a)
    if (diff !== 0) return diff
    return String(a).localeCompare(String(b), 'zh-CN')
  })
})

const matrixFloors = computed(() => floorList.value)

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
const bedGuardCurrentIndex = computed(() => {
  if (!selectedBed.value) return 2
  if (isEmptyBed(selectedBed.value)) return 1
  if (selectedBed.value.riskLevel === 'HIGH') return 3
  return 2
})
const bedGuardStageText = computed(() => {
  if (!selectedBed.value) return '在院运行中'
  if (isEmptyBed(selectedBed.value)) return '空床待分配'
  if (selectedBed.value.riskLevel === 'HIGH') return '高风险干预中'
  return '照护执行中'
})
const bedGuardStageColor = computed(() => {
  if (!selectedBed.value) return 'blue'
  if (isEmptyBed(selectedBed.value)) return 'gold'
  if (selectedBed.value.riskLevel === 'HIGH') return 'red'
  return 'green'
})
const bedGuardSubject = computed(() => {
  if (selectedBed.value) {
    return `床位 ${selectedBed.value.bedNo || '-'} / 房间 ${selectedBed.value.roomNo || '-'} / 长者 ${selectedBed.value.elderName || '空床'}`
  }
  return `当前范围：${selectedBuilding.value || '全部楼栋'} ${selectedFloor.value || '全部楼层'}`
})
const bedGuardBlockers = computed(() => {
  const blockers: Array<{ code: string; text: string; actionLabel?: string; actionKey?: string }> = []
  if (selectedBed.value) {
    if (isEmptyBed(selectedBed.value)) blockers.push({ code: 'B201', text: '当前为空床，需通过入住办理分配', actionLabel: '去入住办理', actionKey: 'go-admission' })
    if (selectedBed.value.status === 2) blockers.push({ code: 'B301', text: '床位处于维修状态' })
    if (selectedBed.value.riskLevel === 'HIGH') blockers.push({ code: 'B401', text: '高风险长者需优先处置并建任务', actionLabel: '生成提醒', actionKey: 'create-alert' })
    return blockers
  }
  if (quickFilter.value === 'IDLE' && stats.value.idle === 0) blockers.push({ code: 'B202', text: '暂无可分配空床' })
  if (quickFilter.value === 'OCCUPIED' && stats.value.occupied === 0) blockers.push({ code: 'B203', text: '当前无在住床位' })
  return blockers
})
const bedGuardHint = computed(() => {
  if (selectedBed.value?.elderId) return '可打开长者档案，检查评估、护理任务与异常提醒'
  return '建议点击床位查看详情，执行分配或风险干预动作'
})

function handleBedGuardAction(item: { actionKey?: string }) {
  if (item.actionKey === 'go-admission') {
    allocateBed()
    return
  }
  if (item.actionKey === 'create-alert') {
    createAlert()
  }
}

const sourceBeds = computed(() => {
  const bedsByQuick = filteredBeds.value.filter((item) => {
    if (quickFilter.value === 'IDLE') return isEmptyBed(item)
    if (quickFilter.value === 'OCCUPIED') return !!item.elderId
    return true
  })
  if (!riskFilterEnabled.value || !riskDataReady.value) return bedsByQuick
  return bedsByQuick.filter((item) => {
    if (!item.riskLevel) return false
    if (riskFilterLevel.value === 'ALL') return true
    return item.riskLevel === riskFilterLevel.value
  })
})

const roomSceneLookup = computed(() => {
  const lookup = new Map<string, RoomScene[]>()
  const roomMap = new Map<string, BedItem[]>()
  scopedBeds.value.forEach((bed) => {
    const building = (bed.building || '未分配楼栋').trim() || '未分配楼栋'
    const floor = (bed.floorNo || '未知楼层').trim() || '未知楼层'
    const roomNo = (bed.roomNo || `房间-${bed.roomId || '-'}`).trim() || `房间-${bed.roomId || '-'}`
    const key = `${building}@@${floor}@@${roomNo}`
    if (!roomMap.has(key)) roomMap.set(key, [])
    roomMap.get(key)!.push(bed)
  })

  roomMap.forEach((roomBeds, key) => {
    const [building, floor, roomNo] = key.split('@@')
    const lookupKey = `${building}@@${floor}`
    if (!lookup.has(lookupKey)) lookup.set(lookupKey, [])
    const roomId = String(roomBeds[0]?.roomId || '')
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
    lookup.get(lookupKey)!.push({
      key,
      roomNo,
      roomType: resolveRoomTypeLabel(roomTypeMap.value[roomId] || '标准间'),
      capacity,
      autoSpan,
      beds: sortedBeds,
      totalBeds,
      occupiedBeds,
      elderCount,
      emptyBeds,
      remark: roomBeds[0]?.roomRemark
    })
  })

  lookup.forEach((rooms) => {
    rooms.sort((a, b) => {
      if (a.capacity !== b.capacity) return b.capacity - a.capacity
      if (a.emptyBeds !== b.emptyBeds) return b.emptyBeds - a.emptyBeds
      return String(a.roomNo).localeCompare(String(b.roomNo))
    })
  })
  return lookup
})

function roomsAt(building: string, floor: string) {
  return roomSceneLookup.value.get(`${building}@@${floor}`) || []
}

function toggleBuilding(building: string) {
  selectedBuilding.value = selectedBuilding.value === building ? '' : building
}

function toggleFloor(floor: string) {
  selectedFloor.value = selectedFloor.value === floor ? '' : floor
}

function clearMatrixSelection() {
  selectedBuilding.value = ''
  selectedFloor.value = ''
}

function resetFilters() {
  keyword.value = ''
  quickFilter.value = 'ALL'
  riskFilterEnabled.value = false
  riskFilterLevel.value = 'ALL'
  clearMatrixSelection()
}

function firstRouteQueryText(value: unknown) {
  if (Array.isArray(value)) return firstRouteQueryText(value[0])
  if (value == null) return ''
  return String(value).trim()
}

function flattenRouteQuery(source: Record<string, unknown>) {
  return Object.entries(source || {}).reduce<Record<string, string>>((acc, [key, value]) => {
    const text = firstRouteQueryText(value)
    if (!text) return acc
    acc[key] = text
    return acc
  }, {})
}

function buildBedRouteSignature(source: Record<string, unknown>) {
  return [...BED_ROUTE_KEYS]
    .sort()
    .map((key) => `${key}:${firstRouteQueryText(source[key])}`)
    .join('|')
}

function applyFiltersFromRoute() {
  keyword.value = firstRouteQueryText(route.query.bedKeyword)

  const nextQuick = firstRouteQueryText(route.query.bedQuick).toUpperCase()
  quickFilter.value = nextQuick === 'IDLE' || nextQuick === 'OCCUPIED' ? nextQuick : 'ALL'

  riskFilterEnabled.value = firstRouteQueryText(route.query.bedRiskEnabled) === '1'
  const nextRisk = firstRouteQueryText(route.query.bedRiskLevel).toUpperCase()
  riskFilterLevel.value = nextRisk === 'HIGH' || nextRisk === 'MEDIUM' || nextRisk === 'LOW' ? nextRisk : 'ALL'

  selectedBuilding.value = firstRouteQueryText(route.query.bedBuilding)
  selectedFloor.value = firstRouteQueryText(route.query.bedFloor)
}

function buildFiltersRouteQuery() {
  const nextQuery: Record<string, string> = {}
  Object.entries(flattenRouteQuery(route.query as Record<string, unknown>)).forEach(([key, value]) => {
    if ((BED_ROUTE_KEYS as readonly string[]).includes(key)) return
    nextQuery[key] = value
  })
  if (keyword.value.trim()) nextQuery.bedKeyword = keyword.value.trim()
  nextQuery.bedQuick = quickFilter.value
  if (riskFilterEnabled.value) nextQuery.bedRiskEnabled = '1'
  if (riskFilterEnabled.value && riskFilterLevel.value !== 'ALL') nextQuery.bedRiskLevel = riskFilterLevel.value
  if (selectedBuilding.value) nextQuery.bedBuilding = selectedBuilding.value
  if (selectedFloor.value) nextQuery.bedFloor = selectedFloor.value
  return nextQuery
}

function hasSameRouteQuery(nextQuery: Record<string, string>) {
  const currentQuery = flattenRouteQuery(route.query as Record<string, unknown>)
  const currentKeys = Object.keys(currentQuery)
  const nextKeys = Object.keys(nextQuery)
  if (currentKeys.length !== nextKeys.length) return false
  return nextKeys.every((key) => currentQuery[key] === nextQuery[key])
}

async function syncFiltersToRoute() {
  const nextQuery = buildFiltersRouteQuery()
  if (hasSameRouteQuery(nextQuery)) return
  skipNextBedRouteWatch.value = true
  bedRouteSignature.value = buildBedRouteSignature(nextQuery)
  await router.replace({ path: route.path, query: nextQuery })
}

function floorSortValue(text: string) {
  const raw = String(text || '').trim()
  if (!raw) return -999
  const normalized = raw.replace(/\s+/g, '').toUpperCase()
  if (/^(ROOF|RF|屋顶|天台)$/.test(normalized)) return 999
  const basement = normalized.match(/(?:地下|负|B)([0-9]+|[一二三四五六七八九十百两]+)/)
  if (basement) return -parseFloorToken(basement[1])
  const match = normalized.match(/([0-9]+|[一二三四五六七八九十百两]+)(?:F|楼|层)?/)
  if (match) return parseFloorToken(match[1])
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
  return section + number
}

function resolveStatus(bed: BedItem): '空闲' | '预定' | '在住' | '维修' | '清洁中' | '锁定' {
  if (bed.elderId) return '在住'
  if (bed.status === 0) return '锁定'
  if (bed.status === 2) return '维修'
  if (bed.status === 3) return '清洁中'
  if (String(bed.bedNo || '').endsWith('R')) return '预定'
  return '空闲'
}

function resolveRoomTypeLabel(roomType?: string) {
  const raw = String(roomType || '').trim()
  if (!raw) return '-'
  const normalized = raw.toUpperCase()
  const map: Record<string, string> = {
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
  return map[normalized] || raw
}

function statusClass(status: string) {
  if (status === '在住') return 'is-occupied'
  if (status === '预定') return 'is-reserved'
  if (status === '维修') return 'is-maintenance'
  if (status === '清洁中') return 'is-cleaning'
  if (status === '锁定') return 'is-locked'
  return 'is-idle'
}

function genderClass(bed: BedItem) {
  if (!bed.elderId) return ''
  if (Number(bed.elderGender) === 2) return 'gender-female'
  if (Number(bed.elderGender) === 1) return 'gender-male'
  return ''
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

function buildingRemarkByName(buildingName: string) {
  const hit = scopedBeds.value.find((item) => String(item.building || '未分配楼栋') === buildingName)
  return hit?.buildingRemark || ''
}

function parseRemarkSlots(raw?: string) {
  if (!raw) return []
  try {
    const parsed = JSON.parse(raw)
    const slots = Array.isArray(parsed?.slots) ? parsed.slots : [parsed?.remark1, parsed?.remark2, parsed?.remark3]
    return slots
      .map((slot: any, index: number) => {
        if (!slot) return null
        if (typeof slot === 'string') {
          return { text: slot, visible: true, index }
        }
        return {
          text: String(slot.text || slot.value || '').trim(),
          visible: slot.visible !== false,
          index
        }
      })
      .filter((slot: any) => slot && slot.text)
  } catch {
    return [{ text: raw, visible: true, index: 0 }]
  }
}

function resolveVisibleRemark(raw?: string) {
  return parseRemarkSlots(raw)
    .filter((item: any) => item.visible)
    .map((item: any) => item.text)
    .join('；')
}

function resolveAllRemark(raw?: string) {
  return parseRemarkSlots(raw)
    .map((item: any) => item.text)
    .join('；')
}

async function openRoomDetail(room: RoomScene) {
  selectedRoom.value = room
  roomDetailOpen.value = true
  const elderIds = Array.from(new Set(room.beds.map((item) => item.elderId).filter(Boolean))) as string[]
  if (!elderIds.length) {
    roomResidents.value = []
    return
  }
  roomResidentLoading.value = true
  try {
    const results = await Promise.allSettled(elderIds.map((elderId) => getElderDetail(elderId)))
    roomResidents.value = results
      .filter((item): item is PromiseFulfilledResult<ElderItem> => item.status === 'fulfilled')
      .map((item) => item.value)
  } finally {
    roomResidentLoading.value = false
  }
}

function openElderProfile(elderId: string) {
  roomDetailOpen.value = false
  router.push(`/elder/detail/${elderId}`)
}

function openResidentBills(elderId: string) {
  router.push(`/finance/bills/in-resident?elderId=${elderId}`)
}

function activeElderId() {
  const elderId = String(selectedBed.value?.elderId || '').trim()
  if (!elderId) {
    message.warning('当前床位未关联长者')
    return ''
  }
  return elderId
}

function openAssessmentArchive() {
  const elderId = activeElderId()
  if (!elderId) return
  detailOpen.value = false
  router.push(`/elder/assessment/ability/archive?elderId=${encodeURIComponent(elderId)}`)
}

function openContractsInvoices() {
  const elderId = activeElderId()
  if (!elderId) return
  detailOpen.value = false
  router.push(`/elder/contracts-invoices?elderId=${encodeURIComponent(elderId)}`)
}

function openStatusChangeCenter() {
  detailOpen.value = false
  const elderId = String(selectedBed.value?.elderId || '').trim()
  router.push({
    path: '/elder/status-change',
    query: elderId ? { residentId: elderId } : undefined
  })
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

function openBedMap() {
  router.push('/logistics/assets/room-state-map')
}

function openBedManage() {
  router.push('/logistics/assets/bed-management')
}

async function loadBeds(includeRisk = false) {
  beds.value = await getBedMap({ params: { includeRisk } })
  if (includeRisk) {
    riskDataReady.value = true
  } else {
    riskDataReady.value = false
  }
}

async function ensureRiskDataLoaded() {
  if (riskDataReady.value || riskDataLoading.value) return
  riskDataLoading.value = true
  try {
    await loadBeds(true)
  } finally {
    riskDataLoading.value = false
  }
}

async function loadRooms() {
  const rooms: RoomItem[] = await getRoomList()
  const typeMap: Record<string, string> = {}
  const capMap: Record<string, number> = {}
  rooms.forEach((item) => {
    const id = String(item.id)
    typeMap[id] = item.roomType || '标准间'
    capMap[id] = Number(item.capacity || 1)
  })
  roomTypeMap.value = typeMap
  roomCapacityMap.value = capMap
}

useLiveSyncRefresh({
  topics: ['elder', 'bed', 'lifecycle', 'finance', 'care', 'dining'],
  refresh: async () => {
    await Promise.all([loadBeds(false), loadRooms()])
    if (riskFilterEnabled.value) {
      await ensureRiskDataLoaded()
    }
  }
})

onMounted(async () => {
  applyFiltersFromRoute()
  bedRouteSignature.value = buildBedRouteSignature(route.query as Record<string, unknown>)
  await Promise.all([loadBeds(false), loadRooms()])
  if (riskFilterEnabled.value) {
    await ensureRiskDataLoaded()
  }
  await syncFiltersToRoute().catch(() => {})
})

watch(
  riskFilterEnabled,
  (enabled) => {
    if (enabled) {
      ensureRiskDataLoaded().catch(() => {})
    }
  }
)

watch(sourceBeds, () => {
  if (selectedBuilding.value && !sourceBeds.value.some((item) => String(item.building || '') === selectedBuilding.value)) {
    selectedBuilding.value = ''
  }
  if (selectedFloor.value && !sourceBeds.value.some((item) => String(item.floorNo || '') === selectedFloor.value)) {
    selectedFloor.value = ''
  }
})

watch(
  [keyword, quickFilter, riskFilterEnabled, riskFilterLevel, selectedBuilding, selectedFloor],
  () => {
    syncFiltersToRoute().catch(() => {})
  }
)

watch(
  () => route.query,
  (queryMap) => {
    const nextSignature = buildBedRouteSignature(queryMap as Record<string, unknown>)
    if (skipNextBedRouteWatch.value) {
      skipNextBedRouteWatch.value = false
      bedRouteSignature.value = nextSignature
      return
    }
    if (nextSignature === bedRouteSignature.value) {
      return
    }
    bedRouteSignature.value = nextSignature
    applyFiltersFromRoute()
  },
  { deep: true }
)
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap');

.portal-page {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  letter-spacing: -0.01em;
}

.selector-strip {
  display: grid;
  gap: 16px;
  margin-bottom: 20px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.8);
}

.selector-group {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.selector-label {
  min-width: 48px;
  color: #1e3a8a;
  font-weight: 700;
  font-size: 13px;
}

.matrix-selection-bar {
  margin-bottom: 16px;
  padding: 0 4px;
}

.matrix-tip {
  color: #64748b;
  font-size: 13px;
  font-weight: 500;
}

.plan-stage {
  min-height: 480px;
  border: 1px solid rgba(226, 232, 240, 0.6);
  border-radius: 20px;
  background: linear-gradient(135deg, rgba(239, 246, 255, 0.4) 0%, rgba(255, 255, 255, 0.6) 100%);
  padding: 16px;
  backdrop-filter: blur(8px);
}

.matrix-viewport {
  overflow-x: auto;
  padding-bottom: 8px;
}

.matrix-grid {
  display: grid;
  min-width: max-content;
  border: 1px solid rgba(226, 232, 240, 0.8);
  border-radius: 16px;
  overflow: hidden;
  background: #ffffff;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.04);
}

.matrix-corner,
.matrix-building-head,
.matrix-floor-axis,
.matrix-cell {
  border-right: 1px solid rgba(226, 232, 240, 0.6);
  border-bottom: 1px solid rgba(226, 232, 240, 0.6);
}

.matrix-corner {
  position: sticky;
  left: 0;
  z-index: 10;
  background: linear-gradient(135deg, #f1f5f9 0%, #e2e8f0 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 72px;
  font-weight: 800;
  color: #1e3a8a;
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.matrix-building-head {
  border: 0;
  appearance: none;
  background: #ffffff;
  min-height: 72px;
  padding: 14px 16px;
  font-weight: 800;
  color: #0f172a;
  text-align: left;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 16px;
}

.matrix-building-head:hover {
  background: #f8fafc;
}

.matrix-building-head.active {
  background: #eff6ff;
  box-shadow: inset 0 -4px 0 #3b82f6;
  color: #2563eb;
}

.building-remark {
  margin-top: 4px;
  color: #94a3b8;
  font-size: 11px;
  font-weight: 500;
  font-style: italic;
}

.matrix-floor-axis {
  border: 0;
  appearance: none;
  position: sticky;
  left: 0;
  z-index: 5;
  background: #ffffff;
  min-height: 240px;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  color: #1e3a8a;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 16px;
}

.matrix-floor-axis:hover {
  background: #f8fafc;
}

.matrix-floor-axis.active {
  background: #eff6ff;
  box-shadow: inset -4px 0 0 #3b82f6;
  color: #2563eb;
}

.matrix-cell {
  background: transparent;
  min-height: 240px;
  max-height: 240px;
  padding: 14px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  align-content: start;
  gap: 12px;
  overflow-y: auto;
  scrollbar-width: none;
}

.matrix-cell::-webkit-scrollbar { display: none; }

.matrix-empty {
  color: #cbd5e1;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 60px;
  font-style: italic;
  font-size: 14px;
}

.room-block {
  border: 1px solid rgba(255, 255, 255, 0.8);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.7);
  padding: 12px;
  backdrop-filter: blur(10px);
  box-shadow:
    0 4px 6px -1px rgba(0, 0, 0, 0.05),
    0 10px 15px -3px rgba(30, 64, 175, 0.04);
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  cursor: pointer;
}

.room-block:hover {
  transform: translateY(-6px);
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 20px 25px -5px rgba(30, 64, 175, 0.1);
  border-color: #3b82f6;
}

.bed-matrix {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
  margin-top: 10px;
}

.room-head {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
}

.room-title {
  font-weight: 800;
  color: #1e293b;
  font-size: 15px;
}

.room-type {
  font-size: 11px;
  font-weight: 700;
  color: #3b82f6;
  text-transform: uppercase;
}

.room-meta {
  margin: 6px 0 2px;
  color: #64748b;
  font-size: 11px;
  font-weight: 600;
  display: flex;
  justify-content: space-between;
}

.room-remark {
  margin-bottom: 6px;
  color: #2563eb;
  font-size: 11px;
  font-weight: 600;
  background: rgba(37, 99, 235, 0.08);
  padding: 2px 6px;
  border-radius: 4px;
}

.bed-tile {
  border: 0;
  border-radius: 10px;
  padding: 10px 8px;
  text-align: left;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  position: relative;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0,0,0,0.03);
  overflow: hidden;
}

.bed-tile:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}

.bed-no {
  font-weight: 800;
  font-size: 13px;
}

.bed-name {
  font-size: 12px;
  font-weight: 500;
  margin-top: 2px;
}

.empty-priority {
  box-shadow: inset 0 0 0 2px #22c55e;
}

.risk-corner {
  position: absolute;
  right: 6px;
  top: 6px;
  border-radius: 6px;
  padding: 2px 4px;
  font-size: 10px;
  font-weight: 800;
  line-height: 1;
  text-transform: uppercase;
}

.risk-high { background: #ef4444; color: #fff; box-shadow: 0 2px 6px rgba(239, 68, 68, 0.3); }
.risk-medium { background: #f59e0b; color: #fff; box-shadow: 0 2px 6px rgba(245, 158, 11, 0.3); }
.risk-low { background: #38bdf8; color: #fff; box-shadow: 0 2px 6px rgba(56, 189, 248, 0.3); }

.is-idle {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  color: #475569;
}

.is-reserved {
  background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%);
  color: #1e40af;
}

.is-occupied {
  background: linear-gradient(135deg, #f0fdf4 0%, #dcfce7 100%);
  color: #166534;
}

.gender-female {
  border-left: 4px solid #ff85c0;
}

.gender-male {
  border-left: 4px solid #3b82f6;
}

.is-maintenance {
  background: linear-gradient(135deg, #fff7ed 0%, #ffedd5 100%);
  color: #9a3412;
}

.is-cleaning {
  background: linear-gradient(135deg, #ecfeff 0%, #cffafe 100%);
  color: #0e7490;
}

.is-locked {
  background: linear-gradient(135deg, #fef2f2 0%, #fee2e2 100%);
  color: #991b1b;
}

@media (max-width: 1400px) {
  .matrix-cell { grid-template-columns: 1fr; }
}

@media (max-width: 640px) {
  .selector-label { min-width: 58px; }
  .room-block { grid-column: span 1 !important; }
}
</style>
