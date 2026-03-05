<template>
  <PageContainer title="营销方案" sub-title="方案起草-审批-发布-全员阅读闭环">
    <MarketingQuickNav />
    <template #extra>
      <a-space>
        <a-input-search
          v-model:value="query.keyword"
          allow-clear
          placeholder="搜索标题/摘要/责任部门"
          style="width: 280px"
          @search="fetchData"
        />
        <a-select v-model:value="query.status" allow-clear placeholder="状态筛选" style="width: 160px" @change="fetchData">
          <a-select-option value="DRAFT">草稿</a-select-option>
          <a-select-option value="PENDING_APPROVAL">待审批</a-select-option>
          <a-select-option value="APPROVED">审批通过</a-select-option>
          <a-select-option value="REJECTED">已驳回</a-select-option>
          <a-select-option value="PUBLISHED">已发布</a-select-option>
          <a-select-option value="INACTIVE">停用</a-select-option>
        </a-select>
        <a-button type="primary" @click="openCreate">新增方案</a-button>
      </a-space>
    </template>

    <a-tabs v-model:activeKey="activeModule" class="plan-tabs" @change="onModuleChange">
      <a-tab-pane key="SPEECH" tab="营销话术库">
        <a-row :gutter="[16, 16]">
          <a-col v-for="item in speechCards" :key="item.id" :xs="24" :md="12" :xl="8">
            <a-card class="card-elevated plan-card" :bordered="false" hoverable>
              <template #title>
                <div class="plan-card-title">
                  <span>{{ item.title }}</span>
                  <a-tag :color="statusColor(item.status)">{{ statusLabel(item.status) }}</a-tag>
                </div>
              </template>
              <div class="plan-card-summary">{{ item.summary || '暂无摘要，建议补充核心场景。' }}</div>
              <div class="plan-card-meta">
                <span>优先级：{{ item.priority }}</span>
                <span>生效：{{ item.effectiveDate || '--' }}</span>
              </div>
              <div class="plan-card-meta">
                <span>阅读：{{ item.readCount || 0 }}/{{ item.totalStaffCount || 0 }}</span>
                <span>同意：{{ item.agreeCount || 0 }}</span>
              </div>
              <div class="plan-card-actions">
                <a-button type="link" size="small" @click="openPreview(item)">查看详情</a-button>
                <a-button type="link" size="small" :disabled="!canEdit(item)" @click="openEdit(item)">编辑</a-button>
                <a-dropdown>
                  <a-button type="link" size="small">流程操作</a-button>
                  <template #overlay>
                    <a-menu>
                      <a-menu-item :disabled="!canSubmit(item)" @click="submitPlan(item)">提交审批</a-menu-item>
                      <a-menu-item :disabled="!canApprove(item)" @click="approvePlan(item)">审批通过</a-menu-item>
                      <a-menu-item :disabled="!canReject(item)" @click="rejectPlan(item)">驳回</a-menu-item>
                      <a-menu-item :disabled="!canPublish(item)" @click="publishPlan(item)">{{ publishActionLabel(item) }}</a-menu-item>
                      <a-menu-item :disabled="!canDeactivate(item)" @click="deactivatePlan(item)">停用</a-menu-item>
                    </a-menu>
                  </template>
                </a-dropdown>
                <a-popconfirm title="确认删除该方案吗？" @confirm="onDelete(item.id)">
                  <a-button type="link" danger size="small">删除</a-button>
                </a-popconfirm>
              </div>
            </a-card>
          </a-col>
        </a-row>
        <a-empty v-if="!speechCards.length && !loading" description="暂无话术，请先新增" />
      </a-tab-pane>

      <a-tab-pane key="POLICY" tab="季度运营政策">
        <a-card class="card-elevated" :bordered="false">
          <MarketingListToolbar :tip="`已勾选 ${selectedCount} 条，审批通过后可发布，停用后可重新发布`">
            <a-space>
              <a-button :disabled="!selectedPlan" @click="previewSelected">详情</a-button>
              <a-button :disabled="!canEditSelected" @click="editSelected">编辑</a-button>
              <a-button :disabled="!canSubmitSelected" @click="submitSelected">提交审批</a-button>
              <a-button :disabled="!canApproveSelected" @click="approveSelected">审批通过</a-button>
              <a-button :disabled="!canRejectSelected" @click="rejectSelected">驳回</a-button>
              <a-button :disabled="!canPublishSelected" @click="publishSelected">{{ selectedPublishLabel }}</a-button>
              <a-button :disabled="!canDeactivateSelected" @click="deactivateSelected">停用</a-button>
              <a-button :disabled="selectedCount === 0" danger @click="deleteSelected">删除</a-button>
            </a-space>
          </MarketingListToolbar>
          <a-table
            :loading="loading"
            :data-source="policyRows"
            :columns="policyColumns"
            :pagination="pagination"
            row-key="id"
            :row-selection="rowSelection"
            @change="onTableChange"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'status'">
                <a-tag :color="statusColor(record.status)">{{ statusLabel(record.status) }}</a-tag>
              </template>
              <template v-else-if="column.key === 'latestApprovalStatus'">
                <a-tag :color="approvalStatusColor(record.latestApprovalStatus)">{{ approvalStatusLabel(record.latestApprovalStatus) }}</a-tag>
              </template>
              <template v-else-if="column.key === 'workflow'">
                <span>阅读 {{ record.readCount || 0 }}/{{ record.totalStaffCount || 0 }}</span>
              </template>
            </template>
          </a-table>
        </a-card>
      </a-tab-pane>
    </a-tabs>

    <a-modal
      v-model:open="modalOpen"
      :title="editingId ? '编辑营销方案' : '新增营销方案'"
      :confirm-loading="submitLoading"
      width="760px"
      @ok="submitForm"
    >
      <a-form ref="formRef" :model="form" :rules="formRules" layout="vertical">
        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="模块类型" name="moduleType">
              <a-select v-model:value="form.moduleType" :disabled="Boolean(editingId)">
                <a-select-option value="SPEECH">营销话术</a-select-option>
                <a-select-option value="POLICY">季度政策</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="状态" name="status">
              <a-select v-model:value="form.status">
                <a-select-option value="DRAFT">草稿</a-select-option>
                <a-select-option value="PENDING_APPROVAL" disabled>待审批（提交审批后自动）</a-select-option>
                <a-select-option value="APPROVED" disabled>审批通过（审批后自动）</a-select-option>
                <a-select-option value="REJECTED" disabled>已驳回（驳回后自动）</a-select-option>
                <a-select-option value="PUBLISHED" disabled>已发布（发布后自动）</a-select-option>
                <a-select-option value="INACTIVE">停用</a-select-option>
                <a-select-option value="ACTIVE" disabled>已发布（兼容状态）</a-select-option>
                <a-select-option value="DELETED" disabled>已删除（通过删除按钮）</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="标题" name="title">
          <a-input v-model:value="form.title" :maxlength="128" show-count />
        </a-form-item>
        <a-form-item label="摘要" name="summary">
          <a-input v-model:value="form.summary" :maxlength="255" show-count />
        </a-form-item>
        <a-row :gutter="12" v-if="form.moduleType === 'POLICY'">
          <a-col :span="8">
            <a-form-item label="季度" name="quarterLabel">
              <a-input v-model:value="form.quarterLabel" placeholder="如 2026 Q1" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="责任部门" name="owner">
              <a-input v-model:value="form.owner" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="生效日期" name="effectiveDate">
              <a-date-picker v-model:value="form.effectiveDate" style="width: 100%" value-format="YYYY-MM-DD" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="联动负责部门" name="linkedDepartmentIds">
          <a-select
            v-model:value="form.linkedDepartmentIds"
            mode="multiple"
            allow-clear
            placeholder="选择需要联动执行的部门"
            :options="departmentOptions"
          />
        </a-form-item>
        <a-form-item v-if="form.moduleType === 'POLICY'" label="运营目标" name="target">
          <a-input v-model:value="form.target" />
        </a-form-item>
        <a-form-item label="优先级" name="priority">
          <a-input-number v-model:value="form.priority" :min="1" :max="999" style="width: 100%" />
        </a-form-item>
        <a-form-item label="内容详情" name="content">
          <a-textarea v-model:value="form.content" :rows="5" placeholder="可填写详细策略、执行要点和异议处理方式" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-drawer v-model:open="previewOpen" title="方案详情" width="640">
      <a-descriptions :column="1" bordered size="small" v-if="previewRecord">
        <a-descriptions-item label="类型">{{ moduleLabel(previewRecord.moduleType) }}</a-descriptions-item>
        <a-descriptions-item label="标题">{{ previewRecord.title }}</a-descriptions-item>
        <a-descriptions-item label="状态">{{ statusLabel(previewRecord.status) }}</a-descriptions-item>
        <a-descriptions-item label="摘要">{{ previewRecord.summary || '--' }}</a-descriptions-item>
        <a-descriptions-item label="季度">{{ previewRecord.quarterLabel || '--' }}</a-descriptions-item>
        <a-descriptions-item label="目标">{{ previewRecord.target || '--' }}</a-descriptions-item>
        <a-descriptions-item label="联动部门">{{ (previewRecord.linkedDepartmentNames || []).join('、') || '--' }}</a-descriptions-item>
        <a-descriptions-item label="审批状态">{{ previewRecord.latestApprovalStatus || '--' }}</a-descriptions-item>
        <a-descriptions-item label="审批意见">{{ previewRecord.latestApprovalRemark || '--' }}</a-descriptions-item>
        <a-descriptions-item label="阅读统计">
          已读 {{ previewRecord.readCount || 0 }}/{{ previewRecord.totalStaffCount || 0 }}，同意 {{ previewRecord.agreeCount || 0 }}，改进 {{ previewRecord.improveCount || 0 }}
        </a-descriptions-item>
        <a-descriptions-item label="内容">
          <div class="preview-content">{{ previewRecord.content || '暂无详细内容' }}</div>
        </a-descriptions-item>
      </a-descriptions>

      <a-card class="card-elevated" :bordered="false" style="margin-top: 12px" v-if="previewRecord">
        <template #title>阅读回执</template>
        <a-space style="margin-bottom: 10px">
          <a-button size="small" @click="refreshWorkflow(previewRecord.id)">刷新</a-button>
          <a-button size="small" type="primary" @click="openReadConfirm('AGREE')">我已同意</a-button>
          <a-button size="small" @click="openReadConfirm('IMPROVE')">提交改进</a-button>
        </a-space>
        <a-table :columns="receiptColumns" :data-source="receipts" size="small" :pagination="false" row-key="staffId" />
      </a-card>
    </a-drawer>

    <a-modal v-model:open="confirmOpen" :title="confirmForm.action === 'AGREE' ? '确认同意方案' : '提交改进建议'" @ok="submitReadConfirm">
      <a-form :model="confirmForm" layout="vertical">
        <a-form-item label="确认动作">
          <a-radio-group v-model:value="confirmForm.action">
            <a-radio value="AGREE">同意方案</a-radio>
            <a-radio value="IMPROVE">方案改进</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="改进详情" v-if="confirmForm.action === 'IMPROVE'">
          <a-textarea v-model:value="confirmForm.actionDetail" :rows="4" placeholder="请输入可执行的改进建议" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, h, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { Input, message, Modal } from 'ant-design-vue'
