<template>
  <PageContainer title="会员生日" subTitle="生日提醒与关怀安排">
    <a-row :gutter="[12, 12]" style="margin-bottom: 10px;">
      <a-col :xs="24" :sm="8">
        <a-card class="card-elevated" :bordered="false" size="small" @click="quickDays(0)">
          <a-statistic title="今日生日" :value="todayCount" />
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="8">
        <a-card class="card-elevated" :bordered="false" size="small" @click="quickDays(7)">
          <a-statistic title="未来7天" :value="next7Count" />
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="8">
        <a-card class="card-elevated" :bordered="false" size="small" @click="quickMonth(currentMonth)">
          <a-statistic title="本月生日" :value="monthCount" />
        </a-card>
      </a-col>
    </a-row>

    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键词">
        <ElderNameAutocomplete v-model:value="query.keyword" placeholder="老人姓名(编号)" width="220px" />
      </a-form-item>
      <a-form-item label="月份">
        <a-select v-model:value="query.month" :options="monthOptions" allow-clear style="width: 140px" />
      </a-form-item>
      <a-form-item label="未来天数">
        <a-input-number v-model:value="query.daysAhead" :min="0" :max="365" style="width: 160px" />
      </a-form-item>
      <a-form-item label="年龄下限">
        <a-input-number v-model:value="query.minAge" :min="0" :max="130" style="width: 140px" />
      </a-form-item>
      <template #extra>
        <a-space wrap>
          <a-button @click="quickMonth(currentMonth)">本月清单</a-button>
          <a-button @click="quickDays(7)">未来7天</a-button>
          <a-button @click="quickAge(80)">80+统计</a-button>
          <a-button @click="quickAge(90)">90+统计</a-button>
          <a-button @click="exportCsv">导出</a-button>
          <a-button @click="printYearPlan">打印年度表</a-button>
        </a-space>
      </template>
    </SearchForm>

    <DataTable
      rowKey="elderId"
      :columns="columns"
      :data-source="rows"
      :loading="loading"
      :pagination="pagination"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'actions'">
          <a-space size="small">
            <a @click="goElderDetail(record)">老人档案</a>
            <a @click="createBirthdayActivity(record)">创建活动</a>
            <a @click="openBirthdayMaterial(record)">物资准备</a>
          </a-space>
        </template>
      </template>
    </DataTable>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import dayjs from 'dayjs'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import ElderNameAutocomplete from '../../components/ElderNameAutocomplete.vue'
import { getBirthdayPage } from '../../api/life'
import { useLiveSyncRefresh } from '../../composables/useLiveSyncRefresh'
import type { BirthdayReminder, PageResult } from '../../types'

const loading = ref(false)
const rows = ref<BirthdayReminder[]>([])
const route = useRoute()
const router = useRouter()
const currentMonth = dayjs().month() + 1
const query = reactive({
  keyword: '',
  month: undefined as number | undefined,
  daysAhead: 30 as number | undefined,
  minAge: undefined as number | undefined,
  pageNo: 1,
  pageSize: 10
})
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const columns = [
  { title: '老人姓名', dataIndex: 'elderName', key: 'elderName', width: 120 },
  { title: '出生日期', dataIndex: 'birthDate', key: 'birthDate', width: 120 },
  { title: '下次生日', dataIndex: 'nextBirthday', key: 'nextBirthday', width: 120 },
  { title: '剩余天数', dataIndex: 'daysUntil', key: 'daysUntil', width: 100 },
  { title: '届时年龄', dataIndex: 'ageOnNextBirthday', key: 'ageOnNextBirthday', width: 100 },
  { title: '房间号', dataIndex: 'roomNo', key: 'roomNo', width: 120 },
  { title: '床位号', dataIndex: 'bedNo', key: 'bedNo', width: 120 },
  { title: '操作', key: 'actions', width: 280 }
]
const monthOptions = Array.from({ length: 12 }).map((_, i) => ({ label: `${i + 1}月`, value: i + 1 }))
const todayCount = computed(() => rows.value.filter((item) => Number(item.daysUntil || 9999) === 0).length)
const next7Count = computed(() => rows.value.filter((item) => Number(item.daysUntil || 9999) >= 0 && Number(item.daysUntil || 9999) <= 7).length)
const monthCount = computed(() => rows.value.filter((item) => {
  if (!item.nextBirthday) return false
  return dayjs(item.nextBirthday).month() + 1 === currentMonth
}).length)

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<BirthdayReminder> = await getBirthdayPage({
      keyword: query.keyword || undefined,
      month: query.month,
      daysAhead: query.daysAhead,
      minAge: query.minAge,
      pageNo: query.pageNo,
      pageSize: query.pageSize
    })
    rows.value = (res.list || []).filter((item) => {
      if (!query.minAge) return true
      return Number(item.ageOnNextBirthday || 0) >= Number(query.minAge || 0)
    })
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
  query.keyword = ''
  query.month = undefined
  query.daysAhead = 30
  query.minAge = undefined
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

