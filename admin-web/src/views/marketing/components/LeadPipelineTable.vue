<template>
  <PageContainer :title="title" :sub-title="subTitle">
    <section class="card-elevated marketing-workspace">
      <div class="marketing-overview">
        <div class="marketing-overview-copy">
          <span class="marketing-overview-kicker">营销工作区</span>
          <strong>{{ modeLabel }}工作区</strong>
          <span>{{ workspaceSummary }}</span>
        </div>
        <div class="marketing-overview-meta">
          <span class="overview-pill">当前列表 {{ displayTotal }} 条</span>
          <span class="overview-pill">{{ activeFilterSummary }}</span>
          <span class="overview-pill overview-pill--accent">已勾选 {{ selectedCount }} 条</span>
        </div>
      </div>

      <StatefulBlock :loading="summaryLoading" :error="summaryError" :empty="false" @retry="fetchData">
        <div class="summary-strip">
          <div v-for="card in summaryCards" :key="card.title" class="summary-strip-card">
            <span class="summary-card-label">{{ card.title }}</span>
            <strong class="summary-card-value">{{ card.value || 0 }}</strong>
          </div>
        </div>
        <div v-if="warningMessage" class="marketing-warning">
          <span class="marketing-warning-dot"></span>
          <span>{{ warningMessage }}</span>
        </div>
      </StatefulBlock>

      <div class="marketing-query-shell">
        <SearchForm
          :model="query"
          title="筛选条件"
          description="搜索、视图切换和表格动作集中在同一块工作区里处理，减少上下跳读。"
          search-text="刷新列表"
          reset-text="重置条件"
          @search="onSearch"
          @reset="reset"
        >
          <a-form-item v-if="props.mode === 'pipeline'" label="线索视图">
            <a-radio-group v-model:value="pipelineTab" button-style="solid" @change="onPipelineTabChange">
              <a-radio-button value="consultation">咨询</a-radio-button>
              <a-radio-button value="intent">意向</a-radio-button>
              <a-radio-button value="callback">待回访</a-radio-button>
            </a-radio-group>
          </a-form-item>

          <template v-if="effectiveMode === 'consultation'">
            <a-form-item label="咨询人姓名">
              <a-input v-model:value="query.consultantName" placeholder="请输入咨询人姓名" allow-clear />
            </a-form-item>
            <a-form-item label="联系电话">
              <a-input v-model:value="query.consultantPhone" placeholder="请输入联系电话" allow-clear />
            </a-form-item>
            <a-form-item label="老人姓名">
              <a-input v-model:value="query.elderName" placeholder="请输入老人姓名" allow-clear />
            </a-form-item>
            <a-form-item label="老人联系电话">
              <a-input v-model:value="query.elderPhone" placeholder="请输入老人联系电话" allow-clear />
            </a-form-item>
            <a-form-item label="咨询日期">
              <a-range-picker
                v-model:value="query.consultDateRange"
                value-format="YYYY-MM-DD"
                :placeholder="['开始日期', '结束日期']"
              />
            </a-form-item>
            <a-form-item label="咨询类型">
              <a-input v-model:value="query.consultType" placeholder="咨询类型" allow-clear />
            </a-form-item>
            <a-form-item label="媒体渠道">
              <a-input v-model:value="query.mediaChannel" placeholder="媒体渠道" allow-clear />
            </a-form-item>
          </template>

          <template v-else-if="effectiveMode === 'intent' || effectiveMode === 'invalid'">
            <a-form-item label="老人姓名">
              <a-input v-model:value="query.elderName" placeholder="请输入老人姓名" allow-clear />
            </a-form-item>
            <a-form-item label="老人联系电话">
              <a-input v-model:value="query.elderPhone" placeholder="请输入老人联系电话" allow-clear />
            </a-form-item>
            <a-form-item label="信息来源">
              <a-input v-model:value="query.infoSource" placeholder="请输入信息来源" allow-clear />
            </a-form-item>
            <a-form-item label="客户标签">
              <a-input v-model:value="query.customerTag" placeholder="请输入客户标签" allow-clear />
            </a-form-item>
            <a-form-item label="负责人">
              <a-input v-model:value="query.marketerName" placeholder="请输入负责人" allow-clear />
            </a-form-item>
          </template>

          <template v-else-if="effectiveMode === 'reservation'">
            <a-form-item label="姓名">
              <a-input v-model:value="query.elderName" placeholder="请输入姓名" allow-clear />
            </a-form-item>
            <a-form-item label="联系电话">
              <a-input v-model:value="query.elderPhone" placeholder="请输入联系电话" allow-clear />
            </a-form-item>
          </template>

          <template v-else>
            <a-form-item label="客户姓名">
              <a-input v-model:value="query.elderName" placeholder="请输入客户姓名" allow-clear />
            </a-form-item>
            <a-form-item label="联系电话">
              <a-input v-model:value="query.elderPhone" placeholder="请输入联系电话" allow-clear />
            </a-form-item>
            <a-form-item label="负责人">
              <a-input v-model:value="query.marketerName" placeholder="请输入负责人" allow-clear />
            </a-form-item>
          </template>
        </SearchForm>
      </div>

      <div class="table-head marketing-table-head">
        <div>
          <strong>{{ modeLabel }}列表</strong>
          <span>筛选、批量动作和单条推进都在这张表里完成，减少来回切页。</span>
        </div>
        <a-space wrap>
          <a-tag color="default">共 {{ displayTotal }} 条</a-tag>
          <a-tag color="blue">当前池 {{ currentSummary.modeCount || 0 }}</a-tag>
          <a-tag color="purple">已勾选 {{ selectedCount }} 条</a-tag>
        </a-space>
      </div>
      <MarketingListToolbar
        :selected-count="selectedCount"
        :tip="selectedCount > 0 ? '批量动作只会处理需要变更的记录。' : '可勾选多条后批量推进，也可在表格右侧逐条处理。'"
      >
        <a-space>
          <a-button
            v-for="item in actionButtons"
            :key="item.key"
            :type="item.type"
            :danger="item.danger"
            :disabled="item.disabled"
            @click="handleTopAction(item.key)"
          >
            {{ item.label }}
          </a-button>
        </a-space>
      </MarketingListToolbar>
      <StatefulBlock
        :loading="loading"
        :error="tableError"
        :empty="!loading && !tableError && displayRows.length === 0"
        empty-text="暂无匹配的客户数据"
        @retry="fetchData"
      >
        <a-table
          :data-source="displayRows"
          :columns="columns"
          :loading="loading"
          :pagination="false"
          row-key="id"
          :row-selection="rowSelection"
          :scroll="{ x: tableScrollX }"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'consultantName' || column.key === 'elderName'">
              <div class="person-cell">
                <strong>{{ record[column.dataIndex] || '-' }}</strong>
                <span>
                  {{
                    column.key === 'consultantName'
                      ? (record.consultantPhone || record.elderPhone || '暂无联系电话')
                      : (record.elderPhone || record.consultantPhone || '暂无联系电话')
                  }}
                </span>
              </div>
            </template>
            <template v-else-if="column.key === 'gender'">
              {{ genderText(record.gender) }}
            </template>
            <template v-else-if="column.key === 'refunded'">
              {{ record.refunded === 1 ? '是' : '否' }}
            </template>
            <template v-else-if="column.key === 'reservationAmount'">
              {{ record.reservationAmount == null ? '-' : `¥${Number(record.reservationAmount).toFixed(2)}` }}
            </template>
            <template v-else-if="column.key === 'ownerDisplayName'">
              <a-tag color="geekblue">{{ record.ownerDisplayName }}</a-tag>
            </template>
            <template v-else-if="column.key === 'infoSource'">
              <a-tag :color="record.infoSource || record.source ? 'cyan' : 'default'">{{ infoSourceLabel(record.infoSource || record.source) }}</a-tag>
            </template>
            <template v-else-if="column.key === 'followupStatus'">
              <a-tag :color="followupStatusColor(record.followupStatus)">{{ record.followupStatus || '未设置' }}</a-tag>
            </template>
            <template v-else-if="column.key === 'reservationStatus'">
              <a-tag :color="reservationStatusColor(record)">{{ reservationStatusLabel(record) }}</a-tag>
            </template>
            <template v-else-if="column.key === 'planTitle'">
              <div class="plan-cell">
                <strong>{{ record.planTitle || '-' }}</strong>
                <span>{{ callbackTypeLabel(record) }}</span>
              </div>
            </template>
            <template v-else-if="column.key === 'action'">
              <div class="row-action-links">
                <a-button
                  v-for="item in buildRowActions(record)"
                  :key="`${record.id}-${item.key}`"
                  type="link"
                  size="small"
                  :danger="item.danger"
                  @click="handleRowAction(item.key, record)"
                >
                  {{ item.label }}
                </a-button>
              </div>
            </template>
          </template>
        </a-table>
        <a-pagination
          class="marketing-pagination"
          :current="query.pageNo"
          :page-size="query.pageSize"
          :total="displayTotal"
          show-size-changer
          @change="onPageChange"
          @showSizeChange="onPageSizeChange"
        />
      </StatefulBlock>
    </section>

    <a-modal v-model:open="open" :title="modalTitle" width="820px" :confirm-loading="submitting" @ok="submit">
      <a-form ref="formRef" :model="form" :rules="rules" layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="咨询人姓名" name="consultantName">
              <a-input v-model:value="form.consultantName" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="联系电话" name="consultantPhone">
              <a-input v-model:value="form.consultantPhone" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="老人姓名" name="elderName">
              <a-input v-model:value="form.elderName" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="老人联系电话" name="elderPhone">
              <a-input v-model:value="form.elderPhone" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="性别">
              <a-select v-model:value="form.gender" allow-clear>
                <a-select-option :value="1">男</a-select-option>
                <a-select-option :value="2">女</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="年龄">
              <a-input-number v-model:value="form.age" :min="0" :max="120" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="咨询日期">
              <a-date-picker v-model:value="form.consultDate" value-format="YYYY-MM-DD" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="咨询类型">
              <a-input v-model:value="form.consultType" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="媒体渠道">
              <a-input v-model:value="form.mediaChannel" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="信息来源">
              <a-input v-model:value="form.infoSource" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="接待人">
              <a-input v-model:value="form.receptionistName" />
            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item label="家庭地址">
              <a-input v-model:value="form.homeAddress" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="负责人">
              <a-input v-model:value="form.marketerName" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="客户标签">
              <a-input v-model:value="form.customerTag" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="预订房号">
              <a-input v-model:value="form.reservationRoomNo" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="预订金额">
              <a-input-number v-model:value="form.reservationAmount" :min="0" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="收款时间">
              <a-date-picker v-model:value="form.paymentTime" value-format="YYYY-MM-DD HH:mm:ss" show-time style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="预约渠道">
              <a-input v-model:value="form.reservationChannel" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="计划执行时间">
              <a-date-picker v-model:value="form.nextFollowDate" value-format="YYYY-MM-DD" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="回访状态">
              <a-input v-model:value="form.followupStatus" placeholder="已回访/待回访" />
            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item label="备注">
              <a-textarea v-model:value="form.remark" :rows="2" />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-modal>

    <a-modal v-model:open="planOpen" title="新增回访计划" :confirm-loading="planSubmitting" @ok="submitCallbackPlan">
      <a-form :model="planForm" layout="vertical">
        <a-form-item label="计划标题">
          <a-input v-model:value="planForm.title" />
        </a-form-item>
        <a-form-item label="回访类型">
          <a-select v-model:value="planForm.callbackType">
            <a-select-option
              v-for="item in MARKETING_CALLBACK_TYPE_OPTIONS"
              :key="item.value"
              :value="item.value"
            >
              {{ item.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="回访内容">
          <a-textarea v-model:value="planForm.followupContent" :rows="3" placeholder="填写本次回访重点内容" />
        </a-form-item>
        <a-form-item label="计划执行时间">
          <a-date-picker v-model:value="planForm.planExecuteTime" value-format="YYYY-MM-DD HH:mm:ss" show-time style="width: 100%" />
        </a-form-item>
        <a-form-item label="跟进人">
          <a-input v-model:value="planForm.executorName" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="executeOpen" title="执行回访计划" :confirm-loading="executeSubmitting" @ok="submitExecuteCallback">
      <a-form :model="executeForm" layout="vertical">
        <a-form-item label="执行备注">
          <a-textarea v-model:value="executeForm.executeNote" :rows="2" />
        </a-form-item>
        <a-form-item label="回访类型">
          <a-select v-model:value="executeForm.callbackType">
            <a-select-option
              v-for="item in MARKETING_CALLBACK_TYPE_OPTIONS"
              :key="item.value"
              :value="item.value"
            >
              {{ item.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="满意度评分">
          <a-input-number v-model:value="executeForm.score" :min="0" :max="5" :step="0.5" style="width: 100%" />
        </a-form-item>
        <a-form-item label="回访结果">
          <a-textarea v-model:value="executeForm.followupResult" :rows="3" placeholder="填写回访结果或改进结论" />
        </a-form-item>
        <a-form-item label="下次回访日期">
          <a-date-picker v-model:value="executeForm.nextFollowDate" value-format="YYYY-MM-DD" style="width: 100%" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal
      v-model:open="assignOpen"
      title="分配线索负责人"
      :confirm-loading="assignSubmitting"
      @ok="submitAssign"
    >
      <a-form :model="assignForm" layout="vertical">
        <a-form-item label="负责人">
          <a-select
            v-model:value="assignForm.ownerStaffId"
            show-search
            allow-clear
            placeholder="请选择负责人"
            :options="staffOptions"
            :filter-option="false"
            @search="searchStaff"
            @focus="() => !staffOptions.length && searchStaff('')"
          />
        </a-form-item>
        <a-form-item label="分配说明">
          <a-textarea v-model:value="assignForm.remark" :rows="3" placeholder="可选，记录分配原因或交接说明" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-drawer v-model:open="traceOpen" title="客户轨迹" width="760">
      <a-spin :spinning="traceLoading">
        <a-descriptions v-if="traceLead" :column="1" bordered size="small">
          <a-descriptions-item label="客户">{{ traceLead.elderName || traceLead.consultantName || '-' }}</a-descriptions-item>
          <a-descriptions-item label="联系电话">{{ traceLead.elderPhone || traceLead.consultantPhone || '-' }}</a-descriptions-item>
          <a-descriptions-item label="当前负责人">{{ traceLead.ownerStaffName || traceLead.marketerName || '-' }}</a-descriptions-item>
          <a-descriptions-item label="当前阶段">
            {{ traceStageLabel(traceLead.flowStage) || `线索状态 ${traceLead.status ?? '-'}` }}
          </a-descriptions-item>
          <a-descriptions-item label="下次跟进">{{ traceLead.nextFollowDate || '-' }}</a-descriptions-item>
        </a-descriptions>

        <MarketingTraceTimelineCard title="负责人分配记录" :items="leadAssignLogs" empty-text="暂无分配记录">
          <template #default="{ item }">
            <div class="trace-item-title">
              {{ item.fromOwnerStaffName || '未分配' }} → {{ item.toOwnerStaffName || '未分配' }}
            </div>
            <div class="trace-item-meta">
              {{ traceTime(item.assignedAt) }} · 操作人 {{ item.assignedByName || '-' }}
            </div>
            <div v-if="traceRemark(item.remark)" class="trace-item-remark">{{ traceRemark(item.remark) }}</div>
          </template>
        </MarketingTraceTimelineCard>

        <MarketingTraceTimelineCard title="阶段流转记录" :items="leadStageLogs" empty-text="暂无阶段流转记录">
          <template #default="{ item }">
            <div class="trace-item-title">
              {{ traceStageLabel(item.fromStage || item.fromStatus) || '起始' }} → {{ traceStageLabel(item.toStage || item.toStatus) || '当前' }}
            </div>
            <div class="trace-item-meta">
              {{ traceTime(item.operatedAt) }} · {{ traceTransitionLabel(item.transitionType) }} · {{ item.operatedByName || '-' }}
            </div>
            <div class="trace-item-meta">
              归属部门 {{ traceStageLabel(item.fromOwnerDept) || '-' }} → {{ traceStageLabel(item.toOwnerDept) || '-' }}
            </div>
            <div v-if="traceRemark(item.remark)" class="trace-item-remark">{{ traceRemark(item.remark) }}</div>
          </template>
        </MarketingTraceTimelineCard>
      </a-spin>
    </a-drawer>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'ant-design-vue'
import { message, Modal } from 'ant-design-vue'
import dayjs from 'dayjs'
import PageContainer from '../../../components/PageContainer.vue'
import SearchForm from '../../../components/SearchForm.vue'
import StatefulBlock from '../../../components/StatefulBlock.vue'
import MarketingListToolbar from './MarketingListToolbar.vue'
import MarketingTraceTimelineCard from './MarketingTraceTimelineCard.vue'
import {
  assignCrmLead,
  batchDeleteLeads,
  batchUpdateLeadStatus,
  createCrmLead,
  createLeadCallbackPlan,
  executeCallbackPlan,
  getLeadAssignLogs,
  getLeadCallbackPlans,
  getMarketingLeadEntrySummary,
  getLeadStageLogs,
  getLeadPage,
  updateCrmLead
} from '../../../api/marketing'
import { useStaffOptions } from '../../../composables/useStaffOptions'
import { useUserStore } from '../../../stores/user'
import { CONTRACT_FLOW_STAGE_LABELS, MARKETING_CALLBACK_TYPE_LABELS, MARKETING_CALLBACK_TYPE_OPTIONS, MARKETING_LEAD_MODE_LABELS, marketingInfoSourceLabel } from '../../../utils/marketingEnums'
import { resolveRouteAccess } from '../../../utils/routeAccess'
import type {
  CrmLeadAssignLogItem,
  CrmLeadItem,
  CrmStageTransitionLogItem,
  Id,
  MarketingCallbackType,
  MarketingLeadEntrySummary,
  MarketingLeadMode
} from '../../../types'

const props = withDefaults(defineProps<{
  mode: 'consultation' | 'intent' | 'reservation' | 'invalid' | 'callback' | 'pipeline'
  title: string
  subTitle: string
  scenario?: string
  callbackType?: MarketingCallbackType | ''
}>(), {
  mode: 'consultation',
  callbackType: ''
})

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const pipelineTab = ref<'consultation' | 'intent' | 'callback'>('consultation')
const effectiveMode = computed<'consultation' | 'intent' | 'reservation' | 'invalid' | 'callback'>(() => {
  return props.mode === 'pipeline' ? pipelineTab.value : props.mode
})
const loading = ref(false)
const tableError = ref('')
const rows = ref<CrmLeadItem[]>([])
const total = ref(0)
const selectedRowKeys = ref<string[]>([])
const summaryLoading = ref(false)
const summaryError = ref('')
const summary = reactive<MarketingLeadEntrySummary>({
  totalCount: 0,
  modeCount: 0,
  consultCount: 0,
  intentCount: 0,
  reservationCount: 0,
  invalidCount: 0,
  signedContractCount: 0,
  unsignedReservationCount: 0,
  refundedReservationCount: 0,
  callbackDueTodayCount: 0,
  callbackOverdueCount: 0,
  callbackPendingCount: 0,
  missingSourceCount: 0,
  missingNextFollowDateCount: 0,
  nonStandardSourceCount: 0
})

const query = reactive({
  consultantName: '',
  consultantPhone: '',
  elderName: '',
  elderPhone: '',
  consultDateRange: [] as string[],
  consultType: '',
  mediaChannel: '',
  infoSource: '',
  customerTag: '',
  marketerName: '',
  pageNo: 1,
  pageSize: 10
})
const { staffOptions, searchStaff, ensureSelectedStaff } = useStaffOptions({ pageSize: 120, preloadSize: 400 })

const open = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<Partial<CrmLeadItem>>({})
const assignOpen = ref(false)
const assignSubmitting = ref(false)
const assignForm = reactive({
  ownerStaffId: '',
  remark: ''
})
const traceOpen = ref(false)
const traceLoading = ref(false)
const traceLead = ref<CrmLeadItem>()
const leadAssignLogs = ref<CrmLeadAssignLogItem[]>([])
const leadStageLogs = ref<CrmStageTransitionLogItem[]>([])

/** 轨迹抽屉展示口径：枚举转中文、ISO 时间转常规格式、剥离系统附带的原始 JSON */
const TRACE_STAGE_LABELS: Record<string, string> = {
  ...CONTRACT_FLOW_STAGE_LABELS,
  ASSESSMENT: '评估部',
  MARKETING: '营销部',
  CONSULTATION: '咨询',
  INTENT: '意向',
  RESERVATION: '预订',
  INVALID: '失效',
  SIGNED: '已签约'
}

function traceStageLabel(value?: string | null) {
  const key = String(value || '').trim()
  if (!key) return ''
  return TRACE_STAGE_LABELS[key] || key
}

function traceTransitionLabel(value?: string | null) {
  const key = String(value || '').trim()
  const map: Record<string, string> = { CREATE: '创建', UPDATE: '更新', ASSIGN: '分配', ADVANCE: '推进', ROLLBACK: '回退' }
  return map[key] || key || '阶段变更'
}

function traceTime(value?: string | null) {
  if (!value) return '-'
  const parsed = dayjs(value)
  return parsed.isValid() ? parsed.format('YYYY-MM-DD HH:mm') : value
}

function traceRemark(value?: string | null) {
  const text = String(value || '').trim()
  if (!text) return ''
  return text.replace(/\s*\|\s*\{[\s\S]*\}\s*$/, '').trim()
}

const infoSourceLabel = marketingInfoSourceLabel

const planOpen = ref(false)
const planSubmitting = ref(false)
const planForm = reactive({
  title: '回访',
  callbackType: 'checkin' as MarketingCallbackType,
  followupContent: '',
  planExecuteTime: dayjs().format('YYYY-MM-DD HH:mm:ss'),
  executorName: ''
})
const executeOpen = ref(false)
const executeSubmitting = ref(false)
const pendingExecutePlanId = ref<Id>()
const executeForm = reactive({
  executeNote: '页面执行',
  callbackType: 'checkin' as MarketingCallbackType,
  score: undefined as number | undefined,
  followupResult: '',
  nextFollowDate: ''
})
const callbackSnapshot = ref<Record<string, {
  title?: string
  followupContent?: string
  followupResult?: string
  planId?: Id
  callbackType?: string
}>>({})
const todayText = dayjs().format('YYYY-MM-DD')

const rules: FormRules = {
  consultantName: [{ required: true, message: '请输入咨询人姓名' }],
  elderName: [{ required: true, message: '请输入老人姓名' }]
}

const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: Array<string | number>) => {
    selectedRowKeys.value = keys.map((item) => String(item))
  }
}))
const selectedCount = computed(() => selectedRowKeys.value.length)
const selectedRows = computed(() => rows.value.filter((item) => selectedRowKeys.value.some((id) => sameId(item.id, id))))
const canAssignLead = computed(() =>
  resolveRouteAccess(router, userStore.roles || [], route.path, userStore.pagePermissions || []).canAccess)
