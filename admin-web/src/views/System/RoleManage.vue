<template>
  <PageContainer title="角色管理" subTitle="只维护角色名称、所属部门、上级领导角色和页面权限">
    <template #meta>
      <span class="soft-pill">角色总数 {{ pagination.total || rows.length }}</span>
      <span class="soft-pill">启用 {{ enabledRoleCount }}</span>
      <span class="soft-pill">预设推荐 {{ recommendedPreset?.label || '无' }}</span>
      <span v-for="tag in activeFilterTags" :key="tag" class="selection-pill">{{ tag }}</span>
    </template>

    <template #stats>
      <div class="metric-grid">
        <div class="metric-card metric-card--primary">
          <span class="metric-card__label">当前页角色</span>
          <strong class="metric-card__value">{{ rows.length }}</strong>
          <span class="metric-card__hint">用于快速核对组织分工</span>
        </div>
        <div class="metric-card metric-card--success">
          <span class="metric-card__label">启用角色</span>
          <strong class="metric-card__value">{{ enabledRoleCount }}</strong>
          <span class="metric-card__hint">可参与当前权限与监管链</span>
        </div>
        <div class="metric-card metric-card--warning">
          <span class="metric-card__label">停用角色</span>
          <strong class="metric-card__value">{{ disabledRoleCount }}</strong>
          <span class="metric-card__hint">建议确认是否仍需保留</span>
        </div>
        <div class="metric-card">
          <span class="metric-card__label">编辑中权限</span>
          <strong class="metric-card__value">{{ checkedPagePermissions.length }}</strong>
          <span class="metric-card__hint">当前已勾选页面权限数</span>
        </div>
      </div>
    </template>

    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键词">
        <a-input v-model:value="query.keyword" placeholder="角色名称" allow-clear />
      </a-form-item>
      <template #extra>
        <a-button type="primary" @click="openDrawer()">新增角色</a-button>
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
        <template v-if="column.key === 'departmentName'">
          {{ resolveDepartmentName(record.departmentId) }}
        </template>
        <template v-else-if="column.key === 'superiorRoleName'">
          {{ resolveRoleName(record.superiorRoleId) }}
        </template>
        <template v-else-if="column.key === 'status'">
          <a-tag :color="record.status === 1 ? 'green' : 'default'">{{ record.status === 1 ? '启用' : '停用' }}</a-tag>
        </template>
        <template v-else-if="column.key === 'pagePermissions'">
          <a-tag color="blue">{{ parseRoutePermissionsJson(record.routePermissionsJson).length || 0 }} 项</a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="openLinkedStaff(record)">关联员工</a-button>
            <a-button type="link" @click="openDrawer(record)">编辑</a-button>
            <a-popconfirm title="确认删除该角色吗？" ok-text="确认" cancel-text="取消" @confirm="remove(record)">
              <a-button type="link" danger>删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </DataTable>

    <a-drawer v-model:open="drawerOpen" :title="drawerTitle" width="620">
      <a-form :model="form" layout="vertical">
        <a-form-item label="角色名称" required>
          <a-input v-model:value="form.roleName" placeholder="例如：行政人事部部长" />
        </a-form-item>
        <a-form-item label="所属部门" required>
          <a-select
            v-model:value="form.departmentId"
            show-search
            allow-clear
            :options="departmentOptions"
            :filter-option="filterOption"
            placeholder="选择所属部门"
          />
        </a-form-item>
        <a-form-item label="上级领导角色">
          <a-select
            v-model:value="form.superiorRoleId"
            show-search
            allow-clear
            :options="superiorRoleOptions"
            :filter-option="filterOption"
            placeholder="不选则表示本角色无上级领导角色"
          />
          <div class="permission-summary">用于员工账号里的“直接领导/间接领导”自动推荐。先定角色上级，再落到具体员工。</div>
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="form.status" :options="statusOptions" />
        </a-form-item>
        <a-form-item label="页面权限">
          <a-space direction="vertical" style="width: 100%">
            <a-alert
              type="info"
              show-icon
              :message="recommendedPreset ? `推荐预设：${recommendedPreset.label}` : '当前角色暂无推荐预设'"
              :description="recommendedPreset?.description || '可直接按页面树勾选。角色编码由系统自动生成，不需要人工填写。'"
            />
            <a-space wrap>
              <a-button size="small" @click="applyPresetPermissions" :disabled="!recommendedPreset">套用推荐预设</a-button>
              <a-button size="small" @click="checkedPagePermissions = []">清空页面权限</a-button>
              <span class="permission-summary">已选 {{ checkedPagePermissions.length }} 个页面</span>
            </a-space>
            <a-tree
              checkable
              default-expand-all
              :tree-data="pagePermissionTree"
              :checked-keys="checkedPagePermissions"
              @check="onPermissionCheck"
            />
          </a-space>
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
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { getRolePage, createRole, updateRole, deleteRole, getDepartmentOptionPage } from '../../api/rbac'
import type { DepartmentItem, PageResult, RoleItem } from '../../types'
import { getPagePermissionTree, getRolePagePreset, parseRoutePermissionsJson, serializeRoutePermissions } from '../../utils/pageAccess'

const router = useRouter()
const route = useRoute()
const query = reactive({ keyword: undefined as string | undefined, pageNo: 1, pageSize: 10 })
const rows = ref<RoleItem[]>([])
const allRoles = ref<RoleItem[]>([])
const departments = ref<DepartmentItem[]>([])
const loading = ref(false)
const saving = ref(false)
const drawerOpen = ref(false)
const checkedPagePermissions = ref<string[]>([])
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const form = reactive<Partial<RoleItem>>({ status: 1 })
const pagePermissionTree = getPagePermissionTree()

