<template>
  <PageContainer title="排班" subTitle="排班落日程、值班提醒、换班申请一体化管理">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item v-if="canManageSchedule" label="员工">
        <a-select
          v-model:value="query.staffId"
          allow-clear
          show-search
          :filter-option="false"
          :options="staffOptions"
          style="width: 220px"
          placeholder="输入员工姓名/拼音首字母"
          @search="searchStaff"
          @focus="() => !staffOptions.length && searchStaff('')"
        />
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="query.status" allow-clear style="width: 140px">
          <a-select-option :value="1">排班中</a-select-option>
          <a-select-option :value="2">已确认</a-select-option>
          <a-select-option :value="0">停用</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="日期范围">
        <a-range-picker v-model:value="query.range" />
      </a-form-item>
      <template #extra>
        <a-space>
          <a-button v-if="canManageSchedule" type="primary" @click="openEdit()">新增排班</a-button>
          <a-button @click="fetchSwaps">刷新换班申请</a-button>
        </a-space>
      </template>
    </SearchForm>

    <DataTable
      rowKey="id"
      :columns="columns"
      :data-source="rows"
      :loading="loading"
      :pagination="pagination"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="record.status === 2 ? 'green' : record.status === 0 ? 'default' : 'blue'">
            {{ record.status === 2 ? '已确认' : record.status === 0 ? '停用' : '排班中' }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'sourceTemplateName'">
          <a-tag color="cyan">{{ record.sourceTemplateName || '手工排班' }}</a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a v-if="canManageSchedule" @click="openEdit(record)">编辑</a>
            <a v-if="canRequestSwap(record)" @click="openSwap(record)">申请换班</a>
            <a v-if="canManageSchedule" @click="remove(record)" style="color: #ff4d4f">删除</a>
          </a-space>
        </template>
      </template>
    </DataTable>

    <a-card :bordered="false" class="card-elevated" style="margin-top: 12px">
      <template #title>换班申请</template>
      <a-table
        row-key="id"
        :columns="swapColumns"
        :data-source="swapRows"
        :loading="swapLoading"
        :pagination="swapPagination"
        @change="handleSwapTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="swapStatusColor(record.status)">{{ swapStatusText(record.status) }}</a-tag>
          </template>
          <template v-else-if="column.key === 'targetConfirmStatus'">
            <a-tag :color="record.targetConfirmStatus === 'AGREED' ? 'green' : record.targetConfirmStatus === 'REJECTED' ? 'red' : 'orange'">
              {{ record.targetConfirmStatus === 'AGREED' ? '已同意' : record.targetConfirmStatus === 'REJECTED' ? '已拒绝' : '待确认' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'actions'">
            <a-space>
              <a v-if="canConfirmSwap(record)" @click="decideSwap(record, true)">同意</a>
              <a v-if="canConfirmSwap(record)" style="color: #ff4d4f" @click="decideSwap(record, false)">拒绝</a>
              <a v-if="record.approvalId" @click="openApproval(record)">查看审批</a>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal v-model:open="editOpen" :title="form.id ? '编辑排班' : '新增排班'" @ok="submit" :confirm-loading="saving" width="620px">
      <a-form layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="员工" required>
              <a-select
                v-model:value="form.staffId"
                show-search
                :filter-option="false"
                :options="staffOptions"
                placeholder="输入员工姓名/拼音首字母"
                @search="searchStaff"
                @focus="() => !staffOptions.length && searchStaff('')"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="员工姓名">
              <a-input v-model:value="form.staffName" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="值班日期" required>
              <a-date-picker v-model:value="dutyDate" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="班次编码">
              <a-input v-model:value="form.shiftCode" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="开始时间">
              <a-input v-model:value="form.startTime" placeholder="2026-03-23T08:00:00" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="结束时间">
              <a-input v-model:value="form.endTime" placeholder="2026-03-23T17:00:00" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="来源方案">
          <a-input v-model:value="form.sourceTemplateName" placeholder="手工排班可不填" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="form.status">
            <a-select-option :value="1">排班中</a-select-option>
            <a-select-option :value="2">已确认</a-select-option>
            <a-select-option :value="0">停用</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="swapOpen" title="申请换班" @ok="submitSwap" :confirm-loading="swapSubmitting" width="720px">
      <a-form layout="vertical">
        <a-form-item label="我的班次">
          <a-input :value="swapSourceText" disabled />
        </a-form-item>
        <a-form-item label="对调员工" required>
          <a-select
            v-model:value="swapForm.targetStaffId"
            show-search
            :filter-option="false"
            :options="staffOptions"
            placeholder="输入员工姓名搜索"
            @search="searchStaff"
            @focus="() => !staffOptions.length && searchStaff('')"
            @change="loadTargetSchedules"
          />
        </a-form-item>
        <a-form-item label="对调班次" required>
          <a-select
            v-model:value="swapForm.targetScheduleId"
            :options="targetScheduleOptions"
            placeholder="选择对方班次"
          />
        </a-form-item>
        <a-form-item label="申请说明">
          <a-textarea v-model:value="swapForm.applicantRemark" :rows="3" placeholder="填写换班原因，系统会先通知对方确认，再进入人事/主管审批。" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import dayjs, { type Dayjs } from 'dayjs'
import { message, Modal } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { useStaffOptions } from '../../composables/useStaffOptions'
import {
  agreeShiftSwap,
  createSchedule,
  createShiftSwap,
  deleteSchedule,
  getSchedulePage,
  getSwapCandidatePage,
  getShiftSwapPage,
  rejectShiftSwap,
  updateSchedule
} from '../../api/schedule'
import type { Id, PageResult, ScheduleItem, ShiftSwapRequestItem } from '../../types'
import { resolveCareError } from './careError'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const saving = ref(false)
const rows = ref<ScheduleItem[]>([])
const swapLoading = ref(false)
const swapSubmitting = ref(false)
const swapRows = ref<ShiftSwapRequestItem[]>([])
const { staffOptions, searchStaff, ensureSelectedStaff } = useStaffOptions({ pageSize: 200, preloadSize: 500 })

const query = reactive({
  staffId: undefined as string | undefined,
  status: undefined as number | undefined,
  range: undefined as [Dayjs, Dayjs] | undefined,
  pageNo: 1,
  pageSize: 10
})
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const swapQuery = reactive({
  pageNo: 1,
  pageSize: 10
})
const swapPagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

const columns = [
  { title: '员工ID', dataIndex: 'staffId', key: 'staffId', width: 100 },
  { title: '员工姓名', dataIndex: 'staffName', key: 'staffName', width: 140 },
  { title: '值班日期', dataIndex: 'dutyDate', key: 'dutyDate', width: 120 },
  { title: '班次', dataIndex: 'shiftCode', key: 'shiftCode', width: 100 },
  { title: '开始', dataIndex: 'startTime', key: 'startTime', width: 180 },
  { title: '结束', dataIndex: 'endTime', key: 'endTime', width: 180 },
  { title: '来源方案', key: 'sourceTemplateName', width: 140 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 90 },
  { title: '操作', key: 'action', width: 220 }
]

const swapColumns = [
  { title: '申请人', dataIndex: 'applicantStaffName', key: 'applicantStaffName', width: 120 },
  { title: '申请班次', key: 'applicantShift', width: 220, customRender: ({ record }: { record: ShiftSwapRequestItem }) => `${record.applicantDutyDate || '-'} ${record.applicantShiftCode || '-'}` },
  { title: '目标员工', dataIndex: 'targetStaffName', key: 'targetStaffName', width: 120 },
  { title: '目标班次', key: 'targetShift', width: 220, customRender: ({ record }: { record: ShiftSwapRequestItem }) => `${record.targetDutyDate || '-'} ${record.targetShiftCode || '-'}` },
  { title: '状态', key: 'status', width: 120 },
  { title: '对方确认', key: 'targetConfirmStatus', width: 120 },
  { title: '操作', key: 'actions', width: 180 }
]

const editOpen = ref(false)
const dutyDate = ref<Dayjs | undefined>()
const form = reactive<Partial<ScheduleItem> & { staffId?: Id }>({ status: 1 })

const swapOpen = ref(false)
const swapSource = ref<ScheduleItem>()
const targetSchedules = ref<ScheduleItem[]>([])
const swapForm = reactive({
  targetStaffId: undefined as Id | undefined,
  targetScheduleId: undefined as Id | undefined,
  applicantRemark: ''
})

const canManageSchedule = computed(() =>
  userStore.roles.some((role) => ['ADMIN', 'SYS_ADMIN', 'DIRECTOR', 'HR_MINISTER'].includes(String(role || '').toUpperCase()))
)

const swapSourceText = computed(() => {
  if (!swapSource.value) return ''
  return `${swapSource.value.dutyDate || '-'} ${swapSource.value.shiftCode || '-'} ${swapSource.value.startTime || ''} ~ ${swapSource.value.endTime || ''}`
})
const targetScheduleOptions = computed(() =>
  targetSchedules.value.map((item) => ({
    label: `${item.dutyDate || '-'} ${item.shiftCode || '-'} ${item.startTime || ''} ~ ${item.endTime || ''}`,
    value: item.id
  }))
)

function currentStaffId() {
  return userStore.staffInfo?.id != null ? String(userStore.staffInfo.id) : ''
}

function canRequestSwap(record: ScheduleItem) {
  return String(record.staffId || '') === currentStaffId() && Number(record.status || 1) !== 0
}

function canConfirmSwap(record: ShiftSwapRequestItem) {
  return String(record.targetStaffId || '') === currentStaffId() && record.status === 'PENDING_TARGET'
}

function swapStatusText(status?: string) {
  switch (status) {
    case 'PENDING_TARGET':
      return '待对方确认'
    case 'PENDING_APPROVAL':
      return '待审批'
    case 'APPROVED':
      return '已通过'
    case 'REJECTED':
      return '审批驳回'
    case 'TARGET_REJECTED':
      return '对方拒绝'
    case 'CANCELLED':
      return '已取消'
    default:
      return status || '处理中'
  }
}

function swapStatusColor(status?: string) {
  switch (status) {
    case 'APPROVED':
      return 'green'
    case 'REJECTED':
    case 'TARGET_REJECTED':
      return 'red'
    case 'PENDING_APPROVAL':
      return 'blue'
    default:
      return 'orange'
  }
}

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<ScheduleItem> = await getSchedulePage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      staffId: query.staffId || undefined,
      status: query.status,
      dateFrom: query.range?.[0] ? dayjs(query.range[0]).format('YYYY-MM-DD') : undefined,
      dateTo: query.range?.[1] ? dayjs(query.range[1]).format('YYYY-MM-DD') : undefined
    })
    rows.value = res.list || []
    pagination.total = res.total || rows.value.length
  } catch (error) {
    message.error(resolveCareError(error, '加载排班失败'))
    rows.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
  await fetchSwaps()
}

