<template>
  <PageContainer :title="pageTitle" :subTitle="pageSubTitle">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="部门">
        <a-select
          v-model:value="query.departmentId"
          allow-clear
          show-search
          :filter-option="false"
          :options="departmentOptions"
          :loading="departmentLoading"
          placeholder="选择部门"
          style="width: 200px"
          @search="searchDepartments"
          @focus="() => !departmentOptions.length && searchDepartments('')"
        />
      </a-form-item>
      <a-form-item v-if="isRecordScene" label="员工">
        <a-select
          v-model:value="query.staffId"
          allow-clear
          show-search
          :filter-option="false"
          :options="staffOptions"
          :loading="staffLoading"
          placeholder="选择员工"
          style="width: 200px"
          @search="searchStaff"
          @focus="() => !staffOptions.length && searchStaff('')"
        />
      </a-form-item>
      <a-form-item v-if="isPlanScene" label="年度">
        <a-input-number v-model:value="query.trainingYear" style="width: 140px" :min="2020" :max="2100" />
      </a-form-item>
      <a-form-item label="课程/关键字">
        <a-input v-model:value="query.keyword" placeholder="培训名称/讲师/工号/部门" allow-clear />
      </a-form-item>
      <a-form-item label="时间范围">
        <a-range-picker v-model:value="query.range" />
      </a-form-item>
      <template #extra>
        <a-button type="primary" @click="openDrawer()">{{ isPlanScene ? '新增计划' : '新增记录' }}</a-button>
        <a-button :disabled="!selectedSingleRecord" @click="editSelected">编辑</a-button>
        <a-button v-if="isRecordScene" :disabled="!selectedSingleRecord" @click="openArchiveBySelection">个人培训档案</a-button>
        <a-button @click="exportCurrentExcel">导出 Excel</a-button>
        <a-button @click="exportCurrentPdf">导出 PDF</a-button>
        <a-button :disabled="!selectedSingleRecord" danger @click="removeSelected">删除</a-button>
        <a-button :disabled="selectedRowKeys.length === 0" danger @click="batchRemove">批量删除</a-button>
        <span class="selection-tip">已勾选 {{ selectedRowKeys.length }} 条</span>
      </template>
    </SearchForm>

    <DataTable
      rowKey="id"
      :row-selection="rowSelection"
      :columns="columns"
      :data-source="rows"
      :loading="loading"
      :pagination="pagination"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="record.status === 1 ? 'green' : 'default'">
            {{ resolveStatusText(record.status) }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'certificateRequired'">
          <a-tag :color="record.certificateRequired === 1 ? 'blue' : 'default'">
            {{ record.certificateRequired === 1 ? '要求考证' : '无需考证' }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'attendanceStatus'">
          <a-tag :color="attendanceTagColor(record.attendanceStatus)">
            {{ resolveAttendanceText(record.attendanceStatus) }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'certificateStatus'">
          <a-tag :color="certificateTagColor(record.certificateStatus)">
            {{ resolveCertificateStatusText(record) }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'attachments'">
          <a-space v-if="record.attachments?.length" wrap>
            <a
              v-for="(item, index) in record.attachments"
              :key="`${record.id}-${index}`"
              :href="String(item.url || item.fileUrl || '')"
              target="_blank"
              rel="noreferrer"
            >
              {{ item.name || item.fileName || `附件${index + 1}` }}
            </a>
          </a-space>
          <span v-else>-</span>
        </template>
      </template>
    </DataTable>

    <a-drawer v-model:open="drawerOpen" :title="drawerTitle" width="720">
      <a-form :model="form" layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="所属部门" :required="isPlanScene">
              <a-select
                v-model:value="form.departmentId"
                allow-clear
                show-search
                :filter-option="false"
                :options="departmentOptions"
                :loading="departmentLoading"
                placeholder="选择部门"
                @search="searchDepartments"
                @focus="() => !departmentOptions.length && searchDepartments('')"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="年度">
              <a-input-number v-model:value="form.trainingYear" style="width: 100%" :min="2020" :max="2100" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item v-if="isRecordScene" label="员工" required>
          <a-select
            v-model:value="form.staffId"
            allow-clear
            show-search
            :filter-option="false"
            :options="staffOptions"
            :loading="staffLoading"
            placeholder="选择员工"
            @search="searchStaff"
            @focus="() => !staffOptions.length && searchStaff('')"
          />
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="培训名称" required>
              <a-input v-model:value="form.trainingName" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="培训类别">
              <a-input v-model:value="form.trainingType" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="讲师">
              <a-input v-model:value="form.instructor" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="培训机构">
              <a-input v-model:value="form.provider" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="培训日期">
          <a-range-picker v-model:value="formRange" />
        </a-form-item>
        <a-row v-if="isRecordScene" :gutter="16">
          <a-col :span="8">
            <a-form-item label="出勤状态">
              <a-select v-model:value="form.attendanceStatus" :options="attendanceOptions" allow-clear />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="成绩">
              <a-input-number v-model:value="form.score" style="width: 100%" :min="0" :max="100" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="测试结果">
              <a-select v-model:value="form.testResult" :options="testResultOptions" allow-clear />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="学时">
              <a-input-number v-model:value="form.hours" style="width: 100%" :min="0" :step="0.5" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="要求考证">
              <a-select v-model:value="form.certificateRequired" :options="certificateRequiredOptions" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="证书状态">
              <a-select v-model:value="form.certificateStatus" :options="certificateStatusOptions" allow-clear />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="证书编号">
              <a-input v-model:value="form.certificateNo" placeholder="完成培训后可自动生成" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="状态">
              <a-select v-model:value="form.status" :options="statusOptions" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="附件">
          <a-upload :show-upload-list="false" :before-upload="beforeUploadAttachment" multiple>
            <a-button :loading="uploadingAttachment">上传附件</a-button>
          </a-upload>
          <div v-if="form.attachments?.length" class="attachment-list">
            <a-tag
              v-for="(item, index) in form.attachments"
              :key="`${item.url || item.fileUrl || index}`"
              closable
              @close.prevent="removeAttachment(index)"
            >
              <a :href="String(item.url || item.fileUrl || '')" target="_blank" rel="noreferrer">
                {{ item.name || item.fileName || `附件${index + 1}` }}
              </a>
            </a-tag>
          </div>
        </a-form-item>
        <a-form-item label="备注">
          <a-textarea v-model:value="form.remark" :rows="3" />
        </a-form-item>
      </a-form>
      <template #footer>
        <a-space>
          <a-button @click="drawerOpen = false">取消</a-button>
          <a-button type="primary" :loading="saving" @click="submit">保存</a-button>
        </a-space>
      </template>
    </a-drawer>

    <a-modal v-model:open="archiveOpen" title="个人培训档案" width="980px" :footer="null">
      <a-descriptions :column="3" bordered size="small" v-if="archiveStaffName">
        <a-descriptions-item label="员工">{{ archiveStaffName }}</a-descriptions-item>
        <a-descriptions-item label="工号">{{ archiveStaffNo || '-' }}</a-descriptions-item>
        <a-descriptions-item label="部门">{{ archiveDepartmentName || '-' }}</a-descriptions-item>
      </a-descriptions>
      <a-tabs style="margin-top: 16px">
        <a-tab-pane key="records" tab="培训记录">
          <a-table :data-source="archiveTrainingRows" :columns="archiveTrainingColumns" :pagination="false" row-key="id" size="small" />
        </a-tab-pane>
        <a-tab-pane key="certificates" tab="证书档案">
          <a-table :data-source="archiveCertificateRows" :columns="archiveCertificateColumns" :pagination="false" row-key="id" size="small" />
        </a-tab-pane>
      </a-tabs>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { message, Modal } from 'ant-design-vue'
import type { Dayjs } from 'dayjs'
import dayjs from 'dayjs'
import { useRoute } from 'vue-router'
import jsPDF from 'jspdf'
import autoTable from 'jspdf-autotable'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import {
  createHrTraining,
  deleteHrTraining,
  getHrProfileCertificatePage,
  getHrTrainingPage,
  updateHrTraining
} from '../../api/hr'
import { uploadOaFile } from '../../api/oa'
import { useStaffOptions } from '../../composables/useStaffOptions'
import { useDepartmentOptions } from '../../composables/useDepartmentOptions'
import { exportExcel } from '../../utils/export'
import type { HrStaffCertificateItem, PageResult, StaffTrainingRecord } from '../../types'

const route = useRoute()

const trainingScene = computed<'plans' | 'records'>(() => {
  const queryScene = String(route.query.scene || '').trim()
  if (queryScene === 'plans') return 'plans'
  if (route.name === 'HrDevelopmentPlans') return 'plans'
  return 'records'
})

const isPlanScene = computed(() => trainingScene.value === 'plans')
const isRecordScene = computed(() => !isPlanScene.value)
const backendScene = computed(() => (isPlanScene.value ? 'PLAN' : 'RECORD'))

const pageTitle = computed(() => (isPlanScene.value ? '培训计划' : '培训记录'))
const pageSubTitle = computed(() => (
  isPlanScene.value
    ? '培训与发展 / 按部门与年度维护培训与考证要求'
    : '培训与发展 / 记录已完成培训并自动生成证书'
))

const query = reactive({
  departmentId: undefined as string | number | undefined,
  staffId: undefined as string | number | undefined,
  trainingYear: dayjs().year(),
  keyword: undefined as string | undefined,
  range: undefined as [Dayjs, Dayjs] | undefined,
  pageNo: 1,
  pageSize: 10
})
const rows = ref<StaffTrainingRecord[]>([])
const loading = ref(false)
const saving = ref(false)
const uploadingAttachment = ref(false)
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const selectedRowKeys = ref<string[]>([])
const drawerOpen = ref(false)
const archiveOpen = ref(false)
const form = reactive<Partial<StaffTrainingRecord>>({
  status: 1,
  certificateRequired: 0,
  certificateStatus: 'NONE',
  trainingYear: dayjs().year(),
  attachments: []
})
const formRange = ref<[Dayjs, Dayjs] | undefined>()

const { staffOptions, staffLoading, searchStaff, ensureSelectedStaff } = useStaffOptions({ pageSize: 120 })
const { departmentOptions, departmentLoading, searchDepartments, ensureSelectedDepartment } = useDepartmentOptions({ pageSize: 120, preloadSize: 400 })

const statusOptions = computed(() => (
  isPlanScene.value
    ? [
        { label: '待执行', value: 0 },
        { label: '已完成', value: 1 }
      ]
    : [
        { label: '未完成', value: 0 },
        { label: '已完成', value: 1 }
      ]
))
const attendanceOptions = [
  { label: '出勤', value: 'PRESENT' },
  { label: '迟到', value: 'LATE' },
  { label: '缺勤', value: 'ABSENT' },
  { label: '请假', value: 'LEAVE' }
]
const testResultOptions = [
  { label: '优秀', value: 'EXCELLENT' },
  { label: '通过', value: 'PASS' },
  { label: '未通过', value: 'FAIL' }
]
const certificateRequiredOptions = [
  { label: '否', value: 0 },
  { label: '是', value: 1 }
]
const certificateStatusOptions = [
  { label: '无需证书', value: 'NONE' },
  { label: '待生成', value: 'PENDING' },
  { label: '已生成', value: 'GENERATED' },
  { label: '已发放', value: 'ISSUED' }
]

const columns = computed(() => (
  isPlanScene.value
    ? [
        { title: '年度', dataIndex: 'trainingYear', key: 'trainingYear', width: 90 },
        { title: '部门', dataIndex: 'departmentName', key: 'departmentName', width: 140 },
        { title: '培训名称', dataIndex: 'trainingName', key: 'trainingName', width: 180 },
        { title: '培训类别', dataIndex: 'trainingType', key: 'trainingType', width: 120 },
        { title: '讲师', dataIndex: 'instructor', key: 'instructor', width: 120 },
        { title: '培训日期', dataIndex: 'startDate', key: 'startDate', width: 120 },
        { title: '学时', dataIndex: 'hours', key: 'hours', width: 90 },
        { title: '考证要求', dataIndex: 'certificateRequired', key: 'certificateRequired', width: 100 },
        { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
        { title: '附件', dataIndex: 'attachments', key: 'attachments', width: 180 }
      ]
    : [
        { title: '员工姓名', dataIndex: 'staffName', key: 'staffName', width: 120 },
        { title: '工号', dataIndex: 'staffNo', key: 'staffNo', width: 120 },
        { title: '所属部门', dataIndex: 'departmentName', key: 'departmentName', width: 140 },
        { title: '培训名称', dataIndex: 'trainingName', key: 'trainingName', width: 180 },
        { title: '培训日期', dataIndex: 'endDate', key: 'endDate', width: 120 },
        { title: '出勤状态', dataIndex: 'attendanceStatus', key: 'attendanceStatus', width: 100 },
        { title: '成绩', dataIndex: 'score', key: 'score', width: 90 },
        { title: '测试结果', dataIndex: 'testResult', key: 'testResult', width: 100 },
        { title: '证书状态', dataIndex: 'certificateStatus', key: 'certificateStatus', width: 110 },
        { title: '学时', dataIndex: 'hours', key: 'hours', width: 90 },
        { title: '讲师', dataIndex: 'instructor', key: 'instructor', width: 120 },
        { title: '附件', dataIndex: 'attachments', key: 'attachments', width: 180 }
      ]
))

const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: (string | number)[]) => {
    selectedRowKeys.value = keys.map((item) => String(item))
  }
}))
const selectedRecords = computed(() => rows.value.filter((item) => selectedRowKeys.value.includes(String(item.id))))
const selectedSingleRecord = computed(() => (selectedRecords.value.length === 1 ? selectedRecords.value[0] : null))
const drawerTitle = computed(() => `${form.id ? '编辑' : '新增'}${isPlanScene.value ? '培训计划' : '培训记录'}`)

const archiveStaffName = ref('')
const archiveStaffNo = ref('')
const archiveDepartmentName = ref('')
const archiveTrainingRows = ref<StaffTrainingRecord[]>([])
const archiveCertificateRows = ref<HrStaffCertificateItem[]>([])
const archiveTrainingColumns = [
  { title: '培训名称', dataIndex: 'trainingName', key: 'trainingName', width: 180 },
  { title: '培训日期', dataIndex: 'endDate', key: 'endDate', width: 120 },
  { title: '学时', dataIndex: 'hours', key: 'hours', width: 90 },
  { title: '讲师', dataIndex: 'instructor', key: 'instructor', width: 120 },
  { title: '测试结果', dataIndex: 'testResult', key: 'testResult', width: 100 },
  { title: '证书状态', dataIndex: 'certificateStatus', key: 'certificateStatus', width: 100 }
]
const archiveCertificateColumns = [
  { title: '证书名称', dataIndex: 'certificateName', key: 'certificateName', width: 180 },
  { title: '证书编号', dataIndex: 'certificateNo', key: 'certificateNo', width: 160 },
  { title: '颁发机构', dataIndex: 'issuingAuthority', key: 'issuingAuthority', width: 140 },
  { title: '颁发日期', dataIndex: 'issueDate', key: 'issueDate', width: 120 },
  { title: '到期日期', dataIndex: 'expiryDate', key: 'expiryDate', width: 120 }
]

function normalizeAttachment(item: any) {
  return {
    ...item,
    name: item?.name || item?.fileName,
    url: item?.url || item?.fileUrl,
    size: item?.size || item?.fileSize
  }
}

function resolveStatusText(status?: number) {
  if (status === 1) return '已完成'
  return isPlanScene.value ? '待执行' : '未完成'
}

function resolveAttendanceText(status?: string) {
  if (status === 'PRESENT') return '出勤'
  if (status === 'LATE') return '迟到'
  if (status === 'ABSENT') return '缺勤'
  if (status === 'LEAVE') return '请假'
  return '-'
}

function attendanceTagColor(status?: string) {
  if (status === 'PRESENT') return 'green'
  if (status === 'LATE') return 'orange'
  if (status === 'ABSENT') return 'red'
  if (status === 'LEAVE') return 'blue'
  return 'default'
}

function resolveCertificateStatusText(record: StaffTrainingRecord) {
  if (record.certificateRequired !== 1) return '无需证书'
  if (record.certificateStatus === 'ISSUED') return '已发放'
  if (record.certificateStatus === 'GENERATED') return '已生成'
  if (record.certificateStatus === 'PENDING') return '待生成'
  return '待处理'
}

function certificateTagColor(status?: string) {
  if (status === 'ISSUED' || status === 'GENERATED') return 'green'
  if (status === 'PENDING') return 'orange'
  return 'default'
}

async function fetchData() {
  loading.value = true
  try {
    const params: any = {
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      scene: backendScene.value,
      departmentId: query.departmentId,
      keyword: query.keyword
    }
    if (isRecordScene.value) {
      params.staffId = query.staffId
    }
    if (isPlanScene.value && query.trainingYear) {
      params.trainingYear = query.trainingYear
    }
    if (query.range) {
      params.dateFrom = query.range[0].format('YYYY-MM-DD')
      params.dateTo = query.range[1].format('YYYY-MM-DD')
    }
    const res: PageResult<StaffTrainingRecord> = await getHrTrainingPage(params)
    rows.value = (res.list || []).map((item) => ({
      ...item,
      id: String(item.id),
      staffId: item.staffId == null ? item.staffId : String(item.staffId),
      departmentId: item.departmentId == null ? item.departmentId : String(item.departmentId),
      attachments: Array.isArray(item.attachments) ? item.attachments.map(normalizeAttachment) : []
    }))
    pagination.total = res.total || rows.value.length
    selectedRowKeys.value = []
  } catch {
    rows.value = []
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
  query.departmentId = undefined
  query.staffId = undefined
  query.keyword = undefined
  query.range = undefined
  query.trainingYear = dayjs().year()
  query.pageNo = 1
  query.pageSize = 10
  pagination.current = 1
  pagination.pageSize = 10
  fetchData()
}

function openDrawer(record?: StaffTrainingRecord) {
  form.id = record?.id
  form.sourceTrainingId = record?.sourceTrainingId
  form.trainingScene = backendScene.value
  form.trainingYear = record?.trainingYear || dayjs().year()
  form.departmentId = record?.departmentId
  form.departmentName = record?.departmentName
  form.staffId = record?.staffId
  form.trainingName = record?.trainingName
  form.trainingType = record?.trainingType
  form.provider = record?.provider
  form.instructor = record?.instructor
  form.hours = record?.hours
  form.score = record?.score
  form.attendanceStatus = record?.attendanceStatus
  form.testResult = record?.testResult
  form.certificateRequired = record?.certificateRequired ?? 0
  form.certificateStatus = record?.certificateStatus || 'NONE'
  form.status = record?.status ?? (isPlanScene.value ? 0 : 1)
  form.certificateNo = record?.certificateNo
  form.remark = record?.remark
  form.attachments = (record?.attachments || []).map(normalizeAttachment)
  if (record?.staffId && record?.staffName) {
    ensureSelectedStaff(record.staffId, `${record.staffName}${record.staffNo ? ` (${record.staffNo})` : ''}`)
  }
  if (record?.departmentId && record?.departmentName) {
    ensureSelectedDepartment(record.departmentId, record.departmentName)
  }
  if (record?.startDate && record?.endDate) {
    formRange.value = [dayjs(record.startDate), dayjs(record.endDate)]
  } else {
    formRange.value = undefined
  }
  drawerOpen.value = true
}

async function beforeUploadAttachment(file: File) {
  uploadingAttachment.value = true
  try {
    const uploaded = await uploadOaFile(file, 'hr-training-attachment')
    const attachments = Array.isArray(form.attachments) ? [...form.attachments] : []
    attachments.push(normalizeAttachment({
      name: uploaded.originalFileName || uploaded.fileName || file.name,
      url: uploaded.fileUrl,
      size: uploaded.fileSize || file.size,
      type: uploaded.fileType
    }))
    form.attachments = attachments
    message.success('附件上传成功')
  } catch {
    message.error('附件上传失败')
  } finally {
    uploadingAttachment.value = false
  }
  return false
}

function removeAttachment(index: number) {
  const attachments = Array.isArray(form.attachments) ? [...form.attachments] : []
  attachments.splice(index, 1)
  form.attachments = attachments
}

async function submit() {
  if (!form.trainingName) {
    message.error('请填写培训名称')
    return
  }
  if (isRecordScene.value && !form.staffId) {
    message.error('请选择员工')
    return
  }
  if (isPlanScene.value && !form.departmentId) {
    message.error('请选择部门')
    return
  }
  saving.value = true
  try {
    if (formRange.value) {
      form.startDate = formRange.value[0].format('YYYY-MM-DD')
      form.endDate = formRange.value[1].format('YYYY-MM-DD')
    }
    form.trainingScene = backendScene.value
    if (form.id) {
      await updateHrTraining(form.id, form)
    } else {
      await createHrTraining(form)
    }
    message.success(isRecordScene.value ? '培训记录已保存' : '培训计划已保存')
    drawerOpen.value = false
    fetchData()
  } catch {
    message.error('保存失败')
  } finally {
    saving.value = false
  }
}

async function remove(record: StaffTrainingRecord) {
  if (!record.id) return
  try {
    await deleteHrTraining(record.id)
    message.success('删除成功')
    fetchData()
  } catch {
    message.error('删除失败')
  }
}

function requireSingleSelection(action: string) {
  if (!selectedSingleRecord.value) {
    message.info(`请先勾选 1 条${isPlanScene.value ? '培训计划' : '培训记录'}后再${action}`)
    return null
  }
  return selectedSingleRecord.value
}

function editSelected() {
  const record = requireSingleSelection('编辑')
  if (!record) return
  openDrawer(record)
}

async function removeSelected() {
  const record = requireSingleSelection('删除')
  if (!record) return
  Modal.confirm({
    title: `确认删除${isPlanScene.value ? '培训计划' : '培训记录'}？`,
    content: `将删除「${record.trainingName || '未命名培训'}」，删除后不可恢复。`,
    okText: '确认删除',
    okType: 'danger',
    cancelText: '取消',
    onOk: async () => {
      await remove(record)
    }
  })
}

async function batchRemove() {
  if (selectedRowKeys.value.length === 0) return
  Modal.confirm({
    title: `确认批量删除${isPlanScene.value ? '培训计划' : '培训记录'}？`,
    content: `将删除 ${selectedRecords.value.length} 条数据，删除后不可恢复。`,
    okText: '确认删除',
    okType: 'danger',
    cancelText: '取消',
    onOk: async () => {
      try {
        await Promise.all(selectedRecords.value.map((record) => deleteHrTraining(String(record.id))))
        message.success(`批量删除成功，共处理 ${selectedRecords.value.length} 条`)
        fetchData()
      } catch {
        message.error('批量删除失败')
      }
    }
  })
}

function toExportRows(list: StaffTrainingRecord[]) {
  return list.map((item) => ({
    年度: item.trainingYear || '',
    员工姓名: item.staffName || '',
    工号: item.staffNo || '',
    所属部门: item.departmentName || '',
    培训名称: item.trainingName || '',
    培训类别: item.trainingType || '',
    培训日期: item.endDate || item.startDate || '',
    出勤状态: resolveAttendanceText(item.attendanceStatus),
    成绩: item.score ?? '',
    测试结果: item.testResult || '',
    证书状态: resolveCertificateStatusText(item),
    学时: item.hours ?? '',
    讲师: item.instructor || '',
    备注: item.remark || ''
  }))
}

function exportCurrentExcel() {
  const exportRows = toExportRows(rows.value)
  if (!exportRows.length) {
    message.info('当前没有可导出的数据')
    return
  }
  exportExcel(exportRows, `${pageTitle.value}-${dayjs().format('YYYYMMDD-HHmmss')}`)
}

function exportCurrentPdf() {
  const exportRows = toExportRows(rows.value)
  if (!exportRows.length) {
    message.info('当前没有可导出的数据')
    return
  }
  const doc = new jsPDF({ orientation: 'landscape', unit: 'pt', format: 'a4' })
  doc.setFontSize(14)
  doc.text(pageTitle.value, 40, 36)
  autoTable(doc, {
    startY: 52,
    head: [Object.keys(exportRows[0])],
    body: exportRows.map((item) => Object.values(item).map((value) => String(value ?? ''))),
    styles: {
      font: 'helvetica',
      fontSize: 8
    },
    margin: { left: 24, right: 24 }
  })
  doc.save(`${pageTitle.value}-${dayjs().format('YYYYMMDD-HHmmss')}.pdf`)
}

async function openArchive(staffId: string | number, staffName?: string, staffNo?: string, departmentName?: string) {
  archiveOpen.value = true
  archiveStaffName.value = staffName || ''
  archiveStaffNo.value = staffNo || ''
  archiveDepartmentName.value = departmentName || ''
  try {
    const [trainingRes, certificateRes] = await Promise.all([
      getHrTrainingPage({ pageNo: 1, pageSize: 200, scene: 'RECORD', staffId }),
      getHrProfileCertificatePage({ pageNo: 1, pageSize: 200, staffId })
    ])
    archiveTrainingRows.value = (trainingRes.list || []) as StaffTrainingRecord[]
    archiveCertificateRows.value = (certificateRes.list || []) as HrStaffCertificateItem[]
  } catch {
    archiveTrainingRows.value = []
    archiveCertificateRows.value = []
    message.error('加载个人培训档案失败')
  }
}

function openArchiveBySelection() {
  const record = requireSingleSelection('查看个人培训档案')
  if (!record || !record.staffId) {
    message.info('该计划未绑定员工，无法查看个人培训档案')
    return
  }
  openArchive(record.staffId, record.staffName, record.staffNo, record.departmentName)
}

searchStaff('')
searchDepartments('')
fetchData()

watch(
  () => route.fullPath,
  () => {
    if (loading.value || saving.value) return
    query.pageNo = 1
    pagination.current = 1
    fetchData()
  }
)
</script>

<style scoped>
.selection-tip {
  color: rgba(0, 0, 0, 0.45);
  font-size: 12px;
}

.attachment-list {
  margin-top: 8px;
}
</style>
