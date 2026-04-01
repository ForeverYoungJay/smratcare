<template>
  <PageContainer title="我的信息" subTitle="个人在岗节奏、费用状态与绩效画像">
    <div class="my-info-shell">
      <section class="hero-panel">
        <div class="hero-copy">
          <span class="hero-kicker">Personal Snapshot</span>
          <h1>把入职、试用期、合同、绩效、报销和电费放到同一条时间线上。</h1>
          <p>普通员工默认查看自己；管理员可以选择多名员工并行对比，快速发现谁即将转正、谁合同将到期、谁费用压力更高。</p>
        </div>
        <div class="hero-metrics">
          <div class="hero-metric">
            <small>当前月份</small>
            <strong>{{ summary.currentMonth || '--' }}</strong>
          </div>
          <div class="hero-metric">
            <small>查看人数</small>
            <strong>{{ cards.length }}</strong>
          </div>
          <div class="hero-metric">
            <small>待处理报销</small>
            <strong>{{ totalPendingReimburse }}</strong>
          </div>
          <div class="hero-metric">
            <small>逾期任务</small>
            <strong>{{ totalOverdueTasks }}</strong>
          </div>
        </div>
      </section>

      <section class="toolbar-panel">
        <div class="toolbar-left">
          <a-tag color="blue">当前：{{ currentUserName }}</a-tag>
          <a-tag v-if="compareEnabled" color="gold">管理员可多人对比</a-tag>
        </div>
        <div class="toolbar-right" v-if="compareEnabled">
          <a-select
            v-model:value="compareStaffIds"
            mode="multiple"
            :options="staffOptions"
            :loading="staffLoading"
            :max-tag-count="3"
            style="width: min(520px, 100%)"
            placeholder="选择员工进行对比"
            @search="searchStaff"
            @focus="() => !staffOptions.length && searchStaff('')"
            @change="handleCompareChange"
          />
          <a-button @click="resetCompare">恢复为我自己</a-button>
        </div>
      </section>

      <section class="card-grid">
        <article v-for="item in cards" :key="String(item.staffId)" class="staff-card">
          <div class="card-head">
            <div>
              <div class="name-row">
                <h2>{{ item.staffName || '未命名员工' }}</h2>
                <a-tag>{{ item.staffNo || '无工号' }}</a-tag>
              </div>
              <p>{{ item.departmentName || '未分配部门' }} · {{ item.jobTitle || '未设置岗位' }}</p>
            </div>
            <div class="salary-source">{{ item.salarySource || '薪资口径未配置' }}</div>
          </div>

          <div class="timeline-strip">
            <div class="timeline-chip">
              <span>入职时间</span>
              <strong>{{ item.hireDate || '--' }}</strong>
            </div>
            <div class="timeline-chip" :class="dayTone(item.probationRemainingDays)">
              <span>距离试用期</span>
              <strong>{{ formatRemainingDays(item.probationRemainingDays) }}</strong>
            </div>
            <div class="timeline-chip" :class="dayTone(item.contractSignRemainingDays)">
              <span>距离签合同</span>
              <strong>{{ formatRemainingDays(item.contractSignRemainingDays) }}</strong>
            </div>
          </div>

          <div class="salary-row">
            <div class="salary-card">
              <span>{{ summary.previousMonth || '上月' }}</span>
              <strong>{{ moneyText(item.previousMonthSalary) }}</strong>
            </div>
            <div class="salary-card highlight">
              <span>{{ summary.currentMonth || '本月' }}</span>
              <strong>{{ moneyText(item.currentMonthSalary) }}</strong>
            </div>
            <div class="salary-card">
              <span>{{ summary.nextMonth || '下月' }}</span>
              <strong>{{ moneyText(item.nextMonthSalary) }}</strong>
            </div>
          </div>

          <div class="stats-grid">
            <div class="stat-tile">
              <small>我的职责任务</small>
              <strong>{{ item.taskOpenCount || 0 }}</strong>
              <span>逾期 {{ item.taskOverdueCount || 0 }} · 本月完成 {{ item.taskCompletedThisMonth || 0 }}</span>
            </div>
            <div class="stat-tile">
              <small>我的绩效</small>
              <strong>{{ scoreText(item.performanceAvgScore) }}</strong>
              <span>积分 {{ item.performancePointsBalance || 0 }} · 可兑现 {{ item.performanceRedeemableCash || 0 }}</span>
            </div>
            <div class="stat-tile">
              <small>我的报销</small>
              <strong>{{ moneyText(item.reimburseMonthAmount) }}</strong>
              <span>本月 {{ item.reimburseMonthCount || 0 }} 笔 · 待审 {{ item.reimbursePendingCount || 0 }}</span>
            </div>
            <div class="stat-tile">
              <small>我的电费</small>
              <strong>{{ moneyText(item.electricityAmount) }}</strong>
              <span>{{ item.dormitoryLabel || '未配置宿舍' }} · {{ item.meterNo || '无电表' }}</span>
            </div>
          </div>

          <div class="task-board">
            <div class="task-board-head">
              <strong>我的职责任务</strong>
              <span>最多展示 4 条待处理任务</span>
            </div>
            <a-empty v-if="!(item.dutyTasks || []).length" :image="false" description="当前没有待处理任务" />
            <ul v-else class="task-list">
              <li v-for="task in item.dutyTasks || []" :key="task">{{ task }}</li>
            </ul>
          </div>
        </article>
      </section>

      <section v-if="cards.length > 1" class="compare-panel">
        <div class="compare-head">
          <div>
            <span class="hero-kicker compare-kicker">Compare Mode</span>
            <h3>员工关键信息对比</h3>
          </div>
          <p>重点看试用期天数、合同签署天数、绩效分、电费与报销额的差异。</p>
        </div>
        <div class="compare-table">
          <div class="compare-row header">
            <div>维度</div>
            <div v-for="item in cards" :key="`head-${item.staffId}`">{{ item.staffName || item.staffNo }}</div>
          </div>
          <div class="compare-row">
            <div>距离试用期</div>
            <div v-for="item in cards" :key="`probation-${item.staffId}`">{{ formatRemainingDays(item.probationRemainingDays) }}</div>
          </div>
          <div class="compare-row">
            <div>距离签合同</div>
            <div v-for="item in cards" :key="`contract-${item.staffId}`">{{ formatRemainingDays(item.contractSignRemainingDays) }}</div>
          </div>
          <div class="compare-row">
            <div>本月工资</div>
            <div v-for="item in cards" :key="`salary-${item.staffId}`">{{ moneyText(item.currentMonthSalary) }}</div>
          </div>
          <div class="compare-row">
            <div>绩效均分</div>
            <div v-for="item in cards" :key="`score-${item.staffId}`">{{ scoreText(item.performanceAvgScore) }}</div>
          </div>
          <div class="compare-row">
            <div>本月报销</div>
            <div v-for="item in cards" :key="`reimburse-${item.staffId}`">{{ moneyText(item.reimburseMonthAmount) }}</div>
          </div>
          <div class="compare-row">
            <div>本月电费</div>
            <div v-for="item in cards" :key="`electric-${item.staffId}`">{{ moneyText(item.electricityAmount) }}</div>
          </div>
          <div class="compare-row">
            <div>待处理任务</div>
            <div v-for="item in cards" :key="`task-${item.staffId}`">{{ item.taskOpenCount || 0 }}</div>
          </div>
        </div>
      </section>
    </div>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import PageContainer from '../../components/PageContainer.vue'