const modeLabel = computed(() => MARKETING_LEAD_MODE_LABELS[effectiveMode.value] || '营销线索')
const workspaceSummary = computed(() => {
  if (effectiveMode.value === 'consultation') {
    return '把咨询线索、负责人分配和转意向动作收进同一张工作表里，适合前置筛选和首轮判断。'
  }
  if (effectiveMode.value === 'intent') {
    return '围绕意向客户推进回访、标签和转预订动作，优先处理逾期跟进与缺失计划。'
  }
  if (effectiveMode.value === 'reservation') {
    return '预订页专注锁床证据、金额、待签约预订与合同前准备，不再混入签约后动作。'
  }
  if (effectiveMode.value === 'invalid') {
    return '失效池保留恢复、负责人补派和归档清理动作，便于复盘和二次激活。'
  }
  return '互动跟进页承接待回访、执行回访和顺延计划，签后客户会自动进入入住后回访节奏。'
})
const activeFilterCount = computed(() => {
  const values = [
    query.consultantName,
    query.consultantPhone,
    query.elderName,
    query.elderPhone,
    query.consultType,
    query.mediaChannel,
    query.infoSource,
    query.customerTag,
    query.marketerName,
    ...(query.consultDateRange || [])
  ].filter((item) => String(item || '').trim())
  if (props.mode === 'pipeline') values.push(pipelineTab.value)
  return values.length
})
const activeFilterSummary = computed(() => (
  activeFilterCount.value > 0 ? `已启用 ${activeFilterCount.value} 个筛选条件` : '未设置筛选条件'
))
const hasScopedDataset = computed(() => {
  const scenario = String(props.scenario || route.query.scenario || '').trim()
  const filter = String(route.query.filter || '').trim()
  const stage = String(route.query.stage || '').trim()
  const source = String(route.query.source || '').trim()
  const callbackType = effectiveMode.value === 'callback'
    ? String(props.callbackType || route.query.type || '').trim()
    : ''
  return Boolean(scenario || filter || stage || source || callbackType)
})

