<template>
  <PageContainer title="权限总览" subTitle="查看当前账号角色、能力分层与模块访问结果">
    <a-card class="card-elevated" :bordered="false" style="margin-bottom: 12px">
      <template #title>角色模拟器</template>
      <a-space wrap>
        <a-switch v-model:checked="simulateEnabled" />
        <span>{{ simulateEnabled ? '已启用模拟角色' : '使用当前登录角色' }}</span>
      </a-space>
      <a-space wrap style="margin-top: 10px">
        <a-button size="small" @click="applyPreset('MEDICAL_EMPLOYEE')">医疗员工</a-button>
        <a-button size="small" @click="applyPreset('NURSING_MINISTER')">护理部长</a-button>
        <a-button size="small" @click="applyPreset('HR_MINISTER')">HR部长</a-button>
        <a-button size="small" @click="applyPreset('DIRECTOR')">院长/管理层</a-button>
        <a-button size="small" @click="applyPreset('SYS_ADMIN')">SYS_ADMIN</a-button>
        <a-button size="small" danger @click="clearSimulation">清空模拟</a-button>
      </a-space>
      <a-input
        v-model:value="simulateInput"
        style="margin-top: 10px"
        placeholder="输入角色并用英文逗号分隔，例如：MEDICAL_EMPLOYEE,HR_MINISTER"
      />
      <div class="simulate-tip">模拟角色仅用于当前页面预览权限，不会修改账号实际权限。</div>
    </a-card>

    <a-row :gutter="12" style="margin-bottom: 12px">
      <a-col :xs="24" :md="8">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic :title="simulateEnabled ? '模拟角色数量' : '当前角色数量'" :value="activeRoles.length" />
          <div class="role-list">
            <a-tag v-for="role in activeRoles" :key="role" color="blue">{{ role }}</a-tag>
          </div>
        </a-card>
      </a-col>
      <a-col :xs="24" :md="5">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="员工能力" :value="capability.staff ? '是' : '否'" />
        </a-card>
      </a-col>
      <a-col :xs="24" :md="5">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="部长能力" :value="capability.minister ? '是' : '否'" />
        </a-card>
      </a-col>
      <a-col :xs="24" :md="6">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="管理层/超管能力" :value="capability.super ? '是' : '否'" />
        </a-card>
      </a-col>
    </a-row>

    <a-card class="card-elevated" :bordered="false">
      <template #title>核心模块访问检查</template>
      <a-table :columns="columns" :data-source="rows" :pagination="false" row-key="path">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'requiredRoles'">
            <span v-if="record.requiredRoles.length === 0">公开</span>
            <span v-else>{{ record.requiredRoles.join(' / ') }}</span>
          </template>
          <template v-else-if="column.key === 'accessible'">
            <a-tag :color="record.accessible ? 'green' : 'red'">{{ record.accessible ? '可访问' : '不可访问' }}</a-tag>
          </template>
        </template>
      </a-table>
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import PageContainer from '../../components/PageContainer.vue'
import { routes } from '../../router/routes'
import { useUserStore } from '../../stores/user'
import { hasMinisterOrHigher, hasRouteAccess, hasStaffOrHigher, hasSuperRole } from '../../utils/roleAccess'

type FlatRoute = { path: string; title: string; roles: string[] }

const userStore = useUserStore()
const roles = computed(() => (userStore.roles || []).map((item) => String(item || '').toUpperCase()))
const simulateEnabled = ref(false)
const simulateInput = ref('')

const simulatedRoles = computed(() =>
  String(simulateInput.value || '')
    .split(',')
    .map((item) => item.trim().toUpperCase())
    .filter((item) => !!item)
)

const activeRoles = computed(() => (simulateEnabled.value ? simulatedRoles.value : roles.value))

const capability = computed(() => ({
  staff: hasStaffOrHigher(activeRoles.value),
  minister: hasMinisterOrHigher(activeRoles.value),
  super: hasSuperRole(activeRoles.value)
}))

function applyPreset(role: string) {
  simulateEnabled.value = true
  simulateInput.value = role
}

function clearSimulation() {
  simulateInput.value = ''
  simulateEnabled.value = false
}

function flattenRoutes(routeList: any[], basePath = ''): FlatRoute[] {
  const result: FlatRoute[] = []
  ;(routeList || []).forEach((route) => {
    const fullPath = route.path?.startsWith('/')
      ? route.path
      : `${basePath}/${route.path || ''}`.replace(/\/+/g, '/')
    if (!route.meta?.hidden && route.meta?.title) {
      result.push({
        path: fullPath,
        title: String(route.meta.title),
        roles: Array.isArray(route.meta.roles) ? route.meta.roles : []
      })
    }
    if (Array.isArray(route.children) && route.children.length > 0) {
      result.push(...flattenRoutes(route.children, fullPath))
    }
  })
  return result
}

const flattened = computed(() => {
  const root = routes.find((item: any) => item.path === '/')
  return flattenRoutes(root?.children || [], '')
})

const inspectPaths = [
  '/elder',
  '/marketing',
  '/logistics',
  '/care',
  '/finance',
  '/medical-care',
  '/assessment',
  '/hr',
  '/system',
  '/elder/status-change/medical-outing',
  '/elder/status-change/death-register',
  '/assessment/template',
  '/system/role'
]

const rows = computed(() => {
  const map = new Map(flattened.value.map((item) => [item.path, item]))
  return inspectPaths.map((path) => {
    const route = map.get(path)
    const requiredRoles = route?.roles || []
    return {
      path,
      title: route?.title || '(未找到路由定义)',
      requiredRoles,
      accessible: hasRouteAccess(activeRoles.value, requiredRoles, path)
    }
  })
})

const columns = [
  { title: '模块', dataIndex: 'title', key: 'title', width: 220 },
  { title: '路径', dataIndex: 'path', key: 'path', width: 320 },
  { title: '需要角色', dataIndex: 'requiredRoles', key: 'requiredRoles' },
  { title: '访问结果', dataIndex: 'accessible', key: 'accessible', width: 120 }
]
</script>

<style scoped>
.role-list {
  margin-top: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.simulate-tip {
  margin-top: 8px;
  color: rgba(0, 0, 0, 0.45);
  font-size: 12px;
}
</style>
