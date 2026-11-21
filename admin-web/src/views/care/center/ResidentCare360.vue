<template>
  <PageContainer class="rm-page" title="长者护理档案" sub-title="护理人员按权限查看；与长者管理共用同一份基本资料，医疗诊断与医嘱只显示授权摘要。">
    <template #extra>
      <a-space wrap>
        <a-button @click="router.push('/care/residents')">切换长者</a-button>
        <a-button type="primary" @click="go('plan')">编辑护理计划</a-button>
      </a-space>
    </template>

    <section class="rm-card">
      <a-skeleton v-if="!elder" active :paragraph="{ rows: 2 }" />
      <div v-else class="r3-head">
        <span class="rm-avatar r3-avatar">{{ elder.fullName.slice(0, 1) }}</span>
        <div class="r3-info">
          <div class="r3-name">
            <strong>{{ elder.fullName }}</strong>
            <span class="rm-pill rm-pill--success">{{ lifecycleText }}</span>
            <span v-if="hasOpenIncident" class="rm-pill rm-pill--warning">有未闭环异常</span>
          </div>
          <div class="r3-meta">{{ metaLine }}</div>
          <div class="r3-meta">护理等级：{{ profile?.currentCareLevel || elder.careLevel || '--' }} ｜ 责任人员：{{ elder.responsibleStaffName || elder.caregiverNames?.join('、') || elder.caregiverName || '--' }}</div>
        </div>
        <div class="r3-tags">
          <span v-for="tag in profile?.riskTags || []" :key="tag" class="rm-pill rm-pill--danger">{{ tag }}</span>
        </div>
      </div>
      <a-tabs :active-key="tab" class="r3-tabs" @change="(key: string | number) => go(String(key))">
        <a-tab-pane v-for="item in RESIDENT_TABS" :key="item.key" :tab="item.label" />
      </a-tabs>
    </section>

    <template v-if="tab === 'overview'">
      <div class="rm-split r3-grid">
        <section class="rm-card">
          <div class="rm-card__head"><span class="rm-card__title">护理基本信息</span></div>
          <div class="r3-facts">
            <div v-for="fact in facts" :key="fact.label"><small>{{ fact.label }}</small><strong>{{ fact.value || '--' }}</strong></div>
          </div>
          <a-alert v-if="profile?.adjustmentRequired" type="warning" show-icon style="margin-top: 12px"
            :message="`建议护理等级调整为 ${profile.suggestedCareLevel}`" :description="profile.recommendationReason" />
        </section>
        <section class="rm-card">
          <div class="rm-card__head">
            <span class="rm-card__title">最新评估摘要</span>
            <a class="rm-link" @click="go('assessment')">发起复评 →</a>
          </div>
          <div v-if="latestAssessment" class="r3-facts r3-facts--3">
            <div><small>能力评估</small><strong>{{ latestAssessment.levelCode || '--' }}</strong><em>{{ latestAssessment.assessmentDate }}</em></div>
            <div><small>评估得分</small><strong>{{ latestAssessment.score ?? '--' }}</strong><em>{{ latestAssessment.assessmentType }}</em></div>
            <div><small>评估结论</small><strong class="r3-small">{{ latestAssessment.resultSummary || '--' }}</strong></div>
          </div>
          <div v-else class="r3-empty">还没有正式评估记录</div>
          <div v-if="riskCard" class="r3-medical">
            医务授权摘要：近 24 小时体征异常 {{ riskCard.abnormalVital24hCount }} 次 · 巡检待闭环 {{ riskCard.abnormalInspectionOpenCount }} 项 · 待执行护理任务 {{ riskCard.pendingCareTaskCount }} 项
          </div>
        </section>
      </div>
      <section class="rm-card r3-gap">
        <div class="rm-card__head">
          <span class="rm-card__title">近期护理服务记录</span>
          <a class="rm-link" @click="go('records')">查看全部 →</a>
        </div>
        <div v-if="!recentRecords.length" class="r3-empty">暂无服务记录</div>
        <div v-for="record in recentRecords" :key="`${record.recordType}-${record.sourceId}`" class="rm-row">
          <div class="rm-row__main">
            <span class="rm-row__title">{{ record.recordTime ? dayjs(record.recordTime).format('MM-DD HH:mm') : '--' }} · {{ record.serviceName || record.planName || '护理服务' }}</span>
            <span class="rm-row__meta">{{ record.staffName || '--' }} · {{ record.remark || nursingRecordStatusLabel(record.status) }}</span>
          </div>
        </div>
      </section>
    </template>

    <section v-else class="rm-card">
      <a-steps v-if="tab === 'assessment'" :current="flowStep" size="small" class="r3-steps"
        :items="['评估登记', '指标评估', '结果复核', '制定护理计划', '审核发布'].map((title) => ({ title }))" />
      <a-alert v-if="tab === 'assessment'" type="info" show-icon class="r3-steps"
        message="能力评估按 GB/T 42195—2022 的指标与评分规则执行；跌倒、压疮、噎食、走失等风险工具和阈值以机构制度为准。评估完成后到「护理计划」制定计划并提交审核。" />
      <EmbeddedPage :key="tab">
        <AssessmentRecords v-if="tab === 'assessment'" assessment-type="CONTINUOUS" title="能力与风险评估" sub-title="" />
        <ServicePlans v-else-if="tab === 'plan'" />
        <NursingRecords v-else-if="tab === 'records'" />
        <template v-else>
          <div class="rm-card__head"><span class="rm-card__title">异常事件</span><a class="rm-link" @click="router.push(`/care/safety?tab=incident&action=create&elderId=${elderId}`)">上报异常 →</a></div>
          <a-table row-key="id" size="small" :data-source="incidents" :columns="incidentColumns" :pagination="false">
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'type'">{{ INCIDENT_TYPES[record.incidentType] || record.incidentType }}</template>
              <template v-else-if="column.key === 'status'"><span class="rm-pill" :class="`rm-pill--${INCIDENT_STATUS[record.status]?.tone}`">{{ INCIDENT_STATUS[record.status]?.label || record.status }}</span></template>
              <template v-else-if="column.key === 'action'"><a class="rm-link" @click="router.push(`/care/safety?tab=incident&id=${record.id}`)">查看</a></template>
            </template>
          </a-table>
          <div class="rm-card__head r3-gap"><span class="rm-card__title">护理任务异常</span></div>
          <Exception />
        </template>
      </EmbeddedPage>
    </section>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import dayjs from 'dayjs'
