<template>
  <PageContainer title="照护任务看板" subTitle="按长者聚焦今日任务、异常任务与执行进度">
    <template #extra>
      <a-space>
        <a-date-picker v-model:value="dateValue" value-format="YYYY-MM-DD" @change="reload" />
        <a-button @click="go('/care/exception')">异常任务</a-button>
        <a-button type="primary" @click="go('/care/today')">进入今日任务</a-button>
      </a-space>
    </template>

    <a-row :gutter="[12, 12]">
      <a-col v-for="item in summaryCards" :key="item.title" :xs="12" :lg="6">
        <a-card class="card-elevated" :bordered="false" size="small">
          <a-statistic :title="item.title" :value="item.value" />
          <div class="stat-desc">{{ item.desc }}</div>
        </a-card>
      </a-col>
    </a-row>

    <a-alert
      v-if="signedLinkageContext.active"
      style="margin: 12px 0;"
      type="success"
      show-icon
      :message="signedLinkageContext.message"
      :description="signedLinkageContext.description"
    >
      <template #action>
        <a-space wrap>
          <a-button size="small" @click="go('/medical-care/inspection')">首次巡检</a-button>
          <a-button size="small" @click="go('/elder/resident-360')">长者360</a-button>
        </a-space>
      </template>
    </a-alert>

    <a-card class="card-elevated" :bordered="false">
      <StatefulBlock :loading="loading" :error="errorMessage" :empty="!rows.length" empty-text="暂无照护任务" @retry="reload">
        <a-table :columns="columns" :data-source="rows" :pagination="false" row-key="taskDailyId" :row-class-name="resolveRowClassName">
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'status'">
              <a-tag :color="statusColor(record.status)">{{ record.status || '-' }}</a-tag>
            </template>
            <template v-else-if="column.key === 'flags'">
              <a-space>
                <a-tag v-if="record.overdueFlag" color="red">超时</a-tag>
                <a-tag v-if="record.suspiciousFlag" color="orange">疑似异常</a-tag>
                <span v-if="!record.overdueFlag && !record.suspiciousFlag">-</span>
              </a-space>
            </template>
          </template>
        </a-table>
      </StatefulBlock>
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import PageContainer from '../../../components/PageContainer.vue'
import StatefulBlock from '../../../components/StatefulBlock.vue'
import { getTaskPage, getTaskSummary } from '../../../api/care'
import { normalizeId, normalizeResidentId } from '../../../utils/id'
import type { CareTaskItem, CareTaskSummary, Id } from '../../../types'
import { resolveCareError } from '../careError'

const route = useRoute()
const router = useRouter()
const residentId = computed<Id | undefined>(() => normalizeResidentId(route.query as Record<string, unknown>))
const activeTaskId = computed<Id | undefined>(() => normalizeId(route.query.taskId))
const filter = computed(() => String(route.query.filter || 'all'))
const initialDate = String(route.query.date || '')
const dateValue = ref(initialDate === 'today' || !initialDate ? dayjs().format('YYYY-MM-DD') : initialDate)

const loading = ref(false)
const errorMessage = ref('')
const rows = ref<CareTaskItem[]>([])
const summary = ref<CareTaskSummary>({})

const columns = [
  { title: '长者', dataIndex: 'elderName', key: 'elderName', width: 100 },
  { title: '房间', dataIndex: 'roomNo', key: 'roomNo', width: 90 },
  { title: '任务', dataIndex: 'taskName', key: 'taskName' },
  { title: '计划时间', dataIndex: 'planTime', key: 'planTime', width: 170 },
  { title: '执行护工', dataIndex: 'staffName', key: 'staffName', width: 100 },
  { title: '状态', key: 'status', width: 90 },
  { title: '标记', key: 'flags', width: 140 }
]

const summaryCards = computed(() => [
  { title: '任务总数', value: summary.value.totalCount || 0, desc: '当日任务池' },
  { title: '待执行', value: summary.value.pendingCount || 0, desc: '含待分配与执行中' },
  { title: '已完成', value: summary.value.doneCount || 0, desc: `完成率 ${Math.round((summary.value.completionRate || 0) * 100)}%` },
  { title: '异常/超时', value: (summary.value.exceptionCount || 0) + (summary.value.overdueCount || 0), desc: '优先处理' }
])
const signedLinkageContext = computed(() => {
  const source = routeQueryText(route.query.source).toLowerCase()
  const entryScene = routeQueryText(route.query.entryScene).toLowerCase()
  const residentName = routeQueryText(route.query.residentName)
  const active = !!residentId.value && (source === 'contract_signed' || entryScene === 'signed_onboarding')
  return {
    active,
    message: active ? `当前为新签约长者 ${residentName || '当前长者'} 的护理交接任务` : '',
    description: active ? '已默认聚焦今日照护任务，请先确认交接、执行安排和责任人。' : ''
  }
})

function statusColor(status?: string) {
  if (status === 'DONE') return 'green'
  if (status === 'EXCEPTION') return 'red'
  if (status === 'PENDING') return 'orange'
  return 'default'
}

function go(path: string) {
  const [pathname, search] = path.split('?')
  const parsed = new URLSearchParams(search || '')
  const query: Record<string, any> = {}
  parsed.forEach((value, key) => {
    query[key] = value
  })
  const resident = route.query.residentId ?? route.query.elderId
  if (resident != null && query.residentId == null && query.elderId == null) {
    query.residentId = resident
    query.elderId = resident
  }
  router.push({ path: pathname, query })
}

function routeQueryText(value: unknown) {
  return typeof value === 'string' ? value.trim() : ''
}

function resolveRowClassName(record: CareTaskItem) {
  if (activeTaskId.value && String(record.taskDailyId || '') === String(activeTaskId.value)) {
    return 'task-row-focus'
  }
  return ''
}

async function reload() {
  loading.value = true
  errorMessage.value = ''
  try {
    const status = filter.value === 'overdue_or_missed' ? 'PENDING' : undefined
    const overdueOnly = filter.value === 'overdue_or_missed' ? true : undefined
    const [page, sum] = await Promise.all([
      getTaskPage({ pageNo: 1, pageSize: 50, date: dateValue.value, elderId: residentId.value, status, overdueOnly }),
      getTaskSummary({ date: dateValue.value, elderId: residentId.value, status, overdueOnly })
    ])
    rows.value = page.list || []
    summary.value = sum || {}
  } catch (error) {
    errorMessage.value = resolveCareError(error, '加载照护任务失败')
    message.error(errorMessage.value)
  } finally {
    loading.value = false
  }
}

onMounted(reload)

watch(
  () => [route.query.residentId, route.query.elderId, route.query.taskId, route.query.date, route.query.filter],
  () => {
    reload()
  }
)
</script>

<style scoped>
.stat-desc {
  margin-top: 4px;
  color: var(--muted);
  font-size: 12px;
}
:deep(.task-row-focus > td) {
  background: #e6f4ff !important;
}
</style>
