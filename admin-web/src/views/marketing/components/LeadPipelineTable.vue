<template>
  <PageContainer :title="title" :sub-title="subTitle">
    <MarketingQuickNav />
    <a-alert
      :type="canViewAllLeads ? 'info' : 'warning'"
      show-icon
      style="margin-bottom: 12px"
      :message="canViewAllLeads ? '当前账号可查看本机构营销线索。' : `当前账号仅查看本人创建线索（${currentUserDisplayName}）。`"
    />
    <a-alert
      show-icon
      type="info"
      style="margin-bottom: 12px"
      :message="moduleLogicMessage"
    />
    <a-card class="card-elevated" :bordered="false">
      <a-form :model="query" layout="inline" class="search-bar">
        <a-form-item v-if="props.mode === 'pipeline'" label="线索视图">
          <a-radio-group v-model:value="pipelineTab" button-style="solid" @change="onPipelineTabChange">
            <a-radio-button value="consultation">咨询</a-radio-button>
            <a-radio-button value="intent">意向</a-radio-button>
            <a-radio-button value="callback">待回访</a-radio-button>
          </a-radio-group>
        </a-form-item>

        <template v-if="effectiveMode === 'consultation'">
          <a-form-item label="咨询人姓名">
            <a-input v-model:value="query.consultantName" placeholder="请输入 咨询人姓名" allow-clear />
          </a-form-item>
          <a-form-item label="联系电话">
            <a-input v-model:value="query.consultantPhone" placeholder="请输入 联系电话" allow-clear />
          </a-form-item>
          <a-form-item label="老人姓名">
            <a-input v-model:value="query.elderName" placeholder="请输入 老人姓名" allow-clear />
          </a-form-item>
          <a-form-item label="老人联系电话">
            <a-input v-model:value="query.elderPhone" placeholder="请输入 老人联系电话" allow-clear />
          </a-form-item>
          <a-form-item label="咨询日期">
            <a-range-picker
              v-model:value="query.consultDateRange"
              value-format="YYYY-MM-DD"
              :placeholder="['开始日期', '结束日期']"
            />
          </a-form-item>
          <a-form-item label="咨询类型">
            <a-input v-model:value="query.consultType" placeholder="请选择 咨询类型" allow-clear />
          </a-form-item>
          <a-form-item label="媒体渠道">
            <a-input v-model:value="query.mediaChannel" placeholder="请选择 媒体渠道" allow-clear />
          </a-form-item>
        </template>

        <template v-else-if="effectiveMode === 'intent' || effectiveMode === 'invalid'">
          <a-form-item label="老人姓名">
            <a-input v-model:value="query.elderName" placeholder="请输入 老人姓名" allow-clear />
          </a-form-item>
          <a-form-item label="老人联系电话">
            <a-input v-model:value="query.elderPhone" placeholder="请输入 老人联系电话" allow-clear />
          </a-form-item>
          <a-form-item label="信息来源">
            <a-input v-model:value="query.infoSource" placeholder="请选择 信息来源" allow-clear />
          </a-form-item>
          <a-form-item label="客户标签">
            <a-input v-model:value="query.customerTag" placeholder="请选择 客户标签" allow-clear />
          </a-form-item>
          <a-form-item label="归属营销人员">
            <a-input v-model:value="query.marketerName" placeholder="请选择 归属营销人员" allow-clear />
          </a-form-item>
        </template>

        <template v-else-if="effectiveMode === 'reservation'">
          <a-form-item label="姓名">
            <a-input v-model:value="query.elderName" placeholder="请输入 姓名" allow-clear />
          </a-form-item>
          <a-form-item label="联系电话">
            <a-input v-model:value="query.elderPhone" placeholder="请输入 联系电话" allow-clear />
          </a-form-item>
        </template>

        <template v-else>
          <a-form-item label="客户姓名">
            <a-input v-model:value="query.elderName" placeholder="请输入 客户姓名" allow-clear />
          </a-form-item>
          <a-form-item label="联系电话">
            <a-input v-model:value="query.elderPhone" placeholder="请输入 联系电话" allow-clear />
          </a-form-item>
          <a-form-item label="跟进人">
            <a-input v-model:value="query.marketerName" placeholder="请输入 跟进人" allow-clear />
          </a-form-item>
        </template>

        <a-form-item>
          <a-space>
            <a-button type="primary" @click="onSearch">搜 索</a-button>
            <a-button @click="reset">清 空</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <StatefulBlock :loading="summaryLoading" :error="summaryError" :empty="false" @retry="fetchData">
      <a-row :gutter="[12, 12]" style="margin-top: 16px">
        <a-col v-for="card in summaryCards" :key="card.title" :xs="12" :lg="6">
          <a-card class="card-elevated" :bordered="false" size="small">
            <a-statistic :title="card.title" :value="card.value || 0" />
          </a-card>
        </a-col>
      </a-row>
      <a-alert
        v-if="warningMessage"
        type="warning"
        show-icon
        style="margin-top: 12px"
        :message="warningMessage"
      />
    </StatefulBlock>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <MarketingListToolbar
        :selected-count="selectedCount"
        :tip="selectedCount > 0 ? '批量状态操作仅处理状态不同项' : '可通过勾选后执行批量操作'"
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
          :scroll="{ x: 1600 }"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'gender'">
              {{ genderText(record.gender) }}
            </template>
            <template v-else-if="column.key === 'refunded'">
              {{ record.refunded === 1 ? '是' : '否' }}
            </template>
            <template v-else-if="column.key === 'reservationAmount'">
              {{ record.reservationAmount == null ? '-' : `¥${Number(record.reservationAmount).toFixed(2)}` }}
            </template>
          </template>
        </a-table>
        <a-pagination
          style="margin-top: 16px; text-align: right;"
          :current="query.pageNo"
          :page-size="query.pageSize"
          :total="displayTotal"
          show-size-changer
          @change="onPageChange"
          @showSizeChange="onPageSizeChange"
        />
      </StatefulBlock>
    </a-card>

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
            <a-form-item label="归属营销人员">
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
              <a-input v-model:value="form.followupStatus" placeholder="已回访/未回访" />
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
            <a-select-option value="checkin">入住后回访</a-select-option>
            <a-select-option value="trial">试住回访</a-select-option>
            <a-select-option value="discharge">退住回访</a-select-option>
            <a-select-option value="score">满意度评分</a-select-option>
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
            <a-select-option value="checkin">入住后回访</a-select-option>
            <a-select-option value="trial">试住回访</a-select-option>
            <a-select-option value="discharge">退住回访</a-select-option>
            <a-select-option value="score">满意度评分</a-select-option>
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
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'ant-design-vue'
import { message, Modal } from 'ant-design-vue'
import dayjs from 'dayjs'
import PageContainer from '../../../components/PageContainer.vue'
import StatefulBlock from '../../../components/StatefulBlock.vue'
import MarketingQuickNav from './MarketingQuickNav.vue'
import MarketingListToolbar from './MarketingListToolbar.vue'
import {
  batchDeleteLeads,
  batchUpdateLeadStatus,
  createCrmLead,
  createLeadCallbackPlan,
  executeCallbackPlan,
  getLeadCallbackPlans,
  getMarketingLeadEntrySummary,
  getLeadPage,
  updateCrmLead
} from '../../../api/marketing'
import { useUserStore } from '../../../stores/user'
import { hasMinisterOrHigher } from '../../../utils/roleAccess'
import type { CrmLeadItem, Id, MarketingCallbackType, MarketingLeadEntrySummary, MarketingLeadMode, PageResult } from '../../../types'

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

