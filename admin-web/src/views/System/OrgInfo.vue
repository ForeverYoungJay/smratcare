<template>
  <PageContainer title="机构信息" subTitle="维护机构基础资料">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键词">
        <a-input v-model:value="query.keyword" placeholder="机构名称/编码" allow-clear />
      </a-form-item>
      <template #extra>
        <a-button type="primary" @click="openDrawer()">新增机构</a-button>
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
          <a-tag :color="record.status === 1 ? 'green' : 'default'">
            {{ record.status === 1 ? '启用' : '停用' }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="openDrawer(record)">编辑</a-button>
            <a-popconfirm title="确认删除该机构吗？" ok-text="确认" cancel-text="取消" @confirm="remove(record)">
              <a-button type="link" danger>删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </DataTable>

    <a-drawer v-model:open="drawerOpen" :title="drawerTitle" width="560">
      <a-form :model="form" layout="vertical">
        <a-form-item label="机构编码" required>
          <a-input v-model:value="form.orgCode" />
        </a-form-item>
        <a-form-item label="机构名称" required>
          <a-input v-model:value="form.orgName" />
        </a-form-item>
        <a-form-item label="机构类型" required>
          <a-input v-model:value="form.orgType" placeholder="如 养老院/社区中心" />
        </a-form-item>
        <a-form-item label="联系人">
          <a-input v-model:value="form.contactName" />
        </a-form-item>
        <a-form-item label="联系电话">
          <a-input v-model:value="form.contactPhone" />
        </a-form-item>
        <a-form-item label="地址">
          <a-textarea v-model:value="form.address" :rows="2" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="form.status" :options="statusOptions" />
        </a-form-item>
      </a-form>
      <template #footer>
        <a-space>
          <a-button @click="drawerOpen = false">取消</a-button>
          <a-button type="primary" :loading="saving" @click="submit">保存</a-button>
        </a-space>
      </template>
    </a-drawer>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { getOrgPage, createOrg, updateOrg, deleteOrg } from '../../api/rbac'
import type { OrgItem, PageResult } from '../../types'

const query = reactive({ keyword: undefined as string | undefined, pageNo: 1, pageSize: 10 })
const rows = ref<OrgItem[]>([])
const loading = ref(false)
const saving = ref(false)
const drawerOpen = ref(false)
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const form = reactive<Partial<OrgItem>>({ status: 1 })

const statusOptions = [
  { label: '启用', value: 1 },
  { label: '停用', value: 0 }
]

const columns = [
  { title: '机构编码', dataIndex: 'orgCode', key: 'orgCode', width: 160 },
  { title: '机构名称', dataIndex: 'orgName', key: 'orgName', width: 160 },
  { title: '机构类型', dataIndex: 'orgType', key: 'orgType', width: 130 },
  { title: '联系人', dataIndex: 'contactName', key: 'contactName', width: 120 },
  { title: '联系电话', dataIndex: 'contactPhone', key: 'contactPhone', width: 140 },
  { title: '状态', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 120 }
]

const drawerTitle = computed(() => (form.id ? '编辑机构' : '新增机构'))

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<OrgItem> = await getOrgPage(query)
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
  query.pageNo = 1
  query.pageSize = pagination.pageSize
  pagination.current = 1
  fetchData()
}

function openDrawer(record?: OrgItem) {
  Object.assign(form, {
    id: record?.id,
    orgCode: record?.orgCode || '',
    orgName: record?.orgName || '',
    orgType: record?.orgType || '',
    contactName: record?.contactName || '',
    contactPhone: record?.contactPhone || '',
    address: record?.address || '',
    status: record?.status ?? 1
  })
  drawerOpen.value = true
}

async function submit() {
  if (!form.orgCode || !form.orgName || !form.orgType) {
    message.error('请填写机构编码、机构名称和机构类型')
    return
  }
  saving.value = true
  try {
    const payload = {
      orgCode: form.orgCode,
      orgName: form.orgName,
      orgType: form.orgType,
      contactName: form.contactName,
      contactPhone: form.contactPhone,
      address: form.address,
      status: form.status ?? 1
    }
    if (form.id) {
      await updateOrg(form.id, payload)
    } else {
      await createOrg(payload)
    }
    message.success('保存成功')
    drawerOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function remove(record: OrgItem) {
  await deleteOrg(record.id)
  message.success('删除成功')
  fetchData()
}

fetchData()
</script>
