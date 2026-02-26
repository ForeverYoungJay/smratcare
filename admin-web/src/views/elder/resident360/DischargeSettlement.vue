<template>
  <PageContainer title="退院结算" subTitle="自动对账、费用清单、押金处理与床位释放">
    <a-row :gutter="16">
      <a-col :xs="24" :lg="14">
        <a-card title="退院结算清单" class="card-elevated" :bordered="false">
          <a-descriptions :column="1" size="small" bordered>
            <a-descriptions-item label="账单汇总">本期应收 3200，已收 2800，待补缴 400</a-descriptions-item>
            <a-descriptions-item label="押金处理">押金 6000，扣减 400，待退 5600</a-descriptions-item>
            <a-descriptions-item label="自动对账">对账完成（无异常）</a-descriptions-item>
            <a-descriptions-item label="床位释放">结算完成后自动释放并回写床态</a-descriptions-item>
          </a-descriptions>
          <a-space style="margin-top: 12px">
            <a-button type="primary" @click="go('/finance/bill?period=this_month')">查看账单</a-button>
            <a-button @click="go('/finance/account-log')">费用明细</a-button>
            <a-button danger @click="go('/elder/discharge')">确认退院登记</a-button>
          </a-space>
        </a-card>
      </a-col>

      <a-col :xs="24" :lg="10">
        <a-card title="流程联动" class="card-elevated" :bordered="false">
          <a-timeline>
            <a-timeline-item color="green">退住申请审批完成</a-timeline-item>
            <a-timeline-item color="green">自动生成退院结算单</a-timeline-item>
            <a-timeline-item color="blue">押金退还/欠费补缴</a-timeline-item>
            <a-timeline-item color="blue">回写长者状态为已退住</a-timeline-item>
            <a-timeline-item color="gray">释放床位并停止餐饮/护理任务</a-timeline-item>
          </a-timeline>
        </a-card>
      </a-col>
    </a-row>
  </PageContainer>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import PageContainer from '../../../components/PageContainer.vue'

const router = useRouter()

function go(path: string) {
  router.push(path)
}
</script>
