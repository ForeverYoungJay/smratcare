<template>
  <WorkbenchModuleCard
    :title="managementView ? detail.managementTitle : detail.employeeTitle"
    :eyebrow="managementView ? '部长视图' : '员工视图'"
    :class="['role-department-workbench', `role-department-workbench--${detail.department.toLowerCase()}`]"
  >
    <template #extra>
      <a-space size="small" wrap>
        <StatusTag :text="detail.scopeLabel" :tone="managementView ? 'normal' : 'pending'" />
        <a-button type="link" @click="open(primaryAction.path)">进入{{ primaryAction.label }}</a-button>
      </a-space>
    </template>

    <div class="role-command-bar">
      <div class="role-command-bar__copy">
        <span>{{ commandCode }}</span>
        <strong>{{ detail.headline }}</strong>
        <p>{{ detail.description }}</p>
      </div>
      <div class="role-actions" :aria-label="`${departmentLabel}常用操作`">
        <button v-for="action in visibleActions" :key="action.path" type="button" @click="open(action.path)">
          <span aria-hidden="true">{{ action.icon }}</span><strong>{{ action.label }}</strong>
        </button>
      </div>
    </div>

    <a-alert
      v-if="detail.dataScopeNotice"
      class="role-scope-alert"
      type="info"
      show-icon
      :message="detail.dataScopeNotice"
      description="工作台只展示明确标注的机构协作数据，不将部门汇总冒充个人待办或个人绩效。"
    />

    <div class="role-metrics" :aria-label="`${departmentLabel}重点指标`">
      <button v-for="metric in visibleMetrics" :key="metric.key" type="button" @click="open(metric.path)">
        <span>{{ metric.label }}</span>
        <strong>{{ metric.value === undefined ? '--' : number(metric.value) }}<small v-if="metric.value !== undefined">{{ metric.suffix }}</small></strong>
        <em>{{ metric.helper }}</em>
        <StatusTag :text="metric.value === undefined ? '数据待接入' : metric.value > 0 && metric.tone !== 'normal' ? '需要关注' : '查看详情'" :tone="metric.value === undefined ? 'offline' : metric.tone || 'normal'" />
      </button>
    </div>

    <div class="role-columns">
      <section class="role-panel" aria-labelledby="role-sequence-heading">
        <div class="role-panel__head"><div><span>工作顺序</span><h4 id="role-sequence-heading">今天建议这样处理</h4></div></div>
        <ol class="role-steps">
          <li v-for="(step, index) in detail.steps" :key="step.title">
            <button type="button" @click="open(step.path)">
              <strong>{{ String(index + 1).padStart(2, '0') }}</strong>
              <div><b>{{ step.title }}</b><span>{{ step.description }}</span></div>
              <em>进入 →</em>
            </button>
          </li>
        </ol>
      </section>

      <section class="role-panel" aria-labelledby="role-operating-heading">
        <div class="role-panel__head">
          <div><span>{{ managementView ? '管理提醒' : '数据说明' }}</span><h4 id="role-operating-heading">{{ managementView ? '部门协调重点' : '个人任务口径' }}</h4></div>
          <StatusTag :text="managementView ? '需要协调' : '后端待接入'" :tone="managementView ? 'warning' : 'offline'" />
        </div>
        <template v-if="managementView">
          <p class="role-summary">{{ detail.managementSummary || '请结合重点指标安排人员、处理风险并复核完成情况。' }}</p>
          <a-alert type="info" show-icon message="当前后端暂无按人员聚合的任务分配与完成情况接口" description="不从部门总量推算员工绩效；请进入具体业务页面核对责任人和处理记录。" />
          <div class="role-panel__actions">
            <a-button type="primary" @click="open(primaryAction.path)">进入部门业务</a-button>
            <a-button v-if="canAccess('/workbench/approvals')" @click="open('/workbench/approvals')">处理待审批</a-button>
          </div>
        </template>
        <template v-else>
          <p class="role-summary">当前指标是机构当班参考，帮助判断今天哪里需要协作；它们不代表本人工作量。</p>
          <a-empty description="个人任务与最近处理记录暂缺可靠接口">
            <a-button @click="open(primaryAction.path)">进入已授权业务</a-button>
          </a-empty>
        </template>
      </section>
    </div>
  </WorkbenchModuleCard>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import StatusTag from '../smartcare/StatusTag.vue'
import WorkbenchModuleCard from '../smartcare/WorkbenchModuleCard.vue'
import type { RoleDepartmentWorkbenchDetail } from '../../workbench/operational'

const props = defineProps<{
  detail: RoleDepartmentWorkbenchDetail
  managementView: boolean
  canAccess: (path: string) => boolean
}>()
const emit = defineEmits<{ open: [path: string] }>()