import type { FormInstance, TablePaginationConfig } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import MarketingListToolbar from './components/MarketingListToolbar.vue'
import MarketingQuickNav from './components/MarketingQuickNav.vue'
import {
  approveMarketingPlan,
  confirmMarketingPlanRead,
  createMarketingPlan,
  deactivateMarketingPlan,
  deleteMarketingPlan,
  getMarketingPlanDetail,
  getMarketingPlanPage,
  getMarketingPlanReceipts,
  publishMarketingPlan,
  rejectMarketingPlan,
  submitMarketingPlan,
  updateMarketingPlan
} from '../../api/marketing'
import { getDepartmentPage } from '../../api/department'
import type {
  DepartmentItem,
  MarketingPlanItem,
  MarketingPlanPayload,
  MarketingPlanQuery,
  MarketingPlanReceiptItem,
  MarketingPlanStatus
} from '../../types'

const route = useRoute()
const activeModule = ref<'SPEECH' | 'POLICY'>('SPEECH')
const loading = ref(false)
const submitLoading = ref(false)
const modalOpen = ref(false)
const previewOpen = ref(false)
const confirmOpen = ref(false)
const previewRecord = ref<MarketingPlanItem>()
const editingId = ref<number | string>()
const formRef = ref<FormInstance>()
const receipts = ref<MarketingPlanReceiptItem[]>([])
const departmentOptions = ref<{ label: string; value: string }[]>([])

