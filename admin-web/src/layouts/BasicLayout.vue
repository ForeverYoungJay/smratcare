<template>
  <div
    v-if="isMobileViewport && mobileSidebarOpen"
    class="mobile-sider-backdrop"
    @click="mobileSidebarOpen = false"
  ></div>
  <AppShell :show-peek-sensor="!isMobileViewport && manualCollapsed && !sidebarPeekOpen" @peek-open="openSidebarPeek">
    <template #sider>
      <PrimarySider
        :collapsed="siderCollapsed"
        :is-peek-open="sidebarPeekOpen"
        :manual-collapsed="manualCollapsed"
        :menu-items="menuItems"
        :mobile="isMobileViewport"
        :open-keys="openKeys"
        :selected-keys="selectedKeys"
        :can-toggle-scope="canToggleMenuScope"
        :show-all-menu="menuShowAll"
        @mouseleave="handleSidebarMouseLeave"
        @menu-click="onMenuClick"
        @open-change="onOpenChange"
        @toggle-menu-scope="toggleMenuScope"
      />
    </template>

    <template #header>
      <GlobalHeader
        :avatar-url="headerSettings.avatarUrl || undefined"
        :breadcrumbs="breadcrumbs"
        :collapsed="manualCollapsed"
        :notification-count="notificationBadgeCount"
        :page-title="currentTitle || '工作台'"
        platform-name="龟峰颐养中心运营管理平台"
        search-placeholder="搜索页面、常用动作或最近访问"
        :todo-count="chatOpenTasks"
        :today-label="todayLabel"
        :user-initial="userInitial"
        :user-name="displayUserName"
        @open-search="openGlobalSearch"
        @toggle-sidebar="toggleSidebar"
      >
        <template #notification-overlay>
          <a-menu class="notify-menu">
            <a-menu-item-group :title="`我的消息 ${chatUnread ? `· ${chatUnread} 条未读` : ''}`">
              <a-menu-item
                v-for="item in headerMessagePreviews"
                :key="`message-${item.roomId}`"
                class="notify-menu__message"
                @click="router.push({ path: '/workbench/chat', query: { c: item.roomId } })"
              >
                <div class="notify-message">
                  <span class="notify-message__avatar" aria-hidden="true">{{ item.roomName.slice(0, 1) }}</span>
                  <span class="notify-message__body">
                    <span class="notify-message__meta"><strong>{{ item.roomName }}</strong><time :datetime="item.createdAt">{{ quickChatPreviewTime(item.createdAt) }}</time></span>
                    <small>{{ item.content }}</small>
                  </span>
                  <span v-if="item.unreadCount" class="notify-message__unread" role="status" aria-atomic="true">{{ item.unreadCount }}</span>
                </div>
              </a-menu-item>
              <a-menu-item v-if="!headerMessagePreviews.length" key="message-empty" disabled>暂无消息</a-menu-item>
              <a-menu-item key="message-all" @click="openQuickChat">查看全部消息</a-menu-item>
            </a-menu-item-group>
            <a-menu-divider />
            <a-menu-item-group v-if="canUseFinanceReminders" :title="`未处理提醒 ${financeReminderPending}`">
              <a-menu-item
                v-for="item in financeReminderItems"
                :key="`reminder-${item.id}`"
                class="notify-menu__reminder"
              >
                <div class="notify-reminder">
                  <div class="notify-reminder__text" @click="goReminderAction(item)">
                    <strong>{{ item.title }}</strong>
                    <small>{{ item.reminderTypeText }}{{ item.bizMonth ? ' · ' + item.bizMonth : '' }}</small>
                  </div>
                  <a-button type="link" size="small" @click.stop="markReminderHandled(item)">已处理</a-button>
                </div>
              </a-menu-item>
              <a-menu-item v-if="!financeReminderItems.length" key="reminder-empty" disabled>
                暂无未处理提醒
              </a-menu-item>
              <a-menu-item
                v-if="financeReminderPending > 0"
                key="reminder-handle-all"
                @click="markAllRemindersHandled"
              >
                一键全部处理（{{ financeReminderPending }} 条）
              </a-menu-item>
              <a-menu-item key="reminder-center" @click="router.push('/finance/reminder-center')">
                进入提醒中心
              </a-menu-item>
            </a-menu-item-group>
            <a-menu-divider v-if="canUseFinanceReminders" />
            <a-menu-item v-for="item in quickNotifyItems" :key="item.title" @click="onQuickNotifyClick(item)">
              {{ item.title }}
            </a-menu-item>
          </a-menu>
        </template>

        <template #todo-overlay>
          <a-menu>
            <a-menu-item @click="handleHeaderQuickTodoClick">我的待办</a-menu-item>
            <a-menu-item @click="router.push('/oa/approval?scope=PENDING_REVIEW')">待我审批</a-menu-item>
          </a-menu>
        </template>

        <template #user-overlay>
          <a-menu>
            <a-menu-item @click="openHeaderSettings">个人设置</a-menu-item>
            <a-menu-item @click="router.push('/hr/profile/account-access')">账号与赋权</a-menu-item>
            <a-menu-item @click="logout">退出</a-menu-item>
          </a-menu>
        </template>
      </GlobalHeader>
    </template>

    <template #tabs>
      <div ref="routeTabsWrapRef">
        <RouteTabsBar
          :active-tab-key="activeTabKey"
          :drag-key="tabDrag.dragKey || undefined"
          :over-key="tabDrag.overKey || undefined"
          :route-tabs="routeTabs"
          @change="onTabChange"
          @context-menu="openTabContextMenu"
          @drag-end="onTabDragEnd"
          @drag-over="onTabDragOver"
          @drag-start="onTabDragStart"
          @drop="onTabDrop"
          @edit="onTabEdit"
        >
          <template #tab-tools-overlay>
            <a-menu>
              <a-menu-item @click="refreshCurrentTab">刷新当前</a-menu-item>
              <a-menu-item @click="closeOtherTabs">关闭其他</a-menu-item>
              <a-menu-item @click="closeAllTabs">关闭全部</a-menu-item>
            </a-menu>
          </template>
          <template #extra>
            <a-badge status="processing" text="系统运行中" />
            <a-badge :count="chatUnread" size="small">
              <a-button size="small" @click="openQuickChat">快捷聊天</a-button>
            </a-badge>
            <a-badge :count="chatOpenTasks" size="small">
              <a-button size="small" @click="openQuickChatTodoDrawer">聊天待办</a-button>
            </a-badge>
          </template>
        </RouteTabsBar>
      </div>
    </template>

    <CompletionGuide />
    <router-view v-slot="{ Component }">
      <keep-alive :max="aliveViewMax">
        <component v-if="shouldCacheCurrentRoute" :is="Component" :key="viewRenderKey" />
      </keep-alive>
      <component v-if="!shouldCacheCurrentRoute" :is="Component" :key="viewRenderKey" />
    </router-view>
  </AppShell>

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
        人事新建账号与监管关系：请前往“账号与赋权”页面完成员工账号、初始密码与领导设置。
      </div>
    </a-form>
  </a-drawer>

  <ChatQuickDrawer v-model:open="chatDrawerOpen" />

  <GlobalSearchPalette
    :action-items="searchActionItems"
    :keyword="globalSearchKeyword"
    :open="globalSearchOpen"
    :page-items="searchPageItems"
    :recent-items="searchRecentItems"
    @close="closeGlobalSearch"
    @select="handleSearchSelection"
    @update:keyword="globalSearchKeyword = $event"
  />
