<template>
  <a-layout class="app-layout">
    <a-layout-sider v-model:collapsed="collapsed" collapsible class="app-sider">
      <div class="brand">
        <div class="logo">智</div>
        <div class="brand-text" v-if="!collapsed">
          <div class="title">智养云</div>
          <div class="subtitle">智慧养老 OA</div>
        </div>
      </div>
      <a-menu
        theme="dark"
        mode="inline"
        :selectedKeys="selectedKeys"
        :openKeys="openKeys"
        @openChange="onOpenChange"
        :items="menuItems"
        @click="onMenuClick"
        class="side-menu"
      />
    </a-layout-sider>

    <a-layout class="app-main">
      <a-layout-header class="app-header">
        <div class="header-left">
          <div class="page-title">{{ currentTitle || '工作台' }}</div>
          <a-breadcrumb class="breadcrumb">
            <a-breadcrumb-item v-for="(bc, idx) in breadcrumbs" :key="idx">
              {{ bc }}
            </a-breadcrumb-item>
          </a-breadcrumb>
        </div>
        <div class="header-right">
          <span class="today-label">{{ todayLabel }}</span>
          <a-badge status="processing" text="系统运行中" class="system-status" />
          <span class="system-name">龟峰颐养中心智慧养老管理平台</span>
          <a-dropdown trigger="click">
            <a-tag :color="presenceStatus.color" class="presence-tag">{{ presenceStatus.label }}</a-tag>
            <template #overlay>
              <a-menu>
                <a-menu-item @click="setPresenceTraining(2)">外出培训 2 小时</a-menu-item>
                <a-menu-item @click="setPresenceTraining(4)">外出培训 4 小时</a-menu-item>
                <a-menu-item @click="clearPresenceTraining">结束培训状态</a-menu-item>
                <a-menu-item @click="toggleDnd">{{ quickChatDnd ? '关闭免打扰' : '开启免打扰' }}</a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
          <a-dropdown trigger="click">
            <a-badge :count="quickNotifyItems.length" size="small">
              <a-button size="small">快捷通知</a-button>
            </a-badge>
            <template #overlay>
              <a-menu>
                <a-menu-item v-for="item in quickNotifyItems" :key="item.title" @click="router.push(item.route)">
                  {{ item.title }}
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
          <a-badge :count="quickChatUnreadCount" size="small">
            <a-button size="small" @click="openQuickChat">快捷聊天</a-button>
          </a-badge>
          <a-dropdown>
            <a class="user-link user-entry">
              <a-avatar :size="28" :src="headerSettings.avatarUrl || undefined">
                {{ (userStore.staffInfo?.realName || '管').slice(0, 1) }}
              </a-avatar>
              <span>{{ displayUserName }}</span>
            </a>
            <template #overlay>
              <a-menu>
                <a-menu-item @click="openHeaderSettings">个人设置</a-menu-item>
                <a-menu-item @click="router.push('/oa/staff')">账号与赋权</a-menu-item>
                <a-menu-item @click="logout">退出</a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
      </a-layout-header>

      <div ref="routeTabsWrapRef" class="route-tabs-wrap">
        <a-tabs
          v-model:activeKey="activeTabKey"
          type="editable-card"
          hide-add
          size="small"
          @change="onTabChange"
          @edit="onTabEdit"
        >
          <a-tab-pane
            v-for="tab in routeTabs"
            :key="tab.key"
            :closable="tab.closable"
          >
            <template #tab>
              <span
                class="route-tab-title"
                :class="{
                  'route-tab-dragging': tabDrag.dragKey === tab.key,
                  'route-tab-over': tabDrag.overKey === tab.key && tabDrag.dragKey !== tab.key
                }"
                draggable="true"
                @dragstart="onTabDragStart(tab.key)"
                @dragover.prevent="onTabDragOver($event, tab.key)"
                @drop.prevent="onTabDrop(tab.key)"
                @dragend="onTabDragEnd"
                @contextmenu.prevent="openTabContextMenu($event, tab.key)"
              >
                {{ tab.title }}
              </span>
            </template>
          </a-tab-pane>
        </a-tabs>
        <a-dropdown trigger="click">
          <a-button size="small" class="tab-tools-btn">标签操作</a-button>
          <template #overlay>
            <a-menu>
              <a-menu-item @click="refreshCurrentTab">刷新当前</a-menu-item>
              <a-menu-item @click="closeOtherTabs">关闭其他</a-menu-item>
              <a-menu-item @click="closeAllTabs">关闭全部</a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
      </div>

      <a-layout-content class="app-content">
        <router-view v-slot="{ Component }">
          <component :is="Component" :key="viewRenderKey" />
        </router-view>
      </a-layout-content>
    </a-layout>
  </a-layout>

  <div
    v-if="tabContext.visible"
    class="tab-context-menu"
    :style="{ left: `${tabContext.x}px`, top: `${tabContext.y}px` }"
    @click.stop
  >
    <a-menu>
      <a-menu-item @click="closeContextCurrent">关闭当前</a-menu-item>
      <a-menu-item @click="closeOtherTabs(tabContext.key)">关闭其他</a-menu-item>
      <a-menu-item @click="closeLeftTabs">关闭左侧</a-menu-item>
      <a-menu-item @click="closeRightTabs">关闭右侧</a-menu-item>
      <a-menu-item @click="refreshCurrentTab">刷新当前</a-menu-item>
    </a-menu>
  </div>

  <a-drawer
    v-model:open="headerSettingsOpen"
    title="个人与系统设置"
    width="420"
    placement="right"
  >
    <a-form layout="vertical">
      <a-form-item label="头像">
        <a-space>
          <a-avatar :size="56" :src="headerSettings.avatarUrl || undefined">
            {{ (headerSettings.realName || '管').slice(0, 1) }}
          </a-avatar>
          <a-upload :show-upload-list="false" :before-upload="beforeAvatarUpload">
            <a-button>上传头像</a-button>
          </a-upload>
        </a-space>
      </a-form-item>
      <a-form-item label="真实姓名">
        <a-input v-model:value="headerSettings.realName" placeholder="请输入真实姓名" />
      </a-form-item>
      <a-form-item label="绑定手机号">
        <a-input v-model:value="headerSettings.phone" placeholder="请输入手机号" />
      </a-form-item>
      <a-divider orientation="left">组织与监管</a-divider>
      <a-form-item label="登录ID（院内职工号）">
        <a-input :value="headerSettings.staffNo || userStore.staffInfo?.username || '-'" disabled />
      </a-form-item>
      <a-form-item label="当前隶属部门">
        <a-input :value="headerSettings.departmentName || '-'" disabled />
      </a-form-item>
      <a-form-item label="直接领导（一级监管）">
        <div class="settings-tip">员工账号仅可选本部门部长；部长账号仅可选院长/SYS_ADMIN。</div>
        <a-select
          v-model:value="headerSettings.directLeaderId"
          show-search
          allow-clear
          :filter-option="false"
          :loading="staffLoading"
          :options="directLeaderOptions"
          placeholder="请输入姓名/拼音首字母检索"
          @search="searchLeaderOptions"
          @change="onDirectLeaderChange"
        />
      </a-form-item>
      <a-form-item label="间接领导（二级监管）">
        <div class="settings-tip">二级监管仅可选院长/DIRECTOR/SYS_ADMIN。</div>
        <a-select
          v-model:value="headerSettings.indirectLeaderId"
          show-search
          allow-clear
          :filter-option="false"
          :loading="staffLoading"
          :options="indirectLeaderOptions"
          placeholder="请输入姓名/拼音首字母检索"
          @search="searchLeaderOptions"
          @change="onIndirectLeaderChange"
        />
      </a-form-item>
      <a-form-item label="系统主色">
        <a-select v-model:value="headerSettings.themeColor" :options="themeColorOptions" @change="applyAppearance" />
      </a-form-item>
      <a-form-item label="字体大小">
        <a-slider v-model:value="headerSettings.fontScale" :min="90" :max="115" :step="5" @change="applyAppearance" />
      </a-form-item>
      <a-form-item label="快捷通知栏">
        <a-switch v-model:checked="headerSettings.quickNotifyEnabled" checked-children="开启" un-checked-children="关闭" />
      </a-form-item>
      <a-divider orientation="left">账户安全</a-divider>
      <a-form-item label="新密码">
        <a-input-password v-model:value="passwordForm.password" placeholder="请输入新密码（至少6位）" />
      </a-form-item>
      <a-form-item label="确认密码">
        <a-input-password v-model:value="passwordForm.confirmPassword" placeholder="请再次输入新密码" />
      </a-form-item>
      <a-space>
        <a-button type="primary" :loading="savingHeaderSettings" @click="saveHeaderSettings">保存设置</a-button>
        <a-button @click="resetHeaderSettings">恢复默认</a-button>
      </a-space>
      <div class="settings-tip">
        院长赋权注册账号：请前往“账号与赋权”页面进行员工账号开通、角色分配与权限管理。
      </div>
    </a-form>
  </a-drawer>

  <a-drawer
    v-model:open="quickChatOpen"
    title="快捷聊天"
    width="860"
    placement="right"
  >
    <a-space style="margin-bottom: 10px;">
      <a-button type="primary" @click="openCreateQuickChatRoom">发起群聊申请</a-button>
      <a-button :disabled="!activeQuickChatRoom" @click="openInviteQuickChat">邀请部门/成员</a-button>
      <a-tag :color="presenceStatus.color">{{ presenceStatus.label }}</a-tag>
      <a-tag v-if="quickChatDnd" color="red">免打扰已开启</a-tag>
    </a-space>
    <div class="quick-chat-layout">
      <div class="quick-chat-room-list">
        <a-list :data-source="quickChatRooms" size="small" :locale="{ emptyText: '暂无会话，先发起一个群聊' }">
          <template #renderItem="{ item }">
            <a-list-item
              class="quick-chat-room-item"
              :class="{ active: item.id === activeQuickChatRoomId }"
              @click="selectQuickChatRoom(item.id)"
            >
              <a-list-item-meta :description="quickChatLastMessageText(item)">
                <template #title>
                  <a-space size="small">
                    <span>{{ item.name }}</span>
                    <a-tag v-if="item.unreadCount" color="red">{{ item.unreadCount }}</a-tag>
                  </a-space>
                </template>
              </a-list-item-meta>
            </a-list-item>
          </template>
        </a-list>
      </div>
      <div class="quick-chat-main">
        <div class="quick-chat-header">
          <strong>{{ activeQuickChatRoom?.name || '请选择会话' }}</strong>
          <a-space size="small">
            <span class="hint-text">{{ activeQuickChatRoom ? `成员 ${activeQuickChatRoom.memberIds.length} 人` : '' }}</span>
            <a-button size="small" :disabled="!activeQuickChatRoom" @click="markActiveRoomRead">全部已读</a-button>
            <a-button size="small" danger :disabled="!activeQuickChatRoom" @click="removeActiveQuickChatRoom">删除会话</a-button>
          </a-space>
        </div>
        <div class="quick-chat-messages">
          <a-empty v-if="!activeQuickChatMessages.length" description="暂无消息，开始聊天吧" />
          <div v-for="msg in activeQuickChatMessages" :key="msg.id" class="quick-chat-message" :class="{ self: msg.senderId === currentQuickChatSenderId }">
            <div class="quick-chat-message-meta">
              <span>{{ msg.senderName }}</span>
              <span>{{ msg.timeText }}</span>
            </div>
            <div class="quick-chat-message-body">
              <template v-if="msg.kind === 'IMAGE' && msg.fileUrl">
                <img :src="msg.fileUrl" :alt="msg.fileName || '图片'" class="quick-chat-image" />
              </template>
              <template v-else-if="msg.kind === 'FILE'">
                <a :href="msg.fileUrl || '#'" target="_blank" rel="noreferrer">{{ msg.fileName || '附件' }}</a>
              </template>
              <template v-else>
                {{ msg.content }}
              </template>
            </div>
          </div>
        </div>
        <div class="quick-chat-input">
          <a-textarea v-model:value="quickChatDraft" :rows="3" :disabled="!activeQuickChatRoom" placeholder="输入消息..." />
          <a-space style="margin-top: 8px;">
            <a-upload :show-upload-list="false" :before-upload="beforeQuickChatUpload">
              <a-button :disabled="!activeQuickChatRoom">发送图片/文档</a-button>
            </a-upload>
            <a-button type="primary" :disabled="!activeQuickChatRoom" @click="sendQuickChatText">发送</a-button>
          </a-space>
        </div>
      </div>
    </div>
  </a-drawer>

  <a-modal
    v-model:open="quickChatRoomEditorOpen"
    :title="quickChatRoomEditorMode === 'create' ? '发起快捷群聊申请' : '邀请部门/成员'"
    width="620"
    @ok="submitQuickChatRoomEditor"
  >
    <a-form layout="vertical">
      <a-form-item label="群聊名称" required>
        <a-input v-model:value="quickChatRoomForm.name" placeholder="例如：行政部-护理部协同群" />
      </a-form-item>
      <a-form-item label="邀请部门（可多选）">
        <a-select
          v-model:value="quickChatRoomForm.departmentIds"
          mode="multiple"
          allow-clear
          show-search
          :filter-option="false"
          :options="quickChatDepartmentOptions"
          placeholder="输入部门名或拼音首字母"
          @search="searchDepartments"
          @focus="() => !departmentOptions.length && searchDepartments('')"
        />
      </a-form-item>
      <a-form-item label="邀请成员（可多选）">
        <a-select
          v-model:value="quickChatRoomForm.memberIds"
          mode="multiple"
          allow-clear
          show-search
          :filter-option="false"
          :options="quickChatStaffOptions"
          placeholder="输入姓名/工号/拼音首字母"
          @search="searchStaff"
          @focus="() => !staffOptions.length && searchStaff('')"
        />
      </a-form-item>
      <a-form-item label="申请说明">
        <a-textarea v-model:value="quickChatRoomForm.applyRemark" :rows="2" placeholder="例如：用于跨部门审批沟通与资料传输" />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import type { UploadProps } from 'ant-design-vue'
