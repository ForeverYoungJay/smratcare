<template>
  <PageContainer class="rm-page" title="人事工作台" :sub-title="`${today} · 招聘入职、合同证书、考勤异常与报销待办`">
    <template v-if="managementView" #extra>
      <a-space wrap>
        <a-button @click="go('/hr/recruitment/needs')">新建招聘</a-button>
        <a-button type="primary" @click="go('/hr/profile/onboarding')">办理入职</a-button>
      </a-space>
    </template>

    <template v-if="managementView">
      <div class="rm-kpis">
        <button v-for="card in kpis" :key="card.key" type="button" class="rm-kpi" @click="go(card.path)">
          <span class="rm-kpi__label">{{ card.label }}</span>
          <span class="rm-kpi__value">{{ loaded ? card.value : '--' }}<small>{{ card.unit }}</small></span>
          <span class="rm-kpi__hint">{{ card.hint }}</span>
        </button>
      </div>

      <div class="rm-split hw-grid">
        <section class="rm-card">
          <div class="rm-card__head">
            <span class="rm-card__title">合同到期提醒 <small class="rm-card__sub">{{ HR_WARNING_DAYS }} 天内</small></span>
            <a class="rm-link" @click="go('/hr/profile/contract-reminders')">查看全部 →</a>
          </div>
          <div v-if="!contracts.length" class="hw-empty">{{ loaded ? '近期没有到期合同' : '加载中' }}</div>
          <div v-for="item in contracts" :key="String(item.contractId || item.staffId)" class="rm-row">
            <div class="rm-row__main">
              <span class="rm-row__title">{{ item.staffName }} <small>{{ item.jobTitle || '' }}</small></span>
              <span class="rm-row__meta">{{ item.contractType || '劳动合同' }} · 到期 {{ item.contractEndDate || '-' }}</span>
            </div>
            <span class="rm-pill" :class="Number(item.remainingDays) < 0 ? 'rm-pill--danger' : 'rm-pill--warning'">{{ remainingText(item.remainingDays) }}</span>
          </div>
        </section>

        <section class="rm-card">
          <div class="rm-card__head"><span class="rm-card__title">重点提醒 <small class="rm-card__sub">及时闭环</small></span></div>
          <div v-if="!alerts.length" class="hw-empty">{{ loaded ? '暂无需要提醒的事项' : '加载中' }}</div>
          <button v-for="item in alerts" :key="item.label" type="button" class="rm-row hw-alert" @click="go(item.path)">
            <span class="hw-dot" :class="`hw-dot--${item.tone}`" />
            <div class="rm-row__main"><span class="rm-row__title">{{ item.label }}</span><span class="rm-row__meta">{{ item.hint }}</span></div>
            <span class="rm-pill" :class="item.tone === 'danger' ? 'rm-pill--danger' : 'rm-pill--warning'">{{ item.count }}</span>
          </button>
        </section>
      </div>

      <div class="rm-split hw-grid">
        <section class="rm-card">
          <div class="rm-card__head">
            <span class="rm-card__title">证书到期提醒 <small class="rm-card__sub">含已过期</small></span>
            <a class="rm-link" @click="go('/hr/development/certificate-reminders')">查看全部 →</a>
          </div>
          <div v-if="!certificates.length" class="hw-empty">{{ loaded ? '近期没有到期证书' : '加载中' }}</div>
          <div v-for="item in certificates" :key="String(item.id)" class="rm-row">
            <div class="rm-row__main">
              <span class="rm-row__title">{{ item.staffName }} · {{ item.certificateName }}</span>
              <span class="rm-row__meta">到期 {{ item.expiryDate || '-' }}</span>
            </div>
            <span class="rm-pill" :class="Number(item.remainingDays) < 0 ? 'rm-pill--danger' : 'rm-pill--warning'">{{ remainingText(item.remainingDays) }}</span>
          </div>
        </section>
        <section class="rm-card">
          <div class="rm-card__head"><span class="rm-card__title">快捷办理</span></div>
          <div class="hw-quick">
            <button v-for="item in QUICK" :key="item.label" type="button" @click="go(item.path)"><b>{{ item.label }}</b><span>{{ item.hint }}</span></button>
          </div>
        </section>
      </div>
    </template>

    <section v-else class="rm-card">
      <div class="rm-card__head"><span class="rm-card__title">我的人事服务</span></div>
      <div class="hw-quick">
        <button v-for="item in SELF" :key="item.label" type="button" @click="go(item.path)"><b>{{ item.label }}</b><span>{{ item.hint }}</span></button>
      </div>
    </section>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import dayjs from 'dayjs'
import { useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import {
  getHrContractReminderPage,
  getHrExpenseApprovalFlowPage,
  getHrProfileCertificateReminderPage,
  getHrRecruitmentNeedPage,
  getHrWorkbenchSummary
} from '../../api/hr'
import type { HrContractReminderItem, HrStaffCertificateItem, HrWorkbenchSummary } from '../../types'
import { useLiveSyncRefresh } from '../../composables/useLiveSyncRefresh'
import { useUserStore } from '../../stores/user'
import { HR_WARNING_DAYS, hrAlerts, hrKpis } from './hrWorkbench'
import '../elder/residentUi.css'

const QUICK = [
  { label: '办理入职', hint: '建档、合同、账号、必修培训', path: '/hr/profile/onboarding' },
  { label: '招聘办理', hint: '需求、候选人、面试、录用', path: '/hr/recruitment/needs' },
  { label: '排班日历', hint: '引用护理/医务实际排班', path: '/hr/attendance/calendar' },
  { label: '请假审批', hint: '请假、调班、加班', path: '/hr/attendance/leave-approval' },
  { label: '培训记录', hint: '培训计划、签到、成绩', path: '/hr/development/records' },
  { label: '员工宿舍', hint: '床位分配、入住与退宿', path: '/hr/expense/dormitory' }
]
const SELF = [
  { label: '我的考勤', hint: '核对考勤、请假与补卡', path: '/workbench/attendance' },
  { label: '通知公告', hint: '了解最新工作安排', path: '/oa/notice' },
  { label: '制度与文档', hint: '查询业务规范', path: '/oa/document' }
]

const router = useRouter()
const user = useUserStore()
const today = dayjs().format('YYYY年M月D日')
const managementView = computed(() => (user.roles || []).some((role) => ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'].includes(String(role).toUpperCase())))
const loaded = ref(false)
const summary = ref<HrWorkbenchSummary | null>(null)
const recruiting = ref(0)
const expensePending = ref(0)
const certificateTotal = ref(0)
const contracts = ref<HrContractReminderItem[]>([])
const certificates = ref<HrStaffCertificateItem[]>([])

const n = (value: unknown) => Number(value ?? 0)
const counts = computed(() => ({
  onJob: n(summary.value?.onJobCount),
  recruiting: recruiting.value,
  contractsExpiring: n(summary.value?.contractExpiringCount),
  certificatesExpiring: certificateTotal.value,
  attendanceAbnormal: n(summary.value?.attendanceAbnormalCount),
  leavePending: n(summary.value?.pendingLeaveApprovalCount),
  expensePending: expensePending.value
}))
const kpis = computed(() => hrKpis(counts.value))
const alerts = computed(() => hrAlerts(counts.value))

function remainingText(days?: number) {
  const value = Number(days)
  if (!Number.isFinite(value)) return '待核对'
  return value < 0 ? `已过期 ${-value} 天` : `剩 ${value} 天`
}
function go(path: string) { router.push(path) }

async function load() {
  if (!managementView.value) return
  const quiet = { silent403: true }
  const window = { pageNo: 1, pageSize: 5, warningDays: HR_WARNING_DAYS }
  const [s, r1, r2, e, c, cert] = await Promise.allSettled([
    getHrWorkbenchSummary({ warningDays: HR_WARNING_DAYS }, quiet),
    getHrRecruitmentNeedPage({ pageNo: 1, pageSize: 1, status: 'PENDING' }),
    getHrRecruitmentNeedPage({ pageNo: 1, pageSize: 1, status: 'IN_PROGRESS' }),
    getHrExpenseApprovalFlowPage({ pageNo: 1, pageSize: 1, status: 'PENDING' }),
    getHrContractReminderPage({ ...window, includeExpired: true }),
    getHrProfileCertificateReminderPage({ ...window, includeExpired: true }, quiet)
  ])
  const total = (r: PromiseSettledResult<{ total?: number }>) => (r.status === 'fulfilled' ? n(r.value.total) : 0)
  summary.value = s.status === 'fulfilled' ? s.value : null
  recruiting.value = total(r1) + total(r2)
  expensePending.value = total(e)
  contracts.value = c.status === 'fulfilled' ? c.value.list || [] : []
  certificates.value = cert.status === 'fulfilled' ? cert.value.list || [] : []
  certificateTotal.value = total(cert)
  loaded.value = true
}

onMounted(load)
useLiveSyncRefresh({ topics: ['hr', 'oa', 'system'], refresh: load, debounceMs: 900 })
</script>

<style scoped>
.hw-grid { grid-template-columns: minmax(0, 1.5fr) minmax(300px, 1fr); margin-top: 16px; }
.hw-empty { padding: 24px 8px; color: #7b877e; text-align: center; }
.hw-alert { width: 100%; border: 0; background: none; text-align: left; cursor: pointer; }
.hw-dot { flex: 0 0 8px; height: 8px; border-radius: 50%; }
.hw-dot--warning { background: #d9a441; }
.hw-dot--danger { background: #d9534f; }
.hw-quick { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 10px; }
.hw-quick button { display: grid; gap: 4px; padding: 12px; border: 1px solid #e3ebe6; border-radius: 10px; background: #fff; text-align: left; cursor: pointer; }
.hw-quick button b { color: #1f6f55; }
.hw-quick button span { color: #6b7a74; font-size: 12px; }
.hw-quick button:hover { border-color: #1f9d74; background: #f3faf6; }
@media (max-width: 1100px) { .hw-grid { grid-template-columns: 1fr; } }
</style>
