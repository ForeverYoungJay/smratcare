<template>
  <PageContainer title="服务计划" subTitle="老人服务计划编排与执行人设置">
    <a-card :bordered="false" class="card-elevated">
      <a-form layout="inline" @submit.prevent>
        <a-form-item label="关键字">
          <a-input v-model:value="query.keyword" placeholder="计划名称" allow-clear />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="query.status" :options="statusOptions" allow-clear style="width: 140px" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="search">查询</a-button>
            <a-button @click="reset">重置</a-button>
            <a-button type="primary" @click="openModal()">新增计划</a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <a-table row-key="id" :columns="columns" :data-source="rows" :loading="loading" :pagination="pagination" @change="onTableChange">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="statusColor(record.status)">{{ statusLabel(record.status) }}</a-tag>
          </template>
          <template v-else-if="column.key === 'approval'">
            <a-tag :color="APPROVAL[record.approvalStatus || 'PUBLISHED']?.color">{{ APPROVAL[record.approvalStatus || 'PUBLISHED']?.label || record.approvalStatus }}</a-tag>
            <span class="plan-rev">v{{ record.revision || 1 }}</span>
            <div v-if="record.approvalStatus === 'REJECTED' && record.rejectReason" class="plan-reject">退回：{{ record.rejectReason }}</div>
          </template>
          <template v-else-if="column.key === 'actions'">
            <div class="row-action-links">
              <a-button type="link" size="small" @click="openModal(record)">编辑</a-button>
              <template v-if="canApprove && record.approvalStatus === 'PENDING'">
                <a-button type="link" size="small" @click="approve(record)">通过发布</a-button>
                <a-button type="link" size="small" danger @click="openReject(record)">退回</a-button>
              </template>
              <a-button v-if="(record.revision || 1) > 1" type="link" size="small" @click="openRevisions(record)">历史版本</a-button>
              <a-button v-if="record.approvalStatus && record.approvalStatus !== 'PUBLISHED' && (record.revision || 1) === 1" type="link" size="small" danger @click="remove(record.id)">删除</a-button>
            </div>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal :mask-closable="false" v-model:open="modalOpen" title="服务计划" :confirm-loading="submitting" width="760" @ok="submit">
      <a-form ref="formRef" layout="vertical" :model="form" :rules="rules">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="计划名称" name="planName" required>
              <a-input v-model:value="form.planName" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="老人" name="elderId" required>
              <a-select
                v-model:value="form.elderId"
                show-search
                :filter-option="false"
                :options="elderOptions"
                @search="searchElder"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="服务项目" name="serviceItemId" required>
              <a-select v-model:value="form.serviceItemId" :options="serviceItemOptions" show-search option-filter-prop="label" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="护理等级" name="careLevelId">
              <a-select v-model:value="form.careLevelId" allow-clear :options="careLevelOptions" show-search option-filter-prop="label" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="周期" name="cycleType">
              <a-select v-model:value="form.cycleType" :options="cycleOptions" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="频次" name="frequency">
              <a-input-number v-model:value="form.frequency" :min="1" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="默认护工" name="defaultStaffId">
              <a-select
                v-model:value="form.defaultStaffId"
                show-search
                allow-clear
                :filter-option="false"
                :options="staffOptions"
                @search="searchStaff"
                @focus="() => !staffOptions.length && searchStaff('')"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="开始日期" name="startDate" required>
              <a-date-picker v-model:value="form.startDate" value-format="YYYY-MM-DD" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="结束日期" name="endDate">
              <a-date-picker v-model:value="form.endDate" value-format="YYYY-MM-DD" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="状态" name="status">
              <a-select v-model:value="form.status" :options="statusOptions" />
            </a-form-item>
          </a-col>
          <a-col :span="16">
            <a-form-item label="备注" name="remark">
              <a-input v-model:value="form.remark" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item v-if="editingPublished" label="变更原因" required extra="已发布的计划修改后保留旧版本，并重新提交审核">
          <a-input v-model:value="form.changeReason" :maxlength="120" placeholder="例如：复评结果变化，增加夜间巡视" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="rejectOpen" title="退回护理计划" :confirm-loading="submitting" @ok="submitReject">
      <a-textarea v-model:value="rejectReason" :rows="3" :maxlength="200" placeholder="请写明退回原因" />
    </a-modal>

    <a-modal v-model:open="revisionsOpen" title="历史版本" :footer="null" width="720">
      <a-table row-key="id" size="small" :pagination="false" :data-source="revisions" :columns="revisionColumns">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'content'">{{ snapshotText(record.snapshotJson) }}</template>
        </template>
      </a-table>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'
import { useRoute } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import { useElderOptions } from '../../composables/useElderOptions'
import { useStaffOptions } from '../../composables/useStaffOptions'
import { listServiceItems } from '../../api/standard'
import { approveServicePlan, createServicePlan, deleteServicePlan, getCareLevelList, getServicePlanPage, getServicePlanRevisions, rejectServicePlan, updateServicePlan } from '../../api/nursing'
import { useUserStore } from '../../stores/user'
import { normalizeResidentId } from '../../utils/id'
import type { CareLevelItem, Id, PageResult, ServiceItem, ServicePlanItem, ServicePlanRevisionItem } from '../../types'
import { resolveCareError } from './careError'

