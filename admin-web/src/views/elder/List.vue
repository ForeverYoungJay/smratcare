<template>
  <PageContainer title="老人列表" subTitle="档案查询与床位管理">
    <a-card class="card-elevated" :bordered="false">
      <a-form :model="query" layout="inline" class="search-form">
        <a-form-item label="姓名">
          <ElderNameAutocomplete v-model:value="query.fullName" placeholder="姓名(编号)" width="180px" @select="runSearch" />
        </a-form-item>
        <a-form-item label="身份证">
          <a-input v-model:value="query.idCardNo" placeholder="身份证号" allow-clear />
        </a-form-item>
        <a-form-item label="床位号">
          <a-input v-model:value="query.bedNo" placeholder="床位号" allow-clear />
        </a-form-item>
        <a-form-item label="护理等级">
          <a-input v-model:value="query.careLevel" placeholder="等级" allow-clear />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="query.status" allow-clear style="width: 120px">
            <a-select-option :value="1">在院</a-select-option>
            <a-select-option :value="2">请假</a-select-option>
            <a-select-option :value="3">离院</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="runSearch">搜索</a-button>
            <a-button @click="reset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <FlowGuardBar
        title="长者入院守卫"
        :subject="elderFlowSubject"
        :stage-text="elderFlowStageText"
        :stage-color="elderFlowStageColor"
        :steps="elderFlowSteps"
        :current-index="elderFlowCurrentIndex"
        :blockers="elderFlowBlockers"
        :hint="elderFlowHint"
        style="margin-bottom: 12px"
      />
      <a-space wrap style="margin-bottom: 10px">
        <a-tag color="gold">待评估 {{ lifecycleStageCounters.pendingAssessment }}</a-tag>
        <a-tag color="blue">待办理入住 {{ lifecycleStageCounters.pendingBedSelect }}</a-tag>
        <a-tag color="purple">待签署 {{ lifecycleStageCounters.pendingSign }}</a-tag>
        <a-tag color="green">已签署 {{ lifecycleStageCounters.signed }}</a-tag>
      </a-space>
      <div class="table-actions">
        <a-space>
          <a-button type="primary" @click="goCreate">新建老人</a-button>
          <a-button @click="exportCsvData">导出CSV</a-button>
          <a-button :disabled="selectedCount !== 1" @click="goDetailSelected">详情</a-button>
          <a-button :disabled="selectedCount !== 1" @click="goEditSelected">编辑</a-button>
          <a-button :disabled="selectedCount !== 1" @click="openChangeBedSelected">换床</a-button>
          <a-button :disabled="selectedCount !== 1" @click="openCheckoutSelected">退住申请</a-button>
          <a-button :disabled="selectedCount !== 1" @click="openBindFamilySelected">绑定家属</a-button>
          <a-button :disabled="selectedCount !== 1" @click="printElderQrSelected">打印二维码</a-button>
          <span class="selection-tip">已勾选 {{ selectedCount }} 条</span>
        </a-space>
      </div>

      <a-table
        :data-source="rows"
        :columns="columns"
        :loading="loading"
        :pagination="false"
        row-key="id"
        :row-selection="rowSelection"
        :scroll="{ x: 1450, y: 520 }"
        @change="onTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="statusTag(record.status)">{{ statusText(record.status) }}</a-tag>
          </template>
          <template v-else-if="column.key === 'lifecycleStage'">
            <a-tag :color="lifecycleStageColor(resolveLifecycleStage(record))">
              {{ lifecycleStageLabel(resolveLifecycleStage(record)) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'sourceType'">
            <a-tag :color="sourceTypeColor(record.sourceType)">{{ sourceTypeText(record.sourceType) }}</a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space size="small">
              <a-button type="link" size="small" @click="goDetail(record.id)">详情</a-button>
              <a-button type="link" size="small" @click="goInHospitalOverview(record)">总览</a-button>
              <a-button type="link" size="small" @click="goAssessmentArchive(record)">评估</a-button>
              <a-button type="link" size="small" @click="goContractsInvoices(record)">合同票据</a-button>
            </a-space>
          </template>
        </template>
      </a-table>

      <a-pagination
        style="margin-top: 16px; text-align: right;"
        :current="query.pageNo"
        :page-size="query.pageSize"
        :total="total"
        show-size-changer
        @change="onPageChange"
        @showSizeChange="onPageSizeChange"
      />
    </a-card>

    <a-modal v-model:open="changeBedOpen" title="换床" width="420px" @ok="submitChangeBed" @cancel="() => (changeBedOpen = false)">
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

    <a-modal v-model:open="bindOpen" title="绑定家属" width="420px" @ok="submitBindFamily" @cancel="() => (bindOpen = false)">
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
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import type { FormInstance, FormRules } from 'ant-design-vue'
import QRCode from 'qrcode'
import PageContainer from '../../components/PageContainer.vue'
import FlowGuardBar from '../../components/FlowGuardBar.vue'
import ElderNameAutocomplete from '../../components/ElderNameAutocomplete.vue'
import { useLiveSyncRefresh } from '../../composables/useLiveSyncRefresh'
import { exportCsv } from '../../utils/export'
import { lifecycleStageColor, lifecycleStageLabel, normalizeLifecycleStage } from '../../utils/lifecycleStage'
import { getElderPage, assignBed, bindFamily } from '../../api/elder'
import { getBedList } from '../../api/bed'
import { getFamilyUserPage } from '../../api/family'
import type { BedItem, ElderItem, FamilyBindRequest, PageResult, FamilyUserItem, Id } from '../../types/api'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const rows = ref<ElderItem[]>([])
const total = ref(0)
const selectedRowKeys = ref<Id[]>([])
const skipNextRouteWatch = ref(false)
const routeSignature = ref('')
const ELDER_LIST_ROUTE_KEYS = ['fullName', 'idCardNo', 'bedNo', 'careLevel', 'status', 'sortBy', 'sortOrder', 'pageNo', 'pageSize'] as const

const query = reactive({
  fullName: undefined as string | undefined,
  idCardNo: undefined as string | undefined,
  bedNo: undefined as string | undefined,
  careLevel: undefined as string | undefined,
  status: undefined as number | undefined,
  sortBy: undefined as string | undefined,
  sortOrder: undefined as string | undefined,
  pageNo: 1,
  pageSize: 10
})

const columns = [
  { title: '姓名', dataIndex: 'fullName', key: 'fullName', width: 120, sorter: true },
  { title: '身份证', dataIndex: 'idCardNo', key: 'idCardNo', width: 180 },
  { title: '生日', dataIndex: 'birthDate', key: 'birthDate', width: 130, sorter: true },
  { title: '家庭地址', dataIndex: 'homeAddress', key: 'homeAddress', width: 220 },
  { title: '床位号', dataIndex: 'bedNo', key: 'bedNo', width: 120, sorter: true },
  { title: '护理等级', dataIndex: 'careLevel', key: 'careLevel', width: 120, sorter: true },
  { title: '来源', dataIndex: 'sourceType', key: 'sourceType', width: 120 },
  { title: '履约阶段', dataIndex: 'lifecycleStage', key: 'lifecycleStage', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100, sorter: true },
  { title: '联动操作', key: 'action', width: 280 }
]
const selectedCount = computed(() => selectedRowKeys.value.length)
const selectedRows = computed(() => rows.value.filter((item) => selectedRowKeys.value.some((id) => String(id) === String(item.id))))
const lifecycleStageCounters = computed(() => {
  const counters = {
    pendingAssessment: 0,
    pendingBedSelect: 0,
    pendingSign: 0,
    signed: 0
  }
  rows.value.forEach((item) => {
    const stage = resolveLifecycleStage(item)
    if (stage === 'PENDING_ASSESSMENT') counters.pendingAssessment += 1
    if (stage === 'PENDING_BED_SELECT') counters.pendingBedSelect += 1
    if (stage === 'PENDING_SIGN') counters.pendingSign += 1
    if (stage === 'SIGNED') counters.signed += 1
  })
  return counters
})
const elderFlowSteps = ['档案建档', '办理入住', '在院管理']
const elderFlowCurrentIndex = computed(() => {
  if (!selectedRows.value.length) return 2
  const row = selectedRows.value[0]
  if (row.status === 1 || row.status === 2) return 2
  return 1
})
const elderFlowStageText = computed(() => {
  if (!selectedRows.value.length) return '展示历史入住补录与合同流程转入住长者'
  return statusText(selectedRows.value[0].status)
})
const elderFlowStageColor = computed(() => {
  if (!selectedRows.value.length) return 'blue'
  const status = selectedRows.value[0].status
  if (status === 1) return 'green'
  if (status === 2) return 'orange'
  return 'default'
})
const elderFlowSubject = computed(() => {
  if (!selectedRows.value.length) return `当前列表共 ${total.value} 位，统一管理历史入住补录与合同流程转入住长者`
  const row = selectedRows.value[0]
  return `长者 ${row.fullName} / 床位 ${row.bedNo || '-'}`
})
const elderFlowBlockers = computed(() => {
  if (!selectedRows.value.length) return []
  const row = selectedRows.value[0]
  if (!row.bedNo) return ['未分配床位']
  return []
})
const elderFlowHint = computed(() => {
  if (!selectedRows.value.length) return '新建老人用于补录平台启用前已入住长者；新入住老人仍需先走合同流程'
  return '可执行换床、退住、家属绑定与二维码打印等在院操作'
})
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
const qrDataUrl = ref('')
const qrText = ref('')

function statusText(status?: number) {
  if (status === 1) return '在院'
  if (status === 2) return '请假'
  if (status === 3) return '离院'
  return '未知'
}

function statusTag(status?: number) {
  if (status === 1) return 'green'
  if (status === 2) return 'orange'
  if (status === 3) return 'red'
  return 'default'
}

function sourceTypeText(value?: string) {
  if (value === 'HISTORICAL_IMPORT') return '历史补录'
  if (value === 'MARKETING_CONTRACT') return '合同流程'
  return '未标记'
}

function sourceTypeColor(value?: string) {
  if (value === 'HISTORICAL_IMPORT') return 'blue'
  if (value === 'MARKETING_CONTRACT') return 'purple'
  return 'default'
}

function resolveLifecycleStage(item: ElderItem) {
  const fallback = item.status === 1 || item.status === 2 || item.status === 3 ? 'SIGNED' : 'PENDING_ASSESSMENT'
  return normalizeLifecycleStage(item.lifecycleStage, item.lifecycleContractStatus || fallback)
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
    const res: PageResult<ElderItem> = await getElderPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      fullName: query.fullName,
      idCardNo: query.idCardNo,
      bedNo: query.bedNo,
      careLevel: query.careLevel,
      status: query.status,
      sortBy: query.sortBy,
      sortOrder: query.sortOrder
    })
    rows.value = res.list.map((r: any) => ({
      ...r,
      bedNo: r.bedNo ?? r.currentBed?.bedNo,
      roomNo: r.roomNo ?? r.currentBed?.roomNo,
      lifecycleStage: normalizeLifecycleStage(
        r.lifecycleStage,
        r.lifecycleContractStatus || (r.status === 1 || r.status === 2 || r.status === 3 ? 'SIGNED' : undefined)
      )
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
    beds.value = await getBedList()
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
  query.fullName = firstRouteQueryText(route.query.fullName) || undefined
  query.idCardNo = firstRouteQueryText(route.query.idCardNo) || undefined
  query.bedNo = firstRouteQueryText(route.query.bedNo) || undefined
  query.careLevel = firstRouteQueryText(route.query.careLevel) || undefined
  const status = Number(firstRouteQueryText(route.query.status))
  query.status = Number.isFinite(status) && status > 0 ? status : undefined
  query.sortBy = firstRouteQueryText(route.query.sortBy) || undefined
  const order = firstRouteQueryText(route.query.sortOrder).toLowerCase()
  query.sortOrder = order === 'asc' || order === 'desc' ? order : undefined
  query.pageNo = parsePositiveInt(route.query.pageNo, 1)
  query.pageSize = parsePositiveInt(route.query.pageSize, 10)
}

function buildQueryRouteQuery() {
  const nextQuery: Record<string, string> = {}
  Object.entries(flattenRouteQuery(route.query as Record<string, unknown>)).forEach(([key, value]) => {
    if ((ELDER_LIST_ROUTE_KEYS as readonly string[]).includes(key)) return
    nextQuery[key] = value
  })
  if (query.fullName) nextQuery.fullName = query.fullName
  if (query.idCardNo) nextQuery.idCardNo = query.idCardNo
  if (query.bedNo) nextQuery.bedNo = query.bedNo
  if (query.careLevel) nextQuery.careLevel = query.careLevel
  if (query.status) nextQuery.status = String(query.status)
  if (query.sortBy) nextQuery.sortBy = query.sortBy
  if (query.sortOrder) nextQuery.sortOrder = query.sortOrder
  nextQuery.pageNo = String(parsePositiveInt(query.pageNo, 1))
  nextQuery.pageSize = String(parsePositiveInt(query.pageSize, 10))
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
  query.pageSize = 10
  query.fullName = undefined
  query.idCardNo = undefined
  query.bedNo = undefined
  query.careLevel = undefined
  query.status = undefined
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

function exportCsvData() {
  exportCsv(
    rows.value.map((r) => ({
      姓名: r.fullName,
      身份证: r.idCardNo || '',
      生日: r.birthDate || '',
      家庭地址: r.homeAddress || '',
      床位号: r.bedNo || '',
      护理等级: r.careLevel || '',
      状态: statusText(r.status)
    })),
    'elder-list.csv'
  )
}

function goCreate() {
  router.push('/elder/create')
}

function goEdit(id: Id) {
  router.push(`/elder/edit/${id}`)
}

function goDetail(id: Id) {
  router.push(`/elder/detail/${id}`)
}

function goInHospitalOverview(row: ElderItem) {
  router.push({
    path: '/elder/in-hospital-overview',
    query: {
      residentId: String(row.id),
      elderName: row.fullName || ''
    }
  })
}

function goAssessmentArchive(row: ElderItem) {
  router.push({
    path: '/elder/assessment/ability/archive',
    query: {
      elderId: String(row.id),
      elderName: row.fullName || ''
    }
  })
}

function goContractsInvoices(row: ElderItem) {
  router.push({
    path: '/elder/contracts-invoices',
    query: {
      elderId: String(row.id),
      elderName: row.fullName || ''
    }
  })
}

function goEditSelected() {
  const row = requireSingleSelection('编辑')
  if (!row) return
  goEdit(row.id)
}

function goDetailSelected() {
  const row = requireSingleSelection('查看详情')
  if (!row) return
  goDetail(row.id)
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
  router.push({
    path: '/elder/discharge-apply',
    query: {
      elderId: String(row.id),
      openCreate: '1'
    }
  })
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
.search-form {
  row-gap: 12px;
}
.table-actions {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}
.selection-tip {
  color: var(--muted);
  font-size: 12px;
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
