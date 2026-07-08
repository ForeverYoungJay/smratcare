<template>
  <PageContainer title="管理驾驶舱" subTitle="一个入口承接经营、服务、安全、人力、后勤、营销和智能化闭环">
    <template #extra>
      <a-space wrap>
        <a-tag color="blue">{{ overallStatusText(capability.overallStatus) }}</a-tag>
        <a-button :loading="loading" @click="loadData">刷新</a-button>
      </a-space>
    </template>

    <a-alert
      class="entry-alert"
      type="info"
      show-icon
      message="运营入口已收敛为一个驾驶舱"
      description="各业务运营数据统一通过标签页查看，需要处理时再进入对应业务明细页面。"
    />

    <a-row :gutter="[16, 16]" class="summary-row">
      <a-col v-for="item in summaryCards" :key="item.key" :xs="12" :lg="6">
        <button class="summary-card" type="button" @click="openSummaryCard(item)">
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
          <small>{{ item.helper }}</small>
        </button>
      </a-col>
    </a-row>

    <a-card :bordered="false">
      <a-tabs v-model:activeKey="activeTab" @change="onTabChange">
        <a-tab-pane key="overview" tab="总览">
          <section class="tab-layout overview-metrics">
            <MetricStrip :items="capability.metrics" />
          </section>
          <a-row :gutter="[16, 16]">
            <a-col v-for="domain in capability.domains" :key="domain.key" :xs="24" :lg="12" :xxl="8">
              <section class="domain-panel">
                <div class="domain-panel__head">
                  <div>
                    <strong>{{ domain.title }}</strong>
                    <p>{{ domain.description }}</p>
                  </div>
                  <a-tag :color="statusColor(domain.status)">{{ domain.statusText || statusText(domain.status) }}</a-tag>
                </div>
                <ul>
                  <li v-for="item in (domain.completed || []).slice(0, 3)" :key="item">{{ item }}</li>
                </ul>
                <a-button type="link" @click="go(domain.routePath)">查看模块</a-button>
              </section>
            </a-col>
          </a-row>
        </a-tab-pane>

        <a-tab-pane key="quality" tab="服务质量">
          <section class="tab-layout">
            <MetricStrip :items="serviceReport.metrics" />
            <PanelList title="质控域" :items="serviceReport.qualityDomains" />
            <ActionList title="月度动作" :items="serviceReport.monthlyActions" />
          </section>
        </a-tab-pane>

        <a-tab-pane key="safety" tab="安全风险">
          <section class="tab-layout">
            <div class="index-banner">
              <span>风险指数</span>
              <strong>{{ safetyRisk.riskIndex }}</strong>
              <a-tag :color="levelColor(safetyRisk.riskLevel)">{{ levelText(safetyRisk.riskLevel) }}</a-tag>
            </div>
            <MetricStrip :items="safetyRisk.metrics" />
            <PanelList title="风险来源" :items="safetyRisk.riskSources" />
            <PanelList title="应急流程" :items="safetyRisk.emergencyFlows" />
            <ActionList title="处置动作" :items="safetyRisk.actions" />
          </section>
        </a-tab-pane>

        <a-tab-pane key="workforce" tab="人力排班">
          <section class="tab-layout">
            <div class="index-banner">
              <span>人力指数</span>
              <strong>{{ workforce.workforceIndex }}</strong>
              <a-tag :color="levelColor(workforce.workforceLevel)">{{ levelText(workforce.workforceLevel) }}</a-tag>
            </div>
            <MetricStrip :items="workforce.metrics" />
            <PanelList title="人力风险" :items="workforce.risks" />
            <PanelList title="岗位负荷" :items="workforce.roleLoads" />
            <PanelList title="人力流程" :items="workforce.flows" />
            <ActionList title="调度动作" :items="workforce.actions" />
          </section>
        </a-tab-pane>

        <a-tab-pane key="logistics" tab="后勤保障">
          <section class="tab-layout">
            <div class="index-banner">
              <span>后勤指数</span>
              <strong>{{ logistics.logisticsIndex }}</strong>
              <a-tag :color="levelColor(logistics.logisticsLevel)">{{ levelText(logistics.logisticsLevel) }}</a-tag>
            </div>
            <MetricStrip :items="logistics.metrics" />
            <PanelList title="保障风险" :items="logistics.risks" />
            <ActionList title="处理动作" :items="logistics.actions" />
          </section>
        </a-tab-pane>

        <a-tab-pane key="marketing" tab="营销转化">
          <section class="tab-layout">
            <div class="index-banner">
              <span>转化指数</span>
              <strong>{{ marketing.conversionIndex }}</strong>
              <a-tag :color="levelColor(marketing.conversionLevel)">{{ levelText(marketing.conversionLevel) }}</a-tag>
            </div>
            <MetricStrip :items="marketing.metrics" />
            <PanelList title="漏斗阶段" :items="marketing.funnelStages" />
            <ActionList title="转化动作" :items="marketing.actions" />
          </section>
        </a-tab-pane>

        <a-tab-pane key="family" tab="家属服务">
          <section class="tab-layout">
            <MetricStrip :items="familyService.metrics" />
            <PanelList title="家属流程" :items="familyService.flows" />
            <ActionList title="服务动作" :items="familyService.actions" />
          </section>
        </a-tab-pane>

        <a-tab-pane key="intelligence" tab="智能化">
          <section class="tab-layout">
            <MetricStrip :items="intelligence.metrics" />
            <PanelList title="智能场景" :items="intelligence.scenarios" />
            <ActionList title="落地动作" :items="intelligence.actions" />
          </section>
        </a-tab-pane>
      </a-tabs>
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, defineComponent, h, ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import {
  getOperationsCapabilityMap,
  getOperationsFamilyService,
  getOperationsIntelligence,
  getOperationsLogistics,
  getOperationsMarketing,
  getOperationsSafetyRisk,
  getOperationsServiceReport,
  getOperationsWorkforce,
  type OperationsCapabilityMap,
  type OperationsFamilyService,
  type OperationsIntelligence,
  type OperationsLogistics,
  type OperationsMarketing,
  type OperationsSafetyRisk,
  type OperationsServiceReport,
  type OperationsWorkforce
} from '../../api/operations'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const tabKeys = ['overview', 'quality', 'safety', 'workforce', 'logistics', 'marketing', 'family', 'intelligence']
const activeTab = ref(normalizeTab(route.query.tab))

