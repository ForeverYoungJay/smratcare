<template>
  <PageContainer title="员工电费" subTitle="宿舍管理 / 员工月度电费">
    <div class="ops-shell">
      <section class="hero-panel">
        <div>
          <p class="eyebrow">Dorm Utility Desk</p>
          <h2>员工宿舍、电表、电费上传与财务推送统一处理</h2>
          <p class="hero-copy">支持按月批量上传员工电费金额，自动回写宿舍与电表信息，并一键同步财务或打印下载。</p>
        </div>
        <div class="hero-metrics">
          <div class="metric-card">
            <span>已导入人数</span>
            <strong>{{ summary.total }}</strong>
            <small>当前筛选电费记录</small>
          </div>
          <div class="metric-card">
            <span>月度电费</span>
            <strong>{{ formatMoney(summary.totalAmount) }}</strong>
            <small>上传后的总金额</small>
          </div>
          <div class="metric-card warning">
            <span>待同步财务</span>
            <strong>{{ summary.pendingSync }}</strong>
            <small>未推送到财务</small>
          </div>
        </div>
      </section>

      <SearchForm :model="query" @search="fetchData" @reset="onReset">
        <a-form-item label="费用月份">
          <a-input v-model:value="query.month" placeholder="YYYY-MM" allow-clear />
        </a-form-item>
        <a-form-item label="关键字">
          <a-input v-model:value="query.keyword" placeholder="工号/姓名/宿舍房间/电表号" allow-clear />
        </a-form-item>
        <a-form-item label="财务同步">
          <a-select v-model:value="query.financeSyncStatus" allow-clear style="width: 180px" :options="syncOptions" />
        </a-form-item>
        <template #extra>
          <a-space wrap>
            <a-button @click="downloadTemplate">下载模板</a-button>
            <a-upload :before-upload="beforeUpload" :show-upload-list="false" accept=".csv">
              <a-button type="primary">上传电费 CSV</a-button>
            </a-upload>
            <a-button :loading="syncing" @click="syncFinance">同步财务</a-button>
            <a-button @click="printCurrentView">一键打印</a-button>
            <a-button @click="exportCurrentCsv">导出 CSV</a-button>
            <a-button @click="exportCurrentExcel">导出 Excel</a-button>
          </a-space>
        </template>
      </SearchForm>

      <DataTable
        rowKey="id"
        :columns="columns"
        :data-source="rows"
        :loading="loading"
        :pagination="pagination"
        :row-selection="rowSelection"
        :row-class-name="rowClassName"
        :scroll="{ x: 1500 }"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'staff'">
            <div class="staff-cell">
              <strong>{{ record.staffName || '-' }}</strong>
              <span>{{ record.staffNo || '-' }}</span>
            </div>
          </template>
          <template v-else-if="column.key === 'dormitory'">
            <div class="plan-cell">
              <strong>{{ dormText(record) }}</strong>
              <span>电表：{{ record.meterNo || '-' }}</span>
            </div>
          </template>
          <template v-else-if="column.key === 'amount'">
            <strong>{{ formatMoney(record.amount) }}</strong>
            <div class="sub-line">{{ record.detailSummary || '电费上传金额' }}</div>
          </template>
          <template v-else-if="column.key === 'financeSyncStatus'">
            <a-tag :color="record.financeSyncStatus === 'SYNCED' ? 'green' : 'orange'">
              {{ financeSyncText(record.financeSyncStatus) }}
            </a-tag>
            <div class="sub-line">{{ record.financeSyncByName || '未同步' }}</div>
          </template>
          <template v-else-if="column.key === 'actions'">
            <a-space size="small">
              <a-button type="link" size="small" @click="openPlanDrawer(record)">宿舍信息</a-button>
              <a-button type="link" size="small" @click="syncFinance([record.id])">同步</a-button>
            </a-space>
          </template>
        </template>
      </DataTable>
    </div>

    <a-drawer v-model:open="importDrawerOpen" title="电费上传预览" width="760">
      <div class="import-summary">
        <div>
          <span>费用月份</span>
          <strong>{{ importMonth }}</strong>
        </div>
        <div>
          <span>待导入条数</span>
          <strong>{{ importRows.length }}</strong>
        </div>
        <div>
          <span>总金额</span>
          <strong>{{ formatMoney(importRows.reduce((sum, item) => sum + Number(item.amount || 0), 0)) }}</strong>
        </div>
      </div>

      <a-alert
        type="info"
        show-icon
        message="模板列顺序：staffNo,staffName,amount,dormitoryBuilding,dormitoryRoomNo,dormitoryBedNo,meterNo,remark"
        class="import-alert"
      />

      <a-table :columns="importColumns" :data-source="importRows" :pagination="false" row-key="staffNo" :scroll="{ x: 980, y: 420 }" />

      <template #footer>
        <a-space>
          <a-button @click="importDrawerOpen = false">取消</a-button>
          <a-button type="primary" :loading="importing" @click="submitImport">确认导入</a-button>
        </a-space>
      </template>
    </a-drawer>

    <a-drawer v-model:open="planDrawerOpen" title="宿舍联动信息" width="560">
      <a-form :model="planForm" layout="vertical">
        <a-form-item label="员工">
          <a-select
            v-model:value="planForm.staffId"
            show-search
            allow-clear
            :filter-option="false"
            :options="staffOptions"
            :loading="staffLoading"
            placeholder="选择员工"
            @search="searchStaff"
            @focus="() => !staffOptions.length && searchStaff('')"
          />
        </a-form-item>
        <div class="dorm-head">
          <span>是否住宿舍</span>
          <a-switch :checked="Number(planForm.liveInDormitory) === 1" @change="(checked) => (planForm.liveInDormitory = checked ? 1 : 0)" />
        </div>
        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="宿舍楼栋">
              <a-input v-model:value="planForm.dormitoryBuilding" placeholder="如 A栋" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="宿舍房间">
              <a-input v-model:value="planForm.dormitoryRoomNo" placeholder="如 3-201" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="床位号">
              <a-input v-model:value="planForm.dormitoryBedNo" placeholder="如 B02" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="电表号">
              <a-input v-model:value="planForm.meterNo" placeholder="导入时可自动带出" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="备注">
          <a-textarea v-model:value="planForm.remark" :rows="3" placeholder="如该员工和同宿舍拼表，按表号上传" />
        </a-form-item>
      </a-form>
      <template #footer>
        <a-space>
          <a-button @click="planDrawerOpen = false">取消</a-button>
          <a-button type="primary" :loading="savingPlan" @click="submitPlan">保存宿舍信息</a-button>
        </a-space>
      </template>
    </a-drawer>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { exportCsv, exportExcel } from '../../utils/export'