const query = reactive<MarketingPlanQuery>({
  pageNo: 1,
  pageSize: 10,
  moduleType: 'SPEECH'
})

const pagination = reactive<TablePaginationConfig>({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total) => `共 ${total} 条`
})

const rows = ref<MarketingPlanItem[]>([])
const selectedRowKeys = ref<string[]>([])

const policyColumns = [
  { title: '季度', dataIndex: 'quarterLabel', key: 'quarterLabel', width: 120 },
  { title: '标题', dataIndex: 'title', key: 'title', width: 200 },
  { title: '运营目标', dataIndex: 'target', key: 'target', width: 240 },
  { title: '责任部门', dataIndex: 'owner', key: 'owner', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 110 },
  { title: '审批状态', dataIndex: 'latestApprovalStatus', key: 'latestApprovalStatus', width: 130 },
  { title: '审批备注', dataIndex: 'latestApprovalRemark', key: 'latestApprovalRemark', width: 180 },
  { title: '阅读', key: 'workflow', width: 130 }
]

const receiptColumns = [
  { title: '姓名', dataIndex: 'staffName', key: 'staffName', width: 110 },
  { title: '已读时间', dataIndex: 'readTime', key: 'readTime', width: 170 },
  { title: '确认动作', dataIndex: 'action', key: 'action', width: 90 },
  { title: '改进详情', dataIndex: 'actionDetail', key: 'actionDetail' }
]

