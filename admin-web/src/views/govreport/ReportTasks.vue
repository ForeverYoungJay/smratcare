<template>
  <PageContainer title="监管上报" subTitle="民政金民工程 / 医保结算上报：构建报文 → 发送 → 回执 → 重试">
    <SearchForm :model="query" @search="onSearch" @reset="onReset">
      <a-form-item label="渠道">
        <a-select v-model:value="query.channel" allow-clear style="width: 160px" placeholder="全部渠道">
          <a-select-option value="MZ_JINMIN">民政金民</a-select-option>
          <a-select-option value="YB_MEDICAL">医保结算</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="上报类型">
        <a-select v-model:value="query.reportType" allow-clear style="width: 160px" placeholder="全部类型">
          <a-select-option v-for="t in reportTypeOptions" :key="t.value" :value="t.value">{{ t.label }}</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="query.status" allow-clear style="width: 140px" placeholder="全部">
          <a-select-option value="PENDING">待发送</a-select-option>
          <a-select-option value="SENT">已发送</a-select-option>
          <a-select-option value="ACKED">已回执</a-select-option>
          <a-select-option value="FAILED">失败</a-select-option>
        </a-select>
      </a-form-item>
      <template #extra>
        <a-button type="primary" @click="openBuild">构建上报任务</a-button>
      </template>
    </SearchForm>

    <DataTable rowKey="id" :columns="columns" :data-source="rows" :loading="loading" :pagination="pagination" @change="handleTableChange">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'channel'">
          <a-tag :color="record.channel === 'MZ_JINMIN' ? 'geekblue' : 'purple'">{{ channelText(record.channel) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'reportType'">{{ reportTypeText(record.reportType) }}</template>
        <template v-else-if="column.key === 'taskStatus'">
          <a-tag :color="statusColor(record.taskStatus)">{{ statusText(record.taskStatus) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <div class="row-action-links">
            <a-button type="link" size="small" @click="viewSnapshot(record)">报文</a-button>
            <a-button
              v-if="['PENDING','FAILED'].includes(record.taskStatus)"
              type="link"
              size="small"
              @click="doSend(record)"
            >发送</a-button>
          </div>
        </template>
      </template>
    </DataTable>

    <!-- 构建任务 -->
    <a-modal v-model:open="buildOpen" title="构建上报任务" :confirm-loading="saving" @ok="submitBuild">
      <a-form layout="vertical">
        <a-form-item label="渠道" required>
          <a-select v-model:value="buildForm.channel">
            <a-select-option value="MZ_JINMIN">民政金民工程</a-select-option>
            <a-select-option value="YB_MEDICAL">医保结算</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="上报类型" required>
          <a-select v-model:value="buildForm.reportType" :options="reportTypeOptions" />
        </a-form-item>
        <a-form-item label="上报周期">
          <a-input v-model:value="buildForm.period" placeholder="如 202606（LTCI_SETTLE 必填）或 2026Q2" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 报文快照 -->
    <a-modal v-model:open="snapshotOpen" title="报文快照" :footer="null" width="720px">
      <a-descriptions v-if="snapshot" :column="1" size="small" bordered style="margin-bottom: 12px">
        <a-descriptions-item label="字段映射版本">{{ snapshot.fieldMappingVersion || '-' }}</a-descriptions-item>
        <a-descriptions-item label="报文指纹">{{ snapshot.payloadHash || '-' }}</a-descriptions-item>
      </a-descriptions>
      <pre class="payload-box">{{ prettyPayload }}</pre>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { buildGovReportTask, sendGovReportTask, getGovReportTaskPage, getGovReportSnapshot } from '../../api/govreport'
import type { GovReportSnapshot, GovReportTask, PageResult } from '../../types'

const reportTypeOptions = [
  { label: '机构信息', value: 'ORG_INFO' },
  { label: '床位', value: 'BED' },
  { label: '长者信息', value: 'ELDER' },
  { label: '服务记录', value: 'SERVICE' },
  { label: '长护险结算', value: 'LTCI_SETTLE' }
]

const loading = ref(false)
const saving = ref(false)
const rows = ref<GovReportTask[]>([])
const query = reactive({
  channel: undefined as string | undefined,
  reportType: undefined as string | undefined,
  status: undefined as string | undefined,
  pageNo: 1,
  pageSize: 10
})
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

const columns = [
  { title: '任务ID', dataIndex: 'id', key: 'id', width: 120 },
  { title: '渠道', key: 'channel', width: 110 },
  { title: '上报类型', key: 'reportType', width: 120 },
  { title: '周期', dataIndex: 'period', key: 'period', width: 100 },
  { title: '记录数', dataIndex: 'recordCount', key: 'recordCount', width: 80 },
  { title: '重试', dataIndex: 'retryCount', key: 'retryCount', width: 70 },
  { title: '状态', key: 'taskStatus', width: 100 },
  { title: '最近错误', dataIndex: 'lastError', key: 'lastError', ellipsis: true },
  { title: '操作', key: 'action', width: 140 }
]

const buildOpen = ref(false)
const buildForm = reactive({ channel: 'MZ_JINMIN', reportType: 'LTCI_SETTLE', period: '' })

const snapshotOpen = ref(false)
const snapshot = ref<GovReportSnapshot | null>(null)
const prettyPayload = computed(() => {
  if (!snapshot.value?.payloadJson) return '（无报文）'
  try {
    return JSON.stringify(JSON.parse(snapshot.value.payloadJson), null, 2)
  } catch {
    return snapshot.value.payloadJson
  }
})

function channelText(c?: string) {
  return c === 'MZ_JINMIN' ? '民政金民' : c === 'YB_MEDICAL' ? '医保结算' : c || '-'
}
function reportTypeText(t?: string) {
  return reportTypeOptions.find((o) => o.value === t)?.label || t || '-'
}
function statusText(s?: string) {
  return ({ PENDING: '待发送', SENT: '已发送', ACKED: '已回执', FAILED: '失败' } as Record<string, string>)[s || ''] || s || '-'
}
function statusColor(s?: string) {
  if (s === 'ACKED') return 'green'
  if (s === 'SENT') return 'blue'
  if (s === 'FAILED') return 'red'
  return 'orange'
}

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<GovReportTask> = await getGovReportTaskPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      channel: query.channel,
      reportType: query.reportType,
      status: query.status
    })
    rows.value = res.list
    pagination.total = res.total || res.list.length
  } finally {
    loading.value = false
  }
}
function onSearch() {
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}
function onReset() {
  query.channel = undefined
  query.reportType = undefined
  query.status = undefined
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}
function handleTableChange(pag: any) {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  query.pageNo = pag.current
  query.pageSize = pag.pageSize
  fetchData()
}

function openBuild() {
  buildForm.channel = 'MZ_JINMIN'
  buildForm.reportType = 'LTCI_SETTLE'
  buildForm.period = ''
  buildOpen.value = true
}
async function submitBuild() {
  saving.value = true
  try {
    await buildGovReportTask({
      channel: buildForm.channel,
      reportType: buildForm.reportType,
      period: buildForm.period || undefined
    })
    message.success('上报任务已构建')
    buildOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function doSend(record: GovReportTask) {
  const res = await sendGovReportTask(record.id)
  message.success(`已发送，状态：${statusText(res.taskStatus)}`)
  fetchData()
}

async function viewSnapshot(record: GovReportTask) {
  snapshot.value = await getGovReportSnapshot(record.id)
  snapshotOpen.value = true
}

fetchData()
</script>

<style scoped>
.payload-box {
  max-height: 420px;
  overflow: auto;
  background: #0b1021;
  color: #d6e2ff;
  padding: 12px;
  border-radius: 8px;
  font-size: 12px;
  line-height: 1.6;
}
</style>