import { useRoute, useRouter } from 'vue-router'
import PageContainer from '../../../components/PageContainer.vue'
import EmbeddedPage from '../../marketing/center/EmbeddedPage.vue'
import AssessmentRecords from '../../assessment/AssessmentRecords.vue'
import ServicePlans from '../ServicePlans.vue'
import NursingRecords from '../NursingRecords.vue'
import Exception from '../Exception.vue'
import { getElderCareProfile, getElderDetail } from '../../../api/elder'
import { getResidentRiskCard } from '../../../api/medicalCare'
import { getIncidentPage, getNursingRecordPage, getServicePlanPage } from '../../../api/nursing'
import { getAssessmentRecordPage } from '../../../api/assessment'
import { nursingRecordStatusLabel } from '../../../utils/nursingEnums'
import type { AssessmentRecord, CareIncidentItem, ElderCareProfile, ElderItem, MedicalResidentRiskCard, NursingRecordItem, ServicePlanItem } from '../../../types'
import { INCIDENT_STATUS, INCIDENT_TYPES, RESIDENT_TABS, resolveTab } from './nursingModel'
import { assessmentFlowStep } from './residentCare'
import '../../elder/residentUi.css'

const route = useRoute()
const router = useRouter()
const elderId = computed(() => String(route.params.id))
const tab = computed(() => resolveTab(RESIDENT_TABS, route.query))
const elder = ref<ElderItem | null>(null)
const profile = ref<ElderCareProfile | null>(null)
const riskCard = ref<MedicalResidentRiskCard | null>(null)
const assessments = ref<AssessmentRecord[]>([])
const plans = ref<ServicePlanItem[]>([])
const recentRecords = ref<NursingRecordItem[]>([])
const incidents = ref<CareIncidentItem[]>([])

