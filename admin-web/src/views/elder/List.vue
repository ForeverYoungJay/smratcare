<template>
  <PageContainer title="长者档案" subTitle="高频查询、在院状态和服务动作集中处理">
    <template #extra>
      <a-space wrap>
        <a-button @click="exportCsvData">导出名单</a-button>
        <a-button type="primary" @click="goCreate">新建长者</a-button>
      </a-space>
    </template>
    <template #meta>
      <span class="soft-pill">当前列表 {{ total }} 位</span>
      <span class="soft-pill">已启用筛选 {{ activeFilterTags.length }} 项</span>
      <span v-for="tag in activeFilterTags.slice(0, 4)" :key="tag" class="selection-pill">{{ tag }}</span>
    </template>

    <SearchForm
      :model="query"
      title="快速查找长者"
      description="按姓名、床位、护理等级或入住状态筛选，输入后回车即可查询。"
      @search="runSearch"
      @reset="reset"
    >
      <a-form-item label="姓名">
        <ElderNameAutocomplete v-model:value="query.fullName" placeholder="姓名(编号)" width="220px" @select="runSearch" />
      </a-form-item>
      <a-form-item label="身份证">
        <a-input v-model:value="query.idCardNo" placeholder="身份证号" allow-clear />
      </a-form-item>
      <a-form-item label="床位号">
        <a-input v-model:value="query.bedNo" placeholder="床位号" allow-clear />
      </a-form-item>
      <a-form-item label="护理等级">
        <a-input v-model:value="query.careLevel" placeholder="护理等级" allow-clear />
      </a-form-item>
    </SearchForm>

    <section class="card-elevated elder-workspace">
      <div class="list-toolbar">
        <div class="status-chips" role="tablist" aria-label="按在院状态筛选">
          <button
            type="button"
            class="status-chip"
            :class="{ 'is-active': !query.lifecycleStatus }"
            @click="applyStatusChip(undefined)"
          >全部</button>
          <button
            v-for="opt in statusChipOptions"
            :key="opt.value"
            type="button"
            class="status-chip"
            :class="{ 'is-active': query.lifecycleStatus === opt.value }"
            @click="applyStatusChip(opt.value)"
          >{{ opt.label }}</button>
        </div>
        <a-tag color="processing">第 {{ query.pageNo }} / {{ totalPageCount }} 页 · 共 {{ total }} 条</a-tag>
      </div>

      <div v-if="selectedCount" class="selection-bar">
        <span class="selection-bar__count">已选 {{ selectedCount }} 位</span>
        <template v-if="selectedCount === 1">
          <a-button size="small" @click="goDetailSelected">详情</a-button>
          <a-button size="small" @click="goEditSelected">编辑</a-button>
          <a-button size="small" @click="openChangeBedSelected">换床</a-button>
          <a-button size="small" @click="openCheckoutSelected">退住申请</a-button>
          <a-button size="small" @click="openBindFamilySelected">绑定家属</a-button>
          <a-button size="small" @click="printElderQrSelected">打印二维码</a-button>
          <a-button size="small" danger @click="deleteSelected">删除</a-button>
        </template>
        <span v-else class="selection-bar__hint">档案操作需要单选一位长者</span>
        <a-button size="small" type="text" class="selection-bar__clear" @click="selectedRowKeys = []">取消选择</a-button>
      </div>

      <DataTable
        row-key="id"
        :columns="columns"
        :data-source="rows"
        :loading="loading"
        :pagination="false"
        :row-selection="rowSelection"
        :empty-title="emptyStateTitle"
        :empty-description="emptyStateDescription"
        :scroll="{ x: 1100, y: 560 }"
        @change="onTableChange"
      >
        <template #emptyExtra>
          <a-space wrap>
            <a-button type="primary" @click="goCreate">新建长者</a-button>
            <a-button v-if="activeFilterTags.length" @click="reset">清空筛选</a-button>
          </a-space>
        </template>
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'fullName'">
            <a-tooltip :title="record.homeAddress ? `家庭住址：${record.homeAddress}` : '暂无住址，可在详情补充'">
              <div class="elder-name-cell">
                <strong>{{ record.fullName }}</strong>
                <span>{{ record.roomNo || '-' }} / {{ record.bedNo || '未分配床位' }}</span>
              </div>
            </a-tooltip>
          </template>
          <template v-else-if="column.key === 'risk'">
            <div class="risk-tags">
              <a-tag v-if="record.riskPrecommit === 'RESCUE_FIRST'" color="volcano">抢救优先</a-tag>
              <a-tag v-else-if="record.riskPrecommit === 'NOTIFY_FAMILY_FIRST'" color="blue">先通知家属</a-tag>
              <a-tag v-if="record.diseases && record.diseases.length" color="orange">慢病 {{ record.diseases.length }}</a-tag>
              <span v-if="!record.riskPrecommit && !(record.diseases && record.diseases.length)" class="risk-tags__none">常规</span>
            </div>
          </template>
          <template v-else-if="column.key === 'idCardNo'">
            <span class="mono-text">{{ maskIdCard(record.idCardNo) }}</span>
          </template>
          <template v-else-if="column.key === 'birthDate'">
            <div class="elder-name-cell">
              <strong class="elder-age" v-if="ageOf(record.birthDate) !== null">{{ ageOf(record.birthDate) }}岁</strong>
              <span>{{ record.birthDate || '-' }}</span>
            </div>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="statusTag(record)">{{ statusText(record) }}</a-tag>
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
            <div class="row-action-links">
              <a-button type="link" size="small" @click="goResidentCenter(record)">长者中心</a-button>
              <a-dropdown trigger="click">
                <a-button type="link" size="small">更多 <DownOutlined /></a-button>
                <template #overlay>
                  <a-menu>
                    <a-menu-item key="detail" @click="goDetail(record.id)">查看详情</a-menu-item>
                    <a-menu-item key="assessment" @click="goAssessmentArchive(record)">评估档案</a-menu-item>
                    <a-menu-item key="contracts" @click="goContractsInvoices(record)">合同票据</a-menu-item>
                    <a-menu-item key="edit" @click="goEdit(record.id)">编辑档案</a-menu-item>
                    <a-menu-item key="bed" @click="openChangeBed(record)">换床</a-menu-item>
                    <a-menu-item key="checkout" @click="openCheckout(record)">退住申请</a-menu-item>
                    <a-menu-item key="family" @click="openBindFamily(record)">绑定家属</a-menu-item>
                    <a-menu-item key="qr" @click="printElderQr(record)">打印二维码</a-menu-item>
                    <a-menu-divider />
                    <a-menu-item key="delete" danger @click="confirmDelete(record)">删除档案</a-menu-item>
                  </a-menu>
                </template>
              </a-dropdown>
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
          @change="onPageChange"
          @showSizeChange="onPageSizeChange"
        />
      </div>
    </section>

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
import { message, Modal } from 'ant-design-vue'
import type { FormInstance, FormRules } from 'ant-design-vue'
import { DownOutlined } from '@ant-design/icons-vue'
import dayjs from 'dayjs'
import QRCode from 'qrcode'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import ElderNameAutocomplete from '../../components/ElderNameAutocomplete.vue'
import { useLiveSyncRefresh } from '../../composables/useLiveSyncRefresh'
import { exportCsv } from '../../utils/export'
import { lifecycleStageColor, lifecycleStageLabel, normalizeLifecycleStage } from '../../utils/lifecycleStage'
import { getElderPage, assignBed, bindFamily, deleteElder } from '../../api/elder'
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
const ELDER_LIST_ROUTE_KEYS = ['fullName', 'idCardNo', 'bedNo', 'careLevel', 'lifecycleStatus', 'status', 'sortBy', 'sortOrder', 'pageNo', 'pageSize'] as const

