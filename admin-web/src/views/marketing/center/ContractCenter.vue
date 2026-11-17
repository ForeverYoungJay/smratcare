<template>
  <PageContainer title="合同与转入住" sub-title="让客户签约、护理评估、报价审批、财务确认与正式入住交接在一条流程中衔接。">
    <template #extra>
      <a-space wrap>
        <a-button @click="router.push('/marketing/contracts/pending')"><FileTextOutlined /> 合同台账</a-button>
        <a-button type="primary" @click="router.push({ path: '/marketing/contracts/pending', query: { quick: '1' } })"><PlusOutlined /> 发起签约</a-button>
      </a-space>
    </template>

    <div class="rm-page mk-contracts">
      <section class="rm-kpis">
        <div v-for="kpi in kpis" :key="kpi.label" class="rm-kpi">
          <span class="rm-kpi__label">{{ kpi.label }}</span>
          <span class="rm-kpi__value">{{ kpi.value }}</span>
          <span class="rm-kpi__hint">{{ kpi.hint }}</span>
          <span class="rm-kpi__icon" :class="kpi.tone ? `rm-kpi__icon--${kpi.tone}` : ''"><component :is="kpi.icon" /></span>
        </div>
      </section>

      <div class="mk-contracts__grid">
        <div class="mk-contracts__col">
          <section class="rm-card">
            <div class="rm-card__head">
              <h3 class="rm-card__title">合同办理列表</h3>
              <div class="rm-card__extra">
                <a-segmented v-model:value="filter" :options="filterOptions" @change="search" />
              </div>
            </div>
            <a-input-search v-model:value="keyword" class="mk-contracts__search" allow-clear placeholder="搜索客户姓名、合同号" @search="search" />
            <a-table
              row-key="id"
              size="middle"
              :columns="columns"
              :data-source="rows"
              :loading="loading"
              :pagination="{ current: pageNo, pageSize: 8, total, showSizeChanger: false, onChange: changePage }"
              :scroll="{ x: 'max-content' }"
              :row-class-name="(record: CrmContractItem) => String(record.id) === String(selected?.id) ? 'mk-row--active' : ''"
              :custom-row="(record: CrmContractItem) => ({ onClick: () => select(record) })"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'status'">
                  <a-tag :color="contractStatusTag(record).color">{{ contractStatusTag(record).label }}</a-tag>
                </template>
                <template v-else-if="column.key === 'action'">
                  <a-button type="link" size="small" @click.stop="openLedger(record)">办理</a-button>
                </template>
              </template>
            </a-table>
            <p class="rm-card__sub">金额与收费以已审批的收费标准和财务实际到账为准；点击行查看签约流程进度。</p>
          </section>

          <section class="rm-card">
            <div class="rm-card__head"><h3 class="rm-card__title">转入住移交清单</h3></div>
            <div class="mk-contracts__counts">
              <div><span>待交接</span><strong>{{ handover.waiting }}<small>份</small></strong></div>
              <div><span>本月已交接</span><strong>{{ handover.doneThisMonth }}<small>份</small></strong></div>
              <div><span>资料待补齐</span><strong class="is-warning">{{ handover.incomplete }}<small>份</small></strong></div>
            </div>
            <ul class="mk-contracts__checks">
              <li><CheckCircleOutlined /> 客户基本资料、家属授权及联系方式</li>
              <li><CheckCircleOutlined /> 护理等级评估结果由专业部门出具</li>
              <li><CheckCircleOutlined /> 合同签署、必要资料及财务确认</li>
            </ul>
          </section>
        </div>

        <div class="mk-contracts__col">
          <section class="rm-card">
            <div class="rm-card__head">
              <h3 class="rm-card__title">签约流程进度<template v-if="selected"> · {{ selected.elderName || selected.name || '-' }}</template></h3>
              <a-tag v-if="selected" class="rm-card__extra" :color="contractStatusTag(selected).color">{{ contractStatusTag(selected).label }}</a-tag>
            </div>
            <a-empty v-if="!selected" description="点击左侧合同查看进度" />
            <template v-else>
              <p class="rm-card__sub">
                合同号 {{ selected.contractNo || '-' }} · 负责人 {{ selected.marketerName || '-' }}<template v-if="selected.reservationRoomNo"> · 意向床位 {{ selected.reservationRoomNo }}</template>
                <br>{{ contractTypeLabel(selected.contractType) }} · 参考月费 {{ selected.referenceMonthlyFee != null ? `¥${selected.referenceMonthlyFee}` : '-' }} · 预计入住 {{ selected.expectedAdmissionDate || '-' }}
              </p>
              <a-spin :spinning="assessmentLoading">
                <a-steps direction="vertical" size="small" :current="currentStepIndex" :items="stepItems" />
              </a-spin>
              <a-alert
                v-if="blockers.length"
                type="warning"
                show-icon
                :message="`暂不能移交入住：${blockers.join('、')}`"
              />
              <a-space wrap class="mk-contracts__actions">
                <a-button type="primary" @click="openLedger(selected)">继续签约办理</a-button>
                <a-tooltip :title="blockers.join('、')">
                  <a-button :disabled="blockers.length > 0" @click="openAdmission(selected)">移交入住</a-button>
                </a-tooltip>
              </a-space>
            </template>
          </section>

          <section class="rm-card">
            <div class="rm-card__head"><h3 class="rm-card__title">跨部门关键控制</h3></div>
            <a-alert type="warning" show-icon message="护理等级、医疗意见等由相应专业人员确认；市场人员只读。" />
            <a-alert class="mk-contracts__gap" type="success" show-icon message="押金、服务收款以财务实际到账和审批结果为准。" />
            <p class="rm-card__sub">正式入住须在长者管理中完成入住登记和床位核验，避免“签约 = 已入住”的错误状态。</p>
          </section>
        </div>
      </div>
    </div>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { AuditOutlined, CheckCircleOutlined, FileDoneOutlined, FileTextOutlined, LoginOutlined, PlusOutlined, WalletOutlined } from '@ant-design/icons-vue'
