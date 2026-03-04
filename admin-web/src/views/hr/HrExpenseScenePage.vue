<template>
  <PageContainer :title="pageTitle" :subTitle="`费用与报销 / ${pageTitle}`">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键字">
        <a-input v-model:value="query.keyword" placeholder="标题/申请人/备注" allow-clear />
      </a-form-item>
      <a-form-item label="审批状态">
        <a-select v-model:value="query.status" allow-clear style="width: 180px" :options="statusOptions" />
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
    />
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { exportCsv, exportExcel } from '../../utils/export'
import { getHrSalarySubsidyPage, getHrSubsidyPage, getHrTrainingReimbursePage } from '../../api/hr'
import type { HrExpenseItem, PageResult } from '../../types'
import { HR_EXPENSE_EXPORT_FIELDS, mapByDict } from './hrExportFields'

const route = useRoute()

const query = reactive({
  keyword: undefined as string | undefined,
  status: undefined as string | undefined,
  pageNo: 1,
  pageSize: 10
})

const statusOptions = [
  { label: '待审批', value: 'PENDING' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已驳回', value: 'REJECTED' }
]

const columns = [
  { title: '标题', dataIndex: 'title', key: 'title', width: 240 },
  { title: '申请人', dataIndex: 'applicantName', key: 'applicantName', width: 120 },
  { title: '金额', dataIndex: 'amount', key: 'amount', width: 100 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 110 },
  { title: '申请开始时间', dataIndex: 'applyStartTime', key: 'applyStartTime', width: 180 },
  { title: '申请结束时间', dataIndex: 'applyEndTime', key: 'applyEndTime', width: 180 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '备注', dataIndex: 'remark', key: 'remark', width: 240 }
]

const rows = ref<HrExpenseItem[]>([])
const loading = ref(false)
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

const pageTitle = computed(() => {
  if (route.name === 'HrExpenseTrainingReimburse') return '外出培训费用报销'
  if (route.name === 'HrExpenseSubsidy') return '补贴申请'
  if (route.name === 'HrExpenseSalarySubsidy') return '工资补贴记录'
  return '费用记录'
})

const fetcher = computed(() => {
  if (route.name === 'HrExpenseTrainingReimburse') return getHrTrainingReimbursePage
  if (route.name === 'HrExpenseSalarySubsidy') return getHrSalarySubsidyPage
  return getHrSubsidyPage
})

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<HrExpenseItem> = await fetcher.value({ ...query })
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
  query.status = undefined
  query.pageNo = 1
  query.pageSize = 10
  pagination.current = 1
  pagination.pageSize = 10
  fetchData()
}

function onExportCsv() {
  exportCsv(mapByDict(rows.value as Record<string, any>[], HR_EXPENSE_EXPORT_FIELDS), `${pageTitle.value}-当前筛选`)
}

function onExportExcel() {
  exportExcel(mapByDict(rows.value as Record<string, any>[], HR_EXPENSE_EXPORT_FIELDS), `${pageTitle.value}-当前筛选`)
}

function rowClassName(record: HrExpenseItem) {
  if (record.status === 'REJECTED') return 'hr-row-danger'
  if (record.status === 'PENDING') return 'hr-row-warning'
  return ''
}

onMounted(fetchData)

watch(
  () => route.name,
  () => {
    onReset()
  }
)
</script>

<style scoped>
:deep(.hr-row-danger) {
  background: #fff1f0 !important;
}

:deep(.hr-row-warning) {
  background: #fffbe6 !important;
}
</style>
