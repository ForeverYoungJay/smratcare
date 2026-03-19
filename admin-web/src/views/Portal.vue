<template>
  <PageContainer title="智慧养老首页" sub-title="待办优先、风险预警、运营与协同一屏联动">
    <StatefulBlock :loading="loading" :error="pageError" @retry="init">
      <div class="portal-page">
        <a-card :bordered="false" class="card-elevated hero-card">
          <div class="hero-left">
            <div class="hero-title">您好，{{ userStore.staffInfo?.realName || '管理员' }}</div>
            <div class="hero-subtitle">当前时间 {{ refreshedAt || '--' }}，优先处理“我的待办”可显著降低超时风险。</div>
            <div class="hero-kpis">
              <div class="hero-kpi-item clickable" @click="go('/oa/todo')">
                <span class="hero-kpi-label">待办总量</span>
                <strong class="hero-kpi-value">{{ totalTodoCount }}</strong>
              </div>
              <div class="hero-kpi-item clickable" @click="focusReminderCenter">
                <span class="hero-kpi-label">风险提醒</span>
                <strong class="hero-kpi-value">{{ activeRiskCount }}</strong>
              </div>
              <div class="hero-kpi-item clickable" @click="agendaDrawerOpen = true">
                <span class="hero-kpi-label">今日日程</span>
                <strong class="hero-kpi-value">{{ todayAgenda.length }}</strong>
              </div>
            </div>
            <div class="hero-search">
              <a-auto-complete
                ref="globalSearchRef"
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
              <div class="hero-search-hint">快捷键：/ 或 Ctrl+K 聚焦搜索，Esc 清空关键词</div>
            </div>
          </div>
          <div class="hero-actions">
            <div class="hero-actions-main">
              <a-space wrap class="hero-action-btns">
                <a-button type="primary" @click="go('/oa/todo')">进入待办中心</a-button>
                <a-button @click="init">刷新首页</a-button>
                <a-button @click="openModuleCustomize">自定义首页</a-button>
                <a-button @click="openCustomCardEditor()">新增卡面</a-button>
              </a-space>
            </div>
            <div class="hero-actions-meta">
              <a-space size="small" class="hero-actions-tip-row">
                <a-tag :color="syncingNow ? 'processing' : 'default'">{{ syncingNow ? '同步中' : '已同步' }}</a-tag>
                <span class="hero-actions-tip">最近同步：{{ refreshedAt || '--' }} · {{ lastSyncSource }}</span>
                <a-switch v-model:checked="autoSyncEnabled" checked-children="自动刷新开" un-checked-children="自动刷新关" />
              </a-space>
            </div>
          </div>
        </a-card>

        <a-row :gutter="[8, 8]" v-if="showRowTodoReminder" class="section-row" :style="{ order: sectionOrder(['todo', 'reminder']) }">
          <a-col :xs="24" :xl="rowTodoReminderSpan('todo')" :style="{ order: moduleOrder('todo') }">
            <a-card
              v-if="isModuleVisible('todo')"
              :bordered="false"
              class="card-elevated full-height module-card todo-focus-card"
              :style="moduleCardStyle('todo')"
              title="1️⃣ 我的待办（最重要）"
            >
              <template #extra>
                <a-space>
                  <a-tag color="processing">总待办 {{ totalTodoCount }}</a-tag>
                  <a-button type="link" size="small" @click="openApprovalOpinionPanel">审批意见</a-button>
                  <a-button type="link" size="small" @click="urgeAllPendingApprovals">一键催办</a-button>
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
              <a-space wrap style="margin-top: 12px;">
                <a-button type="primary" @click="go('/oa/todo')">查看全部</a-button>
                <a-button @click="go('/oa/approval')">批量审批</a-button>
                <a-button @click="go('/logistics/task-center')">打开任务中心</a-button>
              </a-space>
              <a-card size="small" class="approval-track-card" style="margin-top: 10px;">
                <template #title>审批流程跟踪（含财务报销）</template>
                <a-list size="small" :data-source="approvalTrackList" :locale="{ emptyText: '暂无审批流程' }">
                  <template #renderItem="{ item }">
                    <a-list-item>
                      <a-list-item-meta :description="`当前：${item.currentStep} · 下一步：${item.nextStep}${item.currentApprover ? ` · 审批人：${item.currentApprover}` : ''} · 催办${item.urgeCount}次`">
                        <template #title>
                          <a-space size="small">
                            <span>{{ item.title }}</span>
                            <a-tag :color="item.statusColor">{{ item.statusText }}</a-tag>
                          </a-space>
                        </template>
                      </a-list-item-meta>
                      <template #actions>
                        <a-button type="link" size="small" @click="go(`/oa/approval?keyword=${encodeURIComponent(item.title)}`)">查看流程</a-button>
                        <a-button v-if="item.status === 'PENDING'" type="link" size="small" @click="urgeApproval(item)">催进下一步</a-button>
                      </template>
                    </a-list-item>
                  </template>
                </a-list>
              </a-card>
            </a-card>
          </a-col>

          <a-col :xs="24" :xl="rowTodoReminderSpan('reminder')" :style="{ order: moduleOrder('reminder') }">
            <a-card
              v-if="isModuleVisible('reminder')"
              ref="reminderCardRef"
              :bordered="false"
              class="card-elevated full-height module-card reminder-card"
              :style="moduleCardStyle('reminder')"
              title="2️⃣ 提醒中心（系统预警）"
            >
              <template #extra>
                <div class="reminder-extra">
                  <a-radio-group v-model:value="reminderViewMode" size="small" button-style="solid">
                    <a-radio-button value="all">全部</a-radio-button>
                    <a-radio-button value="unread">未读 {{ unreadReminderCount }}</a-radio-button>
                    <a-radio-button value="pinned">置顶</a-radio-button>
                    <a-radio-button value="urgent">紧急 {{ urgentReminderCount }}</a-radio-button>
                  </a-radio-group>
                  <a-space size="small" wrap>
                    <a-tag color="purple">阈值 {{ thresholdConfig.configVersion || '--' }}</a-tag>
                    <a-button size="small" @click="openThresholdConfigModal">阈值设置</a-button>
                    <a-button size="small" @click="markAllReminderRead">全部已读</a-button>
                  </a-space>
                </div>
              </template>
              <a-list size="small" :data-source="displayRiskReminders" :locale="{ emptyText: '暂无提醒' }">
                <template #renderItem="{ item }">
                  <a-list-item :class="{ 'reminder-unread': !isReminderRead(item) }">
                    <a-space>
                      <a-tag :color="severityColor(item.level)">{{ item.level }}</a-tag>
                      <span>{{ item.title }}</span>
                      <span v-if="item.desc" class="hint-text">({{ item.desc }})</span>
                    </a-space>
                    <template #actions>
                      <span class="reminder-count">{{ item.count }}</span>
                      <a-button type="link" size="small" @click="toggleReminderPinned(item)">
                        {{ isReminderPinned(item) ? '取消置顶' : '置顶' }}
                      </a-button>
                      <a-button type="link" size="small" @click="toggleReminderRead(item)">
                        {{ isReminderRead(item) ? '标未读' : '已读' }}
                      </a-button>
                      <a-button type="link" size="small" @click="goRiskItem(item)">处理</a-button>
                    </template>
                  </a-list-item>
                </template>
              </a-list>
              <div class="legend-line">
                <a-tag color="red">红色：紧急</a-tag>
                <a-tag color="orange">橙色：预警</a-tag>
                <a-tag color="blue">蓝色：普通通知</a-tag>
              </div>
              <div class="hint-text" style="margin-top: 6px;">
                当前阈值：异常≥{{ thresholdConfig.abnormalTaskThreshold }}；库存≥{{ thresholdConfig.inventoryAlertThreshold }}；
                床位≥{{ thresholdConfig.bedOccupancyThreshold }}%；收入环比≤-{{ thresholdConfig.revenueDropThreshold }}%
              </div>
            </a-card>
          </a-col>
        </a-row>

        <a-card
          v-if="isModuleVisible('birthdayPlan')"
          :bordered="false"
          class="card-elevated module-card"
          :style="[moduleCardStyle('birthdayPlan'), { order: sectionOrder(['birthdayPlan']) }]"
          title="🎂 老人院内生日计划"
        >
          <template #extra>
            <a-space>
              <a-button size="small" @click="go('/oa/life/birthday')">查看更多</a-button>
              <a-button size="small" @click="exportBirthdayCsv">导出</a-button>
              <a-button size="small" @click="printBirthdayYearPlan">打印年度表</a-button>
              <a-button size="small" @click="syncBirthdayTasks">同步生日提醒到日历</a-button>
            </a-space>
          </template>
          <a-row :gutter="[8, 8]">
            <a-col :xs="24" :md="8">
              <div class="metric-cell" @click="openBirthdayListByDays(0)">
                <div class="metric-title">今日生日</div>
                <div class="metric-value">{{ birthdayStats.today }}</div>
              </div>
            </a-col>
            <a-col :xs="24" :md="8">
              <div class="metric-cell" @click="openBirthdayListByDays(7)">
                <div class="metric-title">未来7天</div>
                <div class="metric-value">{{ birthdayStats.next7Days }}</div>
              </div>
            </a-col>
            <a-col :xs="24" :md="8">
              <div class="metric-cell" @click="openBirthdayListMonth">
                <div class="metric-title">本月生日</div>
                <div class="metric-value">{{ birthdayStats.thisMonth }}</div>
              </div>
            </a-col>
          </a-row>
          <a-alert
            v-if="birthdayStats.today > 0"
            style="margin-top: 10px;"
            type="warning"
            show-icon
            :message="`今日生日：${birthdayStats.today} 人`"
          />
          <div class="birthday-filter-bar">
            <a-space wrap>
              <span class="hint-text">年龄分层：</span>
              <a-radio-group v-model:value="birthdayAgeBand" size="small" button-style="solid">
                <a-radio-button value="ALL">全部</a-radio-button>
                <a-radio-button value="80_PLUS">80+</a-radio-button>
                <a-radio-button value="90_PLUS">90+</a-radio-button>
                <a-radio-button value="UNDER_80">&lt;80</a-radio-button>
              </a-radio-group>
              <a-tag color="blue">筛选总人数 {{ birthdaySliceStats.filteredTotal }}</a-tag>
              <a-tag color="gold">今日 {{ birthdaySliceStats.filteredToday }}</a-tag>
              <a-tag color="purple">未来31天 {{ birthdaySliceStats.filteredMonth }}</a-tag>
            </a-space>
          </div>
          <a-list size="small" :data-source="birthdayUpcomingList" :locale="{ emptyText: '本月暂无生日' }" style="margin-top: 8px;">
            <template #renderItem="{ item }">
              <a-list-item>
                <a-space>
                  <a-tag color="purple">{{ item.nextBirthday || '--' }}</a-tag>
                  <span>{{ item.elderName }}</span>
                  <span class="hint-text">（{{ item.ageOnNextBirthday || '--' }}岁）</span>
                </a-space>
                <template #actions>
                  <a-button type="link" size="small" @click="openBirthdayElderDetail(item)">老人档案</a-button>
                  <a-button type="link" size="small" @click="openBirthdayActivityCreate(item)">创建本月集体活动</a-button>
                  <a-button type="link" size="small" @click="openBirthdayActivityList(item)">查看本月活动</a-button>
                  <a-button type="link" size="small" @click="openBirthdayMaterialPrepare(item)">本月物资准备</a-button>
                  <a-button type="link" size="small" @click="openBirthdayDoneRecord(item)">本月完成记录</a-button>
                </template>
              </a-list-item>
            </template>
          </a-list>
          <a-space wrap style="margin-top: 10px;">
            <a-button size="small" @click="openAgeReport('80+')">一键统计80+</a-button>
            <a-button size="small" @click="openAgeReport('90+')">一键统计90+</a-button>
            <a-button size="small" @click="openAgeReport('CUSTOM')">自定义年龄统计</a-button>
            <a-button size="small" @click="openBirthdayFrequencyCheck">高频生日筛查</a-button>
          </a-space>
        </a-card>

        <a-card
          v-if="isModuleVisible('quickLaunch')"
          :bordered="false"
          class="card-elevated module-card"
          :style="[moduleCardStyle('quickLaunch'), { order: sectionOrder(['quickLaunch']) }]"
          title="3️⃣ 快捷发起（操作入口）"
        >
          <a-row :gutter="[8, 8]">
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
        </a-card>

        <a-card
          v-if="isModuleVisible('customCards')"
          :bordered="false"
          class="card-elevated module-card"
          :style="[moduleCardStyle('customCards'), { order: sectionOrder(['customCards']) }]"
          title="🧩 我的自定义卡面"
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
            </a-space>
          </template>
          <a-row :gutter="[8, 8]">
            <a-col :xs="24" :sm="12" :xl="customCardColSpan" v-for="item in filteredCustomCards" :key="item.id">
              <div class="custom-card-item" :style="customCardItemStyle(item)" @click="handleCustomCardClick(item)">
                <div class="custom-card-title">{{ item.icon || '🧩' }} {{ item.title }}</div>
                <div class="custom-card-route">{{ item.route }}</div>
                <div class="custom-card-desc">{{ item.description || '点击快速进入' }}</div>
                <a-tag class="custom-card-mode" size="small">{{ item.openMode === 'new' ? '新标签打开' : '当前页打开' }}</a-tag>
                <a-tag class="custom-card-category" size="small" color="blue">{{ customCardCategoryText(item.category) }}</a-tag>
                <a-tag class="custom-card-category" size="small" color="purple">{{ audienceText(item.audience) }}</a-tag>
                <a-tag v-if="item.visibleStaffIds?.length" class="custom-card-category" size="small" color="gold">
                  指定账号 {{ item.visibleStaffIds.length }} 人
                </a-tag>
                <div class="custom-card-actions" @click.stop>
                  <a-button type="link" size="small" @click="openCustomCardEditor(item.id)">编辑</a-button>
                  <a-button type="link" size="small" @click="duplicateCustomCard(item.id)">复制</a-button>
                  <a-button type="link" danger size="small" @click="removeCustomCard(item.id)">删除</a-button>
                </div>
              </div>
            </a-col>
            <a-col v-if="!filteredCustomCards.length" :span="24">
              <a-empty description="暂无自定义卡面，可点击右上角“新增卡面”" />
            </a-col>
          </a-row>
        </a-card>

        <a-row :gutter="[8, 8]" v-if="showRowOperation" class="section-row" :style="{ order: sectionOrder(['operation', 'finance', 'salesFunnel']) }">
          <a-col :xs="24" :xl="rowOperationSpan('operation')" :style="{ order: moduleOrder('operation') }">
            <a-card
              v-if="isModuleVisible('operation')"
              :bordered="false"
              class="card-elevated full-height module-card"
              :style="moduleCardStyle('operation')"
              title="4️⃣ 今日运营概览（核心KPI）"
            >
              <a-row :gutter="[10, 10]">
                <a-col :span="12" v-for="item in operationOverview" :key="item.title">
                  <div class="metric-cell" @click="item.route ? go(item.route) : undefined">
                    <div class="metric-title">{{ item.title }}</div>
                    <div class="metric-value">{{ item.value }}</div>
                  </div>
                </a-col>
              </a-row>
            </a-card>
          </a-col>

          <a-col :xs="24" :xl="rowOperationSpan('finance')" :style="{ order: moduleOrder('finance') }">
            <a-card
              v-if="isModuleVisible('finance')"
              :bordered="false"
              class="card-elevated full-height module-card"
              :style="moduleCardStyle('finance')"
              title="5️⃣ 财务运营概览"
            >
              <a-row :gutter="[10, 10]">
                <a-col :span="12" v-for="item in financeOverviewItems" :key="item.title">
                  <div class="metric-cell" @click="item.route ? go(item.route) : undefined">
                    <div class="metric-title">{{ item.title }}</div>
                    <div class="metric-value">{{ item.value }}</div>
                  </div>
                </a-col>
              </a-row>
              <a-button type="link" style="padding-left: 0; margin-top: 8px;" @click="go('/finance/reports/overall')">点击 → 财务分析</a-button>
            </a-card>
          </a-col>

          <a-col :xs="24" :xl="rowOperationSpan('salesFunnel')" :style="{ order: moduleOrder('salesFunnel') }">
            <a-card
              v-if="isModuleVisible('salesFunnel')"
              :bordered="false"
              class="card-elevated full-height module-card"
              :style="moduleCardStyle('salesFunnel')"
              title="6️⃣ 销售运营漏斗"
            >
              <a-row :gutter="[10, 10]">
                <a-col :span="12" v-for="item in salesFunnelItems" :key="item.title">
                  <div class="metric-cell" @click="go(item.route)">
                    <div class="metric-title">{{ item.title }}</div>
                    <div class="metric-value">{{ item.value }}</div>
                  </div>
                </a-col>
              </a-row>
              <v-chart :option="funnelOption" autoresize class="funnel-chart" />
            </a-card>
          </a-col>
        </a-row>

        <a-row :gutter="[8, 8]" v-if="showRowStatusExpense" class="section-row" :style="{ order: sectionOrder(['bedStatus', 'expense']) }">
          <a-col :xs="24" :xl="rowStatusExpenseSpan('bedStatus')" :style="{ order: moduleOrder('bedStatus') }">
            <a-card
              v-if="isModuleVisible('bedStatus')"
              :bordered="false"
              class="card-elevated full-height module-card"
              :style="moduleCardStyle('bedStatus')"
              title="7️⃣ 床位与长者状态"
            >
              <a-row :gutter="[10, 10]">
                <a-col :span="8" v-for="item in bedAndElderStatusItems" :key="item.title">
                  <div class="metric-cell" @click="go(item.route)">
                    <div class="metric-title">{{ item.title }}</div>
                    <div class="metric-value">{{ item.value }}</div>
                  </div>
                </a-col>
              </a-row>
              <a-button type="link" style="padding-left: 0; margin-top: 8px;" @click="go('/elder/bed-panorama')">点击 → 床态全景</a-button>
            </a-card>
          </a-col>

          <a-col :xs="24" :xl="rowStatusExpenseSpan('expense')" :style="{ order: moduleOrder('expense') }">
            <a-card
              v-if="isModuleVisible('expense')"
              :bordered="false"
              class="card-elevated full-height module-card"
              :style="moduleCardStyle('expense')"
              title="8️⃣ 费用管理（我的费用 / 部门费用 / 发票夹）"
            >
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
            </a-card>
          </a-col>
        </a-row>

        <a-card
          v-if="isModuleVisible('calendar')"
          :bordered="false"
          class="card-elevated module-card calendar-card"
          :style="[moduleCardStyle('calendar'), { order: sectionOrder(['calendar']) }]"
          title="9️⃣ 行政日历 / 协同日历（统一页面）"
        >
          <template #extra>
            <a-space>
              <a-button size="small" @click="openCreateSchedule()">创建日程</a-button>
              <a-button size="small" @click="go('/oa/work-execution/calendar')">打开统一日历页面</a-button>
              <a-button size="small" @click="agendaDrawerOpen = true">今日/明日速览</a-button>
              <a-button size="small" @click="go('/oa/attendance-leave?type=LEAVE&quick=1')">发起请假</a-button>
              <a-button size="small" @click="go('/oa/approval?type=LEAVE')">请假审批流程</a-button>
            </a-space>
          </template>
          <div class="calendar-toolbar">
            <a-space size="small" wrap class="calendar-insights">
              <a-tag v-for="item in calendarInsights" :key="item.label" :color="item.color">{{ item.label }} {{ item.value }}</a-tag>
            </a-space>
            <a-space size="small" wrap class="calendar-loads" v-if="calendarLoadLeaders.length">
              <a-tag color="geekblue">负责人负载</a-tag>
              <a-tag v-for="item in calendarLoadLeaders" :key="item.name">{{ item.name }} {{ item.count }}条</a-tag>
            </a-space>
            <div class="calendar-week-trend" v-if="calendarWeeklyLoad.length">
              <span class="hint-text">本周负载：</span>
              <div
                v-for="item in calendarWeeklyLoad"
                :key="item.day"
                class="calendar-week-bar"
                :title="`${item.label} ${item.count}条`"
                :style="{ height: `${Math.max(18, item.height)}px` }"
              >
                <span>{{ item.label }}</span>
              </div>
            </div>
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
              <a-button size="small" type="link" @click="showAllCalendarTypes">显示全部</a-button>
            </a-space>
          </div>
          <StatefulBlock :loading="calendarLoading" :error="''" :empty="!calendarRows.length" empty-text="暂无日程安排" @retry="loadCalendar">
            <FullCalendar ref="calendarRef" :options="calendarOptions" />
          </StatefulBlock>
          <div class="calendar-actions">
            <a-space wrap>
              <a-button type="link" @click="go('/oa/work-execution/calendar')">进入统一日历完整视图</a-button>
              <a-button type="link" @click="go('/oa/work-execution/task')">查看工作执行任务</a-button>
              <a-button type="link" @click="go('/oa/approval?type=LEAVE')">请假审批看板</a-button>
            </a-space>
          </div>
        </a-card>

        <a-card
          v-if="isModuleVisible('dataEntry')"
          :bordered="false"
          class="card-elevated module-card"
          :style="[moduleCardStyle('dataEntry'), { order: sectionOrder(['dataEntry']) }]"
          title="🔟 数据分析入口"
        >
          <a-space wrap style="margin-top: 8px;">
            <a-button type="primary"  @click="go('/stats/org/monthly-operation')">运营分析</a-button>
            <a-button type="primary"  @click="go('/finance/reports/overall')">财务分析</a-button>
            <a-button type="primary"  @click="go('/medical-care/nursing-quality')">医护质量</a-button>
            <a-button type="primary"  @click="go('/marketing/reports/conversion')">销售分析</a-button>
          </a-space>
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
              <a-select v-model:value="scheduleForm.calendarType" :options="calendarTypeOptions" @change="onScheduleCalendarTypeChange" />
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
            <a-form-item label="颜色策略">
              <a-input :value="calendarTypeText(scheduleForm.calendarType)" disabled />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="12">
          <a-col :span="24">
            <a-form-item label="冲突策略">
              <a-segmented v-model:value="conflictPolicy" :options="conflictPolicyOptions" block />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="12">
          <a-col :span="24">
            <a-space wrap>
              <a-button :loading="conflictPreviewLoading" @click="previewScheduleConflicts">立即预检冲突</a-button>
              <span class="hint-text">保存前可先预检，策略生效：{{ conflictPolicyOptions.find((item) => item.value === conflictPolicy)?.label }}</span>
            </a-space>
          </a-col>
        </a-row>
        <a-row :gutter="12" v-if="conflictPreviewItems.length">
          <a-col :span="24">
            <a-alert :message="`预检发现 ${conflictPreviewItems.length} 条冲突`" type="warning" show-icon>
              <template #description>
                <div v-for="(item, index) in conflictPreviewItems.slice(0, 5)" :key="`${item.title}_${index}`">
                  {{ index + 1 }}. {{ item.title || '未命名日程' }}（{{ conflictItemTimeText(item) }}）{{ item.reason ? ` - ${item.reason}` : '' }}
                  <a-button type="link" size="small" @click="focusConflictDate(item.startTime)">定位</a-button>
                </div>
              </template>
            </a-alert>
          </a-col>
        </a-row>
        <a-row :gutter="12" v-if="scheduleForm.calendarType === 'COLLAB'">
          <a-col :span="24">
            <a-form-item label="协同部门（可多选）">
              <a-select
                v-model:value="scheduleForm.collaboratorDeptIds"
                mode="multiple"
                allow-clear
                show-search
                :filter-option="false"
                :options="departmentOptions"
                placeholder="可选择多个部门，系统会自动同步到这些部门成员的协同日历"
                @search="searchDepartments"
                @focus="() => !departmentOptions.length && searchDepartments('')"
              />
            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item label="协同成员">
              <a-select
                v-model:value="scheduleForm.collaboratorIds"
                mode="multiple"
                allow-clear
                show-search
                :filter-option="false"
                :options="staffOptions"
                placeholder="可直接选择某些员工；与协同部门可同时选择"
                @search="searchStaff"
                @focus="() => !staffOptions.length && searchStaff('')"
              />
            </a-form-item>
          </a-col>
          <a-col :span="24" v-if="collaboratorTip">
            <div class="hint-text">
              {{ collaboratorTip }}
              <a-button v-if="collaboratorPreviewAllNames.length > collaboratorPreviewNames.length" type="link" size="small" @click="showAllCollaboratorPreview">
                查看全部
              </a-button>
            </div>
          </a-col>
          <a-col :span="24" v-if="collaboratorDeltaTip">
            <div class="hint-text">{{ collaboratorDeltaTip }}</div>
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
                    <a-tag v-if="item.readonly">系统提醒</a-tag>
                  </a-space>
                </template>
              </a-list-item-meta>
              <template #actions>
                <a-button type="link" size="small" @click="focusCalendarDate(item.dateText)">定位</a-button>
                <a-button v-if="!item.readonly" type="link" size="small" @click="openEditSchedule(item.id)">编辑</a-button>
                <a-button v-if="!item.readonly" type="link" size="small" @click="markScheduleDone(item.id)">完成</a-button>
                <a-button v-if="!item.readonly" type="link" danger size="small" @click="removeSchedule(item.id)">删除</a-button>
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
                    <a-tag v-if="item.readonly">系统提醒</a-tag>
                  </a-space>
                </template>
              </a-list-item-meta>
              <template #actions>
                <a-button type="link" size="small" @click="focusCalendarDate(item.dateText)">定位</a-button>
                <a-button v-if="!item.readonly" type="link" size="small" @click="openEditSchedule(item.id)">编辑</a-button>
                <a-button v-if="!item.readonly" type="link" size="small" @click="markScheduleDone(item.id)">完成</a-button>
                <a-button v-if="!item.readonly" type="link" danger size="small" @click="removeSchedule(item.id)">删除</a-button>
              </template>
            </a-list-item>
          </template>
        </a-list>
      </a-card>
      <div class="calendar-actions">
        <a-space wrap>
          <a-button type="primary" @click="go('/oa/work-execution/calendar')">打开统一日历页面</a-button>
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
                  <a-tag v-if="item.readonly">系统提醒</a-tag>
                </a-space>
              </template>
            </a-list-item-meta>
            <template #actions>
              <a-button v-if="!item.readonly" type="link" size="small" @click="openEditSchedule(item.id)">编辑</a-button>
              <a-button v-if="!item.readonly" type="link" size="small" @click="markScheduleDone(item.id)">完成</a-button>
              <a-button v-if="!item.readonly" type="link" danger size="small" @click="removeSchedule(item.id)">删除</a-button>
            </template>
          </a-list-item>
        </template>
      </a-list>
      <div class="calendar-actions">
        <a-space wrap>
          <a-button type="primary" @click="go('/oa/work-execution/calendar')">打开统一日历页面</a-button>
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
        <a-form-item label="可见账号（可选）">
          <a-select
            v-model:value="customCardForm.visibleStaffIds"
            mode="multiple"
            allow-clear
            show-search
            :filter-option="false"
            :options="staffOptions"
            placeholder="不选则对可见角色全员开放；可按员工姓名筛选"
            @search="searchStaff"
            @focus="() => !staffOptions.length && searchStaff('')"
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

    <a-modal v-model:open="approvalOpinionOpen" title="审批意见窗口" width="760px" :footer="null">
      <a-list :data-source="approvalOpinionList" :locale="{ emptyText: '暂无院长审批意见' }">
        <template #renderItem="{ item }">
          <a-list-item>
            <a-list-item-meta :description="`${item.typeText} · ${item.timeText}`">
              <template #title>
                <a-space size="small">
                  <span>{{ item.title }}</span>
                  <a-tag color="green">已通过</a-tag>
                </a-space>
              </template>
            </a-list-item-meta>
            <template #actions>
              <div class="approval-opinion-text">{{ item.remark || '（暂无审批意见）' }}</div>
              <a-button type="link" size="small" @click="go(`/oa/approval?keyword=${encodeURIComponent(item.title)}`)">查看流程</a-button>
            </template>
          </a-list-item>
        </template>
      </a-list>
    </a-modal>

    <a-modal v-model:open="birthdayInsightOpen" :title="birthdayInsightTitle" width="640px" :footer="null">
      <a-list size="small" :data-source="birthdayInsightRows" :locale="{ emptyText: '暂无统计数据' }">
        <template #renderItem="{ item }">
          <a-list-item>
            <span>{{ item.label }}</span>
            <strong>{{ item.value }}</strong>
          </a-list-item>
        </template>
      </a-list>
    </a-modal>

    <a-modal
      v-model:open="thresholdConfigOpen"
      title="提醒中心阈值设置"
      @cancel="closeThresholdConfigModal"
    >
      <a-form layout="vertical">
        <a-form-item label="护理异常任务阈值（条）">
          <a-input-number v-model:value="thresholdDraft.abnormalTaskThreshold" :min="1" :max="999" style="width: 100%;" />
        </a-form-item>
        <a-form-item label="库存不足阈值（项）">
          <a-input-number v-model:value="thresholdDraft.inventoryAlertThreshold" :min="1" :max="9999" style="width: 100%;" />
        </a-form-item>
        <a-form-item label="床位高占用阈值（%）">
          <a-input-number v-model:value="thresholdDraft.bedOccupancyThreshold" :min="1" :max="100" style="width: 100%;" />
        </a-form-item>
        <a-form-item label="收入环比下滑阈值（%）">
          <a-input-number v-model:value="thresholdDraft.revenueDropThreshold" :min="1" :max="100" style="width: 100%;" />
        </a-form-item>
      </a-form>
      <div class="threshold-modal-preset">
        <span class="hint-text">快捷预设</span>
        <a-space wrap>
          <a-button
            v-for="preset in thresholdPresets"
            :key="preset.label"
            size="small"
            @click="applyThresholdPreset(preset.snapshot)"
          >
            {{ preset.label }}
          </a-button>
        </a-space>
      </div>
      <a-alert
        v-if="thresholdDraftDirty"
        type="warning"
        show-icon
        message="当前有未保存阈值修改，关闭弹窗将自动撤销。"
        style="margin: 8px 0;"
      />
      <div class="hint-text" style="margin-bottom: 6px;">草稿阈值命中预览</div>
      <ThresholdPreviewList :rows="thresholdDraftPreviewRows" />
      <a-divider style="margin: 8px 0;" />
      <div class="hint-text" style="margin-bottom: 6px;">最近阈值变更</div>
      <a-list size="small" :data-source="thresholdLogs.slice(0, 4)" :locale="{ emptyText: '暂无变更记录' }">
        <template #renderItem="{ item }">
          <a-list-item>
            <span class="hint-text">
              {{ dayjs(item.changedAt).format('YYYY-MM-DD HH:mm:ss') }}
              · {{ item.source === 'dashboard' ? '仪表盘' : '首页' }}
              · 异常≥{{ item.abnormalTaskThreshold }}
              · 库存≥{{ item.inventoryAlertThreshold }}
              · 床位≥{{ item.bedOccupancyThreshold }}%
              · 收入≤-{{ item.revenueDropThreshold }}%
            </span>
          </a-list-item>
        </template>
      </a-list>
      <template #footer>
        <a-space>
          <a-button @click="closeThresholdConfigModal">关闭</a-button>
          <a-button :disabled="!thresholdDraftDirty" @click="rollbackThresholdDraft">撤销草稿</a-button>
          <a-button @click="resetThresholdConfig">恢复默认</a-button>
          <a-button type="primary" :disabled="!thresholdDraftDirty" @click="saveThresholdConfig">保存配置</a-button>
        </a-space>
      </template>
    </a-modal>

  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
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
import ThresholdPreviewList from '../components/ThresholdPreviewList.vue'
import { useUserStore } from '../stores/user'
import {
  createOaTask,
  getOaTaskCalendar,
  getPortalSummary,
  getTodoSummary,
  getOaTaskSummary,
  getApprovalSummary,
  updateOaTask,
  completeOaTask,
  deleteOaTask,
  getApprovalPage,
  updateApproval,
  checkOaTaskConflicts
} from '../api/oa'
import { getBirthdayPage } from '../api/life'
import type { OaPortalSummary, OaTask, PageResult, BedItem, CrmContractItem, OaApproval, BirthdayReminder } from '../types'
import { useLiveSyncRefresh } from '../composables/useLiveSyncRefresh'
import {
  getDashboardSummary,
  getDashboardThresholdDefaults,
  type DashboardSummary,
  type DashboardThresholdDefaults
} from '../api/dashboard'
import { getFinanceWorkbenchOverview } from '../api/finance'
import type { FinanceWorkbenchOverview, HrWorkbenchSummary } from '../types'
import { getMarketingConversionReport, getContractPage } from '../api/marketing'
import { getResidenceStatusSummary } from '../api/elderResidence'
import { getBedMap } from '../api/bed'
import { getHrProfileCertificateReminderPage, getHrWorkbenchSummary } from '../api/hr'
import { getStaffPage } from '../api/rbac'
import { useDepartmentOptions } from '../composables/useDepartmentOptions'
import { useStaffOptions } from '../composables/useStaffOptions'
import {
  clearThresholdSnapshot,
  loadThresholdLogs,
  loadThresholdSnapshot,
  saveThresholdSnapshot,
  thresholdPulseKey,
  type ThresholdChangeLog,
  type ThresholdSnapshot
} from '../utils/dashboardThreshold'
import {
  buildThresholdQuery,
  DASHBOARD_THRESHOLD_PRESETS,
  mergeThresholdConfig,
  thresholdSnapshotsEqual,
  toThresholdSnapshot
} from '../utils/dashboardQuery'
import { buildThresholdPreviewRows } from '../utils/dashboardThresholdPreview'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const pageError = ref('')
const calendarLoading = ref(false)
const refreshedAt = ref('')
const scheduleOpen = ref(false)
const scheduleSaving = ref(false)
const collaboratorPreviewTotal = ref(0)
const collaboratorPreviewLoading = ref(false)
const collaboratorPreviewNames = ref<string[]>([])
const collaboratorPreviewAllNames = ref<string[]>([])
const originalCollaboratorIds = ref<string[]>([])
const conflictPreviewLoading = ref(false)
const conflictPreviewItems = ref<Array<{ title?: string; assigneeName?: string; startTime?: string; endTime?: string; reason?: string }>>([])
const calendarRows = ref<OaTask[]>([])
const calendarRef = ref<any>(null)
const agendaDrawerOpen = ref(false)
const editingScheduleId = ref<string | number | null>(null)
const dayEventDrawerOpen = ref(false)
const selectedCalendarDateText = ref(dayjs().format('YYYY-MM-DD'))
const reminderCardRef = ref<any>(null)
const globalSearchRef = ref<any>(null)
const { departmentOptions, searchDepartments } = useDepartmentOptions({ pageSize: 240, preloadSize: 500 })
const { staffOptions, searchStaff } = useStaffOptions({ pageSize: 300, preloadSize: 500 })
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
const exportPayloadOpen = ref(false)
const importPayloadOpen = ref(false)
const exportPayloadText = ref('')
const importPayloadText = ref('')
const customCardManageKeyword = ref('')
const approvalOpinionOpen = ref(false)
const approvalRows = ref<OaApproval[]>([])
const birthdayRows = ref<BirthdayReminder[]>([])
const birthdayInsightOpen = ref(false)
const birthdayInsightTitle = ref('生日统计')
const birthdayInsightRows = ref<Array<{ label: string; value: string }>>([])
const reminderViewMode = ref<'all' | 'unread' | 'pinned' | 'urgent'>('all')
const reminderState = ref<Record<string, { read?: boolean; pinned?: boolean; updatedAt?: string; lastCount?: number }>>({})
const thresholdConfigOpen = ref(false)
const thresholdLogs = ref<ThresholdChangeLog[]>([])
const birthdayAgeBand = ref<'ALL' | '80_PLUS' | '90_PLUS' | 'UNDER_80'>('ALL')
const autoSyncEnabled = ref(true)
const syncingNow = ref(false)
const lastSyncSource = ref('初始化')
let portalSyncTimer: number | undefined
let portalVisibleHandler: (() => void) | null = null
let calendarStorageHandler: ((event: StorageEvent) => void) | null = null
let portalHotkeyHandler: ((event: KeyboardEvent) => void) | null = null
const currentCalendarView = ref<'dayGridMonth' | 'dayGridWeek' | 'dayGridDay'>('dayGridMonth')
const conflictPolicy = ref<'WARN' | 'BLOCK' | 'ALLOW'>('WARN')

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
  | 'birthdayPlan'
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
  x?: number
  y?: number
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
  { key: 'birthdayPlan', title: '老人生日计划', desc: '生日提醒与活动安排', audience: ['ALL'] },
  { key: 'calendar', title: '行政日历/协同日历', desc: '日程、请假、协同安排', audience: ['ALL'] },
  { key: 'dataEntry', title: '数据分析入口', desc: '运营/财务/医护/销售分析入口', audience: ['ALL'] }
]

