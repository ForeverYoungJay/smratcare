<template>
  <PageContainer
    title="服务质量与合规中心"
    subTitle="把护理质检、标准流程、合规档案和月度服务报告放在同一张运营清单里"
    mode="showcase"
    kicker="综合运营"
  >
    <template #meta>
      <a-space wrap>
        <StatusTag :text="`报告月份 ${report.month || monthValue}`" tone="pending" />
        <StatusTag :text="statusLabel(report.status)" :tone="statusTone(report.status)" />
        <StatusTag :text="`生成时间 ${report.generatedAt || '--'}`" tone="offline" />
      </a-space>
    </template>

    <template #extra>
      <a-space wrap>
        <a-date-picker v-model:value="selectedMonth" picker="month" value-format="YYYY-MM" allow-clear style="width: 150px" />
        <a-button :loading="loading" @click="loadReport">刷新</a-button>
        <a-button type="primary" @click="openPath('/medical-care/nursing-quality')">护理质量中心</a-button>
        <a-button @click="openPath('/care/exception')">异常闭环</a-button>
      </a-space>
    </template>

    <section class="metric-grid">
      <button
        v-for="item in report.metrics"
        :key="item.key"
        type="button"
        class="metric-tile"
        @click="openPath(item.routePath)"
      >
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
        <small>{{ item.helper }}</small>
        <StatusTag :text="statusLabel(item.status)" :tone="statusTone(item.status)" />
      </button>
    </section>

    <section class="ops-grid ops-grid--main">
      <SectionPanel title="服务质检闭环" description="按责任人拆解护理、健康、安全、家属体验和收费合同的质检证据。">
        <div class="quality-list">
          <button
            v-for="item in report.qualityDomains"
            :key="item.key"
            type="button"
            class="quality-row"
            @click="openPath(item.routePath)"
          >
            <div class="quality-row__main">
              <div class="row-title">
                <strong>{{ item.title }}</strong>
                <StatusTag :text="statusLabel(item.status)" :tone="statusTone(item.status)" />
              </div>
              <span>责任人：{{ item.owner }}</span>
              <div class="chip-row">
                <span v-for="checkpoint in item.checkpoints" :key="checkpoint" class="soft-chip">{{ checkpoint }}</span>
              </div>
            </div>
            <div class="evidence-list">
              <small v-for="route in item.evidence" :key="route">{{ route }}</small>
            </div>
          </button>
        </div>
      </SectionPanel>

      <SectionPanel title="月度行动项" description="月末前必须有责任人、入口和截止提示，避免报告停留在描述层。">
        <div class="action-list">
          <button
            v-for="item in report.monthlyActions"
            :key="item.title"
            type="button"
            class="action-row"
            @click="openPath(item.routePath)"
          >
            <div>
              <strong>{{ item.title }}</strong>
              <span>{{ item.owner }} · {{ item.dueHint }}</span>
            </div>
            <StatusTag :text="priorityLabel(item.priority)" :tone="priorityTone(item.priority)" />
          </button>
        </div>
      </SectionPanel>
    </section>

    <section class="ops-grid ops-grid--secondary">
      <SectionPanel title="标准化流程" description="入住、护理、事故、退住和交接班流程都要能被执行、追踪和归档。">
        <div class="process-grid">
          <button
            v-for="item in report.standardProcesses"
            :key="item.key"
            type="button"
            class="process-card"
            @click="openPath(item.routePath)"
          >
            <div class="row-title">
              <strong>{{ item.title }}</strong>
              <StatusTag :text="statusLabel(item.status)" :tone="statusTone(item.status)" />
            </div>
            <ol>
              <li v-for="step in item.steps" :key="step">{{ step }}</li>
            </ol>
          </button>
        </div>
      </SectionPanel>

      <SectionPanel title="合规档案包" description="把协议、知情同意、护理记录、事故报告、监管报表和操作日志拆成可补齐的档案清单。">
        <div class="archive-grid">
          <button
            v-for="item in report.complianceArchives"
            :key="item.key"
            type="button"
            class="archive-card"
            @click="openPath(item.routePath)"
          >
            <div class="row-title">
              <strong>{{ item.title }}</strong>
              <StatusTag :text="statusLabel(item.status)" :tone="statusTone(item.status)" />
            </div>
            <div class="archive-columns">
              <div>
                <span>必备材料</span>
                <small v-for="doc in item.requiredDocuments" :key="doc">{{ doc }}</small>
              </div>
              <div>
                <span>下一步</span>
                <small v-for="next in item.missingOrNext" :key="next">{{ next }}</small>
              </div>
            </div>
          </button>
        </div>
      </SectionPanel>
    </section>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import SectionPanel from '../../components/smartcare/SectionPanel.vue'
