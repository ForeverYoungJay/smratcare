<template>
  <PageContainer title="员工宿舍管理" subTitle="人事后勤协同 / 员工住宿台账、空床位与房间基础配置">
    <div class="dorm-shell">
      <section class="hero-panel">
        <div>
          <p class="eyebrow">Dormitory Desk</p>
          <h2>把员工入住台账和宿舍基础配置放在同一个入口统一维护</h2>
          <p class="hero-copy">支持床位分配、电表维护、冲突检查，以及楼栋/楼层/房间标准床位数配置，房态图会直接使用这里的基础数据补齐空床位。</p>
        </div>
        <div class="hero-actions">
          <a-button type="primary" size="large" @click="openDrawer()">新增住宿安排</a-button>
          <a-button size="large" @click="openRoomConfigDrawer()">新增房间配置</a-button>
          <a-button size="large" @click="router.push('/hr/expense/dormitory-map')">宿舍房态图</a-button>
          <a-button size="large" @click="router.push('/hr/expense/meal-fee')">查看员工餐费</a-button>
          <a-button size="large" @click="router.push('/hr/expense/electricity-fee')">查看员工电费</a-button>
        </div>
      </section>

      <a-row :gutter="12">
        <a-col :xs="12" :md="6">
          <a-card size="small" class="summary-card">
            <div class="summary-label">在职员工</div>
            <div class="summary-value">{{ summary.staffCount || 0 }}</div>
            <div class="summary-hint">当前人事范围</div>
          </a-card>
        </a-col>
        <a-col :xs="12" :md="6">
          <a-card size="small" class="summary-card">
            <div class="summary-label">已住宿舍</div>
            <div class="summary-value">{{ summary.liveInDormitoryCount || 0 }}</div>
            <div class="summary-hint">已启用住宿标记</div>
          </a-card>
        </a-col>
        <a-col :xs="12" :md="6">
          <a-card size="small" class="summary-card">
            <div class="summary-label">已配置房间</div>
            <div class="summary-value">{{ summary.configuredRoomCount || 0 }}</div>
            <div class="summary-hint">标准床位 {{ summary.configuredBedCapacity || 0 }}</div>
          </a-card>
        </a-col>
        <a-col :xs="12" :md="6">
          <a-card size="small" class="summary-card warning">
            <div class="summary-label">冲突 / 待分配</div>
            <div class="summary-value">{{ Number(summary.conflictCount || 0) + Number(summary.pendingAssignCount || 0) }}</div>
            <div class="summary-hint">重复占床或缺少床位信息</div>
          </a-card>
        </a-col>
      </a-row>

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
            <a-button type="primary" @click="openDrawer()">新增住宿安排</a-button>
            <a-button @click="router.push('/hr/expense/dormitory-map')">打开房态图</a-button>
            <a-button @click="fetchData">刷新统计</a-button>
          </a-space>
        </template>
      </SearchForm>

      <DataTable
        rowKey="staffId"
        :columns="columns"
        :data-source="rows"
        :loading="loading"
        :pagination="pagination"
        :row-class-name="rowClassName"
        :scroll="{ x: 1420 }"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'staff'">
            <div class="staff-cell">
              <strong>{{ record.staffName || '-' }}</strong>
              <span>{{ record.staffNo || '-' }}</span>
            </div>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-space size="small">
              <a-tag :color="Number(record.status) === 1 ? 'green' : 'default'">
                {{ Number(record.status) === 1 ? '在职' : '离职' }}
              </a-tag>
              <a-tag :color="Number(record.liveInDormitory) === 1 ? 'blue' : 'default'">
                {{ Number(record.liveInDormitory) === 1 ? '住宿舍' : '未住宿' }}
              </a-tag>
            </a-space>
          </template>
          <template v-else-if="column.key === 'dormitory'">
            <div class="plan-cell">
              <strong>{{ record.dormitoryLabel || '未住宿' }}</strong>
              <span>电表：{{ record.meterNo || '-' }}</span>
              <a-tag v-if="record.occupancyConflict" color="red">床位冲突</a-tag>
              <a-tag v-else-if="Number(record.liveInDormitory) === 1 && !hasAssignedBed(record)" color="orange">待分配</a-tag>
            </div>
          </template>
          <template v-else-if="column.key === 'mealPlanSummary'">
            <span>{{ record.mealPlanSummary || '未维护' }}</span>
          </template>
          <template v-else-if="column.key === 'actions'">
            <a-space size="small">
              <a-button type="link" size="small" @click="openDrawer(record)">编辑</a-button>
              <a-button type="link" size="small" @click="router.push(`/hr/expense/meal-fee?keyword=${encodeURIComponent(String(record.staffNo || record.staffName || ''))}`)">餐费</a-button>
              <a-button type="link" size="small" @click="router.push(`/hr/expense/electricity-fee?keyword=${encodeURIComponent(String(record.staffNo || record.staffName || ''))}`)">电费</a-button>
            </a-space>
          </template>
        </template>
      </DataTable>

      <section class="config-section">
        <div class="section-head">
          <div>
            <h3>楼栋 / 房间基础配置</h3>
            <p>维护标准床位容量后，宿舍房态图会自动补齐空床位，未住满房间也能显示完整房态。</p>
          </div>
          <a-space wrap>
            <a-button type="primary" @click="openRoomConfigDrawer()">新增房间</a-button>
            <a-button @click="fetchRoomConfigData">刷新房间配置</a-button>
          </a-space>
        </div>

        <a-row :gutter="12">
          <a-col :xs="12" :md="6">
            <a-card size="small" class="mini-card">
              <div class="summary-label">已配置房间</div>
              <div class="mini-value">{{ roomConfigSummary.roomCount }}</div>
            </a-card>
          </a-col>
          <a-col :xs="12" :md="6">
            <a-card size="small" class="mini-card">
              <div class="summary-label">标准床位</div>
              <div class="mini-value">{{ roomConfigSummary.bedCapacity }}</div>
            </a-card>
          </a-col>
          <a-col :xs="12" :md="6">
            <a-card size="small" class="mini-card">
              <div class="summary-label">已占用床位</div>
              <div class="mini-value">{{ roomConfigSummary.occupiedBeds }}</div>
            </a-card>
          </a-col>
          <a-col :xs="12" :md="6">
            <a-card size="small" class="mini-card">
              <div class="summary-label">剩余空床</div>
              <div class="mini-value">{{ roomConfigSummary.availableBeds }}</div>
            </a-card>
          </a-col>
        </a-row>

        <div class="config-filters">
          <a-space wrap>
            <a-input v-model:value="roomConfigQuery.keyword" placeholder="搜索楼栋/楼层/房间/备注" allow-clear style="width: 240px" />
            <a-input v-model:value="roomConfigQuery.building" placeholder="楼栋" allow-clear style="width: 140px" />
            <a-input v-model:value="roomConfigQuery.floorLabel" placeholder="楼层，如 3层" allow-clear style="width: 150px" />
            <a-select v-model:value="roomConfigQuery.status" :options="planStatusOptions" allow-clear style="width: 140px" />
            <a-button type="primary" @click="fetchRoomConfigData">查询房间</a-button>
            <a-button @click="resetRoomConfigQuery">重置</a-button>
          </a-space>
        </div>

        <a-table
          rowKey="id"
          :columns="roomConfigColumns"
          :data-source="roomConfigRows"
          :loading="roomConfigLoading"
          :pagination="false"
          :scroll="{ x: 1100 }"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'room'">
              <div class="staff-cell">
                <strong>{{ record.building || '-' }} / {{ record.floorLabel || '-' }}</strong>
                <span>{{ record.roomNo || '-' }}</span>
              </div>
            </template>
            <template v-else-if="column.key === 'capacity'">
              <div class="plan-cell">
                <strong>{{ record.bedCapacity || 0 }} 床</strong>
                <span>已占用 {{ roomStats(record).occupiedBeds }} · 空床 {{ roomStats(record).availableBeds }}</span>
                <a-tag v-if="roomStats(record).pendingCount" color="orange">待分配 {{ roomStats(record).pendingCount }}</a-tag>
                <a-tag v-if="roomStats(record).conflictCount" color="red">冲突 {{ roomStats(record).conflictCount }}</a-tag>
                <a-tag v-if="roomStats(record).overflowCount" color="volcano">超配 {{ roomStats(record).overflowCount }}</a-tag>
              </div>
            </template>
            <template v-else-if="column.key === 'status'">
              <a-tag :color="record.status === 'DISABLED' ? 'default' : 'green'">
                {{ record.status === 'DISABLED' ? '停用' : '启用' }}
              </a-tag>
            </template>
            <template v-else-if="column.key === 'actions'">
              <a-space size="small">
                <a-button type="link" size="small" @click="openRoomConfigDrawer(record)">编辑</a-button>
                <a-popconfirm title="确定删除这个宿舍房间配置吗？" @confirm="deleteRoomConfig(record)">
                  <a-button type="link" size="small" danger>删除</a-button>
                </a-popconfirm>
              </a-space>
            </template>
          </template>
        </a-table>
      </section>
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
          <a-textarea
            v-model:value="form.remark"
            :rows="3"
            placeholder="例如夜班固定住 A 栋；与同房共用电表；暂时仅锁定楼栋未分床位"
          />
        </a-form-item>
      </a-form>
      <template #footer>
        <a-space>
          <a-button @click="drawerOpen = false">取消</a-button>
          <a-button type="primary" :loading="saving" @click="submitForm">保存宿舍信息</a-button>
        </a-space>
      </template>
    </a-drawer>

    <a-drawer v-model:open="roomConfigDrawerOpen" :title="roomConfigDrawerTitle" width="520">
      <a-form :model="roomConfigForm" layout="vertical">
        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="宿舍楼栋" required>
              <a-input v-model:value="roomConfigForm.building" placeholder="如 A栋" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="楼层">
              <a-input v-model:value="roomConfigForm.floorLabel" placeholder="如 3层，可不填自动推断" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="房间号" required>
              <a-input v-model:value="roomConfigForm.roomNo" placeholder="如 3-201" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="标准床位数" required>
              <a-input-number v-model:value="roomConfigForm.bedCapacity" :min="1" :max="32" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="排序号">
              <a-input-number v-model:value="roomConfigForm.sortNo" :min="0" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="状态">
              <a-select v-model:value="roomConfigForm.status" :options="planStatusOptions" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="备注">
          <a-textarea v-model:value="roomConfigForm.remark" :rows="3" placeholder="例如夫妻房、夜班宿舍、仅限某部门使用" />
        </a-form-item>
      </a-form>
      <template #footer>
        <a-space>
          <a-button @click="roomConfigDrawerOpen = false">取消</a-button>
          <a-button type="primary" :loading="roomConfigSaving" @click="submitRoomConfig">保存房间配置</a-button>
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
import DataTable from '../../components/DataTable.vue'
import {
  deleteHrDormitoryRoomConfig,
  getHrDormitoryMap,
  getHrDormitoryOverview,
  getHrDormitoryPage,
  getHrDormitoryRoomConfigList,
  getHrStaffServicePlan,
  upsertHrDormitoryRoomConfig,
  upsertHrStaffServicePlan
} from '../../api/hr'
import type { HrDormitoryOverview, HrDormitoryRoomConfigItem, HrDormitoryStaffItem, HrStaffServicePlan, PageResult } from '../../types'
import { useDepartmentOptions } from '../../composables/useDepartmentOptions'
import { useStaffOptions } from '../../composables/useStaffOptions'

