<template>
  <PageContainer title="护理部工作台" subTitle="用专业的护理，守护每一位长者的健康与尊严。" mode="scenic">
    <DepartmentHomeOverview
      department="NURSING"
      :show-header="false"
      :metrics="metrics"
      :data-state="dataState"
      :data-message="errorMessage"
      scope-label="当前账号护理业务范围"
      :period-label="`业务日期 ${businessDate}`"
      :updated-at="updatedAt"
      :refreshing="dataState === 'loading'"
      show-refresh
      @refresh="refreshAll"
    >
      <template #after-metrics>
        <div class="nursing-dashboard">
          <section class="nursing-panel nursing-panel--tasks" aria-label="当前班次任务">
            <div class="nursing-panel__heading"><h2>当前班次任务 <small>({{ tasks.length }})</small></h2><a-button type="link" @click="open('/care/today')">查看全部 →</a-button></div>
            <div class="nursing-tabs" role="group" aria-label="任务状态筛选">
              <button v-for="item in taskTabs" :key="item.key" type="button" :aria-pressed="taskTab === item.key" :class="{ active: taskTab === item.key }" @click="taskTab = item.key">{{ item.label }}</button>
            </div>
            <a-spin :spinning="taskLoading">
              <div v-if="taskError" class="nursing-empty">{{ taskError }} <a-button type="link" @click="loadTasks">重试</a-button></div>
              <div v-else-if="!visibleTasks.length" class="nursing-empty">当前筛选暂无任务</div>
              <div v-else class="nursing-table-wrap">
                <table class="nursing-table"><thead><tr><th>时间</th><th>房间</th><th>长者</th><th>任务内容</th><th>负责人</th><th>状态</th></tr></thead>
                  <tbody><tr v-for="task in visibleTasks" :key="task.taskDailyId"><td>{{ task.planTime ? dayjs(task.planTime).format('HH:mm') : '--' }}</td><td>{{ task.roomNo || '--' }}</td><td>{{ task.elderName || '--' }}</td><td><button type="button" class="nursing-task-link" @click="open('/care/today')">{{ task.taskName || '--' }}</button></td><td>{{ task.staffName || '待分配' }}</td><td><span class="nursing-status" :class="{ danger: task.overdueFlag || task.status === 'EXCEPTION', done: task.status === 'DONE' }">{{ task.overdueFlag ? '已逾期' : task.status === 'DONE' ? '已完成' : task.status === 'EXCEPTION' ? '异常' : '待执行' }}</span></td></tr></tbody>
                </table>
              </div>
            </a-spin>
          </section>
          <section class="nursing-panel nursing-panel--risk" aria-label="重点长者关注">
            <div class="nursing-panel__heading"><h2>重点长者关注 <small>({{ riskTasks.length }})</small></h2><a-button type="link" @click="open('/care/exception')">全部 →</a-button></div>
            <div v-if="riskTasks.length" class="nursing-risk-list">
              <button v-for="task in riskTasks" :key="task.taskDailyId" type="button" @click="open(`/elder/resident-360?residentId=${task.elderId}`)"><span class="nursing-risk-avatar">{{ (task.elderName || '长').slice(0, 1) }}</span><span><strong>{{ task.elderName || '未命名长者' }}</strong><small>{{ task.roomNo || '未分配房间' }} · {{ task.taskName }}</small></span><em>{{ task.overdueFlag ? '超时' : '异常' }}</em></button>
            </div>
            <div v-else class="nursing-empty">当前没有超时或异常护理任务。</div>
          </section>
        </div>
      </template>
    </DepartmentHomeOverview>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import dayjs from 'dayjs'
import { useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import DepartmentHomeOverview from '../../components/workbench/DepartmentHomeOverview.vue'
import { getTaskSummary, getTodayTasks } from '../../api/care'
import type { CareTaskItem, CareTaskSummary } from '../../types'
import type { DepartmentHomeDataState, DepartmentHomeMetric } from '../../workbench/departmentHome'

const summary = ref<CareTaskSummary | null>(null)
const tasks = ref<CareTaskItem[]>([])
const taskLoading = ref(false)
const taskError = ref('')
const taskTab = ref<'all' | 'pending' | 'done'>('all')
const taskTabs = [{ key: 'all', label: '全部' }, { key: 'pending', label: '待执行' }, { key: 'done', label: '已完成' }] as const
const router = useRouter()
const dataState = ref<DepartmentHomeDataState>('loading')
const errorMessage = ref('')
const updatedAt = ref('')
const businessDate = dayjs().format('YYYY-MM-DD')
const visibleTasks = computed(() => tasks.value.filter((task) => taskTab.value === 'all' || (taskTab.value === 'done' ? task.status === 'DONE' : task.status !== 'DONE')).slice(0, 8))
const riskTasks = computed(() => tasks.value.filter((task) => task.overdueFlag || task.status === 'EXCEPTION').slice(0, 5))

function open(path: string) { router.push(path) }

async function loadTasks() {
  taskLoading.value = true
  taskError.value = ''
  try {
    const page = await getTodayTasks({ pageNo: 1, pageSize: 100, date: businessDate })
    tasks.value = page.list || []
  } catch (error: any) {
    tasks.value = []
    taskError.value = error?.message || '任务加载失败'
  } finally {
    taskLoading.value = false
  }
}

function refreshAll() { void loadSummary(); void loadTasks() }

const metrics = computed<DepartmentHomeMetric[]>(() => {
  if (!summary.value) return []
  return [
    { key: 'today', label: '今日任务', value: summary.value.totalCount, helper: '今日护理任务总数', path: '/care/today', tone: 'normal' },
    { key: 'done', label: '已完成', value: summary.value.doneCount, helper: '今日已完成护理任务', path: '/care/today', tone: 'success' },
    { key: 'overdue', label: '超时任务', value: summary.value.overdueCount, helper: '需要优先处理', path: '/care/today?overdueOnly=true', tone: 'danger' },
    { key: 'exception', label: '异常任务', value: summary.value.exceptionCount, helper: '需要说明或协同', path: '/care/exception', tone: 'warning' }
  ]
})

async function loadSummary() {
  if (dataState.value === 'loading' && summary.value) return
  dataState.value = 'loading'
  errorMessage.value = ''
  try {
    const data = await getTaskSummary({ dateFrom: businessDate, dateTo: businessDate })
    summary.value = data || null
    dataState.value = data ? 'ready' : 'empty'
    updatedAt.value = dayjs().format('HH:mm')
  } catch (error: any) {
    summary.value = null
    dataState.value = Number(error?.response?.status) === 403 ? 'forbidden' : 'error'
    errorMessage.value = error?.message || '护理首页数据加载失败'
  }
}

onMounted(refreshAll)
</script>

<style scoped>
.nursing-dashboard { display: grid; grid-template-columns: minmax(0, 2fr) minmax(280px, 1fr); gap: 12px; }
.nursing-panel { min-width: 0; padding: 16px; border: 1px solid #e7ece8; border-radius: 12px; background: #fff; box-shadow: 0 6px 20px rgba(35, 52, 43, .045); }
.nursing-panel__heading { display: flex; align-items: center; justify-content: space-between; gap: 8px; margin-bottom: 10px; }
.nursing-panel h2 { margin: 0; padding-left: 10px; border-left: 3px solid #188265; color: #1e3027; font-size: 17px; }
.nursing-panel h2 small { color: #849189; font-size: 13px; font-weight: 400; }
.nursing-tabs { display: flex; gap: 6px; margin-bottom: 10px; }
.nursing-tabs button { min-height: 34px; padding: 4px 13px; border: 0; border-radius: 7px; background: #f4f6f4; color: #516359; cursor: pointer; }
.nursing-tabs button.active { background: #dcefe7; color: #176b52; font-weight: 700; }
.nursing-table-wrap { overflow-x: auto; }
.nursing-table { width: 100%; min-width: 630px; border-collapse: collapse; text-align: left; font-size: 12px; }
.nursing-table th { padding: 9px 7px; background: #f5f7f5; color: #6b776f; font-weight: 600; }
.nursing-table td { padding: 10px 7px; border-bottom: 1px solid #edf0ed; color: #354238; }
.nursing-table tbody tr:hover { background: #f3faf6; }
.nursing-task-link { padding: 0; border: 0; background: none; color: #166f55; text-align: left; cursor: pointer; }
.nursing-status { display: inline-block; padding: 3px 8px; border-radius: 6px; background: #fff0df; color: #b36c21; white-space: nowrap; }
.nursing-status.danger { background: #fde9e8; color: #c9443d; }
.nursing-status.done { background: #e4f4ed; color: #147757; }
.nursing-risk-list { display: grid; }
.nursing-risk-list button { display: flex; align-items: center; gap: 10px; min-height: 63px; padding: 8px 0; border: 0; border-bottom: 1px solid #eef1ee; background: none; text-align: left; cursor: pointer; }
.nursing-risk-list button:last-child { border-bottom: 0; }
.nursing-risk-avatar { display: grid; place-items: center; flex: 0 0 34px; height: 34px; border-radius: 50%; background: #dcefe7; color: #176d53; font-weight: 700; }
.nursing-risk-list button > span:nth-child(2) { display: grid; flex: 1; min-width: 0; gap: 3px; }
.nursing-risk-list small { overflow: hidden; color: #7b877e; text-overflow: ellipsis; white-space: nowrap; }
.nursing-risk-list em { color: #c9443d; font-style: normal; }
.nursing-empty { padding: 28px 12px; color: #7b877e; text-align: center; }
.nursing-dashboard button:focus-visible { outline: 3px solid #24876d; outline-offset: 2px; }
@media (max-width: 1100px) { .nursing-dashboard { grid-template-columns: 1fr; } }
</style>