import dayjs from 'dayjs'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { getMenuTree } from './menu'
import { getMe } from '../api/auth'
import { getStaffPage } from '../api/rbac'
import { updateStaff } from '../api/staff'
import { useDepartmentOptions } from '../composables/useDepartmentOptions'
import { useStaffOptions } from '../composables/useStaffOptions'
import { canBeDirectLeader, canBeIndirectLeader, ensureSupervisorOrder } from '../utils/supervisor'
import { emitLiveSync, subscribeLiveSync, type LiveSyncPayload } from '../utils/liveSync'

const collapsed = ref(false)
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const openKeys = ref<string[]>([])
const routeTabsWrapRef = ref<HTMLElement | null>(null)
const routeTabs = ref<Array<{ key: string; path: string; title: string; closable: boolean }>>([])
const activeTabKey = ref('')
const refreshSeed = ref(0)
const tabContext = reactive({
  visible: false,
  x: 0,
  y: 0,
  key: ''
})
const tabDrag = reactive({
  dragKey: '',
  overKey: ''
})
const headerSettingsOpen = ref(false)
const savingHeaderSettings = ref(false)
const headerSettings = reactive({
  avatarUrl: '',
  realName: '',
  phone: '',
  staffNo: '',
  departmentName: '',
  directLeaderId: undefined as string | undefined,
  directLeaderName: '',
  indirectLeaderId: undefined as string | undefined,
  indirectLeaderName: '',
  themeColor: '#1b66d6',
  fontScale: 100,
  quickNotifyEnabled: true
})
const passwordForm = reactive({
  password: '',
  confirmPassword: ''
})
const themeColorOptions = [
  { label: '海蓝', value: '#1b66d6' },
  { label: '翠绿', value: '#22a06b' },
  { label: '暖橙', value: '#f59e0b' },
  { label: '典雅紫', value: '#7c3aed' },
  { label: '深青', value: '#0f766e' }
]
const quickNotifyItems = computed(() => {
  if (!headerSettings.quickNotifyEnabled) return []
  return [
    { title: '我的待办', route: '/oa/todo' },
    { title: '待我审批', route: '/oa/approval?scope=PENDING_REVIEW' },
    { title: '行政日历/协同日历', route: '/oa/work-execution/calendar' },
    { title: '会员生日', route: '/oa/life/birthday' }
  ]
})
const { departmentOptions, searchDepartments } = useDepartmentOptions({ pageSize: 200, preloadSize: 600 })
const { staffOptions, staffLoading, searchStaff, ensureSelectedStaff } = useStaffOptions({ pageSize: 120, preloadSize: 500 })
const leaderOptions = computed(() => {
  const selfId = String(userStore.staffInfo?.id || '')
  return staffOptions.value
    .filter((item) => item.value !== selfId)
    .map((item) => ({ label: item.label, value: item.value }))
})
const directLeaderOptions = computed(() => {
  const target = {
    id: userStore.staffInfo?.id,
    departmentId: userStore.staffInfo?.departmentId,
    roleCodes: userStore.roles || []
  }
  return staffOptions.value
    .filter((item) => item.value !== String(userStore.staffInfo?.id || ''))
    .filter((item) => canBeDirectLeader(target, { id: item.value, departmentId: item.departmentId, roleCodes: item.roleCodes || [] }))
    .map((item) => ({ label: item.label, value: item.value }))
})
const indirectLeaderOptions = computed(() => {
  const target = {
    id: userStore.staffInfo?.id,
    departmentId: userStore.staffInfo?.departmentId,
    roleCodes: userStore.roles || []
  }
  return staffOptions.value
    .filter((item) => item.value !== String(userStore.staffInfo?.id || ''))
    .filter((item) => canBeIndirectLeader(target, { id: item.value, departmentId: item.departmentId, roleCodes: item.roleCodes || [] }))
    .map((item) => ({ label: item.label, value: item.value }))
})
const displayUserName = computed(() => {
  const realName = String(userStore.staffInfo?.realName || headerSettings.realName || '').trim()
  const loginId = String(headerSettings.staffNo || userStore.staffInfo?.username || '').trim()
  if (realName && loginId) return `${realName}（${loginId}）`
  return realName || loginId || '管理员'
})

