<template>
  <div class="of-page">
    <OfficeHero>当前有 {{ summary.openCount || 0 }} 项待办事项，请及时处理，保障工作高效推进！</OfficeHero>

    <div class="of-grid-2">
      <div class="of-page">
        <div class="tov-tiles">
          <button type="button" class="tone-green" @click="emit('filter', '')"><UnorderedListOutlined /><b>{{ summary.totalCount || 0 }}</b><span>全部待办</span></button>
          <button v-if="isApprover" type="button" class="tone-orange" @click="router.push('/oa/approval?scope=PENDING_REVIEW')"><AuditOutlined /><b>{{ pendingReview }}</b><span>待我审批</span></button>
          <button type="button" class="tone-blue" @click="emit('filter', 'OPEN')"><CheckSquareOutlined /><b>{{ summary.openCount || 0 }}</b><span>待我处理</span></button>
          <button type="button" class="tone-purple" @click="router.push('/oa/approval?scope=MY')"><SendOutlined /><b>{{ myPending }}</b><span>我发起的</span></button>
          <button type="button" class="tone-green tov-done" @click="emit('filter', 'DONE')"><CheckCircleOutlined /><b>{{ summary.doneCount || 0 }}</b><span>已完成</span></button>
        </div>
        <article class="of-card">
          <div class="of-card__head"><h3><PieChartOutlined />待办处理进度</h3></div>
          <div class="tov-progress">
            <div class="of-donut" :style="{ background: donut }"><div><small>处理率</small><b>{{ rate }}%</b></div></div>
            <div class="of-legend">
              <div><i style="background: #1f9d74"></i>已完成<b>{{ summary.doneCount || 0 }} 项</b></div>
              <div><i style="background: #f0b429"></i>今日到期<b>{{ summary.dueTodayCount || 0 }} 项</b></div>
              <div><i style="background: #e5675a"></i>已逾期<b>{{ summary.overdueCount || 0 }} 项</b></div>
              <div><i style="background: #c9d2cd"></i>待处理<b>{{ summary.openCount || 0 }} 项</b></div>
            </div>
          </div>
        </article>
      </div>

      <div class="of-page">
        <article class="of-card">
          <div class="of-card__head"><h3><BellOutlined />紧急提醒 <small>（{{ urgent.length }}）</small></h3><button type="button" class="of-link" @click="emit('filter', 'OPEN')">查看全部 ›</button></div>
          <div v-if="!urgent.length" class="of-empty">没有 24 小时内到期的待办</div>
          <div v-for="item in urgent.slice(0, 3)" :key="String(item.id)" class="of-row">
            <FireOutlined class="tov-fire" />
            <div class="of-row__main"><b>{{ item.title }}</b><small>{{ dueText(item) }}</small></div>
            <span class="of-pill" :class="overdue(item) ? 'tone-red' : 'tone-orange'">{{ overdue(item) ? '超时' : '紧急' }}</span>
          </div>
        </article>
        <article class="of-card">
          <div class="of-card__head"><h3><ThunderboltOutlined />常用操作</h3></div>
          <div class="of-tile-grid">
            <button v-for="t in tiles" :key="t.label" type="button" class="of-tile" :class="t.tone" @click="t.action()"><component :is="t.icon" /><span>{{ t.label }}</span></button>
          </div>
        </article>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import dayjs from 'dayjs'
import { useRouter } from 'vue-router'
import {
  AuditOutlined, BellOutlined, BookOutlined, CheckCircleOutlined, CheckSquareOutlined, FileSearchOutlined, FireOutlined,
  MessageOutlined, PieChartOutlined, PlusCircleOutlined, SendOutlined, ThunderboltOutlined, UnorderedListOutlined
} from '@ant-design/icons-vue'
import { getApprovalPage, getTodoPage } from '../../api/oa'
import type { OaTodo, OaTodoSummary } from '../../types'
import { useUserStore } from '../../stores/user'
import { hasMinisterOrHigher } from '../../utils/roleAccess'
import { QUICK_CHAT_OPEN_EVENT } from '../../utils/quickChatPreview'
import OfficeHero from './office/OfficeHero.vue'
import './office/office.css'

