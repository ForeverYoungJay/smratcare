<template>
  <PageContainer
    title="家属服务健康中心"
    subTitle="把短信验证码、微信通知与充值支付的正式运行状态集中巡检，帮助运营和财务快速定位阻断链路"
    mode="showcase"
    kicker="正式环境巡检"
  >
    <template #meta>
      <a-space wrap>
        <StatusTag :text="levelText(data.level)" :tone="levelTone(data.level)" />
        <StatusTag :text="`最近巡检 ${data.checkedAt || '--'}`" tone="offline" />
      </a-space>
    </template>

    <template #extra>
      <a-space wrap>
        <a-select v-model:value="hours" style="width: 160px" @change="loadData">
          <a-select-option :value="6">近 6 小时</a-select-option>
          <a-select-option :value="24">近 24 小时</a-select-option>
          <a-select-option :value="72">近 72 小时</a-select-option>
          <a-select-option :value="168">近 7 天</a-select-option>
        </a-select>
        <a-button :loading="loading" @click="loadData">刷新</a-button>
        <a-button @click="openPath('/oa/family-service')">返回家属服务中心</a-button>
        <a-button type="primary" @click="openPath('/oa/work-execution/family-communication')">处理沟通与通知</a-button>
      </a-space>
    </template>

    <a-alert
      class="health-alert"
      :type="levelAlertType(data.level)"
      show-icon
      :message="data.summary || '等待巡检结果'"
      description="正式环境优先关注失败短信、失败通知、待重试消息和支付异常订单，避免家属端出现静默失败。"
    />

    <section class="summary-grid">
      <button class="summary-card" type="button" @click="openPath('/oa/family-service')">
        <span>短信验证码</span>
        <strong>{{ data.sms.failed }}</strong>
        <small>失败 {{ data.sms.failed }} / 已发送 {{ data.sms.sent }} / 已验证 {{ data.sms.verified }}</small>
      </button>
      <button class="summary-card" type="button" @click="openPath('/oa/work-execution/family-communication')">
        <span>微信通知</span>
        <strong>{{ data.notify.failed + data.notify.pendingRetry }}</strong>
        <small>失败 {{ data.notify.failed }} / 待重试 {{ data.notify.pendingRetry }} / 成功 {{ data.notify.success }}</small>
      </button>
      <button class="summary-card" type="button" @click="openPath('/oa/family-recharge')">
        <span>支付异常订单</span>
        <strong>{{ data.recharge.failed + data.recharge.closed }}</strong>
        <small>失败 {{ data.recharge.failed }} / 关闭 {{ data.recharge.closed }} / 已支付 {{ data.recharge.paid }}</small>
      </button>
      <button class="summary-card" type="button" @click="openPath('/oa/family-recharge')">
        <span>支付到账金额</span>
        <strong>{{ moneyText(data.recharge.paidAmount) }}</strong>
        <small>巡检窗口总额 {{ moneyText(data.recharge.totalAmount) }}</small>
      </button>
    </section>

    <section class="health-layout">
      <SectionPanel title="运行指标" description="分别看短信、通知和支付的成功与失败分布，定位链路是否集中异常。">
        <div class="channel-grid">
          <div class="channel-card">
            <div class="card-head">
              <strong>短信验证码</strong>
              <StatusTag :text="data.sms.failed > 0 ? '需关注' : '稳定'" :tone="data.sms.failed > 0 ? 'warning' : 'normal'" />
            </div>
            <div class="metric-stack">
              <div><span>总量</span><b>{{ data.sms.total }}</b></div>
              <div><span>已发送</span><b>{{ data.sms.sent }}</b></div>
              <div><span>已验证</span><b>{{ data.sms.verified }}</b></div>
              <div><span>已使用</span><b>{{ data.sms.used }}</b></div>
              <div><span>已过期</span><b>{{ data.sms.expired }}</b></div>
              <div><span>失败</span><b>{{ data.sms.failed }}</b></div>
            </div>
          </div>

          <div class="channel-card">
            <div class="card-head">
              <strong>微信通知</strong>
              <StatusTag
                :text="data.notify.failed + data.notify.pendingRetry > 0 ? '需关注' : '稳定'"
                :tone="data.notify.failed + data.notify.pendingRetry > 0 ? 'warning' : 'normal'"
              />
            </div>
            <div class="metric-stack">
              <div><span>总量</span><b>{{ data.notify.total }}</b></div>
              <div><span>成功</span><b>{{ data.notify.success }}</b></div>
              <div><span>失败</span><b>{{ data.notify.failed }}</b></div>
              <div><span>待重试</span><b>{{ data.notify.pendingRetry }}</b></div>
              <div><span>处理中</span><b>{{ data.notify.pending }}</b></div>
              <div><span>已跳过</span><b>{{ data.notify.skipped }}</b></div>
            </div>
          </div>

          <div class="channel-card">
            <div class="card-head">
              <strong>充值支付</strong>
              <StatusTag
                :text="data.recharge.failed + data.recharge.closed > 0 ? '需关注' : '稳定'"
                :tone="data.recharge.failed + data.recharge.closed > 0 ? 'warning' : 'normal'"
              />
            </div>
            <div class="metric-stack">
              <div><span>订单总数</span><b>{{ data.recharge.total }}</b></div>
              <div><span>待创建</span><b>{{ data.recharge.init }}</b></div>
              <div><span>待支付</span><b>{{ data.recharge.prepayCreated }}</b></div>
              <div><span>已支付</span><b>{{ data.recharge.paid }}</b></div>
              <div><span>失败</span><b>{{ data.recharge.failed }}</b></div>
              <div><span>已关闭</span><b>{{ data.recharge.closed }}</b></div>
            </div>
          </div>
        </div>
      </SectionPanel>

      <SectionPanel title="处置建议" description="把当前巡检结果直接转成运营和财务可以执行的动作，减少排查成本。">
        <div class="suggestion-list">
          <div v-for="item in data.suggestions" :key="item" class="suggestion-item">
            {{ item }}
          </div>
        </div>
      </SectionPanel>
    </section>

    <SectionPanel title="高频错误样本" description="保留当前窗口内最集中出现的错误原因，帮助判断是配置类问题还是业务类问题。">
      <div v-if="data.topErrors.length" class="error-list">
        <div v-for="item in data.topErrors" :key="`${item.source}-${item.reason}`" class="error-row">
          <div>
            <strong>{{ sourceText(item.source) }}</strong>
            <p>{{ item.reason }}</p>
          </div>
          <span>{{ item.count }} 次</span>
        </div>
      </div>
      <a-empty v-else description="当前巡检窗口内没有错误样本" />
    </SectionPanel>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import SectionPanel from '../../components/smartcare/SectionPanel.vue'