import { getMyInfoSummary } from '../../api/oa'
import { useStaffOptions } from '../../composables/useStaffOptions'
import { useUserStore } from '../../stores/user'
import { hasAnyRole } from '../../utils/roleAccess'
import type { OaMyInfoItem, OaMyInfoSummary } from '../../types'

const userStore = useUserStore()
const summary = ref<OaMyInfoSummary>({})
const cards = computed(() => summary.value.items || [])
const compareStaffIds = ref<string[]>([])
const { staffOptions, staffLoading, searchStaff } = useStaffOptions({ pageSize: 180, preloadSize: 500 })

const compareEnabled = computed(() => {
  const roleCodes = (userStore.roles || []).map((item) => String(item || '').toUpperCase())
  return Boolean(summary.value.compareEnabled) && hasAnyRole(roleCodes, ['ADMIN', 'SYS_ADMIN', 'DIRECTOR'])
})
const currentUserName = computed(() =>
  String(userStore.staffInfo?.realName || userStore.staffInfo?.username || '当前员工').trim()
)
const totalPendingReimburse = computed(() =>
  cards.value.reduce((sum, item) => sum + Number(item.reimbursePendingCount || 0), 0)
)
const totalOverdueTasks = computed(() =>
  cards.value.reduce((sum, item) => sum + Number(item.taskOverdueCount || 0), 0)
)

