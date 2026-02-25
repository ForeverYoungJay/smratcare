<template>
  <PageContainer title="食谱管理" subTitle="维护餐次食谱和适用人群">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键词">
        <a-input v-model:value="query.keyword" placeholder="食谱名称/菜品" allow-clear />
      </a-form-item>
      <a-form-item label="餐次">
        <a-select v-model:value="query.mealType" :options="mealOptions" allow-clear style="width: 160px" />
      </a-form-item>
      <template #extra>
        <a-button type="primary" @click="openCreate">新增食谱</a-button>
      </template>
    </SearchForm>

    <DataTable rowKey="id" :columns="columns" :data-source="rows" :loading="loading" :pagination="pagination" @change="handleTableChange">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'action'">
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
          <a-select v-model:value="form.mealType" :options="mealOptions" />
        </a-form-item>
        <a-form-item label="计划日期">
          <a-date-picker v-model:value="form.planDate" style="width: 100%" />
        </a-form-item>
        <a-form-item label="菜品清单" required>
          <a-input v-model:value="form.dishNames" placeholder="如：南瓜粥,蒸蛋" />
        </a-form-item>
        <a-form-item label="菜品ID集合">
          <a-input v-model:value="form.dishIds" placeholder="如：1001,1002" />
        </a-form-item>
        <a-form-item label="适用人群">
          <a-input v-model:value="form.suitableCrowd" />
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
import { getDiningRecipePage, createDiningRecipe, updateDiningRecipe, deleteDiningRecipe } from '../../api/dining'
import type { DiningRecipe, PageResult } from '../../types'

const mealOptions = [
  { label: '早餐', value: 'BREAKFAST' },
  { label: '午餐', value: 'LUNCH' },
  { label: '晚餐', value: 'DINNER' },
  { label: '加餐', value: 'SNACK' }
]

const loading = ref(false)
const rows = ref<DiningRecipe[]>([])
const query = reactive({ keyword: '', mealType: undefined as string | undefined, pageNo: 1, pageSize: 10 })
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const editOpen = ref(false)
const saving = ref(false)
const form = reactive({
  id: undefined as number | undefined,
  recipeName: '',
  mealType: undefined as string | undefined,
  planDate: undefined as Dayjs | undefined,
  dishNames: '',
  dishIds: '',
  suitableCrowd: '',
  remark: ''
})

const columns = [
  { title: '食谱名称', dataIndex: 'recipeName', key: 'recipeName' },
  { title: '餐次', dataIndex: 'mealType', key: 'mealType', width: 120 },
  { title: '计划日期', dataIndex: 'planDate', key: 'planDate', width: 120 },
  { title: '菜品清单', dataIndex: 'dishNames', key: 'dishNames' },
  { title: '适用人群', dataIndex: 'suitableCrowd', key: 'suitableCrowd', width: 120 },
  { title: '操作', key: 'action', width: 140 }
]

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<DiningRecipe> = await getDiningRecipePage({ ...query })
    rows.value = res.list
    pagination.total = res.total || res.list.length
  } finally {
    loading.value = false
  }
}

function handleTableChange(pag: any) {
  query.pageNo = pag.current
  query.pageSize = pag.pageSize
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchData()
}

function onReset() {
  query.keyword = ''
  query.mealType = undefined
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

function openCreate() {
  form.id = undefined
  form.recipeName = ''
  form.mealType = undefined
  form.planDate = undefined
  form.dishNames = ''
  form.dishIds = ''
  form.suitableCrowd = ''
  form.remark = ''
  editOpen.value = true
}

function openEdit(record: DiningRecipe) {
  form.id = record.id
  form.recipeName = record.recipeName
  form.mealType = record.mealType
  form.planDate = record.planDate ? dayjs(record.planDate) : undefined
  form.dishNames = record.dishNames
  form.dishIds = record.dishIds || ''
  form.suitableCrowd = record.suitableCrowd || ''
  form.remark = record.remark || ''
  editOpen.value = true
}

async function submit() {
  if (!form.recipeName || !form.dishNames) {
    message.error('请填写完整信息')
    return
  }
  saving.value = true
  try {
    const payload = {
      recipeName: form.recipeName,
      mealType: form.mealType,
      planDate: form.planDate ? dayjs(form.planDate).format('YYYY-MM-DD') : undefined,
      dishNames: form.dishNames,
      dishIds: form.dishIds,
      suitableCrowd: form.suitableCrowd,
      remark: form.remark
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
