<template>
  <PageContainer class="rm-page" kicker="MARKET CRM / 03" title="参观与预订" sub-title="参观预约、到访登记、床位意向与预订申请在同一工作区完成">
    <template #extra>
      <a-space wrap>
        <a-button @click="startReservation"><HomeOutlined />预订申请</a-button>
        <a-button type="primary" @click="openForm()"><PlusOutlined />新建参观预约</a-button>
      </a-space>
    </template>

    <a-tabs :active-key="tab" @change="switchTab">
      <a-tab-pane key="calendar" tab="参观日历" />
      <a-tab-pane key="reservation" tab="床位预订" />
    </a-tabs>

    <template v-if="tab === 'calendar'">
      <div class="rm-kpis">
        <div class="rm-kpi">
          <span class="rm-kpi__icon"><CalendarOutlined /></span>
          <span class="rm-kpi__label">本周预约参观</span>
          <span class="rm-kpi__value">{{ isCurrentWeek ? weekActive.length : '--' }}<small>组</small></span>
          <span class="rm-kpi__hint">已到访 {{ isCurrentWeek ? weekArrived : '--' }} 组</span>
        </div>
        <div class="rm-kpi">
          <span class="rm-kpi__icon rm-kpi__icon--info"><UserOutlined /></span>
          <span class="rm-kpi__label">今日预约</span>
          <span class="rm-kpi__value">{{ isCurrentWeek ? todayVisits.length : '--' }}<small>组</small></span>
          <span class="rm-kpi__hint">{{ todayPending }} 组尚未到访</span>
        </div>
        <button type="button" class="rm-kpi" @click="switchTab('reservation')">
          <span class="rm-kpi__icon"><HomeOutlined /></span>
          <span class="rm-kpi__label">有效床位预订</span>
          <span class="rm-kpi__value">{{ beds.length ? beds_.reserved : '--' }}<small>床</small></span>
          <span class="rm-kpi__hint">按床位系统数据校验</span>
        </button>
        <button type="button" class="rm-kpi" @click="switchTab('reservation', 'expiring_lock')">
          <span class="rm-kpi__icon rm-kpi__icon--warning"><ClockCircleOutlined /></span>
          <span class="rm-kpi__label">预订临期≤{{ EXPIRY_WINDOW_DAYS }}天</span>
          <span class="rm-kpi__value">{{ expiringCount }}<small>条</small></span>
          <span class="rm-kpi__hint">到期提醒负责人</span>
        </button>
      </div>

      <div class="rm-split">
        <div class="vc-main">
          <section class="rm-card">
            <div class="rm-card__head">
              <h3 class="rm-card__title">{{ calendarMode === 'week' ? '本周参观接待日历' : '本月参观接待' }}</h3>
              <span class="rm-card__sub">{{ rangeLabel }} · 共 {{ rangeActive.length }} 个预约</span>
              <a-space class="rm-card__extra" wrap>
                <a-button size="small" @click="shift(-1)"><LeftOutlined /></a-button>
                <a-button size="small" @click="resetRange">{{ calendarMode === 'week' ? '本周' : '本月' }}</a-button>
                <a-button size="small" @click="shift(1)"><RightOutlined /></a-button>
                <a-radio-group v-model:value="calendarMode" size="small" button-style="solid" @change="loadVisits">
                  <a-radio-button value="week">周视图</a-radio-button>
                  <a-radio-button value="month">月视图</a-radio-button>
                </a-radio-group>
              </a-space>
            </div>
            <a-spin :spinning="visitsLoading">
              <a-alert v-if="visitsError" type="error" show-icon :message="visitsError" style="margin-bottom: 12px" />
              <div v-if="calendarMode === 'week'" class="vc-week">
                <div v-for="day in days" :key="day.format('YYYY-MM-DD')" class="vc-day" :class="{ 'vc-day--today': day.isSame(today, 'day') }">
                  <div class="vc-day__head">
                    <span>{{ WEEKDAY[(day.day() + 6) % 7] }}</span>
                    <strong>{{ day.date() }}</strong>
                  </div>
                  <button
                    v-for="item in grouped[day.format('YYYY-MM-DD')] || []"
                    :key="String(item.id)"
                    type="button"
                    class="vc-event"
                    :class="`vc-event--${item.status}`"
                    @click="openDetail(item)"
                  >
                    <strong>{{ dayjs(item.appointmentTime).format('HH:mm') }} {{ customerName(item) }}</strong>
                    <span>{{ VISIT_STATUS[item.status]?.label || item.status }} · {{ item.partySize || 1 }}人</span>
                  </button>
                </div>
              </div>
              <a-table
                v-else
                size="small"
                row-key="id"
                :data-source="rangeVisits"
                :columns="monthColumns"
                :pagination="{ pageSize: 10, hideOnSinglePage: true }"
                :scroll="{ x: 'max-content' }"
                :custom-row="(record: CrmVisitAppointmentItem) => ({ onClick: () => openDetail(record), style: 'cursor:pointer' })"
              >
                <template #bodyCell="{ column, record }">
                  <template v-if="column.key === 'time'">{{ dayjs(record.appointmentTime).format('MM-DD ddd HH:mm') }}</template>
                  <template v-else-if="column.key === 'customer'">{{ customerName(record) }}</template>
                  <template v-else-if="column.key === 'status'"><span class="rm-pill" :class="pillClass(record.status)">{{ VISIT_STATUS[record.status]?.label }}</span></template>
                </template>
              </a-table>
              <div class="vc-legend">
                <span v-for="(meta, key) in VISIT_STATUS" :key="key"><i :class="`vc-dot vc-event--${key}`" />{{ meta.label }}</span>
              </div>
            </a-spin>
          </section>

          <section class="rm-card">
            <div class="rm-card__head">
              <h3 class="rm-card__title">最近参观记录与后续动作</h3>
              <span class="rm-card__sub">到访登记后填写结果，系统会同步客户的下次跟进日期</span>
            </div>
            <a-table size="small" row-key="id" :data-source="recent" :columns="recentColumns" :pagination="false" :loading="recentLoading" :scroll="{ x: 'max-content' }">
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'time'">{{ dayjs(record.appointmentTime).format('MM-DD HH:mm') }}</template>
                <template v-else-if="column.key === 'customer'">{{ customerName(record) }}<span class="rm-row__meta"> / {{ record.visitorName }}</span></template>
                <template v-else-if="column.key === 'status'"><span class="rm-pill" :class="pillClass(record.status)">{{ VISIT_STATUS[record.status as CrmVisitAppointmentStatus]?.label }}</span></template>
                <template v-else-if="column.key === 'next'">{{ visitNextStep(record) }}</template>
                <template v-else-if="column.key === 'action'"><button type="button" class="rm-link" @click="openDetail(record)">登记 / 查看</button></template>
              </template>
            </a-table>
          </section>
        </div>

        <aside class="vc-side">
          <section class="rm-card">
            <div class="rm-card__head">
              <h3 class="rm-card__title">实时床位可预订概览</h3>
              <span class="rm-pill rm-pill--success rm-card__extra">来自床位管理</span>
            </div>
            <template v-if="beds.length">
              <div class="rm-card__sub">机构总床位</div>
              <div class="vc-total">{{ beds_.total }}<small>床</small></div>
              <div class="vc-bar" role="img" :aria-label="`在住 ${beds_.occupied}，预订中 ${beds_.reserved}，可预订 ${beds_.available}`">
                <span class="vc-bar__occupied" :style="{ width: pct(beds_.occupied) }" />
                <span class="vc-bar__reserved" :style="{ width: pct(beds_.reserved) }" />
              </div>
              <div class="vc-stats">
                <div><span>已在住</span><strong>{{ beds_.occupied }}</strong></div>
                <div><span>预订中</span><strong class="vc-warn">{{ beds_.reserved }}</strong></div>
                <div><span>可预订</span><strong>{{ beds_.available }}</strong></div>
              </div>
            </template>
            <a-empty v-else :description="bedsError || '暂无床位数据'" />
            <p class="rm-card__sub vc-tip">实时床态由长者管理统一维护，市场部只能申请预订，不直接修改正式入住状态。</p>
          </section>

          <section class="rm-card">
            <div class="rm-card__head"><h3 class="rm-card__title">可预订房型</h3></div>
            <div v-for="item in roomTypes.slice(0, 5)" :key="`${item.building}-${item.roomType}`" class="rm-row">
              <span class="rm-avatar"><HomeOutlined /></span>
              <div class="rm-row__main">
                <span class="rm-row__title">{{ item.building }} · {{ item.roomType }}</span>
                <span class="rm-row__meta">空闲床位</span>
              </div>
              <strong class="vc-count">{{ item.count }}</strong>
            </div>
            <a-empty v-if="!roomTypes.length" description="暂无空闲床位" />
            <a-button block style="margin-top: 12px" @click="startReservation">发起床位预订申请</a-button>
          </section>

          <section class="rm-card">
            <div class="rm-card__head">
              <h3 class="rm-card__title">预订到期提醒</h3>
              <button type="button" class="rm-link rm-card__extra" @click="switchTab('reservation', 'expiring_lock')">全部</button>
            </div>
            <div v-for="item in reminders.slice(0, 5)" :key="String(item.lead.id)" class="rm-row">
              <span class="rm-avatar"><ClockCircleOutlined /></span>
              <div class="rm-row__main">
                <span class="rm-row__title">{{ item.lead.elderName || item.lead.name }}<template v-if="item.bedLabel"> · {{ item.bedLabel }}</template></span>
                <span class="rm-row__meta">{{ item.daysLeft < 0 ? `已过预订截止 ${-item.daysLeft} 天` : `距离预订截止还剩 ${item.daysLeft} 天` }}（{{ item.deadline }}）</span>
              </div>
              <span class="rm-pill" :class="item.daysLeft < 0 ? 'rm-pill--danger' : isExpiringSoon(item) ? 'rm-pill--warning' : ''">
                {{ item.daysLeft < 0 ? '已过期' : isExpiringSoon(item) ? '即将到期' : '有效' }}
              </span>
            </div>
            <a-empty v-if="!reminders.length" description="暂无需要提醒的预订" />
          </section>
        </aside>
      </div>
    </template>

    <template v-else>
      <a-radio-group :value="scenario" button-style="solid" style="margin-bottom: 12px" @change="(e: any) => switchTab('reservation', e.target.value)">
        <a-radio-button value="">全部预订</a-radio-button>
        <a-radio-button value="lock_bed">锁床</a-radio-button>
        <a-radio-button value="expiring_lock">即将到期</a-radio-button>
      </a-radio-group>
      <EmbeddedPage>
        <LeadPipelineTable :key="scenario" mode="reservation" title="床位预订" sub-title="已预订客户与签约前准备" :scenario="scenario" />
      </EmbeddedPage>
    </template>

    <a-drawer v-model:open="detailOpen" title="参观预约" width="440">
      <template v-if="active">
        <a-descriptions :column="1" size="small" bordered>
          <a-descriptions-item label="客户">{{ customerName(active) }}</a-descriptions-item>
          <a-descriptions-item label="来访联系人">{{ active.visitorName }} · {{ active.visitorPhone || '无电话' }}</a-descriptions-item>
          <a-descriptions-item label="预约时间">{{ dayjs(active.appointmentTime).format('YYYY-MM-DD HH:mm') }}</a-descriptions-item>
          <a-descriptions-item label="来访人数">{{ active.partySize || 1 }} 人</a-descriptions-item>
          <a-descriptions-item label="接待负责人">{{ active.ownerStaffName || '-' }}</a-descriptions-item>
          <a-descriptions-item label="状态"><span class="rm-pill" :class="pillClass(active.status)">{{ VISIT_STATUS[active.status]?.label }}</span></a-descriptions-item>
          <a-descriptions-item v-if="active.visitResult || active.cancelReason" label="结果/原因">{{ active.visitResult || active.cancelReason }}</a-descriptions-item>
          <a-descriptions-item v-if="active.nextFollowDate" label="下次跟进">{{ active.nextFollowDate }}</a-descriptions-item>
          <a-descriptions-item v-if="active.remark" label="特殊接待要求">{{ active.remark }}</a-descriptions-item>
        </a-descriptions>
        <p class="rm-row__meta vc-tip">下一步：{{ visitNextStep(active) }}</p>
      </template>
      <template #footer>
        <a-space v-if="active" wrap>
          <a-button v-if="active.status === 'SCHEDULED'" @click="openForm(active)">编辑</a-button>
          <a-button v-if="canCancel(active)" danger @click="openCancel(active)">取消预约</a-button>
          <a-button v-if="active.status === 'SCHEDULED'" type="primary" @click="arrive(active)">登记到访</a-button>
          <a-button v-if="active.status === 'ARRIVED'" type="primary" @click="openComplete(active)">填写参观结果</a-button>
        </a-space>
      </template>
    </a-drawer>

    <a-drawer v-model:open="formOpen" :title="form.id ? '编辑参观预约' : '新建参观预约'" width="520" :mask-closable="false">
      <a-form ref="formRef" layout="vertical" :model="form" :rules="formRules">
        <a-form-item label="客户线索" name="leadId">
          <a-select v-model:value="form.leadId" show-search :filter-option="filterLeadOption" :options="leadSelectOptions" placeholder="输入姓名或电话选择客户" @change="fillFromLead" />
        </a-form-item>
        <a-form-item label="预约参观时间" name="appointmentTime">
          <a-date-picker v-model:value="form.appointmentTime" show-time format="YYYY-MM-DD HH:mm" style="width: 100%" />
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="12"><a-form-item label="来访联系人" name="visitorName"><a-input v-model:value="form.visitorName" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="联系电话"><a-input v-model:value="form.visitorPhone" /></a-form-item></a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12"><a-form-item label="来访人数"><a-input-number v-model:value="form.partySize" :min="1" :max="20" style="width: 100%" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="接待负责人"><a-input v-model:value="form.ownerStaffName" placeholder="默认取客户负责人" /></a-form-item></a-col>
        </a-row>
        <a-form-item label="参观区域与特殊接待要求"><a-textarea v-model:value="form.remark" :rows="3" placeholder="例如行动不便、需要无障碍路线、重点关注房型或护理区" /></a-form-item>
      </a-form>
      <template #footer>
        <a-space><a-button @click="formOpen = false">取消</a-button><a-button type="primary" :loading="saving" @click="submitForm">保存预约</a-button></a-space>
      </template>
    </a-drawer>

    <a-modal v-model:open="completeOpen" title="填写参观结果" :mask-closable="false" :confirm-loading="saving" @ok="submitComplete">
      <a-form layout="vertical" :model="completeForm">
        <a-form-item label="参观结果" required>
          <a-textarea v-model:value="completeForm.visitResult" :rows="4" placeholder="记录实际到访人数、客户关注点、意向变化和需要协助的事项" />
        </a-form-item>
        <a-form-item label="下次跟进日期"><a-date-picker v-model:value="completeForm.nextFollowDate" format="YYYY-MM-DD" style="width: 100%" /></a-form-item>
        <a-form-item label="补充备注"><a-textarea v-model:value="completeForm.remark" :rows="2" /></a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="cancelOpen" title="取消参观预约" :mask-closable="false" :confirm-loading="saving" @ok="submitCancel">
      <a-form layout="vertical">
        <a-form-item label="取消原因" required><a-textarea v-model:value="cancelReason" :rows="3" placeholder="请填写客户取消或改期原因" /></a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import dayjs, { type Dayjs } from 'dayjs'
