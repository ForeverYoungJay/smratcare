<template>
  <PageContainer title="基础疾病维护" subTitle="医护侧统一维护长者基础疾病，并联动商城禁忌与配餐风险">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="长者">
        <ElderNameAutocomplete v-model:value="query.fullName" allow-clear placeholder="姓名(编号)" width="220px" />
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
          <a-button @click="go('/logistics/commerce/risk')">疾病字典/禁忌规则</a-button>
          <a-button type="primary" @click="fetchData">刷新</a-button>
        </a-space>
      </template>
    </SearchForm>

    <DataTable rowKey="id" :columns="columns" :data-source="rows" :loading="loading" :pagination="pagination" @change="onTableChange">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'diseaseSummary'">
          <a-space v-if="diseaseSummaryMap[String(record.id)]?.length" wrap>
            <a-tag v-for="item in diseaseSummaryMap[String(record.id)].slice(0, 3)" :key="item" color="blue">{{ item }}</a-tag>
            <a-tag v-if="diseaseSummaryMap[String(record.id)].length > 3" color="default">+{{ diseaseSummaryMap[String(record.id)].length - 3 }}</a-tag>
          </a-space>
          <span v-else class="disease-empty">未维护</span>
        </template>
        <template v-else-if="column.key === 'status'">
          <a-tag :color="statusColor(record.status)">{{ statusLabel(record.status) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="openDiseaseDrawer(record)">维护基础疾病</a-button>
            <a-button type="link" @click="go(`/elder/resident-360?residentId=${record.id}&from=medicalDiseaseManager`)">长者总览</a-button>
          </a-space>
        </template>
      </template>
    </DataTable>

    <ElderDiseaseEditorDrawer
      v-model:open="drawerOpen"
      :elder-id="activeResident?.id"
      :elder-name="activeResident?.fullName"
      title="维护长者基础疾病"
      @saved="handleDiseaseSaved"
    />
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import ElderNameAutocomplete from '../../components/ElderNameAutocomplete.vue'
import ElderDiseaseEditorDrawer from '../../components/ElderDiseaseEditorDrawer.vue'
import { getElderPage } from '../../api/elder'
import { useElderDiseaseCatalog } from '../../composables/useElderDiseaseCatalog'
import type { ElderItem, PageResult } from '../../types'
import { resolveMedicalError } from './medicalError'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const rows = ref<ElderItem[]>([])
const drawerOpen = ref(false)
const activeResident = ref<ElderItem | null>(null)
const diseaseSummaryMap = ref<Record<string, string[]>>({})
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const query = reactive({
  fullName: '',
  bedNo: '',
  careLevel: '',
  status: undefined as number | undefined,
  pageNo: 1,
  pageSize: 10
})
const { ensureDiseaseCatalogLoaded, loadDiseaseSummaryMap } = useElderDiseaseCatalog()

const columns = [
  { title: '长者', dataIndex: 'fullName', key: 'fullName', width: 120 },
  { title: '床位', dataIndex: 'bedNo', key: 'bedNo', width: 110 },
  { title: '护理等级', dataIndex: 'careLevel', key: 'careLevel', width: 120 },
  { title: '基础疾病', key: 'diseaseSummary', width: 280 },
  { title: '联系电话', dataIndex: 'phone', key: 'phone', width: 140 },
  { title: '状态', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 240 }
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

async function refreshDiseaseSummary(records: ElderItem[]) {
  diseaseSummaryMap.value = await loadDiseaseSummaryMap((records || []).map((item) => item.id))
}

async function fetchData() {
  loading.value = true
  try {
    await ensureDiseaseCatalogLoaded()
    const res = await getElderPage(buildParams()) as PageResult<ElderItem>
    rows.value = res.list || []
    pagination.total = res.total || 0
    await refreshDiseaseSummary(rows.value)
  } catch (error) {
    message.error(resolveMedicalError(error, '加载长者列表失败'))
    rows.value = []
    diseaseSummaryMap.value = {}
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

function onTableChange(pag: any) {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  query.pageNo = pag.current
  query.pageSize = pag.pageSize
  fetchData().catch(() => {})
}

function onReset() {
  query.fullName = ''
  query.bedNo = ''
  query.careLevel = ''
  query.status = undefined
  query.pageNo = 1
  query.pageSize = pagination.pageSize
  pagination.current = 1
  fetchData().catch(() => {})
}

function openDiseaseDrawer(record: ElderItem) {
  activeResident.value = record
  drawerOpen.value = true
}

function openDiseaseDrawerByRoute() {
  const elderId = route.query.elderId || route.query.residentId
  if (!elderId) return
  activeResident.value = {
    id: elderId as string,
    fullName: String(route.query.residentName || '')
  } as ElderItem
  drawerOpen.value = true
}

function handleDiseaseSaved(payload: { elderId?: string | number; diseaseLabels: string[] }) {
  const elderId = String(payload.elderId || '')
  if (!elderId) return
  diseaseSummaryMap.value = {
    ...diseaseSummaryMap.value,
    [elderId]: payload.diseaseLabels || []
  }
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

onMounted(() => {
  fetchData().catch(() => {})
  openDiseaseDrawerByRoute()
})
</script>

<style scoped>
.disease-empty {
  color: #94a3b8;
}
</style>