const LIFECYCLE: Record<string, string> = { IN_HOSPITAL: '在住', TRIAL: '试住', OUTING: '外出', MEDICAL_OUTING: '外出就医', DISCHARGE_PENDING: '退住办理中', DISCHARGED: '已退住' }
const lifecycleText = computed(() => LIFECYCLE[elder.value?.lifecycleStatus || ''] || elder.value?.lifecycleStatus || '在住')
const hasOpenIncident = computed(() => incidents.value.some((item) => item.status !== 'CLOSED'))
const latestAssessment = computed(() => assessments.value.find((item) => item.status !== 'DRAFT') || null)
const flowStep = computed(() => assessmentFlowStep(assessments.value[0], plans.value))
const metaLine = computed(() => {
  const e = elder.value
  if (!e) return ''
  const age = e.birthDate ? `${dayjs().diff(e.birthDate, 'year')}岁` : ''
  const gender = e.gender === 1 ? '男' : e.gender === 2 ? '女' : ''
  return [gender, age, e.elderCode && `长者编号 ${e.elderCode}`, [e.roomNo, e.bedNo].filter(Boolean).join(' / '), (e.checkInDate || e.admissionDate) && `入住 ${e.checkInDate || e.admissionDate}`].filter(Boolean).join(' · ')
})
const facts = computed(() => [
  { label: '当前护理等级', value: profile.value?.currentCareLevel || elder.value?.careLevel },
  { label: '建议护理等级', value: profile.value?.suggestedCareLevel },
  { label: '风险标记', value: (profile.value?.riskTags || []).join('、') },
  { label: '在执行护理计划', value: `${plans.value.filter((p) => p.approvalStatus !== 'REJECTED').length} 项` },
  { label: '下次复评', value: latestAssessment.value?.nextAssessmentDate },
  { label: '基础疾病', value: (elder.value?.diseases || []).map((d: any) => d.diseaseName || d.name).filter(Boolean).join('、') }
])
const incidentColumns = [
  { title: '时间', dataIndex: 'occurredAt', width: 150 },
  { title: '编号', dataIndex: 'incidentNo', width: 140 },
  { title: '类型', key: 'type', width: 120 },
  { title: '描述', dataIndex: 'description', ellipsis: true },
  { title: '进度', key: 'status', width: 100 },
  { title: '', key: 'action', width: 60 }
]

// 嵌入的计划、记录、异常页按 query.residentId 过滤，所以 query 里始终带上它
function go(next: string) { router.replace({ path: route.path, query: { residentId: elderId.value, tab: next } }) }

async function load() {
  const id = elderId.value
  const [detail, care, risk, assess, planPage, records, incidentPage] = await Promise.allSettled([
    getElderDetail(id), getElderCareProfile(id), getResidentRiskCard(id),
    getAssessmentRecordPage({ pageNo: 1, pageSize: 20, elderId: id }),
    getServicePlanPage({ pageNo: 1, pageSize: 50, elderId: id }),
    getNursingRecordPage({ pageNo: 1, pageSize: 3, elderId: id }),
    getIncidentPage({ pageNo: 1, pageSize: 20, elderId: id })
  ])
  elder.value = detail.status === 'fulfilled' ? detail.value : null
  profile.value = care.status === 'fulfilled' ? care.value : null
  riskCard.value = risk.status === 'fulfilled' ? risk.value : null
  assessments.value = assess.status === 'fulfilled' ? assess.value.list || [] : []
  plans.value = planPage.status === 'fulfilled' ? planPage.value.list || [] : []
  recentRecords.value = records.status === 'fulfilled' ? records.value.list || [] : []
  incidents.value = incidentPage.status === 'fulfilled' ? incidentPage.value.list || [] : []
}

onMounted(() => {
  if (route.query.residentId !== elderId.value) router.replace({ path: route.path, query: { ...route.query, residentId: elderId.value } })
  load()
})
watch(elderId, load)
</script>

<style scoped>
.r3-head { display: flex; flex-wrap: wrap; align-items: flex-start; gap: 16px; }
.r3-avatar { width: 56px; height: 56px; font-size: 22px; }
.r3-info { display: grid; flex: 1; gap: 6px; min-width: 260px; }
.r3-name { display: flex; align-items: center; gap: 8px; font-size: 18px; }
.r3-meta { color: #5f7067; font-size: 13px; }
.r3-tags { display: flex; flex-wrap: wrap; gap: 6px; max-width: 280px; }
.r3-tabs { margin-top: 12px; margin-bottom: -16px; }
.r3-grid { margin-top: 16px; }
.r3-gap { margin-top: 16px; }
.r3-facts { display: grid; grid-template-columns: repeat(auto-fill, minmax(150px, 1fr)); gap: 10px; }
.r3-facts--3 { grid-template-columns: repeat(3, 1fr); }
.r3-facts > div { display: grid; gap: 4px; padding: 12px; border-radius: 10px; background: #f5f8f6; }
.r3-facts small { color: #7b877e; }
.r3-facts strong { color: #1e3027; font-size: 15px; }
.r3-facts em { color: #8a968f; font-size: 12px; font-style: normal; }
.r3-small { font-size: 13px !important; font-weight: 500; }
.r3-medical { margin-top: 12px; padding: 10px 12px; border-radius: 8px; background: #f3f7fb; color: #4a5d70; font-size: 12px; }
.r3-empty { padding: 20px 8px; color: #7b877e; text-align: center; }
.r3-steps { margin-bottom: 12px; }
@media (max-width: 720px) { .r3-facts--3 { grid-template-columns: 1fr; } }
</style>