function moneyText(value?: number) {
  if (value == null || Number.isNaN(Number(value))) return '待录入'
  return `¥${Number(value).toFixed(2)}`
}

function scoreText(value?: number) {
  if (value == null || Number.isNaN(Number(value))) return '--'
  return Number(value).toFixed(1)
}

function formatRemainingDays(value?: number) {
  if (value == null) return '待配置'
  if (value < 0) return `已超 ${Math.abs(value)} 天`
  if (value === 0) return '今天'
  return `${value} 天`
}

function dayTone(value?: number) {
  if (value == null) return 'muted'
  if (value < 0) return 'danger'
  if (value <= 7) return 'warning'
  return 'safe'
}

async function loadSummary(staffIds?: string[]) {
  const params = compareEnabled.value && staffIds?.length ? { staffIds } : undefined
  const data = await getMyInfoSummary(params)
  summary.value = {
    ...data,
    items: (data.items || []).map((item: OaMyInfoItem) => ({
      ...item,
      staffId: String(item.staffId)
    }))
  }
}

async function handleCompareChange(values: Array<string | number>) {
  compareStaffIds.value = values.map((item) => String(item))
  await loadSummary(compareStaffIds.value)
}

async function resetCompare() {
  compareStaffIds.value = []
  await loadSummary()
}

loadSummary()
</script>

<style scoped>
.my-info-shell {
  --ink: #1a2434;
  --muted: #697587;
  --sand: #f6efe4;
  --sand-deep: #ecd7b3;
  --sky: #eef6ff;
  --line: rgba(124, 139, 160, 0.18);
  display: grid;
  gap: 18px;
}

.hero-panel {
  display: grid;
  grid-template-columns: minmax(0, 1.3fr) minmax(320px, 0.9fr);
  gap: 18px;
  padding: 28px;
  border-radius: 28px;
  background:
    radial-gradient(circle at top left, rgba(255, 220, 164, 0.38), transparent 34%),
    radial-gradient(circle at bottom right, rgba(103, 165, 255, 0.24), transparent 36%),
    linear-gradient(135deg, #fff8ef 0%, #fff 48%, #f3f8ff 100%);
  border: 1px solid rgba(221, 205, 177, 0.5);
}

.hero-kicker {
  display: inline-flex;
  padding: 6px 10px;
  border-radius: 999px;
  background: rgba(161, 97, 16, 0.1);
  color: #9a5815;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.hero-copy h1 {
  margin: 12px 0 10px;
  color: var(--ink);
  font-size: 32px;
  line-height: 1.15;
}

.hero-copy p {
  margin: 0;
  color: var(--muted);
  line-height: 1.8;
}

.hero-metrics {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.hero-metric {
  padding: 18px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid var(--line);
}

.hero-metric small {
  color: var(--muted);
}

.hero-metric strong {
  display: block;
  margin-top: 6px;
  color: var(--ink);
  font-size: 28px;
}

.toolbar-panel {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 20px;
  border-radius: 22px;
  background: #fff;
  border: 1px solid var(--line);
}

.toolbar-left,
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(360px, 1fr));
  gap: 16px;
}