import type { FormInstance } from 'ant-design-vue'
import { message, Modal } from 'ant-design-vue'
import { CalendarOutlined, ClockCircleOutlined, HomeOutlined, LeftOutlined, PlusOutlined, RightOutlined, UserOutlined } from '@ant-design/icons-vue'
import PageContainer from '../../../components/PageContainer.vue'
import EmbeddedPage from './EmbeddedPage.vue'
import LeadPipelineTable from '../components/LeadPipelineTable.vue'
import {
  arriveVisitAppointment, cancelVisitAppointment, completeVisitAppointment, createVisitAppointment,
  getLeadPage, getVisitAppointmentPage, updateVisitAppointment
} from '../../../api/marketing'
import { getCrmLead } from '../../../api/crm'
import { getBedList } from '../../../api/bed'
import type { BedItem } from '../../../types/bed'
import type { CrmLeadItem, CrmVisitAppointmentItem, CrmVisitAppointmentStatus, PageResult } from '../../../types'
import {
  EXPIRY_WINDOW_DAYS, VISIT_STATUS, availableRoomTypes, bedSummary, groupByDay, isExpiringSoon,
  reservationReminders, visitNextStep, weekDays, weekStart
} from './visitModel'
import '../../elder/residentUi.css'

const WEEKDAY = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
const route = useRoute()
const router = useRouter()
const today = dayjs()

