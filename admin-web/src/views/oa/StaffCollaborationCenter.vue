<template>
  <PageContainer
    title="员工协同中心"
    subTitle="把员工小程序提交的请假调班、物资申领、待办、日报和建议集中到后台，方便主管持续流转处理"
  >
    <template #extra>
      <a-space wrap>
        <a-button :loading="loading" @click="loadData">刷新</a-button>
        <a-button @click="openPath('/stats/staff-mobile')">员工移动端中心</a-button>
        <a-button @click="openPath('/stats/staff-mobile-ledger')">现场台账</a-button>
        <a-button type="primary" @click="openPath('/oa/approval')">审批中心</a-button>
      </a-space>
    </template>

    <section class="summary-grid">
      <button class="summary-card" type="button" @click="openPath('/oa/approval')">
        <span>待审批流程</span>
        <strong>{{ approvalSummary.pendingCount || 0 }}</strong>
        <small>超时 {{ approvalSummary.timeoutPendingCount || 0 }} · 请假待审 {{ approvalSummary.leavePendingCount || 0 }}</small>
      </button>
      <button class="summary-card" type="button" @click="openRoute('/oa/approval', { type: 'MATERIAL_APPLY' })">
        <span>物资申领</span>
        <strong>{{ materialApplyPendingCount }}</strong>
        <small>员工移动端提交的领用/补给申请</small>
      </button>
      <button class="summary-card" type="button" @click="openPath('/oa/todo')">
        <span>员工待办</span>
        <strong>{{ todoSummary.openCount || 0 }}</strong>
        <small>今日到期 {{ todoSummary.dueTodayCount || 0 }} · 逾期 {{ todoSummary.overdueCount || 0 }}</small>
      </button>
      <button class="summary-card" type="button" @click="openPath('/oa/work-report')">
        <span>工作日报</span>
        <strong>{{ reportSummary.missingSummaryCount || 0 }}</strong>
        <small>今日已提交 {{ reportSummary.todaySubmittedCount || 0 }} · 草稿 {{ reportSummary.draftCount || 0 }}</small>
      </button>
      <button class="summary-card" type="button" @click="scrollToSection('suggestions')">
        <span>员工建议</span>
        <strong>{{ employeeSuggestionPendingCount }}</strong>
        <small>处理中 {{ employeeSuggestionProcessingCount }} · 最近提交 {{ employeeSuggestions.length }}</small>
      </button>
      <button class="summary-card" type="button" @click="openRoute('/health/management/data', { source: 'staff-mobile', date: 'today' })">
        <span>体征补录</span>
        <strong>{{ healthSummary.totalCount || 0 }}</strong>
        <small>今日异常 {{ healthSummary.abnormalCount || 0 }} · 最新 {{ healthLatestMeasuredText }}</small>
      </button>
    </section>

    <a-alert
      class="center-alert"
      type="info"
      show-icon
      message="员工端提交后，后台至少要能看到当前状态、提交人、关键说明和下一步处理入口。"
      description="本页优先收口协同流转，不替代原有审批、待办、总结和考勤页；点击卡片后会进入对应业务页继续处理。"
    />

    <section class="panel-grid">
      <a-card class="card-elevated" :bordered="false" title="审批联动预览">
        <template #extra>
          <a-space wrap>
            <a-button size="small" @click="openRoute('/oa/approval', { type: 'LEAVE' })">请假</a-button>
            <a-button size="small" @click="openRoute('/oa/approval', { type: 'SHIFT_CHANGE' })">调班</a-button>
            <a-button size="small" type="primary" @click="openRoute('/oa/approval', { type: 'MATERIAL_APPLY' })">物资申领</a-button>
          </a-space>
        </template>
        <div v-if="approvalPreview.length" class="preview-list">
          <button
            v-for="item in approvalPreview"
            :key="String(item.id)"
            type="button"
            class="preview-item"
            @click="openRoute('/oa/approval', { keyword: item.title || item.applicantName || '' })"
          >
            <div>
              <strong>{{ item.title || `${approvalTypeText(item.approvalType)}审批` }}</strong>
              <p>{{ approvalTypeText(item.approvalType) }} · {{ item.applicantName || '员工' }} · {{ dateText(item.startTime || item.endTime) }}</p>
            </div>
            <a-tag :color="approvalStatusColor(item.status)">{{ approvalStatusText(item.status) }}</a-tag>
          </button>
        </div>
        <a-empty v-else description="当前没有员工协同审批记录" />
      </a-card>

      <a-card class="card-elevated" :bordered="false" title="待办与日报">
        <template #extra>
          <a-space wrap>
            <a-button size="small" @click="openPath('/oa/todo')">待办中心</a-button>
            <a-button size="small" @click="openPath('/oa/work-report')">工作总结</a-button>
          </a-space>
        </template>
        <div class="dual-block">
          <div class="mini-block">
            <div class="mini-title">最近待办</div>
            <div v-if="todoPreview.length" class="preview-list compact">
              <button
                v-for="item in todoPreview"
                :key="String(item.id)"
                type="button"
                class="preview-item"
                @click="openRoute('/oa/todo', { keyword: item.title || '' })"
              >
                <div>
                  <strong>{{ item.title || '待办事项' }}</strong>
                  <p>{{ item.assigneeName || '员工' }} · 截止 {{ dateText(item.dueTime) || '未设置' }}</p>
                </div>
                <a-tag :color="todoStatusColor(item.status)">{{ todoStatusText(item.status) }}</a-tag>
              </button>
            </div>
            <a-empty v-else description="暂无待办预览" />
          </div>

          <div class="mini-block">
            <div class="mini-title">最近日报</div>
            <div v-if="reportPreview.length" class="preview-list compact">
              <button
                v-for="item in reportPreview"
                :key="String(item.id)"
                type="button"
                class="preview-item"
                @click="openRoute('/oa/work-report', { keyword: item.title || item.reporterName || '' })"
              >
                <div>
                  <strong>{{ item.title || '工作日报' }}</strong>
                  <p>{{ item.reporterName || '员工' }} · {{ item.reportDate || '-' }}</p>
                </div>
                <a-tag :color="reportStatusColor(item.status)">{{ reportStatusText(item.status) }}</a-tag>
              </button>
            </div>
            <a-empty v-else description="暂无日报预览" />
          </div>
        </div>
      </a-card>

      <a-card class="card-elevated" :bordered="false" title="体征补录与异常处理">
        <template #extra>
          <a-space wrap>
            <a-button size="small" @click="openRoute('/health/management/data', { source: 'staff-mobile' })">员工补录</a-button>
            <a-button size="small" type="primary" @click="openRoute('/health/management/data', { source: 'staff-mobile', abnormalFlag: '1', date: 'today' })">今日异常</a-button>
          </a-space>
        </template>
        <div class="dual-block">
          <div class="mini-block">
            <div class="mini-title">员工端体征同步</div>
            <div class="health-overview">
              <div class="health-metric">
                <strong>{{ healthSummary.totalCount || 0 }}</strong>
                <span>今日补录记录</span>
              </div>
              <div class="health-metric">
                <strong>{{ healthSummary.abnormalCount || 0 }}</strong>
                <span>待关注异常体征</span>
              </div>
              <div class="health-metric">
                <strong>{{ healthSummary.normalCount || 0 }}</strong>
                <span>正常记录</span>
              </div>
            </div>
          </div>

          <div class="mini-block">
            <div class="mini-title">处理建议</div>
            <div class="preview-list compact">
              <button
                class="preview-item"
                type="button"
                @click="openRoute('/health/management/data', { source: 'staff-mobile', abnormalFlag: '1', date: 'today' })"
              >
                <div>
                  <strong>优先处理今日异常体征</strong>
                  <p>把员工现场补录的异常血压、血氧、体温集中到健康数据页继续跟进。</p>
                </div>
                <a-tag color="red">{{ healthSummary.abnormalCount || 0 }} 条</a-tag>
              </button>
              <button
                class="preview-item"
                type="button"
                @click="openRoute('/health/management/data', { source: 'staff-mobile', date: 'today' })"
              >
                <div>
                  <strong>查看今日全部体征补录</strong>
                  <p>快速核对现场回执是否已经同步到后台并形成台账。</p>
                </div>
                <a-tag color="blue">{{ healthLatestMeasuredText }}</a-tag>
              </button>
            </div>
          </div>
        </div>
      </a-card>
    </section>

    <section id="suggestions">
      <a-card class="card-elevated" :bordered="false" title="员工建议与现场反馈">
        <template #extra>
          <a-space wrap>
            <a-button size="small" @click="openPath('/oa/employee-suggestions')">员工建议中心</a-button>
            <a-button size="small" @click="openPath('/oa/work-execution/family-communication')">反馈处理台</a-button>
            <a-button size="small" @click="openPath('/stats/staff-mobile-ledger')">查看现场回执</a-button>
          </a-space>
        </template>
        <div v-if="employeeSuggestions.length" class="preview-list">
          <div v-for="item in employeeSuggestions" :key="String(item.id)" class="preview-item static">
            <div>
              <strong>{{ employeeSuggestionTitle(item.content) }}</strong>
              <p>{{ item.proposerName || item.contact || '员工' }} · {{ dateText(item.createTime) || '-' }}</p>
              <div class="content-snippet">{{ employeeSuggestionBody(item.content) }}</div>
            </div>
            <a-tag :color="suggestionStatusColor(item.status)">{{ suggestionStatusText(item.status) }}</a-tag>
          </div>
        </div>
        <a-empty v-else description="暂未检索到员工建议反馈" />
      </a-card>
    </section>

    <section class="action-grid">
      <button class="action-card" type="button" @click="openRoute('/oa/approval', { type: 'LEAVE', status: 'PENDING' })">
        <strong>处理请假调班</strong>
        <span>回到审批中心继续审核员工请假、调班和跨班次申请。</span>
      </button>
      <button class="action-card" type="button" @click="openRoute('/oa/approval', { type: 'MATERIAL_APPLY', status: 'PENDING' })">
        <strong>处理物资申领</strong>
        <span>衔接仓库审核、出库确认和低库存补货闭环。</span>
      </button>
      <button class="action-card" type="button" @click="openPath('/oa/attendance-leave')">
        <strong>查看考勤异常</strong>
        <span>进入考勤与请假联动页，继续核对迟到、早退、外出和请假状态。当前视角异常 {{ attendanceAbnormalCount }} 次。</span>
      </button>
      <button class="action-card" type="button" @click="openPath('/oa/todo')">
        <strong>清理待办积压</strong>
        <span>统一处理员工端同步过来的待办、审批提醒和值班提醒。</span>
      </button>
      <button class="action-card" type="button" @click="openPath('/oa/employee-suggestions')">
        <strong>处理员工建议</strong>
        <span>集中跟进流程优化、设备物资、护理协同和培训排班建议。</span>
      </button>
      <button
        class="action-card"
        type="button"
        @click="openRoute('/health/management/data', { source: 'staff-mobile', abnormalFlag: '1', date: 'today' })"
      >
        <strong>处理异常体征</strong>
        <span>把员工补录发现的异常生命体征转入健康数据台账，继续研判、留痕和复核。</span>
      </button>
    </section>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import { getHealthDataSummary } from '../../api/health'
