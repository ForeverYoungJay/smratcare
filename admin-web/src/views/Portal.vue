<template>
  <PageContainer title="智慧养老首页" sub-title="待办优先、风险预警、运营与协同一屏联动">
    <StatefulBlock :loading="loading" :error="pageError" @retry="init">
      <div class="portal-page">
        <a-card :bordered="false" class="card-elevated hero-card">
          <div class="hero-left">
            <div class="hero-title">您好，{{ userStore.staffInfo?.realName || '管理员' }}</div>
            <div class="hero-subtitle">当前时间 {{ refreshedAt || '--' }}，优先处理“我的待办”可显著降低超时风险。</div>
            <div class="hero-kpis">
              <div class="hero-kpi-item">
                <span class="hero-kpi-label">待办总量</span>
                <strong class="hero-kpi-value">{{ totalTodoCount }}</strong>
              </div>
              <div class="hero-kpi-item">
                <span class="hero-kpi-label">风险提醒</span>
                <strong class="hero-kpi-value">{{ activeRiskCount }}</strong>
              </div>
              <div class="hero-kpi-item">
                <span class="hero-kpi-label">今日日程</span>
                <strong class="hero-kpi-value">{{ todayAgenda.length }}</strong>
              </div>
            </div>
            <div class="hero-search">
              <a-auto-complete
                v-model:value="searchKeyword"
                :options="searchOptions"
                :filter-option="false"
                style="width: 560px; max-width: 100%;"
                placeholder="模糊搜索页面、功能或关键词（如：请假审批、床态、签约、欠费）"
                @search="onSearchChange"
                @select="onSearchSelect"
              >
                <a-input-search enter-button="搜索" @search="submitSearch" />
              </a-auto-complete>
            </div>
          </div>
          <div class="hero-actions">
            <a-space wrap>
              <a-button @click="init">刷新首页</a-button>
              <a-button @click="openModuleCustomize">自定义首页</a-button>
              <a-button @click="openCustomCardEditor()">新增卡面</a-button>
              <a-button type="primary" @click="go('/oa/todo')">进入待办中心</a-button>
            </a-space>
            <div class="hero-actions-tip">支持卡片拖拽排序、角色可见范围、导入导出配置</div>
          </div>
        </a-card>

        <a-row :gutter="[12, 12]" v-if="showRowTodoReminder" class="section-row" :style="{ order: sectionOrder(['todo', 'reminder']) }">
          <a-col :xs="24" :xl="14" :style="{ order: moduleOrder('todo') }">
            <a-card
              v-if="isModuleVisible('todo')"
              :bordered="false"
              class="card-elevated full-height module-card"
              :class="portalModuleCardClass('todo')"
              :style="moduleCardStyle('todo')"
              title="1️⃣ 我的待办（最重要）"
              @dragover="onPortalModuleDragOver('todo', $event)"
              @drop.prevent="onPortalModuleDrop('todo')"
            >
              <template #extra>
                <a-space>
                  <a-tag color="processing">总待办 {{ totalTodoCount }}</a-tag>
                  <div
                    class="module-drag-handle"
                    :class="{ 'is-ready': moduleDragReadyKey === 'todo' }"
                    draggable="true"
                    title="按住 120ms 拖动排序"
                    @mousedown.left.stop="onPortalModuleHandleMouseDown('todo')"
                    @mouseup.stop="onPortalModuleHandleRelease"
                    @mouseleave="onPortalModuleHandleRelease"
                    @dragstart="onPortalModuleCardDragStart('todo', $event)"
                    @dragend="onPortalModuleCardDragEnd"
                  >
                    ⋮⋮
                  </div>
                </a-space>
              </template>
              <a-row :gutter="[10, 10]">
                <a-col :xs="12" :sm="6" v-for="item in myTodoStats" :key="item.title">
                  <div class="metric-cell" @click="go(item.route)">
                    <div class="metric-title">{{ item.title }}</div>
                    <div class="metric-value">{{ item.value }}</div>
                  </div>
                </a-col>
              </a-row>
              <div class="source-wrap">
                <span class="block-label">来源：</span>
                <a-space wrap>
                  <a-tag color="blue">OA审批</a-tag>
                  <a-tag color="cyan">任务中心</a-tag>
                  <a-tag color="purple">医护任务</a-tag>
                  <a-tag color="orange">系统异常</a-tag>
                </a-space>
              </div>
              <a-space wrap style="margin-top: 12px;">
                <a-button type="primary" @click="go('/oa/todo')">查看全部</a-button>
                <a-button @click="go('/oa/approval')">批量审批</a-button>
                <a-button @click="go('/logistics/task-center')">打开任务中心</a-button>
              </a-space>
              <div class="module-resize-handle module-resize-right" @mousedown.stop.prevent="startResizeModuleCard('todo', 'right', $event)" />
              <div class="module-resize-handle module-resize-bottom" @mousedown.stop.prevent="startResizeModuleCard('todo', 'bottom', $event)" />
              <div class="module-resize-handle module-resize-corner" @mousedown.stop.prevent="startResizeModuleCard('todo', 'corner', $event)" />
            </a-card>
          </a-col>

          <a-col :xs="24" :xl="10" :style="{ order: moduleOrder('reminder') }">
            <a-card
              v-if="isModuleVisible('reminder')"
              :bordered="false"
              class="card-elevated full-height module-card"
              :class="portalModuleCardClass('reminder')"
              :style="moduleCardStyle('reminder')"
              title="2️⃣ 提醒中心（系统预警）"
              @dragover="onPortalModuleDragOver('reminder', $event)"
              @drop.prevent="onPortalModuleDrop('reminder')"
            >
              <template #extra>
                <div
                  class="module-drag-handle"
                  :class="{ 'is-ready': moduleDragReadyKey === 'reminder' }"
                  draggable="true"
                  title="按住 120ms 拖动排序"
                  @mousedown.left.stop="onPortalModuleHandleMouseDown('reminder')"
                  @mouseup.stop="onPortalModuleHandleRelease"
                  @mouseleave="onPortalModuleHandleRelease"
                  @dragstart="onPortalModuleCardDragStart('reminder', $event)"
                  @dragend="onPortalModuleCardDragEnd"
                >
                  ⋮⋮
                </div>
              </template>
              <a-list size="small" :data-source="riskReminders" :locale="{ emptyText: '暂无提醒' }">
                <template #renderItem="{ item }">
                  <a-list-item>
                    <a-space>
                      <a-tag :color="severityColor(item.level)">{{ item.level }}</a-tag>
                      <span>{{ item.title }}</span>
                    </a-space>
                    <template #actions>
                      <span class="reminder-count">{{ item.count }}</span>
                      <a-button type="link" size="small" @click="go(item.route)">处理</a-button>
                    </template>
                  </a-list-item>
                </template>
              </a-list>
              <div class="legend-line">
                <a-tag color="red">红色：紧急</a-tag>
                <a-tag color="orange">橙色：预警</a-tag>
                <a-tag color="blue">蓝色：普通通知</a-tag>
              </div>
              <div class="module-resize-handle module-resize-right" @mousedown.stop.prevent="startResizeModuleCard('reminder', 'right', $event)" />
              <div class="module-resize-handle module-resize-bottom" @mousedown.stop.prevent="startResizeModuleCard('reminder', 'bottom', $event)" />
              <div class="module-resize-handle module-resize-corner" @mousedown.stop.prevent="startResizeModuleCard('reminder', 'corner', $event)" />
            </a-card>
          </a-col>
        </a-row>

        <a-card
          v-if="isModuleVisible('quickLaunch')"
          :bordered="false"
          class="card-elevated module-card"
          :class="portalModuleCardClass('quickLaunch')"
          :style="[moduleCardStyle('quickLaunch'), { order: sectionOrder(['quickLaunch']) }]"
          title="3️⃣ 快捷发起（操作入口）"
          @dragover="onPortalModuleDragOver('quickLaunch', $event)"
          @drop.prevent="onPortalModuleDrop('quickLaunch')"
        >
          <template #extra>
            <div
              class="module-drag-handle"
              :class="{ 'is-ready': moduleDragReadyKey === 'quickLaunch' }"
              draggable="true"
              title="按住 120ms 拖动排序"
              @mousedown.left.stop="onPortalModuleHandleMouseDown('quickLaunch')"
              @mouseup.stop="onPortalModuleHandleRelease"
              @mouseleave="onPortalModuleHandleRelease"
              @dragstart="onPortalModuleCardDragStart('quickLaunch', $event)"
              @dragend="onPortalModuleCardDragEnd"
            >
              ⋮⋮
            </div>
          </template>
          <a-row :gutter="[12, 12]">
            <a-col :xs="24" :md="12" :xl="6" v-for="group in quickLaunchGroups" :key="group.title">
              <div class="quick-group">
                <div class="quick-title">{{ group.title }}</div>
                <a-space wrap>
                  <a-button size="small" v-for="action in group.actions" :key="action.label" @click="go(action.route)">
                    {{ action.label }}
                  </a-button>
                </a-space>
              </div>
            </a-col>
          </a-row>
          <div class="module-resize-handle module-resize-right" @mousedown.stop.prevent="startResizeModuleCard('quickLaunch', 'right', $event)" />
          <div class="module-resize-handle module-resize-bottom" @mousedown.stop.prevent="startResizeModuleCard('quickLaunch', 'bottom', $event)" />
          <div class="module-resize-handle module-resize-corner" @mousedown.stop.prevent="startResizeModuleCard('quickLaunch', 'corner', $event)" />
        </a-card>

        <a-card
          v-if="isModuleVisible('customCards')"
          :bordered="false"
          class="card-elevated module-card"
          :class="portalModuleCardClass('customCards')"
          :style="[moduleCardStyle('customCards'), { order: sectionOrder(['customCards']) }]"
          title="🧩 我的自定义卡面"
          @dragover="onPortalModuleDragOver('customCards', $event)"
          @drop.prevent="onPortalModuleDrop('customCards')"
        >
          <template #extra>
            <a-space>
              <a-select
                v-model:value="customCardCategoryFilter"
                size="small"
                style="width: 120px;"
                :options="customCardCategoryFilterOptions"
              />
              <a-button size="small" @click="openCustomCardEditor()">新增卡面</a-button>
              <a-button size="small" @click="customCardManageOpen = true">管理卡面</a-button>
              <div
                class="module-drag-handle"
                :class="{ 'is-ready': moduleDragReadyKey === 'customCards' }"
                draggable="true"
                title="按住 120ms 拖动排序"
                @mousedown.left.stop="onPortalModuleHandleMouseDown('customCards')"
                @mouseup.stop="onPortalModuleHandleRelease"
                @mouseleave="onPortalModuleHandleRelease"
                @dragstart="onPortalModuleCardDragStart('customCards', $event)"
                @dragend="onPortalModuleCardDragEnd"
              >
                ⋮⋮
              </div>
            </a-space>
          </template>
          <a-row :gutter="[12, 12]">
            <a-col :xs="24" :sm="12" :xl="6" v-for="item in filteredCustomCards" :key="item.id">
              <div class="custom-card-item" :style="customCardItemStyle(item)" @click="handleCustomCardClick(item)">
                <div class="custom-card-title">{{ item.icon || '🧩' }} {{ item.title }}</div>
                <div class="custom-card-route">{{ item.route }}</div>
                <div class="custom-card-desc">{{ item.description || '点击快速进入' }}</div>
                <a-tag class="custom-card-mode" size="small">{{ item.openMode === 'new' ? '新标签打开' : '当前页打开' }}</a-tag>
                <a-tag class="custom-card-category" size="small" color="blue">{{ customCardCategoryText(item.category) }}</a-tag>
                <a-tag class="custom-card-category" size="small" color="purple">{{ audienceText(item.audience) }}</a-tag>
                <div class="custom-card-actions" @click.stop>
                  <a-button type="link" size="small" @click="openCustomCardEditor(item.id)">编辑</a-button>
                  <a-button type="link" size="small" @click="duplicateCustomCard(item.id)">复制</a-button>
                  <a-button type="link" danger size="small" @click="removeCustomCard(item.id)">删除</a-button>
                </div>
                <div class="resize-handle resize-right" @mousedown.stop.prevent="startResizeCustomCard(item.id, 'right', $event)" />
                <div class="resize-handle resize-bottom" @mousedown.stop.prevent="startResizeCustomCard(item.id, 'bottom', $event)" />
                <div class="resize-handle resize-corner" @mousedown.stop.prevent="startResizeCustomCard(item.id, 'corner', $event)" />
              </div>
            </a-col>
            <a-col v-if="!filteredCustomCards.length" :span="24">
              <a-empty description="暂无自定义卡面，可点击右上角“新增卡面”" />
            </a-col>
          </a-row>
          <div class="module-resize-handle module-resize-right" @mousedown.stop.prevent="startResizeModuleCard('customCards', 'right', $event)" />
          <div class="module-resize-handle module-resize-bottom" @mousedown.stop.prevent="startResizeModuleCard('customCards', 'bottom', $event)" />
          <div class="module-resize-handle module-resize-corner" @mousedown.stop.prevent="startResizeModuleCard('customCards', 'corner', $event)" />
        </a-card>

        <a-row :gutter="[12, 12]" v-if="showRowOperation" class="section-row" :style="{ order: sectionOrder(['operation', 'finance', 'salesFunnel']) }">
          <a-col :xs="24" :xl="8" :style="{ order: moduleOrder('operation') }">
            <a-card
              v-if="isModuleVisible('operation')"
              :bordered="false"
              class="card-elevated full-height module-card"
              :class="portalModuleCardClass('operation')"
              :style="moduleCardStyle('operation')"
              title="4️⃣ 今日运营概览（核心KPI）"
              @dragover="onPortalModuleDragOver('operation', $event)"
              @drop.prevent="onPortalModuleDrop('operation')"
            >
              <template #extra>
                <div
                  class="module-drag-handle"
                  :class="{ 'is-ready': moduleDragReadyKey === 'operation' }"
                  draggable="true"
                  title="按住 120ms 拖动排序"
                  @mousedown.left.stop="onPortalModuleHandleMouseDown('operation')"
                  @mouseup.stop="onPortalModuleHandleRelease"
                  @mouseleave="onPortalModuleHandleRelease"
                  @dragstart="onPortalModuleCardDragStart('operation', $event)"
                  @dragend="onPortalModuleCardDragEnd"
                >
                  ⋮⋮
                </div>
              </template>
              <a-row :gutter="[10, 10]">
                <a-col :span="12" v-for="item in operationOverview" :key="item.title">
                  <div class="metric-cell" @click="item.route ? go(item.route) : undefined">
                    <div class="metric-title">{{ item.title }}</div>
                    <div class="metric-value">{{ item.value }}</div>
                  </div>
                </a-col>
              </a-row>
              <div class="module-resize-handle module-resize-right" @mousedown.stop.prevent="startResizeModuleCard('operation', 'right', $event)" />
              <div class="module-resize-handle module-resize-bottom" @mousedown.stop.prevent="startResizeModuleCard('operation', 'bottom', $event)" />
              <div class="module-resize-handle module-resize-corner" @mousedown.stop.prevent="startResizeModuleCard('operation', 'corner', $event)" />
            </a-card>
          </a-col>

          <a-col :xs="24" :xl="8" :style="{ order: moduleOrder('finance') }">
            <a-card
              v-if="isModuleVisible('finance')"
              :bordered="false"
              class="card-elevated full-height module-card"
              :class="portalModuleCardClass('finance')"
              :style="moduleCardStyle('finance')"
              title="5️⃣ 财务运营概览"
              @dragover="onPortalModuleDragOver('finance', $event)"
              @drop.prevent="onPortalModuleDrop('finance')"
            >
              <template #extra>
                <div
                  class="module-drag-handle"
                  :class="{ 'is-ready': moduleDragReadyKey === 'finance' }"
                  draggable="true"
                  title="按住 120ms 拖动排序"
                  @mousedown.left.stop="onPortalModuleHandleMouseDown('finance')"
                  @mouseup.stop="onPortalModuleHandleRelease"
                  @mouseleave="onPortalModuleHandleRelease"
                  @dragstart="onPortalModuleCardDragStart('finance', $event)"
                  @dragend="onPortalModuleCardDragEnd"
                >
                  ⋮⋮
                </div>
              </template>
              <a-row :gutter="[10, 10]">
                <a-col :span="12" v-for="item in financeOverviewItems" :key="item.title">
                  <div class="metric-cell" @click="item.route ? go(item.route) : undefined">
                    <div class="metric-title">{{ item.title }}</div>
                    <div class="metric-value">{{ item.value }}</div>
                  </div>
                </a-col>
              </a-row>
              <a-button type="link" style="padding-left: 0; margin-top: 8px;" @click="go('/finance/reports/overall')">点击 → 财务分析</a-button>
              <div class="module-resize-handle module-resize-right" @mousedown.stop.prevent="startResizeModuleCard('finance', 'right', $event)" />
              <div class="module-resize-handle module-resize-bottom" @mousedown.stop.prevent="startResizeModuleCard('finance', 'bottom', $event)" />
              <div class="module-resize-handle module-resize-corner" @mousedown.stop.prevent="startResizeModuleCard('finance', 'corner', $event)" />
            </a-card>
          </a-col>

          <a-col :xs="24" :xl="8" :style="{ order: moduleOrder('salesFunnel') }">
            <a-card
              v-if="isModuleVisible('salesFunnel')"
              :bordered="false"
              class="card-elevated full-height module-card"
              :class="portalModuleCardClass('salesFunnel')"
              :style="moduleCardStyle('salesFunnel')"
              title="6️⃣ 销售运营漏斗"
              @dragover="onPortalModuleDragOver('salesFunnel', $event)"
              @drop.prevent="onPortalModuleDrop('salesFunnel')"
            >
              <template #extra>
                <div
                  class="module-drag-handle"
                  :class="{ 'is-ready': moduleDragReadyKey === 'salesFunnel' }"
                  draggable="true"
                  title="按住 120ms 拖动排序"
                  @mousedown.left.stop="onPortalModuleHandleMouseDown('salesFunnel')"
                  @mouseup.stop="onPortalModuleHandleRelease"
                  @mouseleave="onPortalModuleHandleRelease"
                  @dragstart="onPortalModuleCardDragStart('salesFunnel', $event)"
                  @dragend="onPortalModuleCardDragEnd"
                >
                  ⋮⋮
                </div>
              </template>
              <a-row :gutter="[10, 10]">
                <a-col :span="12" v-for="item in salesFunnelItems" :key="item.title">
                  <div class="metric-cell" @click="go(item.route)">
                    <div class="metric-title">{{ item.title }}</div>
                    <div class="metric-value">{{ item.value }}</div>
                  </div>
                </a-col>
              </a-row>
              <v-chart :option="funnelOption" autoresize class="funnel-chart" />
              <div class="module-resize-handle module-resize-right" @mousedown.stop.prevent="startResizeModuleCard('salesFunnel', 'right', $event)" />
              <div class="module-resize-handle module-resize-bottom" @mousedown.stop.prevent="startResizeModuleCard('salesFunnel', 'bottom', $event)" />
              <div class="module-resize-handle module-resize-corner" @mousedown.stop.prevent="startResizeModuleCard('salesFunnel', 'corner', $event)" />
            </a-card>
          </a-col>
        </a-row>

        <a-row :gutter="[12, 12]" v-if="showRowStatusExpense" class="section-row" :style="{ order: sectionOrder(['bedStatus', 'expense']) }">
          <a-col :xs="24" :xl="10" :style="{ order: moduleOrder('bedStatus') }">
            <a-card
              v-if="isModuleVisible('bedStatus')"
              :bordered="false"
              class="card-elevated full-height module-card"
              :class="portalModuleCardClass('bedStatus')"
              :style="moduleCardStyle('bedStatus')"
              title="7️⃣ 床位与长者状态"
              @dragover="onPortalModuleDragOver('bedStatus', $event)"
              @drop.prevent="onPortalModuleDrop('bedStatus')"
            >
              <template #extra>
                <div
                  class="module-drag-handle"
                  :class="{ 'is-ready': moduleDragReadyKey === 'bedStatus' }"
                  draggable="true"
                  title="按住 120ms 拖动排序"
                  @mousedown.left.stop="onPortalModuleHandleMouseDown('bedStatus')"
                  @mouseup.stop="onPortalModuleHandleRelease"
                  @mouseleave="onPortalModuleHandleRelease"
                  @dragstart="onPortalModuleCardDragStart('bedStatus', $event)"
                  @dragend="onPortalModuleCardDragEnd"
                >
                  ⋮⋮
                </div>
              </template>
              <a-row :gutter="[10, 10]">
                <a-col :span="8" v-for="item in bedAndElderStatusItems" :key="item.title">
                  <div class="metric-cell" @click="go(item.route)">
                    <div class="metric-title">{{ item.title }}</div>
                    <div class="metric-value">{{ item.value }}</div>
                  </div>
                </a-col>
              </a-row>
              <a-button type="link" style="padding-left: 0; margin-top: 8px;" @click="go('/elder/bed-panorama')">点击 → 床态全景</a-button>
              <div class="module-resize-handle module-resize-right" @mousedown.stop.prevent="startResizeModuleCard('bedStatus', 'right', $event)" />
              <div class="module-resize-handle module-resize-bottom" @mousedown.stop.prevent="startResizeModuleCard('bedStatus', 'bottom', $event)" />
              <div class="module-resize-handle module-resize-corner" @mousedown.stop.prevent="startResizeModuleCard('bedStatus', 'corner', $event)" />
            </a-card>
          </a-col>

          <a-col :xs="24" :xl="14" :style="{ order: moduleOrder('expense') }">
            <a-card
              v-if="isModuleVisible('expense')"
              :bordered="false"
              class="card-elevated full-height module-card"
              :class="portalModuleCardClass('expense')"
              :style="moduleCardStyle('expense')"
              title="8️⃣ 费用管理（我的费用 / 部门费用 / 发票夹）"
              @dragover="onPortalModuleDragOver('expense', $event)"
              @drop.prevent="onPortalModuleDrop('expense')"
            >
              <template #extra>
                <div
                  class="module-drag-handle"
                  :class="{ 'is-ready': moduleDragReadyKey === 'expense' }"
                  draggable="true"
                  title="按住 120ms 拖动排序"
                  @mousedown.left.stop="onPortalModuleHandleMouseDown('expense')"
                  @mouseup.stop="onPortalModuleHandleRelease"
                  @mouseleave="onPortalModuleHandleRelease"
                  @dragstart="onPortalModuleCardDragStart('expense', $event)"
                  @dragend="onPortalModuleCardDragEnd"
                >
                  ⋮⋮
                </div>
              </template>
              <a-row :gutter="[10, 10]">
                <a-col :xs="24" :md="8" v-for="group in expenseSections" :key="group.title">
                  <div class="expense-block">
                    <div class="quick-title">{{ group.title }}</div>
                    <div v-for="row in group.rows" :key="row.label" class="expense-row" @click="go(row.route)">
                      <span>{{ row.label }}</span>
                      <strong>{{ row.value }}</strong>
                    </div>
                  </div>
                </a-col>
              </a-row>
              <div class="module-resize-handle module-resize-right" @mousedown.stop.prevent="startResizeModuleCard('expense', 'right', $event)" />
              <div class="module-resize-handle module-resize-bottom" @mousedown.stop.prevent="startResizeModuleCard('expense', 'bottom', $event)" />
              <div class="module-resize-handle module-resize-corner" @mousedown.stop.prevent="startResizeModuleCard('expense', 'corner', $event)" />
            </a-card>
          </a-col>
        </a-row>

        <a-card
          v-if="isModuleVisible('calendar')"
          :bordered="false"
          class="card-elevated module-card"
          :class="portalModuleCardClass('calendar')"
          :style="[moduleCardStyle('calendar'), { order: sectionOrder(['calendar']) }]"
          title="9️⃣ 行政日历 / 协同日历"
          @dragover="onPortalModuleDragOver('calendar', $event)"
          @drop.prevent="onPortalModuleDrop('calendar')"
        >
          <template #extra>
            <a-space>
              <a-button size="small" @click="openCreateSchedule()">创建日程</a-button>
              <a-button size="small" @click="go('/oa/work-execution/calendar')">查看日程</a-button>
              <a-button size="small" @click="agendaDrawerOpen = true">今日/明日速览</a-button>
              <a-button size="small" @click="go('/oa/attendance-leave?type=LEAVE&quick=1')">发起请假</a-button>
              <a-button size="small" @click="go('/oa/approval?type=LEAVE')">请假审批流程</a-button>
              <div
                class="module-drag-handle"
                :class="{ 'is-ready': moduleDragReadyKey === 'calendar' }"
                draggable="true"
                title="按住 120ms 拖动排序"
                @mousedown.left.stop="onPortalModuleHandleMouseDown('calendar')"
                @mouseup.stop="onPortalModuleHandleRelease"
                @mouseleave="onPortalModuleHandleRelease"
                @dragstart="onPortalModuleCardDragStart('calendar', $event)"
                @dragend="onPortalModuleCardDragEnd"
              >
                ⋮⋮
              </div>
            </a-space>
          </template>
          <div class="calendar-toolbar">
            <div class="calendar-title-tip">日历分层：个人、部门工作、日常计划、协同日历（类似 Apple Calendar 的多日历开关）。</div>
            <a-space wrap>
              <a-checkable-tag
                v-for="item in calendarBuckets"
                :key="item.type"
                :checked="visibleCalendarTypes.includes(item.type)"
                @change="toggleCalendarType(item.type)"
              >
                <span class="legend-dot" :style="{ background: item.color }"></span>
                {{ item.label }} {{ item.count }}
              </a-checkable-tag>
            </a-space>
          </div>
          <StatefulBlock :loading="calendarLoading" :error="''" :empty="!calendarRows.length" empty-text="暂无日程安排" @retry="loadCalendar">
            <FullCalendar ref="calendarRef" :options="calendarOptions" />
          </StatefulBlock>
          <div class="calendar-actions">
            <a-space wrap>
              <a-button type="link" @click="go('/oa/work-execution/calendar')">进入协同日历完整视图</a-button>
              <a-button type="link" @click="go('/oa/work-execution/task')">查看工作执行任务</a-button>
              <a-button type="link" @click="go('/oa/approval?type=LEAVE')">请假审批看板</a-button>
            </a-space>
          </div>
          <div class="module-resize-handle module-resize-right" @mousedown.stop.prevent="startResizeModuleCard('calendar', 'right', $event)" />
          <div class="module-resize-handle module-resize-bottom" @mousedown.stop.prevent="startResizeModuleCard('calendar', 'bottom', $event)" />
          <div class="module-resize-handle module-resize-corner" @mousedown.stop.prevent="startResizeModuleCard('calendar', 'corner', $event)" />
        </a-card>

        <a-card
          v-if="isModuleVisible('dataEntry')"
          :bordered="false"
          class="card-elevated module-card"
          :class="portalModuleCardClass('dataEntry')"
          :style="[moduleCardStyle('dataEntry'), { order: sectionOrder(['dataEntry']) }]"
          title="🔟 数据分析入口"
          @dragover="onPortalModuleDragOver('dataEntry', $event)"
          @drop.prevent="onPortalModuleDrop('dataEntry')"
        >
          <template #extra>
            <div
              class="module-drag-handle"
              :class="{ 'is-ready': moduleDragReadyKey === 'dataEntry' }"
              draggable="true"
              title="按住 120ms 拖动排序"
              @mousedown.left.stop="onPortalModuleHandleMouseDown('dataEntry')"
              @mouseup.stop="onPortalModuleHandleRelease"
              @mouseleave="onPortalModuleHandleRelease"
              @dragstart="onPortalModuleCardDragStart('dataEntry', $event)"
              @dragend="onPortalModuleCardDragEnd"
            >
              ⋮⋮
            </div>
          </template>
          <div class="hint-text">首页不放复杂图表，仅提供分析入口。</div>
          <a-space wrap style="margin-top: 8px;">
            <a-button type="primary"  @click="go('/stats/org/monthly-operation')">运营分析</a-button>
            <a-button type="primary"  @click="go('/finance/reports/overall')">财务分析</a-button>
            <a-button type="primary"  @click="go('/medical-care/nursing-quality')">医护质量</a-button>
            <a-button type="primary"  @click="go('/marketing/reports/conversion')">销售分析</a-button>
          </a-space>
          <div class="module-resize-handle module-resize-right" @mousedown.stop.prevent="startResizeModuleCard('dataEntry', 'right', $event)" />
          <div class="module-resize-handle module-resize-bottom" @mousedown.stop.prevent="startResizeModuleCard('dataEntry', 'bottom', $event)" />
          <div class="module-resize-handle module-resize-corner" @mousedown.stop.prevent="startResizeModuleCard('dataEntry', 'corner', $event)" />
        </a-card>
      </div>
    </StatefulBlock>

    <a-modal
      v-model:open="scheduleOpen"
      :title="scheduleModalTitle"
      width="620px"
      :confirm-loading="scheduleSaving"
      @ok="submitSchedule"
    >
      <a-form layout="vertical">
        <a-form-item label="标题" required>
          <a-input v-model:value="scheduleForm.title" placeholder="例如：月度行政例会" />
        </a-form-item>
        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="开始时间" required>
              <a-date-picker v-model:value="scheduleForm.startTime" show-time style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="结束时间">
              <a-date-picker v-model:value="scheduleForm.endTime" show-time style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="负责人">
              <a-input v-model:value="scheduleForm.assigneeName" placeholder="例如：行政部" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="优先级">
              <a-select v-model:value="scheduleForm.priority" :options="priorityOptions" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="日历类型">
              <a-select v-model:value="scheduleForm.calendarType" :options="calendarTypeOptions" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="紧急程度">
              <a-select v-model:value="scheduleForm.urgency" :options="urgencyOptions" @change="onUrgencyChange" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="计划分类">
              <a-select v-model:value="scheduleForm.planCategory" :options="planCategoryOptions" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="显示颜色">
              <a-select v-model:value="scheduleForm.eventColor" :options="colorOptions" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="12" v-if="scheduleForm.calendarType === 'COLLAB'">
          <a-col :span="12">
            <a-form-item label="协同部门">
              <a-select
                v-model:value="scheduleForm.collaboratorDeptId"
                allow-clear
                show-search
                :filter-option="false"
                :options="departmentOptions"
                placeholder="输入部门名称/拼音首字母"
                @search="searchDepartments"
                @focus="() => !departmentOptions.length && searchDepartments('')"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="协同成员">
              <a-select
                v-model:value="scheduleForm.collaboratorIds"
                mode="multiple"
                allow-clear
                show-search
                :filter-option="false"
                :options="filteredStaffOptions"
                placeholder="邀请后自动进入对方协同日历"
                @search="searchStaff"
                @focus="() => !staffOptions.length && searchStaff('')"
              />
            </a-form-item>
          </a-col>
        </a-row>
        <a-card size="small" class="repeat-card" title="周期计划">
          <a-row :gutter="12">
            <a-col :span="8">
              <a-form-item label="开启周期">
                <a-switch v-model:checked="scheduleForm.recurring" checked-children="开启" un-checked-children="关闭" />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="频率">
                <a-select v-model:value="scheduleForm.recurrenceRule" :options="recurrenceRuleOptions" :disabled="!scheduleForm.recurring" />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="间隔">
                <a-input-number v-model:value="scheduleForm.recurrenceInterval" :min="1" :max="90" :disabled="!scheduleForm.recurring" style="width: 100%" />
              </a-form-item>
            </a-col>
          </a-row>
          <a-row :gutter="12">
            <a-col :span="8">
              <a-form-item label="生成次数">
                <a-input-number v-model:value="scheduleForm.recurrenceCount" :min="1" :max="30" :disabled="!scheduleForm.recurring" style="width: 100%" />
              </a-form-item>
            </a-col>
            <a-col :span="16">
              <div class="hint-text">支持个人、部门、协同等多日历周期排程。</div>
            </a-col>
          </a-row>
        </a-card>
        <a-form-item label="备注">
          <a-textarea v-model:value="scheduleForm.description" :rows="3" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-drawer
      v-model:open="agendaDrawerOpen"
      title="今日 / 明日日程速览"
      width="460"
      placement="right"
    >
      <a-card size="small" title="今天">
        <a-list :data-source="todayAgenda" :locale="{ emptyText: '今日暂无日程' }" size="small">
          <template #renderItem="{ item }">
            <a-list-item :class="{ 'agenda-item-done': item.status === 'DONE' }">
              <a-list-item-meta :description="`${item.timeText} · ${item.typeText}${item.assigneeText ? ` · ${item.assigneeText}` : ''}`">
                <template #title>
                  <a-space size="small">
                    <span :class="{ 'agenda-title-done': item.status === 'DONE' }">{{ item.title }}</span>
                    <a-tag v-if="item.status === 'DONE'">已完成</a-tag>
                  </a-space>
                </template>
              </a-list-item-meta>
              <template #actions>
                <a-button type="link" size="small" @click="focusCalendarDate(item.dateText)">定位</a-button>
                <a-button type="link" size="small" @click="openEditSchedule(item.id)">编辑</a-button>
                <a-button type="link" size="small" @click="markScheduleDone(item.id)">完成</a-button>
                <a-button type="link" danger size="small" @click="removeSchedule(item.id)">删除</a-button>
              </template>
            </a-list-item>
          </template>
        </a-list>
      </a-card>
      <a-card size="small" title="明天" style="margin-top: 12px;">
        <a-list :data-source="tomorrowAgenda" :locale="{ emptyText: '明日暂无日程' }" size="small">
          <template #renderItem="{ item }">
            <a-list-item :class="{ 'agenda-item-done': item.status === 'DONE' }">
              <a-list-item-meta :description="`${item.timeText} · ${item.typeText}${item.assigneeText ? ` · ${item.assigneeText}` : ''}`">
                <template #title>
                  <a-space size="small">
                    <span :class="{ 'agenda-title-done': item.status === 'DONE' }">{{ item.title }}</span>
                    <a-tag v-if="item.status === 'DONE'">已完成</a-tag>
                  </a-space>
                </template>
              </a-list-item-meta>
              <template #actions>
                <a-button type="link" size="small" @click="focusCalendarDate(item.dateText)">定位</a-button>
                <a-button type="link" size="small" @click="openEditSchedule(item.id)">编辑</a-button>
                <a-button type="link" size="small" @click="markScheduleDone(item.id)">完成</a-button>
                <a-button type="link" danger size="small" @click="removeSchedule(item.id)">删除</a-button>
              </template>
            </a-list-item>
          </template>
        </a-list>
      </a-card>
      <div class="calendar-actions">
        <a-space wrap>
          <a-button type="primary" @click="go('/oa/work-execution/calendar')">打开完整协同日历</a-button>
          <a-button @click="agendaDrawerOpen = false">关闭</a-button>
        </a-space>
      </div>
    </a-drawer>

    <a-drawer
      v-model:open="dayEventDrawerOpen"
      :title="`${selectedCalendarDateText} 日程详情`"
      width="520"
      placement="right"
    >
      <a-list :data-source="selectedCalendarDateAgenda" :locale="{ emptyText: '当天暂无日程' }" size="small">
        <template #renderItem="{ item }">
          <a-list-item :class="{ 'agenda-item-done': item.status === 'DONE' }">
            <a-list-item-meta :description="`${item.timeText} · ${item.typeText}${item.assigneeText ? ` · ${item.assigneeText}` : ''}`">
              <template #title>
                <a-space size="small">
                  <span :class="{ 'agenda-title-done': item.status === 'DONE' }">{{ item.title }}</span>
                  <a-tag v-if="item.status === 'DONE'">已完成</a-tag>
                </a-space>
              </template>
            </a-list-item-meta>
            <template #actions>
              <a-button type="link" size="small" @click="openEditSchedule(item.id)">编辑</a-button>
              <a-button type="link" size="small" @click="markScheduleDone(item.id)">完成</a-button>
              <a-button type="link" danger size="small" @click="removeSchedule(item.id)">删除</a-button>
            </template>
          </a-list-item>
        </template>
      </a-list>
      <div class="calendar-actions">
        <a-space wrap>
          <a-button type="primary" @click="go('/oa/work-execution/calendar')">打开完整协同日历</a-button>
          <a-button @click="dayEventDrawerOpen = false">关闭</a-button>
        </a-space>
      </div>
    </a-drawer>

    <a-drawer
      v-model:open="moduleCustomizeOpen"
      title="自定义养老首页"
      width="420"
      placement="right"
    >
      <div class="hint-text" style="margin-bottom: 10px;">
        可自由隐藏/添加首页卡片，并支持上移/下移排序；配置按当前账号本地保存。
      </div>
      <a-space wrap style="margin-bottom: 10px;">
        <a-button size="small" @click="applyModulePreset('director')">院长视图预设</a-button>
        <a-button size="small" @click="applyModulePreset('nurse')">护士站预设</a-button>
        <a-button size="small" @click="applyModulePreset('finance')">财务视图预设</a-button>
        <a-button size="small" @click="customCardManageOpen = true">管理自定义卡面</a-button>
      </a-space>
      <div class="manage-card-list">
        <div
          v-for="(item, index) in moduleConfigDraft"
          :key="item.key"
          class="manage-card-item"
          :class="{ 'is-dragging': draggingModuleKey === item.key }"
          draggable="true"
          @dragstart="onModuleDragStart(item.key)"
          @dragover.prevent
          @drop.prevent="onModuleDrop(item.key)"
          @dragend="onModuleDragEnd"
        >
          <div class="manage-card-main">
            <div class="manage-card-title">#{{ index + 1 }} {{ item.title }}</div>
            <div class="manage-card-desc">{{ item.desc }} · {{ audienceText(item.audience) }}</div>
          </div>
          <a-space wrap>
            <a-button size="small" :disabled="index === 0" @click="moveModule(index, -1)">上移</a-button>
            <a-button
              size="small"
              :disabled="index === moduleConfigDraft.length - 1"
              @click="moveModule(index, 1)"
            >
              下移
            </a-button>
            <a-select
              v-model:value="item.audience"
              mode="multiple"
              style="min-width: 170px;"
              :options="moduleAudienceOptions"
              size="small"
              placeholder="可见角色"
            />
            <a-switch v-model:checked="item.visible" checked-children="显示" un-checked-children="隐藏" />
          </a-space>
        </div>
      </div>
      <div class="calendar-actions">
        <a-space wrap>
          <a-button @click="resetModuleCustomize">恢复默认</a-button>
          <a-button @click="resetAllModuleCardSizes">重置模块尺寸</a-button>
          <a-button danger @click="resetAllHomepageCustomize">恢复全部默认</a-button>
          <a-button @click="exportHomepageCustomize">导出配置</a-button>
          <a-button @click="importPayloadOpen = true">导入配置</a-button>
          <a-button type="primary" @click="applyModuleCustomize">保存配置</a-button>
        </a-space>
      </div>
    </a-drawer>

    <a-drawer
      v-model:open="customCardManageOpen"
      title="管理自定义卡面"
      width="480"
      placement="right"
    >
      <div class="hint-text" style="margin-bottom: 10px;">
        可新增养老首页卡面入口，支持编辑、删除、排序（上移/下移）。
      </div>
      <a-space wrap style="margin-bottom: 10px;">
        <a-button type="primary" @click="openCustomCardEditor()">新增卡面</a-button>
        <a-button @click="resetCustomCards">恢复默认卡面</a-button>
        <a-button @click="resetAllCustomCardSizes">重置卡面尺寸</a-button>
        <a-button @click="addRecentRouteAsCards">导入最近访问</a-button>
        <a-switch v-model:checked="customCardGroupMode" checked-children="分组" un-checked-children="平铺" />
      </a-space>
      <a-space wrap style="margin-bottom: 10px;">
        <a-input v-model:value="customCardManageKeyword" allow-clear style="width: 220px;" placeholder="搜索卡面标题/地址/说明" />
        <a-button size="small" @click="setCustomCardVisibleByFilter(true)">当前结果全部显示</a-button>
        <a-button size="small" @click="setCustomCardVisibleByFilter(false)">当前结果全部隐藏</a-button>
        <a-button size="small" danger @click="removeHiddenCustomCards">清理已隐藏卡面</a-button>
      </a-space>
      <a-space wrap style="margin-bottom: 10px;">
        <span class="hint-text">模板：</span>
        <a-tag
          v-for="template in customCardTemplateCatalog"
          :key="template.key"
          class="template-tag"
          @click="addCustomCardFromTemplate(template.key)"
        >
          + {{ template.title }}
        </a-tag>
      </a-space>
      <a-collapse v-if="customCardGroupMode" :bordered="false">
        <a-collapse-panel v-for="group in groupedCustomCards" :key="group.category" :header="`${group.title}（${group.list.length}）`">
          <div class="manage-card-list">
            <div
              v-for="item in group.list"
              :key="item.id"
              class="manage-card-item"
              :class="{ 'is-dragging': draggingCustomCardId === item.id }"
              draggable="true"
              @dragstart="onCustomCardDragStart(item.id)"
              @dragover.prevent="onCustomCardDragOver(item.id)"
              @drop.prevent="onCustomCardDrop(item.id)"
              @dragend="onCustomCardDragEnd"
            >
              <div class="manage-card-main">
                <div class="manage-card-title">{{ item.icon || '🧩' }} {{ item.title }}</div>
                <div class="manage-card-desc">{{ item.route }} · {{ audienceText(item.audience) }}</div>
              </div>
              <a-space wrap>
                <a-switch v-model:checked="item.visible" checked-children="显示" un-checked-children="隐藏" @change="persistCustomCards" />
                <a-button type="link" size="small" @click="openCustomCardEditor(item.id)">编辑</a-button>
                <a-button type="link" size="small" @click="duplicateCustomCard(item.id)">复制</a-button>
                <a-button type="link" danger size="small" @click="removeCustomCard(item.id)">删除</a-button>
              </a-space>
            </div>
          </div>
        </a-collapse-panel>
      </a-collapse>
      <div v-else class="manage-card-list">
        <div
          v-for="item in manageFilteredCustomCards"
          :key="item.id"
          class="manage-card-item"
          :class="{ 'is-dragging': draggingCustomCardId === item.id }"
          draggable="true"
          @dragstart="onCustomCardDragStart(item.id)"
          @dragover.prevent="onCustomCardDragOver(item.id)"
          @drop.prevent="onCustomCardDrop(item.id)"
          @dragend="onCustomCardDragEnd"
        >
          <div class="manage-card-main">
            <div class="manage-card-title">{{ item.icon || '🧩' }} {{ item.title }}</div>
            <div class="manage-card-desc">{{ item.route }} · {{ customCardCategoryText(item.category) }} · {{ audienceText(item.audience) }}</div>
          </div>
          <a-space wrap>
            <a-switch v-model:checked="item.visible" checked-children="显示" un-checked-children="隐藏" @change="persistCustomCards" />
            <a-button type="link" size="small" @click="openCustomCardEditor(item.id)">编辑</a-button>
            <a-button type="link" size="small" @click="duplicateCustomCard(item.id)">复制</a-button>
            <a-button type="link" danger size="small" @click="removeCustomCard(item.id)">删除</a-button>
          </a-space>
        </div>
      </div>
      <a-empty v-if="!manageFilteredCustomCards.length" description="暂无匹配卡面，可清空搜索或新增卡面" />
    </a-drawer>

    <a-modal
      v-model:open="customCardEditorOpen"
      :title="customCardEditingId ? '编辑自定义卡面' : '新增自定义卡面'"
      width="560px"
      @ok="saveCustomCard"
    >
      <a-form layout="vertical">
        <a-form-item label="卡面标题" required>
          <a-input v-model:value="customCardForm.title" placeholder="例如：老人档案总览" />
        </a-form-item>
        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="图标">
              <a-input v-model:value="customCardForm.icon" placeholder="例如：📊" maxlength="2" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="分类">
              <a-select v-model:value="customCardForm.category" :options="customCardCategoryOptions" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="可见范围（角色）">
          <a-select
            v-model:value="customCardForm.audience"
            mode="multiple"
            :options="customCardAudienceOptions"
            placeholder="不选默认全员可见"
          />
        </a-form-item>
        <a-form-item label="跳转地址" required>
          <a-auto-complete
            v-model:value="customCardForm.route"
            :options="customCardRouteOptions"
            :filter-option="false"
            placeholder="例如：/elder/in-hospital-overview"
            @search="onCustomCardRouteSearch"
            @select="onCustomCardRouteSelect"
          />
        </a-form-item>
        <a-form-item label="卡面说明">
          <a-input v-model:value="customCardForm.description" placeholder="例如：快速查看在住长者与床位信息" />
        </a-form-item>
        <div class="hint-text" style="margin-bottom: 8px;">提示：内部地址建议以 `/` 开头；外部地址支持 `https://`。</div>
        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="卡面主题色">
              <a-select v-model:value="customCardForm.themeColor" :options="customCardThemeOptions" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="打开方式">
              <a-radio-group v-model:value="customCardForm.openMode">
                <a-radio-button value="current">当前页</a-radio-button>
                <a-radio-button value="new">新标签</a-radio-button>
              </a-radio-group>
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-modal>

    <a-modal v-model:open="exportPayloadOpen" title="导出首页配置" width="700px" :footer="null">
      <a-space direction="vertical" style="width: 100%;">
        <div class="hint-text">可复制下方 JSON，用于跨账号导入。</div>
        <a-textarea v-model:value="exportPayloadText" :rows="14" readonly />
        <a-space>
          <a-button @click="copyExportPayload">复制到剪贴板</a-button>
          <a-button @click="exportPayloadOpen = false">关闭</a-button>
        </a-space>
      </a-space>
    </a-modal>

    <a-modal v-model:open="importPayloadOpen" title="导入首页配置" width="700px" @ok="applyImportedHomepageCustomize">
      <a-space direction="vertical" style="width: 100%;">
        <div class="hint-text">请粘贴导出的 JSON 配置，导入后会覆盖当前账号首页设置。</div>
        <a-textarea v-model:value="importPayloadText" :rows="14" placeholder="粘贴配置 JSON" />
        <div class="hint-text">导入支持字段：`moduleConfig`、`customCards`，缺失字段将按默认补全。</div>
      </a-space>
    </a-modal>

    <div v-if="resizeIndicator.visible" class="resize-indicator" :style="{ left: `${resizeIndicator.x}px`, top: `${resizeIndicator.y}px` }">
      {{ resizeIndicator.text }}
    </div>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import type { Dayjs } from 'dayjs'
