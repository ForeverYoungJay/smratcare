<template>
  <PageContainer title="点餐" subTitle="创建老人点餐并在下单前执行风险预检">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="日期范围">
        <a-range-picker v-model:value="query.range" />
      </a-form-item>
      <a-form-item label="餐次">
        <a-select v-model:value="query.mealType" :options="mealOptions" allow-clear style="width: 140px" />
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="query.status" :options="statusOptions" allow-clear style="width: 140px" />
      </a-form-item>
      <a-form-item label="关键词">
        <a-input v-model:value="query.keyword" placeholder="订单号/老人/菜品" allow-clear />
      </a-form-item>
      <template #extra>
        <a-button type="primary" @click="openCreate">新增点餐</a-button>
      </template>
    </SearchForm>

    <DataTable rowKey="id" :columns="columns" :data-source="rows" :loading="loading" :pagination="pagination" @change="handleTableChange">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'mealType'">
          {{ getDiningMealTypeLabel(record.mealType) }}
        </template>
        <template v-else-if="column.key === 'status'">
          <a-tag :color="statusColor(record.status)">{{ getDiningOrderStatusLabel(record.status) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="openEdit(record)">编辑</a-button>
            <a-dropdown>
              <a-button type="link">状态</a-button>
              <template #overlay>
                <a-menu @click="({ key }: any) => updateStatus(record, key)">
                  <a-menu-item v-for="item in statusOptions" :key="item.value">{{ item.label }}</a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
            <a-button type="link" danger @click="remove(record)">删除</a-button>
          </a-space>
        </template>
      </template>
    </DataTable>

    <a-modal v-model:open="editOpen" title="点餐信息" :confirm-loading="saving" @ok="submit" width="760px">
      <a-form layout="vertical">
        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="老人" required>
              <a-select
                v-model:value="form.elderId"
                show-search
                :options="elderOptions"
                option-filter-prop="label"
                placeholder="请选择老人"
                @change="onElderChange"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="订餐日期" required>
              <a-date-picker v-model:value="form.orderDate" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="餐次" required>
              <a-select v-model:value="form.mealType" :options="mealOptions" @change="onMealTypeChange" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="订单金额(元)" required>
              <a-input-number v-model:value="form.totalAmount" :min="0" :precision="2" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="菜品选择" required>
          <a-select
            v-model:value="form.dishIdList"
            mode="multiple"
            :options="dishOptions"
            placeholder="请选择菜品"
            @change="onDishChange"
          />
        </a-form-item>

        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="备餐分区">
              <a-select
                v-model:value="form.prepZoneId"
                :options="prepZoneOptions"
                allow-clear
                placeholder="请选择备餐分区"
                @change="onPrepZoneChange"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="送餐区域">
              <a-select
                v-model:value="form.deliveryAreaId"
                :options="deliveryAreaOptions"
                allow-clear
                placeholder="请选择送餐区域"
                @change="onDeliveryAreaChange"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="放行审批ID（可选）">
              <a-input-number v-model:value="form.overrideId" :min="1" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="放行申请原因">
              <a-input v-model:value="form.overrideApplyReason" placeholder="命中风险后可提交审批申请" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="备注">
          <a-input v-model:value="form.remark" />
        </a-form-item>

        <a-alert
          v-if="riskResult"
          :type="riskResult.allowed ? 'success' : 'warning'"
          :message="riskResult.allowed ? '预检通过' : `预检拦截：${riskResult.message}`"
          :description="riskDescription"
          show-icon
          style="margin-bottom: 12px"
        />

        <a-space>
          <a-button @click="runRiskCheck">风险预检</a-button>
          <a-button v-if="riskResult && !riskResult.allowed" @click="applyOverride">申请放行</a-button>
        </a-space>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import type { Dayjs } from 'dayjs'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import {
  DINING_MEAL_TYPES,
  DINING_MEAL_TYPE_OPTIONS,
  DINING_MESSAGES,
  DINING_ORDER_STATUS_OPTIONS,
  DINING_STATUS,
  getDiningMealTypeLabel,
  getDiningOrderStatusColor,
  getDiningOrderStatusLabel
} from '../../constants/dining'
import { getElderPage } from '../../api/elder'
import {
  getDiningMealOrderPage,
  createDiningMealOrder,
  updateDiningMealOrder,
  updateDiningMealOrderStatus,
  deleteDiningMealOrder,
  getDiningDishList,
  getDiningPrepZoneList,
  getDiningDeliveryAreaList,
  checkDiningMealOrderRisk,
  applyDiningRiskOverride
} from '../../api/dining'
import type { DiningMealOrder, DiningRiskCheckResponse, PageResult } from '../../types'

const mealOptions = DINING_MEAL_TYPE_OPTIONS
const statusOptions = DINING_ORDER_STATUS_OPTIONS

const loading = ref(false)
const rows = ref<DiningMealOrder[]>([])
const query = reactive({
  range: undefined as [Dayjs, Dayjs] | undefined,
  mealType: undefined as string | undefined,
  status: undefined as string | undefined,
  keyword: '',
  pageNo: 1,
  pageSize: 10
})
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const editOpen = ref(false)
const saving = ref(false)
const elderOptions = ref<{ label: string; value: number }[]>([])
const dishOptions = ref<{ label: string; value: number; price: number }[]>([])
const prepZoneOptions = ref<{ label: string; value: number; name: string }[]>([])
const deliveryAreaOptions = ref<{ label: string; value: number; name: string }[]>([])
const riskResult = ref<DiningRiskCheckResponse | null>(null)

const form = reactive({
  id: undefined as number | undefined,
  elderId: undefined as number | undefined,
  elderName: '',
  orderDate: dayjs(),
  mealType: DINING_MEAL_TYPES.lunch,
  dishIdList: [] as number[],
  dishNames: '',
  totalAmount: 0,
  prepZoneId: undefined as number | undefined,
  prepZoneName: '',
  deliveryAreaId: undefined as number | undefined,
  deliveryAreaName: '',
  overrideId: undefined as number | undefined,
  overrideApplyReason: '',
  remark: ''
})

const columns = [
  { title: '订单号', dataIndex: 'orderNo', key: 'orderNo', width: 150 },
  { title: '老人', dataIndex: 'elderName', key: 'elderName', width: 100 },
  { title: '订餐日期', dataIndex: 'orderDate', key: 'orderDate', width: 120 },
  { title: '餐次', dataIndex: 'mealType', key: 'mealType', width: 100 },
  { title: '菜品清单', dataIndex: 'dishNames', key: 'dishNames' },
  { title: '金额', dataIndex: 'totalAmount', key: 'totalAmount', width: 100 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 120 },
  { title: '操作', key: 'action', width: 200 }
]

const riskDescription = computed(() => {
  if (!riskResult.value) return ''
  if (riskResult.value.allowed) return riskResult.value.message
  const reasons = (riskResult.value.reasons || []).map((item) => item.reasonDetail).join('；')
  return `风险菜品：${(riskResult.value.blockedDishNames || []).join('、')}；${reasons}`
})

function statusColor(status?: string) {
  return getDiningOrderStatusColor(status)
}

async function fetchData() {
  loading.value = true
  try {
    const params: any = { pageNo: query.pageNo, pageSize: query.pageSize, keyword: query.keyword }
    if (query.mealType) {
      params.mealType = query.mealType
    }
    if (query.status) {
      params.status = query.status
    }
    if (query.range) {
      params.dateFrom = query.range[0].format('YYYY-MM-DD')
      params.dateTo = query.range[1].format('YYYY-MM-DD')
    }
    const res: PageResult<DiningMealOrder> = await getDiningMealOrderPage(params)
    rows.value = res.list
    pagination.total = res.total || res.list.length
  } finally {
    loading.value = false
  }
}

async function loadElders() {
  const page = await getElderPage({ pageNo: 1, pageSize: 200 })
  elderOptions.value = (page.list || []).map((item: any) => ({
    label: `${item.fullName || item.elderName || item.name}(${item.id})`,
    value: Number(item.id)
  }))
}

async function loadDishOptions() {
  const list = await getDiningDishList({ mealType: form.mealType, status: DINING_STATUS.enabled })
  dishOptions.value = (list || []).map((item: any) => ({
    label: `${item.dishName} ¥${item.unitPrice || 0}`,
    value: Number(item.id),
    price: Number(item.unitPrice || 0)
  }))
}

async function loadPrepZones() {
  const list = await getDiningPrepZoneList({})
  prepZoneOptions.value = (list || []).map((item: any) => ({
    label: `${item.zoneCode}-${item.zoneName}`,
    value: Number(item.id),
    name: item.zoneName
  }))
}

async function loadDeliveryAreas() {
  const list = await getDiningDeliveryAreaList({})
  deliveryAreaOptions.value = (list || []).map((item: any) => ({
    label: `${item.areaCode}-${item.areaName}`,
    value: Number(item.id),
    name: item.areaName
  }))
}

function onElderChange(value: number) {
  const selected = elderOptions.value.find((item) => item.value === value)
  form.elderName = selected?.label?.split('(')[0] || ''
}

function onMealTypeChange() {
  form.dishIdList = []
  form.dishNames = ''
  form.totalAmount = 0
  riskResult.value = null
  loadDishOptions()
}

function onDishChange(values: number[]) {
  const selected = dishOptions.value.filter((item) => values.includes(item.value))
  form.dishNames = selected.map((item) => item.label.split(' ¥')[0]).join(',')
  form.totalAmount = Number(selected.reduce((sum, item) => sum + item.price, 0).toFixed(2))
  riskResult.value = null
}

function onPrepZoneChange(value: number | undefined) {
  const selected = prepZoneOptions.value.find((item) => item.value === value)
  form.prepZoneName = selected?.name || ''
}

function onDeliveryAreaChange(value: number | undefined) {
  const selected = deliveryAreaOptions.value.find((item) => item.value === value)
  form.deliveryAreaName = selected?.name || ''
}

function handleTableChange(pag: any) {
  query.pageNo = pag.current
  query.pageSize = pag.pageSize
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchData()
}

function onReset() {
  query.range = undefined
  query.mealType = undefined
  query.status = undefined
  query.keyword = ''
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

async function openCreate() {
  form.id = undefined
  form.elderId = undefined
  form.elderName = ''
  form.orderDate = dayjs()
  form.mealType = DINING_MEAL_TYPES.lunch
  form.dishIdList = []
  form.dishNames = ''
  form.totalAmount = 0
  form.prepZoneId = undefined
  form.prepZoneName = ''
  form.deliveryAreaId = undefined
  form.deliveryAreaName = ''
  form.overrideId = undefined
  form.overrideApplyReason = ''
  form.remark = ''
  riskResult.value = null
  await Promise.all([loadDishOptions(), loadPrepZones(), loadDeliveryAreas()])
  editOpen.value = true
}

async function openEdit(record: DiningMealOrder) {
  form.id = record.id
  form.elderId = record.elderId
  form.elderName = record.elderName || ''
  form.orderDate = dayjs(record.orderDate)
  form.mealType = record.mealType
  form.prepZoneId = record.prepZoneId
  form.prepZoneName = record.prepZoneName || ''
  form.deliveryAreaId = record.deliveryAreaId
  form.deliveryAreaName = record.deliveryAreaName || ''
  await Promise.all([loadDishOptions(), loadPrepZones(), loadDeliveryAreas()])
  form.dishIdList = (record.dishIds || '')
    .split(',')
    .map((item) => Number(item.trim()))
    .filter((item) => !!item)
  form.dishNames = record.dishNames
  form.totalAmount = record.totalAmount || 0
  form.overrideId = record.overrideId
  form.overrideApplyReason = ''
  form.remark = record.remark || ''
  riskResult.value = null
  editOpen.value = true
}

async function runRiskCheck() {
  if (!form.elderId || !form.orderDate || !form.mealType || form.dishIdList.length === 0) {
    message.error(DINING_MESSAGES.requiredOrderMealDish)
    return
  }
  riskResult.value = await checkDiningMealOrderRisk({
    elderId: form.elderId,
    elderName: form.elderName,
    orderDate: dayjs(form.orderDate).format('YYYY-MM-DD'),
    mealType: form.mealType,
    dishIds: form.dishIdList.join(','),
    dishNames: form.dishNames,
    totalAmount: form.totalAmount
  })
}

async function applyOverride() {
  if (!riskResult.value || riskResult.value.allowed) {
    message.warning(DINING_MESSAGES.noNeedOverride)
    return
  }
  if (!form.overrideApplyReason) {
    message.error(DINING_MESSAGES.requiredOverrideReason)
    return
  }
  const res = await applyDiningRiskOverride({
    elderId: form.elderId,
    elderName: form.elderName,
    dishNames: form.dishNames,
    applyReason: form.overrideApplyReason
  })
  form.overrideId = res.id
  message.success(`${DINING_MESSAGES.overrideAppliedPrefix} ${res.id}`)
}

async function submit() {
  if (!form.elderId || !form.orderDate || !form.mealType || form.dishIdList.length === 0) {
    message.error(DINING_MESSAGES.requiredOrderFields)
    return
  }
  saving.value = true
  try {
    if (!riskResult.value) {
      await runRiskCheck()
    }
    if (riskResult.value && !riskResult.value.allowed && !form.overrideId) {
      message.error(DINING_MESSAGES.requiredOverrideId)
      return
    }

    const payload = {
      elderId: form.elderId,
      elderName: form.elderName,
      orderDate: dayjs(form.orderDate).format('YYYY-MM-DD'),
      mealType: form.mealType,
      dishIds: form.dishIdList.join(','),
      dishNames: form.dishNames,
      totalAmount: form.totalAmount,
      prepZoneId: form.prepZoneId,
      prepZoneName: form.prepZoneName,
      deliveryAreaId: form.deliveryAreaId,
      deliveryAreaName: form.deliveryAreaName,
      overrideId: form.overrideId,
      remark: form.remark
    }
    if (form.id) {
      await updateDiningMealOrder(form.id, payload)
    } else {
      await createDiningMealOrder(payload)
    }
    editOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function updateStatus(record: DiningMealOrder, status: string) {
  await updateDiningMealOrderStatus(record.id, status)
  fetchData()
}

async function remove(record: DiningMealOrder) {
  await deleteDiningMealOrder(record.id)
  fetchData()
}

loadElders()
loadPrepZones()
loadDeliveryAreas()
fetchData()
</script>
