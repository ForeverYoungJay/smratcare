<template>
  <PageContainer title="月结进度" subTitle="按月追踪账单、收款、票据、异常与催缴的关账状态">
    <div class="close-shell">
      <a-card class="close-hero card-elevated" :bordered="false">
        <div>
          <div class="close-hero__eyebrow">Month Close</div>
          <h2>财务月结不再靠口头确认，而是直接看到哪一步卡住。</h2>
          <p>这页把本月账单、票据、异常修复和催缴收口状态压成一张进度图。</p>
        </div>
        <a-form layout="inline" :model="query" class="search-form">
          <a-form-item label="月份">
            <a-date-picker v-model:value="query.month" picker="month" style="width: 160px" />
          </a-form-item>
          <a-form-item>
            <a-space>
              <a-button type="primary" @click="loadData">刷新摘要</a-button>
              <a-button @click="exportData">导出月结单</a-button>
              <a-button @click="printSummary">打印月结单</a-button>
              <a-button type="primary" ghost :disabled="summary?.locked" @click="openCloseModal">发起关账</a-button>
              <a-button v-if="summary?.locked" danger @click="openUnlockModal">反锁账</a-button>
              <a-button v-if="summary?.locked" @click="openCrossPeriodModal">跨期申请</a-button>
              <a-button @click="go('/finance/reconcile/issue-center')">异常修复中心</a-button>
              <a-button @click="go('/finance/bills/follow-up')">催缴跟进</a-button>
            </a-space>
          </a-form-item>
        </a-form>
      </a-card>

      <StatefulBlock style="margin-top: 16px;" :loading="loading" :error="errorMessage" @retry="loadData">
        <a-row :gutter="[14, 14]">
          <a-col :xs="24" :xl="8">
            <a-card class="progress-card card-elevated" :bordered="false">
              <div class="progress-card__header">
                <div>
                  <div class="progress-card__label">完成度</div>
                  <div class="progress-card__month">{{ summary?.month || '-' }}</div>
                </div>
                <a-progress type="circle" :percent="summary?.completionRate || 0" :stroke-color="progressColor" />
              </div>
              <div class="progress-card__meta">
                <a-tag color="green">已完成 {{ summary?.completedSteps || 0 }}</a-tag>
                <a-tag color="orange">处理中 {{ summary?.warningSteps || 0 }}</a-tag>
                <a-tag color="red">阻塞 {{ summary?.blockedSteps || 0 }}</a-tag>
                <a-tag :color="summary?.closeStatus === 'OPEN' ? 'default' : summary?.closeStatus === 'CLOSED' ? 'green' : 'orange'">
                  {{ summary?.closeStatusLabel || '未关账' }}
                </a-tag>
                <a-tag :color="summary?.locked ? 'red' : 'blue'">{{ summary?.lockStatusLabel || '未锁账' }}</a-tag>
              </div>
            </a-card>
          </a-col>
          <a-col :xs="24" :xl="16">
            <a-row :gutter="[14, 14]">
              <a-col :xs="24" :md="8"><a-card class="card-elevated summary-slab" :bordered="false"><a-statistic title="本月账单" :value="summary?.billCount || 0" /></a-card></a-col>
              <a-col :xs="24" :md="8"><a-card class="card-elevated summary-slab" :bordered="false"><a-statistic title="本月收款" :value="summary?.paymentCount || 0" /></a-card></a-col>
              <a-col :xs="24" :md="8"><a-card class="card-elevated summary-slab" :bordered="false"><a-statistic title="未结账单" :value="summary?.outstandingBillCount || 0" /></a-card></a-col>
              <a-col :xs="24" :md="8"><a-card class="card-elevated summary-slab" :bordered="false"><a-statistic title="待补票据" :value="summary?.unlinkedInvoiceCount || 0" /></a-card></a-col>
              <a-col :xs="24" :md="8"><a-card class="card-elevated summary-slab" :bordered="false"><a-statistic title="修正记录" :value="summary?.adjustmentCount || 0" /></a-card></a-col>
              <a-col :xs="24" :md="8"><a-card class="card-elevated summary-slab" :bordered="false"><a-statistic title="一致性问题" :value="summary?.issueCount || 0" /></a-card></a-col>
            </a-row>
          </a-col>
        </a-row>

        <a-row :gutter="[16, 16]" style="margin-top: 16px;">
          <a-col :xs="24" :xl="16">
            <a-card class="card-elevated" :bordered="false" title="月结检查链路">
              <div class="step-list">
                <div v-for="step in summary?.steps || []" :key="step.key" class="step-card">
                  <div class="step-card__head">
                    <div>
                      <div class="step-card__title">{{ step.label }}</div>
                      <div class="step-card__detail">{{ step.detail }}</div>
                    </div>
                    <a-tag :color="stepColor(step.status)">{{ step.statusLabel }}</a-tag>
                  </div>
                  <div class="step-card__foot">
                    <span>当前数量 {{ step.count || 0 }}</span>
                    <a-button v-if="step.actionPath" type="link" @click="go(step.actionPath)">{{ step.actionLabel || '查看' }}</a-button>
                  </div>
                </div>
              </div>
            </a-card>
          </a-col>
          <a-col :xs="24" :xl="8">
            <a-card class="card-elevated" :bordered="false" title="月结备注">
              <a-alert
                show-icon
                :type="summary?.closeStatus === 'CLOSED' ? 'success' : summary?.closeStatus === 'FORCED_CLOSED' ? 'warning' : 'info'"
                :message="summary?.closeStatusLabel || '未关账'"
                :description="summary?.locked ? `锁账人：${summary?.closedBy || '-'} / ${summary?.closedAt || '-'}${summary?.unlockedAt ? `；最近反锁：${summary?.unlockedAt} / ${summary?.unlockedBy || '-'}` : ''}` : (summary?.unlockedAt ? `最近反锁：${summary?.unlockedAt} / ${summary?.unlockedBy || '-'}` : '本月尚未执行关账动作。')"
                style="margin-bottom: 16px;"
              />
              <a-alert
                v-if="summary?.unlockReason"
                type="warning"
                show-icon
                :message="`反锁原因：${summary.unlockReason}`"
                style="margin-bottom: 16px;"
              />
              <a-timeline>
                <a-timeline-item v-for="(note, index) in summary?.notes || []" :key="`${index}-${note}`" color="blue">
                  {{ note }}
                </a-timeline-item>
              </a-timeline>
            </a-card>
          </a-col>
        </a-row>
      </StatefulBlock>
    </div>
    <a-modal v-model:open="closeVisible" title="月结关账" :confirm-loading="submitLoading" @ok="submitClose">
      <a-alert
        show-icon
        :type="summary?.canClose ? 'success' : 'warning'"
        :message="summary?.canClose ? '当前可执行正式关账' : '当前仍有阻塞步骤，默认不建议直接关账'"
        :description="summary?.closeStatus && summary?.closeStatus !== 'OPEN' ? `最近关账：${summary?.closedAt || '-'} / ${summary?.closedBy || '-'}` : '本月尚未记录关账动作。'"
      />
      <a-form layout="vertical" :model="closeForm" style="margin-top: 16px;">
        <a-form-item label="关账备注">
          <a-textarea v-model:value="closeForm.remark" :rows="4" />
        </a-form-item>
        <a-form-item>
          <a-checkbox v-model:checked="closeForm.forceClose">即使有阻塞项，也按强制关账执行</a-checkbox>
        </a-form-item>
      </a-form>
    </a-modal>
    <a-modal v-model:open="unlockVisible" title="反锁账" :confirm-loading="submitLoading" @ok="submitUnlock">
      <a-form layout="vertical" :model="unlockForm">
        <a-form-item label="反锁原因">
          <a-input v-model:value="unlockForm.reason" allow-clear />
        </a-form-item>
        <a-form-item label="补充说明">
          <a-textarea v-model:value="unlockForm.remark" :rows="4" />
        </a-form-item>
      </a-form>
    </a-modal>
    <a-modal v-model:open="crossPeriodVisible" title="跨期处理申请" :confirm-loading="submitLoading" @ok="submitCrossPeriod">
      <a-form layout="vertical" :model="crossPeriodForm">
        <a-form-item label="动作类型">
          <a-select v-model:value="crossPeriodForm.actionType">
            <a-select-option value="PAYMENT_UPDATE">修改收款</a-select-option>
            <a-select-option value="BILL_INVALIDATE">作废账单</a-select-option>
            <a-select-option value="BILL_REPAIR">修复账单</a-select-option>
            <a-select-option value="OTHER">其他跨期调整</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="申请原因">
          <a-input v-model:value="crossPeriodForm.reason" allow-clear />
        </a-form-item>
        <a-form-item label="补充说明">
          <a-textarea v-model:value="crossPeriodForm.remark" :rows="4" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import StatefulBlock from '../../components/StatefulBlock.vue'