</template>

<script setup lang="ts">
import CompletionGuide from '../components/CompletionGuide.vue'
import { completionGuideKey, type CompletionGuide as CompletionGuideState } from '../composables/useCompletionGuide'
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch, provide } from 'vue'
import { message, notification } from 'ant-design-vue'
import type { UploadProps } from 'ant-design-vue'
import dayjs from 'dayjs'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { workspaceLocation } from '../workspaces/catalog'
import { getMenuTree, supportsJobNavigation } from './menu'
import { getMe } from '../api/auth'
import {
  getReminderPage,
  getReminderSummary,
  handleAllReminders,
  handleReminder,
  type FinanceReminderItem
} from '../api/financeReminder'
import { getStaffPage } from '../api/rbac'
import { updateStaff } from '../api/staff'
import { useDepartmentOptions } from '../composables/useDepartmentOptions'
import { useStaffOptions } from '../composables/useStaffOptions'
import { canBeDirectLeader, canBeIndirectLeader, ensureSupervisorOrder } from '../utils/supervisor'
import { getToken } from '../utils/auth'
import AppShell from './components/AppShell.vue'
import GlobalHeader from './components/GlobalHeader.vue'
import GlobalSearchPalette, { type SearchPaletteItem } from './components/GlobalSearchPalette.vue'
import PrimarySider from './components/PrimarySider.vue'
import ChatQuickDrawer from '../views/chat/ChatQuickDrawer.vue'
import { getChatTasks, getConversations } from '../api/chat'
import RouteTabsBar from './components/RouteTabsBar.vue'
import { buildGroupedMenuItems } from './navigation'
import { loadRecentVisits, saveRecentVisit, type RecentVisitItem } from '../utils/recentVisits'
import { resolveRouteAccess } from '../utils/routeAccess'
import { resolveDefaultHome } from '../access/policy'
import { fuzzyFilter } from '../utils/fuzzySearch'
import { QUICK_CHAT_OPEN_EVENT } from '../utils/quickChatPreview'

const completionGuide = ref<CompletionGuideState | null>(null)
provide(completionGuideKey, completionGuide)
provide('business-page-layout', true)
const MAX_RESTORED_ROUTE_TABS = 8
const MAX_ALIVE_VIEW_CACHE = 3
const NON_CACHED_ROUTE_NAME_KEYWORDS = ['portal', 'dashboard', 'workbench', 'taskcenter', 'todo', 'approval', 'calendar', 'panorama']
const NON_CACHED_ROUTE_PATH_PATTERNS = [
  /^\/portal$/,
  /^\/dashboard$/,
  /^\/oa\/(approval|todo|calendar|portal|task)(\/|$)/,
  /^\/logistics\/(workbench|task-center)(\/|$)/,
  /^\/marketing\/workbench(\/|$)/,
  /^\/elder\/(resident-360|in-hospital-overview|bed-panorama)(\/|$)/,
  /^\/elder\/status-change\/center(\/|$)/,
  /^\/medical\/(workbench|unified-task-center|medical-health-center|nursing-quality-center)(\/|$)/
]

const manualCollapsed = ref(false)
const sidebarPeekOpen = ref(false)
const isMobileViewport = ref(false)
const mobileSidebarOpen = ref(false)
const siderCollapsed = computed(() => {
  if (isMobileViewport.value) return !mobileSidebarOpen.value
  return manualCollapsed.value && !sidebarPeekOpen.value
})
const route = useRoute()
watch(() => route.fullPath, () => { completionGuide.value = null })
const router = useRouter()
const userStore = useUserStore()

// 侧栏展开状态跨会话记忆
const SIDER_OPEN_KEYS_STORAGE = 'smartcare.sider.openKeys'

function loadPersistedOpenKeys(): string[] {
  try {
    const raw = localStorage.getItem(SIDER_OPEN_KEYS_STORAGE)
    const parsed = raw ? JSON.parse(raw) : []
    return Array.isArray(parsed) ? parsed.filter((key) => typeof key === 'string') : []
  } catch {
    return []
  }
}

