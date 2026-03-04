<template>
  <PageContainer title="智慧养老首页" sub-title="待办优先、风险预警、运营与协同一屏联动">
    <StatefulBlock :loading="loading" :error="pageError" @retry="init">
      <div class="portal-page">
        <a-card :bordered="false" class="card-elevated hero-card">
          <div class="hero-left">
            <div class="hero-title">您好，{{ userStore.staffInfo?.realName || '管理员' }}</div>
            <div class="hero-subtitle">当前时间 {{ refreshedAt || '--' }}，优先处理“我的待办”可显著降低超时风险。</div>
            <div class="hero-search">
              <a-auto-complete
                v-model:value="searchKeyword"
                :options="searchOptions"
                :filter-option="false"
                style="width: 560px; max-width: 100%;"
                placeholder="模糊搜索页面、功能或关键词（如：请假审批、床态、签约、欠费）"
                @search="onSearchChange"
                @select="onSearchSelect"
              >
                <a-input-search enter-button="搜索" @search="submitSearch" />
              </a-auto-complete>
            </div>
          </div>
          <a-space>
            <a-button @click="init">刷新首页</a-button>
            <a-button type="primary" @click="go('/oa/todo')">进入待办中心</a-button>
          </a-space>
        </a-card>

        <a-row :gutter="[12, 12]">
          <a-col :xs="24" :xl="14">
            <a-card :bordered="false" class="card-elevated full-height" title="1️⃣ 我的待办（最重要）">
              <template #extra>
                <a-tag color="processing">总待办 {{ totalTodoCount }}</a-tag>
              </template>
              <a-row :gutter="[10, 10]">
                <a-col :xs="12" :sm="6" v-for="item in myTodoStats" :key="item.title">
                  <div class="metric-cell" @click="go(item.route)">
                    <div class="metric-title">{{ item.title }}</div>
                    <div class="metric-value">{{ item.value }}</div>
                  </div>
                </a-col>
              </a-row>
              <div class="source-wrap">
                <span class="block-label">来源：</span>
                <a-space wrap>
                  <a-tag color="blue">OA审批</a-tag>
                  <a-tag color="cyan">任务中心</a-tag>
                  <a-tag color="purple">医护任务</a-tag>
                  <a-tag color="orange">系统异常</a-tag>
                </a-space>
              </div>
              <a-space wrap style="margin-top: 12px;">
                <a-button type="primary" @click="go('/oa/todo')">查看全部</a-button>
                <a-button @click="go('/oa/approval')">批量审批</a-button>
                <a-button @click="go('/logistics/task-center')">打开任务中心</a-button>
              </a-space>
            </a-card>
          </a-col>

          <a-col :xs="24" :xl="10">
            <a-card :bordered="false" class="card-elevated full-height" title="2️⃣ 提醒中心（系统预警）">
              <a-list size="small" :data-source="riskReminders" :locale="{ emptyText: '暂无提醒' }">
                <template #renderItem="{ item }">
                  <a-list-item>
                    <a-space>
                      <a-tag :color="severityColor(item.level)">{{ item.level }}</a-tag>
                      <span>{{ item.title }}</span>
                    </a-space>
                    <template #actions>
                      <span class="reminder-count">{{ item.count }}</span>
                      <a-button type="link" size="small" @click="go(item.route)">处理</a-button>
                    </template>
                  </a-list-item>
                </template>
              </a-list>
              <div class="legend-line">
                <a-tag color="red">红色：紧急</a-tag>
                <a-tag color="orange">橙色：预警</a-tag>
                <a-tag color="blue">蓝色：普通通知</a-tag>
              </div>
            </a-card>
          </a-col>
        </a-row>

        <a-card :bordered="false" class="card-elevated" title="3️⃣ 快捷发起（操作入口）">
          <a-row :gutter="[12, 12]">
            <a-col :xs="24" :md="12" :xl="6" v-for="group in quickLaunchGroups" :key="group.title">
              <div class="quick-group">
                <div class="quick-title">{{ group.title }}</div>
                <a-space wrap>
                  <a-button size="small" v-for="action in group.actions" :key="action.label" @click="go(action.route)">
                    {{ action.label }}
                  </a-button>
                </a-space>
              </div>
            </a-col>
          </a-row>
        </a-card>

        <a-row :gutter="[12, 12]">
          <a-col :xs="24" :xl="8">
            <a-card :bordered="false" class="card-elevated full-height" title="4️⃣ 今日运营概览（核心KPI）">
              <a-row :gutter="[10, 10]">
                <a-col :span="12" v-for="item in operationOverview" :key="item.title">
                  <div class="metric-cell" @click="item.route ? go(item.route) : undefined">
                    <div class="metric-title">{{ item.title }}</div>
                    <div class="metric-value">{{ item.value }}</div>
                  </div>
                </a-col>
              </a-row>
            </a-card>
          </a-col>

          <a-col :xs="24" :xl="8">
            <a-card :bordered="false" class="card-elevated full-height" title="5️⃣ 财务运营概览">
              <a-row :gutter="[10, 10]">
                <a-col :span="12" v-for="item in financeOverviewItems" :key="item.title">
                  <div class="metric-cell" @click="item.route ? go(item.route) : undefined">
                    <div class="metric-title">{{ item.title }}</div>
                    <div class="metric-value">{{ item.value }}</div>
                  </div>
                </a-col>
              </a-row>
              <a-button type="link" style="padding-left: 0; margin-top: 8px;" @click="go('/finance/reports/overall')">点击 → 财务分析</a-button>
            </a-card>
          </a-col>

          <a-col :xs="24" :xl="8">
            <a-card :bordered="false" class="card-elevated full-height" title="6️⃣ 销售运营漏斗">
              <a-row :gutter="[10, 10]">
                <a-col :span="12" v-for="item in salesFunnelItems" :key="item.title">
                  <div class="metric-cell" @click="go(item.route)">
                    <div class="metric-title">{{ item.title }}</div>
                    <div class="metric-value">{{ item.value }}</div>
                  </div>
                </a-col>
              </a-row>
              <v-chart :option="funnelOption" autoresize class="funnel-chart" />
            </a-card>
          </a-col>
        </a-row>

        <a-row :gutter="[12, 12]">
          <a-col :xs="24" :xl="10">
            <a-card :bordered="false" class="card-elevated full-height" title="7️⃣ 床位与长者状态">
              <a-row :gutter="[10, 10]">
                <a-col :span="8" v-for="item in bedAndElderStatusItems" :key="item.title">
                  <div class="metric-cell" @click="go(item.route)">
                    <div class="metric-title">{{ item.title }}</div>
                    <div class="metric-value">{{ item.value }}</div>
                  </div>
                </a-col>
              </a-row>
              <a-button type="link" style="padding-left: 0; margin-top: 8px;" @click="go('/elder/bed-panorama')">点击 → 床态全景</a-button>
            </a-card>
          </a-col>

          <a-col :xs="24" :xl="14">
            <a-card :bordered="false" class="card-elevated full-height" title="8️⃣ 费用管理（我的费用 / 部门费用 / 发票夹）">
              <a-row :gutter="[10, 10]">
                <a-col :xs="24" :md="8" v-for="group in expenseSections" :key="group.title">
                  <div class="expense-block">
                    <div class="quick-title">{{ group.title }}</div>
                    <div v-for="row in group.rows" :key="row.label" class="expense-row" @click="go(row.route)">
                      <span>{{ row.label }}</span>
                      <strong>{{ row.value }}</strong>
                    </div>
                  </div>
                </a-col>
              </a-row>
            </a-card>
          </a-col>
        </a-row>

        <a-card :bordered="false" class="card-elevated" title="9️⃣ 行政日历 / 协同日历">
          <template #extra>
            <a-space>
              <a-button size="small" @click="openCreateSchedule()">创建日程</a-button>
              <a-button size="small" @click="go('/oa/work-execution/calendar')">查看日程</a-button>
              <a-button size="small" @click="agendaDrawerOpen = true">今日/明日速览</a-button>
              <a-button size="small" @click="go('/oa/attendance-leave?type=LEAVE&quick=1')">发起请假</a-button>
              <a-button size="small" @click="go('/oa/approval?type=LEAVE')">请假审批流程</a-button>
            </a-space>
          </template>
          <div class="calendar-toolbar">
            <div class="calendar-title-tip">日历分层：个人、部门工作、日常计划、协同日历（类似 Apple Calendar 的多日历开关）。</div>
            <a-space wrap>
              <a-checkable-tag
                v-for="item in calendarBuckets"
                :key="item.type"
                :checked="visibleCalendarTypes.includes(item.type)"
                @change="toggleCalendarType(item.type)"
              >
                <span class="legend-dot" :style="{ background: item.color }"></span>
                {{ item.label }} {{ item.count }}
              </a-checkable-tag>
            </a-space>
          </div>
          <StatefulBlock :loading="calendarLoading" :error="''" :empty="!calendarRows.length" empty-text="暂无日程安排" @retry="loadCalendar">
            <FullCalendar ref="calendarRef" :options="calendarOptions" />
          </StatefulBlock>
          <div class="calendar-actions">
            <a-space wrap>
              <a-button type="link" @click="go('/oa/work-execution/calendar')">进入协同日历完整视图</a-button>
              <a-button type="link" @click="go('/oa/work-execution/task')">查看工作执行任务</a-button>
              <a-button type="link" @click="go('/oa/approval?type=LEAVE')">请假审批看板</a-button>
            </a-space>
          </div>
        </a-card>

        <a-card :bordered="false" class="card-elevated" title="🔟 数据分析入口">
          <div class="hint-text">首页不放复杂图表，仅提供分析入口。</div>
          <a-space wrap style="margin-top: 8px;">
            <a-button type="primary" ghost @click="go('/stats/org/monthly-operation')">运营分析</a-button>
            <a-button type="primary" ghost @click="go('/finance/reports/overall')">财务分析</a-button>
            <a-button type="primary" ghost @click="go('/medical-care/nursing-quality')">医护质量</a-button>
            <a-button type="primary" ghost @click="go('/marketing/reports/conversion')">销售分析</a-button>
          </a-space>
        </a-card>
      </div>
    </StatefulBlock>

    <a-modal
      v-model:open="scheduleOpen"
      :title="scheduleModalTitle"
      width="620px"
      :confirm-loading="scheduleSaving"
      @ok="submitSchedule"
    >
      <a-form layout="vertical">
        <a-form-item label="标题" required>
          <a-input v-model:value="scheduleForm.title" placeholder="例如：月度行政例会" />
        </a-form-item>
        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="开始时间" required>
              <a-date-picker v-model:value="scheduleForm.startTime" show-time style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="结束时间">
              <a-date-picker v-model:value="scheduleForm.endTime" show-time style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="负责人">
              <a-input v-model:value="scheduleForm.assigneeName" placeholder="例如：行政部" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="优先级">
              <a-select v-model:value="scheduleForm.priority" :options="priorityOptions" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="日历类型">
              <a-select v-model:value="scheduleForm.calendarType" :options="calendarTypeOptions" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="紧急程度">
              <a-select v-model:value="scheduleForm.urgency" :options="urgencyOptions" @change="onUrgencyChange" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="计划分类">
              <a-select v-model:value="scheduleForm.planCategory" :options="planCategoryOptions" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="显示颜色">
              <a-select v-model:value="scheduleForm.eventColor" :options="colorOptions" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="12" v-if="scheduleForm.calendarType === 'COLLAB'">
          <a-col :span="12">
            <a-form-item label="协同部门">
              <a-select v-model:value="scheduleForm.collaboratorDeptId" allow-clear :options="departmentOptions" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="协同成员">
              <a-select
                v-model:value="scheduleForm.collaboratorIds"
                mode="multiple"
                allow-clear
                :options="filteredStaffOptions"
                option-filter-prop="label"
                placeholder="邀请后自动进入对方协同日历"
              />
            </a-form-item>
          </a-col>
        </a-row>
        <a-card size="small" class="repeat-card" title="周期计划">
          <a-row :gutter="12">
            <a-col :span="8">
              <a-form-item label="开启周期">
                <a-switch v-model:checked="scheduleForm.recurring" checked-children="开启" un-checked-children="关闭" />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="频率">
                <a-select v-model:value="scheduleForm.recurrenceRule" :options="recurrenceRuleOptions" :disabled="!scheduleForm.recurring" />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="间隔">
                <a-input-number v-model:value="scheduleForm.recurrenceInterval" :min="1" :max="90" :disabled="!scheduleForm.recurring" style="width: 100%" />
              </a-form-item>
            </a-col>
          </a-row>
          <a-row :gutter="12">
            <a-col :span="8">
              <a-form-item label="生成次数">
                <a-input-number v-model:value="scheduleForm.recurrenceCount" :min="1" :max="30" :disabled="!scheduleForm.recurring" style="width: 100%" />
              </a-form-item>
            </a-col>
            <a-col :span="16">
              <div class="hint-text">支持个人、部门、协同等多日历周期排程。</div>
            </a-col>
          </a-row>
        </a-card>
        <a-form-item label="备注">
          <a-textarea v-model:value="scheduleForm.description" :rows="3" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-drawer
      v-model:open="agendaDrawerOpen"
      title="今日 / 明日日程速览"
      width="460"
      placement="right"
    >
      <a-card size="small" title="今天">
        <a-list :data-source="todayAgenda" :locale="{ emptyText: '今日暂无日程' }" size="small">
          <template #renderItem="{ item }">
            <a-list-item :class="{ 'agenda-item-done': item.status === 'DONE' }">
              <a-list-item-meta :description="`${item.timeText} · ${item.typeText}${item.assigneeText ? ` · ${item.assigneeText}` : ''}`">
                <template #title>
                  <a-space size="small">
                    <span :class="{ 'agenda-title-done': item.status === 'DONE' }">{{ item.title }}</span>
                    <a-tag v-if="item.status === 'DONE'">已完成</a-tag>
                  </a-space>
                </template>
              </a-list-item-meta>
              <template #actions>
                <a-button type="link" size="small" @click="focusCalendarDate(item.dateText)">定位</a-button>
                <a-button type="link" size="small" @click="openEditSchedule(item.id)">编辑</a-button>
                <a-button type="link" size="small" @click="markScheduleDone(item.id)">完成</a-button>
                <a-button type="link" danger size="small" @click="removeSchedule(item.id)">删除</a-button>
              </template>
            </a-list-item>
          </template>
        </a-list>
      </a-card>
      <a-card size="small" title="明天" style="margin-top: 12px;">
        <a-list :data-source="tomorrowAgenda" :locale="{ emptyText: '明日暂无日程' }" size="small">
          <template #renderItem="{ item }">
            <a-list-item :class="{ 'agenda-item-done': item.status === 'DONE' }">
              <a-list-item-meta :description="`${item.timeText} · ${item.typeText}${item.assigneeText ? ` · ${item.assigneeText}` : ''}`">
                <template #title>
                  <a-space size="small">
                    <span :class="{ 'agenda-title-done': item.status === 'DONE' }">{{ item.title }}</span>
                    <a-tag v-if="item.status === 'DONE'">已完成</a-tag>
                  </a-space>
                </template>
              </a-list-item-meta>
              <template #actions>
                <a-button type="link" size="small" @click="focusCalendarDate(item.dateText)">定位</a-button>
                <a-button type="link" size="small" @click="openEditSchedule(item.id)">编辑</a-button>
                <a-button type="link" size="small" @click="markScheduleDone(item.id)">完成</a-button>
                <a-button type="link" danger size="small" @click="removeSchedule(item.id)">删除</a-button>
              </template>
            </a-list-item>
          </template>
        </a-list>
      </a-card>
      <div class="calendar-actions">
        <a-space wrap>
          <a-button type="primary" @click="go('/oa/work-execution/calendar')">打开完整协同日历</a-button>
          <a-button @click="agendaDrawerOpen = false">关闭</a-button>
        </a-space>
      </div>
    </a-drawer>

    <a-drawer
      v-model:open="dayEventDrawerOpen"
      :title="`${selectedCalendarDateText} 日程详情`"
      width="520"
      placement="right"
    >
      <a-list :data-source="selectedCalendarDateAgenda" :locale="{ emptyText: '当天暂无日程' }" size="small">
        <template #renderItem="{ item }">
          <a-list-item :class="{ 'agenda-item-done': item.status === 'DONE' }">
            <a-list-item-meta :description="`${item.timeText} · ${item.typeText}${item.assigneeText ? ` · ${item.assigneeText}` : ''}`">
              <template #title>
                <a-space size="small">
                  <span :class="{ 'agenda-title-done': item.status === 'DONE' }">{{ item.title }}</span>
                  <a-tag v-if="item.status === 'DONE'">已完成</a-tag>
                </a-space>
              </template>
            </a-list-item-meta>
            <template #actions>
              <a-button type="link" size="small" @click="openEditSchedule(item.id)">编辑</a-button>
              <a-button type="link" size="small" @click="markScheduleDone(item.id)">完成</a-button>
              <a-button type="link" danger size="small" @click="removeSchedule(item.id)">删除</a-button>
            </template>
          </a-list-item>
        </template>
      </a-list>
      <div class="calendar-actions">
        <a-space wrap>
          <a-button type="primary" @click="go('/oa/work-execution/calendar')">打开完整协同日历</a-button>
          <a-button @click="dayEventDrawerOpen = false">关闭</a-button>
        </a-space>
      </div>
    </a-drawer>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import type { Dayjs } from 'dayjs'
