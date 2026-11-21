<template>
  <PageContainer
    class="rm-page"
    kicker="RESIDENT MANAGEMENT / 02"
    title="长者列表"
    subTitle="所有长者统一入口，查档案、办理业务不再反复跳转"
  >
    <template #extra>
      <a-space wrap>
        <a-button @click="exportCsvData"><DownloadOutlined /> 导出名单</a-button>
        <a-button v-if="canWrite" type="primary" @click="openCreateDrawer"><PlusOutlined /> 新增长者</a-button>
      </a-space>
    </template>

    <section class="rm-card">
      <div class="rm-card__head">
        <h2 class="rm-card__title">长者统一管理</h2>
        <span class="rm-card__sub">所有状态统一查询，常用操作无需切换页面</span>
        <span class="rm-card__extra rm-card__sub">共 {{ total }} 条</span>
      </div>

      <div class="status-tabs" role="tablist" aria-label="按在院状态筛选">
        <button
          v-for="tab in ELDER_STATUS_TABS"
          :key="tab.label"
          type="button"
          role="tab"
          class="status-tab"
          :class="{ 'is-active': query.lifecycleStatus === tab.value }"
          :aria-selected="query.lifecycleStatus === tab.value"
          @click="applyStatusChip(tab.value)"
        >{{ tab.label }}</button>
      </div>

      <div class="filter-row">
        <a-input-search
          v-model:value="query.keyword"
          class="filter-row__search"
          placeholder="搜索姓名 / 拼音 / 身份证 / 房间号 / 床位号"
          allow-clear
          @search="runSearch"
        />
        <a-auto-complete
          v-model:value="query.careLevel"
          class="filter-row__select"
          :options="careLevelOptions"
          placeholder="护理等级：全部"
          allow-clear
          @select="runSearch"
          @change="onCareLevelChange"
        />
        <a-select
          v-model:value="query.building"
          class="filter-row__select"
          :options="buildingOptions"
          placeholder="全部楼栋"
          allow-clear
          @change="runSearch"
        />
        <a-button v-if="activeFilterTags.length" @click="reset">清空筛选</a-button>
      </div>
      <div v-if="activeFilterTags.length" class="filter-tags">
        <span v-for="tag in activeFilterTags" :key="tag" class="rm-pill">{{ tag }}</span>
      </div>

      <div v-if="selectedCount" class="selection-bar">
        <span class="selection-bar__count">已选 {{ selectedCount }} 位</span>
        <a-button size="small" @click="exportSelected">导出所选</a-button>
        <template v-if="selectedCount === 1 && canWrite">
          <a-button size="small" @click="openChangeBedSelected">换床</a-button>
          <a-button size="small" @click="openCheckoutSelected">退住申请</a-button>
          <a-button size="small" @click="openBindFamilySelected">绑定家属</a-button>
          <a-button size="small" @click="printElderQrSelected">打印二维码</a-button>
          <a-button size="small" danger @click="deleteSelected">删除</a-button>
        </template>
        <span v-else-if="canWrite" class="selection-bar__hint">换床、退住、删除等档案操作需单选一位长者</span>
        <a-button size="small" type="text" class="selection-bar__clear" @click="selectedRowKeys = []">取消选择</a-button>
      </div>

      <DataTable
        :filter-model="query"
        @clear-filters="runSearch"
        row-key="id"
        :columns="columns"
        :data-source="rows"
        :loading="loading"
        :pagination="false"
        :row-selection="rowSelection"
        :empty-title="emptyStateTitle"
        :empty-description="emptyStateDescription"
        :scroll="{ x: 1260 }"
        @change="onTableChange"
      >
        <template #emptyExtra>
          <a-space wrap>
            <a-button v-if="canWrite" type="primary" @click="openCreateDrawer">新增长者</a-button>
            <a-button v-if="activeFilterTags.length" @click="reset">清空筛选</a-button>
          </a-space>
        </template>
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'fullName'">
            <div class="elder-cell">
              <span class="rm-avatar">{{ avatarText(record.fullName) }}</span>
              <div class="elder-cell__main">
                <button type="button" class="rm-link elder-cell__name" @click="goDetail(record.id)">{{ record.fullName }}</button>
                <span v-if="record.riskPrecommit === 'RESCUE_FIRST'" class="rm-pill rm-pill--danger">抢救优先</span>
                <span v-else-if="record.riskPrecommit === 'NOTIFY_FAMILY_FIRST'" class="rm-pill rm-pill--info">先通知家属</span>
                <span v-if="record.diseases && record.diseases.length" class="rm-pill rm-pill--warning">慢病 {{ record.diseases.length }}</span>
              </div>
            </div>
          </template>
          <template v-else-if="column.key === 'elderCode'">{{ record.elderCode || '--' }}</template>
          <template v-else-if="column.key === 'birthDate'">
            {{ ageOf(record.birthDate) !== null ? `${ageOf(record.birthDate)}岁` : '--' }} / {{ genderText(record.gender) }}
          </template>
          <template v-else-if="column.key === 'bed'">
            <template v-if="record.bedNo || record.roomNo">
              {{ buildingOf(record) ? `${buildingOf(record)} ` : '' }}{{ record.bedNo ? bedLabel({ roomNo: record.roomNo, bedNo: record.bedNo } as BedItem) : record.roomNo }}
            </template>
            <span v-else class="muted">待分配</span>
          </template>
          <template v-else-if="column.key === 'careLevel'">{{ record.careLevel || '--' }}</template>
          <template v-else-if="column.key === 'responsibleStaff'">
            {{ record.responsibleStaffName || '--' }}
          </template>
          <template v-else-if="column.key === 'status'">
            <span class="rm-pill" :class="statusPillClass(record.lifecycleStatus)">{{ statusText(record) }}</span>
          </template>
          <template v-else-if="column.key === 'admissionDate'">{{ record.admissionDate || '--' }}</template>
          <template v-else-if="column.key === 'action'">
            <div class="row-actions">
              <button type="button" class="rm-link" @click="goDetail(record.id)">详情</button>
              <a-dropdown trigger="click">
                <button type="button" class="rm-link">办理 <DownOutlined /></button>
                <template #overlay>
                  <a-menu>
                    <a-menu-item key="admission" @click="goWithElder('/elder/admission-processing', record)">入住办理</a-menu-item>
                    <a-menu-item v-if="canWrite" key="bed" @click="openChangeBed(record)">换床</a-menu-item>
                    <a-menu-item key="outing" @click="goBedRecord(record, 'OUTING', 'outing')">外出登记</a-menu-item>
                    <a-menu-item key="medical-outing" @click="goBedRecord(record, 'MEDICAL_OUTING', 'medical')">外出就医登记</a-menu-item>
                    <a-menu-item key="checkout" @click="openCheckout(record)">退住申请</a-menu-item>
                    <a-menu-item key="settlement" @click="goSettlement(record)">费用与退住</a-menu-item>
                    <a-menu-divider />
                    <a-menu-item key="assessment" @click="goWithElder('/elder/assessment/ability/archive', record)">评估档案</a-menu-item>
                    <a-menu-item key="contracts" @click="router.push({ path: `/elder/detail/${record.id}`, query: { tab: 'contract' } })">合同票据</a-menu-item>
                    <a-menu-item v-if="canWrite" key="family" @click="openBindFamily(record)">绑定家属</a-menu-item>
                    <a-menu-item key="qr" @click="printElderQr(record)">打印二维码</a-menu-item>
                    <template v-if="canWrite">
                      <a-menu-divider />
                      <a-menu-item key="delete" danger @click="confirmDelete(record)">删除档案</a-menu-item>
                    </template>
                  </a-menu>
                </template>
              </a-dropdown>
              <button v-if="canWrite" type="button" class="rm-link" @click="openEditDrawer(record)">编辑</button>
            </div>
          </template>
        </template>
      </DataTable>

      <div class="pager-row">
        <a-pagination
          :current="query.pageNo"
          :page-size="query.pageSize"
          :total="total"
          show-size-changer
          :show-total="(n: number) => `共 ${n} 条记录`"
          @change="onPageChange"
          @showSizeChange="onPageSizeChange"
        />
      </div>
    </section>

    <div class="rm-note">
      <SafetyCertificateOutlined />
      小提示：新建 / 编辑在右侧抽屉完成；点击“详情”进入长者档案；外出、退住等状态办理可在“办理”菜单直接发起。
    </div>

    <a-drawer
      v-model:open="formDrawerOpen"
      :title="formDrawerElder ? `编辑档案 · ${formDrawerElder.fullName || ''}` : '新增长者'"
      width="820"
      destroy-on-close
      :mask-closable="false"
    >
      <ElderEdit
        v-if="formDrawerElder"
        :key="String(formDrawerElder.id)"
        embedded
        :elder-id="formDrawerElder.id"
        @saved="onFormSaved"
        @cancel="formDrawerOpen = false"
      />
      <ElderCreate v-else embedded @saved="onFormSaved" @cancel="formDrawerOpen = false" />
    </a-drawer>

    <a-modal :mask-closable="false" v-model:open="changeBedOpen" title="换床" width="420px" @ok="submitChangeBed" @cancel="() => (changeBedOpen = false)">
      <a-form ref="changeBedFormRef" :model="changeBedForm" :rules="changeBedRules" layout="vertical">
        <a-form-item label="选择床位" name="bedId">
          <a-select v-model:value="changeBedForm.bedId" placeholder="请选择床位">
            <a-select-option v-for="item in bedOptions" :key="item.value" :value="item.value">{{ item.label }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="开始日期" name="startDate">
          <a-date-picker v-model:value="changeBedForm.startDate" value-format="YYYY-MM-DD" style="width: 100%" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal :mask-closable="false" v-model:open="bindOpen" title="绑定家属" width="420px" @ok="submitBindFamily" @cancel="() => (bindOpen = false)">
      <a-form ref="bindFormRef" :model="bindForm" :rules="bindRules" layout="vertical">
        <a-form-item label="家属" name="familyUserId">
          <a-select
            v-model:value="bindForm.familyUserId"
            allow-clear
            show-search
            :filter-option="false"
            placeholder="输入姓名/手机号搜索"
            @search="searchFamilyUsers"
            @focus="() => loadFamilyUsers()"
          >
            <a-select-option v-for="item in familyOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="关系" name="relation">
          <a-input v-model:value="bindForm.relation" placeholder="如：子女/配偶" />
        </a-form-item>
        <a-form-item label="主联系人">
          <a-switch v-model:checked="bindForm.isPrimary" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="qrOpen" title="老人二维码" width="360px" @ok="printQr" ok-text="打印" @cancel="() => (qrOpen = false)">
      <div class="qr-preview">
        <img :src="qrDataUrl" alt="qr" />
        <div class="qr-text">{{ qrText }}</div>
      </div>
    </a-modal>

    <ExportConfirmModal
      v-model:open="exportConfirmOpen"
      module="ELDER_LIST"
      module-label="长者名单"
      :scope="exportScope"
      :estimated-rows="exportRows.length"
      @confirmed="doExportCsv"
    />
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import type { FormInstance, FormRules } from 'ant-design-vue'
import { DownOutlined, DownloadOutlined, PlusOutlined, SafetyCertificateOutlined } from '@ant-design/icons-vue'
import QRCode from 'qrcode'
import PageContainer from '../../components/PageContainer.vue'
import DataTable from '../../components/DataTable.vue'
import ExportConfirmModal from '../../components/ExportConfirmModal.vue'
import { useLiveSyncRefresh } from '../../composables/useLiveSyncRefresh'
import { useUserStore } from '../../stores/user'
import { exportCsv } from '../../utils/export'
import { normalizeLifecycleStage } from '../../utils/lifecycleStage'
import { residentStatusText as statusText, ageOf } from '../../utils/elderStatus'
import { completeExport } from '../../api/complianceSecurity'
import { getElderPage, assignBed, bindFamily, deleteElder } from '../../api/elder'
import { getBedList } from '../../api/bed'
import { bedLabel } from './resident360/bedPanoramaModel'
import { getCareLevelList } from '../../api/nursing'
import { getFamilyUserPage } from '../../api/family'
import type { BedItem, ElderItem, FamilyBindRequest, PageResult, FamilyUserItem, Id } from '../../types/api'
import { avatarText, canWriteElder, ELDER_STATUS_TABS, statusPillClass } from './elderListView'
import './residentUi.css'

// 抽屉里才用到，按需加载，列表首屏不背表单代码
const ElderCreate = defineAsyncComponent(() => import('./Create.vue'))
const ElderEdit = defineAsyncComponent(() => import('./Edit.vue'))

// responsibleStaffName 由后端同期补充到 ElderResponse，类型未落地前用本地交叉类型
type ElderRow = ElderItem & { responsibleStaffName?: string }

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const canWrite = computed(() => canWriteElder(userStore.roles))
const loading = ref(false)
const rows = ref<ElderRow[]>([])
const total = ref(0)
const selectedRowKeys = ref<Id[]>([])
const skipNextRouteWatch = ref(false)
const routeSignature = ref('')
const DEFAULT_PAGE_SIZE = 20
const ELDER_LIST_ROUTE_KEYS = ['keyword', 'fullName', 'idCardNo', 'bedNo', 'roomNo', 'careLevel', 'building', 'lifecycleStatus', 'status', 'sortBy', 'sortOrder', 'pageNo', 'pageSize'] as const

const query = reactive({
  keyword: undefined as string | undefined,
  // 以下精确条件只来自其他页面的跳转链接（如 ?fullName=），界面上统一用 keyword 搜索
  fullName: undefined as string | undefined,
  idCardNo: undefined as string | undefined,
  bedNo: undefined as string | undefined,
  roomNo: undefined as string | undefined,
  careLevel: undefined as string | undefined,
  building: undefined as string | undefined,
  lifecycleStatus: undefined as string | undefined,
  sortBy: undefined as string | undefined,
  sortOrder: undefined as string | undefined,
  pageNo: 1,
  pageSize: DEFAULT_PAGE_SIZE
})

const columns = [
  { title: '长者', dataIndex: 'fullName', key: 'fullName', width: 220, sorter: true, fixed: 'left' },
  { title: '档案编号', dataIndex: 'elderCode', key: 'elderCode', width: 110 },
  { title: '年龄 / 性别', dataIndex: 'birthDate', key: 'birthDate', width: 110, sorter: true },
  { title: '楼栋 / 床位', dataIndex: 'bedNo', key: 'bed', width: 150, sorter: true },
  { title: '护理等级', dataIndex: 'careLevel', key: 'careLevel', width: 110, sorter: true },
  { title: '责任护理员', key: 'responsibleStaff', width: 110 },
  { title: '在院状态', dataIndex: 'status', key: 'status', width: 100, sorter: true },
  { title: '入住日期', dataIndex: 'admissionDate', key: 'admissionDate', width: 110 },
  { title: '快捷操作', key: 'action', width: 170, fixed: 'right' }
]

const selectedCount = computed(() => selectedRowKeys.value.length)
const selectedRows = computed(() => rows.value.filter((item) => selectedRowKeys.value.some((id) => String(id) === String(item.id))))
const activeFilterTags = computed(() => {
  const tags: string[] = []
  if (query.keyword) tags.push(`关键词: ${query.keyword}`)
  if (query.fullName) tags.push(`姓名: ${query.fullName}`)
  if (query.idCardNo) tags.push(`身份证: ${query.idCardNo}`)
  if (query.bedNo) tags.push(`床位: ${query.bedNo}`)
  if (query.roomNo) tags.push(`房间: ${query.roomNo}`)
  if (query.careLevel) tags.push(`护理等级: ${query.careLevel}`)
  if (query.building) tags.push(`楼栋: ${query.building}`)
  if (query.lifecycleStatus) tags.push(`状态: ${statusText({ lifecycleStatus: query.lifecycleStatus })}`)
  return tags
})
const emptyStateTitle = computed(() => (
  activeFilterTags.value.length ? '当前筛选下没有匹配的长者' : '还没有长者档案'
))
const emptyStateDescription = computed(() => (
  activeFilterTags.value.length
    ? '可以先清空筛选范围重新查看，或直接创建新的长者档案。'
    : '从这里新建第一位长者，后续就能继续入住、评估、合同和家属绑定等业务。'
))

const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: Id[]) => {
    selectedRowKeys.value = keys
  }
}))

