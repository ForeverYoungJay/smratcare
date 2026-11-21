<template>
  <WorkbenchModuleCard
    :title="managementView ? '医务部今日运行' : '我的今日医务'"
    :eyebrow="managementView ? '部长视图' : '员工视图'"
    class="medical-workbench"
  >
    <template #extra>
      <a-space size="small" wrap>
        <StatusTag :text="detail.scopeLabel" :tone="managementView ? 'normal' : 'pending'" />
        <a-button type="link" @click="open(managementView ? '/medical-care/unified-task-center' : '/medical-care/rounds')">
          {{ managementView ? '进入医务任务' : '进入今日巡诊' }}
        </a-button>
      </a-space>
    </template>

    <div class="medical-command-bar">
      <div class="medical-command-bar__copy">
        <span>{{ managementView ? 'DEPARTMENT ROUND' : 'TODAY FIRST' }}</span>
        <strong>{{ managementView ? '先处理未闭环异常，再协调巡诊、医嘱与用药。' : '先完成巡诊、医嘱和用药，再记录异常与交接重点。' }}</strong>
        <p>{{ managementView ? '部门数字来自医务汇总接口，任务队列按风险优先。' : '个人任务后端口径未接入时，不展示部门数据冒充“我的待办”。' }}</p>
      </div>
      <div class="medical-actions" aria-label="医务常用操作">
        <button v-for="action in visibleActions" :key="action.path" type="button" @click="open(action.path)">
          <span aria-hidden="true">{{ action.icon }}</span>
          <strong>{{ action.label }}</strong>
        </button>
      </div>
    </div>

    <template v-if="managementView">
      <div class="medical-columns">
        <section class="medical-panel" aria-labelledby="medical-queue-heading">
          <div class="medical-panel__head">
            <div><span>风险优先</span><h4 id="medical-queue-heading">部门处置队列</h4></div>
            <StatusTag :text="`${detail.tasks.length} 项预览`" :tone="detail.tasks.length ? 'warning' : 'offline'" />
          </div>
          <div v-if="detail.tasks.length" class="medical-list">
            <button v-for="task in detail.tasks" :key="String(task.id)" type="button" class="medical-task" @click="open(task.path)">
              <span class="medical-task__time">{{ taskTime(task.plannedTime) }}</span>
              <div>
                <strong>{{ task.title }}</strong>
                <span>{{ task.residentName }} · {{ task.moduleLabel }} · {{ task.assignee || '待认领' }}</span>
              </div>
              <StatusTag :text="task.overdue ? '已超时' : task.statusLabel" :tone="taskTone(task)" />
            </button>
          </div>
          <a-empty v-else description="当前没有需要处置的医务任务">
            <a-button @click="open('/medical-care/unified-task-center')">查看任务中心</a-button>
          </a-empty>
        </section>

        <section class="medical-panel" aria-labelledby="medical-risk-heading">
          <div class="medical-panel__head">
            <div><span>重点关注</span><h4 id="medical-risk-heading">高风险长者</h4></div>
            <StatusTag :text="`${detail.riskResidents.length} 位`" :tone="detail.riskResidents.length ? 'danger' : 'offline'" />
          </div>
          <div v-if="detail.riskResidents.length" class="medical-list">
            <button v-for="resident in detail.riskResidents" :key="String(resident.id || resident.name)" type="button" class="medical-resident" @click="open(resident.path)">
              <span class="medical-resident__mark" aria-hidden="true">{{ resident.name.slice(0, 1) }}</span>
              <div><strong>{{ resident.name }}</strong><span>{{ resident.riskFactors }}</span></div>
              <StatusTag :text="resident.riskLabel" :tone="riskTone(resident.riskLevel)" />
            </button>
          </div>
          <a-empty v-else description="当前汇总中没有高风险长者">
            <a-button @click="open('/elder/list')">查询长者健康档案</a-button>
          </a-empty>
        </section>
      </div>

      <section class="medical-panel medical-panel--coordination" aria-labelledby="medical-team-heading">
        <div class="medical-panel__head">
          <div><span>部门协调</span><h4 id="medical-team-heading">人员任务与闭环情况</h4></div>
          <StatusTag text="数据待接入" tone="offline" />
        </div>
        <a-alert type="info" show-icon message="当前后端暂无按医务人员聚合的分配与完成情况接口" description="不从机构任务队列推算个人绩效；可进入任务中心核对具体任务和责任人。" />
        <div class="medical-coordination-actions">
          <a-button type="primary" @click="open('/medical-care/unified-task-center')">协调医务任务</a-button>
          <a-button v-if="canAccess('/workbench/approvals')" @click="open('/workbench/approvals')">处理待审批</a-button>
          <a-button v-if="canAccess('/medical-care/handovers')" @click="open('/medical-care/handovers')">查看医护交接</a-button>
        </div>
      </section>
    </template>

    <template v-else>
      <a-alert
        v-if="!detail.personalTaskDataAvailable"
        class="medical-scope-alert"
        type="info"
        show-icon
        message="个人巡诊、医嘱和用药待办暂缺可靠的数据范围接口"
        description="工作台不会展示医务部汇总数字冒充个人任务；请从下面的业务入口进入现有列表处理。"
      />
      <div class="medical-columns medical-columns--employee">
        <section class="medical-panel" aria-labelledby="medical-next-heading">
          <div class="medical-panel__head">
            <div><span>工作顺序</span><h4 id="medical-next-heading">今天建议这样处理</h4></div>
          </div>
          <ol class="medical-steps">
            <li><strong>01</strong><div><b>巡诊与巡检</b><span>先处理当日巡诊、复测和异常跟进。</span></div></li>
            <li><strong>02</strong><div><b>医嘱与用药</b><span>核对长者、药品、剂量和执行时间。</span></div></li>
            <li><strong>03</strong><div><b>记录与交接</b><span>补齐健康记录，把未闭环风险带入交班。</span></div></li>
          </ol>
        </section>

        <section class="medical-panel" aria-labelledby="medical-record-heading">
          <div class="medical-panel__head">
            <div><span>真实留痕</span><h4 id="medical-record-heading">我今天登记的用药</h4></div>
            <StatusTag :text="`${detail.recentMedicationRecords.length} 条`" :tone="detail.recentMedicationRecords.length ? 'normal' : 'offline'" />
          </div>
          <div v-if="detail.recentMedicationRecords.length" class="medical-list">
            <button v-for="record in detail.recentMedicationRecords" :key="String(record.id)" type="button" class="medical-record" @click="open(record.elderId ? `/medical-care/medication-registration?elderId=${record.elderId}` : '/medical-care/medication-registration')">
              <span>{{ taskTime(record.registerTime) }}</span>
              <div><strong>{{ record.elderName }} · {{ record.drugName }}</strong><span>登记剂量 {{ record.dosage }}</span></div>
              <StatusTag text="已登记" tone="normal" />
            </button>
          </div>
          <a-empty v-else description="今天暂无本人用药登记记录">
            <a-button @click="open('/medical-care/medication-registration')">进入用药登记</a-button>
          </a-empty>
        </section>
      </div>
    </template>
  </WorkbenchModuleCard>