const rows = ref<ServicePlanItem[]>([])
const route = useRoute()
const loading = ref(false)
const modalOpen = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const userStore = useUserStore()
const canApprove = computed(() => (userStore.roles || []).some((role) => ['NURSING_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'].includes(String(role || '').toUpperCase())))
const APPROVAL: Record<string, { label: string; color: string }> = {
  PENDING: { label: '待审核', color: 'orange' }, PUBLISHED: { label: '已发布', color: 'green' }, REJECTED: { label: '已退回', color: 'red' }
}
const editingPublished = computed(() => !!form.id && editingStatus.value === 'PUBLISHED')
const editingStatus = ref<string>()
const rejectOpen = ref(false)
const rejectReason = ref('')
const rejectTarget = ref<ServicePlanItem | null>(null)
const revisionsOpen = ref(false)
const revisions = ref<ServicePlanRevisionItem[]>([])
const revisionColumns = [
  { title: '版本', dataIndex: 'revision', width: 70 },
  { title: '当时内容', key: 'content' },
  { title: '变更原因', dataIndex: 'changeReason', width: 180 },
  { title: '变更人', dataIndex: 'changedByName', width: 100 },
  { title: '时间', dataIndex: 'createTime', width: 160 }
]

function snapshotText(json?: string) {
  try {
    const old = JSON.parse(json || '{}')
    return [old.planName, old.cycleType, old.frequency ? `${old.frequency}次` : '', old.startDate && `${old.startDate}起`].filter(Boolean).join(' · ')
  } catch {
    return '-'
  }
}

async function approve(record: ServicePlanItem) {
  try {
    await approveServicePlan(record.id)
    message.success('计划已发布')
    await load()
  } catch (error) {
    message.error(resolveCareError(error, '发布失败'))
  }
}

function openReject(record: ServicePlanItem) {
  rejectTarget.value = record
  rejectReason.value = ''
  rejectOpen.value = true
}

async function submitReject() {
  if (!rejectTarget.value) return
  if (!rejectReason.value.trim()) return message.warning('请写明退回原因')
  submitting.value = true
  try {
    await rejectServicePlan(rejectTarget.value.id, rejectReason.value.trim())
    rejectOpen.value = false
    message.success('已退回')
    await load()
  } catch (error) {
    message.error(resolveCareError(error, '退回失败'))
  } finally {
    submitting.value = false
  }
}

async function openRevisions(record: ServicePlanItem) {
  revisions.value = await getServicePlanRevisions(record.id).catch(() => [])
  revisionsOpen.value = true
}

const { elderOptions, searchElders, ensureSelectedElder } = useElderOptions({ pageSize: 50 })
const { staffOptions, searchStaff, ensureSelectedStaff } = useStaffOptions({ pageSize: 120 })
const careLevelOptions = ref<Array<{ label: string; value: number }>>([])
const serviceItemOptions = ref<Array<{ label: string; value: number }>>([])

const query = reactive({ pageNo: 1, pageSize: 10, total: 0, keyword: undefined as string | undefined, status: undefined as string | undefined })

const form = reactive<Partial<ServicePlanItem>>({
  planName: '',
  elderId: undefined,
  careLevelId: undefined,
  serviceItemId: undefined,
  cycleType: 'DAILY',
  frequency: 1,
  startDate: '',
  endDate: undefined,
  defaultStaffId: undefined,
  status: 'ACTIVE',
  remark: ''
})

const rules = {
  planName: [{ required: true, message: '请输入计划名称', trigger: 'blur' }],
  elderId: [{ required: true, message: '请选择老人', trigger: 'change' }],
  serviceItemId: [{ required: true, message: '请选择服务项目', trigger: 'change' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }]
}

const cycleOptions = [
  { label: '每日', value: 'DAILY' },
  { label: '每周', value: 'WEEKLY' },
  { label: '每月', value: 'MONTHLY' }
]

const statusOptions = [
  { label: '生效', value: 'ACTIVE' },
  { label: '暂停', value: 'PAUSED' },
  { label: '关闭', value: 'CLOSED' }
]

const columns = [
  { title: '计划名称', dataIndex: 'planName', key: 'planName', width: 180 },
  { title: '老人', dataIndex: 'elderName', key: 'elderName', width: 120 },
  { title: '服务项目', dataIndex: 'serviceItemName', key: 'serviceItemName', width: 140 },
  { title: '周期', dataIndex: 'cycleType', key: 'cycleType', width: 100 },
  { title: '频次', dataIndex: 'frequency', key: 'frequency', width: 80 },
  { title: '开始', dataIndex: 'startDate', key: 'startDate', width: 110 },
  { title: '结束', dataIndex: 'endDate', key: 'endDate', width: 110 },
  { title: '状态', key: 'status', width: 100 },
  { title: '审核', key: 'approval', width: 120 },
  { title: '操作', key: 'actions', width: 220 }
]

