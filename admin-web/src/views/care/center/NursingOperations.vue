<template>
  <PageContainer class="rm-page" title="排班与护理质量" sub-title="护士长在同一页面完成人员调配、工作负荷检查、质量分析和整改跟踪；指标仅作质量改进参考，不单独作为绩效结论。">
    <template #extra>
      <a-space wrap>
        <a-range-picker v-model:value="range" value-format="YYYY-MM-DD" :allow-clear="false" @change="loadMetrics" />
        <a-button @click="exportMetrics">导出指标</a-button>
      </a-space>
    </template>

    <div class="rm-kpis">
      <button v-for="m in metrics" :key="m.key" type="button" class="rm-kpi" @click="router.push(METRIC_DRILLDOWN[m.key] || route.fullPath)">
        <span class="rm-kpi__label">{{ m.label }}</span>
        <span class="rm-kpi__value">{{ formatRate(m) }}</span>
        <span class="rm-kpi__hint">{{ m.numerator }} / {{ m.denominator }} · 点击查看明细</span>
      </button>
    </div>

    <section class="rm-card">
      <a-tabs :active-key="tab" @change="(key: string | number) => go(String(key))">
        <a-tab-pane v-for="item in OPERATIONS_TABS" :key="item.key" :tab="item.label" />
      </a-tabs>

      <template v-if="tab === 'roster'">
        <div class="no-check">
          <span class="rm-section-title">本周排班检查（{{ week[0] }} — {{ week[6] }}）</span>
          <span v-if="rosterLoading" class="no-muted">检查中…</span>
          <template v-else>
            <span v-if="!shiftCodeOptions.length" class="rm-pill rm-pill--warning">未配置班次模板，无法检查岗位空缺</span>
            <span v-else-if="!roster.duplicates.length && !roster.gaps.length" class="rm-pill rm-pill--success">未发现空缺或重复分配</span>
            <span v-for="d in roster.duplicates" :key="`d${d.staffName}${d.dutyDate}`" class="rm-pill rm-pill--danger">{{ d.dutyDate.slice(5) }} {{ d.staffName }} 重复：{{ d.shifts.join('、') }}</span>
            <span v-for="g in roster.gaps.slice(0, 12)" :key="`g${g.dutyDate}${g.shiftCode}`" class="rm-pill rm-pill--warning">{{ g.dutyDate.slice(5) }} {{ shiftName(g.shiftCode) }} 无人</span>
            <span v-if="roster.gaps.length > 12" class="no-muted">另有 {{ roster.gaps.length - 12 }} 处空缺</span>
          </template>
        </div>
        <EmbeddedPage><ShiftCalendar /></EmbeddedPage>
      </template>

      <a-table v-else-if="tab === 'workload'" row-key="staffId" :data-source="workloads" :columns="workloadColumns" :pagination="false" size="middle">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'rate'">
            <a-progress :percent="record.taskCount ? Math.round((record.doneCount / record.taskCount) * 100) : 0" size="small" />
          </template>
          <template v-else-if="column.key === 'action'">
            <a class="rm-link" @click="router.push(`/care/tasks?tab=today&staffId=${record.staffId}&dateFrom=${range[0]}&dateTo=${range[1]}`)">查看任务</a>
          </template>
        </template>
      </a-table>

      <div v-else-if="tab === 'quality'" class="no-quality">
        <div v-for="m in metrics" :key="m.key" class="no-bar">
          <span>{{ m.label }}</span>
          <a-progress :percent="m.rate ?? 0" :show-info="false" :stroke-color="m.rate != null && m.rate < 90 ? '#d9822b' : '#1f9d74'" />
          <strong>{{ formatRate(m) }}</strong>
          <a class="rm-link" @click="router.push(METRIC_DRILLDOWN[m.key] || route.fullPath)">明细</a>
        </div>
        <p class="no-muted">口径：统计区间 {{ range[0] }} 至 {{ range[1] }}；分母为 0 时显示「暂无数据」；已取消的任务不计入。</p>
      </div>

      <RectificationBoard v-else />
    </section>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'
