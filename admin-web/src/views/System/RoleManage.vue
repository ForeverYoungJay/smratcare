<template>
  <PageContainer title="角色管理" subTitle="维护角色与权限基础信息">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键词">
        <a-input v-model:value="query.keyword" placeholder="角色名称/编码" allow-clear />
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
        <template v-if="column.key === 'status'">
          <a-tag :color="record.status === 1 ? 'green' : 'default'">
            {{ record.status === 1 ? '启用' : '停用' }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'pagePermissions'">
          <a-tag color="blue">
            {{ parseRoutePermissionsJson(record.routePermissionsJson).length || 0 }} 项
          </a-tag>
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

    <a-drawer v-model:open="drawerOpen" :title="drawerTitle" width="520">
      <a-form :model="form" layout="vertical">
        <a-form-item label="角色名称" required>
          <a-input v-model:value="form.roleName" />
        </a-form-item>
        <a-form-item label="角色编码" required>
          <a-input v-model:value="form.roleCode" />
        </a-form-item>
        <a-form-item label="角色描述">
          <a-textarea v-model:value="form.roleDesc" :rows="3" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="form.status" :options="statusOptions" />
        </a-form-item>
        <a-form-item label="页面权限">
          <a-space direction="vertical" style="width: 100%">
            <a-alert
              type="info"
              show-icon
              message="页面权限按角色生效"
              description="可直接套用组织架构预设，也可以按页面树自定义。未配置时仍按原有角色规则访问。"
            />
            <a-space wrap>
              <a-button size="small" @click="applyPresetPermissions" :disabled="!recommendedPreset">
                套用{{ recommendedPreset?.label || '组织架构' }}预设
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
import { getRolePage, createRole, updateRole, deleteRole } from '../../api/rbac'
import type { RoleItem, PageResult } from '../../types'
import {
  getPagePermissionTree,
  getRolePagePreset,
  parseRoutePermissionsJson,
  serializeRoutePermissions
} from '../../utils/pageAccess'

const router = useRouter()
const query = reactive({ keyword: undefined as string | undefined, pageNo: 1, pageSize: 10 })
const rows = ref<RoleItem[]>([])
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
  { title: '角色编码', dataIndex: 'roleCode', key: 'roleCode', width: 180 },
  { title: '页面权限', key: 'pagePermissions', width: 120 },
  { title: '角色描述', dataIndex: 'roleDesc', key: 'roleDesc' },
  { title: '状态', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 120 }
]

const drawerTitle = computed(() => (form.id ? '编辑角色' : '新增角色'))
const recommendedPreset = computed(() => getRolePagePreset(form.roleCode))

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<RoleItem> = await getRolePage(query)
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

function openDrawer(record?: RoleItem) {
  Object.assign(form, {
    id: record?.id,
    roleName: record?.roleName || '',
    roleCode: record?.roleCode || '',
    roleDesc: record?.roleDesc || '',
    routePermissionsJson: record?.routePermissionsJson || '',
    status: record?.status ?? 1
  })
  checkedPagePermissions.value = parseRoutePermissionsJson(record?.routePermissionsJson)
  drawerOpen.value = true
}

function onPermissionCheck(checkedKeys: string[] | { checked: string[] }) {
  checkedPagePermissions.value = Array.isArray(checkedKeys) ? checkedKeys : checkedKeys.checked || []
}

function applyPresetPermissions() {
  if (!recommendedPreset.value) {
    message.warning('当前角色没有预设页面权限，可手动勾选')
    return
  }
  checkedPagePermissions.value = [...recommendedPreset.value.paths]
  message.success(`已套用${recommendedPreset.value.label}预设`)
}

async function submit() {
  if (!form.roleName || !form.roleCode) {
    message.error('请填写角色名称和角色编码')
    return
  }
  saving.value = true
  try {
    const payload = {
      roleName: form.roleName,
      roleCode: form.roleCode,
      roleDesc: form.roleDesc,
      routePermissionsJson: serializeRoutePermissions(checkedPagePermissions.value),
      status: form.status ?? 1
    }
    if (form.id) {
      await updateRole(form.id, payload)
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

fetchData()
</script>

<style scoped>
.permission-summary {
  color: rgba(0, 0, 0, 0.45);
  font-size: 12px;
}
</style>