const query = reactive({
  fullName: undefined as string | undefined,
  idCardNo: undefined as string | undefined,
  bedNo: undefined as string | undefined,
  careLevel: undefined as string | undefined,
  lifecycleStatus: undefined as string | undefined,
  sortBy: undefined as string | undefined,
  sortOrder: undefined as string | undefined,
  pageNo: 1,
  pageSize: 10
})

const columns = [
  { title: '长者 / 床位', dataIndex: 'fullName', key: 'fullName', width: 180, sorter: true },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100, sorter: true },
  { title: '护理等级', dataIndex: 'careLevel', key: 'careLevel', width: 110, sorter: true },
  { title: '年龄 / 生日', dataIndex: 'birthDate', key: 'birthDate', width: 120, sorter: true },
  { title: '风险 / 关注', key: 'risk', width: 170 },
  { title: '身份证（脱敏）', dataIndex: 'idCardNo', key: 'idCardNo', width: 150 },
  { title: '履约阶段', dataIndex: 'lifecycleStage', key: 'lifecycleStage', width: 120 },
  { title: '操作', key: 'action', width: 150, fixed: 'right' }
]

const statusChipOptions = [
  { value: 'IN_HOSPITAL', label: '在住' },
  { value: 'TRIAL', label: '试住' },
  { value: 'INTENT', label: '意向' },
  { value: 'OUTING', label: '外出' },
  { value: 'MEDICAL_OUTING', label: '外出就医' },
  { value: 'DISCHARGE_PENDING', label: '待退住' },
  { value: 'DISCHARGED', label: '已退住' },
  { value: 'DECEASED', label: '已身故' }
]