import PageContainer from '../../../components/PageContainer.vue'
import { getContractAssessmentOverview, getCrmContract, getContractPage, getContractStageSummary } from '../../../api/marketing'
import type { CrmContractItem, CrmContractStageSummary } from '../../../types'
import { contractTypeLabel } from '../../../utils/marketingEnums'
import { buildFlowSteps, contractStatusTag, handoverBlockers, handoverCounts, isSigned, type ContractContext } from './contractModel'
import '../../elder/residentUi.css'

const router = useRouter()
const route = useRoute()
const silent = { silent403: true }

const filter = ref<'all' | 'review' | 'sign'>('all')
const filterOptions = [{ label: '全部', value: 'all' }, { label: '待审核', value: 'review' }, { label: '待签约', value: 'sign' }]
const keyword = ref('')
const rows = ref<CrmContractItem[]>([])
const total = ref(0)
const pageNo = ref(1)
const loading = ref(false)

const stage = ref<CrmContractStageSummary | null>(null)
const pendingApproval = ref(0)
const signedList = ref<CrmContractItem[]>([])

const selected = ref<CrmContractItem | null>(null)
const assessed = ref(false)
const assessmentLoading = ref(false)

const columns = [
  { title: '合同编号', dataIndex: 'contractNo', key: 'contractNo' },
  { title: '长者', dataIndex: 'elderName', key: 'elderName' },
  { title: '办理状态', key: 'status' },
  { title: '合同类型', dataIndex: 'contractType', key: 'contractType', customRender: ({ text }: { text?: string }) => contractTypeLabel(text) },
  { title: '预计入住', dataIndex: 'expectedAdmissionDate', key: 'expectedAdmissionDate', customRender: ({ text }: { text?: string }) => text ? dayjs(text).format('MM-DD') : '-' },
  { title: '意向床位', dataIndex: 'reservationRoomNo', key: 'room', customRender: ({ text }: { text?: string }) => text || '-' },
  { title: '负责人', dataIndex: 'marketerName', key: 'marketerName', customRender: ({ text }: { text?: string }) => text || '-' },
  { title: '签约日期', dataIndex: 'contractSignedAt', key: 'signedAt', customRender: ({ text }: { text?: string }) => text ? dayjs(text).format('MM-DD') : '-' },
  { title: '操作', key: 'action', fixed: 'right' as const, width: 72 }
]

