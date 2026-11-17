<template>
  <PageContainer title="摄像头录像管理" subTitle="管理公共区域摄像头、检索录像与配置至少90天留存策略">
    <template #extra>
      <a-space wrap>
        <a-button :loading="loading" @click="refresh">刷新状态</a-button>
        <a-button v-if="canManage" type="primary" @click="editCamera()">新增摄像头</a-button>
      </a-space>
    </template>

    <a-alert v-if="loadError" type="error" show-icon message="录像管理数据加载失败，请点击刷新重试" />
    <a-alert type="info" show-icon message="配置保存后仍需接入采集网关或NVR；最近收到分片不代表90天录像完整。" />
    <div v-if="overview" class="secondary">状态核对时间：{{ formatTime(overview.checkedAt) }}（机构时区）</div>
    <a-row :gutter="[16, 16]" class="metrics business-stat-strip">
      <a-col :xs="12" :lg="6"><a-card><a-statistic title="摄像头总数" :value="overview?.cameraCount ?? '—'" suffix="台" /></a-card></a-col>
      <a-col :xs="12" :lg="6"><a-card><a-statistic title="待接入 / 断录风险" :value="overview?.missingCount ?? '—'" suffix="台" /></a-card></a-col>
      <a-col :xs="12" :lg="6"><a-card><a-statistic title="录像留存策略" :value="overview?.policy.retentionDays ?? '—'" suffix="天" /></a-card></a-col>
      <a-col :xs="12" :lg="6"><a-card><a-statistic title="预计所需容量（含20%余量）" :value="overview?.estimatedTb ?? '—'" suffix="TB" /></a-card></a-col>
    </a-row>

    <a-tabs v-model:activeKey="tab">
      <a-tab-pane key="cameras" tab="摄像头档案">
        <SearchForm :model="cameraQuery" @search="searchCameras" @reset="resetCameras">
          <a-form-item label="关键词"><a-input v-model:value="cameraQuery.keyword" placeholder="编码、名称或位置" allow-clear /></a-form-item>
        </SearchForm>
        <DataTable row-key="id" :columns="cameraColumns" :data-source="cameras" :loading="cameraLoading" :pagination="cameraPagination"
          empty-title="尚未登记摄像头" empty-description="登记位置和网关通道后，由采集网关推送录像分片。" @change="changeCameraPage">
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'state'">
              <a-tag :color="healthColor(record)">{{ healthLabel(record) }}</a-tag>
              <div class="secondary">{{ formatTime(health(record)?.lastRecordedAt) }}</div>
            </template>
            <template v-else-if="column.key === 'actions' && canManage">
              <a-button type="link" @click="editCamera(record)">编辑</a-button>
              <a-button type="link" @click="viewRecordings(record)">查看录像</a-button>
            </template>
          </template>
        </DataTable>
      </a-tab-pane>

      <a-tab-pane v-if="canManage" key="recordings" tab="录像查询与保全">
        <a-alert type="warning" show-icon message="录像含敏感个人信息，仅限授权管理人员按业务目的查阅；播放、保全与解除保全均记录审计。" />
        <SearchForm :model="recordQuery" @search="searchRecordings" @reset="resetRecordings">
          <a-form-item label="摄像头ID"><a-input v-model:value="recordQuery.cameraId" placeholder="可从摄像头档案选择查看" allow-clear /></a-form-item>
          <a-form-item label="录像时间"><a-range-picker v-model:value="recordRange" show-time format="YYYY-MM-DD HH:mm" /></a-form-item>
          <a-form-item label="保全状态"><a-select v-model:value="recordQuery.held" allow-clear :options="holdOptions" style="width: 130px" /></a-form-item>
          <a-form-item><a-button @click="openImport">导入录像分片</a-button></a-form-item>
        </SearchForm>
        <DataTable row-key="id" :columns="recordColumns" :data-source="recordings" :loading="recordLoading" :pagination="recordPagination"
          empty-title="没有符合条件的录像" empty-description="确认采集网关已经推送分片，或调整摄像头与查询时间。" @change="changeRecordPage">
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'time'">{{ formatTime(record.startedAt) }}<div class="secondary">至 {{ formatTime(record.endedAt) }}</div></template>
            <template v-else-if="column.key === 'provider'">{{ providerLabel(record.provider) }}</template>
            <template v-else-if="column.key === 'size'">{{ (record.sizeBytes / 1024 / 1024).toFixed(2) }} MB</template>
            <template v-else-if="column.key === 'expiry'">{{ record.held ? '事件保全，暂停清理' : formatTime(record.expiresAt) }}</template>
            <template v-else-if="column.key === 'status'">
              <a-tag :color="record.status === 'DELETED' ? 'default' : record.held ? 'orange' : 'green'">{{ record.status === 'DELETED' ? '已清理' : record.held ? '已保全' : '可用' }}</a-tag>
              <div v-if="record.holdReason" class="secondary">{{ record.holdReason }}</div>
            </template>
            <template v-else-if="column.key === 'actions' && record.status === 'AVAILABLE'">
              <a-button type="link" :loading="playingId === record.id" @click="play(record)">播放</a-button>
              <a-button type="link" @click="openHold(record)">{{ record.held ? '解除保全' : '事件保全' }}</a-button>
            </template>
          </template>
        </DataTable>
      </a-tab-pane>

      <a-tab-pane key="policy" tab="存储与留存策略">
        <a-row :gutter="[20, 20]">
          <a-col :xs="24" :lg="14">
            <a-card title="录像存储策略">
              <a-form ref="policyFormRef" :model="policyForm" layout="vertical" :disabled="!canManage || !overview">
                <a-form-item label="存储方式" name="provider" :rules="[{ required: true, message: '请选择存储方式' }]">
                  <a-radio-group v-model:value="policyForm.provider" class="provider-options">
                    <a-radio value="UNDECIDED">暂未确定</a-radio><a-radio value="LOCAL">本地专用存储</a-radio><a-radio value="OSS">阿里云 OSS</a-radio>
                  </a-radio-group>
                </a-form-item>
                <a-form-item label="录像保留天数" name="retentionDays" :rules="[{ required: true, type: 'number', min: 90, max: 3650, message: '保留天数须为90至3650之间的整数' }]">
                  <a-input-number v-model:value="policyForm.retentionDays" :min="90" :max="3650" :precision="0" addon-after="天" />
                  <div class="secondary">从每个录像分片结束时间起算；延长策略同时延长既有录像，缩短策略不提前删除原已承诺保留的录像。</div>
                </a-form-item>
                <a-form-item label="留存依据 / 机构制度" name="basis">
                  <a-textarea v-model:value="policyForm.basis" :maxlength="255" show-count placeholder="填写适用标准、地方要求或机构制度；90天为本机构设计底线" />
                </a-form-item>
                <a-form-item label="自动清理到期录像">
                  <a-switch v-model:checked="policyForm.autoCleanup" :disabled="policyForm.provider === 'UNDECIDED'" />
                  <div class="secondary">仅清理已到期且未保全的录像。磁盘紧张不会提前删除90天内录像。</div>
                </a-form-item>
                <a-alert :type="overview?.storageConfigured ? 'info' : 'warning'" show-icon
                  :message="overview?.storageConfigured ? '当前已保存策略的存储参数已配置，实际读写仍需通过分片导入验收。' : '当前已保存策略尚未配置可用存储；可先保存选型，暂不能接收录像。'" />
                <a-button v-if="canManage" type="primary" :loading="savingPolicy" class="save-policy" @click="submitPolicy">保存存储策略</a-button>
              </a-form>
            </a-card>
          </a-col>
          <a-col :xs="24" :lg="10">
            <a-card title="容量与选型参考">
              <p>按 {{ overview?.enabledCount ?? '—' }} 台启用摄像头、总码率 {{ overview?.uploadMbps ?? '—' }} Mbps、当前已保存留存策略估算。容量不含 RAID、备份和事件保全额外空间。</p>
              <a-descriptions :column="1" bordered size="small">
                <a-descriptions-item label="本地存储">适合已有NVR或专用存储、上行带宽有限的机构；需负责磁盘、供电和备份。</a-descriptions-item>
                <a-descriptions-item label="阿里云OSS">需网关持续上传、断网缓存和补传；费用包含容量、请求、回放流量等。</a-descriptions-item>
                <a-descriptions-item label="切换策略">切换后仅新录像使用新存储；历史录像保持原存储并继续可查。</a-descriptions-item>
                <a-descriptions-item label="敏感配置">存储路径、Bucket与凭据由部署人员在服务端配置，不在浏览器录入或回显。</a-descriptions-item>
              </a-descriptions>
              <p class="secondary">“最近收到分片”只说明分片到达状态，实际连续性和90天完整留存需由现场接入验收确认。</p>
            </a-card>
          </a-col>
        </a-row>
      </a-tab-pane>
    </a-tabs>

    <a-modal v-model:open="cameraModal" :title="cameraForm.id ? '编辑摄像头' : '新增摄像头'" :confirm-loading="savingCamera" @ok="submitCamera">
      <a-form ref="cameraFormRef" :model="cameraForm" layout="vertical">
        <a-form-item label="摄像头编码" name="cameraCode" :rules="[{ required: true, pattern: /^[A-Za-z0-9_-]{1,64}$/, message: '请输入64位以内的字母、数字、下划线或短横线编码' }]"><a-input v-model:value="cameraForm.cameraCode" :maxlength="64" /></a-form-item>
        <a-form-item label="摄像头名称" name="cameraName" :rules="[{ required: true, whitespace: true, message: '请输入摄像头名称' }]"><a-input v-model:value="cameraForm.cameraName" :maxlength="128" /></a-form-item>
        <a-form-item label="安装位置（公共区域）" name="location" :rules="[{ required: true, whitespace: true, message: '请输入安装位置' }]"><a-input v-model:value="cameraForm.location" :maxlength="128" placeholder="例如：一号楼一层入口" /></a-form-item>
        <a-form-item label="接入协议" name="protocol" :rules="[{ required: true }]"><a-select v-model:value="cameraForm.protocol" :options="protocolOptions" /></a-form-item>
        <a-form-item label="采集网关通道标识" name="channelRef" :rules="[{ required: true, pattern: /^[A-Za-z0-9_./-]{1,128}$/, message: '请输入通道标识，仅支持字母、数字、下划线、点、斜杠和短横线' }]"><a-input v-model:value="cameraForm.channelRef" :maxlength="128" placeholder="例如 nvr01/channel01；不填摄像头密码" /></a-form-item>
        <a-form-item label="责任人"><a-input v-model:value="cameraForm.responsibleName" :maxlength="64" /></a-form-item>
        <a-form-item label="预计录像码率" name="bitrateMbps" :rules="[{ required: true, type: 'number', min: 0.1, max: 100 }]"><a-input-number v-model:value="cameraForm.bitrateMbps" :min="0.1" :max="100" :precision="2" addon-after="Mbps" /></a-form-item>
        <a-form-item label="启用接收录像"><a-switch v-model:checked="cameraEnabled" /></a-form-item>
        <a-alert type="info" show-icon message="停用仅停止本系统接收分片，不会远程关闭设备，也不删除历史录像。" />
      </a-form>
    </a-modal>

    <a-modal v-model:open="holdModal" :title="holdTarget?.held ? '解除事件保全' : '事件保全'" :confirm-loading="savingHold" @ok="submitHold">
      <p>{{ holdTarget?.held ? '解除后恢复原到期规则，已到期录像可能在下一次清理中删除。' : '保全后暂停自动清理，适用于事故、纠纷和调查取证。' }}</p>
      <a-form layout="vertical"><a-form-item label="操作原因（必填）"><a-textarea v-model:value="holdReason" :maxlength="255" show-count /></a-form-item></a-form>
    </a-modal>

    <a-modal v-model:open="importModal" title="导入录像分片" :confirm-loading="importing" @ok="submitImport">
      <a-alert type="info" show-icon message="用于补录与接入验收，持续录像由采集网关推送；支持可独立播放的MP4，单片不超过10分钟。" />
      <a-form layout="vertical">
        <a-form-item label="摄像头ID"><a-input v-model:value="importCameraId" /></a-form-item>
        <a-form-item label="录像起止时间（机构时区）"><a-range-picker v-model:value="importRange" show-time /></a-form-item>
        <a-form-item :label="`录像文件（最大 ${((overview?.maxSegmentBytes ?? 10485760) / 1024 / 1024).toFixed(0)} MB）`">
          <input ref="fileInput" type="file" accept="video/mp4,.mp4" aria-label="选择MP4录像分片" @change="selectFile" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal :open="playerOpen" title="录像回放" :footer="null" :width="900" destroy-on-close @cancel="closePlayer">
      <video v-if="playerUrl" :src="playerUrl" controls controlslist="nodownload" playsinline class="player" @error="playerFailed = true" />
      <a-alert v-if="playerFailed" type="error" show-icon message="录像播放失败，可能是链接过期或编码不受支持，请重新打开或检查分片编码。" />
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { message, type FormInstance } from 'ant-design-vue'
import type { Dayjs } from 'dayjs'
import dayjs from 'dayjs'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { getRoles } from '../../utils/auth'
import request from '../../utils/request'
import type { Id } from '../../types'
import { getVideoOverview, getVideoCameras, saveVideoCamera, saveVideoPolicy, getVideoRecordings, holdVideoRecording, getVideoPlayback, getVideoContent,
  type VideoCamera, type VideoPolicy, type VideoOverview, type VideoRecording } from '../../api/logisticsVideo'

