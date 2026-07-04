<template>
  <PageContainer title="员工移动端 / 小程序" subTitle="护理员、医生和后勤的一线手机操作闭环">
    <template #extra>
      <a-space wrap>
        <a-tag :color="levelMeta.color">{{ levelMeta.text }}</a-tag>
        <a-button :loading="loading" @click="loadData">刷新</a-button>
        <a-button @click="openHealthData()">体征补录</a-button>
        <a-button @click="go('/oa/staff-collaboration')">员工协同</a-button>
        <a-button @click="go('/stats/staff-mobile-ledger')">现场台账</a-button>
        <a-button @click="go('/stats/operations')">返回驾驶舱</a-button>
      </a-space>
    </template>

    <a-alert
      class="mobile-alert"
      type="info"
      show-icon
      message="员工端应作为一线主入口，而不是后台网页的附属页"
      description="护理执行、查房、用药、巡检、维修和餐食配送都依赖手机端随身处理，建议把扫码、拍照、回执和消息推送统一到员工移动端。"
    />

    <a-row :gutter="[16, 16]" class="hero-row">
      <a-col :xs="24" :xl="8">
        <a-card :bordered="false" class="index-card">
          <div class="index-head">
            <span>员工移动端指数</span>
            <a-tag :color="levelMeta.color">{{ levelMeta.text }}</a-tag>
          </div>
          <div class="index-value">{{ data.mobileIndex }}</div>
          <a-progress :percent="data.mobileIndex" :stroke-color="indexStroke" :show-info="false" />
          <p class="index-note">更新时间 {{ data.generatedAt || '-' }}</p>
        </a-card>
      </a-col>

      <a-col :xs="24" :xl="16">
        <a-row :gutter="[12, 12]">
          <a-col v-for="metric in data.metrics" :key="metric.key" :xs="12" :lg="8">
            <button class="metric-tile" :class="`is-${metric.tone}`" type="button" @click="go(metric.routePath)">
              <span>{{ metric.label }}</span>
              <strong>{{ metric.value }}</strong>
              <small>{{ metric.helper }}</small>
            </button>
          </a-col>
        </a-row>
      </a-col>
    </a-row>

    <a-card :bordered="false" title="现场留痕摘要" class="runtime-card-panel">
      <template #extra>
        <a-space wrap>
          <a-button size="small" @click="openHealthData()">今日补录</a-button>
          <a-button size="small" type="primary" @click="openHealthData(true)">异常体征</a-button>
        </a-space>
      </template>
      <div class="runtime-grid">
        <button
          v-for="item in data.runtimeCards"
          :key="item.key"
          class="runtime-card"
          type="button"
          @click="go(item.routePath)"
        >
          <div class="row-head">
            <strong>{{ item.title }}</strong>
            <a-tag :color="statusColor(item.status)">{{ statusText(item.status) }}</a-tag>
          </div>
          <div class="runtime-value">{{ item.value }}</div>
          <p>{{ item.helper }}</p>
        </button>
      </div>
    </a-card>

    <a-row :gutter="[16, 16]">
      <a-col :xs="24" :xl="14">
        <a-card :bordered="false" title="角色移动场景">
          <div class="role-list">
            <button
              v-for="role in data.roleCards"
              :key="role.key"
              class="role-card"
              type="button"
              @click="go(role.routePath)"
            >
              <div class="row-head">
                <div>
                  <strong>{{ role.title }}</strong>
                  <span>{{ role.owner }}</span>
                </div>
                <a-tag :color="statusColor(role.status)">{{ statusText(role.status) }}</a-tag>
              </div>
              <div class="chip-block">
                <span v-for="scenario in role.scenarios" :key="scenario" class="soft-chip">{{ scenario }}</span>
              </div>
              <div class="chip-block mobile-needs">
                <span v-for="need in role.mobileNeeds" :key="need" class="mobile-chip">{{ need }}</span>
              </div>
            </button>
          </div>
        </a-card>
      </a-col>

      <a-col :xs="24" :xl="10">
        <a-card :bordered="false" title="手机端关键流程">
          <div class="workflow-list">
            <button
              v-for="workflow in data.workflows"
              :key="workflow.key"
              class="workflow-card"
              type="button"
              @click="go(workflow.routePath)"
            >
              <div class="row-head">
                <strong>{{ workflow.title }}</strong>
                <a-tag :color="statusColor(workflow.status)">{{ statusText(workflow.status) }}</a-tag>
              </div>
              <ol>
                <li v-for="step in workflow.steps" :key="step">{{ step }}</li>
              </ol>
            </button>
          </div>
        </a-card>

        <a-card :bordered="false" title="需要优先落地的动作" class="actions-card">
          <div class="action-list">
            <button
              v-for="action in data.actions"
              :key="action.title"
              class="action-card"
              type="button"
              @click="go(action.routePath)"
            >
              <div>
                <strong>{{ action.title }}</strong>
                <span>{{ action.owner }}</span>
              </div>
              <a-tag :color="priorityColor(action.priority)">{{ priorityText(action.priority) }}</a-tag>
              <p>{{ action.description }}</p>
            </button>
          </div>
        </a-card>
      </a-col>
    </a-row>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import { getOperationsStaffMobile, type OperationsStaffMobile } from '../../api/operations'