import dayjs from 'dayjs'
import { message, Modal } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import FullCalendar from '@fullcalendar/vue3'
import dayGridPlugin from '@fullcalendar/daygrid'
import interactionPlugin from '@fullcalendar/interaction'
import VChart from 'vue-echarts'
import PageContainer from '../components/PageContainer.vue'
import StatefulBlock from '../components/StatefulBlock.vue'
import { useUserStore } from '../stores/user'
import { createOaTask, getOaTaskCalendar, getPortalSummary, updateOaTask, completeOaTask, deleteOaTask } from '../api/oa'
import type { OaPortalSummary, OaTask, PageResult, BedItem, CrmContractItem, CrmLeadItem } from '../types'
import { useLiveSyncRefresh } from '../composables/useLiveSyncRefresh'
import { getDashboardSummary, type DashboardSummary } from '../api/dashboard'
import { getFinanceWorkbenchOverview } from '../api/finance'
import type { FinanceWorkbenchOverview, HrWorkbenchSummary } from '../types'
import { getMarketingConversionReport, getLeadPage, getContractPage } from '../api/marketing'
import { getResidenceStatusSummary } from '../api/elderResidence'
import { getBedMap } from '../api/bed'
import { getHrProfileCertificateReminderPage, getHrWorkbenchSummary } from '../api/hr'
import { useDepartmentOptions } from '../composables/useDepartmentOptions'
import { useStaffOptions } from '../composables/useStaffOptions'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const pageError = ref('')
const calendarLoading = ref(false)
const refreshedAt = ref('')
const scheduleOpen = ref(false)
const scheduleSaving = ref(false)
const calendarRows = ref<OaTask[]>([])
const calendarRef = ref<any>(null)
const agendaDrawerOpen = ref(false)
const editingScheduleId = ref<string | number | null>(null)
const dayEventDrawerOpen = ref(false)
const selectedCalendarDateText = ref(dayjs().format('YYYY-MM-DD'))
const { departmentOptions, searchDepartments } = useDepartmentOptions({ pageSize: 240, preloadSize: 500 })
const { staffOptions, searchStaff } = useStaffOptions({ pageSize: 300, preloadSize: 500 })
const staffDeptMap = ref<Record<string, string | undefined>>({})
const visibleCalendarTypes = ref<Array<'PERSONAL' | 'WORK' | 'DAILY' | 'COLLAB'>>(['PERSONAL', 'WORK', 'DAILY', 'COLLAB'])
const searchKeyword = ref('')
const searchOptions = ref<Array<{ value: string; label: string; route: string }>>([])
const searchIndex = ref<Array<{ title: string; route: string; keywords: string }>>([])
const recentSearchRoutes = ref<string[]>([])
const moduleCustomizeOpen = ref(false)
const customCardManageOpen = ref(false)
const customCardEditorOpen = ref(false)
const customCardEditingId = ref('')
const customCardCategoryFilter = ref<'ALL' | 'OPS' | 'CARE' | 'FINANCE' | 'OA' | 'CUSTOM'>('ALL')
const customCardGroupMode = ref(true)
const draggingCustomCardId = ref('')
const draggingModuleKey = ref('')
const draggingPortalModuleKey = ref<PortalModuleKey | ''>('')
const portalModuleDropTargetKey = ref<PortalModuleKey | ''>('')
const moduleDragReadyKey = ref<PortalModuleKey | ''>('')
const moduleDragCommitted = ref(false)
const moduleDragInvalidKey = ref<PortalModuleKey | ''>('')
const moduleDragSnapshot = ref<PortalModuleConfigItem[] | null>(null)
const exportPayloadOpen = ref(false)
const importPayloadOpen = ref(false)
const exportPayloadText = ref('')
const importPayloadText = ref('')
const customCardManageKeyword = ref('')
const resizeIndicator = reactive({
  visible: false,
  text: '',
  x: 0,
  y: 0
})
let portalSyncTimer: number | undefined
let portalVisibleHandler: (() => void) | null = null
let moduleDragHoldTimer: number | undefined
const suppressCardClickUntil = ref(0)