const tab = computed(() => (route.query.tab === 'reservation' ? 'reservation' : 'calendar'))
const scenario = computed(() => (['lock_bed', 'expiring_lock'].includes(String(route.query.scenario)) ? String(route.query.scenario) : ''))

function switchTab(key: string | number, nextScenario = '') {
  const { scenario: _s, tab: _t, action: _a, leadId: _l, ...rest } = route.query
  router.replace({ query: { ...rest, ...(key === 'reservation' ? { tab: 'reservation' } : {}), ...(nextScenario ? { scenario: nextScenario } : {}) } })
}

function startReservation() {
  // 预订必须挂在已有客户上：到意向客户列表选中客户后「转预订」（会校验床位并锁床）
  router.push({ path: '/marketing/customers', query: { tab: 'intent' } })
  message.info('请在意向客户中选择客户，点「转预订」发起床位预订申请')
}

// —— 日历 ——
const calendarMode = ref<'week' | 'month'>('week')
const anchor = ref<Dayjs>(weekStart())
const rangeStart = computed(() => (calendarMode.value === 'week' ? weekStart(anchor.value) : anchor.value.startOf('month')))
const rangeEnd = computed(() => (calendarMode.value === 'week' ? rangeStart.value.add(6, 'day') : anchor.value.endOf('month')))
const days = computed(() => weekDays(rangeStart.value))
const rangeLabel = computed(() => calendarMode.value === 'week'
  ? `${rangeStart.value.format('YYYY年M月D日')}—${rangeEnd.value.format('M月D日')}`
  : rangeStart.value.format('YYYY年M月'))
