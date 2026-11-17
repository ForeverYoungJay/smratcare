<template>
  <PageContainer class="rm-page" title="长者健康档案" sub-title="统一长者身份；医疗信息与护理摘要按权限共享；电子病历与历史事件留痕">
    <div class="ha">
      <section class="rm-card ha-list">
        <div class="rm-card__head"><span class="rm-card__title">长者患者列表</span><span class="rm-pill rm-pill--success">在住 {{ total }} 人</span></div>
        <a-input-search v-model:value="keyword" allow-clear placeholder="搜索姓名、房间、床位" @search="search" />
        <a-spin :spinning="listLoading">
          <button v-for="elder in elders" :key="String(elder.id)" type="button" class="ha-item" :class="{ active: String(elder.id) === residentId }" @click="select(elder.id)">
            <span class="rm-avatar">{{ (elder.fullName || '长').slice(0, 1) }}</span>
            <span class="ha-item__main"><strong>{{ elder.fullName }}</strong><small>{{ [elder.roomNo, elder.bedNo].filter(Boolean).join(' / ') || '未分配床位' }}</small></span>
            <span v-if="elder.careLevel" class="rm-pill">{{ elder.careLevel }}</span>
          </button>
          <div v-if="!elders.length" class="ha-empty">没有符合条件的在住长者</div>
        </a-spin>
        <a-pagination v-if="total > PAGE_SIZE" v-model:current="pageNo" size="small" simple :page-size="PAGE_SIZE" :total="total" @change="loadList" />
        <p class="ha-muted">与长者管理共享身份主数据；健康记录由医务部维护。</p>
      </section>

      <div class="ha-detail">
        <section v-if="!residentId" class="rm-card ha-empty">从左侧选择一位长者查看健康档案</section>
        <template v-else>
          <section class="rm-card">
            <div class="ha-head">
              <span class="rm-avatar ha-avatar">{{ (detail?.fullName || '长').slice(0, 1) }}</span>
              <div class="ha-head__info">
                <div class="ha-head__name"><strong>{{ detail?.fullName || '--' }}</strong><span v-if="detail?.careLevel" class="rm-pill rm-pill--info">{{ detail.careLevel }}</span></div>
                <div class="ha-muted">{{ metaLine }}</div>
                <div class="ha-tags">
                  <span v-for="d in diseases" :key="String(d.id)" class="rm-pill rm-pill--danger">{{ d.diseaseName }}</span>
                  <span v-for="tag in profile?.riskTags || []" :key="tag" class="rm-pill rm-pill--warning">{{ tag }}</span>
                </div>
              </div>
              <a-space>
                <a-button @click="go('emr')">查看病历</a-button>
                <a-button type="primary" @click="router.push(`/medical-care/basic-diseases?residentId=${residentId}`)">维护疾病信息</a-button>
              </a-space>
            </div>
            <a-tabs :active-key="tab" class="ha-tabs" @change="(key: string | number) => go(String(key))">
              <a-tab-pane v-for="item in ARCHIVE_TABS" :key="item.key" :tab="item.label" />
            </a-tabs>
          </section>

          <template v-if="tab === 'overview'">
            <div v-if="riskCard" class="rm-kpis ha-gap">
              <div class="rm-kpi"><span class="rm-kpi__label">24 小时体征异常</span><span class="rm-kpi__value">{{ riskCard.abnormalVital24hCount }}<small>次</small></span></div>
              <div class="rm-kpi"><span class="rm-kpi__label">巡检待闭环</span><span class="rm-kpi__value">{{ riskCard.abnormalInspectionOpenCount }}<small>项</small></span></div>
              <div class="rm-kpi"><span class="rm-kpi__label">未闭环事件</span><span class="rm-kpi__value">{{ riskCard.openIncidentCount }}<small>件</small></span></div>
              <div class="rm-kpi"><span class="rm-kpi__label">待执行护理任务</span><span class="rm-kpi__value">{{ riskCard.pendingCareTaskCount }}<small>项</small></span></div>
            </div>
            <div class="ha-cards ha-gap">
              <section v-for="card in overview?.cards || []" :key="card.key" class="rm-card">
                <div class="rm-card__head"><span class="rm-card__title">{{ card.title }}</span><span class="rm-pill" :class="toneOf(card.tagColor)">{{ card.tag }}</span></div>
                <p v-if="card.description" class="ha-muted">{{ card.description }}</p>
                <div v-for="line in card.lines" :key="line" class="ha-line">{{ line }}</div>
                <a-space wrap class="ha-gap-sm">
                  <a v-for="action in card.actions" :key="action.path" class="rm-link" @click="router.push(action.path)">{{ action.label }} →</a>
                </a-space>
              </section>
              <section v-if="!overview?.cards?.length" class="rm-card ha-empty">暂无健康概览数据</section>
            </div>
          </template>

          <section v-else-if="tab === 'diseases'" class="rm-card ha-gap">
            <div class="rm-card__head"><span class="rm-card__title">基础疾病</span><a class="rm-link" @click="router.push(`/medical-care/basic-diseases?residentId=${residentId}`)">维护 →</a></div>
            <div v-if="!diseases.length" class="ha-empty">未登记基础疾病</div>
            <div v-for="d in diseases" :key="String(d.id)" class="ha-line">{{ d.diseaseName }}</div>
            <a-alert class="ha-gap" type="warning" show-icon message="过敏史以电子病历中医生签署的记录为准；在病历标签页查看。" />
          </section>

          <section v-else-if="tab === 'events'" class="rm-card ha-gap">
            <div class="rm-card__head"><span class="rm-card__title">近 30 天医护事件</span></div>
            <div v-if="!events.length" class="ha-empty">近 30 天没有风险变化记录</div>
            <a-timeline>
              <a-timeline-item v-for="point in events" :key="point.date" :color="point.riskLevel === 'HIGH' || point.riskLevel === 'CRITICAL' ? 'red' : 'green'">
                <strong>{{ point.date }} · 风险指数 {{ point.riskScore ?? '--' }}</strong>
                <div class="ha-muted">待用药 {{ point.medicationPendingCount ?? 0 }} · 巡检待闭环 {{ point.inspectionOpenCount ?? 0 }} · 护理超时 {{ point.careOverdueCount ?? 0 }} · 事件 {{ point.incidentCount ?? 0 }}</div>
              </a-timeline-item>
            </a-timeline>
          </section>

          <section v-else class="rm-card ha-gap">
            <EmbeddedPage :key="`${tab}-${residentId}`">
              <Inspection v-if="tab === 'vitals'" />
              <EmrRecords v-else-if="tab === 'emr'" />
              <MedicalOrders v-else-if="tab === 'orders'" />
              <MedicationRegistration v-else-if="tab === 'medication'" />
            </EmbeddedPage>
          </section>
        </template>
      </div>
    </div>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, ref, watch } from 'vue'
