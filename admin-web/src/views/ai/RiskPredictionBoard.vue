<template>
  <PageContainer title="健康风险预测看板" subTitle="跌倒 / 压疮 / 再入院风险评分，高风险自动生成告警">
    <a-row :gutter="12">
      <a-col :xs="12" :md="6">
        <a-card :bordered="false" class="card-elevated stat-card">
          <a-statistic title="覆盖长者" :value="summary.elderCount" />
        </a-card>
      </a-col>
      <a-col :xs="12" :md="6">
        <a-card :bordered="false" class="card-elevated stat-card">
          <a-statistic title="高风险项" :value="summary.highCount" :value-style="{ color: '#cf1322' }" />
        </a-card>
      </a-col>
      <a-col :xs="12" :md="6">
        <a-card :bordered="false" class="card-elevated stat-card">
          <a-statistic title="中风险项" :value="summary.mediumCount" :value-style="{ color: '#d48806' }" />
        </a-card>
      </a-col>
      <a-col :xs="12" :md="6">
        <a-card :bordered="false" class="card-elevated stat-card">
          <a-statistic title="低风险项" :value="summary.lowCount" :value-style="{ color: '#389e0d' }" />
        </a-card>
      </a-col>
    </a-row>

    <a-card :bordered="false" class="card-elevated" style="margin-top: 12px">
      <a-space wrap style="margin-bottom: 12px">
        <a-select
          v-model:value="query.riskType"
          allow-clear
          placeholder="风险类型"
          style="width: 150px"
          :options="riskTypeOptions"
          @change="reload"
        />
        <a-select
          v-model:value="query.riskLevel"
          allow-clear
          placeholder="风险等级"
          style="width: 130px"
          :options="[
            { label: '高风险', value: 'HIGH' },
            { label: '中风险', value: 'MEDIUM' },
            { label: '低风险', value: 'LOW' }
          ]"
          @change="reload"
        />
        <a-input-search
          v-model:value="query.keyword"
          placeholder="搜索长者姓名"
          style="width: 200px"
          allow-clear
          @search="reload"
        />
        <a-button type="primary" :loading="recomputing" @click="handleRecompute">全员重算</a-button>
        <a-button @click="modelVisible = true">评分模型配置</a-button>
      </a-space>

      <a-space wrap style="margin-bottom: 12px">
        <a-tag v-for="stat in summary.typeStats" :key="stat.riskType" color="geekblue">
          {{ riskTypeName(stat.riskType) }}：高 {{ stat.highCount }} / 中 {{ stat.mediumCount }} / 低 {{ stat.lowCount }}
        </a-tag>
      </a-space>

      <a-table
        :data-source="scores"
        :columns="columns"
        :loading="loading"
        :pagination="{ current: pageNo, pageSize, total, onChange: onPageChange }"
        row-key="id"
        size="middle"
        :scroll="{ x: 900 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'riskType'">
            {{ riskTypeName(record.riskType) }}
          </template>
          <template v-else-if="column.key === 'riskLevel'">
            <a-tag :color="levelColor(record.riskLevel)">{{ levelText(record.riskLevel) }}</a-tag>
          </template>
          <template v-else-if="column.key === 'alert'">
            <a-tag v-if="record.alertId" color="red">已生成告警</a-tag>
            <span v-else>-</span>
          </template>
          <template v-else-if="column.key === 'action'">
            <a @click="openDetail(record)">因子明细/趋势</a>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-drawer v-model:open="drawerVisible" :title="drawerTitle" width="520">
      <template v-if="current">
        <a-descriptions :column="2" size="small" bordered style="margin-bottom: 16px">
          <a-descriptions-item label="长者">{{ current.elderName }}</a-descriptions-item>
          <a-descriptions-item label="风险类型">{{ riskTypeName(current.riskType) }}</a-descriptions-item>
          <a-descriptions-item label="评分">{{ current.score }}</a-descriptions-item>
          <a-descriptions-item label="等级">
            <a-tag :color="levelColor(current.riskLevel)">{{ levelText(current.riskLevel) }}</a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="评估时间" :span="2">{{ current.assessTime || '-' }}</a-descriptions-item>
        </a-descriptions>

        <h4>因子明细</h4>
        <a-table
          :data-source="currentFactors"
          :columns="factorColumns"
          :pagination="false"
          row-key="code"
          size="small"
          style="margin-bottom: 16px"
        />

        <h4>近30天趋势</h4>
        <div v-if="trend.length" class="trend-box">
          <svg :viewBox="`0 0 ${trendWidth} 120`" preserveAspectRatio="none" class="trend-svg">
            <polyline :points="trendPoints" fill="none" stroke="#1677ff" stroke-width="2" />
            <circle
              v-for="(p, i) in trendCoords"
              :key="i"
              :cx="p.x"
              :cy="p.y"
              r="3"
              :fill="levelDotColor(trend[i].riskLevel)"
            />
          </svg>
          <div class="trend-labels">
            <span>{{ trend[0].assessDate }}</span>
            <span>{{ trend[trend.length - 1].assessDate }}</span>
          </div>
        </div>
        <a-empty v-else description="暂无趋势数据" />
      </template>
    </a-drawer>

    <a-modal v-model:open="modelVisible" title="风险评分模型配置" width="720" :footer="null">
      <a-alert
        type="info"
        show-icon
        style="margin-bottom: 12px"
        message="规则JSON：factors（因子+权重+档位分）与 levels（分级阈值）。保存后对本机构生效，覆盖全局默认。"
      />
      <a-collapse>
        <a-collapse-panel v-for="model in models" :key="model.riskType" :header="`${riskTypeName(model.riskType)}（${model.modelName || ''}）`">
          <a-form layout="vertical">
            <a-form-item label="启用">
              <a-switch :checked="model.enabled !== 0" @change="(v: boolean) => (model.enabled = v ? 1 : 0)" />
            </a-form-item>
            <a-form-item label="规则 JSON">
              <a-textarea v-model:value="model.ruleJson" :rows="8" />
            </a-form-item>
            <a-button type="primary" size="small" :loading="modelSaving" @click="handleModelSave(model)">保存该模型</a-button>
          </a-form>
        </a-collapse-panel>
      </a-collapse>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import {
  getAiRiskModels,
  getAiRiskScorePage,
  getAiRiskSummary,
  getAiRiskTrend,
  recomputeAiRisk,
  saveAiRiskModel,
  type AiRiskModel,
  type AiRiskScoreItem,
  type AiRiskSummary,
  type AiRiskTrendPoint
} from '../../api/ai'

