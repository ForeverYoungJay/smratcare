<template>
  <PageContainer title="入住状态变更" subTitle="外出/来访/试住/退住/外出就医/死亡统一入口，自动触发跨部门任务包">
    <a-alert
      type="error"
      show-icon
      message="待处理事件：未闭环事故 1、待审批退住 2、外出超时未返院 1"
      style="margin-bottom: 16px"
    />

    <a-row :gutter="16" style="margin-bottom: 16px">
      <a-col v-for="item in statusItems" :key="item.status" :xs="12" :sm="8" :md="6" :lg="4" style="margin-bottom: 12px">
        <a-card class="card-elevated" :bordered="false" hoverable @click="go(item.path)">
          <a-statistic :title="item.status" :value="item.count" />
        </a-card>
      </a-col>
    </a-row>

    <a-card title="状态变更动作" class="card-elevated" :bordered="false">
      <a-space wrap>
        <a-button type="primary" @click="go('/elder/outing')">外出登记</a-button>
        <a-button @click="go('/elder/visit-register')">来访登记</a-button>
        <a-button @click="go('/elder/trial-stay')">试住登记</a-button>
        <a-button danger @click="go('/elder/discharge-apply')">退住申请</a-button>
        <a-button @click="go('/elder/status-change?event=medical_out')">外出就医登记</a-button>
        <a-button danger @click="go('/elder/status-change?event=deceased')">死亡登记</a-button>
      </a-space>

      <a-divider />
      <a-typography-paragraph>
        触发规则：提交状态变更后自动推送护工/医护/财务/后勤消息，生成任务包（退住交接、事故处置等），并按规则调整费用、床位与餐饮计划。
      </a-typography-paragraph>
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import PageContainer from '../../../components/PageContainer.vue'

const router = useRouter()

const statusItems = [
  { status: '意向', count: 24, path: '/elder/list?status=intent' },
  { status: '试住', count: 5, path: '/elder/list?status=trial' },
  { status: '在住', count: 128, path: '/elder/list?status=in' },
  { status: '外出', count: 9, path: '/elder/list?status=out' },
  { status: '外出就医', count: 3, path: '/elder/list?status=medical_out' },
  { status: '退住办理中', count: 4, path: '/elder/list?status=discharge_pending' },
  { status: '已退住', count: 96, path: '/elder/list?status=discharged' },
  { status: '死亡', count: 2, path: '/elder/list?status=deceased' }
]

function go(path: string) {
  router.push(path)
}
</script>