const modalTitle = computed(() => form.id ? '编辑客户' : '新增客户')

const columns = computed(() => {
  const mode = effectiveMode.value
  if (mode === 'consultation') {
    return [
      { title: '咨询人姓名', dataIndex: 'consultantName', key: 'consultantName', width: 130 },
      { title: '联系电话', dataIndex: 'consultantPhone', key: 'consultantPhone', width: 130 },
      { title: '老人姓名', dataIndex: 'elderName', key: 'elderName', width: 120 },
      { title: '老人联系电话', dataIndex: 'elderPhone', key: 'elderPhone', width: 130 },
      { title: '性别', dataIndex: 'gender', key: 'gender', width: 80 },
      { title: '年龄', dataIndex: 'age', key: 'age', width: 80 },
      { title: '咨询日期', dataIndex: 'consultDate', key: 'consultDate', width: 120 },
      { title: '咨询类型', dataIndex: 'consultType', key: 'consultType', width: 120 },
      { title: '媒体渠道', dataIndex: 'mediaChannel', key: 'mediaChannel', width: 120 },
      { title: '信息来源', dataIndex: 'infoSource', key: 'infoSource', width: 120 },
      { title: '接待人', dataIndex: 'receptionistName', key: 'receptionistName', width: 120 },
      { title: '负责人', dataIndex: 'ownerDisplayName', key: 'ownerDisplayName', width: 120 },
      { title: '家庭地址', dataIndex: 'homeAddress', key: 'homeAddress', width: 160 },
      { title: '所属机构', dataIndex: 'orgName', key: 'orgName', width: 120 },
      { title: '操作', key: 'action', width: 240, fixed: 'right' as const }
    ]
  }
  if (mode === 'intent') {
    return [
      { title: '老人姓名', dataIndex: 'elderName', key: 'elderName', width: 120 },
      { title: '老人联系电话', dataIndex: 'elderPhone', key: 'elderPhone', width: 140 },
      { title: '信息来源', dataIndex: 'infoSource', key: 'infoSource', width: 120 },
      { title: '性别', dataIndex: 'gender', key: 'gender', width: 80 },
      { title: '年龄', dataIndex: 'age', key: 'age', width: 80 },
      { title: '回访状态', dataIndex: 'followupStatus', key: 'followupStatus', width: 120 },
      { title: '回访内容', dataIndex: 'followupContent', key: 'followupContent', width: 180 },
      { title: '回访结果', dataIndex: 'followupResult', key: 'followupResult', width: 180 },
      { title: '推荐渠道', dataIndex: 'referralChannel', key: 'referralChannel', width: 120 },
      { title: '客户标签', dataIndex: 'customerTag', key: 'customerTag', width: 120 },
      { title: '负责人', dataIndex: 'ownerDisplayName', key: 'ownerDisplayName', width: 140 },
      { title: '所属机构', dataIndex: 'orgName', key: 'orgName', width: 120 },
      { title: '操作', key: 'action', width: 280, fixed: 'right' as const }
    ]
  }
  if (mode === 'reservation') {
    return [
      { title: '姓名', dataIndex: 'elderName', key: 'elderName', width: 120 },
      { title: '性别', dataIndex: 'gender', key: 'gender', width: 80 },
      { title: '年龄', dataIndex: 'age', key: 'age', width: 80 },
      { title: '身份证号', dataIndex: 'idCardNo', key: 'idCardNo', width: 160 },
      { title: '联系电话', dataIndex: 'elderPhone', key: 'elderPhone', width: 130 },
      { title: '定金额度', dataIndex: 'reservationAmount', key: 'reservationAmount', width: 120 },
      { title: '预订房号', dataIndex: 'reservationRoomNo', key: 'reservationRoomNo', width: 140 },
      { title: '负责人', dataIndex: 'ownerDisplayName', key: 'ownerDisplayName', width: 120 },
      { title: '收款时间', dataIndex: 'paymentTime', key: 'paymentTime', width: 170 },
      { title: '是否退款', dataIndex: 'refunded', key: 'refunded', width: 100 },
      { title: '预约渠道', dataIndex: 'reservationChannel', key: 'reservationChannel', width: 120 },
      { title: '状态', dataIndex: 'reservationStatus', key: 'reservationStatus', width: 100 },
      { title: '所属机构', dataIndex: 'orgName', key: 'orgName', width: 120 },
      { title: '操作', key: 'action', width: 240, fixed: 'right' as const }
    ]
  }
  if (mode === 'invalid') {
    return [
      { title: '序号', dataIndex: 'index', key: 'index', width: 80 },
      { title: '老人姓名', dataIndex: 'elderName', key: 'elderName', width: 120 },
      { title: '老人联系电话', dataIndex: 'elderPhone', key: 'elderPhone', width: 140 },
      { title: '信息来源', dataIndex: 'infoSource', key: 'infoSource', width: 120 },
      { title: '性别', dataIndex: 'gender', key: 'gender', width: 80 },
      { title: '年龄', dataIndex: 'age', key: 'age', width: 80 },
      { title: '推荐渠道', dataIndex: 'referralChannel', key: 'referralChannel', width: 120 },
      { title: '客户标签', dataIndex: 'customerTag', key: 'customerTag', width: 120 },
      { title: '负责人', dataIndex: 'ownerDisplayName', key: 'ownerDisplayName', width: 140 },
      { title: '失效时间', dataIndex: 'invalidTime', key: 'invalidTime', width: 170 },
      { title: '所属机构', dataIndex: 'orgName', key: 'orgName', width: 120 },
      { title: '操作', key: 'action', width: 220, fixed: 'right' as const }
    ]
  }
  return [
    { title: '客户姓名', dataIndex: 'elderName', key: 'elderName', width: 140 },
    { title: '联系电话', dataIndex: 'elderPhone', key: 'elderPhone', width: 140 },
    { title: '跟进负责人', dataIndex: 'ownerDisplayName', key: 'ownerDisplayName', width: 120 },
    { title: '计划执行时间', dataIndex: 'nextFollowDate', key: 'nextFollowDate', width: 170 },
    { title: '计划标题', dataIndex: 'planTitle', key: 'planTitle', width: 160 },
    { title: '回访内容', dataIndex: 'followupContent', key: 'followupContent', width: 220 },
    { title: '回访结果', dataIndex: 'followupResult', key: 'followupResult', width: 220 },
    { title: '所属机构', dataIndex: 'orgName', key: 'orgName', width: 120 },
    { title: '操作', key: 'action', width: 260, fixed: 'right' as const }
  ]
})

const tableScrollX = computed(() => {
  const mode = effectiveMode.value
  if (mode === 'consultation') return 1700
  if (mode === 'intent') return 1500
  if (mode === 'reservation') return 1450
  if (mode === 'invalid') return 1380
  return 1320
})