function persistOpenKeys(keys: string[]) {
  try {
    localStorage.setItem(SIDER_OPEN_KEYS_STORAGE, JSON.stringify(keys.slice(-16)))
  } catch {}
}

const openKeys = ref<string[]>(loadPersistedOpenKeys())
const routeTabsWrapRef = ref<HTMLElement | null>(null)
const routeTabs = ref<Array<{ key: string; path: string; title: string; closable: boolean }>>([])
const activeTabKey = ref('')
const tabRefreshSeeds = reactive<Record<string, number>>({})
const globalSearchOpen = ref(false)
const globalSearchKeyword = ref('')
const recentVisitItems = ref<RecentVisitItem[]>([])
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
  themeColor: '#21705F',
  fontScale: 100,
  quickNotifyEnabled: true
})
const passwordForm = reactive({
  password: '',
  confirmPassword: ''
})
const themeColorOptions = [
  { label: '品牌绿（默认）', value: '#21705F' },
  { label: '海蓝', value: '#1b66d6' },
  { label: '翠绿', value: '#22a06b' },
  { label: '暖橙', value: '#f59e0b' },
  { label: '典雅紫', value: '#7c3aed' },
  { label: '深青', value: '#0f766e' }
]
const chatDrawerOpen = ref(false)
const chatUnread = ref(0)
const chatOpenTasks = ref(0)
let chatCountTimer: number | undefined
function onChatRead() {
  refreshChatCounts().catch(() => {})
}
/** 顶栏数字来自服务端：未读按已读游标计算，聊天待办取我负责且未完成的统一任务 */
async function refreshChatCounts() {
  const [convs, tasks] = await Promise.all([getConversations(), getChatTasks({ view: 'mine' })])
  chatUnread.value = convs.reduce((sum, c) => sum + Number(c.unread || 0), 0)
  headerMessagePreviews.value = convs.slice(0, 5).map((c) => ({
    roomId: c.id, roomName: c.title, content: c.lastMessagePreview || '暂无消息', createdAt: c.lastMessageAt || '', unreadCount: Number(c.unread || 0)
  }))
  chatOpenTasks.value = tasks.filter((t) => t.status !== 'DONE').length
}
const quickNotifyItems = computed(() => {
  if (!headerSettings.quickNotifyEnabled) return []
  // 铃铛只放通知（未读消息、逾期任务）；页面入口在侧栏「我的办公」和待办按钮里，不在这里重复
  return [
    { title: `未读聊天消息（${chatUnread.value}）`, action: 'openQuickChat' },
    { title: `我负责的聊天待办（${chatOpenTasks.value}）`, action: 'openQuickChatTodo' }
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
const userInitial = computed(() => (userStore.staffInfo?.realName || headerSettings.realName || '管').slice(0, 1))

function toggleSidebar() {
  if (isMobileViewport.value) {
    mobileSidebarOpen.value = !mobileSidebarOpen.value
    return
  }
  manualCollapsed.value = !manualCollapsed.value
  if (!manualCollapsed.value) {
    sidebarPeekOpen.value = false
  }
}

function openSidebarPeek() {
  if (isMobileViewport.value) return
  if (!manualCollapsed.value) return
  sidebarPeekOpen.value = true
}

function handleSidebarMouseLeave() {
  if (isMobileViewport.value) return
  if (!manualCollapsed.value) return
  sidebarPeekOpen.value = false
}

let quickChatWsReconnectTimer: number | undefined
let quickChatWsReconnectAttempt = 0
let quickChatWebSocket: WebSocket | null = null
let quickChatWebSocketManualClose = false

const headerMessagePreviews = ref<{ roomId: string; roomName: string; content: string; createdAt: string; unreadCount: number }[]>([])
// 财务提醒（下月代养费/押金未缴清/电费未登记与未缴）驱动铃铛。
// 非财务角色调用会被后端拦掉，这里静默降级为 0，不影响其它头部功能。
const financeReminderPending = ref(0)
const financeReminderItems = ref<FinanceReminderItem[]>([])
const canUseFinanceReminders = computed(() => canOpenPath('/finance/reminder-center'))

async function loadFinanceReminders() {
  if (!canUseFinanceReminders.value) return
  try {
    const [summary, page] = await Promise.all([
      getReminderSummary(),
      getReminderPage({ pageNo: 1, pageSize: 5, status: 'PENDING' })
    ])
    financeReminderPending.value = Number(summary?.pendingCount || 0)
    financeReminderItems.value = page?.list || []
  } catch {
    financeReminderPending.value = 0
    financeReminderItems.value = []
  }
}

async function markReminderHandled(item: FinanceReminderItem) {
  try {
    await handleReminder(item.id)
    message.success('已标记处理')
    await loadFinanceReminders()
  } catch {
    // 失败信息由请求层统一提示
  }
}

async function markAllRemindersHandled() {
  try {
    const res = await handleAllReminders(undefined, '铃铛一键处理')
    message.success(`已处理 ${res?.handled || 0} 条`)
    await loadFinanceReminders()
  } catch {
    // 失败信息由请求层统一提示
  }
}

function goReminderAction(item: FinanceReminderItem) {
  router.push(item.actionPath || '/finance/reminder-center')
}

// 铃铛徽章必须反映真实未处理量：财务提醒未处理数 + 未读消息 + 逾期待办，
// 不能再等于下拉里固定快捷入口的条数，否则永远显示同一个数字
const notificationBadgeCount = computed(() => {
  if (!headerSettings.quickNotifyEnabled) return 0
  return financeReminderPending.value + chatUnread.value
})
const menuShowAll = ref(false)
const canToggleMenuScope = computed(() => supportsJobNavigation(userStore.roles || []))

function toggleMenuScope() {
  menuShowAll.value = !menuShowAll.value
}

const accessibleMenu = computed(() => getMenuTree(userStore.roles || [], userStore.pagePermissions || [], {
  permissions: userStore.permissions || []
}))

const filteredMenu = computed(() => {
  const roles = userStore.roles || []
  // 按角色收敛默认导航：一线员工/部长默认只看与岗位相关的模块（可手动“显示全部”）；管理员/院长始终全量。
  return getMenuTree(roles, userStore.pagePermissions || [], {
    focused: !menuShowAll.value,
    permissions: userStore.permissions || []
  })
})

function canOpenPath(path: string) {
  return resolveRouteAccess(router, userStore.roles || [], path, userStore.pagePermissions || [], userStore.permissions || []).canAccess
}

const menuItems = computed(() => buildGroupedMenuItems(filteredMenu.value))

// 详情/编辑等未出现在菜单里的深层页面，也按最长前缀高亮所属菜单项
const menuSelectablePaths = computed(() => {
  const paths: string[] = []
  const walk = (items: any[]) => {
    for (const item of items || []) {
      if (item.path) paths.push(String(item.path))
      if (item.children?.length) walk(item.children)
    }
  }
  walk(filteredMenu.value)
  return paths
})

const selectedKeys = computed(() => {
  const path = route.path
  let best = ''
  for (const candidate of menuSelectablePaths.value) {
    if (candidate.length <= best.length) continue
    if (path === candidate || path.startsWith(`${candidate}/`)) best = candidate
  }
  return [best || path]
})
const currentRouteTabKey = computed(() => normalizeTabKey(route.path || route.fullPath) || route.fullPath)
const aliveViewMax = computed(() => Math.min(Math.max(routeTabs.value.length, 4), MAX_ALIVE_VIEW_CACHE))
const viewRenderKey = computed(() => `${currentRouteTabKey.value}::${tabRefreshSeeds[currentRouteTabKey.value] || 0}`)

const currentTitle = computed(() => {
  const title = route.meta?.title as string | undefined
  return title || ''
})
const searchFields = (item: SearchPaletteItem) => ({ title: item.title, extra: `${item.description} ${item.keywords || ''}` })
const searchPageItems = computed<SearchPaletteItem[]>(() => {
  const collect = (items: any[], parents: string[] = []): SearchPaletteItem[] =>
    items.flatMap((item) => {
      const title = String(item.label || '')
      const nextParents = [...parents, title]
      const breadcrumb = nextParents.join(' / ')
      const desc = String(item.desc || '')
      const aliasText = Array.isArray(item.aliases) ? item.aliases.join(' ') : ''
      const keywords = `${title} ${desc} ${breadcrumb} ${aliasText}`.trim()
      if (item.children?.length) {
        const parentEntry: SearchPaletteItem[] = item.path
          ? [{
              key: String(item.path || item.key),
              title,
              description: desc || breadcrumb,
              group: '一级模块',
              path: String(item.path || item.key),
              keywords
            }]
          : []
        return [...parentEntry, ...collect(item.children, nextParents)]
      }
      return [{
        key: String(item.path || item.key),
        title,
        description: desc || breadcrumb,
        group: parents.length > 0 ? parents[0] : '页面',
        path: String(item.path || item.key),
        keywords
      }]
    })
  const list = collect(accessibleMenu.value)
  return fuzzyFilter(list, globalSearchKeyword.value, searchFields).slice(0, 16)
})
const searchActionItems = computed<SearchPaletteItem[]>(() => {
  const items: SearchPaletteItem[] = [
    { key: 'action-search-todo', title: '打开我的待办', description: '进入个人待办中心', group: '常用动作', path: '/workbench/todo' },
    { key: 'action-search-approvals', title: '打开我的审批', description: '进入审批处理页', group: '常用动作', path: '/workbench/approvals' },
    { key: 'action-search-chat', title: '打开快捷聊天', description: '查看聊天与协作消息', group: '常用动作', action: 'openQuickChat' },
    { key: 'action-search-chat-todo', title: '打开聊天任务', description: '查看聊天任务与催办清单', group: '常用动作', action: 'openQuickChatTodo' },
    { key: 'action-search-profile', title: '打开个人设置', description: '查看头像、手机号与主题设置', group: '常用动作', action: 'openHeaderSettings' }
  ]
  const accessible = items.filter((item) => !item.path || canOpenPath(item.path))
  return fuzzyFilter(accessible, globalSearchKeyword.value, searchFields)
})
const searchRecentItems = computed<SearchPaletteItem[]>(() => {
  const base = recentVisitItems.value
    .filter((item) => canOpenPath(item.path))
    .map((item) => ({ key: item.key, title: item.title, description: item.path, group: '最近访问', path: item.path }))
  return fuzzyFilter(base, globalSearchKeyword.value, searchFields).slice(0, 8)
})
const breadcrumbs = computed(() => {
  const homeTitle = (userStore.roles || []).includes('SYS_ADMIN')
    ? '系统管理首页'
    : (userStore.roles || []).some((role) => ['DIRECTOR', 'ADMIN'].includes(role)) ? '经营总览' : '我的工作台'
  const titles = route.matched
    .map((r) => r.meta?.title as string | undefined)
    .filter((title) => title && title.length > 0 && title !== '工作台' && title !== homeTitle) as string[]
  return Array.from(new Set([homeTitle, ...titles]))
})

const todayLabel = computed(() => {
  const now = new Date()
  return now.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit', weekday: 'short' })
})

function recentVisitScope() {
  if (userStore.staffInfo?.id) return `id_${String(userStore.staffInfo.id)}`
  if (userStore.staffInfo?.username) return `u_${String(userStore.staffInfo.username)}`
  return 'default'
}

function syncRecentVisits() {
  recentVisitItems.value = loadRecentVisits(recentVisitScope())
}

function recordRecentVisit(path: string) {
  const title = String(route.meta?.title || currentTitle.value || '')
  if (!path || !title || route.meta?.hidden) return
  saveRecentVisit(recentVisitScope(), {
    key: normalizeTabKey(path) || path,
    path,
    title
  })
  syncRecentVisits()
}

function openGlobalSearch() {
  syncRecentVisits()
  globalSearchOpen.value = true
}

function closeGlobalSearch() {
  globalSearchOpen.value = false
}

function handleSearchSelection(item: SearchPaletteItem) {
  closeGlobalSearch()
  globalSearchKeyword.value = ''
  if (item.action === 'openQuickChat') {
    openQuickChat()
    return
  }
  if (item.action === 'openQuickChatTodo') {
    openQuickChatTodoDrawer()
    return
  }
  if (item.action === 'openHeaderSettings') {
    openHeaderSettings()
    return
  }
  if (item.path) {
    router.push(item.path)
  }
}

function handleGlobalShortcut(event: KeyboardEvent) {
  const target = event.target as HTMLElement | null
  const tagName = String(target?.tagName || '').toLowerCase()
  const isTyping =
    tagName === 'input' ||
    tagName === 'textarea' ||
    target?.isContentEditable ||
    target?.closest('.ant-select-selection-search-input')

  if ((event.metaKey || event.ctrlKey) && event.key.toLowerCase() === 'k') {
    event.preventDefault()
    openGlobalSearch()
    return
  }

  if (!isTyping && event.key === '/') {
    event.preventDefault()
    openGlobalSearch()
    return
  }

  if (event.key === 'Escape' && globalSearchOpen.value) {
    event.preventDefault()
    closeGlobalSearch()
  }
}

function handleHeaderQuickTodoClick() {
  router.push('/workbench/todo')
}

function updateViewportState() {
  if (typeof window === 'undefined') return
  const nextIsMobile = window.innerWidth < 992
  isMobileViewport.value = nextIsMobile
  if (!nextIsMobile) {
    mobileSidebarOpen.value = false
  }
}

watch(
  () => route.fullPath,
  (fullPath) => {
    syncRouteTab(route.path, fullPath)
    recordRecentVisit(route.path)
    const path = route.path
    syncOpenKeysForRoute(path)
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
  if (purePath === '/workbench') {
    return '/workbench/overview'
  }
  return workspaceLocation(purePath.split('#')[0])?.path || purePath || text
}

function shouldCacheRouteView(pathLike: string, routeName: unknown, keepAliveMeta?: unknown) {
  if (keepAliveMeta === true) return true
  if (keepAliveMeta === false) return false
  const normalizedPath = normalizeTabKey(pathLike).toLowerCase()
  const normalizedName = String(routeName || '').toLowerCase().replace(/[^a-z0-9]/g, '')
  if (NON_CACHED_ROUTE_PATH_PATTERNS.some((pattern) => pattern.test(normalizedPath))) {
    return false
  }
  if (NON_CACHED_ROUTE_NAME_KEYWORDS.some((keyword) => normalizedName.includes(keyword))) {
    return false
  }
  return true
}

const shouldCacheCurrentRoute = computed(() => shouldCacheRouteView(route.path, route.name, route.meta?.keepAlive))

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
  const isHomeTab = key === normalizeTabKey(resolveDefaultHome(userStore.roles || []))
  routeTabs.value.push({
    key,
    path: fullPath,
    title: currentTabTitle(),
    closable: !isHomeTab
  })
  activeTabKey.value = key
  persistRouteTabs()
}

function pruneTabRefreshSeeds() {
  const allowedKeys = new Set(routeTabs.value.map((item) => item.key))
  Object.keys(tabRefreshSeeds).forEach((key) => {
    if (!allowedKeys.has(key)) {
      delete tabRefreshSeeds[key]
    }
  })
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
  delete tabRefreshSeeds[targetKey]
  pruneTabRefreshSeeds()
  persistRouteTabs()
  if (activeTabKey.value !== targetKey) return
  const fallback = routeTabs.value[idx] || routeTabs.value[idx - 1]
  router.push(fallback?.key || resolveDefaultHome(userStore.roles || []))
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
  pruneTabRefreshSeeds()
  persistRouteTabs()
  closeTabContextMenu()
}

function closeAllTabs() {
  routeTabs.value = routeTabs.value.filter((item) => !item.closable)
  pruneTabRefreshSeeds()
  persistRouteTabs()
  closeTabContextMenu()
  router.push(resolveDefaultHome(userStore.roles || []))
}

function refreshCurrentTab() {
  const tabKey = currentRouteTabKey.value || activeTabKey.value || normalizeTabKey(route.fullPath)
  if (tabKey) {
    tabRefreshSeeds[tabKey] = (tabRefreshSeeds[tabKey] || 0) + 1
  }
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
  pruneTabRefreshSeeds()
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
  pruneTabRefreshSeeds()
  persistRouteTabs()
  ensureActiveTabAvailable()
  closeTabContextMenu()
}

function ensureActiveTabAvailable() {
  const active = activeTabKey.value || route.fullPath
  if (routeTabs.value.some((item) => item.key === active)) return
  const fallback = routeTabs.value[routeTabs.value.length - 1]
  router.push(fallback?.key || resolveDefaultHome(userStore.roles || []))
}

function routeTabsStorageKey() {
  return `layout_route_tabs_v1_${userStorageScope()}`
}

function persistRouteTabs() {
  try {
    const payload = routeTabs.value
      .slice(-MAX_RESTORED_ROUTE_TABS)
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
        closable: normalizeTabKey(String(item?.path || item?.key || '')) !== normalizeTabKey(resolveDefaultHome(userStore.roles || []))
      }))
      .filter((item: any) => !!item.key && !!item.path && canOpenPath(item.path))
      .reduce((acc: Array<{ key: string; path: string; title: string; closable: boolean }>, item: any) => {
        if (acc.some((x) => x.key === item.key)) return acc
        acc.push(item)
        return acc
      }, [])
      .slice(-MAX_RESTORED_ROUTE_TABS)
    pruneTabRefreshSeeds()
  } catch {}
}

