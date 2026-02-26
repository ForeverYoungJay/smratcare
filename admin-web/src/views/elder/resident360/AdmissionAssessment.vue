<template>
  <PageContainer title="入住评估" subTitle="按 GB/T 42195-2022 执行入院能力与风险评估，评估完成后进入签约与入住办理流程">
    <a-row :gutter="16">
      <a-col :xs="24" :lg="16">
        <a-card title="评估表单（国标分组）" class="card-elevated" :bordered="false">
          <a-form layout="vertical">
            <a-form-item label="自理能力">
              <a-input placeholder="如：进食/穿衣/如厕/移动等评分" />
            </a-form-item>
            <a-form-item label="认知能力">
              <a-input placeholder="如：记忆/定向/理解/沟通" />
            </a-form-item>
            <a-form-item label="风险评估">
              <a-input placeholder="如：跌倒/压疮/走失/吞咽/感染" />
            </a-form-item>
            <a-form-item label="护理需求">
              <a-input placeholder="如：护理频次、夜间巡视、个性化照护要求" />
            </a-form-item>
          </a-form>
          <a-alert
            type="warning"
            show-icon
            message="待你提供 GB/T 42195-2022 的量表附件后，可一比一固化字段和分值规则。"
            style="margin-top: 12px"
          />
        </a-card>
      </a-col>

      <a-col :xs="24" :lg="8">
        <a-card title="自动生成与流程联动" class="card-elevated" :bordered="false">
          <a-typography-paragraph>
            输出：评估等级、建议护理套餐/服务频次、人力配置建议、评估报告 PDF（归档到长者档案）。
          </a-typography-paragraph>
          <a-typography-paragraph>
            联动：评估完成后自动创建“待签约/待入院办理”任务，并同步护理端生成个性化任务规则。
          </a-typography-paragraph>
          <a-space direction="vertical" style="width: 100%">
            <a-button block type="primary" @click="go('/assessment/ability/admission')">进入评估中心</a-button>
            <a-button block @click="go('/assessment/ability/archive')">查看历史评估报告</a-button>
            <a-button block @click="go('/elder/admission-processing')">进入入住办理</a-button>
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

function go(path: string) {
  router.push(path)
}
</script>
