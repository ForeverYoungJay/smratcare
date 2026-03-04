<template>
  <PageContainer title="活动计划" subTitle="活动排期、组织安排与执行跟踪">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键词">
        <a-input v-model:value="query.keyword" placeholder="计划标题/组织人/地点" allow-clear />
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="query.status" allow-clear style="width: 160px" :options="statusOptions" />
      </a-form-item>
      <a-form-item label="日期范围">
        <a-range-picker v-model:value="query.range" />
      </a-form-item>
      <template #extra>
        <a-button type="primary" @click="openEdit()">新增计划</a-button>
        <a-button :disabled="!selectedSingleRecord || !canEditSelected" @click="editSelected">编辑</a-button>
        <a-button :disabled="!selectedSingleRecord || !canStartSelected" @click="startSelected">开始</a-button>
        <a-button :disabled="!selectedSingleRecord || !canDoneSelected" @click="doneSelected">完成</a-button>
        <a-button :disabled="!selectedSingleRecord || !canCancelSelected" @click="cancelSelected">取消</a-button>
        <a-button :disabled="!selectedSingleRecord" danger @click="removeSelected">删除</a-button>
        <a-button :disabled="selectedStartIds.length === 0" @click="batchStart">批量开始</a-button>
        <a-button :disabled="selectedDoneIds.length === 0" @click="batchDone">批量完成</a-button>
        <a-button :disabled="selectedCancelIds.length === 0" @click="batchCancel">批量取消</a-button>
        <a-button :disabled="selectedRowKeys.length === 0" danger @click="batchRemove">批量删除</a-button>
        <a-button @click="downloadExport">导出CSV</a-button>
        <span class="selection-tip">已勾选 {{ selectedRowKeys.length }} 条，批量状态流转按当前状态自动过滤</span>
      </template>
    </SearchForm>

    <DataTable
      rowKey="id"
      :row-selection="rowSelection"
      :columns="columns"
      :data-source="rows"
      :loading="loading"
      :pagination="pagination"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="statusColor(record.status)">{{ statusText(record.status) }}</a-tag>
        </template>
      </template>
    </DataTable>

    <a-modal v-model:open="editOpen" :title="form.id ? '编辑活动计划' : '新增活动计划'" @ok="submit" :confirm-loading="saving" width="760px">
      <a-form layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="计划标题" required>
              <a-input v-model:value="form.title" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="计划日期" required>
              <a-date-picker v-model:value="planDate" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="开始时间">
              <a-date-picker v-model:value="startTime" show-time style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="结束时间">
              <a-date-picker v-model:value="endTime" show-time style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="地点">
              <a-input v-model:value="form.location" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="组织人">
              <a-input v-model:value="form.organizer" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="参与对象">
          <a-input v-model:value="form.participantTarget" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="form.status" :options="statusOptions" />
        </a-form-item>
        <a-form-item label="计划内容">
          <a-textarea v-model:value="form.content" :rows="4" />
        </a-form-item>
        <a-form-item label="备注">
          <a-textarea v-model:value="form.remark" :rows="2" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import dayjs, { type Dayjs } from 'dayjs'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import {
  batchCancelActivityPlan,
  batchCompleteActivityPlan,
  batchDeleteActivityPlan,
  batchStartActivityPlan,
  cancelActivityPlan,
  completeActivityPlan,
  createActivityPlan,
  deleteActivityPlan,
  exportActivityPlan,
  getActivityPlanPage,
  startActivityPlan,
  updateActivityPlan
} from '../../api/oa'
import type { OaActivityPlan, PageResult } from '../../types'

const loading = ref(false)
const saving = ref(false)
const rows = ref<OaActivityPlan[]>([])
const query = reactive({
  keyword: '',
  status: undefined as string | undefined,
  range: undefined as [Dayjs, Dayjs] | undefined,
  pageNo: 1,
  pageSize: 10
})
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const selectedRowKeys = ref<string[]>([])