type AudienceCode = 'ALL' | 'DIRECTOR' | 'NURSE' | 'FINANCE' | 'ADMIN' | 'HR' | 'OPS'

type PortalModuleKey =
  | 'todo'
  | 'reminder'
  | 'quickLaunch'
  | 'operation'
  | 'finance'
  | 'salesFunnel'
  | 'bedStatus'
  | 'expense'
  | 'customCards'
  | 'calendar'
  | 'dataEntry'

interface PortalModuleConfigItem {
  key: PortalModuleKey
  title: string
  desc: string
  visible: boolean
  order: number
  audience: AudienceCode[]
  width?: number
  height?: number
}

const portalModuleCatalog: Array<Omit<PortalModuleConfigItem, 'visible' | 'order'>> = [
  { key: 'todo', title: '我的待办', desc: '待审批、待处理、超时任务', audience: ['ALL'] },
  { key: 'reminder', title: '提醒中心', desc: '风险预警与系统提醒', audience: ['ALL'] },
  { key: 'quickLaunch', title: '快捷发起', desc: '员工/医护/运营/后勤入口', audience: ['ALL'] },
  { key: 'operation', title: '今日运营概览', desc: '在住、空床、入住退住等KPI', audience: ['ALL'] },
  { key: 'finance', title: '财务运营概览', desc: '收入、欠费人数、欠费金额', audience: ['FINANCE', 'DIRECTOR', 'ADMIN'] },
  { key: 'salesFunnel', title: '销售运营漏斗', desc: '咨询-评估-签约转化', audience: ['OPS', 'DIRECTOR', 'ADMIN'] },
  { key: 'bedStatus', title: '床位与长者状态', desc: '在住/外出/空床/清洁中', audience: ['ALL'] },
  { key: 'expense', title: '费用管理', desc: '我的费用、部门费用、发票夹', audience: ['FINANCE', 'DIRECTOR', 'ADMIN', 'HR'] },
  { key: 'customCards', title: '我的自定义卡面', desc: '可自由新增首页入口卡片', audience: ['ALL'] },
  { key: 'calendar', title: '行政日历/协同日历', desc: '日程、请假、协同安排', audience: ['ALL'] },
  { key: 'dataEntry', title: '数据分析入口', desc: '运营/财务/医护/销售分析入口', audience: ['ALL'] }
]

const moduleConfig = ref<PortalModuleConfigItem[]>(
  portalModuleCatalog.map((item, index) => ({ ...item, visible: true, order: index, width: undefined, height: undefined }))
)
const moduleConfigDraft = ref<PortalModuleConfigItem[]>(
  portalModuleCatalog.map((item, index) => ({ ...item, visible: true, order: index, width: undefined, height: undefined }))
)

interface PortalCustomCardItem {
  id: string
  title: string
  route: string
  description: string
  themeColor: string
  openMode: 'current' | 'new'
  icon: string
  category: 'OPS' | 'CARE' | 'FINANCE' | 'OA' | 'CUSTOM'
  visible: boolean
  audience: AudienceCode[]
  width?: number
  height?: number
}

interface PortalCustomCardTemplate {
  key: string
  title: string
  route: string
  description: string
  themeColor: string
  openMode: 'current' | 'new'
  icon: string
  category: 'OPS' | 'CARE' | 'FINANCE' | 'OA' | 'CUSTOM'
  audience: AudienceCode[]
}

const defaultCustomCards: PortalCustomCardItem[] = [
  { id: 'elder-overview', title: '长者总览', route: '/elder/in-hospital-overview', description: '快速查看在住长者信息', themeColor: '#1677ff', openMode: 'current', icon: '👴', category: 'OPS', visible: true, audience: ['ALL'], height: 168 },
  { id: 'bed-panorama', title: '床态全景', route: '/elder/bed-panorama', description: '查看空床、清洁中与占用床位', themeColor: '#13c2c2', openMode: 'current', icon: '🛏️', category: 'OPS', visible: true, audience: ['ALL'], height: 168 },
  { id: 'finance-risk', title: '欠费看板', route: '/finance/bills/in-resident?filter=overdue', description: '快速追踪欠费风险', themeColor: '#fa8c16', openMode: 'current', icon: '💰', category: 'FINANCE', visible: true, audience: ['FINANCE', 'DIRECTOR', 'ADMIN'], height: 168 },
  { id: 'oa-calendar', title: '协同日历', route: '/oa/work-execution/calendar', description: '查看个人/部门/协同计划', themeColor: '#722ed1', openMode: 'current', icon: '📅', category: 'OA', visible: true, audience: ['ALL'], height: 168 }
]

