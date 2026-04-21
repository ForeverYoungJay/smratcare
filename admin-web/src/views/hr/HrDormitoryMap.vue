<template>
  <PageContainer title="员工宿舍房态图" subTitle="宿舍楼栋分层图 / 员工床位占用、空床补齐与待分配可视化">
    <div class="map-shell">
      <section class="hero-panel">
        <div>
          <p class="eyebrow">Dormitory Panorama</p>
          <h2>把宿舍基础配置和员工入住信息合并成完整房态图</h2>
          <p class="hero-copy">已配置的宿舍房间会完整展示标准床位，未住满的房间自动补齐空床位；待分配、冲突和超配房间也会在同一张图上高亮显示。</p>
        </div>
        <div class="hero-actions">
          <a-button type="primary" size="large" @click="router.push('/hr/expense/dormitory')">返回宿舍台账</a-button>
          <a-button size="large" @click="openDrawer()">新增住宿安排</a-button>
        </div>
      </section>

      <SearchForm :model="query" @search="fetchData" @reset="onReset">
        <a-form-item label="关键字">
          <a-input v-model:value="query.keyword" placeholder="姓名/工号/部门/宿舍/电表" allow-clear />
        </a-form-item>
        <a-form-item label="部门">
          <a-select
            v-model:value="query.departmentId"
            allow-clear
            show-search
            :filter-option="false"
            :options="departmentOptions"
            :loading="departmentLoading"
            placeholder="选择部门"
            style="width: 220px"
            @search="searchDepartments"
            @focus="() => !departmentOptions.length && searchDepartments('')"
          />
        </a-form-item>
        <a-form-item label="员工状态">
          <a-select v-model:value="query.status" allow-clear :options="staffStatusOptions" style="width: 150px" />
        </a-form-item>
        <a-form-item label="住宿状态">
          <a-select v-model:value="query.dormitoryState" allow-clear :options="dormitoryStateOptions" style="width: 180px" />
        </a-form-item>
        <a-form-item label="楼栋">
          <a-input v-model:value="query.building" placeholder="如 A栋" allow-clear />
        </a-form-item>
        <a-form-item label="房间">
          <a-input v-model:value="query.roomNo" placeholder="如 3-201" allow-clear />
        </a-form-item>
        <template #extra>
          <a-space wrap>
            <a-radio-group v-model:value="matrixQuickFilter" size="small">
              <a-radio-button value="all">全部</a-radio-button>
              <a-radio-button value="assigned">已分配</a-radio-button>
              <a-radio-button value="pending">待分配</a-radio-button>
              <a-radio-button value="conflict">冲突</a-radio-button>
            </a-radio-group>
          </a-space>
        </template>
      </SearchForm>

      <a-row :gutter="[12, 12]" class="overview-cards">
        <a-col :xs="12" :md="6">
          <a-card size="small"><a-statistic title="楼栋" :value="buildingCount" /></a-card>
        </a-col>
        <a-col :xs="12" :md="6">
          <a-card size="small"><a-statistic title="楼层" :value="floorCount" /></a-card>
        </a-col>
        <a-col :xs="12" :md="6">
          <a-card size="small"><a-statistic title="房间" :value="roomCount" /></a-card>
        </a-col>
        <a-col :xs="12" :md="6">
          <a-card size="small"><a-statistic title="标准床位" :value="bedCount" /></a-card>
        </a-col>
      </a-row>

      <div class="matrix-selection-bar">
        <a-space wrap>
          <a-tag color="blue" v-if="selectedBuilding">楼栋：{{ selectedBuilding }}</a-tag>
          <a-tag color="cyan" v-if="selectedFloor">楼层：{{ selectedFloor }}</a-tag>
          <span v-if="!selectedBuilding && !selectedFloor" class="matrix-tip">点击楼栋和楼层标签可快速聚焦到对应宿舍区域</span>
          <a-button size="small" v-if="selectedBuilding || selectedFloor" @click="clearSelection">清除筛选</a-button>
        </a-space>
      </div>

      <div class="tower-board">
        <a-empty v-if="!displayBuildings.length && !loading" description="当前筛选下暂无宿舍房态数据" />
        <div v-else class="tower-grid">
          <section v-for="building in displayBuildings" :key="building.key" class="tower-building" :class="{ active: selectedBuilding === building.name }">
            <button type="button" class="tower-building-head" @click="toggleBuilding(building.name)">
              <div>
                <div class="building-name">{{ building.name }}</div>
                <div class="building-kpi">{{ building.floors.length }} 层 · {{ building.roomCount }} 间 · {{ building.bedCount }} 床</div>
              </div>
              <div class="building-trend">员工宿舍分布</div>
            </button>

            <div class="tower-building-body">
              <div v-for="floor in building.floors" :key="floor.key" class="tower-floor" :class="{ active: selectedFloor === floor.label }">
                <button type="button" class="tower-floor-badge" @click="toggleFloor(floor.label)">
                  <span>{{ floor.label }}</span>
                  <small>{{ floor.rooms.length }} 间</small>
                </button>
                <div class="tower-floor-content">
                  <div v-for="room in floor.rooms" :key="room.key" class="room-cube">
                    <div class="room-head">
                      <div>
                        <div class="room-title">{{ room.roomNo }}</div>
                        <div class="room-function-name">{{ room.building }} / {{ floor.label }}</div>
                      </div>
                      <div class="room-meta">{{ room.occupiedBeds }}/{{ room.totalBeds }} 床 · 空床 {{ room.availableBeds }}</div>
                    </div>
                    <div class="room-tags">
                      <a-tag v-if="room.hasRoomConfig" color="geekblue">标准 {{ room.configuredCapacity }} 床</a-tag>
                      <a-tag v-else color="default">未配置容量</a-tag>
                      <a-tag v-if="room.availableBeds" color="green">空床 {{ room.availableBeds }}</a-tag>
                      <a-tag v-if="room.pendingCount" color="orange">待分配 {{ room.pendingCount }}</a-tag>
                      <a-tag v-if="room.conflictCount" color="red">冲突 {{ room.conflictCount }}</a-tag>
                      <a-tag v-if="room.overflowCount" color="volcano">超配 {{ room.overflowCount }}</a-tag>
                      <a-tag v-if="room.meterNos.length" color="cyan">电表 {{ room.meterNos[0] }}</a-tag>
                    </div>
                    <div class="bed-grid">
                      <button
                        v-for="bed in room.beds"
                        :key="bed.key"
                        type="button"
                        class="bed-pill"
                        :class="bedStatusClass(bed)"
                        @click="openDrawerByBed(room, bed)"
                      >
                        <span>{{ bed.label }}</span>
                        <small>{{ bed.staffName || '空床待分配' }}</small>
                      </button>
                    </div>
                    <div v-if="room.unassignedStaff.length" class="room-unassigned">
                      <div class="unassigned-label">未分床位</div>
                      <div class="unassigned-list">
                        <button
                          v-for="staff in room.unassignedStaff"
                          :key="staff.staffId"
                          type="button"
                          class="unassigned-chip"
                          @click="openDrawer(staff)"
                        >
                          {{ staff.staffName || staff.staffNo || '待维护' }}
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </section>
        </div>
      </div>
    </div>

    <a-drawer v-model:open="drawerOpen" :title="drawerTitle" width="620">
      <a-form :model="form" layout="vertical">
        <a-form-item label="员工" required>
          <a-select
            v-model:value="form.staffId"
            show-search
            allow-clear
            :filter-option="false"
            :options="staffOptions"
            :loading="staffLoading"
            placeholder="选择员工"
            @search="searchStaff"
            @focus="() => !staffOptions.length && searchStaff('')"
            @change="onStaffChange"
          />
        </a-form-item>
        <div class="dorm-head">
          <span>是否住宿舍</span>
          <a-switch :checked="Number(form.liveInDormitory) === 1" @change="(checked) => (form.liveInDormitory = checked ? 1 : 0)" />
        </div>
        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="宿舍楼栋">
              <a-input v-model:value="form.dormitoryBuilding" placeholder="如 A栋" :disabled="Number(form.liveInDormitory) !== 1" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="宿舍房间">
              <a-input v-model:value="form.dormitoryRoomNo" placeholder="如 3-201" :disabled="Number(form.liveInDormitory) !== 1" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="床位号">
              <a-input v-model:value="form.dormitoryBedNo" placeholder="如 B02" :disabled="Number(form.liveInDormitory) !== 1" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="电表编号">
              <a-input v-model:value="form.meterNo" placeholder="如 DORM-A-201" :disabled="Number(form.liveInDormitory) !== 1" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="方案状态">
          <a-select v-model:value="form.status" :options="planStatusOptions" />
        </a-form-item>
        <a-form-item label="用餐方案">
          <a-input :value="form.mealPlanSummary || '未启用餐费方案'" disabled />
        </a-form-item>
        <a-form-item label="备注">
          <a-textarea v-model:value="form.remark" :rows="3" placeholder="例如临时调宿、拼房共表、夜班固定床位" />
        </a-form-item>
      </a-form>
      <template #footer>
        <a-space>
          <a-button @click="drawerOpen = false">取消</a-button>
          <a-button type="primary" :loading="saving" @click="submitForm">保存宿舍信息</a-button>
        </a-space>
      </template>
    </a-drawer>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import { getHrDormitoryMap, getHrDormitoryRoomConfigList, getHrStaffServicePlan, upsertHrStaffServicePlan } from '../../api/hr'
