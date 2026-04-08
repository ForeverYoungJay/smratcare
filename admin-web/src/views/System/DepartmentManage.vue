<template>
  <PageContainer title="部门管理" subTitle="只维护部门名称和状态，不再设置排序、部门编码和上级部门">
    <template #meta>
      <span class="soft-pill">部门总数 {{ pagination.total || rows.length }}</span>
      <span class="soft-pill">启用 {{ enabledCount }}</span>
      <span class="soft-pill">停用 {{ disabledCount }}</span>
      <span v-for="tag in activeFilterTags" :key="tag" class="selection-pill">{{ tag }}</span>
    </template>

    <template #stats>
      <div class="metric-grid">
        <div class="metric-card metric-card--primary">
          <span class="metric-card__label">当前页部门</span>
          <strong class="metric-card__value">{{ rows.length }}</strong>
          <span class="metric-card__hint">用于维护组织基础结构</span>
        </div>
        <div class="metric-card metric-card--success">
          <span class="metric-card__label">启用部门</span>
          <strong class="metric-card__value">{{ enabledCount }}</strong>
          <span class="metric-card__hint">可继续用于档案和账号归属</span>
        </div>
        <div class="metric-card metric-card--warning">
          <span class="metric-card__label">停用部门</span>
          <strong class="metric-card__value">{{ disabledCount }}</strong>
          <span class="metric-card__hint">建议核对是否仍需保留</span>
        </div>
        <div class="metric-card">
          <span class="metric-card__label">快捷入口</span>
          <strong class="metric-card__value">2</strong>
          <span class="metric-card__hint">支持跳转档案中心与账号设置</span>
        </div>
      </div>
    </template>

    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键词">
        <a-input v-model:value="query.keyword" placeholder="部门名称" allow-clear />
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
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="openHrProfile(record)">档案中心</a-button>
            <a-button type="link" @click="openAccountAccess(record)">账号设置</a-button>
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
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { getDepartmentPage, createDepartment, updateDepartment, deleteDepartment } from '../../api/rbac'
import { useLiveSyncRefresh } from '../../composables/useLiveSyncRefresh'
import type { DepartmentItem, PageResult } from '../../types'

const router = useRouter()
const query = reactive({ keyword: undefined as string | undefined, pageNo: 1, pageSize: 10 })
const rows = ref<DepartmentItem[]>([])
const loading = ref(false)
const saving = ref(false)
const drawerOpen = ref(false)
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const form = reactive<Partial<DepartmentItem>>({ status: 1 })

const statusOptions = [
  { label: '启用', value: 1 },
  { label: '停用', value: 0 }
]

const columns = [
  { title: '部门名称', dataIndex: 'deptName', key: 'deptName', width: 180 },
  { title: '状态', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 120 }
]

const drawerTitle = computed(() => (form.id ? '编辑部门' : '新增部门'))
const enabledCount = computed(() => rows.value.filter((item) => item.status === 1).length)
const disabledCount = computed(() => rows.value.filter((item) => item.status !== 1).length)
const activeFilterTags = computed(() => (query.keyword ? [`关键词: ${query.keyword}`] : []))

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<DepartmentItem> = await getDepartmentPage(query)
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

function openDrawer(record?: DepartmentItem) {
  Object.assign(form, {
    id: record?.id,
    deptName: record?.deptName || '',
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

function openHrProfile(record: DepartmentItem) {
  router.push({
    path: '/hr/profile/basic',
    query: { departmentId: record.id }
  })
}

function openAccountAccess(record: DepartmentItem) {
  router.push({
    path: '/hr/profile/account-access',
    query: { departmentId: record.id }
  })
}

fetchData()

useLiveSyncRefresh({
  topics: ['system', 'hr', 'oa'],
  refresh: () => {
    if (loading.value || saving.value) return
    fetchData().catch(() => {})
  },
  debounceMs: 900
})
</script>
