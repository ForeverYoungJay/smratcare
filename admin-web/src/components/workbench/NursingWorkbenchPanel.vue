<template>
  <WorkbenchModuleCard
    :title="managementView ? '护理部今日运行' : '我的今日护理'"
    :eyebrow="managementView ? '部长视图' : '员工视图'"
    class="nursing-workbench"
  >
    <template #extra>
      <a-space size="small" wrap>
        <StatusTag :text="detail.scopeLabel" tone="normal" />
        <a-button type="link" @click="open('/care/today')">查看全部任务</a-button>
      </a-space>
    </template>

    <div class="nursing-workbench__lead">
      <div>
        <strong>{{ managementView ? '先看风险和分配缺口，再协调班组执行。' : '按计划时间处理任务，异常与超时事项优先。' }}</strong>
        <p>{{ managementView ? '数据为护理部今日口径；人员绩效不从任务预览推算。' : '任务、长者和最近记录均按当前登录员工收窄。' }}</p>
      </div>
      <div class="nursing-workbench__actions" aria-label="护理常用操作">
        <button v-for="action in visibleActions" :key="action.path" type="button" @click="open(action.path)">
          <span aria-hidden="true">{{ action.icon }}</span>
          <strong>{{ action.label }}</strong>
        </button>
      </div>
    </div>

    <div class="nursing-workbench__columns">
      <section class="nursing-panel" aria-labelledby="nursing-task-heading">
        <div class="nursing-panel__head">
          <div>
            <span>优先队列</span>
            <h4 id="nursing-task-heading">{{ managementView ? '部门待执行任务' : '我接下来的任务' }}</h4>
          </div>
          <StatusTag :text="`${detail.upcomingTasks.length} 项预览`" :tone="detail.upcomingTasks.length ? 'pending' : 'offline'" />
        </div>
        <div v-if="detail.upcomingTasks.length" class="nursing-list">
          <button
            v-for="task in detail.upcomingTasks"
            :key="task.id"
            type="button"
            class="nursing-task"
            @click="open(`/care/today?elderId=${task.elderId}`)"
          >
            <time :datetime="task.planTime">{{ taskTime(task.planTime) }}</time>
            <div>
              <strong>{{ task.taskName }}</strong>
              <span>{{ task.elderName }} · {{ task.location }}<template v-if="managementView"> · {{ task.staffName || '待分配' }}</template></span>
            </div>
            <StatusTag :text="task.overdue ? '已超时' : task.statusLabel" :tone="taskTone(task)" />
          </button>
        </div>
        <a-empty v-else description="今天暂无待执行护理任务">
          <a-button @click="open('/care/today')">查看护理任务</a-button>
        </a-empty>
      </section>

      <section class="nursing-panel" aria-labelledby="nursing-elder-heading">
        <div class="nursing-panel__head">
          <div>
            <span>{{ managementView ? '任务分布' : '与我有关' }}</span>
            <h4 id="nursing-elder-heading">{{ managementView ? '重点长者与风险' : '我今天服务的长者' }}</h4>
          </div>
          <StatusTag :text="`${detail.relatedElders.length} 位`" :tone="detail.relatedElders.length ? 'normal' : 'offline'" />
        </div>
        <div v-if="detail.relatedElders.length" class="nursing-list">
          <button
            v-for="elder in detail.relatedElders"
            :key="String(elder.id)"
            type="button"
            class="nursing-elder"
            @click="open(`/elder/detail/${elder.id}`)"
          >
            <span class="nursing-elder__avatar" aria-hidden="true">{{ elder.name.slice(0, 1) }}</span>
            <div>
              <strong>{{ elder.name }}</strong>
              <span>{{ elder.location }} · 待执行 {{ elder.pendingCount }} 项</span>
            </div>
            <StatusTag v-if="elder.riskCount" :text="`风险 ${elder.riskCount}`" tone="danger" />
            <StatusTag v-else text="暂无风险" tone="normal" />
          </button>
        </div>
        <a-empty v-else description="当前任务中暂无关联长者">
          <a-button @click="open('/elder/list')">查询长者</a-button>
        </a-empty>
      </section>
    </div>

    <div class="nursing-workbench__columns nursing-workbench__columns--lower">
      <section class="nursing-panel" aria-labelledby="nursing-record-heading">
        <div class="nursing-panel__head">
          <div>
            <span>执行留痕</span>
            <h4 id="nursing-record-heading">最近完成记录</h4>
          </div>
          <a-button type="link" @click="open('/care/service/nursing-records')">护理记录</a-button>
        </div>
        <div v-if="detail.recentRecords.length" class="nursing-records">
          <button v-for="record in detail.recentRecords" :key="record.id" type="button" @click="open(`/care/today?elderId=${record.elderId}`)">
            <span>{{ taskTime(record.planTime) }}</span>
            <strong>{{ record.elderName }} · {{ record.taskName }}</strong>
            <StatusTag text="已完成" tone="normal" />
          </button>
        </div>
        <a-empty v-else description="今天尚无已完成的护理记录" />
      </section>

      <section v-if="managementView" class="nursing-panel nursing-panel--management" aria-labelledby="nursing-staff-heading">
        <div class="nursing-panel__head">
          <div>
            <span>班组协调</span>
            <h4 id="nursing-staff-heading">任务分配与人员执行</h4>
          </div>
          <StatusTag :text="`待分配 ${numberText(detail.unassignedCount)}`" :tone="detail.unassignedCount ? 'warning' : 'normal'" />
        </div>
        <div class="assignment-summary">
          <div><span>任务总数</span><strong>{{ numberText(detail.totalCount) }}</strong></div>
          <div><span>已分配</span><strong>{{ numberText(detail.assignedCount) }}</strong></div>
          <div><span>待分配</span><strong>{{ numberText(detail.unassignedCount) }}</strong></div>
        </div>
        <a-alert
          type="info"
          show-icon
          message="当前后端暂无按护理人员聚合的完成情况接口"
          description="暂不使用任务预览估算个人绩效；请进入今日任务进行分配与执行核对。"
        />
        <a-button type="primary" @click="open('/care/today')">分配护理任务</a-button>
      </section>
      <section v-else class="nursing-panel nursing-panel--next" aria-labelledby="nursing-next-heading">
        <div class="nursing-panel__head">
          <div>
            <span>下一步</span>
            <h4 id="nursing-next-heading">完成任务后的标准动作</h4>
          </div>
        </div>
        <ol>
          <li><strong>1</strong><span>核对长者与床位信息，避免错人操作。</span></li>
          <li><strong>2</strong><span>完成护理执行并记录异常情况。</span></li>
          <li><strong>3</strong><span>补齐护理记录，重点事项带入交接班。</span></li>
        </ol>
      </section>
    </div>
  </WorkbenchModuleCard>