const riskTypeOptions = [
  { label: '跌倒风险', value: 'FALL' },
  { label: '压疮风险', value: 'PRESSURE_ULCER' },
  { label: '再入院风险', value: 'READMISSION' }
]

const query = reactive<{ riskType?: string; riskLevel?: string; keyword?: string }>({})
const scores = ref<AiRiskScoreItem[]>([])
const loading = ref(false)
const recomputing = ref(false)
const pageNo = ref(1)
const pageSize = ref(10)
const total = ref(0)

const summary = reactive<AiRiskSummary>({ elderCount: 0, highCount: 0, mediumCount: 0, lowCount: 0, typeStats: [] })

const drawerVisible = ref(false)
const current = ref<AiRiskScoreItem | null>(null)
const trend = ref<AiRiskTrendPoint[]>([])

const modelVisible = ref(false)
const modelSaving = ref(false)
const models = ref<AiRiskModel[]>([])

const columns = [
  { title: '长者', dataIndex: 'elderName', key: 'elderName', width: 140 },
  { title: '风险类型', key: 'riskType', width: 120 },
  { title: '评分', dataIndex: 'score', key: 'score', width: 90 },
  { title: '等级', key: 'riskLevel', width: 90 },
  { title: '评估时间', dataIndex: 'assessTime', key: 'assessTime', width: 170 },
  { title: '告警', key: 'alert', width: 110 },
  { title: '操作', key: 'action', width: 140, fixed: 'right' as const }
]

const factorColumns = [
  { title: '因子', dataIndex: 'name', key: 'name' },
  { title: '取值', key: 'value', customRender: ({ record }: any) => (record.value == null ? '缺失' : record.value) },
  { title: '得分', dataIndex: 'score', key: 'score' }
]

