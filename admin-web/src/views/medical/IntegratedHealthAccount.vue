<template>
  <PageContainer title="健康服务与医护账户一体化" subTitle="医护执行 + 老人账户联动总览">
    <StatefulBlock :loading="loading" :error="errorText" :empty="false" @retry="load">
      <a-card class="card-elevated risk-hero" :bordered="false" style="margin-bottom: 12px">
        <a-row :gutter="[12, 12]" align="middle">
          <a-col :xs="24" :xl="9">
            <div class="risk-title">医护-账户联动风险指数</div>
            <div class="risk-score-row">
              <span class="risk-score">{{ riskIndex }}</span>
              <a-tag :color="riskTagColor">{{ riskLevelLabel }}</a-tag>
            </div>
            <a-progress :percent="riskIndex" :status="riskIndex >= 75 ? 'exception' : riskIndex >= 55 ? 'active' : 'normal'" />
            <div class="risk-subline">触发信号 {{ riskSignals.length }} 项 · 业务日期 {{ medicalSummary.snapshotDate || '-' }}</div>
          </a-col>
          <a-col :xs="24" :xl="15">
            <a-space wrap>
              <a-tag color="orange">护理超时率 {{ Number(medicalSummary.careTaskOverdueRate || 0).toFixed(2) }}%</a-tag>
              <a-tag color="blue">医嘱待执行率 {{ Number(medicalSummary.medicationPendingRate || 0).toFixed(2) }}%</a-tag>
              <a-tag color="volcano">整改逾期 {{ medicalSummary.rectifyOverdueCount || 0 }} 条</a-tag>
              <a-tag color="purple">账户预警 {{ warningCount }} 人</a-tag>
            </a-space>
            <a-list size="small" style="margin-top: 8px" :data-source="riskSignals" :pagination="false" :locale="{ emptyText: '暂无协同风险信号' }">
              <template #renderItem="{ item }">
                <a-list-item>{{ item }}</a-list-item>
              </template>
            </a-list>
          </a-col>
        </a-row>
      </a-card>

      <a-alert
        v-if="warningCount > 0 || medicalSummary.overdueCareTaskCount > 0 || medicalSummary.todayMedicationPendingCount > 0"
        type="warning"
        show-icon
        style="margin-bottom: 12px"
        :message="`协同提醒：余额预警 ${warningCount} 人，护理超时 ${medicalSummary.overdueCareTaskCount} 条，用药待执行 ${medicalSummary.todayMedicationPendingCount} 条。`"
      />
      <a-row :gutter="16" style="margin-bottom: 16px">
        <a-col :span="6"><a-card><a-statistic title="今日待执行医嘱" :value="medicalSummary.pendingMedicalOrderCount" /></a-card></a-col>
        <a-col :span="6"><a-card><a-statistic title="护理超时任务" :value="medicalSummary.overdueCareTaskCount" /></a-card></a-col>
        <a-col :span="6"><a-card><a-statistic title="余额预警人数" :value="warningCount" /></a-card></a-col>
        <a-col :span="6"><a-card><a-statistic title="今日巡查待完成" :value="medicalSummary.todayInspectionPendingCount" /></a-card></a-col>
      </a-row>

      <a-card title="账户预警与医护协同" style="margin-bottom: 16px">
        <template #extra>
          <a-space>
            <a-button @click="go('/finance/accounts/list')">老人账户</a-button>
            <a-button @click="go('/health/medication/medication-registration?date=today')">用药登记</a-button>
            <a-button type="primary" @click="go('/care/workbench/task-board?date=today')">护理任务看板</a-button>
          </a-space>
        </template>
        <a-table :columns="warningColumns" :data-source="warningRows" :pagination="false" row-key="id" size="small">
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'balance'">
              <a-tag :color="Number(record.balance) <= Number(record.warnThreshold) ? 'red' : 'green'">{{ record.balance }}</a-tag>
            </template>
          </template>
        </a-table>
      </a-card>

      <a-card title="最近账户流水（用于医护收费核对）">
        <a-table :columns="logColumns" :data-source="logRows" :pagination="false" row-key="id" size="small" />
      </a-card>
    </StatefulBlock>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import StatefulBlock from '../../components/StatefulBlock.vue'
