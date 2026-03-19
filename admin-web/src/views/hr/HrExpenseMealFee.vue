<template>
  <PageContainer title="员工餐费" subTitle="后勤餐饮管理 / 员工月度餐费">
    <div class="ops-shell">
      <section class="hero-panel">
        <div>
          <p class="eyebrow">Meal Billing Desk</p>
          <h2>员工用餐方案、月度扣缴、财务同步放在一页处理</h2>
          <p class="hero-copy">按月份生成员工餐费账单，自动汇总早餐/中餐/晚餐方案，支持一键同步财务与打印台账。</p>
        </div>
        <div class="hero-metrics">
          <div class="metric-card">
            <span>本月账单数</span>
            <strong>{{ summary.total }}</strong>
            <small>已加载餐费记录</small>
          </div>
          <div class="metric-card">
            <span>本月金额</span>
            <strong>{{ formatMoney(summary.totalAmount) }}</strong>
            <small>月度自动扣缴总额</small>
          </div>
          <div class="metric-card warning">
            <span>待同步财务</span>
            <strong>{{ summary.pendingSync }}</strong>
            <small>仍需推送财务审批</small>
          </div>
        </div>
      </section>

      <SearchForm :model="query" @search="fetchData" @reset="onReset">
        <a-form-item label="费用月份">
          <a-input v-model:value="query.month" placeholder="YYYY-MM" allow-clear />
        </a-form-item>
        <a-form-item label="关键字">
          <a-input v-model:value="query.keyword" placeholder="工号/姓名/部门/宿舍" allow-clear />
        </a-form-item>
        <a-form-item label="财务同步">
          <a-select v-model:value="query.financeSyncStatus" allow-clear style="width: 180px" :options="syncOptions" />
        </a-form-item>
        <template #extra>
          <a-space wrap>
            <a-button @click="openPlanDrawer()">维护用餐方案</a-button>
            <a-button type="primary" :loading="generating" @click="generateMonthBill">生成本月餐费</a-button>
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
        :scroll="{ x: 1480 }"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'staff'">
            <div class="staff-cell">
              <strong>{{ record.staffName || '-' }}</strong>
              <span>{{ record.staffNo || '-' }}</span>
            </div>
          </template>
          <template v-else-if="column.key === 'plan'">
            <div class="plan-cell">
              <strong>{{ record.mealPlanSummary || '未维护' }}</strong>
              <span>{{ record.detailSummary || '-' }}</span>
            </div>
          </template>
          <template v-else-if="column.key === 'amount'">
            <strong>{{ formatMoney(record.amount) }}</strong>
          </template>
          <template v-else-if="column.key === 'financeSyncStatus'">
            <a-tag :color="record.financeSyncStatus === 'SYNCED' ? 'green' : 'orange'">
              {{ financeSyncText(record.financeSyncStatus) }}
            </a-tag>
            <div class="sub-line">{{ record.financeSyncByName || '未同步' }}</div>
          </template>
          <template v-else-if="column.key === 'actions'">
            <a-space size="small">
              <a-button type="link" size="small" @click="openPlanDrawer(record)">调整方案</a-button>
              <a-button type="link" size="small" @click="syncFinance([record.id])">同步</a-button>
            </a-space>
          </template>
        </template>
      </DataTable>
    </div>

    <a-drawer v-model:open="planDrawerOpen" title="员工用餐与宿舍方案" width="620">
      <a-form :model="planForm" layout="vertical">
        <a-form-item label="员工" required>
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

        <div class="plan-grid">
          <div class="meal-block">
            <div class="meal-block-head">
              <span>早餐</span>
              <a-switch :checked="Number(planForm.breakfastEnabled) === 1" @change="(checked) => (planForm.breakfastEnabled = checked ? 1 : 0)" />
            </div>
            <a-input-number v-model:value="planForm.breakfastUnitPrice" :min="0" :precision="2" style="width: 100%" placeholder="单价" />
            <a-input-number v-model:value="planForm.breakfastDaysPerMonth" :min="0" :precision="0" style="width: 100%" placeholder="每月天数" />
          </div>
          <div class="meal-block">
            <div class="meal-block-head">
              <span>中餐</span>
              <a-switch :checked="Number(planForm.lunchEnabled) === 1" @change="(checked) => (planForm.lunchEnabled = checked ? 1 : 0)" />
            </div>
            <a-input-number v-model:value="planForm.lunchUnitPrice" :min="0" :precision="2" style="width: 100%" placeholder="单价" />
            <a-input-number v-model:value="planForm.lunchDaysPerMonth" :min="0" :precision="0" style="width: 100%" placeholder="每月天数" />
          </div>
          <div class="meal-block">
            <div class="meal-block-head">
              <span>晚餐</span>
              <a-switch :checked="Number(planForm.dinnerEnabled) === 1" @change="(checked) => (planForm.dinnerEnabled = checked ? 1 : 0)" />
            </div>
            <a-input-number v-model:value="planForm.dinnerUnitPrice" :min="0" :precision="2" style="width: 100%" placeholder="单价" />
            <a-input-number v-model:value="planForm.dinnerDaysPerMonth" :min="0" :precision="0" style="width: 100%" placeholder="每月天数" />
          </div>
        </div>

        <a-divider />

        <div class="dorm-head">
          <span>宿舍联动</span>
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
            <a-form-item label="电表编号">
              <a-input v-model:value="planForm.meterNo" placeholder="用于电费上传联动" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="备注">
          <a-textarea v-model:value="planForm.remark" :rows="3" placeholder="例如仅工作日用中餐，晚餐按排班补扣" />
        </a-form-item>
      </a-form>
      <template #footer>
        <a-space>
          <a-button @click="planDrawerOpen = false">取消</a-button>
          <a-button type="primary" :loading="savingPlan" @click="submitPlan">保存方案</a-button>
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
import { generateHrMealFee, getHrMealFeePage, getHrStaffServicePlan, syncHrMonthlyFeeToFinance, upsertHrStaffServicePlan } from '../../api/hr'
import type { HrStaffMonthlyFeeBillItem, HrStaffServicePlan, PageResult } from '../../types'
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
  { title: '用餐方案', key: 'plan', width: 360 },
  { title: '宿舍房间', dataIndex: 'dormitoryRoomNo', key: 'dormitoryRoomNo', width: 130 },
  { title: '月份', dataIndex: 'feeMonth', key: 'feeMonth', width: 100 },
  { title: '金额', key: 'amount', width: 120 },
  { title: '财务同步', key: 'financeSyncStatus', width: 150 },
  { title: '更新时间', dataIndex: 'updateTime', key: 'updateTime', width: 180 },
  { title: '操作', key: 'actions', fixed: 'right', width: 150 }
]

