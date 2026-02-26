<template>
  <PageContainer title="在院服务总览" subTitle="护理/膳食/医疗/活动/费用一体化视图">
    <a-row :gutter="16">
      <a-col v-for="item in modules" :key="item.title" :xs="24" :sm="12" :lg="8" style="margin-bottom: 16px">
        <a-card :title="item.title" class="card-elevated" :bordered="false">
          <div class="desc">{{ item.desc }}</div>
          <a-space wrap style="margin-top: 12px">
            <a-button v-for="action in item.actions" :key="action.label" :type="action.primary ? 'primary' : 'default'" @click="go(action.path)">
              {{ action.label }}
            </a-button>
          </a-space>
        </a-card>
      </a-col>
    </a-row>
  </PageContainer>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import PageContainer from '../../../components/PageContainer.vue'

const router = useRouter()

const modules = [
  {
    title: '护理',
    desc: '查看今日照护任务、完成率与异常清单，联动护理计划。',
    actions: [
      { label: '今日任务', path: '/care/workbench/task-board?date=today', primary: true },
      { label: '护理计划', path: '/care/workbench/plan' }
    ]
  },
  {
    title: '膳食',
    desc: '查看订餐状态、禁忌与异常退餐，外出自动暂停送餐。',
    actions: [
      { label: '今日订餐', path: '/dining/order?date=today', primary: true },
      { label: '膳食方案', path: '/dining/recipe' }
    ]
  },
  {
    title: '医疗',
    desc: '查看体征、医嘱、用药执行，支持外出就医登记与随访。',
    actions: [
      { label: '医护工作站', path: '/life/health-basic', primary: true },
      { label: '外出就医登记', path: '/elder/status-change?event=medical_out' }
    ]
  },
  {
    title: '活动',
    desc: '查看活动参与、预告和相册内容，支持家属可见控制。',
    actions: [
      { label: '活动日历', path: '/life/activity', primary: true },
      { label: '相册', path: '/life/activity?tab=album' }
    ]
  },
  {
    title: '费用',
    desc: '账户余额、押金、账单与交易流水；支持自动催缴和预警。',
    actions: [
      { label: '账单详情', path: '/finance/resident-bill?period=this_month', primary: true },
      { label: '费用流水', path: '/finance/resident-bill-log' }
    ]
  }
]

function go(path: string) {
  router.push(path)
}
</script>

<style scoped>
.desc {
  color: #595959;
  line-height: 1.8;
}
</style>
