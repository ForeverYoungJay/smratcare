<template>
  <PageContainer title="老人套餐" subTitle="为老人配置护理套餐">
    <a-card :bordered="false" class="card-elevated">
      <div class="table-actions">
        <a-space>
          <a-select
            v-model:value="query.elderId"
            allow-clear
            placeholder="选择老人"
            style="width: 220px"
            @change="load"
          >
            <a-select-option v-for="elder in elderOptions" :key="elder.id" :value="elder.id">
              {{ elder.fullName }}
            </a-select-option>
          </a-select>
          <a-button type="primary" @click="openModal()">新增老人套餐</a-button>
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
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'green' : 'default'">
              {{ record.status === 1 ? '启用' : '停用' }}
            </a-tag>
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

    <a-modal v-model:open="modalOpen" title="老人套餐" :confirm-loading="submitting" @ok="submit">
      <a-form ref="formRef" layout="vertical" :model="form" :rules="rules">
        <a-form-item name="elderId" label="老人" required>
          <a-select v-model:value="form.elderId" placeholder="请选择老人">
            <a-select-option v-for="elder in elderOptions" :key="elder.id" :value="elder.id">
              {{ elder.fullName }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item name="packageId" label="套餐" required>
          <a-select v-model:value="form.packageId" placeholder="请选择套餐">
            <a-select-option v-for="pkg in packageOptions" :key="pkg.id" :value="pkg.id">
              {{ pkg.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item name="startDate" label="开始日期" required>
          <a-date-picker v-model:value="form.startDate" value-format="YYYY-MM-DD" style="width: 100%" />
        </a-form-item>
        <a-form-item name="endDate" label="结束日期">
          <a-date-picker v-model:value="form.endDate" value-format="YYYY-MM-DD" style="width: 100%" />
        </a-form-item>
        <a-form-item name="status" label="状态">
          <a-switch v-model:checked="form.status" :checked-value="1" :un-checked-value="0" />
        </a-form-item>
        <a-form-item name="remark" label="备注">
          <a-input v-model:value="form.remark" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import { getElderPage } from '../../api/elder'
import {
  getElderPackagePage,
  createElderPackage,
  updateElderPackage,
  deleteElderPackage,
  listCarePackages
} from '../../api/standard'
import type { ElderPackage, CarePackage, PageResult } from '../../types'

interface ElderOption {
  id: number
  fullName: string
}

const list = ref<ElderPackage[]>([])
const loading = ref(false)
const modalOpen = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive<Partial<ElderPackage>>({ status: 1 })
const page = reactive({ pageNo: 1, pageSize: 10, total: 0 })
const query = reactive({ elderId: undefined as number | undefined })

const elderOptions = ref<ElderOption[]>([])
const packageOptions = ref<CarePackage[]>([])

const rules = {
  elderId: [{ required: true, message: '请选择老人' }],
  packageId: [{ required: true, message: '请选择套餐' }],
  startDate: [{ required: true, message: '请选择开始日期' }]
}

const columns = [
  { title: '老人', dataIndex: 'elderName', key: 'elderName' },
  { title: '套餐', dataIndex: 'packageName', key: 'packageName' },
  { title: '开始日期', dataIndex: 'startDate', key: 'startDate', width: 140 },
  { title: '结束日期', dataIndex: 'endDate', key: 'endDate', width: 140 },
  { title: '状态', key: 'status', width: 100 },
  { title: '备注', dataIndex: 'remark', key: 'remark' },
  { title: '操作', key: 'actions', width: 160 }
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

function openModal(record?: ElderPackage) {
  Object.assign(form, record || { elderId: query.elderId, packageId: undefined, startDate: '', endDate: '', status: 1, remark: '' })
  modalOpen.value = true
}

async function submit() {
  try {
    await formRef.value?.validate()
    submitting.value = true
    if (form.id) {
      await updateElderPackage(form.id, form)
    } else {
      await createElderPackage(form)
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
    title: '确认删除该老人套餐？',
    onOk: async () => {
      await deleteElderPackage(id)
      message.success('已删除')
      load()
    }
  })
}

async function loadOptions() {
  const elders = await getElderPage({ pageNo: 1, pageSize: 200 })
  elderOptions.value = (elders.list || []).map((item: any) => ({ id: item.id, fullName: item.fullName }))
  packageOptions.value = await listCarePackages()
}

async function load() {
  loading.value = true
  try {
    const res: PageResult<ElderPackage> = await getElderPackagePage({
      pageNo: page.pageNo,
      pageSize: page.pageSize,
      elderId: query.elderId
    })
    list.value = res.list
    page.total = res.total
  } finally {
    loading.value = false
  }
}

loadOptions()
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
