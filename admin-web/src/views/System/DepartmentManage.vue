<template>
  <PageContainer title="部门管理" subTitle="维护机构部门层级与状态">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键词">
        <a-input v-model:value="query.keyword" placeholder="部门名称/编码" allow-clear />
      </a-form-item>
      <template #extra>
        <a-button type="primary" @click="openDrawer()">新增部门</a-button>
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
        <template v-else-if="column.key === 'parentId'">
          {{ resolveParentName(record.parentId) }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="openDrawer(record)">编辑</a-button>
            <a-popconfirm title="确认删除该部门吗？" ok-text="确认" cancel-text="取消" @confirm="remove(record)">
              <a-button type="link" danger>删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </DataTable>

    <a-drawer v-model:open="drawerOpen" :title="drawerTitle" width="520">
      <a-form :model="form" layout="vertical">
        <a-form-item label="部门名称" required>
          <a-input v-model:value="form.deptName" />
        </a-form-item>
        <a-form-item label="部门编码">
          <a-input v-model:value="form.deptCode" />
        </a-form-item>
        <a-form-item label="上级部门">
          <a-select v-model:value="form.parentId" allow-clear :options="parentOptions" />
        </a-form-item>
        <a-form-item label="排序">
          <a-input-number v-model:value="form.sortNo" :min="0" style="width: 100%" />
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
import { getDepartmentPage, createDepartment, updateDepartment, deleteDepartment } from '../../api/rbac'
import type { DepartmentItem, PageResult } from '../../types'

const query = reactive({ keyword: undefined as string | undefined, pageNo: 1, pageSize: 10 })
const rows = ref<DepartmentItem[]>([])
const allRows = ref<DepartmentItem[]>([])
const loading = ref(false)
const saving = ref(false)
const drawerOpen = ref(false)
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const form = reactive<Partial<DepartmentItem>>({ status: 1, sortNo: 0 })

const statusOptions = [
  { label: '启用', value: 1 },
  { label: '停用', value: 0 }
]

const columns = [
  { title: '部门名称', dataIndex: 'deptName', key: 'deptName', width: 180 },
  { title: '部门编码', dataIndex: 'deptCode', key: 'deptCode', width: 160 },
  { title: '上级部门', key: 'parentId', width: 150 },
  { title: '排序', dataIndex: 'sortNo', key: 'sortNo', width: 90 },
  { title: '状态', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 120 }
]

const drawerTitle = computed(() => (form.id ? '编辑部门' : '新增部门'))
const parentOptions = computed(() =>
  allRows.value
    .filter((item) => item.id !== form.id)
    .map((item) => ({ label: item.deptName, value: item.id }))
)

function resolveParentName(parentId?: number) {
  if (!parentId) return '-'
  return allRows.value.find((item) => item.id === parentId)?.deptName || `#${parentId}`
}

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<DepartmentItem> = await getDepartmentPage(query)
    rows.value = res.list
    pagination.total = res.total || res.list.length
    const allRes: PageResult<DepartmentItem> = await getDepartmentPage({ pageNo: 1, pageSize: 1000 })
    allRows.value = allRes.list
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

function openDrawer(record?: DepartmentItem) {
  Object.assign(form, {
    id: record?.id,
    deptName: record?.deptName || '',
    deptCode: record?.deptCode || '',
    parentId: record?.parentId,
    sortNo: record?.sortNo ?? 0,
    status: record?.status ?? 1
  })
  drawerOpen.value = true
}

async function submit() {
  if (!form.deptName) {
    message.error('请填写部门名称')
    return
  }
  saving.value = true
  try {
    const payload = {
      deptName: form.deptName,
      deptCode: form.deptCode,
      parentId: form.parentId,
      sortNo: form.sortNo ?? 0,
      status: form.status ?? 1
    }
    if (form.id) {
      await updateDepartment(form.id, payload)
    } else {
      await createDepartment(payload)
    }
    message.success('保存成功')
    drawerOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function remove(record: DepartmentItem) {
  await deleteDepartment(record.id)
  message.success('删除成功')
  fetchData()
}

fetchData()
</script>