function onOpenChange(keys: string[]) {
  const previousKeys = [...openKeys.value]
  openKeys.value = keys
  persistOpenKeys(keys)
  const addedKey = keys.find((key) => !previousKeys.includes(key))
  if (!addedKey) return
  // 展开一级模块或其目录时，自动打开该模块的业务导航（Hub）
  const addedPath =
    resolveMenuPathByKey(addedKey, filteredMenu.value) ||
    (typeof addedKey === 'string' && addedKey.startsWith('/') ? addedKey : '')
  const moduleSeg = addedPath.split('/').filter(Boolean)[0]
  if (!moduleSeg) return
  const moduleHub = `/${moduleSeg}`
  if (normalizeTabKey(route.path) === normalizeTabKey(moduleHub)) return
  router.push(moduleHub)
}

function onMenuClick(info: any) {
  if (typeof info.key === 'string' && info.key.length > 0) {
    const targetPath = resolveMenuPathByKey(info.key, filteredMenu.value)
    const target = targetPath || info.key
    if (normalizeTabKey(route.path) === normalizeTabKey(target)) return
    mobileSidebarOpen.value = false
    router.push(target)
  }
}

function onQuickNotifyClick(item: { route?: string; action?: string }) {
  if (item.action === 'openQuickChat') {
    openQuickChat()
    return
  }
  if (item.action === 'openQuickChatTodo') {
    openQuickChatTodoDrawer()
    return
  }
  if (item.route) router.push(item.route)
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

function syncOpenKeysForRoute(path: string) {
  const trail = findMenuTrail(path)
  if (trail.length <= 1) return
  const routeOpenKeys = trail.slice(0, trail.length - 1).map((item) => item.key)
  openKeys.value = Array.from(new Set([...openKeys.value, ...routeOpenKeys]))
  persistOpenKeys(openKeys.value)
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
  document.documentElement.style.setProperty('--primary', headerSettings.themeColor || '#21705F')
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
    // 旧版本（无 themeColorVersion）默认主题色是海蓝 #1b66d6，与 antd 品牌绿不一致会造成蓝绿混搭；
    // 仅对旧版本存储且值仍为旧默认的情况迁移到品牌绿；v2 起用户主动选择（含海蓝）原样尊重
    const storedTheme = String(parsed?.themeColor || '')
    const isLegacyDefault = !parsed?.themeColorVersion && storedTheme.toLowerCase() === '#1b66d6'
    headerSettings.themeColor = storedTheme && !isLegacyDefault ? storedTheme : '#21705F'
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
    themeColor: headerSettings.themeColor || '#21705F',
    // v2：默认主题色已从海蓝迁移到品牌绿；带版本号保存，避免加载时把用户主动重选的海蓝再次迁移
    themeColorVersion: 2,
    fontScale: Number(headerSettings.fontScale || 100),
    quickNotifyEnabled: headerSettings.quickNotifyEnabled !== false
  }
  localStorage.setItem(headerSettingsStorageKey(), JSON.stringify(payload))
}