const capability = ref<OperationsCapabilityMap>({
  version: '',
  generatedAt: '',
  overallStatus: '',
  summary: [],
  domains: [],
  metrics: [],
  intelligence: []
})
const serviceReport = ref<OperationsServiceReport>(emptyServiceReport())
const familyService = ref<OperationsFamilyService>({ generatedAt: '', status: '', metrics: [], runtimeCards: [], flows: [], visibleData: [], actions: [] })
const intelligence = ref<OperationsIntelligence>({ generatedAt: '', status: '', metrics: [], scenarios: [], dataSources: [], actions: [] })
const safetyRisk = ref<OperationsSafetyRisk>({ generatedAt: '', status: '', riskLevel: 'LOW', riskIndex: 0, metrics: [], riskSources: [], emergencyFlows: [], actions: [], recentEvents: [] })
const workforce = ref<OperationsWorkforce>({ generatedAt: '', status: '', workforceIndex: 0, workforceLevel: 'LOW', metrics: [], risks: [], roleLoads: [], actions: [], flows: [] })
const logistics = ref<OperationsLogistics>({ generatedAt: '', status: '', logisticsIndex: 0, logisticsLevel: 'LOW', metrics: [], risks: [], actions: [], flows: [] })
const marketing = ref<OperationsMarketing>({ generatedAt: '', status: '', conversionIndex: 0, conversionLevel: 'LOW', metrics: [], funnelStages: [], risks: [], channels: [], actions: [], flows: [] })

const summaryCards = computed(() => [
  { key: 'bed-finance', label: '床位收费', value: '房态', helper: '床态、合同、账单、欠费', routePath: findDomain('bed-finance')?.routePath || '/finance/workbench' },
  { key: 'quality', label: '服务质量', value: serviceReport.value.metrics?.[0]?.value || '--', helper: serviceReport.value.reportTitle || '月度质量与合规', tab: 'quality' },
  { key: 'safety', label: '安全风险', value: String(safetyRisk.value.riskIndex || 0), helper: '风险指数与处置动作', tab: 'safety' },
  { key: 'workforce', label: '人力排班', value: String(workforce.value.workforceIndex || 0), helper: '班次覆盖与岗位负荷', tab: 'workforce' },
  { key: 'marketing', label: '营销转化', value: String(marketing.value.conversionIndex || 0), helper: '线索、参观、评估、签约', tab: 'marketing' }
])