import { getAttendanceOverview } from '../../api/schedule'
import {
  getApprovalPage,
  getApprovalSummary,
  getOaWorkReportPage,
  getOaWorkReportSummary,
  getSuggestionPage,
  getTodoPage,
  getTodoSummary
} from '../../api/oa'
import { useLiveSyncRefresh } from '../../composables/useLiveSyncRefresh'
import type {
  HealthDataSummary,
  OaApproval,
  OaApprovalSummary,
  OaSuggestion,
  OaTodo,
  OaTodoSummary,
  OaWorkReport,
  OaWorkReportSummary
} from '../../types'

const router = useRouter()
const loading = ref(false)
const approvalSummary = reactive<OaApprovalSummary>({
  totalCount: 0,
  pendingCount: 0,
  approvedCount: 0,
  rejectedCount: 0,
  timeoutPendingCount: 0,
  leavePendingCount: 0,
  reimbursePendingCount: 0,
  purchasePendingCount: 0,
  marketingPlanPendingCount: 0
})
const todoSummary = reactive<OaTodoSummary>({
  totalCount: 0,
  openCount: 0,
  doneCount: 0,
  dueTodayCount: 0,
  overdueCount: 0,
  unassignedCount: 0,
  birthdayOpenCount: 0,
  approvalOpenCount: 0,
  normalOpenCount: 0
})
const reportSummary = reactive<OaWorkReportSummary>({
  totalCount: 0,
  draftCount: 0,
  submittedCount: 0,
  todaySubmittedCount: 0,
  weekSubmittedCount: 0,
  monthSubmittedCount: 0,
  dayTypeCount: 0,
  weekTypeCount: 0,
  monthTypeCount: 0,
  yearTypeCount: 0,
  missingSummaryCount: 0
})
const approvalPreview = ref<OaApproval[]>([])
const todoPreview = ref<OaTodo[]>([])
const reportPreview = ref<OaWorkReport[]>([])
const employeeSuggestions = ref<OaSuggestion[]>([])
const attendanceAbnormalCount = ref(0)
const healthSummary = reactive<HealthDataSummary>({
  totalCount: 0,
  abnormalCount: 0,
  normalCount: 0,
  abnormalRate: 0,
  latestMeasuredAt: '',
  typeStats: []
})