const form = reactive({
  moduleType: 'SPEECH' as 'SPEECH' | 'POLICY',
  title: '',
  summary: '',
  content: '',
  quarterLabel: '',
  target: '',
  owner: '',
  linkedDepartmentIds: [] as string[],
  priority: 50,
  status: 'DRAFT' as MarketingPlanStatus,
  effectiveDate: undefined as string | undefined
})

const confirmForm = reactive({
  action: 'AGREE' as 'AGREE' | 'IMPROVE',
  actionDetail: ''
})

const formRules = {
  moduleType: [{ required: true, message: '请选择模块类型' }],
  title: [{ required: true, message: '请输入标题' }],
  status: [{ required: true, message: '请选择状态' }],
  priority: [{ required: true, type: 'number', message: '请输入优先级' }]
}

const speechCards = computed(() => rows.value)
const policyRows = computed(() => rows.value)
const selectedCount = computed(() => selectedRowKeys.value.length)
const selectedPlans = computed(() => policyRows.value.filter((item) => selectedRowKeys.value.some((id) => String(id) === String(item.id))))
const selectedPlan = computed(() => (selectedPlans.value.length === 1 ? selectedPlans.value[0] : null))
const selectedEditablePlans = computed(() => selectedPlans.value.filter(canEdit))
const selectedSubmittablePlans = computed(() => selectedPlans.value.filter(canSubmit))
const selectedApprovablePlans = computed(() => selectedPlans.value.filter(canApprove))
const selectedRejectablePlans = computed(() => selectedPlans.value.filter(canReject))
const selectedPublishablePlans = computed(() => selectedPlans.value.filter(canPublish))
const selectedDeactivatablePlans = computed(() => selectedPlans.value.filter(canDeactivate))
const selectedPublishLabel = computed(() => {
  if (!selectedPlan.value || selectedPlans.value.length > 1) return '批量发布'
  return publishActionLabel(selectedPlan.value)
})
const canEditSelected = computed(() => selectedPlans.value.length === 1 && selectedEditablePlans.value.length === 1)
const canSubmitSelected = computed(() => selectedSubmittablePlans.value.length > 0)
const canApproveSelected = computed(() => selectedApprovablePlans.value.length > 0)
const canRejectSelected = computed(() => selectedRejectablePlans.value.length > 0)
const canPublishSelected = computed(() => selectedPublishablePlans.value.length > 0)
const canDeactivateSelected = computed(() => selectedDeactivatablePlans.value.length > 0)
const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: Array<number | string>) => {
    selectedRowKeys.value = keys.map((key) => String(key))
  }
}))

function canSubmit(item: MarketingPlanItem) {
  return ['DRAFT', 'REJECTED', 'INACTIVE'].includes(item.status || '')
}

function canEdit(item: MarketingPlanItem) {
  return ['DRAFT', 'REJECTED', 'INACTIVE'].includes(item.status || '')
}

function canApprove(item: MarketingPlanItem) {
  return item.status === 'PENDING_APPROVAL'
}

function canReject(item: MarketingPlanItem) {
  return item.status === 'PENDING_APPROVAL'
}