</template>

<script setup lang="ts">
import dayjs from 'dayjs'
import StatusTag from '../smartcare/StatusTag.vue'
import WorkbenchModuleCard from '../smartcare/WorkbenchModuleCard.vue'
import type { NursingWorkbenchDetail, NursingWorkbenchTask } from '../../workbench/nursing'

const props = defineProps<{
  detail: NursingWorkbenchDetail
  managementView: boolean
  canAccess: (path: string) => boolean
}>()

const emit = defineEmits<{ open: [path: string] }>()

const actions = [
  { label: '今日任务', icon: '今', path: '/care/today', managementOnly: false },
  { label: '护理计划', icon: '计', path: '/care/workbench/plan', managementOnly: false },
  { label: '护理记录', icon: '录', path: '/care/service/nursing-records', managementOnly: false },
  { label: '交接班', icon: '交', path: '/medical-care/handovers', managementOnly: false },
  { label: '护理质量', icon: '质', path: '/medical-care/nursing-quality', managementOnly: true },
  { label: '护理排班', icon: '班', path: '/care/scheduling/shift-calendar', managementOnly: true },
  { label: '待我审批', icon: '审', path: '/workbench/approvals', managementOnly: true }
]

const visibleActions = actions.filter((item) => (!item.managementOnly || props.managementView) && props.canAccess(item.path))

function open(path: string) {
  emit('open', path)
}

function taskTime(value?: string) {
  const parsed = dayjs(value)
  return parsed.isValid() ? parsed.format('HH:mm') : '--:--'
}

function taskTone(task: NursingWorkbenchTask) {
  if (task.overdue || task.exceptional) return 'danger'
  if (task.status === 'DONE') return 'normal'
  return 'pending'
}

function numberText(value?: number) {
  return value === undefined || value === null ? '--' : value.toLocaleString('zh-CN')
}
</script>

<style scoped>
.nursing-workbench__lead,
.nursing-workbench__columns,
.nursing-workbench__actions,
.nursing-list,
.nursing-records,
.assignment-summary {
  display: grid;
}

.nursing-workbench__lead {
  grid-template-columns: minmax(240px, 0.7fr) minmax(520px, 1.3fr);
  gap: 18px;
  padding: 16px;
  border: 1px solid rgba(var(--primary-rgb), 0.16);
  border-radius: 16px;
  background: linear-gradient(105deg, rgba(var(--primary-rgb), 0.1), rgba(var(--success-rgb), 0.04));
}

