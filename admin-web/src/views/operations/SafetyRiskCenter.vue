<template>
  <PageContainer title="安全风险运营中心" subTitle="事故、设备告警、夜间巡查、消防隐患和应急流程统一看、统一追">
    <a-row :gutter="[16, 16]" class="overview-row">
      <a-col :xs="24" :xl="7">
        <a-card :bordered="false" class="risk-index-card">
          <div class="risk-index-head">
            <span>综合风险指数</span>
            <a-tag :color="riskLevelMeta.color">{{ riskLevelMeta.text }}</a-tag>
          </div>
          <div class="risk-index-value">{{ data.riskIndex }}</div>
          <a-progress :percent="data.riskIndex" :stroke-color="riskStroke" :show-info="false" />
          <div class="risk-index-meta">更新时间 {{ data.generatedAt || '-' }}</div>
        </a-card>
      </a-col>
      <a-col :xs="24" :xl="17">
        <a-row :gutter="[12, 12]">
          <a-col v-for="metric in data.metrics" :key="metric.key" :xs="12" :lg="8">
            <button class="metric-tile" :class="`is-${metric.tone}`" @click="go(metric.routePath)">
              <span>{{ metric.label }}</span>
              <strong>{{ metric.value }}</strong>
              <small>{{ metric.helper }}</small>
            </button>
          </a-col>
        </a-row>
      </a-col>
    </a-row>

    <a-row :gutter="[16, 16]">
      <a-col :xs="24" :xl="15">
        <a-card :bordered="false" title="风险来源">
          <div class="risk-source-list">
            <section v-for="item in data.riskSources" :key="item.key" class="risk-source">
              <div class="risk-source-main">
                <div>
                  <strong>{{ item.title }}</strong>
                  <span>{{ item.owner }}</span>
                </div>
                <a-tag :color="sourceStatusColor(item.status)">{{ sourceStatusText(item.status) }}</a-tag>
              </div>
              <div class="risk-source-signals">
                <a-tag v-for="signal in item.signals" :key="signal" color="blue">{{ signal }}</a-tag>
              </div>
              <ol>
                <li v-for="control in item.controls" :key="control">{{ control }}</li>
              </ol>
              <a-button type="link" @click="go(item.routePath)">进入处理</a-button>
            </section>
          </div>
        </a-card>
      </a-col>

      <a-col :xs="24" :xl="9">
        <a-card :bordered="false" title="今日处置动作" class="panel-gap">
          <a-list :data-source="data.actions" :locale="{ emptyText: '暂无处置动作' }">
            <template #renderItem="{ item }">
              <a-list-item>
                <a-list-item-meta>
                  <template #title>
                    <a-space>
                      <span>{{ item.title }}</span>
                      <a-tag :color="priorityColor(item.priority)">{{ priorityText(item.priority) }}</a-tag>
                    </a-space>
                  </template>
                  <template #description>
                    <div class="action-desc">
                      <span>{{ item.owner }}</span>
                      <p>{{ item.description }}</p>
                      <a-button size="small" @click="go(item.routePath)">处理</a-button>
                    </div>
                  </template>
                </a-list-item-meta>
              </a-list-item>
            </template>
          </a-list>
        </a-card>

        <a-card :bordered="false" title="近期安全事件">
          <a-timeline>
            <a-timeline-item v-for="item in data.recentEvents" :key="`${item.source}-${item.id}`" :color="eventColor(item.level, item.status)">
              <button class="event-link" @click="go(item.routePath)">
                <strong>{{ item.source }} · {{ item.title }}</strong>
                <span>{{ item.occurredAt || '-' }} / {{ eventStatusText(item.status) }}</span>
              </button>
            </a-timeline-item>
          </a-timeline>
        </a-card>
      </a-col>
    </a-row>

    <a-card :bordered="false" title="应急预案流程" class="flow-card">
      <a-row :gutter="[12, 12]">
        <a-col v-for="flow in data.emergencyFlows" :key="flow.key" :xs="24" :lg="12" :xxl="8">
          <section class="flow-item">
            <div class="flow-head">
              <strong>{{ flow.title }}</strong>
              <a-button type="link" @click="go(flow.routePath)">关联业务</a-button>
            </div>
            <p>{{ flow.trigger }}</p>
            <ol>
              <li v-for="step in flow.steps" :key="step">{{ step }}</li>
            </ol>
          </section>
        </a-col>
      </a-row>
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import { getOperationsSafetyRisk, type OperationsSafetyRisk } from '../../api/operations'

