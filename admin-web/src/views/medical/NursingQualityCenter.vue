<template>
  <PageContainer title="护理与质量中心" subTitle="用药 / 巡查 / 护理计划 / 执行闭环 / 交接 / 药库 / 质量报表">
    <a-tabs v-model:activeKey="activeTab" type="card">
      <a-tab-pane key="medication" tab="用药管理">
        <a-space wrap>
          <a-button type="primary" @click="go('/health/medication/medication-setting')">用药设置</a-button>
          <a-button @click="go('/medical-care/medication-registration')">用药登记</a-button>
          <a-button @click="go('/health/medication/medication-remaining')">剩余用药</a-button>
          <a-button @click="go('/health/medication/drug-deposit')">带药外出/缴存</a-button>
          <a-button @click="go('/health/medication/drug-dictionary')">过敏/禁忌/重复提醒规则</a-button>
        </a-space>
      </a-tab-pane>
      <a-tab-pane key="vital" tab="健康数据与巡查">
        <a-space wrap>
          <a-button type="primary" @click="go('/health/management/data?date=today')">生命体征采集</a-button>
          <a-button @click="go('/medical-care/inspection?date=today')">健康巡检</a-button>
          <a-button @click="go('/medical-care/nursing-log?filter=pending')">护理日志</a-button>
          <a-button @click="go('/medical-care/inspection?filter=abnormal&date=today')">异常预警清单</a-button>
        </a-space>
      </a-tab-pane>
      <a-tab-pane key="plan" tab="护理计划与等级">
        <a-space wrap>
          <a-button type="primary" @click="go('/care/service/care-levels')">护理等级规则</a-button>
          <a-button @click="go('/care/service/service-items')">护理项目库</a-button>
          <a-button @click="go('/care/service/service-plans')">服务计划（周/月）</a-button>
          <a-button @click="go('/care/care-packages')">护理套餐库</a-button>
        </a-space>
      </a-tab-pane>
      <a-tab-pane key="task" tab="护理任务与扫码执行">
        <a-space wrap>
          <a-button type="primary" @click="go('/medical-care/care-task-board?date=today&filter=all')">今日任务清单</a-button>
          <a-button @click="go('/care/workbench/qr?mode=scan')">扫码执行</a-button>
          <a-button @click="go('/medical-care/care-task-board?date=today&filter=overdue')">超时与补录</a-button>
          <a-button @click="go('/medical-care/care-task-board?date=today&filter=overdue_or_missed')">任务异常与事故入口</a-button>
        </a-space>
      </a-tab-pane>
      <a-tab-pane key="handover" tab="交接班与班组">
        <a-space wrap>
          <a-button type="primary" @click="go('/medical-care/handovers?shift=当前班次')">交接清单</a-button>
          <a-button @click="go('/medical-care/handovers?tab=records')">交接记录</a-button>
          <a-button @click="go('/care/staff/caregiver-groups')">护士/护工分组</a-button>
          <a-button @click="go('/care/scheduling/shift-calendar')">排班查看与调整申请</a-button>
        </a-space>
      </a-tab-pane>
      <a-tab-pane key="pharmacy" tab="药库与药品主数据">
        <a-space wrap>
          <a-button type="primary" @click="go('/health/medication/drug-dictionary')">药品字典</a-button>
          <a-button @click="go('/logistics/storage/warehouse')">药库库存</a-button>
          <a-button @click="go('/logistics/storage/inbound')">入库</a-button>
          <a-button @click="go('/logistics/storage/outbound')">出库</a-button>
          <a-button @click="go('/logistics/storage/supplier')">供应商</a-button>
          <a-button @click="go('/logistics/storage/purchase?filter=pending')">领药申请/发药记录</a-button>
        </a-space>
      </a-tab-pane>
      <a-tab-pane key="quality" tab="质量与报表">
        <a-space wrap>
          <a-button type="primary" @click="go('/care/service/nursing-reports')">护理报表</a-button>
          <a-button @click="go('/stats/org/monthly-operation')">医嘱执行率/巡查覆盖率</a-button>
          <a-button @click="go('/survey/stats?filter=low_score_or_complaint')">投诉/问卷关联分析</a-button>
          <a-button @click="go('/oa/work-execution/task?filter=overdue_rectify')">整改任务闭环</a-button>
          <a-button @click="go('/hr/performance')">绩效数据输出</a-button>
        </a-space>
      </a-tab-pane>
    </a-tabs>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'

const router = useRouter()
const route = useRoute()
const activeTab = computed({
  get: () => (typeof route.query.tab === 'string' ? route.query.tab : 'medication'),
  set: (tab: string) => {
    router.replace({ query: { ...route.query, tab } })
  }
})

function go(path: string) {
  router.push(path)
}
</script>
