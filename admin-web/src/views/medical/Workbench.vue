<template>
  <PageContainer title="医护照护工作台" subTitle="Medical & Care 一体化工作入口">
    <a-row :gutter="16">
      <a-col :xs="24" :md="8">
        <a-card :bordered="false" class="card-elevated card-click" @click="go('/care/workbench/task-board?date=today')">
          <a-statistic title="卡片A：我的待办" :value="summary.pendingCareTaskCount" />
          <div class="desc">超时任务 {{ summary.overdueCareTaskCount }} · 巡检待完成 {{ summary.todayInspectionPendingCount }}</div>
        </a-card>
      </a-col>
      <a-col :xs="24" :md="8">
        <a-card :bordered="false" class="card-elevated card-click" @click="go('/health/inspection?date=today')">
          <a-statistic title="卡片F：巡检与体征" :value="summary.abnormalInspectionCount" />
          <div class="desc">今日已巡检 {{ summary.todayInspectionDoneCount }} · 待巡检 {{ summary.todayInspectionPendingCount }}</div>
        </a-card>
      </a-col>
      <a-col :xs="24" :md="8">
        <a-card :bordered="false" class="card-elevated card-click" @click="go('/health/medication/medication-registration?date=today')">
          <a-statistic title="卡片D：用药与预警" :value="summary.todayMedicationPendingCount" />
          <div class="desc">今日应服待执行 · 已执行 {{ summary.todayMedicationDoneCount }}</div>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="16" style="margin-top: 16px">
      <a-col :xs="24" :md="12">
        <a-card title="评估中心快捷入口" :bordered="false" class="card-elevated">
          <a-space wrap>
            <a-button type="primary" @click="go('/medical-care/assessment/tcm')">中医体质评估</a-button>
            <a-button type="primary" @click="go('/medical-care/assessment/cvd')">心血管风险评估</a-button>
            <a-button @click="go('/health/inspection')">健康巡检</a-button>
          </a-space>
          <a-divider />
          <a-space>
            <a-tag color="blue">已发布中医评估：{{ summary.tcmPublishedCount }}</a-tag>
            <a-tag color="red">高风险心血管：{{ summary.cvdHighRiskCount }}</a-tag>
            <a-tag color="orange">需随访：{{ summary.cvdNeedFollowupCount }}</a-tag>
          </a-space>
        </a-card>
      </a-col>
      <a-col :xs="24" :md="12">
        <a-card title="高风险长者 Top" :bordered="false" class="card-elevated">
          <a-empty v-if="!summary.keyResidents?.length" description="暂无高风险长者" />
          <a-list v-else :data-source="summary.keyResidents">
            <template #renderItem="{ item }">
              <a-list-item>
                <a-list-item-meta
                  :title="`${item.elderName || '-'}（${riskLabel(item.riskLevel)}）`"
                  :description="`${item.keyRiskFactors || '暂无因子说明'} · ${item.assessmentDate || '-'}`"
                />
                <template #actions>
                  <a-button type="link" @click="go(`/elder/resident-360?residentId=${item.elderId}&from=medicalCare`)">Resident360</a-button>
                </template>
              </a-list-item>
            </template>
          </a-list>
        </a-card>
      </a-col>
    </a-row>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import { getMedicalCareWorkbenchSummary } from '../../api/medicalCare'
import type { MedicalCareWorkbenchSummary } from '../../types'

const router = useRouter()

const summary = reactive<MedicalCareWorkbenchSummary>({
  pendingCareTaskCount: 0,
  overdueCareTaskCount: 0,
  todayInspectionPendingCount: 0,
  todayInspectionDoneCount: 0,
  abnormalInspectionCount: 0,
  todayMedicationPendingCount: 0,
  todayMedicationDoneCount: 0,
  tcmPublishedCount: 0,
  cvdHighRiskCount: 0,
  cvdNeedFollowupCount: 0,
  keyResidents: []
})

function go(path: string) {
  router.push(path)
}

function riskLabel(value?: string) {
  if (value === 'VERY_HIGH') return '极高风险'
  if (value === 'HIGH') return '高风险'
  if (value === 'MEDIUM') return '中风险'
  if (value === 'LOW') return '低风险'
  return value || '未分层'
}

async function load() {
  const data = await getMedicalCareWorkbenchSummary()
  Object.assign(summary, data)
}

onMounted(load)
</script>

<style scoped>
.card-click {
  cursor: pointer;
}
.desc {
  margin-top: 8px;
  color: var(--muted);
  font-size: 12px;
}
</style>
