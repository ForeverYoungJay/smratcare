<template>
  <div class="of-page sop">
    <OfficeHero>合理安排每日工作，及时跟进服务事项，守护长者的健康与生活品质！</OfficeHero>
    <div class="of-kpis">
      <div v-for="card in cards" :key="card.label" class="of-kpi sop-card">
        <span class="of-kpi__icon" :class="card.tone"><component :is="card.icon" /></span>
        <div class="of-kpi__body">
          <span>{{ card.label }}</span>
          <b>{{ card.items.length }}<small>{{ card.unit }}</small></b>
          <em>{{ card.items.slice(0, 2).map(label).join('、') || card.empty }}</em>
        </div>
      </div>
    </div>
    <MyShiftStrip />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import dayjs from 'dayjs'
import { BellOutlined, CalendarOutlined, CarOutlined, TeamOutlined } from '@ant-design/icons-vue'
import { getOaTaskCalendar } from '../../api/oa'
import type { OaTask } from '../../types'
import OfficeHero from './office/OfficeHero.vue'
import MyShiftStrip from './MyShiftStrip.vue'
import './office/office.css'

const props = defineProps<{ refreshKey?: number }>()
const today = ref<OaTask[]>([])
const label = (t: OaTask) => `${t.title}${t.startTime ? `（${dayjs(t.startTime).format('HH:mm')}）` : ''}`
// 会议、外出按日程分类和标题识别：分类是「协同会议」或标题含会议/外出/出诊/陪同
const isMeeting = (t: OaTask) => t.planCategory === '协同会议' || /会议|例会|讨论会/.test(t.title || '')
const isOuting = (t: OaTask) => /外出|出诊|陪同|陪诊/.test(`${t.title || ''}${t.planCategory || ''}`)
const cards = computed(() => [
  { label: '今日安排', unit: '项', tone: 'tone-green', icon: CalendarOutlined, items: today.value, empty: '今日暂无安排' },
  { label: '待完成事项', unit: '项', tone: 'tone-orange', icon: BellOutlined, items: today.value.filter((t) => t.status !== 'DONE'), empty: '今日事项已全部完成' },
  { label: '会议', unit: '场', tone: 'tone-purple', icon: TeamOutlined, items: today.value.filter(isMeeting), empty: '今日没有会议' },
  { label: '外出安排', unit: '项', tone: 'tone-blue', icon: CarOutlined, items: today.value.filter(isOuting), empty: '今日没有外出' }
])

async function load() {
  const day = dayjs().format('YYYY-MM-DD')
  today.value = await getOaTaskCalendar({ startDate: day, endDate: day }).catch(() => [])
}
onMounted(load)
watch(() => props.refreshKey, load)
</script>

<style scoped>
.sop { margin-bottom: 16px; }
.sop-card { cursor: default; }
.sop-card em { display: block; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
</style>