const props = defineProps<{ summary: OaTodoSummary; refreshKey?: number }>()
const emit = defineEmits<{ filter: [status: string]; create: [] }>()
const router = useRouter()
const isApprover = hasMinisterOrHigher(useUserStore().roles || [])
const pendingReview = ref<number | string>('--')
const myPending = ref<number | string>('--')
const urgent = ref<OaTodo[]>([])

const rate = computed(() => {
  const total = Number(props.summary.totalCount || 0)
  return total ? Math.round((Number(props.summary.doneCount || 0) / total) * 100) : 0
})
const donut = computed(() => `conic-gradient(#1f9d74 0 ${rate.value * 3.6}deg, #e9eeeb ${rate.value * 3.6}deg 360deg)`)
const overdue = (item: OaTodo) => Boolean(item.dueTime && dayjs(item.dueTime).isBefore(dayjs()))
function dueText(item: OaTodo) {
  if (!item.dueTime) return ''
  const due = dayjs(item.dueTime)
  const hours = Math.abs(due.diff(dayjs(), 'hour'))
  return overdue(item) ? `已超时 ${hours} 小时，请尽快处理` : `${due.format('MM-DD HH:mm')} 截止（剩 ${hours} 小时）`
}
const tiles = [
  { label: '新增待办', icon: PlusCircleOutlined, tone: 'tone-green', action: () => emit('create') },
  { label: '发起申请', icon: SendOutlined, tone: 'tone-blue', action: () => router.push('/oa/approval?quick=1') },
  { label: '流程查询', icon: FileSearchOutlined, tone: 'tone-purple', action: () => router.push('/oa/approval?scope=MY') },
  { label: '制度规范', icon: BookOutlined, tone: 'tone-orange', action: () => router.push('/oa/document') },
  // 「在线咨询」打开站内快捷聊天，不是外部客服
  { label: '在线咨询', icon: MessageOutlined, tone: 'tone-green', action: () => window.dispatchEvent(new CustomEvent(QUICK_CHAT_OPEN_EVENT)) }
]

async function load() {
  const total = (r: PromiseSettledResult<{ total?: number }>) => (r.status === 'fulfilled' ? Number(r.value.total || 0) : '--')
  const [review, mine, open] = await Promise.allSettled([
    isApprover ? getApprovalPage({ pageNo: 1, pageSize: 1, status: 'PENDING', pendingMine: true }) : Promise.resolve({ total: 0 }),
    getApprovalPage({ pageNo: 1, pageSize: 1, status: 'PENDING', mine: true }),
    getTodoPage({ pageNo: 1, pageSize: 50, status: 'OPEN', mineOnly: true })
  ])
  pendingReview.value = total(review)
  myPending.value = total(mine)
  const soon = dayjs().add(24, 'hour')
  urgent.value = (open.status === 'fulfilled' ? open.value.list || [] : [])
    .filter((t) => t.dueTime && dayjs(t.dueTime).isBefore(soon))
    .sort((a, b) => String(a.dueTime).localeCompare(String(b.dueTime)))
}
onMounted(load)
watch(() => props.refreshKey, load)
</script>

<style scoped>
.tov-tiles { display: grid; grid-template-columns: repeat(auto-fit, minmax(120px, 1fr)); gap: 12px; }
.tov-tiles button { display: grid; justify-items: center; gap: 4px; padding: 16px 8px; border: 0; border-radius: 16px; cursor: pointer; font-size: 22px; }
.tov-tiles b { font-size: 30px; line-height: 1.1; }
.tov-tiles span { color: #33433a; font-size: 14px; }
.tov-done { background: #f2f8f0; }
.tov-progress { display: flex; align-items: center; gap: 24px; }
.tov-fire { color: #e5675a; font-size: 18px; }
</style>