const columns = [
  { title: '计划标题', dataIndex: 'title', key: 'title', width: 200 },
  { title: '计划日期', dataIndex: 'planDate', key: 'planDate', width: 120 },
  { title: '组织人', dataIndex: 'organizer', key: 'organizer', width: 120 },
  { title: '地点', dataIndex: 'location', key: 'location', width: 140 },
  { title: '参与对象', dataIndex: 'participantTarget', key: 'participantTarget', width: 160 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 110 }
]

const statusOptions = [
  { label: '计划中', value: 'PLANNED' },
  { label: '进行中', value: 'IN_PROGRESS' },
  { label: '已完成', value: 'DONE' },
  { label: '已取消', value: 'CANCELLED' }
]

const editOpen = ref(false)
const planDate = ref<Dayjs | undefined>()
const startTime = ref<Dayjs | undefined>()
const endTime = ref<Dayjs | undefined>()
const form = reactive<Partial<OaActivityPlan>>({ status: 'PLANNED' })
const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: (string | number)[]) => {
    selectedRowKeys.value = keys.map((item) => String(item))
  }
}))
const selectedRecords = computed(() => rows.value.filter((item) => selectedRowKeys.value.includes(String(item.id))))
const selectedSingleRecord = computed(() => (selectedRecords.value.length === 1 ? selectedRecords.value[0] : null))
const canEditSelected = computed(() => !!selectedSingleRecord.value && selectedSingleRecord.value.status !== 'DONE' && selectedSingleRecord.value.status !== 'CANCELLED')
const canStartSelected = computed(() => selectedSingleRecord.value?.status === 'PLANNED')
const canDoneSelected = computed(() => selectedSingleRecord.value?.status === 'IN_PROGRESS')
const canCancelSelected = computed(() => !!selectedSingleRecord.value && selectedSingleRecord.value.status !== 'DONE' && selectedSingleRecord.value.status !== 'CANCELLED')
const selectedStartIds = computed(() =>
  selectedRecords.value.filter((item) => item.status === 'PLANNED').map((item) => String(item.id))
)
const selectedDoneIds = computed(() =>
  selectedRecords.value.filter((item) => item.status === 'IN_PROGRESS').map((item) => String(item.id))
)
const selectedCancelIds = computed(() =>
  selectedRecords.value.filter((item) => item.status !== 'DONE' && item.status !== 'CANCELLED').map((item) => String(item.id))
)

function statusText(status?: string) {
  if (status === 'IN_PROGRESS') return '进行中'
  if (status === 'DONE') return '已完成'
  if (status === 'CANCELLED') return '已取消'
  return '计划中'
}

function statusColor(status?: string) {
  if (status === 'DONE') return 'green'
  if (status === 'IN_PROGRESS') return 'blue'
  if (status === 'CANCELLED') return 'default'
  return 'orange'
}