const moduleConfig = ref<PortalModuleConfigItem[]>(
  portalModuleCatalog.map((item, index) => ({ ...item, visible: true, order: index, width: undefined, height: undefined, x: 0, y: 0 }))
)
const moduleConfigDraft = ref<PortalModuleConfigItem[]>(
  portalModuleCatalog.map((item, index) => ({ ...item, visible: true, order: index, width: undefined, height: undefined, x: 0, y: 0 }))
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
  visibleStaffIds?: string[]
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
  { id: 'oa-calendar', title: '行政日历/协同日历', route: '/oa/work-execution/calendar', description: '查看个人/部门工作/日常/协同计划', themeColor: '#722ed1', openMode: 'current', icon: '📅', category: 'OA', visible: true, audience: ['ALL'], height: 168 }
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
  audience: ['ALL'] as AudienceCode[],
  visibleStaffIds: [] as string[]
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
  { key: 'calendar-full', title: '统一日历全景', route: '/oa/work-execution/calendar', description: '统一查看个人/部门工作/日常/协同日程', themeColor: '#2f54eb', openMode: 'current', icon: '🗓️', category: 'OA', audience: ['ALL'] }
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

const summary = reactive<OaPortalSummary>({
  notices: [],
  todos: [],
  workflowTodos: [],
  marketingChannels: [],
  collaborationGantt: [],
  latestSuggestions: [],
  pendingMedicalOrderCount: 0,
  elderContractExpiringCount: 0
})
const myTodoSummary = reactive({
  pendingApprovalCount: 0,
  openTodoCount: 0,
  timeoutApprovalCount: 0,
  ongoingTaskCount: 0
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
const defaultThresholdConfig: DashboardThresholdDefaults = {
  abnormalTaskThreshold: 3,
  inventoryAlertThreshold: 10,
  bedOccupancyThreshold: 95,
  revenueDropThreshold: 5,
  configVersion: '--'
}
const thresholdConfig = ref<DashboardThresholdDefaults>({ ...defaultThresholdConfig })
const thresholdDraft = ref<ThresholdSnapshot>(toThresholdSnapshot(defaultThresholdConfig))
const thresholdSavedSnapshot = ref<ThresholdSnapshot>(toThresholdSnapshot(defaultThresholdConfig))
const thresholdPresets = DASHBOARD_THRESHOLD_PRESETS
const thresholdDraftDirty = computed(() =>
  !thresholdSnapshotsEqual(thresholdDraft.value, thresholdSavedSnapshot.value)
)
const thresholdDraftPreviewRows = computed(() =>
  buildThresholdPreviewRows(
    {
      abnormalCount: Number(dashboard.value.abnormalTasksToday || 0),
      inventoryCount: Number(summary.inventoryLowStockCount || 0),
      bedOccupancyRate: Number(dashboard.value.bedOccupancyRate || 0),
      revenueGrowthRate: Number(dashboard.value.revenueGrowthRate || 0)
    },
    thresholdDraft.value
  )
)

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
  assigneeId: undefined as string | number | undefined,
  assigneeName: '',
  priority: 'NORMAL',
  description: '',
  calendarType: 'WORK' as 'PERSONAL' | 'WORK' | 'DAILY' | 'COLLAB',
  urgency: 'NORMAL' as 'NORMAL' | 'EMERGENCY',
  planCategory: '基础办公',
  eventColor: '#1677ff',
  collaboratorDeptIds: [] as string[],
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
const conflictPolicyOptions = [
  { label: '提示后可继续', value: 'WARN' as const },
  { label: '发现冲突即阻止', value: 'BLOCK' as const },
  { label: '忽略冲突直接保存', value: 'ALLOW' as const }
]
const SEARCH_RECENT_KEY = 'portal_search_recent_routes'
const SEARCH_RECENT_MAX = 8
const MODULE_CONFIG_KEY_PREFIX = 'portal_module_config_v1_'
const CUSTOM_CARD_KEY_PREFIX = 'portal_custom_card_v1_'
const AUTO_SYNC_KEY_PREFIX = 'portal_auto_sync_v1_'
const VISIBLE_TYPES_STORAGE_KEY = 'oa_calendar_visible_types_v1'
const CALENDAR_VIEW_STORAGE_KEY = 'oa_calendar_view_mode_v1'
const CALENDAR_SYNC_PULSE_KEY = 'oa_calendar_sync_pulse_v1'
const CONFLICT_POLICY_STORAGE_KEY = 'oa_calendar_conflict_policy_v1'
const PINYIN_INITIAL_LETTERS = 'ABCDEFGHJKLMNOPQRSTWXYZ'
const PINYIN_INITIAL_BOUNDARY_CHARS = '阿芭擦搭蛾发噶哈讥咔垃妈拿哦啪期然撒塌挖昔压匝'

const modulePresetMap: Record<'director' | 'nurse' | 'finance', PortalModuleKey[]> = {
  director: ['todo', 'reminder', 'operation', 'finance', 'salesFunnel', 'bedStatus', 'birthdayPlan', 'calendar', 'customCards', 'dataEntry', 'quickLaunch', 'expense'],
  nurse: ['todo', 'reminder', 'calendar', 'birthdayPlan', 'bedStatus', 'quickLaunch', 'operation', 'customCards', 'dataEntry', 'expense', 'finance', 'salesFunnel'],
  finance: ['todo', 'reminder', 'finance', 'expense', 'operation', 'customCards', 'birthdayPlan', 'dataEntry', 'calendar', 'quickLaunch', 'salesFunnel', 'bedStatus']
}

const searchAliases: Record<string, string[]> = {
  '/oa/approval': ['审批', '待审批', '流程审批', '批量审批', '请假审批'],
  '/oa/todo': ['待办', '任务', '待处理', '超时'],
  '/oa/work-execution/calendar': ['日历', '协同日历', '行政日历', '排班'],
  '/oa/life/birthday': ['生日', '生日提醒', '生日计划', '老人生日'],
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
  '/oa/life/birthday',
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

function autoSyncStorageKey() {
  return `${AUTO_SYNC_KEY_PREFIX}${String(userStore.staffInfo?.id || 'default')}`
}

function loadAutoSyncSetting() {
  try {
    const raw = localStorage.getItem(autoSyncStorageKey())
    if (raw === '0') autoSyncEnabled.value = false
    if (raw === '1') autoSyncEnabled.value = true
  } catch {}
}

function persistAutoSyncSetting() {
  try {
    localStorage.setItem(autoSyncStorageKey(), autoSyncEnabled.value ? '1' : '0')
  } catch {}
}

async function loadThresholdConfig() {
  try {
    const defaults = await getDashboardThresholdDefaults()
    thresholdConfig.value = mergeThresholdConfig(defaults)
  } catch {}
  const snapshot = loadThresholdSnapshot(userStore.staffInfo?.id)
  if (snapshot) {
    thresholdConfig.value = mergeThresholdConfig(thresholdConfig.value, snapshot)
  }
  thresholdLogs.value = loadThresholdLogs(userStore.staffInfo?.id)
  thresholdSavedSnapshot.value = toThresholdSnapshot(thresholdConfig.value)
  thresholdDraft.value = toThresholdSnapshot(thresholdConfig.value)
}

function openThresholdConfigModal() {
  thresholdDraft.value = toThresholdSnapshot(thresholdConfig.value)
  thresholdConfigOpen.value = true
}

function closeThresholdConfigModal() {
  thresholdDraft.value = toThresholdSnapshot(thresholdConfig.value)
  thresholdConfigOpen.value = false
}

function rollbackThresholdDraft() {
  thresholdDraft.value = toThresholdSnapshot(thresholdSavedSnapshot.value)
}

function applyThresholdPreset(snapshot: ThresholdSnapshot) {
  thresholdDraft.value = toThresholdSnapshot(snapshot)
}

function saveThresholdConfig() {
  try {
    const snapshot = toThresholdSnapshot(thresholdDraft.value)
    if (!thresholdDraftDirty.value) {
      message.info('阈值未变化，无需保存')
      thresholdConfigOpen.value = false
      return
    }
    thresholdLogs.value = saveThresholdSnapshot(userStore.staffInfo?.id, snapshot, 'portal')
    thresholdConfig.value = mergeThresholdConfig(thresholdConfig.value, snapshot)
    thresholdSavedSnapshot.value = snapshot
    message.success('阈值配置已保存')
    thresholdConfigOpen.value = false
  } catch {
    message.warning('阈值保存失败')
  }
}

async function resetThresholdConfig() {
  clearThresholdSnapshot(userStore.staffInfo?.id)
  await loadThresholdConfig()
  message.success('阈值已恢复默认')
  thresholdConfigOpen.value = false
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

function resolveAdaptiveSpan(rowKeys: PortalModuleKey[], target: PortalModuleKey) {
  const visibleKeys = rowKeys.filter((key) => isModuleVisible(key))
  if (!visibleKeys.includes(target)) return 24
  const count = visibleKeys.length
  if (count <= 1) return 24
  if (count === 2) return 12
  if (count === 3) return 8
  return Math.max(6, Math.floor(24 / count))
}

function rowTodoReminderSpan(target: 'todo' | 'reminder') {
  return resolveAdaptiveSpan(['todo', 'reminder'], target)
}

function rowOperationSpan(target: 'operation' | 'finance' | 'salesFunnel') {
  return resolveAdaptiveSpan(['operation', 'finance', 'salesFunnel'], target)
}

function rowStatusExpenseSpan(target: 'bedStatus' | 'expense') {
  return resolveAdaptiveSpan(['bedStatus', 'expense'], target)
}

function goRiskItem(item: { route: string }) {
  const resolved = router.resolve(item.route)
  router.push({
    path: resolved.path,
    query: {
      ...resolved.query,
      fromSource: 'portal',
      ...buildThresholdQuery(thresholdConfig.value)
    }
  })
}

const customCardColSpan = computed(() => {
  const count = filteredCustomCards.value.length
  if (count <= 1) return 24
  if (count === 2) return 12
  if (count === 3) return 8
  return 6
})

const filteredCustomCards = computed(() => {
  const currentStaffId = String(userStore.staffInfo?.id || '')
  const visibleCards = customCards.value
    .filter((item) => item.visible !== false)
    .filter((item) => cardAudienceMatched(item.audience, currentUserAudience.value))
    .filter((item) => {
      const onlyStaff = Array.isArray(item.visibleStaffIds) ? item.visibleStaffIds.map((id) => String(id)) : []
      if (!onlyStaff.length) return true
      return currentStaffId ? onlyStaff.includes(currentStaffId) : false
    })
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
  { title: '待审批流程', value: myTodoSummary.pendingApprovalCount || 0, route: '/oa/approval?scope=PENDING_REVIEW' },
  { title: '待处理任务', value: myTodoSummary.openTodoCount || 0, route: '/oa/todo' },
  { title: '待确认事项', value: myTodoSummary.ongoingTaskCount || 0, route: '/oa/work-execution/task' },
  { title: '超时未处理', value: myTodoSummary.timeoutApprovalCount || 0, route: '/oa/approval?scope=PENDING_REVIEW&overdue=1' }
])

const totalTodoCount = computed(() =>
  Number(myTodoSummary.pendingApprovalCount || 0) + Number(myTodoSummary.openTodoCount || 0)
)

function thresholdLevel(value: number, warningThreshold: number, urgentThreshold: number) {
  if (value >= urgentThreshold) return '紧急'
  if (value >= warningThreshold) return '预警'
  return '普通通知'
}

const riskReminders = computed(() => {
  const elderAbnormal = Number(summary.elderAbnormalCount || 0) + Number(summary.healthAbnormalCount || 0)
  const arrearsCount = Number(financeOverview.value?.risk?.overdueElderCount || 0)
  const inventoryCount = Number(summary.inventoryLowStockCount || 0)
  const orderPendingCount = Number(summary.pendingMedicalOrderCount || 0)
  const approvalTimeoutCount = Number(summary.approvalTimeoutCount || 0)
  const bedOccupancyRate = Number(dashboard.value.bedOccupancyRate || 0)
  const revenueGrowthRate = Number(dashboard.value.revenueGrowthRate || 0)
  const contractExpiringCount = Number(summary.elderContractExpiringCount || 0)
  const supervisorAnomalyCount = Number(summary.supervisorAnomalyCount || 0)
  const birthdayReminderCount = birthdayRows.value
    .filter((item) => Number(item.daysUntil || 9999) >= 0 && Number(item.daysUntil || 9999) <= 7).length

  const abnormalThreshold = Number(thresholdConfig.value.abnormalTaskThreshold || 1)
  const inventoryThreshold = Number(thresholdConfig.value.inventoryAlertThreshold || 1)
  const bedThreshold = Number(thresholdConfig.value.bedOccupancyThreshold || 1)
  const revenueDropThreshold = Number(thresholdConfig.value.revenueDropThreshold || 1)

  return [
    { title: '长者异常', count: elderAbnormal, route: '/health/inspection', level: elderAbnormal > 0 ? '紧急' : '普通通知' },
    { title: '生日提醒', count: birthdayReminderCount, route: '/oa/life/birthday?daysAhead=7', level: birthdayReminderCount > 0 ? '预警' : '普通通知' },
    { title: '欠费提醒', count: arrearsCount, route: '/finance/bills/in-resident?filter=overdue', level: arrearsCount > 0 ? '紧急' : '普通通知' },
    {
      title: '库存不足',
      count: inventoryCount,
      route: '/logistics/storage/alerts',
      level: thresholdLevel(inventoryCount, inventoryThreshold, inventoryThreshold * 2),
      desc: `阈值 ${inventoryThreshold}`
    },
    {
      title: '医嘱未执行',
      count: orderPendingCount,
      route: '/medical-care/orders',
      level: thresholdLevel(orderPendingCount, abnormalThreshold, abnormalThreshold * 2),
      desc: `阈值 ${abnormalThreshold}`
    },
    {
      title: '床位高占用',
      count: bedOccupancyRate >= bedThreshold ? Number(bedOccupancyRate.toFixed(2)) : 0,
      route: '/stats/org/bed-usage',
      level: thresholdLevel(bedOccupancyRate, bedThreshold, Math.min(100, bedThreshold + 3)),
      desc: `阈值 ${bedThreshold}%`
    },
    {
      title: '收入环比下滑',
      count: revenueGrowthRate <= 0 - revenueDropThreshold ? Number(Math.abs(revenueGrowthRate).toFixed(2)) : 0,
      route: '/stats/monthly-revenue',
      level: revenueGrowthRate <= 0 - revenueDropThreshold
        ? '预警'
        : '普通通知',
      desc: `阈值 -${revenueDropThreshold}%`
    },
    { title: '审批超时', count: approvalTimeoutCount, route: '/oa/approval', level: approvalTimeoutCount > 0 ? '紧急' : '普通通知' },
    { title: '监管链异常', count: supervisorAnomalyCount, route: '/hr/profile/account-access?view=supervisor-anomalies', level: supervisorAnomalyCount > 0 ? '紧急' : '普通通知' },
    { title: '证书到期', count: certificateReminderCount.value, route: '/hr/development/certificate-reminders', level: certificateReminderCount.value > 0 ? '预警' : '普通通知' },
    { title: '合同到期', count: contractExpiringCount, route: '/marketing/contracts/renewal', level: contractExpiringCount > 0 ? '预警' : '普通通知' }
  ]
})
const displayRiskReminders = computed(() => {
  const list = [...riskReminders.value]
    .sort((a, b) => Number(isReminderPinned(b)) - Number(isReminderPinned(a)))
  if (reminderViewMode.value === 'unread') {
    return list.filter((item) => !isReminderRead(item))
  }
  if (reminderViewMode.value === 'pinned') {
    return list.filter((item) => isReminderPinned(item))
  }
  if (reminderViewMode.value === 'urgent') {
    return list.filter((item) => item.level === '紧急')
  }
  return list
})
const unreadReminderCount = computed(() => riskReminders.value.filter((item) => !isReminderRead(item)).length)
const urgentReminderCount = computed(() => riskReminders.value.filter((item) => item.level === '紧急').length)
const activeRiskCount = computed(() => riskReminders.value.filter((item) => item.level !== '普通通知').length)

const approvalOpinionList = computed(() => approvalRows.value
  .filter((item) => item.status === 'APPROVED')
  .slice(0, 12)
  .map((item) => ({
    id: String(item.id),
    title: item.title || '未命名审批',
    remark: String(item.remark || ''),
    typeText: typeLabel(item.approvalType),
    timeText: dayjs(item.endTime || item.startTime || new Date()).format('YYYY-MM-DD HH:mm')
  })))

const approvalTrackList = computed(() => approvalRows.value
  .slice(0, 10)
  .map((item) => {
    const parsed = parseApprovalMeta(item.formData)
    const steps = approvalStepNames(item.approvalType)
    const statusText = item.status === 'APPROVED' ? '已通过' : item.status === 'REJECTED' ? '已驳回' : '审批中'
    const statusColor = item.status === 'APPROVED' ? 'green' : item.status === 'REJECTED' ? 'red' : 'orange'
    const currentStep = resolveApprovalCurrentStep(item.approvalType, item.status)
    const nextStep = resolveApprovalNextStep(item.approvalType, item.status)
    return {
      id: String(item.id),
      title: item.title || `${typeLabel(item.approvalType)}申请`,
      approvalType: item.approvalType || 'LEAVE',
      applicantId: item.applicantId,
      applicantName: item.applicantName || '',
      amount: item.amount,
      startTime: item.startTime,
      endTime: item.endTime,
      status: String(item.status || 'PENDING'),
      statusText,
      statusColor,
      currentStep,
      nextStep,
      stepCount: steps.length,
      currentApprover: parsed.currentApproverName || parsed.nextApproverName || '',
      urgeCount: Number(parsed.urgeCount || 0),
      formData: item.formData || ''
    }
  }))

const birthdayStats = computed(() => {
  const now = dayjs()
  const thisMonth = now.month() + 1
  const monthCount = birthdayRows.value.filter((item) => {
    if (!item.nextBirthday) return false
    return dayjs(item.nextBirthday).month() + 1 === thisMonth
  }).length
  return {
    today: birthdayRows.value.filter((item) => Number(item.daysUntil || 9999) === 0).length,
    next7Days: birthdayRows.value.filter((item) => Number(item.daysUntil || 9999) >= 0 && Number(item.daysUntil || 9999) <= 7).length,
    thisMonth: monthCount
  }
})

const birthdayFilteredRows = computed(() => birthdayRows.value.filter((item) => {
  const age = Number(item.ageOnNextBirthday || 0)
  if (birthdayAgeBand.value === '80_PLUS') return age >= 80
  if (birthdayAgeBand.value === '90_PLUS') return age >= 90
  if (birthdayAgeBand.value === 'UNDER_80') return age > 0 && age < 80
  return true
}))

const birthdaySliceStats = computed(() => ({
  filteredTotal: birthdayFilteredRows.value.length,
  filteredToday: birthdayFilteredRows.value.filter((item) => Number(item.daysUntil || 9999) === 0).length,
  filteredMonth: birthdayFilteredRows.value.filter((item) => Number(item.daysUntil || 9999) >= 0 && Number(item.daysUntil || 9999) <= 31).length
}))

const birthdayUpcomingList = computed(() => birthdayFilteredRows.value
  .filter((item) => Number(item.daysUntil || 9999) >= 0 && Number(item.daysUntil || 9999) <= 31)
  .sort((a, b) => Number(a.daysUntil || 9999) - Number(b.daysUntil || 9999))
  .slice(0, 12))

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
      { label: '新建线索', route: '/marketing/leads/all?quick=create' },
      { label: '入住评估', route: '/elder/assessment/ability/admission' },
      { label: '入住办理', route: '/elder/admission-processing' },
      { label: '合同签约', route: '/marketing/contracts/pending?flowStage=PENDING_ASSESSMENT' }
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
  {
    title: '床位利用率',
    value: `${Number(dashboard.value.bedOccupancyRate || 0).toFixed(1)}%${Number(dashboard.value.bedOccupancyRate || 0) >= Number(thresholdConfig.value.bedOccupancyThreshold || 0) ? ' ⚠' : ''}`,
    route: '/stats/org/bed-usage'
  }
])

const financeOverviewItems = computed(() => [
  { title: '今日收入', value: money(financeOverview.value?.cashier?.todayCollectedTotal || 0), route: '/finance/payments/register?date=today' },
  { title: '本月收入', value: money(financeOverview.value?.revenueStructure?.monthRevenueTotal || 0), route: '/finance/reports/revenue-structure' },
  { title: '欠费人数', value: Number(financeOverview.value?.risk?.overdueElderCount || 0), route: '/finance/bills/in-resident?filter=overdue' },
  { title: '欠费金额', value: money(financeOverview.value?.risk?.overdueAmount || 0), route: '/finance/reports/overall?focus=arrears' }
])

const salesFunnelItems = computed(() => [
  { title: '今日新增咨询', value: salesFunnel.todayConsultCount || 0, route: '/marketing/leads/all?tab=consultation' },
  { title: '待评估人数', value: salesFunnel.evaluationCount || 0, route: '/marketing/contracts/pending?flowStage=PENDING_ASSESSMENT' },
  { title: '待签约人数', value: salesFunnel.pendingSignCount || 0, route: '/marketing/contracts/pending?flowStage=PENDING_SIGN' },
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
        { label: '本月已通过报销', value: money(monthExpenseCount), route: '/hr/expense/approval-flow' },
        { label: '报销申请入口', value: '发起申请', route: '/hr/expense/records?scene=training-reimburse' },
        { label: '我的审批待办', value: `${myTodoSummary.pendingApprovalCount || 0} 条`, route: '/oa/approval?scope=PENDING_REVIEW' }
      ]
    },
    {
      title: '部门费用',
      rows: [
        { label: '本月部门费用', value: money(deptExpenseCount), route: '/hr/workbench' },
        { label: '待审报销单', value: `${summary.reimbursePendingCount || 0} 条`, route: '/oa/approval?type=REIMBURSE&status=PENDING' },
        { label: '待审采购单', value: `${summary.purchasePendingCount || 0} 条`, route: '/oa/approval?type=PURCHASE&status=PENDING' }
      ]
    },
    {
      title: '发票夹',
      rows: [
        { label: '已录入发票数量', value: `${invoiceCount} 张`, route: '/finance/fees/payment-and-invoice' },
        { label: '待报销发票', value: `${myTodoSummary.openTodoCount || 0} 张`, route: '/finance/fees/payment-and-invoice' },
        { label: '本月发票金额', value: money(financeOverview.value?.cashier?.todayInvoiceAmount || 0), route: '/finance/reconcile/invoice' }
      ]
    }
  ]
})

