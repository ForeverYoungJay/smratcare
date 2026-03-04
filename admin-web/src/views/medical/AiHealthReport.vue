<template>
  <PageContainer title="AI健康评估报告" subTitle="报告查看、发布与一键生成任务">
    <SearchForm :model="query" @search="loadReports" @reset="onReset">
      <a-form-item label="报告类型">
        <a-select v-model:value="query.type" allow-clear style="width: 160px" :options="typeOptions" />
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="query.status" allow-clear style="width: 140px" :options="statusOptions" />
      </a-form-item>
      <a-form-item label="日期范围">
        <a-range-picker v-model:value="query.range" />
      </a-form-item>
      <template #extra>
        <a-button type="primary" @click="openCreate">生成报告</a-button>
      </template>
    </SearchForm>

    <DataTable rowKey="id" :columns="columns" :data-source="rows" :loading="loading" :pagination="false">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="statusColor(record.status)">{{ statusLabel(record.status) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="openDetail(record)">详情</a-button>
            <a-button type="link" @click="publish(record)" :disabled="record.status === 'PUBLISHED'">发布</a-button>
            <a-button type="link" @click="generateTasks(record)">生成任务</a-button>
          </a-space>
        </template>
      </template>
    </DataTable>

    <a-drawer v-model:open="detailOpen" title="报告详情" width="760">
      <a-descriptions bordered :column="2" size="small">
        <a-descriptions-item label="报告类型">{{ current?.typeLabel }}</a-descriptions-item>
        <a-descriptions-item label="状态">{{ statusLabel(current?.status) }}</a-descriptions-item>
        <a-descriptions-item label="数据范围">{{ current?.rangeText }}</a-descriptions-item>
        <a-descriptions-item label="高风险提示">{{ current?.highRiskCount }}</a-descriptions-item>
      </a-descriptions>
      <a-card title="建议行动" class="card-elevated" style="margin-top: 12px" :bordered="false">
        <a-space wrap>
          <a-button type="primary" @click="go('/medical-care/inspection?filter=generated_from_ai')">生成巡检计划</a-button>
          <a-button @click="go('/medical-care/care-task-board?filter=generated_from_ai')">生成医护随访任务</a-button>
          <a-button @click="go('/care/workbench/task-board?filter=generated_from_ai')">生成护理干预任务</a-button>
          <a-button @click="go('/elder/resident-360?from=ai_report')">发起家属沟通记录</a-button>
        </a-space>
      </a-card>
    </a-drawer>
  </PageContainer>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'

type ReportStatus = 'GENERATING' | 'GENERATED' | 'PUBLISHED'
type ReportType = 'WEEKLY' | 'MONTHLY' | 'CVD' | 'CHRONIC'

interface ReportItem {
  id: number
  type: ReportType
  typeLabel: string
  status: ReportStatus
  rangeText: string
  highRiskCount: number
  createdAt: string
}

const router = useRouter()
const loading = ref(false)
const detailOpen = ref(false)
const current = ref<ReportItem>()
const query = reactive({
  type: undefined as ReportType | undefined,
  status: undefined as ReportStatus | undefined,
  range: [] as any[]
})

const columns = [
  { title: '报告类型', dataIndex: 'typeLabel', key: 'typeLabel', width: 120 },
  { title: '状态', key: 'status', width: 120 },
  { title: '数据范围', dataIndex: 'rangeText', key: 'rangeText', width: 220 },
  { title: '风险摘要', dataIndex: 'highRiskCount', key: 'highRiskCount', width: 120 },
  { title: '生成时间', dataIndex: 'createdAt', key: 'createdAt', width: 180 },
  { title: '操作', key: 'action', width: 220 }
]

const typeOptions = [
  { label: '周报', value: 'WEEKLY' },
  { label: '月报', value: 'MONTHLY' },
  { label: '专项-心血管', value: 'CVD' },
  { label: '专项-慢病', value: 'CHRONIC' }
]
const statusOptions = [
  { label: '生成中', value: 'GENERATING' },
  { label: '已生成', value: 'GENERATED' },
  { label: '已发布', value: 'PUBLISHED' }
]

const rows = ref<ReportItem[]>([])

function statusLabel(value?: ReportStatus) {
  if (value === 'GENERATING') return '生成中'
  if (value === 'GENERATED') return '已生成'
  if (value === 'PUBLISHED') return '已发布'
  return '-'
}

function statusColor(value?: ReportStatus) {
  if (value === 'GENERATING') return 'processing'
  if (value === 'GENERATED') return 'blue'
  if (value === 'PUBLISHED') return 'green'
  return 'default'
}

function buildMockRows() {
  return [
    { id: 1, type: 'WEEKLY', typeLabel: '周报', status: 'GENERATED', rangeText: '2026-02-24 ~ 2026-03-02', highRiskCount: 6, createdAt: dayjs().subtract(1, 'day').format('YYYY-MM-DD HH:mm:ss') },
    { id: 2, type: 'MONTHLY', typeLabel: '月报', status: 'PUBLISHED', rangeText: '2026-02-01 ~ 2026-02-28', highRiskCount: 18, createdAt: dayjs().subtract(3, 'day').format('YYYY-MM-DD HH:mm:ss') },
    { id: 3, type: 'CVD', typeLabel: '专项-心血管', status: 'GENERATING', rangeText: '2026-02-20 ~ 2026-03-03', highRiskCount: 9, createdAt: dayjs().format('YYYY-MM-DD HH:mm:ss') }
  ] as ReportItem[]
}

function loadReports() {
  loading.value = true
  try {
    let result = buildMockRows()
    if (query.type) {
      result = result.filter((item) => item.type === query.type)
    }
    if (query.status) {
      result = result.filter((item) => item.status === query.status)
    }
    rows.value = result
  } finally {
    loading.value = false
  }
}

function onReset() {
  query.type = undefined
  query.status = undefined
  query.range = []
  loadReports()
}

function openCreate() {
  rows.value.unshift({
    id: Date.now(),
    type: 'WEEKLY',
    typeLabel: '周报',
    status: 'GENERATING',
    rangeText: `${dayjs().subtract(7, 'day').format('YYYY-MM-DD')} ~ ${dayjs().format('YYYY-MM-DD')}`,
    highRiskCount: 0,
    createdAt: dayjs().format('YYYY-MM-DD HH:mm:ss')
  })
  message.success('已创建生成任务')
}

function openDetail(record: ReportItem) {
  current.value = record
  detailOpen.value = true
}

function publish(record: ReportItem) {
  record.status = 'PUBLISHED'
  message.success('报告已发布并可在长者总览查看摘要')
}

function generateTasks(record: ReportItem) {
  current.value = record
  detailOpen.value = true
  message.success('请选择要生成的任务类型')
}

function go(path: string) {
  router.push(path)
}

loadReports()
</script>