</template>

<script setup lang="ts">
import dayjs from 'dayjs'
import StatusTag from '../smartcare/StatusTag.vue'
import WorkbenchModuleCard from '../smartcare/WorkbenchModuleCard.vue'
import type { MedicalWorkbenchDetail, MedicalWorkbenchTask } from '../../workbench/medical'

const props = defineProps<{
  detail: MedicalWorkbenchDetail
  managementView: boolean
  canAccess: (path: string) => boolean
}>()

const emit = defineEmits<{ open: [path: string] }>()

const actions = [
  { label: '今日巡诊', icon: '诊', path: '/medical-care/rounds', managementOnly: false },
  { label: '医嘱管理', icon: '嘱', path: '/medical-care/orders', managementOnly: false },
  { label: '用药登记', icon: '药', path: '/medical-care/medication-registration', managementOnly: false },
  { label: '健康档案', icon: '档', path: '/health/management/archive', managementOnly: false },
  { label: '异常跟进', icon: '异', path: '/medical-care/inspection', managementOnly: false },
  { label: '部门处置', icon: '处', path: '/medical-care/unified-task-center', managementOnly: true },
  { label: '医护交接', icon: '交', path: '/medical-care/handovers', managementOnly: false },
  { label: '设备告警', icon: '警', path: '/medical-care/smart-alerts', managementOnly: true },
  { label: '待我审批', icon: '审', path: '/workbench/approvals', managementOnly: true }
]

const visibleActions = actions.filter((item) => (!item.managementOnly || props.managementView) && props.canAccess(item.path))

function open(path: string) { emit('open', path) }

function taskTime(value?: string) {
  const parsed = dayjs(value)
  return parsed.isValid() ? parsed.format('HH:mm') : '--:--'
}

function taskTone(task: MedicalWorkbenchTask) {
  if (task.overdue || task.priority === 'HIGH') return 'danger'
  if (task.status === 'DONE' || task.status === 'CLOSED') return 'normal'
  return 'pending'
}

function riskTone(level?: string) {
  if (level === 'CRITICAL' || level === 'VERY_HIGH' || level === 'HIGH') return 'danger'
  if (level === 'MEDIUM') return 'warning'
  return 'normal'
}
</script>

<style scoped>
.medical-command-bar,
.medical-columns,
.medical-actions,
.medical-list {
  display: grid;
}

