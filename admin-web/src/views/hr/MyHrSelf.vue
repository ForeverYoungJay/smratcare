<template>
  <PageContainer title="我的工资与积分" sub-title="只显示本人信息：已审核的工资单、积分流水与申诉进度；查看工资单会留下访问记录">
    <div class="me-cards">
      <div class="me-card me-card--green"><span>最近实发工资{{ latest ? `（${latest.period}）` : '' }}</span><b>{{ latest ? `¥${money(latest.line.netAmount)}` : '--' }}</b><small>{{ latest ? PAYROLL_STATUS[latest.status]?.label : '暂无已审核工资单' }}</small></div>
      <div class="me-card"><span>应发工资</span><b>{{ latest ? `¥${money(latest.line.grossAmount)}` : '--' }}</b><small>基本 + 绩效 + 津补贴 + 加班</small></div>
      <div class="me-card me-card--red"><span>扣款与个人负担</span><b>{{ latest ? `¥${money(deductions)}` : '--' }}</b><small>扣款 + 社保公积金个税</small></div>
      <div class="me-card me-card--purple"><span>积分余额</span><b>{{ points.length ? points[0].balanceAfter : '--' }}</b><small>以最近一条流水为准</small></div>
      <div class="me-card me-card--amber"><span>申诉待复核</span><b>{{ appeals.filter((a) => a.status === 'PENDING').length }}</b><small>人事主管复核后更新</small></div>
    </div>
    <a-row :gutter="16">
      <a-col :xs="24" :lg="12">
        <a-card title="我的工资单" size="small">
          <a-empty v-if="!payslips.length" description="暂无已审核的工资单" />
          <a-collapse v-else ghost>
            <a-collapse-panel v-for="p in payslips" :key="p.period" :header="`${p.period} · 实发 ¥${money(p.line.netAmount)} · ${PAYROLL_STATUS[p.status]?.label || p.status}`">
              <a-descriptions size="small" :column="2">
                <a-descriptions-item label="基本工资">{{ money(p.line.baseAmount) }}</a-descriptions-item>
                <a-descriptions-item label="绩效">{{ money(p.line.performanceAmount) }}</a-descriptions-item>
                <a-descriptions-item label="津补贴">{{ money(p.line.allowanceAmount) }}</a-descriptions-item>
                <a-descriptions-item label="加班">{{ money(p.line.overtimeAmount) }}</a-descriptions-item>
                <a-descriptions-item label="扣款">{{ money(p.line.deductionAmount) }}</a-descriptions-item>
                <a-descriptions-item label="个人负担">{{ money(p.line.personalAmount) }}</a-descriptions-item>
                <a-descriptions-item label="应发">{{ money(p.line.grossAmount) }}</a-descriptions-item>
                <a-descriptions-item label="实发"><b>{{ money(p.line.netAmount) }}</b></a-descriptions-item>
              </a-descriptions>
            </a-collapse-panel>
          </a-collapse>
        </a-card>
      </a-col>
      <a-col :xs="24" :lg="12">
        <a-card title="我的积分" size="small">
          <a-empty v-if="!points.length" description="暂无积分记录" />
          <div v-for="p in points" :key="String(p.id)" class="me-row">
            <b :class="p.changePoints < 0 ? 'me-neg' : 'me-pos'">{{ p.changePoints > 0 ? '+' : '' }}{{ p.changePoints }}</b>
            <span>{{ p.remark || p.sourceType }}</span>
            <a v-if="p.sourceType !== 'REVERSAL' && !appealed(p.id)" @click="openAppeal(p)">申诉</a>
            <a-tag v-else-if="appealed(p.id)">{{ APPEAL_STATUS[appealed(p.id)!.status] }}</a-tag>
          </div>
        </a-card>
        <a-card title="我的申诉" size="small" style="margin-top: 16px">
          <a-empty v-if="!appeals.length" description="暂无申诉" />
          <div v-for="a in appeals" :key="String(a.id)" class="me-row">
            <span>{{ a.reason }}</span>
            <a-tag :color="a.status === 'ACCEPTED' ? 'green' : a.status === 'REJECTED' ? 'red' : 'orange'">{{ APPEAL_STATUS[a.status] }}</a-tag>
            <span v-if="a.decisionNote">{{ a.decisionNote }}</span>
          </div>
        </a-card>
      </a-col>
    </a-row>
    <a-modal v-model:open="appealOpen" title="积分申诉" ok-text="提交申诉" @ok="submit">
      <p>对「{{ target?.remark || target?.sourceType }} {{ target?.changePoints }} 分」提出异议：</p>
      <a-textarea v-model:value="reason" :rows="3" placeholder="请说明理由，可写明可核实的依据（如已批准的请假单）" />
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import { getMyAppeals, getMyPayslips, getMyPoints, submitAppeal, type PerformanceAppeal, type PayrollLine, type PointsLog } from '../../api/hrOps'
import { PAYROLL_STATUS } from './hrPayroll'

const APPEAL_STATUS: Record<string, string> = { PENDING: '待复核', ACCEPTED: '申诉成立', REJECTED: '维持原结果' }
const payslips = ref<{ period: string; status: string; line: PayrollLine }[]>([])
const points = ref<PointsLog[]>([])
const appeals = ref<PerformanceAppeal[]>([])
const appealOpen = ref(false)
const target = ref<PointsLog>()
const reason = ref('')

const money = (value?: number) => Number(value || 0).toFixed(2)
const latest = computed(() => payslips.value[0])
// 按分计算，避免浮点误差
const deductions = computed(() => latest.value ? (Math.round(Number(latest.value.line.deductionAmount || 0) * 100) + Math.round(Number(latest.value.line.personalAmount || 0) * 100)) / 100 : 0)
const appealed = (id: PointsLog['id']) => appeals.value.find((a) => a.targetType === 'POINTS_LOG' && String(a.targetId) === String(id))

async function load() {
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
.me-cards { display: grid; grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); gap: 12px; margin-bottom: 16px; }
.me-card { display: grid; gap: 4px; padding: 14px 16px; border-radius: 14px; background: #f5f8fb; border: 1px solid #e6ece8; }
.me-card span { color: #4f6258; font-size: 13px; }
.me-card b { font-size: 24px; color: #22332e; }
.me-card small { color: #8a968f; font-size: 12px; }
.me-card--green { background: #eef8f2; } .me-card--green b { color: #1f6f55; }
.me-card--red { background: #fdf2f1; } .me-card--red b { color: #c0392b; }
.me-card--purple { background: #f4f1fb; } .me-card--amber { background: #fdf7ec; }
.me-row { display: flex; gap: 10px; align-items: baseline; padding: 8px 0; border-bottom: 1px solid #eef2ef; font-size: 13px; }
.me-row span { flex: 1; color: #5d6b63; }
.me-pos { color: #1f6f55; }
.me-neg { color: #c0392b; }
</style>
