<template>
  <PageContainer title="药品缴存" subTitle="登记老人药品缴存情况">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键词">
        <ElderNameAutocomplete v-model:value="query.keyword" placeholder="老人姓名(编号)" width="220px" />
      </a-form-item>
      <template #extra>
        <a-space>
          <a-button @click="exportCsvData" :loading="exporting">导出CSV</a-button>
          <a-button @click="exportExcelData" :loading="exporting">导出Excel</a-button>
          <a-button type="primary" @click="openCreate">新增缴存</a-button>
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
import { reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import ElderNameAutocomplete from '../../components/ElderNameAutocomplete.vue'
import { useElderOptions } from '../../composables/useElderOptions'
import { mapHealthExportRows, drugDepositExportColumns } from '../../constants/healthExport'
import { exportCsv, exportExcel } from '../../utils/export'
import { resolveHealthError } from './healthError'
import {
  getHealthMedicationDepositPage,
  createHealthMedicationDeposit,
  updateHealthMedicationDeposit,
  deleteHealthMedicationDeposit
} from '../../api/health'
import type { HealthMedicationDeposit, PageResult } from '../../types'

const loading = ref(false)
const exporting = ref(false)
const route = useRoute()
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
    const residentId = route.query.residentId ?? route.query.elderId
    const res: PageResult<HealthMedicationDeposit> = await getHealthMedicationDepositPage({
      keyword: query.keyword || undefined,
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      elderId: residentId ? Number(residentId) : undefined
    })
    rows.value = res.list
    pagination.total = res.total || res.list.length
  } catch (error) {
    message.error(resolveHealthError(error, '加载药品缴存失败'))
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
  const residentId = route.query.residentId ?? route.query.elderId
  const residentName = typeof route.query.residentName === 'string' ? route.query.residentName : ''
  form.id = undefined
  form.elderId = residentId ? Number(residentId) : undefined
  form.elderName = residentName
  ensureSelectedElder(form.elderId, residentName)
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
  } catch (error) {
    message.error(resolveHealthError(error, '保存失败'))
  } finally {
    saving.value = false
  }
}

async function remove(record: HealthMedicationDeposit) {
  try {
    await deleteHealthMedicationDeposit(record.id)
    message.success('删除成功')
    fetchData()
  } catch (error) {
    message.error(resolveHealthError(error, '删除失败'))
  }
}

function resolveRowClassName(record: HealthMedicationDeposit) {
  if (record.expireDate && dayjs(record.expireDate).isBefore(dayjs(), 'day')) return 'health-row-danger'
  if (!record.depositorName || Number(record.quantity || 0) <= 0) return 'health-row-warning'
  return ''
}

async function exportCsvData() {
  const records = await loadExportRecords()
  if (!records.length) {
    message.warning('暂无可导出数据')
    return
  }
  exportCsv(records, `药品缴存-${dayjs().format('YYYYMMDD-HHmmss')}.csv`)
  message.success('CSV导出成功')
}

async function exportExcelData() {
  const records = await loadExportRecords()
  if (!records.length) {
    message.warning('暂无可导出数据')
    return
  }
  exportExcel(records, `药品缴存-${dayjs().format('YYYYMMDD-HHmmss')}.xls`)
  message.success('Excel导出成功')
}

async function loadExportRecords() {
  exporting.value = true
  try {
    const pageSize = 500
    let pageNo = 1
    let total = 0
    const list: HealthMedicationDeposit[] = []
    do {
      const residentId = route.query.residentId ?? route.query.elderId
      const page = await getHealthMedicationDepositPage({
        keyword: query.keyword || undefined,
        pageNo,
        pageSize,
        elderId: residentId ? Number(residentId) : undefined
      }) as PageResult<HealthMedicationDeposit>
      total = page.total || 0
      list.push(...(page.list || []))
      pageNo += 1
      if (!page.list || page.list.length < pageSize) break
    } while (list.length < total && pageNo <= 20)
    return mapHealthExportRows(
      list.map((item) => ({
        ...item,
        depositDate: item.depositDate || '',
        expireDate: item.expireDate || ''
      })),
      drugDepositExportColumns
    )
  } catch (error) {
    message.error(resolveHealthError(error, '加载导出数据失败'))
    return []
  } finally {
    exporting.value = false
  }
}

fetchData()
searchElders('')

watch(
  () => [route.query.residentId, route.query.elderId],
  () => {
    query.pageNo = 1
    pagination.current = 1
    fetchData()
  }
)
</script>

<style scoped>
:deep(.health-row-danger > td) {
  background: #fff1f0 !important;
}
:deep(.health-row-warning > td) {
  background: #fff7e6 !important;
}
</style>