const beds = ref<BedItem[]>([])
const bedOptions = computed(() =>
  beds.value
    .filter((b) => !b.elderId && (b.status === 1 || b.status === undefined))
    .map((b) => ({ label: `${b.roomNo || b.roomId}-${b.bedNo}`, value: b.id }))
)
// 分页接口不返回楼栋，借床位列表（换床弹窗本来就要加载）补上
const buildingByBedId = computed(() => new Map(beds.value.map((b) => [String(b.id), b.building || ''])))
const buildingOptions = computed(() => Array.from(new Set(beds.value.map((b) => b.building || '').filter(Boolean)))
  .sort((a, b) => a.localeCompare(b, 'zh-CN', { numeric: true }))
  .map((value) => ({ label: value, value })))
function buildingOf(row: ElderRow) {
  const bedId = row.bedId ?? row.currentBed?.id
  return bedId == null ? '' : buildingByBedId.value.get(String(bedId)) || ''
}

const careLevelOptions = ref<Array<{ value: string }>>([])
async function loadCareLevels() {
  try {
    const list: any[] = await getCareLevelList(undefined, { silent403: true })
    careLevelOptions.value = Array.from(new Set((list || []).map((item) => String(item.levelName || '').trim()).filter(Boolean)))
      .map((value) => ({ value }))
  } catch {
    careLevelOptions.value = []
  }
}
function onCareLevelChange(value?: string) {
  // allow-clear 清空时立即刷新；手输时等回车/选中
  if (!value) runSearch()
}