const isCurrentWeek = computed(() => calendarMode.value === 'week' && rangeStart.value.isSame(weekStart(), 'day'))

const visitsLoading = ref(false)
const visitsError = ref('')
const rangeVisits = ref<CrmVisitAppointmentItem[]>([])
const grouped = computed(() => groupByDay(rangeVisits.value))
const rangeActive = computed(() => rangeVisits.value.filter((v) => v.status !== 'CANCELLED'))
const weekActive = rangeActive
const weekArrived = computed(() => rangeVisits.value.filter((v) => v.status === 'ARRIVED' || v.status === 'COMPLETED').length)
const todayVisits = computed(() => rangeActive.value.filter((v) => dayjs(v.appointmentTime).isSame(today, 'day')))
const todayPending = computed(() => (isCurrentWeek.value ? todayVisits.value.filter((v) => v.status === 'SCHEDULED').length : '--'))

function shift(step: number) {
  anchor.value = anchor.value.add(step, calendarMode.value === 'week' ? 'week' : 'month')
  loadVisits()
}
function resetRange() { anchor.value = dayjs(); loadVisits() }

async function loadVisits() {
  visitsLoading.value = true
  visitsError.value = ''
  try {
    const page: PageResult<CrmVisitAppointmentItem> = await getVisitAppointmentPage({
      pageNo: 1, pageSize: 200, dateFrom: rangeStart.value.format('YYYY-MM-DD'), dateTo: rangeEnd.value.format('YYYY-MM-DD')
    })
    rangeVisits.value = page.list || []
  } catch (error: any) {
    rangeVisits.value = []
    visitsError.value = error?.message || '参观预约加载失败'
  } finally {
    visitsLoading.value = false
  }
}