.staff-card {
  padding: 22px;
  border-radius: 26px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(249, 251, 255, 0.98) 100%);
  border: 1px solid var(--line);
  box-shadow: 0 18px 40px rgba(16, 28, 45, 0.08);
}

.card-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}

.name-row h2 {
  margin: 0;
  color: var(--ink);
  font-size: 24px;
}

.card-head p,
.salary-source,
.task-board-head span,
.compare-head p {
  margin: 0;
  color: var(--muted);
}

.salary-source {
  max-width: 170px;
  text-align: right;
  font-size: 12px;
}

.timeline-strip,
.salary-row,
.stats-grid {
  display: grid;
  gap: 12px;
}

.timeline-strip {
  grid-template-columns: repeat(3, minmax(0, 1fr));
  margin-bottom: 14px;
}

.timeline-chip,
.salary-card,
.stat-tile {
  padding: 14px;
  border-radius: 18px;
  border: 1px solid var(--line);
  background: #fbfcff;
}

.timeline-chip.warning {
  background: #fff7e8;
}

.timeline-chip.danger {
  background: #fff1f0;
}

.timeline-chip.safe {
  background: #f3fbf4;
}

.timeline-chip span,
.salary-card span,
.stat-tile small {
  display: block;
  color: var(--muted);
  font-size: 12px;
}

.timeline-chip strong,
.salary-card strong,
.stat-tile strong {
  display: block;
  margin-top: 6px;
  color: var(--ink);
  font-size: 22px;
}

.salary-row {
  grid-template-columns: repeat(3, minmax(0, 1fr));
  margin-bottom: 14px;
}

.salary-card.highlight {
  background: linear-gradient(180deg, #fff7e7 0%, #fff2d3 100%);
  border-color: rgba(215, 174, 92, 0.55);
}

.stats-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
  margin-bottom: 14px;
}

.stat-tile span {
  display: block;
  margin-top: 6px;
  color: var(--muted);
  line-height: 1.6;
}

.task-board {
  padding: 16px;
  border-radius: 20px;
  background: linear-gradient(180deg, var(--sky) 0%, #fff 100%);
}

.task-board-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.task-list {
  margin: 0;
  padding: 0;
  list-style: none;
  display: grid;
  gap: 10px;
}

.task-list li {
  padding: 12px 14px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.8);
  color: var(--ink);
}

.compare-panel {
  padding: 22px;
  border-radius: 26px;
  background: #fff;
  border: 1px solid var(--line);
}

.compare-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.compare-head h3 {
  margin: 10px 0 0;
  color: var(--ink);
  font-size: 24px;
}

.compare-kicker {
  background: rgba(34, 94, 184, 0.1);
  color: #1f56ad;
}

.compare-table {
  display: grid;
  gap: 8px;
}

.compare-row {
  display: grid;
  grid-template-columns: minmax(160px, 1.1fr) repeat(auto-fit, minmax(140px, 1fr));
  gap: 8px;
}

.compare-row > div {
  padding: 14px;
  border-radius: 14px;
  background: #f8fbff;
  color: var(--ink);
}

.compare-row.header > div {
  background: #edf4ff;
  font-weight: 700;
}

@media (max-width: 960px) {
  .hero-panel,
  .timeline-strip,
  .salary-row,
  .stats-grid,
  .compare-head,
  .toolbar-panel {
    grid-template-columns: 1fr;
    flex-direction: column;
    align-items: flex-start;
  }

  .hero-metrics {
    width: 100%;
  }
}
</style>
