<template>
  <a-drawer :open="open" width="560" class="c360" :body-style="{ paddingTop: '8px' }" @close="emit('update:open', false)">
    <template #title>
      <div v-if="lead" class="c360__head">
        <span class="c360__avatar">{{ (displayName || '客').slice(0, 1) }}</span>
        <div>
          <div class="c360__name">{{ displayName }} <small>{{ ageGender }}</small></div>
          <div class="c360__sub">客户编号 {{ lead.id }} <a-tag v-if="intentLabel" color="orange">{{ intentLabel }}</a-tag></div>
        </div>
      </div>
    </template>

    <a-tabs v-model:active-key="activeTab">
      <a-tab-pane key="info" tab="客户信息">
        <section class="rm-card c360__block">
          <h4>客户与家属信息</h4>
          <a-descriptions :column="1" size="small">
            <a-descriptions-item label="长者姓名">{{ lead?.elderName || '-' }}</a-descriptions-item>
            <a-descriptions-item label="年龄 / 性别">{{ ageGender || '-' }}</a-descriptions-item>
            <a-descriptions-item label="家属联系人">{{ lead?.consultantName || '-' }}</a-descriptions-item>
            <a-descriptions-item label="联系电话">{{ maskPhone(lead?.consultantPhone || lead?.elderPhone) }}</a-descriptions-item>
            <a-descriptions-item label="线索来源">{{ marketingInfoSourceLabel(lead?.infoSource || lead?.source) }}</a-descriptions-item>
            <a-descriptions-item label="归属员工">{{ lead?.ownerStaffName || lead?.marketerName || '未分配' }}</a-descriptions-item>
          </a-descriptions>
        </section>
        <section class="rm-card c360__block">
          <h4>入住意向与客户标签</h4>
          <a-space wrap style="margin-bottom: 8px">
            <a-tag v-if="intentLabel" color="orange">{{ intentLabel }}</a-tag>
            <a-tag v-for="tag in tags" :key="tag" color="green">{{ tag }}</a-tag>
            <a-tag v-if="lead?.consultType" color="blue">{{ lead.consultType }}</a-tag>
          </a-space>
          <a-descriptions :column="1" size="small">
            <a-descriptions-item label="跟进状态">{{ lead?.followupStatus || '-' }}</a-descriptions-item>
            <a-descriptions-item label="下次跟进">{{ lead?.nextFollowDate || '未安排' }}</a-descriptions-item>
          </a-descriptions>
        </section>
        <section v-if="lead?.remark" class="rm-card c360__block">
          <h4>最近沟通摘要</h4>
          <p class="c360__muted">{{ lead.remark }}</p>
        </section>
      </a-tab-pane>

      <a-tab-pane key="followup" tab="跟进与回访">
        <section class="rm-card c360__block">
          <h4>快速新增跟进记录</h4>
          <a-form layout="horizontal" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
            <a-form-item label="跟进方式">
              <a-select v-model:value="form.method" :options="METHODS.map((value) => ({ value }))" />
            </a-form-item>
            <a-form-item label="本次结果">
              <a-select v-model:value="form.followupResult" :options="RESULTS.map((value) => ({ value }))" />
            </a-form-item>
            <a-form-item label="回访内容">
              <a-textarea v-model:value="form.followupContent" :rows="3" placeholder="记录客户需求、顾虑和沟通结果" />
            </a-form-item>
            <a-form-item label="下次回访">
              <a-date-picker v-model:value="form.nextFollowTime" show-time value-format="YYYY-MM-DD HH:mm:ss" format="YYYY-MM-DD HH:mm" style="width: 100%" placeholder="不填则不创建提醒" />
            </a-form-item>
            <div style="text-align: right">
              <a-button type="primary" :loading="saving" @click="saveFollowup">{{ form.nextFollowTime ? '保存并创建下次提醒' : '保存跟进记录' }}</a-button>
            </div>
          </a-form>
        </section>
        <h4 class="c360__title">跟进时间线</h4>
        <a-spin :spinning="loading">
          <a-empty v-if="!timeline.length" description="暂无跟进记录" />
          <a-timeline v-else>
            <a-timeline-item v-for="item in timeline" :key="item.key" :color="item.kind === 'plan' ? 'orange' : item.kind === 'followup' ? 'green' : 'gray'">
              <div class="c360__muted">{{ formatTime(item.time) }}<template v-if="item.meta"> · {{ item.meta }}</template></div>
              <div class="c360__item-title">{{ item.title }}</div>
              <div v-if="item.content" class="c360__item-body">{{ item.content }}</div>
            </a-timeline-item>
          </a-timeline>
        </a-spin>
      </a-tab-pane>

      <a-tab-pane key="visits" tab="参观与预订">
        <section class="rm-card c360__block">
          <h4>参观预约</h4>
          <a-empty v-if="!visits.length" description="暂无参观记录" />
          <div v-for="visit in visits" :key="visit.id" class="c360__row">
            <div>
              <div class="c360__item-title">{{ formatTime(visit.appointmentTime) }} · {{ visit.visitorName }}（{{ visit.partySize || 1 }}人）</div>
              <div class="c360__muted">接待 {{ visit.ownerStaffName || '-' }}<template v-if="visit.visitResult"> · {{ visit.visitResult }}</template></div>
            </div>
            <a-tag :color="VISIT_STATUS[visit.status]?.[1]">{{ VISIT_STATUS[visit.status]?.[0] || visit.status }}</a-tag>
          </div>
        </section>
        <section class="rm-card c360__block">
          <h4>床位预订</h4>
          <a-descriptions :column="1" size="small">
            <a-descriptions-item label="预订状态">{{ lead?.reservationStatus || '未预订' }}</a-descriptions-item>
            <a-descriptions-item label="预订房号">{{ lead?.reservationRoomNo || '-' }}</a-descriptions-item>
            <a-descriptions-item label="定金">{{ lead?.reservationAmount ? `¥${lead.reservationAmount}` : '-' }}</a-descriptions-item>
          </a-descriptions>
          <p class="c360__muted">预订占用不等于正式入住；床位以床位管理实时状态为准。</p>
        </section>
      </a-tab-pane>

      <a-tab-pane key="contract" tab="合同进度">
        <section class="rm-card c360__block">
          <a-empty v-if="!linkage?.contractNo" description="尚未发起合同" />
          <a-descriptions v-else :column="1" size="small">
            <a-descriptions-item label="合同编号">{{ linkage.contractNo }}</a-descriptions-item>
            <a-descriptions-item label="办理阶段">{{ stageLabel(linkage.flowStage) || '-' }}</a-descriptions-item>
            <a-descriptions-item label="合同状态">{{ linkage.contractStatus || '-' }}</a-descriptions-item>
            <a-descriptions-item label="签署时间">{{ linkage.contractSignedAt || '-' }}</a-descriptions-item>
            <a-descriptions-item label="到期日">{{ linkage.contractExpiryDate || '-' }}</a-descriptions-item>
            <a-descriptions-item label="入住日期">{{ linkage.admissionDate || '-' }}</a-descriptions-item>
            <a-descriptions-item label="附件">{{ linkage.attachmentCount ?? 0 }} 份</a-descriptions-item>
          </a-descriptions>
          <p class="c360__muted">收付款以财务确认为准，市场部仅查看状态。</p>
        </section>
      </a-tab-pane>
    </a-tabs>

    <a-alert class="c360__privacy" type="warning" show-icon message="仅显示已授权的联系信息，电话默认脱敏；完整身份信息由对应岗位按需获取。" />

    <template #footer>
      <div class="c360__footer">
        <a-button @click="emit('update:open', false)">关闭</a-button>
        <a-button type="primary" @click="primaryAction.run">{{ primaryAction.label }}</a-button>
      </div>
    </template>
  </a-drawer>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import {
  getContractLinkageByLead, getLeadAssignLogs, getLeadCallbackPlans, getLeadStageLogs,
  getVisitAppointmentPage, recordLeadFollowup
} from '../../../api/marketing'
import { marketingInfoSourceLabel } from '../../../utils/marketingEnums'
import type { ContractLinkageSummary, CrmLeadItem, CrmVisitAppointmentItem } from '../../../types'
import { buildCustomerTimeline, formatTime, maskPhone, stageLabel, type TimelineItem } from './customerModel'
import '../../elder/residentUi.css'