const monthColumns = [
  { title: '预约时间', key: 'time' },
  { title: '客户', key: 'customer' },
  { title: '来访人数', dataIndex: 'partySize' },
  { title: '接待人', dataIndex: 'ownerStaffName' },
  { title: '状态', key: 'status' }
]

// —— 最近记录 ——
const recent = ref<CrmVisitAppointmentItem[]>([])
const recentLoading = ref(false)
const recentColumns = [
  { title: '接待时间', key: 'time' },
  { title: '客户 / 家属', key: 'customer' },
  { title: '状态', key: 'status' },
  { title: '接待人', dataIndex: 'ownerStaffName' },
  { title: '下一步', key: 'next' },
  { title: '操作', key: 'action' }
]
async function loadRecent() {
  recentLoading.value = true
  try {
    const page: PageResult<CrmVisitAppointmentItem> = await getVisitAppointmentPage({ pageNo: 1, pageSize: 8 })
    recent.value = page.list || []
  } catch {
    recent.value = []
  } finally {
    recentLoading.value = false
  }
}

// —— 床位与预订提醒 ——
const beds = ref<BedItem[]>([])
const bedsError = ref('')
const reservationLeads = ref<CrmLeadItem[]>([])
const beds_ = computed(() => bedSummary(beds.value))
const roomTypes = computed(() => availableRoomTypes(beds.value))
const reminders = computed(() => reservationReminders(reservationLeads.value, beds.value))
const expiringCount = computed(() => reminders.value.filter((r) => r.daysLeft <= EXPIRY_WINDOW_DAYS).length)
const pct = (n: number) => `${beds_.value.total ? (n / beds_.value.total) * 100 : 0}%`

