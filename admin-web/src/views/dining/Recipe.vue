<template>
  <PageContainer title="食谱管理" subTitle="支持单条维护，也支持按周期生成早中晚排餐课表">
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
        <a-space>
          <a-button @click="generatePlannerBoard">刷新排餐课表</a-button>
          <a-button type="primary" @click="openCreate">新增食谱</a-button>
        </a-space>
      </template>
    </SearchForm>

    <a-card class="planner-card card-elevated" :bordered="false">
      <div class="planner-hero">
        <div>
          <p class="planner-eyebrow">Multi-Day Planner</p>
          <h2>每日食谱课表</h2>
          <p class="planner-copy">
            先选周期，再直接在早餐、午餐、晚餐单元格里勾菜品，系统会自动生成每日清单并支持批量保存。
          </p>
        </div>
        <div class="planner-hero-stats">
          <div class="planner-stat">
            <span>{{ plannerRows.length }}</span>
            <small>排餐天数</small>
          </div>
          <div class="planner-stat">
            <span>{{ plannerFilledCount }}</span>
            <small>已配置餐次</small>
          </div>
          <div class="planner-stat">
            <span>{{ plannerPendingCount }}</span>
            <small>待补全餐次</small>
          </div>
        </div>
      </div>

      <div class="planner-toolbar">
        <a-form layout="vertical" class="planner-toolbar-form">
          <a-form-item label="排餐日程">
            <a-select v-model:value="planner.preset" :options="plannerPresetOptions" @change="applyPlannerPreset" />
          </a-form-item>
          <a-form-item label="日期范围">
            <a-range-picker v-model:value="planner.range" />
          </a-form-item>
          <a-form-item label="适用人群（批量）">
            <a-input v-model:value="planner.suitableCrowd" placeholder="如：普餐、糖尿病餐、流食" allow-clear />
          </a-form-item>
          <a-form-item label="备注（批量）">
            <a-input v-model:value="planner.remark" placeholder="会在批量保存时写入已编辑餐次" allow-clear />
          </a-form-item>
          <a-form-item label="状态">
            <a-select v-model:value="planner.status" :options="statusOptions" />
          </a-form-item>
        </a-form>
        <div class="planner-actions">
          <a-button @click="generatePlannerBoard" :loading="planner.loading">生成清单</a-button>
          <a-button @click="fillEmptyWithPreviousDay" :disabled="plannerRows.length === 0">沿用前一天</a-button>
          <a-button type="primary" @click="savePlannerBoard" :loading="planner.saving" :disabled="plannerRows.length === 0">
            批量保存多日食谱
          </a-button>
        </div>
      </div>

      <div class="planner-tip">
        <span>说明：保留单元格为空表示该餐次暂不排餐；若清空已存在餐次并保存，会同步删除该日该餐次食谱。</span>
      </div>

      <a-table
        class="planner-table"
        :columns="plannerColumns"
        :data-source="plannerRows"
        :pagination="false"
        :loading="planner.loading"
        :scroll="{ x: 1180 }"
        size="middle"
        row-key="dateKey"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'date'">
            <div class="date-cell">
              <strong>{{ record.dateLabel }}</strong>
              <span>{{ record.weekdayLabel }}</span>
            </div>
          </template>
          <template v-else-if="isPlannerMealColumn(column.key)">
            <div class="meal-cell" :class="{ populated: record[column.key].dishIdList.length > 0 }">
              <div class="meal-cell-head">
                <span>{{ getDiningMealTypeLabel(column.key) }}</span>
                <a-tag v-if="record[column.key].id" color="blue">已存在</a-tag>
                <a-tag v-else color="default">新排餐</a-tag>
              </div>
              <a-select
                mode="multiple"
                :value="record[column.key].dishIdList"
                :options="dishOptionsByMeal[column.key]"
                placeholder="选择菜品"
                :max-tag-count="2"
                allow-clear
                @change="(values) => onPlannerDishChange(record.dateKey, column.key, values)"
              />
              <div class="meal-cell-summary">
                <strong>{{ record[column.key].dishNames || '未选择菜品' }}</strong>
                <span>{{ record[column.key].recipeName }}</span>
              </div>
            </div>
          </template>
        </template>
      </a-table>
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
import { computed, reactive, ref } from 'vue'
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
import type { DiningRecipe, Id, PageResult } from '../../types'