function openHeaderSettings() {
  syncHeaderIdentityFromStore()
  syncDepartmentName()
  if (!headerSettings.staffNo) {
    hydrateStaffNo().catch(() => {})
  }
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
  headerSettings.themeColor = '#21705F'
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

function resolveQuickChatWebSocketUrl() {
  const token = getToken()
  if (!token || typeof window === 'undefined') return ''
  const envBase = String((import.meta as any)?.env?.VITE_WS_BASE_URL || '').trim()
  if (envBase) {
    const normalized = envBase.replace(/\/+$/, '')
    return `${normalized}/ws/quick-chat?token=${encodeURIComponent(token)}`
  }
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const host = window.location.host
  return `${protocol}//${host}/ws/quick-chat?token=${encodeURIComponent(token)}`
}

function applyQuickChatRealtimeEvent(eventRow: any) {
  const eventType = String(eventRow?.eventType || '').trim().toUpperCase()
  // 只处理新版协同消息与待办事件：转给聊天组件按游标补拉，并刷新顶栏数字
  if (!eventType.startsWith('CHAT_') && !eventType.startsWith('TODO_')) return
  window.dispatchEvent(new CustomEvent('chat-event', { detail: eventRow }))
  refreshChatCounts().catch(() => {})
  if (eventType === 'CHAT_TASK_ASSIGNED') notification.info({ message: '你有新的聊天待办', description: String(eventRow?.title || '') })
  if (eventType === 'TODO_URGED') notification.warning({ message: '待办催办', description: String(eventRow?.title || '') })
  if (eventType === 'TODO_ASSIGNED') notification.info({ message: '有待办转交给你', description: String(eventRow?.title || '') })
}

function quickChatPreviewTime(value: string) {
  const parsed = dayjs(value)
  if (!parsed.isValid()) return ''
  return parsed.isSame(dayjs(), 'day') ? parsed.format('HH:mm') : parsed.format('MM-DD')
}

function handleQuickChatOpenEvent() {
  openQuickChat()
}

function handleQuickChatWebSocketMessage(raw: string) {
  if (!raw) return
  try {
    const parsed = JSON.parse(raw)
    applyQuickChatRealtimeEvent(parsed)
  } catch {}
}

function scheduleQuickChatWsReconnect() {
  if (quickChatWebSocketManualClose) return
  if (quickChatWsReconnectTimer) {
    window.clearTimeout(quickChatWsReconnectTimer)
    quickChatWsReconnectTimer = undefined
  }
  quickChatWsReconnectAttempt += 1
  const delay = Math.min(3000 + quickChatWsReconnectAttempt * 1000, 12000)
  quickChatWsReconnectTimer = window.setTimeout(() => {
    quickChatWsReconnectTimer = undefined
    connectQuickChatWebSocket()
  }, delay)
}

function connectQuickChatWebSocket() {
  const url = resolveQuickChatWebSocketUrl()
  if (!url) return
  if (quickChatWebSocket && (quickChatWebSocket.readyState === WebSocket.OPEN || quickChatWebSocket.readyState === WebSocket.CONNECTING)) {
    return
  }
  try {
    quickChatWebSocket = new WebSocket(url)
  } catch {
    scheduleQuickChatWsReconnect()
    return
  }
  quickChatWebSocket.onopen = () => {
    quickChatWsReconnectAttempt = 0
    // 连上（含断线重连）后让协同消息按已读游标补拉漏掉的消息
    window.dispatchEvent(new CustomEvent('chat-event', { detail: { eventType: 'CHAT_RECONNECTED' } }))
    refreshChatCounts().catch(() => {})
  }
  quickChatWebSocket.onmessage = (event) => {
    handleQuickChatWebSocketMessage(String(event?.data || ''))
  }
  quickChatWebSocket.onerror = () => {
  }
  quickChatWebSocket.onclose = (event) => {
    const reason = String((event as CloseEvent)?.reason || '')
    const closeCode = Number((event as CloseEvent)?.code || 0)
    quickChatWebSocket = null
    if (closeCode === 1008 || reason.includes('invalid token') || reason.includes('missing token')) {
      quickChatWebSocketManualClose = true
      return
    }
    scheduleQuickChatWsReconnect()
  }
}

function closeQuickChatWebSocket() {
  quickChatWebSocketManualClose = true
  if (quickChatWsReconnectTimer) {
    window.clearTimeout(quickChatWsReconnectTimer)
    quickChatWsReconnectTimer = undefined
  }
  if (quickChatWebSocket) {
    try {
      quickChatWebSocket.close()
    } catch {}
    quickChatWebSocket = null
  }
}

/** 快捷聊天已改为服务端会话（/api/chat）：所有入口统一打开新抽屉 */
function openQuickChat() {
  chatDrawerOpen.value = true
}

function openQuickChatTodoDrawer() {
  router.push({ path: '/workbench/chat', query: { tab: 'tasks' } })
}

onMounted(() => {
  updateViewportState()
  syncRecentVisits()
  hydrateCurrentStaffInfo()
    .then(() => {
      syncDepartmentName()
      loadHeaderSettings()
      loadFinanceReminders()
    })
    .finally(() => {
      restoreRouteTabs()
      syncRouteTab(route.path, route.fullPath)
    })
  window.addEventListener('resize', updateViewportState)
  window.addEventListener('keydown', handleGlobalShortcut)
  window.addEventListener(QUICK_CHAT_OPEN_EVENT, handleQuickChatOpenEvent)
  window.addEventListener('chat-read', onChatRead)
  connectQuickChatWebSocket()
  refreshChatCounts().catch(() => {})
  // shortcut: 推送不可用时每分钟兜底刷新顶栏数字；推送稳定后可去掉
  chatCountTimer = window.setInterval(() => { if (!document.hidden) refreshChatCounts().catch(() => {}) }, 60000)
  document.addEventListener('click', closeTabContextMenu)
})

onBeforeUnmount(() => {
  closeQuickChatWebSocket()
  window.removeEventListener('resize', updateViewportState)
  window.removeEventListener('keydown', handleGlobalShortcut)
  window.removeEventListener(QUICK_CHAT_OPEN_EVENT, handleQuickChatOpenEvent)
  window.removeEventListener('chat-read', onChatRead)
  if (chatCountTimer) window.clearInterval(chatCountTimer)
  document.removeEventListener('click', closeTabContextMenu)
})
</script>

<style scoped>
.app-layout {
  height: 100vh;
  background:
    linear-gradient(180deg, rgba(33, 112, 95, 0.03) 0%, rgba(33, 112, 95, 0) 200px),
    var(--bg);
  overflow: hidden;
}

.mobile-sider-backdrop {
  position: fixed;
  inset: 0;
  z-index: 35;
  background: rgba(15, 40, 64, 0.18);
  backdrop-filter: blur(2px);
}

.sider-edge-sensor {
  position: fixed;
  top: 0;
  left: 0;
  width: 14px;
  height: 100vh;
  z-index: 120;
  background: transparent;
}

.app-sider {
  background: #fcfcfa;
  border-right: 1px solid var(--border-soft);
  height: 100vh;
  position: sticky;
  top: 0;
  left: 0;
  box-shadow: 1px 0 2px rgba(34, 51, 46, 0.04);
  z-index: 121;
  transition: all 0.22s ease;
}

.app-sider.is-peek-open {
  box-shadow: 0 6px 16px rgba(34, 51, 46, 0.12);
}

.app-sider.is-manual-collapsed:not(.is-peek-open) {
  border-right-color: transparent;
  box-shadow: none;
}

.app-sider :deep(.ant-layout-sider-children) {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 18px 18px;
  color: var(--ink);
  border-bottom: 1px solid var(--border-soft);
  background: #ffffff;
}

.sider-toggle-btn {
  margin-right: 12px;
}

.logo {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: var(--primary-soft);
  display: grid;
  place-items: center;
  box-shadow: var(--shadow-xs);
  overflow: hidden;
  flex-shrink: 0;
}

.logo-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  display: block;
}