async function loadSide() {
  const [bedList, leadPage] = await Promise.allSettled([
    getBedList({ silent403: true }),
    getLeadPage({ pageNo: 1, pageSize: 200, mode: 'reservation' }, { silent403: true })
  ])
  beds.value = bedList.status === 'fulfilled' ? bedList.value || [] : []
  bedsError.value = bedList.status === 'rejected' ? '当前账号无权查看床位数据' : ''
  reservationLeads.value = leadPage.status === 'fulfilled' ? leadPage.value.list || [] : []
}

// —— 预约详情与办理 ——
const detailOpen = ref(false)
const active = ref<CrmVisitAppointmentItem>()
function openDetail(item: CrmVisitAppointmentItem) { active.value = item; detailOpen.value = true }
function customerName(item: CrmVisitAppointmentItem) { return item.elderName || item.leadName || item.visitorName || '-' }
function pillClass(status: string) { const tone = VISIT_STATUS[status as CrmVisitAppointmentStatus]?.tone; return tone ? `rm-pill--${tone}` : '' }
function canCancel(item?: CrmVisitAppointmentItem) { return !!item && (item.status === 'SCHEDULED' || item.status === 'ARRIVED') }

function refresh() { detailOpen.value = false; loadVisits(); loadRecent() }

function arrive(item: CrmVisitAppointmentItem) {
  Modal.confirm({
    title: `确认 ${item.visitorName} 已到访？`,
    content: '登记后即可填写参观结果。',
    onOk: async () => { await arriveVisitAppointment(item.id); message.success('已登记到访'); refresh() }
  })
}

const completeOpen = ref(false)
const completeForm = reactive({ visitResult: '', nextFollowDate: null as Dayjs | null, remark: '' })
function openComplete(item: CrmVisitAppointmentItem) {
  active.value = item
  Object.assign(completeForm, { visitResult: item.visitResult || '', nextFollowDate: dayjs().add(1, 'day'), remark: '' })
  completeOpen.value = true
}
async function submitComplete() {
  if (!active.value || !completeForm.visitResult.trim()) { message.warning('请填写参观结果'); return }
  saving.value = true
  try {
    await completeVisitAppointment(active.value.id, {
      visitResult: completeForm.visitResult.trim(),
      nextFollowDate: completeForm.nextFollowDate?.format('YYYY-MM-DD'),
      remark: completeForm.remark || undefined
    })
    message.success('参观结果已保存，下次跟进已同步到客户')
    completeOpen.value = false
    refresh()
  } finally { saving.value = false }
}