const router = useRouter()

const query = reactive({
  keyword: undefined as string | undefined,
  departmentId: undefined as number | undefined,
  status: 1,
  dormitoryState: undefined as string | undefined,
  building: undefined as string | undefined,
  roomNo: undefined as string | undefined,
  pageNo: 1,
  pageSize: 10
})

const roomConfigQuery = reactive({
  keyword: undefined as string | undefined,
  building: undefined as string | undefined,
  floorLabel: undefined as string | undefined,
  status: 'ENABLED' as string | undefined
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

const columns = [
  { title: '员工', key: 'staff', width: 160 },
  { title: '部门', dataIndex: 'departmentName', key: 'departmentName', width: 140 },
  { title: '状态', key: 'status', width: 150 },
  { title: '宿舍 / 电表', key: 'dormitory', width: 280 },
  { title: '用餐方案', key: 'mealPlanSummary', width: 220 },
  { title: '备注', dataIndex: 'remark', key: 'remark', width: 220 },
  { title: '更新时间', dataIndex: 'updateTime', key: 'updateTime', width: 180 },
  { title: '操作', key: 'actions', fixed: 'right', width: 170 }
]

const roomConfigColumns = [
  { title: '宿舍房间', key: 'room', width: 220 },
  { title: '标准床位 / 当前占用', key: 'capacity', width: 260 },
  { title: '排序号', dataIndex: 'sortNo', key: 'sortNo', width: 90 },
  { title: '状态', key: 'status', width: 100 },
  { title: '备注', dataIndex: 'remark', key: 'remark', width: 240 },
  { title: '更新时间', dataIndex: 'updateTime', key: 'updateTime', width: 180 },
  { title: '操作', key: 'actions', fixed: 'right', width: 130 }
]

const rows = ref<HrDormitoryStaffItem[]>([])
const occupancyRows = ref<HrDormitoryStaffItem[]>([])
const roomConfigRows = ref<HrDormitoryRoomConfigItem[]>([])
const summary = ref<HrDormitoryOverview>({})
const loading = ref(false)
const saving = ref(false)
const roomConfigLoading = ref(false)
const roomConfigSaving = ref(false)
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

const drawerOpen = ref(false)
const roomConfigDrawerOpen = ref(false)

const form = reactive<HrStaffServicePlan>({
  staffId: undefined,
  liveInDormitory: 1,
  status: 'ENABLED'
})

const roomConfigForm = reactive<HrDormitoryRoomConfigItem>({
  id: undefined,
  bedCapacity: 4,
  sortNo: 0,
  status: 'ENABLED'
})

const { departmentOptions, departmentLoading, searchDepartments } = useDepartmentOptions({ pageSize: 120, preloadSize: 400 })
const { staffOptions, staffLoading, searchStaff, ensureSelectedStaff } = useStaffOptions({ pageSize: 120, preloadSize: 500 })

const drawerTitle = computed(() => (form.staffId ? '编辑宿舍安排' : '新增宿舍安排'))
const roomConfigDrawerTitle = computed(() => (roomConfigForm.id ? '编辑房间配置' : '新增房间配置'))

const occupancyMap = computed(() => {
  const map = new Map<string, { occupiedBeds: number; pendingCount: number; conflictCount: number }>()
  occupancyRows.value.forEach((item) => {
    if (Number(item.liveInDormitory) !== 1) return
    const building = normalizeText(item.dormitoryBuilding)
    const roomNo = normalizeText(item.dormitoryRoomNo)
    if (!building || !roomNo) return
    const key = roomKey({ building, roomNo })
    if (!map.has(key)) {
      map.set(key, { occupiedBeds: 0, pendingCount: 0, conflictCount: 0 })
    }
    const stats = map.get(key)!
    if (item.dormitoryBedNo) {
      stats.occupiedBeds += 1
      if (item.occupancyConflict) stats.conflictCount += 1
    } else {
      stats.pendingCount += 1
    }
  })
  return map
})

const roomConfigSummary = computed(() => {
  const roomCount = roomConfigRows.value.length
  const bedCapacity = roomConfigRows.value.reduce((sum, item) => sum + Number(item.bedCapacity || 0), 0)
  const occupiedBeds = roomConfigRows.value.reduce((sum, item) => sum + roomStats(item).occupiedBeds, 0)
  const availableBeds = roomConfigRows.value.reduce((sum, item) => sum + roomStats(item).availableBeds, 0)
  return { roomCount, bedCapacity, occupiedBeds, availableBeds }
})

function normalizeText(value?: string) {
  return String(value || '').trim()
}

function roomKey(record?: { building?: string; roomNo?: string }) {
  return `${normalizeText(record?.building).toLowerCase()}||${normalizeText(record?.roomNo).toLowerCase()}`
}

function hasAssignedBed(record: HrDormitoryStaffItem) {
  return Number(record.liveInDormitory) === 1 && !!record.dormitoryBuilding && !!record.dormitoryRoomNo && !!record.dormitoryBedNo
}

function roomStats(record: HrDormitoryRoomConfigItem) {
  const occupancy = occupancyMap.value.get(roomKey(record)) || { occupiedBeds: 0, pendingCount: 0, conflictCount: 0 }
  const capacity = Number(record.bedCapacity || 0)
  return {
    occupiedBeds: occupancy.occupiedBeds,
    pendingCount: occupancy.pendingCount,
    conflictCount: occupancy.conflictCount,
    availableBeds: Math.max(capacity - occupancy.occupiedBeds, 0),
    overflowCount: Math.max(occupancy.occupiedBeds - capacity, 0)
  }
}

async function fetchSummary() {
  summary.value = await getHrDormitoryOverview({ status: query.status })
}

async function fetchData() {
  loading.value = true
  try {
    const [res, overview, mapRows] = await Promise.all([
      getHrDormitoryPage({ ...query }) as Promise<PageResult<HrDormitoryStaffItem>>,
      getHrDormitoryOverview({ status: query.status }),
      getHrDormitoryMap({ status: query.status })
    ])
    rows.value = res.list || []
    pagination.total = res.total || 0
    summary.value = overview || {}
    occupancyRows.value = mapRows || []
  } catch {
    rows.value = []
    pagination.total = 0
    summary.value = {}
    occupancyRows.value = []
  } finally {
    loading.value = false
  }
}

async function fetchRoomConfigData() {
  roomConfigLoading.value = true
  try {
    roomConfigRows.value = await getHrDormitoryRoomConfigList({ ...roomConfigQuery })
  } catch {
    roomConfigRows.value = []
  } finally {
    roomConfigLoading.value = false
  }
}

function handleTableChange(pag: any) {
  pagination.current = Number(pag.current || 1)
  pagination.pageSize = Number(pag.pageSize || 10)
  query.pageNo = pagination.current
  query.pageSize = pagination.pageSize
  fetchData()
}

function onReset() {
  query.keyword = undefined
  query.departmentId = undefined
  query.status = 1
  query.dormitoryState = undefined
  query.building = undefined
  query.roomNo = undefined
  query.pageNo = 1
  query.pageSize = 10
  pagination.current = 1
  pagination.pageSize = 10
  fetchData()
}

function resetRoomConfigQuery() {
  roomConfigQuery.keyword = undefined
  roomConfigQuery.building = undefined
  roomConfigQuery.floorLabel = undefined
  roomConfigQuery.status = 'ENABLED'
  fetchRoomConfigData()
}

function rowClassName(record: HrDormitoryStaffItem) {
  if (record.occupancyConflict) return 'hr-row-danger'
  if (Number(record.liveInDormitory) === 1 && !hasAssignedBed(record)) return 'hr-row-warning'
  return ''
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

function resetRoomConfigForm() {
  Object.assign(roomConfigForm, {
    id: undefined,
    building: undefined,
    floorLabel: undefined,
    roomNo: undefined,
    bedCapacity: 4,
    sortNo: 0,
    status: 'ENABLED',
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
    await fetchData()
  } finally {
    saving.value = false
  }
}

function openRoomConfigDrawer(record?: HrDormitoryRoomConfigItem) {
  resetRoomConfigForm()
  roomConfigDrawerOpen.value = true
  if (record) {
    Object.assign(roomConfigForm, { ...record })
  }
}

async function submitRoomConfig() {
  if (!normalizeText(roomConfigForm.building) || !normalizeText(roomConfigForm.roomNo)) {
    message.warning('请填写楼栋和房间号')
    return
  }
  if (Number(roomConfigForm.bedCapacity || 0) <= 0) {
    message.warning('标准床位数必须大于 0')
    return
  }
  roomConfigSaving.value = true
  try {
    await upsertHrDormitoryRoomConfig({
      ...roomConfigForm,
      building: normalizeText(roomConfigForm.building),
      floorLabel: normalizeText(roomConfigForm.floorLabel) || undefined,
      roomNo: normalizeText(roomConfigForm.roomNo)
    })
    message.success('房间配置已保存')
    roomConfigDrawerOpen.value = false
    await Promise.all([fetchRoomConfigData(), fetchSummary()])
  } finally {
    roomConfigSaving.value = false
  }
}

async function deleteRoomConfig(record: HrDormitoryRoomConfigItem) {
  if (!record.id) return
  await deleteHrDormitoryRoomConfig(record.id)
  message.success('房间配置已删除')
  await Promise.all([fetchRoomConfigData(), fetchSummary()])
}

onMounted(() => {
  searchDepartments('')
  searchStaff('')
  fetchData()
  fetchRoomConfigData()
})
</script>

<style scoped>
.dorm-shell {
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
    radial-gradient(circle at top left, rgba(251, 191, 36, 0.2), transparent 32%),
    linear-gradient(135deg, #102a43 0%, #1f3a5f 60%, #2f5b8c 100%);
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

.summary-card,
.mini-card {
  height: 100%;
  border-radius: 18px;
}

.summary-card.warning {
  background: linear-gradient(135deg, rgba(255, 247, 237, 0.9), rgba(255, 237, 213, 0.92));
}

.summary-label {
  color: #64748b;
  font-size: 13px;
}

.summary-value {
  margin-top: 8px;
  font-size: 28px;
  font-weight: 700;
  color: #0f172a;
}

.mini-value {
  margin-top: 8px;
  font-size: 24px;
  font-weight: 700;
  color: #0f172a;
}

.summary-hint {
  margin-top: 6px;
  color: #94a3b8;
  font-size: 12px;
}

.staff-cell,
.plan-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.staff-cell span,
.plan-cell span {
  color: #64748b;
  font-size: 12px;
}

.config-section {
  padding: 18px;
  border: 1px solid #e2e8f0;
  border-radius: 24px;
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
}

.section-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
  margin-bottom: 16px;
}

.section-head h3 {
  margin: 0;
  font-size: 20px;
  color: #102a43;
}

.section-head p {
  margin: 8px 0 0;
  color: #64748b;
}

.config-filters {
  margin: 16px 0;
  padding: 14px;
  border-radius: 18px;
  background: #f8fafc;
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
  .hero-panel,
  .section-head {
    grid-template-columns: 1fr;
    display: grid;
  }
}
</style>