const canManage = getRoles().some(role => ['LOGISTICS_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'].includes(role))
const tab = ref('cameras')
const overview = ref<VideoOverview>()
const loading = ref(false), loadError = ref(false), cameraLoading = ref(false), recordLoading = ref(false)
const cameras = ref<VideoCamera[]>([]), recordings = ref<VideoRecording[]>([])
const cameraPagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const recordPagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const cameraQuery = reactive({ keyword: '' })
const recordQuery = reactive({ cameraId: '', held: undefined as boolean | undefined })
const recordRange = ref<[Dayjs, Dayjs]>()
const holdOptions = [{ value: true, label: '已保全' }, { value: false, label: '未保全' }]
const protocolOptions = ['RTSP', 'ONVIF', 'GB28181', 'NVR'].map(value => ({ value, label: value }))
const cameraColumns = computed(() => [
  { title: '编码', dataIndex: 'cameraCode', width: 140 }, { title: '摄像头名称', dataIndex: 'cameraName', width: 160 },
  { title: '安装位置', dataIndex: 'location', width: 170 }, { title: '通道标识', dataIndex: 'channelRef', width: 160 },
  { title: '责任人', dataIndex: 'responsibleName', width: 100 }, { title: '分片接收状态 / 最近录像', key: 'state', width: 210 },
  ...(canManage ? [{ title: '操作', key: 'actions', width: 180 }] : [])
])
const recordColumns = [
  { title: '摄像头ID', dataIndex: 'cameraId', width: 180 }, { title: '录像时间', key: 'time', width: 200 },
  { title: '存储', key: 'provider', width: 100 }, { title: '大小', key: 'size', width: 100 },
  { title: '最早清理时间', key: 'expiry', width: 190 }, { title: '状态 / 原因', key: 'status', width: 170 },
  { title: '操作', key: 'actions', width: 190 }
]
const policyForm = reactive<VideoPolicy>({ provider: 'UNDECIDED', retentionDays: 90, autoCleanup: false, basis: '' })
const policyFormRef = ref<FormInstance>(), cameraFormRef = ref<FormInstance>()
const savingPolicy = ref(false), savingCamera = ref(false), cameraModal = ref(false)
const defaultCamera = (): VideoCamera => ({ cameraCode: '', cameraName: '', location: '', channelRef: '', protocol: 'NVR', responsibleName: '', enabled: 1, bitrateMbps: 2 })
const cameraForm = reactive<VideoCamera>(defaultCamera())
const cameraEnabled = computed({ get: () => cameraForm.enabled === 1, set: value => { cameraForm.enabled = value ? 1 : 0 } })
const holdModal = ref(false), savingHold = ref(false), holdTarget = ref<VideoRecording>(), holdReason = ref('')
const playerOpen = ref(false), playerUrl = ref(''), playerFailed = ref(false), playingId = ref<Id>()
const importModal = ref(false), importing = ref(false), importCameraId = ref(''), importRange = ref<[Dayjs, Dayjs]>()
const importFile = ref<File>(), fileInput = ref<HTMLInputElement>(), ingestKey = ref('')