const cancelOpen = ref(false)
const cancelReason = ref('')
function openCancel(item: CrmVisitAppointmentItem) { active.value = item; cancelReason.value = ''; cancelOpen.value = true }
async function submitCancel() {
  if (!active.value || !cancelReason.value.trim()) { message.warning('请填写取消原因'); return }
  saving.value = true
  try {
    await cancelVisitAppointment(active.value.id, cancelReason.value.trim())
    message.success('预约已取消并保留原因')
    cancelOpen.value = false
    refresh()
  } finally { saving.value = false }
}

// —— 新建 / 编辑预约（沿用原「客户预约参观」页的表单与校验） ——
const saving = ref(false)
const formOpen = ref(false)
const formRef = ref<FormInstance>()
const leadOptions = ref<CrmLeadItem[]>([])
const form = reactive({
  id: undefined as string | number | undefined,
  leadId: undefined as string | undefined,
  appointmentTime: null as Dayjs | null,
  visitorName: '', visitorPhone: '', partySize: 1, ownerStaffName: '', remark: ''
})
const formRules = {
  leadId: [{ required: true, message: '请选择客户线索' }],
  appointmentTime: [{ required: true, message: '请选择预约参观时间' }],
  visitorName: [{ required: true, message: '请填写来访联系人' }]
}
const leadLabel = (lead: CrmLeadItem) => `${lead.elderName || lead.name || lead.consultantName} · ${lead.elderPhone || lead.phone || lead.consultantPhone || '无电话'}`
const leadSelectOptions = computed(() => leadOptions.value.map((lead) => ({ value: String(lead.id), label: leadLabel(lead) })))
function filterLeadOption(input: string, option: any) { return String(option?.label || '').toLowerCase().includes(input.toLowerCase()) }

async function loadLeads() {
  try {
    const page: PageResult<CrmLeadItem> = await getLeadPage({ pageNo: 1, pageSize: 100 }, { silent403: true })
    leadOptions.value = page.list || []
  } catch { leadOptions.value = [] }
}

function openForm(item?: CrmVisitAppointmentItem, leadId?: string) {
  formRef.value?.clearValidate()
  Object.assign(form, item ? {
    id: item.id, leadId: String(item.leadId), appointmentTime: dayjs(item.appointmentTime), visitorName: item.visitorName,
    visitorPhone: item.visitorPhone || '', partySize: item.partySize || 1, ownerStaffName: item.ownerStaffName || '', remark: item.remark || ''
  } : {
    id: undefined, leadId, appointmentTime: dayjs().add(1, 'day').hour(9).minute(0).second(0),
    visitorName: '', visitorPhone: '', partySize: 1, ownerStaffName: '', remark: ''
  })
  if (leadId) fillFromLead(leadId)
  detailOpen.value = false
  formOpen.value = true
}

function fillFromLead(value: string) {
  const lead = leadOptions.value.find((item) => String(item.id) === String(value))
  if (!lead) return
  form.visitorName = lead.consultantName || lead.elderName || lead.name || ''
  form.visitorPhone = lead.consultantPhone || lead.elderPhone || lead.phone || ''
  form.ownerStaffName = lead.ownerStaffName || lead.marketerName || ''
}

async function submitForm() {
  await formRef.value?.validate()
  if (!form.leadId || !form.appointmentTime) return
  saving.value = true
  try {
    const payload = {
      leadId: form.leadId, appointmentTime: form.appointmentTime.format('YYYY-MM-DD HH:mm:ss'), visitorName: form.visitorName,
      visitorPhone: form.visitorPhone || undefined, partySize: form.partySize, ownerStaffName: form.ownerStaffName || undefined, remark: form.remark || undefined
    }
    if (form.id) await updateVisitAppointment(form.id, payload)
    else await createVisitAppointment(payload)
    message.success(form.id ? '预约已更新' : '预约已创建，并已同步客户下次跟进日期')
    formOpen.value = false
    refresh()
  } finally { saving.value = false }
}

