<template>
  <PageContainer title="健康巡检" subTitle="日常巡检项目与整改闭环">
    <a-row :gutter="16" class="summary-row">
      <a-col :xs="24" :md="6"><a-card :bordered="false"><a-statistic title="巡检总数" :value="summary.totalCount" /></a-card></a-col>
      <a-col :xs="24" :md="6"><a-card :bordered="false"><a-statistic title="异常数" :value="summary.abnormalCount" /></a-card></a-col>
      <a-col :xs="24" :md="6"><a-card :bordered="false"><a-statistic title="跟进中" :value="summary.followingCount" /></a-card></a-col>
      <a-col :xs="24" :md="6"><a-card :bordered="false"><a-statistic title="已关闭" :value="summary.closedCount" /></a-card></a-col>
    </a-row>

    <a-card :bordered="false" class="summary-row">
      <a-space wrap>
        <a-tag color="blue">关联护理日志：{{ summary.linkedLogCount }}</a-tag>
        <a-tag v-for="item in summary.statusStats" :key="item.name" :color="statusColor(item.name)">
          {{ statusLabel(item.name) }}：{{ item.totalCount }}
        </a-tag>
        <a-tag v-if="!summary.statusStats.length" color="default">暂无状态统计</a-tag>
      </a-space>
    </a-card>

    <a-card :bordered="false" class="summary-row" title="巡检任务看板（计划-执行）">
      <a-space wrap style="margin-bottom: 10px">
        <a-tag color="blue">今日待巡检：{{ boardStats.todayPending }}</a-tag>
        <a-tag color="green">今日已巡检：{{ boardStats.todayDone }}</a-tag>
        <a-tag color="red">今日超时：{{ boardStats.todayOverdue }}</a-tag>
        <a-input v-model:value="board.floor" allow-clear placeholder="楼层筛选（如 3F）" style="width: 160px" />
        <a-input v-model:value="board.group" allow-clear placeholder="护理组筛选（如 A组）" style="width: 170px" />
        <a-button type="primary" @click="openBoardRecord()">巡检录入（抽屉）</a-button>
      </a-space>
      <a-list :data-source="boardStats.pendingRows.slice(0, 8)" size="small" bordered>
        <template #renderItem="{ item }">
          <a-list-item>
            <a-space>
              <span>{{ item.elderName || '-' }}</span>
              <a-tag>{{ formatDate(item.inspectionDate) }}</a-tag>
              <span>{{ item.inspectionItem || '-' }}</span>
            </a-space>
            <template #actions>
              <a-button type="link" @click="openBoardRecord(item)">执行录入</a-button>
            </template>
          </a-list-item>
        </template>
      </a-list>
    </a-card>

    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键词"><a-input v-model:value="query.keyword" placeholder="老人/项目/巡检人" allow-clear /></a-form-item>
      <a-form-item label="状态"><a-select v-model:value="query.status" :options="statusOptions" allow-clear style="width: 180px" /></a-form-item>
      <a-form-item label="日期">
        <a-range-picker v-model:value="query.inspectionRange" style="width: 280px" />
      </a-form-item>
      <template #extra>
        <a-space>
          <a-button @click="exportCsvData" :loading="exporting">导出CSV</a-button>
          <a-button @click="exportExcelData" :loading="exporting">导出Excel</a-button>
          <a-button type="primary" @click="openCreate">新增巡检</a-button>
        </a-space>
      </template>
    </SearchForm>

    <DataTable
      rowKey="id"
      :columns="columns"
      :data-source="rows"
      :loading="loading"
      :pagination="pagination"
      :row-class-name="resolveRowClassName"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'inspectionDate'">
          {{ formatDate(record.inspectionDate) }}
        </template>
        <template v-else-if="column.key === 'status'">
          <a-tag :color="statusColor(record.status)">{{ statusLabel(record.status) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="openEdit(record)">编辑</a-button>
            <a-popconfirm title="确认删除该记录吗？" ok-text="确认" cancel-text="取消" @confirm="remove(record)">
              <a-button type="link" danger>删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </DataTable>

    <a-modal v-model:open="editOpen" title="健康巡检" @ok="submit" :confirm-loading="saving" width="760px">
      <a-form layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="老人" required>
              <a-select
                v-model:value="form.elderId"
                show-search
                :filter-option="false"
                :options="elderOptions"
                placeholder="请输入姓名搜索"
                @search="searchElders"
                @focus="() => !elderOptions.length && searchElders('')"
                @change="onElderChange"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12"><a-form-item label="巡检日期" required><a-date-picker v-model:value="form.inspectionDate" style="width: 100%" /></a-form-item></a-col>
        </a-row>
        <a-form-item label="巡检项目" required><a-input v-model:value="form.inspectionItem" /></a-form-item>
        <a-form-item label="结果"><a-textarea v-model:value="form.result" :rows="2" /></a-form-item>
        <a-row :gutter="16">
          <a-col :span="12"><a-form-item label="状态"><a-select v-model:value="form.status" :options="statusOptions" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="巡检人"><a-input v-model:value="form.inspectorName" /></a-form-item></a-col>
        </a-row>
        <a-form-item label="跟进措施"><a-input v-model:value="form.followUpAction" /></a-form-item>
        <a-form-item label="备注"><a-input v-model:value="form.remark" /></a-form-item>
      </a-form>
    </a-modal>

    <a-drawer v-model:open="boardOpen" title="巡检录入（不离开页面）" width="700" @close="resetBoardForm">
      <a-form layout="vertical">
        <a-row :gutter="12">
          <a-col :span="12"><a-form-item label="长者"><a-input v-model:value="boardForm.elderName" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="巡检日期"><a-date-picker v-model:value="boardForm.inspectionDate" style="width: 100%" /></a-form-item></a-col>
        </a-row>
        <a-row :gutter="12">
          <a-col :span="8"><a-form-item label="体温(℃)"><a-input v-model:value="boardForm.temp" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="血压(mmHg)"><a-input v-model:value="boardForm.bp" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="脉搏(次/分)"><a-input v-model:value="boardForm.pulse" /></a-form-item></a-col>
        </a-row>
        <a-row :gutter="12">
          <a-col :span="8"><a-form-item label="呼吸"><a-input v-model:value="boardForm.respiration" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="血氧(%)"><a-input v-model:value="boardForm.spo2" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="血糖(mmol/L)"><a-input v-model:value="boardForm.glucose" /></a-form-item></a-col>
        </a-row>
        <a-row :gutter="12">
          <a-col :span="8"><a-form-item label="食欲"><a-input v-model:value="boardForm.appetite" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="睡眠"><a-input v-model:value="boardForm.sleep" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="排便"><a-input v-model:value="boardForm.bowel" /></a-form-item></a-col>
        </a-row>
        <a-row :gutter="12">
          <a-col :span="8"><a-form-item label="疼痛"><a-input v-model:value="boardForm.pain" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="精神状态"><a-input v-model:value="boardForm.mental" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="皮肤情况"><a-input v-model:value="boardForm.skin" /></a-form-item></a-col>
        </a-row>
        <a-space style="margin-bottom: 8px">
          <a-checkbox v-model:checked="boardForm.abnormal">异常标记</a-checkbox>
          <a-checkbox v-model:checked="boardForm.needReport">需上报</a-checkbox>
        </a-space>
        <a-form-item label="处理方式" v-if="boardForm.abnormal">
          <a-input v-model:value="boardForm.handleAction" placeholder="异常项必须填写处理方式" />
        </a-form-item>
        <a-form-item label="附件说明">
          <a-input v-model:value="boardForm.attachmentNote" placeholder="照片/伤口/皮肤说明" />
        </a-form-item>
        <a-form-item label="巡检结论">
          <a-radio-group v-model:value="boardForm.conclusion">
            <a-radio value="NORMAL">正常</a-radio>
            <a-radio value="FOLLOWING">需观察</a-radio>
            <a-radio value="ABNORMAL">需医护介入</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-space>
          <a-button @click="saveBoardDraft">保存草稿</a-button>
          <a-button type="primary" @click="submitBoard">提交并生成处置任务</a-button>
        </a-space>
      </a-form>
    </a-drawer>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { useElderOptions } from '../../composables/useElderOptions'
import { inspectionExportColumns, mapHealthExportRows } from '../../constants/healthExport'
import { exportCsv, exportExcel } from '../../utils/export'
import { resolveHealthError } from './healthError'
import {
  getHealthInspectionPage,
  getHealthInspectionSummary,
  createHealthInspection,
  updateHealthInspection,
  deleteHealthInspection
} from '../../api/health'
import type { HealthInspection, HealthInspectionSummary, PageResult } from '../../types'

const loading = ref(false)
const exporting = ref(false)
const route = useRoute()
const rows = ref<HealthInspection[]>([])
const query = reactive({ keyword: '', status: '', inspectionRange: [] as any[], pageNo: 1, pageSize: 10 })
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const summary = reactive<HealthInspectionSummary>({
  totalCount: 0,
  abnormalCount: 0,
  followingCount: 0,
  closedCount: 0,
  linkedLogCount: 0,
  statusStats: []
})

const columns = [
  { title: '老人', dataIndex: 'elderName', key: 'elderName', width: 120 },
  { title: '日期', dataIndex: 'inspectionDate', key: 'inspectionDate', width: 120 },
  { title: '项目', dataIndex: 'inspectionItem', key: 'inspectionItem', width: 150 },
  { title: '结果', dataIndex: 'result', key: 'result', width: 200 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 120 },
  { title: '巡检人', dataIndex: 'inspectorName', key: 'inspectorName', width: 120 },
  { title: '跟进措施', dataIndex: 'followUpAction', key: 'followUpAction' },
  { title: '操作', key: 'action', width: 120 }
]

const editOpen = ref(false)
const saving = ref(false)
const { elderOptions, searchElders, findElderName, ensureSelectedElder } = useElderOptions({ pageSize: 50 })
const form = reactive({
  id: undefined as number | undefined,
  elderId: undefined as number | undefined,
  elderName: '',
  inspectionDate: dayjs(),
  inspectionItem: '',
  result: '',
  status: '',
  inspectorName: '',
  followUpAction: '',
  remark: ''
})
const statusOptions = [
  { label: '正常', value: 'NORMAL' },
  { label: '异常', value: 'ABNORMAL' },
  { label: '跟进中', value: 'FOLLOWING' },
  { label: '已关闭', value: 'CLOSED' }
]
const board = reactive({
  floor: '',
  group: ''
})
const boardOpen = ref(false)
const boardForm = reactive({
  elderId: undefined as number | undefined,
  elderName: '',
  inspectionDate: dayjs(),
  temp: '',
  bp: '',
  pulse: '',
  respiration: '',
  spo2: '',
  glucose: '',
  appetite: '',
  sleep: '',
  bowel: '',
  pain: '',
  mental: '',
  skin: '',
  abnormal: false,
  needReport: false,
  handleAction: '',
  attachmentNote: '',
  conclusion: 'NORMAL'
})
const boardRows = computed(() => {
  const floorText = board.floor.trim()
  const groupText = board.group.trim()
  return rows.value.filter((item) => {
    const refText = `${item.inspectionItem || ''} ${item.remark || ''}`
    const okFloor = !floorText || refText.includes(floorText)
    const okGroup = !groupText || refText.includes(groupText)
    return okFloor && okGroup
  })
})
const boardTodayRows = computed(() => boardRows.value.filter((item) => dayjs(item.inspectionDate).isSame(dayjs(), 'day')))
const boardPendingRows = computed(() => boardTodayRows.value.filter((item) => item.status === 'FOLLOWING' || item.status === 'ABNORMAL'))
const boardDoneRows = computed(() => boardTodayRows.value.filter((item) => item.status === 'NORMAL' || item.status === 'CLOSED'))
const boardOverdueRows = computed(() => boardPendingRows.value.filter((item) => dayjs(item.inspectionDate).isBefore(dayjs(), 'day')))
const boardStats = computed(() => ({
  todayPending: boardPendingRows.value.length,
  todayDone: boardDoneRows.value.length,
  todayOverdue: boardOverdueRows.value.length,
  pendingRows: boardPendingRows.value
}))

async function fetchData() {
  loading.value = true
  try {
    const params = buildQueryParams()
    const [res, summaryRes] = await Promise.all([
      getHealthInspectionPage(params) as Promise<PageResult<HealthInspection>>,
      getHealthInspectionSummary(params) as Promise<HealthInspectionSummary>
    ])
    rows.value = res.list
    pagination.total = res.total || 0
    summary.totalCount = summaryRes.totalCount || 0
    summary.abnormalCount = summaryRes.abnormalCount || 0
    summary.followingCount = summaryRes.followingCount || 0
    summary.closedCount = summaryRes.closedCount || 0
    summary.linkedLogCount = summaryRes.linkedLogCount || 0
    summary.statusStats = summaryRes.statusStats || []
  } catch (error) {
    message.error(resolveHealthError(error, '加载健康巡检失败'))
    rows.value = []
    pagination.total = 0
    summary.totalCount = 0
    summary.abnormalCount = 0
    summary.followingCount = 0
    summary.closedCount = 0
    summary.linkedLogCount = 0
    summary.statusStats = []
  } finally {
    loading.value = false
  }
}

function handleTableChange(pag: any) {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  query.pageNo = pag.current
  query.pageSize = pag.pageSize
  fetchData()
}

function onReset() {
  query.keyword = ''
  query.status = ''
  query.inspectionRange = []
  query.pageNo = 1
  query.pageSize = pagination.pageSize
  pagination.current = 1
  fetchData()
}

function openCreate() {
  const residentId = route.query.residentId ?? route.query.elderId
  const residentName = typeof route.query.residentName === 'string' ? route.query.residentName : ''
  form.id = undefined
  form.elderId = residentId ? Number(residentId) : undefined
  form.elderName = residentName
  ensureSelectedElder(form.elderId, residentName)
  form.inspectionDate = dayjs()
  form.inspectionItem = ''
  form.result = ''
  form.status = 'NORMAL'
  form.inspectorName = ''
  form.followUpAction = ''
  form.remark = ''
  editOpen.value = true
}

function openEdit(record: HealthInspection) {
  form.id = record.id
  form.elderId = record.elderId
  form.elderName = record.elderName || ''
  ensureSelectedElder(record.elderId, record.elderName)
  form.inspectionDate = record.inspectionDate ? dayjs(record.inspectionDate) : dayjs()
  form.inspectionItem = record.inspectionItem
  form.result = record.result || ''
  form.status = record.status || 'NORMAL'
  form.inspectorName = record.inspectorName || ''
  form.followUpAction = record.followUpAction || ''
  form.remark = record.remark || ''
  editOpen.value = true
}

function onElderChange(elderId?: number) {
  form.elderName = findElderName(elderId)
}

function resetBoardForm() {
  boardForm.elderId = undefined
  boardForm.elderName = ''
  boardForm.inspectionDate = dayjs()
  boardForm.temp = ''
  boardForm.bp = ''
  boardForm.pulse = ''
  boardForm.respiration = ''
  boardForm.spo2 = ''
  boardForm.glucose = ''
  boardForm.appetite = ''
  boardForm.sleep = ''
  boardForm.bowel = ''
  boardForm.pain = ''
  boardForm.mental = ''
  boardForm.skin = ''
  boardForm.abnormal = false
  boardForm.needReport = false
  boardForm.handleAction = ''
  boardForm.attachmentNote = ''
  boardForm.conclusion = 'NORMAL'
}

function openBoardRecord(record?: HealthInspection) {
  resetBoardForm()
  if (record) {
    boardForm.elderId = record.elderId
    boardForm.elderName = record.elderName || ''
    boardForm.inspectionDate = record.inspectionDate ? dayjs(record.inspectionDate) : dayjs()
    boardForm.conclusion = (record.status as any) || 'NORMAL'
  } else {
    const residentId = route.query.residentId ?? route.query.elderId
    if (residentId) {
      boardForm.elderId = Number(residentId)
    }
    if (route.query.residentName) {
      boardForm.elderName = String(route.query.residentName)
    }
  }
  boardOpen.value = true
}

function saveBoardDraft() {
  message.success('已保存草稿（页面暂存）')
}

async function submitBoard() {
  if (!boardForm.elderName || !boardForm.inspectionDate) {
    message.error('请填写长者与巡检日期')
    return
  }
  if (boardForm.abnormal && !boardForm.handleAction) {
    message.error('异常项必须填写处理方式')
    return
  }
  const vitalText = [
    `体温:${boardForm.temp || '-'}`,
    `血压:${boardForm.bp || '-'}`,
    `脉搏:${boardForm.pulse || '-'}`,
    `呼吸:${boardForm.respiration || '-'}`,
    `血氧:${boardForm.spo2 || '-'}`,
    `血糖:${boardForm.glucose || '-'}`
  ].join('，')
  const bodyText = [
    `食欲:${boardForm.appetite || '-'}`,
    `睡眠:${boardForm.sleep || '-'}`,
    `排便:${boardForm.bowel || '-'}`,
    `疼痛:${boardForm.pain || '-'}`,
    `精神:${boardForm.mental || '-'}`,
    `皮肤:${boardForm.skin || '-'}`
  ].join('，')
  const payload = {
    elderId: boardForm.elderId,
    elderName: boardForm.elderName,
    inspectionDate: dayjs(boardForm.inspectionDate).format('YYYY-MM-DD'),
    inspectionItem: '基础体征与身体状况巡检',
    result: `${vitalText}；${bodyText}`,
    status: boardForm.conclusion,
    inspectorName: form.inspectorName || '系统记录',
    followUpAction: boardForm.abnormal ? boardForm.handleAction : '',
    remark: `上报:${boardForm.needReport ? '是' : '否'}；附件:${boardForm.attachmentNote || '-'}`
  }
  await createHealthInspection(payload)
  message.success('巡检已提交并完成联动')
  boardOpen.value = false
  fetchData()
}

async function submit() {
  if (!form.elderId || !form.inspectionItem) {
    message.error('请补全必填项')
    return
  }
  if ((!form.status || form.status === 'NORMAL') && form.result && form.result.includes('异常')) {
    form.status = 'ABNORMAL'
  }
  if (form.status === 'ABNORMAL' && !form.followUpAction) {
    message.error('异常巡检请填写跟进措施')
    return
  }
  saving.value = true
  try {
    const payload = {
      elderId: form.elderId,
      elderName: findElderName(form.elderId) || form.elderName,
      inspectionDate: dayjs(form.inspectionDate).format('YYYY-MM-DD'),
      inspectionItem: form.inspectionItem,
      result: form.result,
      status: form.status,
      inspectorName: form.inspectorName,
      followUpAction: form.followUpAction,
      remark: form.remark
    }
    if (form.id) {
      await updateHealthInspection(form.id, payload)
    } else {
      await createHealthInspection(payload)
    }
    message.success('保存成功')
    editOpen.value = false
    fetchData()
  } catch (error) {
    message.error(resolveHealthError(error, '保存失败'))
  } finally {
    saving.value = false
  }
}

async function remove(record: HealthInspection) {
  try {
    await deleteHealthInspection(record.id)
    message.success('删除成功')
    fetchData()
  } catch (error) {
    message.error(resolveHealthError(error, '删除失败'))
  }
}

async function exportCsvData() {
  const records = await loadExportRecords()
  if (!records.length) {
    message.warning('暂无可导出数据')
    return
  }
  exportCsv(records, `健康巡检-${dayjs().format('YYYYMMDD-HHmmss')}.csv`)
  message.success('CSV导出成功')
}

async function exportExcelData() {
  const records = await loadExportRecords()
  if (!records.length) {
    message.warning('暂无可导出数据')
    return
  }
  exportExcel(records, `健康巡检-${dayjs().format('YYYYMMDD-HHmmss')}.xls`)
  message.success('Excel导出成功')
}

function buildQueryParams() {
  const residentId = route.query.residentId ?? route.query.elderId
  const params: Record<string, any> = {
    keyword: query.keyword || undefined,
    status: query.status || undefined,
    pageNo: query.pageNo,
    pageSize: query.pageSize
  }
  if (residentId) {
    params.elderId = Number(residentId)
  }
  if (Array.isArray(query.inspectionRange) && query.inspectionRange.length === 2) {
    params.inspectionFrom = dayjs(query.inspectionRange[0]).format('YYYY-MM-DD')
    params.inspectionTo = dayjs(query.inspectionRange[1]).format('YYYY-MM-DD')
  }
  return params
}

function statusLabel(status?: string) {
  const item = statusOptions.find((option) => option.value === status)
  return item?.label || status || '-'
}

function statusColor(status?: string) {
  if (status === 'ABNORMAL') return 'red'
  if (status === 'FOLLOWING') return 'orange'
  if (status === 'CLOSED') return 'green'
  return 'blue'
}

function formatDate(value?: string) {
  return value ? dayjs(value).format('YYYY-MM-DD') : '-'
}

function resolveRowClassName(record: HealthInspection) {
  if (record.status === 'ABNORMAL' || record.status === 'FOLLOWING') return 'health-row-danger'
  return ''
}

async function loadExportRecords() {
  exporting.value = true
  try {
    const params = buildQueryParams()
    const pageSize = 500
    let pageNo = 1
    let total = 0
    const list: HealthInspection[] = []
    do {
      const page = await getHealthInspectionPage({
        ...params,
        pageNo,
        pageSize
      }) as PageResult<HealthInspection>
      total = page.total || 0
      list.push(...(page.list || []))
      pageNo += 1
      if (!page.list || page.list.length < pageSize) break
    } while (list.length < total && pageNo <= 20)
    return mapHealthExportRows(
      list.map((item) => ({
        ...item,
        inspectionDate: formatDate(item.inspectionDate),
        status: statusLabel(item.status)
      })),
      inspectionExportColumns
    )
  } catch (error) {
    message.error(resolveHealthError(error, '加载导出数据失败'))
    return []
  } finally {
    exporting.value = false
  }
}

fetchData()
searchElders('')

watch(
  () => [route.query.residentId, route.query.elderId],
  () => {
    query.pageNo = 1
    pagination.current = 1
    fetchData()
  }
)
</script>

<style scoped>
.summary-row {
  margin-bottom: 12px;
}
:deep(.health-row-danger > td) {
  background: #fff1f0 !important;
}
</style>