const props = defineProps<{ open: boolean; lead?: CrmLeadItem; initialTab?: string }>()
const emit = defineEmits<{ 'update:open': [boolean]; changed: [] }>()
const router = useRouter()

const METHODS = ['电话回访', '微信沟通', '到院面谈', '上门拜访']
const RESULTS = ['意向持续，需继续跟进', '意向提升，可安排参观', '已约定参观时间', '暂缓考虑', '无意向/已选择其他机构', '未联系上']
const VISIT_STATUS: Record<string, [string, string]> = {
  SCHEDULED: ['已预约', 'blue'], ARRIVED: ['已到访', 'orange'], COMPLETED: ['已完成', 'green'], CANCELLED: ['已取消', 'default'], NO_SHOW: ['未到访', 'red']
}

const activeTab = ref('info')
const loading = ref(false)
const saving = ref(false)
const timeline = ref<TimelineItem[]>([])
const visits = ref<CrmVisitAppointmentItem[]>([])
const linkage = ref<ContractLinkageSummary | null>(null)
const form = reactive({ method: METHODS[0], followupResult: RESULTS[0], followupContent: '', nextFollowTime: '' as string | undefined })

const displayName = computed(() => props.lead?.elderName || props.lead?.consultantName || props.lead?.name || '')
const ageGender = computed(() => [props.lead?.age ? `${props.lead.age}岁` : '', props.lead?.gender === 1 ? '男' : props.lead?.gender === 2 ? '女' : ''].filter(Boolean).join(' · '))
const intentLabel = computed(() => stageLabel(props.lead?.status))
const tags = computed(() => String(props.lead?.customerTag || '').split(/[,，、\s]+/).filter(Boolean))

