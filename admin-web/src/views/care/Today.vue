<template>
  <PageContainer title="今日护理任务" subTitle="任务分派与执行跟踪">
    <a-card :bordered="false" class="card-elevated">
      <a-form layout="inline" class="search-bar" @submit.prevent>
        <a-form-item label="日期范围">
          <a-range-picker v-model:value="query.range" />
        </a-form-item>
        <a-form-item label="护工">
          <a-select
            v-model:value="query.staffId"
            show-search
            allow-clear
            placeholder="输入姓名搜索"
            :filter-option="false"
            :options="staffOptions"
            style="width: 180px"
            @search="searchStaff"
          />
        </a-form-item>
        <a-form-item label="房间">
          <a-input v-model:value="query.roomNo" placeholder="房间号" allow-clear />
        </a-form-item>
        <a-form-item label="护理等级">
          <a-input v-model:value="query.careLevel" placeholder="如 A/B/C" allow-clear />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="query.status" :options="statusOptions" allow-clear style="width: 140px" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="search">查询</a-button>
            <a-button @click="reset">重置</a-button>
            <a-button type="primary" @click="openAction">新增/派发任务</a-button>
            <a-button @click="exportCsvData">导出CSV</a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <vxe-table
        border
        stripe
        height="520"
        :loading="loading"
        :data="tasks"
        :column-config="{ resizable: true }"
      >
        <vxe-column field="elderName" title="老人" width="140" fixed="left"></vxe-column>
        <vxe-column field="roomNo" title="房间" width="100"></vxe-column>
        <vxe-column field="taskName" title="任务" min-width="160"></vxe-column>
        <vxe-column field="planTime" title="计划时间" width="160"></vxe-column>
        <vxe-column field="staffId" title="护工" width="140">
          <template #default="{ row }">
            <span>{{ staffName(row.staffId) }}</span>
          </template>
        </vxe-column>
        <vxe-column field="status" title="状态" width="100">
          <template #default="{ row }">
            <a-tag :color="statusColor(row.status)">{{ statusLabel(row.status) }}</a-tag>
          </template>
        </vxe-column>
        <vxe-column title="操作" width="200" fixed="right">
          <template #default="{ row }">
            <a-space>
              <a @click="openDetail(row)">详情</a>
              <a @click="openAssign(row)">派发</a>
              <a @click="openReview(row)">评价</a>
            </a-space>
          </template>
        </vxe-column>
      </vxe-table>
      <div class="pager">
        <a-pagination
          v-model:current="page.pageNo"
          v-model:pageSize="page.pageSize"
          :total="page.total"
          show-size-changer
          @change="search"
          @showSizeChange="search"
        />
      </div>
    </a-card>

    <a-drawer v-model:open="detailOpen" title="任务详情" width="520">
      <a-descriptions :column="1" bordered size="small">
        <a-descriptions-item label="老人">{{ current?.elderName }}</a-descriptions-item>
        <a-descriptions-item label="房间">{{ current?.roomNo }}</a-descriptions-item>
        <a-descriptions-item label="任务">{{ current?.taskName }}</a-descriptions-item>
        <a-descriptions-item label="计划时间">{{ current?.planTime }}</a-descriptions-item>
        <a-descriptions-item label="护工">{{ staffName(current?.staffId) }}</a-descriptions-item>
        <a-descriptions-item label="状态">{{ statusLabel(current?.status) }}</a-descriptions-item>
      </a-descriptions>
      <a-divider />
      <a-typography-title :level="5">执行日志</a-typography-title>
      <a-empty description="暂无执行日志" />
    </a-drawer>

    <a-modal v-model:open="assignOpen" title="派发任务" @ok="submitAssign" :confirm-loading="assigning">
      <a-form layout="vertical" ref="assignFormRef" :model="assignForm" :rules="assignRules">
        <a-form-item label="任务" name="taskDailyId">
          <a-input v-model:value="assignForm.taskDailyId" disabled />
        </a-form-item>
        <a-form-item label="护工" name="staffId" required>
          <a-select
            v-model:value="assignForm.staffId"
            show-search
            :filter-option="false"
            :options="staffOptions"
            @search="searchStaff"
          />
        </a-form-item>
        <a-form-item label="覆盖已分配">
          <a-switch v-model:checked="assignForm.force" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="reviewOpen" title="任务评价" @ok="submitReview" :confirm-loading="reviewing">
      <a-form layout="vertical" ref="reviewFormRef" :model="reviewForm" :rules="reviewRules">
        <a-form-item label="任务" name="taskDailyId">
          <a-input v-model:value="reviewForm.taskDailyId" disabled />
        </a-form-item>
        <a-form-item label="护工" name="staffId" required>
          <a-select
            v-model:value="reviewForm.staffId"
            show-search
            :filter-option="false"
            :options="staffOptions"
            @search="searchStaff"
          />
        </a-form-item>
        <a-form-item label="评分" name="score" required>
          <a-rate v-model:value="reviewForm.score" />
        </a-form-item>
        <a-form-item label="评价内容" name="comment">
          <a-textarea v-model:value="reviewForm.comment" :rows="3" />
        </a-form-item>
        <a-form-item label="评价人类型">
          <a-select v-model:value="reviewForm.reviewerType" :options="reviewerOptions" />
        </a-form-item>
        <a-form-item label="评价时间">
          <a-date-picker v-model:value="reviewForm.reviewTime" show-time style="width: 100%" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="actionOpen" title="新增/派发任务" :width="720" @ok="submitAction" :confirm-loading="actionLoading">
      <a-steps :current="actionStep" size="small" class="stepbar">
        <a-step title="选择方式" />
        <a-step title="填写条件" />
        <a-step title="确认执行" />
      </a-steps>

      <div v-if="actionStep === 0" class="step-body">
        <a-radio-group v-model:value="actionMode">
          <a-space direction="vertical">
            <a-radio value="generate">自动生成（按模板/日期）</a-radio>
            <a-radio value="create">手动新建（单条）</a-radio>
            <a-radio value="batch">批量派发（按护工/楼层/房间）</a-radio>
          </a-space>
        </a-radio-group>
      </div>

      <div v-if="actionStep === 1" class="step-body">
        <a-form v-if="actionMode === 'generate'" layout="vertical" ref="generateFormRef" :model="generateForm">
          <a-form-item label="日期">
            <a-date-picker v-model:value="generateForm.date" style="width: 100%" />
          </a-form-item>
          <a-form-item label="覆盖已生成">
            <a-switch v-model:checked="generateForm.force" />
          </a-form-item>
        </a-form>

        <a-form v-else-if="actionMode === 'create'" layout="vertical" ref="createFormRef" :model="createForm" :rules="createRules">
          <a-form-item label="老人" name="elderId" required>
            <a-select
              v-model:value="createForm.elderId"
              show-search
              placeholder="输入姓名搜索"
              :filter-option="false"
              :options="elderOptions"
              @search="searchElders"
            />
          </a-form-item>
          <a-form-item label="自定义任务">
            <a-switch v-model:checked="createForm.custom" />
          </a-form-item>
          <a-form-item v-if="createForm.custom" label="任务名称" name="taskName" required>
            <a-input v-model:value="createForm.taskName" placeholder="请输入任务名称" />
          </a-form-item>
          <a-form-item v-else label="护理模板" name="templateId" required>
            <a-select v-model:value="createForm.templateId" :options="templateOptions" placeholder="选择模板" />
          </a-form-item>
          <a-form-item label="计划时间" name="planTime" required>
            <a-date-picker v-model:value="createForm.planTime" show-time style="width: 100%" />
          </a-form-item>
          <a-form-item label="护工">
            <a-select
              v-model:value="createForm.staffId"
              show-search
              allow-clear
              :filter-option="false"
              :options="staffOptions"
              @search="searchStaff"
            />
          </a-form-item>
        </a-form>

        <a-form v-else layout="vertical" ref="batchFormRef" :model="batchForm" :rules="batchRules">
          <a-form-item label="护工" name="staffId" required>
            <a-select
              v-model:value="batchForm.staffId"
              show-search
              :filter-option="false"
              :options="staffOptions"
              @search="searchStaff"
            />
          </a-form-item>
          <a-form-item label="日期范围">
            <a-range-picker v-model:value="batchForm.range" />
          </a-form-item>
          <a-form-item label="楼栋">
            <a-input v-model:value="batchForm.building" />
          </a-form-item>
          <a-form-item label="楼层">
            <a-input v-model:value="batchForm.floorNo" />
          </a-form-item>
          <a-form-item label="房间号">
            <a-input v-model:value="batchForm.roomNo" />
          </a-form-item>
          <a-form-item label="护理等级">
            <a-input v-model:value="batchForm.careLevel" />
          </a-form-item>
          <a-form-item label="状态">
            <a-select v-model:value="batchForm.status" :options="statusOptions" allow-clear />
          </a-form-item>
          <a-form-item label="覆盖已分配">
            <a-switch v-model:checked="batchForm.force" />
          </a-form-item>
        </a-form>
      </div>

      <div v-if="actionStep === 2" class="step-body">
        <a-alert type="info" show-icon message="请确认将要执行的操作" />
        <a-descriptions :column="1" bordered size="small" class="confirm-box">
          <a-descriptions-item label="操作类型">
            {{ actionModeLabel }}
          </a-descriptions-item>
          <a-descriptions-item v-if="actionMode === 'generate'" label="日期">
            {{ generateForm.date ? dayjs(generateForm.date).format('YYYY-MM-DD') : '-' }}
          </a-descriptions-item>
          <a-descriptions-item v-if="actionMode === 'generate'" label="覆盖已生成">
            {{ generateForm.force ? '是' : '否' }}
          </a-descriptions-item>
          <a-descriptions-item v-if="actionMode === 'create'" label="老人">
            {{ elderName(createForm.elderId) }}
          </a-descriptions-item>
          <a-descriptions-item v-if="actionMode === 'create'" label="任务">
            {{ createForm.custom ? createForm.taskName : templateLabel }}
          </a-descriptions-item>
          <a-descriptions-item v-if="actionMode === 'create'" label="计划时间">
            {{ createForm.planTime ? dayjs(createForm.planTime).format('YYYY-MM-DD HH:mm') : '-' }}
          </a-descriptions-item>
          <a-descriptions-item v-if="actionMode === 'batch'" label="护工">
            {{ staffName(batchForm.staffId) }}
          </a-descriptions-item>
          <a-descriptions-item v-if="actionMode === 'batch'" label="日期范围">
            {{ batchForm.range?.[0] ? dayjs(batchForm.range[0]).format('YYYY-MM-DD') : '-' }}
            ~
            {{ batchForm.range?.[1] ? dayjs(batchForm.range[1]).format('YYYY-MM-DD') : '-' }}
          </a-descriptions-item>
          <a-descriptions-item v-if="actionMode === 'batch'" label="覆盖已分配">
            {{ batchForm.force ? '是' : '否' }}
          </a-descriptions-item>
        </a-descriptions>
      </div>

      <template #footer>
        <a-space>
          <a-button v-if="actionStep > 0" @click="prevStep">上一步</a-button>
          <a-button v-if="actionStep < 2" type="primary" @click="nextStep">下一步</a-button>
          <a-button v-else type="primary" :loading="actionLoading" @click="submitAction">确认执行</a-button>
        </a-space>
      </template>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { reactive, ref, computed } from 'vue'
