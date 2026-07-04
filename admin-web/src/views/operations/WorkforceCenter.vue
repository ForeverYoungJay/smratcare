<template>
  <PageContainer title="员工人力运营中心" subTitle="把员工档案、资质、排班、考勤、培训、绩效和护理任务负荷汇总到院长视角">
    <a-row :gutter="[16, 16]" class="overview-row">
      <a-col :xs="24" :xl="7">
        <a-card :bordered="false" class="index-card">
          <div class="index-head">
            <span>人力风险指数</span>
            <a-tag :color="levelMeta.color">{{ levelMeta.text }}</a-tag>
          </div>
          <div class="index-value">{{ data.workforceIndex }}</div>
          <a-progress :percent="data.workforceIndex" :stroke-color="indexStroke" :show-info="false" />
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
        <a-card :bordered="false" title="人力风险来源">
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
        <a-card :bordered="false" title="今日人力动作" class="panel-gap">
          <a-list :data-source="data.actions" :locale="{ emptyText: '暂无人力动作' }">
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

        <a-card :bordered="false" title="角色负荷">
          <div class="load-list">
            <button v-for="item in data.roleLoads" :key="item.key" class="load-row" @click="go(item.routePath)">
              <div class="row-head">
                <strong>{{ item.title }}</strong>
                <a-tag :color="statusColor(item.status)">{{ statusText(item.status) }}</a-tag>
              </div>
              <div class="load-value">{{ item.coverage }}</div>
              <div class="chip-row">
                <span v-for="signal in item.signals" :key="signal" class="soft-chip">{{ signal }}</span>
              </div>
            </button>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <a-card :bordered="false" title="员工运营闭环流程" class="flow-card">
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
import { getOperationsWorkforce, type OperationsWorkforce } from '../../api/operations'

const router = useRouter()
const data = ref<OperationsWorkforce>({
  generatedAt: '',
  status: '',
  workforceIndex: 0,
  workforceLevel: 'LOW',
  metrics: [],
  risks: [],
  roleLoads: [],
  actions: [],
  flows: []
})

const levelMeta = computed(() => {
  if (data.value.workforceLevel === 'CRITICAL') return { text: '紧急缺口', color: 'red' }
  if (data.value.workforceLevel === 'HIGH') return { text: '高风险', color: 'volcano' }
  if (data.value.workforceLevel === 'MEDIUM') return { text: '中风险', color: 'gold' }
  return { text: '平稳', color: 'green' }
})

const indexStroke = computed(() => {
  if (data.value.workforceIndex >= 75) return '#d4380d'
  if (data.value.workforceIndex >= 50) return '#fa8c16'
  if (data.value.workforceIndex >= 25) return '#d4b106'
  return '#389e0d'
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
    data.value = await getOperationsWorkforce()
  } catch (error: any) {
    message.error(error?.message || '加载员工人力运营中心失败')
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
.action-desc span {
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
.load-row {
  width: 100%;
  min-height: 116px;
  padding: 14px;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: var(--surface-2);
  text-align: left;
  cursor: pointer;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.metric-tile:hover,
.load-row:hover,
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

.metric-tile strong {
  margin: 8px 0 6px;
  color: var(--ink);
  font-size: 24px;
}

.metric-tile.is-warning {
  border-color: rgba(var(--warning-rgb), 0.45);
  background: rgba(var(--warning-rgb), 0.08);
}

.metric-tile.is-danger {
  border-color: rgba(var(--danger-rgb), 0.35);
  background: rgba(var(--danger-rgb), 0.06);
}

.risk-list,
.load-list {
  display: grid;
  gap: 12px;
}

.risk-row,
.flow-item {
  padding: 14px;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: var(--surface-2);
}

.risk-row ol,
.flow-item ol {
  margin: 10px 0 0;
  padding-left: 18px;
  color: var(--ink-soft);
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
  background: var(--surface-3);
  color: var(--muted);
  font-size: 12px;
}

.panel-gap {
  margin-bottom: 16px;
}

.action-desc p {
  margin: 6px 0 10px;
  color: var(--ink-soft);
}

.load-value {
  margin-top: 10px;
  color: var(--ink);
  font-size: 20px;
  font-weight: 700;
}

.flow-card {
  margin-top: 16px;
}
</style>