const router = useRouter()
const data = ref<OperationsSafetyRisk>({
  generatedAt: '',
  status: '',
  riskLevel: 'LOW',
  riskIndex: 0,
  metrics: [],
  riskSources: [],
  emergencyFlows: [],
  actions: [],
  recentEvents: []
})

const riskLevelMeta = computed(() => {
  if (data.value.riskLevel === 'CRITICAL') return { text: '紧急风险', color: 'red' }
  if (data.value.riskLevel === 'HIGH') return { text: '高风险', color: 'volcano' }
  if (data.value.riskLevel === 'MEDIUM') return { text: '中风险', color: 'gold' }
  return { text: '低风险', color: 'green' }
})
const riskStroke = computed(() => {
  if (data.value.riskIndex >= 75) return '#d4380d'
  if (data.value.riskIndex >= 50) return '#fa8c16'
  if (data.value.riskIndex >= 25) return '#d4b106'
  return '#389e0d'
})

function go(path?: string) {
  if (!path) return
  router.push(path)
}

function sourceStatusText(status?: string) {
  if (status === 'READY') return '平稳'
  if (status === 'WARNING') return '需处理'
  if (status === 'PARTIAL') return '建设中'
  return status || '未知'
}

function sourceStatusColor(status?: string) {
  if (status === 'READY') return 'green'
  if (status === 'WARNING') return 'orange'
  if (status === 'PARTIAL') return 'blue'
  return 'default'
}

function priorityText(priority?: string) {
  if (priority === 'HIGH') return '高'
  if (priority === 'MEDIUM') return '中'
  return '低'
}

function priorityColor(priority?: string) {
  if (priority === 'HIGH') return 'red'
  if (priority === 'MEDIUM') return 'gold'
  return 'green'
}

function eventStatusText(status?: string) {
  if (status === 'OPEN') return '处理中'
  if (status === 'ACKNOWLEDGED') return '已确认'
  if (status === 'RESOLVED' || status === 'CLOSED') return '已闭环'
  if (status === 'RUNNING') return '执行中'
  return status || '未知'
}

function eventColor(level?: string, status?: string) {
  if (status === 'OPEN' || level === 'CRITICAL' || level === 'MAJOR') return 'red'
  if (status === 'ACKNOWLEDGED' || status === 'RUNNING' || level === 'HIGH') return 'orange'
  return 'green'
}

async function fetchData() {
  try {
    data.value = await getOperationsSafetyRisk()
  } catch (error: any) {
    message.error(error?.message || '加载安全风险运营中心失败')
  }
}

fetchData()
</script>

<style scoped>
.overview-row {
  margin-bottom: 16px;
}

.risk-index-card {
  height: 100%;
}

.risk-index-head,
.risk-source-main,
.flow-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.risk-index-head span,
.risk-index-meta,
.risk-source-main span,
.flow-item p,
.metric-tile small,
.action-desc span,
.event-link span {
  color: var(--muted);
}

.risk-index-value {
  margin: 18px 0 10px;
  color: var(--ink);
  font-size: 46px;
  font-weight: 760;
  line-height: 1;
}

.risk-index-meta {
  margin-top: 10px;
  font-size: 12px;
}

.metric-tile {
  display: grid;
  gap: 8px;
  width: 100%;
  min-height: 128px;
  padding: 16px;
  text-align: left;
  background: var(--surface-2);
  border: 1px solid var(--border);
  border-radius: 8px;
  cursor: pointer;
}

.metric-tile strong {
  color: var(--ink);
  font-size: 24px;
}

.metric-tile.is-warning {
  background: rgba(var(--warning-rgb), 0.08);
  border-color: rgba(var(--warning-rgb), 0.45);
}

.metric-tile.is-danger {
  background: rgba(var(--danger-rgb), 0.06);
  border-color: rgba(var(--danger-rgb), 0.35);
}

.risk-source-list {
  display: grid;
  gap: 12px;
}

.risk-source,
.flow-item {
  padding: 14px;
  border: 1px solid var(--border);
  border-radius: 8px;
}

.risk-source-signals {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin: 10px 0;
}

.risk-source ol,
.flow-item ol {
  margin: 0;
  padding-left: 18px;
  color: var(--ink-soft);
}

.panel-gap {
  margin-bottom: 16px;
}

.action-desc {
  display: grid;
  gap: 8px;
}

.action-desc p {
  margin: 0;
}

.event-link {
  display: grid;
  gap: 4px;
  padding: 0;
  color: inherit;
  text-align: left;
  background: transparent;
  border: 0;
  cursor: pointer;
}

.flow-card {
  margin-top: 16px;
}
</style>