const changeBedOpen = ref(false)
const changeBedFormRef = ref<FormInstance>()
const changeBedForm = reactive<{ elderId?: Id; bedId?: Id; startDate?: string }>({})
const changeBedRules: FormRules = {
  bedId: [{ required: true, message: '请选择床位' }],
  startDate: [{ required: true, message: '请选择开始日期' }]
}

const bindOpen = ref(false)
const bindFormRef = ref<FormInstance>()
const bindForm = reactive<FamilyBindRequest>({ familyUserId: '', elderId: '', relation: '', isPrimary: false })
const bindRules: FormRules = {
  familyUserId: [{ required: true, message: '请选择家属' }],
  relation: [{ required: true, message: '请输入关系' }]
}
const familyOptions = ref<Array<{ label: string; value: Id }>>([])

const qrOpen = ref(false)

// 新建/编辑抽屉：formDrawerElder 为空即新建
const formDrawerOpen = ref(false)
const formDrawerElder = ref<ElderRow | null>(null)

function openCreateDrawer() {
  formDrawerElder.value = null
  formDrawerOpen.value = true
}

function openEditDrawer(row: ElderRow) {
  formDrawerElder.value = row
  formDrawerOpen.value = true
}

function onFormSaved() {
  formDrawerOpen.value = false
  fetchData()
}