const actionButtons = computed(() => {
  const mode = effectiveMode.value
  if (mode === 'consultation') {
    return [
      { key: 'create', label: '新增', type: 'primary' as const },
      { key: 'edit', label: '编辑', type: 'default' as const, disabled: selectedCount.value !== 1 },
      { key: 'detail', label: '客户轨迹', type: 'default' as const, disabled: selectedCount.value !== 1 },
      { key: 'assign', label: '分配负责人', type: 'default' as const, disabled: selectedCount.value !== 1 || !canAssignLead.value },
      { key: 'setIntent', label: '设为意向', type: 'default' as const, disabled: selectedCount.value === 0 },
      { key: 'abandon', label: '放弃客户', type: 'default' as const, disabled: selectedCount.value === 0, danger: true },
      { key: 'delete', label: '删除', type: 'default' as const, disabled: selectedCount.value === 0, danger: true }
    ]
  }
  if (mode === 'intent') {
    return [
      { key: 'create', label: '新增', type: 'primary' as const },
      { key: 'edit', label: '编辑', type: 'default' as const, disabled: selectedCount.value !== 1 },
      { key: 'detail', label: '客户轨迹', type: 'default' as const, disabled: selectedCount.value !== 1 },
      { key: 'assign', label: '分配负责人', type: 'default' as const, disabled: selectedCount.value !== 1 || !canAssignLead.value },
      { key: 'toReservation', label: '转预订', type: 'default' as const, disabled: selectedCount.value === 0 },
      { key: 'tag', label: '设置客户标签', type: 'default' as const, disabled: selectedCount.value === 0 },
      { key: 'callback', label: '添加回访计划', type: 'default' as const, disabled: selectedCount.value === 0 },
      { key: 'abandon', label: '放弃客户', type: 'default' as const, disabled: selectedCount.value === 0, danger: true },
      { key: 'delete', label: '删除', type: 'default' as const, disabled: selectedCount.value === 0, danger: true }
    ]
  }
  if (mode === 'reservation') {
    return [
      { key: 'create', label: '新增', type: 'primary' as const },
      { key: 'edit', label: '编辑', type: 'default' as const, disabled: selectedCount.value !== 1 },
      { key: 'detail', label: '客户轨迹', type: 'default' as const, disabled: selectedCount.value !== 1 },
      { key: 'assign', label: '分配负责人', type: 'default' as const, disabled: selectedCount.value !== 1 || !canAssignLead.value },
      { key: 'toggleRefund', label: '退款切换', type: 'default' as const, disabled: selectedCount.value !== 1 },
      { key: 'delete', label: '删除', type: 'default' as const, disabled: selectedCount.value === 0, danger: true }
    ]
  }
  if (mode === 'invalid') {
    return [
      { key: 'detail', label: '客户轨迹', type: 'default' as const, disabled: selectedCount.value !== 1 },
      { key: 'assign', label: '分配负责人', type: 'default' as const, disabled: selectedCount.value !== 1 || !canAssignLead.value },
      { key: 'recover', label: '恢复客户', type: 'primary' as const, disabled: selectedCount.value === 0 },
      { key: 'delete', label: '删除', type: 'default' as const, disabled: selectedCount.value === 0, danger: true }
    ]
  }
  if (mode === 'callback') {
    return [
      { key: 'edit', label: '编辑', type: 'default' as const, disabled: selectedCount.value !== 1 },
      { key: 'detail', label: '客户轨迹', type: 'default' as const, disabled: selectedCount.value !== 1 },
      { key: 'assign', label: '分配负责人', type: 'default' as const, disabled: selectedCount.value !== 1 || !canAssignLead.value },
      { key: 'execute', label: '执行回访', type: 'primary' as const, disabled: selectedCount.value !== 1 },
      { key: 'setTodayFollow', label: '设今日回访', type: 'default' as const, disabled: selectedCount.value === 0 },
      { key: 'postponeFollow', label: '顺延3天', type: 'default' as const, disabled: selectedCount.value === 0 },
      { key: 'markFollowed', label: '标记已回访', type: 'default' as const, disabled: selectedCount.value === 0 },
      { key: 'callback', label: '添加回访计划', type: 'default' as const, disabled: selectedCount.value === 0 },
      { key: 'toReservation', label: '转预订', type: 'default' as const, disabled: selectedCount.value === 0 },
      { key: 'abandon', label: '放弃客户', type: 'default' as const, disabled: selectedCount.value === 0, danger: true },
      { key: 'delete', label: '删除', type: 'default' as const, disabled: selectedCount.value === 0, danger: true }
    ]
  }
  return [
    { key: 'edit', label: '编辑', type: 'default' as const, disabled: selectedCount.value !== 1 },
    { key: 'detail', label: '客户轨迹', type: 'default' as const, disabled: selectedCount.value !== 1 },
    { key: 'assign', label: '分配负责人', type: 'default' as const, disabled: selectedCount.value !== 1 || !canAssignLead.value },
    { key: 'execute', label: '执行回访', type: 'primary' as const, disabled: selectedCount.value !== 1 },
    { key: 'setTodayFollow', label: '设今日回访', type: 'default' as const, disabled: selectedCount.value === 0 },
    { key: 'postponeFollow', label: '顺延3天', type: 'default' as const, disabled: selectedCount.value === 0 },
    { key: 'markFollowed', label: '标记已回访', type: 'default' as const, disabled: selectedCount.value === 0 },
    { key: 'callback', label: '添加回访计划', type: 'default' as const, disabled: selectedCount.value === 0 },
    { key: 'toReservation', label: '转预订', type: 'default' as const, disabled: selectedCount.value === 0 },
    { key: 'abandon', label: '放弃客户', type: 'default' as const, disabled: selectedCount.value === 0, danger: true },
    { key: 'delete', label: '删除', type: 'default' as const, disabled: selectedCount.value === 0, danger: true }
  ]
})

const mappedRows = computed(() => rows.value.map((item, index) => {
  const snapshot = callbackSnapshot.value[item.id] || {}
  return {
    ...item,
    index: index + 1,
    ownerDisplayName: item.ownerStaffName || item.marketerName || '未分配',
    planTitle: snapshot.title || item.remark,
    followupContent: snapshot.followupContent,
    followupResult: snapshot.followupResult
  }
}))
const displayRows = computed(() => {
  if (!hasScopedDataset.value) {
    return mappedRows.value
  }
  const start = Math.max((query.pageNo - 1) * query.pageSize, 0)
  return mappedRows.value.slice(start, start + query.pageSize)
})
const displayTotal = computed(() => (hasScopedDataset.value ? mappedRows.value.length : total.value))

const STANDARD_SOURCES = ['抖音', '微信', '转介绍', '自然到访', '线上咨询', '社区活动', '其他']

function normalizeUnifiedSource(value?: string) {
  const text = String(value || '').trim().replace(/　/g, ' ').replace(/\s+/g, '')
  if (!text) return ''
  const lower = text.toLowerCase()
  if (lower.includes('抖音') || lower.includes('douyin') || lower === 'dy') return '抖音'
  if (lower.includes('微信') || lower.includes('weixin') || lower === 'wx') return '微信'
  if (lower.includes('转介绍') || lower.includes('介绍')) return '转介绍'
  if (lower.includes('自然到访') || lower.includes('到访')) return '自然到访'
  if (lower.includes('线上') || lower.includes('官网') || lower.includes('小程序')) return '线上咨询'
  if (lower.includes('社区')) return '社区活动'
  if (lower === '其他' || lower === 'other') return '其他'
  return text
}

function resolveRowSource(item: CrmLeadItem) {
  return normalizeUnifiedSource(item.source || item.infoSource || '')
}

function inferLeadCallbackType(item: CrmLeadItem) {
  if (isSignedLead(item)) return 'checkin'
  return ''
}

function buildScopedSummary(list: CrmLeadItem[]): MarketingLeadEntrySummary {
  const today = todayText
  const consultCount = list.filter(isConsultLead).length
  const intentCount = list.filter(isIntentLead).length
  const reservationCount = list.filter(hasReservationEvidence).length
  const invalidCount = list.filter((item) => Number(item.status) === 3).length
  const signedContractCount = list.filter(isSignedLead).length
  const unsignedReservationCount = list.filter((item) => hasReservationEvidence(item) && !isSignedLead(item)).length
  const refundedReservationCount = list.filter((item) => hasReservationEvidence(item) && Number(item.refunded) === 1).length
  const callbackDueTodayCount = list.filter((item) => {
    if (Number(item.status) === 3) return false
    if (effectiveMode.value !== 'callback' && isSignedLead(item)) return false
    return String(item.nextFollowDate || '').slice(0, 10) === today
  }).length
  const callbackOverdueCount = list.filter((item) => {
    const nextFollow = String(item.nextFollowDate || '').slice(0, 10)
    if (Number(item.status) === 3 || !nextFollow) return false
    if (effectiveMode.value !== 'callback' && isSignedLead(item)) return false
    return nextFollow < today
  }).length
  const missingSourceCount = list.filter((item) => !resolveRowSource(item)).length
  const missingNextFollowDateCount = list.filter((item) => {
    if (Number(item.status) === 3) return false
    if (effectiveMode.value !== 'callback' && isSignedLead(item)) return false
    return !String(item.nextFollowDate || '').slice(0, 10)
  }).length
  const nonStandardSourceCount = list.filter((item) => {
    const source = resolveRowSource(item)
    return !!source && !STANDARD_SOURCES.includes(source)
  }).length

  let modeCount = list.length
  if (effectiveMode.value === 'consultation') modeCount = consultCount
  if (effectiveMode.value === 'intent') modeCount = intentCount
  if (effectiveMode.value === 'reservation') modeCount = reservationCount
  if (effectiveMode.value === 'invalid') modeCount = invalidCount
  if (effectiveMode.value === 'callback') modeCount = list.length

  return {
    totalCount: list.length,
    modeCount,
    consultCount,
    intentCount,
    reservationCount,
    invalidCount,
    signedContractCount,
    unsignedReservationCount,
    refundedReservationCount,
    callbackDueTodayCount,
    callbackOverdueCount,
    callbackPendingCount: callbackDueTodayCount + callbackOverdueCount,
    missingSourceCount,
    missingNextFollowDateCount,
    nonStandardSourceCount
  }
}

const currentSummary = computed<MarketingLeadEntrySummary>(() => (
  hasScopedDataset.value ? buildScopedSummary(rows.value) : { ...summary }
))

const summaryCards = computed(() => {
  const mode = effectiveMode.value
  const metrics = currentSummary.value
  if (mode === 'consultation') {
    return [
      { title: '当前咨询池', value: metrics.modeCount },
      { title: '总线索数', value: metrics.totalCount },
      { title: '缺失来源', value: metrics.missingSourceCount },
      { title: '非标准来源', value: metrics.nonStandardSourceCount }
    ]
  }
  if (mode === 'intent') {
    return [
      { title: '当前意向客户', value: metrics.modeCount },
      { title: '待回访总量', value: metrics.callbackPendingCount },
      { title: '逾期回访', value: metrics.callbackOverdueCount },
      { title: '缺少下次回访', value: metrics.missingNextFollowDateCount }
    ]
  }
  if (mode === 'reservation') {
    return [
      { title: '当前预订客户', value: metrics.modeCount },
      { title: '已签约', value: metrics.signedContractCount },
      { title: '待签约预订', value: metrics.unsignedReservationCount },
      { title: '退款预订', value: metrics.refundedReservationCount }
    ]
  }
  if (mode === 'invalid') {
    return [
      { title: '失效客户', value: metrics.modeCount },
      { title: '咨询客户', value: metrics.consultCount },
      { title: '意向客户', value: metrics.intentCount },
      { title: '预订客户', value: metrics.reservationCount }
    ]
  }
  return [
    { title: '待回访总量', value: metrics.modeCount },
    { title: '今日到期', value: metrics.callbackDueTodayCount },
    { title: '逾期回访', value: metrics.callbackOverdueCount },
    { title: '缺少下次回访', value: metrics.missingNextFollowDateCount }
  ]
})

