<template>
  <PageContainer title="卡务管理" subTitle="开卡、挂失与状态管理">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="状态">
        <a-select v-model:value="query.status" :options="statusOptions" allow-clear style="width: 160px" />
      </a-form-item>
      <a-form-item label="关键词">
        <a-input v-model:value="query.keyword" placeholder="卡号/老人姓名" allow-clear />
      </a-form-item>
      <template #extra>
        <a-button type="primary" @click="openCreate">开卡</a-button>
      </template>
    </SearchForm>

    <DataTable
      rowKey="id"
      :columns="columns"
      :data-source="rows"
      :loading="loading"
      :pagination="pagination"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="record.status === 'ACTIVE' ? 'green' : record.status === 'FROZEN' ? 'orange' : 'red'">
            {{ statusLabel(record.status) }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'lossFlag'">
          <a-tag :color="record.lossFlag === 1 ? 'red' : 'blue'">{{ record.lossFlag === 1 ? '已挂失' : '正常' }}</a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="openEdit(record)">编辑</a-button>
            <a-button type="link" danger @click="remove(record)">删除</a-button>
          </a-space>
        </template>
      </template>
    </DataTable>

    <a-modal v-model:open="editOpen" :title="form.id ? '编辑卡务' : '开卡'" @ok="submit" :confirm-loading="saving" width="620px">
      <a-form layout="vertical">
        <a-form-item label="老人" required>
          <a-select
            v-model:value="form.elderId"
            show-search
            :disabled="!!form.id"
            :options="elderOptions"
            placeholder="请选择老人"
            option-filter-prop="label"
          />
        </a-form-item>
        <a-form-item label="卡号">
          <a-input v-model:value="form.cardNo" placeholder="不填则自动生成" />
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="状态">
              <a-select v-model:value="form.status" :options="statusOptions" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="挂失">
              <a-select v-model:value="form.lossFlag" :options="lossOptions" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="备注">
          <a-input v-model:value="form.remark" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { useElderOptions } from '../../composables/useElderOptions'
import { getCardAccountPage, createCardAccount, updateCardAccount, deleteCardAccount } from '../../api/card'
import type { CardAccount, PageResult } from '../../types'

const loading = ref(false)
const rows = ref<CardAccount[]>([])
const { elderOptions, searchElders, ensureSelectedElder } = useElderOptions({ pageSize: 200 })
const query = reactive({ status: undefined as string | undefined, keyword: '', pageNo: 1, pageSize: 10 })
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

const columns = [
  { title: '老人', dataIndex: 'elderName', key: 'elderName', width: 120 },
  { title: '卡号', dataIndex: 'cardNo', key: 'cardNo', width: 180 },
  { title: '余额', dataIndex: 'balance', key: 'balance', width: 100 },
  { title: '信用额度', dataIndex: 'creditLimit', key: 'creditLimit', width: 100 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '挂失', dataIndex: 'lossFlag', key: 'lossFlag', width: 100 },
  { title: '操作', key: 'action', width: 150 }
]

const statusOptions = [
  { label: '正常', value: 'ACTIVE' },
  { label: '冻结', value: 'FROZEN' },
  { label: '注销', value: 'CANCELLED' }
]
const lossOptions = [
  { label: '否', value: 0 },
  { label: '是', value: 1 }
]

const editOpen = ref(false)
const saving = ref(false)
const form = reactive({
  id: undefined as number | undefined,
  elderId: undefined as number | undefined,
  cardNo: '',
  status: 'ACTIVE',
  lossFlag: 0,
  remark: ''
})

function statusLabel(status?: string) {
  if (status === 'FROZEN') return '冻结'
  if (status === 'CANCELLED') return '注销'
  return '正常'
}

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<CardAccount> = await getCardAccountPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      status: query.status,
      keyword: query.keyword || undefined
    })
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
  query.status = undefined
  query.keyword = ''
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

function openCreate() {
  form.id = undefined
  form.elderId = undefined
  form.cardNo = ''
  form.status = 'ACTIVE'
  form.lossFlag = 0
  form.remark = ''
  editOpen.value = true
}

function openEdit(record: CardAccount) {
  ensureSelectedElder(record.elderId, record.elderName)
  form.id = record.id
  form.elderId = record.elderId
  form.cardNo = record.cardNo
  form.status = record.status || 'ACTIVE'
  form.lossFlag = record.lossFlag ?? 0
  form.remark = record.remark || ''
  editOpen.value = true
}

async function submit() {
  const payload = {
    elderId: form.elderId,
    cardNo: form.cardNo || undefined,
    status: form.status,
    lossFlag: form.lossFlag,
    remark: form.remark
  }
  saving.value = true
  try {
    if (form.id) {
      await updateCardAccount(form.id, payload)
    } else {
      await createCardAccount(payload)
    }
    editOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function remove(record: CardAccount) {
  await deleteCardAccount(record.id)
  fetchData()
}

searchElders('')
fetchData()
</script>