function contextOf(c: CrmContractItem): ContractContext {
  return {
    admitted: Boolean(c.admittedDate),
    admittedAt: c.admittedDate || undefined,
    // 合同分页返回财务只读结论 financeAuditStatus（APPROVED 才算已确认）
    finance: c.financeAuditStatus === 'APPROVED',
    assessed: String(c.id) === String(selected.value?.id) ? assessed.value : undefined
  }
}

const handover = computed(() => handoverCounts(signedList.value, contextOf))
const monthSigned = computed(() => signedList.value.filter((c) => c.contractSignedAt && dayjs(c.contractSignedAt).isSame(dayjs(), 'month')).length)
const inProgress = computed(() => stage.value ? Number(stage.value.pendingAssessment || 0) + Number(stage.value.pendingBedSelect || 0) + Number(stage.value.pendingSign || 0) : 0)
const pendingFinance = computed(() => signedList.value.filter((c) => { const ctx = contextOf(c); return !ctx.admitted && ctx.finance === false }).length)

const kpis = computed(() => [
  { label: '本月合同签约', value: monthSigned.value, hint: `累计已签 ${stage.value?.signed ?? signedList.value.length} 份`, icon: FileDoneOutlined },
  { label: '办理中合同', value: inProgress.value, hint: `待审核 ${pendingApproval.value} 份`, icon: AuditOutlined },
  { label: '待财务确认', value: pendingFinance.value, hint: '已签约、入住费用待财务审核（只读）', icon: WalletOutlined, tone: 'warning' },
  { label: '待移交入住', value: handover.value.waiting, hint: '签约后由长者管理办理正式入住', icon: LoginOutlined }
])

const steps = computed(() => selected.value ? buildFlowSteps(selected.value, contextOf(selected.value)) : [])
const currentStepIndex = computed(() => Math.max(0, steps.value.findIndex((s) => s.state === 'current')))
const stepItems = computed(() => steps.value.map((s) => ({
  title: s.title,
  status: s.state === 'done' ? 'finish' as const : s.state === 'current' ? 'process' as const : 'wait' as const,
  description: s.state === 'done' ? '已完成' : s.state === 'current' ? `当前步骤 · ${s.hint || ''}` : undefined
})))
const blockers = computed(() => selected.value ? handoverBlockers(selected.value, contextOf(selected.value)) : [])

function listParams() {
  return {
    pageNo: pageNo.value,
    pageSize: 8,
    keyword: keyword.value.trim() || undefined,
    status: filter.value === 'review' ? 'PENDING_APPROVAL' : undefined,
    flowStage: filter.value === 'sign' ? 'PENDING_SIGN' : undefined
  }
}

async function loadList() {
  loading.value = true
  try {
    const page = await getContractPage(listParams())
    rows.value = page.list || []
    total.value = page.total || 0
    if (!selected.value && rows.value.length) select(rows.value[0])
  } finally {
    loading.value = false
  }
}

function search() { pageNo.value = 1; loadList() }
function changePage(page: number) { pageNo.value = page; loadList() }

// shortcut: 移交看板只统计最近 200 份已签合同（移交状态由后端逐份返回），已签超过 200 份时改为后端汇总
async function loadBoard() {
  const [stageRes, reviewRes, signedRes] = await Promise.allSettled([
    getContractStageSummary(),
    getContractPage({ pageNo: 1, pageSize: 1, status: 'PENDING_APPROVAL' }, silent),
    getContractPage({ pageNo: 1, pageSize: 200, flowStage: 'SIGNED' }, silent)
  ])
  stage.value = stageRes.status === 'fulfilled' ? stageRes.value : null
  pendingApproval.value = reviewRes.status === 'fulfilled' ? Number(reviewRes.value.total || 0) : 0
  signedList.value = signedRes.status === 'fulfilled' ? signedRes.value.list || [] : []
}