type QuickChatMessage = {
  id: string
  senderId: string
  senderName: string
  kind: 'TEXT' | 'IMAGE' | 'FILE' | 'SYSTEM'
  content: string
  fileName?: string
  fileUrl?: string
  timeText: string
}

type QuickChatRoom = {
  id: string
  name: string
  departmentIds: string[]
  memberIds: string[]
  messages: QuickChatMessage[]
  unreadCount: number
  unreadByUser?: Record<string, number>
  createdAt: string
}

const quickChatOpen = ref(false)
const quickChatRooms = ref<QuickChatRoom[]>([])
const activeQuickChatRoomId = ref('')
const quickChatDraft = ref('')
const quickChatRoomEditorOpen = ref(false)
const quickChatRoomEditorMode = ref<'create' | 'invite'>('create')
const quickChatDnd = ref(false)
const quickChatTrainingUntil = ref('')
const quickChatRoomForm = reactive({
  name: '',
  departmentIds: [] as string[],
  memberIds: [] as string[],
  applyRemark: ''
})
const currentQuickChatSenderId = computed(() => String(userStore.staffInfo?.id || userStore.staffInfo?.username || 'me'))
const currentQuickChatSenderName = computed(() => String(userStore.staffInfo?.realName || userStore.staffInfo?.username || '我'))
const activeQuickChatRoom = computed(() => quickChatRooms.value.find((item) => item.id === activeQuickChatRoomId.value) || null)
const activeQuickChatMessages = computed(() => activeQuickChatRoom.value?.messages || [])
const quickChatUnreadCount = computed(() => quickChatRooms.value.reduce((sum, item) => sum + Number(item.unreadCount || 0), 0))
const quickChatDepartmentOptions = computed(() => departmentOptions.value.map((item) => ({ label: item.label, value: item.value })))
const quickChatStaffOptions = computed(() =>
  staffOptions.value
    .filter((item) => {
      if (!quickChatRoomForm.departmentIds.length) return true
      return item.departmentId ? quickChatRoomForm.departmentIds.includes(String(item.departmentId)) : false
    })
    .map((item) => ({ label: item.label, value: item.value }))
)
const presenceStatus = computed(() => {
  void presenceTick.value
  if (quickChatDnd.value) return { code: 'DND', label: '免打扰', color: 'red' }
  if (quickChatTrainingUntil.value && dayjs().isBefore(dayjs(quickChatTrainingUntil.value))) {
    return { code: 'TRAINING', label: '外出培训', color: 'purple' }
  }
  const hour = Number(dayjs().format('H'))
  if (hour >= 12 && hour < 14) return { code: 'LUNCH', label: '午休', color: 'blue' }
  if (hour >= 9 && hour < 18) return { code: 'WORK', label: '上班', color: 'green' }
  return { code: 'OFF', label: '下班', color: 'default' }
})
const presenceTick = ref(Date.now())
let presenceTimer: number | undefined
let quickChatSyncUnsubscribe = () => {}

const filteredMenu = computed(() => {
  const roles = userStore.roles || []
  return getMenuTree(roles)
})

const menuItems = computed(() => {
  const map = (items: any[]): any[] =>
    items.map((item) => ({
      key: item.path || item.key,
      label: item.label,
      children: item.children ? map(item.children) : undefined
    }))
  return map(filteredMenu.value)
})

const selectedKeys = computed(() => [route.path])
const viewRenderKey = computed(() => `${route.fullPath}::${refreshSeed.value}`)

const currentTitle = computed(() => {
  const title = route.meta?.title as string | undefined
  return title || ''
})

const breadcrumbs = computed(() => {
  const titles = route.matched
    .map((r) => r.meta?.title as string | undefined)
    .filter((t) => t && t.length > 0) as string[]
  return ['首页', ...titles]
})

const todayLabel = computed(() => {
  const now = new Date()
  return now.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit', weekday: 'short' })
})

watch(
  () => route.fullPath,
  (fullPath) => {
    syncRouteTab(route.path, fullPath)
    const path = route.path
    const trail = findMenuTrail(path)
    if (trail.length > 1) {
      openKeys.value = trail.slice(0, trail.length - 1).map((t) => t.key)
    } else {
      openKeys.value = []
    }
  },
  { immediate: true }
)

watch(
  () => userStore.staffInfo?.departmentId,
  () => {
    syncDepartmentName()
  }
)

function currentTabTitle() {
  return String(route.meta?.title || route.name || '未命名页面')
}

function normalizeTabKey(pathLike: string) {
  const text = String(pathLike || '').trim()
  if (!text) return ''
  const [purePath] = text.split('?')
  return purePath || text
}

function syncRouteTab(pathKey: string, fullPath: string) {
  if (!fullPath || fullPath.startsWith('/login') || fullPath.startsWith('/403')) return
  const key = normalizeTabKey(pathKey || fullPath)
  const found = routeTabs.value.find((item) => item.key === key)
  if (found) {
    found.title = currentTabTitle()
    found.path = fullPath
    activeTabKey.value = key
    persistRouteTabs()
    return
  }
  const isHomeTab = key === '/portal'
  routeTabs.value.push({
    key,
    path: fullPath,
    title: currentTabTitle(),
    closable: !isHomeTab
  })
  activeTabKey.value = key
  persistRouteTabs()
}

function onTabChange(key: string) {
  if (!key) return
  const tab = routeTabs.value.find((item) => item.key === key)
  const target = tab?.path || key
  if (target === route.fullPath) return
  router.push(target)
}

function onTabEdit(targetKey: string | MouseEvent, action: 'add' | 'remove') {
  if (action !== 'remove') return
  closeTab(String(targetKey || ''))
}