.title {
  font-weight: 700;
  font-size: 16px;
}

.subtitle {
  font-size: 12px;
  color: var(--muted-2);
  letter-spacing: 0.04em;
}

.side-menu {
  background: transparent !important;
  border-inline-end: none !important;
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  overflow-x: hidden;
  padding-bottom: 10px;
}

.side-menu :deep(.ant-menu) {
  background: transparent !important;
  color: var(--muted) !important;
}

.side-menu :deep(.ant-menu-sub) {
  background: var(--surface-3) !important;
}

.side-menu .ant-menu-item,
.side-menu .ant-menu-submenu-title {
  border-radius: 10px;
  margin: 3px 12px;
  height: auto;
  min-height: 48px;
  display: flex;
  align-items: center;
  line-height: 1.35;
  padding-top: 10px;
  padding-bottom: 10px;
  color: var(--muted) !important;
  font-weight: 600;
}

.side-menu .ant-menu-item:hover,
.side-menu .ant-menu-submenu-title:hover {
  color: var(--ink) !important;
  background: var(--bg-2) !important;
}

.side-menu :deep(.ant-menu-submenu-selected > .ant-menu-submenu-title),
.side-menu :deep(.ant-menu-submenu-open > .ant-menu-submenu-title) {
  color: var(--primary-strong) !important;
}

