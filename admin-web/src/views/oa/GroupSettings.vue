<template>
  <PageContainer title="分组设置" subTitle="跨业务团队与工作组配置">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键词">
        <a-input v-model:value="query.keyword" placeholder="分组名/组长" allow-clear />
      </a-form-item>
      <a-form-item label="分组类型">
        <a-input v-model:value="query.groupType" placeholder="如：行政组" allow-clear />
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="query.status" allow-clear style="width: 140px" :options="statusOptions" />
      </a-form-item>
      <template #extra>
        <a-button type="primary" @click="openEdit()">新增分组</a-button>
        <a-button :disabled="selectedRowKeys.length === 0" @click="batchEnable">批量启用</a-button>
        <a-button :disabled="selectedRowKeys.length === 0" @click="batchDisable">批量停用</a-button>
        <a-button :disabled="selectedRowKeys.length === 0" danger @click="batchRemove">批量删除</a-button>
        <a-button @click="downloadExport">导出CSV</a-button>
      </template>
    </SearchForm>

    <DataTable
      rowKey="id"
      :row-selection="rowSelection"
      :columns="columns"
      :data-source="rows"
      :loading="loading"
      :pagination="pagination"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="record.status === 'ENABLED' ? 'green' : 'default'">
            {{ record.status === 'ENABLED' ? '启用' : '停用' }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a @click="openEdit(record)">编辑</a>
            <a :style="{ color: record.status === 'ENABLED' ? '#999' : '' }" @click="enable(record)">启用</a>
            <a :style="{ color: record.status === 'DISABLED' ? '#999' : '' }" @click="disable(record)">停用</a>
            <a @click="remove(record)" style="color: #ff4d4f">删除</a>
          </a-space>
        </template>
      </template>
    </DataTable>

    <a-modal v-model:open="editOpen" :title="form.id ? '编辑分组' : '新增分组'" @ok="submit" :confirm-loading="saving" width="620px">
      <a-form layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="分组名称" required>
              <a-input v-model:value="form.groupName" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="分组类型">
              <a-input v-model:value="form.groupType" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="组长姓名">
              <a-input v-model:value="form.leaderName" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="成员数">
              <a-input-number v-model:value="form.memberCount" :min="0" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="状态">
          <a-select v-model:value="form.status" :options="statusOptions" />
        </a-form-item>
        <a-form-item label="备注">
          <a-textarea v-model:value="form.remark" :rows="3" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import {
  batchDeleteGroupSetting,
  batchDisableGroupSetting,
  batchEnableGroupSetting,
  createGroupSetting,
  deleteGroupSetting,
  disableGroupSetting,
  enableGroupSetting,
  exportGroupSetting,
  getGroupSettingPage,
  updateGroupSetting
} from '../../api/oa'
import type { OaGroupSetting, PageResult } from '../../types'

const loading = ref(false)
const saving = ref(false)
const rows = ref<OaGroupSetting[]>([])
const query = reactive({ keyword: '', groupType: '', status: undefined as string | undefined, pageNo: 1, pageSize: 10 })
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const selectedRowKeys = ref<number[]>([])

const columns = [
  { title: '分组名称', dataIndex: 'groupName', key: 'groupName', width: 180 },
  { title: '分组类型', dataIndex: 'groupType', key: 'groupType', width: 140 },
  { title: '组长', dataIndex: 'leaderName', key: 'leaderName', width: 120 },
  { title: '成员数', dataIndex: 'memberCount', key: 'memberCount', width: 100 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '备注', dataIndex: 'remark', key: 'remark' },
  { title: '操作', key: 'action', width: 140 }
]

const statusOptions = [
  { label: '启用', value: 'ENABLED' },
  { label: '停用', value: 'DISABLED' }
]

const editOpen = ref(false)
const form = reactive<Partial<OaGroupSetting>>({ status: 'ENABLED', memberCount: 0 })
const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: (string | number)[]) => {
    selectedRowKeys.value = keys.map((item) => Number(item))
  }
}))

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<OaGroupSetting> = await getGroupSettingPage(query)
    rows.value = res.list
    pagination.total = res.total || res.list.length
    selectedRowKeys.value = []
  } finally {
    loading.value = false
  }
}

function handleTableChange(pag: any) {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  query.pageNo = pag.current
  query.pageSize = pag.pageSize
  fetchData()
}

function onReset() {
  query.keyword = ''
  query.groupType = ''
  query.status = undefined
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

function openEdit(record?: OaGroupSetting) {
  Object.assign(form, record || { groupName: '', groupType: '', leaderName: '', status: 'ENABLED', memberCount: 0, remark: '' })
  editOpen.value = true
}

async function submit() {
  if (!form.groupName) return
  saving.value = true
  try {
    if (form.id) {
      await updateGroupSetting(form.id, form)
    } else {
      await createGroupSetting(form)
    }
    editOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function remove(record: OaGroupSetting) {
  await deleteGroupSetting(record.id)
  fetchData()
}

async function enable(record: OaGroupSetting) {
  if (record.status === 'ENABLED') return
  await enableGroupSetting(record.id)
  fetchData()
}

async function disable(record: OaGroupSetting) {
  if (record.status === 'DISABLED') return
  await disableGroupSetting(record.id)
  fetchData()
}

async function batchEnable() {
  if (selectedRowKeys.value.length === 0) return
  const affected = await batchEnableGroupSetting(selectedRowKeys.value)
  message.success(`批量启用，共处理 ${affected || 0} 条`)
  fetchData()
}

async function batchDisable() {
  if (selectedRowKeys.value.length === 0) return
  const affected = await batchDisableGroupSetting(selectedRowKeys.value)
  message.success(`批量停用，共处理 ${affected || 0} 条`)
  fetchData()
}

async function batchRemove() {
  if (selectedRowKeys.value.length === 0) return
  const affected = await batchDeleteGroupSetting(selectedRowKeys.value)
  message.success(`批量删除，共处理 ${affected || 0} 条`)
  fetchData()
}

async function downloadExport() {
  const blob = await exportGroupSetting({
    keyword: query.keyword || undefined,
    groupType: query.groupType || undefined,
    status: query.status
  })
  const href = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = href
  link.download = `oa-group-setting-${new Date().toISOString().slice(0, 10)}.csv`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(href)
}

fetchData()
</script>
