<template>
  <PageContainer title="排班方案" subTitle="标准班次、周期执行人与打卡联动配置">
    <a-card :bordered="false" class="card-elevated">
      <a-form layout="inline" @submit.prevent>
        <a-form-item label="关键字">
          <a-input v-model:value="query.keyword" placeholder="方案名称/班次编码" allow-clear />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="query.enabled" :options="enabledOptions" allow-clear style="width: 140px" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="search">查询</a-button>
            <a-button @click="reset">重置</a-button>
            <a-button type="primary" @click="openModal()">新增方案</a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <a-table
        row-key="id"
        :columns="columns"
        :data-source="rows"
        :loading="loading"
        :pagination="pagination"
        @change="onTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'enabled'">
            <a-tag :color="record.enabled === 1 ? 'green' : 'default'">{{ record.enabled === 1 ? '启用' : '停用' }}</a-tag>
          </template>
          <template v-else-if="column.key === 'recurrenceType'">
            {{ recurrenceLabel(record.recurrenceType) }}
          </template>
          <template v-else-if="column.key === 'attendanceLinked'">
            <a-tag :color="record.attendanceLinked === 1 ? 'blue' : 'default'">{{ record.attendanceLinked === 1 ? '联动打卡' : '不联动' }}</a-tag>
          </template>
          <template v-else-if="column.key === 'actions'">
            <a-space>
              <a @click="openApply(record)">实施</a>
              <a @click="openModal(record)">编辑</a>
              <a class="danger-text" @click="remove(record.id)">删除</a>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal v-model:open="modalOpen" title="排班方案" :confirm-loading="submitting" @ok="submit">
      <a-form ref="formRef" layout="vertical" :model="form" :rules="rules">
        <a-form-item label="方案名称" name="name" required>
          <a-input v-model:value="form.name" />
        </a-form-item>
        <a-form-item label="班次编码" name="shiftCode" required>
          <a-input v-model:value="form.shiftCode" />
        </a-form-item>
        <a-form-item label="开始时间" name="startTime" required>
          <a-time-picker v-model:value="form.startTime" value-format="HH:mm:ss" style="width: 100%" />
        </a-form-item>
        <a-form-item label="结束时间" name="endTime" required>
          <a-time-picker v-model:value="form.endTime" value-format="HH:mm:ss" style="width: 100%" />
        </a-form-item>
        <a-form-item label="跨天班次" name="crossDay">
          <a-switch v-model:checked="form.crossDay" :checked-value="1" :un-checked-value="0" />
        </a-form-item>
        <a-form-item label="建议人数" name="requiredStaffCount">
          <a-input-number v-model:value="form.requiredStaffCount" :min="1" style="width: 100%" />
        </a-form-item>
        <a-form-item label="执行周期" name="recurrenceType">
          <a-select v-model:value="form.recurrenceType">
            <a-select-option value="WEEKLY_ONCE">一周一次</a-select-option>
            <a-select-option value="WEEKLY_TWICE">一周两次</a-select-option>
            <a-select-option value="DAILY_ONCE">每天一次</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="执行人" name="executeStaffId">
          <a-select
            v-model:value="form.executeStaffId"
            show-search
            :filter-option="false"
            placeholder="输入姓名搜索"
            :options="staffOptions"
            @search="searchStaff"
          />
        </a-form-item>
        <a-form-item label="联动上班打卡" name="attendanceLinked">
          <a-switch v-model:checked="form.attendanceLinked" :checked-value="1" :un-checked-value="0" />
        </a-form-item>
        <a-form-item label="启用状态" name="enabled">
          <a-switch v-model:checked="form.enabled" :checked-value="1" :un-checked-value="0" />
        </a-form-item>
        <a-form-item label="备注" name="remark">
          <a-textarea v-model:value="form.remark" :rows="3" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="applyOpen" title="实施排班方案" :confirm-loading="applying" @ok="submitApply">
      <a-form layout="vertical">
        <a-form-item label="方案名称">
          <a-input :value="applyTarget?.name" disabled />
        </a-form-item>
        <a-form-item label="执行周期">
          <a-input :value="recurrenceLabel(applyTarget?.recurrenceType)" disabled />
        </a-form-item>
        <a-form-item label="执行人">
          <a-input :value="applyTarget?.executeStaffName || '-'" disabled />
        </a-form-item>
        <a-form-item label="开始日期" required>
          <a-date-picker v-model:value="applyForm.startDate" value-format="YYYY-MM-DD" style="width: 100%" />
        </a-form-item>
        <a-form-item label="结束日期" required>
          <a-date-picker v-model:value="applyForm.endDate" value-format="YYYY-MM-DD" style="width: 100%" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import { getStaffPage } from '../../api/rbac'
import { applyShiftTemplate, createShiftTemplate, deleteShiftTemplate, getShiftTemplatePage, updateShiftTemplate } from '../../api/nursing'
import type { PageResult, ShiftTemplateItem, StaffItem } from '../../types'

const rows = ref<ShiftTemplateItem[]>([])
const loading = ref(false)
const modalOpen = ref(false)
const submitting = ref(false)
const applying = ref(false)
const applyOpen = ref(false)
const applyTarget = ref<ShiftTemplateItem>()
const formRef = ref<FormInstance>()
const staffOptions = ref<Array<{ label: string; value: number }>>([])

const query = reactive({
  pageNo: 1,
  pageSize: 10,
  total: 0,
  keyword: undefined as string | undefined,
  enabled: undefined as number | undefined
})