import { getHrElectricityFeePage, getHrStaffServicePlan, importHrElectricityFee, syncHrMonthlyFeeToFinance, upsertHrStaffServicePlan } from '../../api/hr'
import type { HrStaffElectricityImportRow, HrStaffMonthlyFeeBillItem, HrStaffServicePlan, PageResult } from '../../types'
import { useStaffOptions } from '../../composables/useStaffOptions'

const currentMonth = new Date().toISOString().slice(0, 7)

const query = reactive({
  month: currentMonth,
  keyword: undefined as string | undefined,
  financeSyncStatus: undefined as string | undefined,
  pageNo: 1,
  pageSize: 10
})

const syncOptions = [
  { label: '待同步', value: 'PENDING' },
  { label: '已同步', value: 'SYNCED' }
]

const columns = [
  { title: '员工', key: 'staff', width: 160 },
  { title: '部门', dataIndex: 'departmentName', key: 'departmentName', width: 140 },
  { title: '宿舍 / 电表', key: 'dormitory', width: 260 },
  { title: '费用月份', dataIndex: 'feeMonth', key: 'feeMonth', width: 100 },
  { title: '金额', key: 'amount', width: 160 },
  { title: '财务同步', key: 'financeSyncStatus', width: 150 },
  { title: '更新时间', dataIndex: 'updateTime', key: 'updateTime', width: 180 },
  { title: '备注', dataIndex: 'remark', key: 'remark', width: 180 },
  { title: '操作', key: 'actions', fixed: 'right', width: 150 }
]

