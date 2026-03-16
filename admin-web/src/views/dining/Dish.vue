<template>
  <PageContainer title="菜品管理" subTitle="分类折叠查看菜品、本月制作次数与采购计划">
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
        <a-space>
          <a-button @click="fetchAnalytics" :loading="analyticsLoading">刷新月度联动</a-button>
          <a-button type="primary" @click="openCreate">新增菜品</a-button>
        </a-space>
      </template>
    </SearchForm>

    <a-card class="analytics-card card-elevated" :bordered="false">
      <div class="analytics-head">
        <div>
          <p class="analytics-eyebrow">Dish Linkage</p>
          <h2>分类与餐次折叠总览</h2>
          <p class="analytics-copy">点击分类可直接查看品名、本月制作次数、当前用餐人数和计划采购量，食谱排班变化会自动联动这里。</p>
        </div>
        <div class="analytics-month">
          <a-date-picker v-model:value="analyticsMonth" picker="month" @change="fetchAnalytics" />
        </div>
      </div>

      <a-row :gutter="16" class="analytics-stats">
        <a-col :xs="24" :md="6">
          <div class="analytics-stat">
            <span>{{ analytics.totalDishCount }}</span>
            <small>菜品数</small>
          </div>
        </a-col>
        <a-col :xs="24" :md="6">
          <div class="analytics-stat">
            <span>{{ analytics.totalRecipeCount }}</span>
            <small>本月制作次数</small>
          </div>
        </a-col>
        <a-col :xs="24" :md="6">
          <div class="analytics-stat">
            <span>{{ formatNumber(analytics.totalPlannedPurchaseQty) }}</span>
            <small>本月采购总量</small>
          </div>
        </a-col>
        <a-col :xs="24" :md="6">
          <div class="analytics-stat">
            <span>{{ formatCurrency(analytics.totalEstimatedCost) }}</span>
            <small>本月采购估算</small>
          </div>
        </a-col>
      </a-row>

      <a-collapse v-model:activeKey="activeCategoryKeys" class="category-collapse">
        <a-collapse-panel
          v-for="category in groupedAnalytics"
          :key="category.key"
          :header="`${category.label} · ${category.items.length}道菜 · 制作${category.recipeCount}次`"
        >
          <a-collapse class="meal-collapse">
            <a-collapse-panel
              v-for="meal in category.meals"
              :key="`${category.key}_${meal.key}`"
              :header="`${meal.label} · ${meal.items.length}道菜 · 制作${meal.recipeCount}次`"
            >
              <a-table
                :columns="analyticsColumns"
                :data-source="meal.items"
                :pagination="false"
                size="small"
                row-key="id"
                :scroll="{ x: 900 }"
              />
            </a-collapse-panel>
          </a-collapse>
        </a-collapse-panel>
      </a-collapse>

      <div class="analytics-note">
        满意度联动位已预留在分类展开场景中；当前仓库暂无菜品级满意度来源，所以这次先接通制作次数和采购联动。
      </div>
    </a-card>

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
          <a-input v-model:value="form.dishCategory" placeholder="如：荤菜、素菜、主食、汤品" />
        </a-form-item>
        <a-form-item label="餐次">
          <a-select v-model:value="form.mealType" :options="mealOptions" />
        </a-form-item>
        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="单价(元)" required>
              <a-input-number v-model:value="form.unitPrice" :min="0" :precision="2" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="现行用餐人数">
              <a-input-number v-model:value="form.currentDiningCount" :min="0" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="单次采购用量">
              <a-input-number v-model:value="form.purchaseQty" :min="0" :precision="2" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="采购单位">
              <a-input v-model:value="form.purchaseUnit" placeholder="如：斤、箱、袋" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="状态">
          <a-select v-model:value="form.status" :options="statusOptions" />
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
import { computed, reactive, ref } from 'vue'
import dayjs from 'dayjs'
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
  deleteDiningDish,
  getDiningDishAnalytics
} from '../../api/dining'
import type { DiningDish, DiningDishAnalyticsItem, DiningDishAnalyticsResponse, PageResult } from '../../types'

const mealOptions = DINING_MEAL_TYPE_OPTIONS
const statusOptions = DINING_ENABLE_STATUS_OPTIONS

const loading = ref(false)
const rows = ref<DiningDish[]>([])
const tagOptions = ref<{ label: string; value: string }[]>([])
const analyticsLoading = ref(false)
const analyticsMonth = ref(dayjs())
const analytics = reactive<DiningDishAnalyticsResponse>({
  month: '',
  totalDishCount: 0,
  totalRecipeCount: 0,
  totalPlannedPurchaseQty: 0,
  totalEstimatedCost: 0,
  items: []
})
const activeCategoryKeys = ref<string[]>([])

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
  currentDiningCount: 0,
  purchaseQty: 0,
  purchaseUnit: '斤',
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
  { title: '用餐人数', dataIndex: 'currentDiningCount', key: 'currentDiningCount', width: 110 },
  { title: '单次采购', key: 'purchaseQty', width: 140, customRender: ({ record }: any) => formatPurchase(record.purchaseQty, record.purchaseUnit) },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 220 }
]

const analyticsColumns = [
  { title: '菜品', dataIndex: 'dishName', key: 'dishName', width: 140 },
  { title: '本月制作次数', dataIndex: 'monthlyRecipeCount', key: 'monthlyRecipeCount', width: 120 },
  { title: '本月点餐次数', dataIndex: 'monthlyOrderCount', key: 'monthlyOrderCount', width: 120 },
  { title: '现行用餐人数', dataIndex: 'currentDiningCount', key: 'currentDiningCount', width: 120 },
  { title: '单次采购用量', key: 'purchaseQty', width: 140, customRender: ({ record }: any) => formatPurchase(record.purchaseQty, record.purchaseUnit) },
  { title: '月采购量', key: 'monthlyPlannedPurchaseQty', width: 140, customRender: ({ record }: any) => formatPurchase(record.monthlyPlannedPurchaseQty, record.purchaseUnit) },
  { title: '月采购估算', key: 'monthlyEstimatedCost', width: 130, customRender: ({ record }: any) => formatCurrency(record.monthlyEstimatedCost) }
]