const MetricStrip = defineComponent({
  props: { items: { type: Array, default: () => [] } },
  setup(props) {
    return () => h('div', { class: 'metric-strip' }, (props.items as any[]).map((item) => h('button', {
      class: ['metric-box', item.tone ? `is-${item.tone}` : ''],
      type: 'button',
      onClick: () => go(item.routePath)
    }, [
      h('span', item.label || item.name || item.title),
      h('strong', metricValue(item)),
      h('small', metricHelper(item))
    ])))
  }
})

const PanelList = defineComponent({
  props: { title: { type: String, default: '' }, items: { type: Array, default: () => [] } },
  setup(props) {
    return () => h('section', { class: 'list-panel' }, [
      h('h3', props.title),
      h('div', { class: 'list-grid' }, (props.items as any[]).map((item) => h('button', {
        class: 'list-card',
        type: 'button',
        onClick: () => go(item.routePath || item.adminRoute)
      }, [
        h('strong', item.title || item.source || item.key),
        h('span', item.owner || item.status || item.coverage || item.familyApi || ''),
        h('p', firstText(item.signals || item.controls || item.checkpoints || item.steps || item.landed || item.nextSteps))
      ])))
    ])
  }
})

const ActionList = defineComponent({
  props: { title: { type: String, default: '' }, items: { type: Array, default: () => [] } },
  setup(props) {
    return () => h('section', { class: 'list-panel' }, [
      h('h3', props.title),
      h('div', { class: 'action-list' }, (props.items as any[]).map((item) => h('button', {
        class: 'action-item',
        type: 'button',
        onClick: () => go(item.routePath)
      }, [
        h('strong', item.title),
        h('span', `${priorityText(item.priority)} · ${item.owner || '-'}`),
        h('p', item.description || item.dueHint || '')
      ])))
    ])
  }
})

watch(() => route.query.tab, (tab) => {
  activeTab.value = normalizeTab(tab)
})

function emptyServiceReport(): OperationsServiceReport {
  return { month: '', generatedAt: '', reportTitle: '', status: '', metrics: [], qualityDomains: [], complianceArchives: [], standardProcesses: [], monthlyActions: [] }
}

function normalizeTab(tab: unknown) {
  const value = Array.isArray(tab) ? tab[0] : tab
  return tabKeys.includes(String(value)) ? String(value) : 'overview'
}

function onTabChange(key: string | number) {
  const tab = String(key)
  router.replace({ path: '/stats/operations', query: tab === 'overview' ? {} : { tab } })
}

function openTab(key: string) {
  activeTab.value = key
  onTabChange(key)
}

function openSummaryCard(item: { tab?: string; routePath?: string }) {
  if (item.routePath) {
    go(item.routePath)
    return
  }
  if (item.tab) {
    openTab(item.tab)
  }
}

function go(path?: string) {
  if (!path) return
  router.push(path)
}

function findDomain(key: string) {
  return capability.value.domains.find((item) => item.key === key)
}

function findCapabilityMetric(key: string) {
  return capability.value.metrics.find((item) => item.key === key)
}

function firstText(items?: string[]) {
  return items?.[0] || '暂无补充说明'
}

function metricValue(item: any) {
  if (item.value) return item.value
  if (item.decisionValue && item.source) return '已接入'
  return item.decisionValue || '--'
}

function metricHelper(item: any) {
  return item.helper || item.decisionValue || item.source || ''
}

function statusText(status?: string) {
  if (status === 'READY') return '已落地'
  if (status === 'PARTIAL') return '增强中'
  if (status === 'PLANNED') return '待建设'
  if (status === 'LANDING') return '落地中'
  return status || '未知'
}

function overallStatusText(status?: string) {
  if (!status) return '运营能力同步中'
  return statusText(status)
}

function levelText(level?: string) {
  if (level === 'CRITICAL') return '严重'
  if (level === 'HIGH') return '高'
  if (level === 'MEDIUM') return '中'
  if (level === 'LOW' || !level) return '低'
  return level
}