const warningMessage = computed(() => {
  const mode = effectiveMode.value
  const metrics = currentSummary.value
  if (mode === 'consultation' && (metrics.missingSourceCount > 0 || metrics.nonStandardSourceCount > 0)) {
    return `数据质量提醒：缺失来源 ${metrics.missingSourceCount} 条，非标准来源 ${metrics.nonStandardSourceCount} 条。`
  }
  if (mode === 'intent' && (metrics.callbackOverdueCount > 0 || metrics.missingNextFollowDateCount > 0)) {
    return `跟进风险提醒：逾期回访 ${metrics.callbackOverdueCount} 条，缺失下次回访日期 ${metrics.missingNextFollowDateCount} 条。`
  }
  if (mode === 'reservation' && (metrics.unsignedReservationCount > 0 || metrics.refundedReservationCount > 0)) {
    return `签约闭环提醒：待签约预订 ${metrics.unsignedReservationCount} 条，退款预订 ${metrics.refundedReservationCount} 条。`
  }
  if (mode === 'callback' && (metrics.callbackOverdueCount > 0 || metrics.callbackDueTodayCount > 0)) {
    return `回访提醒：今日到期 ${metrics.callbackDueTodayCount} 条，逾期 ${metrics.callbackOverdueCount} 条。`
  }
  return ''
})

function buildRowActions(record: CrmLeadItem) {
  const mode = effectiveMode.value
  if (mode === 'consultation') {
    return [
      { key: 'detail', label: '轨迹' },
      { key: 'edit', label: '编辑' },
      { key: 'assign', label: '分配', disabled: !canAssignLead.value },
      { key: 'setIntent', label: '转意向' },
      { key: 'abandon', label: '放弃', danger: true }
    ].filter((item) => !item.disabled)
  }
  if (mode === 'intent') {
    return [
      { key: 'detail', label: '轨迹' },
      { key: 'edit', label: '编辑' },
      { key: 'callback', label: '回访计划' },
      { key: 'toReservation', label: '转预订' },
      { key: 'assign', label: '分配', disabled: !canAssignLead.value },
      { key: 'abandon', label: '放弃', danger: true }
    ].filter((item) => !item.disabled)
  }
  if (mode === 'reservation') {
    return [
      { key: 'detail', label: '轨迹' },
      { key: 'edit', label: '编辑' },
      { key: 'toggleRefund', label: Number(record.refunded) === 1 ? '取消退款' : '标记退款' }
    ]
  }
  if (mode === 'invalid') {
    return [
      { key: 'detail', label: '轨迹' },
      { key: 'recover', label: '恢复' },
      { key: 'assign', label: '分配', disabled: !canAssignLead.value },
      { key: 'delete', label: '删除', danger: true }
    ].filter((item) => !item.disabled)
  }
  return [
    { key: 'detail', label: '轨迹' },
    { key: 'execute', label: '执行回访' },
    { key: 'callback', label: '补计划' },
    { key: 'postponeFollow', label: '顺延3天' },
    { key: 'toReservation', label: '转预订' }
  ]
}

function followupStatusColor(value?: string) {
  const text = String(value || '').trim()
  if (text === '已回访') return 'green'
  if (text === '待回访') return 'gold'
  if (text === '待跟进') return 'blue'
  return 'default'
}

function reservationStatusLabel(record: CrmLeadItem) {
  if (isSignedLead(record)) return '已签约'
  const status = String(record.reservationStatus || '').trim()
  if (status) return status
  if (hasReservationEvidence(record)) return '预订'
  return '未锁定'
}

function reservationStatusColor(record: CrmLeadItem) {
  if (isSignedLead(record)) return 'green'
  if (Number(record.refunded) === 1) return 'red'
  if (hasReservationEvidence(record)) return 'geekblue'
  return 'default'
}

function callbackTypeLabel(record: CrmLeadItem) {
  const snapshot = callbackSnapshot.value[String(record.id)] || {}
  const type = String(snapshot.callbackType || '').trim().toLowerCase()
  if (!type) return '暂无回访类型'
  return MARKETING_CALLBACK_TYPE_LABELS[type as MarketingCallbackType] || type
}

function textContains(value: unknown, keyword: string) {
  return String(value || '').toLowerCase().includes(keyword.toLowerCase())
}

function applyScenarioFilters(
  list: CrmLeadItem[],
  snapshotMap: Record<string, { callbackType?: string }> = {}
) {
  const scenario = String(props.scenario || route.query.scenario || '').trim()
  const source = String(route.query.source || '').trim()
  const filter = String(route.query.filter || '').trim()
  const stage = String(route.query.stage || '').trim()
  const callbackType = String(props.callbackType || route.query.type || '').trim().toLowerCase()

  return (list || []).filter((item) => {
    const snapshot = snapshotMap[String(item.id)] || {}
    const nextFollow = String(item.nextFollowDate || '').slice(0, 10)
    const unifiedSource = resolveRowSource(item)
    const infoSource = String(item.infoSource || '')
    const mediaChannel = String(item.mediaChannel || '')
    const customerTag = String(item.customerTag || '')
    const reservationStatus = String(item.reservationStatus || '')
    const snapshotType = String(snapshot.callbackType || '').trim().toLowerCase()
    const effectiveCallbackType = snapshotType || inferLeadCallbackType(item)

    if (callbackType && effectiveMode.value === 'callback' && effectiveCallbackType !== callbackType) {
      return false
    }

    if (source === 'medical') {
      const isMedicalSource = textContains(infoSource, '医') || textContains(infoSource, '门诊') || textContains(mediaChannel, '医')
      if (!isMedicalSource) return false
    }

    if (filter === 'today' && nextFollow !== todayText) return false
    if (filter === 'overdue' && (!nextFollow || nextFollow >= todayText)) return false
    if (filter === 'unassigned' && String(item.marketerName || '').trim()) return false
    if (filter === 'expiring') {
      if (!textContains(reservationStatus, '锁')) return false
      if (!nextFollow || nextFollow < todayText || dayjs(nextFollow).diff(dayjs(todayText), 'day') > 3) return false
    }
    if (filter === 'missing_followup' && nextFollow) return false
    if (filter === 'unsigned_lock') {
      if (!textContains(reservationStatus, '锁')) return false
      if (isSignedLead(item)) return false
    }

    if (scenario === 'blacklist') {
      if (!textContains(customerTag, '黑')) return false
    }
    if (scenario === 'source_unknown') {
      if (unifiedSource) return false
    }
    if (scenario === 'source_medical') {
      const isMedical = textContains(infoSource, '医') || textContains(infoSource, '门诊') || textContains(mediaChannel, '医')
      if (!isMedical) return false
    }
    if (scenario === 'follow_today') {
      if (!nextFollow || nextFollow !== todayText) return false
    }
    if (scenario === 'follow_overdue') {
      if (!nextFollow || nextFollow >= todayText) return false
    }
    if (scenario === 'lock_bed') {
      if (!textContains(reservationStatus, '锁')) return false
    }
    if (scenario === 'expiring_lock') {
      if (!textContains(reservationStatus, '锁')) return false
      if (!nextFollow || nextFollow < todayText || dayjs(nextFollow).diff(dayjs(todayText), 'day') > 3) return false
    }
    if (stage === 'consultation' && Number(item.status) !== 0) return false
    if (stage === 'evaluation' && !isIntentLead(item)) return false
    if (stage === 'signing' && !hasReservationEvidence(item)) return false
    if (stage === 'lost' && Number(item.status) !== 3) return false
    if (effectiveMode.value === 'reservation' && !hasReservationEvidence(item)) return false
    return true
  })
}

function selectedIds() {
  if (selectedRowKeys.value.length) {
    return [...selectedRowKeys.value]
  }
  return []
}

function requireSingleSelection(actionLabel: string) {
  if (selectedRows.value.length !== 1) {
    message.info(`请先勾选 1 条客户后再${actionLabel}`)
    return null
  }
  return selectedRows.value[0]
}

function sameId(left: Id | undefined, right: Id | undefined) {
  if (left == null || right == null) return false
  return String(left) === String(right)
}

function defaultStatus(mode: string) {
  if (mode === 'consultation') return 0
  if (mode === 'intent') return 1
  if (mode === 'reservation') return undefined
  if (mode === 'invalid') return 3
  return undefined
}

function isSignedLead(item: CrmLeadItem) {
  return Number(item.contractSignedFlag || 0) === 1 || String(item.flowStage || '').toUpperCase() === 'SIGNED'
}

function hasReservationEvidence(item: CrmLeadItem) {
  if (isSignedLead(item) || Number(item.status) === 3) return false
  if (item.reservationBedId || String(item.reservationRoomNo || '').trim()) return true
  if (Number(item.reservationAmount || 0) > 0) return true
  const flowStage = String(item.flowStage || '').toUpperCase()
  return flowStage === 'PENDING_BED_SELECT' || flowStage === 'PENDING_SIGN'
}

function isConsultLead(item: CrmLeadItem) {
  return !isSignedLead(item) && !hasReservationEvidence(item) && Number(item.status) === 0
}

function isIntentLead(item: CrmLeadItem) {
  return !isSignedLead(item) && !hasReservationEvidence(item) && Number(item.status) === 1
}

function genderText(gender?: number) {
  if (gender === 1) return '男'
  if (gender === 2) return '女'
  return '-'
}

function buildQueryParams() {
  const mode = effectiveMode.value
  const isCallback = mode === 'callback'
  const callbackScenario = String(props.scenario || route.query.scenario || '').trim()
  const callbackFilter = String(route.query.filter || '').trim()
  const callbackDueOnly = isCallback && (!!callbackScenario || !!callbackFilter)
  const routeSource = String(route.query.source || '').trim()
  const normalizedSource = query.infoSource || (routeSource && routeSource !== 'medical' ? routeSource : '') || undefined
  const callbackType = isCallback ? String(props.callbackType || route.query.type || '').trim() : ''
  return {
    pageNo: query.pageNo,
    pageSize: query.pageSize,
    mode,
    status: isCallback ? undefined : defaultStatus(mode),
    consultantName: query.consultantName || undefined,
    consultantPhone: query.consultantPhone || undefined,
    elderName: query.elderName || undefined,
    elderPhone: query.elderPhone || undefined,
    consultDateFrom: query.consultDateRange?.[0] || undefined,
    consultDateTo: query.consultDateRange?.[1] || undefined,
    consultType: query.consultType || undefined,
    mediaChannel: query.mediaChannel || undefined,
    infoSource: normalizedSource,
    customerTag: query.customerTag || undefined,
    marketerName: query.marketerName || undefined,
    callbackType: callbackType || undefined,
    followupDueOnly: callbackDueOnly ? true : undefined,
    followupDateTo: callbackDueOnly ? dayjs().format('YYYY-MM-DD') : undefined
  }
}

