<template>
  <PageContainer title="排班方案" subTitle="标准班次与时段配置">
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
          <template v-else-if="column.key === 'actions'">
            <a-space>
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
        <a-form-item label="启用状态" name="enabled">
          <a-switch v-model:checked="form.enabled" :checked-value="1" :un-checked-value="0" />
        </a-form-item>
        <a-form-item label="备注" name="remark">
          <a-textarea v-model:value="form.remark" :rows="3" />
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
import { createShiftTemplate, deleteShiftTemplate, getShiftTemplatePage, updateShiftTemplate } from '../../api/nursing'
import type { PageResult, ShiftTemplateItem } from '../../types'

const rows = ref<ShiftTemplateItem[]>([])
const loading = ref(false)
const modalOpen = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()

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
  enabled: 1,
  remark: ''
})

const enabledOptions = [
  { label: '启用', value: 1 },
  { label: '停用', value: 0 }
]

const rules = {
  name: [{ required: true, message: '请输入方案名称', trigger: 'blur' }],
  shiftCode: [{ required: true, message: '请输入班次编码', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
}

const columns = [
  { title: '方案名称', dataIndex: 'name', key: 'name' },
  { title: '班次编码', dataIndex: 'shiftCode', key: 'shiftCode', width: 120 },
  { title: '开始时间', dataIndex: 'startTime', key: 'startTime', width: 120 },
  { title: '结束时间', dataIndex: 'endTime', key: 'endTime', width: 120 },
  { title: '跨天', key: 'crossDay', width: 80, customRender: ({ record }: { record: ShiftTemplateItem }) => (record.crossDay === 1 ? '是' : '否') },
  { title: '建议人数', dataIndex: 'requiredStaffCount', key: 'requiredStaffCount', width: 100 },
  { title: '状态', key: 'enabled', width: 100 },
  { title: '操作', key: 'actions', width: 140 }
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
</script>

<style scoped>
.danger-text {
  color: #ef4444;
}
</style>
