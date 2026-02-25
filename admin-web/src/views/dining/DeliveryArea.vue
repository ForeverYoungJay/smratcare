<template>
  <PageContainer title="送餐区域" subTitle="维护送餐覆盖区域和责任人">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键词">
        <a-input v-model:value="query.keyword" placeholder="区域编码/名称/负责人" allow-clear />
      </a-form-item>
      <template #extra>
        <a-button type="primary" @click="openCreate">新增区域</a-button>
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

    <a-modal v-model:open="editOpen" title="送餐区域" :confirm-loading="saving" @ok="submit">
      <a-form layout="vertical">
        <a-form-item label="区域编码" required>
          <a-input v-model:value="form.areaCode" />
        </a-form-item>
        <a-form-item label="区域名称" required>
          <a-input v-model:value="form.areaName" />
        </a-form-item>
        <a-form-item label="楼栋">
          <a-input v-model:value="form.buildingName" />
        </a-form-item>
        <a-form-item label="楼层">
          <a-input v-model:value="form.floorNo" />
        </a-form-item>
        <a-form-item label="覆盖房间">
          <a-input v-model:value="form.roomScope" placeholder="如：301-315" />
        </a-form-item>
        <a-form-item label="负责人">
          <a-input v-model:value="form.managerName" />
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
import { getDiningDeliveryAreaPage, createDiningDeliveryArea, updateDiningDeliveryArea, deleteDiningDeliveryArea } from '../../api/dining'
import type { DiningDeliveryArea, PageResult } from '../../types'

const loading = ref(false)
const rows = ref<DiningDeliveryArea[]>([])
const query = reactive({ keyword: '', pageNo: 1, pageSize: 10 })
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const editOpen = ref(false)
const saving = ref(false)
const form = reactive({
  id: undefined as number | undefined,
  areaCode: '',
  areaName: '',
  buildingName: '',
  floorNo: '',
  roomScope: '',
  managerName: ''
})

const columns = [
  { title: '区域编码', dataIndex: 'areaCode', key: 'areaCode', width: 130 },
  { title: '区域名称', dataIndex: 'areaName', key: 'areaName', width: 130 },
  { title: '楼栋', dataIndex: 'buildingName', key: 'buildingName', width: 120 },
  { title: '楼层', dataIndex: 'floorNo', key: 'floorNo', width: 100 },
  { title: '覆盖房间', dataIndex: 'roomScope', key: 'roomScope', width: 150 },
  { title: '负责人', dataIndex: 'managerName', key: 'managerName', width: 120 },
  { title: '操作', key: 'action', width: 140 }
]

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<DiningDeliveryArea> = await getDiningDeliveryAreaPage({ ...query })
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
  form.areaCode = ''
  form.areaName = ''
  form.buildingName = ''
  form.floorNo = ''
  form.roomScope = ''
  form.managerName = ''
  editOpen.value = true
}

function openEdit(record: DiningDeliveryArea) {
  form.id = record.id
  form.areaCode = record.areaCode
  form.areaName = record.areaName
  form.buildingName = record.buildingName || ''
  form.floorNo = record.floorNo || ''
  form.roomScope = record.roomScope || ''
  form.managerName = record.managerName || ''
  editOpen.value = true
}

async function submit() {
  if (!form.areaCode || !form.areaName) {
    message.error('请填写区域编码和名称')
    return
  }
  saving.value = true
  try {
    const payload = {
      areaCode: form.areaCode,
      areaName: form.areaName,
      buildingName: form.buildingName,
      floorNo: form.floorNo,
      roomScope: form.roomScope,
      managerName: form.managerName
    }
    if (form.id) {
      await updateDiningDeliveryArea(form.id, payload)
    } else {
      await createDiningDeliveryArea(payload)
    }
    editOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function remove(record: DiningDeliveryArea) {
  await deleteDiningDeliveryArea(record.id)
  fetchData()
}

fetchData()
</script>
