<template>
  <PageContainer title="权限总览" subTitle="管理员可在这里配置与部门管理、角色管理、账号管理一致的组织架构权限">
    <a-card class="card-elevated" :bordered="false" style="margin-bottom: 12px">
      <template #title>组织架构预设</template>
      <a-space wrap>
        <a-switch v-model:checked="simulateEnabled" />
        <span>{{ simulateEnabled ? '已启用角色模拟' : '当前展示登录账号的真实权限' }}</span>
      </a-space>
      <a-space wrap style="margin-top: 10px">
        <a-button
          v-for="preset in presetEntries"
          :key="preset.code"
          size="small"
          @click="applyPreset(preset.code)"
        >
          {{ preset.label }}
        </a-button>
        <a-button size="small" danger @click="clearSimulation">清空模拟</a-button>
      </a-space>
      <a-input
        v-model:value="simulateInput"
        style="margin-top: 10px"
        placeholder="输入角色编码并用英文逗号分隔，例如：NURSING_MINISTER,HR_EMPLOYEE"
      />
      <div class="simulate-tip">模拟只影响本页预览，不会修改真实账号。</div>
    </a-card>

    <a-row :gutter="12" style="margin-bottom: 12px">
      <a-col :xs="24" :md="6">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic :title="simulateEnabled ? '模拟角色数量' : '当前角色数量'" :value="activeRoles.length" />
          <div class="role-list">
            <a-tag v-for="role in activeRoles" :key="role" color="blue">{{ role }}</a-tag>
          </div>
        </a-card>
      </a-col>
      <a-col :xs="24" :md="6">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="生效页面权限数" :value="activePagePermissions.length" />
          <div class="summary-tip">{{ activePagePermissions.length ? '已进入页权限收口模式' : '未配置页权限时沿用旧角色规则' }}</div>
        </a-card>
      </a-col>
      <a-col :xs="24" :md="4">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="员工能力" :value="capability.staff ? '是' : '否'" />
        </a-card>
      </a-col>
      <a-col :xs="24" :md="4">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="部长能力" :value="capability.minister ? '是' : '否'" />
        </a-card>
      </a-col>
      <a-col :xs="24" :md="4">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="管理层能力" :value="capability.super ? '是' : '否'" />
        </a-card>
      </a-col>
    </a-row>

    <a-card class="card-elevated" :bordered="false" style="margin-bottom: 12px">
      <template #title>账号权限调整</template>
      <a-alert
        type="info"
        show-icon
        style="margin-bottom: 12px"
        message="员工账号通过角色继承页面权限"
        description="先给角色配置页面权限，再为账号勾选对应角色。这样会与部门管理、角色管理、账号管理保持同一套行政人事部/护理部/后勤部组织架构。"
      />
      <a-row :gutter="12">
        <a-col :xs="24" :md="8">
          <a-form-item label="选择员工">
            <a-select
              v-model:value="accountForm.staffId"
              show-search
              allow-clear
              placeholder="选择要调整的员工账号"
              :options="staffOptions"
              :filter-option="filterOption"
              @change="handleStaffChange"
            />
          </a-form-item>
        </a-col>
        <a-col :xs="24" :md="12">
          <a-form-item label="生效角色">
            <a-select
              v-model:value="accountForm.roleIds"
              mode="multiple"
              placeholder="给该账号分配角色"
              :options="roleOptions"
              :disabled="!accountForm.staffId"
            />
          </a-form-item>
        </a-col>
        <a-col :xs="24" :md="4" class="account-action">
          <a-button type="primary" :loading="accountSaving" :disabled="!accountForm.staffId" @click="saveAccountRoles">
            保存账号权限
          </a-button>
        </a-col>
      </a-row>
      <div v-if="selectedAccountRoleNames.length" class="selected-role-summary">
        当前账号角色：
        <a-tag v-for="role in selectedAccountRoleNames" :key="role" color="cyan">{{ role }}</a-tag>
      </div>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-bottom: 12px">
      <template #title>角色页面权限配置</template>
      <template #extra>
        <a-button type="primary" @click="openRoleDrawer()">新增角色</a-button>
      </template>
      <a-table :columns="roleColumns" :data-source="roleRows" :loading="roleLoading" row-key="id" :pagination="false">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'preset'">
            <a-tag :color="getRolePagePreset(record.roleCode) ? 'purple' : 'default'">
              {{ getRolePagePreset(record.roleCode)?.label || '自定义' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'pagePermissions'">
            <a-tag color="blue">{{ parseRoutePermissionsJson(record.routePermissionsJson).length }} 项</a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" @click="openRoleDrawer(record)">编辑页面权限</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-card class="card-elevated" :bordered="false">
      <template #title>核心模块访问检查</template>
      <a-table :columns="accessColumns" :data-source="accessRows" :pagination="false" row-key="path">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'requiredRoles'">
            <span v-if="record.requiredRoles.length === 0">公开</span>
            <span v-else>{{ record.requiredRoles.join(' / ') }}</span>
          </template>
          <template v-else-if="column.key === 'pagePermission'">
            <span>{{ record.pagePermissionLabel }}</span>
          </template>
          <template v-else-if="column.key === 'accessible'">
            <a-tag :color="record.accessible ? 'green' : 'red'">{{ record.accessible ? '可访问' : '不可访问' }}</a-tag>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-drawer v-model:open="roleDrawerOpen" :title="roleDrawerTitle" width="620">
      <a-form :model="roleForm" layout="vertical">
        <a-form-item label="角色名称" required>
          <a-input v-model:value="roleForm.roleName" />
        </a-form-item>
        <a-form-item label="角色编码" required>
          <a-input v-model:value="roleForm.roleCode" />
        </a-form-item>
        <a-form-item label="角色描述">
          <a-textarea v-model:value="roleForm.roleDesc" :rows="3" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="roleForm.status" :options="statusOptions" />
        </a-form-item>
        <a-form-item label="页面权限">
          <a-space direction="vertical" style="width: 100%">
            <a-alert
              type="info"
              show-icon
              :message="recommendedPreset ? `推荐预设：${recommendedPreset.label}` : '当前角色暂无推荐预设'"
              :description="recommendedPreset?.description || '可直接按页面树勾选，也可以后续再补充新的组织架构预设。'"
            />
            <a-space wrap>
              <a-button size="small" @click="applyPresetPermissions" :disabled="!recommendedPreset">
                套用推荐预设
              </a-button>
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
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import { createRole, getRolePage, getStaffOptionPage, getStaffRoleAssignments, updateRole, updateStaffRoles } from '../../api/rbac'
import type { PageResult, RoleItem, StaffItem } from '../../types'
import { useUserStore } from '../../stores/user'
import { hasMinisterOrHigher, hasStaffOrHigher, hasSuperRole } from '../../utils/roleAccess'
import {
  ROLE_PAGE_PRESETS,
  getPagePermissionTree,
  getPageTitle,
  getRecommendedPagePermissions,
  getRolePagePreset,
  parseRoutePermissionsJson,
  serializeRoutePermissions
} from '../../utils/pageAccess'
import { resolveRouteAccess } from '../../utils/routeAccess'

const router = useRouter()
const userStore = useUserStore()

const simulateEnabled = ref(false)
const simulateInput = ref('')
const roleRows = ref<RoleItem[]>([])
const roleLoading = ref(false)
const roleSaving = ref(false)
const roleDrawerOpen = ref(false)
const checkedPagePermissions = ref<string[]>([])
const pagePermissionTree = getPagePermissionTree()
const roleForm = reactive<Partial<RoleItem>>({ status: 1 })
const staffOptions = ref<Array<{ label: string; value: number }>>([])
const accountSaving = ref(false)
const accountForm = reactive({ staffId: undefined as number | undefined, roleIds: [] as number[] })

const currentRoles = computed(() => (userStore.roles || []).map((item) => String(item || '').toUpperCase()))
const currentPagePermissions = computed(() => userStore.pagePermissions || [])
const simulatedRoles = computed(() =>
  String(simulateInput.value || '')
    .split(',')
    .map((item) => item.trim().toUpperCase())
    .filter((item) => !!item)
)
const activeRoles = computed(() => (simulateEnabled.value ? simulatedRoles.value : currentRoles.value))
const activePagePermissions = computed(() => {
  if (!simulateEnabled.value) {
    return currentPagePermissions.value
  }
  return Array.from(
    new Set(
      simulatedRoles.value.flatMap((roleCode) => getRecommendedPagePermissions(roleCode))
    )
  )
})

const capability = computed(() => ({
  staff: hasStaffOrHigher(activeRoles.value),
  minister: hasMinisterOrHigher(activeRoles.value),
  super: hasSuperRole(activeRoles.value)
}))

const presetEntries = computed(() =>
  Object.entries(ROLE_PAGE_PRESETS).map(([code, preset]) => ({ code, label: preset.label }))
)
const roleOptions = computed(() =>
  roleRows.value.map((item) => ({
    label: `${item.roleName} (${item.roleCode})`,
    value: item.id
  }))
)
const selectedAccountRoleNames = computed(() =>
  roleRows.value
    .filter((item) => accountForm.roleIds.includes(item.id))
    .map((item) => item.roleName)
)
const roleDrawerTitle = computed(() => (roleForm.id ? '编辑角色页面权限' : '新增角色'))
const recommendedPreset = computed(() => getRolePagePreset(roleForm.roleCode))

const inspectPaths = [
  '/portal',
  '/oa/my-info',
  '/medical-care',
  '/elder',
  '/hr',
  '/oa',
  '/stats',
  '/logistics',
  '/fire',
  '/finance',
  '/system/role',
  '/system/permission-overview',
  '/base-config'
]

const accessRows = computed(() =>
  inspectPaths.map((path) => {
    const access = resolveRouteAccess(router, activeRoles.value, path, activePagePermissions.value)
    const pagePermissionHit = activePagePermissions.value.find(
      (permissionPath) => path === permissionPath || path.startsWith(`${permissionPath}/`)
    )
    return {
      path,
      title: getPageTitle(path),
      requiredRoles: access.requiredRoles,
      pagePermissionLabel: pagePermissionHit ? getPageTitle(pagePermissionHit) : activePagePermissions.value.length ? '未放行' : '未启用',
      accessible: access.canAccess
    }
  })
)

const roleColumns = [
  { title: '角色名称', dataIndex: 'roleName', key: 'roleName', width: 180 },
  { title: '角色编码', dataIndex: 'roleCode', key: 'roleCode', width: 180 },
  { title: '组织预设', key: 'preset', width: 140 },
  { title: '页面权限', key: 'pagePermissions', width: 120 },
  { title: '操作', key: 'action', width: 160 }
]

const accessColumns = [
  { title: '模块', dataIndex: 'title', key: 'title', width: 220 },
  { title: '路径', dataIndex: 'path', key: 'path', width: 240 },
  { title: '需要角色', dataIndex: 'requiredRoles', key: 'requiredRoles', width: 220 },
  { title: '页权限命中', dataIndex: 'pagePermissionLabel', key: 'pagePermission', width: 180 },
  { title: '访问结果', dataIndex: 'accessible', key: 'accessible', width: 120 }
]

const statusOptions = [
  { label: '启用', value: 1 },
  { label: '停用', value: 0 }
]

function filterOption(input: string, option: { label?: string }) {
  return String(option?.label || '').toLowerCase().includes(String(input || '').toLowerCase())
}

function applyPreset(roleCode: string) {
  simulateEnabled.value = true
  simulateInput.value = roleCode
}

function clearSimulation() {
  simulateInput.value = ''
  simulateEnabled.value = false
}

async function fetchRoles() {
  roleLoading.value = true
  try {
    const res: PageResult<RoleItem> = await getRolePage({ pageNo: 1, pageSize: 200 })
    roleRows.value = res.list || []
  } finally {
    roleLoading.value = false
  }
}

async function fetchStaffOptions() {
  const res: PageResult<StaffItem> = await getStaffOptionPage({ pageNo: 1, pageSize: 200, status: 1 })
  staffOptions.value = (res.list || []).map((item) => ({
    label: `${item.realName || item.username}${item.staffNo ? ` / ${item.staffNo}` : ''}`,
    value: Number(item.id)
  }))
}

async function handleStaffChange(staffId?: number) {
  if (!staffId) {
    accountForm.roleIds = []
    return
  }
  const assignments = await getStaffRoleAssignments(staffId)
  accountForm.roleIds = assignments.map((item) => Number(item.roleId)).filter((item) => Number.isFinite(item))
}

async function saveAccountRoles() {
  if (!accountForm.staffId) {
    message.warning('请先选择员工')
    return
  }
  accountSaving.value = true
  try {
    await updateStaffRoles(accountForm.staffId, accountForm.roleIds)
    message.success('账号权限已更新，目标账号重新登录后生效')
  } finally {
    accountSaving.value = false
  }
}

function openRoleDrawer(record?: RoleItem) {
  Object.assign(roleForm, {
    id: record?.id,
    roleName: record?.roleName || '',
    roleCode: record?.roleCode || '',
    roleDesc: record?.roleDesc || '',
    routePermissionsJson: record?.routePermissionsJson || '',
    status: record?.status ?? 1
  })
  checkedPagePermissions.value = parseRoutePermissionsJson(record?.routePermissionsJson)
  roleDrawerOpen.value = true
}

function onPermissionCheck(checkedKeys: string[] | { checked: string[] }) {
  checkedPagePermissions.value = Array.isArray(checkedKeys) ? checkedKeys : checkedKeys.checked || []
}

function applyPresetPermissions() {
  checkedPagePermissions.value = getRecommendedPagePermissions(roleForm.roleCode)
}

async function saveRole() {
  if (!roleForm.roleName || !roleForm.roleCode) {
    message.error('请填写角色名称和角色编码')
    return
  }
  roleSaving.value = true
  try {
    const payload = {
      roleName: roleForm.roleName,
      roleCode: roleForm.roleCode,
      roleDesc: roleForm.roleDesc,
      routePermissionsJson: serializeRoutePermissions(checkedPagePermissions.value),
      status: roleForm.status ?? 1
    }
    if (roleForm.id) {
      await updateRole(Number(roleForm.id), payload)
    } else {
      await createRole(payload)
    }
    message.success('角色页面权限已保存，相关账号重新登录后生效')
    roleDrawerOpen.value = false
    await fetchRoles()
  } finally {
    roleSaving.value = false
  }
}

onMounted(async () => {
  await Promise.all([fetchRoles(), fetchStaffOptions()])
})
</script>

<style scoped>
.role-list {
  margin-top: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.simulate-tip,
.summary-tip,
.permission-summary {
  margin-top: 8px;
  color: rgba(0, 0, 0, 0.45);
  font-size: 12px;
}

.account-action {
  display: flex;
  align-items: flex-end;
}

.selected-role-summary {
  margin-top: 10px;
}
</style>
