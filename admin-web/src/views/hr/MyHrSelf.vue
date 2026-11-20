<template>
  <PageContainer title="我的工资与积分" sub-title="查看本人已审核的工资单、积分流水与兑现记录；只显示本人数据，查看工资单会留下访问记录">
    <div class="of-page">
      <div class="of-kpis">
        <div class="of-kpi"><span class="of-kpi__icon tone-green"><WalletOutlined /></span><div class="of-kpi__body">
          <span>实发工资{{ current ? `（${current.period}）` : '' }}</span><b>{{ current ? `¥${money(current.line.netAmount)}` : '--' }}</b><em>{{ compare('netAmount') }}</em></div></div>
        <div class="of-kpi"><span class="of-kpi__icon tone-blue"><CreditCardOutlined /></span><div class="of-kpi__body">
          <span>应发工资</span><b>{{ current ? `¥${money(current.line.grossAmount)}` : '--' }}</b><em>{{ compare('grossAmount') }}</em></div></div>
        <div class="of-kpi"><span class="of-kpi__icon tone-red"><MinusCircleOutlined /></span><div class="of-kpi__body">
          <span>扣款与个人负担</span><b>{{ current ? `¥${money(deductionsOf(current.line))}` : '--' }}</b><em>扣款 + 社保公积金个税</em></div></div>
        <div class="of-kpi"><span class="of-kpi__icon tone-purple"><StarOutlined /></span><div class="of-kpi__body">
          <span>个人积分</span><b>{{ summary ? summary.balance : '--' }}<small>分</small></b><em>本月 {{ monthEarned >= 0 ? '+' : '' }}{{ monthEarned }}</em></div></div>
        <div class="of-kpi"><span class="of-kpi__icon tone-orange"><TrophyOutlined /></span><div class="of-kpi__body">
          <span>积分排名</span><b>{{ summary?.rank ? `第 ${summary.rank} 名` : '--' }}</b><em>{{ summary?.rankTotal ? `全院共 ${summary.rankTotal} 人` : '暂无排名' }}</em></div></div>
      </div>

      <div class="of-card mh-filter">
        <span>月份：</span>
        <a-select v-model:value="selectedPeriod" :options="periodOptions" placeholder="暂无已审核工资单" style="width: 180px" :disabled="!payslips.length" />
        <span class="of-muted">工资单由人事核算、财务付款；只有审核通过后才会出现在这里。如有疑问请联系人事。</span>
      </div>

      <div class="mh-grid">
        <article class="of-card">
          <div class="of-card__head"><h3><LineChartOutlined />工资发放趋势 <small>（近 6 期实发）</small></h3></div>
          <div v-if="trend.length < 2" class="of-empty">至少两期工资单后显示趋势</div>
          <v-chart v-else class="mh-chart" :option="trendOption" autoresize />
        </article>
        <article class="of-card">
          <div class="of-card__head">
            <h3><ProfileOutlined />工资明细 <small>{{ current ? `（${current.period}）` : '' }}</small></h3>
            <span v-if="current" class="of-pill" :class="current.status === 'PAID' ? 'tone-green' : 'tone-blue'">{{ PAYROLL_STATUS[current.status]?.label || current.status }}</span>
          </div>
          <div v-if="!current" class="of-empty">暂无已审核的工资单</div>
          <table v-else class="mh-table">
            <thead><tr><th>薪资项目</th><th class="num">金额（元）</th><th>说明</th></tr></thead>
            <tbody>
              <tr v-for="row in payRows" :key="row.label"><td>{{ row.label }}</td><td class="num" :class="{ neg: row.negative }">{{ row.negative ? '-' : '' }}¥{{ money(row.value) }}</td><td class="of-muted">{{ row.note }}</td></tr>
            </tbody>
            <tfoot><tr><td>实发工资</td><td class="num">¥{{ money(current.line.netAmount) }}</td><td class="of-muted">应发 ¥{{ money(current.line.grossAmount) }} − 扣款 ¥{{ money(deductionsOf(current.line)) }}</td></tr></tfoot>
          </table>
        </article>
      </div>

      <div class="of-grid-3">
        <article class="of-card">
          <div class="of-card__head"><h3><StarOutlined />积分明细 <small>（最近）</small></h3><span class="of-muted">本月累计 {{ monthEarned >= 0 ? '+' : '' }}{{ monthEarned }} 分</span></div>
          <div v-if="!points.length" class="of-empty">暂无积分记录</div>
          <div v-for="p in points.slice(0, 6)" :key="String(p.id)" class="of-row">
            <div class="of-row__main"><b>{{ SOURCE[p.sourceType || ''] || p.sourceType || '积分调整' }}</b><small>{{ p.remark || '' }} · {{ p.createTime ? dayjs(p.createTime).format('MM-DD') : '' }}</small></div>
            <b :class="p.changePoints < 0 ? 'mh-neg' : 'mh-pos'">{{ p.changePoints > 0 ? '+' : '' }}{{ p.changePoints }}</b>
            <a v-if="p.sourceType !== 'REVERSAL' && !appealed(p.id)" class="of-link" @click="openAppeal(p)">申诉</a>
            <span v-else-if="appealed(p.id)" class="of-pill tone-orange">{{ APPEAL_STATUS[appealed(p.id)!.status] }}</span>
          </div>
        </article>
        <article class="of-card">
          <div class="of-card__head"><h3><GiftOutlined />积分兑现记录</h3><a class="of-link" @click="router.push('/oa/approval?type=POINTS_CASH&quick=1')">申请兑现 ›</a></div>
          <div v-if="!summary?.exchanges.length" class="of-empty">暂无兑现申请</div>
          <div v-for="(e, i) in summary?.exchanges || []" :key="i" class="of-row">
            <div class="of-row__main"><b>{{ e.title }}</b><small>{{ dayjs(e.time).format('YYYY-MM-DD') }}{{ e.amount ? ` · ¥${money(e.amount)}` : '' }}</small></div>
            <span class="of-pill" :class="e.status === 'APPROVED' ? 'tone-green' : e.status === 'REJECTED' ? 'tone-red' : 'tone-orange'">{{ e.status === 'APPROVED' ? '已通过' : e.status === 'REJECTED' ? '已驳回' : '审批中' }}</span>
          </div>
        </article>
        <article class="of-card">
          <div class="of-card__head"><h3><TrophyOutlined />积分排名与申诉</h3></div>
          <div class="mh-rank">
            <div class="mh-rank__badge"><small>全院排名</small><b>{{ summary?.rank ? `第 ${summary.rank} 名` : '--' }}</b><small>{{ summary?.rankTotal ? `/ 共 ${summary.rankTotal} 人` : '' }}</small></div>
            <div class="of-legend">
              <div>积分余额<b>{{ summary?.balance ?? '--' }}</b></div>
              <div>本月获得<b>{{ monthEarned }}</b></div>
              <div>申诉待复核<b>{{ appeals.filter((a) => a.status === 'PENDING').length }}</b></div>
            </div>
          </div>
          <div v-for="a in appeals.slice(0, 3)" :key="String(a.id)" class="of-row">
            <div class="of-row__main"><b>{{ a.reason }}</b><small>{{ a.decisionNote || '' }}</small></div>
            <span class="of-pill" :class="a.status === 'ACCEPTED' ? 'tone-green' : a.status === 'REJECTED' ? 'tone-red' : 'tone-orange'">{{ APPEAL_STATUS[a.status] }}</span>
          </div>
        </article>
      </div>
    </div>

    <a-modal v-model:open="appealOpen" title="积分申诉" ok-text="提交申诉" @ok="submit">
      <p>对「{{ target?.remark || target?.sourceType }} {{ target?.changePoints }} 分」提出异议：</p>
      <a-textarea v-model:value="reason" :rows="3" placeholder="请说明理由，可写明可核实的依据（如已批准的请假单）" />
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import { useRouter } from 'vue-router'
import {
  CreditCardOutlined, GiftOutlined, LineChartOutlined, MinusCircleOutlined, ProfileOutlined, StarOutlined, TrophyOutlined, WalletOutlined
} from '@ant-design/icons-vue'
import PageContainer from '../../components/PageContainer.vue'
import { getPointsSummary } from '../../api/me'
import { getMyAppeals, getMyPayslips, getMyPoints, submitAppeal, type PerformanceAppeal, type PayrollLine, type PointsLog } from '../../api/hrOps'
import { PAYROLL_STATUS } from './hrPayroll'
import '../oa/office/office.css'