const form = reactive<Partial<ShiftTemplateItem>>({
  name: '',
  shiftCode: '',
  startTime: '08:00:00',
  endTime: '16:00:00',
  crossDay: 0,
  requiredStaffCount: 1,
  recurrenceType: 'WEEKLY_ONCE',
  executeStaffId: undefined,
  attendanceLinked: 1,
  enabled: 1,
  remark: ''
})
const applyForm = reactive({
  startDate: '',
  endDate: ''
})

const enabledOptions = [
  { label: '启用', value: 1 },
  { label: '停用', value: 0 }
]

const rules = {
  name: [{ required: true, message: '请输入方案名称', trigger: 'blur' }],
  shiftCode: [{ required: true, message: '请输入班次编码', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  executeStaffId: [{ required: true, message: '请选择执行人', trigger: 'change' }]
}

const columns = [
  { title: '方案名称', dataIndex: 'name', key: 'name' },
  { title: '班次编码', dataIndex: 'shiftCode', key: 'shiftCode', width: 120 },
  { title: '开始时间', dataIndex: 'startTime', key: 'startTime', width: 120 },
  { title: '结束时间', dataIndex: 'endTime', key: 'endTime', width: 120 },
  { title: '跨天', key: 'crossDay', width: 80, customRender: ({ record }: { record: ShiftTemplateItem }) => (record.crossDay === 1 ? '是' : '否') },
  { title: '建议人数', dataIndex: 'requiredStaffCount', key: 'requiredStaffCount', width: 100 },
  { title: '周期', key: 'recurrenceType', width: 120 },
  { title: '执行人', dataIndex: 'executeStaffName', key: 'executeStaffName', width: 140 },
  { title: '打卡联动', key: 'attendanceLinked', width: 120 },
  { title: '状态', key: 'enabled', width: 100 },
  { title: '操作', key: 'actions', width: 180 }
]

const pagination = computed(() => ({
  current: query.pageNo,
  pageSize: query.pageSize,
  total: query.total,
  showSizeChanger: true
}))

function resetForm() {
  form.id = undefined
  form.name = ''
  form.shiftCode = ''
  form.startTime = '08:00:00'
  form.endTime = '16:00:00'
  form.crossDay = 0
  form.requiredStaffCount = 1
  form.recurrenceType = 'WEEKLY_ONCE'
  form.executeStaffId = undefined
  form.attendanceLinked = 1
  form.enabled = 1
  form.remark = ''
}

function openModal(record?: ShiftTemplateItem) {
  resetForm()
  if (record) {
    Object.assign(form, record)
  }
  modalOpen.value = true
}

async function submit() {
  try {
    await formRef.value?.validate()
    submitting.value = true
    const payload: Partial<ShiftTemplateItem> = {
      name: form.name,
      shiftCode: form.shiftCode,
      startTime: form.startTime,
      endTime: form.endTime,
      crossDay: form.crossDay,
      requiredStaffCount: form.requiredStaffCount,
      recurrenceType: form.recurrenceType,
      executeStaffId: form.executeStaffId,
      attendanceLinked: form.attendanceLinked,
      enabled: form.enabled,
      remark: form.remark
    }
    if (form.id) {
      await updateShiftTemplate(form.id, payload)
    } else {
      await createShiftTemplate(payload)
    }
    message.success('保存成功')
    modalOpen.value = false
    await load()
  } finally {
    submitting.value = false
  }
}

async function remove(id: number) {
  Modal.confirm({
    title: '确认删除该排班方案？',
    onOk: async () => {
      await deleteShiftTemplate(id)
      message.success('删除成功')
      await load()
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
  query.enabled = undefined
  query.pageNo = 1
  load()
}

function recurrenceLabel(type?: string) {
  if (type === 'DAILY_ONCE') return '每天一次'
  if (type === 'WEEKLY_TWICE') return '一周两次'
  return '一周一次'
}

function openApply(record: ShiftTemplateItem) {
  if (!record.executeStaffId) {
    message.warning('请先为方案设置执行人')
    return
  }
  applyTarget.value = record
  const today = new Date().toISOString().slice(0, 10)
  applyForm.startDate = today
  applyForm.endDate = today
  applyOpen.value = true
}

async function submitApply() {
  if (!applyTarget.value?.id || !applyForm.startDate || !applyForm.endDate) {
    message.warning('请选择完整实施日期')
    return
  }
  applying.value = true
  try {
    const count = await applyShiftTemplate(applyTarget.value.id, {
      startDate: applyForm.startDate,
      endDate: applyForm.endDate
    })
    message.success(`实施完成，已生成 ${count || 0} 条排班`)
    applyOpen.value = false
  } finally {
    applying.value = false
  }
}

async function loadStaffOptions(keyword?: string) {
  const res: PageResult<StaffItem> = await getStaffPage({ pageNo: 1, pageSize: 50, keyword })
  staffOptions.value = (res.list || []).map((item) => ({
    label: item.realName || item.username || `员工${item.id}`,
    value: item.id
  }))
}

async function searchStaff(keyword: string) {
  await loadStaffOptions(keyword)
}

async function load() {
  loading.value = true
  try {
    const res: PageResult<ShiftTemplateItem> = await getShiftTemplatePage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      keyword: query.keyword,
      enabled: query.enabled
    })
    rows.value = res.list
    query.total = res.total
  } finally {
    loading.value = false
  }
}

load()
loadStaffOptions()
</script>

<style scoped>
.danger-text {
  color: #ef4444;
}
</style>
