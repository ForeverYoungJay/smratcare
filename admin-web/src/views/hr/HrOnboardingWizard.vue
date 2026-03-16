<template>
  <PageContainer title="入职向导" subTitle="员工档案中心 / 统一入职建档、账号、合同、附件流程">
    <section class="wizard-hero">
      <div class="hero-copy">
        <p class="eyebrow">HR ONBOARDING FLOW</p>
        <h1>把新增人员、建档、签合同、上传附件收成一条清晰的路径。</h1>
        <p class="hero-text">
          我们把分散页面重新编排成 4 个步骤。先建立员工主档，再继续补齐账号与监管、合同、附件；每一步都能直接跳过去并自动打开对应操作。
        </p>
        <div class="hero-actions">
          <a-button type="primary" size="large" @click="startCreate">开始新增人员</a-button>
          <a-button size="large" @click="goToBasic">进入档案中心</a-button>
        </div>
      </div>
      <div class="hero-panel">
        <div class="status-ribbon">
          <span>当前对象</span>
          <strong>{{ selectedStaffLabel }}</strong>
        </div>
        <div class="progress-shell">
          <div class="progress-meta">
            <span>完成进度</span>
            <strong>{{ progressPercent }}%</strong>
          </div>
          <a-progress :percent="progressPercent" :show-info="false" stroke-color="url(#wizardGradient)" />
          <svg width="0" height="0">
            <defs>
              <linearGradient id="wizardGradient" x1="0%" y1="0%" x2="100%" y2="0%">
                <stop offset="0%" stop-color="#d97706" />
                <stop offset="100%" stop-color="#0f766e" />
              </linearGradient>
            </defs>
          </svg>
        </div>
        <div class="metric-grid">
          <div class="metric-card">
            <span class="metric-label">角色 / 监管</span>
            <strong>{{ accountReady ? '已配置' : '待补齐' }}</strong>
          </div>
          <div class="metric-card">
            <span class="metric-label">合同</span>
            <strong>{{ contractReady ? profile.contractNo || '已生成' : '待维护' }}</strong>
          </div>
          <div class="metric-card">
            <span class="metric-label">合同附件</span>
            <strong>{{ contractAttachmentCount }} 份</strong>
          </div>
          <div class="metric-card">
            <span class="metric-label">其他附件</span>
            <strong>{{ profileAttachmentCount }} 份</strong>
          </div>
        </div>
      </div>
    </section>

    <a-row :gutter="[16, 16]" class="wizard-main">
      <a-col :xs="24" :xl="16">
        <div class="step-board">
          <div
            v-for="(step, index) in steps"
            :key="step.key"
            class="step-card"
            :class="[`is-${step.state}`]"
          >
            <div class="step-index">{{ String(index + 1).padStart(2, '0') }}</div>
            <div class="step-content">
              <div class="step-head">
                <div>
                  <div class="step-title">{{ step.title }}</div>
                  <div class="step-subtitle">{{ step.subtitle }}</div>
                </div>
                <a-tag :color="stepTagColor(step.state)">{{ stepTagText(step.state) }}</a-tag>
              </div>
              <p class="step-description">{{ step.description }}</p>
              <div class="step-notes">
                <span v-for="note in step.notes" :key="note">{{ note }}</span>
              </div>
              <div class="step-actions">
                <a-button type="primary" :disabled="step.disabled" @click="step.action()">{{ step.cta }}</a-button>
                <a-button v-if="step.secondary" :disabled="step.disabled" @click="step.secondary.action()">
                  {{ step.secondary.label }}
                </a-button>
              </div>
            </div>
          </div>
        </div>
      </a-col>

      <a-col :xs="24" :xl="8">
        <div class="side-stack">
          <a-card :bordered="false" class="lookup-card">
            <div class="lookup-title">继续已有入职流程</div>
            <div class="lookup-text">搜索员工后，向导会自动刷新状态，并把每一步定位到这个人。</div>
            <a-select
              v-model:value="selectedStaffId"
              allow-clear
              show-search
              :filter-option="false"
              :options="staffOptions"
              :loading="staffLoading"
              placeholder="姓名 / 工号 / 手机号"
              style="width: 100%; margin-top: 14px"
              @search="searchStaff"
              @focus="() => !staffOptions.length && searchStaff('')"
              @change="onSelectStaff"
            />
            <div class="lookup-hint">未选择员工时，仅可启动“新增人员”第一步。</div>
          </a-card>

          <a-card :bordered="false" class="summary-card">
            <div class="summary-title">本次向导会覆盖的内容</div>
            <ul class="summary-list">
              <li>基础资料：姓名、工号、部门、岗位角色、入职日期、紧急联系人。</li>
              <li>账号配置：登录账号、初始密码、上下级监管链、角色分配。</li>
              <li>合同资料：合同类型、状态、起止日期、自动生成合同编号。</li>
              <li>附件归档：合同附件与其他附件上传入口。</li>
            </ul>
          </a-card>

          <a-card :bordered="false" class="summary-card summary-card-accent">
            <div class="summary-title">建议操作顺序</div>
            <div class="sequence-rail">
              <span>建员工主档</span>
              <span>补账号监管</span>
              <span>维护合同</span>
              <span>上传附件</span>
            </div>
            <div class="summary-text">
              当前数据模型要求先创建员工账号主档，档案与合同都挂在 `staffId` 下，所以向导会默认按这条顺序推进。
            </div>
          </a-card>
        </div>
      </a-col>
    </a-row>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import { useStaffOptions } from '../../composables/useStaffOptions'