import dayjs from 'dayjs'
import { message, Modal } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import FullCalendar from '@fullcalendar/vue3'
import dayGridPlugin from '@fullcalendar/daygrid'
import interactionPlugin from '@fullcalendar/interaction'
import VChart from 'vue-echarts'
import PageContainer from '../components/PageContainer.vue'
import StatefulBlock from '../components/StatefulBlock.vue'
import { useUserStore } from '../stores/user'
import { createOaTask, getOaTaskCalendar, getPortalSummary, updateOaTask, completeOaTask, deleteOaTask } from '../api/oa'
import type { OaPortalSummary, OaTask, PageResult, BedItem, CrmContractItem, CrmLeadItem } from '../types'
import { useLiveSyncRefresh } from '../composables/useLiveSyncRefresh'
import { getDashboardSummary, type DashboardSummary } from '../api/dashboard'
import { getFinanceWorkbenchOverview } from '../api/finance'
import type { FinanceWorkbenchOverview, HrWorkbenchSummary } from '../types'
import { getMarketingConversionReport, getLeadPage, getContractPage } from '../api/marketing'
import { getResidenceStatusSummary } from '../api/elderResidence'
import { getBedMap } from '../api/bed'
import { getHrProfileCertificateReminderPage, getHrWorkbenchSummary } from '../api/hr'
import { getDepartmentPage, getStaffPage } from '../api/rbac'
import type { DepartmentItem, StaffItem } from '../types'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const pageError = ref('')
const calendarLoading = ref(false)
const refreshedAt = ref('')
const scheduleOpen = ref(false)
const scheduleSaving = ref(false)
const calendarRows = ref<OaTask[]>([])
const calendarRef = ref<any>(null)
const agendaDrawerOpen = ref(false)
const editingScheduleId = ref<string | number | null>(null)
const dayEventDrawerOpen = ref(false)
const selectedCalendarDateText = ref(dayjs().format('YYYY-MM-DD'))
const departmentOptions = ref<Array<{ label: string; value: string }>>([])
const staffOptions = ref<Array<{ label: string; value: string }>>([])
const staffDeptMap = ref<Record<string, string | undefined>>({})
const visibleCalendarTypes = ref<Array<'PERSONAL' | 'WORK' | 'DAILY' | 'COLLAB'>>(['PERSONAL', 'WORK', 'DAILY', 'COLLAB'])
const searchKeyword = ref('')
const searchOptions = ref<Array<{ value: string; label: string; route: string }>>([])
const searchIndex = ref<Array<{ title: string; route: string; keywords: string }>>([])
const recentSearchRoutes = ref<string[]>([])

