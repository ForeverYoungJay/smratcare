<template>
  <PageContainer :title="pageTitle" :subTitle="subTitle">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键字">
        <a-input v-model:value="query.keyword" :placeholder="keywordPlaceholder" allow-clear />
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="query.status" allow-clear style="width: 180px" :options="statusOptions" />
      </a-form-item>
      <a-form-item v-if="sceneMode === 'candidates'" label="用人意向">
        <a-select v-model:value="query.intentionStatus" allow-clear style="width: 180px" :options="intentionOptions" />
      </a-form-item>
      <a-form-item v-if="sceneMode === 'materials'" label="年度">
        <a-input-number v-model:value="materialsYear" :min="2020" :max="2100" style="width: 110px" />
      </a-form-item>
      <a-form-item v-if="sceneMode === 'materials'" label="部门">
        <a-input v-model:value="materialsDept" style="width: 180px" placeholder="部门名称（如护理部）" />
      </a-form-item>
      <template #extra>
        <a-space wrap>
          <a-button v-if="sceneMode === 'materials'" :loading="folderBootstrapping" @click="bootstrapFolders">一键建档案夹(年度/部门)</a-button>
          <a-button :disabled="selectedRowKeys.length === 0" @click="batchUpdateStatus('IN_PROGRESS')">批量进行中</a-button>
          <a-button :disabled="selectedRowKeys.length === 0" @click="batchUpdateStatus('DONE')">批量完成</a-button>
          <a-button danger :disabled="selectedRowKeys.length === 0" @click="batchUpdateStatus('REJECTED')">批量驳回</a-button>
          <a-button @click="onExportCsv">导出 CSV</a-button>
          <a-button @click="onExportExcel">导出 Excel</a-button>
          <a-button :disabled="!selectedSingle" @click="downloadSheet">下载办理单</a-button>
          <a-button :disabled="!selectedSingle" @click="printSheet">打印办理单</a-button>
          <a-button type="primary" @click="openDrawer()">新增</a-button>
          <a-button @click="applyStandardTemplate">标准模板填充</a-button>
          <a-button :disabled="!selectedSingle" @click="openDrawer(selectedSingle)">编辑</a-button>
          <a-button danger :disabled="!selectedSingle" @click="removeSelected">删除</a-button>
        </a-space>
      </template>
    </SearchForm>

    <DataTable
      rowKey="id"
      :row-selection="rowSelection"
      :columns="columns"
      :data-source="rows"
      :loading="loading"
      :pagination="pagination"
      :row-class-name="rowClassName"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="statusColor(record.status)">{{ statusText(record.status) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'intentionStatus'">
          <a-tag :color="intentionColor(record.intentionStatus)">{{ intentionText(record.intentionStatus) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'resumeUrl'">
          <a-button v-if="record.resumeUrl" type="link" @click="openLink(record.resumeUrl)">查看简历</a-button>
          <span v-else>-</span>
        </template>
        <template v-else-if="column.key === 'followUpStatus'">
          <a-tag :color="followUpTagColor(record)">{{ followUpStatusText(record) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'checklistJson'">
          {{ summarizeJsonList(record.checklistJson) }}
        </template>
      </template>
    </DataTable>

    <a-drawer v-model:open="drawerOpen" :title="drawerTitle" width="880">
      <a-form :model="form" layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="事项标题" required>
              <a-input v-model:value="form.title" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="状态">
              <a-select v-model:value="form.status" :options="statusOptions" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-divider v-if="sceneMode === 'candidates'" orientation="left">候选人信息</a-divider>
        <a-row v-if="sceneMode === 'candidates'" :gutter="16">
          <a-col :span="8"><a-form-item label="候选人姓名" required><a-input v-model:value="form.candidateName" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="联系方式"><a-input v-model:value="form.contactPhone" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="用人意向"><a-select v-model:value="form.intentionStatus" :options="intentionOptions" /></a-form-item></a-col>
        </a-row>
        <a-row v-if="sceneMode === 'candidates'" :gutter="16">
          <a-col :span="12"><a-form-item label="提醒日期"><a-date-picker v-model:value="followUpDate" style="width: 100%" /></a-form-item></a-col>
          <a-col :span="12">
            <a-form-item label="简历上传 (Word/PDF)">
              <a-upload :show-upload-list="false" :before-upload="beforeUploadResume" accept=".pdf,.doc,.docx">
                <a-button :loading="uploadingResume">上传简历</a-button>
              </a-upload>
              <div class="hint-text">{{ form.resumeUrl ? '已上传简历' : '支持 Word/PDF 文件' }}</div>
            </a-form-item>
          </a-col>
        </a-row>

        <a-divider v-if="sceneMode === 'onboarding'" orientation="left">入职办理</a-divider>
        <a-row v-if="sceneMode === 'onboarding'" :gutter="16">
          <a-col :span="8"><a-form-item label="姓名" required><a-input v-model:value="form.candidateName" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="部门"><a-input v-model:value="form.departmentName" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="岗位"><a-input v-model:value="form.positionName" /></a-form-item></a-col>
        </a-row>
        <a-row v-if="sceneMode === 'onboarding'" :gutter="16">
          <a-col :span="8"><a-form-item label="到岗时间"><a-date-picker v-model:value="onboardDate" style="width: 100%" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="薪资"><a-input v-model:value="form.salary" placeholder="如：6000/月" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="试用期"><a-input v-model:value="form.probationPeriod" placeholder="如：3个月" /></a-form-item></a-col>
        </a-row>
        <a-row v-if="sceneMode === 'onboarding'" :gutter="16">
          <a-col :span="8"><a-form-item label="工作地点"><a-input v-model:value="form.workLocation" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="班次"><a-input v-model:value="form.shiftType" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="录用通知状态"><a-select v-model:value="form.offerStatus" :options="offerOptions" /></a-form-item></a-col>
        </a-row>
        <a-form-item v-if="sceneMode === 'onboarding'" label="资料收集清单">
          <a-checkbox-group v-model:value="onboardingChecklist" :options="onboardingChecklistOptions" />
        </a-form-item>
        <a-row v-if="sceneMode === 'onboarding'" :gutter="16">
          <a-col :span="12"><a-form-item label="签署文件"><a-textarea v-model:value="form.signedFilesJson" :rows="2" placeholder="劳动合同、保密协议、制度签收..." /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="系统与权限开通"><a-textarea v-model:value="form.accountPermissionJson" :rows="2" placeholder="OA/护理系统/库存系统..." /></a-form-item></a-col>
        </a-row>
        <a-row v-if="sceneMode === 'onboarding'" :gutter="16">
          <a-col :span="12"><a-form-item label="物资发放签领"><a-textarea v-model:value="form.issuedItemsJson" :rows="2" placeholder="工服、钥匙、工牌、对讲机..." /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="带教师傅"><a-input v-model:value="form.mentorName" /></a-form-item></a-col>
        </a-row>
        <a-row v-if="sceneMode === 'onboarding'" :gutter="16">
          <a-col :span="12"><a-form-item label="试用期目标"><a-textarea v-model:value="form.probationGoal" :rows="2" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="转正建议"><a-textarea v-model:value="form.recommendationNote" :rows="2" /></a-form-item></a-col>
        </a-row>
        <a-form-item v-if="sceneMode === 'onboarding'" label="转正状态">
          <a-select v-model:value="form.regularizationStatus" :options="regularizationOptions" />
        </a-form-item>

        <a-divider v-if="sceneMode === 'offboarding'" orientation="left">退职办理</a-divider>
        <a-row v-if="sceneMode === 'offboarding'" :gutter="16">
          <a-col :span="8"><a-form-item label="姓名" required><a-input v-model:value="form.candidateName" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="离职类型"><a-select v-model:value="form.offboardingType" :options="offboardingTypeOptions" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="离职原因"><a-input v-model:value="form.resignationReason" /></a-form-item></a-col>
        </a-row>
        <a-row v-if="sceneMode === 'offboarding'" :gutter="16">
          <a-col :span="8"><a-form-item label="最后工作日"><a-date-picker v-model:value="lastWorkDate" style="width: 100%" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="交接截止"><a-date-picker v-model:value="handoverDeadline" style="width: 100%" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="离职报告上传"><a-upload :show-upload-list="false" :before-upload="beforeUploadResignation" accept=".pdf,.doc,.docx"><a-button :loading="uploadingResignation">上传报告</a-button></a-upload></a-form-item></a-col>
        </a-row>
        <a-row v-if="sceneMode === 'offboarding'" :gutter="16">
          <a-col :span="12"><a-form-item label="交接清单"><a-textarea v-model:value="form.handoverItemsJson" :rows="2" placeholder="在办事项、风险事项、老人交接..." /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="资产回收"><a-textarea v-model:value="form.assetRecoveryJson" :rows="2" placeholder="工牌/钥匙/设备/制服..." /></a-form-item></a-col>
        </a-row>
        <a-row v-if="sceneMode === 'offboarding'" :gutter="16">
          <a-col :span="12"><a-form-item label="权限回收计划"><a-textarea v-model:value="form.permissionRecycleJson" :rows="2" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="财务结算与证明"><a-textarea v-model:value="form.financeSettlementNote" :rows="2" /></a-form-item></a-col>
        </a-row>
        <a-form-item v-if="sceneMode === 'offboarding'" label="归档与风险标记">
          <a-textarea v-model:value="form.exitArchiveJson" :rows="2" placeholder="离职报告/交接确认/黑名单或复聘标记..." />
        </a-form-item>

        <a-divider v-if="sceneMode === 'materials'" orientation="left">资料收集</a-divider>
        <a-row v-if="sceneMode === 'materials'" :gutter="16">
          <a-col :span="8"><a-form-item label="姓名" required><a-input v-model:value="form.candidateName" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="部门"><a-input v-model:value="form.departmentName" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="到岗日期"><a-date-picker v-model:value="onboardDate" style="width: 100%" /></a-form-item></a-col>
        </a-row>
        <a-form-item v-if="sceneMode === 'materials'" label="资料清单（可多选）">
          <a-checkbox-group v-model:value="materialsChecklist" :options="onboardingChecklistOptions" />
        </a-form-item>
        <a-form-item v-if="sceneMode === 'materials'" label="备注">
          <a-textarea v-model:value="form.remark" :rows="2" />
        </a-form-item>

        <a-form-item label="备注" v-if="sceneMode !== 'materials'">
          <a-textarea v-model:value="form.remark" :rows="2" />
        </a-form-item>
      </a-form>
      <template #footer>
        <a-space>
          <a-button @click="drawerOpen = false">取消</a-button>
          <a-button type="primary" :loading="saving" @click="submit">提交</a-button>
        </a-space>
      </template>
    </a-drawer>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { message, Modal } from 'ant-design-vue'
import dayjs, { type Dayjs } from 'dayjs'
import { useRoute } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import {
  batchUpdateHrRecruitmentNeedStatus,
  bootstrapHrMaterialsFolders,
  createHrRecruitmentNeed,
  deleteHrRecruitmentNeed,
  getHrRecruitmentNeedPage,
  updateHrRecruitmentNeed
} from '../../api/hr'
import { createDocument, uploadOaFile } from '../../api/oa'
import type { HrRecruitmentNeedItem, PageResult } from '../../types'
import { resolveHrError } from './hrError'
import { formatChineseDate } from '../../utils/dateLocale'
import { exportCsv, exportExcel } from '../../utils/export'
import { mapByDict } from './hrExportFields'

const route = useRoute()

const query = reactive({
  keyword: undefined as string | undefined,
  status: undefined as string | undefined,
  intentionStatus: undefined as string | undefined,
  pageNo: 1,
  pageSize: 10
})

const statusOptions = [
  { label: '待处理', value: 'PENDING' },
  { label: '进行中', value: 'IN_PROGRESS' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已驳回', value: 'REJECTED' },
  { label: '已完成', value: 'DONE' }
]
const intentionOptions = [
  { label: '同意用人', value: 'APPROVE' },
  { label: '考虑中（提醒）', value: 'CONSIDERING' },
  { label: '不考虑', value: 'REJECT' }
]
const offerOptions = [
  { label: '待发放', value: 'PENDING' },
  { label: '已发放', value: 'ISSUED' },
  { label: '已确认', value: 'CONFIRMED' }
]
const regularizationOptions = [
  { label: '待申请', value: 'PENDING' },
  { label: '部门推荐中', value: 'DEPARTMENT_RECOMMENDING' },
  { label: 'HR复核中', value: 'HR_REVIEWING' },
  { label: '负责人审批中', value: 'LEADER_REVIEWING' },
  { label: '已转正', value: 'PASSED' },
  { label: '未通过', value: 'REJECTED' }
]
const offboardingTypeOptions = [
  { label: '员工主动离职', value: 'RESIGN' },
  { label: '协商解除', value: 'MUTUAL_TERMINATION' },
  { label: '辞退', value: 'DISMISS' },
  { label: '合同到期不续签', value: 'CONTRACT_EXPIRE' }
]
const onboardingChecklistOptions = [
  '身份证', '居住证明', '学历证明', '资格证', '体检/健康证', '无犯罪记录证明',
  '银行卡信息', '紧急联系人', '背调核验', '劳动合同', '保密协议', '员工手册承诺'
]

type RecruitmentScene = 'needs' | 'job-posting' | 'candidates' | 'interviews' | 'onboarding' | 'materials' | 'offboarding'

const sceneMode = computed<RecruitmentScene>(() => {
  const queryScene = String(route.query.scene || '').trim()
  if (queryScene === 'job-posting') return 'job-posting'
  if (queryScene === 'candidates') return 'candidates'
  if (queryScene === 'interviews') return 'interviews'
  if (queryScene === 'onboarding') return 'onboarding'
  if (queryScene === 'materials') return 'materials'
  if (queryScene === 'offboarding') return 'offboarding'
  if (route.name === 'HrRecruitmentJobPosting') return 'job-posting'
  if (route.name === 'HrRecruitmentCandidates') return 'candidates'
  if (route.name === 'HrRecruitmentInterviews') return 'interviews'
  if (route.name === 'HrRecruitmentOnboarding') return 'onboarding'
  if (route.name === 'HrRecruitmentMaterials') return 'materials'
  if (route.name === 'HrRecruitmentOffboarding') return 'offboarding'
  return 'needs'
})
const pageTitle = computed(() => {
  if (sceneMode.value === 'job-posting') return '岗位发布'
  if (sceneMode.value === 'candidates') return '候选人库'
  if (sceneMode.value === 'interviews') return '面试管理'
  if (sceneMode.value === 'onboarding') return '入职办理'
  if (sceneMode.value === 'materials') return '入职资料收集'
  if (sceneMode.value === 'offboarding') return '退职办理'
  return '招聘需求申请'
})
const subTitle = computed(() => {
  if (sceneMode.value === 'onboarding') return '人事行政 / 入职办理（通知、资料、权限、培训、转正）'
  if (sceneMode.value === 'offboarding') return '人事行政 / 退职办理（交接、回收、结算、归档）'
  if (sceneMode.value === 'materials') return '人事行政 / 入职资料收集（年度/部门档案夹）'
  return '人事行政 / 招聘管理'
})
const keywordPlaceholder = computed(() => {
  if (sceneMode.value === 'candidates') return '姓名/联系方式/备注'
  if (sceneMode.value === 'onboarding') return '姓名/部门/岗位'
  if (sceneMode.value === 'offboarding') return '姓名/离职原因/交接'
  return '标题/申请人/备注'
})
const routeScene = computed(() => (sceneMode.value === 'needs' ? undefined : sceneMode.value))

const columns = computed(() => {
  const common = [
    { title: '事项标题', dataIndex: 'title', key: 'title', width: 190 },
    { title: '状态', dataIndex: 'status', key: 'status', width: 120 },
    { title: '申请人', dataIndex: 'applicantName', key: 'applicantName', width: 120 },
    { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 }
  ]
  if (sceneMode.value === 'candidates') {
    return [
      { title: '候选人', dataIndex: 'candidateName', key: 'candidateName', width: 120 },
      { title: '联系方式', dataIndex: 'contactPhone', key: 'contactPhone', width: 140 },
      { title: '用人意向', dataIndex: 'intentionStatus', key: 'intentionStatus', width: 130 },
      { title: '提醒日期', dataIndex: 'followUpDate', key: 'followUpDate', width: 120 },
      { title: '提醒状态', key: 'followUpStatus', width: 120 },
      { title: '简历', dataIndex: 'resumeUrl', key: 'resumeUrl', width: 120 },
      ...common
    ]
  }
  if (sceneMode.value === 'onboarding') {
    return [
      { title: '姓名', dataIndex: 'candidateName', key: 'candidateName', width: 110 },
      { title: '部门', dataIndex: 'departmentName', key: 'departmentName', width: 120 },
      { title: '岗位', dataIndex: 'positionName', key: 'positionName', width: 120 },
      { title: '到岗时间', dataIndex: 'onboardDate', key: 'onboardDate', width: 120 },
      { title: '录用通知', dataIndex: 'offerStatus', key: 'offerStatus', width: 110 },
      { title: '转正状态', dataIndex: 'regularizationStatus', key: 'regularizationStatus', width: 130 },
      ...common
    ]
  }
  if (sceneMode.value === 'materials') {
    return [
      { title: '姓名', dataIndex: 'candidateName', key: 'candidateName', width: 110 },
      { title: '部门', dataIndex: 'departmentName', key: 'departmentName', width: 120 },
      { title: '到岗日期', dataIndex: 'onboardDate', key: 'onboardDate', width: 120 },
      { title: '资料清单', dataIndex: 'checklistJson', key: 'checklistJson', width: 320 },
      ...common
    ]
  }
  if (sceneMode.value === 'offboarding') {
    return [
      { title: '姓名', dataIndex: 'candidateName', key: 'candidateName', width: 110 },
      { title: '离职类型', dataIndex: 'offboardingType', key: 'offboardingType', width: 140 },
      { title: '最后工作日', dataIndex: 'lastWorkDate', key: 'lastWorkDate', width: 120 },
      { title: '交接截止', dataIndex: 'handoverDeadline', key: 'handoverDeadline', width: 120 },
      { title: '离职原因', dataIndex: 'resignationReason', key: 'resignationReason', width: 180 },
      ...common
    ]
  }
  return [
    { title: '需求标题', dataIndex: 'title', key: 'title', width: 190 },
    { title: '岗位', dataIndex: 'positionName', key: 'positionName', width: 120 },
    { title: '部门', dataIndex: 'departmentName', key: 'departmentName', width: 120 },
    { title: '人数', dataIndex: 'headcount', key: 'headcount', width: 80 },
    { title: '期望到岗', dataIndex: 'requiredDate', key: 'requiredDate', width: 120 },
    ...common
  ]
})

const rows = ref<HrRecruitmentNeedItem[]>([])
const loading = ref(false)
const saving = ref(false)
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const selectedRowKeys = ref<string[]>([])

const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: (string | number)[]) => {
    selectedRowKeys.value = keys.map((item) => String(item))
  }
}))
const selectedSingle = computed(() => {
  const selected = rows.value.filter((row) => selectedRowKeys.value.includes(String(row.id)))
  return selected.length === 1 ? selected[0] : undefined
})

const drawerOpen = ref(false)
const followUpDate = ref<Dayjs>()
const onboardDate = ref<Dayjs>()
const lastWorkDate = ref<Dayjs>()
const handoverDeadline = ref<Dayjs>()
const onboardingChecklist = ref<string[]>([])
const materialsChecklist = ref<string[]>([])
const form = reactive<Partial<HrRecruitmentNeedItem>>({
  status: 'PENDING',
  headcount: 1
})
const uploadingResume = ref(false)
const uploadingResignation = ref(false)

const materialsYear = ref(new Date().getFullYear())
const materialsDept = ref('护理部')
const folderBootstrapping = ref(false)

const drawerTitle = computed(() => `${form.id ? '编辑' : '新增'}${pageTitle.value}`)

function statusColor(status?: string) {
  if (status === 'APPROVED' || status === 'DONE') return 'green'
  if (status === 'REJECTED') return 'red'
  if (status === 'IN_PROGRESS') return 'gold'
  return 'blue'
}
function statusText(status?: string) {
  return statusOptions.find((item) => item.value === status)?.label || status || '-'
}
function intentionColor(v?: string) {
  if (v === 'APPROVE') return 'green'
  if (v === 'REJECT') return 'red'
  return 'gold'
}
function intentionText(v?: string) {
  return intentionOptions.find((item) => item.value === v)?.label || v || '-'
}

function followUpStatusText(record: HrRecruitmentNeedItem) {
  if (record.intentionStatus !== 'CONSIDERING') {
    return '无需提醒'
  }
  const follow = record.followUpDate ? dayjs(record.followUpDate) : null
  if (!follow || !follow.isValid()) {
    return '未设提醒'
  }
  const diffDays = follow.startOf('day').diff(dayjs().startOf('day'), 'day')
  if (diffDays < 0) return '已到期'
  if (diffDays <= 3) return '即将到期'
  return '计划中'
}

function followUpTagColor(record: HrRecruitmentNeedItem) {
  const status = followUpStatusText(record)
  if (status === '已到期') return 'red'
  if (status === '即将到期') return 'orange'
  if (status === '计划中') return 'blue'
  return 'default'
}

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<HrRecruitmentNeedItem> = await getHrRecruitmentNeedPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      keyword: query.keyword,
      status: query.status,
      scene: routeScene.value
    })
    rows.value = (res.list || []).filter((item) => !query.intentionStatus || item.intentionStatus === query.intentionStatus)
    pagination.total = res.total || rows.value.length
    selectedRowKeys.value = []
  } catch {
    rows.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

function handleTableChange(pag: any) {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  query.pageNo = pag.current
  query.pageSize = pag.pageSize
  fetchData()
}

function onReset() {
  query.keyword = undefined
  query.status = undefined
  query.intentionStatus = undefined
  query.pageNo = 1
  query.pageSize = 10
  pagination.current = 1
  pagination.pageSize = 10
  fetchData()
}

function resetForm() {
  form.id = undefined
  form.title = undefined
  form.positionName = undefined
  form.departmentName = undefined
  form.headcount = 1
  form.status = 'PENDING'
  form.remark = undefined
  form.candidateName = undefined
  form.contactPhone = undefined
  form.resumeUrl = undefined
  form.intentionStatus = 'CONSIDERING'
  form.offerStatus = 'PENDING'
  form.salary = undefined
  form.probationPeriod = undefined
  form.workLocation = undefined
  form.shiftType = undefined
  form.signedFilesJson = undefined
  form.accountPermissionJson = undefined
  form.issuedItemsJson = undefined
  form.mentorName = undefined
  form.probationGoal = undefined
  form.regularizationStatus = 'PENDING'
  form.recommendationNote = undefined
  form.offboardingType = 'RESIGN'
  form.resignationReason = undefined
  form.resignationReportUrl = undefined
  form.handoverItemsJson = undefined
  form.assetRecoveryJson = undefined
  form.permissionRecycleJson = undefined
  form.financeSettlementNote = undefined
  form.exitArchiveJson = undefined
  followUpDate.value = undefined
  onboardDate.value = undefined
  lastWorkDate.value = undefined
  handoverDeadline.value = undefined
  onboardingChecklist.value = []
  materialsChecklist.value = []
}

function openDrawer(record?: HrRecruitmentNeedItem) {
  resetForm()
  if (record) {
    Object.assign(form, record)
    followUpDate.value = record.followUpDate ? dayjs(record.followUpDate) : undefined
    onboardDate.value = record.onboardDate ? dayjs(record.onboardDate) : undefined
    lastWorkDate.value = record.lastWorkDate ? dayjs(record.lastWorkDate) : undefined
    handoverDeadline.value = record.handoverDeadline ? dayjs(record.handoverDeadline) : undefined
    onboardingChecklist.value = parseJsonList(record.checklistJson)
    materialsChecklist.value = parseJsonList(record.checklistJson)
  }
  if (!form.title) {
    form.title = `${pageTitle.value}-${formatChineseDate(new Date())}`
  }
  drawerOpen.value = true
}

async function submit() {
  if (!form.title?.trim()) {
    message.warning('请填写事项标题')
    return
  }
  if ((sceneMode.value === 'candidates' || sceneMode.value === 'onboarding' || sceneMode.value === 'offboarding' || sceneMode.value === 'materials') && !form.candidateName?.trim()) {
    message.warning('请填写姓名')
    return
  }
  const checklistValue = sceneMode.value === 'materials' ? materialsChecklist.value : onboardingChecklist.value
  const payload: Partial<HrRecruitmentNeedItem> = {
    ...form,
    scene: routeScene.value,
    followUpDate: followUpDate.value?.format('YYYY-MM-DD'),
    onboardDate: onboardDate.value?.format('YYYY-MM-DD'),
    lastWorkDate: lastWorkDate.value?.format('YYYY-MM-DD'),
    handoverDeadline: handoverDeadline.value?.format('YYYY-MM-DD'),
    checklistJson: checklistValue.length ? JSON.stringify(checklistValue) : undefined
  }
  if (!payload.requiredDate && onboardDate.value) {
    payload.requiredDate = onboardDate.value.format('YYYY-MM-DD')
  }
  saving.value = true
  try {
    if (form.id) {
      await updateHrRecruitmentNeed(form.id, payload)
      message.success('更新成功')
    } else {
      await createHrRecruitmentNeed(payload)
      message.success('创建成功')
      if (sceneMode.value === 'materials' && materialsDept.value) {
        await createMaterialDocument(payload)
      }
    }
    drawerOpen.value = false
    fetchData()
  } catch (error) {
    message.error(resolveHrError(error, '提交失败'))
  } finally {
    saving.value = false
  }
}

function applyStandardTemplate() {
  if (!drawerOpen.value) {
    openDrawer()
  }
  if (sceneMode.value === 'onboarding' || sceneMode.value === 'materials') {
    onboardingChecklist.value = [...onboardingChecklistOptions]
    if (sceneMode.value === 'materials') {
      materialsChecklist.value = [...onboardingChecklistOptions]
    }
    form.offerStatus = form.offerStatus || 'PENDING'
    form.regularizationStatus = form.regularizationStatus || 'PENDING'
    form.shiftType = form.shiftType || '白班/夜班轮转'
    form.workLocation = form.workLocation || '弋阳龟峰颐养中心'
    form.probationPeriod = form.probationPeriod || '3个月'
    message.success('已填充入职标准模板')
    return
  }
  if (sceneMode.value === 'offboarding') {
    form.offboardingType = form.offboardingType || 'RESIGN'
    form.handoverItemsJson = form.handoverItemsJson || '["在办事项","重点住养老人情况","风险事项","文件资料","资产与库存"]'
    form.assetRecoveryJson = form.assetRecoveryJson || '["工牌门禁","钥匙","制服","电脑/手机/对讲机"]'
    form.permissionRecycleJson = form.permissionRecycleJson || '最后工作日自动禁用账号；重要系统提前下线权限；保留审计日志'
    message.success('已填充退职标准模板')
    return
  }
  if (sceneMode.value === 'candidates') {
    form.intentionStatus = form.intentionStatus || 'CONSIDERING'
    message.success('已填充候选人默认模板')
  }
}

async function createMaterialDocument(payload: Partial<HrRecruitmentNeedItem>) {
  const fileUrl = payload.resumeUrl || payload.resignationReportUrl
  if (!fileUrl) return
  const docName = `${payload.candidateName || '未命名'}-${sceneMode.value === 'offboarding' ? '离职资料' : '入职资料'}`
  await createDocument({
    name: docName,
    folder: `人事行政档案/${materialsYear.value}/${materialsDept.value}/${sceneMode.value === 'offboarding' ? '退职资料归档' : '入职资料收集'}`,
    url: fileUrl,
    uploaderName: payload.applicantName || '系统',
    remark: payload.remark
  })
}

async function removeSelected() {
  if (!selectedSingle.value?.id) return
  Modal.confirm({
    title: '确认删除',
    content: '删除后不可恢复，确定继续吗？',
    onOk: async () => {
      try {
        await deleteHrRecruitmentNeed(selectedSingle.value!.id!)
        message.success('删除成功')
        fetchData()
      } catch (error) {
        message.error(resolveHrError(error, '删除失败'))
      }
    }
  })
}

async function batchUpdateStatus(status: string) {
  if (!selectedRowKeys.value.length) return
  const statusLabel = statusText(status)
  Modal.confirm({
    title: `确认批量更新为${statusLabel}`,
    content: `将处理 ${selectedRowKeys.value.length} 条记录`,
    onOk: async () => {
      try {
        const count = await batchUpdateHrRecruitmentNeedStatus(selectedRowKeys.value, status)
        message.success(`已更新 ${count || 0} 条`)
        fetchData()
      } catch (error) {
        message.error(resolveHrError(error, '批量更新失败'))
      }
    }
  })
}

async function beforeUploadResume(file: File) {
  const pass = /\.(pdf|doc|docx)$/i.test(file.name)
  if (!pass) {
    message.warning('仅支持 Word/PDF')
    return false
  }
  uploadingResume.value = true
  try {
    const res = await uploadOaFile(file, 'hr-candidate-resume')
    form.resumeUrl = res.fileUrl || ''
    message.success('简历上传成功')
  } finally {
    uploadingResume.value = false
  }
  return false
}

async function beforeUploadResignation(file: File) {
  const pass = /\.(pdf|doc|docx)$/i.test(file.name)
  if (!pass) {
    message.warning('仅支持 Word/PDF')
    return false
  }
  uploadingResignation.value = true
  try {
    const res = await uploadOaFile(file, 'hr-offboarding-report')
    form.resignationReportUrl = res.fileUrl || ''
    message.success('离职报告上传成功')
  } finally {
    uploadingResignation.value = false
  }
  return false
}

async function bootstrapFolders() {
  folderBootstrapping.value = true
  try {
    const result = await bootstrapHrMaterialsFolders({ year: materialsYear.value, departmentName: materialsDept.value })
    message.success(`建档完成：${result.folderPath || ''}`)
  } catch (error) {
    message.error(resolveHrError(error, '建档失败'))
  } finally {
    folderBootstrapping.value = false
  }
}

function summarizeJsonList(value?: string) {
  const list = parseJsonList(value)
  if (!list.length) return '-'
  if (list.length <= 3) return list.join('、')
  return `${list.slice(0, 3).join('、')} 等${list.length}项`
}

function parseJsonList(value?: string) {
  if (!value) return []
  try {
    const parsed = JSON.parse(value)
    return Array.isArray(parsed) ? parsed.map((item) => String(item)) : []
  } catch {
    return []
  }
}

function openLink(url?: string) {
  if (!url) return
  window.open(url, '_blank')
}

function rowClassName(record: HrRecruitmentNeedItem) {
  if (record.status === 'REJECTED') return 'hr-row-danger'
  if (record.status === 'PENDING') return 'hr-row-warning'
  return ''
}

function buildSheetContent(record: HrRecruitmentNeedItem) {
  const lines = [
    `标题：${record.title || '-'}`,
    `姓名：${record.candidateName || '-'}`,
    `状态：${statusText(record.status)}`,
    `部门：${record.departmentName || '-'}`,
    `岗位：${record.positionName || '-'}`,
    `到岗：${record.onboardDate || '-'}`,
    `薪资：${record.salary || '-'}`,
    `试用期：${record.probationPeriod || '-'}`,
    `用人意向：${intentionText(record.intentionStatus)}`,
    `离职类型：${record.offboardingType || '-'}`,
    `最后工作日：${record.lastWorkDate || '-'}`,
    `交接截止：${record.handoverDeadline || '-'}`,
    `资料清单：${summarizeJsonList(record.checklistJson)}`,
    `备注：${record.remark || '-'}`
  ]
  return lines.join('\n')
}

function downloadSheet() {
  if (!selectedSingle.value) return
  const content = buildSheetContent(selectedSingle.value)
  const blob = new Blob([content], { type: 'text/plain;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `${selectedSingle.value.candidateName || '办理单'}-${sceneMode.value}.txt`
  link.click()
  URL.revokeObjectURL(url)
}

function printSheet() {
  if (!selectedSingle.value) return
  const content = buildSheetContent(selectedSingle.value)
  const printWindow = window.open('', '_blank')
  if (!printWindow) return
  printWindow.document.write(`<pre style="font-family: sans-serif; padding: 16px; white-space: pre-wrap;">${content}</pre>`)
  printWindow.document.close()
  printWindow.focus()
  printWindow.print()
}

const recruitmentExportFields = [
  { key: 'title', label: '事项标题' },
  { key: 'candidateName', label: '姓名' },
  { key: 'departmentName', label: '部门' },
  { key: 'positionName', label: '岗位' },
  { key: 'status', label: '状态' },
  { key: 'onboardDate', label: '到岗时间' },
  { key: 'offboardingType', label: '离职类型' },
  { key: 'createTime', label: '创建时间' }
]

function onExportCsv() {
  exportCsv(mapByDict(rows.value as Record<string, any>[], recruitmentExportFields), `${pageTitle.value}-当前筛选`)
}

function onExportExcel() {
  exportExcel(mapByDict(rows.value as Record<string, any>[], recruitmentExportFields), `${pageTitle.value}-当前筛选`)
}

onMounted(fetchData)
watch(
  () => route.fullPath,
  () => onReset()
)
</script>

<style scoped>
:deep(.hr-row-danger) {
  background: #fff1f0 !important;
}
:deep(.hr-row-warning) {
  background: #fffbe6 !important;
}
.hint-text {
  margin-top: 8px;
  color: #8c8c8c;
  font-size: 12px;
}
</style>