const importColumns = [
  { title: '工号', dataIndex: 'staffNo', key: 'staffNo', width: 120 },
  { title: '员工姓名', dataIndex: 'staffName', key: 'staffName', width: 120 },
  { title: '金额', dataIndex: 'amount', key: 'amount', width: 120 },
  { title: '楼栋', dataIndex: 'dormitoryBuilding', key: 'dormitoryBuilding', width: 120 },
  { title: '房间', dataIndex: 'dormitoryRoomNo', key: 'dormitoryRoomNo', width: 140 },
  { title: '床位', dataIndex: 'dormitoryBedNo', key: 'dormitoryBedNo', width: 120 },
  { title: '电表号', dataIndex: 'meterNo', key: 'meterNo', width: 140 },
  { title: '备注', dataIndex: 'remark', key: 'remark', width: 160 }
]

const rows = ref<HrStaffMonthlyFeeBillItem[]>([])
const loading = ref(false)
const importing = ref(false)
const syncing = ref(false)
const savingPlan = ref(false)
const selectedRowKeys = ref<Array<string | number>>([])
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

const importDrawerOpen = ref(false)
const importRows = ref<HrStaffElectricityImportRow[]>([])
const importMonth = ref(currentMonth)

const summary = computed(() => ({
  total: rows.value.length,
  totalAmount: rows.value.reduce((sum, item) => sum + Number(item.amount || 0), 0),
  pendingSync: rows.value.filter((item) => item.financeSyncStatus !== 'SYNCED').length
}))

const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: Array<string | number>) => {
    selectedRowKeys.value = keys
  }
}))

const planDrawerOpen = ref(false)
const planForm = reactive<HrStaffServicePlan>({
  staffId: undefined,
  liveInDormitory: 1,
  status: 'ENABLED'
})

const { staffOptions, staffLoading, searchStaff, ensureSelectedStaff } = useStaffOptions({ pageSize: 120, preloadSize: 500 })

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<HrStaffMonthlyFeeBillItem> = await getHrElectricityFeePage({ ...query })
    rows.value = res.list || []
    pagination.total = res.total || 0
  } catch {
    rows.value = []
    pagination.total = 0
  } finally {
    loading.value = false
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
  query.month = currentMonth
  query.keyword = undefined
  query.financeSyncStatus = undefined
  query.pageNo = 1
  query.pageSize = 10
  pagination.current = 1
  pagination.pageSize = 10
  selectedRowKeys.value = []
  fetchData()
}

function rowClassName(record: HrStaffMonthlyFeeBillItem) {
  if (record.financeSyncStatus !== 'SYNCED') return 'hr-row-warning'
  return ''
}

function downloadTemplate() {
  exportCsv(
    [
      {
        staffNo: 'S0001',
        staffName: '张三',
        amount: '126.50',
        dormitoryBuilding: 'A栋',
        dormitoryRoomNo: '3-201',
        dormitoryBedNo: 'B02',
        meterNo: 'MTR-3201',
        remark: '3月宿舍电费'
      }
    ],
    '员工电费上传模板'
  )
}

function beforeUpload(file: File) {
  const reader = new FileReader()
  reader.onload = () => {
    const text = String(reader.result || '')
    const lines = text.split(/\r?\n/).map((line) => line.trim()).filter(Boolean)
    const rowsFromFile: HrStaffElectricityImportRow[] = []
    for (const line of lines.slice(1)) {
      const [staffNo, staffName, amount, dormitoryBuilding, dormitoryRoomNo, dormitoryBedNo, meterNo, remark] = line.split(',')
      rowsFromFile.push({
        staffNo: String(staffNo || '').trim() || undefined,
        staffName: String(staffName || '').trim() || undefined,
        amount: Number(amount || 0),
        dormitoryBuilding: String(dormitoryBuilding || '').trim() || undefined,
        dormitoryRoomNo: String(dormitoryRoomNo || '').trim() || undefined,
        dormitoryBedNo: String(dormitoryBedNo || '').trim() || undefined,
        meterNo: String(meterNo || '').trim() || undefined,
        remark: String(remark || '').trim() || undefined
      })
    }
    importRows.value = rowsFromFile.filter((item) => item.staffNo || item.staffName)
    importMonth.value = query.month || currentMonth
    if (!importRows.value.length) {
      message.warning('上传文件没有识别到有效电费记录')
      return
    }
    importDrawerOpen.value = true
  }
  reader.readAsText(file)
  return false
}