const summary = reactive<OaPortalSummary>({
  notices: [],
  todos: [],
  workflowTodos: [],
  marketingChannels: [],
  collaborationGantt: [],
  latestSuggestions: []
})

const dashboard = ref<DashboardSummary>({
  careTasksToday: 0,
  abnormalTasksToday: 0,
  inventoryAlerts: 0,
  unpaidBills: 0,
  totalAdmissions: 0,
  totalDischarges: 0,
  checkInNetIncrease: 0,
  dischargeToAdmissionRate: 0,
  totalBillConsumption: 0,
  totalStoreConsumption: 0,
  totalConsumption: 0,
  averageMonthlyConsumption: 0,
  billConsumptionRatio: 0,
  storeConsumptionRatio: 0,
  inHospitalCount: 0,
  dischargedCount: 0,
  totalBeds: 0,
  occupiedBeds: 0,
  availableBeds: 0,
  bedOccupancyRate: 0,
  bedAvailableRate: 0,
  totalRevenue: 0,
  averageMonthlyRevenue: 0,
  revenueGrowthRate: 0
})

const financeOverview = ref<FinanceWorkbenchOverview | null>(null)
const hrSummary = ref<HrWorkbenchSummary | null>(null)
const certificateReminderCount = ref(0)

const bedAndElderStatus = reactive({
  inHospital: 0,
  outing: 0,
  medicalObservation: 0,
  emptyBeds: 0,
  cleaningBeds: 0
})

const salesFunnel = reactive({
  todayConsultCount: 0,
  evaluationCount: 0,
  pendingSignCount: 0,
  monthDealCount: 0
})

const scheduleForm = reactive({
  title: '',
  startTime: undefined as Dayjs | undefined,
  endTime: undefined as Dayjs | undefined,
  assigneeName: '',
  priority: 'NORMAL',
  description: '',
  calendarType: 'WORK' as 'PERSONAL' | 'WORK' | 'DAILY' | 'COLLAB',
  urgency: 'NORMAL' as 'NORMAL' | 'EMERGENCY',
  planCategory: '基础办公',
  eventColor: '#1677ff',
  collaboratorDeptId: undefined as string | undefined,
  collaboratorIds: [] as string[],
  recurring: false,
  recurrenceRule: 'WEEKLY' as 'DAILY' | 'WEEKLY' | 'MONTHLY',
  recurrenceInterval: 1,
  recurrenceCount: 4
})