import { getElderAccountLogPage, getElderAccountWarnings } from '../../api/finance'
import { getMedicalCareWorkbenchSummary } from '../../api/medicalCare'
import { resolveMedicalError } from './medicalError'
import {
  createMedicalWorkbenchSummaryDefaults,
  normalizeRiskIndex,
  resolveMedicalRiskColor,
  resolveMedicalRiskLabel
} from '../../utils/medicalSummary'
import type { ElderAccount, ElderAccountLog, MedicalCareWorkbenchSummary, PageResult } from '../../types'

const router = useRouter()
const loading = ref(false)
const errorText = ref('')

const medicalSummary = ref<MedicalCareWorkbenchSummary>(createMedicalWorkbenchSummaryDefaults())

const warningRows = ref<ElderAccount[]>([])
const logRows = ref<ElderAccountLog[]>([])

const warningCount = computed(() => warningRows.value.length)
const riskIndex = computed(() => normalizeRiskIndex(medicalSummary.value.riskIndex))
const riskTagColor = computed(() => resolveMedicalRiskColor(medicalSummary.value.riskLevel))
const riskLevelLabel = computed(() => resolveMedicalRiskLabel(medicalSummary.value.riskLevel))
const riskSignals = computed(() => {
  const source = medicalSummary.value.riskSignals || []
  if (source.length > 0) {
    return source
  }
  const rows: string[] = []
  if (Number(medicalSummary.value.overdueCareTaskCount || 0) > 0) {
    rows.push(`护理超时 ${medicalSummary.value.overdueCareTaskCount} 条`)
  }
  if (Number(medicalSummary.value.todayMedicationPendingCount || 0) > 0) {
    rows.push(`用药待执行 ${medicalSummary.value.todayMedicationPendingCount} 条`)
  }
  if (warningCount.value > 0) {
    rows.push(`账户余额预警 ${warningCount.value} 人`)
  }
  return rows
})

const warningColumns = [
  { title: '老人', dataIndex: 'elderName', key: 'elderName', width: 140 },
  { title: '余额', dataIndex: 'balance', key: 'balance', width: 100 },
  { title: '预警阈值', dataIndex: 'warnThreshold', key: 'warnThreshold', width: 120 },
  { title: '备注', dataIndex: 'remark', key: 'remark' }
]

const logColumns = [
  { title: '时间', dataIndex: 'createTime', key: 'createTime', width: 170 },
  { title: '老人', dataIndex: 'elderName', key: 'elderName', width: 140 },
  { title: '方向', dataIndex: 'direction', key: 'direction', width: 90 },
  { title: '金额', dataIndex: 'amount', key: 'amount', width: 100 },
  { title: '余额后', dataIndex: 'balanceAfter', key: 'balanceAfter', width: 110 },
  { title: '来源', dataIndex: 'sourceType', key: 'sourceType', width: 120 },
  { title: '备注', dataIndex: 'remark', key: 'remark' }
]

function go(path: string) {
  router.push(path)
}

async function load() {
  loading.value = true
  errorText.value = ''
  try {
    const [medicalData, warningData, logData] = await Promise.all([
      getMedicalCareWorkbenchSummary(),
      getElderAccountWarnings(),
      getElderAccountLogPage({ pageNo: 1, pageSize: 10 })
    ])
    medicalSummary.value = {
      ...createMedicalWorkbenchSummaryDefaults(),
      ...(medicalData || {})
    }
    warningRows.value = warningData || []
    const page = logData as PageResult<ElderAccountLog>
    logRows.value = page?.list || []
  } catch (error: any) {
    errorText.value = resolveMedicalError(error, '加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.risk-hero {
  border: 1px solid #dcecff;
  background:
    radial-gradient(140% 120% at 0% 0%, rgba(22, 119, 255, 0.1) 0%, rgba(22, 119, 255, 0) 56%),
    linear-gradient(135deg, #f7fbff 0%, #eef6ff 42%, #f8fcff 100%);
}

.risk-title {
  font-size: 14px;
  color: #1f2937;
  font-weight: 600;
}

.risk-score-row {
  margin-top: 8px;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.risk-score {
  font-size: 38px;
  line-height: 1;
  font-weight: 700;
  color: #0f172a;
}

.risk-subline {
  margin-top: 8px;
  color: #64748b;
  font-size: 12px;
}
</style>