function quickMonth(month: number) {
  query.month = month
  query.daysAhead = undefined
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

function quickDays(days: number) {
  query.daysAhead = Math.max(0, days)
  query.month = undefined
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

function quickAge(age: number) {
  query.minAge = age
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

function goElderDetail(record: BirthdayReminder) {
  if (record.elderId != null) {
    router.push(`/elder/detail/${record.elderId}`)
    return
  }
  router.push(`/elder/list?keyword=${encodeURIComponent(record.elderName || '')}`)
}

function createBirthdayActivity(record: BirthdayReminder) {
  router.push(`/life/activity?quick=create&elderName=${encodeURIComponent(record.elderName || '')}`)
}

function openBirthdayMaterial(record: BirthdayReminder) {
  router.push(`/inventory/outbound?scene=birthday&elderName=${encodeURIComponent(record.elderName || '')}`)
}

function exportCsv() {
  if (!rows.value.length) {
    message.info('暂无可导出数据')
    return
  }
  const header = ['姓名', '出生日期', '下次生日', '剩余天数', '届时年龄', '房间', '床位']
  const body = rows.value.map((item) => [item.elderName, item.birthDate, item.nextBirthday, item.daysUntil, item.ageOnNextBirthday, item.roomNo, item.bedNo])
  const csv = [header, ...body]
    .map((row) => row.map((cell) => `"${String(cell ?? '').replace(/"/g, '""')}"`).join(','))
    .join('\n')
  const blob = new Blob([`\uFEFF${csv}`], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `生日清单_${dayjs().format('YYYYMMDD')}.csv`
  a.click()
  URL.revokeObjectURL(url)
}

function printYearPlan() {
  if (!rows.value.length) {
    message.info('暂无可打印数据')
    return
  }
  const lines = rows.value
    .map((item, index) => `<tr><td>${index + 1}</td><td>${item.elderName || '-'}</td><td>${item.nextBirthday || '-'}</td><td>${item.ageOnNextBirthday || '-'}</td><td>${item.roomNo || '-'}/${item.bedNo || '-'}</td></tr>`)
    .join('')
  const html = `<!doctype html><html><head><meta charset="utf-8"><title>生日清单</title><style>body{font-family:'PingFang SC',Arial;padding:20px;}table{width:100%;border-collapse:collapse;}th,td{border:1px solid #ddd;padding:8px;font-size:12px;}th{background:#f7f7f7;}</style></head><body><h2>生日清单（本页筛选结果）</h2><table><thead><tr><th>#</th><th>姓名</th><th>下次生日</th><th>年龄</th><th>房间/床位</th></tr></thead><tbody>${lines}</tbody></table></body></html>`
  const win = window.open('', '_blank')
  if (!win) return
  win.document.write(html)
  win.document.close()
  win.focus()
  win.print()
}

function applyRouteQuery() {
  const month = Number(route.query.month || NaN)
  const daysAhead = Number(route.query.daysAhead || NaN)
  const minAge = Number(route.query.minAge || NaN)
  if (Number.isFinite(month) && month >= 1 && month <= 12) query.month = month
  if (Number.isFinite(daysAhead) && daysAhead >= 0 && daysAhead <= 365) query.daysAhead = daysAhead
  if (Number.isFinite(minAge) && minAge >= 0) query.minAge = minAge
}

watch(
  () => route.query,
  () => {
    applyRouteQuery()
    fetchData()
  }
)

onMounted(() => {
  applyRouteQuery()
  fetchData()
})

useLiveSyncRefresh({
  topics: ['elder', 'oa', 'hr'],
  refresh: () => {
    fetchData().catch(() => {})
  },
  debounceMs: 800
})
</script>