const drawerTitle = computed(() =>
  current.value ? `${current.value.elderName || ''} · ${riskTypeName(current.value.riskType)}` : '因子明细'
)

const currentFactors = computed(() => {
  if (!current.value?.factorJson) return []
  try {
    const parsed = JSON.parse(current.value.factorJson)
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return []
  }
})

const trendWidth = 460
const trendCoords = computed(() => {
  if (!trend.value.length) return []
  const maxScore = Math.max(...trend.value.map((p) => Number(p.score) || 0), 100)
  const step = trend.value.length > 1 ? trendWidth / (trend.value.length - 1) : 0
  return trend.value.map((p, i) => ({
    x: trend.value.length > 1 ? Math.round(i * step) : trendWidth / 2,
    y: Math.round(110 - ((Number(p.score) || 0) / maxScore) * 100)
  }))
})
const trendPoints = computed(() => trendCoords.value.map((p) => `${p.x},${p.y}`).join(' '))

function riskTypeName(type?: string) {
  if (type === 'FALL') return '跌倒风险'
  if (type === 'PRESSURE_ULCER') return '压疮风险'
  if (type === 'READMISSION') return '再入院风险'
  return type || '-'
}

function levelText(level?: string) {
  if (level === 'HIGH') return '高风险'
  if (level === 'MEDIUM') return '中风险'
  return '低风险'
}

function levelColor(level?: string) {
  if (level === 'HIGH') return 'red'
  if (level === 'MEDIUM') return 'orange'
  return 'green'
}

function levelDotColor(level?: string) {
  if (level === 'HIGH') return '#cf1322'
  if (level === 'MEDIUM') return '#d48806'
  return '#389e0d'
}

async function loadSummary() {
  try {
    Object.assign(summary, await getAiRiskSummary())
  } catch {
    // 忽略汇总加载失败
  }
}

async function loadScores() {
  loading.value = true
  try {
    const res = await getAiRiskScorePage({
      pageNo: pageNo.value,
      pageSize: pageSize.value,
      riskType: query.riskType,
      riskLevel: query.riskLevel,
      keyword: query.keyword
    })
    scores.value = res.list || []
    total.value = res.total || 0
  } catch (error: any) {
    message.error(error?.message || '加载评分列表失败')
  } finally {
    loading.value = false
  }
}

function reload() {
  pageNo.value = 1
  loadScores()
}

function onPageChange(no: number, size: number) {
  pageNo.value = no
  pageSize.value = size
  loadScores()
}

async function handleRecompute() {
  recomputing.value = true
  try {
    const res = await recomputeAiRisk({})
    message.success(`重算完成，共生成 ${res.scoreCount} 条评分`)
    await Promise.all([loadSummary(), loadScores()])
  } catch (error: any) {
    message.error(error?.message || '重算失败')
  } finally {
    recomputing.value = false
  }
}

async function openDetail(record: AiRiskScoreItem) {
  current.value = record
  drawerVisible.value = true
  trend.value = []
  try {
    trend.value = (await getAiRiskTrend({ elderId: record.elderId, riskType: record.riskType, days: 30 })) || []
  } catch {
    trend.value = []
  }
}

async function loadModels() {
  try {
    models.value = (await getAiRiskModels()) || []
  } catch {
    models.value = []
  }
}

async function handleModelSave(model: AiRiskModel) {
  modelSaving.value = true
  try {
    await saveAiRiskModel({
      riskType: model.riskType,
      modelName: model.modelName,
      ruleJson: model.ruleJson,
      enabled: model.enabled,
      remark: model.remark
    })
    message.success('模型已保存（机构级覆盖）')
    await loadModels()
  } catch (error: any) {
    message.error(error?.message || '保存失败')
  } finally {
    modelSaving.value = false
  }
}

onMounted(() => {
  loadSummary()
  loadScores()
  loadModels()
})
</script>

<style scoped>
.stat-card {
  margin-bottom: 12px;
}
.trend-box {
  border: 1px solid var(--border-color, #f0f0f0);
  border-radius: 6px;
  padding: 8px;
}
.trend-svg {
  width: 100%;
  height: 120px;
}
.trend-labels {
  display: flex;
  justify-content: space-between;
  color: var(--muted, #999);
  font-size: 12px;
}
</style>
