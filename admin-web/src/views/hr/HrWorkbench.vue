<template>
  <PageContainer title="人事行政工作台" subTitle="招聘、档案、考勤、OA、培训、绩效一体化工作入口">
    <a-card :bordered="false" class="card-elevated" style="margin-bottom: 12px" title="卡片1：今日人力概况">
      <a-row :gutter="16">
        <a-col :xs="12" :md="4">
          <div class="stat-clickable" @click="go('/hr/profile/basic?status=1')">
            <a-statistic title="在职人数" :value="summary.onJobCount" />
          </div>
        </a-col>
        <a-col :xs="12" :md="4">
          <div class="stat-clickable" @click="go('/hr/profile/basic?status=0')">
            <a-statistic title="离职人数" :value="summary.leftCount" />
          </div>
        </a-col>
        <a-col :xs="12" :md="4">
          <div class="stat-clickable" @click="go('/hr/development/records?date=today')">
            <a-statistic title="今日培训" :value="summary.todayTrainingCount" />
          </div>
        </a-col>
        <a-col :xs="12" :md="4">
          <div class="stat-clickable" @click="go('/hr/attendance/leave-approval?type=LEAVE&status=PENDING')">
            <a-statistic title="请假审批待办" :value="summary.pendingLeaveApprovalCount" />
          </div>
        </a-col>
        <a-col :xs="12" :md="4">
          <div class="stat-clickable" @click="go('/hr/attendance/abnormal?date=today')">
            <a-statistic title="考勤异常" :value="summary.attendanceAbnormalCount" />
          </div>
        </a-col>
        <a-col :xs="12" :md="4">
          <div class="stat-clickable" @click="go('/hr/profile/contract-reminders?range=30d')">
            <a-statistic title="合同到期预警" :value="summary.contractExpiringCount" />
          </div>
        </a-col>
        <a-col :xs="12" :md="4">
          <div class="stat-clickable" @click="openBirthdayModal('TODAY')">
            <a-statistic title="今日生日" :value="summary.birthdayTodayCount" />
          </div>
        </a-col>
        <a-col :xs="12" :md="4">
          <div class="stat-clickable" @click="openBirthdayModal('NEXT7')">
            <a-statistic title="7天内生日" :value="summary.birthdayUpcomingCount" />
          </div>
        </a-col>
        <a-col :xs="12" :md="4">
          <div class="stat-clickable" @click="go('/oa/todo?keyword=生日提醒')">
            <a-statistic title="生日待办" :value="summary.birthdayTodoCount" :value-style="{ color: (summary.birthdayTodoCount || 0) > 0 ? '#cf1322' : undefined }" />
          </div>
        </a-col>
      </a-row>
    </a-card>

    <a-row :gutter="12">
      <a-col :xs="24" :xl="8" style="margin-bottom: 12px">
        <a-card :bordered="false" class="card-elevated" title="招聘管理">
          <a-space wrap>
            <a-button type="primary" @click="go('/hr/recruitment/needs')">招聘需求申请</a-button>
            <a-button @click="go('/hr/recruitment/job-posting')">岗位发布</a-button>
            <a-button @click="go('/hr/recruitment/candidates')">候选人库</a-button>
            <a-button @click="go('/hr/recruitment/interviews')">面试管理</a-button>
            <a-button @click="go('/hr/recruitment/onboarding')">入职办理</a-button>
            <a-button @click="go('/hr/recruitment/materials')">入职资料收集</a-button>
          </a-space>
        </a-card>
      </a-col>
      <a-col :xs="24" :xl="8" style="margin-bottom: 12px">
        <a-card :bordered="false" class="card-elevated" title="员工档案中心">
          <a-space wrap>
            <a-button type="primary" @click="go('/hr/profile/basic')">员工基本信息</a-button>
            <a-button @click="go('/hr/profile/contracts')">劳动合同管理</a-button>
            <a-button @click="go('/hr/profile/contract-templates')">合同模板库</a-button>
            <a-button @click="go('/hr/profile/contract-print')">合同打印</a-button>
            <a-button @click="go('/hr/profile/certificates')">证书上传</a-button>
            <a-button @click="go('/hr/profile/training-records')">培训记录</a-button>
            <a-button @click="go('/hr/profile/attachments')">档案附件</a-button>
            <a-button @click="go('/hr/profile/contract-reminders')">合同到期提醒</a-button>
          </a-space>
        </a-card>
      </a-col>
      <a-col :xs="24" :xl="8" style="margin-bottom: 12px">
        <a-card :bordered="false" class="card-elevated" title="排班与考勤管理">
          <a-space wrap>
            <a-button type="primary" @click="go('/hr/attendance/schemes')">排班方案</a-button>
            <a-button @click="go('/hr/attendance/groups')">班组设置</a-button>
            <a-button @click="go('/hr/attendance/calendar')">排班日历</a-button>
            <a-button @click="go('/hr/attendance/leave-approval')">请假审批</a-button>
            <a-button @click="go('/hr/attendance/shift-change')">调班申请</a-button>
            <a-button @click="go('/hr/attendance/overtime')">加班申请</a-button>
            <a-button @click="go('/hr/attendance/records')">考勤记录</a-button>
            <a-button @click="go('/hr/attendance/abnormal')">考勤异常</a-button>
            <a-button @click="go('/hr/attendance/access-control')">门禁对接</a-button>
            <a-button @click="go('/hr/attendance/card-sync')">一卡通数据对接</a-button>
          </a-space>
        </a-card>
      </a-col>
      <a-col :xs="24" :xl="8" style="margin-bottom: 12px">
        <a-card :bordered="false" class="card-elevated" title="行政协同（OA）">
          <a-space wrap>
            <a-button type="primary" @click="go('/hr/oa/notices')">通知管理</a-button>
            <a-button @click="go('/hr/oa/tasks')">任务管理</a-button>
            <a-button @click="go('/hr/oa/execution')">工作执行</a-button>
            <a-button @click="go('/hr/oa/activity-plan')">活动计划</a-button>
            <a-button @click="go('/oa/document')">文档管理</a-button>
            <a-button @click="go('/hr/oa/knowledge')">知识库</a-button>
            <a-button @click="go('/hr/oa/policies')">规章制度库</a-button>
            <a-button @click="go('/hr/oa/policy-alerts')">制度更新预警</a-button>
            <a-badge :count="summary.birthdayTodoCount || 0" :offset="[2, -2]">
              <a-button @click="go('/oa/todo?keyword=生日提醒')">生日提醒待办</a-button>
            </a-badge>
            <a-button @click="go('/hr/oa/groups')">分组设置</a-button>
          </a-space>
        </a-card>
      </a-col>
      <a-col :xs="24" :xl="8" style="margin-bottom: 12px">
        <a-card :bordered="false" class="card-elevated" title="费用与报销">
          <a-space wrap>
            <a-button type="primary" @click="go('/hr/expense/training-reimburse')">外出培训费用报销</a-button>
            <a-button @click="go('/hr/expense/subsidy')">补贴申请</a-button>
            <a-button @click="go('/hr/expense/meal-fee')">员工餐费</a-button>
            <a-button @click="go('/hr/expense/electricity-fee')">员工电费</a-button>
            <a-button @click="go('/hr/expense/salary-subsidy')">工资补贴记录</a-button>
            <a-button @click="go('/hr/expense/approval-flow')">报销审批流</a-button>
          </a-space>
        </a-card>
      </a-col>
      <a-col :xs="24" :xl="8" style="margin-bottom: 12px">
        <a-card :bordered="false" class="card-elevated" title="培训与发展 / 绩效考核">
          <a-space wrap>
            <a-button type="primary" @click="go('/hr/development/plans')">培训计划</a-button>
            <a-button @click="go('/hr/development/enrollments')">培训报名</a-button>
            <a-button @click="go('/hr/development/signin')">培训签到</a-button>
            <a-button @click="go('/hr/development/records')">培训记录</a-button>
            <a-button @click="go('/hr/development/certificates')">证书管理</a-button>
            <a-button @click="go('/hr/development/certificate-reminders')">证书到期提醒</a-button>
            <a-button @click="go('/hr/performance/nursing')">护理绩效</a-button>
            <a-button @click="go('/hr/performance/sales')">销售绩效</a-button>
            <a-button @click="go('/hr/performance/admin')">行政绩效</a-button>
            <a-button @click="go('/hr/performance/scoring-rules')">评分规则配置</a-button>
            <a-button @click="go('/hr/performance/generation')">绩效生成</a-button>
            <a-button @click="go('/hr/performance/reports')">绩效报表</a-button>
            <a-button @click="go('/hr/performance/reward-punishment')">奖惩记录</a-button>
          </a-space>
        </a-card>
      </a-col>
    </a-row>

    <a-modal
      v-model:open="birthdayModalOpen"
      :title="birthdayModalTitle"
      width="880px"
      :footer="null"
      destroy-on-close
    >
      <a-table
        :columns="birthdayColumns"
        :data-source="birthdayRows"
        :loading="birthdayLoading"
        row-key="staffId"
        :pagination="false"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="Number(record.status) === 1 ? 'green' : 'default'">{{ Number(record.status) === 1 ? '在职' : '离职' }}</a-tag>
          </template>
        </template>
      </a-table>
      <div style="margin-top: 12px; display: flex; justify-content: flex-end">
        <a-button @click="printBirthdayRows">打印名单</a-button>
      </div>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import { getHrStaffBirthdayPage, getHrWorkbenchSummary } from '../../api/hr'
