<template>
  <PageContainer title="供应商管理" subTitle="维护供应商档案与联系人信息">
    <a-card class="card-elevated" :bordered="false">
      <a-form layout="inline" :model="query" class="search-form">
        <a-form-item label="关键词">
          <a-input v-model:value="query.keyword" placeholder="供应商编码/名称/联系人" allow-clear />
        </a-form-item>
        <a-form-item label="仅启用">
          <a-switch v-model:checked="query.enabledOnly" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="fetchData">查询</a-button>
            <a-button @click="reset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px">
      <div class="table-actions">
        <a-button type="primary" @click="openCreate">新增供应商</a-button>
      </div>
      <a-table
        row-key="id"
        :loading="loading"
        :data-source="rows"
        :pagination="false"
        :columns="columns"
        size="middle"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="materialEnableStatusColor(record.status)">{{ materialEnableStatusLabel(record.status) }}</a-tag>
          </template>
          <template v-if="column.key === 'actions'">
            <a-space>
              <a @click="openEdit(record)">编辑</a>
              <a-popconfirm title="确认删除该供应商？" @confirm="handleDelete(record.id)">
                <a>删除</a>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
      <a-pagination
        style="margin-top: 16px; text-align: right"
        :current="query.pageNo"
        :page-size="query.pageSize"
        :total="total"
        show-size-changer
        @change="onPageChange"
        @showSizeChange="onPageSizeChange"
      />
    </a-card>

    <a-modal
      v-model:open="editorOpen"
      :title="editingId ? '编辑供应商' : '新增供应商'"
      :confirm-loading="saving"
      @ok="submit"
    >
      <a-form ref="formRef" layout="vertical" :model="form" :rules="rules">
        <a-form-item label="供应商编码" name="supplierCode">
          <a-input v-model:value="form.supplierCode" />
        </a-form-item>
        <a-form-item label="供应商名称" name="supplierName">
          <a-input v-model:value="form.supplierName" />
        </a-form-item>
        <a-form-item label="联系人">
          <a-input v-model:value="form.contactName" />
        </a-form-item>
        <a-form-item label="联系电话">
          <a-input v-model:value="form.contactPhone" />
        </a-form-item>
        <a-form-item label="地址">
          <a-input v-model:value="form.address" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="form.status" :options="statusOptions" />
        </a-form-item>
        <a-form-item label="备注">
          <a-input v-model:value="form.remark" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import { createSupplier, deleteSupplier, getSupplierPage, updateSupplier } from '../../api/material'
import {
  MATERIAL_ENABLE_STATUS_OPTIONS,
  materialEnableStatusColor,
  materialEnableStatusLabel
} from '../../utils/materialStatus'
import type { MaterialSupplierItem, PageResult } from '../../types'

const loading = ref(false)
const saving = ref(false)
const rows = ref<MaterialSupplierItem[]>([])
const total = ref(0)

const query = reactive({
  keyword: '',
  enabledOnly: false,
  pageNo: 1,
  pageSize: 10
})

const columns = [
  { title: '供应商编码', dataIndex: 'supplierCode', key: 'supplierCode', width: 150 },
  { title: '供应商名称', dataIndex: 'supplierName', key: 'supplierName', width: 180 },
  { title: '联系人', dataIndex: 'contactName', key: 'contactName', width: 120 },
  { title: '联系电话', dataIndex: 'contactPhone', key: 'contactPhone', width: 140 },
  { title: '地址', dataIndex: 'address', key: 'address' },
  { title: '状态', dataIndex: 'status', key: 'status', width: 90 },
  { title: '操作', key: 'actions', width: 140, fixed: 'right' }
]

const statusOptions = MATERIAL_ENABLE_STATUS_OPTIONS

const editorOpen = ref(false)
const formRef = ref()
const editingId = ref<number>()
const form = reactive<Partial<MaterialSupplierItem>>({
  supplierCode: '',
  supplierName: '',
  contactName: '',
  contactPhone: '',
  address: '',
  status: 1,
  remark: ''
})

const rules = {
  supplierCode: [{ required: true, message: '请输入供应商编码' }],
  supplierName: [{ required: true, message: '请输入供应商名称' }]
}

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<MaterialSupplierItem> = await getSupplierPage({
      keyword: query.keyword,
      enabledOnly: query.enabledOnly,
      pageNo: query.pageNo,
      pageSize: query.pageSize
    })
    rows.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function reset() {
  query.keyword = ''
  query.enabledOnly = false
  query.pageNo = 1
  fetchData()
}

function onPageChange(page: number) {
  query.pageNo = page
  fetchData()
}

function onPageSizeChange(_page: number, size: number) {
  query.pageNo = 1
  query.pageSize = size
  fetchData()
}

function openCreate() {
  editingId.value = undefined
  Object.assign(form, {
    supplierCode: '',
    supplierName: '',
    contactName: '',
    contactPhone: '',
    address: '',
    status: 1,
    remark: ''
  })
  editorOpen.value = true
}

function openEdit(row: MaterialSupplierItem) {
  editingId.value = row.id
  Object.assign(form, { ...row })
  editorOpen.value = true
}

async function submit() {
  await formRef.value?.validate()
  saving.value = true
  try {
    if (editingId.value) {
      await updateSupplier(editingId.value, form)
      message.success('供应商已更新')
    } else {
      await createSupplier(form)
      message.success('供应商已创建')
    }
    editorOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function handleDelete(id: number) {
  await deleteSupplier(id)
  message.success('已删除')
  fetchData()
}

onMounted(fetchData)
</script>

<style scoped>
.search-form {
  row-gap: 8px;
}
.table-actions {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 12px;
}
</style>
