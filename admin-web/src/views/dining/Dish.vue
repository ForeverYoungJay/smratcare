<template>
  <PageContainer title="菜品管理" subTitle="维护餐饮基础菜品库">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键词">
        <a-input v-model:value="query.keyword" placeholder="菜品名称/分类" allow-clear />
      </a-form-item>
      <a-form-item label="餐次">
        <a-select v-model:value="query.mealType" :options="mealOptions" allow-clear style="width: 160px" />
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="query.status" :options="statusOptions" allow-clear style="width: 140px" />
      </a-form-item>
      <template #extra>
        <a-button type="primary" @click="openCreate">新增菜品</a-button>
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
            <a-button type="link" @click="toggleStatus(record)">{{ record.status === DINING_STATUS.enabled ? '停用' : '启用' }}</a-button>
            <a-button type="link" danger @click="remove(record)">删除</a-button>
          </a-space>
        </template>
      </template>
    </DataTable>

    <a-modal v-model:open="editOpen" title="菜品信息" :confirm-loading="saving" @ok="submit">
      <a-form layout="vertical">
        <a-form-item label="菜品名称" required>
          <a-input v-model:value="form.dishName" />
        </a-form-item>
        <a-form-item label="菜品分类">
          <a-input v-model:value="form.dishCategory" />
        </a-form-item>
        <a-form-item label="餐次">
          <a-select v-model:value="form.mealType" :options="mealOptions" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="form.status" :options="statusOptions" />
        </a-form-item>
        <a-form-item label="单价(元)" required>
          <a-input-number v-model:value="form.unitPrice" :min="0" :precision="2" style="width: 100%" />
        </a-form-item>
        <a-form-item label="热量(kcal)">
          <a-input-number v-model:value="form.calories" :min="0" style="width: 100%" />
        </a-form-item>
        <a-form-item label="过敏原">
          <a-input v-model:value="form.allergenTags" placeholder="如：牛奶,坚果" />
        </a-form-item>
        <a-form-item label="禁忌标签">
          <a-select
            v-model:value="form.tagIds"
            mode="multiple"
            :options="tagOptions"
            placeholder="选择标签用于风险拦截"
          />
        </a-form-item>
        <a-form-item label="营养信息">
          <a-input v-model:value="form.nutritionInfo" />
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
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import {
  DINING_ENABLE_STATUS_OPTIONS,
  DINING_MEAL_TYPE_OPTIONS,
  DINING_MESSAGES,
  DINING_STATUS,
  getDiningEnableStatusLabel,
  getDiningMealTypeLabel
} from '../../constants/dining'
import { getProductTagList } from '../../api/store'
import {
  getDiningDishPage,
  createDiningDish,
  updateDiningDish,
  updateDiningDishStatus,
  deleteDiningDish
} from '../../api/dining'
import type { DiningDish, PageResult } from '../../types'

const mealOptions = DINING_MEAL_TYPE_OPTIONS
const statusOptions = DINING_ENABLE_STATUS_OPTIONS

const loading = ref(false)
const rows = ref<DiningDish[]>([])
const tagOptions = ref<{ label: string; value: string }[]>([])
const query = reactive({
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
  dishName: '',
  dishCategory: '',
  mealType: undefined as string | undefined,
  unitPrice: 0,
  calories: undefined as number | undefined,
  allergenTags: '',
  tagIds: [] as string[],
  nutritionInfo: '',
  status: DINING_STATUS.enabled,
  remark: ''
})

const columns = [
  { title: '菜品名称', dataIndex: 'dishName', key: 'dishName' },
  { title: '分类', dataIndex: 'dishCategory', key: 'dishCategory', width: 120 },
  { title: '餐次', dataIndex: 'mealType', key: 'mealType', width: 120 },
  { title: '单价', dataIndex: 'unitPrice', key: 'unitPrice', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '备注', dataIndex: 'remark', key: 'remark', width: 180 },
  { title: '操作', key: 'action', width: 220 }
]

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<DiningDish> = await getDiningDishPage({ ...query })
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
  query.keyword = ''
  query.mealType = undefined
  query.status = undefined
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

function openCreate() {
  form.id = undefined
  form.dishName = ''
  form.dishCategory = ''
  form.mealType = undefined
  form.unitPrice = 0
  form.calories = undefined
  form.allergenTags = ''
  form.tagIds = []
  form.nutritionInfo = ''
  form.status = DINING_STATUS.enabled
  form.remark = ''
  editOpen.value = true
}

function openEdit(record: DiningDish) {
  form.id = record.id
  form.dishName = record.dishName
  form.dishCategory = record.dishCategory || ''
  form.mealType = record.mealType
  form.unitPrice = record.unitPrice || 0
  form.calories = record.calories
  form.allergenTags = record.allergenTags || ''
  form.tagIds = (record.tagIds || '')
    .split(',')
    .map((item) => item.trim())
    .filter((item) => !!item)
  form.nutritionInfo = record.nutritionInfo || ''
  form.status = record.status || DINING_STATUS.enabled
  form.remark = record.remark || ''
  editOpen.value = true
}

async function submit() {
  if (!form.dishName.trim()) {
    message.error(DINING_MESSAGES.requiredDishName)
    return
  }
  if (form.unitPrice < 0) {
    message.error(DINING_MESSAGES.invalidDishPrice)
    return
  }
  saving.value = true
  try {
    const payload = {
      dishName: form.dishName.trim(),
      dishCategory: form.dishCategory.trim() || undefined,
      mealType: form.mealType,
      unitPrice: form.unitPrice,
      calories: form.calories,
      allergenTags: form.allergenTags.trim() || undefined,
      tagIds: form.tagIds.join(','),
      nutritionInfo: form.nutritionInfo.trim() || undefined,
      status: form.status,
      remark: form.remark.trim() || undefined
    }
    if (form.id) {
      await updateDiningDish(form.id, payload)
    } else {
      await createDiningDish(payload)
    }
    editOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function toggleStatus(record: DiningDish) {
  await updateDiningDishStatus(record.id, record.status === DINING_STATUS.enabled ? DINING_STATUS.disabled : DINING_STATUS.enabled)
  fetchData()
}

async function remove(record: DiningDish) {
  await deleteDiningDish(record.id)
  fetchData()
}

fetchData()

getProductTagList().then((list) => {
  tagOptions.value = (list || []).map((item: any) => ({
    label: `${item.tagName}(${item.tagCode})`,
    value: String(item.id)
  }))
})
</script>