async function fetchSwaps() {
  swapLoading.value = true
  try {
    const res: PageResult<ShiftSwapRequestItem> = await getShiftSwapPage({
      pageNo: swapQuery.pageNo,
      pageSize: swapQuery.pageSize,
      mineOnly: true
    })
    swapRows.value = res.list || []
    swapPagination.total = res.total || swapRows.value.length
  } catch (error) {
    swapRows.value = []
    swapPagination.total = 0
    message.error(resolveCareError(error, '加载换班申请失败'))
  } finally {
    swapLoading.value = false
  }
}

function handleTableChange(pag: any) {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  query.pageNo = pag.current
  query.pageSize = pag.pageSize
  fetchData()
}

function handleSwapTableChange(pag: any) {
  swapPagination.current = pag.current
  swapPagination.pageSize = pag.pageSize
  swapQuery.pageNo = pag.current
  swapQuery.pageSize = pag.pageSize
  fetchSwaps()
}

function onReset() {
  query.staffId = canManageSchedule.value ? undefined : currentStaffId() || undefined
  query.status = undefined
  query.range = undefined
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

function openEdit(record?: ScheduleItem) {
  if (record) {
    Object.assign(form, {
      ...record,
      staffId: record.staffId == null ? undefined : String(record.staffId)
    })
  } else {
    Object.assign(form, {
      id: undefined,
      status: 1,
      staffId: undefined,
      staffName: '',
      shiftCode: '',
      startTime: '',
      endTime: '',
      sourceTemplateName: ''
    })
  }
  if (form.staffId) ensureSelectedStaff(form.staffId, form.staffName)
  dutyDate.value = form.dutyDate ? dayjs(form.dutyDate) : undefined
  editOpen.value = true
}

async function submit() {
  if (!form.staffId || !dutyDate.value) return
  const selected = staffOptions.value.find((item) => String(item.value) === String(form.staffId))
  const payload = {
    ...form,
    staffId: form.staffId as Id,
    staffName: selected?.name || form.staffName,
    dutyDate: dutyDate.value.format('YYYY-MM-DD')
  }
  saving.value = true
  try {
    if (form.id) {
      await updateSchedule(form.id, payload)
    } else {
      await createSchedule(payload)
    }
    message.success('保存成功，已同步个人日程和值班提醒')
    editOpen.value = false
    await fetchData()
  } catch (error) {
    message.error(resolveCareError(error, '保存失败'))
  } finally {
    saving.value = false
  }
}

async function remove(record: ScheduleItem) {
  Modal.confirm({
    title: '确认删除该排班记录吗？',
    onOk: async () => {
      try {
        await deleteSchedule(record.id)
        message.success('删除成功')
        await fetchData()
      } catch (error) {
        message.error(resolveCareError(error, '删除失败'))
      }
    }
  })
}

function openSwap(record: ScheduleItem) {
  swapSource.value = record
  swapForm.targetStaffId = undefined
  swapForm.targetScheduleId = undefined
  swapForm.applicantRemark = ''
  targetSchedules.value = []
  swapOpen.value = true
}

async function loadTargetSchedules() {
  if (!swapSource.value?.dutyDate || !swapForm.targetStaffId) {
    targetSchedules.value = []
    return
  }
  try {
    const res: PageResult<ScheduleItem> = await getSwapCandidatePage({
      pageNo: 1,
      pageSize: 50,
      targetStaffId: swapForm.targetStaffId as Id,
      dutyDate: swapSource.value.dutyDate
    })
    targetSchedules.value = (res.list || []).filter((item) => String(item.id) !== String(swapSource.value?.id || ''))
  } catch {
    targetSchedules.value = []
  }
}

async function submitSwap() {
  if (!swapSource.value?.id || !swapForm.targetScheduleId) {
    message.warning('请选择对调班次')
    return
  }
  swapSubmitting.value = true
  try {
    await createShiftSwap({
      applicantScheduleId: swapSource.value.id,
      targetScheduleId: swapForm.targetScheduleId,
      applicantRemark: swapForm.applicantRemark
    })
    message.success('换班申请已发出，系统已通知对方确认')
    swapOpen.value = false
    await fetchSwaps()
  } catch (error) {
    message.error(resolveCareError(error, '换班申请提交失败'))
  } finally {
    swapSubmitting.value = false
  }
}

function decideSwap(record: ShiftSwapRequestItem, agreed: boolean) {
  Modal.confirm({
    title: agreed ? '确认同意换班？' : '确认拒绝换班？',
    content: agreed ? '同意后系统会自动提交到人事/主管审批。' : '拒绝后流程结束，原排班不变。',
    onOk: async () => {
      try {
        if (agreed) {
          await agreeShiftSwap(record.id)
          message.success('已同意换班，已进入审批流程')
        } else {
          await rejectShiftSwap(record.id)
          message.success('已拒绝换班')
        }
        await fetchSwaps()
      } catch (error) {
        message.error(resolveCareError(error, agreed ? '同意失败' : '拒绝失败'))
      }
    }
  })
}

function openApproval(record: ShiftSwapRequestItem) {
  router.push({
    path: '/oa/approval',
    query: {
      type: 'SHIFT_CHANGE',
      focusId: String(record.approvalId || ''),
      status: 'PENDING'
    }
  })
}

if (!canManageSchedule.value) {
  query.staffId = currentStaffId() || undefined
}

searchStaff('')
fetchData()
</script>