import { executeFinanceMonthClose, exportFinanceMonthCloseCsv, getFinanceMonthCloseSummary, requestFinanceCrossPeriodApproval, unlockFinanceMonthClose } from '../../api/finance'
import type { FinanceMonthCloseSummary } from '../../types'
import { printTableReport } from '../../utils/print'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const errorMessage = ref('')
const summary = ref<FinanceMonthCloseSummary | null>(null)
const closeVisible = ref(false)
const unlockVisible = ref(false)
const crossPeriodVisible = ref(false)
const submitLoading = ref(false)
const closeForm = reactive({
  remark: '',
  forceClose: false
})
const unlockForm = reactive({
  reason: '',
  remark: ''
})
const crossPeriodForm = reactive({
  actionType: 'OTHER',
  reason: '',
  remark: ''
})
const query = reactive({
  month: typeof route.query.month === 'string' ? dayjs(`${route.query.month}-01`) : dayjs().startOf('month')
})

const progressColor = computed(() => {
  const percent = Number(summary.value?.completionRate || 0)
  if (percent >= 90) return '#15803d'
  if (percent >= 60) return '#d97706'
  return '#b91c1c'
})

function go(path: string) {
  router.push(path)
}

function stepColor(status?: string) {
  if (status === 'COMPLETED') return 'green'
  if (status === 'BLOCKED') return 'red'
  return 'orange'
}