const groupedAnalytics = computed(() => {
  const map = new Map<string, { key: string; label: string; items: DiningDishAnalyticsItem[] }>()
  analytics.items.forEach((item) => {
    const key = (item.dishCategory || '未分类').trim() || '未分类'
    const hit = map.get(key) || { key, label: key, items: [] }
    hit.items.push(item)
    map.set(key, hit)
  })
  return Array.from(map.values()).map((category) => {
    const mealMap = new Map<string, { key: string; label: string; items: DiningDishAnalyticsItem[]; recipeCount: number }>()
    category.items.forEach((item) => {
      const mealKey = item.mealType || 'UNKNOWN'
      const mealHit = mealMap.get(mealKey) || {
        key: mealKey,
        label: getDiningMealTypeLabel(mealKey),
        items: [],
        recipeCount: 0
      }
      mealHit.items.push(item)
      mealHit.recipeCount += item.monthlyRecipeCount || 0
      mealMap.set(mealKey, mealHit)
    })
    return {
      ...category,
      recipeCount: category.items.reduce((sum, item) => sum + (item.monthlyRecipeCount || 0), 0),
      meals: Array.from(mealMap.values())
    }
  })
})

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

async function fetchAnalytics() {
  analyticsLoading.value = true
  try {
    const res = await getDiningDishAnalytics({
      month: analyticsMonth.value ? analyticsMonth.value.startOf('month').format('YYYY-MM-DD') : undefined
    })
    analytics.month = res.month
    analytics.totalDishCount = res.totalDishCount || 0
    analytics.totalRecipeCount = res.totalRecipeCount || 0
    analytics.totalPlannedPurchaseQty = res.totalPlannedPurchaseQty || 0
    analytics.totalEstimatedCost = res.totalEstimatedCost || 0
    analytics.items = res.items || []
    activeCategoryKeys.value = groupedAnalytics.value.map((item) => item.key)
  } finally {
    analyticsLoading.value = false
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
  form.currentDiningCount = 0
  form.purchaseQty = 0
  form.purchaseUnit = '斤'
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
  form.currentDiningCount = record.currentDiningCount || 0
  form.purchaseQty = record.purchaseQty || 0
  form.purchaseUnit = record.purchaseUnit || '斤'
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
      currentDiningCount: form.currentDiningCount || 0,
      purchaseQty: form.purchaseQty || 0,
      purchaseUnit: form.purchaseUnit.trim() || '斤',
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
    await Promise.all([fetchData(), fetchAnalytics()])
  } finally {
    saving.value = false
  }
}

async function toggleStatus(record: DiningDish) {
  await updateDiningDishStatus(record.id, record.status === DINING_STATUS.enabled ? DINING_STATUS.disabled : DINING_STATUS.enabled)
  await Promise.all([fetchData(), fetchAnalytics()])
}

async function remove(record: DiningDish) {
  await deleteDiningDish(record.id)
  await Promise.all([fetchData(), fetchAnalytics()])
}

function formatPurchase(value?: number, unit?: string) {
  if (!value) {
    return `0${unit || '斤'}`
  }
  return `${formatNumber(value)}${unit || '斤'}`
}

function formatNumber(value?: number) {
  return Number(value || 0).toFixed(2).replace(/\.00$/, '')
}

function formatCurrency(value?: number) {
  return `${Number(value || 0).toFixed(2)}元`
}

fetchData()
fetchAnalytics()

getProductTagList()
  .then((list) => {
    tagOptions.value = (list || []).map((item: any) => ({ label: item.name, value: String(item.id) }))
  })
  .catch(() => {
    tagOptions.value = []
  })
</script>

<style scoped>
.analytics-card {
  background:
    radial-gradient(circle at top right, rgba(255, 224, 173, 0.24), transparent 28%),
    linear-gradient(135deg, #fffaf3, #f8f2e8);
  border: 1px solid rgba(148, 101, 42, 0.08);
}

.analytics-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.analytics-eyebrow {
  margin: 0 0 6px;
  color: #9a632a;
  text-transform: uppercase;
  letter-spacing: 0.18em;
  font-size: 12px;
}

.analytics-head h2 {
  margin: 0;
  color: #3b281a;
}

.analytics-copy {
  margin: 8px 0 0;
  color: #73523b;
}

.analytics-stats {
  margin-bottom: 16px;
}

.analytics-stat {
  padding: 14px 16px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.76);
  border: 1px solid rgba(148, 101, 42, 0.12);
}

.analytics-stat span {
  display: block;
  font-size: 28px;
  font-weight: 700;
  color: #2f2118;
}

.analytics-stat small {
  color: #88674d;
}

.category-collapse,
.meal-collapse {
  background: transparent;
}

.category-collapse :deep(.ant-collapse-item),
.meal-collapse :deep(.ant-collapse-item) {
  border-radius: 14px;
  overflow: hidden;
  border: 1px solid rgba(148, 101, 42, 0.12);
  margin-bottom: 12px;
}

.category-collapse :deep(.ant-collapse-header),
.meal-collapse :deep(.ant-collapse-header) {
  background: rgba(255, 255, 255, 0.72);
  color: #4b3427;
  font-weight: 600;
}

.analytics-note {
  margin-top: 12px;
  color: #826148;
}

@media (max-width: 768px) {
  .analytics-head {
    flex-direction: column;
  }
}
</style>