const customCards = ref<PortalCustomCardItem[]>([])
const customCardRouteOptions = ref<Array<{ value: string; label: string }>>([])
const customCardForm = reactive({
  title: '',
  route: '',
  description: '',
  themeColor: '#1677ff',
  openMode: 'current' as 'current' | 'new',
  icon: '🧩',
  category: 'CUSTOM' as 'OPS' | 'CARE' | 'FINANCE' | 'OA' | 'CUSTOM',
  audience: ['ALL'] as AudienceCode[]
})

const customCardThemeOptions = [
  { label: '蓝色', value: '#1677ff' },
  { label: '青色', value: '#13c2c2' },
  { label: '绿色', value: '#52c41a' },
  { label: '橙色', value: '#fa8c16' },
  { label: '紫色', value: '#722ed1' },
  { label: '红色', value: '#ff4d4f' }
]

const customCardTemplateCatalog: PortalCustomCardTemplate[] = [
  { key: 'elder-list', title: '长者档案', route: '/elder/list', description: '快速进入长者档案列表', themeColor: '#1677ff', openMode: 'current', icon: '🧾', category: 'OPS', audience: ['ALL'] },
  { key: 'nursing-board', title: '护理任务看板', route: '/medical-care/care-task-board', description: '查看待执行护理任务', themeColor: '#52c41a', openMode: 'current', icon: '🩺', category: 'CARE', audience: ['NURSE', 'DIRECTOR', 'ADMIN'] },
  { key: 'approval', title: '审批中心', route: '/oa/approval', description: '处理请假/报销/采购审批', themeColor: '#722ed1', openMode: 'current', icon: '✅', category: 'OA', audience: ['ALL'] },
  { key: 'finance-overall', title: '财务分析', route: '/finance/reports/overall', description: '收入、欠费、成本总览', themeColor: '#fa8c16', openMode: 'current', icon: '📈', category: 'FINANCE', audience: ['FINANCE', 'DIRECTOR', 'ADMIN'] },
  { key: 'marketing-funnel', title: '销售转化分析', route: '/marketing/reports/conversion', description: '线索转化漏斗分析', themeColor: '#13c2c2', openMode: 'current', icon: '🎯', category: 'OPS', audience: ['OPS', 'DIRECTOR', 'ADMIN'] },
  { key: 'calendar-full', title: '协同日历全景', route: '/oa/work-execution/calendar', description: '统一查看个人/部门/协同日程', themeColor: '#2f54eb', openMode: 'current', icon: '🗓️', category: 'OA', audience: ['ALL'] }
]

const customCardCategoryOptions = [
  { label: '运营', value: 'OPS' },
  { label: '医护', value: 'CARE' },
  { label: '财务', value: 'FINANCE' },
  { label: '行政', value: 'OA' },
  { label: '自定义', value: 'CUSTOM' }
]

const customCardCategoryFilterOptions = [
  { label: '全部分类', value: 'ALL' },
  ...customCardCategoryOptions
]

const customCardAudienceOptions = [
  { label: '全员', value: 'ALL' },
  { label: '院长', value: 'DIRECTOR' },
  { label: '护士', value: 'NURSE' },
  { label: '财务', value: 'FINANCE' },
  { label: '管理员', value: 'ADMIN' },
  { label: '人事', value: 'HR' },
  { label: '运营', value: 'OPS' }
]

const moduleAudienceOptions = [...customCardAudienceOptions]
const resizingCustomCard = reactive({
  id: '',
  direction: '' as '' | 'right' | 'bottom' | 'corner',
  startX: 0,
  startY: 0,
  startWidth: 0,
  startHeight: 0,
  moved: false
})
const resizingModuleCard = reactive({
  key: '' as '' | PortalModuleKey,
  direction: '' as '' | 'right' | 'bottom' | 'corner',
  startX: 0,
  startY: 0,
  startWidth: 0,
  startHeight: 0,
  moved: false
})

const summary = reactive<OaPortalSummary>({
  notices: [],
  todos: [],
  workflowTodos: [],
  marketingChannels: [],
  collaborationGantt: [],
  latestSuggestions: []
})

const dashboard = ref<DashboardSummary>({
  careTasksToday: 0,
  abnormalTasksToday: 0,
  inventoryAlerts: 0,
  unpaidBills: 0,
  totalAdmissions: 0,
  totalDischarges: 0,
  checkInNetIncrease: 0,
  dischargeToAdmissionRate: 0,
  totalBillConsumption: 0,
  totalStoreConsumption: 0,
  totalConsumption: 0,
  averageMonthlyConsumption: 0,
  billConsumptionRatio: 0,
  storeConsumptionRatio: 0,
  inHospitalCount: 0,
  dischargedCount: 0,
  totalBeds: 0,
  occupiedBeds: 0,
  availableBeds: 0,
  bedOccupancyRate: 0,
  bedAvailableRate: 0,
  totalRevenue: 0,
  averageMonthlyRevenue: 0,
  revenueGrowthRate: 0
})

const financeOverview = ref<FinanceWorkbenchOverview | null>(null)
const hrSummary = ref<HrWorkbenchSummary | null>(null)
const certificateReminderCount = ref(0)

const bedAndElderStatus = reactive({
  inHospital: 0,
  outing: 0,
  medicalObservation: 0,
  emptyBeds: 0,
  cleaningBeds: 0
})

const salesFunnel = reactive({
  todayConsultCount: 0,
  evaluationCount: 0,
  pendingSignCount: 0,
  monthDealCount: 0
})

const scheduleForm = reactive({
  title: '',
  startTime: undefined as Dayjs | undefined,
  endTime: undefined as Dayjs | undefined,
  assigneeName: '',
  priority: 'NORMAL',
  description: '',
  calendarType: 'WORK' as 'PERSONAL' | 'WORK' | 'DAILY' | 'COLLAB',
  urgency: 'NORMAL' as 'NORMAL' | 'EMERGENCY',
  planCategory: '基础办公',
  eventColor: '#1677ff',
  collaboratorDeptId: undefined as string | undefined,
  collaboratorIds: [] as string[],
  recurring: false,
  recurrenceRule: 'WEEKLY' as 'DAILY' | 'WEEKLY' | 'MONTHLY',
  recurrenceInterval: 1,
  recurrenceCount: 4
})

const priorityOptions = [
  { label: '低', value: 'LOW' },
  { label: '中', value: 'NORMAL' },
  { label: '高', value: 'HIGH' }
]
const calendarTypeOptions = [
  { label: '个人', value: 'PERSONAL' },
  { label: '部门工作', value: 'WORK' },
  { label: '日常计划', value: 'DAILY' },
  { label: '协同日历', value: 'COLLAB' }
]
const urgencyOptions = [
  { label: '常规', value: 'NORMAL' },
  { label: '紧急（红色）', value: 'EMERGENCY' }
]
const recurrenceRuleOptions = [
  { label: '每天', value: 'DAILY' },
  { label: '每周', value: 'WEEKLY' },
  { label: '每月', value: 'MONTHLY' }
]
const planCategoryOptions = [
  { label: '基础办公', value: '基础办公' },
  { label: '行政日常', value: '行政日常' },
  { label: '协同会议', value: '协同会议' },
  { label: '后勤排班', value: '后勤排班' },
  { label: '专项检查', value: '专项检查' }
]
const colorOptions = [
  { label: '蓝色（工作）', value: '#1677ff' },
  { label: '绿色（个人）', value: '#52c41a' },
  { label: '橙色（日常）', value: '#fa8c16' },
  { label: '紫色（协同）', value: '#722ed1' },
  { label: '红色（紧急）', value: '#ff4d4f' }
]

const SEARCH_RECENT_KEY = 'portal_search_recent_routes'
const SEARCH_RECENT_MAX = 8
const MODULE_CONFIG_KEY_PREFIX = 'portal_module_config_v1_'
const CUSTOM_CARD_KEY_PREFIX = 'portal_custom_card_v1_'
const PINYIN_INITIAL_LETTERS = 'ABCDEFGHJKLMNOPQRSTWXYZ'
const PINYIN_INITIAL_BOUNDARY_CHARS = '阿芭擦搭蛾发噶哈讥咔垃妈拿哦啪期然撒塌挖昔压匝'

const modulePresetMap: Record<'director' | 'nurse' | 'finance', PortalModuleKey[]> = {
  director: ['todo', 'reminder', 'operation', 'finance', 'salesFunnel', 'bedStatus', 'calendar', 'customCards', 'dataEntry', 'quickLaunch', 'expense'],
  nurse: ['todo', 'reminder', 'calendar', 'bedStatus', 'quickLaunch', 'operation', 'customCards', 'dataEntry', 'expense', 'finance', 'salesFunnel'],
  finance: ['todo', 'reminder', 'finance', 'expense', 'operation', 'customCards', 'dataEntry', 'calendar', 'quickLaunch', 'salesFunnel', 'bedStatus']
}

const searchAliases: Record<string, string[]> = {
  '/oa/approval': ['审批', '待审批', '流程审批', '批量审批', '请假审批'],
  '/oa/todo': ['待办', '任务', '待处理', '超时'],
  '/oa/work-execution/calendar': ['日历', '协同日历', '行政日历', '排班'],
  '/oa/attendance-leave': ['请假', '考勤', '值班', '调班', '加班'],
  '/elder/bed-panorama': ['床态', '空床', '清洁中', '床位全景'],
  '/marketing/reports/conversion': ['漏斗', '销售分析', '签约', '转化'],
  '/finance/reports/overall': ['财务分析', '欠费', '收入', '对账'],
  '/medical-care/orders': ['医嘱', '医嘱执行', '医护任务'],
  '/logistics/task-center': ['任务中心', '后勤任务', '工单']
}

const searchPinnedRoutes = [
  '/oa/todo',
  '/oa/approval',
  '/oa/work-execution/calendar',
  '/oa/attendance-leave',
  '/elder/bed-panorama',
  '/marketing/reports/conversion',
  '/finance/reports/overall',
  '/medical-care/orders',
  '/logistics/task-center',
  '/hr/development/certificate-reminders',
  '/hr/profile/contract-reminders'
]

function moduleStorageKey() {
  return `${MODULE_CONFIG_KEY_PREFIX}${String(userStore.staffInfo?.id || 'default')}`
}

function customCardStorageKey() {
  return `${CUSTOM_CARD_KEY_PREFIX}${String(userStore.staffInfo?.id || 'default')}`
}

function isModuleVisible(key: PortalModuleKey) {
  const matched = moduleConfig.value.find((item) => item.key === key)
  if (!matched) return true
  if (!matched.visible) return false
  return cardAudienceMatched(matched.audience, currentUserAudience.value)
}

const orderedModules = computed(() => [...moduleConfig.value].sort((a, b) => a.order - b.order))

function moduleOrder(key: PortalModuleKey) {
  const index = orderedModules.value.findIndex((item) => item.key === key)
  return index >= 0 ? index : 999
}

function sectionOrder(keys: PortalModuleKey[]) {
  const orders = keys
    .map((key) => moduleOrder(key))
    .filter((value) => Number.isFinite(value))
  if (!orders.length) return 999
  return Math.min(...orders)
}

const showRowTodoReminder = computed(() => isModuleVisible('todo') || isModuleVisible('reminder'))
const showRowOperation = computed(() => isModuleVisible('operation') || isModuleVisible('finance') || isModuleVisible('salesFunnel'))
const showRowStatusExpense = computed(() => isModuleVisible('bedStatus') || isModuleVisible('expense'))
const currentUserAudience = computed(() => resolveCurrentUserAudience())
const filteredCustomCards = computed(() => {
  const visibleCards = customCards.value
    .filter((item) => item.visible !== false)
    .filter((item) => cardAudienceMatched(item.audience, currentUserAudience.value))
  if (customCardCategoryFilter.value === 'ALL') return visibleCards
  return visibleCards.filter((item) => item.category === customCardCategoryFilter.value)
})

const manageFilteredCustomCards = computed(() => {
  const keyword = String(customCardManageKeyword.value || '').trim().toLowerCase()
  if (!keyword) return customCards.value
  return customCards.value.filter((item) => {
    const text = `${item.title} ${item.route} ${item.description} ${customCardCategoryText(item.category)}`.toLowerCase()
    return text.includes(keyword)
  })
})

const groupedCustomCards = computed(() => {
  const categories: Array<'OPS' | 'CARE' | 'FINANCE' | 'OA' | 'CUSTOM'> = ['OPS', 'CARE', 'FINANCE', 'OA', 'CUSTOM']
  return categories
    .map((category) => ({
      category,
      title: customCardCategoryText(category),
      list: manageFilteredCustomCards.value.filter((item) => item.category === category)
    }))
    .filter((group) => group.list.length > 0)
})

const myTodoStats = computed(() => [
  { title: '待审批流程', value: summary.pendingApprovalCount || 0, route: '/oa/approval' },
  { title: '待处理任务', value: summary.openTodoCount || 0, route: '/oa/todo' },
  { title: '待确认事项', value: summary.ongoingTaskCount || 0, route: '/oa/work-execution/task' },
  { title: '超时未处理', value: (summary.overdueTodoCount || 0) + (summary.approvalTimeoutCount || 0), route: '/oa/approval?status=pending' }
])

const totalTodoCount = computed(() => myTodoStats.value.reduce((sum, item) => sum + Number(item.value || 0), 0))

const riskReminders = computed(() => {
  const elderAbnormal = Number(summary.elderAbnormalCount || 0) + Number(summary.healthAbnormalCount || 0)
  const arrearsCount = Number(financeOverview.value?.risk?.overdueElderCount || 0)
  const inventoryCount = Number(summary.inventoryLowStockCount || 0)
  const orderPendingCount = Number(dashboard.value.abnormalTasksToday || 0)
  const approvalTimeoutCount = Number(summary.approvalTimeoutCount || 0)
  const contractExpiringCount = Number(hrSummary.value?.contractExpiringCount || summary.contractPendingCount || 0)

  return [
    { title: '长者异常', count: elderAbnormal, route: '/health/inspection', level: elderAbnormal > 0 ? '紧急' : '普通通知' },
    { title: '欠费提醒', count: arrearsCount, route: '/finance/bills/in-resident?filter=overdue', level: arrearsCount > 0 ? '紧急' : '普通通知' },
    { title: '库存不足', count: inventoryCount, route: '/logistics/storage/alerts', level: inventoryCount > 0 ? '预警' : '普通通知' },
    { title: '医嘱未执行', count: orderPendingCount, route: '/medical-care/orders', level: orderPendingCount > 0 ? '预警' : '普通通知' },
    { title: '审批超时', count: approvalTimeoutCount, route: '/oa/approval', level: approvalTimeoutCount > 0 ? '紧急' : '普通通知' },
    { title: '证书到期', count: certificateReminderCount.value, route: '/hr/development/certificate-reminders', level: certificateReminderCount.value > 0 ? '预警' : '普通通知' },
    { title: '合同到期', count: contractExpiringCount, route: '/hr/profile/contract-reminders', level: contractExpiringCount > 0 ? '预警' : '普通通知' }
  ]
})
const activeRiskCount = computed(() => riskReminders.value.reduce((sum, item) => sum + Number(item.count || 0), 0))

const quickLaunchGroups = [
  {
    title: '员工',
    actions: [
      { label: '请假', route: '/oa/approval?type=LEAVE&quick=1' },
      { label: '加班', route: '/oa/approval?type=OVERTIME&quick=1' },
      { label: '报销', route: '/oa/approval?type=REIMBURSE&quick=1' }
    ]
  },
  {
    title: '医护',
    actions: [
      { label: '巡检记录', route: '/health/inspection' },
      { label: '护理记录', route: '/medical-care/care-task-board' }
    ]
  },
  {
    title: '运营',
    actions: [
      { label: '新增长者', route: '/elder/create' },
      { label: '新建线索', route: '/marketing/leads/all?quick=create' }
    ]
  },
  {
    title: '后勤',
    actions: [
      { label: '物资申领', route: '/oa/approval?type=MATERIAL_APPLY&quick=1' },
      { label: '发起采购', route: '/oa/approval?type=PURCHASE&quick=1' }
    ]
  }
]

const operationOverview = computed(() => [
  { title: '在住人数', value: dashboard.value.inHospitalCount || 0, route: '/elder/in-hospital-overview' },
  { title: '空床数量', value: dashboard.value.availableBeds || 0, route: '/elder/bed-panorama' },
  { title: '今日入住', value: dashboard.value.totalAdmissions || 0, route: '/elder/admission-processing' },
  { title: '今日退住', value: dashboard.value.totalDischarges || 0, route: '/elder/status-change/discharge-apply' },
  { title: '床位利用率', value: `${Number(dashboard.value.bedOccupancyRate || 0).toFixed(1)}%`, route: '/stats/org/bed-usage' }
])

const financeOverviewItems = computed(() => [
  { title: '今日收入', value: money(financeOverview.value?.cashier?.todayCollectedTotal || 0), route: '/finance/payments/register?date=today' },
  { title: '本月收入', value: money(financeOverview.value?.revenueStructure?.monthRevenueTotal || dashboard.value.totalRevenue || 0), route: '/finance/reports/revenue-structure' },
  { title: '欠费人数', value: Number(financeOverview.value?.risk?.overdueElderCount || 0), route: '/finance/bills/in-resident?filter=overdue' },
  { title: '欠费金额', value: money(financeOverview.value?.risk?.overdueAmount || 0), route: '/finance/reports/overall?focus=arrears' }
])