const selectedCount = computed(() => selectedRowKeys.value.length)
const totalPageCount = computed(() => Math.max(1, Math.ceil(total.value / query.pageSize)))
const selectedRows = computed(() => rows.value.filter((item) => selectedRowKeys.value.some((id) => String(id) === String(item.id))))
const activeFilterTags = computed(() => {
  const tags: string[] = []
  if (query.fullName) tags.push(`姓名: ${query.fullName}`)
  if (query.idCardNo) tags.push(`身份证: ${query.idCardNo}`)
  if (query.bedNo) tags.push(`床位: ${query.bedNo}`)
  if (query.careLevel) tags.push(`护理等级: ${query.careLevel}`)
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

function resolveResidentLifecycleStatus(item?: Partial<ElderItem>) {
  const lifecycleStatus = String(item?.lifecycleStatus || '').trim().toUpperCase()
  if (lifecycleStatus) return lifecycleStatus
  if (item?.status === 1) return 'IN_HOSPITAL'
  if (item?.status === 2) return 'OUTING'
  if (item?.status === 3) return item?.departureType === 'DEATH' ? 'DECEASED' : 'DISCHARGED'
  return ''
}

function resolveResidentStatusView(item?: Partial<ElderItem>) {
  const lifecycleStage = normalizeLifecycleStage(item?.lifecycleStage, item?.lifecycleContractStatus)
  if (lifecycleStage === 'PENDING_BED_SELECT') return 'PENDING_BED_SELECT'
  if (lifecycleStage === 'PENDING_SIGN') return 'PENDING_SIGN'
  return resolveResidentLifecycleStatus(item)
}

function statusText(item?: Partial<ElderItem> | number) {
  const lifecycleStatus = typeof item === 'number'
    ? resolveResidentLifecycleStatus({ status: item })
    : resolveResidentStatusView(item)
  if (lifecycleStatus === 'PENDING_BED_SELECT') return '待办理入住'
  if (lifecycleStatus === 'PENDING_SIGN') return '待签署入住'
  if (lifecycleStatus === 'INTENT') return '意向'
  if (lifecycleStatus === 'TRIAL') return '试住'
  if (lifecycleStatus === 'IN_HOSPITAL') return '在住'
  if (lifecycleStatus === 'OUTING') return '外出'
  if (lifecycleStatus === 'MEDICAL_OUTING') return '外出就医'
  if (lifecycleStatus === 'DISCHARGE_PENDING') return '待退住'
  if (lifecycleStatus === 'DISCHARGED') return '已退住'
  if (lifecycleStatus === 'DECEASED') return '已身故'
  return '未知'
}

function statusTag(item?: Partial<ElderItem> | number) {
  const lifecycleStatus = typeof item === 'number'
    ? resolveResidentLifecycleStatus({ status: item })
    : resolveResidentStatusView(item)
  if (lifecycleStatus === 'PENDING_BED_SELECT') return 'blue'
  if (lifecycleStatus === 'PENDING_SIGN') return 'purple'
  if (lifecycleStatus === 'INTENT') return 'gold'
  if (lifecycleStatus === 'TRIAL') return 'cyan'
  if (lifecycleStatus === 'IN_HOSPITAL') return 'green'
  if (lifecycleStatus === 'OUTING') return 'orange'
  if (lifecycleStatus === 'MEDICAL_OUTING') return 'volcano'
  if (lifecycleStatus === 'DISCHARGE_PENDING') return 'blue'
  if (lifecycleStatus === 'DISCHARGED') return 'red'
  if (lifecycleStatus === 'DECEASED') return 'magenta'
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
  return normalizeLifecycleStage(item.lifecycleStage, item.lifecycleContractStatus)
}

function maskIdCard(value?: string): string {
  const id = String(value || '').trim()
  if (!id) return '—'
  if (id.length <= 6) return `${id.slice(0, 1)}****`
  return `${id.slice(0, 3)}${'*'.repeat(Math.max(4, id.length - 5))}${id.slice(-2)}`
}

function ageOf(birthDate?: string): number | null {
  if (!birthDate) return null
  const d = dayjs(birthDate)
  if (!d.isValid()) return null
  const age = dayjs().diff(d, 'year')
  return age >= 0 && age < 130 ? age : null
}

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
    const res: PageResult<ElderItem> = await getElderPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      fullName: query.fullName,
      idCardNo: query.idCardNo,
      bedNo: query.bedNo,
      careLevel: query.careLevel,
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
  if (query.lifecycleStatus) nextQuery.lifecycleStatus = query.lifecycleStatus
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

function exportCsvData() {
  exportCsv(
    rows.value.map((r) => ({
      姓名: r.fullName,
      身份证: r.idCardNo || '',
      生日: r.birthDate || '',
      家庭地址: r.homeAddress || '',
      床位号: r.bedNo || '',
      护理等级: r.careLevel || '',
      状态: statusText(r)
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

function goResidentCenter(row: ElderItem) {
  router.push({
    path: '/elder/resident-360',
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
.elder-workspace {
  padding: 18px;
}

.elder-name-cell,
.row-action-links,
.pager-row {
  display: flex;
  align-items: center;
}

/* 状态快捷筛选：一次点击切换最常用的在院状态 */
.list-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 14px;
}

.status-chips {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.status-chip {
  min-height: 32px;
  padding: 4px 14px;
  border: 1px solid var(--border);
  border-radius: 999px;
  background: #ffffff;
  color: var(--muted);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s ease;
}

.status-chip:hover {
  border-color: rgba(var(--primary-rgb), 0.4);
  color: var(--primary);
}

.status-chip.is-active {
  border-color: rgba(var(--primary-rgb), 0.4);
  background: var(--primary-soft);
  color: var(--primary-strong);
}

/* 勾选后才出现的操作条：不占常驻空间，动作跟着选择走 */
.selection-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 12px;
  padding: 10px 14px;
  border: 1px solid rgba(var(--primary-rgb), 0.24);
  border-radius: 12px;
  background: var(--primary-soft);
}

.selection-bar__count {
  color: var(--primary-strong);
  font-size: 13px;
  font-weight: 700;
  margin-right: 4px;
}

.selection-bar__hint {
  color: var(--muted);
  font-size: 12px;
}

.selection-bar__clear {
  margin-left: auto;
  color: var(--muted);
}

.elder-name-cell {
  flex-direction: column;
  align-items: flex-start;
  gap: 2px;
}

.elder-name-cell strong {
  color: var(--ink);
}

.elder-name-cell span {
  color: var(--muted);
  font-size: 12px;
}

.elder-age {
  font-size: 14px;
}

.risk-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.risk-tags__none {
  color: var(--muted);
  font-size: 12px;
}

.mono-text {
  font-variant-numeric: tabular-nums;
  letter-spacing: 0.02em;
  color: var(--muted);
}

.row-action-links {
  gap: 2px;
  flex-wrap: nowrap;
}

.pager-row {
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

@media (max-width: 768px) {
  .elder-workspace {
    padding: 12px;
  }
}
</style>
