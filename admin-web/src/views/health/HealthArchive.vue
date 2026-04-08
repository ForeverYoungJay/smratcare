<template>
  <PageContainer title="健康档案" subTitle="维护老人长期健康信息，并补充基础疾病入口">
    <template #meta>
      <span class="soft-pill">档案总数 {{ pagination.total || rows.length }}</span>
      <span class="soft-pill">已维护疾病 {{ diseaseMaintainedCount }}</span>
      <span v-for="tag in activeFilterTags" :key="tag" class="selection-pill">{{ tag }}</span>
    </template>

    <template #stats>
      <div class="metric-grid">
        <div class="metric-card metric-card--primary">
          <span class="metric-card__label">当前列表</span>
          <strong class="metric-card__value">{{ rows.length }}</strong>
          <span class="metric-card__hint">按当前长者查询范围统计</span>
        </div>
        <div class="metric-card metric-card--success">
          <span class="metric-card__label">已维护疾病</span>
          <strong class="metric-card__value">{{ diseaseMaintainedCount }}</strong>
          <span class="metric-card__hint">具备结构化基础疾病信息</span>
        </div>
        <div class="metric-card metric-card--warning">
          <span class="metric-card__label">待补充疾病</span>
          <strong class="metric-card__value">{{ rows.length - diseaseMaintainedCount }}</strong>
          <span class="metric-card__hint">建议补齐基础疾病标签</span>
        </div>
        <div class="metric-card">
          <span class="metric-card__label">当前编辑对象</span>
          <strong class="metric-card__value">{{ healthArchiveDiseaseLabels.length }}</strong>
          <span class="metric-card__hint">表单中已同步的疾病标签数</span>
        </div>
      </div>
    </template>

    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="老人姓名">
        <ElderNameAutocomplete v-model:value="query.keyword" placeholder="老人姓名(编号)" width="220px" />
      </a-form-item>
      <template #extra>
        <a-space>
          <a-button @click="goMedicalDiseaseManager">基础疾病维护</a-button>
          <a-button @click="exportCsvData" :loading="exporting">导出CSV</a-button>
          <a-button @click="exportExcelData" :loading="exporting">导出Excel</a-button>
          <a-button type="primary" @click="openCreate">新增档案</a-button>
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
        <template v-if="column.key === 'diseaseSummary'">
          <a-space v-if="diseaseSummaryMap[String(record.elderId || '')]?.length" wrap>
            <a-tag v-for="item in diseaseSummaryMap[String(record.elderId || '')].slice(0, 3)" :key="item" color="blue">{{ item }}</a-tag>
            <a-tag v-if="diseaseSummaryMap[String(record.elderId || '')].length > 3" color="default">+{{ diseaseSummaryMap[String(record.elderId || '')].length - 3 }}</a-tag>
          </a-space>
          <span v-else class="disease-empty">未维护</span>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="openDiseaseDrawer(record)">基础疾病</a-button>
            <a-button type="link" @click="openEdit(record)">编辑</a-button>
            <a-popconfirm title="确认删除该记录吗？" ok-text="确认" cancel-text="取消" @confirm="remove(record)">
              <a-button type="link" danger>删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </DataTable>

    <a-modal v-model:open="editOpen" title="健康档案" @ok="submit" :confirm-loading="saving" width="760px">
      <a-form layout="vertical">
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
        <a-row :gutter="16">
          <a-col :span="12"><a-form-item label="血型"><a-input v-model:value="form.bloodType" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="慢病补充说明"><a-input v-model:value="form.chronicDisease" placeholder="用于补充说明，非结构化疾病字段" /></a-form-item></a-col>
        </a-row>
        <a-form-item label="基础疾病">
          <a-space v-if="healthArchiveDiseaseLabels.length" wrap>
            <a-tag v-for="item in healthArchiveDiseaseLabels" :key="item" color="blue">{{ item }}</a-tag>
          </a-space>
          <span v-else class="disease-empty">尚未维护基础疾病</span>
          <a-button
            type="link"
            style="padding-left: 0"
            :disabled="!form.elderId"
            @click="openDiseaseDrawer({ elderId: form.elderId, elderName: form.elderName || findElderName(form.elderId) })"
          >
            维护基础疾病
          </a-button>
        </a-form-item>
        <a-form-item label="过敏史"><a-input v-model:value="form.allergyHistory" /></a-form-item>
        <a-form-item label="既往病史"><a-textarea v-model:value="form.medicalHistory" :rows="3" /></a-form-item>
        <a-row :gutter="16">
          <a-col :span="12"><a-form-item label="紧急联系人"><a-input v-model:value="form.emergencyContact" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="联系电话"><a-input v-model:value="form.emergencyPhone" /></a-form-item></a-col>
        </a-row>
        <a-form-item label="备注"><a-input v-model:value="form.remark" /></a-form-item>
      </a-form>
    </a-modal>

    <ElderDiseaseEditorDrawer
      v-model:open="diseaseDrawerOpen"
      :elder-id="diseaseDrawerTarget.elderId"
      :elder-name="diseaseDrawerTarget.elderName"
      title="维护长者基础疾病"
      @saved="handleDiseaseSaved"
    />
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import ElderNameAutocomplete from '../../components/ElderNameAutocomplete.vue'
import ElderDiseaseEditorDrawer from '../../components/ElderDiseaseEditorDrawer.vue'
import { useElderDiseaseCatalog } from '../../composables/useElderDiseaseCatalog'
import { useElderOptions } from '../../composables/useElderOptions'
import dayjs from 'dayjs'
import { mapHealthExportRows, archiveExportColumns } from '../../constants/healthExport'
import { exportCsv, exportExcel } from '../../utils/export'
import { normalizeResidentId } from '../../utils/id'
import { resolveHealthError } from './healthError'
import { getHealthArchivePage, createHealthArchive, updateHealthArchive, deleteHealthArchive } from '../../api/health'
import type { HealthArchive, Id, PageResult } from '../../types'