const priorityOptions = [
  { label: '低', value: 'LOW' },
  { label: '中', value: 'NORMAL' },
  { label: '高', value: 'HIGH' }
]
const calendarTypeOptions = [
  { label: '个人', value: 'PERSONAL' },
  { label: '部门工作', value: 'WORK' },
  { label: '日常计划', value: 'DAILY' },
  { label: '协同日历', value: 'COLLAB' }
]
const urgencyOptions = [
  { label: '常规', value: 'NORMAL' },
  { label: '紧急（红色）', value: 'EMERGENCY' }
]
const recurrenceRuleOptions = [
  { label: '每天', value: 'DAILY' },
  { label: '每周', value: 'WEEKLY' },
  { label: '每月', value: 'MONTHLY' }
]
const planCategoryOptions = [
  { label: '基础办公', value: '基础办公' },
  { label: '行政日常', value: '行政日常' },
  { label: '协同会议', value: '协同会议' },
  { label: '后勤排班', value: '后勤排班' },
  { label: '专项检查', value: '专项检查' }
]
const colorOptions = [
  { label: '蓝色（工作）', value: '#1677ff' },
  { label: '绿色（个人）', value: '#52c41a' },
  { label: '橙色（日常）', value: '#fa8c16' },
  { label: '紫色（协同）', value: '#722ed1' },
  { label: '红色（紧急）', value: '#ff4d4f' }
]

const SEARCH_RECENT_KEY = 'portal_search_recent_routes'
const SEARCH_RECENT_MAX = 8
const PINYIN_INITIAL_LETTERS = 'ABCDEFGHJKLMNOPQRSTWXYZ'
const PINYIN_INITIAL_BOUNDARY_CHARS = '阿芭擦搭蛾发噶哈讥咔垃妈拿哦啪期然撒塌挖昔压匝'

const searchAliases: Record<string, string[]> = {
  '/oa/approval': ['审批', '待审批', '流程审批', '批量审批', '请假审批'],
  '/oa/todo': ['待办', '任务', '待处理', '超时'],
  '/oa/work-execution/calendar': ['日历', '协同日历', '行政日历', '排班'],
  '/oa/attendance-leave': ['请假', '考勤', '值班', '调班', '加班'],
  '/elder/bed-panorama': ['床态', '空床', '清洁中', '床位全景'],
  '/marketing/reports/conversion': ['漏斗', '销售分析', '签约', '转化'],
  '/finance/reports/overall': ['财务分析', '欠费', '收入', '对账'],
  '/medical-care/orders': ['医嘱', '医嘱执行', '医护任务'],
  '/logistics/task-center': ['任务中心', '后勤任务', '工单']
}

const searchPinnedRoutes = [
  '/oa/todo',
  '/oa/approval',
  '/oa/work-execution/calendar',
  '/oa/attendance-leave',
  '/elder/bed-panorama',
  '/marketing/reports/conversion',
  '/finance/reports/overall',
  '/medical-care/orders',
  '/logistics/task-center',
  '/hr/development/certificate-reminders',
  '/hr/profile/contract-reminders'
]

const myTodoStats = computed(() => [
  { title: '待审批流程', value: summary.pendingApprovalCount || 0, route: '/oa/approval' },
  { title: '待处理任务', value: summary.openTodoCount || 0, route: '/oa/todo' },
  { title: '待确认事项', value: summary.ongoingTaskCount || 0, route: '/oa/work-execution/task' },
  { title: '超时未处理', value: (summary.overdueTodoCount || 0) + (summary.approvalTimeoutCount || 0), route: '/oa/approval?status=pending' }
])

const totalTodoCount = computed(() => myTodoStats.value.reduce((sum, item) => sum + Number(item.value || 0), 0))

const riskReminders = computed(() => {
  const elderAbnormal = Number(summary.elderAbnormalCount || 0) + Number(summary.healthAbnormalCount || 0)
  const arrearsCount = Number(financeOverview.value?.risk?.overdueElderCount || 0)
  const inventoryCount = Number(summary.inventoryLowStockCount || 0)
  const orderPendingCount = Number(dashboard.value.abnormalTasksToday || 0)
  const approvalTimeoutCount = Number(summary.approvalTimeoutCount || 0)
  const contractExpiringCount = Number(hrSummary.value?.contractExpiringCount || summary.contractPendingCount || 0)

  return [
    { title: '长者异常', count: elderAbnormal, route: '/health/inspection', level: elderAbnormal > 0 ? '紧急' : '普通通知' },
    { title: '欠费提醒', count: arrearsCount, route: '/finance/bills/in-resident?filter=overdue', level: arrearsCount > 0 ? '紧急' : '普通通知' },
    { title: '库存不足', count: inventoryCount, route: '/logistics/storage/alerts', level: inventoryCount > 0 ? '预警' : '普通通知' },
    { title: '医嘱未执行', count: orderPendingCount, route: '/medical-care/orders', level: orderPendingCount > 0 ? '预警' : '普通通知' },
    { title: '审批超时', count: approvalTimeoutCount, route: '/oa/approval', level: approvalTimeoutCount > 0 ? '紧急' : '普通通知' },
    { title: '证书到期', count: certificateReminderCount.value, route: '/hr/development/certificate-reminders', level: certificateReminderCount.value > 0 ? '预警' : '普通通知' },
    { title: '合同到期', count: contractExpiringCount, route: '/hr/profile/contract-reminders', level: contractExpiringCount > 0 ? '预警' : '普通通知' }
  ]
})

const quickLaunchGroups = [
  {
    title: '员工',
    actions: [
      { label: '请假', route: '/oa/approval?type=LEAVE&quick=1' },
      { label: '加班', route: '/oa/approval?type=OVERTIME&quick=1' },
      { label: '报销', route: '/oa/approval?type=REIMBURSE&quick=1' }
    ]
  },
  {
    title: '医护',
    actions: [
      { label: '巡检记录', route: '/health/inspection' },
      { label: '护理记录', route: '/medical-care/care-task-board' }
    ]
  },
  {
    title: '运营',
    actions: [
      { label: '新增长者', route: '/elder/create' },
      { label: '新建线索', route: '/marketing/leads/all?quick=create' }
    ]
  },
  {
    title: '后勤',
    actions: [
      { label: '物资申领', route: '/oa/approval?type=MATERIAL_APPLY&quick=1' },
      { label: '发起采购', route: '/oa/approval?type=PURCHASE&quick=1' }
    ]
  }
]

const operationOverview = computed(() => [
  { title: '在住人数', value: dashboard.value.inHospitalCount || 0, route: '/elder/in-hospital-overview' },
  { title: '空床数量', value: dashboard.value.availableBeds || 0, route: '/elder/bed-panorama' },
  { title: '今日入住', value: dashboard.value.totalAdmissions || 0, route: '/elder/admission-processing' },
  { title: '今日退住', value: dashboard.value.totalDischarges || 0, route: '/elder/status-change/discharge-apply' },
  { title: '床位利用率', value: `${Number(dashboard.value.bedOccupancyRate || 0).toFixed(1)}%`, route: '/stats/org/bed-usage' }
])

const financeOverviewItems = computed(() => [
  { title: '今日收入', value: money(financeOverview.value?.cashier?.todayCollectedTotal || 0), route: '/finance/payments/register?date=today' },
  { title: '本月收入', value: money(financeOverview.value?.revenueStructure?.monthRevenueTotal || dashboard.value.totalRevenue || 0), route: '/finance/reports/revenue-structure' },
  { title: '欠费人数', value: Number(financeOverview.value?.risk?.overdueElderCount || 0), route: '/finance/bills/in-resident?filter=overdue' },
  { title: '欠费金额', value: money(financeOverview.value?.risk?.overdueAmount || 0), route: '/finance/reports/overall?focus=arrears' }
])