type PlannerMealKey = 'BREAKFAST' | 'LUNCH' | 'DINNER'

type DishOption = {
  label: string
  value: Id
  name: string
}

type PlannerCell = {
  id?: Id
  recipeName: string
  mealType: PlannerMealKey
  dishIdList: Id[]
  dishNames: string
  suitableCrowd?: string
  remark?: string
  status: string
}

type PlannerRow = {
  dateKey: string
  dateLabel: string
  weekdayLabel: string
  BREAKFAST: PlannerCell
  LUNCH: PlannerCell
  DINNER: PlannerCell
}

const mealOptions = DINING_MEAL_TYPE_OPTIONS
const statusOptions = DINING_ENABLE_STATUS_OPTIONS
const plannerMealKeys: PlannerMealKey[] = ['BREAKFAST', 'LUNCH', 'DINNER']

const loading = ref(false)
const rows = ref<DiningRecipe[]>([])
const dishOptions = ref<DishOption[]>([])
const dishOptionsByMeal = reactive<Record<PlannerMealKey, DishOption[]>>({
  BREAKFAST: [],
  LUNCH: [],
  DINNER: []
})

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
  id: undefined as Id | undefined,
  recipeName: '',
  mealType: undefined as string | undefined,
  planDate: undefined as Dayjs | undefined,
  dishIdList: [] as Id[],
  dishNames: '',
  suitableCrowd: '',
  status: DINING_STATUS.enabled,
  remark: ''
})

const planner = reactive({
  preset: 'THIS_WEEK',
  range: [dayjs().startOf('week'), dayjs().startOf('week').add(6, 'day')] as [Dayjs, Dayjs],
  suitableCrowd: '',
  remark: '',
  status: DINING_STATUS.enabled,
  loading: false,
  saving: false
})
const plannerRows = ref<PlannerRow[]>([])

const columns = [
  { title: '食谱名称', dataIndex: 'recipeName', key: 'recipeName' },
  { title: '餐次', dataIndex: 'mealType', key: 'mealType', width: 120 },
  { title: '计划日期', dataIndex: 'planDate', key: 'planDate', width: 120 },
  { title: '菜品清单', dataIndex: 'dishNames', key: 'dishNames' },
  { title: '适用人群', dataIndex: 'suitableCrowd', key: 'suitableCrowd', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 140 }
]

const plannerColumns = [
  { title: '日期', key: 'date', dataIndex: 'dateLabel', width: 140, fixed: 'left' },
  { title: '早餐', key: 'BREAKFAST', width: 330 },
  { title: '午餐', key: 'LUNCH', width: 330 },
  { title: '晚餐', key: 'DINNER', width: 330 }
]

const plannerPresetOptions = [
  { label: '本周 7 天', value: 'THIS_WEEK' },
  { label: '下周 7 天', value: 'NEXT_WEEK' },
  { label: '未来 14 天', value: 'NEXT_14_DAYS' },
  { label: '本月剩余', value: 'REST_OF_MONTH' }
]

const plannerFilledCount = computed(() =>
  plannerRows.value.reduce((total, row) => {
    return total + plannerMealKeys.filter((mealType) => row[mealType].dishIdList.length > 0).length
  }, 0)
)

const plannerPendingCount = computed(() => Math.max(plannerRows.value.length * plannerMealKeys.length - plannerFilledCount.value, 0))

const mealTypeLabel = getDiningMealTypeLabel

function buildPageParams(pageNo = query.pageNo, pageSize = query.pageSize) {
  const params: any = {
    pageNo,
    pageSize,
    keyword: query.keyword,
    mealType: query.mealType,
    status: query.status
  }
  if (query.range) {
    params.dateFrom = query.range[0].format('YYYY-MM-DD')
    params.dateTo = query.range[1].format('YYYY-MM-DD')
  }
  return params
}

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<DiningRecipe> = await getDiningRecipePage(buildPageParams())
    rows.value = res.list
    pagination.total = res.total || res.list.length
    pagination.current = query.pageNo
    pagination.pageSize = query.pageSize
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
  query.range = undefined
  query.keyword = ''
  query.mealType = undefined
  query.status = undefined
  query.pageNo = 1
  query.pageSize = 10
  pagination.current = 1
  pagination.pageSize = 10
  fetchData()
}