import StatusTag from '../../components/smartcare/StatusTag.vue'
import { getOperationsServiceReport, type OperationsServiceReport } from '../../api/operations'

const router = useRouter()
const loading = ref(false)
const selectedMonth = ref(dayjs().format('YYYY-MM'))
const report = ref<OperationsServiceReport>({
  month: selectedMonth.value,
  generatedAt: '',
  reportTitle: '',
  status: 'LANDING',
  metrics: [],
  qualityDomains: [],
  complianceArchives: [],
  standardProcesses: [],
  monthlyActions: []
})

const monthValue = computed(() => selectedMonth.value || dayjs().format('YYYY-MM'))

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

async function loadReport() {
  loading.value = true
  try {
    report.value = await getOperationsServiceReport({ month: monthValue.value })
  } catch (error: any) {
    message.error(error?.message || '服务质量与合规报告加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadReport()
})
</script>

<style scoped>
.metric-grid,
.ops-grid {
  display: grid;
  gap: 16px;
}

.metric-grid {
  grid-template-columns: repeat(6, minmax(0, 1fr));
}

.ops-grid--main {
  grid-template-columns: minmax(0, 1.4fr) minmax(340px, 0.6fr);
}

.ops-grid--secondary {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.metric-tile,
.quality-row,
.action-row,
.process-card,
.archive-card {
  width: 100%;
  border: 1px solid var(--border-soft);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.96);
  text-align: left;
  cursor: pointer;
}

.metric-tile {
  display: grid;
  gap: 10px;
  min-height: 150px;
  padding: 16px;
}

.metric-tile span,
.metric-tile small,
.quality-row span,
.action-row span,
.archive-columns span,
.archive-columns small,
.evidence-list small {
  color: var(--muted);
  font-size: 12px;
  line-height: 1.6;
}

.metric-tile strong {
  color: var(--ink);
  font-size: 24px;
}

.quality-list,
.action-list,
.process-grid,
.archive-grid {
  display: grid;
  gap: 12px;
}

.quality-row,
.action-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding: 15px;
}

.quality-row__main {
  display: grid;
  gap: 8px;
  min-width: 0;
}

.row-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.row-title strong,
.action-row strong {
  color: var(--ink);
}

.chip-row {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.soft-chip {
  padding: 4px 8px;
  border-radius: 999px;
  background: rgba(var(--primary-rgb), 0.08);
}

.evidence-list {
  display: grid;
  gap: 4px;
  min-width: 180px;
}

.process-card,
.archive-card {
  padding: 16px;
}

.process-card ol {
  display: grid;
  gap: 8px;
  margin: 12px 0 0;
  padding-left: 20px;
  color: var(--muted);
  font-size: 13px;
  line-height: 1.6;
}

.archive-columns {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
  margin-top: 12px;
}

.archive-columns div {
  display: grid;
  gap: 6px;
}

.archive-columns span {
  font-weight: 700;
  color: var(--ink);
}

@media (max-width: 1280px) {
  .metric-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .ops-grid--main,
  .ops-grid--secondary {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .metric-grid {
    grid-template-columns: 1fr;
  }

  .quality-row,
  .action-row {
    align-items: flex-start;
    flex-direction: column;
  }

  .archive-columns {
    grid-template-columns: 1fr;
  }
}
</style>