import type { HrDormitoryRoomConfigItem, HrDormitoryStaffItem, HrStaffServicePlan } from '../../types'
import { useDepartmentOptions } from '../../composables/useDepartmentOptions'
import { useStaffOptions } from '../../composables/useStaffOptions'

type DormBedScene = {
  key: string
  label: string
  staffId?: string | number
  staffName?: string
  staffNo?: string
  occupancyConflict?: boolean
  isVirtualEmpty?: boolean
  row?: HrDormitoryStaffItem
}

type DormRoomScene = {
  key: string
  building: string
  floor: string
  roomNo: string
  beds: DormBedScene[]
  totalBeds: number
  occupiedBeds: number
  availableBeds: number
  configuredCapacity: number
  conflictCount: number
  pendingCount: number
  overflowCount: number
  meterNos: string[]
  unassignedStaff: HrDormitoryStaffItem[]
  hasRoomConfig: boolean
  sortNo: number
}

type DormFloorScene = {
  key: string
  label: string
  sort: number
  rooms: DormRoomScene[]
}

type DormBuildingScene = {
  key: string
  name: string
  floors: DormFloorScene[]
  roomCount: number
  bedCount: number
}

const router = useRouter()

const query = reactive({
  keyword: undefined as string | undefined,
  departmentId: undefined as number | undefined,
  status: 1 as number | undefined,
  dormitoryState: undefined as string | undefined,
  building: undefined as string | undefined,
  roomNo: undefined as string | undefined
})