const pagination = computed(() => ({ current: query.pageNo, pageSize: query.pageSize, total: query.total, showSizeChanger: true }))

function statusLabel(status?: string) {
  if (status === 'PAUSED') return '暂停'
  if (status === 'CLOSED') return '关闭'
  return '生效'
}

function statusColor(status?: string) {
  if (status === 'PAUSED') return 'orange'
  if (status === 'CLOSED') return 'default'
  return 'green'
}

function resetForm() {
  form.id = undefined
  form.planName = ''
  form.elderId = undefined
  form.careLevelId = undefined
  form.serviceItemId = undefined
  form.cycleType = 'DAILY'
  form.frequency = 1
  form.startDate = ''
  form.endDate = undefined
  form.defaultStaffId = undefined
  form.status = 'ACTIVE'
  form.remark = ''
}

function openModal(record?: ServicePlanItem) {
  resetForm()
  form.changeReason = ''
  editingStatus.value = record ? (record.approvalStatus || 'PUBLISHED') : undefined
  if (record) {
    Object.assign(form, record)
    ensureSelectedElder(record.elderId, record.elderName)
    ensureSelectedStaff(record.defaultStaffId, record.defaultStaffName)
  } else {
    const residentId = normalizeResidentId(route.query as Record<string, unknown>)
    const residentName = typeof route.query.residentName === 'string' ? route.query.residentName : undefined
    if (residentId) {
      form.elderId = residentId
      ensureSelectedElder(form.elderId, residentName)
    }
  }
  modalOpen.value = true
}

async function submit() {
  try {
    await formRef.value?.validate()
    if (submitting.value) return
    if (editingPublished.value && !form.changeReason?.trim()) {
      message.warning('修改已发布的计划请填写变更原因')
      return
    }
    submitting.value = true
    const payload: Partial<ServicePlanItem> = {
      planName: form.planName,
      elderId: form.elderId,
      careLevelId: form.careLevelId,
      serviceItemId: form.serviceItemId,
      cycleType: form.cycleType,
      frequency: form.frequency,
      startDate: form.startDate,
      endDate: form.endDate,
      defaultStaffId: form.defaultStaffId,
      status: form.status,
      remark: form.remark,
      changeReason: form.changeReason?.trim() || undefined
    }

    if (form.id) {
      await updateServicePlan(form.id, payload)
    } else {
      await createServicePlan(payload)
    }
    message.success('已保存，等待护士长审核发布')
    modalOpen.value = false
    await load()
  } catch (error) {
    message.error(resolveCareError(error, '保存失败'))
  } finally {
    submitting.value = false
  }
}

async function remove(id: number) {
  Modal.confirm({
    title: '确认删除该服务计划？',
    onOk: async () => {
      try {
        await deleteServicePlan(id)
        message.success('删除成功')
        await load()
      } catch (error) {
        message.error(resolveCareError(error, '删除失败'))
      }
    }
  })
}

function onTableChange(pag: { current?: number; pageSize?: number }) {
  query.pageNo = pag.current || 1
  query.pageSize = pag.pageSize || 10
  load()
}

function search() {
  query.pageNo = 1
  load()
}

function reset() {
  query.keyword = undefined
  query.status = undefined
  query.pageNo = 1
  load()
}

async function searchElder(keyword: string) {
  await searchElders(keyword)
}

async function loadBaseOptions() {
  try {
    const [careLevels, serviceItems] = await Promise.all([
      getCareLevelList({ enabled: 1 }) as Promise<CareLevelItem[]>,
      listServiceItems({ enabled: 1 }) as Promise<ServiceItem[]>
    ])
    careLevelOptions.value = careLevels.map((item) => ({ label: `${item.levelCode} - ${item.levelName}`, value: item.id }))
    serviceItemOptions.value = serviceItems.map((item) => ({ label: item.name, value: item.id }))
    await Promise.all([searchElders(''), searchStaff('')])
  } catch (error) {
    message.error(resolveCareError(error, '加载基础选项失败'))
    careLevelOptions.value = []
    serviceItemOptions.value = []
  }
}

async function load() {
  loading.value = true
  try {
    const res: PageResult<ServicePlanItem> = await getServicePlanPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      status: query.status,
      keyword: query.keyword,
      elderId: normalizeResidentId(route.query as Record<string, unknown>)
    })
    rows.value = res.list
    query.total = res.total
  } catch (error) {
    message.error(resolveCareError(error, '加载服务计划失败'))
    rows.value = []
    query.total = 0
  } finally {
    loading.value = false
  }
}

load()
loadBaseOptions()
</script>


<style scoped>
.plan-rev { margin-left: 4px; color: #8a968f; font-size: 12px; }
.plan-reject { margin-top: 2px; color: #c9443d; font-size: 12px; }
</style>
