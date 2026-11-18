<template>
  <div class="rm-split fo-grid">
    <section class="rm-card">
      <div class="rm-card__head">
        <span class="rm-card__title">录像调阅申请 <small class="rm-card__sub">{{ manager ? '全部申请（可审批）' : '我的申请' }}</small></span>
        <a-radio-group v-model:value="status" size="small" @change="load">
          <a-radio-button value="">全部</a-radio-button><a-radio-button value="PENDING">待审批</a-radio-button>
          <a-radio-button value="APPROVED">已批准</a-radio-button><a-radio-button value="REJECTED">已驳回</a-radio-button>
        </a-radio-group>
      </div>
      <a-table size="small" row-key="id" :data-source="rows" :columns="columns" :loading="loading" :pagination="false">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'range'">{{ fmt(record.rangeStart) }} — {{ fmt(record.rangeEnd) }}</template>
          <template v-else-if="column.key === 'status'">
            <span class="rm-pill" :class="TONE[record.status]">{{ LABEL[record.status] }}</span>
            <small v-if="record.status === 'APPROVED' && record.expiresAt" class="fo-muted"> 至 {{ fmt(record.expiresAt) }}</small>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space wrap>
              <template v-if="manager && record.status === 'PENDING' && String(record.applicantId) !== myId">
                <a-button type="link" size="small" @click="decide(record, true)">批准</a-button>
                <a-button type="link" size="small" danger @click="openReject(record)">驳回</a-button>
              </template>
              <a-button v-if="record.status === 'APPROVED' && String(record.applicantId) === myId" type="link" size="small" @click="openRecordings(record)">查看录像</a-button>
              <a-button v-if="manager" type="link" size="small" @click="openLogs(record)">审计日志</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </section>

    <section class="rm-card">
      <div class="rm-card__head"><span class="rm-card__title">申请调阅</span></div>
      <a-form layout="vertical">
        <a-form-item label="摄像头" required><a-select v-model:value="form.cameraId" :options="cameraOptions" show-search option-filter-prop="label" /></a-form-item>
        <a-form-item label="时间段（不超过 24 小时）" required><a-range-picker v-model:value="form.range" show-time style="width: 100%" /></a-form-item>
        <a-form-item label="调阅事由" required><a-textarea v-model:value="form.reason" :rows="3" placeholder="如：核查长者夜间跌倒经过" /></a-form-item>
        <a-button type="primary" block :loading="saving" @click="submit">提交申请</a-button>
      </a-form>
      <a-alert class="fo-alert" type="warning" show-icon message="录像查看须说明事由并经主管批准，批准后 7 天内由申请人本人查看；每次查看都会留痕。" />
    </section>

    <a-modal v-model:open="rejectOpen" title="驳回调阅申请" @ok="decide(rejectTarget!, false)">
      <a-textarea v-model:value="rejectNote" :rows="3" placeholder="写明驳回原因" />
    </a-modal>
    <a-drawer v-model:open="recordingsOpen" title="批准范围内的录像" width="min(720px, 96vw)">
      <a-empty v-if="!recordings.length" description="该时间段没有录像片段" />
      <div v-for="item in recordings" :key="String(item.id)" class="fo-recording">
        <span>{{ fmt(item.startedAt) }} — {{ fmt(item.endedAt) }}</span>
        <a-button size="small" @click="play(item.id)">播放</a-button>
      </div>
      <video v-if="playUrl" :src="playUrl" controls class="fo-player" />
    </a-drawer>
    <a-drawer v-model:open="logsOpen" title="调阅审计日志" width="420px">
      <a-timeline><a-timeline-item v-for="log in logs" :key="String(log.id)">{{ fmt(log.createTime) }} · {{ log.staffName }} · {{ ACTION[log.action] || log.action }}</a-timeline-item></a-timeline>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import dayjs, { type Dayjs } from 'dayjs'
import { message } from 'ant-design-vue'
import { useUserStore } from '../../../stores/user'
import { getVideoCameras, getVideoContent, type VideoCamera } from '../../../api/logisticsVideo'
import {
  applyVideoAccess, decideVideoAccess, getAccessPlayback, getAccessRecordings, getVideoAccessLogs, getVideoAccessRequests,
  type FireVideoAccessLog, type FireVideoAccessRequest
} from '../../../api/fireOps'
import { canCloseHazard } from './fireModel'
import '../../elder/residentUi.css'