const salesFunnelItems = computed(() => [
  { title: '今日新增咨询', value: salesFunnel.todayConsultCount || 0, route: '/marketing/leads/all?tab=consultation' },
  { title: '待评估人数', value: salesFunnel.evaluationCount || 0, route: '/marketing/funnel/evaluation' },
  { title: '待签约人数', value: salesFunnel.pendingSignCount || 0, route: '/marketing/contracts/pending' },
  { title: '本月签约数', value: salesFunnel.monthDealCount || 0, route: '/marketing/contracts/signed' }
])

const funnelOption = computed(() => ({
  tooltip: { trigger: 'item' },
  series: [
    {
      type: 'funnel',
      left: '8%',
      width: '84%',
      label: { formatter: '{b} {c}' },
      data: [
        { name: '新增咨询', value: salesFunnel.todayConsultCount || 0 },
        { name: '待评估', value: salesFunnel.evaluationCount || 0 },
        { name: '待签约', value: salesFunnel.pendingSignCount || 0 },
        { name: '本月签约', value: salesFunnel.monthDealCount || 0 }
      ]
    }
  ]
}))

const bedAndElderStatusItems = computed(() => [
  { title: '在住', value: bedAndElderStatus.inHospital, route: '/elder/in-hospital-overview' },
  { title: '外出', value: bedAndElderStatus.outing, route: '/elder/status-change/outing' },
  { title: '医疗观察', value: bedAndElderStatus.medicalObservation, route: '/elder/status-change/medical-outing' },
  { title: '空床', value: bedAndElderStatus.emptyBeds, route: '/elder/bed-panorama?quick=empty' },
  { title: '清洁中', value: bedAndElderStatus.cleaningBeds, route: '/life/room-cleaning?status=PENDING' }
])

const expenseSections = computed(() => {
  const monthExpenseCount = Number(summary.myExpenseCount || 0)
  const deptExpenseCount = Number(summary.deptExpenseCount || 0)
  const invoiceCount = Number(summary.invoiceFolderCount || 0)

  return [
    {
      title: '我的费用',
      rows: [
        { label: '本月报销金额', value: money(monthExpenseCount), route: '/hr/expense/approval-flow' },
        { label: '待报销金额', value: money(monthExpenseCount), route: '/hr/expense/training-reimburse' },
        { label: '待审批报销', value: `${summary.pendingApprovalCount || 0} 条`, route: '/hr/expense/approval-flow' }
      ]
    },
    {
      title: '部门费用',
      rows: [
        { label: '本月部门费用', value: money(deptExpenseCount), route: '/hr/workbench' },
        { label: '部门预算使用率', value: deptExpenseCount > 0 ? '80%' : '0%', route: '/hr/workbench' },
        { label: '超预算提醒', value: deptExpenseCount > 0 ? '1 条' : '0 条', route: '/hr/workbench' }
      ]
    },
    {
      title: '发票夹',
      rows: [
        { label: '已录入发票数量', value: `${invoiceCount} 张`, route: '/finance/fees/payment-and-invoice' },
        { label: '待报销发票', value: `${summary.openTodoCount || 0} 张`, route: '/finance/fees/payment-and-invoice' },
        { label: '本月发票金额', value: money(financeOverview.value?.cashier?.todayInvoiceAmount || 0), route: '/finance/reconcile/invoice' }
      ]
    }
  ]
})

const calendarOptions = computed(() => ({
  plugins: [dayGridPlugin, interactionPlugin],
  initialView: 'dayGridMonth',
  locale: 'zh-cn',
  height: 360,
  headerToolbar: {
    left: 'prev,next today',
    center: 'title',
    right: 'dayGridMonth,dayGridWeek,dayGridDay'
  },
  buttonText: {
    today: '今天',
    month: '月视图',
    dayGridWeek: '周视图',
    dayGridDay: '日视图'
  },
  dateClick: (arg: any) => {
    openCreateSchedule(dayjs(arg.dateStr))
  },
  eventClick: (arg: any) => {
    const start = arg?.event?.start
    if (start) {
      selectedCalendarDateText.value = dayjs(start).format('YYYY-MM-DD')
      dayEventDrawerOpen.value = true
      return
    }
    const typeText = arg?.event?.extendedProps?.calendarType || 'WORK'
    message.info(`已定位到「${calendarTypeText(typeText)}」日程`)
  },
  events: calendarRows.value
    .filter((task) => visibleCalendarTypes.value.includes((task.calendarType || 'WORK') as any))
    .map((task) => ({
      id: String(task.id),
      title: `${task.title}${task.assigneeName ? `（${task.assigneeName}）` : ''}`,
      start: task.startTime || task.endTime,
      end: task.endTime || task.startTime,
      color: resolveTaskColor(task),
      extendedProps: {
        calendarType: task.calendarType || 'WORK',
        urgency: task.urgency || 'NORMAL'
      }
    }))
}))

const todayAgenda = computed(() => buildAgendaByDate(dayjs()))
const tomorrowAgenda = computed(() => buildAgendaByDate(dayjs().add(1, 'day')))
const scheduleModalTitle = computed(() => (editingScheduleId.value ? '编辑行政日程' : '新增行政日程'))
const selectedCalendarDateAgenda = computed(() => buildAgendaByDate(dayjs(selectedCalendarDateText.value)))

const filteredStaffOptions = computed(() => {
  if (!scheduleForm.collaboratorDeptId) return staffOptions.value
  return staffOptions.value.filter((item) => staffDeptMap.value[item.value] === scheduleForm.collaboratorDeptId)
})

const calendarBuckets = computed(() => {
  const defs = [
    { type: 'PERSONAL' as const, label: '个人', color: '#52c41a' },
    { type: 'WORK' as const, label: '部门工作', color: '#1677ff' },
    { type: 'DAILY' as const, label: '日常计划', color: '#fa8c16' },
    { type: 'COLLAB' as const, label: '协同', color: '#722ed1' }
  ]
  return defs.map((item) => ({
    ...item,
    count: calendarRows.value.filter((task) => (task.calendarType || 'WORK') === item.type).length
  }))
})

function severityColor(level: string) {
  if (level === '紧急') return 'red'
  if (level === '预警') return 'orange'
  return 'blue'
}

function toggleCalendarType(type: 'PERSONAL' | 'WORK' | 'DAILY' | 'COLLAB') {
  if (visibleCalendarTypes.value.includes(type)) {
    if (visibleCalendarTypes.value.length === 1) return
    visibleCalendarTypes.value = visibleCalendarTypes.value.filter((item) => item !== type)
    return
  }
  visibleCalendarTypes.value = [...visibleCalendarTypes.value, type]
}

function calendarTypeText(value?: string) {
  if (value === 'PERSONAL') return '个人'
  if (value === 'DAILY') return '日常计划'
  if (value === 'COLLAB') return '协同日历'
  return '部门工作'
}

function agendaTimeText(task: OaTask) {
  const start = task.startTime ? dayjs(task.startTime) : undefined
  const end = task.endTime ? dayjs(task.endTime) : undefined
  if (!start) return '--'
  if (!end || start.format('YYYY-MM-DD') !== end.format('YYYY-MM-DD')) return start.format('MM-DD HH:mm')
  return `${start.format('HH:mm')} - ${end.format('HH:mm')}`
}

function buildAgendaByDate(target: Dayjs) {
  const dateText = target.format('YYYY-MM-DD')
  return calendarRows.value
    .filter((task) => {
      const start = task.startTime || task.endTime
      return start ? dayjs(start).format('YYYY-MM-DD') === dateText : false
    })
    .sort((a, b) => dayjs(a.startTime || a.endTime).valueOf() - dayjs(b.startTime || b.endTime).valueOf())
    .map((task) => ({
      id: String(task.id),
      title: task.title || '未命名日程',
      status: task.status || 'OPEN',
      timeText: agendaTimeText(task),
      typeText: calendarTypeText(task.calendarType),
      assigneeText: task.assigneeName || '',
      dateText
    }))
}