function genderText(gender?: number | null) {
  if (gender === 1) return '男'
  if (gender === 2) return '女'
  return '--'
}

const qrDataUrl = ref('')
const qrText = ref('')

function applyStatusChip(value?: string) {
  query.lifecycleStatus = value
  query.pageNo = 1
  syncQueryToRoute()
    .then(() => fetchData())
    .catch(() => {})
}

function requireSingleSelection(actionLabel: string) {
  if (selectedRows.value.length !== 1) {
    message.info(`请先勾选 1 位长者后再${actionLabel}`)
    return null
  }
  return selectedRows.value[0]
}

async function fetchData() {
  loading.value = true
  selectedRowKeys.value = []
  try {
    const res: PageResult<ElderRow> = await getElderPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      keyword: query.keyword,
      fullName: query.fullName,
      idCardNo: query.idCardNo,
      bedNo: query.bedNo,
      roomNo: query.roomNo,
      careLevel: query.careLevel,
      building: query.building,
      lifecycleStatus: query.lifecycleStatus,
      sortBy: query.sortBy,
      sortOrder: query.sortOrder
    })
    rows.value = res.list.map((r: any) => ({
      ...r,
      bedNo: r.bedNo ?? r.currentBed?.bedNo,
      roomNo: r.roomNo ?? r.currentBed?.roomNo,
      lifecycleStage: normalizeLifecycleStage(r.lifecycleStage, r.lifecycleContractStatus)
    }))
    total.value = res.total || res.list.length
  } catch {
    rows.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

async function loadBeds() {
  try {
    beds.value = await getBedList({ silent403: true })
  } catch {
    beds.value = []
  }
}

async function loadFamilyUsers(keyword?: string) {
  try {
    const res: PageResult<FamilyUserItem> = await getFamilyUserPage({
      pageNo: 1,
      pageSize: 50,
      realName: keyword,
      phone: keyword,
      status: 1
    })
    familyOptions.value = res.list.map((item) => ({
      value: item.id,
      label: `${item.realName || '未命名'}${item.phone ? `（${item.phone}）` : ''}`
    }))
  } catch {
    familyOptions.value = []
  }
}

async function searchFamilyUsers(val: string) {
  await loadFamilyUsers(val)
}

function firstRouteQueryText(value: unknown) {
  if (Array.isArray(value)) return firstRouteQueryText(value[0])
  if (value == null) return ''
  return String(value).trim()
}

function parsePositiveInt(value: unknown, fallback: number) {
  const parsed = Number(firstRouteQueryText(value))
  if (!Number.isFinite(parsed) || parsed <= 0) return fallback
  return Math.round(parsed)
}

function flattenRouteQuery(source: Record<string, unknown>) {
  return Object.entries(source || {}).reduce<Record<string, string>>((acc, [key, value]) => {
    const text = firstRouteQueryText(value)
    if (!text) return acc
    acc[key] = text
    return acc
  }, {})
}

function buildRouteSignature(source: Record<string, unknown>) {
  return [...ELDER_LIST_ROUTE_KEYS]
    .sort()
    .map((key) => `${key}:${firstRouteQueryText(source[key])}`)
    .join('|')
}

function applyQueryFromRoute() {
  query.keyword = firstRouteQueryText(route.query.keyword) || undefined
  query.fullName = firstRouteQueryText(route.query.fullName) || undefined
  query.idCardNo = firstRouteQueryText(route.query.idCardNo) || undefined
  query.bedNo = firstRouteQueryText(route.query.bedNo) || undefined
  query.roomNo = firstRouteQueryText(route.query.roomNo) || undefined
  query.careLevel = firstRouteQueryText(route.query.careLevel) || undefined
  query.building = firstRouteQueryText(route.query.building) || undefined
  const lifecycleStatus = firstRouteQueryText(route.query.lifecycleStatus).toUpperCase()
  if (lifecycleStatus) {
    query.lifecycleStatus = lifecycleStatus
  } else {
    const status = Number(firstRouteQueryText(route.query.status))
    query.lifecycleStatus = status === 1 ? 'IN_HOSPITAL' : status === 2 ? 'OUTING' : status === 3 ? 'DISCHARGED' : undefined
  }
  query.sortBy = firstRouteQueryText(route.query.sortBy) || undefined
  const order = firstRouteQueryText(route.query.sortOrder).toLowerCase()
  query.sortOrder = order === 'asc' || order === 'desc' ? order : undefined
  query.pageNo = parsePositiveInt(route.query.pageNo, 1)
  query.pageSize = parsePositiveInt(route.query.pageSize, DEFAULT_PAGE_SIZE)
}

function buildQueryRouteQuery() {
  const nextQuery: Record<string, string> = {}
  Object.entries(flattenRouteQuery(route.query as Record<string, unknown>)).forEach(([key, value]) => {
    if ((ELDER_LIST_ROUTE_KEYS as readonly string[]).includes(key)) return
    nextQuery[key] = value
  })
  if (query.keyword) nextQuery.keyword = query.keyword
  if (query.fullName) nextQuery.fullName = query.fullName
  if (query.idCardNo) nextQuery.idCardNo = query.idCardNo
  if (query.bedNo) nextQuery.bedNo = query.bedNo
  if (query.roomNo) nextQuery.roomNo = query.roomNo
  if (query.careLevel) nextQuery.careLevel = query.careLevel
  if (query.building) nextQuery.building = query.building
  if (query.lifecycleStatus) nextQuery.lifecycleStatus = query.lifecycleStatus
  if (query.sortBy) nextQuery.sortBy = query.sortBy
  if (query.sortOrder) nextQuery.sortOrder = query.sortOrder
  nextQuery.pageNo = String(parsePositiveInt(query.pageNo, 1))
  nextQuery.pageSize = String(parsePositiveInt(query.pageSize, DEFAULT_PAGE_SIZE))
  return nextQuery
}

function hasSameRouteQuery(nextQuery: Record<string, string>) {
  const currentQuery = flattenRouteQuery(route.query as Record<string, unknown>)
  const currentKeys = Object.keys(currentQuery)
  const nextKeys = Object.keys(nextQuery)
  if (currentKeys.length !== nextKeys.length) return false
  return nextKeys.every((key) => currentQuery[key] === nextQuery[key])
}

async function syncQueryToRoute() {
  const nextQuery = buildQueryRouteQuery()
  if (hasSameRouteQuery(nextQuery)) return
  skipNextRouteWatch.value = true
  routeSignature.value = buildRouteSignature(nextQuery)
  await router.replace({ path: route.path, query: nextQuery })
}

async function runSearch() {
  query.pageNo = 1
  await syncQueryToRoute()
  await fetchData()
}

function reset() {
  query.pageNo = 1
  query.pageSize = DEFAULT_PAGE_SIZE
  query.keyword = undefined
  query.fullName = undefined
  query.idCardNo = undefined
  query.bedNo = undefined
  query.roomNo = undefined
  query.careLevel = undefined
  query.building = undefined
  query.lifecycleStatus = undefined
  query.sortBy = undefined
  query.sortOrder = undefined
  selectedRowKeys.value = []
  syncQueryToRoute()
    .then(() => fetchData())
    .catch(() => {})
}

function onPageChange(page: number) {
  query.pageNo = page
  syncQueryToRoute()
    .then(() => fetchData())
    .catch(() => {})
}

function onPageSizeChange(current: number, size: number) {
  query.pageNo = 1
  query.pageSize = size
  selectedRowKeys.value = []
  syncQueryToRoute()
    .then(() => fetchData())
    .catch(() => {})
}

function onTableChange(_: any, __: any, sorter: any) {
  if (Array.isArray(sorter)) sorter = sorter[0]
  query.sortBy = sorter?.field || undefined
  query.sortOrder = sorter?.order === 'ascend' ? 'asc' : sorter?.order === 'descend' ? 'desc' : undefined
  query.pageNo = 1
  syncQueryToRoute()
    .then(() => fetchData())
    .catch(() => {})
}

// 导出二次确认（合规留痕）：先弹窗确认拿一次性 exportTicket，导出后回填行数
const exportConfirmOpen = ref(false)
const exportRows = ref<ElderRow[]>([])
const exportScope = ref('')

function openExport(list: ElderRow[], scope: string) {
  if (!list.length) {
    message.warning('当前列表暂无数据可导出')
    return
  }
  exportRows.value = list
  exportScope.value = scope
  exportConfirmOpen.value = true
}

function exportCsvData() {
  const tags = activeFilterTags.value
  openExport(rows.value, tags.length ? `筛选：${tags.join('、')}（当前页 ${rows.value.length} 行）` : `全部（当前页 ${rows.value.length} 行）`)
}

function exportSelected() {
  openExport(selectedRows.value, `已勾选 ${selectedRows.value.length} 位长者`)
}

function doExportCsv(exportTicket: string) {
  const list = exportRows.value
  exportCsv(
    list.map((r) => ({
      姓名: r.fullName,
      档案编号: r.elderCode || '',
      身份证: r.idCardNo || '',
      生日: r.birthDate || '',
      家庭地址: r.homeAddress || '',
      楼栋: buildingOf(r),
      房间号: r.roomNo || '',
      床位号: r.bedNo || '',
      护理等级: r.careLevel || '',
      责任护理员: r.responsibleStaffName || '',
      状态: statusText(r),
      入住日期: r.admissionDate || ''
    })),
    'elder-list.csv'
  )
  completeExport({ exportTicket, rowCount: list.length }).catch(() => {})
}

function goDetail(id: Id) {
  router.push(`/elder/detail/${id}`)
}

// 办理菜单：目标页都按 elderId（normalizeResidentId 兼容 residentId）预选长者
function goWithElder(path: string, row: ElderItem) {
  router.push({ path, query: { elderId: String(row.id), elderName: row.fullName || '' } })
}

const FEE_EXIT_PAGE = '/elder/status-change/discharge-settlement'

function goSettlement(row: ElderItem) {
  router.push({ path: FEE_EXIT_PAGE, query: { tab: 'fee', elderId: String(row.id) } })
}

function goBedRecord(row: ElderItem, recordType: 'OUTING' | 'MEDICAL_OUTING', action: 'outing' | 'medical') {
  router.push({ path: '/elder/bed-panorama', query: { view: 'records', recordType, elderId: String(row.id), action } })
}

function openChangeBed(row: ElderItem) {
  changeBedForm.elderId = row.id
  changeBedForm.bedId = undefined
  changeBedForm.startDate = undefined
  changeBedOpen.value = true
}

function openChangeBedSelected() {
  const row = requireSingleSelection('换床')
  if (!row) return
  openChangeBed(row)
}

async function submitChangeBed() {
  if (!changeBedFormRef.value) return
  try {
    await changeBedFormRef.value.validate()
    if (!changeBedForm.elderId || !changeBedForm.bedId || !changeBedForm.startDate) return
    await assignBed(changeBedForm.elderId, changeBedForm.bedId, changeBedForm.startDate)
    message.success('换床成功')
    changeBedOpen.value = false
    fetchData()
  } catch {
    message.error('换床失败')
  }
}

function openCheckout(row: ElderItem) {
  router.push({ path: FEE_EXIT_PAGE, query: { tab: 'discharge', elderId: String(row.id), create: '1' } })
}

function openCheckoutSelected() {
  const row = requireSingleSelection('提交退住申请')
  if (!row) return
  openCheckout(row)
}

function openBindFamily(row: ElderItem) {
  bindForm.elderId = row.id
  bindForm.familyUserId = ''
  bindForm.relation = ''
  bindForm.isPrimary = false
  loadFamilyUsers()
  bindOpen.value = true
}

function openBindFamilySelected() {
  const row = requireSingleSelection('绑定家属')
  if (!row) return
  openBindFamily(row)
}

async function submitBindFamily() {
  if (!bindFormRef.value) return
  try {
    await bindFormRef.value.validate()
    if (!bindForm.elderId) return
    await bindFamily(bindForm)
    message.success('绑定成功')
    bindOpen.value = false
  } catch {
    message.error('绑定失败')
  }
}

function confirmDelete(row: ElderItem) {
  Modal.confirm({
    title: '确认删除长者档案？',
    content: `将逻辑删除“${row.fullName || row.id}”档案。仅未占床、未绑定家属、无入住/合同记录的档案允许删除。`,
    okText: '确认删除',
    okButtonProps: { danger: true },
    async onOk() {
      await deleteElder(row.id)
      message.success('长者档案已删除')
      if (selectedRowKeys.value.some((id) => String(id) === String(row.id))) {
        selectedRowKeys.value = selectedRowKeys.value.filter((id) => String(id) !== String(row.id))
      }
      await fetchData()
    }
  })
}

function deleteSelected() {
  const row = requireSingleSelection('删除档案')
  if (!row) return
  confirmDelete(row)
}

async function printElderQr(row: ElderItem) {
  qrText.value = `ELDER:${row.id}`
  qrDataUrl.value = await QRCode.toDataURL(qrText.value)
  qrOpen.value = true
}

function printElderQrSelected() {
  const row = requireSingleSelection('打印二维码')
  if (!row) return
  printElderQr(row)
}

function printQr() {
  if (!qrDataUrl.value) return
  const win = window.open('', '_blank')
  if (!win) return
  win.document.write(`<img src="${qrDataUrl.value}" style="width:220px;height:220px"/>`)
  win.document.write(`<div style="margin-top:8px;font-size:12px">${qrText.value}</div>`)
  win.print()
  win.close()
}

useLiveSyncRefresh({
  topics: ['elder', 'bed', 'lifecycle', 'finance'],
  refresh: () => {
    fetchData()
    loadBeds()
  },
  debounceMs: 600
})

onMounted(() => {
  applyQueryFromRoute()
  routeSignature.value = buildRouteSignature(route.query as Record<string, unknown>)
  fetchData()
  loadBeds()
  loadCareLevels()
  syncQueryToRoute().catch(() => {})
})

watch(
  () => route.query,
  (queryMap) => {
    const nextSignature = buildRouteSignature(queryMap as Record<string, unknown>)
    if (skipNextRouteWatch.value) {
      skipNextRouteWatch.value = false
      routeSignature.value = nextSignature
      return
    }
    if (nextSignature === routeSignature.value) return
    routeSignature.value = nextSignature
    applyQueryFromRoute()
    fetchData()
  },
  { deep: true }
)
</script>

<style scoped>
.status-tabs {
  display: inline-flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-bottom: 14px;
  padding: 4px;
  border: 1px solid var(--rm-border);
  border-radius: 10px;
  background: #f6f9f8;
}

.status-tab {
  padding: 5px 14px;
  border: 0;
  border-radius: 8px;
  background: transparent;
  color: var(--rm-muted);
  font: inherit;
  font-size: 13px;
  cursor: pointer;
}

.status-tab:hover { color: var(--rm-green); }

.status-tab.is-active {
  background: #fff;
  color: var(--rm-green);
  font-weight: 600;
  box-shadow: 0 1px 3px rgba(31, 45, 40, 0.1);
}

.filter-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 12px;
}

.filter-row__search { flex: 1 1 320px; }
.filter-row__select { width: 200px; }

.filter-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 12px;
}

/* 勾选后才出现的操作条：不占常驻空间，动作跟着选择走 */
.selection-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 12px;
  padding: 10px 14px;
  border: 1px solid #cfe6db;
  border-radius: 10px;
  background: var(--rm-green-soft);
}

.selection-bar__count {
  margin-right: 4px;
  color: var(--rm-green-dark);
  font-size: 13px;
  font-weight: 600;
}

.selection-bar__hint {
  color: var(--rm-muted);
  font-size: 12px;
}

.selection-bar__clear {
  margin-left: auto;
  color: var(--rm-muted);
}

.elder-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.elder-cell__main {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px 6px;
  min-width: 0;
}

.elder-cell__name {
  color: var(--rm-text);
  font-size: 14px;
  font-weight: 600;
}

.muted { color: var(--rm-muted); }

.row-actions {
  display: flex;
  align-items: center;
  gap: 14px;
  white-space: nowrap;
}

.pager-row {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.qr-preview {
  display: grid;
  justify-items: center;
  gap: 8px;
}

.qr-text {
  font-size: 12px;
  color: var(--muted);
}
</style>