const open = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<Partial<CrmLeadItem>>({})

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
const moduleLogicMessage = computed(() => {
  if (effectiveMode.value === 'callback') {
    return '回访管理和跟进管理都基于 crm_lead 线索池联动 crm_callback_plan；这里可以直接新增计划、执行回访和推进转预订。'
  }
  return '线索数据统一来自 crm_lead：咨询=status 0，意向=status 1，预订=status 2，失效=status 3；签约与入住阶段改在合同模块处理，所以不会在这里直接展示已签合同。'
})

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
      { title: '家庭地址', dataIndex: 'homeAddress', key: 'homeAddress', width: 160 },
      { title: '所属机构', dataIndex: 'orgName', key: 'orgName', width: 120 }
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
      { title: '归属营销人员', dataIndex: 'marketerName', key: 'marketerName', width: 140 },
      { title: '所属机构', dataIndex: 'orgName', key: 'orgName', width: 120 }
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
      { title: '营销人员', dataIndex: 'marketerName', key: 'marketerName', width: 120 },
      { title: '收款时间', dataIndex: 'paymentTime', key: 'paymentTime', width: 170 },
      { title: '是否退款', dataIndex: 'refunded', key: 'refunded', width: 100 },
      { title: '预约渠道', dataIndex: 'reservationChannel', key: 'reservationChannel', width: 120 },
      { title: '状态', dataIndex: 'reservationStatus', key: 'reservationStatus', width: 100 },
      { title: '所属机构', dataIndex: 'orgName', key: 'orgName', width: 120 }
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
      { title: '归属营销人员', dataIndex: 'marketerName', key: 'marketerName', width: 140 },
      { title: '失效时间', dataIndex: 'invalidTime', key: 'invalidTime', width: 170 },
      { title: '所属机构', dataIndex: 'orgName', key: 'orgName', width: 120 }
    ]
  }
  return [
    { title: '客户姓名', dataIndex: 'elderName', key: 'elderName', width: 140 },
    { title: '联系电话', dataIndex: 'elderPhone', key: 'elderPhone', width: 140 },
    { title: '跟进人', dataIndex: 'marketerName', key: 'marketerName', width: 120 },
    { title: '计划执行时间', dataIndex: 'nextFollowDate', key: 'nextFollowDate', width: 170 },
    { title: '计划标题', dataIndex: 'planTitle', key: 'planTitle', width: 160 },
    { title: '回访内容', dataIndex: 'followupContent', key: 'followupContent', width: 220 },
    { title: '回访结果', dataIndex: 'followupResult', key: 'followupResult', width: 220 },
    { title: '所属机构', dataIndex: 'orgName', key: 'orgName', width: 120 }
  ]
})