const salesFunnelItems = computed(() => [
  { title: '今日新增咨询', value: salesFunnel.todayConsultCount || 0, route: '/marketing/leads/all?tab=consultation' },
  { title: '待评估人数', value: salesFunnel.evaluationCount || 0, route: '/marketing/funnel/evaluation' },
  { title: '待签约人数', value: salesFunnel.pendingSignCount || 0, route: '/marketing/contracts/pending' },
  { title: '本月签约数', value: salesFunnel.monthDealCount || 0, route: '/marketing/contracts/signed' }
])

const funnelOption = computed(() => ({
  tooltip: { trigger: 'item' },
  series: [
    {
      type: 'funnel',
      left: '8%',
      width: '84%',
      label: { formatter: '{b} {c}' },
      data: [
        { name: '新增咨询', value: salesFunnel.todayConsultCount || 0 },
        { name: '待评估', value: salesFunnel.evaluationCount || 0 },
        { name: '待签约', value: salesFunnel.pendingSignCount || 0 },
        { name: '本月签约', value: salesFunnel.monthDealCount || 0 }
      ]
    }
  ]
}))

const bedAndElderStatusItems = computed(() => [
  { title: '在住', value: bedAndElderStatus.inHospital, route: '/elder/in-hospital-overview' },
  { title: '外出', value: bedAndElderStatus.outing, route: '/elder/status-change/outing' },
  { title: '医疗观察', value: bedAndElderStatus.medicalObservation, route: '/elder/status-change/medical-outing' },
  { title: '空床', value: bedAndElderStatus.emptyBeds, route: '/elder/bed-panorama?quick=empty' },
  { title: '清洁中', value: bedAndElderStatus.cleaningBeds, route: '/life/room-cleaning?status=PENDING' }
])

const expenseSections = computed(() => {
  const monthExpenseCount = Number(summary.myExpenseCount || 0)
  const deptExpenseCount = Number(summary.deptExpenseCount || 0)
  const invoiceCount = Number(summary.invoiceFolderCount || 0)

  return [
    {
      title: '我的费用',
      rows: [
        { label: '本月报销金额', value: money(monthExpenseCount), route: '/hr/expense/approval-flow' },
        { label: '待报销金额', value: money(monthExpenseCount), route: '/hr/expense/training-reimburse' },
        { label: '待审批报销', value: `${summary.pendingApprovalCount || 0} 条`, route: '/hr/expense/approval-flow' }
      ]
    },
    {
      title: '部门费用',
      rows: [
        { label: '本月部门费用', value: money(deptExpenseCount), route: '/hr/workbench' },
        { label: '部门预算使用率', value: deptExpenseCount > 0 ? '80%' : '0%', route: '/hr/workbench' },
        { label: '超预算提醒', value: deptExpenseCount > 0 ? '1 条' : '0 条', route: '/hr/workbench' }
      ]
    },
    {
      title: '发票夹',
      rows: [
        { label: '已录入发票数量', value: `${invoiceCount} 张`, route: '/finance/fees/payment-and-invoice' },
        { label: '待报销发票', value: `${summary.openTodoCount || 0} 张`, route: '/finance/fees/payment-and-invoice' },
        { label: '本月发票金额', value: money(financeOverview.value?.cashier?.todayInvoiceAmount || 0), route: '/finance/reconcile/invoice' }
      ]
    }
  ]
})

const calendarOptions = computed(() => ({
  plugins: [dayGridPlugin, interactionPlugin],
  initialView: 'dayGridMonth',
  locale: 'zh-cn',
  height: 360,
  headerToolbar: {
    left: 'prev,next today',
    center: 'title',
    right: 'dayGridMonth,dayGridWeek,dayGridDay'
  },
  buttonText: {
    today: '今天',
    month: '月视图',
    dayGridWeek: '周视图',
    dayGridDay: '日视图'
  },
  dateClick: (arg: any) => {
    openCreateSchedule(dayjs(arg.dateStr))
  },
  eventClick: (arg: any) => {
    const start = arg?.event?.start
    if (start) {
      selectedCalendarDateText.value = dayjs(start).format('YYYY-MM-DD')
      dayEventDrawerOpen.value = true
      return
    }
    const typeText = arg?.event?.extendedProps?.calendarType || 'WORK'
    message.info(`已定位到「${calendarTypeText(typeText)}」日程`)
  },
  events: calendarRows.value
    .filter((task) => visibleCalendarTypes.value.includes((task.calendarType || 'WORK') as any))
    .map((task) => ({
      id: String(task.id),
      title: `${task.title}${task.assigneeName ? `（${task.assigneeName}）` : ''}`,
      start: task.startTime || task.endTime,
      end: task.endTime || task.startTime,
      color: resolveTaskColor(task),
      extendedProps: {
        calendarType: task.calendarType || 'WORK',
        urgency: task.urgency || 'NORMAL'
      }
    }))
}))

const todayAgenda = computed(() => buildAgendaByDate(dayjs()))
const tomorrowAgenda = computed(() => buildAgendaByDate(dayjs().add(1, 'day')))
const scheduleModalTitle = computed(() => (editingScheduleId.value ? '编辑行政日程' : '新增行政日程'))
const selectedCalendarDateAgenda = computed(() => buildAgendaByDate(dayjs(selectedCalendarDateText.value)))

const filteredStaffOptions = computed(() => {
  if (!scheduleForm.collaboratorDeptId) return staffOptions.value
  return staffOptions.value.filter((item) => staffDeptMap.value[item.value] === scheduleForm.collaboratorDeptId)
})

const calendarBuckets = computed(() => {
  const defs = [
    { type: 'PERSONAL' as const, label: '个人', color: '#52c41a' },
    { type: 'WORK' as const, label: '部门工作', color: '#1677ff' },
    { type: 'DAILY' as const, label: '日常计划', color: '#fa8c16' },
    { type: 'COLLAB' as const, label: '协同', color: '#722ed1' }
  ]
  return defs.map((item) => ({
    ...item,
    count: calendarRows.value.filter((task) => (task.calendarType || 'WORK') === item.type).length
  }))
})

function severityColor(level: string) {
  if (level === '紧急') return 'red'
  if (level === '预警') return 'orange'
  return 'blue'
}

function toggleCalendarType(type: 'PERSONAL' | 'WORK' | 'DAILY' | 'COLLAB') {
  if (visibleCalendarTypes.value.includes(type)) {
    if (visibleCalendarTypes.value.length === 1) return
    visibleCalendarTypes.value = visibleCalendarTypes.value.filter((item) => item !== type)
    return
  }
  visibleCalendarTypes.value = [...visibleCalendarTypes.value, type]
}

function calendarTypeText(value?: string) {
  if (value === 'PERSONAL') return '个人'
  if (value === 'DAILY') return '日常计划'
  if (value === 'COLLAB') return '协同日历'
  return '部门工作'
}

function agendaTimeText(task: OaTask) {
  const start = task.startTime ? dayjs(task.startTime) : undefined
  const end = task.endTime ? dayjs(task.endTime) : undefined
  if (!start) return '--'
  if (!end || start.format('YYYY-MM-DD') !== end.format('YYYY-MM-DD')) return start.format('MM-DD HH:mm')
  return `${start.format('HH:mm')} - ${end.format('HH:mm')}`
}

function buildAgendaByDate(target: Dayjs) {
  const dateText = target.format('YYYY-MM-DD')
  return calendarRows.value
    .filter((task) => {
      const start = task.startTime || task.endTime
      return start ? dayjs(start).format('YYYY-MM-DD') === dateText : false
    })
    .sort((a, b) => dayjs(a.startTime || a.endTime).valueOf() - dayjs(b.startTime || b.endTime).valueOf())
    .map((task) => ({
      id: String(task.id),
      title: task.title || '未命名日程',
      status: task.status || 'OPEN',
      timeText: agendaTimeText(task),
      typeText: calendarTypeText(task.calendarType),
      assigneeText: task.assigneeName || '',
      dateText
    }))
}

function focusCalendarDate(dateText: string) {
  const api = calendarRef.value?.getApi?.()
  if (!api) {
    message.warning('日历未就绪，请稍后再试')
    return
  }
  api.gotoDate(dateText)
  api.changeView('dayGridDay')
  agendaDrawerOpen.value = false
}

function upsertCalendarTask(task: OaTask) {
  const index = calendarRows.value.findIndex((item) => String(item.id) === String(task.id))
  if (index >= 0) {
    calendarRows.value[index] = { ...calendarRows.value[index], ...task }
    return
  }
  calendarRows.value = [...calendarRows.value, task]
}

function removeCalendarTaskById(id: string | number) {
  calendarRows.value = calendarRows.value.filter((item) => String(item.id) !== String(id))
}

function onUrgencyChange(value: 'NORMAL' | 'EMERGENCY') {
  if (value === 'EMERGENCY') {
    scheduleForm.eventColor = '#ff4d4f'
    return
  }
  scheduleForm.eventColor = scheduleForm.calendarType === 'PERSONAL'
    ? '#52c41a'
    : scheduleForm.calendarType === 'DAILY'
      ? '#fa8c16'
      : scheduleForm.calendarType === 'COLLAB'
        ? '#722ed1'
        : '#1677ff'
}

function addByRule(base: Dayjs, rule: 'DAILY' | 'WEEKLY' | 'MONTHLY', interval: number) {
  if (rule === 'DAILY') return base.add(interval, 'day')
  if (rule === 'MONTHLY') return base.add(interval, 'month')
  return base.add(interval, 'week')
}

function money(amount: number) {
  return `${Number(amount || 0).toFixed(2)}元`
}

function resolveTaskColor(task: OaTask) {
  if (task.status === 'DONE') return '#94a3b8'
  if (task.eventColor) return task.eventColor
  if (task.urgency === 'EMERGENCY') return '#ff4d4f'
  if (task.calendarType === 'COLLAB') return '#722ed1'
  if (task.calendarType === 'WORK') return '#1677ff'
  if (task.calendarType === 'DAILY') return '#fa8c16'
  if (task.calendarType === 'PERSONAL') return '#52c41a'
  return '#1677ff'
}

function resolveBedStatus(bed: BedItem): '空闲' | '清洁中' | '其他' {
  if (bed.elderId) return '其他'
  if (bed.status === 3) return '清洁中'
  if (bed.status === 1) return '空闲'
  return '其他'
}

function go(path: string, forceNewTab = false) {
  if (!path) return
  if (forceNewTab || path.startsWith('http://') || path.startsWith('https://')) {
    window.open(path, '_blank')
    return
  }
  router.push(path)
}

function handleCustomCardClick(item: PortalCustomCardItem) {
  if (Date.now() < suppressCardClickUntil.value) return
  go(item.route, item.openMode === 'new')
}

function clampCustomCardWidth(value: number) {
  return Math.max(170, Math.min(360, snapResizeValue(value, 10)))
}

function clampCustomCardHeight(value: number) {
  return Math.max(138, Math.min(320, snapResizeValue(value, 8)))
}

function clampModuleCardWidth(value: number) {
  return Math.max(260, Math.min(1200, snapResizeValue(value, 12)))
}

function clampModuleCardHeight(value: number) {
  return Math.max(180, Math.min(880, snapResizeValue(value, 10)))
}

function snapResizeValue(value: number, step: number) {
  return Math.round(value / step) * step
}

function showResizeIndicator(event: MouseEvent, width?: number, height?: number) {
  const parts = []
  if (width != null) parts.push(`宽 ${width}px`)
  if (height != null) parts.push(`高 ${height}px`)
  resizeIndicator.text = parts.join(' · ')
  resizeIndicator.x = event.clientX + 16
  resizeIndicator.y = event.clientY + 12
  resizeIndicator.visible = true
}

function hideResizeIndicator() {
  resizeIndicator.visible = false
  resizeIndicator.text = ''
}

function moduleCardStyle(key: PortalModuleKey) {
  const matched = moduleConfig.value.find((item) => item.key === key)
  const width = matched?.width ? clampModuleCardWidth(matched.width) : undefined
  const height = matched?.height ? clampModuleCardHeight(matched.height) : undefined
  return {
    width: width ? `${width}px` : '100%',
    maxWidth: '100%',
    minHeight: height ? `${height}px` : undefined
  }
}

function portalModuleCardClass(key: PortalModuleKey) {
  return {
    'is-dragging-source': draggingPortalModuleKey.value === key,
    'is-drop-zone': !!draggingPortalModuleKey.value && draggingPortalModuleKey.value !== key,
    'is-drop-target': portalModuleDropTargetKey.value === key,
    'is-invalid-bounce': moduleDragInvalidKey.value === key
  }
}

function customCardItemStyle(item: PortalCustomCardItem) {
  return {
    borderTopColor: item.themeColor || '#1677ff',
    width: item.width ? `${clampCustomCardWidth(item.width)}px` : '100%',
    maxWidth: '100%',
    height: item.height ? `${clampCustomCardHeight(item.height)}px` : '168px'
  }
}

function startResizeModuleCard(key: PortalModuleKey, direction: 'right' | 'bottom' | 'corner', event: MouseEvent) {
  const handleEl = event.currentTarget as HTMLElement | null
  const cardEl = handleEl?.closest('.module-card') as HTMLElement | null
  const target = moduleConfig.value.find((item) => item.key === key)
  if (!cardEl || !target) return
  resizingModuleCard.key = key
  resizingModuleCard.direction = direction
  resizingModuleCard.startX = event.clientX
  resizingModuleCard.startY = event.clientY
  resizingModuleCard.startWidth = target.width || cardEl.getBoundingClientRect().width
  resizingModuleCard.startHeight = target.height || cardEl.getBoundingClientRect().height
  resizingModuleCard.moved = false
  document.body.classList.add('resizing-active')
}

function startResizeCustomCard(id: string, direction: 'right' | 'bottom' | 'corner', event: MouseEvent) {
  const handleEl = event.currentTarget as HTMLElement | null
  const cardEl = handleEl?.parentElement as HTMLElement | null
  const target = customCards.value.find((item) => item.id === id)
  if (!cardEl || !target) return
  resizingCustomCard.id = id
  resizingCustomCard.direction = direction
  resizingCustomCard.startX = event.clientX
  resizingCustomCard.startY = event.clientY
  resizingCustomCard.startWidth = target.width || cardEl.getBoundingClientRect().width
  resizingCustomCard.startHeight = target.height || cardEl.getBoundingClientRect().height
  resizingCustomCard.moved = false
  document.body.classList.add('resizing-active')
}

function onCustomCardResizeMove(event: MouseEvent) {
  if (!resizingCustomCard.id || !resizingCustomCard.direction) return
  const deltaX = event.clientX - resizingCustomCard.startX
  const deltaY = event.clientY - resizingCustomCard.startY
  const useWidth = resizingCustomCard.direction === 'right' || resizingCustomCard.direction === 'corner'
  const useHeight = resizingCustomCard.direction === 'bottom' || resizingCustomCard.direction === 'corner'
  if (Math.abs(deltaX) > 1 || Math.abs(deltaY) > 1) resizingCustomCard.moved = true
  const nextWidth = useWidth ? clampCustomCardWidth(resizingCustomCard.startWidth + deltaX) : undefined
  const nextHeight = useHeight ? clampCustomCardHeight(resizingCustomCard.startHeight + deltaY) : undefined
  customCards.value = customCards.value.map((item) => {
    if (item.id !== resizingCustomCard.id) return item
    return {
      ...item,
      width: useWidth ? nextWidth : item.width,
      height: useHeight ? nextHeight : item.height
    }
  })
  showResizeIndicator(event, useWidth ? nextWidth : undefined, useHeight ? nextHeight : undefined)
}

function onCustomCardResizeEnd() {
  if (!resizingCustomCard.id) return
  if (resizingCustomCard.moved) {
    suppressCardClickUntil.value = Date.now() + 220
    persistCustomCards()
  }
  resizingCustomCard.id = ''
  resizingCustomCard.direction = ''
  if (!resizingModuleCard.key) document.body.classList.remove('resizing-active')
  hideResizeIndicator()
}

function onModuleCardResizeMove(event: MouseEvent) {
  if (!resizingModuleCard.key || !resizingModuleCard.direction) return
  const deltaX = event.clientX - resizingModuleCard.startX
  const deltaY = event.clientY - resizingModuleCard.startY
  const useWidth = resizingModuleCard.direction === 'right' || resizingModuleCard.direction === 'corner'
  const useHeight = resizingModuleCard.direction === 'bottom' || resizingModuleCard.direction === 'corner'
  if (Math.abs(deltaX) > 1 || Math.abs(deltaY) > 1) resizingModuleCard.moved = true
  const nextWidth = useWidth ? clampModuleCardWidth(resizingModuleCard.startWidth + deltaX) : undefined
  const nextHeight = useHeight ? clampModuleCardHeight(resizingModuleCard.startHeight + deltaY) : undefined
  moduleConfig.value = moduleConfig.value.map((item) => {
    if (item.key !== resizingModuleCard.key) return item
    return {
      ...item,
      width: useWidth ? nextWidth : item.width,
      height: useHeight ? nextHeight : item.height
    }
  })
  showResizeIndicator(event, useWidth ? nextWidth : undefined, useHeight ? nextHeight : undefined)
}

function onModuleCardResizeEnd() {
  if (!resizingModuleCard.key) return
  if (resizingModuleCard.moved) {
    persistModuleCustomize()
  }
  resizingModuleCard.key = ''
  resizingModuleCard.direction = ''
  if (!resizingCustomCard.id) document.body.classList.remove('resizing-active')
  hideResizeIndicator()
}

function customCardCategoryText(category?: string) {
  if (category === 'OPS') return '运营'
  if (category === 'CARE') return '医护'
  if (category === 'FINANCE') return '财务'
  if (category === 'OA') return '行政'
  return '自定义'
}

function normalizeCardAudience(values: unknown): AudienceCode[] {
  const valid = new Set(['ALL', 'DIRECTOR', 'NURSE', 'FINANCE', 'ADMIN', 'HR', 'OPS'])
  const list = Array.isArray(values) ? values : []
  const normalized = list
    .map((item) => String(item || '').toUpperCase())
    .filter((item) => valid.has(item)) as AudienceCode[]
  if (!normalized.length) return ['ALL']
  if (normalized.includes('ALL')) return ['ALL']
  return Array.from(new Set(normalized))
}

