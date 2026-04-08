<template>
  <div
    v-if="manualCollapsed && !sidebarPeekOpen"
    class="sider-edge-sensor"
    @mouseenter="openSidebarPeek"
  ></div>
  <a-layout class="app-layout">
    <a-layout-sider
      :collapsed="siderCollapsed"
      collapsible
      :trigger="null"
      :width="244"
      :collapsed-width="0"
      class="app-sider"
      :class="{ 'is-peek-open': sidebarPeekOpen, 'is-manual-collapsed': manualCollapsed }"
      @mouseleave="handleSidebarMouseLeave"
    >
      <div class="brand">
        <div class="logo">智</div>
        <div class="brand-text" v-if="!siderCollapsed">
          <div class="title">龟峰颐养</div>
          <div class="subtitle">运营管理中台</div>
        </div>
      </div>
      <a-menu
        theme="light"
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
          <div class="header-left-top">
            <a-button size="small" class="sider-toggle-btn" @click="toggleSidebar">
              {{ manualCollapsed ? '展开导航' : '收起导航' }}
            </a-button>
            <div class="page-title">{{ currentTitle || '工作台' }}</div>
          </div>
          <a-breadcrumb class="breadcrumb">
            <a-breadcrumb-item v-for="(bc, idx) in breadcrumbs" :key="idx">
              {{ bc }}
            </a-breadcrumb-item>
          </a-breadcrumb>
        </div>
        <div class="header-right">
          <div class="header-status-cluster">
            <span class="today-label">{{ todayLabel }}</span>
            <a-badge status="processing" text="系统运行中" class="system-status" />
            <span class="system-name">龟峰颐养中心智慧养老管理平台</span>
          </div>
          <div class="header-action-cluster">
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
                <a-button size="small">通知</a-button>
              </a-badge>
              <template #overlay>
                <a-menu>
                  <a-menu-item v-for="item in quickNotifyItems" :key="item.title" @click="onQuickNotifyClick(item)">
                    {{ item.title }}
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
            <a-badge :count="quickChatUnreadCount" size="small">
              <a-button size="small" @click="openQuickChat">聊天</a-button>
            </a-badge>
            <a-badge :count="quickChatTodoPendingCount" size="small">
              <a-button size="small" @click="openQuickChatTodoDrawer">待办</a-button>
            </a-badge>
            <a-tag v-if="quickChatPressureTag" :color="quickChatPressureTag.color">{{ quickChatPressureTag.text }}</a-tag>
          </div>
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
                <a-menu-item @click="router.push('/hr/profile/account-access')">账号与赋权</a-menu-item>
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
          <keep-alive :max="aliveViewMax">
            <component v-if="shouldCacheCurrentRoute" :is="Component" :key="viewRenderKey" />
          </keep-alive>
          <component v-if="!shouldCacheCurrentRoute" :is="Component" :key="viewRenderKey" />
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
        人事新建账号与监管关系：请前往“账号与赋权”页面完成员工账号、初始密码与领导设置。
      </div>
    </a-form>
  </a-drawer>

  <a-drawer
    v-model:open="quickChatOpen"
    title="快捷聊天"
    :width="quickChatDrawerWidth"
    placement="right"
    class="quick-chat-drawer"
  >
      <a-space wrap class="quick-chat-top-actions">
        <a-button type="primary" @click="openCreateQuickChatRoom">发起群聊申请</a-button>
        <a-button :disabled="!activeQuickChatRoom" @click="openInviteQuickChat">邀请部门/成员</a-button>
        <a-button :disabled="!activeQuickChatRoom" @click="openRenameQuickChatRoom">重命名会话</a-button>
        <a-select
          v-model:value="activeQuickChatNotifyMode"
          size="small"
          :disabled="!activeQuickChatRoom"
          :options="quickChatNotifyModeOptions"
          style="width: 150px;"
        />
        <a-button :disabled="!activeQuickChatRoom" @click="togglePinActiveQuickChatRoom">
          {{ activeQuickChatRoom?.pinned ? '取消置顶' : '置顶会话' }}
        </a-button>
        <a-button :disabled="!activeQuickChatRoom" @click="toggleArchiveActiveRoom">
          {{ activeQuickChatRoom && isRoomArchivedByCurrentUser(activeQuickChatRoom) ? '恢复会话' : '归档会话' }}
        </a-button>
        <a-button size="small" :loading="quickChatCloudSyncing" @click="forceQuickChatCloudSync">立即同步</a-button>
        <a-button
          v-if="quickChatFanoutQueuedPayload"
          size="small"
          :loading="quickChatFanoutPending"
          @click="retryQuickChatFanoutNow"
        >
          重试分发
        </a-button>
        <a-button :disabled="!activeQuickChatRoom" @click="nudgeActiveQuickChatRoom">催办提醒</a-button>
        <a-button :disabled="!activeQuickChatRoom" @click="exportActiveQuickChatHistory">导出会话</a-button>
        <a-badge :count="quickChatTodoPendingCount" size="small">
          <a-button @click="openQuickChatTodoDrawer">聊天待办</a-button>
        </a-badge>
        <a-button danger :disabled="!activeQuickChatRoom" @click="exitActiveQuickChatRoom">退出会话</a-button>
        <a-tag :color="presenceStatus.color">{{ presenceStatus.label }}</a-tag>
        <a-tag :color="quickChatWsStatus.color">{{ quickChatWsStatus.text }}</a-tag>
        <a-tag :color="quickChatCloudStatus.color">{{ quickChatCloudStatus.text }}</a-tag>
        <a-tag :color="quickChatFanoutStatus.color">{{ quickChatFanoutStatus.text }}</a-tag>
        <a-tag v-if="quickChatDnd" color="red">免打扰已开启</a-tag>
        <a-tag v-if="quickChatPressureTag" :color="quickChatPressureTag.color">{{ quickChatPressureTag.text }}</a-tag>
        <a-button
          v-if="canUndoQuickChatArchive"
          size="small"
          type="link"
          @click="undoLastArchivedQuickChatRoom"
        >
          撤销删除
        </a-button>
      </a-space>
    <div class="quick-chat-layout">
      <div class="quick-chat-room-list">
        <div class="quick-chat-room-search">
          <a-input v-model:value="quickChatKeyword" allow-clear placeholder="搜索会话名/成员名" />
          <a-segmented v-model:value="quickChatRoomListMode" :options="quickChatRoomListModeOptions" size="small" style="margin-top: 8px; width: 100%;" />
          <a-checkbox v-model:checked="quickChatUnreadOnly" style="margin-top: 6px;">仅看未读</a-checkbox>
          <div class="quick-chat-batch-bar">
            <a-button size="small" @click="toggleQuickChatBatchMode">{{ quickChatBatchMode ? '退出批量' : '批量管理' }}</a-button>
            <template v-if="quickChatBatchMode">
              <a-button size="small" @click="selectAllFilteredRooms">全选</a-button>
              <a-button size="small" @click="clearBatchSelection">清空</a-button>
              <a-button size="small" :disabled="!quickChatBatchSelectedIds.length" @click="batchMarkSelectedAsRead">批量已读</a-button>
              <a-button size="small" :disabled="!quickChatBatchSelectedIds.length" @click="batchToggleArchiveSelected">批量归档/恢复</a-button>
            </template>
          </div>
        </div>
        <a-list :data-source="filteredQuickChatRooms" size="small" :locale="{ emptyText: '暂无会话，先发起一个群聊' }">
          <template #renderItem="{ item }">
            <a-list-item
              class="quick-chat-room-item"
              :class="{ active: item.id === activeQuickChatRoomId }"
              @click="selectQuickChatRoom(item.id)"
            >
              <a-checkbox
                v-if="quickChatBatchMode"
                class="quick-chat-room-check"
                :checked="quickChatBatchSelectedIds.includes(item.id)"
                @click.stop
                @change="(event: any) => toggleBatchSelectRoom(item.id, event?.target?.checked === true)"
              />
              <a-list-item-meta :description="quickChatLastMessageText(item)">
                <template #title>
                  <a-space size="small">
                    <span>{{ item.name }}</span>
                    <a-tag v-if="item.pinned" color="gold">置顶</a-tag>
                    <a-tag v-if="quickChatRoomSlaTag(item)" :color="quickChatRoomSlaTag(item)?.color">
                      {{ quickChatRoomSlaTag(item)?.text }}
                    </a-tag>
                    <a-tag v-if="item.unreadCount" color="red">{{ item.unreadCount }}</a-tag>
                  </a-space>
                </template>
              </a-list-item-meta>
            </a-list-item>
          </template>
        </a-list>
      </div>
      <div class="quick-chat-main">
        <div v-if="activeQuickChatRoom?.notice" class="quick-chat-notice">
          <strong>群公告：</strong>{{ activeQuickChatRoom.notice }}
        </div>
        <div v-if="quickChatSyncConflict.visible" class="quick-chat-sync-conflict">
          <div class="quick-chat-sync-conflict-title">{{ quickChatSyncConflictTitle }}</div>
          <div class="quick-chat-sync-conflict-desc">{{ quickChatSyncConflictDesc }}</div>
          <a-space size="small">
            <a-button size="small" type="primary" @click="resolveQuickChatConflictUseLocal">以本地覆盖云端</a-button>
            <a-button size="small" @click="resolveQuickChatConflictUseCloud">使用云端覆盖本地</a-button>
            <a-button size="small" type="link" @click="dismissQuickChatConflict">稍后处理</a-button>
          </a-space>
        </div>
        <div class="quick-chat-header">
          <strong>{{ activeQuickChatRoom?.name || '请选择会话' }}</strong>
          <a-space size="small">
            <span class="hint-text">{{ activeQuickChatRoom ? `成员 ${activeQuickChatRoom.memberIds.length} 人` : '' }}</span>
            <a-button size="small" :disabled="!activeQuickChatRoom" @click="openQuickChatMembers">成员</a-button>
            <a-input
              v-model:value="quickChatMessageKeyword"
              allow-clear
              size="small"
              placeholder="搜索本会话消息"
              style="width: 180px;"
            />
            <a-button size="small" :disabled="!activeQuickChatRoom" @click="openNoticeEditor">编辑公告</a-button>
            <a-button size="small" :disabled="!activeQuickChatRoom" @click="markActiveRoomRead">全部已读</a-button>
            <a-button size="small" danger :disabled="!activeQuickChatRoom" @click="removeActiveQuickChatRoom">删除会话（可恢复）</a-button>
          </a-space>
        </div>
        <div ref="quickChatMessagesRef" class="quick-chat-messages">
          <a-empty v-if="!filteredActiveQuickChatMessages.length" description="暂无匹配消息" />
          <div
            v-for="msg in filteredActiveQuickChatMessages"
            :key="msg.id"
            class="quick-chat-message"
            :class="{ self: isCurrentQuickChatIdentity(msg.senderId) }"
            :data-message-id="msg.id"
          >
            <div class="quick-chat-message-meta">
              <span>{{ msg.senderName }}</span>
              <a-space size="small">
                <span>{{ msg.timeText }}</span>
                <a-button
                  v-if="canRecallMessage(msg)"
                  type="link"
                  size="small"
                  class="quick-chat-recall-btn"
                  @click="recallQuickChatMessage(msg.id)"
                >
                  撤回
                </a-button>
                <a-button
                  v-if="!msg.recalled"
                  type="link"
                  size="small"
                  class="quick-chat-recall-btn"
                  @click="openForwardMessage(msg.id)"
                >
                  转发
                </a-button>
                <a-dropdown trigger="click">
                  <a-button type="link" size="small" class="quick-chat-recall-btn">更多</a-button>
                  <template #overlay>
                    <a-menu>
                      <a-menu-item @click="copyQuickChatMessage(msg)">复制</a-menu-item>
                      <a-menu-item v-if="!msg.recalled" @click="openForwardMessage(msg.id)">转发</a-menu-item>
                      <a-menu-item v-if="!msg.recalled" @click="addMessageToQuickChatTodo(msg)">设为待办</a-menu-item>
                      <a-menu-item v-if="canRecallMessage(msg)" @click="recallQuickChatMessage(msg.id)">撤回</a-menu-item>
                    </a-menu>
                  </template>
                </a-dropdown>
              </a-space>
            </div>
            <div class="quick-chat-message-body">
              <template v-if="msg.recalled">
                <span class="quick-chat-recalled">该消息已撤回</span>
              </template>
              <template v-if="msg.kind === 'IMAGE' && msg.fileUrl">
                <div class="quick-chat-image-wrap">
                  <a-image :src="msg.fileUrl" :alt="msg.fileName || '图片'" class="quick-chat-image" />
                  <div v-if="msg.uploadStatus === 'UPLOADING'" class="quick-chat-upload-mask">上传中...</div>
                  <div v-else-if="msg.uploadStatus === 'FAILED'" class="quick-chat-upload-mask quick-chat-upload-mask-failed">上传失败</div>
                </div>
              </template>
              <template v-else-if="msg.kind === 'FILE'">
                <span v-if="msg.uploadStatus === 'UPLOADING'" class="quick-chat-uploading-file">
                  {{ msg.fileName || '附件' }} 上传中...
                </span>
                <span v-else-if="msg.uploadStatus === 'FAILED'" class="quick-chat-upload-failed-file">
                  {{ msg.fileName || '附件' }} 上传失败，请重试
                </span>
                <a v-else :href="msg.fileUrl || '#'" target="_blank" rel="noreferrer">{{ msg.fileName || '附件' }}</a>
              </template>
              <template v-else-if="!msg.recalled">
                {{ msg.content }}
              </template>
            </div>
            <div v-if="isCurrentQuickChatIdentity(msg.senderId) && !msg.recalled" class="quick-chat-read-status">
              已读 {{ quickChatMessageReadCount(msg) }}/{{ activeQuickChatRoom?.memberIds.length || 0 }}
            </div>
          </div>
        </div>
        <div class="quick-chat-input">
          <a-textarea
            v-model:value="quickChatDraft"
            :rows="3"
            :disabled="!activeQuickChatRoom"
            placeholder="输入消息...支持 @姓名 或 @all（Enter 发送，Shift+Enter 换行）"
            @keydown="onQuickChatInputKeydown"
          />
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
    :title="quickChatRoomEditorMode === 'create' ? '发起快捷群聊申请' : quickChatRoomEditorMode === 'rename' ? '重命名会话' : '邀请部门/成员'"
    width="620"
    @ok="submitQuickChatRoomEditor"
  >
    <a-form layout="vertical">
      <a-form-item label="群聊名称" required>
        <a-input v-model:value="quickChatRoomForm.name" placeholder="例如：行政部-护理部协同群" />
      </a-form-item>
      <a-form-item v-if="quickChatRoomEditorMode !== 'rename'" label="邀请部门（可多选）">
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
      <a-form-item v-if="quickChatRoomEditorMode !== 'rename'" label="邀请成员（可多选）">
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
      <a-form-item v-if="quickChatRoomEditorMode !== 'rename'" label="申请说明">
        <a-textarea v-model:value="quickChatRoomForm.applyRemark" :rows="2" placeholder="例如：用于跨部门审批沟通与资料传输" />
      </a-form-item>
    </a-form>
  </a-modal>

  <a-modal
    v-model:open="quickChatNoticeOpen"
    title="编辑群公告"
    width="560"
    @ok="submitQuickChatNotice"
  >
    <a-form layout="vertical">
      <a-form-item label="公告内容">
        <a-textarea v-model:value="quickChatNoticeDraft" :rows="4" maxlength="200" show-count placeholder="请输入群公告，最多200字" />
      </a-form-item>
    </a-form>
  </a-modal>

  <a-drawer
    v-model:open="quickChatMembersOpen"
    title="群成员"
    width="360"
    placement="right"
  >
    <a-list :data-source="activeQuickChatMemberRows" :locale="{ emptyText: '暂无成员' }" size="small">
      <template #renderItem="{ item }">
        <a-list-item>
          <a-space>
            <a-tag :color="item.statusColor">{{ item.statusLabel }}</a-tag>
            <span>{{ item.name }}</span>
          </a-space>
          <template #actions>
            <a-button
              size="small"
              type="link"
              :disabled="isCurrentQuickChatIdentity(item.id)"
              @click="quickMentionMember(item.name)"
            >
              @提醒
            </a-button>
          </template>
        </a-list-item>
      </template>
    </a-list>
  </a-drawer>

  <a-modal
    v-model:open="quickChatForwardOpen"
    title="转发消息"
    width="520"
    @ok="submitForwardMessage"
  >
    <a-form layout="vertical">
      <a-form-item label="目标会话" required>
        <a-select
          v-model:value="quickChatForwardTargetRoomId"
          show-search
          :filter-option="false"
          :options="quickChatForwardRoomOptions"
          placeholder="请选择转发目标会话"
          @search="() => {}"
        />
      </a-form-item>
      <a-form-item label="附言（可选）">
        <a-input v-model:value="quickChatForwardRemark" maxlength="80" placeholder="例如：请尽快确认这条信息" />
      </a-form-item>
    </a-form>
  </a-modal>

  <a-drawer
    v-model:open="quickChatTodoOpen"
    title="聊天待办"
    width="420"
    placement="right"
  >
    <div class="quick-chat-todo-summary">
      <a-segmented v-model:value="quickChatTodoScopeMode" :options="quickChatTodoScopeModeOptions" size="small" style="width: 100%; margin-bottom: 8px;" />
      <a-segmented v-model:value="quickChatTodoViewMode" :options="quickChatTodoViewModeOptions" size="small" style="width: 100%;" />
      <a-progress :percent="quickChatTodoCompletionRate" size="small" :status="quickChatTodoCompletionRate < 100 ? 'active' : 'success'" style="margin-top: 8px;" />
      <a-space style="margin-top: 8px;">
        <a-switch v-model:checked="quickChatTodoAutoEscalationEnabled" size="small" @change="persistQuickChatTodoAutomation" />
        <span class="hint-text">自动升级催办（逾期4h/24h）</span>
      </a-space>
      <a-space style="margin-top: 8px;" size="small">
        <span class="hint-text">升级阈值：</span>
        <a-input-number v-model:value="quickChatTodoEscalationHours.level1" :min="1" :max="72" size="small" style="width: 76px;" @change="onEscalationHoursChange" />
        <span class="hint-text">h</span>
        <span class="hint-text">/</span>
        <a-input-number v-model:value="quickChatTodoEscalationHours.level2" :min="2" :max="168" size="small" style="width: 76px;" @change="onEscalationHoursChange" />
        <span class="hint-text">h</span>
      </a-space>
      <a-space style="margin-top: 8px;" size="small">
        <a-tag color="red">高优 {{ quickChatTodoPriorityStats.high }}</a-tag>
        <a-tag color="orange">中优 {{ quickChatTodoPriorityStats.medium }}</a-tag>
        <a-tag color="blue">低优 {{ quickChatTodoPriorityStats.low }}</a-tag>
      </a-space>
      <a-space style="margin-top: 8px;" size="small">
        <a-tag color="geekblue">执行压力 {{ quickChatExecutionOverview.totalPressure }}</a-tag>
        <a-tag color="volcano">待办 {{ quickChatExecutionOverview.todoPending }}</a-tag>
        <a-tag color="red" v-if="quickChatExecutionOverview.todoOverdue">逾期 {{ quickChatExecutionOverview.todoOverdue }}</a-tag>
        <a-tag color="purple">未读会话 {{ quickChatExecutionOverview.unreadChats }}</a-tag>
      </a-space>
    </div>
    <a-space class="quick-chat-todo-toolbar" wrap>
      <a-button size="small" type="primary" @click="openQuickChatTodoEditorManual">新增待办</a-button>
      <a-button size="small" :disabled="!quickChatTodoItems.length" @click="clearCompletedQuickChatTodo">清理已完成</a-button>
      <a-button size="small" :disabled="!quickChatTodoOverdueCount" @click="nudgeOverdueQuickChatTodos">催办逾期待办</a-button>
      <a-button size="small" :disabled="!quickChatTodoOverdueCount" @click="escalateOverdueQuickChatTodos">升级逾期待办</a-button>
      <a-button size="small" @click="openQuickChatTodoWeeklySummary">周报摘要</a-button>
      <a-button size="small" :disabled="!quickChatTodoItems.length" @click="exportQuickChatTodoReport">导出看板</a-button>
      <a-tag color="blue">待完成 {{ quickChatTodoPendingCount }}</a-tag>
      <a-tag v-if="quickChatTodoOverdueCount > 0" color="red">逾期 {{ quickChatTodoOverdueCount }}</a-tag>
    </a-space>
    <a-list :data-source="filteredQuickChatTodoItems" :locale="{ emptyText: '暂无聊天待办' }" size="small" class="quick-chat-todo-list">
      <template #renderItem="{ item }">
        <a-list-item class="quick-chat-todo-item">
          <div class="quick-chat-todo-card">
            <div class="quick-chat-todo-head">
              <div class="quick-chat-todo-title-wrap">
                <span class="quick-chat-todo-title" :class="{ 'quick-chat-todo-done': item.done }">{{ item.content }}</span>
              </div>
              <div class="quick-chat-todo-tags">
                <a-tag :color="quickChatTodoPriorityColor(item.priority)">{{ quickChatTodoPriorityLabel(item.priority) }}</a-tag>
                <a-tag v-if="!item.done && isQuickChatTodoOverdue(item)" color="red">已逾期</a-tag>
                <a-tag v-if="!item.done && quickChatTodoEscalationTag(item)" :color="quickChatTodoEscalationTag(item)?.color">
                  {{ quickChatTodoEscalationTag(item)?.text }}
                </a-tag>
                <a-tag v-if="item.calendarSynced" color="cyan">已同步日历</a-tag>
              </div>
            </div>
            <div class="quick-chat-todo-desc">{{ quickChatTodoDescription(item) }}</div>
            <div class="quick-chat-todo-actions">
              <a-button size="small" type="link" @click="jumpToQuickChatTodo(item.id)">定位</a-button>
              <a-button
                size="small"
                type="link"
                :disabled="item.done"
                @click="nudgeQuickChatTodo(item.id)"
              >
                催办
              </a-button>
              <a-button
                size="small"
                type="link"
                :disabled="item.done || !isQuickChatTodoOverdue(item)"
                @click="escalateQuickChatTodo(item.id)"
              >
                升级
              </a-button>
              <a-button
                size="small"
                type="link"
                :disabled="item.done || !item.dueAt"
                @click="syncQuickChatTodoToCalendar(item.id)"
              >
                同步日历
              </a-button>
              <a-button size="small" type="link" @click="openQuickChatTodoEditor(item.id)">编辑</a-button>
              <a-button size="small" type="link" @click="toggleQuickChatTodoDone(item.id)">{{ item.done ? '重开' : '完成' }}</a-button>
              <a-button size="small" type="link" danger @click="removeQuickChatTodo(item.id)">删除</a-button>
            </div>
          </div>
        </a-list-item>
      </template>
    </a-list>
    <div class="quick-chat-todo-rank">
      <div class="hint-text">责任人待办排行（Top 5）</div>
      <a-space wrap size="small" style="margin-top: 6px;">
        <a-tag v-for="row in quickChatTodoAssigneeRank" :key="row.name" :color="row.overdue ? 'red' : 'blue'">
          {{ row.name }}：{{ row.pending }}（逾期{{ row.overdue }}）
        </a-tag>
      </a-space>
    </div>
  </a-drawer>

  <a-modal
    v-model:open="quickChatTodoEditorOpen"
    :title="quickChatTodoEditorMode === 'create' ? '新增聊天待办' : '编辑聊天待办'"
    width="560"
    @ok="submitQuickChatTodoEditor"
  >
    <a-form layout="vertical">
      <a-form-item label="待办内容" required>
        <a-textarea v-model:value="quickChatTodoForm.content" :rows="3" maxlength="180" show-count placeholder="请输入待办内容" />
      </a-form-item>
      <a-form-item label="责任人">
        <a-select
          v-model:value="quickChatTodoForm.assigneeId"
          show-search
          allow-clear
          :options="quickChatTodoAssigneeOptions"
          placeholder="可选，不选默认本人"
        />
      </a-form-item>
      <a-space style="width: 100%;" align="start">
        <a-form-item label="优先级" style="flex: 1;">
          <a-select v-model:value="quickChatTodoForm.priority" :options="quickChatTodoPriorityOptions" />
        </a-form-item>
        <a-form-item label="截止规则" style="flex: 1;">
          <a-select v-model:value="quickChatTodoForm.dueRule" :options="quickChatTodoDueRuleOptions" />
        </a-form-item>
      </a-space>
    </a-form>
  </a-modal>

  <a-modal
    v-model:open="quickChatTodoWeeklyOpen"
    title="聊天待办周报摘要"
    width="560"
    :footer="null"
  >
    <a-descriptions bordered :column="2" size="small">
      <a-descriptions-item label="统计周期">{{ quickChatTodoWeeklyStats.rangeText }}</a-descriptions-item>
      <a-descriptions-item label="新增待办">{{ quickChatTodoWeeklyStats.created }}</a-descriptions-item>
      <a-descriptions-item label="完成待办">{{ quickChatTodoWeeklyStats.completed }}</a-descriptions-item>
      <a-descriptions-item label="完成率">{{ quickChatTodoWeeklyStats.completionRate }}%</a-descriptions-item>
      <a-descriptions-item label="逾期未完成">{{ quickChatTodoWeeklyStats.overduePending }}</a-descriptions-item>
      <a-descriptions-item label="升级催办">{{ quickChatTodoWeeklyStats.escalated }}</a-descriptions-item>
    </a-descriptions>
    <a-space style="margin-top: 12px;">
      <a-button type="primary" @click="exportQuickChatTodoWeeklySummary">导出周报</a-button>
    </a-space>
  </a-modal>
