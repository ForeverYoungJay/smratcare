<template>
  <PageContainer
    title="智能运营中心"
    subTitle="把风险预警、AI护理摘要、语音记录、IoT设备、智能排班和家属问答拆成可执行路线"
    mode="showcase"
    kicker="智能化落地"
  >
    <template #meta>
      <a-space wrap>
        <StatusTag :text="statusLabel(data.status)" :tone="statusTone(data.status)" />
        <StatusTag :text="`最近同步 ${data.generatedAt || '--'}`" tone="offline" />
      </a-space>
    </template>

    <template #extra>
      <a-space wrap>
        <a-button :loading="loading" @click="loadData">刷新</a-button>
        <a-button @click="openPath('/medical-care/smart-alerts')">智慧设备告警</a-button>
        <a-button type="primary" @click="openPath('/medical-care/ai-reports')">AI健康报告</a-button>
      </a-space>
    </template>

    <section class="metric-grid">
      <button
        v-for="item in data.metrics"
        :key="item.key"
        type="button"
        class="metric-card"
        @click="openPath(item.routePath)"
      >
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
        <small>{{ item.helper }}</small>
      </button>
    </section>

    <section class="intel-layout">
      <SectionPanel title="智能化场景路线" description="每个场景都区分已落地能力和下一步补齐事项，避免智能化停留在概念。">
        <div class="scenario-list">
          <button
            v-for="item in data.scenarios"
            :key="item.key"
            type="button"
            class="scenario-row"
            @click="openPath(item.routePath)"
          >
            <div class="scenario-head">
              <div>
                <strong>{{ item.title }}</strong>
                <span>责任：{{ item.owner }}</span>
              </div>
              <StatusTag :text="statusLabel(item.status)" :tone="statusTone(item.status)" />
            </div>
            <div class="scenario-columns">
              <div>
                <b>已落地</b>
                <small v-for="signal in item.landed" :key="signal">{{ signal }}</small>
              </div>
              <div>
                <b>下一步</b>
                <small v-for="step in item.nextSteps" :key="step">{{ step }}</small>
              </div>
            </div>
          </button>
        </div>
      </SectionPanel>

      <SectionPanel title="智能化行动项" description="当前最值得推进的配置、模板和安全边界。">
        <div class="action-list">
          <button
            v-for="item in data.actions"
            :key="item.title"
            type="button"
            class="action-row"
            @click="openPath(item.routePath)"
          >
            <div>
              <strong>{{ item.title }}</strong>
              <span>{{ item.owner }} · {{ item.description }}</span>
            </div>
            <StatusTag :text="priorityLabel(item.priority)" :tone="priorityTone(item.priority)" />
          </button>
        </div>
      </SectionPanel>
    </section>

    <SectionPanel title="智能化数据源" description="智能化落地依赖可解释的数据源，先把信号来源和证据入口固定下来。">
      <div class="source-grid">
        <button
          v-for="item in data.dataSources"
          :key="item.key"
          type="button"
          class="source-card"
          @click="openPath(item.routePath)"
        >
          <div class="source-head">
            <strong>{{ item.title }}</strong>
            <StatusTag :text="statusLabel(item.status)" :tone="statusTone(item.status)" />
          </div>
          <div class="signal-row">
            <span v-for="signal in item.signals" :key="signal">{{ signal }}</span>
          </div>
        </button>
      </div>
    </SectionPanel>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import SectionPanel from '../../components/smartcare/SectionPanel.vue'
import StatusTag from '../../components/smartcare/StatusTag.vue'
import { getOperationsIntelligence, type OperationsIntelligence } from '../../api/operations'

const router = useRouter()
const loading = ref(false)
const data = ref<OperationsIntelligence>({
  generatedAt: '',
  status: 'LANDING',
  metrics: [],
  scenarios: [],
  dataSources: [],
  actions: []
})

function statusLabel(status?: string) {
  if (status === 'READY') return '已落地'
  if (status === 'PARTIAL') return '增强中'
  if (status === 'LANDING') return '落地中'
  return '待建设'
}

function statusTone(status?: string) {
  if (status === 'READY') return 'normal' as const
  if (status === 'PARTIAL' || status === 'LANDING') return 'pending' as const
  return 'offline' as const
}

function priorityLabel(priority?: string) {
  if (priority === 'HIGH') return '高优先'
  if (priority === 'MEDIUM') return '中优先'
  return '低优先'
}

function priorityTone(priority?: string) {
  if (priority === 'HIGH') return 'danger' as const
  if (priority === 'MEDIUM') return 'warning' as const
  return 'offline' as const
}

function openPath(path?: string) {
  if (!path) return
  router.push(path)
}

async function loadData() {
  loading.value = true
  try {
    data.value = await getOperationsIntelligence()
  } catch (error: any) {
    message.error(error?.message || '智能运营中心加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.metric-grid,
.intel-layout,
.source-grid {
  display: grid;
  gap: 16px;
}

.metric-grid {
  grid-template-columns: repeat(5, minmax(0, 1fr));
}

.intel-layout {
  grid-template-columns: minmax(0, 1.35fr) minmax(340px, 0.65fr);
}

.metric-card,
.scenario-row,
.action-row,
.source-card {
  width: 100%;
  border: 1px solid var(--border-soft);
  border-radius: 18px;
  background: var(--surface-2);
  text-align: left;
  cursor: pointer;
}

.metric-card {
  display: grid;
  gap: 10px;
  min-height: 132px;
  padding: 16px;
}

.metric-card span,
.metric-card small,
.scenario-head span,
.action-row span,
.scenario-columns small {
  color: var(--muted);
  font-size: 12px;
  line-height: 1.6;
}

.metric-card strong {
  color: var(--ink);
  font-size: 26px;
}

.scenario-list,
.action-list {
  display: grid;
  gap: 12px;
}

.scenario-row,
.action-row,
.source-card {
  padding: 16px;
}

.action-row,
.scenario-head,
.source-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.action-row strong,
.scenario-head strong,
.source-head strong {
  display: block;
  color: var(--ink);
}

.scenario-columns {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
  margin-top: 12px;
}

.scenario-columns div {
  display: grid;
  gap: 6px;
}

.scenario-columns b {
  color: var(--ink);
  font-size: 13px;
}

.source-grid {
  grid-template-columns: repeat(5, minmax(0, 1fr));
}

.signal-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

.signal-row span {
  padding: 5px 9px;
  border-radius: 999px;
  background: rgba(var(--primary-rgb), 0.08);
  color: var(--primary-strong);
  font-size: 12px;
}

@media (max-width: 1280px) {
  .metric-grid,
  .source-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .intel-layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .metric-grid,
  .source-grid,
  .scenario-columns {
    grid-template-columns: 1fr;
  }

  .action-row,
  .scenario-head,
  .source-head {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
