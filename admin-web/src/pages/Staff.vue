<template>
  <PageContainer title="员工管理" subTitle="账号与角色配置">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键字">
        <a-input v-model:value="query.keyword" placeholder="姓名/账号" />
      </a-form-item>
      <template #extra>
        <a-button type="primary" @click="openDrawer()">新增员工</a-button>
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
            <a @click="openDrawer(record)">编辑</a>
            <a @click="openRole(record)">分配角色</a>
          </a-space>
        </template>
      </template>
    </DataTable>

    <a-drawer v-model:open="drawerOpen" :title="drawerTitle" width="520">
      <a-form :model="form" layout="vertical">
        <a-form-item label="账号" required>
          <a-input v-model:value="form.username" />
        </a-form-item>
        <a-form-item label="姓名" required>
          <a-input v-model:value="form.realName" />
        </a-form-item>
        <a-form-item label="部门">
          <a-select v-model:value="form.departmentId" :options="departmentOptions" />
        </a-form-item>
        <a-form-item label="手机号">
          <a-input v-model:value="form.phone" />
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

    <a-modal v-model:open="roleOpen" title="分配角色" @ok="submitRole">
      <a-form layout="vertical">
        <a-form-item label="角色" required>
          <a-select v-model:value="roleForm.roleIds" mode="multiple" :options="roleOptions" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import PageContainer from '../components/PageContainer.vue'
import SearchForm from '../components/SearchForm.vue'
import DataTable from '../components/DataTable.vue'
import { getStaffPage, createStaff, updateStaff, updateStaffRoles } from '../api/staff'
import { getRolePage } from '../api/role'
import { getDepartmentPage } from '../api/department'
import type { StaffItem, ApiResult, PageResult, RoleItem, DepartmentItem } from '../types/api'

const query = reactive({ keyword: undefined as string | undefined, pageNo: 1, pageSize: 10 })
const rows = ref<StaffItem[]>([])
const loading = ref(false)
const saving = ref(false)
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

const columns = [
  { title: '账号', dataIndex: 'username', key: 'username', width: 120 },
  { title: '姓名', dataIndex: 'realName', key: 'realName', width: 120 },
  { title: '部门', dataIndex: 'departmentId', key: 'departmentId', width: 120 },
  { title: '手机号', dataIndex: 'phone', key: 'phone', width: 140 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 160 }
]

const drawerOpen = ref(false)
const form = reactive<Partial<StaffItem>>({})
const statusOptions = [
  { label: '启用', value: 1 },
  { label: '停用', value: 0 }
]

const roleOpen = ref(false)
const roleForm = reactive<{ staffId?: number; roleIds: number[] }>({ roleIds: [] })

const roles = ref<RoleItem[]>([])
const departments = ref<DepartmentItem[]>([])

const roleOptions = computed(() => roles.value.map((r) => ({ label: r.roleName, value: r.id })))
const departmentOptions = computed(() => departments.value.map((d) => ({ label: d.deptName, value: d.id })))
const drawerTitle = computed(() => (form.id ? '编辑员工' : '新增员工'))

async function fetchData() {
  loading.value = true
  try {
    const res: ApiResult<PageResult<StaffItem>> = await getStaffPage(query)
    if (res.code === 0) {
      rows.value = res.data.records
      pagination.total = res.data.total || res.data.records.length
    }
    const roleRes: ApiResult<PageResult<RoleItem>> = await getRolePage({ pageNo: 1, pageSize: 200 })
    if (roleRes.code === 0) roles.value = roleRes.data.records
    const deptRes: ApiResult<PageResult<DepartmentItem>> = await getDepartmentPage({ pageNo: 1, pageSize: 200 })
    if (deptRes.code === 0) departments.value = deptRes.data.records
  } catch {
    rows.value = []
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
  pagination.current = 1
  fetchData()
}

function openDrawer(record?: StaffItem) {
  Object.assign(form, record || { status: 1 })
  drawerOpen.value = true
}

async function submit() {
  saving.value = true
  try {
    if (form.id) {
      await updateStaff(form.id, form)
    } else {
      await createStaff(form)
    }
    message.success('保存成功')
    drawerOpen.value = false
    fetchData()
  } catch {
    message.error('保存失败')
  } finally {
    saving.value = false
  }
}

function openRole(record: StaffItem) {
  roleForm.staffId = record.id
  roleForm.roleIds = []
  roleOpen.value = true
}

async function submitRole() {
  if (!roleForm.staffId) return
  try {
    await updateStaffRoles(roleForm.staffId, roleForm.roleIds)
    message.success('角色已更新')
    roleOpen.value = false
  } catch {
    message.error('更新失败')
  }
}

fetchData()
</script>