function closeTab(targetKey: string) {
  const idx = routeTabs.value.findIndex((item) => item.key === targetKey)
  if (idx < 0) return
  const tab = routeTabs.value[idx]
  if (!tab.closable) return
  routeTabs.value.splice(idx, 1)
  persistRouteTabs()
  if (activeTabKey.value !== targetKey) return
  const fallback = routeTabs.value[idx] || routeTabs.value[idx - 1]
  router.push(fallback?.key || '/portal')
}

function onTabDragStart(key: string) {
  tabDrag.dragKey = key
  tabDrag.overKey = key
}

function onTabDragOver(event: DragEvent, key: string) {
  if (!tabDrag.dragKey) return
  tabDrag.overKey = key
  autoScrollTabNav(event.clientX)
}

function onTabDrop(key: string) {
  const from = routeTabs.value.findIndex((item) => item.key === tabDrag.dragKey)
  const to = routeTabs.value.findIndex((item) => item.key === key)
  if (from < 0 || to < 0 || from === to) {
    onTabDragEnd()
    return
  }
  const moved = routeTabs.value.splice(from, 1)[0]
  routeTabs.value.splice(to, 0, moved)
  persistRouteTabs()
  onTabDragEnd()
}

function onTabDragEnd() {
  tabDrag.dragKey = ''
  tabDrag.overKey = ''
}

function tabNavWrapElement() {
  return routeTabsWrapRef.value?.querySelector('.ant-tabs-nav-wrap') as HTMLElement | null
}

function autoScrollTabNav(clientX: number) {
  const navWrap = tabNavWrapElement()
  if (!navWrap) return
  const rect = navWrap.getBoundingClientRect()
  const edge = 56
  const step = 28
  if (clientX < rect.left + edge) {
    navWrap.scrollLeft -= step
  } else if (clientX > rect.right - edge) {
    navWrap.scrollLeft += step
  }
}

function closeOtherTabs(keepKey?: string) {
  const keep = keepKey || activeTabKey.value || route.fullPath
  routeTabs.value = routeTabs.value.filter((item) => !item.closable || item.key === keep)
  persistRouteTabs()
  closeTabContextMenu()
}

function closeAllTabs() {
  routeTabs.value = routeTabs.value.filter((item) => !item.closable)
  persistRouteTabs()
  closeTabContextMenu()
  router.push('/portal')
}

function refreshCurrentTab() {
  refreshSeed.value += 1
  closeTabContextMenu()
}

function openTabContextMenu(event: MouseEvent, key: string) {
  tabContext.visible = true
  tabContext.x = event.clientX
  tabContext.y = event.clientY
  tabContext.key = key
}

function closeTabContextMenu() {
  tabContext.visible = false
  tabContext.key = ''
}

function closeContextCurrent() {
  closeTab(tabContext.key)
  closeTabContextMenu()
}

function closeLeftTabs() {
  const idx = routeTabs.value.findIndex((item) => item.key === tabContext.key)
  if (idx <= 0) {
    closeTabContextMenu()
    return
  }
  routeTabs.value = routeTabs.value.filter((item, i) => i >= idx || !item.closable)
  persistRouteTabs()
  ensureActiveTabAvailable()
  closeTabContextMenu()
}

function closeRightTabs() {
  const idx = routeTabs.value.findIndex((item) => item.key === tabContext.key)
  if (idx < 0 || idx >= routeTabs.value.length - 1) {
    closeTabContextMenu()
    return
  }
  routeTabs.value = routeTabs.value.filter((item, i) => i <= idx || !item.closable)
  persistRouteTabs()
  ensureActiveTabAvailable()
  closeTabContextMenu()
}

function ensureActiveTabAvailable() {
  const active = activeTabKey.value || route.fullPath
  if (routeTabs.value.some((item) => item.key === active)) return
  const fallback = routeTabs.value[routeTabs.value.length - 1]
  router.push(fallback?.key || '/portal')
}

function routeTabsStorageKey() {
  return `layout_route_tabs_v1_${userStorageScope()}`
}

function persistRouteTabs() {
  try {
    const payload = routeTabs.value
      .slice(-20)
      .map((item) => ({ key: item.key, path: item.path, title: item.title, closable: item.closable }))
    localStorage.setItem(routeTabsStorageKey(), JSON.stringify(payload))
  } catch {}
}

function restoreRouteTabs() {
  try {
    const raw = localStorage.getItem(routeTabsStorageKey())
    if (!raw) return
    const parsed = JSON.parse(raw)
    if (!Array.isArray(parsed)) return
    routeTabs.value = parsed
      .map((item: any) => ({
        key: normalizeTabKey(String(item?.path || item?.key || '')),
        path: String(item?.path || item?.key || ''),
        title: String(item?.title || '未命名页面'),
        closable: normalizeTabKey(String(item?.path || item?.key || '')) !== '/portal'
      }))
      .filter((item: any) => !!item.key && !!item.path)
      .reduce((acc: Array<{ key: string; path: string; title: string; closable: boolean }>, item: any) => {
        if (acc.some((x) => x.key === item.key)) return acc
        acc.push(item)
        return acc
      }, [])
      .slice(-20)
  } catch {}
}

function onOpenChange(keys: string[]) {
  const previousKeys = [...openKeys.value]
  openKeys.value = keys
  const addedKey = keys.find((key) => !previousKeys.includes(key))
  if (!addedKey) return
  const isTopLevel = filteredMenu.value.some((item) => item.key === addedKey)
  if (!isTopLevel) return
  const targetPath = resolveMenuPathByKey(addedKey, filteredMenu.value)
  if (!targetPath) return
  if (normalizeTabKey(route.path) === normalizeTabKey(targetPath)) return
  router.push(targetPath)
}

function onMenuClick(info: any) {
  if (typeof info.key === 'string' && info.key.length > 0) {
    const targetPath = resolveMenuPathByKey(info.key, filteredMenu.value)
    const target = targetPath || info.key
    if (normalizeTabKey(route.path) === normalizeTabKey(target)) return
    router.push(target)
  }
}

function findMenuTrail(path: string) {
  const walk = (items: any[], trail: any[] = []): any[] => {
    for (const item of items) {
      if (item.path === path) return [...trail, item]
      if (item.children) {
        const found = walk(item.children, [...trail, item])
        if (found.length) return found
      }
    }
    return []
  }
  return walk(filteredMenu.value)
}

function resolveMenuPathByKey(key: string, items: any[]): string | null {
  for (const item of items || []) {
    if (item.key === key) {
      return item.path || firstLeafPath(item.children || [])
    }
    if (item.children && item.children.length > 0) {
      const found = resolveMenuPathByKey(key, item.children)
      if (found) return found
    }
  }
  return null
}

function firstLeafPath(items: any[]): string | null {
  for (const item of items || []) {
    if (item.path) return item.path
    if (item.children && item.children.length > 0) {
      const found = firstLeafPath(item.children)
      if (found) return found
    }
  }
  return null
}

function logout() {
  userStore.clear()
  router.push('/login')
}

function userStorageScope() {
  if (userStore.staffInfo?.id) return `id_${String(userStore.staffInfo.id)}`
  if (userStore.staffInfo?.username) return `u_${String(userStore.staffInfo.username)}`
  if (userStore.token) return `t_${String(userStore.token).slice(-12)}`
  return 'default'
}

function headerSettingsStorageKey() {
  return `portal_header_settings_v1_${userStorageScope()}`
}

function applyAppearance() {
  document.documentElement.style.setProperty('--primary', headerSettings.themeColor || '#1b66d6')
  document.documentElement.style.fontSize = `${Number(headerSettings.fontScale || 100)}%`
}

function syncDepartmentName() {
  const departmentId = String(userStore.staffInfo?.departmentId || '')
  if (!departmentId) {
    headerSettings.departmentName = ''
    return
  }
  const department = departmentOptions.value.find((item) => item.value === departmentId)
  headerSettings.departmentName = department?.name || `部门#${departmentId}`
}