const staffStatusOptions = [
  { label: '仅在职', value: 1 },
  { label: '仅离职', value: 0 }
]

const dormitoryStateOptions = [
  { label: '已住宿', value: 'LIVE_IN' },
  { label: '已分配床位', value: 'ASSIGNED' },
  { label: '待分配床位', value: 'PENDING_ASSIGN' },
  { label: '床位冲突', value: 'CONFLICT' },
  { label: '未住宿', value: 'NO_DORM' }
]

const planStatusOptions = [
  { label: '启用', value: 'ENABLED' },
  { label: '停用', value: 'DISABLED' }
]

const rows = ref<HrDormitoryStaffItem[]>([])
const roomConfigs = ref<HrDormitoryRoomConfigItem[]>([])
const loading = ref(false)
const saving = ref(false)
const drawerOpen = ref(false)
const matrixQuickFilter = ref<'all' | 'assigned' | 'pending' | 'conflict'>('all')
const selectedBuilding = ref('')
const selectedFloor = ref('')

const form = reactive<HrStaffServicePlan>({
  staffId: undefined,
  liveInDormitory: 1,
  status: 'ENABLED'
})

const drawerTitle = computed(() => (form.staffId ? '编辑宿舍安排' : '新增宿舍安排'))

const { departmentOptions, departmentLoading, searchDepartments } = useDepartmentOptions({ pageSize: 120, preloadSize: 400 })
const { staffOptions, staffLoading, searchStaff, ensureSelectedStaff } = useStaffOptions({ pageSize: 120, preloadSize: 500 })