const APPEAL_STATUS: Record<string, string> = { PENDING: '待复核', ACCEPTED: '申诉成立', REJECTED: '维持原结果' }
const SOURCE: Record<string, string> = { TASK: '任务完成', REVIEW: '质控评价', SURVEY: '满意度表扬', MANUAL: '人工调整', REVERSAL: '冲正', REDEEM: '积分兑现' }
type Payslip = { period: string; status: string; line: PayrollLine }
const payslips = ref<Payslip[]>([])
const points = ref<PointsLog[]>([])
const appeals = ref<PerformanceAppeal[]>([])
const summary = ref<Awaited<ReturnType<typeof getPointsSummary>>>()
const selectedPeriod = ref<string>()
const router = useRouter()
const appealOpen = ref(false)
const target = ref<PointsLog>()
const reason = ref('')

const money = (value?: number) => Number(value || 0).toFixed(2)
// 按分计算，避免浮点误差
const cents = (v?: number) => Math.round(Number(v || 0) * 100)
const deductionsOf = (line: PayrollLine) => (cents(line.deductionAmount) + cents(line.personalAmount)) / 100
const periodOptions = computed(() => payslips.value.map((p) => ({ value: p.period, label: p.period })))
const current = computed(() => payslips.value.find((p) => p.period === selectedPeriod.value))
const previous = computed(() => { const i = payslips.value.findIndex((p) => p.period === selectedPeriod.value); return i >= 0 ? payslips.value[i + 1] : undefined })
function compare(field: 'netAmount' | 'grossAmount') {
  if (!current.value || !previous.value) return '暂无上期对比'
  const prev = Number(previous.value.line[field] || 0)
  if (!prev) return '暂无上期对比'
  const pct = ((Number(current.value.line[field] || 0) - prev) / prev) * 100
  return `较上期 ${pct >= 0 ? '+' : ''}${pct.toFixed(1)}%`
}
const payRows = computed(() => {
  const l = current.value?.line
  if (!l) return []
  return [
    { label: '基本工资', value: l.baseAmount, note: '岗位基本薪酬' },
    { label: '绩效', value: l.performanceAmount, note: '按绩效考核结果' },
    { label: '津补贴', value: l.allowanceAmount, note: '岗位、夜班等津贴' },
    { label: '加班', value: l.overtimeAmount, note: '已审批的加班' },
    { label: '扣款', value: l.deductionAmount, note: '依据制度的扣款', negative: true },
    { label: '个人负担', value: l.personalAmount, note: '社保、公积金、个税个人部分', negative: true }
  ]
})
const trend = computed(() => [...payslips.value].slice(0, 6).reverse())
const trendOption = computed(() => ({
  grid: { left: 56, right: 16, top: 24, bottom: 28 },
  tooltip: { trigger: 'axis', valueFormatter: (v: number) => `¥${Number(v).toFixed(2)}` },
  xAxis: { type: 'category', data: trend.value.map((p) => p.period), boundaryGap: false },
  yAxis: { type: 'value', scale: true },
  series: [{ type: 'line', smooth: true, data: trend.value.map((p) => Number(p.line.netAmount || 0)), itemStyle: { color: '#1f9d74' },
    areaStyle: { color: 'rgba(31, 157, 116, 0.12)' }, label: { show: true, formatter: (p: { value: number }) => `¥${p.value}` } }]
}))
const monthEarned = computed(() => points.value
  .filter((p) => p.createTime && dayjs(p.createTime).format('YYYY-MM') === dayjs().format('YYYY-MM'))
  .reduce((sum, p) => sum + Number(p.changePoints || 0), 0))
