<template>
  <PageContainer title="药品字典" subTitle="维护药品基础信息">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键词">
        <a-input v-model:value="query.keyword" placeholder="药品名称/编码/分类" allow-clear />
      </a-form-item>
      <template #extra>
        <a-space>
          <a-button @click="exportCsvData" :loading="exporting">导出CSV</a-button>
          <a-button @click="exportExcelData" :loading="exporting">导出Excel</a-button>
          <a-button type="primary" @click="openCreate">新增药品</a-button>
        </a-space>
      </template>
    </SearchForm>

    <DataTable
      rowKey="id"
      :columns="columns"
      :data-source="rows"
      :loading="loading"
      :pagination="pagination"
      :row-class-name="resolveRowClassName"
      @change="handleTableChange"
    >
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

    <a-modal v-model:open="editOpen" title="药品信息" @ok="submit" :confirm-loading="saving">
      <a-form layout="vertical">
        <a-form-item label="药品编码">
          <a-input v-model:value="form.drugCode" />
        </a-form-item>
        <a-form-item label="药品名称" required>
          <a-input v-model:value="form.drugName" />
        </a-form-item>
        <a-form-item label="规格">
          <a-input v-model:value="form.specification" />
        </a-form-item>
        <a-form-item label="单位">
          <a-input v-model:value="form.unit" />
        </a-form-item>
        <a-form-item label="生产厂家">
          <a-input v-model:value="form.manufacturer" />
        </a-form-item>
        <a-form-item label="分类">
          <a-input v-model:value="form.category" />
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
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { mapHealthExportRows, drugDictionaryExportColumns } from '../../constants/healthExport'
import { exportCsv, exportExcel } from '../../utils/export'
import { resolveHealthError } from './healthError'
import {
  getHealthDrugDictionaryPage,
  createHealthDrugDictionary,
  updateHealthDrugDictionary,
  deleteHealthDrugDictionary
} from '../../api/health'
import type { HealthDrugDictionary, PageResult } from '../../types'

const loading = ref(false)
const exporting = ref(false)
const rows = ref<HealthDrugDictionary[]>([])
const query = reactive({ keyword: '', pageNo: 1, pageSize: 10 })
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

const columns = [
  { title: '编码', dataIndex: 'drugCode', key: 'drugCode', width: 140 },
  { title: '名称', dataIndex: 'drugName', key: 'drugName', width: 180 },
  { title: '规格', dataIndex: 'specification', key: 'specification', width: 140 },
  { title: '单位', dataIndex: 'unit', key: 'unit', width: 90 },
  { title: '厂家', dataIndex: 'manufacturer', key: 'manufacturer', width: 180 },
  { title: '分类', dataIndex: 'category', key: 'category', width: 120 },
  { title: '备注', dataIndex: 'remark', key: 'remark' },
  { title: '操作', key: 'action', width: 120 }
]

const editOpen = ref(false)
const saving = ref(false)
const form = reactive({
  id: undefined as number | undefined,
  drugCode: '',
  drugName: '',
  specification: '',
  unit: '',
  manufacturer: '',
  category: '',
  remark: ''
})

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<HealthDrugDictionary> = await getHealthDrugDictionaryPage(query)
    rows.value = res.list
    pagination.total = res.total || res.list.length
  } catch (error) {
    message.error(resolveHealthError(error, '加载药品字典失败'))
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

function openCreate() {
  form.id = undefined
  form.drugCode = ''
  form.drugName = ''
  form.specification = ''
  form.unit = ''
  form.manufacturer = ''
  form.category = ''
  form.remark = ''
  editOpen.value = true
}

function openEdit(record: HealthDrugDictionary) {
  form.id = record.id
  form.drugCode = record.drugCode || ''
  form.drugName = record.drugName
  form.specification = record.specification || ''
  form.unit = record.unit || ''
  form.manufacturer = record.manufacturer || ''
  form.category = record.category || ''
  form.remark = record.remark || ''
  editOpen.value = true
}

async function submit() {
  if (!form.drugName) {
    message.error('请输入药品名称')
    return
  }
  if (form.drugCode && form.drugCode.length > 64) {
    message.error('药品编码长度不能超过64')
    return
  }
  saving.value = true
  try {
    const payload = {
      drugCode: form.drugCode,
      drugName: form.drugName,
      specification: form.specification,
      unit: form.unit,
      manufacturer: form.manufacturer,
      category: form.category,
      remark: form.remark
    }
    if (form.id) {
      await updateHealthDrugDictionary(form.id, payload)
    } else {
      await createHealthDrugDictionary(payload)
    }
    message.success('保存成功')
    editOpen.value = false
    fetchData()
  } catch (error) {
    message.error(resolveHealthError(error, '保存失败'))
  } finally {
    saving.value = false
  }
}

async function remove(record: HealthDrugDictionary) {
  try {
    await deleteHealthDrugDictionary(record.id)
    message.success('删除成功')
    fetchData()
  } catch (error) {
    message.error(resolveHealthError(error, '删除失败'))
  }
}

function resolveRowClassName(record: HealthDrugDictionary) {
  if (!record.drugCode || !record.specification || !record.unit) return 'health-row-warning'
  return ''
}

async function exportCsvData() {
  const records = await loadExportRecords()
  if (!records.length) {
    message.warning('暂无可导出数据')
    return
  }
  exportCsv(records, `药品字典-${dayjs().format('YYYYMMDD-HHmmss')}.csv`)
  message.success('CSV导出成功')
}

async function exportExcelData() {
  const records = await loadExportRecords()
  if (!records.length) {
    message.warning('暂无可导出数据')
    return
  }
  exportExcel(records, `药品字典-${dayjs().format('YYYYMMDD-HHmmss')}.xls`)
  message.success('Excel导出成功')
}

async function loadExportRecords() {
  exporting.value = true
  try {
    const pageSize = 500
    let pageNo = 1
    let total = 0
    const list: HealthDrugDictionary[] = []
    do {
      const page = await getHealthDrugDictionaryPage({
        keyword: query.keyword || undefined,
        pageNo,
        pageSize
      }) as PageResult<HealthDrugDictionary>
      total = page.total || 0
      list.push(...(page.list || []))
      pageNo += 1
      if (!page.list || page.list.length < pageSize) break
    } while (list.length < total && pageNo <= 20)
    return mapHealthExportRows(list, drugDictionaryExportColumns)
  } catch (error) {
    message.error(resolveHealthError(error, '加载导出数据失败'))
    return []
  } finally {
    exporting.value = false
  }
}

fetchData()
</script>

<style scoped>
:deep(.health-row-warning > td) {
  background: #fff7e6 !important;
}
</style>