const buildingScenes = computed<DormBuildingScene[]>(() => {
  const buildingMap = new Map<string, Map<string, Map<string, DormRoomScene>>>()

  function ensureRoom(building: string, floor: string, roomNo: string) {
    if (!buildingMap.has(building)) buildingMap.set(building, new Map())
    const floorMap = buildingMap.get(building)!
    if (!floorMap.has(floor)) floorMap.set(floor, new Map())
    const roomMap = floorMap.get(floor)!
    if (!roomMap.has(roomNo)) {
      roomMap.set(roomNo, {
        key: `${building}-${floor}-${roomNo}`,
        building,
        floor,
        roomNo,
        beds: [],
        totalBeds: 0,
        occupiedBeds: 0,
        availableBeds: 0,
        configuredCapacity: 0,
        conflictCount: 0,
        pendingCount: 0,
        overflowCount: 0,
        meterNos: [],
        unassignedStaff: [],
        hasRoomConfig: false,
        sortNo: 0
      })
    }
    return roomMap.get(roomNo)!
  }

  roomConfigs.value.forEach((item) => {
    const building = normalizeText(item.building, '未分配楼栋')
    const roomNo = normalizeText(item.roomNo, '待分配房间')
    const floor = normalizeText(item.floorLabel) || resolveFloorLabel(roomNo)
    const room = ensureRoom(building, floor, roomNo)
    room.hasRoomConfig = true
    room.configuredCapacity = Math.max(Number(item.bedCapacity || 0), room.configuredCapacity)
    room.sortNo = Number(item.sortNo || 0)
  })

  rows.value.forEach((item) => {
    if (Number(item.liveInDormitory) !== 1) return
    const building = normalizeText(item.dormitoryBuilding, '未分配楼栋')
    const roomNo = normalizeText(item.dormitoryRoomNo, '待分配房间')
    const floor = findConfiguredFloor(building, roomNo) || resolveFloorLabel(roomNo)
    const room = ensureRoom(building, floor, roomNo)
    const meterNo = normalizeText(item.meterNo)
    if (meterNo && !room.meterNos.includes(meterNo)) {
      room.meterNos.push(meterNo)
    }
    if (item.dormitoryBedNo) {
      room.beds.push({
        key: `${item.staffId || item.staffNo || item.staffName}-${item.dormitoryBedNo}`,
        label: normalizeText(item.dormitoryBedNo, '未标记床位'),
        staffId: item.staffId,
        staffName: item.staffName,
        staffNo: item.staffNo,
        occupancyConflict: item.occupancyConflict,
        row: item
      })
    } else {
      room.unassignedStaff.push(item)
    }
  })

  return Array.from(buildingMap.entries())
    .map(([building, floorMap]) => {
      const floors = Array.from(floorMap.entries())
        .map(([floor, roomMap]) => {
          const rooms = Array.from(roomMap.values())
            .map((room) => {
              const actualBeds = [...room.beds].sort((a, b) => bedSortValue(a.label) - bedSortValue(b.label) || a.label.localeCompare(b.label, 'zh-CN'))
              const baseCapacity = room.configuredCapacity > 0 ? room.configuredCapacity : actualBeds.length
              room.totalBeds = Math.max(baseCapacity, actualBeds.length)
              room.availableBeds = Math.max(room.totalBeds - actualBeds.length, 0)
              room.occupiedBeds = actualBeds.filter((bed) => !!bed.staffId).length
              room.conflictCount = actualBeds.filter((bed) => bed.occupancyConflict).length
              room.pendingCount = room.unassignedStaff.length
              room.overflowCount = room.configuredCapacity > 0 ? Math.max(actualBeds.length - room.configuredCapacity, 0) : 0
              room.beds = [
                ...actualBeds,
                ...Array.from({ length: room.availableBeds }, (_, index) => ({
                  key: `${room.key}-empty-${index + 1}`,
                  label: `空床${String(index + 1).padStart(2, '0')}`,
                  isVirtualEmpty: true
                }))
              ]
              return room
            })
            .filter((room) => matchMatrixFilter(room))
            .sort((a, b) => a.sortNo - b.sortNo || a.roomNo.localeCompare(b.roomNo, 'zh-CN'))
          return {
            key: `${building}-${floor}`,
            label: floor,
            sort: floorSortValue(floor),
            rooms
          }
        })
        .filter((floor) => floor.rooms.length)
        .sort((a, b) => b.sort - a.sort)
      return {
        key: building,
        name: building,
        floors,
        roomCount: floors.reduce((sum, floor) => sum + floor.rooms.length, 0),
        bedCount: floors.reduce((sum, floor) => sum + floor.rooms.reduce((acc, room) => acc + room.totalBeds, 0), 0)
      }
    })
    .filter((building) => building.floors.length)
    .sort((a, b) => a.name.localeCompare(b.name, 'zh-CN'))
})