/** 从客户 360 抽屉跳转：?leadId=xx&action=create 直接打开新建并预选客户 */
async function applyRouteAction() {
  if (route.query.action !== 'create') return
  const leadId = route.query.leadId ? String(route.query.leadId) : undefined
  if (leadId && !leadOptions.value.some((lead) => String(lead.id) === leadId)) {
    const lead = await getCrmLead(leadId).catch(() => undefined)
    if (lead) leadOptions.value = [lead, ...leadOptions.value]
  }
  openForm(undefined, leadId)
  const { action: _a, leadId: _l, ...rest } = route.query
  router.replace({ query: rest })
}

watch(() => route.query.action, () => applyRouteAction())

onMounted(async () => {
  loadVisits(); loadRecent(); loadSide()
  await loadLeads()
  applyRouteAction()
})
</script>

<style scoped>
.vc-main, .vc-side { display: flex; flex-direction: column; gap: 16px; min-width: 0; }
.vc-week { display: grid; grid-template-columns: repeat(7, minmax(110px, 1fr)); overflow-x: auto; border: 1px solid var(--rm-border); border-radius: 10px; }
.vc-day { display: flex; flex-direction: column; gap: 6px; min-height: 260px; padding: 0 6px 8px; border-right: 1px solid var(--rm-border); }
.vc-day:last-child { border-right: 0; }
.vc-day__head { display: flex; flex-direction: column; align-items: center; gap: 2px; margin: 0 -6px 4px; padding: 10px 0; border-bottom: 1px solid var(--rm-border); background: #fafcfb; color: var(--rm-muted); font-size: 12px; }
.vc-day__head strong { font-size: 20px; color: var(--rm-text); }
.vc-day--today .vc-day__head { background: var(--rm-green-soft); }
.vc-day--today .vc-day__head strong { display: grid; place-items: center; width: 32px; height: 32px; border-radius: 50%; background: var(--rm-green); color: #fff; font-size: 16px; }
.vc-event { display: flex; flex-direction: column; gap: 2px; padding: 6px 8px; border: 1px solid; border-left-width: 3px; border-radius: 6px; font: inherit; font-size: 12px; text-align: left; cursor: pointer; }
.vc-event strong { font-weight: 600; }
.vc-event span { opacity: 0.8; }
.vc-event--SCHEDULED { border-color: #b7cdf3; background: #f3f7fe; color: var(--rm-info); }
.vc-event--ARRIVED { border-color: #f5d9a3; background: #fff8eb; color: #a16207; }
.vc-event--COMPLETED { border-color: #bfe0d2; background: #f0f8f4; color: var(--rm-green-dark); }
.vc-event--CANCELLED { border-color: #e3e6e5; background: #f6f7f7; color: #8c9894; text-decoration: line-through; }
.vc-event--NO_SHOW { border-color: #f3c1c1; background: #fff4f4; color: var(--rm-danger); }
.vc-legend { display: flex; flex-wrap: wrap; gap: 14px; margin-top: 10px; color: var(--rm-muted); font-size: 12px; }
.vc-dot { display: inline-block; width: 10px; height: 10px; margin-right: 4px; border: 1px solid; border-radius: 3px; vertical-align: -1px; }
.vc-total { margin: 4px 0 10px; font-size: 30px; font-weight: 600; color: var(--rm-green-dark); }
.vc-total small { margin-left: 4px; font-size: 13px; font-weight: 400; color: var(--rm-muted); }
.vc-bar { display: flex; height: 8px; overflow: hidden; border-radius: 999px; background: #eef2f0; }
.vc-bar__occupied { background: var(--rm-green); }
.vc-bar__reserved { background: #e2b46b; }
.vc-stats { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; margin-top: 12px; }
.vc-stats div { display: flex; flex-direction: column; gap: 4px; padding: 10px; border: 1px solid var(--rm-border); border-radius: 8px; color: var(--rm-muted); font-size: 12px; }
.vc-stats strong { font-size: 20px; color: var(--rm-green-dark); }
.vc-stats .vc-warn { color: var(--rm-warning); }
.vc-count { font-size: 18px; color: var(--rm-green-dark); }
.vc-tip { margin: 12px 0 0; }
</style>
