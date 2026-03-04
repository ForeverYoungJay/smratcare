<template>
  <PageContainer title="证书到期提醒" subTitle="培训与发展 / 证书到期提醒">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键字">
        <a-input v-model:value="query.keyword" placeholder="证书名称/编号/机构" allow-clear />
      </a-form-item>
      <a-form-item label="预警天数">
        <a-input-number v-model:value="query.warningDays" :min="0" :max="365" style="width: 140px" />
      </a-form-item>
      <a-form-item label="包含已过期">
        <a-switch v-model:checked="query.includeExpired" />
      </a-form-item>
      <template #extra>
        <a-space>
          <a-button @click="onExportCsv">导出 CSV</a-button>
          <a-button @click="onExportExcel">导出 Excel</a-button>
        </a-space>
      </template>
    </SearchForm>

    <DataTable
      rowKey="id"
      :columns="columns"
      :data-source="rows"
      :loading="loading"
      :pagination="pagination"
      :row-class-name="rowClassName"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'remainingDays'">
          <a-tag v-if="(record.remainingDays || 0) < 0" color="red">已过期 {{ Math.abs(record.remainingDays || 0) }} 天</a-tag>
          <a-tag v-else-if="(record.remainingDays || 0) <= 7" color="volcano">{{ record.remainingDays }} 天</a-tag>
          <a-tag v-else-if="(record.remainingDays || 0) <= 30" color="orange">{{ record.remainingDays }} 天</a-tag>
          <span v-else>{{ record.remainingDays ?? '-' }} 天</span>
        </template>
      </template>
    </DataTable>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { getHrProfileCertificateReminderPage } from '../../api/hr'
import type { HrStaffCertificateItem, PageResult } from '../../types'
import { exportCsv, exportExcel } from '../../utils/export'
import { mapByDict } from './hrExportFields'

const query = reactive({
  keyword: undefined as string | undefined,
  warningDays: 30,
  includeExpired: false,
  pageNo: 1,
  pageSize: 10
})
const columns = [
  { title: '员工', dataIndex: 'staffName', key: 'staffName', width: 120 },
  { title: '证书名称', dataIndex: 'certificateName', key: 'certificateName', width: 160 },
  { title: '证书编号', dataIndex: 'certificateNo', key: 'certificateNo', width: 160 },
  { title: '颁发机构', dataIndex: 'issuingAuthority', key: 'issuingAuthority', width: 160 },
  { title: '到期日期', dataIndex: 'expiryDate', key: 'expiryDate', width: 120 },
  { title: '剩余天数', dataIndex: 'remainingDays', key: 'remainingDays', width: 120 }
]
const rows = ref<HrStaffCertificateItem[]>([])
const loading = ref(false)
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<HrStaffCertificateItem> = await getHrProfileCertificateReminderPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      keyword: query.keyword,
      warningDays: query.warningDays,
      includeExpired: query.includeExpired
    })
    rows.value = res.list || []
    pagination.total = res.total || rows.value.length
  } catch {
    rows.value = []
    pagination.total = 0
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
  query.keyword = undefined
  query.warningDays = 30
  query.includeExpired = false
  query.pageNo = 1
  query.pageSize = 10
  pagination.current = 1
  pagination.pageSize = 10
  fetchData()
}

function rowClassName(record: HrStaffCertificateItem) {
  const days = Number(record.remainingDays ?? 0)
  if (days < 0) return 'hr-row-danger'
  if (days <= 30) return 'hr-row-warning'
  return ''
}

const exportFields = [
  { key: 'staffName', label: '员工' },
  { key: 'certificateName', label: '证书名称' },
  { key: 'certificateNo', label: '证书编号' },
  { key: 'issuingAuthority', label: '颁发机构' },
  { key: 'expiryDate', label: '到期日期' },
  { key: 'remainingDays', label: '剩余天数' }
]

function onExportCsv() {
  exportCsv(mapByDict(rows.value as Record<string, any>[], exportFields), '证书到期提醒-当前筛选')
}

function onExportExcel() {
  exportExcel(mapByDict(rows.value as Record<string, any>[], exportFields), '证书到期提醒-当前筛选')
}

onMounted(fetchData)
</script>

<style scoped>
:deep(.hr-row-danger) {
  background: #fff1f0 !important;
}

:deep(.hr-row-warning) {
  background: #fffbe6 !important;
}
</style>