import { getHrContractAttachmentPage, getHrProfile, getHrProfileAttachmentPage } from '../../api/hr'
import { getStaff } from '../../api/staff'
import { getStaffRoleAssignments } from '../../api/rbac'
import type { HrStaffProfile, StaffItem, Id } from '../../types'

type StepState = 'done' | 'current' | 'pending'
type StepItem = {
  key: string
  title: string
  subtitle: string
  description: string
  notes: string[]
  cta: string
  disabled: boolean
  state: StepState
  action: () => void
  secondary?: {
    label: string
    action: () => void | Promise<void>
  }
}

const route = useRoute()
const router = useRouter()
const { staffOptions, staffLoading, searchStaff } = useStaffOptions({ pageSize: 120, preloadSize: 200 })

const selectedStaffId = ref<string | undefined>(undefined)
const profile = reactive<Partial<HrStaffProfile>>({})
const account = reactive<Partial<StaffItem>>({})
const roleCount = ref(0)
const contractAttachmentCount = ref(0)
const profileAttachmentCount = ref(0)

const accountReady = computed(() => roleCount.value > 0 || Boolean(account.directLeaderId || account.indirectLeaderId))
const contractReady = computed(() => Boolean(profile.contractNo || profile.contractType || profile.contractStartDate || profile.contractEndDate))
const attachmentReady = computed(() => contractAttachmentCount.value > 0 || profileAttachmentCount.value > 0)
const selectedStaffLabel = computed(() => {
  if (!selectedStaffId.value) return '未选择员工'
  return `${profile.realName || account.realName || '未命名员工'}${profile.staffNo || account.staffNo ? ` / ${profile.staffNo || account.staffNo}` : ''}`
})

const progressPercent = computed(() => {
  const flags = [
    Boolean(selectedStaffId.value),
    accountReady.value,
    contractReady.value,
    attachmentReady.value
  ]
  const completed = flags.filter(Boolean).length
  return Math.round((completed / flags.length) * 100)
})