function syncHeaderIdentityFromStore() {
  headerSettings.realName = userStore.staffInfo?.realName || headerSettings.realName
  headerSettings.phone = userStore.staffInfo?.phone || headerSettings.phone
  headerSettings.staffNo = String(userStore.staffInfo?.staffNo || headerSettings.staffNo || '')
  headerSettings.directLeaderId = userStore.staffInfo?.directLeaderId == null
    ? undefined
    : String(userStore.staffInfo.directLeaderId)
  headerSettings.indirectLeaderId = userStore.staffInfo?.indirectLeaderId == null
    ? undefined
    : String(userStore.staffInfo.indirectLeaderId)
}

function loadHeaderSettings() {
  syncHeaderIdentityFromStore()
  syncDepartmentName()
  try {
    const raw = localStorage.getItem(headerSettingsStorageKey())
    if (!raw) {
      applyAppearance()
      return
    }
    const parsed = JSON.parse(raw)
    headerSettings.avatarUrl = String(parsed?.avatarUrl || '')
    headerSettings.themeColor = String(parsed?.themeColor || '#1b66d6')
    headerSettings.fontScale = Number(parsed?.fontScale || 100)
    headerSettings.quickNotifyEnabled = parsed?.quickNotifyEnabled !== false
    headerSettings.staffNo = String(parsed?.staffNo || '')
    headerSettings.directLeaderId = parsed?.directLeaderId ? String(parsed.directLeaderId) : undefined
    headerSettings.directLeaderName = String(parsed?.directLeaderName || '')
    headerSettings.indirectLeaderId = parsed?.indirectLeaderId ? String(parsed.indirectLeaderId) : undefined
    headerSettings.indirectLeaderName = String(parsed?.indirectLeaderName || '')
    if (parsed?.realName) headerSettings.realName = String(parsed.realName)
    if (parsed?.phone) headerSettings.phone = String(parsed.phone)
    if (parsed?.departmentName) {
      headerSettings.departmentName = String(parsed.departmentName)
    }
    if (headerSettings.directLeaderId) {
      ensureSelectedStaff(headerSettings.directLeaderId, headerSettings.directLeaderName)
    }
    if (headerSettings.indirectLeaderId) {
      ensureSelectedStaff(headerSettings.indirectLeaderId, headerSettings.indirectLeaderName)
    }
    applyAppearance()
  } catch {
    applyAppearance()
  }
}

function persistHeaderSettings() {
  const payload = {
    avatarUrl: headerSettings.avatarUrl || '',
    realName: headerSettings.realName || '',
    phone: headerSettings.phone || '',
    staffNo: headerSettings.staffNo || '',
    departmentName: headerSettings.departmentName || '',
    directLeaderId: headerSettings.directLeaderId || '',
    directLeaderName: headerSettings.directLeaderName || '',
    indirectLeaderId: headerSettings.indirectLeaderId || '',
    indirectLeaderName: headerSettings.indirectLeaderName || '',
    themeColor: headerSettings.themeColor || '#1b66d6',
    fontScale: Number(headerSettings.fontScale || 100),
    quickNotifyEnabled: headerSettings.quickNotifyEnabled !== false
  }
  localStorage.setItem(headerSettingsStorageKey(), JSON.stringify(payload))
}

function openHeaderSettings() {
  syncHeaderIdentityFromStore()
  syncDepartmentName()
  if (!departmentOptions.value.length) {
    searchDepartments('').then(syncDepartmentName).catch(() => {})
  }
  if (!staffOptions.value.length) {
    searchStaff('').catch(() => {})
  }
  headerSettingsOpen.value = true
}

const beforeAvatarUpload: UploadProps['beforeUpload'] = async (file) => {
  const reader = new FileReader()
  reader.onload = () => {
    headerSettings.avatarUrl = String(reader.result || '')
    persistHeaderSettings()
  }
  reader.readAsDataURL(file as File)
  return false
}

async function saveHeaderSettings() {
  const realName = String(headerSettings.realName || '').trim()
  const phone = String(headerSettings.phone || '').trim()
  if (!realName) {
    message.warning('请填写真实姓名')
    return
  }
  if (phone && !/^1\d{10}$/.test(phone)) {
    message.warning('手机号格式不正确')
    return
  }
  if (!ensureSupervisorOrder(headerSettings.directLeaderId, headerSettings.indirectLeaderId)) {
    message.warning('直接领导和间接领导不能为同一人')
    return
  }
  if (headerSettings.directLeaderId && String(headerSettings.directLeaderId) === String(userStore.staffInfo?.id || '')) {
    message.warning('直接领导不能设置为本人')
    return
  }
  if (headerSettings.indirectLeaderId && String(headerSettings.indirectLeaderId) === String(userStore.staffInfo?.id || '')) {
    message.warning('间接领导不能设置为本人')
    return
  }
  if (passwordForm.password || passwordForm.confirmPassword) {
    if (String(passwordForm.password || '').length < 6) {
      message.warning('新密码至少 6 位')
      return
    }
    if (passwordForm.password !== passwordForm.confirmPassword) {
      message.warning('两次输入密码不一致')
      return
    }
  }
  savingHeaderSettings.value = true
  try {
    if (userStore.staffInfo?.id) {
      const updatePayload: Record<string, any> = { realName, phone: phone || undefined }
      updatePayload.directLeaderId = headerSettings.directLeaderId ? Number(headerSettings.directLeaderId) : 0
      updatePayload.indirectLeaderId = headerSettings.indirectLeaderId ? Number(headerSettings.indirectLeaderId) : 0
      if (passwordForm.password) updatePayload.password = passwordForm.password
      await updateStaff(Number(userStore.staffInfo.id), updatePayload)
      userStore.setStaffProfile({
        ...userStore.staffInfo,
        realName,
        phone,
        directLeaderId: updatePayload.directLeaderId > 0 ? updatePayload.directLeaderId : undefined,
        indirectLeaderId: updatePayload.indirectLeaderId > 0 ? updatePayload.indirectLeaderId : undefined
      })
    } else if (userStore.staffInfo) {
      userStore.setStaffProfile({
        ...userStore.staffInfo,
        realName,
        phone,
        directLeaderId: headerSettings.directLeaderId ? Number(headerSettings.directLeaderId) : undefined,
        indirectLeaderId: headerSettings.indirectLeaderId ? Number(headerSettings.indirectLeaderId) : undefined
      })
    }
    syncDepartmentName()
    applyAppearance()
    persistHeaderSettings()
    passwordForm.password = ''
    passwordForm.confirmPassword = ''
    message.success('设置已保存')
    headerSettingsOpen.value = false
  } catch (error: any) {
    message.error(error?.message || '保存失败，请检查权限')
  } finally {
    savingHeaderSettings.value = false
  }
}

function resetHeaderSettings() {
  headerSettings.avatarUrl = ''
  headerSettings.realName = userStore.staffInfo?.realName || ''
  headerSettings.phone = userStore.staffInfo?.phone || ''
  headerSettings.directLeaderId = undefined
  headerSettings.directLeaderName = ''
  headerSettings.indirectLeaderId = undefined
  headerSettings.indirectLeaderName = ''
  headerSettings.themeColor = '#1b66d6'
  headerSettings.fontScale = 100
  headerSettings.quickNotifyEnabled = true
  passwordForm.password = ''
  passwordForm.confirmPassword = ''
  applyAppearance()
  persistHeaderSettings()
  message.success('已恢复默认外观')
}

async function hydrateCurrentStaffInfo() {
  if (!userStore.token) return
  try {
    const me = await getMe()
    if (!me?.id) return
    userStore.setStaffProfile({
      ...me
    })
  } catch {
  }
}