async function loadDishOptions() {
  const list = await getDiningDishList({ mealType: form.mealType, status: DINING_STATUS.enabled })
  dishOptions.value = mapDishOptions(list)
}

async function ensurePlannerDishOptions() {
  const result = await Promise.all(
    plannerMealKeys.map(async (mealType) => {
      const list = await getDiningDishList({ mealType, status: DINING_STATUS.enabled })
      dishOptionsByMeal[mealType] = mapDishOptions(list)
    })
  )
  return result
}

function mapDishOptions(list: any[]) {
  return (list || []).map((item: any) => ({
    label: `${item.dishName} ¥${item.unitPrice || 0}`,
    value: Number.isNaN(Number(item.id)) ? String(item.id) : Number(item.id),
    name: item.dishName
  }))
}

function onMealTypeChange() {
  form.dishIdList = []
  form.dishNames = ''
  loadDishOptions()
}

function onDishChange(values: Id[]) {
  form.dishNames = resolveDishNames(values, dishOptions.value)
}

function openCreate() {
  form.id = undefined
  form.recipeName = ''
  form.mealType = DINING_MEAL_TYPES.lunch
  form.planDate = undefined
  form.dishIdList = []
  form.dishNames = ''
  form.suitableCrowd = ''
  form.status = DINING_STATUS.enabled
  form.remark = ''
  loadDishOptions()
  editOpen.value = true
}

async function openEdit(record: DiningRecipe) {
  form.id = record.id
  form.recipeName = record.recipeName
  form.mealType = record.mealType || DINING_MEAL_TYPES.lunch
  form.planDate = record.planDate ? dayjs(record.planDate) : undefined
  form.dishNames = record.dishNames
  await loadDishOptions()
  form.dishIdList = parseDishIds(record.dishIds)
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
    await Promise.all([fetchData(), generatePlannerBoard()])
  } finally {
    saving.value = false
  }
}

async function remove(record: DiningRecipe) {
  await deleteDiningRecipe(record.id)
  await Promise.all([fetchData(), generatePlannerBoard()])
}

function applyPlannerPreset(value: string) {
  const base = dayjs()
  if (value === 'THIS_WEEK') {
    planner.range = [base.startOf('week'), base.startOf('week').add(6, 'day')]
    return
  }
  if (value === 'NEXT_WEEK') {
    const start = base.startOf('week').add(7, 'day')
    planner.range = [start, start.add(6, 'day')]
    return
  }
  if (value === 'NEXT_14_DAYS') {
    planner.range = [base, base.add(13, 'day')]
    return
  }
  planner.range = [base, base.endOf('month')]
}

async function generatePlannerBoard() {
  if (!planner.range || planner.range.length !== 2) {
    message.warning('请先选择排餐日期范围')
    return
  }
  planner.loading = true
  try {
    await ensurePlannerDishOptions()
    const existingRecords = await getPlannerRecipes()
    plannerRows.value = buildPlannerRows(existingRecords)
  } finally {
    planner.loading = false
  }
}

async function getPlannerRecipes() {
  const res = await getDiningRecipePage({
    pageNo: 1,
    pageSize: 500,
    dateFrom: planner.range[0].format('YYYY-MM-DD'),
    dateTo: planner.range[1].format('YYYY-MM-DD')
  })
  return (res.list || []).filter((item) => isPlannerMealColumn(item.mealType))
}

function buildPlannerRows(records: DiningRecipe[]) {
  const recordMap = new Map<string, DiningRecipe>()
  records.forEach((item) => {
    if (!item.planDate || !isPlannerMealColumn(item.mealType)) {
      return
    }
    const key = `${item.planDate}_${item.mealType}`
    if (!recordMap.has(key)) {
      recordMap.set(key, item)
    }
  })

  const dates = collectDates(planner.range[0], planner.range[1])
  return dates.map((currentDate) => {
    const dateKey = currentDate.format('YYYY-MM-DD')
    return {
      dateKey,
      dateLabel: currentDate.format('MM-DD'),
      weekdayLabel: getWeekdayLabel(currentDate.day()),
      BREAKFAST: createPlannerCell(dateKey, 'BREAKFAST', recordMap.get(`${dateKey}_BREAKFAST`)),
      LUNCH: createPlannerCell(dateKey, 'LUNCH', recordMap.get(`${dateKey}_LUNCH`)),
      DINNER: createPlannerCell(dateKey, 'DINNER', recordMap.get(`${dateKey}_DINNER`))
    }
  })
}

