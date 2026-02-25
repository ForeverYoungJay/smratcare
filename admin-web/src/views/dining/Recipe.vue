<template>
  <PageContainer title="食谱管理" subTitle="维护餐次食谱和适用人群">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="日期范围">
        <a-range-picker v-model:value="query.range" />
      </a-form-item>
      <a-form-item label="关键词">
        <a-input v-model:value="query.keyword" placeholder="食谱名称/菜品" allow-clear />
      </a-form-item>
      <a-form-item label="餐次">
        <a-select v-model:value="query.mealType" :options="mealOptions" allow-clear style="width: 160px" />
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="query.status" :options="statusOptions" allow-clear style="width: 140px" />
      </a-form-item>
      <template #extra>
        <a-button type="primary" @click="openCreate">新增食谱</a-button>
      </template>
    </SearchForm>

    <DataTable rowKey="id" :columns="columns" :data-source="rows" :loading="loading" :pagination="pagination" @change="handleTableChange">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'mealType'">
          {{ mealTypeLabel(record.mealType) }}
        </template>
        <template v-else-if="column.key === 'status'">
          <a-tag :color="record.status === DINING_STATUS.enabled ? 'green' : 'default'">
            {{ getDiningEnableStatusLabel(record.status) }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="openEdit(record)">编辑</a-button>
            <a-button type="link" danger @click="remove(record)">删除</a-button>
          </a-space>
        </template>
      </template>
    </DataTable>

    <a-modal v-model:open="editOpen" title="食谱信息" :confirm-loading="saving" @ok="submit">
      <a-form layout="vertical">
        <a-form-item label="食谱名称" required>
          <a-input v-model:value="form.recipeName" />
        </a-form-item>
        <a-form-item label="餐次">
          <a-select v-model:value="form.mealType" :options="mealOptions" @change="onMealTypeChange" />
        </a-form-item>
        <a-form-item label="计划日期">
          <a-date-picker v-model:value="form.planDate" style="width: 100%" />
        </a-form-item>
        <a-form-item label="菜品清单" required>
          <a-select
            v-model:value="form.dishIdList"
            mode="multiple"
            :options="dishOptions"
            placeholder="请选择菜品"
            @change="onDishChange"
          />
        </a-form-item>
        <a-form-item label="菜品名称">
          <a-input :value="form.dishNames" readonly />
        </a-form-item>
        <a-form-item label="适用人群">
          <a-input v-model:value="form.suitableCrowd" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="form.status" :options="statusOptions" />
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
import type { Dayjs } from 'dayjs'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import {
  DINING_ENABLE_STATUS_OPTIONS,
  DINING_MEAL_TYPES,
  DINING_MEAL_TYPE_OPTIONS,
  DINING_MESSAGES,
  DINING_STATUS,
  getDiningEnableStatusLabel,
  getDiningMealTypeLabel
} from '../../constants/dining'
import { getDiningRecipePage, createDiningRecipe, updateDiningRecipe, deleteDiningRecipe, getDiningDishList } from '../../api/dining'
import type { DiningRecipe, PageResult } from '../../types'

const mealOptions = DINING_MEAL_TYPE_OPTIONS
const statusOptions = DINING_ENABLE_STATUS_OPTIONS

const loading = ref(false)
const rows = ref<DiningRecipe[]>([])
const dishOptions = ref<{ label: string; value: number; name: string }[]>([])
const query = reactive({
  range: undefined as [Dayjs, Dayjs] | undefined,
  keyword: '',
  mealType: undefined as string | undefined,
  status: undefined as string | undefined,
  pageNo: 1,
  pageSize: 10
})
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const editOpen = ref(false)
const saving = ref(false)
const form = reactive({
  id: undefined as number | undefined,
  recipeName: '',
  mealType: undefined as string | undefined,
  planDate: undefined as Dayjs | undefined,
  dishIdList: [] as number[],
  dishNames: '',
  suitableCrowd: '',
  status: DINING_STATUS.enabled,
  remark: ''
})

const columns = [
  { title: '食谱名称', dataIndex: 'recipeName', key: 'recipeName' },
  { title: '餐次', dataIndex: 'mealType', key: 'mealType', width: 120 },
  { title: '计划日期', dataIndex: 'planDate', key: 'planDate', width: 120 },
  { title: '菜品清单', dataIndex: 'dishNames', key: 'dishNames' },
  { title: '适用人群', dataIndex: 'suitableCrowd', key: 'suitableCrowd', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 140 }
]

async function fetchData() {
  loading.value = true
  try {
    const params: any = {
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      keyword: query.keyword,
      mealType: query.mealType,
      status: query.status
    }
    if (query.range) {
      params.dateFrom = query.range[0].format('YYYY-MM-DD')
      params.dateTo = query.range[1].format('YYYY-MM-DD')
    }
    const res: PageResult<DiningRecipe> = await getDiningRecipePage(params)
    rows.value = res.list
    pagination.total = res.total || res.list.length
  } finally {
    loading.value = false
  }
}

const mealTypeLabel = getDiningMealTypeLabel

function handleTableChange(pag: any) {
  query.pageNo = pag.current
  query.pageSize = pag.pageSize
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchData()
}

function onReset() {
  query.range = undefined
  query.keyword = ''
  query.mealType = undefined
  query.status = undefined
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

async function loadDishOptions() {
  const list = await getDiningDishList({ mealType: form.mealType, status: DINING_STATUS.enabled })
  dishOptions.value = (list || []).map((item: any) => ({
    label: `${item.dishName} ¥${item.unitPrice || 0}`,
    value: Number(item.id),
    name: item.dishName
  }))
}

function onMealTypeChange() {
  form.dishIdList = []
  form.dishNames = ''
  loadDishOptions()
}

function onDishChange(values: number[]) {
  const selected = dishOptions.value.filter((item) => values.includes(item.value))
  form.dishNames = selected.map((item) => item.name).join(',')
}

async function openCreate() {
  form.id = undefined
  form.recipeName = ''
  form.mealType = DINING_MEAL_TYPES.lunch
  form.planDate = undefined
  form.dishIdList = []
  form.dishNames = ''
  form.suitableCrowd = ''
  form.status = DINING_STATUS.enabled
  form.remark = ''
  await loadDishOptions()
  editOpen.value = true
}

async function openEdit(record: DiningRecipe) {
  form.id = record.id
  form.recipeName = record.recipeName
  form.mealType = record.mealType || DINING_MEAL_TYPES.lunch
  form.planDate = record.planDate ? dayjs(record.planDate) : undefined
  form.dishNames = record.dishNames
  await loadDishOptions()
  form.dishIdList = (record.dishIds || '')
    .split(',')
    .map((item) => Number(item.trim()))
    .filter((item) => !!item)
  form.suitableCrowd = record.suitableCrowd || ''
  form.status = record.status || DINING_STATUS.enabled
  form.remark = record.remark || ''
  editOpen.value = true
}

async function submit() {
  if (!form.recipeName.trim()) {
    message.error(DINING_MESSAGES.requiredRecipeName)
    return
  }
  if (!form.mealType) {
    message.error(DINING_MESSAGES.requiredMealType)
    return
  }
  if (form.dishIdList.length === 0) {
    message.error(DINING_MESSAGES.requiredDishSelection)
    return
  }
  if (!form.dishNames) {
    message.error(DINING_MESSAGES.requiredRecipeFields)
    return
  }
  saving.value = true
  try {
    const payload = {
      recipeName: form.recipeName.trim(),
      mealType: form.mealType,
      planDate: form.planDate ? dayjs(form.planDate).format('YYYY-MM-DD') : undefined,
      dishNames: form.dishNames,
      dishIds: form.dishIdList.join(','),
      suitableCrowd: form.suitableCrowd.trim() || undefined,
      status: form.status,
      remark: form.remark.trim() || undefined
    }
    if (form.id) {
      await updateDiningRecipe(form.id, payload)
    } else {
      await createDiningRecipe(payload)
    }
    editOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function remove(record: DiningRecipe) {
  await deleteDiningRecipe(record.id)
  fetchData()
}

fetchData()
</script>