const primaryAction = computed(() => {
  const leadId = props.lead?.id
  if (activeTab.value === 'followup' || activeTab.value === 'visits') {
    return { label: '前往参观预约', run: () => router.push({ path: '/marketing/visits', query: { leadId: String(leadId), action: 'create' } }) }
  }
  if (activeTab.value === 'contract') {
    return { label: '前往合同办理', run: () => router.push({ path: '/marketing/contracts', query: { leadId: String(leadId) } }) }
  }
  return { label: '新增跟进记录', run: () => { activeTab.value = 'followup' } }
})

async function loadTimeline(leadId: CrmLeadItem['id']) {
  const [plans, stages, assigns] = await Promise.all([
    getLeadCallbackPlans(leadId).catch(() => []),
    getLeadStageLogs(leadId, 20).catch(() => []),
    getLeadAssignLogs(leadId, 20).catch(() => [])
  ])
  timeline.value = buildCustomerTimeline(plans || [], stages || [], assigns || [])
}

async function load() {
  const leadId = props.lead?.id
  if (!leadId) return
  loading.value = true
  try {
    const [, visitPage, contract] = await Promise.all([
      loadTimeline(leadId),
      getVisitAppointmentPage({ pageNo: 1, pageSize: 20, leadId }, { silent403: true }).catch(() => null),
      getContractLinkageByLead(leadId).catch(() => null)
    ])
    visits.value = visitPage?.list || []
    linkage.value = contract
  } finally {
    loading.value = false
  }
}

async function saveFollowup() {
  const leadId = props.lead?.id
  if (!leadId) return
  saving.value = true
  try {
    await recordLeadFollowup(leadId, { ...form, nextFollowTime: form.nextFollowTime || undefined })
    message.success(form.nextFollowTime ? '已保存，并创建下次回访提醒' : '跟进记录已保存')
    form.followupContent = ''
    await loadTimeline(leadId)
    emit('changed')
  } finally {
    saving.value = false
  }
}

watch(() => [props.open, props.lead?.id], () => {
  if (!props.open) return
  activeTab.value = ['info', 'followup', 'visits', 'contract'].includes(props.initialTab || '') ? props.initialTab! : 'info'
  form.followupContent = ''
  form.nextFollowTime = dayjs().add(1, 'day').hour(10).minute(0).second(0).format('YYYY-MM-DD HH:mm:ss')
  timeline.value = []; visits.value = []; linkage.value = null
  load()
}, { immediate: true })
</script>

<style scoped>
.c360__head { display: flex; align-items: center; gap: 12px; }
.c360__avatar { display: grid; place-items: center; width: 44px; height: 44px; border-radius: 50%; background: #f6ead7; color: #b8894a; font-size: 20px; font-weight: 600; }
.c360__name { font-size: 18px; font-weight: 600; }
.c360__name small { margin-left: 6px; color: #6b7a74; font-size: 12px; font-weight: 400; }
.c360__sub { color: #6b7a74; font-size: 12px; font-weight: 400; }
.c360__block { margin-bottom: 12px; }
.c360__block h4, .c360__title { margin: 0 0 10px; font-size: 14px; font-weight: 600; }
.c360__muted { margin: 0; color: #6b7a74; font-size: 12px; }
.c360__item-title { font-weight: 600; }
.c360__item-body { margin-top: 4px; padding: 8px 10px; border-radius: 8px; background: #f4f8f6; color: #4a5a54; font-size: 12px; }
.c360__row { display: flex; justify-content: space-between; align-items: center; gap: 8px; padding: 8px 0; border-bottom: 1px solid #eef2f0; }
.c360__row:last-child { border-bottom: 0; }
.c360__privacy { margin-top: 4px; }
.c360__footer { display: flex; justify-content: flex-end; gap: 8px; }
</style>