const displayBuildings = computed(() =>
  buildingScenes.value
    .map((building) => ({
      ...building,
      floors: building.floors.filter((floor) => !selectedFloor.value || floor.label === selectedFloor.value)
    }))
    .filter((building) => !selectedBuilding.value || building.name === selectedBuilding.value)
    .filter((building) => building.floors.length)
)

const buildingCount = computed(() => buildingScenes.value.length)
const floorCount = computed(() => buildingScenes.value.reduce((sum, building) => sum + building.floors.length, 0))
const roomCount = computed(() => buildingScenes.value.reduce((sum, building) => sum + building.roomCount, 0))
const bedCount = computed(() => buildingScenes.value.reduce((sum, building) => sum + building.bedCount, 0))

function normalizeText(value?: string, fallback = '') {
  const text = String(value || '').trim()
  return text || fallback
}

function resolveFloorLabel(roomNo?: string) {
  const text = normalizeText(roomNo, '')
  const direct = text.match(/^([A-Za-z]?\d+)[-层Ff]/)
  if (direct?.[1]) return `${direct[1]}层`
  const simple = text.match(/^(\d{1,2})/)
  if (simple?.[1]) return `${simple[1]}层`
  return '未分层'
}

function findConfiguredFloor(building: string, roomNo: string) {
  const matched = roomConfigs.value.find((item) => normalizeText(item.building) === building && normalizeText(item.roomNo) === roomNo)
  return normalizeText(matched?.floorLabel)
}

function floorSortValue(text: string) {
  const raw = String(text || '').replace(/层/g, '').trim()
  const number = Number(raw.replace(/[^\d-]/g, ''))
  return Number.isFinite(number) ? number : -999
}

function bedSortValue(text: string) {
  const match = String(text || '').match(/(\d+)/)
  return match?.[1] ? Number(match[1]) : 999
}

function matchMatrixFilter(room: DormRoomScene) {
  if (matrixQuickFilter.value === 'all') return true
  if (matrixQuickFilter.value === 'assigned') return room.occupiedBeds > 0 && room.pendingCount === 0 && room.conflictCount === 0
  if (matrixQuickFilter.value === 'pending') return room.pendingCount > 0
  if (matrixQuickFilter.value === 'conflict') return room.conflictCount > 0 || room.overflowCount > 0
  return true
}

function bedStatusClass(bed: DormBedScene) {
  if (bed.occupancyConflict) return 'is-conflict'
  if (bed.isVirtualEmpty || !bed.staffId) return 'is-empty'
  return 'is-occupied'
}

function toggleBuilding(name: string) {
  selectedBuilding.value = selectedBuilding.value === name ? '' : name
  if (!selectedBuilding.value) {
    selectedFloor.value = ''
  }
}

function toggleFloor(label: string) {
  selectedFloor.value = selectedFloor.value === label ? '' : label
}

function clearSelection() {
  selectedBuilding.value = ''
  selectedFloor.value = ''
}

async function fetchData() {
  loading.value = true
  try {
    const [staffRows, configRows] = await Promise.all([
      getHrDormitoryMap({ ...query }),
      getHrDormitoryRoomConfigList({
        keyword: query.keyword,
        building: query.building,
        roomNo: query.roomNo,
        status: 'ENABLED'
      })
    ])
    rows.value = staffRows || []
    roomConfigs.value = configRows || []
  } catch {
    rows.value = []
    roomConfigs.value = []
  } finally {
    loading.value = false
  }
}