import PageContainer from '../../../components/PageContainer.vue'
import EmbeddedPage from '../../marketing/center/EmbeddedPage.vue'
import ShiftCalendar from '../ShiftCalendar.vue'
import RectificationBoard from './RectificationBoard.vue'
import { getQualityMetrics } from '../../../api/nursing'
import { getSchedulePage } from '../../../api/schedule'
import { useShiftCodeOptions } from '../../../composables/useShiftCodeOptions'
import { exportCsv } from '../../../utils/export'
import type { NursingQualityMetrics } from '../../../types'
import { METRIC_DRILLDOWN, OPERATIONS_TABS, checkRoster, formatRate, resolveTab } from './nursingModel'
import '../../elder/residentUi.css'

const route = useRoute()
const router = useRouter()
const tab = computed(() => resolveTab(OPERATIONS_TABS, route.query))
const range = ref<[string, string]>([dayjs().startOf('month').format('YYYY-MM-DD'), dayjs().format('YYYY-MM-DD')])
const data = ref<NursingQualityMetrics | null>(null)
const metrics = computed(() => data.value?.metrics || [])
const workloads = computed(() => data.value?.workloads || [])
const workloadColumns = [
  { title: '护理人员', dataIndex: 'staffName', width: 140 },
  { title: '任务数', dataIndex: 'taskCount', width: 100 },
  { title: '已完成', dataIndex: 'doneCount', width: 100 },
  { title: '超时', dataIndex: 'overdueCount', width: 100 },
  { title: '完成比例', key: 'rate' },
  { title: '', key: 'action', width: 100 }
]

const { shiftCodeOptions, loadShiftCodes } = useShiftCodeOptions()
// 不依赖 locale 的周起始：本周一到周日
const monday = dayjs().subtract((dayjs().day() + 6) % 7, 'day')
const week = Array.from({ length: 7 }, (_, i) => monday.add(i, 'day').format('YYYY-MM-DD'))
const rosterLoading = ref(false)
const roster = ref<ReturnType<typeof checkRoster>>({ duplicates: [], gaps: [] })
function shiftName(code: string) { return shiftCodeOptions.value.find((o) => o.value === code)?.label || code }

function go(next: string) { router.replace({ path: route.path, query: { tab: next } }) }

async function loadMetrics() {
  data.value = await getQualityMetrics({ dateFrom: range.value[0], dateTo: range.value[1] }).catch((error) => {
    message.error(error?.message || '质控指标加载失败')
    return null
  })
}

async function loadRoster() {
  rosterLoading.value = true
  try {
    const [page] = await Promise.all([
      getSchedulePage({ pageNo: 1, pageSize: 1000, dateFrom: week[0], dateTo: week[6] }),
      loadShiftCodes()
    ])
    roster.value = checkRoster(page.list || [], week, shiftCodeOptions.value.map((o) => o.value))
  } catch {
    roster.value = { duplicates: [], gaps: [] }
  } finally {
    rosterLoading.value = false
  }
}

function exportMetrics() {
  exportCsv([
    ...metrics.value.map((m) => ({ 类别: '质控指标', 名称: m.label, 分子: m.numerator, 分母: m.denominator, 结果: formatRate(m) })),
    ...workloads.value.map((w) => ({ 类别: '工作负荷', 名称: w.staffName || w.staffId, 分子: w.doneCount, 分母: w.taskCount, 结果: `超时 ${w.overdueCount}` }))
  ], `护理质控-${range.value[0]}-${range.value[1]}`)
}

onMounted(() => {
  loadMetrics()
  loadRoster()
})
</script>

<style scoped>
.no-check { display: flex; flex-wrap: wrap; align-items: center; gap: 8px; margin-bottom: 12px; padding: 12px; border-radius: 10px; background: #f6f9f7; }
.no-muted { color: #8a968f; font-size: 12px; }
.no-quality { display: grid; gap: 12px; max-width: 760px; }
.no-bar { display: grid; grid-template-columns: 130px 1fr 72px 40px; align-items: center; gap: 12px; }
</style>
