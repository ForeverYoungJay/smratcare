<template>
  <PageContainer title="电子病历" subTitle="入院/病程/诊断/检查记录">
    <SearchForm :model="query" @search="onSearch" @reset="onReset">
      <a-form-item label="长者">
        <a-select v-model:value="query.elderId" show-search allow-clear :filter-option="false" :options="elderOptions"
          placeholder="输入姓名搜索" style="width: 200px" @search="searchElders" @focus="() => !elderOptions.length && searchElders('')" />
      </a-form-item>
      <a-form-item label="类型">
        <a-select v-model:value="query.recordType" allow-clear style="width: 140px" placeholder="全部">
          <a-select-option v-for="t in typeOptions" :key="t.value" :value="t.value">{{ t.label }}</a-select-option>
        </a-select>
      </a-form-item>
      <template #extra>
        <a-button type="primary" @click="openCreate">新建病历</a-button>
      </template>
    </SearchForm>

    <DataTable rowKey="id" :columns="columns" :data-source="rows" :loading="loading" :pagination="pagination" @change="handleTableChange">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'recordType'">
          <a-tag>{{ typeText(record.recordType) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <div class="row-action-links">
            <a-button type="link" size="small" @click="openDetail(record)">详情</a-button>
            <a-button type="link" size="small" @click="openEdit(record)">编辑</a-button>
          </div>
        </template>
      </template>
    </DataTable>

    <a-modal v-model:open="editOpen" :title="form.id ? '编辑病历' : '新建病历'" :confirm-loading="saving" width="720px" @ok="submit">
      <a-form layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="长者" required>
              <a-select v-model:value="form.elderId" show-search :filter-option="false" :options="elderOptions"
                placeholder="输入姓名搜索" @search="searchElders" @focus="() => !elderOptions.length && searchElders('')" />
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item label="类型"><a-select v-model:value="form.recordType" :options="typeOptions" /></a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item label="就诊日期"><a-date-picker v-model:value="form.visitDate" value-format="YYYY-MM-DD" style="width:100%" /></a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="主诉"><a-textarea v-model:value="form.chiefComplaint" :rows="2" /></a-form-item>
        <a-form-item label="现病史"><a-textarea v-model:value="form.presentIllness" :rows="2" /></a-form-item>
        <a-form-item label="既往史"><a-textarea v-model:value="form.pastHistory" :rows="2" /></a-form-item>
        <a-form-item label="体格检查"><a-textarea v-model:value="form.physicalExam" :rows="2" /></a-form-item>
        <a-form-item label="诊断"><a-textarea v-model:value="form.diagnosis" :rows="2" /></a-form-item>
        <a-form-item label="诊疗计划"><a-textarea v-model:value="form.treatmentPlan" :rows="2" /></a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="detailOpen" title="病历详情" :footer="null" width="680px">
      <a-descriptions v-if="current" bordered :column="1" size="small">
        <a-descriptions-item label="类型">{{ typeText(current.recordType) }}</a-descriptions-item>
        <a-descriptions-item label="就诊日期">{{ current.visitDate || '-' }}</a-descriptions-item>
        <a-descriptions-item label="主诉">{{ current.chiefComplaint || '-' }}</a-descriptions-item>
        <a-descriptions-item label="现病史">{{ current.presentIllness || '-' }}</a-descriptions-item>
        <a-descriptions-item label="既往史">{{ current.pastHistory || '-' }}</a-descriptions-item>
        <a-descriptions-item label="体格检查">{{ current.physicalExam || '-' }}</a-descriptions-item>
        <a-descriptions-item label="诊断">{{ current.diagnosis || '-' }}</a-descriptions-item>
        <a-descriptions-item label="诊疗计划">{{ current.treatmentPlan || '-' }}</a-descriptions-item>
        <a-descriptions-item label="医生">{{ current.doctorName || '-' }}</a-descriptions-item>
      </a-descriptions>
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
import { getEmrPage, saveEmr } from '../../api/emr'
import type { EmrRecord, Id, PageResult } from '../../types'

const typeOptions = [
  { label: '入院', value: 'ADMISSION' },
  { label: '病程', value: 'PROGRESS' },
  { label: '诊断', value: 'DIAGNOSIS' },
  { label: '检查', value: 'EXAM' }
]

const loading = ref(false)
const saving = ref(false)
const rows = ref<EmrRecord[]>([])
const query = reactive({ elderId: undefined as Id | undefined, recordType: undefined as string | undefined, pageNo: 1, pageSize: 10 })
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const { elderOptions, searchElders } = useElderOptions({ pageSize: 50 })

const columns = [
  { title: '长者ID', dataIndex: 'elderId', key: 'elderId', width: 100 },
  { title: '类型', key: 'recordType', width: 90 },
  { title: '就诊日期', dataIndex: 'visitDate', key: 'visitDate', width: 120 },
  { title: '主诉', dataIndex: 'chiefComplaint', key: 'chiefComplaint', ellipsis: true },
  { title: '诊断', dataIndex: 'diagnosis', key: 'diagnosis', ellipsis: true },
  { title: '医生', dataIndex: 'doctorName', key: 'doctorName', width: 100 },
  { title: '操作', key: 'action', width: 130 }
]

const editOpen = ref(false)
const detailOpen = ref(false)
const current = ref<EmrRecord | null>(null)
const form = reactive<Partial<EmrRecord>>({})

function typeText(t?: string) { return typeOptions.find((o) => o.value === t)?.label || t || '-' }

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<EmrRecord> = await getEmrPage({ pageNo: query.pageNo, pageSize: query.pageSize, elderId: query.elderId, recordType: query.recordType })
    rows.value = res.list
    pagination.total = res.total || res.list.length
  } finally { loading.value = false }
}
function onSearch() { query.pageNo = 1; pagination.current = 1; fetchData() }
function onReset() { query.elderId = undefined; query.recordType = undefined; query.pageNo = 1; pagination.current = 1; fetchData() }
function handleTableChange(p: any) { pagination.current = p.current; pagination.pageSize = p.pageSize; query.pageNo = p.current; query.pageSize = p.pageSize; fetchData() }

function openCreate() {
  Object.assign(form, { id: undefined, elderId: undefined, recordType: 'PROGRESS', visitDate: undefined, chiefComplaint: '', presentIllness: '', pastHistory: '', physicalExam: '', diagnosis: '', treatmentPlan: '' })
  editOpen.value = true
}
function openEdit(record: EmrRecord) { Object.assign(form, record); editOpen.value = true }
function openDetail(record: EmrRecord) { current.value = record; detailOpen.value = true }

async function submit() {
  if (saving.value) return
  if (!form.elderId) { message.error('请选择长者'); return }
  saving.value = true
  try {
    await saveEmr({ ...form })
    message.success('已保存')
    editOpen.value = false
    fetchData()
  } finally { saving.value = false }
}

fetchData()
searchElders('')
</script>