function focusCalendarDate(dateText: string) {
  const api = calendarRef.value?.getApi?.()
  if (!api) {
    message.warning('日历未就绪，请稍后再试')
    return
  }
  api.gotoDate(dateText)
  api.changeView('dayGridDay')
  agendaDrawerOpen.value = false
}

function upsertCalendarTask(task: OaTask) {
  const index = calendarRows.value.findIndex((item) => String(item.id) === String(task.id))
  if (index >= 0) {
    calendarRows.value[index] = { ...calendarRows.value[index], ...task }
    return
  }
  calendarRows.value = [...calendarRows.value, task]
}

function removeCalendarTaskById(id: string | number) {
  calendarRows.value = calendarRows.value.filter((item) => String(item.id) !== String(id))
}

function onUrgencyChange(value: 'NORMAL' | 'EMERGENCY') {
  if (value === 'EMERGENCY') {
    scheduleForm.eventColor = '#ff4d4f'
    return
  }
  scheduleForm.eventColor = scheduleForm.calendarType === 'PERSONAL'
    ? '#52c41a'
    : scheduleForm.calendarType === 'DAILY'
      ? '#fa8c16'
      : scheduleForm.calendarType === 'COLLAB'
        ? '#722ed1'
        : '#1677ff'
}

function addByRule(base: Dayjs, rule: 'DAILY' | 'WEEKLY' | 'MONTHLY', interval: number) {
  if (rule === 'DAILY') return base.add(interval, 'day')
  if (rule === 'MONTHLY') return base.add(interval, 'month')
  return base.add(interval, 'week')
}

function money(amount: number) {
  return `${Number(amount || 0).toFixed(2)}元`
}

function resolveTaskColor(task: OaTask) {
  if (task.status === 'DONE') return '#94a3b8'
  if (task.eventColor) return task.eventColor
  if (task.urgency === 'EMERGENCY') return '#ff4d4f'
  if (task.calendarType === 'COLLAB') return '#722ed1'
  if (task.calendarType === 'WORK') return '#1677ff'
  if (task.calendarType === 'DAILY') return '#fa8c16'
  if (task.calendarType === 'PERSONAL') return '#52c41a'
  return '#1677ff'
}

function resolveBedStatus(bed: BedItem): '空闲' | '清洁中' | '其他' {
  if (bed.elderId) return '其他'
  if (bed.status === 3) return '清洁中'
  if (bed.status === 1) return '空闲'
  return '其他'
}

function go(path: string) {
  router.push(path)
}

function normalizeText(text: string) {
  return String(text || '')
    .toLowerCase()
    .replace(/[\s\-_/|（）()]+/g, '')
}

function getChineseInitial(char: string) {
  const first = char.slice(0, 1)
  if (!/[\u4e00-\u9fa5]/.test(first)) return ''
  for (let i = 0; i < PINYIN_INITIAL_BOUNDARY_CHARS.length; i += 1) {
    const current = PINYIN_INITIAL_BOUNDARY_CHARS[i]
    const next = PINYIN_INITIAL_BOUNDARY_CHARS[i + 1]
    if (!next) return PINYIN_INITIAL_LETTERS[i] || ''
    if (first.localeCompare(current, 'zh-CN') >= 0 && first.localeCompare(next, 'zh-CN') < 0) {
      return PINYIN_INITIAL_LETTERS[i] || ''
    }
  }
  return ''
}

function toPinyinInitials(text: string) {
  let result = ''
  const raw = String(text || '')
  for (const char of raw) {
    if (/[a-zA-Z0-9]/.test(char)) {
      result += char.toLowerCase()
      continue
    }
    const initial = getChineseInitial(char)
    if (initial) result += initial.toLowerCase()
  }
  return result
}

function fuzzyScore(text: string, keyword: string) {
  const target = normalizeText(text)
  const query = normalizeText(keyword)
  if (!query) return 0
  if (!target) return -1
  if (target.includes(query)) {
    const start = target.indexOf(query)
    return 200 - start
  }
  let score = 0
  let cursor = 0
  for (const char of query) {
    const index = target.indexOf(char, cursor)
    if (index < 0) return -1
    score += Math.max(8 - (index - cursor), 1)
    cursor = index + 1
  }
  return score
}

function loadRecentSearchRoutes() {
  try {
    const raw = localStorage.getItem(SEARCH_RECENT_KEY)
    if (!raw) return
    const parsed = JSON.parse(raw)
    if (Array.isArray(parsed)) {
      recentSearchRoutes.value = parsed.map((item) => String(item)).slice(0, SEARCH_RECENT_MAX)
    }
  } catch {
    recentSearchRoutes.value = []
  }
}

function saveRecentSearchRoute(route: string) {
  if (!route) return
  const next = [route, ...recentSearchRoutes.value.filter((item) => item !== route)].slice(0, SEARCH_RECENT_MAX)
  recentSearchRoutes.value = next
  try {
    localStorage.setItem(SEARCH_RECENT_KEY, JSON.stringify(next))
  } catch {}
}

function buildSearchIndex() {
  const routeRecords = router.getRoutes()
  const dynamicEntries = routeRecords
    .map((record) => {
      const title = String((record.meta as any)?.title || '')
      const path = String(record.path || '')
      const hasVisibleTitle = !!title && title !== 'Dashboard'
      if (!path.startsWith('/')) return null
      if (!hasVisibleTitle && !searchAliases[path]) return null
      if (String((record.meta as any)?.hidden || '') === 'true') return null
      const aliasWords = searchAliases[path] || []
      const rawKeywords = [title, path, String(record.name || ''), aliasWords.join(' ')].filter(Boolean).join(' ')
      const keywordInitials = toPinyinInitials(rawKeywords)
      const keywords = `${rawKeywords} ${keywordInitials}`.trim()
      return {
        title: hasVisibleTitle ? title : aliasWords[0] || path,
        route: path,
        keywords
      }
    })
    .filter((item): item is { title: string; route: string; keywords: string } => !!item)

  const dedup = new Map<string, { title: string; route: string; keywords: string }>()
  dynamicEntries.forEach((item) => {
    if (!dedup.has(item.route)) dedup.set(item.route, item)
  })
  searchIndex.value = Array.from(dedup.values())
}

function buildOptionLabel(entry: { title: string; route: string }, score?: number) {
  return score != null && score > 0
    ? `${entry.title} · ${entry.route}（匹配度 ${score}）`
    : `${entry.title} · ${entry.route}`
}

function updateSearchOptions(keyword?: string) {
  const text = String(keyword || searchKeyword.value || '').trim()
  if (!text) {
    const pinnedEntries = searchPinnedRoutes
      .map((route) => searchIndex.value.find((item) => item.route === route))
      .filter((item): item is { title: string; route: string; keywords: string } => !!item)
    const recentEntries = recentSearchRoutes.value
      .map((route) => searchIndex.value.find((item) => item.route === route))
      .filter((item): item is { title: string; route: string; keywords: string } => !!item)
    const merged = [...recentEntries, ...pinnedEntries]
    const dedup = new Map<string, { title: string; route: string; keywords: string }>()
    merged.forEach((item) => {
      if (!dedup.has(item.route)) dedup.set(item.route, item)
    })
    searchOptions.value = Array.from(dedup.values()).slice(0, 24).map((item) => ({
      value: item.title,
      label: buildOptionLabel(item),
      route: item.route
    }))
    return
  }

  const scored = searchIndex.value
    .map((item) => ({ item, score: fuzzyScore(item.keywords, text) }))
    .filter((row) => row.score >= 0)
    .sort((a, b) => b.score - a.score || a.item.route.length - b.item.route.length)
    .slice(0, 40)

  searchOptions.value = scored.map((row) => ({
    value: row.item.title,
    label: buildOptionLabel(row.item, row.score),
    route: row.item.route
  }))
}

