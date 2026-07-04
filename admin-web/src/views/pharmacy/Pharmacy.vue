<template>
  <PageContainer title="药事管理" subTitle="药品目录 · 批次库存 · 发药记录（FEFO 先过期先出）">
    <a-tabs v-model:activeKey="activeKey" @change="onTabChange">
      <a-tab-pane key="drugs" tab="药品目录">
        <SearchForm :model="drugQuery" @search="loadDrugs" @reset="resetDrugs">
          <a-form-item label="关键词">
            <a-input v-model:value="drugQuery.keyword" placeholder="名称/编码/通用名" allow-clear />
          </a-form-item>
        </SearchForm>
        <DataTable rowKey="id" :columns="drugColumns" :data-source="drugs" :loading="loading" :pagination="drugPagination" @change="onDrugPage">
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'control'">
              <a-tag>{{ controlText(record.controlLevel) }}</a-tag>
              <a-tag v-if="record.isHighRisk" color="red">高危</a-tag>
            </template>
            <template v-else-if="column.key === 'price'">{{ yuan(record.price) }} 元</template>
            <template v-else-if="column.key === 'action'">
              <div class="row-action-links">
                <a-button type="link" size="small" @click="openInbound(record)">入库</a-button>
                <a-button type="link" size="small" @click="openDispense(record)">发药</a-button>
                <a-button type="link" size="small" @click="viewBatches(record)">批次</a-button>
              </div>
            </template>
          </template>
        </DataTable>
      </a-tab-pane>

      <a-tab-pane key="batches" tab="批次库存">
        <DataTable rowKey="id" :columns="batchColumns" :data-source="batches" :loading="loading" :pagination="batchPagination" @change="onBatchPage">
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'expireDate'">
              <span :class="{ 'exp-warn': isNearExpiry(record.expireDate) }">{{ record.expireDate || '-' }}</span>
            </template>
            <template v-else-if="column.key === 'status'">
              <a-tag :color="record.status === 'ACTIVE' ? 'green' : 'default'">{{ record.status }}</a-tag>
            </template>
          </template>
        </DataTable>
      </a-tab-pane>

      <a-tab-pane key="records" tab="发药记录">
        <DataTable rowKey="id" :columns="recordColumns" :data-source="records" :loading="loading" :pagination="recordPagination" @change="onRecordPage">
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'dispenseType'">
              <a-tag :color="record.dispenseType === 'RETURN' ? 'orange' : 'blue'">{{ record.dispenseType === 'RETURN' ? '退药' : '发药' }}</a-tag>
            </template>
          </template>
        </DataTable>
      </a-tab-pane>
    </a-tabs>

    <a-modal v-model:open="inboundOpen" :title="`入库 - ${currentDrug?.drugName || ''}`" :confirm-loading="saving" @ok="submitInbound">
      <a-form layout="vertical">
        <a-form-item label="批号" required><a-input v-model:value="inboundForm.batchNo" /></a-form-item>
        <a-row :gutter="16">
          <a-col :span="12"><a-form-item label="数量" required><a-input-number v-model:value="inboundForm.quantity" :min="1" style="width:100%" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="进价(元)"><a-input-number v-model:value="inboundForm.purchasePriceYuan" :min="0" :precision="2" style="width:100%" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="效期"><a-date-picker v-model:value="inboundForm.expireDate" value-format="YYYY-MM-DD" style="width:100%" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="生产日期"><a-date-picker v-model:value="inboundForm.productionDate" value-format="YYYY-MM-DD" style="width:100%" /></a-form-item></a-col>
        </a-row>
        <a-form-item label="供应商"><a-input v-model:value="inboundForm.supplier" /></a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="dispenseOpen" :title="`发药 - ${currentDrug?.drugName || ''}`" :confirm-loading="saving" @ok="submitDispense">
      <a-alert type="info" show-icon message="系统将按 FEFO（先过期先出）自动选择批次扣减" style="margin-bottom:12px" />
      <a-form layout="vertical">
        <a-form-item label="发药数量" required><a-input-number v-model:value="dispenseForm.quantity" :min="1" style="width:100%" /></a-form-item>
        <a-form-item label="长者">
          <a-select v-model:value="dispenseForm.elderId" show-search allow-clear :filter-option="false" :options="elderOptions"
            placeholder="输入姓名搜索" @search="searchElders" @focus="() => !elderOptions.length && searchElders('')" />
        </a-form-item>
        <a-form-item label="关联医嘱ID"><a-input-number v-model:value="dispenseForm.orderId" style="width:100%" placeholder="选填" /></a-form-item>
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
import { useElderOptions } from '../../composables/useElderOptions'
import { getDrugPage, getDrugBatchPage, drugInbound, drugDispense, getDispenseRecordPage } from '../../api/pharmacy'
import type { DrugBatch, DrugCatalog, DrugDispenseRecord, Id, PageResult } from '../../types'

