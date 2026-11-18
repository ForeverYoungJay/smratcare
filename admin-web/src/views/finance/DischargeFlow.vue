<template>
  <div class="discharge-flow">
    <a-steps :current="currentIndex" type="navigation" size="small" class="discharge-flow__steps" @change="(index: number) => go(STEPS[index].key)">
      <a-step v-for="step in STEPS" :key="step.key" :title="step.title" :description="step.hint" />
    </a-steps>
    <component :is="current.component" :key="current.key" />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import DischargeFeeAudit from './DischargeFeeAudit.vue'
import DischargeSettlement from './DischargeSettlement.vue'
import DischargeStatusSync from './DischargeStatusSync.vue'
import { pickDischargeStep } from './dischargeSteps'

/** 退住结算按步骤推进：费用审核 → 结算与退款 → 状态回写，三个原页面合在一个流程里 */
const STEPS = [
  { key: 'review', title: '费用审核', hint: '核对未消费预收、押金与未结应收', component: DischargeFeeAudit },
  { key: 'settle', title: '结算与退款', hint: '生成结算单、主管审批后退款', component: DischargeSettlement },
  { key: 'sync', title: '状态回写', hint: '回写长者档案与床位状态', component: DischargeStatusSync }
]

const router = useRouter()
const current = computed(() => STEPS.find((step) => step.key === pickDischargeStep(router.currentRoute.value.query.step))!)
const currentIndex = computed(() => STEPS.indexOf(current.value))

// 用真实路由改 step，保留工作区的 view 参数；切步骤时丢掉上一步页面的筛选
function go(step: string) {
  const { view } = router.currentRoute.value.query
  router.replace({ query: { ...(view ? { view } : {}), step } })
}
</script>

<style scoped>
.discharge-flow__steps { margin-bottom: 12px; padding: 4px 0; background: var(--surface, #fff); }
</style>