.nursing-workbench__lead > div:first-child { display: grid; gap: 5px; align-content: center; }
.nursing-workbench__lead strong { color: var(--ink); font-size: 16px; }
.nursing-workbench__lead p { margin: 0; color: var(--muted); font-size: 14px; }

.nursing-workbench__actions { grid-template-columns: repeat(auto-fit, minmax(94px, 1fr)); gap: 8px; }
.nursing-workbench__actions button {
  min-height: 58px;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px;
  border: 1px solid rgba(var(--primary-rgb), 0.14);
  border-radius: 12px;
  background: var(--surface);
  color: var(--ink);
  cursor: pointer;
}
.nursing-workbench__actions button:hover,
.nursing-workbench__actions button:focus-visible { border-color: var(--primary); outline: 3px solid rgba(var(--primary-rgb), 0.16); }
.nursing-workbench__actions button span { width: 28px; height: 28px; display: grid; place-items: center; border-radius: 8px; background: rgba(var(--primary-rgb), 0.12); color: var(--primary-strong); font-weight: 800; }

.nursing-workbench__columns { grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 14px; }
.nursing-workbench__columns--lower { align-items: stretch; }
.nursing-panel { min-width: 0; padding: 16px; border: 1px solid var(--line); border-radius: 16px; background: var(--surface); }
.nursing-panel__head { display: flex; align-items: flex-start; justify-content: space-between; gap: 12px; margin-bottom: 12px; }
.nursing-panel__head span { color: var(--muted); font-size: 12px; font-weight: 700; }
.nursing-panel__head h4 { margin: 3px 0 0; color: var(--ink); font-size: 16px; }
.nursing-list, .nursing-records { gap: 8px; }
.nursing-task, .nursing-elder, .nursing-records button { width: 100%; min-height: 52px; display: grid; align-items: center; gap: 10px; padding: 9px 10px; border: 1px solid transparent; border-radius: 10px; background: var(--surface-soft); color: inherit; text-align: left; cursor: pointer; }
.nursing-task { grid-template-columns: 44px minmax(0, 1fr) auto; }
.nursing-task time { color: var(--primary-strong); font-size: 14px; font-weight: 800; }
.nursing-task div, .nursing-elder div { min-width: 0; display: grid; gap: 3px; }
.nursing-task strong, .nursing-elder strong, .nursing-records strong { overflow: hidden; color: var(--ink); font-size: 14px; text-overflow: ellipsis; white-space: nowrap; }
.nursing-task div span, .nursing-elder div span { overflow: hidden; color: var(--muted); font-size: 13px; text-overflow: ellipsis; white-space: nowrap; }
.nursing-task:hover, .nursing-task:focus-visible, .nursing-elder:hover, .nursing-elder:focus-visible, .nursing-records button:hover, .nursing-records button:focus-visible { border-color: var(--primary); outline: 3px solid rgba(var(--primary-rgb), 0.13); }
.nursing-elder { grid-template-columns: 38px minmax(0, 1fr) auto; }
.nursing-elder__avatar { width: 36px; height: 36px; display: grid; place-items: center; border-radius: 11px; background: #e7f3ee; color: #175849; font-weight: 800; }
.nursing-records button { grid-template-columns: 44px minmax(0, 1fr) auto; }
.nursing-records button > span:first-child { color: var(--muted); font-weight: 700; }
.assignment-summary { grid-template-columns: repeat(3, 1fr); gap: 8px; margin-bottom: 12px; }
.assignment-summary div { display: grid; gap: 4px; padding: 12px; border-radius: 11px; background: var(--surface-soft); }
.assignment-summary span { color: var(--muted); font-size: 13px; }
.assignment-summary strong { color: var(--ink); font-size: 22px; }
.nursing-panel--management > .ant-btn { margin-top: 12px; min-height: 40px; }
.nursing-panel--next ol { display: grid; gap: 10px; margin: 0; padding: 0; list-style: none; }
.nursing-panel--next li { display: grid; grid-template-columns: 28px 1fr; align-items: center; gap: 10px; color: var(--muted); font-size: 14px; }
.nursing-panel--next li strong { width: 28px; height: 28px; display: grid; place-items: center; border-radius: 50%; background: rgba(var(--primary-rgb), 0.12); color: var(--primary-strong); }

@media (max-width: 1100px) {
  .nursing-workbench__lead { grid-template-columns: 1fr; }
}
@media (max-width: 760px) {
  .nursing-workbench__columns { grid-template-columns: 1fr; }
  .nursing-workbench__actions { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .nursing-task { grid-template-columns: 42px minmax(0, 1fr); }
  .nursing-task :deep(.status-tag) { grid-column: 2; justify-self: start; }
}
</style>
