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
            <a-button @click="go('/hr/oa/albums')">相册管理</a-button>
            <a-button @click="go('/hr/oa/knowledge')">知识库</a-button>
            <a-button @click="go('/hr/oa/policies')">规章制度库</a-button>
            <a-button @click="go('/hr/oa/policy-alerts')">制度更新预警</a-button>
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
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import { getHrWorkbenchSummary } from '../../api/hr'

const router = useRouter()
const summary = reactive({
  onJobCount: 0,
  leftCount: 0,
  todayTrainingCount: 0,
  pendingLeaveApprovalCount: 0,
  attendanceAbnormalCount: 0,
  contractExpiringCount: 0
})

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
  } catch (_error) {
    summary.onJobCount = 0
    summary.leftCount = 0
    summary.todayTrainingCount = 0
    summary.pendingLeaveApprovalCount = 0
    summary.attendanceAbnormalCount = 0
    summary.contractExpiringCount = 0
  }
}

onMounted(loadSummary)
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