const activeKey = ref('drugs')
const loading = ref(false)
const saving = ref(false)
const { elderOptions, searchElders } = useElderOptions({ pageSize: 50 })

const drugs = ref<DrugCatalog[]>([])
const drugQuery = reactive({ keyword: undefined as string | undefined, pageNo: 1, pageSize: 10 })
const drugPagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const drugColumns = [
  { title: '编码', dataIndex: 'drugCode', key: 'drugCode', width: 100 },
  { title: '名称', dataIndex: 'drugName', key: 'drugName' },
  { title: '规格', dataIndex: 'spec', key: 'spec', width: 120 },
  { title: '剂型', dataIndex: 'dosageForm', key: 'dosageForm', width: 90 },
  { title: '管制/高危', key: 'control', width: 150 },
  { title: '安全库存', dataIndex: 'safetyStock', key: 'safetyStock', width: 90 },
  { title: '单价', key: 'price', width: 100 },
  { title: '操作', key: 'action', width: 170 }
]

const batches = ref<DrugBatch[]>([])
const batchQuery = reactive({ drugId: undefined as Id | undefined, pageNo: 1, pageSize: 10 })
const batchPagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const batchColumns = [
  { title: '药品ID', dataIndex: 'drugId', key: 'drugId', width: 110 },
  { title: '批号', dataIndex: 'batchNo', key: 'batchNo', width: 140 },
  { title: '效期', key: 'expireDate', width: 130 },
  { title: '库存', dataIndex: 'quantity', key: 'quantity', width: 90 },
  { title: '供应商', dataIndex: 'supplier', key: 'supplier' },
  { title: '状态', key: 'status', width: 100 }
]

const records = ref<DrugDispenseRecord[]>([])
const recordPagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const recordQuery = reactive({ pageNo: 1, pageSize: 10 })
const recordColumns = [
  { title: '记录ID', dataIndex: 'id', key: 'id', width: 120 },
  { title: '药品ID', dataIndex: 'drugId', key: 'drugId', width: 110 },
  { title: '批号', dataIndex: 'batchNo', key: 'batchNo', width: 140 },
  { title: '类型', key: 'dispenseType', width: 90 },
  { title: '数量', dataIndex: 'quantity', key: 'quantity', width: 80 },
  { title: '长者ID', dataIndex: 'elderId', key: 'elderId', width: 100 },
  { title: '时间', dataIndex: 'dispenseTime', key: 'dispenseTime', width: 170 }
]

const currentDrug = ref<DrugCatalog | null>(null)
const inboundOpen = ref(false)
const dispenseOpen = ref(false)
const inboundForm = reactive({ batchNo: '', quantity: undefined as number | undefined, purchasePriceYuan: undefined as number | undefined, expireDate: undefined as string | undefined, productionDate: undefined as string | undefined, supplier: '' })
const dispenseForm = reactive({ quantity: undefined as number | undefined, elderId: undefined as Id | undefined, orderId: undefined as number | undefined })

function yuan(fen?: number) { return fen == null ? '0.00' : (Number(fen) / 100).toFixed(2) }
function controlText(c?: string) {
  return ({ NORMAL: '普通', PRESCRIPTION: '处方', PSYCHOTROPIC: '精神', NARCOTIC: '麻醉' } as Record<string, string>)[c || ''] || c || '-'
}
function isNearExpiry(d?: string) {
  return d ? dayjs(d).diff(dayjs(), 'day') <= 30 : false
}

