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

    <a-card class="card-elevated" :bordered="false">
      <StatefulBlock :loading="loading" :error="errorMessage" :empty="!rows.length" empty-text="暂无照护任务" @retry="reload">
        <a-table :columns="columns" :data-source="rows" :pagination="false" row-key="taskDailyId">
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
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import PageContainer from '../../../components/PageContainer.vue'
import StatefulBlock from '../../../components/StatefulBlock.vue'
import { getTaskPage, getTaskSummary } from '../../../api/care'
import type { CareTaskItem, CareTaskSummary } from '../../../types'

const route = useRoute()
const router = useRouter()
const residentId = computed(() => Number(route.query.residentId || 0) || undefined)
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

function statusColor(status?: string) {
  if (status === 'DONE') return 'green'
  if (status === 'EXCEPTION') return 'red'
  if (status === 'PENDING') return 'orange'
  return 'default'
}

function go(path: string) {
  router.push(path)
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
  } catch (error: any) {
    errorMessage.value = error?.message || '加载照护任务失败'
    message.error(errorMessage.value)
  } finally {
    loading.value = false
  }
}

onMounted(reload)
</script>

<style scoped>
.stat-desc {
  margin-top: 4px;
  color: var(--muted);
  font-size: 12px;
}
</style>