const rows = ref<HrStaffMonthlyFeeBillItem[]>([])
const loading = ref(false)
const generating = ref(false)
const syncing = ref(false)
const savingPlan = ref(false)
const selectedRowKeys = ref<Array<string | number>>([])
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

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
  breakfastEnabled: 0,
  lunchEnabled: 1,
  dinnerEnabled: 0,
  liveInDormitory: 0,
  status: 'ENABLED'
})

const { staffOptions, staffLoading, searchStaff, ensureSelectedStaff } = useStaffOptions({ pageSize: 120, preloadSize: 500 })

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<HrStaffMonthlyFeeBillItem> = await getHrMealFeePage({ ...query })
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

async function generateMonthBill() {
  generating.value = true
  try {
    const result = await generateHrMealFee({ month: query.month })
    message.success(result?.message || '餐费账单已生成')
    fetchData()
  } finally {
    generating.value = false
  }
}

async function syncFinance(ids?: Array<string | number | undefined>) {
  syncing.value = true
  try {
    const finalIds = (ids || selectedRowKeys.value).filter(Boolean) as Array<string | number>
    const result = await syncHrMonthlyFeeToFinance({
      feeType: 'MEAL',
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
    await hydratePlan(record.staffId, record.staffName, record.staffNo)
  }
  planDrawerOpen.value = true
}

async function hydratePlan(staffId: string | number, staffName?: string, staffNo?: string) {
  ensureSelectedStaff(staffId, staffName)
  const detail = await getHrStaffServicePlan(staffId)
  Object.assign(planForm, {
    staffId: detail?.staffId,
    breakfastEnabled: Number(detail?.breakfastEnabled || 0),
    breakfastUnitPrice: detail?.breakfastUnitPrice,
    breakfastDaysPerMonth: detail?.breakfastDaysPerMonth,
    lunchEnabled: Number(detail?.lunchEnabled || 0),
    lunchUnitPrice: detail?.lunchUnitPrice,
    lunchDaysPerMonth: detail?.lunchDaysPerMonth,
    dinnerEnabled: Number(detail?.dinnerEnabled || 0),
    dinnerUnitPrice: detail?.dinnerUnitPrice,
    dinnerDaysPerMonth: detail?.dinnerDaysPerMonth,
    liveInDormitory: Number(detail?.liveInDormitory || 0),
    dormitoryBuilding: detail?.dormitoryBuilding,
    dormitoryRoomNo: detail?.dormitoryRoomNo,
    dormitoryBedNo: detail?.dormitoryBedNo,
    meterNo: detail?.meterNo,
    status: detail?.status || 'ENABLED',
    remark: detail?.remark
  })
  if (staffNo) ensureSelectedStaff(staffId, `${staffName || ''}${staffNo ? ` (${staffNo})` : ''}`)
}

function resetPlanForm() {
  Object.assign(planForm, {
    staffId: undefined,
    breakfastEnabled: 0,
    breakfastUnitPrice: undefined,
    breakfastDaysPerMonth: undefined,
    lunchEnabled: 1,
    lunchUnitPrice: undefined,
    lunchDaysPerMonth: undefined,
    dinnerEnabled: 0,
    dinnerUnitPrice: undefined,
    dinnerDaysPerMonth: undefined,
    liveInDormitory: 0,
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
    message.success('用餐方案已保存')
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

function exportRows() {
  return rows.value.map((item) => ({
    费用月份: item.feeMonth || '',
    工号: item.staffNo || '',
    员工姓名: item.staffName || '',
    部门: item.departmentName || '',
    用餐方案: item.mealPlanSummary || '',
    费用明细: item.detailSummary || '',
    宿舍房间: item.dormitoryRoomNo || '',
    金额: Number(item.amount || 0).toFixed(2),
    财务同步: financeSyncText(item.financeSyncStatus),
    同步人: item.financeSyncByName || '',
    更新时间: item.updateTime || '',
    备注: item.remark || ''
  }))
}

function exportCurrentCsv() {
  exportCsv(exportRows(), `员工餐费_${query.month || currentMonth}`)
}

function exportCurrentExcel() {
  exportExcel(exportRows(), `员工餐费_${query.month || currentMonth}`)
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
        <title>员工餐费台账</title>
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
        <h1>员工餐费月度台账</h1>
        <p>费用月份：${query.month || currentMonth}</p>
        <table>
          <thead>
            <tr>
              <th>工号</th>
              <th>员工</th>
              <th>部门</th>
              <th>用餐方案</th>
              <th>费用明细</th>
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
                    <td>${item.mealPlanSummary || '-'}</td>
                    <td>${item.detailSummary || '-'}</td>
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
    radial-gradient(circle at top left, rgba(253, 224, 71, 0.22), transparent 32%),
    linear-gradient(135deg, #0f172a 0%, #1e293b 58%, #334155 100%);
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
  line-height: 1.2;
}

.hero-copy {
  margin: 12px 0 0;
  max-width: 760px;
  color: rgba(226, 232, 240, 0.86);
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
  background: rgba(251, 191, 36, 0.16);
}

.metric-card span,
.metric-card small {
  display: block;
}

.metric-card span {
  color: rgba(226, 232, 240, 0.78);
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

.plan-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.meal-block {
  padding: 14px;
  border-radius: 18px;
  background: linear-gradient(180deg, #fff7ed 0%, #ffffff 100%);
  border: 1px solid #fed7aa;
}

.meal-block-head,
.dorm-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  font-weight: 600;
}

.meal-block :deep(.ant-input-number) + :deep(.ant-input-number) {
  margin-top: 10px;
}

:deep(.hr-row-warning) {
  background: #fff7ed !important;
}

@media (max-width: 1100px) {
  .hero-panel {
    grid-template-columns: 1fr;
  }

  .hero-metrics,
  .plan-grid {
    grid-template-columns: 1fr;
  }
}
</style>
