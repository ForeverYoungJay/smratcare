<template>
  <div class="rm-split fo-grid">
    <section class="rm-card">
      <div class="rm-card__head">
        <span class="rm-card__title">维保报告 <small class="rm-card__sub">先提交签署，再由另一位主管审核签署</small></span>
        <a class="rm-link" @click="router.push('/fire/facilities?tab=maintenance')">登记/上传报告 →</a>
      </div>
      <a-table size="small" row-key="id" :data-source="rows" :columns="columns" :loading="loading" :pagination="false"
        :custom-row="(r: FireSafetyRecord) => ({ onClick: () => select(r), class: r.id === selected?.id ? 'fo-row--active' : '' })">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'file'">
            <a v-if="record.thirdPartyMaintenanceFileUrl" :href="record.thirdPartyMaintenanceFileUrl" target="_blank" rel="noopener">查看文件</a>
            <span v-else class="fo-muted">未上传</span>
          </template>
          <template v-else-if="column.key === 'time'">{{ fmt(record.checkTime) }}</template>
        </template>
      </a-table>
    </section>

    <section class="rm-card">
      <div class="rm-card__head"><span class="rm-card__title">签署记录{{ selected ? ` · ${selected.title}` : '' }}</span></div>
      <div v-if="!selected" class="fo-empty">点击左侧报告查看签署情况</div>
      <template v-else>
        <a-timeline>
          <a-timeline-item v-for="sign in signatures" :key="String(sign.id)" :color="sign.signType === 'REVIEW' ? 'green' : 'blue'">
            <b>{{ sign.signType === 'REVIEW' ? '审核签署' : '提交签署' }}</b> · {{ sign.signerName }} · {{ fmt(sign.signedAt) }}
            <div class="fo-muted">文件 SHA-256：{{ sign.fileSha256 ? `${sign.fileSha256.slice(0, 16)}…` : '当前存储方式不支持计算' }}</div>
            <div v-if="sign.comment" class="fo-muted">意见：{{ sign.comment }}</div>
          </a-timeline-item>
        </a-timeline>
        <a-empty v-if="!signatures.length" description="尚未签署" />
        <a-textarea v-model:value="comment" :rows="2" placeholder="签署意见（可选）" class="fo-comment" />
        <a-space>
          <a-button v-if="!submitted" type="primary" :disabled="!selected.thirdPartyMaintenanceFileUrl" @click="sign('SUBMIT')">提交签署</a-button>
          <a-button v-if="submitted && !reviewed && manager" type="primary" @click="sign('REVIEW')">审核签署</a-button>
          <a-tag v-if="reviewed" color="green">已完成审核签署</a-tag>
        </a-space>
        <p class="fo-muted">签署时由服务端计算报告文件哈希留证；审核时文件与提交时不一致会被拒绝。签署记录只增不改。</p>
      </template>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../../stores/user'
import { getFireSafetyRecordPage } from '../../../api/fire'
import { getReportSignatures, signFireReport, type FireReportSignature } from '../../../api/fireOps'
import type { FireSafetyRecord } from '../../../types'
import { canCloseHazard } from './fireModel'
import '../../elder/residentUi.css'

const columns = [
  { title: '报告', dataIndex: 'title' },
  { title: '登记人', dataIndex: 'inspectorName', width: 100 },
  { title: '登记时间', key: 'time', width: 120 },
  { title: '文件', key: 'file', width: 90 }
]
const router = useRouter()
const user = useUserStore()
const manager = computed(() => canCloseHazard(user.roles || []))
const rows = ref<FireSafetyRecord[]>([])
const loading = ref(false)
const selected = ref<FireSafetyRecord | null>(null)
const signatures = ref<FireReportSignature[]>([])
const comment = ref('')
const submitted = computed(() => signatures.value.some((s) => s.signType === 'SUBMIT'))
const reviewed = computed(() => signatures.value.some((s) => s.signType === 'REVIEW'))
const fmt = (value?: string) => (value ? dayjs(value).format('YYYY-MM-DD HH:mm') : '')

async function select(record: FireSafetyRecord) {
  selected.value = record
  comment.value = ''
  signatures.value = await getReportSignatures(record.id)
}

async function sign(type: 'SUBMIT' | 'REVIEW') {
  await signFireReport(selected.value!.id, type, comment.value || undefined)
  message.success(type === 'SUBMIT' ? '已提交签署' : '已审核签署')
  await select(selected.value!)
}

onMounted(async () => {
  loading.value = true
  try {
    rows.value = (await getFireSafetyRecordPage({ pageNo: 1, pageSize: 50, recordType: 'MAINTENANCE_REPORT' })).list || []
    if (rows.value[0]) await select(rows.value[0])
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.fo-grid { grid-template-columns: minmax(0, 1.4fr) minmax(320px, 1fr); }
.fo-empty { padding: 24px 8px; color: #7b877e; text-align: center; }
.fo-muted { margin: 6px 0 0; color: #8a968f; font-size: 12px; }
.fo-comment { margin: 8px 0; }
:deep(.fo-row--active) td { background: #eef8f3 !important; }
@media (max-width: 1100px) { .fo-grid { grid-template-columns: 1fr; } }
</style>