const actionButtons = computed(() => {
  const mode = effectiveMode.value
  if (mode === 'consultation') {
    return [
      { key: 'create', label: '新增', type: 'primary' as const },
      { key: 'edit', label: '编辑', type: 'default' as const, disabled: selectedCount.value !== 1 },
      { key: 'detail', label: '查看更多', type: 'default' as const, disabled: selectedCount.value !== 1 },
      { key: 'setIntent', label: '设为意向', type: 'default' as const, disabled: selectedCount.value === 0 },
      { key: 'abandon', label: '放弃客户', type: 'default' as const, disabled: selectedCount.value === 0, danger: true },
      { key: 'delete', label: '删除', type: 'default' as const, disabled: selectedCount.value === 0, danger: true }
    ]
  }
  if (mode === 'intent') {
    return [
      { key: 'create', label: '新增', type: 'primary' as const },
      { key: 'edit', label: '编辑', type: 'default' as const, disabled: selectedCount.value !== 1 },
      { key: 'detail', label: '查看更多', type: 'default' as const, disabled: selectedCount.value !== 1 },
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
      { key: 'detail', label: '查看更多', type: 'default' as const, disabled: selectedCount.value !== 1 },
      { key: 'toggleRefund', label: '退款切换', type: 'default' as const, disabled: selectedCount.value !== 1 },
      { key: 'toAdmission', label: '转入住', type: 'default' as const, disabled: selectedCount.value !== 1 },
      { key: 'delete', label: '删除', type: 'default' as const, disabled: selectedCount.value === 0, danger: true }
    ]
  }
  if (mode === 'invalid') {
    return [
      { key: 'detail', label: '查看详情', type: 'default' as const, disabled: selectedCount.value !== 1 },
      { key: 'recover', label: '恢复客户', type: 'primary' as const, disabled: selectedCount.value === 0 },
      { key: 'delete', label: '删除', type: 'default' as const, disabled: selectedCount.value === 0, danger: true }
    ]
  }
  if (mode === 'callback') {
    return [
      { key: 'edit', label: '编辑', type: 'default' as const, disabled: selectedCount.value !== 1 },
      { key: 'detail', label: '查看详情', type: 'default' as const, disabled: selectedCount.value !== 1 },
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
    { key: 'detail', label: '查看详情', type: 'default' as const, disabled: selectedCount.value !== 1 },
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

const displayRows = computed(() => rows.value.map((item, index) => {
  const snapshot = callbackSnapshot.value[item.id] || {}
  return {
    ...item,
    index: index + 1,
    planTitle: snapshot.title || item.remark,
    followupContent: snapshot.followupContent,
    followupResult: snapshot.followupResult
  }
}))
const displayTotal = computed(() => total.value)
const summaryCards = computed(() => {
  const mode = effectiveMode.value
  if (mode === 'consultation') {
    return [
      { title: '当前咨询池', value: summary.modeCount },
      { title: '总线索数', value: summary.totalCount },
      { title: '缺失来源', value: summary.missingSourceCount },
      { title: '非标准来源', value: summary.nonStandardSourceCount }
    ]
  }
  if (mode === 'intent') {
    return [
      { title: '当前意向客户', value: summary.modeCount },
      { title: '待回访总量', value: summary.callbackPendingCount },
      { title: '逾期回访', value: summary.callbackOverdueCount },
      { title: '缺少下次回访', value: summary.missingNextFollowDateCount }
    ]
  }
  if (mode === 'reservation') {
    return [
      { title: '当前预订客户', value: summary.modeCount },
      { title: '已签约', value: summary.signedContractCount },
      { title: '待签约预订', value: summary.unsignedReservationCount },
      { title: '退款预订', value: summary.refundedReservationCount }
    ]
  }
  if (mode === 'invalid') {
    return [
      { title: '失效客户', value: summary.modeCount },
      { title: '咨询客户', value: summary.consultCount },
      { title: '意向客户', value: summary.intentCount },
      { title: '预订客户', value: summary.reservationCount }
    ]
  }
  return [
    { title: '待回访总量', value: summary.modeCount },
    { title: '今日到期', value: summary.callbackDueTodayCount },
    { title: '逾期回访', value: summary.callbackOverdueCount },
    { title: '缺少下次回访', value: summary.missingNextFollowDateCount }
  ]
})

const warningMessage = computed(() => {
  const mode = effectiveMode.value
  if (mode === 'consultation' && (summary.missingSourceCount > 0 || summary.nonStandardSourceCount > 0)) {
    return `数据质量提醒：缺失来源 ${summary.missingSourceCount} 条，非标准来源 ${summary.nonStandardSourceCount} 条。`
  }
  if (mode === 'intent' && (summary.callbackOverdueCount > 0 || summary.missingNextFollowDateCount > 0)) {
    return `跟进风险提醒：逾期回访 ${summary.callbackOverdueCount} 条，缺失下次回访日期 ${summary.missingNextFollowDateCount} 条。`
  }
  if (mode === 'reservation' && (summary.unsignedReservationCount > 0 || summary.refundedReservationCount > 0)) {
    return `签约闭环提醒：待签约预订 ${summary.unsignedReservationCount} 条，退款预订 ${summary.refundedReservationCount} 条。`
  }
  if (mode === 'callback' && (summary.callbackOverdueCount > 0 || summary.callbackDueTodayCount > 0)) {
    return `回访提醒：今日到期 ${summary.callbackDueTodayCount} 条，逾期 ${summary.callbackOverdueCount} 条。`
  }
  return ''
})

const canViewAllLeads = computed(() => hasMinisterOrHigher(userStore.roles || []))
const currentUserDisplayName = computed(() => {
  const realName = String(userStore.staffInfo?.realName || '').trim()
  if (realName) return realName
  return String(userStore.staffInfo?.username || '').trim() || '当前用户'
})

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
    const infoSource = String(item.infoSource || '')
    const mediaChannel = String(item.mediaChannel || '')
    const customerTag = String(item.customerTag || '')
    const reservationStatus = String(item.reservationStatus || '')
    const snapshotType = String(snapshot.callbackType || '').trim().toLowerCase()

    if (callbackType && effectiveMode.value === 'callback' && snapshotType !== callbackType) {
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
    if (filter === 'missing_followup' && String(item.followupStatus || '').trim()) return false
    if (filter === 'unsigned_lock') {
      if (!textContains(reservationStatus, '锁')) return false
      if (item.contractSignedFlag) return false
    }

    if (scenario === 'blacklist') {
      if (!textContains(customerTag, '黑')) return false
    }
    if (scenario === 'source_unknown') {
      if (infoSource.trim()) return false
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
    if (stage === 'evaluation' && Number(item.status) !== 1) return false
    if (stage === 'signing' && Number(item.status) !== 2) return false
    if (stage === 'lost' && Number(item.status) !== 3) return false
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
  if (mode === 'reservation') return 2
  if (mode === 'invalid') return 3
  return undefined
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
  const normalizedSource = query.infoSource || routeSource || undefined
  const callbackType = isCallback ? String(props.callbackType || route.query.type || '').trim() : ''
  return {
    pageNo: query.pageNo,
    pageSize: query.pageSize,
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
    infoSource: query.infoSource || undefined,
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
    const [page, summaryRes] = await Promise.all([
      getLeadPage(buildQueryParams()),
      getMarketingLeadEntrySummary(buildSummaryParams())
    ])
    const rawList = (page.list || []).map((item) => ({ ...item, id: String(item.id) }))
    Object.assign(summary, summaryRes || {})
    const snapshots = await hydrateCallbackSnapshot(rawList)
    rows.value = applyScenarioFilters(rawList, snapshots)
    total.value = rows.value.length
  } catch (error: any) {
    const text = error?.message || '请求失败，请稍后重试'
    tableError.value = text
    summaryError.value = text
  } finally {
    loading.value = false
    summaryLoading.value = false
  }
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
    viewMore(row)
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
    const selectedIdsValue = selectedIds()
    if (!selectedIdsValue.length) {
      message.info('请先勾选要恢复的客户')
      return
    }
    const targets = selectedRows.value.filter((item) => Number(item.status) !== 1)
    if (!targets.length) {
      message.info('选中客户已是意向状态，无需重复恢复')
      return
    }
    const ids = targets.map((item) => item.id)
    await batchUpdateLeadStatus({ ids, status: 1 })
    message.success(`已恢复 ${targets.length} 条客户（仅处理状态不同项）`)
    selectedRowKeys.value = []
    fetchData()
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
    const ids = selectedIds()
    if (!ids.length) {
      message.info('请先勾选要添加计划的客户')
      return
    }
    planForm.title = '回访'
    planForm.callbackType = defaultCallbackType()
    planForm.followupContent = ''
    planForm.planExecuteTime = dayjs().format('YYYY-MM-DD HH:mm:ss')
    planForm.executorName = ''
    planOpen.value = true
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
    const ids = selectedIds()
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
      followupStatus: '未回访',
      customerTag: '',
      nextFollowDate: '',
      reservationAmount: undefined,
      reservationRoomNo: '',
      reservationChannel: '',
      reservationStatus: effectiveMode.value === 'reservation' ? '预定' : '',
      refunded: 0,
      orgName: '弋阳龟峰颐养中心',
      status: defaultStatus(effectiveMode.value) ?? 0
    } as Partial<CrmLeadItem>)
  }
  open.value = true
}

async function submit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    submitting.value = true
    const payload: Partial<CrmLeadItem> = {
      ...form,
      name: form.name || form.elderName || form.consultantName || '未命名线索',
      phone: form.phone || form.elderPhone || form.consultantPhone,
      status: defaultStatus(effectiveMode.value) ?? form.status ?? 0
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

function viewMore(record: CrmLeadItem) {
  Modal.info({
    title: '客户详情',
    content: `${record.elderName || '-'} / ${record.elderPhone || '-'} / ${record.remark || '-'}`
  })
}

function transferToAdmission(record: CrmLeadItem) {
  updateCrmLead(record.id, { ...record, contractSignedFlag: 1, contractSignedAt: dayjs().format('YYYY-MM-DD HH:mm:ss') })
    .then(() => {
      message.success('已标记转入住')
      router.push('/marketing/contract-signing')
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

async function batchMoveToInvalid() {
  const ids = selectedIds()
  if (!ids.length) {
    message.info('请先勾选要放弃的客户')
    return
  }
  const targets = rows.value.filter((item) => ids.some((id) => sameId(item.id, id)) && Number(item.status) !== 3)
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

async function batchMoveToIntent() {
  const ids = selectedIds()
  if (!ids.length) {
    message.info('请先勾选要转为意向的客户')
    return
  }
  const targets = rows.value.filter((item) => ids.some((id) => sameId(item.id, id)) && Number(item.status) !== 1)
  if (!targets.length) {
    message.info('选中客户已是意向状态，无需重复操作')
    return
  }
  await batchUpdateLeadStatus({ ids: targets.map((item) => item.id), status: 1, followupStatus: '待回访' })
  message.success(`已转为意向客户 ${targets.length} 条（仅处理状态不同项）`)
  selectedRowKeys.value = []
  fetchData()
}

async function batchMoveToReservation() {
  const ids = selectedIds()
  if (!ids.length) {
    message.info('请先勾选要转预订的客户')
    return
  }
  const selectedRows = rows.value.filter((item) => ids.some((id) => sameId(item.id, id)) && Number(item.status) !== 2)
  if (!selectedRows.length) {
    message.info('选中客户已是预订状态，无需重复操作')
    return
  }
  await Promise.all(
    selectedRows.map((item) =>
      updateCrmLead(item.id, {
        ...item,
        status: 2,
        reservationStatus: item.reservationStatus || '预定'
      })
    )
  )
  message.success(`已转入预订管理 ${selectedRows.length} 条（仅处理状态不同项）`)
  selectedRowKeys.value = []
  fetchData()
}

async function batchSetTodayFollow() {
  const targetRows = selectedRows.value
    .filter((item) => String(item.nextFollowDate || '').slice(0, 10) !== todayText)
  if (!targetRows.length) {
    message.info('选中客户的回访日期已是今天，无需重复设置')
    return
  }
  await Promise.all(
    targetRows.map((item) =>
      updateCrmLead(item.id, {
        ...item,
        followupStatus: '待回访',
        nextFollowDate: todayText
      })
    )
  )
  message.success(`已设置 ${targetRows.length} 条为今日回访`)
  selectedRowKeys.value = []
  fetchData()
}

async function batchPostponeFollow(days: number) {
  const targetRows = selectedRows.value
    .filter((item) => String(item.followupStatus || '') !== '已回访')
  if (!targetRows.length) {
    message.info('选中客户均为已回访，无需顺延')
    return
  }
  await Promise.all(
    targetRows.map((item) => {
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
  message.success(`已顺延 ${targetRows.length} 条回访计划（+${days}天）`)
  selectedRowKeys.value = []
  fetchData()
}

async function batchMarkFollowed() {
  const targetRows = selectedRows.value
    .filter((item) => String(item.followupStatus || '') !== '已回访')
  if (!targetRows.length) {
    message.info('选中客户已全部为已回访状态')
    return
  }
  await Promise.all(
    targetRows.map((item) =>
      updateCrmLead(item.id, {
        ...item,
        followupStatus: '已回访',
        followupResult: item.followupResult || '已完成回访'
      })
    )
  )
  message.success(`已标记 ${targetRows.length} 条为已回访`)
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
  fetchData()
  const quick = String(route.query.quick || '').trim()
  if (quick === '1') {
    openForm()
    consumeQuickQuery()
  }
})
</script>

<style scoped>
.search-bar {
  margin-bottom: 12px;
}
</style>
