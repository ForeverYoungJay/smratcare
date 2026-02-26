<template>
  <PageContainer title="药品缴存" subTitle="登记老人药品缴存情况">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键词">
        <a-input v-model:value="query.keyword" placeholder="老人/药品" allow-clear />
      </a-form-item>
      <template #extra>
        <a-button type="primary" @click="openCreate">新增缴存</a-button>
      </template>
    </SearchForm>

    <DataTable rowKey="id" :columns="columns" :data-source="rows" :loading="loading" :pagination="pagination" @change="handleTableChange">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="openEdit(record)">编辑</a-button>
            <a-popconfirm title="确认删除该记录吗？" ok-text="确认" cancel-text="取消" @confirm="remove(record)">
              <a-button type="link" danger>删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </DataTable>

    <a-modal v-model:open="editOpen" title="药品缴存" @ok="submit" :confirm-loading="saving" width="680px">
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
          <a-col :span="12"><a-form-item label="药品名称" required><a-input v-model:value="form.drugName" /></a-form-item></a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12"><a-form-item label="缴存日期" required><a-date-picker v-model:value="form.depositDate" style="width: 100%" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="到期日期"><a-date-picker v-model:value="form.expireDate" style="width: 100%" /></a-form-item></a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12"><a-form-item label="数量" required><a-input-number v-model:value="form.quantity" :min="0" style="width: 100%" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="单位"><a-input v-model:value="form.unit" /></a-form-item></a-col>
        </a-row>
        <a-form-item label="缴存人"><a-input v-model:value="form.depositorName" /></a-form-item>
        <a-form-item label="备注"><a-input v-model:value="form.remark" /></a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { useElderOptions } from '../../composables/useElderOptions'
import {
  getHealthMedicationDepositPage,
  createHealthMedicationDeposit,
  updateHealthMedicationDeposit,
  deleteHealthMedicationDeposit
} from '../../api/health'
import type { HealthMedicationDeposit, PageResult } from '../../types'

const loading = ref(false)
const rows = ref<HealthMedicationDeposit[]>([])
const query = reactive({ keyword: '', pageNo: 1, pageSize: 10 })
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

const columns = [
  { title: '老人', dataIndex: 'elderName', key: 'elderName', width: 120 },
  { title: '药品', dataIndex: 'drugName', key: 'drugName', width: 160 },
  { title: '缴存日期', dataIndex: 'depositDate', key: 'depositDate', width: 120 },
  { title: '数量', dataIndex: 'quantity', key: 'quantity', width: 100 },
  { title: '单位', dataIndex: 'unit', key: 'unit', width: 80 },
  { title: '到期日期', dataIndex: 'expireDate', key: 'expireDate', width: 120 },
  { title: '缴存人', dataIndex: 'depositorName', key: 'depositorName', width: 120 },
  { title: '备注', dataIndex: 'remark', key: 'remark' },
  { title: '操作', key: 'action', width: 120 }
]

const editOpen = ref(false)
const saving = ref(false)
const { elderOptions, searchElders, findElderName, ensureSelectedElder } = useElderOptions({ pageSize: 50 })
const form = reactive({
  id: undefined as number | undefined,
  elderId: undefined as number | undefined,
  elderName: '',
  drugName: '',
  depositDate: dayjs(),
  quantity: 0 as number | undefined,
  unit: '',
  expireDate: undefined as any,
  depositorName: '',
  remark: ''
})

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<HealthMedicationDeposit> = await getHealthMedicationDepositPage(query)
    rows.value = res.list
    pagination.total = res.total || res.list.length
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
  query.pageNo = 1
  query.pageSize = pagination.pageSize
  pagination.current = 1
  fetchData()
}

function openCreate() {
  form.id = undefined
  form.elderId = undefined
  form.elderName = ''
  form.drugName = ''
  form.depositDate = dayjs()
  form.quantity = 0
  form.unit = ''
  form.expireDate = undefined
  form.depositorName = ''
  form.remark = ''
  editOpen.value = true
}

function openEdit(record: HealthMedicationDeposit) {
  form.id = record.id
  form.elderId = record.elderId
  form.elderName = record.elderName || ''
  ensureSelectedElder(record.elderId, record.elderName)
  form.drugName = record.drugName
  form.depositDate = record.depositDate ? dayjs(record.depositDate) : dayjs()
  form.quantity = record.quantity
  form.unit = record.unit || ''
  form.expireDate = record.expireDate ? dayjs(record.expireDate) : undefined
  form.depositorName = record.depositorName || ''
  form.remark = record.remark || ''
  editOpen.value = true
}

function onElderChange(elderId?: number) {
  form.elderName = findElderName(elderId)
}

async function submit() {
  if (!form.elderId || !form.drugName) {
    message.error('请填写老人和药品')
    return
  }
  if (!form.quantity || form.quantity <= 0) {
    message.error('缴存数量必须大于0')
    return
  }
  if (form.expireDate && dayjs(form.expireDate).isBefore(dayjs(form.depositDate), 'day')) {
    message.error('到期日期不能早于缴存日期')
    return
  }
  saving.value = true
  try {
    const payload = {
      elderId: form.elderId,
      elderName: findElderName(form.elderId) || form.elderName,
      drugName: form.drugName,
      depositDate: dayjs(form.depositDate).format('YYYY-MM-DD'),
      quantity: form.quantity,
      unit: form.unit,
      expireDate: form.expireDate ? dayjs(form.expireDate).format('YYYY-MM-DD') : undefined,
      depositorName: form.depositorName,
      remark: form.remark
    }
    if (form.id) {
      await updateHealthMedicationDeposit(form.id, payload)
    } else {
      await createHealthMedicationDeposit(payload)
    }
    message.success('保存成功')
    editOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function remove(record: HealthMedicationDeposit) {
  await deleteHealthMedicationDeposit(record.id)
  message.success('删除成功')
  fetchData()
}

fetchData()
searchElders('')
</script>