const portalFestivalEvents = computed(() => {
  const year = dayjs().year()
  const items = [
    { date: `${year}-01-01`, title: '元旦' },
    { date: `${year}-05-01`, title: '劳动节' },
    { date: `${year}-10-01`, title: '国庆节' },
    { date: `${year}-12-31`, title: '年终总结日' }
  ]
  return items.map((item) => ({
    id: `portal-festival-${item.date}`,
    title: `【重大节日】${item.title}`,
    start: `${item.date}T00:00:00`,
    end: `${item.date}T23:59:59`,
    allDay: true,
    color: '#f5222d',
    extendedProps: {
      calendarType: 'DAILY',
      urgency: 'EMERGENCY'
    }
  }))
})

const portalBirthdayEvents = computed(() => birthdayRows.value
  .slice(0, 300)
  .map((item) => {
    const date = item.nextBirthday ? dayjs(item.nextBirthday) : null
    if (!date || !date.isValid()) return null
    const urgency = Number(item.daysUntil || 9999) <= 0 ? 'EMERGENCY' : 'NORMAL'
    return {
      id: `portal-birthday-${item.elderId || item.elderName || item.nextBirthday}`,
      title: `🎂 ${item.elderName || '老人生日'}`,
      start: `${date.format('YYYY-MM-DD')}T09:00:00`,
      end: `${date.format('YYYY-MM-DD')}T10:00:00`,
      color: urgency === 'EMERGENCY' ? '#eb2f96' : '#f759ab',
      extendedProps: {
        calendarType: 'DAILY',
        urgency,
        assigneeName: '系统提醒',
        collaboratorNames: '',
        planCategory: '生日提醒'
      }
    }
  })
  .filter((item): item is NonNullable<typeof item> => !!item))