const appealed = (id: PointsLog['id']) => appeals.value.find((a) => a.targetType === 'POINTS_LOG' && String(a.targetId) === String(id))
watch(payslips, (list) => { if (!selectedPeriod.value && list.length) selectedPeriod.value = list[0].period })

async function load() {
  getPointsSummary().then((v) => { summary.value = v }).catch(() => {})
  const [s, p, a] = await Promise.allSettled([getMyPayslips(), getMyPoints(), getMyAppeals()])
  payslips.value = s.status === 'fulfilled' ? s.value : []
  points.value = p.status === 'fulfilled' ? p.value : []
  appeals.value = a.status === 'fulfilled' ? a.value : []
}
function openAppeal(row: PointsLog) {
  target.value = row
  reason.value = ''
  appealOpen.value = true
}
async function submit() {
  if (!reason.value.trim()) return message.warning('请填写申诉理由')
  await submitAppeal({ targetType: 'POINTS_LOG', targetId: target.value!.id, reason: reason.value.trim() })
  message.success('已提交，人事主管复核后会更新结果')
  appealOpen.value = false
  await load()
}
onMounted(load)
</script>

<style scoped>
.mh-filter { display: flex; flex-wrap: wrap; align-items: center; gap: 12px; }
.mh-grid { display: grid; grid-template-columns: minmax(0, 1fr) minmax(0, 1.25fr); gap: 16px; }
.mh-chart { height: 240px; }
.mh-table { width: 100%; border-collapse: collapse; font-size: 13px; }
.mh-table th { padding: 8px 10px; background: #eef7f1; color: #33433a; text-align: left; font-weight: 600; }
.mh-table td { padding: 8px 10px; border-bottom: 1px solid #f0f3f1; }
.mh-table .num { text-align: right; font-variant-numeric: tabular-nums; }
.mh-table .neg { color: #c94a3d; }
.mh-table tfoot td { background: #eef7f1; font-size: 15px; font-weight: 700; color: #17734f; }
.mh-pos { color: #17734f; }
.mh-neg { color: #c94a3d; }
.mh-rank { display: flex; align-items: center; gap: 16px; margin-bottom: 8px; }
.mh-rank__badge { display: grid; justify-items: center; min-width: 120px; padding: 14px 10px; border-radius: 16px; background: linear-gradient(160deg, #fdf3dc, #fbe7b8); }
.mh-rank__badge b { font-size: 24px; color: #8a5a00; }
.mh-rank__badge small { color: #8a6d2f; }
@media (max-width: 1100px) { .mh-grid { grid-template-columns: 1fr; } }
</style>