const loading = ref(false)
const exporting = ref(false)
const router = useRouter()
const route = useRoute()
const rows = ref<HealthArchive[]>([])
const diseaseSummaryMap = ref<Record<string, string[]>>({})
const healthArchiveDiseaseLabels = ref<string[]>([])
const diseaseDrawerOpen = ref(false)
const diseaseDrawerTarget = ref<{ elderId?: Id; elderName?: string }>({})
const query = reactive({ keyword: '', pageNo: 1, pageSize: 10 })
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

const columns = [
  { title: '老人', dataIndex: 'elderName', key: 'elderName', width: 120 },
  { title: '基础疾病', key: 'diseaseSummary', width: 220 },
  { title: '血型', dataIndex: 'bloodType', key: 'bloodType', width: 100 },
  { title: '慢病补充说明', dataIndex: 'chronicDisease', key: 'chronicDisease', width: 150 },
  { title: '过敏史', dataIndex: 'allergyHistory', key: 'allergyHistory', width: 180 },
  { title: '紧急联系人', dataIndex: 'emergencyContact', key: 'emergencyContact', width: 120 },
  { title: '联系电话', dataIndex: 'emergencyPhone', key: 'emergencyPhone', width: 140 },
  { title: '操作', key: 'action', width: 180 }
]

const editOpen = ref(false)
const saving = ref(false)
const { elderOptions, searchElders, findElderName, ensureSelectedElder } = useElderOptions({ pageSize: 50 })
const { loadDiseaseState, loadDiseaseSummaryMap } = useElderDiseaseCatalog()
const activeFilterTags = computed(() => (query.keyword ? [`长者: ${query.keyword}`] : []))
const diseaseMaintainedCount = computed(() =>
  rows.value.filter((item) => (diseaseSummaryMap.value[String(item.elderId || '')] || []).length > 0).length
)
const form = reactive({
  id: undefined as Id | undefined,
  elderId: undefined as Id | undefined,
  elderName: '',
  bloodType: '',
  allergyHistory: '',
  chronicDisease: '',
  medicalHistory: '',
  emergencyContact: '',
  emergencyPhone: '',
  remark: ''
})