const calendarOptions = computed(() => ({
  plugins: [dayGridPlugin, interactionPlugin],
  initialView: currentCalendarView.value,
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
  datesSet: (arg: any) => {
    const viewType = String(arg?.view?.type || '')
    if (viewType === 'dayGridMonth' || viewType === 'dayGridWeek' || viewType === 'dayGridDay') {
      currentCalendarView.value = viewType
    }
  },
  eventDidMount: (arg: any) => {
    const event = arg?.event
    const taskType = calendarTypeText(event?.extendedProps?.calendarType)
    const urgencyText = event?.extendedProps?.urgency === 'EMERGENCY' ? '紧急' : '常规'
    const assigneeText = event?.extendedProps?.assigneeName || '-'
    const collaboratorText = event?.extendedProps?.collaboratorNames || '-'
    const planCategory = event?.extendedProps?.planCategory || '-'
    arg?.el?.setAttribute?.(
      'title',
      `${event?.title || ''}\n类型：${taskType}\n紧急：${urgencyText}\n负责人：${assigneeText}\n协同：${collaboratorText}\n分类：${planCategory}`
    )
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
  events: [
    ...calendarRows.value
      .filter((task) => visibleCalendarTypes.value.includes((task.calendarType || 'WORK') as any))
      .map((task) => ({
        id: String(task.id),
        title: `${task.title}${task.assigneeName ? `（${task.assigneeName}）` : ''}`,
        start: normalizeDateTimeValue(task.startTime || task.endTime),
        end: normalizeDateTimeValue(task.endTime || task.startTime),
        color: resolveTaskColor(task),
        classNames: task.status === 'DONE' ? ['calendar-event-done'] : [],
        extendedProps: {
          calendarType: task.calendarType || 'WORK',
          urgency: task.urgency || 'NORMAL',
          assigneeName: task.assigneeName || '',
          collaboratorNames: normalizeCollaboratorNames(task.collaboratorNames).join('、'),
          planCategory: task.planCategory || ''
        }
      })),
    ...portalBirthdayEvents.value,
    ...portalFestivalEvents.value
  ]
}))

const todayAgenda = computed(() => buildAgendaByDate(dayjs()))
const tomorrowAgenda = computed(() => buildAgendaByDate(dayjs().add(1, 'day')))
const scheduleModalTitle = computed(() => (editingScheduleId.value ? '编辑行政日程' : '新增行政日程'))
const selectedCalendarDateAgenda = computed(() => buildAgendaByDate(dayjs(selectedCalendarDateText.value)))
const collaboratorTip = computed(() => {
  if (scheduleForm.calendarType !== 'COLLAB') return ''
  const deptCount = scheduleForm.collaboratorDeptIds.length
  const staffCount = scheduleForm.collaboratorIds.length
  if (!deptCount && !staffCount) return '未选择协同对象'
  const previewText = collaboratorPreviewLoading.value
    ? '正在计算同步人数'
    : `预计同步 ${collaboratorPreviewTotal.value} 人`
  const namesText = collaboratorPreviewNames.value.length ? `（${collaboratorPreviewNames.value.join('、')}）` : ''
  return `已选 ${deptCount} 个部门、${staffCount} 位员工；${previewText}${namesText}，保存后自动展开并同步到协同日历`
})

const collaboratorDeltaTip = computed(() => {
  if (scheduleForm.calendarType !== 'COLLAB' || editingScheduleId.value == null) return ''
  const currentSet = new Set(scheduleForm.collaboratorIds)
  const originalSet = new Set(originalCollaboratorIds.value)
  const added = scheduleForm.collaboratorIds.filter((id) => !originalSet.has(id))
  const removed = originalCollaboratorIds.value.filter((id) => !currentSet.has(id))
  if (!added.length && !removed.length) return '协同成员未发生变化'
  const addedText = added.length ? `新增 ${added.length} 人` : '新增 0 人'
  const removedText = removed.length ? `移除 ${removed.length} 人` : '移除 0 人'
  return `协同成员变更：${addedText}，${removedText}`
})

const calendarBuckets = computed(() => {
  const defs = [
    { type: 'PERSONAL' as const, label: '个人', color: '#52c41a' },
    { type: 'WORK' as const, label: '部门工作', color: '#1677ff' },
    { type: 'DAILY' as const, label: '日常计划', color: '#fa8c16' },
    { type: 'COLLAB' as const, label: '协同日历', color: '#722ed1' }
  ]
  return defs.map((item) => ({
    ...item,
    count: calendarRows.value.filter((task) => (task.calendarType || 'WORK') === item.type).length
  }))
})

const calendarInsights = computed(() => {
  const todayText = dayjs().format('YYYY-MM-DD')
  return [
    { label: '今日', value: calendarRows.value.filter((task) => normalizeDateTimeValue(task.startTime || task.endTime)?.startsWith(todayText)).length, color: 'blue' },
    { label: '紧急', value: calendarRows.value.filter((task) => task.urgency === 'EMERGENCY' && task.status !== 'DONE').length, color: 'red' },
    { label: '已完成', value: calendarRows.value.filter((task) => task.status === 'DONE').length, color: 'default' },
    { label: '节假日', value: portalFestivalEvents.value.length, color: 'orange' },
    { label: '生日提醒', value: portalBirthdayEvents.value.length, color: 'magenta' }
  ]
})

const calendarLoadLeaders = computed(() => {
  const bucket = new Map<string, number>()
  calendarRows.value
    .filter((task) => task.status !== 'DONE')
    .forEach((task) => {
      const name = (task.assigneeName || '未分配').trim()
      bucket.set(name, Number(bucket.get(name) || 0) + 1)
    })
  return Array.from(bucket.entries())
    .sort((a, b) => b[1] - a[1])
    .slice(0, 4)
    .map(([name, count]) => ({ name, count }))
})

const calendarWeeklyLoad = computed(() => {
  const start = dayjs().startOf('week')
  const list = Array.from({ length: 7 }).map((_, index) => {
    const day = start.add(index, 'day')
    const dayText = day.format('YYYY-MM-DD')
    const count = calendarRows.value.filter((task) => {
      if (task.status === 'DONE') return false
      const startTime = normalizeDateTimeValue(task.startTime || task.endTime)
      return startTime ? dayjs(startTime).format('YYYY-MM-DD') === dayText : false
    }).length
    return {
      day: dayText,
      label: ['日', '一', '二', '三', '四', '五', '六'][day.day()],
      count
    }
  })
  const max = list.reduce((acc, item) => Math.max(acc, item.count), 1)
  return list.map((item) => ({
    ...item,
    height: Math.round((item.count / max) * 48)
  }))
})

function severityColor(level: string) {
  if (level === '紧急') return 'red'
  if (level === '预警') return 'orange'
  return 'blue'
}

function reminderStorageKey() {
  return `portal_reminder_state_v1_${String(userStore.staffInfo?.id || 'default')}`
}

function reminderKey(item: { title: string }) {
  return String(item.title || '')
}

function loadReminderState() {
  try {
    const raw = localStorage.getItem(reminderStorageKey())
    if (!raw) {
      reminderState.value = {}
      return
    }
    const parsed = JSON.parse(raw)
    reminderState.value = parsed && typeof parsed === 'object' ? parsed : {}
  } catch {
    reminderState.value = {}
  }
}

function persistReminderState() {
  try {
    localStorage.setItem(reminderStorageKey(), JSON.stringify(reminderState.value))
  } catch {}
}

function isReminderRead(item: { title: string }) {
  const key = reminderKey(item)
  return Boolean(reminderState.value[key]?.read)
}

function isReminderPinned(item: { title: string }) {
  const key = reminderKey(item)
  return Boolean(reminderState.value[key]?.pinned)
}

function toggleReminderRead(item: { title: string }) {
  const key = reminderKey(item)
  const current = reminderState.value[key] || {}
  reminderState.value = {
    ...reminderState.value,
    [key]: {
      ...current,
      read: !current.read,
      updatedAt: dayjs().format('YYYY-MM-DDTHH:mm:ss')
    }
  }
  persistReminderState()
}

function toggleReminderPinned(item: { title: string }) {
  const key = reminderKey(item)
  const current = reminderState.value[key] || {}
  reminderState.value = {
    ...reminderState.value,
    [key]: {
      ...current,
      pinned: !current.pinned,
      updatedAt: dayjs().format('YYYY-MM-DDTHH:mm:ss')
    }
  }
  persistReminderState()
}

function markAllReminderRead() {
  const next: Record<string, { read?: boolean; pinned?: boolean; updatedAt?: string; lastCount?: number }> = { ...reminderState.value }
  riskReminders.value.forEach((item) => {
    const key = reminderKey(item)
    next[key] = {
      ...(next[key] || {}),
      read: true,
      updatedAt: dayjs().format('YYYY-MM-DDTHH:mm:ss')
    }
  })
  reminderState.value = next
  persistReminderState()
  message.success('提醒已全部标记为已读')
}

function typeLabel(value?: string) {
  if (value === 'LEAVE') return '请假'
  if (value === 'OVERTIME') return '加班'
  if (value === 'REIMBURSE') return '报销'
  if (value === 'PURCHASE') return '采购'
  if (value === 'MATERIAL_APPLY') return '物资申领'
  if (value === 'MARKETING_PLAN') return '营销方案'
  return value || '审批'
}

function parseApprovalMeta(raw?: string) {
  if (!raw || !raw.trim()) return {} as Record<string, any>
  try {
    const parsed = JSON.parse(raw)
    return parsed && typeof parsed === 'object' ? parsed : {}
  } catch {
    return {}
  }
}

function approvalStepNames(value?: string) {
  if (value === 'LEAVE') return ['员工提交', '部门审批', '人事确认', '院长审批', '归档']
  if (value === 'REIMBURSE') return ['申请提交', '财务初审', '院长审批', '出纳处理', '归档']
  if (value === 'PURCHASE') return ['申请提交', '仓储核验', '财务审批', '院长审批', '采购执行']
  if (value === 'MARKETING_PLAN') return ['方案提交', '运营评审', '院长审批', '执行落地']
  return ['提交', '审批', '归档']
}

function resolveApprovalCurrentStep(type?: string, status?: string) {
  if (status === 'APPROVED') return '流程完成'
  if (status === 'REJECTED') return '已驳回'
  const steps = approvalStepNames(type)
  return steps[Math.min(1, steps.length - 1)] || '待审批'
}

function resolveApprovalNextStep(type?: string, status?: string) {
  if (status === 'APPROVED') return '已归档'
  if (status === 'REJECTED') return '申请人修改后重提'
  const steps = approvalStepNames(type)
  return steps[Math.min(2, steps.length - 1)] || '等待处理'
}

function openApprovalOpinionPanel() {
  if (!approvalRows.value.length) {
    loadApprovalTracks().catch(() => {})
  }
  approvalOpinionOpen.value = true
}

async function urgeApproval(item: {
  id: string
  title: string
  approvalType?: string
  applicantId?: number
  applicantName?: string
  amount?: number
  startTime?: string
  endTime?: string
  currentApprover?: string
  formData?: string
}, silent = false) {
  try {
    const parsed = parseApprovalMeta(item.formData)
    const urgeCount = Number(parsed.urgeCount || 0) + 1
    const fallbackApplicantName = (item.applicantName || userStore.staffInfo?.realName || userStore.staffInfo?.username || '').trim()
    if (!fallbackApplicantName) {
      if (!silent) message.error('催办失败：申请人信息缺失')
      return false
    }
    const nextFormData = JSON.stringify({
      ...parsed,
      urgeCount,
      lastUrgedAt: dayjs().format('YYYY-MM-DDTHH:mm:ss'),
      lastUrgedBy: userStore.staffInfo?.realName || userStore.staffInfo?.username || '系统用户'
    })
    await updateApproval(item.id, {
      approvalType: String(item.approvalType || 'LEAVE'),
      title: String(item.title || '审批催办'),
      applicantId: item.applicantId,
      applicantName: fallbackApplicantName,
      amount: item.amount,
      startTime: item.startTime,
      endTime: item.endTime,
      formData: nextFormData
    })
    await createOaTask({
      title: `催办审批：${item.title}`,
      description: '来自首页审批流程跟踪的催办提醒',
      calendarType: 'WORK',
      priority: 'HIGH',
      urgency: 'EMERGENCY',
      status: 'OPEN',
      assigneeName: item.currentApprover || undefined
    })
    if (!silent) message.success(`已催办：${item.title}`)
    await refreshPortalModules(true)
    return true
  } catch (error: any) {
    if (!silent) message.error(error?.message || '催办失败，请稍后再试')
    return false
  }
}

async function urgeAllPendingApprovals() {
  const pendingList = approvalTrackList.value.filter((item) => item.status === 'PENDING').slice(0, 20)
  if (!pendingList.length) {
    message.info('当前没有待催办审批')
    return
  }
  let successCount = 0
  for (const item of pendingList) {
    // eslint-disable-next-line no-await-in-loop
    const ok = await urgeApproval(item, true)
    if (ok) successCount += 1
  }
  if (successCount > 0) {
    message.success(`已批量催办 ${successCount} 条审批`)
  }
}

function exportBirthdayCsv() {
  if (!birthdayFilteredRows.value.length) {
    message.info('暂无生日数据可导出')
    return
  }
  const header = ['姓名', '出生日期', '下次生日', '剩余天数', '届时年龄', '房间号', '床位号']
  const lines = birthdayFilteredRows.value.map((row) => ([
    row.elderName || '',
    row.birthDate || '',
    row.nextBirthday || '',
    String(row.daysUntil ?? ''),
    String(row.ageOnNextBirthday ?? ''),
    row.roomNo || '',
    row.bedNo || ''
  ]))
  const csv = [header, ...lines]
    .map((cols) => cols.map((cell) => `"${String(cell || '').replace(/"/g, '""')}"`).join(','))
    .join('\n')
  const blob = new Blob([`\uFEFF${csv}`], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `生日日程_${dayjs().format('YYYYMMDD')}.csv`
  a.click()
  URL.revokeObjectURL(url)
}

async function syncBirthdayTasks() {
  const candidates = birthdayFilteredRows.value.filter((item) => Number(item.daysUntil || 9999) >= 0 && Number(item.daysUntil || 9999) <= 7)
  if (!candidates.length) {
    message.info('未来7天暂无生日，无需同步')
    return
  }
  const existedTitles = new Set(calendarRows.value.map((item) => String(item.title || '')))
  let createdCount = 0
  for (const item of candidates.slice(0, 20)) {
    const dateText = item.nextBirthday ? dayjs(item.nextBirthday).format('YYYY-MM-DD') : ''
    if (!dateText) continue
    const plans = [
      { offset: -7, title: `生日准备（T-7）：${item.elderName}`, desc: '提醒活动负责人准备', urgency: 'NORMAL' as const },
      { offset: -1, title: `生日准备（T-1）：${item.elderName}`, desc: '提醒楼层/厨房/后勤准备', urgency: 'NORMAL' as const },
      { offset: 0, title: `今日生日：${item.elderName}`, desc: '今日生日置顶提醒', urgency: 'EMERGENCY' as const }
    ]
    for (const plan of plans) {
      const targetDate = dayjs(dateText).add(plan.offset, 'day')
      if (targetDate.isBefore(dayjs().startOf('day'))) continue
      if (existedTitles.has(plan.title)) continue
      try {
        const created = await createOaTask({
          title: plan.title,
          calendarType: 'DAILY',
          planCategory: '生日卡片',
          startTime: `${targetDate.format('YYYY-MM-DD')}T09:00:00`,
          endTime: `${targetDate.format('YYYY-MM-DD')}T10:00:00`,
          urgency: plan.urgency,
          description: `${plan.desc}（${item.elderName}，${item.ageOnNextBirthday || '--'}岁）`
        })
        if (created?.id != null) {
          upsertCalendarTask(created)
          createdCount += 1
        }
      } catch {}
    }
  }
  await refreshPortalModules(true)
  if (createdCount > 0) emitCalendarSyncPulse('CREATE')
  message.success(createdCount > 0 ? `已同步 ${createdCount} 条生日提醒` : '生日提醒已是最新')
}

function printBirthdayYearPlan() {
  const list = birthdayFilteredRows.value
    .filter((item) => !!item.nextBirthday)
    .sort((a, b) => String(a.nextBirthday).localeCompare(String(b.nextBirthday)))
  if (!list.length) {
    message.info('暂无生日数据可打印')
    return
  }
  const rowsHtml = list
    .map((item, index) => `<tr>
      <td>${index + 1}</td>
      <td>${item.elderName || '-'}</td>
      <td>${item.nextBirthday || '-'}</td>
      <td>${item.ageOnNextBirthday || '-'}</td>
      <td>${item.roomNo || '-'}/${item.bedNo || '-'}</td>
    </tr>`)
    .join('')
  const html = `<!doctype html><html><head><meta charset="utf-8"><title>年度生日计划</title>
    <style>body{font-family:Arial,'PingFang SC';padding:20px;color:#111;} h2{margin:0 0 10px;} table{width:100%;border-collapse:collapse;} th,td{border:1px solid #ddd;padding:8px;font-size:12px;} th{background:#f5f7fa;}</style>
    </head><body><h2>年度生日日程表（老人）</h2><div>生成时间：${dayjs().format('YYYY-MM-DD HH:mm:ss')}</div>
    <table><thead><tr><th>#</th><th>姓名</th><th>生日日期</th><th>年龄</th><th>房间/床位</th></tr></thead><tbody>${rowsHtml}</tbody></table></body></html>`
  const win = window.open('', '_blank')
  if (!win) return
  win.document.write(html)
  win.document.close()
  win.focus()
  win.print()
}

async function refreshAfterCalendarChange() {
  await refreshPortalModules(true, '日程更新')
}

function openBirthdayListMonth() {
  go(`/oa/life/birthday?month=${dayjs().month() + 1}`)
}

function openBirthdayListByDays(days: number) {
  go(`/oa/life/birthday?daysAhead=${days}`)
}

function openBirthdayElderDetail(item: BirthdayReminder) {
  if (item.elderId != null) {
    go(`/elder/detail/${item.elderId}`)
    return
  }
  go(`/elder/list?keyword=${encodeURIComponent(item.elderName || '')}`)
}

function resolveBirthdayMonth(item?: BirthdayReminder) {
  const target = item?.nextBirthday && dayjs(item.nextBirthday).isValid() ? dayjs(item.nextBirthday) : dayjs()
  return target.format('YYYY-MM')
}

function resolveBirthdayMonthCount(monthText: string) {
  return birthdayFilteredRows.value.filter((row) => row.nextBirthday && dayjs(row.nextBirthday).format('YYYY-MM') === monthText).length
}

function openBirthdayActivityCreate(item?: BirthdayReminder) {
  const month = resolveBirthdayMonth(item)
  const count = resolveBirthdayMonthCount(month)
  go(`/oa/activity?quick=create&scope=monthly-birthday&month=${month}&count=${count}`)
}

function openBirthdayActivityList(item?: BirthdayReminder) {
  const month = resolveBirthdayMonth(item)
  go(`/oa/activity?scope=monthly-birthday&month=${month}`)
}

function openBirthdayMaterialPrepare(item?: BirthdayReminder) {
  const month = resolveBirthdayMonth(item)
  go(`/logistics/storage/outbound?scene=monthly-birthday&month=${month}`)
}

function openBirthdayDoneRecord(item?: BirthdayReminder) {
  const month = resolveBirthdayMonth(item)
  go(`/oa/activity?status=DONE&scope=monthly-birthday&month=${month}`)
}

function openAgeReport(mode: '80+' | '90+' | 'CUSTOM') {
  let threshold = 80
  if (mode === '90+') threshold = 90
  if (mode === 'CUSTOM') threshold = Number(window.prompt('请输入年龄阈值（例如 85）', '85') || 85)
  const list = birthdayFilteredRows.value
    .filter((item) => Number(item.ageOnNextBirthday || 0) >= threshold)
    .sort((a, b) => Number(b.ageOnNextBirthday || 0) - Number(a.ageOnNextBirthday || 0))
  birthdayInsightTitle.value = `年龄 ${threshold}+ 生日清单（${list.length} 人）`
  birthdayInsightRows.value = list.map((item) => ({
    label: `${item.elderName} · ${item.nextBirthday || '--'}`,
    value: `${item.ageOnNextBirthday || '--'} 岁`
  }))
  birthdayInsightOpen.value = true
}

function openBirthdayFrequencyCheck() {
  const bucket = new Map<string, number>()
  birthdayFilteredRows.value.forEach((item) => {
    if (!item.nextBirthday) return
    const key = dayjs(item.nextBirthday).format('MM-DD')
    bucket.set(key, Number(bucket.get(key) || 0) + 1)
  })
  const rows = Array.from(bucket.entries())
    .sort((a, b) => b[1] - a[1])
    .slice(0, 12)
  birthdayInsightTitle.value = '同月同日生日高频分布'
  birthdayInsightRows.value = rows.map(([day, count]) => ({ label: day, value: `${count} 人` }))
  birthdayInsightOpen.value = true
}

function toggleCalendarType(type: 'PERSONAL' | 'WORK' | 'DAILY' | 'COLLAB') {
  if (visibleCalendarTypes.value.length === 1 && visibleCalendarTypes.value[0] === type) {
    visibleCalendarTypes.value = ['PERSONAL', 'WORK', 'DAILY', 'COLLAB']
    return
  }
  visibleCalendarTypes.value = [type]
}

function showAllCalendarTypes() {
  visibleCalendarTypes.value = ['PERSONAL', 'WORK', 'DAILY', 'COLLAB']
}

function normalizeVisibleTypes(input: unknown): Array<'PERSONAL' | 'WORK' | 'DAILY' | 'COLLAB'> {
  const valid = ['PERSONAL', 'WORK', 'DAILY', 'COLLAB'] as const
  if (!Array.isArray(input)) return [...valid]
  const filtered = input.filter((item): item is typeof valid[number] => valid.includes(item as typeof valid[number]))
  const deduped = Array.from(new Set(filtered))
  return deduped.length ? deduped : [...valid]
}

function syncVisibleTypesFromStorage() {
  try {
    const raw = localStorage.getItem(VISIBLE_TYPES_STORAGE_KEY)
    if (!raw) return
    visibleCalendarTypes.value = normalizeVisibleTypes(JSON.parse(raw))
  } catch {}
}

function syncCalendarViewFromStorage() {
  try {
    const raw = localStorage.getItem(CALENDAR_VIEW_STORAGE_KEY)
    if (!raw) return
    if (raw === 'dayGridMonth' || raw === 'dayGridWeek' || raw === 'dayGridDay') {
      currentCalendarView.value = raw
    }
  } catch {}
}

function syncConflictPolicyFromStorage() {
  try {
    const raw = localStorage.getItem(CONFLICT_POLICY_STORAGE_KEY)
    if (!raw) return
    if (raw === 'WARN' || raw === 'BLOCK' || raw === 'ALLOW') {
      conflictPolicy.value = raw
    }
  } catch {}
}

function emitCalendarSyncPulse(action: 'CREATE' | 'UPDATE' | 'DONE' | 'DELETE') {
  try {
    localStorage.setItem(CALENDAR_SYNC_PULSE_KEY, JSON.stringify({
      action,
      at: dayjs().format('YYYY-MM-DDTHH:mm:ss')
    }))
  } catch {}
}

function focusReminderCenter() {
  if (urgentReminderCount.value > 0) {
    reminderViewMode.value = 'urgent'
  } else if (unreadReminderCount.value > 0) {
    reminderViewMode.value = 'unread'
  } else {
    reminderViewMode.value = 'all'
  }
  const card = reminderCardRef.value?.$el || reminderCardRef.value
  if (card?.scrollIntoView) {
    card.scrollIntoView({ behavior: 'smooth', block: 'start' })
    return
  }
  go('/oa/approval?status=PENDING')
}

function calendarTypeText(value?: string) {
  if (value === 'PERSONAL') return '个人'
  if (value === 'DAILY') return '日常计划'
  if (value === 'COLLAB') return '协同日历'
  return '部门工作'
}

function normalizeCollaboratorNames(input: string[] | string | undefined) {
  if (!input) return [] as string[]
  if (Array.isArray(input)) return input.filter((item) => !!item)
  return String(input)
    .split(',')
    .map((item) => item.trim())
    .filter((item) => !!item)
}

function normalizeDateTimeValue(value?: string) {
  if (!value) return undefined
  const normalized = String(value).replace(/\s*T\s*/g, 'T').trim()
  const parsed = dayjs(normalized)
  if (!parsed.isValid()) return undefined
  return parsed.format('YYYY-MM-DDTHH:mm:ss')
}

function agendaTimeText(task: OaTask) {
  const startValue = normalizeDateTimeValue(task.startTime || task.endTime)
  const endValue = normalizeDateTimeValue(task.endTime || task.startTime)
  const start = startValue ? dayjs(startValue) : undefined
  const end = endValue ? dayjs(endValue) : undefined
  if (!start) return '--'
  if (!end || start.format('YYYY-MM-DD') !== end.format('YYYY-MM-DD')) return start.format('MM-DD HH:mm')
  return `${start.format('HH:mm')} - ${end.format('HH:mm')}`
}

function buildAgendaByDate(target: Dayjs) {
  const dateText = target.format('YYYY-MM-DD')
  const taskItems = calendarRows.value
    .filter((task) => {
      const start = normalizeDateTimeValue(task.startTime || task.endTime)
      return start ? dayjs(start).format('YYYY-MM-DD') === dateText : false
    })
    .map((task) => ({
      id: String(task.id),
      title: task.title || '未命名日程',
      status: task.status || 'OPEN',
      timeText: agendaTimeText(task),
      typeText: calendarTypeText(task.calendarType),
      assigneeText: task.assigneeName || '',
      dateText,
      readonly: false
    }))

  const birthdayItems = visibleCalendarTypes.value.includes('DAILY')
    ? portalBirthdayEvents.value
    .filter((event) => dayjs(event.start).format('YYYY-MM-DD') === dateText)
    .map((event) => ({
      id: String(event.id),
      title: String(event.title || '生日提醒'),
      status: 'OPEN',
      timeText: '09:00 - 10:00',
      typeText: '日常计划',
      assigneeText: '系统提醒',
      dateText,
      readonly: true
    }))
    : []

  const festivalItems = visibleCalendarTypes.value.includes('DAILY')
    ? portalFestivalEvents.value
    .filter((event) => dayjs(event.start).format('YYYY-MM-DD') === dateText)
    .map((event) => ({
      id: String(event.id),
      title: String(event.title || '节假日'),
      status: 'OPEN',
      timeText: '全天',
      typeText: '日常计划',
      assigneeText: '系统提醒',
      dateText,
      readonly: true
    }))
    : []

  return [...taskItems, ...birthdayItems, ...festivalItems]
    .sort((a, b) => {
      const left = typeof a.timeText === 'string' && a.timeText.includes(':')
        ? dayjs(`${a.dateText} ${a.timeText.slice(0, 5)}`).valueOf()
        : dayjs(`${a.dateText} 00:00`).valueOf()
      const right = typeof b.timeText === 'string' && b.timeText.includes(':')
        ? dayjs(`${b.dateText} ${b.timeText.slice(0, 5)}`).valueOf()
        : dayjs(`${b.dateText} 00:00`).valueOf()
      return left - right
    })
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

function resolveCalendarTypeColor(calendarType: 'PERSONAL' | 'WORK' | 'DAILY' | 'COLLAB', urgency: 'NORMAL' | 'EMERGENCY') {
  if (urgency === 'EMERGENCY') return '#ff4d4f'
  if (calendarType === 'PERSONAL') return '#52c41a'
  if (calendarType === 'DAILY') return '#fa8c16'
  if (calendarType === 'COLLAB') return '#722ed1'
  return '#1677ff'
}

function onUrgencyChange(value: 'NORMAL' | 'EMERGENCY') {
  scheduleForm.eventColor = resolveCalendarTypeColor(scheduleForm.calendarType, value)
}

function onScheduleCalendarTypeChange(value: 'PERSONAL' | 'WORK' | 'DAILY' | 'COLLAB') {
  scheduleForm.eventColor = resolveCalendarTypeColor(value, scheduleForm.urgency)
}

async function resolveCollaborators(departmentIds: string[], staffIds: string[]) {
  const collaboratorMap = new Map<string, string>()
  staffOptions.value
    .filter((item) => staffIds.includes(item.value))
    .forEach((item) => collaboratorMap.set(String(item.value), item.name || item.label))
  for (const staffId of staffIds) {
    if (!collaboratorMap.has(staffId)) collaboratorMap.set(staffId, `员工#${staffId}`)
  }
  if (departmentIds.length > 0) {
    const pages = await Promise.all(
      departmentIds.map((departmentId) => getStaffPage({
        pageNo: 1,
        pageSize: 500,
        departmentId,
        status: 1
      }))
    )
    pages.forEach((page) => {
      ;(page.list || []).forEach((item) => {
        const id = String(item.id)
        const name = (item.realName || item.username || `员工#${id}`).trim()
        collaboratorMap.set(id, name)
      })
    })
  }
  return {
    ids: Array.from(collaboratorMap.keys()),
    names: Array.from(collaboratorMap.values())
  }
}

function resolveAssigneeIdForSubmit() {
  const currentId = userStore.staffInfo?.id != null ? String(userStore.staffInfo.id) : ''
  if (!currentId) return undefined
  const currentRealName = String(userStore.staffInfo?.realName || '').trim()
  const currentUsername = String(userStore.staffInfo?.username || '').trim()
  const input = String(scheduleForm.assigneeName || '').trim()
  if (!input) return Number(currentId)
  if ((currentRealName && input === currentRealName) || (currentUsername && input === currentUsername)) {
    return Number(currentId)
  }
  return scheduleForm.assigneeId != null ? Number(scheduleForm.assigneeId) : undefined
}

function buildConflictMessage(items: Array<{ title?: string; assigneeName?: string; startTime?: string; endTime?: string; reason?: string }>) {
  return items
    .slice(0, 6)
    .map((item, index) => {
      const start = item.startTime ? dayjs(item.startTime).format('MM-DD HH:mm') : '--'
      const end = item.endTime ? dayjs(item.endTime).format('MM-DD HH:mm') : '--'
      return `${index + 1}. ${item.title || '未命名日程'}（${start} ~ ${end}）${item.assigneeName ? ` [${item.assigneeName}]` : ''} - ${item.reason || '时间冲突'}`
    })
    .join('\n')
}

function conflictItemTimeText(item: { startTime?: string; endTime?: string }) {
  const start = item.startTime ? dayjs(item.startTime).format('MM-DD HH:mm') : '--'
  const end = item.endTime ? dayjs(item.endTime).format('MM-DD HH:mm') : '--'
  return `${start} ~ ${end}`
}

function focusConflictDate(startTime?: string) {
  const normalized = normalizeDateTimeValue(startTime)
  if (!normalized) return
  focusCalendarDate(dayjs(normalized).format('YYYY-MM-DD'))
}

function showAllCollaboratorPreview() {
  if (!collaboratorPreviewAllNames.value.length) {
    message.info('暂无协同成员')
    return
  }
  Modal.info({
    title: `协同成员清单（${collaboratorPreviewAllNames.value.length} 人）`,
    content: collaboratorPreviewAllNames.value.join('、'),
    width: 680
  })
}

async function previewScheduleConflicts() {
  if (!scheduleForm.startTime) {
    message.warning('请先选择开始时间')
    return
  }
  const collaboratorPayload = scheduleForm.calendarType === 'COLLAB'
    ? await resolveCollaborators(scheduleForm.collaboratorDeptIds, scheduleForm.collaboratorIds)
    : { ids: [] as string[], names: [] as string[] }
  const payload = {
    taskId: editingScheduleId.value || undefined,
    startTime: dayjs(scheduleForm.startTime).format('YYYY-MM-DDTHH:mm:ss'),
    endTime: scheduleForm.endTime ? dayjs(scheduleForm.endTime).format('YYYY-MM-DDTHH:mm:ss') : undefined,
    assigneeName: scheduleForm.assigneeName || undefined,
    collaboratorIds: collaboratorPayload.ids
  }
  conflictPreviewLoading.value = true
  try {
    const conflicts = await checkOaTaskConflicts(payload)
    conflictPreviewItems.value = conflicts || []
    if (!conflicts?.length) {
      message.success('预检通过，未发现冲突')
    }
  } catch {
    message.warning('冲突预检失败，请稍后重试')
  } finally {
    conflictPreviewLoading.value = false
  }
}

async function confirmByConflicts(payload: { taskId?: string | number; startTime: string; endTime?: string; assigneeName?: string; collaboratorIds: string[] }) {
  if (conflictPolicy.value === 'ALLOW') return true
  try {
    const conflicts = await checkOaTaskConflicts({
      taskId: payload.taskId,
      startTime: payload.startTime,
      endTime: payload.endTime,
      assigneeName: payload.assigneeName,
      collaboratorIds: payload.collaboratorIds
    })
    conflictPreviewItems.value = conflicts || []
    if (!conflicts?.length) return true
    if (conflictPolicy.value === 'BLOCK') {
      message.error(`检测到 ${conflicts.length} 条冲突，当前策略不允许保存`)
      return false
    }
    return await new Promise<boolean>((resolve) => {
      Modal.confirm({
        title: `检测到 ${conflicts.length} 条潜在冲突`,
        content: buildConflictMessage(conflicts),
        okText: '仍然保存',
        cancelText: '取消',
        onOk: () => resolve(true),
        onCancel: () => resolve(false)
      })
    })
  } catch {
    message.warning('冲突检测失败，已跳过冲突提示')
    return true
  }
}

let collaboratorPreviewSeq = 0
async function refreshCollaboratorPreview() {
  if (scheduleForm.calendarType !== 'COLLAB') {
    collaboratorPreviewTotal.value = 0
    collaboratorPreviewNames.value = []
    collaboratorPreviewAllNames.value = []
    collaboratorPreviewLoading.value = false
    return
  }
  const currentSeq = ++collaboratorPreviewSeq
  collaboratorPreviewLoading.value = true
  try {
    const result = await resolveCollaborators(scheduleForm.collaboratorDeptIds, scheduleForm.collaboratorIds)
    if (currentSeq !== collaboratorPreviewSeq) return
    collaboratorPreviewTotal.value = result.ids.length
    collaboratorPreviewAllNames.value = result.names
    collaboratorPreviewNames.value = result.names.slice(0, 6)
  } finally {
    if (currentSeq === collaboratorPreviewSeq) {
      collaboratorPreviewLoading.value = false
    }
  }
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

function clampModuleCardOffsetX(value: number) {
  return Math.max(-2400, Math.min(2400, snapResizeValue(value, 4)))
}

function clampModuleCardOffsetY(value: number) {
  return Math.max(-3200, Math.min(3200, snapResizeValue(value, 4)))
}

function snapResizeValue(value: number, step: number) {
  return Math.round(value / step) * step
}

function moduleCardStyle(key: PortalModuleKey) {
  void key
  return {
    width: '100%'
  }
}

function customCardItemStyle(item: PortalCustomCardItem) {
  return {
    borderTopColor: item.themeColor || '#1677ff',
    width: '100%',
    minHeight: '168px'
  }
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

function isEditableTarget(target: EventTarget | null) {
  const element = target as HTMLElement | null
  if (!element) return false
  if (element.isContentEditable) return true
  const tagName = String(element.tagName || '').toLowerCase()
  return tagName === 'input' || tagName === 'textarea' || tagName === 'select'
}

function focusGlobalSearch() {
  const root = globalSearchRef.value?.$el as HTMLElement | undefined
  const input = root?.querySelector('input') as HTMLInputElement | null
  if (!input) return
  input.focus()
  input.select()
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
    const xMap = new Map<string, number>()
    const yMap = new Map<string, number>()
    parsed.forEach((item: any) => {
      visibilityMap.set(String(item.key), Boolean(item.visible))
      orderMap.set(String(item.key), Number(item.order))
      audienceMap.set(String(item.key), normalizeCardAudience(item.audience))
      const width = Number(item.width)
      const height = Number(item.height)
      const x = Number(item.x)
      const y = Number(item.y)
      widthMap.set(String(item.key), Number.isFinite(width) ? clampModuleCardWidth(width) : undefined)
      heightMap.set(String(item.key), Number.isFinite(height) ? clampModuleCardHeight(height) : undefined)
      xMap.set(String(item.key), Number.isFinite(x) ? clampModuleCardOffsetX(x) : 0)
      yMap.set(String(item.key), Number.isFinite(y) ? clampModuleCardOffsetY(y) : 0)
    })
    moduleConfig.value = portalModuleCatalog.map((item) => ({
      ...item,
      visible: visibilityMap.has(item.key) ? Boolean(visibilityMap.get(item.key)) : true,
      order: Number.isFinite(orderMap.get(item.key)) ? Number(orderMap.get(item.key)) : portalModuleCatalog.findIndex((row) => row.key === item.key),
      audience: audienceMap.get(item.key) || normalizeCardAudience(item.audience),
      width: widthMap.get(item.key),
      height: heightMap.get(item.key),
      x: xMap.get(item.key) ?? 0,
      y: yMap.get(item.key) ?? 0
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
        height: undefined,
        x: 0,
        y: 0
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
    height: undefined,
    x: 0,
    y: 0
  }))
}

function resetAllModuleCardSizes() {
  moduleConfig.value = moduleConfig.value.map((item) => ({ ...item, width: undefined, height: undefined, x: 0, y: 0 }))
  moduleConfigDraft.value = moduleConfigDraft.value.map((item) => ({ ...item, width: undefined, height: undefined, x: 0, y: 0 }))
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

function persistModuleCustomizeStrict(source = moduleConfig.value) {
  const simple = [...source]
    .sort((a, b) => a.order - b.order)
    .map((item, index) => ({
      key: item.key,
      visible: item.visible,
      order: index,
      audience: normalizeCardAudience(item.audience),
      width: item.width != null ? clampModuleCardWidth(item.width) : undefined,
      height: item.height != null ? clampModuleCardHeight(item.height) : undefined,
      x: item.x != null ? clampModuleCardOffsetX(item.x) : 0,
      y: item.y != null ? clampModuleCardOffsetY(item.y) : 0
    }))
  localStorage.setItem(moduleStorageKey(), JSON.stringify(simple))
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
      height: item.height != null ? clampModuleCardHeight(item.height) : undefined,
      x: item.x != null ? clampModuleCardOffsetX(item.x) : 0,
      y: item.y != null ? clampModuleCardOffsetY(item.y) : 0
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
        height: undefined,
        x: 0,
        y: 0
      }))
      moduleConfigDraft.value = portalModuleCatalog.map((item, index) => ({
        ...item,
        visible: true,
        order: index,
        audience: normalizeCardAudience(item.audience),
        width: undefined,
        height: undefined,
        x: 0,
        y: 0
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
    visibleStaffIds: Array.isArray(item?.visibleStaffIds) ? item.visibleStaffIds.map((row: any) => String(row)) : [],
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
    customCardForm.visibleStaffIds = []
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
  customCardForm.visibleStaffIds = Array.isArray(target.visibleStaffIds) ? target.visibleStaffIds.map((item) => String(item)) : []
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
  const visibleStaffIds = Array.isArray(customCardForm.visibleStaffIds)
    ? customCardForm.visibleStaffIds.map((item) => String(item))
    : []
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
      audience,
      visibleStaffIds
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
    visibleStaffIds,
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
        height: item.height != null ? clampModuleCardHeight(item.height) : undefined,
        x: item.x != null ? clampModuleCardOffsetX(item.x) : 0,
        y: item.y != null ? clampModuleCardOffsetY(item.y) : 0
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
    const xMap = new Map<string, number>()
    const yMap = new Map<string, number>()
    parsed.moduleConfig.forEach((item: any, index: number) => {
      const key = String(item?.key || '')
      if (!key) return
      visibilityMap.set(key, item?.visible !== false)
      orderMap.set(key, Number.isFinite(Number(item?.order)) ? Number(item.order) : index)
      audienceMap.set(key, normalizeCardAudience(item?.audience))
      const width = Number(item?.width)
      const height = Number(item?.height)
      const x = Number(item?.x)
      const y = Number(item?.y)
      widthMap.set(key, Number.isFinite(width) ? clampModuleCardWidth(width) : undefined)
      heightMap.set(key, Number.isFinite(height) ? clampModuleCardHeight(height) : undefined)
      xMap.set(key, Number.isFinite(x) ? clampModuleCardOffsetX(x) : 0)
      yMap.set(key, Number.isFinite(y) ? clampModuleCardOffsetY(y) : 0)
    })
    moduleConfig.value = portalModuleCatalog
      .map((item, index) => ({
        ...item,
        visible: visibilityMap.has(item.key) ? Boolean(visibilityMap.get(item.key)) : true,
        order: orderMap.has(item.key) ? Number(orderMap.get(item.key)) : index,
        audience: audienceMap.get(item.key) || normalizeCardAudience(item.audience),
        width: widthMap.get(item.key),
        height: heightMap.get(item.key),
        x: xMap.get(item.key) ?? 0,
        y: yMap.get(item.key) ?? 0
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
  scheduleForm.assigneeId = userStore.staffInfo?.id != null ? String(userStore.staffInfo.id) : undefined
  scheduleForm.assigneeName = (userStore.staffInfo?.realName || userStore.staffInfo?.username || '').trim()
  scheduleForm.priority = 'NORMAL'
  scheduleForm.description = ''
  scheduleForm.calendarType = 'WORK'
  scheduleForm.urgency = 'NORMAL'
  scheduleForm.planCategory = '基础办公'
  scheduleForm.eventColor = resolveCalendarTypeColor(scheduleForm.calendarType, scheduleForm.urgency)
  scheduleForm.collaboratorDeptIds = []
  scheduleForm.collaboratorIds = []
  originalCollaboratorIds.value = []
  collaboratorPreviewNames.value = []
  collaboratorPreviewAllNames.value = []
  conflictPreviewItems.value = []
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
  scheduleForm.assigneeId = matched.assigneeId != null ? String(matched.assigneeId) : undefined
  scheduleForm.assigneeName = matched.assigneeName || ''
  scheduleForm.priority = (matched.priority as any) || 'NORMAL'
  scheduleForm.description = matched.description || ''
  scheduleForm.calendarType = matched.calendarType || 'WORK'
  scheduleForm.urgency = matched.urgency || 'NORMAL'
  scheduleForm.planCategory = matched.planCategory || '基础办公'
  scheduleForm.eventColor = resolveCalendarTypeColor(scheduleForm.calendarType, scheduleForm.urgency)
  scheduleForm.collaboratorDeptIds = []
  scheduleForm.collaboratorIds = Array.isArray(matched.collaboratorIds)
    ? matched.collaboratorIds.map((item) => String(item))
    : typeof matched.collaboratorIds === 'string' && matched.collaboratorIds
      ? matched.collaboratorIds.split(',').map((item) => item.trim()).filter(Boolean)
      : []
  originalCollaboratorIds.value = [...scheduleForm.collaboratorIds]
  collaboratorPreviewAllNames.value = []
  conflictPreviewItems.value = []
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
  if (scheduleForm.calendarType === 'COLLAB' && !scheduleForm.collaboratorDeptIds.length && !scheduleForm.collaboratorIds.length) {
    message.warning('协同日历请至少选择部门或协同成员')
    return
  }
  const collaboratorPayload = scheduleForm.calendarType === 'COLLAB'
    ? await resolveCollaborators(scheduleForm.collaboratorDeptIds, scheduleForm.collaboratorIds)
    : { ids: [] as string[], names: [] as string[] }
  const firstStart = dayjs(scheduleForm.startTime).format('YYYY-MM-DDTHH:mm:ss')
  const firstEnd = scheduleForm.endTime ? dayjs(scheduleForm.endTime).format('YYYY-MM-DDTHH:mm:ss') : undefined
  const canContinue = await confirmByConflicts({
    taskId: editingScheduleId.value || undefined,
    startTime: firstStart,
    endTime: firstEnd,
    assigneeName: scheduleForm.assigneeName || undefined,
    collaboratorIds: collaboratorPayload.ids
  })
  if (!canContinue) return
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
        assigneeId: resolveAssigneeIdForSubmit(),
        assigneeName: scheduleForm.assigneeName || undefined,
        calendarType: scheduleForm.calendarType,
        planCategory: scheduleForm.planCategory || undefined,
        urgency: scheduleForm.urgency,
        eventColor: resolveCalendarTypeColor(scheduleForm.calendarType, scheduleForm.urgency),
        collaboratorIds: collaboratorPayload.ids,
        collaboratorNames: collaboratorPayload.names
      })
      upsertCalendarTask(updated)
      scheduleOpen.value = false
      editingScheduleId.value = null
      conflictPreviewItems.value = []
      await refreshAfterCalendarChange()
      emitCalendarSyncPulse('UPDATE')
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
          assigneeId: resolveAssigneeIdForSubmit(),
          assigneeName: scheduleForm.assigneeName || undefined,
          calendarType: scheduleForm.calendarType,
          planCategory: scheduleForm.planCategory || undefined,
          urgency: scheduleForm.urgency,
          eventColor: resolveCalendarTypeColor(scheduleForm.calendarType, scheduleForm.urgency),
          collaboratorIds: collaboratorPayload.ids,
          collaboratorNames: collaboratorPayload.names,
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
    conflictPreviewItems.value = []
    await refreshAfterCalendarChange()
    emitCalendarSyncPulse('CREATE')
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
      await refreshAfterCalendarChange()
      emitCalendarSyncPulse('DONE')
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
      await refreshAfterCalendarChange()
      emitCalendarSyncPulse('DELETE')
      message.success('已删除')
    }
  })
}

async function loadStaffOptions() {
  try {
    await searchStaff('')
  } catch {}
}

async function loadDepartmentOptions() {
  try {
    await searchDepartments('')
  } catch {}
}

async function loadSummary() {
  const data = await getPortalSummary({ silent403: true })
  Object.assign(summary, data)
}

async function loadMyTodoSummary() {
  const [todo, approval, task] = await Promise.all([
    getTodoSummary({ status: 'OPEN', mineOnly: true }),
    getApprovalSummary({ pendingMine: true }),
    getOaTaskSummary({ status: 'OPEN', mineOnly: true })
  ])
  myTodoSummary.openTodoCount = Number(todo?.openCount || 0)
  myTodoSummary.pendingApprovalCount = Number(approval?.pendingCount || 0)
  myTodoSummary.timeoutApprovalCount = Number(approval?.timeoutPendingCount || 0)
  myTodoSummary.ongoingTaskCount = Number(task?.openCount || 0)
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
  dashboard.value = await getDashboardSummary({ silent403: true })
}

async function loadFinanceOverview() {
  financeOverview.value = await getFinanceWorkbenchOverview({ silent403: true })
}

async function loadBedAndElderStatus() {
  const [residence, beds] = await Promise.all([
    getResidenceStatusSummary({ silent403: true }),
    getBedMap({ silent403: true })
  ])
  bedAndElderStatus.inHospital = Number(residence.inHospitalCount || 0)
  bedAndElderStatus.outing = Number(residence.outingCount || 0)
  bedAndElderStatus.medicalObservation = Number(residence.medicalOutingCount || 0)
  bedAndElderStatus.emptyBeds = (beds || []).filter((bed) => resolveBedStatus(bed as BedItem) === '空闲').length
  bedAndElderStatus.cleaningBeds = (beds || []).filter((bed) => resolveBedStatus(bed as BedItem) === '清洁中').length
}

async function loadSalesFunnel() {
  const today = dayjs().format('YYYY-MM-DD')
  const monthStart = dayjs().startOf('month').format('YYYY-MM-DD')

  const [conversion, pendingAssessmentPage, pendingSignPage] = await Promise.all([
    getMarketingConversionReport({ dateFrom: monthStart, dateTo: today }, { silent403: true }),
    getContractPage({ pageNo: 1, pageSize: 1, flowStage: 'PENDING_ASSESSMENT' }, { silent403: true }) as Promise<PageResult<CrmContractItem>>,
    getContractPage({ pageNo: 1, pageSize: 1, flowStage: 'PENDING_SIGN' }, { silent403: true }) as Promise<PageResult<CrmContractItem>>
  ])

  salesFunnel.todayConsultCount = Number(conversion.consultCount || 0)
  salesFunnel.evaluationCount = Number(pendingAssessmentPage.total || 0)
  salesFunnel.pendingSignCount = Number(pendingSignPage.total || 0)
  salesFunnel.monthDealCount = Number(conversion.contractCount || 0)
}

async function loadHrReminder() {
  const [hrData, certPage] = await Promise.all([
    getHrWorkbenchSummary(undefined, { silent403: true }),
    getHrProfileCertificateReminderPage({ pageNo: 1, pageSize: 1 }, { silent403: true })
  ])
  hrSummary.value = hrData
  certificateReminderCount.value = Number(certPage.total || 0)
}

async function loadApprovalTracks() {
  const approvalPage = await getApprovalPage({
    pageNo: 1,
    pageSize: 16,
    status: undefined,
    scope: 'MY'
  })
  approvalRows.value = approvalPage.list || []
}

async function loadBirthdayPlan() {
  const birthdayPage = await getBirthdayPage({
    pageNo: 1,
    pageSize: 500,
    daysAhead: 365
  }, { silent403: true })
  birthdayRows.value = birthdayPage.list || []
}

async function refreshPortalModules(withCalendar = true, source = '自动刷新') {
  if (syncingNow.value) return
  syncingNow.value = true
  lastSyncSource.value = source
  const jobs: Array<Promise<any>> = [
    loadSummary(),
    loadMyTodoSummary(),
    loadDashboard(),
    loadApprovalTracks(),
    loadBirthdayPlan()
  ]
  if (isModuleVisible('finance') || isModuleVisible('expense')) {
    jobs.push(loadFinanceOverview())
  }
  if (isModuleVisible('salesFunnel')) {
    jobs.push(loadSalesFunnel())
  }
  if (isModuleVisible('bedStatus') || isModuleVisible('operation')) {
    jobs.push(loadBedAndElderStatus())
  }
  if (
    currentUserAudience.value.includes('HR') ||
    currentUserAudience.value.includes('ADMIN') ||
    currentUserAudience.value.includes('DIRECTOR')
  ) {
    jobs.push(loadHrReminder())
  }
  if (withCalendar) {
    jobs.push(loadCalendar())
  }
  await Promise.allSettled(jobs)
  refreshedAt.value = dayjs().format('YYYY-MM-DD HH:mm:ss')
  syncingNow.value = false
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
    loadReminderState()
    loadAutoSyncSetting()
    await loadThresholdConfig()
    updateSearchOptions(searchKeyword.value)
    await Promise.all([loadStaffOptions(), loadDepartmentOptions()])
    await refreshPortalModules(true, '初始化加载')
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
    if (!autoSyncEnabled.value) return
    refreshPortalModules(true, '跨模块联动').catch(() => {})
  },
  debounceMs: 800
})

watch(autoSyncEnabled, (value) => {
  persistAutoSyncSetting()
  if (value) {
    refreshPortalModules(true, '自动刷新开启').catch(() => {})
  }
})

watch(
  () => visibleCalendarTypes.value,
  (value) => {
    try {
      localStorage.setItem(VISIBLE_TYPES_STORAGE_KEY, JSON.stringify(value))
    } catch {}
  },
  { deep: true }
)

watch(
  () => currentCalendarView.value,
  (value) => {
    try {
      localStorage.setItem(CALENDAR_VIEW_STORAGE_KEY, value)
    } catch {}
  }
)

watch(
  () => conflictPolicy.value,
  (value) => {
    try {
      localStorage.setItem(CONFLICT_POLICY_STORAGE_KEY, value)
    } catch {}
  }
)

watch(
  () => [
    scheduleForm.calendarType,
    scheduleForm.collaboratorDeptIds.join(','),
    scheduleForm.collaboratorIds.join(',')
  ],
  () => {
    refreshCollaboratorPreview().catch(() => {})
  },
  { immediate: true }
)

watch(
  () => [
    scheduleForm.startTime ? dayjs(scheduleForm.startTime).format('YYYY-MM-DDTHH:mm:ss') : '',
    scheduleForm.endTime ? dayjs(scheduleForm.endTime).format('YYYY-MM-DDTHH:mm:ss') : '',
    scheduleForm.assigneeName,
    scheduleForm.collaboratorIds.join(','),
    scheduleForm.collaboratorDeptIds.join(',')
  ],
  () => {
    conflictPreviewItems.value = []
  }
)

watch(riskReminders, (list) => {
  let changed = false
  const next = { ...reminderState.value }
  list.forEach((item) => {
    const key = reminderKey(item)
    const currentCount = Number(item.count || 0)
    const previous = next[key]
    if (!previous) {
      next[key] = { read: false, pinned: false, lastCount: currentCount }
      changed = true
      return
    }
    const previousCount = Number(previous.lastCount || 0)
    if (currentCount > previousCount) {
      next[key] = { ...previous, read: false, lastCount: currentCount, updatedAt: dayjs().format('YYYY-MM-DDTHH:mm:ss') }
      changed = true
      return
    }
    if (currentCount !== previousCount) {
      next[key] = { ...previous, lastCount: currentCount }
      changed = true
    }
  })
  if (changed) {
    reminderState.value = next
    persistReminderState()
  }
}, { deep: true })

onMounted(() => {
  syncVisibleTypesFromStorage()
  syncCalendarViewFromStorage()
  syncConflictPolicyFromStorage()
  init()
  if (portalSyncTimer) window.clearInterval(portalSyncTimer)
  portalSyncTimer = window.setInterval(() => {
    if (document.hidden || !autoSyncEnabled.value) return
    refreshPortalModules(true, '定时轮询').catch(() => {})
  }, 20 * 1000)
  portalVisibleHandler = () => {
    if (!document.hidden && autoSyncEnabled.value) refreshPortalModules(true, '回到页面').catch(() => {})
  }
  document.addEventListener('visibilitychange', portalVisibleHandler)
  calendarStorageHandler = (event: StorageEvent) => {
    if (event.key === thresholdPulseKey()) {
      loadThresholdConfig().catch(() => {})
      message.info('阈值配置已由其他页面更新')
      return
    }
    if (event.key === VISIBLE_TYPES_STORAGE_KEY) {
      syncVisibleTypesFromStorage()
      return
    }
    if (event.key === CALENDAR_VIEW_STORAGE_KEY) {
      syncCalendarViewFromStorage()
      return
    }
    if (event.key === CONFLICT_POLICY_STORAGE_KEY) {
      syncConflictPolicyFromStorage()
      return
    }
    if (event.key === CALENDAR_SYNC_PULSE_KEY) {
      refreshPortalModules(true, '跨页面同步').catch(() => {})
    }
  }
  window.addEventListener('storage', calendarStorageHandler)
  portalHotkeyHandler = (event: KeyboardEvent) => {
    const key = String(event.key || '').toLowerCase()
    if ((event.ctrlKey || event.metaKey) && key === 'k') {
      event.preventDefault()
      focusGlobalSearch()
      return
    }
    if (event.key === '/' && !isEditableTarget(event.target)) {
      event.preventDefault()
      focusGlobalSearch()
      return
    }
    if (event.key === 'Escape' && !isEditableTarget(event.target) && searchKeyword.value) {
      searchKeyword.value = ''
      updateSearchOptions('')
    }
  }
  window.addEventListener('keydown', portalHotkeyHandler)
})

onBeforeUnmount(() => {
  if (portalSyncTimer) window.clearInterval(portalSyncTimer)
  if (portalVisibleHandler) document.removeEventListener('visibilitychange', portalVisibleHandler)
  if (calendarStorageHandler) window.removeEventListener('storage', calendarStorageHandler)
  if (portalHotkeyHandler) window.removeEventListener('keydown', portalHotkeyHandler)
  portalVisibleHandler = null
  calendarStorageHandler = null
  portalHotkeyHandler = null
})
</script>

<style scoped>
.portal-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 0;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  letter-spacing: -0.01em;
}

.hero-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  border-radius: 20px;
  background: linear-gradient(135deg, #f0f7ff 0%, #e1effe 100%);
  border: 1px solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 10px 30px rgba(30, 58, 138, 0.06), inset 0 2px 20px rgba(255, 255, 255, 0.6);
  position: relative;
  overflow: hidden;
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
}

.hero-card::after {
  content: '';
  position: absolute;
  right: -56px;
  top: -48px;
  width: 220px;
  height: 220px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(37, 99, 235, 0.16) 0%, rgba(37, 99, 235, 0) 70%);
  pointer-events: none;
}