function buildSummaryParams() {
  const mode = effectiveMode.value
  const routeSource = String(route.query.source || '').trim()
  return {
    mode: mode as MarketingLeadMode,
    consultantName: query.consultantName || undefined,
    consultantPhone: query.consultantPhone || undefined,
    elderName: query.elderName || undefined,
    elderPhone: query.elderPhone || undefined,
    consultDateFrom: query.consultDateRange?.[0] || undefined,
    consultDateTo: query.consultDateRange?.[1] || undefined,
    consultType: query.consultType || undefined,
    mediaChannel: query.mediaChannel || undefined,
    infoSource: query.infoSource || (routeSource && routeSource !== 'medical' ? routeSource : '') || undefined,
    customerTag: query.customerTag || undefined,
    marketerName: query.marketerName || undefined
  }
}

async function fetchData() {
  loading.value = true
  summaryLoading.value = true
  tableError.value = ''
  summaryError.value = ''
  try {
    const params = buildQueryParams()
    const [pageData, summaryRes] = await Promise.all([
      hasScopedDataset.value ? fetchAllLeadPages(params) : getLeadPage(params),
      getMarketingLeadEntrySummary(buildSummaryParams())
    ])
    const rawList = hasScopedDataset.value
      ? (pageData as CrmLeadItem[]).map((item) => ({ ...item, id: String(item.id) }))
      : (((pageData as any)?.list || []).map((item: CrmLeadItem) => ({ ...item, id: String(item.id) })))
    Object.assign(summary, summaryRes || {})
    const snapshots = await hydrateCallbackSnapshot(rawList)
    rows.value = applyScenarioFilters(rawList, snapshots)
    total.value = hasScopedDataset.value ? rows.value.length : Number((pageData as any)?.total || 0)
  } catch (error: any) {
    const text = error?.message || '请求失败，请稍后重试'
    tableError.value = text
    summaryError.value = text
  } finally {
    loading.value = false
    summaryLoading.value = false
  }
}

async function fetchAllLeadPages(baseParams: Record<string, any>) {
  const pageSize = 200
  let pageNo = 1
  let totalPages = 1
  const result: CrmLeadItem[] = []
  while (pageNo <= totalPages && pageNo <= 50) {
    const page = await getLeadPage({ ...baseParams, pageNo, pageSize })
    result.push(...((page?.list || []) as CrmLeadItem[]))
    const totalCount = Number(page?.total || 0)
    totalPages = Math.max(1, Math.ceil(totalCount / pageSize))
    pageNo += 1
  }
  return result
}

async function hydrateCallbackSnapshot(list: CrmLeadItem[]) {
  const mode = effectiveMode.value
  if (mode !== 'intent' && mode !== 'callback') {
    callbackSnapshot.value = {}
    return {}
  }
  const pairs = await Promise.all(list.map(async (item) => {
    try {
      const plans = await getLeadCallbackPlans(item.id)
      const latest = (plans || [])[0]
      const pending = (plans || []).find((plan) => plan.status === 'PENDING')
      return [item.id, {
        title: latest?.title,
        followupContent: latest?.followupContent,
        followupResult: latest?.followupResult,
        planId: pending?.id || latest?.id,
        callbackType: pending?.callbackType || latest?.callbackType
      }] as const
    } catch {
      return [item.id, {}] as const
    }
  }))
  const snapshotMap = Object.fromEntries(pairs)
  callbackSnapshot.value = snapshotMap
  return snapshotMap
}

function onSearch() {
  query.pageNo = 1
  selectedRowKeys.value = []
  fetchData()
}

function reset() {
  query.consultantName = ''
  query.consultantPhone = ''
  query.elderName = ''
  query.elderPhone = ''
  query.consultDateRange = []
  query.consultType = ''
  query.mediaChannel = ''
  query.infoSource = ''
  query.customerTag = ''
  query.marketerName = ''
  query.pageNo = 1
  query.pageSize = 10
  selectedRowKeys.value = []
  fetchData()
}

function syncPipelineTabByRoute() {
  if (props.mode !== 'pipeline') return
  const tab = String(route.query.tab || '')
  const stage = String(route.query.stage || '').trim()
  const scenario = String(props.scenario || route.query.scenario || '').trim()
  const filter = String(route.query.filter || '').trim()
  if (tab === 'intent' || tab === 'callback' || tab === 'consultation') {
    pipelineTab.value = tab
    return
  }
  if (stage === 'evaluation') {
    pipelineTab.value = 'intent'
    return
  }
  if (scenario === 'follow_today' || scenario === 'follow_overdue' || filter === 'today' || filter === 'overdue') {
    pipelineTab.value = 'callback'
    return
  }
  pipelineTab.value = 'consultation'
}

function syncQueryFiltersByRoute() {
  const routeSource = String(route.query.source || '').trim()
  query.infoSource = routeSource || ''
  query.elderName = String(route.query.elderName || '').trim()
  query.elderPhone = String(route.query.elderPhone || '').trim()
  query.marketerName = String(route.query.marketerName || '').trim()
  const consultDateFrom = String(route.query.consultDateFrom || '').trim()
  const consultDateTo = String(route.query.consultDateTo || '').trim()
  if (consultDateFrom || consultDateTo) {
    query.consultDateRange = [consultDateFrom || consultDateTo, consultDateTo || consultDateFrom].filter(Boolean)
  } else {
    query.consultDateRange = []
  }
}

function onPipelineTabChange() {
  if (props.mode !== 'pipeline') return
  router.replace({ path: route.path, query: { ...route.query, tab: pipelineTab.value } })
  query.pageNo = 1
  selectedRowKeys.value = []
  fetchData()
}

function consumeQuickQuery() {
  if (String(route.query.quick || '').trim() !== '1') {
    return
  }
  const nextQuery: Record<string, any> = { ...route.query }
  delete nextQuery.quick
  router.replace({ path: route.path, query: nextQuery })
}

function onPageChange(page: number) {
  query.pageNo = page
  fetchData()
}

function onPageSizeChange(_current: number, size: number) {
  query.pageNo = 1
  query.pageSize = size
  fetchData()
}

function defaultCallbackType(): MarketingCallbackType {
  const scenario = String(props.scenario || route.query.type || route.query.scenario || '').trim()
  if (scenario === 'trial') return 'trial'
  if (scenario === 'discharge') return 'discharge'
  if (scenario === 'score') return 'score'
  return 'checkin'
}

async function handleTopAction(key: string) {
  if (key === 'create') {
    openForm()
    return
  }
  if (key === 'edit') {
    const row = requireSingleSelection('编辑')
    if (!row) return
    openForm(row)
    return
  }
  if (key === 'detail') {
    const row = requireSingleSelection('查看详情')
    if (!row) return
    openTraceDrawer(row)
    return
  }
  if (key === 'assign') {
    const row = requireSingleSelection('分配负责人')
    if (!row) return
    openAssignModal(row)
    return
  }
  if (key === 'execute') {
    const row = requireSingleSelection('执行回访')
    if (!row) return
    await executeCallback(row)
    return
  }
  if (key === 'setIntent') {
    await batchMoveToIntent()
    return
  }
  if (key === 'toReservation') {
    await batchMoveToReservation()
    return
  }
  if (key === 'abandon') {
    await batchMoveToInvalid()
    return
  }
  if (key === 'setTodayFollow') {
    await batchSetTodayFollow()
    return
  }
  if (key === 'postponeFollow') {
    await batchPostponeFollow(3)
    return
  }
  if (key === 'markFollowed') {
    await batchMarkFollowed()
    return
  }
  if (key === 'recover') {
    await recoverRows()
    return
  }
  if (key === 'toggleRefund') {
    const row = requireSingleSelection('切换退款状态')
    if (!row) return
    toggleRefund(row)
    return
  }
  if (key === 'toAdmission') {
    const row = requireSingleSelection('转入住')
    if (!row) return
    transferToAdmission(row)
    return
  }
  if (key === 'callback') {
    openCallbackPlanForIds(selectedIds())
    return
  }
  if (key === 'tag') {
    const ids = selectedIds()
    if (!ids.length) {
      message.info('请先勾选客户')
      return
    }
    await Promise.all(ids.map((id) => {
      const row = rows.value.find((item) => sameId(item.id, id))
      if (!row) return Promise.resolve()
      return updateCrmLead(id, { ...row, customerTag: row.customerTag || 'BBB' })
    }))
    message.success(`已设置 ${ids.length} 条客户标签`)
    fetchData()
    return
  }
  if (key === 'delete') {
    confirmDeleteIds(selectedIds())
  }
}

async function handleRowAction(key: string, record: CrmLeadItem) {
  if (key === 'detail') {
    await openTraceDrawer(record)
    return
  }
  if (key === 'edit') {
    openForm(record)
    return
  }
  if (key === 'assign') {
    openAssignModal(record)
    return
  }
  if (key === 'execute') {
    await executeCallback(record)
    return
  }
  if (key === 'toggleRefund') {
    toggleRefund(record)
    return
  }
  if (key === 'toAdmission') {
    transferToAdmission(record)
    return
  }
  if (key === 'callback') {
    openCallbackPlanForIds([String(record.id)])
    return
  }
  if (key === 'delete') {
    confirmDeleteIds([String(record.id)])
    return
  }
  if (key === 'setIntent') {
    await batchMoveToIntent([record])
    return
  }
  if (key === 'toReservation') {
    await batchMoveToReservation([record])
    return
  }
  if (key === 'abandon') {
    await batchMoveToInvalid([record])
    return
  }
  if (key === 'recover') {
    await recoverRows([record])
    return
  }
  if (key === 'postponeFollow') {
    await batchPostponeFollow(3, [record])
  }
}

function openCallbackPlanForIds(ids: string[]) {
  if (!ids.length) {
    message.info('请先勾选要添加计划的客户')
    return
  }
  selectedRowKeys.value = [...ids]
  planForm.title = '回访'
  planForm.callbackType = defaultCallbackType()
  planForm.followupContent = ''
  planForm.planExecuteTime = dayjs().format('YYYY-MM-DD HH:mm:ss')
  planForm.executorName = ''
  planOpen.value = true
}

