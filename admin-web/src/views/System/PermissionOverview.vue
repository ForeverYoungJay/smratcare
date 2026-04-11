<template>
  <PageContainer title="权限总览" subTitle="只维护角色模板与页面权限。账号赋权请前往员工账号设置。">
    <template #meta>
      <a-space wrap size="small">
        <span class="soft-pill">部门数：{{ departments.length }}</span>
        <span class="soft-pill">角色模板：{{ roleRows.length }}</span>
        <span class="selection-pill">推荐模板：{{ presetEntries.length }}</span>
      </a-space>
    </template>

    <template #stats>
      <div class="metric-grid metric-grid--4">
        <div class="metric-card">
          <span>角色模板</span>
          <strong>{{ roleRows.length }}</strong>
          <small>统一维护页面权限模板</small>
        </div>
        <div class="metric-card">
          <span>启用角色</span>
          <strong>{{ enabledRoleCount }}</strong>
          <small>当前可分配给账号的角色</small>
        </div>
        <div class="metric-card">
          <span>推荐组织模板</span>
          <strong>{{ presetEntries.length }}</strong>
          <small>帮助快速理解岗位分工</small>
        </div>
        <div class="metric-card">
          <span>抽屉已选页面</span>
          <strong>{{ checkedPagePermissions.length }}</strong>
          <small>当前角色权限颗粒度</small>
        </div>
      </div>
    </template>

    <a-card class="card-elevated" :bordered="false" title="1. 组织架构建议">
          <a-row :gutter="[12, 12]">
            <a-col v-for="preset in presetEntries" :key="preset.code" :xs="24" :md="12">
              <div class="preset-card">
                <div class="preset-title">{{ preset.label }}</div>
                <div class="preset-desc">{{ preset.description }}</div>
              </div>
            </a-col>
          </a-row>
    </a-card>

    <a-card class="card-elevated" :bordered="false" title="2. 角色权限模板" style="margin-top: 12px">
      <template #extra>
        <a-button type="primary" @click="openRoleDrawer()">新增角色</a-button>
      </template>
      <a-table :columns="roleColumns" :data-source="roleRows" :loading="roleLoading" row-key="id" :pagination="false">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'departmentName'">
            {{ resolveDepartmentName(record.departmentId) }}
          </template>
          <template v-else-if="column.key === 'superiorRoleName'">
            {{ resolveRoleName(record.superiorRoleId) }}
          </template>
          <template v-else-if="column.key === 'preset'">
            <a-tag :color="getRolePagePreset(record.roleCode || record.roleName) ? 'purple' : 'default'">
              {{ getRolePagePreset(record.roleCode || record.roleName)?.label || '自定义' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'pagePermissions'">
            <a-tag color="blue">{{ parseRoutePermissionsJson(record.routePermissionsJson).length }} 项</a-tag>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'green' : 'default'">{{ record.status === 1 ? '启用' : '停用' }}</a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-button type="link" @click="openRoleDrawer(record)">编辑</a-button>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-drawer v-model:open="roleDrawerOpen" :title="roleDrawerTitle" width="620">
      <a-form :model="roleForm" layout="vertical">
        <a-form-item label="角色名称" required>
          <a-input v-model:value="roleForm.roleName" />
        </a-form-item>
        <a-form-item label="所属部门" required>
          <a-select
            v-model:value="roleForm.departmentId"
            show-search
            allow-clear
            :options="departmentOptions"
            :filter-option="filterOption"
            placeholder="选择所属部门"
          />
        </a-form-item>
        <a-form-item label="上级领导角色">
          <a-select
            v-model:value="roleForm.superiorRoleId"
            show-search
            allow-clear
            :options="superiorRoleOptions"
            :filter-option="filterOption"
            placeholder="不选则表示无上级领导角色"
          />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="roleForm.status" :options="statusOptions" />
        </a-form-item>
        <a-form-item label="页面权限">
          <a-space direction="vertical" style="width: 100%">
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
          <a-button @click="roleDrawerOpen = false">取消</a-button>
          <a-button type="primary" :loading="roleSaving" @click="saveRole">保存</a-button>
        </a-space>
      </template>
    </a-drawer>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import { createRole, getDepartmentOptionPage, getRolePage, updateRole } from '../../api/rbac'
import type { DepartmentItem, PageResult, RoleItem } from '../../types'
import { ROLE_PAGE_PRESETS, getPagePermissionTree, getRecommendedPagePermissions, getRolePagePreset, parseRoutePermissionsJson, serializeRoutePermissions } from '../../utils/pageAccess'

const roleRows = ref<RoleItem[]>([])
const allRoles = ref<RoleItem[]>([])
const departments = ref<DepartmentItem[]>([])
const roleLoading = ref(false)
const roleSaving = ref(false)
const roleDrawerOpen = ref(false)
const checkedPagePermissions = ref<string[]>([])
const pagePermissionTree = getPagePermissionTree()
const roleForm = reactive<Partial<RoleItem>>({ status: 1 })

const presetEntries = computed(() => Object.entries(ROLE_PAGE_PRESETS).map(([code, preset]) => ({ code, ...preset })))
const departmentOptions = computed(() => departments.value.map((item) => ({ label: item.deptName, value: item.id })))
const superiorRoleOptions = computed(() =>
  allRoles.value.filter((item) => item.id !== roleForm.id).map((item) => ({ label: item.roleName, value: item.id }))
)
const roleDrawerTitle = computed(() => (roleForm.id ? '编辑角色权限模板' : '新增角色权限模板'))
const recommendedPreset = computed(() => getRolePagePreset(roleForm.roleCode || roleForm.roleName))
const enabledRoleCount = computed(() => roleRows.value.filter((item) => Number(item.status) === 1).length)

const roleColumns = [
  { title: '角色名称', dataIndex: 'roleName', key: 'roleName', width: 180 },
  { title: '所属部门', key: 'departmentName', width: 160 },
  { title: '上级领导角色', key: 'superiorRoleName', width: 160 },
  { title: '建议模板', key: 'preset', width: 120 },
  { title: '页面权限', key: 'pagePermissions', width: 100 },
  { title: '状态', key: 'status', width: 90 },
  { title: '操作', key: 'action', width: 90 }
]

const statusOptions = [
  { label: '启用', value: 1 },
  { label: '停用', value: 0 }
]

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

async function fetchRoles() {
  roleLoading.value = true
  try {
    const res: PageResult<RoleItem> = await getRolePage({ pageNo: 1, pageSize: 300 })
    roleRows.value = res.list || []
    allRoles.value = res.list || []
  } finally {
    roleLoading.value = false
  }
}

async function fetchDepartments() {
  const res: PageResult<DepartmentItem> = await getDepartmentOptionPage({ pageNo: 1, pageSize: 300, activeOnly: false })
  departments.value = res.list || []
}

function openRoleDrawer(record?: RoleItem) {
  Object.assign(roleForm, {
    id: record?.id,
    roleName: record?.roleName || '',
    roleCode: record?.roleCode || '',
    departmentId: record?.departmentId,
    superiorRoleId: record?.superiorRoleId,
    routePermissionsJson: record?.routePermissionsJson || '',
    status: record?.status ?? 1
  })
  checkedPagePermissions.value = parseRoutePermissionsJson(record?.routePermissionsJson)
  roleDrawerOpen.value = true
}

function applyPresetPermissions() {
  checkedPagePermissions.value = getRecommendedPagePermissions(roleForm.roleCode || roleForm.roleName)
}

function onPermissionCheck(checkedKeys: string[] | { checked: string[] }) {
  checkedPagePermissions.value = Array.isArray(checkedKeys) ? checkedKeys : checkedKeys.checked || []
}

async function saveRole() {
  if (!roleForm.roleName) {
    message.error('请填写角色名称')
    return
  }
  if (!roleForm.departmentId) {
    message.error('请选择所属部门')
    return
  }
  roleSaving.value = true
  try {
    const payload = {
      roleName: roleForm.roleName,
      departmentId: roleForm.departmentId,
      superiorRoleId: roleForm.superiorRoleId,
      routePermissionsJson: serializeRoutePermissions(checkedPagePermissions.value),
      status: roleForm.status ?? 1
    }
    if (roleForm.id) {
      await updateRole(Number(roleForm.id), payload)
    } else {
      await createRole(payload)
    }
    message.success('角色模板已保存，相关账号重新登录后生效')
    roleDrawerOpen.value = false
    await fetchRoles()
  } finally {
    roleSaving.value = false
  }
}

onMounted(async () => {
  await Promise.all([fetchRoles(), fetchDepartments()])
})
</script>

<style scoped>
.permission-summary {
  margin-top: 10px;
  color: rgba(0, 0, 0, 0.65);
}

.preset-card {
  height: 100%;
  padding: 14px;
  border-radius: 12px;
  background: linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%);
}

.preset-title {
  font-weight: 600;
  margin-bottom: 6px;
}

.preset-desc {
  color: rgba(0, 0, 0, 0.55);
  font-size: 12px;
  line-height: 1.6;
}
</style>