.portal-page :deep(.ant-card .ant-card-body) {
  padding: 16px 20px;
}

.portal-page :deep(.module-card .ant-card-head) {
  min-height: 50px;
  padding-inline: 14px;
  border-bottom: 1px solid #e5edf8;
  background: linear-gradient(180deg, rgba(248, 251, 255, 0.9) 0%, rgba(255, 255, 255, 0.98) 100%);
}

.portal-page :deep(.module-card .ant-card-head-wrapper) {
  align-items: flex-start;
  gap: 10px;
}

.portal-page :deep(.module-card .ant-card-head-title) {
  white-space: normal;
  line-height: 1.3;
  padding: 14px 0;
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
  letter-spacing: -0.3px;
}

.portal-page :deep(.module-card .ant-card-extra) {
  padding: 8px 0;
  max-width: 100%;
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

.hero-kpi-item.clickable {
  cursor: pointer;
}

.hero-kpi-item.clickable:hover {
  border-color: #91caff;
  background: #f0f7ff;
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
  position: relative;
  z-index: 1;
  min-width: 360px;
}

.hero-actions-main {
  width: 100%;
  display: flex;
  justify-content: flex-end;
}

.hero-actions-meta {
  width: 100%;
  display: flex;
  justify-content: flex-end;
}

.hero-actions-tip {
  font-size: 12px;
  color: #64748b;
}

.hero-actions-tip-row {
  margin-top: 2px;
}

.hero-action-btns :deep(.ant-btn) {
  border-radius: 999px;
  border-color: rgba(59, 130, 246, 0.32);
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.06);
  min-width: 96px;
}