function audienceText(audience?: AudienceCode[]) {
  const normalized = normalizeCardAudience(audience)
  if (normalized.includes('ALL')) return '全员可见'
  return normalized.map((item) => {
    if (item === 'DIRECTOR') return '院长'
    if (item === 'NURSE') return '护士'
    if (item === 'FINANCE') return '财务'
    if (item === 'ADMIN') return '管理员'
    if (item === 'HR') return '人事'
    if (item === 'OPS') return '运营'
    return item
  }).join(' / ')
}

function resolveCurrentUserAudience() {
  const rawParts: string[] = []
  const anyInfo = userStore.staffInfo as any
  if (Array.isArray(anyInfo?.roleNames)) rawParts.push(...anyInfo.roleNames.map((item: any) => String(item)))
  if (Array.isArray(anyInfo?.roles)) rawParts.push(...anyInfo.roles.map((item: any) => String(item?.name || item?.roleName || item)))
  if (anyInfo?.roleName) rawParts.push(String(anyInfo.roleName))
  const merged = rawParts.join(' ').toLowerCase()
  const roleSet = new Set<AudienceCode>(['ALL'])
  if (/院长|总监|director/.test(merged)) roleSet.add('DIRECTOR')
  if (/护士|护理|nurse/.test(merged)) roleSet.add('NURSE')
  if (/财务|finance|会计/.test(merged)) roleSet.add('FINANCE')
  if (/管理员|admin|super/.test(merged)) roleSet.add('ADMIN')
  if (/人事|hr/.test(merged)) roleSet.add('HR')
  if (/运营|ops|operation/.test(merged)) roleSet.add('OPS')
  return Array.from(roleSet)
}

function cardAudienceMatched(cardAudience: AudienceCode[] | undefined, userAudience: AudienceCode[]) {
  const normalizedCard = normalizeCardAudience(cardAudience)
  if (normalizedCard.includes('ALL')) return true
  return normalizedCard.some((item) => userAudience.includes(item))
}

function normalizeText(text: string) {
  return String(text || '')
    .toLowerCase()
    .replace(/[\s\-_/|（）()]+/g, '')
}

function getChineseInitial(char: string) {
  const first = char.slice(0, 1)
  if (!/[\u4e00-\u9fa5]/.test(first)) return ''
  for (let i = 0; i < PINYIN_INITIAL_BOUNDARY_CHARS.length; i += 1) {
    const current = PINYIN_INITIAL_BOUNDARY_CHARS[i]
    const next = PINYIN_INITIAL_BOUNDARY_CHARS[i + 1]
    if (!next) return PINYIN_INITIAL_LETTERS[i] || ''
    if (first.localeCompare(current, 'zh-CN') >= 0 && first.localeCompare(next, 'zh-CN') < 0) {
      return PINYIN_INITIAL_LETTERS[i] || ''
    }
  }
  return ''
}

function toPinyinInitials(text: string) {
  let result = ''
  const raw = String(text || '')
  for (const char of raw) {
    if (/[a-zA-Z0-9]/.test(char)) {
      result += char.toLowerCase()
      continue
    }
    const initial = getChineseInitial(char)
    if (initial) result += initial.toLowerCase()
  }
  return result
}

function fuzzyScore(text: string, keyword: string) {
  const target = normalizeText(text)
  const query = normalizeText(keyword)
  if (!query) return 0
  if (!target) return -1
  if (target.includes(query)) {
    const start = target.indexOf(query)
    return 200 - start
  }
  let score = 0
  let cursor = 0
  for (const char of query) {
    const index = target.indexOf(char, cursor)
    if (index < 0) return -1
    score += Math.max(8 - (index - cursor), 1)
    cursor = index + 1
  }
  return score
}

function updateCustomCardRouteOptions(keyword = '') {
  const text = String(keyword || '').trim()
  if (!text) {
    customCardRouteOptions.value = searchIndex.value.slice(0, 20).map((item) => ({
      value: item.route,
      label: `${item.title} · ${item.route}`
    }))
    return
  }
  const scored = searchIndex.value
    .map((item) => ({ item, score: fuzzyScore(`${item.title} ${item.keywords}`, text) }))
    .filter((row) => row.score >= 0)
    .sort((a, b) => b.score - a.score || a.item.route.length - b.item.route.length)
    .slice(0, 20)
  customCardRouteOptions.value = scored.map((row) => ({
    value: row.item.route,
    label: `${row.item.title} · ${row.item.route}`
  }))
}

function onCustomCardRouteSearch(value: string) {
  updateCustomCardRouteOptions(value)
}

function onCustomCardRouteSelect(value: string) {
  customCardForm.route = value
}

function loadRecentSearchRoutes() {
  try {
    const raw = localStorage.getItem(SEARCH_RECENT_KEY)
    if (!raw) return
    const parsed = JSON.parse(raw)
    if (Array.isArray(parsed)) {
      recentSearchRoutes.value = parsed.map((item) => String(item)).slice(0, SEARCH_RECENT_MAX)
    }
  } catch {
    recentSearchRoutes.value = []
  }
}

function saveRecentSearchRoute(route: string) {
  if (!route) return
  const next = [route, ...recentSearchRoutes.value.filter((item) => item !== route)].slice(0, SEARCH_RECENT_MAX)
  recentSearchRoutes.value = next
  try {
    localStorage.setItem(SEARCH_RECENT_KEY, JSON.stringify(next))
  } catch {}
}

function buildSearchIndex() {
  const routeRecords = router.getRoutes()
  const dynamicEntries = routeRecords
    .map((record) => {
      const title = String((record.meta as any)?.title || '')
      const path = String(record.path || '')
      const hasVisibleTitle = !!title && title !== 'Dashboard'
      if (!path.startsWith('/')) return null
      if (!hasVisibleTitle && !searchAliases[path]) return null
      if (String((record.meta as any)?.hidden || '') === 'true') return null
      const aliasWords = searchAliases[path] || []
      const rawKeywords = [title, path, String(record.name || ''), aliasWords.join(' ')].filter(Boolean).join(' ')
      const keywordInitials = toPinyinInitials(rawKeywords)
      const keywords = `${rawKeywords} ${keywordInitials}`.trim()
      return {
        title: hasVisibleTitle ? title : aliasWords[0] || path,
        route: path,
        keywords
      }
    })
    .filter((item): item is { title: string; route: string; keywords: string } => !!item)

  const dedup = new Map<string, { title: string; route: string; keywords: string }>()
  dynamicEntries.forEach((item) => {
    if (!dedup.has(item.route)) dedup.set(item.route, item)
  })
  searchIndex.value = Array.from(dedup.values())
}

function buildOptionLabel(entry: { title: string; route: string }, score?: number) {
  return score != null && score > 0
    ? `${entry.title} · ${entry.route}（匹配度 ${score}）`
    : `${entry.title} · ${entry.route}`
}

function updateSearchOptions(keyword?: string) {
  const text = String(keyword || searchKeyword.value || '').trim()
  if (!text) {
    const pinnedEntries = searchPinnedRoutes
      .map((route) => searchIndex.value.find((item) => item.route === route))
      .filter((item): item is { title: string; route: string; keywords: string } => !!item)
    const recentEntries = recentSearchRoutes.value
      .map((route) => searchIndex.value.find((item) => item.route === route))
      .filter((item): item is { title: string; route: string; keywords: string } => !!item)
    const merged = [...recentEntries, ...pinnedEntries]
    const dedup = new Map<string, { title: string; route: string; keywords: string }>()
    merged.forEach((item) => {
      if (!dedup.has(item.route)) dedup.set(item.route, item)
    })
    searchOptions.value = Array.from(dedup.values()).slice(0, 24).map((item) => ({
      value: item.title,
      label: buildOptionLabel(item),
      route: item.route
    }))
    return
  }

  const scored = searchIndex.value
    .map((item) => ({ item, score: fuzzyScore(item.keywords, text) }))
    .filter((row) => row.score >= 0)
    .sort((a, b) => b.score - a.score || a.item.route.length - b.item.route.length)
    .slice(0, 40)

  searchOptions.value = scored.map((row) => ({
    value: row.item.title,
    label: buildOptionLabel(row.item, row.score),
    route: row.item.route
  }))
}

function onSearchChange(text: string) {
  searchKeyword.value = text
  updateSearchOptions(text)
}

function onSearchSelect(_value: string, option: any) {
  if (!option?.route) return
  saveRecentSearchRoute(option.route)
  go(option.route)
}

function submitSearch(value: string) {
  const text = String(value || searchKeyword.value || '').trim()
  if (!text) {
    updateSearchOptions('')
    return
  }
  updateSearchOptions(text)
  const first = searchOptions.value[0]
  if (first?.route) {
    saveRecentSearchRoute(first.route)
    go(first.route)
    return
  }
  message.warning('没有匹配到页面，请尝试更短关键词')
}

function loadModuleCustomize() {
  try {
    const raw = localStorage.getItem(moduleStorageKey())
    if (!raw) return
    const parsed = JSON.parse(raw)
    if (!Array.isArray(parsed)) return
    const visibilityMap = new Map<string, boolean>()
    const orderMap = new Map<string, number>()
    const audienceMap = new Map<string, AudienceCode[]>()
    const widthMap = new Map<string, number | undefined>()
    const heightMap = new Map<string, number | undefined>()
    parsed.forEach((item: any) => {
      visibilityMap.set(String(item.key), Boolean(item.visible))
      orderMap.set(String(item.key), Number(item.order))
      audienceMap.set(String(item.key), normalizeCardAudience(item.audience))
      const width = Number(item.width)
      const height = Number(item.height)
      widthMap.set(String(item.key), Number.isFinite(width) ? clampModuleCardWidth(width) : undefined)
      heightMap.set(String(item.key), Number.isFinite(height) ? clampModuleCardHeight(height) : undefined)
    })
    moduleConfig.value = portalModuleCatalog.map((item) => ({
      ...item,
      visible: visibilityMap.has(item.key) ? Boolean(visibilityMap.get(item.key)) : true,
      order: Number.isFinite(orderMap.get(item.key)) ? Number(orderMap.get(item.key)) : portalModuleCatalog.findIndex((row) => row.key === item.key),
      audience: audienceMap.get(item.key) || normalizeCardAudience(item.audience),
      width: widthMap.get(item.key),
      height: heightMap.get(item.key)
    })).sort((a, b) => a.order - b.order).map((item, index) => ({ ...item, order: index }))
  } catch {}
}

function persistModuleCustomize() {
  try {
    persistModuleCustomizeStrict()
  } catch {}
}

function applyModulePreset(preset: 'director' | 'nurse' | 'finance') {
  const keyOrder = modulePresetMap[preset]
  const fallback = portalModuleCatalog.map((item) => item.key)
  const fullOrder = [...keyOrder, ...fallback.filter((key) => !keyOrder.includes(key))]
  moduleConfigDraft.value = fullOrder
    .map((key, index) => {
      const base = portalModuleCatalog.find((item) => item.key === key)
      if (!base) return null
      return {
        ...base,
        visible: true,
        order: index,
        audience: normalizeCardAudience(base.audience),
        width: undefined,
        height: undefined
      }
    })
    .filter((item): item is PortalModuleConfigItem => !!item)
  message.success('已应用预设，可继续微调后保存')
}

function openModuleCustomize() {
  moduleConfigDraft.value = [...moduleConfig.value]
    .sort((a, b) => a.order - b.order)
    .map((item, index) => ({ ...item, order: index }))
  moduleCustomizeOpen.value = true
}

function resetModuleCustomize() {
  moduleConfigDraft.value = portalModuleCatalog.map((item, index) => ({
    ...item,
    visible: true,
    order: index,
    audience: normalizeCardAudience(item.audience),
    width: undefined,
    height: undefined
  }))
}

function resetAllModuleCardSizes() {
  moduleConfig.value = moduleConfig.value.map((item) => ({ ...item, width: undefined, height: undefined }))
  moduleConfigDraft.value = moduleConfigDraft.value.map((item) => ({ ...item, width: undefined, height: undefined }))
  persistModuleCustomize()
  message.success('模块尺寸已重置')
}

function moveModule(index: number, delta: -1 | 1) {
  const targetIndex = index + delta
  if (targetIndex < 0 || targetIndex >= moduleConfigDraft.value.length) return
  const next = [...moduleConfigDraft.value]
  const [moved] = next.splice(index, 1)
  next.splice(targetIndex, 0, moved)
  moduleConfigDraft.value = next.map((item, itemIndex) => ({ ...item, order: itemIndex }))
}

function onModuleDragStart(key: PortalModuleKey) {
  draggingModuleKey.value = key
}

function onModuleDrop(targetKey: PortalModuleKey) {
  const sourceKey = draggingModuleKey.value
  if (!sourceKey || sourceKey === targetKey) return
  const sourceIndex = moduleConfigDraft.value.findIndex((item) => item.key === sourceKey)
  const targetIndex = moduleConfigDraft.value.findIndex((item) => item.key === targetKey)
  if (sourceIndex < 0 || targetIndex < 0) return
  const next = [...moduleConfigDraft.value]
  const [moved] = next.splice(sourceIndex, 1)
  next.splice(targetIndex, 0, moved)
  moduleConfigDraft.value = next.map((item, index) => ({ ...item, order: index }))
  draggingModuleKey.value = ''
}

function onModuleDragEnd() {
  draggingModuleKey.value = ''
}

function clearPortalModuleHoldTimer() {
  if (moduleDragHoldTimer) {
    window.clearTimeout(moduleDragHoldTimer)
    moduleDragHoldTimer = undefined
  }
}

function resetPortalModuleDragState() {
  clearPortalModuleHoldTimer()
  moduleDragReadyKey.value = ''
  draggingPortalModuleKey.value = ''
  portalModuleDropTargetKey.value = ''
  moduleDragCommitted.value = false
  moduleDragSnapshot.value = null
}

function onPortalModuleHandleMouseDown(key: PortalModuleKey) {
  if (draggingPortalModuleKey.value) return
  clearPortalModuleHoldTimer()
  moduleDragReadyKey.value = ''
  moduleDragHoldTimer = window.setTimeout(() => {
    moduleDragReadyKey.value = key
  }, 120)
}

function onPortalModuleHandleRelease(event?: MouseEvent) {
  if (event?.type === 'mouseleave') {
    return
  }
  if (!draggingPortalModuleKey.value) {
    clearPortalModuleHoldTimer()
    moduleDragReadyKey.value = ''
  }
}

function onPortalModuleCardDragStart(key: PortalModuleKey, event: DragEvent) {
  if (moduleDragReadyKey.value !== key) {
    event.preventDefault()
    return
  }
  const dataTransfer = event.dataTransfer
  if (dataTransfer) {
    dataTransfer.effectAllowed = 'move'
    dataTransfer.dropEffect = 'move'
    dataTransfer.setData('text/plain', key)
  }
  draggingPortalModuleKey.value = key
  portalModuleDropTargetKey.value = ''
  moduleDragCommitted.value = false
  moduleDragSnapshot.value = moduleConfig.value.map((item) => ({ ...item }))
}

function onPortalModuleDragOver(targetKey: PortalModuleKey, event: DragEvent) {
  if (!draggingPortalModuleKey.value || draggingPortalModuleKey.value === targetKey) return
  event.preventDefault()
  if (event.dataTransfer) {
    event.dataTransfer.dropEffect = 'move'
  }
  portalModuleDropTargetKey.value = targetKey
}

function reorderModuleConfig(sourceKey: PortalModuleKey, targetKey: PortalModuleKey, source: PortalModuleConfigItem[]) {
  if (sourceKey === targetKey) return source
  const sourceIndex = source.findIndex((item) => item.key === sourceKey)
  const targetIndex = source.findIndex((item) => item.key === targetKey)
  if (sourceIndex < 0 || targetIndex < 0) return source
  const next = [...source]
  const [moved] = next.splice(sourceIndex, 1)
  next.splice(targetIndex, 0, moved)
  return next.map((item, index) => ({ ...item, order: index }))
}

function persistModuleCustomizeStrict(source = moduleConfig.value) {
  const simple = [...source]
    .sort((a, b) => a.order - b.order)
    .map((item, index) => ({
      key: item.key,
      visible: item.visible,
      order: index,
      audience: normalizeCardAudience(item.audience),
      width: item.width != null ? clampModuleCardWidth(item.width) : undefined,
      height: item.height != null ? clampModuleCardHeight(item.height) : undefined
    }))
  localStorage.setItem(moduleStorageKey(), JSON.stringify(simple))
}

async function onPortalModuleDrop(targetKey: PortalModuleKey) {
  const sourceKey = draggingPortalModuleKey.value
  if (!sourceKey || sourceKey === targetKey) return
  const snapshot = moduleDragSnapshot.value?.map((item) => ({ ...item })) || moduleConfig.value.map((item) => ({ ...item }))
  const next = reorderModuleConfig(sourceKey, targetKey, moduleConfig.value)
  moduleConfig.value = next
  moduleDragCommitted.value = true
  try {
    persistModuleCustomizeStrict(next)
    message.success('首页卡片顺序已更新')
  } catch (error: any) {
    moduleConfig.value = snapshot
    message.error(error?.message || '排序保存失败，已回滚')
  } finally {
    resetPortalModuleDragState()
  }
}

function onPortalModuleCardDragEnd() {
  const sourceKey = draggingPortalModuleKey.value
  const committed = moduleDragCommitted.value
  resetPortalModuleDragState()
  if (!committed && sourceKey) {
    moduleDragInvalidKey.value = sourceKey
    window.setTimeout(() => {
      if (moduleDragInvalidKey.value === sourceKey) {
        moduleDragInvalidKey.value = ''
      }
    }, 240)
  }
}

function applyModuleCustomize() {
  const visibleCount = moduleConfigDraft.value.filter((item) => item.visible).length
  if (visibleCount === 0) {
    message.warning('请至少保留一个首页卡片')
    return
  }
  moduleConfig.value = [...moduleConfigDraft.value]
    .sort((a, b) => a.order - b.order)
    .map((item, index) => ({
      ...item,
      order: index,
      audience: normalizeCardAudience(item.audience),
      width: item.width != null ? clampModuleCardWidth(item.width) : undefined,
      height: item.height != null ? clampModuleCardHeight(item.height) : undefined
    }))
  persistModuleCustomize()
  moduleCustomizeOpen.value = false
  message.success('首页卡片配置已保存')
}