async function hydrateStaffNo() {
  if (headerSettings.staffNo) return
  const staffId = userStore.staffInfo?.id
  const username = userStore.staffInfo?.username || ''
  if (!staffId && !username) return
  try {
    const page = await getStaffPage({
      pageNo: 1,
      pageSize: 120,
      keyword: username || userStore.staffInfo?.realName || ''
    })
    const rows = page.list || []
    const matched = rows.find((item) => Number(item.id) === Number(staffId))
      || rows.find((item) => String(item.username || '') === String(username))
    if (!matched?.staffNo) return
    headerSettings.staffNo = String(matched.staffNo)
    persistHeaderSettings()
  } catch {
  }
}

function fillLeaderNameFromOptions(id?: string) {
  if (!id) return ''
  return (
    leaderOptions.value.find((item) => item.value === id)?.label
    || directLeaderOptions.value.find((item) => item.value === id)?.label
    || indirectLeaderOptions.value.find((item) => item.value === id)?.label
    || ''
  )
}

function onDirectLeaderChange(value?: string) {
  headerSettings.directLeaderId = value || undefined
  headerSettings.directLeaderName = fillLeaderNameFromOptions(headerSettings.directLeaderId)
  persistHeaderSettings()
}

function onIndirectLeaderChange(value?: string) {
  headerSettings.indirectLeaderId = value || undefined
  headerSettings.indirectLeaderName = fillLeaderNameFromOptions(headerSettings.indirectLeaderId)
  persistHeaderSettings()
}

function searchLeaderOptions(keyword: string) {
  searchStaff(keyword || '').catch(() => {})
}

function quickChatStorageKey() {
  const orgId = String(userStore.staffInfo?.orgId || 'default')
  return `layout_quick_chat_rooms_v2_org_${orgId}`
}

function quickChatPresenceKey() {
  return `layout_quick_chat_presence_v1_${userStorageScope()}`
}

function persistQuickChatState() {
  try {
    const payload = quickChatRooms.value.slice(0, 50).map((room) => ({
      id: room.id,
      name: room.name,
      departmentIds: Array.isArray(room.departmentIds) ? room.departmentIds : [],
      memberIds: Array.isArray(room.memberIds) ? room.memberIds : [],
      unreadCount: Number(room.unreadCount || 0),
      unreadByUser: room.unreadByUser || {},
      createdAt: room.createdAt,
      messages: Array.isArray(room.messages)
        ? room.messages.slice(-200).map((message) => ({
          id: message.id,
          senderId: message.senderId,
          senderName: message.senderName,
          kind: message.kind,
          content: message.content || '',
          fileName: message.fileName || '',
          fileUrl: message.fileUrl || '',
          timeText: message.timeText || dayjs().format('MM-DD HH:mm')
        }))
        : []
    }))
    localStorage.setItem(quickChatStorageKey(), JSON.stringify(payload))
    emitQuickChatSync()
  } catch {}
}

function appendRoomSystemMessage(room: QuickChatRoom, text: string) {
  room.messages.push({
    id: `m_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`,
    senderId: 'system',
    senderName: '系统',
    kind: 'SYSTEM',
    content: text,
    timeText: dayjs().format('MM-DD HH:mm')
  })
}

function loadQuickChatState() {
  try {
    const raw = localStorage.getItem(quickChatStorageKey())
    if (!raw) {
      quickChatRooms.value = []
      activeQuickChatRoomId.value = ''
      return
    }
    const parsed = JSON.parse(raw)
    if (!Array.isArray(parsed)) {
      quickChatRooms.value = []
      activeQuickChatRoomId.value = ''
      return
    }
    quickChatRooms.value = parsed.map((room: any) => ({
      id: String(room?.id || `room_${Date.now()}`),
      name: String(room?.name || '未命名群聊'),
      departmentIds: Array.isArray(room?.departmentIds) ? room.departmentIds.map((id: any) => String(id)) : [],
      memberIds: Array.isArray(room?.memberIds) ? room.memberIds.map((id: any) => String(id)) : [],
      unreadCount: Number(room?.unreadByUser?.[currentQuickChatSenderId.value] ?? room?.unreadCount ?? 0),
      unreadByUser: typeof room?.unreadByUser === 'object' && room?.unreadByUser
        ? Object.fromEntries(Object.entries(room.unreadByUser).map(([key, value]) => [String(key), Number(value || 0)]))
        : {
          [currentQuickChatSenderId.value]: Number(room?.unreadCount || 0)
        },
      createdAt: String(room?.createdAt || dayjs().toISOString()),
      messages: Array.isArray(room?.messages)
        ? room.messages.map((message: any) => ({
          id: String(message?.id || `m_${Date.now()}`),
          senderId: String(message?.senderId || 'system'),
          senderName: String(message?.senderName || '系统'),
          kind: (['TEXT', 'IMAGE', 'FILE', 'SYSTEM'].includes(String(message?.kind || 'TEXT')) ? String(message?.kind || 'TEXT') : 'TEXT') as QuickChatMessage['kind'],
          content: String(message?.content || ''),
          fileName: message?.fileName ? String(message.fileName) : undefined,
          fileUrl: message?.fileUrl ? String(message.fileUrl) : undefined,
          timeText: String(message?.timeText || dayjs().format('MM-DD HH:mm'))
        }))
        : []
    }))
    activeQuickChatRoomId.value = quickChatRooms.value[0]?.id || ''
  } catch {
    quickChatRooms.value = []
    activeQuickChatRoomId.value = ''
  }
}

function emitQuickChatSync() {
  const payload: LiveSyncPayload = {
    topics: ['oa', 'system'],
    method: 'LOCAL',
    url: '/local/quick-chat',
    at: Date.now()
  }
  emitLiveSync(payload)
}

function rehydrateQuickChatUnread() {
  const currentId = currentQuickChatSenderId.value
  quickChatRooms.value = quickChatRooms.value.map((room) => {
    const unreadByUser = room.unreadByUser || {}
    return {
      ...room,
      unreadByUser,
      unreadCount: Number(unreadByUser[currentId] || 0)
    }
  })
}

function syncQuickChatFromStorage() {
  const previousActive = activeQuickChatRoomId.value
  loadQuickChatState()
  if (previousActive && quickChatRooms.value.some((item) => item.id === previousActive)) {
    activeQuickChatRoomId.value = previousActive
  }
  rehydrateQuickChatUnread()
}

function persistPresenceState() {
  try {
    localStorage.setItem(quickChatPresenceKey(), JSON.stringify({
      dnd: quickChatDnd.value,
      trainingUntil: quickChatTrainingUntil.value || ''
    }))
  } catch {}
}

function loadPresenceState() {
  try {
    const raw = localStorage.getItem(quickChatPresenceKey())
    if (!raw) return
    const parsed = JSON.parse(raw)
    quickChatDnd.value = parsed?.dnd === true
    quickChatTrainingUntil.value = String(parsed?.trainingUntil || '')
  } catch {}
}

function openQuickChat() {
  quickChatOpen.value = true
  if (!activeQuickChatRoomId.value && quickChatRooms.value.length) {
    activeQuickChatRoomId.value = quickChatRooms.value[0].id
  }
  if (activeQuickChatRoom.value?.unreadCount) {
    activeQuickChatRoom.value.unreadCount = 0
    persistQuickChatState()
  }
}

function selectQuickChatRoom(roomId: string) {
  activeQuickChatRoomId.value = roomId
  const room = quickChatRooms.value.find((item) => item.id === roomId)
  if (!room) return
  const currentId = currentQuickChatSenderId.value
  room.unreadByUser = room.unreadByUser || {}
  if (Number(room.unreadByUser[currentId] || 0) > 0 || room.unreadCount > 0) {
    room.unreadByUser[currentId] = 0
    room.unreadCount = 0
    persistQuickChatState()
  }
}

function resetQuickChatRoomForm() {
  quickChatRoomForm.name = ''
  quickChatRoomForm.departmentIds = []
  quickChatRoomForm.memberIds = []
  quickChatRoomForm.applyRemark = ''
}

function openCreateQuickChatRoom() {
  resetQuickChatRoomForm()
  quickChatRoomEditorMode.value = 'create'
  quickChatRoomEditorOpen.value = true
}

