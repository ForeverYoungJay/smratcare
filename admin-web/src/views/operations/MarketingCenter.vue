<template>
  <PageContainer title="营销转化运营中心" subTitle="把线索来源、销售跟进、参观评估、签约入住和渠道复盘统一放进经营视图">
    <a-row :gutter="[16, 16]" class="overview-row">
      <a-col :xs="24" :xl="7">
        <a-card :bordered="false" class="index-card">
          <div class="index-head">
            <span>转化风险指数</span>
            <a-tag :color="levelMeta.color">{{ levelMeta.text }}</a-tag>
          </div>
          <div class="index-value">{{ data.conversionIndex }}</div>
          <a-progress :percent="data.conversionIndex" :stroke-color="indexStroke" :show-info="false" />
          <div class="index-meta">更新时间 {{ data.generatedAt || '-' }}</div>
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
        <a-card :bordered="false" title="入住转化漏斗" class="panel-gap">
          <div class="stage-grid">
            <button v-for="item in data.funnelStages" :key="item.key" class="stage-card" @click="go(item.routePath)">
              <div class="row-head">
                <strong>{{ item.title }}</strong>
                <a-tag :color="statusColor(item.status)">{{ statusText(item.status) }}</a-tag>
              </div>
              <div class="stage-value">{{ item.value }}</div>
              <div class="chip-row">
                <span v-for="signal in item.signals" :key="signal" class="soft-chip">{{ signal }}</span>
              </div>
            </button>
          </div>
        </a-card>

        <a-card :bordered="false" title="转化风险来源">
          <div class="risk-list">
            <section v-for="item in data.risks" :key="item.key" class="risk-row">
              <div class="row-head">
                <div>
                  <strong>{{ item.title }}</strong>
                  <span>{{ item.owner }}</span>
                </div>
                <a-tag :color="levelColor(item.level)">{{ levelText(item.level) }}</a-tag>
              </div>
              <div class="chip-row">
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
        <a-card :bordered="false" title="今日营销动作" class="panel-gap">
          <a-list :data-source="data.actions" :locale="{ emptyText: '暂无营销动作' }">
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

        <a-card :bordered="false" title="渠道贡献 Top">
          <div class="channel-list">
            <button v-for="item in data.channels" :key="item.source" class="channel-row" @click="go(item.routePath)">
              <strong>{{ item.source }}</strong>
              <span>{{ item.leadCount }} · 签约率 {{ item.contractRate }}</span>
            </button>
            <a-empty v-if="!data.channels.length" :image="null" description="暂无渠道数据" />
          </div>
        </a-card>
      </a-col>
    </a-row>

    <a-card :bordered="false" title="营销成交闭环流程" class="flow-card">
      <a-row :gutter="[12, 12]">
        <a-col v-for="flow in data.flows" :key="flow.key" :xs="24" :lg="12" :xxl="6">
          <section class="flow-item">
            <div class="row-head">
              <strong>{{ flow.title }}</strong>
              <a-tag :color="statusColor(flow.status)">{{ statusText(flow.status) }}</a-tag>
            </div>
            <ol>
              <li v-for="step in flow.steps" :key="step">{{ step }}</li>
            </ol>
            <a-button type="link" @click="go(flow.routePath)">查看业务</a-button>
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
import { getOperationsMarketing, type OperationsMarketing } from '../../api/operations'

const router = useRouter()
const data = ref<OperationsMarketing>({
  generatedAt: '',
  status: '',
  conversionIndex: 0,
  conversionLevel: 'LOW',
  metrics: [],
  funnelStages: [],
  risks: [],
  channels: [],
  actions: [],
  flows: []
})

const levelMeta = computed(() => {
  if (data.value.conversionLevel === 'CRITICAL') return { text: '紧急转化', color: 'red' }
  if (data.value.conversionLevel === 'HIGH') return { text: '高风险', color: 'volcano' }
  if (data.value.conversionLevel === 'MEDIUM') return { text: '中风险', color: 'gold' }
  return { text: '平稳', color: 'green' }
})