const LABEL: Record<string, string> = { PENDING: '待审批', APPROVED: '已批准', REJECTED: '已驳回' }
const TONE: Record<string, string> = { PENDING: 'rm-pill--warning', APPROVED: 'rm-pill--info', REJECTED: 'rm-pill--danger' }
const ACTION: Record<string, string> = { REQUEST: '提交申请', APPROVE: '批准', REJECT: '驳回', LIST: '查看录像列表', VIEW: '播放录像' }
const columns = [
  { title: '摄像头', dataIndex: 'cameraName', width: 120 },
  { title: '时间段', key: 'range' },
  { title: '事由', dataIndex: 'reason' },
  { title: '申请人', dataIndex: 'applicantName', width: 90 },
  { title: '状态', key: 'status', width: 150 },
  { title: '', key: 'action', width: 170 }
]

const user = useUserStore()
const manager = computed(() => canCloseHazard(user.roles || []))
const myId = computed(() => String(user.staffInfo?.id || ''))
const rows = ref<FireVideoAccessRequest[]>([])
const cameras = ref<VideoCamera[]>([])
const cameraOptions = computed(() => cameras.value.map((c) => ({ value: String(c.id), label: c.cameraName })))
const loading = ref(false)
const saving = ref(false)
const status = ref('')
const form = reactive({ cameraId: undefined as string | undefined, range: null as [Dayjs, Dayjs] | null, reason: '' })
const rejectOpen = ref(false)
const rejectNote = ref('')
const rejectTarget = ref<FireVideoAccessRequest | null>(null)
const recordingsOpen = ref(false)
const recordings = ref<{ id: string | number; startedAt: string; endedAt: string }[]>([])
const activeRequest = ref<FireVideoAccessRequest | null>(null)
const playUrl = ref('')
const logsOpen = ref(false)
const logs = ref<FireVideoAccessLog[]>([])

const fmt = (value?: string) => (value ? dayjs(value).format('MM-DD HH:mm') : '')

async function load() {
  loading.value = true
  try {
    rows.value = (await getVideoAccessRequests({ pageNo: 1, pageSize: 50, status: status.value || undefined })).list || []
  } finally {
    loading.value = false
  }
}

async function submit() {
  if (!form.cameraId || !form.range || form.reason.trim().length < 4) return message.warning('请选择摄像头、时间段并写明事由')
  saving.value = true
  try {
    const iso = 'YYYY-MM-DDTHH:mm:ss'
    await applyVideoAccess({ cameraId: form.cameraId, rangeStart: form.range[0].format(iso), rangeEnd: form.range[1].format(iso), reason: form.reason })
    message.success('已提交，等待主管审批')
    Object.assign(form, { cameraId: undefined, range: null, reason: '' })
    await load()
  } finally {
    saving.value = false
  }
}

function openReject(row: FireVideoAccessRequest) {
  rejectTarget.value = row
  rejectNote.value = ''
  rejectOpen.value = true
}

async function decide(row: FireVideoAccessRequest, approve: boolean) {
  if (!approve && !rejectNote.value.trim()) return message.warning('请写明驳回原因')
  await decideVideoAccess(row.id, approve, approve ? '同意调阅' : rejectNote.value)
  rejectOpen.value = false
  await load()
}

async function openRecordings(row: FireVideoAccessRequest) {
  activeRequest.value = row
  playUrl.value = ''
  recordings.value = await getAccessRecordings(row.id)
  recordingsOpen.value = true
}

async function play(recordingId: string | number) {
  const playback = await getAccessPlayback(activeRequest.value!.id, recordingId)
  if (playUrl.value.startsWith('blob:')) URL.revokeObjectURL(playUrl.value)
  // 本地存储走带鉴权的接口取流；云存储直接用私有签名链接
  playUrl.value = playback.provider === 'LOCAL' ? URL.createObjectURL(await getVideoContent(playback.url)) : playback.url
}

async function openLogs(row: FireVideoAccessRequest) {
  logs.value = await getVideoAccessLogs(row.id)
  logsOpen.value = true
}

onMounted(async () => {
  load()
  cameras.value = (await getVideoCameras({ pageNo: 1, pageSize: 200 })).list || []
})
onBeforeUnmount(() => { if (playUrl.value.startsWith('blob:')) URL.revokeObjectURL(playUrl.value) })
</script>

<style scoped>
.fo-grid { grid-template-columns: minmax(0, 1.6fr) minmax(300px, 1fr); }
.fo-alert { margin-top: 12px; }
.fo-muted { color: #8a968f; font-size: 12px; }
.fo-recording { display: flex; align-items: center; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid #eef2f0; }
.fo-player { width: 100%; margin-top: 12px; border-radius: 8px; background: #000; }
@media (max-width: 1100px) { .fo-grid { grid-template-columns: 1fr; } }
</style>
