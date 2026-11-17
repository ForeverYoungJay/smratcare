<template>
  <PageContainer class="rm-page" title="护理任务中心" sub-title="护理服务安排、执行、记录集中在一个页面，护理员按班次操作；点「执行」在右侧抽屉里记录。">
    <div class="rm-kpis">
      <button v-for="card in kpis" :key="card.key" type="button" class="rm-kpi" @click="go('today', card.query)">
        <span class="rm-kpi__icon" :class="card.tone && `rm-kpi__icon--${card.tone}`"><component :is="card.icon" /></span>
        <span class="rm-kpi__label">{{ card.label }}</span>
        <span class="rm-kpi__value">{{ card.value ?? '--' }}<small>项</small></span>
        <span class="rm-kpi__hint">{{ card.hint }}</span>
      </button>
    </div>

    <section class="rm-card">
      <a-tabs :active-key="tab" @change="(key: string | number) => go(String(key))">
        <a-tab-pane v-for="item in TASK_TABS" :key="item.key" :tab="item.label" />
      </a-tabs>
      <EmbeddedPage>
        <Today v-if="tab === 'today'" :key="String(route.query.metric || '')" />
        <TaskBoard v-else-if="tab === 'calendar'" />
        <NursingRecords v-else-if="tab === 'records'" />
        <NursingReports v-else />
      </EmbeddedPage>
    </section>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import dayjs from 'dayjs'
import { useRoute, useRouter } from 'vue-router'
import { CheckCircleOutlined, ClockCircleOutlined, ContainerOutlined, WarningOutlined } from '@ant-design/icons-vue'
import PageContainer from '../../../components/PageContainer.vue'
import EmbeddedPage from '../../marketing/center/EmbeddedPage.vue'
import Today from '../Today.vue'
import TaskBoard from '../workbench/TaskBoard.vue'
import NursingRecords from '../NursingRecords.vue'
import NursingReports from '../NursingReports.vue'
import { getTaskSummary } from '../../../api/care'
import type { CareTaskSummary } from '../../../types'
import { TASK_TABS, resolveTab } from './nursingModel'
import '../../elder/residentUi.css'

const route = useRoute()
const router = useRouter()
const tab = computed(() => resolveTab(TASK_TABS, route.query))
const summary = ref<CareTaskSummary | null>(null)

const kpis = computed((): Array<{ key: string; label: string; value?: number; hint: string; icon: unknown; tone?: string; query: Record<string, string> }> => {
  const s = summary.value
  return [
    { key: 'total', label: '今日总任务', value: s?.totalCount, hint: `未分配 ${s?.unassignedCount ?? '--'} 项`, icon: ContainerOutlined, query: {} },
    { key: 'done', label: '已完成任务', value: s?.doneCount, hint: `完成率 ${s?.completionRate ?? '--'}%`, icon: CheckCircleOutlined, query: { metric: 'done' } },
    { key: 'pending', label: '待执行任务', value: s?.pendingCount, hint: '按计划时间执行', icon: ClockCircleOutlined, tone: 'info', query: { metric: 'pending' } },
    { key: 'overdue', label: '超时 / 异常', value: s ? Number(s.overdueCount ?? 0) + Number(s.exceptionCount ?? 0) : undefined, hint: '未完成必须写明原因', icon: WarningOutlined, tone: 'danger', query: { metric: 'overdue' } }
  ]
})

// 换标签时只保留 tab，避免把上一个标签的筛选条件带进下一个嵌入页
function go(next: string, query: Record<string, string> = {}) {
  router.replace({ path: route.path, query: { tab: next, ...query } })
}

onMounted(async () => {
  const today = dayjs().format('YYYY-MM-DD')
  summary.value = await getTaskSummary({ dateFrom: today, dateTo: today }).catch(() => null)
})
</script>