function canPublish(item: MarketingPlanItem) {
  return ['APPROVED', 'INACTIVE'].includes(item.status || '')
}

function canDeactivate(item: MarketingPlanItem) {
  return item.status === 'PUBLISHED'
}

function publishActionLabel(item: MarketingPlanItem) {
  return item.status === 'INACTIVE' ? '重新发布' : '发布'
}

function requireSingleSelection(actionLabel: string) {
  if (!selectedPlan.value) {
    message.info(`请先勾选 1 条政策后再${actionLabel}`)
    return null
  }
  return selectedPlan.value
}

async function fetchData() {
  loading.value = true
  selectedRowKeys.value = []
  try {
    const params: MarketingPlanQuery = {
      pageNo: pagination.current || 1,
      pageSize: pagination.pageSize || 10,
      moduleType: activeModule.value,
      status: query.status,
      keyword: query.keyword?.trim()
    }
    const page = await getMarketingPlanPage(params)
    rows.value = (page.list || []).map((item) => ({
      ...item,
      id: String(item.id),
      linkedDepartmentIds: (item.linkedDepartmentIds || []).map((deptId) => String(deptId))
    }))
    pagination.total = page.total || 0
  } catch (error: any) {
    message.error(error?.message || '加载营销方案失败')
  } finally {
    loading.value = false
  }
}

async function fetchDepartments() {
  try {
    const page = await getDepartmentPage({ pageNo: 1, pageSize: 500, status: 1 })
    const list = page.list || []
    departmentOptions.value = list.map((item: DepartmentItem) => ({
      label: item.deptName || `部门${item.id}`,
      value: String(item.id)
    }))
  } catch {
    departmentOptions.value = []
  }
}

function onModuleChange(tab: string) {
  activeModule.value = tab as 'SPEECH' | 'POLICY'
  pagination.current = 1
  selectedRowKeys.value = []
  fetchData()
}

function onTableChange(pag: TablePaginationConfig) {
  pagination.current = pag.current || 1
  pagination.pageSize = pag.pageSize || 10
  selectedRowKeys.value = []
  fetchData()
}

function openCreate() {
  editingId.value = undefined
  form.moduleType = activeModule.value
  form.title = ''
  form.summary = ''
  form.content = ''
  form.quarterLabel = ''
  form.target = ''
  form.owner = ''
  form.linkedDepartmentIds = []
  form.priority = 50
  form.status = 'DRAFT'
  form.effectiveDate = undefined
  modalOpen.value = true
}

function openEdit(item: MarketingPlanItem) {
  if (!canEdit(item)) {
    message.warning(`当前状态【${statusLabel(item.status)}】不能编辑`)
    return
  }
  editingId.value = item.id
  form.moduleType = item.moduleType
  form.title = item.title || ''
  form.summary = item.summary || ''
  form.content = item.content || ''
  form.quarterLabel = item.quarterLabel || ''
  form.target = item.target || ''
  form.owner = item.owner || ''
  form.linkedDepartmentIds = item.linkedDepartmentIds || []
  form.priority = item.priority || 50
  form.status = item.status || 'DRAFT'
  form.effectiveDate = item.effectiveDate
  modalOpen.value = true
}

async function openPreview(item: MarketingPlanItem) {
  await refreshWorkflow(item.id)
  previewOpen.value = true
}

async function submitForm() {
  try {
    await formRef.value?.validate()
    const payload: MarketingPlanPayload = {
      moduleType: form.moduleType,
      title: form.title.trim(),
      summary: trimOrUndefined(form.summary),
      content: trimOrUndefined(form.content),
      quarterLabel: trimOrUndefined(form.quarterLabel),
      target: trimOrUndefined(form.target),
      owner: trimOrUndefined(form.owner),
      linkedDepartmentIds: form.linkedDepartmentIds,
      priority: form.priority,
      status: form.status,
      effectiveDate: form.effectiveDate
    }
    submitLoading.value = true
    if (editingId.value) {
      await updateMarketingPlan(editingId.value, payload)
      message.success('更新成功')
    } else {
      await createMarketingPlan(payload)
      message.success('新增成功')
    }
    modalOpen.value = false
    fetchData()
  } catch (error: any) {
    if (error?.errorFields) return
    message.error(error?.message || '保存失败')
  } finally {
    submitLoading.value = false
  }
}

