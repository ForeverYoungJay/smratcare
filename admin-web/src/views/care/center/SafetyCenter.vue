<template>
  <PageContainer class="rm-page" title="交接班与安全" sub-title="重点长者、夜间情况、已发现风险在交接中逐项交代，交接双方各自确认；异常统一上报、分级处置、复核闭环。">
    <div class="rm-kpis">
      <button v-for="card in kpis" :key="card.key" type="button" class="rm-kpi" @click="go(card.tab)">
        <span class="rm-kpi__icon" :class="card.tone && `rm-kpi__icon--${card.tone}`"><component :is="card.icon" /></span>
        <span class="rm-kpi__label">{{ card.label }}</span>
        <span class="rm-kpi__value">{{ card.value ?? '--' }}<small>{{ card.unit }}</small></span>
        <span class="rm-kpi__hint">{{ card.hint }}</span>
      </button>
    </div>

    <section class="rm-card">
      <a-tabs :active-key="tab" @change="(key: string | number) => go(String(key))">
        <a-tab-pane v-for="item in SAFETY_TABS" :key="item.key" :tab="item.label" />
      </a-tabs>
      <a-alert v-if="tab === 'handover'" class="sc-note" type="info" show-icon message="交班人提交「已交接」后，由接班人本人在列表点「接班确认」；只有双方都确认才算完整交接。" />
      <a-alert v-if="tab === 'patrol'" class="sc-note" type="info" show-icon message="巡视频次按机构制度和个别风险照护计划设置；巡视发现的问题可直接在「异常事件」上报。" />
      <EmbeddedPage :key="tab">
        <Handovers v-if="tab === 'handover'" />
        <Inspection v-else-if="tab === 'patrol'" />
        <IncidentBoard v-else-if="tab === 'incident'" @changed="loadSummary" />
        <Exception v-else />
      </EmbeddedPage>
    </section>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import dayjs from 'dayjs'
import { useRoute, useRouter } from 'vue-router'
import { AlertOutlined, CheckCircleOutlined, SafetyOutlined, SwapOutlined } from '@ant-design/icons-vue'
import PageContainer from '../../../components/PageContainer.vue'
import EmbeddedPage from '../../marketing/center/EmbeddedPage.vue'
import Handovers from '../Handovers.vue'
import Inspection from '../../health/Inspection.vue'
import Exception from '../Exception.vue'
import IncidentBoard from './IncidentBoard.vue'
import { getIncidentSummary, getShiftHandoverPage } from '../../../api/nursing'
import type { CareIncidentSummary } from '../../../types'
import { SAFETY_TABS, resolveTab } from './nursingModel'
import '../../elder/residentUi.css'

const route = useRoute()
const router = useRouter()
const tab = computed(() => resolveTab(SAFETY_TABS, route.query))
const incidents = ref<CareIncidentSummary | null>(null)
const handoverTotal = ref<number>()
const handoverWaiting = ref<number>()

const kpis = computed(() => [
  { key: 'handover', label: '今日交接记录', value: handoverTotal.value, unit: '次', hint: `待接班确认 ${handoverWaiting.value ?? '--'} 次`, icon: SwapOutlined, tab: 'handover' },
  { key: 'incident', label: '本月异常事件', value: incidents.value?.monthTotal, unit: '件', hint: `高风险未闭环 ${incidents.value?.openHighCount ?? '--'} 件`, icon: AlertOutlined, tone: 'danger', tab: 'incident' },
  { key: 'review', label: '处理中 / 待复核', value: incidents.value ? `${incidents.value.processingCount} / ${incidents.value.pendingReviewCount}` : undefined, unit: '件', hint: '复核由授权人员结案', icon: SafetyOutlined, tone: 'warning', tab: 'incident' },
  { key: 'closed', label: '本月已闭环', value: incidents.value?.closedCount, unit: '件', hint: '处置与复核均已完成', icon: CheckCircleOutlined, tab: 'incident' }
])

function go(next: string) { router.replace({ path: route.path, query: { tab: next } }) }

async function loadSummary() {
  const today = dayjs().format('YYYY-MM-DD')
  const [inc, all, waiting] = await Promise.allSettled([
    getIncidentSummary(dayjs().format('YYYY-MM')),
    getShiftHandoverPage({ pageNo: 1, pageSize: 1, dateFrom: today, dateTo: today }),
    getShiftHandoverPage({ pageNo: 1, pageSize: 1, dateFrom: today, dateTo: today, status: 'HANDED_OVER' })
  ])
  incidents.value = inc.status === 'fulfilled' ? inc.value : null
  handoverTotal.value = all.status === 'fulfilled' ? all.value.total : undefined
  handoverWaiting.value = waiting.status === 'fulfilled' ? waiting.value.total : undefined
}

onMounted(loadSummary)
</script>

<style scoped>
.sc-note { margin-bottom: 12px; }
</style>