async function submitImport() {
  if (!importRows.value.length) {
    message.warning('没有可导入的电费记录')
    return
  }
  importing.value = true
  try {
    const result = await importHrElectricityFee({
      month: importMonth.value,
      rows: importRows.value
    })
    message.success(result?.message || '电费导入完成')
    importDrawerOpen.value = false
    importRows.value = []
    fetchData()
  } finally {
    importing.value = false
  }
}

async function syncFinance(ids?: Array<string | number | undefined>) {
  syncing.value = true
  try {
    const finalIds = (ids || selectedRowKeys.value).filter(Boolean) as Array<string | number>
    const result = await syncHrMonthlyFeeToFinance({
      feeType: 'ELECTRICITY',
      month: query.month,
      ids: finalIds.length ? finalIds : undefined
    })
    message.success(result?.message || '已同步财务')
    selectedRowKeys.value = []
    fetchData()
  } finally {
    syncing.value = false
  }
}

async function openPlanDrawer(record?: HrStaffMonthlyFeeBillItem) {
  resetPlanForm()
  if (record?.staffId) {
    ensureSelectedStaff(record.staffId, record.staffName)
    const detail = await getHrStaffServicePlan(record.staffId)
    Object.assign(planForm, {
      staffId: detail?.staffId,
      liveInDormitory: Number(detail?.liveInDormitory || 0),
      dormitoryBuilding: detail?.dormitoryBuilding,
      dormitoryRoomNo: detail?.dormitoryRoomNo,
      dormitoryBedNo: detail?.dormitoryBedNo,
      meterNo: detail?.meterNo,
      status: detail?.status || 'ENABLED',
      remark: detail?.remark
    })
  }
  planDrawerOpen.value = true
}

function resetPlanForm() {
  Object.assign(planForm, {
    staffId: undefined,
    liveInDormitory: 1,
    dormitoryBuilding: undefined,
    dormitoryRoomNo: undefined,
    dormitoryBedNo: undefined,
    meterNo: undefined,
    status: 'ENABLED',
    remark: undefined
  })
}

async function submitPlan() {
  if (!planForm.staffId) {
    message.warning('请选择员工')
    return
  }
  savingPlan.value = true
  try {
    await upsertHrStaffServicePlan({ ...planForm })
    message.success('宿舍信息已保存')
    planDrawerOpen.value = false
    fetchData()
  } finally {
    savingPlan.value = false
  }
}

function financeSyncText(value?: string) {
  return value === 'SYNCED' ? '已同步' : '待同步'
}

function formatMoney(value?: number | string | null) {
  return `¥${Number(value || 0).toFixed(2)}`
}

function dormText(record: HrStaffMonthlyFeeBillItem) {
  return [record.dormitoryBuilding, record.dormitoryRoomNo, record.dormitoryBedNo].filter(Boolean).join(' / ') || '未维护宿舍'
}

function exportRows() {
  return rows.value.map((item) => ({
    费用月份: item.feeMonth || '',
    工号: item.staffNo || '',
    员工姓名: item.staffName || '',
    部门: item.departmentName || '',
    宿舍楼栋: item.dormitoryBuilding || '',
    宿舍房间: item.dormitoryRoomNo || '',
    床位号: item.dormitoryBedNo || '',
    电表号: item.meterNo || '',
    金额: Number(item.amount || 0).toFixed(2),
    财务同步: financeSyncText(item.financeSyncStatus),
    同步人: item.financeSyncByName || '',
    更新时间: item.updateTime || '',
    备注: item.remark || ''
  }))
}

function exportCurrentCsv() {
  exportCsv(exportRows(), `员工电费_${query.month || currentMonth}`)
}