async function onDelete(id: number | string) {
  try {
    await deleteMarketingPlan(id)
    message.success('删除成功')
    if (rows.value.length === 1 && (pagination.current || 1) > 1) {
      pagination.current = (pagination.current || 1) - 1
    }
    fetchData()
  } catch (error: any) {
    message.error(error?.message || '删除失败')
  }
}

function previewSelected() {
  const plan = requireSingleSelection('查看详情')
  if (!plan) return
  openPreview(plan)
}

function editSelected() {
  const plan = requireSingleSelection('编辑')
  if (!plan) return
  if (!canEdit(plan)) {
    message.warning(`当前状态【${statusLabel(plan.status)}】不能编辑`)
    return
  }
  openEdit(plan)
}

async function submitSelected() {
  if (selectedPlans.value.length === 1 && selectedPlan.value) {
    if (!canSubmit(selectedPlan.value)) {
      message.warning(`当前状态【${statusLabel(selectedPlan.value.status)}】不能提交审批`)
      return
    }
    await submitPlan(selectedPlan.value)
    return
  }
  await batchExecute('提交审批', selectedSubmittablePlans.value, async (item) => {
    await submitMarketingPlan(item.id)
  })
}

async function approveSelected() {
  if (selectedPlans.value.length === 1 && selectedPlan.value) {
    if (!canApprove(selectedPlan.value)) {
      message.warning(`当前状态【${statusLabel(selectedPlan.value.status)}】不能审批通过`)
      return
    }
    await approvePlan(selectedPlan.value)
    return
  }
  const plans = selectedApprovablePlans.value
  if (!plans.length) {
    message.info('勾选项中没有可审批通过的政策')
    return
  }
  let approvalRemark = ''
  Modal.confirm({
    title: `确认批量审批通过 ${plans.length} 条政策吗？`,
    content: h(Input.TextArea, {
      rows: 4,
      placeholder: '请输入审批意见（可选）',
      onChange: (event: any) => {
        approvalRemark = String(event?.target?.value || '')
      }
    }),
    onOk: async () => {
      await batchExecute('审批通过', plans, async (item) => {
        await approveMarketingPlan(item.id, { remark: trimOrUndefined(approvalRemark) || '院长批量审批通过' })
      })
    }
  })
}

function rejectSelected() {
  if (selectedPlans.value.length === 1 && selectedPlan.value) {
    if (!canReject(selectedPlan.value)) {
      message.warning(`当前状态【${statusLabel(selectedPlan.value.status)}】不能驳回`)
      return
    }
    rejectPlan(selectedPlan.value)
    return
  }
  const plans = selectedRejectablePlans.value
  if (!plans.length) {
    message.info('勾选项中没有可驳回的政策')
    return
  }
  let rejectRemark = ''
  Modal.confirm({
    title: `确认批量驳回 ${plans.length} 条政策吗？`,
    content: h(Input.TextArea, {
      rows: 4,
      placeholder: '请输入驳回原因（建议必填）',
      onChange: (event: any) => {
        rejectRemark = String(event?.target?.value || '')
      }
    }),
    onOk: async () => {
      await batchExecute('驳回', plans, async (item) => {
        await rejectMarketingPlan(item.id, { remark: trimOrUndefined(rejectRemark) || '请补充执行细节后重新提交' })
      })
    }
  })
}

async function publishSelected() {
  if (selectedPlans.value.length === 1 && selectedPlan.value) {
    if (!canPublish(selectedPlan.value)) {
      message.warning(`当前状态【${statusLabel(selectedPlan.value.status)}】不能发布`)
      return
    }
    await publishPlan(selectedPlan.value)
    return
  }
  await batchExecute(selectedPublishLabel.value, selectedPublishablePlans.value, async (item) => {
    await publishMarketingPlan(item.id)
  })
}

async function deactivateSelected() {
  if (selectedPlans.value.length === 1 && selectedPlan.value) {
    if (!canDeactivate(selectedPlan.value)) {
      message.warning(`当前状态【${statusLabel(selectedPlan.value.status)}】不能停用`)
      return
    }
    await deactivatePlan(selectedPlan.value)
    return
  }
  await batchExecute('停用', selectedDeactivatablePlans.value, async (item) => {
    await deactivateMarketingPlan(item.id)
  })
}