const indexStroke = computed(() => {
  if (data.value.conversionIndex >= 75) return 'var(--danger)'
  if (data.value.conversionIndex >= 50) return 'var(--warning)'
  if (data.value.conversionIndex >= 25) return 'var(--warning)'
  return 'var(--success)'
})

function go(path?: string) {
  if (!path) return
  router.push(path)
}

function levelText(level?: string) {
  if (level === 'CRITICAL') return '紧急'
  if (level === 'HIGH') return '高'
  if (level === 'MEDIUM') return '中'
  return '低'
}

function levelColor(level?: string) {
  if (level === 'CRITICAL' || level === 'HIGH') return 'red'
  if (level === 'MEDIUM') return 'gold'
  return 'green'
}

function statusText(status?: string) {
  if (status === 'READY') return '已落地'
  if (status === 'WARNING') return '需关注'
  if (status === 'PARTIAL') return '增强中'
  if (status === 'PLANNED') return '规划中'
  return status || '未知'
}

function statusColor(status?: string) {
  if (status === 'READY') return 'green'
  if (status === 'WARNING') return 'orange'
  if (status === 'PARTIAL') return 'blue'
  if (status === 'PLANNED') return 'default'
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

async function fetchData() {
  try {
    data.value = await getOperationsMarketing()
  } catch (error: any) {
    message.error(error?.message || '加载营销转化运营中心失败')
  }
}

fetchData()
</script>

<style scoped>
.overview-row {
  margin-bottom: 16px;
}

.index-card {
  height: 100%;
}

.index-head,
.row-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.index-head span,
.index-meta,
.metric-tile small,
.risk-row span,
.action-desc span,
.channel-row span {
  color: var(--muted);
}

.index-value {
  margin: 18px 0 10px;
  color: var(--ink);
  font-size: 46px;
  font-weight: 760;
  line-height: 1;
}

.index-meta {
  margin-top: 10px;
  font-size: 12px;
}

.metric-tile,
.stage-card,
.channel-row {
  width: 100%;
  min-height: 116px;
  padding: 14px;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: #fff;
  text-align: left;
  cursor: pointer;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.metric-tile:hover,
.stage-card:hover,
.channel-row:hover,
.risk-row:hover,
.flow-item:hover {
  border-color: var(--primary);
  box-shadow: var(--shadow-sm);
}

.metric-tile span,
.metric-tile small,
.metric-tile strong {
  display: block;
}

.metric-tile strong,
.stage-value {
  margin: 8px 0 6px;
  color: var(--ink);
  font-size: 24px;
  font-weight: 700;
}

.metric-tile.is-warning {
  border-color: rgba(var(--warning-rgb), 0.4);
  background: rgba(var(--warning-rgb), 0.08);
}

.metric-tile.is-danger {
  border-color: rgba(var(--danger-rgb), 0.32);
  background: rgba(var(--danger-rgb), 0.06);
}

.stage-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(190px, 1fr));
  gap: 12px;
}

.risk-list,
.channel-list {
  display: grid;
  gap: 12px;
}

.risk-row,
.flow-item {
  padding: 14px;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: #fff;
}

.risk-row ol,
.flow-item ol {
  margin: 10px 0 0;
  padding-left: 18px;
  color: var(--muted);
}

.chip-row {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  margin-top: 10px;
}

.soft-chip {
  padding: 2px 8px;
  border-radius: 999px;
  background: var(--surface-2);
  color: var(--muted);
  font-size: 12px;
}

.panel-gap {
  margin-bottom: 16px;
}

.action-desc p {
  margin: 6px 0 10px;
  color: var(--muted);
}

.channel-row {
  min-height: 72px;
}

.channel-row strong,
.channel-row span {
  display: block;
}

.flow-card {
  margin-top: 16px;
}
</style>