function openCloseModal() {
  closeForm.remark = summary.value?.closeRemark || ''
  closeForm.forceClose = false
  closeVisible.value = true
}

function openUnlockModal() {
  unlockForm.reason = summary.value?.unlockReason || ''
  unlockForm.remark = ''
  unlockVisible.value = true
}

function openCrossPeriodModal() {
  crossPeriodForm.actionType = 'OTHER'
  crossPeriodForm.reason = ''
  crossPeriodForm.remark = ''
  crossPeriodVisible.value = true
}

async function loadData() {
  loading.value = true
  errorMessage.value = ''
  try {
    summary.value = await getFinanceMonthCloseSummary({
      month: dayjs(query.month).format('YYYY-MM')
    })
  } catch (error: any) {
    errorMessage.value = error?.message || '加载月结摘要失败'
    message.error(errorMessage.value)
  } finally {
    loading.value = false
  }
}

async function submitClose() {
  submitLoading.value = true
  try {
    const res = await executeFinanceMonthClose({
      month: dayjs(query.month).format('YYYY-MM'),
      remark: closeForm.remark,
      forceClose: closeForm.forceClose
    })
    if (res.success) {
      message.success(res.message)
      closeVisible.value = false
      loadData()
      return
    }
    message.warning(res.message)
  } catch (error: any) {
    message.error(error?.message || '关账失败')
  } finally {
    submitLoading.value = false
  }
}