async function fetchData() {
  loading.value = true
  try {
    const params: any = { ...query }
    if (query.range) {
      params.dateFrom = query.range[0].format('YYYY-MM-DD')
      params.dateTo = query.range[1].format('YYYY-MM-DD')
    }
    delete params.range
    const res: PageResult<OaActivityPlan> = await getActivityPlanPage(params)
    rows.value = (res.list || []).map((item) => ({
      ...item,
      id: String(item.id)
    }))
    pagination.total = res.total || res.list.length
    selectedRowKeys.value = []
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
  query.status = undefined
  query.range = undefined
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

function openEdit(record?: OaActivityPlan) {
  if (record && (record.status === 'DONE' || record.status === 'CANCELLED')) return
  Object.assign(form, record || { status: 'PLANNED', title: '', location: '', organizer: '', participantTarget: '', content: '', remark: '' })
  planDate.value = form.planDate ? dayjs(form.planDate) : undefined
  startTime.value = form.startTime ? dayjs(form.startTime) : undefined
  endTime.value = form.endTime ? dayjs(form.endTime) : undefined
  editOpen.value = true
}

async function submit() {
  if (!form.title || !planDate.value) return
  saving.value = true
  try {
    const payload = {
      ...form,
      planDate: planDate.value.format('YYYY-MM-DD'),
      startTime: startTime.value ? startTime.value.format('YYYY-MM-DDTHH:mm:ss') : undefined,
      endTime: endTime.value ? endTime.value.format('YYYY-MM-DDTHH:mm:ss') : undefined
    }
    if (form.id) {
      await updateActivityPlan(form.id, payload)
    } else {
      await createActivityPlan(payload)
    }
    editOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function remove(record: OaActivityPlan) {
  await deleteActivityPlan(String(record.id))
  fetchData()
}

async function start(record: OaActivityPlan) {
  if (record.status !== 'PLANNED') return
  await startActivityPlan(String(record.id))
  fetchData()
}

async function done(record: OaActivityPlan) {
  if (record.status !== 'IN_PROGRESS') return
  await completeActivityPlan(String(record.id))
  fetchData()
}

async function cancel(record: OaActivityPlan) {
  if (record.status === 'DONE' || record.status === 'CANCELLED') return
  await cancelActivityPlan(String(record.id))
  fetchData()
}

function requireSingleSelection(action: string) {
  if (!selectedSingleRecord.value) {
    message.info(`请先勾选 1 条计划后再${action}`)
    return null
  }
  return selectedSingleRecord.value
}

function editSelected() {
  const record = requireSingleSelection('编辑')
  if (!record) return
  openEdit(record)
}

async function startSelected() {
  const record = requireSingleSelection('开始')
  if (!record) return
  await start(record)
}

async function doneSelected() {
  const record = requireSingleSelection('完成')
  if (!record) return
  await done(record)
}

async function cancelSelected() {
  const record = requireSingleSelection('取消')
  if (!record) return
  await cancel(record)
}

async function removeSelected() {
  const record = requireSingleSelection('删除')
  if (!record) return
  await remove(record)
}

async function batchStart() {
  if (selectedStartIds.value.length === 0) {
    message.info('勾选项中没有可开始计划')
    return
  }
  const affected = await batchStartActivityPlan(selectedStartIds.value)
  message.success(`批量开始，共处理 ${affected || 0} 条`)
  fetchData()
}

async function batchDone() {
  if (selectedDoneIds.value.length === 0) {
    message.info('勾选项中没有可完成计划')
    return
  }
  const affected = await batchCompleteActivityPlan(selectedDoneIds.value)
  message.success(`批量完成，共处理 ${affected || 0} 条`)
  fetchData()
}

async function batchCancel() {
  if (selectedCancelIds.value.length === 0) {
    message.info('勾选项中没有可取消计划')
    return
  }
  const affected = await batchCancelActivityPlan(selectedCancelIds.value)
  message.success(`批量取消，共处理 ${affected || 0} 条`)
  fetchData()
}

async function batchRemove() {
  if (selectedRowKeys.value.length === 0) return
  const affected = await batchDeleteActivityPlan(selectedRowKeys.value)
  message.success(`批量删除，共处理 ${affected || 0} 条`)
  fetchData()
}

async function downloadExport() {
  const params: any = {
    keyword: query.keyword || undefined,
    status: query.status
  }
  if (query.range) {
    params.dateFrom = query.range[0].format('YYYY-MM-DD')
    params.dateTo = query.range[1].format('YYYY-MM-DD')
  }
  const blob = await exportActivityPlan(params)
  const href = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = href
  link.download = `oa-activity-plan-${new Date().toISOString().slice(0, 10)}.csv`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(href)
}

fetchData()
</script>

<style scoped>
.selection-tip {
  color: rgba(0, 0, 0, 0.45);
  font-size: 12px;
}
</style>
