<template>
  <PageContainer
    title="家属服务中心"
    subTitle="把家属端可见数据、探访预约、请假外出、缴费充值、投诉建议和沟通处理集中到一个运营闭环"
    mode="showcase"
    kicker="家属体验"
  >
    <template #meta>
      <a-space wrap>
        <StatusTag :text="statusLabel(data.status)" :tone="statusTone(data.status)" />
        <StatusTag :text="`最近同步 ${data.generatedAt || '--'}`" tone="offline" />
      </a-space>
    </template>

    <template #extra>
      <a-space wrap>
        <a-button :loading="loading" @click="loadData">刷新</a-button>
        <a-button @click="openPath('/oa/family-users')">家属账号</a-button>
        <a-button @click="openPath('/oa/family-recharge')">充值台账</a-button>
        <a-button @click="openPath('/oa/family-service-health')">运行健康</a-button>
        <a-button @click="openPath('/oa/work-execution/family-communication')">反馈与沟通</a-button>
        <a-button type="primary" @click="openPath('/elder/status-change/visit-register')">探访登记</a-button>
      </a-space>
    </template>

    <section class="family-metric-grid">
      <button
        v-for="item in data.metrics"
        :key="item.key"
        type="button"
        class="family-metric"
        @click="openPath(item.routePath)"
      >
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
        <small>{{ item.helper }}</small>
      </button>
    </section>

    <SectionPanel title="运行摘要" description="把家属端正式环境最容易阻断闭环的支付、通知和沟通链路集中暴露，方便运营直接跟进。">
      <div class="runtime-grid">
        <button
          v-for="item in data.runtimeCards"
          :key="item.key"
          type="button"
          class="runtime-card"
          @click="openPath(item.routePath)"
        >
          <div class="row-title">
            <strong>{{ item.title }}</strong>
            <StatusTag :text="statusLabel(item.status)" :tone="statusTone(item.status)" />
          </div>
          <div class="runtime-value">{{ item.value }}</div>
          <div class="runtime-helper">{{ item.helper }}</div>
        </button>
      </div>
    </SectionPanel>

    <section class="family-layout">
      <SectionPanel title="家属服务闭环" description="每条链路都标出家属端接口、管理端入口和处理步骤，便于排查哪里还没有落地。">
        <div class="flow-list">
          <button
            v-for="item in data.flows"
            :key="item.key"
            type="button"
            class="flow-row"
            @click="openPath(item.routePath)"
          >
            <div class="flow-main">
              <div class="row-title">
                <strong>{{ item.title }}</strong>
                <StatusTag :text="statusLabel(item.status)" :tone="statusTone(item.status)" />
              </div>
              <div class="route-pair">
                <span>家属端：{{ item.familyApi }}</span>
                <span>管理端：{{ item.adminRoute }}</span>
              </div>
              <div class="step-row">
                <span v-for="step in item.steps" :key="step">{{ step }}</span>
              </div>
            </div>
          </button>
        </div>
      </SectionPanel>

      <SectionPanel title="待推进动作" description="把当前缺口转成运营责任人可处理的事项。">
        <div class="action-list">
          <button
            v-for="item in data.actions"
            :key="item.title"
            type="button"
            class="action-row"
            @click="openPath(item.routePath)"
          >
            <div>
              <strong>{{ item.title }}</strong>
              <span>{{ item.owner }} · {{ item.description }}</span>
            </div>
            <StatusTag :text="priorityLabel(item.priority)" :tone="priorityTone(item.priority)" />
          </button>
        </div>
      </SectionPanel>
    </section>

    <SectionPanel title="家属端可见数据范围" description="敏感数据要能说明来源、可见字段和管理端证据入口，后续再接关系授权与安全密码校验。">
      <div class="visible-grid">
        <button
          v-for="item in data.visibleData"
          :key="item.key"
          type="button"
          class="visible-card"
          @click="openPath(item.adminRoute)"
        >
          <div class="row-title">
            <strong>{{ item.title }}</strong>
            <StatusTag :text="statusLabel(item.status)" :tone="statusTone(item.status)" />
          </div>
          <div class="visible-route">{{ item.familyApi }}</div>
          <div class="field-row">
            <span v-for="field in item.fields" :key="field">{{ field }}</span>
          </div>
        </button>
      </div>
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
import { getOperationsFamilyService, type OperationsFamilyService } from '../../api/operations'