function statusColor(status?: string) {
  if (status === 'READY') return 'green'
  if (status === 'PARTIAL') return 'blue'
  if (status === 'PLANNED') return 'default'
  return 'orange'
}

function levelColor(level?: string) {
  if (level === 'CRITICAL' || level === 'HIGH') return 'red'
  if (level === 'MEDIUM') return 'gold'
  return 'green'
}

function priorityText(priority?: string) {
  if (priority === 'HIGH') return '高'
  if (priority === 'MEDIUM') return '中'
  return '低'
}

async function loadData() {
  loading.value = true
  try {
    const [capabilityData, reportData, familyData, intelligenceData, safetyData, workforceData, logisticsData, marketingData] = await Promise.all([
      getOperationsCapabilityMap(),
      getOperationsServiceReport(),
      getOperationsFamilyService(),
      getOperationsIntelligence(),
      getOperationsSafetyRisk(),
      getOperationsWorkforce(),
      getOperationsLogistics(),
      getOperationsMarketing()
    ])
    capability.value = capabilityData
    serviceReport.value = reportData
    familyService.value = familyData
    intelligence.value = intelligenceData
    safetyRisk.value = safetyData
    workforce.value = workforceData
    logistics.value = logisticsData
    marketing.value = marketingData
  } catch (error: any) {
    message.error(error?.message || '加载管理驾驶舱失败')
  } finally {
    loading.value = false
  }
}

loadData()
</script>

<style scoped>
.entry-alert {
  margin-bottom: 16px;
}

.summary-row {
  margin-bottom: 16px;
}

.overview-metrics {
  margin-bottom: 16px;
}

.summary-card,
.metric-box,
.list-card,
.action-item {
  width: 100%;
  border: 1px solid var(--border-soft);
  border-radius: 8px;
  background: #fff;
  text-align: left;
  cursor: pointer;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}

.summary-card:hover,
.metric-box:hover,
.list-card:hover,
.action-item:hover,
.domain-panel:hover {
  border-color: var(--primary);
  box-shadow: var(--shadow-md);
  transform: translateY(-1px);
}

.summary-card {
  min-height: 120px;
  padding: 18px;
}

.summary-card span,
.metric-box span,
.list-card span,
.action-item span {
  display: block;
  color: var(--muted);
  font-size: 13px;
}

.summary-card strong {
  display: block;
  margin: 8px 0;
  color: var(--ink);
  font-size: 28px;
  line-height: 1.2;
}

.summary-card small,
.metric-box small,
.list-card p,
.action-item p,
.domain-panel p {
  margin: 0;
  color: var(--muted-2);
  font-size: 13px;
  line-height: 1.6;
}

.tab-layout {
  display: grid;
  gap: 16px;
}

.metric-strip {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 12px;
}

.metric-box {
  min-height: 106px;
  padding: 16px;
}

.metric-box strong {
  display: block;
  margin: 8px 0;
  color: var(--ink);
  font-size: 22px;
  line-height: 1.2;
}

.index-banner {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 18px;
  border: 1px solid var(--border-soft);
  border-radius: 8px;
  background: var(--surface-2);
}

.index-banner span {
  color: var(--muted);
}

.index-banner strong {
  color: var(--ink);
  font-size: 32px;
  line-height: 1;
}

.domain-panel {
  height: 100%;
  padding: 18px;
  border: 1px solid var(--border-soft);
  border-radius: 8px;
  background: #fff;
}

.domain-panel__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.domain-panel strong,
.list-card strong,
.action-item strong,
.list-panel h3 {
  color: var(--ink);
}

.domain-panel ul {
  margin: 12px 0;
  padding-left: 18px;
  color: var(--muted);
}

.list-panel h3 {
  margin: 0 0 12px;
  font-size: 16px;
}

.list-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 12px;
}

.list-card,
.action-item {
  min-height: 112px;
  padding: 14px;
}

.list-card strong,
.action-item strong {
  display: block;
  margin-bottom: 6px;
}

.action-list {
  display: grid;
  gap: 10px;
}

@media (max-width: 768px) {
  .summary-card strong,
  .index-banner strong {
    font-size: 24px;
  }
}
</style>