function onReset() {
  query.keyword = undefined
  query.departmentId = undefined
  query.status = 1
  query.dormitoryState = undefined
  query.building = undefined
  query.roomNo = undefined
  selectedBuilding.value = ''
  selectedFloor.value = ''
  matrixQuickFilter.value = 'all'
  fetchData()
}

function resetForm() {
  Object.assign(form, {
    staffId: undefined,
    breakfastEnabled: undefined,
    breakfastUnitPrice: undefined,
    breakfastDaysPerMonth: undefined,
    lunchEnabled: undefined,
    lunchUnitPrice: undefined,
    lunchDaysPerMonth: undefined,
    dinnerEnabled: undefined,
    dinnerUnitPrice: undefined,
    dinnerDaysPerMonth: undefined,
    liveInDormitory: 1,
    dormitoryBuilding: undefined,
    dormitoryRoomNo: undefined,
    dormitoryBedNo: undefined,
    meterNo: undefined,
    status: 'ENABLED',
    mealPlanSummary: undefined,
    remark: undefined
  })
}

async function hydratePlan(staffId: string | number, staffName?: string) {
  ensureSelectedStaff(staffId, staffName)
  const detail = await getHrStaffServicePlan(staffId)
  Object.assign(form, {
    staffId: detail?.staffId,
    breakfastEnabled: detail?.breakfastEnabled,
    breakfastUnitPrice: detail?.breakfastUnitPrice,
    breakfastDaysPerMonth: detail?.breakfastDaysPerMonth,
    lunchEnabled: detail?.lunchEnabled,
    lunchUnitPrice: detail?.lunchUnitPrice,
    lunchDaysPerMonth: detail?.lunchDaysPerMonth,
    dinnerEnabled: detail?.dinnerEnabled,
    dinnerUnitPrice: detail?.dinnerUnitPrice,
    dinnerDaysPerMonth: detail?.dinnerDaysPerMonth,
    liveInDormitory: Number(detail?.liveInDormitory || 0),
    dormitoryBuilding: detail?.dormitoryBuilding,
    dormitoryRoomNo: detail?.dormitoryRoomNo,
    dormitoryBedNo: detail?.dormitoryBedNo,
    meterNo: detail?.meterNo,
    status: detail?.status || 'ENABLED',
    mealPlanSummary: detail?.mealPlanSummary,
    remark: detail?.remark
  })
}

async function openDrawer(record?: HrDormitoryStaffItem) {
  resetForm()
  drawerOpen.value = true
  if (record?.staffId) {
    await hydratePlan(record.staffId, record.staffName)
  }
}

async function openDrawerByBed(room: DormRoomScene, bed: DormBedScene) {
  if (bed.row) {
    await openDrawer(bed.row)
    return
  }
  resetForm()
  drawerOpen.value = true
  form.liveInDormitory = 1
  form.dormitoryBuilding = room.building
  form.dormitoryRoomNo = room.roomNo
  form.meterNo = room.meterNos[0]
}

async function onStaffChange(value: string | number | undefined) {
  if (!value) {
    resetForm()
    return
  }
  await hydratePlan(value)
}

async function submitForm() {
  if (!form.staffId) {
    message.warning('请选择员工')
    return
  }
  saving.value = true
  try {
    await upsertHrStaffServicePlan({ ...form })
    message.success('宿舍信息已保存')
    drawerOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  searchDepartments('')
  searchStaff('')
  fetchData()
})
</script>