const statusOptions = [
  { label: '启用', value: 1 },
  { label: '停用', value: 0 }
]

const columns = [
  { title: '角色名称', dataIndex: 'roleName', key: 'roleName', width: 180 },
  { title: '所属部门', key: 'departmentName', width: 180 },
  { title: '上级领导角色', key: 'superiorRoleName', width: 180 },
  { title: '页面权限', key: 'pagePermissions', width: 120 },
  { title: '状态', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 140 }
]

const drawerTitle = computed(() => (form.id ? '编辑角色' : '新增角色'))
const recommendedPreset = computed(() => getRolePagePreset(form.roleCode || form.roleName))
const enabledRoleCount = computed(() => rows.value.filter((item) => item.status === 1).length)
const disabledRoleCount = computed(() => rows.value.filter((item) => item.status !== 1).length)
const activeFilterTags = computed(() => (query.keyword ? [`关键词: ${query.keyword}`] : []))
const departmentOptions = computed(() =>
  departments.value.map((item) => ({ label: item.deptName, value: item.id }))
)
const superiorRoleOptions = computed(() =>
  allRoles.value
    .filter((item) => item.id !== form.id)
    .map((item) => ({ label: item.roleName, value: item.id }))
)

function filterOption(input: string, option: { label?: string }) {
  return String(option?.label || '').toLowerCase().includes(String(input || '').toLowerCase())
}

function resolveDepartmentName(departmentId?: string | number) {
  if (departmentId == null || departmentId === '') return '-'
  return departments.value.find((item) => String(item.id) === String(departmentId))?.deptName || `#${departmentId}`
}

function resolveRoleName(roleId?: string | number) {
  if (roleId == null || roleId === '') return '-'
  return allRoles.value.find((item) => String(item.id) === String(roleId))?.roleName || `#${roleId}`
}

async function fetchRoles(pageParams = query) {
  const res: PageResult<RoleItem> = await getRolePage(pageParams)
  rows.value = res.list || []
  pagination.total = res.total || rows.value.length
  const allRes: PageResult<RoleItem> = await getRolePage({ pageNo: 1, pageSize: 300 })
  allRoles.value = allRes.list || []
}

async function fetchDepartments() {
  const res: PageResult<DepartmentItem> = await getDepartmentOptionPage({ pageNo: 1, pageSize: 300, activeOnly: false })
  departments.value = res.list || []
}

async function fetchData() {
  loading.value = true
  try {
    await Promise.all([fetchRoles(), fetchDepartments()])
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

function openDrawer(record?: RoleItem) {
  Object.assign(form, {
    id: record?.id,
    roleName: record?.roleName || '',
    roleCode: record?.roleCode || '',
    departmentId: record?.departmentId,
    superiorRoleId: record?.superiorRoleId,
    routePermissionsJson: record?.routePermissionsJson || '',
    status: record?.status ?? 1
  })
  checkedPagePermissions.value = parseRoutePermissionsJson(record?.routePermissionsJson)
  drawerOpen.value = true
}

function syncDrawerFromRoute() {
  const editId = Number(route.query.edit || route.query.roleId || 0)
  if (editId > 0) {
    const matched = allRoles.value.find((item) => Number(item.id) === editId) || rows.value.find((item) => Number(item.id) === editId)
    if (matched) {
      openDrawer(matched)
      return
    }
  }
  if (String(route.query.open || '') === 'new') {
    openDrawer()
  }
}

function onPermissionCheck(checkedKeys: string[] | { checked: string[] }) {
  checkedPagePermissions.value = Array.isArray(checkedKeys) ? checkedKeys : checkedKeys.checked || []
}

function applyPresetPermissions() {
  if (!recommendedPreset.value) {
    message.warning('当前角色没有推荐预设，可手动勾选')
    return
  }
  checkedPagePermissions.value = [...recommendedPreset.value.paths]
}

async function submit() {
  if (!form.roleName) {
    message.error('请填写角色名称')
    return
  }
  if (!form.departmentId) {
    message.error('请选择所属部门')
    return
  }
  saving.value = true
  try {
    const payload = {
      roleName: form.roleName,
      departmentId: form.departmentId,
      superiorRoleId: form.superiorRoleId,
      routePermissionsJson: serializeRoutePermissions(checkedPagePermissions.value),
      status: form.status ?? 1
    }
    if (form.id) {
      await updateRole(Number(form.id), payload)
    } else {
      await createRole(payload)
    }
    message.success('保存成功，相关账号重新登录后生效')
    drawerOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function remove(record: RoleItem) {
  await deleteRole(record.id)
  message.success('删除成功')
  fetchData()
}

function openLinkedStaff(record: RoleItem) {
  router.push({
    path: '/hr/profile/account-access',
    query: { roleId: record.id }
  })
}

watch(
  () => [route.query.edit, route.query.roleId, route.query.open, allRoles.value.length].join('|'),
  () => {
    syncDrawerFromRoute()
  },
  { immediate: false }
)

onMounted(async () => {
  await fetchData()
  syncDrawerFromRoute()
})
</script>

<style scoped>
.permission-summary {
  color: rgba(0, 0, 0, 0.45);
  font-size: 12px;
}
</style>
