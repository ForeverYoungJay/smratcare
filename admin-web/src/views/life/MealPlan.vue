<template>
  <PageContainer title="膳食计划" subTitle="按日期维护每日菜单">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="日期范围">
        <a-range-picker v-model:value="query.range" />
      </a-form-item>
      <a-form-item label="关键词">
        <a-input v-model:value="query.keyword" placeholder="菜品/餐次" allow-clear />
      </a-form-item>
      <template #extra>
        <a-button type="primary" @click="openCreate">新增菜单</a-button>
      </template>
    </SearchForm>

    <DataTable
      rowKey="id"
      :columns="columns"
      :data-source="rows"
      :loading="loading"
      :pagination="pagination"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="openEdit(record)">编辑</a-button>
            <a-button type="link" danger @click="remove(record)">删除</a-button>
          </a-space>
        </template>
      </template>
    </DataTable>

    <a-modal v-model:open="editOpen" title="膳食计划" @ok="submit" :confirm-loading="saving">
      <a-form layout="vertical">
        <a-form-item label="日期" required>
          <a-date-picker v-model:value="form.planDate" style="width: 100%" />
        </a-form-item>
        <a-form-item label="餐次" required>
          <a-select v-model:value="form.mealType" :options="mealOptions" />
        </a-form-item>
        <a-form-item label="菜单" required>
          <a-textarea v-model:value="form.menu" :rows="3" />
        </a-form-item>
        <a-form-item label="热量">
          <a-input-number v-model:value="form.calories" style="width: 100%" />
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
import { getMealPlanPage, createMealPlan, updateMealPlan, deleteMealPlan } from '../../api/life'
import type { MealPlan, PageResult } from '../../types'

const loading = ref(false)
const rows = ref<MealPlan[]>([])
const query = reactive({
  range: undefined as [Dayjs, Dayjs] | undefined,
  keyword: '',
  pageNo: 1,
  pageSize: 10
})
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

const columns = [
  { title: '日期', dataIndex: 'planDate', key: 'planDate', width: 120 },
  { title: '餐次', dataIndex: 'mealType', key: 'mealType', width: 120 },
  { title: '菜单', dataIndex: 'menu', key: 'menu' },
  { title: '热量', dataIndex: 'calories', key: 'calories', width: 100 },
  { title: '备注', dataIndex: 'remark', key: 'remark', width: 160 },
  { title: '操作', key: 'action', width: 140 }
]

const editOpen = ref(false)
const saving = ref(false)
const form = reactive({
  id: undefined as number | undefined,
  planDate: dayjs(),
  mealType: 'BREAKFAST',
  menu: '',
  calories: undefined as number | undefined,
  remark: ''
})
const mealOptions = [
  { label: '早餐', value: 'BREAKFAST' },
  { label: '午餐', value: 'LUNCH' },
  { label: '晚餐', value: 'DINNER' },
  { label: '加餐', value: 'SNACK' }
]

async function fetchData() {
  loading.value = true
  try {
    const params: any = {
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      keyword: query.keyword
    }
    if (query.range) {
      params.dateFrom = query.range[0].format('YYYY-MM-DD')
      params.dateTo = query.range[1].format('YYYY-MM-DD')
    }
    const res: PageResult<MealPlan> = await getMealPlanPage(params)
    rows.value = res.list
    pagination.total = res.total || res.list.length
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
  query.range = undefined
  query.keyword = ''
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

function openCreate() {
  form.id = undefined
  form.planDate = dayjs()
  form.mealType = 'BREAKFAST'
  form.menu = ''
  form.calories = undefined
  form.remark = ''
  editOpen.value = true
}

function openEdit(record: MealPlan) {
  form.id = record.id
  form.planDate = dayjs(record.planDate)
  form.mealType = record.mealType
  form.menu = record.menu
  form.calories = record.calories
  form.remark = record.remark || ''
  editOpen.value = true
}

async function submit() {
  if (!form.menu) {
    message.error('请输入菜单')
    return
  }
  saving.value = true
  try {
    const payload = {
      planDate: dayjs(form.planDate).format('YYYY-MM-DD'),
      mealType: form.mealType,
      menu: form.menu,
      calories: form.calories,
      remark: form.remark
    }
    if (form.id) {
      await updateMealPlan(form.id, payload)
    } else {
      await createMealPlan(payload)
    }
    editOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function remove(record: MealPlan) {
  await deleteMealPlan(record.id)
  fetchData()
}

fetchData()
</script>