function openInviteQuickChat() {
  if (!activeQuickChatRoom.value) {
    message.info('请先选择一个会话')
    return
  }
  quickChatRoomForm.name = activeQuickChatRoom.value.name
  quickChatRoomForm.departmentIds = [...activeQuickChatRoom.value.departmentIds]
  quickChatRoomForm.memberIds = [...activeQuickChatRoom.value.memberIds]
  quickChatRoomForm.applyRemark = ''
  quickChatRoomEditorMode.value = 'invite'
  quickChatRoomEditorOpen.value = true
}

function memberNameById(staffId: string) {
  const matched = staffOptions.value.find((item) => item.value === staffId)
  return matched?.name || `员工#${staffId}`
}

function departmentNameById(departmentId: string) {
  const matched = departmentOptions.value.find((item) => item.value === departmentId)
  return matched?.name || `部门#${departmentId}`
}

function quickChatLastMessageText(room: QuickChatRoom) {
  const latest = room.messages[room.messages.length - 1]
  if (!latest) return '暂无消息'
  if (latest.kind === 'IMAGE') return `${latest.senderName}: [图片]`
  if (latest.kind === 'FILE') return `${latest.senderName}: [文件] ${latest.fileName || ''}`.trim()
  if (latest.kind === 'SYSTEM') return `[系统] ${latest.content}`
  return `${latest.senderName}: ${latest.content}`
}

function submitQuickChatRoomEditor() {
  const name = String(quickChatRoomForm.name || '').trim()
  if (!name) {
    message.warning('请填写群聊名称')
    return
  }
  const selfId = currentQuickChatSenderId.value
  const selectedMembers = Array.from(new Set([...(quickChatRoomForm.memberIds || []), selfId].map((id) => String(id))))
  const selectedDepartments = Array.from(new Set((quickChatRoomForm.departmentIds || []).map((id) => String(id))))
  if (quickChatRoomEditorMode.value === 'create') {
    const room: QuickChatRoom = {
      id: `room_${Date.now()}_${Math.random().toString(36).slice(2, 7)}`,
      name,
      departmentIds: selectedDepartments,
      memberIds: selectedMembers,
      unreadCount: 0,
      unreadByUser: Object.fromEntries(selectedMembers.map((id) => [id, 0])),
      createdAt: dayjs().toISOString(),
      messages: []
    }
    appendRoomSystemMessage(room, `${currentQuickChatSenderName.value} 发起了群聊申请`)
    if (quickChatRoomForm.applyRemark.trim()) {
      appendRoomSystemMessage(room, `申请说明：${quickChatRoomForm.applyRemark.trim()}`)
    }
    if (selectedDepartments.length) {
      appendRoomSystemMessage(room, `邀请部门：${selectedDepartments.map((id) => departmentNameById(id)).join('、')}`)
    }
    quickChatRooms.value.unshift(room)
    activeQuickChatRoomId.value = room.id
    persistQuickChatState()
    message.success('群聊申请已发起')
  } else {
    const room = activeQuickChatRoom.value
    if (!room) {
      message.warning('当前会话不存在')
      return
    }
    room.name = name
    room.departmentIds = selectedDepartments
    room.memberIds = selectedMembers
    room.unreadByUser = room.unreadByUser || {}
    selectedMembers.forEach((id) => {
      if (room.unreadByUser && room.unreadByUser[id] == null) room.unreadByUser[id] = 0
    })
    const invited = selectedMembers.filter((id) => id !== selfId).map((id) => memberNameById(id))
    if (invited.length) {
      appendRoomSystemMessage(room, `${currentQuickChatSenderName.value} 更新了成员：${invited.join('、')}`)
    }
    if (selectedDepartments.length) {
      appendRoomSystemMessage(room, `同步部门：${selectedDepartments.map((id) => departmentNameById(id)).join('、')}`)
    }
    if (quickChatRoomForm.applyRemark.trim()) {
      appendRoomSystemMessage(room, `附言：${quickChatRoomForm.applyRemark.trim()}`)
    }
    persistQuickChatState()
    message.success('群聊已更新')
  }
  quickChatRoomEditorOpen.value = false
  resetQuickChatRoomForm()
}

function sendQuickChatText() {
  const room = activeQuickChatRoom.value
  if (!room) {
    message.info('请先选择会话')
    return
  }
  const content = String(quickChatDraft.value || '').trim()
  if (!content) {
    message.warning('请输入消息内容')
    return
  }
  room.messages.push({
    id: `m_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`,
    senderId: currentQuickChatSenderId.value,
    senderName: currentQuickChatSenderName.value,
    kind: 'TEXT',
    content,
    timeText: dayjs().format('MM-DD HH:mm')
  })
  room.unreadByUser = room.unreadByUser || {}
  room.memberIds.forEach((memberId) => {
    if (memberId === currentQuickChatSenderId.value) {
      room.unreadByUser![memberId] = 0
    } else {
      const currentUnread = Number(room.unreadByUser![memberId] || 0)
      room.unreadByUser![memberId] = currentUnread + 1
    }
  })
  room.unreadCount = Number(room.unreadByUser[currentQuickChatSenderId.value] || 0)
  quickChatDraft.value = ''
  persistQuickChatState()
}

const beforeQuickChatUpload: UploadProps['beforeUpload'] = async (file) => {
  const room = activeQuickChatRoom.value
  if (!room) {
    message.info('请先选择会话')
    return false
  }
  const blobUrl = URL.createObjectURL(file as File)
  const isImage = String((file as File).type || '').startsWith('image/')
  room.messages.push({
    id: `m_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`,
    senderId: currentQuickChatSenderId.value,
    senderName: currentQuickChatSenderName.value,
    kind: isImage ? 'IMAGE' : 'FILE',
    content: isImage ? '[图片]' : '[文件]',
    fileName: (file as File).name,
    fileUrl: blobUrl,
    timeText: dayjs().format('MM-DD HH:mm')
  })
  room.unreadByUser = room.unreadByUser || {}
  room.memberIds.forEach((memberId) => {
    if (memberId === currentQuickChatSenderId.value) {
      room.unreadByUser![memberId] = 0
    } else {
      const currentUnread = Number(room.unreadByUser![memberId] || 0)
      room.unreadByUser![memberId] = currentUnread + 1
    }
  })
  room.unreadCount = Number(room.unreadByUser[currentQuickChatSenderId.value] || 0)
  persistQuickChatState()
  return false
}

function markActiveRoomRead() {
  const room = activeQuickChatRoom.value
  if (!room) return
  const currentId = currentQuickChatSenderId.value
  room.unreadByUser = room.unreadByUser || {}
  room.unreadByUser[currentId] = 0
  room.unreadCount = 0
  persistQuickChatState()
  message.success('已标记为已读')
}

function removeActiveQuickChatRoom() {
  const room = activeQuickChatRoom.value
  if (!room) return
  const roomId = room.id
  quickChatRooms.value = quickChatRooms.value.filter((item) => item.id !== roomId)
  if (activeQuickChatRoomId.value === roomId) {
    activeQuickChatRoomId.value = quickChatRooms.value[0]?.id || ''
  }
  persistQuickChatState()
  message.success('会话已删除')
}

function toggleDnd() {
  quickChatDnd.value = !quickChatDnd.value
  persistPresenceState()
  message.success(quickChatDnd.value ? '已开启免打扰' : '已关闭免打扰')
}

function setPresenceTraining(hours: number) {
  const safeHours = Math.min(Math.max(Number(hours || 2), 1), 12)
  quickChatTrainingUntil.value = dayjs().add(safeHours, 'hour').toISOString()
  quickChatDnd.value = false
  persistPresenceState()
  message.success(`已进入外出培训状态（${safeHours}小时）`)
}

function clearPresenceTraining() {
  quickChatTrainingUntil.value = ''
  persistPresenceState()
  message.success('已结束培训状态')
}