const steps = computed<StepItem[]>(() => {
  const hasStaff = Boolean(selectedStaffId.value)
  const currentKey = !hasStaff ? 'basic' : !accountReady.value ? 'account' : !contractReady.value ? 'contract' : !attachmentReady.value ? 'attachment' : 'done'
  return [
    {
      key: 'basic',
      title: '建立员工主档',
      subtitle: '新增人员 / 录入基础资料 / 生成 staffId',
      description: '从这里开始创建员工账号主档和 HR 档案基础资料。岗位角色、入职信息、合同基础字段都可以一次录入。',
      notes: ['账号、密码、工号、姓名', '部门、岗位角色、入职日期', '紧急联系人、证书、备注'],
      cta: hasStaff ? '编辑基础资料' : '开始新增人员',
      disabled: false,
      state: !hasStaff ? 'current' : 'done',
      action: () => {
        if (hasStaff) {
          router.push({ path: '/hr/profile/basic', query: { staffId: selectedStaffId.value, autoOpen: '1', wizard: '1' } })
          return
        }
        startCreate()
      },
      secondary: hasStaff
        ? { label: '切换员工', action: () => searchStaff('') }
        : undefined
    },
    {
      key: 'account',
      title: '账号与监管配置',
      subtitle: '登录账号 / 角色 / 直接领导 / 间接领导',
      description: '补齐系统账号、角色分配和监管链路。后续审批、OA、统计、组织联动都会依赖这里。',
      notes: ['分配角色', '设置直接领导', '设置间接领导'],
      cta: '去设置账号',
      disabled: !hasStaff,
      state: hasStaff && currentKey === 'account' ? 'current' : accountReady.value ? 'done' : 'pending',
      action: () => goStep('/hr/profile/account-access')
    },
    {
      key: 'contract',
      title: '维护劳动合同',
      subtitle: '合同类型 / 状态 / 起止日期 / 合同编号',
      description: '合同编号由后端自动生成，HR 只需要维护合同要素并上传合同附件。',
      notes: ['自动生成合同编号', '支持合同附件上传', '支持后续打印'],
      cta: '去维护合同',
      disabled: !hasStaff,
      state: hasStaff && currentKey === 'contract' ? 'current' : contractReady.value ? 'done' : 'pending',
      action: () => goStep('/hr/profile/contracts')
    },
    {
      key: 'attachment',
      title: '归档附件材料',
      subtitle: '其他附件 / 补充证明 / 归档资料',
      description: '上传身份证明、入职材料、补充协议等。这里同时承接非合同类的通用档案附件。',
      notes: ['上传其他附件', '自动回填文件链接', '支持继续补传'],
      cta: '去上传附件',
      disabled: !hasStaff,
      state: hasStaff && currentKey === 'attachment' ? 'current' : attachmentReady.value ? 'done' : 'pending',
      action: () => goStep('/hr/profile/attachments'),
      secondary: hasStaff
        ? { label: '合同打印', action: () => router.push({ path: '/hr/profile/contract-print', query: { staffId: selectedStaffId.value } }) }
        : undefined
    }
  ]
})

function stepTagText(state: StepState) {
  if (state === 'done') return '已完成'
  if (state === 'current') return '当前步骤'
  return '待处理'
}

function stepTagColor(state: StepState) {
  if (state === 'done') return 'green'
  if (state === 'current') return 'processing'
  return 'default'
}

function startCreate() {
  router.push({ path: '/hr/profile/basic', query: { autoCreate: '1', wizard: '1' } })
}

function goToBasic() {
  router.push('/hr/profile/basic')
}

function goStep(path: string) {
  if (!selectedStaffId.value) return
  router.push({
    path,
    query: {
      staffId: selectedStaffId.value,
      autoOpen: '1',
      wizard: '1'
    }
  })
}

function onSelectStaff(value?: Id) {
  if (!value) {
    selectedStaffId.value = undefined
    resetDetail()
    syncRoute()
    return
  }
  selectedStaffId.value = String(value)
  syncRoute()
}

function syncRoute() {
  router.replace({
    query: {
      ...route.query,
      staffId: selectedStaffId.value || undefined
    }
  })
}

function resetDetail() {
  Object.keys(profile).forEach((key) => {
    ;(profile as any)[key] = undefined
  })
  Object.keys(account).forEach((key) => {
    ;(account as any)[key] = undefined
  })
  roleCount.value = 0
  contractAttachmentCount.value = 0
  profileAttachmentCount.value = 0
}