function resetAllHomepageCustomize() {
  Modal.confirm({
    title: '确认恢复首页全部默认配置？',
    content: '将重置模块显示顺序和自定义卡面。',
    onOk: async () => {
      moduleConfig.value = portalModuleCatalog.map((item, index) => ({
        ...item,
        visible: true,
        order: index,
        audience: normalizeCardAudience(item.audience),
        width: undefined,
        height: undefined
      }))
      moduleConfigDraft.value = portalModuleCatalog.map((item, index) => ({
        ...item,
        visible: true,
        order: index,
        audience: normalizeCardAudience(item.audience),
        width: undefined,
        height: undefined
      }))
      customCards.value = defaultCustomCards.map((item) => ({ ...item }))
      persistModuleCustomize()
      persistCustomCards()
      moduleCustomizeOpen.value = false
      message.success('首页已恢复默认')
    }
  })
}

function normalizeCustomRoute(route: string) {
  const text = String(route || '').trim()
  if (!text) return ''
  if (text.startsWith('http://') || text.startsWith('https://')) return text
  return text.startsWith('/') ? text : `/${text}`
}

function normalizeCustomCardCategory(value: unknown): 'OPS' | 'CARE' | 'FINANCE' | 'OA' | 'CUSTOM' {
  const text = String(value || '')
  if (text === 'OPS' || text === 'CARE' || text === 'FINANCE' || text === 'OA' || text === 'CUSTOM') return text
  return 'CUSTOM'
}

function normalizeCustomCardPayload(item: any, fallbackId: string) {
  const rawWidth = Number(item?.width)
  const rawHeight = Number(item?.height)
  return {
    id: String(item?.id || fallbackId),
    title: String(item?.title || ''),
    route: normalizeCustomRoute(String(item?.route || '')),
    description: String(item?.description || ''),
    themeColor: String(item?.themeColor || '#1677ff'),
    openMode: item?.openMode === 'new' ? 'new' as const : 'current' as const,
    icon: String(item?.icon || '🧩').trim().slice(0, 2) || '🧩',
    category: normalizeCustomCardCategory(item?.category),
    visible: item?.visible !== false,
    audience: normalizeCardAudience(item?.audience),
    width: Number.isFinite(rawWidth) ? clampCustomCardWidth(rawWidth) : undefined,
    height: Number.isFinite(rawHeight) ? clampCustomCardHeight(rawHeight) : 168
  }
}

function loadCustomCards() {
  try {
    const raw = localStorage.getItem(customCardStorageKey())
    if (!raw) {
      customCards.value = defaultCustomCards.map((item) => ({ ...item }))
      return
    }
    const parsed = JSON.parse(raw)
    if (!Array.isArray(parsed)) {
      customCards.value = defaultCustomCards.map((item) => ({ ...item }))
      return
    }
    customCards.value = parsed
      .map((item: any, index: number) => normalizeCustomCardPayload(item, `card_auto_${index}`))
      .filter((item: PortalCustomCardItem) => !!item.id && !!item.title && !!item.route)
  } catch {
    customCards.value = defaultCustomCards.map((item) => ({ ...item }))
  }
}

function persistCustomCards() {
  try {
    localStorage.setItem(customCardStorageKey(), JSON.stringify(customCards.value))
  } catch {}
}

function openCustomCardEditor(id?: string) {
  if (!id) {
    customCardEditingId.value = ''
    customCardForm.title = ''
    customCardForm.route = ''
    customCardForm.description = ''
    customCardForm.themeColor = '#1677ff'
    customCardForm.openMode = 'current'
    customCardForm.icon = '🧩'
    customCardForm.category = 'CUSTOM'
    customCardForm.audience = ['ALL']
    updateCustomCardRouteOptions('')
    customCardEditorOpen.value = true
    return
  }
  const target = customCards.value.find((item) => item.id === id)
  if (!target) {
    message.warning('未找到卡面')
    return
  }
  customCardEditingId.value = target.id
  customCardForm.title = target.title
  customCardForm.route = target.route
  customCardForm.description = target.description
  customCardForm.themeColor = target.themeColor || '#1677ff'
  customCardForm.openMode = target.openMode || 'current'
  customCardForm.icon = target.icon || '🧩'
  customCardForm.category = target.category || 'CUSTOM'
  customCardForm.audience = normalizeCardAudience(target.audience)
  updateCustomCardRouteOptions(target.route)
  customCardEditorOpen.value = true
}

function saveCustomCard() {
  const title = String(customCardForm.title || '').trim()
  const route = normalizeCustomRoute(customCardForm.route)
  const description = String(customCardForm.description || '').trim()
  const themeColor = String(customCardForm.themeColor || '#1677ff')
  const openMode = customCardForm.openMode === 'new' ? 'new' : 'current'
  const icon = String(customCardForm.icon || '🧩').trim().slice(0, 2) || '🧩'
  const category = customCardForm.category || 'CUSTOM'
  const audience = normalizeCardAudience(customCardForm.audience)
  if (!title) {
    message.warning('请填写卡面标题')
    return
  }
  if (!route) {
    message.warning('请填写跳转地址')
    return
  }
  if (!/^https?:\/\//.test(route) && !route.startsWith('/')) {
    message.warning('地址格式不正确，请填写系统路由或 http(s) 地址')
    return
  }
  if (route.startsWith('/') && !searchIndex.value.some((item) => item.route === route)) {
    message.warning('未识别到系统内路由，请确认地址是否正确')
    return
  }
  const duplicate = customCards.value.find((item) => item.route === route && item.id !== customCardEditingId.value)
  if (duplicate) {
    message.warning(`该地址已存在卡面：${duplicate.title}`)
    return
  }
  if (!customCardEditingId.value && customCards.value.length >= 60) {
    message.warning('卡面数量最多 60 个，请先删除部分卡面')
    return
  }
  if (customCardEditingId.value) {
    customCards.value = customCards.value.map((item) => item.id === customCardEditingId.value ? {
      ...item,
      title,
      route,
      description,
      themeColor,
      openMode,
      icon,
      category,
      audience
    } : item)
    persistCustomCards()
    customCardEditorOpen.value = false
    customCardEditingId.value = ''
    message.success('卡面已更新')
    return
  }
  const id = `card_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`
  customCards.value = [...customCards.value, {
    id,
    title,
    route,
    description,
    themeColor,
    openMode,
    icon,
    category,
    visible: true,
    audience,
    height: 168
  }]
  persistCustomCards()
  customCardEditorOpen.value = false
  customCardEditingId.value = ''
  message.success('卡面已新增')
}

function addCustomCardFromTemplate(templateKey: string) {
  const template = customCardTemplateCatalog.find((item) => item.key === templateKey)
  if (!template) return
  const existed = customCards.value.some((item) => item.title === template.title && item.route === template.route)
  if (existed) {
    message.info('该模板卡面已存在')
    return
  }
  const id = `tpl_${template.key}_${Date.now()}`
  customCards.value = [...customCards.value, { ...template, id, visible: true, audience: normalizeCardAudience(template.audience) }]
  persistCustomCards()
  message.success(`已添加模板卡面：${template.title}`)
}

function duplicateCustomCard(id: string) {
  const source = customCards.value.find((item) => item.id === id)
  if (!source) {
    message.warning('未找到可复制的卡面')
    return
  }
  if (customCards.value.length >= 60) {
    message.warning('卡面数量最多 60 个，请先删除部分卡面')
    return
  }
  const duplicated: PortalCustomCardItem = {
    ...source,
    id: `dup_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`,
    title: `${source.title}（副本）`,
    width: source.width,
    height: source.height
  }
  customCards.value = [...customCards.value, duplicated]
  persistCustomCards()
  message.success('已复制卡面')
}

function addRecentRouteAsCards() {
  const candidates = recentSearchRoutes.value.slice(0, 6)
  if (!candidates.length) {
    message.info('最近暂无访问记录')
    return
  }
  const toAdd = candidates
    .map((route) => searchIndex.value.find((item) => item.route === route))
    .filter((item): item is { title: string; route: string; keywords: string } => !!item)
    .filter((item) => !customCards.value.some((card) => card.route === item.route))
  if (!toAdd.length) {
    message.info('最近访问页面已全部存在')
    return
  }
  const next = toAdd.map((item, idx) => ({
    id: `recent_${Date.now()}_${idx}`,
    title: item.title,
    route: item.route,
    description: '从最近访问自动添加',
    themeColor: '#1677ff',
    openMode: 'current' as const,
    icon: '🧩',
    category: 'CUSTOM' as const,
    visible: true,
    audience: ['ALL'] as AudienceCode[],
    height: 168
  }))
  customCards.value = [...customCards.value, ...next]
  persistCustomCards()
  message.success(`已添加 ${next.length} 张最近访问卡面`)
}

function onCustomCardDragStart(id: string) {
  draggingCustomCardId.value = id
}

function onCustomCardDragOver(_id: string) {}

function onCustomCardDrop(targetId: string) {
  const sourceId = draggingCustomCardId.value
  if (!sourceId || sourceId === targetId) return
  const sourceIndex = customCards.value.findIndex((item) => item.id === sourceId)
  const targetIndex = customCards.value.findIndex((item) => item.id === targetId)
  if (sourceIndex < 0 || targetIndex < 0) return
  const next = [...customCards.value]
  const [moved] = next.splice(sourceIndex, 1)
  next.splice(targetIndex, 0, moved)
  customCards.value = next
  draggingCustomCardId.value = ''
  persistCustomCards()
}

function onCustomCardDragEnd() {
  draggingCustomCardId.value = ''
}

function removeCustomCard(id: string) {
  Modal.confirm({
    title: '确认删除该卡面？',
    okButtonProps: { danger: true },
    onOk: async () => {
      customCards.value = customCards.value.filter((item) => item.id !== id)
      persistCustomCards()
      message.success('卡面已删除')
    }
  })
}

function setCustomCardVisibleByFilter(visible: boolean) {
  if (!manageFilteredCustomCards.value.length) {
    message.info('当前没有可操作的卡面')
    return
  }
  const targetIds = new Set(manageFilteredCustomCards.value.map((item) => item.id))
  customCards.value = customCards.value.map((item) => targetIds.has(item.id) ? { ...item, visible } : item)
  persistCustomCards()
  message.success(visible ? '已将当前结果全部设为显示' : '已将当前结果全部设为隐藏')
}

function removeHiddenCustomCards() {
  const hiddenCount = customCards.value.filter((item) => item.visible === false).length
  if (!hiddenCount) {
    message.info('暂无已隐藏卡面')
    return
  }
  Modal.confirm({
    title: `确认清理 ${hiddenCount} 个已隐藏卡面？`,
    okButtonProps: { danger: true },
    onOk: async () => {
      customCards.value = customCards.value.filter((item) => item.visible !== false)
      persistCustomCards()
      message.success('已清理隐藏卡面')
    }
  })
}

function moveCustomCard(index: number, delta: -1 | 1) {
  const targetIndex = index + delta
  if (targetIndex < 0 || targetIndex >= customCards.value.length) return
  const next = [...customCards.value]
  const [moved] = next.splice(index, 1)
  next.splice(targetIndex, 0, moved)
  customCards.value = next
  persistCustomCards()
}

function resetCustomCards() {
  Modal.confirm({
    title: '确认恢复默认卡面？',
    onOk: async () => {
      customCards.value = defaultCustomCards.map((item) => ({ ...item }))
      persistCustomCards()
      message.success('已恢复默认卡面')
    }
  })
}

function resetAllCustomCardSizes() {
  customCards.value = customCards.value.map((item) => ({ ...item, width: undefined, height: 168 }))
  persistCustomCards()
  message.success('卡面尺寸已重置')
}

function exportHomepageCustomize() {
  const payload = {
    version: 1,
    exportedAt: dayjs().format('YYYY-MM-DD HH:mm:ss'),
    moduleConfig: [...moduleConfig.value]
      .sort((a, b) => a.order - b.order)
      .map((item, index) => ({
        key: item.key,
        visible: item.visible,
        order: index,
        audience: normalizeCardAudience(item.audience),
        width: item.width != null ? clampModuleCardWidth(item.width) : undefined,
        height: item.height != null ? clampModuleCardHeight(item.height) : undefined
      })),
    customCards: customCards.value
  }
  exportPayloadText.value = JSON.stringify(payload, null, 2)
  exportPayloadOpen.value = true
}

async function copyExportPayload() {
  if (!exportPayloadText.value) return
  try {
    await navigator.clipboard.writeText(exportPayloadText.value)
    message.success('已复制配置 JSON')
  } catch {
    message.warning('复制失败，请手动复制')
  }
}

function applyImportedHomepageCustomize() {
  let parsed: any
  try {
    parsed = JSON.parse(String(importPayloadText.value || '').trim())
  } catch {
    message.error('JSON 格式不正确')
    return
  }
  if (!parsed || typeof parsed !== 'object') {
    message.error('配置内容无效')
    return
  }
  if (Array.isArray(parsed.moduleConfig)) {
    const visibilityMap = new Map<string, boolean>()
    const orderMap = new Map<string, number>()
    const audienceMap = new Map<string, AudienceCode[]>()
    const widthMap = new Map<string, number | undefined>()
    const heightMap = new Map<string, number | undefined>()
    parsed.moduleConfig.forEach((item: any, index: number) => {
      const key = String(item?.key || '')
      if (!key) return
      visibilityMap.set(key, item?.visible !== false)
      orderMap.set(key, Number.isFinite(Number(item?.order)) ? Number(item.order) : index)
      audienceMap.set(key, normalizeCardAudience(item?.audience))
      const width = Number(item?.width)
      const height = Number(item?.height)
      widthMap.set(key, Number.isFinite(width) ? clampModuleCardWidth(width) : undefined)
      heightMap.set(key, Number.isFinite(height) ? clampModuleCardHeight(height) : undefined)
    })
    moduleConfig.value = portalModuleCatalog
      .map((item, index) => ({
        ...item,
        visible: visibilityMap.has(item.key) ? Boolean(visibilityMap.get(item.key)) : true,
        order: orderMap.has(item.key) ? Number(orderMap.get(item.key)) : index,
        audience: audienceMap.get(item.key) || normalizeCardAudience(item.audience),
        width: widthMap.get(item.key),
        height: heightMap.get(item.key)
      }))
      .sort((a, b) => a.order - b.order)
      .map((item, index) => ({ ...item, order: index }))
    if (!moduleConfig.value.some((item) => item.visible)) {
      message.error('导入失败：至少需要保留一个首页模块')
      return
    }
  }
  if (Array.isArray(parsed.customCards)) {
    const nextCards = parsed.customCards
      .map((item: any, index: number) => normalizeCustomCardPayload(item, `import_${Date.now()}_${index}`))
      .filter((item: PortalCustomCardItem) => !!item.title && !!item.route)
    customCards.value = nextCards
  }
  if (!Array.isArray(parsed.moduleConfig) && !Array.isArray(parsed.customCards)) {
    message.error('导入失败：缺少 moduleConfig 或 customCards 字段')
    return
  }
  persistModuleCustomize()
  persistCustomCards()
  importPayloadOpen.value = false
  importPayloadText.value = ''
  message.success('首页配置导入成功')
}

function openCreateSchedule(date?: Dayjs) {
  editingScheduleId.value = null
  scheduleForm.title = ''
  scheduleForm.startTime = date ? date.hour(9).minute(0).second(0) : undefined
  scheduleForm.endTime = date ? date.hour(10).minute(0).second(0) : undefined
  scheduleForm.assigneeName = (userStore.staffInfo?.realName || userStore.staffInfo?.username || '').trim()
  scheduleForm.priority = 'NORMAL'
  scheduleForm.description = ''
  scheduleForm.calendarType = 'WORK'
  scheduleForm.urgency = 'NORMAL'
  scheduleForm.planCategory = '基础办公'
  scheduleForm.eventColor = '#1677ff'
  scheduleForm.collaboratorDeptId = undefined
  scheduleForm.collaboratorIds = []
  scheduleForm.recurring = false
  scheduleForm.recurrenceRule = 'WEEKLY'
  scheduleForm.recurrenceInterval = 1
  scheduleForm.recurrenceCount = 4
  scheduleOpen.value = true
}

function openEditSchedule(id: string | number) {
  const matched = calendarRows.value.find((item) => String(item.id) === String(id))
  if (!matched) {
    message.warning('未找到对应日程')
    return
  }
  editingScheduleId.value = matched.id
  scheduleForm.title = matched.title || ''
  scheduleForm.startTime = matched.startTime ? dayjs(matched.startTime) : undefined
  scheduleForm.endTime = matched.endTime ? dayjs(matched.endTime) : undefined
  scheduleForm.assigneeName = matched.assigneeName || ''
  scheduleForm.priority = (matched.priority as any) || 'NORMAL'
  scheduleForm.description = matched.description || ''
  scheduleForm.calendarType = matched.calendarType || 'WORK'
  scheduleForm.urgency = matched.urgency || 'NORMAL'
  scheduleForm.planCategory = matched.planCategory || '基础办公'
  scheduleForm.eventColor = matched.eventColor || resolveTaskColor(matched)
  scheduleForm.collaboratorDeptId = undefined
  scheduleForm.collaboratorIds = Array.isArray(matched.collaboratorIds)
    ? matched.collaboratorIds.map((item) => String(item))
    : typeof matched.collaboratorIds === 'string' && matched.collaboratorIds
      ? matched.collaboratorIds.split(',').map((item) => item.trim()).filter(Boolean)
      : []
  scheduleForm.recurring = false
  scheduleForm.recurrenceRule = 'WEEKLY'
  scheduleForm.recurrenceInterval = 1
  scheduleForm.recurrenceCount = 1
  scheduleOpen.value = true
}