.medical-command-bar {
  grid-template-columns: minmax(260px, 0.75fr) minmax(500px, 1.25fr);
  gap: 18px;
  padding: 17px;
  border: 1px solid rgba(30, 91, 138, 0.18);
  border-radius: 16px;
  background: linear-gradient(110deg, rgba(30, 91, 138, 0.11), rgba(21, 111, 95, 0.05));
}
.medical-command-bar__copy { display: grid; gap: 5px; align-content: center; }
.medical-command-bar__copy > span { color: #1e5b8a; font-size: 11px; font-weight: 900; letter-spacing: 0.12em; }
.medical-command-bar__copy strong { color: var(--ink); font-size: 16px; }
.medical-command-bar__copy p { margin: 0; color: var(--muted); font-size: 14px; }
.medical-actions { grid-template-columns: repeat(auto-fit, minmax(92px, 1fr)); gap: 8px; }
.medical-actions button { min-height: 58px; display: flex; align-items: center; gap: 8px; padding: 10px; border: 1px solid rgba(30, 91, 138, 0.14); border-radius: 12px; background: var(--surface); color: var(--ink); cursor: pointer; }
.medical-actions button:hover, .medical-actions button:focus-visible { border-color: #1e5b8a; outline: 3px solid rgba(30, 91, 138, 0.15); }
.medical-actions button span { width: 28px; height: 28px; display: grid; place-items: center; flex: 0 0 28px; border-radius: 8px; background: #e8f1f8; color: #1e5b8a; font-weight: 900; }
.medical-columns { grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 14px; }
.medical-panel { min-width: 0; padding: 16px; border: 1px solid var(--line); border-radius: 16px; background: var(--surface); }
.medical-panel__head { display: flex; align-items: flex-start; justify-content: space-between; gap: 12px; margin-bottom: 12px; }
.medical-panel__head > div > span { color: var(--muted); font-size: 12px; font-weight: 700; }
.medical-panel__head h4 { margin: 3px 0 0; color: var(--ink); font-size: 16px; }
.medical-list { gap: 8px; }
.medical-task, .medical-resident, .medical-record { width: 100%; min-height: 54px; display: grid; align-items: center; gap: 10px; padding: 9px 10px; border: 1px solid transparent; border-radius: 10px; background: var(--surface-soft); color: inherit; text-align: left; cursor: pointer; }
.medical-task { grid-template-columns: 46px minmax(0, 1fr) auto; }
.medical-task__time { color: #1e5b8a; font-weight: 900; }
.medical-task > div, .medical-resident > div, .medical-record > div { min-width: 0; display: grid; gap: 3px; }
.medical-task strong, .medical-resident strong, .medical-record strong { overflow: hidden; color: var(--ink); font-size: 14px; text-overflow: ellipsis; white-space: nowrap; }
.medical-task div span, .medical-resident div span, .medical-record div span { overflow: hidden; color: var(--muted); font-size: 13px; text-overflow: ellipsis; white-space: nowrap; }
.medical-task:hover, .medical-task:focus-visible, .medical-resident:hover, .medical-resident:focus-visible, .medical-record:hover, .medical-record:focus-visible { border-color: #1e5b8a; outline: 3px solid rgba(30, 91, 138, 0.13); }
.medical-resident { grid-template-columns: 38px minmax(0, 1fr) auto; }
.medical-resident__mark { width: 36px; height: 36px; display: grid; place-items: center; border-radius: 50%; background: #e8f1f8; color: #1e5b8a; font-weight: 900; }
.medical-panel--coordination { display: grid; gap: 12px; }
.medical-coordination-actions { display: flex; flex-wrap: wrap; gap: 8px; }
.medical-scope-alert { border-radius: 14px; }
.medical-steps { display: grid; gap: 12px; margin: 0; padding: 0; list-style: none; }
.medical-steps li { display: grid; grid-template-columns: 42px 1fr; align-items: center; gap: 12px; padding: 10px; border-radius: 11px; background: var(--surface-soft); }
.medical-steps li > strong { color: #1e5b8a; font-size: 17px; }
.medical-steps li div { display: grid; gap: 3px; }
.medical-steps b { color: var(--ink); font-size: 14px; }
.medical-steps span { color: var(--muted); font-size: 13px; }
.medical-record { grid-template-columns: 44px minmax(0, 1fr) auto; }
.medical-record > span:first-child { color: var(--muted); font-weight: 800; }

@media (max-width: 1100px) {
  .medical-command-bar { grid-template-columns: 1fr; }
}
@media (max-width: 760px) {
  .medical-columns { grid-template-columns: 1fr; }
  .medical-actions { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .medical-task, .medical-record { grid-template-columns: 44px minmax(0, 1fr); }
  .medical-task :deep(.status-tag), .medical-record :deep(.status-tag) { grid-column: 2; justify-self: start; }
}
</style>
