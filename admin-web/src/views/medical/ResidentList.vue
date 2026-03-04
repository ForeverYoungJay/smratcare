<template>
  <PageContainer title="长者患者列表" subTitle="护理/医护共同入口">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="长者">
        <a-input v-model:value="query.fullName" allow-clear placeholder="姓名/手机号/身份证" />
      </a-form-item>
      <a-form-item label="床位">
        <a-input v-model:value="query.bedNo" allow-clear placeholder="床位号" />
      </a-form-item>
      <a-form-item label="护理等级">
        <a-input v-model:value="query.careLevel" allow-clear placeholder="护理等级" />
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="query.status" allow-clear style="width: 140px">
          <a-select-option :value="1">在院</a-select-option>
          <a-select-option :value="2">请假</a-select-option>
          <a-select-option :value="3">离院</a-select-option>
        </a-select>
      </a-form-item>
      <template #extra>
        <a-space>
          <a-button @click="go('/medical-care/center')">服务中心</a-button>
          <a-button type="primary" @click="fetchData">刷新</a-button>
        </a-space>
      </template>
    </SearchForm>

    <DataTable rowKey="id" :columns="columns" :data-source="rows" :loading="loading" :pagination="pagination" @change="onTableChange">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="statusColor(record.status)">{{ statusLabel(record.status) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="go(`/elder/resident-360?residentId=${record.id}&from=medicalCare`)">长者总览</a-button>
            <a-button type="link" @click="go(`/medical-care/assessment/tcm?residentId=${record.id}&from=residentList`)">中医评估</a-button>
            <a-button type="link" @click="go(`/medical-care/assessment/cvd?residentId=${record.id}&from=residentList`)">心血管评估</a-button>
            <a-button type="link" @click="go(`/medical-care/inspection?residentId=${record.id}&from=residentList`)">健康巡检</a-button>
          </a-space>
        </template>
      </template>
    </DataTable>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { getElderPage } from '../../api/elder'
import type { ElderItem, PageResult } from '../../types'

const router = useRouter()
const loading = ref(false)
const rows = ref<ElderItem[]>([])
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const query = reactive({
  fullName: '',
  bedNo: '',
  careLevel: '',
  status: undefined as number | undefined,
  pageNo: 1,
  pageSize: 10
})

const columns = [
  { title: '长者', dataIndex: 'fullName', key: 'fullName', width: 120 },
  { title: '床位', dataIndex: 'bedNo', key: 'bedNo', width: 110 },
  { title: '护理等级', dataIndex: 'careLevel', key: 'careLevel', width: 120 },
  { title: '联系电话', dataIndex: 'phone', key: 'phone', width: 140 },
  { title: '入住日期', dataIndex: 'admissionDate', key: 'admissionDate', width: 120 },
  { title: '状态', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 360 }
]

function buildParams() {
  return {
    fullName: query.fullName || undefined,
    bedNo: query.bedNo || undefined,
    careLevel: query.careLevel || undefined,
    status: query.status,
    pageNo: query.pageNo,
    pageSize: query.pageSize
  }
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getElderPage(buildParams()) as PageResult<ElderItem>
    rows.value = res.list || []
    pagination.total = res.total || 0
  } finally {
    loading.value = false
  }
}

function onTableChange(pag: any) {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  query.pageNo = pag.current
  query.pageSize = pag.pageSize
  fetchData()
}

function onReset() {
  query.fullName = ''
  query.bedNo = ''
  query.careLevel = ''
  query.status = undefined
  query.pageNo = 1
  query.pageSize = pagination.pageSize
  pagination.current = 1
  fetchData()
}

function statusLabel(status?: number) {
  if (status === 1) return '在院'
  if (status === 2) return '请假'
  if (status === 3) return '离院'
  return '-'
}

function statusColor(status?: number) {
  if (status === 1) return 'green'
  if (status === 2) return 'orange'
  if (status === 3) return 'red'
  return 'default'
}

function go(path: string) {
  router.push(path)
}

onMounted(fetchData)
</script>