import { openPrintTableReport } from '../../utils/print'
import { useLiveSyncRefresh } from '../../composables/useLiveSyncRefresh'
import type { HrStaffBirthdayItem } from '../../types'

const router = useRouter()
const summary = reactive({
  onJobCount: 0,
  leftCount: 0,
  todayTrainingCount: 0,
  pendingLeaveApprovalCount: 0,
  attendanceAbnormalCount: 0,
  contractExpiringCount: 0,
  birthdayTodayCount: 0,
  birthdayUpcomingCount: 0,
  birthdayTodoCount: 0
})

const birthdayModalOpen = ref(false)
const birthdayScope = ref<'TODAY' | 'NEXT7'>('TODAY')
const birthdayRows = ref<HrStaffBirthdayItem[]>([])
const birthdayLoading = ref(false)
const birthdayColumns = [
  { title: '工号', dataIndex: 'staffNo', key: 'staffNo', width: 110 },
  { title: '姓名', dataIndex: 'realName', key: 'realName', width: 120 },
  { title: '生日', dataIndex: 'birthday', key: 'birthday', width: 120 },
  { title: '下次生日', dataIndex: 'nextBirthday', key: 'nextBirthday', width: 120 },
  { title: '剩余天数', dataIndex: 'daysUntil', key: 'daysUntil', width: 100 },
  { title: '手机号', dataIndex: 'phone', key: 'phone', width: 150 },
  { title: '岗位', dataIndex: 'jobTitle', key: 'jobTitle', width: 140 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 90 }
]
const birthdayModalTitle = computed(() => (birthdayScope.value === 'TODAY' ? '今日生日名单' : '近7天生日名单'))