</template>

<script setup lang="ts">
import { computed, h, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import type { UploadProps } from 'ant-design-vue'
import dayjs from 'dayjs'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { getMenuTree } from './menu'
import { getMe } from '../api/auth'
import { getStaffPage } from '../api/rbac'
import { updateStaff } from '../api/staff'
import { createOaTask, fanoutQuickChatState, getQuickChatState, publishQuickChatEventBatch, saveQuickChatState, uploadOaFile } from '../api/oa'
import { useDepartmentOptions } from '../composables/useDepartmentOptions'
import { useStaffOptions } from '../composables/useStaffOptions'
import { canBeDirectLeader, canBeIndirectLeader, ensureSupervisorOrder } from '../utils/supervisor'
import { emitLiveSync, subscribeLiveSync, type LiveSyncPayload } from '../utils/liveSync'
import { getToken } from '../utils/auth'

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
const siderCollapsed = computed(() => manualCollapsed.value && !sidebarPeekOpen.value)
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const openKeys = ref<string[]>([])
const routeTabsWrapRef = ref<HTMLElement | null>(null)
const routeTabs = ref<Array<{ key: string; path: string; title: string; closable: boolean }>>([])
const activeTabKey = ref('')
const tabRefreshSeeds = reactive<Record<string, number>>({})
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
    { title: '发起快捷聊天', action: 'openQuickChat' },
    { title: `聊天待办（${quickChatTodoPendingCount.value}${quickChatTodoOverdueCount.value ? `/逾期${quickChatTodoOverdueCount.value}` : ''}）`, action: 'openQuickChatTodo' },
    { title: `待办完成率（${quickChatTodoCompletionRate.value}%）`, action: 'openQuickChatTodo' },
    { title: `执行总览（${quickChatExecutionOverview.value.totalPressure}）`, action: 'openQuickChatTodo' },
    { title: '我的待办', route: '/workbench/todo' },
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

function toggleSidebar() {
  manualCollapsed.value = !manualCollapsed.value
  if (!manualCollapsed.value) {
    sidebarPeekOpen.value = false
  }
}

function openSidebarPeek() {
  if (!manualCollapsed.value) return
  sidebarPeekOpen.value = true
}

function handleSidebarMouseLeave() {
  if (!manualCollapsed.value) return
  sidebarPeekOpen.value = false
}

type QuickChatMessage = {
  id: string
  senderId: string
  senderName: string
  kind: 'TEXT' | 'IMAGE' | 'FILE' | 'SYSTEM'
  content: string
  fileName?: string
  fileUrl?: string
  uploadStatus?: 'UPLOADING' | 'FAILED'
  timeText: string
  createdAt: string
  recalled?: boolean
  readByUser?: Record<string, boolean>
}

type QuickChatRoom = {
  id: string
  name: string
  departmentIds: string[]
  memberIds: string[]
  messages: QuickChatMessage[]
  unreadCount: number
  unreadByUser?: Record<string, number>
  pinned?: boolean
  notificationMode?: 'ALL' | 'MENTION' | 'MUTE'
  notice?: string
  archivedByUser?: Record<string, boolean>
  createdAt: string
  updatedAt: string
}

type QuickChatTodoItem = {
  id: string
  roomId: string
  roomName: string
  messageId: string
  content: string
  creatorId?: string
  creatorName?: string
  assigneeId?: string
  assigneeName?: string
  priority: 'HIGH' | 'MEDIUM' | 'LOW'
  dueAt?: string
  dueAtText?: string
  escalationLevel?: 0 | 1 | 2
  lastEscalatedAt?: string
  calendarSynced?: boolean
  calendarTaskId?: string
  doneAt?: string
  createdAt: string
  createdAtText: string
  done: boolean
}

const quickChatOpen = ref(false)
const quickChatRooms = ref<QuickChatRoom[]>([])
const activeQuickChatRoomId = ref('')
const quickChatDraft = ref('')
const quickChatDrawerWidth = ref(860)
const quickChatRoomEditorOpen = ref(false)
const quickChatRoomEditorMode = ref<'create' | 'invite' | 'rename'>('create')
const quickChatNoticeOpen = ref(false)
const quickChatNoticeDraft = ref('')
const quickChatMembersOpen = ref(false)
const quickChatForwardOpen = ref(false)
const quickChatForwardMessageId = ref('')
const quickChatForwardTargetRoomId = ref('')
const quickChatForwardRemark = ref('')
const quickChatTodoOpen = ref(false)
const quickChatTodoItems = ref<QuickChatTodoItem[]>([])
const quickChatTodoViewMode = ref<'all' | 'pending' | 'overdue' | 'completed'>('pending')
const quickChatTodoScopeMode = ref<'mine' | 'created' | 'all'>('mine')
const quickChatTodoAutoEscalationEnabled = ref(true)
const quickChatTodoEscalationHours = reactive({
  level1: 4,
  level2: 24
})
const quickChatTodoEditorOpen = ref(false)
const quickChatTodoEditorMode = ref<'create' | 'edit'>('create')
const quickChatTodoEditingId = ref('')
const quickChatTodoWeeklyOpen = ref(false)
const quickChatTodoForm = reactive({
  roomId: '',
  roomName: '',
  messageId: '',
  content: '',
  assigneeId: undefined as string | undefined,
  priority: 'MEDIUM' as QuickChatTodoItem['priority'],
  dueRule: 'TOMORROW_18' as 'TODAY_18' | 'TOMORROW_18' | 'THREE_DAYS_18' | 'NO_DUE'
})
const quickChatDnd = ref(false)
const quickChatTrainingUntil = ref('')
const quickChatKeyword = ref('')
const quickChatUnreadOnly = ref(false)
const quickChatRoomListMode = ref<'active' | 'archived' | 'all'>('active')
const quickChatBatchMode = ref(false)
const quickChatBatchSelectedIds = ref<string[]>([])
const quickChatMessageKeyword = ref('')
const quickChatMessagesRef = ref<HTMLElement | null>(null)
const quickChatRoomForm = reactive({
  name: '',
  departmentIds: [] as string[],
  memberIds: [] as string[],
  applyRemark: ''
})
const quickChatAliasIdMap = reactive<Record<string, string>>({})
const currentQuickChatSenderId = computed(() => String(userStore.staffInfo?.username || userStore.staffInfo?.id || 'me'))
const currentQuickChatSenderNumericId = computed(() => {
  const rawId = userStore.staffInfo?.id
  if (typeof rawId === 'number') {
    if (!Number.isSafeInteger(rawId) || rawId <= 0) return ''
    return String(rawId)
  }
  const text = String(rawId ?? '').trim()
  if (!/^\d+$/.test(text)) return ''
  try {
    return BigInt(text) > 0n ? text : ''
  } catch {
    return ''
  }
})
const currentQuickChatActorId = computed(() =>
  String(currentQuickChatSenderNumericId.value || currentQuickChatSenderId.value || 'me')
)
const currentQuickChatSenderName = computed(() => String(userStore.staffInfo?.realName || userStore.staffInfo?.username || '我'))
const quickChatNotifyModeOptions = [
  { label: '全部提醒', value: 'ALL' },
  { label: '仅@我提醒', value: 'MENTION' },
  { label: '会话免打扰', value: 'MUTE' }
]
const quickChatTodoPriorityOptions = [
  { label: '高', value: 'HIGH' },
  { label: '中', value: 'MEDIUM' },
  { label: '低', value: 'LOW' }
]
const quickChatTodoViewModeOptions = [
  { label: '待完成', value: 'pending' },
  { label: '逾期', value: 'overdue' },
  { label: '已完成', value: 'completed' },
  { label: '全部', value: 'all' }
]
const quickChatTodoScopeModeOptions = [
  { label: '我负责', value: 'mine' },
  { label: '我创建', value: 'created' },
  { label: '全部', value: 'all' }
]
const quickChatTodoDueRuleOptions = [
  { label: '今天 18:00', value: 'TODAY_18' },
  { label: '明天 18:00', value: 'TOMORROW_18' },
  { label: '3天后 18:00', value: 'THREE_DAYS_18' },
  { label: '暂不设置', value: 'NO_DUE' }
]
const quickChatRoomListModeOptions = [
  { label: '进行中', value: 'active' },
  { label: '已归档', value: 'archived' },
  { label: '全部', value: 'all' }
]
const myQuickChatRooms = computed(() =>
  quickChatRooms.value
    .filter((room) => room.memberIds.some((memberId) => isCurrentQuickChatIdentity(memberId)))
)
const visibleQuickChatRooms = computed(() =>
  myQuickChatRooms.value.filter((room) => {
    const archived = isRoomArchivedByCurrentUser(room)
    if (quickChatRoomListMode.value === 'archived') return archived
    if (quickChatRoomListMode.value === 'all') return true
    return !archived
  })
)
const sortedQuickChatRooms = computed(() =>
  [...visibleQuickChatRooms.value].sort((a, b) => {
    const pinScore = Number(Boolean(b.pinned)) - Number(Boolean(a.pinned))
    if (pinScore !== 0) return pinScore
    return dayjs(b.updatedAt || b.createdAt).valueOf() - dayjs(a.updatedAt || a.createdAt).valueOf()
  })
)
const activeQuickChatRoom = computed(() => visibleQuickChatRooms.value.find((item) => item.id === activeQuickChatRoomId.value) || null)
const activeQuickChatNotifyMode = computed({
  get: () => activeQuickChatRoom.value?.notificationMode || 'ALL',
  set: (value) => changeActiveRoomNotifyMode(value as 'ALL' | 'MENTION' | 'MUTE')
})
const activeQuickChatMessages = computed(() => activeQuickChatRoom.value?.messages || [])
const filteredActiveQuickChatMessages = computed(() => {
  const keyword = String(quickChatMessageKeyword.value || '').trim().toLowerCase()
  if (!keyword) return activeQuickChatMessages.value
  return activeQuickChatMessages.value.filter((messageRow) => {
    const hitText = `${messageRow.senderName || ''} ${messageRow.content || ''} ${messageRow.fileName || ''}`.toLowerCase()
    return hitText.includes(keyword)
  })
})
const quickChatForwardRoomOptions = computed(() =>
  sortedQuickChatRooms.value
    .filter((room) => room.id !== activeQuickChatRoomId.value)
    .map((room) => ({ label: room.name, value: room.id }))
)
const activeQuickChatMemberRows = computed(() => {
  if (!activeQuickChatRoom.value) return [] as Array<{ id: string; name: string; statusLabel: string; statusColor: string }>
  return activeQuickChatRoom.value.memberIds.map((memberId) => {
    const name = memberNameById(memberId)
    if (isCurrentQuickChatIdentity(memberId)) {
      return { id: memberId, name, statusLabel: presenceStatus.value.label, statusColor: String(presenceStatus.value.color || 'default') }
    }
    return { id: memberId, name, statusLabel: '在线', statusColor: 'green' }
  })
})
const quickChatUnreadCount = computed(() =>
  myQuickChatRooms.value
    .filter((room) => !isRoomArchivedByCurrentUser(room))
    .reduce((sum, item) => sum + resolveRoomUnreadForCurrentUser(item), 0)
)
const quickChatPressureTag = computed(() => {
  const unread = quickChatUnreadCount.value
  const overdue = quickChatTodoOverdueCount.value
  if (overdue > 0) return { text: `待办逾期 ${overdue}`, color: 'red' }
  if (unread >= 20) return { text: `高压 ${unread}`, color: 'red' }
  if (unread >= 8) return { text: `待处理 ${unread}`, color: 'orange' }
  return null
})
const quickChatTodoPendingCount = computed(() => quickChatTodoItems.value.filter((item) => !item.done).length)
const quickChatTodoOverdueCount = computed(() =>
  quickChatTodoItems.value.filter((item) => !item.done && isQuickChatTodoOverdue(item)).length
)
const quickChatTodoCompletionRate = computed(() => {
  const total = quickChatTodoItems.value.length
  if (!total) return 100
  const completed = quickChatTodoItems.value.filter((item) => item.done).length
  return Math.round((completed / total) * 100)
})
const quickChatExecutionOverview = computed(() => ({
  todoPending: quickChatTodoPendingCount.value,
  todoOverdue: quickChatTodoOverdueCount.value,
  unreadChats: quickChatUnreadCount.value,
  totalPressure: quickChatTodoPendingCount.value + quickChatUnreadCount.value
}))
const quickChatTodoAssigneeRank = computed(() => {
  const map = new Map<string, { name: string; pending: number; overdue: number }>()
  quickChatTodoItems.value.forEach((item) => {
    if (item.done) return
    const key = String(item.assigneeId || 'unassigned')
    const name = item.assigneeName || (item.assigneeId ? memberNameById(item.assigneeId) : '未指派')
    if (!map.has(key)) {
      map.set(key, { name, pending: 0, overdue: 0 })
    }
    const row = map.get(key)!
    row.pending += 1
    if (isQuickChatTodoOverdue(item)) row.overdue += 1
  })
  return Array.from(map.values())
    .sort((a, b) => b.overdue - a.overdue || b.pending - a.pending || a.name.localeCompare(b.name, 'zh-CN'))
    .slice(0, 5)
})
const quickChatTodoPriorityStats = computed(() => ({
  high: quickChatTodoItems.value.filter((item) => !item.done && item.priority === 'HIGH').length,
  medium: quickChatTodoItems.value.filter((item) => !item.done && item.priority === 'MEDIUM').length,
  low: quickChatTodoItems.value.filter((item) => !item.done && item.priority === 'LOW').length
}))
const filteredQuickChatTodoItems = computed(() => {
  const priorityWeight: Record<QuickChatTodoItem['priority'], number> = {
    HIGH: 3,
    MEDIUM: 2,
    LOW: 1
  }
  const scopedRows = quickChatTodoItems.value.filter((item) => {
    const currentId = currentQuickChatKeyId()
    if (quickChatTodoScopeMode.value === 'all') return true
    if (quickChatTodoScopeMode.value === 'created') return isCurrentQuickChatIdentity(String(item.creatorId || currentId))
    return isCurrentQuickChatIdentity(String(item.assigneeId || currentId))
  })
  const rows = [...scopedRows].sort((a, b) => {
    if (a.done !== b.done) return Number(a.done) - Number(b.done)
    const priorityDiff = priorityWeight[b.priority] - priorityWeight[a.priority]
    if (priorityDiff !== 0) return priorityDiff
    const aDue = a.dueAt ? dayjs(a.dueAt).valueOf() : Number.MAX_SAFE_INTEGER
    const bDue = b.dueAt ? dayjs(b.dueAt).valueOf() : Number.MAX_SAFE_INTEGER
    if (aDue !== bDue) return aDue - bDue
    return dayjs(b.createdAt).valueOf() - dayjs(a.createdAt).valueOf()
  })
  if (quickChatTodoViewMode.value === 'all') return rows
  if (quickChatTodoViewMode.value === 'completed') return rows.filter((item) => item.done)
  if (quickChatTodoViewMode.value === 'overdue') return rows.filter((item) => !item.done && isQuickChatTodoOverdue(item))
  return rows.filter((item) => !item.done)
})
const quickChatTodoWeeklyStats = computed(() => {
  const weekStart = dayjs().startOf('week')
  const weekEnd = dayjs().endOf('week')
  const startTs = weekStart.valueOf()
  const endTs = weekEnd.valueOf()
  const inWeekCreated = quickChatTodoItems.value.filter((item) => {
    const ts = dayjs(item.createdAt).valueOf()
    return ts >= startTs && ts <= endTs
  })
  const inWeekCompleted = quickChatTodoItems.value.filter((item) => {
    if (!item.doneAt) return false
    const ts = dayjs(item.doneAt).valueOf()
    return ts >= startTs && ts <= endTs
  })
  const created = inWeekCreated.length
  const completed = inWeekCompleted.length
  const completionRate = created > 0 ? Math.round((completed / created) * 100) : 100
  const overduePending = quickChatTodoItems.value.filter((item) => !item.done && isQuickChatTodoOverdue(item)).length
  const escalated = quickChatTodoItems.value.filter((item) => Number(item.escalationLevel || 0) > 0).length
  return {
    rangeText: `${weekStart.format('MM-DD')} ~ ${weekEnd.format('MM-DD')}`,
    created,
    completed,
    completionRate,
    overduePending,
    escalated
  }
})
const quickChatTodoAssigneeOptions = computed(() => {
  const roomId = quickChatTodoForm.roomId || activeQuickChatRoom.value?.id || ''
  const room = quickChatRooms.value.find((item) => item.id === roomId)
  if (!room) return []
  return room.memberIds.map((memberId) => ({ label: memberNameById(memberId), value: memberId }))
})
let quickChatTodoEscalationTimer: number | undefined
const filteredQuickChatRooms = computed(() => {
  const keyword = String(quickChatKeyword.value || '').trim().toLowerCase()
  const baseRooms = quickChatUnreadOnly.value
    ? sortedQuickChatRooms.value.filter((room) => Number(room.unreadCount || 0) > 0)
    : sortedQuickChatRooms.value
  if (!keyword) return baseRooms
  return baseRooms.filter((room) => {
    if (room.name.toLowerCase().includes(keyword)) return true
    return room.memberIds.some((memberId) => memberNameById(memberId).toLowerCase().includes(keyword))
  })
})
const quickChatDepartmentOptions = computed(() => departmentOptions.value.map((item) => ({ label: item.label, value: item.value })))
const quickChatStaffOptions = computed(() =>
  staffOptions.value
    .filter((item) => {
      if (!quickChatRoomForm.departmentIds.length) return true
      return item.departmentId ? quickChatRoomForm.departmentIds.includes(String(item.departmentId)) : false
    })
    .map((item) => ({ label: item.label, value: item.username || item.value }))
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
let quickChatCloudPullTimer: number | undefined
let quickChatCloudSaveTimer: number | undefined
let quickChatFanoutTimer: number | undefined
let quickChatFanoutRetryTimer: number | undefined
let quickChatEventEmitTimer: number | undefined
let quickChatLocalPersistTimer: number | undefined
let quickChatLocalPersistIdleTimer: number | undefined
let quickChatWsReconnectTimer: number | undefined
let quickChatPermissionWarned = false
let quickChatAliasHydrated = false
const quickChatWsReconnectAttempt = ref(0)
let quickChatWebSocket: WebSocket | null = null
let quickChatWebSocketManualClose = false
const quickChatRuntimeStarted = ref(false)
const QUICK_CHAT_CLOUD_PULL_INTERVAL_MS = 60000
const QUICK_CHAT_CLOUD_SAVE_DEBOUNCE_MS = 1400
const QUICK_CHAT_FANOUT_DEBOUNCE_MS = 1200
const QUICK_CHAT_FANOUT_RETRY_INTERVAL_MS = 30000
const QUICK_CHAT_EVENT_EMIT_DEBOUNCE_MS = 900
const QUICK_CHAT_LOCAL_PERSIST_DEBOUNCE_MS = 900
const QUICK_CHAT_ARCHIVE_UNDO_WINDOW_MS = 5 * 60 * 1000
const QUICK_CHAT_MAX_ROOMS = 12
const QUICK_CHAT_MAX_MESSAGES_PER_ROOM = 40
const QUICK_CHAT_MAX_TODO_ITEMS = 80
const quickChatCloudReady = ref(false)
const quickChatCloudRemoteUpdatedAt = ref(0)
const quickChatCloudSavePending = ref(false)
const quickChatCloudSyncing = ref(false)
let quickChatCloudLastPayload = ''
let quickChatLastLocalPayload = ''
let quickChatCloudQueuedPayload = ''
let quickChatFanoutLastPayload = ''
const quickChatFanoutQueuedPayload = ref('')
const quickChatEventQueuedSnapshot = ref('')
const quickChatLastEventSnapshot = ref('')
const quickChatWsConnected = ref(false)
const quickChatApplyingRemoteEvent = ref(false)
const quickChatFanoutPending = ref(false)
const quickChatFanoutLastSuccessAt = ref(0)
const quickChatFanoutLastErrorAt = ref(0)
const quickChatFanoutLastAffected = ref(0)
const quickChatSyncConflict = reactive({
  visible: false,
  direction: '' as 'LOCAL_NEWER' | 'CLOUD_NEWER' | '',
  localMs: 0,
  remoteMs: 0,
  remoteJson: ''
})
const quickChatLastArchivedRoomId = ref('')
const quickChatLastArchivedAt = ref(0)
const quickChatPersistPending = reactive({
  skipCloud: true,
  skipFanout: true,
  skipEvent: true
})
const canUndoQuickChatArchive = computed(() => {
  void presenceTick.value
  if (!quickChatLastArchivedRoomId.value || !quickChatLastArchivedAt.value) return false
  return Date.now() - quickChatLastArchivedAt.value <= QUICK_CHAT_ARCHIVE_UNDO_WINDOW_MS
})
const quickChatCloudStatus = computed(() => {
  if (!quickChatRuntimeStarted.value) {
    return { text: '按需同步', color: 'default' }
  }
  if (quickChatCloudSyncing.value || quickChatCloudSavePending.value) {
    return { text: '云端同步中', color: 'blue' }
  }
  if (!quickChatCloudReady.value) {
    return { text: '云端未连接', color: 'default' }
  }
  if (!quickChatCloudRemoteUpdatedAt.value) {
    return { text: '云端待初始化', color: 'orange' }
  }
  return {
    text: `已同步 ${dayjs(quickChatCloudRemoteUpdatedAt.value).format('MM-DD HH:mm')}`,
    color: 'green'
  }
})
const quickChatFanoutStatus = computed(() => {
  if (!quickChatRuntimeStarted.value) {
    return { text: '分发待启用', color: 'default' }
  }
  if (quickChatFanoutPending.value) {
    return { text: '成员分发中', color: 'blue' }
  }
  if (quickChatFanoutQueuedPayload.value) {
    const errTime = quickChatFanoutLastErrorAt.value
      ? dayjs(quickChatFanoutLastErrorAt.value).format('HH:mm:ss')
      : '-'
    return { text: `分发待重试 ${errTime}`, color: 'red' }
  }
  if (quickChatFanoutLastSuccessAt.value) {
    const timeText = dayjs(quickChatFanoutLastSuccessAt.value).format('HH:mm:ss')
    const affected = Number(quickChatFanoutLastAffected.value || 0)
    return { text: `已分发${affected > 0 ? `(${affected})` : ''} ${timeText}`, color: 'green' }
  }
  return { text: '分发待初始化', color: 'default' }
})
const quickChatWsStatus = computed(() => {
  if (!quickChatRuntimeStarted.value) {
    return { text: '推送待启用', color: 'default' }
  }
  if (quickChatWsConnected.value) {
    return { text: '推送已连接', color: 'green' }
  }
  if (quickChatWsReconnectAttempt.value > 0) {
    return { text: `推送重连中(${quickChatWsReconnectAttempt.value})`, color: 'orange' }
  }
  return { text: '推送未连接', color: 'default' }
})
const quickChatSyncConflictTitle = computed(() => {
  if (!quickChatSyncConflict.visible) return ''
  if (quickChatSyncConflict.direction === 'LOCAL_NEWER') return '检测到同步冲突：本地版本更新'
  if (quickChatSyncConflict.direction === 'CLOUD_NEWER') return '检测到同步冲突：云端版本更新'
  return '检测到同步冲突'
})
const quickChatSyncConflictDesc = computed(() => {
  if (!quickChatSyncConflict.visible) return ''
  const localText = quickChatSyncConflict.localMs ? dayjs(quickChatSyncConflict.localMs).format('MM-DD HH:mm:ss') : '-'
  const remoteText = quickChatSyncConflict.remoteMs ? dayjs(quickChatSyncConflict.remoteMs).format('MM-DD HH:mm:ss') : '-'
  if (quickChatSyncConflict.direction === 'LOCAL_NEWER') {
    return `本地更新时间 ${localText}，云端更新时间 ${remoteText}。请选择是否将本地会话覆盖到云端。`
  }
  if (quickChatSyncConflict.direction === 'CLOUD_NEWER') {
    return `云端更新时间 ${remoteText}，本地更新时间 ${localText}。请选择是否使用云端会话覆盖本地。`
  }
  return `本地更新时间 ${localText}，云端更新时间 ${remoteText}。请先确认同步策略。`
})

const filteredMenu = computed(() => {
  const roles = userStore.roles || []
  return getMenuTree(roles, userStore.pagePermissions || [])
})

function renderMenuLabel(label: string) {
  return h(
    'span',
    {
      class: 'side-menu-label',
      title: label
    },
    label
  )
}

const menuItems = computed(() => {
  const map = (items: any[]): any[] =>
    items.map((item) => ({
      key: item.path || item.key,
      label: renderMenuLabel(String(item.label || '')),
      title: String(item.label || ''),
      children: item.children ? map(item.children) : undefined
    }))
  return map(filteredMenu.value)
})

const selectedKeys = computed(() => [route.path])
const currentRouteTabKey = computed(() => normalizeTabKey(route.path || route.fullPath) || route.fullPath)
const aliveViewMax = computed(() => Math.min(Math.max(routeTabs.value.length, 4), MAX_ALIVE_VIEW_CACHE))
const viewRenderKey = computed(() => `${currentRouteTabKey.value}::${tabRefreshSeeds[currentRouteTabKey.value] || 0}`)

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
watch(
  () => quickChatOpen.value,
  (opened) => {
    if (opened) {
      ensureQuickChatRuntime()
      return
    }
    stopQuickChatRuntime()
  }
)
watch(
  () => currentQuickChatSenderId.value,
  () => {
    quickChatCloudLastPayload = ''
    quickChatCloudQueuedPayload = ''
    quickChatFanoutLastPayload = ''
    quickChatFanoutQueuedPayload.value = ''
    quickChatFanoutPending.value = false
    quickChatFanoutLastSuccessAt.value = 0
    quickChatFanoutLastErrorAt.value = 0
    quickChatFanoutLastAffected.value = 0
    quickChatLastEventSnapshot.value = ''
    quickChatEventQueuedSnapshot.value = ''
    quickChatCloudRemoteUpdatedAt.value = 0
    quickChatCloudReady.value = false
    quickChatCloudSyncing.value = false
    quickChatPermissionWarned = false
    quickChatWsReconnectAttempt.value = 0
    quickChatSyncConflict.visible = false
    quickChatSyncConflict.direction = ''
    quickChatSyncConflict.localMs = 0
    quickChatSyncConflict.remoteMs = 0
    quickChatSyncConflict.remoteJson = ''
    quickChatLastArchivedRoomId.value = ''
    quickChatLastArchivedAt.value = 0
    if (quickChatFanoutTimer) {
      window.clearTimeout(quickChatFanoutTimer)
      quickChatFanoutTimer = undefined
    }
    if (quickChatEventEmitTimer) {
      window.clearTimeout(quickChatEventEmitTimer)
      quickChatEventEmitTimer = undefined
    }
    loadQuickChatState()
    reconcileQuickChatSelfIdentity()
    rehydrateQuickChatUnread()
    ensureActiveQuickChatVisible()
    loadQuickChatTodo()
    loadQuickChatTodoAutomation()
    if (quickChatRuntimeStarted.value) {
      ensureQuickChatRuntime()
    }
  }
)
watch(
  () => activeQuickChatRoomId.value,
  () => {
    if (!quickChatOpen.value) return
    scrollQuickChatToBottom()
  }
)
watch(
  () => activeQuickChatMessages.value.length,
  () => {
    if (!quickChatOpen.value) return
    scrollQuickChatToBottom()
  }
)
watch(
  () => quickChatRoomListMode.value,
  () => {
    ensureActiveQuickChatVisible()
    clearBatchSelection()
  }
)
watch(
  () => filteredQuickChatRooms.value.map((room) => room.id).join(','),
  () => {
    if (!quickChatBatchSelectedIds.value.length) return
    const allowSet = new Set(filteredQuickChatRooms.value.map((room) => room.id))
    quickChatBatchSelectedIds.value = quickChatBatchSelectedIds.value.filter((id) => allowSet.has(id))
  }
)
watch(
  () => quickChatTodoEditorOpen.value,
  (opened) => {
    if (opened) return
    quickChatTodoEditingId.value = ''
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
  return purePath || text
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
  pruneTabRefreshSeeds()
  persistRouteTabs()
  closeTabContextMenu()
}

function closeAllTabs() {
  routeTabs.value = routeTabs.value.filter((item) => !item.closable)
  pruneTabRefreshSeeds()
  persistRouteTabs()
  closeTabContextMenu()
  router.push('/portal')
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
  router.push(fallback?.key || '/portal')
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
        closable: normalizeTabKey(String(item?.path || item?.key || '')) !== '/portal'
      }))
      .filter((item: any) => !!item.key && !!item.path)
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

function updateQuickChatDrawerWidth() {
  const viewport = typeof window !== 'undefined' ? window.innerWidth : 1280
  if (viewport <= 768) {
    quickChatDrawerWidth.value = Math.max(320, viewport - 12)
    return
  }
  quickChatDrawerWidth.value = Math.max(640, Math.min(980, viewport - 56))
}

function quickChatStorageKey() {
  const orgId = String(userStore.staffInfo?.orgId || 'default')
  return `layout_quick_chat_rooms_v2_org_${orgId}`
}

function quickChatTodoStorageKey() {
  const orgId = String(userStore.staffInfo?.orgId || 'default')
  return `layout_quick_chat_todo_v1_org_${orgId}_${currentQuickChatSenderId.value}`
}

function quickChatTodoAutomationStorageKey() {
  const orgId = String(userStore.staffInfo?.orgId || 'default')
  return `layout_quick_chat_todo_auto_v1_org_${orgId}_${currentQuickChatSenderId.value}`
}

function rememberQuickChatStaffAlias(row: any) {
  const idText = String(row?.id || '').trim()
  if (!idText) return
  quickChatAliasIdMap[idText] = idText
  const username = String(row?.username || '').trim()
  if (username) quickChatAliasIdMap[username] = idText
  const staffNo = String(row?.staffNo || '').trim()
  if (staffNo) quickChatAliasIdMap[staffNo] = idText
}

async function hydrateQuickChatAliasMap() {
  try {
    const page = await getStaffPage({
      pageNo: 1,
      pageSize: 500,
      keyword: ''
    })
    ;(page.list || []).forEach((row: any) => rememberQuickChatStaffAlias(row))
  } catch {}
}

function ensureQuickChatRuntime(options?: { forcePull?: boolean }) {
  const forcePull = options?.forcePull !== false
  const startedThisTime = !quickChatRuntimeStarted.value
  if (startedThisTime) {
    quickChatRuntimeStarted.value = true
  }
  setupQuickChatCloudPullTimer()
  setupQuickChatFanoutRetryTimer()
  if (!quickChatAliasHydrated) {
    quickChatAliasHydrated = true
    hydrateQuickChatAliasMap().then(() => {
      reconcileQuickChatSelfIdentity()
    }).catch(() => {})
  }
  quickChatWebSocketManualClose = false
  connectQuickChatWebSocket()
  if (forcePull) {
    pullQuickChatStateFromCloud(startedThisTime)
  }
}

function stopQuickChatRuntime() {
  quickChatRuntimeStarted.value = false
  quickChatCloudSyncing.value = false
  quickChatCloudSavePending.value = false
  if (quickChatCloudPullTimer) {
    window.clearInterval(quickChatCloudPullTimer)
    quickChatCloudPullTimer = undefined
  }
  if (quickChatCloudSaveTimer) {
    window.clearTimeout(quickChatCloudSaveTimer)
    quickChatCloudSaveTimer = undefined
  }
  if (quickChatFanoutTimer) {
    window.clearTimeout(quickChatFanoutTimer)
    quickChatFanoutTimer = undefined
  }
  if (quickChatFanoutRetryTimer) {
    window.clearInterval(quickChatFanoutRetryTimer)
    quickChatFanoutRetryTimer = undefined
  }
  if (quickChatEventEmitTimer) {
    window.clearTimeout(quickChatEventEmitTimer)
    quickChatEventEmitTimer = undefined
  }
  closeQuickChatWebSocket()
}

function normalizeQuickChatMemberId(rawId: unknown) {
  const text = String(rawId || '').trim()
  if (!text) return ''
  if (quickChatAliasIdMap[text]) return quickChatAliasIdMap[text]
  const selfId = currentQuickChatSenderId.value
  const selfNumericId = currentQuickChatSenderNumericId.value
  if (selfId && text === selfId) return selfNumericId || selfId
  if (selfNumericId && text === selfNumericId) return selfNumericId
  if (text === 'me') return selfNumericId || selfId || text
  const username = String(userStore.staffInfo?.username || '').trim()
  if (username && text === username) return selfNumericId || selfId || text
  return text
}

function quickChatIdentityAliases() {
  const aliases = new Set<string>()
  const actorId = String(currentQuickChatActorId.value || '').trim()
  const senderId = String(currentQuickChatSenderId.value || '').trim()
  const senderNumericId = String(currentQuickChatSenderNumericId.value || '').trim()
  const username = String(userStore.staffInfo?.username || '').trim()
  if (actorId) aliases.add(actorId)
  if (senderId) aliases.add(senderId)
  if (senderNumericId) aliases.add(senderNumericId)
  if (username) aliases.add(username)
  aliases.add('me')
  return aliases
}

function resolveRoomUnreadByUserForCurrent(unreadByUser?: Record<string, number>) {
  const bucket = unreadByUser || {}
  let maxUnread = 0
  quickChatIdentityAliases().forEach((alias) => {
    const raw = Number(bucket[alias] || 0)
    if (!Number.isFinite(raw)) return
    if (raw > maxUnread) maxUnread = raw
  })
  return Math.max(0, Math.floor(maxUnread))
}

function resolveRoomUnreadForCurrentUser(room: QuickChatRoom) {
  const byUser = resolveRoomUnreadByUserForCurrent(room?.unreadByUser)
  const fallback = Number(room?.unreadCount || 0)
  if (!Number.isFinite(fallback)) return byUser
  return Math.max(byUser, Math.max(0, Math.floor(fallback)))
}

function resolveRoomArchivedByCurrentUser(room: QuickChatRoom) {
  const map = room?.archivedByUser || {}
  for (const alias of quickChatIdentityAliases()) {
    if (map[alias] === true) return true
  }
  return false
}

function isCurrentQuickChatIdentity(rawId: unknown) {
  const rawText = String(rawId || '').trim()
  const normalized = normalizeQuickChatMemberId(rawText)
  const aliases = quickChatIdentityAliases()
  if (rawText && aliases.has(rawText)) return true
  return !!normalized && aliases.has(normalized)
}

function currentQuickChatKeyId() {
  return currentQuickChatActorId.value
}

function quickChatPresenceKey() {
  return `layout_quick_chat_presence_v1_${userStorageScope()}`
}

function cancelQuickChatIdlePersist() {
  if (quickChatLocalPersistIdleTimer === undefined) return
  const idleCancel = (window as any).cancelIdleCallback
  if (typeof idleCancel === 'function') {
    idleCancel(quickChatLocalPersistIdleTimer)
  } else {
    window.clearTimeout(quickChatLocalPersistIdleTimer)
  }
  quickChatLocalPersistIdleTimer = undefined
}

function scheduleQuickChatIdlePersist(task: () => void) {
  cancelQuickChatIdlePersist()
  const idleRequest = (window as any).requestIdleCallback
  if (typeof idleRequest === 'function') {
    quickChatLocalPersistIdleTimer = idleRequest(() => {
      quickChatLocalPersistIdleTimer = undefined
      task()
    }, { timeout: 1200 })
    return
  }
  quickChatLocalPersistIdleTimer = window.setTimeout(() => {
    quickChatLocalPersistIdleTimer = undefined
    task()
  }, 80)
}

function persistQuickChatTodo() {
  try {
    localStorage.setItem(quickChatTodoStorageKey(), JSON.stringify(quickChatTodoItems.value.slice(0, QUICK_CHAT_MAX_TODO_ITEMS)))
  } catch {}
}

function persistQuickChatTodoAutomation() {
  try {
    localStorage.setItem(quickChatTodoAutomationStorageKey(), JSON.stringify({
      autoEscalationEnabled: quickChatTodoAutoEscalationEnabled.value !== false,
      level1Hours: Number(quickChatTodoEscalationHours.level1 || 4),
      level2Hours: Number(quickChatTodoEscalationHours.level2 || 24)
    }))
  } catch {}
}

function loadQuickChatTodo() {
  try {
    const raw = localStorage.getItem(quickChatTodoStorageKey())
    if (!raw) {
      quickChatTodoItems.value = []
      return
    }
    const parsed = JSON.parse(raw)
    if (!Array.isArray(parsed)) {
      quickChatTodoItems.value = []
      return
    }
    quickChatTodoItems.value = parsed.map((item: any) => ({
      id: String(item?.id || `todo_${Date.now()}`),
      roomId: String(item?.roomId || ''),
      roomName: String(item?.roomName || ''),
      messageId: String(item?.messageId || ''),
      content: String(item?.content || ''),
      creatorId: item?.creatorId ? String(item.creatorId) : undefined,
      creatorName: item?.creatorName ? String(item.creatorName) : undefined,
      assigneeId: item?.assigneeId ? String(item.assigneeId) : undefined,
      assigneeName: item?.assigneeName ? String(item.assigneeName) : undefined,
      priority: (['HIGH', 'MEDIUM', 'LOW'].includes(String(item?.priority || 'MEDIUM'))
        ? String(item.priority)
        : 'MEDIUM') as QuickChatTodoItem['priority'],
      dueAt: item?.dueAt ? String(item.dueAt) : undefined,
      dueAtText: item?.dueAtText ? String(item.dueAtText) : undefined,
      escalationLevel: [0, 1, 2].includes(Number(item?.escalationLevel)) ? Number(item.escalationLevel) as 0 | 1 | 2 : 0,
      lastEscalatedAt: item?.lastEscalatedAt ? String(item.lastEscalatedAt) : undefined,
      calendarSynced: item?.calendarSynced === true,
      calendarTaskId: item?.calendarTaskId ? String(item.calendarTaskId) : undefined,
      doneAt: item?.doneAt ? String(item.doneAt) : undefined,
      createdAt: String(item?.createdAt || dayjs().toISOString()),
      createdAtText: String(item?.createdAtText || dayjs().format('MM-DD HH:mm')),
      done: item?.done === true
    }))
  } catch {
    quickChatTodoItems.value = []
  }
}

function loadQuickChatTodoAutomation() {
  try {
    const raw = localStorage.getItem(quickChatTodoAutomationStorageKey())
    if (!raw) {
      quickChatTodoAutoEscalationEnabled.value = true
      return
    }
    const parsed = JSON.parse(raw)
    quickChatTodoAutoEscalationEnabled.value = parsed?.autoEscalationEnabled !== false
    const level1Hours = Number(parsed?.level1Hours || 4)
    const level2Hours = Number(parsed?.level2Hours || 24)
    quickChatTodoEscalationHours.level1 = Math.min(Math.max(level1Hours, 1), 72)
    quickChatTodoEscalationHours.level2 = Math.min(Math.max(level2Hours, quickChatTodoEscalationHours.level1 + 1), 168)
  } catch {
    quickChatTodoAutoEscalationEnabled.value = true
    quickChatTodoEscalationHours.level1 = 4
    quickChatTodoEscalationHours.level2 = 24
  }
}

function buildQuickChatRoomPayload(room: QuickChatRoom) {
  return {
    id: room.id,
    name: room.name,
    departmentIds: Array.isArray(room.departmentIds) ? room.departmentIds : [],
    memberIds: Array.isArray(room.memberIds) ? room.memberIds : [],
    unreadCount: Number(room.unreadCount || 0),
    unreadByUser: room.unreadByUser || {},
    pinned: room.pinned === true,
    notificationMode: room.notificationMode || 'ALL',
    notice: String(room.notice || ''),
    archivedByUser: room.archivedByUser || {},
    createdAt: room.createdAt,
    updatedAt: room.updatedAt || room.createdAt,
    messages: Array.isArray(room.messages)
      ? room.messages.slice(-QUICK_CHAT_MAX_MESSAGES_PER_ROOM).map((message) => ({
        id: message.id,
        senderId: message.senderId,
        senderName: message.senderName,
        kind: message.kind,
        content: message.content || '',
        fileName: message.fileName || '',
        fileUrl: message.fileUrl || '',
        timeText: message.timeText || dayjs().format('MM-DD HH:mm'),
        createdAt: message.createdAt || dayjs().toISOString(),
        recalled: message.recalled === true,
        readByUser: message.readByUser || {}
      }))
      : []
  }
}

function buildQuickChatStatePayload() {
  return quickChatRooms.value.slice(0, QUICK_CHAT_MAX_ROOMS).map((room) => buildQuickChatRoomPayload(room))
}

function normalizeQuickChatRooms(parsed: any[]) {
  return parsed.slice(0, QUICK_CHAT_MAX_ROOMS).map((room: any) => ({
    id: String(room?.id || `room_${Date.now()}`),
    name: String(room?.name || '未命名群聊'),
    departmentIds: Array.isArray(room?.departmentIds) ? room.departmentIds.map((id: any) => String(id)) : [],
    memberIds: Array.isArray(room?.memberIds)
      ? Array.from(new Set(room.memberIds.map((id: any) => normalizeQuickChatMemberId(id)).filter(Boolean)))
      : [],
    unreadCount: Math.max(
      resolveRoomUnreadByUserForCurrent(typeof room?.unreadByUser === 'object' ? room.unreadByUser : undefined),
      Number(room?.unreadCount || 0)
    ),
    unreadByUser: typeof room?.unreadByUser === 'object' && room?.unreadByUser
      ? Object.fromEntries(Object.entries(room.unreadByUser).map(([key, value]) => [String(key), Number(value || 0)]))
      : {
        [currentQuickChatKeyId()]: Number(room?.unreadCount || 0)
      },
    pinned: room?.pinned === true,
    notificationMode: (['ALL', 'MENTION', 'MUTE'].includes(String(room?.notificationMode || 'ALL'))
      ? String(room.notificationMode)
      : 'ALL') as QuickChatRoom['notificationMode'],
    notice: String(room?.notice || ''),
    archivedByUser: typeof room?.archivedByUser === 'object' && room?.archivedByUser
      ? Object.fromEntries(Object.entries(room.archivedByUser).map(([key, value]) => [String(key), value === true]))
      : {},
    createdAt: String(room?.createdAt || dayjs().toISOString()),
    updatedAt: String(room?.updatedAt || room?.createdAt || dayjs().toISOString()),
    messages: Array.isArray(room?.messages)
      ? room.messages.slice(-QUICK_CHAT_MAX_MESSAGES_PER_ROOM).map((message: any) => ({
        id: String(message?.id || `m_${Date.now()}`),
        senderId: String(message?.senderId || 'system'),
        senderName: String(message?.senderName || '系统'),
        kind: (['TEXT', 'IMAGE', 'FILE', 'SYSTEM'].includes(String(message?.kind || 'TEXT')) ? String(message?.kind || 'TEXT') : 'TEXT') as QuickChatMessage['kind'],
        content: String(message?.content || ''),
        fileName: message?.fileName ? String(message.fileName) : undefined,
        fileUrl: message?.fileUrl ? String(message.fileUrl) : undefined,
        timeText: String(message?.timeText || dayjs().format('MM-DD HH:mm')),
        createdAt: String(message?.createdAt || dayjs().toISOString()),
        recalled: message?.recalled === true,
        readByUser: typeof message?.readByUser === 'object' && message?.readByUser
          ? Object.fromEntries(Object.entries(message.readByUser).map(([key, value]) => [String(key), Boolean(value)]))
          : {}
      }))
      : []
  }))
}

function cloneQuickChatMessageRow(messageRow: QuickChatMessage): QuickChatMessage {
  return {
    ...messageRow,
    readByUser: { ...(messageRow.readByUser || {}) }
  }
}

function cloneQuickChatRoomRow(room: QuickChatRoom): QuickChatRoom {
  return {
    ...room,
    departmentIds: [...(room.departmentIds || [])],
    memberIds: [...(room.memberIds || [])],
    unreadByUser: { ...(room.unreadByUser || {}) },
    archivedByUser: { ...(room.archivedByUser || {}) },
    messages: Array.isArray(room.messages) ? room.messages.map((msg) => cloneQuickChatMessageRow(msg)) : []
  }
}

function mergeQuickChatRoomRows(localRooms: QuickChatRoom[], remoteRooms: QuickChatRoom[]) {
  const merged = new Map<string, QuickChatRoom>()
  localRooms.forEach((room) => {
    merged.set(room.id, cloneQuickChatRoomRow(room))
  })
  remoteRooms.forEach((remoteRoom) => {
    const existed = merged.get(remoteRoom.id)
    if (!existed) {
      merged.set(remoteRoom.id, cloneQuickChatRoomRow(remoteRoom))
      return
    }
    const localTs = dayjs(existed.updatedAt || existed.createdAt).valueOf()
    const remoteTs = dayjs(remoteRoom.updatedAt || remoteRoom.createdAt).valueOf()
    const preferRemote = remoteTs >= localTs
    const localMsgMap = new Map<string, QuickChatMessage>(
      (existed.messages || []).map((msg) => [msg.id, cloneQuickChatMessageRow(msg)])
    )
    ;(remoteRoom.messages || []).forEach((remoteMsg) => {
      const msgId = String(remoteMsg.id || '')
      if (!msgId) return
      const localMsg = localMsgMap.get(msgId)
      if (!localMsg) {
        localMsgMap.set(msgId, cloneQuickChatMessageRow(remoteMsg))
        return
      }
      const mergedReadByUser: Record<string, boolean> = { ...(localMsg.readByUser || {}) }
      Object.entries(remoteMsg.readByUser || {}).forEach(([key, value]) => {
        mergedReadByUser[String(key)] = Boolean(mergedReadByUser[String(key)] || value)
      })
      const remoteMsgTs = dayjs(remoteMsg.createdAt || dayjs().toISOString()).valueOf()
      const localMsgTs = dayjs(localMsg.createdAt || dayjs().toISOString()).valueOf()
      if (remoteMsg.recalled === true || remoteMsgTs >= localMsgTs) {
        localMsgMap.set(msgId, {
          ...localMsg,
          ...cloneQuickChatMessageRow(remoteMsg),
          readByUser: mergedReadByUser
        })
      } else {
        localMsg.readByUser = mergedReadByUser
        localMsgMap.set(msgId, localMsg)
      }
    })
    const mergedMessages = Array.from(localMsgMap.values()).sort((a, b) => {
      const aTs = dayjs(a.createdAt || dayjs().toISOString()).valueOf()
      const bTs = dayjs(b.createdAt || dayjs().toISOString()).valueOf()
      if (aTs !== bTs) return aTs - bTs
      return String(a.id || '').localeCompare(String(b.id || ''))
    })
    const mergedUnreadByUser: Record<string, number> = { ...(remoteRoom.unreadByUser || {}) }
    Object.entries(existed.unreadByUser || {}).forEach(([key, value]) => {
      const raw = Number(value || 0)
      if (!Number.isFinite(raw)) return
      mergedUnreadByUser[String(key)] = Math.max(Number(mergedUnreadByUser[String(key)] || 0), raw)
    })
    const mergedRoom: QuickChatRoom = {
      ...existed,
      name: preferRemote ? remoteRoom.name : existed.name,
      departmentIds: Array.from(new Set([...(existed.departmentIds || []), ...(remoteRoom.departmentIds || [])])),
      memberIds: Array.from(new Set([...(existed.memberIds || []), ...(remoteRoom.memberIds || [])])),
      notificationMode: (preferRemote ? remoteRoom.notificationMode : existed.notificationMode) || 'ALL',
      notice: preferRemote ? String(remoteRoom.notice || '') : String(existed.notice || ''),
      archivedByUser: {
        ...(remoteRoom.archivedByUser || {}),
        ...(existed.archivedByUser || {})
      },
      unreadByUser: mergedUnreadByUser,
      messages: mergedMessages,
      createdAt: existed.createdAt || remoteRoom.createdAt || dayjs().toISOString(),
      updatedAt: dayjs(Math.max(localTs || 0, remoteTs || 0, Date.now())).toISOString()
    }
    mergedRoom.unreadCount = resolveRoomUnreadForCurrentUser(mergedRoom)
    merged.set(mergedRoom.id, mergedRoom)
  })
  return Array.from(merged.values()).sort((a, b) => {
    const aTs = dayjs(a.updatedAt || a.createdAt).valueOf()
    const bTs = dayjs(b.updatedAt || b.createdAt).valueOf()
    return bTs - aTs
  })
}

function applyQuickChatRoomsFromPayload(parsed: any[], preferredActiveId = '') {
  quickChatRooms.value = normalizeQuickChatRooms(parsed)
  activeQuickChatRoomId.value = preferredActiveId || sortedQuickChatRooms.value[0]?.id || ''
  rehydrateQuickChatUnread()
  ensureActiveQuickChatVisible()
}

function quickChatLatestRoomUpdateMs(rooms: Array<Partial<QuickChatRoom>>) {
  return rooms.reduce((maxTs, room) => {
    const roomTs = dayjs(room.updatedAt || room.createdAt || dayjs().toISOString()).valueOf()
    const messages = Array.isArray(room.messages) ? room.messages : []
    const messageTs = messages.reduce((msgMax, msg: any) => {
      const ts = dayjs(msg?.createdAt || dayjs().toISOString()).valueOf()
      return ts > msgMax ? ts : msgMax
    }, 0)
    return Math.max(maxTs, roomTs, messageTs)
  }, 0)
}

function localQuickChatLatestMs() {
  return quickChatLatestRoomUpdateMs(quickChatRooms.value)
}

async function pushQuickChatStateToCloud(stateJson: string) {
  if (!stateJson || !quickChatCloudReady.value) return
  if (quickChatCloudSavePending.value) {
    quickChatCloudQueuedPayload = stateJson
    return
  }
  quickChatCloudSavePending.value = true
  try {
    const saved = await saveQuickChatState(stateJson)
    const serverMs = dayjs(saved?.updateTime || dayjs().toISOString()).valueOf()
    quickChatCloudRemoteUpdatedAt.value = serverMs || Date.now()
    quickChatCloudLastPayload = stateJson
    quickChatCloudQueuedPayload = ''
  } catch {
    // 云端失败不阻塞本地，等待下一次自动重试。
    quickChatCloudQueuedPayload = stateJson
  } finally {
    quickChatCloudSavePending.value = false
    if (quickChatCloudQueuedPayload && quickChatCloudQueuedPayload !== quickChatCloudLastPayload) {
      const queued = quickChatCloudQueuedPayload
      quickChatCloudQueuedPayload = ''
      pushQuickChatStateToCloud(queued)
    }
  }
}

function scheduleQuickChatCloudSave(stateJson: string) {
  if (!stateJson || !quickChatRuntimeStarted.value) return
  if (!quickChatCloudReady.value) {
    quickChatCloudQueuedPayload = stateJson
    return
  }
  if (stateJson === quickChatCloudLastPayload) return
  if (quickChatCloudSaveTimer) {
    window.clearTimeout(quickChatCloudSaveTimer)
    quickChatCloudSaveTimer = undefined
  }
  if (quickChatCloudSavePending.value) {
    quickChatCloudQueuedPayload = stateJson
    return
  }
  quickChatCloudSaveTimer = window.setTimeout(() => {
    quickChatCloudSaveTimer = undefined
    pushQuickChatStateToCloud(stateJson)
  }, QUICK_CHAT_CLOUD_SAVE_DEBOUNCE_MS)
}

function collectQuickChatFanoutTargets() {
  const targetSet = new Set<string>()
  quickChatRooms.value.forEach((room) => {
    ;(room.memberIds || []).forEach((memberId) => {
      const id = normalizeQuickChatMemberId(memberId)
      if (!id) return
      if (isCurrentQuickChatIdentity(id)) return
      targetSet.add(id)
    })
  })
  return Array.from(targetSet)
}

async function pushQuickChatStateToMembers(stateJson: string) {
  if (!stateJson) return
  if (stateJson === quickChatFanoutLastPayload && !quickChatFanoutQueuedPayload.value) return
  if (quickChatFanoutPending.value) {
    quickChatFanoutQueuedPayload.value = stateJson
    return
  }
  const targets = collectQuickChatFanoutTargets()
  if (!targets.length) return
  const payload = stateJson
  if (quickChatFanoutQueuedPayload.value === payload) {
    quickChatFanoutQueuedPayload.value = ''
  }
  quickChatFanoutPending.value = true
  try {
    const affected = await fanoutQuickChatState(payload, targets)
    quickChatFanoutLastSuccessAt.value = Date.now()
    quickChatFanoutLastAffected.value = Number(affected || 0)
    quickChatFanoutLastPayload = payload
  } catch {
    quickChatFanoutLastErrorAt.value = Date.now()
    quickChatFanoutQueuedPayload.value = payload
  } finally {
    quickChatFanoutPending.value = false
    const queued = quickChatFanoutQueuedPayload.value
    if (queued && queued !== quickChatFanoutLastPayload) {
      quickChatFanoutQueuedPayload.value = ''
      pushQuickChatStateToMembers(queued)
    }
  }
}

function scheduleQuickChatFanout(stateJson: string) {
  if (!stateJson) return
  if (stateJson === quickChatFanoutLastPayload && !quickChatFanoutQueuedPayload.value && !quickChatFanoutPending.value) return
  if (quickChatFanoutTimer) {
    window.clearTimeout(quickChatFanoutTimer)
    quickChatFanoutTimer = undefined
  }
  quickChatFanoutTimer = window.setTimeout(() => {
    quickChatFanoutTimer = undefined
    pushQuickChatStateToMembers(stateJson)
  }, QUICK_CHAT_FANOUT_DEBOUNCE_MS)
}

function retryQuickChatFanoutNow() {
  if (!quickChatFanoutQueuedPayload.value) {
    message.info('暂无待重试分发内容')
    return
  }
  pushQuickChatStateToMembers(quickChatFanoutQueuedPayload.value)
}

function setupQuickChatFanoutRetryTimer() {
  if (quickChatFanoutRetryTimer) {
    window.clearInterval(quickChatFanoutRetryTimer)
    quickChatFanoutRetryTimer = undefined
  }
  if (quickChatEventEmitTimer) {
    window.clearTimeout(quickChatEventEmitTimer)
    quickChatEventEmitTimer = undefined
  }
  quickChatFanoutRetryTimer = window.setInterval(() => {
    if (quickChatFanoutQueuedPayload.value && !quickChatFanoutPending.value) {
      pushQuickChatStateToMembers(quickChatFanoutQueuedPayload.value)
    }
    if (quickChatEventQueuedSnapshot.value) {
      const queued = quickChatEventQueuedSnapshot.value
      quickChatEventQueuedSnapshot.value = ''
      emitQuickChatIncrementalEvents(queued)
    }
  }, QUICK_CHAT_FANOUT_RETRY_INTERVAL_MS)
}

function roomMemberTargets(room: any) {
  const raw = Array.isArray(room?.memberIds) ? room.memberIds : []
  return raw
    .map((id: any) => normalizeQuickChatMemberId(id))
    .filter((id: string) => Boolean(id) && !isCurrentQuickChatIdentity(id))
}

async function pushQuickChatRoomToMembers(room?: QuickChatRoom | null) {
  if (!room) return
  const targets = roomMemberTargets(room)
  if (!targets.length) return
  const stateJson = JSON.stringify([buildQuickChatRoomPayload(room)])
  try {
    const affected = await fanoutQuickChatState(stateJson, targets)
    quickChatFanoutLastSuccessAt.value = Date.now()
    quickChatFanoutLastAffected.value = Number(affected || 0)
  } catch {
    quickChatFanoutLastErrorAt.value = Date.now()
    quickChatFanoutQueuedPayload.value = JSON.stringify(buildQuickChatStatePayload())
  }
}

function normalizeQuickChatEventRoom(room: any) {
  if (!room || typeof room !== 'object') return null
  return {
    id: String(room.id || ''),
    name: String(room.name || ''),
    memberIds: Array.isArray(room.memberIds) ? room.memberIds.map((id: any) => String(id)) : [],
    departmentIds: Array.isArray(room.departmentIds) ? room.departmentIds.map((id: any) => String(id)) : [],
    notice: String(room.notice || ''),
    notificationMode: String(room.notificationMode || 'ALL'),
    pinned: room.pinned === true,
    updatedAt: String(room.updatedAt || room.createdAt || dayjs().toISOString()),
    createdAt: String(room.createdAt || dayjs().toISOString())
  }
}

function normalizeQuickChatEventMessage(messageRow: any) {
  if (!messageRow || typeof messageRow !== 'object') return null
  return {
    id: String(messageRow.id || ''),
    senderId: String(messageRow.senderId || ''),
    senderName: String(messageRow.senderName || ''),
    kind: String(messageRow.kind || 'TEXT'),
    content: String(messageRow.content || ''),
    fileName: messageRow.fileName ? String(messageRow.fileName) : undefined,
    fileUrl: messageRow.fileUrl ? String(messageRow.fileUrl) : undefined,
    createdAt: String(messageRow.createdAt || dayjs().toISOString()),
    timeText: String(messageRow.timeText || dayjs().format('MM-DD HH:mm')),
    recalled: messageRow.recalled === true,
    readByUser: messageRow.readByUser && typeof messageRow.readByUser === 'object'
      ? { ...messageRow.readByUser }
      : {}
  }
}

function buildQuickChatIncrementalEvents(snapshotJson: string) {
  const events: any[] = []
  const currentRooms = (() => {
    try {
      const parsed = JSON.parse(snapshotJson || '[]')
      return Array.isArray(parsed) ? parsed : []
    } catch {
      return [] as any[]
    }
  })()
  const previousRooms = (() => {
    try {
      const parsed = JSON.parse(quickChatLastEventSnapshot.value || '[]')
      return Array.isArray(parsed) ? parsed : []
    } catch {
      return [] as any[]
    }
  })()
  const prevMap = new Map<string, any>(previousRooms.map((room: any) => [String(room?.id || ''), room]))
  const currMap = new Map<string, any>(currentRooms.map((room: any) => [String(room?.id || ''), room]))

  currMap.forEach((room, roomId) => {
    if (!roomId) return
    const targets = roomMemberTargets(room)
    if (!targets.length) return
    const prevRoom = prevMap.get(roomId)
    if (!prevRoom) {
      const roomPayload = normalizeQuickChatEventRoom(room)
      if (roomPayload) {
        events.push({ eventType: 'ROOM_UPSERT', roomId, room: roomPayload, targetStaffIds: targets })
      }
    } else {
      const currUpdated = String(room?.updatedAt || '')
      const prevUpdated = String(prevRoom?.updatedAt || '')
      if (currUpdated !== prevUpdated || String(room?.name || '') !== String(prevRoom?.name || '') || String(room?.notice || '') !== String(prevRoom?.notice || '')) {
        const roomPayload = normalizeQuickChatEventRoom(room)
        if (roomPayload) {
          events.push({ eventType: 'ROOM_UPSERT', roomId, room: roomPayload, targetStaffIds: targets })
        }
      }
      const prevMessages = Array.isArray(prevRoom?.messages) ? prevRoom.messages : []
      const currMessages = Array.isArray(room?.messages) ? room.messages : []
      const prevMsgMap = new Map<string, any>(prevMessages.map((msg: any) => [String(msg?.id || ''), msg]))
      currMessages.forEach((msg: any) => {
        const msgId = String(msg?.id || '')
        if (!msgId) return
        const prevMsg = prevMsgMap.get(msgId)
        const normalizedMsg = normalizeQuickChatEventMessage(msg)
        if (!normalizedMsg) return
        if (!prevMsg) {
          events.push({
            eventType: 'MESSAGE_APPEND',
            roomId,
            message: normalizedMsg,
            meta: { roomUpdatedAt: String(room?.updatedAt || normalizedMsg.createdAt || dayjs().toISOString()) },
            targetStaffIds: targets
          })
          return
        }
        if (Boolean(prevMsg?.recalled) !== Boolean(msg?.recalled) || String(prevMsg?.content || '') !== String(msg?.content || '')) {
          events.push({
            eventType: 'MESSAGE_UPDATE',
            roomId,
            message: normalizedMsg,
            meta: { roomUpdatedAt: String(room?.updatedAt || normalizedMsg.createdAt || dayjs().toISOString()) },
            targetStaffIds: targets
          })
        }
      })
    }
  })

  prevMap.forEach((room, roomId) => {
    if (!roomId || currMap.has(roomId)) return
    const targets = roomMemberTargets(room)
    if (!targets.length) return
    events.push({ eventType: 'ROOM_REMOVE', roomId, targetStaffIds: targets })
  })
  return events
}

async function emitQuickChatIncrementalEvents(snapshotJson: string) {
  if (!snapshotJson) return
  if (snapshotJson === quickChatLastEventSnapshot.value && !quickChatEventQueuedSnapshot.value) return
  const events = buildQuickChatIncrementalEvents(snapshotJson)
  if (!events.length) return
  try {
    await publishQuickChatEventBatch(events)
    quickChatLastEventSnapshot.value = snapshotJson
  } catch {
    quickChatEventQueuedSnapshot.value = snapshotJson
  }
}

function scheduleQuickChatIncrementalEmit(snapshotJson: string) {
  if (!snapshotJson || quickChatApplyingRemoteEvent.value) return
  if (snapshotJson === quickChatLastEventSnapshot.value && !quickChatEventQueuedSnapshot.value) return
  quickChatEventQueuedSnapshot.value = snapshotJson
  if (quickChatEventEmitTimer) {
    window.clearTimeout(quickChatEventEmitTimer)
    quickChatEventEmitTimer = undefined
  }
  quickChatEventEmitTimer = window.setTimeout(() => {
    quickChatEventEmitTimer = undefined
    const queued = quickChatEventQueuedSnapshot.value
    quickChatEventQueuedSnapshot.value = ''
    emitQuickChatIncrementalEvents(queued)
  }, QUICK_CHAT_EVENT_EMIT_DEBOUNCE_MS)
}

function flushQuickChatStatePersist() {
  quickChatLocalPersistTimer = undefined
  try {
    const payload = buildQuickChatStatePayload()
    const payloadJson = JSON.stringify(payload)
    if (payloadJson !== quickChatLastLocalPayload) {
      const storageKey = quickChatStorageKey()
      scheduleQuickChatIdlePersist(() => {
        try {
          localStorage.setItem(storageKey, payloadJson)
          quickChatLastLocalPayload = payloadJson
          emitQuickChatSync()
        } catch {}
      })
    }
    if (!quickChatPersistPending.skipCloud && quickChatOpen.value) {
      scheduleQuickChatCloudSave(payloadJson)
    }
    if (!quickChatPersistPending.skipFanout && quickChatOpen.value) {
      scheduleQuickChatFanout(payloadJson)
    }
    if (!quickChatPersistPending.skipEvent && quickChatOpen.value) {
      scheduleQuickChatIncrementalEmit(payloadJson)
    }
  } catch {}
  quickChatPersistPending.skipCloud = true
  quickChatPersistPending.skipFanout = true
  quickChatPersistPending.skipEvent = true
}

function persistQuickChatState(options?: { skipCloud?: boolean; skipFanout?: boolean; skipEvent?: boolean }) {
  quickChatPersistPending.skipCloud = quickChatPersistPending.skipCloud && options?.skipCloud === true
  quickChatPersistPending.skipFanout = quickChatPersistPending.skipFanout && options?.skipFanout === true
  quickChatPersistPending.skipEvent = quickChatPersistPending.skipEvent && options?.skipEvent === true
  if (quickChatLocalPersistTimer) {
    window.clearTimeout(quickChatLocalPersistTimer)
  }
  quickChatLocalPersistTimer = window.setTimeout(flushQuickChatStatePersist, QUICK_CHAT_LOCAL_PERSIST_DEBOUNCE_MS)
}

function createQuickChatMessage(payload: Omit<QuickChatMessage, 'createdAt' | 'timeText' | 'readByUser'> & { createdAt?: string }) {
  const createdAt = payload.createdAt || dayjs().toISOString()
  const currentId = currentQuickChatKeyId()
  const readByUser: Record<string, boolean> = {
    [currentId]: true
  }
  return {
    ...payload,
    createdAt,
    timeText: dayjs(createdAt).format('MM-DD HH:mm'),
    readByUser
  }
}

function appendRoomSystemMessage(room: QuickChatRoom, text: string, notifyOthers: boolean | string[] = false) {
  const currentId = currentQuickChatKeyId()
  room.messages.push({
    id: `m_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`,
    senderId: 'system',
    senderName: '系统',
    kind: 'SYSTEM',
    content: text,
    timeText: dayjs().format('MM-DD HH:mm'),
    createdAt: dayjs().toISOString(),
    readByUser: Object.fromEntries(
      room.memberIds.map((id) => [id, isCurrentQuickChatIdentity(id)])
    )
  })
  room.updatedAt = dayjs().toISOString()
  const needNotify = notifyOthers === true || (Array.isArray(notifyOthers) && notifyOthers.length > 0)
  if (needNotify) {
    const notifySet = Array.isArray(notifyOthers) ? new Set(notifyOthers) : null
    room.unreadByUser = room.unreadByUser || {}
    room.memberIds.forEach((memberId) => {
      if (isCurrentQuickChatIdentity(memberId)) return
      if (notifySet && !notifySet.has(memberId)) return
      if (!shouldDeliverUnread(room, memberId, notifySet ? Array.from(notifySet) : undefined, true)) return
      room.unreadByUser![memberId] = Number(room.unreadByUser![memberId] || 0) + 1
    })
    room.unreadCount = Number(room.unreadByUser[currentId] || 0)
  }
}

function isRoomArchivedByCurrentUser(room: QuickChatRoom) {
  const archived = resolveRoomArchivedByCurrentUser(room)
  if (!archived) return false
  room.archivedByUser = room.archivedByUser || {}
  const currentId = currentQuickChatKeyId()
  if (currentId && room.archivedByUser[currentId] !== true) {
    room.archivedByUser[currentId] = true
  }
  return true
}

function shouldDeliverUnread(room: QuickChatRoom, memberId: string, mentionTargetIds?: string[], force = false) {
  if (force) return true
  const mode = room.notificationMode || 'ALL'
  if (mode === 'MUTE') return false
  if (mode === 'MENTION') {
    if (!mentionTargetIds?.length) return false
    return mentionTargetIds.includes(memberId)
  }
  if (mentionTargetIds?.length) return mentionTargetIds.includes(memberId)
  return true
}

function changeActiveRoomNotifyMode(value: 'ALL' | 'MENTION' | 'MUTE') {
  const room = activeQuickChatRoom.value
  if (!room) return
  room.notificationMode = value
  room.updatedAt = dayjs().toISOString()
  persistQuickChatState()
  const label = quickChatNotifyModeOptions.find((item) => item.value === value)?.label || value
  message.success(`已切换为${label}`)
}

function toggleArchiveActiveRoom() {
  const room = activeQuickChatRoom.value
  if (!room) return
  room.archivedByUser = room.archivedByUser || {}
  const userId = currentQuickChatKeyId()
  const willArchive = room.archivedByUser[userId] !== true
  room.archivedByUser[userId] = willArchive
  room.updatedAt = dayjs().toISOString()
  persistQuickChatState()
  if (willArchive) {
    message.success('会话已归档')
    ensureActiveQuickChatVisible()
  } else {
    message.success('会话已恢复')
  }
}

function toggleQuickChatBatchMode() {
  quickChatBatchMode.value = !quickChatBatchMode.value
  if (!quickChatBatchMode.value) {
    quickChatBatchSelectedIds.value = []
  }
}

function toggleBatchSelectRoom(roomId: string, checked: boolean) {
  if (!roomId) return
  if (checked) {
    if (!quickChatBatchSelectedIds.value.includes(roomId)) {
      quickChatBatchSelectedIds.value.push(roomId)
    }
    return
  }
  quickChatBatchSelectedIds.value = quickChatBatchSelectedIds.value.filter((id) => id !== roomId)
}

function clearBatchSelection() {
  quickChatBatchSelectedIds.value = []
}

function selectAllFilteredRooms() {
  quickChatBatchSelectedIds.value = filteredQuickChatRooms.value.map((room) => room.id)
}

function markRoomAsRead(room: QuickChatRoom) {
  const currentId = currentQuickChatKeyId()
  room.messages.forEach((msg) => {
    msg.readByUser = msg.readByUser || {}
    msg.readByUser[currentId] = true
  })
  room.unreadByUser = room.unreadByUser || {}
  room.unreadByUser[currentId] = 0
  room.unreadCount = 0
}

function batchMarkSelectedAsRead() {
  if (!quickChatBatchSelectedIds.value.length) return
  const selectedSet = new Set(quickChatBatchSelectedIds.value)
  quickChatRooms.value.forEach((room) => {
    if (!selectedSet.has(room.id)) return
    markRoomAsRead(room)
  })
  persistQuickChatState()
  message.success(`已批量标记 ${selectedSet.size} 个会话为已读`)
}

function batchToggleArchiveSelected() {
  if (!quickChatBatchSelectedIds.value.length) return
  const selectedSet = new Set(quickChatBatchSelectedIds.value)
  const userId = currentQuickChatKeyId()
  quickChatRooms.value.forEach((room) => {
    if (!selectedSet.has(room.id)) return
    room.archivedByUser = room.archivedByUser || {}
    room.archivedByUser[userId] = room.archivedByUser[userId] !== true
    room.updatedAt = dayjs().toISOString()
  })
  persistQuickChatState()
  ensureActiveQuickChatVisible()
  message.success(`已批量处理 ${selectedSet.size} 个会话归档状态`)
}

function loadQuickChatState() {
  try {
    const raw = localStorage.getItem(quickChatStorageKey())
    if (!raw) {
      quickChatRooms.value = []
      activeQuickChatRoomId.value = ''
      quickChatLastEventSnapshot.value = '[]'
      quickChatLastLocalPayload = '[]'
      return
    }
    const parsed = JSON.parse(raw)
    if (!Array.isArray(parsed)) {
      quickChatRooms.value = []
      activeQuickChatRoomId.value = ''
      quickChatLastEventSnapshot.value = '[]'
      quickChatLastLocalPayload = '[]'
      return
    }
    applyQuickChatRoomsFromPayload(parsed)
    quickChatLastEventSnapshot.value = raw
    quickChatLastLocalPayload = raw
  } catch {
    quickChatRooms.value = []
    activeQuickChatRoomId.value = ''
    quickChatLastEventSnapshot.value = '[]'
    quickChatLastLocalPayload = '[]'
  }
}

function clearQuickChatSyncConflict() {
  quickChatSyncConflict.visible = false
  quickChatSyncConflict.direction = ''
  quickChatSyncConflict.localMs = 0
  quickChatSyncConflict.remoteMs = 0
  quickChatSyncConflict.remoteJson = ''
}

function markQuickChatSyncConflict(direction: 'LOCAL_NEWER' | 'CLOUD_NEWER', localMs: number, remoteMs: number, remoteJson: string) {
  quickChatSyncConflict.visible = true
  quickChatSyncConflict.direction = direction
  quickChatSyncConflict.localMs = localMs
  quickChatSyncConflict.remoteMs = remoteMs
  quickChatSyncConflict.remoteJson = remoteJson
}

function dismissQuickChatConflict() {
  quickChatSyncConflict.visible = false
  message.info('已保留冲突状态，稍后可手动同步处理')
}

function resolveQuickChatConflictUseLocal() {
  const localPayloadJson = JSON.stringify(buildQuickChatStatePayload())
  pushQuickChatStateToCloud(localPayloadJson)
  pushQuickChatStateToMembers(localPayloadJson)
  clearQuickChatSyncConflict()
  message.success('已按本地版本覆盖云端')
}

function resolveQuickChatConflictUseCloud() {
  const remoteJson = String(quickChatSyncConflict.remoteJson || '')
  if (!remoteJson) {
    message.warning('云端版本不存在，无法覆盖')
    return
  }
  try {
    const parsed = JSON.parse(remoteJson)
    if (!Array.isArray(parsed)) {
      message.warning('云端数据格式异常，无法覆盖')
      return
    }
    const previousActive = activeQuickChatRoomId.value
    applyQuickChatRoomsFromPayload(parsed, previousActive)
    localStorage.setItem(quickChatStorageKey(), remoteJson)
    emitQuickChatSync()
    quickChatLastEventSnapshot.value = remoteJson
    quickChatCloudLastPayload = remoteJson
    quickChatCloudRemoteUpdatedAt.value = Math.max(quickChatCloudRemoteUpdatedAt.value, Number(quickChatSyncConflict.remoteMs || 0))
    clearQuickChatSyncConflict()
    message.success('已按云端版本覆盖本地')
  } catch {
    message.warning('云端数据解析失败，无法覆盖')
  }
}

async function pullQuickChatStateFromCloud(force = false) {
  if (!quickChatRuntimeStarted.value) return
  if (!userStore.staffInfo?.id) return
  if (quickChatCloudSyncing.value && !force) return
  let status: 'ok' | 'error' | 'conflict' = 'ok'
  let flushQueuedSave = false
  quickChatCloudSyncing.value = true
  try {
    const remote = await getQuickChatState()
    quickChatCloudReady.value = true
    const remoteJson = String(remote?.stateJson || '')
    const serverMs = dayjs(remote?.updateTime || dayjs().toISOString()).valueOf()
    if (!remoteJson) {
      if (localQuickChatLatestMs() > 0) {
        const localPayloadJson = JSON.stringify(buildQuickChatStatePayload())
        scheduleQuickChatCloudSave(localPayloadJson)
      }
      return
    }
    const parsed = JSON.parse(remoteJson)
    if (!Array.isArray(parsed)) return
    const remotePayloadMs = quickChatLatestRoomUpdateMs(parsed as Array<Partial<QuickChatRoom>>)
    const remoteLatestMs = Math.max(serverMs || 0, remotePayloadMs || 0)
    if (!force && remoteLatestMs > 0 && remoteLatestMs <= quickChatCloudRemoteUpdatedAt.value) return
    const localLatestMs = localQuickChatLatestMs()
    if (localLatestMs > remoteLatestMs + 1000) {
      if (force) {
        markQuickChatSyncConflict('LOCAL_NEWER', localLatestMs, remoteLatestMs, remoteJson)
        status = 'conflict'
        return
      }
      // 自动同步场景下，本地领先也要合并云端增量，避免新会话/新消息被本地时间戳压制。
      const mergedRooms = mergeQuickChatRoomRows(quickChatRooms.value, normalizeQuickChatRooms(parsed))
      quickChatRooms.value = mergedRooms
      rehydrateQuickChatUnread()
      ensureActiveQuickChatVisible()
      const mergedPayloadJson = JSON.stringify(mergedRooms.map((room) => buildQuickChatRoomPayload(room)))
      localStorage.setItem(quickChatStorageKey(), mergedPayloadJson)
      emitQuickChatSync()
      quickChatLastEventSnapshot.value = mergedPayloadJson
      quickChatCloudRemoteUpdatedAt.value = remoteLatestMs || Date.now()
      clearQuickChatSyncConflict()
      scheduleQuickChatCloudSave(mergedPayloadJson)
      return
    }
    // 云端更新默认自动吸收，避免接收方被冲突弹层拦住看不到新消息。
    const previousActive = activeQuickChatRoomId.value
    applyQuickChatRoomsFromPayload(parsed, previousActive)
    localStorage.setItem(quickChatStorageKey(), remoteJson)
    quickChatLastEventSnapshot.value = remoteJson
    quickChatCloudRemoteUpdatedAt.value = remoteLatestMs || Date.now()
    quickChatCloudLastPayload = remoteJson
    clearQuickChatSyncConflict()
    flushQueuedSave = true
  } catch (error: any) {
    quickChatCloudReady.value = true
    const statusCode = Number(error?.response?.status || error?.status || 0)
    if (statusCode === 403 && !quickChatPermissionWarned) {
      quickChatPermissionWarned = true
      message.warning('当前账号暂无聊天权限，请联系管理员检查角色配置')
    }
    status = 'error'
  } finally {
    quickChatCloudSyncing.value = false
    if (flushQueuedSave && quickChatCloudQueuedPayload && quickChatCloudQueuedPayload !== quickChatCloudLastPayload && !quickChatCloudSavePending.value) {
      const queued = quickChatCloudQueuedPayload
      quickChatCloudQueuedPayload = ''
      pushQuickChatStateToCloud(queued)
    }
  }
  return status
}

function forceQuickChatCloudSync() {
  ensureQuickChatRuntime({ forcePull: false })
  pullQuickChatStateFromCloud(true)
    .then((status) => {
      if (status === 'error') {
        message.warning('同步失败，已保留本地数据')
        return
      }
      if (status === 'conflict') {
        message.warning('检测到同步冲突，请先选择处理策略')
        return
      }
      message.success('已完成云端同步')
    })
}

function setupQuickChatCloudPullTimer() {
  if (!quickChatRuntimeStarted.value) return
  if (quickChatCloudPullTimer) {
    window.clearInterval(quickChatCloudPullTimer)
    quickChatCloudPullTimer = undefined
  }
  quickChatCloudPullTimer = window.setInterval(() => {
    if (quickChatFanoutQueuedPayload.value) {
      pushQuickChatStateToMembers(quickChatFanoutQueuedPayload.value)
    }
    pullQuickChatStateFromCloud(false)
  }, QUICK_CHAT_CLOUD_PULL_INTERVAL_MS)
}

function onQuickChatCloudVisibilityChange() {
  if (!quickChatRuntimeStarted.value) return
  if (document.visibilityState !== 'visible') return
  if (!quickChatWsConnected.value) {
    quickChatWebSocketManualClose = false
    connectQuickChatWebSocket()
  }
  if (quickChatFanoutQueuedPayload.value) {
    pushQuickChatStateToMembers(quickChatFanoutQueuedPayload.value)
  }
  pullQuickChatStateFromCloud(false)
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
  if (!eventType) return
  if (eventType === 'WS_READY') return
  if (eventType === 'SYNC_HINT') {
    pullQuickChatStateFromCloud(false)
    return
  }
  const roomId = String(eventRow?.roomId || eventRow?.room?.id || '')
  quickChatApplyingRemoteEvent.value = true
  try {
    if (eventType === 'ROOM_REMOVE') {
      if (roomId) {
        quickChatRooms.value = quickChatRooms.value.filter((item) => item.id !== roomId)
      }
      ensureActiveQuickChatVisible()
      persistQuickChatState({ skipCloud: true, skipFanout: true, skipEvent: true })
      return
    }
    if (eventType === 'ROOM_UPSERT') {
      const roomPayload = eventRow?.room
      if (!roomPayload || typeof roomPayload !== 'object') return
      const existed = quickChatRooms.value.find((item) => item.id === String(roomPayload.id || roomId))
      if (!existed) {
        quickChatRooms.value.unshift({
          id: String(roomPayload.id || roomId || `room_${Date.now()}`),
          name: String(roomPayload.name || '未命名群聊'),
          departmentIds: Array.isArray(roomPayload.departmentIds) ? roomPayload.departmentIds.map((id: any) => String(id)) : [],
          memberIds: Array.isArray(roomPayload.memberIds)
            ? Array.from(new Set(roomPayload.memberIds.map((id: any) => normalizeQuickChatMemberId(id)).filter(Boolean)))
            : [],
          messages: [],
          unreadCount: 0,
          unreadByUser: { [currentQuickChatKeyId()]: 0 },
          pinned: roomPayload.pinned === true,
          notificationMode: (['ALL', 'MENTION', 'MUTE'].includes(String(roomPayload.notificationMode || 'ALL')) ? String(roomPayload.notificationMode) : 'ALL') as QuickChatRoom['notificationMode'],
          notice: String(roomPayload.notice || ''),
          archivedByUser: {},
          createdAt: String(roomPayload.createdAt || dayjs().toISOString()),
          updatedAt: String(roomPayload.updatedAt || roomPayload.createdAt || dayjs().toISOString())
        })
      } else {
        existed.name = String(roomPayload.name || existed.name || '未命名群聊')
        existed.departmentIds = Array.isArray(roomPayload.departmentIds) ? roomPayload.departmentIds.map((id: any) => String(id)) : existed.departmentIds
        existed.memberIds = Array.isArray(roomPayload.memberIds)
          ? Array.from(new Set(roomPayload.memberIds.map((id: any) => normalizeQuickChatMemberId(id)).filter(Boolean)))
          : existed.memberIds
        existed.notice = String(roomPayload.notice || existed.notice || '')
        existed.notificationMode = (['ALL', 'MENTION', 'MUTE'].includes(String(roomPayload.notificationMode || existed.notificationMode || 'ALL'))
          ? String(roomPayload.notificationMode || existed.notificationMode || 'ALL')
          : 'ALL') as QuickChatRoom['notificationMode']
        existed.pinned = roomPayload.pinned === true
        existed.updatedAt = String(roomPayload.updatedAt || dayjs().toISOString())
      }
      ensureActiveQuickChatVisible()
      persistQuickChatState({ skipCloud: true, skipFanout: true, skipEvent: true })
      return
    }
    if (eventType === 'MESSAGE_APPEND' || eventType === 'MESSAGE_UPDATE') {
      if (!roomId) return
      const room = quickChatRooms.value.find((item) => item.id === roomId)
      if (!room) {
        // 接收到消息但本地还没有房间时，直接强制吸收云端，避免因本地时间戳领先而漏消息。
        pullQuickChatStateFromCloud(false)
        return
      }
      const messagePayload = eventRow?.message
      if (!messagePayload || typeof messagePayload !== 'object') return
      const msgId = String(messagePayload.id || '')
      if (!msgId) return
      const existedMsg = room.messages.find((msg) => msg.id === msgId)
      if (!existedMsg) {
        room.messages.push({
          id: msgId,
          senderId: String(messagePayload.senderId || 'system'),
          senderName: String(messagePayload.senderName || '系统'),
          kind: (['TEXT', 'IMAGE', 'FILE', 'SYSTEM'].includes(String(messagePayload.kind || 'TEXT')) ? String(messagePayload.kind || 'TEXT') : 'TEXT') as QuickChatMessage['kind'],
          content: String(messagePayload.content || ''),
          fileName: messagePayload.fileName ? String(messagePayload.fileName) : undefined,
          fileUrl: messagePayload.fileUrl ? String(messagePayload.fileUrl) : undefined,
          timeText: String(messagePayload.timeText || dayjs().format('MM-DD HH:mm')),
          createdAt: String(messagePayload.createdAt || dayjs().toISOString()),
          recalled: messagePayload.recalled === true,
          readByUser: typeof messagePayload.readByUser === 'object' && messagePayload.readByUser
            ? Object.fromEntries(Object.entries(messagePayload.readByUser).map(([k, v]) => [String(k), Boolean(v)]))
            : {}
        })
      } else {
        existedMsg.content = String(messagePayload.content || existedMsg.content || '')
        existedMsg.fileName = messagePayload.fileName ? String(messagePayload.fileName) : existedMsg.fileName
        existedMsg.fileUrl = messagePayload.fileUrl ? String(messagePayload.fileUrl) : existedMsg.fileUrl
        existedMsg.recalled = messagePayload.recalled === true
        existedMsg.readByUser = typeof messagePayload.readByUser === 'object' && messagePayload.readByUser
          ? Object.fromEntries(Object.entries(messagePayload.readByUser).map(([k, v]) => [String(k), Boolean(v)]))
          : existedMsg.readByUser
      }
      room.updatedAt = String(eventRow?.meta?.roomUpdatedAt || messagePayload.createdAt || dayjs().toISOString())
      const currentId = currentQuickChatKeyId()
      room.unreadByUser = room.unreadByUser || {}
      const viewingCurrentRoom = quickChatOpen.value && activeQuickChatRoomId.value === roomId
      const senderId = String(messagePayload.senderId || '')
      if (eventType === 'MESSAGE_APPEND') {
        if (viewingCurrentRoom) {
          room.messages.forEach((msg) => {
            msg.readByUser = msg.readByUser || {}
            msg.readByUser[currentId] = true
          })
          room.unreadByUser[currentId] = 0
        } else if (senderId && !isCurrentQuickChatIdentity(senderId)) {
          room.unreadByUser[currentId] = Number(room.unreadByUser[currentId] || 0) + 1
        }
      }
      room.unreadCount = Number(room.unreadByUser[currentId] || 0)
      persistQuickChatState({ skipCloud: true, skipFanout: true, skipEvent: true })
      return
    }
  } finally {
    quickChatApplyingRemoteEvent.value = false
  }
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
  quickChatWsReconnectAttempt.value += 1
  const delay = Math.min(3000 + quickChatWsReconnectAttempt.value * 1000, 12000)
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
    quickChatWsConnected.value = true
    quickChatWsReconnectAttempt.value = 0
    pullQuickChatStateFromCloud(false)
  }
  quickChatWebSocket.onmessage = (event) => {
    handleQuickChatWebSocketMessage(String(event?.data || ''))
  }
  quickChatWebSocket.onerror = () => {
    quickChatWsConnected.value = false
  }
  quickChatWebSocket.onclose = (event) => {
    quickChatWsConnected.value = false
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
  quickChatWsConnected.value = false
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
  quickChatRooms.value = quickChatRooms.value.map((room) => {
    const unreadByUser = room.unreadByUser || {}
    return {
      ...room,
      unreadByUser,
      unreadCount: resolveRoomUnreadForCurrentUser({ ...room, unreadByUser } as QuickChatRoom)
    }
  })
}

function ensureActiveQuickChatVisible() {
  if (activeQuickChatRoomId.value && visibleQuickChatRooms.value.some((room) => room.id === activeQuickChatRoomId.value)) return
  activeQuickChatRoomId.value = sortedQuickChatRooms.value[0]?.id || ''
}

function syncQuickChatFromStorage() {
  const previousActive = activeQuickChatRoomId.value
  loadQuickChatState()
  if (previousActive && visibleQuickChatRooms.value.some((item) => item.id === previousActive)) {
    activeQuickChatRoomId.value = previousActive
  }
  rehydrateQuickChatUnread()
  ensureActiveQuickChatVisible()
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
  ensureQuickChatRuntime()
  if (!activeQuickChatRoomId.value && sortedQuickChatRooms.value.length) {
    activeQuickChatRoomId.value = sortedQuickChatRooms.value[0].id
  }
  if (activeQuickChatRoom.value) {
    const currentId = currentQuickChatKeyId()
    let readChanged = false
    activeQuickChatRoom.value.messages.forEach((msg) => {
      msg.readByUser = msg.readByUser || {}
      if (!msg.readByUser[currentId]) readChanged = true
      msg.readByUser[currentId] = true
    })
    const unreadChanged = Number(activeQuickChatRoom.value.unreadCount || 0) > 0
    activeQuickChatRoom.value.unreadCount = 0
    activeQuickChatRoom.value.unreadByUser = activeQuickChatRoom.value.unreadByUser || {}
    activeQuickChatRoom.value.unreadByUser[currentId] = 0
    if (unreadChanged || readChanged) persistQuickChatState()
  }
  scrollQuickChatToBottom()
}

function selectQuickChatRoom(roomId: string) {
  activeQuickChatRoomId.value = roomId
  const room = quickChatRooms.value.find((item) => item.id === roomId)
  if (!room) return
  const currentId = currentQuickChatKeyId()
  let readChanged = false
  room.messages.forEach((msg) => {
    msg.readByUser = msg.readByUser || {}
    if (!msg.readByUser[currentId]) readChanged = true
    msg.readByUser[currentId] = true
  })
  room.unreadByUser = room.unreadByUser || {}
  let needPersist = false
  if (Number(room.unreadByUser[currentId] || 0) > 0 || room.unreadCount > 0) {
    room.unreadByUser[currentId] = 0
    room.unreadCount = 0
    needPersist = true
  }
  if (readChanged) needPersist = true
  if (needPersist) persistQuickChatState()
  scrollQuickChatToBottom()
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

function openRenameQuickChatRoom() {
  if (!activeQuickChatRoom.value) {
    message.info('请先选择一个会话')
    return
  }
  quickChatRoomForm.name = activeQuickChatRoom.value.name
  quickChatRoomForm.departmentIds = [...activeQuickChatRoom.value.departmentIds]
  quickChatRoomForm.memberIds = [...activeQuickChatRoom.value.memberIds]
  quickChatRoomForm.applyRemark = ''
  quickChatRoomEditorMode.value = 'rename'
  quickChatRoomEditorOpen.value = true
}

function memberNameById(staffId: string) {
  const matched = staffOptions.value.find((item) => item.value === staffId || item.username === staffId)
  return matched?.name || `员工#${staffId}`
}

function expandDepartmentMemberIds(departmentIds: string[]) {
  if (!departmentIds.length) return [] as string[]
  const set = new Set(departmentIds.map((id) => String(id)))
  return staffOptions.value
    .filter((item) => item.departmentId && set.has(String(item.departmentId)))
    .map((item) => String(item.username || item.value))
}

function ensureQuickChatSenderReady() {
  if (currentQuickChatActorId.value) return true
  message.warning('账号信息尚未同步完成，请稍后重试')
  return false
}

function reconcileQuickChatSelfIdentity() {
  const selfId = currentQuickChatSenderNumericId.value || currentQuickChatSenderId.value
  if (!selfId) return
  const username = String(userStore.staffInfo?.username || '').trim()
  const aliasSet = new Set<string>(['me', currentQuickChatSenderId.value])
  if (username) aliasSet.add(username)
  aliasSet.delete(selfId)
  if (!aliasSet.size) return
  if (username) {
    quickChatAliasIdMap[username] = selfId
  }
  quickChatAliasIdMap[selfId] = selfId
  let changed = false
  quickChatRooms.value.forEach((room) => {
    let roomChanged = false
    const normalizedMembers = room.memberIds.map((id) => (aliasSet.has(id) ? selfId : id))
    room.memberIds = Array.from(new Set(normalizedMembers))
    room.unreadByUser = room.unreadByUser || {}
    room.archivedByUser = room.archivedByUser || {}
    Array.from(aliasSet).forEach((alias) => {
      if (room.unreadByUser && room.unreadByUser[alias] != null) {
        room.unreadByUser[selfId] = Math.max(
          Number(room.unreadByUser[selfId] || 0),
          Number(room.unreadByUser[alias] || 0)
        )
        delete room.unreadByUser[alias]
        roomChanged = true
      }
      if (room.archivedByUser && room.archivedByUser[alias] != null) {
        if (room.archivedByUser[selfId] == null) {
          room.archivedByUser[selfId] = room.archivedByUser[alias] === true
        }
        delete room.archivedByUser[alias]
        roomChanged = true
      }
    })
    room.messages.forEach((msg) => {
      if (aliasSet.has(msg.senderId)) {
        msg.senderId = selfId
        roomChanged = true
      }
      if (!msg.readByUser) return
      Array.from(aliasSet).forEach((alias) => {
        if (msg.readByUser && msg.readByUser[alias] != null) {
          msg.readByUser[selfId] = Boolean(msg.readByUser[selfId] || msg.readByUser[alias])
          delete msg.readByUser[alias]
          roomChanged = true
        }
      })
    })
    if (roomChanged) {
      room.unreadCount = Number(room.unreadByUser[selfId] || 0)
      room.updatedAt = dayjs().toISOString()
      changed = true
    }
  })
  if (changed) {
    persistQuickChatState()
  }
}

function escalationLeaderNames() {
  const directId = userStore.staffInfo?.directLeaderId == null ? '' : String(userStore.staffInfo.directLeaderId)
  const indirectId = userStore.staffInfo?.indirectLeaderId == null ? '' : String(userStore.staffInfo.indirectLeaderId)
  const directName = directId ? memberNameById(directId) : ''
  const indirectName = indirectId && indirectId !== directId ? memberNameById(indirectId) : ''
  return {
    directId,
    indirectId,
    directName,
    indirectName
  }
}

function departmentNameById(departmentId: string) {
  const matched = departmentOptions.value.find((item) => item.value === departmentId)
  return matched?.name || `部门#${departmentId}`
}

function quickChatLastMessageText(room: QuickChatRoom) {
  const latest = room.messages[room.messages.length - 1]
  if (!latest) return '暂无消息'
  if (latest.recalled) return '[消息已撤回]'
  if (latest.kind === 'IMAGE') return `${latest.senderName}: [图片]`
  if (latest.kind === 'FILE') return `${latest.senderName}: [文件] ${latest.fileName || ''}`.trim()
  if (latest.kind === 'SYSTEM') return `[系统] ${latest.content}`
  return `${latest.senderName}: ${latest.content}`
}

function quickChatRoomSlaTag(room: QuickChatRoom) {
  const unread = Number(room.unreadCount || 0)
  if (!unread) return null
  const latest = room.messages[room.messages.length - 1]
  const referenceAt = latest?.createdAt || room.updatedAt || room.createdAt
  const ageHour = Math.max(0, dayjs().diff(dayjs(referenceAt), 'hour', true))
  if (ageHour >= 8) return { text: '严重超时', color: 'red' }
  if (ageHour >= 2) return { text: '即将超时', color: 'orange' }
  return { text: '待跟进', color: 'blue' }
}

function openQuickChatTodoDrawer() {
  quickChatTodoOpen.value = true
}

function quickChatTodoPriorityLabel(priority: QuickChatTodoItem['priority']) {
  if (priority === 'HIGH') return '高'
  if (priority === 'LOW') return '低'
  return '中'
}

function quickChatTodoPriorityColor(priority: QuickChatTodoItem['priority']) {
  if (priority === 'HIGH') return 'red'
  if (priority === 'LOW') return 'blue'
  return 'orange'
}

function resolveQuickChatTodoDueAt(rule: typeof quickChatTodoForm.dueRule) {
  const now = dayjs()
  if (rule === 'NO_DUE') return undefined
  if (rule === 'TODAY_18') return now.hour(18).minute(0).second(0).millisecond(0).toISOString()
  if (rule === 'THREE_DAYS_18') return now.add(3, 'day').hour(18).minute(0).second(0).millisecond(0).toISOString()
  return now.add(1, 'day').hour(18).minute(0).second(0).millisecond(0).toISOString()
}

function formatQuickChatTodoDueAtText(dueAt?: string) {
  if (!dueAt) return ''
  return dayjs(dueAt).format('MM-DD HH:mm')
}

function isQuickChatTodoOverdue(item: QuickChatTodoItem) {
  if (!item.dueAt) return false
  return dayjs().isAfter(dayjs(item.dueAt))
}

function quickChatTodoOverdueHours(item: QuickChatTodoItem) {
  if (!item.dueAt || item.done) return 0
  const diff = dayjs().diff(dayjs(item.dueAt), 'hour', true)
  return Math.max(0, diff)
}

function quickChatTodoEscalationTag(item: QuickChatTodoItem) {
  const level = Number(item.escalationLevel || 0)
  if (level >= 2) return { text: `二级升级（≥${quickChatTodoEscalationHours.level2}h）`, color: 'red' }
  if (level >= 1) return { text: `一级升级（≥${quickChatTodoEscalationHours.level1}h）`, color: 'orange' }
  return null
}

function onEscalationHoursChange() {
  let level1 = Math.min(Math.max(Number(quickChatTodoEscalationHours.level1 || 4), 1), 72)
  let level2 = Math.min(Math.max(Number(quickChatTodoEscalationHours.level2 || 24), 2), 168)
  if (level2 <= level1) level2 = level1 + 1
  quickChatTodoEscalationHours.level1 = level1
  quickChatTodoEscalationHours.level2 = level2
  persistQuickChatTodoAutomation()
}

function quickChatTodoDescription(item: QuickChatTodoItem) {
  const parts = [item.roomName, item.createdAtText]
  if (item.creatorName) parts.push(`创建人 ${item.creatorName}`)
  if (item.assigneeName) parts.push(`责任人 ${item.assigneeName}`)
  if (item.dueAtText) parts.push(`截止 ${item.dueAtText}`)
  const overdueHours = quickChatTodoOverdueHours(item)
  if (overdueHours > 0) parts.push(`逾期 ${Math.floor(overdueHours)}h`)
  if (item.lastEscalatedAt) parts.push(`最近升级 ${dayjs(item.lastEscalatedAt).format('MM-DD HH:mm')}`)
  return parts.filter(Boolean).join(' · ')
}

function resetQuickChatTodoEditorForm() {
  const room = activeQuickChatRoom.value
  quickChatTodoForm.roomId = room?.id || ''
  quickChatTodoForm.roomName = room?.name || ''
  quickChatTodoForm.messageId = ''
  quickChatTodoForm.content = ''
  quickChatTodoForm.assigneeId = currentQuickChatSenderId.value
  quickChatTodoForm.priority = 'MEDIUM'
  quickChatTodoForm.dueRule = 'TOMORROW_18'
}

function openQuickChatTodoEditorManual() {
  resetQuickChatTodoEditorForm()
  quickChatTodoEditorMode.value = 'create'
  quickChatTodoEditingId.value = ''
  quickChatTodoEditorOpen.value = true
}

function addMessageToQuickChatTodo(messageRow: QuickChatMessage) {
  const room = activeQuickChatRoom.value
  if (!room) return
  if (messageRow.recalled) {
    message.warning('已撤回消息不可转待办')
    return
  }
  const existed = quickChatTodoItems.value.find((item) => item.messageId === messageRow.id && item.roomId === room.id)
  if (existed) {
    message.info('该消息已在聊天待办中')
    return
  }
  resetQuickChatTodoEditorForm()
  quickChatTodoForm.roomId = room.id
  quickChatTodoForm.roomName = room.name
  quickChatTodoForm.messageId = messageRow.id
  const content = messageRow.kind === 'IMAGE'
    ? `[图片] ${messageRow.fileName || ''}`.trim()
    : messageRow.kind === 'FILE'
      ? `[文件] ${messageRow.fileName || ''}`.trim()
      : (messageRow.content || '').trim()
  quickChatTodoForm.content = `${messageRow.senderName}：${content || '（空消息）'}`
  quickChatTodoEditorMode.value = 'create'
  quickChatTodoEditingId.value = ''
  quickChatTodoEditorOpen.value = true
}

function openQuickChatTodoEditor(todoId: string) {
  const row = quickChatTodoItems.value.find((item) => item.id === todoId)
  if (!row) return
  quickChatTodoEditorMode.value = 'edit'
  quickChatTodoEditingId.value = todoId
  quickChatTodoForm.roomId = row.roomId
  quickChatTodoForm.roomName = row.roomName
  quickChatTodoForm.messageId = row.messageId
  quickChatTodoForm.content = row.content
  quickChatTodoForm.assigneeId = row.assigneeId || currentQuickChatSenderId.value
  quickChatTodoForm.priority = row.priority || 'MEDIUM'
  if (!row.dueAt) {
    quickChatTodoForm.dueRule = 'NO_DUE'
  } else {
    const diffHour = dayjs(row.dueAt).diff(dayjs(), 'hour')
    if (diffHour <= 12) quickChatTodoForm.dueRule = 'TODAY_18'
    else if (diffHour <= 36) quickChatTodoForm.dueRule = 'TOMORROW_18'
    else quickChatTodoForm.dueRule = 'THREE_DAYS_18'
  }
  quickChatTodoEditorOpen.value = true
}

function submitQuickChatTodoEditor() {
  const content = String(quickChatTodoForm.content || '').trim()
  if (!content) {
    message.warning('请填写待办内容')
    return
  }
  const roomId = quickChatTodoForm.roomId || activeQuickChatRoom.value?.id || ''
  const roomName = quickChatTodoForm.roomName || activeQuickChatRoom.value?.name || '未知会话'
  const dueAt = resolveQuickChatTodoDueAt(quickChatTodoForm.dueRule)
  const dueAtText = formatQuickChatTodoDueAtText(dueAt)
  const assigneeId = quickChatTodoForm.assigneeId || currentQuickChatSenderId.value
  const assigneeName = memberNameById(assigneeId)
  if (quickChatTodoEditorMode.value === 'edit') {
    const row = quickChatTodoItems.value.find((item) => item.id === quickChatTodoEditingId.value)
    if (!row) return
    row.content = content
    row.roomId = roomId
    row.roomName = roomName
    row.assigneeId = assigneeId
    row.assigneeName = assigneeName
    row.priority = quickChatTodoForm.priority
    row.dueAt = dueAt
    row.dueAtText = dueAtText
    row.calendarSynced = false
    row.calendarTaskId = undefined
    if (!row.creatorId) row.creatorId = currentQuickChatSenderId.value
    if (!row.creatorName) row.creatorName = currentQuickChatSenderName.value
    row.escalationLevel = 0
    row.lastEscalatedAt = undefined
    persistQuickChatTodo()
    quickChatTodoEditorOpen.value = false
    message.success('聊天待办已更新')
    return
  }
  quickChatTodoItems.value.unshift({
    id: `todo_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`,
    roomId,
    roomName,
    messageId: quickChatTodoForm.messageId,
    content,
    creatorId: currentQuickChatSenderId.value,
    creatorName: currentQuickChatSenderName.value,
    assigneeId,
    assigneeName,
    priority: quickChatTodoForm.priority,
    dueAt,
    dueAtText,
    escalationLevel: 0,
    calendarSynced: false,
    calendarTaskId: undefined,
    doneAt: undefined,
    createdAt: dayjs().toISOString(),
    createdAtText: dayjs().format('MM-DD HH:mm'),
    done: false
  })
  persistQuickChatTodo()
  quickChatTodoEditorOpen.value = false
  message.success('已加入聊天待办')
}

function toggleQuickChatTodoDone(todoId: string) {
  const row = quickChatTodoItems.value.find((item) => item.id === todoId)
  if (!row) return
  row.done = !row.done
  row.doneAt = row.done ? dayjs().toISOString() : undefined
  if (row.done) {
    const room = quickChatRooms.value.find((item) => item.id === row.roomId)
    if (room) {
      appendRoomSystemMessage(room, `${currentQuickChatSenderName.value} 已完成待办「${row.content}」`)
      persistQuickChatState()
    }
  }
  persistQuickChatTodo()
}

function removeQuickChatTodo(todoId: string) {
  quickChatTodoItems.value = quickChatTodoItems.value.filter((item) => item.id !== todoId)
  persistQuickChatTodo()
}

function clearCompletedQuickChatTodo() {
  quickChatTodoItems.value = quickChatTodoItems.value.filter((item) => !item.done)
  persistQuickChatTodo()
  message.success('已清理已完成待办')
}

function nudgeQuickChatTodo(todoId: string, silent = false) {
  const row = quickChatTodoItems.value.find((item) => item.id === todoId)
  if (!row || row.done) return
  const room = quickChatRooms.value.find((item) => item.id === row.roomId)
  if (!room) {
    message.warning('关联会话不存在')
    return
  }
  const targetIds = row.assigneeId ? [row.assigneeId] : room.memberIds.filter((id) => !isCurrentQuickChatIdentity(id))
  const assignee = row.assigneeName || (row.assigneeId ? memberNameById(row.assigneeId) : '相关负责人')
  const dueText = row.dueAtText ? `，截止 ${row.dueAtText}` : ''
  appendRoomSystemMessage(room, `${currentQuickChatSenderName.value} 催办待办「${row.content}」→ ${assignee}${dueText}`, targetIds)
  persistQuickChatState()
  if (!silent) message.success('催办消息已发送')
}

function evaluateQuickChatTodoEscalationLevel(item: QuickChatTodoItem) {
  const overdueHours = quickChatTodoOverdueHours(item)
  if (overdueHours >= Number(quickChatTodoEscalationHours.level2 || 24)) return 2
  if (overdueHours >= Number(quickChatTodoEscalationHours.level1 || 4)) return 1
  return 0
}

function escalateQuickChatTodo(todoId: string, silent = false, forcedLevel?: 1 | 2) {
  const row = quickChatTodoItems.value.find((item) => item.id === todoId)
  if (!row || row.done || !isQuickChatTodoOverdue(row)) return
  const room = quickChatRooms.value.find((item) => item.id === row.roomId)
  if (!room) return
  const overdueHours = quickChatTodoOverdueHours(row)
  const targetLevel = forcedLevel || (evaluateQuickChatTodoEscalationLevel(row) as 0 | 1 | 2)
  if (!targetLevel) return
  if (!forcedLevel && Number(row.escalationLevel || 0) >= targetLevel) return
  const level = targetLevel >= 2 ? '二级升级' : '一级升级'
  const dueText = row.dueAtText ? `，原截止 ${row.dueAtText}` : ''
  const leaders = escalationLeaderNames()
  const targetIds = targetLevel >= 2
    ? room.memberIds.filter((id) => !isCurrentQuickChatIdentity(id))
    : (row.assigneeId ? [row.assigneeId] : room.memberIds.filter((id) => !isCurrentQuickChatIdentity(id)))
  if (leaders.directId && room.memberIds.includes(leaders.directId) && !targetIds.includes(leaders.directId)) {
    targetIds.push(leaders.directId)
  }
  if (targetLevel >= 2 && leaders.indirectId && room.memberIds.includes(leaders.indirectId) && !targetIds.includes(leaders.indirectId)) {
    targetIds.push(leaders.indirectId)
  }
  const ccNames: string[] = []
  if (targetLevel >= 1 && leaders.directName) ccNames.push(leaders.directName)
  if (targetLevel >= 2 && leaders.indirectName) ccNames.push(leaders.indirectName)
  appendRoomSystemMessage(
    room,
    `${level}催办：待办「${row.content}」已逾期 ${Math.floor(overdueHours)} 小时${dueText}${ccNames.length ? `（抄送：${ccNames.join('、')}）` : ''}，请立即处理。`,
    targetIds
  )
  row.escalationLevel = targetLevel as 1 | 2
  row.lastEscalatedAt = dayjs().toISOString()
  persistQuickChatTodo()
  persistQuickChatState()
  if (!silent) message.success(`${level}催办已发送`)
}

function nudgeOverdueQuickChatTodos() {
  const overdueRows = quickChatTodoItems.value.filter((item) => !item.done && isQuickChatTodoOverdue(item))
  if (!overdueRows.length) return
  overdueRows.forEach((item) => nudgeQuickChatTodo(item.id, true))
  message.success(`已催办 ${overdueRows.length} 条逾期待办`)
}

function escalateOverdueQuickChatTodos() {
  const overdueRows = quickChatTodoItems.value.filter((item) => !item.done && isQuickChatTodoOverdue(item))
  if (!overdueRows.length) return
  overdueRows.forEach((item) => escalateQuickChatTodo(item.id, true, evaluateQuickChatTodoEscalationLevel(item) as 1 | 2))
  message.success(`已升级催办 ${overdueRows.length} 条逾期待办`)
}

function resolveQuickChatTodoCalendarAssigneeId(assigneeId?: string) {
  const normalized = String(assigneeId || '').trim()
  if (!normalized) return undefined
  const numericSelfId = currentQuickChatSenderNumericId.value
  if (normalized === currentQuickChatSenderId.value && numericSelfId) {
    return Number(numericSelfId)
  }
  if (!/^\d+$/.test(normalized)) return undefined
  const parsed = Number(normalized)
  return Number.isSafeInteger(parsed) && parsed > 0 ? parsed : undefined
}

function quickChatTodoCalendarQuery(row: QuickChatTodoItem, taskId?: string) {
  return {
    from: 'quickChatTodo',
    date: row.dueAt ? dayjs(row.dueAt).format('YYYY-MM-DD') : undefined,
    taskId: taskId || row.calendarTaskId || undefined
  }
}

async function syncQuickChatTodoToCalendar(todoId: string) {
  const row = quickChatTodoItems.value.find((item) => item.id === todoId)
  if (!row || !row.dueAt) return
  const date = dayjs(row.dueAt).format('YYYY-MM-DD')
  try {
    if (row.calendarTaskId) {
      router.push({
        path: '/oa/work-execution/calendar',
        query: quickChatTodoCalendarQuery(row)
      })
      quickChatTodoOpen.value = false
      message.success(`该待办已同步到日历（${date}）`)
      return
    }
    const title = `【聊天待办】${row.content}`
    const endAt = dayjs(row.dueAt)
    const startAt = endAt.subtract(30, 'minute')
    const created = await createOaTask({
      title,
      description: `来源会话：${row.roomName}${row.assigneeName ? `\n责任人：${row.assigneeName}` : ''}${row.createdAtText ? `\n创建时间：${row.createdAtText}` : ''}`,
      startTime: startAt.format('YYYY-MM-DDTHH:mm:ss'),
      endTime: endAt.format('YYYY-MM-DDTHH:mm:ss'),
      priority: row.priority === 'MEDIUM' ? 'NORMAL' : row.priority,
      status: 'OPEN',
      assigneeId: resolveQuickChatTodoCalendarAssigneeId(row.assigneeId),
      assigneeName: row.assigneeName || currentQuickChatSenderName.value,
      calendarType: 'WORK',
      planCategory: '聊天待办',
      urgency: row.priority === 'HIGH' ? 'EMERGENCY' : 'NORMAL'
    })
    row.calendarTaskId = created?.id != null ? String(created.id) : undefined
    row.calendarSynced = true
    persistQuickChatTodo()
    router.push({
      path: '/oa/work-execution/calendar',
      query: quickChatTodoCalendarQuery(row, row.calendarTaskId)
    })
    quickChatTodoOpen.value = false
    message.success(`已同步到协同日历（${date}）`)
  } catch (error: any) {
    message.error(error?.message || '同步到日历失败')
  }
}

function exportQuickChatTodoReport() {
  const rows = [...quickChatTodoItems.value]
  if (!rows.length) {
    message.info('暂无可导出的聊天待办')
    return
  }
  const lines: string[] = []
  lines.push(`导出时间：${dayjs().format('YYYY-MM-DD HH:mm:ss')}`)
  lines.push(`总数：${rows.length}，待完成：${quickChatTodoPendingCount.value}，逾期：${quickChatTodoOverdueCount.value}，完成率：${quickChatTodoCompletionRate.value}%`)
  lines.push('--- 明细 ---')
  rows.forEach((item, idx) => {
    const state = item.done ? '已完成' : (isQuickChatTodoOverdue(item) ? '逾期' : '进行中')
    lines.push(`${idx + 1}. [${state}] [${quickChatTodoPriorityLabel(item.priority)}] ${item.content}`)
    lines.push(`   会话：${item.roomName} | 责任人：${item.assigneeName || '-'} | 创建：${item.createdAtText} | 截止：${item.dueAtText || '-'}`)
  })
  const blob = new Blob([lines.join('\n')], { type: 'text/plain;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const anchor = document.createElement('a')
  anchor.href = url
  anchor.download = `聊天待办看板-${dayjs().format('YYYYMMDD-HHmmss')}.txt`
  document.body.appendChild(anchor)
  anchor.click()
  document.body.removeChild(anchor)
  URL.revokeObjectURL(url)
  message.success('聊天待办看板已导出')
}

function openQuickChatTodoWeeklySummary() {
  quickChatTodoWeeklyOpen.value = true
}

function exportQuickChatTodoWeeklySummary() {
  const stats = quickChatTodoWeeklyStats.value
  const lines: string[] = []
  lines.push(`周报周期：${stats.rangeText}`)
  lines.push(`新增待办：${stats.created}`)
  lines.push(`完成待办：${stats.completed}`)
  lines.push(`完成率：${stats.completionRate}%`)
  lines.push(`逾期未完成：${stats.overduePending}`)
  lines.push(`升级催办：${stats.escalated}`)
  lines.push(`导出时间：${dayjs().format('YYYY-MM-DD HH:mm:ss')}`)
  const blob = new Blob([lines.join('\n')], { type: 'text/plain;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const anchor = document.createElement('a')
  anchor.href = url
  anchor.download = `聊天待办周报-${dayjs().format('YYYYMMDD-HHmmss')}.txt`
  document.body.appendChild(anchor)
  anchor.click()
  document.body.removeChild(anchor)
  URL.revokeObjectURL(url)
  message.success('周报摘要已导出')
}

function jumpToQuickChatTodo(todoId: string) {
  const row = quickChatTodoItems.value.find((item) => item.id === todoId)
  if (!row) return
  quickChatOpen.value = true
  quickChatTodoOpen.value = false
  if (!row.done) row.done = true
  activeQuickChatRoomId.value = row.roomId
  persistQuickChatTodo()
  nextTick(() => {
    const el = quickChatMessagesRef.value?.querySelector(`[data-message-id="${row.messageId}"]`) as HTMLElement | null
    if (!el) return
    el.scrollIntoView({ block: 'center', behavior: 'smooth' })
    el.classList.add('quick-chat-message-focus')
    window.setTimeout(() => el.classList.remove('quick-chat-message-focus'), 1200)
  })
}

function submitQuickChatRoomEditor() {
  if (!ensureQuickChatSenderReady()) return
  const name = String(quickChatRoomForm.name || '').trim()
  if (!name) {
    message.warning('请填写群聊名称')
    return
  }
  const selfId = currentQuickChatKeyId()
  const selectedDepartments = Array.from(new Set((quickChatRoomForm.departmentIds || []).map((id) => String(id))))
  const departmentMemberIds = expandDepartmentMemberIds(selectedDepartments)
  const selectedMembers = Array.from(new Set([...(quickChatRoomForm.memberIds || []), ...departmentMemberIds, selfId].map((id) => String(id))))
  if (quickChatRoomEditorMode.value === 'rename') {
    const room = activeQuickChatRoom.value
    if (!room) {
      message.warning('当前会话不存在')
      return
    }
    const previousName = room.name
    room.name = name
    appendRoomSystemMessage(room, `${currentQuickChatSenderName.value} 将会话名从“${previousName}”更新为“${name}”`)
    persistQuickChatState()
    void pushQuickChatRoomToMembers(room)
    message.success('会话名称已更新')
  } else if (quickChatRoomEditorMode.value === 'create') {
    const room: QuickChatRoom = {
      id: `room_${Date.now()}_${Math.random().toString(36).slice(2, 7)}`,
      name,
      departmentIds: selectedDepartments,
      memberIds: selectedMembers,
      unreadCount: 0,
      unreadByUser: Object.fromEntries(selectedMembers.map((id) => [id, 0])),
      pinned: false,
      notificationMode: 'ALL',
      createdAt: dayjs().toISOString(),
      updatedAt: dayjs().toISOString(),
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
    void pushQuickChatRoomToMembers(room)
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
    const invited = selectedMembers.filter((id) => !isCurrentQuickChatIdentity(id)).map((id) => memberNameById(id))
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
    void pushQuickChatRoomToMembers(room)
    message.success('群聊已更新')
  }
  quickChatRoomEditorOpen.value = false
  resetQuickChatRoomForm()
}

function sendQuickChatText() {
  if (!ensureQuickChatSenderReady()) return
  const currentId = currentQuickChatKeyId()
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
  const mentionTargetIds = extractMentionTargets(room, content)
  const outgoingMessage = createQuickChatMessage({
    id: `m_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`,
    senderId: currentId,
    senderName: currentQuickChatSenderName.value,
    kind: 'TEXT',
    content
  })
  room.memberIds.forEach((memberId) => {
    outgoingMessage.readByUser = outgoingMessage.readByUser || {}
    if (!isCurrentQuickChatIdentity(memberId)) outgoingMessage.readByUser[memberId] = false
  })
  room.messages.push(outgoingMessage)
  room.updatedAt = dayjs().toISOString()
  incrementRoomUnread(room, mentionTargetIds.length ? mentionTargetIds : undefined)
  if (mentionTargetIds.length) {
    const mentionNames = mentionTargetIds.map((memberId) => memberNameById(memberId)).join('、')
    appendRoomSystemMessage(room, `${currentQuickChatSenderName.value} 提醒了：${mentionNames}`)
  }
  quickChatDraft.value = ''
  persistQuickChatState()
  void pushQuickChatRoomToMembers(room)
  scrollQuickChatToBottom()
}

function incrementRoomUnread(room: QuickChatRoom, targetMemberIds?: string[]) {
  const currentId = currentQuickChatKeyId()
  room.unreadByUser = room.unreadByUser || {}
  room.memberIds.forEach((memberId) => {
    if (isCurrentQuickChatIdentity(memberId)) {
      room.unreadByUser![memberId] = 0
      return
    }
    if (!shouldDeliverUnread(room, memberId, targetMemberIds)) return
    room.unreadByUser![memberId] = Number(room.unreadByUser![memberId] || 0) + 1
  })
  room.unreadCount = Number(room.unreadByUser[currentId] || 0)
}

const beforeQuickChatUpload: UploadProps['beforeUpload'] = async (file) => {
  if (!ensureQuickChatSenderReady()) return false
  const currentId = currentQuickChatKeyId()
  const room = activeQuickChatRoom.value
  if (!room) {
    message.info('请先选择会话')
    return false
  }
  const currentFile = file as File
  const isImage = String(currentFile.type || '').startsWith('image/')
  const tempPreviewUrl = isImage ? URL.createObjectURL(currentFile) : ''
  const tempMessage = createQuickChatMessage({
    id: `m_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`,
    senderId: currentId,
    senderName: currentQuickChatSenderName.value,
    kind: isImage ? 'IMAGE' : 'FILE',
    content: isImage ? '[图片上传中]' : '[文件上传中]',
    fileName: currentFile.name,
    fileUrl: tempPreviewUrl || undefined,
    uploadStatus: 'UPLOADING'
  })
  room.messages.push(tempMessage)
  room.updatedAt = dayjs().toISOString()
  scrollQuickChatToBottom()
  try {
    const uploaded = await uploadOaFile(currentFile, isImage ? 'oa-quick-chat-image' : 'oa-quick-chat-file')
    const fileUrl = String(uploaded?.fileUrl || '').trim()
    if (!fileUrl) {
      tempMessage.uploadStatus = 'FAILED'
      tempMessage.content = isImage ? '[图片上传失败]' : '[文件上传失败]'
      if (isImage) tempMessage.fileUrl = undefined
      if (tempPreviewUrl) URL.revokeObjectURL(tempPreviewUrl)
      room.updatedAt = dayjs().toISOString()
      persistQuickChatState()
      message.error('发送失败：未返回文件地址')
      return false
    }
    tempMessage.content = isImage ? '[图片]' : '[文件]'
    tempMessage.fileName = uploaded?.originalFileName || uploaded?.fileName || currentFile.name
    tempMessage.fileUrl = fileUrl
    tempMessage.uploadStatus = undefined
    room.memberIds.forEach((memberId) => {
      tempMessage.readByUser = tempMessage.readByUser || {}
      if (!isCurrentQuickChatIdentity(memberId)) tempMessage.readByUser[memberId] = false
    })
    room.updatedAt = dayjs().toISOString()
    incrementRoomUnread(room)
    persistQuickChatState()
    void pushQuickChatRoomToMembers(room)
    scrollQuickChatToBottom()
    if (tempPreviewUrl) URL.revokeObjectURL(tempPreviewUrl)
  } catch (error: any) {
    tempMessage.uploadStatus = 'FAILED'
    tempMessage.content = isImage ? '[图片上传失败]' : '[文件上传失败]'
    if (isImage) tempMessage.fileUrl = undefined
    if (tempPreviewUrl) URL.revokeObjectURL(tempPreviewUrl)
    room.updatedAt = dayjs().toISOString()
    persistQuickChatState()
    message.error(error?.message || '发送失败')
  }
  return false
}

function markActiveRoomRead() {
  const room = activeQuickChatRoom.value
  if (!room) return
  markRoomAsRead(room)
  persistQuickChatState()
  message.success('已标记为已读')
}

function removeActiveQuickChatRoom() {
  const room = activeQuickChatRoom.value
  if (!room) return
  room.archivedByUser = room.archivedByUser || {}
  const userId = currentQuickChatKeyId()
  room.archivedByUser[userId] = true
  room.updatedAt = dayjs().toISOString()
  quickChatLastArchivedRoomId.value = room.id
  quickChatLastArchivedAt.value = Date.now()
  persistQuickChatState()
  ensureActiveQuickChatVisible()
  message.success('会话已移入归档，可在“已归档”中恢复')
}

function undoLastArchivedQuickChatRoom() {
  if (!canUndoQuickChatArchive.value) {
    quickChatLastArchivedRoomId.value = ''
    quickChatLastArchivedAt.value = 0
    message.info('撤销窗口已过期')
    return
  }
  const roomId = quickChatLastArchivedRoomId.value
  const room = quickChatRooms.value.find((item) => item.id === roomId)
  if (!room) {
    quickChatLastArchivedRoomId.value = ''
    quickChatLastArchivedAt.value = 0
    message.warning('会话不存在，无法恢复')
    return
  }
  room.archivedByUser = room.archivedByUser || {}
  const userId = currentQuickChatKeyId()
  room.archivedByUser[userId] = false
  room.updatedAt = dayjs().toISOString()
  quickChatLastArchivedRoomId.value = ''
  quickChatLastArchivedAt.value = 0
  persistQuickChatState()
  activeQuickChatRoomId.value = room.id
  message.success('已恢复会话')
}

function togglePinActiveQuickChatRoom() {
  const room = activeQuickChatRoom.value
  if (!room) return
  room.pinned = !room.pinned
  room.updatedAt = dayjs().toISOString()
  persistQuickChatState()
  message.success(room.pinned ? '已置顶会话' : '已取消置顶')
}

function nudgeActiveQuickChatRoom() {
  const room = activeQuickChatRoom.value
  if (!room) return
  appendRoomSystemMessage(room, `${currentQuickChatSenderName.value} 发起了催办提醒：请相关成员尽快处理。`, true)
  persistQuickChatState()
  message.success('已发送催办提醒')
}

function exitActiveQuickChatRoom() {
  const room = activeQuickChatRoom.value
  if (!room) return
  const selfId = currentQuickChatKeyId()
  const selfName = currentQuickChatSenderName.value
  room.memberIds = room.memberIds.filter((id) => !isCurrentQuickChatIdentity(id))
  if (room.unreadByUser) {
    delete room.unreadByUser[selfId]
  }
  appendRoomSystemMessage(room, `${selfName} 已退出会话`)
  if (!room.memberIds.length) {
    quickChatRooms.value = quickChatRooms.value.filter((item) => item.id !== room.id)
  }
  activeQuickChatRoomId.value = sortedQuickChatRooms.value.find((item) => item.memberIds.some((id) => isCurrentQuickChatIdentity(id)))?.id || sortedQuickChatRooms.value[0]?.id || ''
  persistQuickChatState()
  message.success('已退出会话')
}

function onQuickChatInputKeydown(event: KeyboardEvent) {
  if (event.key !== 'Enter') return
  if (event.shiftKey || event.ctrlKey || event.altKey || event.metaKey) return
  event.preventDefault()
  sendQuickChatText()
}

function openQuickChatMembers() {
  if (!activeQuickChatRoom.value) {
    message.info('请先选择会话')
    return
  }
  quickChatMembersOpen.value = true
}

function quickMentionMember(memberName: string) {
  if (!activeQuickChatRoom.value) return
  const name = String(memberName || '').trim()
  if (!name) return
  const appendText = `@${name} `
  if (quickChatDraft.value.includes(appendText)) return
  quickChatDraft.value = `${quickChatDraft.value}${quickChatDraft.value ? ' ' : ''}${appendText}`
  quickChatMembersOpen.value = false
}

async function copyQuickChatMessage(messageRow: QuickChatMessage) {
  const text = messageRow.recalled
    ? '[消息已撤回]'
    : messageRow.kind === 'IMAGE'
      ? `[图片] ${messageRow.fileName || ''}`.trim()
      : messageRow.kind === 'FILE'
        ? `[文件] ${messageRow.fileName || ''}`.trim()
        : (messageRow.content || '')
  try {
    if (navigator?.clipboard?.writeText) {
      await navigator.clipboard.writeText(text)
      message.success('消息已复制')
      return
    }
  } catch {}
  const textarea = document.createElement('textarea')
  textarea.value = text
  document.body.appendChild(textarea)
  textarea.select()
  document.execCommand('copy')
  document.body.removeChild(textarea)
  message.success('消息已复制')
}

function openForwardMessage(messageId: string) {
  if (!activeQuickChatRoom.value) return
  quickChatForwardMessageId.value = messageId
  quickChatForwardTargetRoomId.value = ''
  quickChatForwardRemark.value = ''
  quickChatForwardOpen.value = true
}

function submitForwardMessage() {
  if (!ensureQuickChatSenderReady()) return
  const currentId = currentQuickChatKeyId()
  const sourceRoom = activeQuickChatRoom.value
  if (!sourceRoom) return
  if (!quickChatForwardMessageId.value) return
  if (!quickChatForwardTargetRoomId.value) {
    message.warning('请选择目标会话')
    return
  }
  const sourceMessage = sourceRoom.messages.find((msg) => msg.id === quickChatForwardMessageId.value)
  if (!sourceMessage || sourceMessage.recalled) {
    message.warning('该消息不可转发')
    return
  }
  const targetRoom = quickChatRooms.value.find((room) => room.id === quickChatForwardTargetRoomId.value)
  if (!targetRoom) {
    message.warning('目标会话不存在')
    return
  }
  let forwardedContent = sourceMessage.content || ''
  if (sourceMessage.kind === 'IMAGE') forwardedContent = '[转发图片]'
  if (sourceMessage.kind === 'FILE') forwardedContent = `[转发文件] ${sourceMessage.fileName || ''}`.trim()
  const messageRow = createQuickChatMessage({
    id: `m_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`,
    senderId: currentId,
    senderName: currentQuickChatSenderName.value,
    kind: sourceMessage.kind,
    content: `转发自 ${sourceRoom.name}：${forwardedContent}`,
    fileName: sourceMessage.fileName,
    fileUrl: sourceMessage.fileUrl
  })
  targetRoom.memberIds.forEach((memberId) => {
    messageRow.readByUser = messageRow.readByUser || {}
    if (!isCurrentQuickChatIdentity(memberId)) messageRow.readByUser[memberId] = false
  })
  targetRoom.messages.push(messageRow)
  if (quickChatForwardRemark.value.trim()) {
    targetRoom.messages.push(createQuickChatMessage({
      id: `m_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`,
      senderId: currentId,
      senderName: currentQuickChatSenderName.value,
      kind: 'TEXT',
      content: `附言：${quickChatForwardRemark.value.trim()}`
    }))
  }
  targetRoom.updatedAt = dayjs().toISOString()
  incrementRoomUnread(targetRoom)
  quickChatForwardOpen.value = false
  quickChatForwardMessageId.value = ''
  quickChatForwardTargetRoomId.value = ''
  quickChatForwardRemark.value = ''
  persistQuickChatState()
  message.success('已转发到目标会话')
}

function extractMentionTargets(room: QuickChatRoom, content: string) {
  const text = String(content || '')
  if (!text.includes('@')) return [] as string[]
  const normalized = text.toLowerCase()
  if (normalized.includes('@all')) {
    return room.memberIds.filter((memberId) => !isCurrentQuickChatIdentity(memberId))
  }
  const targets: string[] = []
  room.memberIds.forEach((memberId) => {
    if (isCurrentQuickChatIdentity(memberId)) return
    const name = memberNameById(memberId)
    if (!name) return
    if (normalized.includes(`@${name.toLowerCase()}`)) {
      targets.push(memberId)
    }
  })
  return Array.from(new Set(targets))
}

function openNoticeEditor() {
  if (!activeQuickChatRoom.value) return
  quickChatNoticeDraft.value = String(activeQuickChatRoom.value.notice || '')
  quickChatNoticeOpen.value = true
}

function submitQuickChatNotice() {
  const room = activeQuickChatRoom.value
  if (!room) return
  room.notice = String(quickChatNoticeDraft.value || '').trim().slice(0, 200)
  appendRoomSystemMessage(room, `${currentQuickChatSenderName.value} 更新了群公告`, true)
  persistQuickChatState()
  quickChatNoticeOpen.value = false
  message.success('群公告已更新')
}

function canRecallMessage(messageRow: QuickChatMessage) {
  if (messageRow.recalled) return false
  if (!isCurrentQuickChatIdentity(messageRow.senderId)) return false
  const diffMinute = dayjs().diff(dayjs(messageRow.createdAt || dayjs().toISOString()), 'minute', true)
  return diffMinute <= 2
}

function recallQuickChatMessage(messageId: string) {
  const room = activeQuickChatRoom.value
  if (!room) return
  const messageRow = room.messages.find((msg) => msg.id === messageId)
  if (!messageRow) return
  if (!canRecallMessage(messageRow)) {
    message.warning('仅支持发送后2分钟内撤回')
    return
  }
  messageRow.recalled = true
  messageRow.content = ''
  messageRow.fileUrl = undefined
  messageRow.fileName = undefined
  room.updatedAt = dayjs().toISOString()
  appendRoomSystemMessage(room, `${currentQuickChatSenderName.value} 撤回了一条消息`, true)
  persistQuickChatState()
}

function quickChatMessageReadCount(messageRow: QuickChatMessage) {
  if (!activeQuickChatRoom.value) return 0
  const readByUser = messageRow.readByUser || {}
  return activeQuickChatRoom.value.memberIds.filter((memberId) => readByUser[memberId] === true).length
}

function exportActiveQuickChatHistory() {
  const room = activeQuickChatRoom.value
  if (!room) {
    message.info('请先选择会话')
    return
  }
  const lines: string[] = []
  lines.push(`会话名称：${room.name}`)
  lines.push(`导出时间：${dayjs().format('YYYY-MM-DD HH:mm:ss')}`)
  lines.push(`成员：${room.memberIds.map((id) => memberNameById(id)).join('、')}`)
  if (room.notice) lines.push(`群公告：${room.notice}`)
  lines.push('--- 聊天记录 ---')
  room.messages.forEach((msg) => {
    const prefix = `[${msg.timeText}] ${msg.senderName}`
    if (msg.recalled) {
      lines.push(`${prefix}: [已撤回]`)
      return
    }
    if (msg.kind === 'IMAGE') {
      lines.push(`${prefix}: [图片] ${msg.fileName || ''}`.trim())
      return
    }
    if (msg.kind === 'FILE') {
      lines.push(`${prefix}: [文件] ${msg.fileName || ''}`.trim())
      return
    }
    lines.push(`${prefix}: ${msg.content || ''}`)
  })
  const blob = new Blob([lines.join('\n')], { type: 'text/plain;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const anchor = document.createElement('a')
  anchor.href = url
  anchor.download = `${room.name || '会话'}-${dayjs().format('YYYYMMDD-HHmmss')}.txt`
  document.body.appendChild(anchor)
  anchor.click()
  document.body.removeChild(anchor)
  URL.revokeObjectURL(url)
  message.success('会话记录已导出')
}

function scrollQuickChatToBottom() {
  nextTick(() => {
    const element = quickChatMessagesRef.value
    if (!element) return
    element.scrollTop = element.scrollHeight
  })
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

function runAutoEscalationIfNeeded() {
  if (!quickChatTodoAutoEscalationEnabled.value) return
  const candidates = quickChatTodoItems.value.filter((item) => !item.done && isQuickChatTodoOverdue(item))
  if (!candidates.length) return
  candidates.forEach((item) => {
    const level = evaluateQuickChatTodoEscalationLevel(item) as 0 | 1 | 2
    if (!level) return
    if (Number(item.escalationLevel || 0) >= level) return
    escalateQuickChatTodo(item.id, true, level as 1 | 2)
  })
}

function setupQuickChatTodoEscalationTimer() {
  if (quickChatTodoEscalationTimer) {
    window.clearInterval(quickChatTodoEscalationTimer)
    quickChatTodoEscalationTimer = undefined
  }
  quickChatTodoEscalationTimer = window.setInterval(() => {
    runAutoEscalationIfNeeded()
  }, 5 * 60 * 1000)
}

onMounted(() => {
  updateQuickChatDrawerWidth()
  hydrateCurrentStaffInfo()
    .then(() => {
      syncDepartmentName()
      loadHeaderSettings()
      loadQuickChatState()
      reconcileQuickChatSelfIdentity()
      loadQuickChatTodo()
      loadQuickChatTodoAutomation()
      loadPresenceState()
      rehydrateQuickChatUnread()
      ensureActiveQuickChatVisible()
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
  }, 2 * 60 * 1000)
  quickChatSyncUnsubscribe = subscribeLiveSync((payload) => {
    if (!payload?.topics?.includes('oa') && payload?.url !== '/local/quick-chat') return
    if (payload.url !== '/local/quick-chat') return
    if (!quickChatOpen.value) return
    syncQuickChatFromStorage()
  })
  setupQuickChatTodoEscalationTimer()
  window.setTimeout(() => runAutoEscalationIfNeeded(), 800)
  window.addEventListener('resize', updateQuickChatDrawerWidth)
  window.addEventListener('storage', onQuickChatStorageChange)
  window.addEventListener('focus', onQuickChatCloudVisibilityChange)
  document.addEventListener('visibilitychange', onQuickChatCloudVisibilityChange)
  document.addEventListener('click', closeTabContextMenu)
})

onBeforeUnmount(() => {
  if (quickChatLocalPersistTimer) {
    window.clearTimeout(quickChatLocalPersistTimer)
    quickChatLocalPersistTimer = undefined
  }
  cancelQuickChatIdlePersist()
  closeQuickChatWebSocket()
  if (presenceTimer) {
    window.clearInterval(presenceTimer)
    presenceTimer = undefined
  }
  quickChatSyncUnsubscribe()
  if (quickChatCloudPullTimer) {
    window.clearInterval(quickChatCloudPullTimer)
    quickChatCloudPullTimer = undefined
  }
  if (quickChatCloudSaveTimer) {
    window.clearTimeout(quickChatCloudSaveTimer)
    quickChatCloudSaveTimer = undefined
  }
  if (quickChatFanoutTimer) {
    window.clearTimeout(quickChatFanoutTimer)
    quickChatFanoutTimer = undefined
  }
  if (quickChatFanoutRetryTimer) {
    window.clearInterval(quickChatFanoutRetryTimer)
    quickChatFanoutRetryTimer = undefined
  }
  if (quickChatTodoEscalationTimer) {
    window.clearInterval(quickChatTodoEscalationTimer)
    quickChatTodoEscalationTimer = undefined
  }
  window.removeEventListener('resize', updateQuickChatDrawerWidth)
  window.removeEventListener('storage', onQuickChatStorageChange)
  window.removeEventListener('focus', onQuickChatCloudVisibilityChange)
  document.removeEventListener('visibilitychange', onQuickChatCloudVisibilityChange)
  document.removeEventListener('click', closeTabContextMenu)
})

function onQuickChatStorageChange(event: StorageEvent) {
  if (event.key === quickChatStorageKey()) {
    if (!quickChatOpen.value) return
    syncQuickChatFromStorage()
    return
  }
  if (event.key === quickChatTodoStorageKey()) {
    if (!quickChatOpen.value) return
    loadQuickChatTodo()
    runAutoEscalationIfNeeded()
    return
  }
  if (event.key === quickChatTodoAutomationStorageKey()) {
    if (!quickChatOpen.value) return
    loadQuickChatTodoAutomation()
  }
}
</script>

<style scoped>
.app-layout {
  height: 100vh;
  background:
    radial-gradient(900px 320px at 100% 0%, rgba(19, 108, 181, 0.06), transparent 46%),
    linear-gradient(180deg, #f7fbfd 0%, var(--bg) 32%, #f1f6fa 100%);
  overflow: hidden;
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
  background:
    linear-gradient(180deg, rgba(250, 252, 253, 0.98) 0%, rgba(239, 246, 251, 0.98) 56%, rgba(233, 241, 247, 0.98) 100%);
  border-right: 1px solid #d7e4ee;
  height: 100vh;
  position: sticky;
  top: 0;
  left: 0;
  box-shadow: inset -1px 0 0 rgba(255, 255, 255, 0.5), 10px 0 30px rgba(18, 49, 77, 0.04);
  z-index: 121;
  transition: all 0.22s ease;
}

.app-sider.is-peek-open {
  box-shadow: 0 12px 36px rgba(23, 56, 84, 0.16), inset -1px 0 0 rgba(255, 255, 255, 0.5);
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
  color: #12314d;
  border-bottom: 1px solid #e7eff5;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.88) 0%, rgba(247, 250, 252, 0.8) 100%);
  backdrop-filter: blur(14px);
}

.sider-toggle-btn {
  margin-right: 12px;
}

.logo {
  width: 42px;
  height: 42px;
  border-radius: 15px;
  background: linear-gradient(135deg, #136cb5 0%, #36a1d9 100%);
  color: #ffffff;
  font-weight: 700;
  display: grid;
  place-items: center;
  box-shadow: 0 14px 24px rgba(19, 108, 181, 0.24);
}

.title {
  font-weight: 700;
  font-size: 16px;
}

.subtitle {
  font-size: 12px;
  color: #7b97ae;
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
  color: #4f6f89 !important;
}

.side-menu :deep(.ant-menu-sub) {
  background: rgba(247, 251, 254, 0.92) !important;
}

.side-menu .ant-menu-item,
.side-menu .ant-menu-submenu-title {
  border-radius: 14px;
  margin: 3px 12px;
  height: auto;
  min-height: 48px;
  display: flex;
  align-items: center;
  line-height: 1.35;
  padding-top: 10px;
  padding-bottom: 10px;
  color: #4f6f89 !important;
  font-weight: 600;
}

.side-menu .ant-menu-item:hover,
.side-menu .ant-menu-submenu-title:hover {
  color: #173854 !important;
  background: #edf7fb !important;
}

.side-menu :deep(.ant-menu-submenu-selected > .ant-menu-submenu-title),
.side-menu :deep(.ant-menu-submenu-open > .ant-menu-submenu-title) {
  color: #173854 !important;
}

.side-menu .ant-menu-item-selected,
.side-menu :deep(.ant-menu-submenu-selected .ant-menu-item-selected) {
  background: linear-gradient(90deg, rgba(19, 108, 181, 0.14) 0%, rgba(95, 187, 212, 0.2) 100%) !important;
  box-shadow: inset 0 0 0 1px rgba(19, 108, 181, 0.12), 0 6px 14px rgba(19, 108, 181, 0.08);
  color: #173854 !important;
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
  background: linear-gradient(120deg, rgba(255, 255, 255, 0.92) 0%, rgba(246, 251, 255, 0.88) 100%);
  backdrop-filter: blur(16px);
  border-bottom: 1px solid var(--border);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  min-height: 72px;
  box-shadow: 0 8px 24px rgba(18, 49, 77, 0.05);
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
  color: #0f5b99;
  background: rgba(19, 108, 181, 0.12);
  border: 1px solid rgba(19, 108, 181, 0.12);
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
  background: rgba(255, 255, 255, 0.76);
  border: 1px solid rgba(216, 229, 239, 0.9);
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
  border-color: rgba(195, 217, 231, 0.9);
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
  border-radius: 14px;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.14);
}

.app-content {
  padding: 24px 24px 26px;
  flex: 1;
  min-height: 0;
  overflow: auto;
}

.quick-chat-layout {
  display: grid;
  grid-template-columns: 240px 1fr;
  gap: 12px;
  min-height: 0;
  max-height: calc(100vh - 180px);
}

.quick-chat-top-actions {
  width: 100%;
  display: flex;
}

.quick-chat-drawer :deep(.ant-drawer-body) {
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.quick-chat-room-list {
  border: 1px solid var(--border);
  border-radius: 12px;
  background: #fff;
  overflow: auto;
  max-height: 100%;
}

.quick-chat-room-search {
  padding: 10px 10px 6px;
  border-bottom: 1px solid var(--border);
  position: sticky;
  top: 0;
  z-index: 1;
  background: #fff;
}

.quick-chat-room-search :deep(.ant-checkbox-wrapper) {
  display: block;
  color: var(--muted);
  font-size: 12px;
}

.quick-chat-batch-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 8px;
}

.quick-chat-room-item {
  cursor: pointer;
  transition: background-color 0.16s ease;
}

.quick-chat-room-check {
  margin-right: 8px;
}

.quick-chat-todo-summary {
  padding: 8px 10px;
  margin-bottom: 10px;
  border: 1px solid var(--border);
  border-radius: 10px;
  background: rgba(248, 250, 252, 0.85);
}

.quick-chat-todo-toolbar {
  display: flex;
  margin-bottom: 10px;
}

.quick-chat-todo-list :deep(.ant-list-items) {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.quick-chat-todo-item {
  padding-block: 0 !important;
}

.quick-chat-todo-card {
  width: 100%;
  padding: 12px;
  border: 1px solid var(--border);
  border-radius: 12px;
  background: linear-gradient(180deg, #fff 0%, rgba(248, 250, 252, 0.92) 100%);
}

.quick-chat-todo-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
}

.quick-chat-todo-title-wrap {
  flex: 1;
  min-width: 0;
}

.quick-chat-todo-title {
  display: block;
  color: var(--ink);
  font-weight: 600;
  line-height: 1.5;
  word-break: break-word;
}

.quick-chat-todo-tags {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 6px;
}

.quick-chat-todo-desc {
  margin-top: 8px;
  color: var(--muted);
  font-size: 12px;
  line-height: 1.6;
  word-break: break-word;
}

.quick-chat-todo-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 2px 8px;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px dashed var(--border);
}

.quick-chat-todo-rank {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed var(--border);
}

.quick-chat-todo-done {
  color: var(--muted);
  text-decoration: line-through;
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
  min-height: 0;
}

.quick-chat-notice {
  padding: 8px 12px;
  background: rgba(245, 158, 11, 0.12);
  border-bottom: 1px solid rgba(245, 158, 11, 0.3);
  color: #92400e;
  font-size: 12px;
}

.quick-chat-sync-conflict {
  padding: 10px 12px;
  background: rgba(239, 68, 68, 0.08);
  border-bottom: 1px solid rgba(239, 68, 68, 0.24);
}

.quick-chat-sync-conflict-title {
  color: #991b1b;
  font-size: 13px;
  font-weight: 600;
}

.quick-chat-sync-conflict-desc {
  margin: 4px 0 8px;
  color: #7f1d1d;
  font-size: 12px;
  line-height: 1.45;
}

.quick-chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 8px;
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

.quick-chat-message-focus {
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.36);
  transition: box-shadow 0.24s ease;
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

.quick-chat-read-status {
  margin-top: 4px;
  text-align: right;
  color: var(--muted);
  font-size: 11px;
}

.quick-chat-recall-btn {
  padding-inline: 0 !important;
  height: auto !important;
  color: #3b82f6;
}

.quick-chat-recalled {
  color: var(--muted);
  font-style: italic;
}

.quick-chat-image {
  display: inline-flex;
}

.quick-chat-image :deep(.ant-image-img) {
  max-width: 220px;
  border-radius: 8px;
  border: 1px solid var(--border);
  display: block;
  cursor: zoom-in;
}

.quick-chat-image-wrap {
  position: relative;
  display: inline-flex;
}

.quick-chat-upload-mask {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  background: rgba(15, 23, 42, 0.38);
  color: #fff;
  font-size: 13px;
  font-weight: 600;
}

.quick-chat-upload-mask-failed {
  background: rgba(185, 28, 28, 0.72);
}

.quick-chat-uploading-file {
  color: var(--muted);
}

.quick-chat-upload-failed-file {
  color: #b91c1c;
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

  .quick-chat-layout {
    grid-template-columns: 1fr;
    max-height: calc(100vh - 210px);
  }

  .quick-chat-room-list {
    max-height: 180px;
  }

  .quick-chat-todo-head {
    flex-direction: column;
  }

  .quick-chat-todo-tags {
    justify-content: flex-start;
  }
}
</style>