const router = useRouter()
const loading = ref(false)
const data = ref<OperationsStaffMobile>({
  generatedAt: '',
  status: '',
  mobileIndex: 0,
  mobileLevel: 'LOW',
  metrics: [],
  runtimeCards: [],
  roleCards: [],
  workflows: [],
  actions: []
})

const levelMeta = computed(() => {
  if (data.value.mobileLevel === 'CRITICAL') return { text: '紧急缺口', color: 'red' }
  if (data.value.mobileLevel === 'HIGH') return { text: '高优先', color: 'volcano' }
  if (data.value.mobileLevel === 'MEDIUM') return { text: '需增强', color: 'gold' }
  return { text: '可用', color: 'green' }
})

const indexStroke = computed(() => {
  if (data.value.mobileIndex >= 75) return '#d4380d'
  if (data.value.mobileIndex >= 50) return '#fa8c16'
  if (data.value.mobileIndex >= 25) return '#d4b106'
  return '#389e0d'
})

function go(path?: string) {
  if (!path) return
  router.push(path)
}

function openHealthData(abnormalOnly = false) {
  router.push({
    path: '/health/management/data',
    query: {
      source: 'staff-mobile',
      date: 'today',
      ...(abnormalOnly ? { abnormalFlag: '1' } : {})
    }
  })
}

function statusText(status?: string) {
  if (status === 'READY') return '已落地'
  if (status === 'PARTIAL') return '增强中'
  if (status === 'PLANNED') return '待建设'
  if (status === 'WARNING') return '需关注'
  return status || '未知'
}

function statusColor(status?: string) {
  if (status === 'READY') return 'green'
  if (status === 'PARTIAL') return 'blue'
  if (status === 'PLANNED') return 'default'
  if (status === 'WARNING') return 'orange'
  return 'default'
}

function priorityText(priority?: string) {
  if (priority === 'HIGH') return '高'
  if (priority === 'MEDIUM') return '中'
  return '低'
}

function priorityColor(priority?: string) {
  if (priority === 'HIGH') return 'red'
  if (priority === 'MEDIUM') return 'gold'
  return 'green'
}

async function loadData() {
  loading.value = true
  try {
    data.value = await getOperationsStaffMobile()
  } catch (error: any) {
    message.error(error?.message || '加载员工移动端中心失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.mobile-alert {
  margin-bottom: 16px;
}

.hero-row {
  margin-bottom: 16px;
}

.runtime-card-panel {
  margin-bottom: 16px;
}

.index-card {
  height: 100%;
  background: linear-gradient(160deg, #f8fbff 0%, #eef5ff 100%);
  border: 1px solid var(--border-soft);
}

.index-head,
.row-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.index-head span {
  color: var(--muted);
}

.index-value {
  margin: 14px 0 6px;
  color: var(--ink);
  font-size: 42px;
  font-weight: 700;
  line-height: 1;
}

.index-note {
  margin: 12px 0 0;
  color: var(--muted);
  font-size: 13px;
}

.metric-tile,
.runtime-card,
.role-card,
.workflow-card,
.action-card {
  width: 100%;
  border: 1px solid var(--border-soft);
  border-radius: 14px;
  background: #fff;
  text-align: left;
  cursor: pointer;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}

.metric-tile:hover,
.runtime-card:hover,
.role-card:hover,
.workflow-card:hover,
.action-card:hover {
  border-color: var(--primary);
  box-shadow: var(--shadow-md);
  transform: translateY(-1px);
}

.metric-tile {
  min-height: 110px;
  padding: 16px;
}

.metric-tile span,
.metric-tile small,
.role-card span,
.workflow-card li,
.action-card span,
.action-card p {
  color: var(--muted);
  font-size: 12px;
  line-height: 1.6;
}

.metric-tile strong {
  display: block;
  margin: 8px 0;
  color: var(--ink);
  font-size: 22px;
  line-height: 1.2;
}

.runtime-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.runtime-card {
  display: grid;
  gap: 10px;
  min-height: 148px;
  padding: 16px;
}

.runtime-card strong {
  color: var(--ink);
}

.runtime-card p {
  margin: 0;
  color: var(--muted);
  font-size: 12px;
  line-height: 1.6;
}

.runtime-value {
  color: var(--ink);
  font-size: 28px;
  font-weight: 700;
  line-height: 1.1;
}

.role-list,
.workflow-list,
.action-list {
  display: grid;
  gap: 12px;
}

.role-card,
.workflow-card,
.action-card {
  padding: 14px;
}

.chip-block {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

.soft-chip,
.mobile-chip {
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  padding: 0 10px;
  border-radius: 999px;
  font-size: 12px;
  line-height: 1;
}

.soft-chip {
  background: var(--surface-2);
  color: var(--muted);
}

.mobile-chip {
  background: var(--primary-soft);
  color: var(--primary-strong);
}

.mobile-needs {
  margin-top: 10px;
}

.workflow-card ol {
  margin: 12px 0 0;
  padding-left: 18px;
  color: var(--muted);
}

.action-card {
  display: grid;
  gap: 6px;
}

.action-card strong {
  color: var(--ink);
}

@media (max-width: 991px) {
  .index-value {
    font-size: 36px;
  }

  .runtime-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .runtime-grid {
    grid-template-columns: 1fr;
  }
}
</style>