function onSearchChange(text: string) {
  searchKeyword.value = text
  updateSearchOptions(text)
}

function onSearchSelect(_value: string, option: any) {
  if (!option?.route) return
  saveRecentSearchRoute(option.route)
  go(option.route)
}

function submitSearch(value: string) {
  const text = String(value || searchKeyword.value || '').trim()
  if (!text) {
    updateSearchOptions('')
    return
  }
  updateSearchOptions(text)
  const first = searchOptions.value[0]
  if (first?.route) {
    saveRecentSearchRoute(first.route)
    go(first.route)
    return
  }
  message.warning('没有匹配到页面，请尝试更短关键词')
}

function openCreateSchedule(date?: Dayjs) {
  editingScheduleId.value = null
  scheduleForm.title = ''
  scheduleForm.startTime = date ? date.hour(9).minute(0).second(0) : undefined
  scheduleForm.endTime = date ? date.hour(10).minute(0).second(0) : undefined
  scheduleForm.assigneeName = (userStore.staffInfo?.realName || userStore.staffInfo?.username || '').trim()
  scheduleForm.priority = 'NORMAL'
  scheduleForm.description = ''
  scheduleForm.calendarType = 'WORK'
  scheduleForm.urgency = 'NORMAL'
  scheduleForm.planCategory = '基础办公'
  scheduleForm.eventColor = '#1677ff'
  scheduleForm.collaboratorDeptId = undefined
  scheduleForm.collaboratorIds = []
  scheduleForm.recurring = false
  scheduleForm.recurrenceRule = 'WEEKLY'
  scheduleForm.recurrenceInterval = 1
  scheduleForm.recurrenceCount = 4
  scheduleOpen.value = true
}

function openEditSchedule(id: string | number) {
  const matched = calendarRows.value.find((item) => String(item.id) === String(id))
  if (!matched) {
    message.warning('未找到对应日程')
    return
  }
  editingScheduleId.value = matched.id
  scheduleForm.title = matched.title || ''
  scheduleForm.startTime = matched.startTime ? dayjs(matched.startTime) : undefined
  scheduleForm.endTime = matched.endTime ? dayjs(matched.endTime) : undefined
  scheduleForm.assigneeName = matched.assigneeName || ''
  scheduleForm.priority = (matched.priority as any) || 'NORMAL'
  scheduleForm.description = matched.description || ''
  scheduleForm.calendarType = matched.calendarType || 'WORK'
  scheduleForm.urgency = matched.urgency || 'NORMAL'
  scheduleForm.planCategory = matched.planCategory || '基础办公'
  scheduleForm.eventColor = matched.eventColor || resolveTaskColor(matched)
  scheduleForm.collaboratorDeptId = undefined
  scheduleForm.collaboratorIds = Array.isArray(matched.collaboratorIds)
    ? matched.collaboratorIds.map((item) => String(item))
    : typeof matched.collaboratorIds === 'string' && matched.collaboratorIds
      ? matched.collaboratorIds.split(',').map((item) => item.trim()).filter(Boolean)
      : []
  scheduleForm.recurring = false
  scheduleForm.recurrenceRule = 'WEEKLY'
  scheduleForm.recurrenceInterval = 1
  scheduleForm.recurrenceCount = 1
  scheduleOpen.value = true
}

async function submitSchedule() {
  if (!scheduleForm.title.trim()) {
    message.warning('请填写标题')
    return
  }
  if (!scheduleForm.startTime) {
    message.warning('请选择开始时间')
    return
  }
  if (scheduleForm.startTime && scheduleForm.endTime && scheduleForm.startTime.isAfter(scheduleForm.endTime)) {
    message.warning('开始时间不能晚于结束时间')
    return
  }
  const collaboratorNames = staffOptions.value
    .filter((item) => scheduleForm.collaboratorIds.includes(item.value))
    .map((item) => item.label)
  const repeatCount = scheduleForm.recurring ? Math.max(1, Number(scheduleForm.recurrenceCount || 1)) : 1
  const repeatRule = scheduleForm.recurrenceRule
  const repeatInterval = Math.max(1, Number(scheduleForm.recurrenceInterval || 1))
  scheduleSaving.value = true
  try {
    if (editingScheduleId.value != null) {
      const updated = await updateOaTask(editingScheduleId.value, {
        title: scheduleForm.title.trim(),
        description: scheduleForm.description || undefined,
        startTime: dayjs(scheduleForm.startTime).format('YYYY-MM-DDTHH:mm:ss'),
        endTime: scheduleForm.endTime ? dayjs(scheduleForm.endTime).format('YYYY-MM-DDTHH:mm:ss') : undefined,
        priority: scheduleForm.priority,
        assigneeName: scheduleForm.assigneeName || undefined,
        calendarType: scheduleForm.calendarType,
        planCategory: scheduleForm.planCategory || undefined,
        urgency: scheduleForm.urgency,
        eventColor: scheduleForm.eventColor,
        collaboratorIds: scheduleForm.calendarType === 'COLLAB' ? scheduleForm.collaboratorIds : [],
        collaboratorNames: scheduleForm.calendarType === 'COLLAB' ? collaboratorNames : []
      })
      upsertCalendarTask(updated)
      scheduleOpen.value = false
      editingScheduleId.value = null
      message.success('日程已更新')
      return
    }
    const tasks: Promise<any>[] = []
    for (let i = 0; i < repeatCount; i += 1) {
      const startTime = i === 0 ? scheduleForm.startTime : addByRule(scheduleForm.startTime, repeatRule, repeatInterval * i)
      const endTime = scheduleForm.endTime ? (i === 0 ? scheduleForm.endTime : addByRule(scheduleForm.endTime, repeatRule, repeatInterval * i)) : undefined
      tasks.push(
        createOaTask({
          title: scheduleForm.title.trim(),
          description: scheduleForm.description || undefined,
          startTime: dayjs(startTime).format('YYYY-MM-DDTHH:mm:ss'),
          endTime: endTime ? dayjs(endTime).format('YYYY-MM-DDTHH:mm:ss') : undefined,
          priority: scheduleForm.priority,
          status: 'OPEN',
          assigneeName: scheduleForm.assigneeName || undefined,
          calendarType: scheduleForm.calendarType,
          planCategory: scheduleForm.planCategory || undefined,
          urgency: scheduleForm.urgency,
          eventColor: scheduleForm.eventColor,
          collaboratorIds: scheduleForm.calendarType === 'COLLAB' ? scheduleForm.collaboratorIds : [],
          collaboratorNames: scheduleForm.calendarType === 'COLLAB' ? collaboratorNames : [],
          recurring: scheduleForm.recurring,
          recurrenceRule: scheduleForm.recurring ? repeatRule : undefined,
          recurrenceInterval: scheduleForm.recurring ? repeatInterval : undefined,
          recurrenceCount: scheduleForm.recurring ? repeatCount : undefined
        })
      )
    }
    const createdRows = await Promise.all(tasks)
    createdRows.forEach((row) => {
      if (row?.id != null) upsertCalendarTask(row)
    })
    scheduleOpen.value = false
    editingScheduleId.value = null
    message.success(scheduleForm.recurring ? `已生成 ${tasks.length} 条周期日程` : '日程已新增')
  } finally {
    scheduleSaving.value = false
  }
}