async function loadDrugs() {
  loading.value = true
  try {
    const res: PageResult<DrugCatalog> = await getDrugPage({ pageNo: drugQuery.pageNo, pageSize: drugQuery.pageSize, keyword: drugQuery.keyword })
    drugs.value = res.list
    drugPagination.total = res.total || res.list.length
  } finally { loading.value = false }
}
function resetDrugs() { drugQuery.keyword = undefined; drugQuery.pageNo = 1; drugPagination.current = 1; loadDrugs() }
function onDrugPage(p: any) { drugPagination.current = p.current; drugPagination.pageSize = p.pageSize; drugQuery.pageNo = p.current; drugQuery.pageSize = p.pageSize; loadDrugs() }

async function loadBatches() {
  loading.value = true
  try {
    const res: PageResult<DrugBatch> = await getDrugBatchPage({ pageNo: batchQuery.pageNo, pageSize: batchQuery.pageSize, drugId: batchQuery.drugId })
    batches.value = res.list
    batchPagination.total = res.total || res.list.length
  } finally { loading.value = false }
}
function onBatchPage(p: any) { batchPagination.current = p.current; batchPagination.pageSize = p.pageSize; batchQuery.pageNo = p.current; batchQuery.pageSize = p.pageSize; loadBatches() }

async function loadRecords() {
  loading.value = true
  try {
    const res: PageResult<DrugDispenseRecord> = await getDispenseRecordPage({ pageNo: recordQuery.pageNo, pageSize: recordQuery.pageSize })
    records.value = res.list
    recordPagination.total = res.total || res.list.length
  } finally { loading.value = false }
}
function onRecordPage(p: any) { recordPagination.current = p.current; recordPagination.pageSize = p.pageSize; recordQuery.pageNo = p.current; recordQuery.pageSize = p.pageSize; loadRecords() }

function onTabChange(key: string) {
  if (key === 'drugs') loadDrugs()
  else if (key === 'batches') loadBatches()
  else loadRecords()
}
function viewBatches(record: DrugCatalog) {
  batchQuery.drugId = record.id
  activeKey.value = 'batches'
  loadBatches()
}

function openInbound(record: DrugCatalog) {
  currentDrug.value = record
  Object.assign(inboundForm, { batchNo: '', quantity: undefined, purchasePriceYuan: undefined, expireDate: undefined, productionDate: undefined, supplier: '' })
  inboundOpen.value = true
}
async function submitInbound() {
  if (!currentDrug.value || !inboundForm.batchNo || !inboundForm.quantity) { message.error('请填写批号与数量'); return }
  saving.value = true
  try {
    await drugInbound({
      drugId: currentDrug.value.id,
      batchNo: inboundForm.batchNo,
      quantity: inboundForm.quantity,
      purchasePrice: inboundForm.purchasePriceYuan != null ? Math.round(inboundForm.purchasePriceYuan * 100) : undefined,
      expireDate: inboundForm.expireDate,
      productionDate: inboundForm.productionDate,
      supplier: inboundForm.supplier || undefined
    })
    message.success('入库成功')
    inboundOpen.value = false
    if (activeKey.value === 'batches') loadBatches()
  } finally { saving.value = false }
}

function openDispense(record: DrugCatalog) {
  currentDrug.value = record
  Object.assign(dispenseForm, { quantity: undefined, elderId: undefined, orderId: undefined })
  dispenseOpen.value = true
}
async function submitDispense() {
  if (!currentDrug.value || !dispenseForm.quantity) { message.error('请填写发药数量'); return }
  saving.value = true
  try {
    const recs = await drugDispense({
      drugId: currentDrug.value.id,
      quantity: dispenseForm.quantity,
      elderId: dispenseForm.elderId,
      orderId: dispenseForm.orderId as unknown as Id
    })
    message.success(`发药成功，扣减 ${recs?.length || 0} 个批次`)
    dispenseOpen.value = false
  } finally { saving.value = false }
}

loadDrugs()
searchElders('')
</script>

<style scoped>
.exp-warn { color: #cf1322; font-weight: 600; }
</style>