onMounted(() => {
  hydrateCurrentStaffInfo()
    .then(() => Promise.allSettled([searchDepartments(''), searchStaff('')]))
    .then(() => {
      syncDepartmentName()
      loadHeaderSettings()
      loadQuickChatState()
      loadPresenceState()
      rehydrateQuickChatUnread()
      return hydrateStaffNo()
    })
    .finally(() => {
      restoreRouteTabs()
      syncRouteTab(route.path, route.fullPath)
    })
  presenceTimer = window.setInterval(() => {
    presenceTick.value = Date.now()
    if (quickChatTrainingUntil.value && dayjs().isAfter(dayjs(quickChatTrainingUntil.value))) {
      quickChatTrainingUntil.value = ''
      persistPresenceState()
    }
  }, 60 * 1000)
  quickChatSyncUnsubscribe = subscribeLiveSync((payload) => {
    if (!payload?.topics?.includes('oa') && payload?.url !== '/local/quick-chat') return
    if (payload.url !== '/local/quick-chat') return
    syncQuickChatFromStorage()
  })
  window.addEventListener('storage', onQuickChatStorageChange)
  document.addEventListener('click', closeTabContextMenu)
})

onBeforeUnmount(() => {
  if (presenceTimer) {
    window.clearInterval(presenceTimer)
    presenceTimer = undefined
  }
  quickChatSyncUnsubscribe()
  window.removeEventListener('storage', onQuickChatStorageChange)
  document.removeEventListener('click', closeTabContextMenu)
})

function onQuickChatStorageChange(event: StorageEvent) {
  if (event.key !== quickChatStorageKey()) return
  syncQuickChatFromStorage()
}
</script>

<style scoped>
.app-layout {
  min-height: 100vh;
  background: var(--bg);
}

.app-sider {
  background: linear-gradient(180deg, #08316d 0%, #0e4a99 100%);
  border-right: 1px solid rgba(255, 255, 255, 0.08);
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 16px;
  color: #f8fafc;
}

.logo {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: #ffffff;
  color: #0b2f6b;
  font-weight: 700;
  display: grid;
  place-items: center;
}

.title {
  font-weight: 700;
  font-size: 16px;
}

.subtitle {
  font-size: 12px;
  color: rgba(248, 250, 252, 0.65);
}

.side-menu {
  background: transparent !important;
  border-inline-end: none !important;
}

.side-menu .ant-menu-item,
.side-menu .ant-menu-submenu-title {
  border-radius: 10px;
  margin: 6px 12px;
}

.side-menu .ant-menu-item-selected {
  background: rgba(255, 255, 255, 0.18) !important;
}

.app-main {
  background: var(--bg);
}

.app-header {
  position: sticky;
  top: 0;
  z-index: 10;
  background: linear-gradient(120deg, rgba(255, 255, 255, 0.92) 0%, rgba(244, 249, 255, 0.92) 100%);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--border);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  height: 64px;
  box-shadow: var(--shadow-sm);
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.page-title {
  font-weight: 600;
  font-size: 16px;
  color: var(--ink);
}

.breadcrumb {
  font-size: 12px;
  color: var(--muted);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.today-label {
  padding: 4px 10px;
  font-size: 12px;
  border-radius: 999px;
  color: #2455a8;
  background: rgba(39, 111, 229, 0.12);
}

.system-name {
  color: var(--muted);
  font-size: 12px;
}

.user-link {
  color: var(--ink);
}

.user-entry {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.system-status {
  font-size: 12px;
}

.presence-tag {
  cursor: pointer;
  user-select: none;
}

.settings-tip {
  margin-top: 12px;
  color: var(--muted);
  font-size: 12px;
}

.route-tabs-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 24px 0;
  background: #fff;
  border-bottom: 1px solid var(--border);
  overflow: hidden;
}

.route-tabs-wrap :deep(.ant-tabs) {
  flex: 1;
  min-width: 0;
  margin-bottom: 0;
}

.route-tabs-wrap :deep(.ant-tabs-nav) {
  margin: 0;
  max-width: 100%;
  overflow: hidden;
}

.route-tabs-wrap :deep(.ant-tabs-nav-wrap) {
  overflow-x: auto !important;
  overflow-y: hidden !important;
  scrollbar-width: thin;
  scrollbar-color: rgba(148, 163, 184, 0.45) transparent;
  padding-bottom: 4px;
}

.route-tabs-wrap :deep(.ant-tabs-nav-wrap::-webkit-scrollbar) {
  height: 3px;
}

.route-tabs-wrap :deep(.ant-tabs-nav-wrap::-webkit-scrollbar-thumb) {
  background: rgba(148, 163, 184, 0.45);
  border-radius: 999px;
}

.route-tabs-wrap :deep(.ant-tabs-nav-wrap::-webkit-scrollbar-track) {
  background: transparent;
}

.route-tabs-wrap :deep(.ant-tabs-nav-list) {
  min-width: max-content;
}

.tab-tools-btn {
  margin-bottom: 6px;
}

.route-tab-title {
  display: inline-block;
  max-width: 160px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  vertical-align: bottom;
  user-select: none;
  transition: background-color 0.18s ease, transform 0.16s ease, box-shadow 0.16s ease;
}

.route-tab-dragging {
  opacity: 0.72;
  transform: scale(0.98);
}

.route-tab-over {
  background: rgba(27, 102, 214, 0.12);
  border-radius: 6px;
  box-shadow: inset 0 0 0 1px rgba(27, 102, 214, 0.24);
}

.tab-context-menu {
  position: fixed;
  z-index: 1200;
  min-width: 140px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.14);
}

.app-content {
  padding: 24px;
  min-height: calc(100vh - 108px);
}

.quick-chat-layout {
  display: grid;
  grid-template-columns: 240px 1fr;
  gap: 12px;
  min-height: 520px;
}

.quick-chat-room-list {
  border: 1px solid var(--border);
  border-radius: 12px;
  background: #fff;
  overflow: auto;
}

.quick-chat-room-item {
  cursor: pointer;
  transition: background-color 0.16s ease;
}

.quick-chat-room-item.active {
  background: rgba(27, 102, 214, 0.08);
}

.quick-chat-main {
  border: 1px solid var(--border);
  border-radius: 12px;
  background: #fff;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.quick-chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 14px;
  border-bottom: 1px solid var(--border);
}

.quick-chat-messages {
  flex: 1;
  padding: 12px;
  overflow-y: auto;
  background: linear-gradient(180deg, rgba(245, 248, 255, 0.72) 0%, #fff 100%);
}

.quick-chat-message {
  margin-bottom: 10px;
  max-width: 76%;
  background: #fff;
  border: 1px solid var(--border);
  border-radius: 10px;
  padding: 8px 10px;
  box-shadow: 0 3px 10px rgba(15, 23, 42, 0.06);
}

.quick-chat-message.self {
  margin-left: auto;
  background: #eaf3ff;
  border-color: rgba(27, 102, 214, 0.22);
}

.quick-chat-message-meta {
  display: flex;
  justify-content: space-between;
  color: var(--muted);
  font-size: 12px;
  margin-bottom: 4px;
}

.quick-chat-message-body {
  color: var(--ink);
  word-break: break-word;
}

.quick-chat-image {
  max-width: 220px;
  border-radius: 8px;
  border: 1px solid var(--border);
  display: block;
}

.quick-chat-input {
  border-top: 1px solid var(--border);
  padding: 10px 12px;
}

.hint-text {
  font-size: 12px;
  color: var(--muted);
}

@media (max-width: 992px) {
  .app-header {
    padding: 0 14px;
  }

  .route-tabs-wrap {
    padding: 6px 12px 0;
    gap: 6px;
  }

  .route-tab-title {
    max-width: 120px;
  }

  .tab-tools-btn {
    padding-inline: 8px;
  }

  .today-label,
  .system-name {
    display: none;
  }

  .app-content {
    padding: 14px;
    min-height: calc(100vh - 108px);
  }

  .quick-chat-layout {
    grid-template-columns: 1fr;
    min-height: unset;
  }

  .quick-chat-room-list {
    max-height: 180px;
  }
}
</style>
