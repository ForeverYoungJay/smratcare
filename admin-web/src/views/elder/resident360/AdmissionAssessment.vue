<template>
  <PageContainer title="入住评估" subTitle="按 GB/T 42195-2022 执行入院能力与风险评估，评估完成后进入签约与入住办理流程">
    <a-card title="评估入口" class="card-elevated" :bordered="false">
      <a-alert
        type="info"
        show-icon
        message="评估结果实时联动签约、入住办理与护理规则，不再使用演示数据。"
        style="margin-bottom: 12px"
      />
      <StatefulBlock :loading="loading" :error="errorMessage" @retry="loadLatestRecord">
        <a-descriptions :column="1" size="small" bordered style="margin-bottom: 12px">
          <a-descriptions-item label="最近评估日期">{{ latestRecord?.assessmentDate || '-' }}</a-descriptions-item>
          <a-descriptions-item label="最近评估状态">{{ latestRecord?.status || '无记录' }}</a-descriptions-item>
          <a-descriptions-item label="最近评估分值">{{ latestRecord?.score ?? '-' }}</a-descriptions-item>
          <a-descriptions-item label="最近评估等级">{{ latestRecord?.levelCode || '-' }}</a-descriptions-item>
          <a-descriptions-item label="复评状态">
            {{ isReassessOverdue ? '已到复评日期' : latestRecord?.nextAssessmentDate ? `下次复评 ${latestRecord?.nextAssessmentDate}` : '未设置' }}
          </a-descriptions-item>
        </a-descriptions>
        <a-typography-paragraph>
          输出：评估等级、建议护理套餐/服务频次、人力配置建议、评估报告下载并归档到长者档案。
        </a-typography-paragraph>
        <a-typography-paragraph>
          联动：评估完成后自动创建“待签约/待入院办理”任务，并同步护理端生成个性化任务规则。
        </a-typography-paragraph>
        <a-space direction="vertical" style="width: 100%">
          <a-button block type="primary" @click="go(primaryActionPath)">{{ primaryActionText }}</a-button>
          <a-button block @click="go(historyPath)">查看历史评估报告</a-button>
          <a-button block :disabled="!latestRecord" @click="downloadLatestReport">下载最近评估报告</a-button>
          <a-button block @click="go(admissionPath)">进入入住办理</a-button>
        </a-space>
      </StatefulBlock>
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import PageContainer from '../../../components/PageContainer.vue'
import StatefulBlock from '../../../components/StatefulBlock.vue'
import { getAssessmentRecordPage } from '../../../api/assessment'
import type { AssessmentRecord, PageResult } from '../../../types'

const router = useRouter()
const route = useRoute()
const residentId = computed(() => Number(route.query.residentId || 0))
const latestRecord = ref<AssessmentRecord | null>(null)
const loading = ref(false)
const errorMessage = ref('')

const admissionPath = computed(() =>
  residentId.value ? `/elder/admission-processing?residentId=${residentId.value}` : '/elder/admission-processing'
)

const historyPath = computed(() =>
  residentId.value ? `/assessment/ability/archive?residentId=${residentId.value}` : '/assessment/ability/archive'
)

const isReassessOverdue = computed(() => {
  const nextDate = latestRecord.value?.nextAssessmentDate
  if (!nextDate) return false
  const today = new Date().toISOString().slice(0, 10)
  return nextDate < today
})

const primaryActionText = computed(() => {
  if (!latestRecord.value) return '开始入住评估'
  if (latestRecord.value.status === 'DRAFT') return '继续填写入住评估'
  if (isReassessOverdue.value) return '发起复评'
  return '新建入住评估'
})

const primaryActionPath = computed(() => {
  const mode = latestRecord.value?.status === 'DRAFT'
    ? 'continue'
    : isReassessOverdue.value
      ? 'reassess'
      : 'new'
  if (!residentId.value) return '/assessment/ability/admission'
  return `/assessment/ability/admission?residentId=${residentId.value}&autoOpen=1&mode=${mode}`
})

function go(path: string) {
  router.push(path)
}

async function loadLatestRecord() {
  loading.value = true
  errorMessage.value = ''
  if (!residentId.value) {
    latestRecord.value = null
    loading.value = false
    return
  }
  try {
    const res: PageResult<AssessmentRecord> = await getAssessmentRecordPage({
      pageNo: 1,
      pageSize: 10,
      assessmentType: 'ADMISSION',
      elderId: residentId.value
    })
    const list = res.list || []
    const draft = list.find((item) => item.status === 'DRAFT')
    latestRecord.value = draft || list[0] || null
  } catch (error: any) {
    errorMessage.value = error?.message || '加载入住评估失败'
    message.error(errorMessage.value)
  } finally {
    loading.value = false
  }
}

function downloadLatestReport() {
  if (!latestRecord.value) {
    message.warning('暂无可下载评估记录')
    return
  }
  const record = latestRecord.value
  const lines = [
    '入住评估报告',
    `老人姓名：${record.elderName || '-'}`,
    `评估日期：${record.assessmentDate || '-'}`,
    `评估状态：${record.status || '-'}`,
    `评估分值：${record.score ?? '-'}`,
    `评估等级：${record.levelCode || '-'}`,
    `评估结论：${record.resultSummary || '-'}`,
    `护理建议：${record.suggestion || '-'}`,
    `下次评估日期：${record.nextAssessmentDate || '-'}`
  ]
  const blob = new Blob([lines.join('\n')], { type: 'text/plain;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `入住评估报告_${record.elderName || '长者'}_${record.assessmentDate || new Date().toISOString().slice(0, 10)}.txt`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
  message.success('最近评估报告已下载')
}

onMounted(() => {
  loadLatestRecord()
})

watch(
  () => residentId.value,
  () => {
    loadLatestRecord()
  }
)
</script>
