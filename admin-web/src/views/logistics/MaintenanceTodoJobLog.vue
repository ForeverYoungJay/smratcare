<template>
  <PageContainer title="维保待办执行日志" subTitle="查看手动/自动生成维保待办的执行记录">
    <a-row :gutter="12" style="margin-bottom: 12px">
      <a-col :xs="24" :md="8">
        <a-card size="small" :title="`近${overview.recentWindowDays || overviewDays}天执行`">
          <div>总次数：{{ overview.recentTotalRuns || 0 }}</div>
          <div>成功：{{ overview.recentSuccessRuns || 0 }}</div>
          <div>失败：<a-typography-text type="danger">{{ overview.recentFailedRuns || 0 }}</a-typography-text></div>
        </a-card>
      </a-col>
      <a-col :xs="24" :md="8">
        <a-card size="small" title="最近一次执行">
          <div>时间：{{ overview.lastExecutedAt || '-' }}</div>
          <div>状态：{{ statusLabel(overview.lastStatus) }}</div>
          <div>结果：新建{{ overview.lastCreatedCount || 0 }} / 跳过{{ overview.lastSkippedCount || 0 }}</div>
        </a-card>
      </a-col>
      <a-col :xs="24" :md="8">
        <a-card size="small" title="失败重跑">
          <div>最近失败时间：{{ overview.lastFailedExecutedAt || '-' }}</div>
          <div class="error-line">错误：{{ overview.lastFailedErrorMessage || '无' }}</div>
          <a-button style="margin-top: 8px" @click="rerunLatestFailed">重跑最近失败</a-button>
        </a-card>
      </a-col>
    </a-row>

    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="触发方式">
        <a-select v-model:value="query.triggerType" :options="triggerTypeOptions" allow-clear style="width: 160px" />
      </a-form-item>
      <a-form-item label="执行状态">
        <a-select v-model:value="query.status" :options="statusOptions" allow-clear style="width: 160px" />
      </a-form-item>
      <template #extra>
        <a-select
          v-model:value="overviewDays"
          style="width: 120px"
          :options="overviewDayOptions"
          @change="fetchOverview"
        />
        <a-button @click="rerunLatestFailed">重跑最近失败</a-button>
        <a-button @click="exportData">导出CSV</a-button>
        <a-button type="primary" @click="fetchData">刷新</a-button>
      </template>
    </SearchForm>

    <DataTable rowKey="id" :columns="columns" :data-source="rows" :loading="loading" :pagination="pagination" @change="handleTableChange">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'triggerType'">
          <a-tag :color="record.triggerType === 'SCHEDULED' ? 'blue' : 'gold'">
            {{ record.triggerType === 'SCHEDULED' ? '自动' : '手动' }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'status'">
          <a-tag :color="record.status === 'SUCCESS' ? 'green' : 'red'">
            {{ record.status === 'SUCCESS' ? '成功' : '失败' }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'result'">
          命中{{ record.totalMatched || 0 }} / 新建{{ record.createdCount || 0 }} / 跳过{{ record.skippedCount || 0 }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-button type="link" @click="rerun(record)">按本次天数重跑</a-button>
        </template>
      </template>
    </DataTable>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import {
  exportMaintenanceTodoJobLog,
  getMaintenanceTodoJobLogOverview,
  getMaintenanceTodoJobLogPage,
  rerunLatestFailedMaintenanceTodoJobLog,
  rerunMaintenanceTodoJobLog
} from '../../api/logistics'
import type { LogisticsMaintenanceTodoJobLog, LogisticsMaintenanceTodoJobLogOverview, PageResult } from '../../types'

const loading = ref(false)
const rows = ref<LogisticsMaintenanceTodoJobLog[]>([])
const overviewDays = ref(7)
const overviewDayOptions = [
  { label: '近7天', value: 7 },
  { label: '近15天', value: 15 },
  { label: '近30天', value: 30 }
]
const overview = reactive<LogisticsMaintenanceTodoJobLogOverview>({
  recentTotalRuns: 0,
  recentSuccessRuns: 0,
  recentFailedRuns: 0,
  recentWindowDays: 7
})
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

const query = reactive({
  triggerType: undefined as string | undefined,
  status: undefined as string | undefined,
  pageNo: 1,
  pageSize: 10
})

const triggerTypeOptions = [
  { label: '手动', value: 'MANUAL' },
  { label: '自动', value: 'SCHEDULED' },
  { label: '重跑', value: 'RETRY' }
]

const statusOptions = [
  { label: '成功', value: 'SUCCESS' },
  { label: '失败', value: 'FAILED' }
]

const columns = [
  { title: '执行时间', dataIndex: 'executedAt', key: 'executedAt', width: 180 },
  { title: '触发方式', dataIndex: 'triggerType', key: 'triggerType', width: 100 },
  { title: '执行状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '扫描天数', dataIndex: 'days', key: 'days', width: 100 },
  { title: '执行结果', key: 'result', width: 280 },
  { title: '错误信息', dataIndex: 'errorMessage', key: 'errorMessage' },
  { title: '操作', key: 'action', width: 140 }
]

async function fetchData() {
  loading.value = true
  try {
    await fetchOverview()
    const res: PageResult<LogisticsMaintenanceTodoJobLog> = await getMaintenanceTodoJobLogPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      triggerType: query.triggerType || undefined,
      status: query.status || undefined
    })
    rows.value = res.list || []
    pagination.total = res.total || 0
  } finally {
    loading.value = false
  }
}

function onReset() {
  query.triggerType = undefined
  query.status = undefined
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

function handleTableChange(pag: any) {
  query.pageNo = pag.current
  query.pageSize = pag.pageSize
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchData()
}

async function rerun(record: LogisticsMaintenanceTodoJobLog) {
  const res: any = await rerunMaintenanceTodoJobLog(record.id)
  if (Number(res?.blockedByWindow || 0)) {
    message.warning('仅支持重跑30天内的执行记录')
    return
  }
  if (Number(res?.blockedByCooldown || 0)) {
    message.warning('重跑过于频繁，请2分钟后再试')
    return
  }
  if (!Number(res?.rerunTriggered || 0)) {
    message.info('未触发重跑')
    return
  }
  message.success('已触发重跑')
  fetchData()
}

async function rerunLatestFailed() {
  const res: any = await rerunLatestFailedMaintenanceTodoJobLog()
  if (Number(res?.blockedByWindow || 0)) {
    message.warning('仅支持重跑30天内的失败记录')
    return
  }
  if (Number(res?.blockedByCooldown || 0)) {
    message.warning('重跑过于频繁，请2分钟后再试')
    return
  }
  if (!Number(res?.rerunTriggered || 0)) {
    message.info('暂无失败记录可重跑')
    return
  }
  message.success(`重跑完成：新建${Number(res?.createdCount || 0)}，跳过${Number(res?.skippedCount || 0)}`)
  fetchData()
}

async function fetchOverview() {
  const data = await getMaintenanceTodoJobLogOverview(overviewDays.value)
  Object.assign(overview, data || {})
}

function statusLabel(status?: string) {
  if (!status) return '-'
  return status === 'SUCCESS' ? '成功' : '失败'
}

async function exportData() {
  await exportMaintenanceTodoJobLog({
    triggerType: query.triggerType || undefined,
    status: query.status || undefined
  })
}

onMounted(fetchData)
</script>

<style scoped>
.error-line {
  margin-top: 4px;
  min-height: 22px;
  color: #cf1322;
  line-height: 1.45;
}
</style>