import dayjs from 'dayjs'
import { useRoute, useRouter } from 'vue-router'
import PageContainer from '../../../components/PageContainer.vue'
import EmbeddedPage from '../../marketing/center/EmbeddedPage.vue'
import { getElderCareProfile, getElderDetail, getElderDiseases, getElderPage } from '../../../api/elder'
import { getMedicalRiskTimeline, getResidentOverview, getResidentRiskCard } from '../../../api/medicalCare'
import type { ElderCareProfile, ElderDiseaseItem, ElderItem, Id, MedicalResidentOverview, MedicalResidentRiskCard, MedicalRiskTimelinePoint } from '../../../types'
import { pickTab } from './centerTabs'
import '../../elder/residentUi.css'

const Inspection = defineAsyncComponent(() => import('../../health/Inspection.vue'))
const EmrRecords = defineAsyncComponent(() => import('../../emr/EmrRecords.vue'))
const MedicalOrders = defineAsyncComponent(() => import('../../medorder/MedicalOrders.vue'))
const MedicationRegistration = defineAsyncComponent(() => import('../../health/MedicationRegistration.vue'))

const ARCHIVE_TABS = [
  { key: 'overview', label: '健康概览' }, { key: 'diseases', label: '疾病与过敏' }, { key: 'vitals', label: '生命体征' },
  { key: 'emr', label: '电子病历' }, { key: 'orders', label: '医嘱' }, { key: 'medication', label: '用药记录' }, { key: 'events', label: '历史事件' }
]
const PAGE_SIZE = 12

const route = useRoute()
const router = useRouter()
const residentId = computed(() => (typeof route.query.residentId === 'string' ? route.query.residentId : ''))
const tab = computed(() => pickTab(ARCHIVE_TABS, route.query.tab)!.key)

