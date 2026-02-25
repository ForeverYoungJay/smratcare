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
      </template>
    </SearchForm>

    <DataTable
      rowKey="id"
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
import { reactive, ref } from 'vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { createGroupSetting, deleteGroupSetting, getGroupSettingPage, updateGroupSetting } from '../../api/oa'
import type { OaGroupSetting, PageResult } from '../../types'

const loading = ref(false)
const saving = ref(false)
const rows = ref<OaGroupSetting[]>([])
const query = reactive({ keyword: '', groupType: '', status: undefined as string | undefined, pageNo: 1, pageSize: 10 })
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

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

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<OaGroupSetting> = await getGroupSettingPage(query)
    rows.value = res.list
    pagination.total = res.total || res.list.length
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

fetchData()
</script>
