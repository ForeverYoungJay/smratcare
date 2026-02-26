<template>
  <PageContainer :title="title" :subTitle="subTitle">
    <a-card class="card-elevated" :bordered="false">
      <a-form :model="query" layout="inline" class="search-bar">
        <a-form-item :label="pageConfig.keywordLabel">
          <a-input v-model:value="query.keyword" :placeholder="pageConfig.keywordPlaceholder" allow-clear />
        </a-form-item>
        <a-form-item v-if="pageConfig.showStatusFilter" label="状态">
          <a-select v-model:value="query.status" allow-clear style="width: 140px">
            <a-select-option value="DRAFT">草稿</a-select-option>
            <a-select-option value="COMPLETED">已完成</a-select-option>
            <a-select-option value="ARCHIVED">已归档</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item v-if="pageConfig.showArchiveTypeFilter" label="评估类型">
          <a-select v-model:value="query.archiveType" allow-clear style="width: 160px" placeholder="请选择评估类型">
            <a-select-option v-for="item in assessmentTypeOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item v-if="pageConfig.showContentKeyword" label="评估内容">
          <a-input v-model:value="query.contentKeyword" placeholder="请输入评估内容" allow-clear />
        </a-form-item>
        <a-form-item v-if="pageConfig.showDateFilter" :label="pageConfig.dateLabel">
          <a-range-picker v-model:value="query.dateRange" value-format="YYYY-MM-DD" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="onSearch">搜索</a-button>
            <a-button @click="reset">清空</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <div class="table-actions">
        <a-space>
          <a-button v-if="pageConfig.showCreateButton" type="primary" @click="openForm()">
            {{ pageConfig.createButtonText }}
          </a-button>
          <a-button v-if="pageConfig.showBatchDelete" danger :disabled="selectedRowKeys.length === 0" @click="removeBatch">
            删除
          </a-button>
          <a-button v-if="pageConfig.showExportButton" @click="exportCsv">导出Excel</a-button>
        </a-space>
      </div>
      <a-table
        :data-source="rows"
        :columns="tableColumns"
        :loading="loading"
        :pagination="false"
        row-key="id"
        :row-selection="rowSelection"
        :locale="{ emptyText: '暂无数据' }"
        :scroll="{ x: 1300, y: 520 }"
      >
        <template #bodyCell="{ column, record, index }">
          <template v-if="column.key === 'index'">
            {{ index + 1 + (query.pageNo - 1) * query.pageSize }}
          </template>
          <template v-else-if="column.key === 'assessmentType'">
            {{ assessmentTypeLabel(record.assessmentTypeLabel || record.assessmentType) }}
          </template>
          <template v-else-if="column.key === 'gender'">
            {{ record.genderLabel || (record.gender === 1 ? '男' : record.gender === 2 ? '女' : '-') }}
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="statusColor(record.status)">{{ record.statusLabel || statusLabel(record.status) }}</a-tag>
          </template>
          <template v-else-if="column.key === 'completedFlag'">
            {{ record.status === 'COMPLETED' || record.status === 'ARCHIVED' ? '是' : '否' }}
          </template>
          <template v-else-if="column.key === 'orgName'">
            {{ orgDisplayName(record) }}
          </template>
          <template v-else-if="column.key === 'assessmentCode'">
            {{ record.archiveNo || record.id || '-' }}
          </template>
          <template v-else-if="column.key === 'assessmentContent'">
            {{ record.levelCode || '-' }}
          </template>
          <template v-else-if="column.key === 'assessmentTime'">
            {{ record.updateTime || record.assessmentDate || '-' }}
          </template>
          <template v-else-if="column.key === 'assessmentPlace'">
            {{ record.source || '-' }}
          </template>
          <template v-else-if="column.key === 'score'">
            {{ record.score ?? '-' }}
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button v-if="pageConfig.actions.includes('assign')" type="link" @click="assignRecord(record)">指派</a-button>
              <a-button v-if="pageConfig.actions.includes('edit')" type="link" @click="openForm(record)">编辑</a-button>
              <a-button v-if="pageConfig.actions.includes('view')" type="link" @click="openForm(record, true)">查看</a-button>
              <a-button v-if="pageConfig.actions.includes('delete')" type="link" danger @click="remove(record.id)">删除</a-button>
            </a-space>
          </template>
          <template v-else>
            {{ displayCell(record, column.dataIndex) }}
          </template>
        </template>
      </a-table>
      <a-pagination
        style="margin-top: 16px; text-align: right;"
        :current="query.pageNo"
        :page-size="query.pageSize"
        :total="total"
        show-size-changer
        @change="onPageChange"
        @showSizeChange="onPageSizeChange"
      />
    </a-card>

    <a-modal
      v-model:open="open"
      :title="formReadonly ? title + '查看' : title + '登记'"
      width="680px"
      :confirm-loading="submitting"
      :ok-button-props="{ disabled: formReadonly }"
      :ok-text="formReadonly ? '已查看' : '保存'"
      cancel-text="关闭"
      @ok="submit"
      @cancel="() => (open = false)"
    >
      <a-form ref="formRef" :model="form" :rules="rules" layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="老人姓名" name="elderId">
              <a-select
                v-model:value="form.elderId"
                :disabled="formReadonly"
                allow-clear
                show-search
                :filter-option="false"
                placeholder="请选择老人"
                @search="searchElderOptions"
                @focus="() => loadElderOptions()"
                @change="onElderChange"
              >
                <a-select-option v-for="item in elderOptions" :key="item.value" :value="item.value">
                  {{ item.label }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="评估等级">
              <a-input v-model:value="form.levelCode" placeholder="如 A/B/C 或 轻/中/重" :disabled="formReadonly" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="评估日期" name="assessmentDate">
              <a-date-picker v-model:value="form.assessmentDate" value-format="YYYY-MM-DD" style="width: 100%" :disabled="formReadonly" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="下次评估日期">
              <a-date-picker
                v-model:value="form.nextAssessmentDate"
                value-format="YYYY-MM-DD"
                style="width: 100%"
                :disabled="formReadonly"
              />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="16">
            <a-form-item label="量表模板">
              <a-select v-model:value="form.templateId" allow-clear placeholder="可选，选择后可自动算分" :disabled="formReadonly">
                <a-select-option v-for="tpl in templates" :key="tpl.id" :value="tpl.id">
                  {{ tpl.templateName }} ({{ tpl.templateCode }})
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="自动评分">
              <a-switch v-model:checked="scoreAutoChecked" :disabled="formReadonly" @change="onScoreAutoChange" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="评估分值">
              <a-input-number v-model:value="form.score" :min="0" :max="999" :step="0.5" style="width: 100%" :disabled="formReadonly" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="评估状态" name="status">
              <a-select v-model:value="form.status" :disabled="formReadonly">
                <a-select-option value="DRAFT">草稿</a-select-option>
                <a-select-option value="COMPLETED">已完成</a-select-option>
                <a-select-option value="ARCHIVED">已归档</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="评估人">
              <a-input v-model:value="form.assessorName" :disabled="formReadonly" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="档案编号">
              <a-input v-model:value="form.archiveNo" :disabled="formReadonly" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="评估结论">
          <a-textarea v-model:value="form.resultSummary" :rows="3" placeholder="简要结论" :disabled="formReadonly" />
        </a-form-item>
        <a-form-item label="护理建议">
          <a-textarea v-model:value="form.suggestion" :rows="3" placeholder="后续护理建议" :disabled="formReadonly" />
        </a-form-item>
        <a-form-item label="量表明细(JSON)">
          <a-textarea
            v-model:value="form.detailJson"
            :rows="4"
            placeholder='如 {"orientation":6,"registration":5}'
            :disabled="formReadonly"
          />
          <div style="margin-top: 8px;">
            <a-space>
              <a-button size="small" :disabled="formReadonly" @click="previewScore">试算分值</a-button>
              <span class="hint">选择模板并填写明细后，可自动计算分值和等级。</span>
            </a-space>
          </div>
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'ant-design-vue'
import { message, Modal } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import { useElderOptions } from '../../composables/useElderOptions'
import {
  getAssessmentRecordPage,
  createAssessmentRecord,
  updateAssessmentRecord,
  deleteAssessmentRecord,
  batchDeleteAssessmentRecord,
  exportAssessmentRecord,
  getAssessmentTemplateList,
  previewAssessmentScore
} from '../../api/assessment'
import type { AssessmentRecord, AssessmentType, AssessmentScaleTemplate, PageResult } from '../../types'

const props = defineProps<{
  title: string
  subTitle: string
  assessmentType: AssessmentType
}>()

const loading = ref(false)
const rows = ref<AssessmentRecord[]>([])
const total = ref(0)
const templates = ref<AssessmentScaleTemplate[]>([])
const {
  elderOptions,
  searchElders: loadElderOptions,
  findElderName,
  ensureSelectedElder
} = useElderOptions({ pageSize: 30 })

type ActionType = 'assign' | 'edit' | 'view' | 'delete'

interface TableColumnDef {
  title: string
  key: string
  dataIndex?: string
  width?: number
  fixed?: 'left' | 'right'
}

interface PageConfig {
  keywordLabel: string
  keywordPlaceholder: string
  dateLabel: string
  showStatusFilter: boolean
  showDateFilter: boolean
  showArchiveTypeFilter: boolean
  showContentKeyword: boolean
  showCreateButton: boolean
  showBatchDelete: boolean
  showExportButton: boolean
  createButtonText: string
  actions: ActionType[]
  columns: TableColumnDef[]
}

const assessmentTypeOptions = [
  { value: 'ADMISSION', label: '入住评估' },
  { value: 'REGISTRATION', label: '评估登记' },
  { value: 'CONTINUOUS', label: '持续评估' },
  { value: 'ARCHIVE', label: '评估档案' },
  { value: 'OTHER_SCALE', label: '其他量表评估' },
  { value: 'COGNITIVE', label: '认知能力评估' },
  { value: 'SELF_CARE', label: '自理能力评估' }
]

const pageConfigMap: Record<AssessmentType, PageConfig> = {
  ADMISSION: {
    keywordLabel: '老人姓名',
    keywordPlaceholder: '请输入老人姓名',
    dateLabel: '评估日期',
    showStatusFilter: false,
    showDateFilter: false,
    showArchiveTypeFilter: false,
    showContentKeyword: false,
    showCreateButton: true,
    showBatchDelete: false,
    showExportButton: false,
    createButtonText: '新增',
    actions: ['assign', 'edit', 'view', 'delete'],
    columns: [
      { title: '序号', key: 'index', width: 70, fixed: 'left' },
      { title: '老人姓名', key: 'elderName', dataIndex: 'elderName', width: 130 },
      { title: '评估人', key: 'assessorName', dataIndex: 'assessorName', width: 120 },
      { title: '评估日期', key: 'assessmentDate', dataIndex: 'assessmentDate', width: 130 },
      { title: '是否完成评估', key: 'completedFlag', width: 130 },
      { title: '评估结果', key: 'resultSummary', dataIndex: 'resultSummary', width: 220 },
      { title: '评估得分', key: 'score', dataIndex: 'score', width: 100 },
      { title: '所属机构', key: 'orgName', width: 140 },
      { title: '指派人', key: 'assigneeName', dataIndex: 'assessorName', width: 120 },
      { title: '操作', key: 'action', width: 220, fixed: 'right' }
    ]
  },
  REGISTRATION: {
    keywordLabel: '用户姓名',
    keywordPlaceholder: '请输入用户姓名',
    dateLabel: '评估日期',
    showStatusFilter: false,
    showDateFilter: false,
    showArchiveTypeFilter: false,
    showContentKeyword: false,
    showCreateButton: true,
    showBatchDelete: false,
    showExportButton: false,
    createButtonText: '新增',
    actions: ['edit', 'view', 'delete'],
    columns: [
      { title: '序号', key: 'index', width: 70, fixed: 'left' },
      { title: '用户姓名', key: 'elderName', dataIndex: 'elderName', width: 120 },
      { title: '评估类型', key: 'assessmentType', dataIndex: 'assessmentType', width: 140 },
      { title: '评估结果', key: 'resultSummary', dataIndex: 'resultSummary', width: 220 },
      { title: '评估人', key: 'assessorName', dataIndex: 'assessorName', width: 120 },
      { title: '评估地点', key: 'assessmentPlace', width: 140 },
      { title: '评估日期', key: 'assessmentDate', dataIndex: 'assessmentDate', width: 130 },
      { title: '所属机构', key: 'orgName', width: 140 },
      { title: '操作', key: 'action', width: 180, fixed: 'right' }
    ]
  },
  CONTINUOUS: {
    keywordLabel: '老人姓名',
    keywordPlaceholder: '请输入老人姓名',
    dateLabel: '评估日期',
    showStatusFilter: false,
    showDateFilter: false,
    showArchiveTypeFilter: false,
    showContentKeyword: false,
    showCreateButton: true,
    showBatchDelete: true,
    showExportButton: false,
    createButtonText: '新增',
    actions: ['edit'],
    columns: [
      { title: '序号', key: 'index', width: 70, fixed: 'left' },
      { title: '老人姓名', key: 'elderName', dataIndex: 'elderName', width: 130 },
      { title: '评估人', key: 'assessorName', dataIndex: 'assessorName', width: 120 },
      { title: '评估日期', key: 'assessmentDate', dataIndex: 'assessmentDate', width: 130 },
      { title: '是否完成评估', key: 'completedFlag', width: 130 },
      { title: '评估结果', key: 'resultSummary', dataIndex: 'resultSummary', width: 220 },
      { title: '评估得分', key: 'score', dataIndex: 'score', width: 100 },
      { title: '所属机构', key: 'orgName', width: 140 },
      { title: '操作', key: 'action', width: 120, fixed: 'right' }
    ]
  },
  ARCHIVE: {
    keywordLabel: '老人姓名',
    keywordPlaceholder: '请输入老人姓名',
    dateLabel: '评估日期',
    showStatusFilter: false,
    showDateFilter: true,
    showArchiveTypeFilter: true,
    showContentKeyword: false,
    showCreateButton: false,
    showBatchDelete: false,
    showExportButton: false,
    createButtonText: '新增',
    actions: ['view'],
    columns: [
      { title: '序号', key: 'index', width: 70, fixed: 'left' },
      { title: '老人姓名', key: 'elderName', dataIndex: 'elderName', width: 130 },
      { title: '性别', key: 'gender', dataIndex: 'gender', width: 90 },
      { title: '年龄', key: 'age', dataIndex: 'age', width: 90 },
      { title: '家庭地址', key: 'address', dataIndex: 'address', width: 260 },
      { title: '评估类型', key: 'assessmentType', dataIndex: 'assessmentType', width: 140 },
      { title: '评估日期', key: 'assessmentDate', dataIndex: 'assessmentDate', width: 130 },
      { title: '评估结论', key: 'resultSummary', dataIndex: 'resultSummary', width: 220 },
      { title: '所属机构', key: 'orgName', width: 140 },
      { title: '操作', key: 'action', width: 100, fixed: 'right' }
    ]
  },
  OTHER_SCALE: {
    keywordLabel: '老人姓名',
    keywordPlaceholder: '请输入老人姓名',
    dateLabel: '评估日期',
    showStatusFilter: false,
    showDateFilter: false,
    showArchiveTypeFilter: false,
    showContentKeyword: true,
    showCreateButton: true,
    showBatchDelete: false,
    showExportButton: true,
    createButtonText: '新增',
    actions: ['edit', 'delete', 'view'],
    columns: [
      { title: '序号', key: 'index', width: 70, fixed: 'left' },
      { title: '评估编码', key: 'assessmentCode', width: 180 },
      { title: '老人姓名', key: 'elderName', dataIndex: 'elderName', width: 120 },
      { title: '性别', key: 'gender', dataIndex: 'gender', width: 90 },
      { title: '年龄', key: 'age', dataIndex: 'age', width: 90 },
      { title: '手机号', key: 'phone', dataIndex: 'phone', width: 130 },
      { title: '评估内容', key: 'assessmentContent', width: 180 },
      { title: '评估时间', key: 'assessmentTime', width: 170 },
      { title: '评估人', key: 'assessorName', dataIndex: 'assessorName', width: 120 },
      { title: '评估结果', key: 'resultSummary', dataIndex: 'resultSummary', width: 180 },
      { title: '所属机构', key: 'orgName', width: 140 },
      { title: '操作', key: 'action', width: 180, fixed: 'right' }
    ]
  },
  COGNITIVE: {
    keywordLabel: '老人姓名',
    keywordPlaceholder: '请输入老人姓名',
    dateLabel: '评估日期',
    showStatusFilter: false,
    showDateFilter: true,
    showArchiveTypeFilter: false,
    showContentKeyword: false,
    showCreateButton: false,
    showBatchDelete: false,
    showExportButton: false,
    createButtonText: '新增',
    actions: [],
    columns: [
      { title: '序号', key: 'index', width: 70, fixed: 'left' },
      { title: '老人姓名', key: 'elderName', dataIndex: 'elderName', width: 130 },
      { title: '性别', key: 'gender', dataIndex: 'gender', width: 90 },
      { title: '年龄', key: 'age', dataIndex: 'age', width: 90 },
      { title: '家庭地址', key: 'address', dataIndex: 'address', width: 260 },
      { title: '评估日期', key: 'assessmentDate', dataIndex: 'assessmentDate', width: 130 },
      { title: '评估结果', key: 'resultSummary', dataIndex: 'resultSummary', width: 220 },
      { title: '所属机构', key: 'orgName', width: 140 },
      { title: '操作', key: 'action', width: 100, fixed: 'right' }
    ]
  },
  SELF_CARE: {
    keywordLabel: '老人姓名',
    keywordPlaceholder: '请输入老人姓名',
    dateLabel: '评估日期',
    showStatusFilter: false,
    showDateFilter: true,
    showArchiveTypeFilter: false,
    showContentKeyword: false,
    showCreateButton: false,
    showBatchDelete: false,
    showExportButton: false,
    createButtonText: '新增',
    actions: [],
    columns: [
      { title: '序号', key: 'index', width: 70, fixed: 'left' },
      { title: '老人姓名', key: 'elderName', dataIndex: 'elderName', width: 130 },
      { title: '性别', key: 'gender', dataIndex: 'gender', width: 90 },
      { title: '年龄', key: 'age', dataIndex: 'age', width: 90 },
      { title: '家庭地址', key: 'address', dataIndex: 'address', width: 260 },
      { title: '评估日期', key: 'assessmentDate', dataIndex: 'assessmentDate', width: 130 },
      { title: '评估结果', key: 'resultSummary', dataIndex: 'resultSummary', width: 220 },
      { title: '所属机构', key: 'orgName', width: 140 },
      { title: '操作', key: 'action', width: 100, fixed: 'right' }
    ]
  }
}

const pageConfig = computed(() => pageConfigMap[props.assessmentType])

const query = reactive({
  keyword: '',
  contentKeyword: '',
  archiveType: undefined as AssessmentType | undefined,
  status: undefined as string | undefined,
  dateRange: undefined as string[] | undefined,
  pageNo: 1,
  pageSize: 10
})

const open = ref(false)
const formReadonly = ref(false)
const formRef = ref<FormInstance>()
const selectedRowKeys = ref<number[]>([])
const form = reactive<Partial<AssessmentRecord>>({
  elderId: undefined,
  assessmentType: props.assessmentType,
  templateId: undefined,
  elderName: '',
  levelCode: '',
  score: undefined,
  assessmentDate: '',
  nextAssessmentDate: undefined,
  status: 'COMPLETED',
  assessorName: '',
  archiveNo: '',
  resultSummary: '',
  suggestion: '',
  detailJson: '',
  scoreAuto: 1
})
const submitting = ref(false)
const scoreAutoChecked = ref(true)

const rules: FormRules = {
  elderId: [{ required: true, message: '请选择老人' }],
  assessmentDate: [{ required: true, message: '请选择评估日期' }],
  status: [{ required: true, message: '请选择状态' }]
}

const tableColumns = computed(() => pageConfig.value.columns)
const rowSelection = computed(() => {
  if (!pageConfig.value.showBatchDelete) {
    return undefined
  }
  return {
    selectedRowKeys: selectedRowKeys.value,
    onChange: (keys: number[]) => {
      selectedRowKeys.value = keys
    }
  }
})

function statusLabel(status?: string) {
  if (status === 'DRAFT') return '草稿'
  if (status === 'ARCHIVED') return '已归档'
  return '已完成'
}

function statusColor(status?: string) {
  if (status === 'DRAFT') return 'orange'
  if (status === 'ARCHIVED') return 'blue'
  return 'green'
}

function assessmentTypeLabel(type?: AssessmentType | string) {
  if (!type) return '-'
  if (typeof type === 'string' && type.includes('评估')) return type
  const matched = assessmentTypeOptions.find((item) => item.value === type)
  return matched?.label || '-'
}

function displayCell(record: Record<string, any>, key?: string) {
  if (!key) return '-'
  const value = record[key]
  if (value === null || value === undefined || value === '') {
    return '-'
  }
  return value
}

function orgDisplayName(record: AssessmentRecord & Record<string, any>) {
  if (record.orgName) return record.orgName
  if (record.orgId) return `机构${record.orgId}`
  return '-'
}

async function fetchData() {
  loading.value = true
  try {
    const assessmentTypeForQuery = props.assessmentType === 'ARCHIVE'
      ? query.archiveType
      : props.assessmentType
    const keyword = query.contentKeyword || query.keyword
    const res: PageResult<AssessmentRecord> = await getAssessmentRecordPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      assessmentType: assessmentTypeForQuery,
      status: pageConfig.value.showStatusFilter ? query.status : undefined,
      keyword,
      dateFrom: query.dateRange?.[0],
      dateTo: query.dateRange?.[1]
    })
    rows.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

async function fetchTemplates() {
  templates.value = await getAssessmentTemplateList({ assessmentType: props.assessmentType })
}

function onSearch() {
  query.pageNo = 1
  fetchData()
}

function reset() {
  query.keyword = ''
  query.contentKeyword = ''
  query.archiveType = undefined
  query.status = undefined
  query.dateRange = undefined
  query.pageNo = 1
  selectedRowKeys.value = []
  fetchData()
}

function onPageChange(page: number) {
  query.pageNo = page
  fetchData()
}

function onPageSizeChange(current: number, size: number) {
  query.pageNo = 1
  query.pageSize = size
  fetchData()
}

async function searchElderOptions(keyword: string) {
  await loadElderOptions(keyword)
}

function onElderChange(elderId?: number) {
  form.elderName = findElderName(elderId)
}

function assignRecord(record: AssessmentRecord) {
  openForm(record)
  message.info('请在弹窗中调整评估人后保存，即可完成指派')
}

function openForm(record?: AssessmentRecord, readonly = false) {
  formReadonly.value = readonly
  if (record) {
    Object.assign(form, record)
    form.scoreAuto = record.scoreAuto === 0 ? 0 : 1
    ensureSelectedElder(record.elderId, record.elderName)
    scoreAutoChecked.value = record.scoreAuto !== 0
  } else {
    Object.assign(form, {
      id: undefined,
      elderId: undefined,
      assessmentType: props.assessmentType,
      templateId: undefined,
      elderName: '',
      levelCode: '',
      score: undefined,
      assessmentDate: '',
      nextAssessmentDate: undefined,
      status: 'COMPLETED',
      assessorName: '',
      archiveNo: '',
      resultSummary: '',
      suggestion: '',
      detailJson: '',
      scoreAuto: 1
    })
    scoreAutoChecked.value = true
  }
  open.value = true
}

function onScoreAutoChange(checked: boolean) {
  form.scoreAuto = checked ? 1 : 0
}

async function previewScore() {
  if (!form.templateId) {
    message.warning('请先选择量表模板')
    return
  }
  if (!form.detailJson) {
    message.warning('请先填写量表明细JSON')
    return
  }
  const result = await previewAssessmentScore({
    templateId: form.templateId,
    detailJson: form.detailJson
  })
  form.score = result.score
  if (result.levelCode) {
    form.levelCode = result.levelCode
  }
  message.success('试算完成')
}

async function submit() {
  if (formReadonly.value) {
    open.value = false
    return
  }
  if (form.nextAssessmentDate && form.assessmentDate && form.nextAssessmentDate < form.assessmentDate) {
    message.warning('下次评估日期不能早于评估日期')
    return
  }
  if (form.scoreAuto !== 0) {
    if (!form.templateId) {
      message.warning('自动评分开启时必须选择量表模板')
      return
    }
    if (!form.detailJson) {
      message.warning('自动评分开启时必须填写量表明细JSON')
      return
    }
  }
  await formRef.value?.validate()
  submitting.value = true
  try {
    const payload = {
      elderId: form.elderId,
      assessmentType: props.assessmentType,
      templateId: form.templateId,
      elderName: findElderName(form.elderId) || form.elderName,
      levelCode: form.levelCode,
      score: form.score,
      assessmentDate: form.assessmentDate,
      nextAssessmentDate: form.nextAssessmentDate,
      status: form.status,
      assessorName: form.assessorName,
      archiveNo: form.archiveNo,
      resultSummary: form.resultSummary,
      suggestion: form.suggestion,
      detailJson: form.detailJson,
      scoreAuto: form.scoreAuto,
      source: 'MANUAL'
    }
    if (form.id) {
      await updateAssessmentRecord(form.id, payload)
      message.success('更新成功')
    } else {
      await createAssessmentRecord(payload)
      message.success('登记成功')
    }
    open.value = false
    fetchData()
  } finally {
    submitting.value = false
  }
}

function remove(id: number) {
  Modal.confirm({
    title: '确认删除该评估记录？',
    onOk: async () => {
      await deleteAssessmentRecord(id)
      message.success('删除成功')
      fetchData()
    }
  })
}

function removeBatch() {
  if (!selectedRowKeys.value.length) return
  Modal.confirm({
    title: `确认删除选中的 ${selectedRowKeys.value.length} 条评估记录？`,
    onOk: async () => {
      await batchDeleteAssessmentRecord(selectedRowKeys.value.map((item) => Number(item)))
      message.success('批量删除成功')
      selectedRowKeys.value = []
      fetchData()
    }
  })
}

async function exportCsv() {
  const assessmentTypeForQuery = props.assessmentType === 'ARCHIVE'
    ? query.archiveType
    : props.assessmentType
  const blob = await exportAssessmentRecord({
    assessmentType: assessmentTypeForQuery,
    status: pageConfig.value.showStatusFilter ? query.status : undefined,
    keyword: query.contentKeyword || query.keyword,
    dateFrom: query.dateRange?.[0],
    dateTo: query.dateRange?.[1]
  })
  const link = document.createElement('a')
  const url = URL.createObjectURL(new Blob([blob], { type: 'text/csv;charset=utf-8;' }))
  link.href = url
  link.download = `${title}_${new Date().toISOString().slice(0, 10)}.csv`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
  message.success('导出成功')
}

onMounted(async () => {
  await Promise.all([fetchTemplates(), fetchData(), loadElderOptions()])
})
</script>

<style scoped>
.search-bar {
  display: flex;
  flex-wrap: wrap;
  row-gap: 8px;
}

.table-actions {
  margin-bottom: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.hint {
  color: rgba(0, 0, 0, 0.45);
  font-size: 12px;
}
</style>
