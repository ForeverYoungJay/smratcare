<template>
  <PageContainer title="医嘱执行" subTitle="按计划时点执行签核（已执行/跳过）">
    <SearchForm :model="query" @search="onSearch" @reset="onReset">
      <a-form-item label="长者">
        <a-select v-model:value="query.elderId" show-search allow-clear :filter-option="false" :options="elderOptions"
          placeholder="输入姓名搜索" style="width: 200px" @search="searchElders" @focus="() => !elderOptions.length && searchElders('')" />
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="query.status" allow-clear style="width: 140px" placeholder="全部">
          <a-select-option value="PENDING">待执行</a-select-option>
          <a-select-option value="DONE">已执行</a-select-option>
          <a-select-option value="SKIPPED">已跳过</a-select-option>
        </a-select>
      </a-form-item>
    </SearchForm>

    <DataTable rowKey="id" :columns="columns" :data-source="rows" :loading="loading" :pagination="pagination" @change="handleTableChange">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="statusColor(record.status)">{{ statusText(record.status) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <div v-if="record.status === 'PENDING'" class="row-action-links">
            <a-button type="link" size="small" @click="openSign(record, 'DONE')">执行</a-button>
            <a-button type="link" size="small" danger @click="openSign(record, 'SKIPPED')">跳过</a-button>
          </div>
          <span v-else class="text-muted">{{ record.executorName || '已签核' }}</span>
        </template>
      </template>
    </DataTable>

    <a-modal v-model:open="signOpen" :title="signForm.status === 'DONE' ? '执行签核' : '跳过执行'" :confirm-loading="saving" @ok="submitSign">
      <a-form layout="vertical">
        <a-form-item :label="signForm.status === 'DONE' ? '执行结果' : '跳过原因'">
          <a-textarea v-model:value="signForm.result" :rows="3" />
        </a-form-item>
        <a-form-item v-if="signForm.status === 'DONE'" label="关联发药记录ID">
          <a-input-number v-model:value="signForm.dispenseRecordId" style="width:100%" placeholder="药疗执行选填" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { useElderOptions } from '../../composables/useElderOptions'
import { getOrderExecutionPage, signOrderExecution } from '../../api/medOrder'
import type { Id, MedicalOrderExecution, PageResult } from '../../types'

const loading = ref(false)
const saving = ref(false)
const rows = ref<MedicalOrderExecution[]>([])
const query = reactive({ elderId: undefined as Id | undefined, status: undefined as string | undefined, pageNo: 1, pageSize: 10 })
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const { elderOptions, searchElders } = useElderOptions({ pageSize: 50 })

const columns = [
  { title: '执行单ID', dataIndex: 'id', key: 'id', width: 120 },
  { title: '医嘱ID', dataIndex: 'orderId', key: 'orderId', width: 110 },
  { title: '长者ID', dataIndex: 'elderId', key: 'elderId', width: 100 },
  { title: '计划时间', dataIndex: 'planTime', key: 'planTime', width: 170 },
  { title: '执行时间', dataIndex: 'execTime', key: 'execTime', width: 170 },
  { title: '状态', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 150 }
]

const signOpen = ref(false)
const signForm = reactive({ executionId: undefined as Id | undefined, status: 'DONE', result: '', dispenseRecordId: undefined as number | undefined })

function statusText(s?: string) { return ({ PENDING: '待执行', DONE: '已执行', SKIPPED: '已跳过' } as Record<string, string>)[s || ''] || s || '-' }
function statusColor(s?: string) { return s === 'DONE' ? 'green' : s === 'SKIPPED' ? 'orange' : 'blue' }

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<MedicalOrderExecution> = await getOrderExecutionPage({ pageNo: query.pageNo, pageSize: query.pageSize, elderId: query.elderId, status: query.status })
    rows.value = res.list
    pagination.total = res.total || res.list.length
  } finally { loading.value = false }
}
function onSearch() { query.pageNo = 1; pagination.current = 1; fetchData() }
function onReset() { query.elderId = undefined; query.status = undefined; query.pageNo = 1; pagination.current = 1; fetchData() }
function handleTableChange(p: any) { pagination.current = p.current; pagination.pageSize = p.pageSize; query.pageNo = p.current; query.pageSize = p.pageSize; fetchData() }

function openSign(record: MedicalOrderExecution, status: string) {
  signForm.executionId = record.id
  signForm.status = status
  signForm.result = ''
  signForm.dispenseRecordId = undefined
  signOpen.value = true
}
async function submitSign() {
  if (saving.value) return
  saving.value = true
  try {
    await signOrderExecution({
      executionId: signForm.executionId as Id,
      status: signForm.status,
      result: signForm.result || undefined,
      dispenseRecordId: signForm.dispenseRecordId as unknown as Id
    })
    message.success('已签核')
    signOpen.value = false
    fetchData()
  } finally { saving.value = false }
}

fetchData()
searchElders('')
</script>

<style scoped>
.text-muted { color: #999; }
</style>
