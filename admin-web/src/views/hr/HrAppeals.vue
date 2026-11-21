<template>
  <PageContainer title="绩效与积分申诉" sub-title="员工对积分、奖惩或绩效结果提出异议；人事主管复核，积分申诉成立时自动冲正，原记录保留">
    <a-radio-group v-model:value="status" style="margin-bottom: 12px" @change="load">
      <a-radio-button value="PENDING">待复核</a-radio-button>
      <a-radio-button value="">全部</a-radio-button>
    </a-radio-group>
    <a-table size="small" row-key="id" :data-source="rows" :columns="columns" :pagination="false">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'target'">{{ TARGET[record.targetType] }}{{ record.targetId ? ` #${record.targetId}` : '' }}</template>
        <template v-else-if="column.key === 'status'">
          <a-tag :color="record.status === 'ACCEPTED' ? 'green' : record.status === 'REJECTED' ? 'red' : 'orange'">{{ STATUS[record.status] }}</a-tag>
          <div v-if="record.decisionNote" class="ap-muted">{{ record.reviewerName }}：{{ record.decisionNote }}</div>
        </template>
        <template v-else-if="column.key === 'action'">
          <a v-if="record.status === 'PENDING' && isManager" @click="open(record)">复核</a>
        </template>
      </template>
    </a-table>
    <a-modal v-model:open="reviewOpen" :title="`复核 · ${target?.staffName || ''}`" @ok="submit">
      <p>{{ target?.reason }}</p>
      <a-radio-group v-model:value="accept"><a-radio :value="true">申诉成立</a-radio><a-radio :value="false">维持原结果</a-radio></a-radio-group>
      <a-textarea v-model:value="note" :rows="2" style="margin-top: 12px" placeholder="复核意见（必填，会告知员工）" />
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import { decideAppeal, getAppeals, type PerformanceAppeal } from '../../api/hrOps'
import { useUserStore } from '../../stores/user'

const TARGET: Record<string, string> = { POINTS_LOG: '积分记录', REWARD_PUNISHMENT: '奖惩记录', PERFORMANCE: '绩效结果' }
const STATUS: Record<string, string> = { PENDING: '待复核', ACCEPTED: '申诉成立', REJECTED: '维持原结果' }
const columns = [
  { title: '员工', dataIndex: 'staffName', width: 100 }, { title: '申诉对象', key: 'target', width: 140 },
  { title: '理由', dataIndex: 'reason' }, { title: '状态', key: 'status', width: 220 }, { title: '', key: 'action', width: 70 }
]
const user = useUserStore()
const isManager = computed(() => (user.roles || []).some((r) => ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'].includes(String(r).toUpperCase())))
const rows = ref<PerformanceAppeal[]>([])
const status = ref('PENDING')
const reviewOpen = ref(false)
const target = ref<PerformanceAppeal>()
const accept = ref(true)
const note = ref('')

async function load() {
  rows.value = (await getAppeals({ pageNo: 1, pageSize: 50, status: status.value || undefined })).list || []
}
function open(row: PerformanceAppeal) {
  target.value = row
  accept.value = true
  note.value = ''
  reviewOpen.value = true
}
async function submit() {
  if (!note.value.trim()) return message.warning('请填写复核意见')
  await decideAppeal(target.value!.id, accept.value, note.value.trim())
  message.success(accept.value ? '申诉成立，相关积分已冲正' : '已维持原结果')
  reviewOpen.value = false
  await load()
}
onMounted(load)
</script>

<style scoped>
.ap-muted { color: #7b877e; font-size: 12px; }
</style>