import dayjs from 'dayjs'
import { Modal, message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import { getTaskPage, assignTask, assignTaskBatch, reviewTask, generateTasks, getTemplatePage, createTask } from '../../api/care'
import { getStaffPage } from '../../api/rbac'
import { getElderPage } from '../../api/elder'
import type { CareTaskItem, PageResult, CareTaskAssignRequest, CareTaskBatchAssignRequest, CareTaskReviewRequest } from '../../types'
import { exportCsv } from '../../utils/export'

const tasks = ref<CareTaskItem[]>([])
const loading = ref(false)
const detailOpen = ref(false)
const current = ref<CareTaskItem | null>(null)
const assignOpen = ref(false)
const reviewOpen = ref(false)
const assigning = ref(false)
const reviewing = ref(false)
const assignFormRef = ref()
const reviewFormRef = ref()
const batchFormRef = ref()
const generateFormRef = ref()
const createFormRef = ref()
const templateOptions = ref<{ label: string; value: number }[]>([])
const actionOpen = ref(false)
const actionStep = ref(0)
const actionMode = ref<'generate' | 'create' | 'batch'>('generate')
const actionLoading = ref(false)
const staffOptions = ref<{ label: string; value: number }[]>([])
const elderOptions = ref<{ label: string; value: number }[]>([])
const staffMap = ref<Record<number, string>>({})
const elderMap = ref<Record<number, string>>({})

const query = reactive({
  range: [dayjs().subtract(1, 'day'), dayjs().add(1, 'day')] as any,
  staffId: undefined as number | undefined,
  roomNo: '',
  careLevel: '',
  status: undefined as string | undefined
})

const page = reactive({ pageNo: 1, pageSize: 10, total: 0 })

const statusOptions = [
  { label: '待执行', value: 'PENDING' },
  { label: '已完成', value: 'DONE' },
  { label: '异常', value: 'EXCEPTION' }
]

const reviewerOptions = [
  { label: '管理员', value: 'ADMIN' },
  { label: '护士长', value: 'SUPERVISOR' },
  { label: '老人', value: 'ELDER' }
]

const assignForm = reactive<CareTaskAssignRequest>({ taskDailyId: 0, staffId: 0, force: true })
const batchForm = reactive<CareTaskBatchAssignRequest>({
  staffId: 0,
  range: [dayjs().subtract(1, 'day'), dayjs().add(1, 'day')] as any,
  force: true
} as any)
const reviewForm = reactive<CareTaskReviewRequest>({
  taskDailyId: 0,
  staffId: 0,
  score: 5,
  reviewerType: 'ADMIN',
  reviewTime: dayjs() as any
})
const generateForm = reactive({ date: dayjs(), force: false })
const createForm = reactive<any>({
  elderId: 0,
  templateId: 0,
  taskName: '',
  custom: false,
  planTime: dayjs(),
  staffId: undefined
})

const assignRules = { staffId: [{ required: true, message: '请选择护工' }] }
const batchRules = { staffId: [{ required: true, message: '请选择护工' }] }
const reviewRules = {
  staffId: [{ required: true, message: '请选择护工' }],
  score: [{ required: true, type: 'number', min: 1, message: '评分最低 1' }]
}
const createRules = {
  elderId: [{ required: true, message: '请选择老人' }],
  templateId: [
    {
      validator: (_: any, value: any) => {
        if (createForm.custom) return Promise.resolve()
        return value ? Promise.resolve() : Promise.reject(new Error('请选择模板'))
      }
    }
  ],
  taskName: [
    {
      validator: (_: any, value: any) => {
        if (!createForm.custom) return Promise.resolve()
        return value ? Promise.resolve() : Promise.reject(new Error('请输入任务名称'))
      }
    }
  ],
  planTime: [{ required: true, message: '请选择计划时间' }]
}

const actionModeLabel = computed(() => {
  switch (actionMode.value) {
    case 'create':
      return '手动新建'
    case 'batch':
      return '批量派发'
    default:
      return '自动生成'
  }
})

const templateLabel = computed(() => {
  const current = templateOptions.value.find((t) => t.value === Number(createForm.templateId))
  return current?.label || '未选择模板'
})

function statusLabel(status?: string) {
  switch (status) {
    case 'DONE':
      return '已完成'
    case 'EXCEPTION':
      return '异常'
    default:
      return '待执行'
  }
}

function statusColor(status?: string) {
  switch (status) {
    case 'DONE':
      return 'green'
    case 'EXCEPTION':
      return 'red'
    default:
      return 'blue'
  }
}

async function search() {
  loading.value = true
  try {
    const res: PageResult<CareTaskItem> = await getTaskPage({
      pageNo: page.pageNo,
      pageSize: page.pageSize,
      dateFrom: query.range?.[0] ? query.range[0].format('YYYY-MM-DD') : undefined,
      dateTo: query.range?.[1] ? query.range[1].format('YYYY-MM-DD') : undefined,
      staffId: query.staffId || undefined,
      roomNo: query.roomNo || undefined,
      careLevel: query.careLevel || undefined,
      status: query.status
    })
    tasks.value = res.list
    page.total = res.total
    await loadStaffOptions()
  } finally {
    loading.value = false
  }
}

function reset() {
  query.range = [dayjs().subtract(1, 'day'), dayjs().add(1, 'day')]
  query.staffId = undefined
  query.roomNo = ''
  query.careLevel = ''
  query.status = undefined
  page.pageNo = 1
  search()
}

function exportCsvData() {
  exportCsv(
    tasks.value.map((t) => ({
      老人: t.elderName,
      房间: t.roomNo,
      任务: t.taskName,
      计划时间: t.planTime,
      护工: staffName(t.staffId),
      状态: statusLabel(t.status)
    })),
    '护理任务_今日'
  )
}

function openDetail(row: CareTaskItem) {
  current.value = row
  detailOpen.value = true
}

function openAssign(row: CareTaskItem) {
  assignForm.taskDailyId = row.taskDailyId
  assignForm.staffId = row.staffId || 0
  assignForm.force = true
  assignOpen.value = true
}

function openReview(row: CareTaskItem) {
  reviewForm.taskDailyId = row.taskDailyId
  reviewForm.staffId = row.staffId || 0
  reviewForm.score = 5
  reviewForm.comment = ''
  reviewForm.reviewerType = 'ADMIN'
  reviewForm.reviewTime = dayjs() as any
  reviewOpen.value = true
}

function openCreate() {
  createForm.elderId = 0
  createForm.templateId = 0
  createForm.taskName = ''
  createForm.custom = false
  createForm.planTime = dayjs()
  createForm.staffId = undefined
}

function openGenerate() {
  generateForm.date = dayjs()
  generateForm.force = false
}

function openBatchAssign() {
  batchForm.staffId = 0
  batchForm.range = [dayjs().subtract(1, 'day'), dayjs().add(1, 'day')]
  batchForm.roomNo = ''
  batchForm.floorNo = ''
  batchForm.building = ''
  batchForm.careLevel = ''
  batchForm.status = undefined
  batchForm.force = true
}

function openAction() {
  actionMode.value = 'generate'
  actionStep.value = 0
  openGenerate()
  openCreate()
  openBatchAssign()
  actionOpen.value = true
}

async function submitAssign() {
  try {
    await assignFormRef.value?.validate()
    await new Promise<void>((resolve, reject) => {
      Modal.confirm({
        title: '确认派发任务？',
        onOk: () => resolve(),
        onCancel: () => reject()
      })
    })
    assigning.value = true
    await assignTask(assignForm)
    message.success('派发成功')
    assignOpen.value = false
    search()
  } catch (err) {
    if (!err) return
    message.error('派发失败')
  } finally {
    assigning.value = false
  }
}

async function submitReview() {
  try {
    await reviewFormRef.value?.validate()
    await new Promise<void>((resolve, reject) => {
      Modal.confirm({
        title: '确认提交评价？',
        onOk: () => resolve(),
        onCancel: () => reject()
      })
    })
    reviewing.value = true
    await reviewTask({
      taskDailyId: reviewForm.taskDailyId,
      staffId: Number(reviewForm.staffId),
      score: Number(reviewForm.score),
      comment: reviewForm.comment,
      reviewerType: reviewForm.reviewerType,
      reviewTime: reviewForm.reviewTime ? dayjs(reviewForm.reviewTime).format('YYYY-MM-DDTHH:mm:ss') : undefined
    })
    message.success('评价已提交')
    reviewOpen.value = false
  } catch (err) {
    if (!err) return
    message.error('评价提交失败')
  } finally {
    reviewing.value = false
  }
}

function nextStep() {
  if (actionStep.value === 0) {
    actionStep.value = 1
    return
  }
  if (actionStep.value === 1) {
    if (actionMode.value === 'create') {
      createFormRef.value?.validate?.().then(() => (actionStep.value = 2))
      return
    }
    if (actionMode.value === 'batch') {
      batchFormRef.value?.validate?.().then(() => (actionStep.value = 2))
      return
    }
    actionStep.value = 2
  }
}

function prevStep() {
  actionStep.value = Math.max(0, actionStep.value - 1)
}

async function submitAction() {
  if (actionStep.value < 2) {
    nextStep()
    return
  }
  actionLoading.value = true
  try {
    if (actionMode.value === 'generate') {
      await generateTasks({
        date: generateForm.date ? dayjs(generateForm.date).format('YYYY-MM-DD') : undefined,
        force: !!generateForm.force
      })
      message.success('任务已生成')
    } else if (actionMode.value === 'create') {
      await createTask({
        elderId: Number(createForm.elderId),
        templateId: createForm.custom ? undefined : Number(createForm.templateId),
        taskName: createForm.custom ? createForm.taskName : undefined,
        planTime: dayjs(createForm.planTime).format('YYYY-MM-DDTHH:mm:ss'),
        staffId: createForm.staffId ? Number(createForm.staffId) : undefined
      })
      message.success('任务创建成功')
    } else {
      await assignTaskBatch({
        staffId: Number(batchForm.staffId),
        dateFrom: batchForm.range?.[0] ? batchForm.range[0].format('YYYY-MM-DD') : undefined,
        dateTo: batchForm.range?.[1] ? batchForm.range[1].format('YYYY-MM-DD') : undefined,
        roomNo: batchForm.roomNo || undefined,
        floorNo: batchForm.floorNo || undefined,
        building: batchForm.building || undefined,
        careLevel: batchForm.careLevel || undefined,
        status: batchForm.status || undefined,
        force: !!batchForm.force
      })
      message.success('批量派发完成')
    }
    actionOpen.value = false
    search()
  } catch (err) {
    message.error('操作失败')
  } finally {
    actionLoading.value = false
  }
}

async function loadTemplates() {
  try {
    const res = await getTemplatePage({ pageNo: 1, pageSize: 200 })
    templateOptions.value = res.list.map((t) => ({ label: t.taskName, value: t.id }))
  } catch {
    templateOptions.value = []
  }
}

async function loadStaffOptions(keyword = '') {
  const res = await getStaffPage({ pageNo: 1, pageSize: 200, keyword })
  staffOptions.value = res.list.map((item: any) => ({
    label: item.realName || item.username || `员工#${item.id}`,
    value: item.id
  }))
  staffMap.value = staffOptions.value.reduce((acc, cur) => {
    acc[cur.value] = cur.label
    return acc
  }, {} as Record<number, string>)
}

async function searchStaff(keyword: string) {
  await loadStaffOptions(keyword)
}

async function searchElders(keyword: string) {
  if (!keyword) {
    elderOptions.value = []
    return
  }
  const res = await getElderPage({ pageNo: 1, pageSize: 50, keyword })
  elderOptions.value = res.list.map((item: any) => ({
    label: item.fullName || '未知老人',
    value: item.id
  }))
  elderMap.value = elderOptions.value.reduce((acc, cur) => {
    acc[cur.value] = cur.label
    return acc
  }, {} as Record<number, string>)
}

function staffName(staffId?: number) {
  if (!staffId) return '未分配'
  return staffMap.value[staffId] || '未知护理员'
}

function elderName(elderId?: number) {
  if (!elderId) return '-'
  return elderMap.value[elderId] || '未知老人'
}

loadTemplates()
search()
</script>

<style scoped>
.search-bar {
  margin-bottom: 12px;
  row-gap: 8px;
}
.stepbar {
  margin-bottom: 16px;
}
.step-body {
  min-height: 220px;
  padding: 8px 0;
}
.confirm-box {
  margin-top: 12px;
}
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
</style>
