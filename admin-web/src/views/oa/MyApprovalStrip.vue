<template>
  <section class="mas">
    <div class="mas-cards">
      <button v-if="isApprover" type="button" class="mas-card mas-card--warn" @click="router.push('/oa/approval?scope=PENDING_REVIEW')">
        <span>待我审批</span><b>{{ pendingReview }}</b><small>点击办理</small>
      </button>
      <button type="button" class="mas-card" @click="router.push('/oa/approval?scope=MY')">
        <span>我发起的（审批中）</span><b>{{ myPending }}</b><small>查看进度</small>
      </button>
      <template v-if="applyTypes.length">
        <button v-for="t in applyTypes" :key="t.type" type="button" class="mas-card mas-card--apply" @click="router.push(`/oa/approval?type=${t.type}&quick=1`)">
          <span>{{ t.label }}</span><b>＋</b><small>发起申请</small>
        </button>
      </template>
    </div>
    <div v-if="applyTypes.length" class="mas-recent">
      <div class="mas-recent__head"><b>我的申请记录</b><a @click="router.push('/oa/approval?scope=MY')">全部 →</a></div>
      <a-empty v-if="!recent.length" description="暂无申请" :image-style="{ height: '32px' }" />
      <div v-for="item in recent" :key="String(item.id)" class="mas-row">
        <a-tag>{{ TYPE_LABEL[item.approvalType] || item.approvalType }}</a-tag>
        <span class="mas-row__title">{{ item.title }}</span>
        <span class="mas-muted">{{ item.startTime ? dayjs(item.startTime).format('MM-DD HH:mm') : '' }}{{ item.endTime ? ` 至 ${dayjs(item.endTime).format('MM-DD HH:mm')}` : '' }}</span>
        <a-tag :color="STATUS[item.status || '']?.color">{{ STATUS[item.status || '']?.label || item.status }}</a-tag>
      </div>
      <p class="mas-muted">漏打卡补卡由人事在「考勤核定与调整」开调整单办理，请联系人事或班组长。</p>
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import dayjs from 'dayjs'
import { useRouter } from 'vue-router'
import { getApprovalPage } from '../../api/oa'
import type { OaApproval } from '../../types'
import { useUserStore } from '../../stores/user'
import { hasMinisterOrHigher } from '../../utils/roleAccess'

const props = withDefaults(defineProps<{ applyTypes?: { type: string; label: string }[] }>(), { applyTypes: () => [] })
const TYPE_LABEL: Record<string, string> = { LEAVE: '请假', OVERTIME: '加班', SHIFT_CHANGE: '调班' }
const STATUS: Record<string, { label: string; color: string }> = {
  PENDING: { label: '审批中', color: 'orange' }, APPROVED: { label: '已通过', color: 'green' },
  REJECTED: { label: '已驳回', color: 'red' }, CANCELLED: { label: '已撤回', color: 'default' }
}

const router = useRouter()
const isApprover = hasMinisterOrHigher(useUserStore().roles || [])
const pendingReview = ref<number | string>('--')
const myPending = ref<number | string>('--')
const recent = ref<OaApproval[]>([])

async function load() {
  const total = (r: PromiseSettledResult<{ total?: number }>) => (r.status === 'fulfilled' ? Number(r.value.total || 0) : '--')
  const [review, mine, ...typed] = await Promise.allSettled([
    isApprover ? getApprovalPage({ pageNo: 1, pageSize: 1, status: 'PENDING', pendingMine: true }) : Promise.resolve({ total: 0 }),
    getApprovalPage({ pageNo: 1, pageSize: 1, status: 'PENDING', mine: true }),
    ...props.applyTypes.map((t) => getApprovalPage({ pageNo: 1, pageSize: 5, mine: true, type: t.type }))
  ])
  pendingReview.value = total(review)
  myPending.value = total(mine)
  recent.value = typed.flatMap((r) => (r.status === 'fulfilled' ? (r.value as { list?: OaApproval[] }).list || [] : []))
    .sort((a, b) => String(b.startTime || '').localeCompare(String(a.startTime || ''))).slice(0, 6)
}
onMounted(load)
</script>

<style scoped>
.mas { margin-bottom: 16px; }
.mas-cards { display: grid; grid-template-columns: repeat(auto-fill, minmax(150px, 1fr)); gap: 12px; }
.mas-card { display: grid; gap: 2px; padding: 12px 14px; border: 1px solid #e3ebe6; border-radius: 12px; background: #fff; text-align: left; cursor: pointer; }
.mas-card span { color: #4f6258; font-size: 13px; }
.mas-card b { font-size: 24px; color: #1f6f55; }
.mas-card small { color: #8a968f; }
.mas-card--warn b { color: #c27c1a; }
.mas-card--apply { background: #f3faf6; }
.mas-card:hover { border-color: #1f9d74; }
.mas-recent { margin-top: 12px; padding: 12px 14px; border-radius: 12px; background: #fff; border: 1px solid #e6ece8; }
.mas-recent__head { display: flex; justify-content: space-between; margin-bottom: 6px; }
.mas-row { display: flex; align-items: center; gap: 10px; padding: 6px 0; border-bottom: 1px solid #f0f3f1; font-size: 13px; }
.mas-row__title { flex: 1; min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.mas-muted { color: #8a968f; font-size: 12px; }
</style>