function confirmDeleteIds(ids: string[]) {
  if (!ids.length) {
    message.info('请先勾选要删除的数据')
    return
  }
  Modal.confirm({
    title: `确认删除选中的 ${ids.length} 条客户记录吗？`,
    onOk: async () => {
      await batchDeleteLeads({ ids })
      message.success(`已删除 ${ids.length} 条记录`)
      selectedRowKeys.value = []
      fetchData()
    }
  })
}

function openForm(row?: CrmLeadItem) {
  if (row) {
    Object.assign(form, row)
  } else {
    Object.assign(form, {
      id: undefined,
      name: '',
      phone: '',
      consultantName: '',
      consultantPhone: '',
      elderName: '',
      elderPhone: '',
      gender: undefined,
      age: undefined,
      consultDate: dayjs().format('YYYY-MM-DD'),
      consultType: '',
      mediaChannel: '',
      infoSource: '',
      receptionistName: '',
      homeAddress: '',
      marketerName: '',
      followupStatus: '待回访',
      customerTag: '',
      nextFollowDate: '',
      reservationAmount: undefined,
      reservationRoomNo: '',
      reservationChannel: '',
      reservationStatus: effectiveMode.value === 'reservation' ? '预定' : '',
      refunded: 0,
      orgName: '弋阳龟峰颐养中心',
      status: effectiveMode.value === 'reservation' ? 1 : (defaultStatus(effectiveMode.value) ?? 0)
    } as Partial<CrmLeadItem>)
  }
  open.value = true
}

async function submit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    if (submitting.value) return
    submitting.value = true
    const payload: Partial<CrmLeadItem> = {
      ...form,
      name: form.name || form.elderName || form.consultantName || '未命名线索',
      phone: form.phone || form.elderPhone || form.consultantPhone,
      status: effectiveMode.value === 'reservation'
        ? 1
        : (defaultStatus(effectiveMode.value) ?? form.status ?? 0)
    }
    if (form.id) {
      await updateCrmLead(form.id, payload)
    } else {
      await createCrmLead(payload)
    }
    message.success('保存成功')
    open.value = false
    fetchData()
  } catch {
    message.error('保存失败')
  } finally {
    submitting.value = false
  }
}

async function submitCallbackPlan() {
  const ids = selectedIds()
  if (!ids.length) {
    message.info('请先勾选客户')
    return
  }
  if (!planForm.title || !planForm.planExecuteTime) {
    message.error('请填写计划标题与执行时间')
    return
  }
  planSubmitting.value = true
  try {
    await Promise.all(ids.map((id) => createLeadCallbackPlan(id, { ...planForm })))
    message.success(`已新增 ${ids.length} 条回访计划`)
    planOpen.value = false
    fetchData()
  } finally {
    planSubmitting.value = false
  }
}

async function recoverRows(targetRows: CrmLeadItem[] = selectedRows.value) {
  if (!targetRows.length) {
    message.info('请先勾选要恢复的客户')
    return
  }
  const targets = targetRows.filter((item) => Number(item.status) !== 1)
  if (!targets.length) {
    message.info('选中客户已是意向状态，无需重复恢复')
    return
  }
  const ids = targets.map((item) => item.id)
  await batchUpdateLeadStatus({ ids, status: 1 })
  message.success(`已恢复 ${targets.length} 条客户（仅处理状态不同项）`)
  selectedRowKeys.value = []
  fetchData()
}

function openAssignModal(record: CrmLeadItem) {
  ensureSelectedStaff(record.ownerStaffId, record.ownerStaffName || record.marketerName)
  assignForm.ownerStaffId = record.ownerStaffId == null ? '' : String(record.ownerStaffId)
  assignForm.remark = ''
  assignOpen.value = true
}

async function submitAssign() {
  const row = requireSingleSelection('分配负责人')
  if (!row) return
  if (!assignForm.ownerStaffId) {
    message.warning('请选择负责人')
    return
  }
  assignSubmitting.value = true
  try {
    const target = staffOptions.value.find((item) => item.value === String(assignForm.ownerStaffId))
    await assignCrmLead(row.id, {
      ownerStaffId: Number(assignForm.ownerStaffId),
      ownerStaffName: target?.name,
      remark: assignForm.remark || undefined
    })
    message.success('负责人已更新')
    assignOpen.value = false
    fetchData()
  } finally {
    assignSubmitting.value = false
  }
}

async function openTraceDrawer(record: CrmLeadItem) {
  traceLead.value = record
  traceOpen.value = true
  traceLoading.value = true
  try {
    const [assignLogs, stageLogs] = await Promise.all([
      getLeadAssignLogs(record.id, 20),
      getLeadStageLogs(record.id, 20)
    ])
    leadAssignLogs.value = assignLogs || []
    leadStageLogs.value = stageLogs || []
  } catch {
    leadAssignLogs.value = []
    leadStageLogs.value = []
    message.warning('轨迹数据加载失败，请稍后重试')
  } finally {
    traceLoading.value = false
  }
}

function transferToAdmission(record: CrmLeadItem) {
  updateCrmLead(record.id, {
    ...record,
    status: 1,
    contractSignedFlag: 0,
    flowStage: 'PENDING_ASSESSMENT',
    currentOwnerDept: 'ASSESSMENT',
    contractStatus: record.contractStatus || '待评估',
    reservationStatus: record.reservationStatus || '预定'
  })
    .then(() => {
      message.success('已转入签约评估流程')
      router.push({
        path: '/marketing/contracts/pending',
        query: { flowStage: 'PENDING_ASSESSMENT' }
      })
    })
    .catch(() => message.error('转入住失败'))
}

async function executeCallback(record: CrmLeadItem) {
  try {
    const plans = await getLeadCallbackPlans(record.id)
    const pending = (plans || []).find((item) => item.status === 'PENDING')
    if (!pending) {
      message.info('该客户暂无待执行回访计划')
      return
    }
    pendingExecutePlanId.value = pending.id
    executeForm.executeNote = '页面执行'
    executeForm.callbackType = pending.callbackType || defaultCallbackType()
    executeForm.score = pending.score
    executeForm.followupResult = pending.followupResult || ''
    executeForm.nextFollowDate = record.nextFollowDate || ''
    executeOpen.value = true
  } catch {
    message.error('执行失败')
  }
}

async function submitExecuteCallback() {
  if (!pendingExecutePlanId.value) {
    message.error('未找到待执行计划')
    return
  }
  executeSubmitting.value = true
  try {
    await executeCallbackPlan(pendingExecutePlanId.value, {
      executeNote: executeForm.executeNote,
      callbackType: executeForm.callbackType,
      score: executeForm.score,
      followupResult: executeForm.followupResult,
      nextFollowDate: executeForm.nextFollowDate || undefined
    })
    message.success('已执行回访计划')
    executeOpen.value = false
    pendingExecutePlanId.value = undefined
    fetchData()
  } catch {
    message.error('执行失败')
  } finally {
    executeSubmitting.value = false
  }
}

function toggleRefund(record: CrmLeadItem) {
  updateCrmLead(record.id, { ...record, refunded: record.refunded === 1 ? 0 : 1 })
    .then(() => {
      message.success('退款状态已更新')
      fetchData()
    })
    .catch(() => message.error('更新失败'))
}

async function batchMoveToInvalid(targetRows: CrmLeadItem[] = selectedRows.value) {
  if (!targetRows.length) {
    message.info('请先勾选要放弃的客户')
    return
  }
  const targets = targetRows.filter((item) => Number(item.status) !== 3)
  if (!targets.length) {
    message.info('选中客户已是失效状态，无需重复操作')
    return
  }
  Modal.confirm({
    title: `确认放弃选中的 ${targets.length} 条客户吗？`,
    content: '放弃后将进入失效用户池，可后续恢复。',
    onOk: async () => {
      await batchUpdateLeadStatus({
        ids: targets.map((item) => item.id),
        status: 3,
        invalidTime: dayjs().format('YYYY-MM-DD HH:mm:ss')
      })
      message.success(`已放弃 ${targets.length} 条客户（仅处理状态不同项）`)
      selectedRowKeys.value = []
      fetchData()
    }
  })
}

async function batchMoveToIntent(targetRows: CrmLeadItem[] = selectedRows.value) {
  if (!targetRows.length) {
    message.info('请先勾选要转为意向的客户')
    return
  }
  const targets = targetRows.filter((item) => Number(item.status) !== 1)
  if (!targets.length) {
    message.info('选中客户已是意向状态，无需重复操作')
    return
  }
  const confirmed = await new Promise<boolean>((resolve) => {
    Modal.confirm({
      title: '确认转为意向客户？',
      content: `将 ${targets.length} 条线索转为意向客户，转换后进入意向跟进流程。`,
      okText: '确认转意向',
      cancelText: '取消',
      onOk: () => resolve(true),
      onCancel: () => resolve(false)
    })
  })
  if (!confirmed) return
  await batchUpdateLeadStatus({ ids: targets.map((item) => item.id), status: 1, followupStatus: '待回访' })
  message.success(`已转为意向客户 ${targets.length} 条（仅处理状态不同项）`)
  selectedRowKeys.value = []
  fetchData()
}

async function batchMoveToReservation(targetRows: CrmLeadItem[] = selectedRows.value) {
  if (!targetRows.length) {
    message.info('请先勾选要转预订的客户')
    return
  }
  const alreadyReserved = targetRows.filter(hasReservationEvidence)
  const missingEvidence = targetRows.filter((item) => !hasReservationEvidence(item))
  if (!missingEvidence.length && !targetRows.length) {
    message.info('选中客户已是预订状态，无需重复操作')
    return
  }
  if (missingEvidence.length) {
    if (missingEvidence.length > 1) {
      message.warning('批量转预订前，请先为每条客户补全预订房号、锁床床位或预订金额')
      return
    }
    const row = missingEvidence[0]
    message.info('请先补全预订房号、锁床床位或预订金额，再完成转预订')
    openForm({
      ...row,
      status: 1,
      reservationStatus: row.reservationStatus || '预定'
    })
    return
  }
  await Promise.all(
    alreadyReserved.map((item) =>
      updateCrmLead(item.id, {
        ...item,
        status: 1,
        reservationStatus: item.reservationStatus || '预定'
      })
    )
  )
  message.success(`已转入预订管理 ${alreadyReserved.length} 条（仅处理状态不同项）`)
  selectedRowKeys.value = []
  fetchData()
}

async function batchSetTodayFollow(targetRows: CrmLeadItem[] = selectedRows.value) {
  const rowsToUpdate = targetRows.filter((item) => String(item.nextFollowDate || '').slice(0, 10) !== todayText)
  if (!rowsToUpdate.length) {
    message.info('选中客户的回访日期已是今天，无需重复设置')
    return
  }
  await Promise.all(
    rowsToUpdate.map((item) =>
      updateCrmLead(item.id, {
        ...item,
        followupStatus: '待回访',
        nextFollowDate: todayText
      })
    )
  )
  message.success(`已设置 ${rowsToUpdate.length} 条为今日回访`)
  selectedRowKeys.value = []
  fetchData()
}

