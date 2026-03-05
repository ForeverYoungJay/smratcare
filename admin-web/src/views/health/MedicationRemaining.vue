<template>
  <PageContainer title="剩余用药" subTitle="按老人和药品统计剩余数量与预警">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键词">
        <a-input v-model:value="query.keyword" placeholder="老人/药品" allow-clear />
      </a-form-item>
      <template #extra>
        <a-space>
          <a-button @click="exportCsvData" :loading="exporting">导出CSV</a-button>
          <a-button @click="exportExcelData" :loading="exporting">导出Excel</a-button>
        </a-space>
      </template>
    </SearchForm>

    <DataTable
      :row-key="resolveRowKey"
      :columns="columns"
      :data-source="rows"
      :loading="loading"
      :pagination="pagination"
      :row-class-name="resolveRowClassName"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'lowStock'">
          <a-tag :color="record.lowStock ? 'red' : 'green'">{{ record.lowStock ? '预警' : '正常' }}</a-tag>
        </template>
      </template>
    </DataTable>
  </PageContainer>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { getHealthMedicationRemainingPage } from '../../api/health'
import type { HealthMedicationRemainingItem, PageResult } from '../../types'
import { mapHealthExportRows, medicationRemainingExportColumns } from '../../constants/healthExport'
import { exportCsv, exportExcel } from '../../utils/export'
import { resolveHealthError } from './healthError'

const loading = ref(false)
const exporting = ref(false)
const route = useRoute()
const rows = ref<HealthMedicationRemainingItem[]>([])
const query = reactive({ keyword: '', pageNo: 1, pageSize: 10 })
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

const columns = [
  { title: '老人', dataIndex: 'elderName', key: 'elderName', width: 120 },
  { title: '药品', dataIndex: 'drugName', key: 'drugName', width: 160 },
  { title: '缴存总量', dataIndex: 'depositQty', key: 'depositQty', width: 110 },
  { title: '已用总量', dataIndex: 'usedQty', key: 'usedQty', width: 110 },
  { title: '剩余用量', dataIndex: 'remainQty', key: 'remainQty', width: 110 },
  { title: '最小阈值', dataIndex: 'minRemainQty', key: 'minRemainQty', width: 110 },
  { title: '状态', key: 'lowStock', width: 100 }
]

async function fetchData() {
  loading.value = true
  try {
    const residentId = route.query.residentId ?? route.query.elderId
    const res: PageResult<HealthMedicationRemainingItem> = await getHealthMedicationRemainingPage({
      ...query,
      elderId: residentId ? Number(residentId) : undefined
    })
    rows.value = res.list
    pagination.total = res.total || res.list.length
  } catch (error) {
    message.error(resolveHealthError(error, '加载剩余用药失败'))
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
  query.keyword = ''
  query.pageNo = 1
  query.pageSize = pagination.pageSize
  pagination.current = 1
  fetchData()
}

function resolveRowClassName(record: HealthMedicationRemainingItem) {
  return Number(record.lowStock) === 1 || Number(record.remainQty) <= Number(record.minRemainQty || 0) ? 'row-warning' : ''
}

function resolveRowKey(record: HealthMedicationRemainingItem) {
  const elderPart = record.elderId ?? record.elderName ?? 'unknown'
  const drugPart = record.drugId ?? record.drugName ?? 'unknown'
  return `${elderPart}_${drugPart}`
}

async function exportData(type: 'csv' | 'excel') {
  exporting.value = true
  try {
    const residentId = route.query.residentId ?? route.query.elderId
    const res: PageResult<HealthMedicationRemainingItem> = await getHealthMedicationRemainingPage({
      keyword: query.keyword || undefined,
      pageNo: 1,
      pageSize: 1000,
      elderId: residentId ? Number(residentId) : undefined
    })
    const list = res.list || []
    if (!list.length) {
      message.warning('暂无可导出数据')
      return
    }
    const rowsForExport = mapHealthExportRows(list, medicationRemainingExportColumns)
    if (type === 'csv') {
      exportCsv(rowsForExport, `剩余用药_${Date.now()}.csv`)
      message.success('CSV导出成功')
    } else {
      exportExcel(rowsForExport, `剩余用药_${Date.now()}.xlsx`)
      message.success('Excel导出成功')
    }
  } catch (error) {
    message.error(resolveHealthError(error, '加载导出数据失败'))
  } finally {
    exporting.value = false
  }
}

function exportCsvData() {
  exportData('csv')
}

function exportExcelData() {
  exportData('excel')
}

fetchData()
</script>

<style scoped>
:deep(.row-warning > td) {
  background: #fff7e6 !important;
}
</style>