async function select(record: CrmContractItem) {
  selected.value = record
  assessed.value = false
  if (isSigned(record) || (!record.leadId && !record.elderId)) return
  assessmentLoading.value = true
  try {
    const overview = await getContractAssessmentOverview({ leadId: record.leadId, elderId: record.elderId })
    const mine = overview?.contracts?.find((c) => String(c.contractId) === String(record.id))
    assessed.value = Boolean(mine?.reports?.length)
  } catch {
    assessed.value = false
  } finally {
    assessmentLoading.value = false
  }
}

function openLedger(record: CrmContractItem) {
  router.push({
    path: '/marketing/contracts/pending',
    // 待签标签按 route.flowStage 过滤（disableDefaultFlowStagePreset），带上阶段才能找到评估/选床中的合同
    query: { ...(isSigned(record) ? { view: 'MarketingContractsSigned' } : { flowStage: record.flowStage || undefined }), contractNo: record.contractNo || undefined, elderName: record.contractNo ? undefined : record.elderName }
  })
}

function openAdmission(record: CrmContractItem) {
  router.push({
    path: '/elder/admission-processing',
    query: { contractNo: record.contractNo || undefined, leadId: record.leadId ? String(record.leadId) : undefined, elderId: record.elderId ? String(record.elderId) : undefined, elderName: record.elderName || undefined }
  })
}

/** 工作台、客户 360 抽屉带 contractId / leadId 过来时直接定位到该合同 */
async function focusFromRoute() {
  const contractId = String(route.query.contractId || '')
  const leadId = String(route.query.leadId || '')
  const found = contractId
    ? rows.value.find((r) => String(r.id) === contractId) || await getCrmContract(contractId).catch(() => null)
    : leadId ? rows.value.find((r) => String(r.leadId) === leadId) || signedList.value.find((r) => String(r.leadId) === leadId) : null
  if (found) select(found)
}

onMounted(async () => { await Promise.all([loadList(), loadBoard()]); focusFromRoute() })
</script>

<style scoped>
.mk-contracts__grid { display: grid; grid-template-columns: minmax(0, 1.6fr) minmax(0, 1fr); gap: 16px; }
.mk-contracts__col { display: flex; flex-direction: column; gap: 16px; min-width: 0; }
.mk-contracts__search { max-width: 280px; margin-bottom: 12px; }
.mk-contracts__counts { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 12px; margin-bottom: 14px; }
.mk-contracts__counts > div { display: flex; flex-direction: column; gap: 4px; padding: 12px 14px; border: 1px solid var(--rm-border); border-radius: 10px; }
.mk-contracts__counts span { color: var(--rm-muted); font-size: 12px; }
.mk-contracts__counts strong { font-size: 22px; color: var(--rm-green-dark); }
.mk-contracts__counts strong.is-warning { color: var(--rm-warning); }
.mk-contracts__counts small { margin-left: 2px; font-size: 12px; font-weight: 400; color: var(--rm-muted); }
.mk-contracts__checks { margin: 0; padding: 0; list-style: none; display: grid; gap: 8px; color: var(--rm-text); }
.mk-contracts__checks :deep(.anticon) { color: var(--rm-green); margin-right: 6px; }
.mk-contracts__actions { margin-top: 12px; }
.mk-contracts__gap { margin: 10px 0; }
.mk-contracts :deep(.mk-row--active > td) { background: var(--rm-green-soft) !important; }
.mk-contracts :deep(.ant-table-row) { cursor: pointer; }
@media (max-width: 1100px) {
  .mk-contracts__grid { grid-template-columns: minmax(0, 1fr); }
}
@media (max-width: 560px) {
  .mk-contracts__counts { grid-template-columns: minmax(0, 1fr); }
  .mk-contracts__search { max-width: none; }
}
</style>