.hero-action-btns :deep(.ant-btn-primary) {
  box-shadow: 0 10px 22px rgba(27, 102, 214, 0.28);
  min-width: 132px;
}

.hero-search {
  margin-top: 10px;
}

.hero-search-hint {
  margin-top: 6px;
  font-size: 12px;
  color: #64748b;
}

.section-row {
  margin-top: 0;
}

.module-card {
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.7);
  transition: all 0.3s cubic-bezier(0.165, 0.84, 0.44, 1);
  position: relative;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.65);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.04);
}

.todo-focus-card {
  background:
    linear-gradient(135deg, rgba(239, 246, 255, 0.9), rgba(255, 255, 255, 0.95)),
    linear-gradient(130deg, rgba(59, 130, 246, 0.15), rgba(56, 189, 248, 0.05));
  border-color: rgba(191, 219, 254, 0.8);
  box-shadow: 0 10px 30px rgba(59, 130, 246, 0.08);
}

.todo-focus-card :deep(.ant-card-head) {
  border-bottom-color: #dbeafe;
}

.module-card:hover {
  border-color: rgba(255, 255, 255, 1);
  box-shadow: 0 16px 40px rgba(37, 99, 235, 0.08);
  transform: translateY(-4px);
  background: rgba(255, 255, 255, 0.85);
}