const visibleActions = computed(() => props.detail.actions.filter((item) => (!item.managementOnly || props.managementView) && props.canAccess(item.path)))
const visibleMetrics = computed(() => props.detail.metrics.filter((item) => props.canAccess(item.path)))
const primaryAction = computed(() => visibleActions.value[0] || props.detail.actions[0])
const departmentLabel = computed(() => ({ LOGISTICS: '后勤', HR: '行政人事', MARKETING: '市场' }[props.detail.department]))
const commandCode = computed(() => ({ LOGISTICS: 'SERVICE CONTINUITY', HR: 'PEOPLE & COMPLIANCE', MARKETING: 'FOLLOW-UP FIRST' }[props.detail.department]))

function open(path: string) { emit('open', path) }
function number(value?: number) { return Number(value || 0).toLocaleString('zh-CN') }
</script>

<style scoped>
.role-department-workbench { --role-accent: #285f7b; --role-soft: #f2f7fa; --role-border: #d5e1e7; }
.role-department-workbench--logistics { --role-accent: #8a581e; --role-soft: #fbf7ef; --role-border: #e5dac9; }
.role-department-workbench--hr { --role-accent: #48643d; --role-soft: #f4f8f1; --role-border: #d8e2d2; }
.role-department-workbench--marketing { --role-accent: #8a3e34; --role-soft: #fbf4f2; --role-border: #ead8d4; }
.role-command-bar { display: grid; grid-template-columns: minmax(280px, 1fr) minmax(430px, 1.4fr); gap: 20px; padding: 20px; border: 1px solid var(--role-border); border-left: 5px solid var(--role-accent); background: var(--role-soft); }
.role-command-bar__copy { display: grid; align-content: center; gap: 5px; }
.role-command-bar__copy > span, .role-panel__head span { color: #69716d; font-size: 12px; font-weight: 700; letter-spacing: .09em; }
.role-command-bar__copy > strong { color: #26352e; font-size: 18px; line-height: 1.5; }
.role-command-bar__copy p, .role-summary { margin: 0; color: #5f6964; font-size: 14px; line-height: 1.7; }
.role-actions { display: grid; grid-template-columns: repeat(4, minmax(82px, 1fr)); gap: 8px; }
.role-actions button { min-height: 72px; padding: 10px 8px; border: 1px solid var(--role-border); border-radius: 4px; background: #fff; color: #2d3b34; cursor: pointer; }
.role-actions button > span { display: block; margin-bottom: 5px; color: var(--role-accent); font-size: 16px; }
.role-actions button > strong { font-size: 14px; }
.role-actions button:hover { border-color: var(--role-accent); background: var(--role-soft); }
.role-scope-alert { margin-top: 16px; }
.role-metrics { display: grid; grid-template-columns: repeat(4, 1fr); gap: 10px; margin-top: 16px; }
.role-metrics > button { display: grid; grid-template-columns: 1fr auto; gap: 6px 8px; align-items: center; min-height: 132px; padding: 16px; text-align: left; border: 1px solid var(--role-border); border-top: 4px solid var(--role-accent); border-radius: 4px; background: #fff; cursor: pointer; }
.role-metrics > button > span { color: #59645f; font-size: 14px; }
.role-metrics > button > strong { grid-column: 1 / -1; color: #263a31; font-size: 26px; }
.role-metrics small { margin-left: 4px; font-size: 13px; }
.role-metrics em { color: #69736e; font-size: 13px; font-style: normal; }
.role-columns { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-top: 16px; }
.role-panel { padding: 18px; border: 1px solid #dfe4e1; border-radius: 4px; background: #fff; }
.role-panel__head { display: flex; align-items: center; justify-content: space-between; gap: 12px; margin-bottom: 14px; }
.role-panel__head h4 { margin: 2px 0 0; color: #2b3b33; font-size: 17px; }
.role-steps { display: grid; gap: 8px; margin: 0; padding: 0; list-style: none; }
.role-steps button { display: grid; grid-template-columns: 42px 1fr auto; gap: 10px; align-items: center; width: 100%; min-height: 76px; padding: 12px; text-align: left; border: 1px solid #e3e7e4; border-radius: 3px; background: #fafbfa; cursor: pointer; }
.role-steps button > strong { color: var(--role-accent); font-size: 20px; }
.role-steps b, .role-steps span { display: block; }
.role-steps b { color: #2b3b33; font-size: 14px; }
.role-steps span { margin-top: 3px; color: #68736d; font-size: 14px; line-height: 1.5; }
.role-steps em { color: var(--role-accent); font-size: 12px; font-style: normal; }
.role-summary { margin-bottom: 14px; padding: 14px; border-left: 4px solid var(--role-accent); background: var(--role-soft); }
.role-panel__actions { display: flex; gap: 10px; margin-top: 14px; }
.role-actions button:focus-visible, .role-metrics button:focus-visible, .role-steps button:focus-visible { outline: 3px solid color-mix(in srgb, var(--role-accent) 28%, transparent); outline-offset: 2px; }
@media (max-width: 1100px) { .role-command-bar { grid-template-columns: 1fr; } .role-metrics { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 760px) { .role-actions { grid-template-columns: repeat(2, 1fr); } .role-metrics, .role-columns { grid-template-columns: 1fr; } .role-panel__actions { flex-direction: column; } }
</style>