const keyword = ref('')
const pageNo = ref(1)
const total = ref(0)
const elders = ref<ElderItem[]>([])
const listLoading = ref(false)
const detail = ref<ElderItem | null>(null)
const profile = ref<ElderCareProfile | null>(null)
const diseases = ref<ElderDiseaseItem[]>([])
const riskCard = ref<MedicalResidentRiskCard | null>(null)
const overview = ref<MedicalResidentOverview | null>(null)
const events = ref<MedicalRiskTimelinePoint[]>([])

const metaLine = computed(() => {
  const e = detail.value
  if (!e) return ''
  const age = e.birthDate ? `${dayjs().diff(e.birthDate, 'year')}岁` : ''
  const gender = e.gender === 1 ? '男' : e.gender === 2 ? '女' : ''
  return [age, gender, [e.roomNo, e.bedNo].filter(Boolean).join(' / '), e.elderCode && `长者编号 ${e.elderCode}`].filter(Boolean).join(' · ')
})

function toneOf(color?: string) {
  return { red: 'rm-pill--danger', orange: 'rm-pill--warning', gold: 'rm-pill--warning', green: 'rm-pill--success', blue: 'rm-pill--info' }[color || ''] || ''
}
// 嵌入的病历、医嘱、巡检、用药页都按 query.residentId 过滤
function select(id: Id) { router.replace({ path: route.path, query: { residentId: String(id), tab: tab.value } }) }
function go(next: string) { router.replace({ path: route.path, query: { residentId: residentId.value, tab: next } }) }
function search() { pageNo.value = 1; loadList() }

async function loadList() {
  listLoading.value = true
  try {
    const res = await getElderPage({ pageNo: pageNo.value, pageSize: PAGE_SIZE, lifecycleStatus: 'IN_HOSPITAL', keyword: keyword.value || undefined })
    elders.value = res.list || []
    total.value = Number(res.total || 0)
    if (!residentId.value && elders.value.length) select(elders.value[0].id)
  } finally {
    listLoading.value = false
  }
}

async function loadDetail() {
  const id = residentId.value
  if (!id) return
  const quiet = { silent403: true }
  const [d, p, ds, rc, ov, ev] = await Promise.allSettled([
    getElderDetail(id), getElderCareProfile(id), getElderDiseases(id), getResidentRiskCard(id), getResidentOverview(id),
    getMedicalRiskTimeline({ elderId: id, windowDays: 30 }, quiet)
  ])
  if (id !== residentId.value) return
  detail.value = d.status === 'fulfilled' ? d.value : null
  profile.value = p.status === 'fulfilled' ? p.value : null
  diseases.value = ds.status === 'fulfilled' ? ds.value || [] : []
  riskCard.value = rc.status === 'fulfilled' ? rc.value : null
  overview.value = ov.status === 'fulfilled' ? ov.value : null
  events.value = ev.status === 'fulfilled' ? [...(ev.value || [])].reverse() : []
}

onMounted(() => { loadList(); loadDetail() })
watch(residentId, loadDetail)
</script>

<style scoped>
.ha { display: grid; grid-template-columns: 300px minmax(0, 1fr); gap: 16px; align-items: start; }
.ha-list { display: grid; gap: 10px; }
.ha-item { display: flex; align-items: center; gap: 10px; width: 100%; padding: 10px 8px; border: 0; border-bottom: 1px solid #eef1ee; background: none; text-align: left; cursor: pointer; }
.ha-item.active { border-radius: 8px; background: #eef8f3; }
.ha-item__main { display: grid; flex: 1; gap: 2px; min-width: 0; }
.ha-item__main small, .ha-muted { color: #7b877e; font-size: 12px; }
.ha-detail { min-width: 0; }
.ha-head { display: flex; flex-wrap: wrap; align-items: flex-start; gap: 16px; }
.ha-avatar { width: 56px; height: 56px; font-size: 22px; }
.ha-head__info { display: grid; flex: 1; gap: 6px; min-width: 240px; }
.ha-head__name { display: flex; align-items: center; gap: 8px; font-size: 18px; }
.ha-tags { display: flex; flex-wrap: wrap; gap: 6px; }
.ha-tabs { margin-top: 12px; margin-bottom: -16px; }
.ha-gap { margin-top: 16px; }
.ha-gap-sm { margin-top: 8px; }
.ha-cards { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 16px; }
.ha-line { padding: 8px 0; border-bottom: 1px solid #f0f3f1; color: #33433a; font-size: 13px; }
.ha-empty { padding: 32px 12px; color: #7b877e; text-align: center; }
@media (max-width: 1000px) { .ha { grid-template-columns: 1fr; } }
</style>