function deleteSelected() {
  const ids = selectedPlans.value.map((item) => item.id)
  if (!ids.length) {
    message.info('请先勾选要删除的政策')
    return
  }
  Modal.confirm({
    title: `确认删除选中的 ${ids.length} 条政策吗？`,
    onOk: async () => {
      const settled = await Promise.allSettled(ids.map((id) => deleteMarketingPlan(id)))
      const successCount = settled.filter((item) => item.status === 'fulfilled').length
      const failedCount = settled.length - successCount
      if (failedCount > 0) {
        message.warning(`删除完成：成功 ${successCount} 条，失败 ${failedCount} 条`)
      } else {
        message.success(`删除成功（${successCount} 条）`)
      }
      selectedRowKeys.value = []
      fetchData()
    }
  })
}

async function submitPlan(item: MarketingPlanItem) {
  if (!canSubmit(item)) {
    message.warning(`当前状态【${statusLabel(item.status)}】不能提交审批`)
    return
  }
  try {
    await submitMarketingPlan(item.id)
    message.success('已提交审批')
    fetchData()
  } catch (error: any) {
    message.error(error?.message || '提交失败')
  }
}

async function batchExecute(
  actionLabel: string,
  plans: MarketingPlanItem[],
  executor: (item: MarketingPlanItem) => Promise<unknown>
) {
  if (!plans.length) {
    message.info(`勾选项中没有可${actionLabel}的政策`)
    return
  }
  const settled = await Promise.allSettled(plans.map((item) => executor(item)))
  const successCount = settled.filter((item) => item.status === 'fulfilled').length
  const failedCount = settled.length - successCount
  const firstRejected = settled.find((item) => item.status === 'rejected') as PromiseRejectedResult | undefined
  if (failedCount > 0) {
    const reason = firstRejected?.reason?.message || firstRejected?.reason || ''
    message.warning(`${actionLabel}完成：成功 ${successCount} 条，失败 ${failedCount} 条${reason ? `（${reason}）` : ''}`)
  } else {
    message.success(`${actionLabel}成功（${successCount} 条）`)
  }
  selectedRowKeys.value = []
  await fetchData()
}

async function approvePlan(item: MarketingPlanItem) {
  if (!canApprove(item)) {
    message.warning(`当前状态【${statusLabel(item.status)}】不能审批通过`)
    return
  }
  let approvalRemark = ''
  Modal.confirm({
    title: '审批通过',
    content: h(Input.TextArea, {
      rows: 4,
      placeholder: '请输入审批意见（可选）',
      onChange: (event: any) => {
        approvalRemark = String(event?.target?.value || '')
      }
    }),
    onOk: async () => {
      try {
        await approveMarketingPlan(item.id, { remark: trimOrUndefined(approvalRemark) || '院长审批通过' })
        message.success('已审批通过')
        fetchData()
        if (String(previewRecord.value?.id || '') === String(item.id)) await refreshWorkflow(item.id)
      } catch (error: any) {
        message.error(error?.message || '审批失败')
      }
    }
  })
}

function rejectPlan(item: MarketingPlanItem) {
  if (!canReject(item)) {
    message.warning(`当前状态【${statusLabel(item.status)}】不能驳回`)
    return
  }
  let rejectRemark = ''
  Modal.confirm({
    title: '确认驳回当前方案吗？',
    content: h(Input.TextArea, {
      rows: 4,
      placeholder: '请输入驳回原因（建议必填）',
      onChange: (event: any) => {
        rejectRemark = String(event?.target?.value || '')
      }
    }),
    onOk: async () => {
      await rejectMarketingPlan(item.id, { remark: trimOrUndefined(rejectRemark) || '请补充执行细节后重新提交' })
      message.success('已驳回')
      fetchData()
      if (String(previewRecord.value?.id || '') === String(item.id)) await refreshWorkflow(item.id)
    }
  })
}

async function publishPlan(item: MarketingPlanItem) {
  if (!canPublish(item)) {
    message.warning(`当前状态【${statusLabel(item.status)}】不能发布`)
    return
  }
  try {
    await publishMarketingPlan(item.id)
    message.success('已发布，已同步生成待办和绩效联动记录')
    fetchData()
    if (String(previewRecord.value?.id || '') === String(item.id)) await refreshWorkflow(item.id)
  } catch (error: any) {
    message.error(error?.message || '发布失败')
  }
}

