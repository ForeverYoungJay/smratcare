<template>
  <a-modal v-model:open="reviewOpen" :title="`审核 · ${doc?.name || ''} v${doc?.versionNo || 1}`" :confirm-loading="busy" ok-text="提交审核" @ok="submitReview">
    <a-radio-group v-model:value="pass">
      <a-radio :value="true">通过（成为当前版本）</a-radio>
      <a-radio :value="false">退回</a-radio>
    </a-radio-group>
    <a-textarea v-model:value="note" :rows="2" style="margin-top: 12px" :placeholder="pass ? '审核意见（可选）' : '退回原因（必填）'" />
  </a-modal>

  <a-modal v-model:open="versionOpen" :title="`上传新版本 · ${doc?.name || ''}`" :confirm-loading="busy" :ok-button-props="{ disabled: !upload.url }" @ok="submitVersion">
    <p class="dr-muted">新版本不会覆盖当前版本；审核通过后才替换，原版本保留为历史版本。</p>
    <a-upload :show-upload-list="false" :before-upload="beforeUpload" accept=".doc,.docx,.pdf">
      <a-button :loading="uploading">选择 Word / PDF</a-button>
    </a-upload>
    <div class="dr-muted" style="margin-top: 8px">{{ upload.fileName || '未选择文件' }}</div>
    <a-input v-model:value="upload.remark" style="margin-top: 12px" placeholder="修订说明" />
  </a-modal>

  <a-drawer v-model:open="historyOpen" :title="`版本记录 · ${doc?.name || ''}`" width="520">
    <a-timeline>
      <a-timeline-item v-for="item in history" :key="String(item.id)" :color="item.isCurrent === 1 ? 'green' : 'gray'">
        <b>v{{ item.versionNo || 1 }}</b> {{ REVIEW_LABEL[item.reviewStatus || 'APPROVED'] }}{{ item.isCurrent === 1 ? ' · 当前版本' : '' }}
        <div class="dr-muted">上传：{{ item.uploaderName || '-' }} {{ fmt(item.uploadedAt) }}</div>
        <div v-if="item.reviewerName" class="dr-muted">审核：{{ item.reviewerName }} {{ fmt(item.reviewedAt) }}{{ item.reviewNote ? ` · ${item.reviewNote}` : '' }}</div>
        <a v-if="item.url" :href="item.url" target="_blank" rel="noopener">查看文件</a>
      </a-timeline-item>
    </a-timeline>
  </a-drawer>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import { createDocumentVersion, getDocumentVersions, reviewDocument, uploadOaFile } from '../../api/oa'
import type { OaDocument } from '../../types'

const REVIEW_LABEL: Record<string, string> = { PENDING: '待审核', APPROVED: '已审核', REJECTED: '已退回' }
const fmt = (value?: string) => (value ? dayjs(value).format('YYYY-MM-DD HH:mm') : '')
const emit = defineEmits<{ changed: [] }>()
const doc = ref<OaDocument>()
const busy = ref(false)
const reviewOpen = ref(false)
const pass = ref(true)
const note = ref('')
const versionOpen = ref(false)
const uploading = ref(false)
const upload = reactive({ url: '', fileName: '', sizeBytes: 0, remark: '' })
const historyOpen = ref(false)
const history = ref<OaDocument[]>([])

function openReview(record: OaDocument) {
  doc.value = record
  pass.value = true
  note.value = ''
  reviewOpen.value = true
}
function openNewVersion(record: OaDocument) {
  doc.value = record
  Object.assign(upload, { url: '', fileName: '', sizeBytes: 0, remark: '' })
  versionOpen.value = true
}
async function openHistory(record: OaDocument) {
  doc.value = record
  historyOpen.value = true
  history.value = await getDocumentVersions(record.id)
}

async function run(fn: () => Promise<unknown>, done: string) {
  busy.value = true
  try {
    await fn()
    message.success(done)
    emit('changed')
  } finally {
    busy.value = false
  }
}
async function submitReview() {
  if (!pass.value && !note.value.trim()) return message.warning('请填写退回原因')
  await run(() => reviewDocument(doc.value!.id, { pass: pass.value, note: note.value || undefined }), pass.value ? '已审核通过' : '已退回')
  reviewOpen.value = false
}
async function submitVersion() {
  await run(() => createDocumentVersion(doc.value!.id, { url: upload.url, sizeBytes: upload.sizeBytes, remark: upload.remark || undefined }), '新版本已提交审核')
  versionOpen.value = false
}
async function beforeUpload(file: File) {
  uploading.value = true
  try {
    const res = await uploadOaFile(file, 'oa-document')
    Object.assign(upload, { url: res.fileUrl || '', fileName: res.originalFileName || file.name, sizeBytes: Number(res.fileSize || file.size || 0) })
  } finally {
    uploading.value = false
  }
  return false
}

defineExpose({ openReview, openNewVersion, openHistory })
</script>

<style scoped>
.dr-muted { color: #7b877e; font-size: 12px; }
</style>