<style scoped>
.map-shell {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.hero-panel {
  display: grid;
  grid-template-columns: minmax(0, 1.7fr) minmax(320px, 1fr);
  gap: 16px;
  padding: 20px 24px;
  border-radius: 24px;
  background:
    radial-gradient(circle at top left, rgba(96, 165, 250, 0.18), transparent 30%),
    linear-gradient(135deg, #0f172a 0%, #172554 56%, #1d4ed8 100%);
  color: #f8fafc;
}

.eyebrow {
  margin: 0 0 8px;
  font-size: 12px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: rgba(248, 250, 252, 0.72);
}

.hero-panel h2 {
  margin: 0;
  font-size: 28px;
  line-height: 1.3;
}

.hero-copy {
  margin: 12px 0 0;
  color: rgba(248, 250, 252, 0.82);
  line-height: 1.7;
}

.hero-actions {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 12px;
}

.matrix-selection-bar {
  margin-top: 4px;
}

.matrix-tip {
  color: #64748b;
  font-size: 13px;
}

.tower-board {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.tower-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(360px, 1fr));
  gap: 16px;
}

.tower-building {
  border: 1px solid #dbe7f3;
  border-radius: 22px;
  background: linear-gradient(180deg, #ffffff 0%, #f8fbff 100%);
  box-shadow: 0 14px 28px rgba(15, 23, 42, 0.05);
  overflow: hidden;
}

.tower-building.active {
  border-color: #3b82f6;
  box-shadow: 0 18px 36px rgba(59, 130, 246, 0.14);
}

.tower-building-head {
  width: 100%;
  border: 0;
  background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%);
  padding: 16px 18px;
  text-align: left;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
}

.building-name {
  font-size: 20px;
  font-weight: 700;
  color: #12314d;
}

.building-kpi,
.building-trend {
  margin-top: 4px;
  color: #5f7b95;
  font-size: 12px;
}

.tower-building-body {
  padding: 16px;
}

.tower-floor {
  display: grid;
  grid-template-columns: 88px minmax(0, 1fr);
  gap: 12px;
  align-items: start;
}

.tower-floor + .tower-floor {
  margin-top: 12px;
}

.tower-floor.active .tower-floor-badge {
  background: linear-gradient(180deg, #2563eb 0%, #1d4ed8 100%);
  color: #fff;
}

.tower-floor-badge {
  border: 0;
  border-radius: 18px;
  background: linear-gradient(180deg, #e2e8f0 0%, #cbd5e1 100%);
  padding: 14px 10px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  align-items: center;
  cursor: pointer;
  color: #1e293b;
}

.tower-floor-content {
  display: grid;
  gap: 12px;
}

.room-cube {
  border: 1px solid #e2e8f0;
  border-radius: 18px;
  background: #fff;
  padding: 14px;
}

.room-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}

.room-title {
  font-weight: 700;
  font-size: 16px;
  color: #0f172a;
}

.room-function-name,
.room-meta {
  color: #64748b;
  font-size: 12px;
}

.room-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 10px;
}

.bed-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 10px;
  margin-top: 12px;
}

.bed-pill {
  border: 0;
  border-radius: 16px;
  min-height: 72px;
  padding: 12px 10px;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
  cursor: pointer;
  text-align: left;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.bed-pill:hover,
.unassigned-chip:hover {
  transform: translateY(-1px);
}

.bed-pill span {
  font-weight: 700;
}

.bed-pill small {
  font-size: 12px;
  opacity: 0.88;
}

.bed-pill.is-occupied {
  background: linear-gradient(180deg, #dcfce7 0%, #bbf7d0 100%);
  color: #166534;
}

.bed-pill.is-empty {
  background: linear-gradient(180deg, #f8fafc 0%, #e2e8f0 100%);
  color: #475569;
}

.bed-pill.is-conflict {
  background: linear-gradient(180deg, #fee2e2 0%, #fecaca 100%);
  color: #b91c1c;
}

.room-unassigned {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed #e2e8f0;
}

.unassigned-label {
  font-size: 12px;
  color: #92400e;
  margin-bottom: 8px;
}

.unassigned-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.unassigned-chip {
  border: 0;
  border-radius: 999px;
  padding: 7px 12px;
  background: #ffedd5;
  color: #9a3412;
  cursor: pointer;
}

.dorm-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  padding: 12px 14px;
  border-radius: 14px;
  background: #f8fafc;
}

@media (max-width: 960px) {
  .hero-panel {
    grid-template-columns: 1fr;
  }

  .tower-grid {
    grid-template-columns: 1fr;
  }

  .tower-floor {
    grid-template-columns: 1fr;
  }
}
</style>