async function loadDetail(staffId?: string) {
  resetDetail()
  if (!staffId) return
  const [profileRes, accountRes, rolesRes, contractAttachmentRes, profileAttachmentRes] = await Promise.all([
    getHrProfile(staffId).catch(() => null),
    getStaff(staffId).catch(() => null),
    getStaffRoleAssignments(staffId).catch(() => []),
    getHrContractAttachmentPage({ pageNo: 1, pageSize: 1, keyword: `staffId:${staffId}` }).catch(() => null),
    getHrProfileAttachmentPage({ pageNo: 1, pageSize: 1, keyword: `staffId:${staffId}` }).catch(() => null)
  ])
  Object.assign(profile, profileRes || {})
  Object.assign(account, accountRes || {})
  roleCount.value = Array.isArray(rolesRes) ? rolesRes.length : 0
  contractAttachmentCount.value = Number(contractAttachmentRes?.total || 0)
  profileAttachmentCount.value = Number(profileAttachmentRes?.total || 0)
}

watch(
  () => route.query.staffId,
  (value) => {
    selectedStaffId.value = value ? String(value) : undefined
    loadDetail(selectedStaffId.value).catch(() => {})
  },
  { immediate: true }
)

onMounted(() => {
  searchStaff('').catch(() => {})
})
</script>

<style scoped>
.wizard-hero {
  --wizard-bg: #f4ede2;
  --wizard-ink: #1f2937;
  --wizard-soft: rgba(71, 85, 105, 0.72);
  --wizard-accent: #b45309;
  --wizard-accent-strong: #0f766e;
  position: relative;
  overflow: hidden;
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(320px, 0.8fr);
  gap: 20px;
  padding: 24px;
  border-radius: 24px;
  background:
    radial-gradient(circle at top right, rgba(15, 118, 110, 0.14), transparent 28%),
    radial-gradient(circle at 20% 10%, rgba(217, 119, 6, 0.16), transparent 26%),
    linear-gradient(135deg, rgba(255, 251, 235, 0.98), rgba(240, 249, 255, 0.96));
  border: 1px solid rgba(180, 83, 9, 0.12);
  box-shadow: 0 18px 50px rgba(15, 23, 42, 0.08);
}

.wizard-hero::after {
  content: '';
  position: absolute;
  inset: auto -40px -60px auto;
  width: 220px;
  height: 220px;
  background: linear-gradient(135deg, rgba(180, 83, 9, 0.14), rgba(15, 118, 110, 0.06));
  border-radius: 40px;
  transform: rotate(18deg);
}

.hero-copy,
.hero-panel {
  position: relative;
  z-index: 1;
}

.eyebrow {
  margin: 0 0 8px;
  color: var(--wizard-accent);
  font-size: 12px;
  letter-spacing: 0.26em;
  font-weight: 700;
}

.hero-copy h1 {
  margin: 0;
  color: var(--wizard-ink);
  font-family: Georgia, 'Times New Roman', serif;
  font-size: clamp(28px, 4vw, 42px);
  line-height: 1.08;
}

.hero-text {
  max-width: 680px;
  margin: 16px 0 0;
  color: var(--wizard-soft);
  font-size: 15px;
  line-height: 1.8;
}

.hero-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 22px;
}