function createPlannerCell(dateKey: string, mealType: PlannerMealKey, recipe?: DiningRecipe): PlannerCell {
  return {
    id: recipe?.id,
    recipeName: recipe?.recipeName || `${dateKey} ${getDiningMealTypeLabel(mealType)}食谱`,
    mealType,
    dishIdList: parseDishIds(recipe?.dishIds),
    dishNames: recipe?.dishNames || '',
    suitableCrowd: recipe?.suitableCrowd,
    remark: recipe?.remark,
    status: recipe?.status || planner.status
  }
}

function collectDates(start: Dayjs, end: Dayjs) {
  const dates: Dayjs[] = []
  let cursor = start.startOf('day')
  const last = end.startOf('day')
  while (cursor.isBefore(last) || cursor.isSame(last, 'day')) {
    dates.push(cursor)
    cursor = cursor.add(1, 'day')
  }
  return dates
}

function getWeekdayLabel(day: number) {
  return ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][day] || ''
}

function parseDishIds(value?: string) {
  return (value || '')
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)
    .map((item) => {
      const parsed = Number(item)
      return Number.isNaN(parsed) ? item : parsed
    })
}

function resolveDishNames(values: Id[], options: DishOption[]) {
  return options
    .filter((item) => values.some((value) => String(value) === String(item.value)))
    .map((item) => item.name)
    .join('、')
}

function onPlannerDishChange(dateKey: string, mealType: PlannerMealKey, values: Id[]) {
  const row = plannerRows.value.find((item) => item.dateKey === dateKey)
  if (!row) {
    return
  }
  const cell = row[mealType]
  cell.dishIdList = values
  cell.dishNames = resolveDishNames(values, dishOptionsByMeal[mealType] || [])
  if (!cell.recipeName.trim()) {
    cell.recipeName = `${dateKey} ${getDiningMealTypeLabel(mealType)}食谱`
  }
}

function fillEmptyWithPreviousDay() {
  plannerRows.value.forEach((row, index) => {
    if (index === 0) {
      return
    }
    const previous = plannerRows.value[index - 1]
    plannerMealKeys.forEach((mealType) => {
      const currentCell = row[mealType]
      const previousCell = previous[mealType]
      if (currentCell.dishIdList.length === 0 && previousCell.dishIdList.length > 0) {
        currentCell.dishIdList = [...previousCell.dishIdList]
        currentCell.dishNames = previousCell.dishNames
        currentCell.recipeName = `${row.dateKey} ${getDiningMealTypeLabel(mealType)}食谱`
      }
    })
  })
  message.success('空白餐次已按前一天菜品补齐，可继续微调')
}

async function savePlannerBoard() {
  const tasks = plannerRows.value.flatMap((row) =>
    plannerMealKeys.map((mealType) => ({
      row,
      mealType,
      cell: row[mealType]
    }))
  )
  if (tasks.every((item) => item.cell.dishIdList.length === 0 && !item.cell.id)) {
    message.warning('请至少为一个餐次选择菜品')
    return
  }
  planner.saving = true
  try {
    for (const task of tasks) {
      const { row, mealType, cell } = task
      if (cell.dishIdList.length === 0) {
        if (cell.id) {
          await deleteDiningRecipe(cell.id)
        }
        continue
      }
      const payload = {
        recipeName: cell.recipeName.trim() || `${row.dateKey} ${getDiningMealTypeLabel(mealType)}食谱`,
        mealType,
        planDate: row.dateKey,
        dishIds: cell.dishIdList.join(','),
        dishNames: cell.dishNames,
        suitableCrowd: planner.suitableCrowd.trim() || cell.suitableCrowd || undefined,
        status: planner.status || cell.status,
        remark: planner.remark.trim() || cell.remark || undefined
      }
      if (cell.id) {
        await updateDiningRecipe(cell.id, payload)
      } else {
        const created = await createDiningRecipe(payload)
        cell.id = created.id
      }
    }
    message.success('多日食谱已批量保存')
    await Promise.all([fetchData(), generatePlannerBoard()])
  } finally {
    planner.saving = false
  }
}