async function batchPostponeFollow(days: number, targetRows: CrmLeadItem[] = selectedRows.value) {
  const rowsToUpdate = targetRows.filter((item) => String(item.followupStatus || '') !== '已回访')
  if (!rowsToUpdate.length) {
    message.info('选中客户均为已回访，无需顺延')
    return
  }
  await Promise.all(
    rowsToUpdate.map((item) => {
      const current = String(item.nextFollowDate || '').slice(0, 10)
      const base = current && current > todayText ? current : todayText
      const nextFollowDate = dayjs(base).add(days, 'day').format('YYYY-MM-DD')
      return updateCrmLead(item.id, {
        ...item,
        followupStatus: '待回访',
        nextFollowDate
      })
    })
  )
  message.success(`已顺延 ${rowsToUpdate.length} 条回访计划（+${days}天）`)
  selectedRowKeys.value = []
  fetchData()
}

async function batchMarkFollowed(targetRows: CrmLeadItem[] = selectedRows.value) {
  const rowsToUpdate = targetRows.filter((item) => String(item.followupStatus || '') !== '已回访')
  if (!rowsToUpdate.length) {
    message.info('选中客户已全部为已回访状态')
    return
  }
  await Promise.all(
    rowsToUpdate.map((item) =>
      updateCrmLead(item.id, {
        ...item,
        followupStatus: '已回访',
        followupResult: item.followupResult || '已完成回访'
      })
    )
  )
  message.success(`已标记 ${rowsToUpdate.length} 条为已回访`)
  selectedRowKeys.value = []
  fetchData()
}

watch(
  () => route.query,
  () => {
    syncPipelineTabByRoute()
    syncQueryFiltersByRoute()
    query.pageNo = 1
    selectedRowKeys.value = []
    fetchData()
    const quick = String(route.query.quick || '').trim()
    if (quick === '1') {
      openForm()
      consumeQuickQuery()
    }
  }
)

onMounted(() => {
  syncPipelineTabByRoute()
  syncQueryFiltersByRoute()
  searchStaff('').catch(() => {})
  fetchData()
  const quick = String(route.query.quick || '').trim()
  if (quick === '1') {
    openForm()
    consumeQuickQuery()
  }
})
</script>

<style scoped>
.marketing-workspace {
  display: grid;
  gap: 18px;
  min-width: 0;
  padding: 18px;
  border-radius: 28px;
  border: 1px solid var(--border);
  background:
    radial-gradient(circle at top right, rgba(var(--info-rgb), 0.09), transparent 24%),
    radial-gradient(circle at left bottom, rgba(var(--warning-rgb), 0.12), transparent 30%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(247, 250, 252, 0.98) 100%);
  box-shadow: var(--shadow-md);
  overflow: hidden;
}

.marketing-workspace > * {
  min-width: 0;
}

.marketing-overview {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
  padding: 22px 24px;
  border-radius: 24px;
  border: 1px solid var(--border-soft);
  background:
    linear-gradient(135deg, rgba(242, 249, 255, 0.98) 0%, rgba(255, 255, 255, 0.98) 52%, rgba(255, 248, 237, 0.98) 100%);
}

.marketing-overview-copy {
  display: grid;
  gap: 8px;
  max-width: 720px;
}

.marketing-overview-kicker {
  color: var(--muted);
  font-size: 11px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.marketing-overview-copy strong {
  color: var(--ink);
  font-size: 26px;
  line-height: 1.08;
}

.marketing-overview-copy span:last-child {
  color: var(--muted);
  font-size: 14px;
  line-height: 1.7;
}

.marketing-overview-meta {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 10px;
}

.overview-pill {
  display: inline-flex;
  align-items: center;
  min-height: 36px;
  padding: 0 14px;
  border-radius: 999px;
  border: 1px solid var(--border);
  background: rgba(255, 255, 255, 0.86);
  color: var(--muted);
  font-size: 12px;
  font-weight: 600;
}

.overview-pill--accent {
  border-color: rgba(var(--warning-rgb), 0.5);
  background: linear-gradient(180deg, rgba(255, 245, 224, 0.96) 0%, rgba(255, 255, 255, 0.92) 100%);
  color: var(--warning);
}

.summary-strip {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.summary-strip-card {
  display: grid;
  gap: 10px;
  align-content: space-between;
  min-height: 108px;
  padding: 18px;
  border-radius: 20px;
  border: 1px solid var(--border-soft);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(245, 249, 252, 0.98) 100%);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.85);
}

.summary-card-label {
  color: var(--muted);
  font-size: 13px;
}

.summary-card-value {
  color: var(--ink);
  font-size: 34px;
  line-height: 1;
  letter-spacing: -0.04em;
}

.marketing-warning {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  min-height: 42px;
  margin-top: 12px;
  padding: 0 14px;
  border-radius: 999px;
  border: 1px solid rgba(var(--warning-rgb), 0.45);
  background: rgba(var(--warning-rgb), 0.1);
  color: var(--warning);
  font-size: 13px;
}

.marketing-warning-dot {
  width: 8px;
  height: 8px;
  border-radius: 999px;
  background: var(--warning);
  box-shadow: 0 0 0 4px rgba(var(--warning-rgb), 0.12);
}

.marketing-query-shell {
  min-width: 0;
  padding: 4px;
  border-radius: 24px;
  border: 1px solid var(--border-soft);
  background: rgba(255, 255, 255, 0.86);
}

.marketing-query-shell :deep(.search-form) {
  min-width: 0;
  max-width: 100%;
  margin: 0;
  border: 0;
  border-radius: 20px;
  box-shadow: none;
  background: linear-gradient(180deg, rgba(250, 252, 253, 0.96) 0%, rgba(255, 255, 255, 0.98) 100%);
}

.marketing-query-shell :deep(.search-form-card),
.marketing-query-shell :deep(.search-form-head),
.marketing-query-shell :deep(.search-copy),
.marketing-query-shell :deep(.search-side),
.marketing-query-shell :deep(.stateful-wrap),
.marketing-query-shell :deep(.stateful-panel) {
  min-width: 0;
  max-width: 100%;
}

.marketing-query-shell :deep(.search-form-head) {
  flex-wrap: wrap;
}

.marketing-query-shell :deep(.search-form__header) {
  margin-bottom: 18px;
}

.marketing-query-shell :deep(.search-form__title) {
  font-size: 22px;
}

.marketing-query-shell :deep(.search-actions) {
  flex-wrap: wrap;
}

.marketing-query-shell :deep(.search-form .ant-form-item) {
  flex: 1 1 220px;
  min-width: 0;
}

.marketing-query-shell :deep(.search-form .ant-form-item-control),
.marketing-query-shell :deep(.search-form .ant-form-item-control-input),
.marketing-query-shell :deep(.search-form .ant-form-item-control-input-content),
.marketing-query-shell :deep(.search-form .ant-input),
.marketing-query-shell :deep(.search-form .ant-input-number),
.marketing-query-shell :deep(.search-form .ant-input-number-group-wrapper),
.marketing-query-shell :deep(.search-form .ant-picker),
.marketing-query-shell :deep(.search-form .ant-select) {
  width: 100%;
  max-width: 100%;
}

.marketing-table-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 2px;
  margin-bottom: -6px;
}

.marketing-table-head strong {
  display: block;
  color: var(--ink);
  font-size: 18px;
}

.marketing-table-head span {
  color: var(--muted);
  font-size: 12px;
}

.marketing-table-head :deep(.ant-tag) {
  border-radius: 999px;
  padding-inline: 10px;
}

.marketing-workspace :deep(.list-toolbar) {
  margin: 0;
  border-radius: 18px;
}

.marketing-workspace :deep(.stateful-block) {
  border-radius: 22px;
  border-color: var(--border-soft);
}

.marketing-workspace :deep(.ant-table-wrapper) {
  width: 100%;
  max-width: 100%;
  padding: 4px 4px 0;
  overflow-x: auto;
}

.marketing-workspace :deep(.ant-spin-nested-loading),
.marketing-workspace :deep(.ant-spin-container),
.marketing-workspace :deep(.ant-table-container),
.marketing-workspace :deep(.ant-table-content) {
  min-width: 0;
  max-width: 100%;
}

.marketing-workspace :deep(.ant-table) {
  border-radius: 18px;
}

.marketing-workspace :deep(.ant-table-thead > tr > th) {
  color: var(--muted);
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  background: var(--surface-3);
}

.marketing-workspace :deep(.ant-table-tbody > tr > td) {
  padding-top: 14px;
  padding-bottom: 14px;
  vertical-align: top;
}

.marketing-workspace :deep(.ant-table-tbody > tr:hover > td) {
  background: var(--surface-2);
}

.person-cell,
.plan-cell {
  display: grid;
  gap: 4px;
}

.person-cell strong,
.plan-cell strong {
  color: var(--ink);
  font-size: 14px;
  font-weight: 700;
}

.person-cell span,
.plan-cell span {
  color: var(--muted);
  font-size: 12px;
  line-height: 1.5;
}

.row-action-links {
  display: flex;
  flex-wrap: wrap;
  gap: 2px 4px;
  min-width: 0;
  margin: -4px -8px;
}

.row-action-links :deep(.ant-btn-link) {
  padding-inline: 8px;
  border-radius: 999px;
  color: var(--primary);
  background: rgba(var(--primary-rgb), 0.08);
  white-space: nowrap;
}

.row-action-links :deep(.ant-btn-link.ant-btn-dangerous) {
  color: var(--danger);
  background: rgba(var(--danger-rgb), 0.08);
}

.marketing-pagination {
  margin-top: 18px;
  text-align: right;
}

.trace-item-title {
  color: var(--ink);
  font-weight: 600;
}

.trace-item-meta {
  color: var(--muted);
  font-size: 12px;
}

.trace-item-remark {
  color: var(--ink-soft);
  margin-top: 4px;
  word-break: break-word;
}

@media (max-width: 1200px) {
  .summary-strip {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .marketing-query-shell :deep(.search-form .ant-form-item) {
    flex-basis: calc(50% - 12px);
  }
}

@media (max-width: 992px) {
  .marketing-workspace {
    padding: 14px;
  }

  .marketing-overview {
    flex-direction: column;
    padding: 18px;
  }

  .marketing-overview-copy strong {
    font-size: 22px;
  }

  .marketing-overview-meta {
    justify-content: flex-start;
  }

  .marketing-query-shell :deep(.search-form .ant-form-item) {
    flex-basis: 100%;
  }
}

@media (max-width: 768px) {
  .summary-strip {
    grid-template-columns: 1fr;
  }

  .marketing-table-head {
    flex-direction: column;
    align-items: flex-start;
  }

  .marketing-query-shell :deep(.search-form__title) {
    font-size: 18px;
  }
}
</style>