function markScheduleDone(id: string | number) {
  Modal.confirm({
    title: '确认标记完成该日程？',
    onOk: async () => {
      const updated = await completeOaTask(id)
      if (updated?.id != null) {
        upsertCalendarTask(updated)
      } else {
        const target = calendarRows.value.find((item) => String(item.id) === String(id))
        if (target) {
          upsertCalendarTask({
            ...target,
            status: 'DONE'
          })
        }
      }
      message.success('已标记完成')
    }
  })
}

function removeSchedule(id: string | number) {
  Modal.confirm({
    title: '确认删除该日程？',
    okButtonProps: { danger: true },
    onOk: async () => {
      await deleteOaTask(id)
      removeCalendarTaskById(id)
      message.success('已删除')
    }
  })
}

async function loadStaffOptions() {
  try {
    const page: PageResult<StaffItem> = await getStaffPage({ pageNo: 1, pageSize: 300 })
    staffDeptMap.value = {}
    ;(page.list || []).forEach((item) => {
      staffDeptMap.value[String(item.id)] = item.departmentId == null ? undefined : String(item.departmentId)
    })
    staffOptions.value = (page.list || []).map((item) => ({
      label: item.realName || item.username || `员工#${item.id}`,
      value: String(item.id)
    }))
  } catch {}
}

async function loadDepartmentOptions() {
  try {
    const page: PageResult<DepartmentItem> = await getDepartmentPage({ pageNo: 1, pageSize: 200 })
    departmentOptions.value = (page.list || []).map((item) => ({
      label: item.deptName,
      value: String(item.id)
    }))
  } catch {}
}

async function loadSummary() {
  const data = await getPortalSummary()
  Object.assign(summary, data)
}

async function loadCalendar() {
  calendarLoading.value = true
  try {
    calendarRows.value = await getOaTaskCalendar({
      startDate: dayjs().startOf('month').format('YYYY-MM-DD'),
      endDate: dayjs().endOf('month').format('YYYY-MM-DD')
    })
  } finally {
    calendarLoading.value = false
  }
}

async function loadDashboard() {
  dashboard.value = await getDashboardSummary()
}

async function loadFinanceOverview() {
  financeOverview.value = await getFinanceWorkbenchOverview()
}

async function loadBedAndElderStatus() {
  const [residence, beds] = await Promise.all([getResidenceStatusSummary(), getBedMap()])
  bedAndElderStatus.inHospital = Number(residence.inHospitalCount || 0)
  bedAndElderStatus.outing = Number(residence.outingCount || 0)
  bedAndElderStatus.medicalObservation = Number(residence.medicalOutingCount || 0)
  bedAndElderStatus.emptyBeds = (beds || []).filter((bed) => resolveBedStatus(bed as BedItem) === '空闲').length
  bedAndElderStatus.cleaningBeds = (beds || []).filter((bed) => resolveBedStatus(bed as BedItem) === '清洁中').length
}

async function loadSalesFunnel() {
  const today = dayjs().format('YYYY-MM-DD')
  const monthStart = dayjs().startOf('month').format('YYYY-MM-DD')

  const [conversion, evalPage, pendingSignPage] = await Promise.all([
    getMarketingConversionReport({ dateFrom: monthStart, dateTo: today }),
    getLeadPage({ pageNo: 1, pageSize: 1, status: 1 }) as Promise<PageResult<CrmLeadItem>>,
    getContractPage({ pageNo: 1, pageSize: 1, flowStage: 'PENDING_SIGN' }) as Promise<PageResult<CrmContractItem>>
  ])

  salesFunnel.todayConsultCount = Number(conversion.consultCount || 0)
  salesFunnel.evaluationCount = Number(evalPage.total || 0)
  salesFunnel.pendingSignCount = Number(pendingSignPage.total || 0)
  salesFunnel.monthDealCount = Number(conversion.contractCount || 0)
}

async function loadHrReminder() {
  const [hrData, certPage] = await Promise.all([
    getHrWorkbenchSummary(),
    getHrProfileCertificateReminderPage({ pageNo: 1, pageSize: 1 })
  ])
  hrSummary.value = hrData
  certificateReminderCount.value = Number(certPage.total || 0)
}

async function init() {
  loading.value = true
  pageError.value = ''
  try {
    if (!searchIndex.value.length) {
      buildSearchIndex()
      loadRecentSearchRoutes()
    }
    updateSearchOptions(searchKeyword.value)
    await Promise.all([loadSummary(), loadCalendar(), loadStaffOptions(), loadDepartmentOptions()])
    await Promise.allSettled([
      loadDashboard(),
      loadFinanceOverview(),
      loadSalesFunnel(),
      loadBedAndElderStatus(),
      loadHrReminder()
    ])
    refreshedAt.value = dayjs().format('YYYY-MM-DD HH:mm:ss')
  } catch (error: any) {
    pageError.value = error?.message || '加载首页失败'
    message.error(pageError.value)
  } finally {
    loading.value = false
  }
}

useLiveSyncRefresh({
  topics: ['elder', 'bed', 'lifecycle', 'finance', 'care', 'health', 'dining', 'marketing', 'oa', 'hr'],
  refresh: () => {
    init()
  },
  debounceMs: 800
})

onMounted(init)
</script>

<style scoped>
.portal-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.hero-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.hero-title {
  font-size: 18px;
  font-weight: 700;
  color: #1f2937;
}

.hero-subtitle {
  margin-top: 4px;
  font-size: 13px;
  color: var(--muted);
}

.hero-search {
  margin-top: 10px;
}

.full-height {
  height: 100%;
}

.metric-cell {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 10px;
  cursor: pointer;
}

.metric-cell:hover {
  border-color: #91caff;
  background: #f0f7ff;
}

.metric-title {
  font-size: 12px;
  color: #64748b;
}

.metric-value {
  margin-top: 6px;
  font-size: 18px;
  font-weight: 700;
  color: #0f172a;
  word-break: break-all;
}

.block-label {
  font-size: 12px;
  color: #64748b;
}

.source-wrap {
  margin-top: 12px;
}

.legend-line {
  margin-top: 8px;
}

.reminder-count {
  min-width: 42px;
  text-align: right;
  color: #475569;
}

.quick-group {
  border: 1px dashed #dbeafe;
  border-radius: 8px;
  padding: 10px;
  height: 100%;
}

.quick-title {
  margin-bottom: 8px;
  font-size: 13px;
  font-weight: 600;
  color: #1e3a8a;
}

.funnel-chart {
  height: 210px;
  margin-top: 8px;
}

.expense-block {
  border: 1px solid #f1f5f9;
  border-radius: 8px;
  padding: 10px;
}

.expense-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 0;
  border-bottom: 1px dashed #e2e8f0;
  cursor: pointer;
}

.expense-row:last-child {
  border-bottom: none;
}

.expense-row strong {
  color: #0f172a;
}

.hint-text {
  color: #64748b;
  font-size: 12px;
}

.calendar-toolbar {
  margin-bottom: 10px;
}

.calendar-title-tip {
  color: #64748b;
  font-size: 12px;
  margin-bottom: 6px;
}

.legend-dot {
  width: 10px;
  height: 10px;
  display: inline-block;
  border-radius: 50%;
  margin-right: 4px;
}

.calendar-actions {
  margin-top: 8px;
}

.repeat-card {
  margin-bottom: 12px;
}

.agenda-item-done {
  opacity: 0.72;
}

.agenda-title-done {
  color: #64748b;
  text-decoration: line-through;
}

@media (max-width: 768px) {
  .hero-card {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
