<template>
  <PageContainer title="分区备餐" subTitle="维护厨房备餐分区和产能">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键词">
        <a-input v-model:value="query.keyword" placeholder="分区编码/名称/负责人" allow-clear />
      </a-form-item>
      <template #extra>
        <a-button type="primary" @click="openCreate">新增分区</a-button>
      </template>
    </SearchForm>

    <DataTable rowKey="id" :columns="columns" :data-source="rows" :loading="loading" :pagination="pagination" @change="handleTableChange">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="openEdit(record)">编辑</a-button>
            <a-button type="link" danger @click="remove(record)">删除</a-button>
          </a-space>
        </template>
      </template>
    </DataTable>

    <a-modal v-model:open="editOpen" title="备餐分区" :confirm-loading="saving" @ok="submit">
      <a-form layout="vertical">
        <a-form-item label="分区编码" required>
          <a-input v-model:value="form.zoneCode" />
        </a-form-item>
        <a-form-item label="分区名称" required>
          <a-input v-model:value="form.zoneName" />
        </a-form-item>
        <a-form-item label="厨房区域">
          <a-input v-model:value="form.kitchenArea" />
        </a-form-item>
        <a-form-item label="备餐能力(份/餐)">
          <a-input-number v-model:value="form.capacity" :min="0" style="width: 100%" />
        </a-form-item>
        <a-form-item label="负责人">
          <a-input v-model:value="form.managerName" />
        </a-form-item>
        <a-form-item label="备注">
          <a-input v-model:value="form.remark" />
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
import { getDiningPrepZonePage, createDiningPrepZone, updateDiningPrepZone, deleteDiningPrepZone } from '../../api/dining'
import type { DiningPrepZone, PageResult } from '../../types'

const loading = ref(false)
const rows = ref<DiningPrepZone[]>([])
const query = reactive({ keyword: '', pageNo: 1, pageSize: 10 })
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const editOpen = ref(false)
const saving = ref(false)
const form = reactive({
  id: undefined as number | undefined,
  zoneCode: '',
  zoneName: '',
  kitchenArea: '',
  capacity: undefined as number | undefined,
  managerName: '',
  remark: ''
})

const columns = [
  { title: '分区编码', dataIndex: 'zoneCode', key: 'zoneCode', width: 140 },
  { title: '分区名称', dataIndex: 'zoneName', key: 'zoneName', width: 140 },
  { title: '厨房区域', dataIndex: 'kitchenArea', key: 'kitchenArea', width: 160 },
  { title: '备餐能力', dataIndex: 'capacity', key: 'capacity', width: 120 },
  { title: '负责人', dataIndex: 'managerName', key: 'managerName', width: 120 },
  { title: '备注', dataIndex: 'remark', key: 'remark' },
  { title: '操作', key: 'action', width: 140 }
]

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<DiningPrepZone> = await getDiningPrepZonePage({ ...query })
    rows.value = res.list
    pagination.total = res.total || res.list.length
  } finally {
    loading.value = false
  }
}

function handleTableChange(pag: any) {
  query.pageNo = pag.current
  query.pageSize = pag.pageSize
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchData()
}

function onReset() {
  query.keyword = ''
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

function openCreate() {
  form.id = undefined
  form.zoneCode = ''
  form.zoneName = ''
  form.kitchenArea = ''
  form.capacity = undefined
  form.managerName = ''
  form.remark = ''
  editOpen.value = true
}

function openEdit(record: DiningPrepZone) {
  form.id = record.id
  form.zoneCode = record.zoneCode
  form.zoneName = record.zoneName
  form.kitchenArea = record.kitchenArea || ''
  form.capacity = record.capacity
  form.managerName = record.managerName || ''
  form.remark = record.remark || ''
  editOpen.value = true
}

async function submit() {
  if (!form.zoneCode || !form.zoneName) {
    message.error('请填写分区编码和名称')
    return
  }
  saving.value = true
  try {
    const payload = {
      zoneCode: form.zoneCode,
      zoneName: form.zoneName,
      kitchenArea: form.kitchenArea,
      capacity: form.capacity,
      managerName: form.managerName,
      remark: form.remark
    }
    if (form.id) {
      await updateDiningPrepZone(form.id, payload)
    } else {
      await createDiningPrepZone(payload)
    }
    editOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function remove(record: DiningPrepZone) {
  await deleteDiningPrepZone(record.id)
  fetchData()
}

fetchData()
</script>
