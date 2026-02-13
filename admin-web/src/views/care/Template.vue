<template>
  <PageContainer title="护理模板" subTitle="任务模板维护">
    <a-card :bordered="false" class="card-elevated">
      <div class="table-actions">
        <a-space>
          <a-button type="primary" @click="openModal()">新增模板</a-button>
          <a-button @click="exportCsvData">导出CSV</a-button>
        </a-space>
      </div>
      <a-table
        row-key="id"
        :columns="columns"
        :data-source="list"
        :loading="loading"
        :pagination="pagination"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'enabled'">
            <a-tag :color="record.enabled ? 'green' : 'default'">
              {{ record.enabled ? '启用' : '停用' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'actions'">
            <a-space>
              <a @click="openModal(record)">编辑</a>
              <a @click="toggle(record)">{{ record.enabled ? '停用' : '启用' }}</a>
              <a class="danger-text" @click="remove(record.id)">删除</a>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal v-model:open="modalOpen" title="护理模板" :confirm-loading="submitting" @ok="submit">
      <a-form ref="formRef" layout="vertical" :model="form" :rules="rules">
        <a-form-item name="taskName" label="任务名称" required>
          <a-input v-model:value="form.taskName" />
        </a-form-item>
        <a-form-item name="frequencyPerDay" label="每日频次" required>
          <a-input-number v-model:value="form.frequencyPerDay" :min="1" style="width: 100%" />
        </a-form-item>
        <a-form-item name="careLevelRequired" label="护理等级">
          <a-input v-model:value="form.careLevelRequired" placeholder="如 A/B/C" />
        </a-form-item>
        <a-form-item name="chargeAmount" label="任务收费(元)">
          <a-input-number v-model:value="form.chargeAmount" :min="0" style="width: 100%" />
        </a-form-item>
        <a-form-item name="enabled" label="启用状态">
          <a-switch v-model:checked="form.enabled" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import { getTemplatePage, createTemplate, updateTemplate, deleteTemplate } from '../../api/care'
import type { CareTaskTemplate, PageResult } from '../../types'
import { exportCsv } from '../../utils/export'

const list = ref<CareTaskTemplate[]>([])
const loading = ref(false)
const modalOpen = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive<Partial<CareTaskTemplate>>({ enabled: true })
const page = reactive({ pageNo: 1, pageSize: 10, total: 0 })

const rules = {
  taskName: [{ required: true, message: '请输入任务名称' }],
  frequencyPerDay: [{ required: true, type: 'number', min: 1, message: '频次至少 1' }]
}

const columns = [
  { title: '任务名称', dataIndex: 'taskName', key: 'taskName' },
  { title: '频次/天', dataIndex: 'frequencyPerDay', key: 'frequencyPerDay', width: 120 },
  { title: '护理等级', dataIndex: 'careLevelRequired', key: 'careLevelRequired', width: 140 },
  { title: '收费(元)', dataIndex: 'chargeAmount', key: 'chargeAmount', width: 120 },
  { title: '状态', key: 'enabled', width: 100 },
  { title: '操作', key: 'actions', width: 180 }
]

const pagination = {
  current: page.pageNo,
  pageSize: page.pageSize,
  total: page.total,
  showSizeChanger: true,
  onChange: (current: number, size: number) => {
    page.pageNo = current
    page.pageSize = size
    load()
  }
}

function openModal(record?: CareTaskTemplate) {
  Object.assign(form, record || { taskName: '', frequencyPerDay: 1, careLevelRequired: '', enabled: true })
  modalOpen.value = true
}

async function submit() {
  try {
    await formRef.value?.validate()
    submitting.value = true
    if (form.id) {
      await updateTemplate(form.id, form)
    } else {
      await createTemplate(form)
    }
    message.success('保存成功')
    modalOpen.value = false
    load()
  } catch {
    message.error('保存失败')
  } finally {
    submitting.value = false
  }
}

async function remove(id: number) {
  Modal.confirm({
    title: '确认删除该模板？',
    onOk: async () => {
      await deleteTemplate(id)
      message.success('已删除')
      load()
    }
  })
}

async function toggle(record: CareTaskTemplate) {
  try {
    await updateTemplate(record.id, { enabled: !record.enabled })
    message.success('状态已更新')
    load()
  } catch {
    message.error('更新失败')
  }
}

async function load() {
  loading.value = true
  try {
    const res: PageResult<CareTaskTemplate> = await getTemplatePage({
      pageNo: page.pageNo,
      pageSize: page.pageSize
    })
    list.value = res.list
    page.total = res.total
  } finally {
    loading.value = false
  }
}

function exportCsvData() {
  exportCsv(
    list.value.map((t) => ({
      任务名称: t.taskName,
      频次: t.frequencyPerDay,
      护理等级: t.careLevelRequired || '',
      收费: t.chargeAmount ?? 0,
      状态: t.enabled ? '启用' : '停用'
    })),
    '护理模板'
  )
}

load()
</script>

<style scoped>
.table-actions {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}
.danger-text {
  color: #ef4444;
}
</style>