function exportCurrentExcel() {
  exportExcel(exportRows(), `员工电费_${query.month || currentMonth}`)
}

function printCurrentView() {
  if (!rows.value.length) {
    message.warning('当前没有可打印数据')
    return
  }
  const html = `
    <html>
      <head>
        <meta charset="UTF-8" />
        <title>员工电费台账</title>
        <style>
          body { font-family: Arial, sans-serif; padding: 24px; color: #1f2937; }
          h1 { margin-bottom: 8px; }
          p { margin: 0 0 16px; color: #4b5563; }
          table { width: 100%; border-collapse: collapse; }
          th, td { border: 1px solid #d1d5db; padding: 8px; font-size: 12px; text-align: left; vertical-align: top; }
          th { background: #f3f4f6; }
        </style>
      </head>
      <body>
        <h1>员工电费月度台账</h1>
        <p>费用月份：${query.month || currentMonth}</p>
        <table>
          <thead>
            <tr>
              <th>工号</th>
              <th>员工</th>
              <th>部门</th>
              <th>宿舍</th>
              <th>电表号</th>
              <th>金额</th>
              <th>财务同步</th>
            </tr>
          </thead>
          <tbody>
            ${rows.value
              .map(
                (item) => `
                  <tr>
                    <td>${item.staffNo || '-'}</td>
                    <td>${item.staffName || '-'}</td>
                    <td>${item.departmentName || '-'}</td>
                    <td>${dormText(item)}</td>
                    <td>${item.meterNo || '-'}</td>
                    <td>${formatMoney(item.amount)}</td>
                    <td>${financeSyncText(item.financeSyncStatus)}</td>
                  </tr>
                `
              )
              .join('')}
          </tbody>
        </table>
      </body>
    </html>
  `
  const printWindow = window.open('', '_blank', 'width=1080,height=760')
  if (!printWindow) return
  printWindow.document.write(html)
  printWindow.document.close()
  printWindow.focus()
  printWindow.print()
}

onMounted(() => {
  searchStaff('')
  fetchData()
})
</script>

<style scoped>
.ops-shell {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.hero-panel {
  display: grid;
  grid-template-columns: minmax(0, 1.6fr) minmax(320px, 1fr);
  gap: 16px;
  padding: 20px 24px;
  border-radius: 24px;
  background:
    radial-gradient(circle at top left, rgba(125, 211, 252, 0.22), transparent 34%),
    linear-gradient(135deg, #082f49 0%, #0f766e 50%, #164e63 100%);
  color: #ecfeff;
}

.eyebrow {
  margin: 0 0 8px;
  font-size: 12px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: rgba(236, 254, 255, 0.72);
}

.hero-panel h2 {
  margin: 0;
  font-size: 28px;
  line-height: 1.2;
}

.hero-copy {
  margin: 12px 0 0;
  max-width: 760px;
  color: rgba(207, 250, 254, 0.88);
}

.hero-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.metric-card {
  padding: 16px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(6px);
}

.metric-card.warning {
  background: rgba(249, 115, 22, 0.18);
}

.metric-card span,
.metric-card small {
  display: block;
}

.metric-card span {
  color: rgba(204, 251, 241, 0.8);
}

.metric-card strong {
  display: block;
  margin: 10px 0 6px;
  font-size: 26px;
}

.staff-cell,
.plan-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.staff-cell span,
.plan-cell span,
.sub-line {
  color: #64748b;
  font-size: 12px;
}

.import-summary {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.import-summary > div {
  padding: 14px;
  border-radius: 16px;
  background: #f0fdfa;
  border: 1px solid #99f6e4;
}

.import-summary span,
.import-summary strong {
  display: block;
}

.import-summary strong {
  margin-top: 8px;
  font-size: 22px;
}

.import-alert {
  margin-bottom: 12px;
}

.dorm-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  font-weight: 600;
}

:deep(.hr-row-warning) {
  background: #ecfeff !important;
}

@media (max-width: 1100px) {
  .hero-panel,
  .hero-metrics,
  .import-summary {
    grid-template-columns: 1fr;
  }
}
</style>
