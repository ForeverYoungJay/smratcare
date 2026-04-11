<template>
  <PageContainer title="权限总览" subTitle="查看当前角色模板的生效权限，统一跳转到角色管理维护。">
    <template #meta>
      <a-space wrap size="small">
        <span class="soft-pill">部门数：{{ departments.length }}</span>
        <span class="soft-pill">角色模板数：{{ roleRows.length }}</span>
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
          <span>默认模板</span>
          <strong>{{ defaultTemplateCount }}</strong>
          <small>沿用系统预设权限的角色数</small>
        </div>
      </div>
    </template>

    <a-card class="card-elevated" :bordered="false" title="角色权限模板快照" style="margin-top: 12px">
      <template #extra>
        <a-space>
          <span class="permission-summary">角色模板统一在“角色管理”页面维护</span>
          <a-button @click="goRoleManage()">去角色管理</a-button>
          <a-button type="primary" @click="goRoleManage(undefined, true)">新增角色</a-button>
        </a-space>
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
            <a-space size="small">
              <a-tag color="blue">{{ getEffectivePagePermissions(record).length }} 项</a-tag>
              <a-tag :color="parseRoutePermissionsJson(record.routePermissionsJson).length ? 'gold' : 'default'">
                {{ parseRoutePermissionsJson(record.routePermissionsJson).length ? '自定义' : '默认' }}
              </a-tag>
            </a-space>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'green' : 'default'">{{ record.status === 1 ? '启用' : '停用' }}</a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-button type="link" @click="goRoleManage(record)">去维护</a-button>
          </template>
        </template>
      </a-table>
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import { getDepartmentOptionPage, getRolePage } from '../../api/rbac'
import type { DepartmentItem, PageResult, RoleItem } from '../../types'
import { ROLE_PAGE_PRESETS, getRecommendedPagePermissions, getRolePagePreset, parseRoutePermissionsJson } from '../../utils/pageAccess'

const router = useRouter()
const roleRows = ref<RoleItem[]>([])
const allRoles = ref<RoleItem[]>([])
const departments = ref<DepartmentItem[]>([])
const roleLoading = ref(false)

const presetEntries = computed(() => Object.entries(ROLE_PAGE_PRESETS).map(([code, preset]) => ({ code, ...preset })))
const enabledRoleCount = computed(() => roleRows.value.filter((item) => Number(item.status) === 1).length)
const defaultTemplateCount = computed(() =>
  roleRows.value.filter((item) => parseRoutePermissionsJson(item.routePermissionsJson).length === 0).length
)

const roleColumns = [
  { title: '角色名称', dataIndex: 'roleName', key: 'roleName', width: 180 },
  { title: '所属部门', key: 'departmentName', width: 160 },
  { title: '上级领导角色', key: 'superiorRoleName', width: 160 },
  { title: '建议模板', key: 'preset', width: 120 },
  { title: '页面权限', key: 'pagePermissions', width: 100 },
  { title: '状态', key: 'status', width: 90 },
  { title: '操作', key: 'action', width: 90 }
]

function resolveDepartmentName(departmentId?: string | number) {
  if (departmentId == null || departmentId === '') return '-'
  return departments.value.find((item) => String(item.id) === String(departmentId))?.deptName || `#${departmentId}`
}

function resolveRoleName(roleId?: string | number) {
  if (roleId == null || roleId === '') return '-'
  return allRoles.value.find((item) => String(item.id) === String(roleId))?.roleName || `#${roleId}`
}

function getEffectivePagePermissions(role?: Partial<RoleItem>) {
  const explicitPermissions = parseRoutePermissionsJson(role?.routePermissionsJson)
  if (explicitPermissions.length) {
    return explicitPermissions
  }
  return getRecommendedPagePermissions(role?.roleCode || role?.roleName)
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

function goRoleManage(record?: RoleItem, createNew = false) {
  router.push({
    path: '/system/role',
    query: createNew
      ? { open: 'new' }
      : record?.id
        ? { edit: record.id }
        : undefined
  })
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
</style>