import StatusTag from '../../components/smartcare/StatusTag.vue'
import { getOperationsFamilyOpsHealth, type OperationsFamilyOpsHealth } from '../../api/operations'

const router = useRouter()
const loading = ref(false)
const hours = ref(24)
const data = ref<OperationsFamilyOpsHealth>({
  checkedAt: '',
  windowHours: 24,
  level: 'healthy',
  summary: '',
  sms: { total: 0, sent: 0, verified: 0, used: 0, expired: 0, failed: 0 },
  notify: { total: 0, success: 0, failed: 0, pending: 0, skipped: 0, pendingRetry: 0 },
  recharge: { total: 0, init: 0, prepayCreated: 0, paid: 0, closed: 0, failed: 0, totalAmount: 0, paidAmount: 0 },
  topErrors: [],
  suggestions: []
})

function openPath(path?: string) {
  if (!path) return
  router.push(path)
}

function levelText(level?: string) {
  if (level === 'critical') return '高风险'
  if (level === 'warning') return '需关注'
  return '健康'
}

function levelTone(level?: string) {
  if (level === 'critical') return 'danger' as const
  if (level === 'warning') return 'warning' as const
  return 'normal' as const
}

function levelAlertType(level?: string) {
  if (level === 'critical') return 'error' as const
  if (level === 'warning') return 'warning' as const
  return 'success' as const
}

function sourceText(source?: string) {
  if (source === 'SMS') return '短信验证码'
  if (source === 'WECHAT_NOTIFY') return '微信通知'
  if (source === 'WECHAT_PAY') return '微信支付'
  return source || '未知来源'
}

function moneyText(value?: number) {
  const amount = Number(value || 0)
  return `¥${amount.toFixed(2)}`
}

async function loadData() {
  loading.value = true
  try {
    data.value = await getOperationsFamilyOpsHealth(hours.value)
  } catch (error: any) {
    message.error(error?.message || '家属服务健康中心加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.health-alert,
.summary-grid {
  margin-bottom: 16px;
}

.summary-grid,
.health-layout,
.channel-grid {
  display: grid;
  gap: 16px;
}

.summary-grid {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.health-layout {
  grid-template-columns: minmax(0, 1.35fr) minmax(320px, 0.65fr);
}

.channel-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.summary-card,
.channel-card,
.error-row,
.suggestion-item {
  border: 1px solid var(--border-soft);
  border-radius: 18px;
  background: var(--surface-2);
}

.summary-card {
  display: grid;
  gap: 10px;
  min-height: 132px;
  padding: 18px;
  text-align: left;
  cursor: pointer;
}

.summary-card span,
.summary-card small,
.metric-stack span,
.error-row p,
.suggestion-item {
  color: var(--muted);
  font-size: 12px;
  line-height: 1.7;
}

.summary-card strong {
  color: var(--ink);
  font-size: 30px;
  line-height: 1.1;
}

.channel-card {
  display: grid;
  gap: 14px;
  padding: 18px;
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.card-head strong,
.error-row strong {
  color: var(--ink);
}

.metric-stack {
  display: grid;
  gap: 10px;
}

.metric-stack div {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.metric-stack b {
  color: var(--primary-strong);
  font-size: 16px;
}

.suggestion-list,
.error-list {
  display: grid;
  gap: 12px;
}

.suggestion-item {
  padding: 14px 16px;
}

.error-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 16px;
}

.error-row p {
  margin: 8px 0 0;
}

.error-row span {
  color: var(--primary-strong);
  font-size: 13px;
  white-space: nowrap;
}

@media (max-width: 1280px) {
  .summary-grid,
  .channel-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .health-layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .summary-grid,
  .channel-grid {
    grid-template-columns: 1fr;
  }

  .error-row {
    flex-direction: column;
  }
}
</style>