.side-menu .ant-menu-item-selected,
.side-menu :deep(.ant-menu-submenu-selected .ant-menu-item-selected) {
  background: var(--primary-soft) !important;
  box-shadow: inset 3px 0 0 0 var(--primary);
  color: var(--primary-strong) !important;
  font-weight: 700;
}

.side-menu :deep(.ant-menu-item-selected::after) {
  display: none !important;
}

.side-menu :deep(.ant-menu-submenu-arrow),
.side-menu :deep(.ant-menu-item-icon),
.side-menu :deep(.ant-menu-submenu .ant-menu-title-content),
.side-menu :deep(.ant-menu-title-content) {
  color: inherit !important;
}

.side-menu :deep(.ant-menu-title-content) {
  flex: 1;
  min-width: 0;
  overflow: visible;
}

.side-menu :deep(.ant-menu-item .side-menu-label),
.side-menu :deep(.ant-menu-submenu-title .side-menu-label) {
  display: -webkit-box;
  overflow: hidden;
  white-space: normal;
  word-break: break-word;
  line-height: 1.35;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.app-main {
  background: var(--bg);
  height: 100vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.app-header {
  position: sticky;
  top: 0;
  z-index: 10;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--border-soft);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  min-height: 72px;
  box-shadow: var(--shadow-xs);
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.header-left-top {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.page-title {
  font-weight: 700;
  font-size: 20px;
  color: var(--ink);
}

.breadcrumb {
  font-size: 12px;
  color: var(--muted);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.header-status-cluster,
.header-action-cluster {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.today-label {
  padding: 6px 12px;
  font-size: 12px;
  border-radius: 999px;
  color: var(--primary-strong);
  background: var(--primary-soft);
  border: 1px solid rgba(var(--primary-rgb), 0.14);
}

.system-name {
  color: var(--muted);
  font-size: 12px;
  max-width: 180px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-link {
  color: var(--ink);
}

.user-entry {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 4px 10px 4px 4px;
  border-radius: 999px;
  background: #ffffff;
  border: 1px solid var(--border);
}

.system-status {
  font-size: 12px;
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
  padding: 10px 24px 0;
  background: rgba(255, 255, 255, 0.74);
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
  background: #ffffff;
  border-color: var(--border);
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
  background: rgba(var(--primary-rgb), 0.12);
  border-radius: 6px;
  box-shadow: inset 0 0 0 1px rgba(var(--primary-rgb), 0.24);
}

.tab-context-menu {
  position: fixed;
  z-index: 1200;
  min-width: 140px;
  background: #fff;
  border: 1px solid var(--border);
  border-radius: 12px;
  box-shadow: var(--shadow-md);
}

.app-content {
  padding: 24px 24px 26px;
  flex: 1;
  min-height: 0;
  overflow: auto;
}

.hint-text {
  font-size: 12px;
  color: var(--muted);
}

@media (max-width: 992px) {
  .app-header {
    padding: 10px 14px;
    align-items: flex-start;
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
    flex: 1;
    min-height: 0;
    overflow: auto;
  }
}

.notify-menu {
  width: min(380px, calc(100vw - 24px));
}

.notify-menu__reminder,
.notify-menu__message {
  height: auto !important;
  padding-top: 6px !important;
  padding-bottom: 6px !important;
  line-height: 1.4 !important;
}

.notify-message {
  display: flex;
  align-items: center;
  gap: 9px;
  min-width: 0;
}

.notify-message__avatar {
  display: grid;
  place-items: center;
  flex: 0 0 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--primary-soft);
  color: var(--primary-strong);
  font-size: 12px;
  font-weight: 700;
}

.notify-message__body {
  display: grid;
  gap: 2px;
  flex: 1;
  min-width: 0;
}

.notify-message__meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.notify-message__meta strong,
.notify-message__body small {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notify-message__meta strong {
  flex: 1;
  min-width: 0;
  font-weight: 600;
}

.notify-message__meta time,
.notify-message__body small {
  color: var(--text-tertiary, #7c8982);
  font-size: 11px;
}

.notify-message__unread {
  flex: 0 0 auto;
  min-width: 18px;
  padding: 1px 5px;
  border-radius: 999px;
  background: #ef4444;
  color: #fff;
  font-size: 10px;
  line-height: 16px;
  text-align: center;
  white-space: nowrap;
}

.notify-reminder {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.notify-reminder__text {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.notify-reminder__text strong {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 500;
}

.notify-reminder__text small {
  color: var(--text-tertiary, #999);
  font-size: 12px;
}
</style>
