<template>
  <PageContainer title="医嘱管理" subTitle="长期/临时医嘱开立与停嘱（长期医嘱按频次自动生成执行单）">
    <SearchForm :model="query" @search="onSearch" @reset="onReset">
      <a-form-item label="长者">
        <a-select v-model:value="query.elderId" show-search allow-clear :filter-option="false" :options="elderOptions"
          placeholder="输入姓名搜索" style="width: 200px" @search="searchElders" @focus="() => !elderOptions.length && searchElders('')" />
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="query.status" allow-clear style="width: 130px" placeholder="全部">
          <a-select-option value="ACTIVE">执行中</a-select-option>
          <a-select-option value="STOPPED">已停嘱</a-select-option>
          <a-select-option value="COMPLETED">已完成</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="类别">
        <a-select v-model:value="query.category" allow-clear style="width: 130px" placeholder="全部">
          <a-select-option v-for="c in categoryOptions" :key="c.value" :value="c.value">{{ c.label }}</a-select-option>
        </a-select>
      </a-form-item>
      <template #extra>
        <a-button type="primary" @click="openCreate">开立医嘱</a-button>
      </template>
    </SearchForm>

    <DataTable rowKey="id" :columns="columns" :data-source="rows" :loading="loading" :pagination="pagination" @change="handleTableChange">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'orderType'">
          <a-tag :color="record.orderType === 'TEMPORARY' ? 'gold' : 'blue'">{{ record.orderType === 'TEMPORARY' ? '临时' : '长期' }}</a-tag>
        </template>
        <template v-else-if="column.key === 'category'">{{ categoryText(record.category) }}</template>
        <template v-else-if="column.key === 'status'">
          <a-tag :color="statusColor(record.status)">{{ statusText(record.status) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-button v-if="record.status === 'ACTIVE'" type="link" size="small" danger @click="stop(record)">停嘱</a-button>
          <span v-else class="text-muted">—</span>
        </template>
      </template>
    </DataTable>

    <a-modal v-model:open="createOpen" title="开立医嘱" :confirm-loading="saving" width="720px" @ok="submit">
      <a-form layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="长者" required>
              <a-select v-model:value="form.elderId" show-search :filter-option="false" :options="elderOptions"
                placeholder="输入姓名搜索" @search="searchElders" @focus="() => !elderOptions.length && searchElders('')" />
            </a-form-item>
          </a-col>
          <a-col :span="6"><a-form-item label="医嘱类型"><a-select v-model:value="form.orderType" :options="typeOptions" /></a-form-item></a-col>
          <a-col :span="6"><a-form-item label="类别"><a-select v-model:value="form.category" :options="categoryOptions" /></a-form-item></a-col>
        </a-row>
        <a-form-item label="医嘱内容" required><a-input v-model:value="form.content" placeholder="如 氨氯地平片 5mg 口服" /></a-form-item>
        <a-row :gutter="16">
          <a-col :span="6"><a-form-item label="药品ID"><a-input-number v-model:value="form.drugId" style="width:100%" placeholder="药疗选填" /></a-form-item></a-col>
          <a-col :span="6"><a-form-item label="剂量"><a-input v-model:value="form.dosage" placeholder="如 5mg" /></a-form-item></a-col>
          <a-col :span="6">
            <a-form-item label="频次">
              <a-select v-model:value="form.frequency" allow-clear :options="freqOptions" placeholder="长期医嘱必填" />
            </a-form-item>
          </a-col>
          <a-col :span="6"><a-form-item label="途径"><a-input v-model:value="form.route" placeholder="如 口服/静滴" /></a-form-item></a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="8"><a-form-item label="每次数量"><a-input-number v-model:value="form.quantityPerTime" :min="0" style="width:100%" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="开始时间"><a-date-picker v-model:value="form.startTime" show-time value-format="YYYY-MM-DDTHH:mm:ss" style="width:100%" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="优先级"><a-input v-model:value="form.priority" placeholder="如 常规/加急" /></a-form-item></a-col>
        </a-row>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { useElderOptions } from '../../composables/useElderOptions'
import { getMedicalOrderPage, createMedicalOrder, stopMedicalOrder } from '../../api/medOrder'
import type { Id, MedicalOrder, PageResult } from '../../types'

const typeOptions = [
  { label: '长期', value: 'LONG_TERM' },
  { label: '临时', value: 'TEMPORARY' }
]
const categoryOptions = [
  { label: '药疗', value: 'DRUG' },
  { label: '护理', value: 'NURSING' },
  { label: '检查', value: 'EXAM' },
  { label: '饮食', value: 'DIET' }
]
const freqOptions = ['QD', 'BID', 'TID', 'QID', 'Q8H', 'Q12H', 'QOD', 'PRN', 'ST'].map((v) => ({ label: v, value: v }))

const loading = ref(false)
const saving = ref(false)
const rows = ref<MedicalOrder[]>([])
const query = reactive({ elderId: undefined as Id | undefined, status: undefined as string | undefined, category: undefined as string | undefined, pageNo: 1, pageSize: 10 })
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const { elderOptions, searchElders } = useElderOptions({ pageSize: 50 })

const columns = [
  { title: '长者ID', dataIndex: 'elderId', key: 'elderId', width: 100 },
  { title: '类型', key: 'orderType', width: 80 },
  { title: '类别', key: 'category', width: 80 },
  { title: '医嘱内容', dataIndex: 'content', key: 'content' },
  { title: '频次', dataIndex: 'frequency', key: 'frequency', width: 80 },
  { title: '途径', dataIndex: 'route', key: 'route', width: 90 },
  { title: '状态', key: 'status', width: 90 },
  { title: '操作', key: 'action', width: 90 }
]

const createOpen = ref(false)
const form = reactive<Partial<MedicalOrder>>({})

function categoryText(c?: string) { return categoryOptions.find((o) => o.value === c)?.label || c || '-' }
function statusText(s?: string) { return ({ ACTIVE: '执行中', STOPPED: '已停嘱', COMPLETED: '已完成' } as Record<string, string>)[s || ''] || s || '-' }
function statusColor(s?: string) { return s === 'ACTIVE' ? 'green' : s === 'STOPPED' ? 'default' : 'blue' }

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<MedicalOrder> = await getMedicalOrderPage({ pageNo: query.pageNo, pageSize: query.pageSize, elderId: query.elderId, status: query.status, category: query.category })
    rows.value = res.list
    pagination.total = res.total || res.list.length
  } finally { loading.value = false }
}
function onSearch() { query.pageNo = 1; pagination.current = 1; fetchData() }
function onReset() { query.elderId = undefined; query.status = undefined; query.category = undefined; query.pageNo = 1; pagination.current = 1; fetchData() }
function handleTableChange(p: any) { pagination.current = p.current; pagination.pageSize = p.pageSize; query.pageNo = p.current; query.pageSize = p.pageSize; fetchData() }

function openCreate() {
  Object.assign(form, { elderId: undefined, orderType: 'LONG_TERM', category: 'DRUG', content: '', drugId: undefined, dosage: '', frequency: 'BID', route: '', quantityPerTime: undefined, startTime: undefined, priority: '' })
  createOpen.value = true
}
async function submit() {
  if (!form.elderId || !form.content) { message.error('请填写长者与医嘱内容'); return }
  saving.value = true
  try {
    await createMedicalOrder({ ...form })
    message.success('医嘱已开立')
    createOpen.value = false
    fetchData()
  } finally { saving.value = false }
}
function stop(record: MedicalOrder) {
  Modal.confirm({
    title: '确认停嘱？',
    onOk: async () => { await stopMedicalOrder(record.id); message.success('已停嘱'); fetchData() }
  })
}

fetchData()
searchElders('')
</script>

<style scoped>
.text-muted { color: #999; }
</style>