async function submitSchedule() {
  if (!scheduleForm.title.trim()) {
    message.warning('请填写标题')
    return
  }
  if (!scheduleForm.startTime) {
    message.warning('请选择开始时间')
    return
  }
  if (scheduleForm.startTime && scheduleForm.endTime && scheduleForm.startTime.isAfter(scheduleForm.endTime)) {
    message.warning('开始时间不能晚于结束时间')
    return
  }
  const collaboratorNames = staffOptions.value
    .filter((item) => scheduleForm.collaboratorIds.includes(item.value))
    .map((item) => item.label)
  const repeatCount = scheduleForm.recurring ? Math.max(1, Number(scheduleForm.recurrenceCount || 1)) : 1
  const repeatRule = scheduleForm.recurrenceRule
  const repeatInterval = Math.max(1, Number(scheduleForm.recurrenceInterval || 1))
  scheduleSaving.value = true
  try {
    if (editingScheduleId.value != null) {
      const updated = await updateOaTask(editingScheduleId.value, {
        title: scheduleForm.title.trim(),
        description: scheduleForm.description || undefined,
        startTime: dayjs(scheduleForm.startTime).format('YYYY-MM-DDTHH:mm:ss'),
        endTime: scheduleForm.endTime ? dayjs(scheduleForm.endTime).format('YYYY-MM-DDTHH:mm:ss') : undefined,
        priority: scheduleForm.priority,
        assigneeName: scheduleForm.assigneeName || undefined,
        calendarType: scheduleForm.calendarType,
        planCategory: scheduleForm.planCategory || undefined,
        urgency: scheduleForm.urgency,
        eventColor: scheduleForm.eventColor,
        collaboratorIds: scheduleForm.calendarType === 'COLLAB' ? scheduleForm.collaboratorIds : [],
        collaboratorNames: scheduleForm.calendarType === 'COLLAB' ? collaboratorNames : []
      })
      upsertCalendarTask(updated)
      scheduleOpen.value = false
      editingScheduleId.value = null
      message.success('日程已更新')
      return
    }
    const tasks: Promise<any>[] = []
    for (let i = 0; i < repeatCount; i += 1) {
      const startTime = i === 0 ? scheduleForm.startTime : addByRule(scheduleForm.startTime, repeatRule, repeatInterval * i)
      const endTime = scheduleForm.endTime ? (i === 0 ? scheduleForm.endTime : addByRule(scheduleForm.endTime, repeatRule, repeatInterval * i)) : undefined
      tasks.push(
        createOaTask({
          title: scheduleForm.title.trim(),
          description: scheduleForm.description || undefined,
          startTime: dayjs(startTime).format('YYYY-MM-DDTHH:mm:ss'),
          endTime: endTime ? dayjs(endTime).format('YYYY-MM-DDTHH:mm:ss') : undefined,
          priority: scheduleForm.priority,
          status: 'OPEN',
          assigneeName: scheduleForm.assigneeName || undefined,
          calendarType: scheduleForm.calendarType,
          planCategory: scheduleForm.planCategory || undefined,
          urgency: scheduleForm.urgency,
          eventColor: scheduleForm.eventColor,
          collaboratorIds: scheduleForm.calendarType === 'COLLAB' ? scheduleForm.collaboratorIds : [],
          collaboratorNames: scheduleForm.calendarType === 'COLLAB' ? collaboratorNames : [],
          recurring: scheduleForm.recurring,
          recurrenceRule: scheduleForm.recurring ? repeatRule : undefined,
          recurrenceInterval: scheduleForm.recurring ? repeatInterval : undefined,
          recurrenceCount: scheduleForm.recurring ? repeatCount : undefined
        })
      )
    }
    const createdRows = await Promise.all(tasks)
    createdRows.forEach((row) => {
      if (row?.id != null) upsertCalendarTask(row)
    })
    scheduleOpen.value = false
    editingScheduleId.value = null
    message.success(scheduleForm.recurring ? `已生成 ${tasks.length} 条周期日程` : '日程已新增')
  } finally {
    scheduleSaving.value = false
  }
}

function markScheduleDone(id: string | number) {
  Modal.confirm({
    title: '确认标记完成该日程？',
    onOk: async () => {
      const updated = await completeOaTask(id)
      if (updated?.id != null) {
        upsertCalendarTask(updated)
      } else {
        const target = calendarRows.value.find((item) => String(item.id) === String(id))
        if (target) {
          upsertCalendarTask({
            ...target,
            status: 'DONE'
          })
        }
      }
      message.success('已标记完成')
    }
  })
}

function removeSchedule(id: string | number) {
  Modal.confirm({
    title: '确认删除该日程？',
    okButtonProps: { danger: true },
    onOk: async () => {
      await deleteOaTask(id)
      removeCalendarTaskById(id)
      message.success('已删除')
    }
  })
}

async function loadStaffOptions() {
  try {
    await searchStaff('')
    staffDeptMap.value = {}
    staffOptions.value.forEach((item: any) => {
      staffDeptMap.value[String(item.value)] = item.departmentId == null ? undefined : String(item.departmentId)
    })
  } catch {}
}

async function loadDepartmentOptions() {
  try {
    await searchDepartments('')
  } catch {}
}

async function loadSummary() {
  const data = await getPortalSummary()
  Object.assign(summary, data)
}

async function loadCalendar() {
  calendarLoading.value = true
  try {
    calendarRows.value = await getOaTaskCalendar({
      startDate: dayjs().startOf('month').format('YYYY-MM-DD'),
      endDate: dayjs().endOf('month').format('YYYY-MM-DD')
    })
  } finally {
    calendarLoading.value = false
  }
}

async function loadDashboard() {
  dashboard.value = await getDashboardSummary()
}

async function loadFinanceOverview() {
  financeOverview.value = await getFinanceWorkbenchOverview()
}

async function loadBedAndElderStatus() {
  const [residence, beds] = await Promise.all([getResidenceStatusSummary(), getBedMap()])
  bedAndElderStatus.inHospital = Number(residence.inHospitalCount || 0)
  bedAndElderStatus.outing = Number(residence.outingCount || 0)
  bedAndElderStatus.medicalObservation = Number(residence.medicalOutingCount || 0)
  bedAndElderStatus.emptyBeds = (beds || []).filter((bed) => resolveBedStatus(bed as BedItem) === '空闲').length
  bedAndElderStatus.cleaningBeds = (beds || []).filter((bed) => resolveBedStatus(bed as BedItem) === '清洁中').length
}

async function loadSalesFunnel() {
  const today = dayjs().format('YYYY-MM-DD')
  const monthStart = dayjs().startOf('month').format('YYYY-MM-DD')

  const [conversion, evalPage, pendingSignPage] = await Promise.all([
    getMarketingConversionReport({ dateFrom: monthStart, dateTo: today }),
    getLeadPage({ pageNo: 1, pageSize: 1, status: 1 }) as Promise<PageResult<CrmLeadItem>>,
    getContractPage({ pageNo: 1, pageSize: 1, flowStage: 'PENDING_SIGN' }) as Promise<PageResult<CrmContractItem>>
  ])

  salesFunnel.todayConsultCount = Number(conversion.consultCount || 0)
  salesFunnel.evaluationCount = Number(evalPage.total || 0)
  salesFunnel.pendingSignCount = Number(pendingSignPage.total || 0)
  salesFunnel.monthDealCount = Number(conversion.contractCount || 0)
}

async function loadHrReminder() {
  const [hrData, certPage] = await Promise.all([
    getHrWorkbenchSummary(),
    getHrProfileCertificateReminderPage({ pageNo: 1, pageSize: 1 })
  ])
  hrSummary.value = hrData
  certificateReminderCount.value = Number(certPage.total || 0)
}

async function refreshPortalModules(withCalendar = true) {
  const jobs: Array<Promise<any>> = [
    loadSummary(),
    loadDashboard(),
    loadFinanceOverview(),
    loadSalesFunnel(),
    loadBedAndElderStatus(),
    loadHrReminder()
  ]
  if (withCalendar) {
    jobs.push(loadCalendar())
  }
  await Promise.allSettled(jobs)
  refreshedAt.value = dayjs().format('YYYY-MM-DD HH:mm:ss')
}

async function init() {
  loading.value = true
  pageError.value = ''
  try {
    if (!searchIndex.value.length) {
      buildSearchIndex()
      loadRecentSearchRoutes()
    }
    updateCustomCardRouteOptions('')
    loadModuleCustomize()
    loadCustomCards()
    updateSearchOptions(searchKeyword.value)
    await Promise.all([loadStaffOptions(), loadDepartmentOptions()])
    await refreshPortalModules(true)
  } catch (error: any) {
    pageError.value = error?.message || '加载首页失败'
    message.error(pageError.value)
  } finally {
    loading.value = false
  }
}

useLiveSyncRefresh({
  topics: ['elder', 'bed', 'lifecycle', 'finance', 'care', 'health', 'dining', 'marketing', 'oa', 'hr', 'logistics', 'system'],
  refresh: () => {
    refreshPortalModules(true).catch(() => {})
  },
  debounceMs: 800
})

onMounted(() => {
  init()
  if (portalSyncTimer) window.clearInterval(portalSyncTimer)
  portalSyncTimer = window.setInterval(() => {
    if (document.hidden) return
    refreshPortalModules(false).catch(() => {})
  }, 45 * 1000)
  portalVisibleHandler = () => {
    if (!document.hidden) refreshPortalModules(false).catch(() => {})
  }
  document.addEventListener('visibilitychange', portalVisibleHandler)
  window.addEventListener('mousemove', onCustomCardResizeMove)
  window.addEventListener('mouseup', onCustomCardResizeEnd)
  window.addEventListener('mousemove', onModuleCardResizeMove)
  window.addEventListener('mouseup', onModuleCardResizeEnd)
  window.addEventListener('mouseup', onPortalModuleHandleRelease)
})

onBeforeUnmount(() => {
  if (portalSyncTimer) window.clearInterval(portalSyncTimer)
  if (portalVisibleHandler) document.removeEventListener('visibilitychange', portalVisibleHandler)
  portalVisibleHandler = null
  window.removeEventListener('mousemove', onCustomCardResizeMove)
  window.removeEventListener('mouseup', onCustomCardResizeEnd)
  window.removeEventListener('mousemove', onModuleCardResizeMove)
  window.removeEventListener('mouseup', onModuleCardResizeEnd)
  window.removeEventListener('mouseup', onPortalModuleHandleRelease)
  resetPortalModuleDragState()
  document.body.classList.remove('resizing-active')
  hideResizeIndicator()
})
</script>

<style scoped>
.portal-page {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 2px;
}

.hero-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  border-radius: 14px;
  background: linear-gradient(135deg, #eef6ff 0%, #f8fbff 55%, #ffffff 100%);
  border: 1px solid #dbeafe;
}

.hero-left {
  flex: 1;
  min-width: 0;
}

.hero-title {
  font-size: 20px;
  font-weight: 700;
  color: #0f172a;
}

.hero-subtitle {
  margin-top: 6px;
  font-size: 13px;
  color: #475569;
}

.hero-kpis {
  margin-top: 10px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.hero-kpi-item {
  min-width: 92px;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 6px 10px;
}

.hero-kpi-label {
  display: block;
  color: #64748b;
  font-size: 12px;
}

.hero-kpi-value {
  color: #0f172a;
  font-size: 18px;
}

.hero-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

.hero-actions-tip {
  font-size: 12px;
  color: #64748b;
}

.hero-search {
  margin-top: 10px;
}

.section-row {
  margin-top: 2px;
}

.module-card {
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  transition: box-shadow 0.2s ease, transform 0.2s ease, border-color 0.2s ease;
  position: relative;
  overflow: hidden;
}

.module-card:hover {
  border-color: #bfdbfe;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.08);
}

.module-card.is-drop-zone {
  border-color: #93c5fd;
}

.module-card.is-dragging-source {
  opacity: 0.6;
  box-shadow: 0 16px 38px rgba(59, 130, 246, 0.28);
}

.module-card.is-drop-target::after {
  content: '';
  position: absolute;
  inset: 8px;
  border-radius: 10px;
  border: 2px dashed #3b82f6;
  background: rgba(59, 130, 246, 0.06);
  pointer-events: none;
}

.module-card.is-invalid-bounce {
  animation: module-invalid-bounce 0.24s ease;
}

@keyframes module-invalid-bounce {
  0% { transform: translateX(0); }
  35% { transform: translateX(-7px); }
  70% { transform: translateX(5px); }
  100% { transform: translateX(0); }
}

.module-drag-handle {
  width: 26px;
  min-width: 26px;
  height: 24px;
  border-radius: 6px;
  border: 1px dashed #bfdbfe;
  color: #64748b;
  background: #f8fbff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: grab;
  user-select: none;
  transition: all 0.16s ease;
}

.module-drag-handle:hover {
  border-color: #60a5fa;
  color: #1d4ed8;
}

.module-drag-handle.is-ready {
  border-style: solid;
  border-color: #2563eb;
  background: #dbeafe;
  color: #1e3a8a;
}

.module-drag-handle:active {
  cursor: grabbing;
}

.module-resize-handle {
  position: absolute;
  z-index: 3;
  opacity: 0;
  transition: opacity 0.16s ease;
}

.module-card:hover .module-resize-handle {
  opacity: 1;
}

.module-resize-right {
  top: 14px;
  right: 0;
  width: 10px;
  height: calc(100% - 26px);
  cursor: ew-resize;
}

.module-resize-bottom {
  left: 14px;
  bottom: 0;
  width: calc(100% - 26px);
  height: 10px;
  cursor: ns-resize;
}

.module-resize-corner {
  right: 0;
  bottom: 0;
  width: 14px;
  height: 14px;
  cursor: nwse-resize;
  background:
    linear-gradient(135deg, transparent 0 46%, #93c5fd 46% 54%, transparent 54% 100%);
}

.full-height {
  height: 100%;
}

.metric-cell {
  background: linear-gradient(180deg, #ffffff 0%, #f8fbff 100%);
  border: 1px solid #dbeafe;
  border-radius: 10px;
  padding: 12px;
  cursor: pointer;
}

.metric-cell:hover {
  border-color: #60a5fa;
  background: #f0f7ff;
}

.metric-title {
  font-size: 12px;
  color: #64748b;
}

.metric-value {
  margin-top: 6px;
  font-size: 18px;
  font-weight: 700;
  color: #0f172a;
  word-break: break-all;
}

.block-label {
  font-size: 12px;
  color: #64748b;
}

.source-wrap {
  margin-top: 12px;
}

.legend-line {
  margin-top: 8px;
}

.reminder-count {
  min-width: 42px;
  text-align: right;
  color: #475569;
}

.quick-group {
  border: 1px dashed #bfdbfe;
  border-radius: 10px;
  background: #f8fbff;
  padding: 12px;
  height: 100%;
}

.quick-title {
  margin-bottom: 8px;
  font-size: 13px;
  font-weight: 600;
  color: #1e3a8a;
}

.funnel-chart {
  height: 210px;
  margin-top: 8px;
}

.expense-block {
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 12px;
  background: #fcfdff;
}

.expense-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 0;
  border-bottom: 1px dashed #e2e8f0;
  cursor: pointer;
}

.expense-row:last-child {
  border-bottom: none;
}

.expense-row strong {
  color: #0f172a;
}

.custom-card-item {
  border: 1px solid #dbeafe;
  border-top: 3px solid #1677ff;
  border-radius: 12px;
  background: #f8fbff;
  padding: 12px;
  cursor: pointer;
  height: 100%;
  position: relative;
  overflow: hidden;
}

.custom-card-item:hover {
  border-color: #91caff;
  background: #eef6ff;
}

.custom-card-title {
  font-weight: 700;
  color: #1e3a8a;
}

.custom-card-route {
  margin-top: 4px;
  font-size: 12px;
  color: #475569;
  word-break: break-all;
}

.custom-card-desc {
  margin-top: 6px;
  font-size: 12px;
  color: #64748b;
}

.custom-card-actions {
  margin-top: 6px;
  position: relative;
  z-index: 2;
}

.custom-card-mode {
  margin-top: 6px;
}

.custom-card-category {
  margin-top: 6px;
  margin-left: 6px;
}

.resize-handle {
  position: absolute;
  z-index: 3;
  opacity: 0;
  transition: opacity 0.16s ease;
}

.custom-card-item:hover .resize-handle {
  opacity: 1;
}

.resize-right {
  top: 10px;
  right: 0;
  width: 10px;
  height: calc(100% - 22px);
  cursor: ew-resize;
}

.resize-bottom {
  left: 10px;
  bottom: 0;
  width: calc(100% - 22px);
  height: 10px;
  cursor: ns-resize;
}

.resize-corner {
  right: 0;
  bottom: 0;
  width: 14px;
  height: 14px;
  cursor: nwse-resize;
  background:
    linear-gradient(135deg, transparent 0 46%, #93c5fd 46% 54%, transparent 54% 100%);
}

.template-tag {
  cursor: pointer;
}

.manage-card-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.manage-card-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 10px;
  background: #fff;
}

.manage-card-item:hover {
  border-color: #91caff;
}

.manage-card-item.is-dragging {
  border-style: dashed;
  border-color: #2563eb;
  box-shadow: 0 6px 16px rgba(37, 99, 235, 0.16);
  background: #eff6ff;
}

.manage-card-main {
  min-width: 0;
  margin-right: 10px;
}

.manage-card-title {
  font-weight: 600;
  color: #1e293b;
}

.manage-card-desc {
  margin-top: 2px;
  font-size: 12px;
  color: #64748b;
  word-break: break-all;
}

.hint-text {
  color: #64748b;
  font-size: 12px;
}

:global(body.resizing-active) {
  user-select: none;
  cursor: nwse-resize;
}

.resize-indicator {
  position: fixed;
  z-index: 9999;
  pointer-events: none;
  background: rgba(15, 23, 42, 0.9);
  color: #fff;
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 12px;
  line-height: 1.2;
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.24);
}

.calendar-toolbar {
  margin-bottom: 10px;
}

.calendar-title-tip {
  color: #64748b;
  font-size: 12px;
  margin-bottom: 6px;
}

.legend-dot {
  width: 10px;
  height: 10px;
  display: inline-block;
  border-radius: 50%;
  margin-right: 4px;
}

.calendar-actions {
  margin-top: 8px;
}

.repeat-card {
  margin-bottom: 12px;
}

.agenda-item-done {
  opacity: 0.72;
}

.agenda-title-done {
  color: #64748b;
  text-decoration: line-through;
}

@media (max-width: 768px) {
  .hero-card {
    flex-direction: column;
    align-items: flex-start;
  }

  .hero-actions {
    width: 100%;
    align-items: flex-start;
  }

  .hero-kpi-item {
    min-width: 84px;
  }
}
</style>