async function deactivatePlan(item: MarketingPlanItem) {
  if (!canDeactivate(item)) {
    message.warning(`当前状态【${statusLabel(item.status)}】不能停用`)
    return
  }
  try {
    await deactivateMarketingPlan(item.id)
    message.success('方案已停用')
    fetchData()
    if (String(previewRecord.value?.id || '') === String(item.id)) await refreshWorkflow(item.id)
  } catch (error: any) {
    message.error(error?.message || '停用失败')
  }
}

async function refreshWorkflow(id: number | string) {
  try {
    const [detail, receiptList] = await Promise.all([getMarketingPlanDetail(id), getMarketingPlanReceipts(id)])
    previewRecord.value = detail
    receipts.value = receiptList || []
  } catch (error: any) {
    message.error(error?.message || '加载方案详情失败')
  }
}

function openReadConfirm(action: 'AGREE' | 'IMPROVE') {
  confirmForm.action = action
  confirmForm.actionDetail = ''
  confirmOpen.value = true
}

async function submitReadConfirm() {
  if (!previewRecord.value) return
  if (confirmForm.action === 'IMPROVE' && !confirmForm.actionDetail.trim()) {
    message.warning('请填写改进详情')
    return
  }
  try {
    await confirmMarketingPlanRead(previewRecord.value.id, {
      action: confirmForm.action,
      actionDetail: trimOrUndefined(confirmForm.actionDetail)
    })
    message.success('已提交阅读确认')
    confirmOpen.value = false
    await refreshWorkflow(previewRecord.value.id)
    fetchData()
  } catch (error: any) {
    message.error(error?.message || '提交失败')
  }
}

function moduleLabel(moduleType: string) {
  return moduleType === 'POLICY' ? '季度政策' : '营销话术'
}

function statusLabel(status?: string) {
  const map: Record<string, string> = {
    DRAFT: '草稿',
    PENDING_APPROVAL: '待审批',
    APPROVED: '审批通过（待发布）',
    REJECTED: '已驳回',
    PUBLISHED: '已发布',
    INACTIVE: '停用',
    ACTIVE: '已发布'
  }
  return map[status || ''] || status || '--'
}

function statusColor(status?: string) {
  if (status === 'PUBLISHED' || status === 'ACTIVE') return 'green'
  if (status === 'APPROVED') return 'blue'
  if (status === 'PENDING_APPROVAL') return 'orange'
  if (status === 'REJECTED') return 'red'
  return 'default'
}

function approvalStatusLabel(status?: string) {
  if (!status) return '--'
  if (status === 'PENDING') return '待审批'
  if (status === 'APPROVED') return '已审批通过'
  if (status === 'REJECTED') return '已驳回'
  return status
}

function approvalStatusColor(status?: string) {
  if (status === 'APPROVED') return 'green'
  if (status === 'PENDING') return 'orange'
  if (status === 'REJECTED') return 'red'
  return 'default'
}

function trimOrUndefined(value?: string) {
  const trimmed = value?.trim()
  return trimmed ? trimmed : undefined
}

function initByQuery() {
  const moduleType = String(route.query.moduleType || '').trim().toUpperCase()
  if (moduleType === 'SPEECH' || moduleType === 'POLICY') {
    activeModule.value = moduleType
  }
  const status = String(route.query.status || '').trim().toUpperCase()
  if (['DRAFT', 'PENDING_APPROVAL', 'APPROVED', 'REJECTED', 'PUBLISHED', 'INACTIVE'].includes(status)) {
    query.status = status as MarketingPlanStatus
  }
  const keyword = String(route.query.keyword || '').trim()
  if (keyword) {
    query.keyword = keyword
  }
}

onMounted(() => {
  initByQuery()
  fetchDepartments()
  fetchData()
})
</script>

<style scoped>
.plan-tabs {
  margin-top: 6px;
}

.plan-card {
  min-height: 228px;
}

.plan-card-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.plan-card-summary {
  min-height: 56px;
  color: var(--muted);
  line-height: 1.6;
}

.plan-card-meta {
  display: flex;
  justify-content: space-between;
  color: var(--muted);
  font-size: 12px;
  margin-top: 8px;
}

.plan-card-actions {
  margin-top: 8px;
  display: flex;
  justify-content: flex-end;
  gap: 2px;
}

.preview-content {
  white-space: pre-wrap;
  line-height: 1.7;
}
</style>