const materialApplyPendingCount = computed(
  () => approvalPreview.value.filter((item) => item.approvalType === 'MATERIAL_APPLY' && item.status === 'PENDING').length
)
const employeeSuggestionPendingCount = computed(
  () => employeeSuggestions.value.filter((item) => item.status === 'PENDING').length
)
const employeeSuggestionProcessingCount = computed(
  () => employeeSuggestions.value.filter((item) => item.status === 'PROCESSING').length
)
const healthLatestMeasuredText = computed(() =>
  healthSummary.latestMeasuredAt ? dayjs(healthSummary.latestMeasuredAt).format('MM-DD HH:mm') : '暂无补录'
)

function openPath(path?: string) {
  if (!path) return
  router.push(path)
}

function openRoute(path: string, query?: Record<string, any>) {
  router.push({ path, query })
}

function scrollToSection(id: string) {
  const target = document.getElementById(id)
  if (target) {
    target.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

function dateText(value?: string) {
  return String(value || '').replace('T', ' ')
}

function approvalTypeText(type?: string) {
  if (type === 'LEAVE') return '请假'
  if (type === 'SHIFT_CHANGE') return '调班'
  if (type === 'MATERIAL_APPLY') return '物资申领'
  return type || '审批'
}

function approvalStatusText(status?: string) {
  if (status === 'APPROVED') return '已通过'
  if (status === 'REJECTED') return '已驳回'
  return '待审批'
}

function approvalStatusColor(status?: string) {
  if (status === 'APPROVED') return 'green'
  if (status === 'REJECTED') return 'red'
  return 'orange'
}

function todoStatusText(status?: string) {
  if (status === 'DONE') return '已完成'
  return '待处理'
}

function todoStatusColor(status?: string) {
  return status === 'DONE' ? 'green' : 'blue'
}

function reportStatusText(status?: string) {
  return status === 'SUBMITTED' ? '已提交' : '草稿'
}

function reportStatusColor(status?: string) {
  return status === 'SUBMITTED' ? 'green' : 'gold'
}

function suggestionStatusText(status?: string) {
  if (status === 'DONE') return '已处理'
  if (status === 'PROCESSING') return '处理中'
  if (status === 'CLOSED') return '已关闭'
  return '待处理'
}

function suggestionStatusColor(status?: string) {
  if (status === 'DONE') return 'green'
  if (status === 'PROCESSING') return 'blue'
  if (status === 'CLOSED') return 'default'
  return 'orange'
}

function employeeSuggestionTitle(content?: string) {
  const text = String(content || '')
  const matched = text.match(/^【员工建议｜(.+?)】/)
  return matched ? `员工建议 · ${matched[1]}` : '员工建议'
}

function employeeSuggestionBody(content?: string) {
  return String(content || '').replace(/^【员工建议｜.+?】/, '').trim() || '暂无内容'
}

async function loadData() {
  loading.value = true
  try {
    const measuredFrom = dayjs().startOf('day').format('YYYY-MM-DDTHH:mm:ss')
    const measuredTo = dayjs().endOf('day').format('YYYY-MM-DDTHH:mm:ss')
    const [
      approvalSummaryRes,
      approvalPage,
      todoSummaryRes,
      todoPage,
      reportSummaryRes,
      reportPage,
      suggestionPage,
      attendanceOverview,
      healthSummaryRes
    ] = await Promise.all([
      getApprovalSummary({}),
      getApprovalPage({
        pageNo: 1,
        pageSize: 8,
        status: 'PENDING'
      }),
      getTodoSummary({}),
      getTodoPage({
        pageNo: 1,
        pageSize: 6,
        status: 'OPEN'
      }),
      getOaWorkReportSummary({ reportType: 'DAY' }),
      getOaWorkReportPage({
        pageNo: 1,
        pageSize: 6,
        reportType: 'DAY'
      }),
      getSuggestionPage({
        pageNo: 1,
        pageSize: 20,
        keyword: '员工建议'
      }),
      getAttendanceOverview({ month: new Date().toISOString().slice(0, 7) }).catch(() => null),
      getHealthDataSummary({
        source: 'staff-mobile',
        measuredFrom,
        measuredTo
      }).catch(() => null)
    ])

    Object.assign(approvalSummary, approvalSummaryRes || {})
    Object.assign(todoSummary, todoSummaryRes || {})
    Object.assign(reportSummary, reportSummaryRes || {})
    Object.assign(healthSummary, {
      totalCount: 0,
      abnormalCount: 0,
      normalCount: 0,
      abnormalRate: 0,
      latestMeasuredAt: '',
      typeStats: [],
      ...(healthSummaryRes || {})
    })

    approvalPreview.value = (approvalPage?.list || []).filter((item) =>
      ['LEAVE', 'SHIFT_CHANGE', 'MATERIAL_APPLY'].includes(String(item.approvalType || ''))
    )
    todoPreview.value = todoPage?.list || []
    reportPreview.value = reportPage?.list || []
    employeeSuggestions.value = (suggestionPage?.list || []).filter((item) =>
      String(item.content || '').startsWith('【员工建议｜')
    )
    attendanceAbnormalCount.value = Number(attendanceOverview?.abnormalCount || 0)
  } catch (error: any) {
    message.error(error?.message || '员工协同中心加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})

useLiveSyncRefresh({
  topics: ['oa', 'hr', 'schedule', 'operations'],
  refresh: () => {
    loadData()
  },
  debounceMs: 900
})
</script>

<style scoped>
.summary-grid,
.panel-grid,
.action-grid {
  display: grid;
  gap: 16px;
}

.summary-grid {
  grid-template-columns: repeat(6, minmax(0, 1fr));
  margin-bottom: 16px;
}

.panel-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
  margin-bottom: 16px;
}

.action-grid {
  grid-template-columns: repeat(4, minmax(0, 1fr));
  margin-top: 16px;
}

.summary-card,
.action-card,
.preview-item {
  width: 100%;
  border: 1px solid var(--border-soft);
  border-radius: 18px;
  background: var(--surface-2);
  text-align: left;
}

.summary-card,
.action-card {
  cursor: pointer;
}

.summary-card {
  display: grid;
  gap: 10px;
  min-height: 138px;
  padding: 18px;
}

.summary-card span,
.summary-card small,
.preview-item p,
.action-card span,
.content-snippet {
  color: var(--muted);
  font-size: 12px;
  line-height: 1.7;
}

.summary-card strong {
  color: var(--ink);
  font-size: 30px;
  line-height: 1.1;
}

.center-alert {
  margin-bottom: 16px;
}

.dual-block {
  display: grid;
  gap: 16px;
}

.mini-block {
  display: grid;
  gap: 12px;
}

.mini-title {
  color: var(--ink);
  font-size: 14px;
  font-weight: 600;
}

.health-overview {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.health-metric {
  display: grid;
  gap: 6px;
  padding: 14px;
  border-radius: 14px;
  background: var(--surface-3);
}

.health-metric strong {
  color: var(--ink);
  font-size: 24px;
  line-height: 1.1;
}

.health-metric span {
  color: var(--muted);
  font-size: 12px;
  line-height: 1.6;
}

.preview-list {
  display: grid;
  gap: 12px;
}

.preview-list.compact {
  gap: 10px;
}

.preview-item {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 16px;
}

.preview-item:not(.static) {
  cursor: pointer;
}

.preview-item strong,
.action-card strong {
  color: var(--ink);
}

.preview-item p {
  margin: 6px 0 0;
}

.content-snippet {
  margin-top: 6px;
  white-space: pre-wrap;
}

.action-card {
  display: grid;
  gap: 8px;
  min-height: 124px;
  padding: 18px;
}

@media (max-width: 1440px) {
  .summary-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .action-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 992px) {
  .panel-grid,
  .summary-grid,
  .action-grid,
  .health-overview {
    grid-template-columns: 1fr;
  }
}
</style>