async function fetchData() {
  loading.value = true
  try {
    const residentId = normalizeResidentId(route.query as Record<string, unknown>)
    const res: PageResult<HealthArchive> = await getHealthArchivePage({
      keyword: query.keyword || undefined,
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      elderId: residentId
    })
    rows.value = res.list
    diseaseSummaryMap.value = await loadDiseaseSummaryMap(
      (rows.value || []).map((item) => item.elderId).filter(Boolean) as Id[]
    )
    pagination.total = res.total || res.list.length
  } catch (error) {
    message.error(resolveHealthError(error, '加载健康档案失败'))
    rows.value = []
    diseaseSummaryMap.value = {}
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
  const residentId = normalizeResidentId(route.query as Record<string, unknown>)
  const residentName = typeof route.query.residentName === 'string' ? route.query.residentName : ''
  form.id = undefined
  form.elderId = residentId
  form.elderName = residentName
  ensureSelectedElder(form.elderId, residentName)
  form.bloodType = ''
  form.allergyHistory = ''
  form.chronicDisease = ''
  form.medicalHistory = ''
  form.emergencyContact = ''
  form.emergencyPhone = ''
  form.remark = ''
  healthArchiveDiseaseLabels.value = []
  if (form.elderId) {
    loadFormDiseaseState(form.elderId).catch(() => {})
  }
  editOpen.value = true
}

function openEdit(record: HealthArchive) {
  form.id = record.id
  form.elderId = record.elderId
  form.elderName = record.elderName || ''
  ensureSelectedElder(record.elderId, record.elderName)
  form.bloodType = record.bloodType || ''
  form.allergyHistory = record.allergyHistory || ''
  form.chronicDisease = record.chronicDisease || ''
  form.medicalHistory = record.medicalHistory || ''
  form.emergencyContact = record.emergencyContact || ''
  form.emergencyPhone = record.emergencyPhone || ''
  form.remark = record.remark || ''
  loadFormDiseaseState(record.elderId).catch(() => {})
  editOpen.value = true
}

async function onElderChange(elderId?: Id) {
  form.elderName = findElderName(elderId)
  await loadFormDiseaseState(elderId)
}

async function loadFormDiseaseState(elderId?: Id) {
  if (!elderId) {
    healthArchiveDiseaseLabels.value = []
    return
  }
  try {
    const state = await loadDiseaseState(elderId)
    healthArchiveDiseaseLabels.value = state.labels
  } catch {
    healthArchiveDiseaseLabels.value = []
  }
}

async function submit() {
  if (!form.elderId) {
    message.error('请选择老人')
    return
  }
  if (form.emergencyPhone && !/^[0-9-]{6,20}$/.test(form.emergencyPhone)) {
    message.error('联系电话格式不正确')
    return
  }
  saving.value = true
  try {
    const payload = {
      elderId: form.elderId,
      elderName: findElderName(form.elderId) || form.elderName,
      bloodType: form.bloodType,
      allergyHistory: form.allergyHistory,
      chronicDisease: form.chronicDisease,
      medicalHistory: form.medicalHistory,
      emergencyContact: form.emergencyContact,
      emergencyPhone: form.emergencyPhone,
      remark: form.remark
    }
    if (form.id) {
      await updateHealthArchive(form.id, payload)
    } else {
      await createHealthArchive(payload)
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

async function remove(record: HealthArchive) {
  try {
    await deleteHealthArchive(record.id)
    message.success('删除成功')
    fetchData()
  } catch (error) {
    message.error(resolveHealthError(error, '删除失败'))
  }
}

function openDiseaseDrawer(record?: Partial<HealthArchive> & { elderId?: Id; elderName?: string }) {
  const elderId = record?.elderId
  if (!elderId) {
    message.warning('请先选择长者')
    return
  }
  diseaseDrawerTarget.value = {
    elderId,
    elderName: record?.elderName || findElderName(elderId)
  }
  diseaseDrawerOpen.value = true
}

function handleDiseaseSaved(payload: { elderId?: Id; diseaseLabels: string[] }) {
  const elderId = String(payload.elderId || '')
  if (!elderId) return
  diseaseSummaryMap.value = {
    ...diseaseSummaryMap.value,
    [elderId]: payload.diseaseLabels || []
  }
  if (String(form.elderId || '') === elderId) {
    healthArchiveDiseaseLabels.value = payload.diseaseLabels || []
  }
}

function goMedicalDiseaseManager() {
  router.push('/medical-care/basic-diseases')
}

function resolveRowClassName(record: HealthArchive) {
  if (!record.emergencyContact || !record.emergencyPhone) return 'health-row-warning'
  return ''
}

async function exportCsvData() {
  const records = await loadExportRecords()
  if (!records.length) {
    message.warning('暂无可导出数据')
    return
  }
  exportCsv(records, `健康档案-${dayjs().format('YYYYMMDD-HHmmss')}.csv`)
  message.success('CSV导出成功')
}

async function exportExcelData() {
  const records = await loadExportRecords()
  if (!records.length) {
    message.warning('暂无可导出数据')
    return
  }
  exportExcel(records, `健康档案-${dayjs().format('YYYYMMDD-HHmmss')}.xls`)
  message.success('Excel导出成功')
}

async function loadExportRecords() {
  exporting.value = true
  try {
    const pageSize = 500
    let pageNo = 1
    let total = 0
    const list: HealthArchive[] = []
    do {
      const residentId = normalizeResidentId(route.query as Record<string, unknown>)
      const page = await getHealthArchivePage({
        keyword: query.keyword || undefined,
        pageNo,
        pageSize,
        elderId: residentId
      }) as PageResult<HealthArchive>
      total = page.total || 0
      list.push(...(page.list || []))
      pageNo += 1
      if (!page.list || page.list.length < pageSize) break
    } while (list.length < total && pageNo <= 20)
    return mapHealthExportRows(list, archiveExportColumns)
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
    fetchData().catch(() => {})
  }
)
</script>

<style scoped>
:deep(.health-row-warning > td) {
  background: #fff7e6 !important;
}

.disease-empty {
  color: #94a3b8;
}
</style>