.hero-panel {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.status-ribbon {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  padding: 14px 16px;
  border-radius: 18px;
  color: #fff;
  background: linear-gradient(135deg, #1f2937, #0f766e);
}

.status-ribbon span {
  font-size: 12px;
  opacity: 0.84;
}

.status-ribbon strong {
  font-size: 14px;
}

.progress-shell,
.metric-grid,
.lookup-card,
.summary-card {
  border-radius: 18px;
  border: 1px solid rgba(148, 163, 184, 0.16);
  background: rgba(255, 255, 255, 0.78);
  backdrop-filter: blur(10px);
  box-shadow: 0 14px 28px rgba(15, 23, 42, 0.05);
}

.progress-shell {
  padding: 16px;
}

.progress-meta {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  color: var(--wizard-soft);
  font-size: 13px;
}

.progress-meta strong {
  color: var(--wizard-ink);
  font-size: 22px;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  padding: 16px;
}

.metric-card {
  padding: 12px;
  border-radius: 14px;
  background: linear-gradient(135deg, rgba(248, 250, 252, 0.96), rgba(255, 255, 255, 0.92));
}

.metric-label {
  display: block;
  color: var(--wizard-soft);
  font-size: 12px;
  margin-bottom: 4px;
}

.metric-card strong {
  color: var(--wizard-ink);
  font-size: 16px;
}

.wizard-main {
  margin-top: 18px;
}

.step-board,
.side-stack {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.step-card {
  display: grid;
  grid-template-columns: 74px minmax(0, 1fr);
  gap: 16px;
  padding: 18px;
  border-radius: 22px;
  border: 1px solid rgba(148, 163, 184, 0.18);
  background: #fff;
  transition: transform 0.22s ease, box-shadow 0.22s ease, border-color 0.22s ease;
}

.step-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 18px 32px rgba(15, 23, 42, 0.08);
}

.step-card.is-current {
  border-color: rgba(13, 148, 136, 0.3);
  background: linear-gradient(135deg, rgba(240, 253, 250, 0.95), rgba(255, 255, 255, 0.98));
}

.step-card.is-done {
  border-color: rgba(34, 197, 94, 0.24);
  background: linear-gradient(135deg, rgba(240, 253, 244, 0.96), rgba(255, 255, 255, 0.98));
}

.step-index {
  width: 74px;
  height: 74px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 24px;
  font-weight: 700;
  background: linear-gradient(135deg, #d97706, #0f766e);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.32);
}

.step-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}

.step-title {
  color: #111827;
  font-size: 20px;
  font-weight: 700;
}

.step-subtitle {
  margin-top: 4px;
  color: #64748b;
  font-size: 12px;
}

.step-description {
  margin: 12px 0 0;
  color: #475569;
  line-height: 1.8;
}

.step-notes {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 12px;
}

.step-notes span {
  padding: 6px 10px;
  border-radius: 999px;
  color: #475569;
  background: rgba(241, 245, 249, 0.9);
  font-size: 12px;
}

.step-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 16px;
}

.lookup-card,
.summary-card {
  padding: 18px;
}

.lookup-title,
.summary-title {
  color: #111827;
  font-size: 16px;
  font-weight: 700;
}

.lookup-text,
.summary-text,
.lookup-hint {
  color: #64748b;
  line-height: 1.7;
}

.lookup-text {
  margin-top: 8px;
}

.lookup-hint {
  margin-top: 10px;
  font-size: 12px;
}

.summary-list {
  margin: 12px 0 0;
  padding-left: 18px;
  color: #475569;
  line-height: 1.9;
}

.summary-card-accent {
  background: linear-gradient(135deg, rgba(30, 41, 59, 0.98), rgba(15, 118, 110, 0.96));
  border-color: rgba(15, 118, 110, 0.28);
}

.summary-card-accent .summary-title,
.summary-card-accent .summary-text,
.summary-card-accent .sequence-rail span {
  color: #ecfeff;
}

.sequence-rail {
  display: grid;
  gap: 10px;
  margin-top: 14px;
}

.sequence-rail span {
  position: relative;
  padding-left: 18px;
  font-size: 13px;
}

.sequence-rail span::before {
  content: '';
  position: absolute;
  left: 0;
  top: 7px;
  width: 8px;
  height: 8px;
  border-radius: 999px;
  background: #f59e0b;
}

.summary-text {
  margin-top: 14px;
}

@media (max-width: 960px) {
  .wizard-hero {
    grid-template-columns: 1fr;
  }

  .step-card {
    grid-template-columns: 1fr;
  }

  .step-index {
    width: 58px;
    height: 58px;
    font-size: 20px;
  }
}
</style>