function isPlannerMealColumn(value?: string): value is PlannerMealKey {
  return value === 'BREAKFAST' || value === 'LUNCH' || value === 'DINNER'
}

fetchData()
generatePlannerBoard()
</script>

<style scoped>
.planner-card {
  position: relative;
  overflow: hidden;
  background:
    radial-gradient(circle at top right, rgba(255, 208, 141, 0.28), transparent 26%),
    linear-gradient(135deg, rgba(255, 250, 241, 0.98), rgba(247, 242, 233, 0.98));
  border: 1px solid rgba(154, 99, 42, 0.08);
}

.planner-hero {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  align-items: flex-start;
  margin-bottom: 18px;
}

.planner-eyebrow {
  margin: 0 0 6px;
  font-size: 12px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: #9a632a;
}

.planner-hero h2 {
  margin: 0;
  font-size: 28px;
  line-height: 1.1;
  color: #3d2b1f;
}

.planner-copy {
  max-width: 720px;
  margin: 8px 0 0;
  color: #74533a;
}

.planner-hero-stats {
  display: grid;
  grid-template-columns: repeat(3, minmax(108px, 1fr));
  gap: 10px;
  min-width: 360px;
}

.planner-stat {
  padding: 14px 16px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.68);
  border: 1px solid rgba(154, 99, 42, 0.1);
  box-shadow: 0 14px 30px rgba(88, 52, 22, 0.08);
}

.planner-stat span {
  display: block;
  font-size: 28px;
  font-weight: 700;
  color: #2f2118;
}

.planner-stat small {
  color: #86654c;
}

.planner-toolbar {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 220px;
  gap: 16px;
  margin-bottom: 14px;
}

.planner-toolbar-form {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 12px;
}

.planner-toolbar-form :deep(.ant-form-item) {
  margin-bottom: 0;
}

.planner-actions {
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  gap: 10px;
}

.planner-tip {
  margin-bottom: 14px;
  padding: 10px 14px;
  color: #7a583e;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.62);
  border: 1px dashed rgba(154, 99, 42, 0.24);
}

.planner-table :deep(.ant-table-container) {
  border-radius: 20px;
  overflow: hidden;
}

.planner-table :deep(.ant-table-thead > tr > th) {
  background: #4b3427;
  color: #fff7f0;
  border-bottom: none;
  font-size: 13px;
}

.planner-table :deep(.ant-table-tbody > tr > td) {
  vertical-align: top;
  background: rgba(255, 255, 255, 0.78);
}

.date-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.date-cell strong {
  font-size: 18px;
  color: #362419;
}

.date-cell span {
  color: #86654c;
}

.meal-cell {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-height: 132px;
  padding: 12px;
  border-radius: 16px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.94), rgba(249, 243, 236, 0.86));
  border: 1px solid rgba(154, 99, 42, 0.08);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.meal-cell.populated {
  box-shadow: 0 16px 28px rgba(95, 58, 25, 0.08);
}

.meal-cell:hover {
  transform: translateY(-2px);
}

.meal-cell-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  color: #5c412d;
  font-weight: 600;
}

.meal-cell-summary {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.meal-cell-summary strong {
  color: #2f2118;
  font-size: 14px;
  line-height: 1.5;
}

.meal-cell-summary span {
  color: #8e6a50;
  font-size: 12px;
}

@media (max-width: 1200px) {
  .planner-toolbar-form {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 992px) {
  .planner-hero {
    flex-direction: column;
  }

  .planner-hero-stats {
    min-width: 0;
    width: 100%;
  }

  .planner-toolbar {
    grid-template-columns: 1fr;
  }

  .planner-actions {
    flex-direction: row;
    flex-wrap: wrap;
  }
}

@media (max-width: 768px) {
  .planner-toolbar-form {
    grid-template-columns: 1fr;
  }

  .planner-hero h2 {
    font-size: 24px;
  }

  .planner-hero-stats {
    grid-template-columns: 1fr;
  }
}
</style>
