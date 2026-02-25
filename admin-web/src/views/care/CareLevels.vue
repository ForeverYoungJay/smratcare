<template>
  <PageContainer title="护理等级" subTitle="护理等级标准与费用参考维护">
    <a-card :bordered="false" class="card-elevated">
      <a-form layout="inline" @submit.prevent>
        <a-form-item label="关键字">
          <a-input v-model:value="query.keyword" placeholder="编码/名称" allow-clear />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="query.enabled" :options="enabledOptions" allow-clear style="width: 140px" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="search">查询</a-button>
            <a-button @click="reset">重置</a-button>
            <a-button type="primary" @click="openModal()">新增等级</a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <a-table row-key="id" :columns="columns" :data-source="rows" :loading="loading" :pagination="pagination" @change="onTableChange">
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

    <a-modal v-model:open="modalOpen" title="护理等级" :confirm-loading="submitting" @ok="submit">
      <a-form ref="formRef" layout="vertical" :model="form" :rules="rules">
        <a-form-item label="等级编码" name="levelCode" required>
          <a-input v-model:value="form.levelCode" />
        </a-form-item>
        <a-form-item label="等级名称" name="levelName" required>
          <a-input v-model:value="form.levelName" />
        </a-form-item>
        <a-form-item label="等级序号" name="severity">
          <a-input-number v-model:value="form.severity" :min="1" style="width: 100%" />
        </a-form-item>
        <a-form-item label="月费参考" name="monthlyFee">
          <a-input-number v-model:value="form.monthlyFee" :min="0" :precision="2" style="width: 100%" />
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
import { createCareLevel, deleteCareLevel, getCareLevelPage, updateCareLevel } from '../../api/nursing'
import type { CareLevelItem, PageResult } from '../../types'

const rows = ref<CareLevelItem[]>([])
const loading = ref(false)
const modalOpen = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const query = reactive({ pageNo: 1, pageSize: 10, total: 0, keyword: undefined as string | undefined, enabled: undefined as number | undefined })
const form = reactive<Partial<CareLevelItem>>({ levelCode: '', levelName: '', severity: 1, monthlyFee: undefined, enabled: 1, remark: '' })

const enabledOptions = [{ label: '启用', value: 1 }, { label: '停用', value: 0 }]
const rules = {
  levelCode: [{ required: true, message: '请输入等级编码', trigger: 'blur' }],
  levelName: [{ required: true, message: '请输入等级名称', trigger: 'blur' }]
}

const columns = [
  { title: '等级编码', dataIndex: 'levelCode', key: 'levelCode', width: 140 },
  { title: '等级名称', dataIndex: 'levelName', key: 'levelName', width: 140 },
  { title: '等级序号', dataIndex: 'severity', key: 'severity', width: 100 },
  { title: '月费参考', dataIndex: 'monthlyFee', key: 'monthlyFee', width: 120 },
  { title: '状态', key: 'enabled', width: 100 },
  { title: '备注', dataIndex: 'remark', key: 'remark' },
  { title: '操作', key: 'actions', width: 140 }
]

const pagination = computed(() => ({ current: query.pageNo, pageSize: query.pageSize, total: query.total, showSizeChanger: true }))

function resetForm() {
  form.id = undefined
  form.levelCode = ''
  form.levelName = ''
  form.severity = 1
  form.monthlyFee = undefined
  form.enabled = 1
  form.remark = ''
}

function openModal(record?: CareLevelItem) {
  resetForm()
  if (record) {
    Object.assign(form, record)
  }
  modalOpen.value = true
}

async function submit() {
  await formRef.value?.validate()
  submitting.value = true
  try {
    const payload: Partial<CareLevelItem> = {
      levelCode: form.levelCode,
      levelName: form.levelName,
      severity: form.severity,
      monthlyFee: form.monthlyFee,
      enabled: form.enabled,
      remark: form.remark
    }
    if (form.id) {
      await updateCareLevel(form.id, payload)
    } else {
      await createCareLevel(payload)
    }
    message.success('保存成功')
    modalOpen.value = false
    await load()
  } finally {
    submitting.value = false
  }
}

function onTableChange(pag: { current?: number; pageSize?: number }) {
  query.pageNo = pag.current || 1
  query.pageSize = pag.pageSize || 10
  load()
}

async function remove(id: number) {
  Modal.confirm({
    title: '确认删除该护理等级？',
    onOk: async () => {
      await deleteCareLevel(id)
      message.success('删除成功')
      await load()
    }
  })
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
    const res: PageResult<CareLevelItem> = await getCareLevelPage({
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
.danger-text { color: #ef4444; }
</style>