async function submitUnlock() {
  submitLoading.value = true
  try {
    await unlockFinanceMonthClose({
      month: dayjs(query.month).format('YYYY-MM'),
      reason: unlockForm.reason,
      remark: unlockForm.remark
    })
    message.success('已完成反锁账')
    unlockVisible.value = false
    loadData()
  } catch (error: any) {
    message.error(error?.message || '反锁失败')
  } finally {
    submitLoading.value = false
  }
}

async function submitCrossPeriod() {
  submitLoading.value = true
  try {
    const res = await requestFinanceCrossPeriodApproval({
      month: dayjs(query.month).format('YYYY-MM'),
      actionType: crossPeriodForm.actionType,
      reason: crossPeriodForm.reason,
      remark: crossPeriodForm.remark
    })
    message.success(res.message || '已提交跨期审批')
    crossPeriodVisible.value = false
  } catch (error: any) {
    message.error(error?.message || '提交失败')
  } finally {
    submitLoading.value = false
  }
}

async function exportData() {
  try {
    await exportFinanceMonthCloseCsv({
      month: dayjs(query.month).format('YYYY-MM')
    })
    message.success('月结单已导出')
  } catch (error: any) {
    message.error(error?.message || '导出失败')
  }
}

function printSummary() {
  if (!summary.value) {
    message.warning('暂无可打印的月结摘要')
    return
  }
  printTableReport({
    title: '财务月结单',
    subtitle: `月份：${summary.value.month}；关账状态：${summary.value.closeStatusLabel || '未关账'}；关账人：${summary.value.closedBy || '-'}；关账时间：${summary.value.closedAt || '-'}`,
    columns: [
      { key: 'label', title: '步骤' },
      { key: 'statusLabel', title: '状态' },
      { key: 'count', title: '数量' },
      { key: 'detail', title: '说明' }
    ],
    rows: (summary.value.steps || []).map(item => ({
      label: item.label,
      statusLabel: item.statusLabel,
      count: item.count,
      detail: item.detail
    })),
    signatures: ['财务经办', '财务主管']
  })
}

onMounted(loadData)
</script>

<style scoped>
.close-hero {
  display: grid;
  gap: 18px;
  grid-template-columns: 1.1fr 1fr;
  background:
    radial-gradient(circle at top left, rgba(255, 214, 10, 0.28), transparent 30%),
    linear-gradient(135deg, #1f2937 0%, #334155 50%, #0f766e 100%);
  color: #fff;
}

.close-hero__eyebrow {
  font-size: 12px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0.72);
}

.close-hero h2 {
  margin: 10px 0;
  font-size: 28px;
  line-height: 1.18;
}

.close-hero p {
  margin: 0;
  color: rgba(255, 255, 255, 0.76);
}

.progress-card {
  min-height: 236px;
  border-radius: 18px;
}

.progress-card__header {
  display: flex;
  justify-content: space-between;
  gap: 14px;
  align-items: center;
}

.progress-card__label {
  font-size: 13px;
  color: #64748b;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.progress-card__month {
  margin-top: 10px;
  font-size: 30px;
  font-weight: 700;
  color: #0f172a;
}

.progress-card__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 20px;
}

.summary-slab,
.step-card {
  border-radius: 18px;
}

.step-list {
  display: grid;
  gap: 12px;
}

.step-card {
  padding: 16px;
  background: linear-gradient(180deg, #ffffff, #f8fafc);
  border: 1px solid #e2e8f0;
}

.step-card__head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.step-card__title {
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
}

.step-card__detail {
  margin-top: 6px;
  color: #64748b;
}

.step-card__foot {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  margin-top: 12px;
  color: #475569;
}

@media (max-width: 1100px) {
  .close-hero {
    grid-template-columns: 1fr;
  }
}
</style>