const router = useRouter()
const loading = ref(false)
const data = ref<OperationsFamilyService>({
  generatedAt: '',
  status: 'LANDING',
  metrics: [],
  runtimeCards: [],
  flows: [],
  visibleData: [],
  actions: []
})

function statusLabel(status?: string) {
  if (status === 'READY') return '已落地'
  if (status === 'PARTIAL') return '增强中'
  if (status === 'LANDING') return '落地中'
  return '待建设'
}

function statusTone(status?: string) {
  if (status === 'READY') return 'normal' as const
  if (status === 'PARTIAL' || status === 'LANDING') return 'pending' as const
  return 'offline' as const
}

function priorityLabel(priority?: string) {
  if (priority === 'HIGH') return '高优先'
  if (priority === 'MEDIUM') return '中优先'
  return '低优先'
}

function priorityTone(priority?: string) {
  if (priority === 'HIGH') return 'danger' as const
  if (priority === 'MEDIUM') return 'warning' as const
  return 'offline' as const
}

function openPath(path?: string) {
  if (!path) return
  router.push(path)
}

async function loadData() {
  loading.value = true
  try {
    data.value = await getOperationsFamilyService()
  } catch (error: any) {
    message.error(error?.message || '家属服务闭环加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.family-metric-grid,
.runtime-grid,
.family-layout,
.visible-grid {
  display: grid;
  gap: 16px;
}

.family-metric-grid {
  grid-template-columns: repeat(5, minmax(0, 1fr));
}

.family-layout {
  grid-template-columns: minmax(0, 1.35fr) minmax(340px, 0.65fr);
}

.runtime-grid {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.family-metric,
.runtime-card,
.flow-row,
.action-row,
.visible-card {
  width: 100%;
  border: 1px solid var(--border-soft);
  border-radius: 18px;
  background: var(--surface-2);
  text-align: left;
  cursor: pointer;
}

.family-metric {
  display: grid;
  gap: 10px;
  min-height: 128px;
  padding: 16px;
}

.runtime-card {
  display: grid;
  gap: 12px;
  min-height: 148px;
  padding: 18px;
}

.family-metric span,
.family-metric small,
.route-pair span,
.action-row span,
.visible-route {
  color: var(--muted);
  font-size: 12px;
  line-height: 1.6;
}

.family-metric strong {
  color: var(--ink);
  font-size: 26px;
}

.runtime-value {
  color: var(--ink);
  font-size: 30px;
  font-weight: 700;
  line-height: 1.1;
}

.runtime-helper {
  color: var(--muted);
  font-size: 12px;
  line-height: 1.7;
}

.flow-list,
.action-list {
  display: grid;
  gap: 12px;
}

.flow-row,
.action-row,
.visible-card {
  padding: 16px;
}

.action-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
}

.flow-main {
  display: grid;
  gap: 10px;
}

.row-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.row-title strong,
.action-row strong {
  color: var(--ink);
}

.route-pair,
.step-row,
.field-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.step-row span,
.field-row span {
  padding: 5px 9px;
  border-radius: 999px;
  background: rgba(var(--primary-rgb), 0.08);
  color: var(--primary-strong);
  font-size: 12px;
}

.visible-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.visible-card {
  display: grid;
  gap: 12px;
}

.visible-route {
  padding: 10px;
  border-radius: 12px;
  background: var(--surface-3);
  word-break: break-all;
}

@media (max-width: 1280px) {
  .family-metric-grid,
  .runtime-grid,
  .visible-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .family-layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .family-metric-grid,
  .runtime-grid,
  .visible-grid {
    grid-template-columns: 1fr;
  }

  .action-row {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