.full-height {
  height: 100%;
}

.metric-cell {
  background: rgba(255, 255, 255, 0.75);
  border: 1px solid rgba(255, 255, 255, 0.9);
  border-radius: 12px;
  padding: 16px;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.02);
  transition: all 0.3s cubic-bezier(0.165, 0.84, 0.44, 1);
  backdrop-filter: blur(10px);
}

.metric-cell:hover {
  border-color: #93c5fd;
  background: rgba(255, 255, 255, 0.95);
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(59, 130, 246, 0.08);
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

.approval-track-card :deep(.ant-card-head) {
  min-height: 34px;
}

.approval-track-card :deep(.ant-card-head-title) {
  padding: 8px 0;
  font-size: 13px;
}

.legend-line {
  margin-top: 8px;
}

.birthday-filter-bar {
  margin-top: 10px;
  padding: 8px 10px;
  border: 1px dashed #bfdbfe;
  border-radius: 10px;
  background: #f8fbff;
}

.reminder-count {
  min-width: 42px;
  text-align: right;
  color: #475569;
}

.reminder-card :deep(.ant-card-head-wrapper) {
  flex-wrap: wrap;
}

.reminder-extra {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
  max-width: min(560px, 100%);
}

.reminder-extra :deep(.ant-radio-group) {
  max-width: 100%;
}

.reminder-extra :deep(.ant-radio-group .ant-radio-button-wrapper) {
  font-size: 12px;
  padding-inline: 10px;
}

.reminder-unread {
  background: rgba(239, 246, 255, 0.78);
}

.quick-group {
  border: 1px dashed #bfdbfe;
  border-radius: 10px;
  background: #f8fbff;
  padding: 10px;
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
  padding: 10px;
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

.calendar-toolbar {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 10px;
  padding: 10px 12px;
  border: 1px solid #deebff;
  border-radius: 12px;
  background: linear-gradient(135deg, #f7fbff 0%, #eef6ff 100%);
}

.calendar-insights {
  margin-bottom: 2px;
}

.calendar-loads {
  margin-bottom: 2px;
}

.calendar-week-trend {
  display: flex;
  align-items: flex-end;
  gap: 6px;
}

.calendar-week-bar {
  min-width: 16px;
  padding: 2px 4px;
  border-radius: 6px 6px 2px 2px;
  background: linear-gradient(180deg, #91caff 0%, #1677ff 100%);
  color: #fff;
  font-size: 11px;
  display: flex;
  align-items: flex-end;
  justify-content: center;
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

.calendar-card :deep(.fc) {
  --fc-border-color: #e2e8f0;
  --fc-page-bg-color: transparent;
  --fc-neutral-bg-color: #f8fafc;
  --fc-event-border-color: transparent;
}

.calendar-card :deep(.fc .fc-toolbar.fc-header-toolbar) {
  margin-bottom: 10px;
  padding: 8px 10px;
  border: 1px solid #dbeafe;
  border-radius: 10px;
  background: #f8fbff;
}

.calendar-card :deep(.fc .fc-button) {
  border-radius: 999px;
  border: 1px solid #bfdbfe;
  background: #ffffff;
  color: #1e3a8a;
  box-shadow: none;
}

.calendar-card :deep(.fc .fc-button:hover) {
  background: #eff6ff;
  border-color: #93c5fd;
}

.calendar-card :deep(.fc .fc-col-header-cell-cushion) {
  color: #334155;
  font-weight: 600;
}

.calendar-card :deep(.fc .fc-daygrid-day-number) {
  color: #64748b;
  font-weight: 600;
}

.calendar-card :deep(.fc .fc-daygrid-day.fc-day-today) {
  background: rgba(219, 234, 254, 0.56);
}

.calendar-card :deep(.fc .fc-daygrid-event) {
  border-radius: 8px;
  padding: 1px 4px;
  box-shadow: 0 2px 8px rgba(30, 64, 175, 0.14);
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

.portal-page :deep(.calendar-event-done .fc-event-title) {
  text-decoration: line-through;
}

.portal-page :deep(.calendar-event-done) {
  opacity: 0.82;
}

.approval-opinion-text {
  max-width: 280px;
  color: #334155;
}

.threshold-modal-preset {
  margin-top: 2px;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

@media (max-width: 768px) {
  .hero-card {
    flex-direction: column;
    align-items: flex-start;
  }

  .hero-actions {
    width: 100%;
    min-width: 0;
    align-items: flex-start;
  }

  .hero-actions-main,
  .hero-actions-meta {
    width: 100%;
    justify-content: flex-start;
  }

  .hero-kpi-item {
    min-width: 84px;
  }

  .reminder-extra {
    align-items: flex-start;
    width: 100%;
  }

  .portal-page :deep(.module-card .ant-card-head-wrapper) {
    flex-wrap: wrap;
  }
}
</style>