function formatTime(value?: string) { return value ? dayjs(value).format('YYYY-MM-DD HH:mm:ss') : '尚无录像' }
function providerLabel(value: string) { return ({ LOCAL: '本地', OSS: '阿里云OSS', UNDECIDED: '未确定' } as Record<string, string>)[value] || value }
function health(camera: VideoCamera) { return overview.value?.cameraHealth.find(item => String(item.cameraId) === String(camera.id)) }
function healthLabel(camera: VideoCamera) {
  if (!camera.enabled) return '已停用'
  const state = health(camera)?.state
  return ({ NOT_CONNECTED: '待接入', STALE: '超过15分钟未收到录像', RECEIVING: '最近收到分片' } as Record<string, string>)[state || ''] || '状态未知'
}
function healthColor(camera: VideoCamera) { return !camera.enabled ? 'default' : health(camera)?.state === 'RECEIVING' ? 'green' : 'orange' }

async function loadOverview() {
  try {
    overview.value = await getVideoOverview()
    Object.assign(policyForm, overview.value.policy)
  } catch { overview.value = undefined; loadError.value = true }
}
async function loadCameras() {
  cameraLoading.value = true
  try {
    const result = await getVideoCameras({ pageNo: cameraPagination.current, pageSize: cameraPagination.pageSize, keyword: cameraQuery.keyword || undefined })
    cameras.value = result.list; cameraPagination.total = result.total
  } catch { cameras.value = []; cameraPagination.total = 0; loadError.value = true }
  finally { cameraLoading.value = false }
}
async function loadRecordings() {
  if (!canManage) return
  if (recordQuery.cameraId && !/^\d+$/.test(recordQuery.cameraId)) { message.warning('摄像头ID须为数字'); return }
  recordLoading.value = true
  try {
    const result = await getVideoRecordings({ pageNo: recordPagination.current, pageSize: recordPagination.pageSize,
      cameraId: recordQuery.cameraId || undefined, held: recordQuery.held,
      from: recordRange.value?.[0].format('YYYY-MM-DDTHH:mm:ss'), to: recordRange.value?.[1].format('YYYY-MM-DDTHH:mm:ss') })
    recordings.value = result.list; recordPagination.total = result.total
  } catch { recordings.value = []; recordPagination.total = 0; loadError.value = true }
  finally { recordLoading.value = false }
}
async function refresh() {
  loading.value = true; loadError.value = false
  try { await Promise.all([loadOverview(), loadCameras(), ...(tab.value === 'recordings' ? [loadRecordings()] : [])]) }
  finally { loading.value = false }
}
function searchCameras() { cameraPagination.current = 1; void loadCameras() }
function resetCameras() { cameraQuery.keyword = ''; searchCameras() }
function changeCameraPage(page: { current: number; pageSize: number }) { Object.assign(cameraPagination, page); void loadCameras() }
function searchRecordings() { recordPagination.current = 1; void loadRecordings() }
function resetRecordings() { recordQuery.cameraId = ''; recordQuery.held = undefined; recordRange.value = undefined; searchRecordings() }
function changeRecordPage(page: { current: number; pageSize: number }) { Object.assign(recordPagination, page); void loadRecordings() }
function viewRecordings(camera: VideoCamera) {
  recordQuery.cameraId = String(camera.id); recordPagination.current = 1
  if (tab.value === 'recordings') void loadRecordings(); else tab.value = 'recordings'
}
function editCamera(camera?: VideoCamera) {
  Object.assign(cameraForm, defaultCamera(), { id: undefined }, camera || {})
  cameraFormRef.value?.clearValidate(); cameraModal.value = true
}
async function submitCamera() {
  try { await cameraFormRef.value?.validate() } catch { return }
  savingCamera.value = true
  try { await saveVideoCamera(cameraForm); cameraModal.value = false; message.success('摄像头档案已保存'); await refresh() }
  catch { /* 请求层显示错误，保留表单 */ }
  finally { savingCamera.value = false }
}
async function submitPolicy() {
  try { await policyFormRef.value?.validate() } catch { return }
  savingPolicy.value = true
  try { await saveVideoPolicy(policyForm); message.success('存储策略已保存'); await loadOverview() }
  catch { /* 请求层显示错误，保留表单 */ }
  finally { savingPolicy.value = false }
}
function openHold(record: VideoRecording) { holdTarget.value = record; holdReason.value = ''; holdModal.value = true }
async function submitHold() {
  if (!holdReason.value.trim()) { message.warning('请填写操作原因'); return }
  if (!holdTarget.value) return
  savingHold.value = true
  try { await holdVideoRecording(holdTarget.value.id, !holdTarget.value.held, holdReason.value.trim()); holdModal.value = false; message.success('保全状态已更新'); await loadRecordings() }
  catch { /* 请求层显示错误 */ }
  finally { savingHold.value = false }
}
function closePlayer() {
  playerOpen.value = false
  if (playerUrl.value.startsWith('blob:')) URL.revokeObjectURL(playerUrl.value)
  playerUrl.value = ''; playerFailed.value = false
}
async function play(record: VideoRecording) {
  if (playingId.value) return
  playingId.value = record.id
  try {
    const result = await getVideoPlayback(record.id)
    closePlayer()
    playerUrl.value = result.provider === 'LOCAL' ? URL.createObjectURL(await getVideoContent(result.url)) : result.url
    playerOpen.value = true
  } catch { /* 请求层显示错误 */ }
  finally { playingId.value = undefined }
}
function openImport() {
  importCameraId.value = recordQuery.cameraId; importRange.value = undefined; importFile.value = undefined
  ingestKey.value = crypto.randomUUID(); importModal.value = true
  if (fileInput.value) fileInput.value.value = ''
}
function selectFile(event: Event) { importFile.value = (event.target as HTMLInputElement).files?.[0]; ingestKey.value = crypto.randomUUID() }
async function submitImport() {
  if (!/^\d+$/.test(importCameraId.value) || !importRange.value || !importFile.value) { message.warning('请填写摄像头ID、起止时间并选择MP4文件'); return }
  if (importFile.value.size > (overview.value?.maxSegmentBytes ?? 10485760)) { message.warning('录像分片超过服务端大小上限'); return }
  importing.value = true
  const form = new FormData()
  form.append('cameraId', importCameraId.value); form.append('ingestKey', ingestKey.value)
  form.append('startedAt', importRange.value[0].format('YYYY-MM-DDTHH:mm:ss'))
  form.append('endedAt', importRange.value[1].format('YYYY-MM-DDTHH:mm:ss')); form.append('file', importFile.value)
  try {
    await request.post('/api/logistics/video/recordings', form, { headers: { 'Content-Type': 'multipart/form-data' } })
    importModal.value = false; message.success('录像分片已存储'); await refresh()
  } catch { /* 保留幂等键和表单，支持重试 */ }
  finally { importing.value = false }
}
watch(tab, value => { if (value === 'recordings') void loadRecordings() })
watch(() => policyForm.provider, value => { if (value === 'UNDECIDED') policyForm.autoCleanup = false })
onMounted(refresh)
onBeforeUnmount(closePlayer)
</script>

<style scoped>
.metrics { margin: 18px 0; }
.secondary { color: var(--text-secondary, #595959); font-size: 13px; line-height: 1.6; margin-top: 6px; overflow-wrap: anywhere; }
.provider-options { display: flex; flex-wrap: wrap; gap: 12px; }
.save-policy { margin-top: 20px; }
.player { width: 100%; max-height: 65vh; background: #111; }
:deep(.ant-alert) { margin-bottom: 12px; }
@media (max-width: 600px) { :deep(.ant-picker-range) { width: 100%; } }
</style>