function go(path: string) {
  router.push(path)
}

async function loadSummary() {
  try {
    const res = await getHrWorkbenchSummary({ warningDays: 30 })
    summary.onJobCount = res.onJobCount || 0
    summary.leftCount = res.leftCount || 0
    summary.todayTrainingCount = res.todayTrainingCount || 0
    summary.pendingLeaveApprovalCount = res.pendingLeaveApprovalCount || 0
    summary.attendanceAbnormalCount = res.attendanceAbnormalCount || 0
    summary.contractExpiringCount = res.contractExpiringCount || 0
    summary.birthdayTodayCount = res.birthdayTodayCount || 0
    summary.birthdayUpcomingCount = res.birthdayUpcomingCount || 0
    summary.birthdayTodoCount = res.birthdayTodoCount || 0
  } catch (_error) {
    summary.onJobCount = 0
    summary.leftCount = 0
    summary.todayTrainingCount = 0
    summary.pendingLeaveApprovalCount = 0
    summary.attendanceAbnormalCount = 0
    summary.contractExpiringCount = 0
    summary.birthdayTodayCount = 0
    summary.birthdayUpcomingCount = 0
    summary.birthdayTodoCount = 0
  }
}

async function openBirthdayModal(scope: 'TODAY' | 'NEXT7') {
  birthdayScope.value = scope
  birthdayModalOpen.value = true
  birthdayLoading.value = true
  try {
    const page = await getHrStaffBirthdayPage({
      pageNo: 1,
      pageSize: 200,
      scope,
      onJobOnly: true
    })
    birthdayRows.value = page.list || []
  } catch {
    birthdayRows.value = []
  } finally {
    birthdayLoading.value = false
  }
}

function printBirthdayRows() {
  if (!birthdayRows.value.length) {
    return
  }
  openPrintTableReport({
    title: birthdayModalTitle.value,
    subtitle: `打印时间：${dayjs().format('YYYY-MM-DD HH:mm:ss')}`,
    columns: [
      { key: 'staffNo', title: '工号' },
      { key: 'realName', title: '姓名' },
      { key: 'birthday', title: '生日' },
      { key: 'nextBirthday', title: '下次生日' },
      { key: 'daysUntil', title: '剩余天数' },
      { key: 'phone', title: '手机号' },
      { key: 'jobTitle', title: '岗位' }
    ],
    rows: birthdayRows.value as any
  })
}

onMounted(loadSummary)

useLiveSyncRefresh({
  topics: ['hr', 'oa', 'system'],
  refresh: () => {
    loadSummary().catch(() => {})
  },
  debounceMs: 900
})
</script>

<style scoped>
.stat-clickable {
  border-radius: 8px;
  padding: 8px;
  cursor: pointer;
}

.stat-clickable:hover {
  background: rgba(24, 144, 255, 0.06);
}
</style>
